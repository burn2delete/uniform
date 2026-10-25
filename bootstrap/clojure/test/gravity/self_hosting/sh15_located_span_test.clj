(ns gravity.self-hosting.sh15-located-span-test
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
       "/coordinator-located-span." extension))
(def ^:private accepted-plan (delay (compile-plan (fixture-path "accepted" "gravity"))))
(def ^:private rejected-plan (delay (compile-plan (fixture-path "rejected" "gravity"))))
(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh15-located-span-development :compiler-artifact-plan? true}
   plan function arguments))
(defn- c15 [function arguments] (invoke @c15-plan function arguments))
(defn- spans [] (invoke @accepted-plan 'coordinator-located-spans []))
(defn- original-row [span]
  (let [input (invoke @accepted-plan 'coordinator-located-span-request [span])
        result (invoke @c12-plan 'sh14-build-authenticated-layout [input])]
    (is (= :rejected (:status result)))
    (is (= :request-schema-mismatch (get-in result [:diagnostics 0 :reason])))
    (first (:diagnostics result))))
(defn- request [row]
  (#'gravity.self-hosting.sh15-absent-layout-location-test/boundary-request row))
(defn- reason [result] (get-in result [:diagnostics 0 :facts :reason]))

(deftest sh15-rejects-malformed-located-inputs-and-cleared-location-views
  (let [valid (first (spans)) row (original-row valid)
        candidate (c15 'sh15-build-diagnostic-boundary [(request row)])
        diagnostic (get-in candidate [:diagnostic-stream :diagnostics 0])
        malformed (invoke @rejected-plan 'coordinator-located-span-invalid [valid])]
    (doseq [span malformed]
      (let [forged (assoc row :source-span span) before (pr-str forged)
            input (request forged)
            result (c15 'sh15-build-diagnostic-boundary [input])
            view (c15 'sh15-render-bounded-diagnostic-text
                      [(-> diagnostic (assoc :redactions [])
                           (assoc-in [:primary :span] span))])
            original (original-row span)]
        ;; Original C12 sanitizes these invalid spans; only the supplied
        ;; post-production mutation contains a malformed located span.
        (is (nil? (:source-span original)))
        (is (= :accepted (:status (c15 'sh15-build-diagnostic-boundary
                                      [(request original)]))))
        (is (false? (c15 'sh15-diagnostic-input-valid? [input])))
        (is (= :rejected (:status result)))
        (is (= :upstream-diagnostic-schema (reason result)))
        (is (= :upstream-diagnostic-schema
               (reason (c15 'sh15-verify-diagnostic-boundary [input candidate]))))
        (is (= :rejected (:status view)))
        (is (= :unsupported-primary-location (reason view)))
        (is (not (contains? view :text)))
        (is (= before (pr-str forged)))))
    (is (= valid (:source-span row)))))

(deftest sh15-preserves-real-monotone-multiline-zero-width-and-boundary-spans
  (let [fresh (compile-plan c15-path)]
    (doseq [span (spans)]
      (let [row (original-row span) input (request row)
            candidate (c15 'sh15-build-diagnostic-boundary [input])
            diagnostic (get-in candidate [:diagnostic-stream :diagnostics 0])
            visible (assoc diagnostic :redactions [])
            view (c15 'sh15-render-bounded-diagnostic-text [visible])]
        (is (= span (:source-span row)))
        (is (= :accepted (:status candidate)))
        (is (= span (get-in diagnostic [:primary :span])))
        (is (= (dissoc span :actual-source-path)
               (get-in candidate [:identity-input :diagnostics 0 :primary :span])))
        (is (= (:generated-origin-chain row) (:origin-chain diagnostic)))
        (is (= :passed (:status (invoke fresh 'sh15-verify-diagnostic-boundary
                                        [input candidate]))))
        (is (= candidate (invoke fresh 'sh15-build-diagnostic-boundary [input])))
        (is (= :accepted (:status view)))
        (is (str/includes? (:text view)
                           (str "location=\"/source.gravity\":"
                                (get-in span [:start :line]) ":"
                                (get-in span [:start :column]))))
        (is (= view (invoke fresh 'sh15-render-bounded-diagnostic-text [visible])))
        (is (= :none (:semantic-authority candidate)))
        (is (= :external-unverified-preservation-input
               (:diagnostic-input-status candidate)))))))

(deftest sh15-located-span-paired-source-fixtures
  (doseq [family ["accepted" "rejected"]]
    (let [g (fixture-path family "gravity") q (fixture-path family "qst")
          calls (if (= "accepted" family)
                  [['coordinator-located-spans []]
                   ['coordinator-located-span-request [(first (spans))]]]
                  [['coordinator-located-span-invalid [(first (spans))]]])]
      (is (= (slurp g) (slurp q)))
      (doseq [[function arguments] calls]
        (is (= (invoke (compile-plan g) function arguments)
               (invoke (compile-plan q) function arguments)))))))
