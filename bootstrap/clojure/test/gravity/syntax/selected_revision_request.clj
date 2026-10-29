(ns gravity.syntax.selected-revision-request
  "C3-specific request preparation. Loading/preparing never launches an owner."
  (:require [clojure.edn :as edn] [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as str]
            [gravity.resolve.selected-source-harness :as h]
            [gravity.syntax.selected-revision-acceptance :as acceptance]
            [gravity.syntax.selected-revision-git :as git])
  (:import (java.nio.file Files LinkOption OpenOption StandardOpenOption)
           (java.security DigestInputStream MessageDigest)))

(def host-profile
  ;; Proposed host restrictions, not Gravity input/admission accounting.
  {:heap-bytes 2147483648 :child-timeout-ms 120000 :broker-timeout-ms 60000
   :whole-operation-timeout-ms 300000 :max-capture-bytes 65536
   :max-capture-chars 65536 :capture-wait-ms 5000})

(def environment-keys ["JAVA_TOOL_OPTIONS" "JDK_JAVA_OPTIONS" "_JAVA_OPTIONS" "CLASSPATH"])
(def runner-relative "bootstrap/clojure/test/gravity/self_hosting/sh01_parallel_test_runner.clj")
(def runner-source-sha "3ddb5fb8e68debdd21e15032b318b6eb09e0ae799770957e371625db7a462456")
(def runner-signatures
  '{start-process [[command working-directory]] retain-owned-process! [[process]]
    retained-process-handles [[owned]] process-result [[process options] [process options owned]]
    finalize-owned-process! [[owned]]})

(defn runner-binding! [root]
  (let [input (h/file-input! (str (io/file root runner-relative)))
        _ (h/ensure! (= runner-source-sha (:sha256 input)) :public-runner-source-drift)
        forms (binding [*read-eval* false]
                (with-open [reader (java.io.PushbackReader. (java.io.StringReader. (:text input)))]
                  (loop [out []]
                    (let [form (read {:eof ::eof} reader)]
                      (if (= ::eof form) out (recur (conj out form)))))))
        definitions (filter #(and (seq? %) (= 'defn (first %))) forms)]
    (doseq [[name expected] runner-signatures]
      (let [matches (filter #(= name (second %)) definitions)
            definition (first matches)
            body (drop-while #(or (string? %) (map? %)) (nnext definition))
            arglists (if (vector? (first body)) [(first body)] (mapv first body))]
        (h/ensure! (and (= 1 (count matches)) (not (:private (meta (second definition))))
                        (= expected arglists)) :public-runner-signature)))
    {:merge "e9ec1c599614a99e3779b5e3a329e8d9a4b8fa80"
     :source (dissoc input :text) :arglists runner-signatures
     :loader {:namespace 'gravity.self-hosting.sh01-parallel-test-runner
              :method :require-in-fresh-direct-JVM :classpath-source-root (str (io/file root "bootstrap/clojure/test"))}
     :lifecycle-scope :retained-raw-handles :global-descendant-coverage :unknown}))

(defn runner-ports!
  "Future explicit parent loader. Preparation and controls do not call it."
  [root]
  (runner-binding! root)
  (require 'gravity.self-hosting.sh01-parallel-test-runner)
  (into {} (for [[key name] '{:launch start-process :retain retain-owned-process!
                             :handles retained-process-handles :wait process-result
                             :finalize finalize-owned-process!}]
             (let [v (ns-resolve 'gravity.self-hosting.sh01-parallel-test-runner name)]
               (h/ensure! (and v (not (:private (meta v)))
                               (= (get runner-signatures name) (vec (:arglists (meta v)))))
                          :loaded-public-runner-signature)
               [key v]))))

(defn wait-C3-child!
  "Use the caller's already-retained raw token. This does not authorize release:
  tracked finalization and C3-specific creation coverage remain separate."
  [ports process owned options]
  (let [primary (atom nil) finalization (atom nil)
        result (try
                 (let [result ((:wait ports) process options owned)]
                   {:process-result result :retained-token owned
                    :raw-handles ((:handles ports) owned)})
                 (catch Throwable error
                   (reset! primary error)
                   (when (instance? InterruptedException error) (.interrupt (Thread/currentThread)))
                   (throw error))
                 (finally
                   (try (reset! finalization ((:finalize ports) owned))
                        (catch Throwable error
                          (when (instance? InterruptedException error) (.interrupt (Thread/currentThread)))
                          (if-let [original @primary]
                            (when-not (identical? original error) (.addSuppressed ^Throwable original error))
                            (throw error)))
                        (finally
                          (when (instance? InterruptedException @primary)
                            (.interrupt (Thread/currentThread)))))))]
    (assoc result :finalization @finalization :release-authorized? false
                  :global-descendant-coverage :unknown)))
(defn environment []
  {:Java-options (into {} (map (fn [k] [k (System/getenv k)]) environment-keys))
   ;; Bind inherited values without writing secrets into the request.
   :inherited-sha256 (h/sha (.getBytes (pr-str (into (sorted-map) (System/getenv))) "UTF-8"))})

(defn fingerprint [path]
  (let [file (io/file path) p (.toPath file) digest (MessageDigest/getInstance "SHA-256")]
    (h/ensure! (and (.isAbsolute file) (not (Files/isSymbolicLink p))
                    (Files/isRegularFile p (make-array LinkOption 0))) :regular-runtime-input)
    (with-open [stream (DigestInputStream. (io/input-stream file) digest)]
      (io/copy stream (java.io.OutputStream/nullOutputStream)))
    {:path (.getCanonicalPath file) :bytes (Files/size p)
     :sha256 (format "%064x" (java.math.BigInteger. 1 (.digest digest)))}))

(defn- path-state [path]
  (let [f (io/file path) p (.toPath f)]
    (h/ensure! (.isAbsolute f) :absolute-Git-identity-input)
    (cond (Files/isSymbolicLink p)
          {:path path :state :symbolic-link :target (str (Files/readSymbolicLink p))
           :resolved (.getCanonicalPath f)
           :file (when (.isFile f) (fingerprint (.getCanonicalPath f)))}
          (.isFile f) {:path path :state :file :file (fingerprint path)}
          (not (.exists f)) {:path path :state :absent}
          :else (throw (ex-info "Unsupported Git identity input" {:path path})))))

(defn- git-path []
  (some (fn [directory]
          (let [f (io/file directory "git")]
            (when (and (.isFile f) (.canExecute f)) (.getAbsolutePath f))))
        (str/split (or (System/getenv "PATH") "") (re-pattern java.io.File/pathSeparator))))

(defn git-binding! [root identity]
  (h/ensure! (and (seq (:configuration-files identity)) (:executable identity)
                  (= (.getCanonicalPath (io/file (:executable identity)))
                     (some-> (git-path) io/file .getCanonicalPath))) :selected-Git-executable)
  {:root root :cwd root :argv-executable "git" :path-selected-entry (git-path)
   :identity-inputs identity :environment (environment)
   :executable-inputs (mapv path-state (into [(:executable identity)] (:dispatch-files identity)))
   :selection-inputs (mapv path-state (:selection-links identity))
   :configuration-inputs (mapv path-state (:configuration-files identity))
   :configuration-scope :explicit-frozen-file-set
   :native-dispatch-and-config-search-coverage :requires-independent-source-assessment
   :stdin :immediate-close :stdout-encoding "UTF-8"
   :stderr-encoding (.name (java.nio.charset.Charset/defaultCharset))
   :stream-retention :complete-as-shell-sh :environment-policy :inherit-unchanged})

(defn verify-git-binding! [binding]
  (h/ensure! (and (nil? shell/*sh-dir*) (nil? shell/*sh-env*)
                  (= (:cwd binding) (.getCanonicalPath (io/file (System/getProperty "user.dir"))))
                  (= binding (git-binding! (:root binding) (:identity-inputs binding))))
             :selected-Git-identity-drift))

(defn runtime-files [java-home jars]
  ;; Include the whole resolved JDK, not just its launcher and release file.
  (let [home (io/file java-home)]
    (h/ensure! (and (.isAbsolute home) (.isDirectory home)) :JDK-home)
    (mapv fingerprint
      (sort (distinct (concat (map #(.getCanonicalPath ^java.io.File %)
                                   (filter #(.isFile ^java.io.File %) (file-seq home))) jars))))))

(defn create-edn! [path value]
  (Files/write (.toPath (io/file path)) (.getBytes (pr-str value) "UTF-8")
    (into-array OpenOption [StandardOpenOption/CREATE_NEW StandardOpenOption/WRITE]))
  (fingerprint path))

(defn child-argv [prepared input]
  [(get-in prepared [:runtime :executable]) "-Xmx2g" "-cp"
   (str/join java.io.File/pathSeparator (get-in prepared [:closure :classpath]))
   "clojure.main" "-e"
   (str "(require 'gravity.syntax.selected-revision-request) "
        "(gravity.syntax.selected-revision-request/child! "
        (pr-str (:path input)) " " (pr-str (:sha256 input)) ")")])

(defn- read-input! [path expected-sha]
  (let [input (h/file-input! path)]
    (h/ensure! (= expected-sha (:sha256 input)) :request-input-drift)
    (let [prepared (edn/read-string (:text input))
          prepared (-> prepared
                       (assoc-in [:closure :argv] (child-argv prepared input))
                       (update-in [:closure :config-files] conj (dissoc input :text)))]
      (h/ensure! (= (:environment prepared) (environment)) :launcher-environment-drift)
      (h/ensure! (= host-profile (:host-profile prepared)) :request-host-profile-drift)
      prepared)))

(defn read-prepared! [path expected-sha]
  (let [prepared (read-input! path expected-sha)]
    (acceptance/recheck-closure! prepared)
    (acceptance/guard-configuration! prepared)
    prepared))

(defn child!
  "Explicit future child entrypoint. Never called by preparation or its tests.
  The parent must first bind the external watchdog/finalizer and admission."
  [path expected-sha]
  (let [input (read-input! path expected-sha)
        ports (assoc (runner-ports! (:root input)) :verify-git! verify-git-binding!)
        records (atom []) primary (atom nil)]
    ;; Only this fresh selected C3 child routes the harness Var. Unselected
    ;; harness source and all other shell/sh callers remain unchanged.
    (with-redefs [h/git! (partial git/git! ports (:git-binding input) records)]
      (try
        (let [prepared (read-prepared! path expected-sha)]
          (h/ensure! (= (:heap-bytes host-profile) (.maxMemory (Runtime/getRuntime))) :child-heap-profile)
          (try
            (require 'gravity.bootstrap)
            (let [result (acceptance/exercise! h/loaded-owner prepared)]
              (create-edn! (get-in prepared [:outputs :result]) result))
            (finally (acceptance/recheck-closure! prepared)
                     (acceptance/guard-configuration! prepared))))
        (catch Throwable error (reset! primary error) (throw error))
        (finally
          (try (create-edn! (get-in input [:outputs :git-observations]) (git/observation-view records))
               (catch Throwable error
                 (if-let [original @primary]
                   (when-not (identical? original error) (.addSuppressed ^Throwable original error))
                   (throw error)))))))))

(defn prepare!
  "Freeze files and a controller request only. Does not resolve runtime ports,
  select a profiler/session, acquire a guard, or start any process."
  [{:keys [root source directory java-home jars coordination-root git-identity]}]
  (let [prepared (h/prepare-inputs! root source)
        root (:root prepared)
        java-home (.getCanonicalPath (io/file java-home))
        jars (mapv #(.getCanonicalPath (io/file %)) jars)
        _ (h/ensure! (.isAbsolute (io/file directory)) :absolute-request-directory)
        directory (.getCanonicalFile (io/file directory))
        _ (h/ensure! (not (.exists directory)) :request-directory-already-exists)
        env (environment)
        _ (h/ensure! (every? nil? (vals (:Java-options env))) :unbound-Java-environment-options)
        executable (.getCanonicalPath (io/file java-home "bin/java"))
        cp (into [(str (io/file root "bootstrap/clojure/src"))
                  (str (io/file root "bootstrap/clojure/test"))] jars)
        runtime (runtime-files java-home jars)
        source-files (mapv fingerprint (acceptance/source-closure-paths root))
        runner (runner-binding! root)
        git-binding (git-binding! root git-identity)
        policy (fingerprint (str (io/file coordination-root "policy.edn")))
        _ (h/ensure! (.mkdirs directory) :create-request-directory)
        path #(str (io/file directory %))
        outputs {:result (path "result.edn") :parent-receipt (path "parent-receipt.edn")
                 :stdout (path "stdout.log") :stderr (path "stderr.log")
                 :whole-watchdog (path "whole-watchdog.edn") :git-observations (path "git-observations.edn")}
        guard {:coordination-root coordination-root :resource-class :exclusive
               :participants ["direct-C3-controller/normal-start-process"]
               :canonical-lock "/private/tmp/gravity-sh07-heavy.lock"
               :lock-mode :immediate-try-lock :acquire-order [:C :L] :release-order [:L :C]}
        guard-input (create-edn! (path "guard.edn") guard)
        prepared (assoc prepared :guard-config-path (:path guard-input)
                   :host-profile host-profile :environment env :outputs outputs
                   :runtime {:executable executable :java-home java-home} :git-binding git-binding
                   :closure {:source-files source-files :runtime-files runtime
                             :config-files [guard-input policy (fingerprint (str (io/file root "deps.edn")))]
                             :classpath cp :cwd root})
        _ (acceptance/guard-configuration! prepared)
        input (create-edn! (path "inputs.edn") prepared)
        request {:selector acceptance/selector :source source :prepared-input input
                 :cwd root :argv (child-argv prepared input) :outputs outputs
                 :host-profile host-profile :guard-proposal guard :guard-policy-input policy
                 :runner-binding runner
                 :git-binding git-binding
                 :host-time-assessment {:broker-wait-ms 60000 :child-wait-ms 120000
                                        :normal-finalization-ms 1500 :post-cleanup-wait-ms 100
                                        :two-capture-waits-ms 10000 :capture-post-close-waits-ms 200
                                        :nominal-sum-ms 191800
                                        :whole-proposal-ms 300000 :remaining-proposal-ms 108200
                                        :status :host-proposal-not-hard-native-bound
                                        :uncovered [:acquisition-native-calls :file-bookends
                                                    :capture-stream-close-native-calls :release-native-calls]}
                 :C3-creation-coverage
                 {:status :unestablished :direct-Java-child :parent-retains-raw-token
                  :known-nested-site {:source 'gravity.resolve.selected-source-harness/git!
                                      :source-input (first (filter #(str/ends-with? (:path %) "/gravity/resolve/selected_source_harness.clj") source-files))
                                      :runtime-input (first (filter #(re-find #"/clojure-[^/]+\.jar$" (:path %)) runtime))
                                      :selected-launcher 'gravity.syntax.selected-revision-git/git!
                                      :legacy-launcher 'clojure.java.shell/sh
                                      :wait 'java.lang.Process/waitFor :raw-handle-retention :same-public-token
                                      :capture :legacy-shell-compatible-complete-streams}
                  :missing [:native-Git-dispatch-and-config-search-coverage
                            :nested-native-creation-coverage
                            :remaining-genuine-C3-callgraph-coverage]
                  :tracked-to-owned-or-global-promotion false}
                 :profile-basis {:status :proposal-not-C3-measurement
                                 :origin :prior-prepared-2GiB-120s-child-300s-whole-profile
                                 :compiler-accounting false}
                 :participation {:acquiring-caller :direct-C3-controller
                                 :child :held-under-parent-lease-no-acquisition
                                 :launcher 'gravity.self-hosting.sh01-parallel-test-runner/start-process
                                 :shell false :warm-session false :profiler false
                                 :external-caller-coverage :parent-confirmation-required}
                 :controller-success-sequence
                 [:recheck-inputs :arm-whole-watchdog :acquire-C-exclusive
                  :try-L-immediately :recheck-inputs :start-direct-child :retain-immediately
                  :retain-raw-handles :wait-and-capture-same-token :finalize-same-token
                  :separate-C3-creation-coverage-check :recheck-inputs
                  :release-L :release-C :disarm-whole-watchdog :publish-parent-receipt]
                 :controller-failure-contract
                 {:C-refusal :no-L-or-child
                  :L-refusal :release-only-owned-C
                  :child-start-exception :preserve-exception-release-only-owned-guards
                  :owned-child :retain-original-handle-for-finalizer
                  :unknown-terminal-state :stop-preserve-handles-do-not-report-clean-or-released
                  :cleanup-exception :retain-primary-exception-and-cleanup-diagnostics
                  :interruption :preserve-interrupted-state
                  :guard-release :exactly-once-thread-affine-L-before-C
                  :unresolved-finalizer-policy :launch-blocking-dependency}
                 :existing-ports
                 {:acquire 'gravity.self-hosting.sh01-host-resource-broker/acquire!
                  :release 'gravity.self-hosting.sh01-host-resource-broker/release!
                  :launch 'gravity.self-hosting.sh01-parallel-test-runner/start-process
                  :retain 'gravity.self-hosting.sh01-parallel-test-runner/retain-owned-process!
                  :handles 'gravity.self-hosting.sh01-parallel-test-runner/retained-process-handles
                  :wait 'gravity.self-hosting.sh01-parallel-test-runner/process-result
                  :finalize 'gravity.self-hosting.sh01-parallel-test-runner/finalize-owned-process!
                  :L-open 'java.nio.channels.FileChannel/open
                  :L-acquire 'java.nio.channels.FileChannel/tryLock
                  :L-release 'java.nio.channels.FileLock/release}
                 :missing-interfaces
                 {:whole-watchdog {:API :unbound
                                   :covers [:acquisition :child :capture :finalization :release]}}
                 :parent-decisions [:shared-C-and-participating-callers-confirmation
                                    :workload-host-profile-review :exact-method-and-runtime-review]
                 :execution :never-submitted :launchable false
                 :terminal-evidence :not-established :genuine-consumer-acceptance :open}]
    ;; Validate the exact argv/input/config graph without resolving any owner.
    (read-prepared! (:path input) (:sha256 input))
    (assoc request :request-file (create-edn! (path "request.edn") request))))
