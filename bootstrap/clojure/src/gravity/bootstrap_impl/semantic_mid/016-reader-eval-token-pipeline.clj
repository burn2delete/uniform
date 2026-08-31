(defn- semantic-mid-reader-eval-tokens-from-characters
  [reader-source-path definitions env form]
  (let [[_ source-path-expr source-text-expr table-expr
         character-stream-expr] form
        source-path (stage1-reader-eval-gravity
                     reader-source-path definitions env source-path-expr)
        source-text (stage1-reader-eval-gravity
                     reader-source-path definitions env source-text-expr)
        table (stage1-reader-eval-gravity
               reader-source-path definitions env table-expr)
        character-stream (stage1-reader-eval-gravity
                          reader-source-path definitions env
                          character-stream-expr)
        token-stream (stage1-reader-token-stream-from-characters
                      source-path source-text table character-stream)]
    (semantic-mid-reader-trace!
     :reader/tokens-from-characters
     [[:token-stream token-stream]
      [:tokens-from-characters
       {:character-count (:character-count character-stream)
        :token-count (:token-count token-stream)}]])
    token-stream))

(defn- semantic-mid-reader-eval-tokens-from-classifier
  [reader-source-path definitions env form]
  (let [[_ source-path-expr source-text-expr classifier-expr
         character-stream-expr] form
        source-path (stage1-reader-eval-gravity
                     reader-source-path definitions env source-path-expr)
        source-text (stage1-reader-eval-gravity
                     reader-source-path definitions env source-text-expr)
        classifier (stage1-reader-eval-gravity
                    reader-source-path definitions env classifier-expr)
        character-stream (stage1-reader-eval-gravity
                          reader-source-path definitions env
                          character-stream-expr)
        token-stream (stage1-reader-token-stream-from-classifier
                      source-path source-text classifier character-stream)]
    (semantic-mid-reader-trace!
     :reader/tokens-from-classifier
     [[:token-classifier classifier]
      [:token-stream token-stream]
      [:tokens-from-classifier
       {:character-count (:character-count character-stream)
        :token-count (:token-count token-stream)}]])
    token-stream))

(defn- semantic-mid-reader-eval-realize-tokens
  [reader-source-path definitions env form]
  (let [[_ source-path-expr source-text-expr classifier-expr
         realizer-expr character-stream-expr] form
        source-path (stage1-reader-eval-gravity
                     reader-source-path definitions env source-path-expr)
        source-text (stage1-reader-eval-gravity
                     reader-source-path definitions env source-text-expr)
        classifier (stage1-reader-eval-gravity
                    reader-source-path definitions env classifier-expr)
        realizer (stage1-reader-eval-gravity
                  reader-source-path definitions env realizer-expr)
        character-stream (stage1-reader-eval-gravity
                          reader-source-path definitions env
                          character-stream-expr)
        token-stream (stage1-reader-token-stream-from-realizer
                      source-path source-text classifier realizer
                      character-stream)]
    (semantic-mid-reader-trace!
     :reader/realize-tokens
     [[:token-classifier classifier]
      [:token-realizer realizer]
      [:token-stream token-stream]
      [:tokens-realized
       {:character-count (:character-count character-stream)
        :token-count (:token-count token-stream)}]])
    token-stream))
