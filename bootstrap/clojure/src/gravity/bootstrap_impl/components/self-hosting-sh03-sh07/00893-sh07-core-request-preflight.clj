(defn
 sh07-core-request-preflight!
 [request]
 (gravity.checked-core/request-preflight!
  request
  (checked-core-stage-operations)))
