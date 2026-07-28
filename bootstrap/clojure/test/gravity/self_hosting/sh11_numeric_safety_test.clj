(ns gravity.self-hosting.sh11-numeric-safety-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource "gravity/self_hosting/sh11_numeric_safety_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-11 test source is not on the classpath"
                {:id "SH11-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "SH11-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(def ^:private c10-source-relative-path
  "bootstrap/gravity/src/gravity/compiler/c10_safety_analysis_pipeline.gravity")

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-11")

(defn- fixture-relative-path
  [family basename extension]
  (str fixture-root "/" family "/" basename extension))

(defn- compile-plan
  [relative-path]
  (let [source-path (path relative-path)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private c10-plan
  (delay (compile-plan c10-source-relative-path)))

(def ^:private accepted-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "accepted" "numeric-safety-outcomes" ".gravity"))))

(def ^:private accepted-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "accepted" "numeric-safety-outcomes" ".qst"))))

(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-numeric-safety" ".gravity"))))

(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-numeric-safety" ".qst"))))

(defn- invoke
  [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh11-numeric-safety-leaf
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- invoke-c10
  [function arguments]
  (invoke c10-plan function arguments))

(defn- request
  [plan function]
  (invoke plan function []))

(defn- classify
  [request]
  (invoke-c10 'sh11-classify-operation [request]))

(def ^:private accepted-functions
  '[sh11-proven-index-request
    sh11-proven-overflow-request
    sh11-proven-wrapping-request
    sh11-proven-division-request
    sh11-proven-cast-request
    sh11-proven-shift-request
    sh11-runtime-division-request
    sh11-unsafe-overflow-request])

(def ^:private rejected-cases
  {'sh11-out-of-bounds-request
   ["C10-NUMERIC"
    :numeric-operation-lacks-proof-check-or-audit
    :bounds-safety]
   'sh11-overflow-request
   ["C10-NUMERIC"
    :numeric-operation-lacks-proof-check-or-audit
    :SAFE9-OVERFLOW]
   'sh11-zero-divisor-request
   ["C10-NUMERIC"
    :numeric-operation-lacks-proof-check-or-audit
    :SAFE9-DIV-ZERO]
   'sh11-narrowing-request
   ["C10-NUMERIC"
    :numeric-operation-lacks-proof-check-or-audit
    :SAFE9-NARROW]
   'sh11-invalid-shift-request
   ["C10-NUMERIC"
    :numeric-operation-lacks-proof-check-or-audit
    :SAFE9-SHIFT]
   'sh11-invalid-check-request
   ["C10-CHECK" :invalid-runtime-check :SAFE9-DIV-ZERO]
   'sh11-unsafe-safe-mode-request
   ["C10-UNSAFE"
    :unsafe-island-policy-or-metadata-gap
    :SAFE9-OVERFLOW]
   'sh11-incomplete-audit-request
   ["C10-UNSAFE"
    :unsafe-island-policy-or-metadata-gap
    :SAFE9-OVERFLOW]
   'sh11-ambiguous-evidence-request
   ["C10-NO-OUTCOME"
    :multiple-outcome-evidence
    :SAFE9-DIV-ZERO]
   'sh11-malformed-request
   ["C10-NO-OUTCOME"
    :malformed-safety-operation
    :bounds-safety]})

(deftest sh11-source-and-fixtures-compile-as-gravity
  (is (= :gravity/stage2-compiler-artifact-plan (:kind @c10-plan)))
  (is (= :meta (get-in @c10-plan [:module :profile])))
  (is (= :jvm (get-in @c10-plan [:module :target])))
  (doseq [function
          '[sh11-safety-policy
            sh11-classify-operation
            sh11-verify-safety-result]]
    (is (map? (get-in @c10-plan [:functions function])) function))
  (let [policy (invoke-c10 'sh11-safety-policy [])]
    (is (= :gravity/sh11-safety-policy (:artifact policy)))
    (is (= 2 (:version policy)))
    (is (= #{:proven-safe
             :runtime-checked
             :rejected
             :unsafe-island}
           (:outcomes policy)))
    (is (= :bounds (:index (:runtime-conditions policy))))
    (is (= :SAFE9-DIV-ZERO
           (:division (:specialized-rules policy))))
    (is (= {:index
            #{:proof-required :checked :panic :unsafe-unchecked}
            :numeric-overflow
            #{:proof-required :checked :panic :wrapping
              :saturating :arbitrary-precision :unsafe-unchecked}
            :division
            #{:proof-required :checked :panic :unsafe-unchecked}
            :numeric-cast
            #{:proof-required :checked :panic :wrapping
              :saturating :unsafe-unchecked}
            :shift
            #{:proof-required :checked :panic
              :wrapping :unsafe-unchecked}}
           (:legal-modes policy)))
    (is (= 16
           (get-in policy [:structural-bounds :maximum-depth])))
    (is (not (some #{"C10-PROOF"} (:diagnostics policy))))
    (is (some #{:memory-safety} (:pending policy)))
    (is (some #{:mir-preservation} (:pending policy))))
  (is (= (slurp
          (path
           (fixture-relative-path
            "accepted" "numeric-safety-outcomes" ".gravity")))
         (slurp
          (path
           (fixture-relative-path
            "accepted" "numeric-safety-outcomes" ".qst")))))
  (is (= (slurp
          (path
           (fixture-relative-path
            "rejected" "invalid-numeric-safety" ".gravity")))
         (slurp
          (path
           (fixture-relative-path
            "rejected" "invalid-numeric-safety" ".qst")))))
  (doseq [plan [accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan)))))

(deftest sh11-classifies-every-supported-operation-into-one-outcome
  (let [gravity-requests
        (mapv #(request accepted-gravity-plan %) accepted-functions)
        qst-requests
        (mapv #(request accepted-qst-plan %) accepted-functions)
        results (mapv classify gravity-requests)
        [index overflow wrapping division cast shift checked unsafe]
        results]
    (is (= gravity-requests qst-requests))
    (is (= results (mapv classify qst-requests)))
    (is (= (vec (repeat 8 :accepted)) (mapv :status results)))
    (is (= [:proven-safe :proven-safe :proven-safe
            :proven-safe :proven-safe :proven-safe
            :runtime-checked :unsafe-island]
           (mapv :outcome results)))
    (doseq [[request result] (map vector gravity-requests results)]
      (is (= 1 (count (:outcomes result))))
      (is (= (:outcome result)
             (get-in result [:outcomes 0 :outcome])))
      (is (= request (get-in result [:identity-input :request])))
      (is (= (:core-node-id request)
             (get-in result [:preserves :core-node-id])))
      (is (= (:type-fact-id request)
             (get-in result [:preserves :type-fact-id])))
      (is (= (:effect-fact-id request)
             (get-in result [:preserves :effect-fact-id])))
      (is (= (:ownership-fact-id request)
             (get-in result [:preserves :ownership-fact-id])))
      (is (= (:source-span request)
             (get-in result [:preserves :source-span])))
      (is (= :passed
             (:status
              (invoke-c10
               'sh11-verify-safety-result [request result])))))
    (doseq [result [index overflow wrapping division cast shift]]
      (is (= 1 (count (:proofs result))))
      (is (empty? (:runtime-checks result)))
      (is (empty? (:unsafe-islands result)))
      (is (= :gravity/sh11-static-safety-proof
             (get-in result [:proofs 0 :artifact])))
      (is (string? (get-in result [:proofs 0 :proof-id])))
      (is (= 71 (count (get-in result [:proofs 0 :proof-id]))))
      (is (seq (get-in result [:proofs 0 :assumptions])))
      (is (seq (get-in result
                       [:proofs 0 :invalidation-conditions]))))
    (is (= :wrapping
           (get-in wrapping [:proofs 0 :facts :numeric-mode])))
    (is (= :valid-divisor-and-quotient
           (get-in checked [:runtime-checks 0 :condition])))
    (is (= :error/numeric
           (get-in checked [:runtime-checks 0 :failure-behavior])))
    (is (= true
           (get-in checked
                   [:runtime-checks 0
                    :guard-proof :guards-exact-operation])))
    (is (= 1 (count (:unsafe-islands unsafe))))
    (is (= 71
           (count
            (get-in unsafe [:unsafe-islands 0 :audit-id]))))))

(deftest sh11-enforces-each-supported-mode-semantics
  (let [overflow
        (request accepted-gravity-plan 'sh11-proven-overflow-request)
        cast
        (request accepted-gravity-plan 'sh11-proven-cast-request)
        shift
        (request accepted-gravity-plan 'sh11-proven-shift-request)
        runtime
        (request accepted-gravity-plan 'sh11-runtime-division-request)
        saturating
        (-> overflow
            (assoc-in [:facts :numeric-mode] :saturating)
            (assoc-in [:facts :left] 2147483647)
            (assoc-in [:facts :right] 1))
        arbitrary
        (-> overflow
            (assoc-in [:facts :numeric-mode] :arbitrary-precision)
            (assoc-in [:facts :left] 2147483647)
            (assoc-in [:facts :right] 1)
            (assoc-in [:facts :arbitrary-precision-supported] true))
        wrapping-cast
        (-> cast
            (assoc-in [:facts :numeric-mode] :wrapping)
            (assoc-in [:facts :conversion] :wrapping-narrowing)
            (assoc-in [:facts :value] 128))
        saturating-cast
        (-> cast
            (assoc-in [:facts :numeric-mode] :saturating)
            (assoc-in [:facts :conversion] :saturating-narrowing)
            (assoc-in [:facts :value] 128))
        masked-shift
        (-> shift
            (assoc-in [:facts :numeric-mode] :wrapping)
            (assoc-in [:facts :shift-semantics] :masked)
            (assoc-in [:facts :shift-count] 8))
        panic
        (-> runtime
            (assoc-in [:facts :numeric-mode] :panic)
            (assoc-in [:runtime-check :failure-behavior]
                      :panic/numeric)
            (assoc-in [:runtime-check :effects-introduced]
                      #{:panic})
            (assoc-in [:runtime-check-support :failure-behaviors]
                      #{:panic/numeric})
            (assoc-in [:runtime-check-support :effects]
                      #{:panic}))
        proven [saturating arbitrary wrapping-cast
                saturating-cast masked-shift]]
    (doseq [request proven]
      (let [result (classify request)]
        (is (= :accepted (:status result)))
        (is (= :proven-safe (:outcome result)))))
    (let [result (classify panic)]
      (is (= :accepted (:status result)))
      (is (= :runtime-checked (:outcome result)))
      (is (= :panic/numeric
             (get-in result
                     [:runtime-checks 0 :failure-behavior]))))))

(deftest sh11-rejects-unresolved-and-invalid-numeric-safety
  (doseq [[function [rule reason specialized-rule]] rejected-cases]
    (testing (str function)
      (let [gravity-request (request rejected-gravity-plan function)
            qst-request (request rejected-qst-plan function)
            gravity-result (classify gravity-request)
            qst-result (classify qst-request)
            diagnostic (first (:diagnostics gravity-result))]
        (is (= gravity-request qst-request))
        (is (= gravity-result qst-result))
        (is (= :rejected (:status gravity-result)))
        (is (= :rejected (:outcome gravity-result)))
        (is (= 1 (count (:outcomes gravity-result))))
        (is (= :rejected
               (get-in gravity-result [:outcomes 0 :outcome])))
        (is (= 1 (count (:diagnostics gravity-result))))
        (is (= rule (:rule diagnostic) (:diagnostic-id diagnostic)))
        (is (= reason (:reason diagnostic)))
        (is (= specialized-rule (:specialized-safe-rule diagnostic)))
        (is (= :safety-analysis (:stage diagnostic)))
        (is (= (:operation-id gravity-request)
               (:operation-id diagnostic)))
        (is (= (:source-span gravity-request)
               (:source-span diagnostic)))
        (is (= (:origin-chain gravity-request)
               (:generated-origin-chain diagnostic)))
        (is (= (:profile gravity-request) (:profile diagnostic)))
        (is (= (:target gravity-request) (:target diagnostic)))
        (is (keyword? (:remediation diagnostic)))))))

(deftest sh11-fails-closed-on-schema-mode-lineage-and-structural-attacks
  (let [base
        (request accepted-gravity-plan 'sh11-proven-overflow-request)
        division
        (request accepted-gravity-plan 'sh11-proven-division-request)
        index
        (request accepted-gravity-plan 'sh11-proven-index-request)
        shift
        (request accepted-gravity-plan 'sh11-proven-shift-request)
        runtime
        (request accepted-gravity-plan 'sh11-runtime-division-request)
        unsafe
        (request accepted-gravity-plan 'sh11-unsafe-overflow-request)
        malformed
        [(assoc-in base [:facts :numeric-mode] :future/mode)
         (assoc base :facts
                {:numeric-mode :wrapping :bit-width 3.5})
         (update division :facts dissoc :dividend)
         (assoc-in index [:facts :index] 0.5)
         (assoc-in shift [:facts :shift-count] 0.5)
         (assoc-in base [:facts :result-max] 999)
         (assoc-in index [:facts :index-width] 7)
         (dissoc base :type-fact-id)
         (dissoc base :effect-fact-id)
         (dissoc base :capability-proof-id)
         (dissoc base :ownership-fact-id)
         (assoc base :source-span {})
         (assoc base :origin-chain [])
         (assoc-in division [:facts :numeric-mode] :wrapping)]
        malformed-results (mapv classify malformed)
        bounds-result
        (classify
         (assoc base :origin-chain
                (vec
                 (repeat
                  100
                  "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"))))]
    (doseq [result malformed-results]
      (is (= :rejected (:status result)))
      (is (= "C10-NO-OUTCOME"
             (get-in result [:diagnostics 0 :rule])))
      (is (= :malformed-safety-operation
             (get-in result [:diagnostics 0 :reason]))))
    (is (= "C10-NO-OUTCOME"
           (get-in bounds-result [:diagnostics 0 :rule])))))

(deftest sh11-fails-closed-on-runtime-unsafe-and-result-attacks
  (let [base
        (request accepted-gravity-plan 'sh11-proven-overflow-request)
        runtime
        (request accepted-gravity-plan 'sh11-runtime-division-request)
        unsafe
        (request accepted-gravity-plan 'sh11-unsafe-overflow-request)
        runtime-attacks
        [(update runtime :runtime-check dissoc :performance-class)
         (assoc-in runtime
                   [:runtime-check :invalidation-conditions] [])
         (assoc-in runtime
                   [:runtime-check :effects-introduced] #{})
         (-> runtime
             (assoc-in [:facts :numeric-mode] :panic)
             (assoc-in [:runtime-check :failure-behavior]
                       :error/numeric))
         (assoc-in runtime
                   [:runtime-check :predicate :operands :divisor] 1)
         (assoc-in runtime
                   [:runtime-check :target-support-id]
                   "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
         (update runtime :runtime-check-support dissoc :provider-id)]
        unsafe-attacks
        [(update unsafe :unsafe-audit dissoc :operation)
         (update unsafe :unsafe-audit dissoc :re-review)
         (assoc-in unsafe
                   [:unsafe-audit :source-span :start-byte] 1)
         (assoc-in unsafe
                   [:unsafe-audit :generated-origin-chain]
                   ["sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"])
         (assoc-in unsafe [:unsafe-audit :review] true)
         (assoc-in unsafe [:unsafe-audit :preconditions] #{})
         (assoc-in unsafe [:unsafe-audit :postconditions] #{})
         (assoc-in unsafe [:unsafe-audit :invariants] #{})
         (assoc-in unsafe [:unsafe-audit :evidence] #{})
         (assoc-in unsafe
                   [:unsafe-audit :policy
                    :empty-effects-approved] false)]
        unchecked-safe
        (classify
         (assoc-in base [:facts :numeric-mode] :unsafe-unchecked))
        valid-result (classify base)
        substitutions
        [(assoc valid-result :status :substituted)
         (assoc-in valid-result
                   [:outcomes 0 :outcome] :runtime-checked)]
        oversized-candidate
        (assoc valid-result :padding (vec (repeat 100 :x)))]
    (doseq [request runtime-attacks]
      (let [result (classify request)]
        (is (= :rejected (:status result)))
        (is (= "C10-CHECK"
               (get-in result [:diagnostics 0 :rule])))))
    (doseq [request unsafe-attacks]
      (let [result (classify request)]
        (is (= :rejected (:status result)))
        (is (= "C10-UNSAFE"
               (get-in result [:diagnostics 0 :rule])))))
    (is (= :rejected (:status unchecked-safe)))
    (is (= "C10-UNSAFE"
           (get-in unchecked-safe [:diagnostics 0 :rule])))
    (doseq [candidate substitutions]
      (let [verification
            (invoke-c10
             'sh11-verify-safety-result [base candidate])]
      (is (= :rejected (:status verification)))
      (is (= "C10-NO-OUTCOME"
             (get-in verification [:diagnostics 0 :rule])))
      (is (= :safety-result-substitution
             (get-in verification [:diagnostics 0 :reason])))))
    (let [verification
          (invoke-c10
           'sh11-verify-safety-result
           [base oversized-candidate])]
      (is (= :rejected (:status verification)))
      (is (= "C10-NO-OUTCOME"
             (get-in verification [:diagnostics 0 :rule])))
      (is (= :candidate-structural-bound
             (get-in verification [:diagnostics 0 :reason]))))))
