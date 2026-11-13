(ns seedless.main-invariants-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is use-fixtures]]
            [seedless.github :as gh]
            [seedless.main :as main]
            [seedless.workflow :as workflow]
            [seedless.workflow-test :as fixture]
            [simplify.main :as shared]
            [simplify.runtime :as rt])
  (:import [java.math BigInteger]
           [java.nio.charset StandardCharsets]
           [java.nio.file Files StandardOpenOption]
           [java.security MessageDigest]))

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

(deftest inspected-pending-evidence-retry-preserves-the-immutable-candidate
  (doseq [[status phase marker] [[:attention :acceptance-pending false]
                               [:attention :checking true] [:attention :review true]
                               [:acceptance-pending :ci false]]]
    (let [ctx (lock-context) key "pending" id "compiler-progress/retained"
          inspection {:evidence "Inspected the original read-only progress failure and retained candidate"
                      :result {:outcome "pending" :missing-evidence ["Additional source inspection"]}}
          t {:key key :status status :resume-status phase :pending-evidence-resume marker
             :base "before" :candidate "retained-candidate" :tree "retained-tree"
             :compiler-progress-intent id
             :compiler-observation {:input-id "before-input"}
             :compiler-result {:status :blocked :input-id "after-input"}
             :checks {:candidate "retained-candidate" :tree "retained-tree" :commands [["clojure" "positive.clj"]]}
             :compiler-investigation-planned "old-investigation"
             :error (when (= :attention status)
                      {:message "Read-only progress assessment failed" :details {:effect id}})}
          file (io/file (:root ctx) "progress-inspection.edn")]
      (try
        (rt/update! ctx assoc :tasks {key t} :effects {id {:status :uncertain}})
        (rt/write-edn! file inspection)
        (let [before (rt/read-edn (:state-file ctx))]
          (is (thrown-with-msg? Exception #"reconciliation" (main/retry! ctx key (str file))))
          (is (= before (rt/read-edn (:state-file ctx)))))
        (shared/reconcile! ctx id (str file))
        ;; Dispatch must reload the reconciled record, as a fresh controller would.
        (reset! (:state ctx) (assoc @(:state ctx) :tasks {}))
        (main/dispatch! ctx "retry" [key (str file)])
        (let [resumed (get-in (rt/read-edn (:state-file ctx)) [:tasks key])]
          (is (= :acceptance-pending (:status resumed)))
          (is (true? (:compiler-progress-resume resumed)))
          (is (nil? (:compiler-investigation-planned resumed)))
          (is (= (rt/digest (pr-str (:history resumed))) (:compiler-progress-inspection resumed)))
          (is (= (select-keys t [:base :candidate :tree :compiler-observation :compiler-result :checks])
                 (select-keys resumed [:base :candidate :tree :compiler-observation :compiler-result :checks])))
          (is (= {:error (:error t) :inspection inspection} (last (:history resumed))))
          (is (= inspection (:feedback resumed)))
          (is (nil? (:acceptance resumed))))
        (is (= :done (get-in @(:state ctx) [:effects id :status])))
        (is (= :uncertain (get-in @(:state ctx) [:effects id :prior :status])))
        (finally (fixture/delete-tree! (:root ctx)))))))

(deftest failed-diagnosis-audit-retry-needs-the-original-effect-settled
  (doseq [[effect-status restricted]
          [[:pending false] [:uncertain false] [:restricted false]
           [:done true] [:not-applied false] [nil false]]]
    (let [ctx (lock-context) id "compiler-diagnosis/failed"
          blocker {:effect id :observation-key "observed-input" :restricted restricted
                   :message "The read-only diagnosis did not yield a validated result"}
          file (io/file (:root ctx) "diagnosis-inspection.edn")]
      (try
        (rt/update! ctx assoc :diagnosis-blocker blocker :last-discovery-key "retained-discovery"
                    :effects (if effect-status {id {:status effect-status}} {}))
        (rt/write-edn! file {:evidence "Inspected the failed read-only diagnosis"})
        (let [before (rt/read-edn (:state-file ctx))]
          (is (thrown? Exception (main/retry! ctx "audit" (str file))))
          (is (= before (rt/read-edn (:state-file ctx)))))
        (finally (fixture/delete-tree! (:root ctx)))))))

(deftest pending-evidence-retry-keeps-unresolved-diagnosis-effects-held
  (doseq [status [:pending :uncertain :restricted]]
    (let [ctx (lock-context) id "compiler-diagnosis/retained"
          file (io/file (:root ctx) "pending-inspection.edn")]
      (try
        (rt/update! ctx assoc :tasks {"pending" {:key "pending" :status :acceptance-pending
                                               :candidate "retained-candidate"
                                               :compiler-result {:diagnosis {:status :blocked :effect id}}}}
                    :effects {id {:status status}})
        (rt/write-edn! file {:evidence "Inspected the pending candidate; original effect is unresolved"})
        (let [before (rt/read-edn (:state-file ctx))]
          (is (thrown-with-msg? Exception #"reconciliation" (main/retry! ctx "pending" (str file))))
          (is (= before (rt/read-edn (:state-file ctx)))))
        (finally (fixture/delete-tree! (:root ctx)))))))

(deftest inspected-diagnosis-recovery-retains-history-and-reopens-discovery
  (let [ctx (lock-context) id "compiler-diagnosis/failed"
        blocker {:effect id :observation-key "observed-input" :restricted false
                 :message "Read-only diagnosis provider failed"}
        inspection {:evidence "Inspected the completed original diagnosis job and reconciled its result"
                    :result {:outcome "blocked"}}
        file (io/file (:root ctx) "diagnosis-inspection.edn")]
    (try
      (rt/update! ctx assoc :diagnosis-blocker blocker :last-discovery-key "retained-discovery"
                  :diagnosis-intents {"observed-input" id} :effects {id {:status :uncertain}})
      (rt/write-edn! file inspection)
      (shared/reconcile! ctx id (str file))
      (let [effect (get-in @(:state ctx) [:effects id])]
        ;; A failed diagnosis is recoverable even after plan! cleared :planning.
        (is (nil? (:planning @(:state ctx))))
        (reset! (:state ctx) (dissoc @(:state ctx) :diagnosis-blocker))
        (main/dispatch! ctx "retry" ["audit" (str file)])
        (let [resumed (rt/read-edn (:state-file ctx))]
          (is (nil? (:diagnosis-blocker resumed)))
          (is (nil? (:last-discovery-key resumed)))
          (is (= blocker (get-in resumed [:inspections 0 :diagnosis-blocker])))
          (is (= inspection (get-in resumed [:inspections 0 :inspection])))
          (is (= {"observed-input" id} (:diagnosis-intents resumed)))
          (is (= effect (get-in resumed [:effects id])))
          (is (empty? (:tasks resumed))))
        (let [before (rt/read-edn (:state-file ctx))]
          (is (thrown-with-msg? Exception #"No inspected planning" (main/retry! ctx "audit" (str file))))
          (is (= before (rt/read-edn (:state-file ctx))))))
      (finally (fixture/delete-tree! (:root ctx))))))

(def status-identity-keys
  [:repository :campaign :objective :contracts :completion-checks :repo-dir :remote
   :base-branch :worktree-root :checks :project-title :codex-command])

(defn status-config [ctx]
  (startup-config ctx))

(defn status-identity [config]
  (assoc (select-keys config status-identity-keys)
         :git-common-dir "/fixture/git-common"))

(defn status-state [config]
  {:schema :seedless/v1
   :identity (status-identity config)
   :status :running
   :tasks
   {"attention/restricted"
    {:status :attention :resume-status :ci
     :error {:message "provider refused"
             :details {:effect ["effect/restricted" "effect/missing"]
                       :effect-status :restricted :restricted true
                       :job "job/refused"
                       :refusal {:denial-lines ["permission denied"]
                                 :item {:command ["gh" "pr" "comment"]
                                        :cwd "/fixture/repo" :exitCode 403
                                        :id "refusal-item" :status :restricted
                                        :type :github}
                                 :job "job/refused"
                                 :output-sha256 "refusal-output"}
                       :path "src/refused.clj" :slice "slice/refused"
                       :writer-ended? false}}
     :reporting-error {:details {:effect-id "effect/uncertain"
                                 :effect-status :uncertain :uncertain true}}}
    "ready/map-key-only" {:status :ready}
    "done/recorded" {:key "done/recorded" :status :done}}
   :effects
   {"effect/completed"
    {:status :done :message "completed"
     :result {:ordinary-result "must not be projected wholesale"}
     :prior {:status :restricted :message "prior refusal" :restricted true
             :result {:prior-result "retained-prior-result"}}
     :previous {:status :not-applied :outcome :not-applied
                :result {:previous-result "retained-previous-result"}
                :settlement {:outcome :not-applied
                             :receipt-digests {:authorization "recorded-authorization-digest"}
                             :receipt-digest "nested-settlement-receipt"}
                :previous {:status :restricted :message "nested prior restriction"}}
     :reconciliation "reconciliation-receipt"
     :settlement {:outcome :not-applied :receipt-digest "top-settlement-receipt"}}
    "effect/restricted" {:status :restricted :message "still restricted"}
    "effect/uncertain" {:status :uncertain :message "still uncertain"}}
   :effect-history [{:id "effect/completed" :kind :native-effect-history}]
   :refusal-history {"effect/completed" {:message "recorded refusal history"}}
   :settlement-history {"effect/completed" {:outcome :not-applied}}
   :refusals {"effect/completed" {:message "recorded refusal"}}
   :settlements {"effect/completed" {:outcome :not-applied}}
   :operator-api-recoveries
   {"recovery/completed"
    {:kind :operator-api :stage :audit :effect "effect/completed"
     :operation :reconcile :outcome :observed
     :auth-diagnostic {:status :authorized :identity "auth-diagnostic-record"}
     :comment-outcome {:status :observed :identity "comment-outcome-record"}
     :authorization {:status :authorized :identity "authorization-record"}
     :previous-shared-guard {:status :restricted :digest "shared-guard"}
     :previous-local-guard {:status :restricted :digest "local-guard"}
     :shared-retirement {:status :retired :digest "shared-retirement"}
     :inspection {:outcome :observed :digest "inspection-record"}
     :review {:status :accepted :digest "review-record"}
     :prior-effect {:status :restricted :message "prior effect"}
     :receipt-digests {:authorization "authorization-receipt"
                       :inspection "inspection-receipt"
                       :shared-settlement "shared-settlement-receipt"}
     :result {:must-not-be-projected "ordinary recovery result"}}}
   :restrictions {:commands {"gh-pr-merge" {:status :restricted}}}
   :restricted-commands
   {"gh-pr-merge"
    {:denial-lines ["permission denied"]
     :item {:command ["gh" "pr" "merge"] :cwd "/fixture/repo"
            :exitCode 403 :id "command-item" :status :restricted :type :command}
     :job "job/refused" :output-sha256 "command-output"}}
   :jobs
   {"job/model"
    {:status :attention :mode :write :thread "thread-1" :turn "turn-1"
     :turn-terminal? true :active-items ["item-1"]
     :message "recorded job attention" :restricted true
     :tool-refusals [{:job "job/refused"
                      :denial-lines ["permission denied"]
                      :item {:command ["gh" "pr" "comment"]
                             :cwd "/fixture/repo" :exitCode 403
                             :id "refusal-item" :status :restricted
                             :type :github}
                      :output-sha256 "tool-refusal-output"}]
     :model "fixture-model" :prompt-sha256 "prompt-digest"
     :input-sha256 "input-digest" :output-sha256 "output-digest"}}})

(defn status-context []
  (let [ctx (fixture/context)
        config (status-config ctx)]
    {:ctx ctx
     :config config
     :state-file (io/file (:state-dir config) "state.edn")
     :control-file (io/file (:state-dir config) "control.edn")}))

(defn write-bytes! [file bytes]
  (Files/write (.toPath (io/file file)) (byte-array bytes)
               (into-array StandardOpenOption
                           [StandardOpenOption/CREATE StandardOpenOption/TRUNCATE_EXISTING])))

(defn utf8-bytes [value]
  (.getBytes value StandardCharsets/UTF_8))

(defn bytes-digest [bytes]
  (format "%064x"
          (BigInteger. 1 (.digest (MessageDigest/getInstance "SHA-256") bytes))))

(defn content-identity [value]
  {:present true
   :sha256 (rt/digest (pr-str value))
   :serialization :edn-pr-str-utf8})

(defn contains-recorded? [value needle]
  (or (= value needle)
      (cond
        (map? value) (some #(contains-recorded? % needle) value)
        (coll? value) (some #(contains-recorded? % needle) value)
        :else false)))

(defn invalid-utf8-at-marker [value marker]
  (let [index (.indexOf value marker)
        bytes (utf8-bytes value)]
    (when (neg? index)
      (throw (ex-info "fixture marker missing" {:marker marker})))
    (byte-array (concat (take index bytes)
                        [(unchecked-byte 0xC3)]
                        (drop (inc index) bytes)))))

(deftest status-summary-preserves-native-records-identities-and-history
  (let [{:keys [ctx config]} (status-context)
        state (status-state config)
        raw (pr-str state)
        digest (rt/digest raw)
        state-file (str (io/file (:state-dir config) "state.edn"))
        control {:file (str (io/file (:state-dir config) "control.edn"))
                 :sha256 "control-digest" :available true :status :paused :paused true}
        summary (main/status-summary config state digest control)
        tasks (into {} (map (juxt :key identity) (:tasks summary)))
        attention (first (:attention summary))
        current-effects (into {} (map (juxt :id identity) (get-in summary [:effects :current])))
        histories (into {} (map (juxt :id identity) (get-in summary [:effects :history])))
        recorded-identity (status-identity config)]
    (try
      (is (= :seedless/v1 (:schema summary)))
      (is (= :matching (get-in summary [:identity :status])))
      (is (= recorded-identity (get-in summary [:identity :recorded])))
      (is (= (select-keys config status-identity-keys)
             (get-in summary [:identity :configured])))
      (is (= :recorded-only (get-in summary [:identity :git-common-dir])))
      (is (= {:file state-file :sha256 digest :recorded-status :running
              :task-count 3 :counts {:attention 1 :ready 1 :done 1}
              :record {:state-file state-file :state-sha256 digest}}
             (:state summary)))
      (is (= #{"attention/restricted" "ready/map-key-only" "done/recorded"}
             (set (keys tasks))))
      (is (= "ready/map-key-only" (:key (tasks "ready/map-key-only"))))
      (is (= "ready/map-key-only"
             (get-in tasks ["ready/map-key-only" :record :task-key])))
      (is (= state-file (get-in tasks ["done/recorded" :record :state-file])))
      (is (= digest (get-in tasks ["done/recorded" :record :state-sha256])))
      (is (= "attention/restricted" (:key attention)))
      (is (= :attention (:status attention)))
      (is (= "provider refused" (get-in attention [:error :message])))
      (is (= true (get-in attention [:error :restricted])))
      (is (= "job/refused" (get-in attention [:error :job])))
      (is (= "src/refused.clj" (get-in attention [:error :path])))
      (is (= "slice/refused" (get-in attention [:error :slice])))
      (is (= false (get-in attention [:error :writer-ended?])))
      (is (= "refusal-output" (get-in attention [:error :refusal :output-sha256])))
      (is (= "refusal-item" (get-in attention [:error :refusal :item :id])))
      (is (= ["permission denied"]
             (get-in attention [:error :refusal :denial-lines])))
      (is (= ["effect/missing" "effect/restricted" "effect/uncertain"]
             (mapv :id (:effects attention))))
      (is (= true (get-in attention [:history :restricted])))
      (is (= true (get-in attention [:history :uncertain])))
      (is (= {:scope :recorded-model-jobs-only
              :count 1
              :records [{:job "job/model" :status :attention :mode :write
                         :thread "thread-1" :turn "turn-1" :model "fixture-model"
                         :identity {:thread "thread-1" :turn "turn-1" :model "fixture-model"
                                    :prompt-sha256 "prompt-digest"
                                    :input-sha256 "input-digest"
                                    :output-sha256 "output-digest"
                                    :turn-terminal? true
                                    :active-items ["item-1"]
                                    :message "recorded job attention"
                                    :restricted true
                                    :tool-refusals [{:job "job/refused"
                                                     :denial-lines ["permission denied"]
                                                     :item {:command ["gh" "pr" "comment"]
                                                            :cwd "/fixture/repo" :exitCode 403
                                                            :id "refusal-item" :status :restricted
                                                            :type :github}
                                                     :output-sha256 "tool-refusal-output"}]}
                         :turn-terminal? true :active-items ["item-1"]
                         :message "recorded job attention" :restricted true
                         :tool-refusals [{:job "job/refused"
                                          :denial-lines ["permission denied"]
                                          :item {:command ["gh" "pr" "comment"]
                                                 :cwd "/fixture/repo" :exitCode 403
                                                 :id "refusal-item" :status :restricted
                                                 :type :github}
                                          :output-sha256 "tool-refusal-output"}]
                         :record {:state-file state-file :state-sha256 digest
                                  :job-key "job/model"}}]}
             (:jobs summary)))
      (is (= :recorded-effects-only (get-in summary [:effects :scope])))
      (is (= #{"effect/completed" "effect/restricted" "effect/uncertain"}
             (set (keys current-effects))))
      (is (= :done (get-in current-effects ["effect/completed" :status])))
      (is (= true (get-in current-effects ["effect/completed" :result-present])))
      (is (= true (get-in current-effects ["effect/completed" :reconciliation-present])))
      (is (= (content-identity (get-in state [:effects "effect/completed" :result]))
             (get-in current-effects ["effect/completed" :result-identity])))
      (is (= (content-identity (get-in state [:effects "effect/completed" :reconciliation]))
             (get-in current-effects ["effect/completed" :reconciliation-identity])))
      (is (not (contains? (current-effects "effect/completed") :result)))
      (is (= :done (get-in histories ["effect/completed" :current-status])))
      (is (= (content-identity (get-in state [:effects "effect/completed" :prior :result]))
             (get-in histories ["effect/completed" :prior :result])))
      (is (= (content-identity (get-in state [:effects "effect/completed" :previous :result]))
             (get-in histories ["effect/completed" :previous :result])))
      (is (= (content-identity (get-in state [:effects "effect/completed" :reconciliation]))
             (get-in histories ["effect/completed" :reconciliation-identity])))
      (is (= "recorded-authorization-digest"
             (get-in histories ["effect/completed" :previous :settlement
                                :receipt-digests :authorization])))
      (is (= :restricted (get-in histories ["effect/completed" :prior-status])))
      (is (= "prior refusal"
             (get-in histories ["effect/completed" :prior :message])))
      (is (= :not-applied (get-in histories ["effect/completed" :previous-status])))
      (is (contains-recorded? (histories "effect/completed") "reconciliation-receipt"))
      (is (contains-recorded? (histories "effect/completed") "nested-settlement-receipt"))
      (is (contains-recorded? (histories "effect/completed") "top-settlement-receipt"))
      (is (contains-recorded? (histories "effect/completed") "nested prior restriction"))
      (is (= (:refusal-history state)
             (get-in summary [:effects :refusal-settlement-history :recorded :refusal-history])))
      (is (= (:settlement-history state)
             (get-in summary [:effects :refusal-settlement-history :recorded :settlement-history])))
      (is (= "recovery/completed"
             (get-in summary [:effects :operator-api-recoveries 0 :id])))
      (is (= {:status :authorized :identity "authorization-record"}
             (get-in summary [:effects :operator-api-recoveries 0 :authorization])))
      (is (= {:status :authorized :identity "auth-diagnostic-record"}
             (get-in summary [:effects :operator-api-recoveries 0 :auth-diagnostic])))
      (is (= {:status :observed :identity "comment-outcome-record"}
             (get-in summary [:effects :operator-api-recoveries 0 :comment-outcome])))
      (is (= {:status :restricted :digest "shared-guard"}
             (get-in summary [:effects :operator-api-recoveries 0 :previous-shared-guard])))
      (is (= {:status :restricted :digest "local-guard"}
             (get-in summary [:effects :operator-api-recoveries 0 :previous-local-guard])))
      (is (= {:status :retired :digest "shared-retirement"}
             (get-in summary [:effects :operator-api-recoveries 0 :shared-retirement])))
      (is (= {:outcome :observed :digest "inspection-record"}
             (get-in summary [:effects :operator-api-recoveries 0 :inspection])))
      (is (= {:status :accepted :digest "review-record"}
             (get-in summary [:effects :operator-api-recoveries 0 :review])))
      (is (= {:status :restricted :message "prior effect"}
             (get-in summary [:effects :operator-api-recoveries 0 :prior-effect])))
      (is (= {:authorization "authorization-receipt"
              :inspection "inspection-receipt"
              :shared-settlement "shared-settlement-receipt"}
             (get-in summary [:effects :operator-api-recoveries 0 :receipt-digests])))
      (is (not (contains? (first (get-in summary [:effects :operator-api-recoveries]))
                          :result)))
      (is (= state-file
             (get-in summary [:effects :operator-api-recoveries 0 :record :state-file])))
      (is (= (select-keys state [:restrictions :restricted-commands])
             (get-in summary [:operator-controls :native-restrictions])))
      (is (= control (get-in summary [:operator-controls :local-control])))
      (is (= :unavailable (get-in summary [:operator-controls :github :status])))
      (is (= :unverified (get-in summary [:liveness :processes])))
      (is (= :unverified (get-in summary [:liveness :service])))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest status-summary-redacts-credential-scalars-without-dropping-identities
  (let [{:keys [ctx config]} (status-context)
        state (-> (status-state config)
                  (assoc-in [:tasks "attention/restricted" :error]
                            "Bearer scalar-error-secret")
                  (assoc-in [:tasks "attention/restricted" :reporting-error]
                            {:message "Bearer reporting-secret"
                             :remote-pr-refusal {:authorization "credential-key-secret"
                                                 :detail "Bearer nested-secret"}})
                  (assoc-in [:effects "effect/completed" :reconciliation]
                            "Bearer reconciliation-secret")
                  (assoc :completion {:status :blocked
                                      :result "Bearer blocker-secret"
                                      :authorization "credential-key-secret"}))
        raw (pr-str state)
        summary (main/status-summary config state (rt/digest raw)
                                     {:file "control.edn" :available false
                                      :status :unavailable})
        attention (first (:attention summary))
        current-effects (into {} (map (juxt :id identity) (get-in summary [:effects :current])))
        histories (into {} (map (juxt :id identity) (get-in summary [:effects :history])))
        serialized (pr-str summary)]
    (try
      (is (= "Bearer [REDACTED]" (get-in attention [:error :message])))
      (is (= "Bearer [REDACTED]"
             (get-in attention [:reporting-error :message])))
      (is (= "[REDACTED]"
             (get-in attention [:reporting-error :remote-pr-refusal :authorization])))
      (is (= "Bearer [REDACTED]"
             (get-in attention [:reporting-error :remote-pr-refusal :detail])))
      (is (= "Bearer [REDACTED]"
             (get-in histories ["effect/completed" :reconciliation])))
      (is (= (content-identity "Bearer reconciliation-secret")
             (get-in current-effects ["effect/completed" :reconciliation-identity])))
      (is (= (content-identity "Bearer reconciliation-secret")
             (get-in histories ["effect/completed" :reconciliation-identity])))
      (is (= "Bearer [REDACTED]"
             (get-in summary [:recorded-blockers :completion :result])))
      (doseq [secret ["scalar-error-secret" "reporting-secret" "credential-key-secret"
                      "nested-secret" "reconciliation-secret" "blocker-secret"]]
        (is (not (str/includes? serialized secret))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest status-summary-reader-fails-closed-and-binds-exact-bytes
  (let [{:keys [ctx config state-file control-file]} (status-context)
        state (status-state config)
        state-raw (pr-str (assoc state :note "UTF8-\u00e9"))
        state-bytes (utf8-bytes state-raw)]
    (try
      (write-bytes! state-file state-bytes)
      (let [control-raw (pr-str {:paused true})
            control-bytes (utf8-bytes control-raw)]
        (write-bytes! control-file control-bytes)
        (let [summary (main/status-summary! config)]
          (is (= (bytes-digest state-bytes) (get-in summary [:state :sha256])))
          (is (= (bytes-digest control-bytes)
                 (get-in summary [:operator-controls :local-control :sha256])))
          (is (= :paused
                 (get-in summary [:operator-controls :local-control :status])))
          (is (= (vec state-bytes)
                 (vec (Files/readAllBytes (.toPath state-file)))))
          (is (= (vec control-bytes)
                 (vec (Files/readAllBytes (.toPath control-file)))))))
      (doseq [bad-state [(assoc state :schema :wrong-schema)
                         (assoc state :tasks [])
                         (assoc state :jobs {"bad" []})
                         (assoc state :effects {"bad" "not-a-record"})
                         (assoc state :tasks {"bad/map-key" {:key "wrong-embedded-key"
                                                             :status :ready}})
                         (assoc state :identity (assoc (status-identity config)
                                                       :campaign "other-campaign"))]]
        (spit state-file (pr-str bad-state))
        (is (thrown-with-msg? Exception
                              (if (= :wrong-schema (:schema bad-state))
                                #"malformed state shape"
                                (if (= "other-campaign" (get-in bad-state [:identity :campaign]))
                                  #"another campaign/configuration"
                                  #"malformed state shape"))
                              (main/status-summary! config))))
      (doseq [bad-state [(assoc state :status nil)
                         (assoc state :status "running")
                         (update-in state [:tasks "ready/map-key-only"] dissoc :status)
                         (assoc-in state [:tasks "ready/map-key-only" :status] "ready")
                         (update-in state [:jobs "job/model"] dissoc :status)
                         (assoc-in state [:jobs "job/model" :status] "attention")
                         (update-in state [:effects "effect/completed"] dissoc :status)
                         (assoc-in state [:effects "effect/completed" :status] "done")]]
        (spit state-file (pr-str bad-state))
        (is (thrown-with-msg? Exception #"malformed state shape"
                              (main/status-summary! config))))
      (spit state-file (str state-raw "\n" state-raw))
      (is (thrown-with-msg? Exception #"malformed state"
                            (main/status-summary! config)))
      (spit state-file "#factory/unsupported {:status :running}")
      (is (thrown-with-msg? Exception #"malformed state"
                            (main/status-summary! config)))
      (write-bytes! state-file (invalid-utf8-at-marker
                                (pr-str (assoc state :note "INVALID_UTF8_MARKER"))
                                "INVALID_UTF8_MARKER"))
      (is (thrown-with-msg? Exception #"cannot read state file"
                            (main/status-summary! config)))
      (write-bytes! state-file state-bytes)
      (Files/deleteIfExists (.toPath control-file))
      (is (= :unavailable
             (get-in (main/status-summary! config)
                     [:operator-controls :local-control :status])))
      (doseq [bad-control ["{:paused \"true\"}"
                           "{}"
                           "{:paused false}\n{:paused true}"]]
        (spit control-file bad-control)
        (is (thrown-with-msg? Exception #"malformed control"
                              (main/status-summary! config))))
      (write-bytes! control-file
                    (invalid-utf8-at-marker "{:paused false :note \"INVALID_UTF8_MARKER\"}"
                                            "INVALID_UTF8_MARKER"))
      (is (thrown-with-msg? Exception #"cannot read control file"
                            (main/status-summary! config)))
      (Files/deleteIfExists (.toPath control-file))
      (Files/createDirectory (.toPath control-file)
                             (make-array java.nio.file.attribute.FileAttribute 0))
      (is (thrown-with-msg? Exception #"malformed control file"
                            (main/status-summary! config)))
      (Files/deleteIfExists (.toPath state-file))
      (is (thrown-with-msg? Exception #"existing state file"
                            (main/status-summary! config)))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest status-summary-cli-is-early-read-only-and-raw-status-is-unchanged
  (let [{:keys [ctx config state-file]} (status-context)
        state (status-state config)
        raw (pr-str state)
        config-file (io/file (:root ctx) "config.edn")
        forbidden (fn [& _]
                    (throw (AssertionError. "status-summary invoked a forbidden effect")))]
    (try
      (spit config-file "{}")
      (spit state-file raw)
      (with-redefs [main/config! (fn [_] config)
                    main/context! forbidden
                    main/target! forbidden
                    main/probe! forbidden
                    main/owned-cleanup! forbidden
                    gh/api forbidden
                    gh/graphql forbidden
                    gh/pages forbidden
                    gh/item! forbidden
                    gh/status! forbidden
                    gh/relation! forbidden
                    gh/setup! forbidden
                    gh/issue! forbidden
                    gh/comment! forbidden
                    gh/milestone! forbidden
                    gh/open-review-threads? forbidden
                    rt/command forbidden
                    rt/run! forbidden
                    rt/coordination forbidden
                    rt/with-lock forbidden
                    rt/import-state-coordination! forbidden
                    rt/save! forbidden
                    rt/update! forbidden
                    rt/write-edn! forbidden
                    rt/event! forbidden
                    clojure.core/shutdown-agents (fn [] nil)]
        (let [output (with-out-str (main/-main "status-summary" (str config-file)))
              parsed (edn/read-string output)]
          (is (= :seedless/v1 (:schema parsed)))
          (is (= :running (get-in parsed [:state :recorded-status])))
          (is (= (rt/digest raw) (get-in parsed [:state :sha256])))))
      (let [output (java.io.StringWriter.)
            failure (try
                      (binding [*out* output]
                        (with-redefs [main/config! (fn [_] config)
                                      rt/fail! (fn [message _] (throw (Error. message)))
                                      clojure.core/shutdown-agents (fn [] nil)]
                          (main/-main "status-summary" (str config-file) "unexpected")))
                      nil
                      (catch Error error error))]
        (is (= "status-summary takes no additional arguments" (.getMessage failure)))
        (is (= "" (str output))))
      (let [output (with-out-str (main/dispatch! {:state (atom state)} "status" []))]
        (is (= state (edn/read-string output))))
      (finally (fixture/delete-tree! (:root ctx))))))
