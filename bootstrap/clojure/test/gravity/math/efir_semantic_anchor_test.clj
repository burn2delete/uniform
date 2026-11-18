(ns gravity.math.efir-semantic-anchor-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(def ^:private source-path "math/efir-semantic-anchor.gravity")
(def ^:private source-span (str source-path ":1:1"))

(def ^:private graph
  (assoc (first bootstrap/efir-default-graphs)
         :source-anchors [{:span source-span :origin :source}]))

(defn- emit-graph [candidate]
  (bootstrap/efir-source-artifact
   source-path
   (pr-str (list 'ns 'math.efir-semantic-anchor
                 '(:profile :native) '(:target :jvm)
                 '(:effects #{}) '(:capabilities #{}) '(:safety :safe)
                 (list :metadata {:math {:efir {:efir-graphs [candidate]}}})))))

(deftest efir-emits-either-kind-of-semantic-reference
  (doseq [candidate [graph
                     (assoc graph :semantic-anchor {:mir-ops ["mir-op-sigmoid-call"]})
                     (-> graph
                         (assoc :semantic-anchor {:mir-ops [:op/sigmoid-call]})
                         (assoc-in [:source-anchors 0 :origin] :generated))]]
    (testing (pr-str (:semantic-anchor candidate))
      (let [artifact (emit-graph candidate)]
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
           ["the MIR reference is an empty string"
            (assoc graph :semantic-anchor {:mir-ops [""]})]
           ["the Core reference is a whitespace-only string"
            (assoc graph :semantic-anchor {:typed-core [" "]})]
           ["the reference vector contains unsupported values"
            (assoc graph :semantic-anchor {:mir-ops [false 42 {:op-id "mir-op-sigmoid-call"}]})]
           ["the reference field is scalar"
            (assoc graph :semantic-anchor {:mir-ops "mir-op-sigmoid-call"})]
           ["a generated origin is present and the semantic anchor is missing"
            (-> graph
                (assoc-in [:source-anchors 0 :origin] :generated)
                (dissoc :semantic-anchor))]
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
                :source-span (if (:source-anchors candidate)
                               source-span
                               {:source source-path})}
               (select-keys diagnostic [:id :graph-id :source-span])))
        (is (false? (:efir-graphs-anchored?
                     (bootstrap/efir-capability-proof {:efir-graphs [candidate]}))))))))
