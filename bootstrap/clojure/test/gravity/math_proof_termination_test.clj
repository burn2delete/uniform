(ns gravity.math-proof-termination-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

(defn- fixture
  [name]
  (str "bootstrap/clojure/fixtures/" name))

(defn- diagnostic-id
  [f]
  (try
    (f)
    nil
    (catch clojure.lang.ExceptionInfo ex
      (:id (ex-data ex)))))

(deftest accepts-decreasing-measure-without-fuel
  (let [artifact (bootstrap/math-proof-file-artifact
                  (fixture "accepted/math-rewrite-decreasing-measure.gravity"))
        termination (:termination-report artifact)
        proof (:capability-based-proof artifact)]
    (is (= :decreasing-measure (:strategy termination)))
    (is (= :strictly-decreasing (:direction termination)))
    (is (= :proof/operator-decrease (:proof-ref termination)))
    (is (nil? (:fuel termination)))
    (is (true? (:termination-bounded? proof)))))

(deftest rejects-unbounded-termination
  (is (= "MATH9-TERMINATION"
         (diagnostic-id
          #(bootstrap/math-proof-file-artifact
            (fixture "rejected/math-rewrite-termination.gravity"))))))

(deftest rejects-nonincreasing-measure-without-decrease-proof
  (is (= "MATH9-TERMINATION"
         (diagnostic-id
          #(bootstrap/math-proof-file-artifact
            (fixture "rejected/math-rewrite-nonincreasing-measure.gravity"))))))
