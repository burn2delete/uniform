(ns ci.progress-test
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :refer [deftest is run-tests]]))

(binding [*command-line-args* nil]
  (load-file "bootstrap/clojure/test/gravity/development_test_runner.clj")
  (load-file "tools/ci/run.clj"))
(require '[gravity.development-test-runner :as runner] '[ci.run :as ci])

(defn- writer [fail?]
  (let [text (StringBuilder.) calls (atom 0)]
    {:text text
     :calls calls
     :writer (proxy [java.io.Writer] []
               (write
                 ([value]
                  (swap! calls inc)
                  (when fail? (throw (java.io.IOException. "progress diagnostic")))
                  (.append text (if (string? value) value (String. ^chars value))))
                 ([value offset length]
                  (swap! calls inc)
                  (when fail? (throw (java.io.IOException. "progress diagnostic")))
                  (.append text (if (string? value)
                                  (subs value offset (+ offset length))
                                  (String. ^chars value (int offset) (int length))))))
               (flush [] (when fail? (throw (java.io.IOException. "progress flush"))))
               (close []))}))

(defn- exercise [{:keys [progress? once-fixture each-fixture body]
                  :or {once-fixture (fn [f] (f))
                       each-fixture (fn [f] (f))
                       body (fn [] nil)}}]
  (let [name 'gravity.progress-fixture-test
        fixture (create-ns name)
        events (atom [])
        output (writer false)
        args (cond-> ["--namespace" (str name)]
               progress? (conj "--progress"))]
    (doseq [symbol '[a b]]
      (let [v (intern fixture symbol nil)]
        (alter-meta! v assoc :test (fn [] (swap! events conj symbol) (body)))))
    (alter-meta! fixture assoc :clojure.test/once-fixtures [once-fixture]
                 :clojure.test/each-fixtures [each-fixture])
    (try
      (let [result (binding [*out* (java.io.StringWriter.)
                             *err* (:writer output)
                             runner/*machine-result-sink* nil]
                     (with-redefs-fn
                       {#'runner/selected-namespace-records
                        (fn [_] [{:namespace name :path "synthetic-only"}])
                        #'clojure.core/require
                        (fn [requested] (assert (= name requested)) (swap! events conj :load))}
                       #(try (runner/run-cli! args)
                             (catch Throwable error error))))]
        {:result result :events @events :output output
         :rows (mapv edn/read-string
                     (remove str/blank? (str/split-lines (str (:text output)))))})
      (finally (remove-ns name)))))

(deftest opt-in-progress-preserves-default-call-sites
  (let [normal (exercise {})
        progress (exercise {:progress? true})]
    (is (= 0 (:result normal) (:result progress)))
    (is (= [:load 'a 'b] (:events normal) (:events progress)))
    (is (empty? (:rows normal)))
    (is (= 12 (count (:rows progress))))
    (is (= #{:namespace-load :namespace-fixture :each-fixture :test-var}
           (set (map :phase (:rows progress)))))
    (is (every? #(= :non-authoritative (:authority %)) (:rows progress)))
    (is (not-any? #(some (set (keys %)) [:argv :environment :message :stack :result :summary])
                  (:rows progress)))))

(deftest disabled-progress-does-not-allocate-or-write
  (let [output (writer false)
        result (with-redefs-fn {#'runner/progress-state
                                (fn [] (throw (AssertionError. "disabled allocation")))}
                 #(binding [*err* (:writer output)]
                    (exercise {})))]
    (is (= 0 (:result result)))
    (is (zero? @(:calls (:output result))))))

(deftest diagnostic-write-failure-is-sticky-after-body
  (let [output (writer true)
        name 'gravity.progress-failure-test
        fixture (create-ns name)
        events (atom [])
        v (intern fixture 'a nil)]
    (alter-meta! v assoc :test (fn [] (swap! events conj :body)))
    (try
      (let [result (binding [*out* (java.io.StringWriter.) *err* (:writer output)]
                     (with-redefs-fn
                       {#'runner/selected-namespace-records
                        (fn [_] [{:namespace name :path "synthetic-only"}])
                        #'clojure.core/require (fn [_] (swap! events conj :load))}
                       #(try (runner/run-cli! ["--namespace" (str name) "--progress"])
                             (catch Throwable error error))))]
        (is (instance? java.io.IOException result))
        (is (= [:load :body] @events)))
      (finally (remove-ns name)))))

(deftest primary-body-error-remains-primary
  (let [original (Exception. "selected body")
        result (exercise {:progress? true
                          :each-fixture (fn [_] (throw original))})]
    (is (identical? original (:result result)))
    (is (= [:load] (:events result)))
    (is (= 6 (count (:rows result))))))

(deftest current-ci-commands-opt-in-all-resolution-children
  (let [macro (first (ci/commands "macro"))
        aggregate (first (ci/commands "resolution"))
        children (mapv #(first (ci/commands %))
                       ["resolution-pipeline" "resolution-unmetered"
                        "resolution-invocation-selection"])]
    (is (= ["macro-regression" 900] (subvec (vec macro) 0 2)))
    (is (= ["resolution-regression" 900] (subvec (vec aggregate) 0 2)))
    (is (every? #(= 1 (count (filter #{"--progress"} (nth % 2))))
                (cons macro (cons aggregate children))))
    (is (= ["resolution-pipeline" "resolution-unmetered"
            "resolution-invocation-selection"]
           (mapv first children)))
    (is (every? #(= 600 (second %)) children))
    (is (not-any? #{"--progress"}
                  (mapcat #(nth (first (ci/commands %)) 2)
                          ["bootstrap" "checked-core-runtime" "static"])))))

(let [result (run-tests)]
  (System/exit (if (zero? (+ (:fail result) (:error result))) 0 1)))
