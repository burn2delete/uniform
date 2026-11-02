

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
   (gravity.mir/validate-carrier!
    {:trusted-carrier-validation #'p15-s23-trusted-carrier-validation
     :valid-unicode-string? #'p15-s23-c6c10-valid-unicode-string?
     :fail! #'p15-s23-c11-mir-fail!
     :contain-exception! #'p15-s23-c11-mir-contain-exception!}
    source-path definition value))
  ;; Existing consumers can migrate independently; these arguments never admit
  ;; or reject a carrier and no accounting measurements are returned.
  ([source-path definition value _maximum-nodes _maximum-depth]
   (p15-s23-c11-mir-bounded-value! source-path definition value)))

(defn p15-s23-c11-mir-metadata-free?
  [value]
  (gravity.mir/metadata-free? value))

(defn p15-s23-c11-mir-require-strict-structure!
  [source-path expected actual missing-fact]
  (gravity.mir/require-strict-structure!
   #'p15-s23-c6c10-strict-first-mismatch #'p15-s23-c11-mir-fail!
   source-path expected actual missing-fact))

(defn p15-s23-c11-mir-require!
  [condition id source-path subject missing-fact]
  (gravity.mir/require! #'p15-s23-c11-mir-fail! condition id source-path subject missing-fact))

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
