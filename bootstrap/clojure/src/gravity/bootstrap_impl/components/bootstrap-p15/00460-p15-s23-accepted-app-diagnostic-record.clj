

(defn p15-s23-accepted-app-diagnostic-record
  [source-path id value data]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p15-s23-accepted-app-execution-proof
   :source-span {:source source-path}
   :message (get p15-s23-accepted-app-diagnostic-messages id)
   :facts data
   :observed value
   :remediation :repair_accepted_app_execution_proof})

(defn p15-s23-accepted-app-output-comparison
  [accepted-app-artifact expected-stdout]
  (let [accepted-stdout (get-in accepted-app-artifact
                                [:accepted-run :stdout])
        reference-stdout (get-in accepted-app-artifact
                                 [:reference-run :stdout])
        accepted-matches-reference? (= accepted-stdout reference-stdout)
        accepted-matches-expected? (= accepted-stdout expected-stdout)]
    {:artifact :gravity/p15-s23-accepted-output-comparison
     :accepted-command (get-in accepted-app-artifact
                               [:accepted-run :command])
     :reference-command (get-in accepted-app-artifact
                                [:reference-run :command])
     :accepted-stdout accepted-stdout
     :reference-stdout reference-stdout
     :expected-stdout expected-stdout
     :accepted-matches-reference? accepted-matches-reference?
     :accepted-matches-expected? accepted-matches-expected?
     :status (if (and accepted-matches-reference?
                      accepted-matches-expected?)
               :complete
               :failed)}))

(defn p15-s23-compiled-plan-execution-trace
  [accepted-app-artifact]
  (let [plan (:compiled-plan accepted-app-artifact)
        proof (:capability-based-proof accepted-app-artifact)]
    {:artifact :gravity/p15-s23-compiled-plan-execution-trace
     :compiled-plan-id (:plan-id plan)
     :compiled-plan-kind (:kind plan)
     :entrypoint (:entrypoint plan)
     :instruction-summary (:instruction-summary plan)
     :effect-summary (:effect-summary plan)
     :accepted-stdout (get-in accepted-app-artifact
                              [:accepted-run :stdout])
     :reference-stdout (get-in accepted-app-artifact
                               [:reference-run :stdout])
     :compiled-plan-emitted? (:compiled-plan-emitted? proof)
     :compiled-plan-executed? (:compiled-plan-executed? proof)
     :source-form-interpreter-replaced?
     (:source-form-interpreter-replaced? proof)
     :status (if (and (= :gravity/stage0-hosted-core-compiled-plan
                         (:kind plan))
                      (true? (:compiled-plan-executed? proof)))
               :complete
               :failed)}))

(defn p15-s23-accepted-app-runtime-capability-use-record
  [accepted-app-artifact runtime-artifact]
  (let [effect-summary (get-in accepted-app-artifact
                               [:compiled-plan :effect-summary])
        declared-effects (get-in accepted-app-artifact
                                 [:module :effects])
        declared-capabilities (get-in accepted-app-artifact
                                      [:module :capabilities])]
    {:artifact :gravity/p15-s23-accepted-app-runtime-capability-use-record
     :application-effects declared-effects
     :application-capabilities declared-capabilities
     :compiled-effect-summary effect-summary
     :required-effects #{:io/write}
     :required-capabilities #{:io/stdout}
     :effect-capability-check-passed?
     (true? (get-in accepted-app-artifact
                    [:capability-based-proof
                     :effects-and-capabilities-checked?]))
     :runtime-enforcement-artifact-id (:artifact-id runtime-artifact)
     :runtime-capability-proof-id (:proof-id runtime-artifact)
     :runtime-capability-manifest
     (select-keys (:runtime-capability-manifest runtime-artifact)
                  [:artifact :deny-by-default? :handles :decisions
                   :status])
     :boundary
     :application_stdout_checked_by_stage0_runner_and_linked_to_p15_runtime_evidence
     :status :complete}))

(defn p15-s23-accepted-app-trusted-boundary-record
  [accepted-app-artifact]
  {:artifact :gravity/p15-s23-accepted-app-trusted-boundary
   :compiler (get-in accepted-app-artifact
                     [:trusted-boundary :compiler])
   :runtime (get-in accepted-app-artifact
                    [:trusted-boundary :runtime])
   :instruction-plan?
   (true? (get-in accepted-app-artifact
                  [:trusted-boundary :instruction-plan?]))
   :direct-form-interpreter? false
   :clojure-instruction-runner? true
   :self-hosted-compiler? false
   :clojure-seed-retired? false
   :seed-boundary :clojure-stage0-bootstrap
   :retirement-condition :complete-p15-s23-evidence-bundle
   :status :complete})

(defn p15-s23-accepted-app-proof-diagnostics
  [source-path candidate]
  (let [proof-contract (:proof-contract candidate)
        accepted-app-artifact (:accepted-app-artifact candidate)
        output-comparison (:accepted-output-comparison candidate)
        execution-trace (:compiled-plan-execution-trace candidate)
        runtime-artifact (:runtime-capability-artifact candidate)
        trusted-boundary (:trusted-boundary-record candidate)
        runtime-use (:runtime-capability-use-record candidate)
        claims (:self-hosting-claims proof-contract)
        preserves (set (:preserves proof-contract))
        missing-preserves
        (set/difference p15-s23-accepted-app-required-preserves preserves)]
    (vec
     (concat
      (when-not (= :gravity/accepted-app-execution-proof
                   (:artifact proof-contract))
        [(p15-s23-accepted-app-diagnostic-record
          source-path "P15S23A001" proof-contract
          {:missing-fields [:artifact]})])
      (when-not
       (and (= :gravity/stage0-hosted-core-compiled-app-proof
               (:kind accepted-app-artifact))
            (= p15-s23-accepted-app-source-path
               (get-in accepted-app-artifact [:source :path]))
            (= 'core.app (get-in accepted-app-artifact
                                  [:module :module]))
            (= :hosted (get-in accepted-app-artifact
                                [:module :profile]))
            (= :jvm (get-in accepted-app-artifact
                             [:module :target]))
            (= #{:io/write} (get-in accepted-app-artifact
                                     [:module :effects]))
            (contains? (set (get-in accepted-app-artifact
                                     [:module :capabilities]))
                       :io/stdout)
            (= :gravity/stage0-hosted-core-compiled-plan
               (get-in accepted-app-artifact [:compiled-plan :kind]))
            (true? (get-in accepted-app-artifact
                           [:capability-based-proof
                            :compiled-plan-executed?]))
            (true? (get-in accepted-app-artifact
                           [:capability-based-proof
                            :function-instructions-covered?]))
            (empty? missing-preserves))
        [(p15-s23-accepted-app-diagnostic-record
          source-path "P15S23A002" accepted-app-artifact
          {:missing-preserves (vec (sort missing-preserves))
           :required-fixture p15-s23-accepted-app-source-path})])
      (when-not
       (and (= :gravity/p15-s23-accepted-output-comparison
               (:artifact output-comparison))
            (= :complete (:status output-comparison))
            (true? (:accepted-matches-reference? output-comparison))
            (true? (:accepted-matches-expected? output-comparison))
            (= (:accepted-stdout output-comparison)
               (:accepted-stdout execution-trace)))
        [(p15-s23-accepted-app-diagnostic-record
          source-path "P15S23A003" output-comparison
          {:expected-stdout p15-s23-accepted-app-expected-stdout})])
      (when-not
       (and (= :gravity/p15-s23-runtime-manifest-capability-enforcement-artifact
               (:kind runtime-artifact))
            (= :gravity/p15-s23-core-lowering-diagnostic-preservation-artifact
               (get-in runtime-artifact
                       [:core-diagnostic-artifact :kind]))
            (= :gravity/p15-s23-compiler-pipeline-manifest-artifact
               (get-in runtime-artifact
                       [:compiler-pipeline-manifest-artifact :kind]))
            (re-find #"^sha256:" (str (:artifact-id runtime-artifact)))
            (re-find #"^sha256:" (str (get-in accepted-app-artifact
                                               [:compiled-plan
                                                :plan-id])))
            (= :complete (:status runtime-use))
            (true? (:effect-capability-check-passed? runtime-use)))
        [(p15-s23-accepted-app-diagnostic-record
          source-path "P15S23A004" runtime-artifact
          {:required-links [:runtime-capability-artifact
                            :compiler-pipeline-manifest-artifact
                            :core-diagnostic-artifact
                            :compiled-plan-id]})])
      (when-not
       (and (= :gravity/p15-s23-accepted-app-trusted-boundary
               (:artifact trusted-boundary))
            (= :clojure/jvm (:compiler trusted-boundary))
            (= :clojure/jvm (:runtime trusted-boundary))
            (true? (:instruction-plan? trusted-boundary))
            (false? (:direct-form-interpreter? trusted-boundary))
            (true? (:clojure-instruction-runner? trusted-boundary))
            (false? (:self-hosted-compiler? trusted-boundary))
            (false? (:clojure-seed-retired? trusted-boundary)))
        [(p15-s23-accepted-app-diagnostic-record
          source-path "P15S23A005" trusted-boundary
          {:required-boundary [:clojure-stage0-bootstrap
                               :clojure-instruction-runner
                               :self-hosted-compiler-false
                               :clojure-seed-retired-false]})])
      (when (or (true? (:full-language-compiler-self-hosted? claims))
                (true? (:clojure-seed-retired? claims)))
        [(p15-s23-accepted-app-diagnostic-record
          source-path "P15S23A006" claims
          {:full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? claims)
           :clojure-seed-retired?
           (:clojure-seed-retired? claims)})])))))

(defn p15-s23-accepted-app-diagnostic-stream
  [source-path proof-id]
  {:artifact :gravity/p15-s23-accepted-app-execution-diagnostic-stream
   :stage :p15-s23-accepted-app-execution-proof
   :source-path source-path
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :p15-s23-accepted-app-execution-proof
            :message (get p15-s23-accepted-app-diagnostic-messages id)})
         p15-s23-accepted-app-diagnostic-ids)
   :status :complete})