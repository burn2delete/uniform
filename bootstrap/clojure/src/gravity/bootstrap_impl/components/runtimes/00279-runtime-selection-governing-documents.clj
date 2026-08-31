

(def runtime-selection-governing-documents
  ["docs/phase-08-runtime-architecture/112-r1-runtime-architecture-overview.md"
   "docs/phase-08-runtime-architecture/113-r2-no-runtime-execution-model.md"
   "docs/phase-03-profile-system/046-p1-profile-system-specification.md"
   "docs/phase-07-backend-architecture/110-b13-artifact-emission-specification.md"
   "docs/phase-12-build-package-and-artifact-system/170-pkg6-capability-and-permission-manifest-specification.md"
   "docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md"])

(def runtime-selection-upstream-artifact-path
  "bootstrap/clojure/fixtures/accepted/backend-artifact-emission.gravity")

(def runtime-selection-diagnostic-ids
  ["R1-SELECTION"
   "R1-SERVICE"
   "R1-FORBIDDEN"
   "R1-CAPABILITY"
   "R1-HOST"
   "R1-REPLAY"
   "R1-STARTUP"
   "R1-FAILURE"
   "R1-MANIFEST"
   "R2-HIDDEN-SERVICE"
   "R2-STARTUP"
   "R2-MEMORY"
   "R2-DISPATCH"
   "R2-FAILURE"
   "R2-CAPABILITY"
   "R2-PROOF"
   "R2-MANIFEST"])

(def runtime-selection-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             runtime-selection-diagnostic-ids)))

(defn runtime-selection-source-overrides
  [module]
  (get-in module [:metadata :runtime :selection] {}))

(defn runtime-selection-missing-policy
  [id]
  (case id
    "R1-SELECTION" :profile-target-runtime-family-selection
    "R1-SERVICE" :linked-generated-delegated-external-forbidden-service-table
    "R1-FORBIDDEN" :hidden-runtime-dependency-rejection
    "R1-CAPABILITY" :runtime-capability-enforcement-table
    "R1-HOST" :typed-host-service-adapter
    "R1-REPLAY" :nondeterminism-replay-or-audit-record
    "R1-STARTUP" :startup-initialization-cleanup-record
    "R1-FAILURE" :failure-diagnostic-artifact-mapping
    "R1-MANIFEST" :complete-runtime-manifest
    "R2-HIDDEN-SERVICE" :no-runtime-forbidden-service-report
    "R2-STARTUP" :reset-entry-section-initialization-record
    "R2-MEMORY" :memory-map-stack-static-allocation-report
    "R2-DISPATCH" :dynamic-dispatch-lowered-or-rejected
    "R2-FAILURE" :explicit-panic-trap-result-reset-signal-policy
    "R2-CAPABILITY" :target-authority-capability-record
    "R2-PROOF" :boundedness-initialization-check-elision-proof
    :complete-no-runtime-manifest))

(defn runtime-selection-fail!
  [id source-path subject extra]
  (fail! id
         "P08 runtime selection and no-runtime proof validation failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :runtime-selection
                 :stage :runtime-selection
                 :profile (or (:profile subject) :firmware)
                 :target (or (:target subject)
                             {:backend :c :platform :bare-metal})
                 :runtime-family (or (:runtime-family subject)
                                     :no-runtime)
                 :service-id (:service-id subject)
                 :effect (:effect subject)
                 :capability (:capability subject)
                 :provider (:provider subject)
                 :artifact-id (:artifact-id subject)
                 :missing-policy (runtime-selection-missing-policy id)
                 :fallback-status :rejected
                 :remediation "P08-T01 requires explicit runtime family selection from profile and target, complete service classification, a no-runtime manifest with startup, memory, failure, proof, and forbidden-service records, capability policy evidence, and backend/package/conformance consumption records."}
                extra)))

(defn runtime-selection-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get runtime-selection-override-diagnostics fail-kind)]
      (runtime-selection-fail!
       id source-path
       {:runtime-family (if (str/starts-with? id "R2")
                          :no-runtime
                          :runtime-architecture)
        :service-id fail-kind
        :effect fail-kind
        :capability fail-kind
        :artifact-id (str "runtime-selection-" (name fail-kind))}
       {:missing-fields [fail-kind]}))))

(defn runtime-selection-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/runtime-selection-diagnostic-stream
   :stage :runtime-selection
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :runtime-selection
            :message-key (keyword "runtime" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "p08-runtime-selection-syntax-" index)
                      :artifact input-id}
            :profile (if (str/starts-with? id "R2") :firmware :multi-profile)
            :target (if (str/starts-with? id "R2")
                      {:backend :c :platform :bare-metal}
                      :multi-target-stage0)
            :runtime-family (if (str/starts-with? id "R2")
                              :no-runtime
                              :runtime-architecture)
            :service-id (case id
                          "R1-HOST" :host-service
                          "R1-REPLAY" :nondeterminism
                          "R2-HIDDEN-SERVICE" :gc
                          "R2-DISPATCH" :dynamic-dispatch
                          "R2-MEMORY" :heap
                          "R2-CAPABILITY" :mmio
                          :runtime-service)
            :effect (case id
                      "R1-CAPABILITY" :filesystem/read
                      "R2-CAPABILITY" :memory/mmio
                      nil)
            :capability (case id
                          "R1-CAPABILITY" :fs/read
                          "R2-CAPABILITY" :hardware/mmio
                          nil)
            :provider (case id
                        "R1-HOST" :untyped-host
                        "R2-CAPABILITY" :target-provider/mmio
                        nil)
            :missing-policy (runtime-selection-missing-policy id)
            :source-generated-origin-chain
            [:profile-validation :capability-validation :target-lowering
             :artifact-emission :runtime-selection]
            :facts {:runtime-selection-explicit true
                    :hidden-runtime-fallbacks-rejected true
                    :capability-checks-do-not-grant-authority true
                    :no-runtime-heap-gc-scheduler-reflection-forbidden true}
            :remediation [{:kind :derive-runtime-family-from-profile-target}
                          {:kind :record-service-classification}
                          {:kind :attach-no-runtime-proof}
                          {:kind :reject-hidden-runtime-service}]
            :redactions []
            :ordering-key [id :runtime-selection]})
         runtime-selection-diagnostic-ids
         (range))
   :status :complete})

(defn runtime-family-selection-record
  [module input-id]
  {:artifact :gravity/runtime-family-selection-record
   :input-artifact input-id
   :source-module (:module module)
   :active-profile (:profile module)
   :active-target (:target module)
   :selection-basis [:profile-manifest :target-lowering-manifest
                     :package-policy :effect-capability-summary
                     :artifact-emission-record]
   :families
   [{:profile :core
     :target {:backend :none :platform :semantic-core}
     :family :no-runtime
     :reason :pure-core-no-services}
    {:profile :firmware
     :target {:backend :c :platform :bare-metal}
     :family :no-runtime
     :reason :constrained-reset-entry}
    {:profile :kernel
     :target {:backend :llvm :platform :kernel-object}
     :family :no-runtime
     :reason :kernel-no-hidden-host}
    {:profile :hardware
     :target {:backend :hdl :platform :fpga}
     :family :no-runtime
     :reason :hardware-description}
    {:profile :gpu
     :target {:backend :gpu :platform :device-kernel}
     :family :no-runtime
     :reason :device-kernel-no-host-runtime}
    {:profile :native
     :target {:backend :llvm :platform :linux}
     :family :minimal-native
     :reason :native-startup-and-panic}
    {:profile :hosted
     :target {:backend :jvm :platform :host}
     :family :managed
     :reason :typed-host-delegation}
    {:profile :distributed
     :target {:backend :workflow-graph :platform :durable}
     :family :distributed
     :reason :event-log-replay}
    {:profile :ai
     :target {:backend :workflow-graph :platform :agent}
     :family :ai
     :reason :model-tool-memory-policy}
    {:profile :meta
     :target {:backend :jvm :platform :tooling}
     :family :repl
     :reason :interactive-compile-time-tooling}
    {:profile :formal
     :target {:backend :proof :platform :offline}
     :family :no-runtime
     :reason :proof-artifact-only}]
   :status :complete})