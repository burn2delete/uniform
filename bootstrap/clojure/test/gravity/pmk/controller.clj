(ns gravity.pmk.controller
  "One-shot PMK lifecycle. Normal ports are explicit and never run at load."
  (:refer-clojure :exclude [run!])
  (:require [clojure.string :as str]
            [gravity.pmk.acceptance :as pmk]
            [gravity.pmk.first-request :as first]))

(def port-keys #{:clock! :bookend! :claim! :acquire! :validate! :release! :start!
                 :supervise! :capture! :terminate! :publish! :interrupt!})

(defn- require! [p reason]
  (when-not p (throw (ex-info "PMK controller refused" {:reason reason}))))

(defn- error-record [error]
  (when error {:class (.getName (class error)) :message (.getMessage ^Throwable error)
               :reason (:reason (ex-data error))}))

(defn expected-bookend [request binding]
  {:source-revision (:source-revision request) :checkout (:checkout request)
   :inputs-sha256 (:inputs-sha256 request) :parent-sha256 (:parent-sha256 request)
   :request-sha256 (first/digest request) :binding-sha256 (first/digest binding)
   :runtime (:runtime binding) :cwd (:cwd request)})

(defn claim-record [request]
  {:status :claimed :operation-id (:operation-id request) :request-sha256 (first/digest request)})

(defn claim-identity [binding]
  (first/digest {:entrypoint 'gravity.pmk.controller/claim-operation!
                 :source-sha256 (get-in binding [:controller :sha256])}))

(defn claim-operation! [bound]
  (let [request (:request bound)
        options (into-array java.nio.file.LinkOption [java.nio.file.LinkOption/NOFOLLOW_LINKS])]
    (doseq [path (vals (:outputs request))]
      (require! (not (java.nio.file.Files/exists (.toPath (java.io.File. ^String path)) options)) :output-exists))
    (let [record (claim-record request)
          path (.toPath (java.io.File. (str (get-in request [:outputs :terminal]) ".started")))
          bytes (.getBytes (str (pr-str record) "\n") "UTF-8")]
      (java.nio.file.Files/write path bytes
        (into-array java.nio.file.OpenOption [java.nio.file.StandardOpenOption/CREATE_NEW
                                             java.nio.file.StandardOpenOption/WRITE]))
      record)))

(defn child-command
  "Direct Java bypasses launcher config discovery. The only target call is
  the normal runner with the exact request's CLI selection and machine sink."
  [request binding]
  (let [result-path (get-in request [:outputs :result])
        args (subvec (:argv request) 3)
        limit (get-in binding [:limits :capture-bytes])
        expression
        (str "(require 'gravity.development-test-runner) "
             "(assert (= 2147483648 (.maxMemory (Runtime/getRuntime)))) "
             "(binding [gravity.development-test-runner/*machine-result-sink* "
             "(fn [r] (let [b (.getBytes (str (pr-str r) \"\\n\") \"UTF-8\")] "
             "(assert (<= (alength b) " (pr-str limit) ")) "
             "(java.nio.file.Files/write (.toPath (java.io.File. " (pr-str result-path) ")) b "
             "(into-array java.nio.file.OpenOption [java.nio.file.StandardOpenOption/CREATE_NEW "
             "java.nio.file.StandardOpenOption/WRITE]))))] "
             "(let [exit (gravity.development-test-runner/run-cli! " (pr-str args) ")] "
             "(shutdown-agents) (System/exit exit)))")]
    [(get-in binding [:runtime :java :path]) "-Xmx2g" "-XX:-OmitStackTraceInFastThrow"
     "-cp" (str/join java.io.File/pathSeparator (map :path (get-in binding [:runtime :ordered-classpath])))
     "clojure.main" "-e" expression]))

(defn run!
  "Call only through the owner's action-specific admitted route. bind-inputs
  and its independently reviewed normal verifier must succeed first. This
  function cannot confer platform authority. No retry, recovery or root default.
  Port calls account elapsed time; an external supervisor must bound native
  start/FS/JVM/cleanup calls, which are not preemptible here."
  [request expected observed verify! ports]
  (let [bound (first/bind-inputs request expected observed verify!)
        _ (require! (and (= port-keys (set (keys ports)))
                         (every? #(or (fn? %) (var? %)) (vals ports))) :ports)
        _ (require! (and (identical? #'claim-operation! (:claim! ports))
                         (= (claim-identity expected) (get-in expected [:controller :ports :claim]))) :claim-port-binding)
        command (child-command request expected)
        _ (require! (= {:argv command :cwd (:cwd request)}
                       (get-in expected [:controller :launcher])) :launcher)
        _ (require! (every? #(or (and (= :broker (:kind %))
                                      (#{:normal :memory-heavy :exclusive} (:resource-class %)))
                                 (and (= :canonical (:kind %)) (= :exclusive (:resource-class %))))
                            (:guards expected)) :guard-kind)
        ;; SH-01's documented protocol forbids nested broker acquisition.
        ;; Distinct roots do not create an exception to that contract.
        _ (require! (<= (count (filter #(= :broker (:kind %)) (:guards expected))) 1)
                    :nested-broker-unsupported)
        _ (require! (= [[:C :broker :exclusive] [:L :canonical :exclusive]]
                       (mapv (juxt :id :kind :resource-class) (:guards expected)))
                    :pmk-guard-configuration)
        clock (:clock! ports) deadline (+ (clock) (get-in expected [:limits :whole-timeout-ms]))
        leases (atom []) child (atom nil) handled (atom false) claimed (atom false)
        failure (atom nil) cleanup (atom nil) releases (atom []) result (atom nil)
        interrupt-errors (atom [])
        fail! (fn [error]
                (when-not @failure (reset! failure error))
                (when (instance? InterruptedException error)
                  (try ((:interrupt! ports))
                       (catch Throwable secondary (swap! interrupt-errors conj (error-record secondary))))))
        remaining (fn [] (let [left (- deadline (clock))]
                           (require! (pos? left) :whole-deadline) left))]
    (try
      (let [before ((:bookend! ports) bound)]
        (require! (= (expected-bookend request expected) before) :source-bookend)
        (remaining)
        (require! (= (claim-record request) ((:claim! ports) bound)) :operation-claimed)
        (reset! claimed true)
        (remaining)
        (doseq [guard (:guards expected)]
          (let [lease ((:acquire! ports) guard (remaining))]
            (require! (some? lease) :lease-absent)
            (swap! leases conj [guard lease])
            ((:validate! ports) guard lease)
            (remaining)))
        (reset! child ((:start! ports) command (:cwd request)))
        (require! (some? @child) :child-absent)
        (let [process ((:supervise! ports) @child
                       {:timeout-ms (remaining)
                        :max-capture-bytes (get-in expected [:limits :capture-bytes])
                        :max-capture-chars (get-in expected [:limits :capture-bytes])})]
          ;; A supervisor that already attempted termination owns that result;
          ;; never terminate it again to guess at unknown cleanup.
          (reset! handled (or (true? (:terminal? process)) (true? (:cleanup-attempted? process))))
          (reset! result process)
          (remaining)
          (require! (and (= :passed (:status process)) (= 0 (:exit-code process))
                         (true? (:terminal? process)) (false? (:root-alive? process))
                         (true? (:root-exited? process))
                         (true? (:tracked-owned-processes-terminal? process))
                         (= :unknown (:descendant-coverage process))
                         (nil? (:cleanup-complete? process))
                         (= :tracked-clean (get-in process [:cleanup :status]))
                         (true? (get-in process [:cleanup :tracked-complete?]))
                         (false? (get-in process [:cleanup :root-alive?]))
                         (nil? (get-in process [:cleanup :complete?]))
                         (= :unknown (get-in process [:cleanup :descendant-coverage]))
                         (zero? (get-in process [:cleanup :live-descendants-at-finalization] -1))
                         (empty? (get-in process [:cleanup :survivor-pids]))
                         (nil? (get-in process [:cleanup :discovery-error]))
                         (nil? (get-in process [:cleanup :cleanup-error]))
                         (false? (:cleanup-failed? process))
                         (false? (:timeout-containment-unproven? process))
                         (false? (:capture-failed? process))
                         (false? (:output-truncated? process))
                         (nil? (:wait-error process)) (false? (:wait-interrupted? process))
                         (= :complete (:stdout-capture-status process) (:stderr-capture-status process))
                         (nil? (:stdout-capture-error process)) (nil? (:stderr-capture-error process))
                         (false? (:stdout-capture-forced-close? process))
                         (false? (:stderr-capture-forced-close? process))) :process-nonpass)
          (let [capture ((:capture! ports) bound process)
                after ((:bookend! ports) bound)
                plan (assoc (:parent-plan request) :selection (:selection request)
                            :closure [(first (:closure (:parent-plan request)))] :argv (:argv request))]
            (require! (= (expected-bookend request expected) before after) :source-drift)
            (pmk/attribute plan plan capture)
            (require! (and (= :passed (:outcome capture)) (= 1 (get-in capture [:summary :test]))
                           (pos? (get-in capture [:summary :pass]))) :capture-nonpass)
            (reset! result (assoc process :reported-result capture :before before :after after)))))
      (catch Throwable error (fail! error))
      (finally
        (when (and @child (not @handled))
          (reset! handled true)
          (try (reset! cleanup ((:terminate! ports) @child))
               (catch Throwable error (reset! cleanup {:status :unknown}) (fail! error))))
        (doseq [[guard lease] (reverse @leases)]
          (try
            (let [release ((:release! ports) guard lease)]
              (swap! releases conj {:guard (:id guard) :receipt release})
              (when-not (= :released (:outcome release))
                (fail! (ex-info "PMK guard release incomplete" {:reason :release-incomplete}))))
            (catch Throwable error
              (swap! releases conj {:guard (:id guard) :error (error-record error)})
              (fail! error))))))
    (try (remaining) (catch Throwable error (fail! error)))
    ;; A failed/duplicate claim does not own this operation's output paths.
    ;; Do not start or publish into the original operation, even a refusal.
    (when-not @claimed (throw @failure))
    (let [receipt {:schema :pmk-first-controller-result-v1 :execution :attempted
                   :operation-id (:operation-id request) :request-sha256 (first/digest request)
                   :claim (claim-record request)
                   :status (if @failure :failed :passed) :error (error-record @failure)
                   :process (when @result
                              (into {} (map (fn [[k v]] [k (if (instance? Throwable v) (error-record v) v)]) @result)))
                   :cleanup @cleanup :releases @releases
                   :interrupt-errors @interrupt-errors
                   :authority :non-authoritative :authenticated? false}]
      ;; Exactly one publication, after all releases. Publication failure escapes;
      ;; it is never retried and cannot be returned as a passing result.
      (try ((:publish! ports) receipt)
           (catch Throwable error
             (throw (ex-info "PMK terminal publication failed; no retry"
                             {:original-error (error-record @failure)
                              :publication-error (error-record error)} (or @failure error)))))
      receipt)))

(defn normal-process-ports
  "Bind existing broker/process implementations, never a new broker root.
  Supply the owner's reviewed bookend, capture and create-new publication
  hooks separately. Constructing ports performs no acquisition or launch."
  [hooks]
  (require! (and (= #{:bookend! :capture! :publish!} (set (keys hooks)))
                 (every? fn? (vals hooks))) :normal-hooks)
  (merge
   {:clock! #(quot (System/nanoTime) 1000000)
    :interrupt! #(.interrupt (Thread/currentThread))
    :claim! #'claim-operation!
    :acquire! (fn [guard timeout]
                (if (= :canonical (:kind guard))
                  (let [path (.toPath (java.io.File. ^String (:root guard)))
                        _ (require! (java.nio.file.Files/isRegularFile path
                                      (into-array java.nio.file.LinkOption [java.nio.file.LinkOption/NOFOLLOW_LINKS]))
                                    :canonical-existing-file)
                        channel (java.nio.channels.FileChannel/open path
                                  (into-array java.nio.file.OpenOption
                                    [java.nio.file.StandardOpenOption/WRITE java.nio.file.LinkOption/NOFOLLOW_LINKS]))]
                    (try (let [lock (.tryLock channel)]
                           (require! lock :canonical-busy) {:channel channel :lock lock})
                         (catch Throwable error (.close channel) (throw error))))
                  ((requiring-resolve 'gravity.self-hosting.sh01-host-resource-broker/acquire!)
                   {:coordination-root (:root guard) :timeout-ms timeout} (:resource-class guard))))
    :validate! (fn [guard lease]
                 (if (= :canonical (:kind guard))
                   (require! (.isValid ^java.nio.channels.FileLock (:lock lease)) :canonical-invalid)
                   (do
                     ((requiring-resolve 'gravity.self-hosting.sh01-host-resource-broker/validate-live-lease!)
                      lease (:root guard) (:resource-class guard))
                     (require! (= 0 (get-in lease [:telemetry :recovered-stale-tickets])) :stale-recovery))))
    :release! (fn [guard lease]
                (if (= :canonical (:kind guard))
                  (try (.release ^java.nio.channels.FileLock (:lock lease))
                       {:outcome :released}
                       (finally (.close ^java.nio.channels.FileChannel (:channel lease))))
                  ((requiring-resolve 'gravity.self-hosting.sh01-host-resource-broker/release!) lease)))
    :start! (fn [command cwd]
              (let [start (requiring-resolve 'gravity.self-hosting.sh01-parallel-test-runner/start-process)
                    retain (requiring-resolve 'gravity.self-hosting.sh01-parallel-test-runner/retain-owned-process!)]
                (retain (start command cwd))))
    :supervise! (fn [owned options]
                  (let [process (:process owned)
                        r ((requiring-resolve 'gravity.self-hosting.sh01-parallel-test-runner/process-result)
                           process options owned)
                        handles ((requiring-resolve 'gravity.self-hosting.sh01-parallel-test-runner/retained-process-handles) owned)
                        alive (.isAlive ^Process process)]
                    (assoc r :root-alive? alive :terminal? (not alive)
                           :retained-handle-count (count handles)
                           :cleanup-attempted? (some? (:cleanup r)))))
    :terminate! (fn [owned]
                  ((requiring-resolve 'gravity.self-hosting.sh01-parallel-test-runner/finalize-owned-process!) owned))}
   hooks))
