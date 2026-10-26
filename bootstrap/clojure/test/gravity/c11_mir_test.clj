(ns gravity.c11-mir-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c11-mir :as c11]))

(def module
  {:module 'gravity.c11-test
   :source-path "c11-test.gravity"
   :profile :hosted
   :target :jvm
   :effects #{:io/write}
   :capabilities #{:io/stdout}
   :safety :safe
   :metadata {}})

(def c10-artifact
  {:kind :gravity/stage0-c10-safety-analysis-artifact
   :artifact-id "sha256:c10"
   :safety-outcome-records
   {:records [{:operation "op-safe"
               :outcome :proven-safe
               :source {:origin-chain []}
               :proof "proof-safe"}]}
   :runtime-check-list {:records []}
   :proof-certificate-references {:records []}
   :capability-based-proof {:status :complete}})

(defn operations
  ([] (operations module))
  ([module-value]
   {:read-source-form-records
    (fn [_ _]
      [{:form '(ns gravity.c11-test (:profile :hosted) (:target :jvm))}])
    :validate-ns-syntax! (fn [_ _] nil)
    :parse-module (fn [_ _] module-value)
    :compiler-c10-safety-source-artifact (fn [_ _] c10-artifact)}))

(deftest contract-is-hosted-compatible-and-nonauthoritative
  (let [contract (c11/c11-engine-contract)
        publics (ns-publics 'gravity.c11-mir)]
    (is (= :hosted-stage0-c11-mir (:contract-boundary contract)))
    (is (true? (:compatibility-only? contract)))
    (is (false? (:canonical-c11-authority? contract)))
    (is (false? (:mir-model-complete? contract)))
    (is (some #{:canonical-mir-verifier-authority} (:does-not-own contract)))
    (is (some #{:target-lowering-authority} (:does-not-own contract)))
    (is (true? (get-in contract
                       [:operation-interposition
                        :unknown-keys-rejected?])))
    (is (= #{'clojure.string 'gravity.digest}
           (set (get-in contract [:dependency-direction :requires]))))
    (is (= (set (keys publics)) (set (keys c11/public-api))))
    (doseq [[name spec] c11/public-api :when (:arglists spec)]
      (is (= (:arglists spec) (:arglists (meta (get publics name))))))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest operation-map-is-validated-and-preserves-nested-interposition
  (doseq [operation-map
          [nil
           {:unknown identity}
           {:c11-family-effects :keyword-is-invokable}
           {:c11-mir-diagnostic-ids [:not-a-string]}
           {:c11-mir-governing-document ""}
           {:c11-mir-required-operation-families [:constant "not-keyword"]}
           {:c11-mir-rejected-designs [:not-a-map]}
           {:c11-mir-override-diagnostics {:verify :not-a-string}}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (c11/with-operations operation-map
                   (constantly :unreachable)))))
  (let [calls (atom [])
        operation
        (c11/with-operations
          {:c11-family-effects
           (fn [family]
             (swap! calls conj family)
             #{:interposed/effect})}
          #(c11/c11-mir-operation
            module {:source "c11-test.gravity" :form-index 0}
            [{:operation "op-safe" :proof "proof-safe"}]
            0 :constant))]
    (is (= #{:interposed/effect} (:effects operation)))
    (is (= [:constant] @calls))))

(deftest mir-engine-emits-complete-products
  (let [artifact (c11/with-operations
                   (operations)
                   #(c11/compiler-c11-mir-source-artifact
                     "c11-test.gravity" "ignored"))
        operations (:mir-operations artifact)]
    (is (= :gravity/stage0-c11-mir-spec-artifact (:kind artifact)))
    (is (= (count c11/c11-mir-required-operation-families)
           (count operations)))
    (is (= (dec (count operations)) (count (:data-flow-graph artifact))))
    (is (= (set c11/c11-mir-required-operation-families)
           (set (map :family operations))))
    (is (every? :type operations))
    (is (every? #(or (empty? (:effects %))
                     (not= :none (:ordering %)))
                operations))
    (is (= 1 (count (:domain-anchor-table artifact))))
    (is (= :passed (get-in artifact [:mir-verifier-report :status])))
    (is (= :complete (get-in artifact [:capability-based-proof :status])))
    (is (= (set c11/c11-mir-diagnostic-ids)
           (set (map :diagnostic
                     (get-in artifact
                             [:mir-diagnostic-stream :diagnostics])))))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:artifact-id artifact)))))

(deftest diagnostic-overrides-retain-c11-identities
  (doseq [[failure expected-id]
          [[:module "C11-MODULE"]
           [:block "C11-BLOCK"]
           [:dominance "C11-DOMINANCE"]
           [:type "C11-TYPE"]
           [:effect "C11-EFFECT"]
           [:safety "C11-SAFETY"]
           [:origin "C11-ORIGIN"]
           [:domain "C11-DOMAIN"]
           [:target-leak "C11-TARGET-LEAK"]
           [:verify "C11-VERIFY"]]]
    (testing expected-id
      (let [failed-module (assoc-in module
                                    [:metadata :compiler :c11-mir-spec :fail]
                                    failure)
            error (try
                    (c11/with-operations
                      (operations failed-module)
                      #(c11/compiler-c11-mir-source-artifact
                        "c11-test.gravity" "ignored"))
                    nil
                    (catch clojure.lang.ExceptionInfo exception exception))]
        (is (some? error))
        (is (= expected-id (:id (ex-data error))))
        (is (= "C11" (:document-id (ex-data error))))))))

(defn- hosted-artifact []
  (c11/with-operations (operations)
    #(c11/compiler-c11-mir-source-artifact "c11-test.gravity" "ignored")))

(defn- recomputed-report [artifact module flow]
  (c11/c11-mir-verifier-report module (:mir-operations artifact) flow
    (:domain-anchor-table artifact) (:mir-diagnostic-stream artifact)))

(defn- validation-error [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo e (ex-data e))))

(deftest failed-dominance-reaches-hosted-report-proof-and-validation
  ;; These are the existing hosted compatibility records, not canonical MIR
  ;; verification or public source/artifact/report admission.
  (let [artifact (hosted-artifact)
        flow (assoc-in (:data-flow-graph artifact) [0 :dominance-status] :failed)
        report (recomputed-report artifact (:mir-module artifact) flow)
        changed (assoc artifact :data-flow-graph flow :mir-verifier-report report)]
    (is (seq (:data-flow-graph artifact)))
    (is (true? (get-in artifact [:mir-verifier-report :dominance-valid?])))
    (is (false? (:dominance-valid? report)))
    (is (= :failed (:status report)))
    (is (false? (:dominance-valid? (c11/c11-mir-capability-proof changed))))
    (doseq [attempt [#(c11/c11-mir-validate! "c11-test.gravity" changed)
                     #(c11/with-operations
                        (assoc (operations) :c11-data-flow-graph (constantly flow))
                        (fn [] (c11/compiler-c11-mir-source-artifact "c11-test.gravity" "ignored")))
                     #(c11/c11-mir-validate! "c11-test.gravity"
                        (update artifact :mir-verifier-report dissoc :dominance-valid?))]]
      (let [error (validation-error attempt)]
        (is (= "C11-DOMINANCE" (:id error)))
        (is (= [:dominance-valid?] (:missing-fields error)))
        (is (= "C11" (:document-id error)))))))

(deftest missing-terminator-cannot-produce-a-passing-hosted-report
  (let [artifact (hosted-artifact) module (:mir-module artifact)
        function (first (keys (:functions module)))
        block (first (keys (get-in module [:functions function :blocks])))
        malformed (update-in module [:functions function :blocks block] dissoc :terminator)
        report (recomputed-report artifact malformed (:data-flow-graph artifact))
        changed (assoc artifact :mir-module malformed :mir-verifier-report report)]
    (is (true? (get-in artifact [:mir-verifier-report :blocks-terminate?])))
    (is (false? (:blocks-terminate? report)))
    (is (= :failed (:status report)))
    (doseq [attempt [#(c11/c11-mir-validate! "c11-test.gravity" changed)
                     #(c11/with-operations
                        (assoc (operations) :c11-mir-module-record (fn [& _] malformed))
                        (fn [] (c11/compiler-c11-mir-source-artifact "c11-test.gravity" "ignored")))]]
      (is (= "C11-BLOCK" (:id (validation-error attempt)))))))
