

(defn invoke-stage0-builtin
  [module callee args]
  (gravity.runtime/invoke-native-stage0-builtin
    {:assert-min-arity! (var assert-min-arity!), :assert-exact-arity! (var assert-exact-arity!), :assert-between-arity! (var assert-between-arity!), :assert-even-arity! (var assert-even-arity!), :fail-call-arity! (var fail-call-arity!), :readable-pr-str (var p15-s23-seed-readable-pr-str), :normalized-rest (var p15-s23-seed-readable-normalized-rest), :fail! (var fail!)}
    module callee args))

(defn invoke-stage0-function
  [module callee args]
  (let [definition (get (:function-table module) callee)
        params (:params definition)]
    (when-not (= (count params) (count args))
      (fail-call-arity! "L2-FUNCTION-ARITY" module callee args
                        (count params)))
    (eval-do module (zipmap params args) (:body definition))))

(defn eval-collection
  [module env form]
  (cond
    (vector? form) (mapv #(eval-expr module env %) form)
    (map? form) (into {} (map (fn [[k v]]
                                [(eval-expr module env k)
                                 (eval-expr module env v)]))
                      form)
    (set? form) (set (map #(eval-expr module env %) form))
    :else form))

(defn eval-expr
  [module env form]
  (cond
    (symbol? form)
    (if (contains? env form)
      (get env form)
      (fail! "L2-UNKNOWN-SYMBOL"
             "stage0 cannot resolve symbol"
             {:source-span {:source (:source-path module)}
              :symbol form
              :remediation "Define the symbol or stay within the stage0 subset."}))

    (seq? form)
    (case (first form)
      println (do
                (validate-module-effects! module)
                (println (clojure.string/join " " (map #(str (eval-expr module env %)) (rest form)))))
      do (eval-do module env (rest form))
      if (let [[_ test then else] form]
           (if (eval-expr module env test)
             (eval-expr module env then)
             (eval-expr module env else)))
      let (let [[_ bindings & body] form]
            (eval-do module (bind-let module env bindings) body))
      quote (second form)
      host-reflect (fail! "P4-HOST-REFLECTION"
                          "host reflection is not implemented in stage0"
                          {:source-span {:source (:source-path module)}
                           :remediation "Remove host reflection from the stage0 executable subset."})
      (let [callee (first form)
            args (mapv #(eval-expr module env %) (rest form))]
        (cond
          (contains? stage0-builtin-functions callee)
          (invoke-stage0-builtin module callee args)

          (contains? (:function-table module) callee)
          (invoke-stage0-function module callee args)

          :else
          (fail! "L2-UNKNOWN-CORE-FORM"
                 "stage0 cannot execute this form"
                 {:source-span {:source (:source-path module)}
                  :operator callee
                  :remediation "Use defn, println, do, if, let, quote, supported core builtins, or local function calls in the stage0 hosted subset."}))))

    (or (vector? form) (map? form) (set? form))
    (eval-collection module env form)

    :else form))

(defn run-main
  [module]
  (let [function-table (stage0-function-table module)
        module (assoc module :function-table function-table)
        main-function (get function-table 'main)]
    (when-not main-function
      (fail! "L3-UNKNOWN-ALIAS"
             "stage0 executable requires a main function"
             {:source-span {:source (:source-path module)}
              :remediation "Add (defn main [] ...)."}))
    (let [params (:params main-function)
          body (:body main-function)]
      (when-not (empty? params)
        (fail! "L2-MAIN-ARITY"
               "stage0 main must take no arguments"
               {:source-span {:source (:source-path module)}
                :params params
                :remediation "Use (defn main [] ...)."}))
      (with-out-str
        (eval-do module {} body)))))

(defn compile-stage0-collection
  [module locals form]
  (gravity.lowering/compile-native-collection #'compile-stage0-expr module locals form))

(defn compile-stage0-let
  [module locals bindings body]
  (gravity.lowering/compile-native-let #'fail! #'compile-stage0-expr module locals bindings body))
