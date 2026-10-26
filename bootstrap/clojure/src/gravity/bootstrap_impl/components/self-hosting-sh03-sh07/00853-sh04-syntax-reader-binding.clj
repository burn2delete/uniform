

(defn sh04-syntax-reader-binding
  [source-path c2-artifact semantic-source-id]
  (let [reader-source (or (get-in c2-artifact [:gravity-reader-boundary
                                              :authenticated-envelope-descriptor :source-revision])
                          (get-in c2-artifact [:sh03-reader-authentication :reader-source-revision]))
        verifier-hash (get-in reader-source [:function-shapes sh03-reader-verifier :semantic-hash])
        current? (or (contains? (:sh03-reader-authentication c2-artifact) :reader-source-revision)
                     (= :gravity/sh03-current-source-reader-plan-binding
                        (get-in c2-artifact [:gravity-reader-boundary :plan-binding :artifact]))
                     (and (some? reader-source) (or (some? verifier-hash)
                       (not= sh03-reader-expected-source-content-hash (:source-content-hash reader-source)))))
        _ (when (and (or current? (some? reader-source))
                     (not (and (map? reader-source) (integer? (:source-byte-count reader-source))
                               (<= 0 (:source-byte-count reader-source) 1048576)
                               (or (not current?) (p15-s23-sh02-sha256-id? verifier-hash))
                               (every? p15-s23-sh02-sha256-id?
                                 (map reader-source [:source-content-hash :plan-semantic-hash
                                                     :functions-semantic-hash :builder-semantic-hash])))))
            (sh03-reader-boundary-fail! source-path :actual-sh03-reader-source-revision nil {}))
        c2-artifact (if current? (c3-current-reader-identity-view source-path c2-artifact) c2-artifact)
        source-unit (:source-unit-record c2-artifact)
        token-stream (:token-stream c2-artifact)
        form-tree (:form-tree c2-artifact)
        seeds (:syntax-seed-stream c2-artifact)
        span-input
        (fn [span]
          (select-keys span [:byte-start :byte-end :scalar-start :scalar-end
                             :line-start :column-start :line-end :column-end]))
        semantic-core
        {:semantic-source-id semantic-source-id
         :source-bytes-hash (:bytes-hash source-unit)
         :reader-options (:reader-options source-unit)
         :token-stream-id
         (reader-canonical-hash
          (mapv (fn [token]
                  {:token-id (:token-id token) :kind (:kind token)
                   :raw (:raw token) :span (span-input (:span token))})
                token-stream))
         :form-tree-id
         (reader-canonical-hash
          (mapv (fn [form]
                  {:form-id (:form-id form) :kind (:kind form)
                   :children (:children form)
                   :parent-form-id (:parent-form-id form)
                   :raw (:raw form) :span (span-input (:span form))})
                form-tree))
         :syntax-seed-stream-id
         (reader-canonical-hash
          (mapv (fn [seed]
                  {:form-id (:form-id seed) :form (:form seed)
                   :span (span-input (:span seed))
                   :metadata (:metadata seed) :phase (:phase seed)
                   :profile (:profile seed)})
                seeds))
         :literal-records-id
         (reader-canonical-hash
          (mapv #(update % :span span-input)
                (:literal-decoding-records c2-artifact)))
         :deferment-records-id
         (reader-canonical-hash
          (mapv #(update % :span span-input)
                (get-in c2-artifact
                        [:semantic-error-deferment-record
                         :deferred-literal-records])))
         :extension-records-id
         (reader-canonical-hash
          (c2-extension-hash-input
           (:reader-extension-invocation-records c2-artifact)))}
        reader-result-id
        (reader-canonical-hash
         {:domain :gravity/sh04-semantic-sh03-reader-result-v1
          :semantic-reader-products semantic-core})
        c2-artifact-id
        (reader-canonical-hash
         {:domain :gravity/sh04-semantic-c2-adapter-v1
          :adapter-contract
          (or (get-in c2-artifact
                      [:gravity-reader-boundary :adapter-contract])
              :gravity/sh03-to-c2-reader-products-v2)
          :semantic-reader-products semantic-core})
        envelope-id
        (reader-canonical-hash
         {:domain :gravity/sh04-semantic-sh03-envelope-v1
          :source-content-hash (get reader-source :source-content-hash sh03-reader-expected-source-content-hash)
          :plan-semantic-hash (get reader-source :plan-semantic-hash sh03-reader-expected-plan-semantic-hash)
          :reader-result-id reader-result-id
          :c2-artifact-id c2-artifact-id})
        binding-base
        (merge {:artifact :gravity/sh04-reader-semantic-binding
                :schema-version 1}
               semantic-core
               {:reader-result-id reader-result-id
                :c2-artifact-id c2-artifact-id
                :authenticated-envelope-id envelope-id})
        binding-preimage
        (assoc binding-base
               :domain :gravity/sh04-reader-semantic-binding-v1)
        semantic-binding-id
        (p15-s23-c6c10-canonical-digest source-path binding-preimage)
        revision-base
        {:artifact :gravity/sh03-reader-source-revision
         :schema-version 1
         :owner :sh03-reader
         :source-language :gravity
         :logical-source-path sh03-reader-source-relative-path
         :source-content-hash (get reader-source :source-content-hash sh03-reader-expected-source-content-hash)
         :source-byte-count (get reader-source :source-byte-count sh03-reader-expected-source-byte-count)
         :plan-semantic-hash (get reader-source :plan-semantic-hash sh03-reader-expected-plan-semantic-hash)
         :functions-semantic-hash
         (get reader-source :functions-semantic-hash sh03-reader-expected-functions-semantic-hash)
         :entry-function sh03-reader-entrypoint
         :entry-semantic-hash (get reader-source :builder-semantic-hash sh03-reader-expected-entrypoint-semantic-hash)
         :verifier-function sh03-reader-verifier
         :verifier-semantic-hash (if current? verifier-hash sh03-reader-expected-verifier-semantic-hash)
         :reader-result-id reader-result-id
         :c2-artifact-id c2-artifact-id
         :authenticated-envelope-id envelope-id
         :semantic-binding-id semantic-binding-id}
        revision
        (assoc revision-base :revision-id
               (p15-s23-c6c10-canonical-digest
                source-path
                {:domain :gravity/sh03-reader-source-revision-v1
                 :revision revision-base}))
        binding (assoc binding-base
                       :semantic-binding-id semantic-binding-id
                       :source-revision-id (:revision-id revision))]
    {:reader-binding binding
     :reader-source-revision revision}))

(defn sh04-syntax-empty-hygiene
  [namespace]
  {:marks [] :lexical-scopes [] :renames {}
   :introduced-identifiers [] :captures []
   :macro-definition-namespace nil
   :macro-call-site-namespace namespace})

(defn sh04-syntax-strip-host-metadata
  [value]
  (let [clean
        (cond
          (map? value)
          (into (empty value)
                (map (fn [[key item]]
                       [(sh04-syntax-strip-host-metadata key)
                        (sh04-syntax-strip-host-metadata item)]))
                value)

          (vector? value)
          (mapv sh04-syntax-strip-host-metadata value)

          (set? value)
          (into #{} (map sh04-syntax-strip-host-metadata) value)

          (seq? value)
          (apply list (map sh04-syntax-strip-host-metadata value))

          :else value)]
    (if (instance? clojure.lang.IObj clean)
      (with-meta clean nil)
      clean)))

(defn sh04-syntax-descendant-form-ids
  [forms-by-id root-form-id]
  (loop [pending [root-form-id]
         visited #{}]
    (if-let [form-id (peek pending)]
      (if (contains? visited form-id)
        (recur (pop pending) visited)
        (let [record (get forms-by-id form-id)]
          (recur (into (pop pending) (:children record))
                 (conj visited form-id))))
      visited)))

(def ^:dynamic ^:private *sh04-syntax-form-index-transaction* nil)
(declare ^:private sh04-syntax-form-index-roots sh04-syntax-form-index-runtime?)

(defn- sh04-syntax-native-keyword-map? [value]
  (and (or (identical? clojure.lang.PersistentArrayMap (class value))
           (identical? clojure.lang.PersistentHashMap (class value)))
       (let [entries (.iterator (.entrySet ^java.util.Map value))]
         (loop []
           (if (.hasNext entries)
             (and (identical? clojure.lang.Keyword
                              (class (.getKey ^java.util.Map$Entry (.next entries))))
                  (recur))
             true)))))

(defn- sh04-syntax-form-index-roots-current? []
  (let [entries (.iterator (.entrySet ^java.util.Map sh04-syntax-form-index-roots))]
    (loop []
      (if (.hasNext entries)
        (let [entry ^java.util.Map$Entry (.next entries)]
          (and (not (.isThreadBound ^clojure.lang.Var (.getKey entry)))
               (identical? (.getValue entry) (.deref ^clojure.lang.Var (.getKey entry)))
               (recur)))
        true))))

(defn- sh04-syntax-form-index [c2-artifact]
  (let [transaction *sh04-syntax-form-index-transaction*]
    (if (and transaction
             (identical? (Thread/currentThread) (:thread transaction))
             (identical? c2-artifact (:artifact transaction))
             (sh04-syntax-native-keyword-map? c2-artifact)
             (sh04-syntax-form-index-roots-current?)
             sh04-syntax-form-index-runtime?)
      (let [tree (:form-tree c2-artifact)
            previous @(:slot transaction)]
        (if (and previous (identical? tree (:tree previous)))
          (:index previous)
          (let [index (into {} (map (juxt :form-id identity)) tree)]
            ;; Only the immutable native lookup projection is reused. All
            ;; descendant, descriptor, validation and authentication work stays
            ;; fresh. Qualification never asks opaque values to compare/hash.
            (when (and (identical? clojure.lang.PersistentVector (class tree))
                       (sh04-syntax-native-keyword-map? index)
                       (let [records (.iterator ^java.lang.Iterable tree)]
                         (loop []
                           (if (.hasNext records)
                             (let [record (.next records)]
                               (and (sh04-syntax-native-keyword-map? record)
                                    (identical? clojure.lang.Keyword (class (:form-id record)))
                                    (recur)))
                             true))))
              (vreset! (:slot transaction) {:tree tree :index index}))
            index)))
      (into {} (map (juxt :form-id identity)) (:form-tree c2-artifact)))))

(defn sh04-syntax-source-descriptor
  [seed form-record c2-artifact integrity-report semantic-source-id
   reader-binding reader-source-revision registered-literals]
  (let [span (sh04-syntax-semantic-span (:span seed) semantic-source-id)
        source-namespace (or (:namespace seed) 'gravity.user)
        namespace-context {:current source-namespace :aliases {} :imports []}
        forms-by-id
        (sh04-syntax-form-index c2-artifact)
        descendant-form-ids
        (sh04-syntax-descendant-form-ids
         forms-by-id (:form-id form-record))
        registered-literal-bindings
        (filterv #(contains? descendant-form-ids (:form-id %))
                 (:bindings registered-literals))
        literal-descriptor
        (or (c3-lossless-literal-descriptor seed form-record c2-artifact
                                            integrity-report)
            (c3-tagged-literal-descriptor seed form-record c2-artifact
                                          integrity-report))
        kind (c3-source-form-kind seed form-record c2-artifact integrity-report)
        host-facts (c3-source-facts seed form-record c2-artifact
                                    integrity-report)
        host-facts
        (cond-> host-facts
          (seq registered-literal-bindings)
          (assoc :registered-literal-bindings
                 registered-literal-bindings))
        facts
        (cond-> (dissoc host-facts :reader-product-integrity-hash
                        :reader-source-id)
          (seq host-facts)
          (assoc :semantic-source-id semantic-source-id
                 :reader-result-id (:reader-result-id reader-binding)
                 :c2-artifact-id (:c2-artifact-id reader-binding)
                 :authenticated-envelope-id
                 (:authenticated-envelope-id reader-binding)))]
    (sh04-syntax-strip-host-metadata
     (sh04-syntax-project-registered-literal-values
      (get-in c2-artifact [:source-unit-record :path])
      registered-literals
      {:form {:kind kind :value (or literal-descriptor (:form seed))
              :raw (or (get-in seed [:reader-origin :raw-excerpt]) "")}
       :span span
       :source {:source-id semantic-source-id
                :form-id (:form-id form-record)
                :token-range [(:open-token form-record)
                              (:close-token form-record)]}
       :namespace namespace-context
       :phase (:phase seed)
       :profile (or (:profile seed) :meta)
       :metadata (or (:metadata seed) {})
       :hygiene (sh04-syntax-empty-hygiene source-namespace)
       :origin
       [{:kind :source
         :span span
         :producer {:kind :reader :name 'gravity.bootstrap.reader
                    :identity (:reader-result-id reader-binding)
                    :source-id semantic-source-id
                    :generated-form-id nil}
         :producer-version "SH-03"
         :input-syntax-ids []
         :generation-reason :source-read
         :build-effects []}]
       :facts
       [{:producer-stage :reader
         :fact-kind :authenticated-reader-products
         :value facts
         :version 1
         :invalidated-by [:macro-expansion :metadata-change
                          :namespace-change]}]
       :reader-binding reader-binding
       :reader-source-revision reader-source-revision
       :version 1}))))

;; The native PersistentVector/into3 path in the supported Clojure runtime.
;; An interposed descriptor/walker or core dependency disables reuse, retaining
;; the original lookup count, callback behavior and error order on that call.
(def ^:private sh04-syntax-form-index-runtime? (= "1.12.5" (clojure-version)))
(def ^:private sh04-syntax-form-index-roots
  {#'sh04-syntax-source-descriptor sh04-syntax-source-descriptor
   #'sh04-syntax-descendant-form-ids sh04-syntax-descendant-form-ids
   #'clojure.core/into into #'clojure.core/map map #'clojure.core/juxt juxt
   #'clojure.core/identity identity #'clojure.core/transduce transduce
   #'clojure.core/transient transient #'clojure.core/conj! conj!
   #'clojure.core/persistent! persistent! #'clojure.core/meta meta
   #'clojure.core/with-meta with-meta #'clojure.core/clojure-version clojure-version
   #'clojure.core/*clojure-version* *clojure-version*})
