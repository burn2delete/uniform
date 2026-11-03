(ns gravity.project-test-runner
  "Scoped development entrypoint shared by language, compiler and bootstrap.

  Aliases bind one project scope through --scope.  The existing development
  runner remains responsible for loading fixtures, applying selectors and
  executing tests; this namespace only prevents a scope from silently
  widening to the old bootstrap default."
  (:require [clojure.string :as str]
            [gravity.development-test-runner :as development]
            [gravity.test-scope-runner :as scope]))

(defn- parse-scope
  [args]
  (loop [remaining (seq args)
         selected nil
         forwarded []]
    (if-not remaining
      (if selected
        [selected forwarded]
        (throw (ex-info "A project test alias requires --scope"
                        {:id "TEST-SCOPE-MISSING"})))
      (let [argument (first remaining)
            tail (next remaining)]
        (if (= "--scope" argument)
          (let [scope-name (str/lower-case (str (first tail)))
                scope-key (get {"language" :language
                                "compiler" :compiler
                                "bootstrap" :bootstrap}
                               scope-name)]
            (when (or (nil? (first tail)) selected)
              (throw (ex-info "--scope requires one unique value"
                              {:id "TEST-SCOPE-OPTION" :option argument})))
            (if scope-key
              (recur (next tail) scope-key forwarded)
              (throw (ex-info "Unknown project test scope"
                              {:id "TEST-SCOPE-UNKNOWN"
                               :scope scope-name}))))
          (recur tail selected (conj forwarded argument)))))))

(defn- print-scope-list!
  [scope-key]
  (doseq [namespace (scope/scope-namespaces scope-key)]
    (println namespace))
  (flush)
  0)

(defn run-cli!
  [args]
  (let [[scope-key forwarded] (parse-scope args)]
    (cond
      (= ["--help"] (vec forwarded))
      (do (print (scope/usage scope-key)) (flush) 0)

      (= ["--list"] (vec forwarded))
      (print-scope-list! scope-key)

      :else
      (development/run-cli!
       (scope/normalize-development-args scope-key forwarded)))))

(defn -main
  [& args]
  (try
    (System/exit (run-cli! args))
    (catch clojure.lang.ExceptionInfo ex
      (binding [*out* *err*]
        (println (str "project-test-runner: " (.getMessage ex)))
        (println "Try --help for usage."))
      (System/exit 2))))
