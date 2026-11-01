

(defn
 sh06-resolution-dependency-record
 [source-path module dependency overrides ordinal]
 (gravity.resolution/dependency-record
  source-path
  module
  dependency
  overrides
  ordinal
  (resolution-stage-operations)))

(defn
 sh06-resolution-import-records
 [source-path module overrides]
 (gravity.resolution/import-records
  source-path
  module
  overrides
  (resolution-stage-operations)))

(defn
 sh06-resolution-import-binding-records
 [module imports overrides]
 (gravity.resolution/import-binding-records
  module
  imports
  overrides
  (resolution-stage-operations)))

(defn
 sh06-resolution-explicit-candidate-records
 [module sh05-artifact overrides]
 (gravity.resolution/explicit-candidate-records
  module
  sh05-artifact
  overrides
  (resolution-stage-operations)))

(defn
 sh06-match-pattern-binding-paths
 [pattern path]
 (gravity.resolution/match-pattern-binding-paths
  pattern
  path
  (resolution-stage-operations)))

(defn
 sh06-fixed-vector-pattern?
 [pattern]
 (gravity.resolution/fixed-vector-pattern?
  pattern
  (resolution-stage-operations)))

(defn
 sh06-unique-match-binding-paths
 [pattern path]
 (gravity.resolution/unique-match-binding-paths
  pattern
  path
  (resolution-stage-operations)))

(defn
 sh06-let-loop-binding-paths
 [pattern path]
 (gravity.resolution/let-loop-binding-paths pattern path))
