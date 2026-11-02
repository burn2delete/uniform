

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
  (ownership/forwarded-value-node-ids node))

(defn p15-s23-closed-core-propagate-managed-ownership
  [nodes]
  (ownership/propagate-managed-ownership #'p15-s23-closed-core-forwarded-value-node-ids nodes))

(defn p15-s23-closed-core-child-obligation-refs
  [node node-by-id capability-proof-by-node-capability]
  (gravity.safety/child-obligation-refs node node-by-id capability-proof-by-node-capability))

(defn p15-s23-closed-core-rederive-proven-safety
  [source-content-hash nodes authority-evidence]
  (gravity.safety/rederive-proven-safety #'p15-s23-checked-core-capability-proof-records #'p15-s23-closed-core-pure-safety-proof #'p15-s23-closed-core-structural-safety-proof #'p15-s23-closed-core-child-obligation-refs source-content-hash nodes authority-evidence))

(defn p15-s23-closed-core-shared-owner-domain-id
  [nodes]
  (ownership/shared-owner-domain-id #'p15-s23-closed-core-digest nodes))

(defn p15-s23-closed-core-canonical-owner-id
  [product node]
  (ownership/canonical-owner-id #'p15-s23-closed-core-shared-owner-domain-id product node))

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