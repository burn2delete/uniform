; Semantic decomposition of committed HEAD reader line 146156.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-sh03-reader-adapt-products!-tokens
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
     token-stream]}
   state
   tokens-by-id
   (into {} (map (juxt :token-id identity) token-stream))
   raw-token-position
   (into {} (map-indexed (fn [index token] [(:token-id token) index]) raw-tokens))
   form-tree
   (mapv
    (fn
     [form]
     (let
      [value
       (host-values (:form-id form))
       kind
       (:kind form)
       open-position
       (raw-token-position (:open-token form))
       close-position
       (raw-token-position (:close-token form))
       leading
       (if
        (integer? open-position)
        (sh03-reader-contiguous-trivia raw-tokens open-position -1 token-id-map)
        [])
       trailing
       (if
        (and (:collection-kind form) (integer? close-position))
        (sh03-reader-contiguous-trivia raw-tokens close-position -1 token-id-map)
        [])
       child-ids
       (mapv form-id-map (:children form))
       target-child-id
       (when
        (contains? #{:metadata-wrapper :abbreviation} kind)
        (if (= :metadata-wrapper kind) (second child-ids) (first child-ids)))
       prefix-span
       (when
        (contains? #{:metadata-wrapper :abbreviation} kind)
        (:span (tokens-by-id (token-id-map (:open-token form)))))
       metadata
       (if (= :metadata-wrapper kind) (or (meta value) {}) (or (meta value) {}))]
      (cond->
       {:children child-ids,
        :open-token (token-id-map (:open-token form)),
        :raw
        (sh03-reader-accepted-raw-text!
         source-path
         source-bytes
         source-content-id
         scalar-boundaries
         (:raw form)
         (:span form)),
        :value value,
        :source-path source-path,
        :parent-form-id (form-id-map (:parent-form-id form)),
        :close-token (token-id-map (:close-token form)),
        :kind kind,
        :leading-trivia-token-ids leading,
        :origin
        {:kind :source,
         :reader :gravity-sh03-reader,
         :projection :clojure-c2-compatibility-adapter,
         :source-id source-id,
         :source-path source-path},
        :source-id source-id,
        :form-id (form-id-map (:form-id form)),
        :metadata metadata,
        :leading-trivia-hash (sh03-reader-legacy-trivia-hash leading tokens-by-id),
        :span (sh03-reader-path-span source-path source-id (:span form))}
       (empty? (:children form))
       (assoc :token-id (token-id-map (:open-token form)))
       (:collection-kind form)
       (assoc
        :collection-kind
        (:collection-kind form)
        :trailing-trivia-token-ids
        trailing
        :trailing-trivia-hash
        (sh03-reader-legacy-trivia-hash trailing tokens-by-id))
       (= :tagged-literal kind)
       (assoc :tag (symbol (sh03-reader-codepoints-text! source-path (:tag-codepoints form))))
       (contains? #{:metadata-wrapper :abbreviation} kind)
       (assoc
        :abbrev
        (:abbrev form)
        :surface-span
        prefix-span
        :expanded-form
        value
        :generated-origin
        [{:kind :generated,
          :producer :reader-abbreviation,
          :reason (:abbrev form),
          :from prefix-span,
          :child-form-id target-child-id}]))))
    raw-forms)]
  (clojure.core/assoc
   state
   :tokens-by-id
   tokens-by-id
   :raw-token-position
   raw-token-position
   :form-tree
   form-tree)))
