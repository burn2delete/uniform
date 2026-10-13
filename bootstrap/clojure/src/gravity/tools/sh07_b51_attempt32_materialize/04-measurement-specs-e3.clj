
(def measurement-specs-e3
  [[0 :l1 0 :maximum-module-forms
    [:collections :module-forms] :maximum-row-cardinality]
   [1 :l1 1 :maximum-top-level-forms
    [:collections :top-level-forms] :maximum-row-cardinality]
   [2 :l1 2 :maximum-fragment-forms
    [:collections :fragment-forms] :maximum-row-cardinality]
   [3 :l1 3 :maximum-form-children
    [:collections :form-children] :maximum-row-cardinality]
   [4 :l1 4 :maximum-form-depth
    [:collections :form-depths] :maximum-row-value]
   [5 :l1 5 :maximum-origin-entries
    [:collections :origin-entries] :maximum-row-cardinality]
   [6 :l1 6 :maximum-metadata-entries
    [:collections :metadata-entries] :maximum-row-cardinality]
   [7 :l1 7 :maximum-alias-records
    [:collections :alias-records] :maximum-row-cardinality]
   [8 :c2 0 :maximum-module-carrier-nodes
    [:collections :module-carriers] :maximum-row-carrier-node-count]
   [9 :c2 1 :maximum-module-carrier-depth
    [:collections :module-carriers] :maximum-row-carrier-depth]
   [10 :c2 2 :maximum-module-carrier-width
    [:collections :module-carriers] :maximum-row-carrier-width]
   [11 :c2 3 :maximum-module-scalar-bytes
    [:collections :module-carriers] :maximum-row-executable-scalar-cost]
   [12 :l2 0 :maximum-fragments
    [:collections :fragments] :maximum-row-cardinality]
   [13 :l2 1 :maximum-module-bindings
    [:collections :module-bindings] :maximum-row-cardinality]
   [14 :l2 2 :maximum-bindings
    [:collections :bindings] :maximum-row-cardinality]
   [15 :l2 3 :maximum-fragment-resolutions
    [:collections :fragment-resolutions] :maximum-row-cardinality]
   [16 :l2 4 :maximum-module-resolutions
    [:collections :module-resolutions] :maximum-row-cardinality]
   [17 :l2 5 :maximum-effects
    [:collections :effects] :maximum-row-cardinality]
   [18 :l2 6 :maximum-capabilities
    [:collections :capabilities] :maximum-row-cardinality]
   [19 :l2 7 :maximum-exports
    [:collections :exports] :maximum-row-cardinality]
   [20 :c6 0 :maximum-module-core-nodes
    [:collections :module-core] :maximum-row-cardinality]
   [21 :c6 1 :maximum-resolved-core-carrier-nodes
    [:collections :resolved-core-carriers] :maximum-row-carrier-node-count]
   [22 :c6 2 :maximum-resolved-core-carrier-depth
    [:collections :resolved-core-carriers] :maximum-row-carrier-depth]
   [23 :c6 3 :maximum-resolved-core-carrier-width
    [:collections :resolved-core-carriers] :maximum-row-carrier-width]
   [24 :c6 4 :maximum-resolved-core-scalar-bytes
    [:collections :resolved-core-carriers] :maximum-row-executable-scalar-cost]
   [25 :boot2 0 :maximum-template-carrier-nodes
    [:collections :template-carriers] :maximum-row-carrier-node-count]
   [26 :boot2 1 :maximum-template-carrier-depth
    [:collections :template-carriers] :maximum-row-carrier-depth]
   [27 :boot2 2 :maximum-template-carrier-width
    [:collections :template-carriers] :maximum-row-carrier-width]
   [28 :boot2 3 :maximum-template-scalar-bytes
    [:collections :template-carriers] :maximum-row-executable-scalar-cost]
   [29 :boot3 0 :maximum-generated-digest-carrier-nodes
    [:collections :generated-digest-carriers]
    :maximum-row-carrier-node-count]
   [30 :boot3 1 :maximum-generated-digest-carrier-depth
    [:collections :generated-digest-carriers] :maximum-row-carrier-depth]
   [31 :boot3 2 :maximum-generated-digest-carrier-width
    [:collections :generated-digest-carriers] :maximum-row-carrier-width]
   [32 :boot3 3 :maximum-generated-digest-scalar-bytes
    [:collections :generated-digest-carriers]
    :maximum-row-executable-scalar-cost]
   [33 :boot4 0 :maximum-module-digest-requests
    [:collections :module-digest-requests] :maximum-row-cardinality]])

(defn validate-semantic-algorithm-authority!
  [descriptors root]
  (when-not
   (and (vector? descriptors)
        (= 7 (count descriptors))
        (= (vec (range 7)) (mapv :ordinal descriptors))
        (= semantic-algorithm-source-paths-e3 (mapv :path descriptors))
        (every? #(and (= semantic-algorithm-descriptor-keys
                         (set (keys %)))
                      (= "100644" (:mode %))
                      (string? (:blob-oid %))
                      (integer? (:byte-count %))
                      (not (neg? (:byte-count %)))
                      (string? (:raw-sha256 %)))
                descriptors)
        (= root
           (codec/H
            [:gravity/sh07-b51-semantic-algorithm-source-root-v1
             descriptors])))
    (fail! :semantic-algorithm-source-authority))
  descriptors)

(defn validate-source-semantic-carrier!
  [content carrier]
  (when-not
   (and (= source-semantic-carrier-keys (set (keys carrier)))
        (vector? (:source-descriptors carrier))
        (= 13 (count (:source-descriptors carrier)))
        (vector? (:source-events carrier))
        (= 40 (count (:source-events carrier)))
        (= (:l1-document carrier) (first (:source-descriptors carrier)))
        (vector? (:l1-source-units carrier))
        (= (vec (range 2 12)) (mapv :ordinal (:l1-source-units carrier)))
        (vector? (:c2-units carrier))
        (= (vec (range 2 12)) (mapv :ordinal (:c2-units carrier)))
        (= content (select-keys (:c2-snapshot carrier) [:commit :tree]))
        (= (codec/H [:gravity/sh07-b51-source-descriptors-root-v1
                     (:source-descriptors carrier)])
           (get-in carrier [:c2-snapshot :source-descriptors-root]))
        (= (codec/H [:gravity/sh07-b51-source-event-root-v1
                     (:source-events carrier)])
           (get-in carrier [:c2-snapshot :event-root])))
    (fail! :source-semantic-carrier))
  carrier)

(defn- module-products [transactions]
  (when-not (and (vector? transactions)
                 (= 7 (count transactions))
                 (= (vec (range 7)) (mapv :module-ordinal transactions)))
    (fail! :module-transactions))
  (mapv
   (fn [ordinal transaction]
     (let [source-id
           (get-in transaction
                   [:authenticated-core-request :lineage
                    :sh06-semantic-projection-id])
           roots
           {:core-root
            (semantic-witness-member-root
             :lowering-modules
             [[:core ordinal source-id (:module-core transaction)]])
            :diagnostic-root
            (semantic-witness-member-root
             :lowering-modules
             [[:diagnostics ordinal source-id
               (get-in transaction [:resolved-core-carrier :diagnostics])]])
            :effect-root
            (semantic-witness-member-root
             :lowering-modules
             [[:effects ordinal source-id
               (get-in transaction [:authenticated-core-request :module
                                     :effects])]])
            :capability-root
            (semantic-witness-member-root
             :lowering-modules
             [[:capabilities ordinal source-id
               (get-in transaction [:authenticated-core-request :module
                                     :capabilities])]])}
           base (merge {:ordinal ordinal :source-id source-id} roots)]
       (when-not (string? source-id) (fail! :module-source-id))
       (assoc base :module-id
              (semantic-witness-member-id :lowering-modules base))))
   (range 7) transactions))

(defn- foundation-preproduct [products algorithm-root]
  (let [fact-records
        (vec
         (mapcat
          (fn [product]
            (mapv
             (fn [[kind root-key]]
               (let [base {:kind kind :subject-id (:module-id product)
                           :value-id (get product root-key)}]
                 (assoc base :id
                        (semantic-witness-member-id :lowering-facts base))))
             [[:core :core-root] [:diagnostic :diagnostic-root]
              [:effect :effect-root] [:capability :capability-root]]))
          products))
        provenance-records
        (vec
         (mapcat
          (fn [product]
            (mapv
             (fn [root-key]
               (let [base {:producer-id algorithm-root
                           :input-ids [(:source-id product)
                                       (:module-id product)]
                           :output-id (get product root-key)}]
                 (assoc base :id
                        (semantic-witness-member-id
                         :lowering-provenance base))))
             [:core-root :diagnostic-root :effect-root :capability-root]))
          products))
        fact-root (codec/H [:gravity/sh07-b51-foundation-fact-root-v1
                            fact-records])
        provenance-root
        (codec/H [:gravity/sh07-b51-foundation-provenance-root-v1
                  provenance-records])
        pass-records
        (mapv
         (fn [product]
           (let [base {:ordinal (:ordinal product)
                       :input-id (:source-id product)
                       :output-id (:core-root product)
                       :invalidates []
                       :preserves [(:diagnostic-root product)
                                   (:effect-root product)
                                   (:capability-root product)]
                       :fact-root fact-root
                       :provenance-root provenance-root}]
             (assoc base :pass-id
                    (semantic-witness-member-id :lowering-passes base))))
         products)]
    {:pass-records pass-records
     :fact-records fact-records
     :provenance-records provenance-records
     :pass-root (codec/H [:gravity/sh07-b51-foundation-pass-root-v1
                          pass-records])
     :fact-root fact-root
     :provenance-root provenance-root}))

(def ^:private root-specs-e3
  [{:root-ordinal 1 :function 'sh07-b51-build-template :arity 1
    :module-ordinal 3}
   {:root-ordinal 2 :function 'sh07-b51-verify-template :arity 3
    :module-ordinal 3}
   {:root-ordinal 3 :function 'sh07-b51-resolve-digest-preimage :arity 3
    :module-ordinal 2}
   {:root-ordinal 4 :function 'sh07-b51-resolve-template :arity 3
    :module-ordinal 3}
   {:root-ordinal 5 :function 'sh07-b51-verify-resolved :arity 4
    :module-ordinal 3}
   {:root-ordinal 6
    :function 'sh07-b51-build-independent-verifier-binding :arity 6
    :module-ordinal 5}
   {:root-ordinal 7 :function 'sh07-b51-build-final-artifact :arity 7
    :module-ordinal 3}
   {:root-ordinal 8 :function 'sh07-b51-finalize-rejection :arity 1
    :module-ordinal 6}])

(defn- root-specs [bundle-core]
  (let [plans (into {} (map (juxt :plan-id identity)
                            (:module-plans bundle-core)))]
    (let [projection
          (mapv (fn [root]
                  (let [plan (get plans (:plan-id root))]
                    (when-not plan (fail! :root-spec-plan))
                    {:root-ordinal (:root-ordinal root)
                     :function (:function root)
                     :arity (:arity root)
                     :module-ordinal (:module-ordinal plan)}))
                (:public-roots bundle-core))]
      (when-not (= root-specs-e3 projection) (fail! :root-spec-projection))
      root-specs-e3)))
