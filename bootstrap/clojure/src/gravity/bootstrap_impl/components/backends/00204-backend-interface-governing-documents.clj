

(def backend-interface-governing-documents
  ["docs/phase-07-backend-architecture/098-b1-backend-interface-specification.md"
   "docs/phase-07-backend-architecture/111-b14-backend-conformance-test-plan.md"])

(def backend-interface-diagnostic-ids
  ["B1-INPUT"
   "B1-PROFILE"
   "B1-TARGET"
   "B1-ABI"
   "B1-RUNTIME"
   "B1-PROOF"
   "B1-CAPABILITY"
   "B1-UNSUPPORTED"
   "B1-METADATA"
   "B14-COVERAGE"
   "B14-METADATA"
   "B14-ARTIFACT"])

(def backend-interface-diagnostic-messages
  {"B1-INPUT" "backend input is unverified or incomplete"
   "B1-PROFILE" "backend profile compatibility failed"
   "B1-TARGET" "backend target feature is unsupported"
   "B1-ABI" "backend ABI or layout is not representable"
   "B1-RUNTIME" "backend runtime or provider dependency is missing or forbidden"
   "B1-PROOF" "backend target assumption lacks proof metadata"
   "B1-CAPABILITY" "backend provider selection loses required authority"
   "B1-UNSUPPORTED" "backend MIR operation or domain anchor is unsupported"
   "B1-METADATA" "backend artifact metadata is incomplete"
   "B14-COVERAGE" "backend conformance fixture coverage is incomplete"
   "B14-METADATA" "backend conformance metadata preservation failed"
   "B14-ARTIFACT" "backend artifact manifest validation failed"})

(def backend-interface-override-diagnostics
  {:b1-input ["B1-INPUT" :backend-input]
   :b1-profile ["B1-PROFILE" :profile-eligibility]
   :b1-target ["B1-TARGET" :target-eligibility]
   :b1-abi ["B1-ABI" :abi-layout]
   :b1-runtime ["B1-RUNTIME" :runtime-provider]
   :b1-proof ["B1-PROOF" :proof-metadata]
   :b1-capability ["B1-CAPABILITY" :capability-preservation]
   :b1-unsupported ["B1-UNSUPPORTED" :unsupported-operation]
   :b1-metadata ["B1-METADATA" :artifact-metadata]
   :b14-coverage ["B14-COVERAGE" :conformance-coverage]
   :b14-metadata ["B14-METADATA" :conformance-metadata]
   :b14-artifact ["B14-ARTIFACT" :conformance-artifact]})

(def backend-manifest-required-fields
  [:artifact :backend :version :accepts :emits :requires
   :supports-profiles :rejects])

(def backend-input-required-fields
  [:input :profile :target :abi :runtime :providers :effects
   :capabilities :safety :proofs :source-map :dependencies])

(defn backend-interface-source-overrides
  [module]
  (or (get-in module [:metadata :backend :interface])
      (get-in module [:metadata :backend])
      {}))

(defn backend-interface-fail!
  [id source-path subject extra]
  (fail! id
         (get backend-interface-diagnostic-messages id
              "backend interface validation failed")
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :backend-interface
                 :stage (or (:stage subject) :backend-interface)
                 :backend-id (:backend-id subject)
                 :input-artifact-id (:input-artifact-id subject)
                 :mir-op (:mir-op subject)
                 :domain-anchor (:domain-anchor subject)
                 :profile (:profile subject)
                 :target (:target subject)
                 :missing-evidence (:missing-evidence subject)
                 :fallback-status (:fallback-status subject)
                 :remediation "Feed the backend only verified MIR or domain IR with profile, target, ABI, runtime, effects, capabilities, safety, proof, provenance, artifact, and conformance metadata."}
                extra)))

(defn backend-interface-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (let [[id subject-kind] (get backend-interface-override-diagnostics
                                 fail-kind)]
      (when id
        (backend-interface-fail!
         id source-path
         {:stage subject-kind
          :backend-id :gravity.backend/interface-v1
          :input-artifact-id (str "backend-input-" (name fail-kind))
          :mir-op :runtime-check
          :domain-anchor :efir
          :profile :hosted
          :target :jvm
          :missing-evidence [fail-kind]
          :fallback-status :unavailable}
         {:missing-fields [fail-kind]})))))

(defn backend-interface-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/backend-diagnostic-stream
   :stage :backend-interface
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :backend-interface
            :message-key (keyword "backend"
                                  (str/lower-case
                                   (str/replace id #"_" "-")))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "backend-syntax-" index)
                      :artifact input-id}
            :backend-id :gravity.backend/interface-v1
            :input-artifact-id input-id
            :mir-op (if (= id "B1-UNSUPPORTED")
                      :target-specific-opcode
                      :runtime-check)
            :domain-anchor (if (= id "B1-UNSUPPORTED") :opaque-domain :efir)
            :profile :hosted
            :target :jvm
            :missing-evidence #{:proof-metadata :source-map}
            :fallback-status :recorded
            :facts {:rule id
                    :metadata-required? true
                    :safe-ub-policy :reject}
            :remediation [{:kind :provide-backend-evidence}
                          {:kind :select-supported-target}]
            :redactions []
            :ordering-key [id :gravity.backend/interface-v1 :jvm]})
         backend-interface-diagnostic-ids
         (range))
   :status :complete})

(defn backend-interface-validate!
  [source-path artifact]
  (let [manifest (:backend-manifest artifact)
        input-packet (:backend-input-packet artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:backend-diagnostic-stream
                                       :diagnostics])))]
    (when-not (set/subset? (set backend-manifest-required-fields)
                           (set (keys manifest)))
      (backend-interface-fail! "B1-INPUT" source-path manifest
                               {:missing-fields
                                (vec (remove (set (keys manifest))
                                             backend-manifest-required-fields))}))
    (when-not (set/subset? (set backend-input-required-fields)
                           (set (keys input-packet)))
      (backend-interface-fail! "B1-INPUT" source-path input-packet
                               {:missing-fields
                                (vec (remove (set (keys input-packet))
                                             backend-input-required-fields))}))
    (when-not (= :eligible (get-in artifact
                                   [:backend-input-eligibility-report
                                    :decision]))
      (backend-interface-fail! "B1-PROFILE" source-path
                               (:backend-input-eligibility-report artifact)
                               {:missing-fields [:eligibility]}))
    (when-not (every? #(= :passed (:status %))
                      (:eligibility-checks artifact))
      (backend-interface-fail! "B1-TARGET" source-path
                               (first (:eligibility-checks artifact))
                               {:missing-fields [:eligibility-checks]}))
    (when-not (= :accepted (get-in artifact
                                   [:proof-to-target-metadata-map :status]))
      (backend-interface-fail! "B1-PROOF" source-path
                               (:proof-to-target-metadata-map artifact)
                               {:missing-fields [:proof-to-target-metadata]}))
    (when-not (= :preserved (get-in artifact
                                    [:capability-preservation-report
                                     :status]))
      (backend-interface-fail! "B1-CAPABILITY" source-path
                               (:capability-preservation-report artifact)
                               {:missing-fields [:capability-preservation]}))
    (when-not (= :recorded (get-in artifact
                                   [:unsupported-feature-report :status]))
      (backend-interface-fail! "B1-UNSUPPORTED" source-path
                               (:unsupported-feature-report artifact)
                               {:missing-fields [:unsupported-feature-report]}))
    (when-not (every? #(and (:provenance %) (:source-debug-map %)
                            (:safety-evidence %) (:conformance %))
                      (:target-artifact-manifest artifact))
      (backend-interface-fail! "B1-METADATA" source-path
                               (first (:target-artifact-manifest artifact))
                               {:missing-fields [:target-artifact-metadata]}))
    (when-not (= :passed (get-in artifact
                                 [:backend-conformance-record :status]))
      (backend-interface-fail! "B14-COVERAGE" source-path
                               (:backend-conformance-record artifact)
                               {:missing-fields [:backend-conformance-record]}))
    (when-not (= :preserved (get-in artifact
                                    [:metadata-preservation-report :status]))
      (backend-interface-fail! "B14-METADATA" source-path
                               (:metadata-preservation-report artifact)
                               {:missing-fields [:metadata-preservation]}))
    (when-not (= :valid (get-in artifact
                                [:artifact-manifest-validation-report
                                 :status]))
      (backend-interface-fail! "B14-ARTIFACT" source-path
                               (:artifact-manifest-validation-report artifact)
                               {:missing-fields [:artifact-manifest-validation]}))
    (when-not (= (set backend-interface-diagnostic-ids) diagnostics)
      (backend-interface-fail! "B14-COVERAGE" source-path
                               (:backend-diagnostic-stream artifact)
                               {:missing-fields [:backend-diagnostics]})))
  :complete)