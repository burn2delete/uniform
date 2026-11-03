(ns gravity.backend.gpu.pipeline-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.backend.gpu :as gpu]
            [gravity.bootstrap :as bootstrap]
            [gravity.digest :as digest]))

(deftest override-precedence-preserves-borrowed-values
  (let [specific {:scope :b8} lowering {:scope :lowering} specialized {:scope :specialized}]
    (doseq [select [gpu/source-overrides bootstrap/b8-document-source-overrides]]
      (is (identical? specific (select {:metadata {:backend {:b8-document specific
                                                            :specialized-lowering lowering
                                                            :specialized specialized}}})))
      (is (identical? lowering (select {:metadata {:backend {:b8-document false
                                                            :specialized-lowering lowering
                                                            :specialized specialized}}})))
      (is (identical? specialized (select {:metadata {:backend {:specialized specialized}}})))
      (is (= {} (select {})))
      (let [empty-value {}]
        (is (identical? empty-value (select {:metadata {:backend {:b8-document empty-value
                                                                 :specialized specialized}}})))))))

(deftest native-rule-context-retains-unknown-defaults
  (doseq [id bootstrap/b8-document-diagnostic-ids]
    (is (= (bootstrap/b8-document-missing-fact id) (gpu/missing-fact id)))
    (is (= (bootstrap/b8-document-kernel-id id) (gpu/kernel-id id))))
  (is (= :gravity_stage0_atomic (gpu/kernel-id "B8-ATOMIC")))
  (is (= :gravity_stage0_reduce (gpu/kernel-id "B8-MATH")))
  (doseq [id [nil :b8-target "B8-UNKNOWN"]]
    (is (= :b8-document-evidence (gpu/missing-fact id)))
    (is (= :gravity_stage0_kernel (gpu/kernel-id id)))))

(defn- actual-refusal [path]
  (try (bootstrap/b8-document-validate! path {})
       nil
       (catch clojure.lang.ExceptionInfo error error)))

(deftest malformed-native-artifact-emits-an-actual-owner-refusal
  (let [path (.getCanonicalPath (io/file "language/compiler/src/gravity/backend/b8_gpu_backend_design.gravity"))
        error (actual-refusal path)
        data (ex-data error)
        file (java.io.File/createTempFile "gravity-gpu-native-diagnostic-" ".edn")]
    (try
      (is (= "B8-MANIFEST" (:id data)))
      (is (= [:specialized-lowering-artifact] (:missing-fields data)))
      (is (= path (get-in data [:source-span :source])))
      (is (= :complete-gpu-artifact-manifest (:missing-proof-or-feature data)))
      (is (= :gravity.backend/gpu (:backend data)))
      (let [record {:message (ex-message error) :data data}]
        (spit file (pr-str record) :encoding "UTF-8")
        (is (= record (edn/read-string (slurp file :encoding "UTF-8")))))
      (finally (.delete file)))))

(deftest late-stage-lookup-and-wrapper-interposition-remain-observable
  (let [calls (atom [])]
    (with-redefs [gpu/missing-fact (fn [id] (swap! calls conj id) :observed-stage-fact)]
      (let [error (actual-refusal "gpu-context.gravity")]
        (is (= "B8-MANIFEST" (:id (ex-data error))))
        (is (= :observed-stage-fact (:missing-proof-or-feature (ex-data error))))
        (is (= ["B8-MANIFEST"] @calls))))
    (with-redefs [bootstrap/b8-document-missing-fact (constantly :observed-wrapper-fact)]
      (is (= :observed-wrapper-fact
             (:missing-proof-or-feature (ex-data (actual-refusal "gpu-context.gravity"))))))))

(deftest catalog-records-retain-supplied-input-identity-and-determinism
  (let [path "gpu-context.gravity"
        input-id (str "sha256:" (digest/sha256-hex (pr-str {})))
        stream (bootstrap/b8-document-diagnostic-stream path input-id)]
    (is (= input-id (:input-artifact stream)))
    (is (= bootstrap/b8-document-diagnostic-ids (mapv :rule (:diagnostics stream))))
    (is (every? #(= path (get-in % [:primary :span :source])) (:diagnostics stream)))
    (is (= (pr-str stream) (pr-str (bootstrap/b8-document-diagnostic-stream path input-id))))
    (is (every? #(= :linear (get-in % [:facts :device-buffer-policy])) (:diagnostics stream)))))
