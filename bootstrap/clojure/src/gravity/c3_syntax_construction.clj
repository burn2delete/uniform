(ns gravity.c3-syntax-construction
  "Hosted Stage0 C3 syntax identity and object construction.

  Inputs to this leaf are already authenticated hosted C2 products and syntax
  seeds. The leaf preserves the Clojure seed's path-neutral identity and object
  projection. It does not authenticate reader products or establish canonical
  SH04/C3, proof, self-hosting, or release authority."
  (:require [gravity.digest :as digest]
            [gravity.syntax-origin :as syntax-origin]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})

(def ^:private function-operation-keys
  #{:c2-path-neutral-span
    :sha256-hex
    :c3-origin-chain
    :c3-source-form-kind
    :c3-source-facts
    :c3-path-neutral-origin
    :c3-identity-input
    :c3-stable-syntax-id
    :c3-syntax-object
    :c3-generated-syntax-object})

(def ^:private namespace-contract
  {:namespace 'gravity.c3-syntax-construction
   :contract-boundary :hosted-c3-syntax-identity-and-object-construction
   :public-api
   {'with-operations {:arglists '([operations thunk])}
    'c3-path-neutral-origin {:arglists '([origin])}
    'c3-identity-input
    {:arglists '([seed origin namespace-context hygiene-context source-form-kind])}
    'c3-stable-syntax-id {:arglists '([identity-input])}
    'c3-syntax-object
    {:arglists '([seed form-record token-record source-unit c2-artifact
                  integrity-report])}
    'c3-generated-syntax-object {:arglists '([base-object])}}
   :operation-interposition
   {:accepted-keys function-operation-keys
    :partial-overrides? true
    :unknown-keys-rejected? true
    :function-values-must-satisfy :fn?}
   :artifact-inputs [:authenticated-hosted-c2-reader-product
                     :hosted-c3-syntax-seed]
   :artifact-outputs [:hosted-c3-syntax-object
                      :hosted-c3-generated-syntax-object]
   :ownership
   {:owns [:hosted-c3-path-neutral-identity-projection
           :hosted-c3-syntax-object-construction]
    :does-not-own [:canonical-c3-syntax-object-authority
                   :c2-reader-product-authentication
                   :literal-decoding
                   :syntax-stream-validation
                   :hygiene-verification
                   :macro-expansion
                   :diagnostic-policy
                   :proof-authority
                   :self-hosted-authority
                   :release-authority]}
   :dependency-direction
   {:requires ['clojure.core 'gravity.digest 'gravity.syntax-origin]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-c3-authority? false
   :self-hosted? false
   :release-authority? false})

(defn- unsupported
  [key]
  (fn [& _]
    (throw (ex-info (str "C3 syntax construction requires operation " key)
                    {:operation key}))))

(def ^:private default-operations
  {:c2-path-neutral-span (fn [span]
                           (if (map? span) (dissoc span :source) span))
   :sha256-hex digest/sha256-hex
   :c3-origin-chain syntax-origin/c3-origin-chain
   :c3-source-form-kind (unsupported :c3-source-form-kind)
   :c3-source-facts (unsupported :c3-source-facts)})

(defn- validate-operations!
  [operations]
  (when-not (map? operations)
    (throw (ex-info "C3 syntax construction operations must be a map"
                    {:operations operations})))
  (let [unknown (vec (remove function-operation-keys (keys operations)))]
    (when (seq unknown)
      (throw (ex-info "C3 syntax construction operations contain unknown keys"
                      {:unknown-keys unknown}))))
  (doseq [[key value] operations]
    (when-not (fn? value)
      (throw (ex-info "C3 syntax construction operation must be a function"
                      {:operation key :value value}))))
  operations)

(defn with-operations
  [operations thunk]
  (validate-operations! operations)
  (when-not (fn? thunk)
    (throw (ex-info "C3 syntax construction thunk must be a function"
                    {:thunk thunk})))
  (binding [*operations* (merge default-operations operations)]
    (thunk)))

(defn- current-operation
  [key]
  (when-not (contains? *active-operation-keys* key)
    (get *operations* key)))

(defmacro ^:private definterposable
  [name args & body]
  (let [key (keyword name)]
    `(defn ~name ~args
       (if-let [operation# (current-operation ~key)]
         (binding [*active-operation-keys* (conj *active-operation-keys* ~key)]
           (operation# ~@args))
         (do ~@body)))))

(defn- invoke
  [key & args]
  (apply (or (current-operation key)
             (get default-operations key)
             (unsupported key))
         args))

(definterposable c3-path-neutral-origin
  [origin]
  (cond-> origin
    (contains? origin :span)
    (update :span #(invoke :c2-path-neutral-span %))
    (contains? origin :source-span)
    (update :source-span #(invoke :c2-path-neutral-span %))
    (contains? origin :from)
    (update :from #(invoke :c2-path-neutral-span %))))

(definterposable c3-identity-input
  [seed origin namespace-context hygiene-context source-form-kind]
  {:form-kind source-form-kind
   :form (pr-str (:form seed))
   :span (invoke :c2-path-neutral-span (:span seed))
   :origin (mapv c3-path-neutral-origin origin)
   :namespace namespace-context
   :phase (:phase seed)
   :profile (:profile seed)
   :metadata (:metadata seed)
   :hygiene hygiene-context
   :version 1})

(definterposable c3-stable-syntax-id
  [identity-input]
  (str "sha256:" (invoke :sha256-hex (pr-str identity-input))))

(definterposable c3-syntax-object
  [seed form-record token-record source-unit c2-artifact integrity-report]
  (let [namespace-context {:current (:namespace seed)
                           :aliases {}
                           :imports []}
        hygiene-context {:marks []
                         :lexical-scopes []
                         :renames {}
                         :captures []
                         :introduced-identifiers []
                         :macro-definition-namespace nil
                         :macro-call-site-namespace (:namespace seed)}
        origin (invoke :c3-origin-chain seed source-unit)
        source-form-kind (invoke :c3-source-form-kind
                                 seed form-record c2-artifact integrity-report)
        identity-input (c3-identity-input seed origin namespace-context
                                          hygiene-context source-form-kind)
        syntax-id (c3-stable-syntax-id identity-input)]
    {:artifact :gravity/syntax-object
     :syntax/id syntax-id
     :identity {:algorithm :sha256
                :semantic-fields [:form-kind :form :span :origin
                                  :namespace :phase :profile :metadata
                                  :hygiene :version]
                :input-hash (str "sha256:"
                                 (invoke :sha256-hex
                                         (pr-str identity-input)))}
     :form {:kind source-form-kind
            :value (:form seed)
            :raw (get-in seed [:reader-origin :raw-excerpt])}
     :span {:primary (:span seed)
            :all [(:span seed)]}
     :source {:source-id (:source-id source-unit)
              :form-id (:form-id form-record)
              :token-range [(:open-token form-record)
                            (:close-token form-record)]
              :token-id (:token-id token-record)}
     :namespace namespace-context
     :phase (:phase seed)
     :profile (:profile seed)
     :metadata (:metadata seed)
     :hygiene hygiene-context
     :origin origin
     :facts (invoke :c3-source-facts
                    seed form-record c2-artifact integrity-report)
     :version 1
     :prior-syntax-ids []
     :immutable? true}))

(definterposable c3-generated-syntax-object
  [base-object]
  (let [origin [{:kind :generated
                 :producer {:kind :macro
                            :name 'compiler.c3/with-capture-demo
                            :version "stage0"}
                 :inputs [(:syntax/id base-object)]
                 :generated-span "generated:compiler.c3/with-capture-demo:1"
                 :reason :generated-syntax-conformance
                 :build-effects []}]
        hygiene-context {:marks [:c3/generated-mark]
                         :lexical-scopes [:caller-scope :introduced-scope]
                         :renames {'tmp__auto__ 'tmp__c3__1}
                         :captures [{:identifier 'captured-binding
                                     :macro-api 'gravity.syntax/capture
                                     :call-site-namespace
                                     (get-in base-object [:namespace :current])
                                     :intentional? true
                                     :authority-bearing? false}]
                         :introduced-identifiers ['tmp__c3__1]
                         :macro-definition-namespace 'compiler.c3
                         :macro-call-site-namespace
                         (get-in base-object [:namespace :current])}
        namespace-context (:namespace base-object)
        identity-input {:form-kind :generated-form
                        :form "(do tmp__c3__1)"
                        :span "generated:compiler.c3/with-capture-demo:1"
                        :origin origin
                        :namespace namespace-context
                        :phase :macro-expanded
                        :profile (:profile base-object)
                        :metadata {:generated true}
                        :hygiene hygiene-context
                        :version 1}
        syntax-id (c3-stable-syntax-id identity-input)]
    {:artifact :gravity/syntax-object
     :syntax/id syntax-id
     :identity {:algorithm :sha256
                :semantic-fields [:form-kind :form :span :origin
                                  :namespace :phase :profile :metadata
                                  :hygiene :version]
                :input-hash (str "sha256:"
                                 (invoke :sha256-hex
                                         (pr-str identity-input)))}
     :form {:kind :generated-form
            :value '(do tmp__c3__1)
            :raw "(do tmp__c3__1)"}
     :span {:primary "generated:compiler.c3/with-capture-demo:1"
            :all ["generated:compiler.c3/with-capture-demo:1"
                  (get-in base-object [:span :primary])]}
     :source {:source-id (get-in base-object [:source :source-id])
              :form-id :generated-form-0
              :token-range []
              :token-id nil}
     :namespace namespace-context
     :phase :macro-expanded
     :profile (:profile base-object)
     :metadata {:generated true
                :source-metadata (:metadata base-object)}
     :hygiene hygiene-context
     :origin origin
     :facts {}
     :version 1
     :prior-syntax-ids [(:syntax/id base-object)]
     :immutable? true}))
