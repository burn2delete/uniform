(defn-
 semantic-llvm-frozen-contract-path-context
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
     expected-artifact-graph]}
   state
   actual-path-provenance
   (:actual-path-provenance artifact)
   actual-path-base-keys
   #{:c14-source
     :b3-source
     :ld-path
     :otool-locator-path
     :clang-locator-path
     :sdk-path
     :tool-installation-record
     :clang-path
     :source
     :c13-c14-b1-packet-binding-id
     :b1-source
     :otool-path
     :ld-locator-path
     :c13-source
     :file-path
     :c11-source
     :publication-path
     :magic-path
     :xcrun-path
     :sdk-locator-path}
   publication-path
   (:publication-path actual-path-provenance)
   publication-receipt
   (:publication-receipt actual-path-provenance)
   physical-record
   (:tool-installation-record actual-path-provenance)
   retentions
   (set (map :retention (vals files)))
   sha256-value?
   (fn
    [value]
    (and
     (string? value)
     (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))
   absolute-path?
   (fn
    [value]
    (and
     (string? value)
     (<= 1 (count value) 4096)
     (str/starts-with? value "/")))]
  (assoc
   state
   :actual-path-provenance
   actual-path-provenance
   :actual-path-base-keys
   actual-path-base-keys
   :publication-path
   publication-path
   :publication-receipt
   publication-receipt
   :physical-record
   physical-record
   :retentions
   retentions
   :sha256-value?
   sha256-value?
   :absolute-path?
   absolute-path?)))
