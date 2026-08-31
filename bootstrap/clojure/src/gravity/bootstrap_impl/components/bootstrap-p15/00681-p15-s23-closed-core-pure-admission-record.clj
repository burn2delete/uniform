

(defn p15-s23-closed-core-pure-admission-record
  [source-path source-content-hash plan-id module requirements
   authority-present? product]
  (let [declared-effects (:effects module)
        declared-capabilities (:capabilities module)
        required-effects (:required-effects requirements)
        required-capabilities (:required-capabilities requirements)
        nodes (:nodes product)
        function-node (or (first (filter #(= :function (:kind %)) nodes))
                          (last nodes))
        subject-for
        (fn [effect capability]
          (let [node (or (first
                          (filter
                           #(or (and effect
                                     (contains? (:effects %) effect))
                                (and capability
                                     (contains? (:capabilities %)
                                                capability)))
                           nodes))
                         function-node)]
            (p15-s23-closed-core-enriched-node-subject
             product node module
             {:effect (or effect :not-applicable)
              :capability (or capability :not-applicable)
              :provider :none-selected
              :grant :none-selected})))]
    (when-let [unknown-effect
               (first (sort-by pr-str
                               (set/difference declared-effects
                                               p15-s23-closed-core-registered-effects)))]
      (p15-s23-closed-core-fail!
       "C8-UNKNOWN" source-path (subject-for unknown-effect nil)
       {:missing-fact :recognized-pure-slice-effect-label
        :effect unknown-effect
        :registered-effect-count
        (count p15-s23-closed-core-registered-effects)}))
    (when-let [unknown-capability
               (first (sort-by pr-str
                               (set/difference declared-capabilities
                                               p15-s23-closed-core-registered-capabilities)))]
      (p15-s23-closed-core-fail!
       "C8-CAPABILITY" source-path
       (subject-for nil unknown-capability)
       {:missing-fact :recognized-pure-slice-capability-label
        :capability unknown-capability
        :registered-capability-count
        (count p15-s23-closed-core-registered-capabilities)}))
    (when-let [undeclared-effect
               (first (sort-by pr-str
                               (set/difference required-effects
                                               declared-effects)))]
      (p15-s23-closed-core-fail!
       "C8-UNDECLARED" source-path
       (subject-for undeclared-effect nil)
       {:missing-fact :required-effect-declaration
        :effect undeclared-effect
        :declared-effects declared-effects
        :required-effects required-effects}))
    (when-let [undeclared-capability
               (first (sort-by pr-str
                               (set/difference required-capabilities
                                               declared-capabilities)))]
      (p15-s23-closed-core-fail!
       "C8-CAPABILITY" source-path
       (subject-for nil undeclared-capability)
       {:missing-fact :required-capability-declaration
        :capability undeclared-capability
        :declared-capabilities declared-capabilities
        :required-capabilities required-capabilities}))
    (when (or (seq required-effects) (seq required-capabilities)
              (seq declared-effects) (seq declared-capabilities))
      (p15-s23-closed-core-fail!
       "C8-RUNTIME" source-path
       (subject-for
        (first (sort-by pr-str
                        (set/union required-effects declared-effects)))
        (first (sort-by pr-str
                        (set/union required-capabilities
                                   declared-capabilities))))
       {:missing-fact :runtime-module-conformance-residual
        :required-effects required-effects
        :required-capabilities required-capabilities
        :declared-effects declared-effects
        :declared-capabilities declared-capabilities
        :deferred-failure-obligation :profile-defined-panic-or-error
        :runtime-module 'runtime.gravity
        :runtime-artifact-source-content-hash
        p15-s23-stage2-runtime-artifact-expected-source-content-hash
        :runtime-artifact-hash
        p15-s23-stage2-runtime-artifact-expected-artifact-hash
        :provider :none-selected
        :grant :none-selected
        :runtime-module-fix-owned-by-follow-on-slice true
        :authority-present? (boolean authority-present?)
        :authority-inspected? false
        :authority-can-rescue? false}))
    (when authority-present?
      (p15-s23-closed-core-fail!
       "C8-CAPABILITY" source-path (subject-for nil nil)
       {:missing-fact :authority-context-not-consumed-by-pure-slice
        :authority-present? true
        :pure-slice? true
        :authority-inspected? false
        :authority-stored? false}))
    (let [base
          {:kind :gravity/p15-s23-pure-closed-admission-record
           :plan-id plan-id
           :source-content-hash source-content-hash
           :module (:module module)
           :profile (:profile module)
           :source-target (:target module)
           :declared-effects #{}
           :declared-capabilities #{}
           :required-effects #{}
           :required-capabilities #{}
           :authority-required? false
           :authority-consumed? false
           :effectful-runtime-module-status :rejected-before-packet
           :decision :accepted-pure-only
           :status :complete-for-pure-closed-slice
           :target-lowering-credit? false
           :release-credit? false
           :self-hosted? false}]
      (assoc base :admission-id (p15-s23-closed-core-digest base)))))

(defn p15-s23-closed-core-pure-admission-valid?
  [record]
  (and
   (map? record)
   (= #{:kind :plan-id :source-content-hash :module :profile
        :source-target :declared-effects :declared-capabilities
        :required-effects :required-capabilities :authority-required?
        :authority-consumed? :effectful-runtime-module-status :decision
        :status :target-lowering-credit? :release-credit? :self-hosted?
        :admission-id}
      (set (keys record)))
   (= :gravity/p15-s23-pure-closed-admission-record (:kind record))
   (= :accepted-pure-only (:decision record))
   (= :complete-for-pure-closed-slice (:status record))
   (= #{} (:declared-effects record)
          (:declared-capabilities record)
          (:required-effects record)
          (:required-capabilities record))
   (false? (:authority-required? record))
   (false? (:authority-consumed? record))
   (= :rejected-before-packet (:effectful-runtime-module-status record))
   (false? (:target-lowering-credit? record))
   (false? (:release-credit? record))
   (false? (:self-hosted? record))
   (= (:admission-id record)
      (p15-s23-closed-core-digest (dissoc record :admission-id)))))


(defn p15-s23-closed-core-raw-provenance-binding-input
  [raw]
  {:origin-id (:origin-id raw)
   :c2-form-id (:c2-form-id raw)
   :c2-open-token-id (:c2-open-token-id raw)
   :c2-close-token-id (:c2-close-token-id raw)
   :c2-span (p15-s23-closed-core-path-neutral-span (:c2-span raw))
   :c2-surface-span
   (p15-s23-closed-core-path-neutral-span (:c2-surface-span raw))
   :c2-form-kind (:c2-form-kind raw)
   :c2-abbrev (:c2-abbrev raw)
   :c2-reader-generated-origin
   (mapv p15-s23-closed-core-path-neutral-generated-origin
         (:c2-reader-generated-origin raw))
   :c3-source (select-keys (:c3-source raw)
                           [:form-id :token-range :token-id])
   :c3-origin
   (mapv p15-s23-closed-core-path-neutral-generated-origin
         (:c3-origin raw))
   :expanded-generated-origin
   (mapv p15-s23-closed-core-path-neutral-generated-origin
         (:expanded-generated-origin raw))
   :generated-role (:generated-role raw)
   :input-origin-id (:input-origin-id raw)})

(defn p15-s23-closed-core-raw-actual-path-binding-input
  [raw]
  ;; The actual-path layer commits to the complete raw record.  Only its own
  ;; self hash is excluded; the separately normalized provenance hash remains
  ;; in the input and anchors this exact record to the path-neutral layer.
  (dissoc raw :actual-path-binding-hash))

(defn p15-s23-closed-core-bind-raw-provenance
  [raw]
  (let [raw
        (assoc raw :provenance-binding-hash
               (p15-s23-closed-core-digest
                (p15-s23-closed-core-raw-provenance-binding-input raw)))]
    (assoc raw :actual-path-binding-hash
           (p15-s23-closed-core-digest
            (p15-s23-closed-core-raw-actual-path-binding-input raw)))))

(defn p15-s23-closed-core-raw-provenance-binding-valid?
  [raw]
  (and (map? raw)
       (= p15-s23-closed-core-origin-closure-keys (set (keys raw)))
       (= (:provenance-binding-hash raw)
          (p15-s23-closed-core-digest
           (p15-s23-closed-core-raw-provenance-binding-input raw)))
       (= (:actual-path-binding-hash raw)
          (p15-s23-closed-core-digest
           (p15-s23-closed-core-raw-actual-path-binding-input raw)))))