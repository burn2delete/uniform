(ns gravity.syntax.selected-revision-parent
  "C3-only parent proposal and sequence. Loading this namespace opens no ports."
  (:refer-clojure :exclude [run!])
  (:require [clojure.edn :as edn] [clojure.java.io :as io]
            [gravity.resolve.selected-source-harness :as h]
            [gravity.syntax.selected-revision-request :as request]
            [gravity.syntax.selected-revision-git :as git]))

(def C-root "/Users/matt/code/burn2delete/uniform/.git/gravity-development-loop-v1/broker")
(def L-path "/private/tmp/gravity-sh07-heavy.lock")
(def profile (assoc request/host-profile :cleanup-reserve-ms 12000))
(def C3-participants ["selected-revision-parent/run!"
                      "selected-revision-request/child! under parent lease"
                      "selected-revision-git/git! under parent lease"])
(def participant-paths
  ["bootstrap/clojure/test/gravity/syntax/selected_revision_parent.clj"
   "bootstrap/clojure/test/gravity/syntax/selected_revision_request.clj"
   "bootstrap/clojure/test/gravity/syntax/selected_revision_git.clj"
   "bootstrap/clojure/test/gravity/syntax/selected_revision_acceptance.clj"
   "bootstrap/clojure/test/gravity/resolve/selected_source_harness.clj"
   "bootstrap/clojure/test/gravity/self_hosting/sh01_development_loop_wiring.clj"
   "bootstrap/clojure/test/gravity/self_hosting/sh01_incremental_check.clj"])
(def broker-path "bootstrap/clojure/test/gravity/self_hosting/sh01_host_resource_broker.clj")
(def participants
  {:direct-parent {:acquires :C-exclusive-then-L-immediate :thread-affine true}
   :direct-child {:acquires false :held-under :parent-C-and-L}
   :parent-and-child-Git-bookends {:acquires false :retention :normal-public-raw-tokens}
   :incremental-check-default-root-callers {:root :Git-common-development-loop-default
                                            :coverage :source-compatible-not-live-confirmation}
   :excluded [:unselected-H-only-profiler :unselected-UEP :warm-session :other-explicit-root-overrides]})

(defn- failure [reason] (ex-info "Selected C3 parent refused" {:reason reason}))
(defn- fatal? [error]
  (loop [error error]
    (cond (nil? error) nil
          (or (instance? ThreadDeath error) (instance? VirtualMachineError error) (instance? LinkageError error)) error
          :else (recur (.getCause ^Throwable error)))))
(defn- combine-errors [original error]
  (let [fatal (fatal? error)]
    (cond (nil? original) (or fatal error)
          (and fatal (not (fatal? original)))
          (do (.addSuppressed ^Throwable fatal original) fatal)
          :else (do (when-not (identical? original error) (.addSuppressed ^Throwable original error)) original))))
(defn- interrupted? [error]
  (loop [error error]
    (cond (nil? error) false (instance? InterruptedException error) true
          :else (recur (.getCause ^Throwable error)))))

(defn run!
  "One C3 parent sequence through explicit ports. Caller retains state, including
  raw handles, even on fatal exits. Controls are not runtime admission. A shared
  monotonic deadline clips waits; interrupt/native-call limits remain explicit."
  [packet ports state]
  (h/ensure! (and (= C-root (get-in packet [:guard-proposal :coordination-root]))
                  (= L-path (get-in packet [:guard-proposal :canonical-lock]))
                  (= :immediate-try-lock (get-in packet [:guard-proposal :lock-mode]))
                  (= :exclusive (get-in packet [:guard-proposal :resource-class]))
                  (= [:C :L] (get-in packet [:guard-proposal :acquire-order]))
                  (= [:L :C] (get-in packet [:guard-proposal :release-order]))
                  (= C3-participants (get-in packet [:guard-proposal :participants]))
                  (= profile (:parent-profile packet)) (empty? @state)) :C3-parent-packet)
  (doseq [key [:now :arm :check-watchdog :disarm :recheck :acquire-C :validate-C :release-C
               :try-L :release-L :launch :retain :handles :wait :finalize :creation-coverage]]
    (h/ensure! (or (fn? (get ports key)) (var? (get ports key))) :C3-parent-port))
  (let [owner (.getId (Thread/currentThread)) began ((:now ports))
        bookends (atom []) primary (atom nil) interrupted (atom false)
        record! (fn [error]
                  (when (or (interrupted? error) (.isInterrupted (Thread/currentThread)))
                    (reset! interrupted true))
                  (swap! primary combine-errors error))
        remaining (fn [maximum reserve]
                    (let [left (- (:whole-operation-timeout-ms profile)
                                  (- ((:now ports)) began) reserve)]
                      (h/ensure! (pos? left) :C3-whole-deadline)
                      (min maximum left)))
        work #(remaining % (:cleanup-reserve-ms profile))
        check! (fn []
                 (h/ensure! (= owner (.getId (Thread/currentThread))) :C3-parent-thread)
                 (remaining (:whole-operation-timeout-ms profile) 0)
                 (when-let [watchdog (:watchdog @state)] ((:check-watchdog ports) watchdog))
                 (when-let [lease (:C @state)]
                   (h/ensure! (identical? lease ((:validate-C ports) lease C-root :exclusive)) :C3-live-C)))
        git-ports (assoc ports :remaining-ms #(work (:whole-operation-timeout-ms profile)))
        recheck! (fn [] (check!) ((:recheck ports) packet git-ports bookends) (check!))]
    (swap! state assoc :owner-thread owner :began-ms began :bookends bookends
           :global-descendant-coverage :unknown :release-authorized? false)
    (try
      (try
        (swap! state assoc :watchdog ((:arm ports) #(remaining (:whole-operation-timeout-ms profile) 0)))
        (h/ensure! (:watchdog @state) :C3-watchdog-unavailable)
        (swap! state assoc :C ((:acquire-C ports) {:coordination-root C-root
                                                  :timeout-ms (work (:broker-timeout-ms profile))} :exclusive))
        (h/ensure! (:C @state) :C3-C-refused)
        (check!)
        (swap! state assoc :L ((:try-L ports) L-path))
        (h/ensure! (:L @state) :C3-L-refused)
        (recheck!)
        (swap! state assoc :start-attempted? true)
        (swap! state assoc :process ((:launch ports) (:argv packet) (:cwd packet)))
        (h/ensure! (:process @state) :C3-process-missing)
        (swap! state assoc :owned ((:retain ports) (:process @state)))
        (swap! state assoc :raw-handles ((:handles ports) (:owned @state)))
        (check!)
        (swap! state assoc :result
               ((:wait ports) (:process @state)
                (assoc (select-keys profile [:max-capture-bytes :max-capture-chars])
                       :timeout-ms (work (:child-timeout-ms profile))
                       :capture-wait-ms (work (:capture-wait-ms profile))) (:owned @state)))
        (when (true? (get-in @state [:result :wait-interrupted?])) (reset! interrupted true))
        (catch Throwable error (record! error))
        (finally
          (when-let [owned (:owned @state)]
            (try
              (swap! state assoc :finalization ((:finalize ports) owned))
              (swap! state assoc :raw-handles ((:handles ports) owned))
              (catch Throwable error (record! error))))
          (when (and (:owned @state) (not (fatal? @primary)))
            (try
              (recheck!)
              (swap! state assoc :post-recheck-complete? true)
              (catch Throwable error (record! error))))
          (when (and (or (:owned @state) (seq @bookends)) (not (fatal? @primary)))
            (try (swap! state assoc :creation-coverage ((:creation-coverage ports) @state))
                 (catch Throwable error (record! error))))
          ;; A returned token proves retention, not creation-set completeness.
          ;; Unknown launch/retention/termination keeps the original guards and
          ;; watchdog in the caller's raw state; there is no automatic recovery.
          (let [safe? (and (every? #(and (true? (get-in @% [:finalization :tracked-complete?]))
                                        (true? (:capture-complete? @%))) @bookends)
                           (or (empty? @bookends) (true? (get-in @state [:creation-coverage :owned-complete?])))
                           (or (not (:start-attempted? @state))
                               (and (true? (get-in @state [:finalization :tracked-complete?]))
                                    (true? (:post-recheck-complete? @state))
                                    (true? (get-in @state [:creation-coverage :owned-complete?])))))]
            (when safe?
              (swap! state assoc :release-authorized? true)
              (when-let [lock (:L @state)]
                (swap! state assoc :L-release :attempted)
                (try
                  (swap! state assoc :L-release-receipt ((:release-L ports) lock))
                  (h/ensure! (true? (get-in @state [:L-release-receipt :released?])) :C3-L-release-unknown)
                  (swap! state assoc :L-release :released)
                     (catch Throwable error (record! error))))
              (when (or (nil? (:L @state)) (= :released (:L-release @state)))
                (when-let [lease (:C @state)]
                  (swap! state assoc :C-release :attempted)
                  (try
                    (swap! state assoc :C-release-receipt ((:release-C ports) lease))
                    (h/ensure! (= :released (get-in @state [:C-release-receipt :outcome])) :C3-C-release-unknown)
                    (swap! state assoc :C-release :released)
                       (catch Throwable error (record! error)))))
              (when (and (or (nil? (:L @state)) (= :released (:L-release @state)))
                         (or (nil? (:C @state)) (= :released (:C-release @state))))
                (when-let [watchdog (:watchdog @state)]
                  (try
                    (remaining (:whole-operation-timeout-ms profile) 0)
                    ((:check-watchdog ports) watchdog)
                    (catch Throwable error (record! error)))
                  (try ((:disarm ports) watchdog) (swap! state assoc :watchdog-disarmed? true)
                       (remaining (:whole-operation-timeout-ms profile) 0)
                       (catch Throwable error (record! error)))))))))
      (when (and (nil? @primary) (not (:release-authorized? @state)))
        (record! (failure :C3-owned-terminal-or-capture-unproven)))
      (let [result (:result @state)]
        (when (and (:process @state)
                   (not (and (= :passed (:status result)) (= 0 (:exit-code result))
                             (every? false? (map result [:wait-interrupted? :output-truncated?
                                                        :capture-failed? :cleanup-failed?])))))
          (record! (failure :C3-child-nonpassing))))
      (swap! state assoc :error @primary :status (if (and (nil? @primary) (:watchdog-disarmed? @state)
                                                       (:release-authorized? @state)) :passed :failed))
      (when-let [fatal (fatal? @primary)] (throw fatal))
      @state
      (finally
        (when @interrupted (.interrupt (Thread/currentThread)))))))

(defn proposal [child-request]
  (h/ensure! (and (= C-root (get-in child-request [:guard-proposal :coordination-root]))
                  (= C3-participants (get-in child-request [:guard-proposal :participants]))) :C3-proposed-C-root)
  (assoc child-request :parent-profile profile :participating-callers participants
         :parent-method {:sequence 'gravity.syntax.selected-revision-parent/run!
                         :public-port-loader 'gravity.syntax.selected-revision-parent/normal-bindings!
                         :watchdog :JDK-daemon-scheduled-parent-interrupt
                         :parent-heap :checked-by-normal-bindings
                         :startup-and-publication :caller-binding-not-yet-reviewed
                         :guard-participants C3-participants
                         :coverage :proposed-C3-only-not-OWN-RV-or-H-inheritance}
         :broker-source (request/fingerprint
                         (str (io/file (:cwd child-request) broker-path)))
         :participant-source-inputs
         (mapv #(request/fingerprint (str (io/file (:cwd child-request) %)))
               participant-paths)
         :whole-deadline {:clock :System-nanoTime :scope :parent-acquisition-through-release
                          :child :parent-wait-clipped-to-remaining :Git :shared-remaining-callback
                          :cleanup :reserved-12000ms :native-hard-deadline? false}
         :unsupported-facilities [:external-startup-native-call-and-shutdown-supervision
                                  :accepted-creation-set-and-platform-binding]
         :remaining-evidence [:actual-cooperating-caller-configuration
                              :effective-valid-config-and-native-library-helper-closure
                              :reviewed-parent-startup-and-publication-binding]
         :execution :never-submitted :launchable false))

(defn prepare! [options]
  (let [child (request/prepare! (assoc options :coordination-root C-root :guard-participants C3-participants))
        packet (proposal child)
        path (str (io/file (:directory options) "parent-packet.edn"))]
    {:packet packet :packet-file (request/create-edn! path packet)}))

(defn- expire! [state ^Thread owner]
  (locking state
    (when (= :armed @state)
      (reset! state :expired)
      (.interrupt owner))))

(defn- arm! [remaining]
  (let [owner (Thread/currentThread) state (atom :armed)
        executor (java.util.concurrent.Executors/newSingleThreadScheduledExecutor
                  (reify java.util.concurrent.ThreadFactory
                    (newThread [_ runnable]
                      (doto (Thread. runnable "selected-C3-deadline") (.setDaemon true)))))]
    (try
      {:state state :executor executor
       :task (.schedule executor ^Runnable (reify Runnable (run [_] (expire! state owner)))
                        (long (remaining)) java.util.concurrent.TimeUnit/MILLISECONDS)}
      (catch Throwable error
        (try (.shutdownNow executor)
             (catch Throwable secondary (throw (combine-errors error secondary))))
        (throw error)))))

(defn- check-watchdog! [watchdog]
  (h/ensure! (= :armed @(:state watchdog)) :C3-whole-deadline))

(defn- disarm! [{:keys [state task executor]}]
  (try
    (locking state
      (h/ensure! (= :armed @state) :C3-whole-deadline)
      (reset! state :cancelled))
    (finally
      (try (.cancel ^java.util.concurrent.Future task false)
           (finally (.shutdownNow ^java.util.concurrent.ExecutorService executor))))))

(defn- try-L! [path]
  (h/ensure! (= L-path path) :C3-canonical-L)
  (let [channel (java.nio.channels.FileChannel/open
                 (.toPath (io/file path))
                 (into-array java.nio.file.OpenOption
                   [java.nio.file.StandardOpenOption/CREATE java.nio.file.StandardOpenOption/WRITE
                    java.nio.file.LinkOption/NOFOLLOW_LINKS]))]
    (try
      (if-let [lock (.tryLock channel)]
        {:channel channel :lock lock :thread (.getId (Thread/currentThread))}
        (do (.close channel) nil))
      (catch Throwable error
        (try (.close channel) (catch Throwable secondary (throw (combine-errors error secondary))))
        (throw error)))))

(defn- release-L! [{:keys [channel lock thread]}]
  (h/ensure! (= thread (.getId (Thread/currentThread))) :C3-parent-thread)
  (let [error (atom nil)]
    (try (.release ^java.nio.channels.FileLock lock)
         (catch Throwable failure (reset! error failure))
         (finally
           (try (.close ^java.nio.channels.FileChannel channel)
                (catch Throwable failure (swap! error combine-errors failure)))))
    (when @error (throw @error))
    {:released? true}))

(defn- verify-source-bindings! [packet]
  (h/ensure! (= (:participant-source-inputs packet)
                  (mapv #(request/fingerprint (str (io/file (:cwd packet) %))) participant-paths))
             :C3-participant-source-drift)
  (h/ensure! (= (:broker-source packet)
                  (request/fingerprint (str (io/file (:cwd packet) broker-path)))) :C3-broker-source-drift))

(defn normal-bindings!
  "Future explicit loader only. The normal creation-set port remains unknown;
  these bindings alone cannot admit a C3 run or authorize guard release."
  [packet]
  (h/ensure! (= (:heap-bytes profile) (.maxMemory (Runtime/getRuntime))) :C3-parent-heap-profile)
  (verify-source-bindings! packet)
  (let [process (request/runner-ports! (:cwd packet))
        _ (require 'gravity.self-hosting.sh01-host-resource-broker)
        broker (into {} (for [[key name args]
                             [[:acquire-C 'acquire! '[options resource-class]]
                              [:validate-C 'validate-live-lease! '[lease expected-root expected-class]]
                              [:release-C 'release! '[lease]]]]
                         (let [v (ns-resolve 'gravity.self-hosting.sh01-host-resource-broker name)]
                           (h/ensure! (and v (not (:private (meta v))) (= [args] (vec (:arglists (meta v)))))
                                      :C3-public-broker-port)
                           [key v])))]
    (merge process broker
      {:now #(quot (System/nanoTime) 1000000) :arm arm! :check-watchdog check-watchdog! :disarm disarm!
       :try-L try-L! :release-L release-L! :verify-git! request/verify-git-binding!
       :creation-coverage (fn [_] {:owned-complete? false :global-descendant-coverage :unknown
                                  :reason :C3-creation-set-and-native-platform-binding-unaccepted})
       :recheck
       (fn [request ports records]
         (with-redefs [h/git! (partial git/git! ports (:git-binding request) records)]
           (request/read-prepared! (get-in request [:prepared-input :path])
                                   (get-in request [:prepared-input :sha256]))
           (request/verify-git-binding! (:git-binding request))))})))

(defn read-proposal! [path expected-sha]
  (let [input (h/file-input! path)]
    (h/ensure! (= expected-sha (:sha256 input)) :C3-parent-packet-drift)
    (let [packet (edn/read-string (:text input))]
      (h/ensure! (and (false? (:launchable packet)) (= :never-submitted (:execution packet))
                      (= C-root (get-in packet [:guard-proposal :coordination-root]))
                      (= profile (:parent-profile packet))) :C3-parent-proposal-only)
      (verify-source-bindings! packet)
      (let [child-file (:request-file packet)
            child-input (h/file-input! (:path child-file))]
        (h/ensure! (= child-file (select-keys child-input [:path :bytes :sha256])) :C3-child-request-drift)
        (let [child (edn/read-string (:text child-input))]
          (h/ensure! (and (map? child) (= child (select-keys packet (keys child)))) :C3-child-request-drift)))
      packet)))

(defn observation-view [state]
  (let [raw @state
        scalars (fn [value keys]
                  (into {} (filter (fn [[_ v]] (or (nil? v) (string? v) (keyword? v)
                                                   (boolean? v) (integer? v))))
                        (select-keys value keys)))]
    (cond-> (assoc (scalars raw [:status :owner-thread :began-ms :start-attempted?
                                :release-authorized? :L-release :C-release :watchdog-disarmed?])
                   :global-descendant-coverage :unknown
                   :result (scalars (:result raw) [:status :exit-code :wait-interrupted?
                                                  :output-truncated? :capture-failed? :cleanup-failed?])
                   :finalization (scalars (:finalization raw) [:tracked-complete? :complete?])
                   :creation-coverage (scalars (:creation-coverage raw) [:owned-complete? :reason])
                   :bookends (when-let [records (:bookends raw)] (git/observation-view records)))
      (:error raw) (assoc :error-class (.getName (class (:error raw)))))))
