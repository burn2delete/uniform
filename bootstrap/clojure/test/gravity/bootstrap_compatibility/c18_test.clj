(ns gravity.bootstrap-compatibility.c18-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.c18-verification :as c18]))

(defn fixture
  [name]
  (str "bootstrap/clojure/fixtures/" name))

(deftest c18-verification-compatibility-wrappers-preserve-interposition
  (is (= '([]) (:arglists (meta #'bootstrap/c18-pass-risk-records))))
  (is (= '([source-path source-text])
         (:arglists
          (meta #'bootstrap/compiler-c18-verification-source-artifact))))
  (is (= '([path])
         (:arglists
          (meta #'bootstrap/compiler-c18-verification-file-artifact))))
  (let [bindings (atom 0)
        with-operations c18/with-operations]
    (with-redefs [c18/with-operations
                  (fn [operations thunk]
                    (swap! bindings inc)
                    (with-operations operations thunk))]
      (bootstrap/compiler-c18-verification-file-artifact
       (fixture "accepted/compiler-c18-verification.gravity")))
    (is (= 1 @bindings)))
  (let [calls (atom 0)
        original bootstrap/c18-pass-risk-records
        artifact
        (with-redefs [bootstrap/c18-pass-risk-records
                      (fn []
                        (swap! calls inc)
                        (mapv #(assoc % :interposed? true) (original)))]
          (bootstrap/compiler-c18-verification-file-artifact
           (fixture "accepted/compiler-c18-verification.gravity")))]
    (is (= 1 @calls))
    (is (every? :interposed? (:pass-risk-classification artifact))))
  (let [sentinel {:kind :sentinel-c18-source}]
    (is (= sentinel
           (with-redefs [bootstrap/compiler-c18-verification-source-artifact
                         (fn [_ _] sentinel)]
             (bootstrap/compiler-c18-verification-file-artifact
              (fixture "accepted/compiler-c18-verification.gravity"))))))
  (let [artifact
        (with-redefs [bootstrap/c18-verification-diagnostic-ids
                      ["C18-SENTINEL"]
                      bootstrap/c18-verification-governing-document
                      "docs/c18-sentinel.md"]
          (bootstrap/compiler-c18-verification-file-artifact
           (fixture "accepted/compiler-c18-verification.gravity")))]
    (is (= "docs/c18-sentinel.md" (:governing-document artifact)))
    (is (= ["C18-SENTINEL"]
           (get-in artifact [:c18-verification-results
                             :required-diagnostic-ids])))
    (is (= #{"C18-SENTINEL"}
           (set (map :diagnostic
                     (get-in artifact
                             [:verification-diagnostic-stream
                              :diagnostics]))))))
  (is (= (:public-api (c18/c18-engine-contract)) c18/public-api)))
