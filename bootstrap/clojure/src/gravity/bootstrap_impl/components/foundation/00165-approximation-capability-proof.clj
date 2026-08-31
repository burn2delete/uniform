

(defn approximation-capability-proof
  [suite]
  {:certificates-shaped?
   (every? #(empty? (approximation-missing-fields %)) (:certificates suite))
   :efir-anchors-present?
   (every? #(perf-present? (:target-efir %)) (:certificates suite))
   :domain-coverage-complete?
   (every? #(= :complete (:domain-coverage %)) (:certificates suite))
   :approximation-and-roundoff-separated?
   (every? #(and (number? (get-in % [:error-proof :approximation]))
                 (number? (get-in % [:error-proof :roundoff]))
                 (number? (get-in % [:error-proof :combined])))
           (:certificates suite))
   :target-assumptions-explicit?
   (every? #(perf-present? (:target-assumptions %)) (:certificates suite))
   :checker-independent-replayable?
   (every? #(and (true? (get-in % [:checker :independent?]))
                 (true? (get-in % [:checker :replayable?])))
           (:certificates suite))
   :runtime-selection-certificate-linked?
   (every? #(or (not (:selected? %))
                (and (:evidence-accepted? %)
                     (perf-present? (:certificate-id %))
                     (perf-present? (:efir-graph %))))
           (:selected-implementations suite))
   :status :complete})

(defn approximation-source-artifact
  [source-path source-text]
  (let [eml-artifact (eml-source-artifact source-path source-text)
        manifest (:profile-manifest eml-artifact)
        suite (approximation-suite manifest)
        _ (approximation-validate-math5! source-path manifest eml-artifact suite)
        capability-proof (approximation-capability-proof suite)
        conformance {:documents ["MATH5"]
                     :task "P05-T04"
                     :required-diagnostic-ids math5-diagnostic-ids
                     :certificate-status :complete
                     :checker-replay-status :complete
                     :runtime-selection-status :complete
                     :status :complete}]
    {:kind :gravity/stage0-certified-approximation-artifact
     :document-set ["MATH5"]
     :pass {:name :certified-approximation
            :input :eml-trace
            :output :approximation-certificate
            :requires [:eml-normalization :efir-validation
                       :numeric-mode-validation]
            :preserves [:source-spans :profile :target :effects
                        :capabilities :target-efir :domain :codomain
                        :numeric-mode :precision-contract :branch-policy]
            :emits [:candidate-approximation-set
                    :selected-implementation-record
                    :approximation-certificate
                    :checker-transcript
                    :target-assumption-manifest
                    :exceptional-path-coverage-report
                    :runtime-implementation-anchor
                    :rejection-report
                    :approximation-conformance-results]
            :rejects math5-diagnostic-ids}
     :eml-artifact-hash (str "sha256:" (sha256-hex (pr-str eml-artifact)))
     :eml-artifact-kind (:kind eml-artifact)
     :profile-manifest manifest
     :candidate-approximation-set (:candidates suite)
     :selected-implementation-record (:selected-implementations suite)
     :approximation-certificate (:certificates suite)
     :checker-transcript
     (mapv #(select-keys (:checker %)
                         [:name :version :input-hash :transcript-hash
                          :independent? :replayable? :trust-root])
           (:certificates suite))
     :target-assumption-manifest
     (mapv #(select-keys % [:certificate-id :target-assumptions])
           (:certificates suite))
     :exceptional-path-coverage-report
     (mapv #(select-keys % [:certificate-id :branch-policy])
           (:certificates suite))
     :runtime-implementation-anchor
     (mapv #(select-keys % [:provider :certificate-id :efir-graph
                            :candidate-id :status])
           (:selected-implementations suite))
     :rejection-report
     (vec (filter #(= :rejected (:status %)) (:candidates suite)))
     :capability-based-proof capability-proof
     :approximation-conformance-results conformance
     :diagnostics []}))