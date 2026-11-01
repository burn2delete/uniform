(def
 ^{:private true, :dynamic true} *sh04-syntax-form-index-transaction*
 nil)

(declare
 ^{:private true} sh04-syntax-form-index-roots
 sh04-syntax-form-index-runtime?
 sh04-syntax-literal-operation-defaults)

(declare
 ^{:private true} sh04-syntax-form-index-roots-current?
 sh04-syntax-native-keyword-map?)

(declare
 ^{:private true} syntax-index-runtime-supported?
 syntax-index-literal-operation-defaults)

(def
 ^{:private true} syntax-stage-index-defaults
 {:literal-operation-defaults
  #'syntax-index-literal-operation-defaults,
  :literal-operations-current?
  #'c3-literal-projection/c3-index-operations-current?,
  :runtime-supported? #'syntax-index-runtime-supported?,
  :sh04-syntax-form-index-roots-current?
  #'sh04-syntax-form-index-roots-current?,
  :sh04-syntax-native-keyword-map? #'sh04-syntax-native-keyword-map?})

(defn- syntax-stage-index-operations [] syntax-stage-index-defaults)

(defn-
 syntax-index-runtime-supported?
 []
 sh04-syntax-form-index-runtime?)

(defn-
 syntax-index-literal-operation-defaults
 []
 sh04-syntax-literal-operation-defaults)

(defn-
 sh04-syntax-native-keyword-map?
 [value]
 (gravity.syntax/native-keyword-map? value))

(defn-
 sh04-syntax-form-index-roots-current?
 []
 (gravity.syntax/index-roots-current? sh04-syntax-form-index-roots))

(defn-
 sh04-syntax-form-index
 [c2-artifact]
 (gravity.syntax/form-index
  c2-artifact
  *sh04-syntax-form-index-transaction*
  ^{:line 23, :column 65}
  (syntax-stage-index-operations)
  syntax-stage-index-defaults))

(defn-
 sh04-syntax-literal-index
 [artifact cache-key field id-key original]
 (gravity.syntax/literal-index
  artifact
  cache-key
  field
  id-key
  original
  *sh04-syntax-form-index-transaction*
  ^{:line 23, :column 65}
  (syntax-stage-index-operations)
  syntax-stage-index-defaults))

;; The native PersistentVector factory paths in the supported Clojure runtime.
;; An interposed descriptor/walker or core dependency disables reuse, retaining
;; the original lookup count, callback behavior and error order on that call.
(def ^:private sh04-syntax-form-index-runtime? (= "1.12.5" (clojure-version)))
(def ^:private sh04-syntax-literal-operation-defaults (c3-literal-projection-ops))
(def ^:private sh04-syntax-form-index-roots
  {#'gravity.syntax/source-descriptor gravity.syntax/source-descriptor
   #'gravity.syntax/descendant-form-ids gravity.syntax/descendant-form-ids
   #'syntax-stage-descriptor-defaults syntax-stage-descriptor-defaults
   #'syntax-stage-index-defaults syntax-stage-index-defaults
   #'sh04-syntax-source-descriptor sh04-syntax-source-descriptor
   #'sh04-syntax-descendant-form-ids sh04-syntax-descendant-form-ids
   #'c3-literal-projection-call c3-literal-projection-call
   #'c3-literal-projection-ops c3-literal-projection-ops
   #'c3-form-index c3-form-index #'c3-token-index c3-token-index
   #'c3-lossless-literal-descriptor c3-lossless-literal-descriptor
   #'c3-tagged-literal-descriptor c3-tagged-literal-descriptor
   #'c3-source-form-kind c3-source-form-kind #'c3-source-facts c3-source-facts
   #'c3-ratio-descriptor-from-raw c3-ratio-descriptor-from-raw
   #'c3-deferred-ratio-descriptor-from-raw c3-deferred-ratio-descriptor-from-raw
   #'c3-c2-reader-integrity-report c3-c2-reader-integrity-report #'form-kind form-kind
   #'c3-literal-projection/with-operations c3-literal-projection/with-operations
   #'c3-literal-projection/current-operation @#'c3-literal-projection/current-operation
   #'c3-literal-projection/invoke @#'c3-literal-projection/invoke
   #'c3-literal-projection/c3-form-index c3-literal-projection/c3-form-index
   #'c3-literal-projection/c3-token-index c3-literal-projection/c3-token-index
   #'c3-literal-projection/c3-lossless-literal-descriptor c3-literal-projection/c3-lossless-literal-descriptor
   #'c3-literal-projection/c3-tagged-literal-descriptor c3-literal-projection/c3-tagged-literal-descriptor
   #'c3-literal-projection/c3-source-form-kind c3-literal-projection/c3-source-form-kind
   #'c3-literal-projection/c3-source-facts c3-literal-projection/c3-source-facts
   #'c3-literal-projection/c3-ratio-descriptor-from-raw c3-literal-projection/c3-ratio-descriptor-from-raw
   #'c3-literal-projection/c3-index-operations-current? c3-literal-projection/c3-index-operations-current?
   #'gravity.c3-literal-projection.indexes/form-index gravity.c3-literal-projection.indexes/form-index
   #'gravity.c3-literal-projection.indexes/token-index gravity.c3-literal-projection.indexes/token-index
   #'gravity.c3-literal-projection.indexes/operations-current? gravity.c3-literal-projection.indexes/operations-current?
   #'gravity.c3-literal-projection.indexes/native-keyword-map? @#'gravity.c3-literal-projection.indexes/native-keyword-map?
   #'clojure.core/get get #'clojure.core/keep keep #'clojure.core/class class
   #'clojure.core/reduce reduce #'clojure.core/conj conj
   #'clojure.core.protocols/coll-reduce clojure.core.protocols/coll-reduce
   #'clojure.core.protocols/internal-reduce clojure.core.protocols/internal-reduce
   #'clojure.core/into into #'clojure.core/map map #'clojure.core/juxt juxt
   #'clojure.core/identity identity #'clojure.core/transduce transduce
   #'clojure.core/transient transient #'clojure.core/conj! conj!
   #'clojure.core/persistent! persistent! #'clojure.core/meta meta
   #'clojure.core/with-meta with-meta #'clojure.core/clojure-version clojure-version
   #'clojure.core/*clojure-version* *clojure-version*})
