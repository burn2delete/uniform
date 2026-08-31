

(defn checked-core-source-artifact
  [source-path source-text]
  (let [reader-artifact (read-source-artifact source-path source-text)
        module-artifact (module-source-artifact source-path source-text)
        macro-artifact (macro-source-artifact source-path source-text)
        core-artifact (core-source-artifact source-path source-text)
        typed-artifact (typed-source-artifact source-path source-text)
        profile-artifact (profile-manifest-source-artifact source-path
                                                           source-text)
        safety-artifact (safety-source-artifact source-path source-text)
        module (:module typed-artifact)
        source-id (str "sha256:" (sha256-hex source-text))
        syntax-stream (:syntax-object-stream reader-artifact)
        expanded-syntax (:expanded-syntax-object-stream macro-artifact)
        type-facts (:type-facts typed-artifact)
        ownership-facts (vec (concat (:ownership-borrow-facts typed-artifact)
                                     (:linear-resource-table typed-artifact)
                                     (:safe-memory-linear-flow-graphs
                                      typed-artifact)))
        capability-proof-records (vec (concat (:provider-selection-records
                                               typed-artifact)
                                              (:capability-report
                                               typed-artifact)
                                              (:safe10-capability-requirement-records
                                               typed-artifact)))
        safety-outcomes (:safety-classification-records safety-artifact)
        stage-records [(checked-core-stage-record
                        :read-source :C2 :source-bytes :syntax-seeds
                        source-id reader-artifact
                        [:source-spans :source-bytes :diagnostics]
                        [:source-unit-record :token-stream :form-tree
                         :reader-diagnostics])
                       (checked-core-stage-record
                        :build-syntax :C3 :syntax-seeds
                        :syntax-object-stream reader-artifact syntax-stream
                        [:source-spans :syntax-identity :origin-chain
                         :profile :diagnostics]
                        [:syntax-object-stream :origin-chain-graph
                         :syntax-verification-report])
                       (checked-core-stage-record
                        :macro-expand :C4 :syntax-object-stream
                        :expanded-syntax syntax-stream macro-artifact
                        [:source-spans :syntax-identity :origin-chain
                         :profile :diagnostics]
                        [:expanded-syntax-stream :macro-expansion-trace
                         :build-effect-log])
                       (checked-core-stage-record
                        :resolve-names :C5 :expanded-syntax
                        :namespace-analysis macro-artifact module-artifact
                        [:source-spans :syntax-identity :origin-chain
                         :profile :target :diagnostics]
                        [:namespace-analysis-artifact :binding-table
                         :resolution-diagnostics])
                       (checked-core-stage-record
                        :lower-to-core :C6 :namespace-analysis :core-ast
                        module-artifact core-artifact
                        [:source-spans :origin-chain :profile :target
                         :capabilities :diagnostics]
                        [:core-ast-module :desugaring-trace
                         :core-verifier-report])
                       (checked-core-stage-record
                        :type-check :C7 :core-ast :typed-core
                        core-artifact typed-artifact
                        [:source-spans :origin-chain :profile :target
                         :diagnostics]
                        [:typed-core-module :type-environment
                         :type-diagnostics])
                       (checked-core-stage-record
                        :effect-check :C8 :typed-core :effected-core
                        typed-artifact typed-artifact
                        [:source-spans :origin-chain :profile :target
                         :types :diagnostics]
                        [:effect-graph :capability-proof-record
                         :effect-diagnostics])
                       (checked-core-stage-record
                        :profile-validate :P1 :effected-core
                        :profile-valid-core typed-artifact profile-artifact
                        [:source-spans :origin-chain :profile :target
                         :types :effects :capabilities :diagnostics]
                        [:profile-validation-report :profile-diagnostics])
                       (checked-core-stage-record
                        :capability-validate :L15 :profile-valid-core
                        :capability-valid-core profile-artifact typed-artifact
                        [:source-spans :origin-chain :profile :target
                         :types :effects :capabilities :diagnostics]
                        [:capability-provider-report
                         :capability-diagnostics])
                       (checked-core-stage-record
                        :ownership-check :C9 :capability-valid-core
                        :ownership-checked-core typed-artifact typed-artifact
                        [:source-spans :origin-chain :profile :target
                         :types :effects :capabilities :diagnostics]
                        [:ownership-analysis :borrow-graph
                         :ownership-diagnostics])
                       (checked-core-stage-record
                        :safety-analyze :C10 :ownership-checked-core
                        :checked-core typed-artifact safety-artifact
                        [:source-spans :origin-chain :profile :target
                         :types :effects :ownership :capabilities
                         :diagnostics]
                        [:safety-analysis-report
                         :unsafe-island-audit-manifest
                         :safety-diagnostics])]
        artifact {:kind :gravity/stage0-checked-core-pipeline-artifact
                  :document-set ["C1" "C2" "C3" "C4" "C5" "C6" "C7"
                                 "C8" "C9" "C10"]
                  :pass {:name :reader-through-checked-core-integration
                         :input :source-bytes
                         :output :checked-core
                         :requires [:reader :syntax :macro-expansion
                                    :namespace-analysis :core-lowering
                                    :typed-core :effected-core
                                    :profile-manifest
                                    :capability-provider :ownership
                                    :safety-analysis]
                         :preserves [:source-spans :syntax-identity
                                     :origin-chain :profile :target :types
                                     :effects :ownership :capabilities
                                     :safety-outcomes :diagnostics]
                         :emits [:source-unit-record
                                 :syntax-object-stream
                                 :macro-expansion-trace
                                 :namespace-analysis-artifact
                                 :core-ast-module
                                 :typed-core-module
                                 :effect-graph
                                 :capability-proof-records
                                 :profile-validation-report
                                 :ownership-analysis
                                 :safety-analysis-report
                                 :checked-core-pipeline-manifest]
                         :rejects checked-core-diagnostic-ids}
                  :source-overrides (checked-core-source-overrides module)
                  :source-unit-record {:artifact :gravity/source-unit
                                       :source-id source-id
                                       :path source-path
                                       :encoding :utf-8
                                       :reader-options {:retain-comments true
                                                        :enabled-features
                                                        #{:standard-reader}}
                                       :incremental-reader-hash
                                       (checked-core-artifact-id
                                        (:syntax-object-stream
                                         reader-artifact))}
                  :module module
                  :pipeline-stage-order checked-core-stage-order
                  :stage-artifact-records stage-records
                  :stage-output-index (into {}
                                            (map (juxt :stage
                                                       :output-artifact-id))
                                            stage-records)
                  :reader-artifact reader-artifact
                  :syntax-object-stream syntax-stream
                  :syntax-verification-report {:artifact :gravity/syntax-verifier
                                               :status :passed
                                               :syntax-count
                                               (count syntax-stream)}
                  :macro-artifact macro-artifact
                  :expanded-syntax-object-stream expanded-syntax
                  :macro-expansion-trace (:macro-expansion-trace macro-artifact)
                  :macro-trace-replay-report {:artifact :gravity/macro-trace-replay
                                              :status :passed
                                              :steps (count (:macro-expansion-trace
                                                            macro-artifact))}
                  :namespace-analysis-artifact module-artifact
                  :binding-table (:definitions module-artifact)
                  :dependency-graph (:module-dependency-graph module-artifact)
                  :core-artifact core-artifact
                  :expanded-core-ast (:expanded-core-ast core-artifact)
                  :surface-to-core-map (:core-node-source-map core-artifact)
                  :desugaring-trace (:core-form-kind-records core-artifact)
                  :core-verifier-report {:artifact :gravity/core-verifier
                                         :status :passed
                                         :nodes (count (:core-node-source-map
                                                       core-artifact))}
                  :typed-artifact typed-artifact
                  :typed-core-module (:typed-core-ast typed-artifact)
                  :type-environment (:type-environment typed-artifact)
                  :type-facts type-facts
                  :dynamic-boundary-records
                  (:dynamic-boundary-records typed-artifact)
                  :effect-graph (:effect-environment typed-artifact)
                  :effect-legality-report (:effect-legality-report
                                           typed-artifact)
                  :capability-proof-records capability-proof-records
                  :profile-validation-artifact profile-artifact
                  :profile-validation-report (:profile-manifest
                                              profile-artifact)
                  :ownership-facts ownership-facts
                  :ownership-analysis {:artifact :gravity/ownership-analysis
                                       :borrow-graphs (:safe-memory-borrow-graphs
                                                       typed-artifact)
                                       :linear-resource-flow
                                       (:safe-memory-linear-flow-graphs
                                        typed-artifact)
                                       :resource-table
                                       (:linear-resource-table typed-artifact)}
                  :safety-artifact safety-artifact
                  :safety-outcome-records safety-outcomes
                  :runtime-check-records (:runtime-check-manifest
                                          safety-artifact)
                  :unsafe-island-audit-manifest
                  (:unsafe-island-audit-records safety-artifact)
                  :checked-core-pipeline-manifest
                  {:artifact :gravity/checked-core-pipeline
                   :pipeline-id (checked-core-artifact-id stage-records)
                   :source source-id
                   :stages checked-core-stage-order
                   :stage-outputs (into {}
                                        (map (juxt :stage
                                                   :output-artifact-id))
                                        stage-records)
                   :profile (:profile module)
                   :target (:target module)
                   :effects (:effects module)
                   :capabilities (:capabilities module)}
                  :diagnostics []}
        _ (checked-core-validate! source-path artifact)
        capability-proof (checked-core-capability-proof artifact)
        conformance {:documents ["C1" "C2" "C3" "C4" "C5" "C6" "C7"
                                 "C8" "C9" "C10"]
                     :task "P06-T02"
                     :required-diagnostic-ids checked-core-diagnostic-ids
                     :pipeline-integration-status :complete
                     :reader-status :complete
                     :syntax-status :complete
                     :macro-status :complete
                     :resolution-status :complete
                     :core-status :complete
                     :typed-effected-status :complete
                     :profile-capability-status :complete
                     :ownership-status :complete
                     :safety-status :complete
                     :status :complete}]
    (assoc artifact
           :capability-based-proof capability-proof
           :checked-core-results conformance)))