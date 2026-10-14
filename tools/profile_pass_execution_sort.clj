;; Explicit bounded development profile. Never used by default test execution.
(require '[clojure.test :as t]
         '[gravity.pass-execution :as execution]
         '[gravity.pass-execution.canonical :as canonical]
         '[gravity.pass-execution-test :as fixture]
         '[gravity.self-hosting.sh01-host-resource-broker :as broker]
         '[clojure.java.shell :as shell])
(when (seq *command-line-args*)
  (throw (ex-info "This profile has a fixed workload and accepts no options" {})))
(defn file-id [path]
  (str "sha256:" (gravity.digest/sha256-bytes-hex
                  (java.nio.file.Files/readAllBytes
                   (.toPath (clojure.java.io/file path))))))
(defn fixture-fn [name] (var-get (ns-resolve 'gravity.pass-execution-test name)))
(def contract ((fixture-fn 'pass-contract) :parse 10))
(def request ((fixture-fn 'request) contract [(:input-a fixture/ids)] #{}))
(def profiler-bean (java.lang.management.ManagementFactory/getThreadMXBean))
(defn observation [f]
  (let [cpu (.getCurrentThreadCpuTime profiler-bean) wall (System/nanoTime)
        bytes (.getThreadAllocatedBytes ^com.sun.management.ThreadMXBean profiler-bean (.getId (Thread/currentThread)))
        value (f)]
    {:wall-ns (- (System/nanoTime) wall) :cpu-ns (- (.getCurrentThreadCpuTime profiler-bean) cpu)
     :allocated-bytes (- (.getThreadAllocatedBytes ^com.sun.management.ThreadMXBean profiler-bean (.getId (Thread/currentThread))) bytes)
     :value value}))
(defn run [request calls]
  (execution/execute-pass! request ((fixture-fn 'operations) (:output-a fixture/ids) calls)))
(def timeout (doto (Thread. #(do (Thread/sleep 120000) (println "PROFILE-TIMEOUT") (System/exit 124))) (.setDaemon true) (.start)))
(def result
  (broker/with-lease {:coordination-root "/private/tmp/gravity-sh01-resource-broker-v1" :timeout-ms 10000} :normal
    (fn []
      (let [load-end (.getUptime (java.lang.management.ManagementFactory/getRuntimeMXBean))]
        (mapv
          (fn [rows]
            (let [request (assoc-in request [:provenance :metadata]
                                   (into {} (map (fn [i] [(keyword (str "row-" i)) {:value i :labels #{:source :proof :profile}}])) (range rows)))
                  calls (atom []) baseline (run request calls)
                  printed (atom 0) original pr-str
                  instrumented (with-redefs [clojure.core/pr-str (fn [& xs] (swap! printed inc) (apply original xs))]
                                 (observation #(run request (atom []))))
                  _ (dotimes [_ 10] (run request (atom [])))
                  rounds (mapv (fn [_] (dissoc (observation #(dotimes [_ 20] (run request (atom [])))) :value)) (range 7))]
              {:rows rows :receipt-id (get-in baseline [:receipt :receipt-id])
               :output-id (get-in baseline [:receipt :output-artifact-id])
               :operation-calls @calls :serialization-calls @printed
               :instrumented (dissoc instrumented :value) :instrumented-equal? (= baseline (:value instrumented))
               :startup-and-require-ms load-end :iterations-per-round 20 :rounds rounds})) [0 64])))))
(prn {:schema :gravity/pass-sort-profile-v1 :authority :non-authoritative
      :source-commit (clojure.string/trim (:out (shell/sh "git" "rev-parse" "HEAD")))
      :source-tree (clojure.string/trim (:out (shell/sh "git" "rev-parse" "HEAD^{tree}")))
      :worktree-status (clojure.string/trim (:out (shell/sh "git" "status" "--short")))
      :canonical-source-id (file-id "bootstrap/clojure/src/gravity/pass_execution/canonical.clj")
      :fixture-source-id (file-id "bootstrap/clojure/test/gravity/pass_execution_test.clj")
      :harness-id (file-id *file*)
      :java (System/getProperty "java.version") :clojure (clojure-version)
      :os (System/getProperty "os.name") :arch (System/getProperty "os.arch")
      :classpath (System/getProperty "java.class.path") :max-heap (.maxMemory (Runtime/getRuntime))
      :samples result})
(shutdown-agents)
