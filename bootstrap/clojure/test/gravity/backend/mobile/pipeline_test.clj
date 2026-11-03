(ns gravity.backend.mobile.pipeline-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.backend.mobile :as mobile]
            [gravity.bootstrap :as bootstrap]
            [gravity.digest :as digest]))

(deftest native-override-projection-preserves-borrowed-values
  (doseq [select [mobile/source-overrides bootstrap/b12-document-source-overrides]]
    (is (= {} (select {})))
    (is (= {} (select {:metadata {:backend {:specialized {:ignored true}
                                          :b12-document {:ignored true}}}})))
    (doseq [value [{:scope :lowering} {} false nil]]
      (is (identical? value (select {:metadata {:backend {:specialized-lowering value
                                                        :specialized {:ignored true}
                                                        :b12-document {:ignored true}}}}))))))

(deftest native-rule-context-retains-unknown-defaults
  (doseq [id bootstrap/b12-document-diagnostic-ids]
    (is (= (bootstrap/b12-document-missing-policy id) (mobile/missing-policy id)))
    (is (= (bootstrap/b12-document-domain-anchor id) (mobile/domain-anchor id))))
  (is (= :platform-api-binding (mobile/domain-anchor "B12-PERMISSION")))
  (is (= :ui-thread-update (mobile/domain-anchor "B12-THREAD")))
  (doseq [id [nil :b12-schema "B12-UNKNOWN"]]
    (is (= :mobile-artifact-manifest (mobile/missing-policy id)))
    (is (= :mobile-manifest (mobile/domain-anchor id)))))

(defn- actual-refusal [path]
  (try (bootstrap/b12-document-validate! path {})
       nil
       (catch clojure.lang.ExceptionInfo error error)))

(deftest malformed-native-artifact-emits-an-actual-owner-refusal
  (let [path (.getCanonicalPath (io/file "bootstrap/gravity/src/gravity/backend/b12_mobile_backend_design.gravity"))
        error (actual-refusal path)
        data (ex-data error)
        file (java.io.File/createTempFile "gravity-mobile-native-diagnostic-" ".edn")]
    (try
      (is (= "B12-MANIFEST" (:id data)))
      (is (= [:specialized-lowering-artifact] (:missing-fields data)))
      (is (= path (get-in data [:source-span :source])))
      (is (= :mobile-artifact-manifest (:missing-policy data)))
      (is (= :gravity.backend/mobile (:backend data)))
      (is (= :hosted (:profile data)))
      (is (= :ios (:target data)))
      (let [record {:message (ex-message error) :data data}]
        (spit file (pr-str record) :encoding "UTF-8")
        (is (= record (edn/read-string (slurp file :encoding "UTF-8")))))
      (finally (.delete file)))))

(deftest late-stage-lookup-and-wrapper-interposition-remain-observable
  (let [calls (atom [])]
    (with-redefs [mobile/missing-policy (fn [id] (swap! calls conj id) :observed-stage-policy)]
      (let [error (actual-refusal "mobile-context.gravity")]
        (is (= "B12-MANIFEST" (:id (ex-data error))))
        (is (= :observed-stage-policy (:missing-policy (ex-data error))))
        (is (= ["B12-MANIFEST"] @calls))))
    (with-redefs [bootstrap/b12-document-missing-policy (constantly :observed-wrapper-policy)]
      (is (= :observed-wrapper-policy
             (:missing-policy (ex-data (actual-refusal "mobile-context.gravity"))))))))

(deftest catalog-records-retain-supplied-input-identity-and-determinism
  (let [path "mobile-context.gravity"
        input-id (str "sha256:" (digest/sha256-hex (pr-str {})))
        stream (bootstrap/b12-document-diagnostic-stream path input-id)]
    (is (= input-id (:input-artifact stream)))
    (is (= bootstrap/b12-document-diagnostic-ids (mapv :rule (:diagnostics stream))))
    (is (every? #(= path (get-in % [:primary :span :source])) (:diagnostics stream)))
    (is (= (pr-str stream) (pr-str (bootstrap/b12-document-diagnostic-stream path input-id))))
    (is (every? #(= [:hosted :ios] ((juxt :profile :target) %)) (:diagnostics stream)))
    (is (= [:network :network/request]
           ((juxt :permission :capability)
            (first (filter #(= "B12-PERMISSION" (:rule %)) (:diagnostics stream))))))))

(deftest native-catalog-assembly-uses-explicit-borrowed-operations
  (let [path "explicit-native-catalog.gravity"
        input-id (str "sha256:" (digest/sha256-hex (pr-str {})))
        ids ["B12-THREAD" "B12-MANIFEST"]
        spans [{:source path :provided-index 0} {:source path :provided-index 1}]
        span-calls (atom []) id-calls (atom 0)
        stream (mobile/diagnostic-stream
                path input-id
                {:source-span (fn [source ordinal]
                                (swap! span-calls conj [source ordinal])
                                (nth spans ordinal))
                 :diagnostic-ids (fn [] (swap! id-calls inc) ids)
                 :domain-anchor (fn [id] [:provided-context id])
                 :missing-policy (fn [id] [:provided-policy id])})
        rows (:diagnostics stream)]
    (is (identical? input-id (:input-artifact stream)))
    (is (= ids (mapv :rule rows)))
    (is (= [[path 0] [path 1]] @span-calls))
    (is (= 1 @id-calls))
    (is (every? true? (map (fn [span row] (identical? span (get-in row [:primary :span]))) spans rows)))
    (is (= (mapv (fn [id] [:provided-context id]) ids) (mapv :domain-anchor rows)))
    (is (= (mapv (fn [id] [:provided-policy id]) ids) (mapv :missing-policy rows)))))

(deftest compatibility-catalog-wrapper-retains-late-native-operations
  (let [captured (atom nil)
        path "late-native-catalog.gravity" input-id "supplied-native-input"]
    (with-redefs [mobile/diagnostic-stream (fn [source input operations]
                                        (reset! captured [source input operations])
                                        :observed-stage-catalog)]
      (is (= :observed-stage-catalog
             (bootstrap/b12-document-diagnostic-stream path input-id))))
    (let [[source input operations] @captured]
      (is (= [path input-id] [source input]))
      (is (= #{:source-span :diagnostic-ids :domain-anchor :missing-policy} (set (keys operations))))
      (with-redefs [bootstrap/source-span (fn [source ordinal] [:late-span source ordinal])
                    bootstrap/b12-document-diagnostic-ids ["LATE-NATIVE-ID"]
                    bootstrap/b12-document-domain-anchor (constantly :late-context)
                    bootstrap/b12-document-missing-policy (constantly :late-policy)]
        (is (= [:late-span path 3] ((:source-span operations) path 3)))
        (is (= ["LATE-NATIVE-ID"] ((:diagnostic-ids operations))))
        (is (= :late-context ((:domain-anchor operations) "LATE-NATIVE-ID")))
        (is (= :late-policy ((:missing-policy operations) "LATE-NATIVE-ID")))))))

(deftest native-catalog-operation-failure-is-not-replaced-with-data
  (let [error (ex-info "native source span failed" {:operation :source-span})
        operations {:source-span (fn [_ _] (throw error))
                    :diagnostic-ids (fn [] ["B12-THREAD"])
                    :domain-anchor (constantly :native-context)
                    :missing-policy (constantly :native-policy)}
        capture (fn [thunk] (try (thunk) nil (catch clojure.lang.ExceptionInfo actual actual)))]
    (is (identical? error (capture #(mobile/diagnostic-stream "failure.gravity" "native-input" operations))))
    (with-redefs [bootstrap/source-span (fn [_ _] (throw error))]
      (is (identical? error (capture #(bootstrap/b12-document-diagnostic-stream "failure.gravity" "native-input")))))))
