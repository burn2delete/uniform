

(def b3-document-diagnostic-ids
  ["B3-TARGET"
   "B3-METADATA"
   "B3-UB"
   "B3-POINTER"
   "B3-NUMERIC"
   "B3-ATOMIC"
   "B3-RUNTIME"
   "B3-ABI"
   "B3-PASS"
   "B3-MANIFEST"])

(def b3-document-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             b3-document-diagnostic-ids)))

(defn b3-document-source-overrides
  [module]
  (or (get-in module [:metadata :backend :b3-document])
      (get-in module [:metadata :backend :native-lowering])
      (get-in module [:metadata :backend :native])
      {}))

(defn b3-document-missing-proof
  [id]
  (case id
    "B3-TARGET" :target-triple-data-layout-proof
    "B3-METADATA" :gravity-proof-for-llvm-metadata
    "B3-UB" :no-llvm-undefined-behavior-proof
    "B3-POINTER" :pointer-provenance-lifetime-range-proof
    "B3-NUMERIC" :numeric-mode-and-shift-overflow-proof
    "B3-ATOMIC" :atomic-volatile-mmio-ordering-proof
    "B3-RUNTIME" :runtime-provider-selection-proof
    "B3-ABI" :calling-convention-layout-unwind-proof
    "B3-PASS" :pass-pipeline-metadata-preservation-proof
    "B3-MANIFEST" :complete-llvm-artifact-manifest
    :b3-document-evidence))

(defn b3-document-llvm-construct
  [id]
  (case id
    "B3-TARGET" :target-triple
    "B3-METADATA" :proof-gated-attribute
    "B3-UB" :poison-producing-operation
    "B3-POINTER" :getelementptr-inbounds
    "B3-NUMERIC" :integer-add-shift-or-float-flag
    "B3-ATOMIC" :atomicrmw-or-volatile-access
    "B3-RUNTIME" :runtime-helper-call
    "B3-ABI" :calling-convention-or-object-format
    "B3-PASS" :optimization-pass-pipeline
    "B3-MANIFEST" :llvm-backend-manifest
    :llvm-backend))

(defn b3-document-selected-fallback
  [id]
  (case id
    "B3-METADATA" :omit-proofless-metadata
    "B3-UB" :guarded-conservative-ir
    "B3-POINTER" :plain-gep-without-inbounds
    "B3-NUMERIC" :checked-branch-or-helper
    "B3-ATOMIC" :runtime-helper-or-reject
    "B3-RUNTIME" :profile-selected-helper-or-reject
    "B3-PASS" :disable-pass-or-repair-metadata
    :reject-target-request))

(defn b3-document-fail!
  [id source-path subject extra]
  (fail! id
         "B3 LLVM backend design document coverage validation failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :b3-llvm-backend-document
                 :stage (or (:stage subject)
                            :b3-llvm-backend-document-coverage)
                 :backend :gravity.backend/llvm
                 :profile (or (:profile subject) :native)
                 :target-triple (or (:target-triple subject)
                                    "x86_64-unknown-linux-gnu")
                 :artifact-id (:artifact-id subject)
                 :mir-op (or (:mir-op subject) :checked-add)
                 :domain-anchor (:domain-anchor subject)
                 :generated-origin-chain
                 (or (:generated-origin-chain subject)
                     [:mir :c14-target-lowering :b1-interface
                      :b3-llvm-backend])
                 :llvm-construct (or (:llvm-construct subject)
                                     (b3-document-llvm-construct id))
                 :missing-proof-or-provider
                 (or (:missing-proof-or-provider subject)
                     (b3-document-missing-proof id))
                 :selected-fallback (or (:selected-fallback subject)
                                        (b3-document-selected-fallback id))
                 :fallback-status (or (:fallback-status subject) :rejected)
                 :remediation "Emit LLVM only from verified backend input with a pinned target/data layout, proof-gated metadata and attributes, conservative defined IR for safe code, preserved pointer/numeric/atomic/runtime/ABI facts, verified pass pipelines, and complete artifact manifests."}
                extra)))

(defn b3-document-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get b3-document-override-diagnostics fail-kind)]
      (b3-document-fail!
       id source-path
       {:stage :b3-llvm-backend-document-coverage
        :artifact-id (str "b3-document-" (name fail-kind))
        :missing-proof-or-provider fail-kind
        :llvm-construct fail-kind}
       {:missing-fields [fail-kind]}))))

(def b3-document-llvm-ir
  (str "; gravity stage0 P07-D100 B3 LLVM backend fixture\n"
       "source_filename = \"gravity_stage0_b3\"\n"
       "target triple = \"x86_64-unknown-linux-gnu\"\n"
       "target datalayout = \"e-m:e-p:64:64-i64:64-n8:16:32:64-S128\"\n\n"
       "define i64 @gravity_entry(i64 %x) {\n"
       "entry:\n"
       "  %overflow = icmp sgt i64 %x, 9223372036854775806\n"
       "  br i1 %overflow, label %overflow_block, label %ok\n\n"
       "ok:\n"
       "  %y = add i64 %x, 1\n"
       "  ret i64 %y\n\n"
       "overflow_block:\n"
       "  ret i64 0\n"
       "}\n"))

(defn b3-document-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/b3-llvm-backend-diagnostic-stream
   :stage :b3-llvm-backend-document-coverage
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :b3-llvm-backend-document-coverage
            :backend :gravity.backend/llvm
            :message-key (keyword "backend-llvm" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "b3-document-syntax-" index)
                      :artifact input-id}
            :mir-op (case id
                      "B3-ATOMIC" :atomic-compare-exchange
                      "B3-POINTER" :pointer-offset
                      "B3-RUNTIME" :panic
                      :checked-add)
            :domain-anchor (when (= id "B3-METADATA") :efir)
            :profile :native
            :target-triple "x86_64-unknown-linux-gnu"
            :generated-origin-chain [:mir :c14-target-lowering
                                     :b1-interface :b3-llvm-backend]
            :llvm-construct (b3-document-llvm-construct id)
            :missing-proof-or-provider (b3-document-missing-proof id)
            :selected-fallback (b3-document-selected-fallback id)
            :fallback-status :rejected
            :facts {:llvm-is-not-semantics? true
                    :proof-gated-metadata-required? true
                    :verification-required? true}
            :remediation [{:kind :pin-llvm-target-record}
                          {:kind :omit-proofless-metadata-or-reject}
                          {:kind :run-llvm-verifier-and-preserve-evidence}]
            :redactions []
            :ordering-key [id :b3-llvm-backend-document-coverage
                           "x86_64-unknown-linux-gnu"]})
         b3-document-diagnostic-ids
         (range))
   :status :complete})