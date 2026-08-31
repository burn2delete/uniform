(defn-
 semantic-llvm-frozen-contract-base-context
 [artifact]
 (let
  [selection
   (p15-s23-b3-llvm-expected-source-target-selection)
   profile
   (p15-s23-b3-llvm-expected-profile-contract)
   target
   (get-in artifact [:c14-request :target])
   b3-target
   (p15-s23-b3-llvm-expected-b3-target-record)
   b13-target
   (p15-s23-b3-llvm-expected-b13-target)
   build-target
   (p15-s23-b3-llvm-expected-build-target)
   abi
   (get-in artifact [:c14-request :abi])
   b3-abi
   (p15-s23-b3-llvm-expected-b3-abi-record)
   runtime
   (p15-s23-b3-llvm-expected-runtime-contract)
   providers
   (p15-s23-b3-llvm-expected-provider-contract)
   pass-record
   (p15-s23-b3-llvm-expected-pass-record)
   contract-bindings
   [(get-in artifact [:c14-request :contract-bindings])
    (get-in artifact [:b1-packet :contract-bindings])
    (get-in artifact [:b3-record :contract-bindings])
    (get-in artifact [:b13-record :contract-bindings])
    (get-in artifact [:c18-record :contract-bindings])]
   bindings
   (first contract-bindings)
   files
   (get-in artifact [:b13-record :artifact-files])
   metadata-free-files
   (into
    (sorted-map)
    (map
     (fn
      [[kind file]]
      [kind
       (dissoc
        file
        :schema-version
        :backend
        :profile
        :bundle-build-id)]))
    files)
   build-id
   (get-in artifact [:b13-record :build-id])
   build-identity
   (get-in artifact [:b13-record :build-identity])
   b14-scope
   (get-in artifact [:b14-record :availability-scope])
   c18
   (get-in artifact [:c18-record])
   expected-bridge-report
   (p15-s23-c13-c14-b1-contextual-report-record
    (:c13-c14-b1-packet artifact))
   expected-build-providers
   (:build providers)
   expected-dependencies
   {:gravity-runtime-providers [],
    :platform-runtime-providers
    [:darwin/process-startup :darwin/dyld :darwin/libsystem],
    :build-providers expected-build-providers,
    :c14-dependencies
    (get-in
     artifact
     [:c13-c14-b1-packet :c14 :request :dependency-provenance]),
    :backend-manifest-id
    (p15-s23-c11-mir-digest
     (get-in artifact [:c13-c14-b1-packet :b1 :backend-manifest])),
    :authenticated-packet-id
    (get-in artifact [:c13-c14-b1-packet :artifact-id])}
   expected-artifact-kinds
   {:llvm-ir
    {:artifact-kind :llvm-ir,
     :logical-path "program.ll",
     :mode "0644"},
    :object
    {:artifact-kind :mach-o-object,
     :logical-path "program.o",
     :mode "0644",
     :format :mach-o,
     :architecture :arm64},
    :executable
    {:artifact-kind :mach-o-executable,
     :logical-path "program",
     :mode "0755",
     :format :mach-o,
     :architecture :arm64}}]
  (hash-map
   :selection
   selection
   :profile
   profile
   :target
   target
   :b3-target
   b3-target
   :b13-target
   b13-target
   :build-target
   build-target
   :abi
   abi
   :b3-abi
   b3-abi
   :runtime
   runtime
   :providers
   providers
   :pass-record
   pass-record
   :contract-bindings
   contract-bindings
   :bindings
   bindings
   :files
   files
   :metadata-free-files
   metadata-free-files
   :build-id
   build-id
   :build-identity
   build-identity
   :b14-scope
   b14-scope
   :c18
   c18
   :expected-bridge-report
   expected-bridge-report
   :expected-build-providers
   expected-build-providers
   :expected-dependencies
   expected-dependencies
   :expected-artifact-kinds
   expected-artifact-kinds)))
