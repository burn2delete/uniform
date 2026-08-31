

(defn execute-stage0-compiled-function
  [plan callee args]
  (let [definition (get-in plan [:functions callee])
        params (:params definition)]
    (when-not definition
      (fail! "L2-UNKNOWN-CORE-FORM"
             "stage0 compiled plan references an unknown function"
             {:source-span {:source (get-in plan [:source :path])}
              :function callee
              :remediation "Regenerate the compiled plan from source."}))
    (when-not (= (count params) (count args))
      (fail-call-arity! "L2-FUNCTION-ARITY"
                        (:module plan)
                        callee
                        args
                        (count params)))
    (execute-stage0-instructions plan (zipmap params args)
                                 (:instructions definition))))

(defn execute-stage0-compiled-plan
  [plan]
  (let [main-function (get-in plan [:functions (:entrypoint plan)])
        params (:params main-function)]
    (when-not main-function
      (fail! "L3-UNKNOWN-ALIAS"
             "stage0 compiled plan requires a main function"
             {:source-span {:source (get-in plan [:source :path])}
              :remediation "Regenerate the compiled plan from a module with main."}))
    (when-not (empty? params)
      (fail! "L2-MAIN-ARITY"
             "stage0 compiled main must take no arguments"
             {:source-span {:source (get-in plan [:source :path])}
              :params params
              :remediation "Use (defn main [] ...)."}))
    (with-out-str
      (execute-stage0-instructions plan {} (:instructions main-function)))))