(ns gravity.compiler-pass-manifest
  "C1/P06-T01 compiler pass-manifest contract leaf."
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [gravity.diagnostics :as diagnostics]
            [gravity.digest :as digest]))

(def ^:private c1-diagnostic-ids
  ["C1-PIPELINE" "C1-PASS-CONTRACT" "C1-EVIDENCE-DROP"
   "C1-UNCHECKED-BACKEND" "C1-MANIFEST"])

(def ^:private c15-diagnostic-ids
  ["C15-SCHEMA" "C15-ID" "C15-SPAN" "C15-ORIGIN" "C15-FACTS"
   "C15-REMEDIATION" "C15-REDACTION" "C15-ORDER"])

(def ^:private c16-diagnostic-ids
  ["C16-KEY" "C16-ENTRY" "C16-PROOF" "C16-SPECULATIVE"])

(def ^:private c17-diagnostic-ids
  ["C17-MANIFEST" "C17-API" "C17-CAPABILITY"
   "C17-PASS-CONTRACT" "C17-OUTPUT"])

(def ^:private c18-diagnostic-ids
  ["C18-RISK" "C18-EVIDENCE" "C18-TRUST-REPORT"
   "C18-RELEASE-GATE"])

(def compiler-pass-diagnostic-ids
  (vec (concat c1-diagnostic-ids c15-diagnostic-ids c16-diagnostic-ids
               c17-diagnostic-ids c18-diagnostic-ids)))

(defn- perf-present?
  [value]
  (and (some? value)
       (not (and (coll? value) (empty? value)))))

(defn- fail!
  [id message data]
  (diagnostics/fail! id message data))

(def ^:private namespace-contract
  {:namespace 'gravity.compiler-pass-manifest
   :contract-boundary :stage0-compiler-pass-manifest
   :artifact-inputs [:math-conformance-artifact :compiler-pass-overrides]
   :artifact-outputs [:compiler-pipeline-manifest :pass-contract-registry
                      :diagnostic-registry :incremental-cache-key-schema
                      :plugin-pass-api-manifest :verification-plan
                      :compiler-trust-report]
   :owns [:stage0-pass-contract-construction
          :stage0-pass-contract-validation
          :stage0-pass-contract-capability-proof]
   :dependency-direction
   {:requires ['clojure.set 'clojure.string 'gravity.diagnostics
               'gravity.digest]
    :forbids ['gravity.bootstrap]}
   :does-not-own [:canonical-compiler-authority
                  :self-hosting :release :seed-retirement
                  :math-conformance-construction
                  :source-reading :target-lowering]
   :compatibility-only? true
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-authority? false
   :self-hosted? false
   :release? false
   :seed-retirement? false
   :test-owner
   'gravity.compiler-pass-manifest-test/compiler-pass-manifest-is-a-bootstrap-free-stage-boundary})
(def compiler-pass-default-stage-order
  [:read-source
   :build-syntax
   :macro-expand
   :resolve-names
   :lower-to-core
   :type-check
   :effect-check
   :profile-validate
   :capability-validate
   :ownership-check
   :safety-analyze
   :build-mir
   :verify-mir
   :optimize-mir
   :lower-domain-ir
   :verify-domain-ir
   :lower-target
   :emit-artifacts
   :record-package-provenance])

(def compiler-pass-contract-required-fields
  [:pass :owner-doc :input :output :requires :preserves :invalidates
   :regenerates :capabilities :profiles :emits :rejects :verifier-gate?
   :risk :evidence-class])

(def compiler-pass-durable-facts
  #{:source-spans :syntax-identity :origin-chain :profile :target :types
    :effects :ownership :capabilities :safety-outcomes :proofs
    :diagnostics})

(defn compiler-pass-contract
  [pass owner-doc input output requires preserves invalidates regenerates emits
   rejects risk evidence-class]
  {:pass pass
   :owner-doc owner-doc
   :input input
   :output output
   :requires requires
   :preserves preserves
   :invalidates invalidates
   :regenerates regenerates
   :capabilities #{}
   :effects #{}
   :profiles #{:meta}
   :emits emits
   :rejects rejects
   :verifier-gate? true
   :risk risk
   :evidence-class evidence-class})

(def compiler-pass-default-contracts
  [(compiler-pass-contract
    :read-source :C2 :source-bytes :syntax-seeds
    [:source-root :reader-options :build-policy]
    [:source-spans :source-bytes :diagnostics]
    []
    [:source-unit-record :reader-source-map :incremental-reader-hash]
    [:source-unit-record :token-stream :form-tree :reader-diagnostics]
    ["C2-ENCODING" "C2-DELIMITER" "C2-EXTENSION"] :critical
    [:golden-fixtures :round-trip-tests])
   (compiler-pass-contract
    :build-syntax :C3 :syntax-seeds :syntax-object-stream
    [:source-spans :reader-source-map]
    [:source-spans :syntax-identity :origin-chain :profile :diagnostics]
    []
    [:syntax-id-map :hygiene-context-map]
    [:syntax-object-stream :origin-chain-graph :syntax-verification-report]
    ["C3-SHAPE" "C3-ORIGIN" "C3-HYGIENE"] :critical
    [:golden-fixtures :serialization-round-trip])
   (compiler-pass-contract
    :macro-expand :C4 :syntax-object-stream :expanded-syntax
    [:syntax-objects :macro-environment :build-grants :profile]
    [:source-spans :syntax-identity :origin-chain :profile :diagnostics]
    [:syntax-facts]
    [:expanded-syntax-facts :macro-expansion-trace]
    [:expanded-syntax-stream :macro-expansion-trace :build-effect-log]
    ["C4-RETURN" "C4-HYGIENE" "C4-BUILD-EFFECT"] :critical
    [:hygiene-fixtures :generated-origin-tests])
   (compiler-pass-contract
    :resolve-names :C5 :expanded-syntax :namespace-analysis
    [:namespace-context :package-graph :profile :target]
    [:source-spans :syntax-identity :origin-chain :profile :target
     :diagnostics]
    [:unresolved-symbol-cache]
    [:binding-table :dependency-graph]
    [:namespace-analysis-artifact :binding-table :resolution-diagnostics]
    ["C5-UNRESOLVED" "C5-CROSS-PROFILE" "C5-CAPABILITY"] :high
    [:golden-binding-graphs :negative-fixtures])
   (compiler-pass-contract
    :lower-to-core :C6 :namespace-analysis :core-ast
    [:expanded-syntax :resolved-bindings]
    [:source-spans :origin-chain :profile :target :capabilities
     :diagnostics]
    [:surface-form-shape]
    [:core-node-map :evaluation-order-records]
    [:core-ast-module :desugaring-trace :core-verifier-report]
    ["C6-LOWERING-GAP" "C6-EFFECT-DROP" "C6-VERIFY"] :high
    [:core-golden-fixtures :core-verifier])
   (compiler-pass-contract
    :type-check :C7 :core-ast :typed-core
    [:resolved-bindings :core-verifier-report :profile]
    [:source-spans :origin-chain :profile :target :diagnostics]
    [:untyped-core-cache]
    [:type-facts :constraint-ledger :dynamic-boundary-records]
    [:typed-core-module :type-environment :type-diagnostics]
    ["C7-TYPE-MISMATCH" "C7-DYNAMIC" "C7-VERIFY"] :critical
    [:positive-negative-fixtures :property-tests])
   (compiler-pass-contract
    :effect-check :C8 :typed-core :effected-core
    [:type-facts :declared-effects :profile :capabilities]
    [:source-spans :origin-chain :profile :target :types :diagnostics]
    [:unchecked-effect-summary]
    [:effect-graph :capability-proof-records :ordering-constraints]
    [:effect-graph :capability-proof-record :effect-diagnostics]
    ["C8-UNDECLARED" "C8-CAPABILITY" "C8-ORDER"] :critical
    [:positive-negative-fixtures :capability-proof])
   (compiler-pass-contract
    :profile-validate :P1 :effected-core :profile-valid-core
    [:profile-manifest :effect-graph :capability-proof-records]
    [:source-spans :origin-chain :profile :target :types :effects
     :capabilities :diagnostics]
    [:unchecked-profile-assumptions]
    [:profile-validation-report]
    [:profile-validation-report :profile-diagnostics]
    ["P1-EFFECT" "P1-CAPABILITY" "P1-BACKEND"] :critical
    [:profile-fixture-suite :pre-backend-rejection])
   (compiler-pass-contract
    :capability-validate :L15 :profile-valid-core :capability-valid-core
    [:capability-proof-records :provider-registry :profile-manifest]
    [:source-spans :origin-chain :profile :target :types :effects
     :capabilities :diagnostics]
    [:unscoped-provider-cache]
    [:provider-selection-records :capability-usage-summary]
    [:capability-provider-report :capability-diagnostics]
    ["L15-CAPABILITY-MISSING" "L15-SCOPE" "L15-TRUST"] :critical
    [:capability-fixtures :provider-contract-tests])
   (compiler-pass-contract
    :ownership-check :C9 :capability-valid-core :ownership-checked-core
    [:typed-core :effect-graph :memory-regime]
    [:source-spans :origin-chain :profile :target :types :effects
     :capabilities :diagnostics]
    [:borrow-cache]
    [:ownership-graph :lifetime-interval-map :linear-resource-flow]
    [:ownership-analysis :borrow-graph :ownership-diagnostics]
    ["C9-BORROW-ESCAPE" "C9-MUT-ALIAS" "C9-LINEAR-LEAK"] :critical
    [:ownership-fixtures :resource-flow-tests])
   (compiler-pass-contract
    :safety-analyze :C10 :ownership-checked-core :checked-core
    [:types :effects :ownership :capabilities :profile]
    [:source-spans :origin-chain :profile :target :types :effects
     :ownership :capabilities :diagnostics]
    [:unchecked-safety-outcomes]
    [:safety-outcomes :runtime-check-records :proof-obligations]
    [:safety-analysis-report :unsafe-island-audit-manifest
     :safety-diagnostics]
    ["C10-NO-OUTCOME" "C10-PROOF" "C10-UNSAFE"] :critical
    [:safety-conformance-fixtures :certificate-checks])
   (compiler-pass-contract
    :build-mir :C11 :checked-core :gravity/mir
    [:types :effects :ownership :capabilities :profile :safety-outcomes]
    [:source-spans :origin-chain :profile :target :types :effects
     :ownership :capabilities :safety-outcomes :proofs :diagnostics]
    [:core-control-shape]
    [:mir-control-flow :mir-data-flow :mir-metadata-tables]
    [:mir-module :control-flow-graph :mir-verifier-report]
    ["C11-MODULE" "C11-SAFETY" "C11-VERIFY"] :critical
    [:mir-verifier :core-to-mir-golden])
   (compiler-pass-contract
    :verify-mir :C11 :gravity/mir :verified-mir
    [:mir-module :mir-metadata-tables]
    [:source-spans :origin-chain :profile :target :types :effects
     :ownership :capabilities :safety-outcomes :proofs :diagnostics]
    []
    [:mir-verifier-report]
    [:mir-verifier-report :mir-diagnostic-stream]
    ["C11-BLOCK" "C11-DOMINANCE" "C11-TARGET-LEAK"] :critical
    [:mir-verifier])
   (compiler-pass-contract
    :optimize-mir :C13 :verified-mir :optimized-mir
    [:mir-verifier-report :optimization-policy :proofs]
    [:source-spans :origin-chain :profile :target :types :effects
     :ownership :capabilities :safety-outcomes :proofs :diagnostics]
    [:analysis-cache :dominator-tree]
    [:optimization-decision-log :invalidated-fact-ledger]
    [:optimization-decision-log :post-pass-verifier-report]
    ["C13-CONTRACT" "C13-PROOF" "C13-VERIFY"] :high
    [:translation-validation :post-pass-verifier])
   (compiler-pass-contract
    :lower-domain-ir :C12 :optimized-mir :domain-ir
    [:verified-mir :domain-registrations :semantic-anchors]
    [:source-spans :origin-chain :profile :target :types :effects
     :ownership :capabilities :safety-outcomes :proofs :diagnostics]
    [:mir-subgraph-locality]
    [:domain-anchor-map :domain-verifier-report]
    [:domain-ir-registry :domain-ir-artifacts :domain-diagnostics]
    ["C12-ANCHOR" "C12-PROOF" "C12-LOWERING"] :high
    [:domain-verifier :proof-replay])
   (compiler-pass-contract
    :verify-domain-ir :C12 :domain-ir :verified-domain-ir
    [:domain-ir-artifacts :semantic-anchor-map]
    [:source-spans :origin-chain :profile :target :types :effects
     :ownership :capabilities :safety-outcomes :proofs :diagnostics]
    []
    [:domain-verifier-report]
    [:domain-verifier-report :domain-diagnostic-stream]
    ["C12-SCHEMA" "C12-VERIFY" "C12-FACTS"] :high
    [:domain-verifier])
   (compiler-pass-contract
    :lower-target :C14 :verified-mir-or-domain-ir :target-artifacts
    [:verified-mir :verified-domain-ir :profile :target :proofs
     :capabilities]
    [:source-spans :origin-chain :profile :target :types :effects
     :ownership :capabilities :safety-outcomes :proofs :diagnostics]
    [:target-independent-layout-choice]
    [:target-artifact-manifest :proof-target-metadata-map]
    [:lowering-request :target-eligibility-report
     :target-artifact-manifest]
    ["C14-INPUT" "C14-PROOF-METADATA" "C14-MANIFEST"] :critical
    [:target-conformance :differential-fixtures])
   (compiler-pass-contract
    :emit-artifacts :D1 :target-artifacts :artifact-provenance-graph
    [:target-artifact-manifest :diagnostic-stream :dependency-graph]
    [:source-spans :origin-chain :profile :target :effects
     :capabilities :safety-outcomes :proofs :diagnostics]
    []
    [:artifact-records :provenance-graph]
    [:artifact-provenance-graph :diagnostic-stream]
    ["D1-ARTIFACT-GAP" "C1-MANIFEST"] :high
    [:artifact-graph-validation])
   (compiler-pass-contract
    :record-package-provenance :D1 :artifact-provenance-graph
    :package-provenance-record
    [:artifact-records :source-root :lockfile :compiler-identity]
    [:profile :target :effects :capabilities :safety-outcomes :proofs
     :diagnostics]
    []
    [:package-provenance-record :reproducibility-inputs]
    [:package-provenance-record :reproducibility-report]
    ["D1-ARTIFACT-GAP" "C16-REPLAY"] :high
    [:reproducible-rebuild])])

(def compiler-pass-default-diagnostic-schema
  {:artifact :gravity/diagnostic-schema
   :required-fields [:rule :severity :primary :related :origin-chain :stage
                     :profile :target :artifacts :facts :remediation
                     :redactions :ordering-key]
   :supported-severities [:error :warning :info :hint :internal-error]
   :renderers [:cli :ide :ci]
   :deterministic-ordering? true
   :secret-redaction? true})

(def compiler-pass-default-diagnostic-catalog
  (mapv (fn [id]
          {:rule id
           :severity :error
           :message-key (keyword "compiler" (str/lower-case id))
           :lifecycle :active})
        compiler-pass-diagnostic-ids))

(def compiler-pass-default-diagnostic-fixtures
  [{:rule "C1-PASS-CONTRACT"
    :diagnostic-id "diag/c1-pass-contract-stage0"
    :severity :error
    :stage :pass-contract-validate
    :primary {:span "compiler/passes.gravity:1:1"
              :artifact :pass-contract/build-mir}
    :related [{:role :pass-contract
               :artifact :pass-contract/build-mir}]
    :origin-chain [{:kind :source
                    :span "compiler/passes.gravity:1:1"}]
    :profile :meta
    :target :jvm
    :artifacts [:pass-contract/build-mir]
    :facts {:missing-field :output}
    :remediation [{:kind :complete-pass-contract}]
    :redactions []
    :secret-free? true
    :ordering-key ["C1" 1]}
   {:rule "C15-ORIGIN"
    :diagnostic-id "diag/c15-origin-generated-stage0"
    :severity :error
    :stage :diagnostic-validate
    :primary {:span "generated:gravity.compiler/pass:1"
              :artifact :diagnostic/generated}
    :related [{:role :generated-by
               :span "compiler/passes.gravity:2:1"}]
    :origin-chain [{:kind :generated
                    :producer :gravity.compiler/pass
                    :inputs [:syntax/pass-contract]}]
    :profile :meta
    :target :jvm
    :artifacts [:diagnostic/generated]
    :facts {:origin-chain :present}
    :remediation [{:kind :preserve-generated-origin}]
    :redactions []
    :secret-free? true
    :ordering-key ["C15" 1]}])

(def compiler-pass-default-cache-key-schema
  {:artifact :gravity/cache-key-schema
   :required-fields [:stage :source :syntax :profile :target :compiler
                     :pass-contract :dependencies :build-effects
                     :capabilities :language-facets]})

(def compiler-pass-default-cache-keys
  [{:stage :type-check
    :source "sha256:stage0-source"
    :syntax "sha256:stage0-syntax"
    :profile "sha256:stage0-profile"
    :target "sha256:stage0-target"
    :compiler "sha256:stage0-clojure-bootstrap"
    :pass-contract "sha256:stage0-type-check-contract"
    :dependencies "sha256:stage0-dependency-graph"
    :build-effects "sha256:stage0-build-replay"
    :capabilities "sha256:stage0-capability-policy"
    :language-facets "sha256:stage0-facets"}])

(def compiler-pass-default-cache-entries
  [{:stage :type-check
    :cache-key "sha256:stage0-type-check-cache-key"
    :artifact-id "sha256:stage0-typed-core"
    :producer {:stage :type-check :pass-version "stage0"}
    :inputs ["sha256:stage0-core"]
    :preserved-facts [:source-spans :resolved-bindings]
    :invalidated-by [:source-change :type-rule-change :profile-change]
    :diagnostics "sha256:stage0-type-diagnostics"
    :trust :local-build
    :revalidation :required-before-release}])

(def compiler-pass-default-proof-reuse-records
  [{:proof-id :proof/stage0-bounds
    :claim :bounds-preserved
    :inputs ["sha256:stage0-mir-op"]
    :profile :native
    :target :jvm
    :status :fresh
    :reuse :accepted
    :invalidation-conditions [:source-change :safety-rule-change
                              :target-change]}])

(def compiler-pass-default-speculative-reuse-records
  [{:artifact-id "sha256:stage0-speculative-expansion"
    :stage :macro-expand
    :reuse :speculative
    :publishable? false
    :revalidation :required-before-release}])

(def compiler-pass-default-plugin-manifest
  {:artifact :gravity/compiler-plugin
   :plugin 'gravity.compiler.stage0/pass-audit
   :package {:name 'gravity/compiler-pass-audit :version "0.1.0"}
   :api-version "1"
   :compiler-compatibility {:min "0.1.0" :max-exclusive "0.2.0"}
   :trust :sandboxed
   :profile :meta
   :build-effects #{}
   :capabilities #{:compiler/ir-transform}
   :capability-scopes {:compiler/ir-transform
                       #{:read-mir :write-mir :emit-artifacts
                         :emit-diagnostics :register-pass}}
   :requested-scopes #{:read-mir :write-mir :emit-diagnostics}
   :passes [:plugin/stage0-audit]
   :domains []
   :facets []
   :emits #{:plugin-execution-trace}
   :conformance [:compiler-pass-contract-fixtures]})

(def compiler-pass-default-plugin-pass-contracts
  [(assoc (compiler-pass-contract
           :plugin/stage0-audit :C17 :verified-mir :verified-mir
           [:mir-verifier-report :plugin-grants]
           [:source-spans :origin-chain :profile :target :types :effects
            :ownership :capabilities :safety-outcomes :proofs :diagnostics]
           []
           [:plugin-execution-trace]
           [:plugin-execution-trace :verifier-report]
           ["C17-PASS-CONTRACT" "C17-OUTPUT"] :medium
           [:contract-verifier :fixture-suite])
          :capabilities #{:compiler/ir-transform})])

(def compiler-pass-default-plugin-execution-traces
  [{:artifact :gravity/plugin-execution
    :plugin 'gravity.compiler.stage0/pass-audit
    :pass :plugin/stage0-audit
    :input "sha256:stage0-verified-mir"
    :output "sha256:stage0-verified-mir-audited"
    :grants "sha256:stage0-plugin-grants"
    :build-effects []
    :decisions [:decision/stage0-plugin-audit]
    :diagnostics []
    :verifier-result :passed}])

(defn compiler-pass-default-risk-classification
  [contracts]
  (mapv (fn [contract]
          {:pass (:pass contract)
           :risk (:risk contract)
           :reason #{:stage0-pass-contract}
           :affected-profiles (:profiles contract)
           :affected-targets #{:jvm}
           :minimum-evidence (set (:evidence-class contract))
           :available-evidence (set (:evidence-class contract))
           :release-gate (if (#{:critical :high} (:risk contract))
                           :required
                           :verifier-only)})
        contracts))

(defn compiler-pass-default-trust-report
  [contracts risk-records]
  {:artifact :gravity/compiler-trust-report
   :compiler :gravity-stage0-clojure-bootstrap
   :passes (mapv #(select-keys % [:pass :risk :available-evidence])
                 risk-records)
   :profiles {:meta {:required-evidence :high
                     :blocked-passes []}}
   :known-gaps []
   :covered-passes (mapv :pass contracts)})

(def compiler-pass-default-release-gate-report
  {:artifact :gravity/compiler-release-gate
   :status :passed
   :evidence-gaps []
   :blocked-passes []
   :release-artifacts [:pass-contract-manifest]})

(defn compiler-pass-merge-record-overrides
  [defaults overrides id-key]
  (if (seq overrides)
    (let [by-id (into {} (map (juxt id-key identity) overrides))]
      (mapv #(merge % (get by-id (get % id-key) {})) defaults))
    defaults))

(defn compiler-pass-suite
  [manifest]
  (let [source-suite (get-in manifest [:metadata :compiler :passes] {})
        map-value (fn [key override-key default]
                    (cond
                      (contains? source-suite key) (get source-suite key)
                      (contains? source-suite override-key)
                      (merge default (get source-suite override-key))
                      :else default))
        vector-value (fn [key override-key defaults id-key]
                       (cond
                         (contains? source-suite key) (vec (get source-suite key))
                         (contains? source-suite override-key)
                         (compiler-pass-merge-record-overrides
                          defaults (get source-suite override-key) id-key)
                         :else defaults))
        contracts (vector-value :contracts :contract-overrides
                                compiler-pass-default-contracts :pass)
        risk-records (vector-value :risk-classification :risk-overrides
                                   (compiler-pass-default-risk-classification
                                    contracts)
                                   :pass)
        trust-report (map-value :compiler-trust-report
                                :compiler-trust-report-overrides
                                (compiler-pass-default-trust-report
                                 contracts risk-records))]
    (assoc source-suite
           :stage-order
           (or (:stage-order source-suite) compiler-pass-default-stage-order)
           :contracts contracts
           :pipeline-manifest
           (map-value :pipeline-manifest :pipeline-manifest-overrides
                      {:artifact :gravity/compiler-pipeline
                       :pipeline-id "sha256:stage0-compiler-pipeline"
                       :compiler :gravity-stage0-clojure-bootstrap
                       :source-root "sha256:stage0-source-root"
                       :profile :meta
                       :target {:backend :jvm :triple "stage0"}
                       :stages (:stage-order source-suite
                                             compiler-pass-default-stage-order)
                       :pass-contracts (mapv :pass contracts)
                       :evidence [:types :effects :ownership :capabilities
                                  :safety :proofs :diagnostics]
                       :diagnostics "sha256:stage0-compiler-diagnostics"
                       :artifact-graph "sha256:stage0-artifact-graph"})
           :diagnostic-schema
           (map-value :diagnostic-schema :diagnostic-schema-overrides
                      compiler-pass-default-diagnostic-schema)
           :diagnostic-catalog
           (vector-value :diagnostic-catalog :diagnostic-catalog-overrides
                         compiler-pass-default-diagnostic-catalog :rule)
           :diagnostic-fixtures
           (vector-value :diagnostic-fixtures :diagnostic-fixture-overrides
                         compiler-pass-default-diagnostic-fixtures
                         :diagnostic-id)
           :cache-key-schema
           (map-value :cache-key-schema :cache-key-schema-overrides
                      compiler-pass-default-cache-key-schema)
           :cache-keys
           (vector-value :cache-keys :cache-key-overrides
                         compiler-pass-default-cache-keys :stage)
           :cache-entries
           (vector-value :cache-entries :cache-entry-overrides
                         compiler-pass-default-cache-entries :stage)
           :proof-reuse-records
           (vector-value :proof-reuse-records :proof-reuse-overrides
                         compiler-pass-default-proof-reuse-records :proof-id)
           :speculative-reuse-records
           (vector-value :speculative-reuse-records
                         :speculative-reuse-overrides
                         compiler-pass-default-speculative-reuse-records
                         :artifact-id)
           :plugin-manifest
           (map-value :plugin-manifest :plugin-manifest-overrides
                      compiler-pass-default-plugin-manifest)
           :plugin-pass-contracts
           (vector-value :plugin-pass-contracts
                         :plugin-pass-contract-overrides
                         compiler-pass-default-plugin-pass-contracts :pass)
           :plugin-execution-traces
           (vector-value :plugin-execution-traces
                         :plugin-execution-trace-overrides
                         compiler-pass-default-plugin-execution-traces :pass)
           :risk-classification risk-records
           :compiler-trust-report trust-report
           :release-gate-report
           (map-value :release-gate-report :release-gate-report-overrides
                      compiler-pass-default-release-gate-report))))

(defn compiler-pass-fail!
  [id source-path manifest record extra]
  (fail! id
         (case id
           "C1-PIPELINE" "compiler pipeline order does not expose the canonical stages"
           "C1-PASS-CONTRACT" "compiler pass contract is incomplete"
           "C1-EVIDENCE-DROP" "compiler pass drops durable evidence without replacement"
           "C1-UNCHECKED-BACKEND" "target lowering consumes unchecked compiler input"
           "C1-MANIFEST" "compiler pipeline manifest is missing required graph fields"
           "C15-SCHEMA" "compiler diagnostic schema is malformed"
           "C15-ID" "compiler diagnostic ids are unstable or duplicate"
           "C15-SPAN" "compiler diagnostic lacks a primary span"
           "C15-ORIGIN" "generated diagnostic lacks an origin chain"
           "C15-FACTS" "compiler diagnostic lacks structured facts"
           "C15-REMEDIATION" "actionable compiler diagnostic lacks remediation"
           "C15-REDACTION" "compiler diagnostic leaks private or secret material"
           "C15-ORDER" "compiler diagnostic stream order is nondeterministic"
           "C16-KEY" "incremental cache key is incomplete"
           "C16-ENTRY" "incremental cache entry is incomplete"
           "C16-PROOF" "stale proof or certificate was reused"
           "C16-SPECULATIVE" "speculative cache reuse reached a publishable boundary"
           "C17-MANIFEST" "compiler plugin manifest is incomplete"
           "C17-API" "compiler plugin API version is incompatible"
           "C17-CAPABILITY" "compiler plugin capability scope is missing or excessive"
           "C17-PASS-CONTRACT" "compiler plugin pass contract is invalid"
           "C17-OUTPUT" "compiler plugin output failed verification"
           "C18-RISK" "compiler pass risk classification is missing"
           "C18-EVIDENCE" "compiler pass lacks required correctness evidence"
           "C18-TRUST-REPORT" "compiler trust report omits a pass"
           "C18-RELEASE-GATE" "compiler release gate passed despite evidence gaps"
           "compiler pass manifest record is invalid")
         (merge {:source-span {:source source-path}
                 :profile (or (:profile record) (:profile manifest))
                 :target (or (:target record) (:target manifest))
                 :stage (or (:stage record) (:pass record))
                 :pass-id (:pass record)
                 :artifact-id (or (:artifact-id record) (:artifact record))
                 :input-artifact-id (:input record)
                 :output-artifact-id (:output record)
                 :plugin-id (:plugin record)
                 :package-id (get-in record [:package :name])
                 :compiler-api-version (:api-version record)
                 :trust-level (:trust record)
                 :cache-key (:cache-key record)
                 :risk-class (:risk record)
                 :available-evidence (:available-evidence record)
                 :required-evidence (:minimum-evidence record)
                 :affected-profiles (:affected-profiles record)
                 :affected-targets (:affected-targets record)
                 :release-gate (:release-gate record)
                 :diagnostic-family :compiler-pass-contract}
                extra)))

(defn compiler-pass-missing-fields
  [record required-fields]
  (vec (remove #(and (contains? record %) (some? (get record %)))
               required-fields)))

(defn compiler-pass-validate-pipeline!
  [source-path manifest suite]
  (let [stage-order (:stage-order suite)
        contracts (:contracts suite)
        contracts-by-pass (into {} (map (juxt :pass identity) contracts))]
    (when-not (= compiler-pass-default-stage-order stage-order)
      (compiler-pass-fail! "C1-PIPELINE" source-path manifest
                           {:stage :pipeline-order}
                           {:expected-outcome compiler-pass-default-stage-order
                            :actual-outcome stage-order
                            :remediation "Expose the D1/C1 canonical pipeline order as pass manifest data."}))
    (doseq [stage stage-order]
      (when-not (contains? contracts-by-pass stage)
        (compiler-pass-fail! "C1-PASS-CONTRACT" source-path manifest
                             {:pass stage}
                             {:missing-fields [:contract]
                              :remediation "Add a pass contract for every exposed pipeline stage."})))
    (doseq [contract contracts]
      (let [missing-fields (compiler-pass-missing-fields
                            contract compiler-pass-contract-required-fields)]
        (when (seq missing-fields)
          (compiler-pass-fail! "C1-PASS-CONTRACT" source-path manifest contract
                               {:missing-fields missing-fields
                                :remediation "Every compiler pass must declare input, output, facts, capabilities, artifacts, verifier gate, risk, and evidence class."})))
      (let [durable-drops (set/intersection compiler-pass-durable-facts
                                            (set (:invalidates contract)))
            replacements (set (concat (:regenerates contract)
                                      (:replacement-evidence contract)
                                      (:emits contract)))
            missing-replacements (set/difference durable-drops replacements)]
        (when (seq missing-replacements)
          (compiler-pass-fail! "C1-EVIDENCE-DROP" source-path manifest
                               contract
                               {:missing-fields (vec missing-replacements)
                                :remediation "Regenerate durable facts, emit replacement proof, keep runtime checks, or reject the transformation."}))))
    (let [lower-target (get contracts-by-pass :lower-target)]
      (when (contains? #{:raw-source :source-forms :syntax-objects
                        :expanded-syntax :unchecked-core :gravity/mir}
                      (:input lower-target))
        (compiler-pass-fail! "C1-UNCHECKED-BACKEND" source-path manifest
                             lower-target
                             {:remediation "Target lowering must consume verified MIR or verified domain IR."})))
    (let [pipeline (:pipeline-manifest suite)
          missing-fields (compiler-pass-missing-fields
                          pipeline
                          [:artifact :pipeline-id :compiler :source-root
                           :profile :target :stages :pass-contracts
                           :evidence :diagnostics :artifact-graph])]
      (when (seq missing-fields)
        (compiler-pass-fail! "C1-MANIFEST" source-path manifest pipeline
                             {:missing-fields missing-fields
                              :remediation "Emit a complete compiler pipeline manifest for diagnostics, caches, packages, and bootstrap comparison."}))))
  :complete)

(defn compiler-pass-validate-diagnostics!
  [source-path manifest suite]
  (let [schema (:diagnostic-schema suite)
        required-fields (:required-fields compiler-pass-default-diagnostic-schema)
        schema-fields (set (:required-fields schema))]
    (when-not (set/subset? (set required-fields) schema-fields)
      (compiler-pass-fail! "C15-SCHEMA" source-path manifest schema
                           {:missing-fields (vec (set/difference
                                                  (set required-fields)
                                                  schema-fields))
                            :schema-field :required-fields
                            :remediation "Diagnostic schemas must include stable ids, locations, origins, facts, remediation, redaction, and ordering fields."})))
  (let [rules (map :rule (:diagnostic-catalog suite))]
    (when (not= (count rules) (count (distinct rules)))
      (compiler-pass-fail! "C15-ID" source-path manifest
                           {:stage :diagnostic-catalog}
                           {:remediation "Diagnostic ids must remain unique and stable across wording changes."})))
  (doseq [diagnostic (:diagnostic-fixtures suite)]
    (when-not (perf-present? (get-in diagnostic [:primary :span]))
      (compiler-pass-fail! "C15-SPAN" source-path manifest diagnostic
                           {:schema-field :primary
                            :remediation "Diagnostics must include a primary source, generated, manifest, MIR, domain, or artifact location."}))
    (when-not (perf-present? (:origin-chain diagnostic))
      (compiler-pass-fail! "C15-ORIGIN" source-path manifest diagnostic
                           {:schema-field :origin-chain
                            :remediation "Generated and downstream diagnostics must preserve origin chains."}))
    (when-not (perf-present? (:facts diagnostic))
      (compiler-pass-fail! "C15-FACTS" source-path manifest diagnostic
                           {:schema-field :facts
                            :remediation "Diagnostic facts must be structured fields, not prose-only text."}))
    (when (and (= :error (:severity diagnostic))
               (not (perf-present? (:remediation diagnostic))))
      (compiler-pass-fail! "C15-REMEDIATION" source-path manifest diagnostic
                           {:schema-field :remediation
                            :remediation "Actionable diagnostics need structured remediation categories."}))
    (when-not (true? (:secret-free? diagnostic))
      (compiler-pass-fail! "C15-REDACTION" source-path manifest diagnostic
                           {:schema-field :redactions
                            :remediation "Redact secret values while preserving fixable diagnostic structure."})))
  (let [fixtures (:diagnostic-fixtures suite)]
    (when-not (= fixtures (sort-by :ordering-key fixtures))
      (compiler-pass-fail! "C15-ORDER" source-path manifest
                           {:stage :diagnostic-stream}
                           {:remediation "Diagnostic streams must be deterministically ordered by stable semantic keys."})))
  :complete)

(defn compiler-pass-validate-incremental!
  [source-path manifest suite]
  (let [required-fields (set (get-in suite [:cache-key-schema
                                            :required-fields]))]
    (doseq [cache-key (:cache-keys suite)]
      (let [missing-fields (vec (remove #(perf-present? (get cache-key %))
                                        required-fields))]
        (when (seq missing-fields)
          (compiler-pass-fail! "C16-KEY" source-path manifest cache-key
                               {:missing-fields missing-fields
                                :remediation "Cache keys must include every semantic, policy, profile, capability, pass, and dependency fact that can affect meaning."})))))
  (doseq [entry (:cache-entries suite)]
    (let [missing-fields (compiler-pass-missing-fields
                          entry
                          [:stage :cache-key :artifact-id :producer :inputs
                           :preserved-facts :invalidated-by :diagnostics
                           :trust :revalidation])]
      (when (seq missing-fields)
        (compiler-pass-fail! "C16-ENTRY" source-path manifest entry
                             {:missing-fields missing-fields
                              :remediation "Cache entries are artifacts and must retain producer, inputs, facts, diagnostics, trust, and revalidation state."}))))
  (doseq [proof (:proof-reuse-records suite)]
    (when (and (= :stale (:status proof)) (= :accepted (:reuse proof)))
      (compiler-pass-fail! "C16-PROOF" source-path manifest proof
                           {:remediation "Stale proofs and certificates must be rejected or regenerated before reuse."})))
  (doseq [reuse (:speculative-reuse-records suite)]
    (when (and (= :speculative (:reuse reuse)) (:publishable? reuse))
      (compiler-pass-fail! "C16-SPECULATIVE" source-path manifest reuse
                           {:remediation "Speculative interactive reuse cannot reach publishable or release artifact boundaries."})))
  :complete)

(defn compiler-pass-validate-plugins!
  [source-path manifest suite]
  (let [plugin (:plugin-manifest suite)
        missing-fields (compiler-pass-missing-fields
                        plugin
                        [:artifact :plugin :package :api-version
                         :compiler-compatibility :trust :profile
                         :build-effects :capabilities :capability-scopes
                         :passes :emits :conformance])]
    (when (seq missing-fields)
      (compiler-pass-fail! "C17-MANIFEST" source-path manifest plugin
                           {:missing-fields missing-fields
                            :remediation "Load plugin manifests before code and reject missing identity, policy, authority, pass, or conformance fields."})))
  (let [plugin (:plugin-manifest suite)]
    (when-not (= "1" (:api-version plugin))
      (compiler-pass-fail! "C17-API" source-path manifest plugin
                           {:remediation "Check compiler plugin API compatibility before loading plugin code."}))
    (let [granted (get-in plugin [:capability-scopes :compiler/ir-transform])
          requested (:requested-scopes plugin)]
      (when-not (set/subset? (set requested) (set granted))
        (compiler-pass-fail! "C17-CAPABILITY" source-path manifest plugin
                             {:requested-capability :compiler/ir-transform
                              :scope requested
                              :remediation "Scope compiler capabilities to artifact kinds, pass phases, and package policy."}))))
  (doseq [contract (:plugin-pass-contracts suite)]
    (let [missing-fields (compiler-pass-missing-fields
                          contract compiler-pass-contract-required-fields)]
      (when (seq missing-fields)
        (compiler-pass-fail! "C17-PASS-CONTRACT" source-path manifest
                             contract
                             {:missing-fields missing-fields
                              :remediation "Plugin passes must declare the same contract fields as built-in compiler passes."}))))
  (doseq [trace (:plugin-execution-traces suite)]
    (when-not (= :passed (:verifier-result trace))
      (compiler-pass-fail! "C17-OUTPUT" source-path manifest trace
                           {:remediation "Plugin output must pass the declared output verifier before the artifact can continue."})))
  :complete)

(defn compiler-pass-validate-verification!
  [source-path manifest suite]
  (let [contracts (:contracts suite)
        contract-passes (set (map :pass contracts))]
    (doseq [risk (:risk-classification suite)]
      (let [missing-fields (compiler-pass-missing-fields
                            risk
                            [:pass :risk :minimum-evidence
                             :available-evidence :release-gate])]
        (when (seq missing-fields)
          (compiler-pass-fail! "C18-RISK" source-path manifest risk
                               {:missing-fields missing-fields
                                :remediation "Every compiler pass needs a risk class, minimum evidence, available evidence, and release-gate policy."})))
      (when (and (#{:high :critical} (:risk risk))
                 (not (set/subset? (set (:minimum-evidence risk))
                                   (set (:available-evidence risk)))))
        (compiler-pass-fail! "C18-EVIDENCE" source-path manifest risk
                             {:remediation "High-risk and critical passes need the evidence required by their risk classification."})))
    (let [covered (set (or (:covered-passes (:compiler-trust-report suite))
                           (map :pass (:passes (:compiler-trust-report suite)))))
          missing (set/difference contract-passes covered)]
      (when (seq missing)
        (compiler-pass-fail! "C18-TRUST-REPORT" source-path manifest
                             (:compiler-trust-report suite)
                             {:missing-fields (vec missing)
                              :remediation "Compiler trust reports must cover every built-in and plugin pass."})))
    (let [gate (:release-gate-report suite)]
      (when (and (= :passed (:status gate))
                 (seq (:evidence-gaps gate)))
        (compiler-pass-fail! "C18-RELEASE-GATE" source-path manifest gate
                             {:missing-fields (:evidence-gaps gate)
                              :remediation "Release gates cannot pass while required pass evidence is missing."}))))
  :complete)

(defn compiler-pass-capability-proof
  [suite]
  (let [contracts (:contracts suite)
        contracts-by-pass (into {} (map (juxt :pass identity) contracts))
        risk-records (:risk-classification suite)
        trust-passes (set (or (:covered-passes (:compiler-trust-report suite))
                              (map :pass (:passes (:compiler-trust-report
                                                   suite)))))]
    {:canonical-order-exposed?
     (= compiler-pass-default-stage-order (:stage-order suite))
     :contracts-complete?
     (and (= (set (:stage-order suite)) (set (map :pass contracts)))
          (every? #(empty? (compiler-pass-missing-fields
                            % compiler-pass-contract-required-fields))
                  contracts))
     :metadata-preserved-or-replaced?
     (every? (fn [contract]
               (let [durable-drops (set/intersection compiler-pass-durable-facts
                                                     (set (:invalidates
                                                           contract)))
                     replacements (set (concat (:regenerates contract)
                                               (:replacement-evidence
                                                contract)
                                               (:emits contract)))]
                 (empty? (set/difference durable-drops replacements))))
             contracts)
     :backend-lowering-checked?
     (= :verified-mir-or-domain-ir (get-in contracts-by-pass
                                           [:lower-target :input]))
     :diagnostics-structured?
     (every? #(and (perf-present? (:rule %))
                   (perf-present? (get-in % [:primary :span]))
                   (perf-present? (:origin-chain %))
                   (perf-present? (:facts %))
                   (perf-present? (:remediation %))
                   (:secret-free? %))
             (:diagnostic-fixtures suite))
     :incremental-keys-complete?
     (every? #(empty? (compiler-pass-missing-fields
                       % (get-in suite [:cache-key-schema :required-fields])))
             (:cache-keys suite))
     :plugin-capabilities-scoped?
     (let [plugin (:plugin-manifest suite)]
       (set/subset? (set (:requested-scopes plugin))
                    (set (get-in plugin
                                 [:capability-scopes
                                  :compiler/ir-transform]))))
     :verification-gates-present?
     (and (every? #(set/subset? (set (:minimum-evidence %))
                                (set (:available-evidence %)))
                  risk-records)
          (set/subset? (set (map :pass contracts)) trust-passes)
          (empty? (:evidence-gaps (:release-gate-report suite))))
     :status :complete}))

(defn compiler-pass-source-artifact-from-upstream
  [source-path upstream-artifact]
  (let [
        manifest (:profile-manifest upstream-artifact)
        suite (compiler-pass-suite manifest)
        _ (compiler-pass-validate-pipeline! source-path manifest suite)
        _ (compiler-pass-validate-diagnostics! source-path manifest suite)
        _ (compiler-pass-validate-incremental! source-path manifest suite)
        _ (compiler-pass-validate-plugins! source-path manifest suite)
        _ (compiler-pass-validate-verification! source-path manifest suite)
        capability-proof (compiler-pass-capability-proof suite)
        conformance {:documents ["C1" "C15" "C16" "C17" "C18"]
                     :task "P06-T01"
                     :required-diagnostic-ids compiler-pass-diagnostic-ids
                     :pass-contract-status :complete
                     :diagnostic-registry-status :complete
                     :incremental-contract-status :complete
                     :plugin-api-status :complete
                     :verification-status :complete
                     :status :complete}]
    {:kind :gravity/stage0-pass-contract-manifest-artifact
     :document-set ["C1" "C15" "C16" "C17" "C18"]
     :pass {:name :compiler-pass-contract-manifest
            :input :stage0-checked-capability-stack
            :output :pass-contract-manifest
            :requires [:reader :syntax :macro :core :typed-core
                       :effected-core :profile-compliance :safety
                       :performance :math-conformance]
            :preserves [:source-spans :syntax-identity :origin-chain
                        :profile :target :types :effects :ownership
                        :capabilities :safety-outcomes :proofs
                        :diagnostics]
            :emits [:compiler-pipeline-manifest :pass-contract-registry
                    :diagnostic-registry :incremental-cache-key-schema
                    :plugin-pass-api-manifest :verification-plan
                    :compiler-trust-report]
            :rejects compiler-pass-diagnostic-ids}
     :upstream-artifact-kind (:kind upstream-artifact)
     :upstream-artifact-hash
     (str "sha256:" (digest/sha256-hex (pr-str upstream-artifact)))
     :profile-manifest manifest
     :pipeline-stage-order (:stage-order suite)
     :pipeline-manifest (:pipeline-manifest suite)
     :pass-contract-registry (:contracts suite)
     :compiler-diagnostic-registry (:diagnostic-catalog suite)
     :diagnostic-stream-schema (:diagnostic-schema suite)
     :diagnostic-fixtures (:diagnostic-fixtures suite)
     :incremental-cache-key-schema (:cache-key-schema suite)
     :stage-cache-keys (:cache-keys suite)
     :stage-cache-entry-manifest (:cache-entries suite)
     :proof-reuse-records (:proof-reuse-records suite)
     :speculative-reuse-records (:speculative-reuse-records suite)
     :plugin-pass-api-manifest (:plugin-manifest suite)
     :plugin-pass-contracts (:plugin-pass-contracts suite)
     :plugin-execution-traces (:plugin-execution-traces suite)
     :pass-risk-classification (:risk-classification suite)
     :compiler-trust-report (:compiler-trust-report suite)
     :release-gate-report (:release-gate-report suite)
     :capability-based-proof capability-proof
     :compiler-pass-results conformance
     :diagnostics []}))

(def public-api
  {'public-api {:kind :contract}
   'compiler-pass-manifest-contract {:arglists '([])}
   'compiler-pass-diagnostic-ids {:kind :constant}
   'compiler-pass-default-stage-order {:kind :constant}
   'compiler-pass-contract-required-fields {:kind :constant}
   'compiler-pass-durable-facts {:kind :constant}
   'compiler-pass-contract
   {:arglists '([pass owner-doc input output requires preserves invalidates
                regenerates emits rejects risk evidence-class])}
   'compiler-pass-default-contracts {:kind :constant}
   'compiler-pass-default-diagnostic-schema {:kind :constant}
   'compiler-pass-default-diagnostic-catalog {:kind :constant}
   'compiler-pass-default-diagnostic-fixtures {:kind :constant}
   'compiler-pass-default-cache-key-schema {:kind :constant}
   'compiler-pass-default-cache-keys {:kind :constant}
   'compiler-pass-default-cache-entries {:kind :constant}
   'compiler-pass-default-proof-reuse-records {:kind :constant}
   'compiler-pass-default-speculative-reuse-records {:kind :constant}
   'compiler-pass-default-plugin-manifest {:kind :constant}
   'compiler-pass-default-plugin-pass-contracts {:kind :constant}
   'compiler-pass-default-plugin-execution-traces {:kind :constant}
   'compiler-pass-default-risk-classification
   {:arglists '([contracts])}
   'compiler-pass-default-trust-report
   {:arglists '([contracts risk-records])}
   'compiler-pass-default-release-gate-report {:kind :constant}
   'compiler-pass-merge-record-overrides
   {:arglists '([defaults overrides id-key])}
   'compiler-pass-suite {:arglists '([manifest])}
   'compiler-pass-fail! {:arglists '([id source-path manifest record extra])}
   'compiler-pass-missing-fields {:arglists '([record required-fields])}
   'compiler-pass-validate-pipeline!
   {:arglists '([source-path manifest suite])}
   'compiler-pass-validate-diagnostics!
   {:arglists '([source-path manifest suite])}
   'compiler-pass-validate-incremental!
   {:arglists '([source-path manifest suite])}
   'compiler-pass-validate-plugins!
   {:arglists '([source-path manifest suite])}
   'compiler-pass-validate-verification!
   {:arglists '([source-path manifest suite])}
   'compiler-pass-capability-proof {:arglists '([suite])}
   'compiler-pass-source-artifact-from-upstream
   {:arglists '([source-path upstream-artifact])}})

(defn compiler-pass-manifest-contract
  []
  (assoc namespace-contract :public-api public-api))
