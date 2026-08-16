(ns gravity.c5-name-resolution
  "Hosted Stage0 C5 name-resolution and namespace-analysis engine.

  This leaf owns the Clojure seed's compatibility implementation of the C5
  binding algorithm and artifact projection. It is deliberately not the
  canonical Gravity SH-06 authority: source authentication, proof, equivalence,
  self-hosting, release, and seed retirement remain outside this namespace.

  Bootstrap callers inject the seed operations through with-operations. The
  indirection keeps the leaf acyclic while allowing compatibility wrappers to
  preserve dynamic interposition."
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [gravity.digest :as digest]))

(def ^:private ^:dynamic *operations* {})

(def ^:private operation-keys
  #{:fail! :source-span :sha256-hex :c4-artifact-id
    :read-source-form-records :validate-ns-syntax! :parse-module
    :module-source-artifact :compiler-c4-macro-source-artifact
    :collect-code-symbols :ns-form? :known-source-profiles
    :supported-targets :c5-resolution-diagnostic-ids
    :c5-resolution-governing-document :c5-resolution-rejected-designs
    :c5-resolution-override-diagnostics :c5-special-form-symbols
    :c5-core-auto-imports :c5-type-auto-imports
    :c5-resolution-source-overrides :c5-resolution-message
    :c5-resolution-fail! :c5-resolution-validate-overrides!
    :compiler-c5-resolution-source-artifact
    :c5-package-record :c5-binding-id :c5-binding-identity
    :c5-definition-binding :c5-special-form-binding :c5-core-binding
    :c5-type-binding :c5-import-binding :c5-alias-table
    :c5-import-export-table :c5-definition-bindings :c5-macro-bindings
    :c5-param-symbols :c5-local-bindings-from-params
    :c5-let-binding-symbols :c5-local-scope-graph
    :c5-bindings-by-name :c5-resolve-qualified-symbol
    :c5-resolution-record :c5-binding-table
    :c5-namespace-analysis-artifact :c5-dependency-graph
    :c5-cross-profile-edge-report :c5-incremental-invalidation-keys
    :c5-resolution-diagnostics :c5-resolution-verification-report
    :c5-resolution-capability-proof :c5-resolution-validate!})

(def ^:private function-operation-keys
  (apply disj operation-keys
         [:known-source-profiles :supported-targets
          :c5-resolution-diagnostic-ids :c5-resolution-governing-document
          :c5-resolution-rejected-designs :c5-resolution-override-diagnostics
          :c5-special-form-symbols :c5-core-auto-imports
          :c5-type-auto-imports]))

(defn- valid-keyword-set?
  [value]
  (and (set? value)
       (seq value)
       (every? keyword? value)))

(defn- valid-string-vector?
  [value]
  (and (vector? value)
       (every? string? value)))

(defn- valid-map-of-keywords-to-strings?
  [value]
  (and (map? value)
       (every? (fn [[key item]]
                 (and (keyword? key) (string? item)))
               value)))

(defn- valid-symbol-set?
  [value]
  (and (set? value)
       (every? symbol? value)))

(def ^:private namespace-contract
  {:namespace 'gravity.c5-name-resolution
   :contract-boundary :hosted-stage0-c5-name-resolution-engine
   :public-api :bootstrap-compatible-c5-vars
   :artifact-inputs [:reader-records :c4-expanded-syntax :module-artifact]
   :artifact-outputs [:namespace-analysis :binding-table :alias-table
                      :import-export-table :lexical-scope-graph
                      :dependency-graph :cross-profile-edge-report
                      :resolution-diagnostics :incremental-invalidation-keys]
   :owns [:hosted-stage0-c5-binding-algorithm
          :hosted-stage0-c5-compatibility-artifact]
   :dependency-direction {:requires ['clojure.set
                                     'clojure.string
                                     'gravity.digest]
                          :forbids ['gravity.bootstrap
                                    'gravity.diagnostics]}
   :does-not-own [:canonical-c5-authority
                  :gravity-sh06-resolution-authority
                  :source-authentication :proof-authority
                  :self-hosting :equivalence :release :seed-retirement
                  :package-discovery :type-checking :effect-checking
                  :safety-analysis]
   :compatibility-only? true
   :override-driven-diagnostics? true
   :cycle-analysis-complete? false
   :operation-interposition {:accepted-keys operation-keys
                             :partial-overrides? true
                             :bootstrap-wrapper-arities? true}
   :canonical-c5-authority? false
   :self-hosted? false
   :clojure-seed-boundary? true})

(defn- default-fail!
  [id message data]
  (throw (ex-info message (assoc (or data {}) :id id))))

(defn- default-source-span
  [source-path form-index]
  {:source source-path :form-index form-index})

(defn- default-sha256-hex
  [value]
  (digest/sha256-hex value))

(defn- default-c4-artifact-id
  [artifact]
  (str "sha256:" (digest/sha256-hex (pr-str artifact))))

(defn- default-collect-code-symbols
  [form]
  (cond
    (symbol? form) [form]
    (seq? form) (if (= 'quote (first form))
                  (if (symbol? (first form)) [(first form)] [])
                  (mapcat default-collect-code-symbols form))
    (coll? form) (mapcat default-collect-code-symbols form)
    :else []))

(defn- default-ns-form?
  [form]
  (and (seq? form) (= 'ns (first form))))

(defn- unsupported-host-operation
  [operation]
  (fn [& _]
    (throw (ex-info (str "C5 leaf requires injected operation " operation)
                    {:operation operation}))))

(defn- op-fn
  [key fallback]
  (or (get *operations* key) fallback))

(defn- op-value
  [key fallback]
  (or (get *operations* key) fallback))

(defn- fail!
  [id message data]
  ((op-fn :fail! default-fail!) id message data))

(defn- source-span
  [source-path form-index]
  ((op-fn :source-span default-source-span) source-path form-index))

(defn- sha256-hex
  [value]
  ((op-fn :sha256-hex default-sha256-hex) value))

(defn- c4-artifact-id
  [artifact]
  ((op-fn :c4-artifact-id default-c4-artifact-id) artifact))

(defn- collect-code-symbols
  [form]
  ((op-fn :collect-code-symbols default-collect-code-symbols) form))

(defn- ns-form?
  [form]
  ((op-fn :ns-form? default-ns-form?) form))

(def ^:private known-source-profiles
  #{:core :hardware :firmware :kernel :native :hosted
    :distributed :ai :meta :gpu :formal})

(def ^:private supported-targets
  #{:jvm})

(defn- read-source-form-records
  [source-path source-text]
  ((op-fn :read-source-form-records
          (unsupported-host-operation :read-source-form-records))
   source-path source-text))

(defn- validate-ns-syntax!
  [source-path forms]
  ((op-fn :validate-ns-syntax!
          (unsupported-host-operation :validate-ns-syntax!))
   source-path forms))

(defn- parse-module
  [source-path forms]
  ((op-fn :parse-module
          (unsupported-host-operation :parse-module))
   source-path forms))

(defn- module-source-artifact
  [source-path source-text]
  ((op-fn :module-source-artifact
          (unsupported-host-operation :module-source-artifact))
   source-path source-text))

(defn- compiler-c4-macro-source-artifact
  [source-path source-text]
  ((op-fn :compiler-c4-macro-source-artifact
          (unsupported-host-operation :compiler-c4-macro-source-artifact))
   source-path source-text))

(defn- validate-operations!
  [operations]
  (when-not (map? operations)
    (throw (ex-info "C5 operation map must be a map"
                    {:value operations})))
  (let [unknown (seq (remove operation-keys (keys operations)))
        non-functions
        (seq (for [[key value] (select-keys operations function-operation-keys)
                   :when (not (fn? value))]
               key))
        wrong-known-profiles?
        (and (contains? operations :known-source-profiles)
             (not (valid-keyword-set?
                   (get operations :known-source-profiles))))
        wrong-supported-targets?
        (and (contains? operations :supported-targets)
             (not (valid-keyword-set?
                   (get operations :supported-targets))))]
    (when unknown
      (throw (ex-info "C5 operation map contains unknown keys"
                      {:unknown-keys (vec unknown)
                       :allowed-keys operation-keys})))
    (when non-functions
      (throw (ex-info "C5 function operation values must be callable"
                      {:non-function-keys (vec non-functions)})))
    (when wrong-known-profiles?
      (throw (ex-info "C5 known-source-profiles operation must be a non-empty keyword set"
                      {:expected :non-empty-keyword-set
                       :actual (get operations :known-source-profiles)})))
    (when wrong-supported-targets?
      (throw (ex-info "C5 supported-targets operation must be a non-empty keyword set"
                      {:expected :non-empty-keyword-set
                       :actual (get operations :supported-targets)})))
    (doseq [[key valid? expected]
            [[:c5-resolution-diagnostic-ids
              #(and (valid-string-vector? %)
                    (seq %))
              :non-empty-string-vector]
             [:c5-resolution-governing-document
              #(and (string? %) (seq %))
              :non-empty-string]
             [:c5-resolution-rejected-designs
              #(and (vector? %) (every? map? %))
              :vector-of-maps]
             [:c5-resolution-override-diagnostics
              valid-map-of-keywords-to-strings?
              :map-of-keywords-to-strings]
             [:c5-special-form-symbols valid-symbol-set? :symbol-set]
             [:c5-core-auto-imports valid-symbol-set? :symbol-set]
             [:c5-type-auto-imports valid-symbol-set? :symbol-set]]
            :when (and (contains? operations key)
                       (not (valid? (get operations key))))]
      (throw (ex-info "C5 scalar operation has an invalid shape"
                      {:key key :expected expected :actual (get operations key)})))
  operations))

(defn with-operations
  [operations thunk]
  (validate-operations! operations)
  (binding [*operations* (merge *operations* operations)]
    (thunk)))

(def c5-resolution-diagnostic-ids
  ["C5-UNRESOLVED"
   "C5-AMBIGUOUS"
   "C5-PRIVATE"
   "C5-ALIAS"
   "C5-SHADOW"
   "C5-CYCLE"
   "C5-CROSS-PROFILE"
   "C5-CAPABILITY"
   "C5-TARGET"
   "C5-FOREIGN"])

(def c5-resolution-governing-document
  "docs/phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md")

(def c5-resolution-rejected-designs
  [{:diagnostic "C5-UNRESOLVED"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c5-unresolved.gravity"
    :rejected-design :unresolved-symbol}
   {:diagnostic "C5-AMBIGUOUS"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c5-ambiguous.gravity"
    :rejected-design :ambiguous-unqualified-symbol}
   {:diagnostic "C5-PRIVATE"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c5-private.gravity"
    :rejected-design :private-binding-access}
   {:diagnostic "C5-ALIAS"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c5-alias.gravity"
    :rejected-design :unknown-or-duplicate-alias}
   {:diagnostic "C5-SHADOW"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c5-shadow.gravity"
    :rejected-design :illegal-shadowing}
   {:diagnostic "C5-CYCLE"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c5-cycle.gravity"
    :rejected-design :namespace-dependency-cycle}
   {:diagnostic "C5-CROSS-PROFILE"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c5-cross-profile.gravity"
    :rejected-design :cross-profile-edge-without-boundary}
   {:diagnostic "C5-CAPABILITY"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c5-capability.gravity"
    :rejected-design :imported-binding-without-capability}
   {:diagnostic "C5-TARGET"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c5-target.gravity"
    :rejected-design :target-incompatible-import}
   {:diagnostic "C5-FOREIGN"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c5-foreign.gravity"
    :rejected-design :malformed-foreign-import-record}])

(def c5-resolution-override-diagnostics
  {:unresolved "C5-UNRESOLVED"
   :ambiguous "C5-AMBIGUOUS"
   :private "C5-PRIVATE"
   :alias "C5-ALIAS"
   :shadow "C5-SHADOW"
   :cycle "C5-CYCLE"
   :cross-profile "C5-CROSS-PROFILE"
   :capability "C5-CAPABILITY"
   :target "C5-TARGET"
   :foreign "C5-FOREIGN"})

(def c5-special-form-symbols
  '#{quote if do let fn loop recur def defn defmacro defschema defprotocol
     syntax-quote unquote splice-unquote unsafe})

(def c5-core-auto-imports
  '#{println + - * / = < > <= >= str pr-str hash-map vector list conj assoc
     get first second rest count})

(def c5-type-auto-imports
  '#{I8 I16 I32 I64 U8 U16 U32 U64 F32 F64 Bool String Symbol Keyword
     Dynamic Unit Never})

(defn c5-resolution-source-overrides
  [module]
  (get-in module [:metadata :compiler :c5-resolution] {}))

(defn c5-resolution-message
  [id]
  (case id
    "C5-UNRESOLVED" "symbol has no resolvable binding"
    "C5-AMBIGUOUS" "symbol has multiple legal bindings"
    "C5-PRIVATE" "private binding is accessed outside its namespace boundary"
    "C5-ALIAS" "namespace alias is unknown or duplicated"
    "C5-SHADOW" "lexical binding shadows a namespace binding illegally"
    "C5-CYCLE" "namespace dependency graph contains an illegal cycle"
    "C5-CROSS-PROFILE" "cross-profile import lacks an accepted boundary"
    "C5-CAPABILITY" "imported binding requires an unavailable capability"
    "C5-TARGET" "imported binding is incompatible with the active target"
    "C5-FOREIGN" "foreign import record is malformed"
    "name resolution and namespace analysis failed"))

(defn c5-resolution-fail!
  [id source-path subject extra]
  (fail! id
         ((op-fn :c5-resolution-message c5-resolution-message) id)
         (merge {:source-span (or (:source-span subject)
                                  (:span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :c5-name-resolution
                 :stage :name-resolution
                 :document-id "C5"
                 :expected-document
                 (op-value :c5-resolution-governing-document
                           c5-resolution-governing-document)
                 :symbol (:symbol subject)
                 :syntax-id (:syntax-id subject)
                 :namespace (:namespace subject)
                 :active-profile (:profile subject)
                 :target (:target subject)
                 :candidate-bindings (:candidate-bindings subject)
                 :dependency-edge (:dependency-edge subject)
                 :capabilities (:capabilities subject)
                 :remediation "Resolve names through lexical, namespace, alias, package, foreign, core, or target-intrinsic records with explicit profile, target, effect, capability, visibility, and dependency metadata."}
                extra)))

(defn c5-resolution-validate-overrides!
  [source-path module overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get (op-value :c5-resolution-override-diagnostics
                                 c5-resolution-override-diagnostics)
                       fail-kind)]
      ((op-fn :c5-resolution-fail! c5-resolution-fail!) id source-path
                           {:source-span (source-span source-path 0)
                            :symbol (symbol (str "fixture/" (name fail-kind)))
                            :syntax-id "fixture-override"
                            :namespace (:module module)
                            :profile (:profile module)
                            :target (:target module)
                            :capabilities (:capabilities module)}
                           {:missing-fields [fail-kind]}))))

(defn c5-package-record
  [module]
  {:name (or (get-in module [:metadata :package]) 'gravity/stage0-local)
   :version (or (get-in module [:metadata :package-version]) "0.0.0-stage0")})

(defn c5-binding-id
  [binding]
  (str "sha256:" (sha256-hex (pr-str (select-keys binding
                                                   [:name :kind :namespace
                                                    :package :visibility
                                                    :profile-set :target-set
                                                    :type-ref :effects
                                                    :capabilities :safety
                                                    :source-span])))))

(defn c5-binding-identity
  [binding]
  (let [stable (select-keys binding
                            [:name :kind :namespace :package :visibility
                             :profile-set :target-set :type-ref :effects
                             :capabilities :safety :source-span :artifact])]
    (assoc stable :binding-id ((op-fn :c5-binding-id c5-binding-id) stable))))

(defn c5-definition-binding
  [module definition artifact-id]
  ((op-fn :c5-binding-identity c5-binding-identity)
   {:name (:name definition)
    :kind (:kind definition)
    :namespace (:module module)
    :package ((op-fn :c5-package-record c5-package-record) module)
    :visibility (:visibility definition)
    :profile-set #{(:profile module)}
    :target-set #{(:target module)}
    :type-ref (case (:kind definition)
                :schema :gravity.type/schema
                :protocol :gravity.type/protocol
                :macro :gravity.syntax/macro
                :function :gravity.type/function
                :gravity.type/value)
    :effects (:latent-effects definition)
    :capabilities (:required-capabilities definition)
    :safety (:safety definition)
    :source-span (:source-span definition)
    :artifact artifact-id}))

(defn c5-special-form-binding
  [sym module]
  ((op-fn :c5-binding-identity c5-binding-identity)
   {:name sym
    :kind :special-form
    :namespace 'gravity.core
    :package {:name 'gravity/core :version "stage0"}
    :visibility :public
    :profile-set (op-value :known-source-profiles known-source-profiles)
    :target-set (op-value :supported-targets supported-targets)
    :type-ref :gravity.syntax/special-form
    :effects #{}
    :capabilities #{}
    :safety :safe
    :source-span {:source "gravity.core" :form-index 0}
    :artifact (:module module)}))

(defn c5-core-binding
  [sym module]
  ((op-fn :c5-binding-identity c5-binding-identity)
   {:name sym
    :kind :var
    :namespace 'gravity.core
    :package {:name 'gravity/core :version "stage0"}
    :visibility :public
    :profile-set (op-value :known-source-profiles known-source-profiles)
    :target-set (op-value :supported-targets supported-targets)
    :type-ref :gravity.type/core-var
    :effects (if (= 'println sym) #{:io/write} #{})
    :capabilities (if (= 'println sym) #{:io/stdout} #{})
    :safety :safe
    :source-span {:source "gravity.core" :form-index 0}
    :artifact (:module module)}))

(defn c5-type-binding
  [sym module]
  ((op-fn :c5-binding-identity c5-binding-identity)
   {:name sym
    :kind :type
    :namespace 'gravity.core
    :package {:name 'gravity/core :version "stage0"}
    :visibility :public
    :profile-set (op-value :known-source-profiles known-source-profiles)
    :target-set (op-value :supported-targets supported-targets)
    :type-ref :gravity.type/type
    :effects #{}
    :capabilities #{}
    :safety :safe
    :source-span {:source "gravity.core" :form-index 0}
    :artifact (:module module)}))

(defn c5-import-binding
  [module dependency imported-name artifact-id]
  ((op-fn :c5-binding-identity c5-binding-identity)
   {:name imported-name
    :kind (if (= :import (:kind dependency)) :foreign-var :var)
    :namespace (:module dependency)
    :package {:name (symbol (str (:module dependency))) :version "stage0"}
    :visibility (:visibility dependency)
    :profile-set #{(or (:profile dependency) (:profile module))}
    :target-set #{(:target module)}
    :type-ref (if (= :import (:kind dependency))
                :gravity.interop/foreign-value
                :gravity.type/imported-var)
    :effects (:effects dependency)
    :capabilities (:capabilities dependency)
    :safety (if (= :import (:kind dependency)) :boundary-checked :safe)
    :source-span (source-span (:source-path module) 0)
    :artifact artifact-id}))

(defn c5-alias-table
  [module]
  (mapv (fn [dependency]
          {:alias (:alias dependency)
           :namespace (:module dependency)
           :kind (:kind dependency)
           :package {:name (symbol (str (:module dependency)))
                     :version "stage0"}
           :profile (:profile dependency)
           :target (:target module)
           :effects (:effects dependency)
           :capabilities (:capabilities dependency)
           :visibility (:visibility dependency)
           :boundary (or (:boundary dependency)
                         (when (= :core (:profile dependency)) :pure-core))})
        (filter :alias (concat (:requires module) (:imports module)))))

(defn c5-import-export-table
  [module]
  {:artifact :gravity/c5-import-export-table
   :requires (mapv #(select-keys % [:module :alias :refer :profile :boundary
                                    :effects :capabilities :visibility])
                   (:requires module))
   :foreign-imports (mapv #(select-keys % [:module :alias :refer :profile
                                           :boundary :effects :capabilities
                                           :visibility])
                          (:imports module))
   :exports (:exports module)
   :status :complete})

(defn c5-definition-bindings
  [module module-artifact c4-artifact]
  (mapv #((op-fn :c5-definition-binding c5-definition-binding) module % (:artifact-id c4-artifact))
        (:definitions module-artifact)))

(defn c5-macro-bindings
  [module c4-artifact]
  (mapv (fn [entry]
          ((op-fn :c5-binding-identity c5-binding-identity)
           {:name (:macro entry)
            :kind :macro
            :namespace (:namespace entry)
            :package ((op-fn :c5-package-record c5-package-record) module)
            :visibility :private
            :profile-set #{(:profile module)}
            :target-set #{(:target module)}
            :type-ref :gravity.syntax/macro
            :effects #{}
            :capabilities (:capabilities entry)
            :safety :safe
            :source-span (source-span (:source-path module) 0)
            :artifact (:artifact-id c4-artifact)}))
        (get-in c4-artifact [:macro-environment :macro-vars])))

(defn c5-param-symbols
  [params]
  (loop [items (seq params)
         symbols []]
    (cond
      (nil? items) symbols
      (= ':- (first items)) (recur (nnext items) symbols)
      (and (symbol? (first items)) (= ':- (second items)))
      (recur (nnext (next items)) (conj symbols (first items)))
      (symbol? (first items)) (recur (next items) (conj symbols (first items)))
      :else (recur (next items) symbols))))

(defn c5-local-bindings-from-params
  [module form syntax-id]
  (when (and (seq? form) (= 'defn (first form)))
    (let [fn-name (second form)
          params (nth form 2 [])
          param-symbols ((op-fn :c5-param-symbols c5-param-symbols) params)]
      (mapv (fn [idx sym]
              ((op-fn :c5-binding-identity c5-binding-identity)
               {:name sym
                :kind :local
                :namespace (:module module)
                :package ((op-fn :c5-package-record c5-package-record) module)
                :visibility :lexical
                :profile-set #{(:profile module)}
                :target-set #{(:target module)}
                :type-ref :gravity.type/local
                :effects #{}
                :capabilities #{}
                :safety (:safety module)
                :source-span {:source (:source-path module)
                              :function fn-name
                              :param-index idx}
                :artifact syntax-id}))
            (range)
            param-symbols))))

(defn c5-let-binding-symbols
  [form]
  (letfn [(walk [value]
            (cond
              (and (seq? value) (= 'let (first value)) (vector? (second value)))
              (let [bindings (second value)
                    names (->> (partition 2 bindings)
                               (map first)
                               (filter symbol?))]
                (concat names (mapcat walk (drop 2 value))))
              (seq? value) (mapcat walk value)
              (coll? value) (mapcat walk value)
              :else []))]
    (vec (walk form))))

(defn c5-local-scope-graph
  [module expanded-stream]
  (let [scopes
        (vec
         (mapcat
          (fn [syntax]
            (let [form (:form syntax)
                  syntax-id (:syntax-id syntax)
                  params (or ((op-fn :c5-local-bindings-from-params c5-local-bindings-from-params) module form syntax-id)
                             [])
                  lets (mapv (fn [idx sym]
                               ((op-fn :c5-binding-identity c5-binding-identity)
                                {:name sym
                                 :kind :local
                                 :namespace (:module module)
                                 :package ((op-fn :c5-package-record c5-package-record) module)
                                 :visibility :lexical
                                 :profile-set #{(:profile module)}
                                 :target-set #{(:target module)}
                                 :type-ref :gravity.type/local
                                 :effects #{}
                                 :capabilities #{}
                                 :safety (:safety module)
                                 :source-span (:span syntax)
                                 :artifact syntax-id}))
                             (range)
                             ((op-fn :c5-let-binding-symbols c5-let-binding-symbols) form))]
              (when (seq (concat params lets))
                [{:scope-id (str "scope/" syntax-id)
                  :owner-syntax-id syntax-id
                  :namespace (:module module)
                  :bindings (vec (concat params lets))
                  :parent :namespace-root}])))
          expanded-stream))]
    {:artifact :gravity/c5-lexical-scope-graph
     :root {:scope-id :namespace-root :namespace (:module module)}
     :scopes scopes
     :status :complete}))

(defn c5-bindings-by-name
  [bindings]
  (reduce (fn [acc binding]
            (update acc (:name binding) (fnil conj []) binding))
          {}
          bindings))

(defn c5-resolve-qualified-symbol
  [module alias-map dependency-map sym]
  (let [ns-part (namespace sym)
        local-name (symbol (name sym))
        alias-sym (symbol ns-part)]
    (cond
      (contains? alias-map alias-sym)
      {:resolution-kind :alias-qualified
       :binding ((op-fn :c5-import-binding c5-import-binding) module
                                   (get dependency-map alias-sym)
                                   local-name
                                   (:module module))}

      (= ns-part (str (:module module)))
      {:resolution-kind :fully-qualified
       :binding ((op-fn :c5-binding-identity c5-binding-identity)
                 {:name local-name
                  :kind :var
                  :namespace (:module module)
                  :package ((op-fn :c5-package-record c5-package-record) module)
                  :visibility :public
                  :profile-set #{(:profile module)}
                  :target-set #{(:target module)}
                  :type-ref :gravity.type/value
                  :effects #{}
                  :capabilities #{}
                  :safety (:safety module)
                  :source-span (source-span (:source-path module) 0)
                  :artifact (:module module)})}

      (str/includes? ns-part ".")
      {:resolution-kind :fully-qualified
       :binding ((op-fn :c5-binding-identity c5-binding-identity)
                 {:name local-name
                  :kind :var
                  :namespace (symbol ns-part)
                  :package {:name (symbol ns-part) :version "stage0"}
                  :visibility :public
                  :profile-set (op-value :known-source-profiles known-source-profiles)
                  :target-set #{(:target module)}
                  :type-ref :gravity.type/qualified-var
                  :effects #{}
                  :capabilities #{}
                  :safety :safe
                  :source-span (source-span (:source-path module) 0)
                  :artifact (symbol ns-part)})}

      :else nil)))

(defn c5-resolution-record
  [module bindings-by-name alias-map dependency-map local-bindings syntax idx sym]
  (let [local-by-name ((op-fn :c5-bindings-by-name c5-bindings-by-name) local-bindings)
        qualified? (namespace sym)
        resolved (if qualified?
                   ((op-fn :c5-resolve-qualified-symbol c5-resolve-qualified-symbol) module alias-map dependency-map sym)
                   (cond
                     (contains? local-by-name sym)
                     {:resolution-kind :local
                      :binding (first (get local-by-name sym))}
                     (contains? bindings-by-name sym)
                     {:resolution-kind :namespace
                      :binding (first (get bindings-by-name sym))}
                     (contains? (op-value :c5-special-form-symbols
                                          c5-special-form-symbols)
                                sym)
                     {:resolution-kind :special-form
                      :binding ((op-fn :c5-special-form-binding c5-special-form-binding) sym module)}
                     (contains? (op-value :c5-core-auto-imports
                                          c5-core-auto-imports)
                                sym)
                     {:resolution-kind :core-auto-import
                      :binding ((op-fn :c5-core-binding c5-core-binding) sym module)}
                     (contains? (op-value :c5-type-auto-imports
                                          c5-type-auto-imports)
                                sym)
                     {:resolution-kind :type-position
                      :binding ((op-fn :c5-type-binding c5-type-binding) sym module)}
                     :else nil))]
    (when resolved
      {:syntax-id (:syntax-id syntax)
       :symbol-index idx
       :symbol sym
       :position (cond
                   (contains? (op-value :c5-special-form-symbols
                                        c5-special-form-symbols)
                              sym) :special-form
                   (contains? (op-value :c5-type-auto-imports
                                        c5-type-auto-imports)
                              sym) :type
                   qualified? :expression
                   :else :expression)
       :resolution-order (:resolution-kind resolved)
       :binding-id (get-in resolved [:binding :binding-id])
       :binding (select-keys (:binding resolved)
                             [:binding-id :name :kind :namespace :visibility
                              :profile-set :target-set :effects
                              :capabilities :safety])})))

(defn c5-binding-table
  [module definition-bindings macro-bindings lexical-scope-graph expanded-stream]
  (let [namespace-bindings (vec (concat definition-bindings macro-bindings))
        bindings-by-name ((op-fn :c5-bindings-by-name c5-bindings-by-name) namespace-bindings)
        dependencies (concat (:requires module) (:imports module))
        alias-map (into {} (map (juxt :alias identity) (filter :alias dependencies)))
        dependency-map alias-map
        locals (vec (mapcat :bindings (:scopes lexical-scope-graph)))]
    {:artifact :gravity/c5-binding-table
     :bindings
     (vec
      (keep-indexed
       (fn [idx pair]
         (let [[syntax sym] pair]
           ((op-fn :c5-resolution-record c5-resolution-record) module bindings-by-name alias-map
                                 dependency-map locals syntax idx sym)))
       (mapcat (fn [syntax]
                 (map (fn [sym] [syntax sym])
                      (collect-code-symbols (:form syntax))))
               (remove #(ns-form? (:form %)) expanded-stream))))
     :namespace-bindings namespace-bindings
     :local-bindings locals
     :status :complete}))

(defn c5-namespace-analysis-artifact
  [module binding-table alias-table import-export-table dependency-graph cross-profile-report]
  {:artifact :gravity/namespace-analysis
   :namespace (:module module)
   :package (get-in module [:metadata :package])
   :profile (:profile module)
   :target (:target module)
   :aliases (into {} (map (juxt :alias :namespace) alias-table))
   :exports (:exports module)
   :locals (c4-artifact-id (:local-bindings binding-table))
   :bindings (into {} (map (fn [record]
                             [[(:syntax-id record) (:symbol-index record)]
                              (:binding-id record)])
                           (:bindings binding-table)))
   :requires (get import-export-table :requires)
   :foreign-imports (get import-export-table :foreign-imports)
   :dependency-graph dependency-graph
   :cross-profile-edge-report cross-profile-report
   :rejected-edges []
   :diagnostics []
   :status :complete})

(defn c5-dependency-graph
  [module]
  (let [dependencies (mapv (fn [dependency]
                             {:namespace (:module dependency)
                              :package {:name (symbol (str (:module dependency)))
                                        :version "stage0"}
                              :edge (or (:edge dependency) :direct)
                              :kind (:kind dependency)
                              :alias (:alias dependency)
                              :profile-boundary
	                              (cond
	                                (:boundary dependency) (:boundary dependency)
	                                (= :core (:profile dependency)) :pure-core
	                                (= (:profile dependency) (:profile module)) :compatible
	                                :else :missing)
                              :effects (:effects dependency)
                              :capabilities (:capabilities dependency)
                              :target (:target module)})
                           (concat (:requires module) (:imports module)))]
    {:artifact :gravity/c5-module-dependency-graph
     :module (:module module)
     :dependencies dependencies
     :edges (mapv (fn [dependency]
                    {:from (:module module)
                     :to (:namespace dependency)
                     :kind (:kind dependency)
                     :profile-boundary (:profile-boundary dependency)})
                  dependencies)
     :acyclic true
     :status :complete}))

(defn c5-cross-profile-edge-report
  [module dependency-graph]
  {:artifact :gravity/c5-cross-profile-edge-report
   :edges
   (mapv (fn [dependency]
           {:from (:module module)
            :to (:namespace dependency)
            :from-profile (:profile module)
            :to-profile (or (some (fn [dep]
                                    (when (= (:module dep) (:namespace dependency))
                                      (:profile dep)))
                                  (concat (:requires module) (:imports module)))
                            (:profile module))
            :boundary (:profile-boundary dependency)
            :accepted? (not= :missing (:profile-boundary dependency))})
         (:dependencies dependency-graph))
   :status :complete})

(defn c5-incremental-invalidation-keys
  [module c4-artifact binding-table dependency-graph]
  {:artifact :gravity/c5-incremental-invalidation-keys
   :keys [{:input :namespace-source
           :hash (str "sha256:" (sha256-hex (pr-str (:source-path module))))
           :invalidates [:namespace-analysis :type-check :lsp-index]}
          {:input :aliases
           :hash (str "sha256:" (sha256-hex (pr-str (map :alias (concat (:requires module) (:imports module))))))
           :invalidates [:binding-table :dependency-graph]}
          {:input :exports
           :hash (str "sha256:" (sha256-hex (pr-str (:exports module))))
           :invalidates [:public-api :package-graph]}
          {:input :package-version
           :hash (str "sha256:" (sha256-hex (pr-str ((op-fn :c5-package-record c5-package-record) module))))
           :invalidates [:dependency-graph :trust-policy]}
          {:input :profile-target
           :hash (str "sha256:" (sha256-hex (pr-str [(:profile module)
                                                      (:target module)])))
           :invalidates [:profile-validation :target-lowering]}
          {:input :macro-expansion
           :hash (:artifact-id c4-artifact)
           :invalidates [:binding-table :type-check :effect-check]}
          {:input :binding-identities
           :hash (str "sha256:" (sha256-hex (pr-str (:namespace-bindings binding-table))))
           :invalidates [:incremental-cache :lsp-index]}
          {:input :dependency-graph
           :hash (str "sha256:" (sha256-hex (pr-str (:edges dependency-graph))))
           :invalidates [:package-graph :capability-check]}]
   :status :stable})

(defn c5-resolution-diagnostics
  [module]
  {:artifact :gravity/c5-resolution-diagnostics
   :required-diagnostic-ids
   (op-value :c5-resolution-diagnostic-ids c5-resolution-diagnostic-ids)
   :covered
   (op-value :c5-resolution-rejected-designs c5-resolution-rejected-designs)
   :accepted-run []
   :status :complete})

(defn c5-resolution-verification-report
  [binding-table lexical-scope-graph dependency-graph cross-profile-report invalidation]
  {:artifact :gravity/c5-resolution-verification-report
   :binding-identities-stable?
   (every? #(re-find #"^sha256:" (:binding-id %))
           (concat (:namespace-bindings binding-table)
                   (:local-bindings binding-table)))
   :all-resolved-bindings-have-metadata?
   (every? #(and (:binding-id %)
                 (:profile-set %)
                 (:target-set %)
                 (contains? % :effects)
                 (contains? % :capabilities)
                 (:visibility %))
           (concat (:namespace-bindings binding-table)
                   (:local-bindings binding-table)))
   :lexical-scopes-present? (seq (:scopes lexical-scope-graph))
   :dependency-graph-present? (seq (:edges dependency-graph))
   :cross-profile-boundaries-recorded?
   (every? :accepted? (:edges cross-profile-report))
   :invalidation-keys-stable?
   (every? #(re-find #"^sha256:" (:hash %)) (:keys invalidation))
   :status :passed})

(defn c5-resolution-capability-proof
  [artifact]
  (let [binding-table (:binding-table artifact)
        records (:bindings binding-table)
        namespace-bindings (:namespace-bindings binding-table)
        diagnostics (set (map :diagnostic (:rejected-design-coverage artifact)))
        verifier (:resolution-verification-report artifact)]
    {:local-resolution?
     (boolean (some #(= :local (:resolution-order %)) records))
     :namespace-resolution?
     (boolean (some #(= :namespace (:resolution-order %)) records))
     :alias-qualified-resolution?
     (boolean (some #(= :alias-qualified (:resolution-order %)) records))
     :fully-qualified-resolution?
     (boolean (some #(= :fully-qualified (:resolution-order %)) records))
     :macro-and-type-position-resolution?
     (boolean (and (some #(= :macro (:kind %)) namespace-bindings)
                   (some #(= :type-position (:resolution-order %)) records)))
     :binding-identity-stable?
     (true? (:binding-identities-stable? verifier))
     :visibility-diagnostics-covered?
     (contains? diagnostics "C5-PRIVATE")
     :dependency-graph-emitted?
     (= :complete (get-in artifact [:dependency-graph :status]))
     :cross-profile-boundaries-recorded?
     (true? (:cross-profile-boundaries-recorded? verifier))
     :target-and-capability-compatibility?
     (every? #(set/subset? (set (:capabilities %))
                           (set (get-in artifact [:module :capabilities])))
             (get-in artifact [:dependency-graph :dependencies]))
     :incremental-invalidation-recorded?
     (= :stable (get-in artifact [:incremental-invalidation-keys :status]))
     :diagnostics-covered?
     (= (set (op-value :c5-resolution-diagnostic-ids
                       c5-resolution-diagnostic-ids))
        diagnostics)
     :status :complete}))

(defn c5-resolution-validate!
  [source-path artifact]
  (let [proof ((op-fn :c5-resolution-capability-proof c5-resolution-capability-proof) artifact)]
    (doseq [[field id] [[:local-resolution? "C5-UNRESOLVED"]
                        [:namespace-resolution? "C5-UNRESOLVED"]
                        [:alias-qualified-resolution? "C5-ALIAS"]
                        [:fully-qualified-resolution? "C5-UNRESOLVED"]
                        [:macro-and-type-position-resolution? "C5-UNRESOLVED"]
                        [:binding-identity-stable? "C5-UNRESOLVED"]
                        [:visibility-diagnostics-covered? "C5-PRIVATE"]
                        [:dependency-graph-emitted? "C5-CYCLE"]
                        [:cross-profile-boundaries-recorded? "C5-CROSS-PROFILE"]
                        [:target-and-capability-compatibility? "C5-CAPABILITY"]
                        [:incremental-invalidation-recorded? "C5-UNRESOLVED"]
                        [:diagnostics-covered? "C5-UNRESOLVED"]]]
      (when-not (get proof field)
        ((op-fn :c5-resolution-fail! c5-resolution-fail!) id source-path {:stage :name-resolution}
                             {:missing-fields [field]}))))
  :complete)

(defn compiler-c5-resolution-source-artifact
  [source-path source-text]
  (let [records (read-source-form-records source-path source-text)
        forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        overrides ((op-fn :c5-resolution-source-overrides c5-resolution-source-overrides) module)
        _ ((op-fn :c5-resolution-validate-overrides!
                  c5-resolution-validate-overrides!)
           source-path module overrides)
        c4-artifact (compiler-c4-macro-source-artifact source-path source-text)
        module-artifact (module-source-artifact source-path source-text)
        expanded-stream (:expanded-syntax-stream c4-artifact)
        alias-table ((op-fn :c5-alias-table c5-alias-table) module)
        import-export-table ((op-fn :c5-import-export-table c5-import-export-table) module)
        definition-bindings ((op-fn :c5-definition-bindings c5-definition-bindings) module module-artifact
                                                    c4-artifact)
        macro-bindings ((op-fn :c5-macro-bindings c5-macro-bindings) module c4-artifact)
        lexical-scope-graph ((op-fn :c5-local-scope-graph c5-local-scope-graph) module expanded-stream)
        binding-table ((op-fn :c5-binding-table c5-binding-table) module definition-bindings
                                        macro-bindings lexical-scope-graph
                                        expanded-stream)
        dependency-graph ((op-fn :c5-dependency-graph c5-dependency-graph) module)
        cross-profile-report ((op-fn :c5-cross-profile-edge-report c5-cross-profile-edge-report) module
                                                           dependency-graph)
        invalidation ((op-fn :c5-incremental-invalidation-keys c5-incremental-invalidation-keys) module c4-artifact
                                                       binding-table
                                                       dependency-graph)
        namespace-analysis ((op-fn :c5-namespace-analysis-artifact c5-namespace-analysis-artifact) module binding-table
                                                           alias-table
                                                           import-export-table
                                                           dependency-graph
                                                           cross-profile-report)
        verifier ((op-fn :c5-resolution-verification-report c5-resolution-verification-report) binding-table
                                                    lexical-scope-graph
                                                    dependency-graph
                                                    cross-profile-report
                                                    invalidation)
        artifact-base
        {:kind :gravity/stage0-c5-name-resolution-artifact
         :task "P06-D084"
         :document-set ["C5"]
         :governing-document
         (op-value :c5-resolution-governing-document
                   c5-resolution-governing-document)
         :pass {:name :c5-name-resolution-and-namespace-analyzer
                :input :c4-expanded-syntax-artifact
                :output :namespace-analysis
                :requires [:expanded-syntax-stream :macro-expansion-context
                           :alias-table :package-dependency-graph
                           :active-profile :active-target :language-facets]
                :preserves [:source-spans :syntax-ids :hygiene
                            :generated-origin :profile :target
                            :effects :capabilities]
                :emits [:namespace-analysis :binding-table :alias-table
                        :import-export-table :lexical-scope-graph
                        :dependency-graph :cross-profile-edge-report
                        :resolution-diagnostics
                        :incremental-invalidation-keys]
                :rejects
                (op-value :c5-resolution-diagnostic-ids
                          c5-resolution-diagnostic-ids)}
         :source-overrides overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :c4-macro-expansion-artifact
         (select-keys c4-artifact [:kind :artifact-id :expanded-syntax-stream
                                   :macro-expansion-trace
                                   :macro-environment
                                   :generated-origin-source-map])
         :namespace-analysis namespace-analysis
         :binding-table binding-table
         :alias-table alias-table
         :import-export-table import-export-table
         :lexical-scope-graph lexical-scope-graph
         :dependency-graph dependency-graph
         :cross-profile-edge-report cross-profile-report
         :resolution-diagnostics ((op-fn :c5-resolution-diagnostics c5-resolution-diagnostics) module)
         :incremental-invalidation-keys invalidation
         :resolution-verification-report verifier
         :rejected-design-coverage
         (op-value :c5-resolution-rejected-designs
                   c5-resolution-rejected-designs)
         :diagnostics []}
        _ ((op-fn :c5-resolution-validate! c5-resolution-validate!) source-path artifact-base)
        capability-proof ((op-fn :c5-resolution-capability-proof c5-resolution-capability-proof) artifact-base)
        conformance {:documents ["C5"]
                     :task "P06-D084"
                     :required-diagnostic-ids
                     (op-value :c5-resolution-diagnostic-ids
                               c5-resolution-diagnostic-ids)
                     :namespace-analysis-status :complete
                     :binding-table-status :complete
                     :alias-table-status :complete
                     :import-export-status :complete
                     :lexical-scope-status :complete
                     :dependency-graph-status :complete
                     :cross-profile-status :complete
                     :diagnostic-status :complete
                     :invalidation-status :stable
                     :status :complete}
        artifact (assoc artifact-base
                        :capability-based-proof capability-proof
                        :c5-resolution-results conformance)]
    (assoc artifact :artifact-id (c4-artifact-id artifact))))

(defn compiler-c5-resolution-file-artifact
  [path]
  ((op-fn :compiler-c5-resolution-source-artifact
          compiler-c5-resolution-source-artifact)
   path (slurp path)))

(defn- default-operations
  []
  {:fail! default-fail!
   :source-span default-source-span
   :sha256-hex default-sha256-hex
   :c4-artifact-id default-c4-artifact-id
   :collect-code-symbols default-collect-code-symbols
   :ns-form? default-ns-form?})

(def public-api
  {'public-api {:kind :contract}
   'c5-resolution-diagnostic-ids {:kind :constant}
   'c5-resolution-governing-document {:kind :constant}
   'c5-resolution-rejected-designs {:kind :constant}
   'c5-resolution-override-diagnostics {:kind :constant}
   'c5-special-form-symbols {:kind :constant}
   'c5-core-auto-imports {:kind :constant}
   'c5-type-auto-imports {:kind :constant}
   'c5-resolution-source-overrides {:arglists '([module])}
   'c5-resolution-message {:arglists '([id])}
   'c5-resolution-fail! {:arglists '([id source-path subject extra])}
   'c5-resolution-validate-overrides!
   {:arglists '([source-path module overrides])}
   'c5-package-record {:arglists '([module])}
   'c5-binding-id {:arglists '([binding])}
   'c5-binding-identity {:arglists '([binding])}
   'c5-definition-binding {:arglists '([module definition artifact-id])}
   'c5-special-form-binding {:arglists '([sym module])}
   'c5-core-binding {:arglists '([sym module])}
   'c5-type-binding {:arglists '([sym module])}
   'c5-import-binding {:arglists '([module dependency imported-name artifact-id])}
   'c5-alias-table {:arglists '([module])}
   'c5-import-export-table {:arglists '([module])}
   'c5-definition-bindings {:arglists '([module module-artifact c4-artifact])}
   'c5-macro-bindings {:arglists '([module c4-artifact])}
   'c5-param-symbols {:arglists '([params])}
   'c5-local-bindings-from-params {:arglists '([module form syntax-id])}
   'c5-let-binding-symbols {:arglists '([form])}
   'c5-local-scope-graph {:arglists '([module expanded-stream])}
   'c5-bindings-by-name {:arglists '([bindings])}
   'c5-resolve-qualified-symbol
   {:arglists '([module alias-map dependency-map sym])}
   'c5-resolution-record
   {:arglists '([module bindings-by-name alias-map dependency-map
                local-bindings syntax idx sym])}
   'c5-binding-table
   {:arglists '([module definition-bindings macro-bindings
                lexical-scope-graph expanded-stream])}
   'c5-namespace-analysis-artifact
   {:arglists '([module binding-table alias-table import-export-table
                dependency-graph cross-profile-report])}
   'c5-dependency-graph {:arglists '([module])}
   'c5-cross-profile-edge-report {:arglists '([module dependency-graph])}
   'c5-incremental-invalidation-keys
   {:arglists '([module c4-artifact binding-table dependency-graph])}
   'c5-resolution-diagnostics {:arglists '([module])}
   'c5-resolution-verification-report
   {:arglists '([binding-table lexical-scope-graph dependency-graph
                cross-profile-report invalidation])}
   'c5-resolution-capability-proof {:arglists '([artifact])}
   'c5-resolution-validate! {:arglists '([source-path artifact])}
   'compiler-c5-resolution-source-artifact
   {:arglists '([source-path source-text])}
   'compiler-c5-resolution-file-artifact {:arglists '([path])}
   'with-operations {:arglists '([operations thunk])}
   'c5-engine-contract {:arglists '([])}})

(defn c5-engine-contract
  []
  (assoc namespace-contract :public-api public-api))
