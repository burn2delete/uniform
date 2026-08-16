(ns gravity.self-hosting.w5-typed-effect-safety-executor-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

; Later focused command (intentionally not run in this static-only change):
; clojure -M:test --namespace gravity.self-hosting.w5-typed-effect-safety-executor-test

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/w5_typed_effect_safety_executor_test.clj")]
    (when-not resource
      (throw
       (ex-info "W5 typed/effect/safety test source is not on the classpath"
                {:id "W5-TES-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "W5-TES-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/w5-typed-effect-safety")
(def ^:private engine-source
  "bootstrap/gravity/src/gravity/self_hosting/w5_typed_effect_safety_executor.gravity")

(defn- path [relative]
  (str (.resolve @root relative)))

(defn- fixture-path [family basename extension]
  (str fixture-root "/" family "/" basename extension))

(defn- compile-plan [relative]
  (let [source-path (path relative)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private engine-plan (delay (compile-plan engine-source)))
(def ^:private accepted-gravity-plan
  (delay
   (compile-plan
    (fixture-path "accepted" "typed-effect-safety-execution" ".gravity"))))
(def ^:private accepted-qst-plan
  (delay
   (compile-plan
    (fixture-path "accepted" "typed-effect-safety-execution" ".qst"))))
(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-path "rejected" "invalid-typed-effect-safety-execution"
                  ".gravity"))))
(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (fixture-path "rejected" "invalid-typed-effect-safety-execution"
                  ".qst"))))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-w5-typed-effect-safety-executor
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- invoke-engine [function arguments]
  (invoke engine-plan function arguments))

(defn- request [plan source-path extension source-kind]
  (invoke plan 'w5-typed-effect-safety-execution-request
          [source-path extension source-kind]))

(defn- request-at [plan source-path extension source-kind]
  (invoke plan 'w5-typed-effect-safety-execution-request-at
          [source-path extension source-kind]))

(deftest w5-typed-effect-safety-engine-and-fixtures-compile
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (doseq [function
          '[w5-typed-effect-safety-policy
            w5-typed-effect-safety-diagnostic-catalog
            w5-tes-string-suffix?
            w5-tes-extension-path-valid?
            w5-tes-target-valid?
            w5-tes-validator-receipt-provenance-ids
            w5-tes-validator-results-valid?
            w5-tes-safe1-outcomes-valid?
            w5-tes-request-valid?
            w5-tes-identity-input
            w5-tes-provenance
            w5-tes-diagnostic
            w5-tes-execute
            w5-tes-run
            w5-tes-verify
            w5-tes-recompute
            w5-tes-verify-result]]
    (is (map? (get (:functions @engine-plan) function)) function))
  (is (= (slurp (path (fixture-path
                       "accepted" "typed-effect-safety-execution" ".gravity")))
         (slurp (path (fixture-path
                       "accepted" "typed-effect-safety-execution" ".qst")))))
  (is (= (slurp (path (fixture-path
                       "rejected" "invalid-typed-effect-safety-execution"
                       ".gravity")))
         (slurp (path (fixture-path
                       "rejected" "invalid-typed-effect-safety-execution"
                       ".qst"))))))

(deftest w5-typed-effect-safety-suffix-helpers-guard-short-paths
  (doseq [[value suffix expected]
          [[".qst" ".qst" true]
           ["x" ".qst" false]
           [".gravity" ".qst" false]]]
    (is (= expected
           (invoke-engine 'w5-tes-string-suffix? [value suffix]))))
  (doseq [[extension path-value expected]
          [[".qst" "/checkout-a/file.qst" true]
           [".qst" "x" false]
           [".gravity" "/checkout-a/file.qst" false]]]
    (is (= expected
           (invoke-engine 'w5-tes-extension-path-valid?
                          [extension path-value])))))

(deftest w5-typed-effect-safety-policy-freezes-authority-and-target
  (let [policy (invoke-engine 'w5-typed-effect-safety-policy [])]
    (is (= :meta (:profile policy)))
    (is (= :jvm (:stage2-target policy)))
    (is (= :llvm-x86_64-linux (:candidate-target policy)))
    (is (= {:os :linux :arch :x86_64 :backend :llvm
            :artifact-format :elf :abi :sysv-amd64}
           (:candidate-platform policy)))
    (is (= [:darwin :darwin-arm64 :darwin-x86_64 :windows]
           (:unsupported-targets policy)))
    (is (= [:prov-c7 :prov-c8 :prov-profile :prov-capability
            :prov-c9 :prov-c10]
           (:validator-provenance-ids policy)))
    (doseq [entry (:unsupported-target-policies policy)]
      (is (false? (:invokes-clojure? entry)))
      (is (false? (:links-jvm? entry)))
      (is (false? (:fallback? entry))))
    (is (true? (:effects-capabilities-separate? policy)))
    (is (false? (:redefines-validator-semantics? policy)))
    (is (= [:proven-safe :runtime-checked :rejected :unsafe-island]
           (:safe1-outcomes policy)))
    (is (= :incomplete (:completion-status policy)))
    (is (= :non-authority (:authority policy)))
    (is (true? (:clojure-seed-boundary? policy)))
    (is (false? (:self-hosted? policy)))
    (is (false? (:public-authority? policy)))
    (is (false? (:release? policy)))
    (is (false? (:fl-240-credit? policy)))))

(deftest w5-typed-effect-safety-accepted-request-is-incomplete
  (doseq [[plan source-path extension source-kind]
          [[accepted-gravity-plan
            "/checkout-a/bootstrap/typed-effect-safety.gravity"
            ".gravity" :gravity]
           [accepted-qst-plan
            "/checkout-a/bootstrap/typed-effect-safety.qst"
            ".qst" :qst]]]
    (let [value (request-at plan source-path extension source-kind)
          result (invoke-engine 'w5-tes-verify [value])]
      (is (true? (invoke-engine 'w5-tes-request-valid? [value])))
      (is (= :accepted (:status result)))
      (is (= :incomplete (:execution-status result)))
      (is (= :non-authority (:authority result)))
      (is (true? (:effects-capabilities-separate? result)))
      (is (= #{:proven-safe :runtime-checked :rejected :unsafe-island}
             (set (map :outcome (:safe1-outcomes result)))))
      (is (true? (:clojure-seed-boundary? result)))
      (is (false? (:self-hosted? result)))
      (is (false? (:public-authority? result)))
      (is (false? (:release? result)))
      (is (false? (:full-language-credit? result)))
      (is (false? (:fl-240-credit? result)))
      (is (= :passed
             (:status (invoke-engine 'w5-tes-verify-result
                                     [value result])))))))

(deftest w5-typed-effect-safety-provenance-identifies-source-kind
  (let [gravity (request-at accepted-gravity-plan
                            "/checkout-a/bootstrap/typed-effect-safety.gravity"
                            ".gravity" :gravity)
        qst (request-at accepted-qst-plan
                        "/checkout-a/bootstrap/typed-effect-safety.qst"
                        ".qst" :qst)]
    (is (= ".gravity"
           (get-in gravity [:provenance :actual-source-extension])))
    (is (= :gravity (get-in gravity [:provenance :source-kind])))
    (is (= ".qst"
           (get-in qst [:provenance :actual-source-extension])))
    (is (= :qst (get-in qst [:provenance :source-kind])))
    (is (str/ends-with?
         (get-in gravity [:provenance :actual-source-path]) ".gravity"))
    (is (str/ends-with?
         (get-in qst [:provenance :actual-source-path]) ".qst"))
    (doseq [value [gravity qst]]
      (let [provenance (:provenance value)
            span (:source-span provenance)
            origin (first (:origin-chain provenance))]
        (is (= #{:source-id :start-byte :end-byte :line :column}
               (set (keys span))))
        (is (integer? (:start-byte span)))
        (is (<= 0 (:start-byte span) (:end-byte span)))
        (is (= #{:origin-kind :source-span :producer-id :producer-version
                 :input-ids :reason}
               (set (keys origin))))
        (is (= :source (:origin-kind origin)))
        (is (= 1 (:producer-version origin)))
        (is (= [:w5-typed-effect-safety-request] (:input-ids origin)))))))

(deftest w5-typed-effect-safety-separates-identity-and-provenance
  (let [left (request accepted-gravity-plan
                      "/checkout-a/bootstrap/typed-effect-safety.gravity"
                      ".gravity" :gravity)
        right (invoke accepted-gravity-plan
                      'w5-typed-effect-safety-alternate-path-request
                      ["/checkout-b/bootstrap/typed-effect-safety.gravity"
                       ".gravity" :gravity])
        left-result (invoke-engine 'w5-tes-verify [left])
        right-result (invoke-engine 'w5-tes-verify [right])]
    (is (= (:identity-input left-result) (:identity-input right-result)))
    (is (not= (:provenance left-result) (:provenance right-result)))
    (is (not (str/includes? (str (:identity-input left-result)) "/checkout")))
    (is (str/includes?
         (:actual-source-path (:provenance left-result)) "/checkout-a/"))
    (is (str/includes?
         (:actual-source-path (:provenance right-result)) "/checkout-b/"))))

(def ^:private rejected-cases
  {'w5-tes-invalid-schema ["W5-TES-SCHEMA" :request]
   'w5-tes-invalid-target ["W5-TES-TARGET" :target-contract]
   'w5-tes-invalid-type ["W5-TES-TYPE" :type]
   'w5-tes-invalid-effect ["W5-TES-EFFECT" :effect]
   'w5-tes-invalid-profile ["W5-TES-PROFILE" :profile]
   'w5-tes-invalid-capability ["W5-TES-CAPABILITY" :capability]
   'w5-tes-invalid-ownership ["W5-TES-OWNERSHIP" :ownership]
   'w5-tes-invalid-safety ["W5-TES-SAFETY" :safety]
   'w5-tes-invalid-lineage ["W5-TES-LINEAGE" :validator-results]
   'w5-tes-invalid-safe1 ["W5-TES-SAFE1" :operations]
   'w5-tes-invalid-operation-span-missing ["W5-TES-SAFE1" :operations]
   'w5-tes-invalid-operation-span-extra ["W5-TES-SAFE1" :operations]
   'w5-tes-invalid-operation-span-type ["W5-TES-SAFE1" :operations]
   'w5-tes-invalid-operation-span-negative ["W5-TES-SAFE1" :operations]
   'w5-tes-invalid-operation-span-order ["W5-TES-SAFE1" :operations]
   'w5-tes-invalid-operation-origin-missing ["W5-TES-SAFE1" :operations]
   'w5-tes-invalid-operation-origin-extra ["W5-TES-SAFE1" :operations]
   'w5-tes-invalid-operation-origin-type ["W5-TES-SAFE1" :operations]
   'w5-tes-invalid-operation-origin-negative ["W5-TES-SAFE1" :operations]
   'w5-tes-invalid-operation-origin-order ["W5-TES-SAFE1" :operations]
   'w5-tes-invalid-operation-origin-empty ["W5-TES-SAFE1" :operations]
   'w5-tes-invalid-provenance ["W5-TES-PROVENANCE" :provenance]
   'w5-tes-invalid-provenance-duplicate
   ["W5-TES-PROVENANCE" :provenance]
   'w5-tes-invalid-provenance-reordered
   ["W5-TES-PROVENANCE" :provenance]
   'w5-tes-invalid-provenance-substituted
   ["W5-TES-PROVENANCE" :provenance]
   'w5-tes-invalid-provenance-span-missing
   ["W5-TES-PROVENANCE" :provenance]
   'w5-tes-invalid-provenance-span-extra
   ["W5-TES-PROVENANCE" :provenance]
   'w5-tes-invalid-provenance-span-type
   ["W5-TES-PROVENANCE" :provenance]
   'w5-tes-invalid-provenance-span-negative
   ["W5-TES-PROVENANCE" :provenance]
   'w5-tes-invalid-provenance-span-order
   ["W5-TES-PROVENANCE" :provenance]
   'w5-tes-invalid-provenance-origin-missing
   ["W5-TES-PROVENANCE" :provenance]
   'w5-tes-invalid-provenance-origin-extra
   ["W5-TES-PROVENANCE" :provenance]
   'w5-tes-invalid-provenance-origin-type
   ["W5-TES-PROVENANCE" :provenance]
   'w5-tes-invalid-provenance-origin-negative
   ["W5-TES-PROVENANCE" :provenance]
   'w5-tes-invalid-provenance-origin-order
   ["W5-TES-PROVENANCE" :provenance]
   'w5-tes-invalid-provenance-origin-empty
   ["W5-TES-PROVENANCE" :provenance]
   'w5-tes-invalid-receipt-provenance-substituted
   ["W5-TES-LINEAGE" :validator-results]
   'w5-tes-invalid-receipt-provenance-duplicate
   ["W5-TES-LINEAGE" :validator-results]
   'w5-tes-invalid-receipt-provenance-reordered
   ["W5-TES-LINEAGE" :validator-results]
   'w5-tes-invalid-evidence ["W5-TES-EVIDENCE" :evidence]
   'w5-tes-invalid-authority ["W5-TES-AUTHORITY" :authority]})

(deftest w5-typed-effect-safety-rejects-mutated-contracts
  (doseq [[function expected] rejected-cases]
    (testing (str function)
      (doseq [[accepted-plan rejected-plan source-path extension source-kind]
              [[accepted-gravity-plan rejected-gravity-plan
                "/checkout-a/bootstrap/typed-effect-safety.gravity"
                ".gravity" :gravity]
               [accepted-qst-plan rejected-qst-plan
                "/checkout-a/bootstrap/typed-effect-safety.qst"
                ".qst" :qst]]]
        (let [base (request-at accepted-plan source-path extension source-kind)
              invalid (invoke rejected-plan function [base])
              result (invoke-engine 'w5-tes-verify [invalid])
              diagnostic (first (:diagnostics result))]
          (is (= :rejected (:status result)))
          (is (= (get expected 0) (:rule diagnostic)))
          (is (= (get expected 0) (:diagnostic-id diagnostic)))
          (is (= (get expected 1) (:field (:facts diagnostic))))
          (is (= :error (:severity diagnostic)))
          (is (= :non-authority (:authority result)))
          (is (false? (:public-authority? result)))
          (is (false? (:release? result))))))))

(deftest w5-typed-effect-safety-rejects-path-suffix-substitution
  (doseq [[accepted-plan rejected-plan function source-path extension source-kind]
          [[accepted-gravity-plan rejected-gravity-plan
            'w5-tes-invalid-gravity-path-suffix
            "/checkout-a/bootstrap/typed-effect-safety.gravity"
            ".gravity" :gravity]
           [accepted-qst-plan rejected-qst-plan
            'w5-tes-invalid-qst-path-suffix
            "/checkout-a/bootstrap/typed-effect-safety.qst"
            ".qst" :qst]]]
    (let [base (request-at accepted-plan source-path extension source-kind)
          invalid (invoke rejected-plan function [base])
          result (invoke-engine 'w5-tes-verify [invalid])
          diagnostic (first (:diagnostics result))]
      (is (= extension
             (get-in invalid [:provenance :actual-source-extension])))
      (is (= source-kind (get-in invalid [:provenance :source-kind])))
      (is (str/ends-with?
           (get-in invalid [:provenance :actual-source-path])
           (if (= extension ".gravity") ".qst" ".gravity")))
      (is (= :rejected (:status result)))
      (is (= "W5-TES-PROVENANCE" (:rule diagnostic)))
      (is (= :provenance (:field (:facts diagnostic)))))))

(deftest w5-typed-effect-safety-rejects-operation-local-span-origin-drift
  (doseq [[accepted-plan rejected-plan source-path extension source-kind]
          [[accepted-gravity-plan rejected-gravity-plan
            "/checkout-a/bootstrap/typed-effect-safety.gravity"
            ".gravity" :gravity]
           [accepted-qst-plan rejected-qst-plan
            "/checkout-a/bootstrap/typed-effect-safety.qst"
            ".qst" :qst]]]
    (doseq [function
            '[w5-tes-invalid-operation-span-missing
              w5-tes-invalid-operation-span-extra
              w5-tes-invalid-operation-span-type
              w5-tes-invalid-operation-span-negative
              w5-tes-invalid-operation-span-order
              w5-tes-invalid-operation-origin-missing
              w5-tes-invalid-operation-origin-extra
              w5-tes-invalid-operation-origin-type
              w5-tes-invalid-operation-origin-negative
              w5-tes-invalid-operation-origin-order
              w5-tes-invalid-operation-origin-empty]]
      (let [base (request-at accepted-plan source-path extension source-kind)
            invalid (invoke rejected-plan function [base])
            result (invoke-engine 'w5-tes-verify [invalid])
            diagnostic (first (:diagnostics result))]
        (is (= (:provenance base) (:provenance invalid)))
        (is (= :rejected (:status result)))
        (is (= "W5-TES-SAFE1" (:rule diagnostic)))
        (is (= :operations (:field (:facts diagnostic))))))))

(deftest w5-typed-effect-safety-rejects-receipt-local-provenance-drift
  (doseq [[accepted-plan rejected-plan source-path extension source-kind]
          [[accepted-gravity-plan rejected-gravity-plan
            "/checkout-a/bootstrap/typed-effect-safety.gravity"
            ".gravity" :gravity]
           [accepted-qst-plan rejected-qst-plan
            "/checkout-a/bootstrap/typed-effect-safety.qst"
            ".qst" :qst]]]
    (doseq [function
            '[w5-tes-invalid-receipt-provenance-substituted
              w5-tes-invalid-receipt-provenance-duplicate
              w5-tes-invalid-receipt-provenance-reordered]]
      (let [base (request-at accepted-plan source-path extension source-kind)
            invalid (invoke rejected-plan function [base])
            result (invoke-engine 'w5-tes-verify [invalid])
            diagnostic (first (:diagnostics result))]
        (is (= (get-in base [:provenance :validator-provenance-ids])
               (get-in invalid [:provenance :validator-provenance-ids])))
        (is (not= (get-in base [:validator-results :effect :provenance-id])
                  (get-in invalid [:validator-results :effect :provenance-id])))
        (is (= [:prov-c7 :prov-c8 :prov-profile :prov-capability
                :prov-c9 :prov-c10]
               (get-in invalid [:provenance :validator-provenance-ids])))
        (is (= :rejected (:status result)))
        (is (= "W5-TES-LINEAGE" (:rule diagnostic)))
        (is (= :validator-results (:field (:facts diagnostic))))
        (is (= :error (:severity diagnostic)))
        (is (= :non-authority (:authority result)))))))

(deftest w5-typed-effect-safety-rejects-result-substitution
  (let [value (request accepted-gravity-plan
                       "/checkout-a/bootstrap/typed-effect-safety.gravity"
                       ".gravity" :gravity)
        result (invoke-engine 'w5-tes-verify [value])
        substituted (invoke rejected-gravity-plan 'w5-tes-invalid-result
                            [result])
        verification (invoke-engine 'w5-tes-verify-result
                                    [value substituted])]
    (is (= :rejected (:status verification)))
    (is (= "W5-TES-SUBSTITUTION"
           (:rule (first (:diagnostics verification)))))
    (is (= :non-authority (:authority verification)))))
