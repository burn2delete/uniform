

(defn b13-document-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/b13-artifact-emission-diagnostic-stream
   :stage :b13-artifact-emission-document-coverage
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :b13-artifact-emission-document-coverage
            :backend :gravity.backend/artifact-emission
            :message-key (keyword "backend-artifact" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "b13-document-syntax-" index)
                      :artifact input-id}
            :profile :hosted
            :target :multi-target-stage0
            :artifact-kind (case id
                             "B13-GRAPH" :gravity/artifact-graph
                             "B13-SOURCEMAP" :gravity/source-debug-map
                             "B13-RELEASE" :gravity/release-gate
                             :gravity/artifact-manifest)
            :missing-policy (b13-document-missing-policy id)
            :source-generated-origin-chain
            [:reader :macro-expansion :core :checked-core :mir
             :domain-ir :optimization :lowering :backend-emission]
            :missing-evidence #{:schema :content-hash :source-map
                                :compiler-provenance
                                :dependency-provenance :safety
                                :proofs :effects :capabilities
                                :runtime-provider :abi-layout
                                :reproducibility :conformance}
            :stale-field id
            :release-grade? (= id "B13-RELEASE")
            :fallback-status :rejected
            :facts {:common-manifest-required true
                    :content-addressing-required true
                    :source-debug-map-required true
                    :release-evidence-required true
                    :downstream-consumption-blocked-on-failure true}
            :remediation [{:kind :rebuild-common-artifact-manifest}
                          {:kind :preserve-generated-origin-chain}
                          {:kind :attach-evidence-provenance-and-hashes}
                          {:kind :block-release-grade-output}]
            :redactions []
            :ordering-key [id :b13-artifact-emission-document-coverage
                           :multi-target-stage0]})
         b13-document-diagnostic-ids
         (range))
   :status :complete})

(defn b13-document-artifact-index
  [artifact-emission-artifact]
  (let [manifests (:artifact-manifests artifact-emission-artifact)]
    {:artifact :gravity/b13-common-artifact-manifest-index
     :schema-version 1
     :input-artifact (:artifact-id artifact-emission-artifact)
     :manifest-count (count manifests)
     :content-hash-count (count (:content-hash-records
                                 artifact-emission-artifact))
     :artifact-kinds (set (map :kind manifests))
     :backends (set (map :backend manifests))
     :profiles (set (map :profile manifests))
     :targets (set (map :target manifests))
     :content-hashes (mapv :content-hash manifests)
     :all-manifests-schema-valid?
     (every? #(set/subset?
               (set native-artifact-manifest-required-fields)
               (set (keys %)))
             manifests)
     :all-content-addressed?
     (every? #(re-find #"^sha256:" (:content-hash %)) manifests)
     :common-fields native-artifact-manifest-required-fields
     :status :complete}))

(defn b13-document-downstream-consumption-record
  [artifact-emission-artifact]
  {:artifact :gravity/b13-downstream-consumption-record
   :input-artifact (:artifact-id artifact-emission-artifact)
   :consumers
   [{:consumer :package-system
     :required-inputs [:artifact-manifests :artifact-graph
                       :content-hash-records :release-gate-record]
     :status :complete}
    {:consumer :tooling
     :required-inputs [:source-debug-map-record
                       :compiler-provenance-record
                       :dependency-provenance-record
                       :diagnostic-stream]
     :status :complete}
    {:consumer :conformance
     :required-inputs [:safety-proof-certificate-bundle
                       :effect-capability-summary
                       :target-runtime-abi-layout-summary
                       :conformance-evidence-reference
                       :reproducibility-record]
     :status :complete}]
   :release-consumption :blocked-until-release-grade-evidence
   :status :complete})

(defn b13-document-validate!
  [source-path artifact]
  (let [artifact-emission (:artifact-emission-artifact artifact)
        index (:common-artifact-manifest-index artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b13-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :gravity/stage0-artifact-emission-artifact
                 (:kind artifact-emission))
      (b13-document-fail! "B13-SCHEMA" source-path artifact-emission
                          {:missing-fields [:artifact-emission-artifact]}))
    (when-not (= :complete (get-in artifact-emission
                                   [:capability-based-proof :status]))
      (b13-document-fail! "B13-EVIDENCE" source-path artifact-emission
                          {:missing-fields [:artifact-emission-proof]}))
    (when-not (= 12 (:manifest-count index))
      (b13-document-fail! "B13-SCHEMA" source-path index
                          {:missing-fields [:common-manifest-count]}))
    (when-not (:all-manifests-schema-valid? index)
      (b13-document-fail! "B13-SCHEMA" source-path index
                          {:missing-fields [:common-manifest-schema]}))
    (when-not (:all-content-addressed? index)
      (b13-document-fail! "B13-HASH" source-path index
                          {:missing-fields [:content-hashes]}))
    (when-not (= 12 (:content-hash-count index))
      (b13-document-fail! "B13-HASH" source-path index
                          {:missing-fields [:content-hash-records]}))
    (when-not (= :complete (get-in artifact
                                   [:artifact-graph :status]))
      (b13-document-fail! "B13-GRAPH" source-path (:artifact-graph artifact)
                          {:missing-fields [:artifact-graph]}))
    (when-not (= :preserved (get-in artifact
                                    [:source-debug-map-record :status]))
      (b13-document-fail! "B13-SOURCEMAP" source-path
                          (:source-debug-map-record artifact)
                          {:missing-fields [:source-debug-map]}))
    (when-not (= :complete (get-in artifact
                                   [:compiler-provenance-record :status]))
      (b13-document-fail! "B13-PROVENANCE" source-path
                          (:compiler-provenance-record artifact)
                          {:missing-fields [:compiler-provenance]}))
    (when-not (= :complete (get-in artifact
                                   [:dependency-provenance-record :status]))
      (b13-document-fail! "B13-PROVENANCE" source-path
                          (:dependency-provenance-record artifact)
                          {:missing-fields [:dependency-provenance]}))
    (when-not (= :complete (get-in artifact
                                   [:safety-proof-certificate-bundle
                                    :status]))
      (b13-document-fail! "B13-EVIDENCE" source-path
                          (:safety-proof-certificate-bundle artifact)
                          {:missing-fields [:safety-proof-certificate-bundle]}))
    (when-not (= :complete (get-in artifact
                                   [:effect-capability-summary :status]))
      (b13-document-fail! "B13-EVIDENCE" source-path
                          (:effect-capability-summary artifact)
                          {:missing-fields [:effect-capability-summary]}))
    (when-not (= :complete (get-in artifact
                                   [:runtime-provider-summary :status]))
      (b13-document-fail! "B13-TARGET" source-path
                          (:runtime-provider-summary artifact)
                          {:missing-fields [:runtime-provider-summary]}))
    (when-not (= :complete (get-in artifact
                                   [:target-runtime-abi-layout-summary
                                    :status]))
      (b13-document-fail! "B13-TARGET" source-path
                          (:target-runtime-abi-layout-summary artifact)
                          {:missing-fields [:target-runtime-abi-layout]}))
    (when-not (= :complete (get-in artifact
                                   [:conformance-evidence-reference
                                    :status]))
      (b13-document-fail! "B13-CONFORMANCE" source-path
                          (:conformance-evidence-reference artifact)
                          {:missing-fields [:conformance-evidence-reference]}))
    (when-not (= :recorded (get-in artifact
                                   [:reproducibility-record :status]))
      (b13-document-fail! "B13-REPRODUCIBILITY" source-path
                          (:reproducibility-record artifact)
                          {:missing-fields [:reproducibility-record]}))
    (when-not (= :blocked-development-only
                 (get-in artifact [:release-gate-record
                                   :release-grade-artifact-status]))
      (b13-document-fail! "B13-RELEASE" source-path
                          (:release-gate-record artifact)
                          {:missing-fields [:release-gate-record]}))
    (when-not (= :complete (get-in artifact
                                   [:downstream-consumption-record
                                    :status]))
      (b13-document-fail! "B13-GRAPH" source-path
                          (:downstream-consumption-record artifact)
                          {:missing-fields [:downstream-consumption-record]}))
    (when-not (= :passed (get-in artifact
                                 [:conformance-criteria-record :status]))
      (b13-document-fail! "B13-CONFORMANCE" source-path
                          (:conformance-criteria-record artifact)
                          {:missing-fields [:conformance-criteria-record]}))
    (when-not (= (set b13-document-diagnostic-ids) diagnostics)
      (b13-document-fail! "B13-SCHEMA" source-path
                          (:b13-diagnostic-stream artifact)
                          {:missing-fields [:b13-diagnostics]})))
  :complete)