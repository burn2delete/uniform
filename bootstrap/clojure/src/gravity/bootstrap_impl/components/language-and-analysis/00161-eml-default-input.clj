

(def eml-default-input
  {:graph-id :efir/sigmoid
   :verified? true
   :artifact-kind :gravity/stage0-efir-artifact})

(def eml-default-expressions
  [{:ir :gravity/eml
    :eml-artifact-id :eml/sigmoid-normalized
    :basis :exp-minus-log
    :source-efir :efir/sigmoid
    :numeric-mode :certified-approx
    :precision {:type :F32 :absolute-error-max 1.0e-5}
    :domain {'x {:real [-8.0 8.0]}}
    :codomain {:real [0.0 1.0]}
    :branch-policy {:exp :real-only
                    :log :principal
                    :complex-intermediates :forbidden}
    :expr {:op :eml
           :x {:op :neg :arg {:var 'x}}
           :y {:const 1}}
    :node-map [{:efir :exp-neg-x
                :eml :e-exp
                :span "math/eml.gravity:8:24"}
               {:efir :denominator
                :eml :e-denominator
                :span "math/eml.gravity:8:20"}]
    :proof-state :candidate
    :runtime-representation? false
    :status :complete}])

(def eml-default-normalization-trace
  [{:step 1
    :stage :efir-read
    :rule :read-verified-efir
    :before :efir/sigmoid
    :after :eml/sigmoid-normalized
    :premises [{:fact :efir-verified :value :efir/sigmoid}
               {:fact :numeric-mode :value :certified-approx}]
    :introduced-assumptions []
    :invalidated #{}
    :proof-obligation :none
    :source {:efir-node :sigmoid
             :span "math/eml.gravity:8:1"}
    :replayable? true}
   {:step 2
    :stage :basis-introduce
    :rule :sigmoid-exp-minus-log
    :before '(/ 1 (+ 1 (exp (- x))))
    :after {:op :eml
            :x {:op :neg :arg {:var 'x}}
            :y {:const 1}}
    :premises [{:fact :domain :value {'x {:real [-8.0 8.0]}}}
               {:fact :branch :value {:exp :real-only}}]
    :introduced-assumptions []
    :invalidated #{:raw-cost-estimate}
    :proof-obligation :proof/sigmoid-eml-equivalence
    :source {:efir-node :exp-neg-x
             :span "math/eml.gravity:8:24"}
    :replayable? true}])

(def eml-default-search-manifest
  {:manifest-id :search/sigmoid-eml
   :grammar {:basis :exp-minus-log
             :max-depth 6
             :constants [0 1 2 :pi :e]
             :operators [:add :sub :mul :div :eml :neg]}
   :domain {'x {:real [-8.0 8.0]}}
   :objective {:kind :equivalence-or-approximation
               :metric [:proof-cost :runtime-cost :error-bound]}
   :fuel {:candidate-limit 20000
          :time-ms 500}
   :ranking {:primary :proof-simplicity
             :secondary :estimated-runtime}
   :pruning-rules [:domain-empty :branch-mismatch :type-invalid]
   :deterministic? true
   :bounded? true
   :tie-policy :stable-source-order
   :source-fingerprint "sha256:stage0-eml-sigmoid"})

(def eml-default-candidates
  [{:candidate-id :candidate/sigmoid-eml
    :eml-artifact-id :eml/sigmoid-normalized
    :state :proved
    :ranking 1
    :proof :proof/sigmoid-eml-equivalence
    :proof-obligations []
    :can-influence-lowering? true
    :lowering-effect :proof-only
    :status :accepted}
   {:candidate-id :candidate/sigmoid-fast-unproved
    :eml-artifact-id :eml/sigmoid-normalized
    :state :rejected
    :ranking 2
    :proof-obligations [:roundoff :target-feature]
    :rejection-reasons [:proof-missing :target-unproven]
    :can-influence-lowering? false
    :status :rejected}])

(def eml-default-proof-requests
  [{:proof-id :proof/sigmoid-eml-equivalence
    :candidate-id :candidate/sigmoid-eml
    :kind :symbolic-replay
    :artifact :proof/sigmoid-eml-equivalence
    :checker :gravity.stage0/eml-trace-checker
    :status :accepted}])

(defn eml-suite
  [manifest]
  (let [source-suite (get-in manifest [:metadata :math :eml] {})]
    (assoc source-suite
           :efir-input
           (if (contains? source-suite :efir-input)
             (:efir-input source-suite)
             eml-default-input)
           :eml-expressions
           (if (contains? source-suite :eml-expressions)
             (vec (:eml-expressions source-suite))
             eml-default-expressions)
           :normalization-trace
           (if (contains? source-suite :normalization-trace)
             (vec (:normalization-trace source-suite))
             eml-default-normalization-trace)
           :search-manifest
           (if (contains? source-suite :search-manifest)
             (:search-manifest source-suite)
             eml-default-search-manifest)
           :candidates
           (if (contains? source-suite :candidates)
             (vec (:candidates source-suite))
             eml-default-candidates)
           :proof-requests
           (if (contains? source-suite :proof-requests)
             (vec (:proof-requests source-suite))
             eml-default-proof-requests)
           :complex-intermediates
           (if (contains? source-suite :complex-intermediates)
             (vec (:complex-intermediates source-suite))
             []))))

(defn eml-fail!
  [id source-path manifest record extra]
  (fail! id
         (case id
           "MATH4-EFIR" "EML lowering requires verified EFIR input"
           "MATH4-BASIS" "EML basis introduction is unsupported"
           "MATH4-DOMAIN" "EML lowering lost or changed domain facts"
           "MATH4-BRANCH" "EML branch policy is missing or incompatible"
           "MATH4-COMPLEX" "complex intermediates are not tracked or proven"
           "MATH4-TRACE" "EML normalization trace cannot replay"
           "MATH4-SEARCH" "EML search manifest is unbounded or nondeterministic"
           "MATH4-CANDIDATE" "EML candidate is used before proof acceptance"
           "MATH4-PROOF" "EML proof artifact is missing or rejected"
           "EML record is invalid")
         (merge {:source-span (or (:source-span record)
                                  {:source source-path})
                 :profile (or (:profile record) (:profile manifest))
                 :target (or (:target record) (:target manifest))
                 :graph-id (or (:graph-id record)
                               (:source-efir record)
                               (get-in record [:source :efir-node]))
                 :eml-artifact-id (:eml-artifact-id record)
                 :rule-id (or (:rule record) (:rule-id record))
                 :candidate-id (:candidate-id record)
                 :numeric-mode (:numeric-mode record)
                 :branch-policy (:branch-policy record)
                 :diagnostic-family :eml-normalization}
                extra)))

(defn eml-validate-math4!
  [source-path manifest efir-artifact suite]
  (let [efir-graphs (set (map :graph-id (:efir-graph efir-artifact)))
        efir-input (:efir-input suite)
        expressions (:eml-expressions suite)
        search-manifest (:search-manifest suite)]
    (when (or (not= :complete (get-in efir-artifact [:capability-based-proof :status]))
              (not (true? (:verified? efir-input)))
              (not (contains? efir-graphs (:graph-id efir-input))))
      (eml-fail! "MATH4-EFIR" source-path manifest efir-input
                 {:remediation "Import EML only from a verified EFIR graph with preserved semantic facts."}))
    (doseq [expr expressions]
      (when-not (contains? efir-graphs (:source-efir expr))
        (eml-fail! "MATH4-EFIR" source-path manifest expr
                   {:remediation "Preserve the originating EFIR graph id in every EML expression."}))
      (when-not (contains? eml-supported-bases (:basis expr))
        (eml-fail! "MATH4-BASIS" source-path manifest expr
                   {:basis (:basis expr)
                    :remediation "Use the registered exp-minus-log EML basis for stage0."}))
      (when (or (not (perf-present? (:domain expr)))
                (false? (:domain-consistent? expr)))
        (eml-fail! "MATH4-DOMAIN" source-path manifest expr
                   {:domain (:domain expr)
                    :remediation "Retain domain and codomain facts while lowering EFIR to EML."}))
      (when (or (not (perf-present? (:branch-policy expr)))
                (false? (:branch-compatible? expr)))
        (eml-fail! "MATH4-BRANCH" source-path manifest expr
                   {:remediation "Attach branch policy and prove compatibility before using EML."})))
    (doseq [complex (:complex-intermediates suite)]
      (when (and (:introduced? complex)
                 (or (not (perf-present? (:branch-policy complex)))
                     (not (perf-present? (:proof complex)))
                     (false? (:final-domain-valid? complex))))
        (eml-fail! "MATH4-COMPLEX" source-path manifest complex
                   {:remediation "Complex intermediates need branch choices, final-domain proof, and a proof artifact."})))
    (when (empty? (:normalization-trace suite))
      (eml-fail! "MATH4-TRACE" source-path manifest {}
                 {:remediation "Emit a replayable normalization trace."}))
    (doseq [step (:normalization-trace suite)]
      (when (or (not (perf-present? (:rule step)))
                (not (perf-present? (:premises step)))
                (not (perf-present? (:source step)))
                (false? (:replayable? step)))
        (eml-fail! "MATH4-TRACE" source-path manifest step
                   {:remediation "Record rule id, premises, source span, and deterministic choices for every EML step."})))
    (when (or (not (perf-present? search-manifest))
              (not (true? (:deterministic? search-manifest)))
              (not (true? (:bounded? search-manifest)))
              (not (perf-present? (:grammar search-manifest)))
              (not (perf-present? (:fuel search-manifest)))
              (not (perf-present? (:ranking search-manifest)))
              (not (perf-present? (:tie-policy search-manifest))))
      (eml-fail! "MATH4-SEARCH" source-path manifest search-manifest
                 {:remediation "Make EML search bounded, deterministic, and replayable."}))
    (doseq [candidate (:candidates suite)]
      (when (and (:can-influence-lowering? candidate)
                 (not (contains? #{:proved :bounded} (:state candidate))))
        (eml-fail! "MATH4-CANDIDATE" source-path manifest candidate
                   {:remediation "Only proved or bounded candidates may affect lowering."})))
    (doseq [request (:proof-requests suite)]
      (when-not (= :accepted (:status request))
        (eml-fail! "MATH4-PROOF" source-path manifest request
                   {:remediation "Proof requests must resolve to accepted proof artifacts before candidate promotion."})))
    (doseq [candidate (:candidates suite)]
      (when (and (contains? #{:proved :bounded} (:state candidate))
                 (not (perf-present? (:proof candidate))))
        (eml-fail! "MATH4-PROOF" source-path manifest candidate
                   {:remediation "Accepted EML candidates need proof or bounded-error evidence."})))
    :complete))