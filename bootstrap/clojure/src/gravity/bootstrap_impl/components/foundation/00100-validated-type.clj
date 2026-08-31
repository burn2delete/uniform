

(defn validated-type?
  [type-name]
  (and (string? type-name) (str/starts-with? type-name "Validated[")))

(defn narrowed-type
  [scrutinee-type pattern]
  (case (pattern-kind pattern)
    :wildcard scrutinee-type
    :binding scrutinee-type
    :literal (or (literal-pattern-type pattern) "Dynamic")
    :vector "Vector"
    :map "Map"
    :constructor (or (constructor-name pattern) "Constructor")
    "Dynamic"))

(defn all-registered-effects
  []
  (set (keys effect-registry)))

(defn all-registered-capabilities
  []
  (set (keys provider-specs)))

(defn permissive-pattern-context
  [ctx]
  (atom (assoc @ctx
               :declared-effects (all-registered-effects)
               :declared-capabilities (all-registered-capabilities))))

(defn guard-effects-illegal?
  [ctx guard-fact]
  (let [declared-effects (:declared-effects @ctx)
        declared-capabilities (:declared-capabilities @ctx)
        denied-effects (get profile-denied-effects (:profile @ctx) #{})]
    (or (some denied-effects (:effects guard-fact))
        (some #(not (contains? declared-effects %)) (:effects guard-fact))
        (some #(not (contains? declared-capabilities %)) (:capabilities guard-fact)))))

(defn constructor-coverage
  [patterns]
  (set (keep constructor-name patterns)))

(defn closed-result-match?
  [patterns]
  (seq (set/intersection #{"Ok" "Err"} (constructor-coverage patterns))))

(defn exhaustive-result-match?
  [patterns]
  (set/subset? #{"Ok" "Err"} (constructor-coverage patterns)))

(defn match-conformance-fixture
  [checker-state]
  (let [covered (->> (concat (mapcat :pattern-families (:match-decision-trees checker-state))
                             (when (seq (:branch-effect-summary checker-state)) [:guard])
                             (when (seq (:pattern-schema-validation-links checker-state)) [:schema])
                             (when (seq (:pattern-ownership-facts checker-state)) [:linear]))
                     (remove nil?)
                     set
                     (sort-by name)
                     vec)
        covered-set (set covered)
        missing (vec (remove covered-set l7-required-pattern-families))]
    {:required-families l7-required-pattern-families
     :covered-families covered
     :missing-families missing
     :status (if (empty? missing) :complete :incomplete)}))

(defn method-contract-value
  [items key default]
  (let [pairs (partition 2 2 nil items)]
    (or (some (fn [[k v]]
                (when (= key k) v))
              pairs)
        default)))

(defn parse-method-contract
  [protocol-name method-form]
  (let [[method-name params & tail] method-form
        return-type (if-let [marker-pos (first (keep-indexed (fn [idx value]
                                                               (when (= ':- value) idx))
                                                             tail))]
                      (type-form-name (nth tail (inc marker-pos)))
                      "Dynamic")
        metadata-tail (drop-while #(not (keyword? %)) tail)]
    {:protocol protocol-name
     :method method-name
     :arity (count params)
     :receiver-position 0
     :parameter-types (vec (repeat (count params) "Dynamic"))
     :return-type return-type
     :effects (method-contract-value metadata-tail :effects #{})
     :capabilities (method-contract-value metadata-tail :capabilities #{})
     :profile-restrictions #{}
     :default-implementation nil
     :stability :stage0
     :source-form method-form}))

(defn protocol-table-from-forms
  [forms]
  (->> forms
       (keep (fn [form]
               (when (and (seq? form) (= 'defprotocol (first form)))
                 (let [protocol-name (second form)
                       methods (mapv #(parse-method-contract protocol-name %) (drop 2 form))]
                   {:protocol protocol-name
                    :methods methods
                    :source-form form}))))
       vec))

(defn method-signature-records
  [protocol-table]
  (vec (mapcat :methods protocol-table)))

(defn find-method-contract
  [protocol-table protocol-name method-name]
  (some (fn [protocol]
          (when (= protocol-name (:protocol protocol))
            (some #(when (= method-name (:method %)) %) (:methods protocol))))
        protocol-table))

(defn implementation-table-from-forms
  [forms protocol-table]
  (->> forms
       (keep (fn [form]
               (when (and (seq? form) (= 'extend (first form)))
                 (let [type-name (second form)
                       protocol-name (nth form 2)
                       methods (drop 3 form)]
                   (mapv (fn [method-form]
                           (let [method-name (first method-form)
                                 contract (find-method-contract protocol-table protocol-name method-name)]
                             {:type type-name
                              :protocol protocol-name
                              :method method-name
                              :arity (count (second method-form))
                              :effects (or (:effects contract) #{})
                              :capabilities (or (:capabilities contract) #{})
                              :dispatch-mode :direct
                              :implementation (symbol (str type-name) (str method-name))
                              :source-form method-form}))
                         methods)))))
       (mapcat identity)
       vec))

(defn multimethod-tables-from-forms
  [forms]
  (->> forms
       (keep (fn [form]
               (when (and (seq? form) (= 'defmulti (first form)))
                 (let [name (second form)
                       metadata (if (map? (nth form 2 nil)) (nth form 2) {})]
                   {:multimethod name
                    :dispatch (:dispatch metadata)
                    :closed-cases (or (:closed-cases metadata) #{})
                    :dispatch-mode :multimethod
                    :source-form form}))))
       vec))