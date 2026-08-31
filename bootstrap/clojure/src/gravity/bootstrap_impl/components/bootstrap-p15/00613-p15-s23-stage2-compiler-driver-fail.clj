

(defn p15-s23-stage2-compiler-driver-fail!
  [id source-path value data]
  (fail! id
         (get p15-s23-stage2-compiler-driver-diagnostic-messages
              id
              "P15-S23 stage2 compiler driver proof failed")
         (merge {:source-span {:source source-path}
                 :stage :p15-s23-stage2-compiler-driver
                 :diagnostic-family :p15-s23-stage2-compiler-driver
                 :value value
                 :remediation "Keep the stage2 compiler driver authored in Gravity source, execute accepted and rejected fixtures through the declared hosted driver boundary, record residual Clojure seed surfaces, and keep full self-hosting claims false."}
                data)))

(defn p15-s23-stage2-compiler-driver-diagnostic-record
  [source-path id value data]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p15-s23-stage2-compiler-driver
   :source-span {:source source-path}
   :message (get p15-s23-stage2-compiler-driver-diagnostic-messages id)
   :facts data
   :observed value
   :remediation :repair_stage2_compiler_driver})

(defn p15-s23-stage2-compiler-driver-rule-record
  [driver]
  (let [steps (set (:driver-steps driver))
        uses (set (get-in driver [:execution-contract :uses]))
        missing-steps
        (set/difference p15-s23-stage2-compiler-driver-required-steps
                        steps)
	        required-uses #{:stage2-front-end-executor
	                        :stage2-source-front-end
	                        :stage2-plan-emitter
	                        :stage2-runtime-kernel
	                        :stage2-runtime-executor}
        missing-uses (set/difference required-uses uses)
        complete?
        (and (= :gravity-stage2-compiler-driver-rules-v1
                (:engine driver))
             (= :gravity-source (:implemented-by driver))
             (= :clojure-stage0-driver-host (:executed-by driver))
             (empty? missing-steps)
             (empty? missing-uses))]
    {:artifact :gravity/p15-s23-stage2-compiler-driver-rule-record
     :engine (:engine driver)
     :implemented-by (:implemented-by driver)
     :executed-by (:executed-by driver)
     :driver-steps (p15-s23-stage2-sort-values steps)
     :uses (p15-s23-stage2-sort-values uses)
     :missing-steps (p15-s23-stage2-sort-values missing-steps)
     :missing-uses (p15-s23-stage2-sort-values missing-uses)
     :rule-set-complete? complete?
     :status (if complete? :complete :failed)}))

(defn p15-s23-stage2-compiler-driver-run-source
  [driver front-end emitter runtime source-path source-text]
  (let [front-end-record
        (p15-s23-stage2-front-end-source-module-record
         front-end source-path source-text)
        module (:module front-end-record)
        stage2-plan (p15-s23-stage2-emitted-core-plan
                     emitter source-path source-text module)
        stage2-runtime-record
        (p15-s23-stage2-runtime-execute-plan runtime stage2-plan)
        stage0-plan (stage0-compiled-core-plan source-path source-text module)
        stage0-output (execute-stage0-compiled-plan stage0-plan)
        ;; The declared accepted-scope stdout is normative for the canonical
        ;; core-app fixture only.  The public stage2 driver may also be used
        ;; for another accepted source unit; in that case the stage0 result is
        ;; the comparison oracle while the contract still governs the driver
        ;; steps and boundaries.
        expected-stdout (if (= (str source-path)
                               (str p15-s23-accepted-app-source-path))
                          (get-in driver [:accepted-scope
                                          :expected-stdout])
                          stage0-output)
        normalized-stage2-bindings
        (mapv #(assoc % :visibility :local)
              (:binding-table stage2-plan))
        normalized-stage0-bindings
        (mapv #(assoc % :visibility :local)
              (:binding-table stage0-plan))
        binding-table-equivalent?
        (= normalized-stage2-bindings normalized-stage0-bindings)
        function-instructions-equivalent?
        (= (update-vals (:functions stage2-plan)
                        #(select-keys % [:params :body :arity
                                         :body-form-count :instructions]))
           (update-vals (:functions stage0-plan)
                        #(select-keys % [:params :body :arity
                                         :body-form-count :instructions])))
        instruction-summary-equivalent?
        (= (:instruction-summary stage2-plan)
           (:instruction-summary stage0-plan))
        effect-summary-equivalent?
        (= (:effect-summary stage2-plan)
           (:effect-summary stage0-plan))
        accepted-output-equivalent?
        (and (= (:stdout stage2-runtime-record) stage0-output)
             (= (:stdout stage2-runtime-record) expected-stdout))
        complete?
        (and binding-table-equivalent?
             function-instructions-equivalent?
             instruction-summary-equivalent?
             effect-summary-equivalent?
             accepted-output-equivalent?)]
    {:artifact :gravity/p15-s23-stage2-compiler-driver-run-record
     :fixture source-path
     :driver-engine (:engine driver)
     :driver-steps (:driver-steps driver)
     :source-id (str "sha256:" (sha256-hex source-text))
     :stage2-plan-id (:plan-id stage2-plan)
     ;; Keep the actual products available to downstream target lowerers.  The
     ;; driver remains the owner of plan emission and runtime execution; these
     ;; fields are intentionally not included in the identity hash.
     :stage2-plan stage2-plan
     :stage0-plan-id (:plan-id stage0-plan)
     :stage2-plan-kind (:kind stage2-plan)
     :stage0-plan-kind (:kind stage0-plan)
     :entrypoint (:entrypoint stage2-plan)
     :stage2-source-front-end-record
     (select-keys front-end-record
                  [:artifact :source-path :source-id :status])
     :stage2-runtime-execution-record stage2-runtime-record
     :stage2-driver-output (:stdout stage2-runtime-record)
     :stage0-reference-output stage0-output
     :expected-stdout expected-stdout
     :stage2-source-front-end-executed? true
     :stage2-plan-emitted? true
     :stage2-runtime-executed? true
     :binding-table-equivalent? binding-table-equivalent?
     :binding-table-visibility-normalized? true
     :function-instructions-equivalent?
     function-instructions-equivalent?
     :instruction-summary-equivalent?
     instruction-summary-equivalent?
     :effect-summary-equivalent? effect-summary-equivalent?
     :accepted-output-equivalent? accepted-output-equivalent?
     :status (if complete? :complete :failed)}))

(defn p15-s23-stage2-compiler-driver-accepted-record
  [driver front-end emitter runtime]
  (p15-s23-stage2-compiler-driver-run-source
   driver
   front-end
   emitter
   runtime
   p15-s23-accepted-app-source-path
   (slurp p15-s23-accepted-app-source-path)))

(defn p15-s23-stage2-compiler-driver-rejected-records
  [driver front-end emitter runtime]
  (mapv
   (fn [{:keys [fixture expected-diagnostic rejected-design]}]
     (try
       (let [source-text (slurp fixture)
             run-record
             (p15-s23-stage2-compiler-driver-run-source
              driver front-end emitter runtime fixture source-text)]
         {:fixture fixture
          :rejected-design rejected-design
          :expected-diagnostic expected-diagnostic
          :status :accepted-unexpectedly
          :run-record run-record
          :matches-expected? false})
       (catch clojure.lang.ExceptionInfo ex
         (let [data (ex-data ex)
               diagnostic (:id data)]
           {:fixture fixture
            :rejected-design rejected-design
            :expected-diagnostic expected-diagnostic
            :status :rejected
            :diagnostic diagnostic
            :message (:message data)
            :diagnostic-data data
            :matches-expected? (= expected-diagnostic diagnostic)}))))
   (:rejected-diagnostic-contract driver)))

(defn p15-s23-stage2-compiler-driver-rejected-record
  [records]
  (let [expected (set (map :expected-diagnostic records))
        observed (set (map :diagnostic records))
        mismatches (remove :matches-expected? records)
        matches? (and (= expected observed) (empty? mismatches))]
    {:artifact :gravity/p15-s23-stage2-compiler-driver-rejected-record
     :expected-diagnostics (p15-s23-stage2-sort-values expected)
     :observed-diagnostics (p15-s23-stage2-sort-values observed)
     :fixture-count (count records)
     :mismatch-count (count mismatches)
     :records records
     :status (if matches? :complete :failed)}))