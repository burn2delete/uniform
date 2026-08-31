; Semantic decomposition of HEAD reader line 12567.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-typed-artifact-phase-12
 [artifact state]
 (let
  [{:keys [checked]} state]
  (assoc
   artifact
   :safe10-attenuation-records
   (distinct-records (:safe10-attenuation-records checked))
   :interop-schema-drift-records
   (distinct-records (:interop-schema-drift-records checked))
   :standard-library-namespace-contracts
   (distinct-records (:standard-library-namespace-contracts checked))
   :alternative-type-gradual-boundaries
   (distinct-records (:alternative-type-gradual-boundaries checked))
   :pattern-ownership-facts
   (distinct-records (:pattern-ownership-facts checked))
   :safe9-numeric-mode-records
   (distinct-records (:safe9-numeric-mode-records checked))
   :safe10-grant-intersection-records
   (distinct-records (:safe10-grant-intersection-records checked))
   :safe7-foreign-declaration-records
   (distinct-records (:safe7-foreign-declaration-records checked))
   :ffi-error-mapping-artifacts
   (distinct-records (:ffi-error-mapping-artifacts checked))
   :safe10-provider-selection-records
   (distinct-records (:safe10-provider-selection-records checked))
   :cache-key-records
   (distinct-records (:cache-key-records checked))
   :interop-type-mapping-records
   (distinct-records (:interop-type-mapping-records checked))
   :safe16-conformance-fixture
   (safe16-conformance-fixture checked)
   :safe9-conformance-records
   (distinct-records (:safe9-conformance-records checked))
   :alternative-memory-ffi-allocator-records
   (distinct-records
    (:alternative-memory-ffi-allocator-records checked))
   :safe-memory-ownership-graphs
   (distinct-records (:safe-memory-ownership-graphs checked))
   :safe10-usage-summaries
   (distinct-records (:safe10-usage-summaries checked))
   :safe7-callback-safety-records
   (distinct-records (:safe7-callback-safety-records checked)))))
