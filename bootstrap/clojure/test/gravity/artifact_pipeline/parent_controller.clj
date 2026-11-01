(ns gravity.artifact-pipeline.parent-controller
  "Exact OWN positive orchestration. Loading this namespace opens no real port.
  Admission/root authority and owned-process finalization are explicit inputs;
  this controller cannot mint either from saved DATA or default directories."
  (:refer-clojure :exclude [run!]))

(def selector "gravity.self-hosting.authenticated-artifact-pipeline-test/real-owner-gravity-prefix-passes-fresh-replay")
(def source {:commit "05e9f8332c6faebb3fd0ada762573d643349ad96"
             :tree "3d115ebedfcd23078d3981a43bb1453c10c30095"
             :inventory "9e7b6053469286fc42701adb7c9b859e1dad36b1b0fce2048db711436e8c7bc7"})
(def argv ["clojure" "-J-Xmx2g" "-M:dev-test" "--namespace"
           "gravity.self-hosting.authenticated-artifact-pipeline-test" "--exact"
           "real-owner-gravity-prefix-passes-fresh-replay" "--fail-fast"])
(def port-names #{:clock :admit :reserve :acquire :validate :release :start :supervise :finish :observe :publish})
(def identity-fields [:id :source :selector :argv :cwd :runtime :config :port-identities :outputs :limits :guards :coverage])

(defn- require! [ok reason]
  (when-not ok (throw (ex-info "OWN parent controller refused" {:reason reason}))))

(defn bind-controller
  "Capture the actual port function values alongside their source-bound IDs.
  The caller must authenticate those IDs/values and the observations through
  its existing owner protocol. Binding is preparation, not execution authority."
  [request ports]
  (require! (and (string? (:id request)) (seq (:id request))) :missing-request-id)
  (require! (and (= source (:source request)) (= selector (:selector request)) (= argv (:argv request))) :wrong-operation)
  (require! (and (= port-names (set (keys ports))) (every? fn? (vals ports))
                 (= port-names (set (keys (:port-identities request))))
                 (every? some? (vals (:port-identities request)))) :unbound-ports)
  {:request request :ports ports :bound-ports ports})

(defn- missing [request]
  (let [coverage (:coverage request)]
    (cond-> []
      (nil? (:config request))
      (conj :source-bound-owner-configuration)
      (not (and (map? coverage) (:authority-source coverage) (:decision-id coverage)
                (seq (:participants coverage)) (= (:participants coverage) (:covered-participants coverage))))
      (conj :authoritative-participant-root-coverage)
      (not (and (vector? (:guards request)) (vector? (:ordered-guards coverage))
                (= #{:broker :exclusive :canonical} (set (map :kind (:guards request))))
                (= 3 (count (:guards request)))
                (every? #(and (:root %) (:source-id %)) (:guards request))
                (= (:guards request) (:ordered-guards coverage))))
      (conj :source-bound-ordered-guards)
      (not (and (string? (:cwd request)) (.startsWith ^String (:cwd request) "/")
                (map? (:runtime request))
                (every? #(and (vector? %) (seq %)) (map (:runtime request) [:classpath :jars :executables]))
                (every? some? (map (:runtime request) [:classpath :jars :executables :configuration]))))
      (conj :runtime-configuration)
      (not (and (= #{:attempt :stdout :stderr :final} (set (keys (:outputs request))))
                (every? #(and (string? %) (seq %)) (vals (:outputs request)))
                (= 4 (count (set (vals (:outputs request)))))))
      (conj :unique-output-identities))))

(defn- terminal-pass? [result terminal]
  (and (= :passed (:status result)) (= 0 (:exit-code result))
       (every? false? (map result [:wait-interrupted? :output-truncated? :capture-failed? :cleanup-failed?
                                   :cleanup-root-alive? :timeout-containment-unproven?]))
       (= :complete (:status terminal)) (true? (:owned-descendants-complete? terminal))
       (false? (:root-alive? terminal))))

(defn run!
  "Run at most one child and publish at most one final receipt. observe must
  return the same authenticated identity projection supplied before entry.
  finish finalizes the original process ownership once, consuming any runner
  cleanup receipt; it must never repeat an unknown/already attempted cleanup."
  [{:keys [request ports bound-ports]} observation]
  ;; No clock, source observer, admission or other port precedes identity checks.
  (require! (= (select-keys request identity-fields) observation) :identity-mismatch)
  (require! (and (= port-names (set (keys ports)))
                 (every? #(identical? (get ports %) (get bound-ports %)) port-names)) :port-binding-mismatch)
  (let [unbound (missing request)]
    (if (seq unbound)
      {:status :refused :reason :unbound-configuration :missing unbound}
      (let [{:keys [clock admit reserve acquire validate release start supervise finish observe publish]} ports
            limits (:limits request)
            _ (require! (every? pos-int? (map limits [:whole-ms :capture-bytes :capture-chars :capture-wait-ms :cleanup-ms])) :invalid-limits)
            deadline (+ (clock) (:whole-ms limits))
            remaining #(let [left (- deadline (clock))]
                         (require! (pos? left) :whole-deadline) left)
            held (atom []) process (atom nil) result (atom nil) terminal (atom nil) reserved? (atom false)
            publication (atom :not-attempted)
            primary (atom nil) secondary (atom []) releases (atom []) raw-releases (atom [])
            release-attempts (atom []) interrupted (atom false)
            record-error! (fn [error]
                            (when (or (instance? InterruptedException error)
                                      (instance? InterruptedException (.getCause ^Throwable error))
                                      (= "SH01-BROKER-INTERRUPTED" (:id (ex-data error)))
                                      (.isInterrupted (Thread/currentThread)))
                              (reset! interrupted true))
                            (if @primary (swap! secondary conj error) (reset! primary error)))
            metadata-view (fn [raw fields]
                            (try
                              (let [view (select-keys raw fields)]
                                (require! (every? #(or (nil? %) (string? %) (keyword? %) (boolean? %) (integer? %))
                                                  (vals view)) :unsupported-receipt-metadata)
                                ;; Only controller-declared receipt counters
                                ;; are normalized; raw owner results stay exact.
                                (reduce-kv (fn [out key value]
                                             (assoc out key
                                                    (if (and (#{:exit-code :stdout-bytes :stderr-bytes :capacity} key)
                                                             (integer? value))
                                                      (do (require! (<= Long/MIN_VALUE value Long/MAX_VALUE) :receipt-counter-representation)
                                                          (long value))
                                                      value))) {} view))
                              (catch Throwable error (record-error! error) nil)))]
        (try
          (try
            ;; This is the existing owner authority port, not acceptance of an
            ;; :accepted label in coverage DATA. Missing authority refuses here.
            (require! (true? (admit request observation (remaining))) :admission-refused)
            ;; CREATE_NEW/same-purpose reservation must be atomic in this port.
            (require! (true? (reserve (:outputs request) (remaining))) :outputs-already-reserved)
            (reset! reserved? true)
            (doseq [guard (:guards request)]
              (let [lease (acquire guard (remaining))]
                (require! (some? lease) :guard-acquisition-refused)
                (swap! held conj [guard lease])
                (require! (identical? lease (validate guard lease)) :invalid-live-guard)))
            (reset! process (start (:argv request) (:cwd request) (remaining)))
            (require! (some? @process) :missing-process)
            ;; Startup time is charged before supervision receives its budget.
            (reset! result (supervise @process (assoc limits :timeout-ms (remaining))))
            (when (true? (:wait-interrupted? @result)) (reset! interrupted true))
            (when (instance? Throwable (:wait-error @result)) (record-error! (:wait-error @result)))
            (remaining)
            (catch Throwable error (record-error! error))
            (finally
              (when @process
                (try (reset! terminal (finish @process @result @primary
                                              {:deadline-ms deadline :cleanup-ms (:cleanup-ms limits) :clock clock}))
                     (catch Throwable error (record-error! error))))
              (when @process
                (try
                  (require! (terminal-pass? @result @terminal) :nonpassing-process-or-cleanup)
                  (require! (= observation (observe)) :source-or-runtime-drift)
                  (remaining)
                  (catch Throwable error (record-error! error))))
              (doseq [[guard lease] (reverse @held)]
                (let [attempt (atom {:guard guard :lease lease :status :unknown})]
                  (try
                    (let [receipt (release guard lease)]
                      (swap! attempt assoc :result receipt)
                      (swap! raw-releases conj receipt)
                      (require! (= :released (:status receipt)) :release-not-finalized)
                      (swap! attempt assoc :status :released))
                    (catch Throwable error
                      (swap! attempt assoc :error error)
                      (record-error! error))
                    (finally
                      (swap! release-attempts conj @attempt)
                      (swap! releases conj
                             (assoc (metadata-view (:result @attempt) [:evidence-id])
                                    :status (:status @attempt) :guard-kind (:kind guard)
                                    :broker-receipt (metadata-view (get-in @attempt [:result :broker-receipt])
                                                                   [:schema :resource-class :capacity :outcome :diagnostic-id])))))))
              (when-not @primary
                (try (remaining) (catch Throwable error (record-error! error))))))
          (let [process-metadata (metadata-view @result [:status :exit-code :wait-interrupted? :output-truncated? :capture-failed?
                                                         :cleanup-failed? :cleanup-root-alive?
                                                         :timeout-containment-unproven? :stdout :stderr
                                                         :stdout-bytes :stderr-bytes])
                terminal-metadata (metadata-view @terminal [:status :owned-descendants-complete? :root-alive? :evidence-id])
                receipt {:request-id (:id request) :source (:source request) :selector (:selector request)
                         :status (if @primary :failed :passed)
                         :process-result process-metadata :terminal terminal-metadata
                         :releases @releases
                         ;; Opaque leases, owner products and Throwables never
                         ;; enter this metadata projection or any artifact codec.
                         :errors (mapv #(hash-map :class (.getName (class %)) :message (.getMessage ^Throwable %))
                                       (if @primary (into [@primary] @secondary) @secondary))}]
            ;; A publication error is not retried or followed by a second write.
            (when @reserved?
              (reset! publication :unknown)
              (try
                (require! (= :published (:status (publish (:final (:outputs request)) receipt))) :publication-unknown)
                (remaining)
                (reset! publication :published)
                (catch Throwable error (record-error! error))))
            {:status (if @primary :failed :passed) :error @primary :secondary-errors @secondary
             :publication @publication :receipt receipt
             :raw-process-result @result :raw-terminal @terminal :raw-releases @raw-releases
             :raw-release-attempts @release-attempts})
          (finally (when @interrupted (.interrupt (Thread/currentThread)))))))))

(defn normal-process-bindings
  "Only function resolution for a later admitted owner controller. Never call
  during source controls. No guard/root/finalizer/admission default is supplied."
  []
  {:broker-acquire (var-get (requiring-resolve 'gravity.self-hosting.sh01-host-resource-broker/acquire!))
   :broker-release (var-get (requiring-resolve 'gravity.self-hosting.sh01-host-resource-broker/release!))
   :broker-validate (var-get (requiring-resolve 'gravity.self-hosting.sh01-host-resource-broker/validate-live-lease!))
   :start (var-get (requiring-resolve 'gravity.self-hosting.sh01-parallel-test-runner/start-process))
   :supervise (var-get (ns-resolve 'gravity.self-hosting.sh01-parallel-test-runner 'process-result))})
