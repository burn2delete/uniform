;; New current-main composition controls. No target/child/C/L is executed.
(require '[clojure.test :refer [deftest is run-tests]] '[clojure.java.io :as io]
         '[clojure.edn :as edn] '[clojure.string :as str] '[clojure.walk :as walk]
         '[gravity.development-test-runner :as dev]
         '[gravity.self-hosting.sh01-host-resource-broker :as broker])
(import '[java.nio.file Files StandardOpenOption LinkOption Path]
        '[java.nio.file.attribute FileAttribute])
(def prefix "/Users/matt/Documents/Codex/2026-10-26/task-23/ci-evidence/")
(def paths [(or (System/getProperty "gravity.ports.dev-source") "bootstrap/clojure/test/gravity/development_test_runner.clj")
            (or (System/getProperty "gravity.ports.broker-source") "bootstrap/clojure/test/gravity/self_hosting/sh01_host_resource_broker.clj")
            (or (System/getProperty "gravity.ports.runner-source") "bootstrap/clojure/test/gravity/self_hosting/sh01_parallel_test_runner.clj")])
(defn form-records [text]
  (with-open [r (clojure.lang.LineNumberingPushbackReader. (java.io.StringReader. text))]
    (binding [*read-eval* false]
      (loop [out []]
        (let [f (read {:eof ::end} r)]
          (if (= ::end f) out
              (recur (conj out {:form f :name (when (and (seq? f) (#{'def 'defn 'defn- 'defrecord} (first f))) (second f))
                               :line (:line (meta f)) :end (.getLineNumber r)}))))))))
(alias 'test 'clojure.test)
(defn texts [source]
  (let [lines (str/split-lines source)]
    (into {} (for [r (form-records source) :when (:name r)]
               [(:name r) (str/join "\n" (subvec lines (dec (:line r)) (:end r)))]))))
(defn forms [path] (into {} (keep #(when (:name %) [(:name %) (:form %)]) (form-records (slurp path)))))
(def actual (mapv forms paths))
(def broker-ns 'gravity.self-hosting.sh01-host-resource-broker)
(defn bv [n] (ns-resolve broker-ns n))
(def root (Files/createTempDirectory "current-main-ports-DATA-" (make-array FileAttribute 0)))
(defn newfile [name] (str (.resolve root (str (java.util.UUID/randomUUID) "-" name))))
(defn thrown [f] (try (f) nil (catch Throwable e e)))
(defn failure [f] (ex-data (thrown f)))

(deftest exact-current-and-donor-source-equations
  (let [changed [#{} '#{with-lease} '#{namespace-command start-process retain-owned-process! process-result run-namespace-process}]]
    (doseq [i (range 3)]
      (let [base (texts (slurp (str prefix "ports-base-" i ".clj.edn"))) candidate (texts (slurp (paths i)))
            donor (texts (slurp (str prefix "ports-donor-" i ".clj.edn")))]
        (doseq [[name text] base :when (not ((changed i) name))]
          (is (= text (get candidate name)) (str "current form conserved " i "/" name)))
        (doseq [[name text] candidate :when (not (contains? base name))]
          (when-not (#{'run-namespace-process-owned 'acquire-resolution-lock!
                       'open-development-result-input! 'read-development-result-bytes! 'read-development-result} name)
            (is (= text (get donor name)) (str "exact accepted donor dependency " name)))))))
  (let [before (get (forms (str prefix "ports-base-1.clj.edn")) 'with-lease)
        after (get (actual 1) 'with-lease)
        branch (first (filter #(and (seq? %) (= '[options resource-class thunk] (first %))) (drop 3 after)))]
    (is (= (drop 4 before) (rest branch)) "legacy three-arity behavior conserved exactly"))
  (let [body (pr-str (get (actual 2) 'run-namespace-process-owned))]
    (is (str/includes? body "(fn [_] nil)"))
    (is (not (str/includes? body "operation-fence"))))
  (is (= (get (actual 0) 'run-cli!) (get (forms (str prefix "ports-base-0.clj.edn")) 'run-cli!))
      "normal CLI body remains current, custom RV child owns machine flag consumption"))

(def selected-ns 'gravity.self-hosting.resolution-parent-mutation-test)
(def selected "actual-macro-trace-input-syntax-id-mutation-at-second-id")
(def source {:source-commit (apply str (repeat 40 "a")) :source-tree (apply str (repeat 40 "b"))
             :configuration-sha256 (apply str (repeat 64 "c"))})
(def selection {:namespaces [(str selected-ns)] :test-vars [(str selected-ns "/" selected)] :fail-fast? false})
(def request {:id "MODEL-one-request" :source source :selection selection})
(def machine {:schema :gravity/development-test-result-v1 :authority :non-authoritative :authoritative? false
              :selection selection :summary {:test 1 :pass 2 :fail 0 :error 0} :outcome :passed :exit-code 0})
(defn delivery [] {:file (newfile "completion.edn") :limit-bytes 65536 :request request})
(deftest actual-thin-writer-private-DATAIO-and-refusals
  (let [d (delivery) events (atom []) sink (dev/result-delivery-sink d #(swap! events conj %))]
    (sink machine)
    (is (= [{:schema :gravity/development-test-result-delivery-v1 :request request :result machine}]
           [(edn/read-string (slurp (:file d)))]))
    (is (= [machine] @events))
    (is (= "DEV-RESULT-DUPLICATE" (:id (failure #(sink machine)))))
    (is (instance? java.nio.file.FileAlreadyExistsException
                   (thrown #((dev/result-delivery-sink d nil) machine)))))
  (doseq [d [(assoc (delivery) :extra true) (assoc (delivery) :limit-bytes 0)
             (assoc (delivery) :file "relative") (assoc-in (delivery) [:request :extra] true)]]
    (is (= "DEV-RESULT-DELIVERY" (:id (failure #(dev/result-delivery-sink d nil))))))
  (let [d (delivery) calls (atom 0) sink (dev/result-delivery-sink d #(swap! calls inc))]
    (is (= "DEV-RESULT-SELECTION" (:id (failure #(sink (assoc machine :selection {}))))))
    (is (zero? @calls)) (is (not (.exists (io/file (:file d))))))
  (let [d (assoc (delivery) :limit-bytes 1)]
    (is (= "DEV-RESULT-LIMIT" (:id (failure #((dev/result-delivery-sink d nil) machine)))))
    (is (not (.exists (io/file (:file d))))))
  (let [d (delivery) target (newfile "untouched") calls (atom 0)]
    (spit target "untouched")
    (Files/createSymbolicLink (.toPath (io/file (:file d))) (.toPath (io/file target)) (make-array FileAttribute 0))
    (is (instance? java.nio.file.FileAlreadyExistsException
                   (thrown #((dev/result-delivery-sink d (fn [_] (swap! calls inc))) machine))))
    (is (= "untouched" (slurp target))) (is (zero? @calls))))

;; Evaluate only inspected actual runner definitions. All native start/L/wait
;; operations are replaced before any method is invoked. No full runner load.
(def model (create-ns 'current-main-ports.model))
(def plan-model (create-ns 'current-main-ports.planner))
(def planner-path "bootstrap/clojure/test/gravity/self_hosting/sh01_impact_test_planner.clj")
(binding [*ns* plan-model]
  (clojure.core/refer 'clojure.core)
  (eval (get (forms planner-path) 'unsupported-execution)))
(def events (atom [])) (def settings (atom {}))
(defn note [x] (swap! events conj x))
(def fake-process (proxy [Process] []
                    (toHandle [] nil)
                    (getInputStream [] (java.io.ByteArrayInputStream. (byte-array 0)))
                    (getErrorStream [] (java.io.ByteArrayInputStream. (byte-array 0)))
                    (destroy [] (throw (AssertionError. "no native termination")))
                    (isAlive [] (throw (AssertionError. "no process probe")))))
(def fake-channel (Object.)) (def fake-L (Object.))
(defn fake-open [_ options _attrs]
  (is (instance? java.util.Set options))
  (is (= #{StandardOpenOption/CREATE StandardOpenOption/WRITE LinkOption/NOFOLLOW_LINKS} options))
  (note :L-open) fake-channel)
(defn fake-try [_] (note :L-try) (when-not (:busy @settings) fake-L))
(defn fake-release [_] (note :L-release) (when-let [e (:release-error @settings)] (throw e)))
(defn fake-valid [_] false)
(defn fake-close [_] (note :L-close))
(defn fake-open? [_] false)
(defn instrument-L [form]
  (walk/postwalk (fn [f]
                   (if (seq? f)
                     (let [replacement (get {'java.nio.channels.FileChannel/open 'user/fake-open
                                             '.tryLock 'user/fake-try '.release 'user/fake-release
                                             '.isValid 'user/fake-valid '.close 'user/fake-close '.isOpen 'user/fake-open?}
                                            (first f))]
                       (if replacement (with-meta (cons replacement (rest f)) (meta f)) f)) f)) form))
(defn intern-port [name implementation] (intern model name implementation))
(def raw {:status :passed :exit-code 0 :root-exited? true :cleanup-root-alive? false
          :tracked-owned-processes-terminal? true :capture-failed? false :output-truncated? false
          :stdout-capture-status :complete :stderr-capture-status :complete
          :stdout-capture-error nil :stderr-capture-error nil :stdout-capture-forced-close? false :stderr-capture-forced-close? false
          :descendant-coverage :unknown})
(intern-port 'default-command ["clojure" "-M:test"])
(intern-port 'forward-child-cache-option? (constantly false))
(binding [*ns* model]
  (clojure.core/refer 'clojure.core) (alias 'broker broker-ns)
  (alias 'io 'clojure.java.io) (alias 'edn 'clojure.edn) (alias 'str 'clojure.string)
  (alias 'planner plan-model) (import 'java.nio.charset.StandardCharsets)
  (doseq [name '[require-supported-namespace! namespace-name command-prefix namespace-command
                 cleanup-failure? capture-failure? result-failure? normalize-result
                 fatal-throwable? fatal-cause interrupted-throwable? restore-interrupt! rethrow-fatal!
                 validate-development-result! open-development-result-input! read-development-result-bytes!
                 read-development-result publish-parent-report!
                 report-inside-lease! resolution-parent-namespace resolution-parent-selector resolution-canonical-lock]]
    (eval (get (actual 2) name))))
(intern-port 'forward-child-cache-option? (constantly false))
(intern-port 'root (delay "/MODEL-cwd"))
(intern-port 'bound-launch-result (fn [r _] r))
(intern-port 'resolution-common-broker-root (fn [_] "/MODEL-C"))
(intern-port 'prepare-development-result (fn [_ opts] (when (:development-result opts)
                                                       (note :prepare) (when-let [e (:prepare-error @settings)] (throw e))
                                                       (let [d (delivery)]
                                                         ;; Supplied MODEL completion DATA, never a child result.
                                                         ((dev/result-delivery-sink d nil) machine)
                                                         d))))
(def actual-read (var-get (ns-resolve model 'read-development-result)))
(def actual-publish (var-get (ns-resolve model 'publish-parent-report!)))
(intern-port 'read-development-result (fn [d result] (note :completion-read) (actual-read d result)))
(intern-port 'publish-parent-report! (fn [_ _ _ observation] (note [:final-report (:state (:owned-resources observation)) (:state (:disposition observation))]) {:observation observation}))
(intern-port 'start-process (fn [& _] (note :start) fake-process))
(intern-port 'process-result
             (fn [_ _]
               (note :capture)
               (let [owned {:process fake-process :owner-thread-id (.getId (Thread/currentThread))}
                     token (broker/retain-process-custody! owned)
                     result (if (:unknown @settings) (assoc raw :tracked-owned-processes-terminal? false) raw)]
                 (broker/record-process-result! token result)
                 result)))
(binding [*ns* model]
  (doseq [name '[acquire-resolution-lock! finalize-resolution-lock!]] (eval (instrument-L (get (actual 2) name))))
  (doseq [name '[run-namespace-process-owned run-namespace-process run-resolution-parent-mutation-process]] (eval (get (actual 2) name))))
(defn call [name & args] (apply (ns-resolve model name) args))
(def operation {:id :RV1-trace-input-syntax-second-id :selector (symbol (str selected-ns "/" selected))
                :reuse-held-operation false :genuine-execution false})
(defn options [] {:resource-broker {:coordination-root "/MODEL-C"}
                 :command ["/MODEL-java" "-cp" "MODEL" "clojure.main" "-m" "gravity.build.trace-selected-caller"]
                 :working-directory "/MODEL-cwd" :exact-test-vars [selected] :child-fail-fast? false
                 :timeout-ms 100 :max-capture-bytes 4096 :max-capture-chars 4096 :capture-wait-ms 10
                 :development-result (assoc source :limit-bytes 65536)
                 :inside-lease-report! (fn [_] (note :semantic-callback) (when-let [e (:callback-error @settings)] (throw e)))
                 :parent-report {:file (newfile "parent.edn") :limit-bytes 65536}})
(defn run-model [changes f]
  (reset! events []) (reset! settings changes)
  (with-redefs [broker/acquire! (fn [_ class] (is (= :exclusive class)) (note :C-acquire) {:released? (atom false)})
                broker/release! (fn [_] (note :C-release)
                                  {:schema :gravity/sh01-host-resource-non-authoritative-receipt-v1
                                   :resource-class :exclusive :capacity 1 :outcome :released})]
    (f)))

(deftest current-defaults-and-ordinary-guard
  (let [released (atom 0) error (Exception. "old-thunk")]
    (with-redefs [broker/acquire! (fn [& _] :old-lease) broker/release! (fn [_] (swap! released inc))]
      (is (= :old-return (broker/with-lease {} :normal (constantly :old-return))))
      (is (identical? error (thrown #(broker/with-lease {} :normal (fn [] (throw error)))))))
    (is (= 2 @released)))
  (run-model {} #(is (= "SH01-IMPACT-EXTERNAL-WRAPPER"
                         (:id (failure (fn [] (call 'run-namespace-process {:namespace selected-ns} {})))))))
  (is (empty? @events))
  (is (= ["java" "--namespace" "gravity.example-test"]
         (call 'namespace-command 'gravity.example-test {:command ["java"]})))
  (is (= ["java" "--namespace" "gravity.example-test" "--exact" "one" "--fail-fast"]
         (call 'namespace-command 'gravity.example-test {:command ["java"] :exact-test-vars ["one"] :child-fail-fast? true}))))

(deftest actual-current-unowned-call-body-conservation
  (let [current-var (ns-resolve model 'run-namespace-process) adopted (var-get current-var)
        baseline (get (forms (str prefix "ports-base-2.clj.edn")) 'run-namespace-process)
        job {:namespace 'gravity.example-test :resource-class :normal}
        opts {:command ["java"] :working-directory "/MODEL-cwd"
              :process-launcher (fn [& _] {:status :passed :exit-code 0 :elapsed-ms 7 :stdout "current"})}
        actual (call 'run-namespace-process job opts)]
    (try
      (binding [*ns* model] (eval baseline))
      (is (= actual (call 'run-namespace-process job opts)))
      (finally (alter-var-root current-var (constantly adopted))))))

;; Only the new start boundary is exercised here, replacing every builder
;; operation BEFORE evaluation. Retention and broker custody code are actual.
(defn model-builder [& _] :MODEL-builder)
(defn model-directory [builder _] builder)
(defn model-redirect [builder _] builder)
(def pending-observed (atom []))
(defn model-start [_]
  (let [registry (var-get (bv '*process-custody*))]
    (swap! pending-observed conj (when registry (:launch-state @(first @registry)))))
  (when-let [e (:start-error @settings)] (throw e))
  fake-process)
(intern-port 'retain-owned-process! (fn [p] {:process p :owner-thread-id (.getId (Thread/currentThread))}))
(def start-form
  (walk/postwalk (fn [f]
                   (if (seq? f)
                     (if-let [name (get {'ProcessBuilder. 'user/model-builder '.directory 'user/model-directory
                                        '.redirectErrorStream 'user/model-redirect '.start 'user/model-start} (first f))]
                       (with-meta (cons name (rest f)) (meta f)) f) f))
                 (let [f (get (actual 2) 'start-process)] (with-meta (list* (first f) 'instrumented-start-process (nnext f)) (meta f)))))
(binding [*ns* model] (eval start-form))

(deftest startup-custody-and-default-interrupt-conservation
  (reset! pending-observed []) (reset! settings {})
  (let [observations (atom []) releases (atom 0)]
    (with-redefs [broker/acquire! (fn [& _] {:released? (atom false)})
                  broker/release! (fn [_] (swap! releases inc) {:outcome :released})]
      (broker/with-lease {} :exclusive
        (fn []
          (let [process (call 'instrumented-start-process ["MODEL-java"] "/MODEL-cwd")
                owned (broker/retained-start-token process)
                token (broker/retain-process-custody! owned)]
            (is (identical? fake-process process))
            (is (identical? owned (:owned @token)))
            (broker/record-process-result! token raw)))
        #(swap! observations conj %))
      (is (= [:pending] @pending-observed)) (is (= 1 @releases))
      (is (= :released (get-in (first @observations) [:disposition :state])))))
  (let [error (InterruptedException. "MODELED native start")]
    (reset! settings {:start-error error}) (Thread/interrupted)
    (let [out (thrown #(call 'instrumented-start-process ["MODEL-java"] "/MODEL-cwd"))
          interrupted? (Thread/interrupted)]
      (is (identical? error out)) (is (false? interrupted?) "unowned start preserves current raw interrupt behavior"))
    (reset! pending-observed [])
    (let [observations (atom []) releases (atom 0)
          out (with-redefs [broker/acquire! (fn [& _] {:released? (atom false)})
                            broker/release! (fn [_] (swap! releases inc))]
                (thrown #(broker/with-lease {} :exclusive
                           (fn [] (call 'instrumented-start-process ["MODEL-java"] "/MODEL-cwd"))
                           (fn [o] (swap! observations conj o)))))
          interrupted? (Thread/interrupted)]
      (is (identical? error out)) (is interrupted?)
      (is (= [:pending] @pending-observed)) (is (zero? @releases))
      (is (= :quarantined (get-in (first @observations) [:disposition :state]))))))

(deftest selected-order-success-and-correlated-disposition
  (let [result (run-model {} #(call 'run-resolution-parent-mutation-process operation (options)))]
    (is (= :passed (:status result)))
    (is (= [:C-acquire :L-open :L-try :prepare :start :capture :completion-read :semantic-callback
            :L-release :L-close :C-release [:final-report :released :released]] @events))
    (is (= :unknown (:descendant-coverage result)))))

(deftest exact-selection-and-closed-options-refuse-before-C
  (doseq [change [#(assoc % :exact-test-vars ["other"]) #(assoc % :allowed-namespace true)
                  #(assoc % :process-launcher (fn [& _] fake-process)) #(assoc-in % [:resource-broker :coordination-root] "/other-C")
                  #(assoc % :child-fail-fast? true) #(assoc % :inside-lease-report! nil)]]
    (run-model {} #(is (= "SH01-RESOLUTION-OWNER-REQUEST"
                          (:id (failure (fn [] (call 'run-resolution-parent-mutation-process operation (change (options)))))))))
    (is (empty? @events))))

(deftest selected-busy-unresolved-release-and-raw-error-paths
  (run-model {:busy true} #(is (some? (thrown (fn [] (call 'run-resolution-parent-mutation-process operation (options)))))))
  (is (= [:C-acquire :L-open :L-try :L-close :C-release [:final-report :not-acquired :released]] @events))
  (run-model {:unknown true} #(is (some? (thrown (fn [] (call 'run-resolution-parent-mutation-process operation (options)))))))
  (is (not (some #{:L-release :C-release} @events)))
  (is (= [:final-report :quarantined :quarantined] (last @events)))
  (doseq [error [(Error. "fatal-callback") (InterruptedException. "interrupt-callback")]]
    (let [out (run-model {:callback-error error} #(thrown (fn [] (call 'run-resolution-parent-mutation-process operation (options)))))
          interrupted? (Thread/interrupted)]
      (is (identical? error out))
      (is (some #{:L-release} @events)) (is (some #{:C-release} @events))
      (is (= [:final-report :released :released] (last @events)))
      (when (instance? InterruptedException error) (is interrupted?))))
  (let [error (Exception. "L-release-failed")]
    (run-model {:release-error error} #(is (some? (thrown (fn [] (call 'run-resolution-parent-mutation-process operation (options)))))))
    (is (not (some #{:C-release} @events)))
    (is (= [:final-report :release-failed :quarantined] (last @events)))))

(deftest actual-reader-and-v2-report-private-DATAIO
  (let [d (delivery) _ ((dev/result-delivery-sink d nil) machine)
        process (assoc raw :stdout "" :stderr "")]
    (is (= machine (actual-read d process)))
    (is (= "SH01-DEVELOPMENT-RESULT" (:id (failure #(actual-read d (assoc process :output-truncated? true)))))))
  (let [opts (options)
        result (run-model {} #(call 'run-resolution-parent-mutation-process operation opts))
        observation (get-in result [:parent-report :observation])
        publication (actual-publish {:namespace selected-ns :resource-class :exclusive} opts "MODEL-parent-id" observation)]
    (is (= :gravity/development-parent-report-v2 (get-in publication [:report :schema])))
    (is (= :released (get-in publication [:report :resource :state])))
    (is (= :released (get-in publication [:report :resource :owned-resource :state])))
    (is (= (:report publication) (edn/read-string (slurp (:file publication))))))
  (is (= java.nio.channels.FileChannel
         (.getDeclaringClass (.getMethod java.nio.channels.FileChannel "open"
                                        (into-array Class [Path java.util.Set (class (make-array FileAttribute 0))])))))
  (is (nil? (find-ns 'gravity.bootstrap))))

(deftest explicit-owned-no-report-cannot-fall-back-to-legacy-three-arity
  (doseq [unknown? [false true]]
    (reset! events []) (reset! settings {:unknown unknown?})
    (let [releases (atom 0)
          out (with-redefs [broker/acquire! (fn [_ class] (is (= :normal class)) {:released? (atom false)})
                            broker/release! (fn [_] (swap! releases inc) {:outcome :released})]
                (call 'run-namespace-process {:namespace 'gravity.example-test :resource-class :normal}
                      {:command ["MODEL-java"] :working-directory "/MODEL-cwd"
                       :resource-broker {:coordination-root "/MODEL-C"}}))]
      (is (= (not unknown?) (:tracked-owned-processes-terminal? out)))
      (is (= (if unknown? 0 1) @releases))
      (is (not (contains? out :parent-report))))))

(deftest completion-read-primary-and-close-errors-are-not-confused
  (doseq [original [(OutOfMemoryError. "MODEL read-fatal") (InterruptedException. "MODEL read-interrupt")
                    (java.io.IOException. "MODEL read-io")]]
    (let [cleanup (java.io.IOException. "MODEL close-error") closes (atom 0)
          input (proxy [java.io.ByteArrayInputStream] [(byte-array 0)]
                  (readNBytes [_n] (throw original))
                  (close [] (swap! closes inc) (throw cleanup)))
          open-var (ns-resolve model 'open-development-result-input!)
          out (with-redefs-fn {open-var (fn [_] input)} #(thrown (fn [] (actual-read (delivery) raw))))
          interrupted? (Thread/interrupted)]
      (is (or (identical? out original) (identical? (.getCause ^Throwable out) original)))
      (is (some #(identical? cleanup %) (.getSuppressed ^Throwable original)))
      (is (= 1 @closes))
      (when (instance? InterruptedException original) (is interrupted?)))))

(when-not (System/getProperty "gravity.ports.controls.load-only")
  (let [r (run-tests)]
    (prn {:scope :current-main-composition-SOURCE-injected-MODEL-privateDATAIO :summary r
          :fixture-root (str root) :actual-process-C-L-lease-probes 0
          :Bootstrap-loaded (boolean (find-ns 'gravity.bootstrap)) :runtime-admission false})
    (when (pos? (+ (:fail r) (:error r))) (System/exit 1))))
