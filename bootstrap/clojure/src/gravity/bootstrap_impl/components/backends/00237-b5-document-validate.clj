

(defn b5-document-validate!
  [source-path artifact]
  (let [hosted (:hosted-lowering-artifact artifact)
        manifest (:jvm-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b5-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :gravity/stage0-hosted-lowering-artifact (:kind hosted))
      (b5-document-fail! "B5-MANIFEST" source-path hosted
                         {:missing-fields [:hosted-lowering-artifact]}))
    (when-not (= :complete (get-in hosted
                                   [:capability-based-proof :status]))
      (b5-document-fail! "B5-MANIFEST" source-path hosted
                         {:missing-fields [:hosted-lowering-proof]}))
    (when-not (= :pinned (get-in manifest
                                 [:classfile-and-jvm-target-record
                                  :status]))
      (b5-document-fail! "B5-TARGET" source-path manifest
                         {:missing-fields [:target-record]}))
    (when-not (= :complete (get-in manifest
                                   [:class-and-module-model :status]))
      (b5-document-fail! "B5-MANIFEST" source-path manifest
                         {:missing-fields [:class-module-model]}))
    (when-not (= :complete (get-in manifest
                                   [:value-representation-record :status]))
      (b5-document-fail! "B5-INTEROP" source-path manifest
                         {:missing-fields [:value-representation]}))
    (when-not (= :complete (get-in manifest
                                   [:java-interop-descriptor :status]))
      (b5-document-fail! "B5-INTEROP" source-path manifest
                         {:missing-fields [:interop-descriptor]}))
    (when-not (= :complete
                 (get-in manifest
                         [:nullability-and-exception-translation-map
                          :status]))
      (b5-document-fail! "B5-NULL" source-path manifest
                         {:missing-fields [:null-exception-map]}))
    (when-not (= :declared (get-in manifest
                                   [:reflection-and-dynamic-use-manifest
                                    :status]))
      (b5-document-fail! "B5-REFLECTION" source-path manifest
                         {:missing-fields [:reflection-manifest]}))
    (when-not (= :complete (get-in manifest
                                   [:classloading-policy-record :status]))
      (b5-document-fail! "B5-CLASSLOADING" source-path manifest
                         {:missing-fields [:classloading-policy]}))
    (when-not (= :complete (get-in manifest
                                   [:resource-cleanup-record :status]))
      (b5-document-fail! "B5-RESOURCE" source-path manifest
                         {:missing-fields [:resource-cleanup]}))
    (when-not (= :complete
                 (get-in manifest
                         [:thread-monitor-executor-atomic-effect-record
                          :status]))
      (b5-document-fail! "B5-THREAD" source-path manifest
                         {:missing-fields [:thread-effect-record]}))
    (when-not (= :consistent (get-in manifest
                                     [:native-image-configuration :status]))
      (b5-document-fail! "B5-NATIVE-IMAGE" source-path manifest
                         {:missing-fields [:native-image-config]}))
    (when-not (= :complete (get-in manifest
                                   [:profile-boundary-record :status]))
      (b5-document-fail! "B5-PROFILE" source-path manifest
                         {:missing-fields [:profile-boundary]}))
    (when-not (b5-document-java-structurally-valid?
               b5-document-java-source)
      (b5-document-fail! "B5-MANIFEST" source-path manifest
                         {:missing-fields [:java-source-structure]}))
    (when-not (every? #(contains? manifest %)
                      [:classfile-and-jvm-target-record
                       :class-and-module-model
                       :java-source-files
                       :module-descriptors
                       :class-files
                       :jar-or-module-artifact
                       :java-interop-descriptor
                       :nullability-and-exception-translation-map
                       :reflection-and-dynamic-use-manifest
                       :native-image-configuration
                       :runtime-helper-manifest
                       :source-debug-map])
      (b5-document-fail! "B5-MANIFEST" source-path manifest
                         {:missing-fields [:jvm-artifact-manifest]}))
    (when-not (= (set b5-document-diagnostic-ids) diagnostics)
      (b5-document-fail! "B5-MANIFEST" source-path
                         (:b5-diagnostic-stream artifact)
                         {:missing-fields [:b5-diagnostics]})))
  :complete)

(defn b5-document-capability-proof
  [artifact]
  (let [manifest (:jvm-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b5-diagnostic-stream
                                       :diagnostics])))]
    {:backend-interface-input-verified?
     (= :complete (get-in artifact
                          [:hosted-lowering-artifact
                           :capability-based-proof :status]))
     :classfile-jvm-target-pinned?
     (= :pinned (get-in manifest
                        [:classfile-and-jvm-target-record :status]))
     :class-module-model-covered?
     (= :complete (get-in manifest [:class-and-module-model :status]))
     :value-representation-covered?
     (= :complete (get-in manifest
                          [:value-representation-record :status]))
     :interop-descriptor-covered?
     (= :complete (get-in manifest [:java-interop-descriptor :status]))
     :nullability-and-exception-covered?
     (= :complete
        (get-in manifest
                [:nullability-and-exception-translation-map :status]))
     :reflection-dynamic-use-declared?
     (= :declared
        (get-in manifest
                [:reflection-and-dynamic-use-manifest :status]))
     :classloading-policy-covered?
     (= :complete (get-in manifest
                          [:classloading-policy-record :status]))
     :resource-cleanup-deterministic?
     (= :complete (get-in manifest
                          [:resource-cleanup-record :status]))
     :thread-monitor-executor-atomic-covered?
     (= :complete
        (get-in manifest
                [:thread-monitor-executor-atomic-effect-record :status]))
     :native-image-config-consistent?
     (= :consistent (get-in manifest
                            [:native-image-configuration :status]))
     :profile-boundary-rejection-covered?
     (= :complete (get-in manifest
                          [:profile-boundary-record :status]))
     :source-debug-map-preserved?
     (= :preserved (get-in manifest [:source-debug-map :status]))
     :java-source-structurally-valid?
     (b5-document-java-structurally-valid? b5-document-java-source)
     :manifest-complete?
     (every? #(contains? manifest %)
             [:classfile-and-jvm-target-record
              :class-and-module-model
              :java-source-files
              :module-descriptors
              :class-files
              :jar-or-module-artifact
              :java-interop-descriptor
              :nullability-and-exception-translation-map
              :reflection-and-dynamic-use-manifest
              :native-image-configuration
              :runtime-helper-manifest
              :source-debug-map])
     :diagnostics-covered?
     (= (set b5-document-diagnostic-ids) diagnostics)
     :requires-external-javac-proof?
     (= :requires-proof-command
        (get-in manifest [:javac-compilation-record :status]))
     :requires-external-jar-proof?
     (= :requires-proof-command
        (get-in manifest [:jar-creation-record :status]))
     :status :complete}))