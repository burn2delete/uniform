

(defn
 sh07-b11-match-pattern-record-coherent?
 [node-by-id references branch-by-clause record record-by-ordinal]
 (gravity.checked-core/match-pattern-record-coherent?
  node-by-id
  references
  branch-by-clause
  record
  record-by-ordinal
  (checked-core-stage-operations)))

(defn
 sh07-b11-match-group-coherent?
 [node-by-id references match-node records skeleton pattern-records]
 (gravity.checked-core/match-group-coherent?
  node-by-id
  references
  match-node
  records
  skeleton
  pattern-records
  (checked-core-stage-operations)))
