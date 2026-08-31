

(defn- c-backend-read-basic-attributes
  [path source-path target missing-fact]
  (try
    (java.nio.file.Files/readAttributes
     path java.nio.file.attribute.BasicFileAttributes c-backend-no-follow)
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (c-backend-fail!
       "B2-DIALECT" "C backend process operation was interrupted"
       source-path target nil
       {:missing-fact :c-backend-process-interrupted
        :operation missing-fact
        :interrupted? true}))
    (catch Exception error
      (c-backend-fail!
       "B2-DIALECT" "C backend private process staging is unavailable"
       source-path target nil
       {:missing-fact missing-fact
        :cause-message (.getMessage error)}))))

(defn- c-backend-private-staging-directory!
  [source-path target]
  (let [temporary-root
        (.toAbsolutePath
         (.normalize
          (java.nio.file.Paths/get
           (System/getProperty "java.io.tmpdir")
           (make-array String 0))))
        temporary-root-attributes
        (c-backend-read-basic-attributes
         temporary-root source-path target :temporary-root-attributes)]
    (when-not (and (.isDirectory temporary-root-attributes)
                   (not (java.nio.file.Files/isSymbolicLink temporary-root))
                   (some? (.fileKey temporary-root-attributes)))
      (c-backend-fail!
       "B2-DIALECT" "C backend temporary root is not a stable directory"
       source-path target nil
       {:missing-fact :stable-private-process-parent
        :temporary-root (.toString temporary-root)}))
    ;; Hold and validate the parent before creating anything.  Permissions are
    ;; supplied atomically at mkdir time, so unsupported POSIX attributes fail
    ;; without leaving a partially initialized directory behind.
    (let [parent temporary-root
          parent-stream
          (try
            (java.nio.file.Files/newDirectoryStream parent)
            (catch Exception error
              (c-backend-fail!
               "B2-DIALECT"
               "C backend private process parent cannot be held securely"
               source-path target nil
               {:missing-fact :secure-private-process-parent
                :cause-message (.getMessage error)})))]
      (when-not (instance? java.nio.file.SecureDirectoryStream parent-stream)
        (try (.close ^java.io.Closeable parent-stream) (catch Exception _ nil))
        (c-backend-fail!
         "B2-DIALECT" "C backend secure process parent is unavailable"
         source-path target nil
         {:missing-fact :secure-private-process-parent}))
      (let [directory-holder (atom nil)
            created-file-key (atom nil)
            root-stream-holder (atom nil)]
        (try
          (let [directory
                (java.nio.file.Files/createTempDirectory
                 parent ".gravity-c-process-"
                 (into-array
                  java.nio.file.attribute.FileAttribute
                [(java.nio.file.attribute.PosixFilePermissions/asFileAttribute
                    c-backend-private-directory-permissions)]))
                _ (reset! directory-holder directory)
                created-attributes
                (java.nio.file.Files/readAttributes
                 directory java.nio.file.attribute.BasicFileAttributes
                 c-backend-no-follow)
                _ (reset! created-file-key (.fileKey created-attributes))
                ;; Keep the validated diagnostic boundary separate from the
                ;; identity capture so any later initialization failure still
                ;; has enough identity to clean descriptor-relative.
                _ (c-backend-read-basic-attributes
                   directory source-path target
                   :private-process-staging-created-attributes)
                leaf (.getFileName directory)
                root-stream
                (.newDirectoryStream
                 ^java.nio.file.SecureDirectoryStream parent-stream
                 leaf c-backend-no-follow)
                _ (reset! root-stream-holder root-stream)]
            (when-not (instance? java.nio.file.SecureDirectoryStream root-stream)
              (c-backend-fail!
               "B2-DIALECT" "C backend secure process root is unavailable"
               source-path target nil
               {:missing-fact :secure-private-process-root}))
            (let [directory-view
                  (.getFileAttributeView
                   ^java.nio.file.SecureDirectoryStream root-stream
                   java.nio.file.attribute.BasicFileAttributeView)
                  directory-attributes (.readAttributes directory-view)]
              (when-not (and (.isDirectory directory-attributes)
                             (.isDirectory temporary-root-attributes)
                             (not (java.nio.file.Files/isSymbolicLink directory))
                             (not (java.nio.file.Files/isSymbolicLink parent))
                             (some? (.fileKey directory-attributes))
                             (= @created-file-key (.fileKey directory-attributes))
                             (= c-backend-private-directory-permissions
                                (set (java.nio.file.Files/getPosixFilePermissions
                                     directory c-backend-no-follow))))
                (c-backend-fail!
                 "B2-DIALECT"
                 "C backend private process staging identity is invalid"
                 source-path target nil
                 {:missing-fact :stable-private-process-staging-binding}))
              {:path directory
               :parent parent
               :leaf leaf
               :parent-stream parent-stream
               :root-stream root-stream
               :parent-file-key (.fileKey temporary-root-attributes)
               :root-file-key (.fileKey directory-attributes)}))
          (catch Throwable error
            (when-let [root-stream @root-stream-holder]
              (try (.close ^java.io.Closeable root-stream) (catch Exception _ nil)))
            ;; Cleanup is descriptor-relative and identity-checked.  If an
            ;; attacker substituted the leaf, retain it and surface the primary
            ;; failure rather than deleting an unverified directory.
            (when-let [directory @directory-holder]
              (let [leaf (.getFileName ^java.nio.file.Path directory)]
                (try
                  (let [view
                        (.getFileAttributeView
                         ^java.nio.file.SecureDirectoryStream parent-stream
                         leaf java.nio.file.attribute.BasicFileAttributeView
                         c-backend-no-follow)
                        attributes (.readAttributes view)]
                    (when (= @created-file-key (.fileKey attributes))
                      (.deleteDirectory
                       ^java.nio.file.SecureDirectoryStream parent-stream leaf)))
                  (catch Exception cleanup
                    (.addSuppressed ^Throwable error ^Throwable cleanup)))))
            (try (.close ^java.io.Closeable parent-stream)
                 (catch Exception cleanup
                   (.addSuppressed ^Throwable error ^Throwable cleanup)))
            (throw error)))))))

(defn- c-backend-process-descendants
  "Take one bounded descendant snapshot without retaining a handle past the cap.

  The stream is allowed to expose one candidate beyond the bound, but that
  candidate is checked before it can enter the returned vector.  Callers merge
  snapshots through `c-backend-merge-census-handles`, which applies the same
  check to the global set accumulated across process churn."
  [process]
  (let [root (.toHandle process)
        maximum (long *c-backend-process-max-descendants*)]
    (when (neg? maximum)
      (throw (ex-info "negative native process descendant bound"
                      {:maximum maximum})))
    (with-open [stream (.descendants root)]
      (let [iterator (.iterator stream)]
        (loop [handles []
               seen #{}]
          (if-not (.hasNext iterator)
            {:handles handles :overflow? false :snapshot-ok? true}
            (let [handle (.next iterator)
                  pid (.pid ^java.lang.ProcessHandle handle)]
              (cond
                (contains? seen pid)
                (recur handles seen)

                (>= (count seen) maximum)
                ;; Do not retain the over-cap handle.  The global merge helper
                ;; performs the equivalent check for sequential snapshots.
                {:handles handles :overflow? true :snapshot-ok? true}

                :else
                (recur (conj handles handle) (conj seen pid))))))))))

(defn- c-backend-census-merge-ids
  "Pure global-cap check used by the process-handle merge.

  Returning the old retained set on overflow is intentional: a churned
  process cannot cause unbounded retention before the caller fails closed."
  [captured-ids candidate-ids maximum]
  (let [captured-ids (set captured-ids)
        new-ids (vec (remove captured-ids candidate-ids))]
    {:new-ids new-ids
     :retained-ids (if (> (+ (count captured-ids) (count new-ids))
                         (long maximum))
                     captured-ids
                     (into captured-ids new-ids))
     :overflow? (> (+ (count captured-ids) (count new-ids))
                   (long maximum))}))

(defn- c-backend-merge-census-handles
  "Merge HANDLES into CAPTURED only after checking the global unique bound.

  CAPTURED is a PID->ProcessHandle map for descendants only; the process root
  is tracked separately.  On overflow the returned map is unchanged, so a
  churn-heavy process cannot make retention grow without bound before the
  supervision failure is reported."
  [captured handles maximum]
  (let [captured (or captured {})
        candidate-by-id
        (reduce (fn [acc handle]
                  (assoc acc (.pid ^java.lang.ProcessHandle handle) handle))
                {}
                handles)
        {:keys [new-ids overflow?]}
        (c-backend-census-merge-ids (keys captured)
                                    (keys candidate-by-id)
                                    maximum)
        new-handles (select-keys candidate-by-id new-ids)]
    {:captured (if overflow?
                 captured
                 (merge captured new-handles))
     :new-handles new-handles
     :overflow? overflow?}))