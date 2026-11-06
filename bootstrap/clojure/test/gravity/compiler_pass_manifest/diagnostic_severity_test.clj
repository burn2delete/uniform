(ns gravity.compiler-pass-manifest.diagnostic-severity-test
  "C15 fixture severity validation through public validation and construction."
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.compiler-pass-manifest :as manifest]))

(def ^:private source-path "compiler-passes.gravity")
(def ^:private default-fixtures
  manifest/compiler-pass-default-diagnostic-fixtures)

(defn- capture [result-key thunk]
  (try
    {result-key (thunk)}
    (catch clojure.lang.ExceptionInfo exception
      {:diagnostic (ex-data exception)
       :message (.getMessage exception)})))

(defn- exercise [overrides]
  (let [profile-manifest {:metadata {:compiler {:passes overrides}}}
        suite (manifest/compiler-pass-suite profile-manifest)]
    {:validator
     (capture :validation
              #(manifest/compiler-pass-validate-diagnostics!
                source-path profile-manifest suite))
     :constructor
     (capture :artifact
              #(manifest/compiler-pass-source-artifact-from-upstream
                source-path
                {:kind :gravity/stage0-math-conformance-artifact
                 :profile-manifest profile-manifest}))}))

(defn- assert-retained [overrides expected-fixtures]
  (let [{:keys [validator constructor]} (exercise overrides)
        artifact (:artifact constructor)]
    (is (= :complete (:validation validator)))
    (is (= :gravity/stage0-pass-contract-manifest-artifact (:kind artifact)))
    ;; Whole-record equality covers identity, source/origin, facts and metadata.
    (is (= expected-fixtures (:diagnostic-fixtures artifact)))
    (is (= [] (:diagnostics artifact)))))

(defn- assert-refused [overrides expected-fields]
  (doseq [[path outcome] (exercise overrides)]
    (testing (name path)
      (is (= expected-fields
             (select-keys (:diagnostic outcome) (keys expected-fields))))
      (is (seq (get-in outcome [:diagnostic :remediation])))
      (is (not (contains? outcome :validation)))
      (is (not (contains? outcome :artifact)))
      (is (not (contains? (:diagnostic outcome) :severity)))
      (is (not (str/includes? (pr-str outcome) "severity-secret-marker-1268"))))))

(deftest default-fixtures-unchanged
  (assert-retained {} default-fixtures))

(deftest five-recognized-severities-retained
  ;; Spell out the contract rather than deriving the expected set from code.
  (doseq [severity [:error :warning :info :hint :internal-error]]
    (testing (name severity)
      (let [record (assoc (first default-fixtures) :severity severity)]
        (assert-retained
         {:diagnostic-fixture-overrides [record]}
         (assoc default-fixtures 0 record))))))

(deftest extra-record-metadata-preserved
  (let [record (assoc (first default-fixtures)
                      :severity :warning
                      :lifecycle :suppressed-by-policy
                      :producer-metadata {:source-id "fixture-source"
                                          :audit-artifact :diagnostic/audit})]
    (assert-retained {:diagnostic-fixture-overrides [record]}
                     (assoc default-fixtures 0 record))))

(deftest invalid-severities-refused-before-construction
  (let [record (second default-fixtures)
        expected {:id "C15-SCHEMA"
                  :schema-field :severity
                  :fixture-index 1
                  :offending-diagnostic-id (:diagnostic-id record)
                  :stage (:stage record)
                  :artifact-id (get-in record [:primary :artifact])
                  :profile (:profile record)
                  :target (:target record)
                  :source-span {:source source-path}
                  :diagnostic-family :compiler-pass-contract}]
    (doseq [[label bad-record reason]
            [[:missing-severity (dissoc record :severity) :missing-severity]
             [:nil-severity (assoc record :severity nil) :invalid-severity]
             [:unknown-keyword-severity (assoc record :severity :notice) :invalid-severity]
             [:string-severity (assoc record :severity "warning") :invalid-severity]
             [:malformed-secret-marker-severity
              (assoc record :severity {:private-output "severity-secret-marker-1268"})
              :invalid-severity]]]
      (testing (name label)
        (assert-refused {:diagnostic-fixtures (assoc default-fixtures 1 bad-record)}
                        (assoc expected :reason reason))))
    (testing "schema-override-cannot-authorize-severity"
      (doseq [schema-overrides
              [{:diagnostic-schema-overrides {:supported-severities [:notice]}}
               {:diagnostic-schema
                (assoc manifest/compiler-pass-default-diagnostic-schema
                       :supported-severities [:notice])}]]
        (assert-refused
         (assoc schema-overrides :diagnostic-fixture-overrides
                [{:diagnostic-id (:diagnostic-id record) :severity :notice}])
         (assoc expected :reason :invalid-severity))))))

(deftest severity-refusal-precedes-per-record-field-checks
  (assert-refused
   {:diagnostic-fixtures [(-> (first default-fixtures)
                             (dissoc :severity :primary :origin-chain :facts
                                     :remediation :secret-free?))]}
   {:id "C15-SCHEMA" :schema-field :severity :reason :missing-severity
    :fixture-index 0
    :offending-diagnostic-id (:diagnostic-id (first default-fixtures))}))

(deftest earlier-schema-and-catalog-refusals-retained
  (let [malformed-fixtures [(dissoc (first default-fixtures) :severity)]
        duplicate-catalog [(first manifest/compiler-pass-default-diagnostic-catalog)
                           (first manifest/compiler-pass-default-diagnostic-catalog)]]
    (testing "required schema fields precede catalog and severity"
      (assert-refused {:diagnostic-fixtures malformed-fixtures
                       :diagnostic-schema-overrides {:required-fields []}
                       :diagnostic-catalog duplicate-catalog}
                      {:id "C15-SCHEMA" :schema-field :required-fields}))
    (testing "catalog identity precedes severity"
      (assert-refused {:diagnostic-fixtures malformed-fixtures
                       :diagnostic-catalog duplicate-catalog}
                      {:id "C15-ID" :stage :diagnostic-catalog}))))

(deftest existing-per-record-and-stream-refusals-retained
  (doseq [[field rule]
          [[:primary "C15-SPAN"]
           [:origin-chain "C15-ORIGIN"]
           [:facts "C15-FACTS"]
           [:remediation "C15-REMEDIATION"]
           [:secret-free? "C15-REDACTION"]]]
    (testing (name field)
      (assert-refused
       {:diagnostic-fixtures [(dissoc (first default-fixtures) field)]}
       {:id rule})))
  (testing "stream ordering still follows record checks"
    (assert-refused {:diagnostic-fixtures (vec (reverse default-fixtures))}
                    {:id "C15-ORDER" :stage :diagnostic-stream})))
