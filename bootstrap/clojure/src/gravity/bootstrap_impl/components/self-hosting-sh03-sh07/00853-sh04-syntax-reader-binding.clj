(defn
 sh04-syntax-reader-binding
 [source-path c2-artifact semantic-source-id]
 (gravity.syntax/reader-semantic-binding
  source-path
  c2-artifact
  semantic-source-id
  (syntax-stage-assembly-operations)))

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
(declare ^:private sh04-syntax-form-index)
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
        expansions (c3-reader-expansions/project (:form-tree c2-artifact)
                     descendant-form-ids seed semantic-source-id)
        host-facts (cond-> host-facts
                     (seq expansions) (assoc :reader-expansion-origins expansions))
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
