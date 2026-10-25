(ns gravity.self-hosting.sh15-bootstrap-diagnostic-record-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh15-absent-layout-location-test]
            [gravity.self-hosting.sh15-published-diagnostic-identity-test]))

(def ^:private source-path "bootstrap/gravity/src/gravity/bootstrap/diagnostics.gravity")
(defn- compile-plan []
  (#'gravity.self-hosting.sh15-absent-layout-location-test/compile-plan source-path))
(def ^:private plan (delay (compile-plan)))
(defn- invoke [compiled arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh15-bootstrap-record-development :compiler-artifact-plan? true}
   compiled 'diagnostic-record arguments))
(defn- c15 [function arguments]
  (#'gravity.self-hosting.sh15-published-diagnostic-identity-test/c15 function arguments))
(defn- located [path]
  (#'gravity.self-hosting.sh15-published-diagnostic-identity-test/original
   (#'gravity.self-hosting.sh15-published-diagnostic-identity-test/source-request
    "bootstrap-record" path)))
(defn- source-cases []
  [[:c15-owned (c15 'sh15-build-diagnostic-boundary [{}])]
   [:c12-located (located "/source.gravity")]
   [:c12-absent
    (#'gravity.self-hosting.sh15-absent-layout-location-test/original-rejection)]])
(defn- arguments [diagnostic]
  [(:rule diagnostic)
   (if (contains? diagnostic :source-span)
     (:source-span diagnostic)
     (get-in diagnostic [:primary :span]))
   (:remediation diagnostic)])

(deftest sh15-bootstrap-record-preserves-actual-c15-and-c12-rejection-data
  ;; Existing paired source fixtures produce the real rejected inputs above.
  ;; This three-field record preserves data; it confers no canonical C15 ID,
  ;; validation, rendering, source authentication or migration authority.
  (doseq [[family source] (source-cases)]
    (let [diagnostic (first (:diagnostics source)) before (pr-str source)
          [code span remediation :as supplied] (arguments diagnostic)
          result (invoke @plan supplied)]
      (is (= :rejected (:status source)) family)
      (is (= :request-schema-mismatch
             (if (= :c15-owned family)
               (get-in diagnostic [:facts :reason])
               (:reason diagnostic))))
      (is (= (if (= :c15-owned family) "C15-SCHEMA" "S7-LAYOUT") code))
      (is (= #{:status :code :span :remediation :preserves} (set (keys result))))
      (is (= :stage1-source-owned (:status result)))
      (is (= code (:code result)))
      (is (= span (:span result)))
      (is (= remediation (:remediation result)))
      (is (= [:diagnostic-codes :source-spans] (:preserves result)))
      (is (= before (pr-str source)))
      (when (not= :c12-located family)
        (is (contains? result :span))
        (is (nil? (:span result)))))))

(deftest sh15-bootstrap-record-replays-and-preserves-physical-span-changes
  (let [fresh (compile-plan)]
    (doseq [[_ source] (source-cases)]
      (let [supplied (arguments (first (:diagnostics source)))
            result (invoke @plan supplied)]
        (is (= result (invoke @plan supplied)))
        (is (= result (invoke fresh supplied)))))
    (let [a (located "/checkout-a/source.gravity")
          b (located "/checkout-b/source.gravity")
          ra (invoke fresh (arguments (first (:diagnostics a))))
          rb (invoke fresh (arguments (first (:diagnostics b))))]
      (is (= :rejected (:status a) (:status b)))
      (is (= "S7-LAYOUT" (:code ra) (:code rb)))
      (is (= (:remediation ra) (:remediation rb)))
      (is (not= (:span ra) (:span rb)))
      (is (= "/checkout-a/source.gravity" (get-in ra [:span :actual-source-path])))
      (is (= "/checkout-b/source.gravity" (get-in rb [:span :actual-source-path]))))))
