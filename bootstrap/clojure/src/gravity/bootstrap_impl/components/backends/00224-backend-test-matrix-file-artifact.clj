

(defn backend-test-matrix-file-artifact
  [path]
  (backend-test-matrix-source-artifact path (slurp path)))

(def b1-document-governing-document
  "docs/phase-07-backend-architecture/098-b1-backend-interface-specification.md")

(def b1-document-diagnostic-ids
  ["B1-INPUT"
   "B1-PROFILE"
   "B1-TARGET"
   "B1-ABI"
   "B1-RUNTIME"
   "B1-PROOF"
   "B1-CAPABILITY"
   "B1-UNSUPPORTED"
   "B1-METADATA"])

(def b1-document-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             b1-document-diagnostic-ids)))

(defn b1-document-source-overrides
  [module]
  (or (get-in module [:metadata :backend :b1-document])
      (get-in module [:metadata :backend :interface])
      {}))

(defn b1-document-fail!
  [id source-path subject extra]
  (fail! id
         (get backend-interface-diagnostic-messages id
              "B1 backend interface document coverage validation failed")
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :b1-backend-interface-document
                 :stage (or (:stage subject) :b1-document-coverage)
                 :backend-id (:backend-id subject)
                 :input-artifact-id (:input-artifact-id subject)
                 :mir-op (:mir-op subject)
                 :domain-anchor (:domain-anchor subject)
                 :profile (:profile subject)
                 :target (:target subject)
                 :missing-evidence (:missing-evidence subject)
                 :fallback-status (or (:fallback-status subject) :rejected)
                 :remediation "Satisfy the B1 backend interface contract with verified MIR or domain IR input, profile and target eligibility, ABI/runtime/provider metadata, proof-backed target assumptions, capability preservation, unsupported-operation diagnostics, artifact metadata, and conformance records."}
                extra)))

(defn b1-document-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get b1-document-override-diagnostics fail-kind)]
      (b1-document-fail!
       id source-path
       {:stage :b1-document-coverage
        :backend-id :gravity.backend/interface-v1
        :input-artifact-id (str "b1-document-" (name fail-kind))
        :mir-op :checked-add
        :profile :hosted
        :target :jvm
        :missing-evidence [fail-kind]}
       {:missing-fields [fail-kind]}))))

(defn b1-document-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/b1-backend-interface-diagnostic-stream
   :stage :b1-document-coverage
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :b1-document-coverage
            :backend-id :gravity.backend/interface-v1
            :message-key (keyword "backend-interface" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "b1-document-syntax-" index)
                      :artifact input-id}
            :mir-op (case id
                      "B1-UNSUPPORTED" :target-specific-opcode
                      :checked-add)
            :domain-anchor (when (= id "B1-UNSUPPORTED") :unknown-domain)
            :profile :hosted
            :target :jvm
            :missing-evidence #{:verified-input :profile :target :abi
                                :runtime :effects :capabilities :safety
                                :proofs :source-map :dependencies}
            :fallback-status :rejected
            :facts {:verified-input-required? true
                    :safe-code-ub-forbidden? true
                    :metadata-required? true}
            :remediation [{:kind :verify-input-artifact}
                          {:kind :attach-interface-metadata}
                          {:kind :reject-unsupported-operation}]
            :redactions []
            :ordering-key [id :b1-document-coverage :jvm]})
         b1-document-diagnostic-ids
         (range))
   :status :complete})

(defn b1-document-validate!
  [source-path artifact]
  (let [interface (:backend-interface-artifact artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b1-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :complete (get-in interface
                                   [:capability-based-proof :status]))
      (b1-document-fail! "B1-INPUT" source-path interface
                         {:missing-fields [:backend-interface-proof]}))
    (when-not (= :complete (get-in artifact
                                   [:requirements-coverage
                                    :verified-input-status]))
      (b1-document-fail! "B1-INPUT" source-path (:requirements-coverage artifact)
                         {:missing-fields [:verified-input]}))
    (when-not (= :complete (get-in artifact
                                   [:requirements-coverage
                                    :eligibility-status]))
      (b1-document-fail! "B1-PROFILE" source-path
                         (:requirements-coverage artifact)
                         {:missing-fields [:eligibility]}))
    (when-not (= :accepted (get-in artifact
                                   [:requirements-coverage
                                    :proof-target-map-status]))
      (b1-document-fail! "B1-PROOF" source-path
                         (:requirements-coverage artifact)
                         {:missing-fields [:proof-to-target-metadata]}))
    (when-not (= :preserved (get-in artifact
                                    [:requirements-coverage
                                     :metadata-status]))
      (b1-document-fail! "B1-METADATA" source-path
                         (:requirements-coverage artifact)
                         {:missing-fields [:metadata]}))
    (when-not (= :passed (get-in artifact
                                 [:conformance-criteria-record :status]))
      (b1-document-fail! "B1-METADATA" source-path
                         (:conformance-criteria-record artifact)
                         {:missing-fields [:conformance]}))
    (when-not (= (set b1-document-diagnostic-ids) diagnostics)
      (b1-document-fail! "B1-INPUT" source-path
                         (:b1-diagnostic-stream artifact)
                         {:missing-fields [:b1-diagnostics]})))
  :complete)

(defn b1-document-capability-proof
  [artifact]
  (let [diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b1-diagnostic-stream
                                       :diagnostics])))]
    {:backend-interface-input-verified?
     (= :complete (get-in artifact
                          [:backend-interface-artifact
                           :capability-based-proof :status]))
     :manifest-validation-covered?
     (= :complete (get-in artifact
                          [:requirements-coverage :manifest-status]))
     :verified-input-and-rejection-covered?
     (= :complete (get-in artifact
                          [:requirements-coverage
                           :verified-input-status]))
     :profile-target-eligibility-covered?
     (= :complete (get-in artifact
                          [:requirements-coverage :eligibility-status]))
     :proof-backed-target-metadata-covered?
     (= :accepted (get-in artifact
                          [:requirements-coverage
                           :proof-target-map-status]))
     :unsupported-operation-diagnostics-covered?
     (contains? diagnostics "B1-UNSUPPORTED")
     :artifact-manifest-emission-covered?
     (= :complete (get-in artifact
                          [:requirements-coverage :artifact-status]))
     :metadata-preservation-covered?
     (= :preserved (get-in artifact
                           [:requirements-coverage :metadata-status]))
     :backend-conformance-record-covered?
     (= :passed (get-in artifact
                        [:conformance-criteria-record :status]))
     :diagnostics-covered?
     (= (set b1-document-diagnostic-ids) diagnostics)
     :status :complete}))