(defn-
 semantic-llvm-legacy-toolchain-run!
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
    [object-format-ok?
     executable-format-ok?
     header-ok?
     dyld-ok?
     libsystem-ok?
     build-version-ok?
     unwind-metadata-ok?
     uuid-ok?
     code-signature-ok?
     entrypoint-ok?
     compile-and-link-silent?]}
   state
   run-step
   (p15-s23-b3-llvm-run-step!
    candidate
    workspace
    source-path
    :run
    ["./program"]
    #{(:expected-exit-code lowering)})
   observed-exit
   (get-in run-step [:result :exit-code])
   stdout-count
   (get-in run-step [:result :stdout :total-byte-count])
   stderr-count
   (get-in run-step [:result :stderr :total-byte-count])]
  (do
   (when-not
    compile-and-link-silent?
    (p15-s23-b3-llvm-fail!
     "B3-PASS"
     source-path
     {}
     {:missing-fact :silent-llvm-codegen-and-link}))
   (when-not
    (and object-format-ok? executable-format-ok? header-ok?)
    (p15-s23-b3-llvm-fail!
     "B3-ABI"
     source-path
     {}
     {:missing-fact :arm64-mach-o-object-and-executable,
      :observed-format :not-matched,
      :observed-architecture :not-matched}))
   (when-not
    (and
     dyld-ok?
     libsystem-ok?
     build-version-ok?
     unwind-metadata-ok?
     uuid-ok?
     code-signature-ok?
     entrypoint-ok?)
    (p15-s23-b3-llvm-fail!
     "B3-RUNTIME"
     source-path
     {}
     {:missing-fact
      :delegated-darwin-dyld-libsystem-build-version-providers}))
   (when-not
    (and
     (= (:expected-exit-code lowering) observed-exit)
     (zero? stdout-count)
     (zero? stderr-count))
    (p15-s23-b3-llvm-fail!
     "B14-DIFFERENTIAL"
     source-path
     {}
     {:missing-fact :exact-reference-process-result,
      :expected-exit-code (:expected-exit-code lowering),
      :exit-code observed-exit,
      :stdout-byte-count stdout-count,
      :stderr-byte-count stderr-count}))
   (assoc
    state
    :run-step
    run-step
    :observed-exit
    observed-exit
    :stdout-count
    stdout-count
    :stderr-count
    stderr-count))))
