(let [static-rebuild-token (nth __gravity_bootstrap_checked_core_authority_values 0)
      p15-s23-stage2-closed-checked-core-source-artifact-internal __gravity_bootstrap_checked_core_source_helper
      p15-s23-stage2-closed-checked-core-rebuild-internal __gravity_bootstrap_checked_core_rebuild_helper]
(defn p15-s23-stage2-closed-checked-core-verification-report
  [artifact context]
  (try
    (doseq [[carrier value] [[:legacy-checked-core artifact]
                             [:legacy-checked-core-context context]]]
      (let [validation
            (p15-s23-trusted-carrier-validation
             value :default-only
             p15-s23-closed-core-max-serialized-values
             p15-s23-closed-core-max-plan-depth
             p15-s23-closed-core-max-derived-nodes)]
        (when-not (= :passed (:status validation))
          (p15-s23-closed-core-fail!
           "C6-VERIFY" "<closed-core>" {}
           (assoc validation
                  :missing-fact :trusted-legacy-checked-core-carrier
                  :carrier carrier)))))
    (p15-s23-checked-core-bounded-context! context)
    (p15-s23-closed-core-bounded-value! "<closed-core>" artifact)
    (let [source-path
          (or (:source-path context)
              (get-in artifact [:provenance :actual-paths :source])
              "<closed-core>")]
      (try
        (p15-s23-stage2-closed-checked-core-verify!* artifact context)
        (catch InterruptedException interrupted
          (.interrupt (Thread/currentThread))
          (throw interrupted))
        (catch clojure.lang.ExceptionInfo ex
          (throw ex))
        (catch StackOverflowError error
          (p15-s23-closed-core-fail!
           "C6-VERIFY" source-path {:missing-fact :host-stack-containment}
           {:missing-fact :contained-public-verifier-host-failure
            :contained-host-error-hash
            (str "sha256:" (sha256-hex (.getName (class error))))}))
        (catch Exception error
          (p15-s23-closed-core-fail!
           "C6-VERIFY" source-path {:missing-fact :host-failure-containment}
           {:missing-fact :contained-public-verifier-host-failure
            :contained-host-error-hash
            (str "sha256:" (sha256-hex (.getName (class error))))
            :cause-message-hash
            (str "sha256:"
                 (sha256-hex (or (.getMessage error) "")))}))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch clojure.lang.ExceptionInfo ex
      (throw ex))
    (catch StackOverflowError error
      (p15-s23-closed-core-fail!
       "C6-VERIFY" "<closed-core>" {}
       {:missing-fact :contained-public-verifier-ingress-host-stack
        :contained-host-error-hash
        (str "sha256:" (sha256-hex (.getName (class error))))}))
    (catch Exception error
      (p15-s23-closed-core-fail!
       "C6-VERIFY" "<closed-core>" {}
       {:missing-fact :contained-public-verifier-ingress-host-failure
        :contained-host-error-hash
        (str "sha256:" (sha256-hex (.getName (class error))))
        :cause-message-hash
        (str "sha256:" (sha256-hex (or (.getMessage error) "")))}))))

(defn p15-s23-stage2-closed-checked-core-verify!
  [artifact context]
  (let [report
        (p15-s23-stage2-closed-checked-core-verification-report
         artifact context)]
    (when-not (= :passed (:status report))
      (p15-s23-closed-core-fail!
       "C6-VERIFY" "<closed-core>" {}
       {:missing-fact :checked-core-verification-report-status}))
    :passed))

(defn p15-s23-stage2-closed-checked-core-authentic?
  ([artifact]
   false)
  ([artifact context]
   (try
     (= :passed
        (p15-s23-stage2-closed-checked-core-verify! artifact context))
     (catch InterruptedException interrupted
       (.interrupt (Thread/currentThread))
       (throw interrupted))
     (catch StackOverflowError _ false)
     (catch Exception _ false)))))
