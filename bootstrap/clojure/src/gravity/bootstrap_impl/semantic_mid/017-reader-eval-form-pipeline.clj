(defn- semantic-mid-reader-eval-run-token-automaton
  [reader-source-path definitions env form]
  (let [[_ source-path-expr source-text-expr classifier-expr
         realizer-expr automaton-expr character-stream-expr] form
        source-path (stage1-reader-eval-gravity
                     reader-source-path definitions env source-path-expr)
        source-text (stage1-reader-eval-gravity
                     reader-source-path definitions env source-text-expr)
        classifier (stage1-reader-eval-gravity
                    reader-source-path definitions env classifier-expr)
        realizer (stage1-reader-eval-gravity
                  reader-source-path definitions env realizer-expr)
        automaton (stage1-reader-eval-gravity
                   reader-source-path definitions env automaton-expr)
        character-stream (stage1-reader-eval-gravity
                          reader-source-path definitions env
                          character-stream-expr)
        token-stream (stage1-reader-token-stream-from-automaton
                      source-path source-text classifier realizer
                      automaton character-stream)]
    (semantic-mid-reader-trace!
     :reader/run-token-automaton
     [[:token-classifier classifier]
      [:token-realizer realizer]
      [:token-automaton automaton]
      [:token-stream token-stream]
      [:token-automaton-ran
       {:character-count (:character-count character-stream)
        :token-count (:token-count token-stream)
        :operation-count (count (:executed-operations token-stream))}]])
    token-stream))

(defn- semantic-mid-reader-eval-forms-from-tokens
  [reader-source-path definitions env form]
  (let [[_ source-path-expr source-text-expr table-expr
         token-stream-expr] form
        source-path (stage1-reader-eval-gravity
                     reader-source-path definitions env source-path-expr)
        source-text (stage1-reader-eval-gravity
                     reader-source-path definitions env source-text-expr)
        table (stage1-reader-eval-gravity
               reader-source-path definitions env table-expr)
        token-stream (stage1-reader-eval-gravity
                      reader-source-path definitions env token-stream-expr)
        records (stage1-reader-records-from-token-stream
                 source-path source-text table token-stream)]
    (semantic-mid-reader-trace!
     :reader/forms-from-tokens
     [[:forms-from-tokens
       {:token-count (:token-count token-stream)
        :form-count (count records)}]])
    records))

(defn- semantic-mid-reader-eval-build-forms
  [reader-source-path definitions env form]
  (let [[_ source-path-expr source-text-expr classifier-expr
         realizer-expr automaton-expr form-builder-expr
         token-stream-expr] form
        source-path (stage1-reader-eval-gravity
                     reader-source-path definitions env source-path-expr)
        source-text (stage1-reader-eval-gravity
                     reader-source-path definitions env source-text-expr)
        classifier (stage1-reader-eval-gravity
                    reader-source-path definitions env classifier-expr)
        realizer (stage1-reader-eval-gravity
                  reader-source-path definitions env realizer-expr)
        automaton (stage1-reader-eval-gravity
                   reader-source-path definitions env automaton-expr)
        form-builder (stage1-reader-eval-gravity
                      reader-source-path definitions env form-builder-expr)
        token-stream (stage1-reader-eval-gravity
                      reader-source-path definitions env token-stream-expr)
        records (stage1-reader-records-from-form-builder
                 source-path source-text classifier realizer automaton
                 form-builder token-stream)]
    (semantic-mid-reader-trace!
     :reader/build-forms
     [[:token-classifier classifier]
      [:token-realizer realizer]
      [:token-automaton automaton]
      [:form-builder form-builder]
      [:forms-built {:token-count (:token-count token-stream)
                     :form-count (count records)}]])
    records))
