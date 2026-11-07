(defn stage1-reader-products-from-token-stream
  [source-path source-text table token-stream]
  (gravity.reader/build-compatibility-forms
   source-path source-text table token-stream
   {:digest-var #'sha256-hex :pipeline-fail-var #'stage1-reader-pipeline-fail!
    :line-starts-var #'line-start-indices :token-index-var #'stage1-reader-token-index
    :span-var #'stage1-reader-span :canonical-hash-var #'reader-canonical-hash
    :decode-atom-var #'stage1-reader-decode-atom :fail-var #'stage1-reader-fail!
    :read-tagged-var #'edn/read-string :form-kind-var #'form-kind}))
