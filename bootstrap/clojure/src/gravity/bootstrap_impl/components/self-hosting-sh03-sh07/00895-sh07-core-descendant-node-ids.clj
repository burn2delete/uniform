

(defn
 sh07-core-descendant-node-ids
 [node-by-id root-node-id]
 (gravity.checked-core/descendant-node-ids
  node-by-id
  root-node-id
  (checked-core-stage-operations)))

(def ^:private sh07-b9-handler-descriptor-keys
  [:clause-ordinal :handler-child-index
   :catch-clause-form-id :catch-clause-syntax-id
   :error-type-form-id :error-type-syntax-id
   :error-type-binding-id
   :catch-binding-form-id :catch-binding-syntax-id
   :catch-binding-id :catch-binding-scope-id])

(defn
 sh07-b9-handler-group-coherent?
 [node-by-id transfers references try-node handlers]
 (gravity.checked-core/handler-group-coherent?
  node-by-id
  transfers
  references
  try-node
  handlers
  (checked-core-stage-operations)))

(defn
 sh07-core-error-handlers-coherent?
 [core]
 (gravity.checked-core/error-handlers-coherent?
  core
  (checked-core-stage-operations)))

(def ^:private sh07-b11-match-clause-keys
  [:clause-ordinal :branch-child-index
   :pattern-kind :pattern-form-id :pattern-syntax-id :pattern-value
   :branch-form-id :branch-syntax-id
   :pattern-binding-id :pattern-binding-scope-id
   :pattern-binding-ids])

(def ^:private sh07-b11-match-pattern-record-keys
  #{:ordinal :clause-ordinal :local-ordinal
    :parent-local-ordinal :root-local-ordinal
    :parent-ordinal :root-ordinal :core-node-id
    :depth :path :pattern-kind
    :pattern-form-id :pattern-syntax-id
    :pattern-value :vector-width
    :pattern-binding-id :pattern-binding-scope-id
    :pattern-binding-use-syntax-ids
    :authenticated-sh06-artifact-id
    :sh06-semantic-projection-id})

(defn
 sh07-b11-guard-source
 [guard]
 (gravity.checked-core/match-guard-source
  guard
  (checked-core-stage-operations)))

(defn
 sh07-b11-guard-source-valid?
 [guard]
 (gravity.checked-core/match-guard-source-valid?
  guard
  (checked-core-stage-operations)))

(defn
 sh07-b11-guard-valid?
 [guard ordinal]
 (gravity.checked-core/match-guard-valid?
  guard
  ordinal
  (checked-core-stage-operations)))

(defn
 sh07-b11-branch-tree
 [node-by-id record]
 (gravity.checked-core/match-branch-tree
  node-by-id
  record
  (checked-core-stage-operations)))

(defn
 sh07-b11-clause
 [record]
 (gravity.checked-core/match-clause
  record
  (checked-core-stage-operations)))

(defn
 sh07-b11-match-pattern-graph-complete?
 [pattern-records]
 (gravity.checked-core/match-pattern-graph-complete?
  pattern-records
  (checked-core-stage-operations)))
