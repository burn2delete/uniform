(ns gravity.backend.query.pipeline-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.backend.query :as query]
            [gravity.bootstrap :as bootstrap]
            [gravity.digest :as digest]))

(deftest native-override-projection-preserves-borrowed-values
  (doseq [select [query/source-overrides bootstrap/b11-document-source-overrides]]
    (is (= {} (select {})))
    (is (= {} (select {:metadata {:backend {:specialized {:ignored true}
                                          :b11-document {:ignored true}}}})))
    (doseq [value [{:scope :lowering} {} false nil]]
      (is (identical? value (select {:metadata {:backend {:specialized-lowering value
                                                        :specialized {:ignored true}
                                                        :b11-document {:ignored true}}}}))))))

(deftest native-rule-context-retains-unknown-defaults
  (doseq [id bootstrap/b11-document-diagnostic-ids]
    (is (= (bootstrap/b11-document-missing-policy id) (query/missing-policy id)))
    (is (= (bootstrap/b11-document-query-id id) (query/query-id id))))
  (is (= :dynamic-sql (query/query-id "B11-TAINT")))
  (is (= :stage0-migration (query/query-id "B11-MIGRATION")))
  (doseq [id [nil :b11-schema "B11-UNKNOWN"]]
    (is (= :query-artifact-manifest (query/missing-policy id)))
    (is (= :query-manifest (query/query-id id)))))

(defn- actual-refusal [path]
  (try (bootstrap/b11-document-validate! path {})
       nil
       (catch clojure.lang.ExceptionInfo error error)))

(deftest malformed-native-artifact-emits-an-actual-owner-refusal
  (let [path (.getCanonicalPath (io/file "language/compiler/src/gravity/backend/b11_query_relational_backend_design.gravity"))
        error (actual-refusal path)
        data (ex-data error)
        file (java.io.File/createTempFile "gravity-query-native-diagnostic-" ".edn")]
    (try
      (is (= "B11-MANIFEST" (:id data)))
      (is (= [:specialized-lowering-artifact] (:missing-fields data)))
      (is (= path (get-in data [:source-span :source])))
      (is (= :query-artifact-manifest (:missing-policy data)))
      (is (= :gravity.backend/query-relational (:backend data)))
      (is (= :hosted (:profile data)))
      (is (= :postgresql (:target data)))
      (let [record {:message (ex-message error) :data data}]
        (spit file (pr-str record) :encoding "UTF-8")
        (is (= record (edn/read-string (slurp file :encoding "UTF-8")))))
      (finally (.delete file)))))

(deftest late-stage-lookup-and-wrapper-interposition-remain-observable
  (let [calls (atom [])]
    (with-redefs [query/missing-policy (fn [id] (swap! calls conj id) :observed-stage-policy)]
      (let [error (actual-refusal "query-context.gravity")]
        (is (= "B11-MANIFEST" (:id (ex-data error))))
        (is (= :observed-stage-policy (:missing-policy (ex-data error))))
        (is (= ["B11-MANIFEST"] @calls))))
    (with-redefs [bootstrap/b11-document-missing-policy (constantly :observed-wrapper-policy)]
      (is (= :observed-wrapper-policy
             (:missing-policy (ex-data (actual-refusal "query-context.gravity"))))))))

(deftest catalog-records-retain-supplied-input-identity-and-determinism
  (let [path "query-context.gravity"
        input-id (str "sha256:" (digest/sha256-hex (pr-str {})))
        stream (bootstrap/b11-document-diagnostic-stream path input-id)]
    (is (= input-id (:input-artifact stream)))
    (is (= bootstrap/b11-document-diagnostic-ids (mapv :rule (:diagnostics stream))))
    (is (every? #(= path (get-in % [:primary :span :source])) (:diagnostics stream)))
    (is (= (pr-str stream) (pr-str (bootstrap/b11-document-diagnostic-stream path input-id))))
    (is (every? #(= [:hosted :postgresql] ((juxt :profile :target) %)) (:diagnostics stream)))
    (is (= [:db/write :db/write]
           ((juxt :effect :capability)
            (first (filter #(= "B11-CAPABILITY" (:rule %)) (:diagnostics stream))))))))
