(ns gravity.profile-validation
  "Pure hosted Stage0 profile-validation compatibility projection.

  The leaf consumes an already-produced effected-core/module projection and
  externally supplied policy tables.  It owns only profile legality facts and
  a deterministic profile-validation report.  Capability declarations and
  requirements are passed through for the following capability pass; this
  namespace does not decide whether any grant authorizes an effect."
  (:require [clojure.set :as set]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private ^:dynamic *bypass-next-operation-keys* #{})

(def ^:private function-operation-keys
  #{:stable-set
    :stable-vec
    :diagnostic-record
    :all-registered-effects
    :effect-registry-entry
    :profile-allowed-effects
    :profile-capabilities
    :profile-contract
    :profile-policy-layer
    :profile-effective-effects
    :effect-permission-table
    :profile-validation-facts})

(def ^:private scalar-operation-keys
  #{:standard-profile-order
    :profile-diagnostic-ids
    :profile-memory-regimes
    :profile-runtime-assumptions
    :profile-unsafe-policies
    :profile-artifact-boundaries
    :effect-registry
    :provider-specs
    :core-forms
    :supported-targets})

(def ^:private operation-keys
  (into function-operation-keys scalar-operation-keys))

(def ^:private standard-profile-order-default
  [:core :meta :hosted :native :firmware :kernel :hardware :distributed :ai
   :gpu :formal])

(def ^:private profile-diagnostic-ids-default
  ["P1-MISSING-PROFILE" "P1-AMBIGUOUS-PROFILE" "P1-EFFECT"
   "P1-CAPABILITY" "P1-MEMORY" "P1-RUNTIME" "P1-CROSS-IMPORT"
   "P1-MACRO" "P1-FACET" "P1-BACKEND"])

(def ^:private profile-memory-regimes-default
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

(def ^:private profile-runtime-assumptions-default
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

(def ^:private profile-unsafe-policies-default
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

(def ^:private profile-artifact-boundaries-default
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

(def ^:private default-scalars
  {:standard-profile-order standard-profile-order-default
   :profile-diagnostic-ids profile-diagnostic-ids-default
   :profile-memory-regimes profile-memory-regimes-default
   :profile-runtime-assumptions profile-runtime-assumptions-default
   :profile-unsafe-policies profile-unsafe-policies-default
   :profile-artifact-boundaries profile-artifact-boundaries-default})

(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key)
    (get *operations* key)))

(defn- operation-value [key]
  (if (contains? *operations* key)
    (get *operations* key)
    (if (contains? default-scalars key)
      (get default-scalars key)
      (throw (ex-info "Profile validation requires an injected operation"
                      {:operation key})))))

(defn- default-stable-vec [values]
  (->> values (sort-by pr-str) vec))

(defn- default-stable-set [values]
  (into (sorted-set-by #(compare (pr-str %1) (pr-str %2))) values))

(defn- invoke [key & args]
  (if-let [operation (current-operation key)]
    (apply operation args)
    (case key
      :stable-set (apply default-stable-set args)
      :stable-vec (apply default-stable-vec args)
      :diagnostic-record
      (let [[id facts] args]
        {:artifact :gravity/profile-diagnostic
         :diagnostic id
         :stage :profile-validation
         :facts facts
         :status :rejected})
      (throw (ex-info "Profile validation requires a function operation"
                      {:operation key})))))

(defmacro ^:private definterposable [name key arguments & body]
  `(defn ~name ~arguments
     (if (contains? *bypass-next-operation-keys* ~key)
       (binding [*bypass-next-operation-keys*
                 (disj *bypass-next-operation-keys* ~key)]
         (do ~@body))
       (if-let [operation# (current-operation ~key)]
         (binding [*active-operation-keys*
                   (conj *active-operation-keys* ~key)]
           (operation# ~@arguments))
         (do ~@body)))))

(definterposable profile-allowed-effects :profile-allowed-effects
  [profile]
  (->> (operation-value :effect-registry)
       (keep (fn [[effect entry]]
               (when (or (contains? (:profiles entry #{}) profile)
                         (and (:requires-build-grant entry)
                              (contains? #{:meta :hosted} profile)))
                 effect)))
       (invoke :stable-set)))

(definterposable profile-capabilities :profile-capabilities
  [profile]
  (->> (operation-value :provider-specs)
       (keep (fn [[capability spec]]
               (when (contains? (:profiles spec #{}) profile)
                 capability)))
       (invoke :stable-set)))

(definterposable all-registered-effects :all-registered-effects
  []
  (set (keys (operation-value :effect-registry))))

(definterposable effect-registry-entry :effect-registry-entry
  [effect]
  (get (operation-value :effect-registry) effect))

(definterposable profile-contract :profile-contract
  [profile]
  (let [allowed-effects (profile-allowed-effects profile)
        effect-registry (operation-value :effect-registry)
        registered-effects (all-registered-effects)]
    {:profile profile
     :allowed-forms (operation-value :core-forms)
     :allowed-effects allowed-effects
     :checked-effects
     (invoke :stable-set
             (for [[effect entry] effect-registry
                   :when (and (contains? allowed-effects effect)
                              (or (:requires-capability entry)
                                  (:requires-build-grant entry)))]
               effect))
     :forbidden-effects
     (set/difference registered-effects (set allowed-effects))
     ;; This is profile legality, copied from the P1 contract.  It is not a
     ;; package, provider, or deployment grant.
     :capabilities (profile-capabilities profile)
     :memory ((operation-value :profile-memory-regimes) profile)
     :runtime ((operation-value :profile-runtime-assumptions) profile)
     :nondeterminism (if (contains? #{:distributed :ai} profile)
                       :recorded-when-effectful
                       :profile-specific)
     :unsafe-policy ((operation-value :profile-unsafe-policies) profile)
     :artifact-boundaries
     ((operation-value :profile-artifact-boundaries) profile)}))

(definterposable profile-policy-layer :profile-policy-layer
  [module metadata-key source-key default-value]
  (or (get-in module [:metadata metadata-key])
      (source-key module)
      default-value))

(definterposable profile-effective-effects :profile-effective-effects
  [module inferred-effects]
  (let [source-effects (:effects module)
        profile-effects (profile-allowed-effects (:profile module))
        package-effects
        (profile-policy-layer module :package-allowed-effects :effects #{})
        provider-effects
        (profile-policy-layer module :provider-effect-grants :effects #{})
        deployment-effects
        (profile-policy-layer module :deployment-allowed-effects :effects #{})]
    {:source source-effects
     :inferred inferred-effects
     :profile profile-effects
     :package package-effects
     :provider provider-effects
     :deployment deployment-effects
     :effective (set/intersection source-effects profile-effects
                                  package-effects provider-effects
                                  deployment-effects)}))

(definterposable effect-permission-table :effect-permission-table
  [module inferred-effects effective]
  (let [source-effects (:source effective)
        row-effects (set/union source-effects inferred-effects)]
    (mapv
     (fn [effect]
       (let [entry (or (effect-registry-entry effect) {})
             profile-allowed? (contains? (:profile effective) effect)
             package-allowed? (contains? (:package effective) effect)
             provider-granted? (contains? (:provider effective) effect)
             deployment-granted? (contains? (:deployment effective) effect)
             effective? (contains? (:effective effective) effect)]
         {:effect effect
          :family (:family entry)
          :requires-capability (boolean (:requires-capability entry))
          :capability (:capability entry)
          :declared? (contains? source-effects effect)
          :inferred? (contains? inferred-effects effect)
          :profile-allowed? profile-allowed?
          :package-allowed? package-allowed?
          :provider-granted? provider-granted?
          :deployment-granted? deployment-granted?
          :effective? effective?
          :state (cond
                   effective? :allowed
                   (not profile-allowed?) :rejected
                   (or (not package-allowed?)
                       (not provider-granted?)
                       (not deployment-granted?)) :checked
                   :else :rejected)
          :policy-layer (cond
                          (not profile-allowed?) :profile
                          (not package-allowed?) :package
                          (not provider-granted?) :provider
                          (not deployment-granted?) :deployment
                          :else :effective)}))
     (invoke :stable-vec row-effects))))

(defn- keyword-set? [value]
  (and (set? value) (every? keyword? value)))

(defn- valid-module? [module]
  (and (map? module)
       (keyword? (:profile module))
       (keyword? (:target module))
       (keyword-set? (:effects module))
       (keyword-set? (:capabilities module))
       (map? (:metadata module))))

(defn- diagnostic-context [module typed-artifact producing-pass consuming-pass]
  {:profile (:profile module)
   :target (:target module)
   :source-span (first (:source-spans typed-artifact))
   :producing-pass producing-pass
   :consuming-pass consuming-pass})

(definterposable profile-validation-facts :profile-validation-facts
  [module typed-artifact module-artifact]
  (when-not (valid-module? module)
    (throw (ex-info "Profile validation module input is malformed"
                    {:module module})))
  (when-not (and (map? typed-artifact) (map? module-artifact))
    (throw (ex-info "Profile validation requires prebuilt artifact maps"
                    {:typed-artifact typed-artifact
                     :module-artifact module-artifact})))
  (let [inferred-effects
        (or (:inferred-effects typed-artifact)
            (get-in typed-artifact [:namespace-effect-summary :inferred])
            #{})
        _ (when-not (keyword-set? inferred-effects)
            (throw (ex-info "Profile validation inferred effects are malformed"
                            {:inferred-effects inferred-effects})))
        required-capabilities (or (:required-capabilities typed-artifact) #{})
        _ (when-not (keyword-set? required-capabilities)
            (throw (ex-info "Profile validation required capabilities are malformed"
                            {:required-capabilities required-capabilities})))
        authority (profile-effective-effects module inferred-effects)
        permission-table
        (effect-permission-table module inferred-effects authority)
        contract (profile-contract (:profile module))
        supported-profile?
        (contains? (set (operation-value :standard-profile-order))
                   (:profile module))
        target-eligible?
        (contains? (operation-value :supported-targets) (:target module))
        rejected-effects (filterv #(= :rejected (:state %)) permission-table)
        checked-effects (filterv #(= :checked (:state %)) permission-table)
        context (diagnostic-context module typed-artifact
                                    :effect-checking :profile-validation)
        diagnostics
        (vec
         (concat
          (when-not supported-profile?
            [(invoke :diagnostic-record "P1-MISSING-PROFILE"
                     (assoc context :remediation
                            :declare-a-standard-profile))])
          (map #(invoke :diagnostic-record "P1-EFFECT"
                        (merge context
                               (select-keys % [:effect :state :policy-layer])
                               {:remediation
                                :remove-effect-or-select-compatible-profile}))
               rejected-effects)
          (when-not target-eligible?
            [(invoke :diagnostic-record "P1-BACKEND"
                     (assoc context :remediation
                            :select-a-supported-target))])))
        accepted? (and supported-profile? target-eligible?
                       (empty? rejected-effects))
        pass
        {:name :profile-validation
         :input :effected-core
         :output :profile-valid-core
         :requires [:type-facts :effect-facts :profile-declaration
                    :module-facts :module-dependency-graph
                    :profile-effect-policy :profile-capability-policy]
         :preserves [:source-spans :types :effects :profile-context
                     :capability-requirements]
         :invalidates [:unchecked-profile-assumptions]
         ;; The report is a new pass output, not a replacement for an
         ;; invalidated fact from the input artifact.
         :regenerates []
         :emits [:profile-facts :effect-permission-table
                 :profile-validation-report :profile-diagnostics
                 :input-provenance]
         :rejects (operation-value :profile-diagnostic-ids)}
        report
        {:artifact :gravity/profile-validation-report
         :profile (:profile module)
         :target (:target module)
         :profile-contract contract
         :effect-permission-table permission-table
         :memory-regime (:memory contract)
         :runtime-assumptions (:runtime contract)
         :backend-eligibility
         {:profile (:profile module)
          :target (:target module)
          :eligible? target-eligible?
          :decision (if target-eligible? :eligible :rejected)
          :authority? false}
         :checked-effects checked-effects
         :rejected-effects rejected-effects
         :diagnostics diagnostics
         :status (if accepted? :accepted :rejected)}]
    {:kind :gravity/stage0-profile-validation-facts
     :pass pass
     :profile-valid-core
     {:artifact :gravity/profile-valid-core
      :effected-core (or (:effected-core typed-artifact)
                         (:typed-core-module typed-artifact))
      :profile (:profile module)
      :target (:target module)
      :effects (:effective authority)
      :declared-capabilities (:capabilities module)
      :required-capabilities
      required-capabilities
      :source-spans (:source-spans typed-artifact)
      :status (if accepted? :accepted :rejected)}
     :profile-validation-report report
     :profile-diagnostics diagnostics
     :module-dependency-graph (:module-dependency-graph module-artifact)
     :input-provenance
     {:typed-artifact-kind (:kind typed-artifact)
      :module-artifact-kind (:kind module-artifact)
      :module (:module module)}
     :status (if accepted? :accepted :rejected)}))

(defn- distinct-keyword-vector? [value]
  (and (vector? value) (seq value) (every? keyword? value)
       (= (count value) (count (distinct value)))))

(defn- non-empty-string-vector? [value]
  (and (vector? value) (seq value) (every? #(and (string? %) (seq %)) value)))

(defn- profile-map? [value value-predicate]
  (and (map? value) (seq value) (every? keyword? (keys value))
       (every? value-predicate (vals value))))

(defn- effect-registry? [value]
  (and (map? value) (seq value)
       (every? keyword? (keys value))
       (every? (fn [entry]
                 (and (map? entry)
                      (set? (:profiles entry))
                      (every? keyword? (:profiles entry))))
               (vals value))))

(defn- provider-specs? [value]
  (and (map? value) (seq value) (every? keyword? (keys value))
       (every? (fn [spec]
                 (and (map? spec)
                      (set? (:profiles spec))
                      (every? keyword? (:profiles spec))))
               (vals value))))

(defn- validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "Profile validation operation map must be a map"
                    {:operations operations})))
  (let [unknown (vec (remove operation-keys (keys operations)))
        invalid-functions
        (vec (for [[key value] (select-keys operations function-operation-keys)
                   :when (not (fn? value))]
               key))]
    (when (seq unknown)
      (throw (ex-info "Profile validation operation map has unknown keys"
                      {:unknown-keys unknown})))
    (when (seq invalid-functions)
      (throw (ex-info "Profile validation function operation is not a function"
                      {:operation-keys invalid-functions}))))
  (doseq [[key predicate expected]
          [[:standard-profile-order distinct-keyword-vector?
            :distinct-non-empty-keyword-vector]
           [:profile-diagnostic-ids non-empty-string-vector?
            :non-empty-string-vector]
           [:profile-memory-regimes #(profile-map? % map?)
            :keyword-to-map]
           [:profile-runtime-assumptions #(profile-map? % map?)
            :keyword-to-map]
           [:profile-unsafe-policies #(profile-map? % keyword?)
            :keyword-to-keyword]
           [:profile-artifact-boundaries
            #(profile-map? % (fn [items]
                               (and (set? items) (seq items)
                                    (every? keyword? items))))
            :keyword-to-non-empty-keyword-set]
           [:effect-registry effect-registry? :effect-registry]
           [:provider-specs provider-specs? :provider-specs]
           [:core-forms #(and (set? %) (seq %) (every? symbol? %))
            :non-empty-symbol-set]
           [:supported-targets #(and (set? %) (seq %)
                                     (every? keyword? %))
            :non-empty-keyword-set]]
          :when (and (contains? operations key)
                     (not (predicate (get operations key))))]
    (throw (ex-info "Profile validation scalar operation has an invalid shape"
                    {:operation key :expected expected
                     :actual (get operations key)})))
  operations)

(defn with-operations [operations thunk]
  (validate-operations! operations)
  (when-not (fn? thunk)
    (throw (ex-info "Profile validation thunk must be a function"
                    {:thunk thunk})))
  (binding [*operations* (merge *operations* operations)]
    (thunk)))

(defn call-entrypoint-body [operation-key operation args]
  (when-not (contains? function-operation-keys operation-key)
    (throw (ex-info "Profile validation entrypoint key is unknown"
                    {:operation operation-key})))
  (when-not (fn? operation)
    (throw (ex-info "Profile validation entrypoint must be a function"
                    {:operation operation-key :value operation})))
  (when-not (sequential? args)
    (throw (ex-info "Profile validation entrypoint args must be sequential"
                    {:operation operation-key :args args})))
  (binding [*active-operation-keys*
            (disj *active-operation-keys* operation-key)
            *bypass-next-operation-keys*
            (conj *bypass-next-operation-keys* operation-key)]
    (apply operation args)))

(def ^:private namespace-contract
  {:namespace 'gravity.profile-validation
   :contract-boundary :hosted-stage0-profile-validation-projection
   :artifact-inputs [:effected-core :module-facts :profile-policy-tables]
   :artifact-outputs [:profile-valid-core :profile-validation-report
                      :profile-diagnostics]
   :owns [:hosted-profile-effect-policy-intersection
          :hosted-profile-capability-legality-policy
          :hosted-profile-validation-facts
          :hosted-profile-validation-report]
   :does-not-own [:source-reading :typed-core-construction
                  :effect-checking-authority :capability-validation
                  :capability-grant-authority :package-grant-authority
                  :deployment-grant-authority :backend-execution
                  :backend-eligibility-authority :canonical-p1-authority
                  :proof-authority :attestation-authority :self-hosting
                  :seed-retirement :release-authority]
   :dependency-direction
   {:requires ['clojure.core 'clojure.set]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :compatibility-only? true
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-p1-authority? false
   :capability-grant-authority? false
   :backend-eligibility-authority? false
   :proof-authority? false
   :self-hosted? false
   :release-authority? false
   :operation-interposition
   {:accepted-keys operation-keys
    :partial-overrides? true
    :unknown-keys-rejected? true
    :function-values-must-satisfy :fn?
    :captured-original-one-shot? true}})

(def public-api
  {'public-api {:kind :contract}
   'profile-validation-contract {:arglists '([])}
   'with-operations {:arglists '([operations thunk])}
   'call-entrypoint-body {:arglists '([operation-key operation args])}
   'all-registered-effects {:arglists '([])}
   'effect-registry-entry {:arglists '([effect])}
   'profile-allowed-effects {:arglists '([profile])}
   'profile-capabilities {:arglists '([profile])}
   'profile-contract {:arglists '([profile])}
   'profile-policy-layer
   {:arglists '([module metadata-key source-key default-value])}
   'profile-effective-effects {:arglists '([module inferred-effects])}
   'effect-permission-table
   {:arglists '([module inferred-effects effective])}
   'profile-validation-facts
   {:arglists '([module typed-artifact module-artifact])}})

(defn profile-validation-contract []
  (assoc namespace-contract :public-api public-api))
