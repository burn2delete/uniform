(ns gravity.self-hosting.sh07-carrier-dag-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- private-fn
  [name]
  (or (some-> (ns-resolve 'gravity.bootstrap name) var-get)
      (throw (ex-info "Missing private Gravity var" {:name name}))))

(defn- analysis
  ([value maximum]
   (analysis (java.util.IdentityHashMap.) value maximum))
  ([memo value maximum]
   ((private-fn 'sh07-proof-transaction-immutable-carrier-analysis)
    memo value maximum)))

(defn- legacy-analysis
  [value maximum]
  (let [pending (java.util.ArrayDeque.)
        push! #(when-not (nil? %) (.push pending %))]
    (push! value)
    (loop [visited 0]
      (cond
        (> visited maximum) {:immutable? false :logical-nodes visited}
        (.isEmpty pending) {:immutable? true :logical-nodes visited}
        :else
        (let [current (.pop pending)
              metadata (when (instance? clojure.lang.IObj current)
                         (meta current))]
          (push! metadata)
          (cond
            (or (boolean? current)
                (instance? java.lang.Byte current)
                (instance? java.lang.Short current)
                (instance? java.lang.Integer current)
                (instance? java.lang.Long current)
                (instance? java.lang.Float current)
                (instance? java.lang.Double current)
                (instance? java.math.BigInteger current)
                (instance? java.math.BigDecimal current)
                (instance? clojure.lang.BigInt current)
                (instance? clojure.lang.Ratio current)
                (string? current) (keyword? current) (symbol? current)
                (char? current))
            (recur (inc visited))

            (instance? clojure.lang.LazySeq current)
            {:immutable? false :logical-nodes visited}

            (map? current)
            (do
              (doseq [[key entry] current] (push! key) (push! entry))
              (recur (inc visited)))

            (or (vector? current) (set? current) (list? current)
                (instance? clojure.lang.IMapEntry current))
            (do (doseq [entry current] (push! entry))
                (recur (inc visited)))

            :else {:immutable? false :logical-nodes visited}))))))

(defn- cyclic-map
  [edge]
  (java.lang.reflect.Proxy/newProxyInstance
   (.getClassLoader clojure.lang.IPersistentMap)
   (into-array Class [clojure.lang.IPersistentMap clojure.lang.IObj])
   (reify java.lang.reflect.InvocationHandler
     (invoke [_ proxy method _]
       (case (.getName method)
         "seq" (when (= :entry edge) (seq [[:self proxy]]))
         "meta" (when (= :metadata edge) {:self proxy})
         "count" (if (= :entry edge) 1 0)
         "iterator" (.iterator ^java.lang.Iterable
                               (if (= :entry edge) [[:self proxy]] []))
         "toString" "<cyclic-carrier>"
         "hashCode" (System/identityHashCode proxy)
         "equals" false
         nil)))))

(deftest shared-diamond-preserves-expanded-count-and-reduces-examinations
  (let [leaf (with-meta {:payload [1 2 {:ok true}]} {:origin [:safe]})
        carrier {:left leaf :right leaf :nested [leaf {:again leaf}]}
        legacy (legacy-analysis carrier 1000)
        optimized (analysis carrier 1000)]
    (is (:immutable? legacy))
    (is (:immutable? optimized))
    (is (= (:logical-nodes legacy) (:logical-nodes optimized)))
    (is (< (:examined-nodes optimized) (:logical-nodes legacy)))
    (is (pos? (:reused-identities optimized)))))

(deftest artifact-and-report-share-memo-but-retain-independent-logical-bounds
  (let [leaf {:shared (vec (range 20))}
        artifact {:artifact leaf :copy leaf}
        report {:status :passed :shared leaf}
        memo (java.util.IdentityHashMap.)
        artifact-result (analysis memo artifact 1000)
        report-result (analysis memo report 1000)]
    (is (= (:logical-nodes (legacy-analysis artifact 1000))
           (:logical-nodes artifact-result)))
    (is (= (:logical-nodes (legacy-analysis report 1000))
           (:logical-nodes report-result)))
    (is (:immutable? artifact-result))
    (is (:immutable? report-result))
    (is (pos? (:reused-identities report-result)))
    (is (< (:examined-nodes report-result)
           (:logical-nodes report-result)))))

(deftest cycles-are-rejected-before-the-logical-bound
  (doseq [edge [:entry :metadata]]
    (let [result (analysis (cyclic-map edge) 100)]
      (is (false? (:immutable? result)) (name edge))
      (is (= :carrier-cycle (:reason result)) (name edge))
      (is (< (:logical-nodes result) 100) (name edge)))))

(deftest equal-distinct-and-poisoned-metadata-is-never-identity-aliased
  (let [safe (with-meta [1 2 3] {:safe true})
        poisoned (with-meta [1 2 3] {:poison (atom 1)})
        result (analysis [safe poisoned] 100)]
    (is (= safe poisoned))
    (is (not (identical? safe poisoned)))
    (is (false? (:immutable? result)))
    (is (= :mutable-or-unknown-carrier (:reason result)))))

(deftest invalid-lazy-mutable-and-shared-invalid-carriers-remain-rejected
  (doseq [value [(lazy-seq [1])
                 (atom 1)
                 (java.util.concurrent.atomic.AtomicInteger. 1)
                 (let [invalid (atom 1)] [invalid invalid])
                 (with-meta [:safe] {:invalid (lazy-seq [1])})]]
    (is (false? (:immutable? (analysis value 100))))))

(deftest cyclic-carrier-is-never-retained-for-receipt-reuse
  (let [context-var
        (ns-resolve 'gravity.bootstrap '*sh07-proof-transaction-context*)
        report-fn (private-fn 'sh07-proof-transaction-report)
        context
        (atom {:open? true :owner-thread-id (.getId (Thread/currentThread))
               :phase :construction :epoch 0 :maximum-receipts 4
               :receipts [] :executions {} :reuses {} :check-catalogs {}
               :failed-report-executions 0})
        artifact {:artifact-id "sha256:cycle" :carrier (cyclic-map :entry)}
        passed {:artifact :test/report :status :passed
                :checks {:complete? true} :failed-checks []}
        executions (atom 0)
        verifier #(do (swap! executions inc) passed)
        epoch {:verifier-root verifier :report-schema-version 1
               :check-catalog-domain :test/cycle}]
    (with-bindings {context-var context}
      (report-fn :sh07 :final epoch artifact verifier)
      (report-fn :sh07 :final epoch artifact verifier))
    (is (= 2 @executions))
    (is (empty? (:receipts @context)))
    (is (nil? (get-in @context [:reuses :sh07])))))

(deftest shared-subtree-still-consumes-its-expanded-logical-bound
  (let [leaf [1 2 3]
        carrier [leaf leaf leaf]
        legacy-over (legacy-analysis carrier 12)
        optimized-over (analysis carrier 12)
        optimized-exact (analysis carrier 13)]
    (is (= 13 (:logical-nodes legacy-over)))
    (is (= 13 (:logical-nodes optimized-over)))
    (is (false? (:immutable? optimized-over)))
    (is (= :carrier-node-bound (:reason optimized-over)))
    (is (:immutable? optimized-exact))
    (is (= 13 (:logical-nodes optimized-exact)))))

(deftest synthetic-shared-dag-benchmark-has-deterministic-count-reduction
  (let [leaf (reduce (fn [child index] {:index index :child child})
                     [:end] (range 64))
        carrier (vec (repeat 1000 leaf))
        legacy-start (System/nanoTime)
        legacy (legacy-analysis carrier 1000000)
        legacy-nanos (- (System/nanoTime) legacy-start)
        optimized-start (System/nanoTime)
        optimized (analysis carrier 1000000)
        optimized-nanos (- (System/nanoTime) optimized-start)]
    (is (:immutable? legacy))
    (is (:immutable? optimized))
    (is (= (:logical-nodes legacy) (:logical-nodes optimized)))
    (is (< (* 20 (:examined-nodes optimized))
           (:logical-nodes legacy)))
    (is (= 999 (:reused-identities optimized)))
    ;; Timing is telemetry only; deterministic work counts are the assertion.
    (is (every? pos? [legacy-nanos optimized-nanos]))))
