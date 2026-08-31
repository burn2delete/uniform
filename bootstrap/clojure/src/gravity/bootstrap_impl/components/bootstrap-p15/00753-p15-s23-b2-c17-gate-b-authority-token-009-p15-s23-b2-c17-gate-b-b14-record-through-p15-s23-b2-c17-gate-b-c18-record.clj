(let [p15-s23-b2-c17-gate-b-authority-token (nth __gravity_bootstrap_lexical_values_131964 0)
      p15-s23-b2-c17-gate-b-tool-state (nth __gravity_bootstrap_lexical_values_131964 1)]
(defn- p15-s23-b2-c17-gate-b-b14-record
  [gate-a contextual-report transaction]
  (let [operation-count (count (:operation-records gate-a))
        source-entry-count
        (count (get-in gate-a [:source-debug-map :entries]))
        proof-binding-count
        (count (get-in gate-a
                       [:proof-to-c-assumption-map :operation-bindings]))
        effect-fact-row-count
        (get-in gate-a [:verified-input-closure :effect-count])
        capability-fact-row-count
        (get-in gate-a [:verified-input-closure :capability-count])
        semantic-closure (:semantic-pure-closure contextual-report)
        metadata-closed?
        (p15-s23-b2-c17-gate-b-metadata-closure?
         gate-a contextual-report)
        _
        (when-not metadata-closed?
          (p15-s23-c-backend-fail!
           "B14-METADATA"
           (get-in gate-a [:actual-path-provenance :source]) gate-a
           {:missing-fact :exact-c17-source-proof-metadata-closure}))
        base
        {:artifact :gravity/b14-bounded-hosted-c17-conformance
         :schema-version 1
         :status :passed-for-bounded-positive-slice
         :backend :gravity.backend/c
         :profile :hosted :target :c :dialect :c17
         :fixture-id
         (p15-s23-c11-mir-digest
          {:kind :gravity/b14-bounded-c17-fixture
           :gate-a-semantic-id (:semantic-id gate-a)})
         :checks
         {:positive-lowering :passed
          :syntax :passed :compile :passed :link :passed
          :artifact-manifest :passed
          :metadata-preservation :passed
          :differential-execution :passed
          :diagnostic-id-regression :not-attached-to-positive-artifact}
         :process-evidence (:process-evidence transaction)
         :abi-evidence (:abi-evidence transaction)
         :runtime-provider-evidence
         (:runtime-provider-evidence transaction)
         :metadata-evidence
         {:source-debug-map-id
          (p15-s23-b2-c17-gate-b-neutral-content-id
           (:source-debug-map gate-a))
          :proof-to-c-assumption-map-id
          (p15-s23-b2-c17-gate-b-neutral-content-id
           (:proof-to-c-assumption-map gate-a))
          :operation-count operation-count
          :source-entry-count source-entry-count
          :proof-binding-count proof-binding-count
          :effect-fact-row-count effect-fact-row-count
          :capability-fact-row-count capability-fact-row-count
          :semantic-effect-count (:semantic-effect-count semantic-closure)
          :semantic-capability-count
          (:semantic-capability-count semantic-closure)
          :semantic-pure-closure-evidence-id
          (:evidence-id semantic-closure)
          :gate-a-contextual-report-id (:report-id contextual-report)
          :generated-origin-preserved? metadata-closed?}
         :nondeterminism-record
         {:status :single-build-candidate
          :timestamp-policy :excluded
          :linker-reproducibility :apple-ld-reproducible
          :cross-run-reproducibility-evidence-id :not-attached}
         :negative-evidence
         {:status :not-attached-to-positive-artifact
          :diagnostic-matrix-evidence-id :not-attached
          :negative-fixture-evidence-id :not-attached}
         :coverage
         {:bounded-pure-scalar-and-single-conditional? true
          :whole-b2? false :whole-b14? false
          :full-backend-fixture-matrix? false}
         :release-eligible? false}]
    (assoc base :record-id
           (p15-s23-b2-c17-gate-b-record-id
            :gravity/b14-bounded-hosted-c17-conformance base))))

(defn- p15-s23-b2-c17-gate-b-c18-record
  [gate-a contextual-report transaction]
  (let [minimum-evidence
        [:fresh-c11-replay :c13-identity-replay
         :c14-c-lowering-reconstruction :b1-c-manifest-reconstruction
         :gravity-b2-source-replay :independent-c-reconstruction
         :semantic-pure-effect-capability-closure
         :c17-syntax :clang-codegen :mach-o-abi
         :darwin-runtime-provider :differential-execution
         :content-hashes :source-origin-preservation]
        base
        {:artifact :gravity/c18-bounded-hosted-c17-verification
         :schema-version 1
         :status :passed-for-experimental-bounded-slice
         :pass :c11-through-b2-hosted-c17-gate-b
         :pass-version 1
         :risk :critical
         :risk-reason
         #{:trusted-semantic-base :seed-executed-native-code-emission
           :target-lowering :host-process-execution}
         :profile :hosted :target :c
         :affected-profiles #{:hosted}
         :affected-targets #{:c}
         :affected-artifact-kinds
         #{:c-source :c-header :mach-o-object :mach-o-executable}
         :minimum-evidence minimum-evidence
         :available-evidence minimum-evidence
         :evidence
         {:gate-a-contextual-report-id (:report-id contextual-report)
          :semantic-pure-closure-evidence-id
          (get-in contextual-report
                  [:semantic-pure-closure :evidence-id])
          :gate-a-artifact-id (:artifact-id gate-a)
          :toolchain-fingerprint-id
          (p15-s23-c11-mir-digest
           (:toolchain-fingerprint transaction))
          :abi-evidence-id
          (p15-s23-c11-mir-digest (:abi-evidence transaction))
          :differential-evidence-id
          (p15-s23-c11-mir-digest (:process-evidence transaction))}
         :semantic-bindings
         {:source-core
          (get-in gate-a [:verified-input-closure :source-core])
          :mir-module-id
          (get-in gate-a [:verified-input-closure :mir-module-id])
          :c11-artifact-id
          (get-in gate-a [:input-bindings :c11-artifact-id])
          :c13-artifact-id
          (get-in gate-a [:input-bindings :c13-artifact-id])
          :b1-artifact-id
          (get-in gate-a [:input-bindings :b1-artifact-id])
          :gate-a-artifact-id (:artifact-id gate-a)
          :emitted-content-hashes
          (into (sorted-map)
                (map (fn [[kind record]]
                       [kind (:content-hash record)]))
                (:artifact-files transaction))
          :expected-result (:expected-exit-code gate-a)
          :observed-result
          (get-in transaction [:process-evidence :observed-exit-code])}
         :origin-preservation
         {:source-debug-map-id
          (p15-s23-b2-c17-gate-b-neutral-content-id
           (:source-debug-map gate-a))
          :generated-origin-preserved?
          (p15-s23-b2-c17-gate-b-metadata-closure?
           gate-a contextual-report)}
         :compiler-trust-report-id
         (p15-s23-c11-mir-digest
          {:kind :gravity/c18-bounded-c17-compiler-trust-report
           :pass :c11-through-b2-hosted-c17-gate-b
           :version 1 :risk :critical
           :available-evidence minimum-evidence})
         :trust-boundary
         (cond->
          [:clojure-stage0-seed :openjdk-26.0.1-process-filesystem
           :gravity-b2-source :apple-xcrun-72 :apple-clang-21
           :macosx-sdk-26.5 :apple-ld-1267 :file-5.41
           :system-file-magic-mgc :llvm-otool-cctools-1040
           :darwin-process-loader :darwin-libsystem]
           (:publication-intent? transaction)
           (conj :openjdk-26.0.1-ffm-native-access
                 :darwin-libsystem-renameatx-np))
         :release-gate :closed
         :public-target-gate :closed
         :self-hosting-gate :closed
         :whole-c11-gate :closed :whole-c13-gate :closed
         :whole-c14-gate :closed :whole-b1-gate :closed
         :whole-b2-gate :closed :whole-b13-gate :closed
         :whole-b14-gate :closed :whole-c18-gate :closed
         :performance-residual
         {:fresh-replay-required? true
          :fresh-replay-latency-slo :not-established
          :single-run-timing-is-performance-evidence? false
          :operational-public-gate :closed
          :release-gate :closed}
         :known-gaps
         [:bounded-pure-scalar-surface-only
          :negative-matrix-not-part-of-this-artifact
          :single-host-toolchain
          :single-build-candidate
          :fresh-replay-latency-slo-not-established
          :whole-process-tree-reaping-proof-unavailable
          :clojure-seed-and-apple-toolchain-in-tcb]
         :process-cleanup-boundary
         {:captured-process-set-rechecked? true
          :whole-process-tree-proof? false}
         :release-result :blocked
         :release-eligible? false :self-hosted? false}]
    (assoc base :record-id
           (p15-s23-b2-c17-gate-b-record-id
            :gravity/c18-bounded-hosted-c17-verification base)))))
