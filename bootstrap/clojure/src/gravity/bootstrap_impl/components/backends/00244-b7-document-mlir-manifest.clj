

(defn b7-document-mlir-manifest
  [input-id]
  (let [module-hash (c4-artifact-id b7-document-mlir-module)]
    {:artifact :gravity/mlir-backend-manifest
     :backend :gravity.backend/mlir
     :mlir-version "stage0-recorded"
     :dialects #{:gravity.mir :gravity.efir :func :arith :scf :cf
                 :memref :affine :vector :gpu :llvm}
     :emits #{:mlir-module :verifier-report :pass-log
              :handoff-manifest}
     :requires #{:dialect-registry :conversion-legality :proof-map
                 :metadata-preservation-policy}
     :rejects #{:metadata-loss :unverified-effect-change :dialect-ub
                :illegal-conversion-target}
     :dialect-registry-manifest
     {:mlir-version "stage0-recorded"
      :dialects [:gravity.mir :gravity.efir :func :arith :scf :cf
                 :memref :affine :vector :gpu :llvm]
      :operation-coverage {:gravity.mir [:gravity.checked_add
                                         :gravity.return]
                           :func [:func.func :func.return]
                           :arith [:arith.constant :arith.addi]
                           :memref []
                           :llvm [:llvm.func]
                           :gpu [:gpu.module]}
      :verifier-hooks [:types :effects :memory-regions :ownership
                       :capabilities :profile-legality :proof-references]
      :status :complete}
     :gravity-dialect-operation-schema
     {:operations [{:operation :gravity.checked_add
                    :operands [:I64 :I64]
                    :results [:I64]
                    :attributes [:source-span :generated-origin
                                 :effect :capability :ownership
                                 :safety-outcome :proof :profile
                                 :diagnostic-back-edge]}
                   {:operation :gravity.return
                    :operands [:I64]
                    :results []
                    :attributes [:source-span :generated-origin
                                 :effect :capability :profile]}]
      :type-schema [:I64 :F64 :Option :Result :opaque-host]
      :effect-interfaces [:MemoryEffectOpInterface]
      :capability-attribute :gravity.capability
      :proof-attribute :gravity.proof
      :profile-attribute :gravity.profile
      :status :complete}
     :standard-dialect-fact-mapping
     {:arith {:numeric-mode :gravity.numeric_mode
              :range-proof :arith.no_overflow}
      :memref {:alias-facts :gravity.alias_set
               :ownership :gravity.owner}
      :func {:source-spans :loc
             :effect-summary :gravity.effect}
      :gpu {:synchronization :gpu.barrier
            :capability :gravity.capability}
      :llvm {:proof-references :gravity.proof
             :debug-metadata :loc}
      :status :complete}
     :operation-and-type-mapping-record
     {:mappings [{:mir-operation :checked-add
                  :mlir-operation :arith.addi
                  :operand-types [:I64 :I64]
                  :result-types [:I64]
                  :memory-effect :none
                  :control-flow-region :single-block
                  :source-span "backend-native-lowering.gravity:entry"
                  :generated-origin-chain [:mir :c14-target-lowering
                                           :b1-interface
                                           :b7-mlir-backend]
                  :ownership-lifetime [:copy]
                  :capabilities #{}
                  :effects #{}
                  :safety-outcome :proven-safe
                  :proof "proof/c18-bounds-check-dominance"
                  :diagnostic-back-edge "B7-NUMERIC"}]
      :compiler-introduced-temporaries
      [{:value "%c1"
        :origin :constant-introduced-by-b7
        :source-back-edge "backend-native-lowering.gravity:entry"}]
      :status :complete}
     :mlir-modules [{:path "gravity_stage0.mlir"
                     :content b7-document-mlir-module
                     :hash module-hash
                     :status :complete}]
     :conversion-target-and-legality-report
     {:targets [:llvm :gpu]
      :legality-checks [{:target :llvm
                         :remaining-illegal-operations []
                         :verifier-status :passed}
                        {:target :gpu
                         :remaining-illegal-operations []
                         :verifier-status :passed}]
      :fallbacks []
      :status :passed}
     :pass-pipeline-log
     [{:pass :canonicalize
       :version "stage0"
       :input-dialects [:gravity.mir :func :arith]
       :output-dialects [:func :arith]
       :legality-conditions [:metadata-preserved :effects-unchanged]
       :facts-preserved [:source-span :generated-origin :types
                         :effects :capabilities :safety-outcome :proof
                         :profile]
       :facts-invalidated []
       :repair-passes []
       :verifier-before :passed
       :verifier-after :passed
       :downstream-handoff-target :gravity.backend/llvm}
      {:pass :convert-func-to-llvm
       :version "stage0"
       :input-dialects [:func :arith]
       :output-dialects [:llvm]
       :legality-conditions [:conversion-target-legal
                             :proof-map-retained]
       :facts-preserved [:source-span :proof :capability
                         :numeric-mode]
       :facts-invalidated [:high-level-function-region]
       :repair-passes [:source-debug-side-table]
       :verifier-before :passed
       :verifier-after :passed
       :downstream-handoff-target :gravity.backend/llvm}]
     :mlir-verifier-report
     {:structural-verifier :passed
      :external-toolchain :not-available-in-current-environment
      :declared-command "mlir-opt --verify-diagnostics /tmp/gravity-p07-b7.mlir"
      :checked [:dialect-registry :operation-schema
                :metadata-preservation :conversion-legality]
      :status :passed}
     :proof-to-dialect-attribute-map
     [{:proof :proof/c18-bounds-check-dominance
       :gravity-fact :range
       :dialect :arith
       :attribute :arith.no_overflow
       :operation :arith.addi}
      {:proof :proof/c18-lifetime-range
       :gravity-fact :alias
       :dialect :memref
       :attribute :gravity.alias_set}
      {:proof :proof/c18-capability-preservation
       :gravity-fact :capability
       :dialect :func
       :attribute :gravity.capability}
      {:proof :proof/math-numeric-mode
       :gravity-fact :numeric-mode
       :dialect :arith
       :attribute :gravity.numeric_mode}]
     :source-debug-map
     {:source input-id
      :locations ["backend-native-lowering.gravity:module"
                  "backend-native-lowering.gravity:function"
                  "backend-native-lowering.gravity:checked-add"]
      :generated-origin-chain [:mir :c14-target-lowering
                               :b1-interface :b7-mlir-backend]
      :unsafe-audit-ids []
      :proof-references ["proof/c18-bounds-check-dominance"]
      :status :preserved}
     :downstream-handoff-manifests
     [{:destination :gravity.backend/llvm
       :accepted-dialects [:func :arith :llvm]
       :remaining-illegal-operations []
       :conversion-target-status :passed
       :verifier-status :passed
       :source-proof-safety-capability-map :preserved
       :abi-runtime-provider-assumptions [:lp64 :stage0-runtime]
       :target-features #{}
       :rejected-or-deferred-constructs []
       :status :complete}
      {:destination :gravity.backend/gpu
       :accepted-dialects [:gpu :func :arith :memref]
       :remaining-illegal-operations []
       :conversion-target-status :passed
       :verifier-status :passed
       :source-proof-safety-capability-map :preserved
       :abi-runtime-provider-assumptions [:gpu-provider-manifest]
       :target-features #{:generic-gpu}
       :rejected-or-deferred-constructs []
       :status :complete}]
     :metadata-preservation-policy
     {:required [:source-span :unsafe-audit-id :capability
                 :effect :numeric-mode :alias-fact :proof
                 :profile :diagnostic-back-edge]
      :loss-policy :backend-error
      :status :complete}
     :semantic-authority-record
     {:mlir-verifier-is-gravity-proof false
      :gravity-proof-remains-authoritative true
      :dialect-ub-not-used-for-safe-gravity true
      :round-trip-target-behavior-to-mir :rejected
      :status :complete}
     :input-artifact input-id
     :status :complete}))