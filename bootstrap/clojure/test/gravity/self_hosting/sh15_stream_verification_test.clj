(ns gravity.self-hosting.sh15-stream-verification-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh15-absent-layout-location-test]))

;; Compile only the owning C15 source. The reused helper has delayed C12 plans;
;; none of those plans or native dependencies are realized by these tests.
(def ^:private plan
  (delay
    (#'gravity.self-hosting.sh15-absent-layout-location-test/compile-plan
     "bootstrap/gravity/src/gravity/compiler/c15_compiler_diagnostics.gravity")))

(defn- verify [stream]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh15-stream-development :compiler-artifact-plan? true}
   @plan 'verify-c15-diagnostic-stream [stream]))

(defn- empty-stream []
  {:stage :read-source :input-artifact nil :output-artifact nil
   :diagnostics []
   :summary-counts {:error 0 :warning 0 :info 0 :hint 0 :internal-error 0}
   :deterministic-ordering-key :diagnostic-id
   :redaction-policy :private-unassessed :rendering-version "c15-private-v1"})

(deftest stream-envelope-is-executed-and-preserves-explicit-unavailable-input
  (doseq [stream [(empty-stream)
                  (assoc (empty-stream) :stage :type-check :input-artifact :typed-core
                         :output-artifact :checked-core)]]
    (let [result (verify stream)]
      (is (= :incomplete (:status result)))
      (is (= :stream-envelope-only (:verification-status result)))
      (is (= stream (:diagnostic-stream result)))
      (is (= [] (:diagnostics result)))
      (is (= result (verify stream)))
      (is (some #{:identity-request-joins} (:nonclaims result)))
      (is (some #{:public-safe-projection} (:nonclaims result))))))

(deftest malformed-stream-envelopes-return-actual-c15-rejections
  (doseq [[stream field reason]
          (concat
           [[nil :stream :stream-map-required]
            [[] :stream :stream-map-required]
            [(list :stream) :stream :unsupported-canonical-carrier]]
           (for [field (keys (empty-stream))]
             [(dissoc (empty-stream) field) field :missing-stream-field])
           (for [[field value reason]
                 [[:stage "read-source" :invalid-stage]
                  [:input-artifact 17 :invalid-input-artifact]
                  [:output-artifact [] :invalid-output-artifact]
                  [:diagnostics {} :invalid-diagnostics]
                  [:summary-counts "zero" :invalid-summary-counts]
                  [:deterministic-ordering-key nil :invalid-ordering-key]
                  [:redaction-policy true :invalid-redaction-policy]
                  [:rendering-version "" :invalid-rendering-version]]]
             [(assoc (empty-stream) field value) field reason]))]
    (let [result (verify stream) diagnostic (first (:diagnostics result))]
      (is (= :rejected (:status result)))
      (is (= :gravity/c15-diagnostic-stream-verification (:artifact result)))
      (is (= "C15-SCHEMA" (:rule diagnostic)))
      (is (= field (:schema-field diagnostic)))
      (is (= reason (get-in diagnostic [:facts :reason])))))
  (is (= :rejected (:status (verify (assoc (empty-stream) :stage :type-check))))))

(deftest envelope-checks-confer-no-record-or-publication-authority
  ;; Record construction, identity joins, summary values and ordering remain
  ;; unfinished in this checkpoint. Even an asserted publication flag cannot
  ;; promote an envelope to a passed stream verification.
  (doseq [stream [(assoc (empty-stream) :diagnostics [{}])
                  (assoc (empty-stream) :summary-counts {:error 99})
                  (assoc (empty-stream) :redaction-policy :public-safe
                         :public-safe? true :redaction-applied? true)]]
    (is (= :incomplete (:status (verify stream))))))
