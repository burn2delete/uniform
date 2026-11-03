(ns gravity.backend.query.pipeline-test
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
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

(deftest native-catalog-assembly-uses-explicit-borrowed-operations
  (let [path "explicit-native-catalog.gravity"
        input-id (str "sha256:" (digest/sha256-hex (pr-str {})))
        ids ["B11-TAINT" "B11-MANIFEST"]
        spans [{:source path :provided-index 0} {:source path :provided-index 1}]
        span-calls (atom []) id-calls (atom 0)
        stream (query/diagnostic-stream
                path input-id
                {:source-span (fn [source ordinal]
                                (swap! span-calls conj [source ordinal])
                                (nth spans ordinal))
                 :diagnostic-ids (fn [] (swap! id-calls inc) ids)
                 :query-id (fn [id] [:provided-context id])
                 :missing-policy (fn [id] [:provided-policy id])})
        rows (:diagnostics stream)]
    (is (identical? input-id (:input-artifact stream)))
    (is (= ids (mapv :rule rows)))
    (is (= [[path 0] [path 1]] @span-calls))
    (is (= 1 @id-calls))
    (is (every? true? (map (fn [span row] (identical? span (get-in row [:primary :span]))) spans rows)))
    (is (= (mapv (fn [id] [:provided-context id]) ids) (mapv :query-id rows)))
    (is (= (mapv (fn [id] [:provided-policy id]) ids) (mapv :missing-policy rows)))))

(deftest compatibility-catalog-wrapper-retains-late-native-operations
  (let [captured (atom nil)
        path "late-native-catalog.gravity" input-id "supplied-native-input"]
    (with-redefs [query/diagnostic-stream (fn [source input operations]
                                        (reset! captured [source input operations])
                                        :observed-stage-catalog)]
      (is (= :observed-stage-catalog
             (bootstrap/b11-document-diagnostic-stream path input-id))))
    (let [[source input operations] @captured]
      (is (= [path input-id] [source input]))
      (is (= #{:source-span :diagnostic-ids :query-id :missing-policy} (set (keys operations))))
      (with-redefs [bootstrap/source-span (fn [source ordinal] [:late-span source ordinal])
                    bootstrap/b11-document-diagnostic-ids ["LATE-NATIVE-ID"]
                    bootstrap/b11-document-query-id (constantly :late-context)
                    bootstrap/b11-document-missing-policy (constantly :late-policy)]
        (is (= [:late-span path 3] ((:source-span operations) path 3)))
        (is (= ["LATE-NATIVE-ID"] ((:diagnostic-ids operations))))
        (is (= :late-context ((:query-id operations) "LATE-NATIVE-ID")))
        (is (= :late-policy ((:missing-policy operations) "LATE-NATIVE-ID")))))))

(deftest native-catalog-operation-failure-is-not-replaced-with-data
  (let [error (ex-info "native source span failed" {:operation :source-span})
        operations {:source-span (fn [_ _] (throw error))
                    :diagnostic-ids (fn [] ["B11-TAINT"])
                    :query-id (constantly :native-context)
                    :missing-policy (constantly :native-policy)}
        capture (fn [thunk] (try (thunk) nil (catch clojure.lang.ExceptionInfo actual actual)))]
    (is (identical? error (capture #(query/diagnostic-stream "failure.gravity" "native-input" operations))))
    (with-redefs [bootstrap/source-span (fn [_ _] (throw error))]
      (is (identical? error (capture #(bootstrap/b11-document-diagnostic-stream "failure.gravity" "native-input")))))))

(deftest native-fixture-recognition-retains-reference-text-and-late-wrapper-calls
  (doseq [[stage-var bootstrap-var text required]
          [[#'query/sql-structurally-valid? #'bootstrap/b11-document-sql-structurally-valid? bootstrap/b11-document-sql-statement "$1::bigint"]
           [#'query/result-adapter-structurally-valid? #'bootstrap/b11-document-result-adapter-structurally-valid? bootstrap/b11-document-result-adapter-fixture ":validation :checked"]]]
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

(deftest native-sql-fixture-recognition-retains-forbidden-fragment-refusals
  (doseq [suffix ["||" (str (char 36) (char 123)) "concat("]]
    (let [text (str bootstrap/b11-document-sql-statement suffix)]
      (is (false? (query/sql-structurally-valid? text)))
      (is (false? (bootstrap/b11-document-sql-structurally-valid? text))))))
