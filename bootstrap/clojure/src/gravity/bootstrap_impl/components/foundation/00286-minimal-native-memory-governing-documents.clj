

(def minimal-native-memory-governing-documents
  ["docs/phase-08-runtime-architecture/114-r3-minimal-native-runtime-design.md"
   "docs/phase-08-runtime-architecture/116-r5-memory-runtime-design.md"
   "docs/phase-01-core-language/020-l10-memory-model-specification.md"
   "docs/phase-02-safety/031-safe2-memory-safety-model.md"
   "docs/phase-02-safety/034-safe5-linear-resource-safety.md"
   "docs/phase-08-runtime-architecture/112-r1-runtime-architecture-overview.md"])

(def minimal-native-runtime-upstream-path
  "bootstrap/clojure/fixtures/accepted/runtime-selection-no-runtime.gravity")

(def minimal-native-memory-diagnostic-ids
  ["R3-SERVICE"
   "R3-ALLOCATOR"
   "R3-PANIC"
   "R3-ATOMICS"
   "R3-FFI"
   "R3-CAPABILITY"
   "R3-DEBUG"
   "R3-MANAGED"
   "R3-MANIFEST"
   "R5-PROVIDER"
   "R5-ALLOC"
   "R5-LIFETIME"
   "R5-LINEAR"
   "R5-RAW"
   "R5-DEVICE"
   "R5-BOUNDS"
   "R5-PROOF"
   "R5-DEBUG"
   "R5-MANIFEST"])

(def minimal-native-memory-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             minimal-native-memory-diagnostic-ids)))

(defn minimal-native-memory-source-overrides
  [module]
  (get-in module [:metadata :runtime :minimal-native] {}))

(defn minimal-native-memory-missing-policy
  [id]
  (case id
    "R3-SERVICE" :declared-linked-native-service
    "R3-ALLOCATOR" :allocator-provider-matches-memory-policy
    "R3-PANIC" :explicit-panic-trap-abort-unwind-policy
    "R3-ATOMICS" :target-memory-order-and-synchronization-provider
    "R3-FFI" :ffi-boundary-metadata-preservation
    "R3-CAPABILITY" :runtime-helper-effect-authority
    "R3-DEBUG" :debug-release-behavior-and-source-maps
    "R3-MANAGED" :hidden-managed-service-rejection
    "R3-MANIFEST" :complete-minimal-native-runtime-manifest
    "R5-PROVIDER" :selected-memory-provider-manifest
    "R5-ALLOC" :allocation-profile-and-region-policy
    "R5-LIFETIME" :region-arena-lifetime-and-escape-record
    "R5-LINEAR" :linear-resource-ledger
    "R5-RAW" :raw-memory-unsafe-wrapper-audit
    "R5-DEVICE" :device-memory-transfer-synchronization-lifetime-record
    "R5-BOUNDS" :bounds-initialization-runtime-check-map
    "R5-PROOF" :runtime-check-elision-proof-agreement
    "R5-DEBUG" :debug-allocation-trace-source-provenance
    :complete-memory-runtime-manifest))

(defn minimal-native-memory-fail!
  [id source-path subject extra]
  (fail! id
         "P08 minimal native and memory runtime validation failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :minimal-native-memory-runtime
                 :stage :minimal-native-memory-runtime
                 :profile (or (:profile subject) :native)
                 :target (or (:target subject)
                             {:backend :llvm :platform :linux})
                 :runtime-family (or (:runtime-family subject)
                                     (if (str/starts-with? id "R5")
                                       :memory
                                       :minimal-native))
                 :service-id (:service-id subject)
                 :provider (:provider subject)
                 :effect (:effect subject)
                 :capability (:capability subject)
                 :allocation-id (:allocation-id subject)
                 :resource-id (:resource-id subject)
                 :proof-id (:proof-id subject)
                 :artifact-id (:artifact-id subject)
                 :missing-policy (minimal-native-memory-missing-policy id)
                 :fallback-status :rejected
                 :remediation "P08-T02 requires declared minimal-native services, allocator, panic, atomics, FFI, runtime checks, debug/release records, capability policy, and memory provider, lifetime, linear resource, raw/device memory, bounds, debug, and proof-elision manifests."}
                extra)))

(defn minimal-native-memory-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get minimal-native-memory-override-diagnostics fail-kind)]
      (minimal-native-memory-fail!
       id source-path
       {:service-id fail-kind
        :provider fail-kind
        :effect fail-kind
        :capability fail-kind
        :allocation-id (str "alloc-" (name fail-kind))
        :resource-id (str "resource-" (name fail-kind))
        :proof-id (str "proof-" (name fail-kind))
        :artifact-id (str "minimal-native-memory-" (name fail-kind))}
       {:missing-fields [fail-kind]}))))

(defn minimal-native-memory-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/minimal-native-memory-diagnostic-stream
   :stage :minimal-native-memory-runtime
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :minimal-native-memory-runtime
            :message-key (keyword "runtime" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "p08-minimal-native-syntax-" index)
                      :artifact input-id}
            :profile :native
            :target {:backend :llvm :platform :linux}
            :runtime-family (if (str/starts-with? id "R5")
                              :memory
                              :minimal-native)
            :service-id (case id
                          "R3-SERVICE" :startup
                          "R3-PANIC" :panic
                          "R3-ATOMICS" :atomics
                          "R3-FFI" :ffi-helper
                          "R3-DEBUG" :debug-stack
                          "R3-MANAGED" :gc
                          "R5-PROVIDER" :region-arena
                          "R5-ALLOC" :allocation
                          "R5-LINEAR" :linear-resource
                          "R5-DEVICE" :device-memory
                          :runtime-service)
            :provider (case id
                        "R3-ALLOCATOR" :allocator/region-arena
                        "R5-PROVIDER" :memory/region-arena
                        "R5-DEVICE" :memory/device
                        :stage0-runtime-provider)
            :effect (case id
                      "R3-CAPABILITY" :filesystem/read
                      "R5-ALLOC" :memory/allocate
                      "R5-RAW" :memory/raw
                      "R5-DEVICE" :memory/device-transfer
                      nil)
            :capability (case id
                          "R3-CAPABILITY" :fs/read
                          "R5-RAW" :memory/raw
                          "R5-DEVICE" :gpu/device-memory
                          nil)
            :allocation-id (str "p08-" (str/lower-case id) "-allocation")
            :resource-id (str "p08-" (str/lower-case id) "-resource")
            :proof-id (str "p08-" (str/lower-case id) "-proof")
            :missing-policy (minimal-native-memory-missing-policy id)
            :source-generated-origin-chain
            [:profile-validation :safety-analysis :runtime-selection
             :minimal-native-memory-runtime]
            :facts {:runtime-helpers-effect-checked true
                    :memory-runtime-does-not-weaken-compiler-checks true
                    :debug-release-distinction-required true
                    :proof-elision-must-match-safe15 true}
            :remediation [{:kind :declare-runtime-service}
                          {:kind :attach-provider-manifest}
                          {:kind :preserve-memory-and-resource-facts}
                          {:kind :reject-or-prove-runtime-check-elision}]
            :redactions []
            :ordering-key [id :minimal-native-memory-runtime]})
         minimal-native-memory-diagnostic-ids
         (range))
   :status :complete})