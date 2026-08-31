(defn p15-s23-stage2-b3-llvm-authentic?
  ([artifact] false)
  ([artifact checked-core context]
   (try
     (= :passed
        (p15-s23-stage2-b3-llvm-verify!
         artifact checked-core context))
     (catch StackOverflowError _ false)
     (catch InterruptedException interrupted
       (.interrupt (Thread/currentThread))
       (throw interrupted))
     (catch AssertionError _ false)
     (catch LinkageError _ false)
     (catch Exception _ false))))

(defn p15-s23-stage2-b3-llvm-artifact-from-c11!
  ([c11-artifact checked-core context]
   (p15-s23-stage2-b3-llvm-artifact-from-c11!
    c11-artifact checked-core context {}))
  ([c11-artifact checked-core context options]
   (let [source-path (p15-s23-c11-ingress-source-path context)]
     (try
       (p15-s23-b3-llvm-linux-target-preflight!
        source-path c11-artifact context)
       (let [validated-options
             (p15-s23-b3-llvm-validated-options!
              source-path options)
             checked-core-sorted-policy
             (p15-s23-c11-carrier-sorted-policy checked-core)
             _
             (p15-s23-c11-mir-require-trusted-carrier!
              source-path :b3-c11-ingress c11-artifact
              checked-core-sorted-policy)
             c11-report
             (p15-s23-stage2-c11-mir-verification-report
              c11-artifact checked-core context)
             _ (when-not (= :passed (:status c11-report))
                 (p15-s23-b3-llvm-fail!
                  "B1-INPUT" source-path c11-artifact
                  {:missing-fact :fresh-c11-replay-pass}))]
         (p15-s23-b3-llvm-linux-build-from-c11!
          c11-artifact checked-core context validated-options))
       (catch StackOverflowError error
         (p15-s23-b3-llvm-fail!
          "B1-INPUT" source-path {}
          {:missing-fact :bounded-hostile-b3-ingress-host-stack}))
       (catch InterruptedException interrupted
         (.interrupt (Thread/currentThread))
         (throw interrupted))
       (catch AssertionError error
         (p15-s23-b3-llvm-contain-exception!
          source-path :contained-b3-constructor-assertion error))
       (catch LinkageError error
         (p15-s23-b3-llvm-contain-exception!
          source-path :contained-b3-constructor-linkage error))
       (catch clojure.lang.ExceptionInfo exception
         (p15-s23-b3-llvm-contain-exception!
          source-path :contained-unstructured-b3-diagnostic exception))
       (catch Exception exception
         (p15-s23-b3-llvm-contain-exception!
          source-path :contained-b3-host-failure exception))))))

(defn p15-s23-stage2-b3-llvm-source-artifact!
  ([source-path source-text]
   (p15-s23-stage2-b3-llvm-source-artifact!
    source-path source-text {}))
  ([source-path source-text options]
   (try
     (let [validated-options
           (p15-s23-b3-llvm-validated-options! source-path options)
           upstream-diagnostic-owner (Object.)
           [checked-core context]
           (binding [*p15-s23-c11-upstream-diagnostic-owner*
                     upstream-diagnostic-owner
                     *p15-s23-c11-mir-diagnostic-context*
                     {:requested-target :llvm-x86_64-linux}
                     *additional-bootstrap-targets*
                     stage2-runtime-derived-source-targets]
             (try
               (let [context
                     (p15-s23-stage2-gravity-checked-core-context
                      source-path source-text :llvm-x86_64-linux)]
                 [(p15-s23-stage2-gravity-checked-core-source-artifact
                   context)
                  context])
               (catch InterruptedException interrupted
                 (.interrupt (Thread/currentThread))
                 (throw interrupted))
              (catch clojure.lang.ExceptionInfo exception
                 (let [data
                       (p15-s23-backend-trusted-exception-data
                        exception 65536 128)]
                   (if (and
                        data
                        (p15-s23-c11-mir-owned-upstream-diagnostic? data))
                     (p15-s23-c11-mir-contain-checked-core-exception!
                      source-path :b3-source-checked-core-diagnostic exception)
                     (throw exception))))))
           c11-artifact
           (p15-s23-stage2-c11-mir-artifact checked-core context)]
       (p15-s23-stage2-b3-llvm-artifact-from-c11!
        c11-artifact checked-core context validated-options))
     (catch StackOverflowError error
       (p15-s23-b3-llvm-fail!
        "B1-INPUT" source-path {}
        {:missing-fact :bounded-hostile-source-host-stack}))
     (catch InterruptedException interrupted
       (.interrupt (Thread/currentThread))
       (throw interrupted))
     (catch AssertionError error
       (p15-s23-b3-llvm-contain-exception!
        source-path :contained-b3-source-assertion error))
     (catch LinkageError error
       (p15-s23-b3-llvm-contain-exception!
        source-path :contained-b3-source-linkage error))
     (catch clojure.lang.ExceptionInfo exception
       (p15-s23-b3-llvm-contain-exception!
        source-path :contained-unstructured-source-diagnostic exception))
     (catch Exception exception
       (p15-s23-b3-llvm-contain-exception!
        source-path :contained-source-host-failure exception)))))
