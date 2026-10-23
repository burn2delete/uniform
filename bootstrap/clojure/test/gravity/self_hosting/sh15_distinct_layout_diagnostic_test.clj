(ns gravity.self-hosting.sh15-distinct-layout-diagnostic-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh15-absent-layout-location-test]))

;; Reuse the existing direct seed-plan loader and explicitly unverified
;; reference-shape request builder; do not construct authenticated upstreams.
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
       "/coordinator-distinct-layout-diagnostic." extension))
(def ^:private accepted-plan (delay (compile-plan (fixture-path "accepted" "gravity"))))
(def ^:private rejected-plan (delay (compile-plan (fixture-path "rejected" "gravity"))))
(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh15-distinct-diagnostics-development :compiler-artifact-plan? true}
   plan function arguments))
(defn- c15 [function arguments] (invoke @c15-plan function arguments))
(defn- source-request [anchor]
  (invoke @accepted-plan 'coordinator-distinct-layout-diagnostic-request [anchor]))
(defn- original-result [request]
  (invoke @c12-plan 'sh14-build-authenticated-layout [request]))
(defn- original-row [anchor] (first (:diagnostics (original-result (source-request anchor)))))
(defn- located-row [anchor path]
  (first (:diagnostics
          (original-result
           (invoke @accepted-plan 'coordinator-distinct-layout-diagnostic-located-request
                   [anchor path])))))
(defn- request [rows]
  (assoc (#'gravity.self-hosting.sh15-absent-layout-location-test/boundary-request
          (first rows))
         :upstream-diagnostics rows))
(defn- build [request] (c15 'sh15-build-diagnostic-boundary [request]))
(defn- verify [request candidate]
  (c15 'sh15-verify-diagnostic-boundary [request candidate]))
(defn- reason [result] (get-in result [:diagnostics 0 :facts :reason]))

(deftest sh15-distinct-layout-paired-fixtures-and-real-original-rejections
  (let [a (original-row "layout-a") b (original-row "layout-b")
        located (located-row "layout-a" "/checkout-a/source.gravity")]
    (doseq [anchor ["layout-a" "layout-b"]]
      (is (= :rejected (:status (original-result (source-request anchor))))))
    (is (= ["S7-LAYOUT" "S7-LAYOUT"] (mapv :diagnostic-id [a b])))
    (is (= [:request-schema-mismatch :request-schema-mismatch] (mapv :reason [a b])))
    (is (= ["layout-a" "layout-b"] (mapv :semantic-anchor [a b])))
    (is (= [nil nil] (mapv :source-span [a b])))
    (is (map? (:source-span located)))
    (is (= :request-schema-mismatch (:reason located)))
    (doseq [family ["accepted" "rejected"]]
      (let [g (fixture-path family "gravity") q (fixture-path family "qst")
            g-plan (if (= "accepted" family) @accepted-plan @rejected-plan)
            q-plan (compile-plan q)
            calls (if (= "accepted" family)
                    [['coordinator-distinct-layout-diagnostic-request ["layout-a"]]
                     ['coordinator-distinct-layout-diagnostic-located-request
                      ["layout-a" "/checkout-a/source.gravity"]]]
                    [['coordinator-distinct-layout-diagnostic-identical [a]]
                     ['coordinator-distinct-layout-diagnostic-renamed-id [located]]
                     ['coordinator-distinct-layout-diagnostic-malformed [a]]])]
        (is (= (slurp g) (slurp q)))
        (doseq [[function arguments] calls]
          (is (= (invoke g-plan function arguments)
                 (invoke q-plan function arguments))))))))

(deftest sh15-distinct-original-layout-diagnostics-preserve-data-order-and-views
  (let [rows [(original-row "layout-a") (original-row "layout-b")]
        before (pr-str rows) input (request rows) candidate (build input)
        diagnostics (get-in candidate [:diagnostic-stream :diagnostics])
        identities (mapv :diagnostic-id (get-in candidate [:identity-input :diagnostics]))]
    (is (true? (c15 'sh15-diagnostic-input-valid? [input])))
    (is (= :accepted (:status candidate)))
    (is (= ["layout-a" "layout-b"] (mapv #(get-in % [:facts :semantic-anchor]) diagnostics)))
    (is (= ["S7-LAYOUT" "S7-LAYOUT"] (mapv :upstream-diagnostic-id diagnostics)))
    (is (= 2 (count identities) (count (distinct identities))))
    (is (= [nil nil] (mapv #(get-in % [:primary :span]) diagnostics)))
    (is (= before (pr-str rows)))
    (is (= rows [(original-row "layout-a") (original-row "layout-b")]))
    (is (= {:error 2 :warning 0 :info 0 :hint 0 :internal-error 0}
           (get-in candidate [:diagnostic-stream :summary-counts])))
    (is (= :passed (:status (verify input candidate))))
    (doseq [diagnostic diagnostics]
      (let [view (c15 'sh15-render-bounded-diagnostic-text [diagnostic])]
        (is (= :accepted (:status view)))
        (is (= "code=\"S7-LAYOUT\" severity=:error location=<redacted> redactions=:not-evaluated"
               (:text view)))))
    (is (= :none (:semantic-authority candidate)))
    (is (= :external-reference-shape-and-equality-only (:verification-status candidate)))
    (is (= :external-unverified-preservation-input (:diagnostic-input-status candidate)))
    (is (false? (:executable? candidate)))
    (is (false? (:self-hosted? candidate)))
    (is (= :pending (get-in candidate [:diagnostic-stream :rendering-version])))
    (let [fresh (compile-plan c15-path)]
      (is (= candidate (invoke fresh 'sh15-build-diagnostic-boundary [input])))
      (is (= :passed (:status (invoke fresh 'sh15-verify-diagnostic-boundary [input candidate])))))))

(deftest sh15-semantic-duplicates-reject-identical-renamed-and-path-only-rows
  (let [a (original-row "layout-a")
        located (located-row "layout-a" "/checkout-a/source.gravity")
        relocated (located-row "layout-a" "/checkout-b/source.gravity")
        identical (invoke @rejected-plan 'coordinator-distinct-layout-diagnostic-identical [a])
        renamed (invoke @rejected-plan 'coordinator-distinct-layout-diagnostic-renamed-id [located])]
    (is (not= (:diagnostic-id (first renamed)) (:diagnostic-id (second renamed))))
    (is (not= (:source-span located) (:source-span relocated)))
    (is (= :accepted (:status (build (request [located])))))
    (is (= :accepted (:status (build (request [(second renamed)])))))
    (is (= :accepted (:status (build (request [relocated])))))
    (doseq [rows [identical renamed [located relocated]]]
      (let [input (request rows) result (build input)]
        (is (false? (c15 'sh15-diagnostic-input-valid? [input])))
        (is (= :rejected (:status result)))
        (is (= "C15-SCHEMA" (get-in result [:diagnostics 0 :rule])))
        (is (= :duplicate-upstream-diagnostic-id (reason result)))))))

(deftest sh15-distinct-layout-retains-row-carrier-and-count-rejections
  (let [a (original-row "layout-a") b (original-row "layout-b")
        malformed (invoke @rejected-plan 'coordinator-distinct-layout-diagnostic-malformed [b])]
    (doseq [row (concat malformed [(dissoc b :source-span) (assoc b :extra :forbidden)
                                   (assoc b :diagnostic-id (apply str (repeat 129 "X")))
                                   (assoc b :generated-origin-chain (vec (repeat 65 {})))])]
      (let [input (request [a row]) result (build input)]
        (is (false? (c15 'sh15-diagnostic-input-valid? [input])))
        (is (= :rejected (:status result)))
        (is (= :upstream-diagnostic-schema (reason result)))))
    (doseq [[rows expected]
            [[(vec (repeat 257 a)) :diagnostic-count-bound]
             [(list a b) :request-carrier-rejected]]]
      (let [result (build (request rows))]
        (is (= :rejected (:status result)))
        (is (= expected (reason result)))))))

(deftest sh15-distinct-layout-fresh-verification-rejects-tamper-and-reorder
  (let [rows [(original-row "layout-a") (original-row "layout-b")]
        input (request rows) candidate (build input)
        reversed-input (request (vec (reverse rows)))
        reversed-candidate (build reversed-input)]
    (is (= :accepted (:status reversed-candidate)))
    (is (= ["layout-b" "layout-a"]
           (mapv #(get-in % [:facts :semantic-anchor])
                 (get-in reversed-candidate [:diagnostic-stream :diagnostics]))))
    (is (= :passed (:status (verify reversed-input reversed-candidate))))
    (doseq [forged [reversed-candidate
                    (assoc-in candidate [:diagnostic-stream :summary-counts :error] 1)
                    (assoc-in candidate [:diagnostic-stream :diagnostics 0 :upstream-diagnostic-id] "OTHER")
                    (assoc-in candidate [:identity-input :diagnostics 0 :diagnostic-id] :forged)
                    (assoc candidate :semantic-authority :compiler)]]
      (let [result (verify input forged)]
        (is (= :rejected (:status result)))
        (is (= :candidate-substitution (reason result)))))
    (let [empty-input (request []) empty-candidate (build empty-input)]
      (is (= :accepted (:status empty-candidate)))
      (is (= 0 (get-in empty-candidate [:diagnostic-stream :summary-counts :error])))
      (is (= :passed (:status (verify empty-input empty-candidate)))))))
