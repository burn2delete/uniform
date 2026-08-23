(require '[gravity.tooling.repository-hygiene :as hygiene])

(try
  (when (seq *command-line-args*)
    (throw (ex-info "usage: clojure -M tools/validate_repository_hygiene.clj"
                    {:diagnostic "RH000"})))
  (let [violations (hygiene/validate-repository)]
    (if (seq violations)
      (do
        (binding [*out* *err*]
          (println "repository hygiene validation failed [RH003]: tracked Python cache output")
          (doseq [path violations] (println (str "- " path))))
        (System/exit 1))
      (println "repository hygiene validation passed: no tracked Python cache output")))
  (catch clojure.lang.ExceptionInfo exception
    (binding [*out* *err*]
      (println (str "repository hygiene validation failed ["
                    (or (:diagnostic (ex-data exception)) "RH999")
                    "]: " (.getMessage exception))))
    (System/exit 2)))
