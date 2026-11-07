

(def stage0-compiled-managed-runtime-required-fields
  [:family :host-runtime :host-version :module-system :package-system
   :services :interop :collection-semantics :dynamic-state
   :exception-policy :nullability-policy :reflection-policy
   :interop-policy :source-debug-map :capability-enforcement
   :observability])

(defn stage0-compiled-runtime-suite [module]
  (gravity.runtime/native-stage0-runtime-suite module))

(defn stage0-compiled-runtime-suite-present? [module]
  (gravity.runtime/native-stage0-runtime-suite-present? module))

(defn validate-stage0-compiled-runtime-selection! [module selection]
  (gravity.runtime/validate-native-stage0-runtime-selection!
    {:fail! #'runtime-selection-fail!,
     :intersection #'set/intersection}
    module
    selection))

(defn validate-stage0-compiled-managed-runtime! [module
                                                 managed-runtime]
  (gravity.runtime/validate-native-stage0-managed-runtime!
    {:fail! #'managed-runtime-fail!,
     :missing-fields #'compiler-pass-missing-fields,
     :required-fields
     #'stage0-compiled-managed-runtime-required-fields}
    module
    managed-runtime))

(defn validate-stage0-compiled-runtime-capability! [module decision]
  (gravity.runtime/validate-native-stage0-runtime-capability!
    #'ai-repl-ffi-capability-fail!
    module
    decision))

(defn validate-stage0-compiled-runtime-observability! [module sink]
  (gravity.runtime/validate-native-stage0-runtime-observability!
    #'runtime-observability-fail!
    module
    sink))

(defn validate-stage0-compiled-runtime! [module]
  (gravity.runtime/validate-native-stage0-runtime!
    {:suite-present? #'stage0-compiled-runtime-suite-present?,
     :suite #'stage0-compiled-runtime-suite,
     :selection! #'validate-stage0-compiled-runtime-selection!,
     :managed! #'validate-stage0-compiled-managed-runtime!,
     :capability! #'validate-stage0-compiled-runtime-capability!,
     :observability! #'validate-stage0-compiled-runtime-observability!}
    module))

(def stage0-compiled-domain-slice-required-fields
  [:document-id :domain :slice-id :profile :target :effects :capabilities
   :artifacts :accepted-fixtures :rejected-fixtures :replacement-scope
   :conformance])

(defn stage0-compiled-domain-suite
  [module]
  (get-in module [:metadata :domain :compiled-gate] {}))

(defn stage0-compiled-domain-suite-present?
  [module]
  (contains? (get-in module [:metadata :domain] {}) :compiled-gate))