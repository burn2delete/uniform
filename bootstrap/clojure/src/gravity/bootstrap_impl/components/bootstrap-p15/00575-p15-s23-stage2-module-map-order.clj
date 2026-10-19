;; L2 source expression order, not a Map enumeration policy.  This context is
;; derived after original C5 verification and is never a public proof input.
(declare p15-s23-stage2-module-fail!)
(defn p15-s23-stage2-module-map-order [source]
  (let [c2 (get-in source [:artifact :sh05-macro-artifact :gravity-macro-boundary
                           :authenticated-sh04-artifact :c2-reader-artifact])
        records (:form-tree c2) index (into {} (map (juxt :form-id identity)) records)
        orders (atom {}) used (atom #{}) visits (atom 0)
        fail (fn [reason record]
               (p15-s23-stage2-module-fail! reason {:source-path (:source-path source)
                                                   :source-form-id (:form-id record)}))]
    (when-not (and (vector? records) (= (count records) (count index)))
      (fail :map-source-form-bijection nil))
    (letfn [(children [record]
              (let [ids (:children record)]
                (when-not (and (vector? ids) (= (count ids) (count (set ids)))
                               (every? #(= (:form-id record) (:parent-form-id (get index %))) ids))
                  (fail :map-source-child-link record))
                (mapv index ids)))
            (unwrap [record]
              (loop [record record seen #{}]
                (when (or (nil? record) (contains? seen (:form-id record)))
                  (fail :map-source-occurrence-missing record))
                (if (= :metadata-wrapper (:kind record))
                  (let [items (children record)]
                    (when-not (= 2 (count items)) (fail :map-source-metadata-shape record))
                    (recur (second items) (conj seen (:form-id record)))) record)))
            (walk [record value function path depth]
              (when (or (> depth 64) (> (swap! visits inc) 65536)) (fail :map-source-work-limit record))
              (let [record (unwrap record) items (children record) kind (:kind record)]
                (when (contains? @used (:form-id record)) (fail :map-source-occurrence-duplicate record))
                (swap! used conj (:form-id record))
                (when-not (= (:value record) value) (fail :map-source-value-mismatch record))
                (cond
                  (and (seq? value) (= 'quote (first value)))
                  (when-not (and (= 2 (count value))
                                 (or (and (= :abbreviation kind) (= :quote (:abbrev record)) (= 1 (count items)))
                                     (and (= :list kind) (= 2 (count items)))))
                    (fail :map-source-quote-shape record))
                  (map? value)
                  (let [entries (vec value) pairs (mapv vec (partition 2 items))
                        keys (mapv #(get-in % [0 :value]) pairs)
                        slots (zipmap (map first entries) (range)) order (mapv slots keys)]
                    (when-not (and (= :map kind) (even? (count items))
                                   (= (count keys) (count entries) (count (set keys)))
                                   (every? #(and (= :keyword (:kind %)) (keyword? (:value %))) (map first pairs))
                                   (every? some? order) (= (set order) (set (range (count entries)))))
                      (fail :map-source-distinct-keyword-bijection record))
                    (swap! orders assoc [function path]
                           {:source-form-id (:form-id record) :source-children (:children record)
                            :host-keys (mapv first entries) :entry-order order})
                    (doseq [[pair slot] (map vector pairs order)]
                      (walk (first pair) (get-in entries [slot 0]) function (conj path (* 2 slot)) (inc depth))
                      (walk (second pair) (get-in entries [slot 1]) function (conj path (inc (* 2 slot))) (inc depth))))
                  (set? value)
                  (let [values (vec (sort-by pr-str value)) slots (zipmap values (range))
                        positions (mapv #(get slots (:value %)) items)]
                    (when-not (and (= :set kind) (= (count items) (count values) (count (set positions)))
                                   (every? some? positions)) (fail :map-source-set-bijection record))
                    (doseq [[item slot] (map vector items positions)]
                      (walk item (get values slot) function (conj path slot) (inc depth))))
                  (or (seq? value) (vector? value))
                  (do (when-not (and (= (if (vector? value) :vector :list) kind) (= (count items) (count value)))
                        (fail :map-source-sequence-shape record))
                      (doseq [[slot item child] (map vector (range) items value)]
                        (walk item child function (conj path slot) (inc depth))))
                  (seq items) (fail :map-source-atomic-children record))))]
      (doseq [[name definition] (:functions source)]
        (let [syntaxes (filterv #(and (seq? (:form %)) (= 'def (first (:form %))) (= name (second (:form %)))) (:syntax source))
              syntax (first syntaxes) record (unwrap (get index (get-in syntax [:source :form-id])))
              items (children record) original (:value record)
              expected (list 'def name (apply list 'fn (:params definition) (:body definition)))
              trace (filterv #(and (= 'defn (:macro %)) (= (:syntax/id syntax) (:output-syntax-id %)))
                             (get-in source [:artifact :sh05-macro-artifact :macro-expansion-trace]))]
          (when-not (and (= 1 (count syntaxes) (count trace)) (= :list (:kind record))
                         (seq? original) (= 'defn (first original)) (= name (second original))
                         (= (:params definition) (nth original 2 nil))
                         (= (vec (drop 3 original)) (:body definition)) (= expected (:form syntax))
                         (= (count items) (+ 3 (count (:body definition)))))
            (fail :map-source-defn-transfer record))
          (doseq [[slot item value] (map vector (range) (drop 3 items) (:body definition))]
            (walk item value name [slot] 0)))))
    @orders))

(defn p15-s23-stage2-module-map-entries [module form path]
  (if (contains? module :p15-s23-stage2-map-order)
    (let [entries (vec form) row (get (:p15-s23-stage2-map-order module)
                                     [(:p15-s23-sh07-enclosing-function module) path])
          order (:entry-order row)]
      (when-not (and (map? row) (vector? order) (= (:host-keys row) (mapv first entries))
                     (= (count order) (count entries)) (= (set order) (set (range (count entries)))))
        (p15-s23-stage2-module-fail! :map-source-order-mismatch {:callsite path}))
      (mapv (fn [slot] (into [slot] (get entries slot))) order))
    (mapv (fn [slot [k v]] [slot k v]) (range) (sort-by (fn [[k v]] (pr-str [k v])) form))))

(defn p15-s23-stage2-seed-compile-collection [emitter module locals form]
  (let [rules (:collection-rules emitter) path (:p15-s23-sh07-callsite-path module [])
        child-module (fn [slot] (if (contains? module :p15-s23-stage2-map-order)
                                  (assoc module :p15-s23-sh07-callsite-path (conj path slot)) module))]
    (cond
      (vector? form)
      {:op (get-in rules [:vector :op] :vector-literal)
       :items (mapv (fn [index item]
                      (p15-s23-stage2-seed-compile-expr emitter
                       (if (contains? module :p15-s23-stage2-module-routes)
                         (assoc module :p15-s23-sh07-callsite-path (conj path index)) module)
                       locals item)) (range (count form)) form)}
      (map? form)
      {:op (get-in rules [:map :op] :map-literal)
       :entries (mapv (fn [[index k v]]
                        {:key (p15-s23-stage2-seed-compile-expr emitter (child-module (* 2 index)) locals k)
                         :value (p15-s23-stage2-seed-compile-expr emitter (child-module (inc (* 2 index))) locals v)})
                      (p15-s23-stage2-module-map-entries module form path))}
      (set? form)
      {:op (get-in rules [:set :op] :set-literal)
       :items (mapv (fn [index item] (p15-s23-stage2-seed-compile-expr emitter (child-module index) locals item))
                    (range) (sort-by pr-str form))}
      :else {:op (get-in rules [:literal :op] :literal) :value form})))
