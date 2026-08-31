(let [p15-s23-b2-c17-gate-b-authority-token (nth __gravity_bootstrap_lexical_values_131964 0)
      p15-s23-b2-c17-gate-b-tool-state (nth __gravity_bootstrap_lexical_values_131964 1)]
(defn p15-s23-b1-wasm-expected-record [lowering backend-manifest]
  (let [request (:request lowering)]
    {:artifact :gravity/b1-verified-backend-input-packet
     :schema-version 1 :status :accepted-for-bounded-wasm
     :input (:input request)
     :bounded-lowering-payload (:bounded-lowering-payload lowering)
     :profile (:profile-contract request)
     :target (:target request)
     :source-target-selection (:source-target-selection request)
     :target-kind :core-module :memory-width :wasm32 :features #{}
     :abi (:abi request) :runtime (:runtime request)
     :providers (:providers request)
     :effects (:effects request) :capabilities (:capabilities request)
     :safety (:safety request) :proofs (:proofs request)
     :proof-to-target-metadata (:proof-to-target-metadata request)
     :source-map (:source-map request)
     :dependencies (:dependency-provenance request)
     :contract-bindings (:contract-bindings request)
     :c14-eligibility (:eligibility lowering)
     :eligibility
     {:artifact :gravity/b1-backend-eligibility-report
      :backend :gravity.backend/wasm
      :input-artifact (get-in request [:input :artifact-id])
      :profile (:profile request) :target (:target request)
      :accepted? true :rejections [] :fallbacks []
      :missing-evidence [] :unsupported-operations [] :remediation []
      :checks
      [:profile-backend-compatibility :target-feature-support
       :runtime-availability-or-no-runtime-proof
       :abi-representability :layout-representability
       :provider-availability :effect-preservation
       :capability-preservation :safety-bundle-completeness
       :proof-validity-for-target-assumptions
       :source-debug-map-preservation :wasm32-core-module-closure]}
     :backend-manifest backend-manifest
     :unsupported-feature-report
     {:status :bounded-surface-only
      :policy (get-in request [:unsupported-feature-report :policy])
      :diagnostic "B1-UNSUPPORTED" :fallback-status :rejected}
     :diagnostics [] :semantic-authority :gravity-source
     :execution-tcb :clojure-stage0-rule-runner
     :clojure-seed-boundary? true
     :whole-b1? false :whole-b4? false :self-hosted? false}))

(defn- p15-s23-b1-wasm-build!
  [candidate source-path c11-artifact c13-record c14-record binding]
  (let [manifest (p15-s23-b1-wasm-backend-manifest c14-record)
        raw
        (p15-s23-c13-c14-b1-invoke!
         candidate source-path binding p15-s23-b1-wasm-builder-function
         [c14-record manifest] "B1-INPUT")
        expected (p15-s23-b1-wasm-expected-record c14-record manifest)]
    (p15-s23-c11-mir-require-strict-structure!
     source-path expected raw :independent-b1-wasm-packet-reconstruction)
    (when-not
     (and (= expected raw)
          (= (:artifact-id c14-record)
             (get-in raw [:backend-manifest :c14-artifact-id]))
          (= (get-in c14-record [:request :request-id])
             (get-in raw [:backend-manifest :c14-request-id]))
          (= (:artifact-id c13-record)
             (get-in raw [:input :artifact-id])
             (get-in raw [:bounded-lowering-payload :c13-artifact-id]))
          (= (:artifact-id c11-artifact)
             (get-in raw [:input :c11-artifact-id])
             (get-in raw [:bounded-lowering-payload :c11-artifact-id]))
          (= (:optimized-mir c13-record)
             (get-in raw [:bounded-lowering-payload :mir])))
      (p15-s23-b4-wasm-fail!
       "B1-METADATA" source-path raw
       {:missing-fact :exact-c14-bound-b1-wasm-reconstruction}))
    (p15-s23-c13-c14-b1-seal-stage
     :gravity/b1-verified-backend-input-packet raw
     (p15-s23-c13-c14-b1-source-rule
      :gravity.backend/b1-backend-interface binding
      p15-s23-b1-wasm-builder-function)
     {:source source-path
      :c11-source (get-in c11-artifact
                          [:provenance :actual-paths :c11-source])
      :c13-source (get-in c13-record
                          [:actual-path-provenance :c13-source])
      :c14-source (get-in c14-record
                          [:actual-path-provenance :c14-source])
      :b1-source (:source-path binding)})))

(defn p15-s23-c14-wasm-host-operation-rejection
  [operations operation block-labels]
  (let [base
        (p15-s23-b3-llvm-operation-rejection
         operations operation block-labels)]
    (if (= base nil)
      (if (and (= :constant (:opcode operation))
               (= :gravity/integer (:type operation))
               (not (<= Integer/MIN_VALUE
                        (get-in operation [:constant-payload :value])
                        Integer/MAX_VALUE)))
        :bounded-scalar-constant-payload
        nil)
      base)))

(defn p15-s23-c13-c14-b1-wasm-preflight!
  [source-path c11-artifact]
  (let [mir (:mir-module c11-artifact)
        function (get-in mir [:functions 'main])
        block-order
        (when (map? function)
          (p15-s23-b3-llvm-block-order mir function))
        operations
        (when (map? function)
          (p15-s23-b3-llvm-operation-sequence function block-order))
        block-labels (p15-s23-b3-llvm-block-labels block-order)]
    (when-not
     (and (= :gravity/p15-s23-c11-authenticated-mir-artifact
             (:kind c11-artifact))
          (= :passed (:verification-status mir))
          (= :verified-mir-candidate-for-b1
             (get-in c11-artifact [:b1-preflight :status]))
          (true? (:target-independent? mir))
          (= :hosted (:profile mir)) (= :jvm (:source-target mir))
          (= :wasm (:target-request mir))
          (= #{'main} (set (keys (:functions mir))))
          (map? function) (= [] (:params function))
          (contains? #{1 4} (count (:blocks function)))
          (empty? (:latent-effects function))
          (empty? (:capabilities function))
          (empty? (:runtime-check-table mir))
          (empty? (:domain-anchors mir)) (empty? (:globals mir))
          (empty? (:diagnostics mir)) (vector? operations)
          (<= 1 (count operations) 127)
          (every? empty? (map :effects operations))
          (every? empty? (map :capabilities operations)))
      (p15-s23-b4-wasm-fail!
       "B1-INPUT" source-path c11-artifact
       {:missing-fact :verified-pure-c11-wasm-backend-input
        :requested-target (:target-request mir)
        :c11-mir-id (:mir-id c11-artifact)}))
    (when-let [operation
               (first
                (filter
                 #(p15-s23-c14-wasm-host-operation-rejection
                   operations % block-labels)
                 operations))]
      (p15-s23-b4-wasm-fail!
       "C14-UNSUPPORTED" source-path operation
       {:missing-fact
        (p15-s23-c14-wasm-host-operation-rejection
         operations operation block-labels)
        :operation-id (:op-id operation) :opcode (:opcode operation)
        :source-operation (:source-operation operation)
        :observed-type (:type operation) :c11-mir-id (:mir-id c11-artifact)}))
    (let [values (p15-s23-c14-c-evaluate-operations operations)
          return-id
          (first (get-in function
                         [:blocks (last block-order)
                          :terminator :operands]))
          result (get values return-id)
          wasm-result (cond (= true result) 1 (= false result) 0
                            (nil? result) 0 :else result)]
      (when-not (and (contains? values return-id)
                     (integer? wasm-result)
                     (<= Integer/MIN_VALUE wasm-result Integer/MAX_VALUE))
        (p15-s23-b4-wasm-fail!
         "C14-UNSUPPORTED" source-path
         (or (get (into {} (map (juxt :op-id identity)) operations)
                  return-id) {})
         {:missing-fact :wasm-result-outside-signed-i32
          :operation-id return-id
          :observed-type
          (:type (get (into {} (map (juxt :op-id identity)) operations)
                      return-id))
          :c11-mir-id (:mir-id c11-artifact)}))
      {:mir mir :function function :block-order block-order
       :block-labels block-labels :operations operations
       :semantic-result wasm-result})))

(def p15-s23-c13-c14-b1-wasm-final-packet-keys
  p15-s23-c13-c14-b1-final-packet-keys)

(def p15-s23-c13-c14-b1-wasm-final-packet-scope
  {:bounded-wasm? true :target-kind :core-module
   :whole-c13? false :whole-c14? false :whole-b1? false
   :whole-b4? false :public? false :release? false :self-hosted? false})

(defn- p15-s23-c13-c14-b1-wasm-final-record
  [source-path c11-artifact checked-core c11-report bindings
   c13-record c14-record b1-record]
  (let [provenance
        {:source source-path
         :c11-source (get-in c11-artifact
                             [:provenance :actual-paths :c11-source])
         :c13-source (get-in bindings [:c13 :source-path])
         :c14-source (get-in bindings [:c14 :source-path])
         :b1-source (get-in bindings [:b1 :source-path])}
        base
        {:kind :gravity/p15-s23-c13-c14-b1-wasm-authenticated-packet
         :schema-version 1 :status :accepted-for-bounded-wasm
         :c11 {:artifact-id (:artifact-id c11-artifact)
               :mir-id (:mir-id c11-artifact)
               :module-id (get-in c11-artifact [:mir-module :module-id])
               :checked-core-artifact-id (:artifact-id checked-core)
               :verifier-record
               (p15-s23-b3-llvm-c11-verifier-record c11-report)}
         :c13 c13-record :c14 c14-record :b1 b1-record
         :optimized-mir (:optimized-mir c13-record)
         :actual-path-provenance provenance :diagnostics []
         :semantic-authority :gravity-source
         :verification-tcb :clojure-stage0-independent-reconstruction
         :scope p15-s23-c13-c14-b1-wasm-final-packet-scope}
        semantic-id (p15-s23-c13-c14-b1-semantic-id base)]
    (assoc base :semantic-id semantic-id
           :artifact-id
           (p15-s23-c11-mir-digest
            {:kind (:kind base) :schema-version 1 :semantic-id semantic-id})
           :actual-path-binding-id
           (p15-s23-c13-c14-b1-actual-path-binding-id
            semantic-id provenance)))))
