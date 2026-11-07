(declare p15-s23-stage2-module-entry-scope! p15-s23-stage2-module-scope? p15-s23-stage2-module-execute-function
         p15-s23-runtime-stage-operations)

(defn- p15-s23-stage2-runtime-execute-function-scoped
  [runtime plan callee args public-invocation-scope]
  (gravity.runtime/execute-function-scoped runtime plan callee args public-invocation-scope
                                           (p15-s23-runtime-stage-operations)))

(defn p15-s23-stage2-runtime-execute-function
  [runtime plan callee args]
  (gravity.runtime/execute-function runtime plan callee args (p15-s23-runtime-stage-operations)))

(defn p15-s23-stage2-runtime-artifact-invoke
  [runtime function args]
  (gravity.runtime/artifact-invoke runtime function args (p15-s23-runtime-stage-operations)))

(defn p15-s23-stage2-runtime-execute-plan
  [runtime plan]
  (gravity.runtime/execute-plan runtime plan (p15-s23-runtime-stage-operations)))

(defn- p15-s23-runtime-stage-operations []
  {:native-scope! #'declared-reader-bridge/native-scope!
   :module-entry-scope! #'p15-s23-stage2-module-entry-scope!
   :registered-scope? #'declared-reader-bridge/registered-scope?
   :assert-function-entry! #'declared-reader-bridge/assert-function-entry!
   :assert-scoped-function-entry! #'p15-s23-sh07-b51-assert-scoped-function-entry!
   :module-scope? #'p15-s23-stage2-module-scope?
   :fail! #'fail!
   :fail-call-arity! #'p15-s23-stage2-runtime-fail-call-arity!
   :bind-values #'p15-s23-stage2-runtime-bind-values
   :execute-instructions-scoped #'p15-s23-stage2-runtime-execute-instructions-scoped
   :recur-signal? #'p15-s23-stage2-runtime-recur-signal?
   :recur-values #'p15-s23-stage2-runtime-recur-values
   :recur-fail! #'p15-s23-stage2-runtime-recur-fail!
   :module-execute-function #'p15-s23-stage2-module-execute-function
   :execute-function-scoped #'p15-s23-stage2-runtime-execute-function-scoped
   :executor-fail! #'p15-s23-stage2-runtime-executor-fail!
   :execute-function #'p15-s23-stage2-runtime-execute-function
   :execute-instructions #'p15-s23-stage2-runtime-execute-instructions})

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

	;; Declared-input reads are an authenticated Reader bridge, not a generic
	;; runtime primitive.  The executor's builtin table still requires the
	;; symbol, exact arity, and source-authenticated dispatch; the kernel
	;; primitive inventory must not pretend that this host bridge is implemented
	;; by the ordinary Gravity primitive table.
	(def ^:private p15-s23-stage2-runtime-reader-bridge-builtins
	  #{'build-read-declared-input})

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
	  (gravity.runtime/kernel-diagnostic-record
	   #'p15-s23-stage2-runtime-kernel-diagnostic-messages #'str/lower-case
	   source-path id value data))

	(defn p15-s23-stage2-runtime-kernel-rule-record
	  [kernel]
	  (gravity.runtime/kernel-rule-record
	   kernel
	   {:declared-builtin-exact-arities #'p15-s23-sh07-declared-builtin-exact-arities
	    :declared-builtin-contracts #'p15-s23-sh07-declared-builtin-contracts
	    :required-instructions #'p15-s23-stage2-runtime-kernel-required-instructions
	    :builtin-functions #'stage0-builtin-functions
	    :reader-bridge-builtins #'p15-s23-stage2-runtime-reader-bridge-builtins
	    :difference #'set/difference
	    :sort-values #'p15-s23-stage2-sort-values}))
