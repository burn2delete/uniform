

(def artifact-emission-governing-documents
  ["docs/phase-07-backend-architecture/110-b13-artifact-emission-specification.md"
   "docs/phase-07-backend-architecture/111-b14-backend-conformance-test-plan.md"
   "docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md"])

(def artifact-emission-diagnostic-ids
  ["B13-SCHEMA"
   "B13-HASH"
   "B13-PROVENANCE"
   "B13-SOURCEMAP"
   "B13-EVIDENCE"
   "B13-TARGET"
   "B13-CONFORMANCE"
   "B13-REPRODUCIBILITY"
   "B13-RELEASE"
   "B13-GRAPH"])

(def artifact-emission-diagnostic-messages
  {"B13-SCHEMA" "artifact manifest schema is missing or unsupported"
   "B13-HASH" "artifact content hash is missing, stale, or mismatched"
   "B13-PROVENANCE" "artifact source, compiler, generator, pass, or dependency provenance is incomplete"
   "B13-SOURCEMAP" "artifact source/debug or generated-origin map is incomplete"
   "B13-EVIDENCE" "artifact safety, proof, certificate, effect, capability, or unsafe-audit evidence is incomplete"
   "B13-TARGET" "artifact target, ABI, layout, runtime, or provider metadata is incomplete"
   "B13-CONFORMANCE" "artifact conformance evidence required by policy is missing"
   "B13-REPRODUCIBILITY" "artifact nondeterminism or environment inputs are not recorded"
   "B13-RELEASE" "release-grade artifact emission was attempted with development-only evidence"
   "B13-GRAPH" "artifact graph edges are invalid or incomplete"})

(def artifact-emission-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             artifact-emission-diagnostic-ids)))

(defn artifact-emission-source-overrides
  [module]
  (or (get-in module [:metadata :backend :artifact-emission])
      (get-in module [:metadata :backend :artifacts])
      {}))

(defn artifact-emission-fail!
  [id source-path subject extra]
  (fail! id
         (get artifact-emission-diagnostic-messages id
              "backend artifact emission validation failed")
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :backend-artifact-emission
                 :stage (or (:stage subject) :artifact-emission)
                 :artifact-id (:artifact-id subject)
                 :artifact-kind (:artifact-kind subject)
                 :backend (or (:backend subject)
                              :gravity.backend/artifact-emission)
                 :profile (or (:profile subject) :hosted)
                 :target (or (:target subject) :multi-target-stage0)
                 :missing-evidence (:missing-evidence subject)
                 :stale-field (:stale-field subject)
                 :release-grade? (:release-grade? subject)
                 :fallback-status (or (:fallback-status subject) :rejected)
                 :remediation "Emit backend artifacts only with common manifests, content hashes, source/debug maps, compiler and dependency provenance, safety/proof/effect/capability evidence, target metadata, reproducibility records, conformance evidence, and explicit development-only release gates."}
                extra)))

(defn artifact-emission-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get artifact-emission-override-diagnostics fail-kind)]
      (artifact-emission-fail!
       id source-path
       {:artifact-id (str "artifact-emission-" (name fail-kind))
        :artifact-kind :gravity/artifact-manifest
        :missing-evidence [fail-kind]
        :stale-field fail-kind
        :release-grade? (= id "B13-RELEASE")}
       {:missing-fields [fail-kind]}))))

(defn artifact-emission-interface-manifest
  [interface-artifact]
  (let [manifest (first (:target-artifact-manifest interface-artifact))]
    {:schema-version 1
     :kind (:kind manifest)
     :backend (:backend manifest)
     :profile (:profile manifest)
     :target (:target manifest)
     :content-hash (:digest manifest)
     :inputs {:source (:source-input manifest)
              :mir (:source-input manifest)
              :backend-interface (:artifact-id interface-artifact)}
     :evidence {:safety (:safety-evidence manifest)
                :proofs (:proof-summary manifest)
                :capabilities (:capability-summary manifest)
                :effects "effect-summary:stage0"
                :conformance (:conformance manifest)
                :unsafe-audit (:unsafe-audit-ids manifest)}
     :provenance {:compiler (get-in manifest [:provenance :compiler])
                  :passes (get-in manifest [:provenance :pass-history])
                  :dependencies (first (:dependencies manifest))
                  :generator :backend-interface}
     :reproducibility {:timestamp-policy :none
                       :nondeterminism []
                       :status :recorded}}))

(defn artifact-emission-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/backend-artifact-emission-diagnostic-stream
   :stage :artifact-emission
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :artifact-emission
            :backend :gravity.backend/artifact-emission
            :message-key (keyword "backend-artifact"
                                  (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "artifact-emission-syntax-" index)
                      :artifact input-id}
            :profile :hosted
            :target :multi-target-stage0
            :artifact-kind (case id
                             "B13-GRAPH" :gravity/artifact-graph
                             "B13-SOURCEMAP" :gravity/source-debug-map
                             "B13-RELEASE" :gravity/release-gate
                             :gravity/artifact-manifest)
            :missing-evidence #{:schema :content-hash :source-map
                                :compiler-provenance
                                :dependency-provenance :safety
                                :proofs :effects :capabilities
                                :runtime-provider :abi-layout
                                :reproducibility :conformance}
            :stale-field id
            :release-grade? (= id "B13-RELEASE")
            :fallback-status :rejected
            :facts {:common-manifest-required? true
                    :content-addressing-required? true
                    :release-blocking? true}
            :remediation [{:kind :rebuild-artifact-manifest}
                          {:kind :preserve-source-debug-map}
                          {:kind :attach-evidence-and-conformance-pack}
                          {:kind :block-release-grade-output}]
            :redactions []
            :ordering-key [id :artifact-emission :multi-target-stage0]})
         artifact-emission-diagnostic-ids
         (range))
   :status :complete})

(defn artifact-emission-validate!
  [source-path artifact]
  (let [manifests (:artifact-manifests artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:artifact-emission-diagnostic-stream
                                       :diagnostics])))
        input-proofs [[:backend-interface-artifact
                       :capability-based-proof :status]
                      [:native-lowering-artifact
                       :capability-based-proof :status]
                      [:hosted-lowering-artifact
                       :capability-based-proof :status]
                      [:specialized-lowering-artifact
                       :capability-based-proof :status]]]
    (doseq [path input-proofs]
      (when-not (= :complete (get-in artifact path))
        (artifact-emission-fail!
         "B13-EVIDENCE" source-path artifact
         {:missing-fields path})))
    (when-not (and (= 12 (count manifests))
                   (every? #(set/subset?
                             (set native-artifact-manifest-required-fields)
                             (set (keys %)))
                           manifests))
      (artifact-emission-fail!
       "B13-SCHEMA" source-path artifact
       {:missing-fields [:artifact-manifests]}))
    (when-not (every? #(re-find #"^sha256:" (:content-hash %)) manifests)
      (artifact-emission-fail!
       "B13-HASH" source-path artifact
       {:missing-fields [:content-hash]}))
    (when-not (every? #(and (get-in % [:provenance :compiler])
                            (seq (get-in % [:provenance :passes]))
                            (get-in % [:provenance :dependencies]))
                      manifests)
      (artifact-emission-fail!
       "B13-PROVENANCE" source-path artifact
       {:missing-fields [:provenance]}))
    (when-not (= :preserved (get-in artifact
                                    [:source-debug-map-record :status]))
      (artifact-emission-fail!
       "B13-SOURCEMAP" source-path (:source-debug-map-record artifact)
       {:missing-fields [:source-debug-map]}))
    (when-not (= :complete (get-in artifact
                                   [:safety-proof-certificate-bundle
                                    :status]))
      (artifact-emission-fail!
       "B13-EVIDENCE" source-path (:safety-proof-certificate-bundle artifact)
       {:missing-fields [:safety-proof-certificate-bundle]}))
    (when-not (= :complete (get-in artifact
                                   [:target-runtime-abi-layout-summary
                                    :status]))
      (artifact-emission-fail!
       "B13-TARGET" source-path (:target-runtime-abi-layout-summary artifact)
       {:missing-fields [:target-runtime-abi-layout]}))
    (when-not (= :complete (get-in artifact
                                   [:conformance-evidence-reference
                                    :status]))
      (artifact-emission-fail!
       "B13-CONFORMANCE" source-path (:conformance-evidence-reference artifact)
       {:missing-fields [:conformance-evidence]}))
    (when-not (= :recorded (get-in artifact
                                   [:reproducibility-record :status]))
      (artifact-emission-fail!
       "B13-REPRODUCIBILITY" source-path (:reproducibility-record artifact)
       {:missing-fields [:reproducibility]}))
    (when-not (= :blocked-development-only
                 (get-in artifact [:release-gate-record
                                   :release-grade-artifact-status]))
      (artifact-emission-fail!
       "B13-RELEASE" source-path (:release-gate-record artifact)
       {:missing-fields [:release-gate]}))
    (when-not (= :complete (get-in artifact [:artifact-graph :status]))
      (artifact-emission-fail!
       "B13-GRAPH" source-path (:artifact-graph artifact)
       {:missing-fields [:artifact-graph]}))
    (when-not (= (set artifact-emission-diagnostic-ids) diagnostics)
      (artifact-emission-fail!
       "B13-SCHEMA" source-path (:artifact-emission-diagnostic-stream artifact)
       {:missing-fields [:artifact-emission-diagnostics]})))
  :complete)