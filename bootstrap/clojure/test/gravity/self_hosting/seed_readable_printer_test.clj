(ns gravity.self-hosting.seed-readable-printer-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

(defn- diagnostic-data
  [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo ex
      (ex-data ex))))

(deftest seed-readable-printer-removes-project-input-quotas
  (let [source-path "/seed-printer-focused.gravity"
        render #(bootstrap/p15-s23-seed-readable-pr-str source-path %)
        nested (nth (iterate vector nil) 98)
        wide (vec (range 513))
        many-nodes (vec (repeat 4097 0))
        huge-integer (.shiftLeft java.math.BigInteger/ONE 4096)
        huge-ratio (clojure.lang.Ratio.
                    java.math.BigInteger/ONE huge-integer)
        huge-string (apply str (repeat 32769 "x"))
        huge-output-string (apply str (repeat 262145 "x"))]
    (doseq [[label value]
            [[:depth nested]
             [:collection-width wide]
             [:node-count many-nodes]
             [:integer-bits huge-integer]
             [:integer-bits-in-ratio huge-ratio]
             [:scalar-bytes huge-string]]]
      (is (string? (render [value])) label))
    (is (> (count (render [huge-output-string])) 262144))
    (let [args (vec (range 257))
          stage0
          (bootstrap/invoke-stage0-builtin
           {:source-path source-path} 'pr-str args)
          stage2
          (bootstrap/p15-s23-stage2-runtime-invoke-builtin
           {:source {:path source-path}} 'pr-str args)]
      (is (= stage0 stage2))
      (is (= (str/join " " (map str args)) stage0)))
    (let [data
          (diagnostic-data
           #(bootstrap/p15-s23-seed-readable-pr-str source-path (range)))]
      (is (= :unsupported-argument-carrier (:reason data)))
      (is (= :clojure-seed-compatibility (:printer-boundary data))))
    (let [data (diagnostic-data #(render ["\uD800"]))]
      (is (= :invalid-unicode-scalar (:reason data)))
      (is (= :clojure-seed-compatibility (:printer-boundary data))))
    (let [backing (object-array 1)
          cyclic (clojure.lang.PersistentVector/adopt backing)]
      (aset backing 0 cyclic)
      (let [data (diagnostic-data #(render [cyclic]))]
        (is (= :cyclic-value (:reason data)))
        (is (= :clojure-seed-compatibility (:printer-boundary data)))))))
