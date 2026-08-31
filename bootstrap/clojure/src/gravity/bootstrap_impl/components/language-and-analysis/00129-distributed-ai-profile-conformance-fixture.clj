

(defn distributed-ai-profile-conformance-fixture
  [profile document matrix required-artifacts]
  {:documents ["P9" "P10"]
   :active-document document
   :active-profile profile
   :required-profiles distributed-ai-profile-order
   :diagnostic-ids (get distributed-ai-diagnostic-ids-by-document document)
   :required-artifacts required-artifacts
   :effective-effects (get-in matrix [:effects :effective])
   :effective-capabilities (get-in matrix [:capabilities :effective])
   :replay-proof-status :complete
   :capability-proof-status :complete
   :status :complete})

(defn distributed-ai-cross-profile-boundary-graph
  [profile document profile-validation]
  {:consumer-profile profile
   :document document
   :boundary-kind (if (= :distributed profile)
                    :service-boundary
                    :agent-tool-boundary)
   :schemas (select-keys profile-validation
                         [:message-schema-bundle :tool-schema-bundle
                          :model-call-trace-schema :event-log-schema])
   :replay (select-keys profile-validation
                        [:replay-policy-log-schema :replay-log-schema
                         :workflow-graph])
   :capability-manifests (select-keys profile-validation
                                      [:external-service-capability-manifest
                                       :tool-capability-manifest])
   :status :complete})

(defn distributed-ai-profile-report
  [profile document manifest-artifact matrix profile-validation required-artifacts]
  {:document document
   :profile profile
   :profile-manifest (:profile-manifest manifest-artifact)
   :required-artifacts required-artifacts
   :artifact-evidence (select-keys profile-validation required-artifacts)
   :cross-profile-boundary-graph
   (distributed-ai-cross-profile-boundary-graph profile document
                                                profile-validation)
   :capability-based-proof
   (constrained-profile-capability-proof manifest-artifact matrix
                                         required-artifacts)
   :diagnostic-ids (get distributed-ai-diagnostic-ids-by-document document)
   :status :complete})

(defn distributed-ai-profile-source-artifact
  [source-path source-text]
  (try
    (let [manifest-artifact (profile-manifest-source-artifact source-path source-text)
          manifest (:profile-manifest manifest-artifact)
          profile (:profile manifest)
          document (distributed-ai-profile-documents-by-profile profile)
          _ (when-not document
              (fail! "P1-PROFILE-UNSUPPORTED"
                     "distributed/AI profile validation covers :distributed and :ai profiles"
                     {:source-span {:source source-path}
                      :profile profile
                      :supported distributed-ai-profile-order
                      :remediation "Use profile-validation for constrained profiles or profile-set for P2-P5."}))
          profile-validation (get-in manifest [:metadata :profile-validation] {})
          required-artifacts (require-distributed-ai-artifacts!
                              source-path profile document profile-validation)
          matrix {:profile profile
                  :document document
                  :effects {:source (:source-effects manifest)
                            :inferred (:inferred-effects manifest)
                            :effective (:effective-effects manifest)
                            :permission-table (:effect-permission-table
                                               manifest-artifact)}
                  :capabilities {:source (:source-capabilities manifest)
                                 :required (:required-capabilities manifest)
                                 :effective (:effective-capabilities manifest)
                                 :permission-table (:capability-permission-table
                                                    manifest-artifact)}
                  :memory (:memory-regime manifest)
                  :runtime (:runtime-assumptions manifest)}
          report (distributed-ai-profile-report profile document
                                                manifest-artifact matrix
                                                profile-validation
                                                required-artifacts)
          conformance (distributed-ai-profile-conformance-fixture
                       profile document matrix required-artifacts)]
      {:kind :gravity/stage0-distributed-ai-profile-artifact
       :document-set ["P9" "P10"]
       :pass {:name :distributed-ai-profile-validation
              :input :profile-manifest
              :output :cross-profile-boundary-graph
              :requires [:reader :namespace-analyzer :macro-expansion
                         :core-lowering :type-effect-capability-check
                         :profile-manifest-validation]
              :preserves [:source-spans :generated-origin :profile :target
                          :effects :capabilities :profile-validation-evidence
                          :replay-policy :human-review-policy]
              :emits [:profile-manifest :effect-capability-matrix
                      :profile-validation-report
                      :cross-profile-boundary-graph
                      :distributed-ai-conformance-fixture]
              :rejects distributed-ai-diagnostic-ids}
       :profile-manifest-artifact-hash (str "sha256:"
                                            (sha256-hex
                                             (pr-str manifest-artifact)))
       :profile-manifest manifest
       :profile-contract (:profile-contract manifest-artifact)
       :effect-capability-matrix matrix
       :profile-validation-report report
       :cross-profile-boundary-graph
       (:cross-profile-boundary-graph report)
       :distributed-ai-conformance-fixture conformance
       :diagnostics []})
    (catch clojure.lang.ExceptionInfo ex
      (throw-distributed-ai-diagnostic! ex))))