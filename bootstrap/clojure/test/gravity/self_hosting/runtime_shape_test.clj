(ns gravity.self-hosting.runtime-shape-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(def ^:private runtime-plan
  (delay
    (let [path "bootstrap/gravity/src/gravity/runtime.gravity"
          text (slurp path)
          emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path text))))

(defn- invoke [name & arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :runtime-shape-test} @runtime-plan name (vec arguments)))

(def ^:private literal {:op :literal :value true})
(def ^:private handler {:type :gravity.type/bool :name 'caught :body literal})
(def ^:private binding-ir {:name 'slot :expr literal})

(deftest complete-handler-shapes-without-cardinality-admission
  (let [check #(invoke 'p15-s23-runtime-typed-handlers-valid? %)
        value {:op :try :protected literal :handlers (vec (repeat 65537 handler))}]
    (is (true? (check value)))
    (testing "The last handler must still satisfy the full type and shape checks"
      (doseq [bad [(assoc handler :type :unknown) (assoc handler :name :bad)
                   (assoc handler :body nil) (assoc handler :extra true) nil]]
        (is (false? (check (update value :handlers assoc 65536 bad))))))
    (is (false? (check (assoc value :handlers []))))
    (is (false? (check (assoc value :extra true))))))

(deftest complete-function-shapes-without-cardinality-admission
  (let [check #(invoke 'p15-s23-runtime-function-valid? %1 %2)
        params (mapv #(symbol (str "p" %)) (range 65537))
        value {:params params :arity 65537 :instructions [literal]}]
    (is (true? (check value 65537)))
    (is (false? (check value 65536)))
    (is (false? (check (update value :params assoc 65536 'p0) 65537)))
    (is (false? (check (update value :params assoc 65536 :bad) 65537)))
    (is (false? (check (assoc value :instructions []) 65537)))
    (let [value {:params [] :instructions (vec (repeat 65537 literal))}]
      (is (true? (check value 0)))
      (is (false? (check (update value :instructions assoc 65536 nil) 0))))
    (let [value {:params [] :instructions [literal]
                 :recur-target (apply str (repeat 257 "t"))}]
      (is (true? (check value 0)))
      (is (false? (check (assoc value :recur-target "") 0)))
      (is (false? (check (assoc value :recur-target :invalid) 0))))))

(deftest complete-loop-shapes-preserve-runtime-target-containment
  (let [check #(invoke 'p15-s23-runtime-loop-valid? %1 %2)
        value {:op :loop :target "loop" :bindings (vec (repeat 65537 binding-ir))
               :body [literal]}]
    (is (true? (check value [])))
    (doseq [bad [(assoc binding-ir :name :bad) (assoc binding-ir :expr nil)
                 (assoc binding-ir :extra true) nil]]
      (is (false? (check (update value :bindings assoc 65536 bad) []))))
    (is (false? (check value [{:id "loop"}])))
    (is (false? (check value (vec (repeat 256 {:id "other"})))))
    (is (false? (check (assoc value :body []) [])))
    (let [value {:op :loop :target "loop" :bindings []
                 :body (vec (repeat 65537 literal))}]
      (is (true? (check value [])))
      (is (false? (check (update value :body assoc 65536 nil) []))))))

(deftest complete-recur-shapes-preserve-target-and-arity
  (let [check #(invoke 'p15-s23-runtime-recur-valid? %1 %2)
        value {:op :recur :target "loop" :args (vec (repeat 65537 literal))}
        targets [{:id "loop" :names (vec (repeat 65537 'p))}]]
    (is (true? (check value targets)))
    (is (false? (check (update value :args pop) targets)))
    (is (false? (check (assoc value :target "wrong") targets)))
    (is (false? (check value [])))
    (is (false? (check (assoc value :extra true) targets)))
    (is (false? (check (assoc value :args '()) targets)))))
