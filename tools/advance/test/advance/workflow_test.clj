(ns advance.workflow-test
  "Real Git candidates and squash receipts with deterministic model/GitHub doubles."
  (:require [advance.catalog :as catalog]
            [advance.github :as project]
            [advance.main :as main]
            [advance.workflow :as workflow]
            [clojure.edn :as edn]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is use-fixtures]]
            [simplify.codex :as codex]
            [simplify.coordination :as coordination]
            [simplify.core-test :as fixture]
            [simplify.github :as gh]
            [simplify.main :as simplify-main]
            [simplify.runtime :as rt]
            [simplify.schema :as schema]
            [simplify.workflow :as flow]
            [simplify.workflow-test :as git-fixture]))

(use-fixtures :each fixture/no-github-network)

(def source-path "specs/demo/tasks.md")
(def source-key (str source-path "#demo/value"))

(deftest provider-context-is-lossless-without-repeating-navigation-metadata
  (let [ctx (assoc-in (fixture/context) [:config :repository] "fixture/repo")
        files [{:filename "renamed\nsource.clj" :previous_filename "old source.clj" :status "renamed"}]
        ref {:ref "topic" :sha "candidate" :label "fixture:topic"
             :repo {:id 17 :full_name "fixture/repo" :html_url "https://example.invalid/repo"
                    :fork false :default_branch "main" :description "Provider navigation"}
             :user {:id 8 :login "fixture" :html_url "https://example.invalid/user" :type "User" :avatar_url "unused"}}
        pulls [{:number 42 :title "Independent candidate" :body "Complete body and acceptance details"
                :draft true :html_url "https://example.invalid/pr/42" :head ref :base (assoc ref :ref "main")}
               {:number 43 :title "Deleted fork" :body nil :draft false :head nil :base nil}]
        requests (atom [])
        presented (with-redefs [gh/pages (fn [_ endpoint] (swap! requests conj endpoint) files)]
                    (project/candidate-files ctx pulls))
        reference (:raw-provider-context (first presented))
        raw (rt/read-edn (:path reference))]
    (is (= 2 (count @requests) (count presented)))
    (is (= (mapv #(assoc % :files files) pulls) raw))
    (is (= (:sha256 reference) (rt/digest (slurp (:path reference)))))
    (is (every? #(= reference (:raw-provider-context %)) presented))
    (is (= (mapv #(select-keys % [:number :title :body :draft :html_url :files]) raw)
           (mapv #(select-keys % [:number :title :body :draft :html_url :files]) presented)))
    (doseq [side [:head :base]]
      (is (= (select-keys (side (first raw)) [:ref :sha :label])
             (select-keys (side (first presented)) [:ref :sha :label])))
      (is (= (get-in raw [0 side :repo :id]) (get-in presented [0 side :repo :id])))
      (is (= (get-in raw [0 side :repo :full_name]) (get-in presented [0 side :repo :full_name])))
      (is (= (get-in raw [0 side :user :login]) (get-in presented [0 side :user :login]))))
    (is (nil? (:head (second presented))))
    (is (nil? (:base (second presented))))
    (is (= "Provider navigation" (get-in raw [0 :head :repo :description])))
    (is (nil? (get-in presented [0 :head :repo :description])))))

(deftest selector-reaches-the-api-with-complete-scope-and-external-message-sized-input
  (let [ctx (assoc-in (fixture/context) [:config :repository] "fixture/repo")
        provider-limit 1048576 ; Actual observed app-server message boundary, not a task allowance.
        large-navigation (apply str (repeat 600000 "n"))
        ref {:ref "topic" :sha "candidate" :label "fixture:topic"
             :repo {:id 1 :full_name "fixture/repo" :description large-navigation}}
        pull {:number 42 :title "Independent work" :body "Full candidate body\n{:untrusted true}" :draft true
              :head ref :base (assoc ref :ref "main")}
        catalog {:tasks [{:key source-key :path source-path :title "First"}
                         {:key "specs/other/tasks.md#other/task" :path "specs/other/tasks.md" :title "Second"}]}
        output {:summary "Both tasks remain current; no increment admitted by this fixture"
                :coverage [source-path "specs/other/tasks.md"] :increments [] :blockers []}
        messages (atom []) transport (atom []) thread-cwd (atom nil)]
    (is (> (count (pr-str pull)) provider-limit))
    ;; Exercise the real model-job request/parser/validator. Only transport is
    ;; synthetic; no server, model, Git, tests or network action is launched.
    (with-redefs [gh/pages (fn [& _] [{:filename "scope.clj" :previous_filename "old-scope.clj"
                                      :status "renamed"}])
                  coordination/claims (fn [_] [])
                  codex/open! (fn [c job _] {:ctx c :job job :active-items (atom {})})
                  codex/close! (fn [& _] nil)
                  codex/send! (fn [& _] nil)
                  codex/request! (fn [_ method params]
                                   (swap! transport conj [method params])
                                   (case method
                                     "initialize" {}
                                     "thread/start" (do
                                                      (reset! thread-cwd (:cwd params))
                                                      (is (= "read-only" (:sandbox params)))
                                                      {:thread {:id "fixture-thread"} :model "fixture-no-inference"})
                                     "turn/start" (let [text (get-in params [:input 0 :text])]
                                                    (is (= {:type "readOnly"} (:sandboxPolicy params)))
                                                    (swap! messages conj text)
                                                    (when (> (count text) provider-limit)
                                                      (throw (ex-info "Input exceeds the maximum length" {:input_error_code "input_too_large"})))
                                                    {:turn {:id "fixture-turn"}})))
                  codex/await-turn! (fn [& _] (json/write-str output))
                  gh/comment! (fn [& _] nil)
                  workflow/admit! (fn [_ _ all increments]
                                    (is (= catalog all)) (is (empty? increments)))]
      (is (= output (workflow/select! ctx {:base "base" :tree "tree" :worktree (:state-dir ctx) :catalog catalog} {} [pull]))))
    (is (= 1 (count @messages)))
    (is (str/includes? (first @messages) source-key))
    (is (str/includes? (first @messages) "specs/other/tasks.md#other/task"))
    (is (str/includes? (first @messages) "Independent work"))
    (is (str/includes? (first @messages) "scope.clj"))
    (is (str/includes? (first @messages) "old-scope.clj"))
    (is (str/includes? (first @messages) "renamed"))
    (is (str/includes? (first @messages) "candidate"))
    (is (not (str/includes? (first @messages) "Full candidate body"))
        "the complete body is not repeated in the bounded selector message")
    (is (str/includes? (first @messages) "raw-provider-context"))
    (is (= :completed (get-in @(:state ctx) [:jobs "select-1" :status])))
    (let [thread-start (some #(when (= "thread/start" (first %)) (second %)) @transport)
          turn-start (some #(when (= "turn/start" (first %)) (second %)) @transport)
          relative-path (re-find #"target/validation/advance/selection-history/[0-9a-f]+\.edn"
                                 (first @messages))
          snapshot-file (when (and @thread-cwd relative-path)
                          (io/file @thread-cwd relative-path))]
      (is (= (:state-dir ctx) (:cwd thread-start)))
      (is (= "read-only" (:sandbox thread-start)))
      (is (= {:type "readOnly"} (:sandboxPolicy turn-start)))
      (is (string? relative-path))
      (is (and snapshot-file (.isFile snapshot-file))
          "the read-only worker cwd contains its snapshot")
      (is (and snapshot-file (map? (rt/read-edn snapshot-file)))
          "the worker can read the exact snapshot")
      (let [candidate-snapshot
            (some (fn [path]
                   (let [file (io/file @thread-cwd path)
                         value (rt/read-edn file)]
                     (when (contains? value :candidates)
                       [file value])))
                  (distinct (re-seq #"target/validation/advance/selection-history/[0-9a-f]+\.edn"
                                    (first @messages))))]
        (is candidate-snapshot "the candidate snapshot is readable in the worker cwd")
        (let [[_ envelope] candidate-snapshot
              candidate (first (:candidates envelope))]
          (is (= "Full candidate body\n{:untrusted true}" (:body candidate)))
          (is (= [{:filename "scope.clj" :previous_filename "old-scope.clj" :status "renamed"}]
                 (:files candidate)))
          (is (= "candidate" (get-in candidate [:head :sha])))
          (is (= "main" (get-in candidate [:base :ref]))))))))

(deftest blocker-schema-uses-only-the-current-catalog
  (let [output {:summary "Environment notes stay here" :coverage [] :increments []
                :blockers [{:task source-key :reason "Needs remaining implementation"}]}
        contract (workflow/selection-output-schema {:tasks [{:key source-key} {:key "second-task"}]})]
    (is (= [source-key "second-task"] (get-in contract [:properties :blockers :items :properties :task :enum])))
    (is (= output (schema/validate! contract output)))
    (doseq [unknown ["local Git inspection" "removed-task"]]
      (is (thrown-with-msg? Exception #"Invalid structured output enum"
                           (schema/validate! contract (assoc-in output [:blockers 0 :task] unknown)))))
    (let [empty-contract (workflow/selection-output-schema {:tasks []})]
      (is (= 0 (get-in empty-contract [:properties :blockers :maxItems])))
      (is (nil? (get-in empty-contract [:properties :blockers :items :properties :task :enum])))
      (is (= (assoc output :blockers []) (schema/validate! empty-contract (assoc output :blockers [])))))))

(deftest selector-uses-supplied-identities-and-preserves-rejected-job-history
  (let [ctx (fixture/context)
        history [{:job "select-23" :status :rejected :output {:original "unchanged"}
                  :restriction "Git/cache writes were denied; no replay or executable transfer"}]
        pending {:job "select-23" :status :rejected :admitted false}
        output {:summary "Environment refusal preserved; implementation still needed"
                :coverage [source-path] :increments []
                :blockers [{:task source-key :reason "Broader acceptance remains missing"}]}
        calls (atom [])]
    (rt/update! ctx assoc :operator-planning history :pending-selection pending)
    (with-redefs [rt/command (fn [& _] (throw (AssertionError. "No identity inspection or command replay")))
                  coordination/claims (fn [_] [])
                  codex/job! (fn [_ _ _ mode instructions prompt contract]
                               (swap! calls conj :model)
                               (is (= :read mode))
                               (is (str/includes? prompt "Base: supplied-base\nTree: supplied-tree"))
                               (is (str/includes? prompt "select-23"))
                               (is (str/includes? prompt "Git/cache writes were denied"))
                               (is (str/includes? instructions "never replay, transfer to another tool or executable"))
                               (is (str/includes? instructions "Put environment and restriction notes in summary"))
                               (is (= [source-key] (get-in contract [:properties :blockers :items :properties :task :enum])))
                               output)
                  gh/comment! (fn [& _] (swap! calls conj :comment))
                  workflow/admit! (fn [_ _ _ increments] (is (empty? increments)) (swap! calls conj :admit))]
      (is (= output (workflow/select! ctx {:base "supplied-base" :tree "supplied-tree" :worktree (:state-dir ctx)
                                          :catalog {:tasks [{:key source-key}]}} {} []))))
    (is (= [:model :comment :admit] @calls))
    (is (= history (:operator-planning @(:state ctx))))
    (is (= pending (:pending-selection @(:state ctx))))))

(deftest invalid-selection-retains-output-without-provider-writes-or-retry
  (doseq [[catalog output message settings]
          [[{:tasks [{:key source-key}]}
            {:summary "Environment issue" :coverage [] :increments []
             :blockers [{:task "local Git inspection" :reason "Denied cache write"}]} #"Invalid structured output enum" {}]
           [{:tasks []}
            {:summary "No source tasks" :coverage [] :increments []
             :blockers [{:task source-key :reason "Not in empty catalog"}]} #"Blocker names an unknown source task" {}]
           [{:tasks [{:key source-key}]}
            {:summary "Attempt blocked task" :coverage []
             :increments [{:key "scoped" :task source-key :title "Scoped task" :evidence "Actual evidence"
                           :proposal "Implement scoped task" :preserve "Keep parent open" :paths [source-path] :dependencies []}]
             :blockers []} #"Selector chose an operator-paused or blocked task" {8 {"Control" "Paused"}}]]]
    (let [ctx (fixture/context) attempts (atom 0)
          previous {:base "previous-base" :output {:summary "Original valid selection"}}
          history [{:job "select-23" :status :rejected :output {:original "retained"}}]
          pending {:job "select-23" :admitted false :status :rejected}]
      (rt/update! ctx assoc :selection previous :operator-planning history :pending-selection pending
                  :source-tasks {source-key {:issue {:number 8}}})
      (with-redefs [coordination/claims (fn [_] [])
                    rt/command (fn [& _] (throw (AssertionError. "No command replay")))
                    codex/job! (fn [& _] (swap! attempts inc) output)
                    gh/comment! (fn [& _] (throw (AssertionError. "No provider write for invalid selection")))
                    workflow/admit! (fn [& _] (throw (AssertionError. "No invalid selection admission")))]
        (is (thrown-with-msg? Exception message
                             (workflow/select! ctx {:base "base" :tree "tree" :worktree (:state-dir ctx) :catalog catalog} settings []))))
      (is (= 1 @attempts))
      (is (= previous (:selection @(:state ctx))))
      (is (= history (:operator-planning @(:state ctx))))
      (is (= pending (:pending-selection @(:state ctx))))
      (let [rejection (last (:selection-rejections @(:state ctx)))]
        (is (= output (:output rejection)))
        (is (= {:base "base" :tree "tree" :admitted false} (select-keys rejection [:base :tree :admitted])))
        (is (re-find message (:message rejection)))))))

(deftest production-job-validation-failure-retains-an-immutable-response-reference
  (let [original (fixture/context)
        ctx (assoc original :log-file (str (io/file (:state-dir original) "events.jsonl")))
        response {:summary "Environment issue" :coverage [] :increments []
                  :blockers [{:task "local Git inspection" :reason "Denied cache write"}]}
        attempts (atom 0)
        previous {:base "previous" :output {:summary "Previously accepted selection"}}
        history [{:job "select-23" :status :rejected :restriction "No denied Git/cache write replay"
                  :output {:original "retained"}}]
        pending {:job "select-23" :admitted false :status :rejected}]
    (rt/update! ctx assoc :selection previous :operator-planning history :pending-selection pending)
    ;; Keep the real job! parser, validator and failure recording. Only its
    ;; transport is a deterministic response fixture; no model or tools run.
    (with-redefs [coordination/claims (fn [_] [])
                  rt/command (fn [& _] (throw (AssertionError. "No command replay")))
                  codex/open! (fn [c job _] {:ctx c :job job})
                  codex/close! (fn [& _] nil)
                  codex/send! (fn [& _] nil)
                  codex/request! (fn [_ method _]
                                   (case method
                                     "initialize" {}
                                     "thread/start" {:thread {:id "fixture-thread"} :model "fixture-no-inference"}
                                     "turn/start" {:turn {:id "fixture-turn"}}))
                  codex/await-turn! (fn [client _]
                                      (swap! attempts inc)
                                      (let [raw (json/write-str response)]
                                        (rt/event! (:ctx client) :rpc-receive
                                                   {:job (:job client) :message {:method "item/completed"
                                                                               :params {:item {:type "agentMessage" :text raw}}}})
                                        raw))
                  gh/comment! (fn [& _] (throw (AssertionError. "No provider write")))
                  workflow/admit! (fn [& _] (throw (AssertionError. "No admission")))]
      (is (thrown-with-msg? Exception #"Invalid structured output enum"
                           (workflow/select! ctx {:base "base" :tree "tree" :worktree (:state-dir ctx)
                                                  :catalog {:tasks [{:key source-key}]}} {} []))))
    (is (= 1 @attempts))
    (is (= previous (:selection @(:state ctx))))
    (is (= history (:operator-planning @(:state ctx))))
    (is (= pending (:pending-selection @(:state ctx))))
    (let [rejection (last (:selection-rejections @(:state ctx)))
          reference (:response-reference rejection)
          job (get-in @(:state ctx) [:jobs (:job rejection)])]
      (is (= {:base "base" :tree "tree" :admitted false :output nil}
             (select-keys rejection [:base :tree :admitted :output])))
      (is (= :attention (:status job)))
      (is (re-find #"Invalid structured output enum" (:message rejection)))
      (is (= {:thread "fixture-thread" :turn "fixture-turn"} (:identity reference)))
      (is (= (:state-file ctx) (:state-file reference)))
      (is (= (:job rejection) (:job reference)))
      (is (= response (-> (slurp (:log-file reference)) str/split-lines first
                                          (json/read-str :key-fn keyword)
                                          (get-in [:message :params :item :text])
                                          (json/read-str :key-fn keyword)))))
    (is (= (:selection-rejections @(:state ctx))
           (:selection-rejections (rt/read-edn (:state-file ctx)))))))

(deftest snapshot-supplies-its-observed-tree-without-an-extra-read
  (let [ctx (fixture/context) reads (atom []) snapshot {:tasks [] :files []}
        base "0123456789abcdef0123456789abcdef01234567"]
    (with-redefs [flow/fresh-main! (fn [_] base)
                  flow/worktree! (fn [& _] {:worktree "/fixture"})
                  catalog/discover (fn [_] snapshot)
                  workflow/mirror! (fn [& _] nil)
                  rt/git (fn [_ cwd & args] (swap! reads conj [cwd (vec args)]) "tree")]
      (is (= {:base base :tree "tree" :worktree "/fixture" :catalog snapshot} (workflow/snapshot! ctx))))
    (is (= [["/fixture" ["rev-parse" "HEAD^{tree}"]]] @reads))
    (is (= "tree" (get-in @(:state ctx) [:catalog :tree])))))

(deftest selection-history-is-compact-but-lossless-and-content-addressed
  (let [ctx (fixture/context)
        worktree (:state-dir ctx)
        detail (apply str (repeat 4096 "retained restricted and uncertain history "))
        tasks [{:key "increment/b"
                :task "specs/demo/tasks.md#demo/b"
                :title "Blocked increment"
                :paths ["src/b.clj"]
                :dependencies ["increment/a"]
                :status :attention
                :base "base-b" :candidate "candidate-b" :tree "tree-b" :merged "merged-b"
                :priority "Stale-low" :control "Stale-blocked"
                :issue {:number 102}
                :error {:details {:effect ["effect/uncertain" "effect/missing" "effect/pending"] :message detail}}
                :review {:findings [detail]}
                :checks [{:command ["clojure" "-M:test"] :result :unknown}]}
               {:key "increment/a"
                :task "specs/demo/tasks.md#demo/a"
                :title "Held increment"
                :paths ["src/a.clj"]
                :dependencies []
                :status :active
                :base "base-a" :candidate "candidate-a" :tree "tree-a" :merged nil
                :priority "Stale-high" :control "Stale-paused" :project-control "Stale-project-control"
                :issue {:number 101}
                :error {:details {:effect "effect/restricted" :message detail}}
                :review {:verdict "changes_requested" :details detail}
                :checks [{:command ["clojure" "-M:test"] :error detail}]}]
        before @(:state ctx)
        effects {"effect/restricted" {:status :restricted :result nil}
                 "effect/uncertain" {:status :uncertain :result nil}
                 "effect/pending" {:status :pending :result nil}}
        settings {101 {"Priority" "Highest" "Control" "Paused"}
                  102 {"Priority" "Low" "Control" "Active"}}
        first-result (workflow/selection-history! worktree "selection-base" "selection-tree" tasks
                                                  {:effects effects :settings settings})
        second-result (workflow/selection-history! worktree "selection-base" "selection-tree" tasks
                                                   {:effects effects :settings settings})
        snapshot (:snapshot first-result)
        snapshot-file (io/file worktree (:path snapshot))
        envelope (rt/read-edn snapshot-file)
        sorted-tasks (vec (sort-by :key tasks))
        index (:index first-result)
        by-key (into {} (map (juxt :key identity) index))]
    (is (= first-result second-result) "same input has a stable index and snapshot identity")
    (is (= before @(:state ctx)) "history persistence does not mutate controller state")
    (is (.isFile snapshot-file))
    (is (= (:digest snapshot) (rt/digest (slurp snapshot-file))))
    (is (= {:base "selection-base" :tree "selection-tree"}
           (select-keys snapshot [:base :tree])))
    (is (= sorted-tasks (:tasks envelope)) "the full, unabridged records remain available")
    (is (= effects (:effects envelope)) "referenced effect status remains in the snapshot")
    (is (= settings (:settings envelope)) "operator controls remain keyed by issue identity")
    (is (= #{"increment/a" "increment/b"} (set (keys by-key))))
    (is (= ["increment/a" "increment/b"] (mapv :key index)) "index order is deterministic")
    (is (= {"Priority" "Highest" "Control" "Paused"}
           (:operator-control (by-key "increment/a"))))
    (is (= {"Priority" "Low" "Control" "Active"}
           (:operator-control (by-key "increment/b"))))
    (is (= {:restricted true :uncertain false :attention false}
           (select-keys (:history (by-key "increment/a")) [:restricted :uncertain :attention])))
    (is (= {:restricted false :uncertain true :attention true}
           (select-keys (:history (by-key "increment/b")) [:restricted :uncertain :attention])))
    (is (= :restricted (get-in (by-key "increment/a") [:history :effect-status "effect/restricted"])))
    (is (= :pending (get-in (by-key "increment/b") [:history :effect-status "effect/pending"])))
    (is (= #{"effect/uncertain" "effect/missing" "effect/pending"}
           (set (get-in (by-key "increment/b") [:history :effect-ids])))
        "effect references remain visible even when a referenced state record is absent")
    (doseq [task tasks
            :let [entry (by-key (:key task))]]
      (is (= (:key task) (get-in entry [:record :key])))
      (is (= (rt/digest (pr-str task)) (get-in entry [:record :digest])))
      (is (= (:path snapshot) (get-in entry [:record :path])))
      (is (pos-int? (get-in entry [:record :line])))
      (is (= task (edn/read-string (nth (str/split-lines (slurp snapshot-file))
                                       (dec (get-in entry [:record :line])))))
          "each index record line resolves to the original task map")
      (is (= (:candidate task) (:candidate entry)))
      (is (= (:tree task) (:tree entry)))
      (is (= (:dependencies task) (:dependencies entry))))
    (let [index-text (pr-str index)
          full-text (pr-str sorted-tasks)]
      (is (< (count index-text) (count full-text)))
      (is (not (str/includes? index-text detail))
          "long review/error/check payloads stay in the exact snapshot"))))

(deftest selection-history-persistence-keeps-an-ignored-git-worktree-clean
  (let [ctx (fixture/context)
        {:keys [repo]} (git-fixture/repository! ctx)
        gitignore (io/file repo ".gitignore")
        _ (spit gitignore "target/validation/\n")
        _ (rt/git ctx repo "add" ".gitignore")
        _ (rt/git ctx repo "commit" "-m" "Ignore development artifacts")
        base (rt/git ctx repo "rev-parse" "HEAD")
        tree (rt/git ctx repo "rev-parse" "HEAD^{tree}")
        tasks [{:key "increment/a" :task "specs/demo/tasks.md#demo/a"
                :title "One increment" :paths ["src/a.clj"] :dependencies []
                :status :active :base base :candidate "candidate" :tree tree}]
        result (workflow/selection-history! repo base tree tasks)
        snapshot-file (io/file repo (get-in result [:snapshot :path]))
        ignored (rt/command ctx repo ["git" "check-ignore" "-q" (get-in result [:snapshot :path])])]
    (is (.isFile snapshot-file))
    (is (zero? (:exit ignored)) "the configured target rule ignores the snapshot")
    (is (= "" (rt/git ctx repo "status" "--porcelain"))
        "snapshot persistence does not dirty the disposable worktree")
    (is (= tree (rt/git ctx repo "rev-parse" "HEAD^{tree}"))
        "snapshot persistence does not change the source tree")))

(deftest candidate-history-keeps-scope-inline-and-bodies-lossless
  (let [ctx (fixture/context)
        worktree (:state-dir ctx)
        head {:ref "feature/renamed" :sha "head-sha" :label "fixture:feature/renamed"
              :repo {:id 17 :full_name "fixture/repo" :html_url "https://example.invalid/repo"
                     :default_branch "main" :fork false}
              :user {:id 8 :login "fixture" :html_url "https://example.invalid/user" :type "User"}}
        base {:ref "main" :sha "base-sha" :label "fixture:main"
              :repo {:id 17 :full_name "fixture/repo" :html_url "https://example.invalid/repo"
                     :default_branch "main" :fork false}
              :user {:id 8 :login "fixture" :html_url "https://example.invalid/user" :type "User"}}
        files [{:filename "src/renamed\nvalue.clj" :previous_filename "src/old value.clj" :status "renamed"}
               {:filename "src/deleted.clj" :previous_filename nil :status "removed"}]
        candidates [{:number 42 :title "Nil body candidate" :html_url "https://example.invalid/pr/42"
                     :draft true :head head :base base :files files :body nil}
                    {:number 43 :title "Empty body candidate" :html_url "https://example.invalid/pr/43"
                     :draft false :head (assoc head :ref "feature/empty" :sha "empty-sha")
                     :base base :files [] :body ""}
                    {:number 44 :title "Hostile body candidate" :html_url "https://example.invalid/pr/44"
                     :draft true :head (assoc head :ref "feature/hostile" :sha "hostile-sha")
                     :base base :files files
                     :body "]}\n{:not-a-record true}\n\u0000provider text"}
                    {:number 45 :title "Deleted fork" :html_url "https://example.invalid/pr/45"
                     :draft false :head nil :base nil :files []}]
        before @(:state ctx)
        first-result (workflow/candidate-history! worktree "candidate-base" "candidate-tree" candidates)
        second-result (workflow/candidate-history! worktree "candidate-base" "candidate-tree" candidates)
        snapshot (:snapshot first-result)
        snapshot-file (io/file worktree (:path snapshot))
        envelope (rt/read-edn snapshot-file)
        index (:index first-result)
        by-number (into {} (map (juxt :number identity) index))
        snapshot-root (rt/canonical (io/file worktree "target/validation/advance/selection-history"))
        snapshot-canonical (rt/canonical snapshot-file)]
    (is (= first-result second-result) "same candidates have stable snapshot and pointers")
    (is (= before @(:state ctx)) "candidate snapshot persistence does not mutate controller state")
    (is (.isFile snapshot-file))
    (is (= (:digest snapshot) (rt/digest (slurp snapshot-file))))
    (is (str/starts-with? snapshot-canonical (str snapshot-root java.io.File/separator))
        "the candidate snapshot stays inside the selector worktree")
    (is (= {:base "candidate-base" :tree "candidate-tree"}
           (select-keys snapshot [:base :tree])))
    (is (= candidates (:candidates envelope)) "full candidate records remain unabridged")
    (is (str/includes? (pr-str index) "Hostile body candidate")
        "candidate titles remain inline")
    (is (not (str/includes? (pr-str index) "not-a-record"))
        "body values are not repeated in the selector index")
    (doseq [candidate candidates
            :let [entry (by-number (:number candidate))
                  pointer (:body entry)]]
      (is (= (dissoc candidate :body) (dissoc entry :body))
          "identity, title, URL, draft, refs and every changed path stay inline")
      (if (contains? candidate :body)
        (let [line (:line pointer)
              resolved (edn/read-string (nth (str/split-lines (slurp snapshot-file))
                                            (dec line)))]
          (is (= #{:path :line :key :digest} (set (keys pointer))))
          (is (= (:number candidate) (:key pointer)))
          (is (= (:path snapshot) (:path pointer)))
          (is (= (rt/digest (pr-str candidate)) (:digest pointer)))
          (is (= candidate resolved) "the pointer resolves to the exact full record")
          (is (= (:body candidate) (:body resolved)) "nil, empty and hostile bodies stay exact"))
        (is (not (contains? entry :body)) "a provider record without a body stays bodyless")))))

(deftest candidate-history-persistence-keeps-an-ignored-git-worktree-clean
  (let [ctx (fixture/context)
        {:keys [repo]} (git-fixture/repository! ctx)
        gitignore (io/file repo ".gitignore")
        _ (spit gitignore "target/validation/\n")
        _ (rt/git ctx repo "add" ".gitignore")
        _ (rt/git ctx repo "commit" "-m" "Ignore development artifacts")
        base (rt/git ctx repo "rev-parse" "HEAD")
        tree (rt/git ctx repo "rev-parse" "HEAD^{tree}")
        candidates [{:number 73 :title "Candidate body" :html_url "https://example.invalid/pr/73"
                     :draft false :head {:ref "topic" :sha "candidate"}
                     :base {:ref "main" :sha base}
                     :files [{:filename "src/value.clj" :previous_filename "src/old.clj" :status "renamed"}]
                     :body "exact body"}]
        result (workflow/candidate-history! repo base tree candidates)
        snapshot-file (io/file repo (get-in result [:snapshot :path]))
        ignored (rt/command ctx repo ["git" "check-ignore" "-q" (get-in result [:snapshot :path])])]
    (is (.isFile snapshot-file))
    (is (zero? (:exit ignored)) "the canonical target rule ignores candidate snapshots")
    (is (= "" (rt/git ctx repo "status" "--porcelain"))
        "candidate snapshot persistence does not dirty the disposable worktree")
    (is (= tree (rt/git ctx repo "rev-parse" "HEAD^{tree}"))
        "candidate snapshot persistence does not change the source tree")))

(deftest corrupt-or-missing-candidate-history-stops-before-model-dispatch
  (doseq [mutation [:corrupt :missing]]
    (let [ctx (fixture/context)
          worktree (:state-dir ctx)
          candidates [{:number 88 :title "Candidate" :html_url "https://example.invalid/pr/88"
                       :draft false :head {:ref "topic" :sha "candidate"}
                       :base {:ref "main" :sha "base"}
                       :files [{:filename "src/value.clj" :previous_filename nil :status "modified"}]
                       :body "complete body"}]
          real-candidate-history workflow/candidate-history!
          established (real-candidate-history worktree "base" "tree" candidates)
          snapshot-file (io/file worktree (get-in established [:snapshot :path]))
          dispatches (atom 0)
          tampered (atom false)]
      ;; The first call is the real persistence path; mutate that real file
      ;; only after helper verification and before model dispatch.
      (with-redefs [project/candidate-files (fn [_ _] candidates)
                    workflow/candidate-history!
                    (fn [path base tree records]
                      (let [result (real-candidate-history path base tree records)]
                        (when-not @tampered
                          (reset! tampered true)
                          (if (= mutation :missing)
                            (io/delete-file snapshot-file)
                            (spit snapshot-file "{:base \"tampered\"}\n")))
                        result))
                    coordination/claims (fn [_] [])
                    codex/job! (fn [& _]
                                 (swap! dispatches inc)
                                 {:summary "must not dispatch" :coverage [] :increments [] :blockers []})
                    gh/comment! (fn [& _] (throw (AssertionError. "No provider write after bad candidate evidence")))
                    workflow/admit! (fn [& _] (throw (AssertionError. "No admission after bad candidate evidence")))]
        (is (thrown-with-msg? Exception #"candidate|snapshot|digest|missing|corrupt|contents"
                              (workflow/select! ctx {:base "base" :tree "tree" :worktree worktree
                                                     :catalog {:tasks []}} {} []))
            (str mutation " candidate evidence fails closed")))
      (is (zero? @dispatches) (str mutation " candidate evidence never reaches codex/job!")))))

(deftest selection-history-refuses-replacement-of-an-immutable-snapshot
  (let [ctx (fixture/context)
        worktree (:state-dir ctx)
        tasks [{:key "increment/a" :task "specs/demo/tasks.md#demo/a"
                :title "Held increment" :paths ["src/a.clj"] :dependencies []
                :status :active :base "base" :candidate "candidate" :tree "tree"
                :error {:status :restricted :message "retained refusal"}}]
        result (workflow/selection-history! worktree "base" "tree" tasks
                                            {:effects {"effect/restricted" {:status :restricted}}
                                             :settings {}})
        snapshot-file (io/file worktree (get-in result [:snapshot :path]))]
    (spit snapshot-file "{:base \"tampered\"}\n")
    (is (thrown-with-msg? Exception #"immutable|digest|contents"
                          (workflow/selection-history! worktree "base" "tree" tasks
                                                     {:effects {"effect/restricted" {:status :restricted}}
                                                      :settings {}})))))

(deftest corrupt-or-missing-selection-history-stops-before-model-dispatch
  (doseq [mutation [:corrupt :missing]]
    (let [ctx (fixture/context)
          worktree (:state-dir ctx)
          tasks [{:key "increment/a" :task "specs/demo/tasks.md#demo/a"
                  :title "Held increment" :paths ["src/a.clj"] :dependencies []
                  :status :active :issue {:number 101}
                  :base "base" :candidate "candidate" :tree "tree"
                  :error {:details {:effect "effect/restricted"}}}]
          effects {"effect/restricted" {:status :restricted :result nil}}
          settings {101 {"Priority" "Highest" "Control" "Paused"}}
          _ (rt/update! ctx assoc :tasks (into {} (map (juxt :key identity) tasks))
                        :effects effects)
          result (workflow/selection-history! worktree "base" "tree" tasks
                                              {:effects effects :settings settings})
          snapshot-file (io/file worktree (get-in result [:snapshot :path]))
          dispatches (atom 0)]
      ;; selection-history! in select! verifies this real snapshot first;
      ;; candidate-files then mutates it before model dispatch is possible.
      (with-redefs [project/candidate-files
                    (fn [_ _]
                      (if (= mutation :missing)
                        (io/delete-file snapshot-file)
                        (spit snapshot-file "{:base \"tampered\"}\n"))
                      [])
                    codex/job! (fn [& _] (swap! dispatches inc)
                                 {:summary "must not dispatch" :coverage [] :increments [] :blockers []})]
        (is (thrown-with-msg? Exception #"immutable|digest|contents|corrupt|missing|snapshot"
                              (workflow/select! ctx {:base "base" :tree "tree" :worktree worktree
                                                     :catalog {:tasks []}} settings []))
            (str mutation " task history fails closed")))
      (is (zero? @dispatches) (str mutation " task history fails before codex/job!")))))

(deftest incompatible-startup-is-refused-before-github-or-state-writes
  (let [ctx (fixture/context) before @(:state ctx) mutations (atom [])]
    (with-redefs [coordination/register! (fn [_] (throw (ex-info "Incompatible live legacy controller" {})))
                  project/setup! (fn [_] (swap! mutations conj :project))
                  workflow/ensure-campaign! (fn [_] (swap! mutations conj :campaign))
                  coordination/unregister! (fn [_] (swap! mutations conj :unregister))]
      (is (thrown-with-msg? Exception #"Incompatible live legacy" (workflow/campaign! ctx)))
      (is (empty? @mutations))
      (is (= before @(:state ctx))))))

(deftest setup-failure-releases-only-admitted-controller-registration
  (let [ctx (fixture/context) sequence (atom [])]
    (with-redefs [coordination/register! (fn [_] (swap! sequence conj :register))
                  project/setup! (fn [_] (swap! sequence conj :setup) (throw (ex-info "Setup failure" {})))
                  workflow/ensure-campaign! (fn [_] (swap! sequence conj :campaign))
                  coordination/unregister! (fn [_] (swap! sequence conj :unregister))]
      (is (thrown-with-msg? Exception #"Setup failure" (workflow/campaign! ctx)))
      (is (= [:register :setup :unregister] @sequence)))))

(deftest retained-inflight-claims-stop-before-all-provider-setup
  (doseq [status [:implementing :review] command [:setup :campaign :simplify-setup]]
    (let [ctx (fixture/context) writes (atom [])
          key "retained" identity [(:state-file ctx) key]
          legacy (io/file (:coordination-dir ctx) "claims.edn")
          original {:identity identity :state-file (:state-file ctx) :key key :paths ["src/owned.clj"]}]
      (.mkdirs (.getParentFile legacy))
      (rt/write-edn! legacy {:claims {identity original}})
      (rt/update! ctx assoc-in [:tasks key] {:key key :status status :paths (:paths original) :implementation-job "preserved-author"})
      (let [before @(:state ctx)]
        (with-redefs [project/setup! (fn [& _] (swap! writes conj :project))
                      workflow/ensure-campaign! (fn [& _] (swap! writes conj :campaign))
                      gh/setup! (fn [& _] (swap! writes conj :shared-project))
                      gh/status! (fn [& _] (swap! writes conj :status))
                      gh/api (fn [& _] (swap! writes conj :api))]
          (is (thrown-with-msg? Exception #"Retained worker requires inspected handover"
                               (case command
                                 :setup (main/dispatch! ctx "setup" [])
                                 :simplify-setup (simplify-main/dispatch! ctx "setup" [])
                                 :campaign (workflow/campaign! ctx)))))
        (is (empty? @writes))
        (is (= (dissoc before :tasks) (dissoc @(:state ctx) :tasks)))
        (is (= :attention (get-in @(:state ctx) [:tasks key :status])))
        (is (= status (get-in @(:state ctx) [:tasks key :resume-status])))
        (is (= (select-keys (get-in before [:tasks key]) [:status :resume-status :error :implementation-job])
               (last (get-in @(:state ctx) [:tasks key :handover-history])))))
      (let [shared (rt/read-edn (:coordination-file ctx)) lease (first (vals (:writers shared)))]
        (is (= :needs-inspection (:status lease)))
        (is (= original (:legacy-claim lease)))
        (is (empty? (:controllers shared)))))))

(deftest retained-worker-recovers-through-inspected-release-and-existing-retry
  (let [ctx (fixture/context) key "retained" original-error {:message "Original failure" :details {:fixture true}}
        original {:key key :status :implementing :paths ["src/owned.clj"] :implementation-job "preserved-author" :error original-error}
        lease (rt/claim-paths! ctx key (:paths original) :implementing)
        lease-file (str (io/file (:state-dir ctx) "lease.edn"))
        inspection-file (str (io/file (:state-dir ctx) "inspection.edn"))]
    (rt/update! ctx assoc-in [:tasks key] original)
    (is (thrown? Exception (rt/with-claimed-paths ctx lease #(throw (ex-info "Unknown writer termination" {})))))
    (with-redefs [project/setup! (fn [& _] (throw (AssertionError. "No provider setup before inspected recovery")))
                  gh/api (fn [& _] (throw (AssertionError. "No provider call during local recovery")))]
      (is (thrown-with-msg? Exception #"Retained worker requires inspected handover" (main/dispatch! ctx "setup" [])))
      (rt/write-edn! lease-file lease)
      (rt/write-edn! inspection-file {:evidence "Fixture original owner inspected scoped writer as ended; no denied action retried"})
      (simplify-main/dispatch! ctx "release-writer" [lease-file inspection-file])
      (simplify-main/dispatch! ctx "retry" [key]))
    (is (= :repair (get-in @(:state ctx) [:tasks key :status])))
    (is (= "preserved-author" (get-in @(:state ctx) [:tasks key :implementation-job])))
    (is (= original-error (get-in @(:state ctx) [:tasks key :error])))
    (is (= :implementing (get-in @(:state ctx) [:tasks key :handover-history 0 :status])))
    (is (empty? (:writers (rt/read-edn (:coordination-file ctx)))))))

(deftest own-unresolved-main-effect-stops-before-provider-setup
  (doseq [command [:setup :campaign :simplify-setup]]
    (let [ctx (fixture/context) writes (atom 0)
          original {:status :uncertain :message "Preserved lost provider response"}]
      (rt/update! ctx assoc-in [:effects "merge/retained"] original)
      (with-redefs [project/setup! (fn [& _] (swap! writes inc))
                    gh/setup! (fn [& _] (swap! writes inc))
                    workflow/ensure-campaign! (fn [& _] (swap! writes inc))]
        (is (thrown-with-msg? Exception #"Unresolved main effect requires inspected handover"
                             (case command
                               :setup (main/dispatch! ctx "setup" [])
                               :simplify-setup (simplify-main/dispatch! ctx "setup" [])
                               :campaign (workflow/campaign! ctx)))))
      (is (zero? @writes))
      (is (= original (get-in @(:state ctx) [:effects "merge/retained"])))
      (is (seq (:main-effects (rt/read-edn (:coordination-file ctx))))))))

(deftest scoped-attention-and-foreign-holds-do-not-block-independent-setup
  (let [ctx (fixture/context) other (fixture/context)
        other (merge other (rt/coordination (.getParent (io/file (:coordination-file ctx))) (:config other)))
        own (rt/claim-paths! ctx "held" ["src/held.clj"] :checking)
        foreign (rt/claim-paths! other "foreign" ["src/foreign.clj"] :checking)
        calls (atom [])]
    (rt/update! ctx assoc-in [:tasks "held"] {:key "held" :status :attention :paths ["src/held.clj"]})
    (doseq [[owner lease] [[ctx own] [other foreign]]]
      (is (thrown? Exception (rt/with-claimed-paths owner lease #(throw (ex-info "Preserved unknown cleanup" {}))))))
    (with-redefs [project/setup! (fn [& _] (swap! calls conj :project))
                  workflow/ensure-campaign! (fn [& _] (swap! calls conj :campaign))]
      (main/dispatch! ctx "setup" []))
    (is (= [:project :campaign] @calls))
    (let [shared (rt/read-edn (:coordination-file ctx))]
      (is (= 2 (count (:writers shared))))
      (is (every? #(= :needs-inspection (:status %)) (vals (:writers shared))))
      (is (empty? (:controllers shared))))))

(deftest selection-sees-other-open-candidates-without-claiming-their-work
  (let [ctx (fixture/context)
        pull {:number 914 :title "Independent candidate" :head {:sha "other-head"}}
        prompt (atom nil)
        reads (atom [])]
    (with-redefs [gh/pages (fn [_ endpoint]
                            (swap! reads conj endpoint)
                            (cond
                              (str/includes? endpoint "pulls?state=open") [pull]
                              (str/ends-with? endpoint "pulls/914/files") [{:filename "native/value.uni" :status "modified"}]
                              :else (throw (ex-info "Unexpected read" {:endpoint endpoint}))))
                  codex/job! (fn [_ _ _ _ _ input _]
                               (reset! prompt input)
                               {:summary "Waiting for independent candidate" :coverage [] :increments [] :blockers []})
                  gh/comment! (fn [& _] nil)
                  coordination/claims (fn [_] [])]
      (workflow/select! ctx {:base "base" :worktree (:state-dir ctx) :catalog {:tasks []}} {} (project/open-prs ctx))
      (is (str/includes? @prompt "native/value.uni"))
      (is (str/includes? @prompt "other-head"))
      (is (str/includes? @prompt "do not adopt, close, merge or claim their acceptance"))
      (is (= 2 (count @reads)))
      (is (empty? (:tasks @(:state ctx)))))))
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
        config (merge main/defaults {:repo-dir repo :state-dir (:state-dir ctx) :repository "fixture/repo" :parallelism 1 :poll-ms 1
                                     :worktree-root (str (io/file (:state-dir ctx) "workers"))
                                     :checks [["clojure" "-M" "fixture-check.clj"]]})
        ctx (merge (assoc ctx :config config :before-implement workflow/validate-source!)
                   (rt/coordination (str (io/file repo ".git")) config))
        pr (atom nil) sequence (atom []) selections (atom 0) implementations (atom 0) checks (atom 0) readies (atom 0)
        issues (atom {}) closed (atom []) original-command rt/command
        response (fn [value] {:exit 0 :out (json/write-str value) :err ""})]
    (with-redefs
      [project/setup! (fn [_] nil)
       project/open-prs (fn [_] [])
       project/settings (fn [_] {})
       gh/review-threads-clear? (fn [& _] true)
       gh/head-workflow-state (fn [& _] {:state :passed :runs []})
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
                       (response (merge {:baseRefName "main" :reviewDecision "APPROVED" :mergeable "MERGEABLE" :mergeStateStatus "CLEAN"
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
                            (throw (ex-info "Sandboxed check failed" {:command argv :result {:exitCode 1} :writer-ended? true}))))
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
                  gh/head-workflow-state (fn [& _] {:state :passed :runs []})
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

(deftest large-current-math-task-mirrors-by-immutable-reference-without-input-loss
  (let [root (.getCanonicalPath (io/file "../.."))
        math (first (filter #(= "specs/domains-workflow/tasks.md#math" (:key %)) (:tasks (catalog/discover root))))
        ctx (assoc-in (fixture/context) [:config :repository] "fixture/repo")
        bodies (atom []) comments (atom [])]
    (is (> (count (:body math)) 65536) "The actual source reproduces GitHub's rejected payload")
    (with-redefs [gh/pages (fn [& _] [])
                  gh/api (fn [_ _ _ body]
                           (is (< (count (:body body)) 65536) "A GitHub representation must fit its actual host format")
                           (swap! bodies conj (:body body))
                           (assoc body :id 1 :number 1 :node_id "math"))
                  gh/status! (fn [& _] nil)
                  gh/comment! (fn [_ _ _ body] (swap! comments conj body))]
      (workflow/mirror! ctx {:tasks [math]} "source-base")
      (is (= (:body math) (get-in @(:state ctx) [:source-tasks (:key math) :body])))
      (workflow/mirror! ctx {:tasks [(assoc math :source-digest "new-digest")]} "new-base"))
    (is (= 1 (count @bodies)))
    (is (str/includes? (first @bodies) (str "/blob/source-base/" (:path math) "#L" (:line math))))
    (is (str/includes? (first @comments) "/blob/new-base/"))
    (is (not (str/includes? (first @bodies) (:body math))))))

(deftest source-sync-reuses-one-issue-inventory-per-pass
  (let [ctx (assoc-in (fixture/context) [:config :repository] "fixture/repo")
        tasks [{:key "one" :title "One" :path "specs/test/tasks.md" :line 1 :source-digest "digest"}
               {:key "two" :title "Two" :path "specs/test/tasks.md" :line 2 :source-digest "digest"}]
        scans (atom 0) created (atom 0)]
    (with-redefs [gh/pages (fn [& _] (swap! scans inc) [])
                  gh/api (fn [_ _ _ body] (let [n (swap! created inc)] (assoc body :id n :number n :node_id (str n))))
                  gh/status! (fn [& _] nil)]
      (workflow/mirror! ctx {:tasks tasks} "base"))
    (is (= 1 @scans))
    (is (= 2 @created))
    (is (= 2 (count (:source-tasks @(:state ctx)))))))
