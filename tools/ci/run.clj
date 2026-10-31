(ns ci.run
  "CI orchestration only. Receipts are observations, never compiler authority."
  (:refer-clojure :exclude [run! descendants])
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.lang ProcessHandle]
           [java.util.concurrent TimeUnit]))

(def output "target/ci")
(def runtime-deps "{:deps {org.clojure/clojure {:mvn/version \"1.12.5\"}}}")

(defn command [heap & args]
  (into ["clojure" (str "-J-Xmx" heap) "-Srepro" "-Sdeps" runtime-deps] args))

(defn descendants [^Process process]
  (with-open [stream (.descendants (.toHandle process))]
    (vec (.toArray stream))))

(defn stop! [^Process process children]
  (let [errors (atom [])]
    (try
      (doseq [^ProcessHandle child (reverse children)]
        (try
          (when (.isAlive child)
            (.destroyForcibly child)
            (.get (.onExit child) 5 TimeUnit/SECONDS))
          (catch Exception error (swap! errors conj (str error)))))
      (finally
        (when (.isAlive process) (.destroyForcibly process))
        (.waitFor process 5 TimeUnit/SECONDS)))
    (when (seq @errors)
      (throw (ex-info "Owned process cleanup failed" {:errors @errors})))))

(defn await! [^Process process seconds]
  (let [deadline (+ (System/nanoTime) (* seconds 1000000000))
        children (atom #{})]
    (try
      (loop []
        (swap! children into (descendants process))
        (cond
          (.waitFor process 100 TimeUnit/MILLISECONDS)
          {:status (if (zero? (.exitValue process)) :passed :failed)
           :exit-code (.exitValue process) :pid (.pid process)
           :descendant-pids (mapv #(.pid ^ProcessHandle %) @children)}
          (>= (System/nanoTime) deadline)
          {:status :timeout :exit-code 124 :pid (.pid process)
           :descendant-pids (mapv #(.pid ^ProcessHandle %) @children)}
          :else (recur)))
      (finally (stop! process @children)))))

(defn execute!
  ([id seconds argv] (execute! id seconds argv nil))
  ([id seconds argv stdin-path]
   (.mkdirs (io/file output))
   (let [started (System/currentTimeMillis)
         log (io/file output (str id ".log"))
         receipt (io/file output (str id ".edn"))
         initial {:id id :argv argv :timeout-seconds seconds
                  :started-ms started :status :running
                  :commit (System/getenv "GITHUB_SHA")
                  :run-id (System/getenv "GITHUB_RUN_ID")
                  :run-attempt (System/getenv "GITHUB_RUN_ATTEMPT")
                  :stdin-path stdin-path}]
     (spit receipt (pr-str initial))
     (println "CI command" id argv)
     (let [result
           (try
             (let [builder (doto (ProcessBuilder. ^java.util.List argv)
                             (.redirectErrorStream true)
                             (.redirectOutput log))
                   _ (when stdin-path (.redirectInput builder (io/file stdin-path)))
                   process (.start builder)]
               (await! process seconds))
             (catch Exception error
               (spit log (str error) :append true)
               {:status :launch-error :exit-code 125}))
           result (merge initial result {:finished-ms (System/currentTimeMillis)})]
       (spit receipt (str (pr-str result) "\n"))
       (print (slurp log))
       (flush)
       result))))

(defn script [id path & args]
  [id 90 (apply command "512m" "-M" path args)])

(defn base-ref [base]
  (if (and base (re-matches #"[0-9a-f]{40}" base)
           (not= base (apply str (repeat 40 "0")))) base "HEAD^"))

(defn comparison-base [event before pr-head parents]
  (if (= event "pull_request")
    (do
      (when-not (and (= 2 (count parents))
                     (re-matches #"[0-9a-f]{40}" (or pr-head ""))
                     (= pr-head (second parents)))
        (throw (ex-info "PR checkout must be the tested merge of the event head"
                        {:pr-head pr-head :parents parents})))
      (first parents))
    (base-ref before)))

(defn checkout-base []
  (let [result (execute! "checkout-parents" 30
                         ["git" "show" "-s" "--format=%P" "HEAD"])]
    (when-not (= :passed (:status result))
      (throw (ex-info "Cannot verify checkout parents" result)))
    (comparison-base (System/getenv "GITHUB_EVENT_NAME")
                     (System/getenv "CI_BASE_SHA")
                     (System/getenv "CI_PR_HEAD_SHA")
                     (str/split (str/trim (slurp (io/file output "checkout-parents.log"))) #"\s+"))))

(defn changed-clojure-paths [base]
  (let [result (execute! "changed-path-discovery" 30
                         ["git" "diff" "--name-only" "--diff-filter=ACMR" "-z" base "HEAD"])]
    (when-not (= :passed (:status result))
      (throw (ex-info "Cannot determine changed files; selection failed closed" result)))
    (->> (str/split (slurp (io/file output "changed-path-discovery.log")) #"\u0000")
         (filter #(or (str/ends-with? % ".clj") (str/ends-with? % ".edn")))
         vec)))

(defn contract-commands []
  [(script "docs" "tools/validate_gravity_docs.clj")
   (script "roadmap" "tools/validate_full_language_roadmap.clj")
   (script "roadmap-negative-controls" "tools/validate_full_language_roadmap.clj" "--self-test")
   (script "dependency-controls" "tools/acceptance_dependencies_test.clj")])

(defn leaf-selection [platform catalog]
  (when-not (#{"portable" "darwin"} platform)
    (throw (ex-info "Unknown leaf platform" {:platform platform})))
  (filterv #(= (= platform "darwin") (= "darwin-publication" (:id %))) catalog))

(defn leaf-command [platform]
  (binding [*command-line-args* nil]
    (load-file "bootstrap/clojure/test/gravity/bootstrap_free_leaf_test_runner.clj"))
  (let [catalog (var-get (requiring-resolve 'gravity.bootstrap-free-leaf-test-runner/catalog))
        selected (leaf-selection platform catalog)]
    (when (empty? selected) (throw (ex-info "Empty platform leaf selection" {:platform platform})))
    (apply command "512m" "-M:leaf-test"
           (mapcat (fn [entry] ["--namespace" (str (:namespace entry))]) selected))))

(defn commands [mode]
  (case mode
    "static" (let [base (checkout-base)
                   paths (changed-clojure-paths base)]
               (concat [["whitespace" 30 ["git" "diff" "--check" base "HEAD"]]
                        ["workflow-lint" 30 ["actionlint" "-color"]]
                        ["clojure-lint" 60 ["clj-kondo" "--lint" "tools/ci"]]
                        ["clojure-format" 60 ["cljfmt" "check" "tools/ci"]]
                        ["harness-controls" 60 (command "256m" "-M" "tools/ci/run_test.clj")]
                        ["macro-context-controls" 60 (command "256m" "-M" "tools/ci/lint_macro_test.clj")]
                        ["facade-context-controls" 60 (command "256m" "-M" "tools/ci/lint_facade_test.clj")]]
                       (when (seq paths)
                         [["changed-clojure-lint" 120
                           (apply command "256m" "-M" "tools/ci/lint.clj" base paths)]])))
    "contracts" (contract-commands)
    "bootstrap" [["cold-bootstrap-load" 120
                  (command "1g" "-M" "-e"
                           "(assert (nil? (find-ns 'gravity.bootstrap))) (require 'gravity.bootstrap) (println :cold-bootstrap-loaded)")]
                 ["reader-regression" 120
                  (command "1g" "-M:dev-test"
                           "--namespace" "gravity.reader.artifact-test"
                           "--namespace" "gravity.reader-primitives-test"
                           "--namespace" "gravity.c3-reader-expansions-test")]
                 ["syntax-regression" 600
                  (command "2g" "-M:dev-test"
                           "--namespace" "gravity.syntax.pipeline-test"
                           "--namespace" "gravity.bootstrap-compatibility.c3-test"
                           "--namespace" "gravity.self-hosting.sh04-fresh-c2-construction-test"
                           "--namespace" "gravity.self-hosting.sh04-form-index-test"
                           "--namespace" "gravity.self-hosting.sh04-literal-index-test")]
                 ["language-boundary" 90
                  (command "512m" "-M:test" "--namespace"
                           "gravity.self-hosting.sh01-language-boundary-test")]
                 ["catalog-controls" 120
                  (command "512m" "-M:test" "--namespace"
                           "gravity.self-hosting.sh01-stage0-leaf-test-runner-test")]
                 ["ownership-inventory" 90
                  (command "512m" "-M:test" "--namespace"
                           "gravity.self-hosting.sh01-ownership-test")]]
    "macro" [["macro-regression" 600
              (command "2g" "-M:dev-test"
                       "--namespace" "gravity.macro.pipeline-test"
                       "--namespace" "gravity.bootstrap-compatibility.c4-test")]]
    "resolution" [["resolution-regression" 600
                   (command "2g" "-M:dev-test"
                            "--namespace" "gravity.resolution.pipeline-test"
                            "--namespace" "gravity.self-hosting.sh06-unmetered-resolution-test"
                            "--namespace" "gravity.self-hosting.sh06-invocation-selection-test")]]
    "leaves-portable" [["fresh-portable-leaf-catalog" 900 (leaf-command "portable")]]
    "darwin-probe" [["darwin-host" 15 ["sw_vers"]]
                    ["darwin-kernel" 15 ["uname" "-a"]]
                    ["darwin-build" 15 ["sysctl" "kern.version" "kern.osversion"]]
                    ["darwin-sdk" 15 ["xcrun" "--show-sdk-version"]]
                    ["darwin-abi" 30 (command "256m" "-J--enable-native-access=ALL-UNNAMED"
                                              "-M" "tools/ci/darwin_probe.clj")]]
    "darwin-unsupported" [["darwin-host" 15 ["sw_vers"]]
                          ["darwin-kernel" 15 ["uname" "-a"]]
                          ["darwin-build" 15 ["sysctl" "kern.version" "kern.osversion"]]
                          ["darwin-sdk" 15 ["xcrun" "--show-sdk-version"]]
                          ["darwin-unsupported-refusal" 30
                           (command "256m" "-J--enable-native-access=ALL-UNNAMED"
                                    "-M" "tools/ci/darwin_unsupported_test.clj")]]
    "leaves-darwin" [["fresh-darwin-leaf-catalog" 180 (leaf-command "darwin")]]
    (throw (ex-info "Unknown CI mode" {:mode mode}))))

(defn run! [mode]
  (let [results (mapv #(apply execute! %) (commands mode))]
    (spit (io/file output (str mode "-summary.edn")) (str (pr-str results) "\n"))
    (if (every? #(= :passed (:status %)) results) 0 1)))

(when (seq *command-line-args*)
  (System/exit (run! (first *command-line-args*))))
