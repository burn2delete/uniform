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

(defn- scalar-bytes [value]
  (let [stats (atom {:nodes 0 :maximum-depth 0 :maximum-width 0
                     :scalar-bytes 0 :maximum-scalar-bytes 0
                     :maximum-integer-bits 0})]
    (with-bindings {(ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-max-container-width)
                    bootstrap/sh03-reader-canonical-maximum-width
                    (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-max-scalar-bytes)
                    bootstrap/sh03-reader-canonical-maximum-scalar-bytes}
      ((ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-canonical-form*)
       "/declared/scalar-policy.gravity" stats 0 value))
    (:scalar-bytes @stats)))

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

(deftest canonical-scalar-budget-is-exact-and-refuses-minus-one
  (let [limit (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-max-total-scalar-bytes)
        row {:a "value"}]
    (with-bindings {limit 12}
      (is (= 12 (scalar-bytes [row row]))))
    ;; Isolate the counter from the generic diagnostic hasher: a deliberately
    ;; tiny test allowance also rejects hashing that diagnostic's own envelope.
    (let [failure
          (with-redefs-fn
            {(ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-host-fail!)
             (fn [_ _ missing subject]
               (throw (ex-info "Counter refusal" {:missing-fact missing :subject subject})))}
            #(with-bindings {limit 11}
               (try (scalar-bytes [row row]) nil
                    (catch clojure.lang.ExceptionInfo error (ex-data error)))))]
      (is (= :maximum-total-scalar-bytes (:missing-fact failure)))
      (is (= 12 (get-in failure [:subject :observed-total-scalar-bytes]))))))

(deftest compiler-row-scalar-class-covers-real-reader-shapes
  (let [source (str "; trivia\n"
                    "[nil true false 42 0xff 2/3 1/0 1.25e2 \"a\\nb\" \\newline :a/b x/y]\n"
                    "{:a [1 2] :b #{3 4}} '(x) @x ^:private x `(~x ~@ys)\n"
                    "#uuid \"550e8400-e29b-41d4-a716-446655440000\"\n"
                    "#inst \"2024-01-02T03:04:05Z\"\n"
                    (apply str (repeat 257 "9")) "\n")
        result ((census-function 'execute) 'sh03-read-source-unit
                ((census-function 'input) source true))
        rows (into {} (map (juxt :form-id identity) (:semantic-value-table result)))]
    (is (= :accepted (:status result)))
    (doseq [token (:token-stream result)]
      (is (<= (scalar-bytes token) 640)))
    (doseq [form (:form-tree result)]
      ;; The worksheet accounts child and root identifier edges separately.
      (is (<= (+ (scalar-bytes (assoc form :children []))
                 (scalar-bytes (get rows (:form-id form))))
              (if (contains? rows (:form-id form)) 1947 1065))))
    (is (<= (+ 1048576 (* 640 524288)) 536870912))
    (is (<= (+ 1048576 (* 2000 262144)) 536870912))))
