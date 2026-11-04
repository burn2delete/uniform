(ns simplify.coordination-test
  "Concurrent controller/state fixtures and real disposable Git; no agents or GitHub."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is use-fixtures]]
            [simplify.codex :as codex]
            [simplify.coordination :as coordination]
            [simplify.core-test :as fixture]
            [simplify.github :as gh]
            [simplify.main :as main]
            [simplify.runtime :as rt]
            [simplify.workflow :as flow]
            [simplify.workflow-test :as git-fixture]))

(use-fixtures :each fixture/no-github-network)

(defn campaign [common name]
  (let [ctx (fixture/context)
        config (assoc (:config ctx) :repository "fixture/repo" :campaign name
                      :remote "origin" :base-branch "main")]
    (merge (assoc ctx :config config) (rt/coordination common config))))

(defn pair []
  (let [common (fixture/temp-dir)] [(campaign common "Simplify") (campaign common "Seedless")]))

(defn shared-state [ctx] (rt/read-edn (:coordination-file ctx)))
(def base (apply str (repeat 40 "a")))
(def candidate (apply str (repeat 40 "b")))
(def tree (apply str (repeat 40 "c")))
(def merged (apply str (repeat 40 "d")))
(def intent {:id (str "merge/" candidate) :base base :candidate candidate :tree tree :method :merge})
(def actual {:commit merged :tree tree :parents [base candidate] :main merged})

(deftest scheduler-preclaim-reuses-only-a-known-active-lease
  (let [[ctx other] (pair)
        lease (rt/claim-paths! ctx "task" ["src/a.clj"] :implementing)
        restarted (assoc ctx :path-leases (atom {}))]
    (is (= lease (rt/claim-paths! ctx "task" ["src/a.clj"] :checking)))
    (is (nil? (rt/claim-paths! restarted "task" ["src/a.clj"] :implementing)))
    (is (nil? (rt/claim-paths! ctx "task" ["src/b.clj"] :implementing)))
    (is (nil? (rt/claim-paths! other "task" ["src/a.clj"] :implementing)))
    (is (thrown? Exception (rt/with-claimed-paths ctx lease #(throw (ex-info "Unknown cleanup" {})))))
    (is (nil? (rt/claim-paths! ctx "task" ["src/a.clj"] :implementing)))
    (rt/inspect-release-paths! ctx lease "Fixture owner inspected the ended writer without any process probe")
    (is (rt/claim-paths! restarted "task" ["src/a.clj"] :implementing))))

(deftest context-discovery-cannot-save-a-stale-owner-snapshot
  (let [fixture (fixture/context)
        {:keys [repo]} (git-fixture/repository! fixture)
        config {:repo-dir repo :state-dir (:state-dir fixture) :repository "fixture/repo" :campaign "Simplify"}
        initial {:schema 1 :repository "fixture/repo" :campaign "Simplify" :owner-marker :initial}
        advanced (assoc initial :owner-marker :advanced)
        original-read main/read-state!
        calls (atom 0)]
    (rt/write-edn! (:state-file fixture) initial)
    (with-redefs [main/read-state! (fn [c path]
                                    (let [stale (original-read c path)]
                                      (rt/write-edn! path advanced)
                                      stale))
                  rt/command (fn [& _]
                               (swap! calls inc)
                               {:exit 1 :err "Operation not permitted" :out ""})]
      (let [ctx (main/context! config)]
        (is (= :initial (:owner-marker @(:state ctx))))
        (is (= advanced (rt/read-edn (:state-file fixture))))
        (is (zero? @calls) "No pre-lock external command can return a denial and save stale state")))
    (let [base (rt/git fixture repo "rev-parse" "HEAD")
          linked (str (io/file (:state-dir fixture) "linked"))]
      (rt/git fixture repo "worktree" "add" "-b" "linked-fixture" linked base)
      (is (= (rt/common-git-directory repo) (rt/common-git-directory linked)))
      (is (= (rt/canonical (io/file repo ".git")) (rt/common-git-directory repo))))))

(deftest ended-author-validation-does-not-reserve-an-idle-writer
  (let [[a b] (pair)
        {:keys [repo]} (git-fixture/repository! a)
        base (rt/git a repo "rev-parse" "HEAD")
        a (assoc-in a [:config :repo-dir] repo)
        t (merge git-fixture/finding {:worktree repo :branch "main" :base base :status :ready})]
    (rt/update! a assoc-in [:tasks (:key t)] t)
    (with-redefs [flow/fresh-main! (fn [_] base)
                  gh/status! (fn [& _] nil)
                  codex/job! (fn [& _]
                               (spit (io/file repo "unowned.edn") "{:preserve true}\n")
                               {:outcome "ready" :checks []})]
      (is (thrown-with-msg? Exception #"unowned path" (flow/prepare! a (:key t)))))
    (is (.exists (io/file repo "unowned.edn")) "The author's rejected edit remains available for inspection")
    (is (empty? (:writers (shared-state a))))
    (let [lease (rt/claim-paths! b "next" (:paths t) :implementing)]
      (is lease)
      (rt/release-paths! b lease))))

(deftest controller-locks-are-independent-main-lock-is-shared
  (let [[a b] (pair) writes (atom 0)]
    (rt/with-lock a
      #(do
         (is (= :second-controller (rt/with-lock b (fn [] :second-controller))))
         (is (thrown? Exception (rt/with-lock a (fn [] :duplicate))))
         (rt/try-with-main-lock a
           (fn []
             (is (= :busy (rt/try-with-main-lock b (fn [] (swap! writes inc)))))
             ;; The other controller can still perform disjoint implementation.
             (let [lease (rt/claim-paths! b "independent" ["runtime/b.clj"] :implementing)]
               (is lease)
               (is (= :working (rt/with-claimed-paths b lease (fn [] :working)))))))))
    (is (zero? @writes))
    (is (= 1 (rt/try-with-main-lock b #(swap! writes inc))))))

(deftest legacy-lock-rejection-precedes-state-reload-or-write
  (let [[ctx _] (pair) called (atom false)
        ctx (assoc ctx :coordinator-owner (atom false) :command "resume"
                       :read-state #(do (reset! called true) (throw (AssertionError. "Legacy writer is live"))))
        legacy (first (:legacy-controller-locks ctx))]
    (rt/write-edn! (:state-file ctx) {:schema 1 :preserve :live-legacy-writer})
    (let [before (slurp (:state-file ctx))]
      (coordination/with-file-lock legacy
        #(is (thrown-with-msg? Exception #"legacy controller" (rt/with-lock ctx (fn [] (reset! called true))))))
      (is (false? @called))
      (is (false? @(:coordinator-owner ctx)))
      (is (= before (slurp (:state-file ctx)))))))

(deftest legacy-registry-import-preserves-scoped-writers-and-check-refusals
  (let [[a b] (pair) legacy (:coordination-dir a)
        identity [(:state-file a) "legacy-task"]
        original {:identity identity :state-file (:state-file a) :key "legacy-task" :paths ["src/legacy.clj"]}
        denial {:error "Retained fixture refusal" :argv ["clojure" "-M:fixture"]}]
    (.mkdirs (io/file legacy))
    (rt/write-edn! (str (io/file legacy "claims.edn")) {:claims {identity original}})
    (rt/write-edn! (str (io/file legacy "denials.edn")) {[:check "check-key"] denial})
    (rt/import-state-coordination! b)
    (is (= denial (rt/shared-restriction a :checks "check-key")))
    (let [lease (first (vals (:writers (shared-state a))))]
      (is (= original (:legacy-claim lease)))
      (is (= :needs-inspection (:status lease)))
      (is (nil? (rt/claim-paths! a "legacy-task" ["src/legacy.clj"] :implementing)))
      (is (thrown? Exception (rt/inspect-release-paths! b lease "Foreign owner cannot release")))
      (rt/inspect-release-paths! a lease "Original fixture owner inspected the legacy writer as ended")
      (rt/import-state-coordination! b)
      (is (empty? (:writers (shared-state a))))
      (is (= denial (rt/shared-restriction b :checks "check-key"))))))

(deftest manual-owners-in-one-directory-retain-distinct-inspected-release-authority
  (let [[ctx _] (pair)
        owner-a (assoc ctx :state-file (str (io/file (:state-dir ctx) "manual-first.edn")))
        owner-b (assoc ctx :state-file (str (io/file (:state-dir ctx) "manual-second.edn")))
        original {:identity [(:state-file owner-a) "legacy"] :state-file (:state-file owner-a) :key "legacy" :paths ["src/held.clj"]}
        path (io/file (:coordination-dir ctx) "claims.edn")]
    (.mkdirs (.getParentFile path))
    (rt/write-edn! path {:claims {(:identity original) original}})
    (rt/import-state-coordination! ctx)
    (let [lease (first (vals (:writers (shared-state ctx))))]
      (is (= (:identity original) (:identity (first (coordination/claims ctx)))))
      (is (thrown-with-msg? Exception #"Writer lease identity differs"
                           (rt/inspect-release-paths! owner-b lease "Different owner in the same directory")))
      (is (= lease (get-in (shared-state ctx) [:writers (:id lease)])))
      (rt/inspect-release-paths! owner-a lease "Original fixture owner inspected actual writer end")
      (is (empty? (:writers (shared-state ctx)))))))

(deftest legacy-main-not-applied-is-still-fenced-until-shared-inspection
  (let [[a b] (pair) calls (atom 0)
        receipt {:status :not-applied :evidence "Legacy local observation" :previous {:status :uncertain}}]
    (rt/update! a assoc :tasks {"task" (assoc intent :status :attention)} :effects {(:id intent) receipt})
    (rt/import-state-coordination! a)
    (is (= :fenced (rt/try-with-main-lock b #(swap! calls inc))))
    (is (thrown-with-msg? Exception #"explicit successor" (rt/effect! a (:id intent) #(swap! calls inc))))
    (is (= receipt (get-in @(:state a) [:effects (:id intent)])))
    (is (zero? @calls))))

(deftest active-writers-not-frozen-candidates-reserve-paths
  (let [[a b] (pair)
        first (rt/claim-paths! a "first" ["compiler/a.clj"] :implementing)
        disjoint (rt/claim-paths! b "other" ["runtime/b.clj"] :checking)]
    (is first)
    (is disjoint)
    (is (nil? (rt/claim-paths! b "conflict" ["compiler"] :implementing)))
    (rt/lease-phase! a first :checking)
    (is (nil? (rt/claim-paths! b "conflict" ["compiler/a.clj"] :implementing)))
    (rt/with-claimed-paths a first (fn [] :checked))
    (rt/update! a assoc-in [:tasks "first"] {:status :ci :paths ["compiler/a.clj"]})
    (let [next (rt/claim-paths! b "next" ["compiler/a.clj"] :implementing)]
      (is next "An immutable CI candidate is not an active writer")
      (rt/release-paths! b next))
    (rt/release-paths! b disjoint)))

(deftest known-ended-refusals-release-paths-unknown-writers-require-inspection
  (let [[a b] (pair)
        ended (rt/claim-paths! a "ended" ["a.clj"] :implementing)]
    (is (thrown? Exception
                 (rt/with-claimed-paths a ended #(throw (ex-info "Denied optional action" {:restricted true :writer-ended? true})))))
    (is (rt/claim-paths! b "allowed-distinct-work" ["a.clj"] :implementing))
    (let [unknown (rt/claim-paths! a "unknown" ["b.clj"] :checking)]
      (is (thrown? Exception
                   (rt/with-claimed-paths a unknown #(throw (ex-info "Cleanup denied" {:restricted true :writer-ended? false})))))
      (is (= :needs-inspection (get-in (shared-state a) [:writers (:id unknown) :status])))
      (is (nil? (rt/claim-paths! b "blocked" ["b.clj"] :implementing)))
      (is (thrown? Exception (rt/inspect-release-paths! b unknown "Another owner cannot release this writer")))
      (rt/inspect-release-paths! a unknown "Fixture original-owner inspection established the scoped writer ended; no denied action retried")
      (is (rt/claim-paths! b "now-allowed" ["b.clj"] :implementing)))))

(deftest unknown-main-effect-survives-controller-release-and-blocks-other-merges
  (let [[a b] (pair) calls (atom 0)]
    (try
      (rt/try-with-main-lock a
        #(rt/effect! a (:id intent)
                     (fn [] (rt/begin-main-merge! a intent)
                       (swap! calls inc)
                       (throw (RuntimeException. "Provider reply lost")))))
      (is false "Expected the uncertain mutation")
      (catch Exception e
        (is (= (:id intent) (:effect (ex-data e))))
        (is (= :uncertain (:effect-status (ex-data e))))))
    (is (= :fenced (rt/try-with-main-lock b #(swap! calls inc))))
    (is (:pending (rt/main-merge-state a (:id intent))))
    (is (= {:pending nil :inspection nil} (rt/main-merge-state b (:id intent))))
    (is (= 1 @calls))
    (is (= :uncertain (:status (first (vals (:main-effects (shared-state b)))))))
    (is (thrown? Exception (rt/reconcile-main-merge! b (:id intent) {:evidence "Foreign owner" :outcome :not-applied})))
    (is (thrown? Exception
                 (rt/reconcile-main-merge! a (:id intent) {:evidence "Wrong result" :outcome :merged
                                                        :actual (assoc actual :parents [candidate base])})))
    (rt/reconcile-main-merge! a (:id intent) {:evidence "Fixture provider/Git inspection proves actual merge" :outcome :merged :actual actual})
    (is (= :merged (get-in (rt/main-merge-state a (:id intent)) [:inspection :inspection :outcome])))
    (rt/import-state-coordination! a)
    (is (empty? (:main-effects (shared-state a))) "An inspected shared effect is not reimported as a legacy unknown")
    (is (= 2 (rt/try-with-main-lock b #(swap! calls inc))))
    (is (= :uncertain (get-in @(:state a) [:effects (:id intent) :status]))
        "Shared reconciliation never clears/replays the original native effect")))

(deftest inspected-recovery-never-rearms-restricted-or-mismatched-evidence
  (doseq [restricted? [true false]]
    (let [[a b] (pair)
          t (merge (select-keys intent [:base :candidate :tree])
                   {:key "recovery" :status :attention :resume-status :merge :merge-intent intent
                    :error {:details {:effect (:id intent) :restricted restricted?}}})
          result-file (str (io/file (:state-dir a) "inspection-result.edn"))]
      (rt/update! a assoc :repository "fixture/repo" :campaign "Simplify" :tasks {"recovery" t})
      (is (thrown? Exception
                   (rt/try-with-main-lock a
                     #(rt/effect! a (:id intent) (fn [] (rt/begin-main-merge! a intent)
                                                  (throw (ex-info "Fixture provider failure" {:restricted restricted?})))))))
      (rt/reconcile-main-merge! a (:id intent) {:outcome :not-applied :evidence "Fixture original owner inspected no remote mutation"})
      (rt/write-edn! result-file {:evidence "Fixture original effect inspected against no applied mutation" :result {:not-applied true}})
      (if restricted?
        (do
          (is (thrown? Exception (main/dispatch! a "reconcile" [(:id intent) result-file])))
          (is (thrown-with-msg? Exception #"Restricted actions" (main/dispatch! a "retry" ["recovery"])))
          (is (= :restricted (get-in @(:state a) [:effects (:id intent) :status]))))
        (do
          (is (thrown-with-msg? Exception #"reconciliation before retry" (main/dispatch! a "retry" ["recovery"])))
          (main/dispatch! a "reconcile" [(:id intent) result-file])
          (is (= :uncertain (get-in @(:state a) [:effects (:id intent) :prior :status])))
          (flow/task! a "recovery" :tree base)
          (is (thrown-with-msg? Exception #"matching local reconciliation" (main/dispatch! a "retry" ["recovery"])))
          (flow/task! a "recovery" :tree tree)
          (rt/shared-restrict! b :commands (rt/digest (pr-str ["gh-pr-merge" "fixture/repo"])) {:error "Retained fixture merge permission refusal"})
          (is (thrown-with-msg? Exception #"Repository merge action remains restricted" (main/dispatch! a "retry" ["recovery"])))))
      (is (= intent (:merge-intent (flow/task a "recovery"))))
      (is (= {:pending nil :inspection nil} (rt/main-merge-state b (:id intent)))))))

(deftest interrupted-inspection-successor-import-retains-the-exact-tuple
  (let [[a b] (pair)
        successor (assoc intent :id (str (:id intent) "/inspection/retained"))]
    (rt/update! a assoc :effects {(:id successor) {:status :pending}}
                :tasks {"source" {:candidate candidate :base base :tree tree :merge-intent successor}})
    (rt/import-state-coordination! a)
    (is (= (select-keys successor [:id :base :candidate :tree :method])
           (select-keys (:pending (rt/main-merge-state a (:id successor))) [:id :base :candidate :tree :method])))
    (is (= :fenced (rt/try-with-main-lock b (fn [] :must-not-run))))))

(deftest incomplete-legacy-intent-retains-the-operators-inspected-source-tuple
  (let [[a _] (pair)
        inspected (assoc intent :method :squash)]
    (rt/update! a assoc :effects {(:id inspected) {:status :uncertain}})
    (rt/import-state-coordination! a)
    (rt/reconcile-main-merge! a (:id inspected)
                              {:outcome :not-applied :evidence "Original owner inspected the legacy source identity and no applied merge"
                               :original-intent inspected})
    (is (= inspected (select-keys (get-in (rt/main-merge-state a (:id inspected)) [:inspection :intent]) (keys inspected))))
    (is (= :uncertain (get-in @(:state a) [:effects (:id inspected) :status])))))

(deftest actual-verification-clears-only-the-exact-main-intent
  (let [[a b] (pair)]
    (rt/try-with-main-lock a
      #(do (rt/begin-main-merge! a intent)
           (is (thrown? Exception (rt/complete-main-merge! a intent (assoc actual :tree base))))) )
    (is (= :fenced (rt/try-with-main-lock b (fn [] :must-not-run))))
    (rt/reconcile-main-merge! a (:id intent) {:evidence "Observed no provider mutation" :outcome :not-applied})
    (rt/try-with-main-lock a
      #(do (rt/begin-main-merge! a intent)
           (is (= actual (rt/complete-main-merge! a intent actual)))))
    (is (= :free (rt/try-with-main-lock b (fn [] :free))))))

(deftest shared-permission-refusals-cannot-transfer-to-a-new-campaign
  (let [[a b] (pair) calls (atom 0)]
    (with-redefs [rt/command (fn [& _] (swap! calls inc) {:exit 1 :out "" :err "HTTP 403: Permission denied"})]
      (is (thrown? Exception (rt/run! a (:state-dir a) ["git" "push" "origin" "first:refs/heads/codex/first"])))
      (is (thrown? Exception (rt/run! b (:state-dir b) ["git" "push" "another-alias" "next:refs/heads/codex/next"])))
      (is (= 1 @calls)))
    (with-redefs [rt/command (fn [& _] (swap! calls inc) {:exit 0 :err "" :out "{\"errors\":[{\"type\":\"FORBIDDEN\",\"message\":\"Denied\"}]}"})]
      (is (thrown? Exception (gh/graphql a "mutation first" {})))
      (is (thrown? Exception (gh/graphql b "mutation different" {})))
      (is (= 2 @calls)))))

(deftest legacy-unknown-writers-and-refusals-are-imported-without-a-global-task-quota
  (let [[a b] (pair)
        refusal-key (rt/digest (pr-str ["POST" "graphql"]))]
    (rt/update! a assoc :restricted-actions {refusal-key {:method "POST" :endpoint "graphql" :error "Original denial"}}
                :tasks {"unknown" {:status :attention :paths ["a.clj" "a_test.clj"]
                                   :shared-dependency-validation {:candidate candidate :artifact-dir "retained-fixture-log" :descendant-coverage :unknown}}
                        "ordinary" {:status :attention :paths ["other.clj"]}})
    (rt/import-state-coordination! a)
    (is (= "Original denial" (:error (rt/shared-restriction b :api refusal-key))))
    (is (nil? (rt/claim-paths! b "overlap" ["a_test.clj"] :implementing)))
    (is (rt/claim-paths! b "independent" ["other.clj"] :implementing))
    (let [lease (first (filter #(= "unknown" (:key %)) (vals (:writers (shared-state a)))))]
      (rt/inspect-release-paths! a lease "Fixture scoped original-owner termination inspection")
      (rt/import-state-coordination! a)
      (is (not-any? #(= "unknown" (:key %)) (vals (:writers (shared-state a))))))))

(deftest simultaneous-snapshots-use-isolated-refs-without-fetch-head-or-tracking-races
  (let [fixture (fixture/context)
        {:keys [repo]} (git-fixture/repository! fixture)
        common (str (io/file repo ".git"))
        initial (rt/git fixture repo "rev-parse" "HEAD")
        other (str (io/file (:state-dir fixture) "second-worktree"))
        _ (rt/git fixture repo "worktree" "add" "--detach" other initial)
        a (assoc-in (campaign common "Simplify") [:config :repo-dir] repo)
        b (assoc-in (campaign common "Seedless") [:config :repo-dir] other)]
    (spit (io/file repo "upstream.edn") "{:next true}\n")
    (rt/git fixture repo "add" "upstream.edn")
    (rt/git fixture repo "commit" "-m" "Concurrent upstream")
    (rt/git fixture repo "push" "origin" "main")
    (let [expected (rt/git fixture repo "rev-parse" "HEAD")
          _ (rt/git fixture repo "update-ref" "refs/remotes/origin/main" initial)
          _ (spit (io/file common "FETCH_HEAD") "preserved fetch head\n")
          jobs (mapv #(future (rt/fresh-main! %)) [a a b b])]
      (is (= [expected expected expected expected] (mapv deref jobs)))
      (is (= initial (rt/git fixture repo "rev-parse" "refs/remotes/origin/main"))))
    (is (= "preserved fetch head\n" (slurp (io/file common "FETCH_HEAD"))))))

(defn java-command [& args]
  (into [(str (io/file (System/getProperty "java.home") "bin/java")) "-cp"
         (str/join java.io.File/pathSeparator
                   (map #(.getAbsolutePath (io/file %))
                        (str/split (System/getProperty "java.class.path") (re-pattern java.io.File/pathSeparator))))
         "clojure.main"] args))

(deftest streamed-check-failures-preserve-real-termination-and-host-capture-evidence
  (doseq [[mode ended? restricted?] [["stream-assertion" true false] ["stream-cleanup" false true]]]
    (let [ctx (assoc-in (fixture/context) [:config :codex-command]
                        (java-command "-m" "simplify.fake-server" mode))]
      (try
        (codex/command! ctx "stream-fixture" (:state-dir ctx) ["clojure" "-M:test"])
        (is false "The failing check must remain a failure")
        (catch Exception e
          (is (= ended? (:writer-ended? (ex-data e))))
          (is (= restricted? (:restricted (ex-data e))))
          (is (seq (get-in (ex-data e) [:result :stderr])))))))
  (let [ctx (assoc-in (fixture/context) [:config :codex-command]
                      (java-command "-m" "simplify.fake-server" "stream-capped"))
        result (codex/command! ctx "capture-fixture" (:state-dir ctx) ["clojure" "-M:test"])]
    (is (:capture-truncated result))
    (is (= "fixture streamed output" (:stderr result)))))

(deftest real-cli-contender-status-and-pause-cannot-clobber-an-owner
  (let [fixture (fixture/context) {:keys [repo]} (git-fixture/repository! fixture)
        state-dir (fixture/temp-dir)
        config (merge main/defaults {:repo-dir repo :state-dir state-dir :campaign "CLI fixture"
                                    :worktree-root (str (io/file (:state-dir fixture) "workers"))})
        config-file (str (io/file state-dir "config.edn"))
        _ (rt/write-edn! config-file config)
        ctx (main/context! config)]
    (rt/with-lock ctx #(rt/update! ctx assoc :status :running :owner-marker :initial))
    (let [stale (main/context! config)]
      (rt/with-lock ctx
        #(do
           (rt/update! ctx assoc :owner-marker :fresh)
           (doseq [[command exit] [["status" 0] ["pause" 0] ["doctor" 1] ["run" 1] ["resume" 1]]]
             (let [before (slurp (:state-file ctx))
                   ;; Exercise the real entry point in another JVM while making
                   ;; even a broken contender unable to contact GitHub.
                   entry (str "(require '[simplify.core-test :as fixture] '[simplify.main :as main]) "
                              "(fixture/no-github-network #(apply main/-main " (pr-str [command config-file]) "))")
                   result (rt/command ctx repo (java-command "-e" entry))]
               (is (= exit (:exit result)))
               (is (= before (slurp (:state-file ctx))))))))
      (with-redefs [rt/command (fn [& _] {:exit 1 :err "HTTP 403: Permission denied" :out ""})]
        (is (thrown? Exception (main/dispatch! stale "doctor" []))))
      (is (= :fresh (:owner-marker (rt/read-edn (:state-file ctx)))))
      (is (seq (:restricted-commands (rt/read-edn (:state-file ctx)))))
      (main/controller! stale #(is (= :fresh (:owner-marker @(:state stale)))))
      (is (thrown? Exception (main/controller! stale #(throw (ex-info "Owned failure" {})))))
      (is (= :attention (:status (rt/read-edn (:state-file ctx))))))))
