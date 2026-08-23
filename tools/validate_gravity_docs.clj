(require '[gravity.tooling.document-validation :as documents])

(try
  (when (seq *command-line-args*)
    (throw (ex-info "usage: clojure -M tools/validate_gravity_docs.clj"
                    {:diagnostic "DOC000"})))
  (let [{:keys [documents phase-indexes]} (documents/validate-repository)]
    (println (str "validation passed: " documents " docs, " phase-indexes
                  " phase indexes, ASCII, no placeholders")))
  (catch clojure.lang.ExceptionInfo exception
    (binding [*out* *err*]
      (println (str "validation failed ["
                    (or (:diagnostic (ex-data exception)) "DOC999")
                    "]: " (.getMessage exception))))
    (System/exit 1)))
