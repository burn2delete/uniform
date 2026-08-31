(defn- __gravity_bootstrap_gate_b_transaction_validation [state]
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
    (when-not (and
                (= #{"program.c" "program.h" "program.o" "program"} inventory)
                (= workspace-binding final-workspace-binding)
                (exact-snapshot? source-initial source-final)
                (exact-snapshot? header-initial header-final)
                (exact-snapshot? object-initial object-final)
                (exact-snapshot? executable-initial executable-final)
                (every? silent? [syntax compile link run-step])
                (= 2 (count (str/split-lines file-text)))
                (=
                  "program.o: Mach-O 64-bit object arm64"
                  (first (str/split-lines file-text)))
                (boolean
                  (re-matches
                    #"program:\s+Mach-O 64-bit executable arm64"
                    (second (str/split-lines file-text))))
                object-header-ok?
                executable-header-ok?
                (=
                  p15-s23-b2-c17-gate-b-object-load-commands
                  object-commands
                  (all-commands object-load-section))
                (= (vec (range 4)) (command-labels object-load-section))
                (=
                  p15-s23-b2-c17-gate-b-executable-load-commands
                  executable-commands
                  (all-commands executable-load-section))
                (= (vec (range 16)) (command-labels executable-load-section))
                build-version-ok?
                provider-ok?
                libsystem-ok?
                dyld-ok?
                (not forbidden-load-command?)
                unwind-ok?
                single-main?
                single-uuid?
                single-code-signature?
                (= expected-exit (get-in run-step [:result :exit-code]))
                (=
                  p15-s23-b2-c17-gate-b-private-directory-permissions
                  (set
                    (java.nio.file.Files/getPosixFilePermissions
                      workspace
                      (into-array
                        java.nio.file.LinkOption
                        [java.nio.file.LinkOption/NOFOLLOW_LINKS]))))
                (every?
                  #(=
                    p15-s23-b2-c17-gate-b-nonexecutable-permissions
                    (set
                      (java.nio.file.Files/getPosixFilePermissions
                        (.resolve workspace %)
                        (into-array
                          java.nio.file.LinkOption
                          [java.nio.file.LinkOption/NOFOLLOW_LINKS]))))
                  ["program.c" "program.h" "program.o"])
                (=
                  p15-s23-b2-c17-gate-b-executable-permissions
                  (set
                    (java.nio.file.Files/getPosixFilePermissions
                      (.resolve workspace "program")
                      (into-array
                        java.nio.file.LinkOption
                        [java.nio.file.LinkOption/NOFOLLOW_LINKS])))))
      (p15-s23-c-backend-fail!
        "B14-ARTIFACT"
        source-path
        gate-a
        {:missing-fact :exact-c17-compile-link-inspection-closure}))
    state))
