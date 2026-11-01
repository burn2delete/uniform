(ns gravity.build.trace-selected-caller-test
  "Complete selected source composition with inert native ports. DATA IO only."
  (:require [clojure.test :as test :refer [deftest is]]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.walk :as walk]
            [gravity.build.trace-selected-caller :as caller]
            [gravity.build.trace-semantic-delivery :as semantic]
            [gravity.build.trace-input-syntax-child-test :as child-fixtures]
            [gravity.development-test-runner :as development]
            [gravity.self-hosting.sh01-host-resource-broker :as broker])
  (:import [java.nio.file Files]
           [java.nio.file.attribute FileAttribute]))

;; Reuse ONLY the accepted owner's inert fixture setup and source substitutions.
;; No foreign deftest/model run, native builder/start/L operation or target load.
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
(def selected-ns 'gravity.self-hosting.resolution-parent-mutation-test)
(def selected-var "actual-macro-trace-input-syntax-id-mutation-at-second-id")
(def selection {:namespaces [(str selected-ns)] :test-vars [(str/join "/" [(str selected-ns) selected-var])] :fail-fast? false})
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
(def job {:namespace selected-ns :resource-class :exclusive})
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
              (recur (if (and (seq? f) (#{'def 'defrecord 'defn 'defn-} (first f)))
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

(def channel (atom {:open? true})) (def lock-handle (atom {:valid? true}))
(def returned-handles (atom #{}))
(def lock-calls (atom {:open 0 :try 0 :release 0 :valid 0 :close 0 :open? 0}))
(doseq [[name f]
        {'fake-lock-open (fn [_]
                           (port :L-open nil) (reset! channel {:open? true})
                           (swap! returned-handles conj :channel) channel)
         'fake-lock-try (fn [actual]
                          (is (identical? channel actual)) (port :L-try nil)
                          (when-not (:busy @settings)
                            (reset! lock-handle {:valid? true})
                            (swap! returned-handles conj :lock) lock-handle))
         'fake-lock-release (fn [actual]
                              (is (identical? lock-handle actual)) (port :L-release nil)
                              (when-not (:release-noop @settings) (reset! lock-handle {:valid? false})))
         'fake-lock-valid (fn [actual] (is (identical? lock-handle actual)) (:valid? @actual))
         'fake-channel-close (fn [actual]
                               (is (identical? channel actual)) (port :L-close nil)
                               (when (:consume-interrupt @settings) (Thread/interrupted))
                               (when-not (:close-noop @settings) (reset! channel {:open? false})))
         'fake-channel-open (fn [actual] (is (identical? channel actual)) (:open? @actual))
         'resolution-common-broker-root (fn [_] (port :common-root "/inert/common"))}]
  (intern model-ns name f))
(def policy-ns (create-ns 'selected-owner.policy-model))
(intern policy-ns 'unsupported-execution
        (fn [name] (note :guard) (when (= name selected-ns) {:id "SH01-IMPACT-EXTERNAL-WRAPPER"})))
(binding [*ns* model-ns] (alias 'str 'clojure.string) (alias 'planner 'selected-owner.policy-model))
(defn inert-lock-form [form]
  (walk/prewalk
   (fn [node]
     (if (seq? node)
       (let [replacement (get {'java.nio.channels.FileChannel/open [:open 'fake-lock-open]
                               '.tryLock [:try 'fake-lock-try] '.release [:release 'fake-lock-release]
                               '.isValid [:valid 'fake-lock-valid] '.close [:close 'fake-channel-close]
                               '.isOpen [:open? 'fake-channel-open]} (first node))]
         (if replacement
           (do (swap! lock-calls update (first replacement) inc)
               (list (second replacement) (second node))) node)) node)) form))
(def acquire-form (inert-lock-form (get fs 'acquire-resolution-lock!)))
(def finalize-form (inert-lock-form (get fs 'finalize-resolution-lock!)))
(assert (= {:open 1 :try 1 :release 1 :valid 1 :close 1 :open? 1} @lock-calls))
(binding [*ns* model-ns]
  (doseq [name '[resolution-parent-namespace resolution-parent-selector resolution-canonical-lock]] (eval (get fs name)))
  (doseq [name '[retain-owned-process!]] (eval (get fs name)))
  (eval modeled-start) (eval acquire-form) (eval finalize-form)
  (doseq [name '[process-result validate-development-result! report-inside-lease! publish-parent-report!
                 require-supported-namespace! run-namespace-process-owned run-namespace-process
                 run-resolution-parent-mutation-process]] (eval (get fs name))))
(def route (var-get (ns-resolve model-ns 'run-resolution-parent-mutation-process)))
(def ordinary (var-get (ns-resolve model-ns 'run-namespace-process)))
(def operation {:id :RV1-trace-input-syntax-second-id :selector (symbol (str selected-ns "/" selected-var))
                :reuse-held-operation false :genuine-execution false})
(defn target []
  (let [p (java.nio.file.Files/createTempDirectory "selected-route-model-" (make-array java.nio.file.attribute.FileAttribute 0))]
    (swap! roots conj p) {:file (str (.resolve p "parent.edn")) :limit-bytes 65536}))
(defn options [output callback]
  {:resource-broker {:coordination-root "/inert/common"} :command ["/inert/java"]
   :exact-test-vars [selected-var] :child-fail-fast? false :working-directory "/inert"
   :development-result (assoc source :limit-bytes 65536) :inside-lease-report! callback :parent-report output
   :timeout-ms 100 :max-capture-bytes 65536 :max-capture-chars 65536 :capture-wait-ms 100})
(defn exercise [configuration]
  (reset! settings configuration) (reset! events []) (reset! quarantines {})
  (reset! channel {:open? true}) (reset! lock-handle {:valid? true})
  (reset! returned-handles #{})
  (let [lease {:released? (atom false)} output (target) seen (atom [])
        opts (options output (fn [obs]
                               (note :inside) (is (:valid? @lock-handle))
                               (is (:open? @channel)) (swap! seen conj obs)
                               (when-let [error (:inside @settings)] (throw error))))
        result (with-redefs [broker/acquire! (fn [_ class]
                                               (is (= :exclusive class)) (port :C-acquire lease))
                             broker/release! (fn [actual]
                                               (is (identical? lease actual))
                                               (when (@returned-handles :lock) (is (not (:valid? @lock-handle))))
                                               (when (@returned-handles :channel) (is (not (:open? @channel))))
                                               (port :C-release release-receipt))]
                 (try {:value (route operation opts)}
                      (catch Throwable error {:error error :interrupted? (.isInterrupted (Thread/currentThread))})
                      (finally (Thread/interrupted))))
        quarantine (get @quarantines (:released? lease))]
    (assoc result :seen @seen :events @events :quarantine quarantine :output output
           :report (when (.exists (io/file (:file output))) (edn/read-string (slurp (:file output)))))))
(defn labels [out] (mapv first (:events out)))
(defn raw-error? [p e]
  (or (identical? p e) (some #(or (identical? % e) (identical? e (:callback-error (ex-data %)))) (.getSuppressed ^Throwable p))))

(def data-observations (atom nil))
(def captured-command (atom nil))
(def delivered (atom nil))
(def child-errors (atom []))
(def original-resolve requiring-resolve)
(def original-correlate semantic/correlate)
(def original-invoke semantic/invoke-child!)

;; Replace earlier injected preparation/read/argv with their ACTUAL common forms.
(binding [*ns* model-ns]
  (doseq [name '[default-command rethrow-fatal! namespace-name command-prefix cleanup-failure? capture-failure?
                 result-failure? normalize-result prepare-development-result
                 open-development-result-input! read-development-result-bytes! read-development-result namespace-command]]
    (when (= name 'namespace-command) (intern model-ns 'forward-child-cache-option? (constantly false)))
    (eval (get fs name))))
(def prepare (var-get (ns-resolve model-ns 'prepare-development-result)))
(def strict-read (var-get (ns-resolve model-ns 'read-development-result)))
(def argv (var-get (ns-resolve model-ns 'namespace-command)))
(def normalize (var-get (ns-resolve model-ns 'normalize-result)))
(def original-start (var-get (ns-resolve model-ns 'fake-start)))
;; Exactly the accepted builder/start substitutions, now retaining full argv.
(intern model-ns 'fake-builder (fn [] (port :builder :inert-builder)))
(intern model-ns 'prepare-development-result
        (fn [job opts] (note :prepare) (let [d (prepare job opts)] (reset! delivered d) d)))
(intern model-ns 'namespace-command
        (fn [namespace opts] (note :command) (let [a (argv namespace opts)] (reset! captured-command a) a)))
(intern model-ns 'read-development-result
        (fn [d result] (note :strict-read) (strict-read d result)))
(intern model-ns 'normalize-result
        (fn [job result elapsed] (note :normalize) (normalize job result elapsed)))
(intern model-ns 'retained-process-result
        (fn [_ _ owned]
          (note :capture)
          (when-let [error (:capture @settings)] (throw error))
          (let [failure (try
                          (note :child-entry)
                          (caller/child-main! (vec (drop-while #(not= "--rv-binding" %) @captured-command)))
                          nil (catch Throwable error (swap! child-errors conj error) error))
                result (merge raw {:output-truncated? false}
                              (when failure {:status :failed :exit-code 1})
                              (:raw @settings))]
            (note :child-complete)
            (reset! (:terminal owned) result) result)))

(defn private-root []
  ;; Own DATA artifacts retained for inspection; no cleanup/custody claim.
  (Files/createTempDirectory "RV-caller-composed-DATA-" (make-array FileAttribute 0)))
(defn sha-file [p]
  (format "%064x" (java.math.BigInteger. 1 (.digest (java.security.MessageDigest/getInstance "SHA-256")
                                                    (Files/readAllBytes (.toPath (io/file p)))))))
(defn configuration [root]
  (let [material (str (.resolve root "inert-launch-material"))
        cwd (System/getProperty "user.dir")
        test-source (str cwd "/bootstrap/clojure/test/gravity/self_hosting/resolution_parent_mutation_test.clj")]
    (spit material "DATA; never executable")
    {:launcher {:path material :sha256 (sha-file material)}
     :jvm-options ["-Xmx256m"] :classpath [(str root)]
     :source-files [{:path material :sha256 (sha-file material)}
                    {:path test-source :sha256 (sha-file test-source)}]
     :working-directory cwd :output-root (str root)
     :parent-report {:file (str (.resolve root "parent.edn")) :limit-bytes 16777216}
     :broker {:coordination-root "/inert/common"}
     :limits {:timeout-ms 100 :max-capture-bytes 65536 :max-capture-chars 65536
              :capture-wait-ms 100 :completion-bytes 65536 :semantic-bytes 16777216}
     :source (select-keys source [:source-commit :source-tree])
     :test-source-sha256 (sha-file test-source) :evidence-kind :data-unit-control}))

(defn composed [configuration-settings]
  (reset! settings configuration-settings) (reset! events []) (reset! quarantines {})
  (reset! captured-command nil) (reset! delivered nil) (reset! child-errors [])
  (reset! returned-handles #{}) (reset! channel {:open? true}) (reset! lock-handle {:valid? true})
  (let [root (private-root) request (caller/bind-request (configuration root))
        lease {:released? (atom false)} audit (atom nil)
        actual-resolver (fn [symbol]
                          (case symbol
                            gravity.self-hosting.sh01-parallel-test-runner/run-resolution-parent-mutation-process
                            (ns-resolve model-ns 'run-resolution-parent-mutation-process)
                            gravity.self-hosting.resolution-parent-mutation-test/observations #'data-observations
                            (throw (AssertionError. (str "Unmodeled resolver " symbol)))))
        original-publication (var-get (ns-resolve model-ns 'publish-parent-report!))]
    (with-open [parent-root (Files/newDirectoryStream root)]
      (with-redefs-fn
        {#'clojure.core/requiring-resolve actual-resolver
         #'development/run-cli!
         (fn [arguments]
           (is (= ["--namespace" (str selected-ns) "--exact" selected-var] arguments))
           (note :fixed-child-runner)
           (reset! data-observations ((var-get #'child-fixtures/data-observation)))
           (development/*machine-result-sink* machine)
           (note :completion-written) 0)
         #'semantic/correlate
         (fn [& args] (note :semantic)
           (is (:valid? @lock-handle)) (is (:open? @channel))
           (when-let [error (:semantic @settings)] (throw error))
           (apply original-correlate args))
         #'semantic/invoke-child!
         (fn [& args]
           (let [result (apply original-invoke args)]
             (if-let [error (:publication-error @settings)] (assoc result :publication-error error) result)))
         #'broker/acquire! (fn [_ class] (is (= :exclusive class)) (port :C-acquire lease))
         #'broker/release! (fn [actual]
                             (is (identical? lease actual))
                             (when (@returned-handles :lock) (is (not (:valid? @lock-handle))))
                             (when (@returned-handles :channel) (is (not (:open? @channel))))
                             (port :C-release release-receipt))
         (ns-resolve model-ns 'publish-parent-report!)
         (fn [& args] (note :parent-report) (apply original-publication args))}
        (fn []
          (let [out (try {:value (caller/run! request parent-root audit)}
                         (catch Throwable error {:error error :interrupted? (.isInterrupted (Thread/currentThread))})
                         (finally (Thread/interrupted)))
                output (get-in request [:configuration :parent-report :file])]
            (assoc out :request request :audit @audit :events @events
                   :child-errors @child-errors :quarantine (get @quarantines (:released? lease))
                   :report (when (.exists (io/file output)) (edn/read-string (slurp output))))))))))

(deftest complete-selected-composition-writes-and-correlates-before-disposition
  (let [out (composed {}) v (:value out) report (:report out) labels (mapv first (:events out))]
    (is (nil? (:error out)) (some-> (:error out) Throwable->map pr-str))
    (is (= [:common-root :C-acquire :L-open :L-try :prepare :command :builder :native-start-port
            :token :fake-handle :capture :child-entry :fixed-child-runner :completion-written
            :child-complete :strict-read :normalize :semantic :L-release :L-close :C-release :parent-report] labels))
    (is (= :matched-supplied-observation (get-in v [:semantic :semantic :correlation])))
    (is (= :gravity/development-parent-report-v2 (:schema report)))
    (is (not= (get-in report [:request :id]) (get-in v [:result :development-result-request :id])))
    (is (= :released (get-in report [:resource :state])))
    (is (= :released (get-in report [:resource :owned-resource :state])))
    (is (false? (:genuine-execution-established? v)))
    (is (= @captured-command (:actual-command v)))
    (is (nil? (find-ns selected-ns)))
    (is (nil? (find-ns 'gravity.self-hosting.sh01-parallel-test-runner)))))

(deftest composed-errors-retain-real-modeled-custody-and-final-report-order
  (doseq [stage [:start :capture :L-try :L-release :L-close]]
    (let [error (ex-info (str "DATA " stage) {}) out (composed {stage error})
          labels (mapv first (:events out))]
      (is (identical? error (:error out)))
      (is (some? (:quarantine out)))
      (is (not-any? #{:C-release} labels))
      (is (= :parent-report (last labels)))
      (is (= :quarantined (get-in out [:report :resource :state])))))
  (let [out (composed {:busy true}) labels (mapv first (:events out))]
    (is (= :not-acquired (get-in out [:report :resource :owned-resource :state])))
    (is (= :released (get-in out [:report :resource :state])))
    (is (not-any? #{:child-entry :semantic :L-release} labels))))

(deftest composed-semantic-fatal-interrupt-and-finalizer-errors-preserve-primary
  (doseq [error [(ex-info "DATA semantic" {}) (InterruptedException. "DATA semantic") (LinkageError. "DATA semantic")]]
    (let [secondary (java.io.IOException. "DATA L release")
          out (composed {:semantic error :L-release secondary})]
      (is (identical? error (:error out)))
      (is (raw-error? error secondary))
      (is (identical? error (get-in out [:audit :error])))
      (is (= (instance? InterruptedException error) (:interrupted? out)))
      (is (some? (:quarantine out)))
      (is (= :parent-report (last (mapv first (:events out))))))))

(deftest child-publication-failure-and-incomplete-terminal-facts-cannot-pass
  (let [error (java.io.IOException. "DATA semantic publication failure")
        out (composed {:publication-error error})]
    (is (identical? error (first (:child-errors out))))
    (is (some? (:error out)))
    (is (nil? (:value out)))
    (is (= :released (get-in out [:report :resource :state])))
    (is (= :not-observed (get-in out [:audit :state])))
    (is (= :parent-report (last (mapv first (:events out))))))
  (let [out (composed {:raw {:tracked-owned-processes-terminal? false}})
        labels (mapv first (:events out))]
    (is (some? (:error out)))
    (is (some? (:quarantine out)))
    (is (not-any? #{:L-release :L-close :C-release} labels))
    (is (= :parent-report (last labels)))))

(deftest child-and-caller-shape-refusals-do-not-load-target
  (is (thrown? clojure.lang.ExceptionInfo (caller/child-main! [])))
  (is (thrown? clojure.lang.ExceptionInfo
               (caller/child-main! ["--rv-binding" "{}" "--namespace" "wrong" "--exact" selected-var "--machine-result" "{}"])))
  (let [root (private-root) request (caller/bind-request (configuration root))]
    (is (thrown? clojure.lang.ExceptionInfo (caller/command-prefix (assoc request :configuration-sha256 "wrong")))))
  (reset! events []) (reset! settings {})
  (is (= "SH01-IMPACT-EXTERNAL-WRAPPER"
         (:id (ex-data (try (ordinary job {}) nil (catch Throwable error error))))))
  (is (= [:guard] (mapv first @events)))
  (is (nil? (find-ns selected-ns))))

(deftest separate-joint-report-and-request-identities-remain-correlated
  (let [out (composed {}) request (:request out) result (get-in out [:value :result]) audit (:audit out)]
    (doseq [mutate [#(assoc-in % [:parent-report :report :schema] :gravity/development-parent-report-v1)
                    #(update-in % [:parent-report :report :resource] dissoc :owned-resource)
                    #(assoc-in % [:parent-report :report :request :source :source-tree] "wrong")
                    #(assoc-in % [:parent-report :report :operation :result :exit-code] 1)]]
      (is (thrown? clojure.lang.ExceptionInfo (caller/correlate-parent request (mutate result) audit))))
    (is (map? (caller/correlate-parent request (assoc-in result [:parent-report :report :request :id] "independent-outer-id") audit))))
  (let [error (java.io.IOException. "DATA C release") out (composed {:C-release error})]
    (is (identical? error (:error out)))
    (is (= :released (get-in out [:report :resource :owned-resource :state])))
    (is (= :release-failed (get-in out [:report :resource :state])))
    (is (some? (:quarantine out))))
  (let [error (ex-info "DATA semantic only" {}) out (composed {:semantic error})]
    (is (identical? error (:error out)))
    (is (nil? (:quarantine out)))
    (is (= :released (get-in out [:report :resource :owned-resource :state])))
    (is (= :released (get-in out [:report :resource :state])))))
