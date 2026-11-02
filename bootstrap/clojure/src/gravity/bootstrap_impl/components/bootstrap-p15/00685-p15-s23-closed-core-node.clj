

(defn p15-s23-closed-core-node
  [source-content-hash path kind source-operation plan-node? plan-depth
   operands attributes type effects capabilities ownership safety profile
   source]
  (let [node-id
        (p15-s23-closed-core-digest
         {:source-content-hash source-content-hash
          :path path
          :kind kind
          :source-operation source-operation
          :origin-id (:origin-id source)})
        safety-record
        (if (= :proven-safe (:outcome safety))
          (assoc
           safety :proof
           (if (and (empty? effects) (empty? capabilities))
             (p15-s23-closed-core-pure-safety-proof
              source-content-hash path source-operation source profile
              type effects capabilities ownership (:basis safety))
             (p15-s23-closed-core-structural-safety-proof
              source-content-hash path source-operation source profile
              type effects capabilities ownership (:basis safety)
              (mapv (fn [node-id] {:node-id node-id}) operands))))
          safety)]
    {:node-id node-id
     :path path
     :kind kind
     :source-operation source-operation
     :plan-node? plan-node?
     :plan-depth plan-depth
     :operands (vec operands)
     :attributes (assoc attributes
                        :intrinsic-effects
                        (p15-s23-closed-core-intrinsic-effects
                         source-operation)
                        :intrinsic-capabilities
                        (p15-s23-closed-core-intrinsic-capabilities
                         source-operation)
                        :aggregate-effects (set effects)
                        :aggregate-capabilities (set capabilities))
     :type type
     :effects (set effects)
     :capabilities (set capabilities)
     :ownership ownership
     :safety safety-record
     :profile profile
     :source source}))

(defn p15-s23-closed-core-forwarded-value-node-ids
  [node]
  (case (:source-operation node)
    :local [(first (:operands node))]
    :let-binding [(first (:operands node))]
    :if (vec (rest (:operands node)))
    :do [(last (:operands node))]
    :let [(last (:operands node))]
    :function [(last (:operands node))]
    []))

(defn p15-s23-closed-core-propagate-managed-ownership
  [nodes]
  (loop [remaining nodes
         normalized []
         node-by-id {}]
    (if-let [node (first remaining)]
      (let [source-node-ids
            (p15-s23-closed-core-forwarded-value-node-ids node)
            managed-forwarding?
            (some
             #(= :gravity.reference/jvm-managed-allocator
                 (get-in node-by-id [% :ownership :provider-id]))
             source-node-ids)
            node
            (if managed-forwarding?
              (update node :ownership
                      assoc
                      :provider-requirement
                      :gravity.reference/jvm-managed-allocator
                      :allocator-requirement :memory/allocator
                      :provider-id :gravity.reference/jvm-managed-allocator
                      :lifetime :managed-reachability)
              node)]
        (recur (next remaining) (conj normalized node)
               (assoc node-by-id (:node-id node) node)))
      normalized)))

(defn p15-s23-closed-core-child-obligation-refs
  [node node-by-id capability-proof-by-node-capability]
  (mapv
   (fn [node-id]
     (let [child (get node-by-id node-id)
           outcome (get-in child [:safety :outcome])]
       {:node-id node-id
        :outcome outcome
        :safety-proof-id
        (or (get-in child [:safety :proof :proof-id]) :not-applicable)
        :runtime-check-id
        (or (get-in child [:safety :check :check-id]) :not-applicable)
        :capability-proof-ids
        (mapv #(get-in capability-proof-by-node-capability
                       [[node-id %] :proof-id])
              (sort-by pr-str (:capabilities child)))}))
   (:operands node)))

(defn p15-s23-closed-core-rederive-proven-safety
  [source-content-hash nodes authority-evidence]
  (let [capability-proof-by-node-capability
        (into {}
              (map (fn [proof]
                     [[(:core-node-id proof) (:capability proof)] proof]))
              (p15-s23-checked-core-capability-proof-records
               nodes authority-evidence))]
    (loop [remaining nodes
           normalized []
           final-node-by-id {}]
      (if-let [node (first remaining)]
        (let [node
              (if (= :proven-safe (get-in node [:safety :outcome]))
                (let [proof
                      (if (and (empty? (:effects node))
                               (empty? (:capabilities node)))
                        (p15-s23-closed-core-pure-safety-proof
                         source-content-hash (:path node)
                         (:source-operation node) (:source node)
                         (:profile node) (:type node) (:effects node)
                         (:capabilities node) (:ownership node)
                         (get-in node [:safety :basis]))
                        (p15-s23-closed-core-structural-safety-proof
                         source-content-hash (:path node)
                         (:source-operation node) (:source node)
                         (:profile node) (:type node) (:effects node)
                         (:capabilities node) (:ownership node)
                         (get-in node [:safety :basis])
                         (p15-s23-closed-core-child-obligation-refs
                          node final-node-by-id
                          capability-proof-by-node-capability)))]
                  (assoc-in node [:safety :proof] proof))
                node)]
          (recur (next remaining) (conj normalized node)
                 (assoc final-node-by-id (:node-id node) node)))
        normalized))))

(defn p15-s23-closed-core-shared-owner-domain-id
  [nodes]
  (when-let [function-node (first (filter #(= :function (:kind %)) nodes))]
    (p15-s23-closed-core-digest
     {:kind :gravity/persistent-shared-owner-domain
      :function-node-id (:node-id function-node)})))

(defn p15-s23-closed-core-canonical-owner-id
  [product node]
  (or (p15-s23-closed-core-shared-owner-domain-id
       (or (:core-nodes product) (:nodes product)))
      ;; Stored fact tables are verifier inputs, not an authority.  They are a
      ;; fallback only for prospective subjects that do not carry the core
      ;; node set needed for independent owner-domain reconstruction.
      (get-in product [:ownership-facts (:node-id node) :owner-id])
      :not-applicable))

(defn p15-s23-closed-core-enriched-node-subject
  "Attach genuine C2/C3 provenance and pass identities to a diagnostic node."
  [product node module extra]
  (let [origin-id (get-in node [:source :origin-id])
        raw (get (:origin-closure product) origin-id)
        requested-target (or (:requested-target module) (:target module))
        generated-origin
        (vec (concat (or (:c2-reader-generated-origin raw) [])
                     (or (:c3-origin raw) [])
                     (or (:expanded-generated-origin raw) [])))]
    (merge
     node
     {:core-node-id (or (:node-id node) :not-applicable)
      :operation-id (or (:node-id node) :not-applicable)
      :value-id (or (:node-id node) :not-applicable)
      :owner-id (p15-s23-closed-core-canonical-owner-id product node)
      :control-path (or (:path node) :not-applicable)
      :syntax-id (or (:c3-syntax-id raw) :not-applicable)
      :c2-form-id (or (:c2-form-id raw) :not-applicable)
      :source-span (or (:c2-span raw) (get-in node [:source :span]))
      :generated-origin generated-origin
      :namespace (:module module)
      :function 'main
      :profile (:profile module)
      :source-target (:target module)
      :requested-target requested-target
      :target requested-target
      :borrow-id :not-applicable
      :region-id :not-applicable
      :arena-generation :not-applicable
      :resource-id :not-applicable
      :provider :not-applicable
      :grant :not-applicable
      :specialized-safe-rule
      :persistent-immutable-pure-closed-operation
     :safety-mode (:safety module)}
     extra)))

(defn p15-s23-closed-core-prospective-node-subject
  [ctx path kind source-operation origin-products extra]
  (let [source (:source origin-products)
        node-id
        (p15-s23-closed-core-digest
         {:source-content-hash (:source-content-hash ctx)
          :path path
          :kind kind
          :source-operation source-operation
          :origin-id (:origin-id source)})]
    (p15-s23-closed-core-enriched-node-subject
     {:origin-closure
      {(:origin-id source) (:raw origin-products)}}
     {:node-id node-id
      :path path
      :kind kind
      :source-operation source-operation
      :source source
      :profile (:profile ctx)}
     {:module (:module ctx)
      :profile (:profile ctx)
      :target (:source-target ctx)
      :requested-target (:requested-target ctx)
      :safety (:safety ctx)}
     extra)))