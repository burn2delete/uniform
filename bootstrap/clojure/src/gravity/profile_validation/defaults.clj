(ns gravity.profile-validation.defaults)

(def standard-profile-order
  [:core :meta :hosted :native :firmware :kernel :hardware :distributed :ai
   :gpu :formal])

(def profile-diagnostic-ids
  ["P1-MISSING-PROFILE" "P1-AMBIGUOUS-PROFILE" "P1-EFFECT"
   "P1-CAPABILITY" "P1-MEMORY" "P1-RUNTIME" "P1-CROSS-IMPORT"
   "P1-MACRO" "P1-FACET" "P1-BACKEND"])

(def profile-memory-regimes
  {:core {:managed false :ownership true :regions false
          :hidden-allocation :forbidden :raw-memory :forbidden}
   :meta {:managed true :ownership false :regions false
          :hidden-allocation :declared :raw-memory :forbidden}
   :hosted {:managed true :ownership false :regions false
            :hidden-allocation :declared :raw-memory :unsafe-only}
   :native {:managed false :ownership true :regions true
            :hidden-allocation :declared :raw-memory :unsafe-only}
   :firmware {:managed false :ownership true :regions true
              :hidden-allocation :forbidden :raw-memory :unsafe-only}
   :kernel {:managed false :ownership true :regions true
            :hidden-allocation :forbidden :raw-memory :unsafe-only}
   :hardware {:managed false :ownership true :regions true
              :hidden-allocation :forbidden :raw-memory :unsafe-only}
   :distributed {:managed true :ownership false :regions false
                 :hidden-allocation :declared :raw-memory :forbidden}
   :ai {:managed true :ownership false :regions false
        :hidden-allocation :declared :raw-memory :forbidden}
   :gpu {:managed false :ownership true :regions true
         :hidden-allocation :forbidden :raw-memory :unsafe-only}
   :formal {:managed false :ownership true :regions true
            :hidden-allocation :forbidden :raw-memory :proof-only}})

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

(def scalar-operations
  {:standard-profile-order standard-profile-order
   :profile-diagnostic-ids profile-diagnostic-ids
   :profile-memory-regimes profile-memory-regimes
   :profile-runtime-assumptions profile-runtime-assumptions
   :profile-unsafe-policies profile-unsafe-policies
   :profile-artifact-boundaries profile-artifact-boundaries})
