(ns gravity.self-hosting.sh03-canonical-scalar-policy-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh03-complete-construction-census-test]))

;; Audit the unchanged canonical representation, counting each occurrence.
;; This is a lower bound: values, identifiers and descriptors add more bytes.
(defn- key-bytes [value]
  (cond
    (map? value)
    (reduce + 0
            (map (fn [[key child]]
                   (+ (if (keyword? key)
                        (+ (count (name key)) (count (or (namespace key) "")))
                        0)
                      (key-bytes child))) value))
    (coll? value) (reduce + 0 (map key-bytes value))
    :else 0))

(defn- census-function [name]
  (deref (ns-resolve 'gravity.self-hosting.sh03-complete-construction-census-test name)))

(deftest canonical-key-occurrences-exceed-old-compiler-input-budget
  (let [arguments ((census-function 'input)
                   "; trivia\n[a :b 42 nil true \"text\" {:x #{1 2}}]\n" true)
        result ((census-function 'execute) 'sh03-read-source-unit arguments)
        tokens (:token-stream result)
        forms (:form-tree result)]
    (is (= :accepted (:status result)))
    (is (seq tokens))
    (is (seq forms))
    (is (every? #(<= 247 (key-bytes %)) tokens))
    (is (every? #(<= 410 (key-bytes %)) forms))
    ;; Authenticated count-only real-source receipts, not host-reader estimates.
    (doseq [[tokens forms] [[417156 208067] [472738 235698]]]
      (is (< 67108864 (* 247 tokens)))
      (is (< 67108864 (* 410 forms))))))

(deftest canonical-budget-counts-physical-keys-despite-host-aliasing
  (let [row {:a "value"}
        operation (ns-resolve 'gravity.bootstrap
                              'p15-s23-c6c10-canonical-form*)
        stats (atom {:nodes 0 :maximum-depth 0 :maximum-width 0
                     :scalar-bytes 0 :maximum-scalar-bytes 0
                     :maximum-integer-bits 0})]
    (operation "/declared/scalar-policy.gravity" stats 0 [row row])
    (is (= 12 (:scalar-bytes @stats)))
    (is (= 2 (key-bytes [row row])))))
