
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
