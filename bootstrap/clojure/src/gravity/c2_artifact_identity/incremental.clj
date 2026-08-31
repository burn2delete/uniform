(ns gravity.c2-artifact-identity.incremental)

(defn hashes
  [{:keys [c2-form-graph-metrics c2-reader-fail! source-span
           max-reader-form-graph-depth c2-semantic-form-hash-input
           c2-token-hash-input c2-form-hash-input c2-syntax-seed-hash-input
           c2-extension-hash-input c2-diagnostic-hash-input reader-canonical-hash]}
   source-unit token-stream form-tree syntax-seeds extension-invocations diagnostics]
  (let [graph-metrics (c2-form-graph-metrics form-tree)
        max-depth (:max-form-depth graph-metrics)
        depth-limit max-reader-form-graph-depth
        subject {:stage :read-source
                 :source-id (:source-id source-unit)
                 :source-span (or (:span (first form-tree))
                                  (source-span (:path source-unit) 0))
                 :reader-options (:reader-options source-unit)}
        _ (when-not (:acyclic? graph-metrics)
            (c2-reader-fail! "C2-HASH" (:path source-unit) subject
                             {:missing-fields [:acyclic-reader-form-graph]
                              :facts {:failure-kind :reader-form-cycle}}))
        _ (when (> max-depth depth-limit)
            (c2-reader-fail! "C2-HASH" (:path source-unit) subject
                             {:missing-fields [:bounded-reader-form-depth]
                              :facts {:observed-form-depth max-depth
                                      :maximum-form-depth depth-limit
                                      :failure-kind :reader-resource-depth-limit}}))
        retain-trivia? (true? (get-in source-unit [:reader-options :retain-comments]))
        form-hash-input ((if retain-trivia? c2-form-hash-input
                             c2-semantic-form-hash-input) form-tree)]
    {:artifact :gravity/reader-incremental-hashes
     :source-unit (:source-id source-unit)
     :token-stream (reader-canonical-hash (c2-token-hash-input token-stream))
     :form-tree (reader-canonical-hash form-hash-input)
     :syntax-seed-stream (reader-canonical-hash
                          (c2-syntax-seed-hash-input syntax-seeds))
     :extension-invocation-set (reader-canonical-hash
                                (c2-extension-hash-input extension-invocations))
     :reader-diagnostics (reader-canonical-hash
                          (c2-diagnostic-hash-input diagnostics))
     :retained-trivia-affects-form-tree? retain-trivia?
     :status :stable}))
