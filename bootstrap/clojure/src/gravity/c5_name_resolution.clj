(ns gravity.c5-name-resolution
  "Hosted Stage0 C5 name-resolution compatibility facade.

  Components own operations, bindings, lexical scopes, resolution, diagnostics,
  artifacts, and orchestration. This namespace retains the bootstrap API."
  (:require [gravity.c5-name-resolution.artifacts :as artifacts]
            [gravity.c5-name-resolution.bindings :as bindings]
            [gravity.c5-name-resolution.config :as config]
            [gravity.c5-name-resolution.diagnostics :as diagnostics]
            [gravity.c5-name-resolution.engine :as engine]
            [gravity.c5-name-resolution.lexical :as lexical]
            [gravity.c5-name-resolution.operations :as operations]
            [gravity.c5-name-resolution.resolution :as resolution]))

;; These private names retain leaf-level seam discovery; the operations component
;; owns the shared dynamic state used by every semantic component.
(def ^:private ^:dynamic *operations* {})
(def ^:private operation-keys operations/operation-keys)
(def ^:private function-operation-keys operations/function-operation-keys)
(def ^:private default-fail! operations/default-fail!)
(def ^:private default-source-span operations/default-source-span)
(def ^:private default-sha256-hex operations/default-sha256-hex)
(def ^:private default-c4-artifact-id operations/default-c4-artifact-id)
(def ^:private default-collect-code-symbols operations/default-collect-code-symbols)
(def ^:private default-ns-form? operations/default-ns-form?)
(def ^:private unsupported-host-operation operations/unsupported-host-operation)
(def ^:private op-fn operations/op-fn)
(def ^:private op-value operations/op-value)
(def ^:private fail! operations/fail!)
(def ^:private source-span operations/source-span)
(def ^:private sha256-hex operations/sha256-hex)
(def ^:private c4-artifact-id operations/c4-artifact-id)
(def ^:private collect-code-symbols operations/collect-code-symbols)
(def ^:private ns-form? operations/ns-form?)
(def ^:private known-source-profiles config/known-source-profiles)
(def ^:private supported-targets config/supported-targets)
(def ^:private read-source-form-records operations/read-source-form-records)
(def ^:private validate-ns-syntax! operations/validate-ns-syntax!)
(def ^:private parse-module operations/parse-module)
(def ^:private module-source-artifact operations/module-source-artifact)
(def ^:private compiler-c4-macro-source-artifact operations/compiler-c4-macro-source-artifact)
(def ^:private validate-operations! operations/validate-operations!)
(defn- default-operations []
  {:fail! default-fail! :source-span default-source-span :sha256-hex default-sha256-hex
   :c4-artifact-id default-c4-artifact-id :collect-code-symbols default-collect-code-symbols
   :ns-form? default-ns-form?})

(def ^:private namespace-contract
  {:namespace 'gravity.c5-name-resolution
   :contract-boundary :hosted-stage0-c5-name-resolution-engine
   :public-api :bootstrap-compatible-c5-vars
   :artifact-inputs [:reader-records :c4-expanded-syntax :module-artifact]
   :artifact-outputs [:namespace-analysis :binding-table :alias-table :import-export-table
                      :lexical-scope-graph :dependency-graph :cross-profile-edge-report
                      :resolution-diagnostics :incremental-invalidation-keys]
   :owns [:hosted-stage0-c5-binding-algorithm :hosted-stage0-c5-compatibility-artifact]
   :dependency-direction {:requires ['clojure.set 'clojure.string 'gravity.digest]
                          :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :does-not-own [:canonical-c5-authority :gravity-sh06-resolution-authority
                  :source-authentication :proof-authority :self-hosting :equivalence
                  :release :seed-retirement :package-discovery :type-checking
                  :effect-checking :safety-analysis]
   :compatibility-only? true :override-driven-diagnostics? true
   :cycle-analysis-complete? false
   :operation-interposition {:accepted-keys operation-keys :partial-overrides? true
                             :bootstrap-wrapper-arities? true}
   :canonical-c5-authority? false :self-hosted? false :clojure-seed-boundary? true})

(defn with-operations [operations thunk]
  (gravity.c5-name-resolution.operations/validate-operations! operations)
  (let [merged (merge *operations* operations)]
    (binding [*operations* merged
              gravity.c5-name-resolution.operations/*operations* merged]
      (thunk))))
(def c5-resolution-diagnostic-ids config/c5-resolution-diagnostic-ids)
(def c5-resolution-governing-document config/c5-resolution-governing-document)
(def c5-resolution-rejected-designs config/c5-resolution-rejected-designs)
(def c5-resolution-override-diagnostics config/c5-resolution-override-diagnostics)
(def c5-special-form-symbols config/c5-special-form-symbols)
(def c5-core-auto-imports config/c5-core-auto-imports)
(def c5-type-auto-imports config/c5-type-auto-imports)
(def ^{:arglists '([module])} c5-resolution-source-overrides config/c5-resolution-source-overrides)
(def ^{:arglists '([id])} c5-resolution-message config/c5-resolution-message)
(def ^{:arglists '([id source-path subject extra])} c5-resolution-fail! diagnostics/c5-resolution-fail!)
(def ^{:arglists '([source-path module overrides])} c5-resolution-validate-overrides! diagnostics/c5-resolution-validate-overrides!)
(def ^{:arglists '([module])} c5-package-record bindings/c5-package-record)
(def ^{:arglists '([binding])} c5-binding-id bindings/c5-binding-id)
(def ^{:arglists '([binding])} c5-binding-identity bindings/c5-binding-identity)
(def ^{:arglists '([module definition artifact-id])} c5-definition-binding bindings/c5-definition-binding)
(def ^{:arglists '([sym module])} c5-special-form-binding bindings/c5-special-form-binding)
(def ^{:arglists '([sym module])} c5-core-binding bindings/c5-core-binding)
(def ^{:arglists '([sym module])} c5-type-binding bindings/c5-type-binding)
(def ^{:arglists '([module dependency imported-name artifact-id])} c5-import-binding bindings/c5-import-binding)
(def ^{:arglists '([module])} c5-alias-table bindings/c5-alias-table)
(def ^{:arglists '([module])} c5-import-export-table bindings/c5-import-export-table)
(def ^{:arglists '([module module-artifact c4-artifact])} c5-definition-bindings bindings/c5-definition-bindings)
(def ^{:arglists '([module c4-artifact])} c5-macro-bindings bindings/c5-macro-bindings)
(def ^{:arglists '([params])} c5-param-symbols lexical/c5-param-symbols)
(def ^{:arglists '([module form syntax-id])} c5-local-bindings-from-params lexical/c5-local-bindings-from-params)
(def ^{:arglists '([form])} c5-let-binding-symbols lexical/c5-let-binding-symbols)
(def ^{:arglists '([module expanded-stream])} c5-local-scope-graph lexical/c5-local-scope-graph)
(def ^{:arglists '([bindings])} c5-bindings-by-name resolution/c5-bindings-by-name)
(def ^{:arglists '([module alias-map dependency-map sym])} c5-resolve-qualified-symbol resolution/c5-resolve-qualified-symbol)
(def ^{:arglists '([module bindings-by-name alias-map dependency-map local-bindings syntax idx sym])} c5-resolution-record resolution/c5-resolution-record)
(def ^{:arglists '([module definition-bindings macro-bindings lexical-scope-graph expanded-stream])} c5-binding-table resolution/c5-binding-table)
(def ^{:arglists '([module binding-table alias-table import-export-table dependency-graph cross-profile-report])} c5-namespace-analysis-artifact artifacts/c5-namespace-analysis-artifact)
(def ^{:arglists '([module])} c5-dependency-graph artifacts/c5-dependency-graph)
(def ^{:arglists '([module dependency-graph])} c5-cross-profile-edge-report artifacts/c5-cross-profile-edge-report)
(def ^{:arglists '([module c4-artifact binding-table dependency-graph])} c5-incremental-invalidation-keys artifacts/c5-incremental-invalidation-keys)
(def ^{:arglists '([module])} c5-resolution-diagnostics artifacts/c5-resolution-diagnostics)
(def ^{:arglists '([binding-table lexical-scope-graph dependency-graph cross-profile-report invalidation])} c5-resolution-verification-report artifacts/c5-resolution-verification-report)
(def ^{:arglists '([artifact])} c5-resolution-capability-proof artifacts/c5-resolution-capability-proof)
(def ^{:arglists '([source-path artifact])} c5-resolution-validate! diagnostics/c5-resolution-validate!)
(def ^{:arglists '([source-path source-text])} compiler-c5-resolution-source-artifact engine/compiler-c5-resolution-source-artifact)
(def ^{:arglists '([path])} compiler-c5-resolution-file-artifact engine/compiler-c5-resolution-file-artifact)

(def public-api
  {'public-api {:kind :contract}
   'c5-resolution-diagnostic-ids {:kind :constant} 'c5-resolution-governing-document {:kind :constant}
   'c5-resolution-rejected-designs {:kind :constant} 'c5-resolution-override-diagnostics {:kind :constant}
   'c5-special-form-symbols {:kind :constant} 'c5-core-auto-imports {:kind :constant} 'c5-type-auto-imports {:kind :constant}
   'c5-resolution-source-overrides {:arglists '([module])} 'c5-resolution-message {:arglists '([id])}
   'c5-resolution-fail! {:arglists '([id source-path subject extra])} 'c5-resolution-validate-overrides! {:arglists '([source-path module overrides])}
   'c5-package-record {:arglists '([module])} 'c5-binding-id {:arglists '([binding])} 'c5-binding-identity {:arglists '([binding])}
   'c5-definition-binding {:arglists '([module definition artifact-id])} 'c5-special-form-binding {:arglists '([sym module])}
   'c5-core-binding {:arglists '([sym module])} 'c5-type-binding {:arglists '([sym module])}
   'c5-import-binding {:arglists '([module dependency imported-name artifact-id])} 'c5-alias-table {:arglists '([module])}
   'c5-import-export-table {:arglists '([module])} 'c5-definition-bindings {:arglists '([module module-artifact c4-artifact])}
   'c5-macro-bindings {:arglists '([module c4-artifact])} 'c5-param-symbols {:arglists '([params])}
   'c5-local-bindings-from-params {:arglists '([module form syntax-id])} 'c5-let-binding-symbols {:arglists '([form])}
   'c5-local-scope-graph {:arglists '([module expanded-stream])} 'c5-bindings-by-name {:arglists '([bindings])}
   'c5-resolve-qualified-symbol {:arglists '([module alias-map dependency-map sym])}
   'c5-resolution-record {:arglists '([module bindings-by-name alias-map dependency-map local-bindings syntax idx sym])}
   'c5-binding-table {:arglists '([module definition-bindings macro-bindings lexical-scope-graph expanded-stream])}
   'c5-namespace-analysis-artifact {:arglists '([module binding-table alias-table import-export-table dependency-graph cross-profile-report])}
   'c5-dependency-graph {:arglists '([module])} 'c5-cross-profile-edge-report {:arglists '([module dependency-graph])}
   'c5-incremental-invalidation-keys {:arglists '([module c4-artifact binding-table dependency-graph])}
   'c5-resolution-diagnostics {:arglists '([module])}
   'c5-resolution-verification-report {:arglists '([binding-table lexical-scope-graph dependency-graph cross-profile-report invalidation])}
   'c5-resolution-capability-proof {:arglists '([artifact])} 'c5-resolution-validate! {:arglists '([source-path artifact])}
   'compiler-c5-resolution-source-artifact {:arglists '([source-path source-text])}
   'compiler-c5-resolution-file-artifact {:arglists '([path])} 'with-operations {:arglists '([operations thunk])}
   'c5-engine-contract {:arglists '([])}})

(defn c5-engine-contract [] (assoc namespace-contract :public-api public-api))
