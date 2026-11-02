

(defn p15-s23-c11-mir-runtime-check-fact-record
  [checked-core node fact-kind]
  (gravity.mir/runtime-check-fact-record #'p15-s23-c11-mir-runtime-check-fact-id #'p15-s23-c11-mir-runtime-check-operation-id #'p15-s23-c11-mir-runtime-check-token-id checked-core node fact-kind))

(defn p15-s23-c11-mir-add-runtime-check-facts
  [checked-core fact-kind table]
  (gravity.mir/add-runtime-check-facts #'p15-s23-c11-mir-runtime-check-fact-id #'p15-s23-c11-mir-runtime-check-fact-record checked-core fact-kind table))

(defn p15-s23-c11-mir-reindex-fact-table
  [nodes table id-key suffix]
  (gravity.mir/reindex-fact-table nodes table id-key suffix))

(defn p15-s23-c11-mir-profile-target-table
  [checked-core]
  (gravity.mir/profile-target-table checked-core))

(defn p15-s23-c11-mir-capability-proof-table
  [checked-core]
  (gravity.mir/capability-proof-table checked-core))

(defn p15-s23-c11-mir-safety-proof-table
  [checked-core]
  (gravity.mir/safety-proof-table checked-core))

(defn p15-s23-c11-mir-pass-contract-record
  []
  {:artifact :gravity/compiler-pass-contract
   :pass-id :c11-build-mir-bounded-slice
   :owner :gravity-source
   :input :gravity/safety-checked-core
   :output :gravity/mir-module
   :requires [:authenticated-checked-core-artifact
              :canonical-core-node-order
              :type-facts :effect-facts :effect-order-graph
              :capability-facts :capability-proofs
              :ownership-facts :safety-outcomes
              :runtime-check-records :profile-facts
              :complete-origin-closure]
   :preserves [:checked-core-artifact-id :core-node-identity
               :source-spans :origin-ids :generated-origin-chains
               :types :effects :effect-order :capabilities
               :ownership :safety :runtime-check-failure-behavior
               :profile :source-target :target-request]
   :invalidates [:core-control-shape]
   :regenerates [:mir-block-placement :control-flow-graph
                 :typed-data-flow-graph :mir-value-use-def
                 :runtime-check-token-guards :mir-source-map]
   :effects #{}
   :capabilities #{}
   :profiles [:hosted]
   :source-targets [:jvm]
   :target-independent-output? true
   :emits [:mir-module :pending-build-mir-pass-execution-record
           :c11-diagnostic]
   :postcondition-verifier :clojure-stage0-independent-c11-verifier
   :risk :bootstrap-seed-tcb
           :scope {:operation-set
           [:literal :implicit-nil :quote :local :let-binding :truthy
            :integer-eq :integer-lt :integer-lte :integer-gt :integer-gte
            :do :if :let :str :println :function
            :runtime-check]
           :maximum-conditionals 1
           :whole-c11? false
           :self-hosted? false}})

(defn p15-s23-c11-mir-canonical-block-order
  [mir]
  (let [source-core (:source-core mir)
        entry-id (str source-core ":mir:entry")
        then-id (str source-core ":mir:then")
        else-id (str source-core ":mir:else")
        join-id (str source-core ":mir:join")
        blocks (get-in mir [:functions
                            (first (keys (:functions mir)))
                            :blocks])]
    (if (contains? blocks join-id)
      [entry-id then-id else-id join-id]
      [entry-id])))

(defn p15-s23-c11-mir-operation-sequence
  [mir]
  (let [blocks (get-in mir [:functions
                            (first (keys (:functions mir)))
                            :blocks])]
    (vec
     (mapcat #(get-in blocks [% :instructions] [])
             (p15-s23-c11-mir-canonical-block-order mir)))))

(defn p15-s23-c11-mir-path-prefix?
  [prefix path]
  (and (vector? prefix)
       (vector? path)
       (<= (count prefix) (count path))
       (= prefix (subvec path 0 (count prefix)))))
