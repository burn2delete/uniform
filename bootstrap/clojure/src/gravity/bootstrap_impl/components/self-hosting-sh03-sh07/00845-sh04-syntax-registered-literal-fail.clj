(declare
 sh04-syntax-registered-literal-fail!
 sh04-syntax-registered-literal-registry
 sh04-syntax-project-registered-literal-values
 sh04-syntax-registered-literal-projection-authentic?)
(declare
 ^:private c3-current-reader-identity-view
 ^:private c3-current-reader-revision-authentic?
 ^:private sh03-c3-precomputed-c2-verify!)

(defn-
 syntax-stage-reader-view-operations
 []
 {:c3-syntax-fail! #'c3-syntax-fail!,
  :p15-s23-c6c10-canonical-digest #'p15-s23-c6c10-canonical-digest,
  :p15-s23-stage2-sh02-descriptor-envelope-verify!
  #'p15-s23-stage2-sh02-descriptor-envelope-verify!,
  :p15-s23-trusted-carrier-validation
  #'p15-s23-trusted-carrier-validation,
  :sh03-c3-precomputed-c2-verify! #'sh03-c3-precomputed-c2-verify!,
  :sh03-reader-expected-source-content-hash
  sh03-reader-expected-source-content-hash,
  :sh03-reader-internal-product-authority
  sh03-reader-internal-product-authority,
  :sh03-reader-verification-invocation!
  #'sh03-reader-verification-invocation!,
  :sh03-reader-verifier sh03-reader-verifier,
  :sh04-syntax-current-sh03-product-binding
  #'sh04-syntax-current-sh03-product-binding,
  :sh04-syntax-project-registered-literal-values
  #'sh04-syntax-project-registered-literal-values,
  :sh04-syntax-reader-binding #'sh04-syntax-reader-binding,
  :sh04-syntax-registered-literal-fail!
  #'sh04-syntax-registered-literal-fail!,
  :sh04-syntax-registered-literal-registry
  #'sh04-syntax-registered-literal-registry,
  :sh04-syntax-semantic-source-id #'sh04-syntax-semantic-source-id,
  :sh04-syntax-strip-host-metadata #'sh04-syntax-strip-host-metadata,
  :source-span #'source-span})

(defn
 sh04-syntax-registered-literal-fail!
 [source-path form-record missing-fields facts]
 (gravity.syntax/registered-literal-fail!
  source-path
  form-record
  missing-fields
  facts
  (syntax-stage-reader-view-operations)))
(defn
 sh04-syntax-registered-literal-registry
 [source-path c2-artifact]
 (gravity.syntax/registered-literal-registry
  source-path
  c2-artifact
  (syntax-stage-reader-view-operations)))
(defn
 sh04-syntax-project-registered-literal-values
 [source-path registry value]
 (gravity.syntax/project-registered-literal-values
  source-path
  registry
  value
  (syntax-stage-reader-view-operations)))
