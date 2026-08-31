

(defn p15-s23-closed-core-canonical-pass-envelopes
  ([nodes module plan-id]
   (p15-s23-closed-core-canonical-pass-envelopes
    nodes module plan-id :pure nil))
  ([nodes module plan-id mode authority-evidence]
  (let [effectful? (= :effectful-reference mode)
        _ (when (and effectful?
                     (not
                      (p15-s23-checked-core-authority-evidence-valid?
                       authority-evidence)))
            (p15-s23-closed-core-fail!
             "C8-CAPABILITY" "<checked-core-facts>" authority-evidence
             {:missing-fact
              :trusted-mode-authenticated-authority-evidence}))
        node-by-id (into {} (map (juxt :node-id identity)) nodes)
        type-id-by-node
        (into (sorted-map)
              (map (fn [node]
                     [(:node-id node)
                      (p15-s23-closed-core-digest
                       {:kind :gravity/type-descriptor
                        :type (p15-s23-closed-core-recomputed-node-type
                               node node-by-id)})]))
              nodes)
        function-node (first (filter #(= :function (:kind %)) nodes))
        function-id (:node-id function-node)
        return-node-id (last (:operands function-node))
        effect-event-ordering
        (when effectful?
          (p15-s23-closed-core-effect-event-ordering nodes))
        typed-core
        {:artifact :gravity/typed-core
         :module module
         :core-input
         (p15-s23-closed-core-digest
          {:plan-id plan-id
           :core-node-keys (mapv :node-id nodes)})
         :types type-id-by-node
         :locals
         (into (sorted-map)
               (map (fn [node]
                      [(:node-id node)
                       {:type (get type-id-by-node (:node-id node))
                        :mutability :immutable
                        :ownership :persistent-shared}]))
               (filter #(= :let-binding (:source-operation %)) nodes))
         :functions
         {function-id
          {:params []
           :return (get type-id-by-node return-node-id)
           :latent-effects (if effectful? (:effects function-node) #{})
           :capabilities (if effectful? (:capabilities function-node) #{})
           :throws #{}}}
         :constraints []
         :dynamic-boundaries []
         :casts []
         :layout-facts :not-applicable
         :diagnostics []}
        effect-graph
        (cond->
        {:artifact :gravity/effect-graph
         :module module
         :nodes
         (into (sorted-map)
               (map (fn [node]
                      [(:node-id node)
                       {:direct (if effectful?
                                  (p15-s23-closed-core-intrinsic-effects
                                   (:source-operation node))
                                  #{})
                        :latent (if (and effectful?
                                          (= :function (:kind node)))
                                  (:effects node)
                                  #{})
                        :transitive (if effectful? (:effects node) #{})
                        :ordering (if (and effectful?
                                           (seq (:effects node)))
                                    :source-order
                                    :none)
                        :source (get-in node [:source :span])}]))
               nodes)
         :functions
         {function-id {:declared (if effectful?
                                   (:effects function-node) #{})
                       :inferred (if effectful?
                                   (:effects function-node) #{})
                       :latent (if effectful?
                                 (:effects function-node) #{})
                       :throws #{}}}
         :namespace {:declared (if effectful?
                                 (:effects function-node) #{})
                     :inferred (if effectful?
                                 (:effects function-node) #{})}
         :build-effects []
         :replay-required #{}
         :diagnostics []}
          effectful?
          (merge effect-event-ordering))
        owner-domain-id
        (p15-s23-closed-core-digest
         {:kind :gravity/persistent-shared-owner-domain
          :function-node-id function-id})
        ownership-analysis
        {:artifact :gravity/ownership-analysis
         :module module
         :owners
         (into (sorted-map)
               (map (fn [node]
                      [(:node-id node)
                       owner-domain-id]))
               nodes)
         :moves []
         :borrows []
         :regions {}
         :arenas {}
         :linear {}
         :transfers []
         :diagnostics []}
        typed-core-key (p15-s23-closed-core-digest typed-core)
        effect-graph-key (p15-s23-closed-core-digest effect-graph)
        ownership-analysis-key
        (p15-s23-closed-core-digest ownership-analysis)]
    {:typed-core typed-core
     :typed-core-key typed-core-key
     :effect-graph effect-graph
     :effect-graph-key effect-graph-key
     :capability-proof-records
     (p15-s23-checked-core-capability-proof-records
      nodes authority-evidence)
     :pure-capability-closure
     (if effectful?
       {:artifact :gravity/p15-s23-checked-core-capability-closure
        :module module
        :required (:capabilities function-node)
        :granted (set (keys (:grant-bindings authority-evidence)))
        :provider-bindings
        (into (sorted-map)
              (map (fn [[capability provider]]
                     [capability (:provider-selection-id provider)]))
              (:provider-bindings authority-evidence))
        :grant-bindings
        (into (sorted-map)
              (map (fn [[capability grant]]
                     [capability (:grant-id grant)]))
              (:grant-bindings authority-evidence))
        :authority-record-id (:authority-record-id authority-evidence)
        :authority-evidence-id (:evidence-id authority-evidence)
        :status :granted-for-authenticated-reference-interpreter}
       {:artifact :gravity/p15-s23-pure-capability-closure
        :module module
        :required #{}
        :granted #{}
        :provider-bindings {}
        :status :not-required})
     :ownership-analysis ownership-analysis
     :ownership-analysis-key ownership-analysis-key})))