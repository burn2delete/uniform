; Semantic decomposition of committed HEAD reader line 146156.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-sh03-reader-adapt-products!-boundary
 [state]
 (clojure.core/let
  [{:keys
    [source-path
     project-context
     resolved
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
     adapted-evidence]}
   state
   descriptor
   (sh03-reader-adapter-summary
    result
    source-unit
    token-stream
    form-tree
    (:reader-extension-invocation-records adapted-evidence)
    token-id-map
    form-id-map)
   envelope-descriptor
   (sh03-reader-sh02-descriptor source-path project-context resolved descriptor)
   envelope-options
   (sh03-reader-authenticated-envelope-options source-path (:plan-binding resolved))
   envelope
   (apply p15-s23-stage2-sh02-descriptor-envelope
          (cond-> [:c2-reader :gravity/sh03-reader-products envelope-descriptor source-path]
            envelope-options (conj envelope-options)))
   _
   (apply p15-s23-stage2-sh02-descriptor-envelope-verify!
          (cond-> [envelope :c2-reader :gravity/sh03-reader-products envelope-descriptor source-path]
            envelope-options (conj envelope-options)))]
  (clojure.core/assoc
   state
   :descriptor
   descriptor
   :envelope-descriptor
   envelope-descriptor
   :envelope
   envelope)))
