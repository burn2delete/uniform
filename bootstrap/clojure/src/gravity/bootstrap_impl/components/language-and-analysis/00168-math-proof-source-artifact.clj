

(defn math-proof-source-artifact
  [source-path source-text]
  (let [approximation-artifact (approximation-source-artifact source-path source-text)
        manifest (:profile-manifest approximation-artifact)
        suite (math-proof-suite manifest)
        _ (math-proof-validate-math6! source-path manifest suite)
        _ (math-proof-validate-math9! source-path manifest suite)
        capability-proof (math-proof-capability-proof suite)
        conformance {:documents ["MATH6" "MATH9"]
                     :task "P05-T05"
                     :required-diagnostic-ids math6-9-diagnostic-ids
                     :interval-proof-status :complete
                     :symbolic-rewrite-status :complete
                     :egraph-status :complete
                     :status :complete}]
    {:kind :gravity/stage0-math-proof-artifact
     :document-set ["MATH6" "MATH9"]
     :pass {:name :interval-symbolic-proof
            :input :approximation-certificate
            :output :proof-and-rewrite-record
            :requires [:certified-approximation :eml-normalization
                       :efir-validation :safe15-proof-import]
            :preserves [:source-spans :profile :target :effects
                        :capabilities :domain :branch-policy :numeric-mode
                        :precision-contract :target-efir]
            :emits [:claim-record
                    :interval-domain-map
                    :partition-tree
                    :rational-bound-ledger
                    :roundoff-ledger
                    :branch-coverage-report
                    :unresolved-cell-report
                    :checker-transcript
                    :safe15-proof-reference
                    :rewrite-rule-registry
                    :rule-proof-artifact
                    :rewrite-application-trace
                    :counterexample-fixture
                    :termination-report
                    :egraph-saturation-report
                    :equality-explanation-trace
                    :math-proof-conformance-results]
            :rejects math6-9-diagnostic-ids}
     :approximation-artifact-hash
     (str "sha256:" (sha256-hex (pr-str approximation-artifact)))
     :approximation-artifact-kind (:kind approximation-artifact)
     :profile-manifest manifest
     :claim-record (:claims suite)
     :interval-domain-map
     (mapv #(select-keys % [:claim-id :domain]) (:claims suite))
     :partition-tree (:partitions suite)
     :rational-bound-ledger (:bound-ledger suite)
     :roundoff-ledger (:roundoff-ledger suite)
     :branch-coverage-report
     (mapv #(select-keys % [:claim-id :branch-policy]) (:claims suite))
     :unresolved-cell-report
     (mapv #(select-keys % [:partition-id :unresolved :residual-check])
           (:partitions suite))
     :checker-transcript (:provider-results suite)
     :safe15-proof-reference
     (mapv #(select-keys % [:claim-id :proof-ref :trust-policy])
           (:provider-results suite))
     :rewrite-rule-registry (:rewrite-rules suite)
     :rule-proof-artifact
     (mapv #(select-keys % [:rule-id :proof-status :proof-ref])
           (:rewrite-rules suite))
     :rewrite-application-trace (:rewrite-traces suite)
     :counterexample-fixture (:counterexamples suite)
     :termination-report (:termination-report suite)
     :egraph-saturation-report (:egraph-report suite)
     :equality-explanation-trace (:equality-claims suite)
     :capability-based-proof capability-proof
     :math-proof-conformance-results conformance
     :diagnostics []}))

(def math-conformance-default-subgraphs
  [{:subgraph-id :subgraph/sigmoid
    :source-spans ["math/conformance.gravity:8:1"]
    :generated-origin-chain [:user-source]
    :efir-graph :efir/sigmoid
    :profile :native
    :target {:triple :jvm :features #{:portable}}
    :numeric-mode :certified-approx
    :precision {:absolute-error-max 1.0e-5}
    :verified-efir? true}])

(def math-conformance-default-candidates
  [{:candidate-id :candidate/fused-poly-7
    :family :fused-polynomial
    :efir-graph :efir/sigmoid
    :source-spans ["math/conformance.gravity:8:1"]
    :profile :native
    :target {:triple :jvm :features #{:portable}}
    :numeric-mode :certified-approx
    :precision {:absolute-error-max 1.0e-5}
    :status :legal
    :selected? true
    :accepted? true
    :proofs [:safe15/proof-sigmoid-bound :proof/sigmoid-rewrite]
    :certificate :cert/sigmoid-poly-f32
    :certificate-status :accepted
    :whole-expression-certificate :cert/sigmoid-poly-f32
    :per-call-certificates []
    :provider :provider/poly-sigmoid-f32
    :provider-eligible? true
    :semantic-contract-satisfied? true
    :simd? true
    :lane-certificate :cert/simd-lane-sigmoid
    :gpu? true
    :device-certificate :cert/gpu-divergence-sigmoid
    :cost {:latency-ns 6 :code-size-bytes 96}}
   {:candidate-id :candidate/hardware-native
    :family :hardware-instruction
    :efir-graph :efir/sigmoid
    :source-spans ["math/conformance.gravity:8:1"]
    :profile :native
    :target {:triple :jvm :features #{:portable}}
    :numeric-mode :hardware-native
    :precision {:absolute-error-max 1.0e-5}
    :status :rejected
    :selected? false
    :accepted? false
    :proofs []
    :certificate nil
    :certificate-status :missing
    :provider :provider/hardware-sigmoid
    :provider-eligible? false
    :semantic-contract-satisfied? false
    :rejection-reason :missing-error-bound}])

(def math-conformance-default-rounding-target
  {:target-id :rounding/sigmoid-f32-nearest-even
   :efir :efir/sigmoid
   :function :sigmoid
   :input-representation {:type :F32 :domain :all-finite}
   :output-representation {:type :F32}
   :rounding-modes [:nearest-even]
   :tie-policy :nearest-even
   :branch-policy {:exp :real-only}
   :exceptional-values {:nan :propagate
                        :inf :domain-error
                        :signed-zero :preserve}
   :target-assumptions {:evaluation-format :F64
                        :contract-fma false
                        :denormals :preserved}})

(def math-conformance-default-interval-ledger
  [{:target-id :rounding/sigmoid-f32-nearest-even
    :cell-id :cell/sigmoid-negative
    :domain {'x {:real [-8 0]}}
    :accepted-result-interval {:lower 0.0 :upper 0.5 :closed? true}
    :tie-cases []
    :subnormal-boundaries []
    :signed-zero :preserve
    :resolved? true}
   {:target-id :rounding/sigmoid-f32-nearest-even
    :cell-id :cell/sigmoid-positive
    :domain {'x {:real [0 8]}}
    :accepted-result-interval {:lower 0.5 :upper 1.0 :closed? true}
    :tie-cases []
    :subnormal-boundaries []
    :signed-zero :preserve
    :resolved? true}])

(def math-conformance-default-synthesis
  [{:candidate-id :candidate/fused-poly-7
    :basis :polynomial
    :degree 7
    :partition :partition/sigmoid-certified-bound
    :range-reduction :none
    :coefficient-representation :binary64
    :evaluation-order :horner
    :fma-policy :forbidden
    :intermediate-format :binary64
    :constraints-checkable? true
    :feasible? true
    :checker :checker/stage0-synthesis}])

(def math-conformance-default-provider-comparisons
  [{:comparison-id :compare/sigmoid
    :providers [:provider/poly-sigmoid-f32 :provider/libm :provider/rlibm]
    :representation-coverage :checked
    :rounding-mode-coverage :checked
    :domain-coverage :checked
    :branch-policy-coverage :checked
    :exceptional-value-behavior :checked
    :target-assumptions :checked
    :certificate-status :checked
    :fallback-behavior :checked
    :semantic-fields-complete? true
    :ranking-source :semantic-then-benchmark}])

(def math-conformance-default-autotune
  {:selection-id :autotune/sigmoid
   :candidate-set-hash "sha256:stage0-sigmoid-candidates"
   :objective {:primary :latency :secondary :code-size}
   :replayable? true
   :deterministic? true
   :selected :candidate/fused-poly-7
   :fallback :provider/libm})

(def math-conformance-default-decisions
  [{:decision-id :decision/sigmoid
    :efir :efir/sigmoid
    :source-spans ["math/conformance.gravity:8:1"]
    :profile :native
    :target {:triple :jvm :features #{:portable}}
    :numeric-mode :certified-approx
    :precision {:absolute-error-max 1.0e-5}
    :domain {'x {:real [-8 8]}}
    :branch-policy {:exp :real-only}
    :objective {:primary :latency :secondary :code-size}
    :selected :candidate/fused-poly-7
    :proofs [:safe15/proof-sigmoid-bound :proof/sigmoid-rewrite]
    :benchmarks [:bench/sigmoid-stage0]
    :fallback :provider/libm
    :fallback-required? true
    :fallback-legal? true
    :replayable? true}])