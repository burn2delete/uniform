

(defn- c-backend-delete-private-staging!
  [staging source-path target]
  (let [root (:path staging)
        parent (:parent staging)
        parent-stream (:parent-stream staging)
        root-stream (:root-stream staging)
        leaf (:leaf staging)
        primary-failure (atom nil)
        cleanup-failure (atom nil)
        result (atom nil)]
    (try
      (when-not (and (instance? java.nio.file.SecureDirectoryStream parent-stream)
                     (instance? java.nio.file.SecureDirectoryStream root-stream))
        (c-backend-fail!
         "B2-DIALECT" "C backend private process staging handles are unavailable"
         source-path target nil
         {:missing-fact :secure-private-process-staging-handles}))
      (let [parent-before
            (try
              (let [parent-view-before
                    (.getFileAttributeView
                     ^java.nio.file.SecureDirectoryStream parent-stream
                     ^java.nio.file.Path leaf
                     java.nio.file.attribute.BasicFileAttributeView
                     c-backend-no-follow)]
                (.readAttributes parent-view-before))
              (catch Exception error
                (c-backend-fail!
                 "B2-DIALECT"
                 "C backend private process root cannot be rechecked"
                 source-path target nil
                 {:missing-fact :secure-private-process-root-reopen
                  :cause-message (.getMessage error)})))]
        (when-not (= (:root-file-key staging) (.fileKey parent-before))
          (c-backend-fail!
           "B2-DIALECT" "C backend private process root identity changed"
           source-path target nil
           {:missing-fact :stable-private-process-staging-cleanup-identity}))
        (try
          (.close ^java.io.Closeable root-stream)
          (catch Exception error
            (c-backend-fail!
             "B2-DIALECT" "C backend private process root handle close failed"
             source-path target nil
             {:missing-fact :private-process-root-handle-close
              :cause-message (.getMessage error)})))
        (let [cleanup-root-stream
              (try
                (.newDirectoryStream
                 ^java.nio.file.SecureDirectoryStream parent-stream
                 leaf c-backend-no-follow)
                (catch Exception error
                  (c-backend-fail!
                   "B2-DIALECT"
                   "C backend private process root cannot be reopened securely"
                   source-path target nil
                   {:missing-fact :secure-private-process-root-reopen
                    :cause-message (.getMessage error)})))]
          (try
            (let [cleanup-view
                  (.getFileAttributeView
                   ^java.nio.file.SecureDirectoryStream cleanup-root-stream
                   java.nio.file.attribute.BasicFileAttributeView)
                  cleanup-attributes (.readAttributes cleanup-view)
                  paths (vec (iterator-seq
                              (.iterator
                               ^java.nio.file.SecureDirectoryStream
                               cleanup-root-stream)))]
              (when-not (= (:root-file-key staging)
                           (.fileKey cleanup-attributes))
                (c-backend-fail!
                 "B2-DIALECT" "C backend private process root identity changed"
                 source-path target nil
                 {:missing-fact :stable-private-process-staging-cleanup-identity}))
              (when (> (count paths) c-backend-process-max-staging-entries)
                (c-backend-fail!
                 "B2-DIALECT" "C backend private process staging exceeded its bound"
                 source-path target nil
                 {:missing-fact :bounded-private-process-staging-cleanup
                  :maximum-entries c-backend-process-max-staging-entries
                  :observed-entries (count paths)}))
              (doseq [path (sort-by #(.getNameCount ^java.nio.file.Path %) > paths)]
                (let [entry (.getFileName ^java.nio.file.Path path)
                      view
                      (.getFileAttributeView
                       ^java.nio.file.SecureDirectoryStream cleanup-root-stream
                       ^java.nio.file.Path entry
                       java.nio.file.attribute.BasicFileAttributeView
                       c-backend-no-follow)
                      attributes (.readAttributes view)]
                  (when-not (.isRegularFile attributes)
                    (c-backend-fail!
                     "B2-DIALECT"
                     "C backend private process staging contains non-file residue"
                     source-path target nil
                     {:missing-fact :nofollow-private-process-staging-cleanup
                      :path (.toString ^java.nio.file.Path path)}))
                  (try
                    (.deleteFile ^java.nio.file.SecureDirectoryStream
                                 cleanup-root-stream entry)
                    (catch Exception error
                      (c-backend-fail!
                       "B2-DIALECT"
                       "C backend private process staging cleanup failed"
                       source-path target nil
                       {:missing-fact :private-process-staging-delete
                        :path (.toString ^java.nio.file.Path path)
                        :cause-message (.getMessage error)}))))))
            (finally
              (.close ^java.io.Closeable cleanup-root-stream)))))
      (let [recheck-root
            (try
              (.newDirectoryStream
               ^java.nio.file.SecureDirectoryStream parent-stream
               leaf c-backend-no-follow)
              (catch Exception error
                (c-backend-fail!
                 "B2-DIALECT" "C backend private process root cannot be rechecked"
                 source-path target nil
                 {:missing-fact :private-process-root-recheck
                  :cause-message (.getMessage error)})))]
        (try
          (let [recheck-view
                (.getFileAttributeView
                 ^java.nio.file.SecureDirectoryStream recheck-root
                 java.nio.file.attribute.BasicFileAttributeView)
                recheck-attributes (.readAttributes recheck-view)
                remaining (vec (iterator-seq (.iterator recheck-root)))]
            (when-not (and (.isDirectory recheck-attributes)
                           (= (:root-file-key staging)
                              (.fileKey recheck-attributes))
                           (empty? remaining))
              (c-backend-fail!
               "B2-DIALECT" "C backend private process staging left residue"
               source-path target nil
               {:missing-fact :private-process-staging-residue
                :path (.toString root)
                :remaining-entries (mapv str remaining)})))
          (finally
            (.close ^java.io.Closeable recheck-root))))
      (let [parent-view
            (.getFileAttributeView
             ^java.nio.file.SecureDirectoryStream parent-stream
             ^java.nio.file.Path leaf
             java.nio.file.attribute.BasicFileAttributeView
             c-backend-no-follow)
            parent-entry (.readAttributes parent-view)]
        (when-not (and (.isDirectory parent-entry)
                       (not (.isSymbolicLink parent-entry))
                       (= (:root-file-key staging) (.fileKey parent-entry)))
          (c-backend-fail!
           "B2-DIALECT" "C backend private process root identity changed"
           source-path target nil
           {:missing-fact :stable-private-process-staging-cleanup-identity}))
        (try
          (.deleteDirectory ^java.nio.file.SecureDirectoryStream
                            parent-stream leaf)
          (catch Exception error
            (c-backend-fail!
             "B2-DIALECT" "C backend private process root cleanup failed"
             source-path target nil
             {:missing-fact :private-process-root-delete
              :cause-message (.getMessage error)})))
        (when (java.nio.file.Files/exists root c-backend-no-follow)
          (c-backend-fail!
           "B2-DIALECT" "C backend private process staging left residue"
           source-path target nil
           {:missing-fact :private-process-staging-residue
            :path (.toString root)})))
      (reset! result
              {:status :complete
               :cleanup-complete? true
               :residue-possible? false
               :root-removed? true
               :directory (.toString root)
               :parent-file-key-hash
               (str "sha256:" (sha256-hex (str (:parent-file-key staging))))
               :root-file-key-hash
               (str "sha256:" (sha256-hex (str (:root-file-key staging))))})
      (catch Throwable error
        (reset! primary-failure error))
      (finally
        (doseq [stream [root-stream parent-stream]]
          (when stream
            (try
              (.close ^java.io.Closeable stream)
              (catch Throwable error
                (let [wrapped
                      (ex-info
                       "C backend private process staging handle close failed"
                       {:id "B2-DIALECT"
                        :message "C backend private process staging handle close failed"
                        :bootstrap-stage :stage0
                        :backend :c
                        :target target
                        :source-path source-path
                        :missing-fact :private-process-staging-handle-close}
                       error)]
                  (if-let [primary @primary-failure]
                    (.addSuppressed ^Throwable primary ^Throwable wrapped)
                    (if-let [existing @cleanup-failure]
                      (.addSuppressed ^Throwable existing ^Throwable wrapped)
                      (reset! cleanup-failure wrapped))))))))))
    (when-let [error @primary-failure]
      (throw ^Throwable error))
    (when-let [error @cleanup-failure]
      (throw ^Throwable error))
    @result))