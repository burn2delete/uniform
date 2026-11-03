(ns gravity.backend.workflow.pipeline-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.backend.workflow :as workflow]
            [gravity.bootstrap :as bootstrap]
            [gravity.digest :as digest]))

(deftest override-precedence-preserves-borrowed-values
  (let [specific {:scope :b10} lowering {:scope :lowering} specialized {:scope :specialized}]
    (doseq [select [workflow/source-overrides bootstrap/b10-document-source-overrides]]
      (is (identical? specific (select {:metadata {:backend {:b10-document specific
                                                            :specialized-lowering lowering
                                                            :specialized specialized}}})))
      (is (identical? lowering (select {:metadata {:backend {:b10-document false
                                                            :specialized-lowering lowering
                                                            :specialized specialized}}})))
      (is (identical? specialized (select {:metadata {:backend {:specialized specialized}}})))
      (is (= {} (select {})))
      (let [empty-value {}]
        (is (identical? empty-value (select {:metadata {:backend {:b10-document empty-value
                                                                 :specialized specialized}}})))))))

(deftest native-rule-context-retains-unknown-defaults
  (doseq [id bootstrap/b10-document-diagnostic-ids]
    (is (= (bootstrap/b10-document-missing-policy id) (workflow/missing-policy id)))
    (is (= (bootstrap/b10-document-step-id id) (workflow/step-id id))))
  (is (= :call-model (workflow/step-id "B10-REPLAY")))
  (is (= :write-database (workflow/step-id "B10-COMPENSATION")))
  (doseq [id [nil :b10-schema "B10-UNKNOWN"]]
    (is (= :complete-workflow-artifact-manifest (workflow/missing-policy id)))
    (is (= :workflow-manifest (workflow/step-id id)))))

(defn- actual-refusal [path]
  (try (bootstrap/b10-document-validate! path {})
       nil
       (catch clojure.lang.ExceptionInfo error error)))

(deftest malformed-native-artifact-emits-an-actual-owner-refusal
  (let [path (.getCanonicalPath (io/file "language/compiler/src/gravity/backend/b10_workflow_graph_backend_design.gravity"))
        error (actual-refusal path)
        data (ex-data error)
        file (java.io.File/createTempFile "gravity-workflow-native-diagnostic-" ".edn")]
    (try
      (is (= "B10-MANIFEST" (:id data)))
      (is (= [:specialized-lowering-artifact] (:missing-fields data)))
      (is (= path (get-in data [:source-span :source])))
      (is (= :complete-workflow-artifact-manifest (:missing-policy data)))
      (is (= :gravity.backend/workflow-graph (:backend data)))
      (is (= :distributed (:profile data)))
      (is (= :durable-workflow (:target data)))
      (let [record {:message (ex-message error) :data data}]
        (spit file (pr-str record) :encoding "UTF-8")
        (is (= record (edn/read-string (slurp file :encoding "UTF-8")))))
      (finally (.delete file)))))

(deftest late-stage-lookup-and-wrapper-interposition-remain-observable
  (let [calls (atom [])]
    (with-redefs [workflow/missing-policy (fn [id] (swap! calls conj id) :observed-stage-fact)]
      (let [error (actual-refusal "workflow-context.gravity")]
        (is (= "B10-MANIFEST" (:id (ex-data error))))
        (is (= :observed-stage-fact (:missing-policy (ex-data error))))
        (is (= ["B10-MANIFEST"] @calls))))
    (with-redefs [bootstrap/b10-document-missing-policy (constantly :observed-wrapper-fact)]
      (is (= :observed-wrapper-fact
             (:missing-policy (ex-data (actual-refusal "workflow-context.gravity"))))))))

(deftest catalog-records-retain-supplied-input-identity-and-determinism
  (let [path "workflow-context.gravity"
        input-id (str "sha256:" (digest/sha256-hex (pr-str {})))
        stream (bootstrap/b10-document-diagnostic-stream path input-id)]
    (is (= input-id (:input-artifact stream)))
    (is (= bootstrap/b10-document-diagnostic-ids (mapv :rule (:diagnostics stream))))
    (is (every? #(= path (get-in % [:primary :span :source])) (:diagnostics stream)))
    (is (= (pr-str stream) (pr-str (bootstrap/b10-document-diagnostic-stream path input-id))))
    (is (every? #(= [:distributed :durable-workflow] ((juxt :profile :target) %)) (:diagnostics stream)))
    (is (every? #(true? (get-in % [:facts :prompt-authority-rejected])) (:diagnostics stream)))))
