(ns gravity.module-analysis.contract)

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

(defn namespace-contract
  [operation-keys]
  {:namespace 'gravity.module-analysis
   :contract-boundary :hosted-stage0-l3-module-analysis-projection
   :public-api public-api
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
   {:requires ['clojure.core 'clojure.string 'java.security.MessageDigest]
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
