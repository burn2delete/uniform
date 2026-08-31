

(defn type-environment
  [typed-roots]
  (->> typed-roots
       (keep (fn [fact]
               (when-let [name (:name fact)]
                 {:name name
                  :type (:type fact)
                  :node-id (:node-id fact)})))
       vec))

(defn distinct-records
  [records]
  (distinct-by-pr-str records))

(defn covered-type-categories
  [coverage]
  (->> coverage
       (map :category)
       set
       (sort-by name)
       vec))

(defn type-conformance-fixture
  [coverage]
  (let [covered (covered-type-categories coverage)
        covered-set (set covered)
        missing (vec (remove covered-set l5-required-type-categories))]
    {:required-categories l5-required-type-categories
     :covered-categories covered
     :missing-categories missing
     :status (if (empty? missing) :complete :incomplete)}))

(defn mir-type-preservation-records
  [type-facts]
  (mapv (fn [fact]
          {:core-node-id (:node-id fact)
           :mir-value-id (str "stage0-mir-value-" (:node-id fact))
           :type (:type fact)
           :source-kind (:source-kind fact)
           :source-span (:source-span fact)
           :preserved? true})
        type-facts))

(def l6-required-effect-families
  [:pure :io :filesystem :network :database :memory-allocation :raw-memory
   :mmio :interrupt :resource :time :random :async :generator
   :resumable-error :ffi :reflection :dynamic :compiler :build :secrets
   :shell :workflow :ai :unsafe-island :handler])

(defn effect-families
  [effect]
  (let [entry (effect-registry-entry effect)]
    (cond-> [(:family entry)]
      (= :unsafe-island (:kind entry)) (conj :unsafe-island))))

(defn covered-effect-families
  [effect-facts build-log handled-table]
  (let [effect-families (->> effect-facts
                             (mapcat :effects)
                             (mapcat effect-families))
        build-families (map (fn [_] :build) build-log)
        handled-families (map (fn [_] :handler) handled-table)
        pure-family (when (empty? (mapcat :effects effect-facts)) [:pure])]
    (->> (concat effect-families build-families handled-families pure-family [:pure])
         (remove nil?)
         set
         (sort-by name)
         vec)))

(defn effect-conformance-fixture
  [effect-facts build-log handled-table]
  (let [covered (covered-effect-families effect-facts build-log handled-table)
        covered-set (set covered)
        missing (vec (remove covered-set l6-required-effect-families))]
    {:required-families l6-required-effect-families
     :covered-families covered
     :missing-families missing
     :status (if (empty? missing) :complete :incomplete)}))

(defn effect-registry-snapshot
  []
  (->> effect-registry
       (map (fn [[effect entry]]
              (merge {:effect effect}
                     (select-keys entry [:family :kind :requires-capability
                                         :requires-build-grant :capability
                                         :nondeterministic :replay-record
                                         :profiles]))))
       (sort-by (comp str :effect))
       vec))

(defn mir-effect-annotation-records
  [effect-facts]
  (mapcat (fn [fact]
            (map (fn [effect]
                   {:core-node-id (:node-id fact)
                    :mir-effect-id (str "stage0-mir-effect-" (:node-id fact) "-" (name effect))
                    :effect effect
                    :source-kind (:source-kind fact)
                    :source-span (:source-span fact)
                    :preserved? true})
                 (:effects fact)))
          effect-facts))