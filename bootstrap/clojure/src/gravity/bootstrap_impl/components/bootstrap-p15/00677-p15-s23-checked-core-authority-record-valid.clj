

(defn p15-s23-checked-core-authority-record-valid?
  [record]
  (try
   (p15-s23-reference-runtime-bounded-value!
    "p15-s23-checked-core-authority" :jvm
    :checked-core-authority-record record
    p15-s23-reference-runtime-max-contract-nodes
    p15-s23-reference-runtime-max-contract-depth)
   (let [structural-operation-set (:structural-operation-set record)
         writes-stdout? (contains? structural-operation-set :println)
         expected-effects
         (cond-> #{}
           (contains? structural-operation-set :str)
           (conj :memory/allocate)
           writes-stdout? (conj :io/write))
         expected-capabilities
         (cond-> #{}
           (contains? structural-operation-set :str)
           (conj :memory/allocator)
           writes-stdout? (conj :io/stdout))
         expected-source-binding
         (p15-s23-checked-core-authority-source-binding
          (:source-content-hash record) (:plan-id record) (:module record)
          structural-operation-set)
         expected-program-records
         (p15-s23-checked-core-program-authority-records
          expected-source-binding (:program-principal record)
          structural-operation-set
          p15-s23-checked-core-expected-program-authority-policy)
         expected-runtime-providers
         (cond-> #{:gravity.reference/jvm-managed-allocator}
           writes-stdout? (conj :gravity.reference/transcript-capture))
         expected-runtime-grants
         (cond-> #{:gravity.reference/grant-managed-allocation}
           writes-stdout?
           (conj :gravity.reference/grant-reference-stdout))
         expected-handler-providers
         (if writes-stdout?
           #{:gravity.reference/transcript-capture} #{})
         expected-handler-grants
         (if writes-stdout?
           #{:gravity.reference/grant-test-fixture} #{})
         expected-adapter
         (p15-s23-reference-runtime-authority
          nil {:observed-operation-set
               (if writes-stdout? #{:println} #{})})]
   (and
   (map? record)
   (= p15-s23-checked-core-authority-record-keys (set (keys record)))
   (= :gravity/p15-s23-checked-core-authority-binding-v1 (:kind record))
   (= 1 (:schema-version record))
   (set? (:structural-operation-set record))
   (seq (:structural-operation-set record))
   (set/subset? (:structural-operation-set record) #{:str :println})
   (set? (:required-effects record))
   (set? (:required-capabilities record))
   (= expected-effects (:required-effects record))
   (= expected-capabilities (:required-capabilities record))
   (= :hosted (:profile record))
   (= :jvm (:source-target record))
   (= (:module record) (:program-principal record))
   (symbol? (:program-principal record))
   (symbol? (:runtime-principal record))
   (keyword? (:handler-principal record))
   (= 3 (count (set [(:program-principal record)
                      (:runtime-principal record)
                      (:handler-principal record)])))
   (= p15-s23-checked-core-program-authority-policy-id
      (:program-authority-policy-id record))
   (= (p15-s23-closed-core-digest
      p15-s23-checked-core-expected-program-authority-policy)
      (:program-authority-policy-hash record))
   (= p15-s23-stage2-runtime-artifact-expected-source-content-hash
      (:runtime-source-content-hash record))
   (= p15-s23-reference-runtime-expected-contract-definition-hash
      (:runtime-contract-definition-hash record))
   (= p15-s23-reference-runtime-expected-derived-facts-hash
      (:runtime-contract-derived-facts-hash record))
   (= p15-s23-stage2-runtime-artifact-expected-artifact-hash
      (:runtime-artifact-hash record))
   (= p15-s23-stage2-runtime-artifact-closed-plan-function
      (:runtime-function record))
   (= (get p15-s23-reference-runtime-expected-function-hashes
           p15-s23-stage2-runtime-artifact-closed-plan-function)
      (:runtime-function-hash record))
   (= :authenticated-checked-core-reference-interpreter (:scope record))
   (= :runtime (:phase record))
   (= :single-reference-execution (:lifetime record))
   (= 'gravity.bootstrap.p15-s23.runtime (:runtime-principal record))
   (= :gravity.bootstrap/reference-harness (:handler-principal record))
   (vector? (:program-provider-records record))
   (vector? (:program-grant-records record))
   (= (count (:program-provider-records record))
      (count (:program-grant-records record)))
   (every? p15-s23-checked-core-program-provider-record-valid?
           (:program-provider-records record))
   (every? p15-s23-checked-core-program-grant-record-valid?
           (:program-grant-records record))
   (= (:provider-records expected-program-records)
      (:program-provider-records record))
   (= (:grant-records expected-program-records)
      (:program-grant-records record))
   (every?
    (fn [[provider grant]]
      (and (= (:provider-selection-id provider)
              (:provider-selection-id grant))
           (= (select-keys provider
                           [:principal-id :effect :capability :provider-id
                            :profile :target :phase :scope :source-binding
                            :lifetime :policy-id])
              (select-keys grant
                           [:principal-id :effect :capability :provider-id
                            :profile :target :phase :scope :source-binding
                            :lifetime :policy-id]))))
    (map vector (:program-provider-records record)
         (:program-grant-records record)))
   (= (:program-principal record)
      (get-in record [:program-provider-records 0 :principal-id]
              (:program-principal record)))
   (every? #(= (:program-principal record) (:principal-id %))
           (:program-provider-records record))
   (every? #(= (:program-principal record) (:principal-id %))
           (:program-grant-records record))
   (every? set? (map record
                     [:runtime-provider-ids :runtime-grant-ids
                      :handler-provider-ids :handler-grant-ids]))
   (= expected-runtime-providers (:runtime-provider-ids record))
   (= expected-runtime-grants (:runtime-grant-ids record))
   (= expected-handler-providers (:handler-provider-ids record))
   (= expected-handler-grants (:handler-grant-ids record))
   (map? (:adapter-authority record))
   (= p15-s23-reference-runtime-authority-keys
      (set (keys (:adapter-authority record))))
   (= (:runtime-principal record)
      (get-in record [:adapter-authority :source-principal]))
   (= (:handler-principal record)
      (get-in record [:adapter-authority :handler-principal]))
   (= (set/union (:runtime-provider-ids record)
                 (:handler-provider-ids record))
      (get-in record [:adapter-authority :providers]))
   (= (set/union (:runtime-grant-ids record)
                 (:handler-grant-ids record))
      (get-in record [:adapter-authority :grants]))
   (= expected-adapter (:adapter-authority record))
   (= :closed-plan-reference
      (get-in record [:adapter-authority :mode]))
   (nil? (get-in record [:adapter-authority :failure-injection]))
   (false? (get-in record [:adapter-authority :deployment-stdout?]))
   (true? (:single-invocation? record))
   (true? (:reference-interpreter? record))
   (false? (:deployment-runtime? record))
   (false? (:live-external-io? record))
   (= (:authority-record-id record)
      (p15-s23-closed-core-digest (dissoc record :authority-record-id)))))
   (catch StackOverflowError _ false)
   (catch Exception _ false)))

(def p15-s23-checked-core-authority-evidence-keys
  #{:kind :authority-record-id :source-content-hash :plan-id :module
    :structural-operation-set :required-effects :required-capabilities
    :program-authority-policy-id
    :program-authority-policy-hash :program-principal :runtime-principal
    :handler-principal :provider-bindings :grant-bindings
    :runtime-provider-ids :runtime-grant-ids :handler-provider-ids
    :handler-grant-ids :adapter-authority-hash
    :runtime-source-content-hash :runtime-contract-definition-hash
    :runtime-contract-derived-facts-hash :runtime-artifact-hash
    :runtime-function :runtime-function-hash :scope :phase :lifetime
    :single-invocation? :reference-interpreter? :deployment-runtime?
    :live-external-io? :active-authority? :non-authorizing-projection?
    :status :evidence-id})

(defn p15-s23-checked-core-authority-evidence
  [authority-record]
  (let [providers
        (into
         (sorted-map)
         (map (fn [record]
                [(:capability record)
                 (select-keys record
                              [:provider-selection-id :effect :capability
                               :provider-id :scope :policy-id :policy-hash
                               :provider-provenance])]))
         (:program-provider-records authority-record))
        grants
        (into
         (sorted-map)
         (map (fn [record]
                [(:capability record)
                 (select-keys record
                              [:grant-id :provider-selection-id :effect
                               :capability :provider-id :scope :policy-id
                               :policy-hash :audit-policy-id
                               :program-grant-template-id
                               :provider-provenance])]))
         (:program-grant-records authority-record))
        base
        {:kind :gravity/p15-s23-checked-core-authority-evidence
         :authority-record-id (:authority-record-id authority-record)
         :source-content-hash (:source-content-hash authority-record)
         :plan-id (:plan-id authority-record)
         :module (:module authority-record)
         :structural-operation-set
         (:structural-operation-set authority-record)
         :required-effects (:required-effects authority-record)
         :required-capabilities (:required-capabilities authority-record)
         :program-authority-policy-id
         (:program-authority-policy-id authority-record)
         :program-authority-policy-hash
         (:program-authority-policy-hash authority-record)
         :program-principal (:program-principal authority-record)
         :runtime-principal (:runtime-principal authority-record)
         :handler-principal (:handler-principal authority-record)
         :provider-bindings providers
         :grant-bindings grants
         :runtime-provider-ids (:runtime-provider-ids authority-record)
         :runtime-grant-ids (:runtime-grant-ids authority-record)
         :handler-provider-ids (:handler-provider-ids authority-record)
         :handler-grant-ids (:handler-grant-ids authority-record)
         :adapter-authority-hash
         (p15-s23-closed-core-digest (:adapter-authority authority-record))
         :runtime-source-content-hash
         (:runtime-source-content-hash authority-record)
         :runtime-contract-definition-hash
         (:runtime-contract-definition-hash authority-record)
         :runtime-contract-derived-facts-hash
         (:runtime-contract-derived-facts-hash authority-record)
         :runtime-artifact-hash (:runtime-artifact-hash authority-record)
         :runtime-function (:runtime-function authority-record)
         :runtime-function-hash (:runtime-function-hash authority-record)
         :scope (:scope authority-record)
         :phase (:phase authority-record)
         :lifetime (:lifetime authority-record)
         :single-invocation? (:single-invocation? authority-record)
         :reference-interpreter? (:reference-interpreter? authority-record)
         :deployment-runtime? (:deployment-runtime? authority-record)
         :live-external-io? (:live-external-io? authority-record)
         :active-authority? false
         :non-authorizing-projection? true
         :status :authenticated-and-consumed}]
    (assoc base :evidence-id (p15-s23-closed-core-digest base))))