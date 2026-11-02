(ns gravity.self-hosting.sh17-c13-optimized-mir-carrier-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh18-native-toolchain-harness :as tool-harness])
  (:import [java.nio.charset StandardCharsets]
           [java.nio.file Files]))

;; This file is deliberately a carrier test, rather than another source
;; coverage test.  The packet builders below are the authenticated public
;; ingress.  C13 is inspected as the carrier that C14 actually receives and
;; C14 is exercised through a freshly compiled Gravity plan.

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh17_c13_optimized_mir_carrier_test.clj")]
    (when-not resource
      (throw (ex-info "SH-17 C13 carrier test source is not on the classpath"
                      {:id "SH17-C13-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Gravity repository root could not be located"
                        {:id "SH17-C13-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(def ^:private c14-source-relative-path
  "bootstrap/gravity/src/gravity/compiler/c14_target_lowering_architecture.gravity")

(def ^:private c14-plan
  (delay
    (let [source-path (path c14-source-relative-path)
          source-text (slurp source-path)
          emitter
          (:emitter
           (bootstrap/c-backend-stage2-plan-emitter-source-rule!
            source-path :llvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan
       emitter source-path source-text))))

(def ^:private development-c13-plan
  (delay
    (let [source-path (path "bootstrap/gravity/src/gravity/compiler/c13_mir_optimization_passes.gravity")
          emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source-path :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source-path (slurp source-path)))))

(def ^:private development-b3-plan
  (delay
    (let [source-path (path "bootstrap/gravity/src/gravity/backend/b3_llvm_backend_design.gravity")
          emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source-path :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source-path (slurp source-path)))))

(def ^:private development-b1-plan
  (delay
    (let [source-path (path "bootstrap/gravity/src/gravity/backend/b1_backend_interface_specification.gravity")
          emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source-path :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source-path (slurp source-path)))))

(defn- development-comparison-input [left right repeated?]
  ;; Supplied actual-MIR-shaped DATA. No public compiler build, seal or C11
  ;; verification is claimed by these deliberately local development kernels.
  (let [digest bootstrap/p15-s23-c11-mir-digest
        core (digest :development-core) entry (str core ":mir:entry")
        source (fn [id] {:core-node id :origin-id (str id ":origin")
                         :span {:source-id (digest :source) :start-byte 0 :end-byte 1
                                :actual-source-path "/development/comparison.gravity"}
                         :origin-chain []})
        operation (fn [id opcode type operands payload]
                    {:artifact :gravity/mir-operation :op-id id :result id :result-kind :value
                     :opcode opcode :source-operation (if (= opcode :constant) :literal opcode)
                     :type type :operands operands :constant-payload payload
                     :effects #{} :capabilities #{} :ordering :none :profile :hosted
                     :block-id entry :verifier-status :passed :domain-anchor nil :source (source id)
                     :facts {:type-fact-id (str id ":type") :effect-fact-id (str id ":effect")
                             :ownership-fact-id (str id ":ownership") :capability-fact-id (str id ":capability")
                             :safety-outcome-id (str id ":safety") :source-origin-id (str id ":origin")}})
        ops [(operation "left" :constant :gravity/integer [] {:present? true :value left})
             (operation "right" :constant :gravity/integer [] {:present? true :value right})
             (operation "comparison" :integer-eq :gravity/bool
                        ["left" (if repeated? "left" "right")] :not-applicable)]
        edges (mapv (fn [index id] {:from id :consumer-kind :operation :consumer-id "comparison"
                                   :consumer-block entry :operand-index index :edge-kind :operand})
                    [0 1] (:operands (last ops)))
        use-row (fn [edge] (assoc (dissoc edge :from) :use-kind :instruction))
        terminator-use {:value-id "comparison" :consumer-kind :terminator :consumer-id "return"
                        :consumer-block entry :operand-index 0 :edge-kind :return-value}
        values (into {} (map (fn [op]
                              [(:result op) {:artifact :gravity/mir-value :value-id (:result op)
                                            :kind :instruction-result :type (:type op)
                                            :constant-payload (:constant-payload op)
                                            :defined-by {:value-id (:result op) :operation-id (:op-id op) :block-id entry}
                                            :uses (if (= "comparison" (:result op))
                                                    [(assoc (dissoc terminator-use :value-id) :use-kind :terminator)]
                                                    (mapv use-row (filter #(= (:result op) (:from %)) edges)))
                                            :source (:source op) :ownership {:model :persistent-immutable-value}
                                            :effects #{} :safety {:outcome :proven-safe}}]) ops))
        table (fn [suffix f] (into {} (map (fn [op] [(str (:op-id op) suffix) (f op)]) ops)))
        mir {:artifact :gravity/mir-module :schema-version 1 :module-id (digest :module) :source-core core
             :profile :hosted :target-request :llvm-x86_64-linux :target-independent? true
             :functions {'main {:params [] :returns :gravity/bool :latent-effects #{} :capabilities #{}
                                :entry entry :blocks {entry {:instructions ops
                                                           :terminator {:kind :return :operands ["comparison"]}}}}}
             :control-flow-graph {:entry entry :edges [] :dominance {entry [entry]} :join nil}
             :data-flow-graph {:edges edges :terminator-uses [terminator-use]
                               :definitions (into {} (map (fn [[id value]] [id (:defined-by value)]) values))
                               :values values}
             :type-table (table ":type" #(hash-map :fact-id (str (:op-id %) ":type") :core-node-id (:op-id %)
                                                   :type (:type %) :dependencies (:operands %)
                                                   :source-origin-id (get-in % [:source :origin-id])))
             :effect-table (table ":effect" #(hash-map :direct #{} :core-node-id (:op-id %)))
             :ownership-table (table ":ownership" #(hash-map :model :persistent-immutable-value :core-node-id (:op-id %)))
             :capability-table (table ":capability" #(hash-map :required #{} :granted #{} :core-node-id (:op-id %)))
             :safety-table (table ":safety" #(hash-map :outcome :proven-safe :core-node-id (:op-id %)))
             :runtime-check-table {} :proof-certificate-table {} :effect-order-graph {:edges []}
             :source-map (into {} (map (juxt :op-id :source) ops)) :globals {} :domain-anchors {}
             :pass-execution-record {:record-id (digest :original-pass)} :b1-preflight {:input-only true}}
        artifact {:artifact-id (digest :supplied-artifact) :mir-id (digest mir) :mir-module mir}
        report {:status :passed :mir-id (:mir-id artifact) :semantic-replay-parity :passed
                :execution-tcb :clojure-stage0-rule-runner :independent-verifier {:input-only true}
                :b1-preflight (:b1-preflight mir)}]
    {:mir mir :evidence (bootstrap/p15-s23-c13-evidence-for-target artifact report :llvm-x86_64-linux)}))

(deftest sh17-development-folds-one-actual-mir-shaped-integer-equality
  (let [invoke (fn [function arguments]
                 (bootstrap/p15-s23-stage2-runtime-execute-function
                  {:engine :gravity-sh17-development-comparison :compiler-artifact-plan? true}
                  @development-c13-plan function arguments))]
    (doseq [[left right repeated? expected] [[2 2 false true] [2 3 false false]
                                            [-9223372036854775808 9223372036854775807 false false]
                                            [9223372036854775807 0 true true]]]
      (let [{:keys [mir evidence]} (development-comparison-input left right repeated?)
            result (invoke 'c13-build-bounded-identity-optimized-mir [mir evidence])
            output (:optimized-mir result) entry (get-in mir [:control-flow-graph :entry])
            comparison (get-in output [:functions 'main :blocks entry :instructions 2])]
        (is (= :accepted (:status result)))
        (is (= :gravity/c13-bounded-equality-fold-optimized-mir (:artifact result)))
        (is (= :constant (:opcode comparison)))
        (is (= :integer-eq (:source-operation comparison)))
        (is (= [] (:operands comparison)))
        (is (= {:present? true :value expected} (:constant-payload comparison)))
        (is (= (:constant-payload comparison) (get-in output [:data-flow-graph :values "comparison" :constant-payload])))
        (is (= [] (get-in output [:data-flow-graph :edges])))
        (is (= [] (get-in output [:data-flow-graph :values "left" :uses])))
        (is (= [] (get-in output [:data-flow-graph :values "right" :uses])))
        (is (= (:control-flow-graph mir) (:control-flow-graph output)))
        (is (= (:source-map mir) (:source-map output)))
        (is (= (:pass-execution-record mir) (:pass-execution-record output)))
        (is (= (:b1-preflight mir) (:b1-preflight output)))
        (is (= result (bootstrap/p15-s23-c13-expected-record mir evidence)))
        (let [delta (get-in result [:verifier-replay :delta])
              invoke-c14-kernel (fn [function arguments]
                                 (bootstrap/p15-s23-stage2-runtime-execute-function
                                  {:engine :gravity-sh17-development-comparison :compiler-artifact-plan? true}
                                  @c14-plan function arguments))
              expected (bootstrap/p15-s23-c13-expected-record mir evidence)
              policy {:fact-bindings (get-in result [:semantic-identity :fact-bindings])
                      :supported-operation-ids (get-in result [:semantic-identity :operation-order])}]
          (is (= mir (gravity.bootstrap-c13-comparison/restore output delta)))
          (is (= mir (invoke-c14-kernel 'c14-equality-original-mir [result])))
          (is (true? (invoke-c14-kernel 'c14-equality-translation-valid? [result policy])))
          (is (true? (invoke-c14-kernel 'c14-llvm-bounded-mir-surface-valid? [result policy])))
          (is (false? (invoke-c14-kernel 'c14-c-source-operation-valid? [:constant :integer-eq])))
          (when (= [left right repeated?] [2 2 false])
            (println "comparison delta carrier nodes (including map entries):" (count (tree-seq coll? seq delta))))
          (doseq [changed-delta [(assoc delta :unexpected true) (assoc delta :edges [])
                                (assoc delta :edges (conj (:edges delta) (first (:edges delta))))
                                (assoc-in delta [:edges 0 :index] -1) (assoc-in delta [:edges 1 :index] 65535)
                                (assoc-in delta [:edges 1 :index] (get-in delta [:edges 0 :index]))
                                (update delta :edges #(vec (reverse %))) (update delta :uses #(vec (reverse %)))
                                (assoc-in delta [:uses 0 :use :unexpected] true)
                                (assoc-in delta [:uses 0 :index] "0")
                                (assoc-in delta [:uses 0 :use :consumer-id] "other")]]
            (let [candidate (assoc-in result [:verifier-replay :delta] changed-delta)]
              (is (false? (invoke-c14-kernel 'c14-equality-translation-valid? [candidate policy])))
              (is (not= expected candidate))))
          (doseq [candidate [(assoc-in result [:optimized-mir :functions 'main :blocks entry :instructions 2 :constant-payload :value]
                                     (not (get-in comparison [:constant-payload :value])))
                             (assoc-in result [:optimized-mir :functions 'main :blocks entry :instructions 2 :constant-payload] 7)
                             (assoc-in result [:optimized-mir :data-flow-graph :values "comparison" :constant-payload :value] :not-a-bool)
                             (assoc-in result [:optimized-mir :type-table "comparison:type" :type] :gravity/integer)
                             (assoc-in result [:optimized-mir :source-map "comparison" :origin-id] "changed")
                             (assoc-in result [:optimized-mir :safety-table "left:safety" :outcome] :unsafe-island)
                             (assoc-in result [:optimized-mir :functions 'main :blocks entry :instructions 2 :block-id] "other")
                             (update-in result [:optimized-mir :data-flow-graph :edges]
                                        conj (first (get-in mir [:data-flow-graph :edges])))
                             (update-in result [:optimized-mir :data-flow-graph :values "left" :uses]
                                        conj (assoc (first (get-in mir [:data-flow-graph :values "left" :uses])) :operand-index 9))
                             (assoc-in result [:decision-record :changed-operations] ["left"])
                             (assoc-in result [:invalidation-ledger :facts-invalidated] [])
                             (assoc-in result [:verifier-replay :artifact] :gravity/c13-post-pass-verifier-replay)]]
            (is (false? (invoke-c14-kernel 'c14-equality-translation-valid? [candidate policy])))
            (is (not= expected candidate)))
          (is (not= expected (assoc-in result [:semantic-identity :c11-output-mir-id]
                                      (bootstrap/p15-s23-c11-mir-digest :fabricated-output)))))))
    (let [{:keys [mir]} (development-comparison-input 2 3 false)
          entry (get-in mir [:control-flow-graph :entry]) path [:functions 'main :blocks entry :instructions]
          original-report {:status :passed :module-shape-valid? true :block-count 1 :operation-count 3
                           :conditional-count 0 :dominance-valid? true :type-facts-valid? true
                           :direct-effects-valid? true :safety-facts-valid? true :origin-closure-valid? true
                           :runtime-checks-valid? true :runtime-checks {:runtime-check-count 0 :runtime-check-ids []}
                           :target-independent? true :data-flow {:edge-count 2 :terminator-use-count 1 :definition-count 3 :value-count 3}}
          invoke-c14-kernel (fn [function arguments]
                             (bootstrap/p15-s23-stage2-runtime-execute-function
                              {:engine :gravity-sh17-development-comparison :compiler-artifact-plan? true}
                              @c14-plan function arguments))]
      (doseq [input [(assoc mir :target-request :c) (assoc mir :target-request :wasm)
                     (assoc-in mir (conj path 0 :type) :gravity/bool)
                     (assoc-in mir (conj path 0 :constant-payload :value) nil)
                     (assoc-in mir (conj path 0 :constant-payload :value) 9223372036854775808N)
                     (assoc-in mir (conj path 2 :operands) ["missing" "right"])
                     (assoc-in mir (conj path 2 :operands) ["comparison" "right"])
                     (assoc-in mir (conj path 2 :effects) #{:io/write})
                     (assoc-in mir [:safety-table "comparison:safety" :outcome] :unsafe-island)
                     (assoc-in mir [:type-table "comparison:type" :type] :gravity/integer)
                     (assoc-in mir [:source-map "comparison" :origin-id] "changed")
                     (assoc-in mir [:data-flow-graph :edges 0 :operand-index] 1)
                     (update-in mir path conj (assoc (get-in mir (conj path 0)) :op-id "duplicate"))]]
        (let [artifact {:artifact-id (bootstrap/p15-s23-c11-mir-digest :input) :mir-id (bootstrap/p15-s23-c11-mir-digest input) :mir-module input}
              evidence (bootstrap/p15-s23-c13-evidence-for-target artifact {:status :passed :semantic-replay-parity :passed} (:target-request input))
              result (invoke 'c13-build-bounded-identity-optimized-mir [input evidence])]
          (is (= :gravity/c13-bounded-identity-optimized-mir (:artifact result)))
          (is (= input (:optimized-mir result)))
          (is (= result (bootstrap/p15-s23-c13-expected-record input evidence)))))
      (let [{:keys [evidence]} (development-comparison-input 2 3 false)
            result (invoke 'c13-build-bounded-identity-optimized-mir [mir evidence])
            output (:optimized-mir result)]
        (is (true? (invoke-c14-kernel 'c14-c11-independent-verifier-valid? [mir original-report])))
        (is (false? (invoke-c14-kernel 'c14-c11-independent-verifier-valid? [output original-report])))
        (is (true? (invoke-c14-kernel 'c14-c11-independent-verifier-valid?
                                    [(invoke-c14-kernel 'c14-equality-original-mir [result]) original-report]))))
      (doseq [malformed [(assoc-in mir [:functions 'main :blocks] 7)
                         (assoc-in mir (conj path) 7)
                         (assoc-in mir (conj path) (vec (repeat 257 (get-in mir (conj path 0)))))
                         (assoc-in mir [:data-flow-graph :edges] 7)
                         (assoc-in mir [:data-flow-graph :edges] (vec (repeat 65537 {})))
                         (assoc-in mir [:data-flow-graph :values "left" :uses] 7)
                         (assoc-in mir [:data-flow-graph :values "left" :uses] (vec (repeat 65537 {})))]]
        (is (nil? (invoke 'c13-equality-selection [malformed])))
        (is (nil? (gravity.bootstrap-c13-comparison/selection malformed))))
      (let [{:keys [evidence]} (development-comparison-input 2 3 false)]
        (doseq [malformed [(assoc-in mir [:functions 'main :blocks] 7) (assoc-in mir (conj path) 7)]]
          (let [result (invoke 'c13-build-bounded-identity-optimized-mir [malformed evidence])]
            (is (= :rejected (:status result)))
            (is (= "C13-VERIFY" (:diagnostic result)))))))))

(defn- development-b3-comparison-input [left right repeated?]
  ;; Complete supplied MIR DATA, not a compiler product or authentication. All
  ;; envelope fields are present BEFORE C13; no transformed output is retagged.
  (let [{:keys [mir]} (development-comparison-input left right repeated?)
        entry (get-in mir [:control-flow-graph :entry])
        source (get-in mir [:source-map "comparison"])
        terminator-id (str entry ":return")
        operations (mapv #(update % :facts merge
                                  {:capability-proof-ids [] :safety-proof-id :not-applicable
                                   :profile-target-fact-id :not-applicable :runtime-check-id :not-applicable
                                   :proof-certificate-ids [] :failure-behavior :not-applicable})
                         (get-in mir [:functions 'main :blocks entry :instructions]))
        block {:artifact :gravity/mir-block :block-id entry :arguments [] :instructions operations
               :terminator {:artifact :gravity/mir-terminator :terminator-id terminator-id
                            :kind :return :operands ["comparison"] :successors [] :effects #{}
                            :ordering :none :source source :profile :hosted
                            :facts {:returned-value "comparison"} :verifier-status :passed}
               :predecessors [] :successors [] :dominators [entry]
               :data-flow {:module-data-flow-graph-reference true} :source source}
        original (-> mir
                     (assoc :source-target :jvm :verification-status :passed :diagnostics []
                            :target-request-metadata {:requested-target :llvm-x86_64-linux :source-target :jvm
                                                      :identity-bearing? false :downstream-lowering-required? true}
                            :capability-proof-table {} :profile-target-table {} :provenance {}
                            :construction {} :pass-contract {} :scope {}
                            :clojure-seed-boundary? true :self-hosted? false)
                     (assoc :functions {'main {:artifact :gravity/mir-function :fn-id "main" :name 'main
                                              :params [] :returns :gravity/bool :latent-effects #{} :capabilities #{}
                                              :entry entry :blocks {entry block} :source source :facts {} :provenance {}}})
                     (assoc-in [:data-flow-graph :terminator-uses 0 :consumer-id] terminator-id)
                     (assoc-in [:data-flow-graph :values "comparison" :uses 0 :consumer-id] terminator-id))
        artifact {:artifact-id (bootstrap/p15-s23-c11-mir-digest :supplied-b3-artifact)
                  :mir-id (bootstrap/p15-s23-c11-mir-digest original) :mir-module original}]
    {:mir original
     :evidence (bootstrap/p15-s23-c13-evidence-for-target
                artifact {:status :passed :semantic-replay-parity :passed} :llvm-x86_64-linux)}))

(deftest sh17-development-emits-c13-folded-bools-through-existing-b3-builder
  ;; B3's verified-MIR input remains an UPSTREAM premise. These direct kernels
  ;; do not seal a public packet or independently authenticate/reprove C13.
  (let [execute (fn [plan function arguments]
                  (bootstrap/p15-s23-stage2-runtime-execute-function
                   {:engine :gravity-sh17-development-b3-comparison :compiler-artifact-plan? true}
                   plan function arguments))
        build #(execute @development-b3-plan 'b3-build-bounded-llvm-x86_64-linux [%])]
    (doseq [[left right repeated? expected] [[2 2 false true] [2 3 false false]
                                            [-9223372036854775808 9223372036854775807 false false]
                                            [9223372036854775807 0 true true]]]
      (let [{:keys [mir evidence]} (development-b3-comparison-input left right repeated?)
            folded (execute @development-c13-plan 'c13-build-bounded-identity-optimized-mir [mir evidence])
            output (:optimized-mir folded) entry (get-in mir [:control-flow-graph :entry])
            operation-path [:functions 'main :blocks entry :instructions 2]
            policy {:fact-bindings (get-in folded [:semantic-identity :fact-bindings])
                    :supported-operation-ids (get-in folded [:semantic-identity :operation-order])}
            original-lowering (build mir) lowering (build output)
            records (:operation-records lowering)]
        (is (= :accepted (:status folded)))
        (is (= folded (bootstrap/p15-s23-c13-expected-record mir evidence)))
        (is (true? (execute @c14-plan 'c14-equality-translation-valid? [folded policy])))
        (is (= mir (execute @c14-plan 'c14-equality-original-mir [folded])))
        (is (= :constructed-unverified (:status original-lowering)))
        (is (re-find #"icmp eq i64" (or (:llvm-ir original-lowering) "")))
        (is (= :constructed-unverified (:status lowering)))
        (is (= :llvm-x86_64-linux (:target lowering)))
        (is (= expected (:semantic-result lowering)))
        (is (= (if expected 1 0) (:expected-exit-code lowering)))
        (is (= ["left" "right" "comparison"] (mapv :mir-operation-id records)))
        (is (= (take 2 (:operation-records original-lowering)) (take 2 records)))
        (is (= :integer-eq (:source-operation (last records))))
        (is (= :constant (:mir-opcode (last records))))
        (is (= (str "  %v2 = add i64 0, " (if expected 1 0)) (:llvm-instruction (last records))))
        (is (not (re-find #"icmp|zext" (or (:llvm-ir lowering) ""))))
        (is (re-find #"target triple = \"x86_64-unknown-linux-gnu\"" (or (:llvm-ir lowering) "")))
        (is (= [] (:ub-sensitive-flags lowering) (:emitted-metadata lowering)))
        (is (= :pending-host-content-binding (get-in lowering [:verified-input-closure :status])))
        ;; These legacy closure values name upstream obligations/input lineage;
        ;; they do not claim C11 reverified the transformed C13 output.
        (is (= :required-upstream-contextual-verifier
               (get-in lowering [:verified-input-closure :c11-full-mir-authenticity])))
        (is (= :upstream-c11-owned (get-in lowering [:verified-input-closure :full-dfg-edge-use-replay])))))
    (let [{:keys [mir evidence]} (development-b3-comparison-input 2 2 false)
          folded (execute @development-c13-plan 'c13-build-bounded-identity-optimized-mir [mir evidence])
          output (:optimized-mir folded) entry (get-in mir [:control-flow-graph :entry])
          op-path [:functions 'main :blocks entry :instructions 2]
          change (fn [field value]
                   (let [changed (assoc-in output (conj op-path field) value)]
                     (if (contains? #{:type :constant-payload} field)
                       (assoc-in changed [:data-flow-graph :values "comparison" field] value) changed)))]
      (doseq [[field value] [[:source-operation :integer-lt] [:source-operation :call]
                             [:type :gravity/integer] [:constant-payload {:present? true :value nil}]
                             [:constant-payload {:present? true :value 1}] [:constant-payload {:present? true}]
                             [:constant-payload {:present? true :value true :extra 1}]
                             [:operands ["left"]]]]
        (let [rejected (build (change field value))]
          (is (= :rejected (:status rejected)))
          (is (= "B1-UNSUPPORTED" (:diagnostic rejected)))
          (is (nil? (:llvm-ir rejected)))))
      (doseq [[field value reason] [[:effects #{:io/write} :operation-effects-must-be-empty-set]
                                    [:verifier-status :rejected :passed-operation-verifier-status]
                                    [:constant-payload {:present? true :value false} :mir-value-payload-parity]]]
        (let [rejected (build (assoc-in output (conj op-path field) value))]
          (is (= :rejected (:status rejected)))
          (is (= "B1-INPUT" (:diagnostic rejected)))
          (is (= reason (:missing-fact rejected))))))))

(defn- development-diamond-input [left right repeated? then-value else-value]
  ;; Complete supplied diamond before optimization. No source build or seal.
  (let [{base :mir} (development-b3-comparison-input left right repeated?)
        core (:source-core base) entry (str core ":mir:entry")
        then-id (str core ":mir:then") else-id (str core ":mir:else") join-id (str core ":mir:join")
        entry-ops (get-in base [:functions 'main :blocks entry :instructions])
        prototype (first entry-ops)
        operation (fn [id block opcode source-operation operands payload]
                    (-> prototype
                        (assoc :op-id id :result id :block-id block :opcode opcode :source-operation source-operation
                               :type :gravity/integer :operands operands :constant-payload payload
                               :source (assoc (:source prototype) :core-node id :origin-id (str id ":origin")))
                        (update :facts merge {:type-fact-id (str id ":type") :effect-fact-id (str id ":effect")
                                              :ownership-fact-id (str id ":ownership") :capability-fact-id (str id ":capability")
                                              :safety-outcome-id (str id ":safety") :source-origin-id (str id ":origin")})))
        then-op (operation "then-value" then-id :constant :literal [] {:present? true :value then-value})
        else-op (operation "else-value" else-id :constant :literal [] {:present? true :value else-value})
        join-op (operation "join-value" join-id :conditional-join :if ["comparison" "then-value" "else-value"] :not-applicable)
        operations (conj entry-ops then-op else-op join-op)
        source (:source join-op)
        terminator (fn [block kind value successors facts]
                     {:artifact :gravity/mir-terminator :terminator-id (str block (case kind :return ":return" :branch ":branch" ":conditional"))
                      :kind kind :operands [value] :successors successors :effects #{} :ordering :none
                      :source source :profile :hosted :facts facts :verifier-status :passed})
        block (fn [id rows predecessors successors dominators term]
                {:artifact :gravity/mir-block :block-id id :arguments [] :instructions rows :terminator term
                 :predecessors predecessors :successors successors :dominators dominators
                 :data-flow {:module-data-flow-graph-reference true} :source source})
        blocks {entry (block entry entry-ops [] [then-id else-id] [entry]
                             (terminator entry :conditional-branch "comparison" [then-id else-id] {:condition "comparison" :join join-id}))
                then-id (block then-id [then-op] [entry] [join-id] [entry then-id]
                               (terminator then-id :branch "then-value" [join-id] {:incoming-value "then-value"}))
                else-id (block else-id [else-op] [entry] [join-id] [entry else-id]
                               (terminator else-id :branch "else-value" [join-id] {:incoming-value "else-value"}))
                join-id (block join-id [join-op] [then-id else-id] [] [entry join-id]
                               (terminator join-id :return "join-value" [] {:returned-value "join-value"}))}
        cfg {:entry entry :edges [{:from entry :to then-id :kind :true} {:from entry :to else-id :kind :false}
                                 {:from then-id :to join-id :kind :join} {:from else-id :to join-id :kind :join}]
             :dominance {entry [entry] then-id [entry then-id] else-id [entry else-id] join-id [entry join-id]}
             :join {:block-id join-id :value-id "join-value"
                    :incoming [{:predecessor then-id :value "then-value"} {:predecessor else-id :value "else-value"}]}}
        edges (vec (mapcat (fn [op] (map-indexed (fn [index id] {:from id :consumer-kind :operation :consumer-id (:op-id op)
                                                               :consumer-block (:block-id op) :operand-index index :edge-kind :operand})
                                               (:operands op))) operations))
        terminator-uses (mapv (fn [id kind] (let [term (:terminator (blocks id))]
                                             {:value-id (first (:operands term)) :consumer-kind :terminator :consumer-id (:terminator-id term)
                                              :consumer-block id :operand-index 0 :edge-kind kind}))
                             [entry then-id else-id join-id] [:condition :branch-value :branch-value :return-value])
        values (into {} (map (fn [op]
                              [(:op-id op) {:artifact :gravity/mir-value :value-id (:op-id op) :kind :instruction-result
                                            :type (:type op) :constant-payload (:constant-payload op)
                                            :defined-by {:value-id (:op-id op) :operation-id (:op-id op) :block-id (:block-id op)}
                                            :uses (into (mapv #(assoc (dissoc % :from) :use-kind :instruction) (filter #(= (:op-id op) (:from %)) edges))
                                                        (map #(assoc (dissoc % :value-id) :use-kind :terminator)
                                                             (filter #(= (:op-id op) (:value-id %)) terminator-uses)))
                                            :source (:source op) :ownership {:model :persistent-immutable-value}
                                            :effects #{} :safety {:outcome :proven-safe}}]) operations))
        table (fn [suffix f] (into {} (map (fn [op] [(str (:op-id op) suffix) (f op)]) operations)))
        mir (-> base
                (assoc-in [:functions 'main :returns] :gravity/integer)
                (assoc-in [:functions 'main :blocks] blocks)
                (assoc :control-flow-graph cfg
                       :data-flow-graph {:edges edges :terminator-uses terminator-uses :values values
                                         :definitions (into {} (map (fn [[id value]] [id (:defined-by value)]) values))}
                       :source-map (into {} (map (juxt :op-id :source) operations))
                       :type-table (table ":type" #(hash-map :fact-id (str (:op-id %) ":type") :core-node-id (:op-id %)
                                                              :type (:type %) :dependencies (:operands %)
                                                              :source-origin-id (get-in % [:source :origin-id])))
                       :effect-table (table ":effect" #(hash-map :direct #{} :core-node-id (:op-id %)))
                       :ownership-table (table ":ownership" #(hash-map :model :persistent-immutable-value :core-node-id (:op-id %)))
                       :capability-table (table ":capability" #(hash-map :required #{} :granted #{} :core-node-id (:op-id %)))
                       :safety-table (table ":safety" #(hash-map :outcome :proven-safe :core-node-id (:op-id %)))))
        artifact {:artifact-id (bootstrap/p15-s23-c11-mir-digest :supplied-diamond)
                  :mir-id (bootstrap/p15-s23-c11-mir-digest mir) :mir-module mir}]
    {:mir mir :evidence (bootstrap/p15-s23-c13-evidence-for-target
                        artifact {:status :passed :semantic-replay-parity :passed} :llvm-x86_64-linux)}))

(deftest sh17-development-folded-predicate-preserves-integer-diamond-emission
  (let [execute (fn [plan function arguments]
                  (bootstrap/p15-s23-stage2-runtime-execute-function
                   {:engine :gravity-sh17-development-folded-diamond :compiler-artifact-plan? true} plan function arguments))
        build #(execute @development-b3-plan 'b3-build-bounded-llvm-x86_64-linux [%])]
    (doseq [[left right repeated? then-value else-value expected]
            [[2 2 false 42 7 42] [2 3 false 42 7 7]
             [-9223372036854775808 9223372036854775807 false 0 255 255]
             [9223372036854775807 0 true 0 255 0]]]
      (let [{:keys [mir evidence]} (development-diamond-input left right repeated? then-value else-value)
            folded (execute @development-c13-plan 'c13-build-bounded-identity-optimized-mir [mir evidence])
            output (:optimized-mir folded) cfg (:control-flow-graph mir) entry (:entry cfg)
            join-id (get-in cfg [:join :block-id])
            policy {:fact-bindings (get-in folded [:semantic-identity :fact-bindings])
                    :supported-operation-ids (get-in folded [:semantic-identity :operation-order])}
            original-lowering (build mir) lowering (build output) ir (or (:llvm-ir lowering) "")]
        (is (= :accepted (:status folded)))
        (is (= folded (bootstrap/p15-s23-c13-expected-record mir evidence)))
        (is (= mir (execute @c14-plan 'c14-equality-original-mir [folded])))
        (is (true? (execute @c14-plan 'c14-equality-translation-valid? [folded policy])))
        (is (true? (execute @c14-plan 'c14-llvm-bounded-mir-surface-valid? [folded policy])))
        (is (= cfg (:control-flow-graph output)))
        (is (= (:source-map mir) (:source-map output)))
        (is (= (:type-table mir) (:type-table output)))
        (is (= (vec (drop 2 (get-in mir [:data-flow-graph :edges]))) (get-in output [:data-flow-graph :edges])))
        (is (= (get-in mir [:data-flow-graph :terminator-uses]) (get-in output [:data-flow-graph :terminator-uses])))
        (is (= :constructed-unverified (:status original-lowering) (:status lowering)))
        (is (= expected (:semantic-result original-lowering) (:semantic-result lowering) (:expected-exit-code lowering)))
        (is (= :pending-host-content-binding (get-in lowering [:verified-input-closure :status])))
        (is (= [entry (str (:source-core mir) ":mir:then") (str (:source-core mir) ":mir:else") join-id] (:block-order lowering)))
        (is (re-find #"%branchcond = icmp ne i64 %v2, 0" ir))
        (is (re-find #"br i1 %branchcond, label %then, label %else" ir))
        (is (re-find #"%v5 = phi i64 \[ %v3, %then \], \[ %v4, %else \]" ir))
        (is (not (re-find #"icmp eq|zext" ir)))
        (is (= (drop 3 (:operation-records original-lowering)) (drop 3 (:operation-records lowering))))
        (doseq [candidate [(assoc-in output [:control-flow-graph :join :incoming 0 :value] "else-value")
                           (assoc-in output [:functions 'main :blocks entry :terminator :operands] ["missing"])
                           (assoc-in output [:functions 'main :blocks join-id :instructions 0 :source-operation] :call)
                           (assoc-in output [:data-flow-graph :values "comparison" :constant-payload :value] :not-bool)]]
          (let [rejected (build candidate)]
            (is (= :rejected (:status rejected)))
            (is (nil? (:llvm-ir rejected)))))
        (is (false? (execute @c14-plan 'c14-equality-translation-valid?
                            [(assoc-in folded [:optimized-mir :functions 'main :blocks entry :instructions 2 :constant-payload :value] :not-bool) policy])))
        (is (false? (execute @c14-plan 'c14-llvm-bounded-mir-surface-valid?
                            [(assoc-in folded [:optimized-mir :functions 'main :blocks join-id :instructions 0 :type] :gravity/bool) policy])))
        ;; Incoming control-flow preservation is checked against the ORIGINAL
        ;; context, not attributed to the local inverse/forward fold predicate.
        (is (not= (bootstrap/p15-s23-c13-expected-record mir evidence)
                  (assoc-in folded [:optimized-mir :control-flow-graph :join :incoming 0 :value] "else-value")))))))

(deftest sh17-development-folded-bool-llvm-ir-passes-cross-target-clang
  ;; External cross-target IR validation only: supplied MIR DATA, no public
  ;; packet, linking, target execution, fresh C11 or native-admission claim.
  (let [requested-target "x86_64-unknown-linux-gnu"
        requested-tool (System/getenv "GRAVITY_SH18_CC")
        _ (when-not (and requested-tool (.isAbsolute (io/file requested-tool)))
            (throw (ex-info "Set GRAVITY_SH18_CC to an absolute Clang executable"
                            {:requested requested-tool})))
        toolchain (tool-harness/discover-toolchain)
        _ (when-not (and (= :available (:status toolchain))
                         (= :clang (:compiler-family toolchain))
                         (= requested-tool (:selected toolchain)))
            (throw (ex-info "Explicit Clang is unavailable; tool validation cannot skip"
                            {:toolchain toolchain})))
        compiler (:compiler-path toolchain)
        tool-observation {:toolchain toolchain :compiler-sha256 (tool-harness/file-sha256 compiler)
                          :requested-target requested-target
                          :identity-probe-commands [[compiler "--version"] [compiler "-dumpmachine"]]
                          :identity-probe-timeout-ms 5000
                          :identity-probe-stream-bound tool-harness/maximum-output-bytes
                          :authoritative? false}
        execute (fn [plan function arguments]
                  (bootstrap/p15-s23-stage2-runtime-execute-function
                   {:engine :gravity-sh17-development-cross-target-clang :compiler-artifact-plan? true}
                   plan function arguments))
        cases (mapv
               (fn [[label diamond? right expected]]
                 (let [{:keys [mir evidence]} (if diamond?
                                              (development-diamond-input 2 right false 42 7)
                                              (development-b3-comparison-input 2 right false))
                       folded (execute @development-c13-plan 'c13-build-bounded-identity-optimized-mir [mir evidence])
                       policy {:fact-bindings (get-in folded [:semantic-identity :fact-bindings])
                               :supported-operation-ids (get-in folded [:semantic-identity :operation-order])}
                       lowering (execute @development-b3-plan 'b3-build-bounded-llvm-x86_64-linux [(:optimized-mir folded)])]
                   (is (= :accepted (:status folded)))
                   (is (= folded (bootstrap/p15-s23-c13-expected-record mir evidence)))
                   (is (= mir (execute @c14-plan 'c14-equality-original-mir [folded])))
                   (is (true? (execute @c14-plan 'c14-equality-translation-valid? [folded policy])))
                   (is (true? (execute @c14-plan 'c14-llvm-bounded-mir-surface-valid? [folded policy])))
                   (is (= :constructed-unverified (:status lowering)))
                   (is (= :pending-host-content-binding (get-in lowering [:verified-input-closure :status])))
                   (is (= expected (:semantic-result lowering)))
                   (is (str/includes? (:llvm-ir lowering) (str "target triple = \"" requested-target "\"")))
                   {:label label :ir (:llvm-ir lowering) :reject? false}))
               [["linear-true" false 2 true] ["linear-false" false 3 false]
                ["diamond-true" true 2 42] ["diamond-false" true 3 7]])
        negative-ir (str/replace-first (:ir (last cases)) "ret i32 %exit" "ret i32 %sh17_undefined")
        subjects (conj cases {:label "undefined-ssa" :ir negative-ir :reject? true})]
    (is (not= (:ir (last cases)) negative-ir))
    (is (= 5 (count subjects)))
    (is (.isAbsolute (io/file compiler)))
    (is (not (str/blank? (:version-line toolchain))))
    (is (not (str/blank? (:target toolchain))))
    (is (false? (:shell-used? toolchain)))
    (println (pr-str {:sh17-external-tool-observation tool-observation}))
    (tool-harness/with-temporary-directory
      "gravity-sh17-folded-bool-ir-"
      (fn [directory]
        ;; Each call returns through the existing bounded runner before the
        ;; next starts. File-size checks are postconditions, not OS quotas.
        (doseq [{:keys [label ir reject?]} subjects]
          (let [bytes (.getBytes ir StandardCharsets/UTF_8)
                source (.resolve directory (str label ".ll"))
                output (.resolve directory (str label ".s"))
                _ (when-not (<= 1 (alength bytes) 65536)
                    (throw (ex-info "LLVM IR size exceeds test bound" {:case label :bytes (alength bytes)})))
                _ (Files/write source bytes (make-array java.nio.file.OpenOption 0))
                source-hash (tool-harness/file-sha256 source)
                command [compiler "-target" requested-target "-x" "ir" "-Werror=override-module"
                         "-S" (str source) "-o" (str output)]
                process (tool-harness/run-bounded-process
                         {:command command :working-directory (str directory)
                          :timeout-ms 10000 :max-output-bytes 8192})
                output-bytes (when (Files/isRegularFile output (make-array java.nio.file.LinkOption 0))
                               (Files/size output))
                _ (when (and output-bytes (> output-bytes 65536))
                    (throw (ex-info "Assembly size exceeds test bound" {:case label :bytes output-bytes})))
                observation {:case label :command command :process process
                             :llvm-ir ir :ir-bytes (alength bytes) :ir-sha256 source-hash
                             :assembly-bytes output-bytes
                             :assembly-sha256 (when output-bytes (tool-harness/file-sha256 output))
                             :authoritative? false}]
            (println (pr-str {:sh17-cross-target-compile-observation observation}))
            (is (= :completed (:status process)) observation)
            (is (<= (:stdout-bytes process) 8192))
            (is (<= (:stderr-bytes process) 8192))
            (if reject?
              (do (is (integer? (:exit-code process)))
                  (is (not= 0 (:exit-code process)))
                  (is (str/includes? (:stderr process) "sh17_undefined")))
              (do (is (= 0 (:exit-code process)) observation)
                  (is (and (integer? output-bytes) (pos? output-bytes)))
                  (is (= ir (slurp (.toFile source) :encoding "UTF-8")))))))))))

(defn- development-b1-request [fold?]
  ;; Declared semantic DATA, never a sealed artifact or a current source proof.
  (let [digest bootstrap/p15-s23-c11-mir-digest
        {base :mir} (development-b3-comparison-input 2 3 false)
        entry (get-in base [:control-flow-graph :entry])
        mir (if fold? base (-> base
                              (assoc-in [:functions 'main :blocks entry :instructions 2 :opcode] :integer-lt)
                              (assoc-in [:functions 'main :blocks entry :instructions 2 :source-operation] :integer-lt)))
        artifact {:artifact-id (digest :supplied-b1-input) :mir-id (digest mir) :mir-module mir}
        evidence (bootstrap/p15-s23-c13-evidence-for-target artifact {:status :passed :semantic-replay-parity :passed} :llvm-x86_64-linux)
        record (bootstrap/p15-s23-stage2-runtime-execute-function
                {:engine :gravity-sh17-development-b1 :compiler-artifact-plan? true}
                @development-c13-plan 'c13-build-bounded-identity-optimized-mir [mir evidence])
        ;; IDs label supplied DATA here, not a public seal or authentication.
        optimized (assoc record :artifact-id (digest [:declared-artifact record]) :semantic-id (digest record))
        independent {:status :passed :module-shape-valid? true :block-count 1 :operation-count 3 :conditional-count 0
                     :dominance-valid? true :type-facts-valid? true :direct-effects-valid? true :safety-facts-valid? true
                     :origin-closure-valid? true :runtime-checks-valid? true :runtime-checks {:runtime-check-count 0 :runtime-check-ids []}
                     :target-independent? true :data-flow {:edge-count 2 :terminator-use-count 1 :definition-count 3 :value-count 3}}
        verifier {:status :passed :mir-id (:mir-id artifact) :semantic-replay-parity :passed :execution-tcb :clojure-stage0-rule-runner
                  :independent-verifier independent
                  :b1-preflight {:artifact :gravity/verified-mir-candidate-for-b1 :status :verified-mir-candidate-for-b1
                                 :input-kind :gravity/mir :requires-verifier-status :passed :mir-verifier-status :passed
                                 :binding {:mir-content-id (:mir-id artifact) :checked-core-artifact-id (:source-core mir)
                                           :independent-verifier-report-id (digest [:report independent])
                                           :independent-verifier-report-hash (digest independent)
                                           :pass-execution-record-id (get-in mir [:pass-execution-record :record-id])
                                           :scope-hash (digest (:scope mir))}
                                 :target-lowering-credit? false :backend-credit? false :llvm-credit? false}}
        lineage (fn [relative]
                  (let [text (slurp (path relative))]
                    {:source-content-hash (str "sha256:" (bootstrap/sha256-hex text))
                     :source-byte-count (alength (.getBytes text "UTF-8"))
                     :compiled-source-rule :not-established}))
        c11-lineage (lineage "bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity")
        c13-lineage (lineage "bootstrap/gravity/src/gravity/compiler/c13_mir_optimization_passes.gravity")
        b3-lineage (lineage "bootstrap/gravity/src/gravity/backend/b3_llvm_backend_design.gravity")
        dependencies {:source-core (get-in verifier [:b1-preflight :binding :checked-core-artifact-id])
                      :c11-source-rule c11-lineage :c11-pass (get-in verifier [:b1-preflight :binding :pass-execution-record-id])
                      :c13-artifact (:artifact-id optimized) :c13-source-rule c13-lineage :b3-source (:source-content-hash b3-lineage)}
        binding-of (fn [value] {:content-id (digest value) :entry-count (count value)})
        profile {:name :hosted :validated? true} target (bootstrap/p15-s23-c14-target-contract)
        abi (assoc (bootstrap/p15-s23-b3-llvm-expected-abi-contract) :return-type :i32)
        runtime (bootstrap/p15-s23-b3-llvm-expected-runtime-contract) providers (bootstrap/p15-s23-b3-llvm-expected-provider-contract)
        proofs {:capability {:content-id (digest {}) :entry-count 0 :proof-ids []}
                :certificates {:content-id (digest {}) :entry-count 0 :safety-proof-count 0 :safety-proof-ids []}}
        verifier-id (digest verifier) source-id (digest (:source-map mir))
        bindings (assoc (into {} (map (fn [[key value]] [key (binding-of value)])
                                     {:profile profile :target target :abi abi :runtime runtime :providers providers
                                      :effects {:direct #{} :order {}} :capabilities {:required #{} :granted #{} :proofs {}}
                                      :type (:type-table mir) :ownership (:ownership-table mir) :safety (:safety-table mir)
                                      :proofs proofs :source-map (:source-map mir) :dependencies dependencies
                                      :c13-optimization {:artifact (:artifact-id optimized) :semantic (:semantic-id optimized)
                                                         :decision (get-in record [:decision-record :decision-id]) :result :passed}}))
                        :c11-verifier {:content-id verifier-id :entry-count 1})
        policy {:request-id (digest [:request optimized]) :profile :hosted :profile-contract profile
                :target target :source-target-selection (bootstrap/p15-s23-b3-llvm-expected-source-target-selection)
                :abi abi :runtime runtime :providers providers :effects #{} :capabilities #{}
                :safety {:outcomes 3 :runtime-checks 0 :unsafe-islands 0 :binding (:safety bindings)}
                :proofs proofs :contract-bindings bindings :required-evidence (:required-evidence bootstrap/p15-s23-b3-llvm-policy)
                :source-map {:id source-id :preserved? true} :dependencies dependencies
                :target-policy (bootstrap/p15-s23-c14-target-policy) :proof-target-metadata {:entries [] :proofless-metadata-rejected? true}
                :unsupported-surface bootstrap/p15-s23-c14-llvm-unsupported-surface :c11-verifier verifier :c11-verifier-id verifier-id}]
    {:request (:request (bootstrap/p15-s23-c14-expected-record optimized policy)) :record record :mir mir}))

(deftest sh17-development-b1-llvm-accepts-matching-current-c14-optimization-statuses
  (let [invoke (fn [function arguments]
                 (bootstrap/p15-s23-stage2-runtime-execute-function
                  {:engine :gravity-sh17-development-b1 :compiler-artifact-plan? true} @development-b1-plan function arguments))
        scoped (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-function-scoped) original @scoped
        observe (fn [request]
                  (let [calls (atom [])
                        result (with-redefs-fn
                                 {scoped (fn [& args]
                                           (let [result (apply original args)]
                                             (when (and (identical? @development-b1-plan (nth args 1))
                                                        (= 'b1-c14-dependencies-valid? (nth args 2)))
                                               (swap! calls conj {:request (first (nth args 3)) :result result}))
                                             result))}
                                 #(invoke 'b1-bounded-c14-request-valid? [request]))]
                    {:result result :calls @calls}))]
    (doseq [fold? [false true]]
      (let [{:keys [request record mir]} (development-b1-request fold?) input (:input request)
            status (if fold? :constant-fold-pass-complete :identity-pass-complete)
            observed (observe request)]
        (is (= :accepted (:status record)))
        (is (= status (:optimization-status input) (:c13-optimization-status request)))
        (is (= (bootstrap/p15-s23-c11-mir-digest mir) (get-in input [:verifier-report :mir-id])))
        (is (= (get-in input [:verifier-report :mir-id])
               (get-in input [:verifier-report :b1-preflight :binding :mir-content-id])))
        (is (true? (invoke 'b1-c14-input-valid? [input])))
        ;; Actual file identities do not establish a compiled pinned cohort.
        ;; Observe ORIGINAL dependency rejection; never override its result.
        (is (false? (:result observed)))
        (is (= [{:request request :result false}] (:calls observed)))
        (is (= :not-established (get-in request [:dependency-provenance :c13-source-rule :compiled-source-rule])))
        (doseq [changed [(assoc input :optimization-status :unknown) (assoc input :optimization-status nil)
                         (assoc-in input [:optimization-report :artifact-id] (bootstrap/p15-s23-c11-mir-digest :other))
                         (assoc-in input [:optimization-report :semantic-id] (bootstrap/p15-s23-c11-mir-digest :other))
                         (assoc-in input [:optimization-report :decision-id] nil)
                         (assoc-in input [:optimization-report :verifier-result] :failed)
                         (assoc-in input [:verifier-report :status] :failed)]]
          (is (false? (invoke 'b1-c14-input-valid? [changed]))))
        (doseq [changed [(assoc request :c13-optimization-status (if fold? :identity-pass-complete :constant-fold-pass-complete))
                         (assoc-in request [:input :optimization-status] (if fold? :identity-pass-complete :constant-fold-pass-complete))
                         (assoc request :c13-optimization-status :unknown)
                         (assoc-in request [:input :optimization-status] :unknown)
                         (assoc-in request [:input :optimization-report :verifier-result] :failed)
                         (assoc-in request [:target :canonical-target] :wasm)
                         (assoc request :request-id nil)
                         (-> request (assoc :c13-optimization-status :unknown) (assoc-in [:input :optimization-status] :unknown))
                         (-> request (assoc :c13-optimization-status nil) (assoc-in [:input :optimization-status] nil))
                         (-> request (assoc :c13-optimization-status false) (assoc-in [:input :optimization-status] false))
                         (-> request (assoc :c13-optimization-status "identity-pass-complete")
                             (assoc-in [:input :optimization-status] "identity-pass-complete"))]]
          (let [observed (observe changed)]
            (is (false? (:result observed)))
            (is (= [] (:calls observed)))))))))

(deftest sh17-development-host-llvm-consumes-c13-folded-bools
  ;; Original pure host consumer parity on supplied DATA; no preflight,
  ;; sealed public artifact, tool invocation or native observation is claimed.
  (let [execute (fn [plan function arguments]
                  (bootstrap/p15-s23-stage2-runtime-execute-function
                   {:engine :gravity-sh17-development-host-llvm :compiler-artifact-plan? true} plan function arguments))]
    (doseq [diamond? [false true]
            [left right repeated?] [[2 2 false] [2 3 false]
                                    [-9223372036854775808 9223372036854775807 false]
                                    [9223372036854775807 0 true]]]
      (let [{:keys [mir evidence]} (if diamond?
                                    (development-diamond-input left right repeated? 42 7)
                                    (development-b3-comparison-input left right repeated?))
            folded (execute @development-c13-plan 'c13-build-bounded-identity-optimized-mir [mir evidence])
            output (:optimized-mir folded) function (get-in output [:functions 'main])
            order (bootstrap/p15-s23-b3-llvm-block-order output function)
            operations (bootstrap/p15-s23-b3-llvm-operation-sequence function order)
            labels (bootstrap/p15-s23-b3-llvm-block-labels order)
            positions (into {} (map-indexed (fn [index op] [(:op-id op) index]) operations))
            original-ops (bootstrap/p15-s23-b3-llvm-operation-sequence (get-in mir [:functions 'main]) order)
            comparison (first (filter #(= "comparison" (:op-id %)) operations))
            policy {:fact-bindings (get-in folded [:semantic-identity :fact-bindings])
                    :supported-operation-ids (get-in folded [:semantic-identity :operation-order])}
            lowering (execute @development-b3-plan 'b3-build-bounded-llvm-x86_64-linux [output])
            original-lowering (execute @development-b3-plan 'b3-build-bounded-llvm-x86_64-linux [mir])
            result-id (if diamond? "join-value" "comparison")]
        (is (= :accepted (:status folded)))
        (is (= folded (bootstrap/p15-s23-c13-expected-record mir evidence)))
        (is (true? (execute @c14-plan 'c14-equality-translation-valid? [folded policy])))
        (is (= mir (execute @c14-plan 'c14-equality-original-mir [folded])))
        (is (= :constructed-unverified (:status lowering)))
        (is (= :pending-host-content-binding (get-in lowering [:verified-input-closure :status])))
        (is (= (vec (repeat (count operations) nil))
               (mapv #(bootstrap/p15-s23-b3-llvm-operation-rejection operations % labels) operations)))
        (is (= (mapv :llvm-instruction (:operation-records lowering))
               (mapv #(bootstrap/p15-s23-b3-llvm-operation-line output % operations positions labels) operations)))
        (is (= (mapv :llvm-instruction (:operation-records original-lowering))
               (mapv #(bootstrap/p15-s23-b3-llvm-operation-line mir % original-ops positions labels) original-ops)))
        (is (= (:semantic-result lowering)
               (get (bootstrap/p15-s23-b3-llvm-evaluate-operations operations) result-id)
               (get (bootstrap/p15-s23-b3-llvm-evaluate-operations original-ops) result-id)))
        (is (= (:expected-exit-code lowering)
               (bootstrap/p15-s23-b3-llvm-scalar-exit-code
                (get (bootstrap/p15-s23-b3-llvm-evaluate-operations operations) result-id))))
        (doseq [candidate [(assoc comparison :source-operation :integer-lt)
                           (assoc comparison :type :gravity/integer)
                           (assoc comparison :operands ["left"])
                           (assoc comparison :operands nil)
                           (assoc comparison :operands '())
                           (assoc comparison :operands {})
                           (assoc comparison :operands 7)
                           (assoc comparison :constant-payload nil)
                           (assoc comparison :constant-payload 7)
                           (assoc comparison :constant-payload [])
                           (assoc comparison :constant-payload {:present? true :value nil})
                           (assoc comparison :constant-payload {:present? true :value 1})
                           (assoc comparison :constant-payload {:present? true})
                           (assoc comparison :constant-payload {:present? false :value true})
                           (assoc comparison :constant-payload {:present? true :value true :extra 0})]]
          (is (= :unsupported-constant-source-operation
                 (bootstrap/p15-s23-b3-llvm-operation-rejection operations candidate labels))))
        (doseq [[candidate reason] [[(assoc comparison :effects #{:io/write} :constant-payload 7) :program-effects-unsupported]
                                    [(assoc comparison :capabilities #{:io/stdout} :operands 7) :program-capabilities-unsupported]
                                    [(assoc comparison :opcode :not-supported :constant-payload nil) :unsupported-mir-opcode]]]
          (is (= reason (bootstrap/p15-s23-b3-llvm-operation-rejection operations candidate labels))))
        ;; Legacy source branches are supplied controls, not a rewrite of the
        ;; actual C13 output's retained comparison ancestry.
        (doseq [source-operation [:literal :implicit-nil]]
          (is (nil? (bootstrap/p15-s23-b3-llvm-operation-rejection
                     operations (assoc comparison :source-operation source-operation :operands nil) labels)))
          (is (= :bounded-scalar-constant-payload
                 (bootstrap/p15-s23-b3-llvm-operation-rejection
                  operations (assoc comparison :source-operation source-operation :constant-payload {:present? true :value 1}) labels))))))))

(def ^:private c13-envelope-keys
  ;; This set is copied from C14's c14-c13-envelope-valid? contract.  Keep it
  ;; explicit: C14 source is authoritative for the C13 carrier envelope.
  #{:artifact :schema-version :status :input :optimized-mir
    :pass-contract :decision-record :invalidation-ledger
    :residual-check-report :verifier-replay :semantic-identity
    :target-instruction-selection :diagnostics :semantic-authority
    :execution-tcb :clojure-seed-boundary? :self-hosted?
    :source-rule :actual-path-provenance :semantic-id :artifact-id
    :actual-path-binding-id})

(def ^:private seal-keys
  #{:source-rule :actual-path-provenance :semantic-id :artifact-id
    :actual-path-binding-id})

(def ^:private targets
  ;; W1 closes one exact target. Other backend families remain distinct and
  ;; cannot serve as evidence for this Linux LLVM carrier.
  [:llvm])

(def ^:private canonical-target :llvm-x86_64-linux)

(def ^:private packet-builders
  {:llvm bootstrap/p15-s23-stage2-c13-c14-b1-packet-from-c11!})

(def ^:private packet-verifiers
  {:llvm bootstrap/p15-s23-stage2-c13-c14-b1-verification-report})

(def ^:private c14-builder-functions
  {:llvm 'c14-build-bounded-llvm-lowering-record})

(def ^:private c14-policy-keys
  ;; C14's three public policy constructors intentionally share one exact
  ;; envelope. The target policy narrows the values in that envelope.
  #{:expected-c11-artifact-id :expected-c13-artifact-id
    :profile :profile-contract :target :source-target-selection
    :abi :runtime :providers :effects :capabilities :safety :proofs
    :contract-bindings :required-evidence :source-map
    :expected-source-map-binding :dependencies :target-policy
    :unsupported-surface :proof-target-metadata :fact-bindings
    :supported-operation-ids :c11-verifier :c11-verifier-id :request-id})

;; These nested keysets are part of the authenticated carrier schema, rather
;; than incidental implementation detail.  Keeping them here makes the
;; positive test prove that C13 emitted the complete replay/closure record and
;; makes every extra or missing field a fail-closed regression.
(def ^:private c13-input-keys
  #{:kind :c11-artifact-id :c11-mir-id :module-id :source-core
    :verifier-report-id :verifier-status :semantic-replay-parity
    :pass-execution-record-id})

(def ^:private c13-pass-contract-keys
  #{:pass-id :family :version :input-ir :output-ir :required-analyses
    :preconditions :preserves :invalidates :regenerates :proof-obligations
    :profile-constraints :target-assumptions :effect-ordering-policy
    :safety-policy :domain-policy :emits})

(def ^:private c13-decision-record-keys
  #{:artifact :pass-id :decision-id :input-mir :output-mir :decision
    :changed? :changed-operations :reason :preserved :invalidated
    :proofs-used :residual-checks :source-map :verifier-result})

(def ^:private c13-invalidation-ledger-keys
  #{:artifact :pass-id :decision-id :input-mir :output-mir
    :analysis-invalidated :facts-invalidated :facts-regenerated
    :proofs-invalidated :certificates-invalidated :runtime-checks-restored
    :passes-to-rerun :caches-cleared :diagnostics-affected :profile :target})

(def ^:private c13-residual-check-keys
  #{:artifact :status :retained-runtime-checks :elided-runtime-checks
    :open-proof-obligations})

(def ^:private c13-verifier-replay-keys
  #{:artifact :required? :c11-artifact-id :verifier-report-id :c11-mir-id
    :expected-input-module-id :expected-output-module-id :fact-bindings
    :semantic-identity-required? :result})

(def ^:private c13-semantic-identity-keys
  #{:c11-input-mir-id :c11-output-mir-id :input-module-id
    :output-module-id :fact-bindings :operation-order :operation-count
    :effect-order-graph :unchanged?})

(def ^:private c13-source-rule-keys
  #{:artifact :owner :source-content-hash :source-byte-count
    :plan-semantic-hash :functions-semantic-hash :builder-function
    :builder-semantic-hash :function-shapes :semantic-authority
    :compiled-by :executed-by :self-hosted?})

(def ^:private c13-path-provenance-keys
  #{:source :c11-source :c13-source})

(def ^:private c11-report-keys
  #{:artifact :status :mir-id :checked-core-artifact-id
    :checked-core-ingress :checked-core-request-binding-provenance
    :source-rule :semantic-replay-parity :invocation-audit
    :execution-count :live-invocation-claim? :execution-tcb
    :independent-verifier :b1-preflight :actual-path-context})

(def ^:private final-envelope-missing-facts
  {:llvm :bounded-c13-c14-b1-final-envelope})

(def ^:private final-identity-missing-facts
  {:llvm :recomputable-c13-c14-b1-final-identities})

(def ^:private contextual-reconstruction-missing-facts
  {:llvm :contextual-fresh-c13-c14-b1-reconstruction})

(def ^:private source-values
  {:llvm "42"})

(defn- closed-pure-source
  ([backend]
   (closed-pure-source backend (get source-values backend)))
  ([backend value]
   (str
    "(ns checked.sh17 (:profile :hosted) (:target :jvm) "
    "(:safety :safe) (:effects #{}) (:capabilities #{}) (:exports [main]))\n"
    "(defn main [] " value ")\n")))

(defn- build-upstream
  ([backend source-path]
   (build-upstream backend source-path (closed-pure-source backend)))
  ([backend source-path source]
    (binding [bootstrap/*p15-s23-c11-mir-diagnostic-context*
              {:requested-target canonical-target}
              bootstrap/*additional-bootstrap-targets*
              bootstrap/stage2-runtime-derived-source-targets]
      (let [context
            (bootstrap/p15-s23-stage2-gravity-checked-core-context
             source-path source canonical-target)
            checked-core
            (bootstrap/p15-s23-stage2-gravity-checked-core-source-artifact
             context)
            c11
            (bootstrap/p15-s23-stage2-c11-mir-artifact
             checked-core context)
            c11-report
            (bootstrap/p15-s23-stage2-c11-mir-verification-report
             c11 checked-core context)
            packet
            ((get packet-builders backend) c11 checked-core context)
            c13 (:c13 packet)
            policy
            (bootstrap/p15-s23-c14-policy
             c11 checked-core c11-report c13 (:source-rule c13))
            raw-c14
            (bootstrap/p15-s23-stage2-runtime-execute-function
             {:engine :gravity-sh17-c13-optimized-mir-carrier-test
              :compiler-artifact-plan? true}
             @c14-plan (get c14-builder-functions backend)
             [c13 policy])
            report ((get packet-verifiers backend) packet checked-core context)]
        {:backend backend :source source :source-path source-path
         :context context :checked-core checked-core :c11 c11
         :c11-report c11-report :packet packet :c13 c13
         :c14 (:c14 packet) :b1 (:b1 packet)
         :policy policy :raw-c14 raw-c14 :report report}))))

(def ^:private prepared
  (delay
    (into {}
          (map (fn [backend]
                 [backend
                  (build-upstream
                   backend
                   (str "sh17-c13-carrier-" (name backend) ".gravity"))])
               targets))))

(defn- carrier
  [backend]
  (get @prepared backend))

(def ^:private alternate-linux-carrier
  (delay
    (build-upstream
     :llvm "sh17-c13-carrier-linux-alternate.gravity"
     (closed-pure-source :llvm "43"))))

(defn- raw-c14-from-packet
  [packet]
  (apply dissoc (:c14 packet) seal-keys))

(defn- invoke-c14
  [backend optimized policy]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh17-c13-optimized-mir-carrier-test
    :compiler-artifact-plan? true}
   @c14-plan (get c14-builder-functions backend) [optimized policy]))

(defn- packet-diagnostic
  [backend packet checked-core context]
  ;; The public verifier intentionally throws ExceptionInfo for a rejected
  ;; packet.  Catch only that owned diagnostic type; a host error must fail the
  ;; test rather than being mistaken for a rejection.
  (try
    {:unexpected ((get packet-verifiers backend)
                  packet checked-core context)}
    (catch clojure.lang.ExceptionInfo exception
      (ex-data exception))))

(defn- rehash-packet
  [packet]
  (let [semantic-id (bootstrap/p15-s23-c13-c14-b1-semantic-id packet)
        artifact-id
        (bootstrap/p15-s23-c11-mir-digest
         {:kind (:kind packet) :schema-version (:schema-version packet)
          :semantic-id semantic-id})
        path-binding-id
        (bootstrap/p15-s23-c13-c14-b1-actual-path-binding-id
         semantic-id (:actual-path-provenance packet))]
    (assoc packet :semantic-id semantic-id :artifact-id artifact-id
           :actual-path-binding-id path-binding-id)))

(defn- maps-in
  [value]
  (tree-seq coll?
           (fn [item]
             (if (map? item)
               (mapcat identity item)
               item))
           value))

(defn- assert-claim-boundary
  [value]
  (doseq [item (filter map? (maps-in value))]
    (when (contains? item :clojure-seed-boundary?)
      (is (true? (:clojure-seed-boundary? item)) item))
    (doseq [claim [:self-hosted? :whole-c13? :whole-c14? :whole-b1?
                   :whole-b2? :whole-b3? :whole-b4? :public? :release?]]
      (when (contains? item claim)
        (is (false? (get item claim)) [claim item])))
    (when (contains? item :semantic-authority)
      (is (= :gravity-source (:semantic-authority item)) item))
    (is (not (contains? item :authority)) item)
    (is (not (contains? item :unsupported-authority)) item)
    (is (not (contains? item :ambient-authority)) item)))

(defn- assert-fresh-c11-report
  [{:keys [c11 checked-core c11-report context]}]
  (is (= c11-report-keys (set (keys c11-report))))
  (is (= :gravity/c11-mir-verification-report (:artifact c11-report)))
  (is (= :passed (:status c11-report)))
  (is (= (:mir-id c11) (:mir-id c11-report)))
  (is (= (:artifact-id checked-core)
         (:checked-core-artifact-id c11-report)))
  (is (= :passed (:semantic-replay-parity c11-report)))
  (is (= :clojure-stage0-rule-runner (:execution-tcb c11-report)))
  (is (= :not-available (:invocation-audit c11-report)))
  (is (= :not-claimed (:execution-count c11-report)))
  (is (false? (:live-invocation-claim? c11-report)))
  (is (= (:source-path context)
         (get-in c11-report [:actual-path-context :source])))
  (is (= :passed
         (get-in c11-report [:independent-verifier :status])))
  (is (true?
       (get-in c11-report [:independent-verifier :module-shape-valid?])))
  (is (true?
       (get-in c11-report [:independent-verifier :type-facts-valid?])))
  (is (true?
       (get-in c11-report [:independent-verifier :target-independent?]))))

(defn- assert-c13-carrier
  [{:keys [source-path c11 c13]}]
  (let [mir (:mir-module c11)
        source-rule (:source-rule c13)
        input (:input c13)
        pass-contract (:pass-contract c13)
        decision-record (:decision-record c13)
        invalidation-ledger (:invalidation-ledger c13)
        residual-check-report (:residual-check-report c13)
        verifier-replay (:verifier-replay c13)
        semantic-identity (:semantic-identity c13)
        actual-path-provenance (:actual-path-provenance c13)
        fact-bindings
        {:type
         (bootstrap/p15-s23-c13-c14-b1-content-binding (:type-table mir))
         :effect
         (bootstrap/p15-s23-c13-c14-b1-content-binding (:effect-table mir))
         :ownership
         (bootstrap/p15-s23-c13-c14-b1-content-binding (:ownership-table mir))
         :capability
         (bootstrap/p15-s23-c13-c14-b1-content-binding
          (:capability-table mir))
         :safety
         (bootstrap/p15-s23-c13-c14-b1-content-binding (:safety-table mir))
         :runtime-checks
         (bootstrap/p15-s23-c13-c14-b1-content-binding
          (:runtime-check-table mir))
         :proofs
         (bootstrap/p15-s23-c13-c14-b1-content-binding
          (:proof-certificate-table mir))
         :source-map
         (bootstrap/p15-s23-c13-c14-b1-content-binding (:source-map mir))}
        operation-order
        (mapv :op-id
              (bootstrap/p15-s23-c11-mir-operation-sequence mir))
        semantic-id
        (bootstrap/p15-s23-c11-mir-digest
         {:kind (:artifact c13)
          :record
          (bootstrap/p15-s23-c13-c14-b1-stage-semantic-input c13)})
        artifact-id
        (bootstrap/p15-s23-c11-mir-digest
         {:kind (:artifact c13) :schema-version (:schema-version c13)
          :semantic-id semantic-id})]
    (is (= 22 (count c13-envelope-keys)))
    (is (= c13-envelope-keys (set (keys c13))))
    (is (= c13-input-keys (set (keys input))))
    (is (= c13-pass-contract-keys (set (keys pass-contract))))
    (is (= c13-decision-record-keys (set (keys decision-record))))
    (is (= c13-invalidation-ledger-keys
           (set (keys invalidation-ledger))))
    (is (= c13-residual-check-keys
           (set (keys residual-check-report))))
    (is (= c13-verifier-replay-keys
           (set (keys verifier-replay))))
    (is (= c13-semantic-identity-keys
           (set (keys semantic-identity))))
    (is (= c13-source-rule-keys
           (set (keys source-rule))))
    (is (= c13-path-provenance-keys
           (set (keys actual-path-provenance))))
    (is (= 1 (:schema-version c13)))
    (is (= :accepted (:status c13)))
    (is (= :gravity-source (:semantic-authority c13)))
    (is (= :clojure-stage0-rule-runner (:execution-tcb c13)))
    (is (true? (:clojure-seed-boundary? c13)))
    (is (false? (:self-hosted? c13)))
    (is (= :forbidden (:target-instruction-selection c13)))
    (is (= [] (:diagnostics c13)))
    (is (= (:mir-id c11) (:c11-mir-id input)))
    (is (= (:artifact-id c11) (:c11-artifact-id input)))
    (is (= (:module-id mir) (:module-id input)))
    (is (= (:source-core mir) (:source-core input)))
    (is (= :passed (:verifier-status input)))
    (is (= :passed (:semantic-replay-parity input)))
    (is (= :hosted (first (:profile-constraints pass-contract))))
    (is (= :c13-bounded-identity (:pass-id pass-contract)))
    (is (= 1 (:version pass-contract)))
    (is (= :retain-input-unchanged (:decision decision-record)))
    (is (false? (:changed? decision-record)))
    (is (= :passed (:verifier-result decision-record)))
    (is (= :complete (:status residual-check-report)))
    (is (= true (:required? verifier-replay)))
    (is (= true (:semantic-identity-required? verifier-replay)))
    (is (= :passed (:result verifier-replay)))
    (is (= true (:unchanged? semantic-identity)))
    (is (= :hosted (:profile invalidation-ledger)))
    ;; C13 itself remains target-independent, but its request is required to
    ;; carry the one canonical Linux identity selected at checked-core ingress.
    (is (= canonical-target (:target invalidation-ledger)))
    (is (= (:mir-id c11) (:input-mir decision-record)))
    (is (= (:mir-id c11) (:output-mir decision-record)))
    (is (= (:mir-id c11) (:c11-mir-id verifier-replay)))
    (is (= (:mir-id c11) (:c11-input-mir-id semantic-identity)))
    (is (= (:mir-id c11) (:c11-output-mir-id semantic-identity)))
    (is (= (:mir-module c11) (:optimized-mir c13)))
    (is (= fact-bindings
           (:fact-bindings semantic-identity)
           (get-in decision-record [:preserved :fact-bindings])
           (:fact-bindings verifier-replay)))
    (is (= operation-order
           (:operation-order semantic-identity)
           (get-in decision-record [:preserved :operation-order])))
    (is (= (:effect-order-graph mir)
           (:effect-order-graph semantic-identity)
           (get-in decision-record [:preserved :effect-order-graph])))
    (is (= (:runtime-check-table mir)
           (:retained-runtime-checks residual-check-report)
           (:residual-checks decision-record)))
    (is (= [] (:elided-runtime-checks residual-check-report)))
    (is (= [] (:open-proof-obligations residual-check-report)))
    (is (= [] (:facts-invalidated invalidation-ledger)))
    (is (= [] (:analysis-invalidated invalidation-ledger)))
    (is (= [] (:proofs-invalidated invalidation-ledger)))
    (is (= semantic-id (:semantic-id c13)))
    (is (= artifact-id (:artifact-id c13)))
    (is (= (:actual-path-binding-id c13)
           (bootstrap/p15-s23-c13-c14-b1-actual-path-binding-id
            (:semantic-id c13) (:actual-path-provenance c13))))
    (is (= :gravity/pinned-gravity-source-rule (:artifact source-rule)))
    (is (= :gravity.compiler/c13-mir-optimization (:owner source-rule)))
    (is (= bootstrap/p15-s23-c13-source-byte-count
           (:source-byte-count source-rule)))
    (is (= bootstrap/p15-s23-c13-expected-source-content-hash
           (:source-content-hash source-rule)))
    (is (= bootstrap/p15-s23-c13-expected-plan-semantic-hash
           (:plan-semantic-hash source-rule)))
    (is (= bootstrap/p15-s23-c13-expected-functions-semantic-hash
           (:functions-semantic-hash source-rule)))
    (is (= bootstrap/p15-s23-c13-builder-function
           (:builder-function source-rule)))
    (is (= bootstrap/p15-s23-c13-expected-builder-semantic-hash
           (:builder-semantic-hash source-rule)))
    (is (= bootstrap/p15-s23-c13-required-functions
           (:function-shapes source-rule)))
    (is (= :gravity-source (:semantic-authority source-rule)))
    (is (= false (:self-hosted? source-rule)))
    (is (= source-path (:source actual-path-provenance)))
    (is (= (get-in c11 [:provenance :actual-paths :c11-source])
           (:c11-source actual-path-provenance)))
    (is (string? (:c13-source actual-path-provenance)))
    (is (= canonical-target
           (:target (get-in c13 [:invalidation-ledger]))))))

(defn- assert-c14-raw
  [backend {:keys [packet c13 policy raw-c14 c11]}]
  (let [expected (raw-c14-from-packet packet)
        request (:request raw-c14)
        expected-backend :gravity.backend/llvm]
    (is (= :llvm backend))
    (is (= c14-policy-keys (set (keys policy))))
    (is (= expected raw-c14))
    (is (= :accepted (:status raw-c14)))
    (is (= :gravity/c14-bounded-llvm-lowering-record
           (:artifact raw-c14)))
    (is (= :accepted (:status request)))
    (is (= expected-backend (get-in raw-c14 [:eligibility :backend])))
    (is (= expected-backend (get-in request [:target :backend])))
    (is (= (:artifact-id c13) (get-in request [:input :artifact-id])))
    (is (= (:semantic-id c13) (get-in request [:input :id])))
    (is (= (:artifact-id c13) (get-in request [:input :optimization-report :artifact-id])))
    (is (= (:request-id policy) (:request-id request)))
    (is (= :hosted (:profile policy) (:profile request)))
    (is (= (bootstrap/p15-s23-b3-llvm-expected-profile-contract)
           (:profile-contract policy)
           (:profile-contract request)))
    (is (= (:target policy) (:target request) (get-in raw-c14 [:eligibility :target])))
    (is (= canonical-target
           (get-in policy [:target :canonical-target])
           (get-in request [:target :canonical-target])
           (get-in raw-c14 [:eligibility :target :canonical-target])))
    (is (= :llvm (get-in policy [:target :request])))
    (is (= :llvm (get-in request [:target :request])))
    (is (= "x86_64-unknown-linux-gnu"
           (get-in policy [:target :triple])
           (get-in request [:target :triple])))
    (is (= :elf (get-in policy [:target :object-format])))
    (is (= :x86_64 (get-in policy [:target :architecture])))
    (is (= (assoc (bootstrap/p15-s23-b3-llvm-expected-abi-contract)
                  :return-type :i32)
           (:abi policy)
           (:abi request)
           (get-in raw-c14 [:abi-layout :abi])))
    (is (= (bootstrap/p15-s23-b3-llvm-expected-runtime-contract)
           (:runtime policy)
           (:runtime request)
           (get-in raw-c14 [:runtime-provider :runtime])))
    (is (= (bootstrap/p15-s23-b3-llvm-expected-provider-contract)
           (:providers policy)
           (:providers request)
           (get-in raw-c14 [:runtime-provider :providers])))
    (is (= (:providers policy) (:providers request)
           (get-in raw-c14 [:eligibility :required-providers])))
    (is (= (:effects policy) (:effects request)))
    (is (= (:capabilities policy) (:capabilities request)))
    (is (= (:safety policy) (:safety request)))
    (is (= (:source-map policy) (:source-map request)))
    (is (= (:dependencies policy)
           (:dependency-provenance raw-c14)))
    (is (= :gravity-source (:semantic-authority raw-c14)))
    (is (= [] (:diagnostics raw-c14)))
    (is (not (contains? raw-c14 :authority)))
    (is (not (contains? request :authority)))
    (let [c11-link
          (or (get-in request [:input :c11-artifact-id])
              (get-in raw-c14 [:bounded-lowering-payload :c11-artifact-id]))]
      (if c11-link
        (is (= (:artifact-id c11) c11-link))
        ;; The LLVM C14 request intentionally carries the C11 id through the
        ;; C13 input record rather than repeating it in the request input.
        (is (= (:artifact-id c11)
               (get-in c13 [:input :c11-artifact-id])))))))

(defn- assert-b1-carrier
  [{:keys [c13 c14 b1 policy]}]
  (let [target (:target policy)
        expected-abi (assoc
                      (bootstrap/p15-s23-b3-llvm-expected-abi-contract)
                      :return-type :i32)]
    (is (= :gravity/b1-verified-backend-input-packet (:artifact b1)))
    (is (= 1 (:schema-version b1)))
    (is (= :accepted-for-bounded-llvm (:status b1)))
    (is (= (:artifact-id c13) (get-in b1 [:input :artifact-id])))
    (is (= target (:target b1)
           (get-in b1 [:eligibility :target])
           (get-in b1 [:c14-eligibility :target])))
    (is (= :llvm-x86_64-linux (:canonical-target target)))
    (is (= "x86_64-unknown-linux-gnu" (:triple target)))
    (is (= :elf (:object-format target)))
    (is (= :x86_64 (:architecture target)))
    (is (= expected-abi (:abi b1)))
    (is (= (:runtime policy) (:runtime b1)))
    (is (= (:providers policy) (:providers b1)))
    (is (= #{} (:effects b1)))
    (is (= #{} (:capabilities b1)))
    (is (= (:safety policy) (:safety b1)))
    (is (= [:llvm-ir :elf-x86_64-object :elf-x86_64-executable]
           (get-in b1 [:backend-manifest :emits])))
    (is (= (get-in c14 [:request :request-id])
           (get-in b1 [:backend-manifest :c14-request-id])))
    (is (= (:artifact-id c14)
           (get-in b1 [:backend-manifest :c14-artifact-id])))
    (is (= :gravity-source (:semantic-authority b1)))
    (is (= :clojure-stage0-rule-runner (:execution-tcb b1)))
    (is (true? (:clojure-seed-boundary? b1)))
    (is (false? (:self-hosted? b1)))
    (is (= [] (:diagnostics b1)))
    (is (not (contains? b1 :authority)))
    (is (not (contains? b1 :public?)))))

(defn- assert-contextual-report
  [{:keys [packet c11 report]}]
  (is (= :gravity/c13-c14-b1-contextual-verification-report
         (:artifact report)))
  (is (= 1 (:schema-version report)))
  (is (= :passed (:status report)))
  (is (= [:passed :passed :passed :passed]
         ((juxt :c11 :c13 :c14 :b1) report)))
  (is (= (:artifact-id packet) (:packet-id report)))
  (is (= (:semantic-id packet) (:semantic-id report)))
  (is (= (:mir-id c11) (:fresh-c11-mir-id report)))
  (is (= (get-in packet [:c13 :artifact-id]) (:c13-artifact-id report)))
  (is (= (get-in packet [:c14 :request :request-id])
         (:c14-request-id report)))
  (is (= (get-in packet [:c14 :artifact-id]) (:c14-artifact-id report)))
  (is (= (get-in packet [:b1 :artifact-id]) (:b1-artifact-id report)))
  (is (= :passed (:gravity-source-replay report)))
  (is (= :passed (:independent-reconstruction report)))
  (is (false? (:self-hosted? report)))
  (is (= (:report-id report)
         (bootstrap/p15-s23-c11-mir-digest
         {:kind :gravity/c13-c14-b1-contextual-verification-report
           :schema-version 1
           :report (dissoc report :report-id)}))))

(defn- assert-packet-envelope
  [backend {:keys [packet c11 checked-core]}]
  (is (= :llvm backend))
  (is (= bootstrap/p15-s23-c13-c14-b1-final-packet-keys
         (set (keys packet))))
  (is (= :gravity/p15-s23-c13-c14-b1-authenticated-packet
         (:kind packet)))
  (is (= :accepted-for-bounded-llvm (:status packet)))
  (is (= :gravity-source (:semantic-authority packet)))
  (is (= :clojure-stage0-independent-reconstruction
         (:verification-tcb packet)))
  (is (= (:artifact-id c11) (get-in packet [:c11 :artifact-id])))
  (is (= (:mir-id c11) (get-in packet [:c11 :mir-id])))
  (is (= (:artifact-id checked-core)
         (get-in packet [:c11 :checked-core-artifact-id])))
  (is (= [] (:diagnostics packet)))
  (is (= bootstrap/p15-s23-c13-c14-b1-final-packet-scope
         (:scope packet)))
  (is (= :passed (get-in packet [:c11 :verifier-record :status])))
  (is (= :passed (get-in packet [:c11 :verifier-record :semantic-replay-parity])))
  (is (= :clojure-stage0-rule-runner
         (get-in packet [:c11 :verifier-record :execution-tcb])))
  (is (not (contains? (get-in packet [:c11 :verifier-record])
                      :live-invocation-claim?))))

(deftest sh17-c13-optimized-mir-carrier-is-authenticated-for-linux-llvm
  (let [rows (mapv (fn [backend] [backend (carrier backend)]) targets)]
    (is (= #{:gravity/c13-bounded-identity-optimized-mir}
           (set (map (comp :artifact :c13) (map second rows)))))
    (doseq [[backend row] rows]
      (testing (name backend)
        (assert-packet-envelope backend row)
        (assert-fresh-c11-report row)
        (assert-c13-carrier row)
        (assert-c14-raw backend row)
        (assert-b1-carrier row)
        (assert-contextual-report row)
        (assert-claim-boundary row)
        (is (= (:raw-c14 row)
               (invoke-c14 backend (:c13 row) (:policy row))))
        (is (true?
             (bootstrap/p15-s23-stage2-c13-c14-b1-authentic?
              (:packet row) (:checked-core row) (:context row))))))))

(deftest sh17-c13-carrier-identities-ignore-actual-checkout-paths
  (let [left (build-upstream :llvm "sh17-c13-carrier-left.gravity")
        right (build-upstream :llvm "sh17-c13-carrier-right.gravity")
        left-c13 (:c13 left) right-c13 (:c13 right)
        left-packet (:packet left) right-packet (:packet right)]
    (is (= (:source left) (:source right)))
    (is (= (:semantic-id left-c13) (:semantic-id right-c13)))
    (is (= (:artifact-id left-c13) (:artifact-id right-c13)))
    (is (= (:semantic-id left-packet) (:semantic-id right-packet)))
    (is (= (:artifact-id left-packet) (:artifact-id right-packet)))
    (is (not= (get-in left-c13 [:actual-path-provenance :source])
              (get-in right-c13 [:actual-path-provenance :source])))
    (is (not= (:actual-path-binding-id left-c13)
              (:actual-path-binding-id right-c13)))
    (is (not= (:actual-path-binding-id left-packet)
              (:actual-path-binding-id right-packet)))
    (is (not (contains?
              (bootstrap/p15-s23-c13-c14-b1-stage-semantic-input left-c13)
              :actual-path-provenance)))
    (assert-claim-boundary left)
    (assert-claim-boundary right)))

(defn- c14-rejection
  [backend optimized policy]
  (invoke-c14 backend optimized policy))

(defn- assert-c14-rejection
  [result diagnostic missing-fact]
  (is (= [:rejected diagnostic missing-fact
          [diagnostic]]
         ((juxt :status :diagnostic :missing-fact :diagnostics) result))
      result)
  (is (not (contains? result :authority))))

(defn- assert-c14-input-rejection
  [result]
  (assert-c14-rejection result "C14-INPUT" :verified-optimized-mir))

(deftest sh17-c13-c14-carrier-mutations-fail-closed
  (doseq [backend targets]
    (let [{:keys [c13 policy]} (carrier backend)
          hostile-c13
          [[:key-set-injection (assoc c13 :hostile-key true)]
           [:key-set-removal (dissoc c13 :optimized-mir)]
           [:artifact (assoc c13 :artifact :gravity/substituted)]
           [:status (assoc c13 :status :rejected)]
           [:authority (assoc c13 :semantic-authority :untrusted)]
           [:source-rule (assoc-in c13 [:source-rule :owner]
                                   :gravity/attacker)]
           [:source-identity (assoc-in c13 [:source-rule :source-content-hash]
                                       "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")]
           [:pass-contract (assoc-in c13 [:pass-contract :family] :substituted)]
           [:artifact-id (assoc c13 :artifact-id
                                "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")]
           [:c11-verifier (assoc-in c13 [:verifier-replay :c11-mir-id]
                                    "sha256:dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd")]
           [:c11-replay (assoc-in c13 [:verifier-replay :result] :rejected)]
           [:fact-bindings (assoc-in c13 [:semantic-identity :fact-bindings :type :content-id]
                                     "sha256:eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee")]
           [:runtime-checks (assoc-in c13 [:residual-check-report :retained-runtime-checks]
                                      [{:hostile true}])]
           [:residual-status (assoc-in c13 [:residual-check-report :status]
                                       :rejected)]
           [:elided-runtime-checks
            (assoc-in c13 [:residual-check-report :elided-runtime-checks]
                      [:hostile-check])]
           [:effect-order (assoc-in c13 [:semantic-identity :effect-order-graph]
                                    {:hostile true})]
           [:operation-order (update-in c13 [:semantic-identity :operation-order]
                                        #(conj % "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"))]
           [:mir-substitution (assoc c13 :optimized-mir
                                     (assoc (:optimized-mir c13)
                                            :module-id "sha256:abababababababababababababababababababababababababababababababab"))]
           [:decision (assoc-in c13 [:decision-record :output-mir]
                                "sha256:1212121212121212121212121212121212121212121212121212121212121212")]
           [:ledger (assoc-in c13 [:invalidation-ledger :facts-invalidated]
                              [:hostile-fact])]
           [:ledger-analysis
            (assoc-in c13 [:invalidation-ledger :analysis-invalidated]
                      [:hostile-analysis])]
           [:ledger-target (assoc-in c13 [:invalidation-ledger :target]
                                     :hostile-target)]
           [:replay-input-module
            (assoc-in c13 [:verifier-replay :expected-input-module-id]
                      "sha256:3434343434343434343434343434343434343434343434343434343434343434")]]]
      (testing (str (name backend) " C13 mutation")
        (doseq [[label hostile] hostile-c13]
          (testing (name label)
            (assert-c14-input-rejection
             (c14-rejection backend hostile policy)))))
      (let [hostile-policies
            [[:policy-key-injection (assoc policy :hostile-key true)]
             [:policy-key-removal (dissoc policy :request-id)]
             [:policy-c11-id (assoc policy :expected-c11-artifact-id
                                    "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")]
             [:policy-c13-id (assoc policy :expected-c13-artifact-id
                                    "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb")]
             [:policy-target (assoc-in policy [:target :canonical-target]
                                       :hostile-non-linux-target)]
             [:policy-triple (assoc-in policy [:target :triple]
                                       "hostile-unsupported-triple")]
             [:policy-profile (assoc policy :profile :native)]
             [:policy-abi (assoc-in policy [:abi :architecture] :hostile-arch)]
             [:policy-runtime (assoc-in policy [:runtime :platform-runtime-providers]
                                        [:unsupported/process-startup])]
             [:policy-providers (assoc-in policy [:providers :platform]
                                          [:unsupported/process-startup])]
             [:policy-effects (assoc policy :effects #{:io})]
             [:policy-capabilities (assoc policy :capabilities #{:filesystem})]
             [:policy-safety (assoc-in policy [:safety :unsafe-islands] 1)]
             [:policy-source-map (assoc-in policy [:source-map :preserved?] false)]
             [:policy-target-policy (assoc-in policy [:target-policy :public?] true)]
             [:policy-c11-verifier-report
              (assoc-in policy [:c11-verifier :status] :rejected)]
             [:policy-facts (assoc-in policy [:fact-bindings :type :content-id]
                                      "sha256:cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc")]
             [:policy-proofs (assoc-in policy [:proofs :capability :content-id]
                                       "sha256:dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd")]
             [:policy-dependencies (assoc-in policy [:dependencies :source-core]
                                             "sha256:eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee")]
             ]]
        (doseq [[label hostile-policy] hostile-policies]
          (testing (str (name backend) " " (name label))
            (let [result (c14-rejection backend c13 hostile-policy)
                  ;; Policy tampering reaches the C14 policy/surface gate,
                  ;; whereas the two expected-id substitutions fail at the
                  ;; authenticated optimized-MIR ingress.  Keep those
                  ;; diagnostics distinct and stable.
                  [diagnostic missing-fact]
                  (cond
                    (= :policy-profile label)
                    ["C14-PROFILE" :hosted-profile-eligibility]

                    (contains? #{:policy-c11-id :policy-c13-id
                                 :policy-facts} label)
                    ["C14-INPUT" :verified-optimized-mir]

                    (contains? #{:policy-key-injection :policy-key-removal
                                 :policy-target :policy-triple :policy-abi
                                 :policy-runtime :policy-providers
                                 :policy-effects :policy-capabilities
                                 :policy-safety :policy-source-map
                                 :policy-target-policy
                                 :policy-c11-verifier-report
                                 :policy-proofs
                                 :policy-dependencies} label)
                    ["C14-TARGET" :explicit-bounded-llvm-target]

                    :else
                    ["C14-UNSUPPORTED" :bounded-llvm-mir-surface])]
              (assert-c14-rejection result diagnostic missing-fact))))))))

(deftest sh17-full-packet-stage-substitution-and-context-replay-fail-closed
  (doseq [backend targets]
    (let [{:keys [packet checked-core context]} (carrier backend)
          other-packet (:packet @alternate-linux-carrier)
          preflight-cases
          [[:packet-key-set-injection (assoc packet :hostile-key true)
            "B1-METADATA"
            (get final-envelope-missing-facts backend)]
           [:packet-key-set-removal (dissoc packet :c13)
            "B1-METADATA"
            (get final-envelope-missing-facts backend)]
           [:packet-artifact (assoc packet :kind :gravity/substituted)
            "B1-METADATA"
            (get final-envelope-missing-facts backend)]
           [:packet-status (assoc packet :status :rejected)
            "B1-METADATA"
            (get final-envelope-missing-facts backend)]
           [:packet-authority (assoc packet :semantic-authority :untrusted)
            "B1-METADATA"
            (get final-envelope-missing-facts backend)]
           [:packet-semantic-id (assoc packet :semantic-id
                                       "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
            "B1-METADATA"
            (get final-identity-missing-facts backend)]
           [:packet-artifact-id (assoc packet :artifact-id
                                       "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb")
            "B1-METADATA"
            (get final-identity-missing-facts backend)]
           [:packet-path-binding-id (assoc packet :actual-path-binding-id
                                           "sha256:cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc")
            "B1-METADATA"
            (get final-identity-missing-facts backend)]
           [:packet-path-provenance
            (rehash-packet
             (assoc-in packet [:actual-path-provenance :source]
                       "attacker/source.gravity"))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:c13-path-provenance
            (rehash-packet
             (assoc-in packet [:c13 :actual-path-provenance :source]
                       "attacker/c13-source.gravity"))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:c13-semantic-id
            (rehash-packet
             (assoc-in packet [:c13 :semantic-id]
                       "sha256:abababababababababababababababababababababababababababababababab"))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:c13-path-binding-id
            (rehash-packet
             (assoc-in packet [:c13 :actual-path-binding-id]
                       "sha256:cdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcd"))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:c14-request-id
            (rehash-packet
             (assoc-in packet [:c14 :request :request-id]
                       "sha256:efefefefefefefefefefefefefefefefefefefefefefefefefefefefefefefef"))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:c13-stage-substitution (rehash-packet
                                     (assoc packet :c13 (:c13 other-packet)))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:c14-stage-substitution (rehash-packet
                                     (assoc packet :c14 (:c14 other-packet)))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:b1-stage-substitution (rehash-packet
                                    (assoc packet :b1 (:b1 other-packet)))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:c11-verifier-replay
            (rehash-packet
             (assoc-in packet [:c13 :verifier-replay :result] :rejected))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:c11-verifier-report
            (rehash-packet
             (assoc-in packet [:c11 :verifier-record :status] :rejected))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:fact-bindings
            (rehash-packet
             (assoc-in packet [:c13 :semantic-identity :fact-bindings :type :content-id]
                       "sha256:dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:runtime-checks
            (rehash-packet
             (assoc-in packet [:c13 :residual-check-report :retained-runtime-checks]
                       [{:hostile true}]))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:operation-effect-order
            (rehash-packet
             (assoc-in packet [:c13 :semantic-identity :effect-order-graph]
                       {:hostile true}))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:optimized-mir-substitution
            (rehash-packet
             (assoc-in packet [:c13 :optimized-mir]
                       (:optimized-mir (:c13 other-packet))))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]
           [:decision-ledger
            (rehash-packet
             (assoc-in packet [:c13 :decision-record :output-mir]
                       "sha256:eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"))
            "C11-VERIFY"
            (get contextual-reconstruction-missing-facts backend)]]]
      (doseq [[label hostile expected-id expected-missing] preflight-cases]
        (testing (str (name backend) " " (name label))
          (let [data (packet-diagnostic backend hostile checked-core context)]
            (is (nil? (:unexpected data)) data)
            (is (= expected-id (:id data)) data)
            (is (= expected-id (:rule data)) data)
            (is (= expected-missing (:missing-fact data)) data)
            (is (= :rejected (:fallback-status data)) data)
            (is (= (:diagnostic-id data)
                   (bootstrap/c15-stable-diagnostic-id data)) data)
            (is (false?
                 (bootstrap/p15-s23-stage2-c13-c14-b1-authentic?
                  hostile checked-core context)))))))))
