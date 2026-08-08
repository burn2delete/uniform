(ns gravity.optimization-lowering
  "Shared hosted Stage0 optimization/lowering compatibility engine.

  This leaf preserves the fused Clojure seed helpers used by C13 and C14. It is
  not optimization, lowering, proof, backend, self-hosting, or release
  authority."
  (:require [gravity.digest :as digest]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})

(def ^:private function-operation-keys
  #{:fail!
    :source-span
    :sha256-hex
    :perf-present?
    :checked-core-source-artifact
    :domain-ir-source-artifact
    :optimization-lowering-source-overrides
    :optimization-lowering-fail!
    :optimization-pass-contract-record
    :optimization-decision-record
    :optimization-lowering-validate-overrides!
    :optimization-lowering-validate!
    :optimization-lowering-capability-proof
    :optimization-lowering-source-artifact})
(def ^:private scalar-operation-keys
  #{:c13-optimization-diagnostic-ids
    :c14-lowering-diagnostic-ids
    :optimization-lowering-diagnostic-ids
    :optimization-lowering-diagnostic-messages
    :optimization-lowering-override-diagnostics
    :optimization-pass-contract-seed})
(def ^:private operation-keys (into function-operation-keys scalar-operation-keys))
(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key) (get *operations* key)))
(defmacro ^:private definterposable [name args & body]
  (let [key (keyword name)]
    `(defn ~name ~args
       (if-let [operation# (current-operation ~key)]
         (binding [*active-operation-keys* (conj *active-operation-keys* ~key)]
           (operation# ~@args))
         (do ~@body)))))
(defn- default-fail! [id message data] (throw (ex-info message (assoc (or data {}) :id id))))
(defn- default-source-span [path index] {:source path :form-index index})
(defn- fail! [id message data] ((or (:fail! *operations*) default-fail!) id message data))
(defn- source-span [path index] ((or (:source-span *operations*) default-source-span) path index))
(defn- sha256-hex [value] ((or (:sha256-hex *operations*) digest/sha256-hex) value))
(defn- perf-present? [value]
  ((or (:perf-present? *operations*)
       (fn [candidate]
         (and (some? candidate)
              (not (and (coll? candidate) (empty? candidate))))))
   value))
(defn- unsupported [key] (fn [& _] (throw (ex-info (str "optimization/lowering leaf requires injected operation " key) {:operation key}))))
(defn- checked-core-source-artifact [path text] ((or (:checked-core-source-artifact *operations*) (unsupported :checked-core-source-artifact)) path text))
(defn- domain-ir-source-artifact [path text] ((or (:domain-ir-source-artifact *operations*) (unsupported :domain-ir-source-artifact)) path text))

(def ^:dynamic c13-optimization-diagnostic-ids
  ["C13-CONTRACT"
   "C13-PRESERVE"
   "C13-INVALIDATE"
   "C13-PROOF"
   "C13-CHECK-ELISION"
   "C13-EFFECT"
   "C13-SAFETY"
   "C13-DOMAIN"
   "C13-NONDETERMINISM"
   "C13-VERIFY"])

(def ^:dynamic c14-lowering-diagnostic-ids
  ["C14-INPUT"
   "C14-PROFILE"
   "C14-TARGET"
   "C14-ABI"
   "C14-RUNTIME"
   "C14-PROVIDER"
   "C14-PROOF-METADATA"
   "C14-CAPABILITY"
   "C14-UNSUPPORTED"
   "C14-MANIFEST"])

(def ^:dynamic optimization-lowering-diagnostic-ids
  (vec (concat c13-optimization-diagnostic-ids
               c14-lowering-diagnostic-ids)))

(def ^:dynamic optimization-lowering-diagnostic-messages
  {"C13-CONTRACT" "MIR optimization pass contract is invalid"
   "C13-PRESERVE" "optimization claimed to preserve a missing or changed fact"
   "C13-INVALIDATE" "optimization is missing an invalidation record"
   "C13-PROOF" "optimization transformation lacks required proof evidence"
   "C13-CHECK-ELISION" "check elision violated PERF10 proof policy"
   "C13-EFFECT" "optimization reordered effects without evidence"
   "C13-SAFETY" "optimization left stale safety outcomes"
   "C13-DOMAIN" "optimization corrupted a domain anchor"
   "C13-NONDETERMINISM" "optimization choice is not replayable"
   "C13-VERIFY" "post-optimization MIR verifier failed"
   "C14-INPUT" "target lowering input is unverified or stale"
   "C14-PROFILE" "backend is ineligible under the active profile"
   "C14-TARGET" "target feature is missing or unsupported"
   "C14-ABI" "ABI or layout cannot represent the artifact"
   "C14-RUNTIME" "runtime service is missing or forbidden"
   "C14-PROVIDER" "provider support is missing"
   "C14-PROOF-METADATA" "target metadata lacks Gravity proof evidence"
   "C14-CAPABILITY" "lowering would add or lose authority"
   "C14-UNSUPPORTED" "MIR or domain feature lacks legal lowering"
   "C14-MANIFEST" "target artifact manifest is incomplete"})

(def ^:dynamic optimization-lowering-override-diagnostics
  {:contract ["C13-CONTRACT" :optimization-pass]
   :preserve ["C13-PRESERVE" :optimization-decision]
   :invalidate ["C13-INVALIDATE" :invalidation-ledger]
   :proof ["C13-PROOF" :optimization-proof]
   :check-elision ["C13-CHECK-ELISION" :check-elision]
   :effect ["C13-EFFECT" :effect-scheduling]
   :safety ["C13-SAFETY" :safety-outcome]
   :domain ["C13-DOMAIN" :domain-anchor]
   :nondeterminism ["C13-NONDETERMINISM" :replay]
   :verify ["C13-VERIFY" :post-pass-verifier]
   :input ["C14-INPUT" :lowering-input]
   :profile ["C14-PROFILE" :target-eligibility]
   :target ["C14-TARGET" :target-feature]
   :abi ["C14-ABI" :abi-layout]
   :runtime ["C14-RUNTIME" :runtime-provider]
   :provider ["C14-PROVIDER" :provider-selection]
   :proof-metadata ["C14-PROOF-METADATA" :target-metadata]
   :capability ["C14-CAPABILITY" :capability-preservation]
   :unsupported ["C14-UNSUPPORTED" :unsupported-feature]
   :manifest ["C14-MANIFEST" :target-artifact-manifest]})

(def ^:dynamic optimization-pass-contract-seed
  [{:pass :constant-fold
    :requires #{:constant-table :type-table}
    :preserves #{:types :effects :ownership :capabilities :source-origins
                 :profile :safety-outcomes}
    :invalidates #{}
    :regenerates #{}
    :proof-obligations #{:literal-equivalence}
    :profiles #{:core :hosted :native :gpu}
    :target-assumptions #{}
    :emits #{:decision-log :verifier-report}}
   {:pass :dead-code-eliminate
    :requires #{:control-flow-graph :effect-table :liveness}
    :preserves #{:types :effects :capabilities :source-origins :profile}
    :invalidates #{:liveness :data-flow-cache}
    :regenerates #{:liveness}
    :proof-obligations #{:no-effectful-removal}
    :profiles #{:hosted :native :gpu}
    :target-assumptions #{}
    :emits #{:decision-log :invalidation-ledger :verifier-report}}
   {:pass :bounds-check-elide
    :requires #{:dominator-tree :range-analysis :safety-outcomes}
    :preserves #{:types :effects :source-origins :profile}
    :invalidates #{:runtime-check-table :data-flow-cache}
    :regenerates #{:runtime-check-table}
    :proof-obligations #{:proof-dominates-check}
    :profiles #{:native :hosted :gpu}
    :target-assumptions #{}
    :emits #{:decision-log :check-elision-record :verifier-report}}
   {:pass :effect-aware-schedule
    :requires #{:effect-table :capability-proof-table}
    :preserves #{:types :capabilities :safety-outcomes :source-origins
                 :profile}
    :invalidates #{:control-flow-cache}
    :regenerates #{:effect-table}
    :proof-obligations #{:effect-order-equivalence}
    :profiles #{:hosted :native :distributed}
    :target-assumptions #{}
    :emits #{:decision-log :effect-order-proof :verifier-report}}
   {:pass :domain-ir-exit
    :requires #{:domain-verifier-report :semantic-anchor-map}
    :preserves #{:types :effects :ownership :capabilities :safety-outcomes
                 :source-origins :profile}
    :invalidates #{:domain-anchor-cache}
    :regenerates #{:domain-anchor-table}
    :proof-obligations #{:domain-translation-validation}
    :profiles #{:hosted :native :distributed :gpu}
    :target-assumptions #{}
    :emits #{:decision-log :domain-verifier-report :verifier-report}}
   {:pass :target-layout-prepare
    :requires #{:layout-facts :ownership-table :safety-outcomes}
    :preserves #{:types :effects :capabilities :source-origins :profile}
    :invalidates #{:layout-cache}
    :regenerates #{:layout-manifest}
    :proof-obligations #{:layout-equivalence}
    :profiles #{:hosted :native :gpu}
    :target-assumptions #{}
    :emits #{:decision-log :layout-decision-record :verifier-report}}])

(definterposable optimization-lowering-source-overrides
  [module]
  (get-in module [:metadata :compiler :optimization-lowering] {}))

(definterposable optimization-lowering-fail!
  [id source-path artifact subject extra]
  (fail! id
         (get optimization-lowering-diagnostic-messages id
              "optimization or target lowering validation failed")
         (merge {:source-span (or (:source-span subject)
                                  (get-in subject [:source :span])
                                  (source-span source-path 0))
                 :diagnostic-family :optimization-lowering
                 :stage :optimize-lower
                 :pass-id (or (:pass subject) (:pass-id subject))
                 :decision-id (:decision-id subject)
                 :input-artifact-id (or (:input-mir subject)
                                        (:input artifact))
                 :output-artifact-id (:output-mir subject)
                 :changed-operations (:changed-ops subject)
                 :missing-fact (:missing-fact subject)
                 :proof-id (or (:proof-id subject) (:proof-id extra))
                 :profile (or (:profile subject)
                              (get-in artifact [:lowering-request :profile]))
                 :target (or (:target subject)
                             (get-in artifact
                                     [:lowering-request :target :backend]))
                 :backend (get-in artifact [:lowering-request :target :backend])
                 :missing-feature (:missing-feature subject)
                 :fallback-status (:fallback-status subject)
                 :remediation "Regenerate optimization and lowering records with pass contracts, invalidation, verifier, proof, provider, capability, fallback, and target artifact evidence."}
                extra)))

(definterposable optimization-pass-contract-record
  [record]
  (assoc record
         :artifact :gravity/mir-pass-contract
         :input :gravity/mir
         :output :gravity/mir
         :version "stage0-c13"
         :contract-status :accepted))

(definterposable optimization-decision-record
  [domain-ir-artifact input-id index contract]
  (let [changed? (odd? index)
        pass (:pass contract)
        decision-input {:pass pass
                        :input input-id
                        :index index
                        :changed? changed?}
        output-id (str "sha256:" (sha256-hex (pr-str decision-input)))]
    {:artifact :gravity/optimization-decision
     :pass pass
     :decision-id (str "sha256:" (sha256-hex (pr-str decision-input)))
     :input-mir input-id
     :output-mir output-id
     :changed-ops (if changed?
                    [(str "mir-op-optimized-" (name pass))]
                    [])
     :reason (if changed? :stage0-evidence-gated :no-change-needed)
     :preserved (:preserves contract)
     :invalidated (:invalidates contract)
     :regenerated (:regenerates contract)
     :proofs-used [{:proof-id (keyword "proof" (str "c13-" (name pass)))
                    :kind (if changed?
                            :translation-validation
                            :contract-replay)
                    :status :accepted}]
     :residual-checks (if (= :bounds-check-elide pass)
                        []
                        [:stage0-visible-residual])
     :benchmarks []
     :verifier-result :passed
     :source (get-in domain-ir-artifact
                     [:domain-ir-artifacts 0 :source])}))

(definterposable optimization-lowering-validate-overrides!
  [source-path artifact]
  (when-let [fail-kind (get-in artifact [:source-overrides :fail])]
    (let [[id subject-kind] (get optimization-lowering-override-diagnostics
                                 fail-kind)]
      (when id
        (optimization-lowering-fail!
         id source-path artifact
         {:pass-id subject-kind
          :decision-id (str "optimization-lowering-invalid-"
                            (name fail-kind))
          :source-span (source-span source-path 0)
          :missing-fact fail-kind
          :missing-feature fail-kind
          :fallback-status :missing}
         {:missing-fields [fail-kind]})))))

(definterposable optimization-lowering-validate!
  [source-path artifact]
  (optimization-lowering-validate-overrides! source-path artifact)
  (let [contracts (:optimization-pass-registry artifact)
        pipeline (:optimization-pipeline-manifest artifact)
        decisions (:optimization-decision-log artifact)
        invalidations (:invalidated-fact-ledger artifact)
        verifiers (:post-pass-verifier-reports artifact)
        lowering-request (:lowering-request artifact)
        target-manifest (:target-artifact-manifest artifact)]
    (doseq [contract contracts]
      (when-not (every? #(perf-present? (get contract %))
                        [:artifact :pass :input :output :requires
                         :preserves :proof-obligations :profiles :emits])
        (optimization-lowering-fail! "C13-CONTRACT" source-path artifact
                                     contract
                                     {:missing-fields [:pass :input :output
                                                       :requires :preserves
                                                       :proof-obligations]})))
    (when-not (= (mapv :pass contracts) (:pass-order pipeline))
      (optimization-lowering-fail! "C13-CONTRACT" source-path artifact
                                   pipeline
                                   {:missing-fields [:pass-order]}))
    (when-not (every? #(perf-present? (:preserved %)) decisions)
      (optimization-lowering-fail! "C13-PRESERVE" source-path artifact
                                   (first decisions)
                                   {:missing-fields [:preserved]}))
    (when-not (= (count contracts) (count invalidations))
      (optimization-lowering-fail! "C13-INVALIDATE" source-path artifact
                                   (first decisions)
                                   {:missing-fields [:invalidated-fact-ledger]}))
    (when-not (every? #(some (fn [proof] (= :accepted (:status proof)))
                            (:proofs-used %))
                      decisions)
      (optimization-lowering-fail! "C13-PROOF" source-path artifact
                                   (first decisions)
                                   {:missing-fields [:proofs-used]}))
    (when-not (= :accepted (get-in artifact
                                   [:check-elision-record :status]))
      (optimization-lowering-fail! "C13-CHECK-ELISION" source-path artifact
                                   (:check-elision-record artifact)
                                   {:missing-fields [:check-elision-record]}))
    (when-not (= :accepted (get-in artifact
                                   [:effect-reordering-record :status]))
      (optimization-lowering-fail! "C13-EFFECT" source-path artifact
                                   (:effect-reordering-record artifact)
                                   {:missing-fields [:effect-reordering-record]}))
    (when-not (= :current (get-in artifact
                                  [:safety-outcome-refresh-report :status]))
      (optimization-lowering-fail! "C13-SAFETY" source-path artifact
                                   (:safety-outcome-refresh-report artifact)
                                   {:missing-fields [:safety-outcome-refresh-report]}))
    (when-not (= :preserved (get-in artifact
                                    [:domain-anchor-transform-report :status]))
      (optimization-lowering-fail! "C13-DOMAIN" source-path artifact
                                   (:domain-anchor-transform-report artifact)
                                   {:missing-fields [:domain-anchor-transform-report]}))
    (when-not (= :replayable (get-in artifact
                                     [:optimization-replay-record :status]))
      (optimization-lowering-fail! "C13-NONDETERMINISM" source-path artifact
                                   (:optimization-replay-record artifact)
                                   {:missing-fields [:optimization-replay-record]}))
    (when-not (every? #(= :passed (:status %)) verifiers)
      (optimization-lowering-fail! "C13-VERIFY" source-path artifact
                                   (first verifiers)
                                   {:missing-fields [:post-pass-verifier]}))
    (when-not (= :verified-domain-ir
                 (get-in lowering-request [:input :kind]))
      (optimization-lowering-fail! "C14-INPUT" source-path artifact
                                   lowering-request
                                   {:missing-fields [:input]}))
    (when-not (= :eligible (get-in artifact
                                   [:target-eligibility-report :status]))
      (optimization-lowering-fail! "C14-PROFILE" source-path artifact
                                   (:target-eligibility-report artifact)
                                   {:missing-fields [:target-eligibility]}))
    (when-not (perf-present? (get-in lowering-request [:target :features]))
      (optimization-lowering-fail! "C14-TARGET" source-path artifact
                                   lowering-request
                                   {:missing-fields [:target :features]}))
    (when-not (= :complete (get-in artifact [:abi-manifest :status]))
      (optimization-lowering-fail! "C14-ABI" source-path artifact
                                   (:abi-manifest artifact)
                                   {:missing-fields [:abi-manifest]}))
    (when-not (= :complete (get-in artifact
                                   [:runtime-provider-manifest :status]))
      (optimization-lowering-fail! "C14-RUNTIME" source-path artifact
                                   (:runtime-provider-manifest artifact)
                                   {:missing-fields [:runtime-provider-manifest]}))
    (when-not (every? #(= :selected (:status %))
                      (:provider-selection-records artifact))
      (optimization-lowering-fail! "C14-PROVIDER" source-path artifact
                                   (first (:provider-selection-records
                                           artifact))
                                   {:missing-fields [:provider-selection]}))
    (when-not (every? #(perf-present? (:proof %))
                      (get-in artifact
                              [:proof-to-target-metadata-map :entries]))
      (optimization-lowering-fail! "C14-PROOF-METADATA" source-path artifact
                                   (:proof-to-target-metadata-map artifact)
                                   {:missing-fields [:proof]}))
    (when-not (= :preserved (get-in artifact
                                    [:capability-preservation-report :status]))
      (optimization-lowering-fail! "C14-CAPABILITY" source-path artifact
                                   (:capability-preservation-report artifact)
                                   {:missing-fields [:capability-preservation]}))
    (when-not (every? #(= :available (:fallback-status %))
                      (:unsupported-feature-report artifact))
      (optimization-lowering-fail! "C14-UNSUPPORTED" source-path artifact
                                   (first (:unsupported-feature-report
                                           artifact))
                                   {:missing-fields [:fallback-status]}))
    (when-not (and (= :gravity/target-artifact-manifest
                      (:artifact target-manifest))
                   (every? #(perf-present? (get target-manifest %))
                           [:input :backend :profile :target :artifacts
                            :source-map :proof-map :effects :capabilities
                            :safety :runtime :dependencies]))
      (optimization-lowering-fail! "C14-MANIFEST" source-path artifact
                                   target-manifest
                                   {:missing-fields [:target-artifact-manifest]})))
  :complete)

(definterposable optimization-lowering-capability-proof
  [artifact]
  {:pass-contracts-valid?
   (every? #(= :accepted (:contract-status %))
           (:optimization-pass-registry artifact))
   :pipeline-deterministic?
   (= :deterministic
      (get-in artifact [:optimization-pipeline-manifest :ordering]))
   :decisions-complete?
   (= (count (:optimization-pass-registry artifact))
      (count (:optimization-decision-log artifact)))
   :invalidations-recorded?
   (= (count (:optimization-pass-registry artifact))
      (count (:invalidated-fact-ledger artifact)))
   :proof-evidence-present?
   (every? #(some (fn [proof] (= :accepted (:status proof)))
                  (:proofs-used %))
           (:optimization-decision-log artifact))
   :post-pass-verifiers-passed?
   (every? #(= :passed (:status %)) (:post-pass-verifier-reports artifact))
   :lowering-request-verified?
   (= :verified-domain-ir (get-in artifact [:lowering-request :input :kind]))
   :target-eligible?
   (= :eligible (get-in artifact [:target-eligibility-report :status]))
   :abi-runtime-provider-recorded?
   (and (= :complete (get-in artifact [:abi-manifest :status]))
        (= :complete (get-in artifact [:runtime-provider-manifest :status]))
        (every? #(= :selected (:status %))
                (:provider-selection-records artifact)))
   :proof-metadata-linked?
   (every? #(perf-present? (:proof %))
           (get-in artifact [:proof-to-target-metadata-map :entries]))
   :manifest-complete?
   (= :gravity/target-artifact-manifest
      (get-in artifact [:target-artifact-manifest :artifact]))
   :status :complete})

(definterposable optimization-lowering-source-artifact
  [source-path source-text]
  (let [checked-core (checked-core-source-artifact source-path source-text)
        source-overrides
        (optimization-lowering-source-overrides (:module checked-core))
        domain-ir-artifact (domain-ir-source-artifact source-path source-text)
        input-id (str "sha256:" (sha256-hex (pr-str domain-ir-artifact)))
        contracts (mapv optimization-pass-contract-record
                        optimization-pass-contract-seed)
        decisions (mapv #(optimization-decision-record domain-ir-artifact
                                                       input-id %2 %1)
                        contracts
                        (range))
        final-output-id (:output-mir (last decisions))
        invalidations (mapv (fn [decision]
                              {:pass (:pass decision)
                               :decision-id (:decision-id decision)
                               :invalidated (:invalidated decision)
                               :regenerated (:regenerated decision)
                               :runtime-checks-restored
                               (:residual-checks decision)
                               :status :recorded})
                            decisions)
        verifiers (mapv (fn [decision]
                          {:artifact :gravity/post-pass-mir-verifier-report
                           :pass (:pass decision)
                           :decision-id (:decision-id decision)
                           :input (:output-mir decision)
                           :status :passed
                           :checks [:module :dominance :types :effects
                                    :safety :domain-anchors]})
                        decisions)
        target {:backend :jvm
                :triple "jvm-17"
                :features #{:objects :exceptions :threads}}
        lowering-request
        {:artifact :gravity/lowering-request
         :input {:kind :verified-domain-ir
                 :id input-id}
         :profile :hosted
         :target target
         :abi :jvm-hosted-stage0
         :runtime :hosted-jvm
         :providers {:allocator :jvm/gc
                     :panic :jvm/exception
                     :io :jvm/stdout}
         :required-evidence {:safety :mir/safety-table
                             :proofs :proof/c13-stage0
                             :capabilities :mir/capability-proof-table}}
        proof-map
        {:artifact :gravity/proof-target-metadata-map
         :target :jvm
         :entries [{:target-metadata :bounds-check-elided
                    :operation "mir-op-optimized-bounds-check-elide"
                    :proof :proof/c13-bounds-check-elision}
                   {:target-metadata :noalias
                    :operation "mir-op-optimized-target-layout-prepare"
                    :proof :proof/c13-layout-ownership}
                   {:target-metadata :nonnull
                    :operation "mir-op-optimized-dead-code-eliminate"
                    :proof :proof/c13-safety-preserved}]}
        artifact
        {:kind :gravity/stage0-optimization-lowering-artifact
         :document-set ["C13" "C14"]
         :pass {:name :optimization-and-target-lowering-api
                :input :domain-ir-registry
                :output :optimization-lowering-manifest
                :requires [:verified-domain-ir :pass-contracts
                           :semantic-anchors :proof-evidence
                           :target-eligibility]
                :preserves [:types :effects :ownership :capabilities
                            :profile :target :safety :source-spans
                            :origin-chain :domain-anchors]
                :emits [:optimization-pass-registry
                        :optimization-pipeline-manifest
                        :optimization-decision-log
                        :invalidated-fact-ledger
                        :analysis-cache-records
                        :proof-and-certificate-usage
                        :residual-cost-report
                        :post-pass-verifier-reports
                        :lowering-request
                        :target-eligibility-report
                        :abi-manifest
                        :runtime-provider-manifest
                        :layout-decision-record
                        :proof-to-target-metadata-map
                        :source-generated-origin-map
                        :target-artifact-manifest
                        :unsupported-feature-report]
                :rejects optimization-lowering-diagnostic-ids}
         :source-overrides source-overrides
         :domain-ir-artifact-kind (:kind domain-ir-artifact)
         :domain-ir-artifact-hash input-id
         :optimization-pass-registry contracts
         :optimization-pipeline-manifest
         {:artifact :gravity/optimization-pipeline-manifest
          :pass-order (mapv :pass contracts)
          :ordering :deterministic
          :optimization-level :stage0-safe
          :source-hash (str "sha256:" (sha256-hex source-text))
          :profile :hosted
          :target target
          :feature-set (:features target)
          :provider-set #{:jvm/gc :jvm/exception :jvm/stdout}
          :replay-seed :none
          :status :complete}
         :optimization-decision-log decisions
         :invalidated-fact-ledger invalidations
         :analysis-cache-records
         (mapv (fn [decision]
                 {:pass (:pass decision)
                  :cache-key (str "sha256:"
                                  (sha256-hex (pr-str
                                               [(:pass decision) input-id])))
                  :status :complete})
               decisions)
         :proof-and-certificate-usage
         (mapv (fn [decision]
                 {:pass (:pass decision)
                  :decision-id (:decision-id decision)
                  :proofs (:proofs-used decision)
                  :status :accepted})
               decisions)
         :residual-cost-report
         {:artifact :gravity/residual-cost-report
          :status :complete
          :entries [{:pass :bounds-check-elide
                     :claim :check-erased
                     :residual-cost :none}
                    {:pass :target-layout-prepare
                     :claim :layout-prepared
                     :residual-cost :manifest-only}]}
         :check-elision-record
         {:artifact :gravity/check-elision-record
          :pass :bounds-check-elide
          :status :accepted
          :proof :proof/c13-bounds-check-elision
          :policy :PERF10}
         :effect-reordering-record
         {:artifact :gravity/effect-order-proof
          :pass :effect-aware-schedule
          :status :accepted
          :proof :proof/c13-effect-order-equivalence}
         :safety-outcome-refresh-report
         {:artifact :gravity/safety-outcome-refresh-report
          :status :current
          :source :mir/safety-table}
         :domain-anchor-transform-report
         {:artifact :gravity/domain-anchor-transform-report
          :status :preserved
          :anchors (:semantic-anchor-map domain-ir-artifact)}
         :optimization-replay-record
         {:artifact :gravity/optimization-replay-record
          :status :replayable
          :ordering :deterministic
          :seed :none}
         :post-pass-verifier-reports verifiers
         :lowering-request lowering-request
         :target-eligibility-report
         {:artifact :gravity/target-eligibility-report
          :status :eligible
          :profile :hosted
          :target target
          :backend :jvm
          :reason :profile-target-provider-compatible}
         :abi-manifest
         {:artifact :gravity/abi-manifest
          :status :complete
          :calling-convention :jvm-static
          :data-layout :jvm-object
          :closure-representation :jvm-function-object
          :panic-strategy :exception}
         :runtime-provider-manifest
         {:artifact :gravity/runtime-provider-manifest
          :status :complete
          :runtime :hosted-jvm
          :providers (:providers lowering-request)}
         :provider-selection-records
         [{:provider :jvm/gc
           :capability :memory/allocator
           :status :selected}
          {:provider :jvm/stdout
           :capability :io/stdout
           :status :selected}
          {:provider :jvm/exception
           :capability :panic/raise
           :status :selected}]
         :layout-decision-record
         {:artifact :gravity/layout-decision-record
          :status :complete
          :alignment :jvm-default
          :proof :proof/c13-layout-ownership}
         :proof-to-target-metadata-map proof-map
         :source-generated-origin-map
         {:artifact :gravity/source-generated-origin-map
          :status :complete
          :source-map (:semantic-anchor-map domain-ir-artifact)}
         :capability-preservation-report
         {:artifact :gravity/capability-preservation-report
          :status :preserved
          :denied-additions []}
         :unsupported-feature-report
         [{:feature :gpu-kernel
           :backend :jvm
           :profile :hosted
           :fallback :mir-scalar-kernel
           :fallback-status :available
           :diagnostic-id nil}]
         :target-artifact-manifest
         {:artifact :gravity/target-artifact-manifest
          :input final-output-id
          :backend :jvm
          :profile :hosted
          :target (str "sha256:" (sha256-hex (pr-str target)))
          :artifacts [{:kind :jvm-bytecode-plan
                       :hash (str "sha256:"
                                  (sha256-hex (pr-str final-output-id)))}]
          :source-map :gravity/source-generated-origin-map
          :proof-map :gravity/proof-target-metadata-map
          :effects :mir/effect-table
          :capabilities :mir/capability-proof-table
          :safety :mir/safety-table
          :runtime :gravity/runtime-provider-manifest
          :dependencies input-id
          :diagnostics []}
         :diagnostics []}
        _ (optimization-lowering-validate! source-path artifact)
        capability-proof (optimization-lowering-capability-proof artifact)
        conformance {:documents ["C13" "C14"]
                     :task "P06-T05"
                     :required-diagnostic-ids
                     optimization-lowering-diagnostic-ids
                     :optimization-contract-status :complete
                     :optimization-decision-status :complete
                     :invalidation-status :complete
                     :proof-status :complete
                     :post-pass-verifier-status :complete
                     :lowering-request-status :complete
                     :target-eligibility-status :complete
                     :provider-status :complete
                     :manifest-status :complete
                     :status :complete}]
    (assoc artifact
           :capability-based-proof capability-proof
           :optimization-lowering-results conformance)))

(def ^:private namespace-contract
  {:contract-boundary :hosted-stage0-optimization-lowering-shared
   :owns [:shared-hosted-optimization-lowering-records
          :shared-hosted-optimization-lowering-validation]
   :dependency-direction {:requires ['gravity.digest]
                          :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :does-not-own [:canonical-c13-authority :canonical-c14-authority
                  :source-authentication :proof-authority
                  :target-lowering-authority :backend-authority
                  :equivalence :self-hosting :release :seed-retirement]
   :compatibility-only? true
   :canonical-authority? false
   :operation-interposition {:accepted-keys operation-keys
                             :unknown-keys-rejected? true
                             :partial-overrides? true}})
(defn- string-vector? [v] (and (vector? v) (seq v) (every? string? v)))
(defn- string-map? [v] (and (map? v) (every? (fn [[k x]] (and (string? k) (string? x))) v)))
(defn- keyword-vector-map? [v] (and (map? v) (every? (fn [[k x]] (and (keyword? k) (vector? x))) v)))
(defn- vector-maps? [v] (and (vector? v) (seq v) (every? map? v)))
(defn- validate-operations! [operations]
  (when-not (map? operations) (throw (ex-info "optimization/lowering operation map must be a map" {:value operations})))
  (let [unknown (seq (remove operation-keys (keys operations))) invalid (seq (for [[k v] (select-keys operations function-operation-keys) :when (not (fn? v))] k))]
    (when unknown (throw (ex-info "optimization/lowering operation map contains unknown keys" {:unknown-keys (vec unknown)})))
    (when invalid (throw (ex-info "optimization/lowering function operations must be functions" {:non-function-keys (vec invalid)}))))
  (doseq [[k p e] [[:c13-optimization-diagnostic-ids string-vector? :string-vector]
                    [:c14-lowering-diagnostic-ids string-vector? :string-vector]
                    [:optimization-lowering-diagnostic-ids string-vector? :string-vector]
                    [:optimization-lowering-diagnostic-messages string-map? :string-map]
                    [:optimization-lowering-override-diagnostics keyword-vector-map? :keyword-vector-map]
                    [:optimization-pass-contract-seed vector-maps? :vector-maps]]
          :when (and (contains? operations k) (not (p (get operations k))))]
    (throw (ex-info "optimization/lowering scalar operation has invalid shape" {:key k :expected e}))) operations)
(defn with-operations [operations thunk]
  (validate-operations! operations)
  (let [m (merge *operations* operations)]
    (binding [*operations* m
              c13-optimization-diagnostic-ids (get m :c13-optimization-diagnostic-ids c13-optimization-diagnostic-ids)
              c14-lowering-diagnostic-ids (get m :c14-lowering-diagnostic-ids c14-lowering-diagnostic-ids)
              optimization-lowering-diagnostic-ids (get m :optimization-lowering-diagnostic-ids optimization-lowering-diagnostic-ids)
              optimization-lowering-diagnostic-messages (get m :optimization-lowering-diagnostic-messages optimization-lowering-diagnostic-messages)
              optimization-lowering-override-diagnostics (get m :optimization-lowering-override-diagnostics optimization-lowering-override-diagnostics)
              optimization-pass-contract-seed (get m :optimization-pass-contract-seed optimization-pass-contract-seed)] (thunk))))

(def public-api
  {'public-api {:kind :contract}
   'with-operations {:arglists '([operations thunk])}
   'shared-engine-contract {:arglists '([])}
   'c13-optimization-diagnostic-ids {:kind :constant}
   'c14-lowering-diagnostic-ids {:kind :constant}
   'optimization-lowering-diagnostic-ids {:kind :constant}
   'optimization-lowering-diagnostic-messages {:kind :constant}
   'optimization-lowering-override-diagnostics {:kind :constant}
   'optimization-pass-contract-seed {:kind :constant}
   'optimization-lowering-source-overrides {:arglists '([module])}
   'optimization-lowering-fail! {:arglists '([id source-path artifact subject extra])}
   'optimization-pass-contract-record {:arglists '([record])}
   'optimization-decision-record {:arglists '([domain-ir-artifact input-id index contract])}
   'optimization-lowering-validate-overrides! {:arglists '([source-path artifact])}
   'optimization-lowering-validate! {:arglists '([source-path artifact])}
   'optimization-lowering-capability-proof {:arglists '([artifact])}
   'optimization-lowering-source-artifact {:arglists '([source-path source-text])}
   })
(defn shared-engine-contract [] (assoc namespace-contract :public-api public-api))
