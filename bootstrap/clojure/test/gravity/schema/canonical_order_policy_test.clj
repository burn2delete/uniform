(ns gravity.schema.canonical-order-policy-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(deftest canonical-manifest-requires-encoded-collection-order
  (let [source-path "bootstrap/clojure/fixtures/accepted/schema-interop.gravity"
        artifact-id bootstrap/c4-artifact-id
        artifact-id-calls (atom 0)]
    (with-redefs [bootstrap/c4-artifact-id
                  (fn [value]
                    (when (= :gravity/stage0-schema-interop-artifact (:kind value))
                      (swap! artifact-id-calls inc))
                    (artifact-id value))]
      (let [artifact (bootstrap/schema-interop-file-artifact source-path)
            canonical-format (:canonical-format artifact)
            source-schema (:source-schema-ir artifact)
            schema-hash (:schema-hash source-schema)]
        (testing "the ordinary source produces an identified canonical format manifest"
          (is (some? (:artifact-id artifact)))
          (is (= 1 @artifact-id-calls))
          (is (= {:schema-id "TicketClassification"
                  :schema-version 2
                  :schema-hash schema-hash
                  :format-version 1
                  :status :complete
                  :map-order :lexicographic-encoded-key
                  :set-order :lexicographic-encoded-value}
                 (select-keys canonical-format [:schema-id :schema-version :schema-hash
                                                :format-version :status :map-order :set-order]))))
        (doseq [[label field expected-rule mutated-format]
                [["host map order" :map-order :lexicographic-encoded-key
                  (assoc canonical-format :map-order :host-iteration-order)]
                 ["map-key rule for sets" :set-order :lexicographic-encoded-value
                  (assoc canonical-format :set-order :lexicographic-encoded-key)]
                 ["missing map order" :map-order :lexicographic-encoded-key
                  (dissoc canonical-format :map-order)]
                 ["missing set order" :set-order :lexicographic-encoded-value
                  (dissoc canonical-format :set-order)]]]
          (testing label
            (is (= {:schema-hash schema-hash :status :complete}
                   (select-keys mutated-format [:schema-hash :status])))
            (let [diagnostic (with-redefs [bootstrap/p10-canonical-format
                                           (constantly mutated-format)]
                               (try
                                 (bootstrap/schema-interop-file-artifact source-path)
                                 nil
                                 (catch clojure.lang.ExceptionInfo error
                                   (ex-data error))))]
              (is (= {:id "S3-ORDER"
                      :document-id "S3"
                      :field-path [:canonical-format field]
                      :expected-rule expected-rule
                      :schema-id "TicketClassification"
                      :schema-version 2
                      :schema-hash schema-hash
                      :format-version 1
                      :source-span (first (:source-spans source-schema))}
                     (select-keys diagnostic [:id :document-id :field-path :expected-rule
                                              :schema-id :schema-version :schema-hash
                                              :format-version :source-span])))
              (is (= 1 @artifact-id-calls)
                  "invalid manifests must fail before output artifact ID construction"))))))))
