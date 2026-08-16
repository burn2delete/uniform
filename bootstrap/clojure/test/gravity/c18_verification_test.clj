(ns gravity.c18-verification-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c18-verification :as c18]))

(def module
  {:module 'gravity.c18-test
   :source-path "c18-test.gravity"
   :profile :hosted
   :target :jvm
   :effects #{}
   :capabilities #{}
   :safety :safe
   :metadata {}})

(def c17-artifact
  {:kind :gravity/stage0-c17-compiler-plugin-artifact
   :task "P06-D096"
   :artifact-id "sha256:c17"
   :governing-document "docs/c17.md"
   :capability-based-proof {:status :complete}})

(defn operations
  ([] (operations module))
  ([module-value]
   {:read-source-form-records
    (fn [_ _] [{:form '(ns gravity.c18-test (:profile :hosted))}])
    :validate-ns-syntax! (fn [_ _] nil)
    :parse-module (fn [_ _] module-value)
    :compiler-c17-plugin-source-artifact (fn [_ _] c17-artifact)}))

(deftest contract-is-hosted-and-never-grants-proof-or-release-authority
  (let [contract (c18/c18-engine-contract)
        publics (ns-publics 'gravity.c18-verification)]
    (is (= :hosted-stage0-c18-verification-evidence
           (:contract-boundary contract)))
    (is (true? (:compatibility-only? contract)))
    (is (false? (:canonical-c18-authority? contract)))
    (is (false? (:evidence-authoritative? contract)))
    (is (false? (:release-authority? contract)))
    (is (false? (:verification-model-complete? contract)))
    (is (some #{:proof-checking-authority} (:does-not-own contract)))
    (is (some #{:translation-validation-authority} (:does-not-own contract)))
    (is (some #{:release-gate-authority} (:does-not-own contract)))
    (is (some #{:release-authorization} (:does-not-own contract)))
    (is (= #{'clojure.set 'clojure.string
             'gravity.compiler-verification-shared 'gravity.digest}
           (set (get-in contract [:dependency-direction :requires]))))
    (is (= (set (keys publics)) (set (keys c18/public-api))))
    (doseq [[name spec] c18/public-api :when (:arglists spec)]
      (is (= (:arglists spec) (:arglists (meta (get publics name))))))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest operation-map-is-validated-and-preserves-nested-interposition
  (doseq [operation-map
          [nil
           {:unknown identity}
           {:c18-pass-risk-records :keyword-is-invokable}
           {:c18-verification-governing-document ""}
           {:c18-verification-diagnostic-ids [:bad]}
           {:c18-pass-risk-required-fields [:artifact "bad"]}
           {:c18-trust-report-required-fields []}
           {:compiler-verification-diagnostic-messages {"C18-OK" :bad}}
           {:compiler-verification-override-diagnostics
            {:c18-risk ["C18-RISK" "bad"]}}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (c18/with-operations operation-map
                   (constantly :unreachable)))))
  (let [calls (atom 0)
        original c18/c18-pass-risk-records
        artifact
        (c18/with-operations
          (assoc (operations)
                 :c18-pass-risk-records
                 (fn []
                   (swap! calls inc)
                   (mapv #(assoc % :interposed? true) (original))))
          #(c18/compiler-c18-verification-source-artifact
            "c18-test.gravity" "source"))]
    (is (= 1 @calls))
    (is (every? :interposed? (:pass-risk-classification artifact))))
  (let [sentinel {:kind :sentinel-source}]
    (is (= sentinel
           (c18/with-operations
             {:compiler-c18-verification-source-artifact (fn [_ _] sentinel)}
             #(c18/compiler-c18-verification-file-artifact
               "bootstrap/clojure/fixtures/accepted/compiler-c18-verification.gravity"))))))

(deftest verification-evidence-engine-emits-complete-products
  (let [artifact
        (c18/with-operations
          (operations)
          #(c18/compiler-c18-verification-source-artifact
            "c18-test.gravity" "source"))]
    (is (= :gravity/stage0-c18-compiler-verification-artifact
           (:kind artifact)))
    (is (= :gravity/stage0-c17-compiler-plugin-artifact
           (:plugin-artifact-kind artifact)))
    (is (= 8 (count (:pass-risk-classification artifact))))
    (is (= 8 (count (:pass-evidence-records artifact))))
    (is (= 8 (count (:stage-verifier-reports artifact))))
    (is (= 2 (count (:translation-validation-logs artifact))))
    (is (every? #(= :accepted (:result %))
                (:translation-validation-logs artifact)))
    (is (= 3 (count (:proof-or-certificate-references artifact))))
    (is (= :passed
           (get-in artifact
                   [:differential-and-property-fixture-results :status])))
    (is (= :complete (get-in artifact [:compiler-trust-report :status])))
    (is (= :passed (get-in artifact [:release-gate-report :status])))
    (is (= :blocked
           (get-in artifact
                   [:release-gate-failure-fixtures 0
                    :release-artifact-status])))
    (is (= :captured
           (get-in artifact [:counterexample-artifacts 0 :status])))
    (is (= :passed (get-in artifact [:plugin-evidence-report :status])))
    (is (= :passed
           (get-in artifact [:target-lowering-conformance 0 :status])))
    (is (= (set c18/c18-verification-diagnostic-ids)
           (set (map :diagnostic
                     (get-in artifact
                             [:verification-diagnostic-stream
                              :diagnostics])))))
    (is (= :complete (get-in artifact [:capability-based-proof :status])))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:artifact-id artifact)))))

(deftest diagnostic-overrides-retain-c18-identities
  (doseq [[failure expected-id]
          [[:c18-risk "C18-RISK"]
           [:c18-evidence "C18-EVIDENCE"]
           [:c18-validation "C18-VALIDATION"]
           [:c18-proof "C18-PROOF"]
           [:c18-trust-report "C18-TRUST-REPORT"]
           [:c18-release-gate "C18-RELEASE-GATE"]
           [:c18-counterexample "C18-COUNTEREXAMPLE"]
           [:c18-plugin "C18-PLUGIN"]
           [:c18-backend "C18-BACKEND"]]]
    (testing expected-id
      (let [failed-module
            (assoc-in module [:metadata :compiler :c18-verification :fail]
                      failure)
            error
            (try
              (c18/with-operations
                (operations failed-module)
                #(c18/compiler-c18-verification-source-artifact
                  "c18-test.gravity" "source"))
              nil
              (catch clojure.lang.ExceptionInfo exception exception))]
        (is (some? error))
        (is (= expected-id (:id (ex-data error))))))))
