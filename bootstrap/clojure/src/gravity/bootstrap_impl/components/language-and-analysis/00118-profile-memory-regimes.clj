

(def profile-memory-regimes
  {:core {:managed false
          :ownership true
          :regions false
          :hidden-allocation :forbidden
          :raw-memory :forbidden}
   :meta {:managed true
          :ownership false
          :regions false
          :hidden-allocation :declared
          :raw-memory :forbidden}
   :hosted {:managed true
            :ownership false
            :regions false
            :hidden-allocation :declared
            :raw-memory :unsafe-only}
   :native {:managed false
            :ownership true
            :regions true
            :hidden-allocation :declared
            :raw-memory :unsafe-only}
   :firmware {:managed false
              :ownership true
              :regions true
              :hidden-allocation :forbidden
              :raw-memory :unsafe-only}
   :kernel {:managed false
            :ownership true
            :regions true
            :hidden-allocation :forbidden
            :raw-memory :unsafe-only}
   :hardware {:managed false
              :ownership true
              :regions true
              :hidden-allocation :forbidden
              :raw-memory :unsafe-only}
   :distributed {:managed true
                 :ownership false
                 :regions false
                 :hidden-allocation :declared
                 :raw-memory :forbidden}
   :ai {:managed true
        :ownership false
        :regions false
        :hidden-allocation :declared
        :raw-memory :forbidden}
   :gpu {:managed false
         :ownership true
         :regions true
         :hidden-allocation :forbidden
         :raw-memory :unsafe-only}
   :formal {:managed false
            :ownership true
            :regions true
            :hidden-allocation :forbidden
            :raw-memory :proof-only}})

(def profile-runtime-assumptions
  {:core {:required false :providers #{}}
   :meta {:required true :providers #{:compiler :macro-engine}}
   :hosted {:required true :providers #{:host :stdio :allocator :scheduler}}
   :native {:required false :providers #{:allocator :threading}}
   :firmware {:required false :providers #{:interrupts :device-map}}
   :kernel {:required false :providers #{:scheduler :interrupts :device-map}}
   :hardware {:required false :providers #{:clock :device-map}}
   :distributed {:required true :providers #{:workflow :replay :scheduler}}
   :ai {:required true :providers #{:model :tool :memory :human-review}}
   :gpu {:required false :providers #{:device :kernel-launch}}
   :formal {:required false :providers #{:solver :certificate-checker}}})

(def profile-unsafe-policies
  {:core :forbidden
   :meta :trusted-compiler-only
   :hosted :audited
   :native :reviewed
   :firmware :systems-audited
   :kernel :systems-audited
   :hardware :systems-audited
   :distributed :forbidden
   :ai :generated-code-audited
   :gpu :systems-audited
   :formal :proof-required})

(def profile-artifact-boundaries
  {:core #{:schema :pure-core}
   :meta #{:syntax-object :compiler-artifact}
   :hosted #{:schema :ffi :host-object :package}
   :native #{:ffi :schema :native-object}
   :firmware #{:schema :device-map :binary-image}
   :kernel #{:schema :syscall :device-map}
   :hardware #{:schema :hdl :device-map}
   :distributed #{:schema :workflow-graph :replay-log}
   :ai #{:schema :tool-manifest :model-manifest :replay-log}
   :gpu #{:schema :gpu-kernel :device-buffer}
   :formal #{:schema :proof-certificate :solver-artifact}})

(declare profile-allowed-effects
         profile-capabilities
         profile-contract
         profile-policy-layer
         profile-effective-effects
         effect-permission-table
         profile-validation-facts)

(defn- profile-validation-effect-registry
  "Return the bootstrap effect registry in the leaf's portable shape.

  Build effects intentionally omit :profiles in the legacy registry because
  their profile legality comes from :requires-build-grant.  The accepted leaf
  validates registry rows structurally, so an absent profile set is projected
  as an empty set without changing the legacy build-grant rule."
  []
  (reduce-kv (fn [registry effect entry]
               (assoc registry effect
                      (if (contains? entry :profiles)
                        entry
                        (assoc entry :profiles #{}))))
             {}
             effect-registry))

(def ^:private ^:dynamic *profile-validation-leaf-call?* false)

(defn- profile-validation-ops
  []
  {:stable-set stable-set
   :stable-vec stable-vec
   :diagnostic-record
   (fn [id facts]
     (compatibility-diagnostic-record
      :gravity/profile-diagnostic :profile-validation id facts))
   ;; Preserve the two distinct legacy registry seams from HEAD 4921fbc.
   ;; profile-contract consults all-registered-effects only for its forbidden
   ;; complement; effect-registry-entry belongs only to permission-table row
   ;; metadata.  The leaf may consume these functions, but registry ownership
   ;; remains in bootstrap (including with-redefs).
   :all-registered-effects all-registered-effects
   :effect-registry-entry effect-registry-entry
   :profile-allowed-effects profile-allowed-effects
   :profile-capabilities profile-capabilities
   :profile-contract profile-contract
   ;; Grant discovery remains a bootstrap-owned seam.  The leaf only
   ;; consumes this function while projecting policy intersections.
   :profile-policy-layer profile-policy-layer
   :profile-effective-effects profile-effective-effects
   :effect-permission-table effect-permission-table
   :profile-validation-facts profile-validation-facts
   :standard-profile-order standard-profile-order
   :profile-diagnostic-ids p1-diagnostic-ids
   :profile-memory-regimes profile-memory-regimes
   :profile-runtime-assumptions profile-runtime-assumptions
   :profile-unsafe-policies profile-unsafe-policies
   :profile-artifact-boundaries profile-artifact-boundaries
   :effect-registry (profile-validation-effect-registry)
   :provider-specs provider-specs
   :core-forms core-forms
   :supported-targets (set/union supported-targets
                                 *additional-bootstrap-targets*)})

(defn- profile-validation-call
  [operation-key operation & args]
  (if *profile-validation-leaf-call?*
    (profile-validation/call-entrypoint-body operation-key operation args)
    (binding [*profile-validation-leaf-call?* true]
      (profile-validation/with-operations
       (profile-validation-ops)
       #(profile-validation/call-entrypoint-body
         operation-key operation args)))))

(defn profile-allowed-effects
  [profile]
  (profile-validation-call
   :profile-allowed-effects profile-validation/profile-allowed-effects profile))

(defn profile-capabilities
  [profile]
  (profile-validation-call
   :profile-capabilities profile-validation/profile-capabilities profile))

(defn profile-contract
  [profile]
  (profile-validation-call
   :profile-contract profile-validation/profile-contract profile))

(defn profile-policy-layer
  [module metadata-key source-key default-value]
  (or (get-in module [:metadata metadata-key])
      (source-key module)
      default-value))

(defn profile-effective-effects
  [module inferred-effects]
  (profile-validation-call
   :profile-effective-effects profile-validation/profile-effective-effects
   module inferred-effects))

(defn profile-effective-capabilities
  [module required-capabilities]
  (let [source-capabilities (:capabilities module)
        profile-caps (profile-capabilities (:profile module))
        package-caps (profile-policy-layer module :package-capabilities :capabilities #{})
        provider-caps (profile-policy-layer module :provider-capability-grants :capabilities #{})
        deployment-caps (profile-policy-layer module :deployment-capabilities :capabilities #{})]
    {:source source-capabilities
     :required required-capabilities
     :profile profile-caps
     :package package-caps
     :provider provider-caps
     :deployment deployment-caps
     :effective (set/intersection source-capabilities profile-caps package-caps
                                  provider-caps deployment-caps)}))