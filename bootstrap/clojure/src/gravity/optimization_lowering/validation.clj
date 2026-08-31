(ns gravity.optimization-lowering.validation
  "C13/C14 artifact validation and capability evidence projection.")

(defn validate-overrides!
  [{:keys [fail! override-diagnostics source-span]} source-path artifact]
  (when-let [fail-kind (get-in artifact [:source-overrides :fail])]
    (let [[id subject-kind] (get override-diagnostics fail-kind)]
      (when id
        (fail! id source-path artifact
               {:pass-id subject-kind
                :decision-id (str "optimization-lowering-invalid-"
                                  (name fail-kind))
                :source-span (source-span source-path 0)
                :missing-fact fail-kind
                :missing-feature fail-kind
                :fallback-status :missing}
               {:missing-fields [fail-kind]})))))

(defn- require-condition!
  [condition fail! id source-path artifact subject missing-fields]
  (when-not condition
    (fail! id source-path artifact subject {:missing-fields missing-fields})))

(defn validate-optimization!
  [{:keys [fail! perf-present?]} source-path artifact]
  (let [contracts (:optimization-pass-registry artifact)
        pipeline (:optimization-pipeline-manifest artifact)
        decisions (:optimization-decision-log artifact)
        invalidations (:invalidated-fact-ledger artifact)
        verifiers (:post-pass-verifier-reports artifact)]
    (doseq [contract contracts]
      (require-condition!
       (every? #(perf-present? (get contract %))
               [:artifact :pass :input :output :requires :preserves
                :proof-obligations :profiles :emits])
       fail! "C13-CONTRACT" source-path artifact contract
       [:pass :input :output :requires :preserves :proof-obligations]))
    (require-condition!
     (= (mapv :pass contracts) (:pass-order pipeline))
     fail! "C13-CONTRACT" source-path artifact pipeline [:pass-order])
    (require-condition!
     (every? #(perf-present? (:preserved %)) decisions)
     fail! "C13-PRESERVE" source-path artifact (first decisions) [:preserved])
    (require-condition!
     (= (count contracts) (count invalidations))
     fail! "C13-INVALIDATE" source-path artifact (first decisions)
     [:invalidated-fact-ledger])
    (require-condition!
     (every? #(some (fn [proof] (= :accepted (:status proof)))
                    (:proofs-used %))
             decisions)
     fail! "C13-PROOF" source-path artifact (first decisions) [:proofs-used])
    (require-condition!
     (= :accepted (get-in artifact [:check-elision-record :status]))
     fail! "C13-CHECK-ELISION" source-path artifact
     (:check-elision-record artifact) [:check-elision-record])
    (require-condition!
     (= :accepted (get-in artifact [:effect-reordering-record :status]))
     fail! "C13-EFFECT" source-path artifact
     (:effect-reordering-record artifact) [:effect-reordering-record])
    (require-condition!
     (= :current (get-in artifact [:safety-outcome-refresh-report :status]))
     fail! "C13-SAFETY" source-path artifact
     (:safety-outcome-refresh-report artifact) [:safety-outcome-refresh-report])
    (require-condition!
     (= :preserved (get-in artifact [:domain-anchor-transform-report :status]))
     fail! "C13-DOMAIN" source-path artifact
     (:domain-anchor-transform-report artifact) [:domain-anchor-transform-report])
    (require-condition!
     (= :replayable (get-in artifact [:optimization-replay-record :status]))
     fail! "C13-NONDETERMINISM" source-path artifact
     (:optimization-replay-record artifact) [:optimization-replay-record])
    (require-condition!
     (every? #(= :passed (:status %)) verifiers)
     fail! "C13-VERIFY" source-path artifact (first verifiers)
     [:post-pass-verifier])))

(defn validate-lowering!
  [{:keys [fail! perf-present?]} source-path artifact]
  (let [request (:lowering-request artifact)
        manifest (:target-artifact-manifest artifact)]
    (require-condition!
     (= :verified-domain-ir (get-in request [:input :kind]))
     fail! "C14-INPUT" source-path artifact request [:input])
    (require-condition!
     (= :eligible (get-in artifact [:target-eligibility-report :status]))
     fail! "C14-PROFILE" source-path artifact
     (:target-eligibility-report artifact) [:target-eligibility])
    (require-condition!
     (perf-present? (get-in request [:target :features]))
     fail! "C14-TARGET" source-path artifact request [:target :features])
    (require-condition!
     (= :complete (get-in artifact [:abi-manifest :status]))
     fail! "C14-ABI" source-path artifact (:abi-manifest artifact) [:abi-manifest])
    (require-condition!
     (= :complete (get-in artifact [:runtime-provider-manifest :status]))
     fail! "C14-RUNTIME" source-path artifact
     (:runtime-provider-manifest artifact) [:runtime-provider-manifest])
    (require-condition!
     (every? #(= :selected (:status %)) (:provider-selection-records artifact))
     fail! "C14-PROVIDER" source-path artifact
     (first (:provider-selection-records artifact)) [:provider-selection])
    (require-condition!
     (every? #(perf-present? (:proof %))
             (get-in artifact [:proof-to-target-metadata-map :entries]))
     fail! "C14-PROOF-METADATA" source-path artifact
     (:proof-to-target-metadata-map artifact) [:proof])
    (require-condition!
     (= :preserved (get-in artifact [:capability-preservation-report :status]))
     fail! "C14-CAPABILITY" source-path artifact
     (:capability-preservation-report artifact) [:capability-preservation])
    (require-condition!
     (every? #(= :available (:fallback-status %))
             (:unsupported-feature-report artifact))
     fail! "C14-UNSUPPORTED" source-path artifact
     (first (:unsupported-feature-report artifact)) [:fallback-status])
    (require-condition!
     (and (= :gravity/target-artifact-manifest (:artifact manifest))
          (every? #(perf-present? (get manifest %))
                  [:input :backend :profile :target :artifacts :source-map
                   :proof-map :effects :capabilities :safety :runtime
                   :dependencies]))
     fail! "C14-MANIFEST" source-path artifact manifest
     [:target-artifact-manifest])))

(defn validate!
  [operations source-path artifact]
  ((:validate-overrides! operations) source-path artifact)
  (validate-optimization! operations source-path artifact)
  (validate-lowering! operations source-path artifact)
  :complete)

(defn capability-proof [perf-present? artifact]
  {:pass-contracts-valid?
   (every? #(= :accepted (:contract-status %))
           (:optimization-pass-registry artifact))
   :pipeline-deterministic?
   (= :deterministic (get-in artifact [:optimization-pipeline-manifest :ordering]))
   :decisions-complete?
   (= (count (:optimization-pass-registry artifact))
      (count (:optimization-decision-log artifact)))
   :invalidations-recorded?
   (= (count (:optimization-pass-registry artifact))
      (count (:invalidated-fact-ledger artifact)))
   :proof-evidence-present?
   (every? #(some (fn [proof] (= :accepted (:status proof))) (:proofs-used %))
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
        (every? #(= :selected (:status %)) (:provider-selection-records artifact)))
   :proof-metadata-linked?
   (every? #(perf-present? (:proof %))
           (get-in artifact [:proof-to-target-metadata-map :entries]))
   :manifest-complete?
   (= :gravity/target-artifact-manifest
      (get-in artifact [:target-artifact-manifest :artifact]))
   :status :complete})
