(ns gravity.self-hosting.sh15-early-carrier-diagnostic-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh15-absent-layout-location-test]))

(def ^:private compile-plan
  #'gravity.self-hosting.sh15-absent-layout-location-test/compile-plan)
(def ^:private c15-path
  "bootstrap/gravity/src/gravity/compiler/c15_compiler_diagnostics.gravity")
(def ^:private c15-plan (delay (compile-plan c15-path)))
(def ^:private c12-plan
  (delay (compile-plan
          "bootstrap/gravity/src/gravity/compiler/c12_domain_ir_architecture.gravity")))
(defn- fixture-path [family extension]
  (str "bootstrap/clojure/fixtures/self-hosting/sh-15/" family
       "/coordinator-early-carrier-diagnostic." extension))
(def ^:private accepted-plan (delay (compile-plan (fixture-path "accepted" "gravity"))))
(def ^:private rejected-plan (delay (compile-plan (fixture-path "rejected" "gravity"))))
(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh15-early-carrier-development :compiler-artifact-plan? true}
   plan function arguments))
(defn- c15 [function arguments] (invoke @c15-plan function arguments))
(defn- source-request [member]
  (invoke @accepted-plan 'coordinator-early-carrier-diagnostic-request [member]))
(defn- original [member]
  (invoke @c12-plan 'sh14-build-authenticated-layout [(source-request member)]))
(defn- row [member] (first (:diagnostics (original member))))
(defn- request [diagnostic]
  (#'gravity.self-hosting.sh15-absent-layout-location-test/boundary-request diagnostic))
(defn- build [input] (c15 'sh15-build-diagnostic-boundary [input]))
(defn- reason [result] (get-in result [:diagnostics 0 :facts :reason]))
(defn- expected-reason [member]
  (case member :artifact :artifact-mismatch :version :schema-version-mismatch
        :request-member-carrier-rejected))

(deftest sh15-early-carrier-original-producer-yields-structured-rejections
  (doseq [member [:metadata :facts :anchor :provenance :artifact :version]]
    (let [result (original member) diagnostic (first (:diagnostics result))]
      (is (= :rejected (:status result)))
      (is (= "S7-LAYOUT" (:rule diagnostic) (:diagnostic-id diagnostic)))
      (is (= (expected-reason member) (:reason diagnostic)))
      (is (= :authenticated-layout-contract (:missing-fact diagnostic)))
      (is (contains? diagnostic :source-span))
      (is (nil? (:source-span diagnostic)))
      (is (= [] (:generated-origin-chain diagnostic)))
      (is (= :safe (:profile diagnostic)))
      (is (= :portable (:target diagnostic)))
      (is (not (str/includes? (pr-str diagnostic) "PRIVATE-PAYLOAD")))))
  (is (= :c11-verification-carrier-rejected
         (:reason (row :normal)))))

(deftest sh15-early-carrier-diagnostic-preserves-explicit-unavailable-location
  (doseq [member [:metadata :facts :anchor :provenance :artifact :version]]
    (let [diagnostic (row member) before (pr-str diagnostic) input (request diagnostic)
          candidate (build input) preserved (get-in candidate [:diagnostic-stream :diagnostics 0])
          view (c15 'sh15-render-bounded-diagnostic-text [preserved])]
      (is (true? (c15 'sh15-diagnostic-input-valid? [input])))
      (is (= :accepted (:status candidate)))
      (is (= "S7-LAYOUT" (:rule preserved) (:upstream-diagnostic-id preserved)))
      (is (= (expected-reason member) (get-in preserved [:facts :reason])))
      (is (= (:remediation diagnostic) (first (:remediation preserved))))
      (is (= {:span nil :artifact nil} (:primary preserved)))
      (is (nil? (get-in candidate [:identity-input :diagnostics 0 :primary :span])))
      (is (= :accepted (:status view)))
      (is (= "code=\"S7-LAYOUT\" severity=:error location=<redacted> redactions=:not-evaluated"
             (:text view)))
      (is (= before (pr-str diagnostic)))
      (is (= diagnostic (row member)))
      (is (= :none (:semantic-authority candidate)))
      (is (= :external-reference-shape-and-equality-only (:verification-status candidate)))
      (is (false? (:executable? candidate)))
      (is (false? (:self-hosted? candidate)))
      (is (= :passed (:status (c15 'sh15-verify-diagnostic-boundary [input candidate])))))))

(deftest sh15-early-carrier-diagnostic-fresh-verification-and-malformed-controls
  (let [diagnostic (row :facts) input (request diagnostic) candidate (build input)
        fresh (compile-plan c15-path)]
    (is (= candidate (invoke fresh 'sh15-build-diagnostic-boundary [input])))
    (is (= :passed (:status (invoke fresh 'sh15-verify-diagnostic-boundary [input candidate]))))
    (doseq [bad (concat (invoke @rejected-plan 'coordinator-early-carrier-diagnostic-invalid-rows [diagnostic])
                       [(dissoc diagnostic :source-span) (assoc diagnostic :extra :forbidden)
                        (row :normal)])]
      (let [result (build (request bad))]
        (is (= :rejected (:status result)))
        (is (= :upstream-diagnostic-schema (reason result)))))
    (doseq [path [[:diagnostic-stream :diagnostics 0 :primary :span]
                  [:identity-input :diagnostics 0 :primary :span]]]
      (let [forged (assoc-in candidate path {:source-id nil :start nil :end nil})
            result (invoke fresh 'sh15-verify-diagnostic-boundary [input forged])]
        (is (= :rejected (:status result)))
        (is (= :candidate-substitution (reason result)))))))

(deftest sh15-early-carrier-diagnostic-paired-source-fixtures
  (let [diagnostic (row :facts)]
    (doseq [family ["accepted" "rejected"]]
      (let [g (fixture-path family "gravity") q (fixture-path family "qst")
            gp (if (= "accepted" family) @accepted-plan @rejected-plan)
            qp (compile-plan q)
            function (if (= "accepted" family)
                       'coordinator-early-carrier-diagnostic-request
                       'coordinator-early-carrier-diagnostic-invalid-rows)
            arguments (if (= "accepted" family) [:facts] [diagnostic])]
        (is (= (slurp g) (slurp q)))
        (is (= (invoke gp function arguments) (invoke qp function arguments)))))))

(deftest sh15-early-envelope-records-preserve-semantics-order-and-counts
  (let [rows (mapv row [:facts :artifact :version])
        before (pr-str rows)
        input (assoc (request (first rows)) :upstream-diagnostics rows)
        candidate (build input)
        diagnostics (get-in candidate [:diagnostic-stream :diagnostics])]
    (is (= :accepted (:status candidate)))
    (is (= [:request-member-carrier-rejected :artifact-mismatch :schema-version-mismatch]
           (mapv #(get-in % [:facts :reason]) diagnostics)))
    (is (= 3 (get-in candidate [:diagnostic-stream :summary-counts :error])))
    (is (= ["S7-LAYOUT" "S7-LAYOUT" "S7-LAYOUT"] (mapv :upstream-diagnostic-id diagnostics)))
    (is (= 3 (count (distinct (map :diagnostic-id diagnostics)))))
    (is (= :passed (:status (c15 'sh15-verify-diagnostic-boundary [input candidate]))))
    (is (= before (pr-str rows)))
    (let [forged (assoc-in candidate [:diagnostic-stream :diagnostics] (vec (reverse diagnostics)))
          result (c15 'sh15-verify-diagnostic-boundary [input forged])]
      (is (= :rejected (:status result)))
      (is (= :candidate-substitution (reason result))))
    (let [duplicate (assoc input :upstream-diagnostics [(first rows) (first rows)])
          result (build duplicate)]
      (is (= :rejected (:status result)))
      (is (= :duplicate-upstream-diagnostic-id (reason result))))))
