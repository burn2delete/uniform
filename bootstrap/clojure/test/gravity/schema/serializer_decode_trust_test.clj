(ns gravity.schema.serializer-decode-trust-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(deftest external-json-remains-untrusted-until-schema-validation
  (let [source-path "bootstrap/clojure/fixtures/accepted/schema-interop.gravity"
        artifact (bootstrap/schema-interop-file-artifact source-path)
        source-schema (:source-schema-ir artifact)
        serializer (:serialization-fixture artifact)
        first-round-trip (first (:round-trip-vectors serializer))]
    (testing "the ordinary source produces an artifact with separate decode and validation states"
      (is (some? (:artifact-id artifact)))
      (is (= {:format :json :trust-boundary :external-data
              :decoded-trust :untrusted :canonical false}
             (select-keys serializer [:format :trust-boundary :decoded-trust :canonical])))
      (is (= {:decoded-taint :untrusted :validation-result :validated-for-schema}
             (select-keys first-round-trip [:decoded-taint :validation-result]))))
    (doseq [[field-path mutated-serializer]
            [[[:decoded-trust]
              (assoc serializer :decoded-trust :validated-for-schema)]
             [[:round-trip-vectors 1 :decoded-taint]
              (update serializer :round-trip-vectors conj
                      (assoc first-round-trip
                             :name :second-ticket-classification-v2-json
                             :decoded-taint :validated-for-schema))]]]
      (testing (str "premature trust at " field-path)
        (is (= (:schema-hash source-schema) (:schema-hash mutated-serializer)))
        (let [diagnostic (with-redefs [bootstrap/p10-serialization-fixture
                                       (constantly mutated-serializer)]
                           (try
                             (bootstrap/schema-interop-file-artifact source-path)
                             nil
                             (catch clojure.lang.ExceptionInfo error
                               (ex-data error))))]
          (is (= {:id "S2-TAINT"
                  :document-id "S2"
                  :schema-id "TicketClassification"
                  :schema-version 2
                  :schema-hash (:schema-hash source-schema)
                  :source-span (first (:source-spans source-schema))
                  :format :json
                  :boundary :external-data
                  :field-path field-path
                  :taint-state :validated-for-schema
                  :numeric-policy :exact-or-diagnostic
                  :string-policy :utf8-nfc}
                 (select-keys diagnostic [:id :document-id :schema-id :schema-version
                                          :schema-hash :source-span :format :boundary
                                          :field-path :taint-state :numeric-policy
                                          :string-policy]))))))))
