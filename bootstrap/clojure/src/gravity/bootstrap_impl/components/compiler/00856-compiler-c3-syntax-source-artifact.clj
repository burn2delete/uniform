

(defn-
 syntax-stage-build-operations
 []
 {:c2-reader-fail! #'c2-reader-fail!,
  :c3-artifact-id #'c3-artifact-id,
  :c3-fact-ledger #'c3-fact-ledger,
  :c3-generated-syntax-report #'c3-generated-syntax-report,
  :c3-hygiene-context-map #'c3-hygiene-context-map,
  :c3-metadata-ledger #'c3-metadata-ledger,
  :c3-origin-chain-graph #'c3-origin-chain-graph,
  :c3-reader-artifact-view #'c3-reader-artifact-view,
  :c3-syntax-capability-proof #'c3-syntax-capability-proof,
  :c3-syntax-diagnostic-ids c3-syntax-diagnostic-ids,
  :c3-syntax-governing-document c3-syntax-governing-document,
  :c3-syntax-overrides-from-forms #'c3-syntax-overrides-from-forms,
  :c3-syntax-rejected-designs c3-syntax-rejected-designs,
  :c3-syntax-schema #'c3-syntax-schema,
  :c3-syntax-serialization-fixture #'c3-syntax-serialization-fixture,
  :c3-syntax-validate! #'c3-syntax-validate!,
  :c3-syntax-validate-overrides! #'c3-syntax-validate-overrides!,
  :c3-syntax-verification-report #'c3-syntax-verification-report,
  :c3-validate-c2-reader-artifact! #'c3-validate-c2-reader-artifact!,
  :compiler-c2-reader-source-artifact
  #'compiler-c2-reader-source-artifact,
  :sh03-c3-precomputed-c2-verify! #'sh03-c3-precomputed-c2-verify!,
  :sh04-syntax-adapter-contract sh04-syntax-adapter-contract,
  :sh04-syntax-build-from-fresh-c2! #'sh04-syntax-build-from-fresh-c2!,
  :sh04-syntax-facade-relative-path sh04-syntax-facade-relative-path})



(defn compiler-c3-syntax-source-artifact
  ([source-path source-text]
   (compiler-c3-syntax-source-artifact
    source-path source-text (reader-project-context-for-source source-path)))
  ([source-path source-text project-context]
   (compiler-c3-syntax-source-artifact
    source-path source-text project-context nil))
  ([source-path source-text project-context precomputed-c2-artifact]
   (if precomputed-c2-artifact
     (c2-reader-fail!
      "C2-HASH" source-path
      {:stage :read-source
       :source-span (source-span source-path 0)
       :reader-options standard-reader-options}
      {:missing-fields [:internal-sh03-precomputed-product-authority]})
     (compiler-c3-syntax-source-artifact
      source-path source-text project-context nil
      sh03-reader-internal-product-authority)))
  ([source-path source-text project-context precomputed-c2-artifact candidate]
   (compiler-c3-syntax-source-artifact source-path source-text project-context
                                       precomputed-c2-artifact candidate nil))
  ([source-path source-text project-context precomputed-c2-artifact candidate reader-source-path]
   (compiler-c3-syntax-source-artifact source-path source-text project-context
    precomputed-c2-artifact candidate reader-source-path
    (when reader-source-path {:reader-source-path reader-source-path
                              :authenticated-envelope-options {:source :development-current}})))
  ([source-path source-text project-context precomputed-c2-artifact candidate reader-source-path invocation]
   (gravity.syntax/build
    {:source-path source-path :source-text source-text :project-context project-context
     :precomputed-c2-artifact precomputed-c2-artifact :candidate candidate
     :reader-source-path reader-source-path :invocation invocation}
    (syntax-stage-build-operations))))

(defn compiler-c3-syntax-file-artifact
  [path]
  (let [c2-artifact (compiler-c2-reader-file-artifact path)
        source-text (c2-reader-artifact-source-text path c2-artifact)]
    (compiler-c3-syntax-source-artifact
     path source-text (reader-project-context-for-source path) c2-artifact
     sh03-reader-internal-product-authority)))
