(defn-
 semantic-llvm-frozen-contract-section-07
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
    ["LC_SEGMENT_64" "LC_BUILD_VERSION" "LC_SYMTAB" "LC_DYSYMTAB"]
    (get-in
     artifact
     [:b14-record
      :abi-evidence
      :object-header
      :load-command-inventory]))
   (=
    ["LC_SEGMENT_64"
     "LC_SEGMENT_64"
     "LC_SEGMENT_64"
     "LC_DYLD_CHAINED_FIXUPS"
     "LC_DYLD_EXPORTS_TRIE"
     "LC_SYMTAB"
     "LC_DYSYMTAB"
     "LC_LOAD_DYLINKER"
     "LC_UUID"
     "LC_BUILD_VERSION"
     "LC_SOURCE_VERSION"
     "LC_MAIN"
     "LC_LOAD_DYLIB"
     "LC_FUNCTION_STARTS"
     "LC_DATA_IN_CODE"
     "LC_CODE_SIGNATURE"]
    (get-in
     artifact
     [:b14-record
      :abi-evidence
      :executable-header
      :load-command-inventory]))
   (every?
    (fn* [p1__198#] (and (integer? p1__198#) (<= 1 p1__198# 65536)))
    [(get-in
      artifact
      [:b14-record :abi-evidence :object-header :sizeofcmds])
     (get-in
      artifact
      [:b14-record :abi-evidence :executable-header :sizeofcmds])])
   (=
    4
    (get-in
     artifact
     [:b14-record :abi-evidence :object-header :ncmds]))
   (=
    16
    (get-in
     artifact
     [:b14-record :abi-evidence :executable-header :ncmds]))
   (=
    {:observed-sdk-version "26.5",
     :libsystem-compatibility-version "1.0.0",
     :gravity-exception-unwind :none,
     :compact-unwind-sections-confirmed? true,
     :single-lc-code-signature-confirmed? true,
     :platform-runtime-providers
     [:darwin/process-startup :darwin/dyld :darwin/libsystem],
     :libsystem-current-version "1356.0.0",
     :full-runtime-conformance? false,
     :single-lc-uuid-confirmed? true,
     :libsystem-load-confirmed? true,
     :executable-build-version
     {:platform :macos,
      :minimum-os-version "14.0",
      :sdk-version "26.5",
      :confirmed? true},
     :exact-linked-provider-paths ["/usr/lib/libSystem.B.dylib"],
     :gravity-runtime-providers [],
     :minimum-os-version-confirmed? true,
     :object-build-version
     {:platform :macos,
      :minimum-os-version "14.0",
      :sdk :not-applicable,
      :confirmed? true},
     :dyld-load-command-confirmed? true,
     :forbidden-load-commands-absent? true,
     :emitted-executable-sdk-version-confirmed? true,
     :platform-unwind-metadata :darwin-compact-unwind-verified}
    (get-in artifact [:b14-record :runtime-provider-evidence]))
   (=
    {:expected-exit-code
     (get-in artifact [:lowering :expected-exit-code]),
     :observed-exit-code
     (get-in artifact [:lowering :expected-exit-code]),
     :stdout-byte-count 0,
     :stderr-byte-count 0,
     :matched? true}
    (get-in artifact [:b14-record :target-process]))
   (=
    (get-in
     artifact
     [:b14-record :reference-oracle :expected-exit-code])
    (get-in artifact [:lowering :expected-exit-code]))
   (=
    :gravity/b14-reference-oracle
    (get-in artifact [:b14-record :reference-oracle :artifact]))
   (=
    :passed
    (get-in artifact [:b14-record :reference-oracle :status]))
   (=
    0
    (get-in
     artifact
     [:b14-record :reference-oracle :stdout-byte-count]))
   (true?
    (get-in
     artifact
     [:b14-record :reference-oracle :clojure-seed-boundary?]))
   (=
    #{:reference-result
      :reference-packet-id
      :reference-result-hash
      :status
      :artifact
      :stdout-byte-count
      :clojure-seed-boundary?
      :expected-exit-code}
    (set (keys (get-in artifact [:b14-record :reference-oracle]))))
   (=
    (p15-s23-c11-mir-digest
     (get-in
      artifact
      [:b14-record :reference-oracle :reference-result]))
    (get-in
     artifact
     [:b14-record :reference-oracle :reference-result-hash]))
   (= #{:llvm-ir :executable :object} (set (keys files)))
   (=
    #{:content-hash
      :bundle-build-id
      :logical-path
      :schema-version
      :mode
      :retention
      :byte-count
      :backend
      :profile
      :artifact-kind}
    (set (keys (:llvm-ir files)))))))
