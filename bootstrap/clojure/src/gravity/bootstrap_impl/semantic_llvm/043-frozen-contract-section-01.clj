(defn-
 semantic-llvm-frozen-contract-section-01
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
   (=
    #{:b14-record
      :release-credit?
      :public-target?
      :c14-request
      :diagnostics
      :b3-record
      :whole-language?
      :artifact-id
      :c11-llvm-credit?
      :schema-version
      :target-lowering-credit?
      :seed-boundary?
      :c13-c14-b1-packet
      :self-hosted?
      :actual-path-binding-id
      :semantic-id
      :actual-path-provenance
      :status
      :kind
      :b13-record
      :lowering
      :c18-record
      :b1-packet
      :toolchain-evidence
      :backend-credit?
      :clojure-seed-boundary?}
    (set (keys artifact)))
   (let
    [packet
     (:c13-c14-b1-packet artifact)
     raw-b1
     (dissoc
      (:b1 packet)
      :source-rule
      :actual-path-provenance
      :semantic-id
      :artifact-id
      :actual-path-binding-id)
     packet-semantic-input
     (p15-s23-c13-c14-b1-semantic-input packet)
     packet-projection
     (p15-s23-c13-c14-b1-reproducible-projection packet)]
    (and
     (=
      :gravity/p15-s23-c13-c14-b1-authenticated-packet
      (:kind packet))
     (= :accepted-for-bounded-llvm (:status packet))
     (= (:c14-request artifact) (get-in packet [:c14 :request]))
     (= (:b1-packet artifact) raw-b1)
     (= (:optimized-mir packet) (get-in packet [:c13 :optimized-mir]))
     (=
      (:semantic-id packet)
      (p15-s23-c11-mir-digest packet-semantic-input))
     (= packet-semantic-input (:semantic-input packet-projection))
     (=
      packet-projection
      {:c14-request-id (get-in packet [:c14 :request :request-id]),
       :c13-artifact-id (get-in packet [:c13 :artifact-id]),
       :artifact-id (:artifact-id packet),
       :semantic-id (:semantic-id packet),
       :b1-artifact-id (get-in packet [:b1 :artifact-id]),
       :semantic-input packet-semantic-input,
       :b1-semantic-id (get-in packet [:b1 :semantic-id]),
       :c14-artifact-id (get-in packet [:c14 :artifact-id]),
       :c14-semantic-id (get-in packet [:c14 :semantic-id]),
       :c13-semantic-id (get-in packet [:c13 :semantic-id])})
     (=
      (:artifact-id packet)
      (p15-s23-c11-mir-digest
       {:kind (:kind packet),
        :schema-version 1,
        :semantic-id (:semantic-id packet)}))
     (=
      (:actual-path-binding-id packet)
      (p15-s23-c11-mir-digest
       {:kind :gravity/c13-c14-b1-actual-path-binding,
        :semantic-id (:semantic-id packet),
        :actual-path-provenance (:actual-path-provenance packet)}))))
   (=
    (cond->
     actual-path-base-keys
     (contains? actual-path-provenance :publication-receipt)
     (conj :publication-receipt))
    (set (keys actual-path-provenance)))
   (=
    {:c13-source
     (get-in
      artifact
      [:c13-c14-b1-packet :actual-path-provenance :c13-source]),
     :c14-source
     (get-in
      artifact
      [:c13-c14-b1-packet :actual-path-provenance :c14-source]),
     :b1-source
     (get-in
      artifact
      [:c13-c14-b1-packet :actual-path-provenance :b1-source]),
     :c13-c14-b1-packet-binding-id
     (get-in artifact [:c13-c14-b1-packet :actual-path-binding-id])}
    (select-keys
     actual-path-provenance
     [:c13-source
      :c14-source
      :b1-source
      :c13-c14-b1-packet-binding-id]))
   (every?
    absolute-path?
    (map actual-path-provenance [:c13-source :c14-source :b1-source]))
   (=
    #{:otool-locator-path
      :otool-effective-path
      :clang-locator-path
      :ld-effective-path
      :clang-effective-path
      :ld-locator-path
      :magic-last-modified-millis
      :sdk-effective-path
      :file-path
      :magic-path
      :xcrun-path
      :magic-file-key-hash
      :sdk-locator-path
      :locator-output-hashes}
    (set (keys physical-record)))
   (=
    {:ld-path (:ld-effective-path physical-record),
     :otool-locator-path (:otool-locator-path physical-record),
     :clang-locator-path (:clang-locator-path physical-record),
     :sdk-path (:sdk-effective-path physical-record),
     :clang-path (:clang-effective-path physical-record),
     :otool-path (:otool-effective-path physical-record),
     :ld-locator-path (:ld-locator-path physical-record),
     :file-path (:file-path physical-record),
     :magic-path (:magic-path physical-record),
     :xcrun-path (:xcrun-path physical-record),
     :sdk-locator-path (:sdk-locator-path physical-record)}
    (select-keys
     actual-path-provenance
     [:xcrun-path
      :file-path
      :magic-path
      :sdk-path
      :sdk-locator-path
      :clang-path
      :clang-locator-path
      :ld-path
      :ld-locator-path
      :otool-path
      :otool-locator-path])))))
