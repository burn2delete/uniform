

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
  (gravity.lowering/native-backend-artifact-manifest
   {:artifact-id #'c4-artifact-id} backend kind content input-id evidence-id))

(defn native-lowering-validate!
  [source-path artifact]
  (gravity.lowering/validate-native-backends!
   {:fail! #'native-lowering-fail!
    :manifest-required-fields #'native-artifact-manifest-required-fields
    :diagnostic-ids #'native-lowering-diagnostic-ids}
   source-path artifact))

(defn native-lowering-capability-proof
  [artifact]
  (gravity.lowering/native-backends-coverage-report
   {:manifest-required-fields #'native-artifact-manifest-required-fields
    :diagnostic-ids #'native-lowering-diagnostic-ids}
   artifact))
