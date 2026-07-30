(ns gravity.self-hosting.sh09-error-effect-integration-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.sh09-authenticated-effect-integration-test]))

(defn- resolved-value [name]
  (or
   (some-> (ns-resolve
            'gravity.self-hosting.sh09-authenticated-effect-integration-test
            name)
           var-get)
   (throw
    (ex-info
     "Required SH-09 integration helper is unavailable"
     {:id "SH09-ERROR-HELPER"
      :name name}))))

(defn- sh08-products [extension]
  @(resolved-value
    (if (= extension ".gravity")
      'sh08-gravity
      'sh08-qst)))

(defn- run-error-effect [extension]
  (let [prepared
        ((resolved-value 'prepared-request)
         (sh08-products extension)
         :error-raise
         (str "/checkout-a/error-effect" extension))
        run ((resolved-value 'run-bridge) prepared)
        verification
        ((resolved-value 'invoke)
         (resolved-value 'bridge-plan)
         'sh09-verify-authenticated-effect-result
         [(:request prepared)
          (:c8-request run)
          (:effect-result run)
          (:effect-verification run)
          (:result run)])]
    {:prepared prepared
     :run run
     :verification verification}))

(def ^:private error-products
  (into
   {}
   (for [extension [".gravity" ".qst"]]
     [extension (delay (run-error-effect extension))])))

(defn- error-product [extension]
  @(get error-products extension))

(deftest sh09-authenticates-declared-language-error-effects
  (doseq [extension [".gravity" ".qst"]]
    (testing extension
      (let [product (error-product extension)
            request
            (get-in product
                    [:prepared :request])
            run (:run product)
            c8-request (:c8-request run)
            effect-result (:effect-result run)
            result (:result run)
            verification (:verification product)]
        (is (= :accepted (:status c8-request)))
        (is (= :error/raise
               (get-in c8-request
                       [:effect-request :effect])))
        (is (= :declared
               (get-in c8-request
                       [:effect-request
                        :authority-mode])))
        (is (nil?
             (get-in c8-request
                     [:effect-request
                      :required-capability])))
        (is (nil?
             (get-in c8-request
                     [:effect-request :provider])))
        (is (nil?
             (get-in c8-request
                     [:effect-request :grant])))
        (is (= :accepted (:status effect-result)))
        (is (= #{:error/raise}
               (:direct-effects effect-result)))
        (is (= #{:error/raise}
               (:residual-effects effect-result)))
        (is (nil? (:capability-proof effect-result)))
        (is (= :passed
               (:status (:effect-verification run))))
        (is (= :accepted (:status result)))
        (is (= :error/raise
               (get-in result
                       [:effect-result :effect])))
        (is (= :passed (:status verification)))
        (is (= result (:expected verification)))
        (is (= result (:candidate verification)))
        (is (= :error-raise (:operation request)))))))

(deftest sh09-error-effect-authority-fails-closed
  (let [product (error-product ".gravity")
        request
        (get-in product [:prepared :request])
        provider
        {:id :unexpected/provider
         :effects #{:error/raise}
         :capabilities #{}
         :profiles #{:meta}
         :targets #{:jvm}
         :phases #{:build}}
        candidates
        [[:undeclared
          (assoc-in
           request
           [:effect-authority
            :declared-effects]
           #{})]
         [:capability
          (assoc-in
           request
           [:effect-authority
            :required-capability]
           :error/raise)]
         [:provider
          (assoc-in
           request
           [:effect-authority :provider]
           provider)]
         [:authority-mode
          (assoc-in
           request
           [:effect-authority
            :authority-mode]
           :explicit)]]]
    (doseq [[label candidate] candidates]
      (testing (name label)
        (let [result
              ((resolved-value 'invoke)
               (resolved-value 'bridge-plan)
               'sh09-build-authenticated-effect-request
               [candidate])]
          (is (= :rejected (:status result)))
          (is (= "STD09-BRIDGE-EFFECT"
                 (get-in result
                         [:diagnostics 0 :rule]))))))))
