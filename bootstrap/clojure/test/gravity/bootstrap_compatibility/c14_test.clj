(ns gravity.bootstrap-compatibility.c14-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.c14-lowering :as c14]))

(defn fixture
  [name]
  (str "bootstrap/clojure/fixtures/" name))

(deftest c14-lowering-compatibility-wrappers-preserve-interposition
  (is (= '([source-path input-id])
         (:arglists (meta #'bootstrap/c14-lowering-diagnostic-catalog))))
  (is (= '([source-path source-text])
         (:arglists (meta #'bootstrap/compiler-c14-lowering-source-artifact))))
  (is (= '([path])
         (:arglists (meta #'bootstrap/compiler-c14-lowering-file-artifact))))
  (let [bindings (atom 0)
        with-operations c14/with-operations]
    (with-redefs [c14/with-operations
                  (fn [operations thunk]
                    (swap! bindings inc)
                    (with-operations operations thunk))]
      (bootstrap/compiler-c14-lowering-file-artifact
       (fixture "accepted/compiler-c14-lowering.gravity")))
    (is (= 1 @bindings)))
  (let [calls (atom 0)
        original bootstrap/c14-lowering-diagnostic-catalog
        artifact
        (with-redefs [bootstrap/c14-lowering-diagnostic-catalog
                      (fn [source-path input-id]
                        (swap! calls inc)
                        (original source-path input-id))]
          (bootstrap/compiler-c14-lowering-file-artifact
           (fixture "accepted/compiler-c14-lowering.gravity")))]
    (is (= 1 @calls))
    (is (= (set bootstrap/c14-lowering-diagnostic-ids)
           (set (map :diagnostic
                     (get-in artifact
                             [:lowering-diagnostic-stream :diagnostics]))))))
  (let [sentinel {:kind :sentinel-c14-source}]
    (is (= sentinel
           (with-redefs [bootstrap/compiler-c14-lowering-source-artifact
                         (fn [_ _] sentinel)]
             (bootstrap/compiler-c14-lowering-file-artifact
              (fixture "accepted/compiler-c14-lowering.gravity"))))))
  (let [artifact
        (with-redefs [bootstrap/c14-lowering-diagnostic-ids
                      ["C14-SENTINEL"]
                      bootstrap/optimization-lowering-diagnostic-messages
                      {"C14-SENTINEL" "sentinel diagnostic"}
                      bootstrap/c14-lowering-governing-document
                      "docs/c14-sentinel.md"]
          (bootstrap/compiler-c14-lowering-file-artifact
           (fixture "accepted/compiler-c14-lowering.gravity")))]
    (is (= "docs/c14-sentinel.md" (:governing-document artifact)))
    (is (= ["C14-SENTINEL"]
           (get-in artifact [:c14-lowering-results
                             :required-diagnostic-ids])))
    (is (= #{"C14-SENTINEL"}
           (set (map :diagnostic
                     (get-in artifact
                             [:lowering-diagnostic-stream :diagnostics]))))))
  (is (= (:public-api (c14/c14-engine-contract)) c14/public-api)))
