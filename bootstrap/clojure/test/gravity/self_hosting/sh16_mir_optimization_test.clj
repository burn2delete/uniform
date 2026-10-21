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

(deftest sh16-unused-result-proofs-do-not-authorize-return-removal
  ;; C11 RETURN is a control transfer. This normalized no-use proof has no
  ;; CFG/unreachability premise, regardless of its supplied verified marker.
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [base (request plan 'sh16-no-change-request)
          return (last (:operations base))
          dead-return (assoc return :dead true
                             :dead-proof {:proof-id :proof/return-unused
                                          :operation-id (:op-id return)
                                          :no-uses true :effect-free true :verified true})
          input (assoc-in base [:operations 1] dead-return)
          rejected (fn [subject rule]
                     (let [result (optimize subject)]
                       (is (= :rejected (:status result)))
                       (is (= rule (get-in result [:diagnostics 0 :rule])))
                       (is (nil? (:optimized-operations result)))
                       (is (= :passed (:status (invoke-engine 'sh16-verify [subject result]))))))
          result (optimize input)
          baseline (optimize base)]
      (is (false? (invoke-engine 'sh16-result-id-used-from? [(:result return) (:operations input) 0])))
      (is (false? (invoke-engine 'sh16-dead-proof-valid? [dead-return (:operations input)])))
      (rejected input "C13-PROOF")
      (is (= :proof (get-in result [:diagnostics 0 :reason])))
      (is (= (:source-span return) (get-in result [:diagnostics 0 :source-span])))
      (is (= (:origin-chain return) (get-in result [:diagnostics 0 :origin-chain])))
      (is (= :accepted (:status baseline)))
      (is (= (:operations base) (:optimized-operations baseline)))
      (is (= return (last (:optimized-operations baseline))))
      (is (= :passed (:status (invoke-engine 'sh16-verify [base baseline]))))
      (is (= :rejected (:status (invoke-engine 'sh16-verify
                                             [base (update baseline :optimized-operations pop)]))))
      (is (= :rejected (:status (invoke-engine 'sh16-verify [input baseline]))))
      (doseq [[subject rule]
              [[(update-in input [:operations 1] dissoc :dead-proof) "C13-PROOF"]
               [(assoc-in input [:operations 1 :dead-proof :operation-id] :op/other) "C13-PROOF"]
               [(assoc-in input [:operations 1 :dead-proof :verified] false) "C13-PROOF"]
               [(assoc-in input [:operations 1 :effects] #{:io/write}) "C13-EFFECT"]
               [(assoc-in input [:operations 1 :safety-outcome] :rejected) "C13-SAFETY"]
               [(assoc input :operations
                       [(assoc (get-in (request plan 'sh16-mixed-optimization-request) [:operations 3])
                               :dead true
                               :dead-proof {:proof-id :proof/check-unused :operation-id :op/bounds-check
                                            :no-uses true :effect-free true :verified true})]) "C13-SAFETY"]
               [(assoc input :source-span nil) "C13-PRESERVE"]
               [(update input :operations conj return) "C13-VERIFY"]]]
        (rejected subject rule))
      ;; The existing no-use/effect-free proof remains sufficient for pure
      ;; value producers; RETURN is not a new spelling of those operations.
      (doseq [operation [(first (:operations base))
                         (first (:operations (request plan 'sh16-mixed-optimization-request)))
                         (get-in (request plan 'sh16-mixed-optimization-request) [:operations 2])]]
        (let [dead (assoc operation :op-id :op/dead-value :result :value/dead-value :dead true
                          :dead-proof {:proof-id :proof/dead-value :operation-id :op/dead-value
                                       :no-uses true :effect-free true :verified true})
              subject (update base :operations #(into [dead] %))
              optimized (optimize subject)]
          (is (= :accepted (:status optimized)))
          (is (= (:operations base) (:optimized-operations optimized)))
          (is (= [:op/dead-value] (get-in optimized [:decision-log 2 :changed-operations])))
          (is (= :passed (:status (invoke-engine 'sh16-verify [subject optimized])))))))))

(deftest sh16-records-only-used-dce-and-runtime-proof-occurrences
  ;; Normalized proof IDs describe supplied records, not provider authority.
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [base (request plan 'sh16-no-change-request)
          mixed (request plan 'sh16-mixed-optimization-request)
          elision (request plan 'sh16-proof-elision-request)
          dead (fn [id proof-id]
                 (-> (get-in mixed [:operations 2])
                     (assoc :op-id id :result id)
                     (assoc-in [:dead-proof :operation-id] id)
                     (assoc-in [:dead-proof :proof-id] proof-id)))
          check (fn [id check-id proof-id]
                  (-> (get-in elision [:operations 0])
                      (assoc :op-id id :result id :runtime-check-id check-id)
                      (assoc-in [:proof :operation-id] id)
                      (assoc-in [:proof :check-id] check-id)
                      (assoc-in [:proof :proof-id] proof-id)))
          rejected (fn [input rule]
                     (let [result (optimize input)]
                       (is (= :rejected (:status result)))
                       (is (= rule (get-in result [:diagnostics 0 :rule])))
                       (is (nil? (:optimized-operations result)))
                       (is (= :passed (:status (invoke-engine 'sh16-verify [input result]))))))]
      (doseq [proof-ids [[:proof/dead-b :proof/dead-a] [:proof/dead-same :proof/dead-same]]]
        (let [operations [(dead :op/dead-b (first proof-ids)) (dead :op/dead-a (second proof-ids))]
              input (assoc base :operations (into operations (:operations base)))
              result (optimize input)]
          (is (nil? (invoke-engine 'sh16-request-error [input])))
          (is (= :accepted (:status result)))
          (is (= proof-ids (get-in result [:decision-log 2 :proofs-used])))
          (is (= proof-ids (get-in result [:translation-validation :proofs])))
          (is (= [:op/dead-b :op/dead-a] (get-in result [:decision-log 2 :changed-operations])))
          (is (= [[] [] []] (mapv :proofs-used ((juxt first second last) (:decision-log result)))))
          (is (= [[] [] [] []] (mapv :proofs-invalidated (:invalidation-ledger result))))
          (is (= [] (get-in result [:post-pass-verifier :invalidated-proofs])))
          (is (= proof-ids (mapv #(get-in % [:dead-proof :proof-id])
                                (take 2 (get-in result [:identity-input :operations])))))
          (is (= :passed (:status (invoke-engine 'sh16-verify [input result]))))
          (doseq [mutant [(assoc-in result [:decision-log 2 :proofs-used] [])
                          (assoc-in result [:translation-validation :proofs] [])
                          (update-in result [:translation-validation :proofs] conj :proof/substituted)]]
            (is (= :rejected (:status (invoke-engine 'sh16-verify [input mutant])))))))
      (doseq [proof-ids [[:proof/runtime-b :proof/runtime-a] [:proof/runtime-same :proof/runtime-same]]]
        (let [checks [(check :op/check-b :check/b (first proof-ids))
                      (check :op/check-a :check/a (second proof-ids))]
              input (assoc elision :operations (conj checks (last (:operations elision))))
              result (optimize input)
              dead-operation (dead :op/dead :proof/dead)
              combined (update input :operations #(into [dead-operation] %))
              combined-result (optimize combined)]
          (is (nil? (invoke-engine 'sh16-request-error [input])))
          (is (= :accepted (:status result)))
          (is (= proof-ids (get-in result [:decision-log 3 :proofs-used])))
          (is (= proof-ids (get-in result [:translation-validation :proofs])))
          (is (= [] (get-in result [:decision-log 2 :proofs-used])))
          (is (= [proof-ids proof-ids proof-ids []]
                 (mapv :proofs-invalidated (:invalidation-ledger result))))
          (is (= (vec (distinct proof-ids)) (get-in result [:post-pass-verifier :invalidated-proofs])))
          (is (= :passed (:status (invoke-engine 'sh16-verify [input result]))))
          ;; DCE invalidates analysis needed for later runtime-check elision;
          ;; retained runtime proofs must not be reported as used or as dead IDs.
          (is (= :accepted (:status combined-result)))
          (is (= [:proof/dead] (get-in combined-result [:decision-log 2 :proofs-used])))
          (is (= [] (get-in combined-result [:decision-log 3 :proofs-used])))
          (is (= [:proof/dead] (get-in combined-result [:translation-validation :proofs])))
          (is (= [proof-ids proof-ids proof-ids []]
                 (mapv :proofs-invalidated (:invalidation-ledger combined-result))))
          (is (= (vec (distinct proof-ids)) (get-in combined-result [:post-pass-verifier :invalidated-proofs])))
          (is (= [:check/b :check/a]
                 (get-in combined-result [:residual-check-report :retained-runtime-checks])))
          (is (= :passed (:status (invoke-engine 'sh16-verify [combined combined-result]))))))
      (let [result (optimize base) operation (dead :op/dead :proof/dead)
            input (update base :operations #(into [operation] %))]
        (is (= [[] [] [] []] (mapv :proofs-used (:decision-log result))))
        (is (= [] (get-in result [:translation-validation :proofs])))
        (doseq [[mutant rule]
                [[(update-in input [:operations 0] dissoc :dead-proof) "C13-PROOF"]
                 [(assoc-in input [:operations 0 :dead-proof :operation-id] :op/other) "C13-PROOF"]
                 [(assoc-in input [:operations 0 :dead-proof :verified] false) "C13-PROOF"]
                 [(assoc-in input [:operations 0 :effects] #{:io/write}) "C13-EFFECT"]
                 [(assoc-in input [:operations 2 :operands] [:op/dead]) "C13-PROOF"]]]
          (rejected mutant rule))))))

(deftest sh16-defers-proven-unused-adds-until-dead-code-elimination
  ;; Original compiler execution over supplied normalized proof DATA, not
  ;; proof-provider evidence or fresh authenticated C11 input.
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [base (request plan 'sh16-no-change-request)
          add (-> (first (:operations (request plan 'sh16-mixed-optimization-request)))
                  (assoc :op-id :op/dead-add :result :value/dead-add :dead true
                         :dead-proof {:proof-id :proof/dead-add :operation-id :op/dead-add
                                      :no-uses true :effect-free true :verified true}))
          subject (assoc base :operations (into [add] (:operations base)))
          rejected (fn [input rule]
                     (let [result (optimize input)]
                       (is (= :rejected (:status result)))
                       (is (= rule (get-in result [:diagnostics 0 :rule])))
                       (is (nil? (:optimized-operations result)))
                       (is (= :passed (:status (invoke-engine 'sh16-verify [input result]))))))]
      (doseq [operands [[2 3] [9223372036854775806 1] [-9223372036854775808 0]]]
        (let [input (assoc-in subject [:operations 0 :operands] operands)
              operation (first (:operations input))
              initial {:status :accepted :operations (:operations input) :decision-log []
                       :invalidation-ledger [] :pass-verifier-reports [] :mutating-passes []
                       :invalidated-analysis [] :invalidated-facts [] :invalidated-proofs []}
              folded (invoke-engine 'sh16-run-pass [input initial :constant-fold])
              simplified (invoke-engine 'sh16-run-pass [input folded :branch-simplify])
              result (optimize input)]
          (is (nil? (invoke-engine 'sh16-request-error [input])))
          (is (true? (invoke-engine 'sh16-foldable-add? [operation])))
          (is (= (:operations input) (:operations folded)))
          (is (= (:operations input) (:operations simplified)))
          (is (= :accepted (:status folded)))
          (is (= :accepted (:status simplified)))
          (is (= :accepted (:status result)))
          (is (= (:operations base) (:optimized-operations result)))
          (is (= [false false true false] (mapv :changed? (:decision-log result))))
          (is (= [[] [] [:op/dead-add] []] (mapv :changed-operations (:decision-log result))))
          (is (= [:dead-code-eliminate] (get-in result [:post-pass-verifier :mutating-passes])))
          (is (every? #(= :passed (:status %)) (get-in result [:post-pass-verifier :pass-reports])))
          (is (= [:data-flow-cache :control-flow-graph]
                 (get-in result [:invalidation-ledger 2 :analysis-invalidated])))
          (is (= [:use-def-facts :control-flow-facts :safety-facts]
                 (get-in result [:invalidation-ledger 2 :facts-invalidated])))
          (is (every? empty? (map :analysis-invalidated (take 2 (:invalidation-ledger result)))))
          (is (= (:dead-proof operation) (get-in result [:identity-input :operations 0 :dead-proof])))
          (is (= (select-keys (:source-span operation) [:source-id :start :end])
                 (get-in result [:identity-input :operations 0 :source-span])))
          (is (= (:origin-chain operation)
                 (mapv #(select-keys % [:kind :source-id])
                       (get-in result [:identity-input :operations 0 :origin-chain]))))
          (is (= :passed (:status (invoke-engine 'sh16-verify [input result]))))
          (doseq [mutant [(update result :optimized-operations #(into [operation] %))
                          (assoc-in result [:decision-log 0 :changed?] true)
                          (assoc-in result [:identity-input :operations 0 :dead-proof :proof-id] :proof/substituted)]]
            (is (= :rejected (:status (invoke-engine 'sh16-verify [input mutant])))))
          (is (false? (invoke-engine 'sh16-post-pass-operation-valid? [operation])))
          (is (false? (invoke-engine 'sh16-pass-output-operation-valid? [:dead-code-eliminate operation])))))
      (doseq [[input rule]
              [[(update-in subject [:operations 0] dissoc :dead-proof) "C13-PROOF"]
               [(assoc-in subject [:operations 0 :dead-proof :operation-id] :op/other) "C13-PROOF"]
               [(assoc-in subject [:operations 2 :operands] [:value/dead-add]) "C13-PROOF"]
               [(assoc-in subject [:operations 0 :effects] #{:io/write}) "C13-EFFECT"]
               [(assoc-in subject [:operations 0 :safety-outcome] :rejected) "C13-SAFETY"]
               [(assoc-in subject [:operations 0 :operands] [9223372036854775807 1]) "C13-SAFETY"]
               [(assoc-in subject [:operations 0 :type] :bool) "C13-SAFETY"]
               [(assoc-in subject [:operations 0 :numeric-mode] nil) "C13-SAFETY"]
               [(assoc-in subject [:operations 0 :dead-proof :verified] false) "C13-PROOF"]
               [(assoc subject :mir-verified false) "C13-CONTRACT"]
               [(assoc subject :source-span nil) "C13-PRESERVE"]
               [(update subject :operations conj add) "C13-VERIFY"]]]
        (rejected input rule))
      (let [live (assoc base :operations [(dissoc add :dead :dead-proof)])
            result (optimize live)]
        (is (= :accepted (:status result)))
        (is (= :const (get-in result [:optimized-operations 0 :opcode])))
        (is (= [5] (get-in result [:optimized-operations 0 :operands])))
        (is (= [:op/dead-add] (get-in result [:decision-log 0 :changed-operations])))
        (is (= :passed (:status (invoke-engine 'sh16-verify [live result]))))))))

(deftest sh16-simplifies-only-established-boolean-conditions
  ;; Supplied normalized MIR is not fresh authenticated C11 input. Nil is not
  ;; a Bool condition and must not become an unconditional else transfer.
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [base (request plan 'sh16-mixed-optimization-request)
          baseline (optimize base)
          branch (get-in base [:operations 1])
          check-rejected
          (fn [subject rule]
            (let [result (optimize subject)]
              (is (= :rejected (:status result)))
              (is (= rule (get-in result [:diagnostics 0 :rule])))
              (is (nil? (:optimized-operations result)))
              (is (= :passed (:status (invoke-engine 'sh16-verify [subject result]))))))]
      (doseq [[condition destination] [[true :block/then] [false :block/else]]]
        (let [subject (assoc-in base [:operations 1 :condition] condition)
              result (optimize subject)
              output (get-in result [:optimized-operations 1])]
          (is (= :accepted (:status result)))
          (is (= :branch (:opcode output)))
          (is (= [destination] (:operands output)))
          (is (= (:source-span branch) (:source-span output)))
          (is (= (:origin-chain branch) (:origin-chain output)))
          (is (= (:effects branch) (:effects output)))
          (is (= [:op/branch] (get-in result [:decision-log 1 :changed-operations])))
          (is (= (get-in baseline [:invalidation-ledger 1])
                 (get-in result [:invalidation-ledger 1])))
          (is (= :passed (:status (invoke-engine 'sh16-verify [subject result]))))))
      (doseq [condition [nil 0 "false" :false [] {}]]
        (let [subject (assoc-in base [:operations 1 :condition] condition)
              result (optimize subject)]
          (check-rejected subject "C13-CONTRACT")
          (is (= :contract (get-in result [:diagnostics 0 :reason])))
          (is (= (:source-span branch) (get-in result [:diagnostics 0 :source-span])))
          (is (= (:origin-chain branch) (get-in result [:diagnostics 0 :origin-chain])))
          (is (false? (invoke-engine 'sh16-simplifiable-branch?
                                    [(assoc branch :condition condition)])))
          (is (= (assoc branch :condition condition)
                 (invoke-engine 'sh16-transform-operation
                                [(assoc branch :condition condition)])))))
      (let [subject (assoc-in base [:operations 1 :condition] nil)]
        (check-rejected (assoc subject :mir-verified false) "C13-CONTRACT")
        (check-rejected (assoc subject :source-span nil) "C13-PRESERVE")
        (check-rejected (assoc subject :nondeterministic-choice true) "C13-NONDETERMINISM")
        (check-rejected (update subject :operations conj branch) "C13-VERIFY")
        (check-rejected (-> subject
                            (assoc-in [:operations 0 :dead] true)
                            (assoc-in [:operations 0 :effects] #{:io/write})) "C13-EFFECT")
        (is (= :rejected (:status (invoke-engine 'sh16-verify [subject baseline]))))))))

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

(deftest sh16-constant-folds-only-an-established-i64-result-domain
  ;; Supplied normalized requests are not authenticated C11 input. Opaque type
  ;; IDs are not declared globally invalid: they lack this selected fold's
  ;; explicit :i64 result premise and must not be guessed from their spelling.
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [base (request plan 'sh16-mixed-optimization-request)
          baseline (optimize base)
          operation (get-in base [:operations 0])
          check-rejected
          (fn [subject rule]
            (let [result (optimize subject)]
              (is (= :rejected (:status result)))
              (is (= rule (get-in result [:diagnostics 0 :rule])))
              (is (nil? (:optimized-operations result)))
              (is (= :passed (:status (invoke-engine 'sh16-verify [subject result]))))))]
      (doseq [[left right expected]
              [[2 3 5] [9223372036854775806 1 9223372036854775807]
               [-9223372036854775807 -1 -9223372036854775808]
               [-9223372036854775808 0 -9223372036854775808]]]
        (let [subject (assoc-in base [:operations 0 :operands] [left right])
              result (optimize subject) folded (first (:optimized-operations result))]
          (is (= :accepted (:status result)))
          (is (= :const (:opcode folded)))
          (is (= :i64 (:type folded)))
          (is (= [expected] (:operands folded)))
          (is (= (:source-span operation) (:source-span folded)))
          (is (= (:origin-chain operation) (:origin-chain folded)))
          (is (= (:safety-outcome operation) (:safety-outcome folded)))
          (is (= [:op/add] (get-in result [:decision-log 0 :changed-operations])))
          (is (= (first (:invalidation-ledger baseline))
                 (first (:invalidation-ledger result))))
          (is (= :passed (:status (invoke-engine 'sh16-verify [subject result]))))))
      (doseq [type [:bool :keyword :foreign/type :gravity.type/i64 'custom-integer]]
        (let [subject (assoc-in base [:operations 0 :type] type)
              result (optimize subject) op (get-in subject [:operations 0])]
          (is (= :rejected (:status result)))
          (is (= "C13-SAFETY" (get-in result [:diagnostics 0 :rule])))
          (is (= :safety (get-in result [:diagnostics 0 :reason])))
          (is (nil? (:optimized-operations result)))
          (is (= (:source-span op) (get-in result [:diagnostics 0 :source-span])))
          (is (= (:origin-chain op) (get-in result [:diagnostics 0 :origin-chain])))
          (is (= :passed (:status (invoke-engine 'sh16-verify [subject result]))))
          (is (false? (invoke-engine 'sh16-foldable-add? [op])))
          (is (= op (invoke-engine 'sh16-transform-operation [op])))))
      (let [subject (assoc-in base [:operations 0 :type] :bool)]
        (check-rejected (assoc subject :mir-verified false) "C13-CONTRACT")
        (check-rejected (assoc subject :source-span nil) "C13-PRESERVE")
        (check-rejected (assoc subject :nondeterministic-choice true) "C13-NONDETERMINISM")
        (check-rejected (assoc-in subject [:operations 0 :op-id] nil) "C13-VERIFY")
        (check-rejected (update subject :operations conj (first (:operations subject))) "C13-VERIFY")
        (check-rejected (-> subject
                            (assoc-in [:operations 0 :dead] true)
                            (assoc-in [:operations 0 :effects] #{:io/write})) "C13-EFFECT")
        (is (= :rejected (:status (invoke-engine 'sh16-verify [subject baseline])))))
      ;; These unchanged paths do not assert general typed-input validation.
      ;; A nonconstant add is not folded; an audited unsafe-island bypasses the
      ;; normal foldability ingress gate but still must not create a bad const.
      (doseq [op [(assoc operation :type :foreign/type :constant-operands false)
                  (assoc operation :type :bool :safety-outcome :unsafe-island
                         :unsafe-audit-id :audit/supplied)]]
        (let [subject (assoc-in base [:operations 0] op) result (optimize subject)]
          (is (= :accepted (:status result)))
          (is (= op (first (:optimized-operations result))))
          (is (false? (get-in result [:decision-log 0 :changed?])))
          (is (= :passed (:status (invoke-engine 'sh16-verify [subject result])))))))))

(deftest sh16-preserves-unsafe-audit-identities-through-transformations
  ;; Supplied normalized audit IDs are not audit-provider or C11 evidence.
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [base (request plan 'sh16-mixed-optimization-request)
          baseline (optimize base)
          audit-ids [:audit/constant-fold :audit/branch-simplify]
          audited (fn [subject positions]
                    (reduce (fn [value index]
                              (update-in value [:operations index]
                                         assoc :safety-outcome :unsafe-island
                                         :unsafe-audit-id (audit-ids index)))
                            subject positions))
          rejected (fn [subject rule]
                     (let [result (optimize subject)]
                       (is (= :rejected (:status result)))
                       (is (= rule (get-in result [:diagnostics 0 :rule])))
                       (is (= :passed (:status (invoke-engine 'sh16-verify [subject result]))))))]
      (doseq [[positions condition] [[#{0} true] [#{1} true] [#{0 1} false]]]
        (let [subject (audited (assoc-in base [:operations 1 :condition] condition) positions)
              expected-baseline (optimize (assoc-in base [:operations 1 :condition] condition))
              result (optimize subject)
              operations (:optimized-operations result)]
          (is (nil? (invoke-engine 'sh16-request-error [subject])))
          (is (= :accepted (:status result)))
          (is (= [:const :branch] (mapv :opcode (take 2 operations))))
          (is (= [5] (:operands (first operations))))
          (is (= [(if condition :block/then :block/else)] (:operands (second operations))))
          (doseq [index [0 1]]
            (let [input (get-in subject [:operations index]) output (get operations index)]
              (is (= (select-keys input [:op-id :result :type :effects :ordering
                                        :source-span :origin-chain :safety-outcome])
                     (select-keys output [:op-id :result :type :effects :ordering
                                         :source-span :origin-chain :safety-outcome])))
              (if (contains? positions index)
                (is (= (audit-ids index) (:unsafe-audit-id output)))
                (is (= (get-in expected-baseline [:optimized-operations index]) output)))))
          (is (= (:decision-log expected-baseline) (:decision-log result)))
          (is (= (:invalidation-ledger expected-baseline) (:invalidation-ledger result)))
          (is (= :passed (get-in result [:post-pass-verifier :status])))
          (is (= :passed (:status (invoke-engine 'sh16-verify [subject result]))))
          (doseq [index positions]
            (let [mutant (assoc-in result [:optimized-operations index :unsafe-audit-id] :audit/substituted)
                  verification (invoke-engine 'sh16-verify [subject mutant])]
              (is (= :rejected (:status verification)))
              (is (= :optimization-result-substitution (get-in verification [:diagnostics 0 :reason])))))))
      (let [subject (audited base #{0 1})]
        (doseq [mutant [(update-in subject [:operations 0] dissoc :unsafe-audit-id)
                        (assoc-in subject [:operations 0 :unsafe-audit-id] nil)
                        (assoc-in subject [:operations 1 :unsafe-audit-id] "not-a-canonical-id")]]
          (rejected mutant "C13-CONTRACT"))
        (rejected (update subject :operations conj (first (:operations subject))) "C13-VERIFY")
        (rejected (assoc subject :source-span nil) "C13-PRESERVE")
        (rejected (-> subject (assoc-in [:operations 0 :dead] true)
                      (assoc-in [:operations 0 :effects] #{:io/write})) "C13-EFFECT"))
      (let [operation (-> (get-in base [:operations 0])
                          (assoc :type :bool :safety-outcome :unsafe-island
                                 :unsafe-audit-id :audit/unsupported-fold-domain))
            subject (assoc-in base [:operations 0] operation) result (optimize subject)]
        (is (= :accepted (:status result)))
        (is (= operation (first (:optimized-operations result))))
        (is (false? (get-in result [:decision-log 0 :changed?])))
        (is (= :passed (:status (invoke-engine 'sh16-verify [subject result])))))
      ;; Third shared helper callsite: a runtime check cannot lawfully carry
      ;; :unsafe-island through the existing public ingress gate.
      (let [operation (get-in base [:operations 3])
            unsafe-check (assoc operation :safety-outcome :unsafe-island
                                :unsafe-audit-id :audit/check)
            subject (assoc base :operations [unsafe-check])
            retained (invoke-engine 'sh16-retained-runtime-check [operation])]
        (is (= :safety (invoke-engine 'sh16-request-error [subject])))
        (rejected subject "C13-SAFETY")
        (is (= operation retained))
        (is (not (contains? retained :unsafe-audit-id))))
      (is (= baseline (optimize base))))))

(deftest sh16-rejections-retain-the-first-failing-operation-source
  ;; Supplied normalized locations are diagnostic DATA, not source admission.
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [base (update (request plan 'sh16-mixed-optimization-request) :operations
                       (fn [operations]
                         (mapv (fn [index operation]
                                 (assoc operation :origin-chain
                                        [{:kind :generated :source-id :source/operation
                                          :generator-id (keyword "generator" (str index))}]))
                               (range) operations)))
          bad-add (assoc-in base [:operations 0 :type] :bool)
          bad-branch (assoc-in base [:operations 1 :condition] nil)
          bad-check (-> base (assoc-in [:operations 3 :elide-requested] true)
                        (assoc-in [:operations 3 :proof] {}))
          both (assoc-in bad-add [:operations 3] (get-in bad-check [:operations 3]))
          rejected
          (fn [subject reason operation]
            (let [result (optimize subject) diagnostic (first (:diagnostics result))
                  legacy (invoke-engine 'sh16-diagnostic [subject reason])
                  expected (if operation
                             (assoc legacy :source-span (:source-span operation)
                                    :origin-chain (:origin-chain operation)) legacy)
                  mutant (assoc-in result [:diagnostics 0 :source-span :start] 999)
                  altered (invoke-engine 'sh16-verify [subject mutant])]
              (is (= :rejected (:status result)))
              (is (= reason (:reason diagnostic)))
              (is (= reason (invoke-engine 'sh16-request-error [subject])))
              (is (= expected diagnostic))
              (is (= (:source-span expected) (:source-span diagnostic)))
              (is (= (:origin-chain expected) (:origin-chain diagnostic)))
              (is (nil? (:optimized-operations result)))
              (is (= :passed (:status (invoke-engine 'sh16-verify [subject result]))))
              (is (= :rejected (:status altered)))
              (is (= :optimization-result-substitution (get-in altered [:diagnostics 0 :reason])))))]
      (doseq [[subject reason index]
              [[bad-add :safety 0] [bad-branch :contract 1] [bad-check :proof 3]
               [(assoc-in base [:operations 1 :effects] #{:io/write}) :effect 1]
               [both :safety 0]
               [(assoc-in bad-branch [:operations 3] (get-in bad-check [:operations 3])) :contract 1]]]
        (rejected subject reason (get-in subject [:operations index])))
      ;; Invalid first-operation metadata keeps the COMPLETE legacy location;
      ;; never skip the first fault in search of a later usable source.
      (doseq [subject [(assoc-in both [:operations 0 :source-span] nil)
                       (update-in both [:operations 0] dissoc :source-span)
                       (assoc-in both [:operations 0 :source-span :end] -1)
                       (assoc-in both [:operations 0 :origin-chain] [])
                       (assoc-in both [:operations 0 :origin-chain] nil)]]
        (rejected subject :contract nil))
      (doseq [[subject reason]
              [[(assoc both :mir-verified false) :contract]
               [(assoc both :source-span nil) :preserve]
               [(assoc both :origin-chain []) :preserve]
               [(assoc both :nondeterministic-choice true) :nondeterminism]
               [(update both :operations conj (first (:operations both))) :verify]]]
        (rejected subject reason nil))
      (let [subject (assoc both :operations (apply list (:operations both)))
            preflight (invoke-engine 'sh16-structural-preflight [subject])
            result (optimize subject)]
        (is (= :rejected (:status result)))
        (is (= :request-structural-bound (get-in result [:diagnostics 0 :reason])))
        (is (= result (invoke-engine 'sh16-structural-rejection
                                    [subject preflight :request-structural-bound])))
        (is (= :request-structural-bound
               (get-in (invoke-engine 'sh16-verify [subject result]) [:diagnostics 0 :reason]))))
      (let [accepted (optimize base)]
        (is (= :accepted (:status accepted)))
        (is (= accepted (invoke-engine 'sh16-accepted-artifact [base])))
        (is (= :passed (:status (invoke-engine 'sh16-verify [base accepted]))))))))

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
