(ns gravity.c10-safety-analysis-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c10-safety-analysis :as c10]))

(def module
  {:module 'gravity.c10-test
   :source-path "c10-test.gravity"
   :profile :hosted
   :target :jvm
   :effects #{:io/write}
   :capabilities #{:io/stdout}
   :safety :safe
   :metadata {}})

(def c9-artifact
  {:kind :gravity/stage0-c9-ownership-checker-artifact
   :artifact-id "sha256:c9"
   :capability-based-proof {:status :complete}})

(defn operations
  ([] (operations module))
  ([module-value]
   {:read-source-form-records
    (fn [_ _]
      [{:form '(ns gravity.c10-test (:profile :hosted) (:target :jvm))}])
    :validate-ns-syntax! (fn [_ _] nil)
    :parse-module (fn [_ _] module-value)
    :compiler-c9-ownership-source-artifact (fn [_ _] c9-artifact)}))

(deftest contract-is-hosted-compatible-and-nonauthoritative
  (let [contract (c10/c10-engine-contract)
        publics (ns-publics 'gravity.c10-safety-analysis)]
    (is (= :hosted-stage0-c10-safety-analysis
           (:contract-boundary contract)))
    (is (true? (:compatibility-only? contract)))
    (is (false? (:canonical-c10-authority? contract)))
    (is (false? (:safety-model-complete? contract)))
    (is (some #{:safe1-classification-authority} (:does-not-own contract)))
    (is (some #{:proof-certificate-authority} (:does-not-own contract)))
    (is (true? (get-in contract
                       [:operation-interposition
                        :unknown-keys-rejected?])))
    (is (= #{'clojure.set 'gravity.digest}
           (set (get-in contract [:dependency-direction :requires]))))
    (is (= (set (keys publics)) (set (keys c10/public-api))))
    (doseq [[name spec] c10/public-api :when (:arglists spec)]
      (is (= (:arglists spec) (:arglists (meta (get publics name))))))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest operation-map-is-validated-and-preserves-nested-interposition
  (doseq [operation-map
          [nil
           {:unknown identity}
           {:c10-safety-capability-proof :keyword-is-invokable}
           {:c10-safety-diagnostic-ids [:not-a-string]}
           {:c10-safety-governing-document ""}
           {:c10-safety-rejected-designs [:not-a-map]}
           {:c10-safety-override-diagnostics {:unsafe :not-a-string}}
           {:c10-safe-outcomes #{"not-a-keyword"}}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (c10/with-operations operation-map
                   (constantly :unreachable)))))
  (let [calls (atom 0)
        proof {:operation-inventory-complete? true
               :exactly-one-outcome-per-operation? true
               :runtime-checks-emitted? true
               :proof-obligations-discharged? true
               :certificate-references-recorded? true
               :unsafe-island-audits-complete? true
               :taint-and-capability-reports-complete? true
               :generated-provenance-recorded? true
               :optimization-evidence-preserved? true
               :diagnostics-covered? true
               :verifier-passed? true}]
    (is (= :complete
           (c10/with-operations
             {:c10-safety-capability-proof
              (fn [_] (swap! calls inc) proof)}
             #(c10/c10-safety-validate! "c10-test.gravity" {}))))
    (is (= 1 @calls))))

(deftest safety-engine-emits-complete-products
  (let [artifact (c10/with-operations
                   (operations)
                   #(c10/compiler-c10-safety-source-artifact
                     "c10-test.gravity" "ignored"))
        outcomes (get-in artifact [:safety-outcome-records :records])]
    (is (= :gravity/stage0-c10-safety-analysis-artifact (:kind artifact)))
    (is (= 12 (count (get-in artifact
                             [:safety-operation-inventory :records]))))
    (is (= 12 (count outcomes)))
    (is (= #{:proven-safe :runtime-checked :unsafe-island}
           (set (map :outcome outcomes))))
    (is (= 3 (count (get-in artifact [:runtime-check-list :records]))))
    (is (= 7 (count (get-in artifact [:proof-obligation-list :records]))))
    (is (= 3 (count (get-in artifact
                            [:proof-certificate-references :records]))))
    (is (= 2 (count (get-in artifact
                            [:unsafe-island-audit-manifest :records]))))
    (is (= 1 (count (get-in artifact
                            [:taint-capability-safety-report
                             :taint-records]))))
    (is (= 1 (count (get-in artifact
                            [:taint-capability-safety-report
                             :capability-records]))))
    (is (= 1 (count (get-in artifact
                            [:generated-code-safety-provenance :records]))))
    (is (= 2 (count (get-in artifact
                            [:optimization-safety-preservation :records]))))
    (is (= :passed (get-in artifact [:safety-verifier-report :status])))
    (is (= :complete (get-in artifact [:capability-based-proof :status])))
    (is (= (set c10/c10-safety-diagnostic-ids)
           (set (map :diagnostic
                     (get-in artifact [:safety-diagnostics :diagnostics])))))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:artifact-id artifact)))))

(deftest diagnostic-overrides-retain-c10-identities
  (doseq [[failure expected-id]
          [[:no-outcome "C10-NO-OUTCOME"]
           [:proof "C10-PROOF"]
           [:check "C10-CHECK"]
           [:unsafe "C10-UNSAFE"]
           [:generated "C10-GENERATED"]
           [:taint "C10-TAINT"]
           [:capability "C10-CAPABILITY"]
           [:ffi "C10-FFI"]
           [:numeric "C10-NUMERIC"]
           [:optimization "C10-OPTIMIZATION"]]]
    (testing expected-id
      (let [failed-module (assoc-in module
                                    [:metadata :compiler
                                     :c10-safety-analysis :fail]
                                    failure)
            error (try
                    (c10/with-operations
                      (operations failed-module)
                      #(c10/compiler-c10-safety-source-artifact
                        "c10-test.gravity" "ignored"))
                    nil
                    (catch clojure.lang.ExceptionInfo exception exception))]
        (is (some? error))
        (is (= expected-id (:id (ex-data error))))
        (is (= "C10" (:document-id (ex-data error))))))))
