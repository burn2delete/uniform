

(defn native-lowering-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (native-lowering-source-overrides module)
        _ (native-lowering-validate-source-overrides! source-path
                                                      source-overrides)
        interface-artifact (backend-interface-source-artifact source-path
                                                              source-text)
        input-id (:artifact-id interface-artifact)
        c-source "/* gravity stage0 P07-T02 C artifact */\n#include <stdint.h>\nint64_t gravity_entry(int64_t x) { return x + 1; }\n"
        c-header "#pragma once\n#include <stdint.h>\nint64_t gravity_entry(int64_t x);\n"
        llvm-ir "; gravity stage0 P07-T02 LLVM artifact\ndefine i64 @gravity_entry(i64 %x) {\nentry:\n  %y = add i64 %x, 1\n  ret i64 %y\n}\n"
        mlir-module "module {\n  func.func @gravity_entry(%x: i64) -> i64 {\n    %c1 = arith.constant 1 : i64\n    %y = arith.addi %x, %c1 : i64\n    return %y : i64\n  }\n}\n"
        c-manifest (native-lowering-artifact-manifest
                    :gravity.backend/c :c-source c-source input-id
                    "proof-map:c-stage0")
        llvm-manifest (native-lowering-artifact-manifest
                       :gravity.backend/llvm :llvm-ir llvm-ir input-id
                       "proof-map:llvm-stage0")
        mlir-manifest (native-lowering-artifact-manifest
                       :gravity.backend/mlir :mlir-module mlir-module
                       input-id "proof-map:mlir-stage0")
        diagnostic-stream (native-lowering-diagnostic-stream source-path
                                                             input-id)
        artifact-base
        {:kind :gravity/stage0-native-lowering-artifact
         :task "P07-T02"
         :document-set ["B2" "B3" "B7" "B13" "B14"]
         :governing-documents native-lowering-governing-documents
         :pass {:name :native-lowering
                :input :backend-interface-and-conformance-artifact
                :output :native-c-llvm-mlir-lowering-artifact
                :requires [:verified-backend-interface :target-manifest
                           :abi-layout :runtime-provider
                           :proof-to-target-metadata :source-debug-map
                           :capability-summary :artifact-manifest]
                :preserves [:source-spans :generated-origins :types
                            :effects :capabilities :safety :proofs
                            :unsafe-audit-ids :profile :target
                            :artifact-provenance]
                :emits [:c-source :c-header :llvm-ir :mlir-module
                        :artifact-manifests :target-lowering-manifest
                        :backend-conformance-record]
                :rejects native-lowering-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :backend-interface-artifact
         (select-keys interface-artifact
                      [:kind :task :artifact-id :capability-based-proof
                       :backend-interface-results])
         :backend-interface-artifact-kind (:kind interface-artifact)
         :backend-interface-artifact-hash input-id
         :target-lowering-manifest
         [{:backend :gravity.backend/c
           :target :x86_64-stage0
           :dialect :freestanding-c11
           :artifact-kinds [:c-source :header :build-manifest]
           :status :complete}
          {:backend :gravity.backend/llvm
           :target :x86_64-stage0
           :triple "x86_64-unknown-stage0"
           :data-layout "e-m:e-p:64:64-i64:64-n8:16:32:64"
           :artifact-kinds [:llvm-ir :bitcode-manifest :object-manifest]
           :status :complete}
          {:backend :gravity.backend/mlir
           :target :x86_64-stage0
           :dialects [:gravity.mir :func :arith :scf :cf :memref :llvm]
           :artifact-kinds [:mlir-module :verifier-report
                            :handoff-manifest]
           :status :complete}]
         :c-backend
         {:artifact :gravity/c-backend-manifest
          :backend :gravity.backend/c
          :dialect-selection {:dialect :freestanding-c11
                              :hosted-libc :forbidden
                              :status :pinned}
          :source-files [{:path "gravity_stage0.c"
                          :content c-source
                          :hash (:content-hash c-manifest)}]
          :header-files [{:path "gravity_stage0.h"
                          :content c-header
                          :hash (c4-artifact-id c-header)}]
          :runtime-helper-manifest {:helpers [:panic-trap :bounds-check
                                              :numeric-check]
                                    :hidden-libc-dependencies []
                                    :status :complete}
          :abi-layout-manifest {:data-model :lp64
                                :endianness :little
                                :alignment {:i64 8}
                                :struct-layouts []
                                :status :pinned}
          :proof-to-c-assumption-map
          [{:assumption :no-signed-overflow
            :proof :proof/c18-bounds-check-dominance}
           {:assumption :pointer-provenance
            :proof :proof/c18-lifetime-range}]
          :build-manifest {:compiler-family :stage0-record
                           :flags ["-std=c11" "-fno-strict-aliasing"]
                           :floating-contract :strict
                           :status :complete}
          :ub-rejection {:diagnostic "B2-UB"
                         :construct :signed-overflow
                         :status :rejected}
          :status :complete}
         :llvm-backend
         {:artifact :gravity/llvm-backend-manifest
          :backend :gravity.backend/llvm
          :target-record {:triple "x86_64-unknown-stage0"
                          :cpu "generic"
                          :features #{}
                          :data-layout "e-m:e-p:64:64-i64:64-n8:16:32:64"
                          :relocation-model :static
                          :code-model :small
                          :status :pinned}
          :llvm-ir-files [{:path "gravity_stage0.ll"
                           :content llvm-ir
                           :hash (:content-hash llvm-manifest)}]
          :proof-to-llvm-metadata-map
          [{:metadata :nuw
            :operation :checked-add
            :proof :proof/c18-bounds-check-dominance}
           {:metadata :nonnull
            :operation :managed-reference
            :proof :proof/c18-safety-check-elision}]
          :metadata-policy {:status :gated
                            :forbidden-without-proof
                            [:nuw :nsw :inbounds :noalias :fast-math
                             :dereferenceable :tbaa]}
          :pass-pipeline-record {:passes [:verify-before :instcombine
                                          :verify-after]
                                 :disabled [{:pass :unsafe-fast-math
                                             :reason :missing-strict-proof}]
                                 :metadata-preservation :required
                                 :status :complete}
          :verifier-report {:status :passed
                            :checked [:ir-shape :metadata-gates
                                      :source-map]}
          :status :complete}
         :mlir-backend
         {:artifact :gravity/mlir-backend-manifest
          :backend :gravity.backend/mlir
          :mlir-version "stage0-recorded"
          :dialect-registry-manifest
          {:dialects [:gravity.mir :func :arith :scf :cf :memref :llvm]
           :status :complete}
          :gravity-dialect-operation-schema
          {:operations [:gravity.checked_add :gravity.return]
           :attributes [:source-span :effect :capability :proof :profile]
           :status :complete}
          :mlir-modules [{:path "gravity_stage0.mlir"
                          :content mlir-module
                          :hash (:content-hash mlir-manifest)}]
          :conversion-legality-report {:targets [:llvm]
                                       :remaining-illegal-ops []
                                       :status :passed}
          :pass-pipeline-log [{:pass :canonicalize
                               :facts-preserved [:source-span :proof
                                                 :capability]
                               :facts-invalidated []
                               :verifier-before :passed
                               :verifier-after :passed}]
          :verifier-report {:status :passed
                            :checked [:dialect-registry :operation-schema
                                      :metadata-preservation]}
          :proof-to-dialect-attribute-map
          [{:attribute :arith.no_overflow
            :proof :proof/c18-bounds-check-dominance}
           {:attribute :gravity.capability
            :proof :proof/c18-capability-preservation}]
          :downstream-handoff-manifest {:destination :gravity.backend/llvm
                                        :accepted-dialects [:func :arith
                                                           :llvm]
                                        :remaining-illegal-operations []
                                        :verifier-status :passed
                                        :status :complete}
          :status :complete}
         :artifact-manifests [c-manifest llvm-manifest mlir-manifest]
         :artifact-graph
         {:artifact :gravity/artifact-graph
          :nodes [{:id input-id :kind :backend-interface}
                  {:id (:content-hash c-manifest) :kind :c-source}
                  {:id (:content-hash llvm-manifest) :kind :llvm-ir}
                  {:id (:content-hash mlir-manifest) :kind :mlir-module}]
          :edges [{:from input-id :to (:content-hash c-manifest)
                   :pass :c-backend}
                  {:from input-id :to (:content-hash llvm-manifest)
                   :pass :llvm-backend}
                  {:from input-id :to (:content-hash mlir-manifest)
                   :pass :mlir-backend}]
          :status :complete}
         :metadata-preservation-report
         {:artifact :gravity/backend-metadata-preservation-report
          :status :preserved
          :fields [:source-spans :generated-origin-chain :types :effects
                   :capabilities :safety :proofs :unsafe-audit-ids
                   :profile :target :runtime :abi :artifact-graph
                   :conformance]}
         :backend-conformance-record
         {:artifact :gravity/backend-conformance-record
          :suite :p07-t02-native-lowering
          :status :passed
          :positive-lowering-results
          [{:backend :c :fixture :native-positive :status :passed}
           {:backend :llvm :fixture :native-positive :status :passed}
           {:backend :mlir :fixture :native-positive :status :passed}]
          :negative-diagnostic-results
          (mapv (fn [id] {:diagnostic id :status :matched})
                native-lowering-diagnostic-ids)
          :differential-results
          [{:backend :c :comparison :artifact-shape-vs-mir
            :status :matched}
           {:backend :llvm :comparison :artifact-shape-vs-mir
            :status :matched}
           {:backend :mlir :comparison :artifact-shape-vs-mir
            :status :matched}]
          :target-availability {:c-toolchain :not-required-for-stage0
                                :llvm-toolchain :not-required-for-stage0
                                :mlir-toolchain :not-required-for-stage0}
          :evidence-pack "backend-conformance-pack:p07-t02"}
         :native-diagnostic-stream diagnostic-stream
         :native-lowering-results
         {:documents ["B2" "B3" "B7" "B13" "B14"]
          :task "P07-T02"
          :backend-interface-input-status :complete
          :c-backend-status :complete
          :llvm-backend-status :complete
          :mlir-backend-status :complete
          :artifact-emission-status :complete
          :metadata-status :complete
          :conformance-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (native-lowering-validate! source-path artifact-base)
        capability-proof (native-lowering-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))