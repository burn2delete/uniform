

(defn p15-s23-reference-runtime-success-adapter-record
  [plan-id source-id io-write-active? capture-invoked?]
  (let [runtime-binding
        {:runtime-artifact-hash
         p15-s23-stage2-runtime-artifact-expected-artifact-hash
         :runtime-contract-definition-hash
         p15-s23-reference-runtime-expected-contract-definition-hash
         :runtime-contract-derived-facts-hash
         p15-s23-reference-runtime-expected-derived-facts-hash
         :runtime-artifact-source-content-hash
         p15-s23-stage2-runtime-artifact-expected-source-content-hash}
        source-principal 'gravity.bootstrap.p15-s23.runtime
        handler-principal :gravity.bootstrap/reference-harness
        allocation-provider :gravity.reference/jvm-managed-allocator
        capture-provider :gravity.reference/transcript-capture
        allocation-grant :gravity.reference/grant-managed-allocation
        stdout-grant :gravity.reference/grant-reference-stdout
        fixture-grant :gravity.reference/grant-test-fixture
        decision
        #(p15-s23-reference-runtime-decision-record
          runtime-binding (merge {:plan-id plan-id :source-id source-id} %))
        action
        #(p15-s23-reference-runtime-action-record
          runtime-binding (merge {:plan-id plan-id :source-id source-id} %))
        allocation-decision
        (decision
         {:action-id :gravity.reference/action-managed-string-allocation
          :principal-id source-principal
          :effect :memory/allocate
          :capability :memory/allocator
          :provider-id allocation-provider
          :grant-id allocation-grant
          :scope :pinned-runtime-plan
          :mode :pinned-reference
          :live-external-authority? false
          :decision :grant
          :result :grant
          :reason :explicit-grant})
        stdout-decision
        (decision
         {:action-id :gravity.reference/action-transcript-capture
          :principal-id source-principal
          :effect :io/write
          :capability :io/stdout
          :provider-id capture-provider
          :grant-id stdout-grant
          :handler-principal-id handler-principal
          :scope :closed-plan-interpreter
          :mode :reference-test-interpreter
          :live-external-authority? false
          :decision :grant
          :result :grant
          :reason :closed-plan-reference-handler})
        fixture-decision
        (decision
         {:action-id :gravity.reference/action-transcript-capture
          :principal-id handler-principal
          :effect :io/write
          :capability :test/fixture
          :provider-id capture-provider
          :grant-id fixture-grant
          :source-principal-id source-principal
          :scope :closed-plan-interpreter
          :mode :reference-test-interpreter
          :live-external-authority? false
          :decision :grant
          :result :grant
          :reason :explicit-reference-harness-policy})
        allocation-action
        (action
         {:action-id :gravity.reference/action-managed-string-allocation
          :operation :managed-allocation
          :provider-id allocation-provider
          :effect :memory/allocate
          :capability :memory/allocator
          :grant-id allocation-grant
          :principal-id source-principal
          :scope :pinned-runtime-plan
          :mode :pinned-reference
          :live-external-authority? false
          :action-started? true
          :action-status :committed
          :result-committed? true
          :output-committed? false
          :diagnostic nil
          :remediation :none})
        capture-action
        (action
         {:action-id :gravity.reference/action-transcript-capture
          :operation :ordered-string-append
          :provider-id capture-provider
          :effect :io/write
          :handled-effect :io/write
          :source-capability :io/stdout
          :source-grant-id stdout-grant
          :capability :test/fixture
          :grant-id fixture-grant
          :principal-id handler-principal
          :source-principal-id source-principal
          :scope :closed-plan-interpreter
          :mode :reference-test-interpreter
          :live-external-authority? false
          :action-started? capture-invoked?
          :action-status (if capture-invoked? :committed :not-invoked)
          :result-committed? capture-invoked?
          :output-committed? capture-invoked?
          :diagnostic nil
          :remediation :none})
        provider-ids
        (cond-> #{allocation-provider}
          io-write-active? (conj capture-provider))
        grant-ids
        (cond-> #{allocation-grant}
          io-write-active? (conj stdout-grant fixture-grant))
        record
        {:artifact :gravity/p15-s23-reference-runtime-adapter-record
         :status :complete
         :mode :closed-plan-reference
         :runtime-artifact-hash
         p15-s23-stage2-runtime-artifact-expected-artifact-hash
         :runtime-contract-definition-hash
         p15-s23-reference-runtime-expected-contract-definition-hash
         :runtime-contract-derived-facts-hash
         p15-s23-reference-runtime-expected-derived-facts-hash
         :function p15-s23-stage2-runtime-artifact-closed-plan-function
         :function-hash
         (get p15-s23-reference-runtime-expected-function-hashes
              p15-s23-stage2-runtime-artifact-closed-plan-function)
         :plan-id plan-id
         :source-id source-id
         :source-principal source-principal
         :handler-principal handler-principal
         :provider-ids provider-ids
         :grant-ids grant-ids
         :authority
         (dissoc
          (p15-s23-reference-runtime-authority
           nil {:observed-operation-set
                (if io-write-active? #{:println} #{})})
          :failure-injection)
         :decision-records
         (cond-> [allocation-decision]
           io-write-active? (conj stdout-decision fixture-decision))
         :action-records
         (cond-> [allocation-action]
           io-write-active? (conj capture-action))
         :io-write-active? io-write-active?
         :reference-interpreter? true
         :deployment-runtime? false
         :clojure-seed-boundary? true
         :self-hosted? false}]
    (assoc record :record-hash
           (p15-s23-reference-runtime-hash record))))

(def p15-s23-reference-runtime-preserved-diagnostic-ids
  #{"P15S23X001" "P15S23X002" "P15S23X003"
    "L2-UNKNOWN-CORE-FORM" "L2-UNKNOWN-SYMBOL" "L2-BUILTIN-ARITY"
    "L2-BUILTIN-ERROR" "L2-FUNCTION-ARITY" "L2-MAIN-ARITY"})

(defn p15-s23-reference-runtime-structured-diagnostic?
  [exception allowed-source-paths]
  (let [data (ex-data exception)
        source (get-in data [:source-span :source])]
    (and (map? data)
         (contains? p15-s23-reference-runtime-preserved-diagnostic-ids
                    (:id data))
         (string? (:message data))
         (= :stage0 (:bootstrap-stage data))
         (keyword? (:stage data))
         (keyword? (:diagnostic-family data))
         (map? (:source-span data))
         (contains? allowed-source-paths source)
         (contains? data :remediation))))

(def p15-s23-checked-core-reference-result-keys
  #{:artifact :entrypoint :entrypoint-result :stdout :status
    :clojure-seed-boundary? :self-hosted?})

(defn p15-s23-checked-core-reference-result-valid?
  [result plan]
  (try
    (and
     (map? result)
     (contains?
      p15-s23-reference-runtime-supported-collection-class-names
      (some-> result class .getName))
     (<= (count result) 7)
     (do
       (p15-s23-reference-runtime-bounded-value!
        "checked-core-reference-result" :jvm
        :checked-core-reference-result result
        p15-s23-reference-runtime-max-contract-nodes
        p15-s23-reference-runtime-max-contract-depth)
       true)
     (= p15-s23-checked-core-reference-result-keys
        (set (keys result)))
     (= :gravity/p15-s23-runtime-closed-plan-execution-record
        (:artifact result))
     (= (:entrypoint plan) (:entrypoint result))
     (symbol? (:entrypoint result))
     (or (nil? (:entrypoint-result result))
         (string? (:entrypoint-result result))
         (boolean? (:entrypoint-result result))
         (integer? (:entrypoint-result result))
         (char? (:entrypoint-result result))
         (keyword? (:entrypoint-result result))
         (symbol? (:entrypoint-result result)))
     (string? (:stdout result))
     (= :complete (:status result))
     (true? (:clojure-seed-boundary? result))
     (false? (:self-hosted? result)))
    (catch StackOverflowError _ false)
    (catch Exception _ false)))