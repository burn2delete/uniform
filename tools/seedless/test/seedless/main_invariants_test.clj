(ns seedless.main-invariants-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is use-fixtures]]
            [seedless.github :as gh]
            [seedless.main :as main]
            [seedless.workflow :as workflow]
            [seedless.workflow-test :as fixture]
            [simplify.main :as shared]
            [simplify.runtime :as rt]))

(use-fixtures :each (fn [test] (with-redefs [rt/event! (fn [& _])] (test))))

(defn lock-context []
  (let [ctx (fixture/context)
        identity {:repository "fixture/repo" :objective "fixture"}]
    (assoc ctx :identity identity :owns-lock? (atom false)
           :lock-file (str (io/file (:root ctx) "controller.lock"))
           :state (atom {:schema :seedless/v1 :identity identity :status :running
                         :tasks {} :effects {} :jobs {}}))))

(deftest a-lock-contender-cannot-write-controller-state
  (let [owner (lock-context)
        contender (assoc owner :state (atom (assoc @(:state owner) :status :new)) :owns-lock? (atom false))]
    (try
      (rt/save! owner)
      (let [before (slurp (:state-file owner))]
        (rt/with-lock owner
          (fn []
            (with-redefs [workflow/campaign! (fn [& _] (throw (ex-info "must not dispatch" {})))]
              (doseq [command ["run" "resume" "doctor" "setup" "coordinate" "retry" "reconcile" "reconcile-main" "release-writer"]]
                (is (thrown? Exception (main/dispatch! contender command ["a" "unused.edn"])))
                (is (= before (slurp (:state-file owner)))))))))
      (is (false? @(:owns-lock? contender)))
      (finally (fixture/delete-tree! (:root owner))))))

(deftest campaign-failure-persists-only-under-owned-lock
  (let [ctx (lock-context) calls (atom []) original-update rt/update!]
    (try
      (rt/save! ctx)
      (with-redefs [workflow/campaign! (fn [_] (throw (ex-info "fixture campaign failure" {})))
                    rt/update! (fn [context & args]
                                 (swap! calls conj @(:owns-lock? context))
                                 (apply original-update context args))]
        (is (thrown-with-msg? Exception #"fixture campaign failure" (main/dispatch! ctx "run" []))))
      (is (= [true] @calls))
      (is (= :attention (:status (rt/read-edn (:state-file ctx)))))
      (is (false? @(:owns-lock? ctx)))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest retained-native-work-blocks-provider-writes-but-keeps-inspected-recovery
  (doseq [phase [:implementing :checking :review :finalizing]
          command ["setup" "run" "resume"]]
    (let [ctx (lock-context) calls (atom [])
          effects {"retained-tracking" {:status :uncertain}}
          t (cond-> {:key "slice" :status phase :paths ["module.clj"]
                     :error {:message "Retained earlier source failure"} :resume-status :repair}
              (= :finalizing phase) (assoc :merged "actual-merge" :finalization-effect "retained-tracking"))]
      (try
        (rt/update! ctx assoc :tasks {"slice" t} :effects effects)
        (let [lease (rt/claim-paths! ctx "slice" (:paths t) :checking)]
          (try (rt/with-claimed-paths ctx lease #(throw (ex-info "Retained unknown fixture writer" {})))
               (catch Exception _))
          (let [writers (:writers (rt/read-edn (:coordination-file ctx)))]
            (with-redefs [gh/setup! (fn [& _] (swap! calls conj :setup))
                          gh/status! (fn [& _] (swap! calls conj :status))
                          gh/comment! (fn [& _] (swap! calls conj :comment))
                          workflow/campaign-issue! (fn [& _] (swap! calls conj :campaign))
                          workflow/job! (fn [& _] (swap! calls conj :author))]
              (is (thrown-with-msg? Exception #"Inspect retained Seedless work" (main/dispatch! ctx command []))))
            (is (empty? @calls))
            (is (= :attention (:status (workflow/task ctx "slice"))))
            (is (= phase (:resume-status (workflow/task ctx "slice"))))
            (is (= (select-keys t [:status :error :resume-status])
                   (dissoc (last (:history (workflow/task ctx "slice"))) :kind)))
            (is (= effects (:effects @(:state ctx))))
            (is (= writers (:writers (rt/read-edn (:coordination-file ctx)))))
            (is (empty? (:controllers (rt/read-edn (:coordination-file ctx)))))
            (when (and (= :implementing phase) (= "setup" command))
              (let [file (io/file (:root ctx) "inspection.edn")]
                (spit file (pr-str {:evidence "Fixture owner inspected the ended writer; original lease remains held until explicit inspected release"}))
                (main/with-current-lock ctx #(main/retry! ctx "slice" (str file)))
                (is (= :repair (:status (workflow/task ctx "slice"))))
                (is (nil? (rt/claim-paths! ctx "slice" ["module.clj"] :implementing)))
                (rt/inspect-release-paths! ctx lease "Fixture owner verified the original writer ended")
                (is (some? (rt/claim-paths! ctx "slice" ["module.clj"] :implementing)))))))
        (finally (fixture/delete-tree! (:root ctx)))))))

(deftest interrupted-controller-setup-performs-no-provider-write
  (let [ctx (lock-context) calls (atom 0)]
    (try
      (rt/update! ctx assoc :interruption {:descendant-coverage :unknown :inspection-required true})
      (let [before @(:state ctx)]
        (with-redefs [gh/setup! (fn [& _] (swap! calls inc))]
          (is (thrown-with-msg? Exception #"Inspect retained controller interruption" (main/dispatch! ctx "setup" []))))
        (is (zero? @calls))
        (is (= before @(:state ctx))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest setup-registration-is-released-after-provider-failure
  (let [ctx (lock-context) admitted (atom false)]
    (try
      (rt/save! ctx)
      (with-redefs [gh/setup! (fn [& _]
                                (reset! admitted (boolean (seq (:controllers (rt/read-edn (:coordination-file ctx))))))
                                (throw (ex-info "Fixture setup failed" {})))]
        (is (thrown-with-msg? Exception #"Fixture setup failed" (main/dispatch! ctx "setup" []))))
      (is @admitted)
      (is (empty? (:controllers (rt/read-edn (:coordination-file ctx)))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest successful-lock-acquisition-refreshes-the-state-snapshot
  (let [ctx (lock-context)
        actual (assoc @(:state ctx) :status :complete :completion {:base "verified-main"}
                      :tasks {"retained" {:status :done :merged "retained-merge"}}
                      :effects {"retained-write" {:status :done :result {:observed true}}}
                      :inspections [{:evidence "Retained owner history"}])]
    (try
      (rt/write-edn! (:state-file ctx) actual)
      (with-redefs [workflow/fresh-main! (constantly "verified-main")
                    workflow/campaign! (fn [& _] (throw (ex-info "stale state was used" {})))]
        (is (= :complete (main/dispatch! ctx "run" []))))
      (is (= actual @(:state ctx)))
      (is (= actual (rt/read-edn (:state-file ctx))))
      (finally (fixture/delete-tree! (:root ctx))))))

(defn startup-config [ctx]
  {:repository "fixture/repo" :campaign "fixture" :objective "fixture objective"
   :repo-dir (rt/canonical (io/file (:root ctx) "repo")) :remote "origin" :base-branch "main"
   :project-title "Uniform Seedless" :state-dir (:state-dir ctx)
   :worktree-root (rt/canonical (io/file (:root ctx) "workers"))
   :contracts ["AGENTS.md"] :checks [main/boundary-check] :completion-checks [main/completion-check]
   :codex-command ["codex" "app-server" "--listen" "stdio://"]})

(defn metadata-probe [ctx argv]
  (let [config (:config ctx)]
    (cond
      (= ["git" "check-ref-format" "--branch"] (vec (take 3 argv))) (:base-branch config)
      (= ["git" "rev-parse" "--show-toplevel"] argv) (:repo-dir config)
      (= ["git" "rev-parse" "--git-common-dir"] argv) (str (io/file (:repo-dir config) ".git"))
      (= ["git" "remote" "get-url"] (vec (take 3 argv))) "git@github.com:fixture/repo.git"
      :else (throw (ex-info "Unexpected fixture probe" {:argv argv})))))

(deftest refused-startup-probes-do-not-persist-a-stale-state
  (let [ctx (lock-context) config (startup-config ctx)]
    (try
      (rt/save! ctx)
      (let [before (slurp (:state-file ctx))]
        (with-redefs [rt/command (fn [& _] {:exit 1 :out "" :err "Permission denied: deterministic fixture"})]
          (let [error (try (main/context! config) nil (catch Exception e e))]
            (is (true? (:restricted (ex-data error))))
            (is (= before (slurp (:state-file ctx)))))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest state-binds-the-execution-target-but-not-host-concurrency
  (let [fixture (fixture/context) config (startup-config fixture)]
    (try
      (with-redefs [main/probe! metadata-probe]
        (let [ctx (main/context! config)]
          (rt/save! ctx)
          (doseq [[key value] [[:repo-dir (rt/canonical (io/file (:root fixture) "another-clone"))]
                               [:remote "another-origin"] [:base-branch "another-branch"]
                               [:worktree-root (rt/canonical (io/file (:root fixture) "other-workers"))]
                               [:project-title "Other project"] [:checks [main/boundary-check ["clojure" "-e" "nil"]]]
                               [:completion-checks [main/completion-check ["clojure" "-e" "nil"]]]
                               [:codex-command ["codex" "app-server" "--listen" "stdio://" "--config" "fixture=true"]]]]
            (is (thrown-with-msg? Exception #"different Seedless campaign" (main/context! (assoc config key value)))))
          (is (= (:identity ctx) (:identity (main/context! (assoc config :parallelism 2 :check-timeout-ms 900000 :model "fixture-model")))))))
      (finally (fixture/delete-tree! (:root fixture))))))

(deftest fetch-and-push-must-identify-the-configured-github-repository
  (let [fixture (fixture/context) config (startup-config fixture)]
    (try
      (doseq [bad-argv [["git" "remote" "get-url" "origin"] ["git" "remote" "get-url" "--push" "origin"]]]
        (with-redefs [main/probe! (fn [ctx argv] (if (= argv bad-argv) "git@github.com:other/repo.git" (metadata-probe ctx argv)))]
          (is (thrown-with-msg? Exception #"same target" (main/context! config)))))
      (is (= "fixture/repo" (main/repository-name "https://github.com/fixture/repo.git")))
      (is (= "fixture/repo" (main/repository-name "ssh://git@github.com/fixture/repo.git")))
      (finally (fixture/delete-tree! (:root fixture))))))

(deftest configuration-rejects-invalid-watchdogs-and-non-codex-launchers
  (let [fixture (fixture/context) config (startup-config fixture)
        file (io/file (:root fixture) "config.edn")]
    (try
      (doseq [[key value] [[:check-timeout-ms 0] [:check-timeout-ms -1]
                           [:codex-command ["sh" "-c" "unrelated command"]]
                           [:codex-command ["codex" "app-server" nil]]
                           [:remote "--all"] [:base-branch nil]]]
        (spit file (pr-str (assoc config key value)))
        (is (thrown? Exception (main/config! (str file)))))
      (finally (fixture/delete-tree! (:root fixture))))))

(deftest cleanup-records-unknown-coverage-and-needs-inspected-recovery
  (let [ctx (lock-context) file (io/file (:root ctx) "inspection.edn")]
    (try
      (rt/save! ctx)
      (main/owned-cleanup! ctx :not-owner)
      (is (nil? (:interruption (rt/read-edn (:state-file ctx)))))
      (reset! (:owns-lock? ctx) true)
      (main/owned-cleanup! ctx :fixture-interruption)
      (reset! (:owns-lock? ctx) false)
      (is (= :unknown (get-in @(:state ctx) [:interruption :descendant-coverage])))
      (is (true? (get-in @(:state ctx) [:interruption :inspection-required])))
      (is (thrown-with-msg? Exception #"Inspect retained" (main/dispatch! ctx "run" [])))
      (spit file (pr-str {:evidence "Inspected deterministic retained state; no worker process was started"}))
      (main/retry! ctx "audit" (str file))
      (is (nil? (:interruption @(:state ctx))))
      (is (= :unknown (get-in @(:state ctx) [:inspections 0 :interruption :descendant-coverage])))
      (rt/update! ctx assoc :interruption {:restricted true :descendant-coverage :unknown})
      (let [before (slurp (:state-file ctx))]
        (is (thrown? Exception (main/retry! ctx "audit" (str file))))
        (is (= before (slurp (:state-file ctx)))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest uncertain-task-retry-needs-reconciliation-and-never-reauthors-a-merge
  (doseq [merged? [false true]]
    (let [ctx (lock-context) key "held"
          id (if merged? "close/held" "push/candidate")
          t (cond-> {:key key :status :attention :resume-status :done
                     :issue {:node_id "issue"} :error {:details {:effect id}}}
              merged? (assoc :merged "verified-merge"))
          file (io/file (:root ctx) "effect-inspection.edn")]
      (try
        (rt/update! ctx assoc-in [:tasks key] t)
        (rt/update! ctx assoc-in [:effects id] {:status :uncertain})
        (spit file (pr-str {:evidence "Inspected original deterministic fixture effect" :result "observed-fixture-result"}))
        (let [before (rt/read-edn (:state-file ctx))]
          (is (thrown-with-msg? Exception #"reconciliation" (main/retry! ctx key (str file))))
          (is (= before (rt/read-edn (:state-file ctx)))))
        (shared/reconcile! ctx id (str file))
        (main/retry! ctx key (str file))
        (is (= (if merged? :ci :repair) (get-in @(:state ctx) [:tasks key :status])))
        (is (= :done (get-in @(:state ctx) [:effects id :status])))
        (is (= (:error t) (get-in @(:state ctx) [:tasks key :history 0 :error])))
        (finally (fixture/delete-tree! (:root ctx)))))))
