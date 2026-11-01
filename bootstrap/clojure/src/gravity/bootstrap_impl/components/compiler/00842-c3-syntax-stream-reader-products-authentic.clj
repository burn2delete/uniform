(clojure.core/require 'gravity.syntax)

(declare ^:private c3-current-reader-revision-authentic?)
(declare ^:private sh04-syntax-reader-expansion-history sh04-syntax-reader-expansions-current?)
(defn-
 syntax-stage-reader-verification-operations
 []
 {:c2-top-level-products #'c2-top-level-products,
  :c3-c2-reader-integrity-report #'c3-c2-reader-integrity-report,
  :c3-current-reader-revision-authentic?
  #'c3-current-reader-revision-authentic?,
  :c3-generated-syntax-object #'c3-generated-syntax-object,
  :c3-syntax-object #'c3-syntax-object,
  :p15-s23-c6c10-canonical-digest #'p15-s23-c6c10-canonical-digest,
  :p15-s23-stage2-sh02-descriptor-envelope
  #'p15-s23-stage2-sh02-descriptor-envelope,
  :p15-s23-stage2-sh02-descriptor-envelope-verify!
  #'p15-s23-stage2-sh02-descriptor-envelope-verify!,
  :reader-canonical-hash #'reader-canonical-hash,
  :sh03-reader-authenticated-envelope-options
  #'sh03-reader-authenticated-envelope-options,
  :sh04-syntax-adapter-contract sh04-syntax-adapter-contract,
  :sh04-syntax-current-binding! #'sh04-syntax-current-binding!,
  :sh04-syntax-current-sh03-product-binding
  #'sh04-syntax-current-sh03-product-binding,
  :sh04-syntax-descriptor-sh03-product-binding
  #'sh04-syntax-descriptor-sh03-product-binding,
  :sh04-syntax-execute! #'sh04-syntax-execute!,
  :sh04-syntax-reader-binding #'sh04-syntax-reader-binding,
  :sh04-syntax-reader-expansion-history
  #'sh04-syntax-reader-expansion-history,
  :sh04-syntax-reader-expansions-current?
  #'sh04-syntax-reader-expansions-current?,
  :sh04-syntax-registered-literal-projection-authentic?
  #'sh04-syntax-registered-literal-projection-authentic?,
  :sh04-syntax-rich-object #'sh04-syntax-rich-object,
  :sh04-syntax-semantic-source-id #'sh04-syntax-semantic-source-id,
  :sh04-syntax-sh02-descriptor #'sh04-syntax-sh02-descriptor})

(defn c3-syntax-stream-reader-products-authentic?
  ([syntax-stream c2-artifact]
   (c3-syntax-stream-reader-products-authentic? syntax-stream c2-artifact nil))
  ([syntax-stream c2-artifact gravity-boundary]
   (c3-syntax-stream-reader-products-authentic? syntax-stream c2-artifact gravity-boundary nil))
  ([syntax-stream c2-artifact gravity-boundary invocation]
   (gravity.syntax/reader-products-authentic?
    syntax-stream c2-artifact gravity-boundary invocation
    (syntax-stage-reader-verification-operations))))

(declare c3-syntax-verification-report
         c3-syntax-capability-proof
         c3-syntax-validate!)

(defn- c3-syntax-verification-ops
  [invocation]
  {:c3-syntax-schema c3-syntax-schema
   :c3-resolvable-span? c3-resolvable-span?
   :c3-syntax-serialization-fixture c3-syntax-serialization-fixture
   :c3-syntax-stream-reader-products-authentic?
   (fn [stream c2 boundary] (c3-syntax-stream-reader-products-authentic? stream c2 boundary invocation))
   :c3-syntax-verification-report
   (fn [stream serialization c2 boundary] (c3-syntax-verification-report stream serialization c2 boundary invocation))
   :c3-syntax-capability-proof (fn [artifact] (c3-syntax-capability-proof artifact invocation))
   :c3-syntax-validate! (fn [path artifact] (c3-syntax-validate! path artifact invocation))
   :c3-syntax-fail! c3-syntax-fail!
   :c3-syntax-diagnostic-ids c3-syntax-diagnostic-ids})
