(ns gravity.module-analysis
  "Hosted Stage0 L3 module-analysis compatibility projection.

  This leaf consumes already-read forms and externally supplied syntax and
  validation operations. It owns only the small namespace-analysis cluster
  used by the hosted bootstrap. It does not read source, authenticate reader
  products, execute macros, establish canonical L3 authority, or grant proof,
  self-hosting, attestation, or release authority."
  (:require [gravity.module-analysis.artifact :as artifact]
            [gravity.module-analysis.boundaries :as boundaries]
            [gravity.module-analysis.clauses :as clauses]
            [gravity.module-analysis.contract :as contract]
            [gravity.module-analysis.definitions :as definitions]
            [gravity.module-analysis.dependencies :as dependencies]
            [gravity.module-analysis.effects :as effects]
            [gravity.module-analysis.module :as module]
            [gravity.module-analysis.policy :as policy]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private ^:dynamic *bypass-next-operation-keys* #{})
(def ^:private function-operation-keys policy/function-operation-keys)
(def ^:private scalar-operation-keys policy/scalar-operation-keys)
(def ^:private operation-keys policy/operation-keys)
(def ^:private known-source-profiles-default policy/known-source-profiles-default)
(def ^:private supported-profiles-default policy/supported-profiles-default)
(def ^:private supported-targets-default policy/supported-targets-default)
(def ^:private effect-capability-default policy/effect-capability-default)
(def ^:private profile-direct-imports-default policy/profile-direct-imports-default)
(def ^:private namespace-contract (contract/namespace-contract operation-keys))
(def ^{:private true :arglists '([value])}
  valid-keyword-set? policy/valid-keyword-set?)
(def ^{:private true :arglists '([value])}
  valid-effect-capability? policy/valid-effect-capability?)
(def ^{:private true :arglists '([value])}
  valid-profile-direct-imports? policy/valid-profile-direct-imports?)
(def ^{:private true :arglists '([id message data])}
  default-fail! policy/default-fail!)
(def ^{:private true :arglists '([source-path form-index])}
  default-source-span policy/default-source-span)
(def ^{:private true :arglists '([form])}
  default-ns-form? policy/default-ns-form?)
(def ^{:private true :arglists '([text])}
  default-sha256-hex policy/default-sha256-hex)
(def ^{:private true :arglists '([key])}
  unsupported-operation policy/unsupported-operation)

(defn- scalar-operation-value [key fallback]
  (if (contains? *operations* key) (get *operations* key) fallback))
(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key) (get *operations* key)))
(defn- invoke [key & args]
  (if-let [operation (current-operation key)]
    (apply operation args)
    (throw (ex-info (str "gravity.module-analysis requires operation " key)
                    {:operation key}))))
(defn- default-bootstrap-target-supported? [target]
  (contains? (scalar-operation-value :supported-targets supported-targets-default)
             target))

(defmacro ^:private definterposable [name key arguments & body]
  `(defn ~name ~arguments
     (if (contains? *bypass-next-operation-keys* ~key)
       (binding [*bypass-next-operation-keys* (disj *bypass-next-operation-keys* ~key)]
         (do ~@body))
       (if-let [operation# (current-operation ~key)]
         (binding [*active-operation-keys* (conj *active-operation-keys* ~key)]
           (operation# ~@arguments))
         (do ~@body)))))
(defmacro ^:private definterposable-private [name key arguments & body]
  `(defn- ~name ~arguments
     (if (contains? *bypass-next-operation-keys* ~key)
       (binding [*bypass-next-operation-keys* (disj *bypass-next-operation-keys* ~key)]
         (do ~@body))
       (if-let [operation# (current-operation ~key)]
         (binding [*active-operation-keys* (conj *active-operation-keys* ~key)]
           (operation# ~@arguments))
         (do ~@body)))))

(defn- fail! [id message data]
  (if-let [operation (current-operation :fail!)]
    (operation id message data) (default-fail! id message data)))
(defn- source-span [source-path form-index]
  (if-let [operation (current-operation :source-span)]
    (operation source-path form-index) (default-source-span source-path form-index)))
(definterposable-private ns-form? :ns-form? [form] (default-ns-form? form))
(definterposable-private bootstrap-target-supported? :bootstrap-target-supported?
  [target] (default-bootstrap-target-supported? target))
(definterposable-private sha256-hex :sha256-hex [text] (default-sha256-hex text))
(definterposable-private validate-ns-syntax! :validate-ns-syntax! [source-path forms]
  ((unsupported-operation :validate-ns-syntax!) source-path forms))
(defn- syntax-object-stream
  ([source-path form-records] (syntax-object-stream source-path form-records nil))
  ([source-path form-records module-context]
   (if (contains? *bypass-next-operation-keys* :syntax-object-stream)
     (binding [*bypass-next-operation-keys* (disj *bypass-next-operation-keys* :syntax-object-stream)]
       ((unsupported-operation :syntax-object-stream) source-path form-records module-context))
     (if-let [operation (current-operation :syntax-object-stream)]
       (binding [*active-operation-keys* (conj *active-operation-keys* :syntax-object-stream)]
         (operation source-path form-records module-context))
       ((unsupported-operation :syntax-object-stream) source-path form-records module-context)))))

(definterposable require-ns :require-ns [source-path forms]
  (clauses/require-ns {:ns-form? ns-form? :fail! fail! :source-span source-span} source-path forms))
(definterposable parse-clause :parse-clause [source-path clause]
  (clauses/parse-clause {:fail! fail! :source-span source-span} source-path clause))
(definterposable single-clause-value :single-clause-value [source-path clause-map key required?]
  (clauses/single-clause-value {:fail! fail! :source-span source-span} source-path clause-map key required?))
(definterposable clause-args :clause-args [clause-map key]
  (clauses/clause-args clause-map key))
(definterposable parse-options :parse-options [source-path entry option-items]
  (dependencies/parse-options {:fail! fail! :source-span source-span} source-path entry option-items))
(definterposable parse-dependency-entry :parse-dependency-entry [source-path kind entry]
  (dependencies/parse-dependency-entry {:fail! fail! :source-span source-span :parse-options parse-options} source-path kind entry))
(definterposable parse-dependencies :parse-dependencies [source-path kind entries]
  (dependencies/parse-dependencies {:parse-dependency-entry parse-dependency-entry} source-path kind entries))
(definterposable top-level-definition :top-level-definition [syntax]
  (definitions/top-level-definition syntax))
(definterposable definition-table :definition-table [syntax module]
  (definitions/definition-table {:top-level-definition top-level-definition} syntax module))
(definterposable collect-symbols :collect-symbols [form]
  (definitions/collect-symbols collect-symbols form))
(definterposable collect-code-symbols :collect-code-symbols [form]
  (definitions/collect-code-symbols collect-code-symbols form))
(definterposable uses-println? :uses-println? [form]
  (effects/uses-println? uses-println? form))
(definterposable infer-effects :infer-effects [forms]
  (effects/infer-effects uses-println? forms))
(definterposable required-capabilities-for-effects :required-capabilities-for-effects [effects]
  (effects/required-capabilities-for-effects (scalar-operation-value :effect-capability effect-capability-default) effects))
(definterposable profile-direct-import-allowed? :profile-direct-import-allowed?
  [consumer-profile producer-profile]
  (boundaries/profile-direct-import-allowed? (scalar-operation-value :profile-direct-imports profile-direct-imports-default) consumer-profile producer-profile))
(definterposable assert-unique-aliases! :assert-unique-aliases! [source-path dependencies]
  (boundaries/assert-unique-aliases! {:fail! fail! :source-span source-span} source-path dependencies))
(definterposable assert-referred-names-unambiguous! :assert-referred-names-unambiguous!
  [source-path dependencies]
  (boundaries/assert-referred-names-unambiguous! {:fail! fail! :source-span source-span} source-path dependencies))
(definterposable assert-qualified-symbols-resolve! :assert-qualified-symbols-resolve!
  [source-path forms module dependencies]
  (boundaries/assert-qualified-symbols-resolve! {:collect-code-symbols collect-code-symbols :fail! fail!} source-path forms module dependencies))
(definterposable assert-profile-boundaries! :assert-profile-boundaries! [source-path module dependencies]
  (boundaries/assert-profile-boundaries! {:profile-direct-import-allowed? profile-direct-import-allowed? :fail! fail! :source-span source-span} source-path module dependencies))
(definterposable assert-namespace-effect-and-capability! :assert-namespace-effect-and-capability!
  [source-path module inferred-effects]
  (boundaries/assert-namespace-effect-and-capability! {:required-capabilities-for-effects required-capabilities-for-effects :fail! fail!} source-path module inferred-effects))
(definterposable parse-module :parse-module [source-path forms]
  (module/parse-module {:require-ns require-ns :parse-clause parse-clause :single-clause-value single-clause-value :clause-args clause-args :parse-dependencies parse-dependencies :fail! fail! :source-span source-span :known-source-profiles (scalar-operation-value :known-source-profiles known-source-profiles-default) :supported-profiles (scalar-operation-value :supported-profiles supported-profiles-default) :supported-targets (scalar-operation-value :supported-targets supported-targets-default) :bootstrap-target-supported? bootstrap-target-supported?} source-path forms))
(definterposable validate-module-effects! :validate-module-effects! [module]
  (effects/validate-module-effects! {:uses-println? uses-println? :fail! fail!} module))
(definterposable module-source-artifact-from-records :module-source-artifact-from-records
  [source-path source-text records]
  (artifact/module-source-artifact-from-records {:validate-ns-syntax! validate-ns-syntax! :parse-module parse-module :syntax-object-stream syntax-object-stream :assert-unique-aliases! assert-unique-aliases! :assert-referred-names-unambiguous! assert-referred-names-unambiguous! :assert-profile-boundaries! assert-profile-boundaries! :assert-qualified-symbols-resolve! assert-qualified-symbols-resolve! :infer-effects infer-effects :assert-namespace-effect-and-capability! assert-namespace-effect-and-capability! :definition-table definition-table :sha256-hex sha256-hex :required-capabilities-for-effects required-capabilities-for-effects} source-path source-text records))

(defn with-operations [operations thunk]
  (policy/validate-operations! operations)
  (when-not (fn? thunk)
    (throw (ex-info "Gravity module-analysis thunk must be a function" {:thunk thunk})))
  (binding [*operations* (merge *operations* operations)] (thunk)))

(defn call-entrypoint-body
  "Enter one extracted function body with one-shot operation bypass.

  The first invocation of the supplied operation runs its captured original
  body. A recursive call through the same public Var can then observe the
  injected operation without recursing indefinitely through that operation."
  [operation-key operation args]
  (when-not (contains? operation-keys operation-key)
    (throw (ex-info "Gravity module-analysis entrypoint key is unknown" {:operation operation-key})))
  (when-not (fn? operation)
    (throw (ex-info "Gravity module-analysis entrypoint must be a function" {:operation operation-key :value operation})))
  (when-not (sequential? args)
    (throw (ex-info "Gravity module-analysis entrypoint args must be sequential" {:operation operation-key :args args})))
  (binding [*active-operation-keys* (disj *active-operation-keys* operation-key)
            *bypass-next-operation-keys* (conj *bypass-next-operation-keys* operation-key)]
    (apply operation args)))

(def public-api contract/public-api)
(defn module-analysis-engine-contract []
  (assoc namespace-contract :public-api public-api))
