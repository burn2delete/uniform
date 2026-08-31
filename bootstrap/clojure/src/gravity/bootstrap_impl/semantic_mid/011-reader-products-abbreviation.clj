(defn- semantic-mid-reader-read-abbreviation
  [{:keys [source-path source-text tokens token-count nodes token-id] :as context}
   idx token leading-trivia form-id]
  (let [abbrev (:abbrev token)
        [metadata-value metadata-end _ metadata-id]
        (when (= :metadata abbrev)
          (let [[child-idx child-trivia]
                (semantic-mid-reader-skip-trivia context (inc idx))]
            (when (>= child-idx token-count)
              (stage1-reader-fail!
               "C2-METADATA" source-path abbrev
               {:source-span (:span token)
                :token-id (token-id token)
                :form-id form-id
                :raw (:raw token)
                :facts {:abbreviation abbrev}}))
            (semantic-mid-reader-read-form
             context child-idx child-trivia)))
        next-start (if (= :metadata abbrev)
                     metadata-end
                     (inc idx))
        [child-idx child-trivia]
        (semantic-mid-reader-skip-trivia context next-start)]
    (when (>= child-idx token-count)
      (stage1-reader-fail!
       (if (= :metadata abbrev) "C2-METADATA" "C2-ABBREV")
       source-path abbrev
       {:source-span (:span token)
        :token-id (token-id token)
        :form-id form-id
        :raw (:raw token)
        :facts {:abbreviation abbrev}}))
    (let [[child-value next-idx child-span child-id]
          (semantic-mid-reader-read-form context child-idx child-trivia)
          child-node (@nodes child-id)
          metadata-node (when metadata-id (@nodes metadata-id))
          metadata (when metadata-id
                     (semantic-mid-reader-metadata-map
                      context metadata-value metadata-node))
          value (case abbrev
                  :quote (list 'quote child-value)
                  :syntax-quote (list 'syntax-quote child-value)
                  :unquote (list 'unquote child-value)
                  :splice-unquote (list 'splice-unquote child-value)
                  :deref (list 'deref child-value)
                  :metadata (semantic-mid-reader-attach-metadata
                             context metadata child-value child-node))
          span (semantic-mid-reader-combined-span
                context token (tokens (dec next-idx)))
          children (cond-> []
                     metadata-id (conj metadata-id)
                     true (conj child-id))
          node {:form-id form-id
                :kind (if (= :metadata abbrev)
                        :metadata-wrapper
                        :abbreviation)
                :abbrev abbrev
                :open-token (token-id token)
                :close-token (:close-token child-node)
                :children children
                :span span
                :surface-span (:span token)
                :metadata (or metadata {})
                :origin {:kind :source :reader :stage1-token-parser}
                :generated-origin
                [{:kind :generated
                  :producer :reader-abbreviation
                  :reason abbrev
                  :from (:span token)
                  :child-form-id child-id}]
                :leading-trivia-token-ids leading-trivia
                :leading-trivia-hash
                (semantic-mid-reader-trivia-hash context leading-trivia)
                :raw (subs source-text
                           (get-in token [:span :start :char])
                           (get-in child-span [:end :char]))
                :expanded-form value
                :value value}]
      (swap! nodes assoc form-id node)
      [value next-idx span form-id])))
