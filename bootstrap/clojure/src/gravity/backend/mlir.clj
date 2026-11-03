(ns gravity.backend.mlir
  "Native B7 diagnostic lookup data; no MLIR or input admission authority.")

(def ^:private namespace-contract
  {:namespace 'gravity.backend.mlir
   :governing-contracts '[D1 D2 D3 D8 D9 C1 C15 B1 B7 B13 B14 BOOT8 TEST2]
   :contract-boundary :native-b7-diagnostic-policy-data
   :public-api {'source-overrides {:arglists '([module]) :admission :none}
                'missing-fact {:arglists '([id]) :admission :none}
                'mlir-operation {:arglists '([id]) :admission :none}
                'dialect {:arglists '([id]) :admission :none}}
   :artifact-inputs [:native-module-metadata :native-b7-diagnostic-id]
   :artifact-outputs [:borrowed-source-overrides :native-missing-fact
                      :native-mlir-operation-name :native-dialect-label]
   :diagnostic-ownership {:stage [:native-override-precedence :native-rule-context-defaults]
                          :existing-owner [:b7-refusal-decisions :failure-construction
                                           :diagnostic-stream :source-admission :publication]}
   :dependency-direction {:requires ['clojure.core] :consumed-by ['gravity.bootstrap]
                          :forbids ['gravity.bootstrap 'gravity.cli 'gravity.release]}
   :authenticates-inputs? false :issues-certificates? false
   :supported [:existing-native-projection :borrowed-identity :compatibility-var-interposition]
   :unsupported [:canonical-b7-semantics :gravity-b7-source-compilation
                 :verified-mir-or-domain-ingress :mlir-emission :mlir-verification
                 :conversion-legality :proof-or-metadata-validation :downstream-handoff
                 :source-authentication :public-diagnostic-redaction :migration :release
                 :self-hosting :seed-retirement]})

(defn source-overrides
  [module]
  (or (get-in module [:metadata :backend :b7-document])
      (get-in module [:metadata :backend :native-lowering])
      (get-in module [:metadata :backend :native])
      {}))

(defn missing-fact
  [id]
  (case id
    "B7-DIALECT" :declared-dialect-operation-registry
    "B7-VERIFY" :mlir-verifier-report
    "B7-CONVERSION" :conversion-target-legality-evidence
    "B7-METADATA" :source-proof-safety-capability-metadata
    "B7-EFFECT" :effect-memory-order-preservation-record
    "B7-NUMERIC" :numeric-mode-dialect-attribute-map
    "B7-ALIAS" :alias-ownership-lifetime-map
    "B7-PASS" :pass-invalidation-repair-record
    "B7-HANDOFF" :downstream-handoff-manifest
    "B7-MANIFEST" :complete-mlir-backend-manifest
    :b7-document-evidence))

(defn mlir-operation
  [id]
  (case id
    "B7-DIALECT" "gravity.checked_add"
    "B7-VERIFY" "func.func"
    "B7-CONVERSION" "arith.addi"
    "B7-METADATA" "loc"
    "B7-EFFECT" "MemoryEffectOpInterface"
    "B7-NUMERIC" "arith.addi"
    "B7-ALIAS" "memref.load"
    "B7-PASS" "canonicalize"
    "B7-HANDOFF" "llvm.func"
    "B7-MANIFEST" "gravity/mlir-backend-manifest"
    "gravity.checked_add"))

(defn dialect
  [id]
  (case id
    "B7-DIALECT" :gravity.mir
    "B7-VERIFY" :func
    "B7-CONVERSION" :llvm
    "B7-METADATA" :builtin
    "B7-EFFECT" :memref
    "B7-NUMERIC" :arith
    "B7-ALIAS" :memref
    "B7-PASS" :canonicalize
    "B7-HANDOFF" :llvm
    "B7-MANIFEST" :gravity.backend/mlir
    :gravity.mir))
(doseq [operation [#'source-overrides #'missing-fact #'mlir-operation #'dialect]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
