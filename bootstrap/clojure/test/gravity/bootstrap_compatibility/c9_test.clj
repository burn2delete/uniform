(ns gravity.bootstrap-compatibility.c9-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.c9-ownership-checker :as c9]))

(defn fixture
  [name]
  (str "bootstrap/clojure/fixtures/" name))

(deftest c9-ownership-checker-compatibility-wrappers-preserve-interposition
  (is (= '([module effect-graph])
         (:arglists (meta #'bootstrap/c9-ownership-graph))))
  (is (= '([source-path source-text])
         (:arglists (meta #'bootstrap/compiler-c9-ownership-source-artifact))))
  (is (= '([path])
         (:arglists (meta #'bootstrap/compiler-c9-ownership-file-artifact))))
  (let [calls (atom [])
        graph
        (with-redefs [bootstrap/c9-node
                      (fn [node-ids index fallback]
                        (swap! calls conj [node-ids index fallback])
                        "interposed-node")]
          (bootstrap/c9-ownership-graph
           {:module 'gravity.c9-probe
            :source-path "c9-probe.gravity"
            :profile :hosted
            :target :jvm}
           {:nodes (sorted-map "node-0" {} "node-1" {})}))]
    (is (= "interposed-node" (get-in graph [:moves 0 :value])))
    (is (= 1 (count @calls))))
  (let [bindings (atom 0)
        with-operations c9/with-operations]
    (with-redefs [c9/with-operations
                  (fn [operations thunk]
                    (swap! bindings inc)
                    (with-operations operations thunk))]
      (bootstrap/compiler-c9-ownership-file-artifact
       (fixture "accepted/compiler-c9-ownership-checker.gravity")))
    (is (= 1 @bindings)))
  (let [artifact
        (with-redefs [bootstrap/c9-ownership-diagnostic-ids ["C9-SENTINEL"]
                      bootstrap/c9-ownership-rejected-designs
                      [{:diagnostic "C9-SENTINEL"}]
                      bootstrap/c9-ownership-governing-document
                      "docs/c9-sentinel.md"]
          (bootstrap/compiler-c9-ownership-file-artifact
           (fixture "accepted/compiler-c9-ownership-checker.gravity")))]
    (is (= "docs/c9-sentinel.md" (:governing-document artifact)))
    (is (= ["C9-SENTINEL"]
           (get-in artifact [:c9-ownership-check-results
                             :required-diagnostic-ids])))
    (is (= #{"C9-SENTINEL"}
           (set (map :diagnostic
                     (get-in artifact
                             [:ownership-diagnostics :diagnostics]))))))
  (is (= (:public-api (c9/c9-engine-contract)) c9/public-api)))
