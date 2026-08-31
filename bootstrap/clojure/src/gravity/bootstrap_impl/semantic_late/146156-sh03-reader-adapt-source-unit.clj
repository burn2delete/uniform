; Semantic decomposition of committed HEAD reader line 146156.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-sh03-reader-adapt-products!-source-unit
 [state]
 (clojure.core/let
  [{:keys [source-path source-text source-bytes reader-options
           project-context result]}
   state
   source-unit
   (c2-source-unit-record source-path source-text reader-options project-context)
   source-id
   (:source-id source-unit)
   source-content-id
   (get-in result [:source-unit :bytes-hash])
   _
   (when-not
    (= source-content-id (:bytes-hash source-unit))
    (sh03-reader-boundary-fail!
     source-path
     :snapshot-bound-sh03-reader-source-unit
     (:source-unit result)
     {:expected-source-content-id (:bytes-hash source-unit)}))
   scalar-boundaries
   (sh03-reader-source-scalar-boundaries! source-path source-text source-bytes)
   raw-tokens
   (:token-stream result)
   raw-forms
   (:form-tree result)
   semantic-index
   (sh03-reader-semantic-value-index! source-path (:semantic-value-table result))]
  (clojure.core/assoc
   state
   :source-unit
   source-unit
   :source-id
   source-id
   :source-content-id
   source-content-id
   :scalar-boundaries
   scalar-boundaries
   :raw-tokens
   raw-tokens
   :raw-forms
   raw-forms
   :semantic-index
   semantic-index)))
