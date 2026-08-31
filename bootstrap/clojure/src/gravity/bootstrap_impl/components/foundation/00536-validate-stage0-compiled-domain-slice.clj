

(defn validate-stage0-compiled-domain-slice!
  [module slice]
  (let [source-path (:source-path module)
        missing-fields (compiler-pass-missing-fields
                        slice
                        stage0-compiled-domain-slice-required-fields)
        document-id (:document-id slice)]
    (when (seq missing-fields)
      (p09-domain-fail!
       "P09-MANIFEST" source-path
       {:document-id document-id
        :domain (:domain slice)
        :profile (:profile slice)
        :target (:target slice)
        :artifact-id (:slice-id slice)
        :missing-fact (first missing-fields)}
       {:missing-fields missing-fields
        :remediation
        "Compiled domain claims must carry the complete slice packet before execution."}))
    (when (contains? #{:full-replacement :platform-wide :provider-replacement}
                     (get-in slice [:replacement-scope :claim-status]))
      (p09-domain-fail!
       "P09-CLAIM" source-path
       {:document-id document-id
        :domain (:domain slice)
        :profile (:profile slice)
        :target (:target slice)
        :artifact-id (:slice-id slice)
        :claim-id (get-in slice [:replacement-scope :claim-id])
        :missing-fact :slice-scoped-claim}
       {:missing-fields [:slice-scoped-claim]
        :remediation
        "Compiled domain metadata may only claim a fixture-backed slice, not a platform-wide or provider-wide replacement."}))
    (when-not (seq (:accepted-fixtures slice))
      (p09-domain-fail!
       "P09-ACCEPTED" source-path
       {:document-id document-id
        :domain (:domain slice)
        :profile (:profile slice)
        :target (:target slice)
        :artifact-id (:slice-id slice)
        :missing-fact :accepted-domain-fixture}
       {:missing-fields [:accepted-fixtures]
        :remediation
        "A compiled domain slice must name accepted fixture evidence for the claimed behavior."}))
    (when-not (seq (:rejected-fixtures slice))
      (p09-domain-fail!
       "P09-REJECTED" source-path
       {:document-id document-id
        :domain (:domain slice)
        :profile (:profile slice)
        :target (:target slice)
        :artifact-id (:slice-id slice)
        :missing-fact :rejected-domain-fixture}
       {:missing-fields [:rejected-fixtures]
        :remediation
        "A compiled domain slice must name rejected fixture evidence for illegal behavior."}))
    (when-not (= :complete (get-in slice [:conformance :status]))
      (p09-domain-fail!
       "P09-CONFORMANCE" source-path
       {:document-id document-id
        :domain (:domain slice)
        :profile (:profile slice)
        :target (:target slice)
        :artifact-id (:slice-id slice)
        :missing-fact :domain-conformance-evidence}
       {:missing-fields [:conformance]
        :remediation
        "A compiled domain slice must record conformance evidence before replacement claims can execute."}))
    (when (and (= "DOM17" document-id)
               (not (true? (:metadata-preserved? slice))))
      (p09-domain-fail!
       "DOM17-METADATA" source-path
       {:document-id document-id
        :domain (:domain slice)
        :profile (:profile slice)
        :target (:target slice)
        :artifact-id (:slice-id slice)
        :missing-fact :metadata_preservation}
       {:missing-fields [:metadata-preserved?]
        :remediation
        "Compiler/tooling domain slices must preserve type, effect, safety, capability, and source metadata."}))))

(defn validate-stage0-compiled-domain!
  [module]
  (when (stage0-compiled-domain-suite-present? module)
    (let [suite (stage0-compiled-domain-suite module)]
      (doseq [slice (:domain-slices suite)]
        (validate-stage0-compiled-domain-slice! module slice)))))

(defn stage0-compiled-schema-suite
  [module]
  (get-in module [:metadata :schema :compiled-gate] {}))

(defn stage0-compiled-schema-suite-present?
  [module]
  (contains? (get-in module [:metadata :schema] {}) :compiled-gate))

(defn compiled-schema-fail!
  [id module subject extra]
  (p10-schema-fail!
   id
   (:source-path module)
   subject
   (merge {:stage :stage0-compiled-schema-gate
           :diagnostic-family :phase10-compiled-schema-data-interop
           :compiled-gate :schema-data-interop
           :remediation
           "Compiled schema/data/interop metadata must preserve source schema authority, taint, canonical hash inputs, API resolver authority, migration safety, ABI ownership, typed configuration redaction, and artifact evidence before instruction-plan execution."}
          extra)))

(defn validate-stage0-compiled-schema-model!
  [module model]
  (when (or (true? (:weakens-source-schema? model))
            (not= (:schema-hash model) p10-schema-hash))
    (compiled-schema-fail!
     "S1-PROJECTION" module model
     {:missing-fields [:non-weakening-source-schema-projection]})))

(defn validate-stage0-compiled-serialization!
  [module serializer canonical]
  (when (or (= :trusted (:decoded-trust serializer))
            (false? (:retains-taint? serializer)))
    (compiled-schema-fail!
     "S2-TAINT" module serializer
     {:missing-fields [:decoded-value-taint]}))
  (when (or (false? (:schema-hash-included canonical))
            (not= p10-schema-hash
                  (get-in canonical [:hash-input-record :schema-hash])))
    (compiled-schema-fail!
     "S3-HASH" module canonical
     {:missing-fields [:canonical-hash-input-schema-hash]})))

(defn validate-stage0-compiled-api-projections!
  [module graphql openapi]
  (when (or (not= :runtime-enforced
                  (get-in graphql [:resolver-adapter :capability-check]))
            (not (contains? (set (:capabilities graphql)) :db/query)))
    (compiled-schema-fail!
     "S4-RESOLVER" module graphql
     {:missing-fields [:resolver-effect-capability]}))
  (when-not (:error-schema openapi)
    (compiled-schema-fail!
     "S5-SCHEMA" module openapi
     {:missing-fields [:request-response-error-schema]})))

(defn validate-stage0-compiled-database!
  [module database]
  (when-not (= :no-data-loss (get-in database [:data-loss-report :policy]))
    (compiled-schema-fail!
     "S6-DATA-LOSS" module database
     {:missing-fields [:destructive-migration-policy]})))

(defn validate-stage0-compiled-binary-abi!
  [module binary-abi]
  (when-not (= :no-raw-pointers-in-stable-record
               (:pointer-policy binary-abi))
    (compiled-schema-fail!
     "S7-POINTER" module binary-abi
     {:missing-fields [:pointer-lifetime-ownership]})))

(defn validate-stage0-compiled-config!
  [module config]
  (when-not (= :redacted
               (get-in config [:redaction-report :database-url]))
    (compiled-schema-fail!
     "S8-SECRET" module config
     {:missing-fields [:secret-redaction]})))

(defn validate-stage0-compiled-artifact-schema!
  [module artifact-schema]
  (when-not (set/subset? #{:types :effects :capabilities :safety
                           :proofs :tests :diagnostics :conformance}
                         (set (:evidence-schema artifact-schema)))
    (compiled-schema-fail!
     "S9-EVIDENCE" module artifact-schema
     {:missing-fields [:artifact-evidence-schema]})))

(defn validate-stage0-compiled-schema!
  [module]
  (when (stage0-compiled-schema-suite-present? module)
    (let [suite (stage0-compiled-schema-suite module)]
      (doseq [model (:schema-models suite)]
        (validate-stage0-compiled-schema-model! module model))
      (doseq [serializer (:serializers suite)]
        (validate-stage0-compiled-serialization!
         module serializer (:canonical serializer)))
      (doseq [projection (:api-projections suite)]
        (validate-stage0-compiled-api-projections!
         module (:graphql projection) (:openapi projection)))
      (doseq [database (:database-mappings suite)]
        (validate-stage0-compiled-database! module database))
      (doseq [binary-abi (:binary-abi-schemas suite)]
        (validate-stage0-compiled-binary-abi! module binary-abi))
      (doseq [config (:typed-configs suite)]
        (validate-stage0-compiled-config! module config))
      (doseq [artifact-schema (:artifact-schemas suite)]
        (validate-stage0-compiled-artifact-schema!
         module artifact-schema)))))

(defn stage0-compiled-ai-suite
  [module]
  (get-in module [:metadata :ai :compiled-gate] {}))

(defn stage0-compiled-ai-suite-present?
  [module]
  (contains? (get-in module [:metadata :ai] {}) :compiled-gate))

(defn compiled-ai-fail!
  [id module subject extra]
  (p11-ai-fail!
   id
   (:source-path module)
   subject
   (merge {:stage :stage0-compiled-ai-gate
           :diagnostic-family :phase11-compiled-ai-agentic
           :compiled-gate :ai-agentic
           :remediation
           "Compiled AI/agentic metadata must preserve model/provider authority, prompt authority partitions, tool schemas, least-privilege capabilities, replay, memory tenancy, policy taint checks, eval gates, human-review payload hashes, and prompt-injection defenses before instruction-plan execution."}
          extra)))

(defn validate-stage0-compiled-ai-program!
  [module program]
  (when-not (= :required-for-write-tool (:human-review-policy program))
    (compiled-ai-fail!
     "AI004" module program
     {:missing-fields [:tool-capability-human-review]})))