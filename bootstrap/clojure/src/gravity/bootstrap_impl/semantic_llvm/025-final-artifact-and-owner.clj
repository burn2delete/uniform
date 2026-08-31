(defn-
 semantic-llvm-final-artifact
 [c11-artifact
  checked-core
  context
  c11-report
  bridge-packet
  bridge-report
  binding
  lowering
  oracle
  toolchain
  state]
 (let
  [{:keys
    [source-path
     bridge-report-id
     mir
     c14-stage
     c14-request
     b1-packet
     capability-proof-table
     proof-certificate-table
     safety-proof-table
     c11-verifier-record
     c11-verifier-record-id
     source-target-selection
     profile-contract
     target-contract
     abi-contract
     runtime-contract
     provider-contract
     dependency-contract
     contract-bindings
     lowering-id
     b3-record
     c13-pass-provenance
     pass-pipeline-base
     pass-pipeline-digest
     compiler-provenance
     dependency-provenance
     b13-build-identity-base
     b13-build-id
     b13-build-identity
     b13-artifact-files
     b13-record
     b14-record
     c18-minimum-evidence
     c18-record]}
   state
   actual-path-provenance
   {:c14-source
    (get-in bridge-packet [:actual-path-provenance :c14-source]),
    :b3-source (:source-path binding),
    :ld-path
    (get-in toolchain [:physical-tool-provenance :ld-effective-path]),
    :otool-locator-path
    (get-in toolchain [:physical-tool-provenance :otool-locator-path]),
    :clang-locator-path
    (get-in toolchain [:physical-tool-provenance :clang-locator-path]),
    :sdk-path
    (get-in toolchain [:physical-tool-provenance :sdk-effective-path]),
    :tool-installation-record (:physical-tool-provenance toolchain),
    :clang-path
    (get-in
     toolchain
     [:physical-tool-provenance :clang-effective-path]),
    :source source-path,
    :c13-c14-b1-packet-binding-id
    (:actual-path-binding-id bridge-packet),
    :b1-source
    (get-in bridge-packet [:actual-path-provenance :b1-source]),
    :otool-path
    (get-in
     toolchain
     [:physical-tool-provenance :otool-effective-path]),
    :ld-locator-path
    (get-in toolchain [:physical-tool-provenance :ld-locator-path]),
    :c13-source
    (get-in bridge-packet [:actual-path-provenance :c13-source]),
    :file-path
    (get-in toolchain [:physical-tool-provenance :file-path]),
    :c11-source
    (get-in c11-artifact [:provenance :actual-paths :c11-source]),
    :publication-path (:actual-publication-path toolchain),
    :magic-path
    (get-in toolchain [:physical-tool-provenance :magic-path]),
    :xcrun-path
    (get-in toolchain [:physical-tool-provenance :xcrun-path]),
    :sdk-locator-path
    (get-in toolchain [:physical-tool-provenance :sdk-locator-path])}
   base
   {:b14-record b14-record,
    :release-credit? false,
    :public-target? false,
    :c14-request c14-request,
    :diagnostics [],
    :b3-record b3-record,
    :whole-language? false,
    :c11-llvm-credit? false,
    :schema-version 1,
    :target-lowering-credit? false,
    :seed-boundary? true,
    :c13-c14-b1-packet bridge-packet,
    :self-hosted? false,
    :actual-path-provenance actual-path-provenance,
    :status :validated-candidate-for-bounded-internal-slice,
    :kind :gravity/p15-s23-b3-authenticated-llvm-artifact,
    :b13-record b13-record,
    :lowering lowering,
    :c18-record c18-record,
    :b1-packet b1-packet,
    :toolchain-evidence
    (dissoc
     toolchain
     :actual-publication-path
     :physical-tool-provenance
     :publication-payload),
    :backend-credit? false,
    :clojure-seed-boundary? true}
   semantic-id
   (p15-s23-b3-llvm-artifact-id base)
   artifact-id
   (p15-s23-c11-mir-digest
    {:kind (:kind base), :schema-version 1, :semantic-id semantic-id})]
  (assoc
   base
   :semantic-id
   semantic-id
   :artifact-id
   artifact-id
   :actual-path-binding-id
   (p15-s23-b3-llvm-actual-path-binding-id
    semantic-id
    actual-path-provenance))))

(defn-
 p15-s23-b3-llvm-final-record
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
  [contract-context
   (semantic-llvm-final-contract-context
    c11-artifact
    checked-core
    context
    c11-report
    bridge-packet
    bridge-report
    binding
    lowering
    oracle
    toolchain)
   build-context
   (semantic-llvm-final-build-context
    c11-artifact
    checked-core
    context
    c11-report
    bridge-packet
    bridge-report
    binding
    lowering
    oracle
    toolchain
    contract-context)
   b13-context
   (semantic-llvm-final-b13-context
    c11-artifact
    checked-core
    context
    c11-report
    bridge-packet
    bridge-report
    binding
    lowering
    oracle
    toolchain
    build-context)
   verification-context
   (semantic-llvm-final-verification-context
    c11-artifact
    checked-core
    context
    c11-report
    bridge-packet
    bridge-report
    binding
    lowering
    oracle
    toolchain
    b13-context)]
  (semantic-llvm-final-artifact
   c11-artifact
   checked-core
   context
   c11-report
   bridge-packet
   bridge-report
   binding
   lowering
   oracle
   toolchain
   verification-context)))
