(ns gravity.c3-literal-projection
  "Hosted Stage0 C3 projection of authenticated C2 literal records.

  This leaf preserves lossless ratio/tagged-literal descriptors and the reader
  facts attached to hosted C3 syntax objects. It consumes an integrity result;
  it does not authenticate C2 products, own numeric semantics, or grant C3,
  proof, self-hosting, or release authority."
  (:require [gravity.reader-primitives :as reader-primitives]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private operation-keys
  #{:c3-c2-reader-integrity-report
    :form-kind
    :c3-deferred-ratio-descriptor-from-raw
    :c3-ratio-descriptor-from-raw
    :c3-lossless-literal-descriptor
    :c3-tagged-literal-descriptor
    :c3-source-form-kind
    :c3-source-facts})

(def ^:private namespace-contract
  {:namespace 'gravity.c3-literal-projection
   :contract-boundary :hosted-c3-authenticated-literal-projection
   :public-api
   {'with-operations {:arglists '([operations thunk])}
    'c3-deferred-ratio-descriptor-from-raw {:arglists '([raw])}
    'c3-ratio-descriptor-from-raw {:arglists '([raw])}
    'c3-lossless-literal-descriptor
    {:arglists '([seed form-record c2-artifact integrity-report])}
    'c3-tagged-literal-descriptor
    {:arglists '([seed form-record c2-artifact integrity-report])}
    'c3-source-form-kind
    {:arglists '([seed form-record c2-artifact integrity-report])}
    'c3-source-facts
    {:arglists '([seed form-record c2-artifact integrity-report])}}
   :operation-interposition
   {:accepted-keys operation-keys
    :partial-overrides? true
    :unknown-keys-rejected? true
    :function-values-must-satisfy :fn?}
   :artifact-inputs [:authenticated-hosted-c2-reader-product
                     :hosted-c2-form-record
                     :hosted-c3-syntax-seed]
   :artifact-outputs [:hosted-c3-lossless-literal-descriptor
                      :hosted-c3-reader-literal-facts]
   :ownership
   {:owns [:hosted-c3-literal-record-projection]
    :does-not-own [:canonical-c3-syntax-object-authority
                   :c2-reader-product-authentication
                   :numeric-semantics
                   :tagged-literal-execution
                   :reader-extension-authority
                   :syntax-object-identity
                   :diagnostic-policy
                   :proof-authority
                   :self-hosted-authority
                   :release-authority]}
   :dependency-direction
   {:requires ['clojure.core 'gravity.reader-primitives]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-c3-authority? false
   :self-hosted? false
   :release-authority? false})

(defn- unsupported [key]
  (fn [& _]
    (throw (ex-info (str "C3 literal projection requires operation " key)
                    {:operation key}))))

(def ^:private default-operations
  {:c3-c2-reader-integrity-report
   (unsupported :c3-c2-reader-integrity-report)
   :form-kind reader-primitives/form-kind})

(defn- validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C3 literal projection operations must be a map"
                    {:operations operations})))
  (let [unknown (vec (remove operation-keys (keys operations)))]
    (when (seq unknown)
      (throw (ex-info "C3 literal projection operations contain unknown keys"
                      {:unknown-keys unknown}))))
  (doseq [[key value] operations]
    (when-not (fn? value)
      (throw (ex-info "C3 literal projection operation must be a function"
                      {:operation key :value value}))))
  operations)

(defn with-operations [operations thunk]
  (validate-operations! operations)
  (when-not (fn? thunk)
    (throw (ex-info "C3 literal projection thunk must be a function"
                    {:thunk thunk})))
  (binding [*operations* (merge default-operations operations)] (thunk)))

(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key) (get *operations* key)))

(defmacro ^:private definterposable [name args & body]
  (let [key (keyword name)]
    `(defn ~name ~args
       (if-let [operation# (current-operation ~key)]
         (binding [*active-operation-keys* (conj *active-operation-keys* ~key)]
           (operation# ~@args))
         (do ~@body)))))

(defn- invoke [key & args]
  (apply (or (current-operation key)
             (get default-operations key)
             (unsupported key))
         args))

(definterposable c3-deferred-ratio-descriptor-from-raw
  [raw]
  (when-let [[_ numerator-spelling denominator-spelling]
             (and (string? raw)
                  (re-matches #"([+-]?[0-9]+)/([+-]?[0-9]+)" raw))]
    (try
      (let [numerator (bigint numerator-spelling)
            denominator (bigint denominator-spelling)]
        (when (zero? denominator)
          {:artifact :gravity/deferred-ratio-literal
           :kind :ratio
           :raw raw
           :numerator-spelling numerator-spelling
           :denominator-spelling denominator-spelling
           :numerator numerator
           :denominator denominator
           :semantic-validation :deferred
           :reason :zero-denominator}))
      (catch NumberFormatException _ nil))))

(definterposable c3-ratio-descriptor-from-raw
  [raw]
  (when-let [[_ numerator-spelling denominator-spelling]
             (and (string? raw)
                  (re-matches #"([+-]?[0-9]+)/([+-]?[0-9]+)" raw))]
    (try
      (let [numerator (bigint numerator-spelling)
            denominator (bigint denominator-spelling)]
        (if (zero? denominator)
          {:artifact :gravity/deferred-ratio-literal
           :kind :ratio
           :raw raw
           :numerator-spelling numerator-spelling
           :denominator-spelling denominator-spelling
           :numerator numerator
           :denominator denominator
           :semantic-validation :deferred
           :reason :zero-denominator}
          {:artifact :gravity/ratio-literal
           :kind :ratio
           :raw raw
           :numerator-spelling numerator-spelling
           :denominator-spelling denominator-spelling
           :numerator numerator
           :denominator denominator
           :semantic-validation :accepted}))
      (catch NumberFormatException _ nil))))

(definterposable c3-lossless-literal-descriptor
  [seed form-record c2-artifact integrity-report]
  (let [integrity-report
        (or integrity-report
            (invoke :c3-c2-reader-integrity-report c2-artifact))
        forms-by-id (into {} (map (juxt :form-id identity)
                                  (:form-tree c2-artifact)))
        tokens-by-id (into {} (map (juxt :token-id identity)
                                   (:token-stream c2-artifact)))
        root-form-id (:form-id form-record)
        ratio-form
        (case (:kind form-record)
          :ratio form-record
          :metadata-wrapper
          (let [ratio-children
                (filterv #(= :ratio (:kind %))
                         (keep forms-by-id (:children form-record)))]
            (when (= 1 (count ratio-children)) (first ratio-children)))
          nil)
        ratio-token (when ratio-form (tokens-by-id (:open-token ratio-form)))
        descriptor (when ratio-token
                     (c3-ratio-descriptor-from-raw (:raw ratio-token)))
        literal-records
        (filterv #(= (:form-id ratio-form) (:form-id %))
                 (:literal-decoding-records c2-artifact))
        deferred-records
        (filterv #(= (:form-id ratio-form) (:form-id %))
                 (get-in c2-artifact
                         [:semantic-error-deferment-record
                          :deferred-literal-records]))
        wrapper? (= :metadata-wrapper (:kind form-record))
        expected-seed-span (when form-record
                             (assoc (:span form-record)
                                    :form-index
                                    (get-in seed [:span :form-index])))]
    (when
     (and (:authentic? integrity-report)
          descriptor ratio-form ratio-token
          (= root-form-id (:form-id seed))
          (if (= :deferred (:semantic-validation descriptor))
            (and (= descriptor (:form seed))
                 (= descriptor (:value ratio-form) (:decoded ratio-token)))
            (= (:form seed) (:value ratio-form) (:decoded ratio-token)))
          (= :ratio (:kind ratio-form) (:kind ratio-token))
          (= (:open-token ratio-form) (:close-token ratio-form)
             (:token-id ratio-token))
          (= (:raw ratio-form) (:raw ratio-token) (:lexeme ratio-token)
             (:raw descriptor))
          (= (:span ratio-form) (:span ratio-token))
          (= 1 (count literal-records))
          (= (if (= :deferred (:semantic-validation descriptor))
               descriptor
               (:form seed))
             (:decoded (first literal-records)))
          (= (:raw descriptor) (:raw (first literal-records)))
          (= (:span ratio-form) (:span (first literal-records)))
          (= {:numerator-spelling (:numerator-spelling descriptor)
              :denominator-spelling (:denominator-spelling descriptor)
              :exact? true}
             (:facts (first literal-records)))
          (if (= :deferred (:semantic-validation descriptor))
            (and (= 1 (count deferred-records))
                 (= descriptor (:value (first deferred-records)))
                 (= (:raw descriptor) (:raw (first deferred-records)))
                 (= (:span ratio-form) (:span (first deferred-records))))
            (and (empty? deferred-records)
                 (= (:form seed) (:decoded (first literal-records))
                    (:value ratio-form) (:decoded ratio-token))))
          (= expected-seed-span (:span seed))
          (= (:raw form-record) (get-in seed [:reader-origin :raw-excerpt]))
          (= (:kind form-record)
             (get-in seed [:reader-origin :raw-form-kind]))
          (if wrapper?
            (and (= :metadata (:abbrev form-record)
                    (get-in seed [:reader-origin :abbreviation]))
                 (= (if (= :deferred (:semantic-validation descriptor))
                      descriptor
                      (:form seed))
                    (:value form-record)
                    (:expanded-form form-record)
                    (get-in seed [:generated-origin 0 :expanded-form]))
                 (= :metadata
                    (get-in seed [:generated-origin 0 :reader-abbreviation]))
                 (= (:surface-span form-record)
                    (get-in seed [:generated-origin 0 :from]))
                 (= (:form-id ratio-form)
                    (get-in form-record [:generated-origin 0 :child-form-id])))
            (and (= (if (= :deferred (:semantic-validation descriptor))
                      descriptor
                      (:form seed))
                    (:value form-record))
                 (nil? (:abbrev form-record))
                 (empty? (:generated-origin seed)))))
     descriptor)))

(definterposable c3-tagged-literal-descriptor
  [seed form-record c2-artifact integrity-report]
  (when (= :tagged-literal (:kind form-record))
    (let [integrity-report
          (or integrity-report
              (invoke :c3-c2-reader-integrity-report c2-artifact))
          forms-by-id
          (into {} (map (juxt :form-id identity) (:form-tree c2-artifact)))
          payload-record (get forms-by-id (first (:children form-record)))
          literal-record
          (first (filter #(= (:form-id form-record) (:form-id %))
                         (:literal-decoding-records c2-artifact)))
          tag (:tag form-record)]
      (when (and (:authentic? integrity-report)
                 (= (:form-id seed) (:form-id form-record))
                 (= 1 (count (:children form-record)))
                 (= :string (:kind payload-record))
                 (= tag (get-in literal-record [:facts :tag]))
                 (= (:raw form-record) (:raw literal-record))
                 (= (:span form-record) (:span literal-record)))
        {:artifact :gravity/tagged-literal-descriptor
         :kind :tagged-literal
         :tag tag
         :raw (:raw form-record)
         :payload (:value payload-record)
         :semantic-validation :accepted}))))

(definterposable c3-source-form-kind
  [seed form-record c2-artifact integrity-report]
  (if (or (c3-lossless-literal-descriptor
           seed form-record c2-artifact integrity-report)
          (c3-tagged-literal-descriptor
           seed form-record c2-artifact integrity-report))
    (:kind form-record)
    (invoke :form-kind (:form seed))))

(definterposable c3-source-facts
  [seed form-record c2-artifact integrity-report]
  (if-let [descriptor
           (or (c3-lossless-literal-descriptor
                seed form-record c2-artifact integrity-report)
               (c3-tagged-literal-descriptor
                seed form-record c2-artifact integrity-report))]
    {:reader-literal-kind (:kind descriptor)
     :reader-literal-descriptor descriptor
     :reader-product-integrity-hash
     (get-in c2-artifact [:reader-product-integrity :integrity-hash])
     :reader-source-id (get-in c2-artifact [:source-unit-record :source-id])
     :reader-form-id (:form-id form-record)
     :reader-seed-id (:syntax-id seed)
     :reader-container-kind
     (when (= :metadata-wrapper (:kind form-record)) :metadata-wrapper)
     :reader-literal-facts
     (select-keys descriptor
                  [:raw :numerator-spelling :denominator-spelling
                   :numerator :denominator :tag :payload
                   :semantic-validation :reason])}
    {}))
