(ns gravity.c12-domain-ir-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c12-domain-ir :as c12]))

(def module
  {:module 'gravity.c12-test
   :source-path "c12-test.gravity"
   :profile :hosted
   :target :jvm
   :effects #{}
   :capabilities #{}
   :safety :safe
   :metadata {}})

(def diagnostic-ids
  ["C12-REGISTRATION" "C12-ANCHOR" "C12-SCHEMA" "C12-FACTS"
   "C12-VERIFY" "C12-PROOF" "C12-LOWERING" "C12-FALLBACK"
   "C12-PLUGIN"])

(def diagnostic-messages
  (into {} (map (fn [id] [id (str "message " id)]) diagnostic-ids)))

(def registry-seed
  [{:domain :efir :owner-doc "MATH3" :entry-passes [:build]
    :exit-passes [:lower] :target-lowerings #{:jvm} :fallback :mir}
   {:domain :schema :owner-doc "S1" :entry-passes [:build]
    :exit-passes [:lower] :target-lowerings #{} :fallback :runtime}])

(def c11-artifact
  {:kind :gravity/stage0-c11-mir-spec-artifact
   :task "P06-D090"
   :artifact-id "sha256:c11"
   :governing-document "docs/c11.md"
   :mir-module {:target-request :jvm}
   :mir-verifier-report {:status :passed}
   :capability-based-proof {:status :complete}})

(defn operations
  ([] (operations module nil))
  ([module-value failure-id]
   {:read-source-form-records
    (fn [_ _] [{:form '(ns gravity.c12-test (:profile :hosted))}])
    :validate-ns-syntax! (fn [_ _] nil)
    :parse-module (fn [_ _] module-value)
    :compiler-c11-mir-source-artifact (fn [_ _] c11-artifact)
    :domain-ir-validate-overrides!
    (fn [_ _]
      (when failure-id
        (throw (ex-info "domain override" {:id failure-id}))))
    :domain-ir-registration-record
    (fn [seed]
      (assoc seed :artifact :gravity/domain-ir-registration
             :schema (str "sha256:" (name (:domain seed)))))
    :domain-ir-artifact-record
    (fn [_ registration index]
      {:artifact :gravity/domain-ir
       :domain (:domain registration)
       :artifact-id (str "sha256:domain-" index)
       :source {:syntax-id (str "syntax-" index)
                :span {:source "c12-test.gravity" :form-index index}
                :origin-chain []}
       :semantic-anchor {:mir-ops [(str "mir-" index)]
                         :typed-core [(str "core-" index)]}
       :profile :hosted :target-request :jvm
       :facts {:types :types :effects :effects :ownership :ownership
               :capabilities :capabilities :safety :safety
               :provenance :provenance}
       :verifier {:result :accepted}
       :proofs [{:status :accepted}]
       :lowering-status :eligible})
    :domain-ir-validate! (fn [_ _] :complete)
    :domain-ir-capability-proof (fn [_] {:status :complete})
    :domain-ir-diagnostic-ids diagnostic-ids
    :domain-ir-diagnostic-messages diagnostic-messages
    :domain-ir-required-families [:efir :schema]
    :domain-ir-registry-seed registry-seed}))

(deftest contract-is-hosted-compatible-and-nonauthoritative
  (let [contract (c12/c12-engine-contract)
        publics (ns-publics 'gravity.c12-domain-ir)]
    (is (= :hosted-stage0-c12-domain-ir (:contract-boundary contract)))
    (is (true? (:compatibility-only? contract)))
    (is (false? (:canonical-c12-authority? contract)))
    (is (false? (:domain-model-complete? contract)))
    (is (some #{:domain-verifier-authority} (:does-not-own contract)))
    (is (some #{:plugin-policy-authority} (:does-not-own contract)))
    (is (true? (get-in contract [:operation-interposition
                                 :unknown-keys-rejected?])))
    (is (= #{'gravity.digest}
           (set (get-in contract [:dependency-direction :requires]))))
    (is (= (set (keys publics)) (set (keys c12/public-api))))
    (doseq [[name spec] c12/public-api :when (:arglists spec)]
      (is (= (:arglists spec) (:arglists (meta (get publics name))))))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest operation-map-is-validated-and-supports-interposition
  (doseq [operation-map
          [nil {:unknown identity} {:domain-ir-validate! :keyword}
           {:c12-domain-ir-governing-document ""}
           {:domain-ir-diagnostic-ids [:bad]}
           {:domain-ir-diagnostic-messages {"id" :bad}}
           {:domain-ir-required-families [:efir "bad"]}
           {:domain-ir-registry-seed []}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (c12/with-operations operation-map
                   (constantly :unreachable)))))
  (let [calls (atom 0)]
    (c12/with-operations
      {:domain-ir-validate-overrides! (fn [_ _] (swap! calls inc))}
      #(c12/c12-domain-ir-validate-source-overrides! "c12.gravity" {}))
    (is (= 1 @calls))))

(deftest domain-ir-engine-emits-complete-products
  (let [artifact (c12/with-operations
                   (operations)
                   #(c12/compiler-c12-domain-ir-source-artifact
                     "c12-test.gravity" "ignored"))]
    (is (= :gravity/stage0-c12-domain-ir-architecture-artifact
           (:kind artifact)))
    (is (= 2 (count (:domain-ir-registry artifact))))
    (is (= 2 (count (:domain-ir-artifacts artifact))))
    (is (= 2 (count (:semantic-anchor-map artifact))))
    (is (= 2 (count (:entry-pass-records artifact))))
    (is (= 2 (count (:exit-pass-records artifact))))
    (is (= 2 (count (:proof-and-certificate-references artifact))))
    (is (= 2 (count (:lowering-eligibility-matrix artifact))))
    (is (= 2 (count (:fallback-records artifact))))
    (is (= :passed (get-in artifact [:domain-verifier-report :status])))
    (is (= diagnostic-ids
           (get-in artifact [:c12-domain-ir-results
                             :required-diagnostic-ids])))
    (is (= (set diagnostic-ids)
           (set (map :diagnostic
                     (get-in artifact
                             [:domain-ir-diagnostic-stream :diagnostics])))))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:artifact-id artifact)))))

(deftest diagnostic-overrides-retain-c12-identities
  (doseq [expected-id diagnostic-ids]
    (testing expected-id
      (let [error (try
                    (c12/with-operations
                      (operations module expected-id)
                      #(c12/compiler-c12-domain-ir-source-artifact
                        "c12-test.gravity" "ignored"))
                    nil
                    (catch clojure.lang.ExceptionInfo exception exception))]
        (is (= expected-id (:id (ex-data error))))))))
