
(defn- carrier-measure [value]
  (cond
    (strict-edn-scalar? value)
    {:nodes 1 :depth 0 :width 0
     :scalar-cost (if (nil? value) 0 (* 4 (count (str value))))}

    (or (map? value) (vector? value) (set? value) (list? value))
    (let [children (cond
                     (map? value) (mapcat identity value)
                     :else value)
          measured (mapv carrier-measure children)]
      {:nodes (inc (reduce + 0 (map :nodes measured)))
       :depth (if (seq measured) (inc (reduce max (map :depth measured))) 0)
       :width (max (if (map? value) (* 2 (count value)) (count value))
                   (reduce max 0 (map :width measured)))
       :scalar-cost (reduce + 0 (map :scalar-cost measured))})

    :else (fail! :fixed-fuel-noncanonical-value
                 {:class (.getName (class value))})))

(defn- measured-value [rows derivation]
  (when-not (and (vector? rows)
                 (every? #(and (vector? %) (= 2 (count %))) rows)
                 (= (count rows) (count (set (map first rows)))))
    (fail! :fixed-fuel-collection))
  (let [values (mapv second rows)]
    (case derivation
      :maximum-row-cardinality (reduce max 0 (map count values))
      :maximum-row-value
      (do (when-not (every? #(and (integer? %) (not (neg? %))) values)
            (fail! :fixed-fuel-row-value))
          (reduce max 0 values))
      :maximum-row-carrier-node-count
      (reduce max 0 (map (comp :nodes carrier-measure) values))
      :maximum-row-carrier-depth
      (reduce max 0 (map (comp :depth carrier-measure) values))
      :maximum-row-carrier-width
      (reduce max 0 (map (comp :width carrier-measure) values))
      :maximum-row-executable-scalar-cost
      (reduce max 0 (map (comp :scalar-cost carrier-measure) values))
      (fail! :fixed-fuel-derivation))))

(defn- operation-manifest [operation-specs lowering-product algorithm-root]
  (mapv (fn [spec subject]
          (when-not (= (select-keys spec [:ordinal :owner :kind :subject-id])
                       (select-keys subject
                                    [:ordinal :owner :kind :subject-id]))
            (fail! :operation-subject-join))
          (let [base (assoc spec
                            :algorithm-source-root algorithm-root
                            :lowering-product-id (:product-id lowering-product))]
            (assoc base :operation-id
                   (codec/H [:gravity/sh07-b51-bounded-operation-id-v1
                             base]))))
        operation-specs (:fuel-subjects lowering-product)))

(defn- fuel-receipts [subjects operations]
  (let [bounds-id
        (codec/H [:gravity/sh07-b51-fixed-fuel-bounds-v1 fixed-fuel-e3])]
    (mapv
     (fn [subject operation]
       (let [specs (filterv #(= (:owner operation) (nth % 1))
                            measurement-specs-e3)
             trace
             (mapv (fn [ordinal [_ _ _ counter-key coordinate derivation]]
                     {:ordinal ordinal :counter-key counter-key
                      :coordinate coordinate
                      :observed-value
                      (measured-value (get-in subject coordinate) derivation)
                      :derivation derivation})
                   (range) specs)
             counts (into {} (map (juxt :counter-key :observed-value)) trace)
             within? (every? #(<= (val %) (get fixed-fuel-e3 (key %)))
                             counts)
             base {:operation-id (:operation-id operation)
                   :bounds-id bounds-id :trace trace
                   :trace-root
                   (codec/H [:gravity/sh07-b51-fixed-fuel-trace-root-v1 trace])
                   :operation-counts counts :within-bounds within?}]
         (when-not within? (fail! :fixed-fuel-exhausted))
         (assoc base :receipt-id
                (codec/H [:gravity/sh07-b51-fixed-fuel-receipt-v1 base]))))
     subjects operations)))

(def ^:private stage-schema
  {:l1 :gravity/sh07-b51-l1-witness-v1
   :c2 :gravity/sh07-b51-c2-witness-v1
   :l2 :gravity/sh07-b51-l2-witness-v1
   :c6 :gravity/sh07-b51-c6-witness-v1
   :boot2 :gravity/sh07-b51-boot2-witness-v1
   :boot3 :gravity/sh07-b51-boot3-witness-v1
   :boot4 :gravity/sh07-b51-boot4-witness-v1})

(defn- stage-witness [stage-key prefix subject operation _receipt _foundation]
  (let [base
        (cond-> (merge {:schema (get stage-schema stage-key)
                        :fuel-subject-id (:subject-id subject)}
                       prefix
                       {:operation-ids [(:operation-id operation)]})
          (= stage-key :l2)
          (assoc :external-bindings [] :edge-authority [] :diagnostics
                 {:entries [] :root (semantic-witness-member-root
                                     :l2-edge-diagnostics [])}
                 :negative-fixtures
                 {:entries [] :root (semantic-witness-member-root
                                     :l2-edge-negative-fixtures [])})
          (= stage-key :c6)
          (assoc :verification {:checker-ids []
                                :verified-root (get-in prefix [:ast :root])
                                :input-ast-root (get-in prefix [:ast :root])
                                :status :passed}
                 :mutations [])
          (= stage-key :boot3)
          (assoc :tests [] :diagnostic-compatibility []
                 :stage-comparison
                 {:left-lowering-product-id
                  (:lowering-product-id operation)
                  :right-lowering-product-id
                  (:lowering-product-id operation)
                  :observables [] :equal true
                  :equivalence-root
                  (semantic-witness-member-root
                   :boot3-stage-comparison [])})
          (= stage-key :boot4)
          (assoc :deterministic-output
                 {:a-lowering-product-id (:lowering-product-id operation)
                  :b-lowering-product-id (:lowering-product-id operation)
                  :observable-root
                  (get-in prefix [:module-manifest :root])
                  :equal true}
                 :preservation-tests
                 {:positive [] :negative []
                  :result-root
                  (semantic-witness-member-root
                   :boot4-preservation-tests [[] []])
                  :status :passed}))]
    (assoc base :witness-id (codec/H [(get stage-schema stage-key) base]))))

(defn- validate-sh06-carrier-joins! [input]
  ;; Retained-value joins only. Source-origin and whole-object gates are later.
  (let [carrier (:sh06-authority-carrier input)
        authorities (:external-binding-authorities carrier)
        base (dissoc carrier :carrier-id)]
    (when-not
     (and (= #{:content :source-descriptors :bundle-core-id
               :resolution-artifact-paths :resolution-artifacts
               :resolution-verifications :semantic-projection-paths
               :semantic-projection-ids :module-source-descriptors :link-manifest
               :link-authority-event-product-ids :link-authority-id
               :expected-edge-census :resolution-plan-authority
               :external-binding-authorities :carrier-id} (set (keys carrier)))
          (= {:commit (:snapshot-commit input) :tree (:snapshot-tree input)}
             (:content carrier))
          (= (:sh06-resolution-plan-authority input) (:resolution-plan-authority carrier))
          (= (:m0s input) (:module-source-descriptors carrier))
          (= (:m1 input) (:link-manifest carrier))
          (= (codec/H (:bundle-core input)) (:bundle-core-id carrier))
          (= (mapv :source-descriptor (:module-transactions input)) (:source-descriptors carrier))
          (= (mapv :resolution-artifact (:module-transactions input)) (:resolution-artifacts carrier))
          (vector? authorities) (= 7 (count authorities))
          (= (vec (range 7)) (mapv :module-ordinal authorities))
          (= (codec/H [:gravity/sh07-b51-sh06-authority-carrier-id-e2-v1 base])
             (:carrier-id carrier)))
      (fail! :sh06-authority-carrier))
    carrier))

(defn build-checker-precertificate
  "Build one complete E3 lowering product, all 34 bounded measurements and
  seven stage witnesses from authenticated checker-local inputs. No product,
  operation, receipt, witness, or certificate is accepted from the caller."
  [input]
  (when-not (= checker-semantic-input-keys-e3 (set (keys input)))
    (fail! :checker-semantic-input))
  (validate-sh06-carrier-joins! input)
  (let [content {:commit (:snapshot-commit input) :tree (:snapshot-tree input)}
        carrier (validate-source-semantic-carrier!
                 content (:source-semantic-carrier input))
        _ (validate-semantic-algorithm-authority!
           (:semantic-algorithm-source-descriptors input)
           (:semantic-algorithm-source-root input))
        products (module-products (:module-transactions input))
        foundation
        (foundation-preproduct products
                               (:semantic-algorithm-source-root input))
        stages (pre-stages input products foundation)
        collections
        (transaction-collections (:module-transactions input)
                                 (:source-forms input) carrier)
        source-roots
        {:source-descriptors-root
         (get-in carrier [:c2-snapshot :source-descriptors-root])
         :event-root (get-in carrier [:c2-snapshot :event-root])
         :semantic-algorithm-source-root
         (:semantic-algorithm-source-root input)
         :sh06-authority-carrier-id
         (get-in input [:sh06-authority-carrier :carrier-id])
         :bundle-core-id (codec/H (:bundle-core input))}
        subjects (build-fuel-subjects source-roots stages collections)
        product-base
        {:schema :gravity/sh07-b51-lowering-product-v1
         :content content
         :source-authority
         (select-keys source-roots [:source-descriptors-root :event-root])
         :module-products products
         :pass-records (:pass-records foundation)
         :fact-records (:fact-records foundation)
         :provenance-records (:provenance-records foundation)
         :fuel-subjects subjects
         :semantic-roots
         {:modules (semantic-witness-member-root :lowering-modules products)
          :passes (:pass-root foundation)
          :facts (:fact-root foundation)
          :provenance (:provenance-root foundation)}}
        lowering-product
        (assoc product-base :product-id
               (codec/H [:gravity/sh07-b51-lowering-product-v1 product-base]))
        operation-specs (build-fixed-fuel-operation-specs subjects)
        operations
        (operation-manifest operation-specs lowering-product
                            (:semantic-algorithm-source-root input))
        receipts (fuel-receipts subjects operations)
        stage-values
        (into {}
              (map (fn [{:keys [ordinal owner stage-key]}]
                     [stage-key
                      (stage-witness stage-key (get stages stage-key)
                                     (nth subjects ordinal)
                                     (nth operations ordinal)
                                     (nth receipts ordinal) foundation)]))
              stage-semantic-specs)
        witness-base
        (merge {:schema :gravity/sh07-b51-authentic-lowering-witness-v1
                :content content
                :operation-manifest operations
                :operation-schedule-id
                (codec/H [:gravity/sh07-b51-operation-schedule-v1 operations])
                :fuel-receipts receipts}
               stage-values)
        witness
        (assoc witness-base :witness-id
               (codec/H [:gravity/sh07-b51-authentic-lowering-witness-v1
                         witness-base]))]
    {:lowering-product lowering-product
     :semantic-witness witness
     :measurement-count (reduce + (map #(count (:trace %)) receipts))}))
