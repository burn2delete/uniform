(defn
 sh04-syntax-resolve-template!
 [source-path binding raw reader-binding reader-source-revision]
 (gravity.syntax/resolve-template!
  source-path
  binding
  raw
  reader-binding
  reader-source-revision
  (syntax-stage-runtime-operations)))

(def
 sh04-syntax-sh03-product-binding-keys
 [:adapter-contract
  :adapted-source-unit-id
  :adapted-token-stream-id
  :adapted-form-tree-id
  :adapted-extension-invocation-set-id])

(declare
 sh04-syntax-logical-stem
 sh04-syntax-semantic-source-id
 sh04-syntax-semantic-span
 sh04-syntax-current-sh03-product-binding
 sh04-syntax-descriptor-sh03-product-binding
 sh04-syntax-reader-binding
 sh04-syntax-rich-object
 sh04-syntax-reader-expansion-history
 sh04-syntax-reader-expansions-current?
 sh04-syntax-generated-products!
 sh04-syntax-sh02-descriptor
 sh04-syntax-build-from-fresh-c2!
 sh04-syntax-resolved-result!)
(declare sh04-syntax-source-descriptor)
(declare ^:private syntax-with-source-index-transaction)

(defn-
 syntax-stage-assembly-operations
 []
 {:c2-extension-hash-input #'c2-extension-hash-input,
  :c2-form-hash-input #'c2-form-hash-input,
  :c2-token-hash-input #'c2-token-hash-input,
  :c2-top-level-products #'c2-top-level-products,
  :c3-current-reader-identity-view #'c3-current-reader-identity-view,
  :c3-validate-c2-reader-artifact! #'c3-validate-c2-reader-artifact!,
  :p15-s23-c6c10-canonical-digest #'p15-s23-c6c10-canonical-digest,
  :p15-s23-sh02-sha256-id? #'p15-s23-sh02-sha256-id?,
  :p15-s23-stage2-sh02-descriptor-envelope
  #'p15-s23-stage2-sh02-descriptor-envelope,
  :p15-s23-stage2-sh02-descriptor-envelope-verify!
  #'p15-s23-stage2-sh02-descriptor-envelope-verify!,
  :reader-canonical-hash #'reader-canonical-hash,
  :reader-normalize-relative-path #'reader-normalize-relative-path,
  :sh03-reader-authenticated-envelope-options
  #'sh03-reader-authenticated-envelope-options,
  :sh03-reader-boundary-fail! #'sh03-reader-boundary-fail!,
  :sh03-reader-entrypoint sh03-reader-entrypoint,
  :sh03-reader-expected-entrypoint-semantic-hash
  sh03-reader-expected-entrypoint-semantic-hash,
  :sh03-reader-expected-functions-semantic-hash
  sh03-reader-expected-functions-semantic-hash,
  :sh03-reader-expected-plan-semantic-hash
  sh03-reader-expected-plan-semantic-hash,
  :sh03-reader-expected-source-byte-count
  sh03-reader-expected-source-byte-count,
  :sh03-reader-expected-source-content-hash
  sh03-reader-expected-source-content-hash,
  :sh03-reader-expected-verifier-semantic-hash
  sh03-reader-expected-verifier-semantic-hash,
  :sh03-reader-source-relative-path sh03-reader-source-relative-path,
  :sh03-reader-verifier sh03-reader-verifier,
  :sh04-syntax-adapter-contract sh04-syntax-adapter-contract,
  :sh04-syntax-boundary-fail! #'sh04-syntax-boundary-fail!,
  :sh04-syntax-build-from-fresh-c2! #'sh04-syntax-build-from-fresh-c2!,
  :sh04-syntax-current-binding! #'sh04-syntax-current-binding!,
  :sh04-syntax-descriptor-sh03-product-binding
  #'sh04-syntax-descriptor-sh03-product-binding,
  :sh04-syntax-envelope-stage sh04-syntax-envelope-stage,
  :sh04-syntax-execute! #'sh04-syntax-execute!,
  :sh04-syntax-generated-products! #'sh04-syntax-generated-products!,
  :sh04-syntax-logical-stem #'sh04-syntax-logical-stem,
  :sh04-syntax-public-function-hashes
  sh04-syntax-public-function-hashes,
  :sh04-syntax-public-function-shapes
  sh04-syntax-public-function-shapes,
  :sh04-syntax-reader-binding #'sh04-syntax-reader-binding,
  :sh04-syntax-reader-expansion-history
  #'sh04-syntax-reader-expansion-history,
  :sh04-syntax-registered-literal-registry
  #'sh04-syntax-registered-literal-registry,
  :sh04-syntax-require-carrier! #'sh04-syntax-require-carrier!,
  :sh04-syntax-resolve-stream-request-preimage!
  #'sh04-syntax-resolve-stream-request-preimage!,
  :sh04-syntax-resolve-stream-template!
  #'sh04-syntax-resolve-stream-template!,
  :sh04-syntax-resolve-template! #'sh04-syntax-resolve-template!,
  :sh04-syntax-rich-object #'sh04-syntax-rich-object,
  :sh04-syntax-sealed-artifact-kind sh04-syntax-sealed-artifact-kind,
  :sh04-syntax-semantic-source-id #'sh04-syntax-semantic-source-id,
  :sh04-syntax-sh02-descriptor #'sh04-syntax-sh02-descriptor,
  :sh04-syntax-sh03-product-binding-keys
  sh04-syntax-sh03-product-binding-keys,
  :sh04-syntax-source-descriptor #'sh04-syntax-source-descriptor,
  :sh04-syntax-source-relative-path sh04-syntax-source-relative-path,
  :sh04-syntax-strip-host-metadata #'sh04-syntax-strip-host-metadata,
  :syntax-with-source-index-transaction
  #'syntax-with-source-index-transaction})

(defn
 sh04-syntax-logical-stem
 [source-path source-unit]
 (gravity.syntax/logical-source-stem
  source-path
  source-unit
  (syntax-stage-assembly-operations)))
(defn
 sh04-syntax-semantic-source-id
 [source-path source-unit]
 (gravity.syntax/semantic-source-id
  source-path
  source-unit
  (syntax-stage-assembly-operations)))
(defn
 sh04-syntax-semantic-span
 [span semantic-source-id]
 (gravity.syntax/semantic-span
  span
  semantic-source-id
  (syntax-stage-assembly-operations)))
(defn
 sh04-syntax-current-sh03-product-binding
 [c2-artifact]
 (gravity.syntax/current-reader-product-binding
  c2-artifact
  (syntax-stage-assembly-operations)))
(defn
 sh04-syntax-descriptor-sh03-product-binding
 [descriptor]
 (gravity.syntax/descriptor-reader-product-binding
  descriptor
  (syntax-stage-assembly-operations)))
