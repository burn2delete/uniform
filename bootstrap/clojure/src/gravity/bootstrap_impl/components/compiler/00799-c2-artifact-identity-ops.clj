(declare ^:private c2-artifact-identity-canonical-defaults)

(defn- c2-artifact-identity-ops
  []
  (cond-> {:sha256-hex sha256-hex
   :c2-form-graph-metrics c2-form-graph-metrics
   :c2-reader-fail! c2-reader-fail!
   :source-span source-span
   :reader-canonical-value reader-canonical-value
   :reader-canonical-hash reader-canonical-hash
   :c2-semantic-form-hash-input c2-semantic-form-hash-input
   :c2-path-neutral-span c2-path-neutral-span
   :c2-token-hash-input c2-token-hash-input
   :c2-form-hash-input c2-form-hash-input
   :c2-syntax-seed-hash-input c2-syntax-seed-hash-input
   :c2-extension-hash-input c2-extension-hash-input
   :c2-diagnostic-hash-input c2-diagnostic-hash-input
   :c2-incremental-hashes c2-incremental-hashes
   :c2-reader-product-integrity-record c2-reader-product-integrity-record
   :c2-reader-artifact-id c2-reader-artifact-id}
    ;; Default recursion needs no bootstrap round trip at every node. Retain
    ;; the compatibility operation whenever either wrapper is interposed.
    (and (identical? reader-canonical-value (first c2-artifact-identity-canonical-defaults))
         (identical? c2-artifact-identity/reader-canonical-value
                     (second c2-artifact-identity-canonical-defaults)))
    (dissoc :reader-canonical-value)))
