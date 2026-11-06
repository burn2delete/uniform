(defn stage1-reader-token-stream
  ([source-path ^String source-text table]
   (stage1-reader-token-stream source-path source-text table
                               {:retain-comments false}))
  ([source-path ^String source-text table reader-options]
   (gravity.reader/tokenize-compatibility-source
    source-path source-text table reader-options
    {:line-starts-var #'line-start-indices :digest-var #'sha256-hex
     :line-terminator-var #'line-terminator-char? :span-var #'stage1-reader-span
     :fail-var #'stage1-reader-fail! :decode-string-var #'stage1-reader-decode-string
     :token-kind-var #'stage1-reader-token-kind :decode-atom-var #'stage1-reader-decode-atom
     :decode-character-var #'stage1-reader-decode-character})))
