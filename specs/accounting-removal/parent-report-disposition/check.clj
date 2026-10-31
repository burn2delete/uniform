;; Inert resource models and owned DATA IO. No real lease/process/target load.
(require '[clojure.java.io :as io] '[clojure.edn :as edn]
         '[clojure.test :refer [deftest is run-tests]])
(load-file (or (System/getProperty "gravity.parent-report-broker-source")
               "bootstrap/clojure/test/gravity/self_hosting/sh01_host_resource_broker.clj"))
(require '[gravity.self-hosting.sh01-host-resource-broker :as broker])
(def broker-ns 'gravity.self-hosting.sh01-host-resource-broker)
(def quarantines (var-get (ns-resolve broker-ns 'quarantined-leases)))
(def events (atom []))
(def roots (atom []))
(def normal-source {:source-commit (apply str (repeat 40 "a"))
                    :source-tree (apply str (repeat 40 "b"))
                    :configuration-sha256 (apply str (repeat 64 "c")) :limit-bytes 65536})
(def selection {:namespaces ["gravity.fixture-test"] :test-vars ["gravity.fixture-test/a"] :fail-fast? false})
(def complete {:status :passed :exit-code 0 :root-exited? true :cleanup-root-alive? false
               :tracked-owned-processes-terminal? true :capture-failed? false
               :stdout-capture-status :complete :stderr-capture-status :complete
               :stdout-capture-error nil :stderr-capture-error nil
               :stdout-capture-forced-close? false :stderr-capture-forced-close? false
               :cleanup-complete? nil :descendant-coverage :unknown
               :development-result-binding-status :supplied-data-matched
               :development-result-request {:id "modeled-child" :source (dissoc normal-source :limit-bytes)
                                            :selection selection}
               :development-result {:schema :gravity/development-test-result-v1 :authority :non-authoritative
                                    :authoritative? false :selection selection
                                    :summary {:test 1 :pass 1 :fail 0 :error 0} :outcome :passed :exit-code 0}})
(def release-receipt {:schema :gravity/sh01-host-resource-non-authoritative-receipt-v1
                      :resource-class :memory-heavy :capacity 1 :outcome :released :diagnostic-id nil})
(defn exercise [settings operation observer]
  (reset! quarantines {}) (reset! events [])
  (let [lease {:released? (atom false) :coordination-root "/inert/root"}
        seen (atom nil)
        outcome (with-redefs [broker/acquire! (fn [_ class]
                                                (swap! events conj [:acquire (.getId (Thread/currentThread))])
                                                (is (= :memory-heavy class))
                                                (when-let [error (:acquire-error settings)] (throw error)) lease)
                              broker/release! (fn [actual]
                                                (swap! events conj [:release (.getId (Thread/currentThread))])
                                                (is (identical? lease actual))
                                                (when-let [error (:release-error settings)] (throw error))
                                                (reset! (:released? actual) true) release-receipt)]
                  (try {:value (broker/with-lease
                                 {} :memory-heavy operation
                                 #(do (swap! events conj [:observe (.getId (Thread/currentThread))])
                                      (reset! seen %) (observer %)))}
                       (catch Throwable error {:error error :interrupted? (.isInterrupted (Thread/currentThread))})
                       (finally (Thread/interrupted))))]
    (assoc outcome :observation @seen :lease lease :events @events
           :quarantine (get @quarantines (:released? lease)))))
(defn operation [result error]
  (fn []
    (swap! events conj [:operation (.getId (Thread/currentThread))])
    (let [owned {:modeled-token true}
          custody (broker/retain-process-custody! owned)]
      (broker/record-process-result! custody result)
      (when error (throw error)) result)))
(defn forms [path]
  (with-open [r (java.io.PushbackReader. (io/reader path))]
    (binding [*read-eval* false]
      (let [eof (Object.)]
        (loop [out {}]
          (let [f (read {:eof eof} r)]
            (if (identical? eof f) out
                (recur (if (and (seq? f) (#{'defn 'defn-} (first f)))
                         (assoc out (second f) f) out)))))))))
(def parent-forms
  (forms (or (System/getProperty "gravity.parent-report-parent-source")
             "bootstrap/clojure/test/gravity/self_hosting/sh01_parallel_test_runner.clj")))
(def report-ns (create-ns 'parent-report.publisher-model))
(binding [*ns* report-ns]
  (clojure.core/refer 'clojure.core) (alias 'io 'clojure.java.io) (alias 'edn 'clojure.edn)
  (import 'java.nio.charset.StandardCharsets)
  (eval (get parent-forms 'publish-parent-report!)))
(def publish (var-get (ns-resolve report-ns 'publish-parent-report!)))
(def job {:namespace 'gravity.fixture-test :resource-class :memory-heavy})
(defn output-options []
  (let [root (java.nio.file.Files/createTempDirectory "parent-report-model-" (make-array java.nio.file.attribute.FileAttribute 0))]
    (swap! roots conj root)
    {:development-result normal-source :parent-report {:file (str (.resolve root "parent.edn")) :limit-bytes 65536}}))
(defn report-observer [options publication]
  #(reset! publication (publish job options "modeled-parent-request" %)))

;; Existing normal caller wiring, with already accepted child/wait/normalizer
;; dependencies modeled. The new observed arity and publisher remain actual.
(binding [*ns* report-ns] (alias 'broker broker-ns))
(doseq [[name implementation]
        {'root (delay "/inert/root")
         'require-supported-namespace! (fn [_] (swap! events conj [:guard]))
         'prepare-development-result (constantly nil)
         'namespace-command (fn [_ _] ["inert-java"])
         'start-process (fn [_ _]
                          (proxy [Process] []
                            (waitFor [] (throw (AssertionError. "No wait")))
                            (exitValue [] (throw (AssertionError. "No probe")))
                            (destroy [] (throw (AssertionError. "No terminate")))))
         'process-result (fn [_ _]
                           (let [custody (broker/retain-process-custody! {:modeled-token true})]
                             (broker/record-process-result! custody complete)))
         'bound-launch-result (fn [x _] x)
         'read-development-result (fn [& _] (throw (AssertionError. "Accepted delivery port not used")))
         'normalize-result (fn [_ value _] value)}]
  (intern report-ns name implementation))
(binding [*ns* report-ns] (eval (get parent-forms 'run-namespace-process)))
(def run-parent (var-get (ns-resolve report-ns 'run-namespace-process)))

(deftest actual-source-order-and-release-receipt-preserve-result
  (let [outcome (exercise {} (operation complete nil) identity)]
    (is (identical? complete (:value outcome)))
    (is (= [:acquire :operation :release :observe] (mapv first (:events outcome))))
    (is (= #{(.getId (Thread/currentThread))} (set (map second (:events outcome)))))
    (is (identical? (:lease outcome) (get-in outcome [:observation :lease])))
    (is (identical? release-receipt (get-in outcome [:observation :disposition :receipt])))))

(deftest quarantine-and-release-failure-have-honest-lifetime
  (doseq [raw [(assoc complete :root-exited? false) (assoc complete :stdout-capture-status :error)]]
    (let [outcome (exercise {} (operation raw nil) identity)]
      (is (identical? raw (:value outcome)))
      (is (= :quarantined (get-in outcome [:observation :disposition :state])))
      (is (false? (get-in outcome [:observation :disposition :release-attempted?])))
      (is (= :unsupported-in-JVM-only (get-in outcome [:observation :disposition :parent-exit-custody])))
      (is (identical? (:lease outcome) (get-in outcome [:quarantine :lease])))
      (is (not-any? #{:release} (map first (:events outcome))))))
  (let [error (java.io.IOException. "modeled partial release failure")
        outcome (exercise {:release-error error} (operation complete nil) identity)]
    (is (identical? error (:error outcome)))
    (is (= :release-failed (get-in outcome [:observation :disposition :state])))
    (is (true? (get-in outcome [:observation :disposition :release-attempted?])))
    (is (identical? error (get-in outcome [:quarantine :release-error])))))

(deftest all-errors-observed-after-disposition-with-original-identity
  (doseq [[original release publish-error expected]
          (let [ordinary (ex-info "operation error" {}) release (java.io.IOException. "release error")
                fatal (LinkageError. "fatal disposition") interrupt (InterruptedException. "operation interrupt")]
            [[ordinary release (java.io.IOException. "publication error") ordinary]
             [ordinary fatal nil fatal] [interrupt release nil interrupt]
             [ordinary release fatal fatal] [nil nil ordinary ordinary]])]
    (let [outcome (exercise {:release-error release} (operation complete original)
                            (fn [_] (when publish-error (throw publish-error))))]
      (is (identical? expected (:error outcome)))
      (is (identical? original (get-in outcome [:observation :operation :error])))
      (is (= [:acquire :operation :release :observe] (mapv first (:events outcome))))
      (is (= (instance? InterruptedException original) (:interrupted? outcome)))
      (is (some #(= "SH01-BROKER-DISPOSITION" (:id (ex-data %))) (.getSuppressed expected)))))
  (let [error (ex-info "acquisition refused" {})
        outcome (exercise {:acquire-error error} (fn [] (throw (AssertionError. "Must not run"))) identity)]
    (is (identical? error (:error outcome)))
    (is (= :acquire-failed (get-in outcome [:observation :disposition :state])))
    (is (= [:acquire :observe] (mapv first (:events outcome))))))

(deftest complete-failed-semantics-remain-failed-despite-release
  (let [raw (-> complete (assoc :status :failed :exit-code 1)
                (assoc-in [:development-result :outcome] :failed)
                (assoc-in [:development-result :exit-code] 1)
                (assoc-in [:development-result :summary :fail] 1))
        options (output-options) publication (atom nil)
        outcome (exercise {} (operation raw nil) (report-observer options publication))
        report (:report @publication)]
    (is (identical? raw (:value outcome)))
    (is (= :released (get-in report [:resource :state])))
    (is (= :failed (:outcome report)))
    (is (= raw (get-in report [:operation :result])))
    (is (= report (edn/read-string (slurp (get-in options [:parent-report :file])))))))

(deftest rejected-report-and-publication-failure-never-skip-disposition
  (let [original (ex-info "child report refused" {:id "SH01-DEVELOPMENT-RESULT" :raw :retained})
        options (output-options) publication (atom nil)
        outcome (exercise {} (operation complete original) (report-observer options publication))]
    (is (identical? original (:error outcome)))
    (is (= :error (get-in @publication [:report :outcome])))
    (is (= :released (get-in @publication [:report :resource :state])))
    (is (= "SH01-DEVELOPMENT-RESULT" (get-in @publication [:report :operation :error :data :id]))))
  (doseq [limit? [true false]]
    (let [options (output-options)
          options (if limit? (assoc-in options [:parent-report :limit-bytes] 1) options)
          _ (when-not limit? (spit (get-in options [:parent-report :file]) "unchanged"))
          outcome (exercise {} (operation complete nil) (report-observer options (atom nil)))]
      (is (some? (:error outcome)))
      (is (= :released (get-in outcome [:observation :disposition :state])))
      (is (= [:acquire :operation :release :observe] (mapv first (:events outcome))))
      (when-not limit? (is (= "unchanged" (slurp (get-in options [:parent-report :file])))))))
  (let [options (output-options)
        raw (dissoc complete :development-result-binding-status)
        outcome (exercise {} (operation raw nil) (report-observer options (atom nil)))]
    (is (= "SH01-PARENT-REPORT" (:id (ex-data (:error outcome)))))
    (is (= :released (get-in outcome [:observation :disposition :state])))))

(deftest no-false-release-and-quarantine-publication-is-not-durable-custody
  (let [options (output-options) publication (atom nil)
        outcome (exercise {} (operation (assoc complete :root-exited? false) nil)
                          (report-observer options publication))]
    (is (= :unresolved (get-in @publication [:report :outcome])))
    (is (= :quarantined (get-in @publication [:report :resource :state])))
    (is (= :unsupported-in-JVM-only (get-in @publication [:report :resource :parent-exit-custody])))
    (is (some? (:quarantine outcome)))))

(deftest assessment-failure-is-retained-without-release
  (let [terminal-var (ns-resolve broker-ns 'terminal-process-custody?)
        error (ex-info "modeled terminal assessment failure" {})
        outcome (with-redefs-fn {terminal-var (fn [_] (throw error))}
                  #(exercise {} (operation complete nil) identity))]
    (is (identical? error (:error outcome)))
    (is (= :quarantined (get-in outcome [:observation :disposition :state])))
    (is (identical? error (get-in outcome [:quarantine :assessment-error])))
    (is (not-any? #{:release} (map first (:events outcome))))))

(deftest final-closed-report-rejects-contradictory-observation-before-creation
  (let [snapshot (:observation (exercise {} (operation complete nil) identity))]
    (doseq [changed [(assoc-in snapshot [:disposition :release-attempted?] false)
                     (assoc-in snapshot [:disposition :receipt :outcome] :quarantined)
                     (assoc-in snapshot [:disposition :receipt :resource-class] :normal)
                     (assoc-in snapshot [:disposition :state] :unknown)
                     (assoc-in snapshot [:operation :returned?] false)
                     (assoc-in snapshot [:operation :value :development-result-request :source :source-tree] "wrong")]]
      (let [options (output-options)
            error (try (publish job options "model-request" changed) nil (catch Throwable error error))]
        (is (= "SH01-PARENT-REPORT" (:id (ex-data error))))
        (is (not (.exists (io/file (get-in options [:parent-report :file])))))))))

(deftest existing-normal-caller-consumes-final-disposition
  (reset! quarantines {})
  (let [options (assoc (output-options) :resource-broker {} :working-directory "/inert/root")
        lease {:released? (atom false)}
        released? (atom false)
        result (with-redefs [broker/acquire! (fn [_ _] lease)
                             broker/release! (fn [actual]
                                               (is (identical? lease actual))
                                               (reset! released? true) release-receipt)]
                 (run-parent job options))
        report (get-in result [:parent-report :report])]
    (is @released?)
    (is (= :passed (:outcome report)))
    (is (= :released (get-in report [:resource :state])))
    (is (= #{:schema :authority :authoritative? :request :operation :resource :outcome}
           (set (keys report))))
    (is (= complete (select-keys result (keys complete))))
    (is (= report (edn/read-string (slurp (get-in options [:parent-report :file])))))))

(let [summary (run-tests 'user)]
  (doseq [root @roots]
    (with-open [paths (java.nio.file.Files/walk root (make-array java.nio.file.FileVisitOption 0))]
      (doseq [path (reverse (sort-by #(.getNameCount ^java.nio.file.Path %) (iterator-seq (.iterator paths))))]
        (java.nio.file.Files/deleteIfExists path))))
  (reset! quarantines {})
  (assert (nil? (find-ns 'gravity.self-hosting.sh01-parallel-test-runner)))
  (assert (nil? (find-ns 'gravity.bootstrap)))
  (prn {:schema :parent-report-disposition/model-v1 :summary summary
        :actual-leases 0 :actual-children 0 :actual-probes 0 :runtime (System/getProperty "java.runtime.version")
        :real-final-receipt :UNRUN :unresolved-parent-exit :unsupported})
  (when (pos? (+ (:fail summary) (:error summary))) (System/exit 1)))
