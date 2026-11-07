(ns gravity.compiler-pass-manifest.diagnostic-facts-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.compiler-pass-manifest :as manifest]))

(def ^:private source-path "compiler-passes.gravity")
(def ^:private secret-marker "DIAG_FACTS_SECRET_MARKER")

(defn- upstream-artifact [source-suite]
  {:kind :gravity/stage0-math-conformance-artifact
   :profile-manifest {:metadata {:compiler {:passes source-suite}}}})

(defn- outcome [operation]
  (try
    {:value (operation)}
    (catch clojure.lang.ExceptionInfo exception
      {:diagnostic (ex-data exception)
       :message (.getMessage exception)})))

(defn- boundary-outcomes [source-suite]
  (let [upstream (upstream-artifact source-suite)
        profile-manifest (:profile-manifest upstream)
        suite (manifest/compiler-pass-suite profile-manifest)]
    [[:validator (outcome #(manifest/compiler-pass-validate-diagnostics!
                           source-path profile-manifest suite))]
     [:artifact (outcome #(manifest/compiler-pass-source-artifact-from-upstream
                          source-path upstream))]]))

(defn- assert-preserved [source-suite expected-fixtures]
  (let [[[_ validation] [_ construction]] (boundary-outcomes source-suite)
        artifact (:value construction)]
    (is (= {:value :complete} validation))
    (is (not (contains? construction :diagnostic)))
    (is (= :gravity/stage0-pass-contract-manifest-artifact (:kind artifact)))
    (is (= :complete (get-in artifact [:compiler-pass-results :status])))
    (is (= expected-fixtures (:diagnostic-fixtures artifact)))
    (is (= (:profile-manifest (upstream-artifact source-suite))
           (:profile-manifest artifact)))))

(deftest default-facts-unchanged
  (assert-preserved {} manifest/compiler-pass-default-diagnostic-fixtures))

(deftest structured-facts-preserved
  (let [defaults manifest/compiler-pass-default-diagnostic-fixtures
        facts {:expected-type :i64 :actual-type :string
               :effect :network/http :capability :http/client}
        source-suite {:diagnostic-fixture-overrides
                      [{:diagnostic-id (:diagnostic-id (first defaults))
                        :facts facts}]}]
    (assert-preserved source-suite (assoc-in defaults [0 :facts] facts))))

(deftest nested-fact-values-preserved
  (let [defaults manifest/compiler-pass-default-diagnostic-fixtures
        facts {:types {:expected [:vector :i64] :actual [:vector :string]}
               :effects #{:io/write :network/http}
               :proof {:id "proof/facts" :assumptions []}
               :values [nil false 0 "typed value" {} []]
               :unavailable nil :checked? false}
        fixtures (assoc-in defaults [1 :facts] facts)]
    (assert-preserved {:diagnostic-fixtures fixtures} fixtures)))

(defn- rejected-fixtures [diagnostic]
  [[:missing-facts (dissoc diagnostic :facts) :missing]
   [:nil-facts (assoc diagnostic :facts nil) :missing]
   [:empty-facts (assoc diagnostic :facts {}) :empty]
   [:prose-facts (assoc diagnostic :facts "expected i64, got string") :invalid-shape]
   [:scalar-facts (assoc diagnostic :facts 42) :invalid-shape]
   [:scalar-facts (assoc diagnostic :facts false) :invalid-shape]
   [:scalar-facts (assoc diagnostic :facts :fact) :invalid-shape]
   [:vector-facts (assoc diagnostic :facts [{:expected-type :i64}]) :invalid-shape]
   [:secret-marker-facts (assoc diagnostic :facts secret-marker) :invalid-shape]])

(deftest malformed-facts-refuse-before-artifact-construction
  (let [defaults manifest/compiler-pass-default-diagnostic-fixtures]
    (doseq [fixture-index (range (count defaults))
            [fixture-name diagnostic reason] (rejected-fixtures (nth defaults fixture-index))]
      (testing (str fixture-name " at fixture " fixture-index)
        (let [source-suite {:diagnostic-fixtures (assoc defaults fixture-index diagnostic)}
              results (boundary-outcomes source-suite)]
          (is (= results (boundary-outcomes source-suite)))
          (doseq [[boundary result] results]
            (testing (name boundary)
              (let [failure (:diagnostic result)]
                (is (not (contains? result :value)))
                (is (= "C15-FACTS" (:id failure)))
                (is (= :facts (:schema-field failure)))
                (is (= reason (:reason failure)))
                (is (= fixture-index (:fixture-index failure)))
                (is (= (:diagnostic-id diagnostic) (:offending-diagnostic-id failure)))
                (is (= (:stage diagnostic) (:stage failure)))
                (is (= (get-in diagnostic [:primary :artifact]) (:artifact-id failure)))
                (is (= {:source source-path} (:source-span failure)))
                (is (= (:profile diagnostic) (:profile failure)))
                (is (= (:target diagnostic) (:target failure)))
                (is (seq (:remediation failure)))
                (is (not (contains? failure :facts)))
                (is (not (str/includes? (pr-str result) secret-marker)))))))))))

(deftest existing-refusal-precedence-is-preserved
  (let [defaults manifest/compiler-pass-default-diagnostic-fixtures
        bad-facts (assoc-in defaults [0 :facts] "unstructured")
        duplicate-rule (first manifest/compiler-pass-default-diagnostic-catalog)
        cases [[:schema-before-facts
                {:diagnostic-schema {:required-fields []}
                 :diagnostic-fixtures bad-facts} "C15-SCHEMA"]
               [:catalog-before-facts
                {:diagnostic-catalog [duplicate-rule duplicate-rule]
                 :diagnostic-fixtures bad-facts} "C15-ID"]
               [:span-before-facts
                {:diagnostic-fixtures (assoc-in bad-facts [0 :primary :span] nil)}
                "C15-SPAN"]
               [:origin-before-facts
                {:diagnostic-fixtures (assoc-in bad-facts [0 :origin-chain] [])}
                "C15-ORIGIN"]
               [:facts-before-remediation
                {:diagnostic-fixtures (assoc-in bad-facts [0 :remediation] [])}
                "C15-FACTS"]
               [:facts-before-redaction
                {:diagnostic-fixtures (assoc-in bad-facts [0 :secret-free?] false)}
                "C15-FACTS"]
               [:facts-before-order
                {:diagnostic-fixtures (assoc-in bad-facts [0 :ordering-key] ["Z" 1])}
                "C15-FACTS"]
               [:remediation-after-valid-facts
                {:diagnostic-fixtures (assoc-in defaults [0 :remediation] [])}
                "C15-REMEDIATION"]
               [:redaction-after-valid-facts
                {:diagnostic-fixtures (assoc-in defaults [0 :secret-free?] false)}
                "C15-REDACTION"]
               [:order-after-valid-facts
                {:diagnostic-fixtures (vec (reverse defaults))} "C15-ORDER"]]]
    (doseq [[fixture-name source-suite expected-id] cases]
      (testing (name fixture-name)
        (let [results (boundary-outcomes source-suite)]
          (is (= results (boundary-outcomes source-suite)))
          (doseq [[boundary result] results]
            (testing (name boundary)
              (is (= expected-id (get-in result [:diagnostic :id])))
              (is (not (contains? result :value))))))))))

(deftest first-failing-fixture-determines-the-refusal
  (let [defaults manifest/compiler-pass-default-diagnostic-fixtures
        bad-facts (-> defaults
                      (assoc-in [0 :facts] nil)
                      (assoc-in [1 :facts] "later invalid facts"))]
    (doseq [[fixture-name fixtures expected-id]
            [[:two-bad-facts bad-facts "C15-FACTS"]
             [:later-span-does-not-overtake-earlier-facts
              (assoc-in bad-facts [1 :primary :span] nil) "C15-FACTS"]
             [:earlier-remediation-precedes-later-facts
              (-> defaults
                  (assoc-in [0 :remediation] [])
                  (assoc-in [1 :facts] nil)) "C15-REMEDIATION"]]]
      (testing (name fixture-name)
        (let [source-suite {:diagnostic-fixtures fixtures}
              results (boundary-outcomes source-suite)]
          (is (= results (boundary-outcomes source-suite)))
          (doseq [[boundary result] results]
            (testing (name boundary)
              (is (= expected-id (get-in result [:diagnostic :id])))
              (is (not (contains? result :value)))
              (when (= "C15-FACTS" expected-id)
                (is (= 0 (get-in result [:diagnostic :fixture-index])))
                (is (= :missing (get-in result [:diagnostic :reason])))
                (is (= (:diagnostic-id (first defaults))
                       (get-in result [:diagnostic :offending-diagnostic-id])))))))))))
