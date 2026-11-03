(ns gravity.backend.mlir.pipeline-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.backend.mlir :as mlir]
            [gravity.bootstrap :as bootstrap]
            [gravity.digest :as digest]))

(deftest override-precedence-preserves-borrowed-values
  (let [specific {:scope :b7} lowering {:scope :lowering} native {:scope :native}]
    (doseq [select [mlir/source-overrides bootstrap/b7-document-source-overrides]]
      (is (identical? specific (select {:metadata {:backend {:b7-document specific
                                                            :native-lowering lowering :native native}}})))
      (is (identical? lowering (select {:metadata {:backend {:b7-document false
                                                            :native-lowering lowering :native native}}})))
      (is (identical? native (select {:metadata {:backend {:native native}}})))
      (is (= {} (select {})))
      (let [empty-value {}]
        (is (identical? empty-value (select {:metadata {:backend {:b7-document empty-value
                                                                 :native native}}})))))))

(deftest native-rule-context-retains-unknown-defaults
  (doseq [id bootstrap/b7-document-diagnostic-ids]
    (is (= (bootstrap/b7-document-missing-fact id) (mlir/missing-fact id)))
    (is (= (bootstrap/b7-document-mlir-operation id) (mlir/mlir-operation id)))
    (is (= (bootstrap/b7-document-dialect id) (mlir/dialect id))))
  (doseq [id [nil :b7-dialect "B7-UNKNOWN"]]
    (is (= :b7-document-evidence (mlir/missing-fact id)))
    (is (= "gravity.checked_add" (mlir/mlir-operation id)))
    (is (= :gravity.mir (mlir/dialect id)))))

(defn- actual-refusal [path]
  (try (bootstrap/b7-document-validate! path {})
       nil
       (catch clojure.lang.ExceptionInfo error error)))

(deftest malformed-native-artifact-emits-an-actual-owner-refusal
  (let [path (.getCanonicalPath (io/file "bootstrap/gravity/src/gravity/backend/b7_mlir_backend_design.gravity"))
        error (actual-refusal path)
        data (ex-data error)
        file (java.io.File/createTempFile "gravity-mlir-native-diagnostic-" ".edn")]
    (try
      (is (= "B7-MANIFEST" (:id data)))
      (is (= [:native-lowering-artifact] (:missing-fields data)))
      (is (= path (get-in data [:source-span :source])))
      (is (= :complete-mlir-backend-manifest (:missing-or-invalidated-fact data)))
      (is (= :gravity.backend/mlir (:backend data)))
      (let [record {:message (ex-message error) :data data}
            bytes (pr-str record)]
        (spit file bytes :encoding "UTF-8")
        (is (= record (edn/read-string (slurp file :encoding "UTF-8")))))
      (finally (.delete file)))))

(deftest late-stage-lookup-and-wrapper-interposition-remain-observable
  (let [calls (atom [])]
    (with-redefs [mlir/missing-fact (fn [id] (swap! calls conj id) :observed-stage-fact)]
      (let [error (actual-refusal "mlir-context.gravity")]
        (is (= "B7-MANIFEST" (:id (ex-data error))))
        (is (= :observed-stage-fact (:missing-or-invalidated-fact (ex-data error))))
        (is (= ["B7-MANIFEST"] @calls))))
    (with-redefs [bootstrap/b7-document-missing-fact (constantly :observed-wrapper-fact)]
      (is (= :observed-wrapper-fact
             (:missing-or-invalidated-fact (ex-data (actual-refusal "mlir-context.gravity"))))))))

(deftest catalog-records-retain-supplied-input-identity-and-determinism
  (let [path "mlir-context.gravity"
        input-id (str "sha256:" (digest/sha256-hex (pr-str {})))
        stream (bootstrap/b7-document-diagnostic-stream path input-id)]
    (is (= input-id (:input-artifact stream)))
    (is (= bootstrap/b7-document-diagnostic-ids (mapv :rule (:diagnostics stream))))
    (is (every? #(= path (get-in % [:primary :span :source])) (:diagnostics stream)))
    (is (= (pr-str stream) (pr-str (bootstrap/b7-document-diagnostic-stream path input-id))))
    (is (every? #(get-in % [:facts :dialect-verifier-is-not-gravity-proof]) (:diagnostics stream)))))
