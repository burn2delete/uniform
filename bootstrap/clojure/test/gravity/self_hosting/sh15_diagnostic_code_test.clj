(ns gravity.self-hosting.sh15-diagnostic-code-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh15-published-diagnostic-identity-test]))

;; Reuse the existing paired accepted source fixture and actual C12 producer.
(defn- row [anchor]
  (#'gravity.self-hosting.sh15-published-diagnostic-identity-test/row
   anchor "/source.gravity"))
(defn- request [diagnostic]
  (#'gravity.self-hosting.sh15-published-diagnostic-identity-test/request [diagnostic]))
(defn- c15 [function arguments]
  (#'gravity.self-hosting.sh15-published-diagnostic-identity-test/c15 function arguments))
(defn- compile-plan [path]
  (#'gravity.self-hosting.sh15-absent-layout-location-test/compile-plan path))
(defn- fixture-path [extension]
  (str "bootstrap/clojure/fixtures/self-hosting/sh-15/rejected/coordinator-diagnostic-code."
       extension))
(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh15-diagnostic-code-development :compiler-artifact-plan? true}
   plan function arguments))
(defn- reason [result] (get-in result [:diagnostics 0 :facts :reason]))

(deftest sh15-empty-rule-and-upstream-code-are-malformed-diagnostic-data
  (let [original (row "code-field")
        malformed (invoke (compile-plan (fixture-path "gravity"))
                          'coordinator-diagnostic-code-empty [original])
        fresh (compile-plan
               "bootstrap/gravity/src/gravity/compiler/c15_compiler_diagnostics.gravity")]
    (is (= :request-schema-mismatch (:reason original)))
    (is (= "S7-LAYOUT" (:rule original) (:diagnostic-id original)))
    (is (= :accepted (:status (c15 'sh15-build-diagnostic-boundary [(request original)]))))
    (doseq [diagnostic malformed]
      (let [before (pr-str diagnostic) input (request diagnostic)
            result (c15 'sh15-build-diagnostic-boundary [input])]
        (is (false? (c15 'sh15-diagnostic-input-valid? [input])))
        (is (= :rejected (:status result)))
        (is (= :upstream-diagnostic-schema (reason result)))
        (is (= :upstream-diagnostic-schema
               (reason (invoke fresh 'sh15-verify-diagnostic-boundary [input result]))))
        (is (= result (invoke fresh 'sh15-build-diagnostic-boundary [input])))
        (is (= before (pr-str diagnostic)))))
    (let [candidate (c15 'sh15-build-diagnostic-boundary [(request original)])
          diagnostic (get-in candidate [:diagnostic-stream :diagnostics 0])
          view (c15 'sh15-render-bounded-diagnostic-text [(assoc diagnostic :rule "")])]
      (is (= :rejected (:status view)))
      (is (= :empty-rule (reason view))))))

(deftest sh15-code-validation-retains-codes-and-the-optional-anchor-domain
  (let [original (row "code-field")]
    ;; Supplied code controls, not claims that C12 emitted new codes.
    (doseq [field [:rule :diagnostic-id]]
      (let [code (apply str (repeat 700 "X")) input (request (assoc original field code))
            result (c15 'sh15-build-diagnostic-boundary [input])
            preserved (get-in result [:diagnostic-stream :diagnostics 0])]
        (is (true? (c15 'sh15-diagnostic-input-valid? [input])))
        (is (= :accepted (:status result)))
        (is (= code (get preserved (if (= :rule field) :rule :upstream-diagnostic-id))))
        (is (= :passed (:status (c15 'sh15-verify-diagnostic-boundary [input result])))))))
  (let [original (row "") result (c15 'sh15-build-diagnostic-boundary [(request original)])]
    (is (= "" (:semantic-anchor original)))
    (is (= :accepted (:status result)))
    (is (= "" (get-in result [:diagnostic-stream :diagnostics 0 :facts :semantic-anchor])))))

(deftest sh15-empty-code-rejected-fixtures-are-co-canonical
  (let [g (fixture-path "gravity") q (fixture-path "qst") original (row "code-field")]
    (is (= (slurp g) (slurp q)))
    (is (= (invoke (compile-plan g) 'coordinator-diagnostic-code-empty [original])
           (invoke (compile-plan q) 'coordinator-diagnostic-code-empty [original])))))
