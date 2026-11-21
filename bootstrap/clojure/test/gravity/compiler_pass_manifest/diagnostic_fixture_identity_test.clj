(ns gravity.compiler-pass-manifest.diagnostic-fixture-identity-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.compiler-pass-manifest :as manifest]))

(def ^:private source-path "compiler-passes.gravity")
(def ^:private fact-marker "DIAGNOSTIC_FIXTURE_IDENTITY_FACT_MARKER")

(defn- boundary-operations
  ([source-suite] (boundary-operations source-suite {}))
  ([source-suite manifest-context]
   (let [profile-manifest (assoc manifest-context :metadata {:compiler {:passes source-suite}})
         suite (manifest/compiler-pass-suite profile-manifest)
         upstream {:kind :gravity/stage0-math-conformance-artifact
                   :profile-manifest profile-manifest}]
     {:validator #(manifest/compiler-pass-validate-diagnostics!
                   source-path profile-manifest suite)
      :artifact #(manifest/compiler-pass-source-artifact-from-upstream
                  source-path upstream)})))

(defn- capture-outcome [operation]
  (try
    {:value (operation)}
    (catch clojure.lang.ExceptionInfo exception
      {:diagnostic (ex-data exception)
       :message (.getMessage exception)})))

(defn- repeated-rule-fixtures []
  (let [first-fixture (first manifest/compiler-pass-default-diagnostic-fixtures)
        second-artifact :pass-contract/verify-mir
        second-span "compiler/passes.gravity:2:1"]
    [first-fixture
     (assoc first-fixture
            :diagnostic-id "diag/c1-pass-contract-verify-mir"
            :primary {:span second-span :artifact second-artifact}
            :related [{:role :pass-contract :artifact second-artifact}]
            :origin-chain [{:kind :source :span second-span}]
            :artifacts [second-artifact]
            :facts {:missing-field :output :detail fact-marker}
            :ordering-key ["C1" 2])]))

(deftest unique-diagnostic-identities-are-preserved
  (let [repeated-rule (repeated-rule-fixtures)]
    (doseq [[label source-suite expected-fixtures]
            [["default stream" {} manifest/compiler-pass-default-diagnostic-fixtures]
             ["repeated rule with distinct subjects and ids"
              {:diagnostic-fixtures repeated-rule} repeated-rule]]]
      (testing label
        (let [{:keys [validator artifact]} (boundary-operations source-suite)
              emitted (artifact)]
          (is (= :complete (validator)))
          (is (= :gravity/stage0-pass-contract-manifest-artifact (:kind emitted)))
          (is (= expected-fixtures (:diagnostic-fixtures emitted))))))))

(deftest duplicate-diagnostic-identity-refuses-artifact-construction
  (let [fixtures (repeated-rule-fixtures)
        duplicate-id (:diagnostic-id (first fixtures))
        other-rule (second manifest/compiler-pass-default-diagnostic-fixtures)]
    (doseq [[duplicate-fixtures duplicate-index]
            [[(assoc-in fixtures [1 :diagnostic-id] duplicate-id) 1]
             [(conj fixtures (assoc other-rule :diagnostic-id duplicate-id)) 2]]
            :let [duplicate (nth duplicate-fixtures duplicate-index)]
            [boundary operation] (boundary-operations {:diagnostic-fixtures duplicate-fixtures})]
      (testing (str (name boundary) " at fixture " duplicate-index)
        (let [{:keys [diagnostic] :as result} (capture-outcome operation)]
          (is (= result (capture-outcome operation)))
          (is (not (contains? result :value)))
          (is (= {:id "C15-ID"
                  :schema-field :diagnostic-id
                  :reason :duplicate
                  :fixture-index duplicate-index
                  :offending-diagnostic-id duplicate-id
                  :stage (:stage duplicate)
                  :profile (:profile duplicate)
                  :target (:target duplicate)
                  :artifact-id (get-in duplicate [:primary :artifact])}
                 (select-keys diagnostic [:id :schema-field :reason :fixture-index
                                          :offending-diagnostic-id :stage :profile :target
                                          :artifact-id])))
          (is (seq (:remediation diagnostic)))
          (is (not (contains? diagnostic :facts)))
          (is (not (str/includes? (pr-str result) fact-marker))))))))

(deftest duplicate-diagnostic-context-is-redacted
  (let [[first-fixture second-fixture] (repeated-rule-fixtures)
        private-facts (:facts second-fixture)
        primary-artifact (get-in second-fixture [:primary :artifact])
        duplicate (assoc second-fixture
                         :diagnostic-id (:diagnostic-id first-fixture)
                         :artifact-id private-facts
                         :stage private-facts
                         :profile private-facts
                         :target private-facts)
        source-suite {:diagnostic-fixtures [first-fixture duplicate]}]
    (doseq [[label manifest-context expected-context]
            [["valid manifest context" {:profile :meta :target :jvm} {:profile :meta :target :jvm}]
             ["malformed manifest context" {:profile private-facts :target private-facts}
              {:profile nil :target nil}]]
            [boundary operation] (boundary-operations source-suite manifest-context)]
      (testing (str label " at " (name boundary))
        (let [{:keys [diagnostic message] :as result} (capture-outcome operation)]
          (is (not (contains? result :value)))
          (is (= (assoc expected-context
                        :id "C15-ID" :fixture-index 1 :stage nil :artifact-id primary-artifact)
                 (select-keys diagnostic [:id :fixture-index :stage :profile :target :artifact-id])))
          (is (not (str/includes? (pr-str diagnostic) fact-marker)))
          (is (not (str/includes? (str message) fact-marker))))))))

(deftest duplicate-malformed-diagnostic-identity-is-redacted
  (let [id-marker "DIAGNOSTIC_FIXTURE_PRIVATE_ID_MARKER"
        malformed-id {:facts {:private-detail id-marker}}
        fixtures (mapv #(assoc % :diagnostic-id malformed-id) (repeated-rule-fixtures))]
    (doseq [[boundary operation] (boundary-operations {:diagnostic-fixtures fixtures})]
      (testing (name boundary)
        (let [{:keys [diagnostic] :as result} (capture-outcome operation)]
          (is (not (contains? result :value)))
          (is (= {:id "C15-ID"
                  :schema-field :diagnostic-id
                  :reason :duplicate
                  :fixture-index 1
                  :offending-diagnostic-id nil}
                 (select-keys diagnostic [:id :schema-field :reason :fixture-index
                                          :offending-diagnostic-id])))
          (is (not (str/includes? (pr-str result) id-marker))))))))

(deftest existing-fixture-failures-precede-duplicate-identity
  (let [fixtures (repeated-rule-fixtures)
        duplicate-fixtures (assoc-in fixtures [1 :diagnostic-id]
                                     (:diagnostic-id (first fixtures)))]
    (doseq [[invalid-fixtures expected-id]
            [[(assoc-in duplicate-fixtures [1 :facts] nil) "C15-FACTS"]
             [(assoc-in duplicate-fixtures [1 :secret-free?] false) "C15-REDACTION"]
             [(vec (reverse duplicate-fixtures)) "C15-ORDER"]]
            [boundary operation] (boundary-operations {:diagnostic-fixtures invalid-fixtures})]
      (testing (str expected-id " at " (name boundary))
        (let [result (capture-outcome operation)]
          (is (= expected-id (get-in result [:diagnostic :id])))
          (is (not (contains? result :value))))))))
