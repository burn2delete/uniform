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

(defn probe! [ctx argv]
  (let [result (rt/command ctx (get-in ctx [:config :repo-dir]) argv)]
    (when-not (zero? (:exit result))
      (let [restricted (rt/authorization-denied? (str (:err result) "\n" (:out result)))]
        (rt/event! ctx :startup-probe-failed {:argv argv :restricted restricted})
        (rt/fail! "Read-only startup probe failed" (assoc result :argv argv :restricted restricted))))
    (str/trim (:out result))))

(defn repository-name [url]
  (second (re-matches #"(?:https://github\.com/|git@github\.com:|ssh://git@github\.com/)([A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+?)(?:\.git)?/?" url)))

(defn target! [ctx]
  (let [config (:config ctx)
        _ (probe! ctx ["git" "check-ref-format" "--branch" (:base-branch config)])
        root (rt/canonical (probe! ctx ["git" "rev-parse" "--show-toplevel"]))
        _ (when-not (= root (:repo-dir config)) (rt/fail! "Repository directory must name its checkout root" {}))
        fetch-url (probe! ctx ["git" "remote" "get-url" (:remote config)])
        push-url (probe! ctx ["git" "remote" "get-url" "--push" (:remote config)])
        _ (doseq [url [fetch-url push-url]]
            (when-not (= (str/lower-case (:repository config))
                         (some-> (repository-name url) str/lower-case))
              (rt/fail! "Git remote and GitHub repository must identify the same target" {})))
        common (io/file (probe! ctx ["git" "rev-parse" "--git-common-dir"]))]
    (assoc (select-keys config [:repository :campaign :objective :contracts :completion-checks
                                :repo-dir :remote :base-branch :worktree-root :checks :project-title :codex-command])
           :git-common-dir (rt/canonical (if (.isAbsolute common) common (io/file root (str common)))))))

(defn config! [path]
  (let [config (shared/config! path)]
    (when-not (and (string? (:objective config)) (not (str/blank? (:objective config)))
                   (vector? (:contracts config)) (seq (:contracts config))
                   (some #{completion-check} (:completion-checks config))
                   (some #{boundary-check} (:checks config)))
      (rt/fail! "Seedless needs its objective, contracts, boundary check and canonical self-host verifier" {}))
    (when-not (and (string? (:remote config)) (re-matches #"[A-Za-z0-9][A-Za-z0-9._-]*" (:remote config))
                   (string? (:base-branch config)) (not (str/blank? (:base-branch config)))
                   (pos-int? (:check-timeout-ms config))
                   (vector? (:codex-command config))
                   (= ["codex" "app-server"] (vec (take 2 (:codex-command config))))
                   (every? #(and (string? %) (not (str/blank? %))) (:codex-command config)))
      (rt/fail! "Seedless needs a named Git target, a check watchdog and the Codex app-server command" {}))
    (doseq [path (:contracts config)] (shared-workflow/safe-path! path))
    (doseq [argv (concat (:checks config) (:completion-checks config))]
      (when-not (and (vector? argv) (seq argv) (every? string? argv)
                     (contains? (:check-executables config) (first argv)))
        (rt/fail! "Configured check is outside the toolset" {:command argv})))
    (assoc config :client-info {:name "uniform_seedless" :title "Uniform Seedless" :version "0.1.0"})))

(defn context! [config]
  (let [directory (io/file (:state-dir config)) _ (.mkdirs directory)
        path (str (io/file directory "state.edn"))
        startup {:config config :state-dir (str directory) :state-file path
                 :log-file (str (io/file directory "events.jsonl")) :log-lock (Object.) :processes (atom #{})}
        identity (target! startup)
        state (if (.exists (io/file path)) (rt/read-edn path)
                  {:schema :seedless/v1 :identity identity :status :new :tasks {} :effects {} :jobs {}})
        _ (when-not (and (= :seedless/v1 (:schema state)) (= identity (:identity state)))
            (rt/fail! "State belongs to a different Seedless campaign/configuration" {}))
        ctx (assoc startup :identity identity :state (atom state) :owns-lock? (atom false)
                   :control-file (str (io/file directory "control.edn")))
        common (:git-common-dir identity)]
    (merge ctx (rt/coordination common config))))

(defn owned-cleanup! [ctx reason]
  (when (and (:owns-lock? ctx) @(:owns-lock? ctx))
    (let [processes @(:processes ctx)
          observation {:reason reason :time (str (java.time.Instant/now))
                       :known-processes (mapv #(.pid ^Process %) processes)
                       :descendant-coverage :unknown :inspection-required true}]
      ;; No global/descendant discovery: signal only process objects this invocation owns.
      (rt/update! ctx assoc :status :attention :interruption observation)
      (try
        (doseq [process processes] (.destroy ^Process process))
        (rt/event! ctx :owned-cleanup observation)
        (catch Exception e
          (let [restricted (or (instance? SecurityException e) (rt/authorization-denied? (.getMessage e)))]
            (rt/update! ctx update :interruption assoc :restricted (boolean restricted) :message (.getMessage e))
            (rt/event! ctx :owned-cleanup-failed {:restricted (boolean restricted) :message (.getMessage e)})))))))

(defn with-current-lock [ctx operation]
  (rt/with-lock ctx
    #(do
       (when (.exists (io/file (:state-file ctx)))
         (let [state (rt/read-edn (:state-file ctx))]
           (when-not (and (= :seedless/v1 (:schema state)) (= (:identity ctx) (:identity state)))
             (rt/fail! "State identity changed before lock ownership" {}))
           (reset! (:state ctx) state)))
       (rt/import-state-coordination! ctx)
       (when (:owns-lock? ctx) (reset! (:owns-lock? ctx) true))
       (try (operation)
            (finally
              (when (seq @(:processes ctx)) (owned-cleanup! ctx :operation-exit))
              (when (:owns-lock? ctx) (reset! (:owns-lock? ctx) false)))))))

(defn run-campaign! [ctx resume?]
  (with-current-lock ctx
    #(try
       (doseq [t (workflow/tasks ctx)] (workflow/assert-no-pending-main! ctx t))
       (when (:interruption @(:state ctx)) (rt/fail! "Inspect retained controller interruption before recovery" {}))
       (when resume? (rt/write-edn! (:control-file ctx) {:paused false}))
       (if (and (= :complete (:status @(:state ctx)))
                (= (get-in @(:state ctx) [:completion :base]) (workflow/fresh-main! ctx)))
         :complete
         (workflow/campaign! ctx))
       (catch Exception e
         (rt/update! ctx assoc :status :attention)
         (throw e)))))

(defn retry! [ctx key file]
  (let [inspection (rt/read-edn file)]
    (when-not (and (string? (:evidence inspection)) (not (str/blank? (:evidence inspection))))
      (rt/fail! "Retry needs a record of the inspected interruption/failure" {}))
    (doseq [t (workflow/tasks ctx)] (workflow/assert-no-pending-main! ctx t))
    (if (= "audit" key)
      (let [planning (:planning @(:state ctx))
            interruption (:interruption @(:state ctx))
            effect (get-in @(:state ctx) [:effects (:effect planning)])]
        (when (or (= :restricted (:status effect)) (get-in @(:state ctx) [:completion :restricted])
                  (:restricted interruption))
          (rt/fail! "Restricted planning/completion actions cannot be retried" {:restricted true}))
        (when-not (or planning interruption (= :failed (get-in @(:state ctx) [:completion :result])))
          (rt/fail! "No inspected planning/completion failure is pending" {}))
        (rt/update! ctx #(-> % (update :inspections (fnil conj []) {:planning planning :completion (:completion %) :interruption interruption :inspection inspection})
                             (assoc :round (max (or (:round %) 0) (or (:round planning) 0)) :planning nil)
                             (dissoc :completion :interruption))))
      (let [task (workflow/task ctx key)]
        (when-not (= :attention (:status task)) (rt/fail! "Only Attention work needs an inspected retry" {}))
        (when (get-in task [:error :details :restricted])
          (rt/fail! "Restricted work cannot be retried" {:restricted true}))
        (doseq [id (distinct (filter string?
                                     [(get-in task [:error :details :effect])
                                      (get-in task [:error :details :effect-id])
                                      (get-in task [:reporting-error :details :effect])
                                      (get-in task [:reporting-error :details :effect-id])]))
                :let [effect (get-in @(:state ctx) [:effects id])]
                :when (not= :done (:status effect))]
          (rt/fail! "Failed task effect needs reconciliation before retry"
                    {:effect id :restricted (= :restricted (:status effect))}))
        (when (:merged task)
          (let [tracking-key (str "project-status/" (get-in @(:state ctx) [:project :id]) "/" (get-in task [:issue :node_id]))
                ids [(str "close/" key) (get-in @(:state ctx) [:tracking-intents tracking-key])]]
            (doseq [id ids :let [effect (get-in @(:state ctx) [:effects id])]
                    :when (#{:pending :uncertain :restricted} (:status effect))]
              (rt/fail! "Merged slice finalization needs effect reconciliation" {:effect id :restricted (= :restricted (:status effect))}))))
        (let [id (or (:merge-effect task) (workflow/merge-effect-id task))
              record (:inspection (rt/main-merge-state ctx id))]
          (when (and (get-in task [:error :details :main-effect]) (not (:merged task)) (nil? record))
            (rt/fail! "Main failure needs original-owner journal inspection before retry" {:effect id :main-effect true}))
          (when (= :not-applied (get-in record [:inspection :outcome]))
            (let [effect (get-in @(:state ctx) [:effects id])
                  digest (rt/digest (pr-str record))
                  restriction (rt/digest (pr-str ["gh-pr-merge" (get-in ctx [:config :repository])]))
                  next-id (str id "/inspection/" digest)]
              (when (or (= :restricted (:status effect))
                        (get-in @(:state ctx) [:restricted-commands restriction])
                        (rt/shared-restriction ctx :commands restriction))
                (rt/fail! "Original repository merge restriction forbids a successor intent" {:effect id :restricted true}))
              (when-not (and (= (assoc (select-keys task [:base :candidate :tree]) :id id :method :merge)
                                (select-keys (:intent record) [:id :base :candidate :tree :method]))
                             (= :done (:status effect)) (= digest (:main-inspection-digest effect)))
                (rt/fail! "Not-applied main inspection needs matching local effect reconciliation" {:effect id :main-effect true}))
              (when (get-in @(:state ctx) [:effects next-id])
                (rt/fail! "Inspected successor already has a retained effect; inspect that outcome" {:effect next-id :main-effect true}))
              (workflow/task! ctx key :merge-recovery (assoc (select-keys task [:base :candidate :tree]) :id next-id)))))
        (rt/update! ctx update-in [:tasks key :history] (fnil conj []) {:error (:error task) :inspection inspection})
        (workflow/task! ctx key :feedback inspection
                        :status (if (or (:merged task) (:merge-recovery (workflow/task ctx key))
                                        (#{:ci :merge} (:resume-status task))) :ci :repair))))))

(defn dispatch! [ctx command args]
  (case command
    "doctor" (with-current-lock ctx #(shared/doctor! ctx))
    "status" (println (pr-str @(:state ctx)))
    "pause" (rt/write-edn! (:control-file ctx) {:paused true})
    "setup" (with-current-lock ctx #(workflow/with-registration! ctx
                                      (fn [] (gh/setup! ctx)
                                        (gh/status! ctx (workflow/campaign-issue! ctx) "Proposed"))))
    "coordinate" (with-current-lock ctx #(println "Retained state registered; no workers or GitHub writes."))
    "run" (run-campaign! ctx false)
    "resume" (run-campaign! ctx true)
    "retry" (with-current-lock ctx #(retry! ctx (first args) (second args)))
    "reconcile" (with-current-lock ctx #(shared/reconcile! ctx (first args) (second args)))
    "reconcile-main" (with-current-lock ctx #(rt/reconcile-main-merge! ctx (first args) (rt/read-edn (second args))))
    "release-writer" (with-current-lock ctx #(rt/inspect-release-paths! ctx (rt/read-edn (first args))
                                                                        (:evidence (rt/read-edn (second args)))))
    (rt/fail! "Usage: clojure -M:run doctor|setup|coordinate|run|status|pause|resume|retry|reconcile|reconcile-main|release-writer CONFIG [KEY FILE]" {})))

(defn -main [& [command path & args]]
  (try
    (when-not path (rt/fail! "Provide a command and configuration file" {}))
    (let [ctx (context! (config! path))]
      (.addShutdownHook (Runtime/getRuntime)
                        (Thread. ^Runnable
                         (fn [] (owned-cleanup! ctx :jvm-shutdown))))
      (try
        (rt/event! ctx :startup {:command command :config (dissoc (:config ctx) :model :models)})
        (dispatch! ctx command args)
        (catch Exception e
          (rt/event! ctx :fatal {:message (.getMessage e) :details (ex-data e)})
          (throw e))))
    (catch Exception e
      (binding [*out* *err*] (println (.getMessage e)))
      (shutdown-agents)
      (System/exit 1)))
  (shutdown-agents))
