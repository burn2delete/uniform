
(def stage-semantic-specs
  [{:ordinal 0 :owner :l1 :stage-key :l1
    :projection {:kind :exact-keys
                 :keys [:document :source-units :grammar-change]}}
   {:ordinal 1 :owner :c2 :stage-key :c2
    :projection {:kind :exact-keys
                 :keys [:snapshot :units :source-map :diagnostics
                        :literal-records :reader-extensions]}}
   {:ordinal 2 :owner :l2 :stage-key :l2
    :projection {:kind :exact-keys
                 :keys [:owner-selection :core-forms :evaluation-order-root
                        :effects-root :capabilities-root]}}
   {:ordinal 3 :owner :c6 :stage-key :c6
    :projection {:kind :exact-keys
                 :keys [:ast :syntax-core-map :desugaring-trace
                        :origins-facts-order]}}
   {:ordinal 4 :owner :boot2 :stage-key :boot2
    :projection {:kind :exact-keys
                 :keys [:source :compiler :backend :dependencies
                        :runtime-assumptions]}}
   {:ordinal 5 :owner :boot3 :stage-key :boot3
    :projection {:kind :boot3-module-exact-keys
                 :module-keys [:ordinal :module :input-artifacts
                               :output-artifacts :effects :facts]
                 :keys [:modules :profile :stage-advanced]}}
   {:ordinal 6 :owner :boot4 :stage-key :boot4
    :projection {:kind :exact-keys
                 :keys [:module-manifest :effects :capabilities
                        :fact-provenance]}}])

(def ^:private fuel-collection-specs
  {:l1 {:kind :l1-source-syntax-walk
        :keys [:module-forms :top-level-forms :fragment-forms :form-children
               :form-depths :origin-entries :metadata-entries :alias-records]}
   :c2 {:kind :c2-token-form-source-map-walk
        :keys [:module-carriers]}
   :l2 {:kind :l2-owner-effect-capability-walk
        :keys [:fragments :module-bindings :bindings :fragment-resolutions
               :module-resolutions :effects :capabilities :exports]}
   :c6 {:kind :c6-core-lowering-walk
        :keys [:module-core :resolved-core-carriers]}
   :boot2 {:kind :boot2-template-carrier-walk
           :keys [:template-carriers]}
   :boot3 {:kind :boot3-generated-digest-walk
           :keys [:generated-digest-carriers]}
   :boot4 {:kind :boot4-module-digest-walk
           :keys [:module-digest-requests]}})

(def ^:private operation-counter-keys
  {:l1 [:maximum-module-forms :maximum-top-level-forms
        :maximum-fragment-forms :maximum-form-children :maximum-form-depth
        :maximum-origin-entries :maximum-metadata-entries
        :maximum-alias-records]
   :c2 [:maximum-module-carrier-nodes :maximum-module-carrier-depth
        :maximum-module-carrier-width :maximum-module-scalar-bytes]
   :l2 [:maximum-fragments :maximum-module-bindings :maximum-bindings
        :maximum-fragment-resolutions :maximum-module-resolutions
        :maximum-effects :maximum-capabilities :maximum-exports]
   :c6 [:maximum-module-core-nodes :maximum-resolved-core-carrier-nodes
        :maximum-resolved-core-carrier-depth
        :maximum-resolved-core-carrier-width
        :maximum-resolved-core-scalar-bytes]
   :boot2 [:maximum-template-carrier-nodes :maximum-template-carrier-depth
           :maximum-template-carrier-width :maximum-template-scalar-bytes]
   :boot3 [:maximum-generated-digest-carrier-nodes
           :maximum-generated-digest-carrier-depth
           :maximum-generated-digest-carrier-width
           :maximum-generated-digest-scalar-bytes]
   :boot4 [:maximum-module-digest-requests]})

(def ^:private fuel-source-root-keys
  #{:source-descriptors-root :event-root :semantic-algorithm-source-root
    :sh06-authority-carrier-id :bundle-core-id})

(defn fixed-fuel-executable
  "Apply the seven architecture aliases and prove the exact 34-key checked-core
  projection before any fuel subject is constructed."
  [proof-contract-bounds checked-core-bounds]
  (when-not (and (map? proof-contract-bounds) (map? checked-core-bounds))
    (fail! :fixed-fuel-shape))
  (let [pairs (mapv (fn [[key value]]
                      [(get fixed-fuel-executable-aliases key key) value])
                    proof-contract-bounds)
        executable (into {} pairs)
        executable-keys (set (keys executable))]
    (when-not (and (= (count pairs) (count executable))
                   (= 34 (count executable))
                   (= executable
                      (select-keys checked-core-bounds executable-keys)))
      (fail! :fixed-fuel-executable-mismatch
             {:proof-key-count (count proof-contract-bounds)
              :executable-key-count (count executable)}))
    executable))

(defn- require-keys! [value expected reason]
  (when-not (and (map? value) (= (set expected) (set (keys value))))
    (fail! reason {:expected expected
                   :actual (when (map? value) (vec (keys value)))}))
  value)

(defn project-stage-semantic
  "Apply the sole two V9 pre-stage projection grammars."
  [stage-spec stage-prefix]
  (let [{:keys [kind keys module-keys]} (:projection stage-spec)]
    (case kind
      :exact-keys
      (do
        (doseq [key keys]
          (when-not (contains? stage-prefix key)
            (fail! :stage-semantic-missing-key {:key key})))
        (into (array-map) (map (fn [key] [key (get stage-prefix key)]) keys)))

      :boot3-module-exact-keys
      (do
        (doseq [key keys]
          (when-not (contains? stage-prefix key)
            (fail! :stage-semantic-missing-key {:key key})))
        {:modules
         (mapv (fn [module]
                 (doseq [key module-keys]
                   (when-not (contains? module key)
                     (fail! :stage-semantic-missing-module-key {:key key})))
                 (into (array-map)
                       (map (fn [key] [key (get module key)]) module-keys)))
               (:modules stage-prefix))
         :profile (:profile stage-prefix)
         :stage-advanced (:stage-advanced stage-prefix)})

      (fail! :stage-semantic-projection-kind {:kind kind}))))

(defn stage-semantic-root [stage-spec stage-prefix]
  (codec/H
   [:gravity/sh07-b51-fixed-fuel-stage-semantic-root-v1
    {:ordinal (:ordinal stage-spec)
     :owner (:owner stage-spec)
     :stage-key (:stage-key stage-spec)
     :semantic (project-stage-semantic stage-spec stage-prefix)}]))

(defn fuel-carrier-roots [owner kind collections]
  (into (array-map)
        (map (fn [[collection value]]
               [collection
                (codec/H
                 [:gravity/sh07-b51-fixed-fuel-carrier-root-v1
                  {:owner owner :kind kind :collection collection
                   :value value}])]))
        collections))

(defn build-fuel-subjects
  "Build all seven receipt-free fuel subjects in operation-owner order."
  [fuel-source-roots pre-stages collections-by-owner]
  (require-keys! fuel-source-roots fuel-source-root-keys
                 :fuel-source-roots-shape)
  (require-keys! pre-stages (mapv :stage-key stage-semantic-specs)
                 :pre-stages-shape)
  (require-keys! collections-by-owner (mapv :owner stage-semantic-specs)
                 :fuel-collections-shape)
  (mapv
   (fn [{:keys [ordinal owner stage-key] :as stage-spec}]
     (let [{kind :kind collection-keys :keys}
           (get fuel-collection-specs owner)
           stage-prefix (get pre-stages stage-key)
           collections (get collections-by-owner owner)]
       (require-keys! stage-prefix
                      (get-in stage-spec [:projection :keys])
                      :pre-stage-prefix-shape)
       (when (= :boot3-module-exact-keys
                (get-in stage-spec [:projection :kind]))
         (doseq [module (:modules stage-prefix)]
           (require-keys! module
                          (get-in stage-spec [:projection :module-keys])
                          :boot3-pre-stage-module-shape)))
       (require-keys! collections collection-keys
                      :fuel-subject-collections-shape)
       (let [ordered-collections
             (into (array-map)
                   (map (fn [key] [key (get collections key)]))
                   collection-keys)
             subject-base
             {:ordinal ordinal
              :owner owner
              :kind kind
              :source-roots fuel-source-roots
              :collections ordered-collections
              :collection-roots
              (fuel-carrier-roots owner kind ordered-collections)
              :stage-semantic-root
              (stage-semantic-root stage-spec stage-prefix)}]
         (assoc subject-base :subject-id
                (codec/H
                 [:gravity/sh07-b51-fixed-fuel-subject-v1 subject-base])))))
   stage-semantic-specs))

(defn build-fixed-fuel-operation-specs
  "Attach each exact seven-operation counter program to its prior subject."
  [fuel-subjects]
  (when-not (and (= 7 (count fuel-subjects))
                 (= (mapv :ordinal fuel-subjects) (vec (range 7)))
                 (= (mapv :owner fuel-subjects)
                    (mapv :owner stage-semantic-specs)))
    (fail! :fuel-subject-order))
  (mapv (fn [{:keys [ordinal owner kind subject-id]}]
          {:ordinal ordinal :owner owner :kind kind :subject-id subject-id
           :counter-keys (get operation-counter-keys owner)})
        fuel-subjects))

(defn q-form
  "Select exactly one authenticated Q form by form ID."
  [q form-id]
  (unique-where! #(= form-id (:form-id %)) (:forms q) :owner-q-form))

(defn owning-top-level-form
  "Follow authenticated parent-form IDs to the unique source top-level form."
  [q form]
  (loop [current form seen #{}]
    (let [form-id (:form-id current)]
      (when (contains? seen form-id)
        (fail! :owner-parent-cycle {:form-id form-id}))
      (if-let [parent-id (:parent-form-id current)]
        (recur (q-form q parent-id) (conj seen form-id))
        (do
          (when-not (= 1 (count (filter #(= form-id %)
                                        (:top-level-form-ids q))))
            (fail! :owner-top-level-membership {:form-id form-id}))
          current)))))

(defn top-level-form-index [q form]
  (let [top-id (:form-id (owning-top-level-form q form))
        matches (keep-indexed #(when (= top-id %2) %1)
                              (:top-level-form-ids q))]
    (when-not (= 1 (count matches))
      (fail! :owner-top-level-index {:form-id top-id}))
    (first matches)))
