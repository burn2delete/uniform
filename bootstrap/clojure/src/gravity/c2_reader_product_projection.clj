(ns gravity.c2-reader-product-projection
  "Pure hosted C2 projections over already-produced reader products.

  This compatibility leaf constructs syntax seeds, deferred literal views,
  top-level product views, partial capability facts, metadata overrides, and
  extension invocation records. It does not read source, construct or
  authenticate canonical C2/SH03 products, own policy/options, use caches, or
  grant proof, self-hosting, attestation, or release authority."
  (:require [gravity.c2-reader-product-projection.policy :as policy]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private ^:dynamic *bypass-next-operation-keys* #{})

(def ^:private operation-keys policy/operation-keys)
(def ^:private namespace-contract policy/namespace-contract)

(defn with-operations [operations thunk]
  (policy/validate-operations! operations)
  (when-not (fn? thunk)
    (throw (ex-info "C2 reader-product projection thunk must be a function"
                    {:thunk thunk})))
  (binding [*operations* operations] (thunk)))

(defn call-entrypoint-body [operation-key operation args]
  (when-not (contains? operation-keys operation-key)
    (throw (ex-info "C2 reader-product projection entrypoint key is unknown"
                    {:operation operation-key})))
  (when-not (fn? operation)
    (throw (ex-info "C2 reader-product projection entrypoint must be a function"
                    {:operation operation-key :value operation})))
  (when-not (sequential? args)
    (throw (ex-info "C2 reader-product projection entrypoint args must be sequential"
                    {:operation operation-key :args args})))
  (binding [*active-operation-keys* (disj *active-operation-keys* operation-key)
            *bypass-next-operation-keys* (conj *bypass-next-operation-keys*
                                               operation-key)]
    (apply operation args)))

(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key)
    (get *operations* key)))

(defn- invoke [key & args]
  (if-let [operation (current-operation key)]
    (apply operation args)
    (throw (ex-info (str "C2 reader-product projection requires operation " key)
                    {:operation key}))))

(defn- operation-value [key]
  (if (contains? *operations* key)
    (get *operations* key)
    (throw (ex-info (str "C2 reader-product projection requires operation " key)
                    {:operation key}))))

(defmacro ^:private definterposable [name key arguments & body]
  `(defn ~name ~arguments
     (if (contains? *bypass-next-operation-keys* ~key)
       (binding [*bypass-next-operation-keys*
                 (disj *bypass-next-operation-keys* ~key)]
         (do ~@body))
       (if-let [operation# (current-operation ~key)]
         (binding [*active-operation-keys*
                   (conj *active-operation-keys* ~key)]
           (operation# ~@arguments))
         (do ~@body)))))

(definterposable c2-syntax-seed-stream :c2-syntax-seed-stream
  [source-path products module-context]
  (let [forms-by-id (into {} (map (juxt :form-id identity)
                                  (:form-tree products)))
        seed-records
        (mapv
         (fn [idx record]
           (let [node (forms-by-id (:form-id record))
                 generated-origins
                 (mapv (fn [origin]
                         {:from (:from origin)
                          :reader-abbreviation (:reason origin)
                          :expanded-form (:value node)})
                       (:generated-origin node))]
             {:form (:form record)
              :form-id (:form-id node)
              :span (assoc (:span node) :form-index idx)
              :metadata (or (:metadata node) {})
              :reader-origin {:kind :source
                              :raw-form-kind (:kind node)
                              :raw-excerpt (:raw node)
                              :abbreviation (:abbrev node)}
              :generated-origin generated-origins}))
         (range)
         (:parsed-records products))]
    (invoke :syntax-object-stream source-path seed-records module-context)))

(definterposable c2-deferred-semantic-literals :c2-deferred-semantic-literals
  [form-tree]
  (mapv #(select-keys % [:form-id :kind :raw :value :span])
        (filter (fn [form]
                  (and (contains? #{:integer :ratio :decimal} (:kind form))
                       (= :deferred
                          (get-in form [:value :semantic-validation]))
                       (contains?
                        #{:gravity/deferred-ratio-literal
                          :gravity/decimal-literal
                          :gravity/deferred-numeric-literal}
                        (get-in form [:value :artifact]))))
                form-tree)))

(definterposable c2-top-level-products :c2-top-level-products
  [artifact]
  (let [forms-by-id (into {} (map (juxt :form-id identity)
                                  (:form-tree artifact)))
        tokens-by-id (into {} (map (juxt :token-id identity)
                                   (:token-stream artifact)))]
    (mapv (fn [form-id]
            (let [form-record (forms-by-id form-id)]
              {:form-record form-record
               :token-record (tokens-by-id (:open-token form-record))}))
          (:top-level-form-ids artifact))))

(definterposable c2-reader-capability-proof :c2-reader-capability-proof
  [artifact]
  (let [diagnostics (set (map :diagnostic (:rejected-design-coverage artifact)))
        hashes (:incremental-reader-hashes artifact)
        lexical (:lexical-product-validation artifact)
        abbreviation-forms (filter #(contains? #{:abbreviation
                                                  :metadata-wrapper}
                                                (:kind %))
                                   (:form-tree artifact))]
    {:source-unit-hash-stable?
     (boolean
      (re-find #"^sha256:" (get-in artifact [:source-unit-record
                                             :source-id])))
     :token-and-form-spans-present?
     (and (every? #(and (:token-id %) (get-in % [:span :byte-start])
                        (get-in % [:span :byte-end]))
                  (:token-stream artifact))
          (every? #(and (:form-id %) (get-in % [:span :byte-start])
                        (get-in % [:span :byte-end]))
                  (:form-tree artifact)))
     :abbreviation-origins-present?
     (every? #(seq (:generated-origin %)) abbreviation-forms)
     :literal-facts-present?
     (let [records (:literal-decoding-records artifact)
           expected-records (invoke :c2-literal-records (:form-tree artifact))]
       (and (= (count expected-records) (count records))
            (every? #(and (:literal-id %)
                          (:form-id %)
                          (:kind %)
                          (string? (:raw %))
                          (:span %)
                          (contains? % :decoded)
                          (map? (:facts %)))
                    records)))
     :trivia-retained?
     (and (true? (get-in artifact [:source-unit-record :reader-options
                                   :retain-comments]))
          (= (mapv :token-id (filter :trivia? (:token-stream artifact)))
             (mapv :trivia-id (:trivia-retention-records artifact))))
     :extension-policy-recorded?
     (= :registered (get-in artifact [:reader-extension-policy :status]))
     :incremental-hashes-stable?
     (and (= :stable (:status hashes))
          (every? #(re-find #"^sha256:" (str (get hashes %)))
                  [:source-unit :token-stream :form-tree
                   :syntax-seed-stream :extension-invocation-set
                   :reader-diagnostics]))
     :diagnostics-covered?
     (= (set (operation-value :c2-reader-diagnostic-ids)) diagnostics)
     :semantic-errors-deferred?
     (true? (get-in artifact [:semantic-error-deferment-record :deferred?]))
     :lexical-token-stream?
     (every? true?
             (map lexical
                  [:ordered-token-ids-unique?
                   :token-raw-slices-exact?
                   :token-provenance-complete?
                   :no-token-contains-top-level-form?]))
     :nested-form-tree?
     (every? true?
             (map lexical
                  [:form-ids-unique?
                   :graph-valid?
                   :root-form-ids-resolve?
                   :form-raw-slices-exact?
                   :form-links-resolve?
                   :parent-spans-enclose-children?
                   :collection-delimiters-resolve?]))
     :representation-status :genuine-lexical-token-and-recursive-form-tree
     :status :partial}))

(definterposable c2-reader-overrides-from-forms :c2-reader-overrides-from-forms
  [forms]
  (let [ns-form (first forms)
        metadata-clause (when (and (seq? ns-form) (= 'ns (first ns-form)))
                          (first (filter #(and (seq? %)
                                               (= :metadata (first %)))
                                         (drop 2 ns-form))))
        metadata (second metadata-clause)]
    (get-in metadata [:compiler :c2-reader] {})))

(definterposable c2-reader-extension-invocations :c2-reader-extension-invocations
  [form-tree]
  (mapv
   (fn [tag]
     (let [forms (filterv #(= tag (:tag %)) form-tree)]
       {:artifact :gravity/reader-extension-invocation
        :tag tag
        :handler ({'inst 'gravity.reader.standard/read-inst
                   'uuid 'gravity.reader.standard/read-uuid}
                  tag)
        :build-effects #{}
        :capabilities #{}
        :profiles #{:kernel :core :hosted :meta}
        :invocations (mapv #(select-keys % [:form-id :span :raw]) forms)
        :status (if (seq forms) :invoked :registered-not-invoked)}))
   (:registered-tags (operation-value :standard-reader-policy))))
