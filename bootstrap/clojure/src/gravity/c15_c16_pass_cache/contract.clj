(ns gravity.c15-c16-pass-cache.contract
  "Static non-authoritative C15/C16 pass-cache contracts.")

(def c15-input-facts
  #{:source-spans :origin-chain :profile-context :target-context
    :lowering-artifact :provenance :proofs :unstructured-diagnostics})

(def c15-output-facts
  #{:source-spans :origin-chain :profile-context :target-context
    :lowering-artifact :provenance :proofs :diagnostic-stream})

(def c15-pass-contract
  {:pass :c15-compiler-diagnostics
   :version "stage0-c15-cache-v1"
   :order 15
   :input :gravity/stage0-c14-target-lowering-artifact
   :output :gravity/stage0-c15-compiler-diagnostics-artifact
   :requires #{:lowering-artifact :source-spans :profile-context
               :target-context :unstructured-diagnostics}
   :preserves #{:source-spans :origin-chain :profile-context :target-context
                :lowering-artifact :provenance :proofs}
   :invalidates #{:unstructured-diagnostics}
   :regenerates #{:diagnostic-stream}
   :replacement-evidence
   {:unstructured-diagnostics :diagnostic-schema}
   :emits #{:diagnostic-stream :diagnostic-schema :diagnostic-catalog
            :diagnostic-verification-report}
   :effects #{}
   :capabilities #{}
   :profiles #{:hosted}
   :required-evidence #{}
   :verifier-required? false
   :authority-ceiling :none})

(def c16-pass-contract
  {:pass :c16-incremental-compilation
   :version "stage0-c16-cache-v1"
   :order 16
   :input :gravity/stage0-c15-compiler-diagnostics-artifact
   :output :gravity/stage0-c16-incremental-compilation-artifact
   :requires #{:diagnostic-stream :source-spans :profile-context
               :target-context :provenance :proofs}
   :preserves c15-output-facts
   :invalidates #{}
   :regenerates #{:cache-key-schema :invalidation-trace
                  :revalidation-report}
   :replacement-evidence {}
   :emits #{:incremental-dependency-graph :cache-key-schema
            :cache-entry-manifest :invalidation-trace
            :artifact-reuse-report :revalidation-report}
   :effects #{}
   :capabilities #{}
   :profiles #{:hosted}
   :required-evidence #{}
   :verifier-required? false
   :authority-ceiling :none})

(def public-api
  {'c15-c16-pass-cache-contract {:arglists '([])}
   'c15-stage-request {:arglists '([context])}
   'c16-stage-request {:arglists '([context c15-artifact-id])}
   'lookup-or-compute! {:arglists '([store context operations])}})

(def namespace-contract
  {:namespace 'gravity.c15-c16-pass-cache
   :contract-boundary :hosted-stage0-c15-c16-generic-v2-cache-integration
   :public-api public-api
   :owns [:exact-c15-c16-c16-invalidator-projection
          :adjacent-pass-cache-orchestration
          :c15-to-c16-receipt-edge
          :two-pass-evidence-root]
   :does-not-own [:c15-pass-semantics :c16-pass-semantics
                  :artifact-identity-policy :diagnostic-policy
                  :compiler-authority :proof-authority :release-authority
                  :equivalence-authority :self-hosting-authority
                  :cache-storage-implementation]
   :dependency-direction
   {:requires ['clojure.core 'clojure.edn
               'gravity.pass-cache 'gravity.pass-execution]
    :forbids ['gravity.bootstrap 'gravity.c15-diagnostics
              'gravity.c16-incremental]}
   :authority {:ceiling :none
               :local-development-only? true
               :speculative-only? true
               :authoritative? false
               :proof? false
               :release? false
               :equivalence? false
               :self-hosting? false}
   :pass-contracts [c15-pass-contract c16-pass-contract]
   :c16-invalidator-fields
   [:c14-artifact-id :compiler-id :capability-policy-id :facet-set-id
    :provider-manifest-id :package-lock-id :diagnostic-schema-id
    :dependency-graph-id :build-effect-replay-id :profile-id :target-id
    :policy-ids :provenance-id :producer-binding-id
    :validation-binding-id :authority-scope]})
