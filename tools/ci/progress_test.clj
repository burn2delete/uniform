(ns ci.progress-test
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :as test :refer [deftest is run-tests]]))

(binding [*command-line-args* nil]
  (load-file "bootstrap/clojure/test/gravity/development_test_runner.clj")
  (load-file "tools/ci/run.clj"))
(require '[gravity.development-test-runner :as runner] '[ci.run :as ci])

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

(defn exercise [{:keys [progress? timing? writer once-fixture each-fixture fail-fast? fail-first? load-error]
                 :or {once-fixture (fn [f] (f)) each-fixture (fn [f] (f))}}]
  (let [name 'gravity.progress-fixture-test
        fixture (create-ns name) events (atom []) records (atom []) timings (atom 0)
        out (java.io.StringWriter.)
        args (cond-> ["--namespace" (str name)]
               progress? (conj "--progress") timing? (conj "--timing-receipt" "unused-injected-path")
               fail-fast? (conj "--fail-fast"))]
    (doseq [symbol '[a b]]
      (let [v (intern fixture symbol nil)]
        (alter-meta! v assoc :test
                     (fn [] (swap! events conj symbol)
                       (test/is (not (and fail-first? (= symbol 'a))))))))
    (alter-meta! fixture assoc :clojure.test/once-fixtures [once-fixture] :clojure.test/each-fixtures [each-fixture])
    (try
      (let [result
            (binding [*out* out *err* writer test/*test-out* out runner/*machine-result-sink* #(swap! records conj %)]
              (with-redefs-fn {#'runner/selected-namespace-records (fn [_] [{:namespace name :path "synthetic-only"}])
                               #'runner/write-timing-receipt! (fn [& _] (swap! timings inc))
                               #'clojure.core/require (fn [requested]
                                                        (assert (= name requested)) (swap! events conj :load)
                                                        (when load-error (throw load-error)))}
                #(try (runner/run-cli! args) (catch Throwable error error))))
            interrupted? (Thread/interrupted)]
        {:result result :interrupted? interrupted? :events @events :records @records
         :timings @timings :out (str out)})
      (finally (remove-ns name)))))

(deftest actual-call-sites-format-defaults-and-command-selection
  (let [sink (writer-fixture nil nil nil)
        normal (exercise {:writer (:writer sink)})
        progress (exercise {:progress? true :writer (:writer sink)})
        rows (mapv edn/read-string (str/split-lines (str (:text sink))))]
    (is (= 0 (:result normal) (:result progress)))
    (is (= [:load 'a 'b] (:events normal) (:events progress)))
    (is (= (:records normal) (:records progress)))
    (is (= 12 (count rows)))
    (is (= #{:namespace-load :namespace-fixture :each-fixture :test-var} (set (map :phase rows))))
    (is (every? #(= :non-authoritative (:authority %)) rows))
    (is (not-any? #(some (set (keys %)) [:argv :environment :message :stack :result :summary]) rows))
    (is (not (str/includes? (:out progress) "development-test-progress")))
    (is (= :passed (:outcome (first (:records progress))))))
  (let [[[id seconds argv]] (ci/commands "macro")]
    (is (= ["macro-regression" 600] [id seconds]))
    (is (= "-J-Xmx2g" (second argv)))
    (is (= ["gravity.macro.pipeline-test" "gravity.bootstrap-compatibility.c4-test"]
           (mapv second (filter #(= "--namespace" (first %)) (partition 2 1 argv)))))
    (is (= 1 (count (filter #{"--progress"} argv))))))

(deftest resolution-optin-preserves-exact-selection-and-command-bounds
  (let [[[id seconds argv]] (ci/commands "resolution")
        selectors ["--namespace" "gravity.resolution.pipeline-test"
                   "--namespace" "gravity.self-hosting.sh06-unmetered-resolution-test"
                   "--namespace" "gravity.self-hosting.sh06-invocation-selection-test"]
        legacy (apply #'ci/command "2g" "-M:dev-test" selectors)
        options (#'runner/parse-args (drop (inc (.indexOf ^java.util.List argv "-M:dev-test")) argv))
        original-options (#'runner/parse-args selectors)]
    (is (= ["resolution-regression" 600] [id seconds]))
    (is (= "-J-Xmx2g" (second argv)))
    (is (= 1 (count (filter #{"--progress"} argv))))
    (is (= legacy (vec (remove #{"--progress"} argv))))
    ;; Identical selector options and unchanged selection code retain the
    ;; selected Var set/order without loading any selected namespace here.
    (is (= (assoc original-options :progress? true) options))
    (doseq [mode ["bootstrap" "checked-core-runtime" "leaves-portable"
                  "leaves-darwin" "darwin-unsupported" "darwin-probe"]]
      (is (not-any? #{"--progress"} (mapcat #(nth % 2) (ci/commands mode)))))
    (let [writer (writer-fixture nil nil nil)
          before (exercise {})
          observed (exercise {:progress? true :writer (:writer writer)})]
      (is (= (:result before) (:result observed)))
      (is (= (:events before) (:events observed)))
      (is (= (:records before) (:records observed)))
      (is (str/includes? (str (:text writer)) ":non-authoritative")))))

(deftest write-and-flush-failure-retains-body-before-timing-and-completion
  (doseq [timing? [false true] edge [:enter :return] stage [:write :flush]]
    (let [original (java.io.IOException. "diagnostic") sink (writer-fixture edge stage original)
          r (exercise {:progress? true :timing? timing? :writer (:writer sink)})]
      (is (identical? original (:result r)))
      (is (= [:load 'a 'b] (:events r)))
      (is (empty? (:records r))) (is (zero? (:timings r)))
      (when (= stage :write)
        (is (not-any? #(and (= :flush (first %)) (= edge (last %))) @(:calls sink))))))
  (let [sink (writer-fixture :enter :write (java.io.IOException.))
        r (exercise {:writer (:writer sink)})]
    (is (= 0 (:result r))) (is (empty? @(:calls sink)))))

(deftest fixture-catch-zero-repeats-and-both-timing-branches-preserve-count-guard
  (doseq [timing? [false true] repeats [0 1 2]]
    (let [fixture (fn [f] (dotimes [_ repeats] (try (f) (catch Throwable _ nil))))
          sink (writer-fixture nil nil nil)
          r (exercise {:progress? true :timing? timing? :writer (:writer sink) :once-fixture fixture})]
      (is (= (if (= repeats 1) 0 1) (:result r)))
      (is (= (* 2 repeats) (get-in r [:records 0 :summary :test])))
      (is (= (if (= repeats 1) :passed :failed) (get-in r [:records 0 :outcome]))))
    (let [original (Exception. "sticky") sink (writer-fixture :return :flush original)
          r (exercise {:progress? true :timing? timing? :writer (:writer sink)
                       :each-fixture (fn [f] (dotimes [_ repeats] (try (f) (catch Throwable _ nil))))})]
      (is (identical? original (:result r))) (is (empty? (:records r))) (is (zero? (:timings r))))))

(deftest raw-operation-fatal-interrupt-and-hostile-inspection-errors
  (doseq [original [(Exception. "operation") (LinkageError. "operation") (InterruptedException. "operation")]]
    (let [secondary (java.io.IOException. "diagnostic") sink (writer-fixture :throw :flush secondary)
          r (exercise {:progress? true :writer (:writer sink)
                       :each-fixture (fn [f] (try (f) (throw original) (finally (Thread/interrupted))))})]
      (is (identical? original (:result r)))
      (is (some #(identical? secondary %) (.getSuppressed ^Throwable original)))
      (is (= (instance? InterruptedException original) (:interrupted? r)))
      (is (empty? (:records r)))))
  (let [accessor (Exception. "accessor") calls (atom 0)
        original (proxy [Exception] []
                   (getCause [] (swap! calls inc) (throw accessor))
                   (equals [_] (throw (AssertionError. "equals")))
                   (hashCode [] (throw (AssertionError. "hash")))
                   (toString [] (throw (AssertionError. "render"))))
        sink (writer-fixture :enter :write original)
        r (exercise {:progress? true :writer (:writer sink)})]
    (is (identical? original (:result r))) (is (= 1 @calls))
    (is (some #(identical? accessor %) (.getSuppressed ^Throwable original)))
    (is (= [:load 'a 'b] (:events r))))
  (let [original (InterruptedException.) internal (IllegalStateException.) sink (writer-fixture nil nil nil)
        r (with-redefs-fn {#'runner/inspect-progress-error (fn [_] (throw internal))}
            #(exercise {:progress? true :writer (:writer sink)
                        :each-fixture (fn [_] (try (throw original) (finally (Thread/interrupted))))}))]
    (is (identical? original (:result r))) (is (:interrupted? r))
    (is (some #(identical? internal %) (.getSuppressed ^Throwable original)))))

(deftest actual-PrintWriter-hidden-errors-format-errors-and-failfast
  (let [sink (writer-fixture :enter :write (java.io.IOException. "hidden"))
        r (exercise {:progress? true :writer (java.io.PrintWriter. ^java.io.Writer (:writer sink))})]
    (is (= "DEV-PROGRESS-WRITE" (:id (ex-data (:result r)))))
    (is (= :unavailable (:original-IO-error (ex-data (:result r)))))
    (is (= [:load 'a 'b] (:events r))) (is (empty? (:records r))))
  (let [original-pr pr-str error (Exception. "format") sink (writer-fixture nil nil nil)
        r (with-redefs [pr-str (fn [& args]
                                 (if (= :gravity/development-test-progress-v1 (:schema (first args)))
                                   (throw error) (apply original-pr args)))]
            (exercise {:progress? true :writer (:writer sink)}))]
    (is (identical? error (:result r))) (is (= [:load 'a 'b] (:events r)))
    (is (empty? (:records r))))
  (doseq [progress? [false true]]
    (let [sink (writer-fixture nil nil nil)
          r (exercise {:progress? progress? :fail-fast? true :fail-first? true :writer (:writer sink)})]
      (is (= 1 (:result r))) (is (= [:load 'a] (:events r)))
      (is (= 1 (get-in r [:records 0 :summary :fail]))))))

(deftest disabled-allocation-load-errors-and-diagnostic-fatal-interruption
  (let [sink (writer-fixture nil nil nil)
        r (with-redefs-fn {#'runner/progress-state (fn [] (throw (AssertionError. "disabled allocation")))}
            #(exercise {:writer (:writer sink)}))]
    (is (= 0 (:result r))) (is (empty? @(:calls sink))))
  (doseq [timing? [false true] progress? [false true]]
    (let [original (Exception. "load") sink (writer-fixture nil nil nil)
          r (exercise {:progress? progress? :timing? timing? :writer (:writer sink) :load-error original})]
      (is (identical? original (:result r))) (is (= [:load] (:events r)))
      (is (empty? (:records r))) (is (zero? (:timings r)))))
  (doseq [original [(LinkageError. "diagnostic") (InterruptedException. "diagnostic")]]
    (let [sink (writer-fixture :enter :flush original)
          r (exercise {:progress? true :writer (:writer sink)
                       :each-fixture (fn [f] (try (f) (finally (Thread/interrupted))))})]
      (is (identical? original (:result r))) (is (= [:load 'a 'b] (:events r)))
      (is (= (instance? InterruptedException original) (:interrupted? r)))
      (is (empty? (:records r))))))

(deftest distinct-equal-errors-and-observed-root-internal-failure
  (let [callbacks (atom 0)
        diagnostic (fn [] (proxy [Exception] []
                            (equals [_] (swap! callbacks inc) true)
                            (hashCode [] (swap! callbacks inc) 0)
                            (toString [] (swap! callbacks inc) "must not render")))
        first-error (diagnostic) second-error (diagnostic)
        entry (writer-fixture :enter :write first-error)
        returns (writer-fixture :return :flush second-error)
        row (atom nil)
        writer (proxy [java.io.Writer] []
                 (write [value]
                   (reset! row (edn/read-string value))
                   (if (= :enter (:edge @row))
                     (.write ^java.io.Writer (:writer entry) ^String value)
                     (.write ^java.io.Writer (:writer returns) ^String value)))
                 (flush []
                   (if (= :enter (:edge @row))
                     (.flush ^java.io.Writer (:writer entry))
                     (.flush ^java.io.Writer (:writer returns))))
                 (close []))
        original (Exception. "operation")
        r (exercise {:progress? true :writer writer :each-fixture (fn [_] (throw original))})]
    (is (identical? original (:result r)))
    (is (= 2 (count (.getSuppressed original))))
    (is (identical? first-error (first (.getSuppressed original))))
    (is (identical? second-error (second (.getSuppressed original))))
    (is (zero? @callbacks))))

(assert (nil? (find-ns 'gravity.bootstrap)))
(let [result (run-tests)]
  (assert (nil? (find-ns 'gravity.bootstrap)))
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
