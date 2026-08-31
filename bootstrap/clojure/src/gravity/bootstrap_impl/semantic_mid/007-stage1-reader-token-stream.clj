(let [context* semantic-mid-reader-token-context
      ops {:char-string semantic-mid-reader-char-string
           :ignored? semantic-mid-reader-ignored?
           :comment? semantic-mid-reader-comment?
           :skip-ignored semantic-mid-reader-skip-ignored
           :whitespace-token semantic-mid-reader-whitespace-token
           :comment-token semantic-mid-reader-comment-token
           :atom-end semantic-mid-reader-atom-end
           :string-token semantic-mid-reader-string-token
           :atom-token semantic-mid-reader-atom-token
           :character-token semantic-mid-reader-character-token
           :abbreviation-token semantic-mid-reader-abbreviation-token
           :dispatch-token semantic-mid-reader-dispatch-token
           :delimiter-token semantic-mid-reader-delimiter-token
           :next-token semantic-mid-reader-next-token
           :tokenize semantic-mid-reader-tokenize}]
  (defn stage1-reader-token-stream
    ([source-path ^String source-text table]
     (stage1-reader-token-stream source-path source-text table
                                 {:retain-comments false}))
    ([source-path ^String source-text table reader-options]
     ((:tokenize ops) ops
      (context* source-path source-text table reader-options)))))

(doseq [helper '[semantic-mid-reader-token-context
                 semantic-mid-reader-char-string
                 semantic-mid-reader-ignored?
                 semantic-mid-reader-comment?
                 semantic-mid-reader-skip-ignored
                 semantic-mid-reader-whitespace-token
                 semantic-mid-reader-comment-token
                 semantic-mid-reader-atom-end
                 semantic-mid-reader-string-token
                 semantic-mid-reader-atom-token
                 semantic-mid-reader-character-token
                 semantic-mid-reader-abbreviation-token
                 semantic-mid-reader-dispatch-token
                 semantic-mid-reader-delimiter-token
                 semantic-mid-reader-next-token
                 semantic-mid-reader-tokenize]]
  (ns-unmap *ns* helper))
