(ns advance.main
  (:require [advance.catalog :as catalog]
            [advance.github :as project]
            [advance.workflow :as workflow]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [simplify.coordination :as coordination]
            [simplify.main :as base]
            [simplify.runtime :as rt])
  (:gen-class))

(def defaults
  (merge base/defaults
    {:harness-id "advance" :coordinator-lock "advance-harness.lock"
     :tracking-ref "refs/advance/main" :branch-prefix "codex/advance-"
     :project-title "Uniform Implementation" :campaign "Uniform project completion"
     :state-dir "../../target/validation/advance" :worktree-root "../../../uniform-advance-worktrees"
     :early-draft true :repair-github-review true :wait-for-ci true :priorities [] :work-label "implementation"
     :labels [["implementation" "0e8a16" "Spec Kit implementation tracking"]
              ["implementation:campaign" "1d76db" "Project completion objective"]
              ["implementation:task" "bfdadc" "Repository-owned implementation outcome"]
              ["implementation:increment" "c5def5" "Independently reviewable implementation increment"]]
     :project-description "Spec Kit implementation: select, draft, implement, review, verify, merge, repeat."
     :project-readme "# Uniform Implementation\n\nRepository specs/**/tasks.md retain requirements and completion authority. Parent issues mirror current outcomes; child issues are independently useful increments. A merged child does not imply parent completion.\n\nPriority controls selection order. Control = Paused or Blocked holds dispatch and integration for that issue and its children; on the campaign it holds the whole campaign. Active workers finish. Status reports execution, separately from Control.\n\nThe controller owns the full merge loop. It shares persistent path claims and a main-write lock with tools/simplify. Interrupted, restricted and uncertain work retains its identity and claims for inspection.\n\nAn empty actionable queue means waiting. Completion requires all current tasks accepted, no unclassified/missing outcomes, successful actual release checks, and independent assessment of the canonical language, self-hosting and seedless-release evidence on still-current main.\n\n"
     :project-views [["All work" "TABLE_LAYOUT" "label:implementation"]
                     ["Workflow" "BOARD_LAYOUT" "label:implementation"]
                     ["Outcomes" "TABLE_LAYOUT" "label:implementation:task"]
                     ["Attention" "TABLE_LAYOUT" "label:implementation status:Attention"]]
     :worker-instructions workflow/instructions
     :review-focus "Check the selected source task, declared increment, task/evidence updates, actual dependencies and completion nonclaims. "
     :completion-checks [["clojure" "-M" "tools/validate_gravity_docs.clj"]
                         ["clojure" "-M" "tools/validate_full_language_roadmap.clj"]
                         ["clojure" "-M:gravity" "self-host" "verify"]]}))

(defn- effect-ids [task]
  (->> [(:effect task) (:effect-id task)
        (get-in task [:error :effect])
        (get-in task [:error :details :effect])]
       (mapcat #(cond
                  (nil? %) []
                  (sequential? %) %
                  :else [%]))
       distinct
       vec))

(defn- task-error-summary [task]
  (let [error (:error task)
        details (:details error)]
    (rt/redact (select-keys (merge (if (map? error) error {})
                                   (if (map? details) details {}))
                            [:message :effect :effect-status :restricted :uncertain]))))

(defn- task-status-summary [state state-file state-digest task]
  (let [effects (or (:effects state) {})
        ids (effect-ids task)
        linked (mapv (fn [id]
                       (let [record (get effects id)
                             status (or (:status record) :missing)]
                         {:id id
                          :status status
                          :restricted (= :restricted status)
                          :uncertain (= :uncertain status)
                          :pending (= :pending status)}))
                     ids)
        error (task-error-summary task)
        statuses (set (concat (keep :status linked)
                              [(:effect-status error)]))]
    {:key (:key task)
     :status (:status task)
     :resume-status (:resume-status task)
     :history {:restricted (or (:restricted error)
                               (contains? statuses :restricted))
               :uncertain (or (:uncertain error)
                              (contains? statuses :uncertain))
               :pending (contains? statuses :pending)}
     :error error
     :effects linked
     :record {:state-file state-file :state-sha256 state-digest :task-key (:key task)}}))

(defn- job-status-summary [state-file state-digest [job record]]
  {:job job
   :status (:status record)
   :mode (:mode record)
   :thread (:thread record)
   :turn (:turn record)
   :record {:state-file state-file :state-sha256 state-digest :job-key job}})

(defn status-summary
  "Build a compact, observational projection from one already-read state.

  This function intentionally reports recorded state only. It does not inspect
  processes, acquire a coordinator lock, import shared coordination state, or
  call a provider."
  [config state state-digest control]
  (let [state-file (str (io/file (:state-dir config) "state.edn"))
        tasks (->> (vals (or (:tasks state) {})) (sort-by :key) vec)
        jobs (->> (or (:jobs state) {})
                  (sort-by (comp str key))
                  (mapv #(job-status-summary state-file state-digest %)))
        attention (->> tasks (filter #(= :attention (:status %)))
                       (mapv #(task-status-summary state state-file state-digest %)))
        statuses (frequencies (map :status tasks))]
    {:schema (:schema state)
     :repository (:repository state)
     :campaign (:campaign state)
     :state {:file state-file
             :sha256 state-digest
             :recorded-status (:status state)
             :recorded-round (:round state)
             :catalog (select-keys (:catalog state) [:base :tree])
             :task-count (count tasks)
             :counts statuses}
     :attention attention
     :jobs {:count (count jobs) :records jobs}
     :operator-controls {:local-control control
                         :github {:status :unavailable
                                  :reason :no-provider-read-in-read-only-summary}}
     :liveness {:processes :unverified
                :service :unverified
                :basis :recorded-local-state-only}}))

(defn- read-status-state! [config]
  (let [state-file (io/file (:state-dir config) "state.edn")]
    (when-not (.isFile state-file)
      (rt/fail! "Advance status summary requires an existing state file"
                {:path (str state-file) :missing true}))
    (let [raw (try (slurp state-file)
                   (catch Exception e
                     (rt/fail! "Advance status summary cannot read state file"
                               {:path (str state-file) :cause (.getMessage e)})))
          digest (rt/digest raw)
          state (try (edn/read-string {:readers {}
                                       :default (fn [tag _]
                                                  (rt/fail! "Unsupported EDN tag in Advance status state"
                                                            {:tag tag}))}
                                      raw)
                     (catch Exception e
                       (rt/fail! "Advance status summary found malformed state"
                                 {:path (str state-file) :cause (.getMessage e)})))]
      (when-not (and (map? state)
                     (= 1 (:schema state))
                     (string? (:repository state))
                     (string? (:campaign state))
                     (contains? state :tasks) (map? (:tasks state))
                     (contains? state :jobs) (map? (:jobs state))
                     (contains? state :effects) (map? (:effects state))
                     (every? (fn [field] (every? map? (vals (get state field))))
                             [:tasks :jobs :effects]))
        (rt/fail! "Advance status summary found malformed state shape"
                  {:path (str state-file)
                   :fields (select-keys state [:schema :repository :campaign :tasks :jobs :effects])}))
      (when-not (and (= 1 (:schema state))
                     (= (:repository config) (:repository state))
                     (= (:campaign config) (:campaign state)))
        (rt/fail! "Advance status summary found state for another campaign"
                  {:path (str state-file)
                   :expected {:schema 1 :repository (:repository config) :campaign (:campaign config)}
                   :actual (select-keys state [:schema :repository :campaign])}))
      {:state state :digest digest})))

(defn- read-status-control [config]
  (let [control-file (io/file (:state-dir config) "control.edn")]
    (if-not (.isFile control-file)
      {:file (str control-file) :available false :status :unavailable}
      (try
        (let [value (rt/read-edn control-file)]
          (if (and (map? value) (contains? value :paused) (boolean? (:paused value)))
            {:file (str control-file) :available true
             :status (if (:paused value) :paused :active)
             :paused (:paused value)}
            {:file (str control-file) :available true :status :malformed
             :error {:message "Control file must contain boolean :paused"}}))
        (catch Exception e
          {:file (str control-file) :available true :status :malformed
           :error (rt/redact {:message (.getMessage e)})})))))

(defn status-summary! [config]
  (let [{:keys [state digest]} (read-status-state! config)]
    (status-summary config state digest (read-status-control config))))

(defn dispatch! [ctx command args]
  (case command
    "catalog" (println (pr-str (catalog/index (catalog/discover (get-in ctx [:config :repo-dir])))))
    "setup" (rt/with-lock ctx #(do (coordination/register! ctx)
                                 (try (project/setup! ctx) (workflow/ensure-campaign! ctx)
                                      (finally (coordination/unregister! ctx)))))
    "run" (rt/with-lock ctx #(when-not (= :complete (:status @(:state ctx))) (workflow/campaign! ctx)))
    "resume" (rt/with-lock ctx #(when-not (= :complete (:status @(:state ctx)))
                                 (rt/write-edn! (:control-file ctx) {:paused false}) (workflow/campaign! ctx)))
    (base/dispatch! ctx command args)))

(defn -main [& [command config-path & args]]
  (try
    (when-not config-path (rt/fail! "Usage: clojure -M:run doctor|catalog|setup|run|resume|pause|status|status-summary|retry|reconcile CONFIG.edn [ARGS]" {}))
    (let [config (base/config! config-path defaults)]
      (if (= command "status-summary")
        (do
          (when (seq args) (rt/fail! "status-summary takes no additional arguments" {:args args}))
          (println (pr-str (status-summary! config))))
        (let [ctx (assoc (base/context! config) :before-implement workflow/validate-source! :command command)]
          (.addShutdownHook (Runtime/getRuntime)
            (Thread. ^Runnable
              (fn [] (base/shutdown! ctx))))
          (rt/event! ctx :startup {:command command :config (dissoc (:config ctx) :worker-instructions)})
          (try (dispatch! ctx command args)
               (catch Exception e
                 (rt/event! ctx :fatal {:message (.getMessage e) :data (ex-data e)})
                 (throw e))))))
    (catch Exception e
      (binding [*out* *err*] (println (.getMessage e)))
      (shutdown-agents)
      (System/exit 1)))
  (shutdown-agents))
