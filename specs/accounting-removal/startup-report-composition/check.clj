;; Composed source model. Native builder/start and wait/capture are inert ports.
;; No foreign checker, actual child, lease, lock, process probe or target load.
(require '[clojure.java.io :as io] '[clojure.edn :as edn] '[clojure.walk :as walk]
         '[clojure.test :refer [deftest is run-tests]])
(load-file (or (System/getProperty "gravity.composed-broker-source")
               "bootstrap/clojure/test/gravity/self_hosting/sh01_host_resource_broker.clj"))
(require '[gravity.self-hosting.sh01-host-resource-broker :as broker])
(def broker-ns 'gravity.self-hosting.sh01-host-resource-broker)
(def custody-var (ns-resolve broker-ns '*process-custody*))
(def quarantines (var-get (ns-resolve broker-ns 'quarantined-leases)))
(def terminal? (var-get (ns-resolve broker-ns 'terminal-process-custody?)))
(def settings (atom {})) (def events (atom [])) (def roots (atom []))
(defn note [label] (swap! events conj [label (.getId (Thread/currentThread))]))
(defn records [] (if-let [r (var-get custody-var)] @r []))
(defn port [label value] (note label) (when-let [error (get @settings label)] (throw error)) value)
(def source {:source-commit (apply str (repeat 40 "a")) :source-tree (apply str (repeat 40 "b"))
             :configuration-sha256 (apply str (repeat 64 "c"))})
(def selection {:namespaces ["gravity.fixture-test"] :test-vars ["gravity.fixture-test/a"] :fail-fast? false})
(def request {:id "composed-model-child" :source source :selection selection})
(def delivery {:request request :file "/inert/child.edn"})
(def machine {:schema :gravity/development-test-result-v1 :authority :non-authoritative :authoritative? false
              :selection selection :summary {:test 1 :pass 1 :fail 0 :error 0} :outcome :passed :exit-code 0})
(def raw {:status :passed :exit-code 0 :root-exited? true :cleanup-root-alive? false
          :tracked-owned-processes-terminal? true :capture-failed? false
          :stdout-capture-status :complete :stderr-capture-status :complete
          :stdout-capture-error nil :stderr-capture-error nil
          :stdout-capture-forced-close? false :stderr-capture-forced-close? false
          :cleanup-complete? nil :descendant-coverage :unknown})
(def normalized (assoc raw :development-result machine :development-result-request request
                       :development-result-binding-status :supplied-data-matched))
(def release-receipt {:schema :gravity/sh01-host-resource-non-authoritative-receipt-v1
                      :resource-class :exclusive :capacity 1 :outcome :released})
(def job {:namespace 'gravity.fixture-test :resource-class :exclusive})
(def fake-process (proxy [Process] []
                    (getInputStream [] (java.io.ByteArrayInputStream. (byte-array 0)))
                    (getErrorStream [] (java.io.ByteArrayInputStream. (byte-array 0)))
                    (toHandle [] (note :fake-handle) nil)
                    (waitFor [] (throw (AssertionError. "No actual wait")))
                    (exitValue [] (throw (AssertionError. "No exit probe")))
                    (isAlive [] (throw (AssertionError. "No alive probe")))
                    (destroy [] (throw (AssertionError. "No termination")))))
(defn forms [path]
  (with-open [r (java.io.PushbackReader. (io/reader path))]
    (binding [*read-eval* false]
      (loop [out {}]
        (let [f (read {:eof ::eof} r)]
          (if (= f ::eof) out
              (recur (if (and (seq? f) (#{'defrecord 'defn 'defn-} (first f)))
                       (assoc out (second f) f) out))))))))
(def fs (forms (or (System/getProperty "gravity.composed-runner-source")
                   "bootstrap/clojure/test/gravity/self_hosting/sh01_parallel_test_runner.clj")))
(def model-ns (create-ns 'startup-report.composed-model))
(binding [*ns* model-ns]
  (clojure.core/refer 'clojure.core) (alias 'broker broker-ns)
  (alias 'io 'clojure.java.io) (alias 'edn 'clojure.edn)
  (import 'java.nio.charset.StandardCharsets)
  (doseq [name '[RetainedProcess fatal-throwable? fatal-cause interrupted-throwable?
                 restore-interrupt! retain-process-error! require-retained-process!]] (eval (get fs name))))
(def constructor (var-get (ns-resolve model-ns '->RetainedProcess)))
(doseq [[name implementation]
        {'root (delay "/inert")
         'require-supported-namespace! (fn [_] (port :guard nil))
         'prepare-development-result (fn [_ _] (port :prepare delivery))
         'namespace-command (fn [_ _] (port :command ["inert-java"]))
         'fake-builder (fn [] (port :builder :inert-builder))
         'fake-start (fn [builder]
                       (is (= :inert-builder builder))
                       (note :native-start-port)
                       (let [record (first (records))]
                         (is (= 1 (count (records))))
                         (is (= :pending (:launch-state @record)))
                         (is (= :unknown (:creation-coverage @record)))
                         (is (nil? (:process @record))) (is (false? (terminal? record))))
                       (when-let [f (:during-start @settings)] (f))
                       (when-let [error (:start @settings)] (throw error)) fake-process)
         '->RetainedProcess (fn [& args]
                              (note :token)
                              (let [record @(first (records))]
                                (is (= :returned (:launch-state record)))
                                (is (identical? fake-process (:process record)))
                                (is (nil? (:owned record))))
                              (when-let [error (:retain @settings)] (throw error))
                              (apply constructor args))
         'retained-process-result (fn [process _ owned]
                                    (is (identical? fake-process process))
                                    (is (identical? owned (:owned @(first (records)))))
                                    (is (identical? owned (broker/retained-start-token process)))
                                    (note :capture)
                                    (when-let [error (:capture @settings)] (throw error))
                                    (let [result (get @settings :raw raw)]
                                      (reset! (:terminal owned) result) result))
         'finalize-owned-process! (fn [_] (port :finalize nil))
         'close-stream! (fn [_] (port :close nil))
         'bound-launch-result (fn [& _] (throw (AssertionError. "No map launcher")))
         'read-development-result (fn [actual result]
                                    (is (identical? delivery actual))
                                    (is (identical? (get @settings :raw raw) result))
                                    (port :read machine))
         'normalize-result (fn [_ _ _] (port :normalize (get @settings :normalized normalized)))}]
  (intern model-ns name implementation))
(def substitutions (atom {:builder 0 :start 0}))
(def modeled-start
  (walk/prewalk (fn [form]
                  (cond
                    (and (seq? form) (= '-> (first form)) (seq? (second form))
                         (= 'ProcessBuilder. (first (second form))))
                    (do (swap! substitutions update :builder inc) '(fake-builder))
                    (= '(.start builder) form) (do (swap! substitutions update :start inc) '(fake-start builder))
                    :else form)) (get fs 'start-process)))
(assert (= {:builder 1 :start 1} @substitutions))
(binding [*ns* model-ns]
  (doseq [name '[retain-owned-process!]] (eval (get fs name)))
  (eval modeled-start)
  (doseq [name '[process-result report-inside-lease! publish-parent-report! run-namespace-process]] (eval (get fs name))))
(def run-parent (var-get (ns-resolve model-ns 'run-namespace-process)))
(def retain (var-get (ns-resolve model-ns 'retain-owned-process!)))
(def process-result (var-get (ns-resolve model-ns 'process-result)))
(defn output []
  (let [p (java.nio.file.Files/createTempDirectory "composed-source-model-" (make-array java.nio.file.attribute.FileAttribute 0))]
    (swap! roots conj p) {:file (str (.resolve p "parent.edn")) :limit-bytes 65536}))
(defn exercise [configuration callback report]
  (reset! settings configuration) (reset! events []) (reset! quarantines {})
  (let [lease {:released? (atom false)} snapshot (atom nil) seen (atom [])
        opts (cond-> {:resource-broker {} :development-result source :working-directory "/inert"}
               (not= ::absent callback) (assoc :inside-lease-report!
                                               (fn [obs]
                                                 (note :inside) (reset! snapshot (records))
                                                 (swap! seen conj obs) (callback obs)))
               report (assoc :parent-report report))
        value (with-redefs [broker/acquire! (fn [& _] (port :acquire lease))
                            broker/release! (fn [actual]
                                              (is (identical? lease actual))
                                              (when-let [f (:before-release @settings)] (f))
                                              (when (:consume-interrupt @settings) (Thread/interrupted))
                                              (port :release release-receipt))]
                (try {:value (run-parent job opts)}
                     (catch Throwable error {:error error :interrupted? (.isInterrupted (Thread/currentThread))})
                     (finally (Thread/interrupted))))]
    (assoc value :seen @seen :records @snapshot :events @events
           :quarantine (get @quarantines (:released? lease)))))
(defn labels [out] (mapv first (:events out)))
(defn raw-error? [primary target]
  (or (identical? primary target)
      (some #(or (identical? % target) (identical? target (:callback-error (ex-data %))))
            (.getSuppressed ^Throwable primary))))

(deftest successful-composed-path-reuses-one-start-token-before-report-and-release
  (let [out (exercise {} identity nil) record @(first (:records out))]
    (is (identical? normalized (:value out)))
    (is (= [:guard :acquire :prepare :command :builder :native-start-port :token :fake-handle :capture :read :normalize :inside :release]
           (labels out)))
    (is (= #{(.getId (Thread/currentThread))} (set (map second (:events out)))))
    (is (= 1 (count (:records out)))) (is (= 1 (count (:seen out))))
    (is (= :returned (:launch-state record))) (is (= :unknown (:creation-coverage record)))
    (is (identical? fake-process (:process record))) (is (some? (:owned record)))
    (is (identical? raw (:result record)))
    (is (identical? request (get-in out [:seen 0 :delivery-request])))
    (is (identical? raw (get-in out [:seen 0 :process-result])))
    (is (identical? normalized (get-in out [:seen 0 :result])))))

(deftest start-and-retention-errors-stay-unknown-and-quarantine-through-callback
  (doseq [stage [:start :retain]
          error [(java.io.IOException. "model startup") (InterruptedException. "model interrupted startup")
                 (LinkageError. "model fatal startup")]]
    (let [out (exercise {stage error} identity nil) record @(first (:records out))]
      (is (identical? error (:error out))) (is (= 1 (count (:seen out))))
      (is (= :threw (get-in out [:seen 0 :tag]))) (is (nil? (get-in out [:seen 0 :result])))
      (is (not (contains? (first (:seen out)) :process-result)))
      (is (= (if (= stage :start) :start-threw :retention-threw) (:launch-state record)))
      (is (identical? error (:start-error record)))
      (is (= :unknown (:creation-coverage record))) (is (nil? (:owned record)))
      (is (= (= stage :retain) (identical? fake-process (:process record))))
      (is (some? (:quarantine out))) (is (not-any? #{:release :capture :finalize} (labels out)))
      (is (= (instance? InterruptedException error) (:interrupted? out))))))

(deftest builder-failure-is-not-attempted-but-error-report-still-precedes-disposition
  (let [error (ex-info "builder refused" {}) out (exercise {:builder error} identity nil)]
    (is (identical? error (:error out))) (is (empty? (:records out)))
    (is (= :threw (get-in out [:seen 0 :tag])))
    (is (= [:guard :acquire :prepare :command :builder :inside :release] (labels out)))))

(deftest supplied-terminal-looking-data-cannot-complete-a-pending-or-thrown-start
  (let [error (ex-info "native start still unknown" {})
        out (exercise {:during-start #(let [record (first (records))]
                                        (broker/record-process-result! record raw)
                                        (is (false? (terminal? record))))
                       :start error} identity nil)
        record @(first (:records out))]
    (is (identical? error (:error out)))
    (is (identical? raw (:result record)))
    (is (= :start-threw (:launch-state record)))
    (is (some? (:quarantine out)))
    (is (not-any? #{:release} (labels out)))))

(deftest actual-process-result-error-path-keeps-custody-and-original-cleanup-errors
  (doseq [error [(ex-info "capture setup" {}) (InterruptedException. "capture interruption") (LinkageError. "capture fatal")]]
    (let [cleanup (ex-info "modeled cleanup failure" {})
          out (exercise {:capture error :finalize cleanup} identity nil)
          record @(first (:records out))]
      (is (identical? error (:error out))) (is (raw-error? error cleanup))
      (is (identical? error (get-in record [:result :error])))
      (is (some? (:quarantine out))) (is (not-any? #{:release :read :normalize} (labels out)))
      (is (= 1 (count (filter #{:token} (labels out)))))
      (is (= 1 (count (filter #{:finalize} (labels out)))))
      (is (= 2 (count (filter #{:close} (labels out)))))
      (is (nil? (get-in out [:seen 0 :result])))
      (is (not (contains? (first (:seen out)) :process-result)))
      (is (= (instance? InterruptedException error) (:interrupted? out))))))

(deftest callback-failure-cannot-release-incomplete-startup-or-terminal-facts
  (doseq [configuration [{:start (ex-info "native throw" {})}
                         {:retain (ex-info "retention throw" {})}
                         {:raw (assoc raw :root-exited? false)}
                         {:raw (assoc raw :stdout-capture-status :error)}]]
    (let [error (ex-info "semantic sink refusal" {}) out (exercise configuration (fn [_] (throw error)) nil)]
      (is (some? (:error out))) (is (some? (:quarantine out)))
      (is (raw-error? (:error out) error)) (is (= 1 (count (:seen out))))
      (is (not-any? #{:release} (labels out))))))

(deftest completed-custody-does-not-hide-read-callback-or-release-errors
  (doseq [stage [:read :normalize nil]
          kind [:ordinary :fatal :interrupt]]
    (let [original (when stage (ex-info "normal completion processing failed" {}))
          callback (case kind :fatal (LinkageError. "callback fatal") :interrupt (InterruptedException. "callback interrupt")
                         (ex-info "callback ordinary" {}))
          release (ex-info "release partial failure" {})
          out (exercise (cond-> {:release release :consume-interrupt true} stage (assoc stage original))
                        (fn [_] (throw callback)) nil)
          expected (if (= kind :fatal) callback (or original callback))]
      (is (identical? expected (:error out))) (is (raw-error? expected callback))
      (when original (is (raw-error? expected original))) (is (raw-error? expected release))
      (is (= (= kind :interrupt) (:interrupted? out)))
      (is (= 1 (count (:seen out)))) (is (= :release (last (labels out))))
      (is (some? (:quarantine out))))))

(deftest actual-data-publisher-stays-after-composed-start-callback-and-release
  (let [target (output)
        out (exercise {:before-release #(is (not (.exists (io/file (:file target)))))}
                      (fn [_] (is (not (.exists (io/file (:file target)))))) target)
        report (get-in out [:value :parent-report :report])]
    (is (nil? (:error out))) (is (= :passed (:outcome report)))
    (is (= :released (get-in report [:resource :state])))
    (is (= report (edn/read-string (slurp (:file target)))))
    (is (= :release (last (labels out)))))
  (let [target (output) _ (spit (:file target) "retained") out (exercise {} identity target)]
    (is (some? (:error out))) (is (= :release (last (labels out))))
    (is (= 1 (count (:seen out)))) (is (= "retained" (slurp (:file target))))))

(deftest unknown-start-error-is-published-as-error-with-quarantine-not-release
  (let [target (output) original (ex-info "unknown native start" {:id "MODELED-START"})
        out (exercise {:start original} identity target)
        report (edn/read-string (slurp (:file target)))]
    (is (identical? original (:error out)))
    (is (= :error (:outcome report)))
    (is (= :quarantined (get-in report [:resource :state])))
    (is (false? (get-in report [:resource :release-attempted?])))
    (is (nil? (get-in report [:resource :receipt])))
    (is (= :unsupported-in-JVM-only (get-in report [:resource :parent-exit-custody])))
    (is (= "MODELED-START" (get-in report [:operation :error :data :id])))
    (is (not-any? #{:release} (labels out)))))

(defn refusal-id [f] (:id (ex-data (try (f) nil (catch Throwable e e)))))
(deftest token-and-thread-refusals-are-real-source-guards-in-the-composed-scope
  (let [out (exercise {}
                      (fn [_]
                        (let [record (first (records)) owned (:owned @record)
                              replacement (constructor fake-process (atom []) (atom nil) (atom nil) (atom nil) (atom nil) (.getId (Thread/currentThread)))]
                          (is (identical? owned (retain fake-process)))
                          (is (= "SH01-PROCESS-OWNERSHIP" (refusal-id #(broker/retain-process-custody! replacement))))
                          (is (= "SH01-PROCESS-OWNERSHIP" (refusal-id #(process-result fake-process {} owned))))
                          (let [other (assoc owned :owner-thread-id -1)]
                            (is (= "SH01-PROCESS-OWNERSHIP" (refusal-id #(process-result fake-process {} other))))))) nil)]
    (is (nil? (:error out))) (is (= :release (last (labels out))))))

(deftest failed-semantics-default-and-original-guard-remain-separate
  (let [failed (-> normalized (assoc :status :failed :exit-code 1)
                   (assoc-in [:development-result :outcome] :failed) (assoc-in [:development-result :exit-code] 1))
        out (exercise {:normalized failed} identity nil)]
    (is (identical? failed (:value out))) (is (= :failed (get-in out [:seen 0 :result :status])))
    (is (= :release (last (labels out)))))
  (let [out (exercise {} ::absent nil)] (is (identical? normalized (:value out))) (is (empty? (:seen out))))
  (doseq [stage [:guard :acquire]]
    (let [error (ex-info "source policy refusal" {}) out (exercise {stage error} identity nil)]
      (is (identical? error (:error out))) (is (empty? (:seen out)))
      (is (not-any? #{:native-start-port :token :capture :release} (labels out))))))

(let [summary (run-tests 'user)]
  (doseq [root @roots]
    (with-open [paths (java.nio.file.Files/walk root (make-array java.nio.file.FileVisitOption 0))]
      (doseq [path (reverse (sort-by #(.getNameCount ^java.nio.file.Path %) (iterator-seq (.iterator paths))))]
        (java.nio.file.Files/deleteIfExists path))))
  (reset! quarantines {})
  (assert (nil? (find-ns 'gravity.self-hosting.sh01-parallel-test-runner)))
  (assert (nil? (find-ns 'gravity.bootstrap)))
  (prn {:schema :startup-report-composition/model-v1 :summary summary :native-substitutions @substitutions
        :actual-children 0 :actual-leases 0 :actual-probes 0 :native-pre-return-creation :UNKNOWN
        :native-startup-bound :UNKNOWN :parent-exit-custody :unsupported-in-JVM-only})
  (when (pos? (+ (:fail summary) (:error summary))) (System/exit 1)))
