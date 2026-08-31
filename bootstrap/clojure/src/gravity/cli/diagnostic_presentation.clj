(ns gravity.cli.diagnostic-presentation
  "Stable EDN presentation for bootstrap CLI diagnostics.")

(defn print-diagnostic!
  [sanitize-complete-diagnostic ex]
  (binding [*out* *err*]
    (let [data (ex-data ex)
          authentic-c-diagnostic
          (sanitize-complete-diagnostic data)
          projection
          (select-keys data [:id :rule :message :severity :bootstrap-stage
                                     :source-span
                                     :profile :active-profile :target
                                     :reader-state :analyzer-stage :alias :symbol
                                     :syntax-id :candidate-bindings
                                     :dependency-edge :namespace
                                     :module :macro :operator :function
                                     :expected-arity :actual-arity :effect
                                     :bootstrap-hosted? :packaged-jvm-cli?
                                     :seedless-release?
                                     :declared-effects :granted-build-effects
                                     :required-effect :required-capability
                                     :requested-capability
                                     :selected-or-missing-provider
                                     :declared-capabilities :grant-id :scope :phase
                                     :expected-type :actual-type :resource
                                     :resource-type :depth :limit :generated-form
                                     :safety-outcome :safety-mode :missing-fact
                                     :safe-rule :dependency :dependency-safety
                                     :underlying-diagnostic :underlying-message
                                     :diagnostic-family :legal-alternative
                                     :release-artifact-id :release-compiler-id
                                     :compiler-path-id :runtime-path-id
                                     :artifact-producer
                                     :release-boundary-path
                                     :release-binary-path
                                     :proof-artifact-path
                                     :proof-artifact-id
                                     :p15-final-seed-retirement-proof
                                     :p18-final-release-proof
                                     :compiler-source
                                     :full-language-compiler-self-hosted?
                                     :clojure-seed-boundary?
                                     :expected-command
                                     :seed-boundary-facts
                                     :output-path :executable-path
                                     :expected-stdout :actual-stdout
                                     :command-contract
                                     :profile-clauses :profiles-clauses
                                     :profile-contract :policy-layers
                                     :dependency-profile :supported
                                     :host-runtime :host-symbol
                                     :target-profile :target-feature
                                     :caller-safety :erased-check
                                     :operation :memory-regime :memory-family
                                     :provider-id :region-id :arena-id
                                     :generation :resource-id :owner-id
                                     :borrow-id :lifetime-id
	                                     :package-id :package-version
	                                     :dependency-path :denied-authority
	                                     :denied-policy-layer :policy-layer
	                                     :manifest-entry :lockfile-entry
	                                     :macro-symbol :macro-definition-span
	                                     :call-site-span :generated-form-span
	                                     :expansion-phase :build-effects
	                                     :model-id :tool-id :prompt-role
	                                     :taint-source :human-review-policy
	                                     :replay-policy :agent-artifact-id
	                                     :consumer-namespace :consumer-profile
	                                     :producer-namespace :producer-profile
	                                     :edge-kind :boundary
	                                     :missing-evidence :required-evidence
	                                     :suggested-boundary
	                                     :unsupported-runtime-providers
	                                     :denied-effects :denied-capabilities
	                                     :generated-origin-chain
	                                     :missing-profiles :missing-documents
	                                     :missing-diagnostic-ids
	                                     :unexpected-diagnostic-ids
	                                     :expected-profile
	                                     :expected-document
	                                     :claim-id :missing-fields
	                                     :target-request :target-features
	                                     :target-fingerprint
	                                     :lost-safety-facts
	                                     :expanded-effects
	                                     :expanded-capabilities
	                                     :source-numeric-mode
	                                     :optimized-numeric-mode
	                                     :generated-variant
	                                     :abstraction :equivalent-form
	                                     :expected-erased-costs
	                                     :erased-costs :residual-costs
	                                     :residual-cost :ir-artifacts
	                                     :profile-illegal-behavior
	                                     :source-function
	                                     :specialization-key
	                                     :guard :guard-stage
	                                     :build-effects
	                                     :ungranted-build-effects
	                                     :grants :cache-key-inputs
	                                     :invalidation-inputs
	                                     :missing-key-inputs
	                                     :missing-cache-inputs
	                                     :variant-selection
	                                     :layout-type :layout-transform
	                                     :alignment :erased-checks
                                     :benchmark-id :metric :baseline
                                     :environment-fingerprint
                                     :sample-summary :threshold
                                     :gate-state :samples
                                     :required-samples
                                     :regression-percent
                                     :classification
                                     :baseline-update
                                     :environment-drift
                                     :profile-data-id :policy
                                     :source-hash :mir-hash
                                     :workload :stale-field
                                     :privacy :pass-id
                                     :decision-id
                                     :changed-operations
                                     :expected-workload
                                     :actual-workload
                                     :candidate-space-id
                                     :candidate-id :variant-id
                                     :objective
                                     :missing-certificate
                                     :selected-status
                                     :fallback-status
                                     :missing-feature
                                     :backend
                                     :dispatch-overhead
                                     :loop-id :path-id
                                     :vector-width :tail-handling
                                     :check-class :ir-node
                                     :invalidating-pass
                                     :failure-mode
                                     :required-samples
                                     :numeric-family :numeric-mode
                                     :conversion-mode :rounding-policy
                                     :manifest-id :format
                                     :inherited-mode :local-override
                                     :missing-families
                                     :function-id :efir-node :graph-id
                                     :branch-policy
                                     :eml-artifact-id :rule-id
                                     :rule-version :expression-id :bound
                                     :failing-bound
	                                     :claim :proof-id :certificate-id
	                                     :artifact-node :provider :trust-root
	                                     :invalidated-assumption :fixture-id
	                                     :document-id :expected-outcome
	                                     :actual-outcome :missing-artifact
                                     :efir-graph-id
                                     :precision-contract
                                     :rounding-target
                                     :interval-generation-ledger
                                     :synthesis-transcript
                                     :missing-proof
                                     :missing-certificate
                                     :provider-comparison-result
                                     :fallback-status
                                     :oracle-id
                                     :stage
                                     :input-artifact-id
                                     :output-artifact-id
                                     :artifact-id
                                     :domain
                                     :semantic-anchor
                                     :owner-doc
                                     :verifier
                                     :schema-field
                                     :plugin-id
                                     :package-id
                                     :compiler-api-version
                                     :trust-level
                                     :cache-key
                                     :pass-version
                                     :risk-class
                                     :available-evidence
                                     :affected-profiles
                                     :affected-targets
                                     :release-gate
	                                     :remediation :cause-message])
          projection
          (if authentic-c-diagnostic
            (assoc projection :facts (:facts authentic-c-diagnostic))
            projection)]
      (prn projection))))
