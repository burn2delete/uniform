(ns advance.workflow
  "Select real Spec Kit increments, implement, review and merge until proven complete."
  (:require [advance.catalog :as catalog]
            [advance.github :as project]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [simplify.codex :as codex]
            [simplify.coordination :as coordination]
            [simplify.github :as gh]
            [simplify.runtime :as rt]
            [simplify.schema :as schema]
            [simplify.workflow :as flow])
  (:import [java.util.concurrent Callable Executors Future TimeUnit]))

(def instructions
  (str flow/instructions
       " Your objective is current Spec Kit implementation and full project completion. "
       "Read .specify/README.md, applicable spec/plan/tasks/evidence and canonical contracts. "
       "The user delegates the full implementation/review/merge loop to this controller, including integration formerly assigned to Deus. "
       "Existing denied/restricted operations remain denied. Do not infer a grant from this delegation. "
       "Use small independently useful increments of broad tasks. Retain task identity and evidence; "
       "leave the parent open until all its acceptance criteria and independent acceptance exist. "
       "Record actual checks and scoped results in the owning feature artifacts. Historical migration checklists are history. "
       "Do not invent task or cost allowances or wait for all upstream components before useful development. "
       "Distinguish start, composition, acceptance and release dependencies; retain genuine cycles. "
       "Missing planning may be prepared within an existing task, with explicit source-backed behavior and evidence. "))

(def increment-schema
  (schema/object-of (-> (:properties schema/finding)
                       (dissoc :kind)
                       (assoc :task schema/text))))
(def selection-schema
  (schema/object-of {:summary schema/text :coverage (schema/array-of schema/text)
                     :increments (schema/array-of increment-schema)
                     :blockers (schema/array-of (schema/object-of {:task schema/text :reason schema/text}))}))
(def completion-schema
  (schema/object-of {:verdict {:type "string" :enum ["complete" "incomplete"]}
                     :base schema/text :tree schema/text :summary schema/text
                     :evidence (schema/array-of schema/text) :remaining (schema/array-of schema/text)}))

(defn ensure-campaign! [ctx]
  (when-not (:campaign-issue @(:state ctx))
    (rt/update! ctx assoc :campaign-issue
      (gh/ensure-issue! ctx "campaign/implementation" (get-in ctx [:config :campaign])
        "Implement current Spec Kit task outcomes through independently reviewed, checked and verified merges. Keep canonical requirements and task files authoritative. Continue toward the complete language, self-hosting and seedless release; an empty actionable queue is not completion."
        nil "implementation:campaign")))
  (when-not (:campaign-tracked @(:state ctx))
    (gh/status! ctx (:campaign-issue @(:state ctx)) "Proposed")
    (rt/update! ctx assoc :campaign-tracked true))
  (:campaign-issue @(:state ctx)))

(defn mirror! [ctx snapshot base]
  (doseq [task (:tasks snapshot)]
    (let [issue (or (get-in @(:state ctx) [:source-tasks (:key task) :issue])
                    (gh/ensure-issue! ctx (str "task/" (:key task)) (:title task)
                      (str "Repository task authority: `" (:key task) "`.\n\nSource at `" base "`:\n\n" (:body task)
                           "\n\nThis issue mirrors the repository outcome. Child issues are finite increments; their verified merges do not close this parent automatically.")
                      nil "implementation:task"))
          previous (get-in @(:state ctx) [:source-tasks (:key task)])]
      (rt/update! ctx assoc-in [:source-tasks (:key task)] (assoc task :issue issue))
      (when (or (nil? previous) (not= (:checked task) (:checked previous)))
        (gh/status! ctx issue (if (:checked task) "Done" "Ready")))
      (when (and previous (not= (:source-digest task) (:source-digest previous)))
        (gh/comment! ctx issue (str "source/" base)
                     (str "Current task authority at `" base "`:\n\n" (:body task))))))
  ;; Removed/reclassified records remain history, never completion evidence.
  (let [present (set (map :key (:tasks snapshot)))]
    (doseq [[key task] (:source-tasks @(:state ctx)) :when (not (present key))]
      (rt/update! ctx assoc-in [:source-tasks key :missing] true)
      (when-not (:missing task) (gh/status! ctx (:issue task) "Attention")))))

(defn snapshot! [ctx]
  (let [base (flow/fresh-main! ctx)
        wt (flow/worktree! ctx (str "catalog-" (flow/next-job! ctx :snapshot) "-" (subs base 0 12)) base)
        snapshot (catalog/discover (:worktree wt))]
    (rt/update! ctx assoc :catalog {:base base :tree (rt/git ctx (:worktree wt) "rev-parse" "HEAD^{tree}")
                                   :worktree (:worktree wt) :source (catalog/index snapshot)})
    (mirror! ctx snapshot base)
    {:base base :worktree (:worktree wt) :catalog snapshot}))

(defn validate-source! [ctx t]
  (let [current (catalog/discover (:worktree t))
        source (first (filter #(= (:task t) (:key %)) (:tasks current)))]
    (when-not (and source (not (:checked source))
                   (= (:task-digest t) (:source-digest source)))
      ;; Repair authors may have updated their own source record. Only a change
      ;; to upstream's task authority invalidates an already-authored increment.
      (let [result (rt/command ctx (:worktree t) ["git" "show" (str (:base t) ":" (:task-path t))])]
        (when-not (and (zero? (:exit result)) (= (:task-digest t) (rt/digest (:out result))))
          (rt/fail! "Source task changed on main; reselect rather than implementing a stale plan" {:task (:task t)}))))))

(defn admit! [ctx base snapshot increments]
  (catalog/validate-selection! snapshot increments (:tasks @(:state ctx)))
  (flow/validate-findings! increments (:tasks @(:state ctx)))
  (doseq [increment increments]
    (let [source (first (filter #(= (:task increment) (:key %)) (:tasks snapshot)))
          increment (update increment :paths #(mapv flow/safe-path! %))
          issue (gh/ensure-finding-issue! ctx (:key increment) (:title increment)
                  (str "Task: `" (:key source) "`.\n\nDeclared increment: " (:proposal increment)
                       "\n\nSource evidence: " (:evidence increment) "\n\nPreserve: " (:preserve increment)
                       "\n\nOwned paths: `" (pr-str (:paths increment)) "`.\n\nIndependent review, local checks, CI and actual squash verification are required. Parent acceptance remains separate.")
                  nil "implementation:increment")
          wt (flow/worktree! ctx (str "increment-" (:number issue)) base)]
      (rt/update! ctx assoc-in [:tasks (:key increment)]
                  (merge increment wt {:issue issue :base base :status :ready
                                       :task-path (:path source) :task-digest (:source-digest source)}))
      (gh/relation! ctx (get-in @(:state ctx) [:source-tasks (:key source) :issue]) "sub_issues" (:id issue))
      (flow/transition! ctx (:key increment) :ready)))
  (doseq [increment increments dep (:dependencies increment)]
    (gh/relation! ctx (:issue (flow/task ctx (:key increment))) "dependencies/blocked_by"
                  (get-in (flow/task ctx dep) [:issue :id]))))

(defn select! [ctx {:keys [base worktree catalog]} settings]
  (let [output (codex/job! ctx (flow/next-job! ctx :select) worktree :read instructions
                 (str "Select independently useful implementation increments from the current task catalog. "
                      "Read owning spec/plan/task/evidence and actual source; the index is navigation. "
                      "Prioritize configured task keys, then Project priority, actual prerequisites and work that unblocks completion. "
                      "Respect Project Control Paused/Blocked on both campaign, source tasks and increments. "
                      "Do not duplicate active, held, restricted or already merged increments. "
                      "Use a fresh stable key for each distinct increment, never just rename an unsuccessful one. "
                      "Include the source tasks.md in owned paths and name concrete accepted/rejected evidence. "
                      "Dependencies reference real increment keys only; inspect artifact-level task dependencies rather than assuming the coarse graph is acyclic. "
                      "If none are actionable, give blockers; never infer completion from an empty selection.\nBase: " base
                      "\nCatalog: " (pr-str (catalog/index catalog))
                      "\nExplicit priorities: " (pr-str (get-in ctx [:config :priorities]))
                      "\nGitHub controls by issue: " (pr-str settings)
                      "\nSource issue identities: " (pr-str (into {} (map (fn [[k v]] [k (get-in v [:issue :number])]) (:source-tasks @(:state ctx)))))
                      "\nExisting increments: " (pr-str (mapv #(select-keys % [:key :task :title :proposal :paths :status :error :merged :review :checks]) (flow/tasks ctx)))
                      "\nShared writer claims: " (pr-str (coordination/claims ctx))) selection-schema)]
    (schema/validate! selection-schema output)
    (doseq [increment (:increments output)]
      (let [number (get-in @(:state ctx) [:source-tasks (:task increment) :issue :number])]
        (when-not (project/runnable? settings number)
          (rt/fail! "Selector chose an operator-paused or blocked task" {:task (:task increment)}))))
    (doseq [blocker (:blockers output)]
      (when-not (some #(= (:task blocker) (:key %)) (:tasks catalog))
        (rt/fail! "Blocker names an unknown source task" {:blocker blocker})))
    (rt/update! ctx assoc :selection {:base base :output output})
    (gh/comment! ctx (:campaign-issue @(:state ctx)) (str "selection/" (flow/next-job! ctx :record))
                 (str "Selection at `" base "`: " (:summary output) "\n\nBlockers: " (pr-str (:blockers output))))
    (admit! ctx base catalog (:increments output))
    output))

(defn complete! [ctx {:keys [base worktree catalog]}]
  (when (and (seq (:tasks catalog)) (every? :checked (:tasks catalog))
             (empty? (:unclassified catalog))
             (every? #(= :done (:status %)) (flow/tasks ctx))
             (not-any? :missing (vals (:source-tasks @(:state ctx)))))
    (let [commands (get-in ctx [:config :completion-checks])
          tree (rt/git ctx worktree "rev-parse" "HEAD^{tree}")]
      (when (empty? commands) (rt/fail! "Project completion requires configured actual release checks" {}))
      (doseq [argv commands]
        (when-not (and (vector? argv) (seq argv) (every? string? argv)
                       (contains? (get-in ctx [:config :check-executables]) (first argv)))
          (rt/fail! "Invalid completion check tool" {:argv argv}))
        (codex/command! ctx (flow/next-job! ctx :completion-check) worktree argv))
      (flow/assert-clean! ctx worktree)
      (when-not (= base (rt/git ctx worktree "rev-parse" "HEAD")) (rt/fail! "Completion checks changed source identity" {}))
      (let [review (codex/job! ctx (flow/next-job! ctx :completion-review) worktree :read instructions
                     (str "Independently assess full project completion at exact base " base ", tree " tree
                          ". Read all roadmap/reconciliation, 240-contract, Phase 18, self-hosting and seed-retirement evidence. "
                          "All catalog checkboxes and configured checks passed; those facts alone do not prove completion. "
                          "Return incomplete for missing source-to-task coverage, acceptance, release evidence or unresolved restrictions. "
                          "Cite actual immutable evidence for every claimed final gate. Checks: " (pr-str commands)) completion-schema)]
        (schema/validate! completion-schema review)
        (when-not (= {:base base :tree tree} (select-keys review [:base :tree]))
          (rt/fail! "Completion review returned a different source identity" {}))
        (rt/update! ctx assoc :completion {:base base :tree tree :commands commands :review review})
        (and (= "complete" (:verdict review)) (seq (:evidence review)) (empty? (:remaining review))
             (= base (flow/fresh-main! ctx)))))))

(defn run-worker! [ctx key]
  (try (flow/prepare! ctx key)
       (catch Exception e
         (cond
           (:ownership-wait (ex-data e)) (flow/transition! ctx key :repair)
           (and (= "Sandboxed check failed" (.getMessage e)) (not (:restricted (ex-data e))))
           (do (flow/task! ctx key :feedback {:local-check (ex-data e) :instruction "Repair the actual failure without weakening checks."})
               (flow/transition! ctx key :repair))
           :else (flow/attention! ctx key e)))))

(defn campaign! [ctx]
  (project/setup! ctx)
  (ensure-campaign! ctx)
  (coordination/register! ctx)
  (doseq [t (flow/tasks ctx) :when (#{:implementing :review} (:status t))]
    (when (and (= :implementing (:status t)) (not (coordination/claim! ctx (:key t) (:paths t))))
      (rt/fail! "Retained increment conflicts with another writer" {:key (:key t)}))
    (when (#{:implementing :review} (:status t))
      (flow/attention! ctx (:key t) (ex-info "Interrupted worker requires inspection; preserving files and thread IDs" {}))))
  (let [pool (Executors/newFixedThreadPool (get-in ctx [:config :parallelism])) running (atom {})]
    (try
      (rt/update! ctx assoc :status :running)
      (loop [selection-input nil]
        (doseq [[key job] @running :when (.isDone ^Future job)]
          (try (.get ^Future job) (catch Exception e (flow/attention! ctx key (or (.getCause e) e))))
          (swap! running dissoc key))
        (if (flow/paused? ctx)
          (if (empty? @running) (rt/update! ctx assoc :status :paused)
              (do (Thread/sleep 250) (recur selection-input)))
          (let [settings (project/settings ctx)
                campaign-active? (project/runnable? settings (get-in @(:state ctx) [:campaign-issue :number]))
                active? (fn [t] (and campaign-active?
                                     (project/runnable? settings (get-in t [:issue :number]))
                                     (project/runnable? settings (get-in @(:state ctx) [:source-tasks (:task t) :issue :number]))))]
            (doseq [t (flow/tasks ctx) :when (and (active? t) (#{:ci :merge} (:status t)) (not (contains? @running (:key t))))]
              (try (flow/integrate! ctx t)
                   (catch Exception e (when-not (:ownership-wait (ex-data e)) (flow/attention! ctx (:key t) e)))))
            (doseq [t (sort-by (fn [t] [(project/priority settings (get-in @(:state ctx) [:source-tasks (:task t) :issue :number])) (:key t)]) (flow/tasks ctx))]
              (when (and (active? t) (< (count @running) (get-in ctx [:config :parallelism]))
                         (not (contains? @running (:key t))) (flow/eligible? ctx t [])
                         (coordination/claim! ctx (:key t) (:paths t)))
                (flow/transition! ctx (:key t) :implementing)
                (swap! running assoc (:key t) (.submit pool ^Callable (reify Callable (call [_] (run-worker! ctx (:key t))))))))
            (let [base (when (and campaign-active? (empty? @running)) (flow/fresh-main! ctx))
                  fingerprint (when base (rt/digest (pr-str [base settings (vec (sort-by :identity (coordination/claims ctx)))
                                                            (mapv #(select-keys % [:key :status :merged :error]) (sort-by :key (flow/tasks ctx)))])))]
              (if (and base (not= fingerprint selection-input))
                (let [snapshot (snapshot! ctx)
                      output (select! ctx snapshot settings)]
                  (if (and (empty? (:increments output)) (complete! ctx snapshot))
                    (do (rt/update! ctx assoc :status :complete)
                        (gh/status! ctx (:campaign-issue @(:state ctx)) "Done")
                        (rt/effect! ctx "close-campaign"
                                    #(gh/api ctx "PATCH" (gh/repo-path ctx (str "issues/" (get-in @(:state ctx) [:campaign-issue :number]))) {:state "closed" :state_reason "completed"})))
                    (do (when (empty? (:increments output))
                          (rt/update! ctx assoc :status :waiting)
                          (gh/status! ctx (:campaign-issue @(:state ctx)) "Attention"))
                        (Thread/sleep (get-in ctx [:config :poll-ms])) (recur fingerprint))))
                (do (Thread/sleep (get-in ctx [:config :poll-ms])) (recur (or fingerprint selection-input))))))))
      (finally (.shutdown pool) (.awaitTermination pool Long/MAX_VALUE TimeUnit/MILLISECONDS)
               (coordination/unregister! ctx)))))
