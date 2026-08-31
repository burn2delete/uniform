(defn-
 semantic-llvm-frozen-contract-section-05
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
    #{:xcrun :file :otool}
    (set
     (keys (:verification-tool-fingerprints toolchain-fingerprint))))
   (every?
    normalized-fingerprint?
    (vals (:verification-tool-fingerprints toolchain-fingerprint)))
   (=
    (:verification-tool-fingerprints toolchain-fingerprint)
    (get-in artifact [:b3-record :verification-tool-record]))
   (=
    #{:artifact-files
      :process-evidence
      :publication
      :runtime-provider-evidence
      :toolchain-fingerprint
      :abi-evidence
      :tool-records}
    (set (keys (:toolchain-evidence artifact))))
   (=
    metadata-free-files
    (get-in artifact [:toolchain-evidence :artifact-files]))
   (=
    (get-in artifact [:b13-record :publication])
    (get-in artifact [:toolchain-evidence :publication]))
   (= 19 (count tool-records) (count tool-record-by-step))
   (=
    (set (keys expected-command-contracts))
    (set (keys tool-record-by-step)))
   (every?
    (fn
     [[step record]]
     (and
      (=
       #{:stderr-hash
         :semantic-output-normalized?
         :exit-code
         :stdout-hash
         :timed-out?
         :raw-output-retained?
         :stdout-retained-byte-count
         :stderr-retained-byte-count
         :environment-policy
         :termination
         :finished?
         :artifact
         :stderr-byte-count
         :command-contract
         :stderr-truncated?
         :stdout-byte-count
         :step
         :stdout-truncated?}
       (set (keys record)))
      (= :gravity/b3-bounded-tool-step (:artifact record))
      (=
       (get expected-command-contracts step)
       (:command-contract record))
      (true? (:finished? record))
      (false? (:timed-out? record))
      (=
       {:kill-requested? false,
        :captured-process-set-reaped? :not-applicable,
        :whole-process-tree-reaping-proved? false,
        :root-alive-after-kill? false,
        :descendants-alive-after-kill 0}
       (:termination record))
      (=
       (if
        (= :run step)
        (get-in artifact [:lowering :expected-exit-code])
        0)
       (:exit-code record))
      (=
       (:stdout-byte-count record)
       (:stdout-retained-byte-count record))
      (=
       (:stderr-byte-count record)
       (:stderr-retained-byte-count record))
      (every?
       (fn*
        [p1__196#]
        (and (integer? p1__196#) (<= 0 p1__196# 1048577)))
       [(:stdout-byte-count record) (:stderr-byte-count record)])
      (every?
       (fn*
        [p1__197#]
        (and
         (string? p1__197#)
         (re-matches #"sha256:[0-9a-f]{64}" p1__197#)))
       [(:stdout-hash record) (:stderr-hash record)])
      (false? (:stdout-truncated? record))
      (false? (:stderr-truncated? record))
      (true? (:semantic-output-normalized? record))
      (=
       p15-s23-b3-llvm-environment-policy
       (:environment-policy record))
      (false? (:raw-output-retained? record))))
    tool-record-by-step)
   (apply = contract-bindings)
   (= (p15-s23-b3-llvm-content-binding profile) (:profile bindings))
   (= (p15-s23-b3-llvm-content-binding target) (:target bindings))
   (= (p15-s23-b3-llvm-content-binding abi) (:abi bindings))
   (= (p15-s23-b3-llvm-content-binding runtime) (:runtime bindings))
   (=
    (p15-s23-b3-llvm-content-binding providers)
    (:providers bindings))
   (=
    {:content-id
     (get-in artifact [:b1-packet :input :verifier-report-id]),
     :entry-count 1}
    (:c11-verifier bindings))
   (=
    (p15-s23-b3-llvm-content-binding
     (get-in artifact [:b1-packet :dependencies]))
    (:dependencies bindings))
   (=
    (assoc (p15-s23-b3-llvm-expected-abi-contract) :return-type :i32)
    abi)
   (= abi (get-in artifact [:b1-packet :abi]))
   (= b3-abi (get-in artifact [:b3-record :abi-record]))
   (=
    {:calling-convention :darwin-pcs-ccc,
     :data-layout (:data-layout p15-s23-b3-llvm-policy),
     :gravity-exception-unwind :none,
     :platform-unwind-metadata :darwin-compact-unwind-verified}
    (get-in artifact [:b13-record :abi-layout]))
   (= runtime (get-in artifact [:c14-request :runtime]))
   (= runtime (get-in artifact [:b1-packet :runtime]))
   (= providers (get-in artifact [:c14-request :providers]))
   (= providers (get-in artifact [:b1-packet :providers]))
   (=
    {:gravity-runtime-providers [],
     :platform-runtime-providers
     [:darwin/process-startup :darwin/dyld :darwin/libsystem],
     :status :no-gravity-helpers-platform-runtime-required,
     :provider-evidence-status :delegated-platform-runtime-verified,
     :full-runtime-conformance? false}
    (get-in artifact [:b3-record :runtime-record]))
   (= expected-dependencies (:dependencies build-identity))
   (=
    expected-dependencies
    (get-in artifact [:b13-record :dependency-provenance]))
   (=
    {:status :platform-runtime-delegated, :full-conformance? false}
    (get-in artifact [:b13-record :runtime]))
   (=
    [:darwin/process-startup :darwin/dyld :darwin/libsystem]
    (get-in artifact [:b13-record :providers]))
   (= #{} (get-in artifact [:b1-packet :effects]))
   (= #{} (get-in artifact [:b1-packet :capabilities])))))
