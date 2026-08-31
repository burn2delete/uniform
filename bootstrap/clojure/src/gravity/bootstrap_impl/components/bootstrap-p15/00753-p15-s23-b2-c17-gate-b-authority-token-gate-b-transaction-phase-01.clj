(defn- __gravity_bootstrap_gate_b_transaction_phase_01 [state]
  (let [{:syms
         [candidate
          gate-a
          source-path
          publication-intent?
          host-runtime
          workspace
          primary-failure]} state
        workspace-binding (p15-s23-b2-c17-gate-b-workspace-binding!
                            candidate
                            workspace
                            source-path)
        source-text (get-in gate-a [:source-file :content])
        header-text (get-in gate-a [:header-file :content])
        expected-exit (:expected-exit-code gate-a)
        source-initial (p15-s23-b2-c17-gate-b-write-text!
                         candidate
                         workspace
                         "program.c"
                         source-text
                         source-path)
        header-initial (p15-s23-b2-c17-gate-b-write-text!
                         candidate
                         workspace
                         "program.h"
                         header-text
                         source-path)
        discovery (p15-s23-b2-c17-gate-b-toolchain-discovery!
                    candidate
                    workspace
                    source-path
                    workspace-binding)
        run (fn [step command exit diagnostic]
              (when-not (=
                          workspace-binding
                          (p15-s23-b2-c17-gate-b-workspace-binding!
                            candidate
                            workspace
                            source-path))
                (p15-s23-c-backend-fail!
                  "B2-MANIFEST"
                  source-path
                  gate-a
                  {:missing-fact :stable-c17-workspace-before-tool-step,
                   :tool-step step}))
              (let [result (p15-s23-b2-c17-gate-b-run-step!
                             candidate
                             workspace
                             source-path
                             step
                             command
                             exit
                             diagnostic)]
                (when-not (=
                            workspace-binding
                            (p15-s23-b2-c17-gate-b-workspace-binding!
                              candidate
                              workspace
                              source-path))
                  (p15-s23-c-backend-fail!
                    "B2-MANIFEST"
                    source-path
                    gate-a
                    {:missing-fact :stable-c17-workspace-after-tool-step,
                     :tool-step step}))
                result))
        target p15-s23-b2-c17-gate-b-target-triple
        clang p15-s23-b2-c17-gate-b-clang-path
        sdk p15-s23-b2-c17-gate-b-sdk-path
        ld p15-s23-b2-c17-gate-b-ld-path
        otool p15-s23-b2-c17-gate-b-otool-real-path
        flags p15-s23-b2-c17-gate-b-c-flags
        syntax-command (vec
                         (concat
                           [clang "-target" target "-isysroot" sdk]
                           flags
                           ["-fsyntax-only" "program.c"]))
        compile-command (vec
                          (concat
                            [clang "-target" target "-isysroot" sdk]
                            flags
                            ["-O0"
                             "-fPIC"
                             "-mcmodel=small"
                             "-mcpu=generic"
                             "-Xclang"
                             "-target-feature"
                             "-Xclang"
                             "+v8a"
                             "-Xclang"
                             "-target-feature"
                             "-Xclang"
                             "+fp-armv8"
                             "-Xclang"
                             "-target-feature"
                             "-Xclang"
                             "+neon"
                             "-c"
                             "program.c"
                             "-o"
                             "program.o"]))
        link-command [clang
                      "-target"
                      target
                      "-isysroot"
                      sdk
                      "-Wl,-reproducible"
                      (str "-fuse-ld=" ld)
                      "program.o"
                      "-o"
                      "program"]
        syntax (run :c17-syntax syntax-command 0 "B2-DIALECT")
        compile (run :c17-compile compile-command 0 "B2-UB")
        _ (java.nio.file.Files/setPosixFilePermissions
            (.resolve workspace "program.o")
            p15-s23-b2-c17-gate-b-nonexecutable-permissions)
        object-initial (p15-s23-b2-c17-gate-b-file-snapshot!
                         candidate
                         workspace
                         (.resolve workspace "program.o")
                         source-path
                         :initial-c17-object
                         (* 8 1024 1024))
        link (run :c17-link link-command 0 "B2-ABI")
        _ (java.nio.file.Files/setPosixFilePermissions
            (.resolve workspace "program")
            p15-s23-b2-c17-gate-b-executable-permissions)
        executable-initial (p15-s23-b2-c17-gate-b-file-snapshot!
                             candidate
                             workspace
                             (.resolve workspace "program")
                             source-path
                             :initial-c17-executable
                             (* 8 1024 1024))
        file-step (run :file-format ["/usr/bin/file" "program.o" "program"] 0 "B2-ABI")
        header-step (run
                      :mach-o-header
                      [otool "-hv" "program.o" "program"]
                      0
                      "B2-ABI")]
    (assoc
      state
      'workspace-binding
      workspace-binding
      'source-text
      source-text
      'header-text
      header-text
      'expected-exit
      expected-exit
      'source-initial
      source-initial
      'header-initial
      header-initial
      'discovery
      discovery
      'run
      run
      'target
      target
      'clang
      clang
      'sdk
      sdk
      'ld
      ld
      'otool
      otool
      'flags
      flags
      'syntax-command
      syntax-command
      'compile-command
      compile-command
      'link-command
      link-command
      'syntax
      syntax
      'compile
      compile
      '_
      _
      'object-initial
      object-initial
      'link
      link
      '_
      _
      'executable-initial
      executable-initial
      'file-step
      file-step
      'header-step
      header-step)))
