

(defn
 sh07-core-match-products-coherent?
 [core]
 (gravity.checked-core/match-products-coherent?
  core
  (checked-core-stage-operations)))

(defn
 sh07-core-verification-checks
 [artifact expected upstream-verification]
 (gravity.checked-core/verification-checks
  artifact
  expected
  upstream-verification
  (checked-core-stage-operations)))

(defn
 sh07-core-proof-from-checks
 [checks]
 (gravity.checked-core/proof-from-checks
  checks
  (checked-core-stage-operations)))

(defn
 sh07-core-exact-comparison-value
 [value]
 (gravity.checked-core/exact-comparison-value
  value
  (checked-core-stage-operations)))