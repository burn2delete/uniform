(ns c6-contract-data-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [clojure.java.io :as io]
            [gravity.c6-c10.source-contract :as contract]))

(def root "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/")
(defn forms [path]
  (with-open [r (java.io.PushbackReader. (io/reader path))]
    (binding [*read-eval* false]
      (loop [out []]
        (let [x (read {:eof ::end} r)] (if (= ::end x) out (recur (conj out x))))))))
(def legacy-names
  '#{p15-s23-c6c10-builder-function p15-s23-c6c10-verifier-function
     p15-s23-c6c10-source-byte-count p15-s23-c6c10-expected-source-content-hash
     p15-s23-c6c10-expected-plan-semantic-hash p15-s23-c6c10-expected-functions-semantic-hash
     p15-s23-c6c10-expected-builder-semantic-hash p15-s23-c6c10-expected-verifier-semantic-hash
     p15-s23-c6c10-required-functions})
(def owner (create-ns (gensym "c6-contract-data-owner-")))
(binding [*ns* owner]
  (clojure.core/refer 'clojure.core)
  (alias 'c6-source-contract 'gravity.c6-c10.source-contract)
  ;; Diagnostic transport is an oracle here; no compiler/owner is loaded.
  (intern owner 'p15-s23-c6c10-host-fail!
          (fn [rule path fact data]
            (throw (ex-info "Synthetic diagnostic transport"
                            {:rule rule :path path :missing-fact fact :data data}))))
  (doseq [form (forms (str root "00701-p15-s23-c6c10-source-relative-path.clj"))
          :when (and (seq? form) (= 'def (first form)) (legacy-names (second form)))]
    (eval form))
  (doseq [form (forms (str root "00701a-p15-s23-c6c10-source-program-contract.clj"))
          :when (and (seq? form) (= 'defn (first form))
                     (= 'p15-s23-c6c10-source-program-contract (second form)))]
    (eval form)))
(defn select-contract [version]
  ((ns-resolve owner 'p15-s23-c6c10-source-program-contract) version))

(deftest immutable-built-in-contract-selection
  (is (identical? contract/current-compiler-program (select-contract 2)))
  (is (identical? contract/flat-bool-compiler-program (select-contract 3)))
  (is (= :closed-flat-bool-literal-sequence (:semantic-family (select-contract 3))))
  (is (= "sha256:7d14fef9a3ca0f47fd7ec301d96cd6c0b57c4c6a3f91a0839c673e8c702a6396"
         (:source-content-hash (select-contract 3))))
  (let [changed (assoc (select-contract 3) :source-content-hash "synthetic-substitution")]
    (is (not= changed (select-contract 3)))
    (is (identical? contract/flat-bool-compiler-program (select-contract 3))))
  (doseq [record [(select-contract 2) (select-contract 3)]]
    (is (not (contains? record :plan)))
    (is (not (contains? record :status)))
    (is (= {'c6-c10-build-checked-core-template {:arity 1 :params ['input]}
            'c6-c10-verify-checked-core-template {:arity 3 :params ['input 'template 'requests]}}
           (:required-functions record)))))

(deftest legacy-contract-is-derived-from-unchanged-legacy-constants
  (let [record (select-contract 1)]
    (is (= 1 (:version record)))
    (is (= 141562 (:source-byte-count record)))
    (is (= "sha256:0299511c26c8b2a191309a4a4358528de397c88551ed98a2aed03d172067b2a5"
           (:source-content-hash record)))
    (is (= 139 (:function-count record)))
    (is (= 64 (:chunk-size record)))
    (is (not (contains? record :semantic-family)))))

(deftest invalid-version-data-rejects-without-foreign-callbacks
  (let [calls (atom 0)
        foreign (proxy [Object clojure.lang.IMeta] []
                  (meta [] (swap! calls inc) {})
                  (hashCode [] (swap! calls inc) 0)
                  (equals [_] (swap! calls inc) true)
                  (toString [] (swap! calls inc) "foreign"))]
    (doseq [value [nil 0 4 (int 2) 2N 2.0 "2" :v2 {:version 2 :pins {}} foreign]]
      (let [data (try (select-contract value) nil
                      (catch clojure.lang.ExceptionInfo error (ex-data error)))]
        (is (= "C6-VERIFY" (:rule data)))
        (is (= :explicit-c6-source-contract-version (:missing-fact data)))
        (is (= {} (:data data)))))
    (is (zero? @calls))))

(let [result (run-tests 'c6-contract-data-test)]
  (assert (nil? (find-ns 'gravity.bootstrap)))
  (println {:scope :fixed-contract-data-only :diagnostic-transport :synthetic
            :bootstrap-loaded false :source-authentication false})
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
