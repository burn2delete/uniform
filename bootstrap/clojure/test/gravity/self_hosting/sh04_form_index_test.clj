(ns gravity.self-hosting.sh04-form-index-test
  (:require [clojure.test :refer [deftest is]] [gravity.bootstrap :as b]))

(defn- private-var [name] (or (ns-resolve 'gravity.bootstrap name) (throw (Exception. "Missing index helper"))))
(defn- index [artifact] ((private-var 'sh04-syntax-form-index) artifact))
(defn- transaction [artifact f]
  (let [slot (volatile! nil)]
    (with-bindings {(private-var '*sh04-syntax-form-index-transaction*)
                    {:artifact artifact :thread (Thread/currentThread) :slot slot}}
      (f slot))))
(defn- artifact [] {:form-tree (mapv #(hash-map :form-id (keyword (str "form-" %)) :children []) (range 20))})
(defn- failure [f] (try (f) nil (catch Throwable t t)))
(defn- original-index [artifact] (into {} (map (juxt :form-id identity)) (:form-tree artifact)))

(deftest native-index-reuse-is-local-to-identical-source-tree-and-owner
  (let [a (artifact) original (original-index a)]
    (is (= original (index a)))
    (is (not (identical? (index a) (index a))))
    (let [first-index (transaction a
                        (fn [slot]
                          (let [first-index (index a) clone (assoc a :other :source)]
                            (is (= original first-index))
                            (is (identical? first-index (index a)))
                            (is (identical? (:form-tree a) (:tree @slot)))
                            (is (not (identical? first-index (index clone))))
                            (is (not (identical? first-index @(future (index a)))))
                            (is (identical? first-index (index a)))
                            first-index)))]
      (is (not (identical? first-index (transaction a (fn [_] (index a)))))))))

(deftest duplicate-nil-string-unicode-and-nonnative-input-semantics
  (doseq [records [[] [{:form-id :a :value 1} {:form-id :a :value 2}]
                   [{:form-id nil :value :nil}] [{:form-id (str "unicode-" (char 0x03bb)) :value :unicode}]
                   (list {:form-id :a :children []})
                   [{:form-id (keyword (str (char 0x03bb))) :value (str (char 0x03bb))}]]]
    (let [a {:form-tree records} expected (original-index a)]
      (transaction a (fn [_] (is (= expected (index a))) (is (= expected (index a))))))))

(deftest opaque-source-and-record-lookups-keep-original-count-and-order
  (let [lookups (atom []) tree (:form-tree (artifact))
        a (reify clojure.lang.ILookup
            (valAt [_ key] (swap! lookups conj key) (when (= :form-tree key) tree))
            (valAt [_ key missing] (swap! lookups conj key) (if (= :form-tree key) tree missing)))]
    (transaction a (fn [slot] (index a) (index a) (is (nil? @slot))))
    (is (= [:form-tree :form-tree] @lookups)))
  (let [lookups (atom 0)
        record (reify clojure.lang.ILookup
                 (valAt [_ key] (swap! lookups inc) (when (= :form-id key) :opaque))
                 (valAt [_ key missing] (swap! lookups inc) (if (= :form-id key) :opaque missing)))
        a {:form-tree [record]}]
    (transaction a (fn [slot] (index a) (index a) (is (nil? @slot))))
    (is (= 2 @lookups))))

(deftest native-source-with-opaque-key-falls-back-without-extra-callbacks
  (let [calls (atom 0) opaque (proxy [Object] []
                              (hashCode [] (swap! calls inc) 1)
                              (equals [_] (swap! calls inc) false))
        a (assoc (artifact) opaque :opaque-value)
        run (fn [f] (reset! calls 0) (f) @calls)
        baseline (run #(do (original-index a) (original-index a)))
        observed (run #(transaction a (fn [slot] (index a) (index a) (is (nil? @slot)))))]
    (is (= baseline observed))))

(deftest opaque-form-id-collision-cannot-hide-behind-a-native-index-key
  (let [calls (atom 0) native-id :same
        opaque (proxy [Object clojure.lang.IHashEq] []
                 (hashCode [] (swap! calls inc) (.hashCode :same))
                 (hasheq [] (swap! calls inc) (.hasheq ^clojure.lang.IHashEq native-id))
                 (equals [other] (swap! calls inc) (identical? :same other)))
        ;; Exceed ArrayMap width so the native HashMap collision path invokes
        ;; the opaque incoming key and retains the earlier native key.
        a {:form-tree (into [{:form-id :same :value 1}]
                            (concat (map #(hash-map :form-id (keyword (str "other-" %))) (range 20))
                                    [{:form-id opaque :value 2}]))}
        run (fn [f] (reset! calls 0) (f) @calls)
        baseline (run #(do (original-index a) (original-index a)))
        observed (run #(transaction a (fn [slot] (index a) (index a) (is (nil? @slot)))))]
    (is (pos? baseline))
    (is ((private-var 'sh04-syntax-native-keyword-map?) (original-index a)))
    (is (= baseline observed))))

(deftest every-native-factory-root-remains-live-after-an-index-hit
  (doseq [name '[into map juxt identity transduce transient conj! persistent! meta with-meta]]
    (let [v (ns-resolve 'clojure.core name) original @v a (artifact) owner (ex-info "second call rejects" {:root name})]
      (transaction a
        (fn [_]
          (let [first-index (index a)]
          ;; Restore with direct Var APIs: redefining map/into must not disable
          ;; the test framework's own restoration path.
          (let [[baseline observed] (try
                        (.bindRoot ^clojure.lang.Var v (fn [& _] (throw owner)))
                        [(try [:returned (original-index a)] (catch Throwable t [:threw t]))
                         (try [:returned (index a)] (catch Throwable t [:threw t]))]
                        (finally (.bindRoot ^clojure.lang.Var v original)))]
            (is (= (first baseline) (first observed)) (str name))
            (if (= :threw (first baseline))
              (is (identical? (second baseline) (second observed)) (str name))
              (do (is (= (second baseline) (second observed)) (str name))
                  (is (not (identical? first-index (second observed))) (str name)))))))))))

(deftest interposed-descriptor-walker-and-runtime-disable-reuse
  (doseq [v [#'b/sh04-syntax-source-descriptor #'b/sh04-syntax-descendant-form-ids
             #'clojure.core/clojure-version]]
    (let [a (artifact) original @v]
      (transaction a
        (fn [_]
          (let [first-index (index a)
                next-index (try
                             (.bindRoot ^clojure.lang.Var v (fn [& _] (throw (Exception. "not called by index"))))
                             (index a)
                             (finally (.bindRoot ^clojure.lang.Var v original)))]
            (is (= first-index next-index))
            (is (not (identical? first-index next-index))))))))
  (let [a (artifact)]
    (transaction a
      (fn [_]
        (let [first-index (index a)
              next-index (binding [*clojure-version* (assoc *clojure-version* :qualifier "different")]
                           (index a))]
          (is (= first-index next-index))
          (is (not (identical? first-index next-index))))))))

(deftest same-value-thread-binding-still-disables-reuse
  (let [a (artifact)]
    (transaction a
      (fn [_]
        (let [first-index (index a)
              next-index (binding [*clojure-version* *clojure-version*] (index a))]
          (is (= first-index next-index))
          (is (not (identical? first-index next-index))))))))

(deftest runtime-qualification-adds-no-version-formatting-callback
  (let [a (artifact) original @#'clojure.core/str owner (ex-info "str must not run" {})]
    (transaction a
      (fn [_]
        (let [first-index (index a)
              next-index (try
                           (.bindRoot #'clojure.core/str (fn [& _] (throw owner)))
                           (index a)
                           (finally (.bindRoot #'clojure.core/str original)))]
          (is (identical? first-index next-index)))))))

(deftest descriptor-signature-and-early-error-order-are-preserved
  (is (= '([seed form-record c2-artifact integrity-report semantic-source-id reader-binding reader-source-revision registered-literals])
         (:arglists (meta #'b/sh04-syntax-source-descriptor))))
  (let [a (artifact) owner (ex-info "span fails first" {})]
    (transaction a
      (fn [slot]
        (let [error (with-redefs [b/sh04-syntax-semantic-span (fn [& _] (throw owner))]
                      (failure #(b/sh04-syntax-source-descriptor {} {} a nil nil nil nil nil)))]
          (is (identical? owner error))
          (is (nil? @slot)))))))
