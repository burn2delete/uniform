(defn
 sh04-syntax-resolve-template!
 [source-path binding raw reader-binding reader-source-revision]
 (gravity.syntax/resolve-template!
  source-path
  binding
  raw
  reader-binding
  reader-source-revision
  (syntax-stage-runtime-operations)))

(defn sh04-syntax-logical-stem
  [source-path source-unit]
  (-> (or (:project-relative-path source-unit)
          (.getName (java.io.File. source-path)))
      reader-normalize-relative-path
      (str/replace #"\.(gravity|qst)$" "")))

(defn sh04-syntax-semantic-source-id
  [source-path source-unit]
  (reader-canonical-hash
   {:domain :gravity/sh04-co-canonical-source-v1
    :logical-source-stem (sh04-syntax-logical-stem source-path source-unit)
    :encoding (:encoding source-unit)
    :bytes-hash (:bytes-hash source-unit)
    :reader-options (:reader-options source-unit)}))

(defn sh04-syntax-semantic-span
  [span semantic-source-id]
  {:source (:source span)
   :file semantic-source-id
   :byte-start (:byte-start span)
   :byte-end (:byte-end span)
   :scalar-start (or (:scalar-start span) (get-in span [:start :char]))
   :scalar-end (or (:scalar-end span) (get-in span [:end :char]))
   :line-start (or (:line-start span) (get-in span [:start :line]))
   :column-start (or (:column-start span) (get-in span [:start :column]))
   :line-end (or (:line-end span) (get-in span [:end :line]))
   :column-end (or (:column-end span) (get-in span [:end :column]))})

(def sh04-syntax-sh03-product-binding-keys
  [:adapter-contract :adapted-source-unit-id :adapted-token-stream-id
   :adapted-form-tree-id :adapted-extension-invocation-set-id])

(defn sh04-syntax-current-sh03-product-binding
  [c2-artifact]
  {:adapter-contract
   (or (get-in c2-artifact [:gravity-reader-boundary :adapter-contract])
       :gravity/sh03-to-c2-reader-products-v2)
   :adapted-source-unit-id
   (get-in c2-artifact [:source-unit-record :source-id])
   :adapted-token-stream-id
   (reader-canonical-hash (c2-token-hash-input (:token-stream c2-artifact)))
   :adapted-form-tree-id
   (reader-canonical-hash (c2-form-hash-input (:form-tree c2-artifact)))
   :adapted-extension-invocation-set-id
   (reader-canonical-hash
    (c2-extension-hash-input
     (:reader-extension-invocation-records c2-artifact)))})

(defn sh04-syntax-descriptor-sh03-product-binding
  [descriptor]
  (let [summary
        (:value
         (some #(when (= :reader-product-identities (:name %)) %)
               (:semantic-projections descriptor)))]
    (when (map? summary)
      (select-keys summary sh04-syntax-sh03-product-binding-keys))))