(defn
 sh06-resolution-require-carrier!
 [source-path carrier value]
 (gravity.resolution/require-carrier!
  source-path
  carrier
  value
  (resolution-stage-operations)))

(defn
 sh06-resolution-digest-reference
 [value]
 (gravity.resolution/digest-reference value))

(defn
 sh06-resolution-resolve-declared-id!
 [source-path value resolved-digests slot]
 (gravity.resolution/resolve-declared-id!
  source-path
  value
  resolved-digests
  slot
  (resolution-stage-operations)))

(defn
 sh06-resolution-resolve-record-binding-id!
 [source-path record resolved-digests slot]
 (gravity.resolution/resolve-record-binding-id!
  source-path
  record
  resolved-digests
  slot
  (resolution-stage-operations)))

(defn
 sh06-resolution-resolve-analysis!
 ([source-path raw-analysis resolved-digests]
  (gravity.resolution/resolve-analysis!
   source-path
   raw-analysis
   resolved-digests
   (resolution-stage-operations)))
 ([source-path
   raw-analysis
   resolved-digests
   allow-pending-artifact-id?]
  (gravity.resolution/resolve-analysis!
   source-path
   raw-analysis
   resolved-digests
   allow-pending-artifact-id?
   (resolution-stage-operations))))

(defn
 sh06-resolution-resolve-digest-requests!
 [source-path raw-analysis digest-requests]
 (gravity.resolution/resolve-digest-requests!
  source-path
  raw-analysis
  digest-requests
  (resolution-stage-operations)))

(defn
 sh06-resolution-raise-rejection!
 [source-path template]
 (gravity.resolution/raise-rejection!
  source-path
  template
  (resolution-stage-operations)))

(defn
 sh06-resolution-run-request!
 [source-path binding request]
 (gravity.resolution/run-request!
  source-path
  binding
  request
  (resolution-stage-operations)))

(defn
 sh06-resolution-order-compatibility
 [order]
 (gravity.resolution/order-compatibility order))
