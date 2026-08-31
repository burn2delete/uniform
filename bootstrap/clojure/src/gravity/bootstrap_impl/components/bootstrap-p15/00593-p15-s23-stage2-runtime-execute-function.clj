

(defn p15-s23-stage2-runtime-execute-function
  [runtime plan callee args]
  (let [definition (get-in plan [:functions callee])
        params (:params definition)
        instructions (:instructions definition)]
    (when-not definition
      (fail! "L2-UNKNOWN-CORE-FORM"
             "stage2 runtime plan references an unknown function"
             {:source-span {:source (get-in plan [:source :path])}
              :function callee
              :remediation "Regenerate the stage2 instruction plan from source."}))
    (when-not (= (count params) (count args))
      (p15-s23-stage2-runtime-fail-call-arity!
       "L2-FUNCTION-ARITY" plan callee args (count params)))
    (loop [env (p15-s23-stage2-runtime-bind-values {} params args)]
      (let [result
            (p15-s23-stage2-runtime-execute-instructions
             runtime plan env instructions)]
        (if (p15-s23-stage2-runtime-recur-signal? result)
          (let [values (p15-s23-stage2-runtime-recur-values result)]
            (when-not (= (count params) (count values))
              (p15-s23-stage2-runtime-recur-fail!
               plan (count params) (count values)
               :function-arity-mismatch))
            (recur
             (p15-s23-stage2-runtime-bind-values {} params values)))
          result)))))

(defn p15-s23-stage2-runtime-artifact-invoke
  "Generic host runner for a Gravity-authored runtime function plan.

  The runner performs only plan lookup, arity validation, and invocation.  It
  deliberately records the remaining Clojure interpreter boundary in the
  caller's manifest; runtime semantics (the formatting function itself) come
  from the compiled Gravity source artifact."
  [runtime function args]
  (let [artifact-plan (:runtime-artifact-plan runtime)
        definition (get-in artifact-plan [:functions function])]
    (when-not (and (map? artifact-plan) (map? definition))
      (p15-s23-stage2-runtime-executor-fail!
       "P15S23X002"
       (:runtime-artifact-source-path runtime)
       artifact-plan
       {:missing-fact :runtime-artifact-function
        :function function}))
    (when-not (= (:arity definition) (count args))
      (p15-s23-stage2-runtime-executor-fail!
       "P15S23X002"
       (:runtime-artifact-source-path runtime)
       definition
       {:missing-fact :runtime-artifact-function-arity
        :function function
        :expected-arity (:arity definition)
        :actual-arity (count args)}))
    (p15-s23-stage2-runtime-execute-function
     ;; Disable nested artifact formatting while running the pure runtime
     ;; function set.  Its body may use normal stage2 builtins such as `str`.
     (dissoc runtime :runtime-artifact-plan)
     artifact-plan
     function
     args)))

(defn p15-s23-stage2-runtime-execute-plan
  [runtime plan]
  (let [main-function (get-in plan [:functions (:entrypoint plan)])
        params (:params main-function)
        result (atom nil)
        stdout
        (with-out-str
          (when-not main-function
            (fail! "L3-UNKNOWN-ALIAS"
                   "stage2 runtime plan requires a main function"
                   {:source-span {:source (get-in plan [:source :path])}
                    :remediation "Regenerate the stage2 plan from a module with main."}))
          (when-not (empty? params)
            (fail! "L2-MAIN-ARITY"
                   "stage2 runtime main must take no arguments"
                   {:source-span {:source (get-in plan [:source :path])}
                    :params params
                    :remediation "Use (defn main [] ...)."}))
          (reset! result
                  (p15-s23-stage2-runtime-execute-instructions
                   runtime plan {} (:instructions main-function))))]
	    {:artifact :gravity/p15-s23-stage2-runtime-execution-record
	     :runtime-engine (:engine runtime)
	     :plan-id (:plan-id plan)
	     :plan-kind (:kind plan)
     :entrypoint (:entrypoint plan)
     :stdout stdout
     :entrypoint-result @result
     :instruction-summary (:instruction-summary plan)
	     :effect-summary (:effect-summary plan)
	     :status :complete}))

	(def p15-s23-stage2-runtime-kernel-required-preserves
	  #{:source-spans :diagnostic-codes :instruction-semantics
	    :function-bindings :effects :capabilities :profile
	    :compiler-lineage :artifact-provenance})

	(def p15-s23-stage2-runtime-kernel-required-emits
	  #{:stage2-runtime-kernel-execution-record
	    :gravity-runtime-primitive-record
	    :accepted-output-comparison
	    :rejected-diagnostic-comparison
	    :stage2-runtime-kernel-boundary-record})

	(def p15-s23-stage2-runtime-kernel-required-instructions
	  p15-s23-stage2-runtime-executor-required-instructions)

	(def p15-s23-stage2-runtime-kernel-diagnostic-messages
	  {"P15S23K001" "P15-S23 stage2 runtime kernel contract is missing"
	   "P15S23K002" "P15-S23 stage2 runtime kernel rule set is incomplete"
	   "P15S23K003" "P15-S23 stage2 runtime kernel accepted output is not equivalent"
	   "P15S23K004" "P15-S23 stage2 runtime kernel rejected diagnostics are not preserved"
	   "P15S23K005" "P15-S23 stage2 runtime kernel evidence links are incomplete"
	   "P15S23K006" "P15-S23 stage2 runtime kernel preservation or emission contract is incomplete"
	   "P15S23K007" "P15-S23 stage2 runtime kernel boundary record is incomplete"
	   "P15S23K008" "P15-S23 stage2 runtime kernel makes an unsupported self-hosting or seed-retirement claim"})

	(def p15-s23-stage2-runtime-kernel-diagnostic-ids
	  ["P15S23K001" "P15S23K002" "P15S23K003" "P15S23K004"
	   "P15S23K005" "P15S23K006" "P15S23K007" "P15S23K008"])

	(defn p15-s23-stage2-runtime-kernel-fail!
	  [id source-path value data]
	  (fail! id
	         (get p15-s23-stage2-runtime-kernel-diagnostic-messages
	              id
	              "P15-S23 stage2 runtime kernel proof failed")
	         (merge {:source-span {:source source-path}
	                 :stage :p15-s23-stage2-runtime-kernel
	                 :diagnostic-family :p15-s23-stage2-runtime-kernel
	                 :value value
	                 :remediation "Keep the runtime kernel authored in Gravity source, prove hosted-core execution and primitive dispatch through the declared kernel boundary, and keep the remaining Clojure verifier/compiler seed boundary explicit."}
	                data)))

	(defn p15-s23-stage2-runtime-kernel-diagnostic-record
	  [source-path id value data]
	  {:artifact :gravity/diagnostic
	   :diagnostic-id (str "diag-" (str/lower-case id))
	   :diagnostic id
	   :severity :error
	   :stage :p15-s23-stage2-runtime-kernel
	   :source-span {:source source-path}
	   :message (get p15-s23-stage2-runtime-kernel-diagnostic-messages id)
	   :facts data
	   :observed value
	   :remediation :repair_stage2_runtime_kernel})

	(defn p15-s23-stage2-runtime-kernel-rule-record
	  [kernel]
	  (let [instruction-rules (set (keys (:instruction-rules kernel)))
	        runtime-primitives (set (get-in kernel
	                                        [:runtime-primitives :functions]))
	        missing-instructions
	        (set/difference
	         p15-s23-stage2-runtime-kernel-required-instructions
	         instruction-rules)
	        missing-primitives
	        (set/difference stage0-builtin-functions runtime-primitives)
	        primitive-boundary
	        (get-in kernel [:runtime-primitives
	                        :host-primitive-boundary])
	        complete?
	        (and (= :gravity-stage2-runtime-kernel-v1
	                (:engine kernel))
	             (= :stage2-instruction-plan (:input kernel))
	             (= :stage2-runtime-kernel-execution-record
	                (:output kernel))
	             (= :gravity-runtime-primitives primitive-boundary)
	             (empty? missing-instructions)
	             (empty? missing-primitives))]
	    {:artifact :gravity/p15-s23-stage2-runtime-kernel-rule-record
	     :engine (:engine kernel)
	     :primitive-engine (get-in kernel [:runtime-primitives :engine])
	     :input (:input kernel)
	     :output (:output kernel)
	     :primitive-boundary primitive-boundary
	     :instruction-rules (p15-s23-stage2-sort-values
	                         instruction-rules)
	     :runtime-primitives (p15-s23-stage2-sort-values
	                          runtime-primitives)
	     :missing-instructions (p15-s23-stage2-sort-values
	                            missing-instructions)
	     :missing-runtime-primitives (p15-s23-stage2-sort-values
	                                  missing-primitives)
	     :rule-set-complete? complete?
	     :status (if complete? :complete :failed)}))

	(defn p15-s23-stage2-runtime-kernel-accepted-record
	  [kernel emitter]
	  (let [source-path p15-s23-accepted-app-source-path
	        source-text (slurp source-path)
	        stage2-plan
	        (p15-s23-stage2-plan-emitter-compile-source
	         emitter source-path source-text)
	        kernel-output
	        (assoc (p15-s23-stage2-runtime-execute-plan kernel
	                                                   stage2-plan)
	               :artifact
	               :gravity/p15-s23-stage2-runtime-kernel-execution-record)
	        stage0-output (execute-stage0-compiled-plan stage2-plan)
	        expected-stdout (get-in kernel
	                                [:accepted-scope :expected-stdout])
	        accepted-output-equivalent?
	        (and (= (:stdout kernel-output) stage0-output)
	             (= (:stdout kernel-output) expected-stdout))
	        entrypoint-result-equivalent?
	        (nil? (:entrypoint-result kernel-output))
	        summary-equivalent?
	        (= (:instruction-summary kernel-output)
	           (:instruction-summary stage2-plan))
	        effect-summary-equivalent?
	        (= (:effect-summary kernel-output)
	           (:effect-summary stage2-plan))
	        equivalent?
	        (and accepted-output-equivalent?
	             entrypoint-result-equivalent?
	             summary-equivalent?
	             effect-summary-equivalent?)]
	    {:artifact :gravity/p15-s23-stage2-runtime-kernel-accepted-record
	     :fixture source-path
	     :stage2-plan-id (:plan-id stage2-plan)
	     :stage2-plan-kind (:kind stage2-plan)
	     :entrypoint (:entrypoint stage2-plan)
	     :kernel-execution-record kernel-output
	     :stage2-runtime-kernel-executed? true
	     :stage0-instruction-runner-output stage0-output
	     :stage2-runtime-kernel-output (:stdout kernel-output)
	     :expected-stdout expected-stdout
	     :entrypoint-result-equivalent? entrypoint-result-equivalent?
	     :instruction-summary-equivalent? summary-equivalent?
	     :effect-summary-equivalent? effect-summary-equivalent?
	     :accepted-output-equivalent? accepted-output-equivalent?
	     :status (if equivalent? :complete :failed)}))