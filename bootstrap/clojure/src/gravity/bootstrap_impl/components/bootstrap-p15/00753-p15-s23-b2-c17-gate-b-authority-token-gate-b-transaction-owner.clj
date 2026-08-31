(defn- p15-s23-b2-c17-gate-b-toolchain-transaction! [candidate
                                                     gate-a
                                                     source-path
                                                     publication-intent?]
  (p15-s23-b2-c17-gate-b-require-authority!
    candidate
    source-path
    :execute-authenticated-c17-gate-b)
  (let [host-runtime (p15-s23-b2-c17-gate-b-host-runtime-preflight!
                       candidate
                       source-path)
        workspace (java.nio.file.Files/createTempDirectory
                    "gravity-b2-c17-gate-b-"
                    (make-array java.nio.file.attribute.FileAttribute 0))
        primary-failure (atom nil)]
    (try
      (java.nio.file.Files/setPosixFilePermissions
        workspace
        p15-s23-b2-c17-gate-b-private-directory-permissions)
      (let [state (hash-map
                    'candidate
                    candidate
                    'gate-a
                    gate-a
                    'source-path
                    source-path
                    'publication-intent?
                    publication-intent?
                    'host-runtime
                    host-runtime
                    'workspace
                    workspace
                    'primary-failure
                    primary-failure)
            state (__gravity_bootstrap_gate_b_transaction_phase_01 state)
            state (__gravity_bootstrap_gate_b_transaction_phase_02 state)
            state (__gravity_bootstrap_gate_b_transaction_phase_03 state)
            state (__gravity_bootstrap_gate_b_transaction_validation state)]
        (__gravity_bootstrap_gate_b_transaction_result state))
      (catch
        Throwable
        error
        (reset! primary-failure error)
        (p15-s23-b2-c17-gate-b-restore-interrupt! error)
        (throw error))
      (finally
        (try
          (p15-s23-b2-c17-gate-b-delete-tree! candidate workspace source-path)
          (catch
            Throwable
            cleanup
            (if-let [error @primary-failure]
              (do
                (p15-s23-b2-c17-gate-b-restore-interrupt! error)
                (p15-s23-b2-c17-gate-b-restore-interrupt! cleanup)
                (cond
                  (instance? Error error) (.addSuppressed error cleanup)
                  (instance? Error cleanup) (do
                                              (.addSuppressed cleanup error)
                                              (throw cleanup))
                  (p15-s23-b2-c17-gate-b-interrupt-like? error) (.addSuppressed
                                                                  error
                                                                  cleanup)
                  (p15-s23-b2-c17-gate-b-interrupt-like? cleanup) (do
                                                                    (.addSuppressed
                                                                      cleanup
                                                                      error)
                                                                    (throw cleanup))
                  :else (.addSuppressed error cleanup)))
              (do
                (p15-s23-b2-c17-gate-b-restore-interrupt! cleanup)
                (throw cleanup)))))))))
