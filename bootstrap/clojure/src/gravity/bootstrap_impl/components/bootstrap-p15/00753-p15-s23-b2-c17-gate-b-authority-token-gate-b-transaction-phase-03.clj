(defn- __gravity_bootstrap_gate_b_transaction_phase_03 [state]
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
          dyld-ok?]} state
        unwind-ok? (and
                     object-load-section
                     executable-load-section
                     (re-find
                       #"(?s)sectname __compact_unwind\s+segname __LD"
                       object-load-section)
                     (re-find
                       #"(?s)sectname __unwind_info\s+segname __TEXT"
                       executable-load-section))
        single-main? (and
                       (string? executable-load-section)
                       (=
                         1
                         (count
                           (re-seq #"(?m)^\s+cmd LC_MAIN$" executable-load-section))))
        single-uuid? (and
                       (string? executable-load-section)
                       (=
                         1
                         (count
                           (re-seq #"(?m)^\s+cmd LC_UUID$" executable-load-section))))
        single-code-signature? (and
                                 (string? executable-load-section)
                                 (=
                                   1
                                   (count
                                     (re-seq
                                       #"(?m)^\s+cmd LC_CODE_SIGNATURE$"
                                       executable-load-section))))
        exact-snapshot? (fn [before after]
                          (and
                            (=
                              (select-keys
                                before
                                [:byte-count :content-hash :file-key-hash])
                              (select-keys
                                after
                                [:byte-count :content-hash :file-key-hash]))
                            (java.util.Arrays/equals (:bytes before) (:bytes after))))
        silent? (fn [step]
                  (and
                    (empty? (get-in step [:result :stdout :text]))
                    (empty? (get-in step [:result :stderr :text]))))]
    (assoc
      state
      'unwind-ok?
      unwind-ok?
      'single-main?
      single-main?
      'single-uuid?
      single-uuid?
      'single-code-signature?
      single-code-signature?
      'exact-snapshot?
      exact-snapshot?
      'silent?
      silent?)))
