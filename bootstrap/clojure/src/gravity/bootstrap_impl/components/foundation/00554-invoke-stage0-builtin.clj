

(defn invoke-stage0-builtin
  [module callee args]
  (try
    (case callee
      + (apply + args)
      - (do (assert-min-arity! module callee args 1)
            (apply - args))
      * (apply * args)
      / (do (assert-min-arity! module callee args 1)
            (apply / args))
      = (do (assert-min-arity! module callee args 1)
            (apply = args))
      < (do (assert-min-arity! module callee args 2)
            (apply < args))
      > (do (assert-min-arity! module callee args 2)
            (apply > args))
      <= (do (assert-min-arity! module callee args 2)
             (apply <= args))
      >= (do (assert-min-arity! module callee args 2)
             (apply >= args))
      str (apply str args)
      pr-str (p15-s23-seed-readable-pr-str (:source-path module) args)
      hash-map (do (assert-even-arity! module callee args)
                   (apply hash-map args))
      vector (vec args)
      list (apply list args)
      conj (do (assert-min-arity! module callee args 1)
               (apply conj args))
      assoc (do
              (assert-min-arity! module callee args 3)
              (when (even? (count args))
                (fail-call-arity! "L2-BUILTIN-ARITY" module callee args
                                  "a collection followed by key/value pairs"))
              (apply assoc args))
      get (do (assert-between-arity! module callee args 2 3)
              (apply get args))
      first (do (assert-exact-arity! module callee args 1)
                (first (first args)))
      second (do (assert-exact-arity! module callee args 1)
                 (second (first args)))
      rest (do (assert-exact-arity! module callee args 1)
               (p15-s23-seed-readable-normalized-rest (first args)))
      count (do (assert-exact-arity! module callee args 1)
                (count (first args))))
    (catch clojure.lang.ExceptionInfo ex
      (throw ex))
    (catch Exception ex
      (fail! "L2-BUILTIN-ERROR"
             "stage0 builtin call failed"
             {:source-span {:source (:source-path module)}
              :function callee
              :cause-message (.getMessage ex)
              :remediation "Keep builtin inputs inside the checked stage0 core subset."}))))

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
  (cond
    (vector? form)
    {:op :vector-literal
     :items (mapv #(compile-stage0-expr module locals %) form)}

    (map? form)
    {:op :map-literal
     :entries (mapv (fn [[k v]]
                      {:key (compile-stage0-expr module locals k)
                       :value (compile-stage0-expr module locals v)})
                    (sort-by (fn [[k v]] (pr-str [k v])) form))}

    (set? form)
    {:op :set-literal
     :items (mapv #(compile-stage0-expr module locals %)
                  (sort-by pr-str form))}

    :else
    {:op :literal :value form}))

(defn compile-stage0-let
  [module locals bindings body]
  (when-not (and (vector? bindings) (even? (count bindings)))
    (fail! "L2-LET-BINDING"
           "let requires an even binding vector"
           {:source-span {:source (:source-path module)}
            :bindings bindings
            :remediation "Use pairs of local names and expressions in let."}))
  (loop [scope locals
         compiled []
         pairs (partition 2 bindings)]
    (if-let [[name expr] (first pairs)]
      (do
        (when-not (symbol? name)
          (fail! "L2-LET-BINDING"
                 "let binding name must be a symbol"
                 {:source-span {:source (:source-path module)}
                  :binding name
                  :remediation "Bind symbols in stage0 let forms."}))
        (recur (conj scope name)
               (conj compiled {:name name
                               :expr (compile-stage0-expr module scope expr)})
               (rest pairs)))
      {:op :let
       :bindings compiled
       :body (mapv #(compile-stage0-expr module scope %) body)})))