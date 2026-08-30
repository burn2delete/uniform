(ns gravity.c13-optimization.validation
  (:require [gravity.c13-optimization.operations :as operations]
            [gravity.optimization-lowering :as shared]))

(defn source-overrides [module]
  (or (get-in module [:metadata :compiler :c13-optimization])
      (get-in module [:metadata :compiler :optimization-lowering])
      {}))

(defn source-overrides-artifact [overrides]
  {:source-overrides overrides
   :lowering-request {:profile :hosted :target {:backend :jvm}}
   :input "sha256:stage0-c13-source-override"})

(defn diagnostic-catalog [configuration source-path source-span]
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
              :remediation (get (:optimization-lowering-diagnostic-messages configuration) id)})
           (:c13-optimization-diagnostic-ids configuration))}))

(defn- fail! [id source-path artifact subject evidence]
  (operations/invoke :optimization-lowering-fail! shared/optimization-lowering-fail!
                     id source-path artifact subject evidence))

(defn- perf-present? [value]
  (operations/invoke :perf-present?
                     (fn [candidate]
                       (and (some? candidate)
                            (not (and (coll? candidate) (empty? candidate)))))
                     value))

(defn validate! [configuration source-path artifact]
  (operations/invoke :optimization-lowering-validate-overrides!
                     shared/optimization-lowering-validate-overrides!
                     source-path artifact)
  (let [contracts (:optimization-pass-registry artifact)
        pipeline (:optimization-pipeline-manifest artifact)
        decisions (:optimization-decision-log artifact)
        invalidations (:invalidated-fact-ledger artifact)
        caches (:analysis-cache-records artifact)
        proof-usage (:proof-and-certificate-usage artifact)
        verifiers (:post-pass-verifier-reports artifact)
        diagnostics (get-in artifact [:optimization-diagnostic-stream :diagnostics])]
    (doseq [contract contracts]
      (when-not (every? #(contains? contract %)
                        [:artifact :pass :input :output :requires :preserves :invalidates
                         :regenerates :proof-obligations :profiles :target-assumptions :emits])
        (fail! "C13-CONTRACT" source-path artifact contract
               {:missing-fields [:pass :input :output :requires :preserves :proof-obligations]})))
    (when-not (= (mapv :pass contracts) (:pass-order pipeline))
      (fail! "C13-CONTRACT" source-path artifact pipeline {:missing-fields [:pass-order]}))
    (when-not (= :deterministic (:ordering pipeline))
      (fail! "C13-NONDETERMINISM" source-path artifact pipeline {:missing-fields [:ordering]}))
    (when-not (every? #(perf-present? (:preserved %)) decisions)
      (fail! "C13-PRESERVE" source-path artifact (first decisions) {:missing-fields [:preserved]}))
    (when-not (= (count contracts) (count invalidations))
      (fail! "C13-INVALIDATE" source-path artifact (first decisions)
             {:missing-fields [:invalidated-fact-ledger]}))
    (when-not (= (count contracts) (count caches))
      (fail! "C13-INVALIDATE" source-path artifact (first decisions)
             {:missing-fields [:analysis-cache-records]}))
    (when-not (= (count contracts) (count proof-usage))
      (fail! "C13-PROOF" source-path artifact (first decisions) {:missing-fields [:proof-usage]}))
    (when-not (every? #(some (fn [proof] (= :accepted (:status proof))) (:proofs-used %)) decisions)
      (fail! "C13-PROOF" source-path artifact (first decisions) {:missing-fields [:proofs-used]}))
    (doseq [[id path]
            [["C13-CHECK-ELISION" [:check-elision-record :status]]
             ["C13-EFFECT" [:effect-reordering-record :status]]
             ["C13-SAFETY" [:safety-outcome-refresh-report :status]]
             ["C13-DOMAIN" [:domain-anchor-transform-report :status]]
             ["C13-NONDETERMINISM" [:optimization-replay-record :status]]]]
      (let [expected ({"C13-CHECK-ELISION" :accepted "C13-EFFECT" :accepted
                       "C13-SAFETY" :current "C13-DOMAIN" :preserved
                       "C13-NONDETERMINISM" :replayable} id)]
        (when-not (= expected (get-in artifact path))
          (fail! id source-path artifact (get-in artifact [(first path)])
                 {:missing-fields [(first path)]}))))
    (when-not (every? #(= :passed (:status %)) verifiers)
      (fail! "C13-VERIFY" source-path artifact (first verifiers)
             {:missing-fields [:post-pass-verifier]}))
    (when-not (= (set (:c13-optimization-diagnostic-ids configuration))
                 (set (map :diagnostic diagnostics)))
      (fail! "C13-CONTRACT" source-path artifact (:optimization-diagnostic-stream artifact)
             {:missing-fields [:optimization-diagnostics]})))
  :complete)

(defn capability-proof [configuration artifact]
  (let [contracts (:optimization-pass-registry artifact)
        decisions (:optimization-decision-log artifact)]
    {:c12-domain-ir-input-verified?
     (= :complete (get-in artifact [:c12-domain-ir-artifact :capability-based-proof :status]))
     :pass-contracts-valid? (every? #(= :gravity/mir-pass-contract (:artifact %)) contracts)
     :pipeline-deterministic? (= :deterministic (get-in artifact [:optimization-pipeline-manifest :ordering]))
     :decisions-complete? (= (count contracts) (count decisions))
     :changed-and-unchanged-decisions-recorded?
     (and (some seq (map :changed-ops decisions)) (some empty? (map :changed-ops decisions)))
     :invalidations-recorded? (= (count contracts) (count (:invalidated-fact-ledger artifact)))
     :analysis-caches-recorded? (= (count contracts) (count (:analysis-cache-records artifact)))
     :proof-evidence-present?
     (every? #(some (fn [proof] (= :accepted (:status proof))) (:proofs-used %)) decisions)
     :residual-cost-visible? (= :complete (get-in artifact [:residual-cost-report :status]))
     :check-elision-proof? (= :accepted (get-in artifact [:check-elision-record :status]))
     :effect-order-preserved? (= :accepted (get-in artifact [:effect-reordering-record :status]))
     :safety-outcomes-current? (= :current (get-in artifact [:safety-outcome-refresh-report :status]))
     :domain-anchors-preserved? (= :preserved (get-in artifact [:domain-anchor-transform-report :status]))
     :replayable? (= :replayable (get-in artifact [:optimization-replay-record :status]))
     :post-pass-verifiers-passed? (every? #(= :passed (:status %)) (:post-pass-verifier-reports artifact))
     :diagnostics-covered?
     (= (set (:c13-optimization-diagnostic-ids configuration))
        (set (map :diagnostic (get-in artifact [:optimization-diagnostic-stream :diagnostics]))))
     :status :complete}))
