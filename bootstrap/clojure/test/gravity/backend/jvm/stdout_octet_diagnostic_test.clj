(ns gravity.backend.jvm.stdout-octet-diagnostic-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap])
  (:import [java.security MessageDigest]
           [java.util HexFormat]))

(defn- sha256-of-hex [hex]
  (let [hex-format (HexFormat/of)
        bytes (.parseHex hex-format hex)
        digest (.digest (MessageDigest/getInstance "SHA-256") bytes)]
    (str "sha256:" (.formatHex hex-format digest))))

(defn- load-parity-stage []
  ;; Bootstrap captures the stage in the artifact pipeline and unmaps its Var.
  (let [stage-ns (create-ns (gensym "jvm-stdout-parity-"))
        resource "gravity/bootstrap_impl/semantic_late/142709-jvm-backend-execution-parity.clj"]
    (try
      (binding [*ns* stage-ns]
        (refer 'clojure.core)
        (refer 'gravity.bootstrap
               :only '[*jvm-backend-java-command* jvm-backend-run-process!
                       c-backend-runtime-bytes jvm-backend-fail! sha256-hex sha256-bytes-hex])
        (with-open [reader (io/reader (io/resource resource))]
          @(load-reader reader)))
      (finally (remove-ns (ns-name stage-ns))))))

(deftest execution-parity-compares-and-hashes-stdout-octets
  (let [expected "\u00e9"
        matching {:exit 0 :stdout-bytes [195 169] :stderr ""}
        mismatched (assoc-in matching [:stdout-bytes 1] 255)
        state {:source-path "stdout.gravity"
               :packet {:stage2-runtime-execution-record {:stdout expected}}
               :staged-paths {:jar "staged.jar"}}
        parity (load-parity-stage)
        run-parity (fn [execution]
                     (with-redefs [bootstrap/jvm-backend-run-process! (constantly execution)]
                       (parity state)))]
    (testing "matching UTF-8 stdout passes byte-exact parity"
      (is (= (assoc state :execution matching :expected-output expected :expected-bytes [195 169])
             (run-parity matching))))
    (testing "invalid UTF-8 stdout is rejected with hashes of the raw bytes"
      (let [data (try
                   (run-parity mismatched)
                   nil
                   (catch clojure.lang.ExceptionInfo ex (ex-data ex)))]
        (is (= {:id "B14-DIFFERENTIAL"
                :missing-fact :stage2-jvm-execution-equivalence
                :expected-stdout-hash (sha256-of-hex "c3a9")
                :actual-stdout-hash (sha256-of-hex "c3ff")}
               (select-keys data [:id :missing-fact :expected-stdout-hash :actual-stdout-hash])))))))
