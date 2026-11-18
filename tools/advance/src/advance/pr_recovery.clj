(ns advance.pr-recovery
  "Resume the explicit review and integration of one operator-authorized PR composition."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [simplify.github :as gh]
            [simplify.recovery :as recovery]
            [simplify.runtime :as rt]
            [simplify.workflow :as flow])
  (:import [java.io ByteArrayInputStream]
           [java.nio.file Files]
           [java.util.zip ZipInputStream]))

(defn- retained [ctx path sha]
  (let [file (io/file (rt/canonical path))]
    (recovery/require! (.startsWith (.toPath file) (.toPath (io/file (rt/canonical (:state-dir ctx)))))
                       "PR recovery receipts must be retained by this campaign")
    (let [raw (slurp file :encoding "UTF-8")]
      (recovery/require! (= sha (rt/digest raw)) "PR recovery receipt changed")
      {:raw raw :value (edn/read-string raw)})))

(defn- source-delta [ctx cwd base candidate]
  (rt/git ctx cwd "diff-tree" "--no-commit-id" "-r" "--raw" "--no-renames" "--no-abbrev" base candidate))

(def full-pr-jobs
  (merge (zipmap ["fresh-leaf-regressions / portable" "fresh-leaf-regressions / darwin" "darwin-unsupported-host"]
                 ["leaves-portable" "leaves-darwin" "leaves-unsupported"])
         (into {} (map (juxt identity identity)
                       ["static" "contracts" "bootstrap" "macro" "checked-core-runtime"
                        "resolution-pipeline" "resolution-unmetered" "resolution-invocation-selection"]))))

(defn- full-selection? [ci jobs]
  (let [by-name (group-by :name jobs)
        required (conj (set (keys full-pr-jobs)) "select CI checks")
        artifacts (:required-artifacts ci)]
    (and (= :full (get-in ci [:selection :kind])) (= (:base ci) (get-in ci [:selection :base]))
         (every? #(= 1 (count %)) (vals by-name))
         (every? #(= "success" (get-in by-name [% 0 :conclusion])) required)
         (every? (fn [job] (or (required (:name job))
                               (and (#{"experimental-source-snapshot" "experimental-prerelease"} (:name job))
                                    (= "skipped" (:conclusion job))))) jobs)
         (= (set (vals full-pr-jobs)) (set (map :name artifacts))) (= (count full-pr-jobs) (count artifacts))
         (every? #(and (= (:name %) (get full-pr-jobs (:job-name %)))
                       (seq (:children %)) (every? (fn [c] (and (= :passed (:status c)) (= 0 (:exit-code c)))) (:children %))) artifacts))))

(defn- same-job-execution? [ctx jobs artifact run]
  ;; A single-job rerun copies successful jobs to new check IDs. Their original
  ;; runner, timestamps and step results still identify the producing execution.
  (let [current (first (filter #(= (:job artifact) (:id %)) jobs))
        original (if (= (:job artifact) (:producing-job artifact)) current
                     (gh/api ctx (gh/repo-path ctx (str "actions/jobs/" (:producing-job artifact)))))
        fields [:run_id :name :conclusion :runner_id :started_at :completed_at :steps]]
    (and (= run (:run_id current) (:run_id original))
         (= (:job-name artifact) (:name current)) (= "success" (:conclusion current))
         (= (:attempt artifact) (:run_attempt original)) (= (:producing-job artifact) (:id original))
         (pos-int? (:runner_id current)) (string? (:started_at current)) (string? (:completed_at current))
         (seq (:steps current)) (= (select-keys original fields) (select-keys current fields)))))

(defn- live-selection? [ctx ci jobs]
  (let [selector (:selector ci)]
    (when (and (pos-int? (:producing-job selector)) (= "select CI checks" (:job-name selector))
               (same-job-execution? ctx jobs selector (:run ci)))
      (let [log (rt/run! ctx (get-in ctx [:config :repo-dir])
                         ["gh" "api" (gh/repo-path ctx (str "actions/jobs/" (:producing-job selector) "/logs"))])
            selections (keep #(some-> (re-find #" CI selection (\{.*\})\s*$" %) second edn/read-string)
                             (str/split-lines log))]
        (= [(:selection ci)] (vec selections))))))

(defn- zip-text-entries [bytes wanted]
  (with-open [zip (ZipInputStream. (ByteArrayInputStream. bytes))]
    (loop [found {}]
      (if-let [entry (.getNextEntry zip)]
        (let [path (.getName entry)]
          (recovery/require! (not (contains? found path)) "Duplicate CI evidence entry")
          (recur (if (wanted path) (assoc found path (String. (.readAllBytes zip) "UTF-8")) found)))
        found))))

(defn- returned-vars [entries logs]
  (vec (into (sorted-set)
             (for [path logs line (str/split-lines (entries path))
                   :when (str/starts-with? line "{:schema :gravity/development-test-progress-v2,")
                   :let [event (edn/read-string line)]
                   :when (= [:test-var :return] ((juxt :phase :edge) event))]
               (:test-var event)))))

(defn- archive-evidence? [file ci {:keys [name sha256 children attempt returned-test-vars]}]
  (let [bytes (Files/readAllBytes (.toPath file))
        summaries (case name
                    "leaves-darwin" ["leaves-darwin-summary.edn" "darwin-probe-summary.edn"]
                    "leaves-unsupported" ["darwin-unsupported-summary.edn"]
                    [(str name "-summary.edn")])
        logs (when (and returned-test-vars (= name "resolution-pipeline")) (mapv #(str (:id %) ".log") children))
        wanted (into (set summaries) (cons "checkout.txt" logs))]
    (and (= sha256 (recovery/bytes-digest bytes))
         (let [entries (zip-text-entries bytes wanted)
               reports (mapv #(some-> (entries %) edn/read-string) summaries)
               records (mapcat identity reports)]
           (and (= wanted (set (keys entries)))
                (= [(:checkout ci) (:tree ci)] (str/split-lines (entries "checkout.txt")))
                (every? #(and (vector? %) (seq %)) reports)
                (every? #(and (string? (:id %)) (= (:checkout ci) (:commit %))
                              (= (str (:run ci)) (:run-id %)) (= (str attempt) (:run-attempt %))) records)
                (= children (mapv #(select-keys % [:id :status :exit-code]) records))
                (or (nil? returned-test-vars) (and (seq logs) (= returned-test-vars (returned-vars entries logs)))))))))

(defn finish! [ctx task-key authority-file authority-digest ci-file ci-digest]
  (recovery/require! (and (true? @(:coordinator-owner ctx)) (= {:paused true} (rt/read-edn (:control-file ctx))))
                     "Pause and drain the coordinator before scoped integration")
  (let [{:keys [raw value]} (retained ctx authority-file authority-digest)
        auth value ci (:value (retained ctx ci-file ci-digest)) s @(:state ctx) t (flow/task ctx task-key)
        scope (:pr-composition t) original (:original-task scope) pr (get-in t [:pr :number])
        tuple (select-keys t [:base :candidate :tree]) checks (:checks t)
        accepted-jobs (set (map :name (:required-artifacts ci)))]
    (recovery/require!
     (and (= (:kind auth) :single-pr-composition) (= :native-passed (:stage scope))
          (= auth (:authority scope)) (= authority-digest (:authority-sha256 scope))
          (= (:repo auth) (get-in ctx [:config :repository])) (= pr (:pr auth))
          (= (:active-issues auth) {:campaign (get-in s [:campaign-issue :number])
                                    :parent (get-in s [:source-tasks (:task t) :issue :number])
                                    :increment (get-in t [:issue :number])})
          (= (:from auth) (:candidate original)) (= [(:from auth) (:base t)] (:parents scope))
          (true? (:require-all-selected-jobs-and-fresh-independent-review auth))
          (= :normal-pr-ci (:hosted auth)) (seq (:required-hosted auth)) (every? accepted-jobs (:required-hosted auth))
          (= :attention (:status t)) (nil? (:error t)) (nil? (:review t))
          (= :passed (:result checks) (:result ci)) (= (:native auth) (:commands checks)) (seq (:commands checks))
          (= tuple (select-keys ci [:base :candidate :tree]))
          (= (:candidate t) (:candidate checks)) (= (:tree t) (:tree checks)))
     "Authority and validation must cover this exact new PR tree")
    (flow/assert-identity! ctx t) (flow/assert-clean! ctx (:worktree t))
    (let [info (gh/api ctx (gh/repo-path ctx (str "pulls/" pr)))
          workflows (gh/head-workflow-state ctx (:candidate t))
          run (first (filter #(= (:run ci) (:id %)) (:runs workflows)))
          jobs (gh/api ctx (gh/repo-path ctx (str "actions/runs/" (:run ci) "/jobs?per_page=100")))
          artifacts (:artifacts (gh/api ctx (gh/repo-path ctx (str "actions/runs/" (:run ci) "/artifacts?per_page=100"))))
          checkout (gh/api ctx (gh/repo-path ctx (str "git/commits/" (:checkout ci))))
          parents (str/split (rt/git ctx (:worktree t) "show" "-s" "--format=%P" (:candidate t)) #" ")
          scopes (map #(select-keys % [:id :name :conclusion :run_attempt]) (:jobs jobs))
          old-delta (source-delta ctx (:worktree t) (:base original) (:candidate original))
          new-delta (source-delta ctx (:worktree t) (:base t) (:candidate t))]
      (recovery/require!
       (and (= (:candidate t) (get-in info [:head :sha]) (:head_sha run))
            (= parents (:parents scope)) (= (:checkout ci) (:sha checkout))
            (= [(:base t) (:candidate t)] (mapv :sha (:parents checkout))) (= (:tree t) (get-in checkout [:tree :sha]))
            (= (:base t) (flow/fresh-main! ctx)) (= "open" (:state info)) (true? (:draft info))
            (= (get-in ctx [:config :base-branch]) (get-in info [:base :ref]))
            (= (:branch t) (get-in info [:head :ref])) (= (get-in ctx [:config :repository]) (get-in info [:head :repo :full_name]))
            (= :passed (:state workflows)) (= "success" (:conclusion run)) (= (:attempt ci) (:run_attempt run))
            (= ".github/workflows/ci.yml" (:path run) (:workflow ci)) (= "pull_request" (:event run))
            (= (:total_count jobs) (count scopes)) (= (set scopes) (set (:jobs ci))) (full-selection? ci (:jobs jobs))
            (live-selection? ctx ci (:jobs jobs))
            (every? #(and (pos-int? (:producing-job %)) (same-job-execution? ctx (:jobs jobs) % (:run ci))) (:required-artifacts ci))
            (every? (fn [{:keys [name artifact sha256 attempt] :as receipt}]
                      (and (pos-int? attempt) (<= attempt (:attempt ci)) (re-matches #"[a-z0-9-]+" name)
                           (some #(and (= artifact (:id %)) (= (str "sha256:" sha256) (:digest %))
                                       (= (str "ci-" name "-" (:checkout ci) "-" attempt) (:name %))
                                       (= (:candidate t) (get-in % [:workflow_run :head_sha]))
                                       (= (:run ci) (get-in % [:workflow_run :id]))) artifacts)
                           (archive-evidence? (io/file (.getParentFile (io/file ci-file)) (str name ".zip")) ci receipt)))
                    (:required-artifacts ci))
            (= old-delta new-delta))
       "The live workflow, PR or composed feature delta differs from its receipt")
      (rt/update! ctx update-in [:tasks task-key]
                  #(-> % (assoc-in [:checks :hosted-ci] (assoc ci :feature-delta-identical-to (:candidate original)
                                                               :feature-blob-delta new-delta :evidence-sha256 ci-digest))
                         (assoc-in [:pr-composition :stage] :reviewing)))
      (let [review (flow/review! ctx (flow/task ctx task-key))
            reviewed (flow/task ctx task-key) job (get-in @(:state ctx) [:jobs (get-in reviewed [:review :job])])]
        (recovery/require! (and (= "approved" (:verdict review)) (= :completed (:status job))
                                (= review (:output job)) (true? (:turn-terminal? job))
                                (not (:restricted job)) (empty? (:tool-refusals job)))
                           "The fresh independent reviewer requires attention")
        (flow/assert-evidence! reviewed)
        (rt/update! ctx assoc-in [:tasks task-key :pr-composition :stage] :approved)
        (let [ctx (assoc ctx :pr-integration {:task-key task-key :candidate (:candidate t)
                                              :authority-raw raw :authority-sha256 authority-digest})]
          (flow/publish! ctx (flow/task ctx task-key))
          (loop []
            (flow/integrate! ctx (flow/task ctx task-key))
            (let [current (flow/task ctx task-key)]
              (cond
                (= :done (:status current)) (prn (select-keys current [:key :candidate :tree :merged]))
                (= :ci (:status current)) (do (Thread/sleep (get-in ctx [:config :poll-ms])) (recur))
                :else (rt/fail! "Scoped integration requires inspection" {:finding task-key :status (:status current)})))))))))
