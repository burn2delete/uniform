;; Semantic C2 pass-cache component loaded by gravity.c2-pass-cache.
;; It deliberately evaluates in the facade namespace to retain private test seams.
+(defn- relative-name
  [filename]
  (Paths/get filename (make-array String 0)))

(declare secure-write-new! secure-fsync-directory! secure-child-exists?
         secure-file-attributes-relative! require-file-channel!)


(defn- verify-secure-child-directory!
  [^SecureDirectoryStream parent ^Path relative owned?]
  (let [{:keys [^BasicFileAttributes basic ^PosixFileAttributes posix]}
        (secure-child-attributes parent relative)
        permissions (.permissions posix)]
    (when-not (and (.isDirectory basic)
                   (not (.isSymbolicLink basic))
                   (= (current-owner-name) (.getName (.owner posix)))
                   (if owned?
                     (= owned-directory-permissions permissions)
                     (safe-shared-directory-permissions? permissions)))
      (cache-fail! "C16-POLICY"
                   "cache directory failed descriptor-relative policy"
                   {:name (str relative)
                    :owned? owned?
                    :owner (.getName (.owner posix))
                    :permissions
                    (PosixFilePermissions/toString permissions)})))
  relative)

(defn- secure-directory-move!
  [^SecureDirectoryStream source ^Path source-name
   ^SecureDirectoryStream destination ^Path destination-name]
  (when-not (and (= "sun.nio.fs.UnixSecureDirectoryStream"
                    (.getName (class source)))
                 (= "sun.nio.fs.UnixSecureDirectoryStream"
                    (.getName (class destination))))
    (cache-fail! "C16-POLICY"
                 "filesystem provider lacks anchored directory publication"
                 {:source-provider (.getName (class source))
                  :destination-provider (.getName (class destination))}))
  (.move source source-name destination destination-name))

(defn- secure-ensure-child-directory!
  [^SecureDirectoryStream parent child-name owned?]
  (let [relative (relative-name child-name)]
    (when-not (secure-child-exists? parent relative)
      (let [temporary
            (Files/createTempDirectory
             "gravity-c2-cache-mkdir-"
             (into-array FileAttribute [directory-attribute]))
            source-parent (.getParent temporary)
            source-relative (.getFileName temporary)
            source-parent-identity
            (directory-identity source-parent false)]
        (with-open [raw-source (Files/newDirectoryStream source-parent)]
          (let [source
                (require-secure-directory-stream!
                 raw-source :cache-directory-bootstrap)]
            (try
              (verify-secure-directory-handle!
               source source-parent-identity)
              (verify-secure-child-directory! source source-relative true)
              (if (secure-child-exists? parent relative)
                nil
                (secure-directory-move!
                 source source-relative parent relative))
              (secure-fsync-directory! source)
              (secure-fsync-directory! parent)
              (finally
                ;; Cleanup is anchored too.  A successful move makes the
                ;; detached name absent; a pre-move failure removes it through
                ;; the still-held source parent descriptor.
                (when (secure-child-exists? source source-relative)
                  (.deleteDirectory source source-relative)
                  (secure-fsync-directory! source))))))))
    (verify-secure-child-directory! parent relative owned?)
    (let [child (.newDirectoryStream parent relative nofollow-links)
          secure-child
          (require-secure-directory-stream!
           child :cache-directory-bootstrap-child)]
      (try
        (let [{:keys [^BasicFileAttributes basic
                      ^PosixFileAttributes posix]}
              (secure-self-attributes secure-child)]
          (when-not (and (.isDirectory basic)
                         (= (current-owner-name)
                            (.getName (.owner posix))))
            (cache-fail! "C16-POLICY"
                         "opened cache directory failed identity policy"
                         {:name child-name})))
        secure-child
        (catch Throwable error
          (.close ^DirectoryStream child)
          (throw error))))))

(defn- with-cache-bootstrap-lock
  [^Path base ^SecureDirectoryStream base-directory operation]
  (let [lock-name (relative-name ".cpcache-bootstrap.lock")
        partial-store {:base base}
        [local-key local-lock]
        (acquire-in-process-key-lock! (str base ":bootstrap"))]
    (try
      (when-not (secure-child-exists? base-directory lock-name)
        (try
          (secure-write-new! partial-store :base base-directory lock-name
                             (byte-array 0))
          (catch java.nio.file.FileAlreadyExistsException _ nil)))
      (secure-file-attributes-relative!
       partial-store :base base-directory lock-name 0)
      (let [raw-channel
            (.newByteChannel base-directory lock-name
                             (HashSet. [StandardOpenOption/READ
                                        StandardOpenOption/WRITE
                                        LinkOption/NOFOLLOW_LINKS])
                             (make-array FileAttribute 0))
            channel (require-file-channel! raw-channel "C16-POLICY"
                                           :cache-bootstrap-lock)]
        (with-open [channel channel
                    lock (.lock channel)]
          (operation)))
      (finally
        (release-in-process-key-lock! local-key local-lock)))))

(defn- legacy-v1-open-local-store
  "Open or create the isolated local cache below `base-path`.

  The returned store has no release or proof authority.  Cache-owned
  directories are required to remain mode 0700."
  [base-path]
  (with-contained-host-errors
   "C16-POLICY" :open-local-store
   (fn []
     (let [base (normalized-absolute-path! base-path)
           _ (ensure-base-directory! base)
           base-identity (directory-identity base false)
           _
           (with-open [raw-base (Files/newDirectoryStream base)]
             (let [base-directory
                   (require-secure-directory-stream!
                    raw-base :cache-directory-bootstrap-base)]
               (verify-secure-directory-handle!
                base-directory base-identity)
               (with-cache-bootstrap-lock
                base base-directory
                (fn []
                  ;; Clojure CLI may already own `.cpcache`; an existing safe
                  ;; parent is reused.  Every absent final namespace is a
                  ;; descriptor-relative move of a private detached directory.
                  (with-open [cpcache-directory
                              (secure-ensure-child-directory!
                               base-directory ".cpcache" false)]
                    (with-open [compiler-pass-directory
                                (secure-ensure-child-directory!
                                 cpcache-directory "compiler-pass" true)]
                      (with-open [root-directory
                                  (secure-ensure-child-directory!
                                   compiler-pass-directory "v1" true)]
                        (doseq [child ["blobs" "entries" "locks" "staging"]]
                          (with-open [owned-directory
                                      (secure-ensure-child-directory!
                                       root-directory child true)]
                            (verify-secure-child-directory!
                             root-directory (relative-name child) true))))))))))
           cpcache (.resolve base ".cpcache")
           compiler-pass (.resolve cpcache "compiler-pass")
           root (.resolve compiler-pass "v1")
           blobs (.resolve root "blobs")
           entries (.resolve root "entries")
           locks (.resolve root "locks")
           staging (.resolve root "staging")
           store
           {:artifact :gravity/local-compiler-pass-cache-store
            :schema-version cache-schema-version
            :base base
            :cpcache cpcache
            :compiler-pass compiler-pass
            :root root
            :blobs blobs
            :entries entries
            :locks locks
            :staging staging
            :directory-identities
            [base-identity
             (directory-identity cpcache false)
             (directory-identity compiler-pass true)
             (directory-identity root true)
             (directory-identity blobs true)
             (directory-identity entries true)
             (directory-identity locks true)
             (directory-identity staging true)]
            :store-policy
            {:maximum-entry-count maximum-entry-count
             :maximum-blob-count maximum-blob-count
             :maximum-lock-count maximum-lock-count
             :maximum-staging-count maximum-staging-count
             :maximum-aggregate-bytes maximum-store-bytes}
            :local-development-only? true
            :release-authority? false}]
       (verify-store-identity! store)))))
