(defn- p15-s23-b3-llvm-publish-final!
  [candidate artifact payload preflight finalize!]
  (p15-s23-b3-llvm-require-authority!
   candidate (get-in artifact [:actual-path-provenance :source])
   :prepare-final-verified-publication)
  (if-not preflight
    (finalize!
     {:status :ephemeral-conformance-artifacts
      :actual-output-directory nil
      :sidecar-hashes {}})
    (let [source-path (get-in artifact [:actual-path-provenance :source])
          workspace
          (java.nio.file.Files/createTempDirectory
           (:parent preflight) ".gravity-b3-final-publish-"
           (make-array java.nio.file.attribute.FileAttribute 0))]
      (try
        (doseq [[kind name]
                [[:llvm-ir "program.ll"]
                 [:object "program.o"]
                 [:executable "program"]]]
          (let [bytes (get payload kind)
                root (.normalize (.toAbsolutePath workspace))
                path (.normalize (.toAbsolutePath (.resolve workspace name)))
                expected (get-in artifact
                                 [:b13-record :artifact-files kind
                                  :content-hash])]
            (when-not (and (= root (.getParent path))
                           (contains? #{"program.ll" "program.o" "program"}
                                      name)
                           (not (java.nio.file.Files/exists
                                 path (make-array java.nio.file.LinkOption 0)))
                           (bytes? bytes)
                           (<= (alength ^bytes bytes)
                               p15-s23-b3-llvm-max-emitted-file-bytes)
                           (= expected
                              (p15-s23-b3-llvm-sha256-bytes bytes)))
              (p15-s23-b3-llvm-fail!
               "B13-HASH" source-path {}
               {:missing-fact :buffered-artifact-hash-before-publication
                :expected-hash expected
                :maximum-byte-count
                p15-s23-b3-llvm-max-emitted-file-bytes
                :observed-byte-count
                (when (bytes? bytes) (alength ^bytes bytes))
                :observed-hash
                (when (bytes? bytes)
                  (p15-s23-b3-llvm-sha256-bytes bytes))}))
            (java.nio.file.Files/write
             path bytes
             (into-array java.nio.file.OpenOption
                         [java.nio.file.StandardOpenOption/CREATE_NEW
                          java.nio.file.StandardOpenOption/WRITE]))
            (let [staged
                  (p15-s23-b3-llvm-file-snapshot!
                   candidate workspace path source-path
                   :verify-private-publication-core-artifact
                   p15-s23-b3-llvm-max-emitted-file-bytes)]
              (when-not (and (= expected (:content-hash staged))
                             (= (alength ^bytes bytes)
                                (:byte-count staged)))
                (p15-s23-b3-llvm-fail!
                 "B13-HASH" source-path {}
                 {:missing-fact :staged-artifact-hash-before-publication
                  :logical-path name
                  :expected-hash expected
                  :observed-hash (:content-hash staged)})))))
        (java.nio.file.Files/setPosixFilePermissions
         (.resolve workspace "program")
         #{java.nio.file.attribute.PosixFilePermission/OWNER_READ
           java.nio.file.attribute.PosixFilePermission/OWNER_WRITE
           java.nio.file.attribute.PosixFilePermission/OWNER_EXECUTE
           java.nio.file.attribute.PosixFilePermission/GROUP_READ
           java.nio.file.attribute.PosixFilePermission/GROUP_EXECUTE
           java.nio.file.attribute.PosixFilePermission/OTHERS_READ
           java.nio.file.attribute.PosixFilePermission/OTHERS_EXECUTE})
        (let [provenance
              (p15-s23-b3-llvm-provenance-sidecar-record artifact)
              conformance
              (p15-s23-b3-llvm-conformance-sidecar-record artifact)
              provenance-hash
              (p15-s23-b3-llvm-write-edn-file!
               candidate workspace "provenance.edn" provenance source-path)
              conformance-hash
              (p15-s23-b3-llvm-write-edn-file!
               candidate workspace "conformance.edn" conformance source-path)
              manifest
              (p15-s23-b3-llvm-manifest-sidecar-record
               artifact provenance-hash conformance-hash)
              manifest-hash
              (p15-s23-b3-llvm-write-edn-file!
               candidate workspace "manifest.edn" manifest source-path)
              _
              (do
                (java.nio.file.Files/setPosixFilePermissions
                 workspace p15-s23-b3-llvm-directory-permissions)
                (java.nio.file.Files/setPosixFilePermissions
                 (.resolve workspace "program")
                 p15-s23-b3-llvm-executable-permissions)
                (doseq [name ["program.ll" "program.o" "manifest.edn"
                              "provenance.edn" "conformance.edn"]]
                  (java.nio.file.Files/setPosixFilePermissions
                   (.resolve workspace name)
                   p15-s23-b3-llvm-nonexecutable-permissions)))
              hashes
              {:manifest manifest-hash
               :provenance provenance-hash
               :conformance conformance-hash}
              staging-attributes
              (java.nio.file.Files/readAttributes
               workspace java.nio.file.attribute.BasicFileAttributes
               (into-array java.nio.file.LinkOption
                           [java.nio.file.LinkOption/NOFOLLOW_LINKS]))
              staging-file-key (.fileKey staging-attributes)
              _
              (when-not (and (.isDirectory staging-attributes)
                             staging-file-key)
                (p15-s23-b3-llvm-fail!
                 "B3-MANIFEST" source-path artifact
                 {:missing-fact
                  :exclusive-publication-staging-file-key}))
              publisher-evidence
              (assoc (get-in preflight [:native-binding :evidence])
                     :commit-primitive :darwin-renamex-np
                     :exclusive-no-clobber? true
                     :no-follow-any? true
                     :parent-file-key-hash
                     (:parent-file-key-hash preflight)
                     :staging-file-key-hash
                     (str "sha256:"
                          (sha256-hex (str staging-file-key))))
              receipt
              {:status :published-atomically-after-final-verification
               :actual-output-directory
               (.toString ^java.nio.file.Path (:destination preflight))
               :sidecar-hashes hashes
               :publisher-evidence publisher-evidence
               :mode-policy
               {:directory "0755" :executable "0755"
                :nonexecutable "0644"}}
              published (finalize! receipt)]
          (when-not
           (and (= provenance
                   (p15-s23-b3-llvm-provenance-sidecar-record published))
                (= conformance
                   (p15-s23-b3-llvm-conformance-sidecar-record published))
                (= manifest
                   (p15-s23-b3-llvm-manifest-sidecar-record
                    published provenance-hash conformance-hash)))
            (p15-s23-b3-llvm-fail!
             "B13-HASH" source-path published
             {:missing-fact :precommit-final-record-sidecar-parity}))
          ;; `published` is fully constructed and verified by `finalize!`.
          ;; The exclusive native rename is the last success-path operation.
          (p15-s23-b3-llvm-publish!
           candidate workspace preflight source-path published))
        (catch Throwable error
          (try
            (p15-s23-b3-llvm-delete-tree!
             candidate workspace source-path)
            (catch Throwable cleanup
              (cond
                (instance? Error error)
                (.addSuppressed error cleanup)

                (instance? Error cleanup)
                (do (.addSuppressed ^Throwable cleanup error)
                    (throw cleanup))

                :else
                (.addSuppressed error cleanup))))
          (throw error))))))
