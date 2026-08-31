

(defn compile-stage0-expr
  [module locals form]
  (cond
    (symbol? form)
    (if (contains? locals form)
      {:op :local :name form}
      (fail! "L2-UNKNOWN-SYMBOL"
             "stage0 cannot resolve symbol"
             {:source-span {:source (:source-path module)}
              :symbol form
              :remediation "Define the symbol or stay within the stage0 subset."}))

    (seq? form)
    (case (first form)
      println {:op :println
               :effect :io/write
               :capability :io/stdout
               :args (mapv #(compile-stage0-expr module locals %) (rest form))}
      do {:op :do
          :body (mapv #(compile-stage0-expr module locals %) (rest form))}
      if (let [[_ test then else] form]
           {:op :if
            :test (compile-stage0-expr module locals test)
            :then (compile-stage0-expr module locals then)
            :else (compile-stage0-expr module locals else)})
      let (let [[_ bindings & body] form]
            (compile-stage0-let module locals bindings body))
      quote {:op :quote :value (second form)}
      host-reflect (fail! "P4-HOST-REFLECTION"
                          "host reflection is not implemented in stage0"
                          {:source-span {:source (:source-path module)}
                           :remediation "Remove host reflection from the stage0 executable subset."})
      (let [callee (first form)
            args (mapv #(compile-stage0-expr module locals %) (rest form))]
        (cond
          (contains? stage0-builtin-functions callee)
          {:op :builtin-call :function callee :args args}

          (contains? (:function-table module) callee)
          {:op :function-call :function callee :args args}

          :else
          (fail! "L2-UNKNOWN-CORE-FORM"
                 "stage0 cannot execute this form"
                 {:source-span {:source (:source-path module)}
                  :operator callee
                  :remediation "Use defn, println, do, if, let, quote, supported core builtins, or local function calls in the stage0 hosted subset."}))))

    (or (vector? form) (map? form) (set? form))
    (compile-stage0-collection module locals form)

    :else
    {:op :literal :value form}))

(defn compile-stage0-function
  [module {:keys [name params body] :as definition}]
  (assoc definition
         :binding {:name name
                   :kind :function
                   :namespace (:module module)
                   :profile (:profile module)
                   :target (:target module)
                   :visibility (if (seq (:exports module))
                                 (if (contains? (set (:exports module)) name)
                                   :public
                                   :private)
                                 :stage0-local)
                   :effects (:effects module)
                   :capabilities (:capabilities module)}
         :instructions (mapv #(compile-stage0-expr module (set params) %) body)))

(defn stage0-instruction-summary
  [instruction]
  (let [children
        (case (:op instruction)
          :println (:args instruction)
          :do (:body instruction)
          :if [(:test instruction) (:then instruction) (:else instruction)]
          :let (concat (map :expr (:bindings instruction)) (:body instruction))
          :loop (concat (map :expr (:bindings instruction))
                        (:body instruction))
          :recur (:args instruction)
          :builtin-call (:args instruction)
          :function-call (:args instruction)
          :vector-literal (:items instruction)
          :set-literal (:items instruction)
          :map-literal (mapcat (fn [{:keys [key value]}] [key value])
                                (:entries instruction))
          [])]
    ;; A parent and child may share an opcode (for example nested builtin
    ;; calls).  Add the parent fact instead of letting `merge` overwrite it;
    ;; this makes the stage0 comparison describe every concrete instruction.
    (merge-with +
                {(:op instruction) 1}
                (apply merge-with +
                       (map stage0-instruction-summary
                            (remove nil? children))))))

(defn stage0-compiled-core-plan
  [source-path source-text module]
  (let [function-table (stage0-function-table module)
        module (assoc module :function-table function-table)
        _ (validate-stage0-compiled-profile! module)
        _ (executable-profile! source-path module (:forms module))
        _ (validate-module-effects! module)
        _ (validate-stage0-executable-safety! module)
        _ (validate-stage0-compiled-performance! module)
        _ (validate-stage0-compiled-math! module)
        _ (validate-stage0-compiled-compiler! module)
        _ (validate-stage0-compiled-backend! module)
        _ (validate-stage0-compiled-runtime! module)
        _ (validate-stage0-compiled-domain! module)
        _ (validate-stage0-compiled-schema! module)
        _ (validate-stage0-compiled-ai! module)
        _ (validate-stage0-compiled-package! module)
        _ (validate-stage0-compiled-tooling! module)
        _ (validate-stage0-compiled-conformance! module)
        functions (into (sorted-map)
                        (map (fn [[name definition]]
                               [name (compile-stage0-function
                                      module
                                      definition)]))
                        function-table)
        main-function (get functions 'main)
        _ (when-not main-function
            (fail! "L3-UNKNOWN-ALIAS"
                   "stage0 executable requires a main function"
                   {:source-span {:source source-path}
                    :remediation "Add (defn main [] ...)."}))
        _ (when-not (empty? (:params main-function))
            (fail! "L2-MAIN-ARITY"
                   "stage0 main must take no arguments"
                   {:source-span {:source source-path}
                    :params (:params main-function)
                    :remediation "Use (defn main [] ...)."}))
        instruction-summary (apply merge-with +
                                   (mapcat (fn [[_ function]]
                                             (map stage0-instruction-summary
                                                  (:instructions function)))
                                           functions))
        plan-base {:kind :gravity/stage0-hosted-core-compiled-plan
                   :entrypoint 'main
                   :source {:path source-path
                            :sha256 (str "sha256:" (sha256-hex source-text))}
                   :compiler {:owner :clojure-bootstrap
                              :stage :stage0
                              :input :macro-expanded-core
                              :output :instruction-plan
                              :retirement-objective
                              :replace-with-gravity-self-hosted-compiler}
                   :module (select-keys module
                                        [:module :source-path :profile :target
                                         :effects :capabilities :providers
                                         :exports
                                         :safety])
                   :binding-table (mapv (fn [[name function]]
                                           (assoc (:binding function)
                                                  :arity (:arity function)))
                                         (sort-by key functions))
                   :functions functions
                   :instruction-summary instruction-summary
                   :effect-summary {:declared (:effects module)
                                    :inferred (if (pos? (get instruction-summary
                                                             :println
                                                             0))
                                                #{:io/write}
                                                #{})
                                    :capabilities (:capabilities module)}
                   :diagnostics []}]
    (assoc plan-base
           :plan-id (str "sha256:" (sha256-hex (pr-str plan-base))))))

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