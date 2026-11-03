(ns gravity.test-scope-runner-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.set :as set]
            [gravity.test-scope-runner :as scope]))

(defn- throws-message?
  [pattern thunk]
  (try
    (thunk)
    false
    (catch clojure.lang.ExceptionInfo ex
      (boolean (re-find pattern (.getMessage ex))))))

(deftest project-scopes-are-fixed-and-disjoint
  (let [language (set (scope/scope-namespaces :language))
        compiler (set (scope/scope-namespaces :compiler))
        bootstrap (set (scope/scope-namespaces :bootstrap))]
    (is (seq language))
    (is (seq compiler))
    (is (seq bootstrap))
    (is (empty? (set/intersection language bootstrap)))
    (is (every? symbol? (concat language compiler bootstrap)))))

(deftest empty-development-invocation-selects-only-its-scope
  (let [arguments (scope/normalize-development-args :language [])]
    (is (= (set (scope/scope-namespaces :language))
           (set (map symbol (map second (partition 2 arguments))))))
    (is (not-any? #{"gravity.bootstrap-test"} arguments))))

(deftest development-selection-rejects-bootstrap-namespace
  (is (throws-message?
       #"outside the requested project scope"
       #(scope/normalize-development-args
         :language
         ["--namespace" "gravity.bootstrap-test"]))))

(deftest development-selection-requires-explicit-namespace
  (is (throws-message?
       #"requires an explicit namespace"
       #(scope/normalize-development-args :compiler ["--exact" "some-test"]))))

(deftest bootstrap-selection-rejects-development-only-options
  (is (throws-message?
       #"Unsupported bootstrap validation option"
       #(scope/normalize-bootstrap-args ["--exact" "some-test"]))))
