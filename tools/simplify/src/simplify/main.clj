(ns simplify.main
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [simplify.github :as gh]
            [simplify.runtime :as rt]
            [simplify.workflow :as workflow])
  (:import [java.lang ProcessHandle])
  (:gen-class))

(def defaults
  {:repository "burn2delete/uniform" :repo-dir "../.." :remote "origin" :base-branch "main"
   :project-title "Uniform Simplify" :campaign "Uniform simplification"
   :state-dir "../../target/validation/simplify" :worktree-root "../../../uniform-simplify-worktrees"
   :scope ["."] :parallelism 3 :poll-ms 15000 :check-timeout-ms 1200000
   :codex-command ["codex" "app-server" "--listen" "stdio://"]
   :check-executables #{"clojure"}
   :checks [["clojure" "-M:test" "--namespace" "gravity.self-hosting.sh01-language-boundary-test"]]})

(defn config!
  ([path] (config! path defaults))
  ([path configuration-defaults]
  (let [file (io/file (rt/canonical path))
        parent (.getParentFile file)
        raw (merge configuration-defaults (rt/read-edn file))
        config (reduce (fn [m key]
                         (let [value (io/file (get m key))]
                           (assoc m key (rt/canonical (if (.isAbsolute value) value (io/file parent (str value)))))))
                       raw [:repo-dir :state-dir :worktree-root])]
    (when-not (and (pos-int? (:parallelism config)) (pos-int? (:poll-ms config))
                   (vector? (:scope config)) (seq (:scope config))
                   (seq (:checks config)) (seq (:codex-command config))
                   (re-matches #"[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+" (:repository config)))
      (rt/fail! "Invalid harness configuration" {}))
    (when (workflow/covers? (:repo-dir config) (:worktree-root config))
      (rt/fail! "Worker worktrees must be outside the source checkout" {}))
    config)))

(defn context! [config]
  (let [directory (io/file (:state-dir config))
        _ (.mkdirs directory)
        path (str (io/file directory "state.edn"))
        ctx {:config config :state-dir (str directory) :state-file path :control-file (str (io/file directory "control.edn"))
             :log-file (str (io/file directory "events.jsonl")) :log-lock (Object.) :processes (atom #{}) :coordinator-owner (atom false)
             :state (atom (if (.exists (io/file path)) (rt/read-edn path)
                              {:schema 1 :repository (:repository config) :campaign (:campaign config) :status :new :tasks {} :effects {} :jobs {} :rounds {}}))}
        _ (when-not (and (= 1 (:schema @(:state ctx))) (= (:repository config) (:repository @(:state ctx)))
                         (= (:campaign config) (:campaign @(:state ctx))))
            (rt/fail! "State belongs to another campaign or schema" {}))
        git-dir (rt/git ctx (:repo-dir config) "rev-parse" "--git-common-dir")
        lock-dir (io/file git-dir)
        lock-dir (if (.isAbsolute lock-dir) lock-dir (io/file (:repo-dir config) git-dir))
        result (assoc ctx :lock-file (str (io/file lock-dir (get config :coordinator-lock "simplify-harness.lock")))
                          :coordinator-owner (atom false)
                          :coordination-dir (rt/canonical (io/file lock-dir "uniform-harness")))]
    (doseq [[key details] (:restricted-commands @(:state ctx))] (rt/record-denial! result :command key details))
    (doseq [[key details] (:restricted-actions @(:state ctx))] (rt/record-denial! result :github key details))
    result))

(defn shutdown! [ctx]
  (doseq [process @(:processes ctx)]
    (with-open [descendants (.descendants ^Process process)]
      (doseq [handle (.toArray descendants)] (.destroy ^ProcessHandle handle)))
    (.destroy ^Process process))
  (when (and (some-> (:coordinator-owner ctx) deref) (#{:running :waiting} (:status @(:state ctx))))
    (rt/update! ctx assoc :status :attention)))

(defn doctor! [ctx]
  (doseq [argv [["git" "--version"] ["gh" "--version"] ["codex" "--version"] ["codex" "login" "status"]]]
    (rt/run! ctx (get-in ctx [:config :repo-dir]) argv))
  (let [owner (gh/owner-info ctx)]
    (gh/projects ctx (:id owner)))
  (rt/event! ctx :doctor-ok {:repository (get-in ctx [:config :repository])
                            :note "Read-only connectivity check; no worker started and no project content changed."}))

(defn reconcile! [ctx effect-id result-file]
  (let [effect (get-in @(:state ctx) [:effects effect-id])
        result (rt/read-edn result-file)]
    (when-not (and (or (#{:pending :uncertain} (:status effect))
                      (and (nil? effect) (some #{effect-id} (vals (:tracking-intents @(:state ctx))))))
                   (string? (:evidence result))
                   (not (str/blank? (:evidence result))) (contains? result :result))
      (rt/fail! "Reconciliation needs an uncertain effect and inspected external result with evidence" {}))
    (rt/update! ctx assoc-in [:effects effect-id] {:status :done :result (:result result) :reconciliation (:evidence result)})
    (rt/event! ctx :effect-reconciled {:effect effect-id :evidence (:evidence result)})))

(defn dispatch! [ctx command args]
  (case command
    "doctor" (doctor! ctx)
    "status" (println (pr-str @(:state ctx)))
    "pause" (do (rt/write-edn! (:control-file ctx) {:paused true}) (println "Dispatch and merges will pause at the next scheduler boundary; active workers finish."))
    "setup" (rt/with-lock ctx #(do (gh/setup! ctx) (gh/status! ctx (workflow/ensure-campaign! ctx) "Proposed")))
    "run" (rt/with-lock ctx #(when-not (= :complete (:status @(:state ctx))) (workflow/campaign! ctx)))
    "resume" (rt/with-lock ctx #(when-not (= :complete (:status @(:state ctx)))
                                 (rt/write-edn! (:control-file ctx) {:paused false}) (workflow/campaign! ctx)))
    "retry" (rt/with-lock ctx
                         #(let [key (first args) t (workflow/task ctx key)]
                            (when-not (= :attention (:status t)) (rt/fail! "Only an inspected Attention finding can be retried" {:finding key}))
                            (when (true? (get-in t [:error :details :restricted]))
                              (rt/fail! "Restricted actions are retained as denied history and cannot be retried" {:finding key}))
                            (workflow/task! ctx key :feedback (:error t) :status :repair)
                            (println "Finding queued for repair. Use resume to continue.")))
    "reconcile" (rt/with-lock ctx #(reconcile! ctx (first args) (second args)))
    (rt/fail! "Usage: clojure -M:run doctor|setup|run|resume|pause|status|retry|reconcile CONFIG.edn [FINDING | EFFECT-ID RESULT.edn]" {})))

(defn -main [& [command config-path & args]]
  (try
    (when-not config-path (rt/fail! "Provide a command and a configuration path; see README.md" {}))
    (let [ctx (assoc (context! (config! config-path)) :command command)]
      (.addShutdownHook (Runtime/getRuntime)
                        (Thread. ^Runnable
                                 (fn [] (shutdown! ctx))))
      (rt/event! ctx :startup {:command command :config (dissoc (:config ctx) :model)})
      (try (dispatch! ctx command args)
           (catch Exception e
             (rt/event! ctx :fatal {:message (.getMessage e) :data (ex-data e)})
             (throw e))))
    (catch Exception e
      (binding [*out* *err*] (println (.getMessage e)))
      (shutdown-agents)
      (System/exit 1)))
  (shutdown-agents))
