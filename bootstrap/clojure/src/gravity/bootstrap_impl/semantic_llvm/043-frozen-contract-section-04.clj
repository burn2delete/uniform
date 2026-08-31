(defn-
 semantic-llvm-frozen-contract-section-04
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
    #{:whole-b3-gate
      :upstream-pass-risk
      :whole-c14-gate
      :whole-c13-gate
      :minimum-evidence
      :source-target-selection
      :native-publication-boundary
      :whole-b13-gate
      :affected-target
      :self-hosting-gate
      :risks
      :public-target-gate
      :performance-residual
      :critical-risk
      :trust-boundary
      :whole-b14-gate
      :release-gate
      :status
      :whole-b1-gate
      :whole-c11-gate
      :artifact
      :pass
      :process-cleanup-boundary
      :contract-bindings
      :evidence-ids
      :whole-c18-gate}
    (set (keys c18)))
   (= [] (get-in artifact [:c14-request :diagnostics]))
   (= [] (get-in artifact [:b1-packet :diagnostics]))
   (= [] (get-in artifact [:b3-record :diagnostics]))
   (= [] (get-in artifact [:b13-record :diagnostics]))
   (=
    (get-in artifact [:b1-packet :input])
    (get-in artifact [:c14-request :input]))
   (=
    #{:domain-status
      :artifact-id
      :optimization-report
      :verifier-report-id
      :verified?
      :optimization-status
      :id
      :kind
      :verifier-report}
    (set (keys (get-in artifact [:b1-packet :input]))))
   (= :gravity/mir (get-in artifact [:b1-packet :input :kind]))
   (true? (get-in artifact [:b1-packet :input :verified?]))
   (=
    :identity-pass-complete
    (get-in artifact [:b1-packet :input :optimization-status]))
   (=
    :passed
    (get-in
     artifact
     [:b1-packet :input :optimization-report :verifier-result]))
   (=
    (:required-evidence p15-s23-b3-llvm-policy)
    (get-in artifact [:c14-request :required-evidence]))
   (=
    (get-in artifact [:c14-request :source-map])
    (get-in artifact [:b1-packet :source-map]))
   (= true (get-in artifact [:b1-packet :source-map :preserved?]))
   (=
    (get-in artifact [:c14-request :dependency-provenance])
    (get-in artifact [:b1-packet :dependencies]))
   (every?
    (fn* [p1__195#] (= selection p1__195#))
    [(get-in artifact [:c14-request :source-target-selection])
     (get-in artifact [:b1-packet :source-target-selection])
     (get-in artifact [:b3-record :source-target-selection])
     (get-in artifact [:b13-record :source-target-selection])
     (:source-target-selection c18)])
   (=
    selection
    (select-keys
     (get-in artifact [:c14-request :target])
     (keys selection)))
   (=
    selection
    (select-keys
     (get-in artifact [:b1-packet :target])
     (keys selection)))
   (= :jvm (:source-declaration-target b14-scope))
   (= :llvm-x86_64-linux (:requested-lowering-target b14-scope))
   (= :hosted (get-in artifact [:c14-request :profile]))
   (= profile (get-in artifact [:c14-request :profile-contract]))
   (= profile (get-in artifact [:b1-packet :profile]))
   (= :hosted (get-in artifact [:b13-record :profile]))
   (= :hosted (:profile build-identity))
   (=
    (assoc
     (p15-s23-b3-llvm-expected-target-contract)
     :profile-eligibility
     [:hosted])
    (dissoc target :fingerprint))
   (=
    (:fingerprint target)
    (p15-s23-c11-mir-digest
     {:kind :gravity/c14-bounded-llvm-target-fingerprint,
      :target (dissoc target :fingerprint)}))
   (= target (get-in artifact [:b1-packet :target]))
   (= b3-target (get-in artifact [:b3-record :target-record]))
   (= b13-target (get-in artifact [:b13-record :target]))
   (= build-target (:target build-identity))
   (=
    {:scope
     :bounded-pure-scalar-forwarding-do-let-if-integer-comparisons,
     :maximum-operation-count p15-s23-b3-llvm-max-bridge-operations,
     :whole-c14? false,
     :whole-b1? false,
     :whole-b3? false,
     :public? false,
     :release? false,
     :self-hosted? false}
    (get-in artifact [:c14-request :target-policy]))
   (=
    p15-s23-b3-llvm-source-lowering-policy
    (get-in artifact [:lowering :policy]))
   (=
    {:source-declaration-target :jvm,
     :requested-lowering-target :llvm-x86_64-linux,
     :triple (:target-triple p15-s23-b3-llvm-policy),
     :architecture :arm64,
     :object-format :mach-o,
     :exposure :internal,
     :tier :experimental}
    (:affected-target c18))
   (= source-rule (get-in artifact [:b3-record :gravity-source-rule]))
   (=
    p15-s23-b3-llvm-expected-source-content-hash
    (get-in artifact [:b13-record :compiler-provenance :b3-source]))
   (=
    p15-s23-b3-llvm-expected-builder-semantic-hash
    (get-in artifact [:b13-record :compiler-provenance :builder]))
   (=
    expected-compiler-provenance
    (get-in artifact [:b13-record :compiler-provenance]))
   (=
    toolchain-static
    (select-keys toolchain-fingerprint (keys toolchain-static)))
   (=
    (set
     (concat
      (keys toolchain-static)
      [:clang-version-normalized-fingerprint
       :linker-version-normalized-fingerprint
       :verification-tool-fingerprints]))
    (set (keys toolchain-fingerprint)))
   (normalized-fingerprint?
    (:clang-version-normalized-fingerprint toolchain-fingerprint))
   (normalized-fingerprint?
    (:linker-version-normalized-fingerprint toolchain-fingerprint)))))
