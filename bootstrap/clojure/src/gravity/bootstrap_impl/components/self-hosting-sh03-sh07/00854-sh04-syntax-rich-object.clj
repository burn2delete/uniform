(defn
 sh04-syntax-rich-object
 [syntax serialization-id]
 (gravity.syntax/rich-object
  syntax
  serialization-id
  (syntax-stage-assembly-operations)))
(defn-
 sh04-syntax-reader-expansion-history
 [source-path c2]
 (gravity.syntax/reader-expansion-history
  source-path
  c2
  (syntax-stage-assembly-operations)))
(defn-
 sh04-syntax-reader-expansions-current?
 [syntax-stream c2 boundary invocation history]
 (gravity.syntax/reader-expansions-current?
  syntax-stream
  c2
  boundary
  invocation
  history
  (syntax-stage-assembly-operations)))
(defn
 sh04-syntax-generated-products!
 [source-path
  binding
  base
  semantic-source-id
  reader-binding
  reader-source-revision]
 (gravity.syntax/generated-products!
  source-path
  binding
  base
  semantic-source-id
  reader-binding
  reader-source-revision
  (syntax-stage-assembly-operations)))
(defn
 sh04-syntax-sh02-descriptor
 [source-path binding summary]
 (gravity.syntax/envelope-descriptor
  source-path
  binding
  summary
  (syntax-stage-assembly-operations)))
