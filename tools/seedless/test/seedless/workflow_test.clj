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
    (merge (rt/coordination root {:state-dir (str dir)})
           {:root root :state-dir (str dir) :state-file (str (io/file dir "state.edn"))
            :control-file (str (io/file dir "control.edn")) :log-lock (Object.) :processes (atom #{})
            :state (atom {:schema :seedless/v1 :tasks {} :jobs {} :effects {}})})))

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
    (merge ctx (rt/coordination (io/file repo ".git") {:state-dir (:state-dir ctx)})
           {:config {:repo-dir (str repo) :repository "fixture/repo" :remote "origin" :base-branch "main"
                     :campaign "fixture" :project-title "Uniform Seedless" :objective "Fixture seed removal"
                     :contracts ["module.clj"] :worktree-root (str (io/file root "worktrees"))
                     :parallelism 2 :poll-ms 1 :checks [main/boundary-check]
                     :completion-checks [main/completion-check] :check-executables #{"clojure"}}})))

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
        ownership! (fn [phase]
                     (let [file (io/file (:coordination-file ctx))
                           state (when (.isFile file) (rt/read-edn file))]
                       (swap! events conj [:ownership phase (count (:writers state))
                                           (some? @(:main-lock-owner ctx))])))
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
        transport/head-workflow-state (fn [& _] {:state :passed :runs [{:path ".github/workflows/ci.yml" :status "completed" :conclusion "success"}]})
        gh/status! (fn [_ _ status] (ownership! [:status status]) (swap! events conj [:status status]))
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
                         (ownership! :check)
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
                           (ownership! :author)
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
                           (ownership! :review)
                           (swap! events conj [:review key (:base t)])
                           (merge (select-keys t [:base :candidate :tree])
                                  {:verdict "approved" :evidence-scope (:kind t) :summary "Independent fixture review"
                                   :findings [] :residual-host-boundary "Fixture host remains"})))))
        rt/command (fn [context cwd argv & input]
                     (cond
                       (= "clojure" (first argv)) (do (ownership! :preflight) (swap! events conj [:preflight argv]) {:exit 0 :out "passed" :err ""})
                       (= ["gh" "pr" "ready"] (vec (take 3 argv)))
                       (do (swap! prs (fn [m] (into {} (map (fn [[k p]] [k (if (= (get-in p [:pr :html_url]) (nth argv 3)) (assoc p :draft false) p)]) m))))
                           {:exit 0 :out "ready" :err ""})
                       (= ["gh" "pr" "merge"] (vec (take 3 argv)))
                       (let [t (some #(when (= (get-in % [:pr :html_url]) (nth argv 3)) %) (w/tasks ctx))
                             repo (get-in ctx [:config :repo-dir])]
                         (ownership! :merge)
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

(deftest ordinary-ended-check-failure-repairs-the-existing-draft
  (harness :normal
           (fn [ctx events phases prs]
             (add-task! ctx (slice "a"))
             (let [original codex/command! fail? (atom true)]
               (with-redefs [codex/command!
                             (fn [context job cwd argv]
                               (if (compare-and-set! fail? true false)
                                 (throw (ex-info "Sandboxed check failed" {:command argv :result {:exitCode 1 :stderr "fixture assertion"}
                                                                           :writer-ended? true :restricted false}))
                                 (original context job cwd argv)))]
                 (w/prepare! ctx "a")))
             (let [t (w/task ctx "a") failure (first (:check-failures t))]
               (is (= :ci (:status t)))
               (is (= 3 (get @phases "a")))
               (is (= 1 (count @prs)))
               (is (= 1 (count (:check-failures t))))
               (is (= (:base t) (:base failure)))
               (is (string? (:tree failure)))
               (is (not= (:candidate t) (:candidate failure)))
               (is (= :passed (get-in t [:checks :result])))
               (is (not-any? #(= [:status "Attention"] %) @events))
               (is (empty? (:writers (rt/read-edn (:coordination-file ctx)))))))))

(deftest failed-composition-check-retains-the-new-base-for-source-repair
  (harness :normal
           (fn [ctx _ phases _]
             (add-task! ctx (slice "a")) (w/prepare! ctx "a")
             (let [repo (get-in ctx [:config :repo-dir]) old (:candidate (w/task ctx "a"))]
               (spit (io/file repo "other.txt") "new main")
               (rt/git ctx repo "add" "other.txt") (rt/git ctx repo "commit" "-m" "new composition base")
               (rt/git ctx repo "push" "origin" "main")
               (let [base (rt/git ctx repo "rev-parse" "HEAD")]
                 (with-redefs [codex/command! (fn [_ _ _ argv]
                                                (throw (ex-info "Sandboxed check failed" {:command argv :result {:exitCode 1}
                                                                                          :writer-ended? true :restricted false})))]
                   (w/integrate! ctx (w/task ctx "a")))
                 (let [t (w/task ctx "a")]
                   (is (= :repair (:status t)))
                   (is (= base (:base t)))
                   (is (not= old (:candidate t)))
                   (is (= (:candidate t) (:published-candidate t)))
                   (is (= 1 (count (:check-failures t))))
                   (is (= base (get-in t [:check-failures 0 :base])))
                   (is (= 2 (get @phases "a")))
                   (is (empty? (:writers (rt/read-edn (:coordination-file ctx)))))
                   (is (empty? (:main-effects (rt/read-edn (:coordination-file ctx)))))))))))

(deftest denied-or-unended-checks-never-queue-another-author
  (doseq [failure [{:restricted true :writer-ended? true}
                   {:restricted false :writer-ended? false}]]
    (harness :normal
             (fn [ctx _ phases _]
               (add-task! ctx (slice "a"))
               (with-redefs [codex/command! (fn [_ _ _ argv]
                                              (throw (ex-info "Sandboxed check failed"
                                                              (merge {:command argv :result {:exitCode 1}} failure))))]
                 (try (w/prepare! ctx "a") (catch Exception e (w/attention! ctx "a" e))))
               (is (= :attention (:status (w/task ctx "a"))))
               (is (= 2 (get @phases "a")))
               (is (empty? (:check-failures (w/task ctx "a"))))
               (let [claims (vals (:writers (rt/read-edn (:coordination-file ctx))))]
                 (if (:writer-ended? failure)
                   (is (empty? claims))
                   (is (= [:needs-inspection] (mapv :status claims)))))))))

(deftest actual-merge-race-does-not-close-the-issue
  (harness :race
           (fn [ctx events _ _]
             (add-task! ctx (slice "a")) (w/prepare! ctx "a")
             (is (thrown-with-msg? Exception #"Actual merge differs" (w/integrate! ctx (w/task ctx "a"))))
             (is (not= :done (:status (w/task ctx "a"))))
             (is (seq (:main-effects (rt/read-edn (:coordination-file ctx))))
                 "An unverified actual write keeps the shared durable fence")
             (is (= :fenced (rt/try-with-main-lock ctx (constantly :must-not-merge))))
             (is (not-any? #(and (= :api (first %)) (= "completed" (get-in % [3 :state_reason]))) @events)))))

(deftest an-unverified-main-write-keeps-attention-local
  (harness :race
           (fn [ctx events _ _]
             (add-task! ctx (slice "a")) (w/prepare! ctx "a")
             (let [error (try (w/integrate! ctx (w/task ctx "a")) nil (catch Exception e e))
                   before @events]
               (is (:main-effect (ex-data error)))
               (w/attention! ctx "a" error)
               (is (= before @events))
               (is (= :attention (:status (w/task ctx "a"))))
               (is (seq (:main-effects (rt/read-edn (:coordination-file ctx)))))))))

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

(deftest writer-check-review-and-merge-have-distinct-ownership-phases
  (harness :normal
           (fn [ctx events _ _]
             (add-task! ctx (slice "a")) (w/prepare! ctx "a")
             (w/integrate! ctx (w/task ctx "a"))
             (doseq [phase [:author :check]]
               (let [entries (filter #(= [:ownership phase] (vec (take 2 %))) @events)]
                 (is (seq entries))
                 (is (every? #(= [1 false] (subvec % 2)) entries))))
             (doseq [phase [:review [:status "Done"]]]
               (let [entries (filter #(= [:ownership phase] (vec (take 2 %))) @events)]
                 (is (seq entries))
                 (is (every? #(= [0 false] (subvec % 2)) entries))))
             (doseq [phase [:preflight :merge]]
               (is (some #(= [:ownership phase 0 true] %) @events)))
             (is (empty? (:main-effects (rt/read-edn (:coordination-file ctx)))))
             (is (= :main-verified (:kind (last (:history (rt/read-edn (:coordination-file ctx))))))))))

(defn peer-context [ctx]
  (let [directory (io/file (:root ctx) "peer-state") _ (.mkdirs directory)]
    (merge ctx (rt/coordination (io/file (get-in ctx [:config :repo-dir]) ".git") {:state-dir (str directory)})
           {:state-dir (str directory) :state-file (str (io/file directory "state.edn"))
            :state (atom {:tasks {} :effects {}})
            :config (assoc (:config ctx) :campaign "another-campaign" :state-dir (str directory))})))

(deftest another-campaign-can-plan-but-cannot-write-an-active-owned-path
  (harness :normal
           (fn [ctx events _ _]
             (let [peer (peer-context ctx)
                   lease (rt/claim-paths! peer "other" ["module.clj"] :checking)]
               (is (= :parallel (rt/with-lock ctx #(rt/with-lock peer (constantly :parallel)))))
               (is (= (:main-lock-file ctx) (:main-lock-file peer)))
               (add-task! ctx (slice "a")) (w/prepare! ctx "a")
               (is (not-any? #(= :author (first %)) @events))
               (rt/release-paths! peer lease)
               (w/prepare! ctx "a")
               (is (= :ci (:status (w/task ctx "a"))))
               (is (empty? (:writers (rt/read-edn (:coordination-file ctx)))))))))

(deftest another-campaigns-main-lock-waits-without-replaying-a-merge
  (harness :normal
           (fn [ctx events _ _]
             (let [peer (peer-context ctx)]
               (add-task! ctx (slice "a")) (w/prepare! ctx "a")
               (rt/try-with-main-lock peer #(w/integrate! ctx (w/task ctx "a")))
               (is (not-any? #(= :merge (first %)) @events))
               (w/integrate! ctx (w/task ctx "a"))
               (is (= 1 (count (filter #(= :merge (first %)) @events))))))))

(deftest provider-evidence-is-refreshed-after-preflight-before-merge
  (doseq [drift [:ci :review :base :head :threads]]
    (harness :normal
             (fn [ctx events _ _]
               (add-task! ctx (slice "a")) (w/prepare! ctx "a")
               (let [original shared/pr-info calls (atom 0) threads (atom 0)]
                 (with-redefs [shared/pr-info
                               (fn [context t]
                                 (let [info (original context t)]
                                   (if (< (swap! calls inc) 3) info
                                       (case drift
                                         :ci (assoc info :statusCheckRollup [{:status "COMPLETED" :conclusion "FAILURE"}])
                                         :review (assoc info :reviewDecision "CHANGES_REQUESTED")
                                         :base (assoc info :baseRefName "foreign")
                                         :head (assoc info :headRefOid "foreign")
                                         info))))
                               gh/open-review-threads? (fn [& _] (and (= :threads drift) (> (swap! threads inc) 1)))]
                   (if (#{:ci :review :threads} drift)
                     (w/integrate! ctx (w/task ctx "a"))
                     (is (thrown? Exception (w/integrate! ctx (w/task ctx "a"))))))
                 (is (some #(= :preflight (first %)) @events))
                 (is (not-any? #(= :merge (first %)) @events))
                 (is (empty? (:main-effects (rt/read-edn (:coordination-file ctx))))))))))

(deftest external-review-repairs-current-head-and-waits-for-reapproval
  (doseq [ci-failed? [false true]]
    (harness :normal
             (fn [ctx events phases _]
               (add-task! ctx (slice "a")) (w/prepare! ctx "a")
               (let [t (w/task ctx "a") original shared/pr-info
                     request {:id 17 :user {:id 3} :state "CHANGES_REQUESTED" :commit_id (:candidate t)
                              :submitted_at "2026-11-04T12:00:00Z" :body "Preserve the rejected case"}
                     comments [{:pull_request_review_id 17 :path "module.clj" :body "Add the missing rejection"}]
                     failed-checks [{:status "COMPLETED" :conclusion "FAILURE"}]]
                 (with-redefs [shared/pr-info (fn [context task]
                                                (cond-> (assoc (original context task) :reviewDecision "CHANGES_REQUESTED")
                                                  (and ci-failed? (= (:candidate t) (:candidate task)))
                                                  (assoc :statusCheckRollup failed-checks)))
                               transport/pages (fn [_ endpoint]
                                                 (cond
                                                   (str/ends-with? endpoint "/reviews") [request]
                                                   (str/ends-with? endpoint "/comments") (conj comments {:pull_request_review_id 999 :body "Unrelated historical comment"})
                                                   :else (throw (ex-info "Unexpected fixture page" {:endpoint endpoint}))))]
                   (w/integrate! ctx t)
                   (is (= :repair (:status (w/task ctx "a"))))
                   (is (= [request] (get-in (w/task ctx "a") [:feedback :github-reviews])))
                   (is (= comments (get-in (w/task ctx "a") [:feedback :inline-comments])))
                   (is (= (select-keys t [:base :candidate :tree]) (get-in (w/task ctx "a") [:feedback :candidate])))
                   (when ci-failed?
                     (is (= failed-checks (get-in (w/task ctx "a") [:feedback :ci])))
                     (is (= :external-or-unavailable (get-in (w/task ctx "a") [:feedback :diagnostics 0 :logs]))))
                   (w/prepare! ctx "a")
                   (is (not= (:candidate t) (:candidate (w/task ctx "a"))))
                   (let [authors @phases]
                     (w/integrate! ctx (w/task ctx "a"))
                     (is (= :ci (:status (w/task ctx "a"))))
                     (is (= authors @phases))
                     (is (not-any? #(= :merge (first %)) @events)))
                   (with-redefs [shared/pr-info (fn [context task] (assoc (original context task) :reviewDecision "APPROVED"))]
                     (w/integrate! ctx (w/task ctx "a")))
                   (is (= :done (:status (w/task ctx "a"))))
                   (is (= 1 (count (filter #(= :merge (first %)) @events))))))))))

(deftest superseded-review-opinions-wait-and-foreign-base-refuses-feedback
  (harness :normal
           (fn [ctx events phases _]
             (add-task! ctx (slice "a")) (w/prepare! ctx "a")
             (let [t (w/task ctx "a") original shared/pr-info authors @phases pages (atom 0)
                   reviews [{:id 11 :user {:id 3} :state "CHANGES_REQUESTED" :commit_id (:candidate t) :submitted_at "2026-11-01T12:00:00Z"}
                            {:id 12 :user {:id 3} :state "APPROVED" :commit_id (:candidate t) :submitted_at "2026-11-02T12:00:00Z"}
                            {:id 13 :user {:id 4} :state "CHANGES_REQUESTED" :commit_id "old-head" :submitted_at "2026-11-03T12:00:00Z"}]]
               (with-redefs [shared/pr-info (fn [context task] (assoc (original context task) :reviewDecision "CHANGES_REQUESTED"))
                             transport/pages (fn [_ endpoint]
                                               (swap! pages inc)
                                               (if (str/ends-with? endpoint "/reviews") reviews
                                                   (throw (ex-info "No current findings need comments" {}))))]
                 (w/integrate! ctx t)
                 (is (= :ci (:status (w/task ctx "a"))))
                 (is (= 1 @pages)))
               (reset! pages 0)
               (with-redefs [shared/pr-info (fn [context task] (assoc (original context task) :reviewDecision "CHANGES_REQUESTED" :baseRefName "foreign"))
                             transport/pages (fn [& _] (swap! pages inc))]
                 (is (thrown-with-msg? Exception #"Review does not target this PR base" (w/integrate! ctx t))))
               (is (zero? @pages))
               (is (= authors @phases))
               (is (not-any? #(= :merge (first %)) @events))))))

(deftest queued-workflow-with-green-rollup-waits-at-each-merge-gate
  (doseq [gate [1 2 3]]
    (harness :normal
             (fn [ctx events phases _]
               (add-task! ctx (slice "a")) (w/prepare! ctx "a")
               (let [calls (atom 0) authors @phases
                     queued {:state :pending :runs [{:path ".github/workflows/ci.yml" :status "queued"}]}]
                 (with-redefs [transport/head-workflow-state
                               (fn [_ candidate]
                                 (is (= (:candidate (w/task ctx "a")) candidate))
                                 (if (>= (swap! calls inc) gate) queued {:state :passed :runs []}))]
                   (w/integrate! ctx (w/task ctx "a")))
                 (is (= gate @calls))
                 (is (= (= gate 3) (boolean (some #(= :preflight (first %)) @events))))
                 (is (not-any? #(= :merge (first %)) @events))
                 (is (empty? (:main-effects (rt/read-edn (:coordination-file ctx)))))
                 (is (= :pending (get-in (w/task ctx "a") [:head-workflows :state])))
                 (is (not= :attention (:status (w/task ctx "a"))))
                 (w/integrate! ctx (w/task ctx "a"))
                 (is (= :done (:status (w/task ctx "a"))))
                 (is (= authors @phases)))))))

(deftest failed-workflow-with-green-rollup-retains-evidence-for-repair
  (harness :normal
           (fn [ctx events _ _]
             (add-task! ctx (slice "a")) (w/prepare! ctx "a")
             (let [failed {:state :failed :runs [{:id 19 :path ".github/workflows/ci.yml" :status "completed" :conclusion "failure"}]}]
               (with-redefs [transport/head-workflow-state (fn [& _] failed)]
                 (w/integrate! ctx (w/task ctx "a")))
               (is (= :repair (:status (w/task ctx "a"))))
               (is (= (assoc failed :candidate (:candidate (w/task ctx "a")))
                      (get-in (w/task ctx "a") [:feedback :workflows])))
               (is (not-any? #(#{:preflight :merge} (first %)) @events))))))

(deftest command-success-with-unverified-merge-needs-original-journal-inspection
  (harness :normal
           (fn [ctx events _ _]
             (add-task! ctx (slice "a")) (w/prepare! ctx "a")
             (with-redefs [w/verify-merge! (fn [& _] (throw (ex-info "Verification interrupted" {})))]
               (try (w/integrate! ctx (w/task ctx "a"))
                    (catch Exception e (w/attention! ctx "a" e))))
             (let [t (w/task ctx "a") id (:merge-effect t)
                   file (io/file (:root ctx) "main-inspection.edn")
                   info (shared/pr-info ctx t) commit (get-in info [:mergeCommit :oid])
                   before @(:state ctx) queries (atom 0)]
               (is (= :done (get-in before [:effects id :status])))
               (is (:pending (rt/main-merge-state ctx id)))
               (spit file (pr-str {:evidence "Actual fixture merge inspected"}))
               (is (thrown-with-msg? Exception #"reconcile-main" (main/retry! ctx "a" (str file))))
               (is (= before @(:state ctx)))
               (with-redefs [shared/pr-info (fn [& _] (swap! queries inc))]
                 (is (thrown-with-msg? Exception #"reconcile-main" (w/integrate! ctx t))))
               (is (zero? @queries))
               (let [observed (w/fresh-main! ctx)
                     actual {:commit commit :tree (:tree t) :parents [(:base t) (:candidate t)] :main observed}]
                 (is (= "" (rt/git ctx (get-in ctx [:config :repo-dir]) "merge-base" "--is-ancestor" commit observed)))
                 (spit file (pr-str {:evidence "Inspected real fixture Git parents/tree, PR merge and observed main reachability" :outcome :merged :actual actual}))
                 (main/dispatch! ctx "reconcile-main" [id (str file)]))
               (main/retry! ctx "a" (str file))
               (w/integrate! ctx (w/task ctx "a"))
               (is (= :done (:status (w/task ctx "a"))))
               (is (= 1 (count (filter #(= :merge (first %)) @events))))))))

(deftest inspected-not-applied-merge-needs-local-settlement-and-explicit-retry
  (doseq [receipt [:uncertain :done :restricted]]
    (harness :normal
             (fn [ctx events _ _]
               (add-task! ctx (slice "a")) (w/prepare! ctx "a")
               (let [original rt/command file (io/file (:root ctx) "not-applied.edn")]
                 (with-redefs [rt/command
                               (fn [context cwd argv & input]
                                 (if (= ["gh" "pr" "merge"] (vec (take 3 argv)))
                                   (if (= :done receipt) {:exit 0 :out "No mutation" :err ""}
                                       (throw (ex-info "Fixture response lost before mutation" {:restricted (= :restricted receipt)})))
                                   (apply original context cwd argv input)))]
                   (try (w/integrate! ctx (w/task ctx "a"))
                        (catch Exception e (w/attention! ctx "a" e))))
                 (let [t (w/task ctx "a") id (:merge-effect t)]
                   (is (= receipt (get-in @(:state ctx) [:effects id :status])))
                   (spit file (pr-str {:evidence "Fixture PR remains open; no Git mutation occurred" :outcome :not-applied}))
                   (main/dispatch! ctx "reconcile-main" [id (str file)])
                   (is (thrown? Exception (main/retry! ctx "a" (str file))))
                   (is (not-any? #(= :merge (first %)) @events))
                   (spit file (pr-str {:evidence "Original receipt settled against shared inspection" :result {:outcome :not-applied}}))
                   (if (= :restricted receipt)
                     (do (is (thrown? Exception (main/dispatch! ctx "reconcile" [id (str file)])))
                         (is (thrown? Exception (main/retry! ctx "a" (str file))))
                         (is (nil? (:merge-recovery (w/task ctx "a")))))
                     (do
                       (main/dispatch! ctx "reconcile" [id (str file)])
                       (main/retry! ctx "a" (str file))
                       (let [next-id (w/merge-effect-id (w/task ctx "a"))]
                         (is (not= id next-id))
                         (is (= receipt (get-in @(:state ctx) [:effects id :prior :status])))
                         (w/integrate! ctx (w/task ctx "a"))
                         (is (= :done (:status (w/task ctx "a"))))
                         (is (= :done (get-in @(:state ctx) [:effects next-id :status])))
                         (is (= 1 (count (filter #(= :merge (first %)) @events)))))))))))))

(deftest planning-contracts-and-frozen-writer-ownership
  (let [ctx (context) p (slice "parent") child (assoc (slice "child") :status :ready :dependencies ["parent"] :stack-on "parent")
        plan {:coverage ["bootstrap/"] :source-closure ["module.clj"] :target "declared target" :subset "required closure"
              :summary "Concrete work" :blockers [] :slices [p (dissoc child :status)]}]
    (try
      (is (= plan (validation/validate! schema/plan plan)))
      (is (= plan (w/validate-plan! plan {})))
      (let [standalone-plan (assoc plan :slices [(assoc p :key "standalone" :dependencies [] :stack-on "")])]
        (is (= standalone-plan (w/validate-plan! standalone-plan {}))))
      (is (thrown-with-msg? Exception #"Stack parent must be a declared dependency"
                            (w/validate-plan! (assoc plan :slices [p (assoc (dissoc child :status) :stack-on "Main branch")]) {})))
      (is (thrown-with-msg? Exception #"Unknown slice dependency"
                            (w/validate-plan! (assoc plan :slices [(assoc p :dependencies ["missing"])]) {})))
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

(deftest invalid-planning-is-recorded-without-admission-and-inspected-retry-gets-feedback
  (let [ctx (repository! (context)) repo (get-in ctx [:config :repo-dir]) base (rt/git ctx repo "rev-parse" "HEAD")
        p (slice "parent") child (assoc (slice "child") :dependencies ["parent"] :stack-on "Main branch")
        invalid {:coverage ["bootstrap/"] :source-closure ["module.clj"] :target "declared target"
                 :subset "required closure" :summary "Concrete work" :blockers []
                 :slices [p child]}
        valid (assoc invalid :slices [p (assoc child :stack-on "parent")])
        prompts (atom []) admitted (atom 0) inspection (io/file (:root ctx) "planning-inspection.edn")]
    (try
      (with-redefs [w/fresh-main! (constantly base)
                    w/worktree! (fn [& _] {:worktree repo :branch "codex/plan"})
                    gh/pages (fn [& _] [])
                    gh/comment! (fn [& _])
                    w/campaign-issue! (fn [& _] {})
                    w/admit! (fn [& _] (swap! admitted inc))
                    w/job! (fn [_ _ _ prompt _] (swap! prompts conj prompt) invalid)]
        (is (thrown-with-msg? Exception #"Stack parent must be a declared dependency" (w/plan! ctx)))
        (is (zero? @admitted))
        (is (= :done (get-in @(:state ctx) [:effects "plan/1" :status])))
        (is (= invalid (get-in @(:state ctx) [:effects "plan/1" :result])))
        (is (= :planning-validation-error (get-in @(:state ctx) [:planning :validation-error :kind])))
        (spit inspection (pr-str {:evidence "Original owner inspected the retained plan validation failure; no provider effect occurred."}))
        (main/retry! ctx "audit" (str inspection))
        (is (nil? (:planning @(:state ctx))))
        (is (= invalid (get-in @(:state ctx) [:effects "plan/1" :result])))
        (with-redefs [w/job! (fn [_ _ _ prompt _] (swap! prompts conj prompt) valid)]
          (w/plan! ctx))
        (is (= 2 (count @prompts)))
        (is (str/includes? (second @prompts) "Original owner inspected the retained plan validation failure"))
        (is (str/includes? (second @prompts) "Stack parent must be a declared dependency")))
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
        (is (= base (get-in @(:state ctx) [:completion :base])))
        (is (nil? (w/task ctx "completion")))
        (is (map? (get-in @(:state ctx) [:completion :review-evidence]))))
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

(deftest configuration-keeps-canonical-gates-and-separate-controller-locks
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
          (is (str/includes? (:lock-file saved) "/uniform-controller-"))
          (is (str/ends-with? (:main-lock-file saved) "/uniform-main.lock"))
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
        t (assoc (w/worktree! ctx "review-fixture" base) :kind "adoption" :paths ["module.clj"]
                 :base base :candidate base :tree (rt/git ctx repo "rev-parse" "HEAD^{tree}")
                 :checks {:candidate base :tree (rt/git ctx repo "rev-parse" "HEAD^{tree}")
                          :commands [["clojure" "-M" "focused"]] :result :passed})]
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

(deftest fresh-review-snapshot-binds-source-patch-checks-and-passive-prompt
  (let [ctx (repository! (context))
        worktree (io/file (get-in ctx [:config :worktree-root]) "review-snapshot")
        source (io/file worktree "module.clj")
        calls (atom [])
        t {:key "fresh-review" :kind "adoption" :base "base" :candidate "candidate"
           :tree "tree" :paths ["module.clj"] :worktree (str worktree)
           :checks {:candidate "candidate" :tree "tree" :commands [["clojure" "-M" "focused"]]
                    :result :passed}}]
    (try
      (.mkdirs worktree)
      (spit source "(ns fixture)\n(def seed :clojure)\n")
      (rt/update! ctx assoc-in [:tasks "fresh-review"] t)
      (with-redefs [w/assert-identity! (fn [& _] (swap! calls conj :identity))
                    w/assert-clean! (fn [& _] (swap! calls conj :clean))
                    rt/assert-authorized! (fn [& _] (swap! calls conj :authorized))
                    rt/command (fn [_ _ argv & _]
                                 (swap! calls conj [:snapshot argv])
                                 {:exit 0 :out "diff --git a/module.clj b/module.clj\n" :err ""})
                    w/job! (fn [_ _ _ prompt _ job]
                             (swap! calls conj [:review job prompt])
                             {:verdict "approved" :base "base" :candidate "candidate" :tree "tree"
                              :evidence-scope "adoption" :summary "evidence supplied"
                              :findings [] :residual-host-boundary "Fixture host remains"})]
        (is (= "approved" (:verdict (w/review! ctx t "adoption"))))
        (let [receipt (get-in @(:state ctx) [:tasks "fresh-review" :review-evidence])
              review-call (some #(when (and (vector? %) (= :review (first %))) %) @calls)
              prompt (nth review-call 2)]
          (is (= {:base "base" :candidate "candidate" :tree "tree"} (:tuple receipt)))
          (is (= "adoption" (:scope receipt)))
          (is (= (rt/digest "diff --git a/module.clj b/module.clj\n")
                 (:patch-sha256 receipt)))
          (is (= "module.clj" (get-in receipt [:source 0 :path])))
          (is (= (rt/digest "(ns fixture)\n(def seed :clojure)\n")
                 (get-in receipt [:source 0 :sha256])))
          (is (= (rt/digest prompt) (:prompt-sha256 receipt)))
          (is (some #(and (vector? %) (= :snapshot (first %))) @calls))
          (is (some #(and (vector? %) (= :review (first %))) @calls))
          (is (.contains ^String prompt "diff --git a/module.clj b/module.clj"))
          (is (.contains ^String prompt "(def seed :clojure)"))
          (is (.contains ^String prompt "Do not run Git, Clojure, tests, builds"))))
      (finally (delete-tree! (:root ctx))))))

(deftest review-snapshot-supports-owned-directories-and-deletions
  (let [ctx (repository! (context))
        repo (get-in ctx [:config :repo-dir])
        base (rt/git ctx repo "rev-parse" "HEAD")
        wt (w/worktree! ctx "review-directory-delete" base)]
    (try
      (let [worktree (:worktree wt)]
        (.mkdirs (io/file worktree "owned"))
        (spit (io/file worktree "owned" "new.clj") "(ns fixture.new)\n")
        (rt/git ctx worktree "rm" "module.clj")
        (rt/git ctx worktree "add" "owned/new.clj")
        (rt/git ctx worktree "commit" "-m" "directory and deletion fixture")
        (let [candidate (rt/git ctx worktree "rev-parse" "HEAD")
              tree (rt/git ctx worktree "rev-parse" "HEAD^{tree}")
              t (merge wt {:key "directory-delete" :kind "adoption"
                           :paths ["owned" "module.clj"] :base base
                           :candidate candidate :tree tree
                           :checks {:candidate candidate :tree tree
                                    :commands [["clojure" "-M" "focused"]]
                                    :result :passed}})]
          (rt/update! ctx assoc-in [:tasks "directory-delete"] t)
          (with-redefs [w/job! (fn [& _] {:verdict "approved" :base base
                                          :candidate candidate :tree tree
                                          :evidence-scope "adoption" :summary "Fixture review"
                                          :findings [] :residual-host-boundary "Fixture host remains"})]
            (is (= "approved" (:verdict (w/review! ctx t "adoption")))))
          (let [sources (get-in @(:state ctx) [:tasks "directory-delete" :review-evidence :source])]
            (is (= #{"module.clj" "owned/new.clj"} (set (map :path sources))))
            (is (= :deleted (:status (some #(when (= "module.clj" (:path %)) %) sources))))
            (is (= :added (:status (some #(when (= "owned/new.clj" (:path %)) %) sources)))))))
      (finally (delete-tree! (:root ctx))))))

(deftest review-snapshot-supports-owned-renames
  (let [ctx (repository! (context))
        repo (get-in ctx [:config :repo-dir])
        base (rt/git ctx repo "rev-parse" "HEAD")
        wt (w/worktree! ctx "review-rename" base)]
    (try
      (let [worktree (:worktree wt)]
        (.mkdirs (io/file worktree "owned"))
        (rt/git ctx worktree "mv" "module.clj" "owned/renamed.clj")
        (rt/git ctx worktree "commit" "-m" "rename fixture")
        (let [candidate (rt/git ctx worktree "rev-parse" "HEAD")
              tree (rt/git ctx worktree "rev-parse" "HEAD^{tree}")
              t (merge wt {:key "rename" :kind "adoption"
                           :paths ["module.clj" "owned"] :base base
                           :candidate candidate :tree tree
                           :checks {:candidate candidate :tree tree
                                    :commands [["clojure" "-M" "focused"]]
                                    :result :passed}})]
          (rt/update! ctx assoc-in [:tasks "rename"] t)
          (with-redefs [w/job! (fn [& _] {:verdict "approved" :base base
                                          :candidate candidate :tree tree
                                          :evidence-scope "adoption" :summary "Fixture review"
                                          :findings [] :residual-host-boundary "Fixture host remains"})]
            (is (= "approved" (:verdict (w/review! ctx t "adoption")))))
          (let [source (first (get-in @(:state ctx) [:tasks "rename" :review-evidence :source]))]
            (is (= :renamed (:status source)))
            (is (= "owned/renamed.clj" (:path source)))
            (is (= "module.clj" (:old-path source))))))
      (finally (delete-tree! (:root ctx))))))

(deftest tampered-review-evidence-is-rejected-before-a-review-turn
  (let [checks {:candidate "candidate" :tree "tree" :commands [["clojure" "-M" "focused"]]
                :result :passed}
        t {:key "evidence" :base "base" :candidate "candidate" :tree "tree"
           :paths ["module.clj"] :checks checks}
        source "(ns fixture)\n"
        evidence {:tuple {:base "base" :candidate "candidate" :tree "tree"}
                  :scope "adoption" :paths ["module.clj"] :patch "patch"
                  :patch-sha256 (rt/digest "patch")
                  :source [{:path "module.clj" :sha256 (rt/digest source)
                            :executable false :content source}]
                  :checks checks :checks-sha256 (rt/digest (pr-str checks))}]
    (is (= evidence (w/validate-review-evidence! t "adoption" evidence)))
    (is (thrown? clojure.lang.ExceptionInfo
                 (w/validate-review-evidence! t "adoption"
                                              (assoc-in evidence [:source 0 :content]
                                                        "(ns tampered)\n"))))
    (is (thrown? clojure.lang.ExceptionInfo
                 (w/validate-review-evidence! t "adoption"
                                              (assoc-in evidence [:tuple :candidate]
                                                        "other"))))))

(deftest restricted-candidate-refuses-before-review-snapshot
  (let [ctx (context)
        t {:key "restricted-review" :kind "adoption" :base "base" :candidate "candidate"
           :tree "tree" :paths ["module.clj"] :worktree "/tmp/never-read"
           :error {:details {:restricted true}}
           :checks {:candidate "candidate" :tree "tree" :commands [["clojure" "-M" "focused"]]
                    :result :passed}}
        called (atom [])]
    (rt/update! ctx assoc-in [:tasks "restricted-review"] t)
    (with-redefs [w/assert-identity! (fn [& _] (swap! called conj :identity))
                  rt/command (fn [& _] (swap! called conj :snapshot) {:exit 0 :out "" :err ""})
                  w/job! (fn [& _] (swap! called conj :review))]
      (let [failure (try (w/review! ctx t "adoption")
                         nil
                         (catch clojure.lang.ExceptionInfo e e))]
        (is (some? failure))
        (is (true? (:restricted (ex-data failure))))
        (is (empty? @called))))))
