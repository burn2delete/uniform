

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

(defn sh05-macro-source-artifact
  [source-path source-text]
  (let [c3-artifact
        (compiler-c3-syntax-source-artifact source-path source-text)
        _ (c3-syntax-validate! source-path c3-artifact)
        c2-artifact (:c2-reader-artifact c3-artifact)
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
    artifact))

(defn sh05-macro-file-artifact
  [source-path]
  (let [c2-artifact (compiler-c2-reader-file-artifact source-path)
        source-text (c2-reader-artifact-source-text source-path c2-artifact)]
    (sh05-macro-source-artifact source-path source-text)))