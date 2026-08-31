(defn stage1-reader-eval-gravity
  [reader-source-path definitions env form]
  (cond
    (symbol? form)
    (cond
      (contains? env form) (get env form)
      (#{stage1-reader-algorithm-entrypoint
         stage1-reader-pipeline-entrypoint
         stage1-reader-character-pipeline-entrypoint
         stage1-reader-token-classifier-pipeline-entrypoint
         stage1-reader-token-realizer-pipeline-entrypoint
         stage1-reader-token-automaton-pipeline-entrypoint
         stage1-reader-form-builder-pipeline-entrypoint
         stage1-reader-executor-pipeline-entrypoint
         stage1-reader-runtime-pipeline-entrypoint
         stage1-reader-compiled-pipeline-entrypoint
         stage1-reader-binary-pipeline-entrypoint
         stage1-reader-self-hosted-runtime-entrypoint} form)
      (get definitions form)
      (contains? definitions form)
      (let [definition (get definitions form)]
        (case (:kind definition)
          :def (stage1-reader-eval-gravity
                reader-source-path definitions env (:value-form definition))
          :defn definition))
      :else
      (stage1-reader-algorithm-fail!
       "STAGE1ALGO002" reader-source-path form {:symbol form}))

    (or (keyword? form) (string? form) (number? form)
        (true? form) (false? form) (nil? form) (map? form)
        (vector? form) (set? form))
    form

    (seq? form)
    (semantic-mid-reader-eval-sequence
     reader-source-path definitions env form)

    :else
    (stage1-reader-algorithm-fail!
     "STAGE1ALGO002" reader-source-path form
     {:form-kind (form-kind form)})))

(doseq [helper '[semantic-mid-reader-eval-let
                 semantic-mid-reader-eval-if
                 semantic-mid-reader-trace!
                 semantic-mid-reader-eval-read-with-table
                 semantic-mid-reader-eval-scan-tokens
                 semantic-mid-reader-eval-source-characters
                 semantic-mid-reader-eval-tokens-from-characters
                 semantic-mid-reader-eval-tokens-from-classifier
                 semantic-mid-reader-eval-realize-tokens
                 semantic-mid-reader-eval-run-token-automaton
                 semantic-mid-reader-eval-forms-from-tokens
                 semantic-mid-reader-eval-build-forms
                 semantic-mid-reader-eval-sequence]]
  (ns-unmap *ns* helper))
