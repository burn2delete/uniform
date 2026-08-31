

(defn p15-s23-closed-core-dependency-edge-kind
  [node operand-index operand-count]
  (case (:source-operation node)
    :local :binding-use
    :let-binding :initializer
    :str :argument-value
    :println :argument-value
    :truthy :control
    :if (if (zero? operand-index) :control :conditional-incoming)
    :do (if (= operand-index (dec operand-count)) :result :sequence)
    :function (if (= operand-index (dec operand-count)) :result :sequence)
    :let (let [binding-count (get-in node [:attributes :binding-count])]
           (cond
             (< operand-index binding-count) :initializer
             (= operand-index (dec operand-count)) :result
             :else :sequence))
    :argument-value))

(defn p15-s23-closed-core-dependency-order-graph
  [nodes]
  (let [index-by-id (into {} (map-indexed (fn [idx node]
                                            [(:node-id node) idx]))
                         nodes)
        node-by-id (into {} (map (juxt :node-id identity)) nodes)
        edges
        (mapv
         (fn [[node operand-index operand]]
           (let [dependency-index (get index-by-id operand)
                 consumer-index (get index-by-id (:node-id node))]
             {:dependency operand
              :consumer (:node-id node)
              :dependency-index dependency-index
              :consumer-index consumer-index
              :edge-kind
              (p15-s23-closed-core-dependency-edge-kind
               node operand-index (count (:operands node)))
              :dependency-precedes-consumer?
              (and (integer? dependency-index)
                   (integer? consumer-index)
                   (< dependency-index consumer-index))}))
         (mapcat (fn [node]
                   (map-indexed (fn [operand-index operand]
                                  [node operand-index operand])
                                (:operands node)))
                 nodes))
        dependencies-resolve?
        (every? :dependency-precedes-consumer? edges)
        lexical-bindings-resolve?
        (every?
         (fn [node]
           (if (= :local (:source-operation node))
             (let [binding-id (first (:operands node))
                   binding-node (get node-by-id binding-id)]
               (and (= 1 (count (:operands node)))
                    (= binding-id
                       (get-in node [:attributes :resolved-binding]))
                    (= :binding (:kind binding-node))))
             true))
         nodes)]
    {:artifact :gravity/p15-s23-closed-dependency-order-graph
     :edges edges
     :all-dependencies-precede-consumers? dependencies-resolve?
     :all-lexical-bindings-resolve? lexical-bindings-resolve?
     :status (if (and dependencies-resolve? lexical-bindings-resolve?)
               :passed
               :failed)}))

(defn p15-s23-closed-core-recomputed-binding-records
  [nodes]
  (mapv
   (fn [node]
     {:binding-node-id (:node-id node)
      :name (get-in node [:attributes :name])
      :path (:path node)
      :value-node-id (first (:operands node))
      :shadowed-binding (get-in node [:attributes :shadowed-binding])
      :source-form-id (get-in node [:attributes :source-form-id])})
   (filterv #(= :let-binding (:source-operation %)) nodes)))

(defn p15-s23-closed-core-binding-shadow-links-valid?
  [nodes binding-records]
  (let [index-by-id (into {} (map-indexed (fn [idx node]
                                            [(:node-id node) idx])
                                          nodes))
        binding-by-id (into {} (map (juxt :binding-node-id identity))
                            binding-records)]
    (every?
     (fn [record]
       (let [shadow-id (:shadowed-binding record)]
         (or (nil? shadow-id)
             (let [shadow (get binding-by-id shadow-id)]
               (and shadow
                    (= (:name record) (:name shadow))
                    (< (get index-by-id shadow-id Long/MAX_VALUE)
                       (get index-by-id (:binding-node-id record)
                            Long/MIN_VALUE)))))))
     binding-records)))

(defn p15-s23-closed-core-semantic-input
  [artifact]
  (select-keys
   artifact
   [:kind :status :scope :source-content-hash :source-core-input
    :entrypoint :profile :source-target :core-nodes :root-node-ids
    :type-facts :effect-facts :capability-facts :ownership-facts
    :safety-facts :profile-facts :typed-core :effect-graph
    :capability-proof-records :pure-capability-closure :ownership-analysis
    :dependency-order-graph
    :lexical-binding-records :source-origin-table :pass-history
    :authenticated-input :bounds :mir-derived? :whole-language?
    :clojure-seed-boundary? :self-hosted?]))

(defn p15-s23-closed-core-node-id-valid?
  [source-content-hash node]
  (= (:node-id node)
     (p15-s23-closed-core-digest
      {:source-content-hash source-content-hash
       :path (:path node)
       :kind (:kind node)
       :source-operation (:source-operation node)
       :origin-id (get-in node [:source :origin-id])})))

(defn p15-s23-closed-core-origin-id-valid?
  [origin]
  (= (:origin-id origin)
     (p15-s23-closed-core-digest (dissoc origin :origin-id))))

(def p15-s23-closed-core-runtime-check-bound-keys
  #{:program-provider-selection-id :program-provider-id :program-grant-id
    :authority-record-id :authority-evidence-id :capability-proof-id
    :runtime-provider-id :runtime-grant-id
    :runtime-handler-provider-id :runtime-handler-grant-id
    :structural-invocation-state})

(defn p15-s23-closed-core-runtime-check-valid?
  ([check]
   (and (map? check)
        (= :gravity/runtime-check (:artifact check))
        (= :required (:status check))
        (= (:check-id check)
           (p15-s23-closed-core-digest
            (dissoc check :artifact :check-id :status)))))
  ([check node authority-evidence capability-proof]
   (let [operation (:source-operation node)
         capability (:capability check)
         provider (get-in authority-evidence
                          [:provider-bindings capability])
         grant (get-in authority-evidence [:grant-bindings capability])
         base-keys
         (case operation
           :str #{:kind :source-content-hash :path :origin-id :effect
                  :capability :provider :failure}
           :println #{:kind :source-content-hash :path :origin-id :effect
                      :capability :provider :handler :delivery
                      :live-external-io? :failure}
           #{})
         expected-keys
         (set/union base-keys p15-s23-closed-core-runtime-check-bound-keys
                    #{:artifact :check-id :status})]
     (and
      (p15-s23-closed-core-runtime-check-valid? check)
      (= expected-keys (set (keys check)))
      (= (:check-id check) (get-in node [:attributes :runtime-check-id]))
      (= (:origin-id check) (get-in node [:source :origin-id]))
      (= (:path check) (:path node))
      (= (:effect check)
         (first (p15-s23-closed-core-intrinsic-effects operation)))
      (= capability
         (first (p15-s23-closed-core-intrinsic-capabilities operation)))
      (map? provider)
      (map? grant)
      (map? capability-proof)
      (= (:provider-selection-id provider)
         (:program-provider-selection-id check))
      (= (:provider-id provider) (:program-provider-id check))
      (= (:grant-id grant) (:program-grant-id check))
      (= (:authority-record-id authority-evidence)
         (:authority-record-id check))
      (= (:evidence-id authority-evidence) (:authority-evidence-id check))
      (= (:proof-id capability-proof) (:capability-proof-id check))
      (= :pre-execution-authority-bound
         (:structural-invocation-state check))
      (case operation
        :str
        (and (= :managed-allocation-result (:kind check))
             (= :gravity.reference/jvm-managed-allocator (:provider check))
             (= :gravity.reference/jvm-managed-allocator
                (:runtime-provider-id check))
             (= :gravity.reference/grant-managed-allocation
                (:runtime-grant-id check))
             (= :not-applicable (:runtime-handler-provider-id check))
             (= :not-applicable (:runtime-handler-grant-id check))
             (= :gravity/allocation-error (:failure check)))

        :println
        (and (= :reference-transcript-delivery (:kind check))
             (= :gravity.reference/transcript-capture (:provider check))
             (= :gravity.bootstrap/reference-harness (:handler check))
             (= :in-memory-reference-transcript (:delivery check))
             (false? (:live-external-io? check))
             (= :gravity.reference/transcript-capture
                (:runtime-provider-id check)
                (:runtime-handler-provider-id check))
             (= :gravity.reference/grant-reference-stdout
                (:runtime-grant-id check))
             (= :gravity.reference/grant-test-fixture
                (:runtime-handler-grant-id check))
             (= :gravity/transcript-capture-error (:failure check)))

        false)))))

(defn p15-s23-closed-core-node-aggregate-facts
  [node node-by-id]
  (let [intrinsic-effects
        (p15-s23-closed-core-intrinsic-effects (:source-operation node))
        intrinsic-capabilities
        (p15-s23-closed-core-intrinsic-capabilities
         (:source-operation node))
        aggregate? (contains? #{:str :println :do :if :let :function
                                :let-binding :truthy}
                              (:source-operation node))
        operand-nodes (keep node-by-id (:operands node))
        effects (if aggregate?
                  (apply set/union intrinsic-effects
                         (map :effects operand-nodes))
                  intrinsic-effects)
        capabilities (if aggregate?
                       (apply set/union intrinsic-capabilities
                              (map :capabilities operand-nodes))
                       intrinsic-capabilities)]
    {:intrinsic-effects intrinsic-effects
     :intrinsic-capabilities intrinsic-capabilities
     :aggregate-effects effects
     :aggregate-capabilities capabilities}))