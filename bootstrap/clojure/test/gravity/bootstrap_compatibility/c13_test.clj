(ns gravity.bootstrap-compatibility.c13-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.c13-optimization :as c13]
            [gravity.optimization-lowering :as optimization-lowering]))

(defn fixture
  [name]
  (str "bootstrap/clojure/fixtures/" name))

(deftest c13-optimization-compatibility-wrappers-preserve-interposition
  (is (= '([record])
         (:arglists (meta #'bootstrap/optimization-pass-contract-record))))
  (is (= '([domain-ir-artifact input-id index contract])
         (:arglists (meta #'bootstrap/optimization-decision-record))))
  (is (= '([source-path source-text])
         (:arglists
          (meta #'bootstrap/compiler-c13-optimization-source-artifact))))
  (is (= '([path])
         (:arglists (meta #'bootstrap/compiler-c13-optimization-file-artifact))))
  (let [bindings (atom 0)
        with-operations c13/with-operations]
    (with-redefs [c13/with-operations
                  (fn [operations thunk]
                    (swap! bindings inc)
                    (with-operations operations thunk))]
      (bootstrap/compiler-c13-optimization-file-artifact
       (fixture "accepted/compiler-c13-optimization.gravity")))
    (is (= 1 @bindings)))
  (let [calls (atom 0)
        original bootstrap/optimization-pass-contract-record
        artifact
        (with-redefs [bootstrap/optimization-pass-contract-record
                      (fn [record]
                        (swap! calls inc)
                        (assoc (original record) :interposed? true))]
          (bootstrap/compiler-c13-optimization-file-artifact
           (fixture "accepted/compiler-c13-optimization.gravity")))]
    (is (= 6 @calls))
    (is (every? :interposed? (:optimization-pass-registry artifact))))
  (let [sentinel {:kind :sentinel-c13-source}]
    (is (= sentinel
           (with-redefs [bootstrap/compiler-c13-optimization-source-artifact
                         (fn [_ _] sentinel)]
             (bootstrap/compiler-c13-optimization-file-artifact
              (fixture "accepted/compiler-c13-optimization.gravity"))))))
  (let [artifact
        (with-redefs [bootstrap/c13-optimization-diagnostic-ids
                      ["C13-SENTINEL"]
                      bootstrap/optimization-lowering-diagnostic-messages
                      {"C13-SENTINEL" "sentinel diagnostic"}
                      bootstrap/c13-optimization-governing-document
                      "docs/c13-sentinel.md"]
          (bootstrap/compiler-c13-optimization-file-artifact
           (fixture "accepted/compiler-c13-optimization.gravity")))]
    (is (= "docs/c13-sentinel.md" (:governing-document artifact)))
    (is (= ["C13-SENTINEL"]
           (get-in artifact [:c13-optimization-results
                             :required-diagnostic-ids])))
    (is (= #{"C13-SENTINEL"}
           (set (map :diagnostic
                     (get-in artifact
                             [:optimization-diagnostic-stream
                              :diagnostics]))))))
  (is (= (:public-api (c13/c13-engine-contract)) c13/public-api))
  (is (= (:public-api (optimization-lowering/shared-engine-contract))
         optimization-lowering/public-api)))

(deftest optimization-lowering-captured-facade-delegates-exactly-once
  (let [record (first bootstrap/optimization-pass-contract-seed)
        original bootstrap/optimization-pass-contract-record
        expected (assoc (original record) :interposed? :direct)
        calls (atom 0)
        actual
        (with-redefs [bootstrap/optimization-pass-contract-record
                      (fn [candidate]
                        (swap! calls inc)
                        (assoc (original candidate) :interposed? :direct))]
          (bootstrap/optimization-pass-contract-record record))]
    (testing "a captured facade delegates without redispatching its replacement"
      (is (= 1 @calls))
      (is (= expected actual))))
  (let [path (fixture "accepted/compiler-optimization-lowering.gravity")
        source-text (slurp path)
        original bootstrap/optimization-pass-contract-record
        calls (atom 0)
        artifact
        (with-redefs [bootstrap/optimization-pass-contract-record
                      (fn [record]
                        (swap! calls inc)
                        (assoc (original record) :interposed? :shared))]
          (bootstrap/optimization-lowering-source-artifact path source-text))]
    (testing "the shared optimization facade retains ordinary interposition"
      (is (= 6 @calls))
      (is (every? #(= :shared (:interposed? %))
                  (:optimization-pass-registry artifact)))))
  (let [path (fixture "accepted/compiler-c13-optimization.gravity")
        original bootstrap/optimization-pass-contract-record
        calls (atom 0)
        artifact
        (with-redefs [bootstrap/optimization-pass-contract-record
                      (fn [record]
                        (swap! calls inc)
                        (assoc (original record) :interposed? :c13))]
          (bootstrap/compiler-c13-optimization-file-artifact path))]
    (testing "the C13 facade retains nested shared-engine interposition"
      (is (= 6 @calls))
      (is (every? #(= :c13 (:interposed? %))
                  (:optimization-pass-registry artifact))))))
