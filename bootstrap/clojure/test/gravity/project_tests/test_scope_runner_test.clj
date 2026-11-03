(ns gravity.project-tests.test-scope-runner-test
  (:require [clojure.set :as set]
            [clojure.test :refer [deftest is]]
            [gravity.project-tests.scope :as scope]))

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

(deftest optional-current-source-namespaces-follow-present-files
  (let [specs scope/current-source-test-specs
        present-paths (set (map :path specs))
        present-specs (fn [scope-key & _]
                        (filterv #(and (= scope-key (:scope %))
                                       (contains? present-paths (:path %)))
                                 specs))]
    (with-redefs [scope/current-source-test-specs-for-scope present-specs]
      (let [compiler (set (scope/scope-namespaces :compiler))
            bootstrap (set (scope/scope-namespaces :bootstrap))]
        (is (contains? compiler
                       'gravity.self-hosting.c6-core-lowering-source-execution-test))
        (is (contains? compiler
                       'gravity.self-hosting.c6-core-lowering-source-classifier-test))
        (is (contains? compiler
                       'gravity.self-hosting.canonical-core-node-source-execution-test))
        (is (contains? bootstrap 'gravity.bootstrap.rebuild.component-test))
        (is (contains? bootstrap 'gravity.bootstrap.rebuild.macro-plan-test))
        (is (every? #(contains? (set (scope/scope-namespaces :compiler)) %)
                    (map symbol (map second
                         (partition 2
                                    (scope/normalize-development-args :compiler []))))))))))

(deftest empty-development-invocation-selects-only-its-scope
  (let [arguments (scope/normalize-development-args :language [])]
    (is (= (set (scope/scope-namespaces :language))
           (set (map symbol (map second (partition 2 arguments))))))
    (is (not-any? #{"gravity.bootstrap-test"} arguments))))

(deftest development-selection-rejects-bootstrap-namespace
  (is (throws-message?
       #"outside the requested project scope"
       #(scope/normalize-development-args
         :language ["--namespace" "gravity.bootstrap-test"]))))

(deftest bootstrap-selection-accepts-existing-focused-selectors
  (is (= ["--namespace" "gravity.self-hosting.w5-stage-equivalence-verifier-test"
          "--exact" "gravity.self-hosting.w5-stage-equivalence-verifier-test/w5-stage-equivalence-rejected-fixture-covers-every-mode-and-control"
          "--fail-fast"]
         (scope/normalize-development-args
          :bootstrap
          ["--namespace" "gravity.self-hosting.w5-stage-equivalence-verifier-test"
           "--exact" "gravity.self-hosting.w5-stage-equivalence-verifier-test/w5-stage-equivalence-rejected-fixture-covers-every-mode-and-control"
           "--fail-fast"]))))

(deftest bootstrap-selection-rejects-language-namespace
  (is (throws-message?
       #"outside the requested project scope"
       #(scope/normalize-development-args
         :bootstrap ["--namespace" "gravity.reader.normal-dispatch-test"]))))
