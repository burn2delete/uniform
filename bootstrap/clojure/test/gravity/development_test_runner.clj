(ns gravity.development-test-runner
  "Selective, deterministic feedback runner for the stage0 bootstrap tests.

  The runner deliberately keeps clojure.test in charge of test execution and
  reporting.  Selection only changes the vars passed to the same fixture and
  reporting pipeline; it does not duplicate test logic or introduce a second
  test framework."
  (:require [clojure.test :as test]
            [clojure.string :as str]
            [gravity.bootstrap-test]))

(def ^:private test-namespace
  'gravity.bootstrap-test)

(def ^:private usage-text
  (str
   "Usage: clojure -M:<alias> -m gravity.development-test-runner [options]\n"
   "\n"
   "Runs gravity.bootstrap-test once, or a deterministic selection of its test vars.\n"
   "With no selector, all tests run.\n"
   "\n"
   "Options (repeatable):\n"
   "  --exact NAME       select NAME (or gravity.bootstrap-test/NAME) exactly\n"
   "  --regex REGEX      select names matching REGEX\n"
   "  --prefix PREFIX    select names beginning with PREFIX\n"
   "  --list             print selected names and exit without running tests\n"
   "  --fail-fast        stop after the first test failure or error\n"
   "  --help             print this help\n"
   "\n"
   "Selectors are matched against the short var name and its qualified name.\n"))

(defn- test-var-records
  "Returns all deftest vars in stable qualified-name order.

  ns-interns is used rather than ns-publics so private deftest vars retain the
  same visibility to this runner as they have to clojure.test/test-all-vars."
  []
  (->> (ns-interns test-namespace)
       (keep (fn [[name test-var]]
               (when (:test (meta test-var))
                 (let [short-name (str name)
                       qualified-name (str (ns-name (:ns (meta test-var))) "/" short-name)]
                   {:name short-name
                    :qualified-name qualified-name
                    :var test-var}))))
       (sort-by :qualified-name)
       vec))

(defn- selector-value
  [option value]
  (let [value (some-> value str/trim)]
    (when (str/blank? value)
      (throw (ex-info (str option " requires a non-empty value")
                      {:type ::invalid-selector
                       :option option})))
    value))

(defn- parse-args
  [args]
  (loop [remaining (seq args)
         options {:exact []
                  :regex []
                  :prefix []
                  :list? false
                  :fail-fast? false
                  :help? false}]
    (if-not remaining
      options
      (let [argument (first remaining)
            tail (next remaining)]
        (cond
          (= argument "--help")
          (recur tail (assoc options :help? true))

          (= argument "--list")
          (recur tail (assoc options :list? true))

          (= argument "--fail-fast")
          (recur tail (assoc options :fail-fast? true))

          (or (= argument "--exact") (= argument "-e"))
          (if (seq tail)
            (recur (next tail)
                   (update options :exact conj (selector-value argument (first tail))))
            (throw (ex-info (str argument " requires a non-empty value")
                            {:type ::invalid-selector
                             :option argument})))

          (or (= argument "--regex") (= argument "--pattern") (= argument "-r"))
          (if (seq tail)
            (recur (next tail)
                   (update options :regex conj (selector-value argument (first tail))))
            (throw (ex-info (str argument " requires a non-empty value")
                            {:type ::invalid-selector
                             :option argument})))

          (or (= argument "--prefix") (= argument "-p"))
          (if (seq tail)
            (recur (next tail)
                   (update options :prefix conj (selector-value argument (first tail))))
            (throw (ex-info (str argument " requires a non-empty value")
                            {:type ::invalid-selector
                             :option argument})))

          (= argument "--")
          (recur nil (update options :exact into (map #(selector-value "selector" %) tail)))

          (str/starts-with? argument "-")
          (throw (ex-info (str "unknown option: " argument)
                          {:type ::usage-error
                           :option argument}))

          :else
          ;; Positional selectors are exact names.  This keeps the command
          ;; convenient in a shell while making every non-explicit selector
          ;; unambiguous and auditable.
          (recur tail
                 (update options :exact conj (selector-value "selector" argument))))))))

(defn- matches-name?
  [record selector]
  (or (= selector (:name record))
      (= selector (:qualified-name record))))

(defn- matches-prefix?
  [record prefix]
  (or (str/starts-with? (:name record) prefix)
      (str/starts-with? (:qualified-name record) prefix)))

(defn- compile-patterns
  [patterns]
  (mapv (fn [pattern]
          (try
            (re-pattern pattern)
            (catch java.util.regex.PatternSyntaxException ex
              (throw (ex-info (str "invalid regex selector " (pr-str pattern) ": "
                                  (.getDescription ex))
                              {:type ::invalid-selector
                               :selector pattern}
                              ex)))))
        patterns))

(defn- matches-regex?
  [record patterns]
  (some (fn [pattern]
          (or (re-find pattern (:name record))
              (re-find pattern (:qualified-name record))))
        patterns))

(defn- select-test-vars
  [records {:keys [exact regex prefix]}]
  (let [compiled-patterns (compile-patterns regex)
        selector-supplied? (or (seq exact) (seq regex) (seq prefix))
        selected
        (filter (fn [record]
                  (or (not selector-supplied?)
                      (some #(matches-name? record %) exact)
                      (some #(matches-prefix? record %) prefix)
                      (matches-regex? record compiled-patterns)))
                records)
        selected (vec selected)]
    (doseq [selector exact]
      (when-not (some #(matches-name? % selector) records)
        (throw (ex-info (str "unknown exact test selector: " selector)
                        {:type ::unknown-selector
                         :selector selector
                         :kind :exact}))))
    (doseq [selector prefix]
      (when-not (some #(matches-prefix? % selector) records)
        (throw (ex-info (str "selector matched no test vars: --prefix " selector)
                        {:type ::unknown-selector
                         :selector selector
                         :kind :prefix}))))
    (doseq [[selector pattern] (map vector regex compiled-patterns)]
      (when-not (some #(or (re-find pattern (:name %))
                           (re-find pattern (:qualified-name %)))
                      records)
        (throw (ex-info (str "selector matched no test vars: --regex " selector)
                        {:type ::unknown-selector
                         :selector selector
                         :kind :regex}))))
    selected))

(defn- report-summary
  [summary]
  (test/do-report (assoc summary :type :summary))
  summary)

(defn- run-selected-tests
  "Runs selected vars with namespace and per-test fixtures intact.

  This mirrors clojure.test/test-vars while allowing a fail-fast boundary
  between vars.  The once fixture still wraps the complete selected batch and
  each fixture still wraps exactly one test, so enabling --fail-fast does not
  change fixture semantics for tests that do run."
  [selected-vars fail-fast?]
  (let [namespace-object (the-ns test-namespace)
        once-fixture-fn (test/join-fixtures (::test/once-fixtures
                                             (meta namespace-object)))
        each-fixture-fn (test/join-fixtures (::test/each-fixtures
                                             (meta namespace-object)))]
    (binding [test/*report-counters* (ref test/*initial-report-counters*)]
      (test/do-report {:type :begin-test-ns :ns namespace-object})
      (let [stopped? (atom false)]
        (once-fixture-fn
         (fn []
           (doseq [test-var selected-vars]
             (when (and (:test (meta test-var)) (not @stopped?))
               (each-fixture-fn #(test/test-var test-var))
               (when (and fail-fast?
                          (pos? (+ (:fail @test/*report-counters*)
                                   (:error @test/*report-counters*))))
                 (reset! stopped? true))))))
        (test/do-report {:type :end-test-ns :ns namespace-object})
        (report-summary @test/*report-counters*)))))

(defn- print-selection
  [selected-records]
  (doseq [{:keys [qualified-name]} selected-records]
    (println qualified-name))
  (flush))

(defn run-cli!
  "Parses args, selects and runs tests, and returns a conventional exit code.

  This function is intentionally side-effectful only through clojure.test's
  normal reports and stdout/stderr.  Keeping it separate from -main makes the
  selection and exit behavior straightforward to exercise in a REPL."
  [args]
  (let [options (parse-args args)]
    (if (:help? options)
      (do
        (print usage-text)
        0)
      (let [records (test-var-records)
            selected-records (select-test-vars records options)]
        (when (and (seq (:exact options)) (empty? selected-records))
          (throw (ex-info "selectors matched no test vars"
                          {:type ::unknown-selector})))
        (if (:list? options)
          (do
            (print-selection selected-records)
            0)
          (let [summary (run-selected-tests
                         (mapv :var selected-records)
                         (:fail-fast? options))]
            (if (and (zero? (:fail summary)) (zero? (:error summary))
                     (= (:test summary) (count selected-records)))
              0
              1)))))))

(defn- report-cli-error
  [^Throwable ex]
  (binding [*out* *err*]
    (println (str "development-test-runner: " (.getMessage ex)))
    (when (= ::usage-error (:type (ex-data ex)))
      (println "Try --help for usage."))
    (flush)))

(defn -main
  [& args]
  (try
    (System/exit (run-cli! args))
    (catch clojure.lang.ExceptionInfo ex
      (report-cli-error ex)
      (System/exit 2))
    (catch Throwable ex
      (report-cli-error ex)
      (System/exit 2))))
