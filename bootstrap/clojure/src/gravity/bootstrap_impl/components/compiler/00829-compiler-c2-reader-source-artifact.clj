(declare ^:private sh03-reader-authenticated-envelope-options)

(defn compiler-c2-reader-source-artifact
  ([source-path source-text]
   (compiler-c2-reader-source-artifact
    source-path source-text (reader-project-context-for-source source-path)))
  ([source-path source-text project-context]
   (compiler-c2-reader-source-artifact
    source-path source-text project-context nil))
  ([source-path source-text project-context precomputed-products]
   (if precomputed-products
     (c2-reader-fail!
      "C2-HASH" source-path
      {:stage :read-source
       :source-span (source-span source-path 0)
       :reader-options standard-reader-options}
      {:missing-fields [:internal-sh03-precomputed-product-authority]})
     (compiler-c2-reader-source-artifact
      source-path source-text project-context nil
      sh03-reader-internal-product-authority)))
  ([source-path source-text project-context precomputed-products candidate]
   (compiler-c2-reader-source-artifact source-path source-text project-context
                                       precomputed-products candidate nil))
  ([source-path source-text project-context precomputed-products candidate reader-source-path]
   (compiler-c2-reader-source-artifact source-path source-text project-context
     precomputed-products candidate reader-source-path
     (when reader-source-path {:reader-source-path reader-source-path
                               :authenticated-envelope-options {:source :development-current}})))
  ([source-path source-text project-context precomputed-products candidate reader-source-path invocation]
   (compiler-c2-reader-source-artifact
    source-path source-text project-context precomputed-products candidate
    reader-source-path invocation standard-reader-options))
  ([source-path source-text project-context precomputed-products candidate reader-source-path invocation reader-options]
   (when-not (= :passed (:status (p15-s23-trusted-carrier-validation reader-options :default-only)))
     (c2-reader-fail! "C2-HASH" source-path {:stage :read-source}
                      {:missing-fields [:native-reader-options]}))
   (when (and (or (reader-pure-text/options? reader-options)
                  (reader-resource-options? reader-options))
              (not= :passed
                    (:status (p15-s23-trusted-carrier-validation
                              {:source-path source-path :source-text source-text
                               :project-context project-context :reader-options reader-options} :default-only))))
     (c2-reader-fail! "C2-HASH" source-path {:stage :read-source}
                      {:missing-fields [:native-pure-text-construction-input]}))
   (when-not (or (= standard-reader-options reader-options)
                 (and (= (assoc standard-reader-options :extension-policy (reader-pure-text/policy-id))
                         reader-options)
                      (nil? precomputed-products) (nil? candidate)
                      (some? invocation) (string? reader-source-path))
                 (and (reader-resource-options? reader-options)
                      (nil? precomputed-products) (nil? candidate)
                      (some? invocation) (string? reader-source-path)
                      (reader-resource-context! source-path source-text project-context)))
     (c2-reader-fail! "C2-HASH" source-path {:stage :read-source}
                      {:missing-fields [:fixed-pure-text-current-source-construction]}))
   (when invocation
     (sh03-reader-authenticated-envelope-options source-path invocation))
   (when (and invocation (nil? precomputed-products)
              (not (and (string? reader-source-path)
                        (try
                          (= (.getCanonicalPath (java.io.File. reader-source-path))
                             (.getCanonicalPath
                              (java.io.File. (:reader-source-path invocation))))
                          (catch Exception _ false)))))
     (c2-reader-fail! "C2-HASH" source-path {:stage :read-source}
                     {:missing-fields [:matching-host-reader-invocation]}))
   (when (and (some? reader-source-path) (or (some? precomputed-products) (some? candidate)))
     (c2-reader-fail! "C2-HASH" source-path {:stage :read-source}
                      {:missing-fields [:direct-current-sh03-reader-construction]}))
   (try
    (let [products
          (if precomputed-products
            (apply sh03-reader-precomputed-products-verify!
             (cond-> [candidate source-path source-text project-context precomputed-products]
               invocation (conj invocation)))
            (if (reader-resource-options? reader-options)
              (reader-resource-products! source-path source-text reader-options project-context)
              (c2-reader-products source-path source-text
                                  reader-options project-context reader-source-path)))
          source-unit (:source-unit products)
          token-stream (:token-stream products)
          form-tree (:form-tree products)
          top-level-form-ids (:root-form-ids products)
          _records (:parsed-records products)
          forms (:parsed-values products)
          _ (validate-ns-syntax! source-path forms)
          module-context (reader-module-context forms)
          overrides (c2-reader-overrides-from-forms forms)
          _ (c2-reader-validate-overrides! source-path overrides source-unit
                                           token-stream)
          syntax-seeds (c2-syntax-seed-stream source-path products
                                              module-context)
          deferred-literals (or (:deferred-literal-records products)
                                (c2-deferred-semantic-literals form-tree))
          literal-records (or (:literal-decoding-records products)
                              (c2-literal-records form-tree))
          extension-invocations
          (or (:reader-extension-invocation-records products)
              (c2-reader-extension-invocations form-tree))
          diagnostics []
          lexical-validation (c2-lexical-product-validation
                              source-text token-stream form-tree
                              top-level-form-ids)
          lexical-token-stream?
          (every? true?
                  (map lexical-validation
                       [:ordered-token-ids-unique?
                        :token-raw-slices-exact?
                        :token-provenance-complete?
                        :no-token-contains-top-level-form?]))
          nested-form-tree?
          (every? true?
                  (map lexical-validation
                       [:form-ids-unique?
                        :graph-valid?
                        :root-form-ids-resolve?
                        :form-raw-slices-exact?
                        :form-links-resolve?
                        :parent-spans-enclose-children?
                        :collection-delimiters-resolve?]))
          incremental-hashes (c2-incremental-hashes
                              source-unit token-stream form-tree syntax-seeds
                              extension-invocations diagnostics)
          integrity-record (c2-reader-product-integrity-record
                            source-unit top-level-form-ids incremental-hashes
                            literal-records deferred-literals)
          artifact-base
          {:kind :gravity/stage0-c2-reader-document-artifact
           :task "P06-D081"
           :document-set ["C2"]
           :governing-document c2-reader-governing-document
           :pass {:name :c2-reader-document-coverage
                  :input :source-bytes
                  :output :reader-document-proof
                  :requires [:source-unit :reader-policy]
                  :preserves [:source-spans :raw-literal-facts
                              :reader-origin :trivia :diagnostics]
                  :emits [:source-unit-record :token-stream :form-tree
                          :syntax-seed-stream :reader-source-map
                          :literal-decoding-records
                          :reader-extension-invocation-records
                          :reader-diagnostics :incremental-reader-hash]
                  :rejects c2-reader-diagnostic-ids}
           :source-overrides overrides
           ;; Diagnostic observations only; never SH03 authentication authority.
           :extension-dispatch-observations
           (:extension-dispatch-observations products)
           :module (assoc (dissoc module-context :namespace-clause-syntax)
                          :source-path source-path)
           :source-unit-record source-unit
           :gravity-reader-boundary
           {:slice :SH-03
            :owner :gravity-source
            :plan-binding (:sh03-reader-plan-binding products)
            :resolved-reader-result
            (select-keys (:sh03-reader-result products)
                         [:artifact :schema-version :status
                          :actual-path-provenance
                          :incremental-reader-hashes
                          :semantic-reader-template
                          :execution-boundary])
            :adapter-contract (:sh03-reader-adapter-contract products)
            :uncredited-source-models
            {:status :not-executed
             :entrypoints sh03-reader-uncredited-source-model-entrypoints
             :self-hosting-credit? false
             :seed-retirement-credit? false
             :release-credit? false}
            :semantic-value-table-id
            (:sh03-semantic-value-table-id products)
            :authenticated-envelope-descriptor
            (:sh02-reader-envelope-descriptor products)
            :authenticated-envelope (:sh02-reader-envelope products)
            :target-source-reread? false
            :clojure-adapter-residual? true
            :self-hosted? false}
           :representation-boundary
           {:token-stream :ordered-utf8-lexical-token-stream
            :form-tree :recursive-delimiter-linked-form-tree
            :lexical-token-stream? lexical-token-stream?
            :nested-form-tree? nested-form-tree?
            :remaining-reader-boundaries
            [:full-language-literal-surface
             :full-language-reader-abbreviation-surface
             :full-language-reader-extension-registry
             :host-and-seed-retirement]
            :sh03-bootstrap-subset-status :complete
            :status (if (and lexical-token-stream? nested-form-tree?)
                      :complete-for-slice
                      :failed)}
           :token-stream token-stream
           :form-tree form-tree
           :top-level-form-ids top-level-form-ids
           :parsed-semantic-values (:parsed-values products)
           :lexical-product-validation lexical-validation
           :syntax-seed-stream syntax-seeds
           :reader-source-map (mapv #(select-keys % [:syntax-id :form-id :span])
                                    syntax-seeds)
           :gravity-reader-source-map (:gravity-reader-source-map products)
           :literal-decoding-records literal-records
           :trivia-retention-records (c2-trivia-records token-stream)
           :reader-extension-policy
           {:artifact :gravity/reader-extension-policy
            :extensions
            [{:tag 'inst
              :handler 'gravity.reader.standard/read-inst
              :build-effects #{}
              :capabilities #{}
              :profiles #{:kernel :core :hosted :meta}
              :output :syntax-seed}
             {:tag 'uuid
              :handler 'gravity.reader.standard/read-uuid
              :build-effects #{}
              :capabilities #{}
              :profiles #{:kernel :core :hosted :meta}
              :output :syntax-seed}]
            :status :registered}
           :reader-extension-invocation-records extension-invocations
           :semantic-error-deferment-record
           {:artifact :gravity/semantic-error-deferment
            :forms-retained [:unknown-symbol :profile-illegal-form
                             :zero-denominator-ratio
                             :host-independent-decimal-range]
            :deferred-literal-records deferred-literals
            :semantic-analysis-in-reader? false
            :module-parser-invoked? false
            :deferred? true
            :owner-phases [:namespace-analysis :type-check :profile-validate
                           :numeric-mode-validation]}
           :reader-diagnostics diagnostics
           :incremental-reader-hashes incremental-hashes
           :reader-product-integrity integrity-record
           :rejected-design-coverage c2-reader-rejected-designs
           :diagnostics []}
          _ (c2-reader-validate! source-path artifact-base)
          capability-proof (c2-reader-capability-proof artifact-base)
          conformance {:documents ["C2"]
                       :task "P06-D081"
                       :required-diagnostic-ids c2-reader-diagnostic-ids
                       :source-unit-status :complete
                       :token-stream-status :complete-for-slice
                       :form-tree-status :complete-for-slice
                       :span-status :exact-utf8-byte-and-line-column
                       :abbreviation-status :complete-bootstrap-subset
                       :literal-status :complete-bootstrap-subset
                       :trivia-status :reader-option-sensitive
                       :extension-status :complete-bootstrap-subset
                       :incremental-hash-status :complete-for-slice
                       :diagnostic-status :complete-bootstrap-subset
                       :semantic-deferment-status :complete-for-slice
                       :status :partial}
          artifact (assoc artifact-base
                          :capability-based-proof capability-proof
                          :c2-reader-results conformance)
          artifact
            (if (reader-pure-text/options? reader-options)
              (-> artifact
                  (assoc-in [:reader-extension-policy :extensions]
                            [{:tag 'text :handler 'sh03-reader-read-text
                              :build-effects #{} :capabilities #{} :profiles #{:meta}
                              :output :syntax-seed}])
                  (assoc-in [:gravity-reader-boundary :pure-text-reconstruction-input]
                            {:source-text source-text :project-context project-context}))
              (if (reader-resource-options? reader-options)
                (assoc-in artifact [:reader-extension-policy :extensions]
                          [{:tag 'resource :handler 'gravity.reader/read-input
                            :build-effects #{:build/read-file} :capabilities #{:fs/read}
                            :profiles #{:meta} :output :syntax-seed}])
                artifact))]
      (assoc artifact :artifact-id (c2-reader-artifact-id artifact)))
     (catch clojure.lang.ExceptionInfo ex
       (c2-reader-remap-exception! source-path ex)))))

(defn reader-pure-source-artifact
  "Construct the fixed Gravity-owned text-only variant from current source.
  Standard constructors and the unsealed host-pure option retain their meaning."
  [source-path source-text project-context reader-source-path]
  (compiler-c2-reader-source-artifact
   source-path source-text project-context nil nil reader-source-path
   {:reader-source-path reader-source-path
    :authenticated-envelope-options {:source :development-current}}
   (assoc standard-reader-options :extension-policy (reader-pure-text/policy-id))))
