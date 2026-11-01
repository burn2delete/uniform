(def ^:private p15-s23-stage2-runtime-source-throw-token (Object.))
(defn- p15-s23-stage2-runtime-source-throw! [plan payload]
  (throw (ex-info "Gravity single-source seed throw"
                  {::source-throw p15-s23-stage2-runtime-source-throw-token
                   :payload payload :source (:source plan) :module (get-in plan [:module :module])})))

(defn p15-s23-stage2-runtime-recur-signal
  [values]
  (P15S23Stage2RuntimeRecurSignal. (vec values)))

(defn- p15-s23-stage2-runtime-map-entry
  [value requested-key]
  (gravity.runtime/map-entry value requested-key))

(defn- p15-s23-stage2-runtime-recur-values
  [value]
  (when (instance? P15S23Stage2RuntimeRecurSignal value)
    (.-values ^P15S23Stage2RuntimeRecurSignal value)))

(defn p15-s23-stage2-runtime-recur-signal?
  [value]
  (boolean
   (and (instance? P15S23Stage2RuntimeRecurSignal value)
        (vector? (p15-s23-stage2-runtime-recur-values value)))))

(defn p15-s23-stage2-runtime-recur-fail!
  [plan target-arity actual-arity reason]
  (gravity.runtime/fail-recur! #'fail! plan target-arity actual-arity reason))

(def ^:private p15-s23-runtime-nontail-operations
  {:recur-signal? #'p15-s23-stage2-runtime-recur-signal?
   :recur-fail! #'p15-s23-stage2-runtime-recur-fail!
   :recur-values #'p15-s23-stage2-runtime-recur-values})

(defn p15-s23-stage2-runtime-nontail-value!
  [plan value reason]
  (gravity.runtime/nontail-value! plan value reason p15-s23-runtime-nontail-operations))

(declare ^:private p15-s23-stage2-runtime-execute-instruction-scoped)

(declare ^:private p15-s23-runtime-evaluation-operations)

(defn- p15-s23-stage2-runtime-execute-value-scoped
  [runtime plan env instruction reason public-invocation-scope]
  (gravity.runtime/execute-value-scoped runtime plan env instruction reason public-invocation-scope p15-s23-runtime-evaluation-operations))

(defn- p15-s23-stage2-runtime-execute-value
  [runtime plan env instruction reason]
  (p15-s23-stage2-runtime-execute-value-scoped
   runtime plan env instruction reason nil))

(defn- p15-s23-stage2-runtime-bind-values
  [base names values]
  (gravity.runtime/bind-values base names values))

(defn- p15-s23-stage2-runtime-execute-values-scoped
  [runtime plan env instructions reason public-invocation-scope]
  (gravity.runtime/execute-values-scoped runtime plan env instructions reason public-invocation-scope p15-s23-runtime-evaluation-operations))

(defn p15-s23-stage2-runtime-execute-values
  [runtime plan env instructions reason]
  (p15-s23-stage2-runtime-execute-values-scoped
   runtime plan env instructions reason nil))

(defn- p15-s23-stage2-runtime-execute-map-entries-scoped
  "Evaluate map-literal entries without allocating an intermediate pair.

  Compiler plans represent entries as a vector of {:key ... :value ...}
  records. The old `into {}`/`map` path evaluated each record into a fresh
  two-element vector before conjoining it into the transient map. Large
  compiler artifacts contain many map literals, so that short-lived carrier
  became a measurable allocation and sequence-traversal hot path. Keep the
  same left-to-right key/value evaluation and last-key-wins semantics while
  associating directly into one transient map. The sequence branch preserves
  the public helper's established behavior for direct list callers.
  "
  [runtime plan env entries public-invocation-scope]
  (gravity.runtime/execute-map-entries-scoped runtime plan env entries public-invocation-scope p15-s23-runtime-evaluation-operations))

(defn- p15-s23-stage2-runtime-execute-map-entries
  [runtime plan env entries]
  (p15-s23-stage2-runtime-execute-map-entries-scoped
   runtime plan env entries nil))

(defn- p15-s23-stage2-runtime-execute-instructions-scoped
  [runtime plan env instructions public-invocation-scope]
  (gravity.runtime/execute-instructions-scoped runtime plan env instructions public-invocation-scope p15-s23-runtime-evaluation-operations))

(defn p15-s23-stage2-runtime-execute-instructions
  [runtime plan env instructions]
  (p15-s23-stage2-runtime-execute-instructions-scoped
   runtime plan env instructions nil))

(def ^:private p15-s23-runtime-generic-call-operations
  {:execute-values-scoped #'p15-s23-stage2-runtime-execute-values-scoped
   :artifact-invoke #'p15-s23-stage2-runtime-artifact-invoke
   :artifact-function #'p15-s23-stage2-runtime-artifact-function
   :artifact-concat-function #'p15-s23-stage2-runtime-artifact-concat-function
   :fail-call-arity! #'p15-s23-stage2-runtime-fail-call-arity!
   :invoke-compiler-internal-builtin #'p15-s23-sh07-runtime-invoke-compiler-internal-builtin
   :invoke-builtin #'p15-s23-stage2-runtime-invoke-builtin})

(defn- p15-s23-stage2-runtime-execute-generic-builtin-call-scoped
  [runtime plan env instruction function public-invocation-scope]
  (gravity.runtime/execute-generic-builtin-call-scoped runtime plan env instruction function public-invocation-scope p15-s23-runtime-generic-call-operations))

(defn- p15-s23-stage2-runtime-execute-generic-builtin-call
  [runtime plan env instruction function]
  (p15-s23-stage2-runtime-execute-generic-builtin-call-scoped
   runtime plan env instruction function nil))

(def ^:private p15-s23-runtime-evaluation-operations
  {:nontail-value! #'p15-s23-stage2-runtime-nontail-value!
   :execute-instruction-scoped #'p15-s23-stage2-runtime-execute-instruction-scoped
   :execute-value-scoped #'p15-s23-stage2-runtime-execute-value-scoped
   :recur-signal? #'p15-s23-stage2-runtime-recur-signal?
   :recur-fail! #'p15-s23-stage2-runtime-recur-fail!
   :recur-values #'p15-s23-stage2-runtime-recur-values})
