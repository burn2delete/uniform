

(defn- c2-artifact-identity-ops
  []
  {:sha256-hex sha256-hex
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
   :c2-reader-artifact-id c2-reader-artifact-id
   :max-reader-form-graph-depth max-reader-form-graph-depth})