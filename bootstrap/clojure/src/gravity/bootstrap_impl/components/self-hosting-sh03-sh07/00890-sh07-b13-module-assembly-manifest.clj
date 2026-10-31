(defn
 sh07-b13-module-assembly-manifest
 [lineage fragment-manifest]
 (gravity.checked-core/module-assembly-manifest
  lineage
  fragment-manifest
  (checked-core-stage-operations)))

(defn-
 sh07-core-request-from-verified-upstream
 [resolution-artifact upstream-verification]
 (#'gravity.checked-core/request-from-verified-upstream
  resolution-artifact
  upstream-verification
  (checked-core-stage-operations)))

(defn
 sh07-core-authenticated-request
 ([resolution-artifact]
  (gravity.checked-core/authenticated-request
   resolution-artifact
   (checked-core-stage-operations)))
 ([resolution-artifact context]
  (gravity.checked-core/authenticated-request
   resolution-artifact
   context
   (checked-core-stage-operations)))
 ([resolution-artifact context invocation]
  (gravity.checked-core/authenticated-request
   resolution-artifact
   context
   invocation
   (checked-core-stage-operations))))
(defn
 sh07-core-digest-reference-ordinal
 [value]
 (gravity.checked-core/digest-reference-ordinal
  value
  (checked-core-stage-operations)))

(defn
 sh07-core-resolve-reference!
 [source-path value resolved-digests]
 (gravity.checked-core/resolve-reference!
  source-path
  value
  resolved-digests
  (checked-core-stage-operations)))

(defn
 sh07-core-resolve-reference-vector!
 [source-path values resolved-digests]
 (gravity.checked-core/resolve-reference-vector!
  source-path
  values
  resolved-digests
  (checked-core-stage-operations)))
(defn
 sh07-core-semantic-projection-id
 [value]
 (gravity.checked-core/semantic-projection-id
  value
  (checked-core-stage-operations)))

(defn
 sh07-core-semantic-binding
 [semantic-projection-id binding]
 (gravity.checked-core/semantic-binding
  semantic-projection-id
  binding
  (checked-core-stage-operations)))

(defn
 sh07-core-semantic-resolution
 [resolution]
 (gravity.checked-core/semantic-resolution
  resolution
  (checked-core-stage-operations)))

(defn
 sh07-core-semantic-var-reference
 [semantic-projection-id reference]
 (gravity.checked-core/semantic-var-reference
  semantic-projection-id
  reference
  (checked-core-stage-operations)))
