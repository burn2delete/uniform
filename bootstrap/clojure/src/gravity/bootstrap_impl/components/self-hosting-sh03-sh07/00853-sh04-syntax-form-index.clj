(def ^:dynamic ^:private *sh04-syntax-form-index-transaction* nil)
(declare ^:private sh04-syntax-form-index-roots sh04-syntax-form-index-runtime?)

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
             (sh04-syntax-native-keyword-map? c2-artifact)
             (sh04-syntax-form-index-roots-current?)
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

;; The native PersistentVector/into3 path in the supported Clojure runtime.
;; An interposed descriptor/walker or core dependency disables reuse, retaining
;; the original lookup count, callback behavior and error order on that call.
(def ^:private sh04-syntax-form-index-runtime? (= "1.12.5" (clojure-version)))
(def ^:private sh04-syntax-form-index-roots
  {#'sh04-syntax-source-descriptor sh04-syntax-source-descriptor
   #'sh04-syntax-descendant-form-ids sh04-syntax-descendant-form-ids
   #'clojure.core/into into #'clojure.core/map map #'clojure.core/juxt juxt
   #'clojure.core/identity identity #'clojure.core/transduce transduce
   #'clojure.core/transient transient #'clojure.core/conj! conj!
   #'clojure.core/persistent! persistent! #'clojure.core/meta meta
   #'clojure.core/with-meta with-meta #'clojure.core/clojure-version clojure-version
   #'clojure.core/*clojure-version* *clojure-version*})
