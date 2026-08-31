(ns gravity.pass-cache.locking
  "Cross-thread and cross-process bootstrap, store, and per-key locking."
  (:require [gravity.pass-cache.path-policy :refer :all]
            [gravity.pass-cache.policy :refer :all]
            [gravity.pass-cache.secure-io :refer :all]
            [gravity.pass-cache.store-directories :refer :all])
  (:import [java.nio.channels FileChannel FileLock OverlappingFileLockException]
           [java.nio.file LinkOption Path Paths SecureDirectoryStream
           StandardOpenOption]
           [java.nio.file.attribute FileAttribute]
           [java.util HashSet]
           [java.util.concurrent.locks ReentrantLock]))

(defn with-cache-bootstrap-lock
  [^Path base ^SecureDirectoryStream base-directory operation]
  (let [lock-name (relative-name! ".cpcache-bootstrap.lock")
        partial-store {:base base}]
    (.lock store-bootstrap-lock)
    (try
      (when-not (secure-child-exists? base-directory lock-name)
        (try
          (secure-write-new! partial-store :base base-directory lock-name
                             (byte-array 0))
          (catch java.nio.file.FileAlreadyExistsException _ nil)))
      (secure-file-attributes-relative!
       partial-store :base base-directory lock-name 0)
      (let [raw (.newByteChannel base-directory lock-name
                                 (HashSet. [StandardOpenOption/READ
                                            StandardOpenOption/WRITE
                                            LinkOption/NOFOLLOW_LINKS])
                                 (make-array FileAttribute 0))
            channel (require-file-channel! raw "C16-POLICY"
                                           :cache-bootstrap-lock)]
        (with-open [channel channel]
          (let [file-lock (.lock channel)]
            (try (operation)
                 (finally (.release ^FileLock file-lock))))))
      (finally (.unlock store-bootstrap-lock)))))

(defn lock-state
  [path]
  (let [id (str path)]
    (get (swap! in-process-key-locks
                (fn [locks]
                  (if-let [entry (get locks id)]
                    (assoc locks id (update entry :references inc))
                    (assoc locks id {:lock (ReentrantLock.) :references 1}))))
         id)))

(defn release-lock-state!
  [path lock]
  (let [id (str path)]
    (swap! in-process-key-locks
           (fn [locks]
             (if-let [entry (get locks id)]
               (if-not (identical? lock (:lock entry))
                 locks
                 (if (= 1 (:references entry))
                   (dissoc locks id)
                   (assoc locks id (update entry :references dec))))
               locks)))))

(defn with-global-store-lock
  [store thunk]
  (.lock store-bootstrap-lock)
  (try
    (with-secure-store-directories
     store
     (fn [directories]
       (let [^SecureDirectoryStream lock-directory (:locks directories)
             lock-name (relative-name! ".store.lock")]
         (when-not (secure-child-exists? lock-directory lock-name)
           (try
             (secure-write-new! store :locks lock-directory lock-name
                                (byte-array 0))
             (catch java.nio.file.FileAlreadyExistsException _ nil)))
         (secure-file-attributes-relative! store :locks lock-directory lock-name
                                            4096)
         (let [raw (.newByteChannel lock-directory lock-name
                                     (HashSet. [StandardOpenOption/READ
                                                StandardOpenOption/WRITE
                                                LinkOption/NOFOLLOW_LINKS])
                                     (make-array FileAttribute 0))
               channel (require-file-channel! raw "C16-POLICY"
                                              :secure-store-lock)]
           (with-open [channel channel]
             (let [file-lock (.lock channel)]
               (try
                 (binding [*store-lock-held* true]
                   (thunk))
                 (finally (.release ^FileLock file-lock)))))))))
  (finally (.unlock store-bootstrap-lock))))

(defn with-key-lock-held
  [store id thunk]
  (let [relative (sha-file-name! :cache-key-id id ".lock")
        lock-id (str (:locks store) ":" id)
        {:keys [^ReentrantLock lock]} (lock-state lock-id)]
      (.lock lock)
      (try
        (with-secure-store-directories
         store
         (fn [directories]
           (let [^SecureDirectoryStream lock-directory (:locks directories)
                 _ (when-not (secure-child-exists? lock-directory relative)
                     (fail! "C16-POLICY"
                            "cache key lock disappeared before acquisition" {}))
                 _ (secure-file-attributes-relative!
                    store :locks lock-directory relative 4096)
                 raw (.newByteChannel lock-directory relative
                                      (HashSet. [StandardOpenOption/READ
                                                 StandardOpenOption/WRITE
                                                 LinkOption/NOFOLLOW_LINKS])
                                      (make-array FileAttribute 0))
                 channel (require-file-channel! raw "C16-POLICY"
                                                :secure-key-lock)]
             (with-open [channel channel]
               (let [file-lock (.lock channel)]
                 (try
                   (binding [*key-lock-held* true]
                     (thunk))
                   (finally (.release ^FileLock file-lock))))))))
        (finally
          (.unlock lock)
          (release-lock-state! lock-id lock)))))

(defn ensure-key-lock-file!
  [store id]
  (let [relative (sha-file-name! :cache-key-id id ".lock")
        present?
        (with-secure-store-directories
         store
         (fn [directories]
           (let [lock-directory (:locks directories)]
             (when (secure-child-exists? lock-directory relative)
               (secure-file-attributes-relative!
                store :locks lock-directory relative 4096)
               true))))]
    (when-not present?
      (with-global-store-lock
       store
       (fn []
         (with-secure-store-directories
          store
          (fn [directories]
            (let [lock-directory (:locks directories)
                  existing? (secure-child-exists? lock-directory relative)]
              (when (and (not existing?)
                         (>= (get-in (secure-store-inventory!
                                     store directories) [:locks :count])
                             maximum-lock-count))
                (fail! "C16-POLICY" "cache lock admission exceeds its count bound"
                       {:maximum-lock-count maximum-lock-count}))
              (when-not existing?
                (try
                  (secure-write-new! store :locks lock-directory relative
                                     (byte-array 0))
                  (catch java.nio.file.FileAlreadyExistsException _ nil)))
              (secure-file-attributes-relative!
               store :locks lock-directory relative 4096)))))))
    relative))

(defn with-key-lock
  [store id thunk]
  (if *store-lock-held*
    (with-key-lock-held store id thunk)
    (do
      ;; Global admission is released before the per-key lock is acquired, so
      ;; a producer never serializes unrelated keys and no lock inversion is
      ;; possible with the publication gate.
      (ensure-key-lock-file! store id)
      (with-key-lock-held store id thunk))))
