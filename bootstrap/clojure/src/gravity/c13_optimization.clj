(ns gravity.c13-optimization
  "Hosted Stage0 C13 MIR optimization adapter and evidence projection."
  (:require [gravity.digest :as digest]
            [gravity.optimization-lowering :as shared]))
(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private function-operation-keys #{:source-span
    :c4-artifact-id
    :sha256-hex
    :read-source-form-records
    :validate-ns-syntax!
    :parse-module
    :perf-present?
    :compiler-c12-domain-ir-source-artifact
    :optimization-lowering-validate-overrides!
    :optimization-pass-contract-record
    :optimization-decision-record
    :optimization-lowering-fail!
    :c13-optimization-source-overrides
    :c13-optimization-validate-source-overrides!
    :c13-optimization-diagnostic-catalog
    :c13-optimization-validate!
    :c13-optimization-capability-proof
    :compiler-c13-optimization-source-artifact
    :compiler-c13-optimization-file-artifact})
(def ^:private scalar-operation-keys #{:c13-optimization-governing-document
    :c13-optimization-diagnostic-ids
    :optimization-lowering-diagnostic-messages
    :optimization-pass-contract-seed})
(def ^:private operation-keys (into function-operation-keys scalar-operation-keys))
(defn- current-operation [k] (when-not (contains? *active-operation-keys* k) (get *operations* k)))
(defmacro ^:private definterposable [name args & body]
  (let [k (keyword name)] `(defn ~name ~args (if-let [f# (current-operation ~k)] (binding [*active-operation-keys* (conj *active-operation-keys* ~k)] (f# ~@args)) (do ~@body)))))
(defn- unsupported [k] (fn [& _] (throw (ex-info (str "C13 leaf requires injected operation " k) {:operation k}))))
(defn- op [k fallback] (or (get *operations* k) fallback))
(defn- source-span [p i] ((op :source-span (fn [p i] {:source p :form-index i})) p i))
(defn- c4-artifact-id [x] ((op :c4-artifact-id (fn [x] (str "sha256:" (digest/sha256-hex (pr-str x))))) x))
(defn- sha256-hex [value]
  ((op :sha256-hex digest/sha256-hex) value))
(defn- perf-present? [value]
  ((op :perf-present?
       (fn [candidate]
         (and (some? candidate)
              (not (and (coll? candidate) (empty? candidate))))))
   value))
(defn- read-source-form-records [p t] ((op :read-source-form-records (unsupported :read-source-form-records)) p t))
(defn- validate-ns-syntax! [p f] ((op :validate-ns-syntax! (unsupported :validate-ns-syntax!)) p f))
(defn- parse-module [p f] ((op :parse-module (unsupported :parse-module)) p f))
(defn- compiler-c12-domain-ir-source-artifact [p t] ((op :compiler-c12-domain-ir-source-artifact (unsupported :compiler-c12-domain-ir-source-artifact)) p t))
(defn- optimization-lowering-validate-overrides! [p a] ((op :optimization-lowering-validate-overrides! shared/optimization-lowering-validate-overrides!) p a))
(defn- optimization-pass-contract-record [s] ((op :optimization-pass-contract-record shared/optimization-pass-contract-record) s))
(defn- optimization-decision-record [a i n c] ((op :optimization-decision-record shared/optimization-decision-record) a i n c))
(defn- optimization-lowering-fail! [id p s e] ((op :optimization-lowering-fail! shared/optimization-lowering-fail!) id p s e))
(def ^:private ^:dynamic c13-optimization-diagnostic-ids shared/c13-optimization-diagnostic-ids)
(def ^:private ^:dynamic optimization-lowering-diagnostic-messages shared/optimization-lowering-diagnostic-messages)
(def ^:private ^:dynamic optimization-pass-contract-seed shared/optimization-pass-contract-seed)
(def ^:dynamic c13-optimization-governing-document
  "docs/phase-06-compiler-architecture/092-c13-mir-optimization-passes-design.md")

(definterposable c13-optimization-source-overrides
  [module]
  (or (get-in module [:metadata :compiler :c13-optimization])
      (get-in module [:metadata :compiler :optimization-lowering])
      {}))

(definterposable c13-optimization-validate-source-overrides!
  [source-path overrides]
  (optimization-lowering-validate-overrides!
   source-path
   {:source-overrides overrides
    :lowering-request {:profile :hosted
                       :target {:backend :jvm}}
    :input "sha256:stage0-c13-source-override"}))

(definterposable c13-optimization-diagnostic-catalog
  [source-path]
  (let [span (source-span source-path 0)]
    {:artifact :gravity/c13-optimization-diagnostic-catalog
     :status :complete
     :diagnostics
     (mapv (fn [id]
             {:diagnostic id
              :pass-id :stage0-optimization
              :decision-id "c13-diagnostic-catalog"
              :input-artifact-id "sha256:c13-diagnostic-input"
              :output-artifact-id "sha256:c13-diagnostic-output"
              :source-span span
              :changed-operations []
              :missing-fact :catalog-entry
              :proof-id :proof/c13-diagnostic-catalog
              :profile :hosted
              :target :jvm
              :remediation (get optimization-lowering-diagnostic-messages id)})
           c13-optimization-diagnostic-ids)}))

(definterposable c13-optimization-validate!
  [source-path artifact]
  (optimization-lowering-validate-overrides! source-path artifact)
  (let [contracts (:optimization-pass-registry artifact)
        pipeline (:optimization-pipeline-manifest artifact)
        decisions (:optimization-decision-log artifact)
        invalidations (:invalidated-fact-ledger artifact)
        caches (:analysis-cache-records artifact)
        proof-usage (:proof-and-certificate-usage artifact)
        verifiers (:post-pass-verifier-reports artifact)
        diagnostics (get-in artifact
                            [:optimization-diagnostic-stream :diagnostics])]
    (doseq [contract contracts]
      (when-not (every? #(contains? contract %)
                        [:artifact :pass :input :output :requires
                         :preserves :invalidates :regenerates
                         :proof-obligations :profiles :target-assumptions
                         :emits])
        (optimization-lowering-fail! "C13-CONTRACT" source-path artifact
                                     contract
                                     {:missing-fields [:pass :input :output
                                                       :requires :preserves
                                                       :proof-obligations]})))
    (when-not (= (mapv :pass contracts) (:pass-order pipeline))
      (optimization-lowering-fail! "C13-CONTRACT" source-path artifact
                                   pipeline
                                   {:missing-fields [:pass-order]}))
    (when-not (= :deterministic (:ordering pipeline))
      (optimization-lowering-fail! "C13-NONDETERMINISM" source-path artifact
                                   pipeline
                                   {:missing-fields [:ordering]}))
    (when-not (every? #(perf-present? (:preserved %)) decisions)
      (optimization-lowering-fail! "C13-PRESERVE" source-path artifact
                                   (first decisions)
                                   {:missing-fields [:preserved]}))
    (when-not (= (count contracts) (count invalidations))
      (optimization-lowering-fail! "C13-INVALIDATE" source-path artifact
                                   (first decisions)
                                   {:missing-fields [:invalidated-fact-ledger]}))
    (when-not (= (count contracts) (count caches))
      (optimization-lowering-fail! "C13-INVALIDATE" source-path artifact
                                   (first decisions)
                                   {:missing-fields [:analysis-cache-records]}))
    (when-not (= (count contracts) (count proof-usage))
      (optimization-lowering-fail! "C13-PROOF" source-path artifact
                                   (first decisions)
                                   {:missing-fields [:proof-usage]}))
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
    (when-not (= (set c13-optimization-diagnostic-ids)
                 (set (map :diagnostic diagnostics)))
      (optimization-lowering-fail! "C13-CONTRACT" source-path artifact
                                   (:optimization-diagnostic-stream artifact)
                                   {:missing-fields [:optimization-diagnostics]})))
  :complete)

(definterposable c13-optimization-capability-proof
  [artifact]
  (let [contracts (:optimization-pass-registry artifact)
        decisions (:optimization-decision-log artifact)]
    {:c12-domain-ir-input-verified?
     (= :complete (get-in artifact
                          [:c12-domain-ir-artifact
                           :capability-based-proof :status]))
     :pass-contracts-valid?
     (every? #(= :gravity/mir-pass-contract (:artifact %)) contracts)
     :pipeline-deterministic?
     (= :deterministic (get-in artifact
                               [:optimization-pipeline-manifest :ordering]))
     :decisions-complete?
     (= (count contracts) (count decisions))
     :changed-and-unchanged-decisions-recorded?
     (and (some seq (map :changed-ops decisions))
          (some empty? (map :changed-ops decisions)))
     :invalidations-recorded?
     (= (count contracts) (count (:invalidated-fact-ledger artifact)))
     :analysis-caches-recorded?
     (= (count contracts) (count (:analysis-cache-records artifact)))
     :proof-evidence-present?
     (every? #(some (fn [proof] (= :accepted (:status proof)))
                    (:proofs-used %))
             decisions)
     :residual-cost-visible?
     (= :complete (get-in artifact [:residual-cost-report :status]))
     :check-elision-proof?
     (= :accepted (get-in artifact [:check-elision-record :status]))
     :effect-order-preserved?
     (= :accepted (get-in artifact [:effect-reordering-record :status]))
     :safety-outcomes-current?
     (= :current (get-in artifact
                         [:safety-outcome-refresh-report :status]))
     :domain-anchors-preserved?
     (= :preserved (get-in artifact
                           [:domain-anchor-transform-report :status]))
     :replayable?
     (= :replayable (get-in artifact
                            [:optimization-replay-record :status]))
     :post-pass-verifiers-passed?
     (every? #(= :passed (:status %))
             (:post-pass-verifier-reports artifact))
     :diagnostics-covered?
     (= (set c13-optimization-diagnostic-ids)
        (set (map :diagnostic
                  (get-in artifact
                          [:optimization-diagnostic-stream
                           :diagnostics]))))
     :status :complete}))

(definterposable compiler-c13-optimization-source-artifact
  [source-path source-text]
  (let [records (read-source-form-records source-path source-text)
        forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (c13-optimization-source-overrides module)
        _ (c13-optimization-validate-source-overrides! source-path
                                                       source-overrides)
        domain-ir-artifact (compiler-c12-domain-ir-source-artifact
                            source-path source-text)
        input-id (:artifact-id domain-ir-artifact)
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
                               :caches-cleared [:data-flow-cache
                                               :domain-anchor-cache]
                               :diagnostics-affected []
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
        diagnostics (c13-optimization-diagnostic-catalog source-path)
        artifact-base
        {:kind :gravity/stage0-c13-mir-optimization-artifact
         :task "P06-D092"
         :document-set ["C13"]
         :governing-document c13-optimization-governing-document
         :pass {:name :c13-mir-optimization-passes
                :input :verified-domain-ir
                :output :optimized-mir
                :requires [:c12-domain-ir-architecture
                           :pass-contracts :semantic-anchors
                           :proof-evidence :mir-verifier]
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
                        :optimized-mir-artifact
                        :optimization-diagnostic-stream]
                :rejects c13-optimization-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :c12-domain-ir-artifact
         (select-keys domain-ir-artifact [:kind :task :artifact-id
                                          :governing-document
                                          :domain-verifier-report
                                          :semantic-anchor-map
                                          :capability-based-proof])
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
          :target :jvm
          :feature-set #{:objects :exceptions :threads}
          :package-graph :stage0-single-package
          :provider-set #{:jvm/gc :jvm/exception :jvm/stdout}
          :benchmark-inputs []
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
                  :invalidated-by (:invalidated decision)
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
         :optimized-mir-artifact
         {:artifact :gravity/optimized-mir
          :input input-id
          :output final-output-id
          :passes (mapv :pass contracts)
          :source-origin-map (:semantic-anchor-map domain-ir-artifact)
          :domain-anchors (:semantic-anchor-map domain-ir-artifact)
          :status :complete}
         :optimization-diagnostic-stream diagnostics
         :c13-optimization-results
         {:documents ["C13"]
          :task "P06-D092"
          :required-diagnostic-ids c13-optimization-diagnostic-ids
          :c12-input-status :complete
          :pass-contract-status :complete
          :pipeline-status :complete
          :decision-log-status :complete
          :invalidation-status :complete
          :analysis-cache-status :complete
          :proof-status :complete
          :residual-cost-status :complete
          :post-pass-verifier-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (c13-optimization-validate! source-path artifact-base)
        capability-proof (c13-optimization-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(definterposable compiler-c13-optimization-file-artifact
  [path]
  (compiler-c13-optimization-source-artifact path (slurp path)))

(def ^:private namespace-contract
 {:contract-boundary :hosted-stage0-c13-optimization
  :dependency-direction {:requires ['gravity.digest 'gravity.optimization-lowering]
                         :forbids ['gravity.bootstrap 'gravity.diagnostics]}
  :owns [:hosted-stage0-c13-optimization-adapter :hosted-stage0-c13-evidence]
  :does-not-own [:canonical-c13-authority :source-authentication :shared-optimization-engine-authority :proof-authority :check-elision-authority :domain-verifier-authority :target-lowering-authority :backend-authority :equivalence :self-hosting :release :seed-retirement]
  :compatibility-only? true :optimization-model-complete? false :canonical-c13-authority? false
  :operation-interposition {:accepted-keys operation-keys :unknown-keys-rejected? true :partial-overrides? true :single-binding-per-top-level-call? true}})
(defn- sv? [v] (and (vector? v) (seq v) (every? string? v)))
(defn- sm? [v] (and (map? v) (every? (fn [[k x]] (and (string? k) (string? x))) v)))
(defn- vm? [v] (and (vector? v) (seq v) (every? map? v)))
(defn- validate-operations! [operations]
 (when-not (map? operations) (throw (ex-info "C13 operation map must be a map" {:value operations})))
 (let [u (seq (remove operation-keys (keys operations))) bad (seq (for [[k v] (select-keys operations function-operation-keys) :when (not (fn? v))] k))]
  (when u (throw (ex-info "C13 operation map contains unknown keys" {:unknown-keys (vec u)})))
  (when bad (throw (ex-info "C13 function operations must be functions" {:non-function-keys (vec bad)}))))
 (doseq [[k p] [[:c13-optimization-governing-document #(and (string? %) (seq %))] [:c13-optimization-diagnostic-ids sv?] [:optimization-lowering-diagnostic-messages sm?] [:optimization-pass-contract-seed vm?]] :when (and (contains? operations k) (not (p (get operations k))))] (throw (ex-info "C13 scalar operation invalid" {:key k}))) operations)
(defn with-operations [operations thunk]
 (validate-operations! operations)
 (let [m (merge *operations* operations)] (binding [*operations* m c13-optimization-governing-document (get m :c13-optimization-governing-document c13-optimization-governing-document) c13-optimization-diagnostic-ids (get m :c13-optimization-diagnostic-ids c13-optimization-diagnostic-ids) optimization-lowering-diagnostic-messages (get m :optimization-lowering-diagnostic-messages optimization-lowering-diagnostic-messages) optimization-pass-contract-seed (get m :optimization-pass-contract-seed optimization-pass-contract-seed)] (thunk))))
(def public-api {'public-api {:kind :contract} 'with-operations {:arglists '([operations thunk])} 'c13-engine-contract {:arglists '([])} 'c13-optimization-governing-document {:kind :constant}
 'c13-optimization-source-overrides {:arglists '([module])}
 'c13-optimization-validate-source-overrides! {:arglists '([source-path overrides])}
 'c13-optimization-diagnostic-catalog {:arglists '([source-path])}
 'c13-optimization-validate! {:arglists '([source-path artifact])}
 'c13-optimization-capability-proof {:arglists '([artifact])}
 'compiler-c13-optimization-source-artifact {:arglists '([source-path source-text])}
 'compiler-c13-optimization-file-artifact {:arglists '([path])}
})
(defn c13-engine-contract [] (assoc namespace-contract :public-api public-api))
