(ns simplify.main
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [simplify.github :as gh]
            [simplify.runtime :as rt]
            [simplify.workflow :as workflow])
  (:gen-class))

(def defaults
  {:repository "burn2delete/uniform" :repo-dir "../.." :remote "origin" :base-branch "main"
   :project-title "Uniform Simplify" :campaign "Uniform simplification"
   :state-dir "../../target/validation/simplify" :worktree-root "../../../uniform-simplify-worktrees"
   :scope ["."] :parallelism 3 :poll-ms 15000 :check-timeout-ms 1200000
   :codex-command ["codex" "app-server" "--listen" "stdio://"]
   :check-executables #{"clojure"}
   :checks [["clojure" "-M:test" "--namespace" "gravity.self-hosting.sh01-language-boundary-test"]]})

(defn config! [path]
  (let [file (io/file (rt/canonical path))
        parent (.getParentFile file)
        raw (merge defaults (rt/read-edn file))
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
    config))

(defn read-state! [config path]
  (let [state (if (.exists (io/file path)) (rt/read-edn path)
                  {:schema 1 :repository (:repository config) :campaign (:campaign config)
                   :status :new :tasks {} :effects {} :jobs {} :rounds {}})]
    (when-not (and (= 1 (:schema state)) (= (:repository config) (:repository state))
                   (= (:campaign config) (:campaign state)))
      (rt/fail! "State belongs to another campaign or schema" {}))
    state))

(defn context! [config]
  (let [directory (io/file (:state-dir config))
        _ (.mkdirs directory)
        path (str (io/file directory "state.edn"))
        ctx {:config config :state-dir (str directory) :state-file path :control-file (str (io/file directory "control.edn"))
             :log-file (str (io/file directory "events.jsonl")) :log-lock (Object.) :processes (atom #{})
             :state (atom (read-state! config path))}]
    (merge ctx (rt/coordination (workflow/common-directory ctx (:repo-dir config)) config))))

(defn with-current-state [ctx f]
  (rt/with-lock ctx
                #(do (reset! (:state ctx) (read-state! (:config ctx) (:state-file ctx)))
                     (when (:coordination-file ctx) (rt/import-state-coordination! ctx))
                     (f))))

(defn controller! [ctx f]
  (with-current-state
    ctx
    #(let [hook (Thread. ^Runnable
                         (fn []
                           ;; Stop only known owned roots. Descendant coverage is not inferred.
                           (doseq [process @(:processes ctx)] (.destroy ^Process process))
                           (rt/event! ctx :controller-shutdown
                                      {:descendant-coverage :unknown :inspection-required true})))]
       (.addShutdownHook (Runtime/getRuntime) hook)
       (try (f)
            (catch Exception e
              ;; This callback still owns the state lock; contenders cannot write here.
              (rt/update! ctx assoc :status :attention)
              (throw e))
            (finally
              (try (.removeShutdownHook (Runtime/getRuntime) hook)
                   (catch IllegalStateException _ nil)))))))

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
    "setup" (with-current-state ctx #(do (gh/setup! ctx) (gh/status! ctx (workflow/ensure-campaign! ctx) "Proposed")))
    "coordinate" (with-current-state ctx #(println "Preserved state registered with shared coordination; no workers or GitHub writes."))
    "run" (controller! ctx #(when-not (= :complete (:status @(:state ctx))) (workflow/campaign! ctx)))
    "resume" (controller! ctx #(when-not (= :complete (:status @(:state ctx)))
                                 (rt/write-edn! (:control-file ctx) {:paused false}) (workflow/campaign! ctx)))
    "retry" (with-current-state ctx
                         #(let [key (first args) t (workflow/task ctx key)]
                            (when-not (= :attention (:status t)) (rt/fail! "Only an inspected Attention finding can be retried" {:finding key}))
                            (when (true? (get-in t [:error :details :restricted]))
                              (rt/fail! "Restricted actions are retained as denied history and cannot be retried" {:finding key}))
                            (workflow/task! ctx key :feedback (:error t) :status :repair)
                            (println "Finding queued for repair. Use resume to continue.")))
    "reconcile" (with-current-state ctx #(reconcile! ctx (first args) (second args)))
    "reconcile-main" (with-current-state ctx #(rt/reconcile-main-merge! ctx (first args) (rt/read-edn (second args))))
    "release-writer" (with-current-state ctx #(rt/inspect-release-paths! ctx (rt/read-edn (first args))
                                                                        (:evidence (rt/read-edn (second args)))))
    (rt/fail! "Usage: clojure -M:run doctor|setup|coordinate|run|resume|pause|status|retry|reconcile|reconcile-main|release-writer CONFIG.edn [KEY FILE]" {})))

(defn -main [& [command config-path & args]]
  (try
    (when-not config-path (rt/fail! "Provide a command and a configuration path; see README.md" {}))
    (let [ctx (context! (config! config-path))]
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
