(ns c11-source-api-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [clojure.java.io :as io]
            [gravity.c11-mir.source-contract :as contract]
            [gravity.c11-mir.source-interface :as interface]
            [gravity.source-interface-header :as header]))

(def component-root "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/")
(def pure-functions
  '#{p15-s23-c11-mir-contract p15-s23-c11-mir-versioned-record
     p15-s23-c11-mir-selected-builder-function p15-s23-c11-mir-selected-verifier-function
     p15-s23-c11-mir-contract-binding p15-s23-c11-mir-source-rule-record})
(defn forms [path]
  (with-open [reader (java.io.PushbackReader. (io/reader path))]
    (binding [*read-eval* false]
      (loop [out []]
        (let [form (read {:eof ::end} reader)]
          (if (= ::end form) out (recur (conj out form))))))))
(def owner (create-ns (gensym "c11-pure-owner-")))
(binding [*ns* owner]
  (clojure.core/refer 'clojure.core)
  (alias 'c11-source-contract 'gravity.c11-mir.source-contract)
  ;; Synthetic legacy constants; these tests do not compile or authenticate source.
  (doseq [name '[p15-s23-c11-mir-source-byte-count
                p15-s23-c11-mir-expected-source-content-hash
                p15-s23-c11-mir-expected-plan-semantic-hash
                p15-s23-c11-mir-expected-functions-semantic-hash
                p15-s23-c11-mir-expected-builder-semantic-hash
                p15-s23-c11-mir-expected-verifier-semantic-hash
                p15-s23-c11-mir-required-functions
                p15-s23-c11-mir-builder-function p15-s23-c11-mir-verifier-function]]
    (intern owner name name))
  (doseq [file ["00732-p15-s23-c11-mir-owned-upstream-diagnostic.clj"
                "00741-p15-s23-c11-mir-map-blocks.clj"]
          form (forms (str component-root file))
          :when (and (seq? form) (= 'defn (first form)) (pure-functions (second form)))]
    (eval form)))
(defn call [name & args] (apply (ns-resolve owner name) args))

(deftest fixed-source-contracts-and-legacy-rule-preservation
  (doseq [[version expected] [[2 contract/v2-source-contract] [3 contract/flat-bool-source-contract]]]
    (is (= expected (call 'p15-s23-c11-mir-contract version)))
    (let [binding (call 'p15-s23-c11-mir-contract-binding version)
          rule (call 'p15-s23-c11-mir-source-rule-record binding)]
      (is (= (:source-content-hash expected) (:source-content-hash binding) (:source-content-hash rule)))
      (is (= (:required-functions expected) (:function-shapes binding) (:function-shapes rule)))
      (is (= {:interface :gravity/c11-mir :version version} (:source-interface rule)))
      (is (true? (:clojure-seed-boundary? rule)))
      (is (false? (:self-hosted? rule)))
      (is (not= rule (call 'p15-s23-c11-mir-source-rule-record
                           (assoc binding :source-content-hash "synthetic-changed-source"))))
      (when (= 3 version)
        (is (= :closed-flat-bool-literal-sequence (:semantic-family binding) (:semantic-family rule)))
        (is (= (:builder-function expected) (:builder-function rule)))
        (is (= (:verifier-function expected) (:verifier-function rule))))))
  (let [binding (call 'p15-s23-c11-mir-contract-binding 1)
        rule (call 'p15-s23-c11-mir-source-rule-record binding)]
    (is (not (contains? binding :interface-version)))
    (is (not (contains? rule :source-interface)))
    (is (not (contains? rule :semantic-family)))
    (is (= 'p15-s23-c11-mir-builder-function (:builder-function rule)))
    (is (= 'p15-s23-c11-mir-verifier-function (:verifier-function rule)))
    (is (= :clojure-stage0-seed (:compiled-by rule)))))

(deftest closed-version-and-artifact-discriminants
  (doseq [bad [nil 0 4 (int 3) 3.0 "3"]]
    (is (thrown? clojure.lang.ExceptionInfo (call 'p15-s23-c11-mir-contract bad))))
  (doseq [version [2 3]]
    (is (= version (interface/interface-version! {:interface :gravity/c11-mir :version version}))))
  (doseq [bad [{} nil {:interface :gravity/c6-c10 :version 3}
               {:interface :gravity/c11-mir :version (int 3)}
               {:interface :gravity/c11-mir :version 1}
               {:interface :gravity/c11-mir :version 3 :pins {}}
               (with-meta {:interface :gravity/c11-mir :version 3} {:caller true})]]
    (is (thrown? clojure.lang.ExceptionInfo (interface/interface-version! bad))))
  (let [artifact {:kind :gravity/p15-s23-c11-authenticated-mir-artifact :schema-version 3}]
    (is (nil? (interface/require-matching-artifact-version! artifact 3)))
    (is (thrown? clojure.lang.ExceptionInfo (interface/require-matching-artifact-version! artifact 2))))
  ;; Generic expression/header owners keep their existing explicit-v2 contract.
  (is (thrown? clojure.lang.ExceptionInfo
        (header/interface-version! {:interface :gravity/c11-mir :version 3}))))

(deftest public-v2-selection-does-not-admit-the-data-only-v3-successor
  (is (= 2 (interface/public-interface-version! {:interface :gravity/c11-mir :version 2})))
  (is (= 3 (interface/interface-version! {:interface :gravity/c11-mir :version 3})))
  (is (thrown? clojure.lang.ExceptionInfo
               (interface/public-interface-version! {:interface :gravity/c11-mir :version 3}))))

(let [result (run-tests 'c11-source-api-test)]
  (assert (nil? (find-ns 'gravity.bootstrap)))
  (println {:scope :pure-source-selection-only :authentication false :bootstrap-loaded false})
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
