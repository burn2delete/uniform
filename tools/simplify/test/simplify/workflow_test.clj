(ns simplify.workflow-test
  "Local Git integration with deterministic GitHub/model doubles. No real agents or GitHub writes."
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is use-fixtures]]
            [simplify.codex :as codex]
            [simplify.core-test :as fixture]
            [simplify.github :as gh]
            [simplify.main :as main]
            [simplify.runtime :as rt]
            [simplify.workflow :as flow]))

(use-fixtures :each fixture/no-github-network)

(def finding {:key "remove-duplicate" :title "Remove duplicate work" :evidence "The same constant is repeated"
              :proposal "Keep the required value once" :preserve "Value remains one"
              :paths ["work.edn"] :dependencies [] :kind "duplication"})

(defn evidenced [t]
  (assoc t :review (assoc (select-keys t [:base :candidate :tree]) :verdict "approved")
           :checks {:candidate (:candidate t) :tree (:tree t) :result :passed}))

(defn repository! [ctx]
  (let [root (:state-dir ctx) bare (str (io/file root "remote.git")) repo (str (io/file root "repo"))]
    (rt/git ctx root "init" "--bare" "--initial-branch=main" bare)
    (rt/git ctx root "clone" bare repo)
    (rt/git ctx repo "config" "user.email" "fixture@example.invalid")
    (rt/git ctx repo "config" "user.name" "Simplify fixture")
    (rt/git ctx root "--git-dir" bare "config" "user.email" "fixture@example.invalid")
    (rt/git ctx root "--git-dir" bare "config" "user.name" "Simplify fixture")
    (spit (io/file repo "work.edn") "{:value 1 :duplicate 1}\n")
    (rt/git ctx repo "add" "work.edn")
    (rt/git ctx repo "commit" "-m" "Fixture")
    (rt/git ctx repo "push" "origin" "main")
    {:repo repo :bare bare}))

(deftest nul-delimited-git-paths-remain-exact
  (let [ctx (fixture/context) {:keys [repo]} (repository! ctx)
        tracked "tracked space.edn" untracked "untracked\nname.edn"]
    (spit (io/file repo tracked) "{:old true}\n")
    (rt/git ctx repo "add" tracked)
    (rt/git ctx repo "commit" "-m" "Tracked path fixture")
    (spit (io/file repo tracked) "{:new true}\n")
    (spit (io/file repo untracked) "{:new true}\n")
    (is (= #{tracked untracked} (set (flow/changed-paths ctx repo))))))

(defn run-fixture! [review-result & [options]]
  (let [ctx (fixture/context)
        {:keys [repo bare]} (repository! ctx)
        ctx (assoc ctx :config {:repo-dir repo :repository "fixture/repo" :remote "origin" :base-branch "main"
                                :campaign "fixture" :scope ["."] :parallelism 2 :poll-ms 1
                                :worktree-root (str (io/file (:state-dir ctx) "workers"))
                                :checks [["clojure" "-M" "fixture-check.clj"]] :check-executables #{"clojure"}})
        _ (.mkdirs (io/file (get-in ctx [:config :worktree-root])))
        _ (rt/update! ctx assoc :repository "fixture/repo" :campaign "fixture")
        calls (atom []) audits (atom 0) pr (atom nil) merge-count (atom 0)
        merge-requests (atom 0)
        check-count (atom 0) implementations (atom 0) claims (atom 0)
        preflight-done? (atom false)
        tracking-locks (atom [])
        other-controller (assoc ctx :main-lock-owner (atom nil))
        original-command rt/command
        original-claim rt/claim-paths!
        response (fn [value] {:exit 0 :out (json/write-str value) :err ""})]
    (with-redefs
      [rt/claim-paths! (fn [c key paths phase]
                        (when-not (and (:first-claim-busy options) (= 1 (swap! claims inc)))
                          (original-claim c key paths phase)))
       rt/command (fn
                    ([c cwd argv]
                     (swap! calls conj argv)
                     (cond
                       (= "clojure" (first argv))
                       (do
                         (when (some #{"--mode"} argv)
                           (reset! preflight-done? true)
                           (when-let [mutate (:preflight-mutation options)] (mutate pr))
                           (when (:pause-after-preflight options) (rt/write-edn! (:control-file c) {:paused true})))
                         {:exit 0 :out "fixture check passed" :err ""})
                       (= ["gh" "pr" "view"] (subvec (vec argv) 0 (min 3 (count argv))))
                       (response (merge {:state "OPEN" :baseRefName "main" :isDraft false :reviewDecision "APPROVED"
                                         :mergeable "MERGEABLE" :mergeStateStatus "CLEAN"
                                         :statusCheckRollup [{:status "COMPLETED" :conclusion "SUCCESS"}]}
                                        @pr))
                       (= ["gh" "pr" "merge"] (subvec (vec argv) 0 (min 3 (count argv))))
                       (if (and (= 1 (swap! merge-requests inc)) (:first-merge-not-applied options))
                         {:exit 0 :out "provider did not apply merge" :err ""}
                         (let [t (first (flow/tasks c))
                             tree (rt/git c cwd "rev-parse" (str (:candidate t) "^{tree}"))
                             merged (rt/git c cwd "--git-dir" bare "commit-tree" tree "-p" (:base t) "-m" "Fixture squash")]
                         (rt/git c cwd "--git-dir" bare "update-ref" "refs/heads/main" merged (:base t))
                         (swap! merge-count inc)
                         (swap! pr assoc :state "MERGED" :mergeCommit {:oid merged})
                         {:exit 0 :out "merged" :err ""}))
                       :else (original-command c cwd argv)))
                    ([c cwd argv input] (original-command c cwd argv input)))
       gh/setup! (fn [_] nil)
       gh/review-threads-clear? (fn [& _] (not (and @preflight-done? (:unresolved-thread options))))
       gh/head-workflow-state (fn [& _] {:state (if (and @preflight-done? (:queued-workflow options)) :pending :passed) :runs []})
       gh/status! (fn [_ issue status]
                    (when (and (= "Attention" status) (= 1 (:number issue)))
                      (rt/write-edn! (:control-file ctx) {:paused true}))
                    (when (= "Done" status)
                      (swap! tracking-locks conj (rt/try-with-main-lock other-controller (fn [] :available)))))
       gh/relation! (fn [& _] nil)
       gh/comment! (fn [& _] nil)
       gh/ensure-milestone! (fn [_ round] {:number round})
       gh/ensure-issue! (fn [_ key title _ _ _] {:id (if (str/starts-with? key "campaign/") 1 2)
                                               :number (if (str/starts-with? key "campaign/") 1 2)
                                               :node_id key :title title})
       gh/api (fn
                ([_ endpoint]
                 (cond
                   (str/includes? endpoint "/protection") {:required_status_checks {:strict true}}
                   (str/includes? endpoint "pulls?state") []
                   :else (throw (ex-info "Unexpected fixture read" {:endpoint endpoint}))))
                ([c method endpoint body]
                 (when (and (= "PATCH" method) (= "closed" (:state body)))
                   (swap! tracking-locks conj (rt/try-with-main-lock other-controller (fn [] :available))))
                 (if (and (= "POST" method) (str/ends-with? endpoint "/pulls"))
                   (let [t (first (flow/tasks c))]
                     (reset! pr {:headRefOid (:candidate t)})
                     {:number 3 :node_id "pr" :html_url "https://example.invalid/pr/3" :state "open"})
                   {:state (:state body)})))
       codex/command! (fn [_ _ _ argv]
                        (swap! calls conj argv)
                        (when (and (:first-check-failure options) (= 1 (swap! check-count inc)))
                          (throw (ex-info "Sandboxed check failed"
                                          (merge {:command argv :result {:exitCode 1 :stderr "Actual fixture assertion failed"} :writer-ended? true}
                                                 (:first-check-failure options)))))
                        {:exitCode 0 :stdout "fixture command" :stderr ""})
       codex/job! (fn [_ job cwd _ _ _ _]
                    (cond
                      (str/starts-with? job "audit-")
                      {:coverage ["work.edn"] :summary "Fixture audit" :findings (if (= 1 (swap! audits inc)) [finding] [])}
                      (str/starts-with? job "implement-")
                      (do (spit (io/file cwd "work.edn") (str "{:value 1 :repair " (swap! implementations inc) "}\n"))
                          {:outcome "ready" :summary "Removed duplicate" :checks []})
                      :else (merge review-result (select-keys (first (flow/tasks ctx)) [:base :candidate :tree]))))]
      (flow/campaign! ctx)
      (when-let [recover (:recover options)]
        (recover ctx bare repo calls merge-requests)
        (rt/write-edn! (:control-file ctx) {:paused false})
        (flow/campaign! ctx)))
    {:state @(:state ctx) :calls @calls :merges @merge-count :audits @audits :repo repo :ctx ctx
     :tracking-locks @tracking-locks :merge-requests @merge-requests :implementations @implementations :claims @claims}))

(deftest ordinary-ended-check-failure-repairs-without-stopping-the-campaign
  (let [{:keys [state implementations merges]} (run-fixture! {:verdict "approved" :summary "Exact acceptance" :findings []}
                                                          {:first-check-failure {}})
        task (get-in state [:tasks "remove-duplicate"])]
    (is (= :complete (:status state)))
    (is (= :done (:status task)))
    (is (= 2 implementations))
    (is (= 1 merges))
    (is (= 1 (count (:failed-checks task))))
    (is (= 1 (get-in task [:failed-checks 0 :details :result :exitCode])))))

(deftest restricted-unknown-and-uncertain-checks-stay-blocked-until-explicit-pause
  (doseq [details [{:restricted true} {:writer-ended? false} {:effect-status :uncertain}]]
    (let [{:keys [state implementations merges]} (run-fixture! {:verdict "approved" :summary "Exact acceptance" :findings []}
                                                            {:first-check-failure details})]
      (is (= :paused (:status state)) "The controller reaches the explicit pause after recording Attention")
      (is (= :attention (get-in state [:tasks "remove-duplicate" :status])))
      (is (= 1 implementations))
      (is (zero? merges)))))

(deftest temporary-writer-contention-waits-and-continues
  (let [{:keys [state implementations merges claims]} (run-fixture! {:verdict "approved" :summary "Exact acceptance" :findings []}
                                                                {:first-claim-busy true})]
    (is (= :complete (:status state)))
    (is (= 1 implementations))
    (is (= 1 merges))
    (is (>= claims 2))))

(deftest unverified-command-success-needs-explicit-inspected-recovery
  (let [{:keys [state merges merge-requests]}
        (run-fixture!
         {:verdict "approved" :summary "Exact fixture acceptance" :findings []}
         {:first-merge-not-applied true
          :recover
          (fn [ctx bare repo calls requests]
            (let [t (first (flow/tasks ctx))
                  intent (flow/main-intent t)
                  id (:id intent)
                  before @calls
                  result-file (str (io/file (:state-dir ctx) "inspected-not-applied.edn"))]
              (is (= :attention (:status t)))
              (is (= :done (get-in @(:state ctx) [:effects id :status])))
              (is (:pending (rt/main-merge-state ctx id)))
              (is (thrown-with-msg? Exception #"Own main mutation" (flow/integrate! ctx t)))
              (is (= before @calls) "An own unresolved mutation is not polled or repeated")
              (is (thrown-with-msg? Exception #"Own main mutation" (main/dispatch! ctx "retry" [(:key t)])))
              (is (= (:base t) (rt/git ctx repo "--git-dir" bare "rev-parse" "refs/heads/main")))
              (rt/reconcile-main-merge! ctx id {:outcome :not-applied :evidence "Inspected disposable remote main and OPEN fixture PR; no merge was applied"})
              (is (thrown-with-msg? Exception #"matching local reconciliation" (main/dispatch! ctx "retry" [(:key t)])))
              (rt/write-edn! result-file {:evidence "Inspected original command success against unchanged remote main and OPEN PR"
                                         :result {:not-applied true :main (:base t)}})
              (main/dispatch! ctx "reconcile" [id result-file])
              (is (= :done (get-in @(:state ctx) [:effects id :prior :status])))
              (main/dispatch! ctx "retry" [(:key t)])
              (let [next-intent (flow/main-intent (flow/task ctx (:key t)))]
                (is (not= id (:id next-intent)))
                (is (= (dissoc intent :id) (dissoc next-intent :id)))
                (is (nil? (get-in @(:state ctx) [:effects (:id next-intent)]))))
              (is (= 1 @requests) "Only the explicit later campaign transaction may issue another mutation")))})]
    (is (= :complete (:status state)))
    (is (= 1 merges))
    (is (= 2 merge-requests))
    (is (= 2 (count (filter #(str/starts-with? % "merge/") (keys (:effects state))))))))

(deftest evidence-changing-during-preflight-prevents-provider-mutation
  (doseq [changes [{:headRefOid "other"} {:baseRefName "other"}
                   {:statusCheckRollup [{:state "FAILURE"}]} {:reviewDecision "CHANGES_REQUESTED"}
                   {:mergeable "CONFLICTING"} {:mergeStateStatus "BLOCKED"}
                   :unresolved-thread :queued-workflow]]
    (let [{:keys [state merges merge-requests]}
          (run-fixture! {:verdict "approved" :summary "Exact fixture acceptance" :findings []}
                        {:pause-after-preflight true
                         :preflight-mutation (fn [pr] (when (map? changes) (swap! pr merge changes)))
                         :unresolved-thread (= :unresolved-thread changes)
                         :queued-workflow (= :queued-workflow changes)})]
      (is (= :paused (:status state)))
      (is (zero? merges))
      (is (zero? merge-requests)))))

(deftest complete-loop-verifies-real-git-result
  (let [{:keys [state merges audits calls tracking-locks]} (run-fixture! {:verdict "approved" :summary "Independent fixture approval" :findings []})
        t (get-in state [:tasks "remove-duplicate"])]
    (is (= :complete (:status state)))
    (is (= :done (:status t)))
    (is (= 1 merges))
    (is (= 2 audits))
    (is (= (repeat 5 :available) tracking-locks) "Post-merge tracking does not hold the main lock")
    (is (= (:merged t) (get-in t [:merge-verification :actual :commit])))
    (is (= (:candidate t) (get-in t [:review :candidate])))
    (is (= (:candidate t) (get-in t [:checks :candidate])))
    (is (some #(and (= "clojure" (first %)) (some #{"--candidate-tree"} %)) calls))
    (is (get-in state [:effects "close-campaign" :result]))))

(deftest unresolved-review-does-not-merge-or-complete
  (let [{:keys [state merges audits]} (run-fixture! {:verdict "needs_input" :summary "Contract ambiguity" :findings []})]
    (is (= :paused (:status state)))
    (is (= :attention (get-in state [:tasks "remove-duplicate" :status])))
    (is (zero? merges))
    (is (= 1 audits))))

(deftest rejects-stale-review-before-merge
  (let [ctx (fixture/context)
        t {:key "a" :status :ci :candidate "candidate" :base "base" :tree "tree" :worktree (:state-dir ctx)
           :checks {:candidate "candidate"} :review {:verdict "approved" :base "old-base" :candidate "candidate" :tree "tree"}}
        ctx (assoc-in ctx [:config :base-branch] "main")]
    (with-redefs [flow/pr-info (fn [& _] {:state "OPEN" :headRefOid "candidate" :baseRefName "main"
                                         :mergeable "MERGEABLE" :mergeStateStatus "CLEAN"
                                         :statusCheckRollup [{:state "SUCCESS"}]})
                  flow/fresh-main! (fn [_] "base")
                  gh/review-threads-clear? (fn [& _] true)
                  gh/head-workflow-state (fn [& _] {:state :passed :runs []})
                  gh/api (fn [& _] {:required_status_checks {:strict true}})]
      (is (thrown-with-msg? Exception #"does not match" (flow/integrate! ctx t))))))

(deftest moving-main-rechecks-and-reviews-composed-candidate
  (let [ctx (fixture/context)
        {:keys [repo]} (repository! ctx)
        base (rt/git ctx repo "rev-parse" "HEAD")
        ctx (assoc ctx :config {:repo-dir repo :worktree-root (str (io/file (:state-dir ctx) "workers"))})
        wt (flow/worktree! ctx "composition" base)
        _ (spit (io/file (:worktree wt) "work.edn") "{:value 1}\n")
        _ (rt/git ctx (:worktree wt) "commit" "-am" "Candidate")
        original (rt/git ctx (:worktree wt) "rev-parse" "HEAD")
        _ (spit (io/file repo "upstream.edn") "{:upstream true}\n")
        _ (rt/git ctx repo "add" "upstream.edn")
        _ (rt/git ctx repo "commit" "-m" "Independent main change")
        moved (rt/git ctx repo "rev-parse" "HEAD")
        checked (atom nil) reviewed (atom nil) published (atom nil)
        t (merge finding wt {:base base :candidate original :tree (rt/git ctx (:worktree wt) "rev-parse" "HEAD^{tree}")})]
    (rt/update! ctx assoc-in [:tasks (:key t)] t)
    (with-redefs [flow/checks! (fn [_ candidate _] (reset! checked (select-keys candidate [:base :candidate :tree])))
                  flow/review! (fn [_ candidate] (reset! reviewed (select-keys candidate [:base :candidate :tree])) {:verdict "approved"})
                  flow/transition! (fn [& _] nil)
                  flow/publish! (fn [_ candidate] (reset! published (select-keys candidate [:base :candidate :tree])))]
      (flow/compose! ctx t moved))
    (is (= moved (:base @reviewed)))
    (is (not= original (:candidate @reviewed)))
    (is (= @checked @reviewed @published))
    (is (= moved (rt/git ctx repo "rev-parse" "HEAD")))
    (is (.exists (io/file (:worktree wt) "upstream.edn")))))

(deftest actual-merge-mismatch-is-never-reported-done
  (let [ctx (fixture/context)
        {:keys [repo]} (repository! ctx)
        commit (rt/git ctx repo "rev-parse" "HEAD")
        ctx (assoc ctx :config {:repo-dir repo :base-branch "main"})]
    (with-redefs [flow/fresh-main! (fn [_] commit)
                  gh/head-workflow-state (fn [& _] {:state :passed :runs []})]
      ;; No parent on this initial commit also rejects an unverifiable merge.
      (is (thrown? Exception (flow/finish-merge! ctx (evidenced {:key "bad" :worktree repo :base "unexpected" :tree "unexpected" :candidate commit})
                                               {:state "MERGED" :baseRefName "main" :headRefOid commit :statusCheckRollup [{:state "SUCCESS"}] :mergeCommit {:oid commit}}))))
    (is (not= :done (get-in @(:state ctx) [:tasks "bad" :status])))))

(deftest checks-and-reviews-reject-candidate-identity-drift
  (let [ctx (fixture/context)
        {:keys [repo]} (repository! ctx)
        base (rt/git ctx repo "rev-parse" "HEAD")
        ctx (assoc ctx :config {:repo-dir repo :worktree-root (str (io/file (:state-dir ctx) "workers"))
                                :checks [["clojure" "-M:test"]] :check-executables #{"clojure"}})
        wt (flow/worktree! ctx "identity" base)
        t (merge finding wt {:base base :candidate base :tree (rt/git ctx repo "rev-parse" "HEAD^{tree}")})
        tuple (select-keys t [:base :candidate :tree])]
    (rt/update! ctx assoc-in [:tasks (:key t)] t)
    (is (nil? (flow/assert-identity! ctx t)))
    (is (thrown? Exception (flow/assert-identity! ctx (assoc t :branch "other"))))
    (is (thrown? Exception (flow/assert-identity! ctx (assoc t :tree "other"))))
    (with-redefs [codex/job! (fn [& _] (merge tuple {:candidate "stale" :verdict "approved"}))]
      (is (thrown-with-msg? Exception #"different candidate tuple" (flow/review! ctx t))))
    (is (nil? (:review (flow/task ctx (:key t)))))
    (with-redefs [codex/job! (fn [& _] (merge tuple {:verdict "changes_requested" :summary "No details" :findings []}))]
      (is (thrown-with-msg? Exception #"actionable findings" (flow/review! ctx t))))
    (with-redefs [codex/job! (fn [& _] (merge tuple {:verdict "approved" :summary "Exact fixture" :findings []}))]
      (is (= "approved" (:verdict (flow/review! ctx t)))))
    (with-redefs [codex/command! (fn [& _] (rt/git ctx (:worktree t) "commit" "--allow-empty" "-m" "Unexpected check commit"))]
      (is (thrown-with-msg? Exception #"identity differs" (flow/checks! ctx t []))))
    (is (nil? (:checks (flow/task ctx (:key t)))))
    (is (thrown? Exception (flow/worktree! ctx "identity" base)))))

(deftest dependent-authoring-base-includes-verified-prerequisite
  (let [ctx (fixture/context)
        {:keys [repo]} (repository! ctx)
        base (rt/git ctx repo "rev-parse" "HEAD")
        ctx (assoc ctx :config {:repo-dir repo :remote "origin" :base-branch "main"
                                :worktree-root (str (io/file (:state-dir ctx) "workers"))})
        wt (flow/worktree! ctx "dependent" base)
        t (merge finding wt {:base base :dependencies ["prerequisite"] :checks {:old true} :review {:old true}})]
    (spit (io/file repo "api.edn") "{:new-api true}\n")
    (rt/git ctx repo "add" "api.edn")
    (rt/git ctx repo "commit" "-m" "Merged prerequisite")
    (rt/git ctx repo "push" "origin" "main")
    (let [merged (rt/git ctx repo "rev-parse" "HEAD")]
      (rt/update! ctx assoc :tasks {(:key t) t "prerequisite" {:status :done :merged merged}})
      (flow/prepare-base! ctx t)
      (is (= merged (:base (flow/task ctx (:key t)))))
      (is (= merged (rt/git ctx (:worktree t) "rev-parse" "HEAD")))
      (is (.exists (io/file (:worktree t) "api.edn")))
      (is (nil? (:checks (flow/task ctx (:key t)))))
      (is (nil? (:review (flow/task ctx (:key t)))))
      ;; A repair keeps its existing implementation while incorporating new main.
      (spit (io/file (:worktree t) "work.edn") "{:value 1}\n")
      (rt/git ctx (:worktree t) "commit" "-am" "Candidate before repair")
      (flow/task! ctx (:key t) :candidate (rt/git ctx (:worktree t) "rev-parse" "HEAD")
                  :tree (rt/git ctx (:worktree t) "rev-parse" "HEAD^{tree}"))
      (spit (io/file repo "later.edn") "{:later true}\n")
      (rt/git ctx repo "add" "later.edn")
      (rt/git ctx repo "commit" "-m" "Later main")
      (rt/git ctx repo "push" "origin" "main")
      (flow/prepare-base! ctx (flow/task ctx (:key t)))
      (is (= "{:value 1}\n" (slurp (io/file (:worktree t) "work.edn"))))
      (is (.exists (io/file (:worktree t) "later.edn")))
      (flow/task! ctx "prerequisite" :merged (rt/git ctx (:worktree t) "rev-parse" "HEAD"))
      (is (thrown? Exception (flow/prepare-base! ctx (flow/task ctx (:key t))))))))

(deftest merge-commit-cannot-count-as-reviewed-squash
  (let [ctx (fixture/context)
        {:keys [repo]} (repository! ctx)
        ctx (update ctx :config assoc :repo-dir repo :base-branch "main")
        base (rt/git ctx repo "rev-parse" "HEAD")
        tree (rt/git ctx repo "rev-parse" "HEAD^{tree}")
        second-parent (rt/git ctx repo "commit-tree" tree "-p" base "-m" "Second parent")
        merged (rt/git ctx repo "commit-tree" tree "-p" base "-p" second-parent "-m" "Not a squash")]
    (with-redefs [flow/fresh-main! (fn [_] merged)
                  gh/head-workflow-state (fn [& _] {:state :passed :runs []})]
      (is (thrown-with-msg? Exception #"differs from reviewed composition"
                            (flow/finish-merge! ctx (evidenced {:key "bad" :worktree repo :base base :tree tree :candidate second-parent})
                                                {:state "MERGED" :baseRefName "main" :headRefOid second-parent :statusCheckRollup [{:state "SUCCESS"}] :mergeCommit {:oid merged}}))))
    (is (not= :done (:status (flow/task ctx "bad"))))))

(deftest changed-pr-base-or-head-cannot-enter-merge-verification
  (let [ctx (assoc-in (fixture/context) [:config :base-branch] "main")
        t {:key "finding" :base "base" :candidate "candidate"}
        calls (atom [])
        valid {:state "OPEN" :baseRefName "main" :headRefOid "candidate"
               :statusCheckRollup [{:state "SUCCESS"}]}]
    (doseq [info [(assoc valid :state "MERGED" :baseRefName "other")
                  (assoc valid :state "MERGED" :headRefOid "other")]]
      (with-redefs [flow/pr-info (fn [& _] info)
                    flow/finish-merge! (fn [& _] (swap! calls conj :verification))]
        (is (thrown? Exception (flow/integrate! ctx t)))))
    (doseq [changed [(assoc valid :baseRefName "other") (assoc valid :headRefOid "other")]]
      (let [reads (atom 0)]
        (with-redefs [flow/pr-info (fn [& _] (if (= 1 (swap! reads inc)) valid changed))
                      flow/fresh-main! (fn [_] "base")
                      gh/head-workflow-state (fn [& _] {:state :passed :runs []})
                      rt/run! (fn [& _] (swap! calls conj :mutation))]
          (flow/integrate! ctx t)
          (is (= 2 @reads)))))
    (is (empty? @calls))))

(deftest finding-issues-belong-to-their-campaign
  (let [first-ctx (assoc-in (fixture/context) [:config :campaign] "First campaign")
        second-ctx (assoc-in (fixture/context) [:config :campaign] "Second campaign")
        issues (atom [])
        ensure! #(gh/ensure-finding-issue! % "shared-key" "Finding" "Evidence" nil "simplify:duplication")]
    (with-redefs [gh/pages (fn [& _] @issues)
                  gh/api (fn [_ method _ body]
                           (is (= "POST" method))
                           (let [issue (assoc body :id (inc (count @issues)))]
                             (swap! issues conj issue)
                             issue))]
      (let [first-issue (ensure! first-ctx)
            second-issue (ensure! second-ctx)]
        (is (not= (:id first-issue) (:id second-issue)))
        (is (= first-issue (ensure! first-ctx)))
        (is (= second-issue (ensure! second-ctx)))
        (is (= 2 (count @issues)))
        ;; Existing same-campaign state remains authoritative for legacy IDs.
        (rt/update! first-ctx assoc-in [:tasks "shared-key" :issue] {:id "legacy-issue"})
        (is (= {:id "legacy-issue"} (ensure! first-ctx)))
        (is (= second-issue (ensure! second-ctx)))
        (is (= 2 (count @issues)))))))
