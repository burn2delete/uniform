

(defn print-diagnostic!
  [exception]
  (cli-diagnostic-presentation/print-diagnostic!
   p15-s23-c-backend-sanitized-complete-diagnostic
   exception))

(defn- resolve-cli-operation
  [operation]
  (or (ns-resolve 'gravity.bootstrap operation)
      (throw
       (IllegalStateException.
        (str "unresolved bootstrap CLI operation: " operation)))))

(defn- sh07-public-core-cli-input [arguments]
  (cli/sh07-core-input arguments))

(defn -main
  [& arguments]
  (let [[command path] arguments]
    (try
      (case command
        "sh07-core" (let [[path options] (sh07-public-core-cli-input arguments)]
                      (prn (sh07-public-core-file-artifact path options)))
        (cli-entrypoint/run!
         arguments
         {:resolve-operation resolve-cli-operation
          :print-diagnostic! print-diagnostic!
          :exit! (fn [status] (System/exit status))}))
      (when (= "sh07-core" command)
        (shutdown-agents))
      (catch clojure.lang.ExceptionInfo exception
        (print-diagnostic! exception)
        (System/exit 1)))))
