(ns gravity.self-hosting.sh01-development-loop-wiring
  "Thin parent-side wiring for incremental development test reuse.

  This namespace computes complete conservative requests before child launch,
  probes immutable hits before executor submission, and keeps the existing
  cache singleflight and host broker at the process execution boundary. It
  grants no test, proof, benchmark, integration, release, self-hosting, or
  seed-retirement authority."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [gravity.digest :as digest]
            [gravity.self-hosting.sh01-development-test-cache :as cache]
            [gravity.self-hosting.sh01-host-resource-broker :as broker])
  (:import [java.io ByteArrayOutputStream]
           [java.nio ByteBuffer]
           [java.nio.charset StandardCharsets]
           [java.nio.file Files LinkOption Path]
           [java.nio.file.attribute BasicFileAttributes FileAttribute
            PosixFilePermission PosixFilePermissions]
           [java.security MessageDigest]))

(def ^:private default-timeout-ms (* 60 60 1000))
(def ^:private maximum-path-count 100000)
(def ^:private no-links (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
(def ^:private no-file-attributes (make-array FileAttribute 0))
(def ^:private private-directory-permissions
  (PosixFilePermissions/fromString "rwx------"))
(def ^:private private-directory-attribute
  (PosixFilePermissions/asFileAttribute private-directory-permissions))
(def ^:private result-shaping-option-keys
  [:output-limit-bytes :output-limit-chars
   :capture-limit-bytes :capture-limit-chars
   :output-cap-bytes :capture-cap-bytes
   :max-output-bytes :max-capture-bytes
   :output-cap-chars :capture-cap-chars
   :max-output-chars :max-capture-chars
   :output-cap :capture-cap :output-limit :capture-limit
   :stdout-limit-bytes :stdout-limit-chars
   :stdout-limit :stdout-chars :stdout-max-bytes :stdout-max-chars
   :stderr-limit-bytes :stderr-limit-chars
   :stderr-limit :stderr-chars :stderr-max-bytes :stderr-max-chars
   :capture-wait-ms])

(defn- fail!
  [id message data]
  (throw
   (ex-info message
            (merge {:id id
                    :authority :non-authoritative
                    :authoritative? false}
                   data))))

(defn- sha256-id
  [text]
  (str "sha256:" (digest/sha256-hex text)))

(defn- message-digest-id
  [^MessageDigest message-digest]
  (str "sha256:"
       (apply str
              (map #(format "%02x" (bit-and % 0xff))
                   (.digest message-digest)))))

(defn- run-git-bytes!
  [working-directory arguments]
  (let [directory (if (instance? Path working-directory)
                    (.toFile ^Path working-directory)
                    (io/file working-directory))
        command (into ["git"] arguments)
        process (-> (ProcessBuilder. ^java.util.List command)
                    (.directory directory)
                    (.redirectErrorStream true)
                    .start)
        output (ByteArrayOutputStream.)]
    (with-open [input (.getInputStream process)]
      (.transferTo input output))
    (let [exit-code (.waitFor process)
          bytes (.toByteArray output)]
      (when-not (zero? exit-code)
        (fail! "SH01-DEVELOPMENT-LOOP-GIT"
               "Development-loop repository identity command failed"
               {:command command
                :exit-code exit-code
                :output (String. bytes StandardCharsets/UTF_8)}))
      bytes)))

(defn- git-common-directory
  [working-directory]
  (let [text (str/trim
              (String.
               (run-git-bytes!
                working-directory
                ["rev-parse" "--path-format=absolute" "--git-common-dir"])
               StandardCharsets/UTF_8))
        path (.normalize (.toAbsolutePath (.toPath (io/file text))))]
    (when (or (str/blank? text)
              (str/includes? text "\n")
              (not (.isAbsolute path)))
      (fail! "SH01-DEVELOPMENT-LOOP-GIT"
             "Git common directory identity is malformed"
             {:git-common-directory text}))
    path))

(defn- git-repository-root
  [working-directory]
  (let [text (str/trim
              (String.
               (run-git-bytes!
                working-directory
                ["rev-parse" "--path-format=absolute" "--show-toplevel"])
               StandardCharsets/UTF_8))
        root (.normalize (.toAbsolutePath (.toPath (io/file text))))
        supplied (if (instance? Path working-directory)
                   (.normalize (.toAbsolutePath ^Path working-directory))
                   (.normalize (.toAbsolutePath
                                (.toPath (io/file working-directory)))))]
    (when-not (= root supplied)
      (fail! "SH01-DEVELOPMENT-LOOP-ROOT"
             "Development-loop working directory must be the Git top level"
             {:working-directory (str supplied)
              :git-top-level (str root)}))
    root))

(defn- private-directory!
  [^Path path]
  (let [path (.normalize (.toAbsolutePath path))
        existed? (Files/exists path no-links)]
    (when (Files/isSymbolicLink path)
      (fail! "SH01-DEVELOPMENT-LOOP-PATH"
             "Development-loop coordination directory cannot be a symlink"
             {:path (str path)}))
    (when-not existed?
      (try
        (Files/createDirectories
         path
         (into-array FileAttribute [private-directory-attribute]))
        (catch java.nio.file.FileAlreadyExistsException _ nil)))
    (when (or (Files/isSymbolicLink path)
              (not (Files/isDirectory path no-links)))
      (fail! "SH01-DEVELOPMENT-LOOP-PATH"
             "Development-loop coordination path is not a directory"
             {:path (str path)}))
    (let [home (.toPath (io/file (System/getProperty "user.home")))]
      (when-not (= (Files/getOwner path no-links)
                   (Files/getOwner home no-links))
        (fail! "SH01-DEVELOPMENT-LOOP-PATH"
               "Development-loop coordination directory has another owner"
               {:path (str path)})))
    (when-not (= private-directory-permissions
                 (Files/getPosixFilePermissions path no-links))
      (fail! "SH01-DEVELOPMENT-LOOP-PATH"
             "Development-loop coordination directory must have mode 0700"
             {:path (str path)}))
    path))

(defn- configured-path!
  [value label]
  (let [path (if (instance? Path value)
               value
               (.toPath (io/file value)))]
    (when-not (.isAbsolute ^Path path)
      (fail! "SH01-DEVELOPMENT-LOOP-PATH"
             "Configured development-loop directory must be absolute"
             {:path-label label :path (str value)}))
    path))

(defn- repository-paths
  [working-directory]
  (let [raw (String.
             (run-git-bytes!
              working-directory
              ["ls-files" "-z" "--cached" "--others"
               "--exclude-standard"])
             StandardCharsets/UTF_8)
        paths (->> (str/split raw #"\u0000" -1)
                   (remove str/blank?)
                   distinct
                   sort
                   vec)]
    (when (> (count paths) maximum-path-count)
      (fail! "SH01-DEVELOPMENT-LOOP-SNAPSHOT-LIMIT"
             "Repository snapshot path count exceeds its reviewed bound"
             {:path-count (count paths) :maximum maximum-path-count}))
    paths))

(defn- update-framed-bytes!
  [^MessageDigest message-digest bytes]
  (.update message-digest
           (.array (doto (ByteBuffer/allocate 8)
                     (.putLong (long (alength ^bytes bytes))))))
  (.update message-digest ^bytes bytes))

(defn- update-framed-text!
  [^MessageDigest message-digest text]
  (update-framed-bytes!
   message-digest
   (.getBytes ^String (str text) StandardCharsets/UTF_8)))

(defn- same-file-snapshot?
  [^BasicFileAttributes before ^BasicFileAttributes after]
  (and (= (.fileKey before) (.fileKey after))
       (= (.size before) (.size after))
       (= (.lastModifiedTime before) (.lastModifiedTime after))
       (= (.isRegularFile before) (.isRegularFile after))
       (= (.isSymbolicLink before) (.isSymbolicLink after))))

(defn- update-file!
  [^MessageDigest message-digest ^Path repository-root relative]
  (let [path (.normalize (.resolve repository-root ^String relative))]
    (when-not (.startsWith path repository-root)
      (fail! "SH01-DEVELOPMENT-LOOP-PATH"
             "Repository snapshot path escaped its root"
             {:path relative}))
    (update-framed-text! message-digest relative)
    (if-not (Files/exists path no-links)
      (update-framed-text! message-digest :absent)
      (let [before (Files/readAttributes path BasicFileAttributes no-links)]
        (cond
          (.isSymbolicLink before)
          (fail! "SH01-DEVELOPMENT-LOOP-SYMLINK"
                 "Repository symlinks are outside the complete cache closure"
                 {:path relative})

          (.isRegularFile before)
          (do
            (update-framed-text! message-digest :regular-file)
            (update-framed-text! message-digest
                                 (if (Files/isExecutable path) :executable
                                     :non-executable))
            (update-framed-text! message-digest (.size before))
            (with-open [input (Files/newInputStream path
                                                   (make-array
                                                    java.nio.file.OpenOption
                                                    0))]
              (let [buffer (byte-array 65536)]
                (loop []
                  (let [count (.read input buffer)]
                    (when (pos? count)
                      (.update message-digest buffer 0 count)
                      (recur)))))))

          :else
          (fail! "SH01-DEVELOPMENT-LOOP-PATH"
                 "Repository snapshot contains an unsupported path kind"
                 {:path relative}))
        (let [after (Files/readAttributes path BasicFileAttributes no-links)]
          (when-not (same-file-snapshot? before after)
            (fail! "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
                   "Repository input changed while its cache identity was read"
                   {:path relative})))))))

(defn- regular-file-identity
  [^Path input-path]
  (let [path (.toRealPath input-path (make-array LinkOption 0))
        before (Files/readAttributes path BasicFileAttributes no-links)]
    (when-not (.isRegularFile before)
      (fail! "SH01-DEVELOPMENT-LOOP-TOOL"
             "Development-loop external input must be a regular file"
             {}))
    (let [message-digest (MessageDigest/getInstance "SHA-256")]
      (with-open [input (Files/newInputStream
                         path (make-array java.nio.file.OpenOption 0))]
        (let [buffer (byte-array 65536)]
          (loop []
            (let [count (.read input buffer)]
              (when (pos? count)
                (.update message-digest buffer 0 count)
                (recur))))))
      (when-not (same-file-snapshot?
                 before
                 (Files/readAttributes path BasicFileAttributes no-links))
        (fail! "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
               "External runtime or classpath input changed while read"
               {}))
      (message-digest-id message-digest))))

(defn repository-snapshot
  "Return one conservative identity over all tracked and untracked inputs."
  [working-directory]
  (let [root (git-repository-root working-directory)
        paths (repository-paths root)
        message-digest (MessageDigest/getInstance "SHA-256")]
    (update-framed-text! message-digest
                         :gravity/sh01-complete-repository-snapshot-v1)
    (doseq [relative paths]
      (update-file! message-digest root relative))
    (when-not (= paths (repository-paths root))
      (fail! "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
             "Repository path inventory changed while its identity was read"
             {}))
    {:schema :gravity/sh01-complete-repository-snapshot-v1
     :repository-identity (message-digest-id message-digest)
     :path-count (count paths)}))

(defn- classpath-identities
  [^Path repository-root repository-identity]
  (let [separator (java.util.regex.Pattern/quote
                   (System/getProperty "path.separator"))
        entries (str/split (System/getProperty "java.class.path")
                           (re-pattern separator) -1)]
    (mapv
     (fn [index entry]
       (let [path (if (str/blank? entry)
                    repository-root
                    (.normalize
                     (.toAbsolutePath
                      (.toPath (io/file entry)))))
             internal? (.startsWith path repository-root)
             identity
             (cond
               internal?
               (sha256-id
                (pr-str
                 (array-map
                  :repository-identity repository-identity
                  :relative-entry
                  (str (.relativize repository-root path)))))

               (Files/isRegularFile path (make-array LinkOption 0))
               (regular-file-identity path)

               :else
               (fail! "SH01-DEVELOPMENT-LOOP-CLASSPATH"
                      "External classpath entries must be regular files"
                      {:entry-index index}))]
         {:path (format "classpath/entry-%04d" index)
          :sha256 identity}))
     (range)
     entries)))

(defn- resolve-executable
  [^Path repository-root executable]
  (let [candidate (.toPath (io/file executable))]
    (cond
      (.isAbsolute candidate) candidate

      (str/includes? executable java.io.File/separator)
      (.resolve repository-root candidate)

      :else
      (or
       (some
        (fn [directory]
          (let [path (.resolve (.toPath (io/file directory)) executable)]
            (when (and (Files/isRegularFile path (make-array LinkOption 0))
                       (Files/isExecutable path))
              path)))
        (str/split (or (System/getenv "PATH") "")
                   (re-pattern
                    (java.util.regex.Pattern/quote
                     (System/getProperty "path.separator")))))
       (fail! "SH01-DEVELOPMENT-LOOP-TOOL"
              "Development-loop command executable could not be resolved"
              {:executable executable})))))

(declare semantic-command)

(defn- base-runtime-identities
  [^Path repository-root options]
  (let [command (semantic-command options)
        java-executable
        (.resolve (.toPath (io/file (System/getProperty "java.home")))
                  "bin/java")
        test-executable (resolve-executable repository-root (first command))]
    [{:id :clojure-runtime
      :sha256 (sha256-id (str (clojure-version)))}
     {:id :java-runtime
      :sha256
      (sha256-id
       (pr-str
        (array-map
         :java-runtime-version (System/getProperty "java.runtime.version")
         :java-vm-name (System/getProperty "java.vm.name")
         :java-vm-version (System/getProperty "java.vm.version")
         :os-name (System/getProperty "os.name")
         :os-arch (System/getProperty "os.arch"))))}
     {:id :java-executable
      :sha256 (regular-file-identity java-executable)}
     {:id :test-command-executable
      :sha256 (regular-file-identity test-executable)}]))

(defn prepare-context
  "Prepare shared roots and one complete repository identity in the parent."
  [working-directory options]
  (let [working-directory
        (if (instance? Path working-directory)
          (.normalize (.toAbsolutePath ^Path working-directory))
          (.normalize (.toAbsolutePath (.toPath (io/file working-directory)))))
        common-directory (git-common-directory working-directory)
        shared-root (private-directory!
                     (.resolve common-directory
                               "gravity-development-loop-v1"))
        cache-directory
        (private-directory!
         (if-let [configured (:cache-directory options)]
           (configured-path! configured :cache-directory)
           (.resolve shared-root "cache")))
        broker-root
        (private-directory!
         (if-let [configured (or (:broker-root options)
                                 (:coordination-root options))]
           (configured-path! configured :broker-root)
           (.resolve shared-root "broker")))
        timeout-ms (long (or (:timeout-ms options) default-timeout-ms))
        snapshot (repository-snapshot working-directory)]
    (when-not (pos? timeout-ms)
      (fail! "SH01-DEVELOPMENT-LOOP-TIMEOUT"
             "Development-loop child timeout must be positive"
             {:timeout-ms timeout-ms}))
    {:schema :gravity/sh01-development-loop-context-v1
     :working-directory (str working-directory)
     :cache-directory cache-directory
     :broker-root broker-root
     :timeout-ms timeout-ms
     :snapshot snapshot
     :revalidate-snapshot? true
     :classpath-inputs
     (classpath-identities working-directory
                           (:repository-identity snapshot))
     :runtime-tool-inputs
     (base-runtime-identities working-directory options)
     :launch-count (atom 0)
     :cache-receipts (atom [])}))

(defn- policy-for-job
  [plan job options timeout-ms]
  (let [jobs (or (:batch-jobs job) [job])
        policies (mapv :test-policy jobs)
        reviewed-policy
        (when (and (seq policies)
                   (every? map? policies)
                   (apply = policies))
          (first policies))
        authoritative?
        (or (true? (:authoritative? plan))
            (= :authoritative (:authority plan))
            (true? (:authoritative? options)))
        nondeterministic?
        (or (true? (:nondeterministic? options))
            (some #(true? (:nondeterministic? %)) jobs))
        custom-operation?
        (boolean (or (:process-launcher options)
                     (:worker options)
                     (:normal-batch-worker options)))
        custom-operation-identified?
        (map? (:development-operation-identity options))]
    {:authority (if authoritative? :authoritative :non-authoritative)
     :deterministic? (boolean (and (:deterministic? reviewed-policy)
                                   (not nondeterministic?)
                                   (or (not custom-operation?)
                                       custom-operation-identified?)))
     :performance? (boolean (or (:performance? reviewed-policy)
                                (:performance? options)
                                (some :performance? jobs)))
     :proof? (boolean (or (:proof? reviewed-policy)
                          (:proof? options)
                          (some :proof? jobs)))
     :freshness-required?
     (boolean (or (:freshness-required? reviewed-policy)
                  (:freshness-required? options)
                  (:fresh? options)
                  (some :freshness-required? jobs)))
     :timeout-ms timeout-ms}))

(defn- runtime-identities
  [options]
  (cond->
   [{:id :clojure-runtime
     :sha256 (sha256-id (str (clojure-version)))}
    {:id :java-runtime
     :sha256
     (sha256-id
      (pr-str
       (array-map
        :java-runtime-version (System/getProperty "java.runtime.version")
        :java-vm-name (System/getProperty "java.vm.name")
        :java-vm-version (System/getProperty "java.vm.version")
        :os-name (System/getProperty "os.name")
        :os-arch (System/getProperty "os.arch"))))}]
    (:development-operation-identity options)
    (conj (:development-operation-identity options))))

(defn- semantic-command
  [options]
  (let [command (or (:command options) (:clojure-command options))]
    (cond
      (nil? command) ["clojure" "-M:test"]
      (vector? command) (mapv str command)
      (sequential? command) (mapv str command)
      :else [(str command) "-M:test"])))

(defn cache-request
  "Build the closed cache request for one scheduled execution unit."
  [context plan job options]
  (let [repository-identity
        (get-in context [:snapshot :repository-identity])
        timeout-ms (:timeout-ms context)
        policy (policy-for-job plan job options timeout-ms)
        namespaces (mapv (comp str :namespace)
                         (or (:batch-jobs job) [job]))
        command (semantic-command options)
        test-material
        (array-map
         :schema :gravity/sh01-development-test-unit-v1
         :namespaces namespaces
         :component-id (:component-id job)
         :batch-key (:batch-key job)
         :resource-class (:resource-class job)
         :command command
         :fail-fast? (boolean (:fail-fast? options))
         :result-shaping-options
         (select-keys options result-shaping-option-keys)
         :policy policy)
        snapshot-input
        (fn [path] {:path path :sha256 repository-identity})]
    {:cache-directory (:cache-directory context)
     :repository-identity repository-identity
     :test-identity
     {:id (str "sh01-development-unit/"
               (subs (sha256-id (pr-str test-material)) 7))
      :sha256 (sha256-id (pr-str test-material))}
     :test-policy policy
     :dependencies
     {:complete? true
      :production-inputs
      [(snapshot-input "repository/complete-production-closure-v1")]
      :transitive-production-inputs
      [(snapshot-input "repository/complete-transitive-closure-v1")]
      :fixture-contract-inputs
      [(snapshot-input "repository/complete-fixture-contract-closure-v1")]
      :runner-identity
      {:id :sh01-development-loop-runner
       :sha256 repository-identity}
      :classpath-inputs
      (or (:classpath-inputs context)
          [(snapshot-input "repository/complete-classpath-closure-v1")])
      :runtime-tool-inputs
      (vec (concat (or (:runtime-tool-inputs context)
                       (runtime-identities {}))
                   (when (:development-operation-identity options)
                     [(:development-operation-identity options)])))}}))

(defn attach-request
  "Attach a fully computed parent-side request to one execution unit."
  [context plan options job]
  (assoc job :development-cache-request
         (cache-request context plan job options)))

(defn- record-cache-receipt!
  [context phase job receipt]
  (swap! (:cache-receipts context)
         conj
         {:phase phase
          :namespaces (mapv (comp str :namespace)
                            (or (:batch-jobs job) [job]))
          :receipt receipt}))

(defn- verify-snapshot!
  [context]
  (when (:revalidate-snapshot? context)
    (when-not (= (:snapshot context)
                 (repository-snapshot (:working-directory context)))
      (fail! "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
             "Repository inputs changed after parent cache-key computation"
             {})))
  nil)

(defn probe
  "Probe in the parent and return the unchanged job or its immutable hit."
  [context job]
  (let [outcome (cache/lookup! (:development-cache-request job))
        receipt (:receipt outcome)]
    (record-cache-receipt! context :parent-probe job receipt)
    (if (= :hit (:decision receipt))
      (do
        (verify-snapshot! context)
        {:job job
         :hit-result
         (assoc (:result outcome)
                :cache-receipt receipt
                :cache-hit? true
                :child-jvm-launched? false
                :broker-acquired-this-run? false)})
      {:job job :hit-result nil})))

(defn record-launch!
  "Record one successful ProcessBuilder or injected launcher return."
  [context]
  (swap! (:launch-count context) inc)
  nil)

(defn- brokered-result
  [context resource-class operation]
  (let [lease (broker/acquire!
               {:coordination-root (:broker-root context)
                :timeout-ms (:timeout-ms context)}
               resource-class)]
    (try
      (let [result (operation)
            release-receipt (broker/release! lease)]
        (if (map? result)
          (assoc result
                 :producer-broker-receipts
                 [(:receipt lease) release-receipt])
          result))
      (catch Throwable throwable
        (let [release-outcome
              (when-not (true? @(:released? lease))
                (try
                  {:receipt (broker/release! lease)}
                  (catch Throwable release-error
                    {:error (or (.getMessage release-error)
                                (str release-error))
                     :diagnostic-id (:id (ex-data release-error))
                     :receipt (:receipt (ex-data release-error))})))
              receipts
              (vec
               (remove nil?
                       [(:receipt lease) (:receipt release-outcome)]))]
          (throw
           (ex-info
            (or (.getMessage throwable) (str throwable))
            (cond-> (assoc (or (ex-data throwable) {})
                           :broker-receipts receipts)
              (:error release-outcome)
              (assoc :broker-release-error (:error release-outcome)
                     :broker-release-diagnostic-id
                     (:diagnostic-id release-outcome)))
            throwable)))))))

(defn- result-policy
  [result policy]
  (if (map? result)
    (assoc result
           :authority :non-authoritative
           :authoritative? false
           :timed-out? (boolean (or (:timed-out? result)
                                    (= :timeout (:status result))))
           :nondeterministic? (not (:deterministic? policy))
           :performance? (:performance? policy)
           :proof? (:proof? policy)
           :freshness-required? (:freshness-required? policy))
    result))

(defn run-unit!
  "Execute one miss through singleflight and one host-wide broker lease."
  [context job operation]
  (let [request (:development-cache-request job)
        outcome
        (cache/lookup-or-run!
         request
         #(result-policy
           (brokered-result
            context
            (:resource-class job)
            (fn []
              (let [result (operation)]
                (try
                  (verify-snapshot! context)
                  result
                  (catch clojure.lang.ExceptionInfo exception
                    (throw
                     (ex-info
                      (.getMessage exception)
                      (assoc (ex-data exception)
                             :child-jvm-launched?
                             (boolean (:child-jvm-launched? result)))
                      exception)))))))
           (:test-policy request)))
        receipt (:receipt outcome)]
    (record-cache-receipt! context :execution job receipt)
    (assoc (:result outcome)
           :cache-receipt receipt
           :cache-hit? (= :hit (:decision receipt))
           :child-jvm-launched?
           (boolean (and (:producer-executed? receipt)
                         (:child-jvm-launched? (:result outcome))))
           :broker-acquired-this-run? (:producer-executed? receipt))))

(defn report-facts
  "Return deterministic cache receipts plus the exact launch count."
  [context]
  {:launched-jvms @(:launch-count context)
   :cache-receipts
   (->> @(:cache-receipts context)
        (sort-by (juxt :namespaces :phase))
        vec)})
