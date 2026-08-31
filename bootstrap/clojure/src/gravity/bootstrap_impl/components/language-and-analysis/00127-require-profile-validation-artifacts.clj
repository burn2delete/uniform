

(defn require-profile-validation-artifacts!
  [source-path profile document profile-validation]
  (let [required (constrained-profile-required-artifacts profile)
        missing (vec (remove #(profile-artifact-present? profile-validation %)
                             required))]
    (when-let [missing-artifact (first missing)]
      (fail! (get-in constrained-profile-artifact-diagnostic-by-key
                     [profile missing-artifact])
             "constrained profile validation evidence is incomplete"
             {:source-span {:source source-path}
              :profile profile
              :document-id document
              :missing-artifact missing-artifact
              :required-artifacts required
              :remediation "Record the required profile-validation evidence in namespace metadata before lowering."}))
    required))

(defn constrained-profile-capability-proof
  [manifest-artifact matrix required-artifacts]
  {:effective-effects (get-in matrix [:effects :effective])
   :effective-capabilities (get-in matrix [:capabilities :effective])
   :effect-permission-table (:effect-permission-table manifest-artifact)
   :capability-permission-table (:capability-permission-table manifest-artifact)
   :memory-regime (:memory matrix)
   :runtime-assumptions (:runtime matrix)
   :required-artifacts required-artifacts
   :status :complete})

(defn constrained-profile-conformance-fixture
  [profile document matrix required-artifacts]
  {:documents ["P6" "P7" "P8" "P11" "P12"]
   :active-document document
   :active-profile profile
   :required-profiles constrained-profile-order
   :diagnostic-ids (get constrained-profile-diagnostic-ids-by-document document)
   :required-artifacts required-artifacts
   :effective-effects (get-in matrix [:effects :effective])
   :effective-capabilities (get-in matrix [:capabilities :effective])
   :artifact-status :complete
   :capability-proof-status :complete
   :status :complete})

(defn constrained-profile-validation-report
  [profile document manifest-artifact matrix profile-validation required-artifacts]
  {:document document
   :profile profile
   :profile-manifest (:profile-manifest manifest-artifact)
   :memory-regime (:memory matrix)
   :runtime-assumptions (:runtime matrix)
   :backend-eligibility (:backend-eligibility-report manifest-artifact)
   :required-artifacts required-artifacts
   :artifact-evidence (select-keys profile-validation required-artifacts)
   :capability-based-proof
   (constrained-profile-capability-proof manifest-artifact matrix
                                         required-artifacts)
   :diagnostic-ids (get constrained-profile-diagnostic-ids-by-document document)
   :status :complete})

(defn constrained-profile-source-artifact
  [source-path source-text]
  (try
    (let [manifest-artifact (profile-manifest-source-artifact source-path source-text)
          manifest (:profile-manifest manifest-artifact)
          profile (:profile manifest)
          document (constrained-profile-documents-by-profile profile)
          _ (when-not document
              (fail! "P1-PROFILE-UNSUPPORTED"
                     "constrained profile validation covers firmware, kernel, hardware, GPU, and formal profiles"
                     {:source-span {:source source-path}
                      :profile profile
                      :supported constrained-profile-order
                      :remediation "Use profile-set for P2-P5 or a later profile task for distributed and AI."}))
          profile-validation (get-in manifest [:metadata :profile-validation] {})
          required-artifacts (require-profile-validation-artifacts!
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
          report (constrained-profile-validation-report
                  profile document manifest-artifact matrix profile-validation
                  required-artifacts)
          conformance (constrained-profile-conformance-fixture
                       profile document matrix required-artifacts)]
      {:kind :gravity/stage0-constrained-profile-validation-artifact
       :document-set ["P6" "P7" "P8" "P11" "P12"]
       :pass {:name :constrained-profile-validation
              :input :profile-manifest
              :output :profile-validation-report
              :requires [:reader :namespace-analyzer :macro-expansion
                         :core-lowering :type-effect-capability-check
                         :profile-manifest-validation]
              :preserves [:source-spans :generated-origin :profile :target
                          :effects :capabilities :memory-regime
                          :runtime-assumptions :profile-validation-evidence]
              :emits [:profile-manifest :effect-capability-matrix
                      :profile-validation-report
                      :constrained-profile-conformance-fixture]
              :rejects constrained-profile-diagnostic-ids}
       :profile-manifest-artifact-hash (str "sha256:"
                                            (sha256-hex
                                             (pr-str manifest-artifact)))
       :profile-manifest manifest
       :profile-contract (:profile-contract manifest-artifact)
       :effect-capability-matrix matrix
       :profile-validation-report report
       :constrained-profile-conformance-fixture conformance
       :diagnostics []})
    (catch clojure.lang.ExceptionInfo ex
      (throw-profile-validation-diagnostic! ex))))

(def distributed-ai-profile-order
  [:distributed :ai])

(def distributed-ai-profile-documents-by-profile
  {:distributed "P9"
   :ai "P10"})