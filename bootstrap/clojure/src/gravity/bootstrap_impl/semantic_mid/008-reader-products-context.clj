(defn- semantic-mid-reader-products-context
  [source-path source-text table token-stream]
  (let [expected-source-id (str "sha256:" (sha256-hex source-text))
        expected-table-id (str "sha256:" (sha256-hex (pr-str table)))
        tokens (:tokens token-stream)]
    (when-not (= :gravity/stage1-reader-token-stream (:kind token-stream))
      (stage1-reader-pipeline-fail! "STAGE1PIPE004" source-path token-stream
                                    {:missing-fields [:kind]}))
    (when-not (= source-path (:source-path token-stream))
      (stage1-reader-pipeline-fail! "STAGE1PIPE004" source-path token-stream
                                    {:missing-fields [:source-path]}))
    (when-not (= expected-source-id (:source-id token-stream))
      (stage1-reader-pipeline-fail! "STAGE1PIPE004" source-path token-stream
                                    {:missing-fields [:source-id]}))
    (when-not (= expected-table-id (:reader-table-id token-stream))
      (stage1-reader-pipeline-fail! "STAGE1PIPE004" source-path token-stream
                                    {:missing-fields [:reader-table-id]}))
    (when-not (and (vector? tokens)
                   (= (count tokens) (:token-count token-stream)))
      (stage1-reader-pipeline-fail! "STAGE1PIPE004" source-path token-stream
                                    {:missing-fields [:tokens
                                                      :token-count]}))
    (let [line-starts (line-start-indices source-text)
          token-count (count tokens)
          token-index (delay (stage1-reader-token-index tokens))
          nodes (atom {})
          form-order (atom [])
          next-form-index (atom 0)
          token-id (fn [token]
                     (or (:token-id token)
                         (keyword (str "tok-" (:index token)))))]
      {:source-path source-path
       :source-text source-text
       :line-starts line-starts
       :tokens tokens
       :token-count token-count
       :token-index token-index
       :nodes nodes
       :form-order form-order
       :next-form-index next-form-index
       :token-id token-id})))

(defn- semantic-mid-reader-new-form-id
  [{:keys [next-form-index form-order]}]
  (let [idx (swap! next-form-index inc)
        id (keyword (str "form-" (dec idx)))]
    (swap! form-order conj id)
    id))

(defn- semantic-mid-reader-combined-span
  [{:keys [source-path source-text line-starts]} start-token end-token]
  (stage1-reader-span source-path source-text line-starts
                      (get-in start-token [:span :start :char])
                      (get-in end-token [:span :end :char])))

(defn- semantic-mid-reader-skip-trivia
  [{:keys [tokens token-count token-id]} idx]
  (loop [idx idx
         trivia []]
    (if (and (< idx token-count)
             (true? (:trivia? (tokens idx))))
      (recur (inc idx)
             (conj trivia (token-id (tokens idx))))
      [idx trivia])))

(defn- semantic-mid-reader-trivia-hash
  [{:keys [token-index]} ids]
  (let [by-id @token-index]
    (reader-canonical-hash
     (mapv (fn [id]
             (let [token (by-id id)]
               {:kind (:kind token)
                :raw (:raw token)
                :span (dissoc (:span token) :source :file)}))
           ids))))

(defn- semantic-mid-reader-parse-token-literal
  [{:keys [source-path token-id]} token]
  (if (contains? token :decoded)
    (:decoded token)
    (stage1-reader-decode-atom
     source-path (:kind token) (:lexeme token) (:span token)
     (token-id token))))
