(ns gravity.mir
  "Native seed C11 records, envelopes and relative validation rules.")

(def ^:private namespace-contract
  '{:namespace gravity.mir
    :contract-boundary :native-seed-c11-records-and-relative-validation
    :governing-contracts [D1 D2 D3 D8 D9 C1 C6 C7 C8 C9 C10 C11 C15 BOOT8 TEST2]
    :public-api
    {node-opcode {:arglists ([node]) :admission :none :native-data-only? true}
     node-facts {:arglists ([checked-core node]) :admission :none :native-data-only? true}
     runtime-check-operation-id {:arglists ([check]) :admission :none :native-data-only? true}
     runtime-check-token-id {:arglists ([check]) :admission :none :native-data-only? true}
     node-operands {:arglists ([token-id node]) :admission :none :native-data-only? true
       :operation-inputs [:runtime-check-token-id]}
     runtime-check-source {:arglists ([node]) :admission :none :native-data-only? true}
     runtime-check-fact-id {:arglists ([check fact-kind]) :admission :none :native-data-only? true}
     module-keys {:arglists nil :admission :none :value-kind :native-module-key-set}
     function-keys {:arglists nil :admission :none :value-kind :native-function-key-set}
     validate-module-envelope!
     {:arglists ([owner-operations source-path checked-core mir]) :admission :none
      :operation-inputs [:entry-parameter! :module-keys :function-keys :require! :require-strict-structure! :pass-contract-record]
      :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     validate-fact-tables!
     {:arglists ([owner-operations source-path checked-core mir]) :admission :none
      :operation-inputs [:add-runtime-check-facts :reindex-fact-table :profile-target-table :capability-proof-table :safety-proof-table :require!]
      :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     validate-operations!
     {:arglists ([owner-operations source-path checked-core mir]) :admission :none
      :operation-inputs [:entry-parameter! :operation-sequence :runtime-check-operation-id :expected-block-operation-ids :require! :block-keys :node-facts :operation-keys :node-opcode :allowed-opcodes :allowed-source-operations :node-operands :expected-runtime-check-operation :set-union]
      :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     validate-effect-edge-placement!
     {:arglists ([require! source-path mir operation-products]) :admission :none
      :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     validate-data-flow!
     {:arglists ([operations source-path checked-core mir operation-products]) :admission :none
      :operation-inputs [:require! :entry-parameter! :expected-data-flow :expected-terminator-uses :runtime-check-token-id :runtime-check-operation-id :expected-value :expected-runtime-check-value :runtime-check-source :valid-dominance-edge? :valid-terminator-use?]
      :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     downstream-ids {:arglists ([outside-nodes if-id]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     expected-block-operation-ids {:arglists ([entry-parameter! runtime-check-operation-id path-prefix? checked-core]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     expected-data-flow {:arglists ([runtime-check-token-id nodes operation-by-id]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     expected-uses {:arglists ([data-flow value-id]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     expected-terminator-uses {:arglists ([checked-core]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     expected-terminator-value-uses {:arglists ([terminator-uses value-id]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     expected-value {:arglists ([node-opcode expected-uses expected-terminator-value-uses node data-flow terminator-uses operation] [node-opcode expected-uses expected-terminator-value-uses node data-flow terminator-uses operation parameter]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     expected-runtime-check-value {:arglists ([runtime-check-token-id runtime-check-operation-id expected-uses runtime-check-source node data-flow operation]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     runtime-check-fact-record {:arglists ([fact-id operation-id token-id checked-core node fact-kind]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     add-runtime-check-facts {:arglists ([fact-id fact-record checked-core fact-kind table]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     reindex-fact-table {:arglists ([nodes table id-key suffix]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     profile-target-table {:arglists ([checked-core]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     capability-proof-table {:arglists ([checked-core]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     safety-proof-table {:arglists ([checked-core]) :admission :none
       :native-data-only? true :authenticates-inputs? false :issues-verifier-certificates? false}
     validate-terminator! {:arglists ([require! terminator-keys source-path checked-core block expected]) :admission :none}
     validate-cfg! {:arglists ([require! validate-terminator! source-path checked-core mir envelope operation-products]) :admission :none}
     valid-dominance-edge? {:arglists ([edge blocks operation-by-id definitions position-by-id join]
                                      [edge blocks operation-by-id definitions position-by-id join parameter]) :admission :none}
     valid-terminator-use? {:arglists ([use blocks operation-by-id position-by-id]) :admission :none}}
    :artifact-inputs [:native-node-and-fact-source-and-check-data :explicit-token-id-operation
                      :native-module-and-function-key-schemas :explicit-envelope-operations
                      :explicit-fact-table-reconstruction-and-require-operations
                      :explicit-operation-rules-and-schema-data
                      :explicit-native-data-flow-rule-operations
                      :native-block-operation-position-definition-use-and-source-tables
                      :native-node-path-operand-and-operation-data
                      :explicit-parameter-opcode-id-source-and-use-operations
                      :native-checked-core-node-fact-and-proof-data
                      :explicit-runtime-check-id-and-fact-record-operations
                      :explicit-require-and-terminator-operations :terminator-key-schema
                      :checked-core-and-mir-data :expected-envelope-and-operation-products
                      :definitions :operation-positions :join-and-parameter-data]
    :artifact-outputs [:native-opcodes-fact-references-operands-generated-source-and-ids
                       :native-module-and-function-key-sets :native-envelope-products
                       :native-fact-table-rule-return :native-operation-products :native-data-flow-counts :native-effect-placement-result
                       :native-block-placement-data :native-use-definition-and-value-records
                       :native-runtime-check-facts :id-indexed-fact-and-proof-tables
                       :cfg-entry-exit-and-conditional-count :native-rule-outcome :dominance-and-use-predicates]
    :diagnostic-ownership {:stage [:native-node-opcode-and-fact-source-id-projection
                                  :native-module-function-and-provenance-envelope-parity :native-envelope-key-schemas
                                  :native-fact-table-equality
                                  :native-operation-shape-parity-and-fact-references
                                  C11-MODULE C11-TARGET-LEAK C11-SAFETY
                                  :native-data-flow-equality-and-dominance :native-effect-edge-placement
                                  C11-DOMINANCE C11-TYPE C11-ORIGIN C11-EFFECT
                                  :native-use-and-placement-reference-records C11-BLOCK :terminating-pure-control-transfer :canonical-cfg-structure]
                           :existing-caller [C11-TYPE C11-EFFECT C11-SAFETY C11-MODULE C11-VERIFY
                                             :effect-graph-equality-and-admission C11-DOMINANCE :predicate-refusal]
                           :supplied-require-operation [:diagnostic-construction :throwing]}
    :dependency-direction {:requires [] :consumed-by [gravity.bootstrap]
                           :forbids [gravity.bootstrap gravity.c11-mir gravity.cli gravity.release]}
    :supported-cfg-families [:single-entry-return :single-conditional-diamond]
    :authenticates-inputs? false :issues-verifier-certificates? false
    :unsupported [:proof-authentication :runtime-check-legality :untrusted-native-ingress :callback-authentication :full-c11-verification
                  :general-cfgs :loops :matches :exceptions :source-admission :b1-admission
                  :backend-or-native-execution :migration :release :self-hosting :seed-retirement]})








(defn node-opcode
  [node]
  (case (:source-operation node)
    :literal :constant
    :implicit-nil :constant
    :quote :constant
    :local :local
    :let-binding :local-binding
    :truthy :truthiness
    :integer-eq :integer-eq
    :integer-lt :integer-lt
    :integer-lte :integer-lte
    :integer-gt :integer-gt
    :integer-gte :integer-gte
    :do :sequence
    :if :conditional-join
    :let :lexical-scope
    :str :call
    :println :call
    :function :function-boundary
    :unsupported-checked-core-operation))

(defn node-facts
  [checked-core node]
  (let [node-id (:node-id node)
        safety (:safety node)
        runtime-check (:check safety)
        capability-fact (get (:capability-facts checked-core) node-id)
        safety-proof (:proof safety)]
    {:type-fact-id (get-in checked-core [:type-facts node-id :fact-id])
     :effect-fact-id (get-in checked-core [:effect-facts node-id :fact-id])
     :capability-fact-id (str node-id ":capability-fact")
     :capability-proof-ids
     (vec (get-in capability-fact [:capability-proof :proof-ids] []))
     :ownership-fact-id
     (get-in checked-core [:ownership-facts node-id :fact-id])
     :safety-outcome-id (str node-id ":safety-outcome")
     :safety-proof-id (or (:proof-id safety-proof) :not-applicable)
     :profile-target-fact-id (str node-id ":profile-target-fact")
     :runtime-check-id (if runtime-check
                         (:check-id runtime-check)
                         :not-applicable)
     :proof-certificate-ids
     (cond-> (vec (get-in capability-fact
                          [:capability-proof :proof-ids] []))
       (:proof-id safety-proof) (conj (:proof-id safety-proof)))
     :failure-behavior (if runtime-check
                         (:failure runtime-check)
                         :not-applicable)
     :source-origin-id (get-in node [:source :origin-id])}))

(defn runtime-check-operation-id
  [check]
  (str (:check-id check) ":mir:runtime-check"))

(defn runtime-check-token-id
  [check]
  (str (:check-id check) ":mir:token"))

(defn node-operands
  [token-id node]
  (if-let [check (get-in node [:safety :check])]
    (into [(token-id check)]
          (:operands node))
    (:operands node)))

(defn runtime-check-source
  [node]
  (let [check (get-in node [:safety :check])
        source (:source node)]
    {:origin-id (:origin-id source)
     :span (:span source)
     :enclosing-syntax-origin-id (:enclosing-syntax-origin-id source)
     :generated? true
     :generated-origin
     [{:role :runtime-check-generated
       :producer-operation-id (:node-id node)
       :runtime-check-id (:check-id check)}]}))

(defn runtime-check-fact-id
  [check fact-kind]
  (str (:check-id check)
       (case fact-kind
         :type ":mir:type-fact"
         :effect ":mir:effect-fact"
         :capability ":mir:capability-fact"
         :ownership ":mir:ownership-fact"
         :safety ":mir:safety-outcome"
         :profile-target ":mir:profile-target-fact")))

(def module-keys
  #{:artifact :schema-version :source-core :module-id :profile
    :source-target :target-request :target-request-metadata :functions
    :globals :control-flow-graph :data-flow-graph :type-table
    :effect-table :effect-order-graph :ownership-table
    :capability-table :capability-proof-table :safety-table
    :profile-target-table :runtime-check-table
    :proof-certificate-table :source-map
    :domain-anchors :diagnostics :provenance :construction
    :pass-contract :pass-execution-record
    :verification-status :target-independent? :b1-preflight :scope
    :clojure-seed-boundary? :self-hosted?})

(def function-keys
  #{:artifact :fn-id :name :params :returns :latent-effects
    :capabilities :blocks :entry :source :facts :provenance})

(defn validate-module-envelope!
  [owner-operations source-path checked-core mir]
  (let [{:keys [entry-parameter! module-keys function-keys require! require-strict-structure! pass-contract-record]} owner-operations
        parameter (entry-parameter! source-path checked-core)
        entrypoint (:entrypoint checked-core)
        function (get-in mir [:functions entrypoint])
        root-id (first (:root-node-ids checked-core))
        root-node (first (filter #(= root-id (:node-id %))
                                 (:core-nodes checked-core)))
        return-id (last (:operands root-node))
        return-node (first (filter #(= return-id (:node-id %))
                                   (:core-nodes checked-core)))]
    (require!
     (and (map? mir)
          (= module-keys (set (keys mir)))
          (= :gravity/mir-module (:artifact mir))
          (= 1 (:schema-version mir))
          (= (:artifact-id checked-core) (:source-core mir))
          (= (get-in checked-core [:source-core-input :module])
             (:module-id mir))
          (= (:profile checked-core) (:profile mir))
          (= (:source-target checked-core) (:source-target mir))
          (= (get-in checked-core
                     [:target-request-metadata :requested-target])
             (:target-request mir))
          (= (:target-request-metadata checked-core)
             (:target-request-metadata mir))
          (map? (:functions mir))
          (= #{entrypoint} (set (keys (:functions mir))))
          (map? (:globals mir))
          (= {} (:globals mir))
          (map? (:domain-anchors mir))
          (= {} (:domain-anchors mir))
          (vector? (:diagnostics mir))
          (= [] (:diagnostics mir))
          (= :pending (:verification-status mir))
          (true? (:target-independent? mir))
          (true? (:clojure-seed-boundary? mir))
          (false? (:self-hosted? mir)))
     "C11-MODULE" source-path mir :authenticated-c11-module-envelope)
    (require!
     (and (map? root-node)
          (map? return-node)
          (map? function)
          (= function-keys (set (keys function)))
          (= :gravity/mir-function (:artifact function))
          (= root-id (:fn-id function))
          (= entrypoint (:name function))
          (vector? (:params function))
          (= (if parameter [(:id parameter)] []) (:params function))
          (= (:type return-node) (:returns function))
          (= (get-in checked-core
                     [:source-core-input :declared-effects])
             (:latent-effects function))
          (= (get-in checked-core
                     [:source-core-input :declared-capabilities])
             (:capabilities function))
          (= (:source root-node) (:source function))
          (= {:type-fact-id
              (get-in checked-core [:type-facts root-id :fact-id])
              :effect-fact-id
              (get-in checked-core [:effect-facts root-id :fact-id])
              :ownership-fact-id
              (get-in checked-core [:ownership-facts root-id :fact-id])
              :safety-outcome-id (str root-id ":safety-outcome")
              :profile-target-fact-id
              (str root-id ":profile-target-fact")}
             (:facts function))
          (= {:checked-core-artifact-id (:artifact-id checked-core)
              :core-node-id root-id}
             (:provenance function)))
     "C11-MODULE" source-path function :authenticated-c11-function-envelope)
    (require!
     (= {:checked-core-artifact-id (:artifact-id checked-core)
         :checked-core-mapping-id (:mapping-id checked-core)
         :checked-core-provenance-binding-id
         (:provenance-binding-id checked-core)
         :checked-core-origin-closure-binding-id
         (:provenance-binding-id checked-core)}
        (:provenance mir))
     "C11-ORIGIN" source-path mir :checked-core-provenance-binding)
    (require-strict-structure!
     source-path
     {:checked-core-artifact-id (:artifact-id checked-core)
      :checked-core-mapping-id (:mapping-id checked-core)
      :checked-core-provenance-binding-id
      (:provenance-binding-id checked-core)
      :checked-core-origin-closure-binding-id
      (:provenance-binding-id checked-core)}
     (:provenance mir)
     :type-sensitive-checked-core-provenance-binding)
    (require!
     (= {:owner :gravity-source
         :function :c11-build-target-independent-mir
         :status :constructed}
        (:construction mir))
     "C11-VERIFY" source-path mir :gravity-owned-mir-construction)
    (require-strict-structure!
     source-path
     {:owner :gravity-source
      :function :c11-build-target-independent-mir
      :status :constructed}
     (:construction mir) :type-sensitive-gravity-owned-mir-construction)
    (require!
     (and (= (pass-contract-record)
             (:pass-contract mir))
          (= {:artifact :gravity/build-mir-pass-execution-record
              :pass-id :c11-build-mir-bounded-slice
              :pass-contract-hash :pending-source-binding
              :input-artifact-id (:artifact-id checked-core)
              :output-content-id :pending-independent-verifier
              :verifier-report-id :pending-independent-verifier
              :verifier-report-hash :pending-independent-verifier
              :diagnostics []
              :status :constructed-unverified
              :record-id :pending-independent-verifier}
             (:pass-execution-record mir)))
     "C11-VERIFY" source-path mir
     :gravity-owned-build-mir-pass-contract-and-pending-execution)
    (require-strict-structure!
     source-path (pass-contract-record)
     (:pass-contract mir) :type-sensitive-build-mir-pass-contract)
    (require-strict-structure!
     source-path
     {:artifact :gravity/build-mir-pass-execution-record
      :pass-id :c11-build-mir-bounded-slice
      :pass-contract-hash :pending-source-binding
      :input-artifact-id (:artifact-id checked-core)
      :output-content-id :pending-independent-verifier
      :verifier-report-id :pending-independent-verifier
      :verifier-report-hash :pending-independent-verifier
      :diagnostics []
      :status :constructed-unverified
      :record-id :pending-independent-verifier}
     (:pass-execution-record mir)
     :type-sensitive-pending-build-mir-pass-execution)
    (require!
     (and (= {:input-kind :gravity/mir
              :requires-verifier-status :passed
              :status :pending-c11-verifier
              :backend-credit? false}
             (:b1-preflight mir))
          (= {:operation-set
              [:literal :implicit-nil :quote :local :let-binding :truthy
               :integer-eq :integer-lt :integer-lte :integer-gt :integer-gte
               :do :if :let :str :println :function
               :runtime-check]
              :maximum-conditionals 1
              :whole-c11? false
              :domain-ir-credit? false
              :optimization-credit? false
              :target-lowering-credit? false
              :backend-credit? false
              :llvm-credit? false
              :release-credit? false
              :whole-language? false
              :self-hosted? false}
             (:scope mir)))
     "C11-VERIFY" source-path mir :bounded-c11-and-b1-scope)
    (require-strict-structure!
     source-path
     {:input-kind :gravity/mir
      :requires-verifier-status :passed
      :status :pending-c11-verifier
      :backend-credit? false}
     (:b1-preflight mir) :type-sensitive-pending-b1-preflight)
    (require-strict-structure!
     source-path
     {:operation-set
      [:literal :implicit-nil :quote :local :let-binding :truthy
       :integer-eq :integer-lt :integer-lte :integer-gt :integer-gte
       :do :if :let :str :println :function
       :runtime-check]
      :maximum-conditionals 1
      :whole-c11? false
      :domain-ir-credit? false
      :optimization-credit? false
      :target-lowering-credit? false
      :backend-credit? false
      :llvm-credit? false
      :release-credit? false
      :whole-language? false
      :self-hosted? false}
     (:scope mir) :type-sensitive-bounded-c11-and-b1-scope)
    {:function function
     :root-node root-node
     :return-node return-node
     :return-id return-id}))

(defn validate-fact-tables!
  [owner-operations source-path checked-core mir]
  (let [{:keys [add-runtime-check-facts reindex-fact-table profile-target-table capability-proof-table safety-proof-table require!]} owner-operations
        nodes (:core-nodes checked-core)
        expected-type
        (add-runtime-check-facts
         checked-core :type
         (reindex-fact-table
          nodes (:type-facts checked-core) :fact-id ":type-fact"))
        expected-effect
        (add-runtime-check-facts
         checked-core :effect
         (reindex-fact-table
          nodes (:effect-facts checked-core) :fact-id ":effect-fact"))
        expected-ownership
        (add-runtime-check-facts
         checked-core :ownership
         (reindex-fact-table
          nodes (:ownership-facts checked-core)
          :fact-id ":ownership-fact"))
        expected-capability
        (add-runtime-check-facts
         checked-core :capability
         (reindex-fact-table
          nodes (:capability-facts checked-core)
          :fact-id ":capability-fact"))
        expected-safety
        (add-runtime-check-facts
         checked-core :safety
         (reindex-fact-table
          nodes (:safety-facts checked-core)
          :outcome-id ":safety-outcome"))
        expected-profile-target
        (add-runtime-check-facts
         checked-core :profile-target
         (profile-target-table checked-core))
        expected-capability-proofs
        (capability-proof-table checked-core)
        expected-safety-proofs
        (safety-proof-table checked-core)]
    (doseq [[expected observed id missing]
            [[expected-type (:type-table mir)
              "C11-TYPE" :id-indexed-c7-type-facts]
             [expected-effect (:effect-table mir)
              "C11-EFFECT" :id-indexed-c8-effect-facts]
             [expected-ownership (:ownership-table mir)
              "C11-VERIFY" :id-indexed-c9-ownership-facts]
             [expected-capability (:capability-table mir)
              "C11-EFFECT" :id-indexed-c8-capability-facts]
             [expected-safety (:safety-table mir)
              "C11-SAFETY" :id-indexed-c10-safety-outcomes]
             [expected-profile-target (:profile-target-table mir)
              "C11-MODULE" :id-indexed-profile-target-facts]
             [expected-capability-proofs (:capability-proof-table mir)
              "C11-EFFECT" :id-indexed-capability-proofs]]]
      (require!
       (= expected observed) id source-path mir missing))
    (require!
     (= {:capability-proof-table-reference :capability-proof-table
         :safety-proofs expected-safety-proofs}
        (:proof-certificate-table mir))
     "C11-SAFETY" source-path mir
     :id-indexed-authenticated-safety-proof-certificates)))

(defn validate-operations!
  [owner-operations source-path checked-core mir]
  (let [{:keys [entry-parameter! operation-sequence runtime-check-operation-id expected-block-operation-ids require! block-keys node-facts operation-keys node-opcode allowed-opcodes allowed-source-operations node-operands expected-runtime-check-operation set-union]} owner-operations
        parameter (entry-parameter! source-path checked-core)
        nodes (:core-nodes checked-core)
        instruction-nodes (filterv #(not= (:id parameter) (:node-id %)) nodes)
        node-by-id (into {} (map (juxt :node-id identity)) nodes)
        runtime-nodes
        (filterv #(map? (get-in % [:safety :check])) nodes)
        operations (operation-sequence mir)
        operation-by-id (into {} (map (juxt :op-id identity)) operations)
        runtime-operation-ids
        (set (map #(runtime-check-operation-id
                    (get-in % [:safety :check]))
                  runtime-nodes))
        block-by-id
        (get-in mir [:functions (:entrypoint checked-core) :blocks])
        expected-block-operation-ids
        (expected-block-operation-ids checked-core)
        observed-block-operation-ids
        (into {}
              (map (fn [[block-id block]]
                     [block-id (mapv :op-id (:instructions block))]))
              block-by-id)]
    (require!
     (and (= (+ (count instruction-nodes) (count runtime-nodes))
             (count operations))
          (= (set-union (set (map :node-id instruction-nodes)) runtime-operation-ids)
             (set (keys operation-by-id)))
          (= (count operations) (count operation-by-id)))
     "C11-MODULE" source-path mir
     :one-core-operation-plus-one-derived-operation-per-runtime-check)
    (require!
     (= expected-block-operation-ids observed-block-operation-ids)
     "C11-DOMINANCE" source-path mir
     :canonical-checked-core-instruction-order-and-placement)
    (doseq [[block-id block] block-by-id]
      (require!
       (and (map? block)
            (= block-keys (set (keys block)))
            (= :gravity/mir-block (:artifact block))
            (= block-id (:block-id block))
            (vector? (:arguments block))
            (vector? (:instructions block))
            (vector? (:predecessors block))
            (vector? (:successors block))
            (vector? (:dominators block))
            (= {:module-data-flow-graph-reference true}
               (:data-flow block)))
       "C11-BLOCK" source-path block :well-formed-mir-block)
      (let [positions
            (into {} (map-indexed (fn [index operation]
                                   [(:op-id operation) index])
                                 (:instructions block)))]
        (doseq [node (filter #(= block-id
                                  (:block-id
                                   (get operation-by-id (:node-id %))))
                             nodes)]
          (let [node-id (:node-id node)
                operation (get operation-by-id node-id)
                attributes (:attributes node)
                expected-effects (:intrinsic-effects attributes)
                expected-capabilities (:intrinsic-capabilities attributes)
                facts (node-facts checked-core node)
                check (get-in node [:safety :check])]
            (require!
             (and (= operation-keys
                     (set (keys operation)))
                  (= :gravity/mir-operation (:artifact operation))
                  (= node-id (:op-id operation))
                  (= (node-opcode node)
                     (:opcode operation))
                  (contains? allowed-opcodes
                             (:opcode operation))
                  (contains? allowed-source-operations
                             (:source-operation operation))
                  (= (:source-operation node) (:source-operation operation))
                  (vector? (:operands operation))
                  (= (node-operands node)
                     (:operands operation))
                  (= node-id (:result operation))
                  (= (if (= :gravity/nil (:type node)) :unit :value)
                     (:result-kind operation))
                  (= (if (= :constant
                            (node-opcode node))
                       {:present? true
                        :value (get-in node [:attributes :value])}
                       :not-applicable)
                     (:constant-payload operation))
                  (= block-id (:block-id operation))
                  (= :pending (:verifier-status operation))
                  (nil? (:domain-anchor operation)))
             (if (contains? allowed-opcodes
                            (:opcode operation))
               "C11-MODULE"
               "C11-TARGET-LEAK")
             source-path operation :checked-core-operation-parity)
            (when (contains?
                   #{:integer-eq :integer-lt :integer-lte
                     :integer-gt :integer-gte}
                   (:source-operation node))
              (let [source-operation (:source-operation node)
                    equality? (= :integer-eq source-operation)
                    primitive
                    (case source-operation
                      :integer-eq '=
                      :integer-lt '<
                      :integer-lte '<=
                      :integer-gt '>
                      :integer-gte '>=)
                    basis
                    (if equality?
                      :exact-signed-integer-equality
                      :exact-integer-ordering)
                    attribute-keys
                    (cond->
                     [:primitive :arity :evaluation-order
                      :operand-types :result-type
                      :numeric-semantics :overflow]
                      (not equality?)
                      (conj :numeric-mode :mathematical-mode))
                    expected-attributes
                    (cond->
                     {:primitive primitive
                      :arity 2
                      :evaluation-order :left-to-right
                      :operand-types
                      [:gravity/integer :gravity/integer]
                      :result-type :gravity/bool
                      :numeric-semantics basis
                      :overflow :not-applicable}
                      (not equality?)
                      (assoc :numeric-mode :proof-required
                             :mathematical-mode :integer-exact))
                    schema-missing
                    (if equality?
                      :exact-binary-integer-equality-schema
                      :exact-binary-integer-order-comparison-schema)
                    proof-missing
                    (if equality?
                      :authenticated-pure-integer-equality-proof
                      :authenticated-pure-integer-order-comparison-proof)
                    [left-id right-id] (:operands node)
                    left (get node-by-id left-id)
                    right (get node-by-id right-id)
                    proof (get-in node [:safety :proof])]
                (require!
                 (and (= 2 (count (:operands node)))
                      (map? left) (map? right)
                      (= :gravity/integer (:type left) (:type right))
                      (= :gravity/bool (:type node) (:type operation))
                      (= :not-applicable (:constant-payload operation))
                      (= expected-attributes
                         (select-keys (:attributes node) attribute-keys)))
                 "C11-TYPE" source-path operation
                 schema-missing)
                (require!
                 (and (= :proven-safe (get-in node [:safety :outcome]))
                      (= basis (get-in node [:safety :basis]))
                      (= basis (:basis proof))
                      (= source-operation (:source-operation proof))
                      (= [left-id right-id] (:operand-node-ids proof))
                      (= :gravity/bool (:type proof))
                      (or equality?
                          (and (= :proof-required (:numeric-mode proof))
                               (= :integer-exact
                                  (:mathematical-mode proof))
                               (= :exact-integer-ordering
                                  (:numeric-semantics proof))))
                      (empty? (:effects operation))
                      (empty? (:capabilities operation)))
                 "C11-SAFETY" source-path operation
                 proof-missing)))
            (require!
             (and (= (:type node) (:type operation))
                  (contains? (:type-table mir) (:type-fact-id facts))
                  (contains? (:effect-table mir) (:effect-fact-id facts))
                  (contains? (:capability-table mir)
                             (:capability-fact-id facts))
                  (contains? (:ownership-table mir)
                             (:ownership-fact-id facts))
                  (contains? (:safety-table mir)
                             (:safety-outcome-id facts))
                  (contains? (:profile-target-table mir)
                             (:profile-target-fact-id facts))
                  (every? #(contains? (:capability-proof-table mir) %)
                          (:capability-proof-ids facts))
                  (every? #(or (contains? (:capability-proof-table mir) %)
                               (contains? (get-in mir
                                                 [:proof-certificate-table
                                                  :safety-proofs]) %))
                          (:proof-certificate-ids facts)))
             "C11-TYPE" source-path operation
             :resolvable-id-referenced-operation-facts)
            (require!
             (and (= expected-effects (:effects operation))
                  (= expected-capabilities (:capabilities operation))
                  (= (if (empty? expected-effects) :none :sequence)
                     (:ordering operation))
                  (= (:profile node) (:profile operation)))
             "C11-EFFECT" source-path operation
             :direct-intrinsic-effect-capability-ordering)
            (require!
             (= facts (:facts operation))
             "C11-SAFETY" source-path operation
             :linked-c6-c10-id-references)
            (require!
             (= (:source node) (:source operation))
             "C11-ORIGIN" source-path operation :checked-core-source-origin)
            (when check
              (let [check-op-id
                    (runtime-check-operation-id check)
                    check-operation (get operation-by-id check-op-id)
                    check-facts (:facts check-operation)]
                (require!
                 (and (= (expected-runtime-check-operation
                          node block-id)
                         check-operation)
                      (= (dec (get positions node-id))
                         (get positions check-op-id))
                      (contains? (:type-table mir)
                                 (:type-fact-id check-facts))
                      (contains? (:effect-table mir)
                                 (:effect-fact-id check-facts))
                      (contains? (:capability-table mir)
                                 (:capability-fact-id check-facts))
                      (contains? (:ownership-table mir)
                                 (:ownership-fact-id check-facts))
                      (contains? (:safety-table mir)
                                 (:safety-outcome-id check-facts))
                      (contains? (:profile-target-table mir)
                                 (:profile-target-fact-id check-facts))
                      (every? #(contains? (:capability-proof-table mir) %)
                              (:capability-proof-ids check-facts))
                      (every? #(contains? (:capability-proof-table mir) %)
                              (:proof-certificate-ids check-facts)))
                 "C11-SAFETY" source-path check-operation
                 :derived-runtime-check-operation-facts-and-guard)))))))
    {:nodes nodes
     :node-by-id node-by-id
     :runtime-nodes runtime-nodes
     :operations operations
     :operation-by-id operation-by-id
     :blocks block-by-id}))

(defn validate-effect-edge-placement!
  [require! source-path mir operation-products]
  (let [blocks (:blocks operation-products)
        operation-by-id (:operation-by-id operation-products)
        join (get-in mir [:control-flow-graph :join])
        position-by-id
        (into {}
              (mapcat
               (fn [[_ block]]
                 (map-indexed (fn [index operation]
                                [(:op-id operation) index])
                              (:instructions block))))
              blocks)]
    (doseq [edge (get-in mir [:effect-order-graph :event-edges])]
      (let [before-operation (get operation-by-id (:before edge))
            after-operation (get operation-by-id (:after edge))
            before-block (:block-id before-operation)
            after-block (:block-id after-operation)
            same-block? (= before-block after-block)
            same-block-order?
            (and same-block?
                 (< (get position-by-id (:before edge) Long/MAX_VALUE)
                    (get position-by-id (:after edge) Long/MIN_VALUE)))
            dominates?
            (and (not same-block?)
                 (contains? (set (:dominators (get blocks after-block)))
                            before-block))
            exact-join-pair?
            (and (= after-block (:block-id join))
                 (= (:after edge) (:value-id join))
                 (some #(= {:predecessor before-block
                            :value (:before edge)}
                           %)
                       (:incoming join)))
            branch-to-post-join?
            (and (= after-block (:block-id join))
                 (contains? (set (map :predecessor (:incoming join)))
                            before-block)
                 (< (get position-by-id (:value-id join) Long/MAX_VALUE)
                    (get position-by-id (:after edge) Long/MIN_VALUE)))]
        (require!
         (and (map? before-operation)
              (map? after-operation)
              (or same-block-order? dominates? exact-join-pair?
                  branch-to-post-join?))
         "C11-EFFECT" source-path after-operation
         :effect-edge-cfg-placement-and-order)))
    :passed))

(defn validate-data-flow!
  [operations source-path checked-core mir operation-products]
  (let [{:keys [require! entry-parameter! expected-data-flow expected-terminator-uses runtime-check-token-id runtime-check-operation-id expected-value expected-runtime-check-value runtime-check-source valid-dominance-edge? valid-terminator-use?]} operations
        parameter (entry-parameter! source-path checked-core)
        nodes (:nodes operation-products)
        operations (:operations operation-products)
        operation-by-id (:operation-by-id operation-products)
        blocks (:blocks operation-products)
        join (get-in mir [:control-flow-graph :join])
        position-by-id
        (into {}
              (mapcat
               (fn [[_ block]]
                 (map-indexed (fn [index operation]
                                [(:op-id operation) index])
                              (:instructions block))))
              blocks)
        data-flow (get-in mir [:data-flow-graph :edges])
        terminator-uses (get-in mir [:data-flow-graph :terminator-uses])
        expected-data-flow
        (expected-data-flow nodes operation-by-id)
        expected-terminator-uses
        (expected-terminator-uses checked-core)
        expected-definitions
        (into
         {}
         (mapcat
          (fn [node]
            (let [node-id (:node-id node)
                  block-id (:block-id (get operation-by-id node-id))
                  check (get-in node [:safety :check])]
              (cond->
               [[node-id (or (when (= node-id (:id parameter)) (:definition parameter)) {:value-id node-id
                          :operation-id node-id
                          :block-id block-id})]]
                check
                (conj
                 [(runtime-check-token-id check)
                  {:value-id
                   (runtime-check-token-id check)
                   :operation-id
                   (runtime-check-operation-id check)
                   :block-id block-id}]))))
          nodes))
        expected-values
        (into
         {}
         (mapcat
          (fn [node]
            (let [node-id (:node-id node)
                  check (get-in node [:safety :check])]
              (cond->
               [[node-id
                 (expected-value
                  node expected-data-flow expected-terminator-uses
                  (get operation-by-id node-id) (when (= node-id (:id parameter)) parameter))]]
                check
                (conj
                 [(runtime-check-token-id check)
                  (expected-runtime-check-value
                   node expected-data-flow
                   (get operation-by-id
                        (runtime-check-operation-id
                         check)))]))))
          nodes))
        expected-source-map
        (into
         {}
         (mapcat
          (fn [node]
            (if-let [check (get-in node [:safety :check])]
              [[(:node-id node) (:source node)]
               [(runtime-check-operation-id check)
                (runtime-check-source node)]]
              [[(:node-id node) (:source node)]]))
          nodes))]
    (require!
     (and (vector? data-flow)
          (= expected-data-flow data-flow))
     "C11-DOMINANCE" source-path mir :exact-checked-core-data-flow)
    (require!
     (every? #(valid-dominance-edge?
               % blocks operation-by-id expected-definitions
               position-by-id join parameter)
             data-flow)
     "C11-DOMINANCE" source-path mir :definition-dominates-use)
    (require!
     (and (vector? terminator-uses)
          (= expected-terminator-uses terminator-uses))
     "C11-DOMINANCE" source-path mir :exact-terminator-use-sites)
    (require!
     (every? #(valid-terminator-use?
               % blocks operation-by-id position-by-id)
             terminator-uses)
     "C11-DOMINANCE" source-path mir
     :terminator-definition-dominance-and-order)
    (require!
     (= expected-definitions
        (get-in mir [:data-flow-graph :definitions]))
     "C11-DOMINANCE" source-path mir :exact-ssa-definition-table)
    (require!
     (= expected-values (get-in mir [:data-flow-graph :values]))
     "C11-TYPE" source-path mir :exact-ssa-value-table)
    (require!
     (= expected-source-map (:source-map mir))
     "C11-ORIGIN" source-path mir :exact-checked-core-source-map)
    (require!
     (every? (fn [operation]
               (let [direct-effects (:effects operation)
                     source-operation (:source-operation operation)]
                 (or (empty? direct-effects)
                     (contains? #{:str :println} source-operation))))
             operations)
     "C11-EFFECT" source-path mir :direct-effectful-operation-boundary)
    {:edge-count (count data-flow)
     :terminator-use-count (count terminator-uses)
     :definition-count (count expected-definitions)
     :value-count (count expected-values)}))


(defn downstream-ids
  [outside-nodes if-id]
  (loop [known #{if-id}]
    (let [expanded
          (reduce
           (fn [result node]
             (if (some result (:operands node))
               (conj result (:node-id node))
               result))
           known
           outside-nodes)]
      (if (= known expanded)
        expanded
        (recur expanded)))))

(defn expected-block-operation-ids
  [entry-parameter! runtime-check-operation-id path-prefix? checked-core]
  (let [parameter (entry-parameter! (:source-path checked-core) checked-core)
        nodes (filterv #(not= (:id parameter) (:node-id %)) (:core-nodes checked-core))
        artifact-id (:artifact-id checked-core)
        entry-id (str artifact-id ":mir:entry")
        then-id (str artifact-id ":mir:then")
        else-id (str artifact-id ":mir:else")
        join-id (str artifact-id ":mir:join")
        if-node (first (filter #(= :if (:source-operation %)) nodes))]
    (letfn [(operation-ids [selected]
              (vec
               (mapcat
                (fn [node]
                  (if-let [check (get-in node [:safety :check])]
                    [(runtime-check-operation-id check)
                     (:node-id node)]
                    [(:node-id node)]))
                selected)))]
     (if-not if-node
      {entry-id (operation-ids nodes)}
      (let [then-prefix (conj (:path if-node) :then)
            else-prefix (conj (:path if-node) :else)
            then-ids
            (set (map :node-id
                      (filter #(path-prefix?
                                then-prefix (:path %))
                              nodes)))
            else-ids
            (set (map :node-id
                      (filter #(path-prefix?
                                else-prefix (:path %))
                              nodes)))
            outside-nodes
            (vec (remove #(or (contains? then-ids (:node-id %))
                              (contains? else-ids (:node-id %)))
                         nodes))
            [entry-nodes continuation-nodes]
            (split-with #(not= (:node-id if-node) (:node-id %))
                        outside-nodes)
            entry-ids (set (map :node-id entry-nodes))
            continuation-ids (set (map :node-id continuation-nodes))
            block-for
            (fn [node]
              (let [node-id (:node-id node)]
                (cond
                  (contains? then-ids node-id) then-id
                  (contains? else-ids node-id) else-id
                  (contains? entry-ids node-id) entry-id
                  (contains? continuation-ids node-id) join-id
                  :else entry-id)))]
        (into {}
              (map (fn [block-id]
                     [block-id
                      (operation-ids
                       (filter #(= block-id (block-for %)) nodes))]))
              [entry-id then-id else-id join-id]))))))

(defn expected-data-flow
  [runtime-check-token-id nodes operation-by-id]
  (vec
   (mapcat
   (fn [node]
      (let [node-id (:node-id node)
            block-id (:block-id (get operation-by-id node-id))
            check (get-in node [:safety :check])
            operand-start (if check 1 0)
            operand-edges
            (mapv (fn [operand-index operand]
                    {:from operand
                     :consumer-kind :operation
                     :consumer-id node-id
                     :consumer-block block-id
                     :operand-index operand-index
                     :edge-kind :operand})
                  (range operand-start
                         (+ operand-start (count (:operands node))))
                  (:operands node))]
        (if check
          (into
           [{:from (runtime-check-token-id check)
             :consumer-kind :operation
             :consumer-id node-id
             :consumer-block block-id
             :operand-index 0
             :edge-kind :runtime-check-guard}]
           operand-edges)
          operand-edges)))
    nodes)))

(defn expected-uses
  [data-flow value-id]
  (vec
   (for [edge data-flow
         :when (= value-id (:from edge))]
     {:use-kind :instruction
      :consumer-kind (:consumer-kind edge)
      :consumer-id (:consumer-id edge)
      :consumer-block (:consumer-block edge)
      :operand-index (:operand-index edge)
      :edge-kind (:edge-kind edge)})))

(defn expected-terminator-uses
  [checked-core]
  (let [nodes (:core-nodes checked-core)
        artifact-id (:artifact-id checked-core)
        root-id (first (:root-node-ids checked-core))
        root-node (first (filter #(= root-id (:node-id %)) nodes))
        return-id (last (:operands root-node))
        entry-id (str artifact-id ":mir:entry")
        then-id (str artifact-id ":mir:then")
        else-id (str artifact-id ":mir:else")
        join-id (str artifact-id ":mir:join")
        if-node (first (filter #(= :if (:source-operation %)) nodes))]
    (if-not if-node
      [{:value-id return-id
        :consumer-kind :terminator
        :consumer-id (str entry-id ":return")
        :consumer-block entry-id
        :operand-index 0
        :edge-kind :return-value}]
      (let [[condition-id then-result-id else-result-id]
            (:operands if-node)]
        [{:value-id condition-id
          :consumer-kind :terminator
          :consumer-id (str entry-id ":conditional")
          :consumer-block entry-id
          :operand-index 0
          :edge-kind :condition}
         {:value-id then-result-id
          :consumer-kind :terminator
          :consumer-id (str then-id ":branch")
          :consumer-block then-id
          :operand-index 0
          :edge-kind :branch-value}
         {:value-id else-result-id
          :consumer-kind :terminator
          :consumer-id (str else-id ":branch")
          :consumer-block else-id
          :operand-index 0
          :edge-kind :branch-value}
         {:value-id return-id
          :consumer-kind :terminator
          :consumer-id (str join-id ":return")
          :consumer-block join-id
          :operand-index 0
          :edge-kind :return-value}]))))

(defn expected-terminator-value-uses
  [terminator-uses value-id]
  (vec
   (for [use terminator-uses
         :when (= value-id (:value-id use))]
     {:use-kind :terminator
      :consumer-kind (:consumer-kind use)
      :consumer-id (:consumer-id use)
      :consumer-block (:consumer-block use)
      :operand-index (:operand-index use)
      :edge-kind (:edge-kind use)})))

(defn expected-value
  ([node-opcode expected-uses expected-terminator-value-uses node data-flow terminator-uses operation]
   (expected-value node-opcode expected-uses expected-terminator-value-uses node data-flow terminator-uses operation nil))
  ([node-opcode expected-uses expected-terminator-value-uses node data-flow terminator-uses operation parameter]
  (let [node-id (:node-id node)]
    {:artifact :gravity/mir-value
     :value-id node-id
     :kind (if parameter :parameter :instruction-result)
     :type (:type node)
     :constant-payload
     (if (and (nil? parameter) (= :constant (node-opcode node)))
       {:present? true :value (get-in node [:attributes :value])}
       :not-applicable)
     :defined-by (or (:definition parameter) {:value-id node-id
                  :operation-id node-id
                  :block-id (:block-id operation)})
     :uses (vec (concat
                 (expected-uses data-flow node-id)
                 (expected-terminator-value-uses
                  terminator-uses node-id)))
     :source (:source node)
     :ownership (:ownership node)
     :effects (:effects node)
     :safety (:safety node)})))

(defn expected-runtime-check-value
  [runtime-check-token-id runtime-check-operation-id expected-uses runtime-check-source node data-flow operation]
  (let [check (get-in node [:safety :check])
        value-id (runtime-check-token-id check)]
    {:artifact :gravity/mir-value
     :value-id value-id
     :kind :runtime-check-token
     :type :gravity/runtime-check-token
     :constant-payload :not-applicable
     :defined-by
     {:value-id value-id
      :operation-id (runtime-check-operation-id check)
      :block-id (:block-id operation)}
     :uses (expected-uses data-flow value-id)
     :source (runtime-check-source node)
     :ownership :not-applicable
     :effects #{}
     :safety {:outcome :runtime-check-token
              :runtime-check-id (:check-id check)
              :guarded-operation-id (:node-id node)}}))


(defn runtime-check-fact-record
  [fact-id operation-id token-id checked-core node fact-kind]
  (let [node-id (:node-id node)
        check (get-in node [:safety :check])
        fact-id (fact-id check fact-kind)
        check-op-id (operation-id check)
        token-id (token-id check)
        origin-id (get-in node [:source :origin-id])
        profile (:profile node)]
    (case fact-kind
      :type
      {:fact-id fact-id
       :core-node-id node-id
       :mir-operation-id check-op-id
       :type-id :gravity/runtime-check-token
       :type :gravity/runtime-check-token
       :producer-rule :runtime-check-token
       :dependencies []
       :source-origin-id origin-id
       :profile profile
       :source-target (:source-target checked-core)
       :constraints []
       :derived? true}

      :effect
      {:fact-id fact-id
       :core-node-id node-id
       :mir-operation-id check-op-id
       :direct #{}
       :latent #{}
       :transitive #{}
       :residual #{}
       :ordering :sequence
       :non-dce? true
       :source-origin-id origin-id
       :profile profile
       :source-target (:source-target checked-core)
       :derived? true}

      :capability
      {:fact-id fact-id
       :core-node-id node-id
       :mir-operation-id check-op-id
       :required #{}
       :granted #{}
       :capability-proof-ids [(:capability-proof-id check)]
       :authority-id (:authority-record-id check)
       :source-origin-id origin-id
       :profile profile
       :source-target (:source-target checked-core)
       :derived? true}

      :ownership
      {:fact-id fact-id
       :core-node-id node-id
       :mir-operation-id check-op-id
       :value-id token-id
       :role :runtime-check-token
       :storage :compiler-generated-token
       :model :non-resource-control-token
       :shareability :single-guard-use
       :mutation :forbidden
       :source-origin-id origin-id
       :profile profile
       :source-target (:source-target checked-core)
       :derived? true}

      :safety
      {:outcome-id fact-id
       :artifact :gravity/safety-outcome
       :operation check-op-id
       :guarded-operation-id node-id
       :kind :runtime-check
       :profile profile
       :target (:source-target checked-core)
       :outcome :runtime-checked
       :runtime-check (:check-id check)
       :proof :not-applicable
       :failure-behavior (:failure check)
       :source-origin-id origin-id
       :derived? true}

      :profile-target
      {:fact-id fact-id
       :core-node-id node-id
       :mir-operation-id check-op-id
       :profile-fact (get (:profile-facts checked-core) node-id)
       :profile profile
       :source-target (:source-target checked-core)
       :target-request
       (get-in checked-core [:target-request-metadata :requested-target])
       :derived? true})))

(defn add-runtime-check-facts
  [fact-id fact-record checked-core fact-kind table]
  (reduce
   (fn [result node]
     (if-let [check (get-in node [:safety :check])]
       (assoc result
              (fact-id check fact-kind)
              (fact-record
               checked-core node fact-kind))
       result))
   table
   (:core-nodes checked-core)))

(defn reindex-fact-table
  [nodes table id-key suffix]
  (into (sorted-map)
        (map (fn [node]
               (let [node-id (:node-id node)
                     record (get table node-id)
                     fact-id (or (get record id-key)
                                 (str node-id suffix))]
                 [fact-id (assoc record id-key fact-id)])))
        nodes))

(defn profile-target-table
  [checked-core]
  (into (sorted-map)
        (map (fn [node]
               (let [node-id (:node-id node)
                     fact-id (str node-id ":profile-target-fact")]
                 [fact-id
                  {:fact-id fact-id
                   :core-node-id node-id
                   :profile-fact (get (:profile-facts checked-core) node-id)
                   :profile (:profile node)
                   :source-target (:source-target checked-core)
                   :target-request
                   (get-in checked-core
                           [:target-request-metadata :requested-target])}])))
        (:core-nodes checked-core)))

(defn capability-proof-table
  [checked-core]
  (into (sorted-map)
        (map (juxt :proof-id identity))
        (:capability-proof-records checked-core)))

(defn safety-proof-table
  [checked-core]
  (into (sorted-map)
        (keep (fn [node]
                (when-let [proof-id (get-in node [:safety :proof :proof-id])]
                  [proof-id (get-in node [:safety :proof])])) )
        (:core-nodes checked-core)))


(defn validate-terminator!
  [require! terminator-keys source-path checked-core block expected]
  (let [terminator (:terminator block)]
    (require!
     (and (map? terminator)
          (= terminator-keys
             (set (keys terminator)))
          (= :gravity/mir-terminator (:artifact terminator))
          (= (:terminator-id expected) (:terminator-id terminator))
          (= (:kind expected) (:kind terminator))
          (vector? (:operands terminator))
          (= (:operands expected) (:operands terminator))
          (vector? (:successors terminator))
          (= (:successors expected) (:successors terminator))
          (= #{} (:effects terminator))
          (= :none (:ordering terminator))
          (= (:source expected) (:source terminator))
          (= (:profile checked-core) (:profile terminator))
          (= (:facts expected) (:facts terminator))
          (= :pending (:verifier-status terminator)))
     "C11-BLOCK" source-path terminator :terminating-pure-control-transfer)))

(defn validate-cfg!
  [require! validate-terminator! source-path checked-core mir envelope operation-products]
  (let [blocks (:blocks operation-products)
        artifact-id (:artifact-id checked-core)
        entry-id (str artifact-id ":mir:entry")
        then-id (str artifact-id ":mir:then")
        else-id (str artifact-id ":mir:else")
        join-id (str artifact-id ":mir:join")
        if-nodes (vec (filter #(= :if (:source-operation %))
                              (:nodes operation-products)))
        conditional-count (count if-nodes)
        cfg (:control-flow-graph mir)
        function (:function envelope)
        return-id (:return-id envelope)]
    (require!
     (and (contains? #{0 1} conditional-count)
          (= entry-id (:entry function))
          (= entry-id (:entry cfg)))
     "C11-BLOCK" source-path mir :bounded-canonical-c11-cfg-entry)
    (if (zero? conditional-count)
      (let [source (:source (first (:nodes operation-products)))
            block (get blocks entry-id)]
        (require!
         (and (= #{entry-id} (set (keys blocks)))
              (vector? (:edges cfg))
              (= [] (:edges cfg))
              (map? (:dominance cfg))
              (every? vector? (vals (:dominance cfg)))
              (= {entry-id [entry-id]} (:dominance cfg))
              (nil? (:join cfg))
              (= [] (:arguments block))
              (= [] (:predecessors block))
              (= [] (:successors block))
              (= [entry-id] (:dominators block))
              (= source (:source block)))
         "C11-BLOCK" source-path block :canonical-linear-c11-cfg)
        (validate-terminator!
         source-path checked-core block
         {:terminator-id (str entry-id ":return")
          :kind :return
          :operands [return-id]
          :successors []
          :source source
          :facts {:returned-value return-id}}))
      (let [if-node (first if-nodes)
            if-id (:node-id if-node)
            [condition-id then-result-id else-result-id] (:operands if-node)
            source (:source if-node)
            expected-edges
            [{:from entry-id :to then-id :kind :true}
             {:from entry-id :to else-id :kind :false}
             {:from then-id :to join-id :kind :join}
             {:from else-id :to join-id :kind :join}]
            expected-dominance
            {entry-id [entry-id]
             then-id [entry-id then-id]
             else-id [entry-id else-id]
             join-id [entry-id join-id]}
            entry-block (get blocks entry-id)
            then-block (get blocks then-id)
            else-block (get blocks else-id)
            join-block (get blocks join-id)]
        (require!
         (and (= #{entry-id then-id else-id join-id} (set (keys blocks)))
              (vector? (:edges cfg))
              (= expected-edges (:edges cfg))
              (map? (:dominance cfg))
              (every? vector? (vals (:dominance cfg)))
              (= expected-dominance (:dominance cfg))
              (vector? (get-in cfg [:join :incoming]))
              (= {:block-id join-id
                  :value-id if-id
                  :incoming
                  [{:predecessor then-id :value then-result-id}
                   {:predecessor else-id :value else-result-id}]}
                 (:join cfg))
              (= [] (:predecessors entry-block))
              (= [then-id else-id] (:successors entry-block))
              (= [entry-id] (:dominators entry-block))
              (= [entry-id] (:predecessors then-block))
              (= [join-id] (:successors then-block))
              (= [entry-id then-id] (:dominators then-block))
              (= [entry-id] (:predecessors else-block))
              (= [join-id] (:successors else-block))
              (= [entry-id else-id] (:dominators else-block))
              (= [then-id else-id] (:predecessors join-block))
              (= [] (:successors join-block))
              (= [entry-id join-id] (:dominators join-block))
              (= source (:source entry-block))
              (= source (:source then-block))
              (= source (:source else-block))
              (= source (:source join-block))
              ;; The explicit :conditional-join instruction is the one SSA
              ;; definition for if-id; CFG :join metadata owns the incoming
              ;; predecessor/value pairing, so no duplicate block argument is
              ;; permitted.
              (= [] (:arguments join-block)))
         "C11-BLOCK" source-path mir :canonical-single-conditional-c11-cfg)
        (validate-terminator!
         source-path checked-core entry-block
         {:terminator-id (str entry-id ":conditional")
          :kind :conditional-branch
          :operands [condition-id]
          :successors [then-id else-id]
          :source source
          :facts {:condition condition-id :join join-id}})
        (validate-terminator!
         source-path checked-core then-block
         {:terminator-id (str then-id ":branch")
          :kind :branch
          :operands [then-result-id]
          :successors [join-id]
          :source source
          :facts {:incoming-value then-result-id}})
        (validate-terminator!
         source-path checked-core else-block
         {:terminator-id (str else-id ":branch")
          :kind :branch
          :operands [else-result-id]
          :successors [join-id]
          :source source
          :facts {:incoming-value else-result-id}})
        (validate-terminator!
         source-path checked-core join-block
         {:terminator-id (str join-id ":return")
          :kind :return
          :operands [return-id]
          :successors []
          :source source
          :facts {:returned-value return-id}})))
    {:conditional-count conditional-count
     :entry-id entry-id
     :exit-id (if (zero? conditional-count) entry-id join-id)}))

(defn valid-dominance-edge?
  ([edge blocks operation-by-id definitions position-by-id join]
   (valid-dominance-edge? edge blocks operation-by-id definitions position-by-id join nil))
  ([edge blocks operation-by-id definitions position-by-id join parameter]
  (let [from-definition (get definitions (:from edge))
        from-operation
        (get operation-by-id (:operation-id from-definition))
        to-operation (get operation-by-id (:consumer-id edge))
        definition-block (:block-id from-operation)
        consumer-block (:consumer-block edge)
        dominators (:dominators (get blocks consumer-block))
        same-block? (= definition-block consumer-block)
        definition-operation-id (:operation-id from-definition)
        ordered-within-block?
        (and same-block?
             (integer? (get position-by-id definition-operation-id))
             (integer? (get position-by-id (:consumer-id edge)))
             (< (get position-by-id definition-operation-id)
                (get position-by-id (:consumer-id edge))))
        predecessor-phi?
        (and (= :conditional-join (:opcode to-operation))
             (= consumer-block (:block-id join))
             (= (:consumer-id edge) (:value-id join))
             (some #(= {:predecessor definition-block
                        :value (:from edge)}
                       %)
                   (:incoming join)))]
    (and (or (map? from-operation)
             (and (= (:from edge) (:id parameter))
                  (= from-definition (:definition parameter))
                  (= :operand (:edge-kind edge))
                  (contains? (set dominators) (:entry-id parameter))))
         (map? to-operation)
         (= :operation (:consumer-kind edge))
         (= (if (= :runtime-check (:opcode from-operation))
              :runtime-check-guard
              :operand)
            (:edge-kind edge))
         (if (= :runtime-check-guard (:edge-kind edge))
           (zero? (:operand-index edge))
           true)
         (= consumer-block (:block-id to-operation))
         (or (and (= (:from edge) (:id parameter)) (= from-definition (:definition parameter)))
             ordered-within-block?
             (and (not same-block?)
                  (contains? (set dominators) definition-block))
             predecessor-phi?)))))

(defn valid-terminator-use?
  [use blocks operation-by-id position-by-id]
  (let [value-operation (get operation-by-id (:value-id use))
        definition-block (:block-id value-operation)
        consumer-block (:consumer-block use)
        block (get blocks consumer-block)
        terminator (:terminator block)
        operand-index (:operand-index use)
        expected-edge-kind
        (case (:kind terminator)
          :conditional-branch :condition
          :branch :branch-value
          :return :return-value
          :unsupported)
        same-block? (= definition-block consumer-block)
        dominates?
        (or (and same-block?
                 (integer? (get position-by-id (:value-id use))))
            (and (not same-block?)
                 (contains? (set (:dominators block)) definition-block)))]
    (and (= #{:value-id :consumer-kind :consumer-id :consumer-block
              :operand-index :edge-kind}
            (set (keys use)))
         (= :terminator (:consumer-kind use))
         (map? value-operation)
         (map? block)
         (map? terminator)
         (= (:consumer-id use) (:terminator-id terminator))
         (integer? operand-index)
         (<= 0 operand-index)
         (< operand-index (count (:operands terminator)))
         (= (:value-id use) (nth (:operands terminator) operand-index))
         (= expected-edge-kind (:edge-kind use))
         dominates?)))

(doseq
 [name (keys (:public-api namespace-contract))
  :let [operation (get (ns-interns *ns*) name)]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
