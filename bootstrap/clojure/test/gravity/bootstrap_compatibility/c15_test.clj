(ns gravity.bootstrap-compatibility.c15-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.c15-diagnostics :as c15]
            [gravity.compiler-verification-shared :as compiler-verification-shared]))

(defn fixture
  [name]
  (str "bootstrap/clojure/fixtures/" name))

(deftest c15-diagnostics-compatibility-wrappers-preserve-interposition
  (is (= '([diagnostic])
         (:arglists (meta #'bootstrap/c15-stable-diagnostic-id))))
  (is (= '([rule severity stage message-key source-path form-index
            primary-artifact facts remediation &
            {:keys [related origin-chain redactions lifecycle generated?]}])
         (:arglists (meta #'bootstrap/c15-diagnostic-record))))
  (is (= '([source-path source-text])
         (:arglists
          (meta #'bootstrap/compiler-c15-diagnostics-source-artifact))))
  (is (= '([path])
         (:arglists (meta #'bootstrap/compiler-c15-diagnostics-file-artifact))))
  (let [bindings (atom 0)
        with-operations c15/with-operations]
    (with-redefs [c15/with-operations
                  (fn [operations thunk]
                    (swap! bindings inc)
                    (with-operations operations thunk))]
      (bootstrap/compiler-c15-diagnostics-file-artifact
       (fixture "accepted/compiler-c15-diagnostics.gravity")))
    (is (= 1 @bindings)))
  (let [calls (atom 0)
        original bootstrap/c15-diagnostic-record
        artifact
        (with-redefs [bootstrap/c15-diagnostic-record
                      (fn [& args]
                        (swap! calls inc)
                        (assoc (apply original args) :interposed? true))]
          (bootstrap/compiler-c15-diagnostics-file-artifact
           (fixture "accepted/compiler-c15-diagnostics.gravity")))]
    (is (= 4 @calls))
    (is (every? :interposed?
                (get-in artifact [:diagnostic-stream :diagnostics]))))
  (let [sentinel {:kind :sentinel-c15-source}]
    (is (= sentinel
           (with-redefs [bootstrap/compiler-c15-diagnostics-source-artifact
                         (fn [_ _] sentinel)]
             (bootstrap/compiler-c15-diagnostics-file-artifact
              (fixture "accepted/compiler-c15-diagnostics.gravity"))))))
  (let [artifact
        (with-redefs [bootstrap/c15-diagnostics-diagnostic-ids
                      ["C15-SENTINEL"]
                      bootstrap/c15-diagnostics-governing-document
                      "docs/c15-sentinel.md"]
          (bootstrap/compiler-c15-diagnostics-file-artifact
           (fixture "accepted/compiler-c15-diagnostics.gravity")))]
    (is (= "docs/c15-sentinel.md" (:governing-document artifact)))
    (is (= ["C15-SENTINEL"]
           (get-in artifact [:c15-diagnostics-results
                             :required-diagnostic-ids])))
    (is (= #{"C15-SENTINEL"}
           (set (map :rule (:golden-diagnostic-fixtures artifact))))))
  (is (= (:public-api (c15/c15-engine-contract)) c15/public-api))
  (is (= (:public-api
          (compiler-verification-shared/shared-contract))
         compiler-verification-shared/public-api)))
