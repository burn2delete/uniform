(ns gravity.schema.validator-taint-boundary-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(deftest validator-clearance-respects-source-boundaries
  (let [source-path "bootstrap/clojure/fixtures/accepted/schema-interop.gravity"
        artifact (bootstrap/schema-interop-file-artifact source-path)
        source-schema (:source-schema-ir artifact)
        validator (:validator-artifact artifact)]
    (testing "the ordinary schema interop artifact preserves its taint policy"
      (is (= :complete (bootstrap/p10-schema-validate! source-path artifact)))
      (is (true? (get-in artifact [:capability-based-proof :validator-boundaries-covered?])))
      (is (= #{:api-input :database-row :model-output :configuration :artifact-manifest}
             (:clears-taint-for validator)))
      (is (= #{:logs :prompts :unvalidated-sinks} (:retains-taint-for validator)))
      (is (= [:untrusted :validated-for-schema :validated-for-sink]
             (:validation-states validator))))
    (doseq [[label boundary mutated-artifact]
            [["clearance adds an undeclared log sink" :logs
              (update-in artifact [:validator-artifact :clears-taint-for] conj :logs)]
             ["a declared clearance is also marked as retained" :api-input
              (update-in artifact [:validator-artifact :retains-taint-for] conj :api-input)]
             ["generated declarations cannot widen the source contract" :logs
              (-> artifact
                  (update-in [:source-schema-ir :boundaries] conj :logs)
                  (update-in [:validator-artifact :boundaries] conj :logs)
                  (update-in [:validator-artifact :clears-taint-for] conj :logs)
                  (update-in [:validator-artifact :retains-taint-for] disj :logs))]]]
      (testing label
        (let [diagnostic (try
                           (bootstrap/p10-schema-validate! source-path mutated-artifact)
                           nil
                           (catch clojure.lang.ExceptionInfo error
                             (ex-data error)))]
          (is (= {:id "S1-TAINT"
                  :diagnostic-family :phase10-schema-interop
                  :schema-id "TicketClassification"
                  :schema-version 2
                  :schema-hash (:schema-hash source-schema)
                  :source-span (first (:source-spans source-schema))
                  :derivation-target :validator
                  :boundary boundary}
                 (select-keys diagnostic [:id :diagnostic-family :schema-id
                                          :schema-version :schema-hash :source-span
                                          :derivation-target :boundary]))))))))
