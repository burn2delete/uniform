

(defn-
 sh07-core-lineage-with-semantic-trace
 [resolution-artifact semantic-trace]
 (#'gravity.checked-core/lineage-with-semantic-trace
  resolution-artifact
  semantic-trace
  (checked-core-stage-operations)))

(defn
 sh07-core-lineage
 [resolution-artifact]
 (gravity.checked-core/lineage
  resolution-artifact
  (checked-core-stage-operations)))

(defn
 sh07-core-projection-binding-input
 [request]
 (gravity.checked-core/projection-binding-input
  request
  (checked-core-stage-operations)))

(defn
 sh07-b13-alias-name
 [resolution]
 (gravity.checked-core/alias-name
  resolution
  (checked-core-stage-operations)))

(defn
 sh07-b13-fragment-record
 [lineage module ordinal tree bindings resolutions aliases]
 (gravity.checked-core/fragment-record
  lineage
  module
  ordinal
  tree
  bindings
  resolutions
  aliases
  (checked-core-stage-operations)))

(defn
 sh07-b13-fragment-manifest
 [lineage module trees bindings resolutions aliases]
 (gravity.checked-core/fragment-manifest
  lineage
  module
  trees
  bindings
  resolutions
  aliases
  (checked-core-stage-operations)))

(defn
 sh07-b13-binding-order
 [module trees bindings]
 (gravity.checked-core/binding-order
  module
  trees
  bindings
  (checked-core-stage-operations)))

(defn
 sh07-b13-fragment-coverage
 [module forms bindings resolutions fragment-manifest]
 (gravity.checked-core/fragment-coverage
  module
  forms
  bindings
  resolutions
  fragment-manifest
  (checked-core-stage-operations)))