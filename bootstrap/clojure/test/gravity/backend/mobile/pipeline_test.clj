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
