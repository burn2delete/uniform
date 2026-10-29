(ns effects-acceptance
  "Exact source/selector/process binding for the finite Effects aggregate change.
  Supplies no real process/broker ports and grants no semantic acceptance."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as str]))

(def test-namespace "gravity.self-hosting.sh09-body-effect-test")
(def test-path "bootstrap/clojure/test/gravity/self_hosting/sh09_body_effect_test.clj")
(def effects-path "bootstrap/gravity/src/gravity/effects.gravity")
(def consumer-path "bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity")
(def selectors
  ["body-effect-public-forwarding-source-boundary"
   "body-module-vectors-check-caller-and-every-producer"
   "body-module-vectors-have-no-cumulative-quota"
   "body-private-map-keys-authenticated-builder-composition"
   "body-private-map-keys-development-kernel"])
(def consumers
  {'build-body-effect-result 3 'verify-body-effect-result 4
   'sh09-build-authenticated-body-effect-result 3
   'sh09-verify-authenticated-body-effect-result 4})
(def pmk-selectors ["body-private-map-keys-authenticated-builder-composition"])
(defn command
  ([] (command selectors))
  ([selected]
   (when-not (or (= selectors selected) (= pmk-selectors selected))
     (throw (ex-info "Unsupported Effects selection" {:reason :selector})))
   (into ["clojure" "-M:dev-test" "--namespace" test-namespace]
         (mapcat #(vector "--exact" %) selected))))
(def resource-paths
  [effects-path test-path consumer-path
   "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity"
   "bootstrap/clojure/test/gravity/self_hosting/sh08_function_call_type_test.clj"
   "bootstrap/clojure/test/gravity/development_test_runner.clj"
   "bootstrap/clojure/src/gravity/bootstrap_impl/load_order.edn"
   "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/local-runtime-private-map-keys.gravity"
   "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/local-runtime-private-map-keys.qst"
   "deps.edn"])
(def dependency-roots
  ["bootstrap/clojure/src" "bootstrap/clojure/test" "bootstrap/clojure/fixtures"
   "bootstrap/gravity" "contracts" "tools"])

(defn reject! [reason]
  (throw (ex-info "Effects acceptance binding rejected" {:reason reason})))
(defn require! [ok reason] (when-not ok (reject! reason)))
(defn sha256 [text]
  (format "%064x" (java.math.BigInteger. 1
    (.digest (java.security.MessageDigest/getInstance "SHA-256") (.getBytes text "UTF-8")))))
(defn forms [text]
  (binding [*read-eval* false *data-readers* {} *default-data-reader-fn* (fn [& _] (reject! :tagged-source))]
    (with-open [r (clojure.lang.LineNumberingPushbackReader. (java.io.StringReader. text))]
      (loop [out []]
        (let [form (read {:eof ::eof} r)]
          (if (= ::eof form) out (recur (conj out form))))))))
(defn definitions [text kind]
  (into {} (for [f (forms text) :when (and (seq? f) (= kind (first f)))] [(second f) f])))
(def ^:dynamic *git-reader*
  "Exact profile-installed Git reader; nil retains compiler-free preparation."
  nil)
(defn git-output [root & args]
  (if *git-reader*
    (*git-reader* root (vec args))
    (let [{:keys [exit out]} (apply shell/sh "git" "-C" (str root) args)]
      (require! (zero? exit) :git-read)
      out)))
(defn git [root & args] (str/trim (apply git-output root args)))
(defn snapshot [root main]
  (require! (boolean (re-matches #"[0-9a-f]{40}" main)) :main-identity)
  (require! (= main (git root "rev-parse" (str main "^{commit}"))) :main-identity)
  (require! (empty? (apply git root "status" "--porcelain=v1" "--untracked-files=all" "--" dependency-roots))
            :dirty-dependency)
  {:candidate {:commit (git root "rev-parse" "HEAD") :tree (git root "rev-parse" "HEAD^{tree}")}
   :main {:commit main :tree (git root "rev-parse" (str main "^{tree}"))}
   :dependency-trees (into (sorted-map)
                      (for [p dependency-roots] [p (git root "rev-parse" (str "HEAD:" p))]))
   :resources (into (sorted-map)
                (for [p resource-paths
                      :let [file (io/file root p)
                            _ (require! (.isFile file) :missing-resource)
                            text (slurp file)
                            committed (git-output root "show" (str "HEAD:" p))]]
                  (do (require! (= text committed) :uncommitted-resource)
                      [p (sha256 text)])))})

(defn validate-identity! [expected]
  (require! (= #{:candidate :main :dependency-trees :resources} (set (keys expected))) :missing-identity)
  (doseq [key [:candidate :main]]
    (require! (= #{:commit :tree} (set (keys (get expected key)))) :missing-identity)
    (doseq [value (vals (get expected key))]
      (require! (and (string? value) (boolean (re-matches #"[0-9a-f]{40}" value))) :missing-identity)))
  (require! (= (set dependency-roots) (set (keys (:dependency-trees expected)))) :missing-identity)
  (doseq [value (vals (:dependency-trees expected))]
    (require! (and (string? value) (boolean (re-matches #"[0-9a-f]{40}" value))) :missing-identity))
  (require! (= (set resource-paths) (set (keys (:resources expected)))) :resource-identity)
  (doseq [value (vals (:resources expected))]
    (require! (and (string? value) (boolean (re-matches #"[0-9a-f]{64}" value))) :resource-identity))
  expected)

(defn prepare
  "Validate an exact observed snapshot and source routes, without requiring tests.
  read-text is an inspection seam; it must return the actual bytes of each resource."
  [expected observed read-text]
  (validate-identity! expected)
  (require! (= expected observed) :source-identity)
  (doseq [p resource-paths]
    (require! (= (get-in expected [:resources p]) (sha256 (read-text p))) :resource-identity))
  (let [tests (definitions (read-text test-path) 'deftest)
        effects (definitions (read-text effects-path) 'defn)
        public (definitions (read-text consumer-path) 'defn)]
    (require! (= (symbol test-namespace) (second (first (forms (read-text test-path))))) :namespace)
    (doseq [s selectors] (require! (contains? tests (symbol s)) :missing-selector))
    (let [nodes (set (tree-seq coll? seq (get tests 'body-private-map-keys-authenticated-builder-composition)))]
      (doseq [route '[(compile-c8-module-bundle (path "bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity"))
                     (invoke 'sh09-build-authenticated-body-effect-result [typed verification binding])
                     (invoke 'sh09-verify-authenticated-body-effect-result [typed verification binding actual])
                     (invoke 'sh09-build-authenticated-body-effect-result [typed verification wrong-binding])]]
        (require! (contains? nodes route) :public-consumer-selector-route)))
    (doseq [[name arity] consumers]
      (require! (= arity (count (nth (get (merge effects public) name) 2 nil))) :consumer-arity))
    (require! (= '(defn sh09-build-authenticated-body-effect-result [typed verification binding]
                   (effects/build-body-effect-result typed verification binding))
                 (get public 'sh09-build-authenticated-body-effect-result)) :consumer-route)
    (require! (= '(defn sh09-verify-authenticated-body-effect-result [typed verification binding candidate]
                   (effects/verify-body-effect-result typed verification binding candidate))
                 (get public 'sh09-verify-authenticated-body-effect-result)) :consumer-route)
    (let [nodes (tree-seq coll? seq (get effects 'build-body-effect-result))
          calls (filter #(and (seq? %) (= 'body-module-vectors-valid? (first %))) nodes)
          branches (filter #(and (seq? %) (= 'if (first %)) (= (first calls) (second %))) nodes)]
      (require! (= ['(body-module-vectors-valid? core facts (get producers :entries))] (vec calls)) :builder-route)
      (require! (= 1 (count branches)) :builder-route)
      (require! (= '(body-error typed :malformed-body-module-vectors {}) (last (first branches))) :builder-diagnostic))
    {:schema :effects/aggregate-selection-v1 :authority :none :identity expected
     :namespace test-namespace :selectors selectors :consumers consumers
     :command (command)
     :execution :unrun-held}))

(defn bind-result
  "Bind an actual future runner result to the plan and before/after snapshots.
  No execution occurs here. A caller must capture the machine sink during run-cli!;
  no sink (namespace load/fixture/driver failure) is a driver error, not a test refusal."
  [plan before after result]
  (require! (= :effects/aggregate-selection-v1 (:schema plan)) :plan)
  (require! (= [:none :unrun-held (command (:selectors plan))] ((juxt :authority :execution :command) plan)) :plan)
  (require! (= test-namespace (:namespace plan)) :namespace)
  (require! (= consumers (:consumers plan)) :consumer)
  (validate-identity! (:identity plan))
  (require! (= (:identity plan) before after) :source-identity)
  (require! (some? result) :missing-runner-result)
  (require! (= :gravity/development-test-result-v1 (:schema result)) :runner-result)
  (require! (= [:non-authoritative false] ((juxt :authority :authoritative?) result)) :runner-result)
  (require! (= {:namespaces [test-namespace]
                :test-vars (mapv #(str test-namespace "/" %) (:selectors plan)) :fail-fast? false}
               (:selection result)) :selector)
  (let [{:keys [test pass fail error]} (:summary result)]
    (require! (every? #(and (integer? %) (<= 0 %)) [test pass fail error]) :runner-counts)
    (require! (= test (count (:selectors plan))) :incomplete-execution)
    (require! (pos? (+ pass fail error)) :incomplete-execution)
    (let [passed? (zero? (+ fail error))]
      (require! (= [(if passed? :passed :failed) (if passed? 0 1)]
                   [(:outcome result) (:exit-code result)]) :misattributed-result)
      {:schema :effects/aggregate-observation-v1 :authority :non-authoritative
       :identity before :runner result :outcome (if passed? :passed :failed)
       :semantic-acceptance false})))

(defn prepare-pmk
  "Prepare only the existing authenticated PMK/public C8 test."
  [expected observed read-text]
  (assoc (prepare expected observed read-text)
         :selectors pmk-selectors :command (command pmk-selectors)))

(def process-port-path "bootstrap/clojure/test/gravity/self_hosting/sh01_parallel_test_runner.clj")
(def process-port-symbols
  {:start 'gravity.self-hosting.sh01-parallel-test-runner/start-process
   :retain 'gravity.self-hosting.sh01-parallel-test-runner/retain-owned-process!
   :handles 'gravity.self-hosting.sh01-parallel-test-runner/retained-process-handles
   :result 'gravity.self-hosting.sh01-parallel-test-runner/process-result
   :cleanup 'gravity.self-hosting.sh01-parallel-test-runner/finalize-owned-process!
   :restore-interrupt 'gravity.self-hosting.sh01-parallel-test-runner/restore-interrupt!})
(def process-bounds
  {:execution-ms 3600000 :capture-wait-ms 5000
   :output-limit-bytes 1048576 :output-limit-chars 1048576
   :cleanup-ms 1500 :post-cleanup-wait-ms 100 :post-close-wait-ms 100
   :cleanup-grace-ms 100
   :observed-handle-reap-max-wait-ms 100 :reap-within-cleanup-deadline? true
   :accounted-reclamation-base-ms 11800})
(defn- canonical [value]
  (cond
    (map? value) (into (sorted-map-by #(compare (pr-str %1) (pr-str %2)))
                       (map (fn [[k v]] [k (canonical v)]) value))
    (set? value) (vec (sort-by pr-str (map canonical value)))
    (sequential? value) (mapv canonical value)
    :else value))
(defn identity-text [value] (pr-str (canonical value)))
(defn request-hash [value] (sha256 (identity-text value)))
(defn- absolute-path? [value]
  (and (string? value) (.isAbsolute (java.io.File. value))
       (= value (str (.normalize (.toPath (java.io.File. value)))))))
(defn- file-pin? [pin]
  (and (absolute-path? (:path pin)) (integer? (:bytes pin)) (pos? (:bytes pin))
       (string? (:sha256 pin)) (boolean (re-matches #"[0-9a-f]{64}" (:sha256 pin)))))
(defn- validate-frozen! [frozen cwd]
  (validate-identity! (:source frozen))
  (require! (= [:effects/frozen-owner-operation-v1 :nonlaunching-operation-frozen]
               ((juxt :schema :status) frozen)) :frozen-command)
  (require! (and (absolute-path? cwd) (= cwd (:cwd frozen))) :working-directory)
  (require! (and (vector? (:command frozen))
                 (absolute-path? (get-in frozen [:broker :coordination-root]))
                 (= (if (= pmk-selectors (get-in frozen [:selection :selectors]))
                      :exclusive :memory-heavy)
                    (get-in frozen [:broker :class]))) :frozen-command)
  (let [plan (:selection frozen) cp (:classpath frozen) files (get-in frozen [:runtime :files])
        identity-file (:identity-file frozen)]
    (require! (= [:effects/aggregate-selection-v1 :none :unrun-held (command (:selectors plan))]
                 ((juxt :schema :authority :execution :command) plan)) :plan)
    (require! (= (:source frozen) (:identity plan)) :source-identity)
    (require! (= [test-namespace consumers] ((juxt :namespace :consumers) plan)) :selector)
    (require! (and (vector? cp) (= 6 (count cp)) (every? file-pin? (drop 3 cp))) :frozen-classpath)
    (doseq [[entry relative] (map vector (take 3 cp) ["tools" "bootstrap/clojure/test" "bootstrap/clojure/src"])]
      (require! (= [(str cwd "/" relative) (get-in frozen [:source :dependency-trees relative])]
                   ((juxt :path :tree) entry)) :frozen-classpath))
    (require! (and (= 4 (count files)) (every? file-pin? files)
                   (str/ends-with? (:path (first files)) "/bin/java")
                   (file-pin? (:operation frozen)) (file-pin? identity-file)
                   (= (request-hash (:source frozen)) (:sha256 identity-file))) :frozen-runtime)
    (require! (= [(:path (first files)) "-Xmx2g" "-cp"
                  (str/join java.io.File/pathSeparator (map :path cp)) "clojure.main"
                  (get-in frozen [:operation :path]) (:path identity-file)
                  (get-in frozen [:broker :coordination-root])]
                 (:command frozen)) :frozen-command)))

(defn validation-request
  "Pure binding to the existing normal runner's ports. Does not resolve/load them.
  Runtime pins are expected inputs, requiring live recheck at actual admission."
  [frozen cwd port-source]
  (validate-frozen! frozen cwd)
  (let [declared (into {} (for [f (forms port-source)
                              :when (and (seq? f) (#{'def 'defn 'defn-} (first f)))]
                          [(second f) f]))]
    (doseq [sym (vals process-port-symbols)]
      (require! (contains? declared (symbol (name sym))) :missing-process-port))
    (doseq [[sym expected] {'default-capture-limit-bytes '(* 1024 1024)
                          'default-capture-limit-chars '(* 1024 1024)
                          'default-capture-wait-ms 5000
                          'process-cleanup-grace-ms 100
                          'process-cleanup-timeout-ms 1500
                          'process-post-cleanup-wait-ms 100
                          'capture-post-close-wait-ms 100}]
      (require! (= expected (last (get declared sym))) :process-port-bounds)))
  (require! (some #{'(.get (.onExit handle)
                      (long (max 1 (min 100 (quot (- deadline (System/nanoTime)) 1000000))))
                      TimeUnit/MILLISECONDS)}
                  (tree-seq coll? seq (get (definitions port-source 'defn-) 'reap-process-handle!)))
            :process-port-bounds)
  (require! (= (:accounted-reclamation-base-ms process-bounds)
               (+ (:cleanup-ms process-bounds) (:post-cleanup-wait-ms process-bounds)
                  (* 2 (+ (:capture-wait-ms process-bounds) (:post-close-wait-ms process-bounds)))))
            :process-port-bounds)
  {:schema :effects/selected-process-request-v1 :frozen frozen :cwd cwd
   :ownership-model :normal-retained-process-v1
   :request-hash-argument :append
   :bounds process-bounds
   :port-source {:path process-port-path :sha256 (sha256 port-source) :symbols process-port-symbols}
   :exclusions [:uninterruptible-native-start-wait-close-or-filesystem
                :JVM-initialization :unobserved-cross-session-descendant-escape
                :ProcessHandle-enumeration-and-native-handle-operations
                :unbounded-result-consumer-or-publication-IO]
   :authority :none :execution :unrun-held})

(defn- restore-if-interrupted! [restore throwable]
  (loop [cause throwable]
    (cond
      (nil? cause) false
      (instance? InterruptedException cause) (do (restore throwable) true)
      :else (recur (.getCause ^Throwable cause)))))

(def result-prefix "EFFECTS_VALIDATION_RESULT ")
(defn read-validation-result
  "Read the single child machine-sink envelope, never infer it from test output."
  [output]
  (let [rows (filter #(str/starts-with? % result-prefix) (str/split-lines (:stdout output "")))]
    (require! (= 1 (count rows)) :missing-runner-result)
    (with-open [reader (java.io.PushbackReader. (java.io.StringReader. (subs (first rows) (count result-prefix))))]
      (let [options {:readers {} :default (fn [& _] (reject! :runner-result)) :eof ::eof}
            payload (edn/read options reader)]
        (require! (and (map? payload) (= ::eof (edn/read options reader))) :runner-result)
        payload))))

(defn observe-validation!
  "Execute only caller-supplied ports. No default real launcher or broker exists.
  The caller must bind/recheck these named normal ports under actual admission.
  now returns monotonic milliseconds; only clean zero output reaches the sink reader."
  [request expected-hash {:keys [now start result cleanup publish restore-interrupt read-port-source]}]
  (require! (= expected-hash (request-hash request)) :request-hash)
  (require! (= :effects/selected-process-request-v1 (:schema request)) :process-request)
  (require! (= :normal-retained-process-v1 (:ownership-model request)) :process-request)
  (require! (= [:none :unrun-held] ((juxt :authority :execution) request)) :process-request)
  (require! (= :append (:request-hash-argument request)) :process-request)
  (validate-frozen! (:frozen request) (:cwd request))
  (require! (= process-bounds (:bounds request)) :process-port-bounds)
  (require! (= process-port-symbols (get-in request [:port-source :symbols])) :missing-process-port)
  (require! (every? fn? [now start result cleanup publish restore-interrupt read-port-source]) :process-ports)
  (require! (= process-port-path (get-in request [:port-source :path])) :missing-process-port)
  (require! (= (get-in request [:port-source :sha256]) (sha256 (read-port-source process-port-path))) :process-port-source)
  (let [began (now) deadline (+ began (:execution-ms process-bounds))
        process (atom nil) cleaned (atom nil) cleanup-attempted? (atom false) publication (atom :not-attempted)
        clean! (fn [] (if @cleanup-attempted? @cleaned
                         (when @process
                           (reset! cleanup-attempted? true)
                           (reset! cleaned {:complete? false :reason :cleanup-in-progress})
                           (let [c (try (cleanup @process)
                                     (catch InterruptedException e
                                       (restore-interrupt e)
                                       {:complete? false :reason :cleanup-interrupted})
                                     (catch Exception e
                                       {:complete? false :reason (if (restore-if-interrupted! restore-interrupt e)
                                                                   :cleanup-interrupted :cleanup-failed)}))]
                             (reset! cleaned (if (map? c) c {:complete? false :reason :cleanup-no-evidence}))
                             @cleaned))))
        fail (fn [why] {:status :refused :reason why :request-hash expected-hash
                        :cleanup @cleaned :publication @publication
                        :start-outcome (if @process :returned-handle :no-returned-handle)
                        :elapsed-ms (- (now) began) :semantic-acceptance false})]
    (try
      (let [p (start (conj (get-in request [:frozen :command]) expected-hash) (:cwd request))]
        (reset! process p)
        (let [remaining (- deadline (now))
              r (when (pos? remaining)
                  (result p (assoc (select-keys process-bounds
                                    [:capture-wait-ms :output-limit-bytes :output-limit-chars])
                                  :timeout-ms remaining)))
              _ (when (some? (:cleanup r))
                  (reset! cleanup-attempted? true)
                  (reset! cleaned (:cleanup r)))
              _ (when (:wait-interrupted? r) (throw (InterruptedException. "Process wait interrupted")))
              c (clean!)
              capture-ok? (every? #(= :complete %) [(:stdout-capture-status r) (:stderr-capture-status r)])
              why (cond
                    (or (not (pos? remaining)) (>= (now) deadline) (= :timeout (:status r))) :timeout
                    (or (not= true (:tracked-complete? c)) (= false (:complete? c)) (:root-alive? c)
                        (not= 0 (:descendants-alive c)) (:discovery-error c)
                        (:timeout-containment-unproven? r) (:cleanup-failed? r)
                        (= false (:cleanup-complete? r))
                        (#{:failed :unproven-timeout} (:cleanup-status r))) :incomplete-cleanup
                    (or (not capture-ok?) (:capture-failed? r)
                        (:stdout-capture-forced-close? r) (:stderr-capture-forced-close? r)
                        (:output-truncated? r)) :failed-capture
                    (or (not= :passed (:status r)) (not= 0 (:exit-code r))) :child-failure)]
          (if why (fail why)
            (let [payload (read-validation-result r)
                  _ (require! (some? payload) :missing-runner-result)
                  _ (require! (= expected-hash (:request-hash payload)) :request-hash)
                  observation (bind-result (get-in request [:frozen :selection])
                                (:before payload) (:after payload) (:runner payload))
                  receipt {:status :observed :request-hash expected-hash
                           :observation observation :cleanup c
                           :containment :tracked-owned-handles-only
                           :descendant-coverage (:descendant-coverage c :unknown)
                           :creation-coverage (:creation-coverage request)
                           :semantic-acceptance false}]
              (reset! publication :unknown)
              (let [published? (try (publish receipt)
                                 (catch InterruptedException e (throw e))
                                 (catch Exception e
                                   (throw (ex-info "Publication outcome unknown" {:reason :publication-failed} e))))]
                (reset! publication (if (= true published?) :confirmed :failed))
                (require! (= true published?) :publication-failed))
              receipt))))
      (catch InterruptedException e
        (try (clean!) (finally (restore-interrupt e)))
        (fail :interrupted))
      (catch clojure.lang.ExceptionInfo e
        (clean!) (fail (if (restore-if-interrupted! restore-interrupt e) :interrupted (:reason (ex-data e)))))
      (catch Exception e
        (clean!) (fail (if (restore-if-interrupted! restore-interrupt e) :interrupted :process-port-failure))))))

(def lease-port-path "bootstrap/clojure/test/gravity/self_hosting/sh01_host_resource_broker.clj")
(def lease-port-symbols
  {:acquire 'gravity.self-hosting.sh01-host-resource-broker/acquire!
   :validate 'gravity.self-hosting.sh01-host-resource-broker/validate-live-lease!
   :release 'gravity.self-hosting.sh01-host-resource-broker/release!})

(defn lease-validation-request
  "Bind the existing SH-01 broker source; this does not establish coverage or admission."
  [request source]
  (require! (let [timeout (get-in request [:frozen :broker :acquisition-timeout-ms])]
              (and (integer? timeout) (pos? timeout))) :lease-timeout)
  (let [declared (definitions source 'defn)]
    (doseq [sym (vals lease-port-symbols)]
      (require! (contains? declared (symbol (name sym))) :missing-lease-port)))
  (assoc request :lease-source {:path lease-port-path :sha256 (sha256 source)
                               :symbols lease-port-symbols
                               :timeout-ms (get-in request [:frozen :broker :acquisition-timeout-ms])}))

(defn observe-under-lease!
  "Hold one actual parent lease through process cleanup and publication.
  Explicit broker ports must be installed from the pinned SH-01 broker source.
  For the PMK singleton, the caller must confirm shared-root coverage and take
  the separate canonical immediate tryLock after this exclusive lease, releasing
  that lock before this lease. These remain actual method prerequisites."
  [request expected-hash process-ports {:keys [acquire validate release read-lease-source]}]
  (require! (= expected-hash (request-hash request)) :request-hash)
  (let [spec (:lease-source request)
        root (get-in request [:frozen :broker :coordination-root])
        kind (get-in request [:frozen :broker :class])]
    (require! (and (integer? (:timeout-ms spec)) (pos? (:timeout-ms spec))) :lease-timeout)
    (require! (= [lease-port-path lease-port-symbols
                  (get-in request [:frozen :broker :acquisition-timeout-ms])]
                 ((juxt :path :symbols :timeout-ms) spec)) :missing-lease-port)
    (require! (every? fn? [acquire validate release read-lease-source]) :lease-ports)
    (require! (every? #(fn? (get process-ports %))
                     [:now :start :result :cleanup :publish :restore-interrupt :read-port-source]) :process-ports)
    (require! (= (:sha256 spec) (sha256 (read-lease-source lease-port-path))) :lease-port-source)
    (validate-frozen! (:frozen request) (:cwd request))
    (let [lease (atom nil) checks (atom []) operation (atom nil) released (atom :not-attempted)
          check! (fn [stage]
                   (require! (identical? @lease (validate @lease root kind)) :lease-token)
                   (swap! checks conj stage))]
      (try
        (reset! lease (acquire {:coordination-root root :timeout-ms (:timeout-ms spec)} kind))
        (require! (some? @lease) :lease-token)
        (check! :acquired)
        (reset! operation
          (observe-validation! request expected-hash
            (assoc process-ports
              :start (fn [argv cwd]
                       (check! :before-start)
                       ((:start process-ports) argv cwd))
              :cleanup (fn [process]
                         (let [result ((:cleanup process-ports) process)]
                           (check! :after-cleanup)
                           result))
              :publish (fn [receipt]
                         (check! :before-publication)
                         ((:publish process-ports) receipt)))))
        (check! :after-operation)
        (catch Exception error
          (restore-if-interrupted! (:restore-interrupt process-ports) error)
          (reset! operation {:status :refused :reason (or (:reason (ex-data error)) :lease-failure)
                             :diagnostic (:id (ex-data error)) :semantic-acceptance false}))
        (finally
          (when @lease
            (reset! released :unknown)
            (try
              (let [receipt (release @lease)]
                (reset! released (if (= :released (:outcome receipt))
                                  {:status :released :receipt receipt}
                                  {:status :failed :receipt receipt})))
              (catch Exception error
                (restore-if-interrupted! (:restore-interrupt process-ports) error)
                (reset! released {:status :unknown :diagnostic (:id (ex-data error))}))))))
      {:status (if (and (= :observed (:status @operation))
                        (= :released (:status @released))) :observed :refused)
       :operation @operation :lease-checks @checks :release @released
       :request-hash expected-hash :semantic-acceptance false})))

(defn -main [& args]
  (try
    (if (and (= 3 (count args)) (= "--snapshot" (second args)))
      (let [[root _ main] args] (prn (snapshot root main)))
      (do
        (require! (= 2 (count args)) :usage)
        (let [[root manifest] args expected (edn/read-string (slurp manifest))]
          (prn (prepare expected (snapshot root (get-in expected [:main :commit]))
                        #(slurp (io/file root %)))))))
    (finally (shutdown-agents))))
