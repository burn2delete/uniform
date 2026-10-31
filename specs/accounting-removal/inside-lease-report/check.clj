;; Inert lifecycle ports, extracted actual caller/helper/publisher, owned DATA IO.
(require '[clojure.java.io :as io] '[clojure.edn :as edn]
         '[clojure.test :refer [deftest is run-tests]])
(load-file "bootstrap/clojure/test/gravity/self_hosting/sh01_host_resource_broker.clj")
(require '[gravity.self-hosting.sh01-host-resource-broker :as broker])
(def broker-ns 'gravity.self-hosting.sh01-host-resource-broker)
(def quarantines (var-get (ns-resolve broker-ns 'quarantined-leases)))
(def settings (atom {})) (def events (atom [])) (def roots (atom []))
(defn note [label] (swap! events conj [label (.getId (Thread/currentThread))]))
(def source {:source-commit (apply str (repeat 40 "a")) :source-tree (apply str (repeat 40 "b"))
             :configuration-sha256 (apply str (repeat 64 "c"))})
(def selection {:namespaces ["gravity.fixture-test"] :test-vars ["gravity.fixture-test/a"] :fail-fast? false})
(def request {:id "inert-child" :source source :selection selection})
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
(def receipt {:schema :gravity/sh01-host-resource-non-authoritative-receipt-v1
              :resource-class :exclusive :capacity 1 :outcome :released})
(def job {:namespace 'gravity.fixture-test :resource-class :exclusive})
(def fake-process (proxy [Process] []
                    (waitFor [] (throw (AssertionError. "No wait")))
                    (exitValue [] (throw (AssertionError. "No probe")))
                    (destroy [] (throw (AssertionError. "No terminate")))))
(defn forms [path]
  (with-open [r (java.io.PushbackReader. (io/reader path))]
    (binding [*read-eval* false]
      (loop [out {}]
        (let [f (read {:eof ::eof} r)]
          (if (= f ::eof) out
              (recur (if (and (seq? f) (#{'defn 'defn-} (first f)))
                       (assoc out (second f) f) out))))))))
(def fs (forms (or (System/getProperty "gravity.inside-lease-source")
                   "bootstrap/clojure/test/gravity/self_hosting/sh01_parallel_test_runner.clj")))
(def model-ns (create-ns 'inside-lease.caller-model))
(binding [*ns* model-ns]
  (clojure.core/refer 'clojure.core) (alias 'broker broker-ns)
  (alias 'io 'clojure.java.io) (alias 'edn 'clojure.edn)
  (import 'java.nio.charset.StandardCharsets))
(defn port [label value]
  (note label)
  (when-let [error (get @settings label)] (throw error)) value)
(doseq [[name value]
        {'root (delay "/inert")
         'require-supported-namespace! (fn [_] (port :guard nil))
         'prepare-development-result (fn [_ _] (port :prepare delivery))
         'namespace-command (fn [_ _] (port :command ["inert-java"]))
         'start-process (fn [_ _] (port :start fake-process))
         'process-result (fn [actual _]
                           (is (identical? fake-process actual))
                           (let [custody (broker/retain-process-custody! {:model true})
                                 result (get @settings :raw raw)]
                             (broker/record-process-result! custody result)
                             (port :capture result)))
         'bound-launch-result (fn [& _] (throw (AssertionError. "No map launcher")))
         'read-development-result (fn [actual value]
                                    (is (identical? delivery actual)) (is (identical? (get @settings :raw raw) value))
                                    (port :read machine))
         'normalize-result (fn [_ _ _] (port :normalize (get @settings :normalized normalized)))}]
  (intern model-ns name value))
(binding [*ns* model-ns]
  (doseq [name '[fatal-throwable? report-inside-lease! publish-parent-report! run-namespace-process]]
    (eval (get fs name))))
(def run-parent (var-get (ns-resolve model-ns 'run-namespace-process)))
(defn options [] {:resource-broker {} :development-result source :working-directory "/inert"
                  :inside-lease-report! (fn [_] nil)})
(defn output []
  (let [path (java.nio.file.Files/createTempDirectory "inside-lease-model-" (make-array java.nio.file.attribute.FileAttribute 0))]
    (swap! roots conj path)
    {:file (str (.resolve path "parent.edn")) :limit-bytes 65536}))
(defn exercise [configuration opts callback]
  (reset! settings configuration) (reset! events []) (reset! quarantines {})
  (let [lease {:released? (atom false)} seen (atom [])
        opts (if (= callback ::absent) (dissoc opts :inside-lease-report!)
                 (assoc opts :inside-lease-report!
                        (fn [observation]
                          (note :inside) (swap! seen conj observation) (callback observation))))
        outcome (with-redefs [broker/acquire! (fn [& _] (port :acquire lease))
                              broker/release! (fn [actual]
                                                (is (identical? lease actual))
                                                (when (:clear-interrupt-on-release @settings)
                                                  (Thread/interrupted))
                                                (when-let [before-release (:before-release @settings)] (before-release))
                                                (port :release receipt))]
                  (try {:value (run-parent job opts)}
                       (catch Throwable error {:error error :interrupted? (.isInterrupted (Thread/currentThread))})
                       (finally (Thread/interrupted))))]
    (assoc outcome :seen @seen :events @events :lease lease
           :quarantined (get @quarantines (:released? lease)))))
(defn labels [outcome] (mapv first (:events outcome)))
(defn has-raw? [primary error]
  (or (identical? primary error)
      (some #(or (identical? % error) (identical? (:callback-error (ex-data %)) error))
            (.getSuppressed ^Throwable primary))))

(deftest actual-order-and-result-identity
  (let [out (exercise {} (options) (constantly {:not :replacement})) obs (first (:seen out))]
    (is (identical? normalized (:value out)))
    (is (= [:guard :acquire :prepare :command :start :capture :read :normalize :inside :release] (labels out)))
    (is (= #{(.getId (Thread/currentThread))} (set (map second (:events out)))))
    (is (= 1 (count (:seen out))))
    (is (= :returned (:tag obs))) (is (nil? (:error obs)))
    (is (identical? normalized (:result obs))) (is (identical? raw (:process-result obs)))
    (is (identical? request (:delivery-request obs))) (is (identical? job (:job obs)))))

(deftest errors-have-only-available-facts-and-still-dispose
  (doseq [stage [:prepare :command :start :capture :read :normalize]]
    (let [error (ex-info (str stage) {:stage stage})
          out (exercise {stage error} (options) identity) obs (first (:seen out))]
      (is (identical? error (:error out))) (is (= :threw (:tag obs)))
      (is (nil? (:result obs))) (is (identical? error (:error obs)))
      (is (= 1 (count (:seen out)))) (is (= :release (last (labels out))))
      (is (= (not= :prepare stage) (contains? obs :delivery-request)))
      (is (= (contains? #{:read :normalize} stage) (contains? obs :process-result))))))

(deftest callback-errors-and-release-errors-preserve-precedence
  (doseq [operation-stage [nil :read]
          kind [:ordinary :fatal :interrupt :wrapped-fatal :wrapped-interrupt]]
    (let [original (when operation-stage (ex-info "original read failure" {}))
          cause (case kind
                  (:fatal :wrapped-fatal) (LinkageError. "model fatal")
                  (:interrupt :wrapped-interrupt) (InterruptedException. "model interrupt")
                  (ex-info "callback failure" {}))
          callback-error (if (#{:wrapped-fatal :wrapped-interrupt} kind) (ex-info "wrapper" {} cause) cause)
          release-error (ex-info "release failure" {})
          out (exercise (cond-> {:release release-error} original (assoc operation-stage original))
                        (options) (fn [_] (throw callback-error)))
          expected (if (#{:fatal :wrapped-fatal} kind) cause (or original callback-error))]
      (is (identical? expected (:error out)))
      (is (= (boolean (#{:interrupt :wrapped-interrupt} kind)) (:interrupted? out)))
      (is (= 1 (count (:seen out)))) (is (= :release (last (labels out))))
      (is (has-raw? expected release-error))
      (when original (is (has-raw? expected original)))
      (is (has-raw? expected callback-error))
      (is (some #(= "SH01-INSIDE-LEASE-REPORT" (:id (ex-data %))) (.getSuppressed expected))))))

(deftest original-fatal-and-interruption-remain-original
  (doseq [original [(LinkageError. "original fatal") (InterruptedException. "original interrupt")]]
    (let [secondary (ex-info "callback failure" {})
          out (exercise {:read original} (options) (fn [_] (throw secondary)))]
      (is (identical? original (:error out))) (is (has-raw? original secondary))
      (is (= (instance? InterruptedException original) (:interrupted? out)))
      (is (= :release (last (labels out)))))))

(deftest callback-failure-cannot-promote-incomplete-custody
  (let [error (ex-info "publication refusal" {})
        out (exercise {:raw (assoc raw :root-exited? false)} (options) (fn [_] (throw error)))]
    (is (identical? error (:error out)))
    (is (not-any? #{:release} (labels out)))
    (is (some? (:quarantined out))) (is (= 1 (count (:seen out))))))

(deftest observed-final-report-remains-after-release
  (let [target (output)
        out (exercise {} (assoc (options) :parent-report target)
                      (fn [_] (is (not (.exists (io/file (:file target)))))))
        report (get-in out [:value :parent-report :report])]
    (is (nil? (:error out)))
    (is (= :released (get-in report [:resource :state]))) (is (= :passed (:outcome report)))
    (is (= report (edn/read-string (slurp (:file target)))))
    (is (= :release (last (labels out))))))

(deftest inside-data-io-precedes-release-and-preserves-success-on-failure
  (let [target (output)
        out (exercise {:before-release #(is (= "owned-model-data" (slurp (:file target))))}
                      (options)
                      (fn [_]
                        (java.nio.file.Files/write
                         (.toPath (io/file (:file target))) (.getBytes "owned-model-data" "UTF-8")
                         (into-array java.nio.file.OpenOption [java.nio.file.StandardOpenOption/CREATE_NEW
                                                               java.nio.file.StandardOpenOption/WRITE]))))]
    (is (identical? normalized (:value out)))
    (is (= :release (last (labels out)))))
  (let [error (java.io.IOException. "inert sink failure")
        out (exercise {} (options) (fn [_] (throw error)))
        context (some #(when (= "SH01-INSIDE-LEASE-REPORT" (:id (ex-data %))) (ex-data %))
                      (.getSuppressed ^Throwable (:error out)))]
    (is (identical? error (:error out)))
    (is (identical? normalized (get-in context [:observation :result])))))

(deftest interruption-survives-explicit-disposition-consumption
  (let [original (ex-info "read failure" {})
        interruption (InterruptedException. "inert callback interrupt")
        out (exercise {:read original :clear-interrupt-on-release true} (options)
                      (fn [_] (throw interruption)))]
    (is (identical? original (:error out)))
    (is (true? (:interrupted? out)))
    (is (has-raw? original interruption))
    (is (= :release (last (labels out))))))

(deftest failed-semantics-and-final-publication-refusal-separate
  (let [failed (-> normalized (assoc :status :failed :exit-code 1)
                   (assoc-in [:development-result :outcome] :failed)
                   (assoc-in [:development-result :exit-code] 1))
        out (exercise {:normalized failed} (options) identity)]
    (is (identical? failed (:value out)))
    (is (= :failed (get-in out [:seen 0 :result :status]))) (is (= :release (last (labels out)))))
  (let [target (output) _ (spit (:file target) "preserved")
        out (exercise {} (assoc (options) :parent-report target) identity)]
    (is (some? (:error out))) (is (= :release (last (labels out))))
    (is (= 1 (count (:seen out)))) (is (= "preserved" (slurp (:file target))))))

(deftest defaults-and-before-operation-refusals
  (let [out (exercise {} (options) ::absent)]
    (is (identical? normalized (:value out))) (is (empty? (:seen out)))
    (is (= [:guard :acquire :prepare :command :start :capture :read :normalize :release] (labels out))))
  (doseq [stage [:guard :acquire]]
    (let [error (ex-info "before operation refusal" {})
          out (exercise {stage error} (options) identity)]
      (is (identical? error (:error out))) (is (empty? (:seen out)))
      (is (not-any? #{:start :release} (labels out)))))
  (doseq [opts [(assoc (options) :inside-lease-report! nil)
                (dissoc (options) :resource-broker) (dissoc (options) :development-result)]]
    (reset! events [])
    (let [error (try (run-parent job opts) nil (catch Throwable error error))]
      (is (= "SH01-INSIDE-LEASE-REPORT-REQUEST" (:id (ex-data error))))
      (is (= [:guard] (mapv first @events))))))

(let [summary (run-tests 'user)]
  (doseq [root @roots]
    (with-open [paths (java.nio.file.Files/walk root (make-array java.nio.file.FileVisitOption 0))]
      (doseq [path (reverse (sort-by #(.getNameCount ^java.nio.file.Path %) (iterator-seq (.iterator paths))))]
        (java.nio.file.Files/deleteIfExists path))))
  (reset! quarantines {})
  (assert (nil? (find-ns 'gravity.self-hosting.sh01-parallel-test-runner)))
  (assert (nil? (find-ns 'gravity.bootstrap)))
  (prn {:schema :inside-lease-report/model-v1 :summary summary :actual-leases 0 :actual-children 0
        :actual-probes 0 :real-semantic-publication :UNRUN :parent-exit-custody :unsupported})
  (when (pos? (+ (:fail summary) (:error summary))) (System/exit 1)))
