;; Semantic C2 pass-cache component loaded by gravity.c2-pass-cache.
;; It deliberately evaluates in the facade namespace to retain private test seams.
(defn- raw-path-has-parent-segment?
  [^Path path]
  (boolean (some #(= ".." (str %)) (iterator-seq (.iterator path)))))

(defn- normalized-absolute-path!
  [path]
  (let [raw (Paths/get (str path) (make-array String 0))]
    (when (raw-path-has-parent-segment? raw)
      (cache-fail! "C16-POLICY" "cache paths cannot contain parent traversal"
                   {:path (str path)}))
    (.normalize (.toAbsolutePath raw))))

(defn- basic-attributes
  [^Path path]
  (Files/readAttributes path BasicFileAttributes nofollow-links))

(defn- unix-link-count
  [^Path path]
  (long (Files/getAttribute path "unix:nlink" nofollow-links)))

(defn- current-owner-name
  []
  (System/getProperty "user.name"))

(defn- path-owner-name
  [^Path path]
  (.getName (Files/getOwner path nofollow-links)))

(defn- current-user-owned?
  [^Path path]
  (= (current-owner-name) (path-owner-name path)))

(defn- safe-shared-directory-permissions?
  [permissions]
  (and (contains? permissions
                  java.nio.file.attribute.PosixFilePermission/OWNER_READ)
       (contains? permissions
                  java.nio.file.attribute.PosixFilePermission/OWNER_WRITE)
       (contains? permissions
                  java.nio.file.attribute.PosixFilePermission/OWNER_EXECUTE)
       (not (contains? permissions
                       java.nio.file.attribute.PosixFilePermission/GROUP_WRITE))
       (not (contains? permissions
                       java.nio.file.attribute.PosixFilePermission/OTHERS_WRITE))))

(defn- verify-directory!
  [^Path path owned?]
  (let [attributes (basic-attributes path)
        permissions (Files/getPosixFilePermissions path nofollow-links)]
    (when-not (.isDirectory attributes)
      (cache-fail! "C16-POLICY" "cache path is not a no-follow directory"
                   {:path (str path)}))
    (when-not (current-user-owned? path)
      (cache-fail! "C16-POLICY" "cache directory is not owned by the current user"
                   {:path (str path)
                    :owner (path-owner-name path)
                    :required-owner (current-owner-name)}))
    (when-not (if owned?
                (= owned-directory-permissions permissions)
                (safe-shared-directory-permissions? permissions))
      (cache-fail! "C16-POLICY"
                   (if owned?
                     "cache-owned directory permissions changed"
                     "shared cache parent has an unsafe permission policy")
                   {:path (str path)
                    :observed (PosixFilePermissions/toString permissions)
                    :required (if owned? "0700"
                                  "owner-rwx-and-no-group-or-other-write")})))
  path)

(defn- directory-identity
  [^Path path owned?]
  (verify-directory! path owned?)
  (let [attributes (basic-attributes path)]
    {:path path
     :owned? owned?
     :file-key (.fileKey attributes)
     :device (Files/getAttribute path "unix:dev" nofollow-links)
     :inode (Files/getAttribute path "unix:ino" nofollow-links)
     :owner (path-owner-name path)
     :permissions (Files/getPosixFilePermissions path nofollow-links)}))

(defn- verify-directory-identity!
  [{:keys [^Path path owned? file-key device inode owner permissions]}]
  (verify-directory! path owned?)
  (let [attributes (basic-attributes path)
        observed {:file-key (.fileKey attributes)
                  :device (Files/getAttribute path "unix:dev" nofollow-links)
                  :inode (Files/getAttribute path "unix:ino" nofollow-links)
                  :owner (path-owner-name path)
                  :permissions (Files/getPosixFilePermissions path nofollow-links)}]
    (when-not (= {:file-key file-key
                  :device device
                  :inode inode
                  :owner owner
                  :permissions permissions}
                 observed)
      (cache-fail! "C16-POLICY"
                   "cache directory identity changed after store open"
                   {:path (str path)})))
  path)

(defn- verify-store-identity!
  [store]
  (doseq [identity (:directory-identities store)]
    (verify-directory-identity! identity))
  store)

(defn- ensure-base-directory!
  [^Path base]
  (when-not (Files/exists base nofollow-links)
    (cache-fail! "C16-POLICY"
                 "explicit cache base must already exist"
                 {:path (str base)}))
  (verify-directory! base false))

(defn- require-secure-directory-stream!
  [stream operation]
  (when-not (instance? SecureDirectoryStream stream)
    (cache-fail! "C16-POLICY"
                 "filesystem provider lacks descriptor-relative cache access"
                 {:operation operation
                  :provider (.getName (class stream))}))
  ^SecureDirectoryStream stream)

(defn- secure-self-attributes
  [^SecureDirectoryStream directory]
  (let [^BasicFileAttributeView basic-view
        (.getFileAttributeView directory BasicFileAttributeView)
        ^PosixFileAttributeView posix-view
        (.getFileAttributeView directory PosixFileAttributeView)]
    (when-not (and basic-view posix-view)
      (cache-fail! "C16-POLICY"
                   "filesystem provider lacks required secure POSIX views"
                   {}))
    {:basic (.readAttributes basic-view)
     :posix (.readAttributes posix-view)}))

(defn- secure-child-attributes
  [^SecureDirectoryStream directory ^Path relative]
  (let [^BasicFileAttributeView basic-view
        (.getFileAttributeView directory relative BasicFileAttributeView
                               nofollow-links)
        ^PosixFileAttributeView posix-view
        (.getFileAttributeView directory relative PosixFileAttributeView
                               nofollow-links)]
    (when-not (and basic-view posix-view)
      (cache-fail! "C16-POLICY"
                   "filesystem provider lacks required secure child views"
                   {:name (str relative)}))
    {:basic (.readAttributes basic-view)
     :posix (.readAttributes posix-view)}))

(defn- same-basic-file?
  [^BasicFileAttributes left ^BasicFileAttributes right]
  (and (= (.fileKey left) (.fileKey right))
       (= (.size left) (.size right))
       (= (.lastModifiedTime left) (.lastModifiedTime right))))

(defn- verify-secure-directory-handle!
  [^SecureDirectoryStream directory identity]
  (let [{:keys [^BasicFileAttributes basic ^PosixFileAttributes posix]}
        (secure-self-attributes directory)]
    (when-not (and (.isDirectory basic)
                   (= (:file-key identity) (.fileKey basic))
                   (= (:owner identity) (.getName (.owner posix)))
                   (= (:permissions identity) (.permissions posix)))
      (cache-fail! "C16-POLICY"
                   "secure directory handle does not match pinned store identity"
                   {:path (str (:path identity))})))
  directory)

(defn- correlated-unix-link-count!
  [^Path absolute ^BasicFileAttributes secure-basic diagnostic-id]
  ;; SecureDirectoryStream deliberately exposes only basic/POSIX views, not
  ;; unix:nlink.  This read-only path query is accepted only when path attrs on
  ;; both sides match the already held descriptor-relative file identity.  No
  ;; child is ever opened or mutated through this path.
  (let [before (basic-attributes absolute)]
    (when-not (same-basic-file? secure-basic before)
      (cache-fail! diagnostic-id
                   "path link-count probe diverged from secure file identity"
                   {:path (str absolute)}))
    (let [links (unix-link-count absolute)
          after (basic-attributes absolute)]
      (when-not (and (same-basic-file? secure-basic after)
                     (= (.fileKey before) (.fileKey after)))
        (cache-fail! diagnostic-id
                     "path changed during link-count integrity probe"
                     {:path (str absolute)}))
      links)))

(defn- require-file-channel!
  [channel diagnostic-id operation]
  (when-not (instance? FileChannel channel)
    (.close ^SeekableByteChannel channel)
    (cache-fail! diagnostic-id
                 "filesystem provider did not supply a durable file channel"
                 {:operation operation
                  :provider (.getName (class channel))}))
  ^FileChannel channel)

(defn- read-channel-exact!
  [^FileChannel channel size diagnostic-id description]
  (let [buffer (ByteBuffer/allocate (int size))]
    (loop []
      (when (.hasRemaining buffer)
        (let [count (.read channel buffer)]
          (when (= -1 count)
            (cache-fail! diagnostic-id
                         (str description " ended before its declared size")
                         {:expected-bytes size}))
          (recur))))
    (when-not (= -1 (.read channel (ByteBuffer/allocate 1)))
      (cache-fail! diagnostic-id (str description " grew during bounded read")
                   {:expected-bytes size}))
    (.array buffer)))
