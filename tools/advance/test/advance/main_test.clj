(ns advance.main-test
  (:require [advance.main :as main]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [simplify.main :as base]
            [simplify.runtime :as rt])
  (:import [java.nio.file Files]))

(defn temp-dir []
  (str (Files/createTempDirectory "uniform-advance-status-test-"
                                  (make-array java.nio.file.attribute.FileAttribute 0))))

(defn config [dir]
  {:repo-dir dir
   :state-dir dir
   :repository "fixture/repo"
   :campaign "Fixture campaign"})

(defn state-fixture []
  {:schema 1
   :repository "fixture/repo"
   :campaign "Fixture campaign"
   :status :running
   :round 7
   :catalog {:base "base-commit" :tree "tree-id" :unrelated "omitted"}
   :tasks
   {"attention/restricted"
    {:key "attention/restricted" :status :attention :resume-status :ci
     :error {:details {:effect "effect/restricted"
                       :effect-status :restricted
                       :restricted true
                       :message "provider refused"}}
     :paths ["src/restricted.clj"]}
    "attention/uncertain"
    {:key "attention/uncertain" :status :attention :resume-status :repair
     :error {:details {:effect "effect/uncertain"
                       :effect-status :uncertain
                       :message "response is held"}}
     :paths ["src/uncertain.clj"]}
    "attention/pending-and-missing"
    {:key "attention/pending-and-missing" :status :attention
     :error {:details {:effect ["effect/pending" "effect/missing"]}}
     :paths ["src/pending.clj"]}
    "ready/item"
    {:key "ready/item" :status :ready :paths ["src/ready.clj"]}}
   :effects
   {"effect/restricted" {:status :restricted :request "held"}
    "effect/uncertain" {:status :uncertain :request "held"}
    "effect/pending" {:status :pending :request "in-flight"}}
   :jobs
   {"job/one" {:status :running :mode :read :thread "thread-1" :turn "turn-1"}}
   :operator-planning [{:job "old" :status :rejected :restriction "history"}]})

(deftest status-summary-is-compact-observational-and-lossless-for-attention
  (let [dir (temp-dir)
        cfg (config dir)
        state (state-fixture)
        digest (rt/digest (pr-str state))
        control {:file (str (io/file dir "control.edn"))
                 :available true :status :paused :paused true}
        summary (main/status-summary cfg state digest control)
        attention (:attention summary)
        by-key (into {} (map (juxt :key identity) attention))]
    (is (= :running (get-in summary [:state :recorded-status])))
    (is (= {:base "base-commit" :tree "tree-id"}
           (get-in summary [:state :catalog])))
    (is (= 4 (get-in summary [:state :task-count])))
    (is (= {:attention 3 :ready 1} (get-in summary [:state :counts])))
    (is (= #{"attention/restricted" "attention/uncertain" "attention/pending-and-missing"}
           (set (keys by-key))))
    (is (= :ci (get-in by-key ["attention/restricted" :resume-status])))
    (is (= :repair (get-in by-key ["attention/uncertain" :resume-status])))
    (is (= {:restricted true :uncertain false :pending false}
           (get-in by-key ["attention/restricted" :history])))
    (is (= {:restricted false :uncertain true :pending false}
           (get-in by-key ["attention/uncertain" :history])))
    (is (= {:restricted false :uncertain false :pending true}
           (get-in by-key ["attention/pending-and-missing" :history])))
    (is (= [{:id "effect/pending" :status :pending
             :restricted false :uncertain false :pending true}
            {:id "effect/missing" :status :missing
             :restricted false :uncertain false :pending false}]
           (get-in by-key ["attention/pending-and-missing" :effects])))
    (is (= {:id "effect/restricted" :status :restricted
            :restricted true :uncertain false :pending false}
           (first (get-in by-key ["attention/restricted" :effects]))))
    (is (= {:scope :recorded-model-jobs-only
            :count 1 :records [{:job "job/one" :status :running
                                :mode :read :thread "thread-1" :turn "turn-1"
                                :record {:state-file (str (io/file dir "state.edn"))
                                         :state-sha256 digest :job-key "job/one"}}]}
           (:jobs summary)))
    (is (= control (:local-control (:operator-controls summary))))
    (is (= :unavailable (get-in summary [:operator-controls :github :status])))
    (is (= :unverified (get-in summary [:liveness :processes])))
    (is (= :unverified (get-in summary [:liveness :service])))
    (is (= digest (get-in by-key ["attention/restricted" :record :state-sha256])))
    (is (= digest (get-in summary [:state :sha256])))
    (is (not (contains? (:state summary) :operator-planning))
        "retained history is represented by pointers and attention facts, not copied wholesale")))

(deftest status-summary-shows-non-attention-evidence-with-explicit-identity-status
  (let [dir (temp-dir)
        cfg (config dir)
        evidence-tasks
        {"ci/early"
         {:key "ci/early" :status :ci :base "base-ci" :candidate "candidate-ci" :tree "tree-ci"
          :checks nil :review nil}
         "check/failed"
         {:key "check/failed" :status :repair :base "base-failed" :candidate "candidate-failed" :tree "tree-failed"
          :checks {:candidate "candidate-failed" :tree "tree-failed" :result :failed :commands [["check"]]}
          :review {:base "base-failed" :candidate "candidate-failed" :tree "tree-failed"
                   :verdict "changes_requested"}}
         "check/unknown"
         {:key "check/unknown" :status :ready :base "base-unknown" :candidate "candidate-unknown" :tree "tree-unknown"
          :checks {:candidate "candidate-unknown" :tree "tree-unknown" :result :unknown}
          :review {:base "base-unknown" :candidate "candidate-unknown" :tree "tree-unknown"
                   :verdict "commented"}}
         "check/passed"
         {:key "check/passed" :status :ready :base "base-passed" :candidate "candidate-passed" :tree "tree-passed"
          :checks {:candidate "candidate-passed" :tree "tree-passed" :result :passed}
          :review {:base "base-passed" :candidate "candidate-passed" :tree "tree-passed"
                   :verdict "approved"}}
         "check/stale"
         {:key "check/stale" :status :repair :base "base-current" :candidate "candidate-current" :tree "tree-current"
          :checks {:candidate "candidate-old" :tree "tree-old" :result :passed}
          :review {:base "base-current" :candidate "candidate-old" :tree "tree-old"
                   :verdict "approved"}}
         "check/incomplete-evidence"
         {:key "check/incomplete-evidence" :status :ready :base "base-incomplete" :candidate "candidate-incomplete"
          :tree "tree-incomplete"
          :checks {:candidate nil :tree "tree-incomplete" :result :passed}
          :review {:base " " :candidate "candidate-incomplete" :tree "tree-incomplete"
                   :verdict "approved"}}
         "check/incomplete-task"
         {:key "check/incomplete-task" :status :ready :base "base-task" :candidate "" :tree "tree-task"
          :checks {:candidate "candidate-task" :tree "tree-task" :result :failed}
          :review {:base "base-task" :candidate "candidate-task" :tree "tree-task"
                   :verdict "changes_requested"}}
         "check/both-blank"
         {:key "check/both-blank" :status :ready :base "" :candidate "" :tree ""
          :checks {:candidate "" :tree "" :result :failed}
          :review {:base "" :candidate "" :tree "" :verdict "commented"}}
         "review/nil-base"
         {:key "review/nil-base" :status :ready :base nil :candidate "candidate-nil-base" :tree "tree-nil-base"
          :checks {:candidate "candidate-nil-base" :tree "tree-nil-base" :result :unknown}
          :review {:base "base-recorded" :candidate "candidate-nil-base" :tree "tree-nil-base"
                   :verdict "approved"}}}
        state (assoc (update (state-fixture) :tasks merge evidence-tasks) :jobs {})
        digest (rt/digest (pr-str state))
        control {:file (str (io/file dir "control.edn"))
                 :available true :status :active :paused false}
        summary (main/status-summary cfg state digest control)
        by-key (into {} (map (juxt :key identity) (:non-attention summary)))
        state-file (str (io/file dir "state.edn"))]
    (is (= :recorded-model-jobs-only (get-in summary [:jobs :scope])))
    (is (= {:scope :recorded-model-jobs-only :count 0 :records []}
           (:jobs summary)))
    (is (= (set (conj (set (keys evidence-tasks)) "ready/item"))
           (set (keys by-key))))
    (doseq [[key task] (merge {"ready/item" (get-in state [:tasks "ready/item"])} evidence-tasks)
            :let [entry (by-key key)]]
      (is (= (merge {:base nil :candidate nil :tree nil}
                    (select-keys task [:key :status :base :candidate :tree]))
             (select-keys entry [:key :status :base :candidate :tree])))
      (is (= {:state-file state-file :state-sha256 digest :task-key key}
             (:record entry))))
    (is (= {:identity-status :missing :identity {} :recorded-result nil}
           (:checks (by-key "ci/early"))))
    (is (= {:identity-status :missing :identity {} :recorded-verdict nil}
           (:review (by-key "ci/early"))))
    (doseq [[key result verdict]
            [["check/failed" :failed "changes_requested"]
             ["check/unknown" :unknown "commented"]
             ["check/passed" :passed "approved"]]]
      (is (= :matching (get-in (by-key key) [:checks :identity-status])))
      (is (= result (get-in (by-key key) [:checks :recorded-result])))
      (is (= :matching (get-in (by-key key) [:review :identity-status])))
      (is (= verdict (get-in (by-key key) [:review :recorded-verdict]))))
    (is (= {:candidate "candidate-failed" :tree "tree-failed"}
           (get-in (by-key "check/failed") [:checks :identity])))
    (is (= {:base "base-failed" :candidate "candidate-failed" :tree "tree-failed"}
           (get-in (by-key "check/failed") [:review :identity])))
    (is (= :stale (get-in (by-key "check/stale") [:checks :identity-status])))
    (is (= :stale (get-in (by-key "check/stale") [:review :identity-status])))
    (is (= :passed (get-in (by-key "check/stale") [:checks :recorded-result])))
    (is (= "candidate-old" (get-in (by-key "check/stale") [:checks :identity :candidate])))
    (is (= "candidate-old" (get-in (by-key "check/stale") [:review :identity :candidate])))
    (doseq [key ["check/incomplete-evidence" "check/incomplete-task" "check/both-blank"]]
      (is (= :stale (get-in (by-key key) [:checks :identity-status])) key)
      (is (= :stale (get-in (by-key key) [:review :identity-status])) key))
    (is (= :matching (get-in (by-key "review/nil-base") [:checks :identity-status])))
    (is (= :stale (get-in (by-key "review/nil-base") [:review :identity-status])))
    (is (nil? (get-in (by-key "check/incomplete-evidence") [:checks :identity :candidate])))
    (is (= " " (get-in (by-key "check/incomplete-evidence") [:review :identity :base])))
    (is (= "candidate-task" (get-in (by-key "check/incomplete-task") [:checks :identity :candidate])))
    (is (= "base-recorded" (get-in (by-key "review/nil-base") [:review :identity :base])))))

(deftest status-summary-redacts-credentials-and-is-deterministic
  (let [dir (temp-dir)
        cfg (config dir)
        secret (str "ghp_" (apply str (repeat 40 "A")))
        state (assoc-in (state-fixture)
                        [:tasks "attention/restricted" :error :details :message]
                        (str "refusal " secret))
        digest (rt/digest (pr-str state))
        control {:file (str (io/file dir "control.edn"))
                 :available true :status :active :paused false}
        first-summary (main/status-summary cfg state digest control)
        second-summary (main/status-summary cfg state digest control)
        rendered (pr-str first-summary)]
    (is (= first-summary second-summary))
    (is (not (.contains rendered secret)))
    (is (.contains rendered "[REDACTED]"))
    (is (= :active (get-in first-summary [:operator-controls :local-control :status])))))

(deftest status-summary-file-reader-distinguishes-control-and-state-failures
  (let [dir (temp-dir)
        cfg (config dir)
        state-file (io/file dir "state.edn")
        control-file (io/file dir "control.edn")
        state (state-fixture)]
    (spit state-file (pr-str state))
    (let [before-state (slurp state-file)
          summary (main/status-summary! cfg)]
      (is (= :unavailable
             (get-in summary [:operator-controls :local-control :status])))
      (is (= before-state (slurp state-file)))
      (spit control-file (pr-str {:paused false}))
      (let [before-control (slurp control-file)
            active (main/status-summary! cfg)]
        (is (= :active
               (get-in active [:operator-controls :local-control :status])))
        (is (= before-control (slurp control-file))))
      (spit control-file (pr-str {:paused true}))
      (is (= :paused
             (get-in (main/status-summary! cfg)
                     [:operator-controls :local-control :status])))
      (doseq [value [{:paused "true"} {}]]
        (spit control-file (pr-str value))
        (is (= :malformed
               (get-in (main/status-summary! cfg)
                       [:operator-controls :local-control :status])))))
    (doseq [[contents pattern]
            [[nil #"requires an existing state file"]
             ["{:malformed" #"found malformed state"]
             [(pr-str (assoc state :repository "other/repo"))
              #"found state for another campaign"]]]
      (if (nil? contents)
        (Files/deleteIfExists (.toPath state-file))
        (spit state-file contents))
      (is (thrown-with-msg? Exception pattern (main/status-summary! cfg))))))

(deftest status-summary-does-not-call-provider-process-or-coordinator
  (let [dir (temp-dir)
        cfg (config dir)
        state-file (io/file dir "state.edn")
        control-file (io/file dir "control.edn")
        before-state (pr-str (state-fixture))
        before-control (pr-str {:paused false})]
    (spit state-file before-state)
    (spit control-file before-control)
    (with-redefs [rt/command (fn [& _] (throw (AssertionError. "status summary must not probe processes")))
                  rt/run! (fn [& _] (throw (AssertionError. "status summary must not execute commands")))]
      (is (= :active
             (get-in (main/status-summary! cfg)
                     [:operator-controls :local-control :status]))))
    (is (= before-state (slurp state-file)))
    (is (= before-control (slurp control-file)))))

(deftest status-summary-rejects-invalid-state-carriers
  (let [dir (temp-dir)
        cfg (config dir)
        state-file (io/file dir "state.edn")
        valid (state-fixture)]
    (doseq [state [(assoc valid :tasks nil)
                   (assoc valid :jobs [])
                   (assoc valid :effects nil)
                   (assoc valid :tasks {"malformed" nil})
                   (assoc valid :jobs {"malformed" []})
                   (assoc valid :effects {"malformed" "not a record"})]]
      (spit state-file (pr-str state))
      (is (thrown-with-msg? Exception #"malformed state shape"
                            (main/status-summary! cfg))))))

(deftest status-summary-binds-digest-and-output-to-one-state-read
  (let [dir (temp-dir)
        cfg (config dir)
        state-file (io/file dir "state.edn")
        original (pr-str (state-fixture))
        changed (pr-str (assoc (state-fixture) :status :changed-after-read))
        calls (atom [])
        original-slurp clojure.core/slurp]
    (spit state-file original)
    (with-redefs [clojure.core/slurp
                  (fn [source & options]
                    (let [value (apply original-slurp source options)]
                      (when (= (.getCanonicalPath (io/file source))
                               (.getCanonicalPath state-file))
                        (swap! calls conj source)
                        (spit state-file changed))
                      value))]
      (let [summary (main/status-summary! cfg)]
        (is (= 1 (count @calls)))
        (is (= (rt/digest original) (get-in summary [:state :sha256])))
        (is (= :running (get-in summary [:state :recorded-status])))
        (is (= (rt/digest original)
               (get-in (first (:attention summary)) [:record :state-sha256])))))))

(deftest status-summary-cli-route-is-read-only-and-keeps-raw-status-delegation
  (let [dir (temp-dir)
        cfg (config dir)
        config-path (str (io/file dir "config.edn"))
        state-file (io/file dir "state.edn")
        raw-state (pr-str (state-fixture))
        forbidden (fn [& _] (throw (AssertionError. "status-summary invoked a mutable/provider path")))]
    (spit state-file raw-state)
    (spit config-path "{}")
    (with-redefs [base/config! (fn [_ _] cfg)
                  base/context! forbidden
                  rt/event! forbidden
                  rt/write-edn! forbidden
                  rt/import-state-coordination! forbidden
                  clojure.core/shutdown-agents (fn [] nil)]
      (let [output (with-out-str (main/-main "status-summary" config-path))
            parsed (edn/read-string output)]
        (is (= :running (get-in parsed [:state :recorded-status])))
        (is (= (rt/digest raw-state) (get-in parsed [:state :sha256])))))
    (with-redefs [base/dispatch! (fn [_ctx command args]
                                   (is (= "status" command))
                                   (is (= [] args))
                                   :raw-status-delegated)]
      (is (= :raw-status-delegated
             (main/dispatch! {:state (atom {})} "status" []))))))
