

(defn native-lowering-source-overrides
  [module]
  (or (get-in module [:metadata :backend :native-lowering])
      (get-in module [:metadata :backend :native])
      (get-in module [:metadata :backend :artifact-emission])
      {}))

(defn native-lowering-fail!
  [id source-path subject extra]
  (fail! id
         (native-lowering-diagnostic-message id)
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :native-backend-lowering
                 :stage (or (:stage subject)
                            (native-lowering-stage-for-diagnostic id))
                 :backend (or (:backend subject)
                              (native-lowering-backend-for-diagnostic id))
                 :profile (or (:profile subject) :native)
                 :target (or (:target subject) :x86_64-stage0)
                 :artifact-id (:artifact-id subject)
                 :mir-op (or (:mir-op subject) :checked-add)
                 :domain-anchor (:domain-anchor subject)
                 :missing-evidence (:missing-evidence subject)
                 :target-construct (:target-construct subject)
                 :fallback-status (or (:fallback-status subject) :rejected)
                 :remediation "Emit native backend artifacts only from verified backend-interface input with pinned ABI/layout/runtime, proof-backed target metadata, preserved source/proof/capability records, valid artifact manifests, and conformance evidence."}
                extra)))

(defn native-lowering-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get native-lowering-override-diagnostics fail-kind)]
      (native-lowering-fail!
       id source-path
       {:stage (native-lowering-stage-for-diagnostic id)
        :backend (native-lowering-backend-for-diagnostic id)
        :artifact-id (str "native-lowering-" (name fail-kind))
        :missing-evidence [fail-kind]
        :target-construct fail-kind}
       {:missing-fields [fail-kind]}))))

(defn native-lowering-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/backend-diagnostic-stream
   :stage :native-backend-lowering
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage (native-lowering-stage-for-diagnostic id)
            :backend (native-lowering-backend-for-diagnostic id)
            :message-key (keyword "backend-native"
                                  (str/lower-case
                                   (str/replace id #"-" "-")))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "native-backend-syntax-" index)
                      :artifact input-id}
            :profile :native
            :target :x86_64-stage0
            :mir-op (case id
                      "B2-MMIO" :volatile-load
                      "B3-ATOMIC" :atomic-compare-exchange
                      "B7-CONVERSION" :domain-anchor-lowering
                      :checked-add)
            :domain-anchor (when (str/starts-with? id "B7-") :efir)
            :missing-evidence #{:proof-metadata :source-map
                                :artifact-manifest}
            :target-construct id
            :fallback-status :rejected
            :facts {:safe-ub-policy :reject
                    :metadata-required? true
                    :artifact-emission-required? true}
            :remediation [{:kind :provide-native-backend-evidence}
                          {:kind :pin-target-assumption}
                          {:kind :preserve-artifact-metadata}]
            :redactions []
            :ordering-key [id :native-backend-lowering :x86_64-stage0]})
         native-lowering-diagnostic-ids
         (range))
   :status :complete})

(defn native-lowering-artifact-manifest
  [backend kind content input-id evidence-id]
  {:schema-version 1
   :kind kind
   :backend backend
   :profile :native
   :target :x86_64-stage0
   :content-hash (c4-artifact-id content)
   :inputs {:source input-id
            :mir input-id
            :backend-interface input-id}
   :evidence {:safety "safety-bundle:stage0"
              :proofs evidence-id
              :capabilities "capability-summary:stage0"
              :effects "effect-summary:stage0"
              :conformance "backend-conformance-pack:p07-t02"}
   :provenance {:compiler "gravity-stage0-clojure"
                :passes ["C14" "B1" "B2/B3/B7" "B13" "B14"]
                :dependencies "dependency-graph:stage0"}
   :reproducibility {:timestamp-policy :none
                     :nondeterminism []
                     :status :recorded}})

(defn native-lowering-validate!
  [source-path artifact]
  (let [diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:native-diagnostic-stream
                                       :diagnostics])))
        manifests (:artifact-manifests artifact)]
    (when-not (= :complete (get-in artifact
                                   [:backend-interface-artifact
                                    :capability-based-proof :status]))
      (native-lowering-fail! "B13-EVIDENCE" source-path
                             (:backend-interface-artifact artifact)
                             {:missing-fields [:backend-interface-proof]}))
    (when-not (= #{:gravity.backend/c :gravity.backend/llvm
                   :gravity.backend/mlir}
                 (set (map :backend
                           (:target-lowering-manifest artifact))))
      (native-lowering-fail! "B14-COVERAGE" source-path
                             (:target-lowering-manifest artifact)
                             {:missing-fields [:native-backends]}))
    (when-not (= :complete (get-in artifact [:c-backend :status]))
      (native-lowering-fail! "B2-MANIFEST" source-path
                             (:c-backend artifact)
                             {:missing-fields [:c-backend]}))
    (when-not (= :rejected (get-in artifact
                                   [:c-backend :ub-rejection :status]))
      (native-lowering-fail! "B2-UB" source-path
                             (get-in artifact [:c-backend :ub-rejection])
                             {:missing-fields [:ub-rejection]}))
    (when-not (= :complete (get-in artifact [:llvm-backend :status]))
      (native-lowering-fail! "B3-MANIFEST" source-path
                             (:llvm-backend artifact)
                             {:missing-fields [:llvm-backend]}))
    (when-not (= :gated (get-in artifact
                                [:llvm-backend :metadata-policy
                                 :status]))
      (native-lowering-fail! "B3-METADATA" source-path
                             (get-in artifact
                                     [:llvm-backend :metadata-policy])
                             {:missing-fields [:llvm-metadata-policy]}))
    (when-not (= :complete (get-in artifact [:mlir-backend :status]))
      (native-lowering-fail! "B7-MANIFEST" source-path
                             (:mlir-backend artifact)
                             {:missing-fields [:mlir-backend]}))
    (when-not (= :passed (get-in artifact
                                 [:mlir-backend :verifier-report
                                  :status]))
      (native-lowering-fail! "B7-VERIFY" source-path
                             (get-in artifact
                                     [:mlir-backend :verifier-report])
                             {:missing-fields [:mlir-verifier]}))
    (when-not (every? #(set/subset? (set native-artifact-manifest-required-fields)
                                    (set (keys %)))
                      manifests)
      (native-lowering-fail! "B13-SCHEMA" source-path
                             (first manifests)
                             {:missing-fields [:artifact-manifest-schema]}))
    (when-not (every? #(re-find #"^sha256:" (:content-hash %)) manifests)
      (native-lowering-fail! "B13-HASH" source-path
                             (first manifests)
                             {:missing-fields [:content-hash]}))
    (when-not (= :preserved (get-in artifact
                                    [:metadata-preservation-report
                                     :status]))
      (native-lowering-fail! "B14-METADATA" source-path
                             (:metadata-preservation-report artifact)
                             {:missing-fields [:metadata-preservation]}))
    (when-not (= :passed (get-in artifact
                                 [:backend-conformance-record :status]))
      (native-lowering-fail! "B14-EVIDENCE" source-path
                             (:backend-conformance-record artifact)
                             {:missing-fields [:conformance-evidence]}))
    (when-not (= (set native-lowering-diagnostic-ids) diagnostics)
      (native-lowering-fail! "B14-NEGATIVE" source-path
                             (:native-diagnostic-stream artifact)
                             {:missing-fields [:native-diagnostics]})))
  :complete)

(defn native-lowering-capability-proof
  [artifact]
  (let [diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:native-diagnostic-stream
                                       :diagnostics])))
        manifests (:artifact-manifests artifact)]
    {:backend-interface-input-verified?
     (= :complete (get-in artifact
                          [:backend-interface-artifact
                           :capability-based-proof :status]))
     :c-lowering-complete?
     (= :complete (get-in artifact [:c-backend :status]))
     :c-undefined-behavior-rejected?
     (= :rejected (get-in artifact [:c-backend :ub-rejection :status]))
     :llvm-lowering-complete?
     (= :complete (get-in artifact [:llvm-backend :status]))
     :llvm-proof-metadata-gated?
     (= :gated (get-in artifact
                       [:llvm-backend :metadata-policy :status]))
     :mlir-lowering-complete?
     (= :complete (get-in artifact [:mlir-backend :status]))
     :mlir-conversion-verified?
     (= :passed (get-in artifact
                        [:mlir-backend :conversion-legality-report
                         :status]))
     :artifact-emission-complete?
     (and (= 3 (count manifests))
          (every? #(set/subset?
                    (set native-artifact-manifest-required-fields)
                    (set (keys %)))
                  manifests)
          (every? #(re-find #"^sha256:" (:content-hash %)) manifests))
     :source-proof-capability-metadata-preserved?
     (= :preserved (get-in artifact
                           [:metadata-preservation-report :status]))
     :conformance-record-passed?
     (= :passed (get-in artifact
                        [:backend-conformance-record :status]))
     :diagnostics-covered?
     (= (set native-lowering-diagnostic-ids) diagnostics)
     :status :complete}))