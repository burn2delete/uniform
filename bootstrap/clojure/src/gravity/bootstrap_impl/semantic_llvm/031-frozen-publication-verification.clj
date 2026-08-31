(defn- p15-s23-b3-llvm-expected-sidecar
  [logical-path record]
  (let [bytes
        (.getBytes
         (str (pr-str (c-backend-canonical-value record)) "\n")
         java.nio.charset.StandardCharsets/UTF_8)]
    {:logical-path logical-path
     :bytes bytes
     :hash-record
     {:byte-count (alength ^bytes bytes)
      :content-hash (p15-s23-b3-llvm-sha256-bytes bytes)
      :logical-path logical-path}}))

(defn- p15-s23-b3-llvm-read-published-sidecar!
  [candidate directory expected source-path]
  (let [logical-path (:logical-path expected)
        path (.resolve directory logical-path)
        observed
        (p15-s23-b3-llvm-file-snapshot!
         candidate directory path source-path
         :verify-published-sidecar
         p15-s23-b3-llvm-max-emitted-file-bytes)]
    (when-not
     (and (= (:hash-record expected)
             (assoc (p15-s23-b3-llvm-snapshot-content observed)
                    :logical-path logical-path))
          (java.util.Arrays/equals
           ^bytes (:bytes expected) ^bytes (:bytes observed)))
      (p15-s23-b3-llvm-fail!
       "B13-HASH" source-path {}
       {:missing-fact :exact-final-record-bound-sidecar-bytes
        :logical-path logical-path
        :expected-hash (get-in expected [:hash-record :content-hash])
        :observed-hash (:content-hash observed)}))
    (:hash-record expected)))

(defn- p15-s23-b3-llvm-verify-publication!
  [candidate artifact]
  (p15-s23-b3-llvm-require-authority!
   candidate (get-in artifact [:actual-path-provenance :source])
   :verify-exclusive-publication)
  (let [source-path (get-in artifact [:actual-path-provenance :source])
        publication-path
        (get-in artifact [:actual-path-provenance :publication-path])
        receipt
        (get-in artifact [:actual-path-provenance :publication-receipt])]
    (if-not publication-path
      (do
        (when-not (= {:status :ephemeral-conformance-artifacts
                      :sidecar-hashes {}}
                     receipt)
          (p15-s23-b3-llvm-fail!
           "B3-MANIFEST" source-path artifact
           {:missing-fact :ephemeral-publication-receipt}))
        {:status :passed :publication :ephemeral})
      (let [directory
            (.normalize
             (.toAbsolutePath
              (java.nio.file.Paths/get
               (str publication-path) (make-array String 0))))
            directory-real
            (try
              (.toRealPath directory
                           (make-array java.nio.file.LinkOption 0))
              (catch Exception _ nil))
            parent (.getParent directory)
            directory-attributes
            (java.nio.file.Files/readAttributes
             directory java.nio.file.attribute.BasicFileAttributes
             (into-array java.nio.file.LinkOption
                         [java.nio.file.LinkOption/NOFOLLOW_LINKS]))
            parent-attributes
            (java.nio.file.Files/readAttributes
             parent java.nio.file.attribute.BasicFileAttributes
             (into-array java.nio.file.LinkOption
                         [java.nio.file.LinkOption/NOFOLLOW_LINKS]))
            native-binding
            (p15-s23-b3-llvm-native-publication-preflight!
             candidate source-path)
            expected-publisher-evidence
            (assoc (:evidence native-binding)
                   :commit-primitive :darwin-renamex-np
                   :exclusive-no-clobber? true
                   :no-follow-any? true
                   :parent-file-key-hash
                   (str "sha256:"
                        (sha256-hex (str (.fileKey parent-attributes))))
                   :staging-file-key-hash
                   (str "sha256:"
                        (sha256-hex
                         (str (.fileKey directory-attributes)))))
            expected-names
            #{"program.ll" "program.o" "program"
              "manifest.edn" "provenance.edn" "conformance.edn"}
            observed-names
            (p15-s23-b3-llvm-capped-directory-inventory!
             candidate directory source-path 6)]
        (loop [ancestor directory]
          (when ancestor
            (when (java.nio.file.Files/isSymbolicLink ancestor)
              (p15-s23-b3-llvm-fail!
               "B3-MANIFEST" source-path artifact
               {:missing-fact
                :canonical-non-symlink-published-bundle-path}))
            (recur (.getParent ancestor))))
        (when-not (and (string? publication-path)
                       (= publication-path (.toString directory))
                       directory-real
                       (= directory directory-real)
                       parent
                       (.isDirectory directory-attributes)
                       (.isDirectory parent-attributes)
                       (.fileKey directory-attributes)
                       (.fileKey parent-attributes)
                       (not (java.nio.file.Files/isSymbolicLink directory))
                       (= #{:status :sidecar-hashes :mode-policy
                            :publisher-evidence}
                          (set (keys receipt)))
                       (= expected-publisher-evidence
                          (:publisher-evidence receipt))
                       (= expected-names observed-names))
          (p15-s23-b3-llvm-fail!
           "B3-MANIFEST" source-path artifact
           {:missing-fact
            :exact-published-bundle-inventory-and-publisher-receipt
            :expected-file-count (count expected-names)
            :observed-file-count (count observed-names)}))
        (doseq [[kind logical-path]
                [[:llvm-ir "program.ll"]
                 [:object "program.o"]
                 [:executable "program"]]]
          (let [path (.resolve directory logical-path)
                observed
                (p15-s23-b3-llvm-snapshot-content
                 (p15-s23-b3-llvm-file-snapshot!
                  candidate directory path source-path
                  :verify-published-core-artifact
                  p15-s23-b3-llvm-max-emitted-file-bytes))
                expected (select-keys
                          (get-in artifact
                                  [:b13-record :artifact-files kind])
                          [:byte-count :content-hash])]
            (when-not (= expected observed)
              (p15-s23-b3-llvm-fail!
               "B13-HASH" source-path artifact
               {:missing-fact :published-core-artifact-content
                :logical-path logical-path
                :expected-hash (:content-hash expected)
                :observed-hash (:content-hash observed)}))))
        (when-not
         (and (= {:directory "0755" :executable "0755"
                  :nonexecutable "0644"}
                 (:mode-policy receipt))
              (= p15-s23-b3-llvm-directory-permissions
                 (set (java.nio.file.Files/getPosixFilePermissions
                       directory
                       (into-array java.nio.file.LinkOption
                                   [java.nio.file.LinkOption/NOFOLLOW_LINKS]))))
              (= p15-s23-b3-llvm-executable-permissions
                 (set (java.nio.file.Files/getPosixFilePermissions
                       (.resolve directory "program")
                       (into-array java.nio.file.LinkOption
                                   [java.nio.file.LinkOption/NOFOLLOW_LINKS]))))
              (every?
               (fn [name]
                 (= p15-s23-b3-llvm-nonexecutable-permissions
                    (set (java.nio.file.Files/getPosixFilePermissions
                          (.resolve directory name)
                          (into-array
                           java.nio.file.LinkOption
                           [java.nio.file.LinkOption/NOFOLLOW_LINKS])))))
               ["program.ll" "program.o" "manifest.edn"
                "provenance.edn" "conformance.edn"]))
          (p15-s23-b3-llvm-fail!
           "B3-MANIFEST" source-path artifact
           {:missing-fact :exact-published-bundle-mode-policy}))
        (let [expected-provenance
              (p15-s23-b3-llvm-expected-sidecar
               "provenance.edn"
               (p15-s23-b3-llvm-provenance-sidecar-record artifact))
              expected-conformance
              (p15-s23-b3-llvm-expected-sidecar
               "conformance.edn"
               (p15-s23-b3-llvm-conformance-sidecar-record artifact))
              expected-manifest
              (p15-s23-b3-llvm-expected-sidecar
               "manifest.edn"
               (p15-s23-b3-llvm-manifest-sidecar-record
                artifact (:hash-record expected-provenance)
                (:hash-record expected-conformance)))
              provenance
              (p15-s23-b3-llvm-read-published-sidecar!
               candidate directory expected-provenance source-path)
              conformance
              (p15-s23-b3-llvm-read-published-sidecar!
               candidate directory expected-conformance source-path)
              manifest
              (p15-s23-b3-llvm-read-published-sidecar!
               candidate directory expected-manifest source-path)
              expected-sidecar-hashes
              {:manifest manifest
               :provenance provenance
               :conformance conformance}]
          (when-not
           (and (= :published-atomically-after-final-verification
                   (:status receipt))
                (= expected-sidecar-hashes (:sidecar-hashes receipt)))
            (p15-s23-b3-llvm-fail!
             "B13-HASH" source-path artifact
             {:missing-fact :final-record-bound-published-sidecars}))
          {:status :passed
           :publication :atomically-published
           :core-artifact-count 3
           :sidecar-count 3})))))
