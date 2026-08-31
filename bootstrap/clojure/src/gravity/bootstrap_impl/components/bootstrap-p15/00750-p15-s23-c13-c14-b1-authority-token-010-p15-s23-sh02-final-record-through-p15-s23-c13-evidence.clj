(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn- p15-s23-sh02-final-record
  [packet binding source-path workspace-root invocation-root envelopes]
  (let [actual-path-provenance
        {:source source-path
         :workspace-root workspace-root
         :invocation-root invocation-root
         :envelope-source (:source-path binding)
         :c13-source (get-in packet
                             [:c13 :actual-path-provenance :c13-source])
         :b1-source (get-in packet
                            [:b1 :actual-path-provenance :b1-source])}
        base
        {:artifact :gravity/sh02-reusable-authenticated-envelopes
         :schema-version 1 :status :accepted
         :packet-id (:artifact-id packet)
         :packet-semantic-id (:semantic-id packet)
         :envelopes envelopes
         :source-rule (p15-s23-sh02-source-rule binding)
         :actual-path-provenance actual-path-provenance
         :diagnostics []
         :semantic-authority :gravity-source
         :host-tcb
         {:boundary :clojure-stage0
          :responsibilities
          [:bound-carriers :authenticate-gravity-source
           :canonical-encode :sha256 :resolve-request-dag
           :instantiate-template :fresh-contextual-replay]
          :release-signature? false
          :verifier-correctness-proof? false}
         :scope p15-s23-sh02-final-scope}
        semantic-id (p15-s23-sh02-final-semantic-id base)]
    (assoc
     base
     :semantic-id semantic-id
     :artifact-id (p15-s23-sh02-final-artifact-id semantic-id)
     :actual-path-binding-id
     (p15-s23-sh02-final-actual-path-binding-id
      semantic-id actual-path-provenance))))

(defn- p15-s23-sh02-build-verified-packet-internal!
  [candidate packet context]
  (let [source-path (p15-s23-c11-ingress-source-path context)]
    (p15-s23-c13-c14-b1-require-authority!
     candidate source-path :construct-sh02-authenticated-envelopes)
    (let [binding (p15-s23-sh02-source-binding! candidate source-path)
          workspace-root (p15-s23-sh02-workspace-root candidate source-path)
          invocation-root (p15-s23-sh02-invocation-root)
          descriptors
          (into
           (sorted-map)
           (map
            (fn [stage]
              [stage
               (p15-s23-sh02-authenticated-envelope-descriptor
                stage packet workspace-root invocation-root)]))
           [:c13 :b1])
          envelopes
          (into
           (sorted-map)
           (map
            (fn [stage]
              [stage
               (p15-s23-sh02-build-stage-envelope!
                candidate stage packet (get descriptors stage)
                binding source-path)]))
           [:c13 :b1])]
      (p15-s23-sh02-final-record
       packet binding source-path workspace-root invocation-root
       envelopes))))

(defn- p15-s23-sh02-build-internal!
  [candidate packet checked-core context]
  (let [source-path (p15-s23-c11-ingress-source-path context)]
    (p15-s23-c13-c14-b1-require-authority!
     candidate source-path :verify-sh02-stage-packet)
    (let [packet-report
          (p15-s23-stage2-c13-c14-b1-verification-report
           packet checked-core context)]
      (when-not (= :passed (:status packet-report))
        (p15-s23-sh02-fail!
         source-path packet :fresh-sh02-stage-packet-replay {}))
      (p15-s23-sh02-build-verified-packet-internal!
       candidate packet context))))

(defn p15-s23-c13-evidence-for-target
  [c11-artifact c11-report target]
  (let [mir (:mir-module c11-artifact)
        verifier-record
        (select-keys c11-report
                     [:status :mir-id :semantic-replay-parity
                      :execution-tcb :independent-verifier :b1-preflight])
        verifier-record-id (p15-s23-c11-mir-digest verifier-record)
        operation-order
        (mapv :op-id (p15-s23-c11-mir-operation-sequence mir))
        fact-bindings
        {:type (p15-s23-c13-c14-b1-content-binding (:type-table mir))
         :effect (p15-s23-c13-c14-b1-content-binding (:effect-table mir))
         :ownership
         (p15-s23-c13-c14-b1-content-binding (:ownership-table mir))
         :capability
         (p15-s23-c13-c14-b1-content-binding (:capability-table mir))
         :safety (p15-s23-c13-c14-b1-content-binding (:safety-table mir))
         :runtime-checks
         (p15-s23-c13-c14-b1-content-binding (:runtime-check-table mir))
         :proofs
         (p15-s23-c13-c14-b1-content-binding
          (:proof-certificate-table mir))
         :source-map
         (p15-s23-c13-c14-b1-content-binding (:source-map mir))}
        base
        {:c11-artifact-id (:artifact-id c11-artifact)
         :c11-mir-id (:mir-id c11-artifact)
         :module-id (:module-id mir)
         :source-core (:source-core mir)
         :verifier-report-id verifier-record-id
         :verifier-status (:status c11-report)
         :semantic-replay-parity (:semantic-replay-parity c11-report)
         :pass-execution-record-id
         (get-in mir [:pass-execution-record :record-id])
         :fact-bindings fact-bindings
         :runtime-check-inventory (:runtime-check-table mir)
         :source-map-binding (:source-map fact-bindings)
         :operation-order operation-order
         :effect-order-graph (:effect-order-graph mir)
         :profile :hosted
         :target target}]
    (assoc base :decision-id
           (p15-s23-c11-mir-digest
            {:kind :gravity/c13-bounded-identity-decision
             :evidence base}))))

(defn p15-s23-c13-evidence
  [c11-artifact c11-report]
  ;; LLVM ingress is target-qualified at C11; retain that canonical identity
  ;; through the optimization evidence and invalidation ledger.  The C14
  ;; request may still carry its historical internal :request :llvm marker.
  (p15-s23-c13-evidence-for-target
   c11-artifact c11-report :llvm-x86_64-linux)))
