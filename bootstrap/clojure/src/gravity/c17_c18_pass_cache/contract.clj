(ns gravity.c17-c18-pass-cache.contract
  "Static non-authoritative C17/C18 pass-cache contracts.")

(def c15-output-facts
  #{:source-spans :origin-chain :profile-context :target-context
    :lowering-artifact :provenance :proofs :diagnostic-stream})

(def c16-output-facts
  (into c15-output-facts
        #{:cache-key-schema :invalidation-trace :revalidation-report}))

(def c17-output-facts
  (into c16-output-facts
        #{:plugin-manifest :plugin-api-compatibility :plugin-grants
          :plugin-pass-contracts :plugin-execution-trace
          :plugin-output-verification}))

(def c18-output-facts
  (into c17-output-facts
        #{:pass-risk-classification :pass-evidence-records
          :translation-validation :compiler-trust-report
          :release-gate-report :counterexample-regressions}))

(def c17-pass-contract
  {:pass :c17-compiler-plugin
   :version "stage0-c17-cache-v1"
   :order 17
   :input :gravity/stage0-c16-incremental-compilation-artifact
   :output :gravity/stage0-c17-compiler-plugin-artifact
   :requires #{:cache-key-schema :revalidation-report
               :diagnostic-stream :profile-context :target-context}
   :preserves c16-output-facts
   :invalidates #{}
   :regenerates #{:plugin-manifest :plugin-api-compatibility :plugin-grants
                  :plugin-pass-contracts :plugin-execution-trace
                  :plugin-output-verification}
   :replacement-evidence {}
   :emits #{:plugin-manifest :plugin-api-compatibility :plugin-grants
            :plugin-pass-contracts :plugin-execution-trace
            :plugin-output-verification}
   :effects #{}
   :capabilities #{}
   :profiles #{:hosted}
   :required-evidence #{}
   :verifier-required? false
   :authority-ceiling :none})

(def c18-pass-contract
  {:pass :c18-compiler-verification
   :version "stage0-c18-cache-v1"
   :order 18
   :input :gravity/stage0-c17-compiler-plugin-artifact
   :output :gravity/stage0-c18-compiler-verification-artifact
   :requires #{:plugin-manifest :plugin-pass-contracts
               :plugin-execution-trace :plugin-output-verification}
   :preserves c17-output-facts
   :invalidates #{}
   :regenerates #{:pass-risk-classification :pass-evidence-records
                  :translation-validation :compiler-trust-report
                  :release-gate-report :counterexample-regressions}
   :replacement-evidence {}
   :emits #{:pass-risk-classification :pass-evidence-records
            :translation-validation :compiler-trust-report
            :release-gate-report :counterexample-regressions}
   :effects #{}
   :capabilities #{}
   :profiles #{:hosted}
   :required-evidence #{}
   :verifier-required? false
   :authority-ceiling :none})

(def public-api
  {'c17-c18-pass-cache-contract {:arglists '([])}
   'c17-stage-request {:arglists '([context upstream-result])}
   'c18-stage-request {:arglists '([context c17-receipt])}
   'lookup-or-compute! {:arglists '([store upstream-result context operations])}})

(def namespace-contract
  {:namespace 'gravity.c17-c18-pass-cache
   :contract-boundary :hosted-stage0-c17-c18-generic-v2-cache-integration
   :public-api public-api
   :owns [:exact-c17-c18-c16-invalidator-projection
          :upstream-evidence-root-revalidation
          :c16-to-c17-to-c18-receipt-edges
          :four-pass-evidence-root]
   :does-not-own [:c17-pass-semantics :c18-pass-semantics
                  :plugin-loading :sandbox-enforcement
                  :artifact-identity-policy :proof-checking-authority
                  :translation-validation-authority :release-gate-authority
                  :release-authority :self-hosting-authority]
   :dependency-direction
   {:requires ['clojure.core 'clojure.edn
               'gravity.pass-cache 'gravity.pass-execution]
    :forbids ['gravity.bootstrap 'gravity.c17-plugin
              'gravity.c18-verification]}
   :authority {:ceiling :none
               :local-development-only? true
               :speculative-only? true
               :authoritative? false
               :proof? false
               :release? false
               :self-hosting? false}
   :pass-contracts [c17-pass-contract c18-pass-contract]})
