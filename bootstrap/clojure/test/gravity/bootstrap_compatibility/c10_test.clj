(ns gravity.bootstrap-compatibility.c10-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.c10-safety-analysis :as c10]))

(defn fixture
  [name]
  (str "bootstrap/clojure/fixtures/" name))

(deftest c10-safety-analysis-compatibility-wrappers-preserve-interposition
  (is (= '([module inventory])
         (:arglists (meta #'bootstrap/c10-safety-outcome-records))))
  (is (= '([source-path source-text])
         (:arglists (meta #'bootstrap/compiler-c10-safety-source-artifact))))
  (is (= '([path])
         (:arglists (meta #'bootstrap/compiler-c10-safety-file-artifact))))
  (let [calls (atom 0)
        proof {:operation-inventory-complete? true
               :exactly-one-outcome-per-operation? true
               :runtime-checks-emitted? true
               :proof-obligations-discharged? true
               :certificate-references-recorded? true
               :unsafe-island-audits-complete? true
               :taint-and-capability-reports-complete? true
               :generated-provenance-recorded? true
               :optimization-evidence-preserved? true
               :diagnostics-covered? true
               :verifier-passed? true}]
    (is (= :complete
           (with-redefs [bootstrap/c10-safety-capability-proof
                         (fn [_] (swap! calls inc) proof)]
             (bootstrap/c10-safety-validate! "c10-probe.gravity" {}))))
    (is (= 1 @calls)))
  (let [bindings (atom 0)
        with-operations c10/with-operations]
    (with-redefs [c10/with-operations
                  (fn [operations thunk]
                    (swap! bindings inc)
                    (with-operations operations thunk))]
      (bootstrap/compiler-c10-safety-file-artifact
       (fixture "accepted/compiler-c10-safety-analysis.gravity")))
    (is (= 1 @bindings)))
  (let [artifact
        (with-redefs [bootstrap/c10-safety-diagnostic-ids ["C10-SENTINEL"]
                      bootstrap/c10-safety-rejected-designs
                      [{:diagnostic "C10-SENTINEL"}]
                      bootstrap/c10-safety-governing-document
                      "docs/c10-sentinel.md"]
          (bootstrap/compiler-c10-safety-file-artifact
           (fixture "accepted/compiler-c10-safety-analysis.gravity")))]
    (is (= "docs/c10-sentinel.md" (:governing-document artifact)))
    (is (= ["C10-SENTINEL"]
           (get-in artifact [:c10-safety-analysis-results
                             :required-diagnostic-ids])))
    (is (= #{"C10-SENTINEL"}
           (set (map :diagnostic
                     (get-in artifact [:safety-diagnostics :diagnostics]))))))
  (is (= (:public-api (c10/c10-engine-contract)) c10/public-api)))
