

;; Keep the reader policy value after all C2 artifact-identity operation Vars
;; used by its strict validation boundary are bound.  The policy hash is
;; computed while this namespace is loading; defining it earlier would pass an
;; unbound c2-form-graph-metrics Var into c2-artifact-identity/with-operations.
(def standard-reader-options
  {:retain-comments true
   :enabled-features #{:standard-reader}
   :extension-policy (reader-canonical-hash standard-reader-policy)})