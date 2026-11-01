(ns gravity.syntax.reader-revision-test
  "Compiler-free host controls for the exact pure Syntax revision predicates."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]))

(defn- with-predicates [check]
  (let [names '[c3-exact-keys? c3-nonnegative-integer? c3-lowercase-hex?
                c3-sha256-id? c3-reader-source-revision? c3-reader-binding?
                c3-reader-binding-revision-match?]
        forms (with-open [r (java.io.PushbackReader.
                            (io/reader "bootstrap/gravity/src/gravity/bootstrap/syntax.gravity"))]
                (binding [*read-eval* false]
                  (into {} (for [form (take-while #(not= ::end %)
                                                (repeatedly #(read {:eof ::end} r)))
                                 :when (and (seq? form) (= 'defn (first form)))]
                             [(second form) form]))))
        space (create-ns (gensym "syntax-revision-control-"))]
    (try
      (binding [*ns* space]
        (clojure.core/refer 'clojure.core)
        (doseq [name names]
          (assert (contains? forms name))
          (eval (get forms name))))
      (check (fn [name & args] (apply (ns-resolve space name) args)))
      (finally (remove-ns (ns-name space))))))

(defn- digest [character] (str "sha256:" (apply str (repeat 64 character))))

(defn- revision [byte-count]
  (merge (zipmap [:revision-id :source-content-hash :plan-semantic-hash
                  :functions-semantic-hash :entry-semantic-hash
                  :verifier-semantic-hash :reader-result-id :c2-artifact-id
                  :authenticated-envelope-id :semantic-binding-id]
                 (repeat (digest \a)))
         {:artifact :gravity/sh03-reader-source-revision :schema-version 1
          :owner :sh03-reader :source-language :gravity
          :logical-source-path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"
          :source-byte-count byte-count :entry-function 'sh03-read-source-unit
          :verifier-function 'sh03-verify-reader-result}))

(defn- reader-binding []
  (merge (zipmap [:semantic-source-id :semantic-binding-id :source-bytes-hash
                  :token-stream-id :form-tree-id :syntax-seed-stream-id
                  :literal-records-id :deferment-records-id :extension-records-id
                  :reader-result-id :c2-artifact-id :authenticated-envelope-id
                  :source-revision-id]
                 (repeat (digest \a)))
         {:artifact :gravity/sh04-reader-semantic-binding :schema-version 1
          :reader-options {}}))

(deftest revision-length-is-nonnegative-integer-without-byte-allowance
  (with-predicates
    (fn [call]
      ;; Declared byte lengths only: no large source or Reader is constructed.
      (doseq [n [0 1048576 1048577 Long/MAX_VALUE]]
        (is (true? (call 'c3-reader-source-revision? (revision n))) (str n)))
      (doseq [n [-1 nil false "1048577" 1048577.0 3/2]]
        (is (false? (call 'c3-reader-source-revision? (revision n))) (pr-str n)))))
  (is (nil? (find-ns 'gravity.bootstrap))))

(deftest large-revision-retains-exact-shape-owner-and-function-checks
  (with-predicates
    (fn [call]
      (let [valid (revision 1048577)]
        (doseq [[key value] [[:artifact :other] [:schema-version 2] [:owner :other]
                             [:source-language :clojure] [:logical-source-path ""]
                             [:entry-function 'other] [:verifier-function 'other]]]
          (is (false? (call 'c3-reader-source-revision? (assoc valid key value))) (str key)))
        (doseq [key (keys valid)]
          (is (false? (call 'c3-reader-source-revision? (dissoc valid key))) (str key)))
        (is (false? (call 'c3-reader-source-revision? (assoc valid :extra true))))
        (doseq [key [:revision-id :source-content-hash :plan-semantic-hash
                     :functions-semantic-hash :entry-semantic-hash :verifier-semantic-hash
                     :reader-result-id :c2-artifact-id :authenticated-envelope-id
                     :semantic-binding-id]]
          (is (false? (call 'c3-reader-source-revision? (assoc valid key "not-a-digest")))
              (str key)))))))

(deftest large-revision-still-requires-exact-reader-binding-identities
  (with-predicates
    (fn [call]
      (let [valid (revision 1048577) reader (reader-binding)]
        (is (true? (call 'c3-reader-binding-revision-match? reader valid)))
        (doseq [[binding-key revision-key]
                [[:source-revision-id :revision-id] [:reader-result-id :reader-result-id]
                 [:c2-artifact-id :c2-artifact-id]
                 [:authenticated-envelope-id :authenticated-envelope-id]
                 [:semantic-binding-id :semantic-binding-id]]]
          (testing (str binding-key)
            (is (false? (call 'c3-reader-binding-revision-match?
                              (assoc reader binding-key (digest \b)) valid)))
            (is (false? (call 'c3-reader-binding-revision-match?
                              reader (assoc valid revision-key (digest \b)))))))))))
