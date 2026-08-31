

(defn r2-document-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/r2-no-runtime-diagnostic-stream
   :stage :r2-document-coverage
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :r2-document-coverage
            :document-id "R2"
            :message-key (keyword "runtime" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "p08-r2-document-syntax-" index)
                      :artifact input-id}
            :profile :firmware
            :target {:backend :c :platform :bare-metal}
            :runtime-family :no-runtime
            :service-id (case id
                          "R2-HIDDEN-SERVICE" :hidden-runtime-service
                          "R2-STARTUP" :reset-handler
                          "R2-MEMORY" :static-memory
                          "R2-DISPATCH" :runtime-dispatch
                          "R2-FAILURE" :panic-trap
                          "R2-CAPABILITY" :mmio
                          "R2-PROOF" :boundedness-proof
                          :no-runtime-manifest)
            :memory-region (case id
                             "R2-MEMORY" :sram
                             "R2-CAPABILITY" :mmio
                             nil)
            :missing-proof (case id
                             "R2-PROOF" :boundedness-or-initialization
                             "R2-DISPATCH" :lowered-static-dispatch
                             nil)
            :capability (case id
                          "R2-CAPABILITY" :hardware/mmio
                          nil)
            :artifact-id input-id
            :missing-policy (r2-document-missing-policy id)
            :source-generated-origin-chain
            [:runtime-selection :no-runtime-manifest :r2-document-coverage]
            :facts {:runtime-none-required true
                    :hidden-runtime-services-forbidden true
                    :heap-allocation-rejected true
                    :check-elision_requires_proof true}
            :remediation [{:kind :declare-runtime-none}
                          {:kind :attach-startup-memory-failure-records}
                          {:kind :reject-hidden-runtime-service}
                          {:kind :attach-target-authority-proof}]
            :redactions []
            :ordering-key [id :r2-document-coverage]})
         r2-document-diagnostic-ids
         (range))
   :status :complete})

(defn r2-document-requirements-coverage
  [runtime-artifact]
  (let [no-runtime (:no-runtime-manifest runtime-artifact)
        mmio-region (some #(when (= :mmio (:name %)) %)
                          (get-in no-runtime [:memory-map :regions]))
        mmio-capability (some #(when (= :hardware/mmio (:capability %)) %)
                              (get-in runtime-artifact
                                      [:runtime-capability-enforcement-table
                                       :rows]))]
    {:artifact :gravity/r2-no-runtime-requirements-coverage
     :runtime-selection-input (:artifact-id runtime-artifact)
     :manifest-status (:status no-runtime)
     :runtime (:runtime no-runtime)
     :profile (:profile no-runtime)
     :target (:target no-runtime)
     :startup-reset-status
     (get-in no-runtime [:startup-reset-record :status])
     :section-layout-status
     (get-in no-runtime [:section-layout :status])
     :memory-map-status
     (get-in no-runtime [:memory-map :status])
     :stack-bound-status
     (get-in no-runtime [:stack-bound-report :status])
     :static-allocation-status
     (get-in no-runtime [:static-allocation-report :status])
     :heap (:heap (:memory no-runtime))
     :forbidden-service-status
     (get-in no-runtime [:forbidden-service-report :status])
     :hidden-runtime-services (:hidden-runtime-services no-runtime)
     :generated-support (:generated-support no-runtime)
     :failure-policy-status
     (get-in no-runtime [:failure-policy :status])
     :proof-record-status
     (get-in no-runtime [:proof-record :status])
     :mmio-region-status (if mmio-region :complete :missing)
     :mmio-capability-status (if mmio-capability :complete :missing)
     :boot-smoke-evidence
     (get-in no-runtime [:proof-record :boot-smoke-evidence])
     :status :complete}))

(defn r2-document-validate!
  [source-path artifact]
  (let [runtime-artifact (:runtime-selection-artifact artifact)
        coverage (:requirements-coverage artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:r2-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :complete (get-in runtime-artifact
                                   [:capability-based-proof :status]))
      (r2-document-fail! "R2-MANIFEST" source-path runtime-artifact
                         {:missing-fields [:runtime-selection-proof]}))
    (when-not (= :none (:runtime coverage))
      (r2-document-fail! "R2-MANIFEST" source-path coverage
                         {:missing-fields [:runtime-none]}))
    (when-not (= :complete (:startup-reset-status coverage))
      (r2-document-fail! "R2-STARTUP" source-path coverage
                         {:missing-fields [:startup-reset]}))
    (when-not (and (= :complete (:memory-map-status coverage))
                   (= :bounded (:stack-bound-status coverage))
                   (= :complete (:static-allocation-status coverage))
                   (= :none (:heap coverage)))
      (r2-document-fail! "R2-MEMORY" source-path coverage
                         {:missing-fields [:memory-map-stack-static-heap]}))
    (when (seq (:hidden-runtime-services coverage))
      (r2-document-fail! "R2-HIDDEN-SERVICE" source-path coverage
                         {:missing-fields [:hidden-runtime-services]}))
    (when-not (= :complete (:failure-policy-status coverage))
      (r2-document-fail! "R2-FAILURE" source-path coverage
                         {:missing-fields [:failure-policy]}))
    (when-not (= :complete (:mmio-capability-status coverage))
      (r2-document-fail! "R2-CAPABILITY" source-path coverage
                         {:missing-fields [:mmio-capability]}))
    (when-not (= :complete (:proof-record-status coverage))
      (r2-document-fail! "R2-PROOF" source-path coverage
                         {:missing-fields [:proof-record]}))
    (when-not (contains? (:generated-support coverage)
                         :static-dispatch-table)
      (r2-document-fail! "R2-DISPATCH" source-path coverage
                         {:missing-fields [:static-dispatch]}))
    (when-not (= :passed (get-in artifact
                                 [:conformance-criteria-record :status]))
      (r2-document-fail! "R2-MANIFEST" source-path
                         (:conformance-criteria-record artifact)
                         {:missing-fields [:conformance]}))
    (when-not (= (set r2-document-diagnostic-ids) diagnostics)
      (r2-document-fail! "R2-MANIFEST" source-path
                         (:r2-diagnostic-stream artifact)
                         {:missing-fields [:r2-diagnostics]})))
  :complete)

(defn r2-document-capability-proof
  [artifact]
  (let [coverage (:requirements-coverage artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:r2-diagnostic-stream
                                       :diagnostics])))]
    {:runtime-selection-input-verified?
     (= :complete (get-in artifact
                          [:runtime-selection-artifact
                           :capability-based-proof :status]))
     :runtime-none-manifest-covered?
     (= :none (:runtime coverage))
     :startup-section-memory-covered?
     (and (= :complete (:startup-reset-status coverage))
          (= :complete (:section-layout-status coverage))
          (= :complete (:memory-map-status coverage))
          (= :bounded (:stack-bound-status coverage))
          (= :complete (:static-allocation-status coverage)))
     :hidden-services-rejected?
     (empty? (:hidden-runtime-services coverage))
     :failure-policy-covered?
     (= :complete (:failure-policy-status coverage))
     :target-authority-covered?
     (= :complete (:mmio-capability-status coverage))
     :proof-evidence-covered?
     (= :complete (:proof-record-status coverage))
     :generated-support-provenance-covered?
     (every? (:generated-support coverage)
             [:bounds-checks :numeric-checks :panic-trap-stub
              :static-dispatch-table :startup-glue :mmio-accessor])
     :diagnostics-covered?
     (= (set r2-document-diagnostic-ids) diagnostics)
     :status :complete}))