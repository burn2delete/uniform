(clojure.core/require 'gravity.syntax)

(def ^:private c3-syntax-serialization-fixture-var
  (clojure.core/resolve 'c3-syntax-serialization-fixture))
(def ^:private default-c3-syntax-serialization-fixture
  (clojure.core/deref c3-syntax-serialization-fixture-var))
(def ^:private reader-canonical-hash-var
  (clojure.core/resolve 'reader-canonical-hash))
(def ^:private default-reader-canonical-hash
  (clojure.core/deref reader-canonical-hash-var))
(def ^:private c3-evidence-serialization-fixture-var
  (clojure.core/ns-resolve 'gravity.c3-syntax-evidence
                           'c3-syntax-serialization-fixture))
(def ^:private default-c3-evidence-serialization-fixture
  (clojure.core/deref c3-evidence-serialization-fixture-var))
(def ^:private mark-production-default-serialization-operation
  (clojure.core/deref (clojure.core/ns-resolve
                       'gravity.c3-syntax-verification
                       'mark-production-default-serialization-operation)))

;; Reuse is safe only for the original production operation and its
;; serialization dependencies. Re-defined or injected callbacks stay
;; observable and are deliberately left unmarked.
(defn- c3-syntax-serialization-live-valid?
  []
  (and (identical? (clojure.core/deref c3-syntax-serialization-fixture-var)
                   default-c3-syntax-serialization-fixture)
       (identical? (clojure.core/deref reader-canonical-hash-var)
                   default-reader-canonical-hash)
       (identical?
        (clojure.core/deref c3-evidence-serialization-fixture-var)
        default-c3-evidence-serialization-fixture)))

(defn- c3-syntax-serialization-operation
  [operation]
  (let [live-valid? (c3-syntax-serialization-live-valid?)]
    (if (and (identical? operation default-c3-syntax-serialization-fixture)
             live-valid?)
      (mark-production-default-serialization-operation
       operation c3-syntax-serialization-live-valid?)
      operation)))

(declare ^:private c3-current-reader-revision-authentic?)
(declare ^:private sh04-syntax-reader-expansion-history sh04-syntax-reader-expansions-current?)
(declare sh04-development-syntax-verification-context!
         sh04-syntax-verification-binding!)
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
  :sh04-syntax-verification-binding! #'sh04-syntax-verification-binding!,
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
   (sh04-development-syntax-verification-context!
    invocation c2-artifact gravity-boundary)
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
   :c3-syntax-serialization-fixture
   (c3-syntax-serialization-operation
    (clojure.core/deref c3-syntax-serialization-fixture-var))
   :c3-syntax-stream-reader-products-authentic?
   (fn [stream c2 boundary] (c3-syntax-stream-reader-products-authentic? stream c2 boundary invocation))
   :c3-syntax-verification-report
   (fn [stream serialization c2 boundary] (c3-syntax-verification-report stream serialization c2 boundary invocation))
   :c3-syntax-capability-proof (fn [artifact] (c3-syntax-capability-proof artifact invocation))
   :c3-syntax-validate! (fn [path artifact] (c3-syntax-validate! path artifact invocation))
   :c3-syntax-fail! c3-syntax-fail!
   :c3-syntax-diagnostic-ids c3-syntax-diagnostic-ids})
