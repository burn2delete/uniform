

(defn managed-runtime-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/managed-runtime-diagnostic-stream
   :stage :managed-runtime
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :managed-runtime
            :message-key (keyword "runtime" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "p08-managed-runtime-syntax-" index)
                      :artifact input-id}
            :profile (if (= "R4-PROFILE" id) :firmware :hosted)
            :target (case id
                      "R4-HOST" :jvm
                      "R4-NULL" :javascript
                      "R4-EXCEPTION" :wasm-host
                      :managed-host)
            :runtime-family :managed
            :host-runtime (case id
                            "R4-HOST" :unsupported-host
                            "R4-NULL" :javascript
                            "R4-EXCEPTION" :jvm
                            "R4-REFLECTION" :jvm
                            "R4-COLLECTION" :host-collection
                            "R4-RESOURCE" :managed-gc
                            "R4-SOURCEMAP" :generated-host-code
                            "R4-PROFILE" :hosted-service
                            :managed)
            :host-symbol (case id
                           "R4-REFLECTION" 'java.lang.Class/forName
                           "R4-NULL" 'undefined
                           "R4-EXCEPTION" 'Promise.reject
                           nil)
            :host-package (case id
                            "R4-HOST" "undeclared.host"
                            "R4-COLLECTION" "host.collections"
                            "R4-RESOURCE" "host.resources"
                            nil)
            :gravity-type (case id
                            "R4-NULL" 'Option
                            "R4-EXCEPTION" 'Result
                            "R4-COLLECTION" 'PersistentVector
                            "R4-RESOURCE" 'LinearResource
                            'ManagedHostValue)
            :effect (case id
                      "R4-EXCEPTION" :host/error
                      "R4-REFLECTION" :host/reflect
                      "R4-RESOURCE" :resource/close
                      nil)
            :capability (case id
                          "R4-REFLECTION" :host/reflection
                          "R4-RESOURCE" :resource/cleanup
                          nil)
            :adapter (case id
                       "R4-NULL" :null-option-adapter
                       "R4-EXCEPTION" :exception-result-adapter
                       "R4-REFLECTION" :reflection-capability-adapter
                       "R4-COLLECTION" :persistent-collection-adapter
                       "R4-RESOURCE" :linear-resource-cleanup-adapter
                       :managed-host-adapter)
            :missing-policy (managed-runtime-missing-policy id)
            :source-generated-origin-chain
            [:profile-validation :runtime-selection :managed-runtime]
            :facts {:hosted-convenience-not-portable-core true
                    :host-null-exceptions-translated true
                    :runtime-checks-do-not-grant-authority true
                    :host-failures-map-to-gravity-source true}
            :remediation [{:kind :declare-host-runtime-contract}
                          {:kind :add-typed-host-adapter}
                          {:kind :gate-dynamic-host-use-with-capability}
                          {:kind :reject-hosted-profile-leakage}]
            :redactions []
            :ordering-key [id :managed-runtime]})
         managed-runtime-diagnostic-ids
         (range))
   :status :complete})

(defn managed-runtime-manifest
  [module input-id]
  {:artifact :gravity/managed-runtime
   :input-artifact input-id
   :family :managed
   :profile (or (:profile module) :hosted)
   :target (or (:target module) :jvm)
   :host {:kind :multi-host
          :version "stage0-declared-range"
          :supported #{:jvm :javascript :wasm-host}
          :module-system {:jvm :classpath-module
                          :javascript :esm
                          :wasm-host :component-model}
          :package-system {:jvm :maven-coordinate
                           :javascript :npm-package
                           :wasm-host :component-package}}
   :services {:delegated #{:gc :host-exceptions :collections
                           :event-loop :host-packages}
              :linked #{:gravity-adapters :capability-checks
                        :source-map-runtime :linear-cleanup}
              :generated #{:null-wrapper :exception-wrapper
                           :host-source-map :adapter-table}
              :forbidden #{:ambient-reflection :unchecked-null
                           :unchecked-exception :gc-only-linear-cleanup
                           :hosted-leakage}}
   :interop {:nulls :option-result-or-opaque
             :undefined :option
             :exceptions :gravity-error-panic-or-effect
             :rejected-promises :result-or-effect
             :foreign-values :opaque-typed-wrapper}
   :reflection :capability-gated
   :dynamic-loading :capability-gated
   :eval :capability-gated
   :collection-semantics :gravity-compatible
   :source-maps :host-to-gravity
   :status :complete})

(defn host-runtime-target-records
  [input-id]
  [{:artifact :gravity/host-runtime-target-record
    :input-artifact input-id
    :kind :jvm
    :version ">=17"
    :module-system :classpath-or-module-path
    :package-system :maven-coordinate
    :host-exception-model :throwable
    :host-null-model :nullable-reference
    :adapter :jvm-managed-adapter
    :status :complete}
   {:artifact :gravity/host-runtime-target-record
    :input-artifact input-id
    :kind :javascript
    :version "es2022+"
    :module-system :esm
    :package-system :npm-package
    :host-exception-model :throw-or-rejected-promise
    :host-null-model :null-or-undefined
    :adapter :js-managed-adapter
    :status :complete}
   {:artifact :gravity/host-runtime-target-record
    :input-artifact input-id
    :kind :wasm-host
    :version "component-model-stage0"
    :module-system :component-import-export
    :package-system :component-package
    :host-exception-model :host-trap-or-result
    :host-null-model :nullable-reference-or-option
    :adapter :wasm-host-managed-adapter
    :status :complete}])

(defn managed-collection-implementation-manifest
  [input-id]
  {:artifact :gravity/managed-collection-implementation-manifest
   :input-artifact input-id
   :implementations [{:gravity-type 'Vector
                      :host-representation :persistent-vector
                      :equality :gravity-value-equality
                      :hashing :gravity-stable-hash
                      :mutation :persistent
                      :identity :not-observable}
                     {:gravity-type 'Map
                      :host-representation :persistent-map
                      :equality :gravity-key-value-equality
                      :hashing :gravity-stable-hash
                      :mutation :persistent
                      :identity :not-observable}
                     {:gravity-type 'HostArray
                      :host-representation :opaque-foreign-value
                      :equality :adapter-defined
                      :hashing :adapter-defined
                      :mutation :capability-gated
                      :identity :opaque}]
   :semantics-tested #{:equality :hashing :serialization
                       :mutability :identity :taint}
   :divergences []
   :status :complete})

(defn dynamic-namespace-runtime-record
  [input-id]
  {:artifact :gravity/dynamic-variable-and-namespace-runtime-record
   :input-artifact input-id
   :dynamic-vars :manifest-declared
   :thread-local-values :host-adapter-declared
   :async-context :propagated-through-adapter
   :repl-state :r9-selected-only
   :namespace-loading :capability-gated
   :hot-reload :profile-gated
   :implicit-capabilities? false
   :status :complete})

(defn exception-null-translation-map
  [input-id]
  {:artifact :gravity/exception-null-translation-map
   :input-artifact input-id
   :nulls [{:host :jvm :source :nullable-reference
            :gravity :Option :unchecked? false}
           {:host :javascript :source :undefined
            :gravity :Option :unchecked? false}
           {:host :wasm-host :source :nullable-reference
            :gravity :Result :unchecked? false}
           {:host :foreign :source :opaque-host-value
            :gravity :OpaqueForeign :unchecked? false}]
   :exceptions [{:host :jvm :source :throwable
                 :gravity-channel :error/effect
                 :effect :host/error}
                {:host :javascript :source :rejected-promise
                 :gravity-channel :Result
                 :effect :host/error}
                {:host :wasm-host :source :trap
                 :gravity-channel :panic-or-result
                 :effect :host/error}]
   :unchecked-null-or-exception? false
   :status :complete})

(defn reflection-dynamic-use-policy
  [input-id]
  {:artifact :gravity/reflection-and-dynamic-use-policy
   :input-artifact input-id
   :reflection :capability-gated
   :class-loading :capability-gated
   :method-handles :capability-gated
   :dynamic-imports :capability-gated
   :eval :capability-gated
   :package-globals :capability-gated
   :required-effects #{:host/reflect :host/dynamic-load :host/eval}
   :required-capabilities #{:host/reflection :host/dynamic-load
                            :host/eval}
   :ambient-use-allowed? false
   :status :complete})