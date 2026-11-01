(ns gravity.runtime
  "Temporary hosted Stage2 invocation, arithmetic, comparison and execution records; operations are trusted seed dependencies.")

(def ^:private namespace-contract
  '{:namespace gravity.runtime
    :contract-boundary :hosted-seed-stage2-invocation-arithmetic-and-comparison
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
                      :trusted-native-scope-module-guard-binding-instruction-and-diagnostic-operations :already-evaluated-seed-numeric-arguments :already-evaluated-seed-comparison-arguments]
    :artifact-outputs [:function-result :runtime-artifact-function-result :stage2-runtime-execution-record :hosted-seed-numeric-result :hosted-seed-comparison-result :unchanged-host-failure]
    :diagnostic-ownership {:stage [L2-UNKNOWN-CORE-FORM L3-UNKNOWN-ALIAS L2-MAIN-ARITY]
                           :existing-operations [L2-FUNCTION-ARITY :recur-arity :executor-lookup-and-arity
                                                 :native-scope :module-scope :function-entry :instruction-semantics
                                                 :builtin-arity :builtin-host-error-translation]}
    :dependency-direction {:requires [clojure.core] :consumed-by [gravity.bootstrap]
                           :forbids [gravity.bootstrap gravity.cli gravity.release]}
    :scope-and-authority-checks :existing-seed-operations
    :trusted-operation-map? true :authenticates-plans? false :grants-authority? false
    :canonical-runtime-manifest? false :stdout :existing-clojure-writer-capture
    :unsupported [:untrusted-callback-ingress :source-or-core-admission :public-plan-authentication
                  :runtime-family-selection :full-r1-r4-r11-r12-conformance :new-provider-authority
                  :native-target-or-backend-execution :general-gravity-numeric-modes :canonical-gravity-equality-and-ordering :full-safe9-conformance
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

(doseq [operation [#'hosted-add #'hosted-subtract #'hosted-multiply #'hosted-divide #'hosted-equal #'hosted-less-than #'hosted-greater-than #'hosted-less-than-or-equal #'hosted-greater-than-or-equal #'execute-function-scoped #'execute-function #'artifact-invoke #'execute-plan]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
