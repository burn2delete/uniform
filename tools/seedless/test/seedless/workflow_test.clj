(ns seedless.workflow-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is use-fixtures]]
            [seedless.github :as gh]
            [seedless.main :as main]
            [seedless.schema :as schema]
            [seedless.workflow :as w]
            [simplify.codex :as codex]
            [simplify.github :as transport]
            [simplify.runtime :as rt]
            [simplify.schema :as validation]
            [simplify.workflow :as shared])
  (:import [java.nio.file Files]))

(use-fixtures :each (fn [test] (with-redefs [rt/event! (fn [& _])] (test))))

(defn context []
  (let [root (.toFile (Files/createTempDirectory "seedless-test-" (make-array java.nio.file.attribute.FileAttribute 0)))
        dir (io/file root "state") _ (.mkdirs dir)]
    {:root root :state-dir (str dir) :state-file (str (io/file dir "state.edn"))
     :control-file (str (io/file dir "control.edn")) :log-lock (Object.) :processes (atom #{})
     :state (atom {:schema :seedless/v1 :tasks {} :jobs {} :effects {}})}))

(defn delete-tree! [root]
  (doseq [file (reverse (file-seq (io/file root)))] (Files/deleteIfExists (.toPath file))))

(defn repository! [ctx]
  (let [root (:root ctx) repo (io/file root "repo") bare (io/file root "remote.git")]
    (.mkdirs repo)
    (rt/git ctx root "init" "--bare" (str bare))
    (rt/git ctx repo "init" "-b" "main")
    (rt/git ctx repo "config" "user.name" "Seedless fixture")
    (rt/git ctx repo "config" "user.email" "seedless@example.invalid")
    (spit (io/file repo "module.clj") "(ns fixture)\n(def seed :clojure)\n")
    (rt/git ctx repo "add" "module.clj")
    (rt/git ctx repo "commit" "-m" "fixture base")
    (rt/git ctx repo "remote" "add" "origin" (str bare))
    (rt/git ctx repo "push" "-u" "origin" "main")
    (assoc ctx :config {:repo-dir (str repo) :repository "fixture/repo" :remote "origin" :base-branch "main"
                        :campaign "fixture" :project-title "Uniform Seedless" :objective "Fixture seed removal"
                        :contracts ["module.clj"] :worktree-root (str (io/file root "worktrees"))
                        :parallelism 2 :poll-ms 1 :checks [main/boundary-check]
                        :completion-checks [main/completion-check] :check-executables #{"clojure"}})))

(defn slice [key]
  {:key key :title (str "Replace " key) :stage "SH-07" :workstream "compiler" :kind "adoption"
   :evidence "Existing hosted leaf still selected" :seed-paths ["module.clj"] :uniform-paths ["module.clj"]
   :consumer "Real compiler call" :proposal "Select the source-owned leaf" :preserve "Order and diagnostics"
   :positive "Actual caller with fallback disabled" :negative "Malformed input rejection"
   :residual-host-boundary "Hosted runner remains" :paths ["module.clj"] :dependencies [] :stack-on ""})

(defn add-task! [ctx s]
  (rt/update! ctx assoc-in [:tasks (:key s)]
              (assoc s :description s :status :ready :issue {:number 1 :id 1 :node_id "issue1"})))

(defn harness [scenario operation]
  (let [ctx (repository! (context)) original rt/command
        events (atom []) phases (atom {}) prs (atom {}) next-pr (atom 0)
        pr-data (fn [[key p]]
                  (let [t (w/task ctx key)
                        remote (rt/git ctx (get-in ctx [:config :repo-dir]) "ls-remote" "origin" (str "refs/heads/" (:branch t)))
                        head (first (str/split remote #"\s+"))]
                    (merge (:pr p)
                           {:state (if (= "MERGED" (:state p)) "closed" "open")
                            :merged (= "MERGED" (:state p)) :body (:body p)
                            :head {:ref (:branch t) :sha head :repo {:full_name "fixture/repo"}}
                            :base {:ref (:base p) :repo {:full_name "fixture/repo"}}})))]
    (try
      (with-redefs
       [rt/event! (fn [& _])
        gh/status! (fn [_ _ status] (swap! events conj [:status status]))
        gh/comment! (fn [& _])
        gh/open-review-threads? (fn [& _] false)
        gh/api (fn
                 ([_ endpoint]
                  (cond
                    (re-find #"/pulls/[0-9]+$" endpoint)
                    (some (fn [[_ p :as entry]]
                            (when (str/ends-with? endpoint (str "/" (get-in p [:pr :number]))) (pr-data entry))) @prs)
                    (str/includes? endpoint "pulls?state=all&head=")
                    (mapv pr-data (filter (fn [[key _]] (str/ends-with? endpoint (str ":" (:branch (w/task ctx key))))) @prs))
                    :else []))
                 ([_ method endpoint body]
                  (swap! events conj [:api method endpoint body])
                  (cond
                    (and (= "POST" method) (str/ends-with? endpoint "/pulls"))
                    (let [n (swap! next-pr inc) key (:key (some #(when (= (:branch %) (:head body)) %) (w/tasks ctx)))
                          pr {:number n :html_url (str "fixture-pr-" n) :node_id (str "pr" n)}]
                      (swap! prs assoc key {:pr pr :draft true :base (:base body) :body (:body body) :state "OPEN"})
                      (pr-data [key (get @prs key)]))
                    (and (= "PATCH" method) (str/includes? endpoint "/pulls/"))
                    (do (swap! prs (fn [m] (into {} (map (fn [[key p]] [key (if (str/ends-with? endpoint (str "/" (get-in p [:pr :number])))
                                                                              (assoc p :base (:base body)) p)]) m)))) {})
                    :else {})))
        shared/pr-info (fn [_ t]
                         (let [p (get @prs (:key t))]
                           {:state (:state p) :headRefOid (:candidate t) :baseRefName (:base p) :isDraft (:draft p)
                            :mergeable "MERGEABLE" :reviewDecision nil :mergeCommit {:oid (:merged p)}
                            :statusCheckRollup [{:status "COMPLETED" :conclusion "SUCCESS"}]}))
        codex/command! (fn [_ _ _ argv]
                         (swap! events conj [:check argv])
                         (when (and (= scenario :release-gap) (= main/completion-check argv))
                           (throw (ex-info "P18T04007: fixture still requires seed" {})))
                         {:exitCode 0})
        codex/job! (fn [_ _ cwd mode _ _ output-schema]
                     (let [t (some #(when (= cwd (:worktree %)) %) (w/tasks ctx)) key (:key t)]
                       (cond
                         (= schema/plan output-schema)
                         (do (swap! events conj [:plan])
                             {:coverage ["module.clj"] :source-closure ["module.clj"] :target "Fixture target"
                              :subset "Required fixture closure" :summary "Fixture audit" :blockers []
                              :slices (if (w/task ctx "a") [] [(slice "a")])})
                         (= mode :write)
                         (let [phase (get (swap! phases update key (fnil inc 0)) key)
                               file (io/file cwd "module.clj") text (slurp file)]
                           (swap! events conj [:author key phase])
                           (spit file (str (if (str/includes? text "<<<<<<<")
                                             (str/join "\n" (remove #(or (str/starts-with? % "<<<<<<<") (= % "=======") (str/starts-with? % ">>>>>>>")) (str/split-lines text)))
                                             text)
                                           "\n(def " key "-" phase " true)\n"))
                           {:outcome (if (= phase 1) "checkpoint" "ready") :summary "Fixture source change"
                            :evidence "Actual fixture changes" :residual-host-boundary "Fixture host remains"
                            :checks [["clojure" "-M" "focused.clj"]]})
                         :else
                         (let [t (or t {:base (rt/git ctx cwd "rev-parse" "HEAD")
                                        :candidate (rt/git ctx cwd "rev-parse" "HEAD")
                                        :tree (rt/git ctx cwd "rev-parse" "HEAD^{tree}") :kind "completion"})]
                           (swap! events conj [:review key (:base t)])
                           (merge (select-keys t [:base :candidate :tree])
                                  {:verdict "approved" :evidence-scope (:kind t) :summary "Independent fixture review"
                                   :findings [] :residual-host-boundary "Fixture host remains"})))))
        rt/command (fn [context cwd argv & input]
                     (cond
                       (= "clojure" (first argv)) (do (swap! events conj [:preflight argv]) {:exit 0 :out "passed" :err ""})
                       (= ["gh" "pr" "ready"] (vec (take 3 argv)))
                       (do (swap! prs (fn [m] (into {} (map (fn [[k p]] [k (if (= (get-in p [:pr :html_url]) (nth argv 3)) (assoc p :draft false) p)]) m))))
                           {:exit 0 :out "ready" :err ""})
                       (= ["gh" "pr" "merge"] (vec (take 3 argv)))
                       (let [t (some #(when (= (get-in % [:pr :html_url]) (nth argv 3)) %) (w/tasks ctx))
                             repo (get-in ctx [:config :repo-dir])]
                         (swap! events conj [:merge argv])
                         (when (= scenario :race)
                           (spit (io/file repo "external.txt") "external writer")
                           (rt/git ctx repo "add" "external.txt") (rt/git ctx repo "commit" "-m" "external main"))
                         (rt/git ctx repo "merge" "--no-ff" (:candidate t) "-m" "fixture merge")
                         (rt/git ctx repo "push" "origin" "main")
                         (swap! prs update (:key t) assoc :state "MERGED" :merged (rt/git ctx repo "rev-parse" "HEAD"))
                         {:exit 0 :out "merged" :err ""})
                       :else (apply original context cwd argv input)))]
        (operation ctx events phases prs))
      (finally (delete-tree! (:root ctx))))))

(deftest campaign-discovers-implements-merges-and-verifies-or-retains-release-gap
  (doseq [scenario [:normal :release-gap]]
    (harness scenario
             (fn [ctx events _ _]
               (rt/update! ctx assoc :campaign-issue {:number 100 :id 100 :node_id "campaign"})
               (with-redefs [gh/setup! (fn [& _]) gh/pages (fn [& _] [])
                             gh/issue! (fn [& _] {:number 1 :id 1 :node_id "issue1"})
                             gh/milestone! (fn [& _] {:number 1}) gh/relation! (fn [& _])]
                 (w/campaign! ctx))
               (is (= :done (:status (w/task ctx "a"))))
               (is (= 1 (count (filter #(= :merge (first %)) @events))))
               (is (= 1 (count (filter #(= [:check main/completion-check] %) @events))))
               (is (= (if (= scenario :normal) :complete :attention) (:status @(:state ctx))))
               (is (= (if (= scenario :normal) :passed :failed) (get-in @(:state ctx) [:completion :result])))))))

(deftest early-draft-checks-review-and-real-merge
  (harness :normal
           (fn [ctx events _ _]
             (add-task! ctx (slice "a")) (w/prepare! ctx "a")
             (let [e @events draft (first (keep-indexed #(when (and (= :api (first %2)) (= "POST" (second %2))) %1) e))
                   second-author (.indexOf e [:author "a" 2])]
               (is (< draft second-author))
               (is (< draft (first (keep-indexed #(when (= :check (first %2)) %1) e))))
               (is (true? (:draft (nth (nth e draft) 3)))))
             (w/integrate! ctx (w/task ctx "a"))
             (is (= :done (:status (w/task ctx "a"))))
             (is (= (:tree (w/task ctx "a")) (rt/git ctx (get-in ctx [:config :repo-dir]) "rev-parse" "HEAD^{tree}")))
             (is (not-any? #(some #{"--squash" "--admin" "--force"} (second %)) (filter #(= :merge (first %)) @events))))))

(deftest stacked-child-starts-before-parent-merge-and-retargets-after-it
  (harness :normal
           (fn [ctx _ _ prs]
             (add-task! ctx (slice "a")) (w/prepare! ctx "a")
             (add-task! ctx (assoc (slice "b") :dependencies ["a"] :stack-on "a"))
             (is (w/eligible? ctx (w/task ctx "b") []))
             (w/prepare! ctx "b")
             (is (= (:branch (w/task ctx "a")) (get-in @prs ["b" :base])))
             (is (= (:candidate (w/task ctx "a")) (:base (w/task ctx "b"))))
             (w/integrate! ctx (w/task ctx "a"))
             (w/integrate! ctx (w/task ctx "b"))
             (is (= "main" (get-in @prs ["b" :base])))
             (is (= (:merged (w/task ctx "a")) (:base (w/task ctx "b"))))
             (w/integrate! ctx (w/task ctx "b"))
             (is (= :done (:status (w/task ctx "b"))))
             (rt/git ctx (get-in ctx [:config :repo-dir]) "merge-base" "--is-ancestor"
                     (:merged (w/task ctx "a")) (:merged (w/task ctx "b"))))))

(deftest moving-main-composes-without-unnecessary-author-edits
  (harness :normal
           (fn [ctx events phases _]
             (add-task! ctx (slice "a")) (w/prepare! ctx "a")
             (let [repo (get-in ctx [:config :repo-dir]) old (:candidate (w/task ctx "a"))]
               (spit (io/file repo "other.txt") "new main")
               (rt/git ctx repo "add" "other.txt") (rt/git ctx repo "commit" "-m" "main movement")
               (rt/git ctx repo "push" "origin" "main")
               (w/integrate! ctx (w/task ctx "a"))
               (is (not= old (:candidate (w/task ctx "a")))))
             (is (= 2 (get @phases "a")))
             (is (= 2 (count (filter #(= :review (first %)) @events)))))))

(deftest stacked-base-must-contain-other-verified-prerequisites
  (harness :normal
           (fn [ctx _ _ _]
             (add-task! ctx (slice "a")) (w/prepare! ctx "a")
             (let [repo (get-in ctx [:config :repo-dir])]
               (spit (io/file repo "runtime.txt") "required runtime")
               (rt/git ctx repo "add" "runtime.txt") (rt/git ctx repo "commit" "-m" "runtime prerequisite")
               (rt/git ctx repo "push" "origin" "main")
               (add-task! ctx (slice "runtime"))
               (w/task! ctx "runtime" :status :done :merged (rt/git ctx repo "rev-parse" "HEAD"))
               (add-task! ctx (assoc (slice "b") :dependencies ["a" "runtime"] :stack-on "a"))
               (is (not (w/eligible? ctx (w/task ctx "b") [])))
               (w/merge-base! ctx (w/task ctx "a") (:merged (w/task ctx "runtime")))
               (is (w/eligible? ctx (w/task ctx "b") []))))))

(deftest owned-conflict-is-repaired-without-dropping-incoming-main
  (harness :normal
           (fn [ctx _ _ _]
             (add-task! ctx (slice "a")) (w/prepare! ctx "a")
             (let [repo (get-in ctx [:config :repo-dir])]
               (spit (io/file repo "module.clj") "(ns fixture)\n(def seed :clojure)\n(def incoming true)\n")
               (rt/git ctx repo "add" "module.clj") (rt/git ctx repo "commit" "-m" "conflicting main")
               (rt/git ctx repo "push" "origin" "main"))
             (w/integrate! ctx (w/task ctx "a"))
             (is (= :repair (:status (w/task ctx "a"))))
             (is (:merge-in-progress (w/task ctx "a")))
             (w/prepare! ctx "a")
             (is (str/includes? (slurp (io/file (:worktree (w/task ctx "a")) "module.clj")) "incoming"))
             (is (str/includes? (slurp (io/file (:worktree (w/task ctx "a")) "module.clj")) "a-2"))
             (w/integrate! ctx (w/task ctx "a"))
             (is (= :done (:status (w/task ctx "a")))))))

(deftest actual-merge-race-does-not-close-the-issue
  (harness :race
           (fn [ctx events _ _]
             (add-task! ctx (slice "a")) (w/prepare! ctx "a")
             (is (thrown-with-msg? Exception #"Actual merge differs" (w/integrate! ctx (w/task ctx "a"))))
             (is (not= :done (:status (w/task ctx "a"))))
             (is (not-any? #(and (= :api (first %)) (= "completed" (get-in % [3 :state_reason]))) @events)))))

(deftest inspected-merge-observation-resumes-without-replaying-the-merge
  (harness :normal
           (fn [ctx events phases _]
             (add-task! ctx (slice "a")) (w/prepare! ctx "a")
             (with-redefs [w/finish-merge! (fn [& _] (throw (ex-info "Observation interrupted" {})))]
               (try (w/integrate! ctx (w/task ctx "a"))
                    (catch Exception e (w/attention! ctx "a" e))))
             (let [file (io/file (:root ctx) "inspection.edn")]
               (spit file (pr-str {:evidence "Inspected actual fixture merge and preserved parents"}))
               (main/retry! ctx "a" (str file)))
             (is (= :ci (:status (w/task ctx "a"))))
             (w/integrate! ctx (w/task ctx "a"))
             (is (= :done (:status (w/task ctx "a"))))
             (is (= 2 (get @phases "a")))
             (is (= 1 (count (filter #(= :merge (first %)) @events)))))))

(deftest planning-contracts-and-frozen-writer-ownership
  (let [ctx (context) p (slice "parent") child (assoc (slice "child") :status :ready :dependencies ["parent"] :stack-on "parent")
        plan {:coverage ["bootstrap/"] :source-closure ["module.clj"] :target "declared target" :subset "required closure"
              :summary "Concrete work" :blockers [] :slices [p (dissoc child :status)]}]
    (try
      (is (= plan (validation/validate! schema/plan plan)))
      (is (= plan (w/validate-plan! plan {})))
      (is (thrown? Exception (w/validate-plan! (assoc plan :slices [(assoc p :dependencies ["child"]) child]) {})))
      (is (thrown? Exception (w/validate-plan! (assoc plan :slices [(assoc p :paths ["../outside"])]) {})))
      (is (thrown? Exception (w/validate-plan! (assoc plan :slices [p (assoc (dissoc child :status) :key "other")])
                                               {"parent" (assoc p :dependencies ["other"])})))
      (add-task! ctx p)
      (w/task! ctx "parent" :status :ci :candidate "frozen")
      (is (w/eligible? ctx child []))
      (is (not (w/eligible? ctx child [(assoc p :status :implementing)])))
      (w/task! ctx "parent" :status :implementing)
      (is (not (w/eligible? ctx child [])))
      (finally (delete-tree! (:root ctx))))))

(deftest completion-needs-checks-independent-review-and-unchanged-main
  (let [ctx (repository! (context)) repo (get-in ctx [:config :repo-dir]) base (rt/git ctx repo "rev-parse" "HEAD")
        tree (rt/git ctx repo "rev-parse" "HEAD^{tree}") checks (atom [])
        completion (w/worktree! ctx "completion-fixture" base)]
    (try
      (rt/update! ctx assoc :last-plan {:base base :slices []} :campaign-issue {:number 1})
      (with-redefs [w/fresh-main! (constantly base)
                    w/worktree! (fn [& _] completion)
                    codex/command! (fn [_ _ _ argv] (swap! checks conj argv))
                    w/job! (fn [& _] {:base base :candidate base :tree tree :verdict "approved"
                                      :evidence-scope "completion" :summary "Verified fixture evidence" :findings []
                                      :residual-host-boundary "No fixture release seed dependency"})
                    gh/status! (fn [& _]) gh/api (fn [& _] {})]
        (w/complete! ctx)
        (is (= [main/completion-check] @checks))
        (is (= :complete (:status @(:state ctx))))
        (is (= base (get-in @(:state ctx) [:completion :base]))))
      (finally (delete-tree! (:root ctx))))))

(deftest a-failed-or-moving-completion-review-cannot-finish
  (doseq [scenario [:rejected :moving]]
    (let [ctx (repository! (context)) repo (get-in ctx [:config :repo-dir]) base (rt/git ctx repo "rev-parse" "HEAD")
          reads (atom 0) statuses (atom [])]
      (try
        (rt/update! ctx assoc :last-plan {:base base :slices []})
        (with-redefs [w/fresh-main! (fn [_] (if (and (= scenario :moving) (> (swap! reads inc) 1)) "new-main" base))
                      w/worktree! (fn [& _] {:worktree repo :branch "main"}) codex/command! (fn [& _])
                      w/review! (fn [& _] {:verdict (if (= scenario :rejected) "changes_requested" "approved")})
                      gh/status! (fn [_ _ status] (swap! statuses conj status))]
          (is (thrown? Exception (w/complete! ctx)))
          (is (not= :complete (:status @(:state ctx))))
          (is (empty? @statuses)))
        (finally (delete-tree! (:root ctx)))))))

(deftest frozen-ci-does-not-impose-an-audit-round-barrier
  (let [ctx (assoc (context) :config {:parallelism 1 :poll-ms 1}) audits (atom 0)]
    (try
      (rt/update! ctx assoc :tasks {"frozen" {:key "frozen" :status :ci :candidate "head" :paths ["module.clj"]}})
      (with-redefs [gh/setup! (fn [& _]) w/campaign-issue! (fn [& _] {}) gh/status! (fn [& _])
                    w/integrate! (fn [& _]) w/fresh-main! (constantly "base")
                    w/plan! (fn [_] (swap! audits inc) (rt/write-edn! (:control-file ctx) {:paused true}) {:slices []})]
        (w/campaign! ctx)
        (is (= 1 @audits))
        (is (= :paused (:status @(:state ctx)))))
      (finally (delete-tree! (:root ctx))))))

(deftest planning-uncertainty-cannot-start-another-model-turn
  (let [ctx (repository! (context)) repo (get-in ctx [:config :repo-dir]) base (rt/git ctx repo "rev-parse" "HEAD")
        calls (atom 0)]
    (try
      (rt/update! ctx assoc :planning {:base base :round 1 :worktree {:worktree repo :branch "main"} :effect "plan/1"}
                  :effects {"plan/1" {:status :uncertain}})
      (with-redefs [codex/job! (fn [& _] (swap! calls inc))]
        (is (thrown-with-msg? Exception #"needs reconciliation" (w/plan! ctx)))
        (is (zero? @calls)))
      (finally (delete-tree! (:root ctx))))))

(deftest configuration-keeps-the-canonical-gates-and-shared-lock
  (let [ctx (repository! (context)) file (io/file (:root ctx) "config.edn")
        config (assoc (:config ctx) :state-dir (:state-dir ctx) :scope ["."])]
    (try
      (spit file (pr-str config))
      (with-redefs [main/probe! (fn [context argv]
                                  (cond
                                    (= ["git" "check-ref-format" "--branch"] (vec (take 3 argv))) "main"
                                    (= ["git" "rev-parse" "--show-toplevel"] argv) (get-in context [:config :repo-dir])
                                    (= ["git" "rev-parse" "--git-common-dir"] argv) (str (io/file (get-in context [:config :repo-dir]) ".git"))
                                    :else "git@github.com:fixture/repo.git"))]
        (let [loaded (main/config! (str file)) saved (main/context! loaded)]
          (is (str/ends-with? (:lock-file saved) "/simplify-harness.lock"))
          (rt/save! saved)
          (is (thrown? Exception (main/context! (assoc loaded :objective "Another objective"))))))
      (spit file (pr-str (assoc config :completion-checks [["clojure" "-e" "(println :passed)"]])))
      (is (thrown? Exception (main/config! (str file))))
      (spit file (pr-str (assoc config :checks [])))
      (is (thrown? Exception (main/config! (str file))))
      (finally (delete-tree! (:root ctx))))))

(deftest empty-audit-cannot-bypass-canonical-verifier
  (let [ctx (repository! (context)) base (rt/git ctx (get-in ctx [:config :repo-dir]) "rev-parse" "HEAD") calls (atom 0)]
    (try
      (rt/update! ctx assoc :last-plan {:base base :slices []})
      (with-redefs [rt/event! (fn [& _]) w/fresh-main! (constantly base)
                    w/worktree! (fn [& _] {:worktree (get-in ctx [:config :repo-dir]) :branch "main"})
                    codex/command! (fn [_ _ _ argv] (swap! calls inc) (is (= main/completion-check argv))
                                     (throw (ex-info "P18T04007: seed still required" {})))
                    w/review! (fn [& _] (throw (ex-info "must not review failed release" {})))]
        (is (thrown? Exception (w/complete! ctx)))
        (is (= :failed (get-in @(:state ctx) [:completion :result])))
        (is (not= :complete (:status @(:state ctx))))
        (is (thrown? Exception (w/complete! ctx)))
        (is (= 1 @calls)))
      (finally (delete-tree! (:root ctx))))))

(deftest stale-independent-review-is-rejected
  (let [ctx (repository! (context)) repo (get-in ctx [:config :repo-dir]) base (rt/git ctx repo "rev-parse" "HEAD")
        t (assoc (w/worktree! ctx "review-fixture" base) :base base :candidate base :tree (rt/git ctx repo "rev-parse" "HEAD^{tree}"))]
    (try
      (with-redefs [rt/event! (fn [& _]) w/job! (fn [& _] (assoc (select-keys t [:base :candidate :tree]) :candidate "wrong"
                                                                 :verdict "approved" :evidence-scope "adoption"))]
        (is (thrown-with-msg? Exception #"Review does not bind" (w/review! ctx t "adoption"))))
      (finally (delete-tree! (:root ctx))))))

(deftest denied-and-uncertain-effects-are-not-replayed
  (let [ctx (context) calls (atom 0)]
    (try
      (with-redefs [rt/event! (fn [& _])]
        (is (thrown? Exception (rt/effect! ctx "publish" #(do (swap! calls inc) (throw (ex-info "connection lost" {}))))))
        (is (thrown? Exception (rt/effect! ctx "publish" #(swap! calls inc))))
        (is (= 1 @calls))
        (rt/update! ctx assoc-in [:tasks "denied"] {:status :attention :error {:details {:restricted true}}})
        (let [file (io/file (:root ctx) "inspection.edn")]
          (spit file (pr-str {:evidence "Inspected original denial"}))
          (is (thrown? Exception (main/retry! ctx "denied" (str file))))))
      (finally (delete-tree! (:root ctx))))))

(deftest project-title-alone-is-not-adoption-authority
  (let [ctx (assoc (context) :config {:repository "fixture/repo"})]
    (try
      (with-redefs [gh/graphql (fn [& _] {:node {:id "p" :readme "Unrelated Project"
                                                 :repositories {:nodes [{:nameWithOwner "fixture/repo"}]}}})]
        (is (thrown? Exception (gh/verify-project! ctx {:id "p"}))))
      (finally (delete-tree! (:root ctx))))))

(deftest project-setup-has-seedless-identity-views-and-labels
  (let [ctx (assoc (context) :config {:repository "fixture/repo" :project-title "Uniform Seedless" :objective "Retire declared seed"})
        identity (atom {:id "project" :owner {:id "owner" :login "fixture"} :readme "" :repositories {:nodes []}})
        writes (atom []) fields [{:id "status" :name "Status" :options [{:id "todo" :name "Todo"} {:id "done" :name "Done"} {:id "progress" :name "In Progress"}]}]]
    (try
      (with-redefs [transport/owner-info (fn [& _] {:id "owner"}) transport/projects (fn [& _] []) transport/fields (fn [& _] fields)
                    gh/pages (fn [& _] [])
                    gh/api (fn ([_ _] {:node_id "repository"}) ([_ method endpoint body] (swap! writes conj [method endpoint body]) {}))
                    gh/graphql (fn [_ query variables]
                                 (swap! writes conj [:graphql query variables])
                                 (cond
                                   (str/includes? query "createProjectV2(") {:createProjectV2 {:projectV2 {:id "project" :title "Uniform Seedless"}}}
                                   (str/includes? query "owner{") {:node @identity}
                                   (str/includes? query "linkProjectV2ToRepository(")
                                   (do (swap! identity assoc :repositories {:nodes [{:nameWithOwner "fixture/repo"}]})
                                       {:linkProjectV2ToRepository {:repository {:id "repository"}}})
                                   (str/includes? query "updateProjectV2(")
                                   (do (swap! identity assoc :readme (:readme variables)) {:updateProjectV2 {:projectV2 @identity}})
                                   (str/includes? query "updateProjectV2Field(") {:updateProjectV2Field {:projectV2Field {:id "status" :options (mapv #(assoc % :id (or (:id %) (:name %))) (:options variables))}}}
                                   (str/includes? query "views(first:") {:node {:views {:nodes []}}}
                                   (str/includes? query "createProjectV2View(") {:createProjectV2View {:projectV2View {:id (:name variables)}}}
                                   :else {}))]
        (gh/setup! ctx)
        (is (= "project" (get-in @(:state ctx) [:project :id])))
        (is (= #{"All work" "Workflow" "SH milestones" "Attention"}
               (set (map #(get-in % [2 :name]) (filter #(and (= :graphql (first %)) (str/includes? (second %) "createProjectV2View(")) @writes)))))
        (is (some #(= "seedless:adoption" (get-in % [2 :name])) @writes))
        (is (some #(and (= :graphql (first %))
                        (str/includes? (or (get-in % [2 :readme]) "") (gh/project-marker ctx))) @writes))
        (is (some #(and (= :graphql (first %))
                        (some (fn [option] (= {:name "Done" :id "done"} (select-keys option [:name :id]))) (get-in % [2 :options]))) @writes)))
      (finally (delete-tree! (:root ctx))))))

(deftest ci-diagnostics-bind-logs-to-the-current-source-head
  (let [ctx (assoc (context) :config {:repository "fixture/repo" :repo-dir "."})
        check {:status "COMPLETED" :conclusion "FAILURE" :detailsUrl "https://github.com/fixture/repo/actions/runs/11/job/22"}]
    (try
      (with-redefs [gh/api (fn [_ endpoint] (if (str/includes? endpoint "/runs/") {:head_sha "head"} {:run_id 11 :conclusion "failure"}))
                    rt/run! (fn [& _] "Actual failed test; ghp_examplecredential")]
        (let [evidence (first (w/ci-evidence! ctx {:candidate "head"} [check])) text (slurp (:log-path evidence))]
          (is (str/includes? text "Actual failed test"))
          (is (str/includes? text "[REDACTED]"))
          (is (= (rt/digest text) (:captured-text-sha256 evidence))))
        (is (thrown-with-msg? Exception #"different candidate" (w/ci-evidence! ctx {:candidate "other"} [check]))))
      (finally (delete-tree! (:root ctx))))))

(deftest shared-transport-records-seedless-client-identity
  (let [ctx (assoc (context) :config {:client-info {:name "uniform_seedless" :title "Uniform Seedless" :version "0.1.0"}})
        requests (atom [])]
    (try
      (with-redefs [codex/open! (fn [& _] {}) codex/close! (fn [& _]) codex/send! (fn [& _])
                    codex/request! (fn [_ method params] (swap! requests conj [method params])
                                     (case method "thread/start" {:thread {:id "thread"} :model "fixture-model"}
                                           "turn/start" {:turn {:id "turn"}} {}))
                    codex/await-turn! (fn [& _] "{\"value\":\"observed\"}") rt/event! (fn [& _])]
        (is (= {:value "observed"} (codex/job! ctx "fixture" (:state-dir ctx) :read "instructions" "input"
                                               (simplify.schema/object-of {:value simplify.schema/text}))))
        (is (= "uniform_seedless" (get-in @requests [0 1 :clientInfo :name]))))
      (finally (delete-tree! (:root ctx))))))
