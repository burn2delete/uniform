(ns gravity.backend.hdl.pipeline-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.backend.hdl :as hdl]
            [gravity.bootstrap :as bootstrap]
            [gravity.digest :as digest]))

(deftest override-precedence-preserves-borrowed-values
  (let [specific {:scope :b9} lowering {:scope :lowering} specialized {:scope :specialized}]
    (doseq [select [hdl/source-overrides bootstrap/b9-document-source-overrides]]
      (is (identical? specific (select {:metadata {:backend {:b9-document specific
                                                            :specialized-lowering lowering
                                                            :specialized specialized}}})))
      (is (identical? lowering (select {:metadata {:backend {:b9-document false
                                                            :specialized-lowering lowering
                                                            :specialized specialized}}})))
      (is (identical? specialized (select {:metadata {:backend {:specialized specialized}}})))
      (is (= {} (select {})))
      (let [empty-value {}]
        (is (identical? empty-value (select {:metadata {:backend {:b9-document empty-value
                                                                 :specialized specialized}}})))))))

(deftest native-rule-context-retains-unknown-defaults
  (doseq [id bootstrap/b9-document-diagnostic-ids]
    (is (= (bootstrap/b9-document-missing-fact id) (hdl/missing-fact id)))
    (is (= (bootstrap/b9-document-signal-id id) (hdl/signal-id id))))
  (is (= :clk (hdl/signal-id "B9-CLOCK")))
  (is (= :cdc_handshake (hdl/signal-id "B9-CDC")))
  (doseq [id [nil :b9-clock "B9-UNKNOWN"]]
    (is (= :hdl-artifact-manifest (hdl/missing-fact id)))
    (is (= :gravity_stage0_hdl (hdl/signal-id id)))))

(defn- actual-refusal [path]
  (try (bootstrap/b9-document-validate! path {})
       nil
       (catch clojure.lang.ExceptionInfo error error)))

(deftest malformed-native-artifact-emits-an-actual-owner-refusal
  (let [path (.getCanonicalPath (io/file "bootstrap/gravity/src/gravity/backend/b9_hdl_backend_design.gravity"))
        error (actual-refusal path)
        data (ex-data error)
        file (java.io.File/createTempFile "gravity-hdl-native-diagnostic-" ".edn")]
    (try
      (is (= "B9-MANIFEST" (:id data)))
      (is (= [:specialized-lowering-artifact] (:missing-fields data)))
      (is (= path (get-in data [:source-span :source])))
      (is (= :hdl-artifact-manifest (:missing-proof-or-constraint data)))
      (is (= :gravity.backend/hdl (:backend data)))
      (is (= :hardware (:profile data)))
      (is (= :systemverilog (:target data)))
      (let [record {:message (ex-message error) :data data}]
        (spit file (pr-str record) :encoding "UTF-8")
        (is (= record (edn/read-string (slurp file :encoding "UTF-8")))))
      (finally (.delete file)))))

(deftest late-stage-lookup-and-wrapper-interposition-remain-observable
  (let [calls (atom [])]
    (with-redefs [hdl/missing-fact (fn [id] (swap! calls conj id) :observed-stage-fact)]
      (let [error (actual-refusal "hdl-context.gravity")]
        (is (= "B9-MANIFEST" (:id (ex-data error))))
        (is (= :observed-stage-fact (:missing-proof-or-constraint (ex-data error))))
        (is (= ["B9-MANIFEST"] @calls))))
    (with-redefs [bootstrap/b9-document-missing-fact (constantly :observed-wrapper-fact)]
      (is (= :observed-wrapper-fact
             (:missing-proof-or-constraint (ex-data (actual-refusal "hdl-context.gravity"))))))))

(deftest catalog-records-retain-supplied-input-identity-and-determinism
  (let [path "hdl-context.gravity"
        input-id (str "sha256:" (digest/sha256-hex (pr-str {})))
        stream (bootstrap/b9-document-diagnostic-stream path input-id)]
    (is (= input-id (:input-artifact stream)))
    (is (= bootstrap/b9-document-diagnostic-ids (mapv :rule (:diagnostics stream))))
    (is (every? #(= path (get-in % [:primary :span :source])) (:diagnostics stream)))
    (is (= (pr-str stream) (pr-str (bootstrap/b9-document-diagnostic-stream path input-id))))
    (is (every? #(= [:hardware :systemverilog] ((juxt :profile :target) %)) (:diagnostics stream)))
    (is (every? #(true? (get-in % [:facts :unmediated-cdc-rejected])) (:diagnostics stream)))))
