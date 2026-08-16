(ns gravity.self-hosting.sh16-mir-optimization-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource "gravity/self_hosting/sh16_mir_optimization_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-16 test source is not on the classpath"
                {:id "SH16-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "SH16-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-16")

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

(def ^:private engine-plan
  (delay
   (compile-plan
    "bootstrap/gravity/src/gravity/compiler/c13_mir_optimization_passes.gravity")))

(def ^:private accepted-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path "accepted" "mir-optimizations" ".gravity"))))

(def ^:private accepted-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path "accepted" "mir-optimizations" ".qst"))))

(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-mir-optimizations" ".gravity"))))

(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-mir-optimizations" ".qst"))))

(defn- invoke
  [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh16-mir-optimization-leaf
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- invoke-engine
  [function arguments]
  (invoke engine-plan function arguments))

(defn- request
  [plan function]
  (invoke plan function []))

(defn- optimize
  [request]
  (invoke-engine 'sh16-optimize [request]))

(defn- nested-vector
  [depth value]
  (loop [remaining depth
         result value]
    (if (zero? remaining)
      result
      (recur (dec remaining) [result]))))

(def ^:private rejected-cases
  {'sh16-unverified-request ["C13-CONTRACT" :contract]
   'sh16-duplicate-operation-request ["C13-VERIFY" :verify]
   'sh16-missing-effect-order-request ["C13-EFFECT" :effect]
   'sh16-effectful-dead-request ["C13-EFFECT" :effect]
   'sh16-missing-dead-proof-request ["C13-PROOF" :proof]
   'sh16-used-dead-operation-request ["C13-PROOF" :proof]
   'sh16-missing-proof-request
   ["C13-CHECK-ELISION" :check-elision]
   'sh16-nondominating-proof-request ["C13-PROOF" :proof]
   'sh16-invalidated-proof-request ["C13-PROOF" :proof]
   'sh16-policy-check-request
   ["C13-CHECK-ELISION" :check-elision]
   'sh16-stale-safety-request ["C13-SAFETY" :safety]
   'sh16-missing-origin-request ["C13-PRESERVE" :preserve]
   'sh16-nondeterministic-request
   ["C13-NONDETERMINISM" :nondeterminism]
   'sh16-unsupported-opcode-request ["C13-CONTRACT" :contract]
   'sh16-implicit-numeric-mode-request ["C13-SAFETY" :safety]})

(deftest sh16-engine-and-co-canonical-fixtures-compile-as-gravity
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (doseq [function
          '[sh16-optimization-policy sh16-optimize sh16-verify]]
    (is (map? (get-in @engine-plan [:functions function])) function))
  (let [policy (invoke-engine 'sh16-optimization-policy [])]
    (is (= :gravity/sh16-optimization-policy (:artifact policy)))
    (is (= 1 (:version policy)))
    (is (= 128 (:maximum-operations policy)))
    (is (= [:constant-fold
            :branch-simplify
            :dead-code-eliminate
            :check-retention-or-elision]
           (:pass-order policy)))
    (is (contains? (:diagnostics policy) "C13-CHECK-ELISION"))
    (is (contains? (:diagnostics policy) "C13-EFFECT"))
    (is (= #{:bounds} (:elidable-check-classes policy)))
    (is (some #{:authenticated-sh15-input} (:pending policy)))
    (is (some #{:complete-c11-mir-adapter} (:pending policy)))
    (is (some #{:remaining-check-class-proof-replay}
              (:pending policy))))
  (doseq [[family basename]
          [["accepted" "mir-optimizations"]
           ["rejected" "invalid-mir-optimizations"]]]
    (is (= (slurp
            (path (fixture-relative-path family basename ".gravity")))
           (slurp
            (path (fixture-relative-path family basename ".qst")))))))

(deftest sh16-runs-deterministic-bounded-pass-pipeline
  (let [gravity-request
        (request accepted-gravity-plan 'sh16-mixed-optimization-request)
        qst-request
        (request accepted-qst-plan 'sh16-mixed-optimization-request)
        gravity-result (optimize gravity-request)
        qst-result (optimize qst-request)
        operations (:optimized-operations gravity-result)
        decisions (:decision-log gravity-result)
        ledgers (:invalidation-ledger gravity-result)]
    (is (= gravity-request qst-request))
    (is (= gravity-result qst-result))
    (is (= :accepted (:status gravity-result)))
    (is (empty? (:diagnostics gravity-result)))
    (is (= [:op/add :op/branch :op/bounds-check :op/return]
           (mapv :op-id operations)))
    (is (= :const (:opcode (first operations))))
    (is (= [5] (:operands (first operations))))
    (is (= :branch (:opcode (second operations))))
    (is (= [:block/then] (:operands (second operations))))
    (is (= :runtime-check (:opcode (nth operations 2))))
    (is (= 4 (count decisions)))
    (is (= [true true true false] (mapv :changed? decisions)))
    (is (= [:constant-fold
            :branch-simplify
            :dead-code-eliminate
            :check-retention-or-elision]
           (mapv :pass-id decisions)))
    (is (= 4 (count ledgers)))
    (is (= [:control-flow-graph :dominator-tree]
           (:analysis-invalidated (second ledgers))))
    (is (= [:check/bounds-retained]
           (get-in gravity-result
                   [:residual-check-report :retained-runtime-checks])))
    (is (empty?
         (get-in gravity-result
                 [:residual-check-report :elided-runtime-checks])))
    (is (= :accepted
           (get-in gravity-result [:translation-validation :result])))
    (is (= :gravity/bounded-local-translation-check
           (get-in gravity-result [:translation-validation :artifact])))
    (is (false?
         (get-in gravity-result
                 [:translation-validation :whole-function?])))
    (is (= :passed
           (get-in gravity-result [:post-pass-verifier :status])))
    (is (false?
         (get-in gravity-result
                 [:post-pass-verifier :whole-c11-module?])))
    (is (= [:constant-fold
            :branch-simplify
            :dead-code-eliminate]
           (get-in gravity-result
                   [:post-pass-verifier :mutating-passes])))
    (is (= :forbidden
           (get-in gravity-result
                   [:pass-pipeline :target-instruction-selection])))
    (is (= :passed
           (:status
            (invoke-engine
             'sh16-verify [gravity-request gravity-result]))))))

(deftest sh16-elides-check-only-with-matching-dominating-proof
  (let [request
        (request accepted-gravity-plan 'sh16-proof-elision-request)
        result (optimize request)
        record (first (:check-elision-records result))
        check-decision (last (:decision-log result))
        check-ledger (last (:invalidation-ledger result))]
    (is (= :accepted (:status result)))
    (is (= [:op/return] (mapv :op-id (:optimized-operations result))))
    (is (= 1 (count (:check-elision-records result))))
    (is (= :check/bounds-proven (:check-id record)))
    (is (= :bounds (:check-class record)))
    (is (= :op/proven-bounds (:operation-id record)))
    (is (= :proof/bounds-dominates (:proof-id record)))
    (is (= :certificate/bounds-dominates
           (:certificate-id record)))
    (is (true? (:proof-dominates-use record)))
    (is (= :verified (:certificate-status record)))
    (is (true? (:proof-condition-replayed? record)))
    (is (= :proven-safe (:resulting-safety-outcome record)))
    (is (empty? (:invalidated-by record)))
    (is (true? (:changed? check-decision)))
    (is (= [:proof/bounds-dominates]
           (:proofs-used check-decision)))
    (is (= [:runtime-check-table]
           (:facts-invalidated check-ledger)))
    (is (= [:runtime-check-table :residual-check-report]
           (:facts-regenerated check-ledger)))
    (is (empty?
         (get-in result
                 [:residual-check-report :retained-runtime-checks])))
    (is (= [:check/bounds-proven]
           (get-in result
                   [:residual-check-report :elided-runtime-checks])))
    (is (= [:proof/bounds-dominates]
           (get-in result [:translation-validation :proofs])))
    (is (= [:check-retention-or-elision]
           (get-in result [:post-pass-verifier :mutating-passes])))
    (is (= :passed
           (:status (invoke-engine 'sh16-verify [request result]))))))

(deftest sh16-emits-decision-records-for-no-op-passes
  (let [request
        (request accepted-gravity-plan 'sh16-no-change-request)
        result (optimize request)]
    (is (= :accepted (:status result)))
    (is (= (:operations request) (:optimized-operations result)))
    (is (= 4 (count (:decision-log result))))
    (is (every? false? (map :changed? (:decision-log result))))
    (is (every? empty?
                (map :changed-operations (:decision-log result))))
    (is (every? #(= :no-legal-candidate (:reason %))
                (:decision-log result)))
    (is (every? empty?
                (map :analysis-invalidated
                     (:invalidation-ledger result))))
    (is (empty?
         (get-in result [:post-pass-verifier :mutating-passes])))
    (is (= :passed
           (:status (invoke-engine 'sh16-verify [request result]))))))

(deftest sh16-keeps-checkout-paths-outside-optimization-identity
  (let [first-request
        (request accepted-gravity-plan 'sh16-mixed-optimization-request)
        second-request
        (request
         accepted-gravity-plan 'sh16-mixed-alternate-path-request)
        first-result (optimize first-request)
        second-result (optimize second-request)]
    (is (not= (get-in first-request [:source-span :actual-source-path])
              (get-in second-request [:source-span :actual-source-path])))
    (is (= (:identity-input first-result)
           (:identity-input second-result)))
    (is (= (mapv :op-id (:optimized-operations first-result))
           (mapv :op-id (:optimized-operations second-result))))
    (is (= (mapv :opcode (:optimized-operations first-result))
           (mapv :opcode (:optimized-operations second-result))))
    (is (not= (:optimized-operations first-result)
              (:optimized-operations second-result)))
    (is (not= (:provenance first-result)
              (:provenance second-result)))
    (is (not
         (str/includes?
          (pr-str (:identity-input first-result)) "/checkout-a/")))
    (is (not
         (str/includes?
          (pr-str (:identity-input second-result)) "/checkout-b/")))
    (is (= :passed
           (:status
            (invoke-engine
             'sh16-verify [first-request first-result]))))
    (is (= :passed
           (:status
            (invoke-engine
             'sh16-verify [second-request second-result]))))))

(deftest sh16-rejects-illegal-transformations-structurally
  (doseq [[function [rule reason]] rejected-cases]
    (testing (str function)
      (let [gravity-request (request rejected-gravity-plan function)
            qst-request (request rejected-qst-plan function)
            gravity-result (optimize gravity-request)
            qst-result (optimize qst-request)
            diagnostic (first (:diagnostics gravity-result))]
        (is (= gravity-request qst-request))
        (is (= gravity-result qst-result))
        (is (= :rejected (:status gravity-result)))
        (is (= 1 (count (:diagnostics gravity-result))))
        (is (= rule (:rule diagnostic) (:diagnostic-id diagnostic)))
        (is (= reason (:reason diagnostic)))
        (is (= :optimize-mir (:stage diagnostic)))
        (is (= (:request-id gravity-request)
               (:request-id diagnostic)))
        (is (= (:input-mir-id gravity-request)
               (:input-mir-id diagnostic)))
        (is (= (:profile gravity-request) (:profile diagnostic)))
        (is (= (:target gravity-request) (:target diagnostic)))
        (is (= (:source-span gravity-request)
               (:source-span diagnostic)))
        (is (= (:origin-chain gravity-request)
               (:origin-chain diagnostic)))
        (is (keyword? (:remediation diagnostic)))))))

(deftest sh16-proof-binding-and-operation-bounds-fail-closed
  (let [request
        (request accepted-gravity-plan 'sh16-proof-elision-request)
        proof-path [:operations 0 :proof]
        proof-mutations
        [(assoc-in request (conj proof-path :certificate-id) nil)
         (assoc-in request (conj proof-path :unexpected-field) :hidden)
         (assoc-in request (conj proof-path :claim) :overflow-impossible)
         (assoc-in request (conj proof-path :check-id) :check/other)
         (assoc-in request (conj proof-path :operation-id) :op/other)
         (assoc-in request (conj proof-path :certificate-status) :rejected)
         (assoc-in request (conj proof-path :result) :runtime-checked)
         (assoc-in request (conj proof-path :assumptions) #{})
         (assoc-in request (conj proof-path :profile) :hosted)
         (assoc-in request (conj proof-path :target) :llvm)
         (assoc-in request [:operations 0 :operands] [5 5])]
        operation (first (:operations request))
        excessive
        (assoc request :operations (vec (repeat 129 operation)))
        empty-request (assoc request :operations [])]
    (doseq [candidate proof-mutations]
      (let [result (optimize candidate)]
        (is (= :rejected (:status result)))
        (is (= "C13-PROOF"
               (get-in result [:diagnostics 0 :rule])))))
    (doseq [candidate [excessive empty-request]]
      (let [result (optimize candidate)]
        (is (= :rejected (:status result)))
        (is (= "C13-CONTRACT"
               (get-in result [:diagnostics 0 :rule])))))))

(deftest sh16-normalized-boundary-rejects-unbound-descriptor-data
  (let [request
        (request accepted-gravity-plan 'sh16-no-change-request)
        candidates
        [[(assoc request :unchecked-semantic-field :hidden)
          "C13-CONTRACT"]
         [(assoc-in request
                    [:operations 0 :unchecked-semantic-field]
                    :hidden)
          "C13-CONTRACT"]
         [(assoc-in request [:source-span :start] -1)
          "C13-PRESERVE"]
         [(assoc request :origin-chain [])
          "C13-PRESERVE"]
         [(assoc request :profile :hosted)
          "C13-CONTRACT"]
         [(assoc request :target :x86-64)
          "C13-CONTRACT"]
         [(assoc-in request [:operations 0 :type] nil)
          "C13-CONTRACT"]
         [(assoc-in request
                    [:operations 0 :operands]
                    (vec (repeat 17 1)))
          "C13-CONTRACT"]
         [(assoc-in request
                    [:operations 1 :runtime-check-id]
                    :check/smuggled)
          "C13-CONTRACT"]]]
    (doseq [[candidate rule] candidates]
      (let [result (optimize candidate)]
        (is (= :rejected (:status result)))
        (is (= rule (get-in result [:diagnostics 0 :rule])))))))

(deftest sh16-runtime-check-identities-are-unambiguous
  (let [request
        (request accepted-gravity-plan 'sh16-proof-elision-request)
        first-check (first (:operations request))
        duplicate-check
        (-> first-check
            (assoc :op-id :op/duplicate-check)
            (assoc-in [:proof :operation-id] :op/duplicate-check))
        candidate
        (assoc request :operations
               (vec (cons duplicate-check (:operations request))))
        result (optimize candidate)]
    (is (= :rejected (:status result)))
    (is (= "C13-VERIFY" (get-in result [:diagnostics 0 :rule])))
    (is (= :verify (get-in result [:diagnostics 0 :reason])))))

(deftest sh16-dead-code-analysis-tracks-distinct-result-identities
  (let [base
        (request rejected-gravity-plan 'sh16-used-dead-operation-request)
        candidate
        (-> base
            (assoc-in [:operations 0 :result] :value/live)
            (assoc-in [:operations 1 :operands] [:value/live]))
        result (optimize candidate)]
    (is (not= (get-in candidate [:operations 0 :op-id])
              (get-in candidate [:operations 0 :result])))
    (is (= :rejected (:status result)))
    (is (= "C13-PROOF" (get-in result [:diagnostics 0 :rule])))
    (is (= :proof (get-in result [:diagnostics 0 :reason])))))

(deftest sh16-verifier-recomputes-and-rejects-result-substitution
  (let [request
        (request accepted-gravity-plan 'sh16-mixed-optimization-request)
        result (optimize request)
        substituted
        (assoc-in result
                  [:optimized-operations 0 :operands]
                  [999])
        verification
        (invoke-engine 'sh16-verify [request substituted])]
    (is (= :accepted (:status result)))
    (is (= :rejected (:status verification)))
    (is (= "C13-VERIFY"
           (get-in verification [:diagnostics 0 :rule])))
    (is (= "C13-VERIFY"
           (get-in verification [:diagnostics 0 :diagnostic-id])))
    (is (= :optimization-result-substitution
           (get-in verification [:diagnostics 0 :reason])))))

(deftest sh16-runs-passes-sequentially-and-retains-stale-proof-checks
  (let [proof-request
        (request accepted-gravity-plan 'sh16-proof-elision-request)
        mixed-request
        (request accepted-gravity-plan 'sh16-mixed-optimization-request)
        add (first (:operations mixed-request))
        branch (second (:operations mixed-request))
        check (first (:operations proof-request))
        return (second (:operations proof-request))
        candidate
        (assoc proof-request :operations [branch check return])
        data-flow-candidate
        (assoc proof-request :operations [add check return])
        result (optimize candidate)
        data-flow-result (optimize data-flow-candidate)
        branch-ledger (second (:invalidation-ledger result))
        check-decision (last (:decision-log result))
        retained-check (second (:optimized-operations result))]
    (is (= :accepted (:status result)))
    (is (= [:op/branch :op/proven-bounds :op/return]
           (mapv :op-id (:optimized-operations result))))
    (is (= :branch (:opcode (first (:optimized-operations result)))))
    (is (= :runtime-check (:opcode retained-check)))
    (is (false? (:elide-requested retained-check)))
    (is (not (contains? retained-check :proof)))
    (is (false? (:changed? check-decision)))
    (is (= [:control-flow-graph :dominator-tree]
           (:analysis-invalidated branch-ledger)))
    (is (= [:proof/bounds-dominates]
           (:proofs-invalidated branch-ledger)))
    (is (= [:check/bounds-proven]
           (get-in result
                   [:residual-check-report :retained-runtime-checks])))
    (is (empty?
         (get-in result
                 [:residual-check-report :elided-runtime-checks])))
    (is (= :passed
           (:status (invoke-engine 'sh16-verify [candidate result]))))
    (is (= :accepted (:status data-flow-result)))
    (is (= [:data-flow-facts :range-facts :safety-facts]
           (get-in data-flow-result
                   [:invalidation-ledger 0 :facts-invalidated])))
    (is (= [:proof/bounds-dominates]
           (get-in data-flow-result
                   [:invalidation-ledger 0 :proofs-invalidated])))
    (is (= [:check/bounds-proven]
           (get-in data-flow-result
                   [:residual-check-report :retained-runtime-checks])))
    (is (empty?
         (get-in data-flow-result
                 [:residual-check-report :elided-runtime-checks])))))

(deftest sh16-mutating-and-final-verifiers-gate-acceptance
  (let [request
        (request accepted-gravity-plan 'sh16-mixed-optimization-request)
        pipeline
        (invoke-engine 'sh16-sequential-pipeline [request])
        bad-operation
        (assoc (first (:operations pipeline)) :opcode :x86-add)
        bad-final-state
        (assoc-in pipeline [:operations 0] bad-operation)
        bad-pass-state
        (assoc-in pipeline
                  [:pass-verifier-reports 0 :status]
                  :rejected)
        final-rejection
        (invoke-engine
         'sh16-finalize-pipeline [request bad-final-state])
        pass-rejection
        (invoke-engine
         'sh16-finalize-pipeline [request bad-pass-state])]
    (is (= :accepted (:status pipeline)))
    (is (every? #(= :passed (:status %))
                (:pass-verifier-reports pipeline)))
    (doseq [result [final-rejection pass-rejection]]
      (is (= :rejected (:status result)))
      (is (= "C13-VERIFY" (get-in result [:diagnostics 0 :rule])))
      (is (= :verify (get-in result [:diagnostics 0 :reason]))))))

(deftest sh16-folds-only-representable-explicit-i64-addition
  (let [base
        (request accepted-gravity-plan 'sh16-mixed-optimization-request)
        accepted
        (assoc-in base
                  [:operations 0 :operands]
                  [Long/MAX_VALUE 0])
        upper-overflow
        (assoc-in base
                  [:operations 0 :operands]
                  [Long/MAX_VALUE 1])
        lower-overflow
        (assoc-in base
                  [:operations 0 :operands]
                  [Long/MIN_VALUE -1])
        accepted-result (optimize accepted)
        upper-result (optimize upper-overflow)
        lower-result (optimize lower-overflow)
        folded (first (:optimized-operations accepted-result))]
    (is (= :accepted (:status accepted-result)))
    (is (= [Long/MAX_VALUE] (:operands folded)))
    (is (= :const (:opcode folded)))
    (is (= #{:op-id :opcode :operands :result :type :effects :ordering
             :source-span :origin-chain :safety-outcome}
           (set (keys folded))))
    (doseq [result [upper-result lower-result]]
      (is (= :rejected (:status result)))
      (is (= "C13-SAFETY" (get-in result [:diagnostics 0 :rule])))
      (is (= :safety (get-in result [:diagnostics 0 :reason]))))))

(deftest sh16-rejects-irrelevant-fields-and-noncanonical-identifiers
  (let [base
        (request accepted-gravity-plan 'sh16-no-change-request)
        proof-request
        (request accepted-gravity-plan 'sh16-proof-elision-request)
        overlong-id (keyword (apply str (repeat 300 "a")))
        candidates
        [(assoc-in base [:operations 0 :condition] true)
         (assoc-in base [:operations 0 :proof] {})
         (assoc-in base [:operations 0 :result]
                   "/checkout-private/value")
         (assoc-in base [:operations 0 :ordering]
                   {:path "/checkout-private/value"})
         (assoc-in base [:operations 0 :origin-chain 0 :macro-id]
                   :macro/unbound)
         (assoc base :request-id overlong-id)
         (assoc-in proof-request [:operations 0 :elide-requested] false)]]
    (doseq [candidate candidates]
      (let [result (optimize candidate)]
        (is (= :rejected (:status result)))
        (is (= "C13-CONTRACT"
               (get-in result [:diagnostics 0 :rule])))))
    (is (not
         (str/includes?
          (pr-str (:identity-input (optimize base)))
          "/checkout-a/")))))

(deftest sh16-structural-preflight-contains-deep-and-sequence-carriers
  (let [base
        (request accepted-gravity-plan 'sh16-no-change-request)
        valid-result (optimize base)
        deep-request
        (assoc-in base [:operations 0 :result]
                  (nested-vector 120 :op/deep))
        sequence-request
        (assoc base :operations (iterate vector nil))
        unbounded-integer-request
        (assoc-in base [:source-span :end]
                  (bigint "9223372036854775808"))
        deep-candidate
        (assoc valid-result :diagnostics
               (nested-vector 120 :deep-candidate))
        sequence-candidate (iterate vector nil)
        deep-request-result (optimize deep-request)
        sequence-request-result (optimize sequence-request)
        unbounded-integer-result (optimize unbounded-integer-request)
        deep-verification
        (invoke-engine 'sh16-verify [base deep-candidate])
        sequence-verification
        (invoke-engine 'sh16-verify [base sequence-candidate])]
    (doseq [result [deep-request-result
                    sequence-request-result
                    unbounded-integer-result]]
      (is (= :rejected (:status result)))
      (is (= :request-structural-bound
             (get-in result [:diagnostics 0 :reason]))))
    (doseq [verification [deep-verification sequence-verification]]
      (is (= :rejected (:status verification)))
      (is (= "C13-VERIFY"
             (get-in verification [:diagnostics 0 :rule])))
      (is (= :candidate-structural-bound
             (get-in verification [:diagnostics 0 :reason]))))))

(deftest sh16-transformed-operations-have-exact-clean-schemas
  (let [request
        (request accepted-gravity-plan 'sh16-mixed-optimization-request)
        result (optimize request)
        folded (first (:optimized-operations result))
        branch (second (:optimized-operations result))]
    (is (= :accepted (:status result)))
    (is (= #{:op-id :opcode :operands :result :type :effects :ordering
             :source-span :origin-chain :safety-outcome}
           (set (keys folded))))
    (is (= (set (keys folded)) (set (keys branch))))
    (is (not (contains? folded :numeric-mode)))
    (is (not (contains? folded :constant-operands)))
    (is (not (contains? branch :condition)))
    (is (not (contains? branch :then-block)))
    (is (not (contains? branch :else-block)))
    (is (= :passed
           (get-in result [:post-pass-verifier :status])))
    (is (= 4
           (count
            (get-in result
                    [:post-pass-verifier :pass-reports]))))))
