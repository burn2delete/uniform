(ns gravity.c2-reader-scalar-spans-test
  "Scalar projection from retained Reader slices; no authentication authority."
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c2-reader-product-projection :as projection]))

(defn- slice [start end scalar-start scalar-end]
  {:artifact :gravity/source-slice :schema-version 1 :encoding :utf-8
   :source-content-id {:digest-ref 0} :byte-start start :byte-end end
   :scalar-start scalar-start :scalar-end scalar-end})
(def ^:private products
  {:root-form-ids [:f0 :f1]
   :parsed-records [{:form-id :f0 :form '["unicode"]} {:form-id :f1 :form '(quote x)}]
   :form-tree [{:form-id :f0 :kind :metadata-wrapper :raw "unicode" :metadata {:m true}
                :span {:byte-start 0 :byte-end 8 :start {:char 0} :end {:char 5}}}
               {:form-id :f1 :kind :abbreviation :raw "'x" :abbrev :quote
                :span {:byte-start 9 :byte-end 11 :start {:char 6} :end {:char 8}}}]
   :sh03-reader-result
   {:top-level-form-ids ["form/0" "form/4"]
    :form-tree [{:form-id "form/0" :raw (slice 0 8 0 4)}
                {:form-id "form/4" :raw (slice 9 11 5 7)}]}})

(defn- project [input calls]
  (projection/with-operations
    {:syntax-object-stream (fn [source records context]
                             (swap! calls conj [source records context]) records)}
    #(projection/c2-syntax-seed-stream "unicode.gravity" input {:module 'example})))

(deftest reader-source-slices-preserve-scalar-offsets-and-legacy-character-offsets
  (let [calls (atom []) output (project products calls)]
    (is (= 1 (count @calls)))
    (is (= [[:f0 0 4 0 5] [:f1 5 7 6 8]]
           (mapv (fn [seed] [(:form-id seed) (get-in seed [:span :scalar-start])
                             (get-in seed [:span :scalar-end]) (get-in seed [:span :start :char])
                             (get-in seed [:span :end :char])]) output)))
    (is (= {:m true} (:metadata (first output))))
    (is (= [:metadata-wrapper :abbreviation] (mapv #(get-in % [:reader-origin :raw-form-kind]) output)))
    (is (= [0 1] (mapv #(get-in % [:span :form-index]) output)))))

(deftest bmp-source-slice-offsets-remain-exact
  (let [input (-> products
                  (assoc :root-form-ids [:f0] :parsed-records [{:form-id :f0 :form 'x}])
                  (assoc :form-tree [{:form-id :f0 :kind :symbol :raw "x"
                                     :span {:byte-start 0 :byte-end 2 :start {:char 0} :end {:char 1}}}])
                  (assoc :sh03-reader-result {:top-level-form-ids ["form/0"]
                                             :form-tree [{:form-id "form/0" :raw (slice 0 2 0 1)}]}))
        output (project input (atom []))]
    (is (= [0 1] ((juxt :scalar-start :scalar-end) (:span (first output)))))))

(deftest hosted-products-without-retained-reader-result-keep-their-existing-shape
  (let [input (dissoc products :sh03-reader-result) output (project input (atom []))]
    (is (= (mapv #(assoc (:span %) :form-index %2) (:form-tree input) (range))
           (mapv :span output)))
    (is (every? #(not (contains? (:span %) :scalar-start)) output))
    (is (every? #(not (contains? (:span %) :scalar-end)) output))))

(deftest incomplete-or-mismatched-retained-root-slices-stop-before-seed-delegation
  (doseq [input [(assoc products :sh03-reader-result nil)
                 (assoc-in products [:sh03-reader-result :form-tree] :not-a-collection)
                 (assoc-in products [:sh03-reader-result :form-tree] [])
                 (assoc-in products [:sh03-reader-result :top-level-form-ids] ["form/4" "form/0"])
                 (assoc-in products [:sh03-reader-result :top-level-form-ids] ["form/0"])
                 (assoc-in products [:parsed-records 0 :form-id] :other)
                 (update-in products [:sh03-reader-result :form-tree 0 :raw] dissoc :scalar-start)
                 (assoc-in products [:sh03-reader-result :form-tree 0 :raw :scalar-start] -1)
                 (assoc-in products [:sh03-reader-result :form-tree 0 :raw :scalar-end] -1)
                 (assoc-in products [:sh03-reader-result :form-tree 0 :raw :byte-end] 7)]]
    (let [calls (atom []) error (try (project input calls) nil
                                   (catch clojure.lang.ExceptionInfo error (ex-data error)))]
      (is (= :reader-root-scalar-spans (:boundary error)))
      (is (empty? @calls)))))

(deftest replaced-seed-projection-remains-fresh-and-keeps-its-returned-object
  (let [calls (atom 0) result (Object.) sentinel (ex-info "second callback" {})]
    (projection/with-operations
      {:c2-syntax-seed-stream (fn [& _] (if (= 1 (swap! calls inc)) result (throw sentinel)))}
      #(do (is (identical? result (projection/c2-syntax-seed-stream "x" products {})))
           (is (identical? sentinel
                 (try (projection/c2-syntax-seed-stream "x" products {}) nil
                      (catch Throwable error error))))))
    (is (= 2 @calls))))
