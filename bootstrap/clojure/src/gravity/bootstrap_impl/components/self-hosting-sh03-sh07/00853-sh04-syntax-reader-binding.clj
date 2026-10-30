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
(declare ^:private sh04-syntax-form-index)
(declare ^:dynamic ^:private *sh04-syntax-form-index-transaction*)
(declare ^:private syntax-with-source-descriptor-context)

(def ^:private syntax-stage-strip-defaults
  {:sh04-syntax-strip-host-metadata #'sh04-syntax-strip-host-metadata})

(def ^:private syntax-stage-descriptor-defaults
{:c3-lossless-literal-descriptor #'c3-lossless-literal-descriptor,
 :c3-source-facts #'c3-source-facts,
 :c3-source-form-kind #'c3-source-form-kind,
 :c3-tagged-literal-descriptor #'c3-tagged-literal-descriptor,
 :descriptor-context #'syntax-with-source-descriptor-context,
 :sh04-syntax-descendant-form-ids #'sh04-syntax-descendant-form-ids,
 :sh04-syntax-empty-hygiene #'sh04-syntax-empty-hygiene,
 :sh04-syntax-form-index #'sh04-syntax-form-index,
 :sh04-syntax-project-registered-literal-values
 #'sh04-syntax-project-registered-literal-values,
 :sh04-syntax-semantic-span #'sh04-syntax-semantic-span,
 :sh04-syntax-strip-host-metadata #'sh04-syntax-strip-host-metadata}
)

(defn- syntax-stage-descriptor-operations [] syntax-stage-descriptor-defaults)
(defn- syntax-with-source-descriptor-context [operations thunk]
  (if (identical? operations syntax-stage-descriptor-defaults)
    (thunk)
    (binding [*sh04-syntax-form-index-transaction* nil] (thunk))))

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
  syntax-stage-descriptor-defaults))
