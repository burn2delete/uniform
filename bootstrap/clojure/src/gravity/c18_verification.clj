(ns gravity.c18-verification
  "Hosted Stage0 C18 pass-correctness/trust evidence projection."
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [gravity.compiler-verification-shared :as shared]
            [gravity.digest :as digest]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private function-operation-keys
  #{:fail! :source-span :c4-artifact-id
    :read-source-form-records :validate-ns-syntax! :parse-module
    :compiler-c17-plugin-source-artifact
    :c18-verification-source-overrides :c18-verification-fail!
    :c18-verification-validate-source-overrides!
    :c18-verification-diagnostic-stream :c18-pass-risk-records
    :c18-verification-validate! :c18-verification-capability-proof
    :compiler-c18-verification-source-artifact
    :compiler-c18-verification-file-artifact})
(def ^:private scalar-operation-keys
  #{:compiler-verification-diagnostic-messages
    :compiler-verification-override-diagnostics
    :c18-verification-governing-document
    :c18-verification-diagnostic-ids
    :c18-pass-risk-required-fields
    :c18-trust-report-required-fields})
(def ^:private operation-keys
  (into function-operation-keys scalar-operation-keys))
(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key)
    (get *operations* key)))
(defmacro ^:private definterposable [name args & body]
  (let [key (keyword name)]
    `(defn ~name ~args
       (if-let [operation# (current-operation ~key)]
         (binding [*active-operation-keys* (conj *active-operation-keys* ~key)]
           (operation# ~@args))
         (do ~@body)))))
(defn- unsupported [key]
  (fn [& _]
    (throw (ex-info (str "C18 leaf requires injected operation " key)
                    {:operation key}))))
(defn- op [key fallback] (or (get *operations* key) fallback))
(defn- fail! [id message data]
  ((op :fail! (fn [rule text payload]
                (throw (ex-info text (assoc (or payload {}) :id rule)))))
   id message data))
(defn- source-span [path index]
  ((op :source-span (fn [p i] {:source p :form-index i})) path index))
(defn- c4-artifact-id [value]
  ((op :c4-artifact-id
       (fn [candidate]
         (str "sha256:" (digest/sha256-hex (pr-str candidate)))))
   value))
(defn- read-source-form-records [path text]
  ((op :read-source-form-records (unsupported :read-source-form-records))
   path text))
(defn- validate-ns-syntax! [path forms]
  ((op :validate-ns-syntax! (unsupported :validate-ns-syntax!)) path forms))
(defn- parse-module [path forms]
  ((op :parse-module (unsupported :parse-module)) path forms))
(defn- compiler-c17-plugin-source-artifact [path text]
  ((op :compiler-c17-plugin-source-artifact
       (unsupported :compiler-c17-plugin-source-artifact))
   path text))
(def ^:private ^:dynamic compiler-verification-diagnostic-messages
  shared/compiler-verification-diagnostic-messages)
(def ^:private ^:dynamic compiler-verification-override-diagnostics
  shared/compiler-verification-override-diagnostics)

(def ^:dynamic c18-verification-governing-document
  "docs/phase-06-compiler-architecture/097-c18-compiler-verification-and-pass-correctness-strategy.md")

(def ^:dynamic c18-verification-diagnostic-ids
  ["C18-RISK"
   "C18-EVIDENCE"
   "C18-VALIDATION"
   "C18-PROOF"
   "C18-TRUST-REPORT"
   "C18-RELEASE-GATE"
   "C18-COUNTEREXAMPLE"
   "C18-PLUGIN"
   "C18-BACKEND"])

(def ^:dynamic c18-pass-risk-required-fields
  [:artifact :pass :version :risk :reason :affected-profiles
   :affected-targets :artifact-kinds :minimum-evidence :release-gate])

(def ^:dynamic c18-trust-report-required-fields
  [:artifact :compiler :profiles :targets :passes :artifact-kinds
   :known-gaps :release-gates :status])

(definterposable c18-verification-source-overrides
  [module]
  (or (get-in module [:metadata :compiler :c18-verification])
      (get-in module [:metadata :compiler :verification])
      {}))

(definterposable c18-verification-fail!
  [id source-path subject extra]
  (fail! id
         (get compiler-verification-diagnostic-messages id
              "compiler verification/pass-correctness validation failed")
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :compiler-pass-correctness
                 :stage (or (:stage subject) :c18-compiler-verification)
                 :pass-id (:pass-id subject)
                 :version (:version subject)
                 :risk-class (:risk-class subject)
                 :required-evidence (:required-evidence subject)
                 :available-evidence (:available-evidence subject)
                 :affected-profiles (:affected-profiles subject)
                 :affected-targets (:affected-targets subject)
                 :artifact-id (:artifact-id subject)
                 :remediation "Regenerate compiler verification evidence with risk classification, required evidence records, translation validation, proof or certificate replay, trust reports, release gates, counterexamples, plugin evidence, and backend conformance."}
                extra)))

(definterposable c18-verification-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (let [[id subject-kind] (get compiler-verification-override-diagnostics
                                 fail-kind)]
      (when (contains? (set c18-verification-diagnostic-ids) id)
        (c18-verification-fail!
         id source-path
         {:stage subject-kind
          :pass-id subject-kind
          :version "stage0-c18"
          :risk-class :high
          :required-evidence #{:translation-validation
                               :proof-certificate
                               :conformance-fixtures}
          :available-evidence #{}
          :affected-profiles #{:hosted :native}
          :affected-targets #{:jvm}
          :artifact-id (str "c18-verification-artifact-"
                            (name fail-kind))}
         {:missing-fields [fail-kind]})))))

(definterposable c18-verification-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/c18-verification-diagnostic-stream
   :stage :c18-compiler-verification
   :input-artifact input-id
   :ordering-key [:rule :pass :risk]
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :c18-compiler-verification
            :message-key (keyword "verification"
                                  (str/lower-case
                                   (str/replace id #"_" "-")))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "c18-verification-syntax-" index)
                      :artifact input-id}
            :related [{:role :generated-origin
                       :span (source-span source-path index)
                       :artifact :compiler-generated-verification-record}]
            :origin-chain [{:producer :c18-compiler-verification
                            :source (source-span source-path index)
                            :generated-artifact input-id}]
            :pass-id (case id
                       "C18-BACKEND" :target-lowering
                       "C18-PLUGIN" :plugin-loop-fuser
                       "C18-VALIDATION" :bounds-check-elide
                       :compiler-verification)
            :version "stage0-c18"
            :risk-class (if (#{"C18-RISK" "C18-TRUST-REPORT"
                                "C18-RELEASE-GATE"}
                              id)
                          :critical
                          :high)
            :required-evidence #{:translation-validation
                                 :proof-or-certificate
                                 :conformance-fixtures}
            :available-evidence #{:fixtures}
            :affected-profiles #{:hosted :native}
            :affected-targets #{:jvm}
            :source-or-artifact-id input-id
            :facts {:rule id
                    :release-blocking? true
                    :evidence-policy :risk-based}
            :remediation [{:kind :add-required-evidence}
                          {:kind :block-release-artifact}]
            :redactions []
            :ordering-key [id :compiler-verification :high]})
         c18-verification-diagnostic-ids
         (range))
   :status :complete})

(definterposable c18-pass-risk-records
  []
  [{:artifact :gravity/pass-risk
    :pass :reader
    :version "stage0-c18"
    :risk :critical
    :reason #{:trusted-semantic-base}
    :affected-profiles #{:core :hosted :native}
    :affected-targets #{:jvm}
    :artifact-kinds #{:syntax-object-stream}
    :minimum-evidence #{:golden-fixtures :round-trip :fuzz}
    :release-gate :required}
   {:artifact :gravity/pass-risk
    :pass :macro-expansion
    :version "stage0-c18"
    :risk :critical
    :reason #{:generated-code :build-effects}
    :affected-profiles #{:hosted :native}
    :affected-targets #{:jvm}
    :artifact-kinds #{:macro-expansion-trace :expanded-syntax}
    :minimum-evidence #{:hygiene-fixtures :generated-origin-fixtures
                        :build-effect-tests}
    :release-gate :required}
   {:artifact :gravity/pass-risk
    :pass :type-effect-check
    :version "stage0-c18"
    :risk :high
    :reason #{:semantic-legality :effect-capability-policy}
    :affected-profiles #{:hosted :native :kernel}
    :affected-targets #{:jvm}
    :artifact-kinds #{:typed-core :effect-graph}
    :minimum-evidence #{:positive-negative-fixtures :property-tests}
    :release-gate :required}
   {:artifact :gravity/pass-risk
    :pass :ownership-safety
    :version "stage0-c18"
    :risk :critical
    :reason #{:safety-outcomes :unsafe-boundary}
    :affected-profiles #{:hosted :native :kernel :firmware}
    :affected-targets #{:jvm}
    :artifact-kinds #{:ownership-graph :safety-report}
    :minimum-evidence #{:vulnerability-fixtures :proof-certificate-checks}
    :release-gate :required}
   {:artifact :gravity/pass-risk
    :pass :mir-construction
    :version "stage0-c18"
    :risk :high
    :reason #{:core-to-mir-semantics}
    :affected-profiles #{:hosted :native}
    :affected-targets #{:jvm}
    :artifact-kinds #{:gravity/mir}
    :minimum-evidence #{:mir-verifier :core-to-mir-goldens}
    :release-gate :required}
   {:artifact :gravity/pass-risk
    :pass :bounds-check-elide
    :version "stage0-c18"
    :risk :high
    :reason #{:removes-runtime-checks :depends-on-proof}
    :affected-profiles #{:native :kernel :firmware :gpu}
    :affected-targets #{:jvm :native}
    :artifact-kinds #{:optimized-mir :safety-outcome}
    :minimum-evidence #{:translation-validation
                        :proof-dominance-check}
    :release-gate :required}
   {:artifact :gravity/pass-risk
    :pass :target-lowering
    :version "stage0-c18"
    :risk :critical
    :reason #{:emits-backend-artifacts :profile-runtime-contract}
    :affected-profiles #{:hosted :native}
    :affected-targets #{:jvm}
    :artifact-kinds #{:target-artifact-manifest}
    :minimum-evidence #{:backend-conformance :differential-execution}
    :release-gate :required}
   {:artifact :gravity/pass-risk
    :pass :plugin-loop-fuser
    :version "stage0-c18"
    :risk :medium
    :reason #{:plugin-ir-transform}
    :affected-profiles #{:hosted :native}
    :affected-targets #{:jvm}
    :artifact-kinds #{:plugin-output-artifact}
    :minimum-evidence #{:sandbox-tests :contract-verifier
                        :fixture-suite}
    :release-gate :required}])

(definterposable c18-verification-validate!
  [source-path artifact]
  (let [risk-records (:pass-risk-classification artifact)
        risk-passes (set (map :pass risk-records))
        evidence-passes (set (map :pass (:pass-evidence-records artifact)))
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:verification-diagnostic-stream
                                       :diagnostics])))
        trust-fields (set (keys (:compiler-trust-report artifact)))]
    (doseq [risk risk-records]
      (when-not (set/subset? (set c18-pass-risk-required-fields)
                             (set (keys risk)))
        (c18-verification-fail!
         "C18-RISK" source-path
         {:pass-id (:pass risk)
          :version (:version risk)
          :risk-class (:risk risk)
          :required-evidence (:minimum-evidence risk)
          :available-evidence #{}
          :affected-profiles (:affected-profiles risk)
          :affected-targets (:affected-targets risk)}
         {:missing-fields
          (vec (remove (set (keys risk))
                       c18-pass-risk-required-fields))})))
    (when-not (= risk-passes evidence-passes)
      (c18-verification-fail! "C18-EVIDENCE" source-path
                              (first risk-records)
                              {:missing-fields [:pass-evidence-records]}))
    (when-not (every? #(= :accepted (:result %))
                      (:translation-validation-logs artifact))
      (c18-verification-fail! "C18-VALIDATION" source-path
                              (first (:translation-validation-logs artifact))
                              {:missing-fields [:accepted-validation]}))
    (when-not (every? #(= :accepted (:status %))
                      (:proof-or-certificate-references artifact))
      (c18-verification-fail! "C18-PROOF" source-path
                              (first (:proof-or-certificate-references
                                      artifact))
                              {:missing-fields [:accepted-proof]}))
    (when-not (set/subset? (set c18-trust-report-required-fields)
                           trust-fields)
      (c18-verification-fail! "C18-TRUST-REPORT" source-path
                              (:compiler-trust-report artifact)
                              {:missing-fields
                               (vec (remove trust-fields
                                            c18-trust-report-required-fields))}))
    (when-not (every? #(= :blocked (:release-artifact-status %))
                      (:release-gate-failure-fixtures artifact))
      (c18-verification-fail! "C18-RELEASE-GATE" source-path
                              (first (:release-gate-failure-fixtures
                                      artifact))
                              {:missing-fields [:blocked-release]}))
    (when-not (every? #(and (= :captured (:status %))
                            (true? (:regression-fixture-created? %)))
                      (:counterexample-artifacts artifact))
      (c18-verification-fail! "C18-COUNTEREXAMPLE" source-path
                              (first (:counterexample-artifacts artifact))
                              {:missing-fields [:regression-fixture]}))
    (when-not (= :passed (get-in artifact [:plugin-evidence-report :status]))
      (c18-verification-fail! "C18-PLUGIN" source-path
                              (:plugin-evidence-report artifact)
                              {:missing-fields [:plugin-evidence-report]}))
    (when-not (every? #(= :passed (:status %))
                      (:target-lowering-conformance artifact))
      (c18-verification-fail! "C18-BACKEND" source-path
                              (first (:target-lowering-conformance artifact))
                              {:missing-fields [:target-lowering-conformance]}))
    (when-not (= (set c18-verification-diagnostic-ids) diagnostics)
      (c18-verification-fail! "C18-EVIDENCE" source-path
                              (:verification-diagnostic-stream artifact)
                              {:missing-fields [:verification-diagnostics]})))
  :complete)

(definterposable c18-verification-capability-proof
  [artifact]
  (let [risk-records (:pass-risk-classification artifact)
        evidence-records (:pass-evidence-records artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:verification-diagnostic-stream
                                       :diagnostics])))]
    {:c17-plugin-input-verified?
     (= :complete (get-in artifact
                          [:c17-plugin-artifact
                           :capability-based-proof :status]))
     :pass-risk-classification-complete?
     (every? #(set/subset? (set c18-pass-risk-required-fields)
                           (set (keys %)))
             risk-records)
     :high-risk-evidence-present?
     (every? (fn [risk]
               (let [record (first (filter #(= (:pass %)
                                               (:pass risk))
                                           evidence-records))]
                 (and record
                      (= :present (:status record))
                      (set/subset? (:minimum-evidence risk)
                                   (:evidence record)))))
             (filter #(#{:high :critical} (:risk %)) risk-records))
     :translation-validation-accepted?
     (every? #(= :accepted (:result %))
             (:translation-validation-logs artifact))
     :proofs-and-certificates-accepted?
     (every? #(= :accepted (:status %))
             (:proof-or-certificate-references artifact))
     :diagnostics-preserve-source-and-generated-origin?
     (every? #(and (get-in % [:primary :span])
                   (seq (:related %))
                   (seq (:origin-chain %)))
             (get-in artifact [:verification-diagnostic-stream
                               :diagnostics]))
     :differential-and-property-fixtures-passed?
     (= :passed (get-in artifact
                        [:differential-and-property-fixture-results
                         :status]))
     :trust-report-complete?
     (= :complete (get-in artifact [:compiler-trust-report :status]))
     :release-gate-blocks-missing-evidence?
     (every? #(= :blocked (:release-artifact-status %))
             (:release-gate-failure-fixtures artifact))
     :counterexample-regression-captured?
     (every? #(and (= :captured (:status %))
                   (true? (:regression-fixture-created? %)))
             (:counterexample-artifacts artifact))
     :experimental-gates-explicit?
     (every? #(= :explicit-feature-gate (:gate %))
             (:experimental-pass-gates artifact))
     :plugin-evidence-policy-passed?
     (= :passed (get-in artifact [:plugin-evidence-report :status]))
     :backend-conformance-passed?
     (every? #(= :passed (:status %))
             (:target-lowering-conformance artifact))
     :diagnostics-covered?
     (= (set c18-verification-diagnostic-ids) diagnostics)
     :status :complete}))

(definterposable compiler-c18-verification-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (c18-verification-source-overrides module)
        _ (c18-verification-validate-source-overrides! source-path
                                                       source-overrides)
        plugin-artifact (compiler-c17-plugin-source-artifact source-path
                                                             source-text)
        input-id (:artifact-id plugin-artifact)
        risk-records (c18-pass-risk-records)
        pass-evidence-records
        (mapv (fn [risk]
                {:artifact :gravity/pass-evidence
                 :pass (:pass risk)
                 :version (:version risk)
                 :risk (:risk risk)
                 :required (:minimum-evidence risk)
                 :evidence (:minimum-evidence risk)
                 :evidence-artifacts [(str "sha256:c18-evidence-"
                                           (name (:pass risk)))]
                 :status :present})
              risk-records)
        translation-validation-logs
        [{:artifact :gravity/translation-validation
          :pass :bounds-check-elide
          :input input-id
          :output "sha256:c18-bounds-check-elide-output"
          :changed-functions [:safe-index]
          :properties #{:same-observable-result :same-effects
                        :same-safety-outcomes}
          :method :symbolic-plus-fixtures
          :proofs [:proof/c18-bounds-check-dominance]
          :counterexamples []
          :result :accepted}
         {:artifact :gravity/translation-validation
          :pass :plugin-loop-fuser
          :input input-id
          :output "sha256:c18-loop-fuser-output"
          :changed-functions [:loop-body]
          :properties #{:same-observable-result :same-effects}
          :method :differential-fixtures
          :proofs [:proof/c18-loop-fuser-effect-order]
          :counterexamples []
          :result :accepted}]
        diagnostic-stream (c18-verification-diagnostic-stream source-path
                                                              input-id)
        artifact-base
        {:kind :gravity/stage0-c18-compiler-verification-artifact
         :task "P06-D097"
         :document-set ["C18"]
         :governing-document c18-verification-governing-document
         :pass {:name :c18-compiler-verification
                :input :compiler-plugin-artifact
                :output :compiler-verification-and-trust-artifact
                :requires [:c17-plugin-artifact :risk-policy
                           :evidence-policy :translation-validation
                           :proof-certificate-policy :trust-report-policy
                           :release-gate-policy]
                :preserves [:source-spans :generated-origins :profile
                            :target :diagnostics :proofs :capabilities]
                :emits [:compiler-verification-plan
                        :pass-risk-classification
                        :pass-evidence-records
                        :translation-validation-logs
                        :proof-or-certificate-references
                        :differential-and-property-fixture-results
                        :compiler-trust-report :release-gate-report
                        :release-gate-failure-fixtures
                        :counterexample-artifacts
                        :experimental-pass-gates
                        :plugin-evidence-report
                        :target-lowering-conformance
                        :verification-diagnostic-stream]
                :rejects c18-verification-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :c17-plugin-artifact
         (select-keys plugin-artifact
                      [:kind :task :artifact-id :governing-document
                       :capability-based-proof])
         :plugin-artifact-kind (:kind plugin-artifact)
         :plugin-artifact-hash input-id
         :compiler-verification-plan
         {:artifact :gravity/compiler-verification-plan
          :status :complete
          :evidence-policy :risk-based
          :passes (mapv :pass risk-records)
          :required-evidence-families
          #{:golden-fixtures :fuzz :translation-validation
            :proof-dominance-check :backend-conformance
            :differential-execution :sandbox-tests :contract-verifier}
          :release-policy :block-affected-profiles-on-failure}
         :pass-risk-classification risk-records
         :pass-evidence-records pass-evidence-records
         :stage-verifier-reports
         (mapv (fn [risk]
                 {:artifact :gravity/stage-verifier-report
                  :pass (:pass risk)
                  :artifact-kinds (:artifact-kinds risk)
                  :source-or-generated-origin-preserved? true
                  :status :passed})
               risk-records)
         :translation-validation-logs translation-validation-logs
         :proof-or-certificate-references
         [{:proof :proof/c18-bounds-check-dominance
           :pass :bounds-check-elide
           :status :accepted}
          {:certificate :cert/c18-safety-check-elision
           :pass :bounds-check-elide
           :status :accepted}
          {:proof :proof/c18-loop-fuser-effect-order
           :pass :plugin-loop-fuser
           :status :accepted}]
         :differential-and-property-fixture-results
         {:artifact :gravity/compiler-fixture-results
          :status :passed
          :families {:front-end {:golden 8 :fuzz 64 :status :passed}
                     :optimization {:translation-validation 2
                                    :property 12
                                    :status :passed}
                     :backend {:differential 5
                               :conformance 4
                               :status :passed}}}
         :compiler-trust-report
         {:artifact :gravity/compiler-trust-report
          :compiler "gravity-stage0-clojure"
          :profiles {:hosted {:required-evidence :high
                              :blocked-passes []}
                     :native {:required-evidence :critical
                              :blocked-passes [:gpu-lowering]}}
          :targets {:jvm {:required-evidence :high
                          :blocked-passes []}}
          :passes (mapv #(select-keys % [:pass :risk
                                          :minimum-evidence
                                          :release-gate])
                        risk-records)
          :artifact-kinds
          (set (mapcat :artifact-kinds risk-records))
          :known-gaps [{:pass :gpu-lowering
                        :profiles #{:gpu}
                        :targets #{:gpu}
                        :status :experimental
                        :gate :explicit-feature-gate}]
          :release-gates [:verifiers :high-risk-evidence
                          :target-lowering-conformance
                          :stale-proof-rejection
                          :diagnostic-goldens
                          :self-hosting-comparison]
          :status :complete}
         :release-gate-report
         {:artifact :gravity/release-gate-report
          :status :passed
          :checks [:verifier-pass-every-artifact
                   :no-active-critical-failures
                   :high-risk-evidence-present
                   :target-lowering-conformance
                   :stale-proof-and-certificate-rejection
                   :diagnostic-golden-fixtures]
          :release-artifacts :allowed-for-hosted-jvm
          :blocked-experimental-passes [:gpu-lowering]}
         :release-gate-failure-fixtures
         [{:artifact :gravity/release-gate-failure
           :pass :gpu-lowering
           :missing-evidence #{:backend-conformance
                               :differential-execution}
           :affected-profiles #{:gpu}
           :affected-targets #{:gpu}
           :diagnostic "C18-RELEASE-GATE"
           :release-artifact-status :blocked}]
         :counterexample-artifacts
         [{:artifact :gravity/counterexample
           :source-fixture "bootstrap/clojure/fixtures/rejected/compiler-verify-c18-validation.gravity"
           :input-artifact input-id
           :output-artifact "sha256:c18-invalid-optimization-output"
           :violated-property :same-safety-outcomes
           :diagnostic-stream :gravity/c18-verification-diagnostic-stream
           :minimized-reproducer "compiler-verify-c18-validation.gravity"
           :affected-pass :bounds-check-elide
           :pass-version "stage0-c18"
           :status :captured
           :regression-fixture-created? true}]
         :experimental-pass-gates
         [{:pass :gpu-lowering
           :profiles #{:gpu}
           :targets #{:gpu}
           :gate :explicit-feature-gate
           :release-default :disabled
           :artifact-status :not-release-quality}]
         :plugin-evidence-report
         {:artifact :gravity/plugin-evidence-report
          :plugin 'gravity.plugins.stage0/loop-fuser
          :required #{:sandbox-tests :contract-verifier
                      :fixture-suite}
          :available #{:sandbox-tests :contract-verifier
                       :fixture-suite}
          :status :passed}
         :target-lowering-conformance
         [{:artifact :gravity/target-lowering-conformance
           :target :jvm
           :profiles #{:hosted}
           :source-core-mir-intent #{:same-observable-result
                                     :same-effects
                                     :same-safety-outcomes}
           :emitted-artifact-behavior :matched
           :method :differential-execution
           :status :passed}]
         :verification-diagnostic-stream diagnostic-stream
         :c18-verification-results
         {:documents ["C18"]
          :task "P06-D097"
          :required-diagnostic-ids c18-verification-diagnostic-ids
          :c17-input-status :complete
          :plan-status :complete
          :risk-status :complete
          :evidence-status :complete
          :translation-validation-status :complete
          :proof-certificate-status :complete
          :fixture-status :complete
          :trust-report-status :complete
          :release-gate-status :complete
          :counterexample-status :complete
          :experimental-gate-status :complete
          :plugin-status :complete
          :backend-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (c18-verification-validate! source-path artifact-base)
        capability-proof (c18-verification-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(definterposable compiler-c18-verification-file-artifact
  [path]
  (compiler-c18-verification-source-artifact path (slurp path)))

(def ^:private namespace-contract
  {:contract-boundary :hosted-stage0-c18-verification-evidence
   :dependency-direction
   {:requires ['clojure.set 'clojure.string
               'gravity.compiler-verification-shared 'gravity.digest]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :owns [:hosted-stage0-c18-risk-classification
          :hosted-stage0-c18-trust-evidence]
   :does-not-own [:canonical-c18-authority :source-authentication
                  :proof-checking-authority :translation-validation-authority
                  :certificate-authority :trust-report-authority
                  :release-gate-authority :release-authorization
                  :backend-conformance-authority :plugin-evidence-authority
                  :equivalence :self-hosting :release :seed-retirement]
   :compatibility-only? true
   :evidence-authoritative? false
   :release-authority? false
   :verification-model-complete? false
   :canonical-c18-authority? false
   :operation-interposition
   {:accepted-keys operation-keys
    :unknown-keys-rejected? true
    :partial-overrides? true
    :single-binding-per-top-level-call? true}})
(defn- string-vector? [value]
  (and (vector? value) (seq value) (every? string? value)))
(defn- keyword-vector? [value]
  (and (vector? value) (seq value) (every? keyword? value)))
(defn- string-map? [value]
  (and (map? value)
       (every? (fn [[key entry]] (and (string? key) (string? entry))) value)))
(defn- override-map? [value]
  (and (map? value)
       (every? (fn [[key entry]]
                 (and (keyword? key) (vector? entry) (= 2 (count entry))
                      (string? (first entry)) (keyword? (second entry))))
               value)))
(defn- validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C18 operation map must be a map" {:value operations})))
  (let [unknown (seq (remove operation-keys (keys operations)))
        invalid (seq (for [[key value]
                           (select-keys operations function-operation-keys)
                           :when (not (fn? value))]
                       key))]
    (when unknown
      (throw (ex-info "C18 operation map contains unknown keys"
                      {:unknown-keys (vec unknown)})))
    (when invalid
      (throw (ex-info "C18 function operations must be functions"
                      {:non-function-keys (vec invalid)}))))
  (doseq [[key predicate]
          [[:compiler-verification-diagnostic-messages string-map?]
           [:compiler-verification-override-diagnostics override-map?]
           [:c18-verification-governing-document
            #(and (string? %) (seq %))]
           [:c18-verification-diagnostic-ids string-vector?]
           [:c18-pass-risk-required-fields keyword-vector?]
           [:c18-trust-report-required-fields keyword-vector?]]
          :when (and (contains? operations key)
                     (not (predicate (get operations key))))]
    (throw (ex-info "C18 scalar operation has invalid shape" {:key key})))
  operations)
(defn with-operations [operations thunk]
  (validate-operations! operations)
  (let [merged (merge *operations* operations)]
    (binding [*operations* merged
              compiler-verification-diagnostic-messages
              (get merged :compiler-verification-diagnostic-messages
                   compiler-verification-diagnostic-messages)
              compiler-verification-override-diagnostics
              (get merged :compiler-verification-override-diagnostics
                   compiler-verification-override-diagnostics)
              c18-verification-governing-document
              (get merged :c18-verification-governing-document
                   c18-verification-governing-document)
              c18-verification-diagnostic-ids
              (get merged :c18-verification-diagnostic-ids
                   c18-verification-diagnostic-ids)
              c18-pass-risk-required-fields
              (get merged :c18-pass-risk-required-fields
                   c18-pass-risk-required-fields)
              c18-trust-report-required-fields
              (get merged :c18-trust-report-required-fields
                   c18-trust-report-required-fields)]
      (thunk))))
(def public-api
  {'public-api {:kind :contract}
   'with-operations {:arglists '([operations thunk])}
   'c18-engine-contract {:arglists '([])}
   'c18-verification-governing-document {:kind :constant}
   'c18-verification-diagnostic-ids {:kind :constant}
   'c18-pass-risk-required-fields {:kind :constant}
   'c18-trust-report-required-fields {:kind :constant}
   'c18-verification-source-overrides {:arglists '([module])}
   'c18-verification-fail! {:arglists '([id source-path subject extra])}
   'c18-verification-validate-source-overrides!
   {:arglists '([source-path overrides])}
   'c18-verification-diagnostic-stream
   {:arglists '([source-path input-id])}
   'c18-pass-risk-records {:arglists '([])}
   'c18-verification-validate! {:arglists '([source-path artifact])}
   'c18-verification-capability-proof {:arglists '([artifact])}
   'compiler-c18-verification-source-artifact
   {:arglists '([source-path source-text])}
   'compiler-c18-verification-file-artifact {:arglists '([path])}})
(defn c18-engine-contract []
  (assoc namespace-contract :public-api public-api))
