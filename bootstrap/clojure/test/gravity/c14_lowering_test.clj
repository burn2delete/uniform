(ns gravity.c14-lowering-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c14-lowering :as c14]
            [gravity.optimization-lowering :as shared]))

(def module
  {:module 'gravity.c14-test
   :source-path "c14-test.gravity"
   :profile :hosted
   :target :jvm
   :effects #{}
   :capabilities #{:io/stdout}
   :safety :safe
   :metadata {}})

(def c13-artifact
  {:kind :gravity/stage0-c13-mir-optimization-artifact
   :task "P06-D092"
   :artifact-id "sha256:c13"
   :governing-document "docs/c13.md"
   :optimized-mir-artifact
   {:artifact :gravity/optimized-mir
    :output "sha256:optimized-mir"
    :source-origin-map []
    :status :complete}
   :capability-based-proof {:status :complete}})

(defn operations
  ([] (operations module))
  ([module-value]
   {:read-source-form-records
    (fn [_ _] [{:form '(ns gravity.c14-test (:profile :hosted))}])
    :validate-ns-syntax! (fn [_ _] nil)
    :parse-module (fn [_ _] module-value)
    :compiler-c13-optimization-source-artifact (fn [_ _] c13-artifact)}))

(deftest contract-is-hosted-compatible-and-nonauthoritative
  (let [contract (c14/c14-engine-contract)
        publics (ns-publics 'gravity.c14-lowering)]
    (is (= :hosted-stage0-c14-target-lowering
           (:contract-boundary contract)))
    (is (true? (:compatibility-only? contract)))
    (is (false? (:canonical-c14-authority? contract)))
    (is (false? (:lowering-model-complete? contract)))
    (is (some #{:abi-authority} (:does-not-own contract)))
    (is (some #{:runtime-provider-authority} (:does-not-own contract)))
    (is (= #{'gravity.digest 'gravity.optimization-lowering}
           (set (get-in contract [:dependency-direction :requires]))))
    (is (true? (get-in contract
                       [:operation-interposition :unknown-keys-rejected?])))
    (is (= (set (keys publics)) (set (keys c14/public-api))))
    (doseq [[name spec] c14/public-api :when (:arglists spec)]
      (is (= (:arglists spec) (:arglists (meta (get publics name))))))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest operation-map-is-validated-and-preserves-nested-interposition
  (doseq [operation-map
          [nil
           {:unknown identity}
           {:c14-lowering-validate! :keyword-is-invokable}
           {:c14-lowering-governing-document ""}
           {:c14-lowering-diagnostic-ids [:bad]}
           {:optimization-lowering-diagnostic-messages {"C14-OK" :bad}}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (c14/with-operations operation-map
                   (constantly :unreachable)))))
  (let [calls (atom 0)
        artifact
        (c14/with-operations
          (assoc (operations)
                 :c14-lowering-diagnostic-catalog
                 (fn [source-path input-id]
                   (swap! calls inc)
                   {:artifact :gravity/c14-lowering-diagnostic-catalog
                    :status :complete
                    :diagnostics
                    (mapv (fn [id]
                            {:diagnostic id
                             :input-artifact-id input-id
                             :source-span {:source source-path
                                           :form-index 0}})
                          shared/c14-lowering-diagnostic-ids)}))
          #(c14/compiler-c14-lowering-source-artifact
            "c14-test.gravity" "source"))]
    (is (= 1 @calls))
    (is (= (set shared/c14-lowering-diagnostic-ids)
           (set (map :diagnostic
                     (get-in artifact
                             [:lowering-diagnostic-stream :diagnostics]))))))
  (let [sentinel {:kind :sentinel-source}]
    (is (= sentinel
           (c14/with-operations
             {:compiler-c14-lowering-source-artifact (fn [_ _] sentinel)}
             #(c14/compiler-c14-lowering-file-artifact
               "bootstrap/clojure/fixtures/accepted/compiler-c14-lowering.gravity"))))))

(deftest lowering-engine-emits-complete-products
  (let [artifact
        (c14/with-operations
          (operations)
          #(c14/compiler-c14-lowering-source-artifact
            "c14-test.gravity" "source"))]
    (is (= :gravity/stage0-c14-target-lowering-artifact (:kind artifact)))
    (is (= :gravity/stage0-c13-mir-optimization-artifact
           (:optimization-artifact-kind artifact)))
    (is (= :optimized-mir (get-in artifact [:lowering-request :input :kind])))
    (is (= :eligible
           (get-in artifact [:target-eligibility-report :status])))
    (is (= :complete (get-in artifact [:abi-manifest :status])))
    (is (= :complete
           (get-in artifact [:runtime-provider-manifest :status])))
    (is (= 3 (count (:provider-selection-records artifact))))
    (is (= 3 (count (get-in artifact
                             [:proof-to-target-metadata-map :entries]))))
    (is (= :gravity/target-artifact-manifest
           (get-in artifact [:target-artifact-manifest :artifact])))
    (is (= :complete (get-in artifact [:capability-based-proof :status])))
    (is (= (set shared/c14-lowering-diagnostic-ids)
           (set (map :diagnostic
                     (get-in artifact
                             [:lowering-diagnostic-stream :diagnostics])))))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:artifact-id artifact)))))

(deftest diagnostic-overrides-retain-c14-identities
  (doseq [[failure expected-id]
          [[:input "C14-INPUT"]
           [:profile "C14-PROFILE"]
           [:target "C14-TARGET"]
           [:abi "C14-ABI"]
           [:runtime "C14-RUNTIME"]
           [:provider "C14-PROVIDER"]
           [:proof-metadata "C14-PROOF-METADATA"]
           [:capability "C14-CAPABILITY"]
           [:unsupported "C14-UNSUPPORTED"]
           [:manifest "C14-MANIFEST"]]]
    (testing expected-id
      (let [failed-module
            (assoc-in module [:metadata :compiler :c14-lowering :fail] failure)
            error
            (try
              (c14/with-operations
                (operations failed-module)
                #(c14/compiler-c14-lowering-source-artifact
                  "c14-test.gravity" "source"))
              nil
              (catch clojure.lang.ExceptionInfo exception exception))]
        (is (some? error))
        (is (= expected-id (:id (ex-data error))))))))
