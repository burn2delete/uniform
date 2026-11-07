(ns advance.workflow
  "Select real Spec Kit increments, implement, review and merge until proven complete."
  (:require [advance.catalog :as catalog]
            [advance.github :as project]
            [clojure.edn :as edn]
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
(defn selection-output-schema [catalog]
  (let [keys (mapv :key (:tasks catalog))]
    (if (seq keys)
      (assoc-in selection-schema [:properties :blockers :items :properties :task :enum] keys)
      (assoc-in selection-schema [:properties :blockers :maxItems] 0))))

(def selector-instructions
  (str instructions
       " Use the supplied base commit and tree; do not run Git identity or merge-history commands. "
       "Every blockers.task must be an exact current catalog task key. Put environment and restriction notes in summary. "
       "Retained denied Git/cache-write actions remain denied: never replay, transfer to another tool or executable, or work around them. "
       "Do not rerun or correct a retained rejected job through tools; its output and refusals remain history. "))
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

(defn source-reference [ctx task base]
  (str "Repository task authority: `" (:key task) "`.\n\n"
       "[Read the complete task and its acceptance criteria](https://github.com/"
       (get-in ctx [:config :repository]) "/blob/" base "/" (:path task) "#L" (:line task) ").\n\n"
       "Source revision: `" base "`; task-file SHA-256: `" (:source-digest task) "`.\n\n"
       "Repository disposition: " (if (:checked task) "checked" "open") ".\n\n"
       "This issue mirrors the repository outcome. Child issues are finite increments; their verified merges do not close this parent automatically."))

(defn mirror! [ctx snapshot base]
  (let [ctx (assoc ctx :issue-cache (atom nil))]
  (doseq [task (:tasks snapshot)]
    (let [issue (or (get-in @(:state ctx) [:source-tasks (:key task) :issue])
                    (gh/ensure-issue! ctx (str "task/" (:key task)) (:title task)
                      (source-reference ctx task base)
                      nil "implementation:task"))
          previous (get-in @(:state ctx) [:source-tasks (:key task)])]
      (rt/update! ctx assoc-in [:source-tasks (:key task)] (assoc task :issue issue))
      (when (or (nil? previous) (not= (:checked task) (:checked previous)))
        (gh/status! ctx issue (if (:checked task) "Done" "Ready")))
      (when (and previous (not= (:source-digest task) (:source-digest previous)))
        (gh/comment! ctx issue (str "source/" base)
                     (source-reference ctx task base)))))
  ;; Removed/reclassified records remain history, never completion evidence.
  (let [present (set (map :key (:tasks snapshot)))]
    (doseq [[key task] (:source-tasks @(:state ctx)) :when (not (present key))]
      (rt/update! ctx assoc-in [:source-tasks key :missing] true)
      (when-not (:missing task) (gh/status! ctx (:issue task) "Attention"))))))

(defn snapshot! [ctx]
  (let [base (flow/fresh-main! ctx)
        wt (flow/worktree! ctx (str "catalog-" (flow/next-job! ctx :snapshot) "-" (subs base 0 12)) base)
        snapshot (catalog/discover (:worktree wt))
        tree (rt/git ctx (:worktree wt) "rev-parse" "HEAD^{tree}")]
    (rt/update! ctx assoc :catalog {:base base :tree tree
                                   :worktree (:worktree wt) :source (catalog/index snapshot)})
    (mirror! ctx snapshot base)
    {:base base :tree tree :worktree (:worktree wt) :catalog snapshot}))

(defn- effect-ids [task]
  (->> [(:effect task) (:effect-id task)
        (get-in task [:error :details :effect])
        (get-in task [:error :effect])]
       (mapcat #(if (sequential? %) % (if (nil? %) [] [%])))
       (remove nil?)
       distinct
       vec))

(defn- history-flags [task effects]
  (let [error (:error task)
        details (:details error)
        effect-status (or (:effect-status error) (:effect-status details))
        referenced-ids (effect-ids task)
        referenced-effects (select-keys (or effects {}) referenced-ids)
        effect-status-by-id (into {}
                                (map (fn [id] [id (or (get-in referenced-effects [id :status])
                                                     :missing)])
                                     referenced-ids))
        effect-statuses (set (vals effect-status-by-id))
        restricted? (boolean (or (:restricted error) (:restricted details)
                                 (= :restricted (:status error))
                                 (= :restricted effect-status)
                                 (contains? effect-statuses :restricted)))
        uncertain? (boolean (or (:uncertain error) (:uncertain details)
                                (= :uncertain (:status error))
                                (= :uncertain effect-status)
                                (contains? effect-statuses :uncertain)))]
    {:restricted restricted?
     :uncertain uncertain?
     :pending (or (= :pending effect-status)
                  (contains? effect-statuses :pending))
     :attention (= :attention (:status task))
     :effect-ids referenced-ids
     :effect-status effect-status-by-id}))

(defn- history-index-entry [snapshot-path line task effects settings]
  {:key (:key task)
   :task (:task task)
   :title (:title task)
   :paths (:paths task)
   :dependencies (:dependencies task)
   :status (:status task)
   :base (:base task)
   :candidate (:candidate task)
   :tree (:tree task)
   :merged (:merged task)
   :operator-control (or (get settings (get-in task [:issue :number])) {})
   :history (history-flags task effects)
   :record {:path snapshot-path :line line
            :key (:key task)
            :digest (rt/digest (pr-str task))}})

(defn- relevant-effects [tasks effects]
  (let [ids (distinct (mapcat effect-ids tasks))]
    (select-keys (or effects {}) ids)))

(defn- relevant-settings [tasks settings]
  (let [numbers (keep #(get-in % [:issue :number]) tasks)]
    (select-keys (or settings {}) numbers)))

(defn- history-path [worktree digest]
  (str "target/validation/advance/selection-history/" digest ".edn"))

(defn- ensure-history-path! [worktree path]
  (let [root (rt/canonical worktree)
        file (io/file worktree path)
        canonical-file (rt/canonical file)]
    (when (or (not (str/starts-with? canonical-file (str root java.io.File/separator)))
              (not (str/starts-with? path "target/validation/advance/selection-history/")))
      (rt/fail! "Selection history snapshot escaped its worktree" {:worktree worktree :path path}))
    (io/make-parents file)
    file))

(defn- verify-history-snapshot! [worktree envelope path expected-digest]
  (let [file (ensure-history-path! worktree path)]
    (when-not (.isFile file)
      (rt/fail! "Selection history snapshot is missing" {:path path :digest expected-digest}))
    (let [raw (try (slurp file)
                   (catch Exception e
                     (rt/fail! "Selection history snapshot cannot be read"
                               {:path path :digest expected-digest :cause (.getMessage e)})))]
      (when-not (= expected-digest (rt/digest raw))
        (rt/fail! "Selection history snapshot digest mismatch"
                  {:path path :expected expected-digest :actual (rt/digest raw)}))
      (let [decoded (try (edn/read-string {:readers {}
                                          :default (fn [tag _]
                                                     (rt/fail! "Unsupported EDN tag" {:tag tag}))}
                                         raw)
                         (catch Exception e
                           (rt/fail! "Selection history snapshot is corrupt"
                                     {:path path :digest expected-digest :cause (.getMessage e)})))]
        (when-not (= envelope decoded)
          (rt/fail! "Selection history snapshot contents differ from its identity"
                    {:path path :digest expected-digest}))
        decoded))))

(defn- snapshot-text [{:keys [base tree effects settings tasks]}]
  (str "{:base " (pr-str base)
       " :tree " (pr-str tree)
       " :effects " (pr-str effects)
       " :settings " (pr-str settings)
       " :tasks [\n"
       (str/join "\n" (map #(str "  " (pr-str %)) tasks))
       "\n]}\n"))

(defn- write-history-snapshot! [file serialized]
  (try
    (java.nio.file.Files/write (.toPath file) (.getBytes serialized "UTF-8")
                               (into-array java.nio.file.StandardOpenOption
                                           [java.nio.file.StandardOpenOption/CREATE_NEW
                                            java.nio.file.StandardOpenOption/WRITE]))
    (catch java.nio.file.FileAlreadyExistsException _ nil)))

(defn selection-history!
  "Persist complete increment records once and return a compact selector index.

  The snapshot lives inside the selector worktree so the existing read-only
  model sandbox can inspect it. Content-addressing and the no-replace check
  make a snapshot immutable for its base/tree/task set."
  ([worktree base tree tasks]
   (selection-history! worktree base tree tasks {}))
  ([worktree base tree tasks {:keys [effects settings] :or {effects {} settings {}}}]
   (let [tasks (vec (sort-by :key tasks))
        effects (relevant-effects tasks effects)
        settings (relevant-settings tasks settings)
        envelope {:base base :tree tree :tasks tasks :effects effects :settings settings}
        serialized (snapshot-text envelope)
        digest (rt/digest serialized)
        path (history-path worktree digest)
        file (ensure-history-path! worktree path)]
     (if (.exists file)
       (when-not (= serialized (slurp file))
         (rt/fail! "Selection history snapshot is immutable but contents differ"
                   {:path path :digest digest}))
       (write-history-snapshot! file serialized))
     (verify-history-snapshot! worktree envelope path digest)
     {:index (mapv (fn [line task]
                     (history-index-entry path (+ 2 line) task effects settings))
                   (range) tasks)
      :snapshot {:path path :digest digest :base base :tree tree}})))

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

(defn select! [ctx {:keys [base tree worktree catalog]} settings pulls]
  (let [history (selection-history! worktree base tree (flow/tasks ctx)
                                  {:effects (or (:effects @(:state ctx)) {})
                                   :settings settings})
        output-schema (selection-output-schema catalog)
        job (flow/next-job! ctx :select)
        prompt (str "Select independently useful implementation increments from the current task catalog. "
                      "Read owning spec/plan/task/evidence and actual source; the index is navigation. "
                      "Prioritize configured task keys, then Project priority, actual prerequisites and work that unblocks completion. "
                      "Respect Project Control Paused/Blocked on both campaign, source tasks and increments. "
                      "Do not duplicate active, held, restricted or already merged increments. "
                      "Open PRs are independent candidates even when their writers released file claims for review/CI. Inspect their scope and avoid duplicating their implementation; do not adopt, close, merge or claim their acceptance. "
                      "Use a fresh stable key for each distinct increment, never just rename an unsuccessful one. "
                      "Include the source tasks.md in owned paths and name concrete accepted/rejected evidence. "
                      "Dependencies reference real increment keys only; inspect artifact-level task dependencies rather than assuming the coarse graph is acyclic. "
                      "If none are actionable, give blockers; never infer completion from an empty selection.\nBase: " base
                      "\nTree: " tree
                      "\nCatalog: " (pr-str (catalog/index catalog))
                      "\nExplicit priorities: " (pr-str (get-in ctx [:config :priorities]))
                      "\nGitHub controls by issue: " (pr-str settings)
                      "\nSource issue identities: " (pr-str (into {} (map (fn [[k v]] [k (get-in v [:issue :number])]) (:source-tasks @(:state ctx)))))
                      "\nExisting increment index (complete records are in the supplied read-only snapshot): "
                      (pr-str (:index history))
                      "\nComplete increment record snapshot: " (pr-str (:snapshot history))
                      " Inspect only relevant full records by key and line as needed to confirm overlap or prerequisites. The controller already verified snapshot integrity; an unreadable or mismatched reference is missing evidence, never permission to ignore history. "
                      "\nRepository open candidates and changed paths: " (pr-str (project/candidate-files ctx pulls))
                      "\nShared writer claims: " (pr-str (coordination/claims ctx))
                      "\nRetained rejected planning notes (history, never retry authorization): "
                      (pr-str (mapv #(select-keys % [:job :status :validation-error :restriction])
                                    (:operator-planning @(:state ctx)))))
        response (volatile! nil)
        output (try
                 (let [output (codex/job! ctx job worktree :read selector-instructions prompt output-schema)]
                   (vreset! response output)
                   (schema/validate! output-schema output)
                   (doseq [increment (:increments output)]
                     (let [number (get-in @(:state ctx) [:source-tasks (:task increment) :issue :number])]
                       (when-not (project/runnable? settings number)
                         (rt/fail! "Selector chose an operator-paused or blocked task" {:task (:task increment)}))))
                   (doseq [blocker (:blockers output)]
                     (when-not (some #(= (:task blocker) (:key %)) (:tasks catalog))
                       (rt/fail! "Blocker names an unknown source task" {:blocker blocker})))
                   output)
                 (catch Exception e
                   (rt/update! ctx update :selection-rejections (fnil conj [])
                               {:job job :base base :tree tree :output @response :admitted false
                                :response-reference {:state-file (:state-file ctx) :log-file (:log-file ctx)
                                                     :job job :identity (select-keys (get-in @(:state ctx) [:jobs job]) [:thread :turn])}
                                :message (.getMessage e) :details (ex-data e)})
                   (throw e)))]
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

(defn failure! [ctx key e]
  (flow/failure! ctx key e))

(defn run-worker! [ctx key]
  (try (flow/prepare! ctx key)
       (catch Exception e
         (if (:ownership-wait (ex-data e)) (flow/transition! ctx key :repair) (failure! ctx key e)))))

(defn campaign! [ctx]
  (coordination/register! ctx)
  (try
  (project/setup! ctx)
  (ensure-campaign! ctx)
  (let [pool (Executors/newFixedThreadPool (get-in ctx [:config :parallelism])) running (atom {})]
    (try
      (rt/update! ctx assoc :status :running)
      (gh/status! ctx (:campaign-issue @(:state ctx)) "Implementing")
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
            (doseq [t (flow/tasks ctx) :when (and (active? t) (= :finalizing (:status t)))]
              (try (flow/finalize-merge! ctx t) (catch Exception e (failure! ctx (:key t) e))))
            (doseq [t (flow/tasks ctx) :when (and (active? t) (#{:ci :merge} (:status t)) (not (contains? @running (:key t))))]
              (try (flow/integrate! ctx t)
                   (catch Exception e (when-not (:ownership-wait (ex-data e)) (failure! ctx (:key t) e)))))
            (doseq [t (sort-by (fn [t] [(project/priority settings (get-in @(:state ctx) [:source-tasks (:task t) :issue :number])) (:key t)]) (flow/tasks ctx))]
              (when (and (active? t) (< (count @running) (get-in ctx [:config :parallelism]))
                         (not (contains? @running (:key t))) (flow/eligible? ctx t [])
                         (coordination/claim! ctx (:key t) (:paths t)))
                (flow/transition! ctx (:key t) :implementing)
                (rt/update! ctx assoc :status :running)
                (swap! running assoc (:key t) (.submit pool ^Callable (reify Callable (call [_] (run-worker! ctx (:key t))))))))
            (let [base (when (and campaign-active? (empty? @running)) (flow/fresh-main! ctx))
                  pulls (when base (project/open-prs ctx))
                  fingerprint (when base (rt/digest (pr-str [base settings pulls (vec (sort-by :identity (coordination/claims ctx)))
                                                            (mapv #(select-keys % [:key :status :merged :error]) (sort-by :key (flow/tasks ctx)))])))]
              (if (and base (not= fingerprint selection-input))
                (let [snapshot (snapshot! ctx)
                      output (select! ctx snapshot settings pulls)]
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
      (finally (.shutdown pool) (.awaitTermination pool Long/MAX_VALUE TimeUnit/MILLISECONDS))))
  (finally (coordination/unregister! ctx))))
