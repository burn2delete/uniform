(ns ci.run
  "CI orchestration only. Receipts are observations, never compiler authority."
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.lang ProcessHandle]
           [java.util.concurrent TimeUnit]))

(def output "target/ci")
(def runtime-deps "{:deps {org.clojure/clojure {:mvn/version \"1.12.5\"}}}")

(defn command [heap & args]
  (into ["clojure" (str "-J-Xmx" heap) "-Srepro" "-Sdeps" runtime-deps] args))

(defn stop! [^Process process]
  (let [children (with-open [stream (.descendants (.toHandle process))]
                   (vec (.toArray stream)))]
    (doseq [^ProcessHandle child (reverse children)] (.destroyForcibly child))
    (.destroyForcibly process)
    (.waitFor process 5 TimeUnit/SECONDS)))

(defn execute! [id seconds argv]
  (.mkdirs (io/file output))
  (let [started (System/currentTimeMillis)
        log (io/file output (str id ".log"))
        receipt (io/file output (str id ".edn"))
        initial {:id id :argv argv :timeout-seconds seconds
                 :started-ms started :status :running
                 :commit (System/getenv "GITHUB_SHA")
                 :run-id (System/getenv "GITHUB_RUN_ID")
                 :run-attempt (System/getenv "GITHUB_RUN_ATTEMPT")}]
    (spit receipt (pr-str initial))
    (println "CI command" id argv)
    (let [result
          (try
            (let [process (.start (doto (ProcessBuilder. ^java.util.List argv)
                                   (.redirectErrorStream true)
                                   (.redirectOutput log)))]
              (try
                (if (.waitFor process seconds TimeUnit/SECONDS)
                  {:status (if (zero? (.exitValue process)) :passed :failed)
                   :exit-code (.exitValue process)}
                  (do (stop! process) {:status :timeout :exit-code 124}))
                (finally (when (.isAlive process) (stop! process)))))
            (catch Exception error
              (spit log (str error) :append true)
              {:status :launch-error :exit-code 125}))
          result (merge initial result {:finished-ms (System/currentTimeMillis)})]
      (spit receipt (str (pr-str result) "\n"))
      (print (slurp log))
      (flush)
      result)))

(defn script [id path & args]
  [id 90 (apply command "512m" "-M" path args)])

(defn projection-commands []
  (let [files (->> (.listFiles (io/file "tools"))
                   (map #(.getName %)) sort)]
    (concat
     [(script "docs" "tools/validate_gravity_docs.clj")
      (script "roadmap" "tools/validate_full_language_roadmap.clj")
      (script "roadmap-negative-controls" "tools/validate_full_language_roadmap.clj" "--self-test")
      (script "dependency-controls" "tools/acceptance_dependencies_test.clj")]
     (for [file files :when (str/ends-with? file "_task_projection.clj")]
       (script (str "check-" file) (str "tools/" file) "--check"))
     (for [file files :when (or (= file "task_projection_test.clj")
                               (str/ends-with? file "_task_projection_test.clj"))]
       (script (str "test-" file) (str "tools/" file))))))

(defn commands [mode]
  (case mode
    "static" [["whitespace" 30 ["git" "diff" "--check" "HEAD^" "HEAD"]]
              ["workflow-lint" 30 ["actionlint" "-color"]]
              ["clojure-lint" 60 ["clj-kondo" "--lint" "tools/ci"]]
              ["clojure-format" 60 ["cljfmt" "check" "tools/ci"]]
              ["harness-controls" 60 (command "256m" "-M" "tools/ci/run_test.clj")]]
    "projections" (projection-commands)
    "bootstrap" [["cold-bootstrap-load" 120
                   (command "1g" "-M" "-e"
                            "(assert (nil? (find-ns 'gravity.bootstrap))) (require 'gravity.bootstrap) (println :cold-bootstrap-loaded)")]
                  ["reader-regression" 120
                   (command "1g" "-M:dev-test"
                            "--namespace" "gravity.reader-primitives-test"
                            "--namespace" "gravity.c3-reader-expansions-test")]
                  ["language-boundary" 90
                   (command "512m" "-M:test" "--namespace"
                            "gravity.self-hosting.sh01-language-boundary-test")]]
    "leaves" [["fresh-leaf-catalog" 900 (command "512m" "-M:leaf-test")]]
    (throw (ex-info "Unknown CI mode" {:mode mode}))))

(defn run! [mode]
  (let [results (mapv #(apply execute! %) (commands mode))]
    (spit (io/file output (str mode "-summary.edn")) (str (pr-str results) "\n"))
    (if (every? #(= :passed (:status %)) results) 0 1)))

(when (= *file* (System/getProperty "babashka.file"))
  (throw (ex-info "Use the pinned Clojure CLI" {})))

(when (seq *command-line-args*)
  (System/exit (run! (first *command-line-args*))))
