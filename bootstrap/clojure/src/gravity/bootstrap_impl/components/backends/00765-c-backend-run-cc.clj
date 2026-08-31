

(defn c-backend-run-cc!
  ([c-source-path executable-path source-path target]
   (c-backend-run-cc! c-source-path executable-path source-path target false))
  ([c-source-path executable-path source-path target execute?]
   (let [c-source-command-path
         (.getAbsolutePath (java.io.File. (str c-source-path)))
         executable-command-path
         (.getAbsolutePath (java.io.File. (str executable-path)))
         staging (c-backend-private-staging-directory! source-path target)
         primary-failure (atom nil)
         cleanup-failure (atom nil)
         compile-result (atom nil)
         runtime-result (atom nil)]
     (try
       (reset! compile-result
               (c-backend-run-process!
                staging
                ["/usr/bin/cc" "-std=c11" "-Wall" "-Werror"
                 c-source-command-path "-o" executable-command-path]
                source-path target :compile))
       (let [result @compile-result]
         (when-not (zero? (:exit result))
           (c-backend-fail! "B2-DIALECT"
                            "host C compiler rejected generated C11 source"
                            source-path target nil
                            {:compiler "/usr/bin/cc"
                             :compile-result result
                             :missing-fact :c11-compiler-acceptance})))
       (when execute?
         (reset! runtime-result
                (c-backend-run-process!
                 staging [executable-command-path]
                 source-path target :runtime))
         (let [result @runtime-result]
           (when-not (zero? (:exit result))
             (c-backend-fail!
              "B2-DIALECT"
              "generated C executable failed at runtime"
              source-path target nil
              {:compiler "/usr/bin/cc"
               :runtime-result result
               :missing-fact :c-runtime-execution}))))
       (cond-> @compile-result
         execute?
         (assoc :compile-out (:out @compile-result)
                :compile-err (:err @compile-result)
                :runtime-out (:out @runtime-result)
                :runtime-err (:err @runtime-result)
                :runtime-exit (:exit @runtime-result)))
       (catch clojure.lang.ExceptionInfo ex
         (reset! primary-failure ex)
         (throw ex))
       (catch InterruptedException interrupted
         (reset! primary-failure interrupted)
         (.interrupt (Thread/currentThread))
         (throw interrupted))
       (catch Error fatal
         ;; A fatal compiler/pump error remains the primary identity while
         ;; staging cleanup failures are attached as suppressed evidence.
         (reset! primary-failure fatal)
         (throw fatal))
       (catch Exception ex
         (reset! primary-failure ex)
         (c-backend-fail! "B2-DIALECT"
                          "host C compiler is unavailable"
                          source-path target nil
                          {:compiler "/usr/bin/cc"
                           :cause-message (.getMessage ex)
                           :missing-fact :c11-compiler}))
       (finally
         (try
           (c-backend-delete-private-staging!
            staging source-path target)
           (catch Throwable cleanup
             (reset! cleanup-failure cleanup)
             (when-let [error @primary-failure]
               (.addSuppressed ^Throwable error ^Throwable cleanup))))))
     (when-let [error @cleanup-failure]
       (when-not @primary-failure
         (throw ^Throwable error)))
     (if-let [error @primary-failure]
       (throw ^Throwable error)
       (cond-> @compile-result
         execute?
         (assoc :compile-out (:out @compile-result)
                :compile-err (:err @compile-result)
                :runtime-out (:out @runtime-result)
                :runtime-err (:err @runtime-result)
                :runtime-exit (:exit @runtime-result)))))))