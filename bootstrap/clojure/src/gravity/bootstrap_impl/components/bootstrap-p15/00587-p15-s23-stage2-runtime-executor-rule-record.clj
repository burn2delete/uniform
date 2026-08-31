

(defn p15-s23-stage2-runtime-executor-rule-record
  [runtime]
  (let [instruction-rules (set (keys (:instruction-rules runtime)))
        builtin-functions (set (get-in runtime
                                       [:builtin-rules :functions]))
        missing-instructions
        (set/difference
         p15-s23-stage2-runtime-executor-required-instructions
         instruction-rules)
        missing-builtins
        (set/difference stage0-builtin-functions builtin-functions)
        complete?
        (and (= :gravity-stage2-runtime-executor-rules-v1
                (:engine runtime))
             (= :stage2-instruction-plan (:input runtime))
             (= :stage2-runtime-execution-record (:output runtime))
             (empty? missing-instructions)
             (empty? missing-builtins))]
    {:artifact :gravity/p15-s23-stage2-runtime-executor-rule-record
     :engine (:engine runtime)
     :input (:input runtime)
     :output (:output runtime)
     :instruction-rules (p15-s23-stage2-sort-values instruction-rules)
     :builtin-functions (p15-s23-stage2-sort-values builtin-functions)
     :missing-instructions (p15-s23-stage2-sort-values
                            missing-instructions)
     :missing-builtin-functions (p15-s23-stage2-sort-values
                                 missing-builtins)
     :rule-set-complete? complete?
     :status (if complete? :complete :failed)}))

(defn p15-s23-stage2-runtime-fail-call-arity!
  [id plan callee args expected]
  (fail! id
         (case id
           "L2-FUNCTION-ARITY" "stage2 runtime function call has the wrong arity"
           "L2-BUILTIN-ARITY" "stage2 runtime builtin call has the wrong arity"
           "stage2 runtime call has the wrong arity")
         {:source-span {:source (get-in plan [:source :path])}
          :function callee
          :expected-arity expected
          :actual-arity (count args)
          :remediation "Call the function with the arity supported by the stage2 hosted-core runtime subset."}))

(defn p15-s23-stage2-runtime-assert-min-arity!
  [plan callee args n]
  (when (< (count args) n)
    (p15-s23-stage2-runtime-fail-call-arity!
     "L2-BUILTIN-ARITY" plan callee args (str "at least " n))))

(defn p15-s23-stage2-runtime-assert-exact-arity!
  [plan callee args n]
  (when-not (= n (count args))
    (p15-s23-stage2-runtime-fail-call-arity!
     "L2-BUILTIN-ARITY" plan callee args n)))

(defn p15-s23-stage2-runtime-assert-between-arity!
  [plan callee args min-n max-n]
  (when-not (<= min-n (count args) max-n)
    (p15-s23-stage2-runtime-fail-call-arity!
     "L2-BUILTIN-ARITY" plan callee args (str min-n " to " max-n))))

(defn p15-s23-stage2-runtime-assert-even-arity!
  [plan callee args]
  (when (odd? (count args))
    (p15-s23-stage2-runtime-fail-call-arity!
     "L2-BUILTIN-ARITY" plan callee args "an even number of arguments")))

(defn p15-s23-stage2-compiler-artifact-plan-context?
  [plan]
  (and (true? (:compiler-artifact-plan? plan))
       (= :gravity/stage2-compiler-artifact-plan (:kind plan))
       (= :meta (:profile (:module plan)))
       (= :p15-s23-stage2-expression-lowering
          (:stage (:compiler plan)))))

(defn- p15-s23-stage2-runtime-add
  [args]
  (case (count args)
    0 0
    1 (nth args 0)
    2 (+ (nth args 0) (nth args 1))
    3 (+ (nth args 0) (nth args 1) (nth args 2))
    (apply + args)))

(defn- p15-s23-stage2-runtime-subtract
  [args]
  (case (count args)
    1 (- (nth args 0))
    2 (- (nth args 0) (nth args 1))
    3 (- (nth args 0) (nth args 1) (nth args 2))
    (apply - args)))

(defn- p15-s23-stage2-runtime-multiply
  [args]
  (case (count args)
    0 1
    1 (nth args 0)
    2 (* (nth args 0) (nth args 1))
    3 (* (nth args 0) (nth args 1) (nth args 2))
    (apply * args)))

(defn- p15-s23-stage2-runtime-divide
  [args]
  (case (count args)
    1 (/ (nth args 0))
    2 (/ (nth args 0) (nth args 1))
    3 (/ (nth args 0) (nth args 1) (nth args 2))
    (apply / args)))

(defn- p15-s23-stage2-runtime-equal
  [args]
  (case (count args)
    1 true
    2 (= (nth args 0) (nth args 1))
    3 (= (nth args 0) (nth args 1) (nth args 2))
    (apply = args)))

(defn- p15-s23-stage2-runtime-less-than
  [args]
  (case (count args)
    2 (< (nth args 0) (nth args 1))
    3 (< (nth args 0) (nth args 1) (nth args 2))
    (apply < args)))

(defn- p15-s23-stage2-runtime-greater-than
  [args]
  (case (count args)
    2 (> (nth args 0) (nth args 1))
    3 (> (nth args 0) (nth args 1) (nth args 2))
    (apply > args)))

(defn- p15-s23-stage2-runtime-less-than-or-equal
  [args]
  (case (count args)
    2 (<= (nth args 0) (nth args 1))
    3 (<= (nth args 0) (nth args 1) (nth args 2))
    (apply <= args)))

(defn- p15-s23-stage2-runtime-greater-than-or-equal
  [args]
  (case (count args)
    2 (>= (nth args 0) (nth args 1))
    3 (>= (nth args 0) (nth args 1) (nth args 2))
    (apply >= args)))

(defn- p15-s23-stage2-runtime-str
  [args]
  (case (count args)
    0 ""
    1 (str (nth args 0))
    2 (str (nth args 0) (nth args 1))
    3 (str (nth args 0) (nth args 1) (nth args 2))
    (apply str args)))

(defn- p15-s23-stage2-runtime-fail-builtin-error!
  [plan callee ex]
  (fail! "L2-BUILTIN-ERROR"
         "stage2 runtime builtin call failed"
         {:source-span {:source (get-in plan [:source :path])}
          :function callee
          :cause-message (.getMessage ex)
          :remediation "Keep builtin inputs inside the checked stage2 hosted-core runtime subset."}))

(defn- p15-s23-stage2-runtime-invoke-get
  ([plan collection key]
   (try
     (get collection key)
     (catch clojure.lang.ExceptionInfo ex
       (throw ex))
     (catch Exception ex
       (p15-s23-stage2-runtime-fail-builtin-error! plan 'get ex))))
  ([plan collection key not-found]
   (try
     (get collection key not-found)
     (catch clojure.lang.ExceptionInfo ex
       (throw ex))
     (catch Exception ex
       (p15-s23-stage2-runtime-fail-builtin-error! plan 'get ex)))))