(ns gravity.compiler-pass-manifest.contracts
  "Canonical built-in compiler pipeline and pass contracts.")

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
