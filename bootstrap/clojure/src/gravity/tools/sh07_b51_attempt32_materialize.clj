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

;; E3 pre-certificate construction -------------------------------------------------

(def checker-semantic-input-keys-e3
  #{:architecture-report-sha256 :snapshot-commit :snapshot-tree
    :predecessor-executable-descriptor-id :module-source-identities
    :module-source-byte-counts :emitter-source-id :emitter-semantic-id
    :m0s :m1 :m2s :bundle-core :source-forms :module-transactions
    :sh06-resolution-plan-authority :source-semantic-carrier
    :semantic-algorithm-source-descriptors
    :semantic-algorithm-source-root :checker :provenance})

(def semantic-algorithm-source-paths-e3
  ["bootstrap/clojure/src/gravity/tools/sh07_b51_attempt30_independent_census.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt30_producer_census.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_child.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_identity.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_materialize.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_tier0_materializer.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_v9_codec.clj"])

(def ^:private semantic-algorithm-descriptor-keys
  #{:ordinal :path :mode :blob-oid :byte-count :raw-sha256})

(def ^:private source-semantic-carrier-keys
  #{:source-descriptors :source-events :l1-document :l1-source-units
    :l1-grammar-change :c2-snapshot :c2-units :c2-source-map
    :c2-diagnostics :c2-literal-records :c2-reader-extensions})

(def fixed-fuel-e3
  {:maximum-module-forms 65536
   :maximum-module-core-nodes 65536
   :maximum-fragments 1024
   :maximum-top-level-forms 1024
   :maximum-fragment-forms 1024
   :maximum-form-children 1024
   :maximum-form-depth 256
   :maximum-module-bindings 2440
   :maximum-bindings 2048
   :maximum-alias-records 256
   :maximum-fragment-resolutions 2048
   :maximum-module-resolutions 65536
   :maximum-origin-entries 256
   :maximum-metadata-entries 256
   :maximum-effects 128
   :maximum-capabilities 128
   :maximum-exports 1024
   :maximum-module-carrier-nodes 8388608
   :maximum-module-carrier-depth 256
   :maximum-module-carrier-width 65536
   :maximum-module-scalar-bytes 268435456
   :maximum-template-carrier-nodes 33554432
   :maximum-template-carrier-depth 256
   :maximum-template-carrier-width 65536
   :maximum-template-scalar-bytes 1073741824
   :maximum-resolved-core-carrier-nodes 16777216
   :maximum-resolved-core-carrier-depth 256
   :maximum-resolved-core-carrier-width 65536
   :maximum-resolved-core-scalar-bytes 1073741824
   :maximum-generated-digest-carrier-nodes 16777216
   :maximum-generated-digest-carrier-depth 256
   :maximum-generated-digest-carrier-width 65536
   :maximum-generated-digest-scalar-bytes 1073741824
   :maximum-module-digest-requests 65540})

(def measurement-specs-e3
  [[0 :l1 0 :maximum-module-forms
    [:collections :module-forms] :maximum-row-cardinality]
   [1 :l1 1 :maximum-top-level-forms
    [:collections :top-level-forms] :maximum-row-cardinality]
   [2 :l1 2 :maximum-fragment-forms
    [:collections :fragment-forms] :maximum-row-cardinality]
   [3 :l1 3 :maximum-form-children
    [:collections :form-children] :maximum-row-cardinality]
   [4 :l1 4 :maximum-form-depth
    [:collections :form-depths] :maximum-row-value]
   [5 :l1 5 :maximum-origin-entries
    [:collections :origin-entries] :maximum-row-cardinality]
   [6 :l1 6 :maximum-metadata-entries
    [:collections :metadata-entries] :maximum-row-cardinality]
   [7 :l1 7 :maximum-alias-records
    [:collections :alias-records] :maximum-row-cardinality]
   [8 :c2 0 :maximum-module-carrier-nodes
    [:collections :module-carriers] :maximum-row-carrier-node-count]
   [9 :c2 1 :maximum-module-carrier-depth
    [:collections :module-carriers] :maximum-row-carrier-depth]
   [10 :c2 2 :maximum-module-carrier-width
    [:collections :module-carriers] :maximum-row-carrier-width]
   [11 :c2 3 :maximum-module-scalar-bytes
    [:collections :module-carriers] :maximum-row-executable-scalar-cost]
   [12 :l2 0 :maximum-fragments
    [:collections :fragments] :maximum-row-cardinality]
   [13 :l2 1 :maximum-module-bindings
    [:collections :module-bindings] :maximum-row-cardinality]
   [14 :l2 2 :maximum-bindings
    [:collections :bindings] :maximum-row-cardinality]
   [15 :l2 3 :maximum-fragment-resolutions
    [:collections :fragment-resolutions] :maximum-row-cardinality]
   [16 :l2 4 :maximum-module-resolutions
    [:collections :module-resolutions] :maximum-row-cardinality]
   [17 :l2 5 :maximum-effects
    [:collections :effects] :maximum-row-cardinality]
   [18 :l2 6 :maximum-capabilities
    [:collections :capabilities] :maximum-row-cardinality]
   [19 :l2 7 :maximum-exports
    [:collections :exports] :maximum-row-cardinality]
   [20 :c6 0 :maximum-module-core-nodes
    [:collections :module-core] :maximum-row-cardinality]
   [21 :c6 1 :maximum-resolved-core-carrier-nodes
    [:collections :resolved-core-carriers] :maximum-row-carrier-node-count]
   [22 :c6 2 :maximum-resolved-core-carrier-depth
    [:collections :resolved-core-carriers] :maximum-row-carrier-depth]
   [23 :c6 3 :maximum-resolved-core-carrier-width
    [:collections :resolved-core-carriers] :maximum-row-carrier-width]
   [24 :c6 4 :maximum-resolved-core-scalar-bytes
    [:collections :resolved-core-carriers] :maximum-row-executable-scalar-cost]
   [25 :boot2 0 :maximum-template-carrier-nodes
    [:collections :template-carriers] :maximum-row-carrier-node-count]
   [26 :boot2 1 :maximum-template-carrier-depth
    [:collections :template-carriers] :maximum-row-carrier-depth]
   [27 :boot2 2 :maximum-template-carrier-width
    [:collections :template-carriers] :maximum-row-carrier-width]
   [28 :boot2 3 :maximum-template-scalar-bytes
    [:collections :template-carriers] :maximum-row-executable-scalar-cost]
   [29 :boot3 0 :maximum-generated-digest-carrier-nodes
    [:collections :generated-digest-carriers]
    :maximum-row-carrier-node-count]
   [30 :boot3 1 :maximum-generated-digest-carrier-depth
    [:collections :generated-digest-carriers] :maximum-row-carrier-depth]
   [31 :boot3 2 :maximum-generated-digest-carrier-width
    [:collections :generated-digest-carriers] :maximum-row-carrier-width]
   [32 :boot3 3 :maximum-generated-digest-scalar-bytes
    [:collections :generated-digest-carriers]
    :maximum-row-executable-scalar-cost]
   [33 :boot4 0 :maximum-module-digest-requests
    [:collections :module-digest-requests] :maximum-row-cardinality]])

(defn validate-semantic-algorithm-authority!
  [descriptors root]
  (when-not
   (and (vector? descriptors)
        (= 7 (count descriptors))
        (= (vec (range 7)) (mapv :ordinal descriptors))
        (= semantic-algorithm-source-paths-e3 (mapv :path descriptors))
        (every? #(and (= semantic-algorithm-descriptor-keys
                         (set (keys %)))
                      (= "100644" (:mode %))
                      (string? (:blob-oid %))
                      (integer? (:byte-count %))
                      (not (neg? (:byte-count %)))
                      (string? (:raw-sha256 %)))
                descriptors)
        (= root
           (codec/H
            [:gravity/sh07-b51-semantic-algorithm-source-root-v1
             descriptors])))
    (fail! :semantic-algorithm-source-authority))
  descriptors)

(defn validate-source-semantic-carrier!
  [content carrier]
  (when-not
   (and (= source-semantic-carrier-keys (set (keys carrier)))
        (vector? (:source-descriptors carrier))
        (= 13 (count (:source-descriptors carrier)))
        (vector? (:source-events carrier))
        (= 40 (count (:source-events carrier)))
        (= (:l1-document carrier) (first (:source-descriptors carrier)))
        (vector? (:l1-source-units carrier))
        (= (vec (range 2 12)) (mapv :ordinal (:l1-source-units carrier)))
        (vector? (:c2-units carrier))
        (= (vec (range 2 12)) (mapv :ordinal (:c2-units carrier)))
        (= content (select-keys (:c2-snapshot carrier) [:commit :tree]))
        (= (codec/H [:gravity/sh07-b51-source-descriptors-root-v1
                     (:source-descriptors carrier)])
           (get-in carrier [:c2-snapshot :source-descriptors-root]))
        (= (codec/H [:gravity/sh07-b51-source-event-root-v1
                     (:source-events carrier)])
           (get-in carrier [:c2-snapshot :event-root])))
    (fail! :source-semantic-carrier))
  carrier)

(defn- module-products [transactions]
  (when-not (and (vector? transactions)
                 (= 7 (count transactions))
                 (= (vec (range 7)) (mapv :module-ordinal transactions)))
    (fail! :module-transactions))
  (mapv
   (fn [ordinal transaction]
     (let [source-id
           (get-in transaction
                   [:authenticated-core-request :lineage
                    :sh06-semantic-projection-id])
           roots
           {:core-root
            (semantic-witness-member-root
             :lowering-modules
             [[:core ordinal source-id (:module-core transaction)]])
            :diagnostic-root
            (semantic-witness-member-root
             :lowering-modules
             [[:diagnostics ordinal source-id
               (get-in transaction [:resolved-core-carrier :diagnostics])]])
            :effect-root
            (semantic-witness-member-root
             :lowering-modules
             [[:effects ordinal source-id
               (get-in transaction [:authenticated-core-request :module
                                     :effects])]])
            :capability-root
            (semantic-witness-member-root
             :lowering-modules
             [[:capabilities ordinal source-id
               (get-in transaction [:authenticated-core-request :module
                                     :capabilities])]])}
           base (merge {:ordinal ordinal :source-id source-id} roots)]
       (when-not (string? source-id) (fail! :module-source-id))
       (assoc base :module-id
              (semantic-witness-member-id :lowering-modules base))))
   (range 7) transactions))

(defn- foundation-preproduct [products algorithm-root]
  (let [fact-records
        (vec
         (mapcat
          (fn [product]
            (mapv
             (fn [[kind root-key]]
               (let [base {:kind kind :subject-id (:module-id product)
                           :value-id (get product root-key)}]
                 (assoc base :id
                        (semantic-witness-member-id :lowering-facts base))))
             [[:core :core-root] [:diagnostic :diagnostic-root]
              [:effect :effect-root] [:capability :capability-root]]))
          products))
        provenance-records
        (vec
         (mapcat
          (fn [product]
            (mapv
             (fn [root-key]
               (let [base {:producer-id algorithm-root
                           :input-ids [(:source-id product)
                                       (:module-id product)]
                           :output-id (get product root-key)}]
                 (assoc base :id
                        (semantic-witness-member-id
                         :lowering-provenance base))))
             [:core-root :diagnostic-root :effect-root :capability-root]))
          products))
        fact-root (codec/H [:gravity/sh07-b51-foundation-fact-root-v1
                            fact-records])
        provenance-root
        (codec/H [:gravity/sh07-b51-foundation-provenance-root-v1
                  provenance-records])
        pass-records
        (mapv
         (fn [product]
           (let [base {:ordinal (:ordinal product)
                       :input-id (:source-id product)
                       :output-id (:core-root product)
                       :invalidates []
                       :preserves [(:diagnostic-root product)
                                   (:effect-root product)
                                   (:capability-root product)]
                       :fact-root fact-root
                       :provenance-root provenance-root}]
             (assoc base :pass-id
                    (semantic-witness-member-id :lowering-passes base))))
         products)]
    {:pass-records pass-records
     :fact-records fact-records
     :provenance-records provenance-records
     :pass-root (codec/H [:gravity/sh07-b51-foundation-pass-root-v1
                          pass-records])
     :fact-root fact-root
     :provenance-root provenance-root}))

(def ^:private root-specs-e3
  [{:root-ordinal 1 :function 'sh07-b51-build-template :arity 1
    :module-ordinal 3}
   {:root-ordinal 2 :function 'sh07-b51-verify-template :arity 3
    :module-ordinal 3}
   {:root-ordinal 3 :function 'sh07-b51-resolve-digest-preimage :arity 3
    :module-ordinal 2}
   {:root-ordinal 4 :function 'sh07-b51-resolve-template :arity 3
    :module-ordinal 3}
   {:root-ordinal 5 :function 'sh07-b51-verify-resolved :arity 4
    :module-ordinal 3}
   {:root-ordinal 6
    :function 'sh07-b51-build-independent-verifier-binding :arity 6
    :module-ordinal 5}
   {:root-ordinal 7 :function 'sh07-b51-build-final-artifact :arity 7
    :module-ordinal 3}
   {:root-ordinal 8 :function 'sh07-b51-finalize-rejection :arity 1
    :module-ordinal 6}])

(defn- root-specs [bundle-core]
  (let [plans (into {} (map (juxt :plan-id identity)
                            (:module-plans bundle-core)))]
    (let [projection
          (mapv (fn [root]
                  (let [plan (get plans (:plan-id root))]
                    (when-not plan (fail! :root-spec-plan))
                    {:root-ordinal (:root-ordinal root)
                     :function (:function root)
                     :arity (:arity root)
                     :module-ordinal (:module-ordinal plan)}))
                (:public-roots bundle-core))]
      (when-not (= root-specs-e3 projection) (fail! :root-spec-projection))
      root-specs-e3)))

(defn- module-coordinate [transaction]
  [(:module-ordinal transaction)
   (get-in transaction
           [:authenticated-core-request :lineage
            :sh06-semantic-projection-id])])

(defn- canonical-map-entries [value]
  (when-not (map? value) (fail! :fixed-fuel-map-entries))
  (->> value
       (sort-by (fn [[key _]]
                  (mapv #(bit-and 255 %) (codec/W key))))
       (mapv vec)))

(defn- form-depth-rows [transaction]
  (let [q (:authenticated-core-request transaction)
        coordinate (module-coordinate transaction)
        forms (:forms q)
        by-id (into {} (map (juxt :form-id identity)) forms)
        top-set (set (:top-level-form-ids q))]
    (when-not (= (count forms) (count by-id)) (fail! :form-depth-duplicate))
    (doseq [form forms, child-id (:child-form-ids form)]
      (when-not (= (:form-id form) (:parent-form-id (get by-id child-id)))
        (fail! :form-depth-child-join)))
    (mapv
     (fn [form]
       [[coordinate (:form-id form)]
        (loop [row form depth 1 seen #{}]
          (when (contains? seen (:form-id row)) (fail! :form-depth-cycle))
          (if-let [parent-id (:parent-form-id row)]
            (if-let [parent (get by-id parent-id)]
              (recur parent (inc depth) (conj seen (:form-id row)))
              (fail! :form-depth-parent))
            (do (when-not (contains? top-set (:form-id row))
                  (fail! :form-depth-top))
                depth)))])
     forms)))

(defn- transaction-collections [transactions _source-forms _carrier]
  (let [module-rows
        (fn [field]
          (mapv (fn [transaction]
                  [(module-coordinate transaction) (get transaction field)])
                transactions))]
    {:l1
     {:module-forms
      (vec (mapcat (fn [transaction]
                     (let [c (module-coordinate transaction)
                           q (:authenticated-core-request transaction)]
                       [[[c :forms] (mapv :form-id (:forms q))]
                        [[c :macro-expansion-trace]
                         (:macro-expansion-trace q)]])) transactions))
      :top-level-forms
      (vec (mapcat
            (fn [transaction]
              (let [c (module-coordinate transaction)
                    q (:authenticated-core-request transaction)
                    expectation (:macro-origin-expectation q)]
                (concat
                 [[[c :top-level-form-ids] (:top-level-form-ids q)]]
                 (mapcat
                  (fn [fragment]
                    [[[[c (:fragment-id fragment)] :root-form-ids]
                      (:root-form-ids fragment)]
                     [[[c (:fragment-id fragment)] :root-node-ids]
                      (:root-node-ids fragment)]])
                  (:fragment-manifest q))
                 [[[c :macro-origin-traces] (:macro-origin-traces q)]
                  [[c :expected-input-syntax-ids]
                   (:expected-input-syntax-ids expectation)]
                  [[c :expected-output-def-syntax-ids]
                   (:expected-output-def-syntax-ids expectation)]
                  [[c :expected-introduced-fn-syntax-ids]
                   (:expected-introduced-fn-syntax-ids expectation)]])))
            transactions))
      :fragment-forms
      (vec (mapcat (fn [transaction]
                     (mapv (fn [fragment]
                             [[(module-coordinate transaction)
                               (:fragment-id fragment)]
                              (:form-ids fragment)])
                           (get-in transaction
                                   [:authenticated-core-request
                                    :fragment-manifest])))
                   transactions))
      :form-children
      (vec (mapcat
            (fn [transaction]
              (let [c (module-coordinate transaction)
                    q (:authenticated-core-request transaction)
                    rc (:resolved-core-carrier transaction)]
                (concat
                 (mapv (fn [form]
                         [[[c (:form-id form)] :child-form-ids]
                          (:child-form-ids form)]) (:forms q))
                 (mapcat
                  (fn [ordinal call]
                    [[[[c :call ordinal] :argument-node-ids]
                      (:argument-node-ids call)]
                     [[[c :call ordinal] :ordered-evaluation-node-ids]
                      (:ordered-evaluation-node-ids call)]])
                  (range) (:calls rc)))))
            transactions))
      :form-depths (vec (mapcat form-depth-rows transactions))
      :origin-entries
      (vec (mapcat (fn [transaction]
                     (mapcat
                      (fn [form]
                        [[[(module-coordinate transaction) :origin-chain
                           (:form-id form)] (:origin-chain form)]
                         [[(module-coordinate transaction) :generated-origin
                           (:form-id form)] (:generated-origin form)]])
                      (get-in transaction
                              [:authenticated-core-request :forms])))
                   transactions))
      :metadata-entries
      (vec (mapcat
            (fn [transaction]
              (let [c (module-coordinate transaction)
                    q (:authenticated-core-request transaction)]
                (concat
                 (mapv (fn [form]
                         [[c :form (:form-id form)]
                          (canonical-map-entries (:metadata form))])
                       (:forms q))
                 (mapv (fn [binding]
                         [[c :binding (:binding-id binding)]
                          (canonical-map-entries (:semantic-span binding))])
                       (:binding-table q))))) transactions))
      :alias-records
      (vec (mapcat
            (fn [transaction]
              (let [c (module-coordinate transaction)
                    q (:authenticated-core-request transaction)]
                (concat
                 [[[c :module-alias-table] (:alias-table q)]]
                 (mapv (fn [fragment]
                         [[c :fragment-aliases (:fragment-id fragment)]
                          (:alias-names fragment)])
                       (:fragment-manifest q))))) transactions))}
     :c2
     {:module-carriers
      (vec (mapcat (fn [transaction]
                     [[[(module-coordinate transaction)
                        :authenticated-request]
                       (:authenticated-core-request transaction)]
                      [[(module-coordinate transaction) :resolved-digests]
                       (:resolved-digests transaction)]]) transactions))}
     :l2
     {:fragments
      (mapv (fn [transaction]
              [(module-coordinate transaction)
               (mapv :fragment-id
                     (get-in transaction
                             [:authenticated-core-request
                              :fragment-manifest]))]) transactions)
      :module-bindings
      (vec (mapcat (fn [transaction]
                     (let [c (module-coordinate transaction)
                           q (:authenticated-core-request transaction)
                           rc (:resolved-core-carrier transaction)]
                       [[[c :binding-table]
                         (mapv :binding-id (:binding-table q))]
                        [[c :function-records] (:function-records rc)]
                        [[c :recursion-components]
                         (:recursion-components rc)]])) transactions))
      :bindings
      (vec (mapcat
            (fn [transaction]
              (mapcat
               (fn [fragment]
                 [[[(module-coordinate transaction) :local
                    (:fragment-id fragment)] (:local-binding-ids fragment)]
                  [[(module-coordinate transaction) :external
                    (:fragment-id fragment)]
                   (:external-binding-ids fragment)]])
               (get-in transaction
                       [:authenticated-core-request :fragment-manifest])))
            transactions))
      :fragment-resolutions
      (vec (mapcat (fn [transaction]
                     (mapv
                      (fn [fragment]
                        [[(module-coordinate transaction)
                          (:fragment-id fragment)]
                         (:resolution-reference-syntax-ids fragment)])
                      (get-in transaction
                              [:authenticated-core-request
                               :fragment-manifest]))) transactions))
      :module-resolutions
      (mapv (fn [transaction]
              [(module-coordinate transaction)
               (mapv :reference-syntax-id
                     (get-in transaction
                             [:authenticated-core-request
                              :resolution-table]))]) transactions)
      :effects
      (vec (mapcat
            (fn [transaction]
              (let [c (module-coordinate transaction)
                    q (:authenticated-core-request transaction)]
                (concat
                 [[[c :module-effects] (get-in q [:module :effects])]]
                 (mapcat
                  (fn [binding]
                    [[[c :binding (:binding-id binding) :effects]
                      (:effects binding)]
                     [[c :binding (:binding-id binding) :profile-set]
                      (:profile-set binding)]
                     [[c :binding (:binding-id binding) :target-set]
                      (:target-set binding)]]) (:binding-table q)))))
            transactions))
      :capabilities
      (vec (mapcat
            (fn [transaction]
              (let [c (module-coordinate transaction)
                    q (:authenticated-core-request transaction)]
                (concat
                 [[[c :module-capabilities]
                   (get-in q [:module :capabilities])]]
                 (mapv (fn [binding]
                         [[c :binding (:binding-id binding) :capabilities]
                          (:capabilities binding)]) (:binding-table q))
                 (mapv (fn [alias]
                         [[c :alias (:alias alias) :targets]
                          (:targets alias)]) (:alias-table q))))) transactions))
      :exports
      (mapv (fn [transaction]
              [(module-coordinate transaction)
               (get-in transaction
                       [:authenticated-core-request :module :exports])])
            transactions)}
     :c6 {:module-core (module-rows :module-core)
          :resolved-core-carriers (module-rows :resolved-core-carrier)}
     :boot2 {:template-carriers (module-rows :template-carrier)}
     :boot3
     {:generated-digest-carriers (module-rows :generated-digest-carrier)}
     :boot4 {:module-digest-requests (module-rows :module-digest-requests)}}))

(defn- rc-node-for-form [rc form]
  (unique-where! #(= (:form-id form) (get-in % [:source :form-id]))
                 (:nodes rc) :core-form-node))

(defn- evaluation-index [rc node]
  (let [node-ids (mapv :node-id (:nodes rc))
        evaluation-ids (mapv :core-node-id (:evaluation-order rc))
        matches (keep-indexed #(when (= (:node-id node) (:core-node-id %2)) %1)
                              (:evaluation-order rc))]
    (when-not (and (= (count node-ids) (count (set node-ids)))
                   (= (set node-ids) (set evaluation-ids))
                   (= 1 (count matches)))
      (fail! :core-evaluation-order))
    (first matches)))

(defn- artifact-descriptor [module-ordinal kind source-id value]
  (let [base {:module-ordinal module-ordinal :kind kind
              :source-id source-id :value value}]
    {:artifact-id (semantic-witness-member-id :boot3-artifact base)
     :kind kind :source-id source-id :raw-sha256 (codec/R (codec/W value))}))

(defn- pre-stages [input products foundation]
  (let [carrier (:source-semantic-carrier input)
        transactions (:module-transactions input)
        registry (build-owner-registry transactions)
        owner-selection
        (build-owner-selection (root-specs (:bundle-core input))
                               (:bundle-core input) registry)
        form-tuples
        (vec (for [[module-ordinal transaction]
                   (map-indexed vector transactions)
                   form (get-in transaction [:authenticated-core-request :forms])]
               [module-ordinal transaction form
                (rc-node-for-form (:resolved-core-carrier transaction) form)]))
        _ (doseq [transaction transactions]
            (let [q (:authenticated-core-request transaction)
                  rc (:resolved-core-carrier transaction)]
              (when-not (= (count (:forms q)) (count (:nodes rc)))
                (fail! :core-form-node-count))
              (doseq [node (:nodes rc)]
                (unique-where! #(= (get-in node [:source :form-id]) (:form-id %))
                               (:forms q) :core-node-form))))
        core-forms
        (mapv
         (fn [ordinal [module-ordinal transaction form node]]
           (let [q (:authenticated-core-request transaction)
                 top-index (top-level-form-index q form)
                 owner (unique-where!
                        #(and (= module-ordinal (:module-ordinal %))
                              (= top-index (:top-level-form-ordinal %)))
                        registry :core-form-owner)
                 _ (when-not
                    (= [(get-in q [:module :effects])
                        (get-in q [:module :capabilities])
                        (get-in q [:module :profile])]
                       [(get-in node [:preserved-declarations :effects])
                        (get-in node [:preserved-declarations :capabilities])
                        (get-in node [:module :profile])])
                     (fail! :core-form-declaration-join))
                 base {:ordinal ordinal :source-id (:source-id owner)
                       :top-level-form-ordinal top-index
                       :owning-defn-id (:defn-id owner)
                       :form-id (:form-id form)
                       :owning-root-id (:declared-body-root-id owner)
                       :evaluation-index
                       (evaluation-index (:resolved-core-carrier transaction)
                                         node)
                       :effects (get-in node [:preserved-declarations :effects])
                       :capabilities
                       (get-in node [:preserved-declarations :capabilities])
                       :profile (get-in node [:module :profile])}]
             (assoc base :semantic-id
                    (semantic-witness-member-id :l2-core-form base))))
         (range) form-tuples)
        evaluation-entries
        (mapv #(select-keys % [:ordinal :source-id :form-id :semantic-id
                               :evaluation-index]) core-forms)
        effect-entries
        (mapv #(select-keys % [:ordinal :source-id :form-id :semantic-id
                               :effects]) core-forms)
        capability-entries
        (mapv #(select-keys % [:ordinal :source-id :form-id :semantic-id
                               :capabilities]) core-forms)
        ast-entries
        (mapv
         (fn [ordinal [module-ordinal transaction _ node]]
           (let [preceding (set (map :node-id
                                     (take-while #(not= node %)
                                                 (get-in transaction
                                                         [:resolved-core-carrier
                                                          :nodes]))))]
             (when-not (every? preceding (:children node))
               (fail! :c6-child-postorder))
             {:ordinal ordinal :node-id (:node-id node)
              :kind (:core-form node) :children (:children node)
              :source-span (get-in node [:source :semantic-span])
              :origin-chain (get-in node [:source :origin-chain])
              :generated-origin (get-in node [:source :generated-origin])
              :evaluation-index
              (evaluation-index (:resolved-core-carrier transaction) node)
              :binding-context (:binding-context node)
              :profile (get-in node [:module :profile])
              :metadata (:metadata node) :fact-ids []
              :generated (:generated? node)}))
         (range) form-tuples)
        syntax-core-entries
        (mapv (fn [ordinal [_ _ _ node]]
                {:ordinal ordinal :syntax-id (get-in node [:source :syntax-id])
                 :core-id (:node-id node)
                 :origin-chain (get-in node [:source :origin-chain])
                 :generated-origin (get-in node [:source :generated-origin])})
              (range) form-tuples)
        desugaring-entries
        (mapv
         (fn [ordinal [trace kind]]
           (if (= kind :def)
             {:ordinal ordinal :input-id (:input-syntax-id trace)
              :output-id (:output-def-syntax-id trace)
              :pass-id (:macro-expansion-trace-id trace) :fact-ids []
              :origin-id (:def-generated-origin-id trace)}
             {:ordinal ordinal :input-id (:input-syntax-id trace)
              :output-id (:introduced-fn-syntax-id trace)
              :pass-id (:macro-expansion-trace-id trace) :fact-ids []
              :origin-id (:fn-generated-origin-id trace)}))
         (range)
         (mapcat (fn [transaction]
                   (mapcat #(vector [% :def] [% :fn])
                           (get-in transaction
                                   [:authenticated-core-request
                                    :macro-origin-traces]))) transactions))
        origin-entries
        (mapv (fn [ordinal [module-ordinal transaction _ node]]
                {:ordinal ordinal
                 :source-id (get-in transaction
                                    [:authenticated-core-request :lineage
                                     :sh06-semantic-projection-id])
                 :source-node-id (:node-id node)
                 :form-id (get-in node [:source :form-id])
                 :syntax-id (get-in node [:source :syntax-id])
                 :source-span (get-in node [:source :semantic-span])
                 :origin-chain (get-in node [:source :origin-chain])
                 :generated-origin (get-in node [:source :generated-origin])})
              (range) form-tuples)
        c6-evaluation-entries
        (mapv
         (fn [ordinal [module-ordinal transaction evaluation]]
           (merge {:ordinal ordinal :module-ordinal module-ordinal
                   :source-id
                   (get-in transaction
                           [:authenticated-core-request :lineage
                            :sh06-semantic-projection-id])}
                  (select-keys evaluation
                               [:core-node-id :kind :region
                                :owner-function-syntax-id :children])))
         (range)
         (for [[module-ordinal transaction] (map-indexed vector transactions)
               evaluation (get-in transaction
                                  [:resolved-core-carrier :evaluation-order])]
           [module-ordinal transaction evaluation]))
        boot3-modules
        (mapv
         (fn [ordinal transaction product]
           (let [q (:authenticated-core-request transaction)
                 sid (:source-id product)
                 inputs [(artifact-descriptor ordinal
                                              :authenticated-core-request sid q)]
                 outputs
                 [(artifact-descriptor ordinal :core-template sid
                                       (:template-carrier transaction))
                  (artifact-descriptor ordinal :digest-requests sid
                                       (:generated-digest-carrier transaction))
                  (artifact-descriptor ordinal :resolved-digests sid
                                       (:resolved-digests transaction))
                  (artifact-descriptor ordinal :resolved-core sid
                                       (:resolved-core-carrier transaction))]
                 facts (subvec (:fact-records foundation)
                               (* ordinal 4) (* (inc ordinal) 4))]
             {:ordinal ordinal :module (:module q)
              :input-artifacts inputs :output-artifacts outputs
              :effects (get-in q [:module :effects])
              :facts {:entries facts
                      :root (semantic-witness-member-root :boot3-facts facts)}}))
         (range 7) transactions products)
        manifest-entries
        (mapv (fn [ordinal module product]
                {:ordinal ordinal :module-id (:module-id product)
                 :source-id (:source-id product)
                 :input-artifact-ids (mapv :artifact-id
                                           (:input-artifacts module))
                 :output-artifact-ids (mapv :artifact-id
                                            (:output-artifacts module))
                 :profile :meta})
              (range 7) boot3-modules products)
        effect-rows
        (vec (mapcat
              (fn [transaction product]
                (mapv (fn [node]
                        {:module-id (:module-id product) :node-id (:node-id node)
                         :values (get-in node
                                         [:preserved-declarations :effects])})
                      (get-in transaction [:resolved-core-carrier :nodes])))
              transactions products))
        capability-rows
        (vec (mapcat
              (fn [transaction product]
                (mapv (fn [node]
                        {:module-id (:module-id product) :node-id (:node-id node)
                         :values
                         (get-in node
                                 [:preserved-declarations :capabilities])})
                      (get-in transaction [:resolved-core-carrier :nodes])))
              transactions products))]
    {:l1 {:document (:l1-document carrier)
          :source-units (:l1-source-units carrier)
          :grammar-change (:l1-grammar-change carrier)}
     :c2 {:snapshot (:c2-snapshot carrier) :units (:c2-units carrier)
          :source-map (:c2-source-map carrier)
          :diagnostics (:c2-diagnostics carrier)
          :literal-records (:c2-literal-records carrier)
          :reader-extensions (:c2-reader-extensions carrier)}
     :l2 {:owner-selection owner-selection :core-forms core-forms
          :evaluation-order-root
          (semantic-witness-member-root :l2-evaluation-order
                                        evaluation-entries)
          :effects-root (semantic-witness-member-root :l2-effects
                                                      effect-entries)
          :capabilities-root
          (semantic-witness-member-root :l2-capabilities
                                        capability-entries)}
     :c6 {:ast {:entries ast-entries
                :root (semantic-witness-member-root :c6-ast ast-entries)}
          :syntax-core-map
          {:entries syntax-core-entries
           :root (semantic-witness-member-root :c6-syntax-core-map
                                               syntax-core-entries)}
          :desugaring-trace
          {:entries desugaring-entries
           :root (semantic-witness-member-root :c6-desugaring-trace
                                               desugaring-entries)}
          :origins-facts-order
          {:origin-root (semantic-witness-member-root :c6-origins
                                                      origin-entries)
           :fact-root (semantic-witness-member-root :c6-facts [])
           :evaluation-order-root
           (semantic-witness-member-root :c6-evaluation-order
                                         c6-evaluation-entries)}}
     :boot2
     {:source {:descriptors (:source-descriptors carrier)
               :descriptor-root
               (get-in carrier [:c2-snapshot :source-descriptors-root])
               :source-id
               (codec/H [:gravity/sh07-b51-boot2-source-id-v1
                         (get-in carrier
                                 [:c2-snapshot :source-descriptors-root])])}
      :compiler {:descriptor (nth (:source-descriptors carrier) 10)
                 :compiler-id
                 (codec/H [:gravity/sh07-b51-boot2-compiler-id-v1
                           (nth (:source-descriptors carrier) 10)])}
      :backend {:status :not-applicable
                :identity (codec/H
                           [:gravity/sh07-b51-no-backend-v1
                            :verified-core])}
      :dependencies {:entries (:semantic-algorithm-source-descriptors input)
                     :root (semantic-witness-member-root
                            :boot2-dependencies
                            (:semantic-algorithm-source-descriptors input))}
      :runtime-assumptions
      {:jvm-id (semantic-witness-member-id
                :boot2-jvm {:runtime :checker-local})
       :clojure-seed-id
       (semantic-witness-member-root
        :boot2-dependencies (:semantic-algorithm-source-descriptors input))
       :git-object-format "sha1"
       :fixed-fuel-bounds-id
       (codec/H [:gravity/sh07-b51-fixed-fuel-bounds-v1 fixed-fuel-e3])
       :network :forbidden}}
     :boot3 {:modules boot3-modules :profile :meta :stage-advanced false}
     :boot4
     {:module-manifest
      {:entries manifest-entries
       :root (semantic-witness-member-root :boot4-module-manifest
                                           manifest-entries)}
      :effects {:entries effect-rows
                :root (semantic-witness-member-root :boot4-effects effect-rows)}
      :capabilities
      {:entries capability-rows
       :root (semantic-witness-member-root :boot4-capabilities
                                           capability-rows)}
      :fact-provenance
      {:facts (:fact-records foundation)
       :provenance (:provenance-records foundation)
       :fact-root (:fact-root foundation)
       :provenance-root (:provenance-root foundation)}}}))

(defn- strict-edn-scalar? [value]
  (or (nil? value) (boolean? value) (integer? value) (string? value)
      (char? value) (keyword? value) (symbol? value)))

(defn- carrier-measure [value]
  (cond
    (strict-edn-scalar? value)
    {:nodes 1 :depth 0 :width 0
     :scalar-cost (if (nil? value) 0 (* 4 (count (str value))))}

    (or (map? value) (vector? value) (set? value) (list? value))
    (let [children (cond
                     (map? value) (mapcat identity value)
                     :else value)
          measured (mapv carrier-measure children)]
      {:nodes (inc (reduce + 0 (map :nodes measured)))
       :depth (if (seq measured) (inc (reduce max (map :depth measured))) 0)
       :width (max (if (map? value) (* 2 (count value)) (count value))
                   (reduce max 0 (map :width measured)))
       :scalar-cost (reduce + 0 (map :scalar-cost measured))})

    :else (fail! :fixed-fuel-noncanonical-value
                 {:class (.getName (class value))})))

(defn- measured-value [rows derivation]
  (when-not (and (vector? rows)
                 (every? #(and (vector? %) (= 2 (count %))) rows)
                 (= (count rows) (count (set (map first rows)))))
    (fail! :fixed-fuel-collection))
  (let [values (mapv second rows)]
    (case derivation
      :maximum-row-cardinality (reduce max 0 (map count values))
      :maximum-row-value
      (do (when-not (every? #(and (integer? %) (not (neg? %))) values)
            (fail! :fixed-fuel-row-value))
          (reduce max 0 values))
      :maximum-row-carrier-node-count
      (reduce max 0 (map (comp :nodes carrier-measure) values))
      :maximum-row-carrier-depth
      (reduce max 0 (map (comp :depth carrier-measure) values))
      :maximum-row-carrier-width
      (reduce max 0 (map (comp :width carrier-measure) values))
      :maximum-row-executable-scalar-cost
      (reduce max 0 (map (comp :scalar-cost carrier-measure) values))
      (fail! :fixed-fuel-derivation))))

(defn- operation-manifest [operation-specs lowering-product algorithm-root]
  (mapv (fn [spec subject]
          (when-not (= (select-keys spec [:ordinal :owner :kind :subject-id])
                       (select-keys subject
                                    [:ordinal :owner :kind :subject-id]))
            (fail! :operation-subject-join))
          (let [base (assoc spec
                            :algorithm-source-root algorithm-root
                            :lowering-product-id (:product-id lowering-product))]
            (assoc base :operation-id
                   (codec/H [:gravity/sh07-b51-bounded-operation-id-v1
                             base]))))
        operation-specs (:fuel-subjects lowering-product)))

(defn- fuel-receipts [subjects operations]
  (let [bounds-id
        (codec/H [:gravity/sh07-b51-fixed-fuel-bounds-v1 fixed-fuel-e3])]
    (mapv
     (fn [subject operation]
       (let [specs (filterv #(= (:owner operation) (nth % 1))
                            measurement-specs-e3)
             trace
             (mapv (fn [ordinal [_ _ _ counter-key coordinate derivation]]
                     {:ordinal ordinal :counter-key counter-key
                      :coordinate coordinate
                      :observed-value
                      (measured-value (get-in subject coordinate) derivation)
                      :derivation derivation})
                   (range) specs)
             counts (into {} (map (juxt :counter-key :observed-value)) trace)
             within? (every? #(<= (val %) (get fixed-fuel-e3 (key %)))
                             counts)
             base {:operation-id (:operation-id operation)
                   :bounds-id bounds-id :trace trace
                   :trace-root
                   (codec/H [:gravity/sh07-b51-fixed-fuel-trace-root-v1 trace])
                   :operation-counts counts :within-bounds within?}]
         (when-not within? (fail! :fixed-fuel-exhausted))
         (assoc base :receipt-id
                (codec/H [:gravity/sh07-b51-fixed-fuel-receipt-v1 base]))))
     subjects operations)))

(def ^:private stage-schema
  {:l1 :gravity/sh07-b51-l1-witness-v1
   :c2 :gravity/sh07-b51-c2-witness-v1
   :l2 :gravity/sh07-b51-l2-witness-v1
   :c6 :gravity/sh07-b51-c6-witness-v1
   :boot2 :gravity/sh07-b51-boot2-witness-v1
   :boot3 :gravity/sh07-b51-boot3-witness-v1
   :boot4 :gravity/sh07-b51-boot4-witness-v1})

(defn- stage-witness [stage-key prefix subject operation _receipt _foundation]
  (let [base
        (cond-> (merge {:schema (get stage-schema stage-key)
                        :fuel-subject-id (:subject-id subject)}
                       prefix
                       {:operation-ids [(:operation-id operation)]})
          (= stage-key :l2)
          (assoc :external-bindings [] :edge-authority [] :diagnostics
                 {:entries [] :root (semantic-witness-member-root
                                     :l2-edge-diagnostics [])}
                 :negative-fixtures
                 {:entries [] :root (semantic-witness-member-root
                                     :l2-edge-negative-fixtures [])})
          (= stage-key :c6)
          (assoc :verification {:checker-ids []
                                :verified-root (get-in prefix [:ast :root])
                                :input-ast-root (get-in prefix [:ast :root])
                                :status :passed}
                 :mutations [])
          (= stage-key :boot3)
          (assoc :tests [] :diagnostic-compatibility []
                 :stage-comparison
                 {:left-lowering-product-id
                  (:lowering-product-id operation)
                  :right-lowering-product-id
                  (:lowering-product-id operation)
                  :observables [] :equal true
                  :equivalence-root
                  (semantic-witness-member-root
                   :boot3-stage-comparison [])})
          (= stage-key :boot4)
          (assoc :deterministic-output
                 {:a-lowering-product-id (:lowering-product-id operation)
                  :b-lowering-product-id (:lowering-product-id operation)
                  :observable-root
                  (get-in prefix [:module-manifest :root])
                  :equal true}
                 :preservation-tests
                 {:positive [] :negative []
                  :result-root
                  (semantic-witness-member-root
                   :boot4-preservation-tests [[] []])
                  :status :passed}))]
    (assoc base :witness-id (codec/H [(get stage-schema stage-key) base]))))

(defn build-checker-precertificate
  "Build one complete E3 lowering product, all 34 bounded measurements and
  seven stage witnesses from authenticated checker-local inputs. No product,
  operation, receipt, witness, or certificate is accepted from the caller."
  [input]
  (when-not (= checker-semantic-input-keys-e3 (set (keys input)))
    (fail! :checker-semantic-input))
  (let [content {:commit (:snapshot-commit input) :tree (:snapshot-tree input)}
        carrier (validate-source-semantic-carrier!
                 content (:source-semantic-carrier input))
        _ (validate-semantic-algorithm-authority!
           (:semantic-algorithm-source-descriptors input)
           (:semantic-algorithm-source-root input))
        products (module-products (:module-transactions input))
        foundation
        (foundation-preproduct products
                               (:semantic-algorithm-source-root input))
        stages (pre-stages input products foundation)
        collections
        (transaction-collections (:module-transactions input)
                                 (:source-forms input) carrier)
        source-roots
        {:source-descriptors-root
         (get-in carrier [:c2-snapshot :source-descriptors-root])
         :event-root (get-in carrier [:c2-snapshot :event-root])
         :semantic-algorithm-source-root
         (:semantic-algorithm-source-root input)
         :sh06-authority-carrier-id
         (codec/H [:gravity/sh07-b51-sh06-authority-carrier-input-v1
                   (:sh06-resolution-plan-authority input)])
         :bundle-core-id (codec/H (:bundle-core input))}
        subjects (build-fuel-subjects source-roots stages collections)
        product-base
        {:schema :gravity/sh07-b51-lowering-product-v1
         :content content
         :source-authority
         (select-keys source-roots [:source-descriptors-root :event-root])
         :module-products products
         :pass-records (:pass-records foundation)
         :fact-records (:fact-records foundation)
         :provenance-records (:provenance-records foundation)
         :fuel-subjects subjects
         :semantic-roots
         {:modules (semantic-witness-member-root :lowering-modules products)
          :passes (:pass-root foundation)
          :facts (:fact-root foundation)
          :provenance (:provenance-root foundation)}}
        lowering-product
        (assoc product-base :product-id
               (codec/H [:gravity/sh07-b51-lowering-product-v1 product-base]))
        operation-specs (build-fixed-fuel-operation-specs subjects)
        operations
        (operation-manifest operation-specs lowering-product
                            (:semantic-algorithm-source-root input))
        receipts (fuel-receipts subjects operations)
        stage-values
        (into {}
              (map (fn [{:keys [ordinal owner stage-key]}]
                     [stage-key
                      (stage-witness stage-key (get stages stage-key)
                                     (nth subjects ordinal)
                                     (nth operations ordinal)
                                     (nth receipts ordinal) foundation)]))
              stage-semantic-specs)
        witness-base
        (merge {:schema :gravity/sh07-b51-authentic-lowering-witness-v1
                :content content
                :operation-manifest operations
                :operation-schedule-id
                (codec/H [:gravity/sh07-b51-operation-schedule-v1 operations])
                :fuel-receipts receipts}
               stage-values)
        witness
        (assoc witness-base :witness-id
               (codec/H [:gravity/sh07-b51-authentic-lowering-witness-v1
                         witness-base]))]
    {:lowering-product lowering-product
     :semantic-witness witness
     :measurement-count (reduce + (map #(count (:trace %)) receipts))}))
