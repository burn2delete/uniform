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
  (when (map? value)
    (if (sorted? value)
      ;; A sorted map can use a comparator that rejects a heterogeneous lookup
      ;; key before equality is considered. Runtime records may legitimately
      ;; be inspected for keyword control keys even when their own keys use a
      ;; different comparable type, so retain the equality scan for that case.
      (some (fn [entry]
              (when (= requested-key (key entry)) entry))
            value)
      ;; Ordinary runtime records are persistent hash/array maps. `find`
      ;; preserves their key equality and nil-value behavior without scanning
      ;; every entry for every interpreted instruction.
      (find value requested-key))))

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
  (fail! "L2-RECUR-TARGET"
         "recur has no compatible loop or function target"
         {:source-span {:source (get-in plan [:source :path])}
          :target-arity target-arity
          :actual-arity actual-arity
          :reason reason
          :remediation
          "Use recur only in tail position inside a compatible loop or function with matching arity."}))

(defn p15-s23-stage2-runtime-nontail-value!
  [plan value reason]
  (when (p15-s23-stage2-runtime-recur-signal? value)
    (p15-s23-stage2-runtime-recur-fail!
     plan nil (count (p15-s23-stage2-runtime-recur-values value)) reason))
  value)

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

(defn- p15-s23-stage2-runtime-execute-generic-builtin-call-scoped
  [runtime plan env instruction function public-invocation-scope]
  (let [args (p15-s23-stage2-runtime-execute-values-scoped
              runtime plan env (:args instruction)
              :recur-inside-builtin-argument public-invocation-scope)]
    (if (and (= 'str function)
             (:runtime-artifact-plan runtime))
      (case (count args)
        1 (p15-s23-stage2-runtime-artifact-invoke
           runtime p15-s23-stage2-runtime-artifact-function args)
        2 (p15-s23-stage2-runtime-artifact-invoke
           runtime p15-s23-stage2-runtime-artifact-concat-function args)
        (p15-s23-stage2-runtime-fail-call-arity!
         "L2-BUILTIN-ARITY" plan function args "1 or 2"))
      (if (contains? instruction :compiler-internal-caller)
        (p15-s23-sh07-runtime-invoke-compiler-internal-builtin
         runtime plan instruction args public-invocation-scope)
        (p15-s23-stage2-runtime-invoke-builtin
         runtime plan function args instruction public-invocation-scope)))))

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
