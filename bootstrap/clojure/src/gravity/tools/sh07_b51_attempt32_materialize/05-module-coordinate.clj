
(defn- module-coordinate [transaction]
  [(:module-ordinal transaction)
   (get-in transaction
           [:authenticated-core-request :lineage
            :sh06-semantic-projection-id])])

(defn- canonical-map-entries [value]
  (when-not (map? value) (fail! :fixed-fuel-map-entries))
  (->> value
       (sort-by (fn [[key _]]
                  (mapv #(bit-and 255 %) (codec/W key))))
       (mapv vec)))

(defn- form-depth-rows [transaction]
  (let [q (:authenticated-core-request transaction)
        coordinate (module-coordinate transaction)
        forms (:forms q)
        by-id (into {} (map (juxt :form-id identity)) forms)
        top-set (set (:top-level-form-ids q))]
    (when-not (= (count forms) (count by-id)) (fail! :form-depth-duplicate))
    (doseq [form forms, child-id (:child-form-ids form)]
      (when-not (= (:form-id form) (:parent-form-id (get by-id child-id)))
        (fail! :form-depth-child-join)))
    (mapv
     (fn [form]
       [[coordinate (:form-id form)]
        (loop [row form depth 1 seen #{}]
          (when (contains? seen (:form-id row)) (fail! :form-depth-cycle))
          (if-let [parent-id (:parent-form-id row)]
            (if-let [parent (get by-id parent-id)]
              (recur parent (inc depth) (conj seen (:form-id row)))
              (fail! :form-depth-parent))
            (do (when-not (contains? top-set (:form-id row))
                  (fail! :form-depth-top))
                depth)))])
     forms)))

(defn- transaction-collections [transactions _source-forms _carrier]
  (let [module-rows
        (fn [field]
          (mapv (fn [transaction]
                  [(module-coordinate transaction) (get transaction field)])
                transactions))]
    {:l1
     {:module-forms
      (vec (mapcat (fn [transaction]
                     (let [c (module-coordinate transaction)
                           q (:authenticated-core-request transaction)]
                       [[[c :forms] (mapv :form-id (:forms q))]
                        [[c :macro-expansion-trace]
                         (:macro-expansion-trace q)]])) transactions))
      :top-level-forms
      (vec (mapcat
            (fn [transaction]
              (let [c (module-coordinate transaction)
                    q (:authenticated-core-request transaction)
                    expectation (:macro-origin-expectation q)]
                (concat
                 [[[c :top-level-form-ids] (:top-level-form-ids q)]]
                 (mapcat
                  (fn [fragment]
                    [[[[c (:fragment-id fragment)] :root-form-ids]
                      (:root-form-ids fragment)]
                     [[[c (:fragment-id fragment)] :root-node-ids]
                      (:root-node-ids fragment)]])
                  (:fragment-manifest q))
                 [[[c :macro-origin-traces] (:macro-origin-traces q)]
                  [[c :expected-input-syntax-ids]
                   (:expected-input-syntax-ids expectation)]
                  [[c :expected-output-def-syntax-ids]
                   (:expected-output-def-syntax-ids expectation)]
                  [[c :expected-introduced-fn-syntax-ids]
                   (:expected-introduced-fn-syntax-ids expectation)]])))
            transactions))
      :fragment-forms
      (vec (mapcat (fn [transaction]
                     (mapv (fn [fragment]
                             [[(module-coordinate transaction)
                               (:fragment-id fragment)]
                              (:form-ids fragment)])
                           (get-in transaction
                                   [:authenticated-core-request
                                    :fragment-manifest])))
                   transactions))
      :form-children
      (vec (mapcat
            (fn [transaction]
              (let [c (module-coordinate transaction)
                    q (:authenticated-core-request transaction)
                    rc (:resolved-core-carrier transaction)]
                (concat
                 (mapv (fn [form]
                         [[[c (:form-id form)] :child-form-ids]
                          (:child-form-ids form)]) (:forms q))
                 (mapcat
                  (fn [ordinal call]
                    [[[[c :call ordinal] :argument-node-ids]
                      (:argument-node-ids call)]
                     [[[c :call ordinal] :ordered-evaluation-node-ids]
                      (:ordered-evaluation-node-ids call)]])
                  (range) (:calls rc)))))
            transactions))
      :form-depths (vec (mapcat form-depth-rows transactions))
      :origin-entries
      (vec (mapcat (fn [transaction]
                     (mapcat
                      (fn [form]
                        [[[(module-coordinate transaction) :origin-chain
                           (:form-id form)] (:origin-chain form)]
                         [[(module-coordinate transaction) :generated-origin
                           (:form-id form)] (:generated-origin form)]])
                      (get-in transaction
                              [:authenticated-core-request :forms])))
                   transactions))
      :metadata-entries
      (vec (mapcat
            (fn [transaction]
              (let [c (module-coordinate transaction)
                    q (:authenticated-core-request transaction)]
                (concat
                 (mapv (fn [form]
                         [[c :form (:form-id form)]
                          (canonical-map-entries (:metadata form))])
                       (:forms q))
                 (mapv (fn [binding]
                         [[c :binding (:binding-id binding)]
                          (canonical-map-entries (:semantic-span binding))])
                       (:binding-table q))))) transactions))
      :alias-records
      (vec (mapcat
            (fn [transaction]
              (let [c (module-coordinate transaction)
                    q (:authenticated-core-request transaction)]
                (concat
                 [[[c :module-alias-table] (:alias-table q)]]
                 (mapv (fn [fragment]
                         [[c :fragment-aliases (:fragment-id fragment)]
                          (:alias-names fragment)])
                       (:fragment-manifest q))))) transactions))}
     :c2
     {:module-carriers
      (vec (mapcat (fn [transaction]
                     [[[(module-coordinate transaction)
                        :authenticated-request]
                       (:authenticated-core-request transaction)]
                      [[(module-coordinate transaction) :resolved-digests]
                       (:resolved-digests transaction)]]) transactions))}
     :l2
     {:fragments
      (mapv (fn [transaction]
              [(module-coordinate transaction)
               (mapv :fragment-id
                     (get-in transaction
                             [:authenticated-core-request
                              :fragment-manifest]))]) transactions)
      :module-bindings
      (vec (mapcat (fn [transaction]
                     (let [c (module-coordinate transaction)
                           q (:authenticated-core-request transaction)
                           rc (:resolved-core-carrier transaction)]
                       [[[c :binding-table]
                         (mapv :binding-id (:binding-table q))]
                        [[c :function-records] (:function-records rc)]
                        [[c :recursion-components]
                         (:recursion-components rc)]])) transactions))
      :bindings
      (vec (mapcat
            (fn [transaction]
              (mapcat
               (fn [fragment]
                 [[[(module-coordinate transaction) :local
                    (:fragment-id fragment)] (:local-binding-ids fragment)]
                  [[(module-coordinate transaction) :external
                    (:fragment-id fragment)]
                   (:external-binding-ids fragment)]])
               (get-in transaction
                       [:authenticated-core-request :fragment-manifest])))
            transactions))
      :fragment-resolutions
      (vec (mapcat (fn [transaction]
                     (mapv
                      (fn [fragment]
                        [[(module-coordinate transaction)
                          (:fragment-id fragment)]
                         (:resolution-reference-syntax-ids fragment)])
                      (get-in transaction
                              [:authenticated-core-request
                               :fragment-manifest]))) transactions))
      :module-resolutions
      (mapv (fn [transaction]
              [(module-coordinate transaction)
               (mapv :reference-syntax-id
                     (get-in transaction
                             [:authenticated-core-request
                              :resolution-table]))]) transactions)
      :effects
      (vec (mapcat
            (fn [transaction]
              (let [c (module-coordinate transaction)
                    q (:authenticated-core-request transaction)]
                (concat
                 [[[c :module-effects] (get-in q [:module :effects])]]
                 (mapcat
                  (fn [binding]
                    [[[c :binding (:binding-id binding) :effects]
                      (:effects binding)]
                     [[c :binding (:binding-id binding) :profile-set]
                      (:profile-set binding)]
                     [[c :binding (:binding-id binding) :target-set]
                      (:target-set binding)]]) (:binding-table q)))))
            transactions))
      :capabilities
      (vec (mapcat
            (fn [transaction]
              (let [c (module-coordinate transaction)
                    q (:authenticated-core-request transaction)]
                (concat
                 [[[c :module-capabilities]
                   (get-in q [:module :capabilities])]]
                 (mapv (fn [binding]
                         [[c :binding (:binding-id binding) :capabilities]
                          (:capabilities binding)]) (:binding-table q))
                 (mapv (fn [alias]
                         [[c :alias (:alias alias) :targets]
                          (:targets alias)]) (:alias-table q))))) transactions))
      :exports
      (mapv (fn [transaction]
              [(module-coordinate transaction)
               (get-in transaction
                       [:authenticated-core-request :module :exports])])
            transactions)}
     :c6 {:module-core (module-rows :module-core)
          :resolved-core-carriers (module-rows :resolved-core-carrier)}
     :boot2 {:template-carriers (module-rows :template-carrier)}
     :boot3
     {:generated-digest-carriers (module-rows :generated-digest-carrier)}
     :boot4 {:module-digest-requests (module-rows :module-digest-requests)}}))

(defn- rc-node-for-form [rc form]
  (unique-where! #(= (:form-id form) (get-in % [:source :form-id]))
                 (:nodes rc) :core-form-node))
