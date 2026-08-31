

(defn profile-set-source-artifact
  [source-path source-text]
  (try
    (let [manifest-artifact (profile-manifest-source-artifact source-path source-text)
          manifest (:profile-manifest manifest-artifact)
          profile (:profile manifest)
          document (profile-documents-by-profile profile)
          _ (when-not document
              (fail! "P1-PROFILE-UNSUPPORTED"
                     "profile-set covers only :core, :meta, :hosted, and :native"
                     {:source-span {:source source-path}
                      :profile profile
                      :supported first-profile-set-order
                      :remediation "Use profile-manifest for the generic P1 artifact or a later profile-set task."}))
          matrix {:profile profile
                  :document document
                  :effects {:source (:source-effects manifest)
                            :inferred (:inferred-effects manifest)
                            :effective (:effective-effects manifest)
                            :permission-table (:effect-permission-table
                                               manifest-artifact)}
                  :capabilities {:source (:source-capabilities manifest)
                                 :required (:required-capabilities manifest)
                                 :effective (:effective-capabilities manifest)
                                 :permission-table (:capability-permission-table
                                                    manifest-artifact)}
                  :memory (:memory-regime manifest)
                  :runtime (:runtime-assumptions manifest)}
          conformance (profile-set-conformance-fixture profile document matrix)]
      {:kind :gravity/stage0-profile-set-artifact
       :document-set ["P2" "P3" "P4" "P5"]
       :pass {:name :core-meta-hosted-native-profile-validation
              :input :profile-manifest
              :output :effect-capability-matrix
              :requires [:reader :namespace-analyzer :macro-expansion
                         :core-lowering :type-effect-capability-check
                         :profile-manifest-validation]
              :preserves [:source-spans :generated-origin :profile :target
                          :effects :capabilities :memory-regime
                          :runtime-assumptions]
              :emits [:profile-manifest :effect-capability-matrix
                      :profile-specific-report :profile-set-conformance-fixture]
              :rejects profile-set-diagnostic-ids}
       :profile-manifest-artifact-hash (str "sha256:"
                                            (sha256-hex
                                             (pr-str manifest-artifact)))
       :profile-manifest manifest
       :profile-contract (:profile-contract manifest-artifact)
       :effect-capability-matrix matrix
       :profile-specific-report {:document document
                                 :profile profile
                                 :memory-regime (:memory matrix)
                                 :runtime-assumptions (:runtime matrix)
                                 :backend-eligibility
                                 (:backend-eligibility-report
                                  manifest-artifact)
                                 :diagnostic-ids
                                 (get profile-set-diagnostic-ids-by-document
                                      document)
                                 :status :complete}
       :profile-set-conformance-fixture conformance
       :diagnostics []})
    (catch clojure.lang.ExceptionInfo ex
      (throw-profile-set-diagnostic! ex))))

(def constrained-profile-order
  [:firmware :kernel :hardware :gpu :formal])

(def constrained-profile-documents-by-profile
  {:firmware "P6"
   :kernel "P7"
   :hardware "P8"
   :gpu "P11"
   :formal "P12"})

(def constrained-profile-diagnostic-ids-by-document
  {"P6" ["P6-GC" "P6-ALLOC" "P6-STACK" "P6-STATIC" "P6-MMIO"
         "P6-INTERRUPT" "P6-LATENCY" "P6-HOST" "P6-EXCEPTION"
         "P6-CAPABILITY"]
   "P7" ["P7-HIDDEN-ALLOC" "P7-GC" "P7-RAW-MEMORY" "P7-MMIO"
         "P7-INTERRUPT" "P7-SCHEDULER" "P7-ATOMIC" "P7-EXCEPTION"
         "P7-ABI" "P7-AUTHORITY"]
   "P8" ["P8-WIDTH" "P8-CLOCK" "P8-RESET" "P8-CDC" "P8-UNBOUNDED"
         "P8-RUNTIME" "P8-PORT" "P8-NUMERIC" "P8-TIMING"
         "P8-TARGET" "P8-CAPABILITY" "P8-TAG" "P8-COMPARTMENT"
         "P8-TEMPORAL" "P8-SYNTHESIS"]
   "P11" ["P11-HOST-EFFECT" "P11-DEVICE-MEMORY" "P11-TRANSFER"
          "P11-SYNC" "P11-ALIAS" "P11-TARGET-FEATURE" "P11-LAUNCH"
          "P11-MATH" "P11-RAW" "P11-BOUNDARY"]
   "P12" ["P12-NONDETERMINISM" "P12-EFFECT" "P12-MATH-MODE"
          "P12-ASSUMPTION" "P12-PROOF" "P12-CERTIFICATE"
          "P12-TRUST" "P12-UNSAFE" "P12-SYMBOLIC-LOWERING"
          "P12-BACKEND"]})

(def constrained-profile-diagnostic-ids
  (vec (mapcat constrained-profile-diagnostic-ids-by-document
               ["P6" "P7" "P8" "P11" "P12"])))

(def constrained-profile-diagnostic-mapping
  {[:firmware "P1-EFFECT"] "P6-HOST"
   [:firmware "P1-CAPABILITY"] "P6-CAPABILITY"
   [:firmware "P1-MEMORY"] "P6-ALLOC"
   [:firmware "P1-RUNTIME"] "P6-HOST"
   [:firmware "P1-BACKEND"] "P6-CAPABILITY"
   [:kernel "P1-EFFECT"] "P7-AUTHORITY"
   [:kernel "P1-CAPABILITY"] "P7-AUTHORITY"
   [:kernel "P1-MEMORY"] "P7-HIDDEN-ALLOC"
   [:kernel "P1-RUNTIME"] "P7-AUTHORITY"
   [:kernel "P1-BACKEND"] "P7-ABI"
   [:hardware "P1-EFFECT"] "P8-RUNTIME"
   [:hardware "P1-CAPABILITY"] "P8-CAPABILITY"
   [:hardware "P1-MEMORY"] "P8-RUNTIME"
   [:hardware "P1-RUNTIME"] "P8-RUNTIME"
   [:hardware "P1-BACKEND"] "P8-TARGET"
   [:gpu "P1-EFFECT"] "P11-HOST-EFFECT"
   [:gpu "P1-CAPABILITY"] "P11-TARGET-FEATURE"
   [:gpu "P1-MEMORY"] "P11-DEVICE-MEMORY"
   [:gpu "P1-RUNTIME"] "P11-BOUNDARY"
   [:gpu "P1-BACKEND"] "P11-TARGET-FEATURE"
   [:formal "P1-EFFECT"] "P12-EFFECT"
   [:formal "P1-CAPABILITY"] "P12-TRUST"
   [:formal "P1-MEMORY"] "P12-UNSAFE"
   [:formal "P1-RUNTIME"] "P12-EFFECT"
   [:formal "P1-BACKEND"] "P12-BACKEND"})

(def constrained-profile-required-artifacts
  {:firmware [:stack-budget-record :static-memory-budget
              :bounded-allocation-report :interrupt-capability-table
              :mmio-address-map :vector-table-record :linker-script-record
              :latency-loop-bound-report :firmware-image-manifest
              :unsafe-audit-records]
   :kernel [:kernel-capability-manifest :kernel-memory-map
            :allocator-policy :interrupt-safety-report
            :scheduler-atomic-support-report :unsafe-island-audit-report
            :driver-abi-manifest :no-hidden-allocation-proof]
   :hardware [:hardware-ir :hdl-module :hardware-target-manifest
              :fixed-width-layout-manifest
              :capability-pointer-layout-manifest
              :capability-tag-preservation-report :clock-domain-report
              :reset-domain-report :state-machine-graph :port-bus-manifest
              :timing-constraint-report :compartment-temporal-safety-report]
   :gpu [:host-device-boundary-manifest :kernel-ir
         :device-memory-lifetime-report :transfer-graph
         :synchronization-graph :target-feature-manifest
         :occupancy-launch-report :math-approximation-certificates]
   :formal [:symbolic-ir :proof-object :assumption-manifest
            :trusted-kernel-record :checked-theorem-summary
            :certificate-hash-chain :math-mode-rounding-record
            :imported-proof-verification-record]})

(def constrained-profile-artifact-diagnostic-by-key
  {:firmware {:stack-budget-record "P6-STACK"
              :static-memory-budget "P6-STATIC"
              :bounded-allocation-report "P6-ALLOC"
              :interrupt-capability-table "P6-INTERRUPT"
              :mmio-address-map "P6-MMIO"
              :vector-table-record "P6-CAPABILITY"
              :linker-script-record "P6-CAPABILITY"
              :latency-loop-bound-report "P6-LATENCY"
              :firmware-image-manifest "P6-CAPABILITY"
              :unsafe-audit-records "P6-MMIO"}
   :kernel {:kernel-capability-manifest "P7-AUTHORITY"
            :kernel-memory-map "P7-MMIO"
            :allocator-policy "P7-HIDDEN-ALLOC"
            :interrupt-safety-report "P7-INTERRUPT"
            :scheduler-atomic-support-report "P7-SCHEDULER"
            :unsafe-island-audit-report "P7-RAW-MEMORY"
            :driver-abi-manifest "P7-ABI"
            :no-hidden-allocation-proof "P7-HIDDEN-ALLOC"}
   :hardware {:hardware-ir "P8-SYNTHESIS"
              :hdl-module "P8-SYNTHESIS"
              :hardware-target-manifest "P8-TARGET"
              :fixed-width-layout-manifest "P8-WIDTH"
              :capability-pointer-layout-manifest "P8-CAPABILITY"
              :capability-tag-preservation-report "P8-TAG"
              :clock-domain-report "P8-CLOCK"
              :reset-domain-report "P8-RESET"
              :state-machine-graph "P8-SYNTHESIS"
              :port-bus-manifest "P8-PORT"
              :timing-constraint-report "P8-TIMING"
              :compartment-temporal-safety-report "P8-TEMPORAL"}
   :gpu {:host-device-boundary-manifest "P11-BOUNDARY"
         :kernel-ir "P11-BOUNDARY"
         :device-memory-lifetime-report "P11-DEVICE-MEMORY"
         :transfer-graph "P11-TRANSFER"
         :synchronization-graph "P11-SYNC"
         :target-feature-manifest "P11-TARGET-FEATURE"
         :occupancy-launch-report "P11-LAUNCH"
         :math-approximation-certificates "P11-MATH"}
   :formal {:symbolic-ir "P12-SYMBOLIC-LOWERING"
            :proof-object "P12-PROOF"
            :assumption-manifest "P12-ASSUMPTION"
            :trusted-kernel-record "P12-TRUST"
            :checked-theorem-summary "P12-PROOF"
            :certificate-hash-chain "P12-CERTIFICATE"
            :math-mode-rounding-record "P12-MATH-MODE"
            :imported-proof-verification-record "P12-CERTIFICATE"}})

(defn profile-validation-diagnostic-id
  [data]
  (let [id (:id data)]
    (cond
      (contains? (set constrained-profile-diagnostic-ids) id) id
      :else (get constrained-profile-diagnostic-mapping
                 [(or (:active-profile data) (:profile data)) id]))))

(defn throw-profile-validation-diagnostic!
  [ex]
  (let [data (ex-data ex)]
    (if-let [id (profile-validation-diagnostic-id data)]
      (throw (diagnostic id
                         (or (:message data)
                             (str "profile validation diagnostic " id))
                         (merge (dissoc data :id :message)
                                {:underlying-diagnostic (:id data)
                                 :underlying-message (:message data)
                                 :active-profile (or (:active-profile data)
                                                     (:profile data))
                                 :target (:target data)
                                 :legal-alternative (:remediation data)
                                 :diagnostic-family :constrained-profile-validation})))
      (throw ex))))

(defn profile-artifact-present?
  [profile-validation key]
  (let [sentinel ::missing
        value (get profile-validation key sentinel)]
    (and (not= sentinel value)
         (not (nil? value))
         (not (and (coll? value) (empty? value))))))