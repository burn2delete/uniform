(defn
 sh07-core-semantic-mutation
 [semantic-projection-id mutation]
 (gravity.checked-core/semantic-mutation
  semantic-projection-id
  mutation
  (checked-core-stage-operations)))

(defn
 sh07-core-semantic-error-transfer
 [semantic-projection-id transfer]
 (gravity.checked-core/semantic-error-transfer
  semantic-projection-id
  transfer
  (checked-core-stage-operations)))

(defn
 sh07-core-semantic-error-handler
 [semantic-projection-id handler]
 (gravity.checked-core/semantic-error-handler
  semantic-projection-id
  handler
  (checked-core-stage-operations)))

(defn
 sh07-core-semantic-node
 [semantic-projection-id node]
 (gravity.checked-core/semantic-node
  semantic-projection-id
  node
  (checked-core-stage-operations)))

(defn
 sh07-core-semantic-identity-preimage
 [preimage]
 (gravity.checked-core/semantic-identity-preimage
  preimage
  (checked-core-stage-operations)))

(defn
 sh07-core-resolve-digest-preimage!
 [source-path purpose preimage resolved-digests]
 (gravity.checked-core/resolve-digest-preimage!
  source-path
  purpose
  preimage
  resolved-digests
  (checked-core-stage-operations)))

(defn
 sh07-core-digest-requests
 [source-path digest-requests]
 (gravity.checked-core/digest-requests
  source-path
  digest-requests
  (checked-core-stage-operations)))

(defn
 sh07-core-resolve-result
 [source-path template digest-requests resolved-digests]
 (gravity.checked-core/resolve-result
  source-path
  template
  digest-requests
  resolved-digests
  (checked-core-stage-operations)))
