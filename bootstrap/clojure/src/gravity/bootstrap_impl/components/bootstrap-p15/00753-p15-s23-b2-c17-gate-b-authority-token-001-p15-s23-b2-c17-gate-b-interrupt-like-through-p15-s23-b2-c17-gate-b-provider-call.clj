(let [p15-s23-b2-c17-gate-b-authority-token (nth __gravity_bootstrap_lexical_values_131964 0)
      p15-s23-b2-c17-gate-b-tool-state (nth __gravity_bootstrap_lexical_values_131964 1)]
(defn- p15-s23-b2-c17-gate-b-interrupt-like?
  [error]
  (or (instance? InterruptedException error)
      (instance? java.nio.channels.ClosedByInterruptException error)
      (instance? java.io.InterruptedIOException error)))

(defn- p15-s23-b2-c17-gate-b-restore-interrupt!
  [error]
  (when (p15-s23-b2-c17-gate-b-interrupt-like? error)
    (.interrupt (Thread/currentThread)))
  error)

(defn- p15-s23-b2-c17-gate-b-rethrow-interrupt!
  [error]
  (when (p15-s23-b2-c17-gate-b-interrupt-like? error)
    (p15-s23-b2-c17-gate-b-restore-interrupt! error)
    (throw error))
  error)

(defn p15-s23-b2-c17-gate-b-tool-execution-snapshot
  []
  @p15-s23-b2-c17-gate-b-tool-state)

(defn- p15-s23-b2-c17-gate-b-require-authority!
  [candidate source-path operation]
  (when-not (identical? candidate
                         p15-s23-b2-c17-gate-b-authority-token)
    (p15-s23-c-backend-fail!
     "B2-MANIFEST" source-path {}
     {:missing-fact :opaque-authenticated-c17-gate-b-authority
      :bounded-reason operation})))

(defn- p15-s23-b2-c17-gate-b-provider-error?
  [error]
  (true? (:gravity.darwin-publication/error (ex-data error))))

(defn- p15-s23-b2-c17-gate-b-provider-mode
  [mode]
  (case mode
    0755 "0755"
    0644 "0644"
    :redacted))

(defn- p15-s23-b2-c17-gate-b-provider-fail!
  [source-path boundary error]
  (let [data (ex-data error)
        operation (:operation data)
        reason (:reason data)
        native-reasons
        #{:unsupported-host-runtime :native-access-disabled
          :missing-native-symbol :ffi-binding-unavailable}
        authority-reasons
        #{:invalid-output-location :parent-descriptor-open-failed
          :untrusted-parent-descriptor :destination-exists
          :destination-absence-check-failed :invalid-provider-context
          :invalid-provider-control-state :invalid-provider-lifecycle}
        schema-reasons
        #{:invalid-file-set :invalid-file-specification
          :created-file-metadata-mismatch :descriptor-chmod-failed
          :staging-contract-mismatch
          :directory-inventory-limit :directory-read-failed
          :fdopendir-failed :invalid-directory-entry-layout
          :unterminated-directory-entry :invalid-utf8-directory-entry
          :platform-thread-required :invalid-publication-receipt}
        hash-reasons
        #{:file-write-failed :zero-progress-file-write
          :file-readback-failed :short-file-readback
          :file-content-or-metadata-mismatch
          :published-bundle-content-or-identity-mismatch}
        provenance-reasons
        #{:descriptor-stat-failed :descriptor-path-failed
          :descriptor-sync-failed :descriptor-close-failed
          :access-control-list-inspection-failed
          :access-control-list-release-failed
          :nontrivial-extended-access-control-list
          :bounded-staging-name-exhausted
          :staging-directory-create-failed
          :staging-directory-open-failed
          :directory-reopen-failed :directory-stream-close-failed
          :cleanup-directory-open-failed
          :exclusive-file-create-failed :unique-file-open-failed
          :untrusted-staging-descriptor
          :descriptor-or-content-identity-changed
          :published-directory-open-failed
          :published-directory-provenance-mismatch}
        [diagnostic-id missing-fact]
        (cond
          (= :native-access-disabled reason)
          ["B2-MANIFEST"
           :jdk26-native-access-required-for-c17-publication]

          (= :unsupported-host-runtime reason)
          ["B2-MANIFEST"
           :pinned-jdk26-macos-aarch64-c17-host-runtime]

          (contains? #{:missing-native-symbol :ffi-binding-unavailable}
                     reason)
          ["B2-MANIFEST"
           :jdk26-darwin-c17-publication-ffi-binding]

          (= :destination-exists reason)
          ["B2-MANIFEST"
           :collision-free-regular-c17-output-directory]

          (contains? #{:destination-collision
                       :exclusive-rename-failed}
                     reason)
          ["B2-MANIFEST" :exclusive-no-clobber-c17-publication]

          (or (contains? native-reasons reason)
              (contains? authority-reasons reason))
          ["B2-MANIFEST" :descriptor-relative-c17-publication-authority]

          (contains? schema-reasons reason)
          ["B13-SCHEMA" :exact-descriptor-relative-c17-bundle]

          (contains? hash-reasons reason)
          ["B13-HASH" :descriptor-relative-c17-publication-content]

          (contains? provenance-reasons reason)
          ["B13-PROVENANCE"
           :descriptor-relative-c17-publication-provenance]

          (= :verify boundary)
          ["B13-PROVENANCE"
           :descriptor-relative-c17-publication-verification]

          :else
          ["B2-MANIFEST" :contained-darwin-publication-failure])
        facts
        (cond->
         {:missing-fact missing-fact
          :bounded-reason
          (if (keyword? reason) reason :invalid-provider-failure)
          :tool-step
          (if (keyword? operation) operation :darwin-publication)}
          (boolean? (:output-collision? data))
          (assoc :output-collision? (:output-collision? data))

          (boolean? (:native-access-enabled? data))
          (assoc :native-access-enabled? (:native-access-enabled? data))

          (integer? (:return-code data))
          (assoc (if (= :commit operation)
                   :rename-return-code
                   :provider-return-code)
                 (:return-code data))

          (integer? (:errno data))
          (assoc :captured-errno (:errno data))

          (integer? (:expected-file-count data))
          (assoc :expected-file-count (:expected-file-count data))

          (integer? (:observed-file-count data))
          (assoc :observed-file-count (:observed-file-count data))

          (integer? (:observed-byte-count data))
          (assoc :observed-byte-count (:observed-byte-count data))

          (and (integer? (:expected-byte-count data))
               (<= 0 (:expected-byte-count data) (* 8 1024 1024)))
          (assoc :expected-byte-count (:expected-byte-count data))

          (string? (:logical-path data))
          (assoc :logical-path (:logical-path data))

          (integer? (:observed-mode data))
          (assoc :observed-mode
                 (p15-s23-b2-c17-gate-b-provider-mode
                  (:observed-mode data)))

          (contains? data :expected-mode)
          (assoc :expected-mode
                 (p15-s23-b2-c17-gate-b-provider-mode
                  (:expected-mode data))))]
    (p15-s23-c-backend-fail!
     diagnostic-id source-path {} facts)))

(defn- p15-s23-b2-c17-gate-b-provider-call!
  [source-path boundary operation]
  (try
    (operation)
    (catch clojure.lang.ExceptionInfo error
      (if (p15-s23-b2-c17-gate-b-provider-error? error)
        (p15-s23-b2-c17-gate-b-provider-fail!
         source-path boundary error)
        (throw error))))))
