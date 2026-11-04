(ns seedless.controller-invariants-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [seedless.github :as gh]
            [seedless.workflow :as workflow]
            [simplify.runtime :as rt]
            [simplify.workflow :as shared])
  (:import [java.nio.file Files]))

(defn context []
  (let [root (.toFile (Files/createTempDirectory "seedless-controller-"
                                                 (make-array java.nio.file.attribute.FileAttribute 0)))
        state-dir (io/file root "state")
        source-root (io/file root "source")
        worktree-root (io/file root "worktrees")]
    (.mkdirs state-dir)
    (.mkdirs source-root)
    (.mkdirs worktree-root)
    (merge (rt/coordination root {:state-dir (str state-dir)})
           {:root root
            :state-dir (str state-dir)
            :state-file (str (io/file state-dir "state.edn"))
            :control-file (str (io/file state-dir "control.edn"))
            :log-lock (Object.)
            :processes (atom #{})
            :state (atom {:schema :seedless/v1 :tasks {} :jobs {} :effects {}
                          :tracking-intents {}})
            :config {:repo-dir (str source-root)
                     :worktree-root (str worktree-root)
                     :repository "fixture/repo"
                     :remote "origin"
                     :base-branch "main"}})))

(defn task [key]
  {:key key :title "Replace fixture slice" :branch "codex/seedless-slice"
   :worktree "/tmp/seedless-candidate" :base "base" :candidate "candidate"
   :tree "tree" :issue {:number 17 :node_id "issue-17"}
   :proposal "Use the verified source" :paths ["module.clj"]
   :dependencies [] :stack-on "" :status :ci})

(defn ex-data-of [f]
  (try
    (f)
    nil
    (catch clojure.lang.ExceptionInfo e
      (ex-data e))))

(defn full-pr [t state sha]
  {:number 7
   :html_url "https://github.com/fixture/repo/pull/7"
   :node_id "pr-7"
   :state state
   :body (str "Implements #" (get-in t [:issue :number]) ".\n\n" (gh/marker (:key t)))
   :head {:ref (:branch t) :sha sha :repo {:full_name "fixture/repo"}}
   :base {:ref "main" :repo {:full_name "fixture/repo"}}})

(deftest seedless-identity-requires-contained-candidate
  (let [ctx (context)
        root (io/file (get-in ctx [:config :worktree-root]))
        inside (io/file root "candidate")
        outside (io/file (:root ctx) "outside")
        source-child (io/file (get-in ctx [:config :repo-dir]) "candidate")
        prefix-sibling (io/file (str (get-in ctx [:config :worktree-root]) "-sibling") "candidate")
        called (atom 0)]
    (.mkdirs inside)
    (.mkdirs outside)
    (.mkdirs source-child)
    (with-redefs [shared/assert-identity! (fn [_ _] (swap! called inc))]
      (workflow/assert-identity! ctx {:worktree (str inside)})
      (is (= 1 @called))
      (doseq [path [outside source-child prefix-sibling]]
        (let [data (ex-data-of #(workflow/assert-identity! ctx {:worktree (str path)}))]
          (is (= :root-containment (:seedless-identity data)))))
      (let [link (io/file root "linked")]
        (try
          (Files/createSymbolicLink (.toPath link) (.toPath outside)
                                    (make-array java.nio.file.attribute.FileAttribute 0))
          (let [data (ex-data-of #(workflow/assert-identity! ctx {:worktree (str link)}))]
            (is (= :root-containment (:seedless-identity data))))
          (catch UnsupportedOperationException _
            (is true "fixture filesystem does not support symbolic links")))))))

(deftest publish-refuses-foreign-or-closed-pr-before-push
  (doseq [remote-pr [(assoc (full-pr (task "slice") "open" "old")
                            :head {:ref "someone-elses-branch" :sha "old" :repo {:full_name "foreign/repo"}})
                     (assoc (full-pr (task "slice") "closed" "old")
                            :head {:ref "codex/seedless-slice" :sha "old" :repo {:full_name "fixture/repo"}})
                     (assoc (full-pr (task "slice") "open" "old") :body "unrelated PR")]]
    (let [ctx (context) t (task "slice") calls (atom [])]
      (rt/update! ctx assoc-in [:tasks "slice"] t)
      (with-redefs [workflow/assert-clean! (fn [& _])
                    workflow/assert-identity! (fn [& _])
                    gh/api (fn
                             [_ endpoint]
                             (swap! calls conj endpoint)
                             (if (str/includes? endpoint "pulls?state=all") [remote-pr] remote-pr))]
        (let [data (ex-data-of #(workflow/publish! ctx t))]
          (is (:remote-pr-refusal data))
          (is (empty? (get-in @(:state ctx) [:effects])))
          (is (not-any? #(str/starts-with? % "push/") @calls)))))))

(deftest publish-adopts-only-owned-existing-pr-after-head-check
  (let [ctx (context)
        ctx (assoc-in ctx [:config :poll-ms] 1)
        t (assoc (task "slice") :published-candidates ["old"])
        calls (atom 0) pushes (atom [])
        old-pr (full-pr t "open" "old") new-pr (full-pr t "open" "candidate")]
    (rt/update! ctx assoc-in [:tasks "slice"] t)
    (with-redefs [workflow/assert-clean! (fn [& _])
                  workflow/assert-identity! (fn [& _])
                  rt/git (fn [_ _ & args]
                           (when (= "push" (first args)) (swap! pushes conj args))
                           "")
                  gh/api (fn
                           [_ endpoint]
                           (let [n (swap! calls inc)
                                 pr (if (<= n 2) old-pr new-pr)]
                             (if (str/includes? endpoint "pulls?state=all") [pr] pr)))]
      (workflow/publish! ctx t)
      (is (= 1 (count @pushes)))
      (is (= 7 (get-in (workflow/task ctx "slice") [:pr :number])))
      (is (= :done (get-in @(:state ctx) [:effects "push/candidate" :status])))
      (is (nil? (get-in @(:state ctx) [:tasks "slice" :pr-effect]))))))

(deftest merged-pr-must-target-this-main-and-head
  (let [ctx (context) t (assoc (task "slice") :pr {:number 9})
        finished (atom false)]
    (rt/update! ctx assoc-in [:tasks "slice"] t)
    (with-redefs [shared/pr-info (fn [_ _] {:state "MERGED" :statusCheckRollup []})
                  workflow/assert-identity! (fn [& _])
                  workflow/finish-merge! (fn [& _] (reset! finished true))
                  gh/api (fn [_ _]
                           {:number 9 :state "closed"
                            :head {:ref (:branch t) :sha (:candidate t)
                                   :repo {:full_name "fixture/repo"}}
                            :base {:ref "wrong-main" :repo {:full_name "fixture/repo"}}})]
      (let [data (ex-data-of #(workflow/integrate! ctx t))]
        (is (:remote-pr-refusal data))
        (is (false? @finished))))))

(defn merge-command [_ _ & args]
  (case args
    ["rev-list" "--parents" "-n" "1" "merge"] "merge base candidate"
    ["rev-parse" "merge^{tree}"] "tree"
    ["merge-base" "--is-ancestor" "merge" "main"] ""
    ""))

(deftest merge-finalization-completes-after-tracking-close
  (let [ctx (context) t (assoc (task "slice") :status :merge :worktree "/tmp/seedless-candidate")
        transitions (atom [])]
    (rt/update! ctx assoc-in [:tasks "slice"] t)
    (with-redefs [workflow/fresh-main! (fn [_] "main")
                  workflow/assert-identity! (fn [& _])
                  rt/git merge-command
                  rt/event! (fn [& _])
                  workflow/transition! (fn [c k status]
                                         (swap! transitions conj status)
                                         (workflow/task! c k :status status))
                  gh/api (fn [_ _ _ _] {:state "closed"})]
      (workflow/finish-merge! ctx t {:mergeCommit {:oid "merge"}})
      (is (= [:finalizing :done] @transitions))
      (is (= :done (get-in @(:state ctx) [:tasks "slice" :status])))
      (is (= "merge" (get-in @(:state ctx) [:tasks "slice" :merged])))
      (is (= :done (get-in @(:state ctx) [:effects "close/slice" :status]))))))

(deftest failed-tracking-close-stays-in-finalization
  (let [ctx (context) t (assoc (task "slice") :status :merge :worktree "/tmp/seedless-candidate")
        transitions (atom [])]
    (rt/update! ctx assoc-in [:tasks "slice"] t)
    (with-redefs [workflow/fresh-main! (fn [_] "main")
                  workflow/assert-identity! (fn [& _])
                  rt/git merge-command
                  rt/event! (fn [& _])
                  workflow/transition! (fn [c k status]
                                         (swap! transitions conj status)
                                         (workflow/task! c k :status status))
                  rt/effect! (fn [c id _]
                               (rt/update! c assoc-in [:effects id] {:status :uncertain})
                               (throw (ex-info "tracker unavailable" {:effect id})))]
      (is (thrown? Exception (workflow/finish-merge! ctx t {:mergeCommit {:oid "merge"}})))
      (is (= [:finalizing] @transitions))
      (is (= :finalizing (get-in @(:state ctx) [:tasks "slice" :status])))
      (is (= "merge" (get-in @(:state ctx) [:tasks "slice" :merged])))
      (is (= :uncertain (get-in @(:state ctx) [:effects "close/slice" :status]))))))

(deftest a-failed-done-status-does-not-record-the-slice-done
  (let [ctx (context) t (assoc (task "slice") :status :merge)
        statuses (atom [])]
    (rt/update! ctx assoc-in [:tasks "slice"] t)
    (with-redefs [workflow/fresh-main! (constantly "main")
                  workflow/assert-identity! (fn [& _])
                  rt/git merge-command rt/event! (fn [& _])
                  gh/api (fn [& _] {:state "closed"})
                  gh/status! (fn [context _ status]
                               (swap! statuses conj status)
                               (when (= "Done" status)
                                 (rt/effect! context "done-status"
                                             #(throw (ex-info "lost final status response" {})))))]
      (is (thrown? Exception (workflow/finish-merge! ctx t {:mergeCommit {:oid "merge"}})))
      (is (= ["Merge queue" "Done"] @statuses))
      (is (= :finalizing (get-in @(:state ctx) [:tasks "slice" :status])))
      (is (= :done (get-in @(:state ctx) [:effects "close/slice" :status])))
      (is (= :uncertain (get-in @(:state ctx) [:effects "done-status" :status]))))))

(deftest attention-keeps-effect-failures-local
  (doseq [effect-status [:pending :uncertain :restricted :unexpected]]
    (let [ctx (context) t (assoc (task "slice") :status :finalizing
                                 :finalization-effect "close/slice")
          events (atom [])]
      (rt/update! ctx assoc-in [:tasks "slice"] t)
      (rt/update! ctx assoc-in [:effects "close/slice"] {:status effect-status})
      (with-redefs [rt/event! (fn [_ kind data] (swap! events conj [kind data]))
                    gh/status! (fn [& _] (throw (ex-info "status write forbidden" {})))
                    gh/comment! (fn [& _] (throw (ex-info "comment write forbidden" {})))
                    workflow/transition! (fn [& _] (throw (ex-info "transition write forbidden" {})))]
        (workflow/attention! ctx "slice" (ex-info "close failed" {:effect "close/slice"}))
        (is (= :attention (get-in @(:state ctx) [:tasks "slice" :status])))
        (is (= :finalizing (get-in @(:state ctx) [:tasks "slice" :resume-status])))
        (is (= (if (= :unexpected effect-status) :unknown effect-status)
               (get-in @(:state ctx) [:tasks "slice" :error :details :effect-status])))
        (is (some #(= :attention (first %)) @events))))))

(deftest attention-keeps-pr-refusal-local
  (let [ctx (context) t (task "slice") events (atom [])]
    (rt/update! ctx assoc-in [:tasks "slice"] t)
    (with-redefs [rt/event! (fn [_ kind data] (swap! events conj [kind data]))
                  gh/status! (fn [& _] (throw (ex-info "status write forbidden" {})))
                  gh/comment! (fn [& _] (throw (ex-info "comment write forbidden" {})))
                  workflow/transition! (fn [& _] (throw (ex-info "transition write forbidden" {})))]
      (workflow/attention! ctx "slice" (ex-info "foreign PR" {:remote-pr-refusal true :reason :closed}))
      (is (= :attention (get-in @(:state ctx) [:tasks "slice" :status])))
      (is (= :closed (get-in @(:state ctx) [:tasks "slice" :error :details :reason])))
      (is (some #(= :attention (first %)) @events)))))

(deftest attention-does-not-inherit-unrelated-uncertain-effect
  (let [ctx (context) t (assoc (task "slice") :status :ci :publish-effect "push/old")
        statuses (atom []) comments (atom 0)]
    (rt/update! ctx assoc-in [:tasks "slice"] t)
    (rt/update! ctx assoc-in [:effects "push/old"] {:status :uncertain})
    (with-redefs [gh/status! (fn [_ _ status] (swap! statuses conj status))
                  gh/comment! (fn [& _] (swap! comments inc))
                  rt/event! (fn [& _])]
      (workflow/attention! ctx "slice" (ex-info "ordinary failure" {:reason :ordinary}))
      (is (= ["Attention"] @statuses))
      (is (= 1 @comments))
      (is (= :attention (get-in @(:state ctx) [:tasks "slice" :status])))
      (is (nil? (get-in @(:state ctx) [:tasks "slice" :error :details :effect-status]))))))

(deftest real-generic-effect-failure-does-not-trigger-reporting-writes
  (let [ctx (context) t (task "slice") calls (atom [])]
    (rt/update! ctx assoc-in [:tasks "slice"] t)
    (with-redefs [rt/event! (fn [& _])
                  gh/status! (fn [& _] (swap! calls conj :status))
                  gh/comment! (fn [& _] (swap! calls conj :comment))]
      (try
        (rt/effect! ctx "original-write" #(throw (ex-info "Response lost" {})))
        (catch Exception e (workflow/attention! ctx "slice" e)))
      (is (empty? @calls))
      (is (= :uncertain (get-in @(:state ctx) [:effects "original-write" :status])))
      (is (= "original-write" (get-in (workflow/task ctx "slice") [:error :details :effect])))
      (is (= :attention (:status (workflow/task ctx "slice")))))))

(deftest outside-worktrees-are-refused-before-checks-or-provider-observation
  (let [ctx (context) t (task "slice") calls (atom [])]
    (rt/update! ctx assoc-in [:tasks "slice"] t)
    (with-redefs [shared/checks! (fn [& _] (swap! calls conj :check))
                  shared/pr-info (fn [& _] (swap! calls conj :provider))
                  rt/git (fn [& _] (swap! calls conj :git))]
      (doseq [operation [#(workflow/check-candidate! ctx "slice" [] nil)
                         #(workflow/integrate! ctx t)
                         #(workflow/verify-merge! ctx t {:mergeCommit {:oid "merge"}})]]
        (is (= :root-containment (:seedless-identity (ex-data-of operation)))))
      (is (empty? @calls)))))
