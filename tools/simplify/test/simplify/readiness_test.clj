(ns simplify.readiness-test
  "Generated regression coverage for the observed startup and dependency failures.
   Protocol fixtures make no model, GitHub or compiler execution claim."
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [simplify.codex :as codex]
            [simplify.github :as gh]
            [simplify.main :as main]
            [simplify.runtime :as rt])
  (:import [java.nio.file Files]))

(defn- context [mode]
  (let [directory (str (Files/createTempDirectory "uniform-readiness-test-" (make-array java.nio.file.attribute.FileAttribute 0)))
        classpath (str/join java.io.File/pathSeparator
                            (map #(.getAbsolutePath (io/file %))
                                 (str/split (System/getProperty "java.class.path") (re-pattern java.io.File/pathSeparator))))
        config {:repository "fixture/repo" :repo-dir directory :state-dir directory :remote "origin" :base-branch "main"
                :codex-command [(str (io/file (System/getProperty "java.home") "bin/java")) "-cp" classpath
                                "clojure.main" "-m" "simplify.fake-server" mode]
                :check-timeout-ms 30000
                :compiler-observation-command ["clojure" "-J-Xmx2g" "-M:compiler-probe"]
                :checks [["clojure" "-M:test" "--namespace" "fixture.test"]]
                :completion-checks [["clojure" "-M:gravity" "self-host" "verify"]]}]
    (merge {:state-dir directory :state-file (str (io/file directory "state.edn"))
            :log-file (str (io/file directory "events.jsonl")) :log-lock (Object.) :processes (atom #{})
            :state (atom {:schema 1 :tasks {} :jobs {} :effects {}}) :config config}
           (rt/coordination directory config))))

(defn- events [ctx]
  (mapv #(json/read-str % :key-fn keyword) (str/split-lines (slurp (:log-file ctx)))))

(defn- rpc-messages [ctx]
  (mapv :message (filter #(= "rpc-send" (:event %)) (events ctx))))

(defn- hosted-checks [f]
  (with-redefs [rt/run! (fn [_ _ argv]
                         (is (contains? #{["git" "--version"] ["gh" "--version"] ["codex" "--version"]
                                          ["codex" "login" "status"] ["gh" "auth" "status" "--hostname" "github.com"]} argv))
                         "fixture availability")
                rt/git (fn [_ _ & argv]
                         (is (= ["ls-remote" "--exit-code" "origin" "refs/heads/main"] (vec argv)))
                         "fixture-head\trefs/heads/main")
                gh/api (fn [_ endpoint]
                         (is (= "repos/fixture/repo" endpoint))
                         {:full_name "fixture/repo" :permissions {:push true}})
                gh/owner-info (fn [_] {:id "fixture-owner"})
                gh/projects (fn [_ id] (is (= "fixture-owner" id)) [])]
    (f)))

(defn- report-check [report id]
  (first (filter #(= id (:check %)) (:checks report))))

(deftest readiness-exercises-configured-sandbox-and-alias-dependencies-without-inference
  (let [ctx (context "doctor")
        before @(:state ctx)
        report (hosted-checks #(main/doctor! ctx))
        messages (rpc-messages ctx)
        commands (filter #(= "command/exec" (:method %)) messages)
        expected [["git" "--version"]
                  ["clojure" "-M" "-e" "(println :uniform/doctor-ready)"]
                  ["clojure" "-Sforce" "-Spath" "-J-Xmx2g" "-M:compiler-probe"]
                  ["clojure" "-Sforce" "-Spath" "-M:test"]
                  ["clojure" "-Sforce" "-Spath" "-M:gravity"]]]
    (is (= :ready (:status report)))
    (is (= expected (mapv #(get-in % [:params :command]) commands)))
    (is (every? #{"initialize" "initialized" "command/exec"} (map :method messages)))
    (is (= (count expected) (count (filter #(= "initialize" (:method %)) messages))))
    (is (every? #(= {:type "workspaceWrite" :writableRoots [(:state-dir ctx)] :networkAccess false}
                    (get-in % [:params :sandboxPolicy])) commands))
    (is (every? #(= 30000 (get-in % [:params :timeoutMs])) commands))
    (is (= before @(:state ctx)))
    (is (empty? @(:processes ctx)))
    (is (= 1 (count (filter #(= "doctor-ok" (:event %)) (events ctx)))))))

(deftest failed-initialization-keeps-uncertain-diagnosis-and-skips-dependent-commands
  (doseq [mode ["initialize-exit" "initialize-malformed" "initialize-approval" "initialize-approval-error"]]
    (testing mode
      (let [ctx (context mode)
            effect {:status :uncertain :message "Original initialization failed"}
            _ (rt/update! ctx assoc-in [:effects "compiler-diagnosis/original"] effect)
            failure (hosted-checks #(try (main/doctor! ctx) (catch Exception e e)))
            report (ex-data failure)
            messages (rpc-messages ctx)]
        (is (instance? Exception failure))
        (is (= :blocked (:status (report-check report :app-server-initialize))))
        (is (= :skipped (:status (report-check report :sandbox-command))))
        (is (= :skipped (:status (report-check report :configured-dependencies))))
        (is (= 1 (count (filter #(= "initialize" (:method %)) messages))))
        (is (not-any? #(contains? #{"thread/start" "turn/start" "command/exec"} (:method %)) messages))
        (is (= effect (get-in @(:state ctx) [:effects "compiler-diagnosis/original"])))
        (is (empty? (:jobs @(:state ctx))))
        (is (empty? @(:processes ctx)))
        (is (not-any? #(= "doctor-ok" (:event %)) (events ctx)))
        (when (#{"initialize-approval" "initialize-approval-error"} mode)
          (is (true? (get-in (report-check report :app-server-initialize) [:details :restricted])))
          (is (some #(= {:decision "decline"} (:result %)) messages))
          (is (thrown? Exception (rt/assert-authorized! ctx :check (codex/check-key ctx ["git" "--version"])))))))))

(deftest missing-github-login-does-not-hide-working-runtime-or-invoke-provider-queries
  (let [ctx (context "doctor")
        failure (hosted-checks
                  #(let [run rt/run!]
                     (with-redefs [rt/run! (fn [context cwd argv]
                                            (if (= ["gh" "auth" "status" "--hostname" "github.com"] argv)
                                              (throw (ex-info "No logged-in GitHub hosts" {:argv argv :exit 1}))
                                              (run context cwd argv)))
                                   gh/api (fn [& _] (throw (AssertionError. "No provider query without login")))
                                   gh/owner-info (fn [& _] (throw (AssertionError. "No Projects query without login")))]
                       (try (main/doctor! ctx) (catch Exception e e)))))
        report (ex-data failure)]
    (is (= :blocked (:status report)))
    (is (= :blocked (:status (report-check report :github-login))))
    (is (= :skipped (:status (report-check report :github-repository))))
    (is (= :skipped (:status (report-check report :github-projects))))
    (is (= :passed (:status (report-check report :app-server-initialize))))
    (is (= :passed (:status (report-check report [:clojure-runtime "clojure"]))))))

(deftest stalled-initialization-uses-the-existing-check-watchdog-and-cleans-up
  (let [ctx (assoc-in (context "initialize-stall") [:config :check-timeout-ms] 1500)
        failure (hosted-checks #(try (main/doctor! ctx) (catch Exception e e)))
        startup (report-check (ex-data failure) :app-server-initialize)]
    (is (= :blocked (:status startup)))
    (is (= {:method "initialize" :timeout-ms 1500 :timed-out true} (:details startup)))
    (is (= ["initialize"] (mapv :method (rpc-messages ctx))))
    (is (empty? @(:processes ctx)))
    (is (empty? (:jobs @(:state ctx))))))

(deftest dependency-failure-is-distinct-from-successful-initialization-and-runtime
  (let [ctx (context "doctor-dependency-failure")
        failure (hosted-checks #(try (main/doctor! ctx) (catch Exception e e)))
        report (ex-data failure)
        dependencies (filter #(= :clojure-dependencies (first (when (vector? (:check %)) (:check %)))) (:checks report))]
    (is (= :passed (:status (report-check report :app-server-initialize))))
    (is (= :passed (:status (report-check report [:clojure-runtime "clojure"]))))
    (is (= 3 (count dependencies)))
    (is (every? #(= :blocked (:status %)) dependencies))
    (is (every? #(= "Could not resolve fixture Maven dependency" (get-in % [:details :result :stderr])) dependencies))
    (is (empty? @(:processes ctx)))))

(deftest undeclared-alias-is-blocked-even-when-a-cached-classpath-is-silent
  (let [ctx (assoc-in (context "doctor-undeclared-alias") [:config :compiler-observation-command]
                      ["clojure" "-M:missing-alias"])
        cached (codex/command! ctx "fixture-cached-dependency" (:state-dir ctx)
                               ["clojure" "-Spath" "-M:missing-alias"])
        before @(:state ctx)
        failure (hosted-checks #(try (main/doctor! ctx) (catch Exception e e)))
        report (ex-data failure)
        dependency (report-check report [:clojure-dependencies ["clojure" "-Sforce" "-Spath" "-M:missing-alias"]])]
    (is (= 0 (:exitCode cached)))
    (is (= "" (:stderr cached)))
    (is (= :blocked (:status report)))
    (is (= :passed (:status (report-check report :app-server-initialize))))
    (is (= :passed (:status (report-check report [:clojure-runtime "clojure"]))))
    (is (= :blocked (:status dependency)))
    (is (= "Configured Clojure aliases are undeclared" (:message dependency)))
    (is (= 0 (get-in dependency [:details :result :exitCode])))
    (is (= "/fixture/resolved-classpath" (get-in dependency [:details :result :stdout])))
    (is (= before @(:state ctx)))
    (is (empty? @(:processes ctx)))
    (is (not-any? #(contains? #{"thread/start" "turn/start"} (:method %)) (rpc-messages ctx)))))

(deftest malformed-command-results-retain-successful-initialization
  (doseq [mode ["command-malformed" "command-nonmap" "command-exit-type" "command-stdout-type" "command-stderr-type"]]
    (testing mode
      (let [ctx (context mode)
            before @(:state ctx)
            failure (hosted-checks #(try (main/doctor! ctx) (catch Exception e e)))
            report (ex-data failure)
            command (report-check report :sandbox-command)]
        (is (= :blocked (:status report)))
        (is (= :passed (:status (report-check report :app-server-initialize))))
        (is (= :blocked (:status command)))
        (is (= "App-server command returned a malformed result" (:message command)))
        (is (= "command/exec" (get-in command [:details :method])))
        (is (false? (get-in command [:details :writer-ended?])))
        (is (= :skipped (:status (report-check report :configured-dependencies))))
        (is (= ["initialize" "initialized" "command/exec"] (mapv :method (rpc-messages ctx))))
        (is (= before @(:state ctx)))
        (is (empty? @(:processes ctx)))))))

(deftest malformed-command-results-preserve-an-observed-refusal
  (let [ctx (context "command-malformed-approval")
        failure (hosted-checks #(try (main/doctor! ctx) (catch Exception e e)))
        report (ex-data failure)
        command (report-check report :sandbox-command)]
    (is (= :passed (:status (report-check report :app-server-initialize))))
    (is (= :blocked (:status command)))
    (is (true? (get-in command [:details :restricted])))
    (is (some #(= {:decision "decline"} (:result %)) (rpc-messages ctx)))
    (is (thrown? Exception (rt/assert-authorized! ctx :check (codex/check-key ctx ["git" "--version"]))))
    (is (empty? @(:processes ctx)))))

(deftest readiness-never-runs-an-unsupported-configured-check
  (let [ctx (assoc-in (context "doctor") [:config :checks] [["unexpected-launcher" "mutate-source"]])
        failure (hosted-checks #(try (main/doctor! ctx) (catch Exception e e)))
        report (ex-data failure)
        commands (filter #(= "command/exec" (:method %)) (rpc-messages ctx))]
    (is (= :blocked (:status (report-check report :configured-dependencies))))
    (is (= [["git" "--version"]] (mapv #(get-in % [:params :command]) commands)))
    (is (empty? (:effects @(:state ctx))))))

(deftest incomplete-sandbox-output-does-not-establish-readiness
  (let [ctx (context "stream-capped")
        failure (hosted-checks #(try (main/doctor! ctx) (catch Exception e e)))
        report (ex-data failure)]
    (is (= :passed (:status (report-check report :app-server-initialize))))
    (is (= :blocked (:status (report-check report :sandbox-command))))
    (is (= :skipped (:status (report-check report :configured-dependencies))))
    (is (= 1 (count (filter #(= "command/exec" (:method %)) (rpc-messages ctx)))))
    (is (empty? @(:processes ctx)))))

(deftest readiness-requires-observed-repository-delivery-permission
  (let [ctx (context "doctor")
        failure (hosted-checks
                  #(with-redefs [gh/api (fn [& _] {:full_name "fixture/repo" :permissions {:pull true :push false}})]
                     (try (main/doctor! ctx) (catch Exception e e))))]
    (is (= :blocked (:status (report-check (ex-data failure) :github-repository))))
    (is (= :passed (:status (report-check (ex-data failure) :sandbox-command))))))
