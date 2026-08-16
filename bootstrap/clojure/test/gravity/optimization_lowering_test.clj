(ns gravity.optimization-lowering-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.optimization-lowering :as shared]))

(def domain-artifact
  {:kind :gravity/stage0-domain-ir-registry
   :semantic-anchor-map [{:domain :efir :syntax-id "syntax-0"}]
   :domain-ir-artifacts
   [{:source {:syntax-id "syntax-0"
              :span {:source "optimization.gravity" :form-index 0}}}]})

(defn operations
  ([] (operations {}))
  ([compiler-metadata]
   {:checked-core-source-artifact
    (fn [_ _] {:module {:metadata {:compiler compiler-metadata}}})
    :domain-ir-source-artifact (fn [_ _] domain-artifact)}))

(deftest contract-is-shared-compatible-and-nonauthoritative
  (let [contract (shared/shared-engine-contract)
        publics (ns-publics 'gravity.optimization-lowering)]
    (is (= :hosted-stage0-optimization-lowering-shared
           (:contract-boundary contract)))
    (is (true? (:compatibility-only? contract)))
    (is (false? (:canonical-authority? contract)))
    (is (some #{:canonical-c13-authority} (:does-not-own contract)))
    (is (some #{:canonical-c14-authority} (:does-not-own contract)))
    (is (= #{'gravity.digest}
           (set (get-in contract [:dependency-direction :requires]))))
    (is (true? (get-in contract
                       [:operation-interposition :unknown-keys-rejected?])))
    (is (= (set (keys publics)) (set (keys shared/public-api))))
    (doseq [[name spec] shared/public-api :when (:arglists spec)]
      (is (= (:arglists spec) (:arglists (meta (get publics name))))))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest operation-map-is-validated-and-preserves-nested-interposition
  (doseq [operation-map
          [nil
           {:unknown identity}
           {:optimization-decision-record :keyword-is-invokable}
           {:c13-optimization-diagnostic-ids [:not-a-string]}
           {:c14-lowering-diagnostic-ids []}
           {:optimization-lowering-diagnostic-ids ["C13-OK" :bad]}
           {:optimization-lowering-diagnostic-messages {"C13-OK" :bad}}
           {:optimization-lowering-override-diagnostics {:contract :bad}}
           {:optimization-pass-contract-seed []}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (shared/with-operations operation-map
                   (constantly :unreachable)))))
  (let [calls (atom [])
        decision
        (shared/with-operations
          {:sha256-hex (fn [value]
                         (swap! calls conj value)
                         "sentinel")}
          #(shared/optimization-decision-record
            domain-artifact "sha256:input" 1
            (shared/optimization-pass-contract-record
             (first shared/optimization-pass-contract-seed))))]
    (is (= "sha256:sentinel" (:decision-id decision)))
    (is (= "sha256:sentinel" (:output-mir decision)))
    (is (= 2 (count @calls)))))

(deftest shared-engine-emits-complete-optimization-and-lowering-products
  (let [artifact
        (shared/with-operations
          (operations)
          #(shared/optimization-lowering-source-artifact
            "optimization.gravity" "source"))]
    (is (= :gravity/stage0-optimization-lowering-artifact (:kind artifact)))
    (is (= :gravity/stage0-domain-ir-registry
           (:domain-ir-artifact-kind artifact)))
    (is (= 6 (count (:optimization-pass-registry artifact))))
    (is (= 6 (count (:optimization-decision-log artifact))))
    (is (= 6 (count (:invalidated-fact-ledger artifact))))
    (is (= 6 (count (:post-pass-verifier-reports artifact))))
    (is (= :deterministic
           (get-in artifact [:optimization-pipeline-manifest :ordering])))
    (is (= :eligible
           (get-in artifact [:target-eligibility-report :status])))
    (is (= :gravity/target-artifact-manifest
           (get-in artifact [:target-artifact-manifest :artifact])))
    (is (= :complete (get-in artifact [:capability-based-proof :status])))
    (is (= (set shared/optimization-lowering-diagnostic-ids)
           (set (get-in artifact
                        [:optimization-lowering-results
                         :required-diagnostic-ids]))))))

(deftest diagnostic-overrides-retain-c13-and-c14-identities
  (doseq [[failure [expected-id _]]
          shared/optimization-lowering-override-diagnostics]
    (testing expected-id
      (let [error
            (try
              (shared/with-operations
                (operations {:optimization-lowering {:fail failure}})
                #(shared/optimization-lowering-source-artifact
                  "optimization.gravity" "source"))
              nil
              (catch clojure.lang.ExceptionInfo exception exception))]
        (is (some? error))
        (is (= expected-id (:id (ex-data error))))))))
