(ns gravity.self-hosting.sh22-utf8-byte-slice-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

(def ^:private engine
  (delay
    (let [source (.getCanonicalPath
                  (io/file "bootstrap/gravity/src/gravity/stdlib/self_hosting_core.gravity"))
          emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source (slurp source)))))

(defn- call [function & args]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh22-standard-library-leaf :compiler-artifact-plan? true}
   @engine function (vec args)))

(def ^:private validate-request
  {:artifact :gravity/sh22-library-request
   :request-id :validate :profile :meta :experimental-opt-in true
   :operation :utf8-validate :arguments [[65 240 159 152 128 66]]
   :effects #{:memory/allocate} :capabilities #{} :allocation-policy :bounded-by-input
   :source-span {:source-id :byte-slice-fixture :start 0 :end 6}
   :origin-chain [{:kind :source :source-id :byte-slice-fixture}]
   :actual-source-path "bootstrap/clojure/test/gravity/self_hosting/sh22_utf8_byte_slice_test.clj"})

(defn- slice-request [value start end]
  (assoc validate-request :request-id :slice :operation :utf8-byte-slice
         :allocation-policy :bounded-by-result :arguments [value start end]))

(deftest byte-slice-contract
  (let [policy (call 'sh22-library-policy)
        exports (:exports (call 'sh22-module-manifest))
        matches (filter #(= :utf8-byte-slice (:name %)) exports)]
    (is (contains? (:diagnostics policy) "STD4002"))
    (is (contains? (:operation-set policy) :utf8-byte-slice))
    (is (= 1 (count matches)))
    (is (= {:family :text :profiles #{:meta} :stability :experimental
            :effects #{:memory/allocate} :capabilities #{} :allocation :bounded-by-result}
           (select-keys (first matches) [:family :profiles :stability :effects
                                         :capabilities :allocation])))
    (is (= #{"STD4001" "STD4002" "STD10003"} (set (:errors (first matches)))))))

(deftest byte-slice-validator-composition-and-replay
  (let [validated (call 'sh22-handle-request validate-request)
        value (:value validated)]
    (is (= :accepted (:status validated)))
    (doseq [[start end bytes scalars] [[1 5 [240 159 152 128] 1]
                                       [6 6 [] 0]
                                       [0 6 [65 240 159 152 128 66] 3]]]
      (let [request (slice-request value start end)
            result (call 'sh22-handle-request request)]
        (is (= :accepted (:status result)))
        (is (= {:artifact :gravity/validated-utf8 :encoding :utf-8
                :bytes bytes :byte-count (count bytes) :scalar-count scalars}
               (:value result)))
        (is (= (:arguments request) (get-in result [:identity-input :arguments])))
        (is (= (select-keys request [:source-span :origin-chain :actual-source-path])
               (:provenance result)))
        (is (= :passed (:status (call 'sh22-verify-result request result))))))
    (let [request (slice-request value 1 5)
          result (call 'sh22-handle-request request)
          changed (assoc-in result [:value :bytes 3] 129)
          replay (call 'sh22-verify-result request changed)]
      (is (= :rejected (:status replay)))
      (is (= :library-result-substitution (get-in replay [:diagnostics 0 :reason]))))))

(deftest byte-slice-checked-failures
  (let [value (:value (call 'sh22-handle-request validate-request))
        base (slice-request value 1 5)]
    (doseq [[request rule]
            (concat
             (for [[start end] [[2 5] [1 4] [3 3] [5 1] [-1 1] [1 7] [nil 1] [0 :end]]]
               [(slice-request value start end) "STD4002"])
             (for [invalid [(assoc-in value [:bytes 5] 255)
                            (assoc value :byte-count 5) (assoc value :scalar-count 4)
                            (assoc value :encoding :ascii) (dissoc value :artifact)]]
               [(slice-request invalid 1 5) "STD4001"])
             [[(assoc base :arguments [value]) "STD1001"]
              [(assoc base :effects #{}) "STD1002"]
              [(assoc base :capabilities #{:io/stdout}) "STD1003"]
              [(assoc base :allocation-policy :none) "STD1001"]
              [(assoc base :experimental-opt-in false) "STD1001"]
              [(assoc base :profile :core) "STD1001"]])]
      (let [result (call 'sh22-handle-request request)]
        (is (= :rejected (:status result)))
        (is (= rule (get-in result [:diagnostics 0 :rule])))
        (is (= :passed (:status (call 'sh22-verify-result request result))))))))
