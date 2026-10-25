(ns gravity.self-hosting.sh15-published-diagnostic-identity-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh15-absent-layout-location-test]))

;; Existing direct seed loader and unverified-reference request builder only.
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
       "/coordinator-published-diagnostic-identity." extension))
(def ^:private accepted-plan (delay (compile-plan (fixture-path "accepted" "gravity"))))
(def ^:private rejected-plan (delay (compile-plan (fixture-path "rejected" "gravity"))))
(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh15-published-identity-development :compiler-artifact-plan? true}
   plan function arguments))
(defn- c15 [function arguments] (invoke @c15-plan function arguments))
(defn- source-request [anchor path]
  (invoke @accepted-plan 'coordinator-published-diagnostic-identity-request [anchor path]))
(defn- original [input]
  (invoke @c12-plan 'sh14-build-authenticated-layout [input]))
(defn- row [anchor path]
  (first (:diagnostics (original (source-request anchor path)))))
(defn- request [rows]
  (assoc (#'gravity.self-hosting.sh15-absent-layout-location-test/boundary-request
          (first rows)) :upstream-diagnostics rows))
(defn- build [input] (c15 'sh15-build-diagnostic-boundary [input]))
(defn- published [candidate] (get-in candidate [:diagnostic-stream :diagnostics 0]))
(defn- verify [input candidate] (c15 'sh15-verify-diagnostic-boundary [input candidate]))
(defn- reason [result] (get-in result [:diagnostics 0 :facts :reason]))

(deftest sh15-published-id-is-invariant-under-physical-relocation
  (let [a (row "layout-a" "/checkout-a/source.gravity")
        b (row "layout-a" "/checkout-b/source.gravity")
        ca (build (request [a])) cb (build (request [b]))
        da (published ca) db (published cb)]
    (is (= :rejected (:status (original (source-request "layout-a" "/checkout-a/source.gravity")))))
    (is (= :request-schema-mismatch (:reason a) (:reason b)))
    (is (= :accepted (:status ca) (:status cb)))
    (is (= (:diagnostic-id da) (:diagnostic-id db))
        "physical relocation must not change the published semantic ID")
    (is (= (:diagnostic-id da)
           (get-in ca [:identity-input :diagnostics 0 :diagnostic-id])))
    (is (= (invoke @accepted-plan 'coordinator-published-diagnostic-identity-primary [])
           (get (:diagnostic-id da) 2)))
    (is (= (:source-span a) (get-in da [:primary :span])))
    (is (= (:source-span b) (get-in db [:primary :span])))
    (is (not= (:primary da) (:primary db)))
    (is (= (:generated-origin-chain a) (:origin-chain da)))
    (is (= (:generated-origin-chain b) (:origin-chain db)))
    (is (= (:identity-input ca) (:identity-input cb)))
    (is (= "S7-LAYOUT" (:upstream-diagnostic-id da) (:upstream-diagnostic-id db)))))

(deftest sh15-published-id-retains-existing-semantic-distinctions
  (let [input (source-request "layout-a" "/source.gravity")
        requests [input
                  (source-request "layout-b" "/source.gravity")
                  (assoc-in input [:layout-request :source-span :source-id] :other-source)
                  (-> input
                      (assoc-in [:layout-request :source-span :start :byte] 1)
                      (assoc-in [:layout-request :source-span :end :byte] 2))]
        candidates (mapv #(build (request [(first (:diagnostics (original %)))])) requests)
        ids (mapv #(get-in % [:diagnostic-stream :diagnostics 0 :diagnostic-id]) candidates)]
    (is (every? #(= :accepted (:status %)) candidates))
    (is (= 4 (count (distinct ids))))
    (doseq [candidate candidates]
      (is (= (get-in candidate [:identity-input :diagnostics 0 :diagnostic-id])
             (:diagnostic-id (published candidate)))))))

(deftest sh15-published-id-preserves-data-wording-views-and-fresh-verification
  (let [rows [(row "layout-a" "/source.gravity")]
        before (pr-str rows) input (request rows) candidate (build input)
        diagnostic (published candidate) id (:diagnostic-id diagnostic)
        changed-wording (assoc diagnostic :message-key :different-wording
                               :message "different display wording")
        view (c15 'sh15-render-bounded-diagnostic-text [diagnostic])
        fresh (compile-plan c15-path)]
    (is (= before (pr-str rows)))
    (is (= (:remediation (first rows)) (first (:remediation diagnostic))))
    (is (= (:semantic-anchor (first rows)) (get-in diagnostic [:facts :semantic-anchor])))
    (is (= :accepted (:status view)))
    (is (= id (:diagnostic-id diagnostic) (:diagnostic-id changed-wording)))
    (is (= view (c15 'sh15-render-bounded-diagnostic-text [changed-wording])))
    (is (= candidate (invoke fresh 'sh15-build-diagnostic-boundary [input])))
    (is (= :passed (:status (invoke fresh 'sh15-verify-diagnostic-boundary [input candidate]))))
    (is (= :none (:semantic-authority candidate)))
    (is (= :external-reference-shape-and-equality-only (:verification-status candidate)))
    (is (false? (:executable? candidate)))
    (is (false? (:self-hosted? candidate)))
    (doseq [function '[coordinator-published-diagnostic-identity-forged-id
                       coordinator-published-diagnostic-identity-path-id]]
      (let [forged (invoke @rejected-plan function [diagnostic])
            result (invoke fresh 'sh15-verify-diagnostic-boundary
                           [input (assoc-in candidate [:diagnostic-stream :diagnostics 0] forged)])]
        (is (not= id (:diagnostic-id forged)))
        (is (= :rejected (:status result)))
        (is (= :candidate-substitution (reason result)))))))

(deftest sh15-published-id-paired-source-fixtures
  (let [upstream (row "layout-a" "/source.gravity")
        diagnostic (published (build (request [upstream])))]
    (doseq [family ["accepted" "rejected"]]
      (let [g (fixture-path family "gravity") q (fixture-path family "qst")
            gp (if (= "accepted" family) @accepted-plan @rejected-plan)
            qp (compile-plan q)
            calls (if (= "accepted" family)
                    [['coordinator-published-diagnostic-identity-request ["layout-a" "/source.gravity"]]
                     ['coordinator-published-diagnostic-identity-primary []]]
                    [['coordinator-published-diagnostic-identity-forged-id [diagnostic]]
                     ['coordinator-published-diagnostic-identity-path-id [diagnostic]]
                     ['coordinator-published-diagnostic-identity-invalid-row [upstream]]])]
        (is (= (slurp g) (slurp q)))
        (doseq [[function arguments] calls]
          (is (= (invoke gp function arguments) (invoke qp function arguments))))))))

(deftest sh15-published-id-keeps-rejections-and-duplicate-controls
  (let [a (row "layout-a" "/checkout-a/source.gravity")
        relocated (row "layout-a" "/checkout-b/source.gravity")
        other (row "layout-b" "/checkout-a/source.gravity")
        invalid (invoke @rejected-plan 'coordinator-published-diagnostic-identity-invalid-row [a])
        pair (build (request [a other]))]
    (is (= :accepted (:status pair)))
    (is (= 2 (get-in pair [:diagnostic-stream :summary-counts :error])))
    (is (= 2 (count (distinct (map :diagnostic-id
                                  (get-in pair [:diagnostic-stream :diagnostics]))))))
    (doseq [rows [[a a] [a relocated] [a (assoc a :diagnostic-id "OTHER-UPSTREAM-ID")]]]
      (let [result (build (request rows))]
        (is (= :rejected (:status result)))
        (is (= :duplicate-upstream-diagnostic-id (reason result)))))
    (let [result (build (request [invalid]))]
      (is (= :rejected (:status result)))
      (is (= :upstream-diagnostic-schema (reason result))))))
