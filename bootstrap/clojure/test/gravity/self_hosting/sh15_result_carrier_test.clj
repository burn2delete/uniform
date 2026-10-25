(ns gravity.self-hosting.sh15-result-carrier-test
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
       "/coordinator-result-carrier." extension))
(def ^:private accepted-plan (delay (compile-plan (fixture-path "accepted" "gravity"))))
(def ^:private rejected-plan (delay (compile-plan (fixture-path "rejected" "gravity"))))
(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh15-result-carrier-development :compiler-artifact-plan? true}
   plan function arguments))
(defn- c15 [function arguments] (invoke @c15-plan function arguments))
(defn- rows [size]
  (mapv (fn [index]
          (let [input (invoke @accepted-plan 'coordinator-result-carrier-request
                              [(str "carrier-" index)])
                rejection (invoke @c12-plan 'sh14-build-authenticated-layout [input])]
            (is (= :rejected (:status rejection)))
            (is (= :request-schema-mismatch
                   (get-in rejection [:diagnostics 0 :reason])))
            (first (:diagnostics rejection))))
        (range size)))
(defn- request [diagnostics]
  (assoc (#'gravity.self-hosting.sh15-absent-layout-location-test/boundary-request
          (first diagnostics)) :upstream-diagnostics diagnostics))
(defn- reason [result] (get-in result [:diagnostics 0 :facts :reason]))

(deftest sh15-rejects-a-request-whose-expanded-result-exceeds-the-carrier
  (let [diagnostics (rows 64) input (request diagnostics)
        before (pr-str input)
        result (c15 'sh15-build-diagnostic-boundary [input])]
    (is (= :accepted (:status (c15 'sh15-carrier-preflight [input]))))
    (is (= 64 (count (distinct (map :semantic-anchor diagnostics)))))
    (is (false? (c15 'sh15-diagnostic-input-valid? [input])))
    (is (= :rejected (:status result)))
    (is (= :result-carrier-rejected (reason result)))
    (is (not (contains? result :diagnostic-stream)))
    (is (= :accepted (:status (c15 'sh15-carrier-preflight [result]))))
    (is (= :result-carrier-rejected
           (reason (c15 'sh15-verify-diagnostic-boundary [input result]))))
    (is (= before (pr-str input)))))

(deftest sh15-small-original-stream-remains-bounded-and-verifiable
  (let [diagnostics (rows 4) input (request diagnostics)
        result (c15 'sh15-build-diagnostic-boundary [input])
        preserved (get-in result [:diagnostic-stream :diagnostics])
        fresh (compile-plan c15-path)]
    (is (true? (c15 'sh15-diagnostic-input-valid? [input])))
    (is (= :accepted (:status result)))
    (is (= :accepted (:status (c15 'sh15-carrier-preflight [result]))))
    (is (= 4 (get-in result [:diagnostic-stream :summary-counts :error])))
    (is (= (mapv :semantic-anchor diagnostics)
           (mapv #(get-in % [:facts :semantic-anchor]) preserved)))
    (is (= (mapv :source-span diagnostics) (mapv #(get-in % [:primary :span]) preserved)))
    (is (= :passed (:status (invoke fresh 'sh15-verify-diagnostic-boundary [input result]))))
    (is (= result (invoke fresh 'sh15-build-diagnostic-boundary [input])))
    (let [forged (invoke @rejected-plan 'coordinator-result-carrier-forged-summary [result])]
      (is (= :candidate-substitution
             (reason (invoke fresh 'sh15-verify-diagnostic-boundary [input forged])))))
    (is (= :none (:semantic-authority result)))
    (is (some #{:complete-c15-conformance} (:nonclaims result)))
    (is (= :external-unverified-preservation-input (:diagnostic-input-status result)))))

(deftest sh15-result-carrier-paired-fixtures
  (doseq [[family function arguments]
          [["accepted" 'coordinator-result-carrier-request ["carrier-0"]]
           ["rejected" 'coordinator-result-carrier-forged-summary
            [{:diagnostic-stream {:summary-counts {:error 4}}}]]]]
    (let [g (fixture-path family "gravity") q (fixture-path family "qst")]
      (is (= (slurp g) (slurp q)))
      (is (= (invoke (compile-plan g) function arguments)
             (invoke (compile-plan q) function arguments))))))

(deftest sh15-owned-rejection-does-not-copy-oversized-profile-or-target
  (let [oversized (keyword (apply str (repeat 512 "X")))
        bounded (keyword (apply str (repeat 511 "X")))]
    (doseq [field [:profile :target]]
      (let [input {field oversized}
            result (c15 'sh15-build-diagnostic-boundary [input])
            verification (c15 'sh15-verify-diagnostic-boundary [input result])]
        (is (= :rejected (:status result)))
        (is (= :request-schema-mismatch (reason result)))
        (is (nil? (get-in result [:diagnostics 0 field])))
        (is (= :accepted (:status (c15 'sh15-carrier-preflight [result]))))
        (is (= result (assoc verification :artifact :gravity/sh15-diagnostic-boundary))))
      (is (= bounded
             (get-in (c15 'sh15-build-diagnostic-boundary [{field bounded}])
                     [:diagnostics 0 field]))))))
