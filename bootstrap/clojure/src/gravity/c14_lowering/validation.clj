(ns gravity.c14-lowering.validation
  (:require [gravity.c14-lowering.operations :as operations]
            [gravity.optimization-lowering :as shared]))

(defn source-overrides [module]
  (or (get-in module [:metadata :compiler :c14-lowering])
      (get-in module [:metadata :compiler :optimization-lowering]) {}))
(defn source-overrides-artifact [overrides]
  {:source-overrides overrides
   :lowering-request {:profile :hosted :target {:backend :jvm}}
   :input "sha256:stage0-c14-source-override"})
(defn diagnostic-catalog [configuration source-path input-id source-span]
  (let [span (source-span source-path 0)]
    {:artifact :gravity/c14-lowering-diagnostic-catalog :status :complete
     :diagnostics
     (mapv (fn [id]
             {:diagnostic id :input-artifact-id input-id :mir-operation "c14-diagnostic-op"
              :domain-anchor "c14-diagnostic-anchor" :source-span span :origin-chain []
              :profile :hosted :target :jvm :backend :jvm :missing-feature :catalog-entry
              :proof-expected :proof/c14-diagnostic-catalog :provider-expected :jvm/provider
              :fallback-status :available
              :remediation (get (:optimization-lowering-diagnostic-messages configuration) id)})
           (:c14-lowering-diagnostic-ids configuration))}))
(defn- perf-present? [value]
  (operations/invoke :perf-present?
                     (fn [candidate] (and (some? candidate) (not (and (coll? candidate) (empty? candidate)))))
                     value))
(defn- fail! [id path artifact subject extra]
  (operations/invoke :optimization-lowering-fail! shared/optimization-lowering-fail!
                     id path artifact subject extra))
(defn validate! [configuration source-path artifact]
  (operations/invoke :optimization-lowering-validate-overrides!
                     shared/optimization-lowering-validate-overrides! source-path artifact)
  (let [request (:lowering-request artifact)
        providers (:provider-selection-records artifact)
        metadata (get-in artifact [:proof-to-target-metadata-map :entries])
        unsupported (:unsupported-feature-report artifact)
        diagnostics (get-in artifact [:lowering-diagnostic-stream :diagnostics])]
    (when-not (= :optimized-mir (get-in request [:input :kind]))
      (fail! "C14-INPUT" source-path artifact request {:missing-fields [:input]}))
    (when-not (= :eligible (get-in artifact [:target-eligibility-report :status]))
      (fail! "C14-PROFILE" source-path artifact (:target-eligibility-report artifact)
             {:missing-fields [:target-eligibility]}))
    (when-not (perf-present? (get-in request [:target :features]))
      (fail! "C14-TARGET" source-path artifact request {:missing-fields [:target :features]}))
    (doseq [[id path expected missing-field]
            [["C14-ABI" [:abi-manifest :status] :complete :abi-manifest]
             ["C14-RUNTIME" [:runtime-provider-manifest :status] :complete :runtime-provider]
             ["C14-CAPABILITY" [:capability-preservation-report :status] :preserved
              :capability-preservation]]]
      (when-not (= expected (get-in artifact path))
        (fail! id source-path artifact (get-in artifact [(first path)])
               {:missing-fields [missing-field]})))
    (when-not (every? #(= :selected (:status %)) providers)
      (fail! "C14-PROVIDER" source-path artifact (first providers) {:missing-fields [:provider-selection]}))
    (when-not (every? #(perf-present? (:proof %)) metadata)
      (fail! "C14-PROOF-METADATA" source-path artifact (:proof-to-target-metadata-map artifact)
             {:missing-fields [:proof]}))
    (when-not (every? #(= :available (:fallback-status %)) unsupported)
      (fail! "C14-UNSUPPORTED" source-path artifact (first unsupported) {:missing-fields [:unsupported-feature]}))
    (when-not (= :gravity/target-artifact-manifest (get-in artifact [:target-artifact-manifest :artifact]))
      (fail! "C14-MANIFEST" source-path artifact (:target-artifact-manifest artifact)
             {:missing-fields [:target-artifact-manifest]}))
    (when-not (= (set (:c14-lowering-diagnostic-ids configuration))
                 (set (map :diagnostic diagnostics)))
      (fail! "C14-MANIFEST" source-path artifact (:lowering-diagnostic-stream artifact)
             {:missing-fields [:lowering-diagnostics]})))
  :complete)
(defn capability-proof [configuration artifact]
  {:c13-optimized-mir-input-verified?
   (= :complete (get-in artifact [:c13-optimization-artifact :capability-based-proof :status]))
   :lowering-request-verified? (= :optimized-mir (get-in artifact [:lowering-request :input :kind]))
   :target-eligible? (= :eligible (get-in artifact [:target-eligibility-report :status]))
   :abi-manifest-complete? (= :complete (get-in artifact [:abi-manifest :status]))
   :runtime-provider-recorded? (= :complete (get-in artifact [:runtime-provider-manifest :status]))
   :providers-selected? (every? #(= :selected (:status %)) (:provider-selection-records artifact))
   :proof-metadata-linked? (every? #(perf-present? (:proof %)) (get-in artifact [:proof-to-target-metadata-map :entries]))
   :source-proof-safety-metadata-preserved?
   (and (= :complete (get-in artifact [:source-generated-origin-map :status]))
        (perf-present? (get-in artifact [:target-artifact-manifest :proof-map]))
        (perf-present? (get-in artifact [:target-artifact-manifest :safety])))
   :capabilities-preserved? (= :preserved (get-in artifact [:capability-preservation-report :status]))
   :unsupported-fallbacks-recorded? (every? #(= :available (:fallback-status %)) (:unsupported-feature-report artifact))
   :manifest-complete? (= :gravity/target-artifact-manifest (get-in artifact [:target-artifact-manifest :artifact]))
   :diagnostics-covered? (= (set (:c14-lowering-diagnostic-ids configuration))
                            (set (map :diagnostic (get-in artifact [:lowering-diagnostic-stream :diagnostics]))))
   :status :complete})
