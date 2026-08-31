(defn-
 semantic-llvm-final-build-context
 [c11-artifact
  checked-core
  context
  c11-report
  bridge-packet
  bridge-report
  binding
  lowering
  oracle
  toolchain
  state]
 (let
  [{:keys
    [source-path
     bridge-report-id
     mir
     c14-stage
     c14-request
     b1-packet
     capability-proof-table
     proof-certificate-table
     safety-proof-table
     c11-verifier-record
     c11-verifier-record-id
     source-target-selection
     profile-contract
     target-contract
     abi-contract
     runtime-contract
     provider-contract
     dependency-contract
     contract-bindings
     lowering-id]}
   state
   b3-record
   {:unsupported-record
    {:policy (:unsupported-surface p15-s23-b3-llvm-policy),
     :diagnostic "B1-UNSUPPORTED",
     :fail-before-tool? true},
    :abi-record (p15-s23-b3-llvm-expected-b3-abi-record),
    :source-target-selection source-target-selection,
    :diagnostics [],
    :lowering-id lowering-id,
    :pass-record (p15-s23-b3-llvm-expected-pass-record),
    :gravity-source-rule (dissoc binding :plan :source-path),
    :target-record (p15-s23-b3-llvm-expected-b3-target-record),
    :source-map-record
    {:mir-source-map-id
     (p15-s23-c11-mir-digest
      (get-in c11-artifact [:mir-module :source-map])),
     :operation-map (:operation-records lowering),
     :actual-paths-excluded-from-lowering-identity? true},
    :runtime-record
    {:gravity-runtime-providers [],
     :platform-runtime-providers
     [:darwin/process-startup :darwin/dyld :darwin/libsystem],
     :status :no-gravity-helpers-platform-runtime-required,
     :provider-evidence-status :delegated-platform-runtime-verified,
     :full-runtime-conformance? false},
    :status :validated-candidate-for-bounded-internal-slice,
    :provider-record
    {:build-authority
     {:effects
      #{:filesystem/read
        :process/spawn
        :process/execute
        :filesystem/write},
      :capabilities
      #{:build/apple-toolchain
        :build/atomic-publication
        :build/private-workspace},
      :providers (:build provider-contract),
      :environment-policy p15-s23-b3-llvm-environment-policy},
     :program-effects #{},
     :program-capabilities #{}},
    :artifact :gravity/b3-internal-arm64-macos-llvm-record,
    :metadata-record
    {:emitted-metadata [],
     :emitted-function-attributes [],
     :proof-to-metadata-map {},
     :proofless-metadata-rejected? true},
    :verification-tool-record
    (get-in
     toolchain
     [:toolchain-fingerprint :verification-tool-fingerprints]),
    :contract-bindings contract-bindings}
   c13-pass-provenance
   {:pass-id (get-in bridge-packet [:c13 :pass-contract :pass-id]),
    :version (get-in bridge-packet [:c13 :pass-contract :version]),
    :decision-id
    (get-in bridge-packet [:c13 :decision-record :decision-id]),
    :c13-artifact-id (get-in bridge-packet [:c13 :artifact-id]),
    :input-mir-id
    (get-in bridge-packet [:c13 :decision-record :input-mir]),
    :output-mir-id
    (get-in bridge-packet [:c13 :decision-record :output-mir])}
   pass-pipeline-base
   {:c11
    (get-in
     c11-artifact
     [:mir-module :pass-execution-record :record-id]),
    :c13 c13-pass-provenance,
    :b3 (get-in b3-record [:pass-record :passes]),
    :optimization-level
    (get-in b3-record [:pass-record :optimization-level]),
    :ub-sensitive-flags
    (get-in b3-record [:pass-record :ub-sensitive-flags])}
   pass-pipeline-digest
   (p15-s23-c11-mir-digest pass-pipeline-base)
   compiler-provenance
   {:c13-source-rule-id
    (p15-s23-c11-mir-digest
     (get-in bridge-packet [:c13 :source-rule])),
    :c14-source-rule-id
    (p15-s23-c11-mir-digest
     (get-in bridge-packet [:c14 :source-rule])),
    :b1-source-rule-id
    (p15-s23-c11-mir-digest (get-in bridge-packet [:b1 :source-rule])),
    :b3-source p15-s23-b3-llvm-expected-source-content-hash,
    :builder p15-s23-b3-llvm-expected-builder-semantic-hash,
    :toolchain (:toolchain-fingerprint toolchain),
    :target-toolchain-digest
    (p15-s23-c11-mir-digest (:toolchain-fingerprint toolchain)),
    :pass-pipeline-digest pass-pipeline-digest}
   dependency-provenance
   {:gravity-runtime-providers [],
    :platform-runtime-providers
    [:darwin/process-startup :darwin/dyld :darwin/libsystem],
    :build-providers (:build provider-contract),
    :c14-dependencies dependency-contract,
    :backend-manifest-id
    (p15-s23-c11-mir-digest
     (get-in bridge-packet [:b1 :backend-manifest])),
    :authenticated-packet-id (:artifact-id bridge-packet)}
   b13-build-identity-base
   {:schema-version 1,
    :source-inputs
    {:c14-request (get-in bridge-packet [:c14 :request :request-id]),
     :b3-source p15-s23-b3-llvm-expected-source-content-hash,
     :c14 (get-in bridge-packet [:c14 :artifact-id]),
     :authenticated-packet (:artifact-id bridge-packet),
     :mir (:mir-id c11-artifact),
     :b1 (get-in bridge-packet [:b1 :artifact-id]),
     :c13 (get-in bridge-packet [:c13 :artifact-id]),
     :b3-builder p15-s23-b3-llvm-expected-builder-semantic-hash,
     :checked-core (:artifact-id checked-core),
     :lowering lowering-id},
    :kind :native-executable-bundle,
    :compiler compiler-provenance,
    :build-environment
    {:policy p15-s23-b3-llvm-environment-policy,
     :content-id
     (p15-s23-c11-mir-digest p15-s23-b3-llvm-environment-policy)},
    :artifact-content-hashes
    (into
     (sorted-map)
     (map (fn [[kind record]] [kind (:content-hash record)]))
     (:artifact-files toolchain)),
    :artifact :gravity/b13-bounded-llvm-build-identity,
    :target (p15-s23-b3-llvm-expected-build-target),
    :backend :gravity.backend/llvm,
    :dependencies dependency-provenance,
    :profile :hosted}
   b13-build-id
   (p15-s23-c11-mir-digest b13-build-identity-base)
   b13-build-identity
   (assoc b13-build-identity-base :build-id b13-build-id)
   b13-artifact-files
   (into
    (sorted-map)
    (map
     (fn
      [[kind record]]
      [kind
       (assoc
        record
        :schema-version
        1
        :backend
        :gravity.backend/llvm
        :profile
        :hosted
        :bundle-build-id
        b13-build-id)]))
    (:artifact-files toolchain))]
  (assoc
   state
   :b3-record
   b3-record
   :c13-pass-provenance
   c13-pass-provenance
   :pass-pipeline-base
   pass-pipeline-base
   :pass-pipeline-digest
   pass-pipeline-digest
   :compiler-provenance
   compiler-provenance
   :dependency-provenance
   dependency-provenance
   :b13-build-identity-base
   b13-build-identity-base
   :b13-build-id
   b13-build-id
   :b13-build-identity
   b13-build-identity
   :b13-artifact-files
   b13-artifact-files)))
