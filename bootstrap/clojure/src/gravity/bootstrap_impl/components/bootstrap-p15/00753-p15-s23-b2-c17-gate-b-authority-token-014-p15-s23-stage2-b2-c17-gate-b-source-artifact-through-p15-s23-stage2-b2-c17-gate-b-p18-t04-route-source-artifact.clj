(let [p15-s23-b2-c17-gate-b-authority-token (nth __gravity_bootstrap_lexical_values_131964 0)
      p15-s23-b2-c17-gate-b-tool-state (nth __gravity_bootstrap_lexical_values_131964 1)]
(defn p15-s23-stage2-b2-c17-gate-b-source-artifact!
  ([source-path source-text]
   (p15-s23-stage2-b2-c17-gate-b-source-artifact!
    source-path source-text {}))
  ([source-path source-text options]
   (try
     ;; Reject hostile options before reader/compiler reconstruction.
     (let [validated-options
           (p15-s23-b2-c17-gate-b-validated-options! source-path options)
           upstream-diagnostic-owner (Object.)
           [checked-core context]
           (binding
            [*p15-s23-c11-upstream-diagnostic-owner* upstream-diagnostic-owner
             *p15-s23-c11-mir-diagnostic-context* {:requested-target :c}
             *additional-bootstrap-targets* stage2-runtime-derived-source-targets]
             (try
               (let [context
                     (p15-s23-stage2-gravity-checked-core-context
                      source-path source-text :c)]
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
                   (if (and data
                            (p15-s23-c11-mir-owned-upstream-diagnostic? data))
                     (p15-s23-c11-mir-contain-checked-core-exception!
                      source-path :c17-gate-b-source-checked-core-diagnostic
                      exception)
                     (throw exception))))))
           c11 (p15-s23-stage2-c11-mir-artifact checked-core context)]
       (p15-s23-stage2-b2-c17-gate-b-artifact-from-c11!
        c11 checked-core context validated-options))
     (catch InterruptedException interrupted
       (.interrupt (Thread/currentThread))
       (throw interrupted))
     (catch StackOverflowError _
       (p15-s23-c-backend-fail!
        "B2-MANIFEST" source-path {}
        {:missing-fact :bounded-hostile-c17-gate-b-source-stack}))
     (catch clojure.lang.ExceptionInfo exception
       (let [data
             (p15-s23-backend-trusted-exception-data exception 65536 128)
             replayed
             (when (map? data)
               (p15-s23-stage2-reader-replayed-diagnostic
                source-path source-text))]
         (if (and data
                  (p15-s23-stage2-canonical-c2-diagnostic-authentic?
                   source-path source-text data replayed))
           (throw exception)
           (p15-s23-c-backend-contain-exception!
            source-path :contained-c17-gate-b-source-diagnostic exception))))
     (catch Exception exception
       (p15-s23-c-backend-contain-exception!
        source-path :contained-c17-gate-b-source-host-failure exception)))))

(defn- p15-s23-stage2-b2-c17-gate-b-source-artifact-projected!
  "Private success-projector path used by an enclosing transactional route.
  Projector execution occurs in private staging before exclusive publication."
  [projector-authority source-path source-text options success-projector]
  (p15-s23-b2-c17-gate-b-require-authority!
   projector-authority source-path :authenticated-c17-source-projector)
  (when-not (ifn? success-projector)
    (p15-s23-c-backend-fail!
     "B2-MANIFEST" source-path {}
     {:missing-fact :callable-authenticated-c17-success-projector}))
  (try
    (let [validated-options
          (p15-s23-b2-c17-gate-b-validated-options! source-path options)
          upstream-diagnostic-owner (Object.)
          [checked-core context]
          (binding
           [*p15-s23-c11-upstream-diagnostic-owner* upstream-diagnostic-owner
            *p15-s23-c11-mir-diagnostic-context* {:requested-target :c}
            *additional-bootstrap-targets* stage2-runtime-derived-source-targets]
            (try
              (let [context
                    (p15-s23-stage2-gravity-checked-core-context
                     source-path source-text :c)]
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
                  (if (and data
                           (p15-s23-c11-mir-owned-upstream-diagnostic? data))
                    (p15-s23-c11-mir-contain-checked-core-exception!
                     source-path :c17-projected-source-checked-core-diagnostic
                     exception)
                    (throw exception))))))
          c11 (p15-s23-stage2-c11-mir-artifact checked-core context)
          gate-a
          (p15-s23-stage2-b2-c17-artifact-from-c11!
           c11 checked-core context)]
      (p15-s23-stage2-b2-c17-gate-b-artifact!
       gate-a checked-core context validated-options
       p15-s23-b2-c17-gate-b-authority-token success-projector))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch java.nio.channels.ClosedByInterruptException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch java.io.InterruptedIOException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch StackOverflowError _
      (p15-s23-c-backend-fail!
       "B2-MANIFEST" source-path {}
       {:missing-fact :bounded-hostile-c17-projected-source-stack}))
    (catch clojure.lang.ExceptionInfo exception
      (let [data
            (p15-s23-backend-trusted-exception-data exception 65536 128)
            replayed
            (when (map? data)
              (p15-s23-stage2-reader-replayed-diagnostic
               source-path source-text))]
        (if (and data
                 (p15-s23-stage2-canonical-c2-diagnostic-authentic?
                  source-path source-text data replayed))
          (throw exception)
          (p15-s23-c-backend-contain-exception!
           source-path :contained-c17-projected-source-diagnostic
           exception))))
    (catch Exception exception
      (p15-s23-c-backend-contain-exception!
       source-path :contained-c17-projected-source-host-failure exception))))

(defn- p15-s23-stage2-b2-c17-gate-b-p18-t04-route-source-artifact!
  "Sealed Gate-C entry: callers provide data, never executable projectors."
  [source-path source-text source-snapshot-evidence output-directory]
  (p15-s23-stage2-b2-c17-gate-b-source-artifact-projected!
   p15-s23-b2-c17-gate-b-authority-token
   source-path source-text {:output-directory output-directory}
   (fn [finalized-gate-b]
     (p18-t04-experimental-verified-mir-c-route-record
      source-path source-text source-snapshot-evidence
      output-directory finalized-gate-b)))))
