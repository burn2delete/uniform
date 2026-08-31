(defn- semantic-mid-reader-read-tagged
  [{:keys [source-path source-text tokens token-count nodes token-id] :as context}
   idx token leading-trivia form-id]
  (let [[child-idx child-trivia]
        (semantic-mid-reader-skip-trivia context (inc idx))]
    (when (>= child-idx token-count)
      (stage1-reader-fail!
       "STAGE1READER004" source-path (:raw token)
       {:source-span (:span token)
        :token-id (token-id token)
        :form-id form-id
        :raw (:raw token)
        :extension-tag (:tag token)}))
    (let [[child-value next-idx child-span child-id]
          (semantic-mid-reader-read-form context child-idx child-trivia)
          child-node (@nodes child-id)
          raw (str (:raw token) " " (pr-str child-value))
          value (try
                  (edn/read-string raw)
                  (catch Exception ex
                    (stage1-reader-fail!
                     "STAGE1READER004" source-path raw
                     {:source-span (:span token)
                      :token-id (token-id token)
                      :form-id form-id
                      :raw raw
                      :extension-tag (:tag token)
                      :cause-message (.getMessage ex)})))
          span (semantic-mid-reader-combined-span
                context token (tokens (dec next-idx)))
          node {:form-id form-id
                :kind :tagged-literal
                :tag (:tag token)
                :open-token (token-id token)
                :close-token (:close-token child-node)
                :children [child-id]
                :span span
                :metadata {}
                :origin {:kind :source :reader :stage1-token-parser}
                :leading-trivia-token-ids leading-trivia
                :leading-trivia-hash
                (semantic-mid-reader-trivia-hash context leading-trivia)
                :raw (subs source-text
                           (get-in token [:span :start :char])
                           (get-in child-span [:end :char]))
                :value value}]
      (swap! nodes assoc form-id node)
      [value next-idx span form-id])))

(defn- semantic-mid-reader-read-literal
  [{:keys [nodes token-id] :as context} idx token leading-trivia form-id]
  (let [value (semantic-mid-reader-parse-token-literal context token)
        span (:span token)
        node {:form-id form-id
              :kind (:kind token)
              :open-token (token-id token)
              :close-token (token-id token)
              :token-id (token-id token)
              :children []
              :span span
              :metadata {}
              :origin {:kind :source :reader :stage1-token-parser}
              :leading-trivia-token-ids leading-trivia
              :leading-trivia-hash
              (semantic-mid-reader-trivia-hash context leading-trivia)
              :raw (:raw token)
              :value value}]
    (swap! nodes assoc form-id node)
    [value (inc idx) span form-id]))

(defn- semantic-mid-reader-read-form
  [{:keys [source-path tokens token-count token-id] :as context}
   idx leading-trivia]
  (when (>= idx token-count)
    (stage1-reader-fail! "STAGE1READER002" source-path nil
                         {:source-span {:source source-path}
                          :expected :form}))
  (let [token (tokens idx)
        form-id (semantic-mid-reader-new-form-id context)]
    (case (:kind token)
      :close
      (stage1-reader-fail!
       "STAGE1READER001" source-path (:lexeme token)
       {:source-span (:span token)
        :token-id (token-id token)
        :form-id form-id
        :raw (:raw token)
        :actual (:lexeme token)
        :facts {:actual-delimiter (:lexeme token)}})

      :list-open
      (semantic-mid-reader-read-delimited
       context (inc idx) :list (:close-token token)
       token leading-trivia form-id)

      :vector-open
      (semantic-mid-reader-read-delimited
       context (inc idx) :vector (:close-token token)
       token leading-trivia form-id)

      :map-open
      (semantic-mid-reader-read-delimited
       context (inc idx) :map (:close-token token)
       token leading-trivia form-id)

      :set-open
      (semantic-mid-reader-read-delimited
       context (inc idx) :set (:close-token token)
       token leading-trivia form-id)

      :abbreviation
      (semantic-mid-reader-read-abbreviation
       context idx token leading-trivia form-id)

      :tag
      (semantic-mid-reader-read-tagged
       context idx token leading-trivia form-id)

      (semantic-mid-reader-read-literal
       context idx token leading-trivia form-id))))
