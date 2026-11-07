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
          decimal-occurrence-projection
          (sh03-reader-decimal-occurrence-projection-from-products!
           source-path products)
          _ (when-not (= decimal-occurrence-projection
                         (:sh07-decimal-occurrence-projection products))
              (c2-reader-fail!
               "C2-HASH" source-path
               {:stage :read-source
                :source-span (source-span source-path 0)
                :reader-options reader-options}
               {:missing-fields
                [:internal-sh03-c2-decimal-occurrence-projection]}))
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
          integrity-record (c2-reader-product-integrity-record-v3
                            source-unit top-level-form-ids incremental-hashes
                            literal-records deferred-literals
                            (:projection-id decimal-occurrence-projection))
          projection
          (gravity.reader/native-document-projection
           source-path source-text project-context reader-options
           {:overrides overrides :products products :module-context module-context
            :source-unit source-unit :lexical-token-stream? lexical-token-stream?
            :nested-form-tree? nested-form-tree? :token-stream token-stream :form-tree form-tree
            :top-level-form-ids top-level-form-ids :lexical-validation lexical-validation
            :syntax-seeds syntax-seeds :literal-records literal-records
            :extension-invocations extension-invocations :deferred-literals deferred-literals
            :diagnostics diagnostics :incremental-hashes incremental-hashes
            :integrity-record integrity-record}
           {:governing-document-var #'c2-reader-governing-document
            :diagnostic-ids-var #'c2-reader-diagnostic-ids
            :uncredited-entrypoints-var #'sh03-reader-uncredited-source-model-entrypoints
            :trivia-records-var #'c2-trivia-records :rejected-designs-var #'c2-reader-rejected-designs
            :validate-var #'c2-reader-validate! :capability-proof-var #'c2-reader-capability-proof
            :pure-text-options-var #'reader-pure-text/options?
            :resource-options-var #'reader-resource-options? :artifact-id-var #'c2-reader-artifact-id})
          artifact-with-bridge
          (-> (:artifact projection)
              (assoc :sh07-decimal-occurrence-projection
                     decimal-occurrence-projection)
              (assoc-in [:gravity-reader-boundary
                         :decimal-occurrence-projection-id]
                        (:projection-id decimal-occurrence-projection))
              (update-in [:pass :emits]
                         (fnil conj [])
                         :sh07-decimal-occurrence-projection))
          artifact (assoc artifact-with-bridge
                          :artifact-id (c2-reader-artifact-id artifact-with-bridge))]
      artifact)
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
