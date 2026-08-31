(defn stage1-reader-products-from-token-stream
  [source-path source-text table token-stream]
  (semantic-mid-reader-products
   (semantic-mid-reader-products-context
    source-path source-text table token-stream)))

(doseq [helper '[semantic-mid-reader-products-context
                 semantic-mid-reader-new-form-id
                 semantic-mid-reader-combined-span
                 semantic-mid-reader-skip-trivia
                 semantic-mid-reader-trivia-hash
                 semantic-mid-reader-parse-token-literal
                 semantic-mid-reader-literal-set-key?
                 semantic-mid-reader-set-value
                 semantic-mid-reader-metadata-map
                 semantic-mid-reader-attach-metadata
                 semantic-mid-reader-read-delimited
                 semantic-mid-reader-read-abbreviation
                 semantic-mid-reader-read-tagged
                 semantic-mid-reader-read-literal
                 semantic-mid-reader-read-form
                 semantic-mid-reader-products]]
  (ns-unmap *ns* helper))
