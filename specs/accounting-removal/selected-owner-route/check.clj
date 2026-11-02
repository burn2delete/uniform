;; Composed source model. Native builder/start and wait/capture are inert ports.
;; No foreign checker, actual child, lease, lock, process probe or target load.
(require '[clojure.java.io :as io] '[clojure.edn :as edn] '[clojure.walk :as walk] '[clojure.string :as str]
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

(deftest exact-route-is-C-then-L-through-prepare-child-semantic-then-L-C-report
  (let [out (exercise {}) report (:report out)]
    (is (nil? (:error out)))
    (is (= [:common-root :C-acquire :L-open :L-try :prepare :command :builder :native-start-port :token :fake-handle
            :capture :read :normalize :inside :L-release :L-close :C-release] (labels out)))
    (is (= #{(.getId (Thread/currentThread))} (set (map second (:events out)))))
    (is (= 1 (count (:seen out))))
    (is (= :released (get-in report [:resource :state])))
    (is (= :released (get-in report [:resource :owned-resource :state])))
    (is (true? (get-in report [:resource :owned-resource :receipt :lock-released?])))
    (is (true? (get-in report [:resource :owned-resource :receipt :channel-closed?])))
    (is (= "/private/tmp/gravity-sh07-heavy.lock" (get-in report [:resource :owned-resource :receipt :canonical-path])))
    (is (= :gravity/development-parent-report-v2 (:schema report)))
    (is (= :passed (:outcome report)))))

(deftest failed-semantic-return-and-error-do-not-blanket-quarantine-terminal-resources
  (let [failed (-> normalized (assoc :status :failed :exit-code 1)
                   (assoc-in [:development-result :outcome] :failed) (assoc-in [:development-result :exit-code] 1))
        out (exercise {:normalized failed})]
    (is (nil? (:error out))) (is (= :failed (get-in out [:report :outcome])))
    (is (= :C-release (last (labels out)))))
  (let [error (ex-info "semantic correlation failed" {}) out (exercise {:inside error})]
    (is (identical? error (:error out))) (is (nil? (:quarantine out)))
    (is (= :released (get-in out [:report :resource :state])))
    (is (= :released (get-in out [:report :resource :owned-resource :state])))
    (is (= :error (get-in out [:report :outcome])))))

(deftest pending-start-retention-and-capture-errors-retain-L-and-C
  (doseq [stage [:start :retain :capture]]
    (let [error (ex-info (str stage) {}) out (exercise {stage error})
          owned (get-in out [:quarantine :owned-resources]) cell (:custody owned)]
      (is (identical? error (:error out))) (is (some? (:quarantine out)))
      (is (= :quarantined (:state owned)))
      (is (identical? channel (:channel @cell))) (is (identical? lock-handle (:lock @cell)))
      (is (:valid? @lock-handle)) (is (:open? @channel))
      (is (not-any? #{:L-release :L-close :C-release} (labels out)))
      (is (= :quarantined (get-in out [:report :resource :state])))
      (is (= :quarantined (get-in out [:report :resource :owned-resource :state]))))))

(deftest failed-L-release-with-terminal-child-prevents-C-release
  (doseq [stage [:L-release :L-close]]
    (let [error (java.io.IOException. (str stage)) out (exercise {stage error})
          resources (get-in out [:quarantine :owned-resources])]
      (is (identical? error (:error out))) (is (= :release-failed (:state resources)))
      (is (identical? channel (:channel @(:custody resources))))
      (is (not-any? #{:C-release} (labels out)))
      (is (= (= stage :L-release) (:valid? @lock-handle)))
      (is (:open? @channel))
      (is (= :quarantined (get-in out [:report :resource :state])))
      (is (= :release-failed (get-in out [:report :resource :owned-resource :state])))))
  (doseq [configuration [{:release-noop true} {:close-noop true}]]
    (let [out (exercise configuration)]
      (is (some? (:error out))) (is (some? (:quarantine out)))
      (is (not-any? #{:C-release} (labels out))))))

(deftest known-no-L-versus-unknown-L-acquisition
  (let [out (exercise {:busy true})]
    (is (= "SH01-RESOLUTION-CANONICAL-BUSY" (:id (ex-data (:error out)))))
    (is (not-any? #{:prepare :native-start-port :inside :L-release} (labels out)))
    (is (= :not-acquired (get-in out [:report :resource :owned-resource :state])))
    (is (= :released (get-in out [:report :resource :state])))
    (is (false? (get-in out [:report :resource :owned-resource :receipt :lock-acquired?]))))
  (let [error (ex-info "unknown tryLock" {}) out (exercise {:L-try error})]
    (is (identical? error (:error out))) (is (some? (:quarantine out)))
    (is (= :unknown (get-in out [:report :resource :owned-resource :receipt :lock-acquired?])))
    (is (not-any? #{:prepare :L-release :L-close :C-release} (labels out)))))

(deftest C-release-failure-retains-C-with-honestly-released-L
  (let [original (java.io.IOException. "C release failure") out (exercise {:C-release original})]
    (is (identical? original (:error out)))
    (is (some? (:quarantine out)))
    (is (= :release-failed (get-in out [:report :resource :state])))
    (is (= :released (get-in out [:report :resource :owned-resource :state])))
    (is (true? (get-in out [:report :resource :owned-resource :receipt :lock-released?])))
    (is (true? (get-in out [:report :resource :owned-resource :receipt :channel-closed?])))))

(deftest original-fatal-interrupt-and-secondary-L-error-identities
  (doseq [original [(ex-info "original" {}) (InterruptedException. "original") (LinkageError. "original")]
          finalizer [(ex-info "L error" {}) (InterruptedException. "L interrupt") (LinkageError. "L fatal")]]
    (let [out (exercise {:inside original :L-release finalizer :consume-interrupt true})
          expected (if (and (not (instance? LinkageError original)) (instance? LinkageError finalizer)) finalizer original)]
      (is (identical? expected (:error out)))
      (is (raw-error? expected original)) (is (raw-error? expected finalizer))
      (is (= (or (instance? InterruptedException original) (instance? InterruptedException finalizer)) (:interrupted? out)))
      (is (not-any? #{:C-release} (labels out))))))

(deftest generic-five-arity-preserves-hook-failure-custody-and-observer-semantics
  (doseq [failure [(ex-info "hook" {}) (InterruptedException. "hook") (LinkageError. "hook")]]
    (let [lease {:released? (atom false)} seen (atom nil) calls (atom []) raw-handle (Object.)
          hook (let [held raw-handle] (fn [_] (swap! calls conj held) (throw failure)))
          observer-error (ex-info "observer" {})
          out (with-redefs [broker/acquire! (fn [& _] lease)
                            broker/release! (fn [_] (throw (AssertionError. "No C release")))]
                (try (broker/with-lease {} :exclusive (constantly :original) hook
                       #(do (reset! seen %) (throw observer-error)))
                     nil (catch Throwable error error) (finally (Thread/interrupted))))
          quarantine (get @quarantines (:released? lease))]
      (is (identical? failure out)) (is (raw-error? out observer-error))
      (is (= [raw-handle] @calls)) (is (identical? hook (:finalize-owned-resources! quarantine)))
      (is (identical? hook (get-in quarantine [:owned-resources :custody])))
      (is (= :unknown (get-in @seen [:owned-resources :release-attempted?])))
      (is (= :quarantined (get-in @seen [:disposition :state]))))))

(deftest ordinary-guard-and-selected-shape-refusals-stay-before-acquisition
  (reset! settings {}) (reset! events [])
  (is (= "SH01-IMPACT-EXTERNAL-WRAPPER"
         (:id (ex-data (try (ordinary job {}) nil (catch Throwable e e))))))
  (is (= [:guard] (mapv first @events)))
  (doseq [mutation [#(assoc % :exact-test-vars ["other"]) #(assoc % :process-launcher identity)
                    #(assoc % :command ["relative-java"]) #(assoc % :child-fail-fast? true)
                    #(assoc-in % [:resource-broker :coordination-root] "/wrong")
                    #(assoc-in % [:development-result :source-tree] "wrong") #(assoc % :timeout-ms 0)]]
    (let [opts (mutation (options (target) identity))]
      (reset! events [])
      (let [error (with-redefs [broker/acquire! (fn [& _] (note :C-acquire) (throw (AssertionError. "Must refuse before C")))]
                    (try (route operation opts) nil (catch Throwable e e)))]
        (is (some? error))
        (is (not-any? #{:C-acquire :L-open :prepare :native-start-port} (mapv first @events))))))
  (let [error (with-redefs [broker/acquire! (fn [& _] (throw (AssertionError. "Must refuse before C")))]
                (try (route (assoc operation :selector 'other) (options (target) identity)) nil (catch Throwable e e)))]
    (is (= "SH01-RESOLUTION-OWNER-REQUEST" (:id (ex-data error))))))

(deftest original-three-four-arity-and-normal-nonprotected-route-remain-usable
  (let [calls (atom []) value (Object.) lease {:released? (atom false)} seen (atom nil)]
    (with-redefs [broker/acquire! (fn [& _] (swap! calls conj :C-acquire) lease)
                  broker/release! (fn [_] (swap! calls conj :C-release) release-receipt)]
      (is (identical? value (broker/with-lease {} :normal (constantly value))))
      (is (identical? value (broker/with-lease {} :normal (constantly value) #(reset! seen %))))
      (is (= :released (get-in @seen [:disposition :state])))
      (is (= [:C-acquire :C-release :C-acquire :C-release] @calls))))
  (reset! settings {}) (reset! events [])
  (let [opts (options (target) (fn [_] (note :inside)))
        out (with-redefs [broker/acquire! (fn [& _] (note :C-acquire) {:released? (atom false)})
                          broker/release! (fn [_] (note :C-release) release-receipt)]
              (ordinary {:namespace 'gravity.fixture-test :resource-class :exclusive} opts))]
    (is (= :passed (:status out)))
    (is (not-any? #{:common-root :L-open :L-try :L-release :L-close} (mapv first @events)))
    (is (= :gravity/development-parent-report-v1 (get-in out [:parent-report :report :schema])))
    (is (not (contains? (get-in out [:parent-report :report :resource]) :owned-resource)))))

(deftest no-L-when-open-or-C-acquisition-refuses
  (doseq [stage [:L-open :C-acquire]]
    (let [error (ex-info (str stage) {}) out (exercise {stage error})]
      (is (identical? error (:error out)))
      (is (= :not-acquired (get-in out [:report :resource :owned-resource :state])))
      (is (not-any? #{:prepare :native-start-port :L-release :L-close} (labels out)))
      (is (= (= stage :L-open) (boolean (some #{:C-release} (labels out))))))))

(deftest five-arity-rejects-contradictory-finalizer-without-releasing-C
  (doseq [resources [{:state :released :release-attempted? false :custody (atom {}) :error nil}
                     {:state :release-failed :release-attempted? true :custody (atom {}) :error nil}
                     {:state :invented :release-attempted? false :custody nil :error nil}]]
    (let [lease {:released? (atom false)} seen (atom nil)
          error (with-redefs [broker/acquire! (fn [& _] lease)
                              broker/release! (fn [_] (throw (AssertionError. "No release")))]
                  (try (broker/with-lease {} :exclusive (constantly :result) (constantly resources) #(reset! seen %))
                       nil (catch Throwable e e)))]
      (is (= "SH01-BROKER-OWNED-RESOURCE" (:id (ex-data error))))
      (is (= :quarantined (get-in @seen [:disposition :state])))
      (is (= :unknown (get-in @seen [:owned-resources :release-attempted?]))))))

(deftest static-Git-metadata-root-resolution-uses-DATA-IO-not-a-Git-helper
  (let [metadata-ns (create-ns 'selected-owner.metadata-model)]
    (binding [*ns* metadata-ns]
      (clojure.core/refer 'clojure.core) (alias 'io 'clojure.java.io) (alias 'str 'clojure.string)
      (import 'java.nio.charset.StandardCharsets)
      (eval (get fs 'resolution-common-broker-root)))
    (let [resolve-root (var-get (ns-resolve metadata-ns 'resolution-common-broker-root))
          dir (java.nio.file.Files/createTempDirectory "selected-root-data-" (make-array java.nio.file.attribute.FileAttribute 0))
          _ (swap! roots conj dir)
          main (.resolve dir "main") worktree (.resolve dir "worktree") meta (.resolve main ".git")
          slot (.resolve meta "worktrees/slot")]
      (java.nio.file.Files/createDirectories slot (make-array java.nio.file.attribute.FileAttribute 0))
      (java.nio.file.Files/createDirectory worktree (make-array java.nio.file.attribute.FileAttribute 0))
      (is (= (str (.resolve meta "gravity-development-loop-v1/broker")) (resolve-root (str main))))
      (spit (.toFile (.resolve worktree ".git")) (str "gitdir: " slot "\n"))
      (spit (.toFile (.resolve slot "commondir")) "../..\n")
      (is (= (resolve-root (str main)) (resolve-root (str worktree))))
      (java.nio.file.Files/delete (.resolve worktree ".git"))
      (java.nio.file.Files/createSymbolicLink (.resolve worktree ".git") meta (make-array java.nio.file.attribute.FileAttribute 0))
      (is (= "SH01-RESOLUTION-OWNER-REQUEST"
             (:id (ex-data (try (resolve-root (str worktree)) nil (catch Throwable e e)))))))))

(let [summary (run-tests 'user)]
  (doseq [root @roots]
    (with-open [paths (java.nio.file.Files/walk root (make-array java.nio.file.FileVisitOption 0))]
      (doseq [path (reverse (sort-by #(.getNameCount ^java.nio.file.Path %) (iterator-seq (.iterator paths))))]
        (java.nio.file.Files/deleteIfExists path))))
  (reset! quarantines {})
  (assert (nil? (find-ns 'gravity.self-hosting.sh01-parallel-test-runner)))
  (assert (nil? (find-ns 'gravity.bootstrap)))
  (prn {:schema :selected-owner-route/model-v1 :summary summary :native-process-substitutions @substitutions
        :native-lock-substitutions @lock-calls :actual-children 0 :actual-leases 0 :actual-locks 0 :actual-probes 0
        :native-start-bound :not-promised :parent-exit-durable-custody :not-supplied :genuine-runtime :UNRUN})
  (when (pos? (+ (:fail summary) (:error summary))) (System/exit 1)))
