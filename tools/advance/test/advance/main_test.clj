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
    (is (= {:count 1 :records [{:job "job/one" :status :running
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
