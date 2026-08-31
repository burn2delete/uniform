(defn-
 semantic-llvm-frozen-contract-section-06
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
    {:outcomes
     (count
      (get-in
       artifact
       [:c13-c14-b1-packet :optimized-mir :safety-table])),
     :runtime-checks
     (count
      (get-in
       artifact
       [:c13-c14-b1-packet :optimized-mir :runtime-check-table])),
     :unsafe-islands 0,
     :binding (:safety bindings)}
    (get-in artifact [:b1-packet :safety]))
   (=
    #{}
    (get-in artifact [:b3-record :provider-record :program-effects]))
   (=
    #{}
    (get-in
     artifact
     [:b3-record :provider-record :program-capabilities]))
   (= #{} (get-in artifact [:b13-record :effects]))
   (= #{} (get-in artifact [:b13-record :capabilities]))
   (=
    {:emitted-metadata [],
     :emitted-function-attributes [],
     :proof-to-metadata-map {},
     :proofless-metadata-rejected? true}
    (get-in artifact [:b3-record :metadata-record]))
   (= pass-record (get-in artifact [:b3-record :pass-record]))
   (=
    (:passes pass-record)
    (get-in artifact [:b13-record :pass-provenance :b3]))
   (=
    {:runtime-checks 0,
     :unsafe-islands 0,
     :ub-sensitive-flags [],
     :table-binding (:safety bindings)}
    (get-in artifact [:b13-record :safety]))
   (=
    {:metadata-map {},
     :c11-verifier :passed,
     :c11-verifier-record-id
     (get-in bindings [:c11-verifier :content-id]),
     :c13-c14-b1-contextual-replay :passed,
     :c13-c14-b1-contextual-report-id
     (:report-id expected-bridge-report),
     :capability-proof-table (:capabilities bindings),
     :proof-certificate-table (:proofs bindings),
     :b3-reconstruction :passed}
    (get-in artifact [:b13-record :proof]))
   (=
    :identity-pass-complete
    (get-in artifact [:c14-request :c13-optimization-status]))
   (=
    {:c13-c14-b1-contextual-replay (:report-id expected-bridge-report)}
    (:evidence-ids c18))
   (=
    :not-applicable
    (get-in artifact [:c14-request :domain-ir-status]))
   (= :not-run (get-in artifact [:c14-request :fusion-status]))
   (=
    {:effects
     #{:filesystem/read
       :process/spawn
       :process/execute
       :filesystem/write},
     :capabilities
     #{:build/apple-toolchain
       :build/atomic-publication
       :build/private-workspace},
     :providers (:build providers),
     :environment-policy p15-s23-b3-llvm-environment-policy}
    (get-in artifact [:b3-record :provider-record :build-authority]))
   (=
    #{:build-authority :program-effects :program-capabilities}
    (set (keys (get-in artifact [:b3-record :provider-record]))))
   (=
    {:policy (:unsupported-surface p15-s23-b3-llvm-policy),
     :diagnostic "B1-UNSUPPORTED",
     :fail-before-tool? true}
    (get-in artifact [:b3-record :unsupported-record]))
   (true?
    (get-in
     artifact
     [:b3-record
      :source-map-record
      :actual-paths-excluded-from-lowering-identity?]))
   (=
    #{:operation-map
      :mir-source-map-id
      :actual-paths-excluded-from-lowering-identity?}
    (set (keys (get-in artifact [:b3-record :source-map-record]))))
   (=
    (get-in
     artifact
     [:b3-record :source-map-record :mir-source-map-id])
    (get-in artifact [:c14-request :source-map :id]))
   (=
    (get-in artifact [:lowering :operation-records])
    (get-in artifact [:b3-record :source-map-record :operation-map]))
   (=
    (get-in artifact [:b14-record :abi-evidence])
    (get-in artifact [:b13-record :conformance :abi])
    (get-in artifact [:toolchain-evidence :abi-evidence]))
   (=
    (get-in artifact [:b14-record :runtime-provider-evidence])
    (get-in artifact [:b13-record :conformance :runtime])
    (get-in artifact [:toolchain-evidence :runtime-provider-evidence]))
   (=
    (get-in artifact [:b14-record :target-process])
    (get-in artifact [:b13-record :conformance :differential])
    (get-in artifact [:toolchain-evidence :process-evidence]))
   (true? (get-in artifact [:b14-record :same-result?]))
   (=
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
     :cross-host? false}
    b14-scope)
   (=
    {:executable-confirmed? true,
     :compile-and-link-silent? true,
     :gravity-exception-unwind :none,
     :architecture :arm64,
     :compact-unwind-sections-confirmed? true,
     :single-lc-code-signature-confirmed? true,
     :header-confirmed? true,
     :object-confirmed? true,
     :single-lc-uuid-confirmed? true,
     :object-format :mach-o,
     :single-lc-main-confirmed? true,
     :target-triple (:target-triple p15-s23-b3-llvm-policy),
     :platform-unwind-metadata :darwin-compact-unwind-verified}
    (dissoc
     (get-in artifact [:b14-record :abi-evidence])
     :object-header
     :executable-header)))))
