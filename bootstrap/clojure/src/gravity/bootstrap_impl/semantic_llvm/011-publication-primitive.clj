(defn- p15-s23-b3-llvm-publish!
  [candidate workspace preflight source-path committed-result]
  (p15-s23-b3-llvm-require-authority!
   candidate source-path :atomic-publish-verified-staging)
  (let [destination (:destination preflight)
        parent (:parent preflight)
        workspace (.normalize (.toAbsolutePath workspace))
        expected-names
        #{"program.ll" "program.o" "program"
          "manifest.edn" "provenance.edn" "conformance.edn"}
        publication-receipt
        (get-in committed-result
                [:actual-path-provenance :publication-receipt])
        expected-hashes
        {"program.ll"
         (select-keys
          (get-in committed-result
                  [:b13-record :artifact-files :llvm-ir])
          [:byte-count :content-hash])
         "program.o"
         (select-keys
          (get-in committed-result
                  [:b13-record :artifact-files :object])
          [:byte-count :content-hash])
         "program"
         (select-keys
          (get-in committed-result
                  [:b13-record :artifact-files :executable])
          [:byte-count :content-hash])
         "manifest.edn"
         (select-keys (get-in publication-receipt
                              [:sidecar-hashes :manifest])
                      [:byte-count :content-hash])
         "provenance.edn"
         (select-keys (get-in publication-receipt
                              [:sidecar-hashes :provenance])
                      [:byte-count :content-hash])
         "conformance.edn"
         (select-keys (get-in publication-receipt
                              [:sidecar-hashes :conformance])
                      [:byte-count :content-hash])}]
    (loop [ancestor parent]
      (when ancestor
        (when (java.nio.file.Files/isSymbolicLink ancestor)
          (p15-s23-b3-llvm-fail!
           "B3-MANIFEST" source-path {}
           {:missing-fact :non-symlink-output-ancestor}))
        (recur (.getParent ancestor))))
    (when-not (and parent
                   (java.nio.file.Files/isDirectory
                    parent (make-array java.nio.file.LinkOption 0))
                   (not (java.nio.file.Files/isSymbolicLink parent))
                   (not (java.nio.file.Files/exists
                         destination (make-array java.nio.file.LinkOption 0))))
      (p15-s23-b3-llvm-fail!
       "B3-MANIFEST" source-path {}
       {:missing-fact :collision-free-regular-output-directory
        :output-collision?
        (boolean
         (and destination
              (java.nio.file.Files/exists
               destination
               (into-array java.nio.file.LinkOption
                           [java.nio.file.LinkOption/NOFOLLOW_LINKS]))))}))
    (let [parent-real
          (.toRealPath parent (make-array java.nio.file.LinkOption 0))
          workspace-parent-real
          (.toRealPath (.getParent workspace)
                       (make-array java.nio.file.LinkOption 0))
          observed-names
          (p15-s23-b3-llvm-capped-directory-inventory!
           candidate workspace source-path 6)]
      (when-not (and (= parent-real workspace-parent-real)
                     (java.nio.file.Files/isDirectory
                      workspace (make-array java.nio.file.LinkOption 0))
                     (not (java.nio.file.Files/isSymbolicLink workspace))
                     (= expected-names observed-names))
        (p15-s23-b3-llvm-fail!
         "B3-MANIFEST" source-path {}
         {:missing-fact :complete-same-filesystem-publication-staging
          :expected-file-count (count expected-names)
          :observed-file-count (count observed-names)}))
      (doseq [name expected-names]
        (let [path (.resolve workspace name)]
          (when-not (= (get expected-hashes name)
                       (p15-s23-b3-llvm-snapshot-content
                        (p15-s23-b3-llvm-file-snapshot!
                         candidate workspace path source-path
                         :final-precommit-staging-snapshot
                         p15-s23-b3-llvm-max-emitted-file-bytes)))
            (p15-s23-b3-llvm-fail!
             "B13-HASH" source-path committed-result
             {:missing-fact :final-precommit-staging-content
              :logical-path name}))))
      (when-not
       (and (= p15-s23-b3-llvm-directory-permissions
               (set (java.nio.file.Files/getPosixFilePermissions
                     workspace
                     (into-array java.nio.file.LinkOption
                                 [java.nio.file.LinkOption/NOFOLLOW_LINKS]))))
            (= p15-s23-b3-llvm-executable-permissions
               (set (java.nio.file.Files/getPosixFilePermissions
                     (.resolve workspace "program")
                     (into-array java.nio.file.LinkOption
                                 [java.nio.file.LinkOption/NOFOLLOW_LINKS]))))
            (every?
             (fn [name]
               (= p15-s23-b3-llvm-nonexecutable-permissions
                  (set (java.nio.file.Files/getPosixFilePermissions
                        (.resolve workspace name)
                        (into-array java.nio.file.LinkOption
                                    [java.nio.file.LinkOption/NOFOLLOW_LINKS])))))
             ["program.ll" "program.o" "manifest.edn"
              "provenance.edn" "conformance.edn"]))
        (p15-s23-b3-llvm-fail!
         "B3-MANIFEST" source-path {}
         {:missing-fact :exact-publication-mode-policy}))
      (when-not (= parent-real
                   (.toRealPath parent
                                (make-array java.nio.file.LinkOption 0)))
        (p15-s23-b3-llvm-fail!
         "B3-MANIFEST" source-path {}
         {:missing-fact :stable-real-output-parent-before-atomic-move}))
      ;; Construct every fallible binding before the exclusive native commit.
      ;; The rc=0 branch returns immediately: no filesystem access, Arena close,
      ;; or cleanup operation follows successful publication.
      (let [receipt committed-result
            current-parent-attributes
            (java.nio.file.Files/readAttributes
             parent java.nio.file.attribute.BasicFileAttributes
             (into-array java.nio.file.LinkOption
                         [java.nio.file.LinkOption/NOFOLLOW_LINKS]))
            _
            (when-not (and (.isDirectory current-parent-attributes)
                           (= (:parent-file-key preflight)
                              (.fileKey current-parent-attributes)))
              (p15-s23-b3-llvm-fail!
               "B3-MANIFEST" source-path committed-result
               {:missing-fact
                :stable-file-key-output-parent-before-exclusive-rename}))
            staging-attributes
            (java.nio.file.Files/readAttributes
             workspace java.nio.file.attribute.BasicFileAttributes
             (into-array java.nio.file.LinkOption
                         [java.nio.file.LinkOption/NOFOLLOW_LINKS]))
            staging-file-key (.fileKey staging-attributes)
            _
            (when-not staging-file-key
              (p15-s23-b3-llvm-fail!
               "B3-MANIFEST" source-path committed-result
               {:missing-fact :exclusive-rename-staging-file-key}))
            staging-file-key-hash
            (str "sha256:" (sha256-hex (str staging-file-key)))
            native-binding (:native-binding preflight)
            native-runtime (:runtime native-binding)
            handle (:handle native-runtime)
            state-layout (:state-layout native-runtime)
            errno-handle (:errno-handle native-runtime)
            expected-publisher-evidence
            (assoc (:evidence native-binding)
                   :commit-primitive :darwin-renamex-np
                   :exclusive-no-clobber? true
                   :no-follow-any? true
                   :parent-file-key-hash
                   (:parent-file-key-hash preflight)
                   :staging-file-key-hash staging-file-key-hash)
            _
            (when-not (= expected-publisher-evidence
                         (get-in committed-result
                                 [:actual-path-provenance
                                  :publication-receipt
                                  :publisher-evidence]))
              (p15-s23-b3-llvm-fail!
               "B3-MANIFEST" source-path committed-result
               {:missing-fact :content-bound-native-publisher-evidence}))
            arena (java.lang.foreign.Arena/ofAuto)
            state-segment (.allocate arena state-layout)
            source-segment (.allocateFrom arena (.toString workspace))
            destination-segment
            (.allocateFrom arena (.toString destination))
            flags (int (get-in native-binding
                               [:evidence :flags :combined]))
            return-code
            (int (.invokeWithArguments
                  handle
                  (object-array
                   [state-segment source-segment destination-segment flags])))]
        (if (zero? return-code)
          receipt
          (let [captured-errno
                (int (.invokeWithArguments
                      errno-handle
                      (object-array [state-segment (long 0)])))
                failure-source-attributes
                (try
                  (java.nio.file.Files/readAttributes
                   workspace java.nio.file.attribute.BasicFileAttributes
                   (into-array
                    java.nio.file.LinkOption
                    [java.nio.file.LinkOption/NOFOLLOW_LINKS]))
                  (catch Exception _ nil))]
            (when-not (and staging-file-key failure-source-attributes
                           (.isDirectory failure-source-attributes)
                           (= staging-file-key
                              (.fileKey failure-source-attributes)))
              (p15-s23-b3-llvm-fail!
               "B3-MANIFEST" source-path committed-result
               {:missing-fact :exclusive-rename-failure-source-preservation
                :rename-return-code return-code
                :captured-errno captured-errno}))
            (p15-s23-b3-llvm-fail!
             "B3-MANIFEST" source-path committed-result
             {:missing-fact :exclusive-no-clobber-publication
              :output-collision? (= 17 captured-errno)
              :rename-return-code return-code
              :captured-errno captured-errno})))))))
