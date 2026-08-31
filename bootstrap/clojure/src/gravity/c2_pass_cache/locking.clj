;; Semantic C2 pass-cache component loaded by gravity.c2-pass-cache.
;; It deliberately evaluates in the facade namespace to retain private test seams.
(defn- ensure-key-lock-admitted!
  [store key]
  (let [relative (lock-name key)
        existing?
        (with-secure-store-directories
         store
         (fn [directories]
           (let [^SecureDirectoryStream lock-directory (:locks directories)]
             (when (secure-child-exists? lock-directory relative)
               (secure-file-attributes-relative!
                store :locks lock-directory relative 0)
               true))))]
    (when-not existing?
      (with-global-store-lock
       store
       (fn [directories]
         (let [inventory (secure-store-inventory! store directories)
               ^SecureDirectoryStream lock-directory (:locks directories)]
           (when-not (secure-child-exists? lock-directory relative)
             (when (>= (get-in inventory [:locks :count]) maximum-lock-count)
               (cache-fail! "C16-POLICY"
                            "cache per-key lock admission exceeds store policy"
                            {:maximum-lock-count maximum-lock-count}))
             (secure-write-new! store :locks lock-directory relative
                                (byte-array 0)))
           (secure-file-attributes-relative!
            store :locks lock-directory relative 0)
           (let [post (fresh-store-inventory! store)]
             (when-not (<= (get-in post [:locks :count]) maximum-lock-count)
               (cache-fail!
                "C16-POLICY"
                "cache lock inventory exceeds store policy after admission"
                {:maximum-lock-count maximum-lock-count})))))))
    relative))

(defn- with-key-lock
  [store key operation]
  (ensure-key-lock-admitted! store key)
  (let [relative (lock-name key)
        local-path (str (:root store) ":" (:storage-key-id key))
        [local-key local-lock] (acquire-in-process-key-lock! local-path)]
    (try
      (with-secure-store-directories
       store
       (fn [directories]
         (let [^SecureDirectoryStream lock-directory (:locks directories)
               _ (secure-file-attributes-relative!
                  store :locks lock-directory relative 0)
               raw-channel
               (.newByteChannel lock-directory relative
                                (HashSet. [StandardOpenOption/READ
                                           StandardOpenOption/WRITE
                                           LinkOption/NOFOLLOW_LINKS])
                                (make-array FileAttribute 0))
               channel (require-file-channel! raw-channel "C16-POLICY"
                                              :secure-key-lock)]
           (with-open [channel channel
                       lock (.lock channel)]
             (secure-file-attributes-relative!
              store :locks lock-directory relative 0)
             (operation directories)))))
      (finally
        (release-in-process-key-lock! local-key local-lock)))))
