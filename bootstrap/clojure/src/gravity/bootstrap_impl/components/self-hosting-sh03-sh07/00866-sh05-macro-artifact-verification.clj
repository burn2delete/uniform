

(defn sh05-macro-artifact-verification
  ([artifact] (sh05-macro-artifact-verification artifact nil))
  ([artifact invocation]
  (when invocation
    (sh03-reader-authenticated-envelope-options
     (get-in artifact [:provenance :source-path]) invocation))
  (sh07-proof-transaction-report
   :sh05 :final
   {:verifier-root
    (var-get #'sh05-macro-artifact-verification*)
    :report-schema-version 1
    :invocation invocation
    :check-catalog-domain :gravity/sh05-final-verification-checks
    :source-content-hash sh05-macro-expected-source-content-hash
    :plan-semantic-hash sh05-macro-expected-plan-semantic-hash
    :functions-semantic-hash
    sh05-macro-expected-functions-semantic-hash}
   artifact
   #(sh05-macro-artifact-verification* artifact false invocation))))

(defn sh05-macro-capability-based-proof-for-construction
  ([artifact] (sh05-macro-capability-based-proof-for-construction artifact nil))
  ([artifact invocation]
  (when invocation
    (sh03-reader-authenticated-envelope-options
     (get-in artifact [:provenance :source-path]) invocation))
  (let [report
        (sh07-proof-transaction-report
         :sh05 :construction
         {:verifier-root
          (var-get #'sh05-macro-artifact-verification*)
          :report-schema-version 1
          :invocation invocation
          :check-catalog-domain :gravity/sh05-construction-verification-checks
          :source-content-hash sh05-macro-expected-source-content-hash
          :plan-semantic-hash sh05-macro-expected-plan-semantic-hash
          :functions-semantic-hash
          sh05-macro-expected-functions-semantic-hash}
         artifact
         #(sh05-macro-artifact-verification* artifact true invocation))
        checks (:checks report)]
    (assoc checks
           :artifact :gravity/sh05-macro-capability-proof
           :status (if (= :passed (:status report)) :complete :failed)
           :failed-checks (:failed-checks report)))))

(defn sh05-macro-capability-based-proof
  ([artifact] (sh05-macro-capability-based-proof artifact nil))
  ([artifact invocation]
  (let [report (sh05-macro-artifact-verification artifact invocation)
        checks (:checks report)]
    (assoc checks
           :artifact :gravity/sh05-macro-capability-proof
           :status (if (= :passed (:status report)) :complete :failed)
           :failed-checks (:failed-checks report)))))

(defn sh05-authenticated-source-snapshot!
  "Check current bounded no-follow source identity against nested C2."
  [source-path c2-artifact phase legacy?]
  (if legacy?
    {:canonical-path source-path :byte-count nil :bytes-hash
     (get-in c2-artifact [:source-unit-record :bytes-hash])}
    (let [source-unit (:source-unit-record c2-artifact)
        expected-path (:path source-unit)
        expected-hash (:bytes-hash source-unit)
        expected-count (or (:byte-count source-unit)
                           (get-in c2-artifact
                                   [:gravity-reader-boundary
                                    :authenticated-envelope-descriptor
                                    :source-revision :source-byte-count]))
        snapshot (try
                   (c2-pass-cache/bounded-source-snapshot! source-path)
                   (catch Exception error
                     (sh05-macro-boundary-fail!
                      "C4-RETURN" source-path phase
                      {:source-path source-path
                       :error-class (some-> error class .getName)
                       :error-data (ex-data error)}
                      {:required [:bounded-no-follow-source-snapshot]})))]
    (when-not (and (= source-path expected-path)
                   (= (:canonical-path snapshot)
                      (.getCanonicalPath (java.io.File. expected-path)))
                   (= expected-hash (:bytes-hash snapshot))
                   (or (nil? expected-count)
                       (= (long expected-count) (long (:byte-count snapshot)))))
      (sh05-macro-boundary-fail!
       "C4-RETURN" source-path phase
       {:source-path source-path :expected-path expected-path
        :observed-canonical-path (:canonical-path snapshot)
        :expected-bytes-hash expected-hash
        :observed-bytes-hash (:bytes-hash snapshot)
        :expected-byte-count expected-count
        :observed-byte-count (:byte-count snapshot)}
       {:required [:source-path-provenance :source-bytes-hash
                   :source-byte-count]}))
      snapshot)))

(defn- sh05-source-snapshot-stable!
  [source-path before c2-artifact legacy?]
  (let [after (sh05-authenticated-source-snapshot!
               source-path c2-artifact :source-stability-after-c4 legacy?)]
    (if legacy?
      after
      (do
        (when-not (= (select-keys before [:canonical-path :byte-count :bytes-hash])
                     (select-keys after [:canonical-path :byte-count :bytes-hash]))
          (sh05-macro-boundary-fail!
           "C4-RETURN" source-path :source-mutated-during-c4
           {:before (select-keys before [:canonical-path :byte-count :bytes-hash])
            :after (select-keys after [:canonical-path :byte-count :bytes-hash])}
           {:required [:stable-source-before-and-after-construction]}))
        after))))

(defn- sh05-authenticated-c3-parent!
  "Validate the complete C3 owner artifact before SH-05 consumes it.

  The C3 artifact id is only one witness.  SH-05 also requires the C3 verifier,
  capability proof, accepted status, and source/path provenance to agree.  This
  keeps the direct-parent edge honest while preserving the established C4
  boundary diagnostic for an invalid parent.
  "
  [source-path c3-artifact]
  (let [c2-artifact (:c2-reader-artifact c3-artifact)
        source-unit (:source-unit-record c2-artifact)
        report (:syntax-verification-report c3-artifact)
        proof (:capability-based-proof c3-artifact)
        results (:c3-syntax-results c3-artifact)
        observed-path (:path source-unit)
        artifact-id (try
                      (c3-artifact-id c3-artifact)
                      (catch Exception _ nil))
        ;; The source-unit/revision ids intentionally live in different
        ;; domains.  The path, bytes, and owner identity are the join here;
        ;; avoid accepting a malformed nil provenance merely because the
        ;; owner artifact id happens to recompute.
        passed? (and (map? c3-artifact)
                     (= :gravity/stage0-c3-syntax-object-artifact
                        (:kind c3-artifact))
                     (= :passed (:status report))
                     (= :complete (:status proof))
                     (= :complete (:status results))
                     (= (:artifact-id c3-artifact) artifact-id)
                     (= source-path observed-path)
                     (map? source-unit)
                     (string? (:source-id source-unit))
                     (string? (:bytes-hash source-unit))
                     (= (:bytes-hash source-unit)
                        (get-in source-unit [:identity-inputs :bytes-hash]))
                     (= (:project-relative-path source-unit)
                        (get-in source-unit
                                [:identity-inputs :project-relative-path]))
                     (= (:project-root source-unit)
                        (get-in source-unit
                                [:identity-inputs :project-root-id]))
                     (= source-path
                        (get-in c3-artifact
                                [:gravity-syntax-boundary
                                 :authenticated-envelope-descriptor
                                 :actual-path-provenance
                                 :source-path]))
                     (= source-path
                        (get-in c3-artifact
                                [:gravity-syntax-boundary
                                 :reader-authentication-provenance
                                 :actual-sh03-envelope-descriptor
                                 :actual-path-provenance
                                 :source-path]))
                     (= :accepted
                        (get-in c3-artifact
                                [:gravity-syntax-boundary
                                 :authenticated-envelope :status]))
                     (= :passed
                        (get-in c3-artifact
                                [:gravity-syntax-boundary
                                 :resolved-stream-verification :status]))) ]
    (when-not passed?
      (sh05-macro-boundary-fail!
       "C4-RETURN" source-path :authenticated-c3-syntax-parent
       {:source-path source-path
        :observed-path observed-path
        :artifact-kind (:kind c3-artifact)
        :artifact-id (:artifact-id c3-artifact)
        :recomputed-artifact-id artifact-id
        :syntax-verification-status (:status report)
        :capability-proof-status (:status proof)
        :c3-results-status (:status results)}
       {:required [:complete-c3-artifact-identity
                   :passed-c3-verifier
                   :complete-c3-capability-proof
                   :source-unit-path-provenance]}))
    c3-artifact))
