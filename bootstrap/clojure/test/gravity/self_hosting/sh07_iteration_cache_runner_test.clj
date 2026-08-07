(ns gravity.self-hosting.sh07-iteration-cache-runner-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh07-iteration-cache-runner :as runner]))

(deftest iteration-cache-reuses-equal-source-and-identical-artifacts
  (let [sh06-calls (atom 0)
        core-calls (atom 0)
        verification-calls (atom 0)]
    (with-redefs
      [bootstrap/sh06-resolution-source-artifact
       (fn [path source]
         (swap! sh06-calls inc)
         {:artifact-id (str path ":" source)
          :provenance {:source-path path}
          :gravity-resolution-boundary
          {:resolved-analysis {:semantic-projection-id source}}})
       bootstrap/sh07-core-from-resolution-artifact
       (fn [resolution]
         (swap! core-calls inc)
         {:artifact-id (:artifact-id resolution)})
       bootstrap/sh07-core-artifact-verification
       (fn [artifact]
         (swap! verification-calls inc)
         {:verified (:artifact-id artifact)})]
      (let [result
            (runner/with-iteration-cache
             {:maximum-entries 2}
             (fn []
               (let [left (bootstrap/sh06-resolution-source-artifact "a" "x")
                     right (bootstrap/sh06-resolution-source-artifact "a" "x")
                     core-left
                     (bootstrap/sh07-core-from-resolution-artifact left)
                     core-right
                     (bootstrap/sh07-core-from-resolution-artifact right)]
                 [(bootstrap/sh07-core-artifact-verification core-left)
                  (bootstrap/sh07-core-artifact-verification core-right)])))]
        (is (= 1 @sh06-calls @core-calls @verification-calls))
        (is (= {:sh06-hits 1 :sh06-misses 1
                :core-hits 1 :core-misses 1
                :verification-hits 1 :verification-misses 1}
               (:cache result)))
        (is (= :non-authoritative (:authority result)))
        (is (false? (:authoritative? result)))
        (is (false? (:cache-authoritative? result)))
        (is (true? (:fresh-authoritative-run-required? result)))))))

(deftest iteration-cache-is-content-keyed-and-bounded
  (let [calls (atom 0)]
    (with-redefs
      [bootstrap/sh06-resolution-source-artifact
       (fn [path source]
         (swap! calls inc)
         [path source])]
      (let [result
            (runner/with-iteration-cache
             {:maximum-entries 1}
             (fn []
               [(bootstrap/sh06-resolution-source-artifact "a" "one")
                (bootstrap/sh06-resolution-source-artifact "a" "two")
                (bootstrap/sh06-resolution-source-artifact "a" "one")]))]
        (is (= 3 @calls))
        (is (= 3 (get-in result [:cache :sh06-misses])))
        (is (zero? (get-in result [:cache :sh06-hits])))))))

(deftest iteration-cache-serializes-concurrent-misses
  (let [calls (atom 0)]
    (with-redefs
      [bootstrap/sh06-resolution-source-artifact
       (fn [path source]
         (swap! calls inc)
         (Thread/sleep 25)
         [path source])]
      (let [result
            (runner/with-iteration-cache
             {:maximum-entries 1}
             (fn []
               (mapv deref
                     (repeatedly
                      8
                      #(future
                         (bootstrap/sh06-resolution-source-artifact
                          "same" "content"))))))]
        (is (= 1 @calls))
        (is (= 1 (get-in result [:cache :sh06-misses])))
        (is (= 7 (get-in result [:cache :sh06-hits])))
        (is (apply = (:value result)))))))

(deftest argument-selection-is-explicit-and-owned
  (testing "repeatable namespaces and cache bound"
    (is (= {:namespaces '[gravity.diagnostics-test gravity.cli-test]
            :maximum-entries 2}
           (runner/parse-arguments
            ["--namespace" "gravity.diagnostics-test"
             "--max-cache-entries" "2"
             "--namespace" "gravity.cli-test"]))))
  (testing "zero work and invalid bounds fail closed"
    (is (= "SH07-ITERATION-CACHE-SELECTION"
           (:id (ex-data (try (runner/parse-arguments [])
                              (catch clojure.lang.ExceptionInfo error
                                error))))))
    (is (= "SH07-ITERATION-CACHE-ARGUMENT"
           (:id (ex-data
                 (try
                   (runner/parse-arguments
                    ["--namespace" "gravity.cli-test"
                     "--max-cache-entries" "0"])
                   (catch clojure.lang.ExceptionInfo error error)))))))
  (testing "unowned namespaces are rejected by the coordinator"
    (is (= "SH01-TEST-NAMESPACE"
           (:id (ex-data
                 (try
                   (runner/parse-arguments
                   ["--namespace" "gravity.not-owned-test"])
                   (catch clojure.lang.ExceptionInfo error error)))))))
  (testing "duplicate namespace work is rejected"
    (is (= "SH07-ITERATION-CACHE-DUPLICATE-NAMESPACE"
           (:id (ex-data
                 (try
                   (runner/parse-arguments
                    ["--namespace" "gravity.cli-test"
                     "--namespace" "gravity.cli-test"])
                   (catch clojure.lang.ExceptionInfo error error))))))))
