(ns gravity.self-hosting.sh01-stage0-leaf-test-runner-test
  "Contract tests for the bootstrap-free Stage 0 leaf catalog and CLI.

  Running all 44 leaf namespaces remains the :leaf-test command's aggregate
  acceptance check; this namespace also exercises its adversarial boundaries."
  (:require [clojure.string :as str]
            [clojure.test :as test]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap-free-leaf-test-runner :as runner])
  (:import [java.lang ProcessHandle]
           [java.nio.file Files Path]
           [java.nio.file.attribute FileAttribute]))

(def expected-foundation-reader-stems
  #{"digest"
    "module_analysis"
    "reader_cursor"
    "reader_diagnostic_policy"
    "reader_host_oracle"
    "reader_namespace"
    "source_unit"
    "syntax_object_stream"
    "syntax_origin"})

(defn- exception-data
  [f]
  (try
    (f)
    nil
    (catch clojure.lang.ExceptionInfo exception
      (ex-data exception))))

(defn- stem
  [entry]
  (-> (:test-path entry)
      (str/split #"/")
      last
      (str/replace #"_test\.clj$" "")))

(defn- private-runner-var
  [name]
  (or (ns-resolve 'gravity.bootstrap-free-leaf-test-runner name)
      (throw (ex-info "missing private runner var" {:name name}))))

(defn- valid-child-summary
  [entry counts]
  {:schema runner/summary-schema
   :authority :non-authoritative
   :authoritative? false
   :catalog-count (count runner/catalog)
   :catalog-hash runner/catalog-hash
   :selected-count 1
   :selected [(:namespace entry)]
   :selected-hash (runner/catalog-hash-of [entry])
   :fail-fast? false
   :counts counts
   :test (:test counts)
   :pass (:pass counts)
   :fail (:fail counts)
   :error (:error counts)
   :elapsed-ms 1})

(defn- run-synthetic-namespace
  [test-functions {:keys [fail-fast? hook fixtures]
                   :or {fail-fast? false}}]
  (let [namespace-symbol (symbol (str "gravity.synthetic-leaf-" (gensym)))
        namespace-object (create-ns namespace-symbol)]
    (try
      (doseq [[index test-function] (map-indexed vector test-functions)]
        (let [test-var (intern namespace-object
                               (symbol (str "test-" index))
                               (fn []))]
          (alter-meta! test-var assoc :test test-function)))
      (when fixtures
        (alter-meta! namespace-object merge fixtures))
      (when hook
        (intern namespace-object 'test-ns-hook #(hook namespace-object)))
      (binding [test/*report-counters* (ref test/*initial-report-counters*)]
        ((var-get (private-runner-var 'run-namespace-tests!))
         namespace-symbol fail-fast?)
        @test/*report-counters*)
      (finally
        (remove-ns namespace-symbol)))))

(deftest catalog-is-exactly-the-owned-bootstrap-free-top-level-surface
  (is (nil? (find-ns 'gravity.bootstrap)))
  (is (= runner/expected-catalog-count (count runner/catalog)))
  (is (= (set (runner/owned-top-level-test-paths))
         (set (map :test-path runner/catalog))))
  (is (= runner/catalog
         (vec (sort-by (comp str :namespace) runner/catalog))))
  (is (true? (runner/validate-catalog!)))
  (is (= runner/catalog-hash
         (runner/catalog-hash-of runner/catalog))))

(deftest catalog-identities-and-paths-are-unique
  (doseq [field [:id :namespace :source-path :test-path]]
    (let [values (mapv field runner/catalog)]
      (is (= (count values) (count (distinct values))) field)))
  (is (every? symbol? (map :namespace runner/catalog)))
  (is (every? #(str/ends-with? (:test-path %) "_test.clj") runner/catalog))
  (is (every? #(str/ends-with? (:source-path %) ".clj") runner/catalog))
  (is (= ["-J--enable-native-access=ALL-UNNAMED"]
         (:jvm-options (some #(when (= "darwin-publication" (:id %)) %)
                             runner/catalog))))
  (is (= []
         (:jvm-options (some #(when (= "digest" (:id %)) %)
                             runner/catalog)))))

(deftest catalog-matches-normative-component-contract
  (let [contract (runner/read-strict-component-contract)
        expected (runner/component-contract-leaf-tuples contract)
        actual (mapv #(select-keys % [:id :source-path :test-path
                                     :namespace :group])
                     runner/catalog)]
    (is (= "gravity/stage0-clojure-components-v1"
           (get contract "schema")))
    (is (= 44 (count expected)))
    (is (= expected actual))
    (is (= {:foundation-reader 9 :c2-c3 12 :compiler 23}
           (frequencies (map :group expected))))))

(deftest component-contract-json-is-strict
  (doseq [text ["{\"a\": 1, \"a\": 2}"
                "{\"a\": 1} trailing"
                "{\"a\": NaN}"]]
    (let [data (exception-data #(runner/read-strict-component-contract text))]
      (is (= "S0LEAF-COMPONENT-CONTRACT-JSON" (:id data)) text))))

(deftest component-contract-execution-group-is-distinct-from-semantic-group
  (let [contract (runner/read-strict-component-contract)
        by-id (into {} (map (juxt #(get % "id") identity)
                            (get contract "components")))]
    (is (= ["compatibility-support" "foundation-reader"]
           (mapv #(get (get by-id "digest") %)
                 ["stage0_group" "leaf_execution_group"])))
    (is (= ["compiler" "foundation-reader"]
           (mapv #(get (get by-id "syntax-origin") %)
                 ["stage0_group" "leaf_execution_group"])))
    (is (= ["compatibility-support" "compiler"]
           (mapv #(get (get by-id "darwin-publication") %)
                 ["stage0_group" "leaf_execution_group"])))
    (doseq [component (get contract "components")]
      (is (= (= "bootstrap-free" (get-in component ["test" "lane"]))
             (some? (get component "leaf_execution_group")))
          (get component "id")))))

(deftest group-semantics-are-explicit
  (doseq [entry runner/catalog
          :let [name (stem entry)]]
    (cond
      (or (str/starts-with? name "c2_")
          (str/starts-with? name "c3_"))
      (is (= :c2-c3 (:group entry)) name)

      (contains? expected-foundation-reader-stems name)
      (is (= :foundation-reader (:group entry)) name)

      :else
      (is (= :compiler (:group entry)) name)))
  (is (= #{:foundation-reader :c2-c3 :compiler}
         (set (map :group runner/catalog)))))

(deftest cli-selects-canonical-subsets-without-loading-bootstrap
  (testing "namespace and exact selectors select one entry"
    (let [namespace "gravity.digest-test"
          by-namespace (runner/select-entries
                        (runner/parse-args ["--namespace" namespace]))
          by-id (runner/select-entries
                 (runner/parse-args ["--exact" "digest"]))]
      (is (= [namespace] (mapv (comp str :namespace) by-namespace)))
      (is (= by-namespace by-id))))
  (testing "groups retain catalog order"
    (let [selected (runner/select-entries
                    (runner/parse-args ["--group" "c2-c3"]))]
      (is (= 12 (count selected)))
      (is (= selected (vec (sort-by (comp str :namespace) selected))))
      (is (every? #(= :c2-c3 (:group %)) selected))))
  (testing "listing does not run selected tests"
    (let [output (with-out-str
                   (is (= 0 (runner/run-cli!
                             ["--group" "c2-c3" "--list"]))))
          lines (->> (str/split-lines output)
                     (remove str/blank?)
                     vec)]
      (is (= 12 (count lines)))
      (is (= lines (vec (sort lines))))))
  (is (nil? (find-ns 'gravity.bootstrap))))

(deftest cli-rejects-unknown-duplicate-and-unsupported-selection
  (let [unknown-namespace
        (exception-data
         #(runner/select-entries
           (runner/parse-args ["--namespace" "gravity.missing-test"])))
        duplicate
        (exception-data
         #(runner/parse-args ["--group" "compiler" "--group" "compiler"]))
        unsupported
        (exception-data #(runner/parse-args ["--regex" "digest"]))
        unknown-exact
        (exception-data
         #(runner/select-entries
           (runner/parse-args ["--exact" "missing"]))) ]
    (testing "unknown namespace"
      (is (= :namespace (:kind unknown-namespace))))
    (testing "duplicate selector"
      (is (= "S0LEAF-CLI-USAGE" (:id duplicate))))
    (testing "unsupported option"
      (is (= "S0LEAF-CLI-USAGE" (:id unsupported))))
    (testing "unknown exact values fail closed"
      (is (= :exact (:kind unknown-exact))))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest aggregate-runner-keeps-child-order-and-fail-fast-boundary
  (let [entries (vec (take 2 runner/catalog))
        calls (atom [])
        fake-child
        (fn [entry _options]
          (swap! calls conj (:namespace entry))
          {:entry entry
           :namespace (:namespace entry)
           :status :passed
           :exit-code 0
           :summary {:counts {:test 2 :pass 4 :fail 0 :error 0}}
           :elapsed-ms 3})
        summary (binding [runner/*child-executor* fake-child]
                  (runner/run-selected-tests! entries))]
    (is (= (mapv :namespace entries) @calls))
    (is (= 2 (:executed-count summary)))
    (is (= 4 (get-in summary [:counts :test])))
    (is (= 8 (get-in summary [:counts :pass])))
    (is (= :passed (:status summary)))
    (is (= 0 (:exit-code summary))))
  (let [entries (vec (take 2 runner/catalog))
        calls (atom 0)
        fake-child
        (fn [entry options]
          (swap! calls inc)
          (is (true? (:fail-fast? options)))
          {:entry entry
           :namespace (:namespace entry)
           :status :failed
           :exit-code 1
           :summary {:counts {:test 1 :pass 0 :fail 1 :error 0}}
           :elapsed-ms 1})
        summary (binding [runner/*child-executor* fake-child]
                  (runner/run-selected-tests!
                   entries {:fail-fast? true}))]
    (is (= 1 @calls))
    (is (= 1 (:executed-count summary)))
    (is (= 1 (:skipped-count summary)))
    (is (= :failed (:status summary)))
    (is (= 1 (:exit-code summary)))))

(deftest child-command-forwards-fail-fast-after-the-exact-namespace
  (let [entry (first runner/catalog)
        command (runner/child-command entry {:fail-fast? true})]
    (is (= ["--run-one" (str (:namespace entry)) "--fail-fast"]
           (subvec command (- (count command) 3))))))

(deftest clojure-test-counts-each-test-var-exactly-once
  (let [fixture-events (atom [])
        counters
        (run-synthetic-namespace
         [(fn [] (test/is true))
          (fn [] (test/is (= 2 (+ 1 1))))]
         {:fixtures
          {::test/once-fixtures
           [(fn [body]
              (swap! fixture-events conj :once-before)
              (body)
              (swap! fixture-events conj :once-after))]
           ::test/each-fixtures
           [(fn [body]
              (swap! fixture-events conj :each-before)
              (body)
              (swap! fixture-events conj :each-after))]}})]
    (is (= 2 (:test counters)))
    (is (= 2 (:pass counters)))
    (is (= 0 (:fail counters)))
    (is (= 0 (:error counters)))
    (is (= [:once-before :each-before :each-after
            :each-before :each-after :once-after]
           @fixture-events))))

(deftest fail-fast-stops-later-vars-inside-one-child-namespace
  (let [executed (atom [])
        counters
        (run-synthetic-namespace
         [(fn []
            (swap! executed conj :first)
            (test/is false "intentional fail-fast sentinel"))
          (fn []
            (swap! executed conj :second)
            (test/is true))]
         {:fail-fast? true
          :hook (fn [namespace-object]
                  (test/test-vars
                   (sort-by #(str (:name (meta %)))
                            (filter #(:test (meta %))
                                    (vals (ns-interns namespace-object))))))})]
    (is (= [:first] @executed))
    (is (= 1 (:test counters)))
    (is (= 1 (:fail counters)))))

(deftest test-ns-hook-is-invoked-through-normal-test-var-semantics
  (let [hook-called? (atom false)
        test-called? (atom false)
        counters
        (run-synthetic-namespace
         [(fn []
            (reset! test-called? true)
            (test/is true))]
         {:hook (fn [namespace-object]
                  (reset! hook-called? true)
                  (test/test-vars
                   (filter #(:test (meta %))
                           (vals (ns-interns namespace-object)))))})]
    (is @hook-called?)
    (is @test-called?)
    (is (= 1 (:test counters)))
    (is (= 1 (:pass counters)))))

(deftest child-summary-parser-ignores-prior-clojure-test-output
  (let [entry (first runner/catalog)
        summary (valid-child-summary
                 entry {:test 1 :pass 1 :fail 0 :error 0})
        stdout (str "\nTesting gravity.example-test\nnoise {not-edn}\n"
                    "GRAVITY_LEAF_SUMMARY " (pr-str summary) "\n")]
    (is (= summary (runner/parse-child-summary stdout entry 0)))
    (is (= summary (runner/parse-child-summary stdout)))
    (is (nil? (runner/parse-child-summary
               "Testing gravity.example-test\n{:schema :wrong}")))))

(deftest child-summary-parser-rejects-incomplete-forged-and-inconsistent-edn
  (let [entry (first runner/catalog)
        counts {:test 1 :pass 1 :fail 0 :error 0}
        valid (valid-child-summary entry counts)
        line #(str "GRAVITY_LEAF_SUMMARY " (pr-str %) "\n")]
    (testing "a schema-only map cannot turn exit zero into success"
      (is (nil? (runner/parse-child-summary
                 (line {:schema runner/summary-schema}) entry 0))))
    (testing "trailing EDN cannot hide behind an otherwise valid map"
      (is (nil? (runner/parse-child-summary
                 (str (str/trim-newline (line valid)) " {:forged true}\n")
                 entry 0))))
    (doseq [[label forged]
            [[:authority (assoc valid :authority :authoritative)]
             [:authoritative (assoc valid :authoritative? true)]
             [:catalog-count (assoc valid :catalog-count 1)]
             [:catalog-hash (assoc valid :catalog-hash "sha256:forged")]
             [:selected (assoc valid :selected ['gravity.forged-test])]
             [:selected-count (assoc valid :selected-count 2)]
             [:selected-hash (assoc valid :selected-hash "sha256:forged")]
             [:negative-count (assoc-in valid [:counts :pass] -1)]
             [:noninteger-count (assoc-in valid [:counts :pass] 1.0)]
             [:extra-count (assoc-in valid [:counts :skip] 1)]
             [:top-level-count (assoc valid :pass 2)]]]
      (is (nil? (runner/parse-child-summary (line forged) entry 0)) label))
    (testing "process exit and reported failures must agree"
      (is (nil? (runner/parse-child-summary (line valid) entry 1)))
      (let [failed (valid-child-summary
                    entry {:test 1 :pass 0 :fail 1 :error 0})]
        (is (nil? (runner/parse-child-summary (line failed) entry 0)))
        (is (= failed (runner/parse-child-summary (line failed) entry 1)))))))

(deftest aggregate-preserves-child-failure-diagnostics
  (let [entry (first runner/catalog)
        result {:entry entry
                :namespace (:namespace entry)
                :status :failed
                :exit-code 1
                :summary nil
                :stdout "assertion output"
                :stderr "child diagnostic"
                :stdout-truncated? true
                :stderr-truncated? false
                :summary-parse-diagnostic :invalid-edn
                :summary-parse-message "bad map"
                :elapsed-ms 4}
        summary (runner/aggregate-results [entry] [result] false 4)
        projected (first (:results summary))]
    (is (= :failed (:status summary)))
    (is (= "assertion output" (:stdout projected)))
    (is (= "child diagnostic" (:stderr projected)))
    (is (true? (:stdout-truncated? projected)))
    (is (= :invalid-edn (:summary-parse-diagnostic projected)))
    (is (= "bad map" (:summary-parse-message projected)))))

(deftest timeout-kills-descendant-and-reports-bounded-output
  (let [entry (first runner/catalog)
        command ["/bin/sh" "-c"
                 "sleep 30 & child=$!; printf '%s\\n' \"$child\"; wait \"$child\""]
        result (binding [runner/*child-timeout-ms* 150]
                 (with-redefs [runner/child-command (fn [_ _] command)]
                   (runner/run-child-process! entry {})))
        pid (some-> (:stdout result) str/trim parse-long)
        alive? (when pid
                 (some-> (ProcessHandle/of pid) (.orElse nil) (.isAlive)))]
    (is (= :timeout (:status result)))
    (is (= 124 (:exit-code result)))
    (is (integer? pid))
    (is (not alive?))))

(deftest exited-root-cannot-leave-background-child-holding-streams
  (let [entry (first runner/catalog)
        attributes (make-array FileAttribute 0)
        acknowledgement (Files/createTempFile "gravity-leaf-observed-" ".ack"
                                               attributes)
        pid-file (Files/createTempFile "gravity-leaf-target-" ".pid" attributes)
        observed-pids (atom #{})
        command ["/bin/sh" "-c"
                 (str "ack=$1; pid_file=$2; sleep 30 & child=$!; "
                      "printf '%s\\n' \"$child\" > \"$pid_file\"; "
                      "printf '%s\\n' \"$child\"; "
                      "while [ ! -e \"$ack\" ]; do sleep 0.01; done; exit 0")
                 "gravity-leaf-lifecycle-test"
                 (str acknowledgement)
                 (str pid-file)]]
    (try
      (Files/deleteIfExists acknowledgement)
      (Files/deleteIfExists pid-file)
      (let [started (System/nanoTime)
            result (binding [runner/*child-timeout-ms* 5000
                             runner/*child-stream-drain-timeout-ms* 500
                             runner/*descendant-observer*
                             (fn [^ProcessHandle handle]
                               (swap! observed-pids conj (.pid handle))
                               (when-let [target-pid
                                          (try
                                            (some-> (slurp (str pid-file))
                                                    str/trim
                                                    parse-long)
                                            (catch java.io.FileNotFoundException _
                                              nil))]
                                 (when (contains? @observed-pids target-pid)
                                   (Files/write
                                    acknowledgement
                                    (byte-array 0)
                                    (make-array java.nio.file.OpenOption 0)))))]
                     (with-redefs [runner/child-command (fn [_ _] command)]
                       (runner/run-child-process! entry {})))
            elapsed-ms (long (/ (- (System/nanoTime) started) 1000000))
            pid (some-> (:stdout result) str/trim parse-long)
            alive? (when pid
                     (some-> (ProcessHandle/of pid) (.orElse nil) (.isAlive)))]
        (is (= :failed (:status result)))
        (is (< elapsed-ms 3000) elapsed-ms)
        (is (integer? pid) (pr-str (select-keys result [:status :exit-code :stdout
                                                         :stderr :stdout-truncated?])))
        (is (contains? @observed-pids pid)
            "the terminated child was observed by the sampler")
        (is (not alive?)))
      (finally
        (Files/deleteIfExists acknowledgement)
        (Files/deleteIfExists pid-file)))))

(deftest bounded-child-output-records-truncation-and-parse-failure
  (let [entry (first runner/catalog)
        command ["/bin/sh" "-c" "printf '0123456789abcdefghijklmnopqrstuvwxyz'"]
        result (binding [runner/*child-output-limit-bytes* 12]
                 (with-redefs [runner/child-command (fn [_ _] command)]
                   (runner/run-child-process! entry {})))]
    (is (= :failed (:status result)))
    (is (= "0123456789ab" (:stdout result)))
    (is (true? (:stdout-truncated? result)))
    (is (= :missing-summary-line (:summary-parse-diagnostic result)))))

(deftest repository-root-prefers-classpath-and-rejects-path-traversal
  (let [expected (runner/repository-root)
        previous (System/getProperty "user.dir")]
    (try
      (System/setProperty "user.dir" "/")
      (is (= expected (runner/repository-root)))
      (finally
        (System/setProperty "user.dir" previous))))
  (is (= "S0LEAF-REPOSITORY-PATH"
         (:id (exception-data #(runner/repository-path "../outside")))))
  (is (= "S0LEAF-REPOSITORY-PATH"
         (:id (exception-data #(runner/repository-path "/tmp/outside"))))))

(deftest repository-path-policy-rejects-symbolic-link-components
  (let [attributes (make-array FileAttribute 0)
        root (Files/createTempDirectory "gravity-leaf-root-" attributes)
        destination (Files/createTempDirectory "gravity-leaf-destination-"
                                               attributes)
        link (.resolve root "linked")
        reject-links! (var-get
                       (private-runner-var
                        'reject-symbolic-link-components!))]
    (try
      (Files/createSymbolicLink link destination attributes)
      (is (= "S0LEAF-REPOSITORY-SYMLINK"
             (:id (exception-data
                   #(reject-links! root
                                   (Path/of "linked/file.clj"
                                            (make-array String 0)))))))
      (finally
        (Files/deleteIfExists link)
        (Files/deleteIfExists root)
        (Files/deleteIfExists destination)))))

(deftest one-cheap-real-child-preserves-bootstrap-boundary
  (let [entry (first (filter #(= 'gravity.digest-test (:namespace %))
                             runner/catalog))
        summary (runner/run-selected-tests! [entry])]
    (is (= :passed (:status summary)))
    (is (= 0 (:exit-code summary)))
    (is (= 1 (:executed-count summary)))
    (is (nil? (find-ns 'gravity.bootstrap)))))
