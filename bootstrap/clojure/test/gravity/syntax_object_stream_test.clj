(ns gravity.syntax-object-stream-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.syntax-object-stream :as syntax-object-stream]))

(deftest projection-preserves-exact-shape-order-and-references
  (let [form (Object.)
        span (Object.)
        metadata (Object.)
        reader-origin (Object.)
        generated-origin (Object.)
        records [{:form form
                  :span span
                  :metadata metadata
                  :reader-origin reader-origin
                  :generated-origin generated-origin}
                 {:form :second}]
        result (syntax-object-stream/syntax-object-stream
                "ignored.gravity" records
                {:module 'example.module :profile :hosted})]
    (is (= [{:syntax-id "stage0-syntax-0"
             :form form
             :span span
             :origin :source
             :reader-origin reader-origin
             :generated-origin generated-origin
             :namespace 'example.module
             :phase :read
             :profile :hosted
             :hygiene []
             :metadata metadata}
            {:syntax-id "stage0-syntax-1"
             :form :second
             :span nil
             :origin :source
             :reader-origin nil
             :generated-origin nil
             :namespace 'example.module
             :phase :read
             :profile :hosted
             :hygiene []
             :metadata nil}]
           result))
    (is (= ["stage0-syntax-0" "stage0-syntax-1"]
           (mapv :syntax-id result)))
    (is (identical? form (:form (first result))))
    (is (identical? span (:span (first result))))
    (is (identical? metadata (:metadata (first result))))
    (is (identical? reader-origin (:reader-origin (first result))))
    (is (identical? generated-origin (:generated-origin (first result))))))

(deftest nil-context-and-form-id-presence-are-preserved-exactly
  (let [result (syntax-object-stream/syntax-object-stream
                nil
                [{:form :absent}
                 {:form :present-nil :form-id nil}
                 {:form :present-value :form-id "form-2"}])]
    (is (= [nil nil nil] (mapv :namespace result)))
    (is (= [nil nil nil] (mapv :profile result)))
    (is (every? #(contains? % :namespace) result))
    (is (every? #(contains? % :profile) result))
    (is (not (contains? (nth result 0) :form-id)))
    (is (contains? (nth result 1) :form-id))
    (is (nil? (:form-id (nth result 1))))
    (is (= "form-2" (:form-id (nth result 2))))))

(deftest source-path-is-compatible-but-does-not-affect-projection
  (let [records [{:form 'value :span {:source "record.gravity"}}]
        nil-path (syntax-object-stream/syntax-object-stream nil records)
        string-path (syntax-object-stream/syntax-object-stream
                     "different.gravity" records)
        odd-path (syntax-object-stream/syntax-object-stream (Object.) records)]
    (is (= nil-path string-path odd-path))))

(deftest collection-boundaries-retain-mapv-range-behavior
  (testing "nil and empty inputs produce an eager empty vector"
    (is (= [] (syntax-object-stream/syntax-object-stream "source" nil)))
    (is (= [] (syntax-object-stream/syntax-object-stream "source" [])))
    (is (vector? (syntax-object-stream/syntax-object-stream "source" '()))))
  (testing "lists and lazy inputs retain encounter order and finite truncation"
    (is (= ["stage0-syntax-0" "stage0-syntax-1"]
           (mapv :syntax-id
                 (syntax-object-stream/syntax-object-stream
                  "source" (list {:form :a} {:form :b})))))
    (is (= [:a :b :c]
           (mapv :form
                 (syntax-object-stream/syntax-object-stream
                  "source" (take 3 (map #(hash-map :form %) [:a :b :c :d])))))))
  (testing "non-map sequential entries retain destructuring's nil field behavior"
    (let [result (syntax-object-stream/syntax-object-stream
                  "source" [nil []])]
      (is (= [nil nil] (mapv :form result)))
      (is (every? #(not (contains? % :form-id)) result)))))

(deftest syntax-object-stream-realizes-record-input-eagerly
  (let [observed (atom [])
        records (map (fn [form]
                       (swap! observed conj form)
                       {:form form})
                     [:first :second :third])
        result (syntax-object-stream/syntax-object-stream "source" records)]
    (is (vector? result))
    (is (= [:first :second :third] @observed))
    (is (= [:first :second :third] (mapv :form result)))))

(deftest syntax-object-stream-contract-is-narrow-and-nonauthoritative
  (let [contract-var
        (get (ns-interns 'gravity.syntax-object-stream) 'namespace-contract)
        contract (var-get contract-var)]
    (is (true? (:private (meta contract-var))))
    (is (= 'gravity.syntax-object-stream (:namespace contract)))
    (is (= :hosted-reader-to-syntax-record-projection
           (:contract-boundary contract)))
    (is (= #{'syntax-object-stream} (set (keys (:public-api contract)))))
    (is (= '([source-path form-records]
             [source-path form-records module-context])
           (:arglists (meta #'syntax-object-stream/syntax-object-stream))))
    (is (= ['clojure.core]
           (get-in contract [:dependency-direction :requires])))
    (is (= ['gravity.bootstrap 'gravity.diagnostics]
           (get-in contract [:dependency-direction :forbids])))
    (is (= #{:hosted-reader-to-syntax-record-projection}
           (set (get-in contract [:ownership :owns]))))
    (doseq [nonclaim [:canonical-c2-reader-authority
                      :canonical-c3-syntax-object-authority
                      :source-reading
                      :source-authentication
                      :macro-expansion
                      :hygiene-semantics]]
      (is (some #{nonclaim} (get-in contract [:ownership :does-not-own]))
          nonclaim))
    (is (false? (:canonical-c2-authority? contract)))
    (is (false? (:canonical-c3-authority? contract)))
    (is (empty? (ns-aliases 'gravity.syntax-object-stream)))
    (is (= #{'syntax-object-stream}
           (set (keys (ns-publics 'gravity.syntax-object-stream)))))
    (is (nil? (find-ns 'gravity.bootstrap)))
    (is (true? (:bootstrap-hosted? contract)))
    (is (true? (:clojure-seed-boundary? contract)))
    (is (false? (:self-hosted? contract)))))
