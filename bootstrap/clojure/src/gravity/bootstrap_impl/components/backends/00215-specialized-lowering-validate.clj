

(defn specialized-lowering-validate!
  [source-path artifact]
  (let [diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:specialized-diagnostic-stream
                                       :diagnostics])))
        manifests (:artifact-manifests artifact)]
    (when-not (= :complete (get-in artifact
                                   [:backend-interface-artifact
                                    :capability-based-proof :status]))
      (specialized-lowering-fail!
       "B10-MANIFEST" source-path (:backend-interface-artifact artifact)
       {:missing-fields [:backend-interface-proof]}))
    (when-not (= #{:gravity.backend/gpu :gravity.backend/hdl
                   :gravity.backend/workflow-graph
                   :gravity.backend/query-relational
                   :gravity.backend/mobile}
                 (set (map :backend (:target-lowering-manifest artifact))))
      (specialized-lowering-fail!
       "B10-MANIFEST" source-path (:target-lowering-manifest artifact)
       {:missing-fields [:specialized-backends]}))
    (doseq [[path id] [[[:gpu-backend :status] "B8-MANIFEST"]
                       [[:hdl-backend :status] "B9-MANIFEST"]
                       [[:workflow-backend :status] "B10-MANIFEST"]
                       [[:query-backend :status] "B11-MANIFEST"]
                       [[:mobile-backend :status] "B12-MANIFEST"]]]
      (when-not (= :complete (get-in artifact path))
        (specialized-lowering-fail!
         id source-path artifact {:missing-fields path})))
    (when-not (= :declared (get-in artifact
                                   [:gpu-backend
                                    :host-device-boundary :status]))
      (specialized-lowering-fail!
       "B8-KERNEL" source-path (:gpu-backend artifact)
       {:missing-fields [:host-device-boundary]}))
    (when-not (= :complete (get-in artifact
                                   [:hdl-backend
                                    :clock-domain-report :status]))
      (specialized-lowering-fail!
       "B9-CLOCK" source-path (:hdl-backend artifact)
       {:missing-fields [:clock-domain-report]}))
    (when-not (= :complete (get-in artifact
                                   [:workflow-backend
                                    :replay-policy :status]))
      (specialized-lowering-fail!
       "B10-REPLAY" source-path (:workflow-backend artifact)
       {:missing-fields [:replay-policy]}))
    (when-not (= :parameterized (get-in artifact
                                        [:query-backend
                                         :prepared-binding-manifest
                                         :status]))
      (specialized-lowering-fail!
       "B11-PARAMETER" source-path (:query-backend artifact)
       {:missing-fields [:prepared-binding-manifest]}))
    (when-not (= :complete (get-in artifact
                                   [:mobile-backend
                                    :permission-manifest :status]))
      (specialized-lowering-fail!
       "B12-PERMISSION" source-path (:mobile-backend artifact)
       {:missing-fields [:permission-manifest]}))
    (when-not (every? #(set/subset?
                        (set native-artifact-manifest-required-fields)
                        (set (keys %)))
                      manifests)
      (specialized-lowering-fail!
       "B10-MANIFEST" source-path (first manifests)
       {:missing-fields [:artifact-manifest]}))
    (when-not (every? #(re-find #"^sha256:" (:content-hash %)) manifests)
      (specialized-lowering-fail!
       "B11-MANIFEST" source-path (first manifests)
       {:missing-fields [:content-hash]}))
    (when-not (= :preserved (get-in artifact
                                    [:metadata-preservation-report :status]))
      (specialized-lowering-fail!
       "B12-MANIFEST" source-path (:metadata-preservation-report artifact)
       {:missing-fields [:metadata-preservation]}))
    (when-not (= :passed (get-in artifact
                                 [:backend-conformance-record :status]))
      (specialized-lowering-fail!
       "B10-MANIFEST" source-path (:backend-conformance-record artifact)
       {:missing-fields [:conformance]}))
    (when-not (= (set specialized-lowering-diagnostic-ids) diagnostics)
      (specialized-lowering-fail!
       "B10-MANIFEST" source-path (:specialized-diagnostic-stream artifact)
       {:missing-fields [:specialized-diagnostics]})))
  :complete)

(defn specialized-lowering-capability-proof
  [artifact]
  (let [diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:specialized-diagnostic-stream
                                       :diagnostics])))
        manifests (:artifact-manifests artifact)]
    {:backend-interface-input-verified?
     (= :complete (get-in artifact
                          [:backend-interface-artifact
                           :capability-based-proof :status]))
     :gpu-lowering-complete?
     (= :complete (get-in artifact [:gpu-backend :status]))
     :gpu-host-device-boundary-declared?
     (= :declared (get-in artifact
                          [:gpu-backend :host-device-boundary :status]))
     :hdl-lowering-complete?
     (= :complete (get-in artifact [:hdl-backend :status]))
     :hdl-clock-reset-timing-complete?
     (and (= :complete (get-in artifact
                               [:hdl-backend :clock-domain-report :status]))
          (= :complete (get-in artifact
                               [:hdl-backend :timing-constraint-file
                                :status])))
     :workflow-lowering-complete?
     (= :complete (get-in artifact [:workflow-backend :status]))
     :workflow-replay-policy-complete?
     (= :complete (get-in artifact
                          [:workflow-backend :replay-policy :status]))
     :query-lowering-complete?
     (= :complete (get-in artifact [:query-backend :status]))
     :query-parameters-taint-checked?
     (= :parameterized (get-in artifact
                               [:query-backend
                                :prepared-binding-manifest :status]))
     :mobile-lowering-complete?
     (= :complete (get-in artifact [:mobile-backend :status]))
     :mobile-permissions-lifecycle-checked?
     (and (= :complete (get-in artifact
                               [:mobile-backend :permission-manifest
                                :status]))
          (= :complete (get-in artifact
                               [:mobile-backend :lifecycle-threading-map
                                :status])))
     :artifact-emission-complete?
     (and (= 5 (count manifests))
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
     (= (set specialized-lowering-diagnostic-ids) diagnostics)
     :status :complete}))