(ns gravity.syntax.selected-revision-request
  "C3-specific request preparation. Loading/preparing never launches an owner."
  (:require [clojure.edn :as edn] [clojure.java.io :as io]
            [clojure.string :as str]
            [gravity.resolve.selected-source-harness :as h]
            [gravity.syntax.selected-revision-acceptance :as acceptance])
  (:import (java.nio.file Files LinkOption OpenOption StandardOpenOption)
           (java.security DigestInputStream MessageDigest)))

(def host-profile
  ;; Proposed host restrictions, not Gravity input/admission accounting.
  {:heap-bytes 2147483648 :child-timeout-ms 120000 :broker-timeout-ms 60000
   :whole-operation-timeout-ms 300000 :max-capture-bytes 65536
   :max-capture-chars 65536 :capture-wait-ms 5000})

(def environment-keys ["JAVA_TOOL_OPTIONS" "JDK_JAVA_OPTIONS" "_JAVA_OPTIONS" "CLASSPATH"])
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

(defn read-prepared! [path expected-sha]
  (let [input (h/file-input! path)]
    (h/ensure! (= expected-sha (:sha256 input)) :request-input-drift)
    (let [prepared (edn/read-string (:text input))
          prepared (-> prepared
                       (assoc-in [:closure :argv] (child-argv prepared input))
                       (update-in [:closure :config-files] conj (dissoc input :text)))]
      (h/ensure! (= (:environment prepared) (environment)) :launcher-environment-drift)
      (h/ensure! (= host-profile (:host-profile prepared)) :request-host-profile-drift)
      (acceptance/recheck-closure! prepared)
      (acceptance/guard-configuration! prepared)
      prepared)))

(defn child!
  "Explicit future child entrypoint. Never called by preparation or its tests.
  The parent must first bind the external watchdog/finalizer and admission."
  [path expected-sha]
  (let [prepared (read-prepared! path expected-sha)]
    (h/ensure! (= (:heap-bytes host-profile) (.maxMemory (Runtime/getRuntime))) :child-heap-profile)
    (try
      (require 'gravity.bootstrap)
      (let [result (acceptance/exercise! h/loaded-owner prepared)]
        (create-edn! (get-in prepared [:outputs :result]) result))
      (finally (acceptance/recheck-closure! prepared)
               (acceptance/guard-configuration! prepared)))))

(defn prepare!
  "Freeze files and a controller request only. Does not resolve runtime ports,
  select a profiler/session, acquire a guard, or start any process."
  [{:keys [root source directory java-home jars coordination-root]}]
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
        policy (fingerprint (str (io/file coordination-root "policy.edn")))
        _ (h/ensure! (.mkdirs directory) :create-request-directory)
        path #(str (io/file directory %))
        outputs {:result (path "result.edn") :parent-receipt (path "parent-receipt.edn")
                 :stdout (path "stdout.log") :stderr (path "stderr.log")
                 :whole-watchdog (path "whole-watchdog.edn")}
        guard {:coordination-root coordination-root :resource-class :exclusive
               :participants ["direct-C3-controller/normal-start-process"]
               :canonical-lock "/private/tmp/gravity-sh07-heavy.lock"
               :lock-mode :immediate-try-lock :acquire-order [:C :L] :release-order [:L :C]}
        guard-input (create-edn! (path "guard.edn") guard)
        prepared (assoc prepared :guard-config-path (:path guard-input)
                   :host-profile host-profile :environment env :outputs outputs
                   :runtime {:executable executable :java-home java-home}
                   :closure {:source-files source-files :runtime-files runtime
                             :config-files [guard-input policy (fingerprint (str (io/file root "deps.edn")))]
                             :classpath cp :cwd root})
        _ (acceptance/guard-configuration! prepared)
        input (create-edn! (path "inputs.edn") prepared)
        request {:selector acceptance/selector :source source :prepared-input input
                 :cwd root :argv (child-argv prepared input) :outputs outputs
                 :host-profile host-profile :guard-proposal guard :guard-policy-input policy
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
                  :try-L-immediately :recheck-inputs :start-direct-child
                  :wait-and-capture :finalize-retained-owned-handles :recheck-inputs
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
                  :wait 'gravity.self-hosting.sh01-parallel-test-runner/process-result
                  :L-open 'java.nio.channels.FileChannel/open
                  :L-acquire 'java.nio.channels.FileChannel/tryLock
                  :L-release 'java.nio.channels.FileLock/release}
                 :missing-interfaces
                 {:retained-finalizer {:owner "01a17baa-ed84-75e4-846b-a1862288101e"
                                       :API :not-yet-published
                                       :requires [:raw-owned-handles :natural-exit-finalization
                                                  :root-and-tracked-descendant-status
                                                  :unknown-coverage-preserved :bounded-capture
                                                  :exactly-once-finalization :original-exception-and-interruption]}
                  :whole-watchdog {:API :unbound
                                   :covers [:acquisition :child :capture :finalization :release]}}
                 :parent-decisions [:shared-C-and-participating-callers-confirmation
                                    :workload-host-profile-review :exact-method-and-runtime-review]
                 :execution :never-submitted :launchable false
                 :terminal-evidence :not-established :genuine-consumer-acceptance :open}]
    ;; Validate the exact argv/input/config graph without resolving any owner.
    (read-prepared! (:path input) (:sha256 input))
    (assoc request :request-file (create-edn! (path "request.edn") request))))
