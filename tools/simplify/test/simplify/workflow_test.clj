(ns simplify.workflow-test
  "Local Git integration with deterministic GitHub/model doubles. No real agents or GitHub writes."
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [simplify.codex :as codex]
            [simplify.core-test :as fixture]
            [simplify.github :as gh]
            [simplify.runtime :as rt]
            [simplify.workflow :as flow]))

(def finding {:key "remove-duplicate" :title "Remove duplicate work" :evidence "The same constant is repeated"
              :proposal "Keep the required value once" :preserve "Value remains one"
              :paths ["work.edn"] :dependencies [] :kind "duplication"})

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

(defn run-fixture! [review-result]
  (let [ctx (fixture/context)
        {:keys [repo bare]} (repository! ctx)
        ctx (assoc ctx :config {:repo-dir repo :repository "fixture/repo" :remote "origin" :base-branch "main"
                                :campaign "fixture" :scope ["."] :parallelism 2 :poll-ms 1
                                :worktree-root (str (io/file (:state-dir ctx) "workers"))
                                :checks [["clojure" "-M" "fixture-check.clj"]] :check-executables #{"clojure"}})
        _ (.mkdirs (io/file (get-in ctx [:config :worktree-root])))
        calls (atom []) audits (atom 0) pr (atom nil) merge-count (atom 0)
        original-command rt/command
        response (fn [value] {:exit 0 :out (json/write-str value) :err ""})]
    (with-redefs
      [rt/command (fn
                    ([c cwd argv]
                     (swap! calls conj argv)
                     (cond
                       (= "clojure" (first argv)) {:exit 0 :out "fixture check passed" :err ""}
                       (= ["gh" "pr" "view"] (subvec (vec argv) 0 (min 3 (count argv))))
                       (response (merge {:state "OPEN" :baseRefName "main" :isDraft false :reviewDecision "APPROVED"
                                         :statusCheckRollup [{:status "COMPLETED" :conclusion "SUCCESS"}]}
                                        @pr))
                       (= ["gh" "pr" "merge"] (subvec (vec argv) 0 (min 3 (count argv))))
                       (let [t (first (flow/tasks c))
                             tree (rt/git c cwd "rev-parse" (str (:candidate t) "^{tree}"))
                             merged (rt/git c cwd "--git-dir" bare "commit-tree" tree "-p" (:base t) "-m" "Fixture squash")]
                         (rt/git c cwd "--git-dir" bare "update-ref" "refs/heads/main" merged (:base t))
                         (swap! merge-count inc)
                         (swap! pr assoc :state "MERGED" :mergeCommit {:oid merged})
                         {:exit 0 :out "merged" :err ""})
                       :else (original-command c cwd argv)))
                    ([c cwd argv input] (original-command c cwd argv input)))
       gh/setup! (fn [_] nil)
       gh/status! (fn [& _] nil)
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
                 (if (and (= "POST" method) (str/ends-with? endpoint "/pulls"))
                   (let [t (first (flow/tasks c))]
                     (reset! pr {:headRefOid (:candidate t)})
                     {:number 3 :node_id "pr" :html_url "https://example.invalid/pr/3" :state "open"})
                   {:state (:state body)})))
       codex/command! (fn [_ _ _ argv] (swap! calls conj argv) {:exitCode 0 :stdout "fixture command" :stderr ""})
       codex/job! (fn [_ job cwd _ _ _ _]
                    (cond
                      (str/starts-with? job "audit-")
                      {:coverage ["work.edn"] :summary "Fixture audit" :findings (if (= 1 (swap! audits inc)) [finding] [])}
                      (str/starts-with? job "implement-")
                      (do (spit (io/file cwd "work.edn") "{:value 1}\n")
                          {:outcome "ready" :summary "Removed duplicate" :checks []})
                      :else review-result))]
      (flow/campaign! ctx))
    {:state @(:state ctx) :calls @calls :merges @merge-count :audits @audits :repo repo :ctx ctx}))

(deftest complete-loop-verifies-real-git-result
  (let [{:keys [state merges audits calls]} (run-fixture! {:verdict "approved" :summary "Independent fixture approval" :findings []})
        t (get-in state [:tasks "remove-duplicate"])]
    (is (= :complete (:status state)))
    (is (= :done (:status t)))
    (is (= 1 merges))
    (is (= 2 audits))
    (is (= (:candidate t) (get-in t [:review :candidate])))
    (is (= (:candidate t) (get-in t [:checks :candidate])))
    (is (some #(and (= "clojure" (first %)) (some #{"--candidate-tree"} %)) calls))
    (is (get-in state [:effects "close-campaign" :result]))))

(deftest unresolved-review-does-not-merge-or-complete
  (let [{:keys [state merges audits]} (run-fixture! {:verdict "needs_input" :summary "Contract ambiguity" :findings []})]
    (is (= :attention (:status state)))
    (is (= :attention (get-in state [:tasks "remove-duplicate" :status])))
    (is (zero? merges))
    (is (= 1 audits))))

(deftest rejects-stale-review-before-merge
  (let [ctx (fixture/context)
        t {:key "a" :status :ci :candidate "candidate" :base "base" :tree "tree" :worktree (:state-dir ctx)
           :checks {:candidate "candidate"} :review {:verdict "approved" :base "old-base" :candidate "candidate" :tree "tree"}}
        ctx (assoc-in ctx [:config :base-branch] "main")]
    (with-redefs [flow/pr-info (fn [& _] {:state "OPEN" :headRefOid "candidate" :baseRefName "main"
                                         :statusCheckRollup [{:state "SUCCESS"}]})
                  flow/fresh-main! (fn [_] "base")
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
        ctx (assoc ctx :config {:repo-dir repo})]
    (with-redefs [flow/fresh-main! (fn [_] commit)]
      ;; No parent on this initial commit also rejects an unverifiable merge.
      (is (thrown? Exception (flow/finish-merge! ctx {:key "bad" :worktree repo :base "unexpected" :tree "unexpected"}
                                               {:mergeCommit {:oid commit}}))))
    (is (not= :done (get-in @(:state ctx) [:tasks "bad" :status])))))
