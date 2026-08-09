(ns gravity.compiler-verification-shared-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.compiler-verification-shared :as shared]))

(deftest shared-catalog-contract-is-nonauthoritative
  (let [contract (shared/shared-contract)
        publics (ns-publics 'gravity.compiler-verification-shared)]
    (is (= :hosted-stage0-compiler-verification-shared
           (:contract-boundary contract)))
    (is (true? (:compatibility-only? contract)))
    (is (false? (:canonical-authority? contract)))
    (is (= [] (get-in contract [:dependency-direction :requires])))
    (is (some #{:canonical-c15-authority} (:does-not-own contract)))
    (is (some #{:canonical-c18-authority} (:does-not-own contract)))
    (is (= 37 (count shared/compiler-verification-diagnostic-ids)))
    (is (= (set shared/compiler-verification-diagnostic-ids)
           (set (keys shared/compiler-verification-diagnostic-messages))))
    (is (= (set (keys publics)) (set (keys shared/public-api))))
    (is (nil? (find-ns 'gravity.bootstrap)))))
