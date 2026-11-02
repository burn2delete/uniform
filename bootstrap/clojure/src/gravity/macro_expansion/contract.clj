(ns gravity.macro-expansion.contract)

(def operation-keys
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

(def namespace-contract
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
