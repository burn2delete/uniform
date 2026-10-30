;; Compiler-free source-form/DATA IO model. No real runner, tests or child load.
(require '[clojure.java.io :as io] '[clojure.edn :as edn]
         '[clojure.string :as str] '[clojure.test :as test :refer [deftest is]])
(def roots (atom #{}))
(def scenario (atom {}))
(def events (atom []))
(def captured (atom nil))
(defn forms [path]
  (with-open [r (java.io.PushbackReader. (io/reader path))]
    (binding [*read-eval* false]
      (let [eof (Object.)]
        (loop [result {}]
          (let [f (read {:eof eof} r)]
            (if (identical? eof f) result
                (recur (if (and (seq? f) (#{'def 'defn 'defn-} (first f)))
                         (assoc result (second f) f) result)))))))))
(def dev-forms (forms (or (System/getProperty "gravity.development-result-child-source")
                          "bootstrap/clojure/test/gravity/development_test_runner.clj")))
(def parent-forms (forms (or (System/getProperty "gravity.development-result-parent-source")
                             "bootstrap/clojure/test/gravity/self_hosting/sh01_parallel_test_runner.clj")))
(def child-ns (create-ns 'development-result.child-model))
(def parent-ns (create-ns 'development-result.parent-model))
(def broker-ns (create-ns 'development-result.broker-model))
(doseq [space [child-ns parent-ns]]
  (binding [*ns* space]
    (clojure.core/refer 'clojure.core)
    (alias 'io 'clojure.java.io) (alias 'edn 'clojure.edn) (alias 'str 'clojure.string)
    (import 'java.nio.charset.StandardCharsets)))
(doseq [name '[*machine-result-sink* *timing-events*]]
  (binding [*ns* child-ns] (eval (get dev-forms name))))
(def selected-vars ["gravity.fixture-test/a" "gravity.fixture-test/b" "gravity.fixture-test/c"])
(doseq [[name implementation]
        {'selected-namespace-records (fn [_] [{:namespace 'gravity.fixture-test}])
         'compile-patterns (constantly [])
         'load-selected-namespaces! (fn [_] (swap! events conj :modeled-namespace-load))
         'test-var-records (fn [_] (mapv #(hash-map :qualified-name %) selected-vars))
         'select-test-vars (fn [records _ _] records)
         'run-selected-tests (fn [_ _ _]
                               (swap! events conj :test-finally-complete)
                               (when-let [error (:test-error @scenario)] (throw error))
                               (or (:counts @scenario) {:test 3 :pass 6 :fail 0 :error 0}))
         'print-selection (fn [_] (throw (AssertionError. "No selection printing")))
         'print-catalog (fn [] (throw (AssertionError. "No catalog query")))
         'write-timing-receipt! (fn [& _] (throw (AssertionError. "No timing route")))}]
  (intern child-ns name implementation))
(binding [*ns* child-ns]
  (doseq [name '[machine-result-schema machine-result-file-sink usage-text selector-value parse-args run-cli!]]
    (eval (get dev-forms name))))
(def child-run (var-get (ns-resolve child-ns 'run-cli!)))
(def sink-factory (var-get (ns-resolve child-ns 'machine-result-file-sink)))
(def source-binding {:source-commit (apply str (repeat 40 "a"))
                     :source-tree (apply str (repeat 40 "b"))
                     :configuration-sha256 (apply str (repeat 64 "c"))
                     :limit-bytes 65536})
(def job {:namespace 'gravity.fixture-test :resource-class :memory-heavy})
(def options {:command ["inert-java" "clojure.main" "-m" "gravity.development-test-runner"]
              :exact-test-vars ["a" "b" "c"] :child-fail-fast? true
              :working-directory "/inert/cwd" :resource-broker {:coordination-root "/inert/root"}
              :development-result source-binding :timeout-ms 1000})
(def complete-process
  {:status :passed :exit-code 0 :root-exited? true :cleanup-root-alive? false
   :tracked-owned-processes-terminal? true :capture-failed? false :output-truncated? false
   :stdout-capture-status :complete :stderr-capture-status :complete
   :stdout-capture-error nil :stderr-capture-error nil
   :stdout-capture-forced-close? false :stderr-capture-forced-close? false
   :cleanup-complete? nil :descendant-coverage :unknown :stdout "capture" :stderr ""})
(defn fake-process []
  (proxy [Process] []
    (waitFor [] (throw (AssertionError. "No real wait")))
    (exitValue [] (throw (AssertionError. "No real exit probe")))
    (isAlive [] (throw (AssertionError. "No real alive probe")))
    (destroy [] (throw (AssertionError. "No real termination")))))
(intern broker-ns 'with-lease
        (fn [_ _ operation]
          (swap! events conj [:modeled-acquire (.getId (Thread/currentThread))])
          (try (operation)
               (finally (swap! events conj [:modeled-disposition (.getId (Thread/currentThread))])))))
(binding [*ns* parent-ns] (alias 'broker (ns-name broker-ns)))
(doseq [[name implementation]
        {'root (delay "/inert/default")
         'forward-child-cache-option? (constantly false)
         'require-supported-namespace! (fn [_] (swap! events conj :owner-guard))
         'start-process (fn [argv cwd]
                          (swap! events conj :modeled-start)
                          (reset! captured {:argv argv :cwd cwd})
                          (fake-process))
         'process-result (fn [_ _]
                           (let [argv (:argv @captured)
                                 at (.indexOf ^java.util.List argv "--namespace")
                                 exit (child-run (subvec argv at))]
                             (swap! events conj :modeled-process-complete)
                             (or (:process @scenario)
                                 (assoc complete-process :exit-code exit
                                        :status (if (zero? exit) :passed :failed)))))
         'result-failure? (constantly false)
         'bound-launch-result (fn [result _] result)}]
  (intern parent-ns name implementation))
(binding [*ns* parent-ns]
  (doseq [name '[default-command namespace-name command-prefix namespace-command
                 fatal-throwable? fatal-cause rethrow-fatal! restore-interrupt!
                 prepare-development-result read-development-result normalize-result run-namespace-process]]
    (eval (get parent-forms name))))
(def prepare-actual (var-get (ns-resolve parent-ns 'prepare-development-result)))
(defn tracked-prepare [job options]
  (when-let [delivery (prepare-actual job options)]
    (swap! roots conj (.getParent (.toPath (io/file (:file delivery)))))
    delivery))
(intern parent-ns 'prepare-development-result tracked-prepare)
(def read-result (var-get (ns-resolve parent-ns 'read-development-result)))
(def run-parent (var-get (ns-resolve parent-ns 'run-namespace-process)))
(defn fresh [] (tracked-prepare job options))
(defn machine-record []
  {:schema :gravity/development-test-result-v1 :authority :non-authoritative :authoritative? false
   :selection {:namespaces ["gravity.fixture-test"] :test-vars selected-vars :fail-fast? true}
   :summary {:test 3 :pass 6 :fail 0 :error 0} :outcome :passed :exit-code 0})
(defn envelope [delivery result]
  {:schema :gravity/development-test-result-delivery-v1 :request (:request delivery) :result result})
(defn write-data! [delivery data]
  (spit (:file delivery) (str (pr-str data) "\n")))
(defn failure [operation]
  (try (operation) nil (catch Throwable error error)))

(deftest actual-sink-receives-final-completion-on-the-normal-call
  (reset! scenario {}) (reset! events [])
  (let [outcome (run-parent job options)
        result (:development-result outcome)
        path (:development-result-file outcome)]
    (is (= (machine-record) result))
    (is (= 0 (:exit-code outcome)))
    (is (nil? (:cleanup-complete? outcome)))
    (is (= :unknown (:descendant-coverage outcome)))
    (is (= :supplied-data-matched (:development-result-binding-status outcome)))
    (is (.isFile (io/file path)))
    (is (str/ends-with? path "/result.edn"))
    (is (= result (:result (edn/read-string (slurp path)))))
    (is (< (.indexOf ^java.util.List @events :test-finally-complete)
           (.indexOf ^java.util.List @events :modeled-process-complete)))
    (is (= "/inert/cwd" (:cwd @captured)))
    (is (= 1 (count (filter #{"--machine-result"} (:argv @captured)))))
    (is (= (first @events) :owner-guard))
    (is (= [[:modeled-acquire (.getId (Thread/currentThread))]
            [:modeled-disposition (.getId (Thread/currentThread))]]
           (vec (filter vector? @events))))))

(deftest default-sink-and-original-errors-are-preserved
  (reset! scenario {})
  (let [seen (atom nil)
        sink (fn [record] (reset! seen record))]
    (with-bindings {(ns-resolve child-ns '*machine-result-sink*) sink}
      (is (= 0 (child-run ["--namespace" "gravity.fixture-test"]))))
    (is (= (dissoc (machine-record) :selection)
           (dissoc @seen :selection))))
  (let [original (ex-info "modeled test/finally exception" {})
        delivery (fresh)]
    (reset! scenario {:test-error original})
    (is (identical? original (failure #(child-run ["--namespace" "gravity.fixture-test"
                                                   "--machine-result" (pr-str delivery)]))))
    (is (not (.exists (io/file (:file delivery))))))
  (reset! scenario {}))

(deftest exactly-one-write-and-original-callback-error
  (let [delivery (fresh) record (machine-record) seen (atom nil)
        sink (sink-factory delivery #(reset! seen %))]
    (sink record)
    (is (identical? record @seen))
    (is (= record (read-result delivery complete-process)))
    (is (= "DEV-RESULT-DUPLICATE" (:id (ex-data (failure #(sink record)))))))
  (let [delivery (fresh) original (java.io.IOException. "inherited sink failure")
        sink (sink-factory delivery (fn [_] (throw original)))]
    (is (identical? original (failure #(sink (machine-record)))))
    (is (.exists (io/file (:file delivery))))
    (is (= :process-capture
           (:field (ex-data (failure #(read-result delivery (assoc complete-process :status :failed :exit-code 2)))))))))

(deftest stale-duplicate-partial-and-contradictory-records-refuse
  (doseq [change [(fn [r] (assoc-in r [:request :id] "stale"))
                  (fn [r] (assoc-in r [:request :source :configuration-sha256] "stale"))
                  (fn [r] (assoc-in r [:result :selection :namespaces] ["gravity.other-test"]))
                  (fn [r] (assoc-in r [:result :selection :test-vars] (conj selected-vars "gravity.fixture-test/d")))
                  (fn [r] (assoc-in r [:result :selection :fail-fast?] false))
                  (fn [r] (assoc-in r [:result :summary :test] 2))
                  (fn [r] (assoc-in r [:result :summary :test] "3"))
                  (fn [r] (update-in r [:result :summary] dissoc :pass))
                  (fn [r] (assoc-in r [:result :summary :pass] -1))
                  (fn [r] (assoc-in r [:result :authoritative?] true))
                  (fn [r] (assoc-in r [:result :summary :fail] 1))
                  (fn [r] (assoc-in r [:result :outcome] :failed))
                  (fn [r] (assoc-in r [:result :exit-code] 1))
                  (fn [r] (assoc-in r [:result :extra] :forbidden))]]
    (let [delivery (fresh) record (change (envelope delivery (machine-record)))]
      (write-data! delivery record)
      (let [error (failure #(read-result delivery complete-process))]
        (is (= "SH01-DEVELOPMENT-RESULT" (:id (ex-data error))))
        (is (identical? complete-process (:process-result (ex-data error))))
        (is (= record (:record (ex-data error)))))))
  (doseq [text ["" "{" "{:partial" "{} {}"]]
    (let [delivery (fresh)]
      (spit (:file delivery) text)
      (is (some? (failure #(read-result delivery complete-process))))))
  (let [delivery (fresh) record (envelope delivery (machine-record))]
    (spit (:file delivery) (str (pr-str record) "\n" (pr-str record)))
    (is (= :one-record (:field (ex-data (failure #(read-result delivery complete-process))))))))

(deftest actual-process-capture-facts-must-agree
  (doseq [raw [(assoc complete-process :exit-code 2)
               (assoc complete-process :status :failed)
               (assoc complete-process :root-exited? false)
               (assoc complete-process :cleanup-root-alive? true)
               (assoc complete-process :tracked-owned-processes-terminal? false)
               (assoc complete-process :capture-failed? true)
               (assoc complete-process :output-truncated? true)
               (assoc complete-process :stdout-capture-status :truncated)
               (assoc complete-process :stderr-capture-status :capture-timeout)
               (assoc complete-process :stderr-capture-error "error")
               (assoc complete-process :stdout-capture-forced-close? true)
               (dissoc complete-process :root-exited?)]]
    (let [delivery (fresh)]
      ((sink-factory delivery nil) (machine-record))
      (let [error (failure #(read-result delivery raw))]
        (is (= :process-capture (:field (ex-data error))))
        (is (identical? raw (:process-result (ex-data error)))))))
  (let [delivery (fresh)
        record (assoc (machine-record) :summary {:test 3 :pass 5 :fail 1 :error 0}
                      :outcome :failed :exit-code 1)]
    ((sink-factory delivery nil) record)
    (is (= record (read-result delivery (assoc complete-process :status :failed :exit-code 1)))))
  (let [delivery (fresh)
        record (assoc (machine-record) :summary {:test 1 :pass 0 :fail 1 :error 0}
                      :outcome :failed :exit-code 1)]
    ((sink-factory delivery nil) record)
    (is (= :completion (:field (ex-data (failure #(read-result delivery (assoc complete-process :status :failed :exit-code 1)))))))))

(deftest bounded-file-errors-missing-symlink-and-encoding-refuse
  (let [delivery (fresh)
        error (failure #(read-result delivery complete-process))]
    (is (instance? java.nio.file.NoSuchFileException (.getCause error)))
    (is (identical? complete-process (:process-result (ex-data error)))))
  (let [delivery (fresh) sink (sink-factory (assoc delivery :limit-bytes 1) nil)]
    (is (= "DEV-RESULT-LIMIT" (:id (ex-data (failure #(sink (machine-record)))))))
    (is (not (.exists (io/file (:file delivery))))))
  (let [delivery (fresh)]
    (spit (:file delivery) "existing")
    (is (instance? java.nio.file.FileAlreadyExistsException
                   (failure #((sink-factory delivery nil) (machine-record)))))
    (is (= "existing" (slurp (:file delivery)))))
  (let [delivery (fresh)
        target (.resolve (.getParent (.toPath (io/file (:file delivery)))) "target.edn")]
    (spit (str target) "unchanged")
    (java.nio.file.Files/createSymbolicLink (.toPath (io/file (:file delivery))) target
                                            (make-array java.nio.file.attribute.FileAttribute 0))
    (is (some? (failure #(read-result delivery complete-process))))
    (is (= "unchanged" (slurp (str target)))))
  (let [delivery (fresh)]
    (java.nio.file.Files/write (.toPath (io/file (:file delivery))) (byte-array [(unchecked-byte 255)])
                               (make-array java.nio.file.OpenOption 0))
    (is (instance? java.nio.charset.MalformedInputException
                   (.getCause (failure #(read-result delivery complete-process)))))))

(deftest invalid-requests-and-no-execution-modes-do-not-create-results
  (doseq [binding [nil {} (assoc source-binding :source-tree "wrong")
                   (assoc source-binding :limit-bytes 0) (assoc source-binding :extra true)]]
    (is (some? (failure #(run-parent job (assoc options :development-result binding))))))
  (doseq [vars [nil [] [:bad] ["a" "a"] ["other.namespace/a"]]]
    (is (some? (failure #(tracked-prepare job (assoc options :exact-test-vars vars))))))
  (doseq [mode ["--help" "--catalog" "--list"]]
    (let [delivery (fresh)]
      (is (some? (failure #(child-run [mode "--machine-result" (pr-str delivery)]))))
      (is (not (.exists (io/file (:file delivery)))))))
  (let [delivery (fresh)]
    (is (some? (failure #(child-run ["--machine-result" (pr-str delivery)
                                     "--machine-result" (pr-str delivery)]))))))

(let [summary (test/run-tests 'user)]
  (doseq [root @roots]
    (with-open [paths (java.nio.file.Files/walk root (make-array java.nio.file.FileVisitOption 0))]
      (doseq [path (reverse (sort-by #(.getNameCount ^java.nio.file.Path %)
                                     (iterator-seq (.iterator paths))))]
        (java.nio.file.Files/deleteIfExists path))))
  (assert (nil? (find-ns 'gravity.development-test-runner)))
  (assert (nil? (find-ns 'gravity.self-hosting.sh01-parallel-test-runner)))
  (assert (nil? (find-ns 'gravity.bootstrap)))
  (prn {:schema :development-result-boundary/model-v1 :summary summary
        :runtime (System/getProperty "java.runtime.version")
        :actual-children 0 :actual-leases 0 :actual-process-probes 0
        :source-config-binding :DATA-not-attestation :semantic-authenticity :not-established
        :real-runner-target-namespace-loads 0 :native-startup-and-final-custody :open})
  (when (pos? (+ (:fail summary) (:error summary))) (System/exit 1)))
