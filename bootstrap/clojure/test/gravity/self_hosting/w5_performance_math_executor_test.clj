(ns gravity.self-hosting.w5-performance-math-executor-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

; Later focused command (intentionally not run in this static-only change):
; clojure -M:test --namespace gravity.self-hosting.w5-performance-math-executor-test

(defn- repository-root []
  (let [resource
        (io/resource "gravity/self_hosting/w5_performance_math_executor_test.clj")]
    (when-not resource
      (throw (ex-info "W5 performance/math test source is not on classpath"
                      {:id "W5-PM-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "W5-PM-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/w5-performance-math")

(defn- path [relative] (str (.resolve @root relative)))
(defn- fixture-path [family basename extension]
  (str fixture-root "/" family "/" basename extension))

(defn- compile-plan [relative]
  (let [source-path (path relative)
        source-text (slurp source-path)
        emitter (:emitter
                 (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                  source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private engine-plan
  (delay (compile-plan
          "bootstrap/gravity/src/gravity/self_hosting/w5_performance_math_executor.gravity")))
(def ^:private accepted-gravity-plan
  (delay (compile-plan
          (fixture-path "accepted" "performance-math-execution" ".gravity"))))
(def ^:private accepted-qst-plan
  (delay (compile-plan
          (fixture-path "accepted" "performance-math-execution" ".qst"))))
(def ^:private rejected-gravity-plan
  (delay (compile-plan
          (fixture-path "rejected" "invalid-performance-math-execution"
                        ".gravity"))))
(def ^:private rejected-qst-plan
  (delay (compile-plan
          (fixture-path "rejected" "invalid-performance-math-execution"
                        ".qst"))))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-w5-performance-math-executor
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- invoke-engine [function arguments]
  (invoke engine-plan function arguments))

(defn- request [plan source-path extension source-kind]
  (invoke plan 'w5-performance-math-execution-request
          [source-path extension source-kind]))

(defn- request-at [plan source-path extension source-kind]
  (invoke plan 'w5-performance-math-execution-request-at
          [source-path extension source-kind]))

(deftest w5-performance-math-engine-and-fixtures-compile
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (doseq [function
          '[w5-performance-math-policy
            w5-performance-math-diagnostic-catalog
            w5-pm-benchmark-evidence-contract
            w5-pm-target-valid? w5-pm-performance-valid?
            w5-pm-semantic-valid? w5-pm-efir-valid? w5-pm-eml-valid?
            w5-pm-numeric-valid?
            w5-pm-certificate-valid? w5-pm-check-elision-valid?
            w5-pm-path-ends-with?
            w5-pm-request-valid? w5-pm-identity-input w5-pm-provenance
            w5-pm-diagnostic w5-pm-execute w5-pm-run w5-pm-verify
            w5-pm-recompute w5-pm-verify-result]]
    (is (map? (get (:functions @engine-plan) function)) function))
  (is (= (slurp (path (fixture-path
                       "accepted" "performance-math-execution" ".gravity")))
         (slurp (path (fixture-path
                       "accepted" "performance-math-execution" ".qst")))))
  (is (= (slurp (path (fixture-path
                       "rejected" "invalid-performance-math-execution"
                       ".gravity")))
         (slurp (path (fixture-path
                       "rejected" "invalid-performance-math-execution"
                       ".qst"))))))

(deftest w5-performance-math-policy-is-bounded-and-nonauthority
  (let [policy (invoke-engine 'w5-performance-math-policy [])]
    (is (= :meta (:profile policy)))
    (is (= :jvm (:stage2-target policy)))
    (is (= :llvm-x86_64-linux (:candidate-target policy)))
    (is (= {:os :linux :arch :x86_64 :backend :llvm
            :artifact-format :elf :abi :sysv-amd64}
           (:candidate-platform policy)))
    (is (= [:darwin :darwin-arm64 :darwin-x86_64 :windows]
           (:unsupported-targets policy)))
    (doseq [entry (:unsupported-target-policies policy)]
      (is (false? (:invokes-clojure? entry)))
      (is (false? (:links-jvm? entry)))
      (is (false? (:fallback? entry))))
    (is (= :performance-only (:benchmark-evidence-domain policy)))
    (is (= :correctness-only (:semantic-evidence-domain policy)))
    (is (false? (:benchmark-proves-semantics? policy)))
    (is (false? (:benchmark-proves-numeric-correctness? policy)))
    (is (true? (:efir-runtime-semantic-carrier? policy)))
    (is (false? (:eml-tree-identity-implies-equality? policy)))
    (is (= :incomplete (:completion-status policy)))
    (is (= :blocked (:closure-status policy)))
    (is (= :non-authority (:authority policy)))
    (is (true? (:clojure-seed-boundary? policy)))
    (is (false? (:self-hosted? policy)))
    (is (false? (:public-authority? policy)))
    (is (false? (:release? policy)))
    (is (false? (:full-language-credit? policy)))))

(deftest w5-performance-math-accepted-request-remains-incomplete
  (doseq [[plan source-path extension source-kind]
          [[accepted-gravity-plan
            (fixture-path "accepted" "performance-math-execution" ".gravity")
            ".gravity" :gravity]
           [accepted-qst-plan
            (fixture-path "accepted" "performance-math-execution" ".qst")
            ".qst" :qst]]]
    (let [value (request-at plan source-path extension source-kind)
          result (invoke-engine 'w5-pm-verify [value])]
      (is (true? (invoke-engine 'w5-pm-request-valid? [value])))
      (is (= :accepted (:status result)))
      (is (= :incomplete (:execution-status result)))
      (is (= :blocked (:closure-status result)))
      (is (= :non-authority (:authority result)))
      (is (= :performance-only (:benchmark-evidence-domain result)))
      (is (= :correctness-only (:semantic-evidence-domain result)))
      (is (true? (:efir-runtime-semantic-carrier? result)))
      (is (false? (:eml-tree-identity-implies-equality? result)))
      (is (true? (:clojure-seed-boundary? result)))
      (is (false? (:self-hosted? result)))
      (is (false? (:public-authority? result)))
      (is (false? (:release? result)))
      (is (false? (:full-language-credit? result)))
      (is (= :passed
             (:status (invoke-engine 'w5-pm-verify-result [value result])))))))

(deftest w5-performance-math-benchmark-evidence-contract-is-frozen
  (let [contract (invoke-engine 'w5-pm-benchmark-evidence-contract [])
        request (request-at accepted-gravity-plan
                            (fixture-path "accepted"
                                          "performance-math-execution"
                                          ".gravity")
                            ".gravity" :gravity)
        evidence (:benchmark-evidence request)]
    (is (= {:artifact :gravity/benchmark-evidence
            :benchmark-id :pending-vector-sine-benchmark
            :claim-id :vector-sine-throughput
            :harness :perf/vector-sine
            :baseline :c-o3
            :sample-count :pending
            :target-fingerprint-id :pending-target-fingerprint
            :compiler-identity-id :pending-compiler-identity
            :source-identity-id :pending-source-identity
            :optimization-manifest-id :pending-optimization-manifest
            :status :pending}
           contract))
    (is (= contract evidence))
    (is (= (:benchmark-id (:performance-claim request))
           (:benchmark-id evidence)))
    (is (= (:claim-id (:performance-claim request))
           (:claim-id evidence)))))

(deftest w5-performance-math-provenance-identifies-source-kind
  (let [gravity (request-at accepted-gravity-plan
                            (fixture-path "accepted"
                                          "performance-math-execution"
                                          ".gravity")
                            ".gravity" :gravity)
        qst (request-at accepted-qst-plan
                        (fixture-path "accepted"
                                      "performance-math-execution" ".qst")
                        ".qst" :qst)]
    (is (= ".gravity"
           (:actual-source-extension (:provenance gravity))))
    (is (= :gravity (:source-kind (:provenance gravity))))
    (is (= ".qst" (:actual-source-extension (:provenance qst))))
    (is (= :qst (:source-kind (:provenance qst))))
    (is (str/ends-with?
         (:actual-source-path (:provenance gravity)) ".gravity"))
    (is (str/ends-with?
         (:actual-source-path (:provenance qst)) ".qst"))))

(deftest w5-performance-math-path-suffix-helper-is-exact
  (is (true? (invoke-engine 'w5-pm-path-ends-with?
                            ["x.qst" ".qst"])))
  (is (true? (invoke-engine 'w5-pm-path-ends-with?
                            ["x.gravity" ".gravity"])))
  (is (false? (invoke-engine 'w5-pm-path-ends-with?
                             ["x.qst" ".gravity"])))
  (is (false? (invoke-engine 'w5-pm-path-ends-with?
                             ["x.gravity.qst" ".gravity"]))))

(deftest w5-performance-math-identity-is-path-neutral
  (let [left (request accepted-gravity-plan
                      "/checkout-a/bootstrap/performance-math.gravity"
                      ".gravity" :gravity)
        right (invoke accepted-gravity-plan
                      'w5-performance-math-alternate-path-request
                      ["/checkout-b/bootstrap/performance-math.gravity"
                       ".gravity" :gravity])
        left-result (invoke-engine 'w5-pm-verify [left])
        right-result (invoke-engine 'w5-pm-verify [right])]
    (is (= (:identity-input left-result) (:identity-input right-result)))
    (is (not= (:provenance left-result) (:provenance right-result)))
    (is (not (str/includes? (str (:identity-input left-result)) "/checkout")))
    (is (str/includes?
         (:actual-source-path (:provenance left-result)) "/checkout-a/"))
    (is (str/includes?
         (:actual-source-path (:provenance right-result)) "/checkout-b/"))))

(def ^:private rejected-cases
  {'w5-pm-invalid-schema ["W5-PM-SCHEMA" :request]
   'w5-pm-invalid-target ["W5-PM-TARGET" :target-contract]
   'w5-pm-invalid-performance ["W5-PM-PERFORMANCE" :benchmark-evidence]
   'w5-pm-invalid-benchmark-harness ["W5-PM-PERFORMANCE" :benchmark-evidence]
   'w5-pm-invalid-benchmark-baseline ["W5-PM-PERFORMANCE" :benchmark-evidence]
   'w5-pm-invalid-benchmark-sample-count ["W5-PM-PERFORMANCE" :benchmark-evidence]
   'w5-pm-invalid-target-fingerprint ["W5-PM-PERFORMANCE" :benchmark-evidence]
   'w5-pm-invalid-compiler-identity ["W5-PM-PERFORMANCE" :benchmark-evidence]
   'w5-pm-invalid-source-identity ["W5-PM-PERFORMANCE" :benchmark-evidence]
   'w5-pm-invalid-optimization-manifest ["W5-PM-PERFORMANCE" :benchmark-evidence]
   'w5-pm-invalid-safety-mode ["W5-PM-PERFORMANCE" :benchmark-evidence]
   'w5-pm-invalid-domain ["W5-PM-PERFORMANCE" :benchmark-evidence]
   'w5-pm-invalid-layout ["W5-PM-PERFORMANCE" :benchmark-evidence]
   'w5-pm-invalid-effects ["W5-PM-PERFORMANCE" :benchmark-evidence]
   'w5-pm-invalid-capabilities ["W5-PM-PERFORMANCE" :benchmark-evidence]
   'w5-pm-invalid-erased-checks ["W5-PM-PERFORMANCE" :benchmark-evidence]
   'w5-pm-invalid-semantic ["W5-PM-SEMANTIC" :semantic-evidence]
   'w5-pm-invalid-lineage ["W5-PM-LINEAGE" :evidence-domains]
   'w5-pm-invalid-efir ["W5-PM-EFIR" :efir]
   'w5-pm-invalid-eml ["W5-PM-EML" :eml]
   'w5-pm-invalid-numeric ["W5-PM-NUMERIC" :numeric-contract]
   'w5-pm-invalid-certificate ["W5-PM-CERTIFICATE" :certificate]
   'w5-pm-invalid-proof ["W5-PM-PROOF" :check-elision]
   'w5-pm-invalid-proof-duplicate ["W5-PM-PERFORMANCE" :benchmark-evidence]
   'w5-pm-invalid-proof-reordered ["W5-PM-PERFORMANCE" :benchmark-evidence]
   'w5-pm-invalid-check-elision ["W5-PM-PROOF" :check-elision]
   'w5-pm-invalid-provenance ["W5-PM-PROVENANCE" :provenance]
   'w5-pm-invalid-evidence ["W5-PM-EVIDENCE" :evidence]
   'w5-pm-invalid-authority ["W5-PM-AUTHORITY" :authority]})

(deftest w5-performance-math-rejects-contract-substitution
  (doseq [[function expected] rejected-cases]
    (testing (str function)
      (doseq [[accepted-plan rejected-plan source-path extension source-kind]
              [[accepted-gravity-plan rejected-gravity-plan
                (fixture-path "accepted" "performance-math-execution"
                              ".gravity") ".gravity" :gravity]
               [accepted-qst-plan rejected-qst-plan
                (fixture-path "accepted" "performance-math-execution" ".qst")
                ".qst" :qst]]]
        (let [base (request-at accepted-plan source-path extension source-kind)
              invalid (invoke rejected-plan function [base])
              result (invoke-engine 'w5-pm-verify [invalid])
              diagnostic (first (:diagnostics result))]
          (is (= :rejected (:status result)))
          (is (= (get expected 0) (:rule diagnostic)))
          (is (= (get expected 0) (:diagnostic-id diagnostic)))
          (is (= (get expected 1) (:field (:facts diagnostic))))
          (is (= :error (:severity diagnostic)))
          (is (= :non-authority (:authority result)))
          (is (false? (:public-authority? result))))))))

(deftest w5-performance-math-rejects-path-suffix-substitution
  (doseq [[accepted-plan rejected-plan source-path extension source-kind
           mutator wrong-suffix]
          [[accepted-gravity-plan rejected-gravity-plan
            (fixture-path "accepted" "performance-math-execution"
                          ".gravity") ".gravity" :gravity
            'w5-pm-invalid-gravity-path-suffix #"\.qst$"]
           [accepted-qst-plan rejected-qst-plan
            (fixture-path "accepted" "performance-math-execution" ".qst")
            ".qst" :qst
            'w5-pm-invalid-qst-path-suffix #"\.gravity$"]]]
    (let [base (request-at accepted-plan source-path extension source-kind)
          invalid (invoke rejected-plan mutator [base])
          result (invoke-engine 'w5-pm-verify [invalid])
          diagnostic (first (:diagnostics result))
          provenance (:provenance invalid)]
      (is (re-find wrong-suffix
                   (:actual-source-path provenance)))
      (is (= extension (:actual-source-extension provenance)))
      (is (= source-kind (:source-kind provenance)))
      (is (= :rejected (:status result)))
      (is (= "W5-PM-PROVENANCE" (:rule diagnostic)))
      (is (= "W5-PM-PROVENANCE" (:diagnostic-id diagnostic)))
      (is (= :provenance (:field (:facts diagnostic))))
      (is (= :error (:severity diagnostic)))
      (is (= :non-authority (:authority result))))))

(deftest w5-performance-math-rejects-result-substitution
  (let [value (request-at accepted-gravity-plan
                          (fixture-path "accepted"
                                        "performance-math-execution"
                                        ".gravity")
                          ".gravity" :gravity)
        result (invoke-engine 'w5-pm-verify [value])
        substituted (invoke rejected-gravity-plan 'w5-pm-invalid-result [result])
        verification (invoke-engine 'w5-pm-verify-result [value substituted])]
    (is (= :rejected (:status verification)))
    (is (= "W5-PM-SUBSTITUTION"
           (:rule (first (:diagnostics verification)))))
    (is (= :non-authority (:authority verification)))))
