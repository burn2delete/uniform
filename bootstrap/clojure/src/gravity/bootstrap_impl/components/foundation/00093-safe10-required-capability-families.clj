

(def safe10-required-capability-families
  #{:filesystem :network :environment :secret :process :model :tool
    :memory :ffi :compiler :hardware})

(def safe10-required-families
  [:capability-requirement :grant-intersection :provider-selection
   :scope-check :attenuation :revocation :secret-redaction
   :runtime-check :usage-summary :capability-family-coverage])

(def safe14-required-families
  [:package-manifest :lockfile :build-effect-summary
   :runtime-capability-summary :unsafe-summary :native-dependency
   :generated-provenance :signature-attestation :authority-diff])

(defn safe10-conformance-fixture
  [checker-state]
  (let [requirements (:safe10-capability-requirement-records checker-state)
        covered-capability-families (set (map :family requirements))
        covered (cond-> #{}
                  (seq requirements) (conj :capability-requirement)
                  (seq (:safe10-grant-intersection-records checker-state))
                  (conj :grant-intersection)
                  (seq (:safe10-provider-selection-records checker-state))
                  (conj :provider-selection)
                  (seq (:safe10-scope-check-records checker-state))
                  (conj :scope-check)
                  (seq (:safe10-attenuation-records checker-state))
                  (conj :attenuation)
                  (seq (:safe10-revocation-records checker-state))
                  (conj :revocation)
                  (seq (:safe10-secret-redaction-records checker-state))
                  (conj :secret-redaction)
                  (seq (:safe10-runtime-check-records checker-state))
                  (conj :runtime-check)
                  (seq (:safe10-usage-summaries checker-state))
                  (conj :usage-summary)
                  (set/subset? safe10-required-capability-families
                               covered-capability-families)
                  (conj :capability-family-coverage))
        missing (vec (remove covered safe10-required-families))]
    {:required-families safe10-required-families
     :required-capability-families (vec (sort-by name safe10-required-capability-families))
     :covered-families (vec (sort-by name covered))
     :covered-capability-families (vec (sort-by name covered-capability-families))
     :document :SAFE10
     :missing-families missing
     :status (if (empty? missing) :complete :incomplete)}))

(defn safe14-conformance-fixture
  [checker-state]
  (let [covered (cond-> #{}
                  (seq (:safe14-package-safety-manifests checker-state))
                  (conj :package-manifest)
                  (seq (:safe14-lockfile-records checker-state))
                  (conj :lockfile)
                  (seq (:safe14-build-effect-summaries checker-state))
                  (conj :build-effect-summary)
                  (seq (:safe14-runtime-capability-summaries checker-state))
                  (conj :runtime-capability-summary)
                  (seq (:safe14-unsafe-summaries checker-state))
                  (conj :unsafe-summary)
                  (seq (:safe14-native-dependency-records checker-state))
                  (conj :native-dependency)
                  (seq (:safe14-generated-artifact-provenance checker-state))
                  (conj :generated-provenance)
                  (seq (:safe14-signature-attestation-records checker-state))
                  (conj :signature-attestation)
                  (seq (:safe14-authority-diff-records checker-state))
                  (conj :authority-diff))
        missing (vec (remove covered safe14-required-families))]
    {:required-families safe14-required-families
     :covered-families (vec (sort-by name covered))
     :document :SAFE14
     :missing-families missing
     :status (if (empty? missing) :complete :incomplete)}))

(defn capability-supply-chain-conformance-fixture
  [checker-state]
  (let [safe10 (safe10-conformance-fixture checker-state)
        safe14 (safe14-conformance-fixture checker-state)
        statuses (map :status [safe10 safe14])]
    {:documents [:SAFE10 :SAFE14]
     :document-statuses {:SAFE10 (:status safe10)
                         :SAFE14 (:status safe14)}
     :required-families (vec (concat safe10-required-families
                                     safe14-required-families))
     :covered-families (vec (sort-by name
                                     (set (concat (:covered-families safe10)
                                                  (:covered-families safe14)))))
     :missing-families (vec (concat (:missing-families safe10)
                                    (:missing-families safe14)))
     :status (if (every? #{:complete} statuses) :complete :incomplete)}))

(def safe12-required-families
  [:macro-safety-declaration :generated-origin-chain
   :macro-build-effect-record :generated-unsafe-island
   :hygiene-capture-record :taint-capability-propagation
   :facet-output-record :alternative-engine-equivalence])

(def safe13-required-families
  [:model-call-trace :tool-call-trace :prompt-provenance
   :tool-schema-validation :human-review-record :replay-record
   :model-output-taint :generated-code-safety :memory-retention-policy])

(def safe15-required-families
  [:proof-record :certificate :check-erasure :trust-record
   :invalidation-record :imported-certificate :proof-provider
   :unsafe-wrapper-audit :backend-preservation])

(def safe16-required-documents
  #{:SAFE1 :SAFE2 :SAFE3 :SAFE4 :SAFE5 :SAFE6 :SAFE7 :SAFE8 :SAFE9
    :SAFE10 :SAFE11 :SAFE12 :SAFE13 :SAFE14 :SAFE15})

(def safe16-required-families
  [:fixture-manifest :expected-outcome :diagnostic-match
   :runtime-check-inspection :unsafe-audit-inspection
   :certificate-inspection :profile-matrix :backend-preservation
   :conformance-report :fixture-family-coverage])

(defn safe12-conformance-fixture
  [checker-state]
  (let [covered (cond-> #{}
                  (seq (:safe12-macro-safety-declarations checker-state))
                  (conj :macro-safety-declaration)
                  (seq (:safe12-generated-origin-chains checker-state))
                  (conj :generated-origin-chain)
                  (seq (:safe12-macro-build-effect-records checker-state))
                  (conj :macro-build-effect-record)
                  (seq (:safe12-generated-unsafe-island-records checker-state))
                  (conj :generated-unsafe-island)
                  (seq (:safe12-hygiene-capture-records checker-state))
                  (conj :hygiene-capture-record)
                  (seq (:safe12-taint-capability-propagation checker-state))
                  (conj :taint-capability-propagation)
                  (seq (:safe12-facet-output-records checker-state))
                  (conj :facet-output-record)
                  (seq (:safe12-alternative-engine-equivalence checker-state))
                  (conj :alternative-engine-equivalence))
        missing (vec (remove covered safe12-required-families))]
    {:required-families safe12-required-families
     :covered-families (vec (sort-by name covered))
     :document :SAFE12
     :missing-families missing
     :status (if (empty? missing) :complete :incomplete)}))

(defn safe13-conformance-fixture
  [checker-state]
  (let [covered (cond-> #{}
                  (seq (:safe13-model-call-traces checker-state))
                  (conj :model-call-trace)
                  (seq (:safe13-tool-call-traces checker-state))
                  (conj :tool-call-trace)
                  (seq (:safe13-prompt-provenance-records checker-state))
                  (conj :prompt-provenance)
                  (seq (:safe13-tool-schema-validation-records checker-state))
                  (conj :tool-schema-validation)
                  (seq (:safe13-human-review-records checker-state))
                  (conj :human-review-record)
                  (seq (:safe13-replay-records checker-state))
                  (conj :replay-record)
                  (seq (:safe13-model-output-taint-records checker-state))
                  (conj :model-output-taint)
                  (seq (:safe13-generated-code-safety-records checker-state))
                  (conj :generated-code-safety)
                  (seq (:safe13-memory-retention-policies checker-state))
                  (conj :memory-retention-policy))
        missing (vec (remove covered safe13-required-families))]
    {:required-families safe13-required-families
     :covered-families (vec (sort-by name covered))
     :document :SAFE13
     :missing-families missing
     :status (if (empty? missing) :complete :incomplete)}))

(defn safe15-conformance-fixture
  [checker-state]
  (let [covered (cond-> #{}
                  (seq (:safe15-proof-records checker-state))
                  (conj :proof-record)
                  (seq (:safe15-certificates checker-state))
                  (conj :certificate)
                  (seq (:safe15-check-erasure-records checker-state))
                  (conj :check-erasure)
                  (seq (:safe15-trust-records checker-state))
                  (conj :trust-record)
                  (seq (:safe15-invalidation-records checker-state))
                  (conj :invalidation-record)
                  (seq (:safe15-imported-certificate-verifications checker-state))
                  (conj :imported-certificate)
                  (seq (:safe15-proof-provider-records checker-state))
                  (conj :proof-provider)
                  (seq (:safe15-unsafe-wrapper-audit-views checker-state))
                  (conj :unsafe-wrapper-audit)
                  (seq (:safe15-backend-preservation-records checker-state))
                  (conj :backend-preservation))
        missing (vec (remove covered safe15-required-families))]
    {:required-families safe15-required-families
     :covered-families (vec (sort-by name covered))
     :document :SAFE15
     :missing-families missing
     :status (if (empty? missing) :complete :incomplete)}))