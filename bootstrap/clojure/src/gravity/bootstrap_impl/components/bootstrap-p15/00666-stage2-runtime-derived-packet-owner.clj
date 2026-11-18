(declare stage2-runtime-derived-current-runtime-development-packet
         stage2-runtime-derived-pinned-packet)

(declare sh03-reader-resolve-source-path
         sh04-syntax-resolve-source-path)

(declare jvm-backend-validate-plan!)

(defn stage2-runtime-derived-packet
  "Build the target-neutral, verified stage2 packet shared by executable\n  backends.  The packet owns source/front-end/plan/runtime authenticity and\n  differential execution; concrete backends only validate and emit their\n  target instruction subset.\n\n  `:jvm` remains the bootstrap seed target and may be explicitly overridden by\n  a backend lowering request.  A non-seed source target is a real constraint\n  and must equal the requested target.  The eligibility decision is carried as\n  evidence rather than erased or relabelled."
  ([source-path source-text requested-target]
    (stage2-runtime-derived-packet source-path source-text requested-target {}))
  ([source-path source-text requested-target {:keys [validate-plan!
                                                     current-runtime-development?]
                                              :as options}]
    (if current-runtime-development?
      (stage2-runtime-derived-current-runtime-development-packet
       source-path source-text requested-target options)
      (stage2-runtime-derived-pinned-packet
       source-path source-text requested-target validate-plan!))))

(defn- stage2-runtime-derived-pinned-packet
  [source-path source-text requested-target validate-plan!]
    (when-not (contains? stage2-runtime-derived-source-targets requested-target)
      (stage2-runtime-derived-packet-fail!
        "C14-UNSUPPORTED"
        "requested runtime-derived target is not supported"
        source-path
        requested-target
        nil
        {:requested-target requested-target,
         :supported-targets (vec (sort stage2-runtime-derived-source-targets)),
         :missing-fact :supported-runtime-derived-target}))
    (binding [*additional-bootstrap-targets* stage2-runtime-derived-source-targets]
      (let [state (hash-map
                    'source-path
                    source-path
                    'source-text
                    source-text
                    'requested-target
                    requested-target
                    'validate-plan!
                    validate-plan!)
            state (__gravity_bootstrap_runtime_packet_packet_construction_phase_01
                    state)
            state (__gravity_bootstrap_runtime_packet_packet_construction_phase_02
                    state)
            state (__gravity_bootstrap_runtime_packet_packet_construction_phase_03
                    state)
            state (__gravity_bootstrap_runtime_packet_packet_construction_phase_04
                    state)]
        (__gravity_bootstrap_runtime_packet_packet_identity state))))

(defn- stage2-runtime-current-expression-binding
  [source-path plan]
  (let [binding (p15-s23-stage2-current-expression-development-binding!
                 source-path)
        compiler (:compiler plan)
        record
        {:artifact :gravity/p15-s23-stage2-expression-lowering-binding
         :source-content-hash (:expression-lowering-source-content-hash compiler)
         :semantic-hash (:expression-lowering-semantic-hash compiler)
         :artifact-hash (:expression-lowering-artifact-hash compiler)
         :functions (:functions binding)
         :invoked? (true? (:expression-lowering-invoked? compiler))
         :generic-bridge-residual?
         (true? (:expression-lowering-generic-bridge-residual? compiler))
         :plan-assembly-function (:plan-assembly-function compiler)
         :plan-assembly-artifact-hash (:plan-assembly-artifact-hash compiler)
         :plan-assembly-source-content-hash
         (:plan-assembly-source-content-hash compiler)
         :plan-assembly-semantic-hash (:plan-assembly-semantic-hash compiler)
         :plan-assembly-invoked? (true? (:plan-assembly-invoked? compiler))
         :plan-assembly-generic-bridge-residual?
         (true? (:plan-assembly-generic-bridge-residual? compiler))
         :clojure-seed-boundary? true
         :self-hosted? false
         :source-contract-version 2
         :functions-semantic-hash (:expression-lowering-functions-semantic-hash compiler)
         :function-count (:expression-lowering-function-count compiler)}
        binding-summary
        (assoc (select-keys binding [:source-path :source-content-hash :semantic-hash
                                     :artifact-hash :functions-semantic-hash
                                     :function-count :source-contract-version
                                     :source-mode :status])
               :plan-id (get-in binding [:source-program :plan-id]))
        summary-valid?
        (and (= :development-current (:source-mode binding))
             (= :complete (:status binding))
             (= (:source-content-hash binding) (:source-content-hash record))
             (= (:semantic-hash binding) (:semantic-hash record))
             (= (:artifact-hash binding) (:artifact-hash record))
             (= (:functions-semantic-hash binding)
                (:functions-semantic-hash record))
             (= (:function-count binding) (:function-count record))
             (p15-s23-stage2-current-expression-development-record-authentic?
              record binding plan))]
    (when-not summary-valid?
      (p15-s23-stage2-plan-emitter-fail!
       "P15S23Q002" source-path record
       {:missing-fact :current-expression-development-binding-plan-join}))
    {:record record :binding-summary binding-summary}))

(defn stage2-runtime-current-reader-invocation
  []
  {:reader-source-path (sh03-reader-resolve-source-path)
   :authenticated-envelope-options {:source :development-current}
   :syntax-source-selection
   {:schema :gravity/sh04-development-syntax-selection-v1
    :source-path (sh04-syntax-resolve-source-path)}})

(defn stage2-runtime-derived-current-runtime-development-packet
  [source-path source-text requested-target {:keys [validate-plan!]}]
  (when-not (contains? #{:c :jvm} requested-target)
    (stage2-runtime-derived-packet-fail!
     "C14-UNSUPPORTED"
     "current-source runtime development lowering does not support this target"
     source-path requested-target nil
     {:requested-target requested-target
      :supported-targets [:c :jvm]
      :missing-fact :current-runtime-development-target}))
  (let [emitter-rule (c-backend-stage2-plan-emitter-source-rule!
                      source-path requested-target)
        reader-invocation (stage2-runtime-current-reader-invocation)
        plan (try
               (p15-s23-stage2-plan-emitter-compile-source
                (:emitter emitter-rule) source-path source-text reader-invocation)
               (catch clojure.lang.ExceptionInfo ex (throw ex))
               (catch Exception ex
                 (stage2-runtime-derived-packet-fail!
                  "C14-UNSUPPORTED"
                  "current Reader stage2 plan emission failed"
                  source-path requested-target nil
                  {:cause-message (.getMessage ex)
                   :missing-fact :current-runtime-development-plan-emission})))
        _ (when validate-plan! (validate-plan! plan))
        _ (when-not (and
                     (= :gravity/stage2-hosted-core-compiled-plan (:kind plan))
                     (= :p15-s23-stage2-plan-emitter
                        (get-in plan [:compiler :rule-source]))
                     (map? (:functions plan))
                     (symbol? (:entrypoint plan))
                     (contains? (:functions plan) (:entrypoint plan))
                     (vector? (get-in plan [:functions (:entrypoint plan)
                                            :instructions])))
            (stage2-runtime-derived-packet-fail!
             "C14-INPUT" "stage2 plan integrity validation failed"
             source-path requested-target plan
             {:missing-fact :stage2-plan-integrity}))
        raw-source-target (get-in plan [:module :target])
        source-target (if (= :js raw-source-target) :js-ts raw-source-target)
        target-eligibility
        (cond
          (= source-target requested-target)
          {:status :accepted :source-declared-target source-target
           :requested-target requested-target :selection :source-and-request-agree}
          (= :jvm source-target)
          {:status :accepted :source-declared-target :jvm
           :requested-target requested-target
           :selection :explicit-bootstrap-seed-target-override
           :bootstrap-seed-target? true}
          :else
          (stage2-runtime-derived-packet-fail!
           "C14-PROFILE"
           "source target constraint is incompatible with requested target"
           source-path requested-target plan
           {:source-declared-target source-target
            :requested-target requested-target
            :missing-fact :target-constraint-compatibility}))
        closed-plan-validation
        (p15-s23-closed-runtime-plan-validation!
         source-path requested-target plan)
        current-expression (stage2-runtime-current-expression-binding
                           source-path plan)
        compiler-artifact-record (:record current-expression)
        runtime-rule (c-backend-current-runtime-development-source-rule!
                      source-path requested-target (:emitter emitter-rule))
        runtime-plan (:runtime-artifact-plan runtime-rule)
        closed-plan-execution
        (p15-s23-stage2-runtime-execute-function
         (:runtime runtime-rule) runtime-plan
         'p15-s23-runtime-execute-closed-plan [plan])
        reference-output (execute-stage0-compiled-plan plan)
        _ (when-not (and
                     (= :gravity/p15-s23-runtime-closed-plan-execution-record
                        (:artifact closed-plan-execution))
                     (= (:entrypoint plan) (:entrypoint closed-plan-execution))
                     (= :complete (:status closed-plan-execution))
                     (string? (:stdout closed-plan-execution))
                     (true? (:clojure-seed-boundary? closed-plan-execution))
                     (false? (:self-hosted? closed-plan-execution))
                     (= reference-output (:stdout closed-plan-execution)))
            (p15-s23-stage2-runtime-executor-fail!
             "P15S23X003" source-path closed-plan-execution
             {:target requested-target
              :reference-output reference-output
              :missing-fact :current-runtime-development-output-equivalence}))
        stdout-hash (str "sha256:" (sha256-hex (:stdout closed-plan-execution)))
        execution-hash
        (str "sha256:" (sha256-hex
                         (pr-str (c-backend-canonical-value
                                  closed-plan-execution))))
        invocation
        {:artifact :gravity/current-runtime-development-invocation-record
         :function 'p15-s23-runtime-execute-closed-plan
         :function-hash (:runtime-artifact-closed-plan-function-hash runtime-rule)
         :runtime-artifact-hash (:runtime-artifact-hash runtime-rule)
         :runtime-plan-id (:plan-id runtime-plan)
         :plan-id (:plan-id plan)
         :stdout-hash stdout-hash
         :execution-hash execution-hash
         :invocation-count 1
         :invocation-count-scope :current-runtime-development-packet
         :verification-replays-excluded? true
         :status :complete
         :clojure-seed-boundary? true
         :self-hosted? false}
        execution
        {:artifact :gravity/current-runtime-development-execution-record
         :plan-id (:plan-id plan)
         :runtime-plan-id (:plan-id runtime-plan)
         :entrypoint (:entrypoint plan)
         :stdout (:stdout closed-plan-execution)
         :status :complete
         :clojure-seed-boundary? true
         :self-hosted? false}
        adapter-record
        {:artifact :gravity/current-runtime-development-adapter-record
         :runtime-source-content-hash (:runtime-source-content-hash runtime-rule)
         :compatibility-source-content-hash
         (get-in runtime-rule [:current-runtime-compatibility-link
                               :source-content-hash])
         :runtime-rule-hash (:runtime-rule-hash runtime-rule)
         :revision-status :development-unadmitted
         :effects :not-checked
         :clojure-seed-boundary? true
         :self-hosted? false}
        adapter-record
        (assoc adapter-record
               :record-hash
               (str "sha256:" (sha256-hex
                                (pr-str (c-backend-canonical-value
                                         adapter-record)))))
        driver-record
        {:artifact :gravity/current-runtime-development-comparison-record
         :source-id (str "sha256:" (sha256-hex source-text))
         :stage2-plan-id (:plan-id plan)
         :stage2-runtime-execution-record execution
         :stage2-runtime-executed? true
         :accepted-output-equivalent? true
         :current-runtime-output (:stdout closed-plan-execution)
         :stage0-reference-output reference-output
         :status :complete
         :comparison-boundary :comparison-only
         :revision-status :development-unadmitted
         :effects :not-checked
         :clojure-seed-boundary? true
         :self-hosted? false}
        source-content-hash (str "sha256:" (sha256-hex source-text))
        packet
        {:kind :gravity/current-source-runtime-development-packet
         :status :complete
         :requested-target requested-target
         :plan plan
         :stage2-plan-emitter-rule emitter-rule
         :stage2-runtime-rule runtime-rule
         :stage2-compiler-artifact-record compiler-artifact-record
         :current-expression-binding (:binding-summary current-expression)
         :current-runtime-development-comparison-record driver-record
         :stage2-runtime-execution-record execution
         :closed-plan-validation-record closed-plan-validation
         :closed-plan-execution-record closed-plan-execution
         :closed-plan-invocation-record invocation
         :runtime-contract-adapter-record adapter-record
         :reference-output reference-output
         :target-eligibility target-eligibility
         :provenance
         {:source-path source-path
          :source-content-hash source-content-hash
          :reader-invocation reader-invocation
          :actual-paths
          {:stage2-compiler-source (:stage2-compiler-source-path runtime-rule)
           :stage2-expression-lowering-source
           (p15-s23-stage2-compiler-artifact-source-path)
           :runtime-owner (:runtime-artifact-source-path runtime-rule)
           :runtime-compatibility-facade
           (get-in runtime-rule [:current-runtime-compatibility-link
                                 :source-path])}}
         :revision-status :development-unadmitted
         :effects :not-checked
         :clojure-seed-boundary? true
         :self-hosted? false}]
    packet))
