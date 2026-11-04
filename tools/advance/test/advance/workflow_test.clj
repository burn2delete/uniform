(ns advance.workflow-test
  "Real Git candidates and squash receipts with deterministic model/GitHub doubles."
  (:require [advance.github :as project]
            [advance.main :as main]
            [advance.workflow :as workflow]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [simplify.codex :as codex]
            [simplify.coordination :as coordination]
            [simplify.core-test :as fixture]
            [simplify.github :as gh]
            [simplify.runtime :as rt]
            [simplify.workflow :as flow]
            [simplify.workflow-test :as git-fixture]))

(def source-path "specs/demo/tasks.md")
(def source-key (str source-path "#demo/value"))
(def increment {:key "preserve-value" :task source-key :title "Implement the value increment"
                :evidence "Current value has duplicate representation" :proposal "Preserve one value and record scoped progress"
                :preserve "The value remains one; the broader parent remains open"
                :paths ["work.edn" source-path] :dependencies []})

(defn run-loop! [local-failure? & [composition-failure?]]
  (let [ctx (fixture/context)
        {:keys [repo bare]} (git-fixture/repository! ctx)
        directory (io/file repo "specs/demo")
        _ (.mkdirs directory)
        _ (spit (io/file repo source-path) "# Tasks\n<!-- demo-implementation-tasks:start -->\n- [ ] `demo/value` Complete value behavior\nDone when: value and all additional behavior independently accepted.\n<!-- demo-implementation-tasks:end -->\n")
        _ (rt/git ctx repo "add" source-path)
        _ (rt/git ctx repo "commit" "-m" "Current task authority")
        _ (rt/git ctx repo "push" "origin" "main")
        config (merge main/defaults {:repo-dir repo :repository "fixture/repo" :parallelism 1 :poll-ms 1
                                     :worktree-root (str (io/file (:state-dir ctx) "workers"))
                                     :checks [["clojure" "-M" "fixture-check.clj"]]})
        ctx (assoc ctx :config config :before-implement workflow/validate-source!
                       :coordination-dir (str (io/file (:state-dir ctx) "coordination"))
                       :lock-file (str (io/file (:state-dir ctx) "advance-harness.lock")))
        pr (atom nil) sequence (atom []) selections (atom 0) implementations (atom 0) checks (atom 0) readies (atom 0)
        issues (atom {}) closed (atom []) original-command rt/command
        response (fn [value] {:exit 0 :out (json/write-str value) :err ""})]
    (with-redefs
      [project/setup! (fn [_] nil)
       project/settings (fn [_] {})
       gh/status! (fn [& _] nil)
       gh/relation! (fn [& _] nil)
       gh/comment! (fn [& _] nil)
       gh/ensure-issue! (fn [_ key title _ _ _]
                          (or (get @issues key)
                              (let [issue {:id (inc (count @issues)) :number (inc (count @issues)) :node_id key :title title}]
                                (swap! issues assoc key issue) issue)))
       gh/draft! (fn [_ _ draft? _]
                   (swap! sequence conj (if draft? :draft-again :ready))
                   (swap! pr assoc :draft draft? :isDraft draft?)
                   (when (and (not draft?) (= 1 (swap! readies inc)) composition-failure?)
                     (spit (io/file repo "upstream.edn") "{:upstream true}\n")
                     (rt/git ctx repo "add" "upstream.edn")
                     (rt/git ctx repo "commit" "-m" "Concurrent main increment")
                     (rt/git ctx repo "push" "origin" "main")))
       gh/api (fn
                ([_ endpoint]
                 (if (str/includes? endpoint "pulls?state") (if @pr [@pr] [])
                     (throw (ex-info "Unexpected fixture API read" {:endpoint endpoint}))))
                ([c method endpoint body]
                 (if (and (= "POST" method) (str/ends-with? endpoint "/pulls"))
                   (let [t (first (flow/tasks c))
                         created {:number 50 :node_id "pr" :html_url "https://example.invalid/pr/50"
                                  :state "open" :draft (:draft body) :isDraft (:draft body) :headRefOid (:candidate t)}]
                     (is (true? (:draft body)))
                     (swap! sequence conj :draft-created)
                     (reset! pr created))
                   (do (when (= "closed" (:state body)) (swap! closed conj endpoint)) {:state (:state body)}))))
       rt/command (fn
                    ([c cwd argv]
                     (cond
                       (= "clojure" (first argv)) (do (swap! sequence conj :preflight) {:exit 0 :out "fixture preflight" :err ""})
                       (= ["gh" "pr" "view"] (vec (take 3 argv)))
                       (response (merge {:baseRefName "main" :reviewDecision "APPROVED"
                                         :statusCheckRollup [{:status "COMPLETED" :conclusion "SUCCESS"}]}
                                        @pr {:state (if (= "merged" (:state @pr)) "MERGED" "OPEN")}))
                       (= ["gh" "pr" "merge"] (vec (take 3 argv)))
                       (let [t (first (flow/tasks c))
                             merged (rt/git c cwd "--git-dir" bare "commit-tree" (:tree t) "-p" (:base t) "-m" "Fixture squash")]
                         (rt/git c cwd "--git-dir" bare "update-ref" "refs/heads/main" merged (:base t))
                         (swap! pr assoc :state "merged" :mergeCommit {:oid merged})
                         (swap! sequence conj :merged)
                         {:exit 0 :out "merged" :err ""})
                       :else (let [result (original-command c cwd argv)]
                               (when (and @pr (= ["git" "push"] (vec (take 2 argv))))
                                 (swap! pr assoc :headRefOid (:candidate (first (flow/tasks c)))))
                               result)))
                    ([c cwd argv input] (original-command c cwd argv input)))
       codex/command! (fn [_ _ _ argv]
                        (is (= "clojure" (first argv)))
                        (is (:isDraft @pr) "Draft is visible before local checks")
                        (swap! sequence conj :checked)
                        (let [n (swap! checks inc)]
                          (when (or (and local-failure? (= 1 n)) (and composition-failure? (= 2 n)))
                            (throw (ex-info "Sandboxed check failed" {:command argv :result {:exitCode 1}}))))
                        {:exitCode 0})
       codex/job! (fn [c job cwd _ _ _ _]
                    (cond
                      (str/starts-with? job "select-")
                      (if (= 1 (swap! selections inc))
                        {:summary "Implement a finite value increment" :coverage [source-path] :increments [increment] :blockers []}
                        (do (rt/write-edn! (:control-file c) {:paused true})
                            {:summary "Parent still needs additional behavior" :coverage [source-path]
                             :increments [] :blockers [{:task source-key :reason "Remaining broader task acceptance"}]}))
                      (str/starts-with? job "implement-")
                      (do (swap! implementations inc)
                          (spit (io/file cwd "work.edn") (str "{:value 1 :revision " @implementations "}\n"))
                          (spit (io/file cwd source-path) (str "\nScoped increment " @implementations " implemented; parent acceptance remains open.\n") :append true)
                          {:outcome "ready" :summary "Actual scoped implementation" :checks []})
                      :else
                      (do (is (:isDraft @pr) "PR remains draft during independent review")
                          (is (empty? (coordination/claims c)) "Immutable candidates do not reserve active writer paths")
                          (swap! sequence conj :reviewed)
                          (merge (select-keys (first (flow/tasks c)) [:base :candidate :tree])
                                 {:verdict "approved" :summary "Independent exact-candidate review" :findings []}))))]
      (workflow/campaign! ctx))
    {:ctx ctx :state @(:state ctx) :sequence @sequence :closed @closed :implementations @implementations :selections @selections}))

(deftest full-loop-publishes-early-and-verifies-squash-with-parent-still-open
  (let [{:keys [ctx state sequence closed selections]} (run-loop! false)
        t (get-in state [:tasks "preserve-value"])]
    (is (= [:draft-created :checked :reviewed :ready :preflight :merged] sequence))
    (is (= :done (:status t)))
    (is (:merged t))
    (is (= :paused (:status state)))
    (is (= 2 selections) "Continues selecting after the verified increment")
    (is (not (get-in state [:source-tasks source-key :checked])))
    (is (= 1 (count closed)) "Only the increment closes")
    (is (empty? (coordination/claims ctx)))
    (is (= (:candidate t) (get-in t [:checks :candidate]) (get-in t [:review :candidate])))))

(deftest actual-local-check-failure-returns-to-repair-and-preserves-draft
  (let [{:keys [state implementations sequence]} (run-loop! true)]
    (is (= 2 implementations))
    (is (= 2 (count (filter #{:checked} sequence))))
    (is (= 1 (count (filter #{:merged} sequence))))
    (is (= :done (get-in state [:tasks "preserve-value" :status])))))

(deftest moving-main-composition-failure-repairs-and-reviews-the-new-candidate
  (let [{:keys [state implementations sequence]} (run-loop! false true)
        task (get-in state [:tasks "preserve-value"])]
    (is (= 2 implementations))
    (is (= 3 (count (filter #{:checked} sequence))))
    (is (= 2 (count (filter #{:reviewed} sequence))))
    (is (= 1 (count (filter #{:merged} sequence))))
    (is (= :done (:status task)))
    (is (= (:candidate task) (get-in task [:review :candidate]) (get-in task [:checks :candidate])))
    (is (.exists (io/file (get-in state [:catalog :worktree]) "upstream.edn")))))

(deftest externally-merged-drafts-cannot-bypass-independent-acceptance
  (let [ctx (fixture/context) t {:key "unaccepted" :base "base" :candidate "candidate" :tree "tree"}]
    (with-redefs [flow/pr-info (fn [& _] {:state "MERGED" :headRefOid "candidate" :mergeCommit {:oid "external"}
                                         :statusCheckRollup [{:state "SUCCESS"}]})
                  gh/status! (fn [& _] (throw (AssertionError. "Unaccepted merge cannot be marked Done")))]
      (is (thrown-with-msg? Exception #"evidence does not match" (flow/integrate! ctx t))))
    (is (nil? (get-in @(:state ctx) [:tasks "unaccepted" :merged])))))

(deftest queue-exhaustion-cannot-establish-project-completion
  (let [ctx (fixture/context)]
    (with-redefs [codex/command! (fn [& _] (throw (AssertionError. "Open tasks cannot reach release checks")))]
      (is (nil? (workflow/complete! ctx {:catalog {:tasks [{:checked false}] :unclassified []}})))
      (is (nil? (workflow/complete! ctx {:catalog {:tasks [] :unclassified []}})))
      (is (nil? (workflow/complete! ctx {:catalog {:tasks [{:checked true}] :unclassified ["unknown/tasks.md"]}}))))))

(deftest completion-needs-executed-checks-exact-review-and-current-main
  (let [ctx (assoc (fixture/context) :config {:completion-checks [["clojure" "-M:release-check"]]
                                             :check-executables #{"clojure"}})
        snapshot {:base "base" :worktree (:state-dir ctx) :catalog {:tasks [{:checked true}] :unclassified []}}
        checked (atom [])
        review (atom {:base "base" :tree "tree" :verdict "complete" :summary "Fixture release evidence verified"
                      :evidence ["immutable fixture receipt"] :remaining []})
        main (atom "base")]
    (with-redefs [rt/git (fn [& _] "tree") flow/assert-clean! (fn [& _] nil)
                  flow/fresh-main! (fn [_] @main)
                  codex/command! (fn [_ _ _ command] (swap! checked conj command))
                  codex/job! (fn [& _] @review)]
      ;; HEAD and tree are distinct identities, even in this deterministic fixture.
      (with-redefs [rt/git (fn [_ _ & args] (if (= ["rev-parse" "HEAD"] (vec args)) "base" "tree"))]
        (is (true? (workflow/complete! ctx snapshot)))
        (is (= [["clojure" "-M:release-check"]] @checked))
        (reset! main "new-main")
        (is (false? (workflow/complete! ctx snapshot)))
        (reset! main "base")
        (swap! review assoc :verdict "incomplete" :remaining ["Unaccepted final seed retirement"])
        (is (false? (workflow/complete! ctx snapshot)))
        (swap! review assoc :base "different-source")
        (is (thrown-with-msg? Exception #"different source" (workflow/complete! ctx snapshot)))))))

(deftest campaign-is-tracked-without-resetting-operator-state-on-resume
  (let [ctx (fixture/context) statuses (atom [])]
    (with-redefs [gh/ensure-issue! (fn [& _] {:number 1 :node_id "campaign"})
                  gh/status! (fn [_ issue status] (swap! statuses conj [(:number issue) status]))]
      (workflow/ensure-campaign! ctx)
      (workflow/ensure-campaign! ctx)
      (is (= [[1 "Proposed"]] @statuses))
      (is (true? (:campaign-tracked @(:state ctx)))))))

(deftest operator-controls-have-independent-priority-and-execution-meaning
  (let [settings {1 {"Priority" "Highest" "Control" "Paused"} 2 {"Priority" "Low" "Control" "Active"}}]
    (is (= 0 (project/priority settings 1)))
    (is (= 3 (project/priority settings 2)))
    (is (not (project/runnable? settings 1)))
    (is (project/runnable? settings 2))
    (is (project/runnable? settings 3))))
