(ns gravity.project-structure-test-runner
  "Bounded one-JVM gate for the extracted Stage 0 project-structure leaves.

  The leaf namespaces run first, in a fixed order, followed by the four
  compatibility vars selected from gravity.bootstrap-test.  This is a
  development feedback gate only; it deliberately does not claim authority or
  equivalence with the broad bootstrap suite."
  (:require [clojure.test :as test]
            [clojure.string :as str]))

(def ^:private leaf-namespaces
  ['gravity.source-unit-test
   'gravity.source-span-test
   'gravity.digest-test])

(def ^:private bootstrap-namespace
  'gravity.bootstrap-test)

(def ^:private bootstrap-exact-vars
  #{"gravity.bootstrap-test/hosted-hello-runs"
    "gravity.bootstrap-test/reader-source-unit-identity-preserves-path-extension-and-options"
    "gravity.bootstrap-test/reader-file-policy-rejects-extension-and-malformed-utf8"
    "gravity.bootstrap-test/c2-reader-treats-cr-lf-and-crlf-as-line-terminators"})

(def ^:private usage-text
  (str
   "Usage: clojure -M:project-structure-test [options]\n"
   "\n"
   "Runs the extracted project-structure tests and four bootstrap compatibility vars in one JVM.\n"
   "\n"
   "Options:\n"
   "  --exact NAME       select one qualified bootstrap compatibility var (repeatable)\n"
   "  --fail-fast        stop after the first failure or error\n"
   "  --help             print this help\n"))

(defn- selector-value
  [option value]
  (let [value (some-> value str/trim)]
    (when (str/blank? value)
      (throw (ex-info (str option " requires a non-empty value")
                      {:type ::usage-error})))
    value))

(defn- parse-args
  [args]
  (loop [remaining (seq args)
         options {:exact []
                  :fail-fast? false}]
    (if-not remaining
      options
      (let [argument (first remaining)
            tail (next remaining)]
        (cond
          (= argument "--help")
          (recur tail (assoc options :help? true))

          (= argument "--fail-fast")
          (recur tail (assoc options :fail-fast? true))

          (= argument "--exact")
          (if (seq tail)
            (recur (next tail)
                   (update options :exact conj (selector-value argument (first tail)))
                   )
            (throw (ex-info "--exact requires a non-empty value"
                            {:type ::usage-error})))

          (str/starts-with? argument "-")
          (throw (ex-info (str "unknown option: " argument)
                          {:type ::usage-error}))

          :else
          (throw (ex-info (str "unexpected argument: " argument)
                          {:type ::usage-error})))))))

(defn- test-var-records
  [namespace-symbol]
  (let [namespace-object (the-ns namespace-symbol)]
    (->> (ns-interns namespace-object)
         (map (fn [[name test-var]]
                (let [short-name (str name)
                      qualified-name (str (ns-name (:ns (meta test-var))) "/" short-name)]
                  {:name short-name
                   :qualified-name qualified-name
                   :var test-var})))
         (sort-by :qualified-name)
         vec)))

(defn- validate-exact-selectors
  [selectors]
  (let [selectors (vec selectors)]
    (when-not (= (count selectors) (count (distinct selectors)))
      (throw (ex-info "duplicate --exact bootstrap test selector"
                      {:type ::usage-error
                       :selectors selectors})))
    (let [unknown (seq (remove bootstrap-exact-vars selectors))]
      (when unknown
        (throw (ex-info (str "unknown exact bootstrap test selector: " (first unknown))
                        {:type ::usage-error
                         :selector (first unknown)}))))
    (when-not (= (set selectors) bootstrap-exact-vars)
      (throw (ex-info "project-structure gate requires exactly the four bootstrap compatibility vars"
                      {:type ::usage-error
                       :expected (sort bootstrap-exact-vars)
                       :actual (sort (set selectors))})))
    selectors))

(defn- selected-bootstrap-vars
  [namespace-symbol selectors]
  (let [selectors (validate-exact-selectors selectors)
        records (filterv #(get (meta (:var %)) :test)
                         (test-var-records namespace-symbol))
        known (set (map :qualified-name records))]
    (doseq [selector selectors]
      (when-not (contains? known selector)
        (throw (ex-info (str "unknown exact bootstrap test selector: " selector)
                        {:type ::usage-error
                         :selector selector}))))
    (mapv (fn [selector]
            (some #(when (= selector (:qualified-name %)) %) records))
          selectors)))

(defn- report-summary
  [summary]
  (test/do-report (assoc summary :type :summary))
  summary)

(defn- run-selected-vars
  "Run one namespace with its normal once/each fixtures intact.

  A separate clojure.test counter binding is used per namespace so the
  aggregate summary remains deterministic while fixtures still wrap exactly
  the same vars they wrap in the broad runner."
  [namespace-symbol selected-vars fail-fast?]
  (let [namespace-object (the-ns namespace-symbol)
        test-ns-hook (ns-resolve namespace-object 'test-ns-hook)
        once-fixture-fn (test/join-fixtures (::test/once-fixtures
                                             (meta namespace-object)))
        each-fixture-fn (test/join-fixtures (::test/each-fixtures
                                             (meta namespace-object)))]
    (when test-ns-hook
      (throw (ex-info (str "project-structure runner cannot select namespace with test-ns-hook: "
                          namespace-symbol)
                      {:type ::unsupported-test-hook
                       :namespace namespace-symbol})))
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
                 (reset! stopped? true))))
           ))
        (test/do-report {:type :end-test-ns :ns namespace-object})
        (report-summary @test/*report-counters*)))))

(defn- add-summaries
  [left right]
  (merge-with (fn [a b] (+ (long a) (long b))) left right))

(defn- planned-test-count
  [records]
  (count (filter #(get (meta (:var %)) :test) records)))

(defn run-suite!
  "Run a supplied leaf/bootstrap plan and return its summary and exit code.

  The plan arguments are intentionally explicit so tests can supply synthetic
  namespaces and exercise fail-fast/error paths without adding a public
  command-line failure injection hook."
  [{:keys [leaf-namespaces bootstrap-namespace bootstrap-selectors bootstrap-vars
           leaf-loader bootstrap-loader bootstrap-resolver fail-fast?]
    :or {leaf-namespaces gravity.project-structure-test-runner/leaf-namespaces
         bootstrap-namespace gravity.project-structure-test-runner/bootstrap-namespace
         leaf-loader require
         bootstrap-loader require
         bootstrap-resolver selected-bootstrap-vars
         fail-fast? false}}]
  (let [leaf-result
        (reduce
         (fn [{:keys [summary ran-namespaces failed? planned-tests] :as aggregate}
              namespace-symbol]
           (leaf-loader namespace-symbol)
           (let [records (test-var-records namespace-symbol)
                 result (run-selected-vars
                         namespace-symbol
                         (mapv :var records)
                         fail-fast?)
                 failed-now? (pos? (+ (:fail result) (:error result)))]
             (if (and fail-fast? failed-now?)
               (reduced {:summary (add-summaries summary result)
                         :ran-namespaces (conj ran-namespaces namespace-symbol)
                         :failed? true
                         :planned-tests (+ planned-tests (planned-test-count records))})
               {:summary (add-summaries summary result)
                :ran-namespaces (conj ran-namespaces namespace-symbol)
                :failed? (or failed? failed-now?)
                :planned-tests (+ planned-tests (planned-test-count records))})))
         {:summary test/*initial-report-counters*
          :ran-namespaces []
          :failed? false
          :planned-tests 0}
         leaf-namespaces)
        leaf-failed? (:failed? leaf-result)
        stop-before-bootstrap? (and fail-fast? leaf-failed?)
        bootstrap-result
        (if stop-before-bootstrap?
          {:summary test/*initial-report-counters*
           :ran-namespaces []}
          (do
            ;; In fail-fast mode bootstrap is deliberately loaded only after
            ;; every leaf result is green. This avoids paying the
            ;; require/resolve cost for a failing leaf and prevents a false
            ;; compatibility pass. Full-signal mode reports all leaf results
            ;; and then continues to the compatibility component.
            (when (nil? bootstrap-vars)
              (bootstrap-loader bootstrap-namespace))
            (let [selected-bootstrap (if (some? bootstrap-vars)
                                       (mapv #(if (map? %) % {:var %}) bootstrap-vars)
                                       (bootstrap-resolver bootstrap-namespace
                                                            bootstrap-selectors))
                  result (run-selected-vars
                          bootstrap-namespace
                          (mapv :var selected-bootstrap)
                          fail-fast?)]
              {:summary result
               :ran-namespaces [bootstrap-namespace]})))
        summary (add-summaries (:summary leaf-result) (:summary bootstrap-result))
        ran-namespaces (into (:ran-namespaces leaf-result)
                             (:ran-namespaces bootstrap-result))
        expected-tests (+ (:planned-tests leaf-result)
                          (if stop-before-bootstrap?
                            0
                            (if (some? bootstrap-vars)
                              (count bootstrap-vars)
                              (count bootstrap-exact-vars))))
        exit-code (if (and (zero? (:fail summary))
                           (zero? (:error summary))
                           (= (:test summary) expected-tests))
                    0
                    1)]
    {:summary summary
     :expected-tests expected-tests
     :ran-namespaces ran-namespaces
     :exit-code exit-code}))

(defn run-cli!
  "Run the bounded leaf-plus-compatibility gate and return an exit code."
  [args]
  (let [options (parse-args args)]
    (if (:help? options)
      (do (print usage-text) 0)
      (let [_ (validate-exact-selectors (:exact options))
            {:keys [summary exit-code]} (run-suite!
                                         {:bootstrap-selectors (:exact options)
                                          :fail-fast? (:fail-fast? options)})]
        (println (str "project-structure-gate: authority=non-authoritative, "
                      (:test summary) " tests, " (:pass summary) " assertions passed, "
                      (:fail summary) " failures, " (:error summary) " errors"))
        (flush)
        exit-code))))

(defn- cleanup!
  []
  (flush)
  (shutdown-agents)
  (flush))

(defn- report-cli-error
  [^Throwable ex]
  (binding [*out* *err*]
    (println (str "project-structure-test-runner: " (.getMessage ex)))
    (println "Try --help for usage.")
    (flush)))

(defn -main
  [& args]
  (try
    (let [exit-code
          (try
            (run-cli! args)
            (catch clojure.lang.ExceptionInfo ex
              (if (= ::usage-error (:type (ex-data ex)))
                (do
                  (report-cli-error ex)
                  2)
                (throw ex))))]
      (cleanup!)
      (System/exit exit-code))
    (catch Throwable ex
      ;; Fixture/test-hook failures retain their original Throwable after the
      ;; lifecycle cleanup has completed; do not turn them into a false pass.
      (cleanup!)
      (throw ex))))
