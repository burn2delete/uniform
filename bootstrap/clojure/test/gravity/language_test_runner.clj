(ns gravity.language-test-runner
  "Explicit language-semantic development entrypoint.

  This command uses the existing development runner and its ordinary focused
  selectors, but prevents an invocation from falling back to bootstrap tests."
  (:require [gravity.test-scope-runner :as scope]
            [gravity.development-test-runner :as development]))

(defn run-cli!
  [args]
  (cond
    (= ["--help"] (vec args))
    (do (print (scope/usage :language)) (flush) 0)
    (= ["--list"] (vec args))
    (do (doseq [namespace (scope/scope-namespaces :language)]
          (println namespace))
        (flush)
        0)
    :else
    (development/run-cli!
     (scope/normalize-development-args :language args))))

(defn -main
  [& args]
  (try
    (System/exit (run-cli! args))
    (catch clojure.lang.ExceptionInfo ex
      (binding [*out* *err*]
        (println (str "language-test-runner: " (.getMessage ex)))
        (println "Try --help for usage."))
      (System/exit 2))))
