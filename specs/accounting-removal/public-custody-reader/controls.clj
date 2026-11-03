(require '[clojure.test :refer [deftest is run-tests]] '[clojure.java.io :as io]
         '[clojure.set :as set]
         '[gravity.self-hosting.sh01-host-resource-broker :as broker]
         '[gravity.self-hosting.sh01-parallel-test-runner :as parallel]
         '[gravity.self-hosting.sh01-operation-fence :as fence])
(import '(java.nio.file Files OpenOption StandardOpenOption) '(java.nio.file.attribute FileAttribute)
        '(java.security MessageDigest))
(defn bv [s] (ns-resolve 'gravity.self-hosting.sh01-host-resource-broker s))
(defn fv [s] (ns-resolve 'gravity.self-hosting.sh01-operation-fence s))
(defn sha [bytes] (format "%064x" (java.math.BigInteger. 1 (.digest (MessageDigest/getInstance "SHA-256") bytes))))
(defn forbidden [& _] (throw (Exception. "Forbidden actual resource port")))
(def ports (into {} (for [s '[acquire! acquire-lease! release! with-admission-lock trusted-root
                              prepare-root! create-ticket! try-slot try-admit!]] [(bv s) forbidden])))
(defn forms [p] (binding [*read-eval* false]
  (with-open [r (java.io.PushbackReader. (io/reader p))]
    (loop [out []] (let [f (read {:eof ::end} r)] (if (= ::end f) out (recur (conj out f))))))))
(defn local! [config]
  (let [seen (atom nil) events (atom []) error (:error config) value (Object.)
        out (with-redefs-fn ports #(try
          (broker/with-process-custody
            (fn [] (swap! events conj :operation)
              (when (:pending config) (broker/retain-process-start-custody!))
              (when error (throw error)) value)
            (fn [_] (swap! events conj :finalize)
              (when (:late config) (try (broker/retain-process-start-custody!) (catch Throwable _ nil)))
              {:state :not-acquired :release-attempted? false :custody nil})
            (fn [o] (swap! events conj :observe) (reset! seen o)
              (is (nil? @(:completion o)))
              (when (:late-observer config)
                (try (broker/retain-process-start-custody!) (catch Throwable _ nil)))))
          (catch Throwable e e)))
        interrupted (.isInterrupted (Thread/currentThread))]
    (Thread/interrupted)
    {:out out :value value :events @events :interrupted interrupted
     :final (some-> @seen :completion deref)}))
(deftest actual-loaded-public-origins-and-missing-selected-export
  (doseq [resource ["gravity/self_hosting/sh01_parallel_test_runner.clj"
                   "gravity/self_hosting/sh01_host_resource_broker.clj"
                   "gravity/self_hosting/sh01_operation_fence.clj" "acc08_fence.clj"]]
    (let [urls (vec (enumeration-seq (.getResources (clojure.lang.RT/baseLoader) resource)))
          expected (.toURL (.toURI (io/file "bootstrap/clojure/test" resource)))]
      (is (= [expected] urls))))
  (is (nil? (ns-resolve 'gravity.self-hosting.sh01-parallel-test-runner 'run-selected-r5-process)))
  (is (= '([command working-directory]) (:arglists (meta #'parallel/start-process))))
  (is (nil? (ns-resolve 'gravity.self-hosting.sh01-host-resource-broker 'with-lease-domains)))
  (let [fs (forms "bootstrap/clojure/test/gravity/self_hosting/sh01_host_resource_broker.clj")
        excluded '#{prepare-admission! r5-domain-pair joint-custodies joint-lock-call! with-joint-locks!
                    joint-budget! rollback-joint-slots! reserve-joint! joint-attempt! acquire-r5-domains!
                    dispose-joint-domains! with-lease-domains}]
    (is (empty? (set/intersection excluded (set (filter symbol? (tree-seq coll? seq fs)))))))
  (is (empty? @(fv 'declarations))))
(deftest local-success-unknown-phase-and-original-interruption
  (let [r (local! {})] (is (identical? (:value r) (:out r)))
    (is (= [:operation :finalize :observe] (:events r)))
    (is (= :complete (get-in r [:final :local-disposition]))))
  (doseq [config [{:pending true} {:late true} {:late-observer true}]]
    (is (= :quarantined (get-in (local! config) [:final :local-disposition]))))
  (doseq [error [(Exception. "original") (LinkageError. "fatal") (InterruptedException. "interrupt")]]
    (let [r (local! {:error error})]
      (is (identical? error (:out r)))
      (when (instance? InterruptedException error) (is (:interrupted r))))))

(deftest observer-cannot-rewrite-private-local-ledger
  (let [thread-id (.getId (Thread/currentThread))
        process (proxy [java.lang.Process] []
                  (getInputStream [] (java.io.ByteArrayInputStream. (byte-array 0)))
                  (getErrorStream [] (java.io.ByteArrayInputStream. (byte-array 0)))
                  (getOutputStream [] (java.io.ByteArrayOutputStream.))
                  (waitFor [] (throw (AssertionError. "No actual wait")))
                  (exitValue [] (throw (AssertionError. "No exit probe")))
                  (destroy [] (throw (AssertionError. "No termination"))))
        terminal-result {:status :passed :exit-code 0
                         :root-exited? true :cleanup-root-alive? false
                         :tracked-owned-processes-terminal? true
                         :descendant-coverage :unknown :capture-failed? false
                         :stdout-capture-status :complete
                         :stderr-capture-status :complete
                         :stdout-capture-error nil :stderr-capture-error nil
                         :stdout-capture-forced-close? false
                         :stderr-capture-forced-close? false}
        observed (atom nil)
        thrown (try
                 (broker/with-process-custody
                   (fn []
                     (let [custody (broker/retain-process-start-custody!)
                           owned {:process process :owner-thread-id thread-id}]
                       (broker/record-started-process! custody process)
                       (broker/record-started-ownership! custody owned)
                       (broker/record-process-result! custody terminal-result)
                       :ok))
                   (fn [_]
                     {:state :not-acquired :release-attempted? false
                      :error nil :custody nil})
                   (fn [view]
                     (reset! observed view)
                     ;; This is the old attack: first trigger a sealed-phase
                     ;; violation, then clear it through any mutable process
                     ;; record/lifecycle that an observer was handed.
                     (try (broker/retain-process-start-custody!)
                          (catch Throwable _ nil))
                     (let [public-processes (:processes view)
                           records (if (instance? clojure.lang.IAtom public-processes)
                                     @public-processes public-processes)]
                       (doseq [record records]
                         (when (instance? clojure.lang.IAtom record)
                           (when-let [ledger (:local-lifecycle @record)]
                             (swap! ledger assoc :violations [])))))))
                 nil
                 (catch Throwable error error))
        completion @(:completion @observed)]
    (is (instance? clojure.lang.ExceptionInfo thrown))
    (is (= "SH01-PROCESS-PHASE" (:id (ex-data thrown))))
    (is (= :quarantined (:local-disposition completion)))
    (is (seq (:phase-violations completion)))
    (is (vector? (:processes @observed)))
    (is (not-any? #(instance? clojure.lang.IAtom %) (:processes @observed)))
    (is (not-any? #(contains? % :local-lifecycle) (:processes @observed)))
    (is (not-any? #(instance? clojure.lang.IAtom %)
                  (get-in @observed [:terminal :snapshot])))
    (is (not (contains? (:owned-resources @observed) :custody)))))

(deftest legacy-lease-still-releases-after-ordinary-error
  (let [events (atom []) error (Exception. "original")]
    (with-redefs-fn (assoc ports (bv 'acquire!) (fn [& _] (swap! events conj :acquire) :lease)
                                (bv 'release!) (fn [_] (swap! events conj :release)))
      #(is (identical? error (try (broker/with-lease {} :normal (fn [] (throw error))) (catch Throwable e e)))))
    (is (= [:acquire :release] @events))))
(deftest catalog-is-deferred-and-reader-refuses-content-mismatch
  (let [v (ns-resolve 'gravity.self-hosting-test-runner 'explicitly-selectable-test-namespaces)]
    (is (instance? clojure.lang.Delay @v)) (is (not (realized? @v))))
  (let [directory (Files/createTempDirectory "public-reader-inert-" (make-array FileAttribute 0))
        file (.resolve directory "data.bin") bytes (.getBytes "inert-data" java.nio.charset.StandardCharsets/UTF_8)
        _ (Files/write file bytes (into-array OpenOption [StandardOpenOption/CREATE_NEW StandardOpenOption/WRITE]))
        reference {:path (str file) :sha256 (sha bytes)}]
    (is (java.util.Arrays/equals bytes (fence/read-exact-child-DATA! reference (alength bytes))))
    (is (thrown? clojure.lang.ExceptionInfo (fence/read-exact-child-DATA! reference (inc (alength bytes)))))
    (is (thrown? clojure.lang.ExceptionInfo (fence/read-exact-child-DATA! (assoc reference :sha256 (apply str (repeat 64 "0"))) (alength bytes))))))
(deftest read-close-retains-fatal-primary-and-causal-interruption
  (let [directory (Files/createTempDirectory "public-reader-error-" (make-array FileAttribute 0))
        file (.resolve directory "data.bin") bytes (.getBytes "inert" java.nio.charset.StandardCharsets/UTF_8)
        _ (Files/write file bytes (into-array OpenOption [StandardOpenOption/CREATE_NEW StandardOpenOption/WRITE]))
        original (LinkageError. "read failure") cleanup (ex-info "close failure" {} (InterruptedException. "cause"))
        closed (atom 0)
        out (with-redefs-fn {(fv 'open-fence-input!)
              (fn [_] (proxy [java.io.ByteArrayInputStream] [bytes]
                (readNBytes [_] (throw original))
                (close [] (swap! closed inc) (Thread/interrupted) (throw cleanup))))}
              #(let [e (try (fence/read-exact-child-DATA! {:path (str file) :sha256 (sha bytes)} (alength bytes))
                         (catch Throwable e e))
                     interrupted (.isInterrupted (Thread/currentThread))]
                 (Thread/interrupted) {:error e :interrupted interrupted}))]
    (is (identical? original (:error out))) (is (= 1 @closed))
    (is (some #(identical? cleanup %) (.getSuppressed ^Throwable original)))
    (is (:interrupted out))))
(def result (run-tests 'user))
(assert (zero? (+ (:fail result) (:error result))))
(assert (nil? (find-ns 'gravity.bootstrap)))
(prn {:scope :fresh-public-namespace-source-injected-controls :actual-process-C-L-provider-runtime 0})
