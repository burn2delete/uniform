

(defn safe16-conformance-fixture
  [checker-state]
  (let [expected-outcomes (:safe16-expected-outcome-manifests checker-state)
        covered-documents (set (map :document-id expected-outcomes))
        covered (cond-> #{}
                  (seq (:safe16-fixture-manifests checker-state))
                  (conj :fixture-manifest)
                  (seq expected-outcomes)
                  (conj :expected-outcome)
                  (seq (:safe16-diagnostic-match-records checker-state))
                  (conj :diagnostic-match)
                  (seq (:safe16-runtime-check-inspections checker-state))
                  (conj :runtime-check-inspection)
                  (seq (:safe16-unsafe-audit-inspections checker-state))
                  (conj :unsafe-audit-inspection)
                  (seq (:safe16-certificate-inspections checker-state))
                  (conj :certificate-inspection)
                  (seq (:safe16-profile-matrix-reports checker-state))
                  (conj :profile-matrix)
                  (seq (:safe16-backend-preservation-reports checker-state))
                  (conj :backend-preservation)
                  (seq (:safe16-conformance-reports checker-state))
                  (conj :conformance-report)
                  (set/subset? safe16-required-documents covered-documents)
                  (conj :fixture-family-coverage))
        missing (vec (remove covered safe16-required-families))]
    {:required-families safe16-required-families
     :required-documents (vec (sort-by name safe16-required-documents))
     :covered-families (vec (sort-by name covered))
     :covered-documents (vec (sort-by name covered-documents))
     :document :SAFE16
     :missing-families missing
     :status (if (empty? missing) :complete :incomplete)}))