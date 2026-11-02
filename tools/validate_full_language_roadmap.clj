(require '[gravity.tooling.full-language-roadmap :as roadmap])

(try
  (if (= ["--self-test"] *command-line-args*)
    (let [{:keys [accepted rejected]} (roadmap/self-test)]
      (println (str "full-language roadmap validation self-test passed: "
                    accepted " accepted and " rejected " rejected fixtures")))
    (do
      (when (seq *command-line-args*)
        (throw (ex-info "usage: clojure -M tools/validate_full_language_roadmap.clj [--self-test]"
                        {:diagnostic "FLR000"})))
      (roadmap/validate-current)
      (println "full-language roadmap validation passed")))
  (catch clojure.lang.ExceptionInfo exception
    (binding [*out* *err*]
      (println (str "full-language roadmap validation failed ["
                    (or (:diagnostic (ex-data exception)) "FLR999")
                    "]: " (.getMessage exception))))
    (System/exit 1)))
