
(defn stage1-reader-table-driven-records
  [source-path ^String source-text table]
  (let [line-starts (line-start-indices source-text)
        whitespace (set (:whitespace table))
        delimiters (:delimiters table)
        dispatch (:dispatch table)
        comment-marker (:line-comment table)
        string-delimiter (:string-delimiter table)
        source-length (count source-text)]
    (letfn [(char-string [idx]
              (str (.charAt source-text idx)))
            (ignored? [idx]
              (and (< idx source-length)
                   (contains? whitespace (char-string idx))))
            (comment? [idx]
              (and (< idx source-length)
                   (= comment-marker (char-string idx))))
            (skip-ignored [idx]
              (loop [idx idx]
                (cond
                  (>= idx source-length) idx
                  (ignored? idx) (recur (inc idx))
                  (comment? idx) (recur (loop [j idx]
                                          (if (and (< j source-length)
                                                   (not (line-terminator-char?
                                                         (.charAt source-text j))))
                                            (recur (inc j))
                                            j)))
                  :else idx)))
            (close-token? [idx close-token]
              (and (< idx source-length)
                   (= close-token (char-string idx))))
            (unexpected-close? [idx]
              (and (< idx source-length)
                   (= :close (get-in delimiters [(char-string idx) :kind]))))
            (read-string-literal [idx]
              (let [start idx
                    builder (StringBuilder.)]
                (loop [idx (inc idx)]
                  (cond
                    (>= idx source-length)
                    (stage1-reader-fail! "STAGE1READER003" source-path nil
                                         {:source-span
                                          (stage1-reader-span source-path
                                                              source-text
                                                              line-starts
                                                              start idx)})

                    (= string-delimiter (char-string idx))
                    [(.toString builder)
                     (inc idx)
                     (stage1-reader-span source-path source-text
                                         line-starts start (inc idx))]

                    (= \\ (.charAt source-text idx))
                    (if (< (inc idx) source-length)
                      (do
                        (.append builder (.charAt source-text (inc idx)))
                        (recur (+ idx 2)))
                      (stage1-reader-fail! "STAGE1READER003" source-path nil
                                           {:source-span
                                            (stage1-reader-span source-path
                                                                source-text
                                                                line-starts
                                                                start idx)}))

                    :else
                    (do
                      (.append builder (.charAt source-text idx))
                      (recur (inc idx)))))))
            (atom-end [idx]
              (loop [idx idx]
                (if (or (>= idx source-length)
                        (ignored? idx)
                        (comment? idx)
                        (contains? delimiters (char-string idx))
                        (contains? dispatch (char-string idx)))
                  idx
                  (recur (inc idx)))))
            (parse-atom [token]
              (cond
                (= token "nil") nil
                (= token "true") true
                (= token "false") false
                (str/starts-with? token ":") (keyword (subs token 1))
                (re-matches #"-?[0-9]+" token) (Long/parseLong token)
                :else (symbol token)))
            (read-atom [idx]
              (let [start idx
                    end (atom-end idx)
                    token (subs source-text start end)]
                [(parse-atom token)
                 end
                 (stage1-reader-span source-path source-text
                                     line-starts start end)]))
            (read-delimited [idx collection-kind close-token]
              (let [start idx]
                (loop [idx (inc idx)
                       values []]
                  (let [idx (skip-ignored idx)]
                    (cond
                      (>= idx source-length)
                      (stage1-reader-fail! "STAGE1READER002" source-path nil
                                           {:source-span
                                            (stage1-reader-span source-path
                                                                source-text
                                                                line-starts
                                                                start idx)
                                            :expected close-token})

                      (close-token? idx close-token)
                      (let [end (inc idx)
                            value (case collection-kind
                                    :list (apply list values)
                                    :vector (vec values)
                                    :set (set values)
                                    :map (do
                                           (when (odd? (count values))
                                             (stage1-reader-fail!
                                              "STAGE1READER005" source-path values
                                              {:source-span
                                               (stage1-reader-span
                                                source-path source-text
                                                line-starts start end)}))
                                           (apply hash-map values)))]
                        [value
                         end
                         (stage1-reader-span source-path source-text
                                             line-starts start end)])

                      :else
                      (let [[value next-idx _span] (read-form idx)]
                        (recur next-idx (conj values value))))))))
            (read-dispatch [idx]
              (let [dispatch-token (char-string idx)
                    next-token (when (< (inc idx) source-length)
                                 (char-string (inc idx)))
                    dispatch-entry (get-in dispatch [dispatch-token next-token])]
                (if (= :set-open (:kind dispatch-entry))
                  (let [[value next-idx span]
                        (read-delimited (inc idx) :set (:closes dispatch-entry))]
                    [value next-idx (assoc span :dispatch dispatch-token)])
                  (stage1-reader-fail! "STAGE1READER004" source-path dispatch-token
                                       {:source-span
                                        (stage1-reader-span source-path
                                                            source-text
                                                            line-starts
                                                            idx
                                                            (min source-length
                                                                 (+ idx 2)))}))))
            (read-form [idx]
              (let [idx (skip-ignored idx)]
                (when (>= idx source-length)
                  (stage1-reader-fail! "STAGE1READER002" source-path nil
                                       {:source-span
                                        (stage1-reader-span source-path
                                                            source-text
                                                            line-starts
                                                            idx idx)}))
                (let [token (char-string idx)
                      delimiter (get delimiters token)]
                  (cond
                    (unexpected-close? idx)
                    (stage1-reader-fail! "STAGE1READER001" source-path token
                                         {:source-span
                                          (stage1-reader-span source-path
                                                              source-text
                                                              line-starts
                                                              idx (inc idx))})

                    (= string-delimiter token)
                    (read-string-literal idx)

                    (= :list-open (:kind delimiter))
                    (read-delimited idx :list (:closes delimiter))

                    (= :vector-open (:kind delimiter))
                    (read-delimited idx :vector (:closes delimiter))

                    (= :map-open (:kind delimiter))
                    (read-delimited idx :map (:closes delimiter))

                    (contains? dispatch token)
                    (read-dispatch idx)

                    :else
                    (read-atom idx)))))]
      (loop [idx 0
             form-index 0
             records []]
        (let [idx (skip-ignored idx)]
          (if (>= idx source-length)
            records
            (let [[form next-idx span] (read-form idx)]
              (recur next-idx
                     (inc form-index)
                     (conj records
                           {:form form
                            :kind (form-kind form)
                            :span (assoc span :form-index form-index)})))))))))

(defn stage1-reader-execution-diagnostic-stream
  [source-path table-id]
  {:artifact :gravity/stage1-reader-execution-diagnostic-stream
   :stage :stage1-reader-execution
   :source-path source-path
   :reader-table-id table-id
   :diagnostics
   (mapv (fn [id]
           {:diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :stage1-reader-execution
            :artifact :gravity/diagnostic
            :message (stage1-reader-execution-diagnostic-messages id)})
         stage1-reader-execution-diagnostic-ids)
   :status :complete})