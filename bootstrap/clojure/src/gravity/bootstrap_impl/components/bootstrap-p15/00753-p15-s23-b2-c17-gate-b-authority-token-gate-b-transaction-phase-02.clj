(defn- __gravity_bootstrap_gate_b_transaction_phase_02 [state]
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
          header-step]} state
        load-step (run
                    :mach-o-load-commands
                    [otool "-l" "program.o" "program"]
                    0
                    "B2-ABI")
        provider-step (run :runtime-providers [otool "-L" "program"] 0 "B2-RUNTIME")
        run-step (run :run ["./program"] expected-exit "B14-DIFFERENTIAL")
        physical-final (p15-s23-b2-c17-gate-b-pinned-physical-toolchain!
                         candidate
                         source-path)
        _ (when-not (= (:physical-record discovery) physical-final)
            (p15-s23-c-backend-fail!
              "B13-HASH"
              source-path
              gate-a
              {:missing-fact :stable-c17-toolchain-through-transaction}))
        source-final (p15-s23-b2-c17-gate-b-file-snapshot!
                       candidate
                       workspace
                       (.resolve workspace "program.c")
                       source-path
                       :final-c17-source
                       (* 8 1024 1024))
        header-final (p15-s23-b2-c17-gate-b-file-snapshot!
                       candidate
                       workspace
                       (.resolve workspace "program.h")
                       source-path
                       :final-c17-header
                       (* 8 1024 1024))
        object-final (p15-s23-b2-c17-gate-b-file-snapshot!
                       candidate
                       workspace
                       (.resolve workspace "program.o")
                       source-path
                       :final-c17-object
                       (* 8 1024 1024))
        executable-final (p15-s23-b2-c17-gate-b-file-snapshot!
                           candidate
                           workspace
                           (.resolve workspace "program")
                           source-path
                           :final-c17-executable
                           (* 8 1024 1024))
        inventory (p15-s23-b2-c17-gate-b-capped-directory-inventory!
                    candidate
                    workspace
                    source-path
                    4)
        final-workspace-binding (p15-s23-b2-c17-gate-b-workspace-binding!
                                  candidate
                                  workspace
                                  source-path)
        file-text (get-in file-step [:result :stdout :text])
        header-text-output (get-in header-step [:result :stdout :text])
        load-text (get-in load-step [:result :stdout :text])
        provider-text (get-in provider-step [:result :stdout :text])
        header-parts (re-matches
                       #"(?s)\Aprogram\.o:\n(.*?)\nprogram:\n(.*)\z"
                       header-text-output)
        load-parts (re-matches #"(?s)\Aprogram\.o:\n(.*?)\nprogram:\n(.*)\z" load-text)
        object-header-section (nth header-parts 1 nil)
        executable-header-section (nth header-parts 2 nil)
        object-load-section (nth load-parts 1 nil)
        executable-load-section (nth load-parts 2 nil)
        object-header-ok? (when object-header-section
                            (re-matches
                              #"(?s)\AMach header\n\s*magic\s+cputype\s+cpusubtype\s+caps\s+filetype\s+ncmds\s+sizeofcmds\s+flags\nMH_MAGIC_64\s+ARM64\s+ALL\s+0x00\s+OBJECT\s+4\s+360\s+SUBSECTIONS_VIA_SYMBOLS\s*\z"
                              object-header-section))
        executable-header-ok? (when executable-header-section
                                (re-matches
                                  #"(?s)\AMach header\n\s*magic\s+cputype\s+cpusubtype\s+caps\s+filetype\s+ncmds\s+sizeofcmds\s+flags\nMH_MAGIC_64\s+ARM64\s+ALL\s+0x00\s+EXECUTE\s+16\s+744\s+NOUNDEFS\s+DYLDLINK\s+TWOLEVEL\s+PIE\s*\z"
                                  executable-header-section))
        commands (fn [section]
                   (when section
                     (mapv second (re-seq #"(?m)^\s+cmd (LC_[A-Z0-9_]+)$" section))))
        all-commands (fn [section]
                       (when section
                         (mapv second (re-seq #"(?m)^\s+cmd\s+(\S+)$" section))))
        command-labels (fn [section]
                         (when section
                           (mapv
                             (comp parse-long second)
                             (re-seq #"(?m)^Load command ([0-9]{1,5})$" section))))
        object-commands (commands object-load-section)
        executable-commands (commands executable-load-section)
        forbidden-load-command? (boolean
                                  (re-find
                                    #"(?m)^\s+cmd LC_(RPATH|LOAD_WEAK_DYLIB|REEXPORT_DYLIB|LOAD_UPWARD_DYLIB)$"
                                    load-text))
        provider-ok? (boolean
                       (re-matches
                         #"\Aprogram:\n\s+/usr/lib/libSystem\.B\.dylib \(compatibility version 1\.0\.0, current version 1356\.0\.0\)\n?\z"
                         provider-text))
        build-version-ok? (boolean
                            (and
                              object-load-section
                              executable-load-section
                              (re-find
                                #"(?s)cmd LC_BUILD_VERSION\s+cmdsize 24\s+platform 1\s+minos 14\.0\s+sdk 26\.5\s+ntools 0"
                                object-load-section)
                              (re-find
                                #"(?s)cmd LC_BUILD_VERSION\s+cmdsize 32\s+platform 1\s+minos 14\.0\s+sdk 26\.5\s+ntools 1\s+tool 3\s+version 1267\.0"
                                executable-load-section)))
        libsystem-ok? (boolean
                        (and
                          provider-ok?
                          executable-load-section
                          (re-find
                            #"(?s)cmd LC_LOAD_DYLIB\s+cmdsize 56\s+name /usr/lib/libSystem\.B\.dylib \(offset 24\)\s+time stamp 2 .+?\s+current version 1356\.0\.0\s+compatibility version 1\.0\.0"
                            executable-load-section)))
        dyld-ok? (and
                   object-load-section
                   executable-load-section
                   (not (str/includes? object-load-section "LC_LOAD_DYLINKER"))
                   (=
                     1
                     (count
                       (re-seq
                         #"(?m)^\s+name /usr/lib/dyld \(offset [0-9]+\)$"
                         executable-load-section))))]
    (assoc
      state
      'load-step
      load-step
      'provider-step
      provider-step
      'run-step
      run-step
      'physical-final
      physical-final
      '_
      _
      'source-final
      source-final
      'header-final
      header-final
      'object-final
      object-final
      'executable-final
      executable-final
      'inventory
      inventory
      'final-workspace-binding
      final-workspace-binding
      'file-text
      file-text
      'header-text-output
      header-text-output
      'load-text
      load-text
      'provider-text
      provider-text
      'header-parts
      header-parts
      'load-parts
      load-parts
      'object-header-section
      object-header-section
      'executable-header-section
      executable-header-section
      'object-load-section
      object-load-section
      'executable-load-section
      executable-load-section
      'object-header-ok?
      object-header-ok?
      'executable-header-ok?
      executable-header-ok?
      'commands
      commands
      'all-commands
      all-commands
      'command-labels
      command-labels
      'object-commands
      object-commands
      'executable-commands
      executable-commands
      'forbidden-load-command?
      forbidden-load-command?
      'provider-ok?
      provider-ok?
      'build-version-ok?
      build-version-ok?
      'libsystem-ok?
      libsystem-ok?
      'dyld-ok?
      dyld-ok?)))
