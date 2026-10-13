
(defn- cleanup-unpublished!
  [^Path final-root reservation-key created]
  (let [marker (.resolve final-root identity-receipt-name)
        created-names (mapv :name created)
        state (current-directory-state final-root)
        removable?
        (and (not (Files/exists marker no-link-options))
             (= reservation-key (:file-key state))
             (= (set created-names) (set (:names state)))
             (= created-names (subvec evidence-names 0 (count created-names)))
             (every?
              (fn [{:keys [name] expected-file-key :file-key}]
                (let [path (.resolve final-root name)]
                  (and (Files/isRegularFile path no-link-options)
                       (not (Files/isSymbolicLink path))
                       (= expected-file-key
                          (file-key path
                                    "SH07-B51-GIT-PUBLICATION-DELTA")))))
              created))]
    (if-not removable?
      {:outcome :quarantined :foreign-entry true}
      (do
        (doseq [{:keys [name] expected-file-key :file-key} (reverse created)]
          (let [path (.resolve final-root name)
                current (current-directory-state final-root)]
            (when-not (and (= reservation-key (:file-key current))
                           (= expected-file-key
                              (file-key path
                                        "SH07-B51-GIT-PUBLICATION-DELTA")))
              (fail! "SH07-B51-GIT-PUBLICATION-DELTA"
                     :cleanup-race {:path (str path)}))
            (Files/delete path)))
        (when-not (= reservation-key
                     (:file-key (current-directory-state final-root)))
          (fail! "SH07-B51-GIT-PUBLICATION-DELTA" :cleanup-directory-race))
        (Files/delete final-root)
        {:outcome :removed :foreign-entry false}))))

(defn publish-new-evidence!
  "Publish exactly six validated staged payloads and one L-supplied identity
  receipt.  The final directory is reserved with createDirectory, all files use
  CREATE_NEW, and identity-receipt.edn is written last as the commit marker."
  [{:keys [source-worktree publication-worktree content staging-root
           payload-descriptors identity-receipt]}]
  (let [worktrees (validate-publication-worktrees!
                   {:source-worktree source-worktree
                    :publication-worktree publication-worktree
                    :content content})
        publication-root
        (Paths/get (get-in worktrees
                           [:publication-worktree :canonical-path])
                   (make-array String 0))
        stage-root (canonical-directory! staging-root :staging-root
                                         "SH07-B51-GIT-INPUT-BLOB")
        _ (when (or (.startsWith stage-root publication-root)
                    (.startsWith publication-root stage-root))
            (fail! "SH07-B51-GIT-PUBLICATION-DELTA"
                   :staging-inside-worktree))
        staged (validate-staged-payloads!
                {:staging-root stage-root
                 :payload-descriptors payload-descriptors})
        receipt (validate-identity-receipt-envelope! content identity-receipt)
        receipt-bytes (codec/W receipt)
        _ ((:strict-read-wire transport-api) :response receipt-bytes)
        final-root (.resolve publication-root evidence-relative-root)
        final-parent (.getParent final-root)
        _ (when-not (and (Files/isDirectory final-parent no-link-options)
                         (not (Files/isSymbolicLink final-parent)))
            (fail! "SH07-B51-GIT-PUBLICATION-DELTA" :final-parent
                   {:path (str final-parent)}))
        created (atom [])
        reservation-key (atom nil)]
    (try
      ;; This atomic reservation, not the earlier absence check, owns creation.
      (Files/createDirectory
       final-root (make-array java.nio.file.attribute.FileAttribute 0))
      (reset! reservation-key
              (file-key final-root "SH07-B51-GIT-PUBLICATION-DELTA"))
      (doseq [{:keys [name bytes]} staged]
        (let [target (.resolve final-root name)]
          (create-new-bytes! target bytes)
          (swap! created conj
                 {:name name
                  :file-key
                  (file-key target "SH07-B51-GIT-PUBLICATION-DELTA")})))
      ;; The identity receipt is the sole publication commit marker.
      (let [target (.resolve final-root identity-receipt-name)]
        (create-new-bytes! target receipt-bytes)
        (swap! created conj
               {:name identity-receipt-name
                :file-key
                (file-key target "SH07-B51-GIT-PUBLICATION-DELTA")}))
      {:publication
       {:mode :new
        :logical-root evidence-relative-root
        :source-worktree (:source-worktree worktrees)
        :publication-worktree (:publication-worktree worktrees)
        :existing-evidence nil
        :staging-root (str stage-root)
        :reservation-created true
        :reservation-file-key @reservation-key
        :files-created evidence-paths
        :identity-receipt-created-last true
        :final-write-count 7
        :foreign-entry false
        :outcome :staged-new}
       :payload-values (mapv :value staged)
       :identity-receipt receipt
       :authority :none}
      (catch Throwable error
        (let [cleanup
              (if @reservation-key
                (cleanup-unpublished! final-root @reservation-key @created)
                {:outcome :not-reserved :foreign-entry false})]
          (fail! "SH07-B51-GIT-PUBLICATION-DELTA"
                 (if (= :quarantined (:outcome cleanup))
                   :publication-quarantined
                   :publication-failed)
                 {:cause (.getName (class error))
                  :cleanup cleanup
                  :created (mapv :name @created)}))))))
