(defn-
 semantic-llvm-frozen-contract-section-08
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
   (every?
    (fn*
     [p1__199#]
     (=
      #{:format
        :architecture
        :content-hash
        :bundle-build-id
        :logical-path
        :schema-version
        :mode
        :retention
        :byte-count
        :backend
        :profile
        :artifact-kind}
      (set (keys (get files p1__199#)))))
    [:object :executable])
   (every?
    (fn
     [[kind file]]
     (and
      (=
       (get expected-artifact-kinds kind)
       (select-keys file (keys (get expected-artifact-kinds kind))))
      (= 1 (:schema-version file))
      (= :gravity.backend/llvm (:backend file))
      (= :hosted (:profile file))
      (= build-id (:bundle-build-id file))
      (integer? (:byte-count file))
      (<= 0 (:byte-count file) p15-s23-b3-llvm-max-emitted-file-bytes)
      (string? (:content-hash file))
      (re-matches #"sha256:[0-9a-f]{64}" (:content-hash file))))
    files)
   (contains?
    #{#{:published-output-intent} #{:ephemeral-conformance-intent}}
    (set (map :retention (vals files))))
   (=
    #{:schema-version
      :source-inputs
      :build-id
      :kind
      :compiler
      :build-environment
      :artifact-content-hashes
      :artifact
      :target
      :backend
      :dependencies
      :profile}
    (set (keys build-identity)))
   (=
    :gravity/b13-bounded-llvm-build-identity
    (:artifact build-identity))
   (= 1 (:schema-version build-identity))
   (= :native-executable-bundle (:kind build-identity))
   (= :gravity.backend/llvm (:backend build-identity))
   (= :native-executable-bundle (get-in artifact [:b13-record :kind]))
   (= :gravity.backend/llvm (get-in artifact [:b13-record :backend]))
   (= source-inputs (:source-inputs build-identity))
   (=
    {:source-core (:checked-core source-inputs),
     :mir (:mir source-inputs),
     :c13 (:c13 source-inputs),
     :c14-request (:c14-request source-inputs),
     :c14 (:c14 source-inputs),
     :b1 (:b1 source-inputs),
     :authenticated-packet (:authenticated-packet source-inputs),
     :lowering (:lowering source-inputs)}
    (get-in artifact [:b13-record :inputs]))
   (= expected-compiler-provenance (:compiler build-identity))
   (=
    (:c11 pass-pipeline-base)
    (get-in
     artifact
     [:b1-packet
      :input
      :verifier-report
      :b1-preflight
      :binding
      :pass-execution-record-id]))
   (=
    {:c11 (:c11 pass-pipeline-base),
     :c13 (:c13 pass-pipeline-base),
     :b3 (:b3 pass-pipeline-base),
     :pass-pipeline-digest pass-pipeline-digest}
    (get-in artifact [:b13-record :pass-provenance]))
   (=
    {:policy p15-s23-b3-llvm-environment-policy,
     :content-id
     (p15-s23-c11-mir-digest p15-s23-b3-llvm-environment-policy)}
    (:build-environment build-identity))
   (=
    {:directory "0755", :executable "0755", :nonexecutable "0644"}
    (get-in artifact [:b13-record :mode-policy]))
   (=
    :omitted-for-reproducibility
    (get-in artifact [:b13-record :timestamp-policy]))
   (=
    {:fixed-basenames true,
     :random-parent-excluded true,
     :default-linker-uuid-and-adhoc-signature true}
    (get-in artifact [:b13-record :nondeterminism-policy]))
   (=
    expected-artifact-graph
    (get-in artifact [:b13-record :artifact-graph]))
   (=
    {:pass-pipeline-digest pass-pipeline-digest,
     :independent-repeat-required-for-credit? true,
     :random-parent-path-excluded? true,
     :target-toolchain-digest toolchain-digest,
     :environment-inputs-digest
     (p15-s23-c11-mir-digest p15-s23-b3-llvm-environment-policy),
     :status :single-build-candidate,
     :environment-inputs p15-s23-b3-llvm-environment-policy,
     :fixed-logical-names ["program.ll" "program.o" "program"],
     :content-hashes-recorded? true}
    (get-in artifact [:b13-record :reproducibility]))
   (=
    (if
     (=
      #{:published-output-intent}
      (set (map :retention (vals files))))
     :published-after-final-verification-intent
     :ephemeral-conformance-artifacts)
    (get-in artifact [:b13-record :publication :status]))
   (=
    #{:status}
    (set (keys (get-in artifact [:b13-record :publication]))))
   (=
    :closed
    (:release-gate c18)
    (:public-target-gate c18)
    (:self-hosting-gate c18)
    (:whole-c11-gate c18)
    (:whole-c13-gate c18)
    (:whole-c14-gate c18)
    (:whole-b1-gate c18)
    (:whole-b3-gate c18)
    (:whole-b13-gate c18)
    (:whole-b14-gate c18)
    (:whole-c18-gate c18)))))
