(defn-
 semantic-llvm-final-contract-context
 [c11-artifact
  checked-core
  context
  c11-report
  bridge-packet
  bridge-report
  binding
  lowering
  oracle
  toolchain]
 (let
  [source-path
   (:source-path context)
   _
   (when-not
    (=
     bridge-report
     (p15-s23-c13-c14-b1-contextual-report-record bridge-packet))
    (p15-s23-b3-llvm-fail!
     "B13-HASH"
     source-path
     bridge-packet
     {:missing-fact :contextual-bridge-report-identity}))
   bridge-report-id
   (:report-id bridge-report)
   mir
   (:optimized-mir bridge-packet)
   c14-stage
   (:c14 bridge-packet)
   c14-request
   (:request c14-stage)
   b1-packet
   (dissoc
    (:b1 bridge-packet)
    :source-rule
    :actual-path-provenance
    :semantic-id
    :artifact-id
    :actual-path-binding-id)
   capability-proof-table
   (:capability-proof-table mir)
   proof-certificate-table
   (:proof-certificate-table mir)
   safety-proof-table
   (:safety-proofs proof-certificate-table)
   c11-verifier-record
   (p15-s23-b3-llvm-c11-verifier-record c11-report)
   c11-verifier-record-id
   (p15-s23-c11-mir-digest c11-verifier-record)
   source-target-selection
   (:source-target-selection c14-request)
   profile-contract
   (:profile-contract c14-request)
   target-contract
   (:target c14-request)
   abi-contract
   (p15-s23-b3-llvm-expected-abi-contract)
   runtime-contract
   (p15-s23-b3-llvm-expected-runtime-contract)
   provider-contract
   (p15-s23-b3-llvm-expected-provider-contract)
   dependency-contract
   (:dependency-provenance c14-request)
   contract-bindings
   (:contract-bindings c14-request)
   lowering-id
   (p15-s23-c11-mir-digest
    (dissoc lowering :clojure-seed-boundary? :self-hosted?))]
  (hash-map
   :source-path
   source-path
   :bridge-report-id
   bridge-report-id
   :mir
   mir
   :c14-stage
   c14-stage
   :c14-request
   c14-request
   :b1-packet
   b1-packet
   :capability-proof-table
   capability-proof-table
   :proof-certificate-table
   proof-certificate-table
   :safety-proof-table
   safety-proof-table
   :c11-verifier-record
   c11-verifier-record
   :c11-verifier-record-id
   c11-verifier-record-id
   :source-target-selection
   source-target-selection
   :profile-contract
   profile-contract
   :target-contract
   target-contract
   :abi-contract
   abi-contract
   :runtime-contract
   runtime-contract
   :provider-contract
   provider-contract
   :dependency-contract
   dependency-contract
   :contract-bindings
   contract-bindings
   :lowering-id
   lowering-id)))
