(defn- __gravity_bootstrap_gate_b_transaction_result [state]
  (let [{:syms
         [candidate
          gate-a
          source-path
          publication-intent?
          host-runtime
          workspace
          primary-failure
          workspace-binding
          source-text
          header-text
          expected-exit
          source-initial
          header-initial
          discovery
          run
          target
          clang
          sdk
          ld
          otool
          flags
          syntax-command
          compile-command
          link-command
          syntax
          compile
          _
          object-initial
          link
          executable-initial
          file-step
          header-step
          load-step
          provider-step
          run-step
          physical-final
          source-final
          header-final
          object-final
          executable-final
          inventory
          final-workspace-binding
          file-text
          header-text-output
          load-text
          provider-text
          header-parts
          load-parts
          object-header-section
          executable-header-section
          object-load-section
          executable-load-section
          object-header-ok?
          executable-header-ok?
          commands
          all-commands
          command-labels
          object-commands
          executable-commands
          forbidden-load-command?
          provider-ok?
          build-version-ok?
          libsystem-ok?
          dyld-ok?
          unwind-ok?
          single-main?
          single-uuid?
          single-code-signature?
          exact-snapshot?
          silent?]} state]
    {:artifact-files
     {:source
      (assoc
        (p15-s23-b2-c17-gate-b-snapshot-content source-final)
        :artifact-kind
        :c-source
        :logical-path
        "program.c"
        :mode
        "0644"),
      :header
      (assoc
        (p15-s23-b2-c17-gate-b-snapshot-content header-final)
        :artifact-kind
        :c-header
        :logical-path
        "program.h"
        :mode
        "0644"),
      :object
      (assoc
        (p15-s23-b2-c17-gate-b-snapshot-content object-final)
        :artifact-kind
        :mach-o-object
        :logical-path
        "program.o"
        :mode
        "0644"
        :format
        :mach-o
        :architecture
        :arm64),
      :executable
      (assoc
        (p15-s23-b2-c17-gate-b-snapshot-content executable-final)
        :artifact-kind
        :mach-o-executable
        :logical-path
        "program"
        :mode
        "0755"
        :format
        :mach-o
        :architecture
        :arm64)},
     :process-evidence
     {:expected-exit-code expected-exit,
      :observed-exit-code (get-in run-step [:result :exit-code]),
      :stdout-byte-count 0,
      :stderr-byte-count 0,
      :matched? true},
     :runtime-provider-evidence
     {:libsystem-compatibility-version "1.0.0",
      :compact-unwind-confirmed? true,
      :build-version-confirmed? true,
      :platform-runtime-providers
      [:darwin/process-startup :darwin/dyld :darwin/libsystem],
      :libsystem-current-version "1356.0.0",
      :full-runtime-conformance? false,
      :status :passed,
      :libsystem-load-confirmed? true,
      :exact-linked-provider-paths ["/usr/lib/libSystem.B.dylib"],
      :gravity-runtime-providers [],
      :dyld-load-command-confirmed? true,
      :forbidden-load-commands-absent? true,
      :libsystem-path "/usr/lib/libSystem.B.dylib"},
     :physical-tool-provenance physical-final,
     :publication-payload
     {:source (:bytes source-final),
      :header (:bytes header-final),
      :object (:bytes object-final),
      :executable (:bytes executable-final)},
     :toolchain-fingerprint
     (assoc (:semantic-record discovery) :host-runtime host-runtime),
     :publication-intent? (boolean publication-intent?),
     :abi-evidence
     {:single-code-signature-confirmed? true,
      :executable-header-confirmed? true,
      :single-main-confirmed? true,
      :compile-and-link-silent? true,
      :architecture :arm64,
      :compact-unwind-confirmed? true,
      :single-uuid-confirmed? true,
      :object-header {:ncmds 4, :sizeofcmds 360},
      :executable-header {:ncmds 16, :sizeofcmds 744},
      :executable-load-command-inventory
      p15-s23-b2-c17-gate-b-executable-load-commands,
      :object-format :mach-o,
      :status :passed,
      :executable-build-version {:minimum-os "14.0", :sdk "26.5", :linker "1267.0"},
      :object-load-command-inventory p15-s23-b2-c17-gate-b-object-load-commands,
      :target-triple target,
      :syntax-confirmed? true,
      :object-build-version {:minimum-os "14.0", :sdk "26.5"},
      :object-header-confirmed? true,
      :dialect :c17},
     :tool-records
     (vec
       (concat
         (:tool-records discovery)
         (mapv
           :record
           [syntax
            compile
            link
            file-step
            header-step
            load-step
            provider-step
            run-step])))}))
