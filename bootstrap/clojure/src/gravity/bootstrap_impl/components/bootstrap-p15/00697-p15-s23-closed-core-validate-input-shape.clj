

(defn p15-s23-closed-core-validate-input-shape!
  ([source-path artifact]
   (p15-s23-closed-core-validate-input-shape! source-path artifact :pure))
  ([source-path artifact mode]
  (let [expected-status
        (if (= :effectful-reference mode)
          :complete-for-authenticated-hosted-jvm-reference-interpreter-slice
          :complete-for-pure-closed-slice)]
  (when-not (and (map? artifact)
                 (= p15-s23-closed-core-artifact-keys
                    (set (keys artifact)))
                 (= :gravity/p15-s23-stage2-closed-checked-core-artifact
                    (:kind artifact))
                 (= expected-status (:status artifact))
                 (if (= :effectful-reference mode)
                   (= :effectful-reference
                      (get-in artifact [:source-core-input :mode]))
                   (not (contains? (:source-core-input artifact) :mode))))
    (p15-s23-closed-core-fail!
     "C6-CORE-SHAPE" source-path {:missing-fact :closed-core-artifact-schema}
     {:missing-fact :exact-closed-core-artifact-schema}))
  (let [nodes (:core-nodes artifact)]
    (when-not (vector? nodes)
      (p15-s23-closed-core-fail!
       "C6-VERIFY" source-path {:missing-fact :closed-core-node-vector}
       {:missing-fact :typed-closed-core-node-vector}))
    (when-not (every? #(and (map? %)
                            (= p15-s23-closed-core-node-keys
                               (set (keys %))))
                      nodes)
      (p15-s23-closed-core-fail!
       "C6-CORE-SHAPE" source-path {:missing-fact :closed-core-node-schema}
       {:missing-fact :exact-closed-core-node-schema}))
    (when-not
     (every?
      (fn [node]
        (and (string? (:node-id node))
             (vector? (:path node))
             (keyword? (:kind node))
             (keyword? (:source-operation node))
             (boolean? (:plan-node? node))
             (integer? (:plan-depth node))
             (not (neg? (:plan-depth node)))
             (vector? (:operands node))
             (every? string? (:operands node))
             (map? (:attributes node))
             (map? (:ownership node))
             (map? (:safety node))
             (keyword? (:profile node))))
      nodes)
      (p15-s23-closed-core-fail!
       "C6-VERIFY" source-path {:missing-fact :typed-core-node-fields}
       {:missing-fact :typed-bounded-closed-core-node-fields}))
    (when-not (every? #(set? (:effects %)) nodes)
      (p15-s23-closed-core-fail!
       "C8-VERIFY" source-path {:missing-fact :typed-node-effects}
       {:missing-fact :closed-core-node-effect-set}))
    (when-not (every? #(set? (:capabilities %)) nodes)
      (p15-s23-closed-core-fail!
       "C8-VERIFY" source-path {:missing-fact :typed-node-capabilities}
       {:missing-fact :closed-core-node-capability-set}))
    (when-not (every? #(and (map? (:source %))
                            (string? (get-in % [:source :origin-id])))
                      nodes)
      (p15-s23-closed-core-fail!
       "C6-ORIGIN" source-path {:missing-fact :typed-node-source-origin}
       {:missing-fact :closed-core-node-origin-record})))
  (when-not
   (and (string? (:artifact-id artifact))
        (string? (:mapping-id artifact))
        (string? (:provenance-binding-id artifact))
        (string? (:actual-path-binding-id artifact))
        (string? (:source-content-hash artifact))
        (map? (:scope artifact))
        (map? (:source-core-input artifact))
        (map? (:target-request-metadata artifact))
        (vector? (:root-node-ids artifact))
        (every? string? (:root-node-ids artifact))
        (every? map? (map artifact
                          [:type-facts :effect-facts :capability-facts
                           :ownership-facts :safety-facts :profile-facts
                           :dependency-order-graph :source-origin-table
                           :origin-closure :authenticated-input :bounds
                           :provenance :instruction-origin-sidecar
                           :typed-core :effect-graph
                           :pure-capability-closure
                           :ownership-analysis]))
        (vector? (:capability-proof-records artifact))
        (vector? (:lexical-binding-records artifact))
        (vector? (:pass-history artifact))
        (vector? (:diagnostics artifact))
        (set? (get-in artifact [:source-core-input :declared-effects]))
        (set? (get-in artifact
                      [:source-core-input :declared-capabilities]))
        (vector? (get-in artifact [:source-core-input :declared-exports]))
        (every? symbol?
                (get-in artifact [:source-core-input :declared-exports]))
        (contains? #{:public :private :stage2-local}
                   (get-in artifact
                           [:source-core-input :entrypoint-visibility])))
    (p15-s23-closed-core-fail!
     "C6-VERIFY" source-path {:missing-fact :typed-artifact-fields}
     {:missing-fact :typed-closed-core-artifact-fields}))
  (let [bounds (:bounds artifact)
        bounded-fields
        [:maximum-plan-nodes :maximum-plan-depth :maximum-derived-nodes
         :maximum-source-bytes :maximum-artifact-scalar-bytes
         :maximum-integer-bits :observed-source-bytes
         :observed-plan-nodes :observed-plan-depth :observed-derived-nodes]]
    (when-not (every? #(let [value (get bounds %)]
                         (and (integer? value) (not (neg? value))))
                      bounded-fields)
      (p15-s23-closed-core-fail!
       "C6-VERIFY" source-path {:missing-fact :typed-artifact-bounds}
       {:missing-fact :nonnegative-integer-closed-core-bounds})))
  :passed)))