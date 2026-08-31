

(defn- c3-syntax-construction-call
  [operation & args]
  (if *c3-syntax-construction-leaf-call?*
    (apply operation args)
    (binding [*c3-syntax-construction-leaf-call?* true]
      (c3-syntax-construction/with-operations
       (c3-syntax-construction-ops)
       #(apply operation args)))))

(defn c3-path-neutral-origin
  [origin]
  (c3-syntax-construction-call
   c3-syntax-construction/c3-path-neutral-origin origin))

(defn c3-identity-input
  [seed origin namespace-context hygiene-context source-form-kind]
  (c3-syntax-construction-call
   c3-syntax-construction/c3-identity-input
   seed origin namespace-context hygiene-context source-form-kind))

(defn c3-stable-syntax-id
  [identity-input]
  (c3-syntax-construction-call
   c3-syntax-construction/c3-stable-syntax-id identity-input))

(defn c3-syntax-object
  [seed form-record token-record source-unit c2-artifact integrity-report]
  (c3-syntax-construction-call
   c3-syntax-construction/c3-syntax-object
   seed form-record token-record source-unit c2-artifact integrity-report))

(defn c3-generated-syntax-object
  [base-object]
  (c3-syntax-construction-call
   c3-syntax-construction/c3-generated-syntax-object base-object))

(def c3-required-form-kinds c3-syntax-evidence/c3-required-form-kinds)

(defn c3-syntax-schema
  []
  (c3-syntax-evidence/c3-syntax-schema c3-required-form-kinds))

(defn c3-hygiene-context-map
  [syntax-stream]
  (c3-syntax-evidence/c3-hygiene-context-map syntax-stream))

(defn c3-origin-chain-graph
  [syntax-stream]
  (c3-syntax-evidence/c3-origin-chain-graph syntax-stream))

(defn c3-metadata-ledger
  [syntax-stream]
  (c3-syntax-evidence/c3-metadata-ledger syntax-stream))

(defn c3-fact-ledger
  [syntax-stream]
  (c3-syntax-evidence/c3-fact-ledger syntax-stream))

(defn c3-generated-syntax-report
  [syntax-stream]
  (c3-syntax-evidence/c3-generated-syntax-report syntax-stream))

(defn c3-syntax-serialization-fixture
  [syntax-stream]
  (c3-syntax-evidence/c3-syntax-serialization-fixture
   syntax-stream reader-canonical-hash))

(defn c3-resolvable-span?
  [span]
  (c3-syntax-evidence/c3-resolvable-span? span))