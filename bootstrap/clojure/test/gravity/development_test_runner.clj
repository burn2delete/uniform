(ns gravity.development-test-runner
  "Namespace-lazy, deterministic feedback runner for reviewed compatibility tests.

  The static catalog is the complete require authority. Selection changes only
  the vars passed through clojure.test's normal namespace and fixture pipeline."
  (:require [clojure.string :as str]
            [clojure.test :as test]))

(def namespace-catalog
  [{:namespace 'gravity.bootstrap-test
    :path "bootstrap/clojure/test/gravity/bootstrap_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c2-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c2_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c3-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c3_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.module-analysis-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/module_analysis_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.core-ast-lowering-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/core_ast_lowering_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.profile-validation-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/profile_validation_test.clj"
    :selectors
    ["gravity.bootstrap-compatibility.profile-validation-test/profile-facades-match-head-4921fbc-reference-table"
     "gravity.bootstrap-compatibility.profile-validation-test/profile-head-reference-policy-denial-matrix-and-dynamic-seams"
     "gravity.bootstrap-compatibility.profile-validation-test/profile-downstream-caller-artifacts-retain-head-shape"
     "gravity.bootstrap-compatibility.profile-validation-test/profile-facades-preserve-public-arglists-and-exact-leaf-parity"
     "gravity.bootstrap-compatibility.profile-validation-test/profile-policy-map-redefs-reach-the-leaf-through-the-central-seam"
     "gravity.bootstrap-compatibility.profile-validation-test/profile-registry-function-seams-match-head-4921fbc-ownership"
     "gravity.bootstrap-compatibility.profile-validation-test/profile-validation-facade-preserves-central-diagnostics-and-target-gates"
     "gravity.bootstrap-compatibility.profile-validation-test/profile-captured-original-interposition-is-one-shot"
     "gravity.bootstrap-compatibility.profile-validation-test/profile-leaf-operation-interposition-is-observable-through-facade"]}
   {:namespace 'gravity.bootstrap-compatibility.capability-validation-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/capability_validation_test.clj"
    :selectors
    ["gravity.bootstrap-compatibility.capability-validation-test/capability-facades-preserve-arglists-and-explicit-pass-parity"
     "gravity.bootstrap-compatibility.capability-validation-test/capability-final-authority-narrows-trust-without-rewriting-legacy-row"
     "gravity.bootstrap-compatibility.capability-validation-test/capability-policy-and-provider-seams-remain-interposable"
     "gravity.bootstrap-compatibility.capability-validation-test/capability-diagnostic-policy-scalar-reaches-leaf-pass-contract"
     "gravity.bootstrap-compatibility.capability-validation-test/capability-diagnostics-preserve-source-context-and-stable-ids"
     "gravity.bootstrap-compatibility.capability-validation-test/capability-provider-name-matches-head-4921fbc-reference-table"
     "gravity.bootstrap-compatibility.capability-validation-test/capability-captured-original-provider-interposition-is-one-shot"]}
   {:namespace 'gravity.bootstrap-compatibility.c4-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c4_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c5-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c5_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c6-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c6_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c7-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c7_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c8-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c8_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c9-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c9_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c10-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c10_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c11-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c11_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c12-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c12_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c13-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c13_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c14-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c14_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c15-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c15_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c16-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c16_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c17-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c17_test.clj"}
   {:namespace 'gravity.bootstrap-compatibility.c18-test
    :path "bootstrap/clojure/test/gravity/bootstrap_compatibility/c18_test.clj"}])

(def ^:private default-test-namespace 'gravity.bootstrap-test)
(def ^:private catalog-by-namespace
  (into {} (map (juxt :namespace identity) namespace-catalog)))

(def ^:private usage-text
  (str
   "Usage: clojure -M:<alias> -m gravity.development-test-runner [options]\n"
   "\n"
   "Runs gravity.bootstrap-test by default, or selected static catalog namespaces.\n"
   "With no selector, all tests in the selected namespace set run.\n"
   "\n"
   "Options (repeatable):\n"
   "  --namespace NS    select an allowed static-catalog namespace\n"
   "  --exact NAME      select NAME or a selected namespace/NAME exactly\n"
   "  --regex REGEX     select names matching REGEX\n"
   "  --prefix PREFIX   select names beginning with PREFIX\n"
   "  --list            print selected qualified names without running tests\n"
   "  --catalog         print the static namespace/path catalog without loading tests\n"
   "  --fail-fast       stop after the first test failure or error\n"
   "  --help            print this help without loading tests\n"
   "\n"
   "Qualified selectors resolve only within explicitly selected namespaces.\n"))

(defn- selector-value [option value]
  (let [value (some-> value str/trim)]
    (when (str/blank? value)
      (throw (ex-info (str option " requires a non-empty value")
                      {:type ::invalid-selector :option option})))
    value))

(defn- parse-args [args]
  (loop [remaining (seq args)
         options {:namespaces [] :exact [] :regex [] :prefix []
                  :list? false :catalog? false :fail-fast? false :help? false}]
    (if-not remaining
      options
      (let [argument (first remaining)
            tail (next remaining)]
        (cond
          (= argument "--help") (recur tail (assoc options :help? true))
          (= argument "--list") (recur tail (assoc options :list? true))
          (= argument "--catalog") (recur tail (assoc options :catalog? true))
          (= argument "--fail-fast") (recur tail (assoc options :fail-fast? true))

          (= argument "--namespace")
          (if (seq tail)
            (recur (next tail)
                   (update options :namespaces conj
                           (symbol (selector-value argument (first tail)))))
            (throw (ex-info "--namespace requires a non-empty value"
                            {:type ::invalid-selector :option argument})))

          (or (= argument "--exact") (= argument "-e"))
          (if (seq tail)
            (recur (next tail)
                   (update options :exact conj (selector-value argument (first tail))))
            (throw (ex-info (str argument " requires a non-empty value")
                            {:type ::invalid-selector :option argument})))

          (or (= argument "--regex") (= argument "--pattern") (= argument "-r"))
          (if (seq tail)
            (recur (next tail)
                   (update options :regex conj (selector-value argument (first tail))))
            (throw (ex-info (str argument " requires a non-empty value")
                            {:type ::invalid-selector :option argument})))

          (or (= argument "--prefix") (= argument "-p"))
          (if (seq tail)
            (recur (next tail)
                   (update options :prefix conj (selector-value argument (first tail))))
            (throw (ex-info (str argument " requires a non-empty value")
                            {:type ::invalid-selector :option argument})))

          (= argument "--")
          (recur nil (update options :exact into
                             (map #(selector-value "selector" %) tail)))

          (str/starts-with? argument "-")
          (throw (ex-info (str "unknown option: " argument)
                          {:type ::usage-error :option argument}))

          :else
          (recur tail
                 (update options :exact conj
                         (selector-value "selector" argument))))))))

(defn- selected-namespace-records [{:keys [namespaces]}]
  (let [requested (if (seq namespaces) (set namespaces) #{default-test-namespace})]
    (doseq [namespace requested]
      (when-not (contains? catalog-by-namespace namespace)
        (throw (ex-info (str "unknown test namespace: " namespace)
                        {:type ::unknown-namespace :namespace namespace}))))
    (filterv #(contains? requested (:namespace %)) namespace-catalog)))

(defn- load-selected-namespaces! [namespace-records]
  (doseq [{:keys [namespace]} namespace-records]
    (require namespace)))

(defn- test-var-records [namespace-records]
  (->> namespace-records
       (mapcat
        (fn [{:keys [namespace]}]
          (let [namespace-object (the-ns namespace)]
            (keep (fn [[name test-var]]
                    (when (:test (meta test-var))
                      (let [short-name (str name)]
                        {:name short-name
                         :qualified-name (str namespace "/" short-name)
                         :namespace namespace
                         :namespace-object namespace-object
                         :var test-var})))
                  (ns-interns namespace-object)))))
       (sort-by :qualified-name)
       vec))

(defn- matches-name? [record selector]
  (or (= selector (:name record)) (= selector (:qualified-name record))))

(defn- matches-prefix? [record prefix]
  (or (str/starts-with? (:name record) prefix)
      (str/starts-with? (:qualified-name record) prefix)))

(defn- compile-patterns [patterns]
  (mapv (fn [pattern]
          (try
            (re-pattern pattern)
            (catch java.util.regex.PatternSyntaxException ex
              (throw (ex-info (str "invalid regex selector " (pr-str pattern) ": "
                                  (.getDescription ex))
                              {:type ::invalid-selector :selector pattern} ex)))))
        patterns))

(defn- matches-regex? [record patterns]
  (some #(or (re-find % (:name record))
             (re-find % (:qualified-name record)))
        patterns))

(defn- select-test-vars [records {:keys [exact regex prefix]}]
  (let [compiled-patterns (compile-patterns regex)
        selector-supplied? (or (seq exact) (seq regex) (seq prefix))
        selected (->> records
                      (filter (fn [record]
                                (or (not selector-supplied?)
                                    (some #(matches-name? record %) exact)
                                    (some #(matches-prefix? record %) prefix)
                                    (matches-regex? record compiled-patterns))))
                      vec)]
    (doseq [selector exact]
      (when-not (some #(matches-name? % selector) records)
        (throw (ex-info (str "unknown exact test selector: " selector)
                        {:type ::unknown-selector :selector selector :kind :exact}))))
    (doseq [selector prefix]
      (when-not (some #(matches-prefix? % selector) records)
        (throw (ex-info (str "selector matched no test vars: --prefix " selector)
                        {:type ::unknown-selector :selector selector :kind :prefix}))))
    (doseq [[selector pattern] (map vector regex compiled-patterns)]
      (when-not (some #(or (re-find pattern (:name %))
                           (re-find pattern (:qualified-name %))) records)
        (throw (ex-info (str "selector matched no test vars: --regex " selector)
                        {:type ::unknown-selector :selector selector :kind :regex}))))
    selected))

(defn- report-summary [summary]
  (test/do-report (assoc summary :type :summary))
  summary)

(defn- run-selected-tests [namespace-records selected-records fail-fast?]
  (binding [test/*report-counters* (ref test/*initial-report-counters*)]
    (let [stopped? (atom false)
          selected-by-namespace (group-by :namespace selected-records)]
      (doseq [{:keys [namespace]} namespace-records
              :let [records (get selected-by-namespace namespace)]
              :when (and (seq records) (not @stopped?))]
        (let [namespace-object (the-ns namespace)
              once-fixture-fn (test/join-fixtures
                               (::test/once-fixtures (meta namespace-object)))
              each-fixture-fn (test/join-fixtures
                               (::test/each-fixtures (meta namespace-object)))]
          (test/do-report {:type :begin-test-ns :ns namespace-object})
          (once-fixture-fn
           (fn []
             (doseq [{:keys [var]} records :while (not @stopped?)]
               (each-fixture-fn #(test/test-var var))
               (when (and fail-fast?
                          (pos? (+ (:fail @test/*report-counters*)
                                   (:error @test/*report-counters*))))
                 (reset! stopped? true)))))
          (test/do-report {:type :end-test-ns :ns namespace-object})))
      (report-summary @test/*report-counters*))))

(defn- print-selection [selected-records]
  (doseq [{:keys [qualified-name]} selected-records]
    (println qualified-name))
  (flush))

(defn- print-catalog []
  (doseq [{:keys [namespace path]} namespace-catalog]
    (println (str namespace "\t" path)))
  (flush))

(defn run-cli! [args]
  (let [options (parse-args args)]
    (cond
      (:help? options) (do (print usage-text) 0)
      (:catalog? options) (do (print-catalog) 0)
      :else
      (let [namespace-records (selected-namespace-records options)
            _ (load-selected-namespaces! namespace-records)
            records (test-var-records namespace-records)
            selected-records (select-test-vars records options)]
        (if (:list? options)
          (do (print-selection selected-records) 0)
          (let [summary (run-selected-tests namespace-records selected-records
                                            (:fail-fast? options))]
            (if (and (zero? (:fail summary))
                     (zero? (:error summary))
                     (= (:test summary) (count selected-records)))
              0
              1)))))))

(defn- report-cli-error [^Throwable ex]
  (binding [*out* *err*]
    (println (str "development-test-runner: " (.getMessage ex)))
    (when (= ::usage-error (:type (ex-data ex)))
      (println "Try --help for usage."))
    (flush)))

(defn -main [& args]
  (try
    (System/exit (run-cli! args))
    (catch clojure.lang.ExceptionInfo ex
      (report-cli-error ex)
      (System/exit 2))
    (catch Throwable ex
      (report-cli-error ex)
      (System/exit 2))))
