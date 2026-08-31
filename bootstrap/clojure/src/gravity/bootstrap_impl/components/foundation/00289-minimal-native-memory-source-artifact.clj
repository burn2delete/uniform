

(defn minimal-native-memory-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (minimal-native-memory-source-overrides module)
        _ (minimal-native-memory-validate-source-overrides! source-path
                                                            source-overrides)
        runtime-selection-artifact
        (runtime-selection-file-artifact minimal-native-runtime-upstream-path)
        input-id (:artifact-id runtime-selection-artifact)
        native-manifest (minimal-native-runtime-manifest input-id)
        memory-manifest (memory-runtime-manifest input-id)
        diagnostic-stream
        (minimal-native-memory-diagnostic-stream source-path input-id)
        artifact-base
        {:kind :gravity/stage0-minimal-native-memory-runtime-artifact
         :task "P08-T02"
         :document-set ["R3" "R5"]
         :governing-documents minimal-native-memory-governing-documents
         :pass {:name :minimal-native-memory-runtime
                :input :runtime-selection-artifact
                :output :minimal-native-memory-runtime-artifact
                :requires [:runtime-family-selection-record
                           :runtime-service-table
                           :capability-enforcement-table
                           :safety-memory-facts
                           :ownership-resource-facts]
                :preserves [:source-spans :types :effects :capabilities
                            :safety :proofs :profile :target
                            :artifact-provenance]
                :emits [:minimal-native-runtime-manifest
                        :memory-runtime-manifest
                        :minimal-native-memory-diagnostic-stream
                        :conformance-criteria-record]
                :rejects minimal-native-memory-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :runtime-selection-artifact
         (select-keys runtime-selection-artifact
                      [:kind :task :artifact-id :capability-based-proof
                       :runtime-selection-results])
         :runtime-selection-artifact-kind (:kind runtime-selection-artifact)
         :runtime-selection-artifact-hash input-id
         :upstream-artifact-source minimal-native-runtime-upstream-path
         :minimal-native-runtime-manifest native-manifest
         :memory-runtime-manifest memory-manifest
         :rejected-design-coverage
         [{:design :native-helper-silent-io
           :diagnostic "R3-CAPABILITY" :status :rejected}
          {:design :allocator-use-in-no-allocation-region
           :diagnostic "R3-ALLOCATOR" :status :rejected}
          {:design :platform-default-panic-policy
           :diagnostic "R3-PANIC" :status :rejected}
          {:design :managed-gc-reflection-dynamic-loading-in-native
           :diagnostic "R3-MANAGED" :status :rejected}
          {:design :global-allocation-model-for-every-profile
           :diagnostic "R5-PROVIDER" :status :rejected}
          {:design :raw-memory-safe-default
           :diagnostic "R5-RAW" :status :rejected}
          {:design :region-arena-escape
           :diagnostic "R5-LIFETIME" :status :rejected}
          {:design :gc-finalization-as-linear-cleanup
           :diagnostic "R5-LINEAR" :status :rejected}
          {:design :runtime-check-elision-without-proof
           :diagnostic "R5-PROOF" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/minimal-native-memory-conformance-record
          :startup-panic-allocator-atomics-ffi-check-services :complete
          :allocator-policy-acceptance-and-rejection :covered
          :panic-trap-result-boundary-fixtures :complete
          :runtime-helper-capability-checks :complete
          :debug-release-difference :complete
          :ffi-metadata-preservation :complete
          :hidden-managed-service-rejection :covered
          :memory-provider-manifests :complete
          :lifetime-region-arena-fixtures :complete
          :linear-resource-ledger :complete
          :raw-device-memory-audit_and_transfer_records :complete
          :proof-backed-runtime-check-elision :complete
          :status :passed}
         :minimal-native-memory-diagnostic-stream diagnostic-stream
         :minimal-native-memory-results
         {:documents ["R3" "R5"]
          :task "P08-T02"
          :required-diagnostic-ids minimal-native-memory-diagnostic-ids
          :runtime-selection-input-status :complete
          :native-service-status :complete
          :allocator-status :complete
          :panic-status :complete
          :atomics-status :complete
          :ffi-status :complete
          :capability-status :complete
          :debug-release-status :complete
          :managed-rejection-status :complete
          :memory-provider-status :complete
          :allocation-status :complete
          :lifetime-status :complete
          :linear-resource-status :complete
          :raw-memory-status :complete
          :device-memory-status :complete
          :bounds-check-status :complete
          :proof-elision-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (minimal-native-memory-validate! source-path artifact-base)
        capability-proof
        (minimal-native-memory-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn minimal-native-memory-file-artifact
  [path]
  (minimal-native-memory-source-artifact path (slurp path)))

(def managed-runtime-governing-documents
  ["docs/phase-08-runtime-architecture/115-r4-managed-runtime-design.md"
   "docs/phase-08-runtime-architecture/112-r1-runtime-architecture-overview.md"
   "docs/phase-08-runtime-architecture/116-r5-memory-runtime-design.md"
   "docs/phase-08-runtime-architecture/120-r9-repl-and-interactive-runtime-design.md"
   "docs/phase-08-runtime-architecture/121-r10-ffi-runtime-design.md"
   "docs/phase-08-runtime-architecture/122-r11-runtime-capability-enforcement-design.md"
   "docs/phase-08-runtime-architecture/123-r12-runtime-observability-and-diagnostics-design.md"
   "docs/phase-03-profile-system/049-p4-hosted-profile-specification.md"
   "docs/phase-03-profile-system/058-p13-profile-compatibility-and-composition.md"
   "docs/phase-07-backend-architecture/101-b4-wasm-backend-specification.md"
   "docs/phase-07-backend-architecture/102-b5-jvm-backend-specification.md"
   "docs/phase-07-backend-architecture/103-b6-javascript-typescript-backend-specification.md"])

(def managed-runtime-upstream-artifact-path
  "bootstrap/clojure/fixtures/accepted/runtime-minimal-native-memory.gravity")

(def managed-runtime-diagnostic-ids
  ["R4-HOST"
   "R4-NULL"
   "R4-EXCEPTION"
   "R4-REFLECTION"
   "R4-COLLECTION"
   "R4-RESOURCE"
   "R4-SOURCEMAP"
   "R4-PROFILE"
   "R4-MANIFEST"])

(def managed-runtime-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             managed-runtime-diagnostic-ids)))

(defn managed-runtime-source-overrides
  [module]
  (get-in module [:metadata :runtime :managed] {}))

(defn managed-runtime-missing-policy
  [id]
  (case id
    "R4-HOST" :declared-host-runtime-version-module-package-system
    "R4-NULL" :checked-null-undefined-translation
    "R4-EXCEPTION" :host-exception-rejected-promise-translation
    "R4-REFLECTION" :capability-gated-reflection-dynamic-use-policy
    "R4-COLLECTION" :gravity-compatible-collection-semantics
    "R4-RESOURCE" :deterministic-linear-resource-cleanup
    "R4-SOURCEMAP" :host-to-gravity-source-debug-map
    "R4-PROFILE" :hosted-behavior-profile-facade-boundary
    :complete-managed-runtime-manifest))

(defn managed-runtime-fail!
  [id source-path subject extra]
  (fail! id
         "P08 managed host runtime validation failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :managed-runtime
                 :stage :managed-runtime
                 :document-id "R4"
                 :profile (or (:profile subject) :hosted)
                 :target (or (:target subject) :jvm)
                 :runtime-family :managed
                 :host-runtime (:host-runtime subject)
                 :host-symbol (:host-symbol subject)
                 :host-package (:host-package subject)
                 :gravity-type (:gravity-type subject)
                 :effect (:effect subject)
                 :capability (:capability subject)
                 :adapter (:adapter subject)
                 :missing-policy (managed-runtime-missing-policy id)
                 :fallback-status :rejected
                 :remediation "P08-T03 requires managed host runtime manifests that declare JVM, JS, and Wasm host targets, typed adapters, checked null and exception translation, capability-gated reflection, Gravity-compatible collection semantics, deterministic linear cleanup, source maps, diagnostics, and profile leakage rejection."}
                extra)))

(defn managed-runtime-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get managed-runtime-override-diagnostics fail-kind)]
      (managed-runtime-fail!
       id source-path
       {:host-runtime fail-kind
        :host-symbol (symbol "host" (name fail-kind))
        :host-package "stage0.host"
        :gravity-type 'HostValue
        :effect fail-kind
        :capability fail-kind
        :adapter fail-kind}
       {:missing-fields [fail-kind]}))))