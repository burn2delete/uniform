(ns gravity.self-hosting.sh11-overflow-cast-safety-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh11-division-bounds-safety-test]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh11_overflow_cast_safety_test.clj")]
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "repository root not found"
                        {:id "SH11-OVERFLOW-CAST-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path [relative]
  (str (.resolve @root relative)))

(defn- division-var [name]
  (var-get
   (ns-resolve
    'gravity.self-hosting.sh11-division-bounds-safety-test name)))

(defn- adapter-var [name]
  (var-get
   (ns-resolve
    'gravity.self-hosting.sh11-c9-safety-adapter-test name)))

(defn- invoke-c10 [function arguments]
  ((division-var 'invoke-c10) function arguments))

(defn- digest [ordinal]
  ((division-var 'digest) ordinal))

(defn- compile-fixture [relative]
  ((division-var 'compile-fixture) relative))

(defn- invoke-fixture [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh11-overflow-cast-fixture-test
    :compiler-artifact-plan? true}
   plan function arguments))

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-11/overflow-cast")

(defn- fixture-path [family basename extension]
  (str fixture-root "/" family "/" basename extension))

(def ^:private accepted-gravity-path
  (fixture-path "accepted" "overflow-cast-safety" ".gravity"))

(def ^:private accepted-qst-path
  (fixture-path "accepted" "overflow-cast-safety" ".qst"))

(def ^:private rejected-gravity-path
  (fixture-path "rejected" "invalid-overflow-cast-safety" ".gravity"))

(def ^:private rejected-qst-path
  (fixture-path "rejected" "invalid-overflow-cast-safety" ".qst"))

(def ^:private accepted-gravity-plan
  (delay (compile-fixture accepted-gravity-path)))

(def ^:private accepted-qst-plan
  (delay (compile-fixture accepted-qst-path)))

(def ^:private rejected-gravity-plan
  (delay (compile-fixture rejected-gravity-path)))

(def ^:private rejected-qst-plan
  (delay (compile-fixture rejected-qst-path)))

(defn- prepared-two-integer-c9 []
  ((division-var 'prepared-two-integer-c9)))

(defn- operand-binding [role request result identity]
  ((division-var 'operand-binding) role request result identity))

(defn- operand-reference [binding]
  ((division-var 'operand-reference) binding))

(defn- operation-inputs [prepared operator]
  (let [bound (:bound prepared)
        requests (get-in bound [:ownership-core :ownership-requests])
        results (get-in bound [:ownership-core :ownership-results])
        identities (:fact-identities bound)
        roles (if (= operator :checked-narrowing)
                [:value]
                [:left :right])
        bindings
        (mapv operand-binding roles requests results identities)
        first-binding (first bindings)]
    {:source
     {:core-node-id
      (digest (if (= operator :add)
                801
                (if (= operator :multiply) 802 803)))
      :source-span (:source-span first-binding)
      :origin-chain (:origin-chain first-binding)
      :profile (:profile first-binding)
      :target (:target first-binding)
      :type-fact-id (:type-fact-id first-binding)
      :effect-fact-id (:effect-fact-id first-binding)
      :capability-proof-id (:capability-proof-id first-binding)
      :ownership-fact-id (:ownership-fact-id first-binding)}
     :bindings bindings
     :operands (mapv operand-reference bindings)}))

(defn- fixture-operation [plan function inputs]
  (invoke-fixture
   @plan function [(:source inputs) (:operands inputs)]))

(defn- template [prepared operation]
  (invoke-c10
   'sh11-authenticated-overflow-cast-check-request
   [(:bound prepared) (:verification prepared) operation]))

(defn- resolved-check [request ordinal]
  {:request request :digest (digest ordinal)})

(defn- classify [prepared operation resolution]
  (invoke-c10
   'sh11-classify-authenticated-overflow-cast
   [(:bound prepared) (:verification prepared) operation resolution]))

(defn- verify-result [prepared operation resolution candidate]
  (invoke-c10
   'sh11-verify-authenticated-overflow-cast-result
   [(:bound prepared) (:verification prepared)
    operation resolution candidate]))

(defn- diagnostic [result]
  (first (:diagnostics result)))

(deftest sh11-overflow-cast-api-fixtures-and-policy-are-closed
  (let [policy
        (invoke-c10 'sh11-authenticated-overflow-cast-policy [])
        functions
        (:functions @(adapter-var 'c10-plan))]
    (is (= #{:numeric-overflow :numeric-cast}
           (:operation-kinds policy)))
    (is (= #{:add :multiply} (:overflow-operators policy)))
    (is (= #{:checked-narrowing} (:cast-conversions policy)))
    (is (= #{:proven-safe :runtime-checked :rejected :unsafe-island}
           (:outcomes policy)))
    (is (= #{:runtime-checked :rejected}
           (:implemented-outcomes policy)))
    (is (= :SAFE9-OVERFLOW
           (get-in policy [:specialized-rules :numeric-overflow])))
    (is (= :SAFE9-NARROW
           (get-in policy [:specialized-rules :numeric-cast])))
    (is (some #{:sh11-completion} (:nonclaims policy)))
    (is (some #{:floating-or-elementary-math-safety}
              (:nonclaims policy)))
    (is (some #{:concurrency-and-race-safety} (:nonclaims policy)))
    (is (some #{:ffi-safety} (:nonclaims policy)))
    (is (some #{:taint-safety} (:nonclaims policy)))
    (doseq [[function arity]
            [['sh11-authenticated-overflow-cast-policy 0]
             ['sh11-authenticated-overflow-cast-check-request 3]
             ['sh11-classify-authenticated-overflow-cast 4]
             ['sh11-verify-authenticated-overflow-cast-result 5]]]
      (is (= arity (get-in functions [function :arity])) function))
    (is (java.util.Arrays/equals
         (java.nio.file.Files/readAllBytes
          (java.nio.file.Path/of (path accepted-gravity-path)
                                 (make-array String 0)))
         (java.nio.file.Files/readAllBytes
          (java.nio.file.Path/of (path accepted-qst-path)
                                 (make-array String 0)))))
    (is (java.util.Arrays/equals
         (java.nio.file.Files/readAllBytes
          (java.nio.file.Path/of (path rejected-gravity-path)
                                 (make-array String 0)))
         (java.nio.file.Files/readAllBytes
          (java.nio.file.Path/of (path rejected-qst-path)
                                 (make-array String 0)))))
    (doseq [plan [accepted-gravity-plan accepted-qst-plan
                  rejected-gravity-plan rejected-qst-plan]]
      (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))))

(deftest sh11-checked-add-multiply-and-cast-retain-exact-residual-checks
  (let [prepared (prepared-two-integer-c9)]
    (doseq [[operator function condition expression rule ordinal]
            [[:add 'sh11-authenticated-checked-add-operation
              :overflow :checked-add-result-within-width
              :SAFE9-OVERFLOW 811]
             [:multiply 'sh11-authenticated-checked-multiply-operation
              :overflow :checked-multiply-result-within-width
              :SAFE9-OVERFLOW 812]
             [:checked-narrowing 'sh11-authenticated-checked-cast-operation
              :representable-cast :value-representable-in-target-width
              :SAFE9-NARROW 813]]]
      (let [inputs (operation-inputs prepared operator)
            operation
            (fixture-operation accepted-gravity-plan function inputs)
            qst-operation
            (fixture-operation accepted-qst-plan function inputs)
            request-template (template prepared operation)
            resolution
            (resolved-check (:check-request request-template) ordinal)
            result (classify prepared operation resolution)
            outcome (first (:outcomes result))
            runtime-check (first (:runtime-checks result))]
        (testing (name operator)
          (is (= operation qst-operation))
          (is (= :accepted (:status request-template))
              (pr-str request-template))
          (is (= :accepted (:status result)) (pr-str result))
          (is (= :runtime-checked (:outcome result)))
          (is (= [:runtime-checked]
                 (mapv :outcome (:outcomes result))))
          (is (= (:runtime-checks result) (:residual-checks result)))
          (is (= [runtime-check] (:residual-checks result)))
          (is (= condition (:condition runtime-check)))
          (is (= expression
                 (get-in runtime-check
                         [:guard-proof :predicate :expression])))
          (is (= (:bindings inputs)
                 (get-in runtime-check
                         [:guard-proof :predicate :operands])))
          (is (= (:numeric-contract operation)
                 (get-in runtime-check
                         [:guard-proof :predicate :numeric-contract])))
          (is (true?
               (get-in runtime-check
                       [:guard-proof :guards-exact-operation])))
          (is (= :error/numeric (:failure-behavior runtime-check)))
          (is (= #{:error/raise} (:effects-introduced runtime-check)))
          (is (= rule (:specialized-safe-rule outcome)))
          (is (= (:bindings inputs)
                 (get-in outcome [:source :operand-bindings])))
          (is (= (:source inputs)
                 (select-keys
                  (:preserves result)
                  [:core-node-id :source-span :origin-chain :profile
                   :target :type-fact-id :effect-fact-id
                   :capability-proof-id :ownership-fact-id])))
          (is (= (:bindings inputs)
                 (get-in result [:preserves :operand-bindings])))
          (is (= (get-in prepared [:bound :provenance])
                 (get-in result [:provenance :upstream])))
          (is (= (select-keys
                  (:source inputs)
                  [:core-node-id :source-span :origin-chain])
                 (get-in result [:provenance :operation-source])))
          (is (= [] (:proofs result)))
          (is (= [] (:unsafe-islands result)))
          (is (= [] (:diagnostics result)))
          (is (= :passed
                 (:status
                  (verify-result
                   prepared operation resolution result)))))))))

(deftest sh11-overflow-cast-rejections-use-stable-c10-diagnostics
  (let [prepared (prepared-two-integer-c9)
        add-inputs (operation-inputs prepared :add)
        multiply-inputs (operation-inputs prepared :multiply)
        cast-inputs (operation-inputs prepared :checked-narrowing)
        accepted-add
        (fixture-operation
         accepted-gravity-plan
         'sh11-authenticated-checked-add-operation add-inputs)
        accepted-template (template prepared accepted-add)
        unproved
        (fixture-operation
         rejected-gravity-plan 'sh11-unproved-add-operation add-inputs)
        incompatible
        (fixture-operation
         rejected-gravity-plan
         'sh11-incompatible-cast-operation cast-inputs)
        unsafe
        (fixture-operation
         rejected-gravity-plan
         'sh11-unsafe-multiply-operation multiply-inputs)
        cases
        [[(template prepared unproved)
          "C10-PROOF"
          :numeric-proof-not-authenticated-by-ownership-facts]
         [(template prepared incompatible)
          "C10-NUMERIC"
          :incompatible-overflow-or-cast-contract]
         [(template prepared unsafe)
          "C10-UNSAFE"
          :authenticated-unsafe-overflow-cast-not-supported]
         [(classify prepared accepted-add nil)
          "C10-CHECK"
          :missing-authenticated-residual-check]
         [(classify
           prepared accepted-add
           {:request (assoc (:check-request accepted-template)
                            :condition :bounds)
            :digest (digest 821)})
          "C10-CHECK"
          :substituted-authenticated-residual-check]
         [(template
           prepared
           (assoc-in accepted-add [:operands 0 :ownership-fact-id]
                     (digest 822)))
          "C10-PROOF"
          :operation-operands-not-bound-to-c9-facts]]]
    (doseq [[result id reason] cases]
      (is (= :rejected (:status result)) (pr-str result))
      (is (= id (:diagnostic-id (diagnostic result))))
      (is (= reason (:reason (diagnostic result))))
      (is (map? (:source-span (diagnostic result))))
      (is (vector? (:generated-origin-chain (diagnostic result))))
      (is (= :meta (:profile (diagnostic result))))
      (is (= :jvm (:target (diagnostic result))))
      (is (keyword? (:missing-fact (diagnostic result))))
      (is (keyword? (:remediation (diagnostic result)))))))

(deftest sh11-overflow-cast-verifier-rejects-erasure-fifth-outcomes-and-substitution
  (let [prepared (prepared-two-integer-c9)
        inputs (operation-inputs prepared :multiply)
        operation
        (fixture-operation
         accepted-gravity-plan
         'sh11-authenticated-checked-multiply-operation inputs)
        request-template (template prepared operation)
        resolution
        (resolved-check (:check-request request-template) 831)
        result (classify prepared operation resolution)
        erased
        (-> result
            (assoc :runtime-checks [])
            (assoc :residual-checks [])
            (assoc-in [:outcomes 0 :runtime-check] nil))
        fifth
        (-> result
            (assoc :outcome :deferred-to-backend)
            (assoc-in [:outcomes 0 :outcome] :deferred-to-backend))
        duplicated
        (assoc result :outcomes
               (conj (:outcomes result) (first (:outcomes result))))
        substituted
        (assoc-in result [:preserves :ownership-fact-id] (digest 832))
        malformed (dissoc result :residual-checks)
        cases
        [[erased "C10-OPTIMIZATION"
          :authenticated-residual-check-substitution]
         [fifth "C10-NO-OUTCOME" :illegal-outcome]
         [duplicated "C10-NO-OUTCOME" :outcome-count-mismatch]
         [substituted "C10-OPTIMIZATION"
          :authenticated-residual-check-substitution]
         [malformed "C10-NO-OUTCOME" :candidate-structural-bound]]]
    (doseq [[candidate id reason] cases]
      (let [verification
            (verify-result prepared operation resolution candidate)
            result-diagnostic (diagnostic verification)]
        (is (= :rejected (:status verification)) (pr-str verification))
        (is (= id (:diagnostic-id result-diagnostic)))
        (is (= reason (:reason result-diagnostic)))))))
