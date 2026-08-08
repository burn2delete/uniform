(ns gravity.bootstrap-compatibility.c7-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.c7-type-checker :as c7]))

(defn fixture
  [name]
  (str "bootstrap/clojure/fixtures/" name))

(deftest c7-type-checker-compatibility-wrappers-preserve-interposition
  (is (= '([node]) (:arglists (meta #'bootstrap/c7-node-type))))
  (is (= '([source-path source-text])
         (:arglists (meta #'bootstrap/compiler-c7-type-source-artifact))))
  (is (= '([path])
         (:arglists (meta #'bootstrap/compiler-c7-type-file-artifact))))
  (is (= "CheckedCast[String]"
         (with-redefs [bootstrap/c7-node-operator
                       (constantly 'dynamic/cast)]
           (bootstrap/c7-node-type
            {:form :call :children {:operator 'dynamic/value}}))))
  (let [bindings (atom 0)
        with-operations c7/with-operations]
    (with-redefs [c7/with-operations
                  (fn [operations thunk]
                    (swap! bindings inc)
                    (with-operations operations thunk))]
      (bootstrap/compiler-c7-type-file-artifact
       (fixture "accepted/compiler-c7-type-checker.gravity")))
    (is (= 1 @bindings)))
  (let [artifact
        (with-redefs [bootstrap/c7-type-diagnostic-ids ["C7-SENTINEL"]
                      bootstrap/c7-type-rejected-designs
                      [{:diagnostic "C7-SENTINEL"}]
                      bootstrap/c7-type-governing-document
                      "docs/c7-sentinel.md"]
          (bootstrap/compiler-c7-type-file-artifact
           (fixture "accepted/compiler-c7-type-checker.gravity")))]
    (is (= "docs/c7-sentinel.md" (:governing-document artifact)))
    (is (= ["C7-SENTINEL"]
           (get-in artifact [:c7-type-check-results
                             :required-diagnostic-ids])))
    (is (= #{"C7-SENTINEL"}
           (set (map :diagnostic
                     (get-in artifact [:type-diagnostics :diagnostics]))))))
  (is (= (:public-api (c7/c7-engine-contract)) c7/public-api)))
