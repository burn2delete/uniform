(ns gravity.ai.metric-threshold-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(def ^:private source-path
  "bootstrap/clojure/fixtures/accepted/ai-agentic.gravity")

(defn- rejection [report]
  (with-redefs [bootstrap/p11-evaluation-report (constantly report)]
    (try
      (bootstrap/ai-agentic-file-artifact source-path)
      nil
      (catch clojure.lang.ExceptionInfo error
        (ex-data error)))))

(deftest evaluation-accepts-observations-at-or-above-the-declared-minimum
  (let [report (bootstrap/p11-evaluation-report)
        artifact (bootstrap/ai-agentic-file-artifact source-path)]
    (is (some? (:artifact-id artifact)))
    (is (= :passed (get-in artifact [:evaluation-report :release-gate])))
    (is (= report (:evaluation-report artifact)))
    (doseq [[threshold observed] [[0.85 0.85] [0.80 0.84]]]
      (testing (str "precision " observed " at minimum " threshold)
        (let [updated-report (assoc-in report [:metrics :finding-precision]
                                       {:direction :min
                                        :threshold threshold
                                        :observed observed})]
          (with-redefs [bootstrap/p11-evaluation-report (constantly updated-report)]
            (let [artifact (bootstrap/ai-agentic-file-artifact source-path)]
              (is (some? (:artifact-id artifact)))
              (is (= updated-report (:evaluation-report artifact))))))))))

(deftest passing-gate-label-cannot-override-a-failed-metric
  (let [report (bootstrap/p11-evaluation-report)]
    (doseq [[metric comparison]
            [[:finding-precision {:direction :min :threshold 0.85 :observed 0.84}]
             [:schema-validity {:direction :equals :threshold 1.0 :observed 0.99}]
             [:schema-validity {:direction :equals :threshold 0.98 :observed 0.99}]
             [:finding-precision {:threshold 0.85 :observed 0.91}]
             [:finding-precision {:direction :min :observed 0.91}]
             [:finding-precision {:direction :min :threshold 0.85}]]]
      (testing (str metric " " comparison)
        (let [diagnostic (rejection (assoc-in report [:metrics metric] comparison))]
          (is (= (merge {:id "A9004"
                         :document-id "A9"
                         :source-span (bootstrap/source-span source-path 0)
                         :eval-id :support-triage-release
                         :subject-hash (:subject-hash report)
                         :dataset "SupportTriageCases/v4"
                         :provider-id :primary-ai
                         :release-gate :passed
                         :metric metric}
                        comparison)
                 (select-keys diagnostic
                              [:id :document-id :source-span :eval-id :subject-hash
                               :dataset :provider-id :release-gate :metric
                               :direction :threshold :observed]))))))))

(deftest missing-release-gate-retains-its-diagnostic
  (let [diagnostic (rejection (dissoc (bootstrap/p11-evaluation-report) :release-gate))]
    (is (= {:id "A9001" :document-id "A9" :missing-fields [:eval-gate]}
           (select-keys diagnostic [:id :document-id :missing-fields])))))
