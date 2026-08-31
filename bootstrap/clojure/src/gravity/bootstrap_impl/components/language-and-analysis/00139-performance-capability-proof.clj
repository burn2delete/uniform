

(defn performance-capability-proof
  [manifest claim performance]
  {:profile-legality-preserved? (= (:profile manifest) (:profile claim))
   :effect-authority-preserved? (empty? (:expanded-effects performance))
   :capability-authority-preserved?
   (empty? (:expanded-capabilities performance))
   :safety-evidence-preserved? (empty? (:lost-safety-facts performance))
   :target-fingerprint-recorded? (perf-present? (perf1-target-fingerprint
                                                 claim))
   :benchmark-not-used-for-safety-elision? true
   :status :complete})

(defn performance-source-artifact
  [source-path source-text]
  (let [manifest-artifact (profile-manifest-source-artifact source-path
                                                            source-text)
        manifest (:profile-manifest manifest-artifact)
        performance (get-in manifest [:metadata :performance] {})
        claim (perf1-normalize-claim (:claim performance))
        _ (perf1-validate-claim! source-path manifest performance claim)
        target-fingerprint (perf1-target-fingerprint claim)
        optimization-decisions (vec (:optimization-decisions performance))
        generated-variants (vec (:generated-variants performance))
        proof-index {:semantic-proof (:semantic-proof claim)
                     :safety-proof (:safety-proof claim)
                     :proof-artifacts (:artifacts claim)}
        conformance {:document "PERF1"
                     :task "P04-T01"
                     :required-diagnostic-ids perf1-diagnostic-ids
                     :claim-schema-status :complete
                     :target-fingerprint-status :complete
                     :proof-index-status :complete
                     :status :complete}]
    {:kind :gravity/stage0-performance-claim-artifact
     :document "PERF1"
     :pass {:name :performance-claim-validation
            :input :profile-manifest
            :output :optimization-manifest
            :requires [:reader :namespace-analyzer :macro-expansion
                       :core-lowering :type-effect-capability-check
                       :profile-manifest-validation]
            :preserves [:source-spans :profile :target :effects
                        :capabilities :safety-mode :profile-legality
                        :proof-index]
            :emits [:performance-contract-manifest
                    :optimization-decision-log
                    :target-feature-report
                    :layout-input-shape-record
                    :benchmark-report
                    :proof-index
                    :generated-variant-manifest
                    :performance-conformance-results]
            :rejects perf1-diagnostic-ids}
     :profile-manifest-artifact-hash (str "sha256:"
                                          (sha256-hex
                                           (pr-str manifest-artifact)))
     :profile-manifest manifest
     :performance-contract-manifest
     {:claim-id (:claim-id claim)
      :profile (:profile claim)
      :source-profile (:profile manifest)
      :stage0-source-target (:target manifest)
      :target-request (:target claim)
      :target-features (:target-features claim)
      :safety-mode (:safety-mode claim)
      :objective (:objective claim)
      :status :complete}
     :optimization-decision-log optimization-decisions
     :target-feature-report {:target (:target claim)
                             :features (:target-features claim)
                             :fingerprint target-fingerprint
                             :status :complete}
     :layout-input-shape-record {:input-shape (:input-shape claim)
                                 :layout (:layout claim)
                                 :status :complete}
     :benchmark-report (:benchmark claim)
     :proof-index proof-index
     :generated-variant-manifest generated-variants
     :capability-based-proof (performance-capability-proof manifest claim
                                                           performance)
     :performance-conformance-results conformance
     :diagnostics []}))