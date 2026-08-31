(ns gravity.pass-cache.path-policy
  "No-follow path validation, ownership, and pinned store identities."
  (:require [gravity.pass-cache.policy :refer :all])
  (:import [java.nio.file Files LinkOption Path Paths]
           [java.nio.file.attribute BasicFileAttributes PosixFilePermission
            PosixFilePermissions]))

(declare relative-name!)

(defn child-path
  [^Path parent name]
  (let [name (str (relative-name! name))
        child (.resolve parent name)]
    (when-not (= parent (.getParent child))
      (fail! "C16-POLICY" "cache child escaped its parent"
             {:parent (str parent) :name name}))
    child))

(defn path-parent-segment?
  [^Path path]
  (boolean (some #(= ".." (str %)) (iterator-seq (.iterator path)))))

(defn absolute-base-path!
  [base-path]
  (when (nil? base-path)
    (fail! "C16-POLICY" "cache base path must be explicit" {}))
  (let [raw (Paths/get (str base-path) (make-array String 0))]
    (when (path-parent-segment? raw)
      (fail! "C16-POLICY" "cache base path cannot contain parent traversal"
             {:path (str base-path)}))
    (.normalize (.toAbsolutePath raw))))

(defn relative-name!
  "Return one descriptor-relative name, rejecting paths and dot segments."
  [value]
  (let [^Path relative (if (instance? Path value)
                         value
                         (Paths/get (str value) (make-array String 0)))]
    (when (or (.isAbsolute relative)
              (not= 1 (.getNameCount relative))
              (#{"." ".."} (str relative))
              (not= (str relative) (str (.getFileName relative))))
      (fail! "C16-POLICY" "cache descriptor-relative name is invalid"
             {:name (str value)}))
    relative))

(defn sha-file-name!
  [field id suffix]
  (require-sha256! field id)
  (relative-name! (str id suffix)))

(defn attrs
  [^Path path]
  (try
    (Files/readAttributes path BasicFileAttributes nofollow-links)
    (catch Throwable error
      (if (fatal? error)
        (throw error)
        (fail! "C16-POLICY" "cache path attributes are unavailable"
               {:path (str path) :contained-host-error (.getName (class error))})))))

(defn owner-name
  [^Path path]
  (try
    (.getName (Files/getOwner path nofollow-links))
    (catch Throwable error
      (if (fatal? error)
        (throw error)
        nil))))

(defn current-owner-name [] (System/getProperty "user.name"))

(defn unix-link-count
  [^Path path]
  (try
    (long (Files/getAttribute path "unix:nlink" nofollow-links))
    (catch UnsupportedOperationException error
      (fail! "C16-POLICY" "filesystem provider cannot verify link count"
             {:path (str path) :contained-host-error (.getName (class error))}))
    (catch IllegalArgumentException error
      (fail! "C16-POLICY" "filesystem provider cannot verify link count"
             {:path (str path) :contained-host-error (.getName (class error))}))
    (catch java.io.IOException error
      (fail! "C16-POLICY" "filesystem provider cannot verify link count"
             {:path (str path) :contained-host-error (.getName (class error))}))))

(defn safe-shared-permissions?
  [permissions]
  (and (contains? permissions PosixFilePermission/OWNER_READ)
       (contains? permissions PosixFilePermission/OWNER_WRITE)
       (contains? permissions PosixFilePermission/OWNER_EXECUTE)
       (not (contains? permissions PosixFilePermission/GROUP_WRITE))
       (not (contains? permissions PosixFilePermission/OTHERS_WRITE))))

(defn verify-directory!
  [^Path path owned?]
  (let [attributes (attrs path)
        permissions (Files/getPosixFilePermissions path nofollow-links)]
    (when (or (.isSymbolicLink attributes) (.isOther attributes)
              (not (.isDirectory attributes)))
      (fail! "C16-POLICY" "cache path is not a no-follow directory"
             {:path (str path)}))
    (when-not (= (current-owner-name) (owner-name path))
      (fail! "C16-POLICY" "cache directory is not owned by current user"
             {:path (str path) :owner (owner-name path)}))
    (when-not (if owned?
                (= private-directory-permissions permissions)
                (safe-shared-permissions? permissions))
      (fail! "C16-POLICY" "cache directory permissions are unsafe"
             {:path (str path)
              :observed (PosixFilePermissions/toString permissions)
              :owned? owned?})))
  path)

(defn identity-of
  [^Path path owned?]
  (verify-directory! path owned?)
  (let [a (attrs path)]
    {:path path :owned? owned? :file-key (.fileKey a)
     :owner (owner-name path)
     :permissions (Files/getPosixFilePermissions path nofollow-links)}))

(defn verify-identity!
  [{:keys [^Path path owned? file-key owner permissions]}]
  (verify-directory! path owned?)
  (let [a (attrs path)
        observed {:file-key (.fileKey a)
                  :owner (owner-name path)
                  :permissions (Files/getPosixFilePermissions path nofollow-links)}]
    (when-not (= {:file-key file-key :owner owner :permissions permissions}
                 observed)
      (fail! "C16-POLICY" "cache directory identity changed"
             {:path (str path)})))
  path)

(defn verify-store-identities!
  [store]
  (doseq [identity (:directory-identities store)]
    (verify-identity! identity))
  store)

(defn validate-store!
  [store]
  (when-not (and (map? store)
                 (= local-store-fields (set (keys store)))
                 (= schema-version (:schema-version store))
                 (= store-policy (:store-policy store))
                 (vector? (:directory-identities store)))
    (fail! "C16-POLICY" "local cache store schema is unknown or incomplete" {}))
  (doseq [path-key [:base :cpcache :compiler-pass :root :blobs :entries
                    :receipts :locks :staging]]
    (when-not (instance? Path (get store path-key))
      (fail! "C16-POLICY" "local cache store path projection is malformed"
             {:path-key path-key})))
  (let [base (:base store)
        expected {:cpcache (child-path base ".cpcache")
                  :compiler-pass (child-path (:cpcache store) "compiler-pass")
                  :root (child-path (:compiler-pass store) "v2")
                  :blobs (child-path (:root store) "blobs")
                  :entries (child-path (:root store) "entries")
                  :receipts (child-path (:root store) "receipts")
                  :locks (child-path (:root store) "locks")
                  :staging (child-path (:root store) "staging")}
        expected-identities
        [[base false]
         [(:cpcache store) false]
         [(:compiler-pass store) false]
         [(:root store) true]
         [(:blobs store) true]
         [(:entries store) true]
         [(:receipts store) true]
         [(:locks store) true]
         [(:staging store) true]]
        identities (:directory-identities store)]
    (when-not (= expected-identities
                 (mapv (fn [identity]
                         (when-not (and (map? identity)
                                        (= #{:path :owned? :file-key :owner
                                             :permissions}
                                           (set (keys identity))))
                           (fail! "C16-POLICY"
                                  "local cache directory identity is malformed"
                                  {}))
                         [(:path identity) (:owned? identity)])
                       identities))
      (fail! "C16-POLICY"
             "local cache directory identities are not exact and ordered" {}))
    (doseq [[path-key expected-path] expected]
      (when-not (= expected-path (get store path-key))
        (fail! "C16-POLICY" "local cache store path projection was substituted"
               {:path-key path-key}))))
  (verify-store-identities! store))

;; SecureDirectoryStream is the descriptor-relative filesystem boundary.  A
;; provider that cannot expose secure POSIX/basic views is rejected instead of
;; falling back to path-precheck/open races for cache-owned data.
