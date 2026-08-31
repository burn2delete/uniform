(defn- p15-s23-b3-llvm-toolchain-preflight!
  [candidate workspace source-path]
  (p15-s23-b3-llvm-require-authority!
   candidate source-path :pinned-toolchain-preflight)
  (p15-s23-b3-llvm-fail!
   "B3-TARGET" source-path {}
   {:missing-fact :legacy-host-toolchain-route-disabled})
  (let [xcrun-version-step
        (p15-s23-b3-llvm-run-step!
         candidate workspace source-path :xcrun-version
         ["/usr/bin/xcrun" "--version"])
        file-version-step
        (p15-s23-b3-llvm-run-step!
         candidate workspace source-path :file-version
         ["/usr/bin/file" "--version"])
        clang-path-step
        (p15-s23-b3-llvm-run-step!
         candidate workspace source-path :clang-path
         ["/usr/bin/xcrun" "--find" "clang"])
        ld-path-step
        (p15-s23-b3-llvm-run-step!
         candidate workspace source-path :ld-path
         ["/usr/bin/xcrun" "--find" "ld"])
        otool-path-step
        (p15-s23-b3-llvm-run-step!
         candidate workspace source-path :otool-path
         ["/usr/bin/xcrun" "--find" "otool"])
        sdk-path-step
        (p15-s23-b3-llvm-run-step!
         candidate workspace source-path :sdk-path
         ["/usr/bin/xcrun" "--sdk" "macosx" "--show-sdk-path"])
        sdk-version-step
        (p15-s23-b3-llvm-run-step!
         candidate workspace source-path :sdk-version
         ["/usr/bin/xcrun" "--sdk" "macosx" "--show-sdk-version"])
        clang-path-text
        (str/trim (get-in clang-path-step [:result :stdout :text]))
        ld-path-text
        (str/trim (get-in ld-path-step [:result :stdout :text]))
        otool-path-text
        (str/trim (get-in otool-path-step [:result :stdout :text]))
        clang-path
        (java.nio.file.Paths/get clang-path-text (make-array String 0))
        ld-path
        (java.nio.file.Paths/get ld-path-text (make-array String 0))
        otool-path
        (java.nio.file.Paths/get otool-path-text (make-array String 0))
        clang-real-path
        (.toRealPath clang-path
                     (make-array java.nio.file.LinkOption 0))
        ld-real-path
        (.toRealPath ld-path
                     (make-array java.nio.file.LinkOption 0))
        otool-real-path
        (.toRealPath otool-path
                     (make-array java.nio.file.LinkOption 0))
        clang-effective (.toString clang-real-path)
        ld-effective (.toString ld-real-path)
        otool-effective (.toString otool-real-path)
        otool-version-step
        (p15-s23-b3-llvm-run-step!
         candidate workspace source-path :otool-version
         [otool-effective "--version"])
        clang-version-step
        (p15-s23-b3-llvm-run-step!
         candidate workspace source-path :clang-version
         [clang-effective "--version"])
        target-triple-step
        (p15-s23-b3-llvm-run-step!
         candidate workspace source-path :clang-target-triple
         [clang-effective "-target"
          (:target-triple p15-s23-b3-llvm-policy)
          "-print-target-triple"])
        default-target-step
        (p15-s23-b3-llvm-run-step!
         candidate workspace source-path :clang-default-target
         [clang-effective "-print-target-triple"])
        linker-version-step
        (p15-s23-b3-llvm-run-step!
         candidate workspace source-path :linker-version
         [ld-effective "-v"])
        sdk-path-text
        (str/trim (get-in sdk-path-step [:result :stdout :text]))
        sdk-version
        (str/trim (get-in sdk-version-step [:result :stdout :text]))
        observed-target-triple
        (str/trim (get-in target-triple-step [:result :stdout :text]))
        default-target-triple
        (str/trim (get-in default-target-step [:result :stdout :text]))
        clang-version-text
        (get-in clang-version-step [:result :stdout :text])
        linker-version-text
        (str (get-in linker-version-step [:result :stdout :text])
             (get-in linker-version-step [:result :stderr :text]))
        xcrun-version-text
        (str/trim (get-in xcrun-version-step [:result :stdout :text]))
        file-version-text
        (get-in file-version-step [:result :stdout :text])
        otool-version-text
        (str (get-in otool-version-step [:result :stdout :text])
             (get-in otool-version-step [:result :stderr :text]))
        sdk-path
        (java.nio.file.Paths/get sdk-path-text (make-array String 0))
        sdk-real-path
        (.toRealPath sdk-path (make-array java.nio.file.LinkOption 0))
        sdk-real-path-text (.toString sdk-real-path)
        xcrun-path
        (.toRealPath
         (java.nio.file.Paths/get "/usr/bin/xcrun" (make-array String 0))
         (into-array java.nio.file.LinkOption
                     [java.nio.file.LinkOption/NOFOLLOW_LINKS]))
        file-path
        (.toRealPath
         (java.nio.file.Paths/get "/usr/bin/file" (make-array String 0))
         (into-array java.nio.file.LinkOption
                     [java.nio.file.LinkOption/NOFOLLOW_LINKS]))
        magic-path
        (java.nio.file.Paths/get "/usr/share/file/magic.mgc"
                                 (make-array String 0))
        magic-snapshot
        (p15-s23-b3-llvm-file-snapshot!
         candidate (.getParent magic-path) magic-path source-path
         :bind-file-magic-database
         p15-s23-b3-llvm-max-emitted-file-bytes)]
    (when-not
     (and (str/starts-with? sdk-path-text "/")
          (java.nio.file.Files/isDirectory
           sdk-path (make-array java.nio.file.LinkOption 0))
          (str/starts-with? clang-path-text "/")
          (java.nio.file.Files/isRegularFile
           clang-path (make-array java.nio.file.LinkOption 0))
          (= "26.5" sdk-version)
          (= "xcrun version 72." xcrun-version-text)
          (and (str/includes? file-version-text "file-5.41")
               (str/includes? file-version-text
                              "magic file from /usr/share/file/magic"))
          (and (str/includes? otool-version-text
                             "Apple Inc. version cctools-1040")
               (str/includes? otool-version-text
                              "LLVM version 21.0.0"))
          (str/ends-with? sdk-real-path-text "/MacOSX26.5.sdk")
          (str/includes?
           clang-version-text
           "Apple clang version 21.0.0 (clang-2100.1.1.101)")
          (= "arm64-apple-darwin25.5.0" default-target-triple)
          (str/includes? linker-version-text "ld-1267")
          (= (:target-triple p15-s23-b3-llvm-policy)
             observed-target-triple)
          (= p15-s23-b3-llvm-expected-file-magic-content
             (p15-s23-b3-llvm-snapshot-content magic-snapshot)))
      (p15-s23-b3-llvm-fail!
       "B3-TARGET" source-path {}
       {:missing-fact :resolved-bounded-xcode-clang-sdk-toolchain}))
    {:sdk-actual-path sdk-real-path-text
     :sdk-locator-path sdk-path-text
     :clang-actual-path clang-effective
     :ld-actual-path ld-effective
     :otool-actual-path otool-effective
     :file-actual-path (.toString file-path)
     :semantic-record
     {:artifact :gravity/b3-llvm-toolchain-fingerprint
      :sdk-version sdk-version
      :sdk-policy :macosx-26.5
      :clang-identity "Apple clang 21.0.0 clang-2100.1.1.101"
      :default-target-triple default-target-triple
      :linker-identity :apple-ld-1267
      :xcrun-identity :apple-xcrun-72
      :file-identity :file-5.41
      :file-magic-source :system-file-magic-mgc
      :file-magic-content
      (p15-s23-b3-llvm-snapshot-content magic-snapshot)
      :otool-identity :llvm-otool-cctools-1040-llvm-21
      :clang-version-normalized-fingerprint
      (select-keys (:record clang-version-step)
                   [:stdout-hash :stderr-hash
                    :stdout-byte-count :stderr-byte-count])
      :linker-version-normalized-fingerprint
      {:stdout-hash (get-in linker-version-step [:record :stdout-hash])
       :stderr-hash (get-in linker-version-step [:record :stderr-hash])
       :stdout-byte-count
       (get-in linker-version-step [:record :stdout-byte-count])
       :stderr-byte-count
       (get-in linker-version-step [:record :stderr-byte-count])}
      :verification-tool-fingerprints
      {:xcrun (select-keys (:record xcrun-version-step)
                           [:stdout-hash :stderr-hash
                            :stdout-byte-count :stderr-byte-count])
       :file (select-keys (:record file-version-step)
                         [:stdout-hash :stderr-hash
                          :stdout-byte-count :stderr-byte-count])
       :otool (select-keys (:record otool-version-step)
                          [:stdout-hash :stderr-hash
                           :stdout-byte-count :stderr-byte-count])}
      :observed-target-triple observed-target-triple
      :target-policy-source :pinned-not-host-inferred
      :observed-environment-only? true}
     :tool-records
     (mapv :record
      [xcrun-version-step file-version-step
       clang-path-step ld-path-step otool-path-step
       otool-version-step sdk-path-step sdk-version-step clang-version-step
       target-triple-step default-target-step linker-version-step])
     :physical-record
     {:xcrun-path (.toString xcrun-path)
      :file-path (.toString file-path)
      :magic-path (.toString (.toAbsolutePath magic-path))
      :magic-file-key-hash (:file-key-hash magic-snapshot)
      :magic-last-modified-millis (:last-modified-millis magic-snapshot)
      :sdk-locator-path sdk-path-text
      :sdk-effective-path sdk-real-path-text
      :clang-locator-path clang-path-text
      :clang-effective-path clang-effective
      :ld-locator-path ld-path-text
      :ld-effective-path ld-effective
      :otool-locator-path otool-path-text
      :otool-effective-path otool-effective
      :locator-output-hashes {:sdk (get-in sdk-path-step [:result :stdout :hash])
       :clang (get-in clang-path-step [:result :stdout :hash])
       :ld (get-in ld-path-step [:result :stdout :hash])
       :otool (get-in otool-path-step [:result :stdout :hash])}}}))
