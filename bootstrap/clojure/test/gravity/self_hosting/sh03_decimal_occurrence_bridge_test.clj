(ns gravity.self-hosting.sh03-decimal-occurrence-bridge-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- private-var
  [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw (ex-info "missing SH-07 decimal bridge seam" {:symbol symbol}))))

(defn- private-call
  [symbol & args]
  (apply @(private-var symbol) args))

(defn- failure-data
  [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo failure
      (ex-data failure))))

(deftest current-c2-contract-does-not-claim-the-unintegrated-decimal-join
  (let [install-source
        (slurp "bootstrap/clojure/src/gravity/bootstrap_impl/semantic_late/146156-sh03-reader-adapt-install.clj")
        pass-contract
        (slurp "bootstrap/clojure/src/gravity/bootstrap_impl/components/compiler/00831-c2-pass-cache-compiler-contract.clj")
        consumer-sources
        (map slurp ["bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00943-p15-s23-sh07-host-builtins-and-oracle-04.clj"
                    "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00943-p15-s23-sh07-host-builtins-and-oracle-05.clj"
                    "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00943-p15-s23-sh07-host-builtins-and-oracle-08.clj"])]
    (is (.contains install-source ":gravity/sh03-to-c2-reader-products-v2"))
    (is (.contains pass-contract ":gravity/sh03-to-c2-reader-products-v2"))
    (is (not (.contains install-source ":sh07-decimal-occurrence-projection")))
    (is (not (.contains pass-contract ":sh07-decimal-occurrence-projection")))
    (is (every? #(not (.contains % "sh03-reader-final-decimal-occurrence-projection-valid?"))
                consumer-sources))))

(deftest decimal-consumers-fail-closed-until-the-v3-bridge-is-integrated
  (let [evidence {:c2-reader-artifact {}
                  :adapted-form-id :form-0
                  :adapted-token-id :token-0}
        expected {:reason :unsupported-decimal-readable-spelling
                  :adapter-contract :gravity/sh03-to-c2-reader-products-v2
                  :required-adapter-contract :gravity/sh03-to-c2-reader-products-v3
                  :source-authority :current-c2-reader-products
                  :projection-authority :not-integrated}]
    (doseq [symbol ['p15-s23-sh07-p-decimal-occurrence
                    'p15-s23-sh07-o-decimal-occurrence]]
      (testing (name symbol)
        (is (= expected
               (failure-data #(private-call symbol evidence))))))))
