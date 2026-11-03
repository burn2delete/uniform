(ns gravity.p15-s23-source-inventory-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

(deftest explicit-relocation-preserves-logical-source-authorship
  (let [declared
        "bootstrap/gravity/src/gravity/backend/b9_hdl_backend_design.gravity"
        actual
        "language/compiler/src/gravity/backend/b9_hdl_backend_design.gravity"
        record
        (first
         (bootstrap/p15-s23-source-module-records
          {:source-modules [{:component :hdl-backend :path declared}]}))]
    (is (= 24 (count bootstrap/p15-s23-compiler-source-relocation-map)))
    (is (= actual
           (get bootstrap/p15-s23-compiler-source-relocation-map declared)))
    (is (= declared (:path record)))
    (is (= declared (:logical-path record)))
    (is (= actual (:actual-path record)))
    (is (true? (:relocated? record)))
    (is (= :hdl-backend (:component record)))
    (is (= :present (:status record)))
    (is (re-find #"^sha256:" (:source-hash record)))))

(deftest unmapped-or-missing-source-does-not-fallback-to-a-relocated-file
  (let [declared
        "bootstrap/gravity/src/gravity/compiler/c6_call_arity_legality.gravity"
        resolution (bootstrap/p15-s23-compiler-source-path-resolution declared)]
    (is (= declared (:logical-path resolution)))
    (is (= declared (:actual-path resolution)))
    (is (false? (:relocated? resolution)))
    (is (false? (bootstrap/p15-s23-source-module-present?
                 {:component :unmapped :path declared})))
    (is (false? (bootstrap/p15-s23-source-module-present?
                 {:component :missing
                  :path "bootstrap/gravity/src/gravity/compiler/not-listed.gravity"})))))
