(defn-
 semantic-llvm-legacy-toolchain-record
 [candidate
  source-path
  lowering
  publication-intent?
  workspace
  ir-path
  object-path
  executable-path
  target
  primary-failure
  state]
 (let
  [{:keys
    [toolchain
     ir-canonical
     compile-step
     object-canonical
     link-step
     executable-canonical
     file-step
     header-step
     load-step
     provider-step
     object-format-ok?
     executable-format-ok?
     object-ncmds
     object-sizeofcmds
     executable-ncmds
     executable-sizeofcmds
     object-load-command-inventory
     executable-load-command-inventory
     header-ok?
     dyld-ok?
     provider-paths
     forbidden-load-command?
     libsystem-ok?
     build-version-ok?
     unwind-metadata-ok?
     uuid-ok?
     code-signature-ok?
     entrypoint-ok?
     compile-and-link-silent?
     run-step
     observed-exit
     stdout-count
     stderr-count
     ir-artifact
     object-artifact
     executable-artifact]}
   state]
  {:artifact-files
   {:llvm-ir
    (assoc
     ir-artifact
     :logical-path
     "program.ll"
     :retention
     (if
      publication-intent?
      :published-output-intent
      :ephemeral-conformance-intent)),
    :object
    (assoc
     object-artifact
     :logical-path
     "program.o"
     :format
     :mach-o
     :architecture
     :arm64
     :retention
     (if
      publication-intent?
      :published-output-intent
      :ephemeral-conformance-intent)),
    :executable
    (assoc
     executable-artifact
     :logical-path
     "program"
     :format
     :mach-o
     :architecture
     :arm64
     :retention
     (if
      publication-intent?
      :published-output-intent
      :ephemeral-conformance-intent))},
   :process-evidence
   {:expected-exit-code (:expected-exit-code lowering),
    :observed-exit-code observed-exit,
    :stdout-byte-count stdout-count,
    :stderr-byte-count stderr-count,
    :matched? true},
   :publication
   {:status
    (if
     publication-intent?
     :published-after-final-verification-intent
     :ephemeral-conformance-artifacts)},
   :runtime-provider-evidence
   {:observed-sdk-version
    (get-in toolchain [:semantic-record :sdk-version]),
    :libsystem-compatibility-version "1.0.0",
    :gravity-exception-unwind :none,
    :compact-unwind-sections-confirmed? unwind-metadata-ok?,
    :single-lc-code-signature-confirmed? code-signature-ok?,
    :platform-runtime-providers
    [:darwin/process-startup :darwin/dyld :darwin/libsystem],
    :libsystem-current-version "1356.0.0",
    :full-runtime-conformance? false,
    :single-lc-uuid-confirmed? uuid-ok?,
    :libsystem-load-confirmed? libsystem-ok?,
    :executable-build-version
    {:platform :macos,
     :minimum-os-version "14.0",
     :sdk-version "26.5",
     :confirmed? build-version-ok?},
    :exact-linked-provider-paths provider-paths,
    :gravity-runtime-providers [],
    :minimum-os-version-confirmed? build-version-ok?,
    :object-build-version
    {:platform :macos,
     :minimum-os-version "14.0",
     :sdk :not-applicable,
     :confirmed? build-version-ok?},
    :dyld-load-command-confirmed? dyld-ok?,
    :forbidden-load-commands-absent? (not forbidden-load-command?),
    :emitted-executable-sdk-version-confirmed? build-version-ok?,
    :platform-unwind-metadata :darwin-compact-unwind-verified},
   :physical-tool-provenance (:physical-record toolchain),
   :publication-payload
   {:llvm-ir (:bytes ir-canonical),
    :object (:bytes object-canonical),
    :executable (:bytes executable-canonical)},
   :toolchain-fingerprint (:semantic-record toolchain),
   :abi-evidence
   {:executable-confirmed? executable-format-ok?,
    :compile-and-link-silent? compile-and-link-silent?,
    :gravity-exception-unwind :none,
    :architecture :arm64,
    :compact-unwind-sections-confirmed? unwind-metadata-ok?,
    :single-lc-code-signature-confirmed? code-signature-ok?,
    :header-confirmed? header-ok?,
    :object-header
    {:ncmds object-ncmds,
     :sizeofcmds object-sizeofcmds,
     :load-command-inventory object-load-command-inventory},
    :object-confirmed? object-format-ok?,
    :executable-header
    {:ncmds executable-ncmds,
     :sizeofcmds executable-sizeofcmds,
     :load-command-inventory executable-load-command-inventory},
    :single-lc-uuid-confirmed? uuid-ok?,
    :object-format :mach-o,
    :single-lc-main-confirmed? entrypoint-ok?,
    :target-triple target,
    :platform-unwind-metadata :darwin-compact-unwind-verified},
   :tool-records
   (vec
    (concat
     (:tool-records toolchain)
     (mapv
      :record
      [compile-step
       link-step
       file-step
       header-step
       load-step
       provider-step
       run-step])))}))
