

(defn runtime-selection-validate!
  [source-path artifact]
  (let [artifact-emission (:artifact-emission-artifact artifact)
        families (set (map :family
                           (get-in artifact
                                   [:runtime-family-selection-record
                                    :families])))
        service-table (:runtime-service-table artifact)
        no-runtime (:no-runtime-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:runtime-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :gravity/stage0-artifact-emission-artifact
                 (:kind artifact-emission))
      (runtime-selection-fail! "R1-MANIFEST" source-path artifact-emission
                               {:missing-fields [:artifact-emission-artifact]}))
    (when-not (= :complete (get-in artifact-emission
                                   [:capability-based-proof :status]))
      (runtime-selection-fail! "R1-MANIFEST" source-path artifact-emission
                               {:missing-fields [:artifact-emission-proof]}))
    (when-not (every? families
                      [:no-runtime :minimal-native :managed :distributed
                       :ai :repl])
      (runtime-selection-fail! "R1-SELECTION" source-path
                               (:runtime-family-selection-record artifact)
                               {:missing-fields [:runtime-families]}))
    (when-not (= #{:linked :generated :delegated :external :forbidden}
                 (:classification-kinds service-table))
      (runtime-selection-fail! "R1-SERVICE" source-path service-table
                               {:missing-fields [:service-classifications]}))
    (when (seq (get-in no-runtime
                       [:forbidden-service-report
                        :observed-hidden-services]))
      (runtime-selection-fail! "R1-FORBIDDEN" source-path no-runtime
                               {:missing-fields [:hidden-services]}))
    (when-not (= :complete
                 (get-in artifact
                         [:runtime-capability-enforcement-table
                          :status]))
      (runtime-selection-fail! "R1-CAPABILITY" source-path
                               (:runtime-capability-enforcement-table
                                artifact)
                               {:missing-fields [:capability-table]}))
    (when-not (= :none (:runtime no-runtime))
      (runtime-selection-fail! "R2-MANIFEST" source-path no-runtime
                               {:missing-fields [:runtime-none]}))
    (when-not (= :complete (get-in no-runtime
                                   [:startup-reset-record :status]))
      (runtime-selection-fail! "R2-STARTUP" source-path no-runtime
                               {:missing-fields [:startup-reset-record]}))
    (when-not (= :none (get-in no-runtime [:memory :heap]))
      (runtime-selection-fail! "R2-MEMORY" source-path no-runtime
                               {:missing-fields [:heap-none]}))
    (when-not (= :bounded (get-in no-runtime
                                  [:stack-bound-report :status]))
      (runtime-selection-fail! "R2-MEMORY" source-path no-runtime
                               {:missing-fields [:stack-bound-report]}))
    (when-not (contains? (:forbidden no-runtime) :runtime-dispatch)
      (runtime-selection-fail! "R2-DISPATCH" source-path no-runtime
                               {:missing-fields [:runtime-dispatch]}))
    (when-not (= :complete (get-in no-runtime [:failure-policy :status]))
      (runtime-selection-fail! "R2-FAILURE" source-path no-runtime
                               {:missing-fields [:failure-policy]}))
    (when-not (some #(= :hardware/mmio (:capability %))
                    (get-in artifact
                            [:runtime-capability-enforcement-table
                             :rows]))
      (runtime-selection-fail! "R2-CAPABILITY" source-path no-runtime
                               {:missing-fields [:target-authority]}))
    (when-not (= :complete (get-in no-runtime [:proof-record :status]))
      (runtime-selection-fail! "R2-PROOF" source-path no-runtime
                               {:missing-fields [:proof-record]}))
    (when-not (= :complete (get-in artifact
                                   [:runtime-package-permission-record
                                    :status]))
      (runtime-selection-fail! "R1-CAPABILITY" source-path
                               (:runtime-package-permission-record
                                artifact)
                               {:missing-fields [:package-permission-record]}))
    (when-not (= :complete (get-in artifact
                                   [:runtime-backend-consumption-record
                                    :status]))
      (runtime-selection-fail! "R1-MANIFEST" source-path
                               (:runtime-backend-consumption-record
                                artifact)
                               {:missing-fields [:backend-consumption]}))
    (when-not (= (set runtime-selection-diagnostic-ids) diagnostics)
      (runtime-selection-fail! "R1-MANIFEST" source-path
                               (:runtime-diagnostic-stream artifact)
                               {:missing-fields [:runtime-diagnostics]})))
  :complete)

(defn runtime-selection-capability-proof
  [artifact]
  (let [families (set (map :family
                           (get-in artifact
                                   [:runtime-family-selection-record
                                    :families])))
        no-runtime (:no-runtime-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:runtime-diagnostic-stream
                                       :diagnostics])))]
    {:artifact-emission-input-verified?
     (= :complete (get-in artifact
                          [:artifact-emission-artifact
                           :capability-based-proof :status]))
     :runtime-selection-explicit?
     (every? families [:no-runtime :minimal-native :managed :distributed
                       :ai :repl])
     :service-classification-complete?
     (= #{:linked :generated :delegated :external :forbidden}
        (get-in artifact [:runtime-service-table
                          :classification-kinds]))
     :no-runtime-absence-proven?
     (and (= :none (:runtime no-runtime))
          (empty? (:hidden-runtime-services no-runtime))
          (true? (get-in no-runtime
                         [:forbidden-service-report
                          :runtime-dependencies-absent?])))
     :forbidden-services-rejected?
     (every? (:forbidden no-runtime)
             [:gc :scheduler :dynamic-eval :reflection :host-io
              :classloading :managed-exceptions :hidden-allocator
              :dynamic-loader :repl :host-exceptions
              :runtime-dispatch])
     :startup-memory-failure-explicit?
     (and (= :complete (get-in no-runtime
                               [:startup-reset-record :status]))
          (= :complete (get-in no-runtime
                               [:section-layout :status]))
          (= :bounded (get-in no-runtime
                              [:stack-bound-report :status]))
          (= :complete (get-in no-runtime
                               [:failure-policy :status])))
     :capability-policy-closed?
     (and (= :complete (get-in artifact
                               [:runtime-capability-enforcement-table
                                :status]))
          (true? (get-in artifact
                         [:runtime-package-permission-record
                          :ambient-authority-rejected?])))
     :no-runtime-proof-record-complete?
     (= :complete (get-in no-runtime [:proof-record :status]))
     :backend-package-consumption-covered?
     (= :complete (get-in artifact
                          [:runtime-backend-consumption-record
                           :status]))
     :diagnostics-covered?
     (= (set runtime-selection-diagnostic-ids) diagnostics)
     :status :complete}))