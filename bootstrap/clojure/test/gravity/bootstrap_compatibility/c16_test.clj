(ns gravity.bootstrap-compatibility.c16-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.c16-incremental :as c16]))

(defn fixture
  [name]
  (str "bootstrap/clojure/fixtures/" name))

(deftest c16-incremental-compatibility-wrappers-preserve-interposition
  (is (= '([stage source-hash dependency-hash])
         (:arglists (meta #'bootstrap/c16-stage-cache-key))))
  (is (= '([source-path source-text])
         (:arglists
          (meta #'bootstrap/compiler-c16-incremental-source-artifact))))
  (is (= '([path])
         (:arglists (meta #'bootstrap/compiler-c16-incremental-file-artifact))))
  (let [bindings (atom 0)
        with-operations c16/with-operations]
    (with-redefs [c16/with-operations
                  (fn [operations thunk]
                    (swap! bindings inc)
                    (with-operations operations thunk))]
      (bootstrap/compiler-c16-incremental-file-artifact
       (fixture "accepted/compiler-c16-incremental.gravity")))
    (is (= 1 @bindings)))
  (let [calls (atom [])
        original bootstrap/c16-stage-cache-key
        artifact
        (with-redefs [bootstrap/c16-stage-cache-key
                      (fn [stage source dependency]
                        (swap! calls conj stage)
                        (assoc (original stage source dependency)
                               :interposed? true))]
          (bootstrap/compiler-c16-incremental-file-artifact
           (fixture "accepted/compiler-c16-incremental.gravity")))]
    (is (= 8 (count @calls)))
    (is (every? :interposed? (:stage-cache-keys artifact))))
  (let [sentinel {:kind :sentinel-c16-source}]
    (is (= sentinel
           (with-redefs [bootstrap/compiler-c16-incremental-source-artifact
                         (fn [_ _] sentinel)]
             (bootstrap/compiler-c16-incremental-file-artifact
              (fixture "accepted/compiler-c16-incremental.gravity"))))))
  (let [artifact
        (with-redefs [bootstrap/c16-incremental-diagnostic-ids
                      ["C16-SENTINEL"]
                      bootstrap/c16-incremental-governing-document
                      "docs/c16-sentinel.md"]
          (bootstrap/compiler-c16-incremental-file-artifact
           (fixture "accepted/compiler-c16-incremental.gravity")))]
    (is (= "docs/c16-sentinel.md" (:governing-document artifact)))
    (is (= ["C16-SENTINEL"]
           (get-in artifact [:c16-incremental-results
                             :required-diagnostic-ids])))
    (is (= #{"C16-SENTINEL"}
           (set (map :diagnostic
                     (get-in artifact
                             [:incremental-diagnostic-stream
                              :diagnostics]))))))
  (is (= (:public-api (c16/c16-engine-contract)) c16/public-api)))
