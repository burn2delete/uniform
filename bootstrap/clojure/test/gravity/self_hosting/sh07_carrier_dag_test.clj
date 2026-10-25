(ns gravity.self-hosting.sh07-carrier-dag-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

(defn- private-fn [name]
  (or (some-> (ns-resolve 'gravity.bootstrap name) var-get)
      (throw (ex-info "Missing private Gravity var" {:name name}))))

(defn- analysis
  ([value] (analysis (java.util.IdentityHashMap.) value))
  ([memo value]
   ((private-fn 'sh07-proof-transaction-immutable-carrier-analysis) memo value)))

(defn- cyclic-map [edge]
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

(deftest shared-artifact-and-report-remain-immutable
  (let [leaf (with-meta {:payload [1 2 {:ok true}]} {:origin [:safe]})
        artifact {:left leaf :right leaf :nested [leaf {:again leaf}]}
        report {:status :passed :shared leaf}
        memo (java.util.IdentityHashMap.)]
    (is (:immutable? (analysis memo artifact)))
    (is (:immutable? (analysis memo report)))))

(deftest shared-dag-validity-does-not-depend-on-expanded-occurrence-count
  (let [carrier (reduce (fn [child _] [child child]) [:leaf] (range 64))]
    (is (= {:immutable? true :reason nil} (analysis carrier)))))

(deftest cycles-remain-rejected
  (doseq [edge [:entry :metadata]]
    (let [result (analysis (cyclic-map edge))]
      (is (false? (:immutable? result)) (name edge))
      (is (= :carrier-cycle (:reason result)) (name edge)))))

(deftest equal-distinct-and-poisoned-metadata-is-never-identity-aliased
  (let [safe (with-meta [1 2 3] {:safe true})
        poisoned (with-meta [1 2 3] {:poison (atom 1)})
        result (analysis [safe poisoned])]
    (is (= safe poisoned))
    (is (not (identical? safe poisoned)))
    (is (false? (:immutable? result)))
    (is (= :mutable-or-unknown-carrier (:reason result)))))

(deftest invalid-lazy-mutable-and-shared-invalid-carriers-remain-rejected
  (doseq [value [(lazy-seq [1]) (atom 1)
                 (java.util.concurrent.atomic.AtomicInteger. 1)
                 (let [invalid (atom 1)] [invalid invalid])
                 (with-meta [:safe] {:invalid (lazy-seq [1])})]]
    (is (false? (:immutable? (analysis value))))))

(defn- context []
  (atom {:open? true :owner-thread-id (.getId (Thread/currentThread))
         :phase :construction :epoch 0 :receipts [] :executions {} :reuses {}
         :check-catalogs {} :failed-report-executions 0}))

(deftest cyclic-carrier-is-never-retained-for-receipt-reuse
  (let [context-var (ns-resolve 'gravity.bootstrap '*sh07-proof-transaction-context*)
        report-fn (private-fn 'sh07-proof-transaction-report)
        state (context)
        artifact {:artifact-id "sha256:cycle" :carrier (cyclic-map :entry)}
        passed {:artifact :test/report :status :passed
                :checks {:complete? true} :failed-checks []}
        executions (atom 0)
        verifier #(do (swap! executions inc) passed)
        epoch {:verifier-root verifier :report-schema-version 1
               :check-catalog-domain :test/cycle}]
    (with-bindings {context-var state}
      (report-fn :sh07 :final epoch artifact verifier)
      (report-fn :sh07 :final epoch artifact verifier))
    (is (= 2 @executions))
    (is (empty? (:receipts @state)))
    (is (nil? (get-in @state [:reuses :sh07])))))

(deftest receipt-retention-has-no-project-quota-and-never-reuses-failed-reports
  (let [context-var (ns-resolve 'gravity.bootstrap '*sh07-proof-transaction-context*)
        report-fn (private-fn 'sh07-proof-transaction-report)
        state (context)
        artifacts (mapv (fn [index] {:artifact-id (str "sha256:artifact-" index) :value [index]}) (range 65))
        passed {:artifact :test/report :status :passed :checks {:complete? true} :failed-checks []}
        failed (assoc passed :status :failed :checks {:complete? false} :failed-checks [:complete])
        executions (atom 0)
        verifier #(do (swap! executions inc) passed)
        epoch {:verifier-root verifier :report-schema-version 1 :check-catalog-domain :test/receipts}
        changed (assoc (first artifacts) :value [:changed])]
    (with-bindings {context-var state}
      (doseq [artifact artifacts]
        (is (= passed (report-fn :sh07 :final epoch artifact verifier))))
      (is (= passed (report-fn :sh07 :final epoch (first artifacts) verifier)))
      (is (= 65 @executions))
      (is (= 65 (count (:receipts @state))))
      (dotimes [_ 2]
        (is (= failed (report-fn :sh07 :final epoch changed #(do (swap! executions inc) failed))))))
    (is (= 67 @executions))
    (is (= 65 (count (:receipts @state))))
    (is (= 2 (:failed-report-executions @state)))))
