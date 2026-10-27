(ns gravity.self-hosting.sh04-literal-index-test
  (:require [clojure.test :refer [deftest is]] [gravity.bootstrap :as b]
            [gravity.c3-literal-projection :as l]
            [gravity.c3-literal-projection.indexes :as indexes]))
(defn- pv [name] (ns-resolve 'gravity.bootstrap name))
(defn- transaction [artifact f]
  (let [slot (volatile! nil)]
    (with-bindings {(pv '*sh04-syntax-form-index-transaction*)
                    {:artifact artifact :thread (Thread/currentThread) :slot slot}}
      (f slot))))
(defn- artifact []
  {:form-tree (mapv #(hash-map :form-id (keyword (str "f" %)) :kind :list :children []) (range 20))
   :token-stream (mapv #(hash-map :token-id (keyword (str "t" %))) (range 20))
   :literal-decoding-records [] :semantic-error-deferment-record {:deferred-literal-records []}})
(defn- outcome [f] (try [:returned (f)] (catch Throwable t [:threw t])))

(deftest complete-family-reuses-both-pure-indexes-only-in-its-transaction
  (let [a (artifact) seed {:form '(x) :form-id :f0} form (first (:form-tree a))]
    (is (not (identical? (b/c3-form-index a) (b/c3-form-index a))))
    (transaction a
      (fn [slot]
        (let [sh04 ((pv 'sh04-syntax-form-index) a)
              values [(b/c3-lossless-literal-descriptor seed form a {:authentic? false})
                      (b/c3-source-form-kind seed form a {:authentic? false})
                      (b/c3-source-facts seed form a {:authentic? false})]
              forms (b/c3-form-index a) tokens (b/c3-token-index a)]
          (is (= [nil :list {}] values))
          (is (identical? sh04 ((pv 'sh04-syntax-form-index) a)))
          (is (identical? forms (get-in @slot [:literal-forms :index])))
          (is (identical? tokens (get-in @slot [:literal-tokens :index])))
          (is (identical? forms (b/c3-form-index a)))
          (is (identical? tokens (b/c3-token-index a)))
          (is (not (identical? forms @(future (b/c3-form-index a)))))
          (is (not (identical? forms (b/c3-form-index (assoc a :other true)))))
          (is (not (identical? forms (transaction a (fn [_] (b/c3-form-index a)))))))))))

(deftest input-shapes-duplicates-and-unicode-preserve-last-wins
  (doseq [id [:same (keyword (str (char 0x03bb))) nil "string"]]
    (let [a {:form-tree [{:form-id id :value 1} {:form-id id :value 2}]
             :token-stream [{:token-id id :value 1} {:token-id id :value 2}]}]
      (transaction a (fn [_]
                       (is (= (indexes/form-index a) (b/c3-form-index a)))
                       (is (= (indexes/token-index a) (b/c3-token-index a)))
                       (when-not (identical? clojure.lang.Keyword (class id))
                         (is (not (identical? (b/c3-form-index a) (b/c3-form-index a))))))))))

(deftest fresh-integrity-callback-and-second-rejection-survive-index-hits
  (let [a (artifact) calls (atom 0) owner (ex-info "second rejects" {})]
    (transaction a
      (fn [_]
        (b/c3-form-index a) (b/c3-token-index a)
        (let [result (with-redefs [b/c3-c2-reader-integrity-report
                                  (fn [_] (if (= 2 (swap! calls inc)) (throw owner) {:authentic? false}))]
                       [(outcome #(b/c3-lossless-literal-descriptor {} {:kind :list} a nil))
                        (outcome #(b/c3-lossless-literal-descriptor {} {:kind :list} a nil))])]
          (is (= [:returned nil] (first result)))
          (is (identical? owner (second (second result))))
          (is (= 2 @calls)))))))

(deftest every-live-factory-consumer-and-predicate-root-disables-reuse
  (doseq [v [#'clojure.core/keep #'clojure.core/get
             #'clojure.core/reduce #'clojure.core/class
             #'l/c3-index-operations-current? #'indexes/operations-current?
             #'l/current-operation #'l/invoke
             #'b/c3-source-form-kind #'l/with-operations]]
    (let [a (artifact) original @v]
      (transaction a
        (fn [_]
          (let [first-index (b/c3-form-index a)
                result (try
                         (.bindRoot ^clojure.lang.Var v (fn [& args] (apply original args)))
                         (outcome #(b/c3-form-index a))
                         (finally (.bindRoot ^clojure.lang.Var v original)))]
            (is (= :returned (first result)))
            (is (= first-index (second result)))
            (is (not (identical? first-index (second result))))))))))

(deftest altered-effective-operation-map-cannot-reuse-even-identical-source
  (let [a (artifact) calls (atom 0) marker (ex-info "custom factory rejects" {})]
    (transaction a
      (fn [slot]
        (let [first-index (b/c3-form-index a)
              first-entry (:literal-forms @slot)
              result (with-bindings {(pv '*c3-literal-projection-leaf-call?*) true}
                       (l/with-operations
                        (assoc ((pv 'c3-literal-projection-ops))
                               :c3-form-index (fn [_] (swap! calls inc) (throw marker)))
                        #(outcome (fn [] (b/c3-form-index a)))))]
          (is (identical? marker (second result))) (is (= 1 @calls))
          (is (identical? first-entry (:literal-forms @slot)))
          (is (identical? first-index (b/c3-form-index a))))))))

(deftest tagged-path-and-nested-kind-facts-use-the-shared-form-factory
  (let [span {:source "tag.gravity" :byte-start 0 :byte-end 10}
        form {:form-id :tag :kind :tagged-literal :tag 'schema :children [:payload] :raw "#schema x" :span span}
        a {:form-tree [form {:form-id :payload :kind :string :value "x"}]
           :token-stream [] :literal-decoding-records [{:form-id :tag :raw "#schema x" :span span :facts {:tag 'schema}}]}
        seed {:form-id :tag :syntax-id :seed :form '(tagged)}]
    (transaction a
      (fn [slot]
        (let [descriptor (b/c3-tagged-literal-descriptor seed form a {:authentic? true})
              index (get-in @slot [:literal-forms :index])]
          (is (= "x" (:payload descriptor)))
          (is (= :tagged-literal (b/c3-source-form-kind seed form a {:authentic? true})))
          (is (= descriptor (:reader-literal-descriptor (b/c3-source-facts seed form a {:authentic? true}))))
          (is (identical? index (get-in @slot [:literal-forms :index]))))))))

(deftest opaque-form-and-token-record-callbacks-remain-fresh
  (doseq [[factory field id] [[b/c3-form-index :form-tree :form-id] [b/c3-token-index :token-stream :token-id]]]
    (let [calls (atom 0) record (reify clojure.lang.ILookup
                               (valAt [_ key] (swap! calls inc) (when (= id key) :opaque))
                               (valAt [_ key missing] (swap! calls inc) (if (= id key) :opaque missing)))
          a {field [record]}]
      (transaction a (fn [slot] (factory a) (factory a) (is (nil? @slot))))
      (is (= 2 @calls)))))

(deftest dynamic-same-value-runtime-binding-disables-both-indexes
  (let [a (artifact)]
    (transaction a
      (fn [_]
        (let [forms (b/c3-form-index a) tokens (b/c3-token-index a)
              fresh (binding [*clojure-version* *clojure-version*]
                      [(b/c3-form-index a) (b/c3-token-index a)])]
          (is (= [forms tokens] fresh))
          (is (not (identical? forms (first fresh))))
          (is (not (identical? tokens (second fresh)))))))))

(deftest opaque-collision-cannot-introduce-a-post-build-get-callback
  (doseq [[factory field id] [[b/c3-form-index :form-tree :form-id] [b/c3-token-index :token-stream :token-id]]]
    (let [original @#'clojure.core/get calls (atom 0) owner (ex-info "get must not be introduced" {})
          changed (fn [& _] (swap! calls inc) (throw owner))
          native :same
          opaque (proxy [Object clojure.lang.IHashEq] []
                   (hashCode [] (.bindRoot #'clojure.core/get changed) (.hashCode native))
                   (hasheq [] (.bindRoot #'clojure.core/get changed) (.hasheq ^clojure.lang.IHashEq native))
                   (equals [other] (identical? native other)))
          records (into [{id native :value 1}]
                        (concat (map #(hash-map id (keyword (str "other" %))) (range 20)) [{id opaque :value 2}]))
          a {field records}
          result (transaction a
                   (fn [slot]
                     (try [(outcome #(factory a)) @slot]
                          (finally (.bindRoot #'clojure.core/get original)))))]
      (is (= :returned (ffirst result)))
      (is (zero? @calls)) (is (nil? (second result))))))

(deftest reduction-protocol-callbacks-remain-live-after-a-native-index-hit
  (doseq [[factory fresh] [[b/c3-form-index indexes/form-index] [b/c3-token-index indexes/token-index]]
          v [#'clojure.core.protocols/coll-reduce #'clojure.core.protocols/internal-reduce]]
    (let [a {:form-tree (mapv #(hash-map :form-id (keyword (str "f" %))) (range 65))
             :token-stream (mapv #(hash-map :token-id (keyword (str "t" %))) (range 65))}
          original @v owner (ex-info "reduction callback rejects" {})
          calls (java.util.concurrent.atomic.AtomicLong.)]
      (transaction a
        (fn [_]
          (with-bindings {(pv '*c3-literal-projection-leaf-call?*) true}
            (l/with-operations ((pv 'c3-literal-projection-ops))
              (fn []
                (let [first-index (factory a) hit (factory a)
                      results (try
                                (.bindRoot ^clojure.lang.Var v (fn [& _] (.incrementAndGet calls) (throw owner)))
                                [(outcome #(fresh a)) (outcome #(factory a))]
                                (finally (.bindRoot ^clojure.lang.Var v original)))]
                  (is (identical? first-index hit))
                  (is (identical? owner (second (first results))))
                  (is (identical? owner (second (second results))))
                  (is (= 2 (.get calls))))))))))))
