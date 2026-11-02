(ns gravity.c15-diagnostics-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c15-diagnostics :as c15]))

(def module
  {:module 'gravity.c15-test
   :source-path "c15-test.gravity"
   :profile :hosted
   :target :jvm
   :effects #{}
   :capabilities #{}
   :safety :safe
   :metadata {}})

(def c14-artifact
  {:kind :gravity/stage0-c14-target-lowering-artifact
   :task "P06-D093"
   :artifact-id "sha256:c14"
   :governing-document "docs/c14.md"
   :target-artifact-manifest {:artifact :gravity/target-artifact-manifest}
   :capability-based-proof {:status :complete}})

(defn operations
  ([] (operations module))
  ([module-value]
   {:read-source-form-records
    (fn [_ _] [{:form '(ns gravity.c15-test (:profile :hosted))}])
    :validate-ns-syntax! (fn [_ _] nil)
    :parse-module (fn [_ _] module-value)
    :compiler-c14-lowering-source-artifact (fn [_ _] c14-artifact)}))

(deftest contract-is-hosted-compatible-and-nonauthoritative
  (let [contract (c15/c15-engine-contract)
        publics (ns-publics 'gravity.c15-diagnostics)]
    (is (= :hosted-stage0-c15-compiler-diagnostics
           (:contract-boundary contract)))
    (is (true? (:compatibility-only? contract)))
    (is (false? (:canonical-c15-authority? contract)))
    (is (false? (:diagnostic-system-complete? contract)))
    (is (some #{:redaction-policy-authority} (:does-not-own contract)))
    (is (some #{:renderer-authority} (:does-not-own contract)))
    (is (= #{'clojure.string 'gravity.compiler-verification-shared
             'gravity.digest}
           (set (get-in contract [:dependency-direction :requires]))))
    (is (true? (get-in contract
                       [:operation-interposition :unknown-keys-rejected?])))
    (is (= (set (keys publics)) (set (keys c15/public-api))))
    (doseq [[name spec] c15/public-api :when (:arglists spec)]
      (is (= (:arglists spec) (:arglists (meta (get publics name))))))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest operation-map-is-validated-and-preserves-variadic-interposition
  (doseq [operation-map
          [nil
           {:unknown identity}
           {:c15-diagnostic-record :keyword-is-invokable}
           {:c15-diagnostics-governing-document ""}
           {:c15-diagnostics-diagnostic-ids [:bad]}
           {:c15-diagnostic-required-fields [:artifact "bad"]}
           {:compiler-verification-diagnostic-messages {"C15-OK" :bad}}
           {:compiler-verification-override-diagnostics
            {:c15-schema ["C15-SCHEMA" "bad"]}}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (c15/with-operations operation-map
                   (constantly :unreachable)))))
  (let [received (atom nil)
        sentinel {:artifact :sentinel}]
    (is (= sentinel
           (c15/with-operations
             {:c15-diagnostic-record
              (fn [& args]
                (reset! received args)
                sentinel)}
             #(c15/c15-diagnostic-record
               "C15-SCHEMA" :error :c15 "message" "c15.gravity" 0
               "artifact" {:field :schema} [{:kind :repair}]
               :generated? true :related [{:role :generated-by}]))))
    (is (= [:generated? true :related [{:role :generated-by}]]
           (vec (drop 9 @received)))))
  (let [sentinel {:kind :sentinel-source}]
    (is (= sentinel
           (c15/with-operations
             {:compiler-c15-diagnostics-source-artifact (fn [_ _] sentinel)}
             #(c15/compiler-c15-diagnostics-file-artifact
               "bootstrap/clojure/fixtures/accepted/compiler-c15-diagnostics.gravity"))))))

(deftest diagnostics-engine-emits-complete-products
  (let [artifact
        (c15/with-operations
          (operations)
          #(c15/compiler-c15-diagnostics-source-artifact
            "c15-test.gravity" "source"))
        diagnostics (get-in artifact [:diagnostic-stream :diagnostics])]
    (is (= :gravity/stage0-c15-compiler-diagnostics-artifact
           (:kind artifact)))
    (is (= :gravity/stage0-c14-target-lowering-artifact
           (:lowering-artifact-kind artifact)))
    (is (= :complete (get-in artifact [:diagnostic-schema :status])))
    (is (= 4 (count diagnostics)))
    (is (= diagnostics (vec (sort-by :ordering-key diagnostics))))
    (is (every? #(re-matches #"diag-[0-9a-f]{64}" (:diagnostic-id %))
                diagnostics))
    (is (= 9 (count (get-in artifact [:diagnostic-catalog :rules]))))
    (is (= 9 (count (:remediation-and-quick-fix-records artifact))))
    (is (true? (get-in artifact [:redaction-report :public-safe?])))
    (is (= #{:cli :ide :ci :safety-report :package-report}
           (set (map :renderer (:rendering-records artifact)))))
    (is (= 9 (count (:golden-diagnostic-fixtures artifact))))
    (is (= :complete (get-in artifact [:capability-based-proof :status])))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:artifact-id artifact)))))

(deftest diagnostic-overrides-retain-c15-identities
  (doseq [[failure expected-id]
          [[:c15-schema "C15-SCHEMA"]
           [:c15-id "C15-ID"]
           [:c15-span "C15-SPAN"]
           [:c15-origin "C15-ORIGIN"]
           [:c15-facts "C15-FACTS"]
           [:c15-remediation "C15-REMEDIATION"]
           [:c15-redaction "C15-REDACTION"]
           [:c15-order "C15-ORDER"]
           [:c15-golden "C15-GOLDEN"]]]
    (testing expected-id
      (let [failed-module
            (assoc-in module [:metadata :compiler :c15-diagnostics :fail]
                      failure)
            error
            (try
              (c15/with-operations
                (operations failed-module)
                #(c15/compiler-c15-diagnostics-source-artifact
                  "c15-test.gravity" "source"))
              nil
              (catch clojure.lang.ExceptionInfo exception exception))]
        (is (some? error))
        (is (= expected-id (:id (ex-data error))))))))
