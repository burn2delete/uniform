(ns gravity.compiler-composition-test-runner
  "Explicit compiler composition development entrypoint.

  This scope exercises selected pass handoffs through the existing development
  runner.  It is composition feedback, not self-hosting or release evidence."
  (:require [gravity.test-scope-runner :as scope]
            [gravity.development-test-runner :as development]))

(defn run-cli!
  [args]
  (cond
    (= ["--help"] (vec args))
    (do (print (scope/usage :compiler)) (flush) 0)
    (= ["--list"] (vec args))
    (do (doseq [namespace (scope/scope-namespaces :compiler)]
          (println namespace))
        (flush)
        0)
    :else
    (development/run-cli!
     (scope/normalize-development-args :compiler args))))

(defn -main
  [& args]
  (try
    (System/exit (run-cli! args))
    (catch clojure.lang.ExceptionInfo ex
      (binding [*out* *err*]
        (println (str "compiler-composition-test-runner: " (.getMessage ex)))
        (println "Try --help for usage."))
      (System/exit 2))))
