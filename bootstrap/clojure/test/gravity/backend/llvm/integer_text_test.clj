(ns gravity.backend.llvm.integer-text-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

; Current source execution is development evidence, not migration authority.
(def ^:private compiler-plan
  (delay
    (let [source (.getCanonicalPath
                  (io/file "language/compiler/src/gravity/backend/b3_llvm_backend_design.gravity"))
          emitter (:emitter
                   (bootstrap/c-backend-stage2-plan-emitter-source-rule! source :c))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source (slurp source)))))

(defn- invoke [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-llvm-integer-text-development :compiler-artifact-plan? true}
   @compiler-plan function arguments))

(deftest decimal-text-computes-signed-digits
  (doseq [[value expected]
          [[0 "0"] [9 "9"] [10 "10"] [99 "99"] [100 "100"]
           [-9 "-9"] [-10 "-10"] [-99 "-99"] [-100 "-100"]
           [Long/MIN_VALUE "-9223372036854775808"]
           [Long/MAX_VALUE "9223372036854775807"]]]
    (is (= expected (invoke 'llvm-integer-text [value])))))

(deftest decimal-text-rejects-nonintegers-without-coercion
  (doseq [value [nil true false "12" :twelve 1.5 [] {}]]
    (let [result (invoke 'llvm-integer-text [value])]
      (is (= :rejected (:status result)))
      (is (= "B3-NUMERIC" (:diagnostic result)))
      (is (= :integer-text-input (:missing-fact result))))))

(deftest value-references-compute-multidigit-ordinals
  (doseq [[ordinal expected] [[0 "%v0"] [9 "%v9"] [10 "%v10"]
                            [99 "%v99"] [100 "%v100"]]]
    (is (= expected (invoke 'b3-value-reference [{"value" ordinal} "value"])))))
