(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn- p15-s23-c13-c14-b1-invoke!
  [candidate source-path binding function-name arguments diagnostic]
  (p15-s23-c13-c14-b1-require-authority!
   candidate source-path :execute-pinned-gravity-bridge-source)
  (let [result
        (try
          (p15-s23-stage2-runtime-execute-function
           {:engine :gravity-c13-c14-b1-pinned-source-host-runner
            :compiler-artifact-plan? true}
           (:plan binding) function-name arguments)
          (catch InterruptedException interrupted
            (.interrupt (Thread/currentThread))
            (throw interrupted))
          (catch StackOverflowError _
            (p15-s23-b3-llvm-fail!
             diagnostic source-path {}
             {:missing-fact :bounded-gravity-bridge-builder-host-stack}))
          (catch AssertionError error
            (p15-s23-b3-llvm-contain-exception!
             source-path :contained-gravity-bridge-builder-assertion error))
          (catch LinkageError error
            (p15-s23-b3-llvm-contain-exception!
             source-path :contained-gravity-bridge-builder-linkage error)))]
    (p15-s23-c13-c14-b1-require-trusted!
     source-path :gravity-bridge-builder-result result :default-only)
    (when (= :rejected (:status result))
      (p15-s23-b3-llvm-fail!
       (or (:diagnostic result) diagnostic) source-path result
       (merge
        {:missing-fact (or (:missing-fact result)
                           :rejected-gravity-bridge-builder)}
        (select-keys result
                     [:operation-id :opcode :source-operation
                      :observed-type]))))
    result))

(defn p15-s23-c13-c14-b1-content-binding
  [value]
  {:content-id
   (p15-s23-c11-mir-digest
    (p15-s23-c11-mir-path-neutral-value value))
   :entry-count (if (coll? value) (count value) 1)})

(def p15-s23-sh02-authenticated-envelope-bounds
  {:maximum-semantic-projections 64
   :maximum-fact-transitions 64
   :maximum-identity-subjects 64
   :maximum-lineage-records 32
   :maximum-reference-nodes 128
   :maximum-reference-edges 128
   :maximum-reference-depth 64
   :maximum-logical-source-path-code-units 128
   :maximum-reference-id-code-units 128
   :maximum-digest-requests 2048
   :maximum-carrier-nodes 65536
   :maximum-carrier-depth 64
   :maximum-container-width 128
   :maximum-scalar-bytes 65536
   :maximum-integer-bits 256})

(def p15-s23-sh02-fact-families
  [[:type :type-table]
   [:effect :effect-table]
   [:ownership :ownership-table]
   [:capability :capability-table]
   [:capability-proof :capability-proof-table]
   [:safety :safety-table]
   [:runtime-check :runtime-check-table]
   [:proof-certificate :proof-certificate-table]
   [:source-map :source-map]
   [:effect-order :effect-order-graph]])

(defn p15-s23-sh02-value-entry-count
  [value]
  (cond
    (nil? value) 0
    (coll? value) (count value)
    :else 1))

(defn p15-s23-sh02-sha256-id?
  [value]
  (and (string? value)
       (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn p15-s23-sh02-contained-values
  [value]
  (tree-seq
   coll?
   (fn [item]
     (if (map? item)
       (mapcat identity item)
       item))
   value))

(defn p15-s23-sh02-sha256-ids
  [value]
  (->> (p15-s23-sh02-contained-values value)
       (filter p15-s23-sh02-sha256-id?)
       distinct
       sort
       vec))

(defn p15-s23-sh02-stage-source-revision
  [stage-record logical-source-path]
  (let [rule (:source-rule stage-record)]
    {:owner (:owner rule)
     :source-language :gravity
     :logical-source-path logical-source-path
     :source-content-hash (:source-content-hash rule)
     :source-byte-count (:source-byte-count rule)
     :plan-semantic-hash (:plan-semantic-hash rule)
     :functions-semantic-hash (:functions-semantic-hash rule)
     :builder-function (:builder-function rule)
     :builder-semantic-hash (:builder-semantic-hash rule)
     :function-shapes (:function-shapes rule)}))

(defn p15-s23-sh02-projection
  [name role value]
  (let [value (p15-s23-c13-c14-b1-path-neutral-value value)]
    {:name name :role role
     :entry-count (p15-s23-sh02-value-entry-count value)
     :value value}))

(defn p15-s23-sh02-stage-semantic-projections
  [stage packet]
  (let [record (get packet stage)]
    (case stage
      :c13
      [(p15-s23-sh02-projection :stage-input :pass-input (:input record))
       (p15-s23-sh02-projection
        :optimized-mir :pass-output (:optimized-mir record))
       (p15-s23-sh02-projection
        :pass-contract :pass-contract (:pass-contract record))
       (p15-s23-sh02-projection
        :decision-record :decision (:decision-record record))
       (p15-s23-sh02-projection
        :verifier-replay :verification (:verifier-replay record))]

      :b1
      [(p15-s23-sh02-projection :stage-input :backend-input (:input record))
       (p15-s23-sh02-projection
        :profile-contract :compile-time-contract (:profile record))
       (p15-s23-sh02-projection
        :target-contract :target-selection (:target record))
       (p15-s23-sh02-projection :abi-contract :abi (:abi record))
       (p15-s23-sh02-projection
        :runtime-contract :runtime (:runtime record))
       (p15-s23-sh02-projection
        :provider-selection :providers (:providers record))
       (p15-s23-sh02-projection
        :backend-manifest :backend-contract (:backend-manifest record))
       (p15-s23-sh02-projection
        :contract-bindings :preserved-facts (:contract-bindings record))]

      (throw (ex-info "Unsupported SH-02 envelope stage"
                      {:stage stage})))))

(defn p15-s23-sh02-stage-evidence-ids
  [stage packet]
  (let [record (get packet stage)]
    (case stage
      :c13
      [(get-in record [:input :verifier-report-id])
       (get-in record [:decision-record :decision-id])
       (:artifact-id record)]

      :b1
      [(get-in packet [:c14 :artifact-id])
       (get-in packet [:c14 :request :request-id])
       (:artifact-id record)])))

(defn p15-s23-sh02-fact-transitions
  [stage packet]
  (let [mir (:optimized-mir packet)
        evidence-ids (p15-s23-sh02-stage-evidence-ids stage packet)]
    (mapv
     (fn [[name mir-key]]
       (let [value
             (p15-s23-c13-c14-b1-path-neutral-value (get mir mir-key))
             entry-count (p15-s23-sh02-value-entry-count value)]
         {:name name
          :disposition :preserved
          :input value :output value
          :input-count entry-count :output-count entry-count
          :evidence-ids evidence-ids}))
     p15-s23-sh02-fact-families)))

(defn p15-s23-sh02-effect-capability-relation
  [stage packet]
  (let [mir (:optimized-mir packet)
        record (get packet stage)]
    (p15-s23-c13-c14-b1-path-neutral-value
     {:effect-facts (:effect-table mir)
      :capability-facts (:capability-table mir)
      :capability-proof-facts (:capability-proof-table mir)
      :effect-order (:effect-order-graph mir)
      :provider-selections (if (= stage :b1) (:providers record) {})
      :grant-scopes (if (= stage :b1) (:capabilities record) #{})})))

(defn p15-s23-sh02-proof-composite
  [stage packet]
  (let [mir (:optimized-mir packet)
        record (get packet stage)
        proof-records
        (if (= stage :b1)
          (:proofs record)
          (get-in record [:decision-record :proofs-used]))]
    (p15-s23-c13-c14-b1-path-neutral-value
     {:proof-records proof-records
      :proof-certificate-table (:proof-certificate-table mir)
      :proof-summary
      (p15-s23-c13-c14-b1-content-binding
       {:capability-proofs (:capability-proof-table mir)
        :proof-certificates (:proof-certificate-table mir)})
      :proof-usage
      {:stage stage
       :identity-pass? (= stage :c13)
       :backend-admission? (= stage :b1)}}))))
