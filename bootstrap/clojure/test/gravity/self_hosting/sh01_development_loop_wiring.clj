(ns gravity.self-hosting.sh01-development-loop-wiring
  "Thin parent-side wiring for incremental development test reuse.

  This namespace computes complete conservative requests before child launch,
  probes immutable hits before executor submission, and keeps the existing
  cache singleflight and host broker at the process execution boundary. It
  grants no test, proof, benchmark, integration, release, self-hosting, or
  seed-retirement authority."
  (:require [clojure.java.basis :as basis]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [gravity.digest :as digest]
            [gravity.self-hosting.sh01-component-test-dependencies
             :as component-dependencies]
            [gravity.self-hosting.sh01-development-test-cache :as cache]
            [gravity.self-hosting.sh01-host-resource-broker :as broker])
  (:import [java.io ByteArrayOutputStream]
           [java.lang.management ManagementFactory]
           [java.nio ByteBuffer]
           [java.nio.charset CharacterCodingException CodingErrorAction
            StandardCharsets]
           [java.nio.file Files LinkOption Path]
           [java.nio.file.attribute BasicFileAttributes FileAttribute
            PosixFilePermission PosixFilePermissions]
           [java.security MessageDigest]
           [java.util Arrays]
           [java.util.jar Attributes$Name JarFile]))

(def ^:private default-timeout-ms (* 60 60 1000))
(def ^:private child-runtime-token (Object.))
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

(def ^:private ^:dynamic *producer-git-lifecycle* nil)

(defn- start-git-process [command directory]
  (-> (ProcessBuilder. ^java.util.List command)
      (.directory directory)
      (.redirectErrorStream true)
      .start))

(defn- run-git-bytes!
  ([working-directory arguments]
   (run-git-bytes! working-directory arguments nil))
  ([working-directory arguments input-bytes]
   (let [directory (if (instance? Path working-directory)
                     (.toFile ^Path working-directory)
                     (io/file working-directory))
         command (into ["git"] arguments)
         lifecycle *producer-git-lifecycle*
         _ (when lifecycle ((:before! lifecycle) command directory))
         process (start-git-process command directory)
         owned (when lifecycle ((:retain! lifecycle) process command directory))
         primary (atom nil)]
     (try
       (let [writer-error (atom nil)
             writer
             (doto
              (Thread.
               ^Runnable
               (fn []
                 (try
                   (with-open [input (.getOutputStream process)]
                     (when input-bytes
                       (.write input ^bytes input-bytes)))
                   (catch Throwable exception
                     (reset! writer-error exception)))))
              (.setDaemon true)
              (.start))
             output (ByteArrayOutputStream.)]
         (with-open [input (.getInputStream process)]
           (.transferTo input output))
         (.join ^Thread writer)
         (let [exit-code (.waitFor process)
               _ (when lifecycle ((:waited! lifecycle) owned exit-code @writer-error))
               bytes (.toByteArray output)]
           (when-not (zero? exit-code)
             (fail! "SH01-DEVELOPMENT-LOOP-GIT"
                    "Development-loop repository identity command failed"
                    {:command command
                     :exit-code exit-code
                     :output (String. bytes StandardCharsets/UTF_8)}))
           (when @writer-error
             (fail! "SH01-DEVELOPMENT-LOOP-GIT"
                    "Development-loop repository identity input failed"
                    {:command command :exit-code exit-code}))
           bytes))
       (catch Throwable error (reset! primary error) (throw error))
       (finally
         (when lifecycle ((:finalize! lifecycle) owned @primary)))))))

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

(defn- strict-utf8
  [bytes]
  (try
    (str (.decode (doto (.newDecoder StandardCharsets/UTF_8)
                    (.onMalformedInput CodingErrorAction/REPORT)
                    (.onUnmappableCharacter CodingErrorAction/REPORT))
                  (ByteBuffer/wrap ^bytes bytes)))
    (catch CharacterCodingException _
      (fail! "SH01-DEVELOPMENT-LOOP-GIT"
             "Git returned a path that is not valid UTF-8"
             {}))))

(defn- repository-paths
  [working-directory]
  (let [raw (strict-utf8
             (run-git-bytes!
              working-directory
              ["ls-files" "-z" "--cached" "--others"
               "--exclude-standard"]))
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

(defn- nul-separated-paths
  [bytes]
  (->> (str/split (strict-utf8 bytes) #"\u0000" -1)
       (remove str/blank?)
       distinct
       sort
       vec))

(defn- enforce-nul-entry-limit!
  [bytes]
  (loop [index 0
         count 0]
    (if (= index (alength ^bytes bytes))
      count
      (let [next-count (if (zero? (aget ^bytes bytes index))
                         (inc count) count)]
        (when (> next-count maximum-path-count)
          (fail! "SH01-DEVELOPMENT-LOOP-SNAPSHOT-LIMIT"
                 "Repository snapshot path count exceeds its reviewed bound"
                 {:path-count next-count :maximum maximum-path-count}))
        (recur (inc index) next-count)))))

(defn- index-entry
  [entry]
  (let [tab (.indexOf ^String entry "\t")]
    (when (neg? tab)
      (fail! "SH01-DEVELOPMENT-LOOP-GIT"
             "Git index entry is malformed"
             {}))
    (let [[mode object-id stage :as header]
          (str/split (subs entry 0 tab) #" " -1)
          path (subs entry (inc tab))]
      (when-not (and (= 3 (count header))
                     (re-matches #"[0-7]{6}" (or mode ""))
                     (re-matches #"[0-9a-f]{40,64}" (or object-id ""))
                     (re-matches #"[0-3]" (or stage ""))
                     (not (str/blank? path)))
        (fail! "SH01-DEVELOPMENT-LOOP-GIT"
               "Git index entry is malformed"
               {}))
      {:mode mode :object-id object-id :stage stage :path path})))

(defn- index-entries
  [bytes]
  (mapv index-entry (nul-separated-paths bytes)))

(defn- nul-terminated-input
  [paths]
  (let [output (ByteArrayOutputStream.)]
    (doseq [path paths]
      (.write output (.getBytes ^String path StandardCharsets/UTF_8))
      (.write output 0))
    (.toByteArray output)))

(defn- status-entry
  [entry]
  (when-not (and (<= 4 (count entry))
                 (= \space (.charAt ^String entry 2)))
    (fail! "SH01-DEVELOPMENT-LOOP-GIT"
           "Git status entry is malformed"
           {}))
  (let [status (subs entry 0 2)
        path (subs entry 3)]
    (when (or (str/blank? path)
              (= "!!" status))
      (fail! "SH01-DEVELOPMENT-LOOP-GIT"
             "Git status entry is unsupported"
             {}))
    {:status status :path path :untracked? (= "??" status)}))

(defn- status-entries
  [bytes]
  (mapv status-entry (nul-separated-paths bytes)))

(defn- incremental-observation
  [working-directory]
  (let [index-bytes
        (run-git-bytes! working-directory ["ls-files" "--stage" "-z"])
        _ (enforce-nul-entry-limit! index-bytes)
        entries (index-entries index-bytes)
        flags-bytes
        (run-git-bytes! working-directory ["ls-files" "-v" "-z"])
        file-mode-bytes
        (run-git-bytes! working-directory ["config" "--bool" "core.filemode"])
        auto-crlf-bytes
        (run-git-bytes! working-directory
                        ["config" "--get" "--default" "false"
                         "core.autocrlf"])
        attributes-bytes
        (run-git-bytes! working-directory
                        ["check-attr" "-z" "--all" "--stdin"]
                        (nul-terminated-input (map :path entries)))
        status-bytes
        (run-git-bytes! working-directory
                        ["status" "--porcelain=v1" "-z"
                         "--untracked-files=all" "--no-renames"])]
    {:index-bytes index-bytes
     :flags-bytes flags-bytes
     :file-mode-bytes file-mode-bytes
     :auto-crlf-bytes auto-crlf-bytes
     :attributes-bytes attributes-bytes
     :status-bytes status-bytes
     :index-entries entries
     :status-entries (status-entries status-bytes)}))

(defn- incremental-observation-supported?
  [{:keys [index-entries flags-bytes file-mode-bytes auto-crlf-bytes
           attributes-bytes]}]
  (let [flags (nul-separated-paths flags-bytes)
        paths (mapv :path index-entries)]
    (and
     (= (count paths) (count (distinct paths)))
     (= "true" (str/trim (String. ^bytes file-mode-bytes
                                  StandardCharsets/UTF_8)))
     (= "false" (str/trim (String. ^bytes auto-crlf-bytes
                                   StandardCharsets/UTF_8)))
     (zero? (alength ^bytes attributes-bytes))
     (every? #(= "0" (:stage %)) index-entries)
     (every? #(contains? #{"100644" "100755"} (:mode %)) index-entries)
     (every? (fn [entry]
               (let [tag (first entry)]
                 (and tag
                      (not (Character/isLowerCase ^char tag))
                      (not= \S tag))))
             flags))))

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
      (do
        (update-framed-text! message-digest :absent)
        {:hashed-path-count 0 :hashed-byte-count 0})
      (let [before (Files/readAttributes path BasicFileAttributes no-links)
            before-executable? (and (.isRegularFile before)
                                    (Files/isExecutable path))]
        (cond
          (.isSymbolicLink before)
          (fail! "SH01-DEVELOPMENT-LOOP-SYMLINK"
                 "Repository symlinks are outside the complete cache closure"
                 {:path relative})

          (.isRegularFile before)
          (do
            (update-framed-text! message-digest :regular-file)
            (update-framed-text! message-digest
                                 (if before-executable? :executable
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
        (let [after (Files/readAttributes path BasicFileAttributes no-links)
              after-executable? (and (.isRegularFile after)
                                     (Files/isExecutable path))]
          (when-not (and (same-file-snapshot? before after)
                         (= before-executable? after-executable?))
            (fail! "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
                   "Repository input changed while its cache identity was read"
                   {:path relative})))
        {:hashed-path-count 1
         :hashed-byte-count (.size before)}))))

(defn- regular-file-identity
  [^Path input-path]
  (let [path (.toRealPath input-path (make-array LinkOption 0))
        before (Files/readAttributes path BasicFileAttributes no-links)
        before-executable? (Files/isExecutable path)]
    (when-not (.isRegularFile before)
      (fail! "SH01-DEVELOPMENT-LOOP-TOOL"
             "Development-loop external input must be a regular file"
             {}))
    (let [message-digest (MessageDigest/getInstance "SHA-256")]
      (update-framed-text! message-digest
                           :gravity/sh01-external-file-identity-v1)
      (update-framed-text! message-digest
                           (if before-executable? :executable
                               :non-executable))
      (with-open [input (Files/newInputStream
                         path (make-array java.nio.file.OpenOption 0))]
        (let [buffer (byte-array 65536)]
          (loop []
            (let [count (.read input buffer)]
              (when (pos? count)
                (.update message-digest buffer 0 count)
                (recur))))))
      (let [after (Files/readAttributes path BasicFileAttributes no-links)]
        (when-not (and (same-file-snapshot? before after)
                       (= before-executable? (Files/isExecutable path)))
          (fail! "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
                 "External runtime or classpath input changed while read"
                 {})))
      (message-digest-id message-digest))))

(defn- full-repository-snapshot
  "Return one conservative identity over all tracked and untracked inputs."
  [working-directory]
  (let [root (git-repository-root working-directory)
        paths (repository-paths root)
        message-digest (MessageDigest/getInstance "SHA-256")
        _ (update-framed-text! message-digest
                               :gravity/sh01-complete-repository-snapshot-v1)
        totals
        (reduce
         (fn [current relative]
           (merge-with + current (update-file! message-digest root relative)))
         {:hashed-path-count 0 :hashed-byte-count 0}
         paths)]
    (when-not (= paths (repository-paths root))
      (fail! "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
             "Repository path inventory changed while its identity was read"
             {}))
    {:schema :gravity/sh01-complete-repository-snapshot-v1
     :repository-identity (message-digest-id message-digest)
     :path-count (count paths)
     :snapshot-engine :full-content-v1
     :hashed-path-count (:hashed-path-count totals)
     :hashed-byte-count (:hashed-byte-count totals)}))

(defn- full-index-repository-snapshot
  [working-directory before]
  (let [full (full-repository-snapshot working-directory)
        after (incremental-observation working-directory)]
    (when-not (and (Arrays/equals ^bytes (:index-bytes before)
                                  ^bytes (:index-bytes after))
                   (Arrays/equals ^bytes (:flags-bytes before)
                                  ^bytes (:flags-bytes after))
                   (Arrays/equals ^bytes (:file-mode-bytes before)
                                  ^bytes (:file-mode-bytes after))
                   (Arrays/equals ^bytes (:auto-crlf-bytes before)
                                  ^bytes (:auto-crlf-bytes after))
                   (Arrays/equals ^bytes (:attributes-bytes before)
                                  ^bytes (:attributes-bytes after))
                   (Arrays/equals ^bytes (:status-bytes before)
                                  ^bytes (:status-bytes after)))
      (fail! "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
             "Repository index or inventory changed while its identity was read"
             {}))
    (let [message-digest (MessageDigest/getInstance "SHA-256")]
      (update-framed-text! message-digest
                           :gravity/sh01-git-index-full-content-snapshot-v2)
      (update-framed-text! message-digest (:repository-identity full))
      (update-framed-bytes! message-digest (:index-bytes before))
      (update-framed-bytes! message-digest (:flags-bytes before))
      (update-framed-bytes! message-digest (:file-mode-bytes before))
      (update-framed-bytes! message-digest (:auto-crlf-bytes before))
      (update-framed-bytes! message-digest (:attributes-bytes before))
      (update-framed-bytes! message-digest (:status-bytes before))
      (assoc full
             :schema :gravity/sh01-incremental-repository-snapshot-v2
             :repository-identity (message-digest-id message-digest)
             :snapshot-engine :git-index-full-content-v2))))

(defn- incremental-repository-snapshot
  [working-directory]
  (let [root (git-repository-root working-directory)
        before (incremental-observation root)]
    (if-not (incremental-observation-supported? before)
      (full-index-repository-snapshot root before)
      (let [index-paths (mapv :path (:index-entries before))
            overlay-paths (->> (:status-entries before)
                               (map :path) distinct sort vec)
            untracked-paths (->> (:status-entries before)
                                 (filter :untracked?) (map :path) vec)
            all-paths (->> (concat index-paths untracked-paths)
                           distinct sort vec)]
      (when (> (count all-paths) maximum-path-count)
        (fail! "SH01-DEVELOPMENT-LOOP-SNAPSHOT-LIMIT"
               "Repository snapshot path count exceeds its reviewed bound"
               {:path-count (count all-paths) :maximum maximum-path-count}))
      (let [message-digest (MessageDigest/getInstance "SHA-256")
            _ (update-framed-text!
               message-digest
               :gravity/sh01-git-index-overlay-snapshot-v2)
            _ (update-framed-bytes! message-digest (:index-bytes before))
            totals
            (reduce
             (fn [current relative]
               (merge-with + current (update-file! message-digest root relative)))
             {:hashed-path-count 0 :hashed-byte-count 0}
             overlay-paths)]
        (let [after (incremental-observation root)]
          (when-not (and (Arrays/equals ^bytes (:index-bytes before)
                                        ^bytes (:index-bytes after))
                         (Arrays/equals ^bytes (:flags-bytes before)
                                        ^bytes (:flags-bytes after))
                         (Arrays/equals ^bytes (:file-mode-bytes before)
                                        ^bytes (:file-mode-bytes after))
                         (Arrays/equals ^bytes (:auto-crlf-bytes before)
                                        ^bytes (:auto-crlf-bytes after))
                         (Arrays/equals ^bytes (:attributes-bytes before)
                                        ^bytes (:attributes-bytes after))
                         (Arrays/equals ^bytes (:status-bytes before)
                                        ^bytes (:status-bytes after)))
            (fail! "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
                   "Repository index or inventory changed while its identity was read"
                   {})))
        {:schema :gravity/sh01-incremental-repository-snapshot-v2
         :repository-identity (message-digest-id message-digest)
         :path-count (count all-paths)
         :tracked-path-count (count index-paths)
         :overlay-path-count (count overlay-paths)
         :snapshot-engine :git-index-overlay-v2
         :hashed-path-count (:hashed-path-count totals)
         :hashed-byte-count (:hashed-byte-count totals)})))))

(defn repository-snapshot
  "Return a path-neutral repository identity using the Git index plus only the
  dirty and untracked content overlay. Unsupported index states retain the
  complete content-hashing fallback."
  [working-directory]
  (incremental-repository-snapshot working-directory))

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
  (let [candidate (.toPath (io/file executable))
        resolved
        (cond
          (.isAbsolute candidate) candidate

          (str/includes? executable java.io.File/separator)
          (.resolve repository-root candidate)

          :else
          (some
           (fn [directory]
             (let [path (.resolve (.toPath (io/file directory)) executable)]
               (when (and (Files/isRegularFile path
                                               (make-array LinkOption 0))
                          (Files/isExecutable path))
                 path)))
           (str/split (or (System/getenv "PATH") "")
                      (re-pattern
                       (java.util.regex.Pattern/quote
                        (System/getProperty "path.separator"))))))]
    (when-not (and resolved
                   (Files/isRegularFile ^Path resolved
                                        (make-array LinkOption 0))
                   (Files/isExecutable ^Path resolved))
      (fail! "SH01-DEVELOPMENT-LOOP-TOOL"
             "Development-loop command executable could not be resolved"
             {:executable executable}))
    resolved))

(declare semantic-command)

(defn- jvm-input-arguments
  []
  (vec (.getInputArguments (ManagementFactory/getRuntimeMXBean))))

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
         :jvm-input-arguments (jvm-input-arguments)
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
        snapshot (repository-snapshot working-directory)
        snapshot-telemetry
        (atom {:full-snapshot-invocations 1
               :full-snapshot-path-observations (:path-count snapshot)
               :rehash-path-observations (:hashed-path-count snapshot)
               :rehash-byte-observations (:hashed-byte-count snapshot)
               :snapshot-engines {(:snapshot-engine snapshot) 1}
               :phases {:initial 1}})
        identity-options (select-keys options [:command :clojure-command])]
    (when-not (pos? timeout-ms)
      (fail! "SH01-DEVELOPMENT-LOOP-TIMEOUT"
             "Development-loop child timeout must be positive"
             {:timeout-ms timeout-ms}))
    {:schema :gravity/sh01-development-loop-context-v1
     :working-directory (str working-directory)
     :cache-directory cache-directory
     :broker-root broker-root
     :timeout-ms timeout-ms
     :revision
     (let [commit (str/trim (strict-utf8
                            (run-git-bytes! working-directory
                                            ["rev-parse" "--verify" "HEAD"])))]
       {:commit commit
        :tree (str/trim (strict-utf8
                        (run-git-bytes! working-directory
                                        ["rev-parse" "--verify" (str commit "^{tree}")])))} )
     :snapshot snapshot
     :snapshot-telemetry snapshot-telemetry
     :revalidate-snapshot? true
     :revalidate-external-inputs? true
     :identity-options identity-options
     :classpath-inputs
     (classpath-identities working-directory
                           (:repository-identity snapshot))
     :runtime-tool-inputs
     (base-runtime-identities working-directory identity-options)
     :launch-count (atom 0)
     :cache-receipts (atom [])}))

(defn validate-context!
  "Fail closed when an injected context does not describe this execution."
  [context working-directory options]
  (let [expected-working-directory
        (.normalize
         (.toAbsolutePath
          (.toPath (io/file working-directory))))
        context-working-directory
        (when-let [value (:working-directory context)]
          (.normalize
           (.toAbsolutePath
            (.toPath (io/file value)))))
        expected-identity-options
        (select-keys options [:command :clojure-command])
        custom-operation?
        (boolean (or (:process-launcher options)
                     (:worker options)
                     (:normal-batch-worker options)))]
    (when-not (and (= :gravity/sh01-development-loop-context-v1
                      (:schema context))
                   (= expected-working-directory context-working-directory)
                   (= expected-identity-options (:identity-options context))
                   (map? (:snapshot context))
                   (:cache-directory context)
                   (:broker-root context)
                   (:launch-count context)
                   (:cache-receipts context)
                   (or (not (:test-only? context)) custom-operation?)
                   (or (:revalidate-snapshot? context)
                       (and (:test-only? context) custom-operation?)))
      (fail! "SH01-DEVELOPMENT-LOOP-CONTEXT"
             "Injected development-loop context does not match this execution"
             {}))
    context))

(defn- policy-for-job
  [context plan job options timeout-ms]
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
     ;; The Clojure launcher can consume external deps, alias JVM options and
     ;; environment configuration not identified by the parent JVM snapshot.
     ;; A parent cannot identify the effective child configuration. Only the
     ;; child-local entrypoint below can supply this process-local token after
     ;; measuring its supported effective launch. A serialized marker cannot.
     (boolean (or (not (or (:test-only? context)
                           (identical? child-runtime-token
                                       (:child-runtime-token context))))
                  (:freshness-required? reviewed-policy)
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
        :jvm-input-arguments (jvm-input-arguments)
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

(defn- supported-resource-loaders?
  "Observe only the reviewed resource-delegation domain, not in-memory class identity."
  []
  (try
    (let [system (ClassLoader/getSystemClassLoader)
          delegates?
          (fn [loader]
            (loop [current loader seen #{}]
              (cond
                (identical? current system) true
                (not= clojure.lang.DynamicClassLoader (class current)) false
                (contains? seen current) false
                (seq (.getURLs ^clojure.lang.DynamicClassLoader current)) false
                :else (recur (.getParent ^ClassLoader current) (conj seen current)))))]
      (and (= "jdk.internal.loader.ClassLoaders$AppClassLoader" (.getName (class system)))
           (nil? (.getClassLoader (class system)))
           (identical? system (.getClassLoader clojure.lang.Compiler))
           (delegates? (.getContextClassLoader (Thread/currentThread)))
           (delegates? (clojure.lang.RT/baseLoader))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Exception _ false)))

(defn- archive-loader-inputs-contained?
  "Listed archive bytes cover manifest loading only when no Class-Path is declared.
  Unknown archives are outside the reviewed reuse domain. No referenced URL is opened."
  [^Path path]
  (try
    (and (Files/isRegularFile path no-links)
         (with-open [archive (JarFile. (.toFile path))]
           (let [manifest (.getManifest archive)]
             (or (nil? manifest)
                 (not (.containsKey (.getMainAttributes manifest)
                                    Attributes$Name/CLASS_PATH))))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Exception _ false)))

(defn- explicit-classpath-identities
  [^Path repository-root closure-root]
  (let [separator (java.util.regex.Pattern/quote
                   (System/getProperty "path.separator"))
        entries (str/split (System/getProperty "java.class.path")
                           (re-pattern separator) -1)
        paths
        (mapv
         (fn [entry]
           (let [candidate (.toPath (io/file entry))]
             (.normalize
              (if (.isAbsolute candidate)
                candidate
                (.resolve repository-root candidate)))))
         entries)
        internal-roots
        (->> paths
             (filter #(.startsWith ^Path % repository-root))
             (mapv #(str (.relativize repository-root ^Path %))))
        external-paths
        (remove #(.startsWith ^Path % repository-root) paths)]
    (when (and (supported-resource-loaders?)
               (= 2 (count internal-roots))
               (= #{"bootstrap/clojure/src" "bootstrap/clojure/test"}
                  (set internal-roots))
               (every? archive-loader-inputs-contained? external-paths))
      (mapv
       (fn [index ^Path path]
         {:path (format "classpath/entry-%04d" index)
          :sha256
          (if (.startsWith path repository-root)
            (sha256-id
             (pr-str
              (array-map
               :repository-identity closure-root
               :relative-entry (str (.relativize repository-root path)))))
            (regular-file-identity path))})
       (range)
       paths))))

(defn- reviewed-job-cache-closure
  [job]
  (try
    (let [index (component-dependencies/dependency-index)
          members (or (:batch-jobs job) [job])
          closures
          (mapv
           (fn [member]
             (let [reviewed
                   (component-dependencies/reviewed-cache-closure
                    index (:namespace member))]
               (when (and (:cache-closure-authorized? member)
                          (= reviewed (:cache-closure member)))
                 reviewed)))
           members)]
      (when (and (seq closures) (every? some? closures))
        {:schema component-dependencies/cache-closure-schema
         :inventories
         (->> closures
              (mapcat :inventories)
              distinct
              (sort-by (juxt (comp str :kind) :root :suffix))
              vec)
         :inputs
         (->> closures
              (mapcat :inputs)
              distinct
              (sort-by (juxt (comp str :role) :path))
              vec)}))
    (catch clojure.lang.ExceptionInfo _ nil)))

(defn- repository-input-identity
  [^Path repository-root {:keys [path role]}]
  (let [message-digest (MessageDigest/getInstance "SHA-256")]
    (update-framed-text!
     message-digest :gravity/sh01-explicit-cache-input-v1)
    (update-framed-text! message-digest role)
    (update-file! message-digest repository-root path)
    {:path path :role role :sha256 (message-digest-id message-digest)}))

(defn- closure-inventory
  [^Path repository-root {:keys [kind root suffix] :as declaration}]
  (let [inventory-root (.normalize (.resolve repository-root ^String root))]
    (when-not (and (.startsWith inventory-root repository-root)
                   (Files/isDirectory inventory-root no-links))
      (fail! "SH01-DEVELOPMENT-LOOP-PATH"
             "Explicit cache-closure inventory root is unavailable"
             {:root root :kind kind}))
    (with-open [paths (Files/walk
                       inventory-root
                       (make-array java.nio.file.FileVisitOption 0))]
      (let [matches
            (->> (.iterator paths)
                 iterator-seq
                 (keep
                  (fn [^Path path]
                    (when (Files/isSymbolicLink path)
                      (fail! "SH01-DEVELOPMENT-LOOP-SYMLINK"
                             "Repository symlinks are outside the explicit cache closure"
                             {:path (str (.relativize repository-root path))}))
                    (when (and (Files/isRegularFile path no-links)
                               (str/ends-with? (str path) suffix))
                      (str (.relativize repository-root path)))))
                 sort
                 vec)]
        (when (> (count matches) maximum-path-count)
          (fail! "SH01-DEVELOPMENT-LOOP-SNAPSHOT-LIMIT"
                 "Explicit cache-closure inventory exceeds its reviewed bound"
                 {:root root :path-count (count matches)}))
        (assoc declaration :paths matches)))))

(defn- declared-shadow-paths
  [inputs inventories]
  (let [declared-paths (set (map :path inputs))
        classpath-roots ["bootstrap/clojure/src" "bootstrap/clojure/test"]
        clojure-paths
        (->> inputs
             (map :path)
             (filter #(str/ends-with? % ".clj")))
        alternatives
        (mapcat
         (fn [path]
           (let [classpath-root
                 (some #(when (str/starts-with? path (str % "/")) %)
                       classpath-roots)
                 relative (when classpath-root
                            (subs path (inc (count classpath-root))))]
             (when relative
               (mapcat
                (fn [root]
                  (let [stem (subs relative 0 (- (count relative) 4))]
                    [(str root "/" relative)
                     (str root "/" stem ".cljc")
                     (str root "/" stem "__init.class")]))
                classpath-roots))))
         clojure-paths)
        data-readers
        ["bootstrap/clojure/src/data_readers.clj"
         "bootstrap/clojure/src/data_readers.cljc"
         "bootstrap/clojure/test/data_readers.clj"
         "bootstrap/clojure/test/data_readers.cljc"]
        class-paths
        (mapcat :paths
                (filter #(= :internal-class-path-set-v1 (:kind %))
                        inventories))]
    (->> (concat alternatives data-readers class-paths)
         (remove declared-paths)
         distinct
         sort
         vec)))

(defn- required-input-state
  [^Path repository-root relative]
  (let [path (.normalize (.resolve repository-root ^String relative))]
    (try
      (let [attributes
            (Files/readAttributes path BasicFileAttributes no-links)]
        {:attributes attributes
         :executable? (and (.isRegularFile attributes)
                           (Files/isExecutable path))})
      (catch java.nio.file.NoSuchFileException _ nil))))

(defn- same-required-input-state?
  [before after]
  (and before
       after
       (same-file-snapshot? (:attributes before) (:attributes after))
       (= (:executable? before) (:executable? after))))

(defn- explicit-closure-material
  [context job]
  (when-let [closure (reviewed-job-cache-closure job)]
    (let [repository-root
          (.normalize (.toAbsolutePath
                       (.toPath (io/file (:working-directory context)))))
          paths (mapv :path (:inputs closure))]
      (when (every? #(Files/exists (.resolve repository-root ^String %)
                                   no-links)
                    paths)
        (let [inventories
              (mapv #(closure-inventory repository-root %)
                    (:inventories closure))
              required-input-states
              (mapv #(required-input-state repository-root %) paths)
              _
              (when (some nil? required-input-states)
                (fail! "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
                       "Required explicit cache-closure input disappeared"
                       {}))
              inputs
              (mapv #(repository-input-identity repository-root %)
                    (:inputs closure))
              shadow-inputs
              (mapv #(repository-input-identity
                      repository-root {:path % :role :shadow})
                    (declared-shadow-paths inputs inventories))
              verified-inventories
              (mapv #(closure-inventory repository-root %)
                    (:inventories closure))
              verified-required-input-states
              (mapv #(required-input-state repository-root %) paths)
              _
              (when-not
               (and (= inventories verified-inventories)
                    (every? true?
                            (map same-required-input-state?
                                 required-input-states
                                 verified-required-input-states)))
                (fail! "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
                       "Explicit cache-closure inputs changed while read"
                       {}))
              closure-root
              (sha256-id
               (pr-str
                (array-map
                 :schema component-dependencies/cache-closure-schema
                 :dependency-contract-schema
                 component-dependencies/contract-schema
                 :inputs inputs
                 :shadow-inputs shadow-inputs
                 :inventories inventories)))
              classpath-inputs
              (explicit-classpath-identities repository-root closure-root)
              by-role (group-by :role inputs)
              identities
              (fn [& roles]
                (->> roles
                     (mapcat #(get by-role %))
                     (mapv #(select-keys % [:path :sha256]))))]
          (when classpath-inputs
            {:repository-identity closure-root
             :dependencies
             {:complete? true
              :production-inputs (identities :production)
              :transitive-production-inputs (identities :transitive)
              :fixture-contract-inputs
              (identities :test :fixture :contract :classpath)
              :runner-identity
              {:id :sh01-development-loop-runner
               :sha256
               (sha256-id
                (pr-str
                 (array-map
                  :schema :gravity/sh01-explicit-runner-identity-v1
                  :inputs (vec (get by-role :runner)))))}
              :classpath-inputs classpath-inputs
              :runtime-tool-inputs
              (:runtime-tool-inputs context)}}))))))

(defn cache-request
  "Build the closed cache request for one scheduled execution unit."
  [context plan job options]
  (let [full-repository-identity
        (get-in context [:snapshot :repository-identity])
        timeout-ms (:timeout-ms context)
        policy (policy-for-job context plan job options timeout-ms)
        explicit-material
        (when (and (= :non-authoritative (:authority policy))
                   (:deterministic? policy)
                   (not (:performance? policy))
                   (not (:proof? policy))
                   (not (:freshness-required? policy)))
          (explicit-closure-material context job))
        repository-identity
        (or (:repository-identity explicit-material)
            full-repository-identity)
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
     (or
      (some-> (:dependencies explicit-material)
              (update :runtime-tool-inputs
                      #(vec
                        (concat (or % (runtime-identities {}))
                                (when (:development-operation-identity options)
                                  [(:development-operation-identity options)])))))
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
                      [(:development-operation-identity options)])))})}))

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

(defn- record-snapshot!
  [context phase snapshot]
  (when-let [telemetry (:snapshot-telemetry context)]
    (swap! telemetry
           (fn [current]
             (cond->
              (-> current
                  (update :full-snapshot-invocations (fnil inc 0))
                  (update :full-snapshot-path-observations
                          (fnil + 0) (:path-count snapshot))
                  (update-in [:phases phase] (fnil inc 0)))
               (number? (:hashed-path-count snapshot))
               (update :rehash-path-observations
                       (fnil + 0) (:hashed-path-count snapshot))

               (number? (:hashed-byte-count snapshot))
               (update :rehash-byte-observations
                       (fnil + 0) (:hashed-byte-count snapshot))

               (:snapshot-engine snapshot)
               (update-in [:snapshot-engines (:snapshot-engine snapshot)]
                          (fnil inc 0))))))
  snapshot)

(defn- verify-snapshot!
  ([context]
   (verify-snapshot! context :verification))
  ([context phase]
   (when (:revalidate-snapshot? context)
     (let [working-directory (:working-directory context)
           current-snapshot
           (record-snapshot! context phase
                             (repository-snapshot working-directory))]
       (when-not (= (:snapshot context) current-snapshot)
         (fail! "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
                "Repository inputs changed after parent cache-key computation"
                {:input-kind :repository}))
       (when (:revalidate-external-inputs? context)
         (let [repository-root
               (.normalize (.toAbsolutePath
                            (.toPath (io/file working-directory))))
               current-classpath
               (classpath-identities
                repository-root (:repository-identity current-snapshot))
               current-runtime
               (base-runtime-identities repository-root
                                        (:identity-options context))]
           (when-not (= (:classpath-inputs context) current-classpath)
             (fail! "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
                    "External classpath inputs changed after cache-key computation"
                    {:input-kind :classpath}))
           (when-not (= (:runtime-tool-inputs context) current-runtime)
             (fail! "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
                    "Runtime or command inputs changed after cache-key computation"
                    {:input-kind :runtime-tool}))))))
   nil))

(defn- explicit-request?
  [context job]
  (let [request (:development-cache-request job)
        policy (:test-policy request)]
    (and (not= (:repository-identity request)
               (get-in context [:snapshot :repository-identity]))
         (= :non-authoritative (:authority policy))
         (:deterministic? policy)
         (not-any? policy [:performance? :proof? :freshness-required?])
         (reviewed-job-cache-closure job))))

(defn- verify-inputs!
  "Revalidate consumed closures; incomplete or fresh work keeps the tree guard.

  A matching cache key is never sufficient: reread every reviewed input,
  inventory, shadow path and classpath entry before admitting a hit or a
  producer result. Runtime executables and environment identity remain fresh."
  [context phase jobs]
  (when (:revalidate-snapshot? context)
    (if (and (seq jobs) (every? #(explicit-request? context %) jobs))
      (do
        (doseq [job jobs]
          (let [expected (:development-cache-request job)
                actual (explicit-closure-material context job)]
            (when-not
             (and actual
                  (= (:repository-identity expected)
                     (:repository-identity actual))
                  (= (dissoc (:dependencies expected) :runtime-tool-inputs)
                     (dissoc (:dependencies actual) :runtime-tool-inputs)))
              (fail! "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
                     "Consumed development inputs changed after request computation"
                     {:input-kind :explicit-closure :phase phase}))))
        (when (:revalidate-external-inputs? context)
          (let [root (.normalize (.toAbsolutePath
                                 (.toPath (io/file (:working-directory context)))))
                current (base-runtime-identities root (:identity-options context))]
            (when-not (= (:runtime-tool-inputs context) current)
              (fail! "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
                     "Runtime or command inputs changed after request computation"
                     {:input-kind :runtime-tool :phase phase})))))
      (verify-snapshot! context phase))))

(defn- probe*
  [context job verify-hit?]
  (let [outcome (cache/lookup! (:development-cache-request job))
        receipt (:receipt outcome)]
    (record-cache-receipt! context :parent-probe job receipt)
    (if (= :hit (:decision receipt))
      (do
        (when verify-hit?
          (verify-inputs! context :standalone-parent-hit [job]))
        {:job job
         :hit-result
         (assoc (:result outcome)
                :cache-receipt receipt
                :cache-hit? true
                :child-jvm-launched? false
                :broker-acquired-this-run? false)})
      {:job job :hit-result nil})))

(defn probe
  "Probe in the parent and return the unchanged job or its immutable hit."
  [context job]
  (probe* context job true))

(defn probe-batch
  "Probe all scheduled units, then validate once before admitting any result."
  [context jobs]
  (let [outcomes (mapv #(probe* context % false) jobs)]
    (when (some :hit-result outcomes)
      (verify-inputs! context :parent-probe-batch
                      (mapv :job (filter :hit-result outcomes))))
    outcomes))

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
                  (verify-inputs! context :producer-post-operation [job])
                  (cond-> result
                    (and (map? result) (:revision context))
                    (assoc :execution-provenance
                           {:revision (:revision context)
                            :repository-snapshot (:snapshot context)
                            :consumed-input-identity (:repository-identity request)}))
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
    (when (= :hit (:decision receipt))
      (verify-inputs! context :secondary-singleflight-hit [job]))
    (record-cache-receipt! context :execution job receipt)
    (assoc (:result outcome)
           :cache-receipt receipt
           :cache-hit? (= :hit (:decision receipt))
           :child-jvm-launched?
           (boolean (and (:producer-executed? receipt)
                         (:child-jvm-launched? (:result outcome))))
           :broker-acquired-this-run? (:producer-executed? receipt))))

(defn- supported-child-jvm-argument?
  [argument]
  ;; File-loading and agent options are deliberately unknown. Hashing their
  ;; spelling would not identify the bytes they load outside the classpath.
  (or (= "-XX:-OmitStackTraceInFastThrow" argument)
      (boolean (re-matches #"-Xm[sx][0-9]+[kKmMgG]?" argument))
      (boolean (re-matches #"-Xss[0-9]+[kKmMgG]?" argument))
      (= argument (str "-Dclojure.basis="
                       (System/getProperty "clojure.basis")))))

(defn- child-config-source
  [root value]
  (cond
    (or (nil? value) (map? value)) value
    (string? value)
    (let [candidate (.toPath (io/file value))
          path (.normalize (if (.isAbsolute candidate)
                             candidate (.resolve ^Path root candidate)))]
      {:path (str path)
       :sha256 (if (Files/exists path no-links)
                 (regular-file-identity path)
                 :absent)})
    :else (throw (ex-info "Unknown child configuration source" {}))))

(defn- child-semantic-arguments
  [arguments]
  ;; Only the runner-owned output destination is operational rather than a
  ;; test input. Its spelling changes for every parent-created batch report.
  (loop [remaining (seq arguments) result []]
    (if-let [argument (first remaining)]
      (if (contains? #{"--report-file" "--development-cache"} argument)
        (recur (nnext remaining) result)
        (recur (next remaining) (conj result argument)))
      result)))

(defn project-startup-resources-absent?
  "True only when known project-classpath startup override locations are absent.
  This is a current filesystem observation, not proof of prior nonexecution."
  [working-directory]
  (try
    (let [root (.normalize
                (.toAbsolutePath
                 (if (instance? Path working-directory) working-directory
                     (.toPath (io/file working-directory)))))]
      (every? #(Files/notExists (.resolve root ^String %) no-links)
              ["bootstrap/clojure/src/user.clj"
               "bootstrap/clojure/test/user.clj"
               "bootstrap/clojure/src/clojure"
               "bootstrap/clojure/test/clojure"]))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Exception _ false)))

(defn- child-startup-resources-absent? [working-directory]
  (try
    (and (project-startup-resources-absent? working-directory)
         (nil? (io/resource "user.clj")))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Exception _ false)))

(defn- effective-child-runtime
  [working-directory arguments]
  (try
    (when (supported-resource-loaders?)
      (let [current (basis/current-basis)
          config (:basis-config current)
          root (.normalize (.toAbsolutePath (.toPath (io/file working-directory))))
          jvm-arguments (jvm-input-arguments)
          expected-command (str/join " "
                                     (into ["clojure.main" "-m"
                                            "gravity.self-hosting-test-runner"]
                                           arguments))
          properties (into (sorted-map) (System/getProperties))
          environment (into (sorted-map) (System/getenv))
          classpath (str/split (System/getProperty "java.class.path")
                              (re-pattern (java.util.regex.Pattern/quote
                                           (System/getProperty "path.separator"))))
          classpath-paths (mapv #(.normalize (.toAbsolutePath (.toPath (io/file %))))
                               classpath)
          internal (filterv #(.startsWith ^Path % root) classpath-paths)
          internal-relative (mapv #(str (.relativize root ^Path %)) internal)]
      (when (and (map? current)
                 (child-startup-resources-absent? working-directory)
                 (= current (basis/initial-basis))
                 (map? config)
                 (every? #{:root :user :project :extra :args :aliases} (keys config))
                 (= ["-m" "gravity.self-hosting-test-runner"]
                    (get-in current [:argmap :main-opts]))
                 (= expected-command (get properties "sun.java.command"))
                 (string? (get properties "clojure.basis"))
                 (every? supported-child-jvm-argument? jvm-arguments)
                 (not-any? #(seq (get environment %))
                           ["LD_PRELOAD" "LD_AUDIT" "LD_LIBRARY_PATH"
                            "DYLD_INSERT_LIBRARIES" "DYLD_LIBRARY_PATH"
                            "DYLD_FRAMEWORK_PATH" "GRAVITY_FRESH_PROGRESS_FILE"])
                 (= 2 (count internal-relative))
                 (= #{"bootstrap/clojure/src" "bootstrap/clojure/test"}
                    (set internal-relative))
                 (every? #(or (.startsWith ^Path % root)
                              (archive-loader-inputs-contained? %))
                         classpath-paths))
        (let [material
              {:basis current
            :basis-file (child-config-source root (get properties "clojure.basis"))
            :configuration-sources
            (into (sorted-map)
                  (for [key [:root :user :project :extra]]
                    [key (child-config-source root (get config key))]))
            :jvm-input-arguments jvm-arguments
            :environment environment
            :system-properties
            (assoc properties "sun.java.command"
                   (pr-str (child-semantic-arguments arguments)))}
              fingerprints (into (sorted-map)
                                 (map (fn [[key value]] [key (sha256-id (pr-str value))]))
                                 material)]
          {:id :actual-child-runtime-v1
           :sha256 (sha256-id (pr-str fingerprints))
           :fingerprints fingerprints}))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Exception _
      ;; Unknown launch inputs disable the optional optimization, not the test.
      nil)))

(def ^:private unreviewed-bootstrap-effect-selections
  ;; Full repository identity binds source bytes, not initialization effects.
  ;; These two inventoried suites still lack that execution-effect closure.
  '#{gravity.bootstrap-compatibility.c11-test
     gravity.self-hosting.sh01-compiler-pass-manifest-compatibility-test})

(defn- observe-fresh-producer-binding!
  [working-directory arguments options]
  (let [namespace 'gravity.c2-artifact-identity-test
        index (component-dependencies/dependency-index)
        entry (some #(when (= namespace (:namespace %)) %)
                    (vals (:by-test-path index)))
        closure (component-dependencies/reviewed-cache-closure index namespace)
        runtime (effective-child-runtime working-directory arguments)]
    (when-not (and closure (true? (get-in entry [:test-policy :freshness-required?]))
                   runtime)
      (fail! "SH01-PRODUCER-OBSERVATION"
             "Fresh producer observation requires the declared fresh selection and actual child runtime"
             {}))
    (let [context (prepare-context working-directory options)
          _ (when-not (= (:repository-identity options)
                         (get-in context [:snapshot :repository-identity]))
              (fail! "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
                     "Fresh observer inputs differ from the prelaunch repository snapshot"
                     {:input-kind :child-startup}))
          root (.toPath (io/file working-directory))
          paths (mapv :path (:inputs closure))
          states (mapv #(required-input-state root %) paths)
          _ (when-not (every? #(and % (.isRegularFile ^BasicFileAttributes (:attributes %))) states)
              (fail! "SH01-PRODUCER-OBSERVATION"
                     "Fresh producer requires every declared input to be a regular file" {}))
          inputs (mapv #(repository-input-identity root %) (:inputs closure))
          inventories (mapv #(closure-inventory root %) (:inventories closure))
          shadows (mapv #(repository-input-identity root {:path % :role :shadow})
                        (declared-shadow-paths inputs inventories))
          verified-inventories (mapv #(closure-inventory root %) (:inventories closure))
          verified-states (mapv #(required-input-state root %) paths)
          _ (when-not (and (= inventories verified-inventories)
                           (every? true? (map same-required-input-state? states verified-states)))
              (fail! "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
                     "Fresh producer inputs changed while observed" {:input-kind :producer-observation}))
          consumed (sha256-id
                    (pr-str (array-map
                             :schema (:schema closure)
                             :dependency-contract-schema component-dependencies/contract-schema
                             :inputs inputs :shadow-inputs shadows :inventories inventories)))
          classpath (explicit-classpath-identities root consumed)
          clean? (str/blank? (strict-utf8 (run-git-bytes! root ["status" "--porcelain"])))
          binding {:source (assoc (:revision context)
                                  :repository-identity (get-in context [:snapshot :repository-identity])
                                  :clean? clean?)
                   :consumed-input-identity consumed :inputs inputs :inventories inventories
                   :shadow-inputs shadows :classpath-identities classpath
                   :child-runtime-identity runtime
                   :runtime-tool-inputs
                   (cond-> (:runtime-tool-inputs context)
                     (:producer-git-binding options)
                     (conj {:id :selected-observer-git-binding
                            :sha256 (sha256-id (pr-str (:producer-git-binding options)))}))}]
      (when-not (and clean? classpath)
        (fail! "SH01-PRODUCER-OBSERVATION"
               "Fresh producer source or classpath cannot be observed completely" {}))
      {:binding binding :repository-snapshot (:snapshot context)})))

(defn- observe-fresh-producer!
  [namespaces {:keys [arguments] :as options} operation]
  (when-not (= ['gravity.c2-artifact-identity-test] namespaces)
    (fail! "SH01-PRODUCER-OBSERVATION"
           "Producer observation requires exactly the declared Reader identity selection" {}))
  (let [directory (System/getProperty "user.dir")
        observe #(binding [*producer-git-lifecycle* (:producer-git-lifecycle options)]
                   (observe-fresh-producer-binding! directory arguments options))
        before (observe)
        result (operation)
        after (observe)]
    (when-not (= before after)
      (fail! "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
             "Fresh producer observations changed during execution" {:input-kind :producer-observation}))
    (let [binding (:binding before)]
      (assoc result :test-producer-observation
             {:schema :gravity/sh01-fresh-producer-observation-v1
              :binding-before binding :binding-after (:binding after)
              :producer-provenance
              {:revision (select-keys (:source binding) [:commit :tree])
               :repository-snapshot (:repository-snapshot before)
               :consumed-input-identity (:consumed-input-identity binding)
               :classpath-identities (:classpath-identities binding)
               :child-runtime-identity (:child-runtime-identity binding)}
              :applying-source (:source binding)}))))

(defn run-child-unit!
  "Optionally reuse a complete selection inside the actual existing test JVM.

  The parent still launches and holds its broker lease. This entrypoint does
  not inspect processes or acquire a nested lease. Unknown launches and test
  policies execute fresh; no parent assertion can supply a child identity."
  [namespaces {:keys [arguments fail-fast?] :as options} operation]
  (if (:observe-producer? options)
    (observe-fresh-producer!
     namespaces options
     #(run-child-unit! namespaces (dissoc options :observe-producer?) operation))
    (let [effects-reviewed? (not-any? unreviewed-bootstrap-effect-selections namespaces)
        resource-loaders-supported? (supported-resource-loaders?)
        index (component-dependencies/dependency-index)
        by-namespace (into {} (map (juxt :namespace identity)
                                  (vals (:by-test-path index))))
        jobs (mapv (fn [namespace]
                     (let [entry (get by-namespace namespace)
                           closure (component-dependencies/reviewed-cache-closure
                                    index namespace)]
                       (assoc entry :namespace namespace
                                    :cache-closure closure
                                    ;; Only the re-audited bootstrap-free leaves
                                    ;; have explicit closures. Bootstrap-dependent
                                    ;; selections retain complete-tree identity.
                                    :cache-closure-authorized?
                                    (and (some? closure)
                                         (contains? '#{gravity.c2-artifact-identity-test
                                                       gravity.c11-mir-test
                                                       gravity.compiler-pass-manifest-test}
                                                    namespace)))))
                   namespaces)
        reviewed? (every? (fn [job]
                            (let [policy (:test-policy job)]
                              (and (:deterministic? policy)
                                   (not-any? policy [:performance? :proof?
                                                     :freshness-required?]))))
                          jobs)
        working-directory (System/getProperty "user.dir")
        startup-absent? (and effects-reviewed? resource-loaders-supported? reviewed?
                             (child-startup-resources-absent? working-directory))
        runtime (when (and effects-reviewed? resource-loaders-supported? reviewed? startup-absent?)
                  (effective-child-runtime working-directory arguments))]
    (if-not runtime
      (assoc (operation)
             :child-jvm-launched? true
             :tests-executed? true
             :child-cache-hit? false
             :child-cache-receipt
             {:decision :fresh :cacheable? false :stored? false
              :producer-executed? true
              :reason (cond
                        (not effects-reviewed?) :unreviewed-bootstrap-initialization-effects
                        (not resource-loaders-supported?) :unreviewed-resource-loaders
                        (not reviewed?) :unreviewed-or-fresh-test-policy
                        (not startup-absent?) :unreviewed-startup-resources
                        :else :unknown-child-configuration)})
      (let [context (assoc (prepare-context working-directory options)
                           :child-runtime-token child-runtime-token)
            _ (when-not (= (:repository-identity options)
                           (get-in context [:snapshot :repository-identity]))
                (fail! "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
                       "Child inputs differ from the prelaunch repository snapshot"
                       {:input-kind :child-startup}))
            job {:batch-jobs jobs :resource-class :normal}
            request (update-in
                     (cache-request context {:authority :non-authoritative}
                                    job {:fail-fast? fail-fast?})
                     [:dependencies :runtime-tool-inputs] conj
                     (select-keys runtime [:id :sha256]))
            job (assoc job :development-cache-request request)
            verify! (fn [phase]
                      (verify-inputs! context phase [job])
                      (when-not (= runtime (effective-child-runtime working-directory arguments))
                        (fail! "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
                               "Effective child configuration changed during reuse"
                               {:input-kind :child-runtime :phase phase})))
            outcome
            (cache/lookup-or-run!
             request
             (fn []
               (verify! :child-before-execution)
               (let [result (operation)]
                 (verify! :child-after-execution)
                 (result-policy
                  (assoc result :test-producer-provenance
                         {:revision (:revision context)
                          :repository-snapshot (:snapshot context)
                          :consumed-input-identity (:repository-identity request)
                          :classpath-identities
                          (get-in request [:dependencies :classpath-inputs])
                          :child-runtime-identity runtime})
                  (:test-policy request)))))
            receipt (:receipt outcome)]
        (when (= :hit (:decision receipt))
          (verify! :child-cache-hit))
        (assoc (:result outcome)
               :test-applying-revision (:revision context)
               :test-applying-repository-snapshot (:snapshot context)
               :child-jvm-launched? true
               :tests-executed? (:producer-executed? receipt)
               :child-cache-hit? (= :hit (:decision receipt))
               :child-cache-receipt receipt))))))

(defn report-facts
  "Return deterministic cache receipts plus the exact launch count."
  [context]
  {:launched-jvms @(:launch-count context)
   :revision (:revision context)
   :snapshot-telemetry
   (when-let [telemetry (:snapshot-telemetry context)] @telemetry)
   :cache-receipts
   (->> @(:cache-receipts context)
        (sort-by (juxt :namespaces :phase))
        vec)})
