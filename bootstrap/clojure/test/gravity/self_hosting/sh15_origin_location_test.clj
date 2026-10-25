(ns gravity.self-hosting.sh15-origin-location-test
  (:require [clojure.test :refer [deftest is]]
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
       "/coordinator-origin-location." extension))
(def ^:private accepted-plan (delay (compile-plan (fixture-path "accepted" "gravity"))))
(def ^:private rejected-plan (delay (compile-plan (fixture-path "rejected" "gravity"))))
(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh15-origin-location-development :compiler-artifact-plan? true}
   plan function arguments))
(defn- c15 [function arguments] (invoke @c15-plan function arguments))
(defn- original-row [path]
  (let [input (invoke @accepted-plan 'coordinator-origin-location-request [path])
        result (invoke @c12-plan 'sh14-build-authenticated-layout [input])]
    (is (= :rejected (:status result)))
    (is (= :request-schema-mismatch (get-in result [:diagnostics 0 :reason])))
    (first (:diagnostics result))))
(defn- request [row]
  (#'gravity.self-hosting.sh15-absent-layout-location-test/boundary-request row))
(defn- reason [result] (get-in result [:diagnostics 0 :facts :reason]))

(deftest sh15-rejects-a-forged-leaf-origin-without-a-physical-path
  (let [row (original-row "/producer.gravity")
        forged (invoke @rejected-plan 'coordinator-origin-location-forged [row])
        before (pr-str forged) input (request forged)
        candidate (c15 'sh15-build-diagnostic-boundary [input])
        genuine (original-row "")
        sanitized (c15 'sh15-build-diagnostic-boundary [(request genuine)])]
    (is (= [] (:generated-origin-chain genuine)))
    (is (= :accepted (:status sanitized)))
    (is (= [] (get-in sanitized [:diagnostic-stream :diagnostics 0 :origin-chain])))
    (is (= "" (get-in forged [:generated-origin-chain 0 :actual-source-path])))
    (is (false? (c15 'sh15-diagnostic-input-valid? [input])))
    (is (= :rejected (:status candidate)))
    (is (= :upstream-diagnostic-schema (reason candidate)))
    (is (= :upstream-diagnostic-schema
           (reason (c15 'sh15-verify-diagnostic-boundary [input candidate]))))
    (is (= before (pr-str forged)))))

(deftest sh15-real-leaf-paths-preserve-provenance-and-path-neutral-identity
  (let [fresh (compile-plan c15-path)
        candidates
        (mapv (fn [path]
                (let [row (original-row path) input (request row)
                      candidate (c15 'sh15-build-diagnostic-boundary [input])
                      diagnostic (get-in candidate [:diagnostic-stream :diagnostics 0])]
                  (is (= path (get-in row [:generated-origin-chain 0 :actual-source-path])))
                  (is (= :accepted (:status candidate)))
                  (is (= (:generated-origin-chain row) (:origin-chain diagnostic)))
                  (is (= [{:kind :source :source-id :fixture}]
                         (get-in candidate [:identity-input :diagnostics 0 :origin-chain])))
                  (is (= :passed (:status (invoke fresh 'sh15-verify-diagnostic-boundary
                                                  [input candidate]))))
                  (is (= candidate (invoke fresh 'sh15-build-diagnostic-boundary [input])))
                  (is (= :not-evaluated (:redactions diagnostic)))
                  (is (= :none (:semantic-authority candidate)))
                  candidate))
              ["x" "/producer.gravity" (apply str (repeat 512 "x"))])]
    (is (apply = (map :identity-input candidates)))
    (is (apply = (map #(get-in % [:diagnostic-stream :diagnostics 0 :diagnostic-id])
                     candidates)))))

(deftest sh15-origin-location-keeps-upper-bound-and-exact-leaf-schema
  (let [row (original-row "/producer.gravity")
        over-bound (original-row (apply str (repeat 513 "x")))
        origin (first (:generated-origin-chain row))]
    (is (= 513 (count (get-in over-bound [:generated-origin-chain 0 :actual-source-path]))))
    (doseq [invalid [over-bound
                     (assoc row :generated-origin-chain [(assoc origin :actual-source-path nil)])
                     (assoc row :generated-origin-chain
                            [(assoc origin :generated-origin {:generator-id :unallocated})])]]
      (is (= :upstream-diagnostic-schema
             (reason (c15 'sh15-build-diagnostic-boundary [(request invalid)])))))))

(deftest sh15-origin-location-paired-source-fixtures
  (doseq [[family function arguments]
          [["accepted" 'coordinator-origin-location-request ["/producer.gravity"]]
           ["rejected" 'coordinator-origin-location-forged [(original-row "/producer.gravity")]]]]
    (let [g (fixture-path family "gravity") q (fixture-path family "qst")]
      (is (= (slurp g) (slurp q)))
      (is (= (invoke (compile-plan g) function arguments)
             (invoke (compile-plan q) function arguments))))))
