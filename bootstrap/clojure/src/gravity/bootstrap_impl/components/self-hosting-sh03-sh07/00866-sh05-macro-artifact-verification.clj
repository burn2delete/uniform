

(defn sh05-macro-artifact-verification
  [artifact]
  (sh07-proof-transaction-report
   :sh05 :final
   {:verifier-root
    (var-get #'sh05-macro-artifact-verification*)
    :report-schema-version 1
    :check-catalog-domain :gravity/sh05-final-verification-checks
    :source-content-hash sh05-macro-expected-source-content-hash
    :plan-semantic-hash sh05-macro-expected-plan-semantic-hash
    :functions-semantic-hash
    sh05-macro-expected-functions-semantic-hash}
   artifact
   #(sh05-macro-artifact-verification* artifact false)))

(defn sh05-macro-capability-based-proof-for-construction
  [artifact]
  (let [report
        (sh07-proof-transaction-report
         :sh05 :construction
         {:verifier-root
          (var-get #'sh05-macro-artifact-verification*)
          :report-schema-version 1
          :check-catalog-domain :gravity/sh05-construction-verification-checks
          :source-content-hash sh05-macro-expected-source-content-hash
          :plan-semantic-hash sh05-macro-expected-plan-semantic-hash
          :functions-semantic-hash
          sh05-macro-expected-functions-semantic-hash}
         artifact
         #(sh05-macro-artifact-verification* artifact true))
        checks (:checks report)]
    (assoc checks
           :artifact :gravity/sh05-macro-capability-proof
           :status (if (= :passed (:status report)) :complete :failed)
           :failed-checks (:failed-checks report))))

(defn sh05-macro-capability-based-proof
  [artifact]
  (let [report (sh05-macro-artifact-verification artifact)
        checks (:checks report)]
    (assoc checks
           :artifact :gravity/sh05-macro-capability-proof
           :status (if (= :passed (:status report)) :complete :failed)
           :failed-checks (:failed-checks report))))

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

(defn- sh05-macro-artifact-from-c3-artifact*
  "Expand an already authenticated C3 artifact without rebuilding C2/C3.

  The one-argument form derives the supplied source path from the authenticated
  C3 source-unit record.  Callers that have a path-scoped snapshot should use
  the two-argument form so path rebinding is checked against that snapshot.
  "
  ([c3-artifact]
   (sh05-macro-artifact-from-c3-artifact*
    (get-in c3-artifact [:c2-reader-artifact :source-unit-record :path])
    c3-artifact
    false))
  ([source-path c3-artifact]
   (sh05-macro-artifact-from-c3-artifact* source-path c3-artifact false))
  ([source-path c3-artifact legacy?]
   (sh05-authenticated-c3-parent! source-path c3-artifact)
   (let [c2-artifact (:c2-reader-artifact c3-artifact)
        source-before (sh05-authenticated-source-snapshot!
                       source-path c2-artifact :source-authentication-before-c4 legacy?)
        _ (c3-syntax-validate! source-path c3-artifact)
        forms (:parsed-semantic-values c2-artifact)
        module (parse-module source-path forms)
        binding (sh05-macro-current-binding! source-path)
        source-syntax (sh05-source-syntax-stream c3-artifact)
        _ (when-not (= (count forms) (count source-syntax))
            (sh05-macro-boundary-fail!
             "C4-RETURN" source-path :complete-sh04-source-syntax-lockstep
             {:forms (count forms) :syntaxes (count source-syntax)} {}))
        compiler-metadata (get-in module [:metadata :compiler])
        request-overrides
        (if (and (map? compiler-metadata)
                 (contains? compiler-metadata :sh05-request))
          (:sh05-request compiler-metadata)
          {})
        _ (when-not (map? request-overrides)
            (sh05-macro-boundary-fail!
             "C4-RETURN" source-path :map-shaped-macro-request-overrides
             request-overrides
             {:observed-type (some-> request-overrides class .getName)}))
        pairs
        (mapv
         (fn [form syntax]
           (if (sh05-defn-form? form)
             (let [request
                   (sh05-deep-merge
                    (sh05-default-macro-request
                     source-path c3-artifact module syntax form)
                    request-overrides)]
               {:form form :syntax syntax
                :run (sh05-run-macro-request!
                      source-path binding request)})
             {:form form :syntax syntax :run nil}))
         forms source-syntax)
        runs (mapv :run (filter :run pairs))
        expanded-stream
        (mapv (fn [{:keys [syntax form run]}]
                (sh05-expanded-syntax-object syntax form run))
              pairs)
        ;; C4's compatibility projection is the expanded namespace body.  The
        ;; authenticated syntax stream retains the namespace declaration for
        ;; C5, while :expanded-forms matches the established stage0 boundary.
        expanded-forms (mapv :form (subvec expanded-stream 1))
        trace
        (mapv #(sh05-package-trace % (:profile module) (:target module))
              runs)
        stream-id
        (sh05-expanded-syntax-stream-id source-path expanded-stream)
        trace-id
        (sh05-macro-trace-id source-path trace)
        graph (sh05-expanded-graph expanded-stream trace)
        sh04-view
        {:artifact :gravity/sh04-syntax-object-artifact
         :artifact-id (:artifact-id c3-artifact)
         :status :accepted
         :syntax-stream-id
         (get-in c3-artifact
                 [:gravity-syntax-boundary :resolved-syntax-result
                  :artifact-id])}
        plan-binding (dissoc binding :plan :source-text)
        boundary-base
        {:slice :SH-05
         :owner :gravity-source
         :adapter-contract sh05-macro-adapter-contract
         :plan-binding plan-binding
         :authenticated-sh04-artifact c3-artifact
         :expansion-runs runs
         :raw-template-result (:raw-template-result (first runs))
         :resolved-expansion (:resolved-expansion (first runs))
         :digest-requests (:digest-requests (first runs))
         :resolved-digests (:resolved-digests (first runs))
         :target-source-reread? false
         :clojure-adapter-residual? true
         :self-hosted? false}
        artifact-base
        {:kind :gravity/sh05-macro-expansion-artifact
         :status :accepted
         :slice :SH-05
         :task "SH-05"
         :document-set ["L4" "C4"]
         :governing-document c4-macro-governing-document
         :artifact-id nil
         :expanded-syntax-stream-id stream-id
         :macro-expansion-trace-id trace-id
         :expanded-defn-count (count runs)
         :expanded-syntax-stream expanded-stream
         :expanded-forms expanded-forms
         :macro-expansion-trace trace
         :macro-environment
         {:artifact :gravity/sh05-macro-environment
          :macros [{:name 'defn :version sh05-macro-version
                    :phase :macro-expansion}]
          :status :complete}
         :generated-origin-source-map
         (mapv #(select-keys % [:output-syntax-id :generated-origin]) trace)
         :expanded-syntax-graph graph
         :sh04-syntax-artifact sh04-view
         :gravity-macro-boundary boundary-base
         :provenance {:source-path source-path
                      :source-id (get-in c2-artifact
                                         [:source-unit-record :source-id])}
         :pass {:name :sh05-gravity-macro-expansion
                :input :authenticated-sh04-syntax
                :output :authenticated-sh05-expanded-syntax
                :preserves [:syntax-identity :metadata :hygiene
                            :origins :reader-lineage]
                :rejects c4-macro-diagnostic-ids}
         :capability-based-proof nil
         :execution-boundary
         {:macro-authority :gravity
          :gravity-module 'gravity.macro
          :plan-runner :clojure-stage0
          :digest-resolver :clojure-stage0
          :envelope-binder :clojure-stage0
          :c4-stage0-adapter :compatibility-only
          :self-hosted? false}
         :diagnostics []}
        artifact-id (sh05-macro-artifact-id artifact-base)
        summary
        (sh05-macro-envelope-summary
         (assoc artifact-base :artifact-id artifact-id))
        descriptor
        (sh05-macro-sh02-descriptor source-path binding summary)
        envelope
        (p15-s23-stage2-sh02-descriptor-envelope
         sh05-macro-envelope-stage sh05-macro-sealed-artifact-kind
         descriptor source-path)
        _ (p15-s23-stage2-sh02-descriptor-envelope-verify!
           envelope sh05-macro-envelope-stage
           sh05-macro-sealed-artifact-kind descriptor source-path)
        artifact-with-envelope
        (-> artifact-base
            (assoc :artifact-id artifact-id)
            (assoc-in [:gravity-macro-boundary
                       :authenticated-envelope-descriptor] descriptor)
            (assoc-in [:gravity-macro-boundary
                       :authenticated-envelope] envelope))
        proof
        (sh05-macro-capability-based-proof-for-construction
         artifact-with-envelope)
        artifact (assoc artifact-with-envelope :capability-based-proof proof)
        final-report (sh05-macro-artifact-verification artifact)]
    (when-not (= :passed (:status final-report))
      (sh05-macro-boundary-fail!
       "C4-TRACE" source-path :final-authenticated-macro-artifact
       (:failed-checks final-report) {}))
    (sh05-source-snapshot-stable! source-path source-before c2-artifact legacy?)
    artifact)))

(defn sh05-macro-artifact-from-c3-artifact
  ([c3-artifact]
   (sh05-macro-artifact-from-c3-artifact*
    (get-in c3-artifact [:c2-reader-artifact :source-unit-record :path])
    c3-artifact false))
  ([source-path c3-artifact]
   (sh05-macro-artifact-from-c3-artifact* source-path c3-artifact false)))

(defn sh05-macro-source-artifact
  [source-path source-text]
  ;; Compatibility wrapper: the historical source API still owns C3
  ;; construction, while the new parent API above owns C4 consumption.
  (sh05-macro-artifact-from-c3-artifact*
   source-path
   (compiler-c3-syntax-source-artifact source-path source-text)
   true))

(defn sh05-macro-file-artifact
  [source-path]
  (let [c2-artifact (compiler-c2-reader-file-artifact source-path)
        source-text (c2-reader-artifact-source-text source-path c2-artifact)]
    (sh05-macro-source-artifact source-path source-text)))
