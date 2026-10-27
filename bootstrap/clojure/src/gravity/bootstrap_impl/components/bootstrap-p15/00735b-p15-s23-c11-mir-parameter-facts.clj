(defn p15-s23-c11-mir-parameter-facts-valid? [core node]
  (let [id (:node-id node) origin (get-in node [:source :origin-id])
        sha? #(and (string? %) (boolean (re-matches #"sha256:[0-9a-f]{64}" %)))
        type-fact (get-in core [:type-facts id]) effect (get-in core [:effect-facts id])
        ownership (get-in core [:ownership-facts id]) safety (get-in core [:safety-facts id])
        proof (get-in node [:safety :proof])
        own {:model :persistent-immutable-value :role :function-parameter :shareability :shared
             :alias-policy :immutable-sharing :mutation :forbidden
             :managed-reachability :value-and-program-reachability :cleanup-policy :no-explicit-cleanup
             :provider-requirement :not-required :allocator-requirement :not-required
             :escape-policy :safe-persistent-value :derived? true :storage :persistent-parameter-value}
        root (last (:core-nodes core)) root-fact (get-in core [:type-facts (:node-id root)])
        common {:core-node-id id :source-origin-id origin :profile :hosted :source-target :jvm :derived? true}]
    (and (= #{:node-id :path :kind :source-operation :plan-node? :plan-depth :operands
              :attributes :type :effects :capabilities :ownership :safety :profile :source}
            (set (keys node)))
         (= [:binding false 0 :hosted #{} #{}]
            ((juxt :kind :plan-node? :plan-depth :profile :effects :capabilities) node))
         (= :hosted (:profile core)) (= :jvm (:source-target core))
         (= own (:ownership node))
         (= (:owner-id ownership) (get-in core [:ownership-facts (:node-id root) :owner-id]))
         (every? sha? [(:fact-id type-fact) (:type-id type-fact) (:fact-id effect)
                       (:fact-id ownership) (:owner-id ownership) (:proof-id proof) (:ownership-digest proof)])
         (= type-fact (assoc common :fact-id (:fact-id type-fact) :type-id (:type-id type-fact)
                            :type :gravity/bool :constraints [] :dependencies []
                            :producer-rule :resolved-bool-parameter-declaration :declaration (:attributes node)))
         (every? sha? [(:fact-id root-fact) (:type-id root-fact)])
         (= root-fact (merge (:type root)
                            {:core-node-id (:node-id root) :source-origin-id (get-in root [:source :origin-id])
                             :profile :hosted :source-target :jvm :derived? true
                             :fact-id (:fact-id root-fact) :type-id (:type-id root-fact)
                             :type (:type root) :constraints [] :dependencies (:operands root)
                             :producer-rule :closed-function-type}))
         (= effect (assoc common :fact-id (:fact-id effect) :type-fact-id (:fact-id type-fact)
                         :direct #{} :latent #{} :transitive #{} :residual #{} :ordering :none))
         (= ownership
            (merge own common {:fact-id (:fact-id ownership) :owner-id (:owner-id ownership)
                               :value-id id :source-value-ids [] :region :not-applicable :arena :not-applicable
                               :linear-resource :not-applicable :borrow :not-applicable :move :not-applicable
                               :consume :not-applicable :transfer :not-applicable :forwarding :not-applicable
                               :result-disposition :not-applicable :runtime-check :not-applicable :unsafe-audit :not-applicable}))
         (= (get-in core [:capability-facts id])
            (assoc common :required #{} :granted #{} :authority-source :none-required :authority-id nil
                   :grant-ids [] :provider-bindings {}
                   :capability-proof {:basis :empty-capability-requirements :proof-ids [] :status :not-required}))
         (= (get-in core [:profile-facts id])
            {:legal? true :operation :parameter :profile :hosted :scope :gravity-owned-bounded-pure-hosted-jvm-slice})
         (= (:safety node) {:outcome :proven-safe :basis :resolved-bool-parameter-binding :proof proof})
         (= proof (get-in core [:proof-records id]))
         (= proof {:artifact :gravity/c6-c10-pure-safety-proof :domain :gravity/c10-pure-safety-proof-v1
                   :proof-id (:proof-id proof) :core-node-id id :plan-path (:path node)
                   :source-operation :parameter :operand-node-ids [] :type :gravity/bool
                   :effects #{} :capabilities #{} :ownership-model :persistent-immutable-value
                   :ownership-digest (:ownership-digest proof) :profile :hosted :source-target :jvm
                   :source-origin {:origin-id origin :plan-path (:path node) :span (get-in node [:source :span])}
                   :source-content-hash (:source-content-hash core) :basis :resolved-bool-parameter-binding
                   :specialized-safe-rule :persistent-immutable-pure-bounded-operation
                   :effect-requirements #{} :capability-requirements #{}
                   :status :proved-for-bounded-pure-slice :rule-version "c6-c10-pure-safety-v1"
                   :invalidation-conditions [:source-origin-change :operand-change :source-operation-change
                                             :type-change :effect-change :capability-change :ownership-change
                                             :profile-change :source-target-change :rule-version-change]})
         (= safety {:artifact :gravity/safety-outcome :kind :parameter :operation id
                    :source {:core-node id :origin-chain [origin] :span (get-in node [:source :span])}
                    :profile :hosted :target :jvm :outcome :proven-safe :condition :always
                    :proof (:proof-id proof) :runtime-check :not-applicable :failure-behavior :not-applicable
                    :unsafe-audit :not-applicable :facts {:type (:fact-id type-fact)
                                                        :effects (:fact-id effect) :ownership (:fact-id ownership)}}))))
