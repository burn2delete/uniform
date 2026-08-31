(ns gravity.self-hosting.sh11-division-bounds-safety-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [clojure.walk :as walk]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh11-c9-safety-adapter-test]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh11_division_bounds_safety_test.clj")]
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "repository root not found"
                        {:id "SH11-DIVISION-BOUNDS-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- test-var [namespace name]
  (var-get (ns-resolve namespace name)))

(defn- sh11-var [name]
  (test-var 'gravity.self-hosting.sh11-c9-safety-adapter-test name))

(defn- sh10-var [name]
  (test-var 'gravity.self-hosting.sh10-c8-ownership-adapter-test name))

(defn- sh09-var [name]
  (test-var 'gravity.self-hosting.sh09-c7-effect-adapter-test name))

(defn- invoke-c9 [function arguments]
  ((sh11-var 'invoke-c9) function arguments))

(defn- invoke-c10 [function arguments]
  ((sh11-var 'invoke-c10) function arguments))

(defn- digest [ordinal]
  ((sh11-var 'digest) ordinal))

(defn- resolve-requests [requests first-ordinal]
  ((sh11-var 'resolve-requests) requests first-ordinal))

(defn- compile-fixture [relative-path]
  ((sh11-var 'compile-plan) relative-path))

(defn- invoke-fixture [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh11-division-bounds-fixture-test
    :compiler-artifact-plan? true}
   plan function arguments))

(def ^:private accepted-gravity-plan
  (delay
    (compile-fixture
     "bootstrap/clojure/fixtures/self-hosting/sh-11/accepted/division-bounds-safety.gravity")))

(def ^:private accepted-qst-plan
  (delay
    (compile-fixture
     "bootstrap/clojure/fixtures/self-hosting/sh-11/accepted/division-bounds-safety.qst")))

(def ^:private rejected-gravity-plan
  (delay
    (compile-fixture
     "bootstrap/clojure/fixtures/self-hosting/sh-11/rejected/invalid-division-bounds-safety.gravity")))

(def ^:private rejected-qst-plan
  (delay
    (compile-fixture
     "bootstrap/clojure/fixtures/self-hosting/sh-11/rejected/invalid-division-bounds-safety.qst")))

(defn- prepared-two-integer-c9
  ([] (prepared-two-integer-c9 "/checkout-a/primitive.gravity"))
  ([actual-path]
   (let [typed
         ((sh09-var 'typed-result) actual-path)
         two-integer-typed
         (walk/postwalk-replace
          {:gravity.type/bool :gravity.type/integer}
          typed)
         typed-verification
         ((sh09-var 'upstream-verification) two-integer-typed)
         prepared
         ((sh10-var 'prepared-bound-products)
          two-integer-typed typed-verification)
         owned
         (invoke-c9
          'sh10-build-authenticated-ownership-core
          [(:bound prepared) (:binding-verification prepared)])
         fact-template
         (invoke-c9
          'sh10-authenticated-ownership-identity-requests
          [(:bound prepared) (:binding-verification prepared) owned])
         fact-resolutions
         (resolve-requests (:fact-requests fact-template) 501)
         core-template
         (invoke-c9
          'sh10-authenticated-ownership-core-identity-request
          [(:bound prepared) (:binding-verification prepared)
           owned fact-resolutions])
         resolved
         {:fact-resolutions fact-resolutions
          :core-resolution
          {:request (:core-request core-template) :digest (digest 601)}}
         bound
         (invoke-c9
          'sh10-bind-authenticated-ownership-identities
          [(:bound prepared) (:binding-verification prepared)
           owned resolved])
         verification
         (invoke-c9
          'sh10-verify-authenticated-ownership-identities
          [(:bound prepared) (:binding-verification prepared)
           owned resolved bound])]
     {:typed-verification typed-verification
      :prepared prepared
      :owned owned
      :bound bound
      :verification verification})))

(defn- operand-binding [role request result identity]
  (let [fact (first (:ownership-facts result))
        state (:state-before fact)]
    {:role role
     :core-node-id (:value-id request)
     :type :gravity.type/integer
     :type-fact-id (:type-fact-id request)
     :effect-fact-id (:effect-fact-id request)
     :capability-proof-id (:capability-proof-id request)
     :ownership-fact-id (:ownership-fact-id identity)
     :ownership-fact-id-request (:fact-id-request fact)
     :ownership-kind (:ownership-kind request)
     :initialization (:initialization state)
     :availability (:availability state)
     :event-id (:event-id fact)
     :event (:operation fact)
     :source-span (:source-span fact)
     :origin-chain (:origin-chain fact)
     :profile (:profile fact)
     :target (:target fact)}))

(defn- operand-reference [binding]
  (select-keys
   binding
   [:role :core-node-id :type-fact-id :effect-fact-id
    :capability-proof-id :ownership-fact-id
    :ownership-fact-id-request :source-span :origin-chain]))

(defn- operation-inputs [prepared kind]
  (let [bound (:bound prepared)
        requests (get-in bound [:ownership-core :ownership-requests])
        results (get-in bound [:ownership-core :ownership-results])
        identities (:fact-identities bound)
        roles (if (= kind :division)
                [:dividend :divisor]
                [:index :length])
        bindings
        (mapv operand-binding roles requests results identities)
        first-binding (first bindings)]
    {:source
     {:core-node-id (digest (if (= kind :division) 611 612))
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

(defn- check-template [prepared operation]
  (invoke-c10
   'sh11-authenticated-division-bounds-check-request
   [(:bound prepared) (:verification prepared) operation]))

(defn- classify [prepared operation resolution]
  (invoke-c10
   'sh11-classify-authenticated-division-bounds
   [(:bound prepared) (:verification prepared) operation resolution]))

(defn- resolved-check [template ordinal]
  {:request (:check-request template) :digest (digest ordinal)})

(defn- diagnostic [result]
  (first (:diagnostics result)))

(deftest sh11-division-bounds-source-api-and-real-c9-input-are-exact
  (let [prepared (prepared-two-integer-c9)
        policy (invoke-c10 'sh11-authenticated-division-bounds-policy [])
        functions (:functions @(sh11-var 'c10-plan))]
    (is (= :passed (:status (:typed-verification prepared))))
    (is (= :accepted (get-in prepared [:prepared :bound :status])))
    (is (= :passed
           (get-in prepared [:prepared :binding-verification :status])))
    (is (= :accepted (get-in prepared [:owned :status])))
    (is (= 2 (count (get-in prepared
                            [:bound :ownership-core
                             :ownership-requests]))))
    (is (= :passed (:status (:verification prepared))))
    (is (= #{:runtime-checked :rejected} (:implemented-outcomes policy)))
    (is (= #{:proven-safe :runtime-checked :rejected :unsafe-island}
           (:outcomes policy)))
    (is (= {:arity 4
            :params ['bound 'verification 'descriptor 'check-resolution]}
           (select-keys
            (get functions 'sh11-classify-authenticated-division-bounds)
            [:arity :params])))
    (is (some #{:sh11-completion} (:nonclaims policy)))))

(deftest sh11-division-and-bounds-retain-exact-runtime-checks
  (let [prepared (prepared-two-integer-c9)]
    (doseq [[kind plan function ordinal condition failure rule]
            [[:division accepted-gravity-plan
              'sh11-authenticated-division-operation 701
              :valid-divisor-and-quotient :error/numeric :SAFE9-DIV-ZERO]
             [:index accepted-gravity-plan
              'sh11-authenticated-bounds-operation 702 :bounds
              :panic/bounds :SAFE2-BOUNDS]]]
      (let [inputs (operation-inputs prepared kind)
            operation (fixture-operation plan function inputs)
            qst-operation
            (fixture-operation
             accepted-qst-plan function inputs)
            template (check-template prepared operation)
            resolution (resolved-check template ordinal)
            result (classify prepared operation resolution)
            outcome (first (:outcomes result))
            runtime-check (first (:runtime-checks result))]
        (testing (name kind)
          (is (= operation qst-operation))
          (is (= :accepted (:status template)) (pr-str template))
          (is (= :accepted (:status result)) (pr-str result))
          (is (= :runtime-checked (:outcome result)))
          (is (= 1 (count (:outcomes result))))
          (is (= [:runtime-checked] (mapv :outcome (:outcomes result))))
          (is (= (:bindings inputs)
                 (get-in template [:check-request :operand-bindings])))
          (is (= (:operands inputs)
                 (get-in template [:check-request :operation :operands])))
          (is (= condition (:condition runtime-check)))
          (is (= failure (:failure-behavior runtime-check)))
          (is (= rule (:specialized-safe-rule outcome)))
          (is (= (:bindings inputs)
                 (get-in runtime-check [:guard-proof :predicate :operands])))
          (is (true?
               (get-in runtime-check
                       [:guard-proof :guards-exact-operation])))
          (is (= (:runtime-checks result) (:residual-checks result)))
          (is (= runtime-check (:runtime-check outcome)))
          (is (= (:source inputs)
                 (select-keys
                  (:preserves result)
                  [:core-node-id :source-span :origin-chain :profile
                   :target :type-fact-id :effect-fact-id
                   :capability-proof-id :ownership-fact-id])))
          (is (= [] (:proofs result)))
          (is (= [] (:unsafe-islands result)))
          (is (= [] (:diagnostics result)))
          (is (= :passed
                 (:status
                  (invoke-c10
                   'sh11-verify-authenticated-division-bounds-result
                   [(:bound prepared) (:verification prepared)
                    operation resolution result])))))))))

(deftest sh11-division-bounds-rejections-have-stable-c10-diagnostics
  (let [prepared (prepared-two-integer-c9)
        division-inputs (operation-inputs prepared :division)
        bounds-inputs (operation-inputs prepared :index)
        cases
        [[rejected-gravity-plan 'sh11-unproved-division-operation
          division-inputs "C10-PROOF"
          :numeric-proof-not-authenticated-by-ownership-facts]
         [rejected-gravity-plan 'sh11-unproved-bounds-operation
          bounds-inputs "C10-PROOF"
          :numeric-proof-not-authenticated-by-ownership-facts]
         [rejected-gravity-plan 'sh11-unsafe-division-operation
          division-inputs "C10-UNSAFE"
          :authenticated-unsafe-division-bounds-not-supported]
         [rejected-gravity-plan 'sh11-invalid-bounds-check-operation
          bounds-inputs "C10-CHECK"
          :invalid-authenticated-runtime-check-policy]]]
    (doseq [[plan function inputs rule reason] cases]
      (let [operation (fixture-operation plan function inputs)
            qst-operation
            (fixture-operation rejected-qst-plan function inputs)
            result (classify prepared operation nil)
            diag (diagnostic result)]
        (is (= operation qst-operation))
        (is (= :rejected (:status result)) (pr-str result))
        (is (= :rejected (:outcome result)))
        (is (= [:rejected] (mapv :outcome (:outcomes result))))
        (is (= rule (:diagnostic-id diag)))
        (is (= reason (:reason diag)))
        (is (= (:source-span operation) (:source-span diag)))
        (is (= (:origin-chain operation)
               (:generated-origin-chain diag)))
        (is (= (:operands operation)
               (mapv operand-reference
                     (get-in result [:preserves :operand-bindings]))))))
    (let [operation
          (fixture-operation
           accepted-gravity-plan
           'sh11-authenticated-division-operation division-inputs)
          template (check-template prepared operation)
          missing (classify prepared operation nil)
          substituted
          (classify
           prepared operation
           {:request (assoc (:check-request template)
                            :condition :substituted)
            :digest (digest 799)})
          swapped-operation
          (assoc operation :operands (vec (reverse (:operands operation))))
          swapped (classify prepared swapped-operation nil)
          changed-source
          (classify
           prepared
           (assoc operation :source-span
                  (assoc (:source-span operation) :start-byte 999))
           nil)
          tampered-bound
          (assoc-in (:bound prepared)
                    [:ownership-core :ownership-results 0
                     :ownership-facts 0 :owner-id]
                    :forged-owner)
          tampered-verification
          (assoc (:verification prepared)
                 :expected tampered-bound
                 :candidate tampered-bound)
          tampered-linkage
          (invoke-c10
           'sh11-classify-authenticated-division-bounds
           [tampered-bound tampered-verification operation nil])
          malformed-events-bound
          (assoc-in (:bound prepared)
                    [:ownership-core :ownership-requests 0 :events]
                    :forged-events)
          malformed-events-verification
          (assoc (:verification prepared)
                 :expected malformed-events-bound
                 :candidate malformed-events-bound)
          malformed-events
          (invoke-c10
           'sh11-classify-authenticated-division-bounds
           [malformed-events-bound malformed-events-verification
            operation nil])
          tampered-state-bound
          (assoc-in (:bound prepared)
                    [:ownership-core :ownership-results 0 :state]
                    {:forged true})
          tampered-state-verification
          (assoc (:verification prepared)
                 :expected tampered-state-bound
                 :candidate tampered-state-bound)
          tampered-state
          (invoke-c10
           'sh11-classify-authenticated-division-bounds
           [tampered-state-bound tampered-state-verification
            operation nil])]
      (is (= "C10-CHECK" (:diagnostic-id (diagnostic missing))))
      (is (= :missing-authenticated-residual-check
             (:reason (diagnostic missing))))
      (is (= "C10-CHECK" (:diagnostic-id (diagnostic substituted))))
      (is (= :substituted-authenticated-residual-check
             (:reason (diagnostic substituted))))
      (is (= "C10-PROOF" (:diagnostic-id (diagnostic swapped))))
      (is (= :operation-operands-not-bound-to-c9-facts
             (:reason (diagnostic swapped))))
      (is (= "C10-PROOF"
             (:diagnostic-id (diagnostic changed-source))))
      (is (= :operation-operands-not-bound-to-c9-facts
             (:reason (diagnostic changed-source))))
      (doseq [forged
              [tampered-linkage malformed-events tampered-state]]
        (is (= "C10-PROOF"
               (:diagnostic-id (diagnostic forged))))
        (is (= :untrusted-or-incomplete-c9-operand-facts
               (:reason (diagnostic forged))))))))

(deftest sh11-verifier-rejects-fifth-outcomes-and-residual-check-erasure
  (let [prepared (prepared-two-integer-c9)
        inputs (operation-inputs prepared :division)
        operation
        (fixture-operation
         accepted-gravity-plan
         'sh11-authenticated-division-operation inputs)
        template (check-template prepared operation)
        resolution (resolved-check template 801)
        result (classify prepared operation resolution)
        verify
        (fn [candidate]
          (invoke-c10
           'sh11-verify-authenticated-division-bounds-result
           [(:bound prepared) (:verification prepared)
            operation resolution candidate]))
        erased (assoc result :residual-checks [])
        mutated-guard
        (assoc-in result
                  [:runtime-checks 0 :guard-proof :predicate :expression]
                  :substituted-predicate)
        duplicated
        (assoc result :outcomes
               [(first (:outcomes result)) (first (:outcomes result))])
        fifth
        (assoc result
               :outcome :implicit-fifth
               :outcomes
               [(assoc (first (:outcomes result))
                       :outcome :implicit-fifth)])
        malformed-outcomes (assoc result :outcomes :malformed)
        missing-outcomes (dissoc result :outcomes)
        malformed-runtime-checks
        (assoc result :runtime-checks :malformed)
        malformed-residual-checks
        (assoc result :residual-checks :malformed)]
    (is (= "C10-OPTIMIZATION"
           (get-in (verify erased) [:diagnostics 0 :diagnostic-id])))
    (is (= :authenticated-residual-check-substitution
           (get-in (verify erased) [:diagnostics 0 :reason])))
    (is (= "C10-OPTIMIZATION"
           (get-in (verify mutated-guard)
                   [:diagnostics 0 :diagnostic-id])))
    (is (= :authenticated-residual-check-substitution
           (get-in (verify mutated-guard) [:diagnostics 0 :reason])))
    (is (= "C10-NO-OUTCOME"
           (get-in (verify duplicated) [:diagnostics 0 :diagnostic-id])))
    (is (= :outcome-count-mismatch
           (get-in (verify duplicated) [:diagnostics 0 :reason])))
    (is (= "C10-NO-OUTCOME"
           (get-in (verify fifth) [:diagnostics 0 :diagnostic-id])))
    (is (= :illegal-outcome
           (get-in (verify fifth) [:diagnostics 0 :reason])))
    (doseq [malformed
            [malformed-outcomes missing-outcomes
             malformed-runtime-checks malformed-residual-checks]]
      (is (= "C10-NO-OUTCOME"
             (get-in (verify malformed)
                     [:diagnostics 0 :diagnostic-id])))
      (is (= :candidate-structural-bound
             (get-in (verify malformed) [:diagnostics 0 :reason]))))))
