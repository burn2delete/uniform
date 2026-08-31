(let [p15-s23-b2-c17-gate-b-authority-token (nth __gravity_bootstrap_lexical_values_131964 0)
      p15-s23-b2-c17-gate-b-tool-state (nth __gravity_bootstrap_lexical_values_131964 1)]
(defn- p15-s23-b2-c17-gate-b-toolchain-discovery!
  [candidate workspace source-path expected-workspace-binding]
  (p15-s23-b2-c17-gate-b-require-authority!
   candidate source-path :discover-pinned-c17-toolchain)
  (let [physical-before
        (p15-s23-b2-c17-gate-b-pinned-physical-toolchain!
         candidate source-path)
        run
        (fn [step command diagnostic]
          (when-not (= expected-workspace-binding
                       (p15-s23-b2-c17-gate-b-workspace-binding!
                        candidate workspace source-path))
            (p15-s23-c-backend-fail!
             "B2-MANIFEST" source-path {}
             {:missing-fact :stable-c17-workspace-before-discovery-step
              :tool-step step}))
          (let [result
                (p15-s23-b2-c17-gate-b-run-step!
                 candidate workspace source-path step command 0 diagnostic)]
            (when-not (= expected-workspace-binding
                         (p15-s23-b2-c17-gate-b-workspace-binding!
                          candidate workspace source-path))
              (p15-s23-c-backend-fail!
               "B2-MANIFEST" source-path {}
               {:missing-fact :stable-c17-workspace-after-discovery-step
                :tool-step step}))
            result))
        xcrun (run :xcrun-version
                   ["/usr/bin/xcrun" "--version"] "B2-DIALECT")
        file (run :file-version
                  ["/usr/bin/file" "--version"] "B2-DIALECT")
        clang-path (run :clang-path
                        ["/usr/bin/xcrun" "--find" "clang"]
                        "B2-DIALECT")
        ld-path (run :ld-path
                     ["/usr/bin/xcrun" "--find" "ld"] "B2-ABI")
        otool-path (run :otool-path
                        ["/usr/bin/xcrun" "--find" "otool"] "B2-ABI")
        sdk-path
        (run :sdk-path
             ["/usr/bin/xcrun" "--sdk" "macosx" "--show-sdk-path"]
             "B2-DIALECT")
        sdk-version
        (run :sdk-version
             ["/usr/bin/xcrun" "--sdk" "macosx" "--show-sdk-version"]
             "B2-DIALECT")
        clang-version
        (run :clang-version
             [p15-s23-b2-c17-gate-b-clang-path "--version"]
             "B2-DIALECT")
        clang-target
        (run :clang-target
             [p15-s23-b2-c17-gate-b-clang-path
              "-target" p15-s23-b2-c17-gate-b-target-triple
              "-print-target-triple"]
             "B2-DIALECT")
        clang-default-target
        (run :clang-default-target
             [p15-s23-b2-c17-gate-b-clang-path
              "-print-target-triple"]
             "B2-DIALECT")
        ld-version
        (run :ld-version
             [p15-s23-b2-c17-gate-b-ld-path "-v"] "B2-ABI")
        otool-version
        (run :otool-version
             [p15-s23-b2-c17-gate-b-otool-real-path "--version"]
             "B2-ABI")
        text (fn [step] (str/trim (get-in step [:result :stdout :text])))
        both-text
        (fn [step]
          (str (get-in step [:result :stdout :text])
               (get-in step [:result :stderr :text])))
        sdk-locator
        (java.nio.file.Paths/get
         p15-s23-b2-c17-gate-b-sdk-locator-path (make-array String 0))
        sdk-real
        (try
          (.toRealPath sdk-locator (make-array java.nio.file.LinkOption 0))
          (catch Exception error
            (p15-s23-b2-c17-gate-b-rethrow-interrupt! error)
            nil))]
    (when-not
     (and (= "xcrun version 72." (text xcrun))
          (str/starts-with? (text file) "file-5.41")
          (str/includes? (both-text file)
                         "magic file from /usr/share/file/magic")
          (= p15-s23-b2-c17-gate-b-clang-path (text clang-path))
          (= p15-s23-b2-c17-gate-b-ld-path (text ld-path))
          (= p15-s23-b2-c17-gate-b-otool-path (text otool-path))
          (= p15-s23-b2-c17-gate-b-sdk-locator-path (text sdk-path))
          (= "26.5" (text sdk-version))
          (str/includes? (both-text clang-version)
                         "Apple clang version 21.0.0 (clang-2100.1.1.101)")
          (= p15-s23-b2-c17-gate-b-target-triple (text clang-target))
          (= "arm64-apple-darwin25.5.0" (text clang-default-target))
          (str/includes? (both-text ld-version) "PROJECT:ld-1267")
          (str/includes? (both-text otool-version) "cctools-1040")
          (str/includes? (both-text otool-version) "LLVM version 21.0.0")
          sdk-real
          (= p15-s23-b2-c17-gate-b-sdk-path (.toString sdk-real))
          (= p15-s23-b2-c17-gate-b-file-magic-content
             (select-keys (:file-magic physical-before)
                          [:byte-count :content-hash])))
      (p15-s23-c-backend-fail!
       "B2-DIALECT" source-path {}
       {:missing-fact :pinned-apple-c17-toolchain-identity}))
    (let [physical-after
          (p15-s23-b2-c17-gate-b-pinned-physical-toolchain!
           candidate source-path)
          _
          (when-not (= physical-before physical-after)
            (p15-s23-c-backend-fail!
             "B13-HASH" source-path {}
             {:missing-fact :stable-pinned-c17-toolchain-discovery}))
          records
          (mapv :record
                [xcrun file clang-path ld-path otool-path sdk-path sdk-version
                 clang-version clang-target clang-default-target
                 ld-version otool-version])]
      {:semantic-record
       {:artifact :gravity/b2-c17-toolchain-fingerprint
        :schema-version 1
        :sdk-version "26.5"
        :sdk-policy :macosx-26.5
        :clang-identity "Apple clang 21.0.0 clang-2100.1.1.101"
        :target-triple p15-s23-b2-c17-gate-b-target-triple
        :default-target-triple "arm64-apple-darwin25.5.0"
        :linker-identity :apple-ld-1267
        :xcrun-identity :apple-xcrun-72
        :file-identity :file-5.41
        :file-magic-source :system-file-magic-mgc
        :file-magic-content p15-s23-b2-c17-gate-b-file-magic-content
        :otool-identity :llvm-otool-cctools-1040-llvm-21
        :environment-policy p15-s23-b2-c17-gate-b-environment-policy
        :whole-process-tree-reaping-proved? false}
       :tool-records records
       :physical-record physical-before})))

(def ^:private p15-s23-b2-c17-gate-b-object-load-commands
  ["LC_SEGMENT_64" "LC_BUILD_VERSION" "LC_SYMTAB" "LC_DYSYMTAB"])

(def ^:private p15-s23-b2-c17-gate-b-executable-load-commands
  ["LC_SEGMENT_64" "LC_SEGMENT_64" "LC_SEGMENT_64"
   "LC_DYLD_CHAINED_FIXUPS" "LC_DYLD_EXPORTS_TRIE"
   "LC_SYMTAB" "LC_DYSYMTAB" "LC_LOAD_DYLINKER"
   "LC_UUID" "LC_BUILD_VERSION" "LC_SOURCE_VERSION"
   "LC_MAIN" "LC_LOAD_DYLIB" "LC_FUNCTION_STARTS"
   "LC_DATA_IN_CODE" "LC_CODE_SIGNATURE"])

(defn- p15-s23-b2-c17-gate-b-output-section
  [text start-marker end-marker]
  (let [start (str/index-of text start-marker)
        content-start (when start (+ start (count start-marker)))
        end (when content-start
              (or (when end-marker
                    (str/index-of text end-marker content-start))
                  (count text)))]
    (when (and content-start end (<= content-start end))
      (subs text content-start end))))

(defn- p15-s23-b2-c17-gate-b-workspace-binding!
  [candidate workspace source-path]
  (p15-s23-b2-c17-gate-b-require-authority!
   candidate source-path :bind-private-c17-workspace)
  (let [nofollow
        (into-array java.nio.file.LinkOption
                    [java.nio.file.LinkOption/NOFOLLOW_LINKS])
        root (.normalize (.toAbsolutePath workspace))
        parent (.getParent root)
        root-attributes
        (java.nio.file.Files/readAttributes
         root java.nio.file.attribute.BasicFileAttributes nofollow)
        parent-attributes
        (java.nio.file.Files/readAttributes
         parent java.nio.file.attribute.BasicFileAttributes nofollow)]
    (when-not (and (.isDirectory root-attributes)
                   (.isDirectory parent-attributes)
                   (not (java.nio.file.Files/isSymbolicLink root))
                   (not (java.nio.file.Files/isSymbolicLink parent))
                   (some? (.fileKey root-attributes))
                   (some? (.fileKey parent-attributes)))
      (p15-s23-c-backend-fail!
       "B2-MANIFEST" source-path {}
       {:missing-fact :stable-private-c17-workspace-binding}))
    {:root (.toString root)
     :parent-real (.toString
                   (.toRealPath parent (make-array java.nio.file.LinkOption 0)))
     :root-file-key-hash
     (str "sha256:" (sha256-hex (str (.fileKey root-attributes))))
     :parent-file-key-hash
     (str "sha256:" (sha256-hex (str (.fileKey parent-attributes))))})))
