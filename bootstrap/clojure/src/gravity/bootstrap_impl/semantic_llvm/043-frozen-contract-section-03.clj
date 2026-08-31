(defn-
 semantic-llvm-frozen-contract-section-03
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
    #{:proofs
      :capabilities
      :providers
      :source-target-selection
      :request-id
      :diagnostics
      :unsupported-feature-report
      :required-evidence
      :fusion-status
      :schema-version
      :c13-optimization-status
      :profile-contract
      :status
      :proof-to-target-metadata
      :effects
      :runtime
      :safety
      :abi
      :source-map
      :dependency-provenance
      :target-policy
      :artifact
      :input
      :target
      :domain-ir-status
      :contract-bindings
      :profile}
    (set (keys (:c14-request artifact))))
   (= 1 (get-in artifact [:c14-request :schema-version]))
   (=
    :accepted-for-bounded-llvm
    (get-in artifact [:b1-packet :status]))
   (=
    :gravity/b1-verified-backend-input-packet
    (get-in artifact [:b1-packet :artifact]))
   (=
    #{:proofs
      :capabilities
      :providers
      :source-target-selection
      :diagnostics
      :unsupported-feature-report
      :backend-manifest
      :execution-tcb
      :schema-version
      :eligibility
      :self-hosted?
      :status
      :proof-to-target-metadata
      :effects
      :runtime
      :safety
      :abi
      :c14-eligibility
      :source-map
      :artifact
      :input
      :target
      :semantic-authority
      :dependencies
      :contract-bindings
      :profile
      :clojure-seed-boundary?}
    (set (keys (:b1-packet artifact))))
   (= 1 (get-in artifact [:b1-packet :schema-version]))
   (=
    :gravity/b3-internal-arm64-macos-llvm-record
    (get-in artifact [:b3-record :artifact]))
   (=
    #{:unsupported-record
      :abi-record
      :source-target-selection
      :diagnostics
      :lowering-id
      :pass-record
      :gravity-source-rule
      :target-record
      :source-map-record
      :runtime-record
      :status
      :provider-record
      :artifact
      :metadata-record
      :verification-tool-record
      :contract-bindings}
    (set (keys (:b3-record artifact))))
   (=
    :validated-candidate-for-bounded-internal-slice
    (get-in artifact [:b3-record :status]))
   (=
    :gravity/b13-bounded-llvm-emission-record
    (get-in artifact [:b13-record :artifact]))
   (=
    #{:capabilities
      :providers
      :timestamp-policy
      :source-target-selection
      :mode-policy
      :artifact-files
      :compiler-provenance
      :diagnostics
      :pass-provenance
      :conformance
      :build-identity
      :publication
      :schema-version
      :artifact-graph
      :content-hashes
      :proof
      :build-id
      :inputs
      :nondeterminism-policy
      :reproducibility
      :status
      :effects
      :runtime
      :safety
      :kind
      :dependency-provenance
      :artifact
      :target
      :abi-layout
      :backend
      :contract-bindings
      :profile}
    (set (keys (:b13-record artifact))))
   (= 1 (get-in artifact [:b13-record :schema-version]))
   (= :content-addressed (get-in artifact [:b13-record :status]))
   (=
    :gravity/b14-bounded-llvm-differential-record
    (get-in artifact [:b14-record :artifact]))
   (= :passed (get-in artifact [:b14-record :status]))
   (=
    #{:availability-scope
      :runtime-provider-evidence
      :reference-oracle
      :status
      :target-process
      :same-result?
      :artifact
      :abi-evidence}
    (set (keys (:b14-record artifact))))
   (= :gravity/c18-bounded-llvm-risk-trust-record (:artifact c18))
   (= :internal-experimental-only (:status c18)))))
