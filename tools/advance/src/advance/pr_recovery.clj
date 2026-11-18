(ns advance.pr-recovery
  "Resume the explicit review and integration of one operator-authorized PR composition."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [simplify.github :as gh]
            [simplify.recovery :as recovery]
            [simplify.runtime :as rt]
            [simplify.workflow :as flow]))

(defn- retained [ctx path sha]
  (let [file (io/file (rt/canonical path))]
    (recovery/require! (.startsWith (.toPath file) (.toPath (io/file (rt/canonical (:state-dir ctx)))))
                       "PR recovery receipts must be retained by this campaign")
    (let [raw (slurp file :encoding "UTF-8")]
      (recovery/require! (= sha (rt/digest raw)) "PR recovery receipt changed")
      {:raw raw :value (edn/read-string raw)})))

(defn- source-delta [ctx cwd base candidate]
  (rt/git ctx cwd "diff-tree" "--no-commit-id" "-r" "--raw" "--no-renames" "--no-abbrev" base candidate))

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
          scopes (map #(select-keys % [:id :name :conclusion]) (:jobs jobs))
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
            (= (:total_count jobs) (count scopes)) (= (set scopes) (set (:jobs ci)))
            (every? (fn [a] (some #(= [(:job a) (:name a) "success"] ((juxt :id :name :conclusion) %)) scopes))
                    (:required-artifacts ci))
            (every? (fn [{:keys [name artifact sha256]}]
                      (and (re-matches #"[a-z0-9-]+" name)
                           (some #(and (= artifact (:id %)) (= (str "sha256:" sha256) (:digest %))
                                       (= (str "ci-" name "-" (:checkout ci) "-" (:attempt ci)) (:name %))
                                       (= (:candidate t) (get-in % [:workflow_run :head_sha]))
                                       (= (:run ci) (get-in % [:workflow_run :id]))) artifacts)
                           (= sha256 (recovery/file-digest (io/file (.getParentFile (io/file ci-file)) (str name ".zip"))))))
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
