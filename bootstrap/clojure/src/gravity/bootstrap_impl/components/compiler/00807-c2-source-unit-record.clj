

(defn c2-source-unit-record
  ([source-path source-text reader-options]
   (c2-source-unit-record source-path source-text reader-options
                          (reader-project-context-for-source source-path)))
  ([source-path source-text reader-options project-context]
   (c2-source-identity-call
    :c2-source-unit-record c2-source-identity/c2-source-unit-record
    source-path source-text reader-options project-context)))

(defn c2-token-record
  [token source-unit]
  (c2-source-identity-call
   :c2-token-record c2-source-identity/c2-token-record token source-unit))

(defn c2-form-record
  [record source-unit]
  (c2-source-identity-call
   :c2-form-record c2-source-identity/c2-form-record record source-unit))

(defn c2-literal-records
  [form-tree]
  (c2-source-identity-call
   :c2-literal-records c2-source-identity/c2-literal-records form-tree))

(defn c2-trivia-records
  [token-stream]
  (c2-source-identity-call
   :c2-trivia-records c2-source-identity/c2-trivia-records token-stream))

(defn c2-semantic-form-hash-input
  [form-tree]
  (c2-artifact-identity-call
   :c2-semantic-form-hash-input
   c2-artifact-identity/c2-semantic-form-hash-input form-tree))

(defn c2-path-neutral-span
  [span]
  (c2-artifact-identity-call
   :c2-path-neutral-span
   c2-artifact-identity/c2-path-neutral-span span))

(defn c2-token-hash-input
  [token-stream]
  (c2-artifact-identity-call
   :c2-token-hash-input
   c2-artifact-identity/c2-token-hash-input token-stream))

(defn c2-form-hash-input
  [form-tree]
  (c2-artifact-identity-call
   :c2-form-hash-input
   c2-artifact-identity/c2-form-hash-input form-tree))

(defn c2-syntax-seed-hash-input
  [syntax-seeds]
  (c2-artifact-identity-call
   :c2-syntax-seed-hash-input
   c2-artifact-identity/c2-syntax-seed-hash-input syntax-seeds))

(defn c2-extension-hash-input
  [extension-invocations]
  (c2-artifact-identity-call
   :c2-extension-hash-input
   c2-artifact-identity/c2-extension-hash-input extension-invocations))

(defn c2-diagnostic-hash-input
  [diagnostics]
  (c2-artifact-identity-call
   :c2-diagnostic-hash-input
   c2-artifact-identity/c2-diagnostic-hash-input diagnostics))

(defn c2-incremental-hashes
  [source-unit token-stream form-tree syntax-seeds extension-invocations
  diagnostics]
  (c2-artifact-identity-call
   :c2-incremental-hashes
   c2-artifact-identity/c2-incremental-hashes
   source-unit token-stream form-tree syntax-seeds extension-invocations
   diagnostics))

(defn c2-reader-product-integrity-record
  [source-unit top-level-form-ids incremental-hashes literal-records
   deferred-literal-records]
  (c2-artifact-identity-call
   :c2-reader-product-integrity-record
   c2-artifact-identity/c2-reader-product-integrity-record
   source-unit top-level-form-ids incremental-hashes literal-records
   deferred-literal-records))

(defn c2-reader-artifact-id
  [artifact]
  (c2-artifact-identity-call
   :c2-reader-artifact-id
   c2-artifact-identity/c2-reader-artifact-id artifact))