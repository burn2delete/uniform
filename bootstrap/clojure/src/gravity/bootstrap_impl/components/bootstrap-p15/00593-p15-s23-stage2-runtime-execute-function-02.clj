
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
