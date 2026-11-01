;; Inert actual-form model. No child, broker/lock acquisition or target load.
(require '[clojure.java.io :as io]
         '[clojure.test :refer [deftest is run-tests]])

(load-file "bootstrap/clojure/test/gravity/self_hosting/sh01_host_resource_broker.clj")
(require '[gravity.self-hosting.sh01-host-resource-broker :as broker])
(def broker-ns 'gravity.self-hosting.sh01-host-resource-broker)
(def quarantines (var-get (ns-resolve broker-ns 'quarantined-leases)))
(def custody-var (ns-resolve broker-ns '*process-custody*))
(def scenario (atom {}))
(def events (atom []))
(def model-ns (create-ns 'synchronous-selector-forwarding.runner-model))
(def source-path
  (or (System/getProperty "gravity.selector-forwarding-model-source")
      "bootstrap/clojure/test/gravity/self_hosting/sh01_parallel_test_runner.clj"))
(def forms
  (with-open [r (java.io.PushbackReader. (io/reader source-path))]
    (binding [*read-eval* false]
      (let [eof (Object.)]
        (loop [forms {}]
          (let [f (read {:eof eof} r)]
            (if (identical? eof f) forms
                (recur (if (and (seq? f) (#{'def 'defn 'defn- 'defrecord} (first f)))
                         (assoc forms (second f) f) forms)))))))))

(defn event! [kind]
  (swap! events conj [kind (.getId (Thread/currentThread))]))
(def complete
  {:status :passed :exit-code 0 :root-exited? true :cleanup-root-alive? false
   :tracked-owned-processes-terminal? true :descendant-coverage :unknown
   :cleanup-complete? nil :capture-failed? false
   :stdout-capture-status :complete :stderr-capture-status :complete
   :stdout-capture-error nil :stderr-capture-error nil
   :stdout-capture-forced-close? false :stderr-capture-forced-close? false
   :stdout "modeled stdout" :stderr "modeled stderr"})
(defn fake-process []
  (proxy [Process] []
    (getInputStream [] (java.io.ByteArrayInputStream. (byte-array 0)))
    (getErrorStream [] (java.io.ByteArrayInputStream. (byte-array 0)))
    (getOutputStream [] (java.io.ByteArrayOutputStream.))
    (toHandle [] (event! :fake-to-handle) nil)
    (waitFor [] (throw (AssertionError. "No wait allowed")))
    (exitValue [] (throw (AssertionError. "No exit observation allowed")))
    (isAlive [] (throw (AssertionError. "No alive observation allowed")))
    (destroy [] (throw (AssertionError. "No termination allowed")))))

(binding [*ns* model-ns]
  (clojure.core/refer 'clojure.core)
  (alias 'broker broker-ns)
  (alias 'str 'clojure.string))

(doseq [[name implementation]
        {'root (delay "/inert/default-cwd")
         'forward-child-cache-option? (constantly false)
         'require-supported-namespace! (fn [_]
                                         (event! :namespace-guard)
                                         (when-let [error (:blocked @scenario)] (throw error)))
         'start-process (fn [command cwd]
                          (event! :start)
                          (is (= (:expected-command @scenario) command))
                          (is (= (:expected-cwd @scenario) cwd))
                          (when (:expect-lease? @scenario)
                            (is (some? (var-get custody-var))))
                          (when-let [error (:start-error @scenario)] (throw error))
                          (:launched @scenario))
         'retained-process-result
         (fn [process options owned]
           (event! :result)
           (is (identical? (:launched @scenario) process))
           (is (identical? process (:process owned)))
           (is (= (.getId (Thread/currentThread)) (:owner-thread-id owned)))
           (is (identical? (:expected-options @scenario) options))
           (when-let [registry (var-get custody-var)]
             (is (= 1 (count @registry)))
             (is (identical? owned (:owned @(first @registry)))))
           (when-let [error (:result-error @scenario)] (throw error))
           (reset! (:terminal owned) {:result :modeled-terminal})
           (or (:raw-result @scenario) complete))
         'finalize-owned-process! (fn [owned]
                                    (event! :finalize)
                                    (reset! (:terminal owned) {:result :modeled-terminal})
                                    (when-let [error (:cleanup-error @scenario)] (throw error)))
         'close-stream! (fn [_] (event! :close))
         'bound-launch-result (fn [launched _] (event! :map-result) launched)
         'result-failure? (fn [_] (throw (AssertionError. "Unmodeled fallback status")))}]
  (intern model-ns name implementation))

;; Only inert data/Throwable helpers and the actual selected caller/API forms.
;; Native builder/start, wait/capture, finalization, policy discovery and broker
;; acquire/release are NOT evaluated; their ports are explicit local models.
(binding [*ns* model-ns]
  (doseq [name '[default-command namespace-name command-prefix namespace-command
                 RetainedProcess fatal-throwable? fatal-cause interrupted-throwable?
                 restore-interrupt! retain-process-error! require-retained-process!
                 retain-owned-process! process-result normalize-result run-namespace-process]]
    (assert (get forms name) name)
    (eval (get forms name))))
(def command (var-get (ns-resolve model-ns 'namespace-command)))
(def run-child (var-get (ns-resolve model-ns 'run-namespace-process)))
(def broker-options {:coordination-root "/inert/shared-root" :timeout-ms 30000})
(def prefix ["/inert/java with space" "-Xmx2g" "-cp" "/inert/test:/inert/src:/inert/core.jar"
             "clojure.main" "-m" "gravity.development-test-runner"])
(def selectors ["first-selected-var" "second-selected-var" "third-selected-var"])
(def job {:namespace 'gravity.fixture-test :resource-class :memory-heavy})
(def options {:command prefix :exact-test-vars selectors :child-fail-fast? true
              :working-directory "/inert/cwd with space" :resource-broker broker-options
              :timeout-ms 120000 :max-capture-bytes 65536 :max-capture-chars 65536})
(def expected-command
  (into prefix ["--namespace" "gravity.fixture-test"
                "--exact" "first-selected-var" "--exact" "second-selected-var"
                "--exact" "third-selected-var" "--fail-fast"]))

(defn exercise
  ([settings] (exercise job options settings))
  ([selected opts settings]
   (reset! quarantines {})
   (reset! events [])
   (reset! scenario (merge {:launched (fake-process) :expected-options opts
                            :expected-command expected-command
                            :expected-cwd (:working-directory opts)
                            :expect-lease? (contains? opts :resource-broker)} settings))
   (let [released (atom false)
         lease {:released? released :coordination-root "/inert/shared-root"}
         outcome
         (with-redefs [broker/acquire! (fn [actual class]
                                         (event! :acquire)
                                         (is (identical? (:resource-broker opts) actual))
                                         (is (= (:resource-class selected) class))
                                         (when-let [error (:acquire-error @scenario)] (throw error))
                                         lease)
                       broker/release! (fn [actual]
                                         (event! :release)
                                         (is (identical? lease actual))
                                         (is (false? @released))
                                         (reset! released true))]
           (try {:value (run-child selected opts)}
                (catch Throwable error
                  {:error error :interrupted? (.isInterrupted (Thread/currentThread))})
                (finally (Thread/interrupted))))]
     (assoc outcome :released? @released :events @events
            :quarantine (get @quarantines released)))))

(deftest exact-selector-tail-and-default-prefix-preserve-the-request
  (is (= expected-command (command (:namespace job) options)))
  (is (= ["clojure" "-M:test" "--namespace" "gravity.fixture-test"]
         (command (:namespace job))))
  (is (= (into prefix ["--namespace" "gravity.fixture-test"])
         (command (:namespace job) {:command prefix})))
  (is (= (into prefix ["--namespace" "gravity.fixture-test" "--exact" "one"])
         (command (:namespace job) {:command prefix :exact-test-vars ["one"]
                                    :child-fail-fast? false})))
  (doseq [bad [nil [] '("one") [""] [" "] [:one] ["one" nil]]]
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"nonempty vector"
                          (command (:namespace job) (assoc options :exact-test-vars bad)))))
  (doseq [bad [nil "true" 1]]
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"explicit boolean"
                          (command (:namespace job) (assoc options :child-fail-fast? bad))))))

(deftest direct-resource-owned-call-is-synchronous-and-token-bound
  (let [outcome (exercise {})
        result (:value outcome)]
    (is (= [:namespace-guard :acquire :start :fake-to-handle :result :release]
           (mapv first (:events outcome))))
    (is (= #{(.getId (Thread/currentThread))} (set (map second (:events outcome)))))
    (is (:released? outcome))
    (is (nil? (:quarantine outcome)))
    (is (= expected-command (:command result)))
    (is (= :passed (:status result)))
    (is (nil? (:cleanup-complete? result)))
    (is (= :unknown (:descendant-coverage result)))))

(deftest result-status-output-and-payload-are-not-promoted
  (doseq [raw [(assoc complete :status :failed :exit-code 7)
               (assoc complete :status :timeout :exit-code 124
                      :cleanup-complete? false :timeout-containment-unproven? true)]]
    (let [payload (Object.)
          raw (assoc raw :payload payload)
          outcome (exercise {:raw-result raw})
          result (:value outcome)]
      (is (:released? outcome))
      (is (= (:status raw) (:status result)))
      (is (= (:exit-code raw) (:exit-code result)))
      (is (= (:stdout raw) (:stdout result)))
      (is (= (:stderr raw) (:stderr result)))
      (is (identical? payload (:payload result))))))

(deftest unresolved-post-token-result-stays-in-existing-quarantine
  (doseq [raw [(assoc complete :root-exited? false)
               (assoc complete :tracked-owned-processes-terminal? false)
               (assoc complete :stdout-capture-status :capture-timeout)
               (assoc complete :stderr-capture-error "fake capture error")]]
    (let [outcome (exercise {:raw-result raw})
          record @(first @(:processes (:quarantine outcome)))]
      (is (false? (:released? outcome)))
      (is (identical? raw (:result record)))
      (is (some? (:owned record)))
      (is (not-any? #{:release} (map first (:events outcome)))))))

(deftest original-fatal-interruption-and-cleanup-errors-survive
  (doseq [error [(ex-info "fake wait/capture failure" {})
                 (InterruptedException. "fake wait interrupt")
                 (LinkageError. "fake fatal wait")]]
    (let [cleanup (ex-info "fake cleanup failure" {})
          outcome (exercise {:result-error error :cleanup-error cleanup})
          record @(first @(:processes (:quarantine outcome)))]
      (is (identical? error (:error outcome)))
      (is (identical? error (get-in record [:result :error])))
      (is (= (instance? InterruptedException error) (:interrupted? outcome)))
      (is (= [cleanup] (vec (.getSuppressed error))))
      (is (= 1 (count (filter #{:finalize} (map first (:events outcome))))))
      (is (false? (:released? outcome))))))

(deftest acquisition-and-owner-guard-refuse-before-launch
  (let [error (ex-info "fake acquisition refusal" {})
        outcome (exercise {:acquire-error error})]
    (is (identical? error (:error outcome)))
    (is (= [:namespace-guard :acquire] (mapv first (:events outcome)))))
  (let [error (ex-info "existing owner wrapper required" {:id "SH01-IMPACT-EXTERNAL-WRAPPER"})
        outcome (exercise {:blocked error})]
    (is (identical? error (:error outcome)))
    (is (= [:namespace-guard] (mapv first (:events outcome))))))

(deftest resource-options-cannot-silently-downgrade-or-accept-a-map
  (doseq [[selected opts] [[(dissoc job :resource-class) options]
                           [(assoc job :resource-class :invented) options]
                           [job (assoc options :resource-broker nil)]
                           [job (assoc options :resource-broker false)]]]
    (let [outcome (exercise selected opts {})]
      (is (= "SH01-PARALLEL-RESOURCE-CALLER" (:id (ex-data (:error outcome)))))
      (is (= [:namespace-guard] (mapv first (:events outcome))))))
  (let [outcome (exercise {:launched complete})]
    (is (= "SH01-PARALLEL-RESOURCE-CALLER" (:id (ex-data (:error outcome)))))
    (is (not-any? #{:map-result :result} (map first (:events outcome))))))

(deftest legacy-unleased-map-behavior-remains-and-startup-is-still-excluded
  (let [opts (dissoc options :resource-broker)
        outcome (exercise job opts {:launched complete})]
    (is (= :passed (get-in outcome [:value :status])))
    (is (= expected-command (get-in outcome [:value :command])))
    (is (= [:namespace-guard :start :map-result] (mapv first (:events outcome)))))
  ;; Honest base limitation: no post-token registration exists yet. This
  ;; source increment does NOT adopt the held pending-start36 candidate.
  (let [error (InterruptedException. "fake native pre-return failure")
        outcome (exercise {:start-error error})]
    (is (identical? error (:error outcome)))
    (is (:released? outcome))
    (is (nil? (:quarantine outcome)))
    (is (= [:namespace-guard :acquire :start :release] (mapv first (:events outcome))))))

(deftest token-and-process-identity-validation-is-still-the-actual-api
  (let [require-owned (var-get (ns-resolve model-ns 'require-retained-process!))
        retain (var-get (ns-resolve model-ns 'retain-owned-process!))
        process-result (var-get (ns-resolve model-ns 'process-result))
        process (fake-process)
        owned (retain process)]
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"retaining thread"
                          (require-owned (update owned :owner-thread-id inc))))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"unused retained"
                          (process-result (fake-process) {} owned)))
    (reset! (:terminal owned) {:result :already-modeled})
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"unused retained"
                          (process-result process {} owned)))))

(let [summary (run-tests 'user)]
  (assert (nil? (find-ns 'gravity.bootstrap)))
  (assert (nil? (find-ns 'gravity.self-hosting.sh01-parallel-test-runner)))
  (prn {:schema :synchronous-selector-forwarding/model-v1 :summary summary
        :source source-path :runtime (System/getProperty "java.runtime.version")
        :actual-children 0 :actual-leases 0 :actual-process-probes 0
        :target-namespaces-loaded false :Bootstrap-loaded false
        :startup-coverage :not-supplied :effective-runtime-config :not-attested})
  (reset! quarantines {})
  (when (pos? (+ (:fail summary) (:error summary))) (System/exit 1)))
