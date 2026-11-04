(ns seedless.main
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [seedless.github :as gh]
            [seedless.workflow :as workflow]
            [simplify.main :as shared]
            [simplify.workflow :as shared-workflow]
            [simplify.runtime :as rt]))

(def completion-check ["clojure" "-M:gravity" "self-host" "verify"])
(def boundary-check ["clojure" "-M:test" "--namespace" "gravity.self-hosting.sh01-language-boundary-test"])

(defn config! [path]
  (let [config (shared/config! path)]
    (when-not (and (string? (:objective config)) (not (str/blank? (:objective config)))
                   (vector? (:contracts config)) (seq (:contracts config))
                   (some #{completion-check} (:completion-checks config))
                   (some #{boundary-check} (:checks config)))
      (rt/fail! "Seedless needs its objective, contracts, boundary check and canonical self-host verifier" {}))
    (doseq [path (:contracts config)] (shared-workflow/safe-path! path))
    (doseq [argv (concat (:checks config) (:completion-checks config))]
      (when-not (and (vector? argv) (seq argv) (every? string? argv)
                     (contains? (:check-executables config) (first argv)))
        (rt/fail! "Configured check is outside the toolset" {:command argv})))
    (assoc config :client-info {:name "uniform_seedless" :title "Uniform Seedless" :version "0.1.0"})))

(defn context! [config]
  (let [directory (io/file (:state-dir config)) _ (.mkdirs directory)
        path (str (io/file directory "state.edn"))
        identity (select-keys config [:repository :campaign :objective :contracts :completion-checks])
        state (if (.exists (io/file path)) (rt/read-edn path)
                  {:schema :seedless/v1 :identity identity :status :new :tasks {} :effects {} :jobs {}})
        _ (when-not (and (= :seedless/v1 (:schema state)) (= identity (:identity state)))
            (rt/fail! "State belongs to a different Seedless campaign/configuration" {}))
        ctx {:config config :state-dir (str directory) :state-file path :state (atom state)
             :control-file (str (io/file directory "control.edn"))
             :log-file (str (io/file directory "events.jsonl")) :log-lock (Object.) :processes (atom #{})}
        common (shared-workflow/common-directory ctx (:repo-dir config))]
    ;; Deliberately share Simplify's existing repository lock. Never take it over.
    (assoc ctx :lock-file (str (io/file common "simplify-harness.lock")))))

(defn retry! [ctx key file]
  (let [inspection (rt/read-edn file)]
    (when-not (and (string? (:evidence inspection)) (not (str/blank? (:evidence inspection))))
      (rt/fail! "Retry needs a record of the inspected interruption/failure" {}))
    (if (= "audit" key)
      (let [planning (:planning @(:state ctx))
            effect (get-in @(:state ctx) [:effects (:effect planning)])]
        (when (or (= :restricted (:status effect)) (get-in @(:state ctx) [:completion :restricted]))
          (rt/fail! "Restricted planning/completion actions cannot be retried" {:restricted true}))
        (when-not (or planning (= :failed (get-in @(:state ctx) [:completion :result])))
          (rt/fail! "No inspected planning/completion failure is pending" {}))
        (rt/update! ctx #(-> % (update :inspections (fnil conj []) {:planning planning :completion (:completion %) :inspection inspection})
                             (assoc :round (max (or (:round %) 0) (or (:round planning) 0)) :planning nil)
                             (dissoc :completion))))
      (let [task (workflow/task ctx key)]
        (when-not (= :attention (:status task)) (rt/fail! "Only Attention work needs an inspected retry" {}))
        (when (get-in task [:error :details :restricted])
          (rt/fail! "Restricted work cannot be retried" {:restricted true}))
        (rt/update! ctx update-in [:tasks key :history] (fnil conj []) {:error (:error task) :inspection inspection})
        (workflow/task! ctx key :feedback inspection
                        :status (if (#{:ci :merge} (:resume-status task)) :ci :repair))))))

(defn dispatch! [ctx command args]
  (case command
    "doctor" (shared/doctor! ctx)
    "status" (println (pr-str @(:state ctx)))
    "pause" (rt/write-edn! (:control-file ctx) {:paused true})
    "setup" (rt/with-lock ctx #(do (gh/setup! ctx) (gh/status! ctx (workflow/campaign-issue! ctx) "Proposed")))
    "run" (rt/with-lock ctx #(if (and (= :complete (:status @(:state ctx)))
                                      (= (get-in @(:state ctx) [:completion :base]) (workflow/fresh-main! ctx)))
                               :complete
                               (workflow/campaign! ctx)))
    "resume" (rt/with-lock ctx #(do (rt/write-edn! (:control-file ctx) {:paused false}) (workflow/campaign! ctx)))
    "retry" (rt/with-lock ctx #(retry! ctx (first args) (second args)))
    "reconcile" (rt/with-lock ctx #(shared/reconcile! ctx (first args) (second args)))
    (rt/fail! "Usage: clojure -M:run doctor|setup|run|status|pause|resume|retry|reconcile CONFIG [KEY INSPECTION.edn | EFFECT RESULT.edn]" {})))

(defn -main [& [command path & args]]
  (try
    (when-not path (rt/fail! "Provide a command and configuration file" {}))
    (let [ctx (context! (config! path))]
      (.addShutdownHook (Runtime/getRuntime)
                        (Thread. ^Runnable
                         (fn []
                           (doseq [process @(:processes ctx)]
                             (with-open [children (.descendants ^Process process)]
                               (doseq [child (.toArray children)] (.destroy ^java.lang.ProcessHandle child)))
                             (.destroy ^Process process)))))
      (try
        (rt/event! ctx :startup {:command command :config (dissoc (:config ctx) :model :models)})
        (dispatch! ctx command args)
        (catch Exception e
          (when (#{"run" "resume"} command) (rt/update! ctx assoc :status :attention))
          (rt/event! ctx :fatal {:message (.getMessage e) :details (ex-data e)})
          (throw e))))
    (catch Exception e
      (binding [*out* *err*] (println (.getMessage e)))
      (shutdown-agents)
      (System/exit 1)))
  (shutdown-agents))
