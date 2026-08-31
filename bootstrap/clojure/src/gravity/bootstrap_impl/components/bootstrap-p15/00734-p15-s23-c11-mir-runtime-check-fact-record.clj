

(defn p15-s23-c11-mir-runtime-check-fact-record
  [checked-core node fact-kind]
  (let [node-id (:node-id node)
        check (get-in node [:safety :check])
        fact-id (p15-s23-c11-mir-runtime-check-fact-id check fact-kind)
        check-op-id (p15-s23-c11-mir-runtime-check-operation-id check)
        token-id (p15-s23-c11-mir-runtime-check-token-id check)
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

(defn p15-s23-c11-mir-add-runtime-check-facts
  [checked-core fact-kind table]
  (reduce
   (fn [result node]
     (if-let [check (get-in node [:safety :check])]
       (assoc result
              (p15-s23-c11-mir-runtime-check-fact-id check fact-kind)
              (p15-s23-c11-mir-runtime-check-fact-record
               checked-core node fact-kind))
       result))
   table
   (:core-nodes checked-core)))

(defn p15-s23-c11-mir-reindex-fact-table
  [nodes table id-key suffix]
  (into (sorted-map)
        (map (fn [node]
               (let [node-id (:node-id node)
                     record (get table node-id)
                     fact-id (or (get record id-key)
                                 (str node-id suffix))]
                 [fact-id (assoc record id-key fact-id)])))
        nodes))

(defn p15-s23-c11-mir-profile-target-table
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

(defn p15-s23-c11-mir-capability-proof-table
  [checked-core]
  (into (sorted-map)
        (map (juxt :proof-id identity))
        (:capability-proof-records checked-core)))

(defn p15-s23-c11-mir-safety-proof-table
  [checked-core]
  (into (sorted-map)
        (keep (fn [node]
                (when-let [proof-id (get-in node [:safety :proof :proof-id])]
                  [proof-id (get-in node [:safety :proof])])) )
        (:core-nodes checked-core)))

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
           :maximum-module-carrier-nodes
           p15-s23-c11-mir-max-carrier-nodes
           :maximum-final-artifact-carrier-nodes
           p15-s23-c11-mir-max-final-artifact-carrier-nodes
           :maximum-carrier-depth 256
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