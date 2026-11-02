

(def ^:private p15-s23-runtime-rule-operations
  {:declared-builtin-exact-arities #'p15-s23-sh07-declared-builtin-exact-arities
   :declared-builtin-contracts #'p15-s23-sh07-declared-builtin-contracts
   :difference #'set/difference
   :required-instructions #'p15-s23-stage2-runtime-executor-required-instructions
   :builtin-functions #'stage0-builtin-functions
   :sort-values #'p15-s23-stage2-sort-values})

(defn p15-s23-stage2-runtime-executor-rule-record
  [runtime]
  (gravity.runtime/executor-rule-record runtime p15-s23-runtime-rule-operations))

(defn p15-s23-stage2-runtime-fail-call-arity!
  [id plan callee args expected]
  (gravity.runtime/fail-call-arity! #'fail! id plan callee args expected))

(defn p15-s23-stage2-runtime-assert-min-arity!
  [plan callee args n]
  (gravity.runtime/assert-min-arity! #'p15-s23-stage2-runtime-fail-call-arity! plan callee args n))

(defn p15-s23-stage2-runtime-assert-exact-arity!
  [plan callee args n]
  (gravity.runtime/assert-exact-arity! #'p15-s23-stage2-runtime-fail-call-arity! plan callee args n))

(defn p15-s23-stage2-runtime-assert-between-arity!
  [plan callee args min-n max-n]
  (gravity.runtime/assert-between-arity! #'p15-s23-stage2-runtime-fail-call-arity! plan callee args min-n max-n))

(defn p15-s23-stage2-runtime-assert-even-arity!
  [plan callee args]
  (gravity.runtime/assert-even-arity! #'p15-s23-stage2-runtime-fail-call-arity! plan callee args))

(declare p15-s23-sh07-b51-runtime-augmented-plan-authorized?
         p15-s23-sh02-source-byte-count
         sh03-reader-expected-source-byte-count
         sh03-reader-expected-source-content-hash
         sh03-reader-expected-plan-semantic-hash
         p15-s23-sh02-expected-source-content-hash
         p15-s23-sh02-expected-plan-id
         sh04-syntax-expected-source-byte-count
         sh04-syntax-expected-source-content-hash
         sh04-syntax-expected-plan-id
         sh05-macro-expected-source-content-hash
         sh05-macro-expected-source-byte-count
         sh05-macro-expected-plan-id
         sh06-resolution-expected-source-content-hash
         sh06-resolution-expected-source-byte-count
         sh06-resolution-expected-plan-id)

(def p15-s23-stage2-pinned-augmented-plan-contract-keys
  #{:stage :module :source-byte-count :source-content-hash :plan-id
    :compiler-source-field :compiler-semantic-field
    :emitter-source-content-hash :emitter-semantic-hash})

(def ^:private p15-s23-stage2-pinned-augmented-plan-contract-table
  (delay
   [{:stage :sh02
    :module 'gravity.compiler.authenticated-envelope
    :source-byte-count p15-s23-sh02-source-byte-count
    :source-content-hash p15-s23-sh02-expected-source-content-hash
    :plan-id p15-s23-sh02-expected-plan-id
    :compiler-source-field :source-content-hash
    :compiler-semantic-field :semantic-hash
    :emitter-source-content-hash
    p15-s23-stage2-compiler-artifact-expected-source-content-hash
    :emitter-semantic-hash
    p15-s23-stage2-compiler-artifact-expected-semantic-hash}
   {:stage :sh03
    :module 'gravity.bootstrap.reader
    :source-byte-count sh03-reader-expected-source-byte-count
    :source-content-hash sh03-reader-expected-source-content-hash
    :plan-id sh03-reader-expected-plan-semantic-hash
    :compiler-source-field :emitter-source-content-hash
    :compiler-semantic-field :emitter-semantic-hash
    :emitter-source-content-hash
    p15-s23-stage2-compiler-artifact-expected-source-content-hash
    :emitter-semantic-hash
    p15-s23-stage2-compiler-artifact-expected-semantic-hash}
   {:stage :sh04
    :module 'gravity.bootstrap.syntax
    :source-byte-count sh04-syntax-expected-source-byte-count
    :source-content-hash sh04-syntax-expected-source-content-hash
    :plan-id sh04-syntax-expected-plan-id
    :compiler-source-field :source-content-hash
    :compiler-semantic-field :semantic-hash
    :emitter-source-content-hash
    p15-s23-stage2-compiler-artifact-expected-source-content-hash
    :emitter-semantic-hash
    p15-s23-stage2-compiler-artifact-expected-semantic-hash}
   {:stage :sh05
    :module 'gravity.macro
    :source-byte-count sh05-macro-expected-source-byte-count
    :source-content-hash sh05-macro-expected-source-content-hash
    :plan-id sh05-macro-expected-plan-id
    :compiler-source-field :source-content-hash
    :compiler-semantic-field :semantic-hash
    :emitter-source-content-hash
    p15-s23-stage2-compiler-artifact-expected-source-content-hash
    :emitter-semantic-hash
    p15-s23-stage2-compiler-artifact-expected-semantic-hash}
   {:stage :sh06
    :module 'gravity.resolution
    :source-byte-count sh06-resolution-expected-source-byte-count
    :source-content-hash sh06-resolution-expected-source-content-hash
    :plan-id sh06-resolution-expected-plan-id
    :compiler-source-field :source-content-hash
    :compiler-semantic-field :semantic-hash
    :emitter-source-content-hash
    p15-s23-stage2-compiler-artifact-expected-source-content-hash
    :emitter-semantic-hash
    p15-s23-stage2-compiler-artifact-expected-semantic-hash}]))

(defn p15-s23-stage2-pinned-augmented-plan-contracts
  []
  @p15-s23-stage2-pinned-augmented-plan-contract-table)

(defn p15-s23-stage2-pinned-augmented-plan-contract?
  [contract]
  (and (= p15-s23-stage2-pinned-augmented-plan-contract-keys
          (set (keys contract)))
       (keyword? (:stage contract))
       (symbol? (:module contract))
       (integer? (:source-byte-count contract))
       (pos? (:source-byte-count contract))
       (string? (:source-content-hash contract))
       (re-matches #"sha256:[0-9a-f]{64}" (:source-content-hash contract))
       (string? (:plan-id contract))
       (re-matches #"sha256:[0-9a-f]{64}" (:plan-id contract))
       (contains? #{:source-content-hash :emitter-source-content-hash}
                  (:compiler-source-field contract))
       (contains? #{:semantic-hash :emitter-semantic-hash}
                  (:compiler-semantic-field contract))
       (string? (:emitter-source-content-hash contract))
       (re-matches #"sha256:[0-9a-f]{64}"
                   (:emitter-source-content-hash contract))
       (string? (:emitter-semantic-hash contract))
       (re-matches #"sha256:[0-9a-f]{64}"
                   (:emitter-semantic-hash contract))))

(def ^:private p15-s23-stage2-pinned-augmented-plan-contract-table-valid?
  (delay
   (let [contracts (p15-s23-stage2-pinned-augmented-plan-contracts)]
     (and (= 5 (count contracts))
          (= 5 (count (set (map :stage contracts))))
          (= 5 (count (set (map :module contracts))))
          (every? p15-s23-stage2-pinned-augmented-plan-contract?
                  contracts)))))

(def ^:private p15-s23-stage2-pinned-augmented-plan-contract-index
  (delay
   (into {}
         (map (fn [contract]
                [[(:module contract)
                  (:source-content-hash contract)
                  (:plan-id contract)]
                 contract]))
         (p15-s23-stage2-pinned-augmented-plan-contracts))))

(defn p15-s23-stage2-pinned-augmented-plan-authorized?
  [plan]
  (let [contract
        (get @p15-s23-stage2-pinned-augmented-plan-contract-index
             [(get-in plan [:module :module])
              (get-in plan [:source :sha256])
              (:plan-id plan)])]
    (boolean
     (and @p15-s23-stage2-pinned-augmented-plan-contract-table-valid?
          contract
          (= (:source-byte-count contract)
             (get-in plan [:source :byte-count]))
          (= (:emitter-source-content-hash contract)
             (get-in plan [:compiler (:compiler-source-field contract)]))
          (= (:emitter-semantic-hash contract)
             (get-in plan [:compiler
                           (:compiler-semantic-field contract)]))))))
