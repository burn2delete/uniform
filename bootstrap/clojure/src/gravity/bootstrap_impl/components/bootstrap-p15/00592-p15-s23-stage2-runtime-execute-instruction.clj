(defn p15-s23-stage2-runtime-execute-instruction
  [runtime plan env instruction]
  (case (:op instruction)
    :literal (:value instruction)
    :quote (:value instruction)
    :local
    (let [name (:name instruction)]
      (if (contains? env name)
        (get env name)
        (fail! "L2-UNKNOWN-SYMBOL"
               "stage2 runtime cannot resolve local"
               {:source-span {:source (get-in plan [:source :path])}
                :symbol name
                :remediation "Regenerate the stage2 instruction plan from a valid source module."})))
    :vector-literal
    (p15-s23-stage2-runtime-execute-values
     runtime plan env (:items instruction) :recur-inside-vector)
    :set-literal
    (set (p15-s23-stage2-runtime-execute-values
          runtime plan env (:items instruction) :recur-inside-set))
    :map-literal
    (p15-s23-stage2-runtime-execute-map-entries
     runtime plan env (:entries instruction))
    :println
    (let [module (:module plan)
          args (p15-s23-stage2-runtime-execute-values
                runtime plan env (:args instruction)
                :recur-inside-println-argument)]
      (validate-module-effects! module)
      (if (and (= 1 (count args))
               (:runtime-artifact-plan runtime))
        ;; The single-argument effect is owned by the Gravity-authored
        ;; runtime function.  Its :println instruction still crosses the
        ;; explicitly recorded generic host bridge, but formatting and the
        ;; newline effect no longer live in this executor branch.
        (p15-s23-stage2-runtime-artifact-invoke
         runtime p15-s23-stage2-runtime-artifact-println-function args)
        (if (and (= 2 (count args))
                 (:runtime-artifact-plan runtime))
          ;; Exactly two arguments are owned by the dedicated
          ;; Gravity-authored runtime function.  Arity >2 remains an explicit
          ;; compatibility boundary below until a variadic effect contract is
          ;; authored and validated.
          (p15-s23-stage2-runtime-artifact-invoke
           runtime p15-s23-stage2-runtime-artifact-println-two-function args)
          (println
           (clojure.string/join
            " "
            (map (fn [value]
                   (if (:runtime-artifact-plan runtime)
                     (p15-s23-stage2-runtime-artifact-invoke
                      runtime p15-s23-stage2-runtime-artifact-function [value])
                     (str value)))
                 args))))))
    :do
    (p15-s23-stage2-runtime-execute-instructions
     runtime plan env (:body instruction))
    :if
    (if (p15-s23-stage2-runtime-nontail-value!
         plan
         (p15-s23-stage2-runtime-execute-instruction
          runtime plan env (:test instruction))
         :recur-inside-if-test)
      (p15-s23-stage2-runtime-execute-instruction
       runtime plan env (:then instruction))
      (p15-s23-stage2-runtime-execute-instruction
       runtime plan env (:else instruction)))
    :let
    (loop [env env
           bindings (:bindings instruction)]
      (if-let [{:keys [name expr]} (first bindings)]
        (recur (assoc env name
                      (p15-s23-stage2-runtime-nontail-value!
                       plan
                       (p15-s23-stage2-runtime-execute-instruction
                        runtime plan env expr)
                       :recur-inside-let-binding))
               (rest bindings))
        (p15-s23-stage2-runtime-execute-instructions
         runtime plan env (:body instruction))))
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
                    (p15-s23-stage2-runtime-nontail-value!
                     plan
                     (p15-s23-stage2-runtime-execute-instruction
                      runtime plan scope expr)
                     :recur-inside-loop-initializer)]
                (recur (assoc scope name value) (rest remaining)))
              scope))]
      (loop [loop-env initial-env]
        (let [result
              (p15-s23-stage2-runtime-execute-instructions
               runtime plan loop-env body)]
          (if (p15-s23-stage2-runtime-recur-signal? result)
            (let [values (p15-s23-stage2-runtime-recur-values result)]
              (when-not (= target-arity (count values))
                (p15-s23-stage2-runtime-recur-fail!
                 plan target-arity (count values) :loop-arity-mismatch))
              (recur
               (p15-s23-stage2-runtime-bind-values
                env binding-names values)))
            result))))
    :recur
    (p15-s23-stage2-runtime-recur-signal
     (p15-s23-stage2-runtime-execute-values
      runtime plan env (:args instruction) :recur-inside-recur-argument))
    :builtin-call
    (let [function (:function instruction)
          argument-instructions (:args instruction)
          argument-count (count argument-instructions)]
      (cond
        (and (= 2 argument-count)
             (instance? clojure.lang.Symbol function)
             (.equals ^clojure.lang.Symbol 'get function))
        (let [collection
              (p15-s23-stage2-runtime-execute-value
               runtime plan env (nth argument-instructions 0)
               :recur-inside-builtin-argument)
              key
              (p15-s23-stage2-runtime-execute-value
               runtime plan env (nth argument-instructions 1)
               :recur-inside-builtin-argument)]
          (p15-s23-stage2-runtime-invoke-get plan collection key))

        (and (= 3 argument-count)
             (instance? clojure.lang.Symbol function)
             (.equals ^clojure.lang.Symbol 'get function))
        (let [collection
              (p15-s23-stage2-runtime-execute-value
               runtime plan env (nth argument-instructions 0)
               :recur-inside-builtin-argument)
              key
              (p15-s23-stage2-runtime-execute-value
               runtime plan env (nth argument-instructions 1)
               :recur-inside-builtin-argument)
              not-found
              (p15-s23-stage2-runtime-execute-value
               runtime plan env (nth argument-instructions 2)
               :recur-inside-builtin-argument)]
          (p15-s23-stage2-runtime-invoke-get
           plan collection key not-found))

        (and (= 1 argument-count)
             (instance? clojure.lang.Symbol function)
             (.equals ^clojure.lang.Symbol 'count function))
        (let [value
              (p15-s23-stage2-runtime-execute-value
               runtime plan env (nth argument-instructions 0)
               :recur-inside-builtin-argument)]
          (try
            (count value)
            (catch clojure.lang.ExceptionInfo ex
              (throw ex))
            (catch Exception ex
              (p15-s23-stage2-runtime-fail-builtin-error!
               plan function ex))))

        (and (= 1 argument-count)
             (instance? clojure.lang.Symbol function)
             (.equals ^clojure.lang.Symbol 'str function)
             (:runtime-artifact-plan runtime))
        (let [value
              (p15-s23-stage2-runtime-execute-value
               runtime plan env (nth argument-instructions 0)
               :recur-inside-builtin-argument)]
          ;; Preserve the established artifact boundary: ordinary artifact
          ;; exceptions are not reclassified as host builtin failures.
          (p15-s23-stage2-runtime-artifact-invoke
           runtime p15-s23-stage2-runtime-artifact-function [value]))

        (= 1 argument-count)
        (let [value
              (p15-s23-stage2-runtime-execute-value
               runtime plan env (nth argument-instructions 0)
               :recur-inside-builtin-argument)]
          (p15-s23-stage2-runtime-invoke-unary-or-generic-builtin
           plan function value))

        :else
        ;; Retain the original exact-two equality-or-generic control-flow
        ;; shape, then admit exact-three assoc before the generic fallback.
        (if (= 2 argument-count)
          (if (and (instance? clojure.lang.Symbol function)
                   (.equals ^clojure.lang.Symbol '= function))
            (let [left
                  (p15-s23-stage2-runtime-execute-value
                   runtime plan env (nth argument-instructions 0)
                   :recur-inside-builtin-argument)
                  right
                  (p15-s23-stage2-runtime-execute-value
                   runtime plan env (nth argument-instructions 1)
                   :recur-inside-builtin-argument)]
              ;; Preserve the generic path's boundary: argument evaluation is
              ;; outside the builtin try, while host equality failures are
              ;; mapped exactly like invoke-builtin.
              (try
                (= left right)
                (catch clojure.lang.ExceptionInfo ex
                  (throw ex))
                (catch Exception ex
                  (p15-s23-stage2-runtime-fail-builtin-error!
                   plan function ex))))
            (p15-s23-stage2-runtime-execute-generic-builtin-call
             runtime plan env instruction function))
          (if (and (= 3 argument-count)
                   (instance? clojure.lang.Symbol function)
                   (.equals ^clojure.lang.Symbol 'assoc function))
            (let [collection
                  (p15-s23-stage2-runtime-execute-value
                   runtime plan env (nth argument-instructions 0)
                   :recur-inside-builtin-argument)
                  key
                  (p15-s23-stage2-runtime-execute-value
                   runtime plan env (nth argument-instructions 1)
                   :recur-inside-builtin-argument)
                  value
                  (p15-s23-stage2-runtime-execute-value
                   runtime plan env (nth argument-instructions 2)
                   :recur-inside-builtin-argument)]
              ;; Preserve the generic path's argument and exception boundaries
              ;; while avoiding its argument vector and apply seq.
              (try
                (assoc collection key value)
                (catch clojure.lang.ExceptionInfo ex
                  (throw ex))
                (catch Exception ex
                  (p15-s23-stage2-runtime-fail-builtin-error!
                   plan function ex))))
            (p15-s23-stage2-runtime-execute-generic-builtin-call
             runtime plan env instruction function)))))
    :function-call
    (let [function (:function instruction)
          args (p15-s23-stage2-runtime-execute-values
                runtime plan env (:args instruction)
                :recur-inside-function-argument)]
      (p15-s23-stage2-runtime-execute-function
       runtime plan function args))
    (fail! "L2-UNKNOWN-CORE-FORM"
           "stage2 runtime plan contains an unknown instruction"
           {:source-span {:source (get-in plan [:source :path])}
            :operator (:op instruction)
            :remediation "Regenerate the stage2 instruction plan with a supported stage2 plan emitter."})))