

(defn sh07-core-build-form-tree
  [source-path source-revision-id decimal-evidence syntax trace]
  (let [root-id (:sh07/root-syntax-id syntax)
        source-span (sh07-core-semantic-span (:span syntax))
        trace? (and trace (= root-id (:output-def-syntax-id trace)))
        records (atom [])]
    (letfn [(walk [value path parent-form-id]
              (let [kind (sh07-core-value-kind value)
                    syntax-id
                    (sh07-core-projected-syntax-id
                     source-revision-id root-id path kind
                     (when trace? trace))
                    form-id
                    (sh07-core-projected-form-id
                     source-revision-id root-id path kind)
                    children (sh07-core-children value)
                    child-records
                    (mapv
                     (fn [index child]
                       (walk child (conj path index) form-id))
                     (range) children)
                    generated-origin
                    (cond
                      (and trace? (empty? path))
                      [{:origin-id (:def-generated-origin-id trace)
                        :kind :macro-expansion
                        :from-syntax-id (:input-syntax-id trace)
                        :role :introduced-def}]
                      (and trace? (= path [2]))
                      [{:origin-id (:fn-generated-origin-id trace)
                        :kind :macro-expansion
                        :from-syntax-id (:input-syntax-id trace)
                        :role :introduced-fn}]
                      :else [])
                    record
                    {:form-id form-id
                     :syntax-id syntax-id
                     :kind kind
                     :value
                     (sh07-core-neutral-value
                      source-path source-revision-id root-id path
                      decimal-evidence value)
                     :parent-form-id parent-form-id
                     :child-form-ids (mapv :form-id child-records)
                     :source-span source-span
                     :origin-chain []
                     :generated-origin generated-origin
                     :metadata
                     (sh05-path-neutral-semantic-value
                      (or (meta value) {}))
                     :scope-id nil}]
                (swap! records conj record)
                record))]
      (let [root (walk (:form syntax) [] nil)]
        {:root root :records @records}))))

(defn sh07-core-source-map-order
  [resolution-artifact syntaxes trees]
  ;; Keep host-path IDs, decimal occurrences and binding/reference coordinates.
  ;; Only map parent child lists change; C2 owns their textual pair order.
  (let [records (vec (mapcat :records trees))
        maps (filterv #(= :map (:kind %)) records)]
    (if (empty? maps) trees
      (let [source-path (sh07-core-source-path-from-resolution resolution-artifact)
            c2 (get-in resolution-artifact [:sh05-macro-artifact :gravity-macro-boundary
                                            :authenticated-sh04-artifact :c2-reader-artifact])
            sources (:form-tree c2) source-index (into {} (map (juxt :form-id identity)) sources)
            projected (into {} (map (juxt :form-id identity)) records)
            reordered (atom {})
            fail (fn [reason source target]
                   (throw (ex-info "SH-07 map source occurrence projection is not bijective"
                            {:id "C6-ORIGIN" :rule "C6-ORIGIN" :stage :core-lowering :reason reason
                             :source-path source-path :source-span (:span source)
                             :source-form-id (:form-id source) :projected-form-id (:form-id target)})))]
        (when-not (and (vector? sources) (= (count sources) (count source-index))
                       (= (count records) (count projected)) (= (count syntaxes) (count trees)))
          (fail :map-source-form-id-bijection nil nil))
        (letfn [(children! [source target]
                  (let [ids (:children source)]
                    (when-not (and (vector? ids) (= (count ids) (count (set ids)))
                                   (every? #(= (:form-id source) (:parent-form-id (get source-index %))) ids))
                      (fail :map-source-child-link source target))
                    (mapv source-index ids)))
                (inert! [root-id]
                  (loop [pending [root-id] seen #{}]
                    (when-let [id (peek pending)]
                      (when (or (contains? seen id) (not (contains? projected id)))
                        (fail :map-projected-child-link nil (get projected id)))
                      (let [record (get projected id)]
                        (when (= :map (:kind record))
                          (swap! reordered assoc id (:child-form-ids record)))
                        (recur (into (pop pending) (:child-form-ids record)) (conj seen id))))))
                (walk [source target-id value]
                  (let [target (get projected target-id)]
                    (when-not (and (map? source) (map? target))
                      (fail :map-source-occurrence-missing source target))
                    (if (= :metadata-wrapper (:kind source))
                      (let [children (children! source target)]
                        (when-not (= 2 (count children)) (fail :map-source-metadata-shape source target))
                        (walk (second children) target-id value))
                      (do
                        (when-not (and (= (sh07-core-value-kind value) (:kind target))
                                       (or (contains? #{:abbreviation :tagged-literal} (:kind source))
                                           (= (:kind source) (:kind target))))
                          (fail :map-source-value-or-kind source target))
                        (let [source-children (children! source target)
                              target-ids (:child-form-ids target)]
                          (when-not (and (vector? target-ids) (= (count target-ids) (count (set target-ids)))
                                         (every? #(= target-id (:parent-form-id (get projected %))) target-ids))
                            (fail :map-projected-child-link source target))
                          (when (and (not (coll? value))
                                     (not (and (= (:value source) value)
                                          (if (instance? java.math.BigDecimal value)
                                            (and (= :gravity/arbitrary-decimal-literal (:kind (:value target)))
                                                 (= (.scale ^java.math.BigDecimal value) (:scale (:value target)))
                                                 (= (str (.unscaledValue ^java.math.BigDecimal value)) (:unscaled-value (:value target))))
                                            (= value (:value target))))))
                            (fail :map-source-projected-literal source target))
                          (cond
                            (and (seq? value) (= 'quote (first value)) (= 2 (count value)))
                            (do (when-not (= 2 (count target-ids)) (fail :map-source-quote-shape source target))
                                (if (= :abbreviation (:kind source))
                                  (when-not (and (= :quote (:abbrev source)) (= 1 (count source-children)))
                                    (fail :map-source-quote-shape source target))
                                  (do (when-not (= 2 (count source-children)) (fail :map-source-quote-shape source target))
                                      (walk (first source-children) (first target-ids) 'quote)))
                                ;; Quoted DATA keeps host materialization, including duplicate keys.
                                (inert! (get target-ids 1)))

                            (map? value)
                            (let [entries (vec value)
                                  pairs (mapv vec (partition 2 source-children))
                                  ;; Index keys only. Values are joined once by their paired child occurrence.
                                  slots (into {} (map-indexed (fn [index [key _]] [key index])) entries)
                                  matched (mapv (fn [[key child]]
                                                  (let [index (get slots (:value key) ::missing)]
                                                    (when (= ::missing index)
                                                      (fail :map-source-entry-bijection source target))
                                                    index)) pairs)]
                              (when-not (and (even? (count source-children))
                                             (= (count source-children) (count target-ids) (* 2 (count entries)))
                                             (= (count matched) (count (set matched))))
                                (fail :map-source-entry-bijection source target))
                              (when (contains? @reordered target-id) (fail :map-source-occurrence-duplicate source target))
                              (swap! reordered assoc target-id
                                     (vec (mapcat #(subvec target-ids (* 2 %) (+ 2 (* 2 %))) matched)))
                              (doseq [[[key child] index] (map vector pairs matched)]
                                (let [[raw-key raw-child] (get entries index)]
                                  (walk key (get target-ids (* 2 index)) raw-key)
                                  (walk child (get target-ids (inc (* 2 index))) raw-child))))

                            (set? value)
                            (let [values (sh07-core-children value)
                                  ;; Same bounded semantic member coordinates as existing set projection.
                                  slots (zipmap values (range))
                                  matched (mapv (fn [child]
                                                  (let [index (get slots (:value child) ::missing)]
                                                    (when (= ::missing index) (fail :map-source-set-bijection source target))
                                                    index)) source-children)]
                              (when-not (= (count source-children) (count values) (count target-ids) (count (set matched)))
                                (fail :map-source-set-bijection source target))
                              (doseq [[child index] (map vector source-children matched)]
                                (walk child (get target-ids index) (get values index))))

                            (= :abbreviation (:kind source))
                            (do (when-not (and (= 1 (count source-children)) (= 2 (count target-ids)) (seq? value)
                                              (= (first value) (get {:quote 'quote :syntax-quote 'syntax-quote :unquote 'unquote
                                                                    :splice-unquote 'splice-unquote :deref 'deref} (:abbrev source))))
                                  (fail :map-source-abbreviation-shape source target))
                                (walk (first source-children) (get target-ids 1) (second value)))

                            (or (vector? value) (seq? value))
                            (do (when-not (= (count source-children) (count target-ids) (count value))
                                  (fail :map-source-sequence-shape source target))
                                (doseq [[child id raw] (map vector source-children target-ids value)] (walk child id raw)))

                            :else nil))))))
                (root! [syntax tree]
                  (let [source (get source-index (get-in syntax [:source :form-id]))
                        source (loop [item source]
                                 (if (= :metadata-wrapper (:kind item))
                                   (let [children (children! item (:root tree))]
                                     (when-not (= 2 (count children)) (fail :map-source-metadata-shape item (:root tree)))
                                     (recur (second children))) item))
                        value (:form syntax) target (:root tree)]
                    (if (and (seq? (:value source)) (= 'defn (first (:value source))))
                      (let [children (children! source target)
                            fn-target (get projected (get (:child-form-ids target) 2))
                            fn-value (nth value 2 nil)
                            trace (filter #(and (= 'defn (:macro %)) (= (:syntax/id syntax) (:output-syntax-id %)))
                                          (get-in resolution-artifact [:sh05-macro-artifact :macro-expansion-trace]))]
                        (when-not (and (= 1 (count trace)) (seq? value) (= 'def (first value))
                                       (seq? fn-value) (= 'fn (first fn-value))
                                       (= 3 (count (:child-form-ids target)))
                                       (= (dec (count children)) (count (:child-form-ids fn-target))))
                          (fail :map-source-defn-transfer source target))
                        (walk (get children 1) (get (:child-form-ids target) 1) (second value))
                        (doseq [index (range 2 (count children))]
                          (walk (get children index) (get (:child-form-ids fn-target) (dec index)) (nth fn-value (dec index)))))
                      (walk source (:form-id target) value))))]
          (doseq [[syntax tree] (map vector syntaxes trees)] (root! syntax tree))
          (when-not (= (set (map :form-id maps)) (set (keys @reordered)))
            (fail :map-source-projected-coverage nil nil))
          (mapv (fn [tree]
                  (let [change (fn [record]
                                 (if-let [ids (get @reordered (:form-id record))]
                                   (assoc record :child-form-ids ids) record))]
                    {:root (change (:root tree)) :records (mapv change (:records tree))})) trees))))))

(defn sh07-core-parameter-binding-paths
  [parameters parameter-path]
  (loop [remaining (seq (if (sequential? parameters) parameters []))
         index 0
         result []]
    (if (empty? remaining)
      result
      (let [item (first remaining)]
        (cond
          (= item ':-)
          (recur (nnext remaining) (+ index 2) result)

          (= item '&)
          (recur (next remaining) (inc index) result)

          (symbol? item)
          (recur
           (next remaining)
           (inc index)
           (conj result
                 {:name item
                  :path (conj parameter-path index)}))

          :else
          (recur (next remaining) (inc index) result))))))
