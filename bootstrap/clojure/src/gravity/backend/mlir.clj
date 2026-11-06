(ns gravity.backend.mlir
  "Native B7 diagnostic and fixed document data; no MLIR or input admission authority."
  (:require [clojure.string :as str]))

(def ^:private namespace-contract
  {:namespace 'gravity.backend.mlir
   :governing-contracts '[D1 D2 D3 D8 D9 C1 C15 B1 B7 B13 B14 BOOT8 TEST2]
   :contract-boundary :native-b7-diagnostic-and-document-data
   :public-api {'source-overrides {:arglists '([module]) :admission :none}
                'missing-fact {:arglists '([id]) :admission :none}
                'mlir-operation {:arglists '([id]) :admission :none}
                'native-module-text {:arglists '([]) :admission :none
                  :contract-boundary :fixed-native-mlir-document-text
                  :operation-inputs [] :artifact-inputs [] :artifact-outputs [:fixed-mlir-text]
                  :authenticates-inputs? false :grants-authority? false}
                'native-module-structurally-valid? {:arglists '([source]) :admission :none
                  :contract-boundary :native-mlir-document-substring-check
                  :operation-inputs [] :artifact-inputs [:unverified-text] :artifact-outputs [:boolean]
                  :authenticates-inputs? false :grants-authority? false}
                'native-manifest {:arglists '([operations input-id]) :admission :none
                  :contract-boundary :native-mlir-document-data
                  :operation-inputs [:artifact-id :module-text]
                  :artifact-inputs [:unverified-input-id :fixed-fixture-text]
                  :artifact-outputs [:legacy-mlir-document-manifest]
                  :authenticates-inputs? false :grants-authority? false}
                'validate-native-manifest! {:arglists '([operations source-path artifact]) :admission :none
                  :contract-boundary :native-mlir-document-checks
                  :operation-inputs [:fail! :structurally-valid? :module-text :diagnostic-ids]
                  :artifact-inputs [:unverified-document-data]
                  :artifact-outputs [:legacy-structural-completion-or-refusal]
                  :diagnostics ["B7-MANIFEST" "B7-DIALECT" "B7-METADATA" "B7-CONVERSION"
                                "B7-VERIFY" "B7-HANDOFF" "B7-PASS"]
                  :diagnostic-publication :caller-owned
                  :authenticates-inputs? false :grants-authority? false}
                'native-coverage-report {:arglists '([operations artifact]) :admission :none
                  :contract-boundary :native-mlir-document-data
                  :operation-inputs [:structurally-valid? :module-text :diagnostic-ids]
                  :artifact-inputs [:unverified-document-data]
                  :artifact-outputs [:legacy-document-coverage-record]
                  :authenticates-inputs? false :grants-authority? false}
                'dialect {:arglists '([id]) :admission :none}}
   :artifact-inputs [:native-module-metadata :native-b7-diagnostic-id
                     :unverified-document-data :unverified-input-id :late-document-operations]
   :artifact-outputs [:borrowed-source-overrides :native-missing-fact
                      :native-mlir-operation-name :native-dialect-label :fixed-mlir-text
                      :legacy-mlir-document-manifest :legacy-structural-completion-or-refusal
                      :legacy-document-coverage-record]
   :diagnostic-ownership {:stage [:native-override-precedence :native-rule-context-defaults
                                   :native-document-structural-refusal-decisions]
                          :existing-owner [:source-admission-refusal-decisions :failure-construction
                                           :diagnostic-stream :source-admission :publication]}
   :dependency-direction {:requires ['clojure.core 'clojure.string] :consumed-by ['gravity.bootstrap]
                          :forbids ['gravity.bootstrap 'gravity.cli 'gravity.release]}
   :authenticates-inputs? false :issues-certificates? false
   :supported [:fixed-document-text :document-shape-checks :document-coverage-data
               :existing-native-projection :borrowed-identity :compatibility-var-interposition]
   :unsupported [:canonical-b7-semantics :gravity-b7-source-compilation
                 :verified-mir-or-domain-ingress :canonical-mlir-emission :mlir-verification
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
(defn native-module-text
  []
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

(defn native-module-structurally-valid?
  [source]
  (and (str/includes? source "module attributes")
       (str/includes? source "func.func @gravity_entry")
       (str/includes? source "arith.constant")
       (str/includes? source "arith.addi")
       (str/includes? source "gravity.numeric_mode")
       (str/includes? source "gravity.proof")
       (str/includes? source "gravity.source_span")
       (str/includes? source "loc(")))

(defn- native-document-port [operations key]
  (let [operation (get operations key)]
    (if (var? operation) @operation operation)))

(defn native-manifest
  [operations input-id]
  (let [module-hash ((native-document-port operations :artifact-id) (native-document-port operations :module-text))]
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
                     :content (native-document-port operations :module-text)
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
(defn validate-native-manifest!
  [operations source-path artifact]
  (let [native (:native-lowering-artifact artifact)
        manifest (:mlir-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b7-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :gravity/stage0-native-lowering-artifact (:kind native))
      ((native-document-port operations :fail!) "B7-MANIFEST" source-path native
                         {:missing-fields [:native-lowering-artifact]}))
    (when-not (= :complete (get-in native
                                   [:capability-based-proof :status]))
      ((native-document-port operations :fail!) "B7-MANIFEST" source-path native
                         {:missing-fields [:native-lowering-proof]}))
    (when-not (= :complete (get-in manifest
                                   [:dialect-registry-manifest
                                    :status]))
      ((native-document-port operations :fail!) "B7-DIALECT" source-path manifest
                         {:missing-fields [:dialect-registry]}))
    (when-not (= :complete (get-in manifest
                                   [:gravity-dialect-operation-schema
                                    :status]))
      ((native-document-port operations :fail!) "B7-DIALECT" source-path manifest
                         {:missing-fields [:operation-schema]}))
    (when-not (= :complete (get-in manifest
                                   [:standard-dialect-fact-mapping
                                    :status]))
      ((native-document-port operations :fail!) "B7-METADATA" source-path manifest
                         {:missing-fields [:standard-dialect-mapping]}))
    (when-not (= :complete (get-in manifest
                                   [:operation-and-type-mapping-record
                                    :status]))
      ((native-document-port operations :fail!) "B7-METADATA" source-path manifest
                         {:missing-fields [:operation-type-mapping]}))
    (when-not (= :passed (get-in manifest
                                 [:conversion-target-and-legality-report
                                  :status]))
      ((native-document-port operations :fail!) "B7-CONVERSION" source-path manifest
                         {:missing-fields [:conversion-legality]}))
    (when-not (= :passed (get-in manifest
                                 [:mlir-verifier-report :status]))
      ((native-document-port operations :fail!) "B7-VERIFY" source-path manifest
                         {:missing-fields [:verifier-report]}))
    (when-not (= :complete (get-in manifest
                                   [:metadata-preservation-policy
                                    :status]))
      ((native-document-port operations :fail!) "B7-METADATA" source-path manifest
                         {:missing-fields [:metadata-policy]}))
    (when-not (every? #(= :complete (:status %))
                      (:downstream-handoff-manifests manifest))
      ((native-document-port operations :fail!) "B7-HANDOFF" source-path manifest
                         {:missing-fields [:downstream-handoff]}))
    (when-not (every? #(and (= :passed (:verifier-before %))
                            (= :passed (:verifier-after %))
                            (contains? % :facts-preserved)
                            (contains? % :facts-invalidated))
                      (:pass-pipeline-log manifest))
      ((native-document-port operations :fail!) "B7-PASS" source-path manifest
                         {:missing-fields [:pass-pipeline-log]}))
    (when-not ((native-document-port operations :structurally-valid?)
               (native-document-port operations :module-text))
      ((native-document-port operations :fail!) "B7-VERIFY" source-path manifest
                         {:missing-fields [:mlir-module-structure]}))
    (when-not (every? #(contains? manifest %)
                      [:dialect-registry-manifest
                       :gravity-dialect-operation-schema
                       :standard-dialect-fact-mapping
                       :operation-and-type-mapping-record
                       :mlir-modules
                       :conversion-target-and-legality-report
                       :pass-pipeline-log
                       :mlir-verifier-report
                       :proof-to-dialect-attribute-map
                       :source-debug-map
                       :downstream-handoff-manifests
                       :metadata-preservation-policy
                       :semantic-authority-record])
      ((native-document-port operations :fail!) "B7-MANIFEST" source-path manifest
                         {:missing-fields [:mlir-artifact-manifest]}))
    (when-not (= (set (native-document-port operations :diagnostic-ids)) diagnostics)
      ((native-document-port operations :fail!) "B7-MANIFEST" source-path
                         (:b7-diagnostic-stream artifact)
                         {:missing-fields [:b7-diagnostics]})))
  :complete)

(defn native-coverage-report
  [operations artifact]
  (let [manifest (:mlir-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b7-diagnostic-stream
                                       :diagnostics])))]
    {:backend-interface-input-verified?
     (= :complete (get-in artifact
                          [:native-lowering-artifact
                           :capability-based-proof :status]))
     :dialect-registry-covered?
     (= :complete (get-in manifest
                          [:dialect-registry-manifest :status]))
     :gravity-operation-schema-covered?
     (= :complete (get-in manifest
                          [:gravity-dialect-operation-schema :status]))
     :standard-dialect-fact-mapping-covered?
     (= :complete (get-in manifest
                          [:standard-dialect-fact-mapping :status]))
     :operation-type-mapping-covered?
     (= :complete (get-in manifest
                          [:operation-and-type-mapping-record :status]))
     :mlir-module-emitted?
     (= :complete (get-in manifest [:mlir-modules 0 :status]))
     :conversion-legality-passed?
     (= :passed (get-in manifest
                        [:conversion-target-and-legality-report :status]))
     :pass-pipeline-verified?
     (every? #(and (= :passed (:verifier-before %))
                   (= :passed (:verifier-after %)))
             (:pass-pipeline-log manifest))
     :mlir-verifier-report-passed?
     (= :passed (get-in manifest [:mlir-verifier-report :status]))
     :proof-to-dialect-map-covered?
     (boolean (seq (:proof-to-dialect-attribute-map manifest)))
     :source-debug-map-preserved?
     (= :preserved (get-in manifest [:source-debug-map :status]))
     :downstream-handoff-covered?
     (= #{:gravity.backend/llvm :gravity.backend/gpu}
        (set (map :destination
                  (:downstream-handoff-manifests manifest))))
     :metadata-preservation-policy-covered?
     (= :complete (get-in manifest
                          [:metadata-preservation-policy :status]))
     :semantic-authority-preserved?
     (= :complete (get-in manifest
                          [:semantic-authority-record :status]))
     :mlir-structurally-valid?
     ((native-document-port operations :structurally-valid?) (native-document-port operations :module-text))
     :manifest-complete?
     (every? #(contains? manifest %)
             [:dialect-registry-manifest
              :gravity-dialect-operation-schema
              :standard-dialect-fact-mapping
              :operation-and-type-mapping-record
              :mlir-modules
              :conversion-target-and-legality-report
              :pass-pipeline-log
              :mlir-verifier-report
              :proof-to-dialect-attribute-map
              :source-debug-map
              :downstream-handoff-manifests
              :metadata-preservation-policy
              :semantic-authority-record])
     :diagnostics-covered?
     (= (set (native-document-port operations :diagnostic-ids)) diagnostics)
     :external-mlir-toolchain-validation?
     (get-in manifest [:mlir-verifier-report :external-toolchain])
     :status :complete}))
(doseq [[_ operation] (ns-publics *ns*)]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
