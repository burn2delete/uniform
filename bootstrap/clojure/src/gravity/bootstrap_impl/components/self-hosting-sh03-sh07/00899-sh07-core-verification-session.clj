

(defn sh07-core-verification-session
  "Build one fresh expected SH-07 artifact for development-time replay.

  The returned immutable session may be used to check several candidate
  artifacts with `sh07-core-artifact-verification-against-expected`.  This is
  intentionally separate from `sh07-core-artifact-verification`, whose fresh
  replay remains the authoritative admission verifier.  A session is bound to
  the exact upstream artifact and expected artifact identity; stale or
  substituted candidates fail closed without rebuilding the expected product.
  The explicit invocation is forwarded unchanged to both fresh construction
  and upstream verification; it does not turn development replay into admission."
  ([resolution-artifact] (sh07-core-verification-session resolution-artifact nil))
  ([resolution-artifact invocation]
  (let [upstream-snapshot
        (sh07-core-verification-session-upstream-snapshot resolution-artifact)
        source-path (:source-path upstream-snapshot)
        expected
        (try
          (if (some? invocation)
            (sh07-core-from-resolution-artifact resolution-artifact invocation)
            (sh07-core-from-resolution-artifact resolution-artifact))
          (catch InterruptedException interrupted
            (.interrupt (Thread/currentThread))
            (throw interrupted))
          (catch Throwable _ nil))
        upstream-verification
        (try
          (if (some? invocation)
            (sh06-resolution-artifact-verification resolution-artifact ::legacy invocation)
            (sh06-resolution-artifact-verification resolution-artifact))
          (catch InterruptedException interrupted
            (.interrupt (Thread/currentThread))
            (throw interrupted))
          (catch Throwable _
            {:artifact :gravity/sh06-resolution-artifact-verification
             :status :failed
             :failed-checks [:contained-failure]}))
        status
        (if (and expected
                 (= :passed (:status upstream-verification)))
          :ready
          :failed)
        failure-reason
        (cond
          (nil? expected) :expected-build-failed
          (not= :passed (:status upstream-verification))
          :upstream-verification-failed
          :else nil)]
    {:artifact sh07-core-verification-session-kind
     :schema-version sh07-core-verification-session-schema-version
     :status status
     :source-path source-path
     :resolution-artifact-id (:artifact-id resolution-artifact)
     :upstream-snapshot upstream-snapshot
     :expected-artifact-id (:artifact-id expected)
     :expected-integrity
     (when expected
       (sh07-core-verification-session-expected-integrity expected))
     :expected expected
     :upstream-verification upstream-verification
     :session-binding
     (when (and expected
                (= :passed (:status upstream-verification)))
       (sh07-core-verification-session-binding
        upstream-snapshot expected upstream-verification))
     :failure-reason failure-reason})))

(defn- sh07-core-verification-session-valid?
  [artifact session]
  (try
    (let [expected (:expected session)
          resolution-artifact (:sh06-resolution-artifact artifact)
          upstream-snapshot (:upstream-snapshot session)
          candidate-upstream-snapshot
          (when (map? resolution-artifact)
            (sh07-core-verification-session-upstream-snapshot
             resolution-artifact))
          session-resolution-id (:resolution-artifact-id session)
          expected-resolution-id
          (get-in expected [:sh06-resolution-artifact :artifact-id])
          expected-integrity
          (when (map? expected)
            (sh07-core-verification-session-expected-integrity expected))]
      (and (= sh07-core-verification-session-kind (:artifact session))
           (= sh07-core-verification-session-schema-version
              (:schema-version session))
           (= :ready (:status session))
           (map? expected)
           (= (:expected-artifact-id session) (:artifact-id expected))
           (= (:expected-integrity session) expected-integrity)
           (= session-resolution-id expected-resolution-id)
           (= session-resolution-id (:artifact-id resolution-artifact))
           (map? upstream-snapshot)
           (= upstream-snapshot candidate-upstream-snapshot)
           (= :passed (get-in session [:upstream-verification :status]))
           (= (:session-binding session)
              (sh07-core-verification-session-binding
               upstream-snapshot expected (:upstream-verification session)
               expected-integrity))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn sh07-core-source-artifact
  [source-path source-text]
  (sh07-core-from-resolution-artifact
   (sh06-resolution-source-artifact source-path source-text)))

(defn sh07-core-file-artifact
  [source-path]
  (let [source-text
        (try
          (slurp source-path)
          (catch Exception _
            (throw
             (ex-info
              "SH-07 could not read the requested source"
              {:artifact :gravity/sh07-core-diagnostic
               :rule "C6-VERIFY"
               :severity :error
               :stage :core-lowering
               :syntax-id nil
               :form-id nil
               :core-node-id nil
               :source-span {:source source-path}
               :generated-origin-chain []
               :lowering-rule :sh07-b47-function-call-recursion-products
               :facts {:reason :source-read-failed
                       :fail-closed true}
               :remediation
               "Provide a readable source file through the declared artifact source boundary."
               :diagnostic-id-request
               (reader-canonical-hash
                {:domain :gravity/sh07-source-read-diagnostic-v1
                 :source-path source-path
                 :reason :source-read-failed})}))))]
    (sh07-core-source-artifact source-path source-text)))

(declare compiler-c5-resolution-source-artifact)

(defn sh07-public-core-file-artifact
  "Builds the authenticated SH-07 core artifact through the strict public
  source boundary. The Clojure bootstrap still owns byte loading, UTF-8
  decoding, plan execution, digest resolution, and final artifact assembly.
  {:reader :development-current} selects the unadmitted current reader."
  ([source-path] (sh07-public-core-file-artifact source-path {}))
  ([source-path options]
   (when-not (or (= {} options) (= {:reader :development-current} options))
     (throw (ex-info "SH-07 source options must be {} or {:reader :development-current}"
                     {:id "SH07-API-OPTIONS" :stage :read-source :severity :error})))
   (let [text (try (read-gravity-source-text source-path)
                  (catch clojure.lang.ExceptionInfo ex (throw ex))
                  (catch Exception _
                    (throw (ex-info "SH-07 could not read the requested public source"
                            {:artifact :gravity/sh07-core-diagnostic :rule "C6-VERIFY" :severity :error
                             :stage :core-lowering :syntax-id nil :form-id nil :core-node-id nil
                             :source-span {:source source-path} :generated-origin-chain []
                             :lowering-rule :sh07-b46-public-routing
                             :facts {:reason :source-read-failed :fail-closed true}
                             :remediation "Provide a readable .gravity or .qst source file through the declared public source boundary."}))))]
     (if (= {} options)
       (sh07-core-source-artifact source-path text)
       (let [reader-path (sh03-reader-resolve-source-path)
             invocation {:reader-source-path reader-path
                         :authenticated-envelope-options {:source :development-current}}
             resolved (compiler-c5-resolution-source-artifact source-path text
                        {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
                        reader-path invocation)]
         (sh07-core-from-resolution-artifact resolved invocation))))))

(defn
 sh07-core-artifact-identity-input
 [artifact]
 (gravity.checked-core/artifact-identity-input
  artifact
  (checked-core-stage-operations)))

(defn-
 sh07-core-verification-report
 [artifact expected upstream-verification]
 (#'gravity.checked-core/verification-report
  artifact
  expected
  upstream-verification
  (checked-core-stage-operations)))

(defn sh07-core-artifact-verification-against-expected
  "Verify an SH-07 candidate against a previously authenticated session.

  This development-only path performs no expected-artifact reconstruction.
  It validates the session binding and upstream identity before reusing the
  exact SH-07 check catalog. Invalid, stale, or substituted sessions produce
  the same fail-closed verification report shape as a contained verifier
  failure. `sh07-core-artifact-verification` remains the fresh authoritative
  verifier for admission and release evidence."
  [artifact session]
  (if (sh07-core-verification-session-valid? artifact session)
    (sh07-core-verification-report
     artifact (:expected session) (:upstream-verification session))
    (sh07-core-verification-report
     artifact nil
     (or (:upstream-verification session)
         {:artifact :gravity/sh06-resolution-artifact-verification
          :status :failed
          :failed-checks [:invalid-verification-session]}))))
