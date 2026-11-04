
(defn stage1-reader-table-driven-records
  [source-path ^String source-text table]
  (gravity.reader/parse-compatibility-records
   source-path source-text table
   {:line-starts-var #'line-start-indices :line-terminator-var #'line-terminator-char?
    :fail-var #'stage1-reader-fail! :span-var #'stage1-reader-span
    :starts-with-var #'str/starts-with? :form-kind-var #'form-kind}))

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