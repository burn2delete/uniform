

(defn r5-document-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/r5-memory-runtime-diagnostic-stream
   :stage :r5-document-coverage
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :r5-document-coverage
            :document-id "R5"
            :message-key (keyword "runtime" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "p08-r5-document-syntax-" index)
                      :artifact input-id}
            :profile :native
            :target {:backend :llvm :platform :linux}
            :runtime-family :memory
            :allocation-id (str "alloc-" index)
            :resource-id (str "resource-" index)
            :provider (case id
                        "R5-PROVIDER" :missing-provider
                        "R5-ALLOC" :no-allocation-region
                        "R5-LIFETIME" :region
                        "R5-RAW" :raw
                        "R5-DEVICE" :device
                        :memory/region-arena)
            :lifetime (case id
                        "R5-LIFETIME" :escaped-region
                        "R5-DEVICE" :missing-device-lifetime
                        :lexical-region)
            :effect (case id
                      "R5-ALLOC" :memory/allocate
                      "R5-RAW" :memory/raw
                      "R5-DEVICE" :memory/device
                      nil)
            :capability (case id
                          "R5-RAW" :memory/raw
                          "R5-DEVICE" :device/memory
                          nil)
            :proof-id (when (= "R5-PROOF" id) "safe15-runtime-check-proof")
            :artifact-id input-id
            :missing-policy (r5-document-missing-policy id)
            :source-generated-origin-chain
            [:minimal-native-runtime :memory-runtime :r5-document-coverage]
            :facts {:compiler-memory-facts-preserved true
                    :safe-wrapper-required-for-raw-memory true
                    :runtime-checks-agree-with-proofs true}
            :remediation [{:kind :select-memory-provider}
                          {:kind :attach-lifetime-record}
                          {:kind :audit-raw-memory}
                          {:kind :preserve-runtime-check-or-proof}]
            :redactions []
            :ordering-key [id :r5-document-coverage]})
         r5-document-diagnostic-ids
         (range))
   :status :complete})

(defn r5-document-requirements-coverage
  [minimal-artifact]
  (let [memory (:memory-runtime-manifest minimal-artifact)]
    {:artifact :gravity/r5-memory-runtime-requirements-coverage
     :minimal-native-input (:artifact-id minimal-artifact)
     :manifest-status (:status memory)
     :family (:family memory)
     :provider-family-statuses
     (set (map :status (vals (:provider-families memory))))
     :provider-selection-status
     (get-in memory [:provider-selection-record :status])
     :allocation-status
     (get-in memory [:allocation-deallocation-contract :status])
     :region-arena-status
     (get-in memory [:region-arena-manifest :status])
     :ownership-borrow-status
     (get-in memory [:ownership-borrow-runtime-check-map :status])
     :compiler-facts-preserved?
     (get-in memory [:ownership-borrow-runtime-check-map
                     :compiler-facts-preserved?])
     :linear-ledger-status
     (get-in memory [:linear-resource-ledger :status])
     :duplicated-handles
     (get-in memory [:linear-resource-ledger :duplicated-handles])
     :unconsumed-handles
     (get-in memory [:linear-resource-ledger :unconsumed-handles])
     :raw-audit-statuses
     (set (map :status (:raw-memory-unsafe-audit-records memory)))
     :device-memory-status
     (get-in memory [:device-memory-provider-manifest :status])
     :debug-trace-status
     (get-in memory [:debug-allocation-trace-schema :status])
     :hidden-authority-effects
     (get-in memory [:debug-allocation-trace-schema
                     :hidden-authority-effects])
     :proof-agreement-status
     (get-in memory [:runtime-check-proof-agreement :status])
     :unproved-elisions
     (get-in memory [:runtime-check-proof-agreement :unproved-elisions])
     :status :complete}))

(defn r5-document-validate!
  [source-path artifact]
  (let [minimal-artifact (:minimal-native-memory-artifact artifact)
        coverage (:requirements-coverage artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:r5-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :complete (get-in minimal-artifact
                                   [:capability-based-proof :status]))
      (r5-document-fail! "R5-MANIFEST" source-path minimal-artifact
                         {:missing-fields [:memory-runtime-proof]}))
    (when-not (and (= :complete (:manifest-status coverage))
                   (= :memory (:family coverage)))
      (r5-document-fail! "R5-MANIFEST" source-path coverage
                         {:missing-fields [:memory-manifest]}))
    (when-not (= :complete (:provider-selection-status coverage))
      (r5-document-fail! "R5-PROVIDER" source-path coverage
                         {:missing-fields [:provider-selection]}))
    (when-not (= :complete (:allocation-status coverage))
      (r5-document-fail! "R5-ALLOC" source-path coverage
                         {:missing-fields [:allocation-contract]}))
    (when-not (= :complete (:region-arena-status coverage))
      (r5-document-fail! "R5-LIFETIME" source-path coverage
                         {:missing-fields [:region-arena]}))
    (when-not (and (= :complete (:linear-ledger-status coverage))
                   (empty? (:duplicated-handles coverage))
                   (empty? (:unconsumed-handles coverage)))
      (r5-document-fail! "R5-LINEAR" source-path coverage
                         {:missing-fields [:linear-ledger]}))
    (when-not (contains? (:raw-audit-statuses coverage) :audited)
      (r5-document-fail! "R5-RAW" source-path coverage
                         {:missing-fields [:raw-audit]}))
    (when-not (= :complete (:device-memory-status coverage))
      (r5-document-fail! "R5-DEVICE" source-path coverage
                         {:missing-fields [:device-memory]}))
    (when-not (true? (:compiler-facts-preserved? coverage))
      (r5-document-fail! "R5-BOUNDS" source-path coverage
                         {:missing-fields [:runtime-check-map]}))
    (when (seq (:unproved-elisions coverage))
      (r5-document-fail! "R5-PROOF" source-path coverage
                         {:missing-fields [:proof-elision]}))
    (when (seq (:hidden-authority-effects coverage))
      (r5-document-fail! "R5-DEBUG" source-path coverage
                         {:missing-fields [:debug-hidden-authority]}))
    (when-not (= (set r5-document-diagnostic-ids) diagnostics)
      (r5-document-fail! "R5-MANIFEST" source-path
                         (:r5-diagnostic-stream artifact)
                         {:missing-fields [:r5-diagnostics]})))
  :complete)

(defn r5-document-capability-proof
  [artifact]
  (let [coverage (:requirements-coverage artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:r5-diagnostic-stream
                                       :diagnostics])))]
    {:memory-runtime-input-verified?
     (= :complete (get-in artifact
                          [:minimal-native-memory-artifact
                           :capability-based-proof :status]))
     :provider-selection-covered?
     (= :complete (:provider-selection-status coverage))
     :allocation-lifetime-covered?
     (and (= :complete (:allocation-status coverage))
          (= :complete (:region-arena-status coverage)))
     :linear-resource-covered?
     (and (= :complete (:linear-ledger-status coverage))
          (empty? (:duplicated-handles coverage))
          (empty? (:unconsumed-handles coverage)))
     :raw-memory-audited?
     (contains? (:raw-audit-statuses coverage) :audited)
     :device-memory-covered?
     (= :complete (:device-memory-status coverage))
     :runtime-checks-preserve-compiler-facts?
     (true? (:compiler-facts-preserved? coverage))
     :debug-trace-safe?
     (empty? (:hidden-authority-effects coverage))
     :proof-elision-covered?
     (empty? (:unproved-elisions coverage))
     :diagnostics-covered?
     (= (set r5-document-diagnostic-ids) diagnostics)
     :status :complete}))