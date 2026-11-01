(ns gravity.runtime
  "Temporary hosted Stage2 invocation, builtin/value operations and execution records; operations are trusted seed dependencies.")

(def ^:private namespace-contract
  '{:namespace gravity.runtime
    :contract-boundary :hosted-seed-stage2-invocation-builtin-and-value-operations
    :governing-contracts [D1 D2 D3 D8 D9 L2 C1 C15 SAFE9 R1 R4 R11 R12 BOOT8 TEST2]
    :public-api
    {hosted-add
     {:arglists ([args]) :admission :none :hosted-seed-only? true
      :arity-validation-owner :existing-builtin-dispatcher
      :error-translation-owner :existing-builtin-dispatcher}
     hosted-subtract
     {:arglists ([args]) :admission :none :hosted-seed-only? true
      :arity-validation-owner :existing-builtin-dispatcher
      :error-translation-owner :existing-builtin-dispatcher}
     hosted-multiply
     {:arglists ([args]) :admission :none :hosted-seed-only? true
      :arity-validation-owner :existing-builtin-dispatcher
      :error-translation-owner :existing-builtin-dispatcher}
     hosted-divide
     {:arglists ([args]) :admission :none :hosted-seed-only? true
      :arity-validation-owner :existing-builtin-dispatcher
      :error-translation-owner :existing-builtin-dispatcher}
     hosted-equal
     {:arglists ([args]) :admission :none :hosted-seed-only? true
      :arity-validation-owner :existing-builtin-dispatcher
      :error-translation-owner :existing-builtin-dispatcher}
     hosted-less-than
     {:arglists ([args]) :admission :none :hosted-seed-only? true
      :arity-validation-owner :existing-builtin-dispatcher
      :error-translation-owner :existing-builtin-dispatcher}
     hosted-greater-than
     {:arglists ([args]) :admission :none :hosted-seed-only? true
      :arity-validation-owner :existing-builtin-dispatcher
      :error-translation-owner :existing-builtin-dispatcher}
     hosted-less-than-or-equal
     {:arglists ([args]) :admission :none :hosted-seed-only? true
      :arity-validation-owner :existing-builtin-dispatcher
      :error-translation-owner :existing-builtin-dispatcher}
     hosted-greater-than-or-equal
     {:arglists ([args]) :admission :none :hosted-seed-only? true
      :arity-validation-owner :existing-builtin-dispatcher
      :error-translation-owner :existing-builtin-dispatcher}
     hosted-str
     {:arglists ([args]) :admission :none :hosted-seed-only? true}
     fail-builtin-error!
     {:arglists ([fail-var plan callee ex]) :admission :none :hosted-seed-only? true
      :operation-vars [fail-var] :transport-owner :supplied-failure-operation}
     hosted-get
     {:arglists ([fail-builtin-error-var plan collection key]
                [fail-builtin-error-var plan collection key not-found])
      :admission :none :hosted-seed-only? true :operation-vars [fail-builtin-error-var]
      :error-selection :late-after-lookup-failure :arity-validation-owner :existing-callers}
     hosted-builtin
     {:arglists ([plan callee args operations]) :admission :none :hosted-seed-only? true
      :operation-keys [:add :subtract :multiply :divide :equal :less-than :greater-than :less-than-or-equal :greater-than-or-equal :str :assert-min-arity! :assert-exact-arity! :assert-even-arity! :assert-between-arity! :fail-call-arity! :readable-pr-str :normalized-rest]
      :reserved-operation-owner :bootstrap :exception-translation-owner :bootstrap}
     execute-function-scoped
     {:arglists ([runtime plan callee args public-invocation-scope operations]) :admission :none
      :operation-keys [:native-scope! :module-entry-scope! :registered-scope? :assert-function-entry!
                       :assert-scoped-function-entry! :module-scope? :fail! :fail-call-arity!
                       :bind-values :execute-instructions-scoped :recur-signal? :recur-values :recur-fail!]}
     execute-function
     {:arglists ([runtime plan callee args operations]) :admission :none
      :operation-keys [:module-execute-function :execute-function-scoped]}
     artifact-invoke
     {:arglists ([runtime function args operations]) :admission :none
      :operation-keys [:executor-fail! :execute-function]}
     execute-plan
     {:arglists ([runtime plan operations]) :admission :none
      :operation-keys [:fail! :execute-instructions]
      :entry-protocol :legacy-main-arity-and-instruction-runner
      :adds-function-entry-scope-guard? false}}
    :artifact-inputs [:runtime-context :compiled-plan-data :function-and-arguments :native-invocation-scope
                      :trusted-native-scope-module-guard-binding-instruction-and-diagnostic-operations :already-evaluated-seed-numeric-arguments :already-evaluated-seed-comparison-arguments :trusted-host-value-arguments
                      :lookup-collection-key-and-default :builtin-plan-and-host-exception :trusted-failure-operation-vars
                      :ordinary-builtin-callee-and-evaluated-arguments :trusted-late-builtin-operation-vars]
    :artifact-outputs [:function-result :runtime-artifact-function-result :stage2-runtime-execution-record :hosted-seed-numeric-result :hosted-seed-comparison-result :hosted-string :lookup-value :legacy-host-error-facts :unchanged-host-failure :ordinary-hosted-builtin-result]
    :diagnostic-ownership {:ordinary-builtin-dispatch-and-arity-placement :stage
                           :stage [L2-UNKNOWN-CORE-FORM L3-UNKNOWN-ALIAS L2-MAIN-ARITY L2-BUILTIN-ERROR]
                           :existing-operations [L2-FUNCTION-ARITY :recur-arity :executor-lookup-and-arity
                                                 :native-scope :module-scope :function-entry :instruction-semantics
                                                 :builtin-arity-facts :builtin-host-error-selection-and-transport]}
    :dependency-direction {:requires [clojure.core] :consumed-by [gravity.bootstrap]
                           :forbids [gravity.bootstrap gravity.cli gravity.release]}
    :scope-and-authority-checks :existing-seed-operations
    :trusted-operation-map? true :authenticates-plans? false :grants-authority? false
    :canonical-runtime-manifest? false :stdout :existing-clojure-writer-capture
    :unsupported [:untrusted-callback-ingress :source-or-core-admission :public-plan-authentication
                  :runtime-family-selection :full-r1-r4-r11-r12-conformance :new-provider-authority
                  :native-target-or-backend-execution :general-gravity-numeric-modes :canonical-gravity-equality-and-ordering :canonical-formatting-and-collection-semantics :full-safe9-conformance
                  :untrusted-numeric-carriers :numeric-proof-or-check-records :redaction-and-publication :performance
                  :migration :release :self-hosting :seed-retirement]})

(defn execute-function-scoped
  [runtime plan callee args public-invocation-scope operations]
  (let [{:keys [native-scope! module-entry-scope! registered-scope? assert-function-entry! assert-scoped-function-entry! module-scope? fail! fail-call-arity! bind-values execute-instructions-scoped recur-signal? recur-values recur-fail!]} operations]
  (native-scope! public-invocation-scope)
  (let [public-invocation-scope
        (module-entry-scope! plan callee args public-invocation-scope)]
  (if (registered-scope? public-invocation-scope)
    (assert-function-entry! runtime plan callee args public-invocation-scope)
    (assert-scoped-function-entry!
     runtime plan callee args (if (module-scope? public-invocation-scope) nil public-invocation-scope)))
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
      (fail-call-arity!
       "L2-FUNCTION-ARITY" plan callee args (count params)))
    (loop [env (bind-values {} params args)]
      (let [result
            (execute-instructions-scoped
             runtime plan env instructions public-invocation-scope)]
        (if (recur-signal? result)
          (let [values (recur-values result)]
            (when-not (= (count params) (count values))
              (recur-fail!
               plan (count params) (count values)
               :function-arity-mismatch))
            (recur
             (bind-values {} params values)))
          result))))))

)

(defn execute-function
  [runtime plan callee args operations]
  (let [{:keys [module-execute-function execute-function-scoped]} operations]
  (if (= :gravity/stage2-compiler-artifact-module-bundle (:kind plan))
    (module-execute-function runtime plan callee args)
    (execute-function-scoped runtime plan callee args nil)))

)

(defn artifact-invoke
  "Generic host runner for a Gravity-authored runtime function plan.

  The runner performs only plan lookup, arity validation, and invocation.  It
  deliberately records the remaining Clojure interpreter boundary in the
  caller's manifest; runtime semantics (the formatting function itself) come
  from the compiled Gravity source artifact."
  [runtime function args operations]
  (let [{:keys [executor-fail! execute-function]} operations
        artifact-plan (:runtime-artifact-plan runtime)
        definition (get-in artifact-plan [:functions function])]
    (when-not (and (map? artifact-plan) (map? definition))
      (executor-fail!
       "P15S23X002"
       (:runtime-artifact-source-path runtime)
       artifact-plan
       {:missing-fact :runtime-artifact-function
        :function function}))
    (when-not (= (:arity definition) (count args))
      (executor-fail!
       "P15S23X002"
       (:runtime-artifact-source-path runtime)
       definition
       {:missing-fact :runtime-artifact-function-arity
        :function function
        :expected-arity (:arity definition)
        :actual-arity (count args)}))
    (execute-function
     ;; Disable nested artifact formatting while running the pure runtime
     ;; function set.  Its body may use normal stage2 builtins such as `str`.
     (dissoc runtime :runtime-artifact-plan)
     artifact-plan
     function
     args)))

(defn execute-plan
  [runtime plan operations]
  (let [{:keys [fail! execute-instructions]} operations
        main-function (get-in plan [:functions (:entrypoint plan)])
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
                  (execute-instructions
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

(defn hosted-add
  [args]
  (case (count args)
    0 0
    1 (nth args 0)
    2 (+ (nth args 0) (nth args 1))
    3 (+ (nth args 0) (nth args 1) (nth args 2))
    (apply + args)))

(defn hosted-subtract
  [args]
  (case (count args)
    1 (- (nth args 0))
    2 (- (nth args 0) (nth args 1))
    3 (- (nth args 0) (nth args 1) (nth args 2))
    (apply - args)))

(defn hosted-multiply
  [args]
  (case (count args)
    0 1
    1 (nth args 0)
    2 (* (nth args 0) (nth args 1))
    3 (* (nth args 0) (nth args 1) (nth args 2))
    (apply * args)))

(defn hosted-divide
  [args]
  (case (count args)
    1 (/ (nth args 0))
    2 (/ (nth args 0) (nth args 1))
    3 (/ (nth args 0) (nth args 1) (nth args 2))
    (apply / args)))

(defn hosted-equal
  [args]
  (case (count args)
    1 true
    2 (= (nth args 0) (nth args 1))
    3 (= (nth args 0) (nth args 1) (nth args 2))
    (apply = args)))

(defn hosted-less-than
  [args]
  (case (count args)
    2 (< (nth args 0) (nth args 1))
    3 (< (nth args 0) (nth args 1) (nth args 2))
    (apply < args)))

(defn hosted-greater-than
  [args]
  (case (count args)
    2 (> (nth args 0) (nth args 1))
    3 (> (nth args 0) (nth args 1) (nth args 2))
    (apply > args)))

(defn hosted-less-than-or-equal
  [args]
  (case (count args)
    2 (<= (nth args 0) (nth args 1))
    3 (<= (nth args 0) (nth args 1) (nth args 2))
    (apply <= args)))

(defn hosted-greater-than-or-equal
  [args]
  (case (count args)
    2 (>= (nth args 0) (nth args 1))
    3 (>= (nth args 0) (nth args 1) (nth args 2))
    (apply >= args)))

(defn hosted-str
  [args]
  (case (count args)
    0 ""
    1 (str (nth args 0))
    2 (str (nth args 0) (nth args 1))
    3 (str (nth args 0) (nth args 1) (nth args 2))
    (apply str args)))

(defn fail-builtin-error!
  [fail-var plan callee ex]
  ((deref fail-var) "L2-BUILTIN-ERROR"
         "stage2 runtime builtin call failed"
         {:source-span {:source (get-in plan [:source :path])}
          :function callee
          :cause-message (.getMessage ex)
          :remediation "Keep builtin inputs inside the checked stage2 hosted-core runtime subset."}))

(defn hosted-get
  ([fail-builtin-error-var plan collection key]
   (try
     (get collection key)
     (catch clojure.lang.ExceptionInfo ex
       (throw ex))
     (catch Exception ex
       ((deref fail-builtin-error-var) plan 'get ex))))
  ([fail-builtin-error-var plan collection key not-found]
   (try
     (get collection key not-found)
     (catch clojure.lang.ExceptionInfo ex
       (throw ex))
     (catch Exception ex
       ((deref fail-builtin-error-var) plan 'get ex)))))

(defn hosted-builtin
  [plan callee args operations]
  (case callee
      + ((deref (get operations :add)) args)
      - (do ((deref (get operations :assert-min-arity!))
             plan callee args 1)
            ((deref (get operations :subtract)) args))
      * ((deref (get operations :multiply)) args)
      / (do ((deref (get operations :assert-min-arity!))
             plan callee args 1)
            ((deref (get operations :divide)) args))
      = (do ((deref (get operations :assert-min-arity!))
             plan callee args 1)
            ((deref (get operations :equal)) args))
      < (do ((deref (get operations :assert-min-arity!))
             plan callee args 2)
            ((deref (get operations :less-than)) args))
      > (do ((deref (get operations :assert-min-arity!))
             plan callee args 2)
            ((deref (get operations :greater-than)) args))
      <= (do ((deref (get operations :assert-min-arity!))
              plan callee args 2)
             ((deref (get operations :less-than-or-equal)) args))
      >= (do ((deref (get operations :assert-min-arity!))
              plan callee args 2)
             ((deref (get operations :greater-than-or-equal)) args))
      str ((deref (get operations :str)) args)
      pr-str ((deref (get operations :readable-pr-str))
              (get-in plan [:source :path]) args)
      hash-map (do ((deref (get operations :assert-even-arity!))
                    plan callee args)
                   (apply hash-map args))
      vector (vec args)
      list (apply list args)
      conj (do ((deref (get operations :assert-min-arity!))
                plan callee args 1)
               (apply conj args))
      assoc (do
              ((deref (get operations :assert-min-arity!))
               plan callee args 3)
              (when (even? (count args))
                ((deref (get operations :fail-call-arity!))
                 "L2-BUILTIN-ARITY" plan callee args
                 "a collection followed by key/value pairs"))
              (apply assoc args))
      get (do ((deref (get operations :assert-between-arity!))
               plan callee args 2 3)
              ;; `get` dominates checked-core execution. Avoid `apply` here:
              ;; it turns the already-materialized argument vector into a seq
              ;; and performs generic bounded-arity dispatch on every lookup.
              (case (count args)
                2 (get (nth args 0) (nth args 1))
                3 (get (nth args 0) (nth args 1) (nth args 2))))
      first (do ((deref (get operations :assert-exact-arity!))
                 plan callee args 1)
                (first (nth args 0)))
      second (do ((deref (get operations :assert-exact-arity!))
                  plan callee args 1)
                 (second (nth args 0)))
      rest (do ((deref (get operations :assert-exact-arity!))
                plan callee args 1)
               ((deref (get operations :normalized-rest)) (nth args 0)))
      count (do ((deref (get operations :assert-exact-arity!))
                 plan callee args 1)
                (count (nth args 0)))
      symbol? (do
                ((deref (get operations :assert-exact-arity!))
                 plan callee args 1)
                (symbol? (nth args 0)))
      keyword? (do
                 ((deref (get operations :assert-exact-arity!))
                  plan callee args 1)
                 (keyword? (nth args 0)))
      char? (do
              ((deref (get operations :assert-exact-arity!))
               plan callee args 1)
              (char? (nth args 0)))
      number? (do
                ((deref (get operations :assert-exact-arity!))
                 plan callee args 1)
                (number? (nth args 0)))
      seq? (do
             ((deref (get operations :assert-exact-arity!))
              plan callee args 1)
             (seq? (nth args 0)))
      list? (do
              ((deref (get operations :assert-exact-arity!))
               plan callee args 1)
              (list? (nth args 0)))
      vector? (do
                ((deref (get operations :assert-exact-arity!))
                 plan callee args 1)
                (vector? (nth args 0)))
      map? (do
             ((deref (get operations :assert-exact-arity!))
              plan callee args 1)
             (map? (nth args 0)))
      set? (do
             ((deref (get operations :assert-exact-arity!))
              plan callee args 1)
             (set? (nth args 0)))
      string? (do
                ((deref (get operations :assert-exact-arity!))
                 plan callee args 1)
                (string? (nth args 0)))
      contains? (do
                  ((deref (get operations :assert-exact-arity!))
                   plan callee args 2)
                  (contains? (nth args 0) (nth args 1)))
      even? (do
              ((deref (get operations :assert-exact-arity!))
               plan callee args 1)
              (even? (nth args 0)))
      integer? (do
                 ((deref (get operations :assert-exact-arity!))
                  plan callee args 1)
                 (integer? (nth args 0)))
      boolean? (do
                 ((deref (get operations :assert-exact-arity!))
                  plan callee args 1)
                 (boolean? (nth args 0)))
      keys (do
             ((deref (get operations :assert-exact-arity!))
              plan callee args 1)
             (apply list (keys (nth args 0))))
      set (do
            ((deref (get operations :assert-exact-arity!))
             plan callee args 1)
            (set (nth args 0)))
      sort-by-pr-str (do
                       ((deref (get operations :assert-exact-arity!))
                        plan callee args 1)
                       (apply list (sort-by pr-str (nth args 0))))
      vec (do
            ((deref (get operations :assert-exact-arity!))
             plan callee args 1)
            (into [] (nth args 0)))
      quot (do
             ((deref (get operations :assert-exact-arity!))
              plan callee args 2)
             (quot (nth args 0) (nth args 1)))
      subvec (do
               ((deref (get operations :assert-exact-arity!))
                plan callee args 3)
               (into []
                     (subvec (nth args 0) (nth args 1) (nth args 2))))))

(doseq [operation [#'hosted-builtin #'hosted-add #'hosted-subtract #'hosted-multiply #'hosted-divide #'hosted-equal #'hosted-less-than #'hosted-greater-than #'hosted-less-than-or-equal #'hosted-greater-than-or-equal #'hosted-str #'fail-builtin-error! #'hosted-get #'execute-function-scoped #'execute-function #'artifact-invoke #'execute-plan]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
