(ns gravity.self-hosting.w5-domain-schema-ai-executor-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root []
  (let [resource
        (io/resource "gravity/self_hosting/w5_domain_schema_ai_executor_test.clj")]
    (when-not resource
      (throw (ex-info "W5 domain schema AI test source is not on the classpath"
                      {:id "W5-DSAI-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "W5-DSAI-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/w5-domain-schema-ai")
(defn- path [relative] (str (.resolve @root relative)))
(defn- fixture-path [family basename extension]
  (str fixture-root "/" family "/" basename extension))
(defn- compile-plan [relative-path]
  (let [source-path (path relative-path)
        source-text (slurp source-path)
        emitter (:emitter
                 (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                  source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private engine-plan
  (delay (compile-plan
          "bootstrap/gravity/src/gravity/self_hosting/w5_domain_schema_ai_executor.gravity")))
(def ^:private accepted-gravity-plan
  (delay (compile-plan (fixture-path "accepted" "domain-schema-ai-execution" ".gravity"))))
(def ^:private accepted-qst-plan
  (delay (compile-plan (fixture-path "accepted" "domain-schema-ai-execution" ".qst"))))
(def ^:private rejected-gravity-plan
  (delay (compile-plan (fixture-path "rejected" "invalid-domain-schema-ai-execution" ".gravity"))))
(def ^:private rejected-qst-plan
  (delay (compile-plan (fixture-path "rejected" "invalid-domain-schema-ai-execution" ".qst"))))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-w5-domain-schema-ai-executor
    :compiler-artifact-plan? true}
   @plan function arguments))
(defn- invoke-engine [function arguments] (invoke engine-plan function arguments))
(defn- request [plan extension kind]
  (invoke plan 'w5-domain-schema-ai-request
          [(path (fixture-path "accepted" "domain-schema-ai-execution" extension))
           extension kind]))

(deftest w5-domain-schema-ai-engine-and-fixtures-compile
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (doseq [function
          '[w5-domain-schema-ai-policy
            w5-domain-schema-ai-domain-manifest
            w5-domain-schema-ai-schema-manifest
            w5-domain-schema-ai-diagnostic-catalog
            w5-domain-schema-ai-domain-valid?
            w5-domain-schema-ai-schema-valid?
            w5-domain-schema-ai-serializer-valid?
            w5-domain-schema-ai-migration-valid?
            w5-domain-schema-ai-provider-valid?
            w5-domain-schema-ai-model-valid?
            w5-domain-schema-ai-prompt-valid?
            w5-domain-schema-ai-tool-valid?
            w5-domain-schema-ai-agent-valid?
            w5-domain-schema-ai-workflow-valid?
            w5-domain-schema-ai-memory-valid?
            w5-domain-schema-ai-policy-valid?
            w5-domain-schema-ai-evaluation-valid?
            w5-domain-schema-ai-human-review-valid?
            w5-domain-schema-ai-defense-valid?
            w5-domain-schema-ai-effects-valid?
            w5-domain-schema-ai-diagnostics-valid?
            w5-domain-schema-ai-crosslinks-valid?
            w5-domain-schema-ai-model-budget-valid?
            w5-domain-schema-ai-agent-budget-valid?
            w5-domain-schema-ai-budget-relations-valid?
            w5-domain-schema-ai-request-valid?
            w5-domain-schema-ai-path-ends-with?
            w5-domain-schema-ai-provider-identity-valid?
            w5-domain-schema-ai-identity-input
            w5-domain-schema-ai-provenance
            w5-domain-schema-ai-diagnostic
            w5-domain-schema-ai-execute
            w5-domain-schema-ai-run
            w5-domain-schema-ai-verify
            w5-domain-schema-ai-recompute
            w5-domain-schema-ai-verify-result]]
    (is (map? (get (get @engine-plan :functions) function)) function))
  (is (= (slurp (path (fixture-path "accepted" "domain-schema-ai-execution" ".gravity")))
         (slurp (path (fixture-path "accepted" "domain-schema-ai-execution" ".qst")))))
  (is (= (slurp (path (fixture-path "rejected" "invalid-domain-schema-ai-execution" ".gravity")))
         (slurp (path (fixture-path "rejected" "invalid-domain-schema-ai-execution" ".qst"))))))

(deftest w5-domain-schema-ai-path-suffix-helper-binds-extension
  (is (true? (invoke-engine 'w5-domain-schema-ai-path-ends-with?
                            [".gravity" ".gravity"])))
  (is (true? (invoke-engine 'w5-domain-schema-ai-path-ends-with?
                            ["x.gravity" ".gravity"])))
  (is (false? (invoke-engine 'w5-domain-schema-ai-path-ends-with?
                             ["x.gravity.tmp" ".gravity"])))
  (is (false? (invoke-engine 'w5-domain-schema-ai-path-ends-with?
                             ["x" ".gravity"]))))

(deftest w5-domain-schema-ai-policy-freezes-target-and-denies-authority
  (let [policy (invoke-engine 'w5-domain-schema-ai-policy [])]
    (is (= :meta (:profile policy)))
    (is (= :jvm (:stage2-target policy)))
    (is (= {:backend :llvm :os :linux :arch :x86_64
            :artifact-format :elf :abi :sysv-amd64
            :triple "x86_64-unknown-linux-gnu"}
           (:candidate-target policy)))
    (is (= [:darwin :darwin-arm64 :darwin-x86_64 :windows]
           (:unsupported-targets policy)))
    (doseq [entry (:unsupported-target-policies policy)]
      (is (false? (:invokes-clojure? entry)))
      (is (false? (:links-jvm? entry)))
      (is (false? (:fallback? entry))))
    (is (= :non-authority (:authority policy)))
    (is (true? (:clojure-seed-boundary? policy)))
    (is (false? (:self-hosted? policy)))
    (is (false? (:public-authority? policy)))
    (is (false? (:release? policy)))
    (is (false? (:full-language-credit? policy)))))

(deftest w5-domain-schema-ai-accepted-is-incomplete-and-nonauthority
  (doseq [[plan extension kind]
          [[accepted-gravity-plan ".gravity" :gravity]
           [accepted-qst-plan ".qst" :qst]]]
    (let [value (request plan extension kind)
          result (invoke-engine 'w5-domain-schema-ai-verify [value])]
      (is (true? (invoke-engine 'w5-domain-schema-ai-request-valid? [value])))
      (is (= :accepted (:status result)))
      (is (= :incomplete (:execution-status result)))
      (is (= :blocked (:closure-status result)))
      (is (= :non-authority (:authority result)))
      (is (true? (:clojure-seed-boundary? result)))
      (is (false? (:self-hosted? result)))
      (is (false? (:public? result)))
      (is (false? (:release? result)))
      (is (false? (:full-language-credit? result)))
      (is (false? (:provider-executed? result)))
      (is (false? (:tool-executed? result)))
      (is (false? (:database-written? result)))
      (is (false? (:ambient-authority? result)))
      (is (= extension (:source-extension (:provenance result))))
      (is (= kind (:source-kind (:provenance result))))
      (is (= :passed
             (:status (invoke-engine 'w5-domain-schema-ai-verify-result
                                     [value result])))))))

(deftest w5-domain-schema-ai-budgets-are-complete-and-bounded
  (let [value (request accepted-gravity-plan ".gravity" :gravity)
        model-budget (get (get value :model) :budget)
        agent-budget (get (get value :agent) :budget)]
    (is (= {:max-output-tokens 4096 :max-retries 2
            :max-wall-time-ms 30000 :max-cost-usd 0.25}
           model-budget))
    (is (= {:max-model-calls 3 :max-tool-calls 8 :max-retries 2
            :max-wall-time-ms 120000 :max-output-tokens 12288
            :max-cost-usd 1.0 :max-human-reviews 1}
           agent-budget))
    (is (true? (invoke-engine 'w5-domain-schema-ai-model-budget-valid?
                              [model-budget])))
    (is (true? (invoke-engine 'w5-domain-schema-ai-agent-budget-valid?
                              [agent-budget])))
    (is (true? (invoke-engine 'w5-domain-schema-ai-budget-relations-valid?
                              [value])))))

(deftest w5-domain-schema-ai-identity-is-path-neutral
  (let [left (request accepted-gravity-plan ".gravity" :gravity)
        right (invoke accepted-gravity-plan
                      'w5-domain-schema-ai-alternate-path-request
                      ["/checkout-b/bootstrap/clojure/fixtures/self-hosting/w5-domain-schema-ai/accepted/domain-schema-ai-execution.gravity"
                       ".gravity" :gravity])
        left-result (invoke-engine 'w5-domain-schema-ai-verify [left])
        right-result (invoke-engine 'w5-domain-schema-ai-verify [right])]
    (is (= (:identity-input left-result) (:identity-input right-result)))
    (is (not= (:provenance left-result) (:provenance right-result)))
    (is (not-any? #(re-find #"/checkout|/secret|/different|/opt" (str %))
                  (tree-seq coll? seq (:identity-input left-result))))
    (is (re-find #"/checkout-a/"
                 (:actual-source-path (:provenance left-result))))
    (is (re-find #"/checkout-b/"
                 (:actual-source-path (:provenance right-result))))))

(deftest w5-domain-schema-ai-path-suffix-substitution-is-rejected
  (doseq [[accepted-plan rejected-plan extension kind function expected-suffix]
          [[accepted-gravity-plan rejected-gravity-plan ".gravity" :gravity
            'w5-domain-schema-ai-invalid-gravity-path-suffix ".qst"]
           [accepted-qst-plan rejected-qst-plan ".qst" :qst
            'w5-domain-schema-ai-invalid-qst-path-suffix ".gravity"]]]
    (let [base (request accepted-plan extension kind)
          invalid (invoke rejected-plan function [base])
          result (invoke-engine 'w5-domain-schema-ai-verify [invalid])
          diagnostic (first (:diagnostics result))]
      (is (= :rejected (:status result)))
      (is (= "DOM17-DIAGNOSTIC" (:rule diagnostic)))
      (is (= :provenance (get (:facts diagnostic) :field)))
      (is (= extension (get-in invalid [:provenance :source-extension])))
      (is (= kind (get-in invalid [:provenance :source-kind])))
      (is (str/ends-with? (get-in invalid [:provenance :actual-source-path])
                          expected-suffix)))))

(def ^:private rejected-cases
  {'w5-domain-schema-ai-invalid-model-budget-missing-token ["A2005" :model-budget]
   'w5-domain-schema-ai-invalid-model-budget-missing-wall-time ["A2005" :model-budget]
   'w5-domain-schema-ai-invalid-model-budget-missing-retry ["A2005" :model-budget]
   'w5-domain-schema-ai-invalid-model-budget-missing-cost ["A2005" :model-budget]
   'w5-domain-schema-ai-invalid-model-budget-extra-key ["A2005" :model-budget]
   'w5-domain-schema-ai-invalid-model-budget-wrong-type ["A2005" :model-budget]
   'w5-domain-schema-ai-invalid-model-budget-negative ["A2005" :model-budget]
   'w5-domain-schema-ai-invalid-model-budget-zero ["A2005" :model-budget]
   'w5-domain-schema-ai-invalid-model-budget-out-of-bound ["A2005" :model-budget]
   'w5-domain-schema-ai-invalid-model-budget-substituted ["A2005" :model-budget]
   'w5-domain-schema-ai-invalid-agent-budget-missing-model-calls ["A5006" :agent-budget]
   'w5-domain-schema-ai-invalid-agent-budget-missing-tool-calls ["A5006" :agent-budget]
   'w5-domain-schema-ai-invalid-agent-budget-missing-wall-time ["A5006" :agent-budget]
   'w5-domain-schema-ai-invalid-agent-budget-missing-token ["A5006" :agent-budget]
   'w5-domain-schema-ai-invalid-agent-budget-missing-retry ["A5006" :agent-budget]
   'w5-domain-schema-ai-invalid-agent-budget-missing-cost ["A5006" :agent-budget]
   'w5-domain-schema-ai-invalid-agent-budget-missing-human-reviews ["A5006" :agent-budget]
   'w5-domain-schema-ai-invalid-agent-budget-extra-key ["A5006" :agent-budget]
   'w5-domain-schema-ai-invalid-agent-budget-wrong-type ["A5006" :agent-budget]
   'w5-domain-schema-ai-invalid-agent-budget-negative ["A5006" :agent-budget]
   'w5-domain-schema-ai-invalid-agent-budget-zero ["A5006" :agent-budget]
   'w5-domain-schema-ai-invalid-agent-budget-out-of-bound ["A5006" :agent-budget]
   'w5-domain-schema-ai-invalid-agent-budget-substituted ["A5006" :agent-budget]
   'w5-domain-schema-ai-invalid-agent-max-tool-calls ["A5006" :agent-budget]
   'w5-domain-schema-ai-invalid-agent-max-cost ["A5006" :agent-budget]
   'w5-domain-schema-ai-invalid-artifact ["W5-DSAI-SCHEMA" :request]
   'w5-domain-schema-ai-invalid-request-schema ["W5-DSAI-SCHEMA" :request]
   'w5-domain-schema-ai-invalid-profile ["W5-DSAI-SCHEMA" :request]
   'w5-domain-schema-ai-invalid-model ["DOM18-MODEL" :model]
   'w5-domain-schema-ai-invalid-diagnostics ["W5-DSAI-SCHEMA" :diagnostics]
   'w5-domain-schema-ai-invalid-keyword-model ["A5002" :agent]
   'w5-domain-schema-ai-invalid-missing-link ["W5-DSAI-AUTHORITY" :crosslinks]
   'w5-domain-schema-ai-invalid-mismatched-link ["W5-DSAI-AUTHORITY" :crosslinks]
   'w5-domain-schema-ai-invalid-duplicate-link ["W5-DSAI-AUTHORITY" :crosslinks]
   'w5-domain-schema-ai-invalid-ambient-capability ["W5-DSAI-AUTHORITY" :effects]
   'w5-domain-schema-ai-invalid-extra-capability ["W5-DSAI-AUTHORITY" :crosslinks]
   'w5-domain-schema-ai-invalid-schema ["S1-MANIFEST" :schema-manifest]
   'w5-domain-schema-ai-invalid-serializer ["S2-MANIFEST" :serialization]
   'w5-domain-schema-ai-invalid-migration ["S6-MIGRATION" :migration]
   'w5-domain-schema-ai-invalid-provider ["A2001" :provider]
   'w5-domain-schema-ai-invalid-prompt ["A3003" :prompt]
   'w5-domain-schema-ai-invalid-tool ["A4005" :tool]
   'w5-domain-schema-ai-invalid-agent ["A5002" :agent]
   'w5-domain-schema-ai-invalid-workflow ["A6001" :workflow]
   'w5-domain-schema-ai-invalid-memory ["A7001" :memory]
   'w5-domain-schema-ai-invalid-policy ["A8001" :policy]
   'w5-domain-schema-ai-invalid-evaluation ["A9001" :evaluation]
   'w5-domain-schema-ai-invalid-human-review ["A10001" :human-review]
   'w5-domain-schema-ai-invalid-defense ["A11001" :defense]
   'w5-domain-schema-ai-invalid-effects ["W5-DSAI-AUTHORITY" :effects]
   'w5-domain-schema-ai-invalid-target ["W5-DSAI-TARGET" :target]
   'w5-domain-schema-ai-invalid-authority ["W5-DSAI-AUTHORITY" :authority]})

(def ^:private span-rejected-cases
  {'w5-domain-schema-ai-invalid-source-span-missing
   ["W5-DSAI-SCHEMA" :source-span]
   'w5-domain-schema-ai-invalid-source-span-extra
   ["W5-DSAI-SCHEMA" :source-span]
   'w5-domain-schema-ai-invalid-source-span-wrong-type
   ["W5-DSAI-SCHEMA" :source-span]
   'w5-domain-schema-ai-invalid-source-span-negative
   ["W5-DSAI-SCHEMA" :source-span]
   'w5-domain-schema-ai-invalid-source-span-negative-byte
   ["W5-DSAI-SCHEMA" :source-span]
   'w5-domain-schema-ai-invalid-source-span-negative-column
   ["W5-DSAI-SCHEMA" :source-span]
   'w5-domain-schema-ai-invalid-source-span-order
   ["W5-DSAI-SCHEMA" :source-span]})

(def ^:private nested-authority-rejected-cases
  {'w5-domain-schema-ai-invalid-migration-extra-process
   ["S6-MIGRATION" :migration]
   'w5-domain-schema-ai-invalid-migration-extra-shell
   ["S6-MIGRATION" :migration]
   'w5-domain-schema-ai-invalid-tool-extra-process
   ["A4005" :tool]
   'w5-domain-schema-ai-invalid-tool-extra-shell
   ["A4005" :tool]
   'w5-domain-schema-ai-invalid-agent-extra-process
   ["A5002" :agent]
   'w5-domain-schema-ai-invalid-agent-extra-shell
   ["A5002" :agent]
   'w5-domain-schema-ai-invalid-memory-extra-process
   ["A7001" :memory]
   'w5-domain-schema-ai-invalid-memory-extra-shell
   ["A7001" :memory]
   'w5-domain-schema-ai-invalid-policy-extra-process
   ["A8001" :policy]
   'w5-domain-schema-ai-invalid-policy-extra-shell
   ["A8001" :policy]})

(def ^:private provider-identity-rejected-cases
  {'w5-domain-schema-ai-invalid-provider-identity-provider-id
   "A2007"
   'w5-domain-schema-ai-invalid-provider-identity-model-id
   "A2007"
   'w5-domain-schema-ai-invalid-provider-identity-version
   "A2007"
   'w5-domain-schema-ai-invalid-provider-identity-adapter-version
   "A2007"
   'w5-domain-schema-ai-invalid-provider-identity-schema-mode
   "A2007"})

(deftest w5-domain-schema-ai-nested-authority-sets-are-exact
  (doseq [[function [expected-rule expected-field]]
          nested-authority-rejected-cases]
    (testing (str function)
      (doseq [[accepted-plan rejected-plan extension kind]
              [[accepted-gravity-plan rejected-gravity-plan ".gravity" :gravity]
               [accepted-qst-plan rejected-qst-plan ".qst" :qst]]]
        (let [base (request accepted-plan extension kind)
              invalid (invoke rejected-plan function [base])
              result (invoke-engine 'w5-domain-schema-ai-verify [invalid])
              diagnostic (first (:diagnostics result))]
          (is (= :rejected (:status result)))
          (is (= expected-rule (:rule diagnostic)))
          (is (= expected-rule (:diagnostic-id diagnostic)))
          (is (= expected-field (get (:facts diagnostic) :field)))
          (is (= :error (:severity diagnostic))))))))

(deftest w5-domain-schema-ai-provider-identity-is-bound
  (doseq [[function expected-rule] provider-identity-rejected-cases]
    (testing (str function)
      (doseq [[accepted-plan rejected-plan extension kind]
              [[accepted-gravity-plan rejected-gravity-plan ".gravity" :gravity]
               [accepted-qst-plan rejected-qst-plan ".qst" :qst]]]
        (let [base (request accepted-plan extension kind)
              invalid (invoke rejected-plan function [base])
              result (invoke-engine 'w5-domain-schema-ai-verify [invalid])
              diagnostic (first (:diagnostics result))]
          (is (= :rejected (:status result)))
          (is (= expected-rule (:rule diagnostic)))
          (is (= expected-rule (:diagnostic-id diagnostic)))
          (is (= :identity (get (:facts diagnostic) :field)))
          (is (= extension (get-in invalid [:provenance :source-extension])))
          (is (= kind (get-in invalid [:provenance :source-kind]))))))))

(deftest w5-domain-schema-ai-source-spans-are-exact-and-bounded
  (doseq [[function [expected-rule expected-field]] span-rejected-cases]
    (testing (str function)
      (doseq [[accepted-plan rejected-plan extension kind]
              [[accepted-gravity-plan rejected-gravity-plan ".gravity" :gravity]
               [accepted-qst-plan rejected-qst-plan ".qst" :qst]]]
        (let [base (request accepted-plan extension kind)
              invalid (invoke rejected-plan function [base])
              result (invoke-engine 'w5-domain-schema-ai-verify [invalid])
              diagnostic (first (:diagnostics result))]
          (is (= :rejected (:status result)))
          (is (= expected-rule (:rule diagnostic)))
          (is (= expected-rule (:diagnostic-id diagnostic)))
          (is (= expected-field (get (:facts diagnostic) :field)))
          (is (= :error (:severity diagnostic))))))))

(deftest w5-domain-schema-ai-rejected-mutators-are-specific-and-stable
  (doseq [[function [rule field]] rejected-cases]
    (testing (str function)
        (doseq [[accepted-plan rejected-plan extension kind]
                [[accepted-gravity-plan rejected-gravity-plan ".gravity" :gravity]
                 [accepted-qst-plan rejected-qst-plan ".qst" :qst]]]
        (let [base (request accepted-plan extension kind)
              invalid (invoke rejected-plan function [base])
              result (invoke-engine 'w5-domain-schema-ai-verify [invalid])
              diagnostic (first (:diagnostics result))]
          (is (= :rejected (:status result)))
          (is (= rule (:rule diagnostic)))
          (is (= rule (:diagnostic-id diagnostic)))
          (is (= field (get (:facts diagnostic) :field)))
          (is (= :error (:severity diagnostic)))
          (is (map? (:primary diagnostic)))
          (is (vector? (:related diagnostic)))
          (is (vector? (:origin-chain diagnostic)))
          (is (vector? (:remediation diagnostic)))
          (is (false? (:ambient-authority? result))))))))

(deftest w5-domain-schema-ai-result-substitution-is-rejected
  (let [value (request accepted-gravity-plan ".gravity" :gravity)
        result (invoke-engine 'w5-domain-schema-ai-verify [value])
        substituted (invoke rejected-gravity-plan
                             'w5-domain-schema-ai-invalid-result [result])
        verification (invoke-engine 'w5-domain-schema-ai-verify-result
                                    [value substituted])]
    (is (= :rejected (:status verification)))
    (is (= "A9002" (:rule (first (:diagnostics verification)))))
    (is (= :non-authority (:authority result)))))
