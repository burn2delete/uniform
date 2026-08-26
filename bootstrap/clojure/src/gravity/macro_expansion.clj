(ns gravity.macro-expansion
  "Hosted stage0 macro-expansion engine.

  This namespace owns the Clojure seed's small macro/template engine and its
  compatibility artifact projection.  It is deliberately not the canonical
  C4/SH-05 authority: authenticated Gravity macro expansion, self-hosting,
  equivalence, release, proof, and seed-retirement claims remain with the
  canonical pipeline in `gravity.bootstrap` and the Gravity source modules.

  Callers may inject the seed's diagnostic, hashing, syntax, and recursive
  operation functions.  The injection boundary keeps this leaf acyclic while
  preserving the bootstrap wrappers' dynamic interposition behavior."
  (:require [clojure.set :as set]
            [gravity.digest :as digest]))

(def ^:private operation-keys
  #{:assert-build-effects! :assert-generated-profile!
    :assert-generated-unsafe! :assert-hygiene! :bind-macro-arguments
    :built-in-registry :builtin-defn-output :builtin-macros
    :builtin-thread-first-output :builtin-when-output :collect-let-bindings
    :collect-symbols :contains-form-op? :distinct-by-pr-str
    :expand-child-form :expand-form-children :expand-macro-form
    :expand-syntax-object :expand-template :expand-template-items
    :expansion-generated-origin :expansion-trace-record :fail! :form-op?
    :local-macro-symbol :macro-build-effect-record :macro-build-grants
    :macro-call :macro-env-value :macro-namespace-entry :macro-registry
    :max-macro-expansion-depth :parse-defmacro-form :parse-param-list
    :parse-syntax-template :sha256-hex :source-span :splice-key
    :thread-first-step})

(def ^:private namespace-contract
  {:namespace 'gravity.macro-expansion
   :contract-boundary :hosted-stage0-macro-expansion-engine
   :public-api
   {'local-macro-symbol {:arglists '([module name])}
    'parse-param-list {:arglists '([params])}
    'bind-macro-arguments {:arglists '([macro args call-span])}
    'expand-template-items {:arglists '([env items])}
    'macro-env-value {:arglists '([env sym])}
    'expand-template {:arglists '([env template])}
    'parse-syntax-template {:arglists '([macro call-span])}
    'builtin-defn-output {:arglists '([args call-span])}
    'builtin-when-output {:arglists '([args call-span])}
    'thread-first-step {:arglists '([value step])}
    'builtin-thread-first-output {:arglists '([args call-span])}
    'builtin-macros {:value :stage0-built-in-macro-registry}
    'built-in-registry {:arglists '([])}
    'parse-defmacro-form {:arglists '([module syntax])}
    'macro-registry {:arglists '([module syntax])}
    'macro-namespace-entry {:arglists '([macro])}
    'macro-build-effect-record {:arglists '([macro])}
    'macro-build-grants {:arglists '([module])}
    'assert-build-effects! {:arglists '([module macro call-span])}
    'collect-let-bindings {:arglists '([form])}
    'assert-hygiene! {:arglists '([macro args output call-span])}
    'assert-generated-profile! {:arglists '([module macro output call-span])}
    'assert-generated-unsafe! {:arglists '([module macro output call-span])}
    'expand-macro-form {:arglists '([module macro args call-span])}
    'expansion-generated-origin {:arglists '([macro syntax input output])}
    'macro-call {:arglists '([registry form])}
    'expand-child-form {:arglists '([registry module syntax form trace depth])}
    'expand-form-children {:arglists '([registry module syntax form trace depth])}
    'expansion-trace-record
    {:arglists '([module macro syntax input output generated-origin depth])}
    'distinct-by-pr-str {:arglists '([values])}
    'expand-syntax-object
    {:arglists '([registry module syntax trace depth])}
    'macro-source-artifact-from-records
    {:arglists '([source-path source-text records module syntax])}
    'with-normalized-operations
    {:arglists '([operations operation])}}
   :artifact-inputs [:reader-syntax-records :module-context :source-path]
   :artifact-outputs [:stage0-macro-artifact :expanded-syntax :macro-trace]
   :operation-interposition
   {:accepted-keys operation-keys
    :partial-overrides? true
    :leaf-functions-add-final-operations-argument? true
    :public-api-arglists-describe :bootstrap-compatibility-arities}
   :ownership
   {:owns [:hosted-stage0-macro-expansion
           :hosted-stage0-macro-compatibility-artifact]
    :does-not-own [:canonical-c4-authority
                   :gravity-sh05-macro-authority
                   :source-reading
                   :source-authentication
                   :macro-proof-authority
                   :self-hosting
                   :equivalence
                   :release
                   :seed-retirement
                   :downstream-profile-type-effect-or-safety-checks]}
   :dependency-direction
   {:requires ['clojure.core 'clojure.set 'gravity.digest]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-c4-authority? false
   :self-hosted? false
   :proof-authority? false
   :equivalence-authority? false
   :release-authority? false})

(defn- default-fail!
  [id message data]
  (throw (ex-info message (assoc (or data {}) :id id))))

(defn- default-form-op?
  [op form]
  (and (seq? form) (= op (first form))))

(declare default-contains-form-op?)

(defn- default-contains-form-op?
  [op form]
  (cond
    (default-form-op? op form) true
    (seq? form) (some #(default-contains-form-op? op %) form)
    (coll? form) (some #(default-contains-form-op? op %) form)
    :else false))

(defn- default-collect-symbols
  [form]
  (cond
    (symbol? form) [form]
    (seq? form) (mapcat default-collect-symbols form)
    (coll? form) (mapcat default-collect-symbols form)
    :else []))

(defn- default-local-macro-symbol
  [module name]
  (symbol (str (:module module)) (str name)))

(defn- default-source-span
  [source-path form-index]
  {:source source-path :form-index form-index})

(defn- default-ops
  []
  {:fail! default-fail!
   :form-op? default-form-op?
   :contains-form-op? default-contains-form-op?
   :collect-symbols default-collect-symbols
   :local-macro-symbol default-local-macro-symbol
   :source-span default-source-span
   :sha256-hex digest/sha256-hex
   :splice-key ::splice
   :max-macro-expansion-depth 16})

;; Operation maps cross the hosted bootstrap boundary for every recursive
;; macro-expansion call.  Keep the fast path request-local and identity-bound:
;; the private token cannot be forged by supplying an ordinary operation map,
;; and no normalized map is retained after the caller's dynamic binding ends.
(def ^:private normalized-ops-context-token (Object.))
(def ^:private ^:dynamic *normalized-ops-context* nil)

(defn- normalize-ops-uncached
  [ops]
  (when-not (or (nil? ops) (map? ops))
    (throw (ex-info "macro expansion operations must be a map"
                    {:id "STAGE0-MACRO-EXPANSION-OPERATIONS"
                     :operations ops})))
  (let [provided (or ops {})
        unexpected (set/difference (set (keys provided)) operation-keys)
        scalar-keys #{:builtin-macros :max-macro-expansion-depth :splice-key}
        non-functions (->> (keys provided)
                           (remove scalar-keys)
                           (remove #(fn? (get provided %)))
                           set)]
    (when (or (seq unexpected)
              (seq non-functions)
              (and (contains? provided :builtin-macros)
                   (not (map? (:builtin-macros provided))))
              (and (contains? provided :max-macro-expansion-depth)
                   (not (and (integer? (:max-macro-expansion-depth provided))
                             (pos? (:max-macro-expansion-depth provided))))))
      (throw (ex-info "macro expansion operations are invalid"
                      {:id "STAGE0-MACRO-EXPANSION-OPERATIONS"
                       :accepted-operation-keys operation-keys
                       :unexpected-operation-keys unexpected
                       :non-function-operation-keys non-functions
                       :operations provided})))
    (merge (default-ops) provided)))

(defn- normalize-ops
  [ops]
  (let [context *normalized-ops-context*]
    (if (and (map? context)
             (identical? normalized-ops-context-token (:token context))
             (identical? ops (:ops context)))
      ops
      (normalize-ops-uncached ops))))

(defn with-normalized-operations
  "Run `operation` with one validated operation map for this request.

  The normalized map is passed to the operation and is also bound by identity
  for nested calls.  The binding is dynamic and request-local; it is not a
  persistent cache or an authority-bearing artifact field."
  [operations operation]
  (let [normalized (normalize-ops operations)]
    (binding [*normalized-ops-context*
              {:token normalized-ops-context-token
               :ops normalized}]
      (operation normalized))))

(defn- op
  [ops key fallback]
  (get ops key fallback))

(defn- fail!
  [ops id message data]
  ((op ops :fail! default-fail!) id message data))

(defn local-macro-symbol
  ([module name]
   (local-macro-symbol module name nil))
  ([module name ops]
   ((op (normalize-ops ops) :local-macro-symbol default-local-macro-symbol)
    module name)))

(defn parse-param-list
  ([params]
   (parse-param-list params nil))
  ([params ops]
   (let [ops (normalize-ops ops)]
     (loop [items (seq params)
            fixed []]
       (cond
         (nil? items) {:fixed fixed :rest nil}
         (= '& (first items))
         (let [rest-name (second items)]
           (when-not (and (symbol? rest-name) (nil? (nnext items)))
             (fail! ops "L4-MACRO-NOT-SYNTAX"
                    "macro rest parameter must be a single symbol"
                    {:params params
                     :remediation "Use a parameter vector such as [x & body]."}))
           {:fixed fixed :rest rest-name})
         (symbol? (first items)) (recur (next items) (conj fixed (first items)))
         :else
         (fail! ops "L4-MACRO-NOT-SYNTAX"
                "macro parameters must be symbols"
                {:params params
                 :remediation "Use symbolic macro parameters."}))))))

(defn bind-macro-arguments
  ([macro args call-span]
   (bind-macro-arguments macro args call-span nil))
  ([macro args call-span ops]
   (let [ops (normalize-ops ops)
         {:keys [fixed rest]} (:params macro)]
     (when (or (< (count args) (count fixed))
               (and (nil? rest) (not= (count args) (count fixed))))
       (fail! ops "L4-MACRO-NOT-SYNTAX"
              "macro call does not match the accepted syntax shape"
              {:source-span call-span
               :macro (:identity macro)
               :params fixed
               :rest rest
               :argument-count (count args)
               :remediation "Call the macro with the syntax shape declared by its parameter vector."}))
     (let [fixed-bindings (zipmap fixed (take (count fixed) args))]
       (if rest
         (assoc fixed-bindings rest (vec (drop (count fixed) args)))
         fixed-bindings)))))

(declare expand-template)

(defn expand-template-items
  ([env items]
   (expand-template-items env items nil))
  ([env items ops]
   (let [ops (normalize-ops ops)
         splice-key (or (:splice-key ops) ::splice)
         expand-template-op (or (:expand-template ops)
                                (fn [inner-env template]
                                  (expand-template inner-env template ops)))]
     (mapcat (fn [item]
               (let [expanded (expand-template-op env item)]
                 (if (and (map? expanded) (contains? expanded splice-key))
                   (get expanded splice-key)
                   [expanded])))
             items))))

(defn macro-env-value
  ([env sym]
   (macro-env-value env sym nil))
  ([env sym ops]
   (let [ops (normalize-ops ops)]
     (if (contains? env sym)
       (get env sym)
       (fail! ops "L4-MACRO-NOT-SYNTAX"
              "syntax template references an unbound macro parameter"
              {:symbol sym
               :remediation "Use only symbols bound by the macro parameter vector inside unquote forms."})))))

(defn expand-template
  ([env template]
   (expand-template env template nil))
  ([env template ops]
   (let [ops (normalize-ops ops)
         splice-key (or (:splice-key ops) ::splice)
         expand-template-op (or (:expand-template ops)
                                (fn [inner-env value]
                                  (expand-template inner-env value ops)))
         macro-env-value-op (or (:macro-env-value ops)
                                (fn [inner-env sym]
                                  (macro-env-value inner-env sym ops)))
         expand-template-items-op (or (:expand-template-items ops)
                                      (fn [inner-env values]
                                        (expand-template-items inner-env values ops)))]
     (cond
       (seq? template)
       (case (first template)
         unquote
         (let [[_ sym] template]
           (when-not (symbol? sym)
             (fail! ops "L4-MACRO-NOT-SYNTAX"
                    "unquote requires a macro parameter symbol"
                    {:form template
                     :remediation "Use (unquote name) inside syntax templates."}))
           (macro-env-value-op env sym))

         splice-unquote
         (let [[_ sym] template
               value (macro-env-value-op env sym)]
           (when-not (sequential? value)
             (fail! ops "L4-MACRO-NOT-SYNTAX"
                    "splice-unquote requires a rest parameter value"
                    {:form template
                     :value value
                     :remediation "Use splice-unquote with a rest parameter such as body."}))
           {splice-key value})

         (apply list (expand-template-items-op env template)))

       (vector? template) (vec (expand-template-items-op env template))
       (map? template) (into {} (map (fn [[k v]]
                                       [(expand-template-op env k)
                                        (expand-template-op env v)])
                                     template))
       (set? template) (set (expand-template-items-op env template))
       :else template))))

(defn parse-syntax-template
  ([macro call-span]
   (parse-syntax-template macro call-span nil))
  ([macro call-span ops]
   (let [ops (normalize-ops ops)
         body (:body macro)]
     (when-not (and (= 1 (count body))
                    (seq? (first body))
                    (= 'syntax-quote (ffirst body)))
       (fail! ops "L4-MACRO-NOT-SYNTAX"
              "stage0 macros must return syntax through a syntax-quote template"
              {:source-span call-span
               :macro (:identity macro)
               :body body
               :remediation "Return (syntax-quote form) from stage0 defmacro bodies."}))
     (second (first body)))))

(defn builtin-defn-output
  ([args call-span]
   (builtin-defn-output args call-span nil))
  ([args call-span ops]
   (let [ops (normalize-ops ops)
         [name params & body] args
         [return-type body] (if (= ':- (first body))
                              [(second body) (nnext body)]
                              [nil body])
         fn-form (if return-type
                   (list 'fn params (list 'typed/return (list 'quote return-type)
                                          (cons 'do body)))
                   (cons 'fn (cons params body)))]
     (when-not (and (symbol? name) (vector? params))
       (fail! ops "L4-MACRO-NOT-SYNTAX"
              "defn expansion requires a symbolic name and vector parameters"
              {:source-span call-span
               :form (cons 'defn args)
               :remediation "Use (defn name [args] body...)."}))
     (list 'def name fn-form))))

(defn builtin-when-output
  ([args call-span]
   (builtin-when-output args call-span nil))
  ([args call-span ops]
   (let [[condition & body] args]
     (when (nil? condition)
       (fail! (normalize-ops ops) "L4-MACRO-NOT-SYNTAX"
              "when requires a condition"
              {:source-span call-span
               :remediation "Use (when condition body...)."}))
     (list 'if condition (cons 'do body) nil))))

(defn thread-first-step
  [value step]
  (if (seq? step)
    (apply list (first step) value (rest step))
    (list step value)))

(defn builtin-thread-first-output
  ([args call-span]
   (builtin-thread-first-output args call-span nil))
  ([args call-span ops]
   (let [ops (normalize-ops ops)
         [initial & steps] args
         thread-step (op ops :thread-first-step thread-first-step)]
     (when (nil? initial)
       (fail! ops "L4-MACRO-NOT-SYNTAX"
              "thread-first requires an initial expression"
              {:source-span call-span
               :remediation "Use (-> value step...)."}))
     (reduce thread-step initial steps))))

(def builtin-macros
  {'defn {:name 'defn
          :identity 'gravity.core/defn
          :kind :built-in
          :version "stage0-builtin"
          :macro-namespace 'gravity.core
          :params {:fixed '[name params] :rest 'body}
          :build-effects #{}
          :required-build-capabilities #{}
          :hygiene-policy :hygienic
          :output-contract :gravity-syntax
          :expander builtin-defn-output}
   'when {:name 'when
          :identity 'gravity.core/when
          :kind :built-in
          :version "stage0-builtin"
          :macro-namespace 'gravity.core
          :params {:fixed '[condition] :rest 'body}
          :build-effects #{}
          :required-build-capabilities #{}
          :hygiene-policy :hygienic
          :output-contract :gravity-syntax
          :expander builtin-when-output}
   '-> {:name '->
        :identity 'gravity.core/->
        :kind :built-in
        :version "stage0-builtin"
        :macro-namespace 'gravity.core
        :params {:fixed '[initial] :rest 'steps}
        :build-effects #{}
        :required-build-capabilities #{}
        :hygiene-policy :hygienic
        :output-contract :gravity-syntax
        :expander builtin-thread-first-output}})

(defn built-in-registry
  ([ ]
   (built-in-registry nil))
  ([ops]
   (let [ops (normalize-ops ops)
         macros (or (:builtin-macros ops) builtin-macros)]
     (reduce-kv (fn [acc name macro]
                  (assoc acc name macro (:identity macro) macro))
                {}
                macros))))

(defn parse-defmacro-form
  ([module syntax]
   (parse-defmacro-form module syntax nil))
  ([module syntax ops]
   (let [ops (normalize-ops ops)
         form (:form syntax)
         [_ name & tail] form
         [metadata tail] (if (map? (first tail))
                           [(first tail) (rest tail)]
                           [{} tail])
         params (first tail)
         body (vec (rest tail))]
     (when-not (symbol? name)
       (fail! ops "L4-MACRO-NOT-SYNTAX"
              "defmacro requires a symbolic name"
              {:source-span (:span syntax)
               :form form
               :remediation "Use (defmacro name [args] (syntax-quote ...))."}))
     (when-not (vector? params)
       (fail! ops "L4-MACRO-NOT-SYNTAX"
              "defmacro requires a parameter vector"
              {:source-span (:span syntax)
               :macro name
               :remediation "Use a vector parameter list."}))
     (let [identity ((op ops :local-macro-symbol default-local-macro-symbol)
                     module name)
           parse-params (op ops :parse-param-list
                             (fn [values] (parse-param-list values ops)))
           macro {:name name
                  :identity identity
                  :kind :source
                  :version (or (:version metadata) "stage0-source")
                  :macro-namespace (:module module)
                  :params (parse-params params)
                  :source-span (:span syntax)
                  :metadata metadata
                  :body body
                  :build-effects (or (:build-effects metadata) #{})
                  :uses-build-effects (or (:uses-build-effects metadata) #{})
                  :required-build-capabilities
                  (or (:required-build-capabilities metadata) #{})
                  :hygiene-policy (or (:hygiene-policy metadata) :hygienic)
                  :output-contract (or (:output-contract metadata) :gravity-syntax)
                  :allow-unsafe? (true? (:allow-unsafe metadata))
                  :omit-generated-origin? (true? (:omit-generated-origin metadata))}]
       [name identity macro]))))

(defn macro-registry
  ([module syntax]
   (macro-registry module syntax nil))
  ([module syntax ops]
   (let [ops (normalize-ops ops)
         form-op (op ops :form-op? default-form-op?)
         parse-defmacro (op ops :parse-defmacro-form
                              (fn [m s] (parse-defmacro-form m s ops)))
         built-ins (op ops :built-in-registry
                        (fn [] (built-in-registry ops)))]
     (reduce (fn [registry syn]
               (if (form-op 'defmacro (:form syn))
                 (let [[name identity macro] (parse-defmacro module syn)]
                   (assoc registry name macro identity macro))
                 registry))
             (built-ins)
             syntax))))

(defn macro-namespace-entry
  [macro]
  (select-keys macro [:name :identity :kind :version :macro-namespace :params
                      :build-effects :required-build-capabilities :hygiene-policy
                      :output-contract :source-span]))

(defn macro-build-effect-record
  [macro]
  {:macro (:identity macro)
   :declared-build-effects (:build-effects macro)
   :used-build-effects (or (:uses-build-effects macro) (:build-effects macro) #{})
   :required-build-capabilities (:required-build-capabilities macro)})

(defn macro-build-grants
  [module]
  (or (get-in module [:metadata :build-grants]) #{}))

(defn assert-build-effects!
  ([module macro call-span]
   (assert-build-effects! module macro call-span nil))
  ([module macro call-span ops]
   (let [ops (normalize-ops ops)
         used (or (:uses-build-effects macro) (:build-effects macro) #{})
         declared (:build-effects macro)
         grants ((op ops :macro-build-grants macro-build-grants) module)
         undeclared (first (remove declared used))
         ungranted (first (remove grants used))]
     (when undeclared
       (fail! ops "L4-BUILD-EFFECT"
              "macro used an undeclared build effect"
              {:source-span call-span
               :macro (:identity macro)
               :effect undeclared
               :declared-effects declared
               :remediation "Declare build effects in the macro definition."}))
     (when ungranted
       (fail! ops "L4-BUILD-EFFECT"
              "macro used a build effect not granted by the build policy"
              {:source-span call-span
               :macro (:identity macro)
               :effect ungranted
               :declared-effects declared
               :granted-build-effects grants
               :remediation "Grant the build effect in project metadata or remove the effect."})))))

(defn collect-let-bindings
  ([form]
   (collect-let-bindings form nil))
  ([form ops]
   (let [ops (normalize-ops ops)
         form-op (op ops :form-op? default-form-op?)
         recurse (fn [value] (collect-let-bindings value ops))]
     (cond
       (form-op 'let form)
       (concat (take-nth 2 (second form))
               (mapcat recurse (drop 2 form)))
       (seq? form) (mapcat recurse form)
       (coll? form) (mapcat recurse form)
       :else []))))

(defn assert-hygiene!
  ([macro args output call-span]
   (assert-hygiene! macro args output call-span nil))
  ([macro args output call-span ops]
   (let [ops (normalize-ops ops)
         collect-bindings (op ops :collect-let-bindings
                              (fn [form] (collect-let-bindings form ops)))
         introduced (set (filter symbol? (collect-bindings output)))
         caller-symbols (set (mapcat (op ops :collect-symbols default-collect-symbols)
                                     args))
         accidental (first (set/intersection introduced caller-symbols))]
     (when (and accidental (not= :explicit-capture (:hygiene-policy macro)))
       (fail! ops "L4-HYGIENE-CAPTURE"
              "macro expansion would accidentally capture a caller binding"
              {:source-span call-span
               :macro (:identity macro)
               :symbol accidental
               :hygiene-policy (:hygiene-policy macro)
               :remediation "Use a fresh generated binding or mark the macro as explicit capture."})))))

(defn assert-generated-profile!
  ([module macro output call-span]
   (assert-generated-profile! module macro output call-span nil))
  ([module macro output call-span ops]
   (let [ops (normalize-ops ops)]
     (when (and (not= :hosted (:profile module))
                ((op ops :contains-form-op? default-contains-form-op?)
                 'host-reflect output))
       (fail! ops "L4-GENERATED-PROFILE"
              "macro generated code that violates the caller profile"
              {:source-span call-span
               :macro (:identity macro)
               :profile (:profile module)
               :generated-form output
               :remediation "Generated code must pass the caller profile, not the macro implementation profile."})))))

(defn assert-generated-unsafe!
  ([module macro output call-span]
   (assert-generated-unsafe! module macro output call-span nil))
  ([module macro output call-span ops]
   (let [ops (normalize-ops ops)]
     (when (and ((op ops :contains-form-op? default-contains-form-op?) 'unsafe output)
                (not (:allow-unsafe? macro))
                (#{:safe :safe-optimized nil} (:safety module)))
       (fail! ops "L4-GENERATED-UNSAFE"
              "macro generated unsafe code without an explicit unsafe policy"
              {:source-span call-span
               :macro (:identity macro)
               :safety (:safety module)
               :generated-form output
               :remediation "Make unsafe generation explicit and attach an unsafe audit record."})))))

(defn expand-macro-form
  ([module macro args call-span]
   (expand-macro-form module macro args call-span nil))
  ([module macro args call-span ops]
   (let [ops (normalize-ops ops)
         assert-build (op ops :assert-build-effects!
                          (fn [m mac span] (assert-build-effects! m mac span ops)))
         bind-args (op ops :bind-macro-arguments
                       (fn [mac values span] (bind-macro-arguments mac values span ops)))
         parse-template (op ops :parse-syntax-template
                            (fn [mac span] (parse-syntax-template mac span ops)))
         expand-template-op (op ops :expand-template
                                (fn [env template] (expand-template env template ops)))
         assert-hygiene (op ops :assert-hygiene!
                            (fn [mac values output span]
                              (assert-hygiene! mac values output span ops)))
         assert-profile (op ops :assert-generated-profile!
                            (fn [m mac output span]
                              (assert-generated-profile! m mac output span ops)))
         assert-unsafe (op ops :assert-generated-unsafe!
                           (fn [m mac output span]
                             (assert-generated-unsafe! m mac output span ops)))]
     (assert-build module macro call-span)
     (let [output (if-let [expander (:expander macro)]
                    (expander args call-span)
                    (let [env (bind-args macro args call-span)
                          template (parse-template macro call-span)]
                      (expand-template-op env template)))]
       (when (= :source (:kind macro))
         (assert-hygiene macro args output call-span))
       (assert-profile module macro output call-span)
       (assert-unsafe module macro output call-span)
       output))))

(defn expansion-generated-origin
  ([macro syntax input output]
   (expansion-generated-origin macro syntax input output nil))
  ([macro syntax input output ops]
   (let [ops (normalize-ops ops)
         hash (op ops :sha256-hex digest/sha256-hex)]
     (when-not (:omit-generated-origin? macro)
       [{:from (:span syntax)
         :macro (:identity macro)
         :macro-version (:version macro)
         :input-hash (str "sha256:" (hash (pr-str input)))
         :output-hash (str "sha256:" (hash (pr-str output)))}]))))

(defn macro-call
  [registry form]
  (when (seq? form)
    (get registry (first form))))

(declare expand-syntax-object)

(defn expand-child-form
  ([registry module syntax form trace depth]
   (expand-child-form registry module syntax form trace depth nil))
  ([registry module syntax form trace depth ops]
   (let [ops (normalize-ops ops)
         expand-syntax (op ops :expand-syntax-object
                           (fn [r m syn tr d]
                             (expand-syntax-object r m syn tr d ops)))]
     (:form (expand-syntax registry module (assoc syntax :form form) trace depth)))))

(defn expand-form-children
  ([registry module syntax form trace depth]
   (expand-form-children registry module syntax form trace depth nil))
  ([registry module syntax form trace depth ops]
   (let [ops (normalize-ops ops)
         expand-child (op ops :expand-child-form
                          (fn [r m syn value tr d]
                            (expand-child-form r m syn value tr d ops)))]
     (cond
       (seq? form) (apply list (map #(expand-child registry module syntax % trace depth) form))
       (vector? form) (vec (map #(expand-child registry module syntax % trace depth) form))
       (map? form) (into {} (map (fn [[k v]]
                                   [(expand-child registry module syntax k trace depth)
                                    (expand-child registry module syntax v trace depth)])
                                 form))
       (set? form) (set (map #(expand-child registry module syntax % trace depth) form))
       :else form))))

(defn expansion-trace-record
  ([module macro syntax input output generated-origin depth]
   (expansion-trace-record module macro syntax input output generated-origin depth nil))
  ([module macro syntax input output generated-origin depth ops]
   (let [hash (op (normalize-ops ops) :sha256-hex digest/sha256-hex)]
     {:macro (:identity macro)
      :macro-version (:version macro)
      :macro-namespace (:macro-namespace macro)
      :caller-namespace (:namespace syntax)
      :caller-profile (:profile syntax)
      :depth depth
      :call-span (:span syntax)
      :input-syntax-id (:syntax-id syntax)
      :input-hash (str "sha256:" (hash (pr-str input)))
      :output-hash (str "sha256:" (hash (pr-str output)))
      :build-effects (vec (sort-by str (or (:uses-build-effects macro)
                                            (:build-effects macro)
                                            #{})))
      :generated-origin generated-origin
      :generated-spans [(str "generated:" (:identity macro) ":"
                              (get-in syntax [:span :form-index]))]
      :hygiene-policy (:hygiene-policy macro)
      :hygiene-marks (:hygiene syntax)
      :metadata (:metadata syntax)
      :diagnostics []})))

(defn distinct-by-pr-str
  [values]
  (vec (vals (reduce (fn [acc value] (assoc acc (pr-str value) value)) {} values))))

(declare expand-syntax-object)

(defn expand-syntax-object
  ([registry module syntax trace depth]
   (expand-syntax-object registry module syntax trace depth nil))
  ([registry module syntax trace depth ops]
   (let [ops (normalize-ops ops)
         form-op (op ops :form-op? default-form-op?)
         macro-call-op (op ops :macro-call macro-call)
         expand-macro (op ops :expand-macro-form
                           (fn [m mac args span]
                             (expand-macro-form m mac args span ops)))
         generated-origin (op ops :expansion-generated-origin
                              (fn [mac syn input output]
                                (expansion-generated-origin mac syn input output ops)))
         trace-record (op ops :expansion-trace-record
                          (fn [m mac syn input output origin d]
                            (expansion-trace-record m mac syn input output origin d ops)))
         expand-children (op ops :expand-form-children
                             (fn [r m syn value tr d]
                               (expand-form-children r m syn value tr d ops)))
         max-depth (:max-macro-expansion-depth ops 16)]
     (loop [syntax syntax
            depth depth]
       (let [form (:form syntax)]
         (cond
       (form-op 'defmacro form)
       (assoc syntax :phase :macro-definition)

       (macro-call-op registry form)
       (do
         (when (>= depth max-depth)
           (fail! ops "L4-EXPANSION-DEPTH"
                  "macro expansion exceeded the configured depth limit"
                  {:source-span (:span syntax)
                   :form form
                   :depth depth
                   :limit max-depth
                   :remediation "Stop recursive expansion or raise the project expansion limit with evidence."}))
         (let [macro (macro-call-op registry form)
               input form
               output (expand-macro module macro (vec (rest form)) (:span syntax))
               origin (generated-origin macro syntax input output)]
           (when (empty? origin)
             (fail! ops "L4-PROVENANCE-MISSING"
                    "macro expansion output lacks generated-origin metadata"
                    {:source-span (:span syntax)
                     :macro (:identity macro)
                     :remediation "Attach generated-origin metadata linking output syntax to the macro call site."}))
           (swap! trace conj (trace-record module macro syntax input output origin depth))
           (recur (-> syntax
                       (assoc :form output
                              :origin :generated
                              :phase :macro-expanded
                              :generated-origin (vec (concat (:generated-origin syntax) origin))
                              :macro-namespace (:macro-namespace macro))
                       (update :hygiene conj {:macro (:identity macro)
                                              :policy (:hygiene-policy macro)}))
                   (inc depth))))

       (coll? form)
       (assoc syntax
              :form (expand-children registry module syntax form trace depth)
              :phase :macro-expanded)

       :else
         (assoc syntax :phase :macro-expanded)))))))

(defn macro-source-artifact-from-records
  ([source-path source-text records module syntax]
   (macro-source-artifact-from-records source-path source-text records module syntax nil))
  ([source-path source-text records module syntax ops]
   (let [ops (normalize-ops ops)
         registry-op (op ops :macro-registry
                         (fn [m s] (macro-registry m s ops)))
         form-op (op ops :form-op? default-form-op?)
         expand-syntax (op ops :expand-syntax-object
                           (fn [r m syn tr d]
                             (expand-syntax-object r m syn tr d ops)))
         trace (atom [])
         registry (registry-op module syntax)
         raw-expanded-syntax (->> syntax
                                  (remove #(form-op 'defmacro (:form %)))
                                  (mapv #(expand-syntax registry module % trace 0)))
         trace-records @trace
         origins-by-syntax-id (group-by :input-syntax-id trace-records)
         hygiene-by-syntax-id (reduce (fn [acc record]
                                        (update acc (:input-syntax-id record) (fnil conj [])
                                                {:macro (:macro record)
                                                 :policy (:hygiene-policy record)}))
                                      {}
                                      trace-records)
         distinct-op (op ops :distinct-by-pr-str distinct-by-pr-str)
         expanded-syntax (mapv (fn [syn]
                                 (let [trace-origins (mapcat :generated-origin
                                                             (get origins-by-syntax-id (:syntax-id syn)))
                                       trace-hygiene (get hygiene-by-syntax-id (:syntax-id syn))]
                                   (cond-> syn
                                     (seq trace-origins)
                                     (assoc :generated-origin
                                            (distinct-op (concat (:generated-origin syn)
                                                                  trace-origins)))
                                     (seq trace-hygiene)
                                     (assoc :hygiene
                                            (distinct-op (concat (:hygiene syn)
                                                                  trace-hygiene))))))
                               raw-expanded-syntax)
         body-forms (mapv :form (subvec expanded-syntax 1))
         macro-definitions (->> registry
                                vals
                                (distinct)
                                (sort-by (comp str :identity))
                                vec)
         macro-entry (op ops :macro-namespace-entry macro-namespace-entry)
         build-record (op ops :macro-build-effect-record macro-build-effect-record)
         macro-entries (mapv macro-entry macro-definitions)]
     {:kind :gravity/stage0-macro-artifact
      :pass {:name :macro-expansion
             :input :syntax-object-stream
             :output :expanded-syntax
             :requires [:reader :namespace-analyzer]
             :preserves [:source-spans :metadata :hygiene :profile :generated-origin]
             :rejects ["L4-MACRO-NOT-SYNTAX" "L4-HYGIENE-CAPTURE"
                       "L4-BUILD-EFFECT" "L4-EXPANSION-DEPTH"
                       "L4-GENERATED-PROFILE" "L4-GENERATED-UNSAFE"
                       "L4-PROVENANCE-MISSING"]}
      :module (select-keys module [:module :source-path :profile :target :effects
                                   :capabilities :safety :metadata])
      :macro-namespace-entries macro-entries
      :macro-build-effect-records (mapv build-record macro-definitions)
      :macro-expansion-trace trace-records
      :generated-origin-source-map (mapv #(select-keys % [:syntax-id :span :generated-origin])
                                         expanded-syntax)
      :hygiene-marks (mapv #(select-keys % [:syntax-id :hygiene]) expanded-syntax)
      :expanded-syntax-object-stream expanded-syntax
      :expanded-forms body-forms
      :diagnostics []})))
