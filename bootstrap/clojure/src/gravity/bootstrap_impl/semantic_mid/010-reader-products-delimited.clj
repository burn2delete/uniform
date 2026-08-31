(defn- semantic-mid-reader-read-delimited
  [{:keys [source-path source-text line-starts tokens token-count nodes token-id]
    :as context}
   idx collection-kind close-token open-token leading-trivia form-id]
  (loop [idx idx
         child-ids []
         values []]
    (let [[idx between-trivia]
          (semantic-mid-reader-skip-trivia context idx)]
      (if (>= idx token-count)
        (let [eof-span (stage1-reader-span
                        source-path source-text line-starts
                        (count source-text) (count source-text))]
          (stage1-reader-fail!
           "STAGE1READER002" source-path nil
           {:source-span eof-span
            :token-id (token-id open-token)
            :form-id form-id
            :raw (:raw open-token)
            :expected close-token
            :facts {:expected-delimiter close-token
                    :open-token
                    (token-id open-token)}}))
        (let [token (tokens idx)]
          (cond
            (and (= :close (:kind token))
                 (= close-token (:lexeme token)))
            (let [span (semantic-mid-reader-combined-span
                        context open-token token)
                  value (case collection-kind
                          :list (apply list values)
                          :vector (vec values)
                          :set (semantic-mid-reader-set-value
                                context child-ids values)
                          :map (do
                                 (when (odd? (count values))
                                   (stage1-reader-fail!
                                    "STAGE1READER005" source-path values
                                    {:source-span span
                                     :token-id
                                     (token-id token)
                                     :form-id form-id
                                     :raw (:raw token)
                                     :facts {:entry-count (count values)}}))
                                 (apply hash-map values)))
                  node {:form-id form-id
                        :kind collection-kind
                        :collection-kind collection-kind
                        :open-token
                        (token-id open-token)
                        :close-token
                        (token-id token)
                        :children child-ids
                        :span span
                        :metadata {}
                        :origin {:kind :source
                                 :reader :stage1-token-parser}
                        :leading-trivia-token-ids leading-trivia
                        :leading-trivia-hash
                        (semantic-mid-reader-trivia-hash
                         context leading-trivia)
                        :trailing-trivia-token-ids between-trivia
                        :trailing-trivia-hash
                        (semantic-mid-reader-trivia-hash
                         context between-trivia)
                        :raw (subs source-text
                                   (get-in open-token [:span :start :char])
                                   (get-in token [:span :end :char]))
                        :value value}]
              (swap! nodes assoc form-id node)
              [value (inc idx) span form-id])

            (= :close (:kind token))
            (stage1-reader-fail!
             "STAGE1READER001" source-path (:lexeme token)
             {:source-span (:span token)
              :token-id (token-id token)
              :form-id form-id
              :raw (:raw token)
              :expected close-token
              :actual (:lexeme token)
              :facts {:expected-delimiter close-token
                      :actual-delimiter (:lexeme token)}})

            :else
            (let [[value next-idx _span child-id]
                  (semantic-mid-reader-read-form
                   context idx between-trivia)]
              (recur next-idx
                     (conj child-ids child-id)
                     (conj values value)))))))))
