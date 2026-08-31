

(defn profile-conformance-fixture
  [manifest effect-table capability-table dependency-graph backend-report]
  (let [required-fields [:profile :target :source-effects :inferred-effects
                         :effective-effects :source-capabilities
                         :required-capabilities :effective-capabilities
                         :memory-regime :runtime-assumptions :unsafe-policy
                         :dependencies :provider-selections]
        missing (vec (remove #(contains? manifest %) required-fields))]
    {:document :P1
     :required-fields required-fields
     :missing-fields missing
     :diagnostic-ids p1-diagnostic-ids
     :standard-profiles standard-profile-order
     :positive-fixtures :profile-manifest-suite
     :negative-fixtures :p1-diagnostic-suite
     :effect-permission-rows (count effect-table)
     :capability-permission-rows (count capability-table)
     :dependency-graph-status (if (:acyclic dependency-graph) :complete :incomplete)
     :backend-eligibility-status (if (:eligible? backend-report) :complete :rejected)
     :manifest-status (if (empty? missing) :complete :incomplete)
     :status (if (and (empty? missing) (:eligible? backend-report))
               :complete
               :incomplete)}))