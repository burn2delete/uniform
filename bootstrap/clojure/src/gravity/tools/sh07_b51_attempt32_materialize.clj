(ns gravity.tools.sh07-b51-attempt32-materialize
  "Pure V9 semantic materialization helpers loaded by the fresh M process only."
  (:require [gravity.tools.sh07-b51-v9-codec :as codec]))

(defn- fail! [reason & [context]]
  (throw (ex-info "SH-07 Attempt32 evidence publication rejected"
                  {:reason reason :context (or context {})})))

(def ^:private edge-endpoint-keys
  [:consumer-module-ordinal :consumer-source-id :consumer-fragment-id
   :consumer-binding-id :provider-module-ordinal :provider-source-id
   :provider-fragment-id :provider-binding-id])

(def bad-actual-keyset
  #{:status :code :rule :reason :input-id :mutated-id :expected :actual
    :committed-row :output})

(defn consumer-endpoint
  "Project the exact consumer endpoint used by the V9 edge-authority check."
  [edge]
  {:module-ordinal (:consumer-module-ordinal edge)
   :source-id (:consumer-source-id edge)
   :fragment-id (:consumer-fragment-id edge)
   :binding-id (:consumer-binding-id edge)})

(defn provider-endpoint
  "Project the exact provider endpoint used by the V9 edge-authority check."
  [edge]
  {:module-ordinal (:provider-module-ordinal edge)
   :source-id (:provider-source-id edge)
   :fragment-id (:provider-fragment-id edge)
   :binding-id (:provider-binding-id edge)})

(defn semantic-witness-member-id
  "Construct the architecture-defined MID(KIND, ENTRY)."
  [kind entry]
  (codec/H
   [:gravity/sh07-b51-semantic-witness-member-id-v1
    {:kind kind :entry entry}]))

(defn semantic-witness-member-root
  "Construct the architecture-defined MR(KIND, ENTRIES)."
  [kind entries]
  (codec/H
   [:gravity/sh07-b51-semantic-witness-member-root-v1
    {:kind kind :entries entries}]))

(defn- unique-where! [predicate rows reason]
  (let [matches (filterv predicate rows)]
    (when-not (= 1 (count matches))
      (fail! reason {:match-count (count matches)}))
    (first matches)))

(def ^:private cross-fragment-context-keys
  #{:carrier-valid? :pairing-valid? :expected-valid? :authentic-qs
    :authentic-m0 :authentic-m1 :owner-registry :expected-authority})

(def ^:dynamic ^:private *cross-fragment-authority-context* nil)

(defn verify-unauthorized-cross-fragment
  "Run the exact receipt-free V9 BAD-BASE verifier.

  The authenticated values are closed over by
  `build-unauthorized-cross-fragment-verifier`; callers provide only the six
  architecture inputs. No receipt, diagnostic, fixture, witness, path, or
  process value participates in this decision."
  [qs m0 m1 owner expected-authority candidate]
  (let [{:keys [carrier-valid? pairing-valid? expected-valid? authentic-qs
                authentic-m0 authentic-m1 owner-registry]
         closed-expected :expected-authority
         :as context}
        *cross-fragment-authority-context*]
    (when-not (= cross-fragment-context-keys (set (keys context)))
      (fail! :cross-fragment-authority-context))
    (let [m1-row
          (unique-where! #(= % (:m1-edge candidate)) (:edges m1)
                         :cross-fragment-m1-row)
          expected-row
          (unique-where! #(= (:m1-edge %) m1-row) expected-authority
                         :cross-fragment-expected-row)
          authorized-base (dissoc expected-row :edge-id)
          candidate-id
          (semantic-witness-member-id :l2-expected-edge-census candidate)
          expected-ids (set (map :edge-id expected-authority))
          unchanged-candidate (apply dissoc candidate edge-endpoint-keys)
          unchanged-authorized (apply dissoc authorized-base edge-endpoint-keys)]
      (when-not
       (and (true? carrier-valid?)
            (true? pairing-valid?)
            (true? expected-valid?)
            (= qs authentic-qs)
            (= m0 authentic-m0)
            (= m1 authentic-m1)
            (= owner owner-registry)
            (= expected-authority closed-expected)
            (= (set (keys candidate)) (set (keys authorized-base)))
            (= (:edge-id expected-row)
               (semantic-witness-member-id :l2-expected-edge-census
                                           authorized-base))
            (= unchanged-candidate unchanged-authorized)
            (not= (consumer-endpoint authorized-base)
                  (provider-endpoint authorized-base))
            (= (consumer-endpoint candidate)
               (provider-endpoint authorized-base))
            (= (provider-endpoint candidate)
               (consumer-endpoint authorized-base))
            (not= candidate authorized-base)
            (not= candidate-id (:edge-id expected-row))
            (not (contains? expected-ids candidate-id)))
        (fail! :unauthorized-cross-fragment-verifier-precondition))
      {:status :rejected
       :code "SH07-B51-EDGE-AUTHORITY"
       :rule "L2-UNKNOWN-CORE-FORM"
       :reason :unauthenticated-b51-external-link
       :input-id (:edge-id expected-row)
       :mutated-id candidate-id
       :expected (consumer-endpoint authorized-base)
       :actual (consumer-endpoint candidate)
       :committed-row nil
       :output nil})))

(defn build-unauthorized-cross-fragment-verifier
  "Close the exact authentic V9 authority facts over the six-input verifier."
  [context]
  (when-not (and (= cross-fragment-context-keys (set (keys context)))
                 (true? (:carrier-valid? context))
                 (true? (:pairing-valid? context))
                 (true? (:expected-valid? context)))
    (fail! :cross-fragment-authority-context))
  (fn [qs m0 m1 owner expected-authority candidate]
    (binding [*cross-fragment-authority-context* context]
      (verify-unauthorized-cross-fragment
       qs m0 m1 owner expected-authority candidate))))

(defn swap-edge-endpoints
  "Construct V9 BAD-BASE by swapping only the four endpoint pairs."
  [edge-base]
  (assoc edge-base
         :consumer-module-ordinal (:provider-module-ordinal edge-base)
         :consumer-source-id (:provider-source-id edge-base)
         :consumer-fragment-id (:provider-fragment-id edge-base)
         :consumer-binding-id (:provider-binding-id edge-base)
         :provider-module-ordinal (:consumer-module-ordinal edge-base)
         :provider-source-id (:consumer-source-id edge-base)
         :provider-fragment-id (:consumer-fragment-id edge-base)
         :provider-binding-id (:consumer-binding-id edge-base)))

(defn construct-negative-edge-evidence
  "Construct BAD-ACTUAL before selecting the L2 fuel receipt and deriving
  diagnostics/fixtures. This preserves the architecture's receipt-free
  rejection boundary and one-way identity DAG."
  [{:keys [authentic-qs authentic-m0 authentic-m1 owner-registry
           expected-authority]
    :as context}
   edge-bases operation-manifest fuel-receipts]
  (when-not (seq edge-bases)
    (fail! :cross-fragment-empty-edge-census))
  (let [seed (first edge-bases)
        bad-base (swap-edge-endpoints seed)
        bad-edge-id
        (semantic-witness-member-id :l2-expected-edge-census bad-base)
        verifier (build-unauthorized-cross-fragment-verifier context)
        ;; The verifier must run before any fuel receipt is selected.
        bad-actual
        (verifier authentic-qs authentic-m0 authentic-m1 owner-registry
                  expected-authority bad-base)
        bad-actual-expected
        {:status :rejected
         :code "SH07-B51-EDGE-AUTHORITY"
         :rule "L2-UNKNOWN-CORE-FORM"
         :reason :unauthenticated-b51-external-link
         :input-id (:edge-id (first expected-authority))
         :mutated-id bad-edge-id
         :expected (consumer-endpoint seed)
         :actual (consumer-endpoint bad-base)
         :committed-row nil
         :output nil}]
    (when-not (and (= bad-actual bad-actual-expected)
                   (= bad-actual-keyset (set (keys bad-actual)))
                   (nil? (:committed-row bad-actual))
                   (nil? (:output bad-actual)))
      (fail! :cross-fragment-bad-actual))
    (let [diagnostic-observation-keys
          [:code :status :rule :reason :input-id :mutated-id :expected :actual]
          diagnostic-base
          (select-keys bad-actual diagnostic-observation-keys)
          diagnostic-id
          (semantic-witness-member-id :l2-edge-diagnostics diagnostic-base)
          diagnostic (assoc diagnostic-base :diagnostic-id diagnostic-id)
          l2-diagnostics
          {:entries [diagnostic]
           :root (semantic-witness-member-root :l2-edge-diagnostics
                                               [diagnostic])}
          l2-operation
          (unique-where! #(= :l2 (:owner %)) operation-manifest
                         :cross-fragment-l2-operation)
          l2-fuel-receipt
          (unique-where! #(= (:operation-id l2-operation) (:operation-id %))
                         fuel-receipts :cross-fragment-l2-fuel-receipt)
          fixture-base
          {:input-id (:input-id bad-actual)
           :mutation {:operation :swap-endpoints
                      :paths (mapv vector edge-endpoint-keys)
                      :before [(consumer-endpoint seed)
                               (provider-endpoint seed)]
                      :after [(consumer-endpoint bad-base)
                              (provider-endpoint bad-base)]}
           :mutated-id (:mutated-id bad-actual)
           :diagnostic-id diagnostic-id
           :status (:status bad-actual)
           :fuel-receipt-id (:receipt-id l2-fuel-receipt)}
          fixture-id
          (semantic-witness-member-id :l2-edge-negative-fixtures fixture-base)
          fixture (assoc fixture-base :fixture-id fixture-id)
          l2-negative-fixtures
          {:entries [fixture]
           :root (semantic-witness-member-root :l2-edge-negative-fixtures
                                               [fixture])}]
      {:seed seed
       :bad-base bad-base
       :bad-edge-id bad-edge-id
       :bad-actual bad-actual
       :bad-actual-expected bad-actual-expected
       :diagnostic diagnostic
       :l2-diagnostics l2-diagnostics
       :l2-fuel-receipt l2-fuel-receipt
       :fixture fixture
       :l2-negative-fixtures l2-negative-fixtures
       :rejected-edge-ids []})))

(def fixed-fuel-executable-aliases
  {:maximum-module-forms :maximum-forms
   :maximum-fragment-resolutions :maximum-resolutions
   :maximum-module-carrier-nodes :maximum-carrier-nodes
   :maximum-module-carrier-depth :maximum-carrier-depth
   :maximum-module-carrier-width :maximum-carrier-width
   :maximum-module-scalar-bytes :maximum-scalar-bytes
   :maximum-module-digest-requests :maximum-digest-requests})

(def stage-semantic-specs
  [{:ordinal 0 :owner :l1 :stage-key :l1
    :projection {:kind :exact-keys
                 :keys [:document :source-units :grammar-change]}}
   {:ordinal 1 :owner :c2 :stage-key :c2
    :projection {:kind :exact-keys
                 :keys [:snapshot :units :source-map :diagnostics
                        :literal-records :reader-extensions]}}
   {:ordinal 2 :owner :l2 :stage-key :l2
    :projection {:kind :exact-keys
                 :keys [:owner-selection :core-forms :evaluation-order-root
                        :effects-root :capabilities-root]}}
   {:ordinal 3 :owner :c6 :stage-key :c6
    :projection {:kind :exact-keys
                 :keys [:ast :syntax-core-map :desugaring-trace
                        :origins-facts-order]}}
   {:ordinal 4 :owner :boot2 :stage-key :boot2
    :projection {:kind :exact-keys
                 :keys [:source :compiler :backend :dependencies
                        :runtime-assumptions]}}
   {:ordinal 5 :owner :boot3 :stage-key :boot3
    :projection {:kind :boot3-module-exact-keys
                 :module-keys [:ordinal :module :input-artifacts
                               :output-artifacts :effects :facts]
                 :keys [:modules :profile :stage-advanced]}}
   {:ordinal 6 :owner :boot4 :stage-key :boot4
    :projection {:kind :exact-keys
                 :keys [:module-manifest :effects :capabilities
                        :fact-provenance]}}])

(def ^:private fuel-collection-specs
  {:l1 {:kind :l1-source-syntax-walk
        :keys [:module-forms :top-level-forms :fragment-forms :form-children
               :form-depths :origin-entries :metadata-entries :alias-records]}
   :c2 {:kind :c2-token-form-source-map-walk
        :keys [:module-carriers]}
   :l2 {:kind :l2-owner-effect-capability-walk
        :keys [:fragments :module-bindings :bindings :fragment-resolutions
               :module-resolutions :effects :capabilities :exports]}
   :c6 {:kind :c6-core-lowering-walk
        :keys [:module-core :resolved-core-carriers]}
   :boot2 {:kind :boot2-template-carrier-walk
           :keys [:template-carriers]}
   :boot3 {:kind :boot3-generated-digest-walk
           :keys [:generated-digest-carriers]}
   :boot4 {:kind :boot4-module-digest-walk
           :keys [:module-digest-requests]}})

(def ^:private operation-counter-keys
  {:l1 [:maximum-module-forms :maximum-top-level-forms
        :maximum-fragment-forms :maximum-form-children :maximum-form-depth
        :maximum-origin-entries :maximum-metadata-entries
        :maximum-alias-records]
   :c2 [:maximum-module-carrier-nodes :maximum-module-carrier-depth
        :maximum-module-carrier-width :maximum-module-scalar-bytes]
   :l2 [:maximum-fragments :maximum-module-bindings :maximum-bindings
        :maximum-fragment-resolutions :maximum-module-resolutions
        :maximum-effects :maximum-capabilities :maximum-exports]
   :c6 [:maximum-module-core-nodes :maximum-resolved-core-carrier-nodes
        :maximum-resolved-core-carrier-depth
        :maximum-resolved-core-carrier-width
        :maximum-resolved-core-scalar-bytes]
   :boot2 [:maximum-template-carrier-nodes :maximum-template-carrier-depth
           :maximum-template-carrier-width :maximum-template-scalar-bytes]
   :boot3 [:maximum-generated-digest-carrier-nodes
           :maximum-generated-digest-carrier-depth
           :maximum-generated-digest-carrier-width
           :maximum-generated-digest-scalar-bytes]
   :boot4 [:maximum-module-digest-requests]})

(def ^:private fuel-source-root-keys
  #{:source-descriptors-root :event-root :semantic-algorithm-source-root
    :sh06-authority-carrier-id :bundle-core-id})

(defn fixed-fuel-executable
  "Apply the seven architecture aliases and prove the exact 34-key checked-core
  projection before any fuel subject is constructed."
  [proof-contract-bounds checked-core-bounds]
  (when-not (and (map? proof-contract-bounds) (map? checked-core-bounds))
    (fail! :fixed-fuel-shape))
  (let [pairs (mapv (fn [[key value]]
                      [(get fixed-fuel-executable-aliases key key) value])
                    proof-contract-bounds)
        executable (into {} pairs)
        executable-keys (set (keys executable))]
    (when-not (and (= (count pairs) (count executable))
                   (= 34 (count executable))
                   (= executable
                      (select-keys checked-core-bounds executable-keys)))
      (fail! :fixed-fuel-executable-mismatch
             {:proof-key-count (count proof-contract-bounds)
              :executable-key-count (count executable)}))
    executable))

(defn- require-keys! [value expected reason]
  (when-not (and (map? value) (= (set expected) (set (keys value))))
    (fail! reason {:expected expected
                   :actual (when (map? value) (vec (keys value)))}))
  value)

(defn project-stage-semantic
  "Apply the sole two V9 pre-stage projection grammars."
  [stage-spec stage-prefix]
  (let [{:keys [kind keys module-keys]} (:projection stage-spec)]
    (case kind
      :exact-keys
      (do
        (doseq [key keys]
          (when-not (contains? stage-prefix key)
            (fail! :stage-semantic-missing-key {:key key})))
        (into (array-map) (map (fn [key] [key (get stage-prefix key)]) keys)))

      :boot3-module-exact-keys
      (do
        (doseq [key keys]
          (when-not (contains? stage-prefix key)
            (fail! :stage-semantic-missing-key {:key key})))
        {:modules
         (mapv (fn [module]
                 (doseq [key module-keys]
                   (when-not (contains? module key)
                     (fail! :stage-semantic-missing-module-key {:key key})))
                 (into (array-map)
                       (map (fn [key] [key (get module key)]) module-keys)))
               (:modules stage-prefix))
         :profile (:profile stage-prefix)
         :stage-advanced (:stage-advanced stage-prefix)})

      (fail! :stage-semantic-projection-kind {:kind kind}))))

(defn stage-semantic-root [stage-spec stage-prefix]
  (codec/H
   [:gravity/sh07-b51-fixed-fuel-stage-semantic-root-v1
    {:ordinal (:ordinal stage-spec)
     :owner (:owner stage-spec)
     :stage-key (:stage-key stage-spec)
     :semantic (project-stage-semantic stage-spec stage-prefix)}]))

(defn fuel-carrier-roots [owner kind collections]
  (into (array-map)
        (map (fn [[collection value]]
               [collection
                (codec/H
                 [:gravity/sh07-b51-fixed-fuel-carrier-root-v1
                  {:owner owner :kind kind :collection collection
                   :value value}])]))
        collections))

(defn build-fuel-subjects
  "Build all seven receipt-free fuel subjects in operation-owner order."
  [fuel-source-roots pre-stages collections-by-owner]
  (require-keys! fuel-source-roots fuel-source-root-keys
                 :fuel-source-roots-shape)
  (require-keys! pre-stages (mapv :stage-key stage-semantic-specs)
                 :pre-stages-shape)
  (require-keys! collections-by-owner (mapv :owner stage-semantic-specs)
                 :fuel-collections-shape)
  (mapv
   (fn [{:keys [ordinal owner stage-key] :as stage-spec}]
     (let [{kind :kind collection-keys :keys}
           (get fuel-collection-specs owner)
           stage-prefix (get pre-stages stage-key)
           collections (get collections-by-owner owner)]
       (require-keys! stage-prefix
                      (get-in stage-spec [:projection :keys])
                      :pre-stage-prefix-shape)
       (when (= :boot3-module-exact-keys
                (get-in stage-spec [:projection :kind]))
         (doseq [module (:modules stage-prefix)]
           (require-keys! module
                          (get-in stage-spec [:projection :module-keys])
                          :boot3-pre-stage-module-shape)))
       (require-keys! collections collection-keys
                      :fuel-subject-collections-shape)
       (let [ordered-collections
             (into (array-map)
                   (map (fn [key] [key (get collections key)]))
                   collection-keys)
             subject-base
             {:ordinal ordinal
              :owner owner
              :kind kind
              :source-roots fuel-source-roots
              :collections ordered-collections
              :collection-roots
              (fuel-carrier-roots owner kind ordered-collections)
              :stage-semantic-root
              (stage-semantic-root stage-spec stage-prefix)}]
         (assoc subject-base :subject-id
                (codec/H
                 [:gravity/sh07-b51-fixed-fuel-subject-v1 subject-base])))))
   stage-semantic-specs))

(defn build-fixed-fuel-operation-specs
  "Attach each exact seven-operation counter program to its prior subject."
  [fuel-subjects]
  (when-not (and (= 7 (count fuel-subjects))
                 (= (mapv :ordinal fuel-subjects) (vec (range 7)))
                 (= (mapv :owner fuel-subjects)
                    (mapv :owner stage-semantic-specs)))
    (fail! :fuel-subject-order))
  (mapv (fn [{:keys [ordinal owner kind subject-id]}]
          {:ordinal ordinal :owner owner :kind kind :subject-id subject-id
           :counter-keys (get operation-counter-keys owner)})
        fuel-subjects))

(defn q-form
  "Select exactly one authenticated Q form by form ID."
  [q form-id]
  (unique-where! #(= form-id (:form-id %)) (:forms q) :owner-q-form))

(defn owning-top-level-form
  "Follow authenticated parent-form IDs to the unique source top-level form."
  [q form]
  (loop [current form seen #{}]
    (let [form-id (:form-id current)]
      (when (contains? seen form-id)
        (fail! :owner-parent-cycle {:form-id form-id}))
      (if-let [parent-id (:parent-form-id current)]
        (recur (q-form q parent-id) (conj seen form-id))
        (do
          (when-not (= 1 (count (filter #(= form-id %)
                                        (:top-level-form-ids q))))
            (fail! :owner-top-level-membership {:form-id form-id}))
          current)))))

(defn top-level-form-index [q form]
  (let [top-id (:form-id (owning-top-level-form q form))
        matches (keep-indexed #(when (= top-id %2) %1)
                              (:top-level-form-ids q))]
    (when-not (= 1 (count matches))
      (fail! :owner-top-level-index {:form-id top-id}))
    (first matches)))

(defn executable-top-level-forms
  "Return every authenticated executable top-level defn in exact source order."
  [q]
  (let [tops (mapv #(q-form q %) (:top-level-form-ids q))
        traces (:macro-origin-traces q)]
    (when-not (= (count tops) (count traces))
      (fail! :owner-top-level-trace-count))
    (doseq [top tops]
      (unique-where!
       #(and (= 'defn (:macro %))
             (= (:syntax-id top) (:output-def-syntax-id %)))
       traces :owner-top-level-trace))
    (doseq [trace traces]
      (when-not (and (= 'defn (:macro trace))
                     (= 1 (count (filter
                                  #(= (:output-def-syntax-id trace)
                                      (:syntax-id %))
                                  tops))))
        (fail! :owner-trace-top-level)))
    tops))

(defn build-owner-registry
  "Construct the authentic Q/RC top-level defn registry in module/source order."
  [module-transactions]
  (let [registry
        (reduce
         (fn [rows [module-index transaction]]
           (when-not (= module-index (:module-ordinal transaction))
             (fail! :owner-module-order))
           (let [q (:authenticated-core-request transaction)
                 rc (:resolved-core-carrier transaction)
                 tops (executable-top-level-forms q)]
             (reduce
              (fn [inner [top-index top]]
                (let [trace
                      (unique-where!
                       #(and (= 'defn (:macro %))
                             (= (:syntax-id top)
                                (:output-def-syntax-id %)))
                       (:macro-origin-traces q) :owner-defn-trace)
                      function-form
                      (unique-where!
                       #(= (:introduced-fn-syntax-id trace) (:syntax-id %))
                       (:forms q) :owner-function-form)
                      function-record
                      (unique-where!
                       #(and (= :named-top-level (:definition-kind %))
                             (= (:form-id function-form)
                                (:function-form-id %))
                             (= (:syntax-id function-form)
                                (:function-syntax-id %)))
                       (:function-records rc) :owner-function-record)
                      definition-record
                      (unique-where!
                       #(and (= (:definition-binding-id function-record)
                                (:binding-id %))
                             (= (:definition-core-node-id function-record)
                                (:core-node-id %)))
                       (:definitions rc) :owner-definition-record)
                      function-node
                      (unique-where!
                       #(= (:function-core-node-id function-record)
                           (:node-id %))
                       (:nodes rc) :owner-function-node)
                      base
                      {:ordinal (count inner)
                       :source-id
                       (get-in q [:lineage :sh06-semantic-projection-id])
                       :module-ordinal module-index
                       :top-level-form-ordinal top-index
                       :declared-name (:definition-name function-record)
                       :arity (:fixed-arity function-record)
                       :defn-id (:definition-core-node-id function-record)
                       :sh05-expanded-def-id (:output-def-syntax-id trace)
                       :sh05-expanded-fn-id (:introduced-fn-syntax-id trace)
                       :declared-body-root-id
                       (:body-core-node-id function-record)}]
                  (when-not
                   (and (= (:definition-core-node-id function-record)
                           (:core-node-id definition-record))
                        (= (:definition-binding-id function-record)
                           (:binding-id definition-record))
                        (= [(:body-core-node-id function-record)]
                           (:children function-node)))
                    (fail! :owner-definition-join))
                  (conj inner
                        (assoc base :registry-id
                               (semantic-witness-member-id
                                :l2-owner-registry base)))))
              rows (map-indexed vector tops))))
         [] (map-indexed vector module-transactions))]
    ;; Every Q form, including nested forms, must reach one authentic registry
    ;; row through its unique owning top-level defn.
    (doseq [[module-index transaction]
            (map-indexed vector module-transactions)
            form (:forms (:authenticated-core-request transaction))]
      (let [top-index
            (top-level-form-index (:authenticated-core-request transaction)
                                  form)]
        (unique-where!
         #(and (= module-index (:module-ordinal %))
               (= top-index (:top-level-form-ordinal %)))
         registry :owner-form-registry-join)))
    registry))

(defn build-owner-selection
  "Select the eight public roots from the complete authentic owner registry."
  [root-specs bundle-core owner-registry]
  (doseq [root-spec root-specs]
    (require-keys! root-spec
                   [:root-ordinal :function :arity :module-ordinal]
                   :owner-root-spec-shape))
  (let [bundle-root-specs
        (mapv
         (fn [root]
           (require-keys! root [:root-ordinal :function :arity :plan-id]
                          :owner-bundle-root-shape)
           (let [plan
                 (unique-where! #(= (:plan-id root) (:plan-id %))
                                (:module-plans bundle-core)
                                :owner-bundle-root-plan)]
             {:root-ordinal (:root-ordinal root)
              :function (:function root)
              :arity (:arity root)
              :module-ordinal (:module-ordinal plan)}))
         (:public-roots bundle-core))]
    (when-not (= root-specs bundle-root-specs)
      (fail! :owner-root-spec-projection))
    (let [candidate-vectors
          (mapv
           (fn [spec]
             (mapv :registry-id
                   (filterv #(and (= (:module-ordinal spec)
                                     (:module-ordinal %))
                                  (= (:function spec) (:declared-name %))
                                  (= (:arity spec) (:arity %)))
                            owner-registry)))
           root-specs)
          selections
          (mapv
           (fn [spec candidates]
             (when-not (= 1 (count candidates))
               (fail! :owner-root-candidate-count
                      {:root-ordinal (:root-ordinal spec)
                       :candidate-count (count candidates)}))
             (let [row
                   (unique-where! #(= (first candidates) (:registry-id %))
                                  owner-registry :owner-root-candidate)]
               {:root-spec spec
                :registry-id (:registry-id row)
                :defn-id (:defn-id row)
                :declared-body-root-id (:declared-body-root-id row)}))
           root-specs candidate-vectors)
          rejected []
          root-specs-source-id
          (codec/H
           [:gravity/sh07-b51-public-root-specs-id-v1 root-specs])
          bundle-root-specs-id
          (codec/H
           [:gravity/sh07-b51-bundle-core-public-roots-id-v1
            bundle-root-specs])]
      {:root-specs root-specs
       :root-specs-source-id root-specs-source-id
       :bundle-core-public-roots-projection bundle-root-specs
       :bundle-core-public-roots-id bundle-root-specs-id
       :registry owner-registry
       :candidates candidate-vectors
       :selections selections
       :rejected rejected
       :registry-root
       (semantic-witness-member-root
        :l2-owner-registry
        [root-specs owner-registry candidate-vectors selections rejected])})))
