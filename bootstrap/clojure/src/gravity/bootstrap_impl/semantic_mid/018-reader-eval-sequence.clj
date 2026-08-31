(defn- semantic-mid-reader-eval-sequence
  [reader-source-path definitions env form]
  (case (first form)
    quote (second form)
    do (stage1-reader-eval-body reader-source-path definitions env
                                (rest form))
    let (semantic-mid-reader-eval-let
         reader-source-path definitions env form)
    if (semantic-mid-reader-eval-if
        reader-source-path definitions env form)
    reader/read-with-table
    (semantic-mid-reader-eval-read-with-table
     reader-source-path definitions env form)
    reader/scan-tokens
    (semantic-mid-reader-eval-scan-tokens
     reader-source-path definitions env form)
    reader/source-characters
    (semantic-mid-reader-eval-source-characters
     reader-source-path definitions env form)
    reader/tokens-from-characters
    (semantic-mid-reader-eval-tokens-from-characters
     reader-source-path definitions env form)
    reader/tokens-from-classifier
    (semantic-mid-reader-eval-tokens-from-classifier
     reader-source-path definitions env form)
    reader/realize-tokens
    (semantic-mid-reader-eval-realize-tokens
     reader-source-path definitions env form)
    reader/run-token-automaton
    (semantic-mid-reader-eval-run-token-automaton
     reader-source-path definitions env form)
    reader/forms-from-tokens
    (semantic-mid-reader-eval-forms-from-tokens
     reader-source-path definitions env form)
    reader/build-forms
    (semantic-mid-reader-eval-build-forms
     reader-source-path definitions env form)
    (let [operator (first form)]
      (if (contains? definitions operator)
        (stage1-reader-execute-gravity-function
         reader-source-path definitions operator
         (mapv #(stage1-reader-eval-gravity
                 reader-source-path definitions env %)
               (rest form)))
        (stage1-reader-algorithm-fail!
         "STAGE1ALGO003" reader-source-path form
         {:operator operator})))))
