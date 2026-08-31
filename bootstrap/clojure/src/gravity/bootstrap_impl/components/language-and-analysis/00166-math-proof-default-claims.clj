

(def math-proof-default-claims
  [{:artifact :gravity/interval-proof
    :claim-id :claim/sigmoid-certified-bound
    :source {:efir :efir/sigmoid
             :eml-candidate :candidate/sigmoid-eml
             :span "math/proof.gravity:8:1"}
    :claim {:kind :bounded-error
            :expr '(abs (- candidate reference))
            :bound 1.0e-5}
    :domain {'x {:real [-8 8]
                 :endpoint-kind :exact-rational
                 :boundary :closed}}
    :branch-policy {:exp :real-only
                    :complex-intermediates :forbidden}
    :numeric-mode :certified-approx
    :precision {:type :F32 :absolute-error-max 1.0e-5}
    :domain-valid? true
    :branch-compatible? true
    :status :proved}])

(def math-proof-default-partitions
  [{:partition-id :partition/sigmoid-certified-bound
    :claim-id :claim/sigmoid-certified-bound
    :strategy :adaptive
    :ordering :source-stable
    :replayable? true
    :cells [{:id :c0
             :domain {'x {:real [-8 0]}}
             :status :proved
             :bounds {:approximation 4.0e-6
                      :roundoff 2.0e-6}}
            {:id :c1
             :domain {'x {:real [0 8]}}
             :status :proved
             :bounds {:approximation 4.0e-6
                      :roundoff 2.0e-6}}]
    :unresolved []
    :residual-check {:emitted? false
                     :required? false}}])

(def math-proof-default-bound-ledger
  [{:claim-id :claim/sigmoid-certified-bound
    :method :interval
    :real-bound 7.0e-6
    :roundoff-bound 2.0e-6
    :combined-bound 9.0e-6
    :required-bound 1.0e-5
    :bounds-sufficient? true
    :assumptions [:domain :branch-policy :rounding]
    :invalidated-by #{:target-change :floating-mode-change
                      :branch-policy-change}}])

(def math-proof-default-roundoff-ledger
  [{:claim-id :claim/sigmoid-certified-bound
    :format :binary32
    :rounding :nearest-ties-to-even
    :outward-rounding-proven? true
    :target-assumptions {:denormals :preserve
                         :fma :forbidden}}])

(def math-proof-default-provider-results
  [{:provider :provider/stage0-interval
    :claim-id :claim/sigmoid-certified-bound
    :method :interval
    :imported-through-safe15? true
    :trust-policy :accepted
    :proof-ref :safe15/proof-sigmoid-bound
    :replayable? true}])

(def math-proof-default-rewrite-rules
  [{:artifact :gravity/rewrite-rule
    :rule-id :rewrite/sigmoid-definition
    :version 1
    :pattern '(sigmoid x)
    :replacement '(/ 1 (+ 1 (exp (- x))))
    :domain {'x {:real [-8 8]}}
    :codomain {:real [0 1]}
    :side-conditions []
    :branch-policy {:exp :real-only}
    :numeric-modes #{:symbolic :exact :certified-approx}
    :proof-status :proved
    :proof-ref :proof/sigmoid-rewrite
    :counterexamples []
    :invalidated-by #{:branch-policy-change :domain-change :mode-change}
    :source {:package :gravity.math
             :span "math/proof.gravity:12:1"}
    :domain-compatible? true
    :branch-compatible? true
    :used-as-accepted? true}])

(def math-proof-default-rewrite-traces
  [{:trace-id :trace/sigmoid-definition
    :step 1
    :rule :rewrite/sigmoid-definition
    :state :proved
    :before '(sigmoid x)
    :after '(/ 1 (+ 1 (exp (- x))))
    :matched {'x :node/x}
    :side-condition-results []
    :preserved #{:domain :branch-policy :floating-manifest}
    :invalidated #{:cost-estimate}
    :source {:efir-node :sigmoid
             :span "math/proof.gravity:12:1"}
    :replayable? true}])

(def math-proof-default-termination
  {:strategy :bounded-fuel
   :fuel 64
   :deterministic? true
   :unbounded? false
   :exhaustion-behavior :emit-candidate-trace})

(def math-proof-default-counterexamples
  [{:rule :sqrt-square
    :claim '(= (sqrt (* x x)) x)
    :domain {'x {:real :all}}
    :counterexample {'x -1}
    :reason :domain-too-wide
    :suggested-guard '(>= x 0)
    :disproves-accepted? false}])

(def math-proof-default-egraph
  {:run-id :egraph/sigmoid-definition
   :root-eclass :e0
   :selected '(/ 1 (+ 1 (exp (- x))))
   :cost-model {:id :math9-default-cost
                :version 1
                :weights {:operator-count 1
                          :runtime-check 4
                          :unproved-edge :forbidden}
                :tie-break [:proof-depth :source-order :node-id]}
   :bounds {:iterations 8
            :nodes 128
            :per-rule-fuel 16}
   :guards {:domain {'x {:real [-8 8]}}
            :branch-policy {:exp :real-only}}
   :analysis {:domain :consistent
              :branch-policy :consistent
              :proof-status :proved}
   :proof-replay [:trace/sigmoid-definition]
   :explanation :explain/sigmoid-definition
   :valid? true
   :status :proved})

(def math-proof-default-equality-claims
  [{:claim-id :equality/sigmoid-definition
    :source :proof-replay
    :accepted? true
    :proof-ref :proof/sigmoid-rewrite
    :status :proved}])

(defn math-proof-suite
  [manifest]
  (let [source-suite (get-in manifest [:metadata :math :proof] {})]
    (assoc source-suite
           :claims
           (if (contains? source-suite :claims)
             (vec (:claims source-suite))
             (if (contains? source-suite :claim-overrides)
               (mapv #(merge (first math-proof-default-claims) %)
                     (:claim-overrides source-suite))
               math-proof-default-claims))
           :partitions
           (if (contains? source-suite :partitions)
             (vec (:partitions source-suite))
             (if (contains? source-suite :partition-overrides)
               (mapv #(merge (first math-proof-default-partitions) %)
                     (:partition-overrides source-suite))
               math-proof-default-partitions))
           :bound-ledger
           (if (contains? source-suite :bound-ledger)
             (vec (:bound-ledger source-suite))
             (if (contains? source-suite :bound-overrides)
               (mapv #(merge (first math-proof-default-bound-ledger) %)
                     (:bound-overrides source-suite))
               math-proof-default-bound-ledger))
           :roundoff-ledger
           (if (contains? source-suite :roundoff-ledger)
             (vec (:roundoff-ledger source-suite))
             (if (contains? source-suite :roundoff-overrides)
               (mapv #(merge (first math-proof-default-roundoff-ledger) %)
                     (:roundoff-overrides source-suite))
               math-proof-default-roundoff-ledger))
           :provider-results
           (if (contains? source-suite :provider-results)
             (vec (:provider-results source-suite))
             (if (contains? source-suite :provider-overrides)
               (mapv #(merge (first math-proof-default-provider-results) %)
                     (:provider-overrides source-suite))
               math-proof-default-provider-results))
           :rewrite-rules
           (if (contains? source-suite :rewrite-rules)
             (vec (:rewrite-rules source-suite))
             (if (contains? source-suite :rewrite-rule-overrides)
               (mapv #(merge (first math-proof-default-rewrite-rules) %)
                     (:rewrite-rule-overrides source-suite))
               math-proof-default-rewrite-rules))
           :rewrite-traces
           (if (contains? source-suite :rewrite-traces)
             (vec (:rewrite-traces source-suite))
             (if (contains? source-suite :rewrite-trace-overrides)
               (mapv #(merge (first math-proof-default-rewrite-traces) %)
                     (:rewrite-trace-overrides source-suite))
               math-proof-default-rewrite-traces))
           :termination-report
           (if (contains? source-suite :termination-report)
             (:termination-report source-suite)
             math-proof-default-termination)
           :counterexamples
           (if (contains? source-suite :counterexamples)
             (vec (:counterexamples source-suite))
             math-proof-default-counterexamples)
           :egraph-report
           (if (contains? source-suite :egraph-report)
             (:egraph-report source-suite)
             math-proof-default-egraph)
           :equality-claims
           (if (contains? source-suite :equality-claims)
             (vec (:equality-claims source-suite))
             math-proof-default-equality-claims))))