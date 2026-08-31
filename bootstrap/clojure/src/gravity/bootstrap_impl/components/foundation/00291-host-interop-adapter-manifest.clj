

(defn host-interop-adapter-manifest
  [input-id]
  {:artifact :gravity/host-interop-adapter-manifest
   :input-artifact input-id
   :adapters [{:adapter :jvm-managed-adapter
               :host-runtime :jvm
               :types #{:nullable-reference :throwable :class}
               :effects #{:host/error}
               :capabilities #{}
               :taint :preserved
               :error-mapping :throwable-to-gravity-error
               :source-map :jvm-bytecode-to-gravity}
              {:adapter :js-managed-adapter
               :host-runtime :javascript
               :types #{:null :undefined :promise :object}
               :effects #{:host/error :async/await}
               :capabilities #{}
               :taint :preserved
               :error-mapping :throw-or-rejected-promise-to-result
               :source-map :source-map-v3-to-gravity}
              {:adapter :wasm-host-managed-adapter
               :host-runtime :wasm-host
               :types #{:component-import :component-export :trap}
               :effects #{:host/error}
               :capabilities #{}
               :taint :preserved
               :error-mapping :trap-or-result-to-gravity
               :source-map :wasm-debug-to-gravity}
              {:adapter :reflection-capability-adapter
               :host-runtime :jvm
               :types #{:class :method-handle}
               :effects #{:host/reflect}
               :capabilities #{:host/reflection}
               :taint :preserved
               :error-mapping :reflective-error-to-result
               :source-map :reflection-site-to-gravity}]
   :all-adapters-typed? true
   :status :complete})

(defn managed-resource-cleanup-manifest
  [input-id]
  {:artifact :gravity/managed-resource-cleanup-manifest
   :input-artifact input-id
   :resources [{:resource-id :managed-file
                :host-runtime :jvm
                :gravity-type 'LinearResource
                :cleanup :deterministic-close
                :fallback :debug-finalizer-warning
                :terminal-states #{:closed}
                :gc-only? false}
               {:resource-id :managed-transaction
                :host-runtime :javascript
                :gravity-type 'LinearResource
                :cleanup :deterministic-commit-or-rollback
                :fallback :debug-leak-diagnostic
                :terminal-states #{:committed :rolled-back}
                :gc-only? false}
               {:resource-id :wasm-host-handle
                :host-runtime :wasm-host
                :gravity-type 'LinearResource
                :cleanup :explicit-resource-drop
                :fallback :debug-leak-diagnostic
                :terminal-states #{:dropped}
                :gc-only? false}]
   :linear-resources-deterministic? true
   :gc-finalization-cleanup-policy? false
   :status :complete})

(defn managed-source-debug-map
  [source-path input-id]
  {:artifact :gravity/managed-source-debug-map
   :input-artifact input-id
   :entries [{:host-runtime :jvm
              :host-frame "gravity.runtime.Managed/main"
              :gravity-source source-path
              :source-span (source-span source-path 0)
              :generated-origin-chain [:managed-runtime :jvm-lowering]}
             {:host-runtime :javascript
              :host-frame "managedRuntime.main"
              :gravity-source source-path
              :source-span (source-span source-path 1)
              :generated-origin-chain [:managed-runtime :js-lowering]}
             {:host-runtime :wasm-host
              :host-frame "managed-runtime::main"
              :gravity-source source-path
              :source-span (source-span source-path 2)
              :generated-origin-chain [:managed-runtime :wasm-host-lowering]}]
   :host-failures-map-to-gravity? true
   :generated-origin-chain-preserved? true
   :status :complete})

(defn managed-runtime-validate!
  [source-path artifact]
  (let [upstream (:minimal-native-memory-artifact artifact)
        manifest (:managed-runtime-manifest artifact)
        targets (:host-runtime-target-records artifact)
        collections (:collection-implementation-manifest artifact)
        dynamic-state (:dynamic-variable-and-namespace-runtime-record artifact)
        translation (:exception-null-translation-map artifact)
        reflection (:reflection-and-dynamic-use-policy artifact)
        interop (:host-interop-adapter-manifest artifact)
        cleanup (:resource-cleanup-manifest artifact)
        source-map (:managed-source-debug-map artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:managed-runtime-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :gravity/stage0-minimal-native-memory-runtime-artifact
                 (:kind upstream))
      (managed-runtime-fail! "R4-MANIFEST" source-path upstream
                             {:missing-fields [:minimal-native-memory-artifact]}))
    (when-not (= :complete (get-in upstream
                                   [:capability-based-proof :status]))
      (managed-runtime-fail! "R4-MANIFEST" source-path upstream
                             {:missing-fields [:upstream-proof]}))
    (when-not (= :complete (:status manifest))
      (managed-runtime-fail! "R4-MANIFEST" source-path manifest
                             {:missing-fields [:managed-runtime-manifest]}))
    (when-not (every? (set (map :kind targets))
                      [:jvm :javascript :wasm-host])
      (managed-runtime-fail! "R4-HOST" source-path manifest
                             {:missing-fields [:host-runtime-targets]}))
    (when-not (every? #(= :complete (:status %)) targets)
      (managed-runtime-fail! "R4-HOST" source-path targets
                             {:missing-fields [:target-status]}))
    (when (true? (:unchecked-null-or-exception? translation))
      (managed-runtime-fail! "R4-NULL" source-path translation
                             {:missing-fields [:checked-null-translation]}))
    (when-not (every? :gravity-channel (:exceptions translation))
      (managed-runtime-fail! "R4-EXCEPTION" source-path translation
                             {:missing-fields [:exception-translation]}))
    (when-not (and (= :capability-gated (:reflection reflection))
                   (false? (:ambient-use-allowed? reflection)))
      (managed-runtime-fail! "R4-REFLECTION" source-path reflection
                             {:missing-fields [:capability-gated-reflection]}))
    (when-not (= :complete (:status collections))
      (managed-runtime-fail! "R4-COLLECTION" source-path collections
                             {:missing-fields [:collection-manifest]}))
    (when (seq (:divergences collections))
      (managed-runtime-fail! "R4-COLLECTION" source-path collections
                             {:missing-fields [:collection-semantics]}))
    (when-not (and (true? (:linear-resources-deterministic? cleanup))
                   (false? (:gc-finalization-cleanup-policy? cleanup)))
      (managed-runtime-fail! "R4-RESOURCE" source-path cleanup
                             {:missing-fields [:deterministic-cleanup]}))
    (when-not (and (true? (:host-failures-map-to-gravity? source-map))
                   (true? (:generated-origin-chain-preserved? source-map)))
      (managed-runtime-fail! "R4-SOURCEMAP" source-path source-map
                             {:missing-fields [:host-source-map]}))
    (when-not (false? (:implicit-capabilities? dynamic-state))
      (managed-runtime-fail! "R4-PROFILE" source-path dynamic-state
                             {:missing-fields [:hosted-profile-facade]}))
    (when-not (every? (fn [adapter]
                        (and (:types adapter)
                             (:effects adapter)
                             (:capabilities adapter)
                             (:taint adapter)
                             (:error-mapping adapter)
                             (:source-map adapter)))
                      (:adapters interop))
      (managed-runtime-fail! "R4-MANIFEST" source-path interop
                             {:missing-fields [:typed-adapter-fields]}))
    (when-not (= (set managed-runtime-diagnostic-ids) diagnostics)
      (managed-runtime-fail! "R4-MANIFEST" source-path
                             (:managed-runtime-diagnostic-stream artifact)
                             {:missing-fields [:diagnostics]})))
  :complete)

(defn managed-runtime-capability-proof
  [artifact]
  (let [manifest (:managed-runtime-manifest artifact)
        targets (:host-runtime-target-records artifact)
        collections (:collection-implementation-manifest artifact)
        dynamic-state (:dynamic-variable-and-namespace-runtime-record artifact)
        translation (:exception-null-translation-map artifact)
        reflection (:reflection-and-dynamic-use-policy artifact)
        interop (:host-interop-adapter-manifest artifact)
        cleanup (:resource-cleanup-manifest artifact)
        source-map (:managed-source-debug-map artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:managed-runtime-diagnostic-stream
                                       :diagnostics])))]
    {:minimal-native-memory-input-verified?
     (= :complete (get-in artifact
                          [:minimal-native-memory-artifact
                           :capability-based-proof :status]))
     :host-runtime-targets-declared?
     (every? (set (map :kind targets)) [:jvm :javascript :wasm-host])
     :manifest-declares-host-services?
     (and (= :managed (:family manifest))
          (contains? (get-in manifest [:services :delegated]) :gc)
          (contains? (get-in manifest [:services :forbidden])
                     :ambient-reflection))
     :null-and-exception-translation-checked?
     (and (false? (:unchecked-null-or-exception? translation))
          (every? :gravity-channel (:exceptions translation)))
     :reflection-and-dynamic-use-capability-gated?
     (and (= :capability-gated (:reflection reflection))
          (false? (:ambient-use-allowed? reflection)))
     :collection-semantics-gravity-compatible?
     (and (= :complete (:status collections))
          (empty? (:divergences collections)))
     :linear-resources-deterministic?
     (and (true? (:linear-resources-deterministic? cleanup))
          (false? (:gc-finalization-cleanup-policy? cleanup)))
     :hosted_state_does_not_grant_capabilities?
     (false? (:implicit-capabilities? dynamic-state))
     :host-adapters-typed-and-mapped?
     (and (true? (:all-adapters-typed? interop))
          (every? :source-map (:adapters interop)))
     :source-debug-map-complete?
     (and (true? (:host-failures-map-to-gravity? source-map))
          (true? (:generated-origin-chain-preserved? source-map)))
     :diagnostics-covered?
     (= (set managed-runtime-diagnostic-ids) diagnostics)
     :status :complete}))