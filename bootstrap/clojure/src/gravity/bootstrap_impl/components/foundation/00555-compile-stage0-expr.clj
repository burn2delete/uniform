

(def ^:private stage0-instruction-lowering-operations
  {:compile-expression #'compile-stage0-expr
   :compile-collection #'compile-stage0-collection
   :compile-let #'compile-stage0-let
   :builtin-functions #'stage0-builtin-functions
   :fail! #'fail!})

(defn compile-stage0-expr
  [module locals form]
  (gravity.lowering/compile-native-expression stage0-instruction-lowering-operations module locals form))

(defn compile-stage0-function
  [module definition]
  (gravity.lowering/compile-native-function #'compile-stage0-expr module definition))

(defn stage0-instruction-summary
  [instruction]
  (gravity.lowering/native-instruction-summary #'stage0-instruction-summary instruction))

(def ^:private stage0-plan-assembly-operations
  {:function-table #'stage0-function-table
   :profile! #'validate-stage0-compiled-profile!
   :executable-profile! #'executable-profile!
   :effects! #'validate-module-effects!
   :safety! #'validate-stage0-executable-safety!
   :performance! #'validate-stage0-compiled-performance!
   :math! #'validate-stage0-compiled-math!
   :compiler! #'validate-stage0-compiled-compiler!
   :backend! #'validate-stage0-compiled-backend!
   :runtime! #'validate-stage0-compiled-runtime!
   :domain! #'validate-stage0-compiled-domain!
   :schema! #'validate-stage0-compiled-schema!
   :ai! #'validate-stage0-compiled-ai!
   :package! #'validate-stage0-compiled-package!
   :tooling! #'validate-stage0-compiled-tooling!
   :conformance! #'validate-stage0-compiled-conformance!
   :compile-function #'compile-stage0-function
   :fail! #'fail!
   :sha256-hex #'sha256-hex
   :instruction-summary #'stage0-instruction-summary})

(defn stage0-compiled-core-plan
  [source-path source-text module]
  (gravity.lowering/assemble-native-stage0-plan stage0-plan-assembly-operations source-path source-text module))

(defn execute-stage0-instructions
  [plan env instructions]
  (reduce (fn [_ instruction]
            (execute-stage0-instruction plan env instruction))
          nil
          instructions))

(defn execute-stage0-instruction
  [plan env instruction]
  (case (:op instruction)
    :literal (:value instruction)
    :quote (:value instruction)
    :local (if (contains? env (:name instruction))
             (get env (:name instruction))
             (fail! "L2-UNKNOWN-SYMBOL"
                    "stage0 compiled plan cannot resolve local"
                    {:source-span {:source (get-in plan [:source :path])}
                     :symbol (:name instruction)
                     :remediation "Regenerate the compiled plan from a valid stage0 source module."}))
    :vector-literal (mapv #(execute-stage0-instruction plan env %)
                          (:items instruction))
    :set-literal (set (map #(execute-stage0-instruction plan env %)
                           (:items instruction)))
    :map-literal (into {}
                       (map (fn [{:keys [key value]}]
                              [(execute-stage0-instruction plan env key)
                               (execute-stage0-instruction plan env value)]))
                       (:entries instruction))
    :println (let [module (:module plan)
                   args (mapv #(execute-stage0-instruction plan env %)
                              (:args instruction))]
               (validate-module-effects! module)
               (println (clojure.string/join " " (map str args))))
    :do (execute-stage0-instructions plan env (:body instruction))
    :if (if (execute-stage0-instruction plan env (:test instruction))
          (execute-stage0-instruction plan env (:then instruction))
          (execute-stage0-instruction plan env (:else instruction)))
    :let (loop [env env
                bindings (:bindings instruction)]
           (if-let [{:keys [name expr]} (first bindings)]
             (recur (assoc env name
                           (execute-stage0-instruction plan env expr))
                    (rest bindings))
             (execute-stage0-instructions plan env (:body instruction))))
    :builtin-call (let [module (:module plan)
                        args (mapv #(execute-stage0-instruction plan env %)
                                   (:args instruction))]
                    (invoke-stage0-builtin module (:function instruction) args))
    :function-call (let [args (mapv #(execute-stage0-instruction plan env %)
                                    (:args instruction))]
                     (execute-stage0-compiled-function
                      plan
                      (:function instruction)
                      args))
    (fail! "L2-UNKNOWN-CORE-FORM"
           "stage0 compiled plan contains an unknown instruction"
           {:source-span {:source (get-in plan [:source :path])}
            :operator (:op instruction)
            :remediation "Regenerate the compiled plan with a supported stage0 compiler."})))
