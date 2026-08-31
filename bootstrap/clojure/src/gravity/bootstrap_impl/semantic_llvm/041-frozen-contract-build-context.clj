(defn-
 semantic-llvm-frozen-contract-build-context
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
     expected-artifact-kinds]}
   state
   source-rule
   (p15-s23-b3-llvm-expected-source-rule)
   toolchain-fingerprint
   (get-in artifact [:toolchain-evidence :toolchain-fingerprint])
   toolchain-static
   (p15-s23-b3-llvm-expected-toolchain-static-record)
   tool-records
   (get-in artifact [:toolchain-evidence :tool-records])
   tool-record-by-step
   (into {} (map (juxt :step identity)) tool-records)
   expected-command-contracts
   (p15-s23-b3-llvm-expected-command-contracts)
   normalized-fingerprint?
   (fn
    [record]
    (and
     (=
      #{:stderr-hash
        :stdout-hash
        :stderr-byte-count
        :stdout-byte-count}
      (set (keys record)))
     (every?
      (fn*
       [p1__192#]
       (and
        (string? p1__192#)
        (re-matches #"sha256:[0-9a-f]{64}" p1__192#)))
      ((juxt :stdout-hash :stderr-hash) record))
     (every?
      (fn*
       [p1__193#]
       (and (integer? p1__193#) (<= 0 p1__193# 1048577)))
      ((juxt :stdout-byte-count :stderr-byte-count) record))))
   source-inputs
   {:c14-request
    (get-in artifact [:c13-c14-b1-packet :c14 :request :request-id]),
    :b3-source p15-s23-b3-llvm-expected-source-content-hash,
    :c14 (get-in artifact [:c13-c14-b1-packet :c14 :artifact-id]),
    :authenticated-packet
    (get-in artifact [:c13-c14-b1-packet :artifact-id]),
    :mir (get-in artifact [:c13-c14-b1-packet :c11 :mir-id]),
    :b1 (get-in artifact [:c13-c14-b1-packet :b1 :artifact-id]),
    :c13 (get-in artifact [:c13-c14-b1-packet :c13 :artifact-id]),
    :b3-builder p15-s23-b3-llvm-expected-builder-semantic-hash,
    :checked-core
    (get-in
     artifact
     [:c13-c14-b1-packet :c11 :checked-core-artifact-id]),
    :lowering (get-in artifact [:b3-record :lowering-id])}
   toolchain-digest
   (p15-s23-c11-mir-digest toolchain-fingerprint)
   c13-pass-provenance
   {:pass-id
    (get-in
     artifact
     [:c13-c14-b1-packet :c13 :pass-contract :pass-id]),
    :version
    (get-in
     artifact
     [:c13-c14-b1-packet :c13 :pass-contract :version]),
    :decision-id
    (get-in
     artifact
     [:c13-c14-b1-packet :c13 :decision-record :decision-id]),
    :c13-artifact-id
    (get-in artifact [:c13-c14-b1-packet :c13 :artifact-id]),
    :input-mir-id
    (get-in
     artifact
     [:c13-c14-b1-packet :c13 :decision-record :input-mir]),
    :output-mir-id
    (get-in
     artifact
     [:c13-c14-b1-packet :c13 :decision-record :output-mir])}
   pass-pipeline-base
   {:c11 (get-in artifact [:b13-record :pass-provenance :c11]),
    :c13 c13-pass-provenance,
    :b3 (:passes pass-record),
    :optimization-level (:optimization-level pass-record),
    :ub-sensitive-flags (:ub-sensitive-flags pass-record)}
   pass-pipeline-digest
   (p15-s23-c11-mir-digest pass-pipeline-base)
   expected-compiler-provenance
   {:c13-source-rule-id
    (p15-s23-c11-mir-digest
     (get-in artifact [:c13-c14-b1-packet :c13 :source-rule])),
    :c14-source-rule-id
    (p15-s23-c11-mir-digest
     (get-in artifact [:c13-c14-b1-packet :c14 :source-rule])),
    :b1-source-rule-id
    (p15-s23-c11-mir-digest
     (get-in artifact [:c13-c14-b1-packet :b1 :source-rule])),
    :b3-source p15-s23-b3-llvm-expected-source-content-hash,
    :builder p15-s23-b3-llvm-expected-builder-semantic-hash,
    :toolchain toolchain-fingerprint,
    :target-toolchain-digest toolchain-digest,
    :pass-pipeline-digest pass-pipeline-digest}
   expected-artifact-graph
   [{:from (:checked-core source-inputs),
     :to (:mir source-inputs),
     :edge :authenticated-c11-mir-construction}
    {:from (:mir source-inputs),
     :to (:c13 source-inputs),
     :edge :gravity-c13-identity-optimization}
    {:from (:c13 source-inputs),
     :to (:c14-request source-inputs),
     :edge :gravity-c14-lowering-request}
    {:from (:c14-request source-inputs),
     :to (:c14 source-inputs),
     :edge :gravity-c14-target-lowering-acceptance}
    {:from (:c14 source-inputs),
     :to (:b1 source-inputs),
     :edge :gravity-b1-backend-authentication}
    {:from (:b1 source-inputs),
     :to (:authenticated-packet source-inputs),
     :edge :authenticated-c13-c14-b1-packet}
    {:from (:authenticated-packet source-inputs),
     :to (:lowering source-inputs),
     :edge :verified-optimized-mir-lowering}
    {:from (:lowering source-inputs),
     :to (get-in files [:llvm-ir :content-hash]),
     :edge :llvm-ir-emission}
    {:from (get-in files [:llvm-ir :content-hash]),
     :to (get-in files [:object :content-hash]),
     :edge :clang-codegen}
    {:from (get-in files [:object :content-hash]),
     :to (get-in files [:executable :content-hash]),
     :edge :darwin-link}
    {:from build-id,
     :to (get-in files [:llvm-ir :content-hash]),
     :edge :bundle-build-identity}
    {:from build-id,
     :to (get-in files [:object :content-hash]),
     :edge :bundle-build-identity}
    {:from build-id,
     :to (get-in files [:executable :content-hash]),
     :edge :bundle-build-identity}]]
  (assoc
   state
   :source-rule
   source-rule
   :toolchain-fingerprint
   toolchain-fingerprint
   :toolchain-static
   toolchain-static
   :tool-records
   tool-records
   :tool-record-by-step
   tool-record-by-step
   :expected-command-contracts
   expected-command-contracts
   :normalized-fingerprint?
   normalized-fingerprint?
   :source-inputs
   source-inputs
   :toolchain-digest
   toolchain-digest
   :c13-pass-provenance
   c13-pass-provenance
   :pass-pipeline-base
   pass-pipeline-base
   :pass-pipeline-digest
   pass-pipeline-digest
   :expected-compiler-provenance
   expected-compiler-provenance
   :expected-artifact-graph
   expected-artifact-graph)))
