

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
  (gravity.mir/node-opcode node))

(defn p15-s23-c11-mir-node-facts
  [checked-core node]
  (gravity.mir/node-facts checked-core node))

(defn p15-s23-c11-mir-runtime-check-operation-id
  [check]
  (gravity.mir/runtime-check-operation-id check))

(defn p15-s23-c11-mir-runtime-check-token-id
  [check]
  (gravity.mir/runtime-check-token-id check))

(defn p15-s23-c11-mir-node-operands
  [node]
  (gravity.mir/node-operands #'p15-s23-c11-mir-runtime-check-token-id node))

(defn p15-s23-c11-mir-runtime-check-source
  [node]
  (gravity.mir/runtime-check-source node))

(defn p15-s23-c11-mir-runtime-check-fact-id
  [check fact-kind]
  (gravity.mir/runtime-check-fact-id check fact-kind))
