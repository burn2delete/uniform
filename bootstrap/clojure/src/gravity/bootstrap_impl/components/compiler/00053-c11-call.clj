
(defn- c11-call [operation & args]
  (if *c11-leaf-call?*
    (apply operation args)
    (binding [*c11-leaf-call?* true]
      (c11/with-operations (c11-mir-ops)
        #(apply operation args)))))

(defn c11-mir-source-overrides
  [module]
  (c11-call c11/c11-mir-source-overrides module))

(defn c11-mir-message
  [id]
  (c11-call c11/c11-mir-message id))

(defn c11-mir-fail!
  [id source-path subject extra]
  (c11-call c11/c11-mir-fail! id source-path subject extra))

(defn c11-mir-validate-overrides!
  [source-path module overrides]
  (c11-call c11/c11-mir-validate-overrides! source-path module overrides))

(defn c11-family-opcode
  [family]
  (c11-call c11/c11-family-opcode family))

(defn c11-family-effects
  [family]
  (c11-call c11/c11-family-effects family))

(defn c11-mir-operation
  [module span outcome-by-index index family]
  (c11-call c11/c11-mir-operation module span outcome-by-index index family))

(defn c11-mir-module-record
  [module c10-artifact operations]
  (c11-call c11/c11-mir-module-record module c10-artifact operations))

(defn c11-data-flow-graph
  [operations]
  (c11-call c11/c11-data-flow-graph operations))

(defn c11-domain-anchor-table
  []
  (c11-call c11/c11-domain-anchor-table))

(defn c11-present?
  [value]
  (c11-call c11/c11-present? value))

(defn c11-mir-diagnostics
  [source-path]
  (c11-call c11/c11-mir-diagnostics source-path))

(defn c11-mir-verifier-report
  [module operations data-flow domain-anchors diagnostics]
  (c11-call c11/c11-mir-verifier-report module operations data-flow domain-anchors diagnostics))

(defn c11-mir-capability-proof
  [artifact]
  (c11-call c11/c11-mir-capability-proof artifact))

(defn c11-mir-validate!
  [source-path artifact]
  (c11-call c11/c11-mir-validate! source-path artifact))

(defn compiler-c11-mir-source-artifact
  [source-path source-text]
  (c11-call c11/compiler-c11-mir-source-artifact source-path source-text))

(defn compiler-c11-mir-file-artifact
  [path]
  (c11-call c11/compiler-c11-mir-file-artifact path))