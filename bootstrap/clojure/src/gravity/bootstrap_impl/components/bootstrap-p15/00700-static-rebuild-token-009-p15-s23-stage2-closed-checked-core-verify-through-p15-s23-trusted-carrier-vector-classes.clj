(let [static-rebuild-token (nth __gravity_bootstrap_checked_core_authority_values 0)
      p15-s23-stage2-closed-checked-core-source-artifact-internal __gravity_bootstrap_checked_core_source_helper
      p15-s23-stage2-closed-checked-core-rebuild-internal __gravity_bootstrap_checked_core_rebuild_helper]
(defn p15-s23-stage2-closed-checked-core-verify!*
  [artifact context]
  (p15-s23-checked-core-bounded-context! context)
  ;; Use the iterative closed-artifact walker here so the public verifier
  ;; preserves the precise C6 bound that failed.  Generic runtime-carrier
  ;; containment would collapse every hostile artifact into one opaque ingress
  ;; error and lose the established checked-core diagnostic contract.
  (p15-s23-closed-core-bounded-value! "<closed-core>" artifact)
  (let [source-path (or (:source-path context)
                        (get-in artifact [:provenance :actual-paths :source])
                        "<closed-core>")
        mode
        (p15-s23-stage2-closed-checked-core-context-mode context)
        _ (when (= :invalid mode)
            (p15-s23-closed-core-fail!
             "C6-CORE-SHAPE" source-path context
             {:missing-fact :exact-bounded-closed-core-verification-context}))
        effectful? (= :effectful-reference mode)
        authority-record (when effectful? (:authority-record context))
        _ (when (and effectful?
                     (not
                      (p15-s23-checked-core-authority-record-valid?
                       authority-record)))
            (p15-s23-closed-core-fail!
             "C8-CAPABILITY" source-path context
             {:missing-fact
              :integral-typed-fourth-verification-authority-record}))
        expected-authority-evidence
        (when effectful?
          (p15-s23-checked-core-authority-evidence authority-record))
        execution-evidence
        (when effectful?
          (get-in artifact
                  [:authenticated-input :reference-execution-evidence]))]
    ;; Structural bounds and independent local fact reconstruction happen
    ;; before any whole-artifact canonical comparison.
    (p15-s23-closed-core-validate-structure!
     source-path artifact mode expected-authority-evidence)
    (let [authorized-rebuild
          (if effectful?
            (p15-s23-authorized-checked-core-verification-rebuild
             artifact context expected-authority-evidence
             execution-evidence source-path)
            {:verification-replay nil
             :fresh
             (p15-s23-stage2-closed-checked-core-rebuild context)})
          verification-replay (:verification-replay authorized-rebuild)
          fresh (:fresh authorized-rebuild)
          nodes (:core-nodes artifact)
          fresh-nodes (:core-nodes fresh)
          node-projection
          (fn [records fields]
            (mapv #(select-keys % fields) records))
          diagnostic-module
          {:module (get-in artifact [:source-core-input :module])
           :profile (:profile artifact)
           :target (:source-target artifact)
           :requested-target
           (get-in artifact [:target-request-metadata :requested-target])
           :safety (get-in artifact [:source-core-input :declared-safety])}
          first-node (or (first nodes) {})
          enriched
          (fn [node extra]
            (p15-s23-closed-core-enriched-node-subject
             artifact node diagnostic-module extra))
          first-mismatch
          (fn [fields]
            (or (first
                 (map first
                      (filter
                       (fn [[left right]]
                         (not= (select-keys left fields)
                               (select-keys right fields)))
                       (map vector nodes fresh-nodes))))
                first-node))]
      (when-not
       (= (select-keys artifact
                       [:kind :status :scope :source-content-hash
                        :source-core-input :entrypoint :profile
                        :source-target :target-request-metadata
                        :root-node-ids :pass-history
                        :authenticated-input :bounds :mir-derived?
                        :whole-language? :clojure-seed-boundary?
                        :self-hosted? :provenance])
          (select-keys fresh
                       [:kind :status :scope :source-content-hash
                        :source-core-input :entrypoint :profile
                        :source-target :target-request-metadata
                        :root-node-ids :pass-history
                        :authenticated-input :bounds :mir-derived?
                        :whole-language? :clojure-seed-boundary?
                        :self-hosted? :provenance]))
        (p15-s23-closed-core-fail!
         "C6-CORE-SHAPE" source-path
         (enriched first-node
                   {:missing-fact
                    :fresh-source-packet-front-end-core-binding})
         {:missing-fact :fresh-source-packet-front-end-core-binding}))
      (when-not
       (= (node-projection nodes
                           [:node-id :path :kind :source-operation
                            :plan-node? :plan-depth :operands :attributes])
          (node-projection fresh-nodes
                           [:node-id :path :kind :source-operation
                            :plan-node? :plan-depth :operands :attributes]))
        (p15-s23-closed-core-fail!
         "C6-VERIFY" source-path
         (enriched
          (first-mismatch
           [:node-id :path :kind :source-operation :plan-node?
            :plan-depth :operands :attributes])
          {:missing-fact :fresh-structural-and-dependency-order-parity})
         {:missing-fact :fresh-structural-and-dependency-order-parity}))
      (when-not (= (node-projection nodes [:node-id :type])
                   (node-projection fresh-nodes [:node-id :type]))
        (p15-s23-closed-core-fail!
         "C7-VERIFY" source-path
         (let [node (first-mismatch [:node-id :type])]
           (enriched
            node {:missing-fact :fresh-type-parity
                  :expected-type
                  (:type (first (filter #(= (:node-id node) (:node-id %))
                                        fresh-nodes)))
                  :actual-type (:type node)
                  :relevant-binding-id
                  (or (get-in node [:attributes :resolved-binding])
                      :not-applicable)}))
         {:missing-fact :fresh-type-parity}))
      (when-not (= (node-projection nodes [:node-id :effects])
                   (node-projection fresh-nodes [:node-id :effects]))
        (p15-s23-closed-core-fail!
         "C8-VERIFY" source-path
         (enriched (first-mismatch [:node-id :effects])
                   {:missing-fact :fresh-effect-parity})
         {:missing-fact :fresh-effect-parity}))
      (when-not (= (node-projection nodes [:node-id :capabilities])
                   (node-projection fresh-nodes [:node-id :capabilities]))
        (p15-s23-closed-core-fail!
         "C8-VERIFY" source-path
         (enriched (first-mismatch [:node-id :capabilities])
                   {:missing-fact :fresh-capability-parity})
         {:missing-fact :fresh-capability-parity}))
      (when-not (= (node-projection nodes [:node-id :safety])
                   (node-projection fresh-nodes [:node-id :safety]))
        (p15-s23-closed-core-fail!
         "C10-PROOF" source-path
         (enriched (first-mismatch [:node-id :safety])
                   {:missing-fact :fresh-safety-parity})
         {:missing-fact :fresh-safety-parity}))
      (when-not
       (= (node-projection nodes [:node-id :ownership :profile])
          (node-projection fresh-nodes [:node-id :ownership :profile]))
        (p15-s23-closed-core-fail!
         "C6-VERIFY" source-path
         (enriched (first-mismatch [:node-id :ownership :profile])
                   {:missing-fact :fresh-ownership-profile-parity})
         {:missing-fact :fresh-ownership-profile-parity}))
      (when-not
       (= (node-projection nodes [:node-id :source])
          (node-projection fresh-nodes [:node-id :source]))
        (p15-s23-closed-core-fail!
         "C6-ORIGIN" source-path
         (enriched (first-mismatch [:node-id :source])
                   {:missing-fact :fresh-node-origin-parity})
         {:missing-fact :fresh-node-origin-parity}))
      (when-not
       (= (select-keys artifact
                       [:mapping-id :source-origin-table :origin-closure])
          (select-keys fresh
                       [:mapping-id :source-origin-table :origin-closure]))
        (p15-s23-closed-core-fail!
         "C6-ORIGIN" source-path
         (enriched first-node
                   {:missing-fact :fresh-c2-c3-origin-closure-parity})
         {:missing-fact :fresh-c2-c3-origin-closure-parity}))
      (when-not
       (= (select-keys artifact
                       [:type-facts :effect-facts :capability-facts
                        :ownership-facts :safety-facts :profile-facts
                        :typed-core :effect-graph
                        :capability-proof-records
                        :pure-capability-closure :ownership-analysis
                        :dependency-order-graph :lexical-binding-records])
          (select-keys fresh
                       [:type-facts :effect-facts :capability-facts
                        :ownership-facts :safety-facts :profile-facts
                        :typed-core :effect-graph
                        :capability-proof-records
                        :pure-capability-closure :ownership-analysis
                        :dependency-order-graph :lexical-binding-records]))
        (p15-s23-closed-core-fail!
         "C6-VERIFY" source-path
         (enriched first-node
                   {:missing-fact :fresh-c6-c10-fact-bundle-parity})
         {:missing-fact :fresh-c6-c10-fact-bundle-parity}))
      (when-not (= (:artifact-id artifact) (:artifact-id fresh))
        (p15-s23-closed-core-fail!
         "C6-VERIFY" source-path
         (enriched first-node
                   {:missing-fact :fresh-semantic-artifact-id-parity})
         {:missing-fact :fresh-semantic-artifact-id-parity}))
      {:status :passed
       :mode mode
       :verification-replay-record
       (:replay-record verification-replay)
       :actual-path-context
       (:actual-path-context verification-replay)})))

(def p15-s23-trusted-carrier-map-classes
  #{"clojure.lang.PersistentArrayMap"
    "clojure.lang.PersistentHashMap"})

(def p15-s23-trusted-carrier-vector-classes
  #{"clojure.lang.PersistentVector"}))
