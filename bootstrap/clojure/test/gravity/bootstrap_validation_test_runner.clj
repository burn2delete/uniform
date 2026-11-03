(ns gravity.bootstrap-validation-test-runner
  "Explicit bootstrap-validation development entrypoint.

  The selected tests exercise existing stage, identity, equivalence and
  provenance boundaries.  The report is non-authoritative and does not claim
  rebuild, equivalence, seed retirement or release completion."
  (:require [gravity.self-hosting-test-runner :as self-hosting]
            [gravity.test-scope-runner :as scope]))

(defn- parse-args
  [args]
  (let [args (scope/normalize-bootstrap-args args)]
    (loop [remaining (seq args)
           namespaces []
           fail-fast? false]
      (if-not remaining
        {:namespaces (mapv symbol namespaces)
         :fail-fast? fail-fast?}
        (let [argument (first remaining)
              tail (next remaining)]
          (cond
            (= "--fail-fast" argument)
            (recur tail namespaces true)

            (= "--namespace" argument)
            (recur (next tail) (conj namespaces (first tail)) fail-fast?)

            :else
            (throw (ex-info "Unsupported bootstrap validation option"
                            {:id "TEST-SCOPE-OPTION" :option argument}))))))))

(defn run-cli!
  [args]
  (cond
    (= ["--help"] (vec args))
    (do (print (scope/usage :bootstrap)) (flush) 0)
    (= ["--list"] (vec args))
    (do (doseq [namespace (scope/scope-namespaces :bootstrap)]
          (println namespace))
        (flush)
        0)
    :else
    (let [{:keys [namespaces fail-fast?]} (parse-args args)
          report (self-hosting/run-namespaces
                  {:namespaces namespaces
                   :fail-fast? fail-fast?
                   :visible? true})]
      (prn report)
      (if (= :passed (:status report)) 0 1))))

(defn -main
  [& args]
  (try
    (System/exit (run-cli! args))
    (catch clojure.lang.ExceptionInfo ex
      (binding [*out* *err*]
        (println (str "bootstrap-validation-test-runner: " (.getMessage ex)))
        (println "Try --help for usage."))
      (System/exit 2))))
