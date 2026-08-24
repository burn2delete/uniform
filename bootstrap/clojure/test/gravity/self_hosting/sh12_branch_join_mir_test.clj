(ns gravity.self-hosting.sh12-branch-join-mir-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh12_branch_join_mir_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-12 branch/join test is not on the classpath"
        {:id "SH12-BRANCH-JOIN-TEST-ROOT"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH12-BRANCH-JOIN-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(def ^:private c11-source
  "bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity")

(def ^:private accepted-fixtures
  {".gravity"
   "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/branch_join_mir.gravity"
   ".qst"
   "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/branch_join_mir.qst"})

(def ^:private rejected-fixtures
  {".gravity"
   "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/rejected/malformed_branch_join_mir.gravity"
   ".qst"
   "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/rejected/malformed_branch_join_mir.qst"})

(defn- compile-plan
  [relative-path]
  (let [source-path (str (.resolve @root relative-path))
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private c11-plan (delay (compile-plan c11-source)))

(def ^:private accepted-plans
  (into {}
        (map (fn [[extension path]]
               [extension (delay (compile-plan path))]))
        accepted-fixtures))

(def ^:private rejected-plans
  (into {}
        (map (fn [[extension path]]
               [extension (delay (compile-plan path))]))
        rejected-fixtures))

(defn- invoke
  [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh12-branch-join-mir-test
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- checked-core
  [extension]
  (invoke
   (get accepted-plans extension)
   'sh12-accepted-branch-join-checked-core
   [(str (.resolve @root (get accepted-fixtures extension)))]))

(defn- rejected-path
  [extension]
  (str (.resolve @root (get rejected-fixtures extension))))

(defn- build-mir
  [checked]
  (invoke c11-plan 'sh12-build-branch-join-mir [checked]))

(defn- verify-mir
  [checked mir]
  (invoke c11-plan 'sh12-verify-branch-join-mir [checked mir]))

(defn- diagnostic
  [report]
  (first (:diagnostics report)))

(def ^:private stable-diagnostic-fields
  #{:diagnostic-id :severity :stage :mir-module :function :block
    :operation-id :value-id :source-span :origin-chain :profile
    :target-request :missing-fact :provenance :remediation})

(deftest sh12-branch-join-api-is-gravity-owned-and-bounded
  (let [functions (:functions @c11-plan)
        policy (invoke c11-plan 'sh12-branch-join-mir-policy [])]
    (is (= 0 (get-in functions ['sh12-branch-join-mir-policy :arity])))
    (is (= 1 (get-in functions ['sh12-build-branch-join-mir :arity])))
    (is (= 2 (get-in functions ['sh12-verify-branch-join-mir :arity])))
    (is (= :single-conditional-branch-join (:scope policy)))
    (is (= :entry-then-else-join-return
           (:control-flow-shape policy)))
    (is (= 1 (:maximum-functions policy)))
    (is (= 1 (:maximum-conditionals policy)))
    (is (true? (:target-independent? policy)))
    (is (some #{:generic-c11-verifier} (:nonclaims policy)))
    (is (some #{:sh12-completion} (:pending policy)))))

(deftest sh12-branch-join-fixtures-are-co-canonical
  (doseq [fixtures [accepted-fixtures rejected-fixtures]]
    (is (= (slurp (str (.resolve @root (get fixtures ".gravity"))))
           (slurp (str (.resolve @root (get fixtures ".qst")))))))
  (is (= (checked-core ".gravity")
         (-> (checked-core ".qst")
             (assoc-in [:provenance :actual-source-path]
                       (get-in (checked-core ".gravity")
                               [:provenance :actual-source-path])))))
  (is (= (invoke (get rejected-plans ".gravity")
                 'sh12-branch-join-rejection-cases
                 [(rejected-path ".gravity")])
         (mapv #(assoc % :actual-source-path (rejected-path ".gravity"))
               (invoke (get rejected-plans ".qst")
                       'sh12-branch-join-rejection-cases
                       [(rejected-path ".qst")])))))

(deftest sh12-constructs-and-verifies-a-branch-join-diamond
  (doseq [extension [".gravity" ".qst"]]
    (testing extension
      (let [checked (checked-core extension)
            mir (build-mir checked)
            report (verify-mir checked mir)
            function (first (vals (:functions mir)))
            blocks (:blocks function)
            control-flow (:control-flow-graph mir)
            join (:join control-flow)
            incoming (:incoming join)]
        (is (= :gravity/mir-module (:artifact mir)))
        (is (= (str (.resolve @root (get accepted-fixtures extension)))
               (get-in checked [:provenance :actual-source-path])))
        (is (= (:artifact-id checked) (:source-core mir)))
        (is (true? (:target-independent? mir)))
        (is (= 4 (count blocks)))
        (is (= 4 (count (:edges control-flow))))
        (is (= 2 (count incoming)))
        (is (= (set (map :predecessor incoming))
               (set (:predecessors (get blocks (:block-id join))))))
        (is (= [:conditional-branch :branch :branch :return]
               (mapv (fn [block-id]
                       (get-in blocks [block-id :terminator :kind]))
                     [(:entry function)
                      (:predecessor (first incoming))
                      (:predecessor (second incoming))
                      (:block-id join)])))
        (doseq [node (:core-nodes checked)]
          (let [node-id (:node-id node)]
            (is (contains? (:type-table mir)
                           (get-in checked [:type-facts node-id :fact-id])))
            (is (contains? (:effect-table mir)
                           (get-in checked [:effect-facts node-id :fact-id])))
            (is (contains? (:ownership-table mir)
                           (get-in checked
                                   [:ownership-facts node-id :fact-id])))
            (is (contains? (:capability-table mir)
                           (str node-id ":capability-fact")))
            (is (contains? (:safety-table mir)
                           (str node-id ":safety-outcome")))
            (is (contains? (:profile-target-table mir)
                           (str node-id ":profile-target-fact")))
            (is (= (:source node) (get-in mir [:source-map node-id])))))
        (is (contains? (:capability-proof-table mir)
                       "sh12:branch-join:capability-proof"))
        (is (= "sh12:branch-join:provenance"
               (get-in mir [:provenance
                            :checked-core-provenance-binding-id])))
        (is (= :passed (:status report)))
        (is (empty? (:diagnostics report)))
        (is (every? (set (:preserves report))
                    [:type-facts :effect-facts :ownership-facts
                     :safety-facts :source-origin-map
                     :control-flow-edges :data-flow-edges
                     :dominance-facts]))))))

(deftest sh12-runtime-check-consumes-and-guards-the-branch-condition
  (doseq [extension [".gravity" ".qst"]]
    (testing extension
      (let [checked
            (invoke (get rejected-plans extension)
                    'sh12-runtime-checked-core
                    [(checked-core extension)])
            mir (build-mir checked)
            report (verify-mir checked mir)
            function-name (first (keys (:functions mir)))
            function (first (vals (:functions mir)))
            blocks (:blocks function)
            operations (mapcat :instructions (vals (:blocks function)))
            runtime-operation
            (first (filter #(= :runtime-check (:opcode %)) operations))
            if-operation
            (first (filter #(= :if (:source-operation %)) operations))
            condition-id "sh12:branch-join:condition"
            check-id "sh12:branch-join:runtime-check"
            check-operation-id (str check-id ":mir:runtime-check")
            token-id (str check-id ":mir:token")
            data-edges (get-in mir [:data-flow-graph :edges])
            terminator-uses
            (get-in mir [:data-flow-graph :terminator-uses])
            check-entry (get-in mir [:runtime-check-table check-id])
            entry-block-id (:entry function)
            entry-block (get blocks entry-block-id)
            entry-terminator (:terminator entry-block)
            entry-terminator-id (:terminator-id entry-terminator)
            entry-operation-ids (mapv :op-id (:instructions entry-block))
            join-block-id (get-in mir [:control-flow-graph :join :block-id])
            join-operation-ids
            (mapv :op-id
                  (get-in function [:blocks join-block-id :instructions]))
            effect-entry
            (get-in mir [:effect-table
                         (get-in runtime-operation
                                 [:facts :effect-fact-id])])
            late-check-mir
            (-> mir
                (update-in [:functions function-name :blocks
                            entry-block-id :instructions]
                           #(vec (remove
                                  (fn [operation]
                                    (= check-operation-id
                                       (:op-id operation)))
                                  %)))
                (update-in [:functions function-name :blocks
                            join-block-id :instructions]
                           #(vec (cons runtime-operation %))))
            late-check-report (verify-mir checked late-check-mir)]
        (is (= :passed (:status report)))
        (is (empty? (:diagnostics report)))
        (is (= check-operation-id (:op-id runtime-operation)))
        (is (= [condition-id] (:operands runtime-operation)))
        (is (= #{:error/raise} (:effects runtime-operation)))
        (is (= #{:error/raise} (:latent-effects function)))
        (is (= #{:error/raise} (:direct effect-entry)))
        (is (= #{:error/raise}
               (get-in mir [:data-flow-graph :values token-id :effects])))
        (is (= entry-terminator-id
               (get-in runtime-operation
                       [:facts :guarded-operation-id])))
        (is (= entry-terminator-id
               (:guarded-operation-id check-entry)))
        (is (= check-operation-id (:check-operation-id check-entry)))
        (is (= token-id (:token-value-id check-entry)))
        (is (= entry-block-id (:block-id runtime-operation)))
        (is (= check-operation-id (last entry-operation-ids)))
        (is (= -1 (.indexOf join-operation-ids check-operation-id)))
        (is (< -1 (.indexOf join-operation-ids (:op-id if-operation))))
        (is (= [token-id condition-id] (:operands entry-terminator)))
        (is (= check-id
               (get-in entry-terminator [:facts :runtime-check-id])))
        (is (= token-id
               (get-in entry-terminator [:facts :runtime-check-token-id])))
        (is (= [condition-id
                "sh12:branch-join:then"
                "sh12:branch-join:else"]
               (:operands if-operation)))
        (is (some #(and (= condition-id (:from %))
                        (= check-operation-id (:consumer-id %))
                        (= entry-block-id (:consumer-block %))
                        (= :operand (:edge-kind %)))
                  data-edges))
        (is (some #(and (= token-id (:value-id %))
                        (= entry-terminator-id (:consumer-id %))
                        (= entry-block-id (:consumer-block %))
                        (= :runtime-check-guard (:edge-kind %)))
                  terminator-uses))
        (is (some #(and (= condition-id (:value-id %))
                        (= entry-terminator-id (:consumer-id %))
                        (= 1 (:operand-index %))
                        (= :condition (:edge-kind %)))
                  terminator-uses))
        (is (= :runtime-check-generated
               (get-in runtime-operation
                       [:source :generated-origin 0 :role])))
        (is (= :rejected (:status late-check-report)))
        (is (= "C11-BLOCK"
               (get-in late-check-report [:diagnostics 0 :diagnostic-id])))
        (is (= :block-terminator-or-control-flow-edge
               (get-in late-check-report [:diagnostics 0 :missing-fact])))))))

(deftest sh12-rejects-malformed-control-flow-and-fact-loss
  (doseq [extension [".gravity" ".qst"]]
    (testing extension
      (let [checked (checked-core extension)
            mir (build-mir checked)
            rejected-plan (get rejected-plans extension)
            cases
            (invoke rejected-plan
                    'sh12-branch-join-rejection-cases
                    [(rejected-path extension)])]
        (doseq [{:keys [case diagnostic-id missing-fact
                        actual-source-path]} cases]
          (testing (name case)
            (let [mutation
                  (invoke rejected-plan
                          'sh12-malformed-branch-join-mir
                          [case mir actual-source-path])
                  malformed (:mir mutation)
                  report (verify-mir checked malformed)
                  observed (diagnostic report)]
              (is (= actual-source-path (:actual-source-path mutation)))
              (is (not= mir malformed))
              (is (= :rejected (:status report)))
              (is (= 1 (count (:diagnostics report))))
              (is (= stable-diagnostic-fields (set (keys observed))))
              (is (= diagnostic-id (:diagnostic-id observed)))
              (is (= missing-fact (:missing-fact observed)))
              (is (= :error (:severity observed)))
              (is (= :c11-mir-verification (:stage observed)))
              (is (= actual-source-path
                     (get-in observed [:provenance
                                       :actual-source-path])))
              (if (= diagnostic-id "C11-MODULE")
                (is (= :not-applicable (:block observed)))
                (is (string? (:block observed))))
              (if (= diagnostic-id "C11-DOMINANCE")
                (is (= "sh12:branch-join:if" (:value-id observed)))
                (is (= :not-applicable (:value-id observed)))))))))))

(deftest sh12-rejects-malformed-checked-core-before-construction
  (doseq [extension [".gravity" ".qst"]]
    (testing extension
      (let [checked (checked-core extension)
            mir (build-mir checked)
            rejected-plan (get rejected-plans extension)
            actual-source-path (rejected-path extension)
            cases
            (invoke rejected-plan
                    'sh12-checked-core-rejection-cases
                    [actual-source-path])]
        (doseq [{:keys [case diagnostic-id missing-fact]} cases]
          (testing (name case)
            (let [malformed
                  (invoke rejected-plan
                          'sh12-malformed-checked-core
                          [case checked actual-source-path])
                  construction (build-mir malformed)
                  verification (verify-mir malformed mir)]
              (is (= :rejected (:status construction)))
              (is (= diagnostic-id
                     (get-in construction
                             [:diagnostics 0 :diagnostic-id])))
              (is (= missing-fact
                     (get-in construction
                             [:diagnostics 0 :missing-fact])))
              (is (= :rejected (:status verification)))
              (is (= diagnostic-id
                     (get-in verification
                             [:diagnostics 0 :diagnostic-id])))
              (is (= missing-fact
                     (get-in verification
                             [:diagnostics 0 :missing-fact])))
              (if (= case :checked-empty-provenance-path)
                (is (= ""
                       (get-in verification
                               [:diagnostics 0 :provenance
                                :actual-source-path])))
                (is (= actual-source-path
                       (get-in verification
                               [:diagnostics 0 :provenance
                                :actual-source-path])))))))))))

(deftest sh12-rejects-host-constructed-empty-symbol-origin
  (doseq [extension [".gravity" ".qst"]]
    (testing extension
      (let [checked (checked-core extension)
            mir (build-mir checked)
            node-id (get-in checked [:core-nodes 0 :node-id])
            malformed
            (update checked :core-nodes
                    (fn [nodes]
                      (mapv
                       (fn [node]
                         (if (= node-id (:node-id node))
                           (-> node
                               (assoc-in [:source :generated?] true)
                               (assoc-in [:source :generated-origin]
                                         [(symbol "")]))
                           node))
                       nodes)))
            construction (build-mir malformed)
            verification (verify-mir malformed mir)]
        (is (= :rejected (:status construction)))
        (is (= "C11-ORIGIN"
               (get-in construction [:diagnostics 0 :diagnostic-id])))
        (is (= :source-span-or-origin-fact
               (get-in construction [:diagnostics 0 :missing-fact])))
        (is (= :rejected (:status verification)))
        (is (= "C11-ORIGIN"
               (get-in verification [:diagnostics 0 :diagnostic-id])))
        (is (= :source-span-or-origin-fact
               (get-in verification [:diagnostics 0 :missing-fact])))))))
