(ns gravity.bootstrap-compatibility.c17-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.c17-plugin :as c17]))

(defn fixture
  [name]
  (str "bootstrap/clojure/fixtures/" name))

(deftest c17-plugin-compatibility-wrappers-preserve-interposition
  (is (= '([source-path plugin-manifest input-id])
         (:arglists (meta #'bootstrap/c17-plugin-diagnostic-stream))))
  (is (= '([source-path source-text])
         (:arglists (meta #'bootstrap/compiler-c17-plugin-source-artifact))))
  (is (= '([path])
         (:arglists (meta #'bootstrap/compiler-c17-plugin-file-artifact))))
  (let [bindings (atom 0)
        with-operations c17/with-operations]
    (with-redefs [c17/with-operations
                  (fn [operations thunk]
                    (swap! bindings inc)
                    (with-operations operations thunk))]
      (bootstrap/compiler-c17-plugin-file-artifact
       (fixture "accepted/compiler-c17-plugin.gravity")))
    (is (= 1 @bindings)))
  (let [calls (atom 0)
        original bootstrap/c17-plugin-diagnostic-stream
        artifact
        (with-redefs [bootstrap/c17-plugin-diagnostic-stream
                      (fn [path manifest input]
                        (swap! calls inc)
                        (assoc (original path manifest input)
                               :interposed? true))]
          (bootstrap/compiler-c17-plugin-file-artifact
           (fixture "accepted/compiler-c17-plugin.gravity")))]
    (is (= 1 @calls))
    (is (true? (get-in artifact [:plugin-diagnostic-stream :interposed?]))))
  (let [sentinel {:kind :sentinel-c17-source}]
    (is (= sentinel
           (with-redefs [bootstrap/compiler-c17-plugin-source-artifact
                         (fn [_ _] sentinel)]
             (bootstrap/compiler-c17-plugin-file-artifact
              (fixture "accepted/compiler-c17-plugin.gravity"))))))
  (let [artifact
        (with-redefs [bootstrap/c17-plugin-diagnostic-ids
                      ["C17-SENTINEL"]
                      bootstrap/c17-plugin-governing-document
                      "docs/c17-sentinel.md"]
          (bootstrap/compiler-c17-plugin-file-artifact
           (fixture "accepted/compiler-c17-plugin.gravity")))]
    (is (= "docs/c17-sentinel.md" (:governing-document artifact)))
    (is (= ["C17-SENTINEL"]
           (get-in artifact [:c17-plugin-results
                             :required-diagnostic-ids])))
    (is (= #{"C17-SENTINEL"}
           (set (map :diagnostic
                     (get-in artifact
                             [:plugin-diagnostic-stream :diagnostics]))))))
  (is (= (:public-api (c17/c17-engine-contract)) c17/public-api)))
