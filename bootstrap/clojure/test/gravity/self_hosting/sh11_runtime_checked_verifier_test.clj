(ns gravity.self-hosting.sh11-runtime-checked-verifier-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.sh09-error-effect-integration-test]
            [gravity.self-hosting.sh11-authenticated-safety-integration-test]))

(defn- resolved-value [name]
  (or
   (some-> (ns-resolve
            'gravity.self-hosting.sh11-authenticated-safety-integration-test
            name)
           var-get)
   (throw
    (ex-info
     "Required SH-11 integration helper is unavailable"
     {:id "SH11-RUNTIME-HELPER"
      :name name}))))

(defn- sh08-products [extension]
  @(resolved-value
    (if (= extension ".gravity")
      'sh08-gravity
      'sh08-qst)))

(defn- error-value [name]
  (or
   (some->
    (ns-resolve
     'gravity.self-hosting.sh09-error-effect-integration-test
     name)
    var-get)
   (throw
    (ex-info
     "Required SH-09 error-effect helper is unavailable"
     {:id "SH11-RUNTIME-ERROR-EFFECT-HELPER"
      :name name}))))

(defn- run-runtime [extension]
  (let [actual-path
        (str "/checkout-a/runtime-checked" extension)
        error-product
        ((error-value 'error-product) extension)
        effect
        {:effected-core
         (get-in error-product [:run :result])
         :verification
         (:verification error-product)}
        ownership
        ((resolved-value 'ownership-products)
         (:ownership (sh08-products extension))
         (str actual-path "/ownership"))
        upstream
        {:effect effect
         :ownership ownership}
        links
        ((resolved-value 'fact-links) upstream)
        safety-operation
        ((resolved-value 'safety-operation)
         :runtime-division links)
        c10 ((resolved-value 'c10-products)
             safety-operation)
        descriptor
        ((resolved-value 'descriptor)
         upstream safety-operation c10
         :runtime-division links actual-path)
        authenticated
        ((resolved-value 'authenticated-envelope)
         descriptor
         (:verification effect)
         (:verification ownership))
        request
        ((resolved-value 'request)
         upstream safety-operation c10
         :runtime-division descriptor
         authenticated actual-path)
        prepared
        {:request request
         :upstream upstream
         :links links
         :safety-operation safety-operation
         :c10 c10
         :descriptor descriptor
         :authenticated authenticated}
        run ((resolved-value 'run-bridge) prepared)
        verification
        ((resolved-value 'invoke)
         (resolved-value 'bridge-plan)
         'sh11-verify-authenticated-safety-result
         [(:request prepared)
          (:c10-request run)
          (:safety-result run)
          (:safety-verification run)
          (:result run)])]
    {:prepared prepared
     :run run
     :verification verification}))

(def ^:private runtime-products
  (into
   {}
   (for [extension [".gravity" ".qst"]]
     [extension (delay (run-runtime extension))])))

(defn- runtime-product [extension]
  @(get runtime-products extension))

(defn- deep-value [depth]
  (loop [remaining depth
         value :leaf]
    (if (zero? remaining)
      value
      (recur (dec remaining) [value]))))

(deftest sh11-runtime-checked-classification-is-accepted-without-host-stack-failure
  (doseq [extension [".gravity" ".qst"]]
    (testing extension
      (let [product (runtime-product extension)
            run (:run product)
            result (:result run)
            verification (:verification product)]
        (is (= :runtime-checked
               (:outcome (:safety-result run))))
        (is (= :passed
               (:status (:safety-verification run))))
        (is (= 1
               (count
                (:runtime-checks
                 (:safety-result run)))))
        (is (= :accepted (:status result)))
        (is (= :runtime-checked (:outcome result)))
        (is (= [] (:diagnostics result)))
        (is (= :passed (:status verification)))
        (is (= result (:expected verification)))
        (is (= result (:candidate verification)))))))

(deftest sh11-runtime-checked-verifier-detects-alteration
  (let [product (runtime-product ".gravity")
        prepared (:prepared product)
        run (:run product)
        result (:result run)
        verify
        (fn [candidate]
          ((resolved-value 'invoke)
           (resolved-value 'bridge-plan)
           'sh11-verify-authenticated-safety-result
           [(:request prepared)
            (:c10-request run)
            (:safety-result run)
            (:safety-verification run)
            candidate]))
        altered-check
        (assoc-in
         result
         [:safety-result :runtime-checks 0 :condition]
         :always-true)
        altered-identity
        (assoc-in
         result
         [:identity-input :outcome]
         :proven-safe)
        altered-status (assoc result :status :rejected)
        extra-field (assoc result :unexpected true)]
    (doseq [[label candidate]
            [[:runtime-check altered-check]
             [:identity altered-identity]
             [:status altered-status]
             [:schema extra-field]]]
      (testing (name label)
        (is (= :rejected
               (:status (verify candidate))))))))

(deftest sh11-verifier-rejects-deep-candidates-without-echoing-them
  (let [accepted-product (runtime-product ".gravity")
        accepted-prepared (:prepared accepted-product)
        accepted-run (:run accepted-product)
        accepted-candidate
        (assoc-in
         (:result accepted-run)
         [:safety-result :hostile-depth]
         (deep-value 256))
        rejected-prepared
        ((resolved-value 'prepared-request)
         (sh08-products ".gravity")
         :unresolved-division
         "/checkout-a/deep-rejected.gravity")
        rejected-run
        ((resolved-value 'run-bridge) rejected-prepared)
        rejected-candidate
        (assoc-in
         (:result rejected-run)
         [:diagnostics 0 :facts :hostile-depth]
         (deep-value 256))
        verify
        (fn [prepared run candidate]
          ((resolved-value 'invoke)
           (resolved-value 'bridge-plan)
           'sh11-verify-authenticated-safety-result
           [(:request prepared)
            (:c10-request run)
            (:safety-result run)
            (:safety-verification run)
            candidate]))]
    (doseq [[label verification]
            [[:accepted-shape
              (verify
               accepted-prepared accepted-run
               accepted-candidate)]
             [:rejected-shape
              (verify
               rejected-prepared rejected-run
               rejected-candidate)]]]
      (testing (name label)
        (is (= :rejected (:status verification)))
        (is (= :candidate-carrier-bound
               (:reason verification)))
        (is (= :carrier-depth-bound
               (get-in
                verification
                [:candidate-preflight :reason])))
        (is (= "STD11-BRIDGE-VERIFY"
               (get-in verification
                       [:diagnostics 0 :rule])))
        (is (= :omitted
               (:candidate-echo verification)))
        (is (not (contains? verification :candidate)))
        (is (not (contains? verification :expected)))))))
