(defn
 sh07-core-run-structural-request-for-test
 "Test-only structural runner. It does not authenticate SH-06 membership;\n  callers testing the executable boundary must use\n  sh07-core-run-request-for-test with the verified SH-06 artifact."
 [authenticated-request]
 (gravity.checked-core/run-structural-request-for-test
  authenticated-request
  (checked-core-stage-operations)))

(defn
 sh07-core-run-request-for-test
 ([resolution-artifact authenticated-request]
  (gravity.checked-core/run-request-for-test
   resolution-artifact
   authenticated-request
   (checked-core-stage-operations)))
 ([resolution-artifact authenticated-request invocation]
  (gravity.checked-core/run-request-for-test
   resolution-artifact
   authenticated-request
   invocation
   (checked-core-stage-operations))))

(defn
 sh07-core-projection-diagnostic!
 [resolution-artifact reason]
 (gravity.checked-core/projection-diagnostic!
  resolution-artifact
  reason
  (checked-core-stage-operations)))

(declare sh07-core-exact-comparison-value)

(defn
 sh07-core-error-transfers-coherent?
 [core]
 (gravity.checked-core/error-transfers-coherent?
  core
  (checked-core-stage-operations)))
