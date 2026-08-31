

(def b2-document-c-source
  (str "/* gravity stage0 P07-D099 B2 C backend fixture */\n"
       "#include <stdbool.h>\n"
       "#include <stdint.h>\n"
       "#include <limits.h>\n\n"
       "typedef struct {\n"
       "  int64_t value;\n"
       "  bool overflow;\n"
       "} gravity_i64_checked;\n\n"
       "static gravity_i64_checked gravity_checked_add_i64(int64_t x, int64_t y) {\n"
       "  if ((y > 0 && x > INT64_MAX - y) || (y < 0 && x < INT64_MIN - y)) {\n"
       "    return (gravity_i64_checked){0, true};\n"
       "  }\n"
       "  return (gravity_i64_checked){x + y, false};\n"
       "}\n\n"
       "int64_t gravity_entry(int64_t x) {\n"
       "  gravity_i64_checked result = gravity_checked_add_i64(x, 1);\n"
       "  return result.overflow ? 0 : result.value;\n"
       "}\n"))

(def b2-document-c-header
  (str "#pragma once\n"
       "#include <stdint.h>\n"
       "int64_t gravity_entry(int64_t x);\n"))

(defn b2-document-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/b2-c-backend-diagnostic-stream
   :stage :b2-c-backend-document-coverage
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :b2-c-backend-document-coverage
            :backend :gravity.backend/c
            :message-key (keyword "backend-c" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "b2-document-syntax-" index)
                      :artifact input-id}
            :mir-op (case id
                      "B2-MMIO" :volatile-load
                      "B2-FFI" :foreign-call
                      "B2-POINTER" :pointer-cast
                      :checked-add)
            :domain-anchor (when (= id "B2-MMIO") :mmio-register)
            :profile :native
            :target :x86_64-stage0
            :c-dialect :freestanding-c11
            :generated-origin-chain [:mir :c14-target-lowering
                                     :b1-interface :b2-c-backend]
            :missing-fact (b2-document-missing-fact id)
            :helper (b2-document-helper id)
            :target-construct (b2-document-target-construct id)
            :fallback-status :rejected
            :facts {:safe-gravity-has-no-c-undefined-behavior? true
                    :profile-runtime-helpers-explicit? true
                    :artifact-manifest-required? true}
            :remediation [{:kind :declare-c-dialect}
                          {:kind :pin-abi-layout-and-pointer-facts}
                          {:kind :emit-proof-backed-c-helper-or-reject}]
            :redactions []
            :ordering-key [id :b2-c-backend-document-coverage
                           :x86_64-stage0]})
         b2-document-diagnostic-ids
         (range))
   :status :complete})

(defn b2-document-c-manifest
  [source-path input-id]
  (let [source-hash (c4-artifact-id b2-document-c-source)
        header-hash (c4-artifact-id b2-document-c-header)]
    {:artifact :gravity/c-backend-manifest
     :backend :gravity.backend/c
     :dialects #{:freestanding-c11 :hosted-c17}
     :emits #{:c-source :header :build-manifest :provenance}
     :requires #{:layout :abi :safety-bundle :pointer-provenance}
     :rejects #{:implicit-c-ub :unpinned-abi
                :missing-pointer-provenance :hidden-libc
                :unchecked-overflow}
     :dialect-selection {:dialect :freestanding-c11
                         :hosted-libc :forbidden
                         :compiler-extension-profile nil
                         :status :pinned}
     :source-files [{:path "gravity_stage0_b2.c"
                     :content b2-document-c-source
                     :hash source-hash}]
     :header-files [{:path "gravity_stage0_b2.h"
                     :content b2-document-c-header
                     :hash header-hash}]
     :runtime-helper-manifest {:helpers [:panic-trap :bounds-check
                                         :numeric-check
                                         :mmio-volatile-access]
                               :profile-support
                               {:freestanding-c11 :package-or-platform
                                :hosted-c17 :package-or-platform}
                               :hidden-libc-dependencies []
                               :hidden-allocation []
                               :status :complete}
     :abi-layout-manifest {:compiler-family :stage0-record
                           :compiler-version-constraint "recorded-stage0"
                           :target-triple "x86_64-unknown-stage0"
                           :data-model :lp64
                           :endianness :little
                           :alignment {:i8 1 :i32 4 :i64 8
                                       :pointer 8}
                           :struct-field-order :source-order
                           :padding-policy :explicit-recorded
                           :enum-tag-width :u32
                           :closure-representation :env-pointer-plus-code
                           :slice-representation [:ptr :len]
                           :region-handle-representation :opaque-pointer
                           :linear-resource-handle-representation :owned-token
                           :ffi-calling-convention :c
                           :status :pinned}
     :pointer-memory-lowering
     {:required-facts [:object-identity :valid-range :alignment :lifetime
                       :aliasing-mode :mutability :nullability
                       :allocator-identity :provenance-across-casts]
      :raw-pointer-arithmetic :unsafe-island-or-proof-only
      :mmio-access :profile-specific-volatile-helper
      :status :complete}
     :numeric-lowering
     {:integer-modes {:checked :helper-or-proof-elided
                      :wrapping :unsigned-or-explicit-wrap
                      :saturating :helper-or-inline}
      :floating-mode :strict
      :fast-math-flags []
      :proof-assumptions [:no-signed-overflow-without-check
                          :no-invalid-shift
                          :no-unchecked-narrowing]
      :status :complete}
     :ffi-boundary-map
     {:adapters [{:boundary :gravity_ffi_stage0
                  :calling-convention :c
                  :ownership-transfer :manifested
                  :safety :checked}]
      :incomplete-foreign-boundaries []
      :status :complete}
     :proof-to-c-assumption-map
     [{:assumption :no-signed-overflow
       :proof :proof/c18-bounds-check-dominance}
      {:assumption :initialized-storage-before-read
       :proof :proof/c18-definite-initialization}
      {:assumption :pointer-provenance
       :proof :proof/c18-lifetime-range}
      {:assumption :strict-aliasing-disabled
       :proof :manifest/fno-strict-aliasing}]
     :build-manifest {:compiler-family :stage0-record
                      :flags ["-std=c11" "-fno-strict-aliasing"]
                      :floating-contract :strict
                      :overflow-semantics :gravity-checked
                      :status :complete}
     :source-debug-map {:source input-id
                        :source-path source-path
                        :locations [(str source-path ":c-source")
                                    (str source-path ":header")
                                    (str source-path ":build-manifest")
                                    (str source-path ":abi-layout")]
                        :generated-origin-chain [:mir
                                                 :c14-target-lowering
                                                 :b1-interface
                                                 :b2-c-backend]
                        :generated-files ["gravity_stage0_b2.c"
                                          "gravity_stage0_b2.h"]
                        :generated-source-map
                        {:c-source (str source-path
                                        ":c-source:gravity_checked_add")
                         :header (str source-path
                                      ":header:gravity_checked_add")
                         :build-manifest (str source-path
                                              ":build-manifest:c11")
                         :abi-layout (str source-path
                                          ":abi-layout:lp64")}
                        :status :preserved}
     :c-fixture-compilation-record
     {:source-file "gravity_stage0_b2.c"
      :declared-command "cc -std=c11 -fno-strict-aliasing -fsyntax-only gravity_stage0_b2.c"
      :proof-artifact "docs/artifacts/phase-07/reports/p07-d099-b2-c-backend-report.md"
      :status :requires-proof-command}
     :input-artifact input-id
     :status :complete}))