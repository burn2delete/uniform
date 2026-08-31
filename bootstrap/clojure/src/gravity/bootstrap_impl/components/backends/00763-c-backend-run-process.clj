

(defn- c-backend-run-process!
  [staging command source-path target role]
  (let [root (:path staging)
        builder (ProcessBuilder. ^java.util.List command)
        _ (.directory builder (.toFile root))
        environment (.environment builder)
        _ (.clear environment)
        _ (.put environment "PATH" "/usr/bin:/bin:/usr/sbin:/sbin")
        _ (.put environment "LC_ALL" "C")
        _ (.put environment "LANG" "C")
        _ (.put environment "HOME" (.toString root))
        _ (.put environment "TMPDIR" (.toString root))
        _ (.redirectErrorStream builder false)
        process-holder (atom nil)
        pumps-holder (atom [])
        primary-failure (atom nil)]
    (try
      (let [process (*c-backend-process-start-fn* builder)
            _ (reset! process-holder process)
            _ (.close (.getOutputStream process))
            stdout-pump (c-backend-start-output-pump!
                         (.getInputStream process) :stdout)
            _ (reset! pumps-holder [stdout-pump])
            stderr-pump (c-backend-start-output-pump!
                         (.getErrorStream process) :stderr)
            pumps [stdout-pump stderr-pump]
            _ (reset! pumps-holder pumps)
            wait-result (c-backend-await-process! process pumps)]
        (when (= :timeout (:status wait-result))
          (let [termination
                (c-backend-terminate-process-tree!
                 process source-path target)]
            (c-backend-fail!
             "B2-DIALECT" "C backend process exceeded its timeout"
             source-path target nil
             {:missing-fact :c-backend-process-timeout
              :role role
              :timeout-ms *c-backend-process-timeout-ms*
              :timed-out? true
              :termination termination})))
        (when (= :output-failure (:status wait-result))
          (c-backend-fail-output-pump!
           process (:failure wait-result) source-path target role))
        ;; A pump can publish overflow after await's failure check but before
        ;; waitFor observes that the pipe-closing child has exited.  Recheck
        ;; immediately on the :finished edge and route the result through the
        ;; same termination evidence path before the normal finisher can emit
        ;; a diagnostic without containment evidence.
        (when (= :finished (:status wait-result))
          ;; The child may have exited before a finite buffered stream was
          ;; fully drained.  Give both bounded pumps an EOF join before the
          ;; recheck so a post-exit overflow cannot fall through to the normal
          ;; result finisher.
          (doseq [pump pumps]
            (.join ^Thread (:thread pump) 2000))
          (when-let [failure (some c-backend-output-pump-failure pumps)]
            (c-backend-fail-output-pump!
             process failure source-path target role)))
        (let [descendants (c-backend-process-descendants process)]
          (when (or (:overflow? descendants)
                    (seq (:handles descendants)))
            (let [termination
                  (c-backend-terminate-process-tree!
                   process source-path target)]
              (c-backend-fail!
               "B2-DIALECT" "C backend process left descendants behind"
               source-path target nil
               {:missing-fact :c-backend-process-descendants
                :role role
                :snapshot-overflow? (:overflow? descendants)
                :termination termination}))))
        (let [stdout (c-backend-finish-output-pump!
                      stdout-pump source-path target)
              stderr (c-backend-finish-output-pump!
                      stderr-pump source-path target)
              failure
              (some (fn [outcome]
                      (when (or (:limit-exceeded? outcome)
                                (:decode-error outcome)
                                (:fatal-error outcome)
                                (:read-error outcome))
                        outcome))
                    [stdout stderr])]
          ;; Finish both pumps before diagnosing either one.  Any late outcome
          ;; failure still passes through process termination so containment
          ;; evidence is never omitted.
          (when failure
            (c-backend-fail-output-pump!
             process failure source-path target role))
          {:exit (.exitValue process)
           :out (:text stdout)
           :err (:text stderr)
           :stdout-byte-count (:byte-count stdout)
           :stderr-byte-count (:byte-count stderr)
           :stdout-total-byte-count (:total-byte-count stdout)
           :stderr-total-byte-count (:total-byte-count stderr)
           :stdout-hash (:hash stdout)
           :stderr-hash (:hash stderr)
           :finished? true
           :timed-out? false
           :role role}))
      (catch InterruptedException interrupted
        (reset! primary-failure interrupted)
        (when-let [process @process-holder]
          (try
            (c-backend-terminate-process-tree!
             process source-path target)
            (catch Throwable cleanup
              (.addSuppressed ^Throwable interrupted ^Throwable cleanup))))
        (.interrupt (Thread/currentThread))
        ;; Preserve the exact main-thread InterruptedException.  Cleanup
        ;; evidence is attached as suppressed rather than replacing it with a
        ;; diagnostic wrapper.
        (throw interrupted))
      (catch Error fatal
        ;; OOME and ThreadDeath are fatal control flow, not ordinary process
        ;; failures.  The finally block may add bounded cleanup evidence, but
        ;; it must never replace the original object.
        (reset! primary-failure fatal)
        (throw fatal))
      (catch clojure.lang.ExceptionInfo ex
        (reset! primary-failure ex)
        (throw ex))
      (catch Exception ex
        (reset! primary-failure ex)
        (try
          (c-backend-fail!
           "B2-DIALECT" "C backend process could not be started"
           source-path target nil
           {:missing-fact :c-backend-process-start
            :role role
            :cause-message (.getMessage ex)})
          (catch clojure.lang.ExceptionInfo diagnostic
            (.addSuppressed ^Throwable diagnostic ^Throwable ex)
            (reset! primary-failure diagnostic)
            (throw diagnostic))))
      (finally
        (let [cleanup-failure (atom nil)]
          (when-let [process @process-holder]
            (when (.isAlive process)
              (try
                (c-backend-terminate-process-tree!
                 process source-path target)
                (catch Throwable cleanup
                  (reset! cleanup-failure cleanup)))))
          ;; Process input/error streams are pump-owned.  Close and bounded-
          ;; join both threads on success, timeout, overflow, interruption,
          ;; and every diagnostic path.  A cleanup failure never replaces an
          ;; active primary failure.
          (doseq [pump @pumps-holder]
            (when-let [cleanup (c-backend-clean-output-pump! pump)]
              (if-let [existing @cleanup-failure]
                (.addSuppressed ^Throwable existing ^Throwable cleanup)
                (reset! cleanup-failure cleanup))))
          (when-let [cleanup @cleanup-failure]
            (if-let [error @primary-failure]
              (.addSuppressed ^Throwable error ^Throwable cleanup)
              (throw cleanup))))))))