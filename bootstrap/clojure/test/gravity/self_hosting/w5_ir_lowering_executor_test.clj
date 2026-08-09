(ns gravity.self-hosting.w5-ir-lowering-executor-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/w5_ir_lowering_executor_test.clj")]
    (when-not resource
      (throw
       (ex-info "W5 IR lowering test source is not on the classpath"
                {:id "W5-IR-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "W5-IR-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/w5-ir-lowering")

(defn- path [relative]
  (str (.resolve @root relative)))

(defn- fixture-path [family basename extension]
  (str fixture-root "/" family "/" basename extension))

(defn- compile-plan [relative-path]
  (let [source-path (path relative-path)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private engine-plan
  (delay
   (compile-plan
    "bootstrap/gravity/src/gravity/self_hosting/w5_ir_lowering_executor.gravity")))
(def ^:private accepted-gravity-plan
  (delay
   (compile-plan
    (fixture-path "accepted" "ir-lowering-execution" ".gravity"))))
(def ^:private accepted-qst-plan
  (delay
   (compile-plan
    (fixture-path "accepted" "ir-lowering-execution" ".qst"))))
(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-path "rejected" "invalid-ir-lowering-execution" ".gravity"))))
(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (fixture-path "rejected" "invalid-ir-lowering-execution" ".qst"))))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-w5-ir-lowering-executor
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- invoke-engine [function arguments]
  (invoke engine-plan function arguments))

(defn- request [plan extension kind]
  (invoke plan 'w5-ir-lowering-request
          [(path (fixture-path "accepted" "ir-lowering-execution" extension))
           extension kind]))

(deftest w5-ir-engine-and-co-canonical-fixtures-compile
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (doseq [function
          '[w5-ir-lowering-policy
            w5-ir-c11-mir-schema
            w5-ir-c12-domain-schema
            w5-ir-c13-pass-contract
            w5-ir-c14-lowering-policy
            w5-ir-c15-diagnostic-catalog
            w5-ir-path-ends-with?
            w5-ir-mir-valid?
            w5-ir-domain-valid?
            w5-ir-pass-valid?
            w5-ir-optimization-valid?
            w5-ir-lowering-valid?
            w5-ir-stage10a-valid?
            w5-ir-diagnostics-valid?
            w5-ir-request-valid?
            w5-ir-identity-input
            w5-ir-provenance
            w5-ir-lowering-execute
            w5-ir-execute
            w5-ir-run
            w5-ir-verify
            w5-ir-verify-result
            w5-ir-recompute]]
    (is (map? (get (get @engine-plan :functions) function)) function))
  (is (= (slurp (path (fixture-path "accepted" "ir-lowering-execution" ".gravity")))
         (slurp (path (fixture-path "accepted" "ir-lowering-execution" ".qst")))))
  (is (= (slurp (path (fixture-path "rejected" "invalid-ir-lowering-execution" ".gravity")))
         (slurp (path (fixture-path "rejected" "invalid-ir-lowering-execution" ".qst"))))))

(deftest w5-ir-policy-freezes-target-and-residual-authority
  (let [policy (invoke-engine 'w5-ir-lowering-policy [])
        unsupported (:unsupported-target-policies policy)]
    (is (= :meta (:profile policy)))
    (is (= :jvm (:stage2-target policy)))
    (is (= {:backend :llvm :os :linux :arch :x86_64
            :artifact-format :elf :abi :sysv-amd64
            :triple "x86_64-unknown-linux-gnu"}
           (:candidate-target policy)))
    (is (= [:darwin :darwin-arm64 :darwin-x86_64 :windows]
           (:unsupported-targets policy)))
    (doseq [entry unsupported]
      (is (= :unsupported (:support entry)))
      (is (false? (:invokes-clojure? entry)))
      (is (false? (:links-jvm? entry)))
      (is (false? (:fallback? entry))))
    (is (true? (:clojure-seed-boundary? policy)))
    (is (false? (:self-hosted? policy)))
    (is (false? (:public-authority? policy)))
    (is (false? (:release? policy)))
    (is (= :non-authority (:authority policy)))
    (is (false? (:stage10a-executable-continuity? policy)))))

(deftest w5-ir-accepted-request-is-incomplete-and-nonauthority
  (doseq [[plan extension kind]
          [[accepted-gravity-plan ".gravity" :gravity]
           [accepted-qst-plan ".qst" :qst]]]
    (let [value (request plan extension kind)
          result (invoke-engine 'w5-ir-verify [value])]
      (is (true? (invoke-engine 'w5-ir-request-valid? [value])))
      (is (= :accepted (:status result)))
      (is (= :incomplete (:execution-status result)))
      (is (= :blocked (:closure-status result)))
      (is (= :non-authority (:authority result)))
      (is (false? (:executable-load? result)))
      (is (= :evidence-only-rejected (:stage10a-status result)))
      (is (false? (:full-language-credit? result)))
      (is (true? (:clojure-seed-boundary? result)))
      (is (false? (:self-hosted? result)))
      (is (false? (:public? result)))
      (is (false? (:release? result)))
      (is (= extension (get (:provenance result) :source-extension)))
      (is (= kind (get (:provenance result) :source-kind)))
      (is (= :passed
             (:status
              (invoke-engine 'w5-ir-verify-result [value result])))))))

(deftest w5-ir-fixture-source-kind-is-parameterized
  (doseq [[plan extension kind]
          [[accepted-gravity-plan ".gravity" :gravity]
           [accepted-qst-plan ".qst" :qst]
           [rejected-gravity-plan ".gravity" :gravity]
           [rejected-qst-plan ".qst" :qst]]]
    (let [source-path (str "/checkout-kind/bootstrap/clojure/fixtures/self-hosting/w5-ir-lowering/accepted/ir-lowering-execution" extension)
          value (invoke plan 'w5-ir-lowering-request-for-source
                        [source-path extension kind])
          result (invoke-engine 'w5-ir-verify [value])]
      (is (= source-path
             (get (:provenance result) :actual-source-path)))
      (is (= extension (get (:provenance result) :source-extension)))
      (is (= kind (get (:provenance result) :source-kind)))
      (is (= :accepted (:status result))))))

(deftest w5-ir-rejects-path-suffix-substitution
  (doseq [[accepted-plan rejected-plan extension kind mutator wrong-suffix]
          [[accepted-gravity-plan rejected-gravity-plan ".gravity" :gravity
            'w5-ir-invalid-gravity-path-suffix #"\.qst$"]
           [accepted-qst-plan rejected-qst-plan ".qst" :qst
            'w5-ir-invalid-qst-path-suffix #"\.gravity$"]]]
    (let [base (request accepted-plan extension kind)
          invalid (invoke rejected-plan mutator [base])
          result (invoke-engine 'w5-ir-verify [invalid])
          diagnostic (first (:diagnostics result))
          provenance (:provenance invalid)]
      (is (re-find wrong-suffix
                   (get provenance :actual-source-path)))
      (is (= extension (get provenance :source-extension)))
      (is (= kind (get provenance :source-kind)))
      (is (= :rejected (:status result)))
      (is (= "C15-ORIGIN" (:rule diagnostic)))
      (is (= "C15-ORIGIN" (:diagnostic-id diagnostic)))
      (is (= :provenance (get (:facts diagnostic) :field)))
      (is (= :error (:severity diagnostic)))
      (is (= :non-authority (:authority result))))))

(deftest w5-ir-path-suffix-helper-is-exact
  (is (true? (invoke-engine 'w5-ir-path-ends-with?
                            ["x.qst" ".qst"])))
  (is (true? (invoke-engine 'w5-ir-path-ends-with?
                            ["x.gravity" ".gravity"])))
  (is (false? (invoke-engine 'w5-ir-path-ends-with?
                             ["x.qst" ".gravity"])))
  (is (false? (invoke-engine 'w5-ir-path-ends-with?
                             ["x.gravity.qst" ".gravity"]))))

(deftest w5-ir-identity-is-path-neutral-and-provenance-is-path-bearing
  (let [left (request accepted-gravity-plan ".gravity" :gravity)
        right (invoke accepted-gravity-plan
                      'w5-ir-lowering-alternate-path-request
                      ["/checkout-b/bootstrap/clojure/fixtures/self-hosting/w5-ir-lowering/accepted/ir-lowering-execution.gravity"
                       ".gravity" :gravity])
        left-result (invoke-engine 'w5-ir-verify [left])
        right-result (invoke-engine 'w5-ir-verify [right])]
    (is (= (:identity-input left-result) (:identity-input right-result)))
    (is (not= (:provenance left-result) (:provenance right-result)))
    (is (not-any? #(re-find #"/checkout|/secret|/different|/opt" (str %))
                  (tree-seq coll? seq (:identity-input left-result))))
    (is (re-find #"/checkout-a/"
                 (:actual-source-path (:provenance left-result))))
    (is (re-find #"/checkout-b/"
                 (:actual-source-path (:provenance right-result))))))

(def ^:private rejected-cases
  {'w5-ir-invalid-artifact ["W5-IR-SCHEMA" :request]
   'w5-ir-invalid-schema ["W5-IR-SCHEMA" :request]
   'w5-ir-invalid-profile ["W5-IR-SCHEMA" :request]
   'w5-ir-invalid-mir ["C11-VERIFY" :mir]
   'w5-ir-invalid-domain ["C12-VERIFY" :domain-ir]
   'w5-ir-invalid-intent ["C13-CONTRACT" :intent]
   'w5-ir-invalid-invalidation ["C13-INVALIDATE" :regenerated-facts]
   'w5-ir-invalid-proof ["C13-PROOF" :proofs-used]
   'w5-ir-invalid-lowering-target ["C14-INPUT" :lowering]
   'w5-ir-invalid-abi ["C14-INPUT" :lowering]
   'w5-ir-invalid-stage10a ["C14-INPUT" :stage10a]
   'w5-ir-invalid-diagnostic ["C15-SCHEMA" :diagnostics]})

(deftest w5-ir-rejects-each-contract-mutation-with-structured-diagnostic
  (doseq [[function [rule field]] rejected-cases]
    (testing (str function)
      (doseq [[accepted-plan rejected-plan extension kind]
              [[accepted-gravity-plan rejected-gravity-plan ".gravity" :gravity]
               [accepted-qst-plan rejected-qst-plan ".qst" :qst]]]
        (let [base (request accepted-plan extension kind)
              invalid (invoke rejected-plan function [base])
              result (invoke-engine 'w5-ir-verify [invalid])
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
          (is (false? (:executable-load? result)))
          (is (= :non-authority (:authority result))))))))

(deftest w5-ir-result-substitution-is-rejected
  (let [value (request accepted-gravity-plan ".gravity" :gravity)
        result (invoke-engine 'w5-ir-verify [value])
        substituted (invoke rejected-gravity-plan
                             'w5-ir-invalid-result [result])
        verification (invoke-engine 'w5-ir-verify-result
                                    [value substituted])]
    (is (= :rejected (:status verification)))
    (is (= "C15-GOLDEN" (:rule (first (:diagnostics verification)))))
    (is (= :non-authority (:authority result)))
    (is (false? (:executable-load? result)))))
