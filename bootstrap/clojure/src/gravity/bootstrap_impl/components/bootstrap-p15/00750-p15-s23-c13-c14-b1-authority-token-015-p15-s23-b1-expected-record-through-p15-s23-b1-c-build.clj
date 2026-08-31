(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn p15-s23-b1-expected-record
  [lowering backend-manifest]
  (let [request (:request lowering)]
    {:artifact :gravity/b1-verified-backend-input-packet
     :schema-version 1 :status :accepted-for-bounded-llvm
     :input (:input request)
     :profile (:profile-contract request)
     :target (:target request)
     :source-target-selection (:source-target-selection request)
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
      :backend :gravity.backend/llvm
      :input-artifact (get-in request [:input :artifact-id])
      :profile (:profile request) :target (:target request)
      :accepted? true :rejections [] :fallbacks []
      :missing-evidence [] :unsupported-operations [] :remediation []
      :checks [:profile-backend-compatibility :target-feature-support
               :runtime-availability-or-no-runtime-proof
               :abi-representability :layout-representability
               :provider-availability :effect-preservation
               :capability-preservation :safety-bundle-completeness
               :proof-validity-for-target-assumptions
               :source-debug-map-preservation]}
     :backend-manifest backend-manifest
     :unsupported-feature-report
     {:status :bounded-surface-only
      :policy (get-in request [:unsupported-feature-report :policy])
      :diagnostic "B1-UNSUPPORTED" :fallback-status :rejected}
     :diagnostics [] :semantic-authority :gravity-source
     :execution-tcb :clojure-stage0-rule-runner
     :clojure-seed-boundary? true :self-hosted? false}))

(defn- p15-s23-b1-build!
  [candidate source-path c11-artifact c13-record c14-record binding]
  (let [manifest (p15-s23-b1-backend-manifest c14-record)
        raw
        (p15-s23-c13-c14-b1-invoke!
         candidate source-path binding p15-s23-b1-builder-function
         [c14-record manifest] "B1-INPUT")
        expected (p15-s23-b1-expected-record c14-record manifest)]
    (p15-s23-c11-mir-require-strict-structure!
     source-path expected raw :independent-b1-packet-reconstruction)
    (when-not
     (and (= expected raw)
          (= (:artifact-id c14-record)
             (get-in raw [:backend-manifest :c14-artifact-id]))
          (= (get-in c14-record [:request :request-id])
             (get-in raw [:backend-manifest :c14-request-id]))
          (= (:artifact-id c13-record)
             (get-in raw [:input :artifact-id])))
      (p15-s23-b3-llvm-fail!
       "B1-METADATA" source-path raw
       {:missing-fact :exact-c14-bound-b1-reconstruction}))
    (p15-s23-c13-c14-b1-seal-stage
     :gravity/b1-verified-backend-input-packet raw
     (p15-s23-c13-c14-b1-source-rule
      :gravity.backend/b1-backend-interface binding
      p15-s23-b1-builder-function)
     {:source source-path
      :c11-source (get-in c11-artifact
                          [:provenance :actual-paths :c11-source])
      :c13-source (get-in c13-record
                          [:actual-path-provenance :c13-source])
      :c14-source (get-in c14-record
                          [:actual-path-provenance :c14-source])
      :b1-source (:source-path binding)})))

(declare p15-s23-b1-c-builder-function)

(defn p15-s23-b1-c-backend-manifest
  [c14-record]
  {:artifact :gravity/backend-manifest
   :backend :gravity.backend/c
   :version :bounded-authenticated-c17-v1
   :accepts [:gravity/mir]
   :emits [:c-source :c-header]
   :requires [:profile-manifest :target-manifest :c-dialect-selection
              :abi-policy :runtime-provider-selection :effect-summary
              :capability-proof-summary :safety-bundle :proof-table
              :source-map :dependency-graph]
   :supports-profiles [:hosted]
   :rejects [:unverified-ir :unsupported-op :missing-proof
             :implicit-ub :ambient-capability :profile-violation
             :dialect-mismatch]
   :dialect :c17
   :c14-request-id (get-in c14-record [:request :request-id])
   :c14-artifact-id (:artifact-id c14-record)})

(defn p15-s23-b1-c-expected-record
  [lowering backend-manifest]
  (let [request (:request lowering)]
    {:artifact :gravity/b1-verified-backend-input-packet
     :schema-version 1 :status :accepted-for-bounded-c
     :input (:input request)
     :bounded-lowering-payload (:bounded-lowering-payload lowering)
     :profile (:profile-contract request)
     :target (:target request)
     :source-target-selection (:source-target-selection request)
     :dialect :c17
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
      :backend :gravity.backend/c
      :input-artifact (get-in request [:input :artifact-id])
      :profile (:profile request) :target (:target request)
      :accepted? true :rejections [] :fallbacks []
      :missing-evidence [] :unsupported-operations [] :remediation []
      :checks [:profile-backend-compatibility :target-feature-support
               :runtime-availability-or-no-runtime-proof
               :abi-representability :layout-representability
               :provider-availability :effect-preservation
               :capability-preservation :safety-bundle-completeness
               :proof-validity-for-target-assumptions
               :source-debug-map-preservation :c17-dialect-closure]}
     :backend-manifest backend-manifest
     :unsupported-feature-report
     {:status :bounded-surface-only
      :policy (get-in request [:unsupported-feature-report :policy])
      :diagnostic "B1-UNSUPPORTED" :fallback-status :rejected}
     :diagnostics [] :semantic-authority :gravity-source
     :execution-tcb :clojure-stage0-rule-runner
     :clojure-seed-boundary? true
     :whole-b1? false :whole-b2? false :self-hosted? false}))

(defn- p15-s23-b1-c-build!
  [candidate source-path c11-artifact c13-record c14-record binding]
  (let [manifest (p15-s23-b1-c-backend-manifest c14-record)
        raw
        (p15-s23-c13-c14-b1-invoke!
         candidate source-path binding p15-s23-b1-c-builder-function
         [c14-record manifest] "B1-INPUT")
        expected (p15-s23-b1-c-expected-record c14-record manifest)]
    (p15-s23-c11-mir-require-strict-structure!
     source-path expected raw :independent-b1-c-packet-reconstruction)
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
      (p15-s23-b3-llvm-fail!
       "B1-METADATA" source-path raw
       {:missing-fact :exact-c14-bound-b1-c-reconstruction}))
    (p15-s23-c13-c14-b1-seal-stage
     :gravity/b1-verified-backend-input-packet raw
     (p15-s23-c13-c14-b1-source-rule
      :gravity.backend/b1-backend-interface binding
      p15-s23-b1-c-builder-function)
     {:source source-path
      :c11-source (get-in c11-artifact
                          [:provenance :actual-paths :c11-source])
      :c13-source (get-in c13-record
                          [:actual-path-provenance :c13-source])
      :c14-source (get-in c14-record
                          [:actual-path-provenance :c14-source])
      :b1-source (:source-path binding)}))))
