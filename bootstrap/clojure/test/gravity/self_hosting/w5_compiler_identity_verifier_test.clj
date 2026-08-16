(ns gravity.self-hosting.w5-compiler-identity-verifier-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

; Later focused command (intentionally not run in this static-only change):
; clojure -M:test --namespace gravity.self-hosting.w5-compiler-identity-verifier-test

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/w5_compiler_identity_verifier_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "W5 compiler-identity test source is not on the classpath"
        {:id "W5-CI-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "W5-CI-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private engine-source
  "bootstrap/gravity/src/gravity/self_hosting/w5_compiler_identity_verifier.gravity")
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/w5-compiler-identity")

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
    (fixture-path "accepted" "incomplete-compiler-identity" ".gravity"))))
(def ^:private accepted-qst-plan
  (delay
   (compile-plan
    (fixture-path "accepted" "incomplete-compiler-identity" ".qst"))))
(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-path "rejected" "invalid-compiler-identity" ".gravity"))))
(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (fixture-path "rejected" "invalid-compiler-identity" ".qst"))))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-w5-compiler-identity-verifier
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- request [plan function]
  (invoke plan function []))

(defn- request-for-source [plan root-path extension]
  (invoke plan 'w5-compiler-identity-request-for-source
          [root-path extension]))

(defn- verify [request-value]
  (invoke engine-plan 'w5-compiler-identity-verify [request-value]))

(defn- attacker-path [actual-path]
  (str "/attacker" (subs actual-path (count "/checkout-a/gravity"))))

(def ^:private rejected-cases
  {'w5-compiler-identity-invalid-source-request "W5-CI-SOURCE"
   'w5-compiler-identity-invalid-constructor-request "W5-CI-CONSTRUCTOR"
   'w5-compiler-identity-invalid-executable-request "W5-CI-EXECUTABLE"
   'w5-compiler-identity-invalid-stage-request "W5-CI-STAGE"
   'w5-compiler-identity-invalid-lineage-request "W5-CI-LINEAGE"
   'w5-compiler-identity-invalid-recipe-request "W5-CI-RECIPE"
   'w5-compiler-identity-invalid-environment-request "W5-CI-ENVIRONMENT"
   'w5-compiler-identity-invalid-lock-request "W5-CI-LOCK"
   'w5-compiler-identity-invalid-toolchain-request "W5-CI-TOOLCHAIN"
   'w5-compiler-identity-invalid-provenance-request "W5-CI-PROVENANCE"
   'w5-compiler-identity-invalid-target-request "W5-CI-TARGET"
   'w5-compiler-identity-invalid-cross-target-request "W5-CI-TARGET"
   'w5-compiler-identity-invalid-fallback-request "W5-CI-TARGET"
   'w5-compiler-identity-invalid-conformance-request "W5-CI-CONFORMANCE"
   'w5-compiler-identity-invalid-evidence-request "W5-CI-EVIDENCE"
   'w5-compiler-identity-invalid-authority-request "W5-CI-AUTHORITY"
   'w5-compiler-identity-invalid-source-crosslink-request "W5-CI-SOURCE"
   'w5-compiler-identity-invalid-stage-crosslink-request "W5-CI-STAGE"
   'w5-compiler-identity-invalid-paired-lineage-request "W5-CI-LINEAGE"
   'w5-compiler-identity-invalid-provenance-crosslink-request "W5-CI-PROVENANCE"
   'w5-compiler-identity-invalid-provenance-suffix-request "W5-CI-PROVENANCE"
   'w5-compiler-identity-invalid-attacker-root-request "W5-CI-PROVENANCE"
   'w5-compiler-identity-invalid-coherent-alternate-root-request
   "W5-CI-PROVENANCE"
   'w5-compiler-identity-invalid-source-span-request "W5-CI-SCHEMA"
   'w5-compiler-identity-invalid-top-span-owner-request "W5-CI-PROVENANCE"
   'w5-compiler-identity-invalid-inventory-span-owner-request "W5-CI-SOURCE"
   'w5-compiler-identity-invalid-constructor-span-owner-request
   "W5-CI-CONSTRUCTOR"
   'w5-compiler-identity-invalid-stage-span-owner-request "W5-CI-STAGE"})

(deftest w5-compiler-identity-engine-and-fixtures-compile
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (doseq [function
          '[w5-compiler-identity-policy
            w5-compiler-identity-diagnostic-catalog
            w5-compiler-identity-target-valid?
            w5-ci-source-span?
            w5-ci-source-span-for?
            w5-ci-path-ends-with?
            w5-ci-source-extension
            w5-ci-source-path?
            w5-ci-repository-root
            w5-ci-path-at-root?
            w5-ci-path-provenance?
            w5-ci-provenance-crosslinks-valid?
            w5-ci-diagnostic-source-span
            w5-compiler-identity-source-inventory-valid?
            w5-compiler-identity-constructors-valid?
            w5-compiler-identity-executables-valid?
            w5-compiler-identity-stages-valid?
            w5-compiler-identity-lineage-valid?
            w5-compiler-identity-build-recipe-valid?
            w5-compiler-identity-environment-valid?
            w5-compiler-identity-lock-valid?
            w5-compiler-identity-toolchain-valid?
            w5-compiler-identity-provenance-valid?
            w5-compiler-identity-conformance-valid?
            w5-compiler-identity-evidence-valid?
            w5-compiler-identity-authority-valid?
            w5-compiler-identity-request-valid?
            w5-compiler-identity-input
            w5-compiler-identity-record
            w5-compiler-identity-verify
            w5-compiler-identity-execute
            w5-compiler-identity-run
            w5-compiler-identity-recompute
            w5-ci-result-shape?
            w5-compiler-identity-verify-result]]
    (is (map? (get (get @engine-plan :functions) function)) function))
  (doseq [[family basename]
          [["accepted" "incomplete-compiler-identity"]
           ["rejected" "invalid-compiler-identity"]]]
    (is (= (slurp (path (fixture-path family basename ".gravity")))
           (slurp (path (fixture-path family basename ".qst")))))))

(deftest w5-compiler-identity-policy-is-exact-static-and-nonauthority
  (let [policy (request engine-plan 'w5-compiler-identity-policy)
        target (:candidate-platform policy)
        catalog (request engine-plan 'w5-compiler-identity-diagnostic-catalog)]
    (is (= :gravity/w5-compiler-identity-policy (:artifact policy)))
    (is (= :wave4-static-only (:scope policy)))
    (is (= :jvm (:target policy)))
    (is (= :llvm-x86_64-linux (:candidate-target policy)))
    (is (= {:os :linux :arch :x86_64 :backend :llvm
            :artifact-format :elf :abi :sysv-amd64}
           target))
    (is (= [:darwin :darwin-arm64 :darwin-x86_64 :windows]
           (:unsupported-platforms policy)))
    (is (= :non-authority (:authority policy)))
    (is (true? (:static-only? policy)))
    (is (true? (:clojure-seed-boundary? policy)))
    (is (false? (:self-hosted? policy)))
    (is (false? (:release? policy)))
    (is (false? (:public-authority? policy)))
    (is (= :incomplete (:completion-status policy)))
    (is (some #{"W5-CI-LINEAGE"} (:rules catalog)))
    (doseq [record (:unsupported-target-policies policy)]
      (is (= :unsupported (:support record)))
      (is (false? (:invokes-clojure? record)))
      (is (false? (:links-jvm? record)))
      (is (false? (:fallback? record))))))

(deftest w5-compiler-identity-span-and-suffix-helpers-fail-closed
  (is (true? (invoke engine-plan 'w5-ci-path-ends-with?
                     ["x.gravity" ".gravity"])))
  (is (false? (invoke engine-plan 'w5-ci-path-ends-with?
                      ["x.qst" ".gravity"])))
  (is (false? (invoke engine-plan 'w5-ci-path-ends-with?
                     ["x" ".gravity"])))
  (is (= ".gravity"
         (invoke engine-plan 'w5-ci-source-extension ["x.gravity"])))
  (is (= ".qst"
         (invoke engine-plan 'w5-ci-source-extension ["x.qst"])))
  (is (= nil
         (invoke engine-plan 'w5-ci-source-extension ["x.edn"])))
  (is (true? (invoke engine-plan 'w5-ci-source-path?
                     ["x.gravity"])))
  (is (true? (invoke engine-plan 'w5-ci-source-path?
                     ["x.qst"])))
  (is (false? (invoke engine-plan 'w5-ci-source-path?
                      ["x.edn"])))
  (is (= "/checkout-a/gravity"
         (invoke engine-plan 'w5-ci-repository-root
                 [(str "/checkout-a/gravity/bootstrap/clojure/fixtures/"
                       "self-hosting/w5-compiler-identity/accepted/"
                       "incomplete-compiler-identity.gravity")])))
  (is (= nil
         (invoke engine-plan 'w5-ci-repository-root
                 ["/attacker/incomplete-compiler-identity.gravity"])))
  (is (true? (invoke engine-plan 'w5-ci-path-at-root?
                     ["/checkout-a/gravity/source/:compiler.gravity"
                      "/checkout-a/gravity"
                      "/source/:compiler.gravity"])))
  (is (false? (invoke engine-plan 'w5-ci-path-at-root?
                      ["/attacker/source/:compiler.gravity"
                       "/checkout-a/gravity"
                       "/source/:compiler.gravity"])))
  (is (false? (invoke engine-plan 'w5-ci-source-span?
                      [{:source-id :bad :start-byte -1 :end-byte 0
                        :line 1 :column 1}])))
  (is (false? (invoke engine-plan 'w5-ci-source-span?
                      [{:source-id :bad :start-byte 0 :end-byte 1
                        :line 1 :column 1}])))
  (is (true? (invoke engine-plan 'w5-ci-source-span-for?
                     [{:source-id :gravity.self-hosting/w5-compiler-identity
                       :start-byte 0 :end-byte 1 :line 1 :column 1}
                      :gravity.self-hosting/w5-compiler-identity])))
  (is (= {:source-id :gravity.self-hosting/w5-compiler-identity
          :start-byte 0 :end-byte 0 :line 1 :column 1}
         (invoke engine-plan 'w5-ci-diagnostic-source-span
                 [{:source-span {:source-id :bad :start-byte -1
                                 :end-byte 0 :line 1 :column 1}}]))))

(deftest w5-compiler-identity-accepted-record-is-incomplete
  (doseq [[plan extension]
          [[accepted-gravity-plan ".gravity"]
           [accepted-qst-plan ".qst"]]]
    (let [request-value
          (request-for-source plan "/checkout-a/gravity" extension)
          result (verify request-value)
          stages (:stages request-value)]
      (is (= :incomplete (:claimed-status request-value)))
      (is (= :accepted (:status result)))
      (is (= :incomplete (:completion-status result)))
      (is (= :gravity/w5-compiler-identity-result (:artifact result)))
      (is (true? (invoke engine-plan 'w5-ci-result-shape? [result])))
      (is (= (get-in request-value [:provenance :source-id])
             (get-in request-value [:source-span :source-id])))
      (doseq [entry (get-in request-value [:source-inventory :entries])]
        (is (= (:source-id entry) (get-in entry [:source-span :source-id]))))
      (doseq [role [:compiler :verifier :artifact-constructor]]
        (let [constructor (get-in request-value [:constructors role])]
          (is (= (:source-id constructor)
                 (get-in constructor [:source-span :source-id])))))
      (doseq [stage-key [:stage1 :stage2 :stage3]]
        (let [stage (get-in request-value [:stages stage-key])]
          (is (= (:compiler-source-id stage)
                 (get-in stage [:source-span :source-id])))))
      (is (= :blocked
             (get (get result :verifier-gate) :decision)))
      (is (false? (get (get result :verifier-gate) :release-eligible?)))
      (is (= [:stage1 :stage2 :stage3]
             (:stage-order request-value)))
      (is (= (get (get stages :stage1) :output-artifact-id)
             (get (get stages :stage2) :input-artifact-id)))
      (is (= (get (get stages :stage2) :output-artifact-id)
             (get (get stages :stage3) :input-artifact-id)))
      (is (= :pending
             (get (get (get request-value :evidence)
                      :execution-status)))
          )
      (is (empty? (:diagnostics result)))
      (is (str/ends-with?
           (get (get request-value :provenance) :actual-source-path)
           extension))
      (is (true? (:clojure-seed-boundary? result)))
      (is (false? (:self-hosted? result)))
      (is (false? (:release? result)))
      (is (false? (:public-authority? result)))
      (is (= :non-authority (:authority result)))
      (is (true? (:fail-closed? result))))))

(deftest w5-compiler-identity-is-path-neutral-but-provenance-retains-path
  (let [left-request (request-for-source accepted-gravity-plan
                                         "/checkout-a/gravity"
                                         ".gravity")
        right-request
        (request-for-source accepted-gravity-plan
                            "/checkout-b/gravity"
                            ".gravity")
        left (verify left-request)
        right (verify right-request)]
    (is (= (:identity-input left) (:identity-input right)))
    (is (not= (:provenance left) (:provenance right)))
    (is (not (str/includes? (pr-str (:identity-input left)) "/checkout-a/")))
    (is (not (str/includes? (pr-str (:identity-input right)) "/checkout-b/")))
    (is (str/includes? (pr-str (:provenance left)) "/checkout-a/"))
    (is (str/includes? (pr-str (:provenance right)) "/checkout-b/"))))

(deftest w5-compiler-identity-provenance-is-bound-to-one-checkout-root
  (doseq [[accepted-plan rejected-plan extension]
          [[accepted-gravity-plan rejected-gravity-plan ".gravity"]
           [accepted-qst-plan rejected-qst-plan ".qst"]]]
    (let [base (request-for-source accepted-plan
                                   "/checkout-a/gravity" extension)
          attacker
          (invoke rejected-plan
                  'w5-compiler-identity-invalid-attacker-root-request
                  [base])
          alternate
          (invoke rejected-plan
                  'w5-compiler-identity-invalid-coherent-alternate-root-request
                  [base])
          attacker-result (verify attacker)
          alternate-result (verify alternate)]
      (is (str/ends-with?
           (get-in attacker [:provenance :actual-verifier-source-path])
           "/bootstrap/gravity/src/gravity/self_hosting/w5_compiler_identity_verifier.gravity"))
      (is (str/starts-with?
           (get-in attacker [:provenance :actual-verifier-source-path])
           "/attacker/"))
      (is (= (get-in base [:provenance :actual-source-path])
             (get-in alternate [:provenance :actual-source-path])))
      (is (str/starts-with?
           (get-in alternate [:provenance :actual-verifier-source-path])
           "/coherent-alternate/gravity/"))
      (doseq [role [:compiler :verifier :artifact-constructor]]
        (is (str/starts-with?
             (get-in alternate
                     [:constructors role :provenance :actual-source-path])
             "/coherent-alternate/gravity/")))
      (doseq [stage [:stage1 :stage2 :stage3]]
        (is (str/starts-with?
             (get-in alternate [:stages stage :provenance
                                :actual-executable-path])
             "/coherent-alternate/gravity/")))
      (doseq [result [attacker-result alternate-result]]
        (is (= :rejected (:status result)))
        (is (= "W5-CI-PROVENANCE"
               (get-in result [:diagnostics 0 :rule]))))
      (doseq [path-key [:actual-verifier-source-path
                        :actual-constructor-source-path]]
        (let [actual-path (get-in base [:provenance path-key])
              invalid
              (invoke rejected-plan
                      'w5-compiler-identity-substitute-top-provenance-path
                      [base path-key (attacker-path actual-path)])
              result (verify invalid)]
          (is (= :rejected (:status result)))
          (is (= "W5-CI-PROVENANCE"
                 (get-in result [:diagnostics 0 :rule])))))
      (doseq [role [:compiler :verifier :artifact-constructor]
              path-key [:actual-source-path :actual-executable-path
                        :actual-artifact-path]]
        (let [actual-path
              (get-in base [:constructors role :provenance path-key])
              invalid
              (invoke
               rejected-plan
               'w5-compiler-identity-substitute-constructor-provenance-path
               [base role path-key (attacker-path actual-path)])
              result (verify invalid)]
          (is (= :rejected (:status result)))
          (is (= "W5-CI-PROVENANCE"
                 (get-in result [:diagnostics 0 :rule])))))
      (doseq [stage [:stage1 :stage2 :stage3]
              path-key [:actual-source-path :actual-executable-path
                        :actual-artifact-path]]
        (let [actual-path
              (get-in base [:stages stage :provenance path-key])
              invalid
              (invoke
               rejected-plan
               'w5-compiler-identity-substitute-stage-provenance-path
               [base stage path-key (attacker-path actual-path)])
              result (verify invalid)]
          (is (= :rejected (:status result)))
          (is (= "W5-CI-PROVENANCE"
                 (get-in result [:diagnostics 0 :rule]))))))))

(deftest w5-compiler-identity-rejected-fixture-covers-families
  (doseq [[function-name expected-rule] rejected-cases]
    (testing (str function-name)
      (doseq [[accepted-plan rejected-plan extension]
              [[accepted-gravity-plan rejected-gravity-plan ".gravity"]
               [accepted-qst-plan rejected-qst-plan ".qst"]]]
        (let [base (request-for-source accepted-plan
                                       "/checkout-a/gravity"
                                       extension)
              invalid (invoke rejected-plan function-name [base])
              result (verify invalid)
              diagnostic (first (:diagnostics result))]
          (is (= base (invoke rejected-plan
                               'w5-compiler-identity-base-request
                               ["/checkout-a/gravity" extension])))
          (is (= :rejected (:status result)))
          (is (= expected-rule (:rule diagnostic)))
          (is (= expected-rule (:diagnostic-id diagnostic)))
          (is (= :compiler-identity (:stage diagnostic)))
          (is (= :meta (:profile diagnostic)))
          (is (= :llvm-x86_64-linux (:target diagnostic)))
          (is (map? (:source-span diagnostic)))
          (is (map? (:provenance diagnostic)))
          (is (keyword? (:remediation diagnostic)))
          (is (= :non-authority (:authority result)))
          (is (true? (:clojure-seed-boundary? result)))
          (is (false? (:self-hosted? result)))
          (is (false? (:release? result))))))))

(deftest w5-compiler-identity-result-verifier-recomputes
  (let [request-value
        (request-for-source accepted-gravity-plan
                            "/checkout-a/gravity"
                            ".gravity")
        result (verify request-value)
        verification
        (invoke engine-plan 'w5-compiler-identity-verify-result
                [request-value result])
        altered (assoc result :completion-status :complete)
        altered-verification
        (invoke engine-plan 'w5-compiler-identity-verify-result
                [request-value altered])
        artifact-substituted
        (invoke rejected-gravity-plan
                'w5-compiler-identity-result-artifact-substitution [result])
        artifact-verification
        (invoke engine-plan 'w5-compiler-identity-verify-result
                [request-value artifact-substituted])
        extra-key
        (invoke rejected-gravity-plan
                'w5-compiler-identity-result-extra-key [result])
        extra-key-verification
        (invoke engine-plan 'w5-compiler-identity-verify-result
                [request-value extra-key])
        nested-extra-key
        (invoke rejected-gravity-plan
                'w5-compiler-identity-result-nested-extra-key [result])
        nested-extra-key-verification
        (invoke engine-plan 'w5-compiler-identity-verify-result
                [request-value nested-extra-key])]
    (is (= :passed (:status verification)))
    (is (true? (:recomputed verification)))
    (is (= :incomplete (:completion-status verification)))
    (is (= :rejected (:status altered-verification)))
    (is (= :rejected (:status artifact-verification)))
    (is (= :rejected (:status extra-key-verification)))
    (is (= :rejected (:status nested-extra-key-verification)))
    (doseq [candidate [artifact-verification extra-key-verification
                       nested-extra-key-verification]]
      (is (= "W5-CI-SUBSTITUTION"
             (get-in candidate [:diagnostics 0 :rule]))))
    (is (false? (invoke engine-plan 'w5-ci-result-shape?
                        [artifact-substituted])))
    (is (false? (invoke engine-plan 'w5-ci-result-shape?
                        [extra-key])))
    (is (false? (invoke engine-plan 'w5-ci-result-shape?
                        [nested-extra-key])))
    (is (= "W5-CI-SUBSTITUTION"
           (get (first (:diagnostics altered-verification)) :rule)))
    (is (= :non-authority (:authority altered-verification)))
    (is (true? (:clojure-seed-boundary? altered-verification)))))
