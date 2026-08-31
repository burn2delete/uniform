

(defn eml-capability-proof
  [suite]
  {:efir-input-verified? (true? (get-in suite [:efir-input :verified?]))
   :semantic-facts-preserved?
   (every? #(and (perf-present? (:source-efir %))
                 (perf-present? (:domain %))
                 (perf-present? (:numeric-mode %))
                 (perf-present? (:precision %))
                 (perf-present? (:branch-policy %)))
           (:eml-expressions suite))
   :trace-replayable?
   (every? #(and (perf-present? (:rule %))
                 (perf-present? (:premises %))
                 (perf-present? (:source %))
                 (not (false? (:replayable? %))))
           (:normalization-trace suite))
   :search-bounded-deterministic?
   (and (true? (get-in suite [:search-manifest :deterministic?]))
        (true? (get-in suite [:search-manifest :bounded?]))
        (perf-present? (get-in suite [:search-manifest :tie-policy])))
   :candidate-promotion-proof-gated?
   (every? #(or (not (:can-influence-lowering? %))
                (and (contains? #{:proved :bounded} (:state %))
                     (perf-present? (:proof %))))
           (:candidates suite))
   :complex-intermediates-tracked?
   (every? #(or (not (:introduced? %))
                (and (perf-present? (:branch-policy %))
                     (perf-present? (:proof %))
                     (not (false? (:final-domain-valid? %)))))
           (:complex-intermediates suite))
   :eml-not-runtime-or-equality?
   (every? #(false? (:runtime-representation? %)) (:eml-expressions suite))
   :status :complete})

(defn eml-source-artifact
  [source-path source-text]
  (let [efir-artifact (efir-source-artifact source-path source-text)
        manifest (:profile-manifest efir-artifact)
        suite (eml-suite manifest)
        _ (eml-validate-math4! source-path manifest efir-artifact suite)
        capability-proof (eml-capability-proof suite)
        conformance {:documents ["MATH4"]
                     :task "P05-T03"
                     :required-diagnostic-ids math4-diagnostic-ids
                     :eml-lowering-status :complete
                     :trace-replay-status :complete
                     :search-manifest-status :complete
                     :candidate-lifecycle-status :complete
                     :status :complete}]
    {:kind :gravity/stage0-eml-artifact
     :document-set ["MATH4"]
     :pass {:name :eml-normalization
            :input :efir-graph
            :output :eml-trace
            :requires [:efir-validation :numeric-mode-validation
                       :proof-obligation-seed-list]
            :preserves [:source-spans :profile :target :effects
                        :capabilities :source-efir :domain :codomain
                        :numeric-mode :precision-contract :branch-policy]
            :emits [:eml-expression-tree
                    :efir-to-eml-node-map
                    :domain-environment
                    :branch-policy-ledger
                    :normalization-trace
                    :search-space-manifest
                    :candidate-list
                    :proof-request-table
                    :complex-intermediate-ledger
                    :accepted-proof-artifacts
                    :eml-conformance-results]
            :rejects math4-diagnostic-ids}
     :efir-artifact-hash (str "sha256:" (sha256-hex (pr-str efir-artifact)))
     :efir-artifact-kind (:kind efir-artifact)
     :profile-manifest manifest
     :eml-expression-tree (:eml-expressions suite)
     :efir-to-eml-node-map
     (mapv #(select-keys % [:eml-artifact-id :source-efir :node-map])
           (:eml-expressions suite))
     :domain-environment
     (mapv #(select-keys % [:eml-artifact-id :domain :codomain])
           (:eml-expressions suite))
     :branch-policy-ledger
     (mapv #(select-keys % [:eml-artifact-id :branch-policy])
           (:eml-expressions suite))
     :normalization-trace (:normalization-trace suite)
     :search-space-manifest (:search-manifest suite)
     :candidate-list (:candidates suite)
     :proof-request-table (:proof-requests suite)
     :complex-intermediate-ledger (:complex-intermediates suite)
     :accepted-proof-artifacts
     (vec (filter #(= :accepted (:status %)) (:proof-requests suite)))
     :capability-based-proof capability-proof
     :eml-conformance-results conformance
     :diagnostics []}))