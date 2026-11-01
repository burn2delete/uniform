(ns gravity.runtime
  "Temporary hosted Stage2 invocation, instruction/evaluation, builtin policy and rule data; trusted seed dependencies.")

(def ^:private namespace-contract
  '{:namespace gravity.runtime
    :contract-boundary :hosted-seed-stage2-invocation-instruction-evaluation-and-value-operations
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
     executor-rule-record
     {:arglists ([runtime operations]) :admission :none :hosted-seed-only? true
      :operation-keys [:declared-builtin-exact-arities :declared-builtin-contracts :difference :required-instructions :builtin-functions :sort-values] :completion-label-admits? false}
     fail-call-arity!
     {:arglists ([fail-var id plan callee args expected]) :admission :none :hosted-seed-only? true
      :operation-vars [fail-var] :transport-owner :supplied-failure-operation}
     assert-min-arity!
     {:arglists ([fail-call-arity-var plan callee args n]) :admission :none :hosted-seed-only? true
      :operation-vars [fail-call-arity-var]}
     assert-exact-arity!
     {:arglists ([fail-call-arity-var plan callee args n]) :admission :none :hosted-seed-only? true
      :operation-vars [fail-call-arity-var]}
     assert-between-arity!
     {:arglists ([fail-call-arity-var plan callee args min-n max-n]) :admission :none :hosted-seed-only? true
      :operation-vars [fail-call-arity-var]}
     assert-even-arity!
     {:arglists ([fail-call-arity-var plan callee args]) :admission :none :hosted-seed-only? true
      :operation-vars [fail-call-arity-var]}
     hosted-unary-builtin
     {:arglists ([runtime plan function value operations]) :admission :none :hosted-seed-only? true
      :operation-keys [:normalized-rest :invoke-builtin :fail-builtin-error!] :argument-evaluation-owner :existing-caller}
     map-entry
     {:arglists ([value requested-key]) :admission :none :hosted-seed-only? true}
     fail-recur!
     {:arglists ([fail-var plan target-arity actual-arity reason]) :admission :none :hosted-seed-only? true :operation-vars [fail-var]}
     nontail-value!
     {:arglists ([plan value reason operations]) :admission :none :hosted-seed-only? true :operation-keys [:recur-signal? :recur-fail! :recur-values]}
     execute-generic-builtin-call-scoped
     {:arglists ([runtime plan env instruction function public-invocation-scope operations]) :admission :none :hosted-seed-only? true
      :operation-keys [:execute-values-scoped :artifact-invoke :artifact-function :artifact-concat-function :fail-call-arity! :invoke-compiler-internal-builtin :invoke-builtin]
      :data-operation-keys [:artifact-function :artifact-concat-function] :call-authority-owner :existing-seed-operations}
     execute-call-scoped
     {:arglists ([runtime plan env instruction public-invocation-scope operations]) :admission :none :hosted-seed-only? true
      :operation-keys [:authorize-external-link :execute-values-scoped :execute-external-machine :execute-function-scoped] :call-authority-owner :existing-seed-operations}
     execute-instruction-scoped
     {:arglists ([runtime plan env instruction public-invocation-scope operations]) :admission :none :hosted-seed-only? true
      :operation-keys [:fail! :execute-values-scoped :execute-map-entries-scoped :validate-module-effects! :artifact-invoke :artifact-println-function :artifact-println-two-function :join :artifact-function :execute-instructions-scoped :nontail-value! :execute-instruction-scoped :recur-signal? :recur-values :recur-fail! :bind-values :recur-signal :source-throw! :execute-value-scoped :invoke-get :fail-builtin-error! :invoke-compiler-internal-builtin :invoke-unary-or-generic-builtin :execute-generic-builtin-call-scoped :execute-call-scoped :module-execute-call]
      :data-operation-keys [:artifact-println-function :artifact-println-two-function :artifact-function]
      :instruction-admission-owner :existing-callers :call-authority-owner :existing-seed-operations}
     execute-value-scoped
     {:arglists ([runtime plan env instruction reason public-invocation-scope operations]) :admission :none :hosted-seed-only? true
      :operation-keys [:nontail-value! :execute-instruction-scoped]}
     bind-values
     {:arglists ([base names values]) :admission :none :hosted-seed-only? true}
     execute-values-scoped
     {:arglists ([runtime plan env instructions reason public-invocation-scope operations]) :admission :none :hosted-seed-only? true
      :operation-keys [:execute-value-scoped]}
     execute-map-entries-scoped
     {:arglists ([runtime plan env entries public-invocation-scope operations]) :admission :none :hosted-seed-only? true
      :operation-keys [:nontail-value! :execute-instruction-scoped]}
     execute-instructions-scoped
     {:arglists ([runtime plan env instructions public-invocation-scope operations]) :admission :none :hosted-seed-only? true
      :operation-keys [:execute-instruction-scoped :recur-signal? :recur-fail! :recur-values]}
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
                      :ordinary-builtin-callee-and-evaluated-arguments :trusted-late-builtin-operation-vars
                      :trusted-environment-bindings-instruction-and-map-entry-carriers :opaque-invocation-scope
                      :trusted-late-instruction-and-recurrence-operation-vars
                      :trusted-instruction-function-and-data-operation-vars
                      :trusted-map-entry-and-recurrence-data :trusted-call-sequencing-operations :trusted-runtime-rule-data :call-signature-and-arity-data :trusted-late-policy-operation-vars]
    :artifact-outputs [:function-result :runtime-artifact-function-result :stage2-runtime-execution-record :hosted-seed-numeric-result :hosted-seed-comparison-result :hosted-string :lookup-value :legacy-host-error-facts :unchanged-host-failure :ordinary-hosted-builtin-result :bound-environment :evaluated-argument-vector :evaluated-map :instruction-sequence-result :hosted-writer-effect :source-throw-control :legacy-executor-rule-record :nil-or-arity-refusal :native-map-entry-or-nil :recurrence-refusal-or-original-value :sequenced-call-result]
    :diagnostic-ownership {:ordinary-builtin-dispatch-and-arity-placement :stage
                           :evaluation-carrier-traversal-and-recur-check-placement :stage
                           :instruction-selection-core-control-and-fast-paths :stage
                           :map-entry-recurrence-facts-and-call-sequencing :stage
                           :arity-guards-call-facts-unary-selection-and-rule-data :stage
                           :stage [L2-RECUR-TARGET L2-FUNCTION-ARITY L2-BUILTIN-ARITY L2-UNKNOWN-SYMBOL L2-THROW-ILLEGAL L2-UNKNOWN-CORE-FORM L3-UNKNOWN-ALIAS L2-MAIN-ARITY L2-BUILTIN-ERROR]
                           :existing-operations [:recurrence-representation :executor-lookup-and-arity
                                                 :native-scope :module-scope :function-entry :instruction-primitives-and-call-authority
                                                 :generic-builtin-host-error-boundary-and-failure-transport]}
    :dependency-direction {:requires [clojure.core] :consumed-by [gravity.bootstrap]
                           :forbids [gravity.bootstrap gravity.cli gravity.release]}
    :scope-and-authority-checks :existing-seed-operations
    :trusted-operation-map? true :authenticates-plans? false :grants-authority? false
    :canonical-runtime-manifest? false :stdout :existing-clojure-writer-capture
    :unsupported [:untrusted-callback-ingress :source-or-core-admission :public-plan-authentication
                  :runtime-family-selection :full-r1-r4-r11-r12-conformance :new-provider-authority
                  :native-target-or-backend-execution :general-gravity-numeric-modes :canonical-gravity-equality-and-ordering :canonical-formatting-and-collection-semantics :full-safe9-conformance
                  :untrusted-numeric-carriers :numeric-proof-or-check-records :redaction-and-publication :performance
                  :canonical-instruction-admission :canonical-gravity-source-map-evaluation-order
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

(defn execute-value-scoped
  [runtime plan env instruction reason public-invocation-scope operations]
  ((deref (get operations :nontail-value!))
   plan
   ((deref (get operations :execute-instruction-scoped))
    runtime plan env instruction public-invocation-scope)
   reason))

(defn bind-values
  [base names values]
  ;; Function and loop binders are normally tiny. Avoid `zipmap`'s transient
  ;; map plus `merge`'s seq traversal for the common fixed-arity cases.
  (case (count names)
    0 base
    1 (assoc base (nth names 0) (nth values 0))
    2 (assoc (assoc base
                    (nth names 0) (nth values 0))
             (nth names 1) (nth values 1))
    3 (assoc (assoc (assoc base
                          (nth names 0) (nth values 0))
                   (nth names 1) (nth values 1))
             (nth names 2) (nth values 2))
    (merge base (zipmap names values))))

(defn execute-values-scoped
  [runtime plan env instructions reason public-invocation-scope operations]
  ;; Builtin and function calls overwhelmingly carry zero to three arguments.
  ;; `mapv` allocates a transient vector root/tail even for those tiny
  ;; collections, which accounted for most allocation in a live B47 profile.
  ;; Preserve the generic path for larger carriers.
  (case (count instructions)
    0 []
    1 [((deref (get operations :execute-value-scoped))
        runtime plan env (nth instructions 0) reason
        public-invocation-scope)]
    2 [((deref (get operations :execute-value-scoped))
        runtime plan env (nth instructions 0) reason
        public-invocation-scope)
       ((deref (get operations :execute-value-scoped))
        runtime plan env (nth instructions 1) reason
        public-invocation-scope)]
    3 [((deref (get operations :execute-value-scoped))
        runtime plan env (nth instructions 0) reason
        public-invocation-scope)
       ((deref (get operations :execute-value-scoped))
        runtime plan env (nth instructions 1) reason
        public-invocation-scope)
       ((deref (get operations :execute-value-scoped))
        runtime plan env (nth instructions 2) reason
        public-invocation-scope)]
    (mapv (fn [instruction]
            ((deref (get operations :execute-value-scoped))
             runtime plan env instruction reason public-invocation-scope))
          instructions)))

(defn execute-map-entries-scoped
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
  [runtime plan env entries public-invocation-scope operations]
  (if (vector? entries)
    (let [entry-count (count entries)]
      (loop [index 0
             result (transient {})]
        (if (< index entry-count)
          (let [{:keys [key value]} (nth entries index)
                evaluated-key
                ((deref (get operations :nontail-value!))
                 plan
                 ((deref (get operations :execute-instruction-scoped))
                  runtime plan env key public-invocation-scope)
                 :recur-inside-map-key)
                evaluated-value
                ((deref (get operations :nontail-value!))
                 plan
                 ((deref (get operations :execute-instruction-scoped))
                  runtime plan env value public-invocation-scope)
                 :recur-inside-map-value)]
            (recur (inc index)
                   (assoc! result evaluated-key evaluated-value)))
          (persistent! result))))
    (loop [remaining (seq entries)
           result (transient {})]
      (if (seq remaining)
        (let [{:keys [key value]} (first remaining)
              evaluated-key
              ((deref (get operations :nontail-value!))
               plan
               ((deref (get operations :execute-instruction-scoped))
                runtime plan env key public-invocation-scope)
               :recur-inside-map-key)
              evaluated-value
              ((deref (get operations :nontail-value!))
               plan
               ((deref (get operations :execute-instruction-scoped))
                runtime plan env value public-invocation-scope)
               :recur-inside-map-value)]
          (recur (next remaining)
                 (assoc! result evaluated-key evaluated-value)))
        (persistent! result)))))

(defn execute-instructions-scoped
  [runtime plan env instructions public-invocation-scope operations]
  (if (vector? instructions)
    ;; Compiler plans use vectors. Indexing avoids constructing a chunked seq
    ;; and calling `next` for every interpreted function body.
    (let [instruction-count (count instructions)]
      (loop [index 0
             result nil]
        (if (< index instruction-count)
          (let [value ((deref (get operations :execute-instruction-scoped))
                       runtime plan env (nth instructions index)
                       public-invocation-scope)
                more? (< (inc index) instruction-count)]
            (if (and ((deref (get operations :recur-signal?)) value) more?)
              ((deref (get operations :recur-fail!))
               plan nil (count ((deref (get operations :recur-values)) value))
               :non-tail-sequential-position)
              (recur (inc index) value)))
          result)))
    ;; Retain the established behavior for direct callers that supply lists.
    (loop [remaining (seq instructions)
           result nil]
      (if-let [instruction (first remaining)]
        (let [value ((deref (get operations :execute-instruction-scoped))
                     runtime plan env instruction public-invocation-scope)
              more (next remaining)]
          (if (and ((deref (get operations :recur-signal?)) value) more)
            ((deref (get operations :recur-fail!))
             plan nil (count ((deref (get operations :recur-values)) value))
             :non-tail-sequential-position)
            (recur more value)))
        result))))

(defn execute-instruction-scoped
  [runtime plan env instruction public-invocation-scope operations]
  (case (:op instruction)
    :literal (:value instruction)
    :quote (:value instruction)
    :local
    (let [name (:name instruction)]
      (if (contains? env name)
        (get env name)
        ((deref (get operations :fail!)) "L2-UNKNOWN-SYMBOL"
               "stage2 runtime cannot resolve local"
               {:source-span {:source (get-in plan [:source :path])}
                :symbol name
                :remediation "Regenerate the stage2 instruction plan from a valid source module."})))
    :vector-literal
    ((deref (get operations :execute-values-scoped))
     runtime plan env (:items instruction) :recur-inside-vector public-invocation-scope)
    :set-literal
    (set ((deref (get operations :execute-values-scoped))
          runtime plan env (:items instruction) :recur-inside-set
          public-invocation-scope))
    :map-literal
    ((deref (get operations :execute-map-entries-scoped))
     runtime plan env (:entries instruction) public-invocation-scope)
    :println
    (let [module (:module plan)
          args ((deref (get operations :execute-values-scoped))
                runtime plan env (:args instruction) :recur-inside-println-argument public-invocation-scope)]
      ((deref (get operations :validate-module-effects!)) module)
      (if (and (= 1 (count args))
               (:runtime-artifact-plan runtime))
        ;; The single-argument effect is owned by the Gravity-authored
        ;; runtime function.  Its :println instruction still crosses the
        ;; explicitly recorded generic host bridge, but formatting and the
        ;; newline effect no longer live in this executor branch.
        ((deref (get operations :artifact-invoke))
         runtime (deref (get operations :artifact-println-function)) args)
        (if (and (= 2 (count args))
                 (:runtime-artifact-plan runtime))
          ;; Exactly two arguments are owned by the dedicated
          ;; Gravity-authored runtime function.  Arity >2 remains an explicit
          ;; compatibility boundary below until a variadic effect contract is
          ;; authored and validated.
          ((deref (get operations :artifact-invoke))
           runtime (deref (get operations :artifact-println-two-function)) args)
          (println
           ((deref (get operations :join))
            " "
            (map (fn [value]
                   (if (:runtime-artifact-plan runtime)
                     ((deref (get operations :artifact-invoke))
                      runtime (deref (get operations :artifact-function)) [value])
                     (str value)))
                 args))))))
    :do
    ((deref (get operations :execute-instructions-scoped))
     runtime plan env (:body instruction) public-invocation-scope)
    :if
    (if ((deref (get operations :nontail-value!))
         plan
         ((deref (get operations :execute-instruction-scoped))
          runtime plan env (:test instruction) public-invocation-scope)
         :recur-inside-if-test)
      ((deref (get operations :execute-instruction-scoped))
       runtime plan env (:then instruction) public-invocation-scope)
      ((deref (get operations :execute-instruction-scoped))
       runtime plan env (:else instruction) public-invocation-scope))
    :let
    (loop [env env
           bindings (:bindings instruction)]
      (if-let [{:keys [name expr]} (first bindings)]
        (recur (assoc env name
                      ((deref (get operations :nontail-value!))
                       plan
                       ((deref (get operations :execute-instruction-scoped))
                        runtime plan env expr public-invocation-scope)
                       :recur-inside-let-binding))
               (rest bindings))
        ((deref (get operations :execute-instructions-scoped))
         runtime plan env (:body instruction) public-invocation-scope)))
    :loop
    (let [bindings (:bindings instruction)
          body (:body instruction)
          binding-names (mapv :name bindings)
          target-arity (count binding-names)
          initial-env
          (loop [scope env
                 remaining bindings]
            (if-let [{:keys [name expr]} (first remaining)]
              (let [value
                    ((deref (get operations :nontail-value!))
                     plan
                     ((deref (get operations :execute-instruction-scoped))
                      runtime plan scope expr public-invocation-scope)
                     :recur-inside-loop-initializer)]
                (recur (assoc scope name value) (rest remaining)))
              scope))]
      (loop [loop-env initial-env]
        (let [result
              ((deref (get operations :execute-instructions-scoped))
               runtime plan loop-env body public-invocation-scope)]
          (if ((deref (get operations :recur-signal?)) result)
            (let [values ((deref (get operations :recur-values)) result)]
              (when-not (= target-arity (count values))
                ((deref (get operations :recur-fail!))
                 plan target-arity (count values) :loop-arity-mismatch))
              (recur
               ((deref (get operations :bind-values))
                env binding-names values)))
            result))))
    :recur
    ((deref (get operations :recur-signal))
     ((deref (get operations :execute-values-scoped))
      runtime plan env (:args instruction) :recur-inside-recur-argument
      public-invocation-scope))
    :throw
    (do
      (when-not (and (= #{:op :value} (set (keys instruction))) (map? (:value instruction)))
        ((deref (get operations :fail!)) "L2-THROW-ILLEGAL" "seed throw instruction requires one operand instruction"
               {:source-span {:source (get-in plan [:source :path])} :reason :malformed-throw-instruction}))
      ((deref (get operations :source-throw!)) plan
        ((deref (get operations :execute-value-scoped))
         runtime plan env (:value instruction) :recur-inside-throw-operand public-invocation-scope)))
    :builtin-call
    (let [function (:function instruction)
          argument-instructions (:args instruction)
          argument-count (count argument-instructions)]
      (cond
        (and (= 2 argument-count)
             (instance? clojure.lang.Symbol function)
             (.equals ^clojure.lang.Symbol 'get function))
        (let [collection
              ((deref (get operations :execute-value-scoped))
               runtime plan env (nth argument-instructions 0) :recur-inside-builtin-argument public-invocation-scope)
              key
              ((deref (get operations :execute-value-scoped))
               runtime plan env (nth argument-instructions 1) :recur-inside-builtin-argument public-invocation-scope)]
          ((deref (get operations :invoke-get)) plan collection key))
        (and (= 3 argument-count)
             (instance? clojure.lang.Symbol function)
             (.equals ^clojure.lang.Symbol 'get function))
        (let [collection
              ((deref (get operations :execute-value-scoped))
               runtime plan env (nth argument-instructions 0) :recur-inside-builtin-argument public-invocation-scope)
              key
              ((deref (get operations :execute-value-scoped))
               runtime plan env (nth argument-instructions 1) :recur-inside-builtin-argument public-invocation-scope)
              not-found
              ((deref (get operations :execute-value-scoped))
               runtime plan env (nth argument-instructions 2) :recur-inside-builtin-argument public-invocation-scope)]
          ((deref (get operations :invoke-get))
           plan collection key not-found))
        (and (= 1 argument-count)
             (instance? clojure.lang.Symbol function)
             (.equals ^clojure.lang.Symbol 'count function))
        (let [value
              ((deref (get operations :execute-value-scoped))
               runtime plan env (nth argument-instructions 0) :recur-inside-builtin-argument public-invocation-scope)]
          (try
            (count value)
            (catch clojure.lang.ExceptionInfo ex (throw ex))
            (catch Exception ex
              ((deref (get operations :fail-builtin-error!))
               plan function ex))))
        (and (= 1 argument-count)
             (instance? clojure.lang.Symbol function)
             (.equals ^clojure.lang.Symbol 'str function)
             (:runtime-artifact-plan runtime))
        (let [value
              ((deref (get operations :execute-value-scoped))
               runtime plan env (nth argument-instructions 0) :recur-inside-builtin-argument public-invocation-scope)]
          ;; Preserve the established artifact boundary: ordinary artifact
          ;; exceptions are not reclassified as host builtin failures.
          ((deref (get operations :artifact-invoke))
           runtime (deref (get operations :artifact-function)) [value]))
        (= 1 argument-count)
        (let [value
              ((deref (get operations :execute-value-scoped))
               runtime plan env (nth argument-instructions 0) :recur-inside-builtin-argument public-invocation-scope)]
          (if (contains? instruction :compiler-internal-caller)
            ((deref (get operations :invoke-compiler-internal-builtin))
             runtime plan instruction [value] public-invocation-scope)
            ((deref (get operations :invoke-unary-or-generic-builtin))
             runtime plan function value)))
        :else
        ;; Retain the original exact-two equality-or-generic control-flow
        ;; shape, then admit exact-three assoc before the generic fallback.
        (if (= 2 argument-count)
          (if (and (instance? clojure.lang.Symbol function)
                   (.equals ^clojure.lang.Symbol '= function))
            (let [left
                  ((deref (get operations :execute-value-scoped))
                   runtime plan env (nth argument-instructions 0) :recur-inside-builtin-argument public-invocation-scope)
                  right
                  ((deref (get operations :execute-value-scoped))
                   runtime plan env (nth argument-instructions 1) :recur-inside-builtin-argument public-invocation-scope)]
              ;; Preserve the generic path's boundary: argument evaluation is
              ;; outside the builtin try, while host equality failures are
              ;; mapped exactly like invoke-builtin.
              (try
                (= left right)
                (catch clojure.lang.ExceptionInfo ex (throw ex))
                (catch Exception ex
                  ((deref (get operations :fail-builtin-error!))
                   plan function ex))))
            ((deref (get operations :execute-generic-builtin-call-scoped))
             runtime plan env instruction function public-invocation-scope))
          (if (and (= 3 argument-count)
                   (instance? clojure.lang.Symbol function)
                   (.equals ^clojure.lang.Symbol 'assoc function))
            (let [collection
                  ((deref (get operations :execute-value-scoped))
                   runtime plan env (nth argument-instructions 0) :recur-inside-builtin-argument public-invocation-scope)
                  key
                  ((deref (get operations :execute-value-scoped))
                   runtime plan env (nth argument-instructions 1) :recur-inside-builtin-argument public-invocation-scope)
                  value
                  ((deref (get operations :execute-value-scoped))
                   runtime plan env (nth argument-instructions 2) :recur-inside-builtin-argument public-invocation-scope)]
              ;; Preserve the generic path's argument and exception boundaries
              ;; while avoiding its argument vector and apply seq.
              (try
                (assoc collection key value)
                (catch clojure.lang.ExceptionInfo ex (throw ex))
                (catch Exception ex
                  ((deref (get operations :fail-builtin-error!))
                   plan function ex))))
            ((deref (get operations :execute-generic-builtin-call-scoped))
             runtime plan env instruction function public-invocation-scope)))))
    :function-call
    ((deref (get operations :execute-call-scoped)) runtime plan env instruction public-invocation-scope)
    :compiler-module-call
    ((deref (get operations :module-execute-call)) runtime plan env instruction public-invocation-scope)
    ((deref (get operations :fail!)) "L2-UNKNOWN-CORE-FORM"
           "stage2 runtime plan contains an unknown instruction"
           {:source-span {:source (get-in plan [:source :path])}
            :operator (:op instruction)
            :remediation "Regenerate the stage2 instruction plan with a supported stage2 plan emitter."})))

(defn executor-rule-record
  [runtime operations]
  (let [instruction-rules (set (keys (:instruction-rules runtime)))
        builtin-functions (set (get-in runtime
                                       [:builtin-rules :functions]))
        builtin-exact-arities
        (get-in runtime [:builtin-rules :compiler-internal-builtin-arities])
        builtin-contracts
        (get-in runtime [:builtin-rules :compiler-internal-builtin-contracts])
        declared-arities-current?
        (= (deref (get operations :declared-builtin-exact-arities)) builtin-exact-arities)
        declared-contracts-current?
        (= (deref (get operations :declared-builtin-contracts)) builtin-contracts)
        missing-instructions
        ((deref (get operations :difference))
         (deref (get operations :required-instructions))
         instruction-rules)
        missing-builtins
        ((deref (get operations :difference)) (deref (get operations :builtin-functions)) builtin-functions)
        complete?
        (and (= :gravity-stage2-runtime-executor-rules-v1
                (:engine runtime))
             (= :stage2-instruction-plan (:input runtime))
             (= :stage2-runtime-execution-record (:output runtime))
             (empty? missing-instructions)
             (empty? missing-builtins)
             declared-arities-current?
             declared-contracts-current?)]
    {:artifact :gravity/p15-s23-stage2-runtime-executor-rule-record
     :engine (:engine runtime)
     :input (:input runtime)
     :output (:output runtime)
     :instruction-rules ((deref (get operations :sort-values)) instruction-rules)
     :builtin-functions ((deref (get operations :sort-values)) builtin-functions)
     :builtin-exact-arities builtin-exact-arities
     :declared-builtin-arities-current? declared-arities-current?
     :compiler-internal-builtin-contracts builtin-contracts
     :declared-builtin-contracts-current? declared-contracts-current?
     :missing-instructions ((deref (get operations :sort-values))
                            missing-instructions)
     :missing-builtin-functions ((deref (get operations :sort-values))
                                 missing-builtins)
     :rule-set-complete? complete?
     :status (if complete? :complete :failed)}))

(defn fail-call-arity!
  [fail-var id plan callee args expected]
  ((deref fail-var) id
         (case id
           "L2-FUNCTION-ARITY" "stage2 runtime function call has the wrong arity"
           "L2-BUILTIN-ARITY" "stage2 runtime builtin call has the wrong arity"
           "stage2 runtime call has the wrong arity")
         {:source-span {:source (get-in plan [:source :path])}
          :function callee
          :expected-arity expected
          :actual-arity (count args)
          :remediation "Call the function with the arity supported by the stage2 hosted-core runtime subset."}))

(defn assert-min-arity!
  [fail-call-arity-var plan callee args n]
  (when (< (count args) n)
    ((deref fail-call-arity-var)
     "L2-BUILTIN-ARITY" plan callee args (str "at least " n))))

(defn assert-exact-arity!
  [fail-call-arity-var plan callee args n]
  (when-not (= n (count args))
    ((deref fail-call-arity-var)
     "L2-BUILTIN-ARITY" plan callee args n)))

(defn assert-between-arity!
  [fail-call-arity-var plan callee args min-n max-n]
  (when-not (<= min-n (count args) max-n)
    ((deref fail-call-arity-var)
     "L2-BUILTIN-ARITY" plan callee args (str min-n " to " max-n))))

(defn assert-even-arity!
  [fail-call-arity-var plan callee args]
  (when (odd? (count args))
    ((deref fail-call-arity-var)
     "L2-BUILTIN-ARITY" plan callee args "an even number of arguments")))

(defn hosted-unary-builtin
  [runtime plan function value operations]
  ;; Argument evaluation deliberately happens at the call site, before this
  ;; exception boundary and before compiler-context validation. This retains
  ;; the generic path's evaluation order while avoiding its one-element vector.
  (try
    (case function
      first (first value)
      second (second value)
      rest ((deref (get operations :normalized-rest)) value)

      symbol? (symbol? value)
      keyword? (keyword? value)
      char? (char? value)
      number? (number? value)
      seq? (seq? value)
      list? (list? value)
      vector? (vector? value)
      map? (map? value)
      set? (set? value)
      string? (string? value)
      even? (even? value)
      integer? (integer? value)
      boolean? (boolean? value)

      keys (apply list (keys value))
      set (set value)
      sort-by-pr-str
      (apply list (sort-by pr-str value))
      vec (into [] value)

      ;; Variadic builtins, pr-str, and malformed callees retain generic
      ;; dispatch after the sole argument has been evaluated exactly once.
      ((deref (get operations :invoke-builtin)) runtime plan function [value]))
    (catch clojure.lang.ExceptionInfo ex
      (throw ex))
    (catch Exception ex
      ((deref (get operations :fail-builtin-error!)) plan function ex))))

(defn map-entry
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

(defn fail-recur!
  [fail-var plan target-arity actual-arity reason]
  ((deref fail-var) "L2-RECUR-TARGET"
         "recur has no compatible loop or function target"
         {:source-span {:source (get-in plan [:source :path])}
          :target-arity target-arity
          :actual-arity actual-arity
          :reason reason
          :remediation
          "Use recur only in tail position inside a compatible loop or function with matching arity."}))

(defn nontail-value!
  [plan value reason operations]
  (when ((deref (get operations :recur-signal?)) value)
    ((deref (get operations :recur-fail!))
     plan nil (count ((deref (get operations :recur-values)) value)) reason))
  value)

(defn execute-generic-builtin-call-scoped
  [runtime plan env instruction function public-invocation-scope operations]
  (let [args ((deref (get operations :execute-values-scoped))
              runtime plan env (:args instruction)
              :recur-inside-builtin-argument public-invocation-scope)]
    (if (and (= 'str function)
             (:runtime-artifact-plan runtime))
      (case (count args)
        1 ((deref (get operations :artifact-invoke))
           runtime (deref (get operations :artifact-function)) args)
        2 ((deref (get operations :artifact-invoke))
           runtime (deref (get operations :artifact-concat-function)) args)
        ((deref (get operations :fail-call-arity!))
         "L2-BUILTIN-ARITY" plan function args "1 or 2"))
      (if (contains? instruction :compiler-internal-caller)
        ((deref (get operations :invoke-compiler-internal-builtin))
         runtime plan instruction args public-invocation-scope)
        ((deref (get operations :invoke-builtin))
         runtime plan function args instruction public-invocation-scope)))))

(defn execute-call-scoped [runtime plan env instruction public-invocation-scope operations]
  (let [function (:function instruction)
        external-authority
        (when (contains? instruction :external-link)
          ((deref (get operations :authorize-external-link)) runtime plan instruction))
        args ((deref (get operations :execute-values-scoped))
              runtime plan env (:args instruction) :recur-inside-function-argument public-invocation-scope)]
    (if external-authority
      ((deref (get operations :execute-external-machine))
       runtime plan instruction external-authority args public-invocation-scope)
      ((deref (get operations :execute-function-scoped)) runtime plan function args public-invocation-scope))))


(doseq [operation [#'map-entry #'fail-recur! #'nontail-value! #'execute-generic-builtin-call-scoped #'execute-call-scoped #'executor-rule-record #'fail-call-arity! #'assert-min-arity! #'assert-exact-arity! #'assert-between-arity! #'assert-even-arity! #'hosted-unary-builtin #'execute-instruction-scoped #'execute-value-scoped #'bind-values #'execute-values-scoped #'execute-map-entries-scoped #'execute-instructions-scoped #'hosted-builtin #'hosted-add #'hosted-subtract #'hosted-multiply #'hosted-divide #'hosted-equal #'hosted-less-than #'hosted-greater-than #'hosted-less-than-or-equal #'hosted-greater-than-or-equal #'hosted-str #'fail-builtin-error! #'hosted-get #'execute-function-scoped #'execute-function #'artifact-invoke #'execute-plan]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
