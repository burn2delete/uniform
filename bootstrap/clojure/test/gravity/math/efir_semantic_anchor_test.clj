(ns gravity.math.efir-semantic-anchor-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(def ^:private source-path "math/efir-semantic-anchor.gravity")

(def ^:private graph
  (let [span (str source-path ":1:1")]
    (assoc (first bootstrap/efir-default-graphs)
           :source-span span
           :source-anchors [{:span span :origin :source}])))

(defn- emit-graph [candidate]
  (bootstrap/efir-source-artifact
   source-path
   (pr-str (list 'ns 'math.efir-semantic-anchor
                 '(:profile :native) '(:target :jvm)
                 '(:effects #{}) '(:capabilities #{}) '(:safety :safe)
                 (list :metadata {:math {:efir {:efir-graphs [candidate]}}})))))

(deftest efir-emits-either-kind-of-semantic-reference
  (doseq [anchor [(:semantic-anchor graph)
                  {:mir-ops ["mir-op-sigmoid-call"]}]]
    (testing (pr-str anchor)
      (let [candidate (assoc graph :semantic-anchor anchor)
            artifact (emit-graph candidate)]
        (is (= [candidate] (:efir-graph artifact)))
        (is (= [(select-keys candidate [:graph-id :source-anchors :semantic-anchor])]
               (:source-anchor-map artifact)))
        (is (true? (get-in artifact [:capability-based-proof :efir-graphs-anchored?])))))))

(deftest efir-rejects-missing-semantic-or-source-anchors
  (doseq [[label candidate]
          [["both reference vectors are empty"
            (assoc graph :semantic-anchor {:typed-core [] :mir-ops []})]
           ["an unrelated key is present"
            (assoc graph :semantic-anchor {:unrelated [:typed/math-sigmoid-call]})]
           ["a reference vector contains only nil"
            (assoc graph :semantic-anchor {:typed-core [nil] :mir-ops []})]
           ["the reference field is scalar"
            (assoc graph :semantic-anchor {:mir-ops "mir-op-sigmoid-call"})]
           ["a Core reference is present and the source anchor is missing"
            (dissoc graph :source-anchors)]]]
    (testing label
      (let [diagnostic (try
                         (emit-graph candidate)
                         nil
                         (catch clojure.lang.ExceptionInfo error
                           (ex-data error)))]
        (is (= {:id "MATH3-SOURCE"
                :graph-id (:graph-id candidate)
                :source-span (:source-span candidate)}
               (select-keys diagnostic [:id :graph-id :source-span])))
        (is (false? (:efir-graphs-anchored?
                     (bootstrap/efir-capability-proof {:efir-graphs [candidate]}))))))))
