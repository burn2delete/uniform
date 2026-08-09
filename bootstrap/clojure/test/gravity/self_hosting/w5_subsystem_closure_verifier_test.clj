(ns gravity.self-hosting.w5-subsystem-closure-verifier-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

; Later focused command (intentionally not run in this static-only change):
; clojure -M:test --namespace gravity.self-hosting.w5-subsystem-closure-verifier-test

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/w5_subsystem_closure_verifier_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "W5 subsystem-closure verifier test source is not on the classpath"
        {:id "W5-SC-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "W5-SC-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private engine-source
  "bootstrap/gravity/src/gravity/self_hosting/w5_subsystem_closure_verifier.gravity")
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/w5-subsystem-closure")

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
    (fixture-path "accepted" "incomplete-subsystem-closure" ".gravity"))))
(def ^:private accepted-qst-plan
  (delay
   (compile-plan
    (fixture-path "accepted" "incomplete-subsystem-closure" ".qst"))))
(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-path "rejected" "invalid-subsystem-closure" ".gravity"))))
(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (fixture-path "rejected" "invalid-subsystem-closure" ".qst"))))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-w5-subsystem-closure-verifier
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- request [plan function]
  (invoke plan function []))

(defn- request-for-source [plan root-path extension]
  (invoke plan 'w5-subsystem-closure-request-for-source
          [root-path extension]))

(defn- nested-get [value first-key second-key]
  (get (get value first-key) second-key))

(defn- nested-get3 [value first-key second-key third-key]
  (get (get (get value first-key) second-key) third-key))

(defn- plan-function [plan function]
  (nested-get @plan :functions function))

(def ^:private rejected-cases
  {'w5-subsystem-closure-invalid-schema-request
   ["W5-SC-SCHEMA" :request-shape]
   'w5-subsystem-closure-invalid-target-request
   ["W5-SC-TARGET" :target-scope]
   'w5-subsystem-closure-invalid-runtime-request
   ["W5-SC-RUNTIME" :runtime]
   'w5-subsystem-closure-invalid-standard-library-request
   ["W5-SC-STDLIB" :standard-library]
   'w5-subsystem-closure-invalid-package-build-request
   ["W5-SC-PACKAGE" :package-build]
   'w5-subsystem-closure-invalid-linkage-request
   ["W5-SC-LINKAGE" :cross-subsystem-linkage]
   'w5-subsystem-closure-invalid-linkage-verification-request
   ["W5-SC-LINKAGE" :cross-subsystem-linkage]
   'w5-subsystem-closure-invalid-compiler-request
   ["W5-SC-COMPILER" :compiler]
   'w5-subsystem-closure-invalid-transcript-request
   ["W5-SC-TRANSCRIPT" :transcripts]
   'w5-subsystem-closure-invalid-replay-request
   ["W5-SC-REPLAY" :replay]
   'w5-subsystem-closure-invalid-recipe-request
   ["W5-SC-RECIPE" :build-recipes]
   'w5-subsystem-closure-invalid-environment-request
   ["W5-SC-ENVIRONMENT" :environments]
   'w5-subsystem-closure-invalid-lock-request
   ["W5-SC-LOCK" :locks]
   'w5-subsystem-closure-invalid-toolchain-request
   ["W5-SC-TOOLCHAIN" :toolchains]
   'w5-subsystem-closure-invalid-source-status-request
   ["W5-SC-RUNTIME" :runtime]
   'w5-subsystem-closure-invalid-executable-status-request
   ["W5-SC-RUNTIME" :runtime]
   'w5-subsystem-closure-invalid-conformance-request
   ["W5-SC-CONFORMANCE" :runtime]
   'w5-subsystem-closure-invalid-provenance-status-request
   ["W5-SC-RUNTIME" :runtime]
   'w5-subsystem-closure-invalid-missing-status-request
   ["W5-SC-RUNTIME" :runtime]
   'w5-subsystem-closure-invalid-extra-status-request
   ["W5-SC-RUNTIME" :runtime]
   'w5-subsystem-closure-invalid-status-drift-request
   ["W5-SC-CONFORMANCE" :runtime]
   'w5-subsystem-closure-invalid-compiler-status-request
   ["W5-SC-COMPILER" :compiler]
   'w5-subsystem-closure-invalid-linkage-status-request
   ["W5-SC-LINKAGE" :cross-subsystem-linkage]
   'w5-subsystem-closure-invalid-provenance-request
   ["W5-SC-PROVENANCE" :provenance]
   'w5-subsystem-closure-invalid-tcb-request
   ["W5-SC-TCB" :residual-tcb]
   'w5-subsystem-closure-invalid-emulator-authority-request
   ["W5-SC-REPLAY" :replay]
   'w5-subsystem-closure-invalid-emulator-tcb-request
   ["W5-SC-TCB" :residual-tcb]
   'w5-subsystem-closure-invalid-authority-request
   ["W5-SC-AUTHORITY" :authority]
   'w5-subsystem-closure-invalid-upstream-request
   ["W5-SC-UPSTREAM" :upstream-workstreams]
   'w5-subsystem-closure-invalid-source-span-request
   ["W5-SC-SCHEMA" :source-span]
   'w5-subsystem-closure-invalid-replay-transcript-request
   ["W5-SC-REPLAY" :replay]
   'w5-subsystem-closure-invalid-recipe-source-request
   ["W5-SC-RECIPE" :build-recipes]
   'w5-subsystem-closure-invalid-environment-seed-request
   ["W5-SC-ENVIRONMENT" :environments]
   'w5-subsystem-closure-invalid-lock-id-request
   ["W5-SC-LOCK" :locks]
   'w5-subsystem-closure-invalid-toolchain-family-request
   ["W5-SC-TOOLCHAIN" :toolchains]
   'w5-subsystem-closure-invalid-provenance-recipe-request
   ["W5-SC-PROVENANCE" :provenance]
   'w5-subsystem-closure-invalid-source-fixture-suffix-request
   ["W5-SC-PROVENANCE" :source-span]
   'w5-subsystem-closure-invalid-source-extension-request
   ["W5-SC-SCHEMA" :source-extension]
   'w5-subsystem-closure-invalid-source-extension-mismatch-request
   ["W5-SC-PROVENANCE" :source-span]
   'w5-subsystem-closure-invalid-source-span-source-id-request
   ["W5-SC-SCHEMA" :source-span]
   'w5-subsystem-closure-invalid-runtime-span-crosslink-request
   ["W5-SC-RUNTIME" :runtime]
   'w5-subsystem-closure-invalid-compiler-source-suffix-request
   ["W5-SC-COMPILER" :compiler]
   'w5-subsystem-closure-invalid-transcript-parity-id-request
   ["W5-SC-TRANSCRIPT" :transcripts]
   'w5-subsystem-closure-invalid-replay-path-request
   ["W5-SC-REPLAY" :replay]
   'w5-subsystem-closure-invalid-tcb-substitution-request
   ["W5-SC-TCB" :residual-tcb]
   'w5-subsystem-closure-invalid-provenance-path-request
   ["W5-SC-PROVENANCE" :provenance]
   'w5-subsystem-closure-invalid-coherent-environment-request
   ["W5-SC-RECIPE" :build-recipes]})

(deftest w5-subsystem-closure-engine-and-fixtures-compile
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (doseq [function
          '[w5-subsystem-closure-policy
            w5-subsystem-closure-diagnostic-catalog
            w5-subsystem-closure-target-valid?
            w5-sc-path-ends-with?
            w5-sc-source-fixture-path?
            w5-subsystem-closure-source-valid?
            w5-subsystem-closure-executable-valid?
            w5-subsystem-closure-conformance-valid?
            w5-subsystem-closure-provenance-valid?
            w5-subsystem-closure-subsystem-valid?
            w5-subsystem-closure-subsystems-valid?
            w5-subsystem-closure-linkage-valid?
            w5-subsystem-closure-compiler-valid?
            w5-subsystem-closure-transcripts-valid?
            w5-subsystem-closure-replay-valid?
            w5-subsystem-closure-build-recipes-valid?
            w5-subsystem-closure-environments-valid?
            w5-subsystem-closure-locks-valid?
            w5-subsystem-closure-toolchains-valid?
            w5-subsystem-closure-evidence-valid?
            w5-subsystem-closure-provenance-records-valid?
            w5-subsystem-closure-tcb-valid?
            w5-subsystem-closure-authority-valid?
            w5-subsystem-closure-request-valid?
            w5-subsystem-closure-identity-input
            w5-subsystem-closure-provenance
            w5-subsystem-closure
            w5-subsystem-closure-verify
            w5-subsystem-closure-execute
            w5-subsystem-closure-run
            w5-subsystem-closure-recompute
            w5-subsystem-closure-verify-result]]
    (is (map? (plan-function engine-plan function)) function))
  (doseq [[family basename]
          [["accepted" "incomplete-subsystem-closure"]
           ["rejected" "invalid-subsystem-closure"]]]
    (is (= (slurp (path (fixture-path family basename ".gravity")))
           (slurp (path (fixture-path family basename ".qst")))))))

(deftest w5-subsystem-closure-policy-is-exact-static-and-nonauthority
  (let [policy (request engine-plan 'w5-subsystem-closure-policy)
        catalog (request engine-plan 'w5-subsystem-closure-diagnostic-catalog)]
    (is (= :gravity/w5-subsystem-closure-policy (:artifact policy)))
    (is (= :wave4-static-subsystem-closure (:scope policy)))
    (is (= :jvm (:harness-target policy)))
    (is (= :llvm-x86_64-linux (:candidate-target policy)))
    (is (= {:os :linux :arch :x86_64 :backend :llvm
            :artifact-format :elf :abi :sysv-amd64}
           (:candidate-platform policy)))
    (is (= [:runtime :standard-library :package-build]
           (:required-subsystems policy)))
    (is (= [:darwin :darwin-arm64 :darwin-x86_64 :windows]
           (:unsupported-targets policy)))
    (is (= [{:target :darwin :invokes-clojure? false :links-jvm? false
             :fallback? false}
            {:target :darwin-arm64 :invokes-clojure? false :links-jvm? false
             :fallback? false}
            {:target :darwin-x86_64 :invokes-clojure? false :links-jvm? false
             :fallback? false}
            {:target :windows :invokes-clojure? false :links-jvm? false
             :fallback? false}]
           (:unsupported-target-policies policy)))
    (is (false? (:cross-target-inference? policy)))
    (is (false? (:darwin-fallback? policy)))
    (is (false? (:candidate-invokes-clojure? policy)))
    (is (false? (:candidate-links-jvm? policy)))
    (is (true? (:no-fallback? policy)))
    (is (true? (:static-only? policy)))
    (is (= :incomplete (:closure-status policy)))
    (is (= :blocked (:completion-status policy)))
    (is (= :non-authority (:authority policy)))
    (is (true? (:clojure-seed-boundary? policy)))
    (is (false? (:self-hosted? policy)))
    (is (false? (:release? policy)))
    (is (false? (:public-authority? policy)))
    (is (= :non-authority (:authority catalog)))
    (is (= (set ["W5-SC-SCHEMA" "W5-SC-TARGET" "W5-SC-RUNTIME"
                "W5-SC-STDLIB" "W5-SC-PACKAGE" "W5-SC-LINKAGE"
                "W5-SC-COMPILER" "W5-SC-TRANSCRIPT" "W5-SC-REPLAY"
                "W5-SC-RECIPE" "W5-SC-ENVIRONMENT" "W5-SC-LOCK"
                "W5-SC-TOOLCHAIN" "W5-SC-CONFORMANCE"
                "W5-SC-PROVENANCE" "W5-SC-TCB" "W5-SC-AUTHORITY"
                "W5-SC-UPSTREAM" "W5-SC-SUBSTITUTION"])
           (set (:diagnostics catalog))))))

(deftest w5-subsystem-closure-accepts-incomplete-descriptors
  (let [gravity-request
        (request-for-source accepted-gravity-plan "/checkout-a" ".gravity")
        qst-request
        (request-for-source accepted-qst-plan "/checkout-a" ".qst")
        result (invoke engine-plan 'w5-subsystem-closure [gravity-request])]
    (is (= (dissoc gravity-request :source-span :source-extension)
           (dissoc qst-request :source-span :source-extension)))
    (is (= ".gravity" (:source-extension gravity-request)))
    (is (= ".qst" (:source-extension qst-request)))
    (is (str/ends-with?
         (get (get gravity-request :source-span) :actual-source-path)
         ".gravity"))
    (is (str/ends-with?
         (get (get qst-request :source-span) :actual-source-path)
         ".qst"))
    (is (true? (invoke engine-plan
                       'w5-subsystem-closure-request-valid?
                       [qst-request])))
    (is (= :incomplete (:claimed-status gravity-request)))
    (doseq [subsystem (vals (:subsystems gravity-request))]
      (is (= :descriptor-only (:source-status subsystem)))
      (is (= :descriptor-only (:executable-status subsystem)))
      (is (= :pending (:conformance-status subsystem)))
      (is (= :descriptor-only (:provenance-status subsystem))))
    (is (= :descriptor-only (nested-get gravity-request :compiler :status)))
    (is (= :pending
           (nested-get gravity-request :cross-subsystem-linkage :status)))
    (is (false? (:native-host? (first (:replay gravity-request)))))
    (is (false? (:emulator-authority?
                 (first (:replay gravity-request)))))
    (is (not (some #{:development-emulator}
                   (nested-get gravity-request
                               :residual-tcb
                               :residual-trusted-components))))
    (is (true? (invoke engine-plan
                       'w5-subsystem-closure-request-valid?
                       [gravity-request])))
    (is (= :accepted (:status result)))
    (is (= :incomplete (:closure-status result)))
    (is (= :blocked (:completion-status result)))
    (is (= :blocked (:verification-status result)))
    (is (= :non-authority (:authority result)))
    (is (= :llvm-x86_64-linux (:candidate-target result)))
    (is (= :pending (nested-get result :execution :runtime)))
    (is (= :pending (nested-get result :execution :standard-library)))
    (is (= :pending (nested-get result :execution :package-build)))
    (is (empty? (:diagnostics result)))
    (is (true? (:clojure-seed-boundary? result)))
    (is (false? (:self-hosted? result)))
    (is (false? (:release? result)))
    (is (false? (:public-authority? result)))
    (is (some #{:residual-tcb-present} (:gaps result)))
    (is (some #{:runtime-execution-pending} (:gaps result)))))

(deftest w5-subsystem-closure-source-path-helpers-fail-closed
  (is (true? (invoke engine-plan 'w5-sc-path-ends-with?
                     ["x.gravity" ".gravity"])))
  (is (true? (invoke engine-plan 'w5-sc-path-ends-with?
                     ["x" "x"])))
  (is (false? (invoke engine-plan 'w5-sc-path-ends-with?
                      ["x.qst" ".gravity"])))
  (is (false? (invoke engine-plan 'w5-sc-path-ends-with?
                      ["x" ".gravity"])))
  (is (false? (invoke engine-plan 'w5-sc-path-ends-with?
                      ["x" "xx"])))
  (is (true? (invoke engine-plan 'w5-sc-source-fixture-path?
                     ["x.qst"])))
  (is (false? (invoke engine-plan 'w5-sc-source-fixture-path?
                      ["x.edn"]))))

(deftest w5-subsystem-closure-identity-is-path-neutral
  (let [left-request
        (request accepted-gravity-plan 'w5-subsystem-closure-request)
        right-request
        (request accepted-gravity-plan
                 'w5-subsystem-closure-alternate-path-request)
        left (invoke engine-plan 'w5-subsystem-closure [left-request])
        right (invoke engine-plan 'w5-subsystem-closure [right-request])]
    (is (= (:identity-input left) (:identity-input right)))
    (is (not= (:provenance left) (:provenance right)))
    (is (not (str/includes? (pr-str (:identity-input left)) "/checkout-a")))
    (is (not (str/includes? (pr-str (:identity-input right)) "/checkout-b")))
    (is (str/includes? (pr-str (:provenance left)) "/checkout-a"))
    (is (str/includes? (pr-str (:provenance right)) "/checkout-b"))))

(deftest w5-subsystem-closure-rejected-fixture-covers-diagnostic-families
  (doseq [[accepted-plan rejected-plan extension]
          [[accepted-gravity-plan rejected-gravity-plan ".gravity"]
           [accepted-qst-plan rejected-qst-plan ".qst"]]]
    (let [base (request-for-source accepted-plan "/checkout-a" extension)]
      (doseq [[function-name [expected-rule expected-field]] rejected-cases]
        (testing (str function-name " " extension)
          (let [invalid
              (invoke rejected-plan function-name [base])
              result (invoke engine-plan 'w5-subsystem-closure [invalid])
              diagnostic (get (:diagnostics result) 0)]
          (is (= :rejected (:status result)))
          (is (= expected-rule (:rule diagnostic)))
          (is (= expected-rule (:diagnostic-id diagnostic)))
          (is (= expected-field (:field diagnostic)))
          (is (= :w5-subsystem-closure (:stage diagnostic)))
          (is (= :meta (:profile diagnostic)))
          (is (= :llvm-x86_64-linux (:target diagnostic)))
          (is (map? (:source-span diagnostic)))
          (is (map? (:provenance diagnostic)))
          (is (keyword? (:remediation diagnostic)))
          (is (= :non-authority (:authority result)))
          (is (true? (:clojure-seed-boundary? result)))
          (is (false? (:self-hosted? result)))
          (is (false? (:release? result)))
          (is (false? (:public-authority? result)))))))))

(deftest w5-subsystem-closure-result-substitution-is-rejected
  (let [base (request accepted-gravity-plan 'w5-subsystem-closure-request)
        result (invoke engine-plan 'w5-subsystem-closure [base])
        verification
        (invoke engine-plan 'w5-subsystem-closure-verify-result
                [base result])
        substituted
        (invoke rejected-gravity-plan
                'w5-subsystem-closure-substituted-result [result])
        substituted-verification
        (invoke engine-plan 'w5-subsystem-closure-verify-result
                [base substituted])]
    (is (= :passed (:status verification)))
    (is (true? (:recomputed verification)))
    (is (= :rejected (:status substituted-verification)))
    (is (= "W5-SC-SUBSTITUTION"
           (nested-get3 substituted-verification :diagnostics 0 :rule)))
    (is (= :non-authority (:authority substituted-verification)))))
