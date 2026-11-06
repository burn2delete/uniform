(ns advance.workflow-test
  "Real Git candidates and squash receipts with deterministic model/GitHub doubles."
  (:require [advance.catalog :as catalog]
            [advance.github :as project]
            [advance.main :as main]
            [advance.workflow :as workflow]
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
      (is (= output (workflow/select! ctx {:base "supplied-base" :tree "supplied-tree" :worktree "/fixture"
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
                             (workflow/select! ctx {:base "base" :tree "tree" :worktree "/fixture" :catalog catalog} settings []))))
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
      (workflow/select! ctx {:base "base" :worktree "/fixture" :catalog {:tasks []}} {} (project/open-prs ctx))
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

(def authority-text
  (str "# Tasks\n<!-- demo-implementation-tasks:start -->\n"
       "- [ ] `demo/value` Complete value behavior\nDone when: the value is independently accepted.\n"
       "- [ ] `demo/other` Complete other behavior\nDone when: other behavior is accepted.\n"
       "<!-- demo-implementation-tasks:end -->\n"))

(defn authority-fixture! []
  (let [ctx (fixture/context) {:keys [repo]} (git-fixture/repository! ctx)
        file (io/file repo source-path)
        _ (io/make-parents file)
        _ (spit file authority-text)
        _ (rt/git ctx repo "add" source-path)
        _ (rt/git ctx repo "commit" "-m" "Selected task authority")
        _ (rt/git ctx repo "push" "origin" "main")
        base (rt/git ctx repo "rev-parse" "HEAD")
        config (merge main/defaults {:repo-dir repo :repository "fixture/repo" :campaign "fixture"
                                     :state-dir (:state-dir ctx) :worktree-root (str (io/file (:state-dir ctx) "workers"))})
        ctx (merge (assoc ctx :config config) (rt/coordination (str (io/file repo ".git")) config))
        task {:key "selected" :task source-key :task-path source-path :task-digest (rt/digest authority-text)
              :source-base base :base base :paths [source-path "work.edn"] :dependencies [] :status :ready}]
    {:ctx ctx :repo repo :task task :base base}))

(defn publish-authority! [ctx repo text]
  (spit (io/file repo source-path) text)
  (rt/git ctx repo "add" source-path)
  (rt/git ctx repo "commit" "-m" "Published task-file change")
  (rt/git ctx repo "push" "origin" "main")
  (rt/git ctx repo "rev-parse" "HEAD"))

(deftest unrelated-task-edits-preserve-an-immutable-selection-across-base-updates
  (let [{:keys [ctx repo task base]} (authority-fixture!)
        wt (flow/worktree! ctx "selected" base)
        task (merge (dissoc task :source-base) wt)
        _ (rt/update! ctx assoc-in [:tasks "selected"] task)
        current (publish-authority! ctx repo (str/replace authority-text "other behavior is accepted" "other behavior and its diagnostics are accepted"))]
    (flow/prepare-base! ctx task)
    (let [prepared (flow/task ctx "selected")]
      (is (= base (:source-base prepared)) "Legacy task selection is captured before its merge base advances")
      (is (= current (:base prepared)))
      (is (= (:task-digest task) (:task-digest prepared)))
      (is (nil? (workflow/validate-source! ctx prepared)))
      (spit (io/file (:worktree wt) source-path) (str (slurp (io/file (:worktree wt) source-path)) "\nScoped author evidence outside the task region.\n"))
      (is (nil? (workflow/validate-source! ctx prepared)) "Owned evidence does not replace published task authority")
      (with-redefs [flow/fresh-main! (fn [_] current) flow/assert-clean! (fn [& _] nil)]
        (flow/prepare-base! ctx prepared))
      (is (= base (:source-base (flow/task ctx "selected"))) "Later preparation never rebinds the selected source"))))

(deftest changed-or-unverifiable-selected-task-authority-remains-held
  (doseq [case-id [:changed :checked :removed :wrong-digest :wrong-origin :missing-origin :context :region]]
    (let [{:keys [ctx repo task]} (authority-fixture!)
          text (case case-id
                 :changed (str/replace authority-text "the value is independently accepted" "the value and new semantics are accepted")
                 :checked (str/replace authority-text "- [ ] `demo/value`" "- [x] `demo/value`")
                 :removed (str/replace authority-text #"- \[ \] `demo/value`[^\n]*\nDone when:[^\n]*\n" "")
                 :context (str/replace authority-text "# Tasks\n" "# Tasks\nGlobal closing rule: every task needs new verifier evidence.\n")
                 :region (str/replace authority-text "demo-implementation-tasks" "changed-implementation-tasks")
                 (str/replace authority-text "other behavior is accepted" "other behavior and its diagnostics are accepted"))
          current (publish-authority! ctx repo text)
          task (cond-> (assoc task :base current :worktree repo)
                 (= :wrong-digest case-id) (assoc :task-digest (rt/digest "not the captured source"))
                 (= :wrong-origin case-id) (assoc :source-base current)
                 (= :missing-origin case-id) (dissoc :source-base))]
      (rt/update! ctx assoc-in [:tasks (:key task)] task)
      (is (thrown? Exception (workflow/validate-source! ctx task)) (name case-id))
      (is (= task (flow/task ctx (:key task))) "Checking does not promote or rewrite the task"))))

(deftest task-source-refusals-are-retained-even-with-a-zero-exit-status
  (let [{:keys [ctx repo task]} (authority-fixture!)
        task (assoc task :worktree repo)
        argv ["git" "show" (str (:base task) ":" source-path)]
        key (rt/digest (pr-str argv))]
    (with-redefs [rt/command (fn [& _] {:exit 0 :out authority-text :err "git: error: cache: Operation not permitted"})]
      (is (thrown-with-msg? Exception #"encountered a refusal" (workflow/validate-source! ctx task))))
    (is (rt/shared-restriction ctx :commands key))
    (is (thrown-with-msg? Exception #"restricted|cannot retry or transfer" (workflow/validate-source! ctx task)))))
