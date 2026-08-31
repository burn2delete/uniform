; Semantic decomposition of HEAD reader line 12567.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-typed-artifact-phase-02
 [artifact state]
 (let
  [{:keys [checked host-dispatch-records]} state]
  (assoc
   artifact
   :branch-type-narrowing-table
   (distinct-records (:branch-type-narrowing-table checked))
   :alternative-memory-conformance-fixture
   (alternative-memory-conformance-fixture checked)
   :concurrency-conformance-fixture
   (concurrency-conformance-fixture checked)
   :standard-library-profile-availability-reports
   (distinct-records
    (:standard-library-profile-availability-reports checked))
   :standard-library-api-contracts
   (distinct-records (:standard-library-api-contracts checked))
   :function-signature-table
   (:function-signature-table checked)
   :safe14-native-dependency-records
   (distinct-records (:safe14-native-dependency-records checked))
   :alternative-memory-provider-declarations
   (distinct-records
    (:alternative-memory-provider-declarations checked))
   :capability-provider-conformance-fixture
   (capability-provider-conformance-fixture checked)
   :safe11-deserialization-records
   (distinct-records (:safe11-deserialization-records checked))
   :safe9-runtime-check-records
   (distinct-records (:safe9-runtime-check-records checked))
   :standard-library-documentation-examples
   (distinct-records
    (:standard-library-documentation-examples checked))
   :host-interop-dispatch-records
   host-dispatch-records
   :alternative-macro-equivalence-reports
   (distinct-records (:alternative-macro-equivalence-reports checked))
   :function-latent-effect-table
   (mapv
    (fn*
     [p1__206#]
     (select-keys p1__206# [:node-id :latent-effects :capabilities]))
    (:function-signature-table checked))
   :safe14-package-safety-manifests
   (distinct-records (:safe14-package-safety-manifests checked))
   :safe16-runtime-check-inspections
   (distinct-records (:safe16-runtime-check-inspections checked))
   :facet-generated-gravity-records
   (distinct-records (:facet-generated-gravity-records checked)))))
