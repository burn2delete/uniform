(ns gravity.pass-cache-request-key-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.pass-cache :as cache]
            [gravity.pass-cache-test]
            [gravity.pass-execution :as pass-execution]
            [gravity.pass-execution.contract :as contract]))

(defn- request-fixture []
  ((var-get (ns-resolve 'gravity.pass-cache-test 'request))))

(deftest stage-cache-key-validates-contract-once
  (let [calls (atom 0)
        original contract/validate-pass-contract!]
    (with-redefs [contract/validate-pass-contract!
                  (fn [value]
                    (swap! calls inc)
                    (original value))]
      (cache/stage-cache-key (request-fixture)))
    (is (= 1 @calls))))

(deftest stage-cache-key-retains-validator-interposition
  (let [calls (atom 0)
        original pass-execution/validate-pass-contract!]
    (with-redefs [pass-execution/validate-pass-contract!
                  (fn [value]
                    (swap! calls inc)
                    (original value))]
      (cache/stage-cache-key (request-fixture)))
    (is (= 1 @calls))))

(deftest standalone-stage-validation-still-runs-validator
  (let [calls (atom 0)
        validate-stage-request!
        (var-get (ns-resolve 'gravity.pass-cache.request-key
                             'validate-stage-request!))
        original pass-execution/validate-pass-contract!]
    (with-redefs [pass-execution/validate-pass-contract!
                  (fn [value]
                    (swap! calls inc)
                    (original value))]
      (validate-stage-request! (request-fixture)))
    (is (= 1 @calls))))

(deftest stage-cache-key-retains-contract-id-interposition
  (let [calls (atom 0)
        original pass-execution/pass-contract-id]
    (with-redefs [pass-execution/pass-contract-id
                  (fn [value]
                    (swap! calls inc)
                    (original value))]
      (cache/stage-cache-key (request-fixture)))
    (is (= 1 @calls))))

(deftest invalid-contract-is-still-rejected
  (let [request (assoc-in (request-fixture) [:contract :unknown] true)
        failure (try
                  (cache/stage-cache-key request)
                  nil
                  (catch clojure.lang.ExceptionInfo error
                    error))]
    (is (= "C1-PASS-CONTRACT" (:id (ex-data failure))))))
