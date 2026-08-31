; Semantic decomposition of committed HEAD reader line 146156.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-sh03-reader-adapt-products!-host-values
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
     host-values]}
   state
   token-stream
   (mapv
    (fn
     [token]
     (let
      [kind
       (:kind token)
       raw
       (sh03-reader-accepted-raw-text!
        source-path
        source-bytes
        source-content-id
        scalar-boundaries
        (:raw token)
        (:span token))
       descriptor
       (if
        (= :gravity/semantic-value-reference (:artifact (:descriptor token)))
        (sh03-reader-semantic-reference-value!
         source-path
         semantic-index
         (:descriptor token)
         :descriptor)
        (:descriptor token))
       decoded
       (cond
        (contains? #{:whitespace :comment} kind)
        nil
        (contains? #{:list-open :vector-open :close :map-open} kind)
        raw
        (= :set-open kind)
        :set
        (= :abbreviation kind)
        (:abbreviation token)
        (= :reader-tag kind)
        (symbol (sh03-reader-codepoints-text! source-path (:tag-codepoints token)))
        :else
        (sh03-reader-host-atomic-value!
         source-path
         source-bytes
         source-content-id
         scalar-boundaries
         token
         descriptor))]
      (cond->
       {:reader-origin :source,
        :raw raw,
        :token-id (token-id-map (:token-id token)),
        :decoded decoded,
        :source-path source-path,
        :lexeme (if (contains? #{:string :character} kind) decoded raw),
        :kind (if (= :reader-tag kind) :tag kind),
        :trivia-before [],
        :source-id source-id,
        :span (sh03-reader-path-span source-path source-id (:span token))}
       (true? (:trivia? token))
       (assoc :trivia? true)
       (= :abbreviation kind)
       (assoc :abbrev (:abbreviation token))
       (= :reader-tag kind)
       (assoc :tag decoded)
       (contains? #{:list-open :set-open :vector-open :map-open} kind)
       (assoc :close-token ({:list-open ")", :vector-open "]", :map-open "}", :set-open "}"} kind))
       (= :set-open kind)
       (assoc :dispatch "#"))))
    raw-tokens)]
  (clojure.core/assoc state :token-stream token-stream)))
