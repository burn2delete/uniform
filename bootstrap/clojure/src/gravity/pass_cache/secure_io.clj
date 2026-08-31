(ns gravity.pass-cache.secure-io
  "Descriptor-relative bounded I/O and immutable publication primitives."
  (:require [gravity.pass-cache.path-policy :refer :all]
            [gravity.pass-cache.policy :refer :all])
  (:import [java.nio ByteBuffer]
           [java.nio.channels FileChannel SeekableByteChannel]
           [java.nio.file DirectoryStream LinkOption Path Paths SecureDirectoryStream
            StandardOpenOption]
           [java.nio.file.attribute BasicFileAttributes BasicFileAttributeView
            FileAttribute PosixFileAttributeView PosixFileAttributes]
           [java.util HashSet UUID]))

(defn require-secure-directory-stream!
  [stream operation]
  (when-not (instance? SecureDirectoryStream stream)
    (fail! "C16-POLICY" "filesystem provider lacks descriptor-relative cache access"
           {:operation operation
            :provider (.getName (class stream))}))
  ^SecureDirectoryStream stream)

(defn secure-self-attributes
  [^SecureDirectoryStream directory]
  (let [^BasicFileAttributeView basic-view
        (.getFileAttributeView directory BasicFileAttributeView)
        ^PosixFileAttributeView posix-view
        (.getFileAttributeView directory PosixFileAttributeView)]
    (when-not (and basic-view posix-view)
      (fail! "C16-POLICY" "filesystem provider lacks required secure POSIX views" {}))
    {:basic (.readAttributes basic-view)
     :posix (.readAttributes posix-view)}))

(defn secure-child-attributes
  [^SecureDirectoryStream directory ^Path relative]
  (let [relative (relative-name! relative)
        ^BasicFileAttributeView basic-view
        (.getFileAttributeView directory relative BasicFileAttributeView
                               nofollow-links)
        ^PosixFileAttributeView posix-view
        (.getFileAttributeView directory relative PosixFileAttributeView
                               nofollow-links)]
    (when-not (and basic-view posix-view)
      (fail! "C16-POLICY" "filesystem provider lacks required secure child views"
             {:name (str relative)}))
    {:basic (.readAttributes basic-view)
     :posix (.readAttributes posix-view)}))

(defn same-basic-file?
  [^BasicFileAttributes left ^BasicFileAttributes right]
  (and (= (.fileKey left) (.fileKey right))
       (= (.size left) (.size right))
       (= (.lastModifiedTime left) (.lastModifiedTime right))))

(defn verify-secure-directory-handle!
  [^SecureDirectoryStream directory identity]
  (let [{:keys [^BasicFileAttributes basic ^PosixFileAttributes posix]}
        (secure-self-attributes directory)]
    (when-not (and (.isDirectory basic)
                   (= (:file-key identity) (.fileKey basic))
                   (= (:owner identity) (.getName (.owner posix)))
                   (= (:permissions identity) (.permissions posix)))
      (fail! "C16-POLICY" "secure directory handle does not match pinned identity"
             {:path (str (:path identity))})))
  directory)

(defn secure-child-exists?
  [^SecureDirectoryStream directory ^Path relative]
  (let [relative (relative-name! relative)]
   (try
    (secure-child-attributes directory relative)
    true
    (catch java.nio.file.NoSuchFileException _ false))))

(defn secure-file-attributes-relative!
  [store path-key ^SecureDirectoryStream directory ^Path relative maximum-bytes]
  (let [relative (relative-name! relative)
        {:keys [^BasicFileAttributes basic ^PosixFileAttributes posix] :as child-attrs}
        (secure-child-attributes directory relative)
        absolute (.resolve ^Path (get store path-key) relative)
        path-before (attrs absolute)
        _ (when-not (same-basic-file? basic path-before)
            (fail! "C16-POLICY"
                   "path link-count probe diverged from secure file identity"
                   {:path (str absolute)}))
        links (unix-link-count absolute)
        path-after (attrs absolute)
        _ (when-not (same-basic-file? basic path-after)
            (fail! "C16-POLICY"
                   "cache path changed during link-count integrity probe"
                   {:path (str absolute)}))]
    (when-not (and (.isRegularFile basic) (not (.isSymbolicLink basic))
                   (= 1 links) (<= 0 (.size basic) maximum-bytes)
                   (= (current-owner-name) (.getName (.owner posix)))
                   (= private-file-permissions (.permissions posix)))
      (fail! "C16-POLICY" "cache file failed descriptor-relative integrity checks"
             {:path-key path-key :name (str relative)
              :regular-file? (.isRegularFile basic)
              :symbolic-link? (.isSymbolicLink basic)
              :link-count links :observed-bytes (.size basic)
              :maximum-bytes maximum-bytes}))
    child-attrs))

(defn require-file-channel!
  [channel diagnostic-id operation]
  (if (instance? FileChannel channel)
    ^FileChannel channel
    (do
      (.close ^SeekableByteChannel channel)
      (fail! diagnostic-id "filesystem provider did not supply durable file channel"
             {:operation operation :provider (.getName (class channel))}))))

(defn read-channel-exact!
  [^FileChannel channel size diagnostic-id description]
  (let [buffer (ByteBuffer/allocate (int size))]
    (loop []
      (when (.hasRemaining buffer)
        (let [count (.read channel buffer)]
          (when (= -1 count)
            (fail! diagnostic-id (str description " ended before its declared size")
                   {:expected-bytes size}))
          (recur))))
    (when-not (= -1 (.read channel (ByteBuffer/allocate 1)))
      (fail! diagnostic-id (str description " grew during bounded read")
             {:expected-bytes size}))
    (.array buffer)))

(defn secure-fsync-directory!
  [^SecureDirectoryStream directory]
  (let [raw (.newByteChannel directory (Paths/get "." (make-array String 0))
                             (HashSet. [StandardOpenOption/READ
                                        LinkOption/NOFOLLOW_LINKS])
                             (make-array FileAttribute 0))
        channel (require-file-channel! raw "C16-POLICY"
                                       :secure-directory-fsync)]
    (with-open [channel channel]
      (.force channel true))))

(defn secure-read-bytes!
  [store path-key ^SecureDirectoryStream directory ^Path relative maximum-bytes]
  (let [relative (relative-name! relative)
        before (secure-file-attributes-relative!
                store path-key directory relative maximum-bytes)
        expected-size (long (.size ^BasicFileAttributes (:basic before)))
        raw (.newByteChannel directory relative
                             (HashSet. [StandardOpenOption/READ
                                        LinkOption/NOFOLLOW_LINKS])
                             (make-array FileAttribute 0))
        channel (require-file-channel! raw "C16-ENTRY" :secure-cache-read)]
    (with-open [channel channel]
      (let [bytes (read-channel-exact! channel expected-size "C16-ENTRY"
                                        "cache file")
            after (secure-file-attributes-relative!
                   store path-key directory relative maximum-bytes)]
        (when-not (same-basic-file? (:basic before) (:basic after))
          (fail! "C16-ENTRY" "cache file changed during secure read"
                 {:path-key path-key :name (str relative)}))
        bytes))))

(defn secure-write-new!
  [store path-key ^SecureDirectoryStream directory ^Path relative ^bytes bytes]
  (let [relative (relative-name! relative)
        raw (.newByteChannel directory relative create-new-write-options
                             (into-array FileAttribute [private-file-attribute]))
        channel (require-file-channel! raw "C16-ENTRY"
                                       :secure-cache-publication)]
    (with-open [channel channel]
      (let [buffer (ByteBuffer/wrap bytes)]
        (while (.hasRemaining buffer)
          (.write channel buffer)))
      (.force channel true))
    (secure-file-attributes-relative! store path-key directory relative
                                       (alength bytes))
    (secure-fsync-directory! directory)
    relative))

(defn secure-publish-move!
  [^SecureDirectoryStream staging ^Path temporary
   ^SecureDirectoryStream destination-directory ^Path destination]
  (let [temporary (relative-name! temporary)
        destination (relative-name! destination)]
   (when-not (and (= "sun.nio.fs.UnixSecureDirectoryStream"
                    (.getName (class staging)))
                 (= "sun.nio.fs.UnixSecureDirectoryStream"
                    (.getName (class destination-directory))))
    (fail! "C16-POLICY" "filesystem provider lacks anchored atomic rename"
           {:source-provider (.getName (class staging))
            :destination-provider (.getName (class destination-directory))}))
   (.move staging temporary destination-directory destination)))

(defn bytes-equal?
  [^bytes left ^bytes right]
  (java.util.Arrays/equals left right))

(defn publish-create-or-verify!
  [store directories path-key ^SecureDirectoryStream destination-directory
   ^Path destination ^bytes bytes maximum-bytes]
  (let [destination (relative-name! destination)]
   (if (secure-child-exists? destination-directory destination)
    (let [existing (secure-read-bytes! store path-key destination-directory
                                       destination maximum-bytes)]
      (when-not (bytes-equal? existing bytes)
        (fail! "C16-ENTRY" "immutable cache destination contains divergent bytes"
               {:path-key path-key :name (str destination)}))
      :verified-identical)
    (let [^SecureDirectoryStream staging (:staging directories)
          temporary (Paths/get (str ".stage-" (UUID/randomUUID) ".tmp")
                               (make-array String 0))
          active-path (str (.resolve ^Path (:staging store) temporary))]
      (swap! active-staging conj active-path)
      (try
        (secure-write-new! store :staging staging temporary bytes)
        ;; Test-safe private hook for crash-recovery validation.  Production
        ;; callers leave it nil; a killed writer leaves a durable stage for the
        ;; next descriptor-locked bootstrap to recover.
        (when *publication-hook*
          (*publication-hook*))
        (if (secure-child-exists? destination-directory destination)
          (let [existing (secure-read-bytes!
                          store path-key destination-directory destination
                          maximum-bytes)]
            (when-not (bytes-equal? existing bytes)
              (fail! "C16-ENTRY"
                     "concurrent immutable cache publication conflicts"
                     {:path-key path-key :name (str destination)}))
            :converged-identical)
          (do
            (secure-publish-move! staging temporary destination-directory
                                  destination)
            (secure-fsync-directory! staging)
            (secure-fsync-directory! destination-directory)
            (let [published (secure-read-bytes!
                             store path-key destination-directory destination
                             maximum-bytes)]
              (when-not (bytes-equal? published bytes)
                (fail! "C16-ENTRY" "published cache bytes changed"
                       {:path-key path-key :name (str destination)})))
            :published))
        (finally
          (when (secure-child-exists? staging temporary)
            (.deleteFile staging temporary)
            (secure-fsync-directory! staging))
          (swap! active-staging disj active-path)))))))
