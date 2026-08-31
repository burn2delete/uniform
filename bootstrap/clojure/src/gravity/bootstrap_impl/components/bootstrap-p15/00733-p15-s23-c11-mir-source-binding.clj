

(defn p15-s23-c11-mir-source-binding!
  [request-source requested-target]
  (try
    (p15-s23-c11-mir-source-binding!*
     request-source requested-target)
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch StackOverflowError error
      (p15-s23-c11-mir-contain-exception!
       request-source :contained-c11-source-binding-host-stack error))
    (catch AssertionError error
      (p15-s23-c11-mir-contain-exception!
       request-source :contained-c11-source-binding-assertion error))
    (catch LinkageError error
      (p15-s23-c11-mir-contain-exception!
       request-source :contained-c11-source-binding-linkage error))
    (catch clojure.lang.ExceptionInfo exception
      (p15-s23-c11-mir-contain-exception!
       request-source :contained-c11-source-binding-diagnostic exception))
    (catch Exception exception
      (p15-s23-c11-mir-contain-exception!
       request-source :contained-c11-source-binding-host-failure exception))))

(defn p15-s23-c11-mir-bounded-value!
  ([source-path definition value]
   (p15-s23-c11-mir-bounded-value!
    source-path definition value
    p15-s23-c11-mir-max-carrier-nodes
    p15-s23-c11-mir-max-carrier-depth))
  ([source-path definition value maximum-nodes maximum-depth]
  (try
    (p15-s23-reference-runtime-bounded-value!
     (p15-s23-c11-mir-safe-source-path source-path)
     :target-neutral
     definition
     value
     maximum-nodes
     maximum-depth)
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch StackOverflowError error
      (p15-s23-c11-mir-contain-exception!
       source-path :contained-c11-carrier-host-stack error))
    (catch AssertionError error
      (p15-s23-c11-mir-contain-exception!
       source-path :contained-c11-carrier-assertion error))
    (catch LinkageError error
      (p15-s23-c11-mir-contain-exception!
       source-path :contained-c11-carrier-linkage error))
    (catch clojure.lang.ExceptionInfo ex
      (let [data (ex-data ex)
            owned-bound?
            (and (= "P15S23X002" (:id data))
                 (= :p15-s23-stage2-runtime-executor (:stage data))
                 (= :p15-s23-stage2-runtime-executor
                    (:diagnostic-family data))
                 (= :pinned-reference-runtime-contract (:boundary data))
                 (= definition (:runtime-contract-definition data))
                 (= :target-neutral (:target data))
                 (false? (:result-committed? data))
                 (false? (:output-committed? data))
                 (contains?
                  #{:runtime-contract-value-bounds
                    :runtime-contract-collection-width
                    :runtime-contract-total-scalar-bounds
                    :runtime-contract-scalar-bounds
                    :runtime-contract-unsupported-scalar
                    :runtime-contract-unsupported-collection}
                  (:missing-fact data)))]
        (if owned-bound?
          (p15-s23-c11-mir-fail!
           "C11-VERIFY" source-path {}
           (merge
            {:missing-fact :bounded-c11-carrier
             :bounded-reason (:missing-fact data)
             :runtime-contract-definition definition}
            (select-keys
             data
             [:observed-nodes :observed-depth :observed-width
              :observed-total-scalar-bytes :maximum-nodes
              :maximum-depth :maximum-width
              :maximum-total-scalar-bytes])))
          (p15-s23-c11-mir-contain-exception!
           source-path :contained-c11-carrier-diagnostic ex))))
    (catch Exception error
      (p15-s23-c11-mir-contain-exception!
       source-path :contained-c11-carrier-host-failure error)))))

(defn p15-s23-c11-mir-metadata-free?
  [value]
  (loop [stack [value]]
    (if (empty? stack)
      true
      (let [item (peek stack)
            stack (pop stack)]
        (if (and (instance? clojure.lang.IObj item)
                 (some? (meta item)))
          false
          (cond
            (map? item)
            (recur
             (reduce (fn [pending [key child]]
                       (conj pending key child))
                     stack item))

            (or (vector? item) (list? item) (set? item))
            (recur (reduce conj stack item))

            :else (recur stack)))))))

(defn p15-s23-c11-mir-require-strict-structure!
  [source-path expected actual missing-fact]
  (when (p15-s23-c6c10-strict-first-mismatch
         source-path expected actual [])
    (p15-s23-c11-mir-fail!
     "C11-VERIFY" source-path {}
     {:missing-fact missing-fact})))

(defn p15-s23-c11-mir-require!
  [condition id source-path subject missing-fact]
  (when-not condition
    (p15-s23-c11-mir-fail!
     id source-path subject {:missing-fact missing-fact})))

(defn p15-s23-c11-mir-node-opcode
  [node]
  (case (:source-operation node)
    :literal :constant
    :implicit-nil :constant
    :quote :constant
    :local :local
    :let-binding :local-binding
    :truthy :truthiness
    :integer-eq :integer-eq
    :integer-lt :integer-lt
    :integer-lte :integer-lte
    :integer-gt :integer-gt
    :integer-gte :integer-gte
    :do :sequence
    :if :conditional-join
    :let :lexical-scope
    :str :call
    :println :call
    :function :function-boundary
    :unsupported-checked-core-operation))

(defn p15-s23-c11-mir-node-facts
  [checked-core node]
  (let [node-id (:node-id node)
        safety (:safety node)
        runtime-check (:check safety)
        capability-fact (get (:capability-facts checked-core) node-id)
        safety-proof (:proof safety)]
    {:type-fact-id (get-in checked-core [:type-facts node-id :fact-id])
     :effect-fact-id (get-in checked-core [:effect-facts node-id :fact-id])
     :capability-fact-id (str node-id ":capability-fact")
     :capability-proof-ids
     (vec (get-in capability-fact [:capability-proof :proof-ids] []))
     :ownership-fact-id
     (get-in checked-core [:ownership-facts node-id :fact-id])
     :safety-outcome-id (str node-id ":safety-outcome")
     :safety-proof-id (or (:proof-id safety-proof) :not-applicable)
     :profile-target-fact-id (str node-id ":profile-target-fact")
     :runtime-check-id (if runtime-check
                         (:check-id runtime-check)
                         :not-applicable)
     :proof-certificate-ids
     (cond-> (vec (get-in capability-fact
                          [:capability-proof :proof-ids] []))
       (:proof-id safety-proof) (conj (:proof-id safety-proof)))
     :failure-behavior (if runtime-check
                         (:failure runtime-check)
                         :not-applicable)
     :source-origin-id (get-in node [:source :origin-id])}))

(defn p15-s23-c11-mir-runtime-check-operation-id
  [check]
  (str (:check-id check) ":mir:runtime-check"))

(defn p15-s23-c11-mir-runtime-check-token-id
  [check]
  (str (:check-id check) ":mir:token"))

(defn p15-s23-c11-mir-node-operands
  [node]
  (if-let [check (get-in node [:safety :check])]
    (into [(p15-s23-c11-mir-runtime-check-token-id check)]
          (:operands node))
    (:operands node)))

(defn p15-s23-c11-mir-runtime-check-source
  [node]
  (let [check (get-in node [:safety :check])
        source (:source node)]
    {:origin-id (:origin-id source)
     :span (:span source)
     :enclosing-syntax-origin-id (:enclosing-syntax-origin-id source)
     :generated? true
     :generated-origin
     [{:role :runtime-check-generated
       :producer-operation-id (:node-id node)
       :runtime-check-id (:check-id check)}]}))

(defn p15-s23-c11-mir-runtime-check-fact-id
  [check fact-kind]
  (str (:check-id check)
       (case fact-kind
         :type ":mir:type-fact"
         :effect ":mir:effect-fact"
         :capability ":mir:capability-fact"
         :ownership ":mir:ownership-fact"
         :safety ":mir:safety-outcome"
         :profile-target ":mir:profile-target-fact")))