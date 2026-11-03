(ns ci.progress-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :as test :refer [deftest is run-tests]]))

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

;; Keep the original PR697 fault-injection harness in the current runner port.
;; These tests deliberately exercise diagnostic failures at both progress edges
;; and both Writer operations; the diagnostics are observations and must never
;; replace the selected call's value or exception.
(defn writer-fixture [edge stage error]
  (let [text (StringBuilder.) calls (atom []) last-row (atom nil)
        accept! (fn [s]
                  (let [row (edn/read-string s)]
                    (reset! last-row row)
                    (swap! calls conj [:write (:phase row) (:edge row)])
                    (if (and (= :write stage) (= edge (:edge row)))
                      (do (.append text (subs s 0 (min 5 (count s)))) (throw error))
                      (.append text s))))
        writer (proxy [java.io.Writer] []
                 (write
                   ([value] (accept! (if (string? value) value (String. ^chars value))))
                   ([value offset length]
                    (accept! (if (string? value)
                               (subs value offset (+ offset length))
                               (String. ^chars value (int offset) (int length))))))
                 (flush []
                   (swap! calls conj [:flush (:phase @last-row) (:edge @last-row)])
                   (when (and (= :flush stage) (= edge (:edge @last-row))) (throw error)))
                 (close []))]
    {:writer writer :text text :calls calls}))

(defn- exercise-legacy
  [{:keys [progress? timing? writer once-fixture each-fixture fail-fast?
           fail-first? load-error]
    :or {once-fixture (fn [f] (f)) each-fixture (fn [f] (f))}}]
  (let [name 'gravity.progress-fixture-test
        fixture (create-ns name) events (atom []) records (atom []) timings (atom 0)
        out (java.io.StringWriter.)
        args (cond-> ["--namespace" (str name)]
               progress? (conj "--progress")
               timing? (conj "--timing-receipt" "unused-injected-path")
               fail-fast? (conj "--fail-fast"))]
    (doseq [symbol '[a b]]
      (let [v (intern fixture symbol nil)]
        (alter-meta! v assoc :test
                     (fn [] (swap! events conj symbol)
                       (test/is (not (and fail-first? (= symbol 'a))))))))
    (alter-meta! fixture assoc :clojure.test/once-fixtures [once-fixture]
                 :clojure.test/each-fixtures [each-fixture])
    (try
      (let [result
            (binding [*out* out *err* writer test/*test-out* out
                      runner/*machine-result-sink* #(swap! records conj %)]
              (with-redefs-fn
                {#'runner/selected-namespace-records
                 (fn [_] [{:namespace name :path "synthetic-only"}])
                 #'runner/write-timing-receipt!
                 (fn [& _] (swap! timings inc))
                 #'clojure.core/require
                 (fn [requested]
                   (assert (= name requested))
                   (swap! events conj :load)
                   (when load-error (throw load-error)))}
                #(try (runner/run-cli! args) (catch Throwable error error))))
            interrupted? (Thread/interrupted)]
        {:result result :interrupted? interrupted? :events @events :records @records
         :timings @timings :out (str out)})
      (finally (remove-ns name)))))

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

(deftest composition-preserves-timing-and-single-result-delivery
  (let [name 'gravity.progress-composition-test
        fixture (create-ns name)
        events (atom [])
        directory (.toFile (java.nio.file.Files/createTempDirectory
                            "uniform-progress-composition-"
                            (make-array java.nio.file.attribute.FileAttribute 0)))
        receipt (io/file directory "timing.edn")
        result-file (io/file directory "result.edn")
        test-vars [(str name "/a") (str name "/b")]
        selection {:namespaces [(str name)] :test-vars test-vars :fail-fast? false}
        delivery {:file (.getAbsolutePath result-file)
                  :limit-bytes 100000
                  :request {:id "composition"
                            :source {:kind :synthetic}
                            :selection selection}}]
    (doseq [symbol '[a b]]
      (let [v (intern fixture symbol nil)]
        (alter-meta! v assoc :test (fn [] (swap! events conj symbol)))))
    (try
      (let [exit-code
            (binding [*out* (java.io.StringWriter.)
                      *err* (java.io.StringWriter.)
                      runner/*machine-result-sink* nil]
              (with-redefs-fn
                {#'runner/selected-namespace-records
                 (fn [_] [{:namespace name :path "synthetic-only"}])
                 #'clojure.core/require
                 (fn [requested] (assert (= name requested)) (swap! events conj :load))}
                #(runner/run-cli! ["--namespace" (str name) "--progress"
                                   "--timing-receipt" (.getAbsolutePath receipt)
                                   "--machine-result" (pr-str delivery)])))]
        (is (= 0 exit-code))
        (is (= [:load 'a 'b] @events))
        (is (.isFile receipt))
        (is (.isFile result-file))
        (let [timing (edn/read-string (slurp receipt))
              delivered (edn/read-string (slurp result-file))]
          (is (= selection (:selection timing)))
          (is (= :passed (get-in timing [:outcome])))
          (is (= :passed (get-in delivered [:result :outcome])))
          (is (= selection (get-in delivered [:request :selection])))))
      (finally
        (remove-ns name)
        (doseq [file (reverse (file-seq directory))]
          (.delete file))))))

(deftest legacy-write-and-flush-failure-retains-body-before-timing-and-completion
  (doseq [timing? [false true] edge [:enter :return] stage [:write :flush]]
    (let [original (java.io.IOException. "diagnostic")
          sink (writer-fixture edge stage original)
          r (exercise-legacy {:progress? true :timing? timing? :writer (:writer sink)})]
      (is (identical? original (:result r)))
      (is (= [:load 'a 'b] (:events r)))
      (is (empty? (:records r)))
      (is (zero? (:timings r)))
      (when (= stage :write)
        (is (not-any? #(and (= :flush (first %)) (= edge (last %)))
                      @(:calls sink))))))
  (let [sink (writer-fixture :enter :write (java.io.IOException.))
        r (exercise-legacy {:writer (:writer sink)})]
    (is (= 0 (:result r)))
    (is (empty? @(:calls sink)))))

(deftest legacy-fixture-catch-zero-repeats-and-timing-preserve-count-guard
  (doseq [timing? [false true] repeats [0 1 2]]
    (let [fixture (fn [f] (dotimes [_ repeats] (try (f) (catch Throwable _ nil))))
          sink (writer-fixture nil nil nil)
          r (exercise-legacy {:progress? true :timing? timing?
                              :writer (:writer sink) :once-fixture fixture})]
      (is (= (if (= repeats 1) 0 1) (:result r)))
      (is (= (* 2 repeats) (get-in r [:records 0 :summary :test])))
      (is (= (if (= repeats 1) :passed :failed)
             (get-in r [:records 0 :outcome]))))
    (let [original (Exception. "sticky")
          sink (writer-fixture :return :flush original)
          r (exercise-legacy {:progress? true :timing? timing?
                              :writer (:writer sink)
                              :each-fixture
                              (fn [f]
                                (dotimes [_ repeats]
                                  (try (f) (catch Throwable _ nil))))})]
      (is (identical? original (:result r)))
      (is (empty? (:records r)))
      (is (zero? (:timings r))))))

(deftest legacy-operation-fatal-interrupt-and-hostile-inspection-errors
  (doseq [original [(Exception. "operation")
                    (LinkageError. "operation")
                    (InterruptedException. "operation")]]
    (let [secondary (java.io.IOException. "diagnostic")
          sink (writer-fixture :throw :flush secondary)
          r (exercise-legacy
             {:progress? true :writer (:writer sink)
              :each-fixture
              (fn [f]
                (try (f) (throw original)
                     (finally (Thread/interrupted))))})]
      (is (identical? original (:result r)))
      (is (some #(identical? secondary %)
                (.getSuppressed ^Throwable original)))
      (is (= (instance? InterruptedException original) (:interrupted? r)))
      (is (empty? (:records r)))))
  (let [accessor (Exception. "accessor") calls (atom 0)
        original (proxy [Exception] []
                   (getCause [] (swap! calls inc) (throw accessor))
                   (equals [_] (throw (AssertionError. "equals")))
                   (hashCode [] (throw (AssertionError. "hash")))
                   (toString [] (throw (AssertionError. "render"))))
        sink (writer-fixture :enter :write original)
        r (exercise-legacy {:progress? true :writer (:writer sink)})]
    (is (identical? original (:result r)))
    (is (= 1 @calls))
    (is (some #(identical? accessor %)
              (.getSuppressed ^Throwable original)))
    (is (= [:load 'a 'b] (:events r))))
  (let [original (InterruptedException.)
        internal (IllegalStateException.)
        sink (writer-fixture nil nil nil)
        r (with-redefs-fn {#'runner/inspect-progress-error
                           (fn [_] (throw internal))}
            #(exercise-legacy
              {:progress? true :writer (:writer sink)
               :each-fixture
               (fn [_]
                 (try (throw original)
                      (finally (Thread/interrupted))))}))]
    (is (identical? original (:result r)))
    (is (:interrupted? r))
    (is (some #(identical? internal %)
              (.getSuppressed ^Throwable original)))))

(deftest legacy-print-writer-hidden-errors-format-errors-and-fail-fast
  (let [sink (writer-fixture :enter :write (java.io.IOException. "hidden"))
        r (exercise-legacy
           {:progress? true
            :writer (java.io.PrintWriter. ^java.io.Writer (:writer sink))})]
    (is (= "DEV-PROGRESS-WRITE" (:id (ex-data (:result r)))))
    (is (= :unavailable (:original-IO-error (ex-data (:result r)))))
    (is (= [:load 'a 'b] (:events r)))
    (is (empty? (:records r))))
  (let [original-pr pr-str
        error (Exception. "format")
        sink (writer-fixture nil nil nil)
        r (with-redefs [pr-str
                        (fn [& args]
                          (if (= :gravity/development-test-progress-v1
                                 (:schema (first args)))
                            (throw error)
                            (apply original-pr args)))]
            (exercise-legacy {:progress? true :writer (:writer sink)}))]
    (is (identical? error (:result r)))
    (is (= [:load 'a 'b] (:events r)))
    (is (empty? (:records r))))
  (doseq [progress? [false true]]
    (let [sink (writer-fixture nil nil nil)
          r (exercise-legacy {:progress? progress? :fail-fast? true
                              :fail-first? true :writer (:writer sink)})]
      (is (= 1 (:result r)))
      (is (= [:load 'a] (:events r)))
      (is (= 1 (get-in r [:records 0 :summary :fail]))))))

(deftest legacy-disabled-allocation-load-errors-and-diagnostic-interruption
  (let [sink (writer-fixture nil nil nil)
        r (with-redefs-fn {#'runner/progress-state
                           (fn [] (throw (AssertionError. "disabled allocation")))}
            #(exercise-legacy {:writer (:writer sink)}))]
    (is (= 0 (:result r)))
    (is (empty? @(:calls sink))))
  (doseq [timing? [false true] progress? [false true]]
    (let [original (Exception. "load")
          sink (writer-fixture nil nil nil)
          r (exercise-legacy {:progress? progress? :timing? timing?
                              :writer (:writer sink) :load-error original})]
      (is (identical? original (:result r)))
      (is (= [:load] (:events r)))
      (is (empty? (:records r)))
      (is (zero? (:timings r)))))
  (doseq [original [(LinkageError. "diagnostic")
                    (InterruptedException. "diagnostic")]]
    (let [sink (writer-fixture :enter :flush original)
          r (exercise-legacy
             {:progress? true :writer (:writer sink)
              :each-fixture
              (fn [f]
                (try (f) (finally (Thread/interrupted))))})]
      (is (identical? original (:result r)))
      (is (= [:load 'a 'b] (:events r)))
      (is (= (instance? InterruptedException original) (:interrupted? r)))
      (is (empty? (:records r))))))

(deftest legacy-distinct-equal-errors-and-internal-observation-failure
  (let [callbacks (atom 0)
        diagnostic (fn []
                    (proxy [Exception] []
                      (equals [_] (swap! callbacks inc) true)
                      (hashCode [] (swap! callbacks inc) 0)
                      (toString [] (swap! callbacks inc) "must not render")))
        first-error (diagnostic)
        second-error (diagnostic)
        entry (writer-fixture :enter :write first-error)
        returns (writer-fixture :return :flush second-error)
        row (atom nil)
        writer (proxy [java.io.Writer] []
                 (write [value]
                   (reset! row (edn/read-string value))
                   (.write ^java.io.Writer
                           (if (= :enter (:edge @row)) (:writer entry) (:writer returns))
                           ^String value))
                 (flush []
                   (.flush ^java.io.Writer
                           (if (= :enter (:edge @row)) (:writer entry) (:writer returns))))
                 (close []))
        original (Exception. "operation")
        r (exercise-legacy {:progress? true :writer writer
                            :each-fixture (fn [_] (throw original))})]
    (is (identical? original (:result r)))
    (is (= 2 (count (.getSuppressed original))))
    (is (identical? first-error (first (.getSuppressed original))))
    (is (identical? second-error (second (.getSuppressed original))))
    (is (zero? @callbacks))))

(let [result (run-tests)]
  (System/exit (if (zero? (+ (:fail result) (:error result))) 0 1)))
