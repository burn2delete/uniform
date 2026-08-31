; Semantic decomposition of committed HEAD reader line 146156.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-sh03-reader-adapt-products!-forms
 [state]
 (clojure.core/let
  [{:keys
    [source-path
     source-bytes
     result
     source-unit
     source-id
     source-content-id
     scalar-boundaries
     raw-tokens
     raw-forms
     semantic-index
     token-id-map
     form-id-map
     host-values
     token-stream
     tokens-by-id
     raw-token-position
     form-tree]}
   state
   forms-by-id
   (into {} (map (juxt :form-id identity) form-tree))
   root-form-ids
   (mapv form-id-map (:top-level-form-ids result))
   parsed-records
   (mapv
    (fn
     [root-index form-id]
     (let
      [form (forms-by-id form-id)]
      {:form (:value form),
       :kind (:kind form),
       :form-id form-id,
       :span (assoc (:span form) :form-index root-index),
       :parent-form-id nil}))
    (range)
    root-form-ids)
   adapted-evidence
   (sh03-reader-adapt-evidence!
    source-path
    source-id
    source-bytes
    source-content-id
    scalar-boundaries
    result
    form-id-map
    token-id-map
    token-stream
    form-tree)]
  (clojure.core/assoc
   state
   :forms-by-id
   forms-by-id
   :root-form-ids
   root-form-ids
   :parsed-records
   parsed-records
   :adapted-evidence
   adapted-evidence)))
