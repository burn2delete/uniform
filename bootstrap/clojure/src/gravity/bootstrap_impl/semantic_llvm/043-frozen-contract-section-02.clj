(defn-
 semantic-llvm-frozen-contract-section-02
 [artifact state]
 (let
  [{:keys
    [selection
     profile
     target
     b3-target
     b13-target
     build-target
     abi
     b3-abi
     runtime
     providers
     pass-record
     contract-bindings
     bindings
     files
     metadata-free-files
     build-id
     build-identity
     b14-scope
     c18
     expected-bridge-report
     expected-build-providers
     expected-dependencies
     expected-artifact-kinds
     source-rule
     toolchain-fingerprint
     toolchain-static
     tool-records
     tool-record-by-step
     expected-command-contracts
     normalized-fingerprint?
     source-inputs
     toolchain-digest
     c13-pass-provenance
     pass-pipeline-base
     pass-pipeline-digest
     expected-compiler-provenance
     expected-artifact-graph
     actual-path-provenance
     actual-path-base-keys
     publication-path
     publication-receipt
     physical-record
     retentions
     sha256-value?
     absolute-path?]}
   state]
  (and
   (every?
    absolute-path?
    (map
     physical-record
     [:xcrun-path
      :file-path
      :magic-path
      :sdk-locator-path
      :sdk-effective-path
      :clang-locator-path
      :clang-effective-path
      :ld-locator-path
      :ld-effective-path
      :otool-locator-path
      :otool-effective-path]))
   (= "/usr/bin/xcrun" (:xcrun-path physical-record))
   (= "/usr/bin/file" (:file-path physical-record))
   (= "/usr/share/file/magic.mgc" (:magic-path physical-record))
   (str/ends-with?
    (:sdk-effective-path physical-record)
    "/MacOSX26.5.sdk")
   (str/ends-with?
    (:clang-effective-path physical-record)
    "/usr/bin/clang")
   (str/ends-with? (:ld-effective-path physical-record) "/usr/bin/ld")
   (str/ends-with?
    (:otool-effective-path physical-record)
    "/usr/bin/llvm-otool")
   (sha256-value? (:magic-file-key-hash physical-record))
   (and
    (integer? (:magic-last-modified-millis physical-record))
    (<= 0 (:magic-last-modified-millis physical-record)))
   (=
    #{:sdk :ld :clang :otool}
    (set (keys (:locator-output-hashes physical-record))))
   (every?
    sha256-value?
    (vals (:locator-output-hashes physical-record)))
   (or
    (and
     (not (contains? actual-path-provenance :publication-receipt))
     (nil? publication-path))
    (and
     (nil? publication-path)
     (=
      {:status :ephemeral-conformance-artifacts, :sidecar-hashes {}}
      publication-receipt)
     (= #{:ephemeral-conformance-intent} retentions)
     (=
      :ephemeral-conformance-artifacts
      (get-in artifact [:b13-record :publication :status])))
    (and
     (absolute-path? publication-path)
     (= #{:published-output-intent} retentions)
     (=
      :published-after-final-verification-intent
      (get-in artifact [:b13-record :publication :status]))
     (=
      #{:mode-policy :publisher-evidence :sidecar-hashes :status}
      (set (keys publication-receipt)))
     (=
      :published-atomically-after-final-verification
      (:status publication-receipt))
     (=
      {:directory "0755", :executable "0755", :nonexecutable "0644"}
      (:mode-policy publication-receipt))
     (=
      #{:conformance :manifest :provenance}
      (set (keys (:sidecar-hashes publication-receipt))))
     (every?
      (fn*
       [p1__194#]
       (and
        (=
         #{:content-hash :logical-path :byte-count}
         (set (keys p1__194#)))
        (integer? (:byte-count p1__194#))
        (<=
         1
         (:byte-count p1__194#)
         p15-s23-b3-llvm-max-emitted-file-bytes)
        (sha256-value? (:content-hash p1__194#))))
      (vals (:sidecar-hashes publication-receipt)))
     (=
      {:manifest "manifest.edn",
       :provenance "provenance.edn",
       :conformance "conformance.edn"}
      (into
       {}
       (map (fn [[kind record]] [kind (:logical-path record)]))
       (:sidecar-hashes publication-receipt)))
     (=
      #{:jdk-feature
        :native-library
        :parent-file-key-hash
        :native-access-enabled?
        :errno-read-policy
        :symbol
        :ffi-provider
        :jdk-version
        :guarantee-scope
        :no-follow-any?
        :staging-file-key-hash
        :exclusive-no-clobber?
        :flags
        :path-identity-linearization
        :commit-primitive}
      (set (keys (:publisher-evidence publication-receipt))))
     (=
      {:jdk-feature 26,
       :native-library :darwin-libsystem,
       :native-access-enabled? true,
       :errno-read-policy :failure-only,
       :symbol "renamex_np",
       :ffi-provider :jdk-26-foreign-function-and-memory,
       :jdk-version "26.0.1",
       :guarantee-scope
       #{:no-symlink-traversal :exclusive-destination},
       :no-follow-any? true,
       :exclusive-no-clobber? true,
       :flags {:rename-excl 4, :rename-nofollow-any 16, :combined 20},
       :path-identity-linearization
       :precommit-file-key-checked-not-fd-relative,
       :commit-primitive :darwin-renamex-np}
      (dissoc
       (:publisher-evidence publication-receipt)
       :parent-file-key-hash
       :staging-file-key-hash))
     (sha256-value?
      (get-in
       publication-receipt
       [:publisher-evidence :parent-file-key-hash]))
     (sha256-value?
      (get-in
       publication-receipt
       [:publisher-evidence :staging-file-key-hash]))))
   (= 1 (:schema-version artifact))
   (=
    :validated-candidate-for-bounded-internal-slice
    (:status artifact))
   (= [] (:diagnostics artifact))
   (= :accepted (get-in artifact [:c14-request :status]))
   (=
    :gravity/c14-internal-target-lowering-request
    (get-in artifact [:c14-request :artifact])))))
