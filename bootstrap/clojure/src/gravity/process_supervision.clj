(ns gravity.process-supervision
  "Small, dependency-free ownership primitives for bounded child processes.

  The substrate retains the actual Process and ProcessHandle objects returned by
  the JVM.  It never reconstructs ownership from numeric PIDs.  Callers supply
  their result policy and, where needed, callbacks for diagnostics or timing;
  this namespace only owns launch, observation, bounded waiting, and cleanup."
  (:import [java.io File]
           [java.lang Process ProcessHandle]
           [java.util.concurrent TimeUnit]
           [java.util.concurrent.locks LockSupport]))

(defrecord OwnedProcess
    [process handles discovery-error cleanup-error wait-throwable terminal owner-thread-id])

(defn fatal-throwable?
  [throwable]
  (or (instance? ThreadDeath throwable)
      (instance? VirtualMachineError throwable)
      (instance? LinkageError throwable)))

(defn fatal-cause
  [throwable]
  (loop [candidate throwable]
    (cond
      (nil? candidate) nil
      (fatal-throwable? candidate) candidate
      :else (recur (.getCause ^Throwable candidate)))))

(defn rethrow-fatal!
  [throwable]
  (when-let [fatal (fatal-cause throwable)]
    (throw fatal))
  throwable)

(defn restore-interrupt!
  [throwable]
  (loop [candidate throwable]
    (when candidate
      (if (instance? InterruptedException candidate)
        (.interrupt (Thread/currentThread))
        (recur (.getCause ^Throwable candidate))))))

(defn interrupted-throwable?
  [throwable]
  (loop [candidate throwable]
    (cond
      (nil? candidate) false
      (instance? InterruptedException candidate) true
      :else (recur (.getCause ^Throwable candidate)))))

(defn throwable-message
  [throwable]
  (or (.getMessage ^Throwable throwable)
      (str throwable)))

(defn start-process
  "Start an explicit argv vector without invoking a shell.

  Options may supply :working-directory, :environment, :redirect-output,
  :redirect-input, and :redirect-error-stream.  Environment entries are
  additions to the inherited environment; callers that require a complete
  replacement must clear it explicitly in their own host boundary."
  ([argv]
   (start-process argv {}))
  ([argv {:keys [working-directory environment redirect-output redirect-input
                 redirect-error-stream]}]
   (when-not (and (vector? argv) (seq argv) (every? string? argv))
     (throw (ex-info "Process launch requires a nonempty argv vector"
                     {:id "PROCESS-LAUNCH-ARGV" :argv argv})))
   (let [builder (ProcessBuilder. ^java.util.List argv)]
     (when working-directory
       (.directory builder (File. (str working-directory))))
     (when redirect-output
       (.redirectOutput builder (File. (str redirect-output))))
     (when redirect-input
       (.redirectInput builder (File. (str redirect-input))))
     (.redirectErrorStream builder (boolean redirect-error-stream))
     (doseq [[name value] (or environment {})]
       (when-not (and (string? name) (string? value))
         (throw (ex-info "Process environment entries must be strings"
                         {:id "PROCESS-LAUNCH-ENVIRONMENT"
                          :name name :value value})))
       (.put (.environment builder) name value))
     (.start builder))))

(defn handle-descendants
  "Return the currently observable raw descendant handles for one handle."
  [^ProcessHandle root]
  (try
    (with-open [descendants (.descendants root)]
      {:handles (vec (iterator-seq (.iterator descendants)))
       :error nil})
    (catch Throwable throwable
      (restore-interrupt! throwable)
      {:handles []
       :error throwable})))

(defn retain-process-error!
  "Keep the first ordinary error, while promoting a later fatal cause and
  retaining the other object as suppressed evidence."
  [cell error]
  (restore-interrupt! error)
  (let [previous @cell
        fatal (fatal-cause error)
        primary (cond (nil? previous) (or fatal error)
                      (and fatal (not (fatal-cause previous))) fatal
                      :else previous)
        secondary (if (identical? primary previous) fatal previous)]
    (when (and secondary (not (identical? primary secondary))
               (< (count (.getSuppressed ^Throwable primary)) 8)
               (not-any? #(identical? secondary %)
                         (.getSuppressed ^Throwable primary)))
      (.addSuppressed ^Throwable primary secondary))
    (reset! cell primary)))

(defn new-owned-process
  [process]
  (->OwnedProcess process (atom []) (atom nil) (atom nil) (atom nil)
                 (atom nil) (.getId (Thread/currentThread))))

(defn require-owned-process!
  [owned]
  (when-not (and (map? owned)
                 (instance? Process (:process owned))
                 (instance? clojure.lang.IAtom (:handles owned))
                 (instance? clojure.lang.IAtom (:discovery-error owned))
                 (instance? clojure.lang.IAtom (:cleanup-error owned))
                 (instance? clojure.lang.IAtom (:wait-throwable owned))
                 (instance? clojure.lang.IAtom (:terminal owned))
                 (= (:owner-thread-id owned)
                    (.getId (Thread/currentThread))))
    (throw (ex-info "Owned process handle belongs to its retaining thread"
                    {:id "PROCESS-OWNERSHIP"})))
  owned)

(defn retain-owned-process!
  "Retain actual process and handle objects immediately after launch.

  :owned-factory is an internal compatibility seam for callers that preserve
  an existing record type.  The returned value must expose the same fields as
  OwnedProcess; all lifecycle operations continue to use raw handles."
  ([process]
   (retain-owned-process! process {}))
  ([process {:keys [owned-factory]}]
   (when-not (instance? Process process)
     (throw (ex-info "Retaining ownership requires an actual Process"
                     {:id "PROCESS-OWNERSHIP"})))
   (let [owned (if owned-factory
                 (owned-factory process)
                 (new-owned-process process))]
     (when-not (= (:owner-thread-id owned)
                  (.getId (Thread/currentThread)))
       (throw (ex-info "Owned process handle belongs to its retaining thread"
                       {:id "PROCESS-OWNERSHIP"})))
     (try
       (swap! (:handles owned) conj (.toHandle ^Process process))
       (catch Throwable error
         (retain-process-error! (:discovery-error owned) error)))
     owned)))

(defn retained-process-handles
  "Return the retained raw handles, never PID-reconstructed handles."
  [owned]
  @(:handles (require-owned-process! owned)))

(defn- process-handle-limit-error?
  [throwable]
  (= :process-handle-limit (:id (ex-data throwable))))

(defn observe-owned-process!
  "Refresh every retained handle, preserving handles that disappear from a
  later enumeration.  :max-handles is an optional host observation bound; it
  records an incomplete observation rather than becoming a semantic pass."
  ([owned]
   (observe-owned-process! owned {}))
  ([owned {:keys [handle-descendants-fn retain-error-fn max-handles]}]
   (require-owned-process! owned)
   (let [discover (or handle-descendants-fn handle-descendants)
         retain-error (or retain-error-fn retain-process-error!)]
     ;; Revisit retained descendants as well as the root: a known child can
     ;; fork after root exit.  Absence from a later enumeration never drops a
     ;; handle already retained by this owner.
     (doseq [handle (vec @(:handles owned))]
       (let [{:keys [handles error]} (discover handle)
             handles (if max-handles
                       (vec (take (inc (long max-handles)) handles))
                       handles)]
         (when error (retain-error (:discovery-error owned) error))
         (swap! (:handles owned) #(vec (distinct (concat % handles))))))
     (when (and max-handles
                (> (count @(:handles owned)) (long max-handles))
                (not (process-handle-limit-error? @(:discovery-error owned))))
       (retain-error (:discovery-error owned)
                     (ex-info "Process handle observation bound exceeded"
                              {:id :process-handle-limit
                               :limit (long max-handles)}))
       ;; Retain only the bounded, actual handles.  The recorded observation
       ;; error makes the cleanup result fail closed about omitted descendants.
       (swap! (:handles owned)
              #(vec (take (long max-handles) %))))
     owned)))

(defn alive-handle?
  [^ProcessHandle handle owned]
  (try
    (.isAlive handle)
    (catch Throwable throwable
      (retain-process-error! (:cleanup-error owned) throwable)
      ;; A handle that cannot be queried remains a cleanup failure, not an
      ;; unverified clean result.
      true)))

(defn request-handle-termination!
  [^ProcessHandle handle forcibly? owned]
  (try
    (if forcibly?
      (.destroyForcibly handle)
      (.destroy handle))
    (catch Throwable throwable
      (retain-process-error! (:cleanup-error owned) throwable)
      false)))

(defn reap-process-handle!
  [^ProcessHandle handle deadline owned]
  (try
    ;; ProcessHandle::onExit is the JVM wait boundary for descendants that do
    ;; not have a java.lang.Process object of their own.
    (when (< (System/nanoTime) deadline)
      (.get (.onExit handle)
            (long (max 1 (min 100
                              (quot (- deadline (System/nanoTime)) 1000000))))
            TimeUnit/MILLISECONDS)
      true)
    (catch Throwable throwable
      (retain-process-error! (:cleanup-error owned) throwable)
      false)))

(defn retained-root-alive?
  [owned]
  (try
    (.isAlive ^Process (:process owned))
    (catch Throwable error
      (retain-process-error! (:cleanup-error owned) error)
      true)))

(defn- handle-pid
  [^ProcessHandle handle]
  (long (.pid handle)))

(defn terminate-owned-process!
  "Bounded descendant-first cleanup for one retained process.

  Callbacks are supplied so compatibility callers can preserve existing
  diagnostics and test seams while sharing the ownership transaction."
  ([owned]
   (terminate-owned-process! owned {}))
  ([owned {:keys [grace-ms timeout-ms observe-fn observe-options alive-fn
                 request-fn reap-fn root-alive-fn]
           :or {grace-ms 100 timeout-ms 1500}}]
   (require-owned-process! owned)
   (let [observe! (or observe-fn
                      #(observe-owned-process! % (or observe-options {})))
         alive? (or alive-fn alive-handle?)
         request! (or request-fn request-handle-termination!)
         reap! (or reap-fn reap-process-handle!)
         root-alive? (or root-alive-fn retained-root-alive?)
         root (first @(:handles owned))
         started (System/nanoTime)
         grace-deadline (+ started (* (long grace-ms) 1000000))
         deadline (+ started (* (long timeout-ms) 1000000))
         requested (atom #{})
         forced (atom #{})
         live-at-entry (atom nil)]
     (loop []
       (observe! owned)
       (let [observed @(:handles owned)
             alive (vec (filter #(alive? % owned) observed))
             root-alive (root-alive? owned)
             now (System/nanoTime)]
         (compare-and-set! live-at-entry nil
                           (count (remove #(= root %) alive)))
         (doseq [handle (reverse (remove #(= root %) alive))
                 :when (not (contains? @requested handle))]
           (request! handle false owned)
           (swap! requested conj handle))
         ;; The actual Process is root control, even when its ProcessHandle is
         ;; unavailable or cannot be queried.
         (when (and root-alive (not (contains? @requested :root)))
           (try (.destroy ^Process (:process owned))
                (catch Throwable error
                  (retain-process-error! (:cleanup-error owned) error)))
           (swap! requested conj :root))
         (cond
           (and (empty? alive) (not root-alive))
           (let [reaped (mapv #(reap! % deadline owned) observed)
                 reaped? (and (every? true? reaped)
                              (nil? @(:cleanup-error owned)))]
             {:status (if reaped? :tracked-clean :failed)
              :complete? (when-not reaped? false)
              :tracked-complete? reaped?
              :descendant-coverage :unknown
              :root-alive? false
              :descendants-alive 0
              :live-descendants-at-finalization @live-at-entry
              :forced? (boolean (seq @forced))
              :observed-pids (mapv handle-pid observed)
              :descendant-pids (mapv handle-pid
                                     (remove #(= root %) observed))
              :survivor-pids []
              :discovery-error (some-> @(:discovery-error owned) str)
              :cleanup-error (some-> @(:cleanup-error owned) str)
              :observation-truncated? (process-handle-limit-error?
                                       @(:discovery-error owned))})

           (>= now deadline)
           {:status :failed :complete? false :tracked-complete? false
            :descendant-coverage :unknown :root-alive? root-alive
            :descendants-alive (count (remove #(= root %) alive))
            :live-descendants-at-finalization @live-at-entry
            :forced? (boolean (seq @forced))
            :observed-pids (mapv handle-pid observed)
            :descendant-pids (mapv handle-pid
                                   (remove #(= root %) observed))
            :survivor-pids (mapv handle-pid alive)
            :discovery-error (some-> @(:discovery-error owned) str)
            :cleanup-error (some-> @(:cleanup-error owned) str)
            :observation-truncated? (process-handle-limit-error?
                                     @(:discovery-error owned))}

           :else
           (do
             (when (>= now grace-deadline)
               (doseq [handle (reverse (remove #(= root %) alive))
                       :when (and (not (contains? @forced handle))
                                  (alive? handle owned))]
                 (request! handle true owned)
                 (swap! forced conj handle))
               (when (and (root-alive? owned)
                          (not (contains? @forced :root)))
                 (try (.destroyForcibly ^Process (:process owned))
                      (catch Throwable error
                        (retain-process-error! (:cleanup-error owned) error)))
                 (swap! forced conj :root)))
             (LockSupport/parkNanos
              (long (* (min 25
                            (max 1 (quot (- deadline now) 1000000)))
                       1000000)))
             (recur))))))))

(defn finalize-owned-process!
  "Finalize one retained process once on its owner thread.

  :terminate-fn may provide a caller-specific result policy.  :fatal-fn is
  called for recorded discovery/cleanup errors; ordinary errors remain in the
  result and never become a false clean claim."
  ([owned]
   (finalize-owned-process! owned {}))
  ([owned {:keys [require-fn terminate-fn fatal-fn]}]
   ((or require-fn require-owned-process!) owned)
   (let [terminal (:terminal owned)]
     (if (compare-and-set! terminal nil :finalizing)
       (try
         (let [result ((or terminate-fn terminate-owned-process!) owned)]
           (when-let [error @(:discovery-error owned)]
             ((or fatal-fn rethrow-fatal!) error))
           (when-let [error @(:cleanup-error owned)]
             ((or fatal-fn rethrow-fatal!) error))
           (reset! terminal {:result result})
           result)
         (catch Throwable error
           (reset! terminal {:error error})
           (throw error)))
       (let [{:keys [result error]} @terminal]
         (cond error (throw error)
               result result
               :else (throw (ex-info "Reentrant process finalization refused"
                                     {:id "PROCESS-FINALIZATION"}))))))))

(defn wait-for-process!
  "Wait with an optional bound while repeatedly observing retained handles."
  ([^Process process timeout-ms owned]
   (wait-for-process! process timeout-ms owned {}))
  ([^Process process timeout-ms owned
    {:keys [observe-fn observe-options fatal-fn restore-fn interrupted-fn]}]
   (let [observe! (or observe-fn
                      #(observe-owned-process! % (or observe-options {})))
         fatal! (or fatal-fn rethrow-fatal!)
         restore! (or restore-fn restore-interrupt!)
         interrupted? (or interrupted-fn interrupted-throwable?)]
     (try
       (let [deadline (when timeout-ms
                        (+ (System/nanoTime) (* (long timeout-ms) 1000000)))]
         (loop []
           (observe! owned)
           (when-let [error @(:discovery-error owned)] (fatal! error))
           (let [remaining (when deadline
                             (max 0 (quot (- deadline (System/nanoTime))
                                          1000000)))
                 completed? (.waitFor process
                                      (long (min 50 (or remaining 50)))
                                      TimeUnit/MILLISECONDS)]
             (observe! owned)
             (cond completed? {:completed? true :interrupted? false}
                   (and deadline (>= (System/nanoTime) deadline))
                   {:completed? false :interrupted? false}
                   :else (recur)))))
       (catch Throwable throwable
         (reset! (:wait-throwable owned) throwable)
         (fatal! throwable)
         (restore! throwable)
         {:completed? false
          :interrupted? (interrupted? throwable)
          :error (throwable-message throwable)})))))

(defn bounded-process-wait!
  [^Process process timeout-ms]
  (try
    (.waitFor process (long timeout-ms) TimeUnit/MILLISECONDS)
    (catch Throwable throwable
      (rethrow-fatal! throwable)
      (restore-interrupt! throwable)
      false)))
