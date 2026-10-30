(ns gravity.syntax.selected-revision-parent-test
  "C3-only fake ports. No actual broker, lock, runner, timer or child is opened."
  (:require [clojure.test :refer [deftest is]]
            [clojure.java.io :as io]
            [gravity.syntax.selected-revision-request :as request]
            [gravity.syntax.selected-revision-parent :as parent]))

(def packet
  {:guard-proposal {:coordination-root parent/C-root :canonical-lock parent/L-path
                    :resource-class :exclusive :lock-mode :immediate-try-lock
                    :participants parent/C3-participants
                    :acquire-order [:C :L] :release-order [:L :C]}
   :parent-profile parent/profile :argv ["fixture-java" "fixture-C3"] :cwd "/fixture"})

(defn fixture []
  (let [events (atom []) clock (atom 0) state (atom {})
        C (Object.) L (Object.) process (Object.) owned (Object.) watchdog (Object.)
        terminal (atom nil) physical-finalizations (atom 0)
        finalize (fn [token]
                   (is (identical? owned token)) (swap! events conj :finalize)
                   (or @terminal (do (swap! physical-finalizations inc)
                                     (reset! terminal {:tracked-complete? true :complete? nil}))))
        ports {:now #(deref clock)
               :arm (fn [remaining] (is (fn? remaining)) (is (= 300000 (remaining)))
                      (swap! events conj :arm) watchdog)
               :check-watchdog (fn [token] (is (identical? watchdog token)))
               :disarm (fn [token] (is (identical? watchdog token)) (swap! events conj :disarm))
               :recheck (fn [request git-ports records]
                          (is (= packet request)) (is (fn? (:remaining-ms git-ports)))
                          (is (instance? clojure.lang.Atom records))
                          (swap! events conj :recheck))
               :acquire-C (fn [options kind]
                            (is (= {:coordination-root parent/C-root :timeout-ms 60000} options))
                            (is (= :exclusive kind)) (swap! events conj :C) C)
               :validate-C (fn [token root kind]
                             (is (identical? C token)) (is (= parent/C-root root))
                             (is (= :exclusive kind)) token)
               :release-C (fn [token] (is (identical? C token)) (swap! events conj :release-C)
                            {:outcome :released})
               :try-L (fn [path] (is (= parent/L-path path)) (swap! events conj :L) L)
               :release-L (fn [token] (is (identical? L token)) (swap! events conj :release-L)
                            {:released? true})
               :launch (fn [argv cwd]
                         (is (= (:argv packet) argv)) (is (= (:cwd packet) cwd))
                         (swap! events conj :launch) process)
               :retain (fn [p] (is (identical? process p)) (swap! events conj :retain) owned)
               :handles (fn [token] (is (identical? owned token)) [process])
               :wait (fn [p options token]
                       (is (identical? process p)) (is (identical? owned token))
                       (is (<= 1 (:timeout-ms options) 120000))
                       (is (<= 1 (:capture-wait-ms options) 5000))
                       (swap! events conj :wait)
                       (finalize token)
                       {:status :passed :exit-code 0 :wait-interrupted? false
                        :output-truncated? false :capture-failed? false :cleanup-failed? false})
               :finalize finalize
               :creation-coverage (fn [raw]
                                    (is (identical? process (:process raw)))
                                    (is (identical? owned (:owned raw)))
                                    ;; This is injected control data, never native evidence.
                                    {:owned-complete? true :global-descendant-coverage :unknown})}]
    {:ports ports :events events :clock clock :state state :C C :L L :process process :owned owned
     :watchdog watchdog :physical-finalizations physical-finalizations}))

(deftest parent-order-same-tokens-cached-finalization-and-reverse-release
  (let [{:keys [ports events state process owned physical-finalizations]} (fixture)
        result (parent/run! packet ports state)]
    (is (= [:arm :C :L :recheck :launch :retain :wait :finalize :finalize :recheck
            :release-L :release-C :disarm] @events))
    (is (= :passed (:status result)))
    (is (identical? process (:process result))) (is (identical? owned (:owned result)))
    (is (= 1 @physical-finalizations))
    (is (nil? (get-in result [:finalization :complete?])))
    (is (= :unknown (:global-descendant-coverage result)))))

(deftest prelaunch-refusals-and-unknown-ownership-retain-exact-state
  (doseq [phase [:acquire-C :try-L :launch :retain]]
    (let [{:keys [ports events state process]} (fixture) original (ex-info "injected" {:phase phase})
          result (parent/run! packet (assoc ports phase (fn [& _] (throw original))) state)]
      (is (identical? original (:error result))) (is (= :failed (:status result)))
      (if (#{:acquire-C :try-L} phase)
        (do (is (not-any? #{:launch :retain :wait} @events))
            (is (true? (:watchdog-disarmed? result))))
        (do (is (not-any? #{:release-L :release-C :disarm} @events))
            (is (false? (:release-authorized? result)))
            (when (= :retain phase) (is (identical? process (:process result))))))))
  (let [{:keys [ports events state]} (fixture)
        result (parent/run! packet (assoc ports :try-L (constantly nil)) state)]
    (is (= :C3-L-refused (:reason (ex-data (:error result)))))
    (is (= [:arm :C :release-C :disarm] @events))))

(deftest cleanup-interruption-fatal-errors-and-unknown-coverage
  (let [{:keys [ports state]} (fixture) original (InterruptedException. "wait")]
    (try
      (let [result (parent/run! packet (assoc ports :wait (fn [& _] (throw original))) state)
            interrupted (.isInterrupted (Thread/currentThread))]
        (is (identical? original (:error result))) (is interrupted)
        (is (= :released (:C-release result))))
      (finally (Thread/interrupted))))
  (doseq [cleanup [(ex-info "cleanup" {}) (LinkageError. "fatal cleanup")]]
    (let [{:keys [ports state process owned events]} (fixture) original (ex-info "wait" {})
          result (try (parent/run! packet (assoc ports :wait (fn [& _] (throw original))
                                             :finalize (fn [_] (throw cleanup))) state)
                      (catch LinkageError error error))]
      (is (identical? process (:process @state))) (is (identical? owned (:owned @state)))
      (is (not-any? #{:release-L :release-C :disarm} @events))
      (if (instance? LinkageError cleanup)
        (do (is (identical? cleanup result))
            (is (some #(identical? original %) (.getSuppressed ^Throwable result))))
        (do (is (identical? original (:error result)))
            (is (some #(identical? cleanup %) (.getSuppressed ^Throwable (:error result))))))))
  (let [{:keys [ports state events]} (fixture)
        result (parent/run! packet (assoc ports :creation-coverage (constantly {:owned-complete? false})) state)]
    (is (= :C3-owned-terminal-or-capture-unproven (:reason (ex-data (:error result)))))
    (is (not-any? #{:release-L :release-C :disarm} @events))))

(deftest one-deadline-is-recomputed-after-startup-and-release-errors-are-not-retried
  (let [{:keys [ports state clock]} (fixture) wait (:wait ports) observed (atom nil)]
    (parent/run! packet
      (assoc ports :launch (fn [& args] (reset! clock 240000) (apply (:launch ports) args))
                   :wait (fn [p options token] (reset! observed options) (wait p options token))) state)
    (is (= 48000 (:timeout-ms @observed)))
    (is (= 5000 (:capture-wait-ms @observed))))
  (doseq [phase [:release-L :release-C]]
    (let [{:keys [ports state events]} (fixture) calls (atom 0)
          result (parent/run! packet (assoc ports phase (fn [_] (swap! calls inc) nil)) state)]
      (is (= 1 @calls)) (is (= :failed (:status result)))
      (is (not-any? #{:disarm} @events))
      (when (= phase :release-L) (is (not-any? #{:release-C} @events)))))
  (let [{:keys [ports state clock]} (fixture)
        result (parent/run! packet
                 (assoc ports :release-C (fn [lease]
                                          (let [result ((:release-C ports) lease)]
                                            (reset! clock 300000) result))) state)]
    (is (= :failed (:status result)))
    (is (= :C3-whole-deadline (:reason (ex-data (:error result)))))
    (is (true? (:watchdog-disarmed? result)))))

(deftest unknown-bookend-capture-keeps-C-and-L-and-raw-observation-is-not-serialized
  (doseq [capture-complete? [false true]]
  (let [{:keys [ports state events]} (fixture) process (Object.) original (ex-info "bookend" {})
        result (parent/run! packet
                 (assoc ports :creation-coverage (constantly {:owned-complete? false})
                              :recheck (fn [_ _ records]
                                        (swap! records conj (atom {:process process :retained-token (Object.)
                                                                   :capture-complete? capture-complete?
                                                                   :finalization {:tracked-complete? true}}))
                                        (throw original))) state)
        view (parent/observation-view state)]
    (is (identical? original (:error result)))
    (is (not-any? #{:release-L :release-C :disarm :launch} @events))
    (is (identical? process (:process @(first @(:bookends result)))))
    (is (not (.contains (pr-str view) "#object")))
    (is (= :unknown (:global-descendant-coverage view))))))

(deftest timer-expiry-and-cancellation-are-serialized-without-starting-a-thread
  (let [events (atom []) owner (proxy [Thread] [] (interrupt [] (swap! events conj :interrupt)))
        armed (atom :armed) cancelled (atom :cancelled)]
    (#'parent/expire! armed owner) (#'parent/expire! armed owner) (#'parent/expire! cancelled owner)
    (is (= :expired @armed)) (is (= [:interrupt] @events)))
  (doseq [initial [:armed :expired]]
    (let [events (atom []) state (atom initial)
          task (reify java.util.concurrent.Future
                 (cancel [_ interrupt?] (is (false? interrupt?)) (swap! events conj :cancel) true)
                 (isCancelled [_] false) (isDone [_] false)
                 (get [_] nil) (get [_ _ _] nil))
          executor (proxy [java.util.concurrent.AbstractExecutorService] []
                     (shutdownNow [] (swap! events conj :shutdown) (java.util.Collections/emptyList))
                     (shutdown [] nil) (isShutdown [] false) (isTerminated [] false)
                     (awaitTermination [_ _] false) (execute [_] nil))
          error (try (#'parent/disarm! {:state state :task task :executor executor}) nil
                     (catch clojure.lang.ExceptionInfo error error))]
      (is (= [:cancel :shutdown] @events))
      (if (= initial :armed) (is (= :cancelled @state))
          (is (= :C3-whole-deadline (:reason (ex-data error))))))))

(deftest preparation-pins-the-proposed-root-and-never-loads-runtime-ports
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                            "C3-parent-packet-" (make-array java.nio.file.attribute.FileAttribute 0)))
        root (.getCanonicalPath (io/file ".")) captured (atom nil)]
    (try
      (with-redefs [request/prepare! (fn [options]
                                      (reset! captured options)
                                      (let [child (assoc packet :cwd root :execution :never-submitted :launchable false)]
                                        (assoc child :request-file
                                               (request/create-edn! (str (io/file directory "request.edn")) child))))]
        (let [{:keys [packet packet-file]} (parent/prepare! {:directory (.getCanonicalPath directory)})]
          (is (= parent/C-root (:coordination-root @captured)))
          (is (= packet (parent/read-proposal! (:path packet-file) (:sha256 packet-file))))
          (is (false? (:launchable packet)))
          (is (= parent/participants (:participating-callers packet)))
          (is (= parent/C3-participants (:guard-participants @captured)))
          (is (= 7 (count (:participant-source-inputs packet))))
          (doseq [[name altered reason]
                  [["argv" (assoc packet :argv ["changed"]) :C3-child-request-drift]
                   ["participants" (assoc packet :participant-source-inputs []) :C3-participant-source-drift]
                   ["broker" (assoc packet :broker-source nil) :C3-broker-source-drift]]]
            (let [input (request/create-edn! (str (io/file directory (str name ".edn"))) altered)]
              (is (= reason
                     (try (parent/read-proposal! (:path input) (:sha256 input)) nil
                          (catch clojure.lang.ExceptionInfo error (:reason (ex-data error)))))))))
        (is (nil? (find-ns 'gravity.bootstrap)))
        (is (nil? (find-ns 'gravity.self-hosting.sh01-host-resource-broker)))
        (is (nil? (find-ns 'gravity.self-hosting.sh01-parallel-test-runner))))
      (finally (doseq [file (reverse (file-seq directory))] (.delete ^java.io.File file))))))
