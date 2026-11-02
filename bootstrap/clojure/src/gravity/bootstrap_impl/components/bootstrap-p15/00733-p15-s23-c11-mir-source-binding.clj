

(defn p15-s23-c11-mir-source-binding!
  ([request-source requested-target]
   (p15-s23-c11-mir-source-binding! request-source requested-target 1))
  ([request-source requested-target version]
  (try
    (p15-s23-c11-mir-source-binding!*
     request-source requested-target version)
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
       request-source :contained-c11-source-binding-host-failure exception)))))

(defn p15-s23-c11-mir-bounded-value!
  "Legacy entrypoint for semantic native-carrier validation, without quotas."
  ([source-path definition value]
  (try
    ;; Establish exact native classes, comparators, metadata and acyclicity
    ;; before traversing. Numeric representations are checked by this helper;
    ;; Float/Double remain legitimate carriers, not canonical digest values.
    (let [validation (p15-s23-trusted-carrier-validation value :default-only)]
      (when-not (= :passed (:status validation))
        (p15-s23-c11-mir-fail!
         "C11-VERIFY" source-path {}
         {:missing-fact :valid-native-c11-carrier
          :reason (:reason validation)
          :runtime-contract-definition definition})))
    (loop [pending [value]]
      (when (seq pending)
        (let [item (peek pending) pending (pop pending)
              texts (cond (string? item) [item]
                          (or (keyword? item) (symbol? item))
                          (cond-> [(name item)] (namespace item) (conj (namespace item)))
                          :else [])]
          (when-not
           (and (every? #(and (string? %)
                              (p15-s23-c6c10-valid-unicode-string? %)) texts)
                (not (and (char? item) (<= 0xD800 (int item) 0xDFFF))))
            (p15-s23-c11-mir-fail!
             "C11-VERIFY" source-path {}
             {:missing-fact :well-formed-unicode-c11-carrier
              :runtime-contract-definition definition}))
          (recur (cond
                   (map? item) (reduce-kv #(conj %1 %2 %3) pending item)
                   (or (vector? item) (set? item) (seq? item))
                   (reduce conj pending item)
                   :else pending)))))
    {:status :passed}
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
      (p15-s23-c11-mir-contain-exception!
       source-path :contained-c11-carrier-diagnostic ex))
    (catch Exception error
      (p15-s23-c11-mir-contain-exception!
       source-path :contained-c11-carrier-host-failure error))))
  ;; Existing consumers can migrate independently; these arguments never admit
  ;; or reject a carrier and no accounting measurements are returned.
  ([source-path definition value _maximum-nodes _maximum-depth]
   (p15-s23-c11-mir-bounded-value! source-path definition value)))

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
