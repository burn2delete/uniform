

(defn b1-document-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (b1-document-source-overrides module)
        _ (b1-document-validate-source-overrides! source-path
                                                  source-overrides)
        interface-artifact (backend-interface-source-artifact source-path
                                                              source-text)
        input-id (:artifact-id interface-artifact)
        diagnostic-stream (b1-document-diagnostic-stream source-path input-id)
        artifact-base
        {:kind :gravity/stage0-b1-backend-interface-document-artifact
         :task "P07-D098"
         :document-set ["B1"]
         :governing-document b1-document-governing-document
         :pass {:name :b1-backend-interface-document-coverage
                :input :backend-interface-and-conformance-artifact
                :output :b1-document-coverage-artifact
                :requires [:backend-interface-artifact
                           :backend-manifest :backend-input-packet
                           :eligibility-report :target-artifact-manifest
                           :abi-layout-record
                           :runtime-provider-dependency-record
                           :proof-to-target-metadata-map
                           :source-debug-map
                           :unsupported-feature-report
                           :backend-conformance-record]
                :preserves [:profile :target :effects :capabilities
                            :safety :proofs :source-spans
                            :generated-origins :artifact-provenance]
                :emits [:requirements-coverage
                        :rejected-design-coverage
                        :conformance-criteria-record
                        :b1-diagnostic-stream]
                :rejects b1-document-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :backend-interface-artifact
         (select-keys interface-artifact
                      [:kind :task :artifact-id :capability-based-proof
                       :backend-interface-results])
         :requirements-coverage
         {:artifact :gravity/b1-requirements-coverage
          :manifest-status (get-in interface-artifact
                                   [:backend-manifest :status])
          :verified-input-status
          (get-in interface-artifact
                  [:backend-input-eligibility-report :status])
          :input-packet-fields (set (keys (:backend-input-packet
                                           interface-artifact)))
          :eligibility-status :complete
          :eligibility-check-count (count (:eligibility-checks
                                           interface-artifact))
          :artifact-status
          (if (seq (:target-artifact-manifest interface-artifact))
            :complete
            :missing)
          :abi-layout-status (get-in interface-artifact
                                     [:abi-layout-record :status])
          :runtime-provider-status
          (get-in interface-artifact
                  [:runtime-provider-dependency-record :status])
          :proof-target-map-status
          (get-in interface-artifact
                  [:proof-to-target-metadata-map :status])
          :source-debug-map-status (get-in interface-artifact
                                           [:source-debug-map :status])
          :capability-status
          (get-in interface-artifact
                  [:capability-preservation-report :status])
          :unsupported-feature-status
          (get-in interface-artifact
                  [:unsupported-feature-report :status])
          :metadata-status
          (get-in interface-artifact
                  [:metadata-preservation-report :status])
          :diagnostic-status :complete}
         :rejected-design-coverage
         [{:design :unchecked-ir :diagnostic "B1-INPUT" :status :rejected}
          {:design :target-undefined-behavior-shortcut
           :diagnostic "B1-PROOF" :status :rejected}
          {:design :metadata-without-compiler-evidence
           :diagnostic "B1-METADATA" :status :rejected}
          {:design :artifact-without-provenance-or-safety
           :diagnostic "B1-METADATA" :status :rejected}
          {:design :backend-support-only-in-prose
           :diagnostic "B1-UNSUPPORTED" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/b1-conformance-criteria-record
          :criteria [:manifest-validation
                     :verified-input-acceptance
                     :unverified-input-rejection
                     :profile-target-eligibility
                     :proof-backed-target-metadata
                     :unsupported-operation-diagnostics
                     :artifact-manifest-emission
                     :metadata-preservation-through-emission
                     :positive-negative-backend-conformance]
          :status :passed}
         :b1-diagnostic-stream diagnostic-stream
         :b1-document-results
         {:documents ["B1"]
          :task "P07-D098"
          :required-diagnostic-ids b1-document-diagnostic-ids
          :backend-interface-input-status :complete
          :manifest-status :complete
          :input-contract-status :complete
          :eligibility-status :complete
          :artifact-status :complete
          :metadata-status :complete
          :conformance-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (b1-document-validate! source-path artifact-base)
        capability-proof (b1-document-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn b1-document-file-artifact
  [path]
  (b1-document-source-artifact path (slurp path)))

(def b2-document-governing-document
  "docs/phase-07-backend-architecture/099-b2-c-backend-design.md")

(def b2-document-diagnostic-ids
  ["B2-DIALECT"
   "B2-UB"
   "B2-ABI"
   "B2-POINTER"
   "B2-NUMERIC"
   "B2-RUNTIME"
   "B2-FFI"
   "B2-MMIO"
   "B2-MANIFEST"])

(def b2-document-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             b2-document-diagnostic-ids)))

(defn b2-document-source-overrides
  [module]
  (or (get-in module [:metadata :backend :b2-document])
      (get-in module [:metadata :backend :native-lowering])
      (get-in module [:metadata :backend :native])
      {}))

(defn b2-document-missing-fact
  [id]
  (case id
    "B2-DIALECT" :declared-c-dialect
    "B2-UB" :c-undefined-behavior-disposition
    "B2-ABI" :pinned-abi-layout
    "B2-POINTER" :pointer-provenance-alignment-lifetime
    "B2-NUMERIC" :numeric-mode-and-proof
    "B2-RUNTIME" :profile-legal-runtime-helper
    "B2-FFI" :foreign-boundary-map
    "B2-MMIO" :volatile-mmio-facts
    "B2-MANIFEST" :complete-c-artifact-manifest
    :b2-document-evidence))

(defn b2-document-target-construct
  [id]
  (case id
    "B2-DIALECT" :c-dialect-selection
    "B2-UB" :signed-overflow-lowering
    "B2-ABI" :struct-layout-record
    "B2-POINTER" :raw-pointer-cast
    "B2-NUMERIC" :integer-addition
    "B2-RUNTIME" :libc-helper
    "B2-FFI" :foreign-call-adapter
    "B2-MMIO" :volatile-mmio-access
    "B2-MANIFEST" :c-artifact-manifest
    :c-backend))

(defn b2-document-helper
  [id]
  (case id
    "B2-RUNTIME" {:selected :none :rejected :hidden-libc}
    "B2-FFI" {:selected :ffi-adapter :rejected :unchecked-foreign-call}
    "B2-MMIO" {:selected :mmio-volatile-helper
               :rejected :plain-pointer-dereference}
    "B2-NUMERIC" {:selected :gravity_checked_add_i64
                  :rejected :unchecked-signed-add}
    {:selected :stage0-c-backend-record :rejected nil}))

(defn b2-document-fail!
  [id source-path subject extra]
  (fail! id
         "B2 C backend design document coverage validation failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :b2-c-backend-document
                 :stage (or (:stage subject) :b2-c-backend-document-coverage)
                 :backend :gravity.backend/c
                 :profile (or (:profile subject) :native)
                 :target (or (:target subject) :x86_64-stage0)
                 :c-dialect (or (:c-dialect subject) :freestanding-c11)
                 :artifact-id (:artifact-id subject)
                 :mir-op (or (:mir-op subject) :checked-add)
                 :domain-anchor (:domain-anchor subject)
                 :generated-origin-chain
                 (or (:generated-origin-chain subject)
                     [:mir :c14-target-lowering :b1-interface
                      :b2-c-backend])
                 :missing-fact (or (:missing-fact subject)
                                   (b2-document-missing-fact id))
                 :helper (or (:helper subject) (b2-document-helper id))
                 :target-construct (or (:target-construct subject)
                                       (b2-document-target-construct id))
                 :fallback-status (or (:fallback-status subject) :rejected)
                 :remediation "Emit C only from verified backend input with a declared C dialect, no implicit C undefined behavior, pinned ABI/layout and pointer facts, profile-legal runtime helpers, complete FFI/MMIO/numeric evidence, source maps, and a complete C artifact manifest."}
                extra)))

(defn b2-document-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get b2-document-override-diagnostics fail-kind)]
      (b2-document-fail!
       id source-path
       {:stage :b2-c-backend-document-coverage
        :artifact-id (str "b2-document-" (name fail-kind))
        :missing-fact fail-kind
        :target-construct fail-kind}
       {:missing-fields [fail-kind]}))))