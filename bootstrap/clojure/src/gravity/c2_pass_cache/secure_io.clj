;; Semantic C2 pass-cache component loaded by gravity.c2-pass-cache.
;; It deliberately evaluates in the facade namespace to retain private test seams.
(defn- secure-child-exists?
  [^SecureDirectoryStream directory ^Path relative]
  (try
    (secure-child-attributes directory relative)
    true
    (catch java.nio.file.NoSuchFileException _ false)))

(defn- secure-file-attributes-relative!
  [store path-key ^SecureDirectoryStream directory ^Path relative
   maximum-bytes]
  (let [{:keys [^BasicFileAttributes basic ^PosixFileAttributes posix]
         :as attributes}
        (secure-child-attributes directory relative)
        absolute (.resolve ^Path (get store path-key) relative)
        link-count (correlated-unix-link-count! absolute basic "C16-ENTRY")]
    (when-not (and (.isRegularFile basic)
                   (not (.isSymbolicLink basic))
                   (= 1 link-count)
                   (<= 0 (.size basic) maximum-bytes)
                   (= (current-owner-name) (.getName (.owner posix)))
                   (= owned-file-permissions (.permissions posix)))
      (cache-fail! "C16-ENTRY"
                   "cache file failed descriptor-relative integrity checks"
                   {:path-key path-key
                    :name (str relative)
                    :regular-file? (.isRegularFile basic)
                    :symbolic-link? (.isSymbolicLink basic)
                    :link-count link-count
                    :observed-bytes (.size basic)
                    :maximum-bytes maximum-bytes
                    :owner (.getName (.owner posix))
                    :required-owner (current-owner-name)
                    :required-permissions "0600"}))
    attributes))

(defn- secure-read-bytes!
  [store path-key ^SecureDirectoryStream directory ^Path relative
   maximum-bytes]
  (let [before (secure-file-attributes-relative!
                store path-key directory relative maximum-bytes)
        ^BasicFileAttributes before-basic (:basic before)
        expected-size (long (.size before-basic))
        raw-channel (.newByteChannel
                     directory relative
                     (HashSet. [StandardOpenOption/READ
                                LinkOption/NOFOLLOW_LINKS])
                     (make-array FileAttribute 0))
        channel (require-file-channel! raw-channel "C16-ENTRY"
                                       :secure-cache-read)]
    (with-open [channel channel]
      (let [bytes (read-channel-exact! channel expected-size "C16-ENTRY"
                                       "cache file")
            after (secure-file-attributes-relative!
                   store path-key directory relative maximum-bytes)]
        (when-not (and (= expected-size (.size channel))
                       (same-basic-file? before-basic (:basic after)))
          (cache-fail! "C16-ENTRY" "cache file changed during secure read"
                       {:path-key path-key :name (str relative)}))
        bytes))))

(defn- secure-fsync-directory!
  [^SecureDirectoryStream directory]
  (let [raw-channel (.newByteChannel
                     directory (relative-name ".")
                     (HashSet. [StandardOpenOption/READ
                                LinkOption/NOFOLLOW_LINKS])
                     (make-array FileAttribute 0))
        channel (require-file-channel! raw-channel "C16-POLICY"
                                       :secure-directory-fsync)]
    (with-open [channel channel]
      (.force channel true))))

(defn- secure-write-new!
  [store path-key ^SecureDirectoryStream directory ^Path relative bytes]
  (let [raw-channel (.newByteChannel
                     directory relative create-new-write-options
                     (into-array FileAttribute [file-attribute]))
        channel (require-file-channel! raw-channel "C16-ENTRY"
                                       :secure-cache-publication)]
    (with-open [channel channel]
      (let [buffer (ByteBuffer/wrap bytes)]
        (while (.hasRemaining buffer)
          (.write channel buffer)))
      (.force channel true))
    (secure-file-attributes-relative!
     store path-key directory relative (alength bytes))
    (secure-fsync-directory! directory)))

(defn- lock-name
  [key]
  (relative-name
   (str (subs (:storage-key-id key) (count "sha256:")) ".lock")))

(defn- secure-ensure-lock-file!
  [store directories relative]
  (let [^SecureDirectoryStream directory (:locks directories)]
    (when-not (secure-child-exists? directory relative)
      (try
        (secure-write-new! store :locks directory relative (byte-array 0))
        (catch java.nio.file.FileAlreadyExistsException _ nil)))
    (secure-file-attributes-relative! store :locks directory relative 0)
    relative))

(defn- secure-directory-inventory!
  [store path-key ^SecureDirectoryStream directory name-pattern
   maximum-count maximum-file-bytes]
  (loop [items (iterator-seq (.iterator directory))
         count 0
         bytes 0]
    (if-let [item (first items)]
      (let [name (str (.getFileName ^Path item))
            next-count (inc count)]
        (when (or (> next-count maximum-count)
                  (not (boolean (re-matches name-pattern name))))
          (cache-fail! "C16-POLICY" "cache store inventory violates policy"
                       {:path-key path-key
                        :observed-name name
                        :maximum-count maximum-count}))
        (let [relative (relative-name name)
              attributes (secure-file-attributes-relative!
                          store path-key directory relative maximum-file-bytes)
              next-bytes (+ bytes (.size ^BasicFileAttributes
                                         (:basic attributes)))]
          (when (> next-bytes maximum-store-bytes)
            (cache-fail! "C16-POLICY"
                         "cache directory exceeds aggregate byte policy"
                         {:path-key path-key
                          :maximum-aggregate-bytes maximum-store-bytes}))
          (recur (next items) next-count next-bytes)))
      {:count count :bytes bytes})))

(defn- secure-store-inventory!
  [store directories]
  (let [blobs (secure-directory-inventory!
               store :blobs (:blobs directories) #"[0-9a-f]{64}\.edn"
               maximum-blob-count maximum-blob-bytes)
        entries (secure-directory-inventory!
                 store :entries (:entries directories) #"[0-9a-f]{64}\.edn"
                 maximum-entry-count maximum-entry-bytes)
        locks (secure-directory-inventory!
               store :locks (:locks directories)
               #"(?:store|[0-9a-f]{64})\.lock"
               maximum-lock-count 0)
        staging (secure-directory-inventory!
                 store :staging (:staging directories)
                 #"\.stage-[0-9a-f-]{36}\.tmp"
                 maximum-staging-count maximum-blob-bytes)
        aggregate (+ (:bytes blobs) (:bytes entries) (:bytes locks)
                     (:bytes staging))]
    (when (> aggregate maximum-store-bytes)
      (cache-fail! "C16-POLICY" "cache store exceeds aggregate byte policy"
                   {:observed-aggregate-bytes aggregate
                    :maximum-aggregate-bytes maximum-store-bytes}))
    {:blobs blobs :entries entries :locks locks :staging staging
     :aggregate-bytes aggregate}))

(defn- fresh-store-inventory!
  [store]
  (with-secure-store-directories
   store
   (fn [directories]
     (secure-store-inventory! store directories))))

(defn- recover-staging-residue!
  [store]
  (with-secure-store-directories
   store
   (fn [directories]
     (let [^SecureDirectoryStream staging (:staging directories)
           items (vec (iterator-seq (.iterator staging)))]
       (when (> (count items) maximum-staging-count)
         (cache-fail! "C16-POLICY"
                      "cache staging residue exceeds recovery policy"
                      {:maximum-staging-count maximum-staging-count}))
       (doseq [item items]
         (let [name (str (.getFileName ^Path item))
               relative (relative-name name)]
           (when-not (re-matches #"\.stage-[0-9a-f-]{36}\.tmp" name)
             (cache-fail! "C16-POLICY"
                          "cache staging residue name is invalid"
                          {:observed-name name}))
           (secure-file-attributes-relative!
            store :staging staging relative maximum-blob-bytes)
           (.deleteFile staging relative)))
       (when (seq items)
         (secure-fsync-directory! staging))))))

(defn- with-global-store-lock
  [store operation]
  (let [[local-key local-lock]
        (acquire-in-process-key-lock! (str (:root store) ":global"))]
    (try
      (with-secure-store-directories
       store
       (fn [directories]
         (let [lock-name (secure-ensure-lock-file!
                          store directories (relative-name "store.lock"))
               ^SecureDirectoryStream lock-directory (:locks directories)
               raw-channel
               (.newByteChannel lock-directory lock-name
                                (HashSet. [StandardOpenOption/READ
                                           StandardOpenOption/WRITE
                                           LinkOption/NOFOLLOW_LINKS])
                                (make-array FileAttribute 0))
               channel (require-file-channel! raw-channel "C16-POLICY"
                                              :secure-store-lock)]
           (with-open [channel channel
                       lock (.lock channel)]
             (secure-file-attributes-relative!
              store :locks lock-directory lock-name 0)
             (recover-staging-residue! store)
             ;; Recovery can invalidate directory-entry snapshots held by an
             ;; already-open provider stream.  Keep the global lock channel,
             ;; but perform admission/publication through a fresh anchored
             ;; traversal after recovery.
             (with-secure-store-directories store operation)))))
      (finally
        (release-in-process-key-lock! local-key local-lock)))))
