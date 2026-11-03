(ns gravity.backend.gpu
  "Native B8 diagnostic lookup data; no GPU or input admission authority.")

(def ^:private namespace-contract
  {:namespace 'gravity.backend.gpu
   :governing-contracts '[D1 D2 D3 D8 D9 C1 C15 B1 B8 B13 B14 BOOT8 TEST2]
   :contract-boundary :native-b8-diagnostic-policy-data
   :public-api {'source-overrides {:arglists '([module]) :admission :none}
                'missing-fact {:arglists '([id]) :admission :none}
                'kernel-id {:arglists '([id]) :admission :none}}
   :artifact-inputs [:native-module-metadata :native-b8-diagnostic-id]
   :artifact-outputs [:borrowed-source-overrides :native-missing-fact :native-kernel-label]
   :diagnostic-ownership {:stage [:native-override-precedence :native-rule-context-defaults]
                          :existing-owner [:b8-refusal-decisions :failure-construction
                                           :diagnostic-stream :source-admission :publication]}
   :dependency-direction {:requires ['clojure.core] :consumed-by ['gravity.bootstrap]
                          :forbids ['gravity.bootstrap 'gravity.cli 'gravity.release]}
   :authenticates-inputs? false :issues-certificates? false
   :supported [:existing-native-projection :borrowed-identity :compatibility-var-interposition]
   :unsupported [:canonical-b8-semantics :gravity-b8-source-compilation
                 :verified-mir-or-domain-ingress :gpu-emission :gpu-verification
                 :kernel-legality :device-memory-lifetimes :transfer-or-synchronization-validation
                 :launch-or-target-validation :math-certificates :downstream-handoff
                 :source-authentication :public-diagnostic-redaction :migration :release
                 :self-hosting :seed-retirement]})

(defn source-overrides
  [module]
  (or (get-in module [:metadata :backend :b8-document])
      (get-in module [:metadata :backend :specialized-lowering])
      (get-in module [:metadata :backend :specialized])
      {}))

(defn missing-fact
  [id]
  (case id
    "B8-TARGET" :gpu-api-device-feature-binary-format
    "B8-KERNEL" :gpu-profile-kernel-legality
    "B8-HOST-EFFECT" :host-effect-absence-proof
    "B8-MEMORY" :device-memory-lifetime-layout-address-space
    "B8-TRANSFER" :explicit-transfer-graph
    "B8-SYNC" :synchronization-graph-barrier-fence
    "B8-ATOMIC" :atomic-order-memory-scope-map
    "B8-LAUNCH" :launch-geometry-occupancy-shared-memory-record
    "B8-MATH" :numeric-mode-math-certificate
    "B8-MANIFEST" :complete-gpu-artifact-manifest
    :b8-document-evidence))

(defn kernel-id
  [id]
  (case id
    "B8-TARGET" :gravity_stage0_kernel
    "B8-KERNEL" :gravity_stage0_kernel
    "B8-HOST-EFFECT" :gravity_stage0_kernel
    "B8-MEMORY" :gravity_stage0_kernel
    "B8-TRANSFER" :gravity_stage0_kernel
    "B8-SYNC" :gravity_stage0_kernel
    "B8-ATOMIC" :gravity_stage0_atomic
    "B8-LAUNCH" :gravity_stage0_kernel
    "B8-MATH" :gravity_stage0_reduce
    "B8-MANIFEST" :gravity_stage0_kernel
    :gravity_stage0_kernel))

(doseq [operation [#'source-overrides #'missing-fact #'kernel-id]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
