; Semantic decomposition of committed HEAD reader line 146156.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/let
 [semantic-late-sh03-reader-adapt-products!-source-unit
  semantic-late-sh03-reader-adapt-products!-source-unit
  semantic-late-sh03-reader-adapt-products!-identities
  semantic-late-sh03-reader-adapt-products!-identities
  semantic-late-sh03-reader-adapt-products!-host-values
  semantic-late-sh03-reader-adapt-products!-host-values
  semantic-late-sh03-reader-adapt-products!-tokens
  semantic-late-sh03-reader-adapt-products!-tokens
  semantic-late-sh03-reader-adapt-products!-forms
  semantic-late-sh03-reader-adapt-products!-forms
  semantic-late-sh03-reader-adapt-products!-boundary
  semantic-late-sh03-reader-adapt-products!-boundary]
 (defn
  sh03-reader-adapt-products!
  [source-path source-text source-bytes reader-options project-context resolved]
  (let
   [result (:result resolved)]
   (sh03-reader-raise-rejection! source-path source-bytes reader-options project-context result)
   (when-not
    (= :accepted (:status result))
    (sh03-reader-boundary-fail! source-path :accepted-sh03-reader-result result {}))
   (clojure.core/let
     [state-0
     {:source-path source-path,
      :source-text source-text,
      :source-bytes source-bytes,
      :reader-options reader-options,
      :project-context project-context,
      :resolved resolved,
      :result result}
     state-1
     (semantic-late-sh03-reader-adapt-products!-source-unit state-0)
     state-2
     (semantic-late-sh03-reader-adapt-products!-identities state-1)
     state-3
     (semantic-late-sh03-reader-adapt-products!-host-values state-2)
     state-4
     (semantic-late-sh03-reader-adapt-products!-tokens state-3)
     state-5
     (semantic-late-sh03-reader-adapt-products!-forms state-4)
     state-6
     (semantic-late-sh03-reader-adapt-products!-boundary state-5)]
    (clojure.core/let
     [{:keys
       [source-path
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
        form-tree
        forms-by-id
        root-form-ids
        parsed-records
        adapted-evidence
        descriptor
        envelope-descriptor
        envelope]}
      state-6]
     {:root-form-ids root-form-ids,
      :sh03-reader-raw-result (:raw-result resolved),
      :form-tree form-tree,
      :sh03-semantic-value-table-id (:semantic-value-table-id descriptor),
      :token-stream token-stream,
      :sh03-reader-plan-binding (:plan-binding resolved),
      :sh03-reader-verification-report (:verification-report resolved),
      :literal-decoding-records (:literal-decoding-records adapted-evidence),
      :sh02-reader-envelope envelope,
      :gravity-reader-source-map (:reader-source-map adapted-evidence),
      :sh03-reader-adapter-contract :gravity/sh03-to-c2-reader-products-v2,
      :parsed-records parsed-records,
      :reader-extension-invocation-records (:reader-extension-invocation-records adapted-evidence),
      :sh03-reader-adapter-descriptor descriptor,
      :source-unit source-unit,
      :sh02-reader-envelope-descriptor envelope-descriptor,
      :sh03-reader-result result,
      :parsed-values (mapv :form parsed-records),
      :deferred-literal-records (:deferred-literal-records adapted-evidence)})))))
