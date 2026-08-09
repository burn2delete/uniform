(ns gravity.c13-optimization-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c13-optimization :as c13]
            [gravity.optimization-lowering :as shared]))

(def module
  {:module 'gravity.c13-test
   :source-path "c13-test.gravity"
   :profile :hosted
   :target :jvm
   :effects #{}
   :capabilities #{}
   :safety :safe
   :metadata {}})

(def c12-artifact
  {:kind :gravity/stage0-c12-domain-ir-architecture-artifact
   :task "P06-D091"
   :artifact-id "sha256:c12"
   :governing-document "docs/c12.md"
   :domain-verifier-report {:status :passed}
   :semantic-anchor-map [{:domain :efir :syntax-id "syntax-0"}]
   :capability-based-proof {:status :complete}
   :domain-ir-artifacts
   [{:source {:syntax-id "syntax-0"
              :span {:source "c13-test.gravity" :form-index 0}}}]})

(defn operations
  ([] (operations module))
  ([module-value]
   {:read-source-form-records
    (fn [_ _] [{:form '(ns gravity.c13-test (:profile :hosted))}])
    :validate-ns-syntax! (fn [_ _] nil)
    :parse-module (fn [_ _] module-value)
    :compiler-c12-domain-ir-source-artifact (fn [_ _] c12-artifact)}))

(deftest contract-is-hosted-compatible-and-nonauthoritative
  (let [contract (c13/c13-engine-contract)
        publics (ns-publics 'gravity.c13-optimization)]
    (is (= :hosted-stage0-c13-optimization (:contract-boundary contract)))
    (is (true? (:compatibility-only? contract)))
    (is (false? (:canonical-c13-authority? contract)))
    (is (false? (:optimization-model-complete? contract)))
    (is (some #{:check-elision-authority} (:does-not-own contract)))
    (is (some #{:target-lowering-authority} (:does-not-own contract)))
    (is (= #{'gravity.digest 'gravity.optimization-lowering}
           (set (get-in contract [:dependency-direction :requires]))))
    (is (true? (get-in contract
                       [:operation-interposition :unknown-keys-rejected?])))
    (is (= (set (keys publics)) (set (keys c13/public-api))))
    (doseq [[name spec] c13/public-api :when (:arglists spec)]
      (is (= (:arglists spec) (:arglists (meta (get publics name))))))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest operation-map-is-validated-and-preserves-nested-interposition
  (doseq [operation-map
          [nil
           {:unknown identity}
           {:c13-optimization-validate! :keyword-is-invokable}
           {:c13-optimization-governing-document ""}
           {:c13-optimization-diagnostic-ids [:bad]}
           {:optimization-lowering-diagnostic-messages {"C13-OK" :bad}}
           {:optimization-pass-contract-seed []}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (c13/with-operations operation-map
                   (constantly :unreachable)))))
  (let [calls (atom 0)
        artifact
        (c13/with-operations
          (assoc (operations)
                 :optimization-decision-record
                 (fn [domain input index contract]
                   (swap! calls inc)
                   (shared/optimization-decision-record
                    domain input index contract)))
          #(c13/compiler-c13-optimization-source-artifact
            "c13-test.gravity" "source"))]
    (is (= 6 @calls))
    (is (= 6 (count (:optimization-decision-log artifact)))))
  (let [sentinel {:kind :sentinel-source}]
    (is (= sentinel
           (c13/with-operations
             {:compiler-c13-optimization-source-artifact
              (fn [_ _] sentinel)}
             #(c13/compiler-c13-optimization-file-artifact
               "bootstrap/clojure/fixtures/accepted/compiler-c13-optimization.gravity"))))))

(deftest optimization-engine-emits-complete-products
  (let [artifact
        (c13/with-operations
          (operations)
          #(c13/compiler-c13-optimization-source-artifact
            "c13-test.gravity" "source"))
        decisions (:optimization-decision-log artifact)]
    (is (= :gravity/stage0-c13-mir-optimization-artifact (:kind artifact)))
    (is (= :gravity/stage0-c12-domain-ir-architecture-artifact
           (:domain-ir-artifact-kind artifact)))
    (is (= 6 (count (:optimization-pass-registry artifact))))
    (is (= 6 (count decisions)))
    (is (= 6 (count (:invalidated-fact-ledger artifact))))
    (is (= 6 (count (:analysis-cache-records artifact))))
    (is (= 6 (count (:proof-and-certificate-usage artifact))))
    (is (= 6 (count (:post-pass-verifier-reports artifact))))
    (is (some seq (map :changed-ops decisions)))
    (is (some empty? (map :changed-ops decisions)))
    (is (= :complete (get-in artifact [:capability-based-proof :status])))
    (is (= (set shared/c13-optimization-diagnostic-ids)
           (set (map :diagnostic
                     (get-in artifact
                             [:optimization-diagnostic-stream
                              :diagnostics])))))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:artifact-id artifact)))))

(deftest diagnostic-overrides-retain-c13-identities
  (doseq [[failure expected-id]
          [[:contract "C13-CONTRACT"]
           [:preserve "C13-PRESERVE"]
           [:invalidate "C13-INVALIDATE"]
           [:proof "C13-PROOF"]
           [:check-elision "C13-CHECK-ELISION"]
           [:effect "C13-EFFECT"]
           [:safety "C13-SAFETY"]
           [:domain "C13-DOMAIN"]
           [:nondeterminism "C13-NONDETERMINISM"]
           [:verify "C13-VERIFY"]]]
    (testing expected-id
      (let [failed-module
            (assoc-in module
                      [:metadata :compiler :c13-optimization :fail]
                      failure)
            error
            (try
              (c13/with-operations
                (operations failed-module)
                #(c13/compiler-c13-optimization-source-artifact
                  "c13-test.gravity" "source"))
              nil
              (catch clojure.lang.ExceptionInfo exception exception))]
        (is (some? error))
        (is (= expected-id (:id (ex-data error))))))))
