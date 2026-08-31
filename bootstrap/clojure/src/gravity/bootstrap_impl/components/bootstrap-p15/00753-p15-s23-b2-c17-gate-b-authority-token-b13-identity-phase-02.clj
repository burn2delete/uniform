(defn- __gravity_bootstrap_gate_b_b13_identity_phase_02 [state]
  (let [{:syms
         [gate-a
          transaction
          b14
          c18
          files
          content-hashes
          pass-provenance
          pass-pipeline-digest
          compiler-provenance
          dependency-provenance
          build-identity-base
          build-id
          build-identity
          target-common
          target-fingerprint-id
          source-provenance
          compiler-provenance-id
          dependency-provenance-id]} state
        artifact-files (into
                         (sorted-map)
                         (map
                           (fn [[kind record]] [kind
                                                (assoc
                                                  record
                                                  :schema-version
                                                  1
                                                  :kind
                                                  (:artifact-kind record)
                                                  :backend
                                                  :gravity.backend/c
                                                  :profile
                                                  :hosted
                                                  :target
                                                  target-fingerprint-id
                                                  :source-provenance
                                                  (p15-s23-c11-mir-digest
                                                    source-provenance)
                                                  :compiler-provenance
                                                  compiler-provenance-id
                                                  :dependency-provenance
                                                  dependency-provenance-id
                                                  :build-identity
                                                  build-id
                                                  :bundle-build-id
                                                  build-id)]))
                         files)
        edge (fn [from to edge-name pass generator]
               (let [base {:input-digest from,
                           :edge edge-name,
                           :invalidation-rules
                           #{:generator-change :target-change :input-digest-change},
                           :to-digest to,
                           :output-digest to,
                           :from from,
                           :from-digest from,
                           :generator generator,
                           :pass pass,
                           :target target-fingerprint-id,
                           :profile :hosted,
                           :to to}]
                 (assoc base :edge-digest (p15-s23-c11-mir-digest base))))
        source-node (p15-s23-b2-c17-gate-b-neutral-content-id
                      (get-in gate-a [:source-debug-map :source-map-id]))
        origin-node (p15-s23-b2-c17-gate-b-neutral-content-id
                      (:source-debug-map gate-a))
        checked-core-node (get-in gate-a [:verified-input-closure :source-core])
        c11-node (get-in gate-a [:input-bindings :c11-artifact-id])
        c13-node (get-in gate-a [:input-bindings :c13-artifact-id])
        c14-node (get-in gate-a [:backend-manifest :c14-artifact-id])
        b1-node (get-in gate-a [:input-bindings :b1-artifact-id])]
    (assoc
      state
      'artifact-files
      artifact-files
      'edge
      edge
      'source-node
      source-node
      'origin-node
      origin-node
      'checked-core-node
      checked-core-node
      'c11-node
      c11-node
      'c13-node
      c13-node
      'c14-node
      c14-node
      'b1-node
      b1-node)))
