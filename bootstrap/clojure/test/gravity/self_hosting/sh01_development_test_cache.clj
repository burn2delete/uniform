(ns gravity.self-hosting.sh01-development-test-cache
  "Concurrent persistent reuse for non-authoritative development test results.

  The cache is a Stage 0 development leaf. Callers supply a complete
  dependency closure and a shared cache directory. Eligible results are stored
  as immutable, content-addressed per-key EDN files. A persistent per-key file
  lock provides cooperative cross-JVM singleflight without serializing
  unrelated keys.

  This namespace never reads or rewrites the historical results-v1.edn file and
  grants no test, benchmark, proof, release, self-hosting, or seed-retirement
  authority. The cache directory and callers are a cooperative, trusted
  same-user boundary: this leaf does not defend against concurrent hostile
  path replacement, evict persistent key files, or impose a timeout on the
  supplied producer. Callers must bound producer execution separately."
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [gravity.digest :as digest])
  (:import [java.io PushbackReader StringReader]
           [java.nio ByteBuffer]
           [java.nio.channels FileChannel]
           [java.nio.charset CodingErrorAction StandardCharsets]
           [java.nio.file AtomicMoveNotSupportedException Files LinkOption
            Path Paths StandardCopyOption StandardOpenOption]
           [java.nio.file.attribute BasicFileAttributes FileAttribute]
           [java.util HashSet UUID]
           [java.util.concurrent.locks ReentrantLock]))

(def ^:private key-schema :gravity/development-test-cache-key-v2)
(def ^:private entry-schema :gravity/development-test-cache-entry-v2)
(def ^:private receipt-schema :gravity/development-test-cache-receipt-v2)
(def ^:private storage-directory-name "v2")
(def ^:private maximum-input-count 4096)
(def ^:private maximum-total-identity-bytes (* 2 1024 1024))
(def ^:private maximum-relative-path-characters 4096)
(def ^:private maximum-logical-id-characters 512)
(def ^:private maximum-canonical-depth 64)
(def ^:private maximum-canonical-nodes 65536)
(def ^:private maximum-collection-count 4096)
(def ^:private maximum-key-material-bytes (* 4 1024 1024))
(def ^:private maximum-result-bytes (* 1024 1024))
(def ^:private maximum-entry-bytes (* 6 1024 1024))
(def ^:private maximum-receipt-bytes (* 64 1024))
(def ^:private sha256-pattern #"sha256:[0-9a-f]{64}")
(def ^:private drive-path-pattern #"(?i)^[a-z]:.*")
(def ^:private no-links (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
(def ^:private no-file-attributes (make-array FileAttribute 0))
(def ^:private create-new-write-options
  (HashSet. [StandardOpenOption/CREATE_NEW
             StandardOpenOption/WRITE
             LinkOption/NOFOLLOW_LINKS]))
(def ^:private lock-open-options
  (HashSet. [StandardOpenOption/CREATE
             StandardOpenOption/WRITE
             LinkOption/NOFOLLOW_LINKS]))
(def ^:private in-process-key-locks (atom {}))

(def ^:private request-fields
  #{:cache-directory :repository-identity :test-identity
    :test-policy :dependencies})

(def ^:private dependency-fields
  #{:complete? :production-inputs :transitive-production-inputs
    :fixture-contract-inputs :runner-identity :classpath-inputs
    :runtime-tool-inputs})

(def ^:private policy-fields
  #{:authority :deterministic? :performance? :proof?
    :freshness-required? :timeout-ms})

(defn- fail!
  [id message data]
  (throw
   (ex-info message
            (merge {:id id
                    :release-authority? false
                    :proof-authority? false
                    :self-hosting-authority? false}
                   data))))

(defn- fatal-throwable?
  [error]
  (or (instance? ThreadDeath error)
      (and (instance? VirtualMachineError error)
           (not (instance? StackOverflowError error)))
      (instance? InterruptedException error)))

(defn- utf8-bytes
  [^String value]
  (.getBytes value StandardCharsets/UTF_8))

(defn- sha256
  [^String value]
  (str "sha256:" (digest/sha256-hex value)))

(defn- sha256-id?
  [value]
  (and (string? value)
       (boolean (re-matches sha256-pattern value))))

(defn- scalar-value?
  [value]
  (or (nil? value)
      (boolean? value)
      (string? value)
      (keyword? value)
      (symbol? value)
      (integer? value)))

(defn- scalar-text
  [value]
  (when-not (scalar-value? value)
    (fail! "DEV-TEST-CACHE-IDENTITY"
           "Canonical EDN map keys and set members must be scalar"
           {:value-class (some-> value class .getName)}))
  (pr-str value))

(defn- canonical-text
  [value maximum-bytes diagnostic-id]
  (let [nodes (volatile! 0)
        builder (StringBuilder.)]
    (letfn [(reject! [reason item]
              (fail! diagnostic-id
                     "Value is outside the bounded canonical EDN subset"
                     {:reason reason
                      :value-class (some-> item class .getName)}))
            (append! [text]
              (.append builder ^String text)
              (when (> (.length builder) maximum-bytes)
                (reject! :canonical-size-bound value)))
            (walk! [item depth]
              (vswap! nodes inc)
              (when (> @nodes maximum-canonical-nodes)
                (reject! :canonical-node-bound item))
              (when (> depth maximum-canonical-depth)
                (reject! :canonical-depth-bound item))
              (when (meta item)
                (reject! :metadata-not-canonical item))
              (cond
                (scalar-value? item)
                (append! (scalar-text item))

                (map? item)
                (do
                  (when (> (count item) maximum-collection-count)
                    (reject! :collection-count-bound item))
                  (append! "{")
                  (doseq [[index [key entry]]
                          (map-indexed
                           vector
                           (sort-by (comp scalar-text key) item))]
                    (when (pos? index) (append! " "))
                    (walk! key (inc depth))
                    (append! " ")
                    (walk! entry (inc depth)))
                  (append! "}"))

                (vector? item)
                (do
                  (when (> (count item) maximum-collection-count)
                    (reject! :collection-count-bound item))
                  (append! "[")
                  (doseq [[index entry] (map-indexed vector item)]
                    (when (pos? index) (append! " "))
                    (walk! entry (inc depth)))
                  (append! "]"))

                (list? item)
                (do
                  (when (> (count item) maximum-collection-count)
                    (reject! :collection-count-bound item))
                  (append! "(")
                  (doseq [[index entry] (map-indexed vector item)]
                    (when (pos? index) (append! " "))
                    (walk! entry (inc depth)))
                  (append! ")"))

                (set? item)
                (do
                  (when (> (count item) maximum-collection-count)
                    (reject! :collection-count-bound item))
                  (append! "#{")
                  (doseq [[index entry]
                          (map-indexed vector (sort-by scalar-text item))]
                    (when (pos? index) (append! " "))
                    (walk! entry (inc depth)))
                  (append! "}"))

                :else
                (reject! :unsupported-value item)))]
      (walk! value 0)
      (let [text (.toString builder)]
        (when (> (alength (utf8-bytes text)) maximum-bytes)
          (fail! diagnostic-id
                 "Canonical EDN exceeds its UTF-8 byte bound"
                 {:reason :canonical-size-bound
                  :maximum-bytes maximum-bytes}))
        text))))

(defn- logical-id?
  [value]
  (let [text (when (or (string? value) (keyword? value) (symbol? value))
               (str value))]
    (and text
         (nil? (meta value))
         (<= 1 (count text) maximum-logical-id-characters)
         (not (str/blank? text))
         (not (str/starts-with? text "/"))
         (not (str/starts-with? text "\\"))
         (not (re-matches drive-path-pattern text))
         (not-any? #(or (zero? (int %)) (Character/isISOControl ^char %)) text))))

(defn- normalized-relative-path?
  [value]
  (and (string? value)
       (<= 1 (count value) maximum-relative-path-characters)
       (not (str/starts-with? value "/"))
       (not (str/includes? value "\\"))
       (not (re-matches drive-path-pattern value))
       (not-any? #(or (zero? (int %)) (Character/isISOControl ^char %)) value)
       (let [segments (str/split value #"/" -1)]
         (and (every? #(and (not (str/blank? %))
                            (not= "." %)
                            (not= ".." %))
                      segments)
              (= value (str/join "/" segments))))))

(defn- named-identity?
  [value]
  (and (map? value)
       (nil? (meta value))
       (= #{:id :sha256} (set (keys value)))
       (logical-id? (:id value))
       (sha256-id? (:sha256 value))))

(defn- relative-input-identity?
  [value]
  (and (map? value)
       (nil? (meta value))
       (= #{:path :sha256} (set (keys value)))
       (normalized-relative-path? (:path value))
       (sha256-id? (:sha256 value))))

(defn- identity-vector?
  [value identity-predicate identity-field]
  (and (vector? value)
       (nil? (meta value))
       (<= (count value) maximum-input-count)
       (every? identity-predicate value)
       (= (count value)
          (count (distinct (map identity-field value))))))

(defn- complete-dependencies?
  [dependencies]
  (and (map? dependencies)
       (nil? (meta dependencies))
       (= dependency-fields (set (keys dependencies)))
       (true? (:complete? dependencies))
       (identity-vector? (:production-inputs dependencies)
                         relative-input-identity? :path)
       (identity-vector? (:transitive-production-inputs dependencies)
                         relative-input-identity? :path)
       (identity-vector? (:fixture-contract-inputs dependencies)
                         relative-input-identity? :path)
       (named-identity? (:runner-identity dependencies))
       (identity-vector? (:classpath-inputs dependencies)
                         relative-input-identity? :path)
       (identity-vector? (:runtime-tool-inputs dependencies)
                         named-identity? :id)
       (let [file-identities
             (mapcat dependencies
                     [:production-inputs :transitive-production-inputs
                      :fixture-contract-inputs :classpath-inputs])
             named-identities
             (cons (:runner-identity dependencies)
                   (:runtime-tool-inputs dependencies))
             all-identities (concat file-identities named-identities)
             identity-bytes
             (reduce
              +
              (map (fn [identity]
                     (+ (alength
                         (utf8-bytes
                          (str (or (:path identity) (:id identity)))))
                        (alength (utf8-bytes (:sha256 identity)))))
                   all-identities))]
         (and (<= (count all-identities) maximum-input-count)
              (<= identity-bytes maximum-total-identity-bytes)))))

(defn- complete-policy?
  [policy]
  (and (map? policy)
       (nil? (meta policy))
       (= policy-fields (set (keys policy)))
       (contains? #{:non-authoritative :authoritative} (:authority policy))
       (every? boolean?
               ((juxt :deterministic? :performance? :proof?
                      :freshness-required?) policy))
       (integer? (:timeout-ms policy))
       (<= (:timeout-ms policy) Long/MAX_VALUE)
       (pos? (:timeout-ms policy))))

(defn- request-eligibility
  [request]
  (cond
    (not (map? request))
    {:cacheable? false :reason :invalid-request}

    (some? (meta request))
    {:cacheable? false :reason :invalid-request}

    (not= request-fields (set (keys request)))
    {:cacheable? false :reason :unsupported-request-fields}

    (not (sha256-id? (:repository-identity request)))
    {:cacheable? false :reason :incomplete-repository-identity}

    (not (named-identity? (:test-identity request)))
    {:cacheable? false :reason :incomplete-test-identity}

    (not (complete-policy? (:test-policy request)))
    {:cacheable? false :reason :incomplete-test-policy}

    (not= :non-authoritative (get-in request [:test-policy :authority]))
    {:cacheable? false :reason :authoritative-test}

    (not (true? (get-in request [:test-policy :deterministic?])))
    {:cacheable? false :reason :nondeterministic-test}

    (true? (get-in request [:test-policy :performance?]))
    {:cacheable? false :reason :performance-test}

    (true? (get-in request [:test-policy :proof?]))
    {:cacheable? false :reason :proof-test}

    (true? (get-in request [:test-policy :freshness-required?]))
    {:cacheable? false :reason :freshness-required-test}

    (not (complete-dependencies? (:dependencies request)))
    {:cacheable? false :reason :incomplete-dependencies}

    :else {:cacheable? true}))

(defn- sorted-identities
  [identities field]
  (vec (sort-by (comp scalar-text field) identities)))

(defn- key-material
  [request]
  (array-map
   :schema key-schema
   :repository-identity (:repository-identity request)
   :test-identity (:test-identity request)
   :test-policy (:test-policy request)
   :production-inputs
   (sorted-identities
    (get-in request [:dependencies :production-inputs]) :path)
   :transitive-production-inputs
   (sorted-identities
    (get-in request [:dependencies :transitive-production-inputs]) :path)
   :fixture-contract-inputs
   (sorted-identities
    (get-in request [:dependencies :fixture-contract-inputs]) :path)
   :runner-identity (get-in request [:dependencies :runner-identity])
   :classpath-inputs
   (sorted-identities
    (get-in request [:dependencies :classpath-inputs]) :path)
   :runtime-tool-inputs
   (sorted-identities
    (get-in request [:dependencies :runtime-tool-inputs]) :id)))

(defn cache-key
  "Return the content key for one complete reusable development-test request.

  The absolute cache directory is configuration and is deliberately excluded
  from the key. Unknown request fields, including branch or worktree-root
  fields, make the request ineligible instead of silently affecting identity."
  [request]
  (let [{:keys [cacheable? reason]} (request-eligibility request)]
    (when-not cacheable?
      (fail! "DEV-TEST-CACHE-INELIGIBLE"
             "Development test request is not cacheable"
             {:reason reason}))
    (sha256 (canonical-text (key-material request)
                            maximum-key-material-bytes
                            "DEV-TEST-CACHE-IDENTITY"))))

(defn- reusable-result?
  [result]
  (and (map? result)
       (= :passed (:status result))
       (integer? (:exit-code result))
       (zero? (:exit-code result))
       (= :non-authoritative (:authority result))
       (false? (:authoritative? result))
       (false? (:timed-out? result))
       (not (true? (:nondeterministic? result)))
       (not (true? (:performance? result)))
       (not (true? (:proof? result)))
       (not (true? (:freshness-required? result)))))

(defn- as-path
  [value]
  (cond
    (instance? Path value) value
    (string? value) (Paths/get value (make-array String 0))
    :else nil))

(defn- verify-directory!
  [^Path directory label]
  (when (or (Files/isSymbolicLink directory)
            (not (Files/isDirectory directory no-links)))
    (fail! "DEV-TEST-CACHE-PATH"
           "Development test cache directory is not a regular directory"
           {:path-label label}))
  directory)

(defn- canonical-cache-path!
  [^Path path]
  (loop [component path
         missing-components []]
    (if (Files/exists component no-links)
      (do
        (when (Files/isSymbolicLink component)
          (fail! "DEV-TEST-CACHE-PATH"
                 "Development test cache path contains a symbolic link"
                 {:path-label :cache-path-component}))
        (when-not (Files/isDirectory component no-links)
          (fail! "DEV-TEST-CACHE-PATH"
                 "Development test cache path ancestor is not a directory"
                 {:path-label :cache-path-component}))
        (reduce #(.resolve ^Path %1 ^Path %2)
                (.toRealPath component (make-array LinkOption 0))
                (reverse missing-components)))
      (let [parent (.getParent component)]
        (when-not parent
          (fail! "DEV-TEST-CACHE-PATH"
                 "Development test cache path has no existing ancestor"
                 {:path-label :cache-path-component}))
        (recur parent (conj missing-components (.getFileName component)))))))

(defn- ensure-child-directory!
  [^Path parent name]
  (let [child (.resolve parent name)]
    (when-not (Files/exists child no-links)
      (try
        (Files/createDirectory child no-file-attributes)
        (catch java.nio.file.FileAlreadyExistsException _ nil)))
    (verify-directory! child (keyword name))))

(defn- cache-directories!
  [cache-directory]
  (let [configured-path (as-path cache-directory)]
    (when-not (and configured-path
                   (.isAbsolute ^Path configured-path)
                   (= configured-path (.normalize ^Path configured-path)))
      (fail! "DEV-TEST-CACHE-CONFIGURATION"
             "Cache directory must be an absolute normalized path"
             {}))
    (let [path (canonical-cache-path! configured-path)]
      (Files/createDirectories path no-file-attributes)
      (verify-directory! path :cache-root)
      (let [root (ensure-child-directory! path storage-directory-name)
            entries (ensure-child-directory! root "entries")
            locks (ensure-child-directory! root "locks")]
        {:root root :entries entries :locks locks}))))

(defn- key-file-name
  [key suffix]
  (when-not (sha256-id? key)
    (fail! "DEV-TEST-CACHE-IDENTITY"
           "Cache filename requires a lowercase SHA-256 key"
           {}))
  (str (subs key 7) suffix))

(defn- acquire-local-lock!
  [lock-id]
  (locking in-process-key-locks
    (let [{:keys [lock users]}
          (get @in-process-key-locks lock-id
               {:lock (ReentrantLock.) :users 0})]
      (swap! in-process-key-locks assoc lock-id
             {:lock lock :users (inc users)})
      lock)))

(defn- release-local-lock!
  [lock-id lock]
  (locking in-process-key-locks
    (let [{current-lock :lock users :users}
          (get @in-process-key-locks lock-id)]
      (when (= current-lock lock)
        (if (= 1 users)
          (swap! in-process-key-locks dissoc lock-id)
          (swap! in-process-key-locks assoc lock-id
                 {:lock lock :users (dec users)}))))))

(defn- with-local-key-lock
  [lock-id operation]
  (let [^ReentrantLock lock (acquire-local-lock! lock-id)]
    (.lock lock)
    (try
      (operation)
      (finally
        (.unlock lock)
        (release-local-lock! lock-id lock)))))

(defn- regular-file?
  [^Path path]
  (and (not (Files/isSymbolicLink path))
       (Files/isRegularFile path no-links)))

(defn- with-cross-process-key-lock
  [{:keys [^Path root ^Path locks]} key operation]
  (let [lock-path (.resolve locks (key-file-name key ".lock"))
        lock-id (str root ":" key)]
    (with-local-key-lock
      lock-id
      (fn []
        (when (and (Files/exists lock-path no-links)
                   (not (regular-file? lock-path)))
          (fail! "DEV-TEST-CACHE-PATH"
                 "Development test cache lock is not a regular file"
                 {:path-label :key-lock}))
        (with-open [channel (FileChannel/open lock-path lock-open-options
                                              no-file-attributes)]
          (when-not (regular-file? lock-path)
            (fail! "DEV-TEST-CACHE-PATH"
                   "Development test cache lock is not a regular file"
                   {:path-label :key-lock}))
          (let [file-lock (.lock channel)]
            (try
              (operation)
              (finally
                (.release file-lock)))))))))

(defn- decode-utf8
  [bytes]
  (let [decoder (doto (.newDecoder StandardCharsets/UTF_8)
                  (.onMalformedInput CodingErrorAction/REPORT)
                  (.onUnmappableCharacter CodingErrorAction/REPORT))]
    (str (.decode decoder (ByteBuffer/wrap bytes)))))

(defn- read-one-edn
  [text]
  (let [eof (Object.)]
    (with-open [reader (PushbackReader. (StringReader. text))]
      (let [options {:eof eof
                     :readers {}
                     :default
                     (fn [tag _]
                       (fail! "DEV-TEST-CACHE-CORRUPT"
                              "Tagged cache content is not accepted"
                              {:tag tag}))}
            value (edn/read options reader)
            trailing (edn/read options reader)]
        (when (or (identical? eof value) (not (identical? eof trailing)))
          (fail! "DEV-TEST-CACHE-CORRUPT"
                 "Cache entry must contain exactly one EDN value"
                 {}))
        value))))

(defn- same-file-snapshot?
  [^BasicFileAttributes before ^BasicFileAttributes after]
  (and (= (.fileKey before) (.fileKey after))
       (= (.size before) (.size after))
       (= (.lastModifiedTime before) (.lastModifiedTime after))))

(defn- entry-value-valid?
  [entry request key]
  (and (map? entry)
       (= #{:schema :cache-key :key-material :result :result-sha256}
          (set (keys entry)))
       (= entry-schema (:schema entry))
       (= key (:cache-key entry))
       (= (key-material request) (:key-material entry))
       (= key (sha256 (canonical-text (:key-material entry)
                                      maximum-key-material-bytes
                                      "DEV-TEST-CACHE-CORRUPT")))
       (reusable-result? (:result entry))
       (= (:result-sha256 entry)
          (sha256 (canonical-text (:result entry)
                                  maximum-result-bytes
                                  "DEV-TEST-CACHE-CORRUPT")))))

(defn- read-entry
  [^Path entry-path request key]
  (if-not (Files/exists entry-path no-links)
    {:status :miss}
    (try
      (when-not (regular-file? entry-path)
        (fail! "DEV-TEST-CACHE-CORRUPT"
               "Cache entry is not a regular no-follow file"
               {:reason :invalid-entry-file}))
      (let [before (Files/readAttributes entry-path BasicFileAttributes no-links)
            size (.size ^BasicFileAttributes before)]
        (when (> size maximum-entry-bytes)
          (fail! "DEV-TEST-CACHE-CORRUPT"
                 "Cache entry exceeds its byte bound"
                 {:reason :entry-size-bound}))
        (let [bytes (Files/readAllBytes entry-path)
              after (Files/readAttributes entry-path BasicFileAttributes no-links)
              _ (when-not (and (= size (alength bytes))
                               (same-file-snapshot? before after))
                  (fail! "DEV-TEST-CACHE-CORRUPT"
                         "Cache entry changed during its bounded read"
                         {:reason :entry-read-race}))
              raw (decode-utf8 bytes)
              entry (read-one-edn raw)
              canonical (str (canonical-text entry maximum-entry-bytes
                                             "DEV-TEST-CACHE-CORRUPT")
                             "\n")]
          (when-not (= raw canonical)
            (fail! "DEV-TEST-CACHE-CORRUPT"
                   "Cache entry bytes are not canonical"
                   {:reason :noncanonical-entry}))
          (when-not (entry-value-valid? entry request key)
            (fail! "DEV-TEST-CACHE-CORRUPT"
                   "Cache entry does not bind its request and result"
                   {:reason :entry-binding-mismatch}))
          {:status :hit :result (:result entry)}))
      (catch Throwable error
        (if (fatal-throwable? error)
          (throw error)
          {:status :corrupt
           :reason (or (:reason (ex-data error)) :invalid-entry-content)})))))

(defn- write-all!
  [^FileChannel channel bytes]
  (let [buffer (ByteBuffer/wrap bytes)]
    (while (.hasRemaining buffer)
      (.write channel buffer)))
  (.force channel true))

(defn- publish-entry!
  [^Path entries ^Path entry-path request key result]
  (let [result-text (canonical-text result maximum-result-bytes
                                    "DEV-TEST-CACHE-IDENTITY")
        entry (array-map
               :schema entry-schema
               :cache-key key
               :key-material (key-material request)
               :result result
               :result-sha256 (sha256 result-text))
        bytes (utf8-bytes
               (str (canonical-text entry maximum-entry-bytes
                                    "DEV-TEST-CACHE-IDENTITY")
                    "\n"))
        temporary (.resolve entries
                            (str "." (key-file-name key "") "."
                                 (UUID/randomUUID) ".tmp"))]
    (try
      (if (Files/exists entry-path no-links)
        {:stored? false :reason :destination-occupied}
        (do
          (with-open [channel (FileChannel/open temporary
                                                create-new-write-options
                                                no-file-attributes)]
            (write-all! channel bytes))
          (when-not (regular-file? temporary)
            (fail! "DEV-TEST-CACHE-PATH"
                   "Cache staging file is not a regular file"
                   {:path-label :staging-file}))
          (Files/move temporary entry-path
                      (into-array java.nio.file.CopyOption
                                  [StandardCopyOption/ATOMIC_MOVE]))
          (let [published (read-entry entry-path request key)]
            (if (and (= :hit (:status published))
                     (= result (:result published)))
              {:stored? true :reason :not-found}
              {:stored? false :reason :publication-validation-failed}))))
      (catch AtomicMoveNotSupportedException _
        {:stored? false :reason :atomic-publication-unavailable})
      (catch java.nio.file.FileAlreadyExistsException _
        {:stored? false :reason :destination-occupied})
      (catch java.io.IOException _
        {:stored? false :reason :publication-failed})
      (finally
        (Files/deleteIfExists temporary)))))

(defn- receipt
  [request decision reason key cacheable? stored? producer-executed?
   diagnostic-id]
  (let [base
        (array-map
         :artifact receipt-schema
         :decision decision
         :reason reason
         :diagnostic-id diagnostic-id
         :repository-identity
         (when (sha256-id? (:repository-identity request))
           (:repository-identity request))
         :test-identity
         (when (named-identity? (:test-identity request))
           (:test-identity request))
         :cache-key key
         :cacheable? (boolean cacheable?)
         :stored? (boolean stored?)
         :producer-executed? (boolean producer-executed?)
         :authority :non-authoritative
         :authoritative? false
         :cache-authoritative? false
         :fresh-authoritative-run-required? true
         :benchmark-authority? false
         :release-authority? false
         :proof-authority? false
         :self-hosting-authority? false
         :seed-retirement-authority? false)
        receipt-id
        (sha256 (canonical-text base maximum-receipt-bytes
                                "DEV-TEST-CACHE-IDENTITY"))]
    (assoc base :receipt-id receipt-id)))

(defn- fresh-result
  [request decision reason key cacheable? operation diagnostic-id]
  {:result (operation)
   :receipt (receipt request decision reason key cacheable? false true
                     diagnostic-id)})

(defn lookup!
  "Probe one eligible immutable entry without executing a producer.

  This parent-side seam lets the development runner remove a valid hit before
  submitting child-JVM work. A miss is only an observation: callers must use
  `lookup-or-run!` for the later execution so cross-process singleflight is
  rechecked under the per-key lock. Ineligible and corrupt requests return a
  deterministic non-authoritative receipt and no result."
  [request]
  (let [{:keys [cacheable? reason]} (request-eligibility request)]
    (if-not cacheable?
      {:result nil
       :receipt (receipt request :miss reason nil false false false
                         "DEV-TEST-CACHE-INELIGIBLE")}
      (let [key (cache-key request)
            directories (cache-directories! (:cache-directory request))
            entry-path (.resolve ^Path (:entries directories)
                                 (key-file-name key ".edn"))
            loaded (read-entry entry-path request key)]
        (if (= :hit (:status loaded))
          {:result (:result loaded)
           :receipt (receipt request :hit :matching-input-closure
                             key true false false nil)}
          {:result nil
           :receipt
           (receipt request
                    (if (= :corrupt (:status loaded)) :invalidation :miss)
                    (if (= :corrupt (:status loaded))
                      (:reason loaded)
                      :not-found)
                    key true false false
                    (when (= :corrupt (:status loaded))
                      "DEV-TEST-CACHE-CORRUPT"))})))))

(defn lookup-or-run!
  "Reuse one eligible successful result, or run `operation` fresh.

  Request fields are closed: :cache-directory, :repository-identity,
  :test-identity, :test-policy, and :dependencies. Dependency paths are
  normalized repository-relative identities with content hashes. Runner,
  classpath, and runtime/tool identities are explicit. Incomplete,
  authoritative, nondeterministic, performance, proof, and freshness-required
  requests always execute fresh and never enter singleflight.

  Cooperative same-key Clojure processes execute at most one successful
  producer. Different keys use independent locks. Corrupt entries are rejected,
  preserved, and never overwritten. The caller remains responsible for
  enforcing the timeout declared in the request policy."
  [request operation]
  (when-not (ifn? operation)
    (fail! "DEV-TEST-CACHE-OPERATION"
           "Development test operation must be callable"
           {}))
  (let [{:keys [cacheable? reason]} (request-eligibility request)]
    (if-not cacheable?
      (fresh-result request :miss reason nil false operation
                    "DEV-TEST-CACHE-INELIGIBLE")
      (let [key (cache-key request)
            directories (cache-directories! (:cache-directory request))
            entry-path (.resolve ^Path (:entries directories)
                                 (key-file-name key ".edn"))]
        (with-cross-process-key-lock
          directories key
          (fn []
            (let [loaded (read-entry entry-path request key)]
              (if (= :hit (:status loaded))
                {:result (:result loaded)
                 :receipt (receipt request :hit :matching-input-closure
                                   key true false false nil)}
                (let [result (operation)
                      reusable? (reusable-result? result)
                      result-text
                      (when reusable?
                        (try
                          (canonical-text result maximum-result-bytes
                                          "DEV-TEST-CACHE-IDENTITY")
                          (catch clojure.lang.ExceptionInfo _ nil)))
                      corrupt? (= :corrupt (:status loaded))
                      publication
                      (when (and (not corrupt?) reusable? result-text)
                        (publish-entry! (:entries directories) entry-path
                                        request key result))
                      stored? (true? (:stored? publication))
                      decision (if corrupt? :invalidation :miss)
                      final-reason
                      (cond
                        corrupt? (:reason loaded)
                        (not reusable?) :result-not-reusable
                        (nil? result-text) :result-not-persistable
                        publication (:reason publication)
                        :else :not-found)
                      diagnostic-id
                      (when corrupt? "DEV-TEST-CACHE-CORRUPT")]
                  {:result result
                   :receipt
                   (receipt request decision final-reason key true stored?
                            true diagnostic-id)})))))))))
