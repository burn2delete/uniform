(ns gravity.c18-verification.contract
  "Static boundary and public API contracts for the hosted C18 facade.")

(def namespace-contract
  {:contract-boundary :hosted-stage0-c18-verification-evidence
   :dependency-direction
   {:requires ['clojure.set 'clojure.string
               'gravity.compiler-verification-shared 'gravity.digest]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :owns [:hosted-stage0-c18-risk-classification
          :hosted-stage0-c18-trust-evidence]
   :does-not-own [:canonical-c18-authority :source-authentication
                  :proof-checking-authority :translation-validation-authority
                  :certificate-authority :trust-report-authority
                  :release-gate-authority :release-authorization
                  :backend-conformance-authority :plugin-evidence-authority
                  :equivalence :self-hosting :release :seed-retirement]
   :compatibility-only? true
   :evidence-authoritative? false
   :release-authority? false
   :verification-model-complete? false
   :canonical-c18-authority? false})

(def public-api
  {'public-api {:kind :contract}
   'with-operations {:arglists '([operations thunk])}
   'c18-engine-contract {:arglists '([])}
   'c18-verification-governing-document {:kind :constant}
   'c18-verification-diagnostic-ids {:kind :constant}
   'c18-pass-risk-required-fields {:kind :constant}
   'c18-trust-report-required-fields {:kind :constant}
   'c18-verification-source-overrides {:arglists '([module])}
   'c18-verification-fail! {:arglists '([id source-path subject extra])}
   'c18-verification-validate-source-overrides!
   {:arglists '([source-path overrides])}
   'c18-verification-diagnostic-stream
   {:arglists '([source-path input-id])}
   'c18-pass-risk-records {:arglists '([])}
   'c18-verification-validate! {:arglists '([source-path artifact])}
   'c18-verification-capability-proof {:arglists '([artifact])}
   'compiler-c18-verification-source-artifact
   {:arglists '([source-path source-text])}
   'compiler-c18-verification-file-artifact {:arglists '([path])}})

(defn engine-contract [operation-keys]
  (assoc namespace-contract
         :operation-interposition
         {:accepted-keys operation-keys
          :unknown-keys-rejected? true
          :partial-overrides? true
          :single-binding-per-top-level-call? true}
         :public-api public-api))
