

(defn- sh07-core-source-occurrence-context
  [resolution-artifact syntaxes]
  ;; Reuse C2 occurrence alignment before host-tree recursion. Values do not
  ;; establish Character kind; the original form and decoding record do.
  (let [source-path (sh07-core-source-path-from-resolution resolution-artifact)
        c2 (get-in resolution-artifact [:sh05-macro-artifact :gravity-macro-boundary
                                        :authenticated-sh04-artifact :c2-reader-artifact])
        sources (:form-tree c2) source-index (into {} (map (juxt :form-id identity)) sources)
        literals (group-by :form-id (:literal-decoding-records c2))
        fail (fn [reason source]
               (throw (ex-info "SH-07 source occurrence projection is not bijective"
                        {:id "C6-ORIGIN" :rule "C6-ORIGIN" :stage :core-lowering :reason reason
                         :source-path source-path :source-span (:span source) :source-form-id (:form-id source)})))]
    (when-not (and (vector? sources) (= (count sources) (count source-index)))
      (fail :map-source-form-id-bijection nil))
    (letfn [(children! [source]
              (let [ids (:children source)]
                (when-not (and (vector? ids) (= (count ids) (count (set ids)))
                               (every? #(= (:form-id source) (:parent-form-id (get source-index %))) ids))
                  (fail :map-source-child-link source))
                (mapv source-index ids)))
            (unwrap [source]
              (loop [source source seen #{}]
                (when (or (nil? source) (contains? seen (:form-id source)))
                  (fail :map-source-occurrence-missing source))
                (if (= :metadata-wrapper (:kind source))
                  (let [children (children! source)]
                    (when-not (= 2 (count children)) (fail :map-source-metadata-shape source))
                    (recur (second children) (conj seen (:form-id source))))
                  source)))
            (character! [source value]
              (let [records (get literals (:form-id source)) record (first records)
                    codepoint (if (char? value) (int value) (when (map? value) (:codepoint value)))
                    expected (when (and (integer? codepoint) (<= 0 codepoint 0x10ffff)
                                        (not (<= 0xd800 codepoint 0xdfff)))
                               (sh03-reader-host-character-value! source-path {:codepoint codepoint}))]
                (when-not (and (= [] (:children source)) (= 1 (count records))
                               (= :character (:kind record))
                               (= value expected (:value source) (:decoded record))
                               (= (:raw source) (:raw record)) (= (:span source) (:span record)))
                  (fail :character-c2-occurrence-mismatch source))))]
      (into {}
        (map
          (fn [syntax]
            (let [occurrences (atom {}) orders (atom {}) used (atom #{})
                  remember! (fn [source path]
                              (when (contains? @used (:form-id source))
                                (fail :map-source-occurrence-duplicate source))
                              (swap! used conj (:form-id source))
                              (swap! occurrences assoc path source))]
              (letfn [(walk [source value path inert?]
                        (let [source (unwrap source) kind (:kind source)
                              children (children! source)]
                          (remember! source path)
                          (if (= :character kind)
                            (character! source value)
                            (do
                              (when-not (or (= kind (sh07-core-value-kind value))
                                            (contains? #{:abbreviation :tagged-literal} kind))
                                (fail :map-source-value-or-kind source))
                              (when (and (not (coll? value)) (not= (:value source) value))
                                (fail :map-source-projected-literal source))
                              (cond
                                (and (seq? value) (= 'quote (first value)) (= 2 (count value)))
                                (do
                                  (if (= :abbreviation kind)
                                    (when-not (and (= :quote (:abbrev source)) (= 1 (count children)))
                                      (fail :map-source-quote-shape source))
                                    (do (when-not (= 2 (count children)) (fail :map-source-quote-shape source))
                                        (walk (first children) 'quote (conj path 0) inert?)))
                                  (walk (last children) (second value) (conj path 1) true))
                                (map? value)
                                (let [entries (vec value) pairs (mapv vec (partition 2 children))
                                      slots (into {} (map-indexed (fn [index [key _]] [key index])) entries)
                                      matched (mapv #(get slots (:value (first %)) ::missing) pairs)]
                                  (when-not (and (even? (count children))
                                                 (if inert? true
                                                   (and (= (count children) (* 2 (count entries)))
                                                        (not-any? #{::missing} matched)
                                                        (= (count matched) (count (set matched))))))
                                    (fail :map-source-entry-bijection source))
                                  (swap! orders assoc path (when-not inert? matched))
                                  (doseq [[index [key value]] (map-indexed vector entries)]
                                    (let [pair (last (filter #(and (= key (:value (first %)))
                                                                 (= value (:value (second %)))) pairs))]
                                      (when-not pair (fail :map-source-entry-bijection source))
                                      (walk (first pair) key (conj path (* 2 index)) inert?)
                                      (walk (second pair) value (conj path (inc (* 2 index))) inert?))))
                                (set? value)
                                (let [values (sh07-core-children value) slots (zipmap values (range))
                                      matched (mapv #(get slots (:value %) ::missing) children)]
                                  (when-not (and (= (count children) (count values) (count (set matched)))
                                                 (not-any? #{::missing} matched))
                                    (fail :map-source-set-bijection source))
                                  (swap! orders assoc path (when-not inert? matched))
                                  (doseq [[child index] (map vector children matched)]
                                    (walk child (get values index) (conj path index) inert?)))
                                (= :abbreviation kind)
                                (do (when-not (and (= 1 (count children)) (= 2 (count value)) (seq? value)
                                                   (= (first value) (get {:quote 'quote :syntax-quote 'syntax-quote :unquote 'unquote
                                                                         :splice-unquote 'splice-unquote :deref 'deref} (:abbrev source))))
                                      (fail :map-source-abbreviation-shape source))
                                    (walk (first children) (second value) (conj path 1) inert?))
                                (or (vector? value) (seq? value))
                                (do (when-not (= (count children) (count value)) (fail :map-source-sequence-shape source))
                                    (doseq [[index child raw] (map vector (range) children value)]
                                      (walk child raw (conj path index) inert?)))
                                :else nil)))))]
                (let [source (unwrap (get source-index (get-in syntax [:source :form-id]))) value (:form syntax)]
                  (if (and (seq? (:value source)) (= 'defn (first (:value source))))
                    (let [children (children! source) fn-value (nth value 2 nil)
                          trace (filter #(and (= 'defn (:macro %)) (= (:syntax/id syntax) (:output-syntax-id %)))
                                        (get-in resolution-artifact [:sh05-macro-artifact :macro-expansion-trace]))]
                      (when-not (and (= 1 (count trace)) (seq? value) (= 'def (first value)) (= 3 (count value))
                                     (seq? fn-value) (= 'fn (first fn-value)) (= (dec (count children)) (count fn-value)))
                        (fail :map-source-defn-transfer source))
                      (remember! source [])
                      (walk (get children 1) (second value) [1] false)
                      (doseq [index (range 2 (count children))]
                        (walk (get children index) (nth fn-value (dec index)) [2 (dec index)] false)))
                    (walk source value [] false))))
              [(:sh07/root-syntax-id syntax) {:sources @occurrences :collection-orders @orders}]))
          syntaxes)))))

(defn sh07-core-build-form-tree
  [source-path source-revision-id decimal-evidence syntax trace]
  (let [root-id (:sh07/root-syntax-id syntax)
        source-span (sh07-core-semantic-span (:span syntax))
        trace? (and trace (= root-id (:output-def-syntax-id trace)))
        records (atom [])]
    (letfn [(walk [value path parent-form-id]
              (let [source (get-in syntax [:sh07/source-occurrences :sources path])
                    kind (if (= :character (:kind source)) :character (sh07-core-value-kind value))
                    syntax-id
                    (sh07-core-projected-syntax-id
                     source-revision-id root-id path kind
                     (when trace? trace))
                    form-id
                    (sh07-core-projected-form-id
                     source-revision-id root-id path kind)
                    children (if (= :character kind) [] (sh07-core-children value))
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
                      decimal-evidence value (:sh07/source-occurrences syntax))
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
  ;; Existing host-path identities stay fixed. C2 owns evaluated collection order;
  ;; inert quoted containers retain their materialized value behavior.
  (let [contexts (sh07-core-source-occurrence-context resolution-artifact syntaxes)
        records (vec (mapcat :records trees)) projected (into {} (map (juxt :form-id identity)) records)
        reordered (atom {})
        fail (fn [reason target]
               (throw (ex-info "SH-07 projected source occurrence does not match C2"
                        {:id "C6-ORIGIN" :rule "C6-ORIGIN" :stage :core-lowering :reason reason
                         :projected-form-id (:form-id target)})))]
    (when-not (and (= (count records) (count projected)) (= (count syntaxes) (count trees)))
      (fail :map-source-form-id-bijection nil))
    (doseq [[syntax tree] (map vector syntaxes trees)]
      (let [context (get contexts (:sh07/root-syntax-id syntax))]
        (letfn [(walk [id path seen]
                  (let [target (get projected id) source (get (:sources context) path)
                        children (:child-form-ids target)]
                    (when-not (and target (not (contains? seen id)) (vector? children)
                                   (= (count children) (count (set children)))
                                   (every? #(= id (:parent-form-id (get projected %))) children))
                      (fail :map-projected-child-link target))
                    (when (= :character (:kind source))
                      (when-not (and (= :character (:kind target)) (= [] children) (= (:value source) (:value target)))
                        (fail :character-c2-occurrence-mismatch target)))
                    (when-let [reason (sh07-core-projected-source-mismatch source target)] (fail reason target))
                    (when (contains? #{:map :set} (:kind target))
                      (when-not (= (:kind target) (:kind source)) (fail :map-source-value-or-kind target))
                      (when-let [order (get (:collection-orders context) path)]
                        (let [width (if (= :map (:kind target)) 2 1)]
                          (when-not (= (count children) (* width (count order))) (fail :map-source-entry-bijection target))
                          (swap! reordered assoc id (vec (mapcat #(subvec children (* width %) (+ width (* width %))) order))))))
                    (doseq [[index child] (map-indexed vector children)]
                      (walk child (conj path index) (conj seen id)))))]
          (walk (get-in tree [:root :form-id]) [] #{}))))
    (mapv (fn [tree]
            (let [change (fn [record]
                           (if-let [ids (get @reordered (:form-id record))]
                             (assoc record :child-form-ids ids) record))]
              {:root (change (:root tree)) :records (mapv change (:records tree))})) trees)))

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
