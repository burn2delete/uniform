(ns gravity.artifact-pipeline.owner-request
  "Preparation and concrete ports for the first OWN positive. No top-level IO,
  real namespace resolution, admission, lock acquisition or launch."
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as str]
            [gravity.artifact-pipeline.parent-controller :as controller])
  (:import [java.nio.channels FileChannel FileLock]
           [java.nio.file Files LinkOption OpenOption Paths StandardOpenOption]
           [java.security MessageDigest]))

(def controller-commit "5f7eda401162586cb3ace0a49f11e14e79985ee1")
(def controller-sha256 "44e1cf0c32eea3b7151cfc4c278ef1fe8ea5634b2fb7b8166f49b07163c3d24a")
(def canonical-path "/private/tmp/gravity-sh07-heavy.lock")
(def normal-sources
  {"gravity/self_hosting/sh01_host_resource_broker.clj" "0126b1d96f11d7e6908ea693f7f29a6d3d139cf30f22471ff6525229ca5ece20"
   "gravity/self_hosting/sh01_parallel_test_runner.clj" "72d8ea13e3cfebefc55f4e6350fcabc5bab58050f3f2a85b7058bf2ef097e0af"})
(def proposed-limits
  ;; OWN's historical 1200 s is only a proposal. Reserve the existing runner's
  ;; two capture waits/post-close waits, cleanup interval and root wait.
  {:whole-ms 1200000 :child-ms 1188200 :capture-bytes 1048576
   :capture-chars 1048576 :capture-wait-ms 5000 :cleanup-ms 1500})

(defn- ensure! [value reason]
  (when-not value (throw (ex-info "OWN request binding refused" {:reason reason}))))
(defn sha256 [bytes]
  (format "%064x" (java.math.BigInteger. 1 (.digest (MessageDigest/getInstance "SHA-256") bytes))))
(defn file-hash [path] (sha256 (Files/readAllBytes (.toPath (io/file path)))))
(defn- absolute [path] (.getCanonicalPath (io/file path)))
(defn- git [cwd & args]
  (let [result (apply shell/sh "/usr/bin/git" "--no-optional-locks" "-c" "core.fsmonitor=false"
                      "-C" cwd args)]
    (ensure! (zero? (:exit result)) :git-readback-failed)
    (str/trim (:out result))))
(defn- inventory [paths]
  (into (sorted-map)
        (for [path (sort (distinct paths))]
          [path (file-hash path)])))
(defn- resources [roots]
  (vec (sort (mapcat (fn [path]
                       (let [file (io/file path)]
                         (if (.isDirectory file)
                           (map #(.getCanonicalPath ^java.io.File %)
                                (filter #(.isFile ^java.io.File %) (file-seq file)))
                           [(absolute path)]))) roots))))
(defn- environment []
  (into (sorted-map) (map (fn [[key value]] [key (sha256 (.getBytes ^String value "UTF-8"))]) (System/getenv))))
(defn create-output! [path bytes]
  (Files/write (.toPath (io/file path)) bytes
               (into-array OpenOption [StandardOpenOption/CREATE_NEW StandardOpenOption/WRITE])))
(defn- write-edn! [path value]
  (create-output! path (.getBytes (str (pr-str value) "\n") "UTF-8")))

(defn prepare!
  "Freeze a clean child, loader closure and proposed profile; does not resolve
  broker/runner namespaces or call any operation port. output-root must be a
  new directory. No current-parent acceptance is inferred from these bytes."
  [child-root control-root output-root child-classpath config-files]
  (let [cwd (absolute child-root)
        control-root (absolute control-root)
        _ (ensure! (= (:commit controller/source) (git cwd "rev-parse" "HEAD")) :child-commit)
        _ (ensure! (= (:tree controller/source) (git cwd "rev-parse" "HEAD^{tree}")) :child-tree)
        _ (ensure! (str/blank? (git cwd "status" "--porcelain")) :dirty-child)
        controller-file (str control-root "/bootstrap/clojure/test/gravity/artifact_pipeline/parent_controller.clj")
        _ (ensure! (= controller-sha256 (file-hash controller-file)) :controller-source)
        tracked (mapv #(str cwd "/" %) (str/split (git cwd "ls-files" "-z") #"\u0000"))
        cp (mapv #(absolute (if (.isAbsolute (io/file %)) % (str cwd "/" %))) child-classpath)
        parent-cp (into [(str control-root "/bootstrap/clojure/test")] cp)
        java-home (absolute (System/getProperty "java.home"))
        java (str java-home "/bin/java")
        source-files (resources [(str control-root "/bootstrap/clojure/test/gravity/artifact_pipeline")])
        _ (doseq [[resource digest] normal-sources]
            (ensure! (= digest (file-hash (str cwd "/bootstrap/clojure/test/" resource))
                        (file-hash (str control-root "/bootstrap/clojure/test/" resource))) :normal-source-equivalence))
        root (str (absolute (git cwd "rev-parse" "--git-common-dir")) "/gravity-development-loop-v1/broker")
        id (str "own-first-positive-" (java.util.UUID/randomUUID))
        output-root (.toAbsolutePath (.toPath (io/file output-root)))
        outputs (into {} (for [key [:attempt :stdout :stderr :final]] [key (str output-root "/" (name key) ".edn")]))
        class-files (resources parent-cp)
        frozen {:cwd cwd :source controller/source :controller {:commit controller-commit :file controller-file :sha256 controller-sha256}
                :tracked-inputs (inventory tracked) :loader-resources (inventory class-files)
                :loader-inventory class-files :classpath parent-cp :child-classpath cp
                :runtime {:home java-home :version (System/getProperty "java.runtime.version")
                          :files (inventory (resources [java-home]))}
                :configuration (inventory (map absolute config-files)) :environment-sha256 (environment)
                :adapter-sources (inventory source-files)}
        adapter-source (str control-root "/bootstrap/clojure/test/gravity/artifact_pipeline/owner_request.clj")
        local-id (fn [name] {:file adapter-source :sha256 (file-hash adapter-source) :function (str 'gravity.artifact-pipeline.owner-request "/" name)})
        identities (into {} (for [port controller/port-names] [port (assoc (local-id "assemble-ports") :port port)]))
        guards [{:kind :broker :acquisition :broker :resource-class :exclusive :root root
                 :source-id {:resource "gravity/self_hosting/sh01_host_resource_broker.clj" :sha256 (get normal-sources "gravity/self_hosting/sh01_host_resource_broker.clj")}}
                {:kind :canonical :acquisition :non-broker :root canonical-path :source-id (local-id "canonical-acquire!")
                 :acquire-function (:acquire-guard identities)}]
        participants [{:owner :own-outer-controller :broker-root root :resource-class :exclusive :canonical canonical-path}
                      {:owner :direct-development-runner :broker-acquisition false}
                      {:owner :direct-owner-chain :broker-acquisition false}]
        request {:id id :source controller/source :selector controller/selector :argv controller/argv :cwd cwd
                 :runtime {:classpath parent-cp :jars (filterv #(str/ends-with? % ".jar") cp)
                           :executables [java] :configuration {:files (:configuration frozen) :environment (:environment-sha256 frozen)}}
                 :port-identities identities :config {:profile :own-first-positive-proposal :parent-acceptance :pending}
                 :outputs outputs :limits proposed-limits :guards guards
                 :coverage {:authority-source :current-parent-proposal :decision-id :pending-parent-acceptance
                            :participants participants :covered-participants participants :ordered-guards guards}}
        command (into [java "-Xmx2g" "-cp" (str/join java.io.File/pathSeparator cp)
                       "clojure.main" "-m" "gravity.development-test-runner"] (subvec controller/argv 3))
        proposal {:request request :request-sha256 (sha256 (.getBytes (pr-str request) "UTF-8"))
                  :frozen frozen :actual-child-command command :command-sha256 (sha256 (.getBytes (pr-str command) "UTF-8")) :parent-heap-proposal "256m"
                  :host-watchdog-proposal-ms 1200000 :normal-sources normal-sources
                  :unavailable {:admission {:interface :current-parent-acceptance :status :pending}
                                :owned-finalizer {:interface :retained-owned-descendants-on-natural-exit :status :absent-from-normal-runner}
                                :host-watchdog {:interface :platform-action-supervision :status :not-bound-in-source-controller}}}]
    (Files/createDirectory output-root (make-array java.nio.file.attribute.FileAttribute 0))
    (write-edn! (str output-root "/first-request.edn") proposal)
    proposal))

(defn verify! [proposal]
  (let [{:keys [cwd tracked-inputs loader-resources loader-inventory classpath runtime configuration adapter-sources]} (:frozen proposal)]
    (ensure! (= (:request-sha256 proposal) (sha256 (.getBytes (pr-str (:request proposal)) "UTF-8"))) :request-drift)
    (ensure! (= (:command-sha256 proposal) (sha256 (.getBytes (pr-str (:actual-child-command proposal)) "UTF-8"))) :command-drift)
    (ensure! (= controller/selector (get-in proposal [:request :selector])) :selector-identity)
    (ensure! (= controller/argv (get-in proposal [:request :argv])) :argv-identity)
    (ensure! (= cwd (get-in proposal [:request :cwd])) :cwd-identity)
    (ensure! (= (:source (:request proposal)) controller/source) :operation-identity)
    (ensure! (= (:commit controller/source) (git cwd "rev-parse" "HEAD")) :child-commit)
    (ensure! (= (:tree controller/source) (git cwd "rev-parse" "HEAD^{tree}")) :child-tree)
    (ensure! (str/blank? (git cwd "status" "--porcelain")) :dirty-child)
    (ensure! (= loader-inventory (resources classpath)) :loader-inventory)
    (doseq [files [tracked-inputs loader-resources (:files runtime) configuration adapter-sources]]
      (ensure! (= files (inventory (keys files))) :input-drift))
    (ensure! (= classpath (str/split (System/getProperty "java.class.path") (re-pattern java.io.File/pathSeparator))) :parent-classpath)
    (ensure! (= (:home runtime) (absolute (System/getProperty "java.home"))) :runtime-home)
    (ensure! (= (:version runtime) (System/getProperty "java.runtime.version")) :runtime-version)
    (ensure! (= (:environment-sha256 (:frozen proposal)) (environment)) :environment-drift)
    (ensure! (= (:sha256 (:controller (:frozen proposal)))
                (file-hash (:file (:controller (:frozen proposal))))) :controller-source)
    (select-keys (:request proposal) controller/identity-fields)))

(defn canonical-acquire! []
  (let [channel (FileChannel/open (Paths/get canonical-path (make-array String 0))
                                  (into-array OpenOption [StandardOpenOption/CREATE StandardOpenOption/WRITE LinkOption/NOFOLLOW_LINKS]))]
    (try
      (let [lock (.tryLock channel)]
        (ensure! (some? lock) :canonical-busy)
        {:channel channel :lock lock :released? (atom false)})
      (catch Throwable error (.close channel) (throw error)))))
(defn canonical-validate! [{:keys [^FileChannel channel ^FileLock lock released?] :as handle}]
  (ensure! (and (.isOpen channel) (.isValid lock) (false? @released?)) :canonical-invalid)
  handle)
(defn canonical-release! [{:keys [^FileChannel channel ^FileLock lock released?]}]
  (ensure! (compare-and-set! released? false true) :canonical-release-repeated)
  (try (.release lock) (finally (.close channel)))
  {:status :released :evidence-id :canonical-lock-and-channel-released})

(defn owned-finalizer
  "Consume the normal cleanup result without retry. No normal-runner API
  retains descendant ownership on natural exit; root exit cannot fill it."
  [process result _error _bounds]
  {:status :unknown :owned-descendants-complete? false :root-alive? (.isAlive ^Process process)
   :reason :retained-owned-descendants-interface-unavailable :runner-cleanup (:cleanup result)})

(defn- assemble-ports [proposal normal]
  (let [request (:request proposal) root (get-in request [:guards 0 :root])
        reserved? (atom false) finished? (atom false) supervised? (atom false) published? (atom false)
        captured (atom nil)]
    {:clock #(quot (System/nanoTime) 1000000)
     :admit (fn [& _] (throw (ex-info "Current parent has not accepted this OWN profile" {:reason :own-profile-unaccepted})))
     :reserve (fn [outputs _]
                (ensure! (= outputs (:outputs request)) :output-identity)
                (ensure! (compare-and-set! reserved? false true) :reservation-repeated)
                (doseq [path (vals outputs)] (ensure! (not (Files/exists (.toPath (io/file path)) (make-array LinkOption 0))) :output-already-exists))
                (write-edn! (:attempt outputs) {:request-id (:id request) :status :claimed}) true)
     :acquire (fn [guard remaining]
                (ensure! (= guard (first (:guards request))) :broker-guard-identity)
                ((:acquire normal) {:coordination-root root :timeout-ms remaining} :exclusive))
     :acquire-guard (fn [guard _]
                      (ensure! (= guard (second (:guards request))) :canonical-guard-identity)
                      (canonical-acquire!))
     :validate (fn [guard handle]
                 (if (= :broker (:acquisition guard))
                   ((:validate normal) handle root :exclusive) (canonical-validate! handle)))
     :release (fn [guard handle]
                (if (= :broker (:acquisition guard))
                  (let [receipt ((:release normal) handle)]
                    (ensure! (= :released (:outcome receipt)) :broker-release-not-final)
                    {:status :released :broker-receipt receipt})
                  (canonical-release! handle)))
     :start (fn [argv cwd _]
              (ensure! (= argv controller/argv) :child-argv)
              (ensure! (= cwd (:cwd request)) :child-cwd)
              (verify! proposal)
              ((:start normal) (:actual-child-command proposal) cwd))
     :supervise (fn [process limits]
                  (ensure! (compare-and-set! supervised? false true) :supervision-repeated)
                  (let [timeout (min (:child-ms proposed-limits) (- (:timeout-ms limits) 11800))
                        _ (ensure! (pos? timeout) :insufficient-child-deadline)
                        result ((:supervise normal) process
                                                    {:timeout-ms timeout
                                                     :max-capture-bytes (:capture-bytes limits) :max-capture-chars (:capture-chars limits)
                                                     :capture-wait-ms (:capture-wait-ms limits)})]
                    (reset! captured result)
                    result))
     :finish (fn [& args]
               (ensure! (compare-and-set! finished? false true) :finalization-repeated)
               (apply owned-finalizer args))
     :observe #(verify! proposal)
     :publish (fn [path receipt]
                (ensure! (= path (get-in request [:outputs :final])) :publication-identity)
                (ensure! (compare-and-set! published? false true) :publication-repeated)
                (when @captured
                  (doseq [key [:stdout :stderr]]
                    (create-output! (get-in request [:outputs key]) (.getBytes ^String (get @captured key) "UTF-8"))))
                (write-edn! path receipt) {:status :published})}))

(defn operation-ports
  "Later binding only, never called during preparation. Resolve exact existing
  functions after source/loader checks. This proposal's admission always refuses."
  [proposal]
  (verify! proposal)
  (doseq [[resource digest] normal-sources]
    (let [url (io/resource resource)]
      (ensure! (and url (= "file" (.getProtocol url)) (= digest (file-hash (io/file (.toURI url))))) :normal-loader-source)))
  (let [broker 'gravity.self-hosting.sh01-host-resource-broker
        runner 'gravity.self-hosting.sh01-parallel-test-runner
        _ (require broker runner)
        resolve-value (fn [namespace name] (var-get (ns-resolve namespace name)))]
    (assemble-ports proposal {:acquire (resolve-value broker 'acquire!) :validate (resolve-value broker 'validate-live-lease!)
                              :release (resolve-value broker 'release!) :start (resolve-value runner 'start-process)
                              :supervise (resolve-value runner 'process-result)})))
