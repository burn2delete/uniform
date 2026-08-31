(defn- __gravity_bootstrap_gate_b_b13_identity_phase_01 [state]
  (let [{:syms [gate-a transaction b14 c18]} state
        files (:artifact-files transaction)
        content-hashes (into
                         (sorted-map)
                         (map (fn [[kind record]] [kind (:content-hash record)]))
                         files)
        pass-provenance {:c11-pass
                         (get-in
                           gate-a
                           [:verified-input-closure :pass-execution-record-id]),
                         :c13-artifact-id
                         (get-in gate-a [:input-bindings :c13-artifact-id]),
                         :c14-target :c,
                         :b1-artifact-id
                         (get-in gate-a [:input-bindings :b1-artifact-id]),
                         :b2-builder p15-s23-b2-c17-expected-builder-semantic-hash,
                         :tool-steps (mapv :step (:tool-records transaction))}
        pass-pipeline-digest (p15-s23-c11-mir-digest pass-provenance)
        compiler-provenance {:generator :gravity.backend/b2-c,
                             :generator-schema-version 1,
                             :backend-version :bounded-authenticated-c17-v1,
                             :b2-source-rule-id
                             (p15-s23-c11-mir-digest (:source-rule gate-a)),
                             :b2-source-content-hash
                             p15-s23-b2-c17-expected-source-content-hash,
                             :b2-builder-semantic-hash
                             p15-s23-b2-c17-expected-builder-semantic-hash,
                             :target-toolchain-digest
                             (p15-s23-c11-mir-digest
                               (:toolchain-fingerprint transaction)),
                             :pass-pipeline-digest pass-pipeline-digest}
        dependency-provenance {:generated-header
                               (get-in files [:header :content-hash]),
                               :sdk {:version "26.5", :policy :macosx-26.5},
                               :build-tools
                               [:apple-xcrun-72
                                :apple-clang-21
                                :apple-ld-1267
                                :file-5.41
                                :system-file-magic-mgc
                                :llvm-otool-cctools-1040],
                               :gravity-runtime-helpers [],
                               :platform-runtime-providers
                               [:darwin/process-startup
                                :darwin/dyld
                                :darwin/libsystem],
                               :linked-providers ["/usr/lib/libSystem.B.dylib"]}
        build-identity-base {:schema-version 1,
                             :source-inputs
                             {:source-core
                              (get-in gate-a [:verified-input-closure :source-core]),
                              :mir-module-id
                              (get-in gate-a [:verified-input-closure :mir-module-id]),
                              :gate-a-semantic-id (:semantic-id gate-a),
                              :gate-a-artifact-id (:artifact-id gate-a)},
                             :kind :hosted-c17-executable-bundle,
                             :compiler compiler-provenance,
                             :build-environment
                             {:policy p15-s23-b2-c17-gate-b-environment-policy,
                              :content-id
                              (p15-s23-c11-mir-digest
                                p15-s23-b2-c17-gate-b-environment-policy)},
                             :artifact-content-hashes content-hashes,
                             :artifact :gravity/b13-bounded-c17-build-identity,
                             :target
                             {:minimum-os-version "14.0",
                              :features [:+v8a :+fp-armv8 :+neon],
                              :c-standard :c17,
                              :runtime-reference
                              (p15-s23-c11-mir-digest
                                (:runtime-provider-evidence transaction)),
                              :architecture :arm64,
                              :optimization-level :O0,
                              :abi-layout-reference
                              (p15-s23-c11-mir-digest (:abi-evidence transaction)),
                              :code-model :small,
                              :object-format :mach-o,
                              :triple p15-s23-b2-c17-gate-b-target-triple,
                              :relocation-model :pic,
                              :cpu :generic,
                              :dialect :hosted-c17},
                             :backend :gravity.backend/c,
                             :dependencies dependency-provenance,
                             :profile :hosted}
        build-id (p15-s23-c11-mir-digest build-identity-base)
        build-identity (assoc build-identity-base :build-id build-id)
        target-common {:minimum-os-version "14.0",
                       :features [:+v8a :+fp-armv8 :+neon],
                       :c-standard :c17,
                       :runtime-reference
                       (p15-s23-c11-mir-digest
                         (:runtime-provider-evidence transaction)),
                       :architecture :arm64,
                       :optimization-level :O0,
                       :abi-layout-reference
                       (p15-s23-c11-mir-digest (:abi-evidence transaction)),
                       :code-model :small,
                       :object-format :mach-o,
                       :triple p15-s23-b2-c17-gate-b-target-triple,
                       :relocation-model :pic,
                       :cpu :generic,
                       :dialect :hosted-c17}
        target-fingerprint-id (p15-s23-c11-mir-digest target-common)
        source-provenance {:gate-a-semantic-id (:semantic-id gate-a),
                           :source-debug-map-id
                           (p15-s23-b2-c17-gate-b-neutral-content-id
                             (:source-debug-map gate-a)),
                           :generated-origin-policy
                           :preserved-and-path-neutralized-for-id}
        compiler-provenance-id (p15-s23-c11-mir-digest compiler-provenance)
        dependency-provenance-id (p15-s23-c11-mir-digest dependency-provenance)]
    (assoc
      state
      'files
      files
      'content-hashes
      content-hashes
      'pass-provenance
      pass-provenance
      'pass-pipeline-digest
      pass-pipeline-digest
      'compiler-provenance
      compiler-provenance
      'dependency-provenance
      dependency-provenance
      'build-identity-base
      build-identity-base
      'build-id
      build-id
      'build-identity
      build-identity
      'target-common
      target-common
      'target-fingerprint-id
      target-fingerprint-id
      'source-provenance
      source-provenance
      'compiler-provenance-id
      compiler-provenance-id
      'dependency-provenance-id
      dependency-provenance-id)))
