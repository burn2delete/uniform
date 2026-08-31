

(defn artifact-emission-capability-proof
  [artifact]
  (let [manifests (:artifact-manifests artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:artifact-emission-diagnostic-stream
                                       :diagnostics])))]
    {:backend-inputs-verified?
     (every? #(= :complete %)
             [(get-in artifact [:backend-interface-artifact
                                :capability-based-proof :status])
              (get-in artifact [:native-lowering-artifact
                                :capability-based-proof :status])
              (get-in artifact [:hosted-lowering-artifact
                                :capability-based-proof :status])
              (get-in artifact [:specialized-lowering-artifact
                                :capability-based-proof :status])])
     :common-manifest-schema-complete?
     (and (= 12 (count manifests))
          (every? #(set/subset?
                    (set native-artifact-manifest-required-fields)
                    (set (keys %)))
                  manifests))
     :content-hashes-complete?
     (every? #(re-find #"^sha256:" (:content-hash %)) manifests)
     :provenance-complete?
     (every? #(and (get-in % [:provenance :compiler])
                   (seq (get-in % [:provenance :passes]))
                   (get-in % [:provenance :dependencies]))
             manifests)
     :source-debug-map-preserved?
     (= :preserved (get-in artifact [:source-debug-map-record :status]))
     :evidence-bundle-complete?
     (= :complete (get-in artifact
                          [:safety-proof-certificate-bundle :status]))
     :effect-capability-summary-complete?
     (= :complete (get-in artifact
                          [:effect-capability-summary :status]))
     :target-runtime-abi-metadata-complete?
     (= :complete (get-in artifact
                          [:target-runtime-abi-layout-summary :status]))
     :reproducibility-recorded?
     (= :recorded (get-in artifact [:reproducibility-record :status]))
     :development-release-gate-blocked?
     (= :blocked-development-only
        (get-in artifact [:release-gate-record
                          :release-grade-artifact-status]))
     :artifact-graph-complete?
     (= :complete (get-in artifact [:artifact-graph :status]))
     :conformance-evidence-complete?
     (= :complete (get-in artifact
                          [:conformance-evidence-reference :status]))
     :diagnostics-covered?
     (= (set artifact-emission-diagnostic-ids) diagnostics)
     :status :complete}))