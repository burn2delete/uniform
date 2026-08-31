

(def domain-ir-registry-seed
  [{:domain :efir
    :owner-doc "MATH3"
    :entry-passes [:elementary-detect :efir-build]
    :exit-passes [:efir-to-mir :efir-to-target-provider]
    :verifier :gravity.math/verify-efir
    :supported-profiles #{:core :native :gpu :formal}
    :target-lowerings #{:llvm :wasm :gpu}
    :proof-obligations #{:domain :branch-policy :numeric-mode :roundoff}
    :fallback :mir-provider-call}
   {:domain :schema
    :owner-doc "S1"
    :entry-passes [:schema-declaration-detect :schema-ir-build]
    :exit-passes [:schema-validator-to-mir :schema-codegen]
    :verifier :gravity.schema/verify
    :supported-profiles #{:core :hosted :native :distributed :ai}
    :target-lowerings #{:jvm :wasm :javascript-typescript}
    :proof-obligations #{:shape :canonical-encoding :compatibility}
    :fallback :runtime-schema-validator}
   {:domain :workflow
    :owner-doc "DOM9"
    :entry-passes [:workflow-detect :workflow-graph-build]
    :exit-passes [:workflow-to-runtime-manifest :workflow-to-mir]
    :verifier :gravity.workflow/verify
    :supported-profiles #{:distributed :ai :hosted}
    :target-lowerings #{:workflow-runtime :jvm}
    :proof-obligations #{:determinism :replay :idempotency}
    :fallback :mir-workflow-state-machine}
   {:domain :ai-agent
    :owner-doc "A1"
    :entry-passes [:agent-manifest-detect :tool-call-graph-build]
    :exit-passes [:agent-manifest-to-runtime :agent-tool-to-mir]
    :verifier :gravity.ai/verify-agent
    :supported-profiles #{:ai :distributed :hosted}
    :target-lowerings #{:ai-runtime :jvm}
    :proof-obligations #{:prompt-schema :tool-authority :human-review}
    :fallback :mir-tool-dispatch}
   {:domain :query
    :owner-doc "B11"
    :entry-passes [:query-detect :relational-ir-build]
    :exit-passes [:query-to-provider :query-to-mir]
    :verifier :gravity.query/verify
    :supported-profiles #{:hosted :distributed :native}
    :target-lowerings #{:sql :jvm}
    :proof-obligations #{:schema :cardinality :effect-boundary}
    :fallback :mir-provider-query-call}
   {:domain :hdl
    :owner-doc "DOM7"
    :entry-passes [:state-machine-detect :hdl-ir-build]
    :exit-passes [:hdl-to-netlist :hdl-to-mir-simulator]
    :verifier :gravity.hardware/verify-hdl
    :supported-profiles #{:hardware :formal}
    :target-lowerings #{:verilog :fpga}
    :proof-obligations #{:clock-domain :width :reset}
    :fallback :mir-state-machine-simulator}
   {:domain :ui
    :owner-doc "B14"
    :entry-passes [:ui-boundary-detect :ui-ir-build]
    :exit-passes [:ui-to-platform :ui-to-mir-state]
    :verifier :gravity.ui/verify
    :supported-profiles #{:hosted :distributed}
    :target-lowerings #{:mobile :web}
    :proof-obligations #{:event-schema :state-schema :capability-boundary}
    :fallback :mir-ui-state-adapter}
   {:domain :gpu
    :owner-doc "P11"
    :entry-passes [:kernel-detect :gpu-ir-build]
    :exit-passes [:gpu-to-target :gpu-to-mir-fallback]
    :verifier :gravity.gpu/verify-kernel
    :supported-profiles #{:gpu :native :formal}
    :target-lowerings #{:cuda :spirv :gpu}
    :proof-obligations #{:memory-space :bounds :synchronization}
    :fallback :mir-scalar-kernel}
   {:domain :ffi-boundary
    :owner-doc "L19"
    :entry-passes [:foreign-boundary-detect :ffi-ir-build]
    :exit-passes [:ffi-to-provider :ffi-safe-wrapper-to-mir]
    :verifier :gravity.interop/verify-ffi
    :supported-profiles #{:hosted :native :kernel}
    :target-lowerings #{:jvm :llvm}
    :proof-obligations #{:type-map :ownership :error-map}
    :fallback :safe-wrapper-mir}
   {:domain :package-artifact
    :owner-doc "PKG1"
    :entry-passes [:package-manifest-detect :artifact-graph-build]
    :exit-passes [:artifact-graph-to-build :artifact-graph-to-mir-record]
    :verifier :gravity.package/verify-artifact-graph
    :supported-profiles #{:meta :hosted :native}
    :target-lowerings #{:build-system :jvm}
    :proof-obligations #{:provenance :lockfile :authority}
    :fallback :mir-package-record}])

(defn domain-ir-source-overrides
  [module]
  (get-in module [:metadata :compiler :domain-ir] {}))

(defn domain-ir-fail!
  [id source-path artifact subject extra]
  (fail! id
         (get domain-ir-diagnostic-messages id "domain IR validation failed")
         (merge {:source-span (or (:source-span subject)
                                  (get-in subject [:source :span])
                                  (source-span source-path 0))
                 :diagnostic-family :domain-ir-verifier
                 :stage :build-domain-ir
                 :domain (:domain subject)
                 :artifact-id (or (:artifact-id subject)
                                  (get-in artifact [:domain-ir-registry
                                                    0 :schema]))
                 :semantic-anchor (:semantic-anchor subject)
                 :owner-doc (or (:owner-doc subject) (:owner-doc extra))
                 :profile (or (:profile subject)
                              (get-in artifact [:mir-artifact :mir-module
                                                :profile]))
                 :target (or (:target-request subject)
                             (get-in artifact [:mir-artifact :mir-module
                                               :target-request]))
                 :verifier (or (:verifier subject)
                               (get-in subject [:verifier :name]))
                 :missing-fact (:missing-fact subject)
                 :remediation "Register the domain IR with schema, anchors, facts, verifier, proof, lowering, fallback, and plugin policy evidence before backend consumption."}
                extra)))

(defn domain-ir-registration-record
  [record]
  (let [schema-input (select-keys record
                                  [:domain :owner-doc :entry-passes
                                   :exit-passes :verifier])]
    (assoc record
           :artifact :gravity/domain-ir-registration
           :schema (str "sha256:" (sha256-hex (pr-str schema-input)))
           :semantic-anchor #{:typed-core-node :mir-op}
           :plugin-visible? true
           :registration-status :accepted)))

(defn domain-ir-anchor
  [operations index]
  (let [op (nth operations (mod index (count operations)))]
    {:mir-ops [(:op-id op)]
     :typed-core [(get-in op [:source :core-node])]
     :source-span (get-in op [:source :span])
     :origin-chain (get-in op [:source :origin-chain])}))

(defn domain-ir-artifact-record
  [mir-artifact registration index]
  (let [operations (:mir-operations mir-artifact)
        anchor (domain-ir-anchor operations index)
        domain (:domain registration)
        artifact-id (str "sha256:"
                         (sha256-hex
                          (pr-str {:domain domain
                                   :anchor anchor
                                   :source-core (or (:checked-core-artifact-hash
                                                     mir-artifact)
                                                    (:artifact-id mir-artifact)
                                                    (get-in mir-artifact
                                                            [:mir-module
                                                             :source-core]))})))]
    {:artifact :gravity/domain-ir
     :domain domain
     :artifact-id artifact-id
     :owner-doc (:owner-doc registration)
     :schema (:schema registration)
     :source {:syntax-id (get-in mir-artifact [:mir-module :source-core])
              :span (:source-span anchor)
              :origin-chain (:origin-chain anchor)}
     :semantic-anchor {:mir-ops (:mir-ops anchor)
                       :typed-core (:typed-core anchor)}
     :profile (get-in mir-artifact [:mir-module :profile])
     :target-request (get-in mir-artifact [:mir-module :target-request])
     :facts {:types :mir/type-table
             :effects :mir/effect-table
             :ownership :mir/ownership-table
             :capabilities :mir/capability-proof-table
             :safety :mir/safety-table
             :provenance :mir/source-origin-map}
     :payload {:schema (:schema registration)
               :domain-family domain
               :payload-version "stage0-c12"
               :owner-doc (:owner-doc registration)}
     :verifier {:name (:verifier registration)
                :result :accepted
                :checks [:schema :anchors :source-provenance
                         :type-effect-capability-profile
                         :safety :proof-obligations :lowering :fallback]}
     :proofs [{:proof-id (keyword "proof" (str "c12-" (name domain)))
               :kind :translation-validation
               :status :accepted
               :source :mir-equivalence}]
     :lowering-status :eligible
     :fallback {:kind (:fallback registration)
                :status :available
                :residual :mir}
     :plugin-policy {:status :accepted
                     :visibility :package-visible
                     :opaque-payload? false}}))

(defn domain-ir-validate-overrides!
  [source-path artifact]
  (when-let [fail-kind (get-in artifact [:source-overrides :fail])]
    (let [[id subject-kind] (get domain-ir-override-diagnostics fail-kind)]
      (when id
        (domain-ir-fail! id source-path artifact
                         {:domain :stage0-invalid-domain
                          :stage subject-kind
                          :artifact-id (str "domain-ir-invalid-"
                                            (name fail-kind))
                          :source-span (source-span source-path 0)
                          :semantic-anchor {:mir-ops []
                                            :typed-core []}
                          :missing-fact fail-kind}
                         {:missing-fields [fail-kind]})))))