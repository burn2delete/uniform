(ns gravity.module-analysis
  "Hosted Stage0 L3 module-analysis compatibility projection.

  This leaf consumes already-read forms and externally supplied syntax and
  validation operations. It owns only the small namespace-analysis cluster
  used by the hosted bootstrap. It does not read source, authenticate reader
  products, execute macros, establish canonical L3 authority, or grant proof,
  self-hosting, attestation, or release authority."
  (:require [clojure.string :as str]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private ^:dynamic *bypass-next-operation-keys* #{})

(def ^:private function-operation-keys
  #{:fail!
    :source-span
    :ns-form?
    :bootstrap-target-supported?
    :validate-ns-syntax!
    :syntax-object-stream
    :sha256-hex
    :require-ns
    :parse-clause
    :single-clause-value
    :clause-args
    :parse-options
    :parse-dependency-entry
    :parse-dependencies
    :top-level-definition
    :definition-table
    :collect-symbols
    :collect-code-symbols
    :infer-effects
    :required-capabilities-for-effects
    :profile-direct-import-allowed?
    :assert-unique-aliases!
    :assert-referred-names-unambiguous!
    :assert-qualified-symbols-resolve!
    :assert-profile-boundaries!
    :assert-namespace-effect-and-capability!
    :parse-module
    :uses-println?
    :validate-module-effects!
    :module-source-artifact-from-records})

(def ^:private scalar-operation-keys
  #{:known-source-profiles
    :supported-profiles
    :supported-targets
    :effect-capability
    :profile-direct-imports})

(def ^:private operation-keys
  (into function-operation-keys scalar-operation-keys))

(def ^:private known-source-profiles-default
  #{:core :hardware :firmware :kernel :native :hosted
    :distributed :ai :meta :gpu :formal})

(def ^:private supported-profiles-default #{:hosted})
(def ^:private supported-targets-default #{:jvm})

(def ^:private effect-capability-default
  {:io/write :io/stdout
   :network/listen :network/listener})

(def ^:private profile-direct-imports-default
  {:core #{:core}
   :meta #{:core :meta}
   :hosted #{:core :hosted}
   :native #{:core :native}
   :firmware #{:core :firmware}
   :kernel #{:core :kernel}
   :hardware #{:core :hardware}
   :distributed #{:core :distributed}
   :ai #{:core :distributed :ai}
   :gpu #{:core :gpu}
   :formal #{:core :formal}})

(def ^:private namespace-contract
  {:namespace 'gravity.module-analysis
   :contract-boundary :hosted-stage0-l3-module-analysis-projection
   :public-api
   {'with-operations {:arglists '([operations thunk])}
    'call-entrypoint-body {:arglists '([operation-key operation args])}
    'require-ns {:arglists '([source-path forms])}
    'parse-clause {:arglists '([source-path clause])}
    'single-clause-value {:arglists '([source-path clause-map key required?])}
    'clause-args {:arglists '([clause-map key])}
    'parse-options {:arglists '([source-path entry option-items])}
    'parse-dependency-entry {:arglists '([source-path kind entry])}
    'parse-dependencies {:arglists '([source-path kind entries])}
    'top-level-definition {:arglists '([syntax])}
    'definition-table {:arglists '([syntax module])}
    'collect-symbols {:arglists '([form])}
    'collect-code-symbols {:arglists '([form])}
    'infer-effects {:arglists '([forms])}
    'required-capabilities-for-effects {:arglists '([effects])}
    'profile-direct-import-allowed?
    {:arglists '([consumer-profile producer-profile])}
    'assert-unique-aliases! {:arglists '([source-path dependencies])}
    'assert-referred-names-unambiguous!
    {:arglists '([source-path dependencies])}
    'assert-qualified-symbols-resolve!
    {:arglists '([source-path forms module dependencies])}
    'assert-profile-boundaries!
    {:arglists '([source-path module dependencies])}
    'assert-namespace-effect-and-capability!
    {:arglists '([source-path module inferred-effects])}
    'parse-module {:arglists '([source-path forms])}
    'uses-println? {:arglists '([form])}
    'validate-module-effects! {:arglists '([module])}
    'module-source-artifact-from-records
    {:arglists '([source-path source-text records])}}
   :operation-interposition
   {:accepted-keys operation-keys
    :partial-overrides? true
    :unknown-keys-rejected? true
    :function-values-must-satisfy :fn?
    :scalar-values-must-satisfy
    {:known-source-profiles :non-empty-keyword-set
     :supported-profiles :non-empty-keyword-set
     :supported-targets :non-empty-keyword-set
     :effect-capability :keyword-to-keyword-map
     :profile-direct-imports :keyword-to-non-empty-keyword-set-map}
    :entrypoint-requirements
    {'require-ns #{:fail! :source-span :ns-form?}
     'required-capabilities-for-effects #{:effect-capability}
     'profile-direct-import-allowed? #{:profile-direct-imports}
     'assert-profile-boundaries!
     #{:fail! :source-span :profile-direct-imports}
     'assert-namespace-effect-and-capability!
     #{:fail! :effect-capability}
     'parse-module #{:fail! :source-span :ns-form?
                     :known-source-profiles :supported-targets
                     :bootstrap-target-supported?}
     'module-source-artifact-from-records
     #{:fail! :source-span :ns-form? :validate-ns-syntax!
       :syntax-object-stream :sha256-hex :effect-capability
       :profile-direct-imports}}}
   :artifact-inputs [:hosted-reader-forms
                     :source-text-for-identity
                     :source-path-provenance
                     :injected-syntax-stream]
   :artifact-outputs [:hosted-module-analysis-tables
                      :hosted-module-artifact
                      :hosted-public-api-manifest]
   :ownership
   {:owns [:hosted-stage0-l3-namespace-clause-projection
           :hosted-stage0-l3-dependency-projection
           :hosted-stage0-l3-definition-table
           :hosted-stage0-l3-symbol-and-effect-facts
           :hosted-stage0-l3-profile-boundary-checks
           :hosted-stage0-l3-module-artifact-projection]
    :does-not-own [:source-reading
                   :filesystem-access
                   :reader-tokenization
                   :reader-form-construction
                   :reader-product-authentication
                   :canonical-c2-reader-authority
                   :canonical-c3-syntax-authority
                   :canonical-l3-module-analysis-authority
                   :namespace-policy-authority
                   :macro-execution
                   :diagnostic-policy
                   :proof-authority
                   :attestation-authority
                   :self-hosted-authority
                   :release-authority]}
   :dependency-direction
   {:requires ['clojure.core 'clojure.string
               'java.security.MessageDigest]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :bootstrap-hosted? true
   :compatibility-only? true
   :clojure-seed-boundary? true
   :canonical-l3-authority? false
   :source-reading? false
   :filesystem-access? false
   :macro-execution? false
   :diagnostic-policy-authority? false
   :proof-authority? false
   :self-hosted? false
   :release-authority? false})

(defn- valid-keyword-set?
  [value]
  (and (set? value)
       (seq value)
       (every? keyword? value)))

(defn- valid-effect-capability?
  [value]
  (and (map? value)
       (every? keyword? (keys value))
       (every? keyword? (vals value))))

(defn- valid-profile-direct-imports?
  [value]
  (and (map? value)
       (every? keyword? (keys value))
       (every? valid-keyword-set? (vals value))))

(defn- default-fail!
  [id message data]
  (throw (ex-info message
                  (merge {:id id
                          :message message
                          :bootstrap-stage :stage0}
                         data))))

(defn- default-source-span
  [source-path form-index]
  {:source source-path :form-index form-index})

(declare scalar-operation-value)

(defn- default-ns-form?
  [form]
  (and (seq? form) (= 'ns (first form))))

(defn- default-bootstrap-target-supported?
  [target]
  (contains? (scalar-operation-value :supported-targets
                                     supported-targets-default)
             target))

(defn- default-sha256-hex
  [text]
  (let [digest (.digest (java.security.MessageDigest/getInstance "SHA-256")
                        (.getBytes ^String text "UTF-8"))]
    (apply str (map #(format "%02x" (bit-and % 0xff)) digest))))

(defn- unsupported-operation
  [key]
  (fn [& _]
    (throw (ex-info (str "gravity.module-analysis requires operation " key)
                    {:operation key}))))

(defn- scalar-operation-value
  [key fallback]
  (if (contains? *operations* key)
    (get *operations* key)
    fallback))

(defn- current-operation
  [key]
  (when-not (contains? *active-operation-keys* key)
    (get *operations* key)))

(defn- invoke
  [key & args]
  (if-let [operation (current-operation key)]
    (apply operation args)
    (throw (ex-info (str "gravity.module-analysis requires operation " key)
                    {:operation key}))))

(defmacro ^:private definterposable
  [name key arguments & body]
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

(defmacro ^:private definterposable-private
  [name key arguments & body]
  `(defn- ~name ~arguments
     (if (contains? *bypass-next-operation-keys* ~key)
       (binding [*bypass-next-operation-keys*
                 (disj *bypass-next-operation-keys* ~key)]
         (do ~@body))
       (if-let [operation# (current-operation ~key)]
         (binding [*active-operation-keys*
                   (conj *active-operation-keys* ~key)]
           (operation# ~@arguments))
         (do ~@body)))))

(defn- fail!
  [id message data]
  (if-let [operation (current-operation :fail!)]
    (operation id message data)
    (default-fail! id message data)))

(defn- source-span
  [source-path form-index]
  (if-let [operation (current-operation :source-span)]
    (operation source-path form-index)
    (default-source-span source-path form-index)))

(definterposable-private ns-form? :ns-form?
  [form]
  (default-ns-form? form))

(definterposable-private bootstrap-target-supported? :bootstrap-target-supported?
  [target]
  (default-bootstrap-target-supported? target))

(definterposable-private sha256-hex :sha256-hex
  [text]
  (default-sha256-hex text))

(definterposable-private validate-ns-syntax! :validate-ns-syntax!
  [source-path forms]
  ((unsupported-operation :validate-ns-syntax!) source-path forms))

(defn- syntax-object-stream
  ([source-path form-records]
   (syntax-object-stream source-path form-records nil))
  ([source-path form-records module-context]
   (if (contains? *bypass-next-operation-keys* :syntax-object-stream)
     (binding [*bypass-next-operation-keys*
               (disj *bypass-next-operation-keys* :syntax-object-stream)]
       ((unsupported-operation :syntax-object-stream)
        source-path form-records module-context))
     (if-let [operation (current-operation :syntax-object-stream)]
       (binding [*active-operation-keys*
                 (conj *active-operation-keys* :syntax-object-stream)]
         (operation source-path form-records module-context))
       ((unsupported-operation :syntax-object-stream)
        source-path form-records module-context)))))

(definterposable require-ns :require-ns
  [source-path forms]
  (let [first-form (first forms)]
    (when-not (ns-form? first-form)
      (fail! "L3-NS-MISSING"
             "Gravity source must start with an ns form"
             {:source-span (source-span source-path 0)
              :remediation "Add an ns form with :profile, :effects, and :capabilities clauses."}))
    first-form))

(definterposable parse-clause :parse-clause
  [source-path clause]
  (when-not (and (seq? clause) (keyword? (first clause)))
    (fail! "L3-NS-CLAUSE"
           "namespace clause must be a list starting with a keyword"
           {:source-span (source-span source-path 0)
            :clause clause
            :remediation "Use clauses such as (:profile :hosted) or (:effects #{:io/write})."}))
  [(first clause) (vec (rest clause))])

(definterposable single-clause-value :single-clause-value
  [source-path clause-map key required?]
  (let [values (get clause-map key)]
    (cond
      (and required? (empty? values))
      (fail! "L3-NS-MISSING"
             (str "namespace is missing " key " clause")
             {:source-span (source-span source-path 0)
              :missing key
              :remediation "Declare the required namespace clause."})

      (> (count values) 1)
      (fail! "L3-PROFILE-MULTIPLE"
             (str "namespace declares " key " more than once")
             {:source-span (source-span source-path 0)
              :clause key
              :remediation "Keep one active implementation profile/target clause."})

      :else
      (first values))))

(definterposable clause-args :clause-args
  [clause-map key]
  (vec (mapcat identity (get clause-map key))))

(definterposable parse-options :parse-options
  [source-path entry option-items]
  (when-not (even? (count option-items))
    (fail! "L3-UNKNOWN-ALIAS"
           "namespace dependency options must be key/value pairs"
           {:source-span (source-span source-path 0)
            :entry entry
            :remediation "Use dependency entries such as [gravity.io :as io :profile :hosted]."}))
  (loop [items option-items
         options {}]
    (if-let [[k v & more] (seq items)]
      (do
        (when-not (keyword? k)
          (fail! "L3-UNKNOWN-ALIAS"
                 "namespace dependency option keys must be keywords"
                 {:source-span (source-span source-path 0)
                  :entry entry
                  :option k
                  :remediation "Use keyword options such as :as, :refer, :profile, :effects, or :boundary."}))
        (recur more (assoc options k v)))
      options)))

(definterposable parse-dependency-entry :parse-dependency-entry
  [source-path kind entry]
  (when-not (and (vector? entry) (symbol? (first entry)))
    (fail! "L3-UNKNOWN-ALIAS"
           "namespace dependency entry must start with a module symbol"
           {:source-span (source-span source-path 0)
            :entry entry
            :remediation "Use entries such as [gravity.io :as io]."}))
  (let [[module & option-items] entry
        options (parse-options source-path entry option-items)
        alias (:as options)
        refer (:refer options)
        effects (or (:effects options) #{})
        capabilities (or (:capabilities options) #{})]
    (when (and alias (not (symbol? alias)))
      (fail! "L3-UNKNOWN-ALIAS"
             "namespace alias must be a symbol"
             {:source-span (source-span source-path 0)
              :entry entry
              :alias alias
              :remediation "Use :as with a symbolic alias."}))
    (when (= :all refer)
      (fail! "L3-AMBIGUOUS-NAME"
             "wildcard imports are rejected for stable stage0 modules"
             {:source-span (source-span source-path 0)
              :entry entry
              :remediation "Import explicit public symbols instead of :refer :all."}))
    (when (some #(str/starts-with? (name %) "private-")
                (if (vector? refer) refer []))
      (fail! "L3-PRIVATE-IMPORT"
             "private definitions cannot be imported as public API"
             {:source-span (source-span source-path 0)
              :entry entry
              :refer refer
              :remediation "Export a public facade or remove the private import."}))
    {:kind kind
     :module module
     :alias alias
     :refer (cond
              (nil? refer) []
              (vector? refer) refer
              :else [refer])
     :profile (:profile options)
     :boundary (:boundary options)
     :edge (:edge options)
     :facade (:facade options)
     :evidence (or (:evidence options) #{})
     :artifact (:artifact options)
     :artifact-schema (:artifact-schema options)
     :runtime (:runtime options)
     :memory (:memory options)
     :generated? (boolean (:generated? options))
     :matrix-override (:matrix-override options)
     :producer-effects (or (:producer-effects options) #{})
     :producer-capabilities (or (:producer-capabilities options) #{})
     :safety-evidence (or (:safety-evidence options) #{})
     :provider (:provider options)
     :effects effects
     :capabilities capabilities
     :visibility (or (:visibility options) :public)}))

(definterposable parse-dependencies :parse-dependencies
  [source-path kind entries]
  (mapv #(parse-dependency-entry source-path kind %) entries))

(definterposable top-level-definition :top-level-definition
  [syntax]
  (let [form (:form syntax)]
    (when (seq? form)
      (case (first form)
        def {:name (second form) :kind :var}
        defconst {:name (second form) :kind :compile-time-constant}
        defn {:name (second form) :kind :function}
        defmacro {:name (second form) :kind :macro}
        defschema {:name (second form) :kind :schema}
        defprotocol {:name (second form) :kind :protocol}
        nil))))

(definterposable definition-table :definition-table
  [syntax module]
  (let [exports (set (:exports module))]
    (->> syntax
         (keep (fn [syn]
                 (when-let [definition (top-level-definition syn)]
                   (when (symbol? (:name definition))
                     (merge definition
                            {:visibility (if (contains? exports (:name definition))
                                           :public
                                           :private)
                             :source-span (:span syn)
                             :profile (:profile module)
                             :safety (:safety module)
                             :latent-effects #{}
                             :required-capabilities #{}
                             :artifact-links []})))))
         vec)))

(definterposable collect-symbols :collect-symbols
  [form]
  (cond
    (symbol? form) [form]
    (seq? form) (mapcat collect-symbols form)
    (coll? form) (mapcat collect-symbols form)
    :else []))

(definterposable collect-code-symbols :collect-code-symbols
  [form]
  (cond
    (symbol? form) [form]
    (seq? form) (if (= 'quote (first form))
                  (if (symbol? (first form)) [(first form)] [])
                  (mapcat collect-code-symbols form))
    (coll? form) (mapcat collect-code-symbols form)
    :else []))

(definterposable uses-println? :uses-println?
  [form]
  (cond
    (seq? form) (or (= 'println (first form)) (some uses-println? form))
    (coll? form) (some uses-println? form)
    :else false))

(definterposable infer-effects :infer-effects
  [forms]
  (set (mapcat (fn [form]
                 (cond
                   (uses-println? form) [:io/write]
                   (and (seq? form) (= 'network-listen (first form))) [:network/listen]
                   :else []))
               forms)))

(definterposable required-capabilities-for-effects
  :required-capabilities-for-effects
  [effects]
  (set (keep (scalar-operation-value :effect-capability
                                     effect-capability-default)
             effects)))

(definterposable profile-direct-import-allowed?
  :profile-direct-import-allowed?
  [consumer-profile producer-profile]
  (contains? (get (scalar-operation-value :profile-direct-imports
                                          profile-direct-imports-default)
                  consumer-profile
                  #{})
             producer-profile))

(definterposable assert-unique-aliases! :assert-unique-aliases!
  [source-path dependencies]
  (let [aliases (keep :alias dependencies)
        duplicate (first (for [[alias n] (frequencies aliases) :when (> n 1)] alias))]
    (when duplicate
      (fail! "L3-AMBIGUOUS-NAME"
             "namespace alias resolves to multiple dependencies"
             {:source-span (source-span source-path 0)
              :alias duplicate
              :remediation "Use one unique alias per required or imported module."}))))

(definterposable assert-referred-names-unambiguous!
  :assert-referred-names-unambiguous!
  [source-path dependencies]
  (let [referred (mapcat :refer dependencies)
        duplicate (first (for [[sym n] (frequencies referred) :when (> n 1)] sym))]
    (when duplicate
      (fail! "L3-AMBIGUOUS-NAME"
             "unqualified imported name resolves to multiple dependencies"
             {:source-span (source-span source-path 0)
              :symbol duplicate
              :remediation "Remove one refer or qualify the symbol through an alias."}))))

(definterposable assert-qualified-symbols-resolve!
  :assert-qualified-symbols-resolve!
  [source-path forms module dependencies]
  (let [aliases (set (map str (keep :alias dependencies)))
        allowed-qualified (conj aliases (str (:module module)))
        unknown (first (for [sym (mapcat collect-code-symbols forms)
                             :let [ns-part (namespace sym)]
                             :when (and ns-part
                                        (not (contains? allowed-qualified ns-part))
                                        (not (str/includes? ns-part ".")))]
                         sym))]
    (when unknown
      (fail! "L3-UNKNOWN-ALIAS"
             "qualified symbol uses an unknown namespace alias"
             {:source-span {:source source-path}
              :symbol unknown
              :alias (symbol (namespace unknown))
              :remediation "Declare the alias in :requires or :imports, or use a fully qualified namespace."}))))

(definterposable assert-profile-boundaries! :assert-profile-boundaries!
  [source-path module dependencies]
  (doseq [dependency dependencies]
    (let [dep-profile (:profile dependency)
          module-profile (:profile module)]
      (when (and dep-profile
                 (not (profile-direct-import-allowed? module-profile
                                                      dep-profile))
                 (nil? (:boundary dependency)))
        (fail! "L3-CROSS-PROFILE"
               "cross-profile import requires an explicit boundary"
               {:source-span (source-span source-path 0)
                :module (:module dependency)
                :profile module-profile
                :dependency-profile dep-profile
                :remediation "Use a :core API, profile-safe facade, typed schema/artifact boundary, or explicit interop boundary."})))))

(definterposable assert-namespace-effect-and-capability!
  :assert-namespace-effect-and-capability!
  [source-path module inferred-effects]
  (let [declared-effects (:effects module)
        widened (first (remove declared-effects inferred-effects))
        required-capabilities (required-capabilities-for-effects inferred-effects)
        missing-capability (first (remove (:capabilities module) required-capabilities))]
    (when widened
      (fail! "L3-EFFECT-WIDEN"
             "inferred namespace effects exceed declared effect allowance"
             {:source-span {:source source-path}
              :effect widened
              :declared-effects declared-effects
              :remediation "Declare the effect at namespace level or remove the effectful form."}))
    (when missing-capability
      (fail! "L3-CAPABILITY-MISSING"
             "namespace requires a capability not declared by the namespace"
             {:source-span {:source source-path}
              :required-capability missing-capability
              :declared-capabilities (:capabilities module)
              :remediation "Declare the required capability or remove the capability-using form."}))))

(definterposable parse-module :parse-module
  [source-path forms]
  (let [ns-form (require-ns source-path forms)
        module-name (second ns-form)
        clauses (map #(parse-clause source-path %) (drop 2 ns-form))
        clause-map (reduce (fn [acc [k v]] (update acc k (fnil conj []) v)) {} clauses)
        active-profile-values (get clause-map :profile)
        library-profile-values (get clause-map :profiles)
        profile (first (single-clause-value source-path clause-map :profile true))
        target (or (first (single-clause-value source-path clause-map :target false)) :jvm)
        effects (or (first (single-clause-value source-path clause-map :effects false)) #{})
        capabilities (or (first (single-clause-value source-path clause-map :capabilities false)) #{})
        requires (parse-dependencies source-path :require (clause-args clause-map :requires))
        imports (parse-dependencies source-path :import (clause-args clause-map :imports))
        exports (or (first (single-clause-value source-path clause-map :exports false)) [])
        safety (or (first (single-clause-value source-path clause-map :safety false)) :safe)
        providers (or (first (single-clause-value source-path clause-map :providers false)) [])
        metadata (or (first (single-clause-value source-path clause-map :metadata false)) {})
        docs (or (first (single-clause-value source-path clause-map :doc false)) nil)]
    (when (or (> (count active-profile-values) 1)
              (and (seq active-profile-values) (seq library-profile-values))
              (seq library-profile-values))
      (fail! "L3-PROFILE-MULTIPLE"
             "stage0 implementation namespaces must declare exactly one active profile"
             {:source-span (source-span source-path 0)
              :profile-clauses active-profile-values
              :profiles-clauses library-profile-values
              :remediation "Use one (:profile p) clause for stage0 implementation modules."}))
    (when-not (symbol? module-name)
      (fail! "L3-NS-MISSING"
             "namespace name must be a symbol"
             {:source-span (source-span source-path 0)
              :remediation "Use a symbolic namespace name, for example hello.main."}))
    (when-not (contains? (scalar-operation-value :known-source-profiles
                                                 known-source-profiles-default)
                         profile)
      (fail! "P1-PROFILE-UNSUPPORTED"
             "stage0 bootstrap does not know this source profile"
             {:source-span (source-span source-path 0)
              :profile profile
              :known (scalar-operation-value :known-source-profiles
                                             known-source-profiles-default)
              :supported (scalar-operation-value :supported-profiles
                                                 supported-profiles-default)
              :remediation "Use a known source profile such as :hosted, :core, or :kernel."}))
    (when-not (bootstrap-target-supported? target)
      (fail! "B1-TARGET-UNSUPPORTED"
             "stage0 bootstrap does not support this requested target"
             {:source-span (source-span source-path 0)
              :target target
              :supported (scalar-operation-value :supported-targets
                                                 supported-targets-default)
              :remediation "Use a target enabled by the selected bootstrap backend."}))
    {:module module-name
     :source-path source-path
     :profile profile
     :target target
     :effects effects
     :capabilities capabilities
     :requires requires
     :imports imports
     :exports exports
     :safety safety
     :providers providers
     :metadata metadata
     :doc docs
     :forms (vec (rest forms))}))

(definterposable validate-module-effects! :validate-module-effects!
  [module]
  (let [forms (:forms module)
        writes? (some uses-println? forms)]
    (when (and writes? (not (contains? (:effects module) :io/write)))
      (fail! "L6-EFFECT-UNDECLARED"
             "println requires the :io/write effect"
             {:source-span {:source (:source-path module)}
              :required-effect :io/write
              :declared-effects (:effects module)
              :remediation "Add :io/write to the namespace effects."}))
    (when (and writes? (not (contains? (:capabilities module) :io/stdout)))
      (fail! "L3-CAPABILITY-MISSING"
             "println requires the :io/stdout capability"
             {:source-span {:source (:source-path module)}
              :required-capability :io/stdout
              :declared-capabilities (:capabilities module)
              :remediation "Add :io/stdout to the namespace capabilities."}))))

(definterposable module-source-artifact-from-records
  :module-source-artifact-from-records
  [source-path source-text records]
  (let [forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        syntax (syntax-object-stream source-path records module)
        dependencies (vec (concat (:requires module) (:imports module)))
        _ (assert-unique-aliases! source-path dependencies)
        _ (assert-referred-names-unambiguous! source-path dependencies)
        _ (assert-profile-boundaries! source-path module dependencies)
        _ (assert-qualified-symbols-resolve! source-path (:forms module) module dependencies)
        inferred-effects (infer-effects (:forms module))
        _ (assert-namespace-effect-and-capability!
           source-path module inferred-effects)
        definitions (definition-table syntax module)
        source-hash (str "sha256:" (sha256-hex source-text))
        definitions-hash (str "sha256:" (sha256-hex (pr-str definitions)))
        dependency-records
        (mapv #(select-keys % [:kind :module :alias :profile :boundary
                               :effects :capabilities])
              dependencies)
        public-api (filterv #(= :public (:visibility %)) definitions)]
    {:kind :gravity/stage0-module-artifact
     :pass {:name :namespace-analyzer
            :input :syntax-object-stream
            :output :module-artifact
            :requires [:reader]
            :preserves [:source-spans :profile :target :effects :capabilities]
            :rejects ["L3-NS-MISSING" "L3-PROFILE-MULTIPLE" "L3-UNKNOWN-ALIAS"
                      "L3-AMBIGUOUS-NAME" "L3-PRIVATE-IMPORT" "L3-CROSS-PROFILE"
                      "L3-EFFECT-WIDEN" "L3-CAPABILITY-MISSING"]}
     :namespace-table [{:name (:module module)
                        :package (get-in module [:metadata :package])
                        :profile (:profile module)
                        :target (:target module)
                        :source-path source-path
                        :safety (:safety module)
                        :metadata (:metadata module)}]
     :alias-table (mapv (fn [dependency]
                          {:alias (:alias dependency)
                           :module (:module dependency)
                           :kind (:kind dependency)
                           :profile (:profile dependency)})
                        (filter :alias dependencies))
     :import-export-table {:requires (:requires module)
                           :imports (:imports module)
                           :exports (:exports module)}
     :module-dependency-graph {:module (:module module)
                               :dependencies dependency-records
                               :acyclic true}
     :namespace-effect-summary {:declared (:effects module)
                                :inferred inferred-effects}
     :namespace-capability-summary
     {:declared (:capabilities module)
      :required (required-capabilities-for-effects inferred-effects)}
     :profile-boundary-records
     (mapv (fn [dependency]
             {:module (:module dependency)
              :from-profile (:profile module)
              :to-profile (:profile dependency)
              :boundary (or (:boundary dependency)
                            (when (= :core (:profile dependency)) :pure-core))})
           (filter #(or (:boundary %)
                        (and (:profile %)
                             (not= (:profile %) (:profile module))))
                   dependencies))
     :module-artifact {:module (:module module)
                       :package (get-in module [:metadata :package])
                       :profile (:profile module)
                       :target (:target module)
                       :exports (:exports module)
                       :requires (mapv #(select-keys % [:module :profile :effects])
                                       (:requires module))
                       :imports (mapv #(select-keys % [:module :profile :effects
                                                       :boundary])
                                      (:imports module))
                       :effects (:effects module)
                       :capabilities (:capabilities module)
                       :safety (:safety module)
                       :source-hash source-hash
                       :definitions definitions-hash}
     :public-api-manifest {:module (:module module)
                           :exports public-api}
     :definitions definitions
     :syntax-object-stream syntax
     :diagnostics []}))

(defn with-operations
  [operations thunk]
  (when-not (map? operations)
    (throw (ex-info "Gravity module-analysis operations must be a map"
                    {:operations operations})))
  (let [unknown (vec (remove operation-keys (keys operations)))]
    (when (seq unknown)
      (throw (ex-info "Gravity module-analysis operations contain unknown keys"
                      {:unknown-keys unknown
                       :allowed-keys operation-keys}))))
  (doseq [key function-operation-keys
          :when (contains? operations key)]
    (when-not (fn? (get operations key))
      (throw (ex-info "Gravity module-analysis operation must be a function"
                      {:operation key :value (get operations key)}))))
  (doseq [[key value] [[:known-source-profiles
                        (get operations :known-source-profiles)]
                       [:supported-profiles
                        (get operations :supported-profiles)]
                       [:supported-targets
                        (get operations :supported-targets)]]
          :when (contains? operations key)]
    (when-not (valid-keyword-set? value)
      (throw (ex-info "Gravity module-analysis scalar operation must be a non-empty keyword set"
                      {:operation key :value value}))))
  (when (and (contains? operations :effect-capability)
             (not (valid-effect-capability?
                   (get operations :effect-capability))))
    (throw (ex-info "Gravity module-analysis effect-capability operation must map keywords to keywords"
                    {:operation :effect-capability
                     :value (get operations :effect-capability)})))
  (when (and (contains? operations :profile-direct-imports)
             (not (valid-profile-direct-imports?
                   (get operations :profile-direct-imports))))
    (throw (ex-info "Gravity module-analysis profile-direct-imports operation must map keywords to non-empty keyword sets"
                    {:operation :profile-direct-imports
                     :value (get operations :profile-direct-imports)})))
  (when-not (fn? thunk)
    (throw (ex-info "Gravity module-analysis thunk must be a function"
                    {:thunk thunk})))
  (binding [*operations* (merge *operations* operations)]
    (thunk)))

(defn call-entrypoint-body
  "Enter one extracted function body with one-shot operation bypass.

  The first invocation of the supplied operation runs its captured original
  body. A recursive call through the same public Var can then observe the
  injected operation without recursing indefinitely through that operation."
  [operation-key operation args]
  (when-not (contains? operation-keys operation-key)
    (throw (ex-info "Gravity module-analysis entrypoint key is unknown"
                    {:operation operation-key})))
  (when-not (fn? operation)
    (throw (ex-info "Gravity module-analysis entrypoint must be a function"
                    {:operation operation-key :value operation})))
  (when-not (sequential? args)
    (throw (ex-info "Gravity module-analysis entrypoint args must be sequential"
                    {:operation operation-key :args args})))
  (binding [*active-operation-keys*
            (disj *active-operation-keys* operation-key)
            *bypass-next-operation-keys*
            (conj *bypass-next-operation-keys* operation-key)]
    (apply operation args)))

(def public-api
  {'public-api {:kind :contract}
   'module-analysis-engine-contract {:arglists '([])}
   'with-operations {:arglists '([operations thunk])}
   'call-entrypoint-body {:arglists '([operation-key operation args])}
   'require-ns {:arglists '([source-path forms])}
   'parse-clause {:arglists '([source-path clause])}
   'single-clause-value {:arglists '([source-path clause-map key required?])}
   'clause-args {:arglists '([clause-map key])}
   'parse-options {:arglists '([source-path entry option-items])}
   'parse-dependency-entry {:arglists '([source-path kind entry])}
   'parse-dependencies {:arglists '([source-path kind entries])}
   'top-level-definition {:arglists '([syntax])}
   'definition-table {:arglists '([syntax module])}
   'collect-symbols {:arglists '([form])}
   'collect-code-symbols {:arglists '([form])}
   'infer-effects {:arglists '([forms])}
   'required-capabilities-for-effects {:arglists '([effects])}
   'profile-direct-import-allowed?
   {:arglists '([consumer-profile producer-profile])}
   'assert-unique-aliases! {:arglists '([source-path dependencies])}
   'assert-referred-names-unambiguous!
   {:arglists '([source-path dependencies])}
   'assert-qualified-symbols-resolve!
   {:arglists '([source-path forms module dependencies])}
   'assert-profile-boundaries!
   {:arglists '([source-path module dependencies])}
   'assert-namespace-effect-and-capability!
   {:arglists '([source-path module inferred-effects])}
   'parse-module {:arglists '([source-path forms])}
   'uses-println? {:arglists '([form])}
   'validate-module-effects! {:arglists '([module])}
   'module-source-artifact-from-records
   {:arglists '([source-path source-text records])}})

(defn module-analysis-engine-contract
  []
  (assoc namespace-contract :public-api public-api))
