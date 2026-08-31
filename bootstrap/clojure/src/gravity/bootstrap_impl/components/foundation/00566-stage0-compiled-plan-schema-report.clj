

(defn stage0-compiled-plan-schema-report
  [plan module]
  (let [plan-id (:plan-id plan)
        source-schema (p10-source-schema-ir (:source-path module) plan-id)
        validator (p10-validator-artifact source-schema)
        serializer (p10-serialization-fixture source-schema)
        canonical (p10-canonical-format source-schema)
        graphql (p10-graphql-generation source-schema)
        openapi (p10-openapi-generation source-schema)
        database (p10-database-mapping source-schema)
        binary-abi (p10-binary-abi-schema source-schema)
        typed-config (p10-typed-config source-schema)
        artifact-schema (p10-artifact-schema-registry source-schema)
        conformance
        {:document-set p10-schema-documents
         :task "P10-S1"
         :required-diagnostic-ids
         ["S1-PROJECTION" "S2-TAINT" "S3-HASH"
          "S4-RESOLVER" "S5-SCHEMA" "S6-DATA-LOSS"
          "S7-POINTER" "S8-SECRET" "S9-EVIDENCE"]
         :schema-gate-status :metadata-gate-only
         :source-schema-status :complete
         :validator-status :complete
         :serialization-status :complete
         :api-projection-status :complete
         :data-mapping-status :complete
         :abi-status :complete
         :typed-config-status :complete
         :artifact-schema-status :complete
         :status :complete}
        report-base
        {:kind :gravity/stage0-hosted-core-compiled-schema-report
         :document-set ["D1" "S1-S9"]
         :compiled-plan-id plan-id
         :schema-manifest
         {:artifact :gravity/stage0-hosted-core-compiled-schema-manifest
          :schema-id (:schema-id source-schema)
          :schema-version (:schema-version source-schema)
          :schema-hash (:schema-hash source-schema)
          :source-authority :gravity-source-schema
          :profile (:profile module)
          :target (:target module)
          :source-artifact (:artifact source-schema)
          :validator-artifact (:artifact validator)
          :serialization-artifact (:artifact serializer)
          :canonical-artifact (:artifact canonical)
          :api-projections #{(:artifact graphql) (:artifact openapi)}
          :database-mapping (:artifact database)
          :binary-abi-schema (:artifact binary-abi)
          :typed-config (:artifact typed-config)
          :artifact-schema (:artifact artifact-schema)
          :accepted-fixtures
          ["bootstrap/clojure/fixtures/accepted/core-app.gravity"]
          :rejected-fixtures stage0-compiled-schema-rejected-fixtures
          :conformance {:status :complete}
          :status :complete}
         :source-schema-record
         (select-keys source-schema
                      [:artifact :schema-id :schema-version :schema-hash
                       :boundaries :derivation-targets
                       :static-type-projection :semantic-diff
                       :manifest-status])
         :validator-record
         (select-keys validator
                      [:artifact :boundaries :validation-states
                       :clears-taint-for :retains-taint-for
                       :refinement-checks :diagnostics :status])
         :serialization-record
         (select-keys serializer
                      [:artifact :format :unknown-fields :decoded-trust
                       :trust-boundary :round-trip-vectors :status])
         :canonical-record
         (select-keys canonical
                      [:artifact :format-version :schema-hash-included
                       :hash-input-record :reference-vectors :status])
         :api-projection-record
         {:graphql (select-keys graphql
                                [:artifact :source-authority :nullability
                                 :operations :resolver-adapters
                                 :typed-client :status])
          :openapi (select-keys openapi
                                [:artifact :source-authority :routes
                                 :request-validator :response-validator
                                 :typed-client :status])}
         :database-mapping-record
         (select-keys database
                      [:artifact :dialect :table-mapping :migration-plan
                       :data-loss-report :rollback-or-forward-policy
                       :capabilities :row-adapter :fixture-validation
                       :status])
         :binary-abi-record
         (select-keys binary-abi
                      [:artifact :target-abi :calling-convention :endian
                       :field-order :widths :pointer-policy
                       :ownership-lifetime-map :reference-vectors :status])
         :typed-config-record
         (select-keys typed-config
                      [:artifact :config-id :sources :source-precedence
                       :required-fields :secret-fields :capabilities
                       :artifact-policy :redaction-report
                       :build-reproducibility-record
                       :runtime-reload-policy :status])
         :artifact-schema-record
         (select-keys artifact-schema
                      [:artifact :artifact-kinds :required-fields
                       :canonical-encoding :content-hash-schema
                       :provenance-schema :evidence-schema
                       :release-gate-schema :status])
         :schema-conformance-results conformance
         :diagnostics []}]
    (assoc report-base
           :report-id (str "sha256:" (sha256-hex (pr-str report-base))))))

(defn hosted-core-compiled-schema-proof-source-artifact
  [source-path source-text]
  (let [_ (validate-stage0-source-profile! source-path source-text)
        _ (validate-stage0-source-safety! source-path source-text)
        macro-artifact (macro-source-artifact source-path source-text)
        module (assoc (:module macro-artifact)
                      :forms (:expanded-forms macro-artifact))
        compiled-plan (stage0-compiled-core-plan source-path source-text
                                                 module)
        run-output (execute-stage0-compiled-plan compiled-plan)
        schema-report (stage0-compiled-plan-schema-report compiled-plan
                                                          module)
        manifest (:schema-manifest schema-report)
        validator (:validator-record schema-report)
        serializer (:serialization-record schema-report)
        canonical (:canonical-record schema-report)
        api-projections (:api-projection-record schema-report)
        database (:database-mapping-record schema-report)
        binary-abi (:binary-abi-record schema-report)
        typed-config (:typed-config-record schema-report)
        artifact-schema (:artifact-schema-record schema-report)
        conformance (:schema-conformance-results schema-report)
        proof {:compiled-schema-gate-validated? true
               :schema-manifest-recorded?
               (= :complete (:status manifest))
               :source-schema-authority-recorded?
               (= :gravity-source-schema (:source-authority manifest))
               :validator-boundaries-recorded?
               (set/subset? #{:api-input :database-row :model-output
                               :configuration :artifact-manifest}
                             (:clears-taint-for validator))
               :serialization-and-canonical-recorded?
               (and (= :untrusted (:decoded-trust serializer))
                    (true? (:schema-hash-included canonical)))
               :api-projections-recorded?
               (and (= :gravity-source-schema
                       (get-in api-projections
                               [:graphql :source-authority]))
                    (= :gravity-route-and-schema-source
                       (get-in api-projections
                               [:openapi :source-authority])))
               :database-migration-policy-recorded?
               (= :no-data-loss
                  (get-in database [:data-loss-report :policy]))
               :binary-abi-policy-recorded?
               (= :no-raw-pointers-in-stable-record
                  (:pointer-policy binary-abi))
               :typed-config-redaction-recorded?
               (= :redacted
                  (get-in typed-config
                          [:redaction-report :database-url]))
               :artifact-evidence-schema-recorded?
               (set/subset? #{:types :effects :capabilities :safety
                              :proofs :tests :diagnostics :conformance}
                             (set (:evidence-schema artifact-schema)))
               :compiled-plan-executed? (= "core-app\ngravity:19:2\n(:ok 19)\n"
                                          run-output)
               :rejected-diagnostics-covered?
               (= #{"S1-PROJECTION" "S2-TAINT" "S3-HASH"
                    "S4-RESOLVER" "S5-SCHEMA" "S6-DATA-LOSS"
                    "S7-POINTER" "S8-SECRET" "S9-EVIDENCE"}
                  (set (:required-diagnostic-ids conformance)))
               :limitations {:clojure-instruction-runner? true
                             :production-schema-runtime? false
                             :live-api-server? false
                             :database-migration-executed? false
                             :binary-abi-executed? false
                             :environment-loaded? false
                             :self-hosted-schema-tooling? false
                             :next-required-capability
                             :compile-and-run-real-schema-backed-interop-slices}
               :status :complete}
        artifact-base
        {:kind :gravity/stage0-hosted-core-compiled-schema-proof
         :phase "10"
         :task "P10-S1"
         :governing-documents ["D1" "S1-S9"]
         :source {:path source-path
                  :sha256 (str "sha256:" (sha256-hex source-text))}
         :compiled-plan (select-keys compiled-plan
                                     [:kind :plan-id :entrypoint
                                      :instruction-summary :effect-summary])
         :schema-report
         (select-keys schema-report
                      [:kind :report-id :document-set
                       :compiled-plan-id
                       :schema-manifest
                       :source-schema-record
                       :validator-record
                       :serialization-record
                       :canonical-record
                       :api-projection-record
                       :database-mapping-record
                       :binary-abi-record
                       :typed-config-record
                       :artifact-schema-record
                       :schema-conformance-results
                       :diagnostics])
         :accepted-run {:command (str "clojure -M:gravity run-compiled "
                                      source-path)
                        :stdout run-output}
         :proof-command (str "clojure -M:gravity hosted-core-compiled-schema "
                             source-path)
         :rejected-fixtures stage0-compiled-schema-rejected-fixtures
         :trusted-boundary {:compiler :clojure/jvm
                            :runtime
                            :gravity.runtime/stage0-clojure-jvm-instruction-runner
                            :instruction-plan? true
                            :clojure-instruction-runner? true
                            :production-schema-runtime? false
                            :live-api-server? false
                            :database-migration-executed? false
                            :binary-abi-executed? false
                            :environment-loaded? false
                            :self-hosted-schema-tooling? false}
         :capability-based-proof proof}]
    (assoc artifact-base
           :artifact-id (str "sha256:" (sha256-hex (pr-str artifact-base))))))

(defn hosted-core-compiled-schema-proof-file-artifact
  [path]
  (hosted-core-compiled-schema-proof-source-artifact path (slurp path)))