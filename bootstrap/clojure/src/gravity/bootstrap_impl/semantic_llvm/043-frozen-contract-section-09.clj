(defn-
 semantic-llvm-frozen-contract-section-09
 [artifact state]
 (let
  [{:keys
    [selection
     profile
     target
     b3-target
     b13-target
     build-target
     abi
     b3-abi
     runtime
     providers
     pass-record
     contract-bindings
     bindings
     files
     metadata-free-files
     build-id
     build-identity
     b14-scope
     c18
     expected-bridge-report
     expected-build-providers
     expected-dependencies
     expected-artifact-kinds
     source-rule
     toolchain-fingerprint
     toolchain-static
     tool-records
     tool-record-by-step
     expected-command-contracts
     normalized-fingerprint?
     source-inputs
     toolchain-digest
     c13-pass-provenance
     pass-pipeline-base
     pass-pipeline-digest
     expected-compiler-provenance
     expected-artifact-graph
     actual-path-provenance
     actual-path-base-keys
     publication-path
     publication-receipt
     physical-record
     retentions
     sha256-value?
     absolute-path?]}
   state]
  (and
   (=
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
     :darwin-platform-runtime]
    (:trust-boundary c18))
   (=
    {:status :bounded-no-clobber-no-symlink,
     :provider :darwin-libsystem-renamex-np,
     :ffi-provider :openjdk-26.0.1-ffm-native-access,
     :flags 20,
     :errno-read-policy :failure-only,
     :concurrent-same-uid-inode-linearization? false}
    (:native-publication-boundary c18))
   (=
    {:captured-process-set-rechecked? true,
     :concurrent-descendant-fork-linearization? false,
     :whole-process-tree-proof? false}
    (:process-cleanup-boundary c18))
   (=
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
     :whole-pass-gate :closed}
    (:upstream-pass-risk c18))
   (=
    {:id :gravity-b3-bounded-arm64-macos-llvm,
     :version 1,
     :risk-class :critical-seed-and-native-code-emission,
     :required-evidence
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
      :content-hashes],
     :available-evidence
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
      :content-hashes],
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
      :whole-process-tree-reaping-proof]}
    (:pass c18))
   (= :seed-and-single-host-toolchain (:critical-risk c18))
   (=
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
    (:minimum-evidence c18))
   (=
    [:seed-hosted-builder-execution
     :bounded-language-surface
     :single-host-toolchain
     :single-build-candidate
     :upstream-fresh-replay-performance-residual
     :jvm-source-target-bootstrap-override
     :concurrent-same-uid-publication-path-mutation-unproved
     :concurrent-descendant-fork-during-timeout-cleanup-unproved
     :native-publication-requires-jdk26-native-access]
    (:risks c18))
   (=
    {:status :open,
     :boundary :pre-b3-fresh-checked-core-c11-replay,
     :observed-order-of-magnitude :approximately-150-seconds,
     :latency-slo :not-established,
     :replay-weakened? false,
     :source-to-native-operationally-complete? false}
    (:performance-residual c18))
   (true? (:seed-boundary? artifact))
   (true? (:clojure-seed-boundary? artifact))
   (every?
    false?
    (map
     artifact
     [:c11-llvm-credit?
      :target-lowering-credit?
      :backend-credit?
      :public-target?
      :release-credit?
      :self-hosted?
      :whole-language?])))))
