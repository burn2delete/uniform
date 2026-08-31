

(defn hosted-lowering-validate!
  [source-path artifact]
  (let [diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:hosted-diagnostic-stream
                                       :diagnostics])))
        manifests (:artifact-manifests artifact)]
    (when-not (= :complete (get-in artifact
                                   [:backend-interface-artifact
                                    :capability-based-proof :status]))
      (hosted-lowering-fail! "B5-MANIFEST" source-path
                             (:backend-interface-artifact artifact)
                             {:missing-fields [:backend-interface-proof]}))
    (when-not (= #{:gravity.backend/wasm :gravity.backend/jvm
                   :gravity.backend/js-ts}
                 (set (map :backend
                           (:target-lowering-manifest artifact))))
      (hosted-lowering-fail! "B4-MANIFEST" source-path
                             (:target-lowering-manifest artifact)
                             {:missing-fields [:hosted-backends]}))
    (when-not (= :complete (get-in artifact [:wasm-backend :status]))
      (hosted-lowering-fail! "B4-MANIFEST" source-path
                             (:wasm-backend artifact)
                             {:missing-fields [:wasm-backend]}))
    (when-not (= :declared (get-in artifact
                                   [:wasm-backend
                                    :import-capability-manifest
                                    :status]))
      (hosted-lowering-fail! "B4-IMPORT" source-path
                             (get-in artifact
                                     [:wasm-backend
                                      :import-capability-manifest])
                             {:missing-fields [:wasm-import-capabilities]}))
    (when-not (= :complete (get-in artifact [:jvm-backend :status]))
      (hosted-lowering-fail! "B5-MANIFEST" source-path
                             (:jvm-backend artifact)
                             {:missing-fields [:jvm-backend]}))
    (when-not (= :checked (get-in artifact
                                  [:jvm-backend :nullability-map
                                   :status]))
      (hosted-lowering-fail! "B5-NULL" source-path
                             (get-in artifact
                                     [:jvm-backend :nullability-map])
                             {:missing-fields [:jvm-nullability-map]}))
    (when-not (= :complete (get-in artifact [:js-ts-backend :status]))
      (hosted-lowering-fail! "B6-MANIFEST" source-path
                             (:js-ts-backend artifact)
                             {:missing-fields [:js-ts-backend]}))
    (when-not (= :declared (get-in artifact
                                   [:js-ts-backend
                                    :capability-manifest :status]))
      (hosted-lowering-fail! "B6-GLOBAL" source-path
                             (get-in artifact
                                     [:js-ts-backend
                                      :capability-manifest])
                             {:missing-fields [:js-ts-capabilities]}))
    (when-not (every? #(set/subset?
                        (set native-artifact-manifest-required-fields)
                        (set (keys %)))
                      manifests)
      (hosted-lowering-fail! "B4-MANIFEST" source-path
                             (first manifests)
                             {:missing-fields [:artifact-manifest]}))
    (when-not (every? #(re-find #"^sha256:" (:content-hash %)) manifests)
      (hosted-lowering-fail! "B6-MANIFEST" source-path
                             (first manifests)
                             {:missing-fields [:content-hash]}))
    (when-not (= :preserved (get-in artifact
                                    [:metadata-preservation-report
                                     :status]))
      (hosted-lowering-fail! "B6-MANIFEST" source-path
                             (:metadata-preservation-report artifact)
                             {:missing-fields [:metadata-preservation]}))
    (when-not (= :passed (get-in artifact
                                 [:backend-conformance-record :status]))
      (hosted-lowering-fail! "B4-MANIFEST" source-path
                             (:backend-conformance-record artifact)
                             {:missing-fields [:conformance]}))
    (when-not (= (set hosted-lowering-diagnostic-ids) diagnostics)
      (hosted-lowering-fail! "B6-MANIFEST" source-path
                             (:hosted-diagnostic-stream artifact)
                             {:missing-fields [:hosted-diagnostics]})))
  :complete)

(defn hosted-lowering-capability-proof
  [artifact]
  (let [diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:hosted-diagnostic-stream
                                       :diagnostics])))
        manifests (:artifact-manifests artifact)]
    {:backend-interface-input-verified?
     (= :complete (get-in artifact
                          [:backend-interface-artifact
                           :capability-based-proof :status]))
     :wasm-lowering-complete?
     (= :complete (get-in artifact [:wasm-backend :status]))
     :wasm-host-authority-declared?
     (= :declared (get-in artifact
                          [:wasm-backend
                           :import-capability-manifest :status]))
     :jvm-lowering-complete?
     (= :complete (get-in artifact [:jvm-backend :status]))
     :jvm-null-exception-boundaries-checked?
     (and (= :checked (get-in artifact
                              [:jvm-backend :nullability-map :status]))
          (= :translated (get-in artifact
                                 [:jvm-backend :exception-translation-map
                                  :status])))
     :js-ts-lowering-complete?
     (= :complete (get-in artifact [:js-ts-backend :status]))
     :js-ts-host-boundaries-declared?
     (= :declared (get-in artifact
                          [:js-ts-backend :capability-manifest :status]))
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
     (= (set hosted-lowering-diagnostic-ids) diagnostics)
     :status :complete}))