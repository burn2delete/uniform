(ns gravity.backend.workflow.pipeline-test
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
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
  (let [path (.getCanonicalPath (io/file "bootstrap/gravity/src/gravity/backend/b10_workflow_graph_backend_design.gravity"))
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

(deftest native-catalog-assembly-uses-explicit-borrowed-operations
  (let [path "explicit-native-catalog.gravity"
        input-id (str "sha256:" (digest/sha256-hex (pr-str {})))
        ids ["B10-REPLAY" "B10-MANIFEST"]
        spans [{:source path :provided-index 0} {:source path :provided-index 1}]
        span-calls (atom []) id-calls (atom 0)
        stream (workflow/diagnostic-stream
                path input-id
                {:source-span (fn [source ordinal]
                                (swap! span-calls conj [source ordinal])
                                (nth spans ordinal))
                 :diagnostic-ids (fn [] (swap! id-calls inc) ids)
                 :step-id (fn [id] [:provided-context id])
                 :missing-policy (fn [id] [:provided-policy id])})
        rows (:diagnostics stream)]
    (is (identical? input-id (:input-artifact stream)))
    (is (= ids (mapv :rule rows)))
    (is (= [[path 0] [path 1]] @span-calls))
    (is (= 1 @id-calls))
    (is (every? true? (map (fn [span row] (identical? span (get-in row [:primary :span]))) spans rows)))
    (is (= (mapv (fn [id] [:provided-context id]) ids) (mapv :step-id rows)))
    (is (= (mapv (fn [id] [:provided-policy id]) ids) (mapv :missing-policy rows)))))

(deftest compatibility-catalog-wrapper-retains-late-native-operations
  (let [captured (atom nil)
        path "late-native-catalog.gravity" input-id "supplied-native-input"]
    (with-redefs [workflow/diagnostic-stream (fn [source input operations]
                                        (reset! captured [source input operations])
                                        :observed-stage-catalog)]
      (is (= :observed-stage-catalog
             (bootstrap/b10-document-diagnostic-stream path input-id))))
    (let [[source input operations] @captured]
      (is (= [path input-id] [source input]))
      (is (= #{:source-span :diagnostic-ids :step-id :missing-policy} (set (keys operations))))
      (with-redefs [bootstrap/source-span (fn [source ordinal] [:late-span source ordinal])
                    bootstrap/b10-document-diagnostic-ids ["LATE-NATIVE-ID"]
                    bootstrap/b10-document-step-id (constantly :late-context)
                    bootstrap/b10-document-missing-policy (constantly :late-policy)]
        (is (= [:late-span path 3] ((:source-span operations) path 3)))
        (is (= ["LATE-NATIVE-ID"] ((:diagnostic-ids operations))))
        (is (= :late-context ((:step-id operations) "LATE-NATIVE-ID")))
        (is (= :late-policy ((:missing-policy operations) "LATE-NATIVE-ID")))))))

(deftest native-catalog-operation-failure-is-not-replaced-with-data
  (let [error (ex-info "native source span failed" {:operation :source-span})
        operations {:source-span (fn [_ _] (throw error))
                    :diagnostic-ids (fn [] ["B10-REPLAY"])
                    :step-id (constantly :native-context)
                    :missing-policy (constantly :native-policy)}
        capture (fn [thunk] (try (thunk) nil (catch clojure.lang.ExceptionInfo actual actual)))]
    (is (identical? error (capture #(workflow/diagnostic-stream "failure.gravity" "native-input" operations))))
    (with-redefs [bootstrap/source-span (fn [_ _] (throw error))]
      (is (identical? error (capture #(bootstrap/b10-document-diagnostic-stream "failure.gravity" "native-input")))))))

(deftest native-fixture-recognition-retains-reference-text-and-late-wrapper-calls
  (doseq [[stage-var bootstrap-var text required]
          [[#'workflow/workflow-graph-structurally-valid? #'bootstrap/b10-document-workflow-graph-structurally-valid? bootstrap/b10-document-workflow-graph ":replay-barriers"]
           [#'workflow/replay-fixture-structurally-valid? #'bootstrap/b10-document-replay-fixture-structurally-valid? bootstrap/b10-document-replay-fixture ":model-output-recorded"]]]
    (let [missing (str/replace text required "")]
      (is (not= text missing))
      (doseq [recognize [(var-get stage-var) (var-get bootstrap-var)]]
        (is (true? (recognize text)))
        (is (false? (recognize "")))
        (is (false? (recognize missing))))
      (with-redefs-fn
        {stage-var (fn [actual]
                     (is (identical? text actual))
                     :observed-native-fixture-match)}
        (fn []
          (is (= :observed-native-fixture-match ((var-get bootstrap-var) text))))))))
