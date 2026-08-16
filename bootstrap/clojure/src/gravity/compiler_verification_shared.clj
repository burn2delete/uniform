(ns gravity.compiler-verification-shared
  "Shared hosted Stage0 C15-C18 diagnostic compatibility catalogs.")

(def ^:dynamic compiler-verification-diagnostic-ids
  ["C15-SCHEMA"
   "C15-ID"
   "C15-SPAN"
   "C15-ORIGIN"
   "C15-FACTS"
   "C15-REMEDIATION"
   "C15-REDACTION"
   "C15-ORDER"
   "C15-GOLDEN"
   "C16-KEY"
   "C16-ENTRY"
   "C16-STALE"
   "C16-PROOF"
   "C16-SPECULATIVE"
   "C16-REPLAY"
   "C16-POLICY"
   "C16-DIAGNOSTIC"
   "C16-GRAPH"
   "C17-MANIFEST"
   "C17-API"
   "C17-CAPABILITY"
   "C17-BUILD-EFFECT"
   "C17-SANDBOX"
   "C17-PASS-CONTRACT"
   "C17-OUTPUT"
   "C17-DOMAIN"
   "C17-FACET"
   "C17-TRUST"
   "C18-RISK"
   "C18-EVIDENCE"
   "C18-VALIDATION"
   "C18-PROOF"
   "C18-TRUST-REPORT"
   "C18-RELEASE-GATE"
   "C18-COUNTEREXAMPLE"
   "C18-PLUGIN"
   "C18-BACKEND"])

(def ^:dynamic compiler-verification-diagnostic-messages
  {"C15-SCHEMA" "diagnostic record does not match schema"
   "C15-ID" "diagnostic id is unstable or duplicate"
   "C15-SPAN" "diagnostic primary span is missing"
   "C15-ORIGIN" "diagnostic generated-origin chain is missing"
   "C15-FACTS" "diagnostic facts are missing or unstructured"
   "C15-REMEDIATION" "diagnostic remediation is missing"
   "C15-REDACTION" "diagnostic leaks secret or private data"
   "C15-ORDER" "diagnostic stream order is nondeterministic"
   "C15-GOLDEN" "golden diagnostic fixture does not match"
   "C16-KEY" "incremental cache key is malformed"
   "C16-ENTRY" "incremental cache entry is malformed"
   "C16-STALE" "stale artifact was reused"
   "C16-PROOF" "stale proof or certificate was reused"
   "C16-SPECULATIVE" "speculative cache reached publish boundary"
   "C16-REPLAY" "build-effect replay record is missing"
   "C16-POLICY" "cache crossed incompatible policy"
   "C16-DIAGNOSTIC" "stale diagnostic stream was reused"
   "C16-GRAPH" "incremental dependency graph is inconsistent"
   "C17-MANIFEST" "compiler plugin manifest is malformed"
   "C17-API" "compiler plugin API version is incompatible"
   "C17-CAPABILITY" "compiler plugin capabilities are missing or excessive"
   "C17-BUILD-EFFECT" "compiler plugin requested ungranted build effects"
   "C17-SANDBOX" "compiler plugin violated sandbox policy"
   "C17-PASS-CONTRACT" "compiler plugin pass contract is invalid"
   "C17-OUTPUT" "compiler plugin output failed verification"
   "C17-DOMAIN" "compiler plugin domain IR registration is invalid"
   "C17-FACET" "compiler plugin facet registration is invalid"
   "C17-TRUST" "compiler plugin trust or signature is rejected"
   "C18-RISK" "pass risk classification is missing"
   "C18-EVIDENCE" "required compiler verification evidence is missing"
   "C18-VALIDATION" "translation validation failed"
   "C18-PROOF" "proof or certificate was rejected"
   "C18-TRUST-REPORT" "compiler trust report is incomplete"
   "C18-RELEASE-GATE" "release gate is blocked by verification gaps"
   "C18-COUNTEREXAMPLE" "counterexample artifact is malformed"
   "C18-PLUGIN" "plugin evidence is below policy"
   "C18-BACKEND" "backend lowering conformance is incomplete"})

(def ^:dynamic compiler-verification-override-diagnostics
  {:c15-schema ["C15-SCHEMA" :diagnostic-schema]
   :c15-id ["C15-ID" :diagnostic-id]
   :c15-span ["C15-SPAN" :diagnostic-span]
   :c15-origin ["C15-ORIGIN" :diagnostic-origin]
   :c15-facts ["C15-FACTS" :diagnostic-facts]
   :c15-remediation ["C15-REMEDIATION" :diagnostic-remediation]
   :c15-redaction ["C15-REDACTION" :diagnostic-redaction]
   :c15-order ["C15-ORDER" :diagnostic-order]
   :c15-golden ["C15-GOLDEN" :diagnostic-golden]
   :c16-key ["C16-KEY" :cache-key]
   :c16-entry ["C16-ENTRY" :cache-entry]
   :c16-stale ["C16-STALE" :cache-stale]
   :c16-proof ["C16-PROOF" :cache-proof]
   :c16-speculative ["C16-SPECULATIVE" :cache-speculative]
   :c16-replay ["C16-REPLAY" :cache-replay]
   :c16-policy ["C16-POLICY" :cache-policy]
   :c16-diagnostic ["C16-DIAGNOSTIC" :cache-diagnostic]
   :c16-graph ["C16-GRAPH" :cache-graph]
   :c17-manifest ["C17-MANIFEST" :plugin-manifest]
   :c17-api ["C17-API" :plugin-api]
   :c17-capability ["C17-CAPABILITY" :plugin-capability]
   :c17-build-effect ["C17-BUILD-EFFECT" :plugin-build-effect]
   :c17-sandbox ["C17-SANDBOX" :plugin-sandbox]
   :c17-pass-contract ["C17-PASS-CONTRACT" :plugin-pass-contract]
   :c17-output ["C17-OUTPUT" :plugin-output]
   :c17-domain ["C17-DOMAIN" :plugin-domain]
   :c17-facet ["C17-FACET" :plugin-facet]
   :c17-trust ["C17-TRUST" :plugin-trust]
   :c18-risk ["C18-RISK" :pass-risk]
   :c18-evidence ["C18-EVIDENCE" :pass-evidence]
   :c18-validation ["C18-VALIDATION" :translation-validation]
   :c18-proof ["C18-PROOF" :verification-proof]
   :c18-trust-report ["C18-TRUST-REPORT" :trust-report]
   :c18-release-gate ["C18-RELEASE-GATE" :release-gate]
   :c18-counterexample ["C18-COUNTEREXAMPLE" :counterexample]
   :c18-plugin ["C18-PLUGIN" :plugin-evidence]
   :c18-backend ["C18-BACKEND" :backend-conformance]})

(def ^:private namespace-contract
  {:contract-boundary :hosted-stage0-compiler-verification-shared
   :dependency-direction {:requires []
                          :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :owns [:shared-hosted-c15-c18-diagnostic-catalogs]
   :does-not-own [:canonical-c15-authority :canonical-c16-authority
                  :canonical-c17-authority :canonical-c18-authority
                  :diagnostic-rendering-authority :cache-authority
                  :plugin-authority :proof-authority :release-authority]
   :compatibility-only? true
   :canonical-authority? false})
(def public-api
  {'public-api {:kind :contract}
   'shared-contract {:arglists '([])}
   'compiler-verification-diagnostic-ids {:kind :constant}
   'compiler-verification-diagnostic-messages {:kind :constant}
   'compiler-verification-override-diagnostics {:kind :constant}})
(defn shared-contract []
  (assoc namespace-contract :public-api public-api))
