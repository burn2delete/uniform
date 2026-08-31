(ns gravity.pass-cache.store-directories
  "Pinned secure-directory traversal, creation, and bounded inventory."
  (:require [gravity.pass-cache.path-policy :refer :all]
            [gravity.pass-cache.policy :refer :all]
            [gravity.pass-cache.secure-io :refer :all])
  (:import [java.nio.file DirectoryStream Files LinkOption Path
            SecureDirectoryStream]
           [java.nio.file.attribute BasicFileAttributes FileAttribute
            PosixFileAttributes PosixFilePermissions]))

(defn create-private-tree!
  [base]
  (let [cpcache (child-path base ".cpcache")
        compiler-pass (child-path cpcache "compiler-pass")
        root (child-path compiler-pass "v2")
        blobs (child-path root "blobs")
        entries (child-path root "entries")
        receipts (child-path root "receipts")
        locks (child-path root "locks")
        staging (child-path root "staging")]
    ;; The SecureDirectoryStream bootstrap above creates and verifies every
    ;; component.  This retained map is only a path projection; no operation
    ;; opens or mutates cache data through it.
    {:base base :cpcache cpcache :compiler-pass compiler-pass :root root
     :blobs blobs :entries entries :receipts receipts :locks locks
     :staging staging}))

(defn open-secure-child!
  [^SecureDirectoryStream parent child-name identity]
  (let [relative (relative-name! child-name)
        raw (.newDirectoryStream parent
                                 relative
                                 nofollow-links)
        child (require-secure-directory-stream!
               raw :open-secure-store-child)]
    (try
      (verify-secure-directory-handle! child identity)
      child
      (catch Throwable error
        (.close ^DirectoryStream raw)
        (throw error)))))

(defn identity-for-path
  [store path-key]
  (let [path (get store path-key)]
    (or (some #(when (= path (:path %)) %) (:directory-identities store))
        (fail! "C16-POLICY" "store directory identity is missing"
               {:path-key path-key}))))

(defn with-secure-store-directories
  [store operation]
  (verify-store-identities! store)
  (with-open [raw-base (Files/newDirectoryStream ^Path (:base store))]
    (let [base (require-secure-directory-stream!
                raw-base :open-secure-store-base)]
      (verify-secure-directory-handle! base (identity-for-path store :base))
      (with-open [cpcache (open-secure-child!
                           base ".cpcache" (identity-for-path store :cpcache))
                  compiler-pass (open-secure-child!
                                 cpcache "compiler-pass"
                                 (identity-for-path store :compiler-pass))
                  root (open-secure-child!
                        compiler-pass "v2" (identity-for-path store :root))
                  blobs (open-secure-child!
                         root "blobs" (identity-for-path store :blobs))
                  entries (open-secure-child!
                           root "entries" (identity-for-path store :entries))
                  receipts (open-secure-child!
                            root "receipts" (identity-for-path store :receipts))
                  locks (open-secure-child!
                         root "locks" (identity-for-path store :locks))
                  staging (open-secure-child!
                           root "staging" (identity-for-path store :staging))]
        (let [directories {:base base :cpcache cpcache
                           :compiler-pass compiler-pass :root root
                           :blobs blobs :entries entries :receipts receipts
                           :locks locks :staging staging}]
          (operation directories))))))

(defn verify-secure-child-directory!
  [^SecureDirectoryStream parent ^Path relative owned?]
  (let [relative (relative-name! relative)
        {:keys [^BasicFileAttributes basic ^PosixFileAttributes posix]}
        (secure-child-attributes parent relative)
        permissions (.permissions posix)]
    (when-not (and (.isDirectory basic)
                   (not (.isSymbolicLink basic))
                   (= (current-owner-name) (.getName (.owner posix)))
                   (if owned?
                     (= private-directory-permissions permissions)
                     (safe-shared-permissions? permissions)))
      (fail! "C16-POLICY" "cache directory failed descriptor-relative policy"
             {:name (str relative) :owned? owned?})))
  relative)

(defn secure-directory-move!
  [^SecureDirectoryStream source ^Path source-name
   ^SecureDirectoryStream destination ^Path destination-name]
  (let [source-name (relative-name! source-name)
        destination-name (relative-name! destination-name)]
   (when-not (and (= "sun.nio.fs.UnixSecureDirectoryStream"
                    (.getName (class source)))
                 (= "sun.nio.fs.UnixSecureDirectoryStream"
                    (.getName (class destination))))
    (fail! "C16-POLICY" "filesystem provider lacks anchored directory publication"
           {:source-provider (.getName (class source))
            :destination-provider (.getName (class destination))}))
   (.move source source-name destination destination-name)))

(defn secure-ensure-child-directory!
  [^SecureDirectoryStream parent child-name owned?]
  (let [relative (relative-name! child-name)]
    (when-not (secure-child-exists? parent relative)
      (let [temporary (Files/createTempDirectory
                       "gravity-pass-cache-mkdir-"
                       (into-array FileAttribute [private-directory-attribute]))
            source-parent (.getParent temporary)
            source-relative (.getFileName temporary)
            source-parent-identity (identity-of source-parent false)]
        (with-open [raw-source (Files/newDirectoryStream source-parent)]
          (let [source (require-secure-directory-stream!
                        raw-source :cache-directory-bootstrap)]
            (try
              (verify-secure-directory-handle! source source-parent-identity)
              (verify-secure-child-directory! source source-relative true)
              (when-not (secure-child-exists? parent relative)
                (secure-directory-move! source source-relative parent relative))
              (secure-fsync-directory! source)
              (secure-fsync-directory! parent)
              (finally
                (when (secure-child-exists? source source-relative)
                  (.deleteDirectory source source-relative)
                  (secure-fsync-directory! source))))))))
    (verify-secure-child-directory! parent relative owned?)
    (let [raw-child (.newDirectoryStream parent relative nofollow-links)
          child (require-secure-directory-stream!
                 raw-child :cache-directory-bootstrap-child)]
      (try
        child
        (catch Throwable error
          (.close ^DirectoryStream raw-child)
          (throw error))))))

(defn secure-directory-inventory!
  [store path-key ^SecureDirectoryStream directory pattern maximum maximum-bytes]
  (loop [iterator (.iterator directory) count 0 bytes 0 names []]
    (if (.hasNext iterator)
      (let [item (.next iterator)
            name (str (.getFileName ^Path item))
            next-count (inc count)]
        (when (or (> next-count maximum) (not (re-matches pattern name)))
          (fail! "C16-POLICY" "cache directory inventory violates policy"
                 {:directory path-key :name name :maximum maximum}))
        (let [relative (relative-name! name)
              attrs (secure-file-attributes-relative!
                     store path-key directory relative maximum-bytes)
              next-bytes (+ bytes (.size ^BasicFileAttributes (:basic attrs)))]
          (when (> next-bytes maximum-store-bytes)
            (fail! "C16-POLICY" "cache directory exceeds aggregate byte policy"
                   {:directory path-key :maximum-bytes maximum-store-bytes}))
          (recur iterator next-count next-bytes (conj names relative))))
      {:count count :bytes bytes :names names})))

(defn secure-store-inventory!
  [store directories]
  (let [counts {:entries (secure-directory-inventory!
                          store :entries (:entries directories)
                          #"sha256:[0-9a-f]{64}\.edn"
                          maximum-entry-count maximum-entry-bytes)
                :blobs (secure-directory-inventory!
                        store :blobs (:blobs directories)
                        #"sha256:[0-9a-f]{64}\.edn"
                        maximum-blob-count maximum-blob-bytes)
                :receipts (secure-directory-inventory!
                           store :receipts (:receipts directories)
                           #"sha256:[0-9a-f]{64}\.edn"
                           maximum-receipt-count maximum-entry-bytes)
                :locks (secure-directory-inventory!
                        store :locks (:locks directories)
                        #"(?:sha256:[0-9a-f]{64}\.lock|\.store\.lock)"
                        maximum-lock-count 4096)
                :staging (secure-directory-inventory!
                          store :staging (:staging directories)
                          #"\.stage-[0-9a-f-]{36}\.tmp"
                          maximum-staging-count maximum-file-bytes)}
        total (reduce + 0 (map :bytes (vals counts)))]
    (when (> total maximum-store-bytes)
      (fail! "C16-POLICY" "cache store exceeds aggregate byte bound"
             {:maximum-bytes maximum-store-bytes :observed-bytes total}))
    counts))

(defn inventory!
  [store]
  (with-secure-store-directories
   store
   #(secure-store-inventory! store %)))
