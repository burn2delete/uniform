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

(defn p15-s23-stage2-module-map-reference-index [caller syntax definition]
  ;; Index actual C5 enumeration, not source evaluation order or name counts.
  ;; Scope/resolution/binding authority remains the original verified C5 rows.
  (let [refs (get-in caller [:request :references]) rows (atom []) work (atom 0)
        module {:p15-s23-stage2-map-order (:map-order caller)
                :p15-s23-sh07-enclosing-function (:name definition)}
        fail (fn [reason] (p15-s23-stage2-module-fail! reason {:function (:name definition)}))
        charge! (fn [amount] (when (> (swap! work + amount) 65536) (fail :map-reference-work-limit)))
        emit! (fn [symbol position path] (swap! rows conj {:symbol symbol :position position :path path}))]
    (charge! (count refs))
    (letfn [(walk [actual seed path position depth]
              (charge! 1)
              (when (> depth 64) (fail :map-reference-depth-limit))
              (cond
                (symbol? actual)
                (do (when-not (= actual seed) (fail :map-reference-value-mismatch)) (emit! actual position nil))
                (seq? actual)
                (let [op (first actual)]
                  (when-not (and (seq? seed) (= (count actual) (count seed)) (= op (first seed)) (symbol? op))
                    (fail :map-reference-call-shape))
                  (when (contains? '#{def defn fn defconst match try syntax-quote} op)
                    (fail :unsupported-map-reference-form))
                  (emit! op :operator path)
                  (cond
                    (= 'quote op) (when-not (= 2 (count actual)) (fail :map-reference-quote-shape))
                    (contains? '#{let loop} op)
                    (let [bindings (second actual) target (second seed)]
                      (charge! (count bindings))
                      (when-not (and (vector? bindings) (even? (count bindings)) (= (count bindings) (count target))
                                     (every? symbol? (take-nth 2 bindings))
                                     (= (vec (take-nth 2 bindings)) (vec (take-nth 2 target))))
                        (fail :unsupported-map-reference-bindings))
                      (doseq [slot (range 1 (count bindings) 2)]
                        (walk (get bindings slot) (get target slot) (when path (conj path 1 slot)) :expression (inc depth)))
                      (doseq [slot (range 2 (count actual))]
                        (walk (nth actual slot) (nth seed slot) (when path (conj path slot)) :expression (inc depth))))
                    :else (doseq [slot (range 1 (count actual))]
                            (walk (nth actual slot) (nth seed slot) (when path (conj path slot)) position (inc depth)))))
                (map? actual)
                (let [entries (if (map? seed) (vec seed) []) host-keys (mapv first entries) slots (zipmap host-keys (range))]
                  (charge! (* 2 (count actual)))
                  (when-not (and (map? seed) (= (count actual) (count seed)) (every? keyword? (keys actual))
                                 (= (set (keys actual)) (set host-keys))) (fail :map-reference-key-bijection))
                  (when path (p15-s23-stage2-module-map-entries module seed path))
                  (doseq [[key value] actual]
                    (let [slot (get slots key)]
                      (walk key key (when path (conj path (* 2 slot))) :expression (inc depth))
                      (walk value (get seed key) (when path (conj path (inc (* 2 slot)))) :expression (inc depth)))))
                (vector? actual)
                (do (when-not (and (vector? seed) (= (count actual) (count seed))) (fail :map-reference-vector-shape))
                    (doseq [slot (range (count actual))]
                      (walk (get actual slot) (get seed slot) (when path (conj path slot)) position (inc depth))))
                (set? actual)
                (do (when-not (and (set? seed) (= actual seed)) (fail :map-reference-set-shape))
                    ;; Imports here were already rejected by the inherited Set fence.
                    (doseq [item actual] (walk item item nil position (inc depth))))
                (not= actual seed) (fail :map-reference-value-mismatch)))]
      (let [form (:form syntax) function (when (seq? form) (nth form 2 nil)) params (:params definition)
            owned (filterv #(= (:span syntax) (:source-span (second %))) (mapv vector (range 1 (inc (count refs))) refs))]
        (when-not (and (seq? form) (= 'def (first form)) (= 3 (count form)) (= (:name definition) (second form))
                       (seq? function) (= 'fn (first function)) (= params (second function))
                       (vector? params) (every? symbol? params) (= (count (:body definition)) (- (count function) 2)))
          (fail :unsupported-map-reference-header))
        (charge! (+ (count params) (count owned)))
        (emit! 'def :operator nil) (emit! 'fn :operator nil)
        (doseq [[slot actual seed] (map vector (range) (drop 2 function) (:body definition))]
          (walk actual seed [slot] :expression 0))
        (when-not (= (count owned) (count @rows)) (fail :map-reference-owner-census))
        (doseq [[[ordinal reference] row] (map vector owned @rows)]
          (let [symbol (:symbol row) position (:position row)]
            (when-not (and (= symbol (:symbol reference)) (= position (:position reference))
                           (= (some-> (namespace symbol) clojure.core/symbol) (:qualifier reference))
                           (= (clojure.core/symbol (name symbol)) (:name reference))
                           (= (sh06-resolution-semantic-span (:span syntax) ordinal) (:semantic-span reference))
                           (= (:syntax-id reference) (reader-canonical-hash
                              {:domain :gravity/sh06-reference-syntax-v1 :owner-syntax-id (:syntax/id syntax)
                               :ordinal ordinal :symbol symbol :position position})))
              (fail :map-reference-occurrence-mismatch))))
        (let [selected (filterv #(and (= :operator (:position (second %))) (namespace (:symbol (second %))))
                                (mapv vector owned @rows))
              index (into {} (map (fn [[[ _ reference] row]] [(:path row) reference])) selected)]
          (when-not (and (= (count selected) (count index)) (every? vector? (keys index)))
            (fail :map-reference-path-bijection))
          index)))))

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
