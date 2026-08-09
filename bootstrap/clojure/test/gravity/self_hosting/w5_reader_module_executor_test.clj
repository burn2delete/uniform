(ns gravity.self-hosting.w5-reader-module-executor-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

; Later focused command (intentionally not run in this static-only change):
; clojure -M:test --namespace gravity.self-hosting.w5-reader-module-executor-test

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/w5_reader_module_executor_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "W5 reader-module test source is not on the classpath"
        {:id "W5-RM-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "W5-RM-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private engine-source
  "bootstrap/gravity/src/gravity/self_hosting/w5_reader_module_executor.gravity")
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/w5-reader-module")

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
    (fixture-path "accepted" "reader-module-execution" ".gravity"))))
(def ^:private accepted-qst-plan
  (delay
   (compile-plan
    (fixture-path "accepted" "reader-module-execution" ".qst"))))
(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-path "rejected" "invalid-reader-module-execution" ".gravity"))))
(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (fixture-path "rejected" "invalid-reader-module-execution" ".qst"))))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-w5-reader-module-executor
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- request
  ([plan function]
   (invoke plan function []))
  ([plan function arguments]
   (invoke plan function arguments)))

(defn- request-at [plan checkout extension source-kind]
  (invoke plan 'w5-reader-module-execution-request-at
          [checkout extension source-kind]))

(defn- verify [request-value]
  (invoke engine-plan 'w5-reader-module-verify [request-value]))

(def ^:private rejected-cases
  {'w5-reader-module-invalid-schema-request-at "W5-RM-SCHEMA"
   'w5-reader-module-invalid-artifact-request-at "W5-RM-SCHEMA"
   'w5-reader-module-invalid-verifier-request-at "W5-RM-SCHEMA"
   'w5-reader-module-invalid-profile-request-at "W5-RM-SCHEMA"
   'w5-reader-module-invalid-status-request-at "W5-RM-SCHEMA"
   'w5-reader-module-invalid-residual-request-at "W5-RM-AUTHORITY"
   'w5-reader-module-invalid-span-request-at "W5-RM-SCHEMA"
   'w5-reader-module-invalid-span-missing-request-at "W5-RM-SCHEMA"
   'w5-reader-module-invalid-span-extra-request-at "W5-RM-SCHEMA"
   'w5-reader-module-invalid-span-type-request-at "W5-RM-SCHEMA"
   'w5-reader-module-invalid-span-negative-request-at "W5-RM-SCHEMA"
   'w5-reader-module-invalid-span-order-request-at "W5-RM-SCHEMA"
   'w5-reader-module-invalid-target-request-at "W5-RM-TARGET"
   'w5-reader-module-invalid-source-unit-request-at "W5-RM-SOURCE-UNIT"
   'w5-reader-module-invalid-syntax-origin-request-at "W5-RM-SYNTAX-ORIGIN"
   'w5-reader-module-invalid-macro-hygiene-request-at "W5-RM-MACRO-HYGIENE"
   'w5-reader-module-invalid-namespace-module-request-at
   "W5-RM-NAMESPACE-MODULE"
   'w5-reader-module-invalid-core-lowering-request-at
   "W5-RM-CORE-LOWERING"
   'w5-reader-module-invalid-lineage-request-at "W5-RM-LINEAGE"
   'w5-reader-module-invalid-provenance-request-at "W5-RM-PROVENANCE"
   'w5-reader-module-invalid-evidence-request-at "W5-RM-EVIDENCE"
   'w5-reader-module-invalid-authority-request-at "W5-RM-AUTHORITY"
   'w5-reader-module-invalid-c2-request-at "W5-RM-SOURCE-UNIT"
   'w5-reader-module-invalid-c3-request-at "W5-RM-SYNTAX-ORIGIN"
   'w5-reader-module-invalid-c4-request-at "W5-RM-MACRO-HYGIENE"
   'w5-reader-module-invalid-c5-request-at "W5-RM-NAMESPACE-MODULE"
   'w5-reader-module-invalid-c6-request-at "W5-RM-CORE-LOWERING"})

(deftest w5-reader-module-engine-and-fixtures-compile
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (doseq [function
          '[w5-reader-module-policy
            w5-reader-module-diagnostic-catalog
            w5-reader-module-target-valid?
            w5-reader-module-source-unit-valid?
            w5-reader-module-syntax-origin-valid?
            w5-reader-module-macro-hygiene-valid?
            w5-reader-module-namespace-valid?
            w5-reader-module-module-valid?
            w5-reader-module-core-lowering-valid?
            w5-reader-module-request-valid?
            w5-reader-module-input
            w5-reader-module-record
            w5-reader-module-diagnostic
            w5-rm-diagnostic-source-span
            w5-reader-module-verify
            w5-reader-module-execute
            w5-reader-module-run
            w5-reader-module-recompute
            w5-reader-module-verify-result
            w5-rm-policy w5-rm-diagnostic-catalog
            w5-rm-string-suffix? w5-rm-extension-paths-valid?
            w5-rm-target-valid?
            w5-rm-request-valid? w5-rm-verify w5-rm-execute w5-rm-run
            w5-rm-recompute w5-rm-verify-result]]
    (is (map? (get (get @engine-plan :functions) function)) function))
  (doseq [[family basename]
          [["accepted" "reader-module-execution"]
           ["rejected" "invalid-reader-module-execution"]]]
    (is (= (slurp (path (fixture-path family basename ".gravity")))
           (slurp (path (fixture-path family basename ".qst")))))))

(deftest w5-reader-module-suffix-helpers-guard-short-paths
  (doseq [[value suffix expected]
          [[".gravity" ".gravity" true]
           ["x" ".gravity" false]
           [".qst" ".gravity" false]]]
    (is (= expected
           (invoke engine-plan 'w5-rm-string-suffix? [value suffix]))))
  (doseq [[extension actual-path project-path expected]
          [[".gravity" "/checkout-a/file.gravity" "file.gravity" true]
           [".gravity" "x" "x" false]
           [".qst" "/checkout-a/file.gravity" "file.gravity" false]]]
    (is (= expected
           (invoke engine-plan 'w5-rm-extension-paths-valid?
                   [extension actual-path project-path])))))

(deftest w5-reader-module-policy-is-static-and-nonauthority
  (let [policy (request engine-plan 'w5-reader-module-policy)
        catalog (request engine-plan 'w5-reader-module-diagnostic-catalog)]
    (is (= :gravity/w5-reader-module-execution-policy (:artifact policy)))
    (is (= :wave5-slice-a-static-only (:scope policy)))
    (is (= :meta (:profile policy)))
    (is (= :jvm (:target policy)))
    (is (= :llvm-x86_64-linux (:candidate-target policy)))
    (is (= {:os :linux :arch :x86_64 :backend :llvm
            :artifact-format :elf :abi :sysv-amd64}
           (:candidate-platform policy)))
    (is (= [:darwin :darwin-arm64 :darwin-x86_64 :windows]
           (:unsupported-platforms policy)))
    (is (= :non-authority (:authority policy)))
    (is (true? (:static-only? policy)))
    (is (true? (:clojure-seed-boundary? policy)))
    (is (false? (:self-hosted? policy)))
    (is (false? (:release? policy)))
    (is (false? (:public-authority? policy)))
    (is (false? (:fl-240-credit? policy)))
    (is (= :incomplete (:completion-status policy)))
    (is (some #{"W5-RM-C2"} (:rules catalog)))
    (is (some #{"W5-RM-C6"} (:rules catalog)))
    (doseq [record (:unsupported-target-policies policy)]
      (is (false? (:invokes-clojure? record)))
      (is (false? (:links-jvm? record)))
      (is (false? (:fallback? record))))))

(deftest w5-reader-module-accepted-record-is-incomplete
  (doseq [[plan checkout extension source-kind]
          [[accepted-gravity-plan "/checkout-a/gravity" ".gravity" :gravity]
           [accepted-qst-plan "/checkout-a/gravity" ".qst" :qst]]]
    (let [request-value (request-at plan checkout extension source-kind)
          result (verify request-value)]
      (is (= :incomplete (:claimed-status request-value)))
      (is (= :accepted (:status result)))
      (is (= :incomplete (:completion-status result)))
      (is (= :blocked (get (get result :verifier-gate) :decision)))
      (is (false? (get (get result :verifier-gate) :release-eligible?)))
      (is (= [:c2-reader :c3-syntax-origin :c4-macro-hygiene
              :c5-namespace-module :c6-core-lowering]
             (:phase-order result)))
      (is (empty? (:diagnostics result)))
      (is (= :pending (:execution-status result)))
      (is (true? (:clojure-seed-boundary? result)))
      (is (false? (:self-hosted? result)))
      (is (false? (:release? result)))
      (is (false? (:public-authority? result)))
      (is (= :non-authority (:authority result)))
      (is (true? (:fail-closed? result))))))

(deftest w5-reader-module-provenance-identifies-each-source-kind
  (let [gravity (request-at accepted-gravity-plan
                            "/checkout-a/gravity" ".gravity" :gravity)
        qst (request-at accepted-qst-plan
                        "/checkout-a/gravity" ".qst" :qst)]
    (is (= ".gravity"
           (get-in gravity [:provenance :actual-source-extension])))
    (is (= :gravity (get-in gravity [:provenance :source-kind])))
    (is (= ".qst"
           (get-in qst [:provenance :actual-source-extension])))
    (is (= :qst (get-in qst [:provenance :source-kind])))
    (is (str/ends-with?
         (get-in gravity [:provenance :actual-source-path]) ".gravity"))
    (is (str/ends-with?
         (get-in qst [:provenance :actual-source-path]) ".qst"))))

(deftest w5-reader-module-identity-is-path-neutral
  (let [left-request
        (request accepted-gravity-plan
                 'w5-reader-module-execution-request
                 ["/checkout-a/gravity" ".gravity" :gravity])
        right-request
        (request accepted-gravity-plan
                 'w5-reader-module-alternate-path-request
                 ["/checkout-b/gravity" ".gravity" :gravity])
        left (verify left-request)
        right (verify right-request)]
    (is (= (:identity-input left) (:identity-input right)))
    (is (not= (:provenance left) (:provenance right)))
    (is (not (str/includes? (pr-str (:identity-input left))
                            "/checkout-a/")))
    (is (not (str/includes? (pr-str (:identity-input right))
                            "/checkout-b/")))
    (is (str/includes? (pr-str (:provenance left)) "/checkout-a/"))
    (is (str/includes? (pr-str (:provenance right)) "/checkout-b/"))))

(deftest w5-reader-module-rejected-fixture-covers-families
  (doseq [[function-name expected-rule] rejected-cases]
    (testing (str function-name)
      (doseq [[accepted-plan rejected-plan]
              [[accepted-gravity-plan rejected-gravity-plan]
               [accepted-qst-plan rejected-qst-plan]]]
        (let [extension (if (= accepted-plan accepted-qst-plan)
                          ".qst"
                          ".gravity")
              source-kind (if (= extension ".qst") :qst :gravity)
              base (request-at accepted-plan "/checkout-a/gravity"
                               extension source-kind)
              rejected-base (request-at rejected-plan "/checkout-a/gravity"
                                         extension source-kind)
              invalid (invoke rejected-plan function-name [base])
              result (verify invalid)
              diagnostic (first (:diagnostics result))]
          (is (= base rejected-base))
          (is (= :rejected (:status result)))
          (is (= 1 (count (:diagnostics result))))
          (is (= expected-rule (:rule diagnostic)))
          (is (= expected-rule (:diagnostic-id diagnostic)))
          (is (= :reader-module (:stage diagnostic)))
          (is (= :meta (:profile diagnostic)))
          (is (= :llvm-x86_64-linux (:target diagnostic)))
          (is (map? (:source-span diagnostic)))
          (is (map? (:provenance diagnostic)))
          (is (keyword? (:remediation diagnostic)))
          (is (= :non-authority (:authority result)))
          (is (true? (:clojure-seed-boundary? result)))
          (is (false? (:self-hosted? result)))
          (is (false? (:release? result))))))))

(deftest w5-reader-module-rejects-path-suffix-substitution
  (doseq [[accepted-plan rejected-plan function extension source-kind]
          [[accepted-gravity-plan rejected-gravity-plan
            'w5-reader-module-invalid-gravity-path-suffix-request-at
            ".gravity" :gravity]
           [accepted-qst-plan rejected-qst-plan
            'w5-reader-module-invalid-qst-path-suffix-request-at
            ".qst" :qst]]]
    (let [base (request-at accepted-plan "/checkout-a/gravity"
                          extension source-kind)
          invalid (invoke rejected-plan function [base])
          diagnostic (first (:diagnostics (verify invalid)))]
      (is (= extension
             (get-in invalid [:provenance :actual-source-extension])))
      (is (= source-kind (get-in invalid [:provenance :source-kind])))
      (is (str/ends-with?
           (get-in invalid [:provenance :actual-source-path])
           (if (= extension ".gravity") ".qst" ".gravity")))
      (is (= :rejected (:status (verify invalid))))
      (is (= "W5-RM-PROVENANCE" (:rule diagnostic)))
      (is (= "W5-RM-PROVENANCE" (:diagnostic-id diagnostic)))
      (is (= :retain-actual-path-and-extension-provenance
             (:remediation diagnostic))))))

(deftest w5-reader-module-diagnostic-span-fails-closed
  (let [fallback {:source-id :gravity/w5-reader-module-diagnostic
                  :start-byte 0 :end-byte 0 :line 1 :column 1}]
    (doseq [[accepted-plan rejected-plan function extension source-kind]
            [[accepted-gravity-plan rejected-gravity-plan
              'w5-reader-module-invalid-span-missing-request-at
              ".gravity" :gravity]
             [accepted-gravity-plan rejected-gravity-plan
              'w5-reader-module-invalid-span-request-at
              ".gravity" :gravity]
             [accepted-qst-plan rejected-qst-plan
              'w5-reader-module-invalid-span-missing-request-at
              ".qst" :qst]
             [accepted-qst-plan rejected-qst-plan
              'w5-reader-module-invalid-span-request-at
              ".qst" :qst]]]
      (let [base (request-at accepted-plan "/checkout-a/gravity"
                            extension source-kind)
            invalid (invoke rejected-plan function [base])
            diagnostic (first (:diagnostics (verify invalid)))]
        (is (= fallback (:source-span diagnostic)))
        (is (= #{:source-id :start-byte :end-byte :line :column}
               (set (keys (:source-span diagnostic)))))
        (is (= :rejected (:status (verify invalid))))))))

(deftest w5-reader-module-rejects-nested-top-provenance-drift
  (doseq [[accepted-plan rejected-plan function extension source-kind expected-extension]
          [[accepted-gravity-plan rejected-gravity-plan
            'w5-reader-module-invalid-provenance-cross-binding-gravity-request-at
            ".gravity" :gravity ".gravity"]
           [accepted-qst-plan rejected-qst-plan
            'w5-reader-module-invalid-provenance-cross-binding-qst-request-at
            ".qst" :qst ".qst"]
           [accepted-gravity-plan rejected-gravity-plan
            'w5-reader-module-invalid-provenance-cross-kind-gravity-request-at
            ".gravity" :gravity ".qst"]
           [accepted-qst-plan rejected-qst-plan
            'w5-reader-module-invalid-provenance-cross-kind-qst-request-at
            ".qst" :qst ".gravity"]]]
    (let [base (request-at accepted-plan "/checkout-a/gravity"
                          extension source-kind)
          invalid (invoke rejected-plan function [base])
          result (verify invalid)
          diagnostic (first (:diagnostics result))
          nested (get-in invalid [:source-unit :provenance])
          top (:provenance invalid)]
      (is (not= nested top))
      (is (= expected-extension (:actual-source-extension top)))
      (is (= :rejected (:status result)))
      (is (= "W5-RM-PROVENANCE" (:rule diagnostic)))
      (is (= "W5-RM-PROVENANCE" (:diagnostic-id diagnostic)))
      (is (= :provenance (:reason (:facts diagnostic)))))))

(deftest w5-reader-module-result-verifier-recomputes
  (let [request-value
        (request accepted-gravity-plan
                 'w5-reader-module-execution-request
                 ["/checkout-a/gravity" ".gravity" :gravity])
        result (verify request-value)
        verification
        (invoke engine-plan 'w5-reader-module-verify-result
                [request-value result])
        altered (assoc result :completion-status :complete)
        altered-verification
        (invoke engine-plan 'w5-reader-module-verify-result
                [request-value altered])]
    (is (= :passed (:status verification)))
    (is (true? (:recomputed verification)))
    (is (= :incomplete (:completion-status verification)))
    (is (= :rejected (:status altered-verification)))
    (is (= "W5-RM-SUBSTITUTION"
           (get (first (:diagnostics altered-verification)) :rule)))
    (is (= :non-authority (:authority altered-verification)))
    (is (true? (:clojure-seed-boundary? altered-verification)))))
