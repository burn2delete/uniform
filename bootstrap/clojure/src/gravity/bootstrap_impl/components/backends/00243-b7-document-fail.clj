

(defn b7-document-fail!
  [id source-path subject extra]
  (fail! id
         "B7 MLIR backend design document coverage validation failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :b7-mlir-backend-document
                 :stage (or (:stage subject)
                            :b7-mlir-backend-document-coverage)
                 :backend :gravity.backend/mlir
                 :profile (or (:profile subject) :native)
                 :artifact-id (:artifact-id subject)
                 :mir-op (or (:mir-op subject) :checked-add)
                 :domain-anchor (or (:domain-anchor subject) :mir)
                 :mlir-operation (or (:mlir-operation subject)
                                     (b7-document-mlir-operation id))
                 :dialect (or (:dialect subject)
                              (b7-document-dialect id))
                 :pass-name (or (:pass-name subject) :canonicalize)
                 :missing-or-invalidated-fact
                 (or (:missing-or-invalidated-fact subject)
                     (b7-document-missing-fact id))
                 :downstream-target
                 (or (:downstream-target subject) :gravity.backend/llvm)
                 :fallback-status (or (:fallback-status subject) :rejected)
                 :remediation "Emit MLIR only from verified MIR/domain IR with declared dialect registry, operation schemas, verifier and conversion legality reports, pass invalidation/repair logs, proof-to-dialect mappings, source/debug preservation, and complete downstream handoff manifests."}
                extra)))

(defn b7-document-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get b7-document-override-diagnostics fail-kind)]
      (b7-document-fail!
       id source-path
       {:stage :b7-mlir-backend-document-coverage
        :artifact-id (str "b7-document-" (name fail-kind))
        :missing-or-invalidated-fact fail-kind
        :mlir-operation (name fail-kind)
        :dialect fail-kind}
       {:missing-fields [fail-kind]}))))

(def b7-document-mlir-module
  (str "module attributes {gravity.profile = \"native\", gravity.target = \"x86_64-stage0\"} {\n"
       "  func.func @gravity_entry(%x: i64 loc(\"backend-native-lowering.gravity:entry\")) -> i64\n"
       "      attributes {gravity.effect = \"pure\", gravity.capability = \"none\", gravity.profile = \"native\"} {\n"
       "    %c1 = arith.constant 1 : i64 loc(\"proof/c18-bounds-check-dominance\")\n"
       "    %y = arith.addi %x, %c1 : i64\n"
       "      {gravity.numeric_mode = \"checked-i64\", gravity.proof = \"proof/c18-bounds-check-dominance\", gravity.source_span = \"backend-native-lowering.gravity:entry\"}\n"
       "      loc(\"backend-native-lowering.gravity:checked-add\")\n"
       "    return %y : i64 loc(\"backend-native-lowering.gravity:return\")\n"
       "  } loc(\"backend-native-lowering.gravity:function\")\n"
       "} loc(\"backend-native-lowering.gravity:module\")\n"))

(defn b7-document-mlir-structurally-valid?
  [source]
  (and (str/includes? source "module attributes")
       (str/includes? source "func.func @gravity_entry")
       (str/includes? source "arith.constant")
       (str/includes? source "arith.addi")
       (str/includes? source "gravity.numeric_mode")
       (str/includes? source "gravity.proof")
       (str/includes? source "gravity.source_span")
       (str/includes? source "loc(")))

(defn b7-document-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/b7-mlir-backend-diagnostic-stream
   :stage :b7-mlir-backend-document-coverage
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :b7-mlir-backend-document-coverage
            :backend :gravity.backend/mlir
            :message-key (keyword "backend-mlir" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "b7-document-syntax-" index)
                      :artifact input-id}
            :mir-op (case id
                      "B7-CONVERSION" :domain-anchor-lowering
                      "B7-EFFECT" :memory-effect-change
                      "B7-NUMERIC" :numeric-lowering
                      "B7-ALIAS" :ownership-alias-lowering
                      :checked-add)
            :domain-anchor (case id
                             "B7-HANDOFF" :downstream-llvm
                             "B7-NUMERIC" :efir
                             :mir)
            :mlir-operation (b7-document-mlir-operation id)
            :dialect (b7-document-dialect id)
            :pass-name (case id
                         "B7-PASS" :canonicalize
                         "B7-CONVERSION" :convert-func-to-llvm
                         :verify)
            :missing-or-invalidated-fact (b7-document-missing-fact id)
            :downstream-target (case id
                                 "B7-HANDOFF" :gravity.backend/gpu
                                 :gravity.backend/llvm)
            :fallback-status :rejected
            :facts {:dialect-verifier-is-not-gravity-proof true
                    :metadata-loss-policy :backend-error
                    :pass-invalidation-policy :repair-or-reject
                    :conversion-legality-policy :verifier-gated}
            :remediation [{:kind :declare-dialect-schema}
                          {:kind :preserve-gravity-metadata}
                          {:kind :run-conversion-and-handoff-gates}]
            :redactions []
            :ordering-key [id :b7-mlir-backend-document-coverage
                           :x86_64-stage0]})
         b7-document-diagnostic-ids
         (range))
   :status :complete})