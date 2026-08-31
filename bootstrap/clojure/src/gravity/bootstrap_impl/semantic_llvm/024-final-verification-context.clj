(defn-
 semantic-llvm-final-verification-context
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
     b13-artifact-files
     b13-record]}
   state
   b14-record
   {:artifact :gravity/b14-bounded-llvm-differential-record,
    :status :passed,
    :availability-scope
    {:kind :bounded-positive-and-negative,
     :source-declaration-target :jvm,
     :requested-lowering-target :llvm-x86_64-linux,
     :positive
     [:scalar-i64-bool-nil
      :forwarding
      :single-conditional
      :signed-i64-integer-comparisons
      :mach-o-arm64
      :darwin-process-exit],
     :negative (:unsupported-surface p15-s23-b3-llvm-policy),
     :whole-backend? false,
     :cross-host? false},
    :reference-oracle oracle,
    :target-process (:process-evidence toolchain),
    :abi-evidence (:abi-evidence toolchain),
    :runtime-provider-evidence (:runtime-provider-evidence toolchain),
    :same-result?
    (=
     (:expected-exit-code oracle)
     (get-in toolchain [:process-evidence :observed-exit-code]))}
   c18-minimum-evidence
   [:fresh-c11-replay
    :c13-identity-verifier-replay
    :c14-lowering-eligibility-reconstruction
    :b1-manifest-reconstruction
    :c13-c14-b1-contextual-replay
    :gravity-b3-replay
    :independent-lowering-reconstruction
    :clang-codegen
    :mach-o-abi
    :darwin-runtime-provider
    :differential-execution
    :content-hashes]
   c18-record
   {:whole-b3-gate :closed,
    :upstream-pass-risk
    {:minimum-evidence
     #{:effect-order-graph-replay
       :fresh-c11-replay
       :identity-bound-c11-verifier-replay
       :c13-source-pin
       :fact-table-replay
       :operation-order-replay},
     :release-policy :internal-experimental-only,
     :artifact-kinds #{:gravity/mir :gravity/optimized-mir},
     :self-hosting-gate :closed,
     :target-assumptions [],
     :available-evidence
     #{:effect-order-graph-replay
       :fresh-c11-replay
       :identity-bound-c11-verifier-replay
       :c13-source-pin
       :fact-table-replay
       :operation-order-replay},
     :risk :critical,
     :reason
     #{:seed-executed-semantic-preservation-boundary
       :trusted-semantic-base},
     :maximum-operation-count p15-s23-b3-llvm-max-bridge-operations,
     :release-gate :closed,
     :affected-profiles #{:hosted},
     :artifact :gravity/pass-risk,
     :pass :c13-bounded-identity,
     :version 1,
     :affected-targets #{:llvm},
     :whole-pass-gate :closed},
    :whole-c14-gate :closed,
    :whole-c13-gate :closed,
    :minimum-evidence c18-minimum-evidence,
    :source-target-selection source-target-selection,
    :native-publication-boundary
    {:status :bounded-no-clobber-no-symlink,
     :provider :darwin-libsystem-renamex-np,
     :ffi-provider :openjdk-26.0.1-ffm-native-access,
     :flags 20,
     :errno-read-policy :failure-only,
     :concurrent-same-uid-inode-linearization? false},
    :whole-b13-gate :closed,
    :affected-target
    {:source-declaration-target :jvm,
     :requested-lowering-target :llvm-x86_64-linux,
     :triple (:target-triple p15-s23-b3-llvm-policy),
     :architecture :arm64,
     :object-format :mach-o,
     :exposure :internal,
     :tier :experimental},
    :self-hosting-gate :closed,
    :risks
    [:seed-hosted-builder-execution
     :bounded-language-surface
     :single-host-toolchain
     :single-build-candidate
     :upstream-fresh-replay-performance-residual
     :jvm-source-target-bootstrap-override
     :concurrent-same-uid-publication-path-mutation-unproved
     :concurrent-descendant-fork-during-timeout-cleanup-unproved
     :native-publication-requires-jdk26-native-access],
    :public-target-gate :closed,
    :performance-residual
    {:status :open,
     :boundary :pre-b3-fresh-checked-core-c11-replay,
     :observed-order-of-magnitude :approximately-150-seconds,
     :latency-slo :not-established,
     :replay-weakened? false,
     :source-to-native-operationally-complete? false},
    :critical-risk :seed-and-single-host-toolchain,
    :trust-boundary
    [:clojure-stage0-seed
     :clojure-stage0-rule-runner
     :gravity-c13-source
     :gravity-c14-source
     :gravity-b1-source
     :gravity-b3-source
     :apple-xcrun-72
     :apple-clang-21
     :apple-ld-1267
     :file-5.41
     :llvm-otool-cctools-1040
     :darwin-process-loader
     :openjdk-26.0.1-ffm-native-access
     :darwin-libsystem-renamex-np
     :darwin-platform-runtime],
    :whole-b14-gate :closed,
    :release-gate :closed,
    :status :internal-experimental-only,
    :whole-b1-gate :closed,
    :whole-c11-gate :closed,
    :artifact :gravity/c18-bounded-llvm-risk-trust-record,
    :pass
    {:id :gravity-b3-bounded-arm64-macos-llvm,
     :version 1,
     :risk-class :critical-seed-and-native-code-emission,
     :required-evidence c18-minimum-evidence,
     :available-evidence c18-minimum-evidence,
     :missing-for-full-credit
     [:source-declared-llvm
      :bounded-fresh-replay-latency
      :second-host-toolchain
      :whole-c11
      :whole-c13
      :whole-c14
      :whole-b1
      :whole-b3
      :whole-language
      :seedless-self-hosting
      :fd-relative-publication-linearization
      :whole-process-tree-reaping-proof]},
    :process-cleanup-boundary
    {:captured-process-set-rechecked? true,
     :concurrent-descendant-fork-linearization? false,
     :whole-process-tree-proof? false},
    :contract-bindings contract-bindings,
    :evidence-ids {:c13-c14-b1-contextual-replay bridge-report-id},
    :whole-c18-gate :closed}]
  (assoc
   state
   :b14-record
   b14-record
   :c18-minimum-evidence
   c18-minimum-evidence
   :c18-record
   c18-record)))
