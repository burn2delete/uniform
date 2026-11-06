(ns gravity.compiler-pass-manifest.trust-report-test
  "C18 declared report consistency; no evidence authentication or pass execution."
  (:require [clojure.string :as string]
            [clojure.test :refer [deftest is testing]]
            [gravity.compiler-pass-manifest :as manifest]))

(def ^:private source-path "trust-report.gravity")
(def ^:private default-suite (manifest/compiler-pass-suite {}))
(def ^:private default-report (:compiler-trust-report default-suite))

(defn- profile-manifest [options]
  {:profile :meta :target {:backend :jvm :triple "stage0"}
   :metadata {:compiler {:passes options}}})

(defn- construct [profile]
  (manifest/compiler-pass-source-artifact-from-upstream
   source-path {:kind :gravity/stage0-math-conformance-artifact
                :profile-manifest profile}))

(defn- attempt [thunk]
  (try
    {:value (thunk)}
    (catch clojure.lang.ExceptionInfo exception
      {:diagnostic (ex-data exception)})))

(deftest accepted-detailed-reports-are-retained
  (is (= {:artifact :gravity/compiler-trust-report
          :compiler :gravity-stage0-clojure-bootstrap
          :passes (mapv #(select-keys % [:pass :risk :available-evidence])
                        (:risk-classification default-suite))
          :profiles {:meta {:required-evidence :high :blocked-passes []}}
          :known-gaps []
          :covered-passes (mapv :pass (:contracts default-suite))}
         default-report))
  (let [extra {:pass :additional-report-pass :risk :low
               :available-evidence #{:conformance-fixture}
               :origin-chain [{:source source-path :producer :fixture}]}
        extended (-> default-report
                     (update :passes conj extra)
                     (update :covered-passes conj (:pass extra)))
        reordered (update default-report :passes #(vec (reverse %)))
        without-summary (dissoc default-report :covered-passes)]
    (doseq [[fixture options expected]
            [[:default-report-unchanged {} default-report]
             [:complete-details-without-summary
              {:compiler-trust-report without-summary} without-summary]
             [:reordered-details
              {:compiler-trust-report-overrides {:passes (:passes reordered)}}
              reordered]
             [:additional-pass-record-retained
              {:compiler-trust-report-overrides
               (select-keys extended [:passes :covered-passes])}
              extended]]]
      (testing (name fixture)
        (let [profile (profile-manifest options)
              suite (manifest/compiler-pass-suite profile)
              artifact (construct profile)]
          (is (= :complete (manifest/compiler-pass-validate-verification!
                            source-path profile suite)))
          (is (true? (:verification-gates-present?
                      (manifest/compiler-pass-capability-proof suite))))
          (is (= :gravity/stage0-pass-contract-manifest-artifact (:kind artifact)))
          (is (= expected (:compiler-trust-report suite)))
          (is (= expected (:compiler-trust-report artifact)))
          (is (true? (get-in artifact [:capability-based-proof
                                      :verification-gates-present?])))
          (is (= profile (:profile-manifest artifact))))))))

(deftest incomplete-and-contradictory-reports-block-artifacts
  (let [required (mapv :pass (:contracts default-suite))
        critical-pass :read-source
        omitted (vec (remove #(= critical-pass (:pass %))
                             (:passes default-report)))]
    (is (= :critical (:risk (first (:passes default-report)))))
    (doseq [[fixture options reason missing]
            [[:summary-only-coverage
              {:compiler-trust-report (dissoc default-report :passes)}
              :missing-pass required]
             [:empty-details-with-complete-summary
              {:compiler-trust-report-overrides {:passes []}}
              :missing-pass required]
             [:omitted-critical-pass-with-complete-summary
              {:compiler-trust-report-overrides {:passes omitted}}
              :missing-pass [critical-pass]]
             [:contradictory-summary
              {:compiler-trust-report-overrides
               {:covered-passes (conj required :summary-only-pass)}}
              :summary-mismatch [:covered-passes]]
             [:shortened-summary
              {:compiler-trust-report-overrides
               {:covered-passes (vec (rest required))}}
              :summary-mismatch [:covered-passes]]
             [:nil-summary
              {:compiler-trust-report-overrides {:covered-passes nil}}
              :summary-mismatch [:covered-passes]]
             [:malformed-summary
              {:compiler-trust-report-overrides {:covered-passes :not-a-summary}}
              :summary-mismatch [:covered-passes]]]]
      (testing (name fixture)
        (let [profile (profile-manifest options)
              suite (manifest/compiler-pass-suite profile)
              validation (attempt #(manifest/compiler-pass-validate-verification!
                                    source-path profile suite))
              construction (attempt #(construct profile))]
          (is (false? (:verification-gates-present?
                       (manifest/compiler-pass-capability-proof suite))))
          (doseq [result [validation construction]]
            (is (nil? (:value result)))
            (is (= "C18-TRUST-REPORT" (get-in result [:diagnostic :id])))
            (is (= reason (get-in result [:diagnostic :reason])))
            (is (= missing (get-in result [:diagnostic :missing-fields])))
            (is (= {:source source-path}
                   (get-in result [:diagnostic :source-span])))))))))

(deftest malformed-detailed-record-diagnostics-exclude-payloads
  (let [secret "private-detailed-report-payload"
        poisoned {:risk secret :available-evidence [secret]
                  :profile secret :target secret :artifact secret}
        records (:passes default-report)]
    (doseq [[fixture passes index]
            [[:missing-pass (conj records poisoned) (count records)]
             [:nil-pass (conj records (assoc poisoned :pass nil)) (count records)]
             [:scalar-record (conj records secret) (count records)]
             [:first-record-malformed (assoc records 0 poisoned) 0]
             [:nil-record (conj records nil) (count records)]
             [:malformed-records-container {:private secret} nil]]]
      (testing (name fixture)
        (let [profile (profile-manifest
                       {:compiler-trust-report-overrides {:passes passes}})
              suite (manifest/compiler-pass-suite profile)]
          (is (false? (:verification-gates-present?
                       (manifest/compiler-pass-capability-proof suite))))
          (doseq [result [(attempt #(manifest/compiler-pass-validate-verification!
                                    source-path profile suite))
                          (attempt #(construct profile))]]
            (let [diagnostic (:diagnostic result)]
              (is (nil? (:value result)))
              (is (= "C18-TRUST-REPORT" (:id diagnostic)))
              (is (= :malformed-record (:reason diagnostic)))
              (is (= index (:record-index diagnostic)))
              (is (= (if (some? index) [:pass] [:passes])
                     (:missing-fields diagnostic)))
              (is (not (string/includes? (pr-str diagnostic) secret)))
              (is (= :meta (:profile diagnostic)))
              (is (= (:target profile) (:target diagnostic))))))))))

(deftest earlier-evidence-failures-keep-precedence
  (doseq [[expected options]
          [["C18-RISK"
            {:risk-overrides [{:pass :read-source :risk nil}]}]
           ["C18-EVIDENCE"
            {:risk-overrides [{:pass :read-source :available-evidence #{}}]}]
           ["C1-PASS-CONTRACT"
            {:contract-overrides [{:pass :read-source :output nil}]}]]]
    (testing expected
      (let [profile (profile-manifest
                     (assoc options :compiler-trust-report-overrides {:passes []}))
            suite (manifest/compiler-pass-suite profile)]
        (when (not= expected "C1-PASS-CONTRACT")
          (is (= expected
                 (get-in (attempt #(manifest/compiler-pass-validate-verification!
                                    source-path profile suite))
                         [:diagnostic :id]))))
        (let [result (attempt #(construct profile))]
          (is (nil? (:value result)))
          (is (= expected (get-in result [:diagnostic :id]))))))))
