(def ^:dynamic ^:private *sh04-syntax-form-index-transaction* nil)
(declare ^:private sh04-syntax-form-index-roots sh04-syntax-form-index-runtime?)
(declare ^:private sh04-syntax-literal-operation-defaults)

(defn- sh04-syntax-native-keyword-map? [value]
  (and (or (identical? clojure.lang.PersistentArrayMap (class value))
           (identical? clojure.lang.PersistentHashMap (class value)))
       (let [entries (.iterator (.entrySet ^java.util.Map value))]
         (loop []
           (if (.hasNext entries)
             (and (identical? clojure.lang.Keyword
                              (class (.getKey ^java.util.Map$Entry (.next entries))))
                  (recur))
             true)))))

(defn- sh04-syntax-form-index-roots-current? []
  (let [entries (.iterator (.entrySet ^java.util.Map sh04-syntax-form-index-roots))]
    (loop []
      (if (.hasNext entries)
        (let [entry ^java.util.Map$Entry (.next entries)]
          (and (nil? (.getThreadBinding ^clojure.lang.Var (.getKey entry)))
               (identical? (.getValue entry) (.deref ^clojure.lang.Var (.getKey entry)))
               (recur)))
        true))))

(defn- sh04-syntax-form-index [c2-artifact]
  (let [transaction *sh04-syntax-form-index-transaction*]
    (if (and transaction
             (identical? (Thread/currentThread) (:thread transaction))
             (identical? c2-artifact (:artifact transaction))
             (sh04-syntax-form-index-roots-current?)
             (sh04-syntax-native-keyword-map? c2-artifact)
             sh04-syntax-form-index-runtime?)
      (let [tree (:form-tree c2-artifact)
            previous @(:slot transaction)]
        (if (and previous (identical? tree (:tree previous)))
          (:index previous)
          (let [index (into {} (map (juxt :form-id identity)) tree)]
            ;; Only the immutable native lookup projection is reused. All
            ;; descendant, descriptor, validation and authentication work stays
            ;; fresh. Qualification never asks opaque values to compare/hash.
            (when (and (identical? clojure.lang.PersistentVector (class tree))
                       (sh04-syntax-native-keyword-map? index)
                       (let [records (.iterator ^java.lang.Iterable tree)]
                         (loop []
                           (if (.hasNext records)
                             (let [record (.next records)]
                               (and (sh04-syntax-native-keyword-map? record)
                                    (identical? clojure.lang.Keyword (class (:form-id record)))
                                    (recur)))
                             true))))
              (vreset! (:slot transaction) {:tree tree :index index}))
            index)))
      (into {} (map (juxt :form-id identity)) (:form-tree c2-artifact)))))

(defn- sh04-syntax-literal-index
  [artifact cache-key field id-key original]
  (let [transaction *sh04-syntax-form-index-transaction*]
    (if (and transaction
             (identical? (Thread/currentThread) (:thread transaction))
             (identical? artifact (:artifact transaction))
             (sh04-syntax-form-index-roots-current?)
             (sh04-syntax-native-keyword-map? artifact)
             sh04-syntax-form-index-runtime?
             (c3-literal-projection/c3-index-operations-current?
              sh04-syntax-literal-operation-defaults))
      (let [records (get artifact field) previous (get @(:slot transaction) cache-key)]
        (if (and previous (identical? records (:records previous)))
          (:index previous)
          (let [index (original)]
            ;; Preserve the original eager construction before qualification,
            ;; including malformed/opaque callbacks and last-wins behavior.
            (when (and (sh04-syntax-form-index-roots-current?)
                       (identical? clojure.lang.PersistentVector (class records))
                       (sh04-syntax-native-keyword-map? index)
                       (let [iterator (.iterator ^java.lang.Iterable records)]
                         (loop []
                           (if (.hasNext iterator)
                             (let [record (.next iterator)]
                               (and (sh04-syntax-native-keyword-map? record)
                                    (identical? clojure.lang.Keyword
                                                (class (.valAt ^clojure.lang.ILookup record id-key)))
                                    (recur)))
                             true))))
              (vreset! (:slot transaction)
                       (assoc @(:slot transaction) cache-key {:records records :index index})))
            index)))
      (original))))

;; The native PersistentVector factory paths in the supported Clojure runtime.
;; An interposed descriptor/walker or core dependency disables reuse, retaining
;; the original lookup count, callback behavior and error order on that call.
(def ^:private sh04-syntax-form-index-runtime? (= "1.12.5" (clojure-version)))
(def ^:private sh04-syntax-literal-operation-defaults (c3-literal-projection-ops))
(def ^:private sh04-syntax-form-index-roots
  {#'sh04-syntax-source-descriptor sh04-syntax-source-descriptor
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
   #'clojure.core/into into #'clojure.core/map map #'clojure.core/juxt juxt
   #'clojure.core/identity identity #'clojure.core/transduce transduce
   #'clojure.core/transient transient #'clojure.core/conj! conj!
   #'clojure.core/persistent! persistent! #'clojure.core/meta meta
   #'clojure.core/with-meta with-meta #'clojure.core/clojure-version clojure-version
   #'clojure.core/*clojure-version* *clojure-version*})
