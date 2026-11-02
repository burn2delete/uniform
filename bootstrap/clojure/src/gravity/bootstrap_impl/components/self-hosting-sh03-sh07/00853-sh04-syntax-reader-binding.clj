(defn
 sh04-syntax-reader-binding
 [source-path c2-artifact semantic-source-id]
 (gravity.syntax/reader-semantic-binding
  source-path
  c2-artifact
  semantic-source-id
  (syntax-stage-assembly-operations)))

^{:line 105, :column 4}
(declare
 sh04-syntax-empty-hygiene
 sh04-syntax-strip-host-metadata
 sh04-syntax-descendant-form-ids
 sh04-syntax-source-descriptor)

(declare ^{:private true} sh04-syntax-form-index)

(declare
 ^{:private true, :dynamic true} *sh04-syntax-form-index-transaction*)

(def
 ^{:private true} syntax-stage-strip-defaults
 {:sh04-syntax-strip-host-metadata #'sh04-syntax-strip-host-metadata})

(def
 ^{:private true} syntax-stage-descriptor-defaults
 {:sh04-syntax-project-registered-literal-values
  #'sh04-syntax-project-registered-literal-values,
  :c3-source-facts #'c3-source-facts,
  :sh04-syntax-form-index #'sh04-syntax-form-index,
  :sh04-syntax-empty-hygiene #'sh04-syntax-empty-hygiene,
  :c3-source-form-kind #'c3-source-form-kind,
  :c3-tagged-literal-descriptor #'c3-tagged-literal-descriptor,
  :sh04-syntax-descendant-form-ids #'sh04-syntax-descendant-form-ids,
  :sh04-syntax-semantic-span #'sh04-syntax-semantic-span,
  :c3-lossless-literal-descriptor #'c3-lossless-literal-descriptor,
  :sh04-syntax-strip-host-metadata #'sh04-syntax-strip-host-metadata})

(defn-
 syntax-stage-descriptor-operations
 []
 syntax-stage-descriptor-defaults)

(defn
 sh04-syntax-empty-hygiene
 [namespace]
 (gravity.syntax/empty-hygiene namespace))

(defn
 sh04-syntax-strip-host-metadata
 [value]
 (gravity.syntax/strip-host-metadata value syntax-stage-strip-defaults))

(defn
 sh04-syntax-descendant-form-ids
 [forms-by-id root-form-id]
 (gravity.syntax/descendant-form-ids forms-by-id root-form-id))

(defn
 sh04-syntax-source-descriptor
 [seed
  form-record
  c2-artifact
  integrity-report
  semantic-source-id
  reader-binding
  reader-source-revision
  registered-literals]
 (gravity.syntax/source-descriptor
  seed
  form-record
  c2-artifact
  integrity-report
  semantic-source-id
  reader-binding
  reader-source-revision
  registered-literals
  ^{:line 70, :column 50}
  (syntax-stage-descriptor-operations)
  syntax-stage-descriptor-defaults
  #'*sh04-syntax-form-index-transaction*))
