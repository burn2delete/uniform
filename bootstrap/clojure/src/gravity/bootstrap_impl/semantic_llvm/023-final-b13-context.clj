(defn-
 semantic-llvm-final-b13-context
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
     lowering-id
     b3-record
     c13-pass-provenance
     pass-pipeline-base
     pass-pipeline-digest
     compiler-provenance
     dependency-provenance
     b13-build-identity-base
     b13-build-id
     b13-build-identity
     b13-artifact-files]}
   state
   b13-record
   {:capabilities #{},
    :providers
    [:darwin/process-startup :darwin/dyld :darwin/libsystem],
    :timestamp-policy :omitted-for-reproducibility,
    :source-target-selection source-target-selection,
    :mode-policy
    {:directory "0755", :executable "0755", :nonexecutable "0644"},
    :artifact-files b13-artifact-files,
    :compiler-provenance compiler-provenance,
    :diagnostics [],
    :pass-provenance
    {:c11
     (get-in
      c11-artifact
      [:mir-module :pass-execution-record :record-id]),
     :c13 c13-pass-provenance,
     :b3 (get-in b3-record [:pass-record :passes]),
     :pass-pipeline-digest pass-pipeline-digest},
    :conformance
    {:abi (:abi-evidence toolchain),
     :runtime (:runtime-provider-evidence toolchain),
     :differential (:process-evidence toolchain)},
    :build-identity b13-build-identity,
    :publication (:publication toolchain),
    :schema-version 1,
    :artifact-graph
    [{:from (:artifact-id checked-core),
      :to (:mir-id c11-artifact),
      :edge :authenticated-c11-mir-construction}
     {:from (:mir-id c11-artifact),
      :to (get-in bridge-packet [:c13 :artifact-id]),
      :edge :gravity-c13-identity-optimization}
     {:from (get-in bridge-packet [:c13 :artifact-id]),
      :to (get-in bridge-packet [:c14 :request :request-id]),
      :edge :gravity-c14-lowering-request}
     {:from (get-in bridge-packet [:c14 :request :request-id]),
      :to (get-in bridge-packet [:c14 :artifact-id]),
      :edge :gravity-c14-target-lowering-acceptance}
     {:from (get-in bridge-packet [:c14 :artifact-id]),
      :to (get-in bridge-packet [:b1 :artifact-id]),
      :edge :gravity-b1-backend-authentication}
     {:from (get-in bridge-packet [:b1 :artifact-id]),
      :to (:artifact-id bridge-packet),
      :edge :authenticated-c13-c14-b1-packet}
     {:from (:artifact-id bridge-packet),
      :to lowering-id,
      :edge :verified-optimized-mir-lowering}
     {:from lowering-id,
      :to (get-in toolchain [:artifact-files :llvm-ir :content-hash]),
      :edge :llvm-ir-emission}
     {:from
      (get-in toolchain [:artifact-files :llvm-ir :content-hash]),
      :to (get-in toolchain [:artifact-files :object :content-hash]),
      :edge :clang-codegen}
     {:from (get-in toolchain [:artifact-files :object :content-hash]),
      :to
      (get-in toolchain [:artifact-files :executable :content-hash]),
      :edge :darwin-link}
     {:from b13-build-id,
      :to (get-in toolchain [:artifact-files :llvm-ir :content-hash]),
      :edge :bundle-build-identity}
     {:from b13-build-id,
      :to (get-in toolchain [:artifact-files :object :content-hash]),
      :edge :bundle-build-identity}
     {:from b13-build-id,
      :to
      (get-in toolchain [:artifact-files :executable :content-hash]),
      :edge :bundle-build-identity}],
    :content-hashes
    (into
     (sorted-map)
     (map (fn [[kind record]] [kind (:content-hash record)]))
     b13-artifact-files),
    :proof
    {:metadata-map {},
     :c11-verifier :passed,
     :c11-verifier-record-id c11-verifier-record-id,
     :c13-c14-b1-contextual-replay :passed,
     :c13-c14-b1-contextual-report-id bridge-report-id,
     :capability-proof-table (get contract-bindings :capabilities),
     :proof-certificate-table (get contract-bindings :proofs),
     :b3-reconstruction :passed},
    :build-id b13-build-id,
    :inputs
    {:source-core (:artifact-id checked-core),
     :mir (:mir-id c11-artifact),
     :c13 (get-in bridge-packet [:c13 :artifact-id]),
     :c14-request (get-in bridge-packet [:c14 :request :request-id]),
     :c14 (get-in bridge-packet [:c14 :artifact-id]),
     :b1 (get-in bridge-packet [:b1 :artifact-id]),
     :authenticated-packet (:artifact-id bridge-packet),
     :lowering lowering-id},
    :nondeterminism-policy
    {:fixed-basenames true,
     :random-parent-excluded true,
     :default-linker-uuid-and-adhoc-signature true},
    :reproducibility
    {:pass-pipeline-digest pass-pipeline-digest,
     :independent-repeat-required-for-credit? true,
     :random-parent-path-excluded? true,
     :target-toolchain-digest
     (p15-s23-c11-mir-digest (:toolchain-fingerprint toolchain)),
     :environment-inputs-digest
     (p15-s23-c11-mir-digest p15-s23-b3-llvm-environment-policy),
     :status :single-build-candidate,
     :environment-inputs p15-s23-b3-llvm-environment-policy,
     :fixed-logical-names ["program.ll" "program.o" "program"],
     :content-hashes-recorded? true},
    :status :content-addressed,
    :effects #{},
    :runtime
    {:status :platform-runtime-delegated, :full-conformance? false},
    :safety
    {:runtime-checks 0,
     :unsafe-islands 0,
     :ub-sensitive-flags [],
     :table-binding (get contract-bindings :safety)},
    :kind :native-executable-bundle,
    :dependency-provenance dependency-provenance,
    :artifact :gravity/b13-bounded-llvm-emission-record,
    :target (p15-s23-b3-llvm-expected-b13-target),
    :abi-layout
    {:calling-convention :darwin-pcs-ccc,
     :data-layout (:data-layout p15-s23-b3-llvm-policy),
     :gravity-exception-unwind :none,
     :platform-unwind-metadata :darwin-compact-unwind-verified},
    :backend :gravity.backend/llvm,
    :contract-bindings contract-bindings,
    :profile :hosted}]
  (assoc state :b13-record b13-record)))
