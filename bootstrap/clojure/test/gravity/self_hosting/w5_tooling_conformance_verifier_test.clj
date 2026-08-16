(ns gravity.self-hosting.w5-tooling-conformance-verifier-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

; Later focused command (intentionally not run in this static-only change):
; clojure -M:test --namespace gravity.self-hosting.w5-tooling-conformance-verifier-test

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/w5_tooling_conformance_verifier_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "W5 tooling-conformance test source is not on the classpath"
        {:id "W5-TC-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "W5-TC-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private engine-source
  "bootstrap/gravity/src/gravity/self_hosting/w5_tooling_conformance_verifier.gravity")
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/w5-tooling-conformance")

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
    (fixture-path "accepted" "tooling-conformance" ".gravity"))))
(def ^:private accepted-qst-plan
  (delay
   (compile-plan
    (fixture-path "accepted" "tooling-conformance" ".qst"))))
(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-path "rejected" "invalid-tooling-conformance" ".gravity"))))
(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (fixture-path "rejected" "invalid-tooling-conformance" ".qst"))))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-w5-tooling-conformance-verifier
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- request
  ([plan function]
   (invoke plan function []))
  ([plan function source-path extension source-kind]
   (invoke plan function [source-path extension source-kind])))

(defn- request-at [plan source-path extension source-kind]
  (invoke plan 'w5-tooling-conformance-request-at
          [source-path extension source-kind]))

(defn- verify [request-value]
  (invoke engine-plan 'w5-tooling-conformance-verify [request-value]))

(def ^:private rejected-cases
  {'w5-tooling-conformance-invalid-source-request "W5-TC-SOURCE"
   'w5-tooling-conformance-invalid-check-request "W5-TC-CHECK"
   'w5-tooling-conformance-invalid-replay-request "W5-TC-REPLAY"
   'w5-tooling-conformance-invalid-narrative-request "W5-TC-NARRATIVE"
   'w5-tooling-conformance-invalid-suite-request "W5-TC-SUITE"
   'w5-tooling-conformance-invalid-provenance-request "W5-TC-PROVENANCE"
   'w5-tooling-conformance-invalid-authority-request "W5-TC-AUTHORITY"
   'w5-tooling-conformance-source-substitution-request "W5-TC-SOURCE"
   'w5-tooling-conformance-check-substitution-request "W5-TC-CHECK"
   'w5-tooling-conformance-replay-substitution-request "W5-TC-REPLAY"
   'w5-tooling-conformance-narrative-substitution-request
   "W5-TC-NARRATIVE"
   'w5-tc-invalid-source-request "W5-TC-SOURCE"
   'w5-tc-invalid-check-request "W5-TC-CHECK"
   'w5-tc-invalid-replay-request "W5-TC-REPLAY"
   'w5-tc-invalid-narrative-request "W5-TC-NARRATIVE"})

(def ^:private span-rejected-cases
  {'w5-tooling-conformance-invalid-source-span-missing-request
   "W5-TC-SOURCE"
   'w5-tooling-conformance-invalid-source-span-extra-request
   "W5-TC-SOURCE"
   'w5-tooling-conformance-invalid-source-span-wrong-type-request
   "W5-TC-SOURCE"
   'w5-tooling-conformance-invalid-source-span-negative-request
   "W5-TC-SOURCE"
   'w5-tooling-conformance-invalid-source-span-order-request
   "W5-TC-SOURCE"})

(def ^:private provenance-binding-rejected-cases
  {'w5-tooling-conformance-invalid-top-provenance-cross-kind-request
   "W5-TC-PROVENANCE"
   'w5-tooling-conformance-invalid-nested-source-provenance-cross-kind-request
   "W5-TC-PROVENANCE"})

(deftest w5-tooling-conformance-source-provenance-bindings-are-exact
  (doseq [[function expected-rule] provenance-binding-rejected-cases]
    (testing (str function)
      (doseq [[accepted-plan rejected-plan source-path extension source-kind]
              [[accepted-gravity-plan rejected-gravity-plan
                (fixture-path "accepted" "tooling-conformance" ".gravity")
                ".gravity" :gravity]
               [accepted-qst-plan rejected-qst-plan
                (fixture-path "accepted" "tooling-conformance" ".qst")
                ".qst" :qst]]]
        (let [base (request-at accepted-plan source-path extension source-kind)
              invalid (invoke rejected-plan function [base])
              result (verify invalid)
              diagnostic (first (:diagnostics result))]
          (is (= :rejected (:status result)))
          (is (= expected-rule (:rule diagnostic)))
          (is (= expected-rule (:diagnostic-id diagnostic)))
          (is (= :provenance (get-in diagnostic [:facts :reason])))
          (is (not= (get invalid :provenance)
                    (get-in invalid [:source :provenance]))))))))

(deftest w5-tooling-conformance-source-spans-are-exact-and-bounded
  (doseq [[function expected-rule] span-rejected-cases]
    (testing (str function)
      (doseq [[accepted-plan rejected-plan source-path extension source-kind]
              [[accepted-gravity-plan rejected-gravity-plan
                (fixture-path "accepted" "tooling-conformance" ".gravity")
                ".gravity" :gravity]
               [accepted-qst-plan rejected-qst-plan
                (fixture-path "accepted" "tooling-conformance" ".qst")
                ".qst" :qst]]]
        (let [base (request-at accepted-plan source-path extension source-kind)
              invalid (invoke rejected-plan function [base])
              result (verify invalid)
              diagnostic (first (:diagnostics result))]
          (is (= :rejected (:status result)))
          (is (= expected-rule (:rule diagnostic)))
          (is (= expected-rule (:diagnostic-id diagnostic)))
          (is (= :tooling-conformance (:stage diagnostic)))
          (is (= :meta (:profile diagnostic)))
          (is (= :llvm-x86_64-linux (:target diagnostic))))))))

(deftest w5-tooling-conformance-engine-and-fixtures-compile
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (doseq [function
          '[w5-tooling-conformance-policy
            w5-tooling-conformance-diagnostic-catalog
            w5-tooling-conformance-target-valid?
            w5-tooling-conformance-source-valid?
            w5-tooling-conformance-check-valid?
            w5-tooling-conformance-replay-valid?
            w5-tooling-conformance-narrative-valid?
            w5-tooling-conformance-suite-valid?
            w5-tooling-conformance-request-valid?
            w5-tc-path-ends-with?
            w5-tc-source-provenance-bindings-valid?
            w5-tooling-conformance-input
            w5-tooling-conformance-record
            w5-tooling-conformance-diagnostic
            w5-tooling-conformance-verify
            w5-tooling-conformance-execute
            w5-tooling-conformance-run
            w5-tooling-conformance-recompute
            w5-tooling-conformance-verify-result
            w5-tc-policy w5-tc-diagnostic-catalog w5-tc-target-valid?
            w5-tc-request-valid? w5-tc-verify w5-tc-execute w5-tc-run
            w5-tc-recompute w5-tc-verify-result]]
    (is (map? (get (get @engine-plan :functions) function)) function))
  (doseq [[family basename]
          [["accepted" "tooling-conformance"]
           ["rejected" "invalid-tooling-conformance"]]]
    (is (= (slurp (path (fixture-path family basename ".gravity")))
           (slurp (path (fixture-path family basename ".qst")))))))

(deftest w5-tooling-conformance-path-suffix-helper-binds-extension
  (is (true? (invoke engine-plan 'w5-tc-path-ends-with?
                           [".gravity" ".gravity"])))
  (is (true? (invoke engine-plan 'w5-tc-path-ends-with?
                           ["x.qst" ".qst"])))
  (is (false? (invoke engine-plan 'w5-tc-path-ends-with?
                            ["x.qst.tmp" ".qst"])))
  (is (false? (invoke engine-plan 'w5-tc-path-ends-with?
                            ["x" ".qst"]))))

(deftest w5-tooling-conformance-policy-is-static-and-nonauthority
  (let [policy (request engine-plan 'w5-tooling-conformance-policy)
        catalog (request engine-plan
                         'w5-tooling-conformance-diagnostic-catalog)]
    (is (= :gravity/w5-tooling-conformance-policy (:artifact policy)))
    (is (= :wave6-slice-c-static-only (:scope policy)))
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
    (is (false? (:public-binary? policy)))
    (is (false? (:plugin-authority? policy)))
    (is (false? (:package-authority? policy)))
    (is (false? (:backend-conformance? policy)))
    (is (false? (:matrix-240-credit? policy)))
    (is (= :incomplete (:completion-status policy)))
    (is (some #{"W5-TC-SOURCE"} (:rules catalog)))
    (is (some #{"W5-TC-SUBSTITUTION"} (:rules catalog)))
    (doseq [record (:unsupported-target-policies policy)]
      (is (false? (:invokes-clojure? record)))
      (is (false? (:links-jvm? record)))
      (is (false? (:fallback? record))))))

(deftest w5-tooling-conformance-accepted-record-is-incomplete
  (doseq [[plan source-path extension source-kind]
          [[accepted-gravity-plan
            (fixture-path "accepted" "tooling-conformance" ".gravity")
            ".gravity" :gravity]
           [accepted-qst-plan
            (fixture-path "accepted" "tooling-conformance" ".qst")
            ".qst" :qst]]]
    (let [request-value
          (request-at plan source-path extension source-kind)
          result (verify request-value)]
      (is (= :incomplete (:claimed-status request-value)))
      (is (= :accepted (:status result)))
      (is (= :incomplete (:completion-status result)))
      (is (= :blocked (get (get result :verifier-gate) :decision)))
      (is (false? (get (get result :verifier-gate) :release-eligible?)))
      (is (= [:source :check :replay :narrative :suite]
             (:tool-order result)))
      (is (= :accepted (:source-status result)))
      (is (= :accepted (:check-status result)))
      (is (= :pending (:replay-status result)))
      (is (= :not-claimed (:performance-status result)))
      (is (empty? (:diagnostics result)))
      (is (true? (:clojure-seed-boundary? result)))
      (is (false? (:self-hosted? result)))
      (is (false? (:release? result)))
      (is (false? (:public-authority? result)))
      (is (false? (:backend-conformance? result)))
      (is (true? (:fail-closed? result))))))

(deftest w5-tooling-conformance-provenance-identifies-source-kind
  (let [gravity (request-at accepted-gravity-plan
                            (fixture-path "accepted" "tooling-conformance"
                                          ".gravity")
                            ".gravity" :gravity)
        qst (request-at accepted-qst-plan
                        (fixture-path "accepted" "tooling-conformance" ".qst")
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

(deftest w5-tooling-conformance-identity-is-path-neutral
  (let [left-request
        (request accepted-gravity-plan
                 'w5-tooling-conformance-request
                 "/checkout-a/bootstrap/tooling-conformance.gravity"
                 ".gravity" :gravity)
        right-request
        (request accepted-gravity-plan
                 'w5-tooling-conformance-alternate-path-request
                 "/checkout-b/bootstrap/tooling-conformance.gravity"
                 ".gravity" :gravity)
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

(deftest w5-tooling-conformance-path-suffix-substitution-is-rejected
  (doseq [[accepted-plan rejected-plan source-path extension source-kind function expected-suffix]
          [[accepted-gravity-plan rejected-gravity-plan
            (fixture-path "accepted" "tooling-conformance" ".gravity")
            ".gravity" :gravity
            'w5-tooling-conformance-invalid-gravity-path-suffix-request ".qst"]
           [accepted-qst-plan rejected-qst-plan
            (fixture-path "accepted" "tooling-conformance" ".qst")
            ".qst" :qst
            'w5-tooling-conformance-invalid-qst-path-suffix-request ".gravity"]]]
    (let [base (request-at accepted-plan source-path extension source-kind)
          invalid (invoke rejected-plan function [base])
          result (verify invalid)
          diagnostic (first (:diagnostics result))]
      (is (= :rejected (:status result)))
      (is (= "W5-TC-PROVENANCE" (:rule diagnostic)))
      (is (= "W5-TC-PROVENANCE" (:diagnostic-id diagnostic)))
      (is (= :provenance (get-in diagnostic [:facts :reason])))
      (is (= extension (get-in invalid [:provenance :actual-source-extension])))
      (is (= source-kind (get-in invalid [:provenance :source-kind])))
      (is (re-find (re-pattern (str (java.util.regex.Pattern/quote expected-suffix) "$"))
                   (get-in invalid [:provenance :actual-source-path]))))))

(deftest w5-tooling-conformance-rejected-families-are-stable
  (doseq [[function-name expected-rule] rejected-cases]
    (testing (str function-name)
      (doseq [[accepted-plan rejected-plan source-path extension source-kind]
              [[accepted-gravity-plan rejected-gravity-plan
                (fixture-path "accepted" "tooling-conformance" ".gravity")
                ".gravity" :gravity]
               [accepted-qst-plan rejected-qst-plan
                (fixture-path "accepted" "tooling-conformance" ".qst")
                ".qst" :qst]]]
        (let [base (request-at accepted-plan source-path extension source-kind)
              invalid (invoke rejected-plan function-name [base])
              result (verify invalid)
              diagnostic (first (:diagnostics result))]
          (is (= base (request-at rejected-plan source-path extension source-kind)))
          (is (= :rejected (:status result)))
          (is (= 1 (count (:diagnostics result))))
          (is (= expected-rule (:rule diagnostic)))
          (is (= expected-rule (:diagnostic-id diagnostic)))
          (is (= :tooling-conformance (:stage diagnostic)))
          (is (= :meta (:profile diagnostic)))
          (is (= :llvm-x86_64-linux (:target diagnostic)))
          (is (map? (:source-span diagnostic)))
          (is (map? (:provenance diagnostic)))
          (is (keyword? (:remediation diagnostic)))
          (is (= :non-authority (:authority result)))
          (is (true? (:clojure-seed-boundary? result)))
          (is (false? (:self-hosted? result)))
          (is (false? (:release? result))))))))

(deftest w5-tooling-conformance-result-verifier-recomputes
  (let [request-value
        (request-at accepted-gravity-plan
                    (fixture-path "accepted" "tooling-conformance"
                                  ".gravity")
                    ".gravity" :gravity)
        result (verify request-value)
        verification
        (invoke engine-plan 'w5-tooling-conformance-verify-result
                [request-value result])
        altered (assoc result :completion-status :complete)
        altered-verification
        (invoke engine-plan 'w5-tooling-conformance-verify-result
                [request-value altered])]
    (is (= :passed (:status verification)))
    (is (true? (:recomputed verification)))
    (is (= :incomplete (:completion-status verification)))
    (is (= :rejected (:status altered-verification)))
    (is (= "W5-TC-SUBSTITUTION"
           (get (first (:diagnostics altered-verification)) :rule)))
    (is (= :non-authority (:authority altered-verification)))
    (is (true? (:clojure-seed-boundary? altered-verification)))))
