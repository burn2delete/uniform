(ns gravity.bootstrap-compatibility.c12-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.c12-domain-ir :as c12]))

(defn fixture
  [name]
  (str "bootstrap/clojure/fixtures/" name))

(deftest c12-domain-ir-compatibility-wrappers-preserve-interposition
  (is (= '([source-path overrides])
         (:arglists
          (meta #'bootstrap/c12-domain-ir-validate-source-overrides!))))
  (is (= '([source-path source-text])
         (:arglists (meta #'bootstrap/compiler-c12-domain-ir-source-artifact))))
  (is (= '([path])
         (:arglists (meta #'bootstrap/compiler-c12-domain-ir-file-artifact))))
  (let [calls (atom [])]
    (with-redefs [bootstrap/domain-ir-validate-overrides!
                  (fn [source-path artifact]
                    (swap! calls conj [source-path artifact])
                    :complete)]
      (bootstrap/c12-domain-ir-validate-source-overrides!
       "c12-probe.gravity" {:probe true}))
    (is (= 1 (count @calls)))
    (is (= {:probe true}
           (get-in (second (first @calls)) [:source-overrides]))))
  (let [bindings (atom 0)
        with-operations c12/with-operations]
    (with-redefs [c12/with-operations
                  (fn [operations thunk]
                    (swap! bindings inc)
                    (with-operations operations thunk))]
      (bootstrap/compiler-c12-domain-ir-file-artifact
       (fixture "accepted/compiler-c12-domain-ir.gravity")))
    (is (= 1 @bindings)))
  (let [catalog
        (with-redefs [bootstrap/domain-ir-diagnostic-ids ["C12-SENTINEL"]
                      bootstrap/domain-ir-diagnostic-messages
                      {"C12-SENTINEL" "sentinel message"}]
          (bootstrap/c12-domain-ir-diagnostic-catalog "c12-probe.gravity"))]
    (is (= ["C12-SENTINEL"]
           (mapv :diagnostic (:diagnostics catalog))))
    (is (= "sentinel message"
           (get-in catalog [:diagnostics 0 :remediation]))))
  (is (= (:public-api (c12/c12-engine-contract)) c12/public-api)))
