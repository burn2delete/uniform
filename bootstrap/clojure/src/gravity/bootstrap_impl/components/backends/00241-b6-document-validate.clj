

(defn b6-document-validate!
  [source-path artifact]
  (let [hosted (:hosted-lowering-artifact artifact)
        manifest (:js-ts-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b6-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :gravity/stage0-hosted-lowering-artifact (:kind hosted))
      (b6-document-fail! "B6-MANIFEST" source-path hosted
                         {:missing-fields [:hosted-lowering-artifact]}))
    (when-not (= :complete (get-in hosted
                                   [:capability-based-proof :status]))
      (b6-document-fail! "B6-MANIFEST" source-path hosted
                         {:missing-fields [:hosted-lowering-proof]}))
    (when-not (= :pinned (get-in manifest
                                 [:runtime-and-module-target-record
                                  :status]))
      (b6-document-fail! "B6-TARGET" source-path manifest
                         {:missing-fields [:runtime-module-target-record]}))
    (when-not (= :complete (get-in manifest
                                   [:value-and-type-representation-record
                                    :status]))
      (b6-document-fail! "B6-MANIFEST" source-path manifest
                         {:missing-fields [:value-type-representation]}))
    (when-not (= :declared (get-in manifest
                                   [:capability-manifest :status]))
      (b6-document-fail! "B6-GLOBAL" source-path manifest
                         {:missing-fields [:capability-manifest]}))
    (when-not (= :complete (get-in manifest
                                   [:package-dependency-manifest
                                    :status]))
      (b6-document-fail! "B6-IMPORT" source-path manifest
                         {:missing-fields [:package-dependency-manifest]}))
    (when-not (= :checked
                 (get-in manifest
                         [:nullish-and-exception-translation-map
                          :status]))
      (b6-document-fail! "B6-NULLISH" source-path manifest
                         {:missing-fields [:nullish-exception-map]}))
    (when-not (= :complete (get-in manifest
                                   [:numeric-representation-manifest
                                    :status]))
      (b6-document-fail! "B6-NUMERIC" source-path manifest
                         {:missing-fields [:numeric-manifest]}))
    (when-not (= :complete (get-in manifest
                                   [:dynamic-code-and-prototype-policy
                                    :status]))
      (b6-document-fail! "B6-EVAL" source-path manifest
                         {:missing-fields [:dynamic-prototype-policy]}))
    (when-not (= :complete (get-in manifest
                                   [:async-effect-boundary-map :status]))
      (b6-document-fail! "B6-ASYNC" source-path manifest
                         {:missing-fields [:async-effect-boundary-map]}))
    (when-not (= :not-applicable (get-in manifest
                                         [:ui-component-binding-metadata
                                          :status]))
      (b6-document-fail! "B6-UI" source-path manifest
                         {:missing-fields [:ui-component-metadata]}))
    (when-not (b6-document-js-structurally-valid? b6-document-js-source)
      (b6-document-fail! "B6-MANIFEST" source-path manifest
                         {:missing-fields [:javascript-module-structure]}))
    (when-not (b6-document-ts-structurally-valid?
               b6-document-ts-declarations)
      (b6-document-fail! "B6-MANIFEST" source-path manifest
                         {:missing-fields [:typescript-declaration-structure]}))
    (when-not (b6-document-source-map-structurally-valid?
               b6-document-source-map)
      (b6-document-fail! "B6-MANIFEST" source-path manifest
                         {:missing-fields [:source-map-structure]}))
    (when-not (b6-document-package-structurally-valid?
               b6-document-package-json)
      (b6-document-fail! "B6-MANIFEST" source-path manifest
                         {:missing-fields [:package-json-structure]}))
    (when-not (every? #(contains? manifest %)
                      [:runtime-and-module-target-record
                       :javascript-module-artifacts
                       :typescript-declaration-files
                       :source-maps-and-generated-origin-maps
                       :package-metadata
                       :value-and-type-representation-record
                       :capability-manifest
                       :package-dependency-manifest
                       :async-effect-boundary-map
                       :nullish-and-exception-translation-map
                       :numeric-representation-manifest
                       :dynamic-code-and-prototype-policy
                       :ui-component-binding-metadata
                       :source-debug-map])
      (b6-document-fail! "B6-MANIFEST" source-path manifest
                         {:missing-fields [:js-ts-artifact-manifest]}))
    (when-not (= (set b6-document-diagnostic-ids) diagnostics)
      (b6-document-fail! "B6-MANIFEST" source-path
                         (:b6-diagnostic-stream artifact)
                         {:missing-fields [:b6-diagnostics]})))
  :complete)

(defn b6-document-capability-proof
  [artifact]
  (let [manifest (:js-ts-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b6-diagnostic-stream
                                       :diagnostics])))]
    {:backend-interface-input-verified?
     (= :complete (get-in artifact
                          [:hosted-lowering-artifact
                           :capability-based-proof :status]))
     :runtime-module-target-pinned?
     (= :pinned (get-in manifest
                        [:runtime-and-module-target-record :status]))
     :javascript-module-emitted?
     (= :complete (get-in manifest
                          [:javascript-module-artifacts 0 :status]))
     :typescript-declarations-emitted?
     (= :complete (get-in manifest
                          [:typescript-declaration-files 0 :status]))
     :source-map-generated-origin-preserved?
     (= :complete
        (get-in manifest
                [:source-maps-and-generated-origin-maps 0 :status]))
     :host-global-capabilities-declared?
     (= :declared (get-in manifest [:capability-manifest :status]))
     :package-import-policy-covered?
     (= :complete (get-in manifest
                          [:package-dependency-manifest :status]))
     :nullish-exception-translation-covered?
     (= :checked
        (get-in manifest
                [:nullish-and-exception-translation-map :status]))
     :numeric-representation-covered?
     (= :complete
        (get-in manifest
                [:numeric-representation-manifest :status]))
     :async-effect-boundaries-covered?
     (= :complete (get-in manifest
                          [:async-effect-boundary-map :status]))
     :dynamic-code-and-prototype-rejected?
     (= :complete
        (get-in manifest
                [:dynamic-code-and-prototype-policy :status]))
     :ui-metadata-recorded?
     (= :not-applicable
        (get-in manifest
                [:ui-component-binding-metadata :status]))
     :source-debug-map-preserved?
     (= :preserved (get-in manifest [:source-debug-map :status]))
     :js-source-structurally-valid?
     (b6-document-js-structurally-valid? b6-document-js-source)
     :ts-declarations-structurally-valid?
     (b6-document-ts-structurally-valid? b6-document-ts-declarations)
     :source-map-structurally-valid?
     (b6-document-source-map-structurally-valid? b6-document-source-map)
     :package-json-structurally-valid?
     (b6-document-package-structurally-valid? b6-document-package-json)
     :manifest-complete?
     (every? #(contains? manifest %)
             [:runtime-and-module-target-record
              :javascript-module-artifacts
              :typescript-declaration-files
              :source-maps-and-generated-origin-maps
              :package-metadata
              :value-and-type-representation-record
              :capability-manifest
              :package-dependency-manifest
              :async-effect-boundary-map
              :nullish-and-exception-translation-map
              :numeric-representation-manifest
              :dynamic-code-and-prototype-policy
              :ui-component-binding-metadata
              :source-debug-map])
     :diagnostics-covered?
     (= (set b6-document-diagnostic-ids) diagnostics)
     :requires-external-node-proof?
     (= :requires-proof-command
        (get-in manifest [:node-syntax-and-runtime-record :status]))
     :requires-external-tsc-proof?
     (= :requires-proof-command
        (get-in manifest
                [:typescript-declaration-check-record :status]))
     :status :complete}))