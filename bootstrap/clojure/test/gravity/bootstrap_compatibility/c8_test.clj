(ns gravity.bootstrap-compatibility.c8-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.c8-effect-checker :as c8]))

(defn fixture
  [name]
  (str "bootstrap/clojure/fixtures/" name))

(deftest c8-effect-checker-compatibility-wrappers-preserve-interposition
  (is (= '([module type-facts functions])
         (:arglists (meta #'bootstrap/c8-effect-graph))))
  (is (= '([source-path source-text])
         (:arglists (meta #'bootstrap/compiler-c8-effect-source-artifact))))
  (is (= '([path])
         (:arglists (meta #'bootstrap/compiler-c8-effect-file-artifact))))
  (let [calls (atom [])
        fact {:core-node "probe" :effects #{}}]
    (is (= [fact]
           (with-redefs [bootstrap/c8-fact-direct-effects
                         (fn [value]
                           (swap! calls conj value)
                           #{:interposed/effect})]
             (vec (bootstrap/c8-effectful-facts [fact])))))
    (is (= 1 (count @calls))))
  (let [bindings (atom 0)
        with-operations c8/with-operations]
    (with-redefs [c8/with-operations
                  (fn [operations thunk]
                    (swap! bindings inc)
                    (with-operations operations thunk))]
      (bootstrap/compiler-c8-effect-file-artifact
       (fixture "accepted/compiler-c8-effect-checker.gravity")))
    (is (= 1 @bindings)))
  (let [artifact
        (with-redefs [bootstrap/c8-effect-diagnostic-ids ["C8-SENTINEL"]
                      bootstrap/c8-effect-rejected-designs
                      [{:diagnostic "C8-SENTINEL"}]
                      bootstrap/c8-effect-governing-document
                      "docs/c8-sentinel.md"]
          (bootstrap/compiler-c8-effect-file-artifact
           (fixture "accepted/compiler-c8-effect-checker.gravity")))]
    (is (= "docs/c8-sentinel.md" (:governing-document artifact)))
    (is (= ["C8-SENTINEL"]
           (get-in artifact [:c8-effect-check-results
                             :required-diagnostic-ids])))
    (is (= #{"C8-SENTINEL"}
           (set (map :diagnostic
                     (get-in artifact [:effect-diagnostics :diagnostics]))))))
  (is (= (:public-api (c8/c8-engine-contract)) c8/public-api)))
