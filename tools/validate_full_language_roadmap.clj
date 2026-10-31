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
      (let [{:keys [coverage-snapshot]} (roadmap/validate-current)]
        (println (format
                  (str "full-language roadmap structural claim validation passed; "
                       "dated coverage snapshot generated on %s (non-authoritative): "
                       "public accepted %s/%s "
                       "pass; public rejected %s/%s have feature-specific diagnostics; "
                       "completion admission %s (reported %s/%s, not a current "
                       "execution measure)")
                  (:generated-on coverage-snapshot)
                  (get-in coverage-snapshot [:accepted :passing])
                  (get-in coverage-snapshot [:accepted :total])
                  (get-in coverage-snapshot [:rejected :feature-specific])
                  (get-in coverage-snapshot [:rejected :total])
                  (if (:completion-admission-disabled? coverage-snapshot)
                    "disabled"
                    "enabled")
                  (:reported-complete-documents coverage-snapshot)
                  (:normative-documents coverage-snapshot))))))
  (catch clojure.lang.ExceptionInfo exception
    (binding [*out* *err*]
      (println (str "full-language roadmap validation failed ["
                    (or (:diagnostic (ex-data exception)) "FLR999")
                    "]: " (.getMessage exception))))
    (System/exit 1)))
