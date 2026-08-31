

(def math-conformance-default-suite-manifest
  {:suite-id :suite/phase05-stage0
   :documents [:MATH1 :MATH2 :MATH3 :MATH4 :MATH5 :MATH6
               :MATH7 :MATH8 :MATH9 :MATH10]
   :profiles [:core :hosted :native :gpu :formal :firmware]
   :targets [:portable :jvm :gpu-sm80]
   :fixture-families [:numeric-tower :elementary-registry :efir :eml
                      :approximation-certificates :interval-proofs
                      :numeric-modes :floating :rewrites :optimization]
   :oracles [:exact-rational :mpfr :interval-checker :symbolic-checker
             :provider-conformance :known-counterexample]
   :negative-diagnostics true
   :content-addressed? true})

(def math-conformance-default-oracles
  [{:oracle-id :oracle/interval
    :kind :interval-checker
    :trusted? true
    :available? true
    :version "stage0"
    :target-independent? true
    :accepted-error-model :absolute-bound}
   {:oracle-id :oracle/symbolic
    :kind :symbolic-checker
    :trusted? true
    :available? true
    :version "stage0"
    :target-independent? true
    :accepted-error-model :proof-replay}])

(def math-conformance-default-fixtures
  [{:fixture-id :fixture/sigmoid-certified-f32
    :source '(sigmoid x)
    :profile :native
    :target :jvm
    :mode :certified-approx
    :domain {'x {:real [-8 8]}}
    :expected-artifacts [:efir :eml :approximation-certificate
                         :interval-proof :rewrite-trace
                         :optimization-decision]
    :oracle :oracle/interval
    :expected {:compile :accepted
               :runtime :within-bound
               :diagnostics []}
    :valid? true
    :provenance {:source-hash "sha256:stage0-sigmoid-source"
                 :suite :suite/phase05-stage0}}])

(def math-conformance-default-artifact-results
  [{:fixture-id :fixture/sigmoid-certified-f32
    :expected [:efir :eml :approximation-certificate
               :interval-proof :rewrite-trace :optimization-decision]
    :present [:efir :eml :approximation-certificate
              :interval-proof :rewrite-trace :optimization-decision]
    :missing []
    :complete? true}])

(def math-conformance-default-efir-reports
  [{:fixture-id :fixture/sigmoid-certified-f32
    :efir :efir/sigmoid
    :matched? true
    :source-preserved? true}])

(def math-conformance-default-eml-replays
  [{:fixture-id :fixture/sigmoid-certified-f32
    :trace :trace/sigmoid-eml
    :replayed? true
    :deterministic? true}])

(def math-conformance-default-certificate-replays
  [{:fixture-id :fixture/sigmoid-certified-f32
    :certificate :cert/sigmoid-poly-f32
    :replayed? true
    :checker :checker/stage0-approximation}])

(def math-conformance-default-interval-replays
  [{:fixture-id :fixture/sigmoid-certified-f32
    :proof :safe15/proof-sigmoid-bound
    :replayed? true
    :checker :checker/stage0-interval}])

(def math-conformance-default-floating-reports
  [{:fixture-id :fixture/sigmoid-certified-f32
    :format :binary32
    :rounding :nearest-ties-to-even
    :exceptional-values :covered
    :matched? true}])

(def math-conformance-default-rewrite-replays
  [{:fixture-id :fixture/sigmoid-certified-f32
    :trace :trace/sigmoid-definition
    :replayed? true
    :side-conditions :proved}])

(def math-conformance-default-optimization-results
  [{:fixture-id :fixture/sigmoid-certified-f32
    :decision :decision/sigmoid
    :selected :candidate/fused-poly-7
    :matched? true}])

(def math-conformance-default-negative-diagnostics
  [{:fixture-id :fixture/reject-missing-proof
    :expected-diagnostic "MATH10-PROOF"
    :actual-diagnostic "MATH10-PROOF"}
   {:fixture-id :fixture/reject-wrong-diagnostic
    :expected-diagnostic "MATH11-DIAGNOSTIC"
    :actual-diagnostic "MATH11-DIAGNOSTIC"}])

(def math-conformance-default-result-matrix
  [{:profile :native
    :target :jvm
    :fixtures 1
    :accepted 1
    :rejected 26
    :status :pass}])

(defn math-conformance-suite
  [manifest]
  (let [source-suite (get-in manifest [:metadata :math :conformance] {})
        vector-value (fn [key override-key defaults]
                       (cond
                         (contains? source-suite key) (vec (get source-suite key))
                         (contains? source-suite override-key)
                         (mapv #(merge (first defaults) %)
                               (get source-suite override-key))
                         :else defaults))
        map-value (fn [key override-key default]
                    (cond
                      (contains? source-suite key) (get source-suite key)
                      (contains? source-suite override-key)
                      (merge default (get source-suite override-key))
                      :else default))]
    (assoc source-suite
           :subgraphs
           (vector-value :subgraphs :subgraph-overrides
                         math-conformance-default-subgraphs)
           :candidates
           (vector-value :candidates :candidate-overrides
                         math-conformance-default-candidates)
           :rounding-target
           (map-value :rounding-target :rounding-target-overrides
                      math-conformance-default-rounding-target)
           :interval-ledger
           (vector-value :interval-ledger :interval-ledger-overrides
                         math-conformance-default-interval-ledger)
           :synthesis-transcripts
           (vector-value :synthesis-transcripts :synthesis-overrides
                         math-conformance-default-synthesis)
           :provider-comparisons
           (vector-value :provider-comparisons
                         :provider-comparison-overrides
                         math-conformance-default-provider-comparisons)
           :autotune-evidence
           (map-value :autotune-evidence :autotune-overrides
                      math-conformance-default-autotune)
           :selected-decisions
           (vector-value :selected-decisions :selected-decision-overrides
                         math-conformance-default-decisions)
           :backend-lowering-map
           (vector-value :backend-lowering-map :backend-lowering-overrides
                         math-conformance-default-backend-map)
           :suite-manifest
           (map-value :suite-manifest :suite-manifest-overrides
                      math-conformance-default-suite-manifest)
           :oracles
           (vector-value :oracles :oracle-overrides
                         math-conformance-default-oracles)
           :fixtures
           (vector-value :fixtures :fixture-overrides
                         math-conformance-default-fixtures)
           :artifact-results
           (vector-value :artifact-results :artifact-result-overrides
                         math-conformance-default-artifact-results)
           :efir-verification-reports
           (vector-value :efir-verification-reports
                         :efir-verification-overrides
                         math-conformance-default-efir-reports)
           :eml-trace-replay-reports
           (vector-value :eml-trace-replay-reports
                         :eml-replay-overrides
                         math-conformance-default-eml-replays)
           :certificate-replay-logs
           (vector-value :certificate-replay-logs
                         :certificate-replay-overrides
                         math-conformance-default-certificate-replays)
           :interval-proof-replay-logs
           (vector-value :interval-proof-replay-logs
                         :interval-replay-overrides
                         math-conformance-default-interval-replays)
           :floating-conformance-report
           (vector-value :floating-conformance-report
                         :floating-conformance-overrides
                         math-conformance-default-floating-reports)
           :rewrite-trace-replay-logs
           (vector-value :rewrite-trace-replay-logs
                         :rewrite-replay-overrides
                         math-conformance-default-rewrite-replays)
           :optimization-result-records
           (vector-value :optimization-result-records
                         :optimization-result-overrides
                         math-conformance-default-optimization-results)
           :negative-diagnostic-results
           (vector-value :negative-diagnostic-results
                         :negative-diagnostic-overrides
                         math-conformance-default-negative-diagnostics)
           :per-profile-target-result-matrix
           (vector-value :per-profile-target-result-matrix
                         :result-matrix-overrides
                         math-conformance-default-result-matrix))))