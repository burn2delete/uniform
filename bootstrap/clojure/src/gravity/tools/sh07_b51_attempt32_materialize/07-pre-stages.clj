
(defn- pre-stages [input products foundation]
  (let [carrier (:source-semantic-carrier input)
        transactions (:module-transactions input)
        registry (build-owner-registry transactions)
        owner-selection
        (build-owner-selection (root-specs (:bundle-core input))
                               (:bundle-core input) registry)
        form-tuples
        (vec (for [[module-ordinal transaction]
                   (map-indexed vector transactions)
                   form (get-in transaction [:authenticated-core-request :forms])]
               [module-ordinal transaction form
                (rc-node-for-form (:resolved-core-carrier transaction) form)]))
        _ (doseq [transaction transactions]
            (let [q (:authenticated-core-request transaction)
                  rc (:resolved-core-carrier transaction)]
              (when-not (= (count (:forms q)) (count (:nodes rc)))
                (fail! :core-form-node-count))
              (doseq [node (:nodes rc)]
                (unique-where! #(= (get-in node [:source :form-id]) (:form-id %))
                               (:forms q) :core-node-form))))
        core-forms
        (mapv
         (fn [ordinal [module-ordinal transaction form node]]
           (let [q (:authenticated-core-request transaction)
                 top-index (top-level-form-index q form)
                 owner (unique-where!
                        #(and (= module-ordinal (:module-ordinal %))
                              (= top-index (:top-level-form-ordinal %)))
                        registry :core-form-owner)
                 _ (when-not
                    (= [(get-in q [:module :effects])
                        (get-in q [:module :capabilities])
                        (get-in q [:module :profile])]
                       [(get-in node [:preserved-declarations :effects])
                        (get-in node [:preserved-declarations :capabilities])
                        (get-in node [:module :profile])])
                     (fail! :core-form-declaration-join))
                 base {:ordinal ordinal :source-id (:source-id owner)
                       :top-level-form-ordinal top-index
                       :owning-defn-id (:defn-id owner)
                       :form-id (:form-id form)
                       :owning-root-id (:declared-body-root-id owner)
                       :evaluation-index
                       (evaluation-index (:resolved-core-carrier transaction)
                                         node)
                       :effects (get-in node [:preserved-declarations :effects])
                       :capabilities
                       (get-in node [:preserved-declarations :capabilities])
                       :profile (get-in node [:module :profile])}]
             (assoc base :semantic-id
                    (semantic-witness-member-id :l2-core-form base))))
         (range) form-tuples)
        evaluation-entries
        (mapv #(select-keys % [:ordinal :source-id :form-id :semantic-id
                               :evaluation-index]) core-forms)
        effect-entries
        (mapv #(select-keys % [:ordinal :source-id :form-id :semantic-id
                               :effects]) core-forms)
        capability-entries
        (mapv #(select-keys % [:ordinal :source-id :form-id :semantic-id
                               :capabilities]) core-forms)
        ast-entries
        (mapv
         (fn [ordinal [module-ordinal transaction _ node]]
           (let [preceding (set (map :node-id
                                     (take-while #(not= node %)
                                                 (get-in transaction
                                                         [:resolved-core-carrier
                                                          :nodes]))))]
             (when-not (every? preceding (:children node))
               (fail! :c6-child-postorder))
             {:ordinal ordinal :node-id (:node-id node)
              :kind (:core-form node) :children (:children node)
              :source-span (get-in node [:source :semantic-span])
              :origin-chain (get-in node [:source :origin-chain])
              :generated-origin (get-in node [:source :generated-origin])
              :evaluation-index
              (evaluation-index (:resolved-core-carrier transaction) node)
              :binding-context (:binding-context node)
              :profile (get-in node [:module :profile])
              :metadata (:metadata node) :fact-ids []
              :generated (:generated? node)}))
         (range) form-tuples)
        syntax-core-entries
        (mapv (fn [ordinal [_ _ _ node]]
                {:ordinal ordinal :syntax-id (get-in node [:source :syntax-id])
                 :core-id (:node-id node)
                 :origin-chain (get-in node [:source :origin-chain])
                 :generated-origin (get-in node [:source :generated-origin])})
              (range) form-tuples)
        desugaring-entries
        (mapv
         (fn [ordinal [trace kind]]
           (if (= kind :def)
             {:ordinal ordinal :input-id (:input-syntax-id trace)
              :output-id (:output-def-syntax-id trace)
              :pass-id (:macro-expansion-trace-id trace) :fact-ids []
              :origin-id (:def-generated-origin-id trace)}
             {:ordinal ordinal :input-id (:input-syntax-id trace)
              :output-id (:introduced-fn-syntax-id trace)
              :pass-id (:macro-expansion-trace-id trace) :fact-ids []
              :origin-id (:fn-generated-origin-id trace)}))
         (range)
         (mapcat (fn [transaction]
                   (mapcat #(vector [% :def] [% :fn])
                           (get-in transaction
                                   [:authenticated-core-request
                                    :macro-origin-traces]))) transactions))
        origin-entries
        (mapv (fn [ordinal [module-ordinal transaction _ node]]
                {:ordinal ordinal
                 :source-id (get-in transaction
                                    [:authenticated-core-request :lineage
                                     :sh06-semantic-projection-id])
                 :source-node-id (:node-id node)
                 :form-id (get-in node [:source :form-id])
                 :syntax-id (get-in node [:source :syntax-id])
                 :source-span (get-in node [:source :semantic-span])
                 :origin-chain (get-in node [:source :origin-chain])
                 :generated-origin (get-in node [:source :generated-origin])})
              (range) form-tuples)
        c6-evaluation-entries
        (mapv
         (fn [ordinal [module-ordinal transaction evaluation]]
           (merge {:ordinal ordinal :module-ordinal module-ordinal
                   :source-id
                   (get-in transaction
                           [:authenticated-core-request :lineage
                            :sh06-semantic-projection-id])}
                  (select-keys evaluation
                               [:core-node-id :kind :region
                                :owner-function-syntax-id :children])))
         (range)
         (for [[module-ordinal transaction] (map-indexed vector transactions)
               evaluation (get-in transaction
                                  [:resolved-core-carrier :evaluation-order])]
           [module-ordinal transaction evaluation]))
        boot3-modules
        (mapv
         (fn [ordinal transaction product]
           (let [q (:authenticated-core-request transaction)
                 sid (:source-id product)
                 inputs [(artifact-descriptor ordinal
                                              :authenticated-core-request sid q)]
                 outputs
                 [(artifact-descriptor ordinal :core-template sid
                                       (:template-carrier transaction))
                  (artifact-descriptor ordinal :digest-requests sid
                                       (:generated-digest-carrier transaction))
                  (artifact-descriptor ordinal :resolved-digests sid
                                       (:resolved-digests transaction))
                  (artifact-descriptor ordinal :resolved-core sid
                                       (:resolved-core-carrier transaction))]
                 facts (subvec (:fact-records foundation)
                               (* ordinal 4) (* (inc ordinal) 4))]
             {:ordinal ordinal :module (:module q)
              :input-artifacts inputs :output-artifacts outputs
              :effects (get-in q [:module :effects])
              :facts {:entries facts
                      :root (semantic-witness-member-root :boot3-facts facts)}}))
         (range 7) transactions products)
        manifest-entries
        (mapv (fn [ordinal module product]
                {:ordinal ordinal :module-id (:module-id product)
                 :source-id (:source-id product)
                 :input-artifact-ids (mapv :artifact-id
                                           (:input-artifacts module))
                 :output-artifact-ids (mapv :artifact-id
                                            (:output-artifacts module))
                 :profile :meta})
              (range 7) boot3-modules products)
        effect-rows
        (vec (mapcat
              (fn [transaction product]
                (mapv (fn [node]
                        {:module-id (:module-id product) :node-id (:node-id node)
                         :values (get-in node
                                         [:preserved-declarations :effects])})
                      (get-in transaction [:resolved-core-carrier :nodes])))
              transactions products))
        capability-rows
        (vec (mapcat
              (fn [transaction product]
                (mapv (fn [node]
                        {:module-id (:module-id product) :node-id (:node-id node)
                         :values
                         (get-in node
                                 [:preserved-declarations :capabilities])})
                      (get-in transaction [:resolved-core-carrier :nodes])))
              transactions products))]
    {:l1 {:document (:l1-document carrier)
          :source-units (:l1-source-units carrier)
          :grammar-change (:l1-grammar-change carrier)}
     :c2 {:snapshot (:c2-snapshot carrier) :units (:c2-units carrier)
          :source-map (:c2-source-map carrier)
          :diagnostics (:c2-diagnostics carrier)
          :literal-records (:c2-literal-records carrier)
          :reader-extensions (:c2-reader-extensions carrier)}
     :l2 {:owner-selection owner-selection :core-forms core-forms
          :evaluation-order-root
          (semantic-witness-member-root :l2-evaluation-order
                                        evaluation-entries)
          :effects-root (semantic-witness-member-root :l2-effects
                                                      effect-entries)
          :capabilities-root
          (semantic-witness-member-root :l2-capabilities
                                        capability-entries)}
     :c6 {:ast {:entries ast-entries
                :root (semantic-witness-member-root :c6-ast ast-entries)}
          :syntax-core-map
          {:entries syntax-core-entries
           :root (semantic-witness-member-root :c6-syntax-core-map
                                               syntax-core-entries)}
          :desugaring-trace
          {:entries desugaring-entries :root (semantic-witness-member-root :c6-desugaring-trace desugaring-entries)}
          :origins-facts-order {:origin-root (semantic-witness-member-root :c6-origins origin-entries)
           :fact-root (semantic-witness-member-root :c6-facts [])
           :evaluation-order-root (semantic-witness-member-root :c6-evaluation-order c6-evaluation-entries)}}
     :boot2 {:source {:descriptors (:source-descriptors carrier)
               :descriptor-root (get-in carrier [:c2-snapshot :source-descriptors-root])
               :source-id (codec/H [:gravity/sh07-b51-boot2-source-id-v1
                         (get-in carrier [:c2-snapshot :source-descriptors-root])])}
      :compiler {:descriptor (nth (:source-descriptors carrier) 10)
                 :compiler-id (codec/H [:gravity/sh07-b51-boot2-compiler-id-v1 (nth (:source-descriptors carrier) 10)])}
      :backend {:status :not-applicable :identity (codec/H [:gravity/sh07-b51-no-backend-v1 :verified-core])}
      :dependencies {:entries (:semantic-algorithm-source-descriptors input) :root (semantic-witness-member-root
                            :boot2-dependencies (:semantic-algorithm-source-descriptors input))}
      :runtime-assumptions {:jvm-id (semantic-witness-member-id
                :boot2-jvm {:runtime :checker-local}) :clojure-seed-id (semantic-witness-member-root
        :boot2-dependencies (:semantic-algorithm-source-descriptors input)) :git-object-format "sha1"
       :fixed-fuel-bounds-id (codec/H [:gravity/sh07-b51-fixed-fuel-bounds-v1 fixed-fuel-e3]) :network :forbidden}}
     :boot3 {:modules boot3-modules :profile :meta :stage-advanced false} :boot4 {:module-manifest
      {:entries manifest-entries :root (semantic-witness-member-root :boot4-module-manifest manifest-entries)}
      :effects {:entries effect-rows :root (semantic-witness-member-root :boot4-effects effect-rows)}
      :capabilities {:entries capability-rows :root (semantic-witness-member-root :boot4-capabilities capability-rows)}
      :fact-provenance {:facts (:fact-records foundation) :provenance (:provenance-records foundation)
       :fact-root (:fact-root foundation) :provenance-root (:provenance-root foundation)}}}))

(defn- strict-edn-scalar? [value]
  (or (nil? value) (boolean? value) (integer? value) (string? value)
      (char? value) (keyword? value) (symbol? value)))
