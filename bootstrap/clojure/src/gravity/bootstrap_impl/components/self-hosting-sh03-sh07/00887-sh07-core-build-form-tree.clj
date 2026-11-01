

(defn-
 sh07-core-source-occurrence-context
 [resolution-artifact syntaxes]
 (#'gravity.checked-core/source-occurrence-context
  resolution-artifact
  syntaxes
  (checked-core-stage-operations)))

(defn
 sh07-core-build-form-tree
 [source-path source-revision-id decimal-evidence syntax trace]
 (gravity.checked-core/build-form-tree
  source-path
  source-revision-id
  decimal-evidence
  syntax
  trace
  (checked-core-stage-operations)))

(defn
 sh07-core-source-map-order
 [resolution-artifact syntaxes trees]
 (gravity.checked-core/source-map-order
  resolution-artifact
  syntaxes
  trees
  (checked-core-stage-operations)))

(defn
 sh07-core-parameter-binding-paths
 [parameters parameter-path]
 (gravity.checked-core/parameter-binding-paths
  parameters
  parameter-path
  (checked-core-stage-operations)))
