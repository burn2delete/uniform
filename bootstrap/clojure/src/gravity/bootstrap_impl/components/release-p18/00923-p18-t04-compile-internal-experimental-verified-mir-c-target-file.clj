

(defn- p18-t04-compile-internal-experimental-verified-mir-c-target-file!
  [request]
  (try
    (let [request (p18-t04-verified-mir-c-request! request)
          output-directory
          (p18-t04-verified-mir-c-output-directory!
           (:source-path request) (:output-path request))
          {:keys [source-path source-text source-snapshot-evidence]}
          (p18-t04-verified-mir-c-source-input! (:source-path request))]
      ;; The private Gate-B path executes the projector while the complete
      ;; bundle is still staged, then makes the exclusive rename its final
      ;; success-path effect and returns this precomputed route record.
      (p15-s23-stage2-b2-c17-gate-b-p18-t04-route-source-artifact!
       source-path source-text source-snapshot-evidence output-directory))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch java.nio.channels.ClosedByInterruptException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch java.io.InterruptedIOException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch clojure.lang.ExceptionInfo exception
      (throw exception))
    (catch Exception _
      (p15-s23-c-backend-fail!
       "C14-INPUT" (or (:source-path request) "<verified-mir-c-route>") {}
       {:missing-fact :contained-internal-verified-mir-c-route-host-failure
        :source-target :jvm :requested-target :c
        :bounded-reason :internal-route-host-failure}))))

(defn p18-t04-compile-experimental-verified-mir-c-target-file!
  "Governed public wrapper for the internal verified-MIR C candidate."
  [request]
  (let [request (p18-t04-verified-mir-c-public-authority! request)]
    (p18-t04-compile-internal-experimental-verified-mir-c-target-file!
     request)))