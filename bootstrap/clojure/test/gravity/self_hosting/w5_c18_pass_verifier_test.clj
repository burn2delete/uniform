(ns gravity.self-hosting.w5-c18-pass-verifier-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/w5_c18_pass_verifier_test.clj")]
    (when-not resource
      (throw
       (ex-info "W5 C18 test source is not on the classpath"
                {:id "W5-C18-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "W5-C18-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path [relative]
  (str (.resolve @root relative)))

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/w5-c18")

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
    "bootstrap/gravity/src/gravity/compiler/w5_c18_pass_verifier.gravity")))

(def ^:private accepted-gravity-plan
  (delay
   (compile-plan
    (fixture-path "accepted" "pass-verification" ".gravity"))))

(def ^:private accepted-qst-plan
  (delay
   (compile-plan
    (fixture-path "accepted" "pass-verification" ".qst"))))

(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-path "rejected" "invalid-pass-verification" ".gravity"))))

(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (fixture-path "rejected" "invalid-pass-verification" ".qst"))))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-w5-c18-pass-verifier-leaf
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- invoke-engine [function arguments]
  (invoke engine-plan function arguments))

(defn- request [plan function actual-source-path]
  (invoke plan function [actual-source-path]))

(defn- verify [request]
  (invoke-engine 'w5-c18-verify [request]))

(def ^:private rejected-cases
  {'w5-c18-invalid-risk
   ["C18-RISK" :risk-class-required]
   'w5-c18-invalid-evidence
   ["C18-EVIDENCE" :stale-evidence]
   'w5-c18-invalid-validation
   ["C18-VALIDATION" :translation-validation-rejected]
   'w5-c18-invalid-proof
   ["C18-PROOF" :stale-proof-or-certificate]
   'w5-c18-invalid-trust-report
   ["C18-TRUST-REPORT" :trust-report-recomputed-mismatch]
   'w5-c18-invalid-release-gate
   ["C18-RELEASE-GATE" :release-gate-recomputed-mismatch]
   'w5-c18-invalid-counterexample
   ["C18-COUNTEREXAMPLE" :source-fixture-required]
   'w5-c18-invalid-plugin
   ["C18-PLUGIN" :ambient-authority-rejected]
   'w5-c18-invalid-backend
   ["C18-BACKEND" :mir-intent-mismatch]
   'w5-c18-invalid-source-span
   ["C18-RISK" :record-provenance-or-origin-invalid]
   'w5-c18-invalid-provenance
   ["C18-RISK" :record-provenance-or-origin-invalid]
   'w5-c18-invalid-origin-chain
   ["C18-RISK" :record-provenance-or-origin-invalid]
   'w5-c18-invalid-translation-link
   ["C18-VALIDATION" :validation-pass-version-mismatch]
   'w5-c18-invalid-proof-link
   ["C18-PROOF" :proof-input-mismatch]
   'w5-c18-orphan-evidence
   ["C18-EVIDENCE" :orphan-evidence-pass]
   'w5-c18-duplicate-evidence
   ["C18-EVIDENCE" :duplicate-or-missing-evidence-id]
   'w5-c18-invalid-backend-link
   ["C18-BACKEND" :backend-candidate-target-mismatch]
   'w5-c18-invalid-pass-chain
   ["C18-RISK" :pass-artifact-chain-mismatch]
   'w5-c18-invalid-validation-proof-reference
   ["C18-VALIDATION" :validation-proof-reference-mismatch]
   'w5-c18-coherent-evidence-owner-substitution
   ["C18-EVIDENCE" :evidence-id-owner-mismatch]
   'w5-c18-coherent-validation-owner-permutation
   ["C18-VALIDATION" :validation-id-owner-mismatch]
   'w5-c18-coherent-proof-owner-permutation
   ["C18-PROOF" :proof-id-owner-mismatch]
   'w5-c18-coherent-certificate-owner-permutation
   ["C18-PROOF" :certificate-id-owner-mismatch]
   'w5-c18-empty-validation-proof-references
   ["C18-VALIDATION" :validation-proof-reference-mismatch]
   'w5-c18-empty-validation-certificate-references
   ["C18-VALIDATION" :validation-certificate-reference-mismatch]
   'w5-c18-substituted-validation-certificate-reference
   ["C18-VALIDATION" :validation-certificate-reference-mismatch]
   'w5-c18-coherent-translation-hash-substitution
   ["C18-RISK" :input-content-hash-required]
   'w5-c18-coherent-proof-hash-substitution
   ["C18-RISK" :proof-content-hash-required]
   'w5-c18-added-backend-artifact
   ["C18-BACKEND" :emitted-artifact-cardinality-mismatch]})

(deftest w5-c18-engine-and-co-canonical-fixtures-compile
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (doseq [function
          '[w5-c18-policy
            w5-c18-build-counterexample
            w5-c18-build-trust-report
            w5-c18-build-release-gate
            w5-c18-verify
            w5-c18-verify-result]]
    (is (map? (get-in @engine-plan [:functions function])) function))
  (is (= (slurp (path (fixture-path "accepted" "pass-verification" ".gravity")))
         (slurp (path (fixture-path "accepted" "pass-verification" ".qst")))))
  (is (= (slurp (path (fixture-path "rejected" "invalid-pass-verification" ".gravity")))
         (slurp (path (fixture-path "rejected" "invalid-pass-verification" ".qst"))))))

(deftest w5-c18-policy-is-meta-bounded-and-nonauthority
  (let [policy (invoke-engine 'w5-c18-policy [])]
    (is (= :gravity/w5-c18-pass-verifier-policy (:artifact policy)))
    (is (= :meta (:profile policy)))
    (is (= :llvm-x86_64-linux (:target policy)))
    (is (= :jvm (:stage2-target policy)))
    (is (= {:os :linux :arch :x86_64 :backend :llvm
            :artifact-format :elf :abi :sysv-amd64}
           (:candidate-platform policy)))
    (is (= [:darwin :darwin-arm64 :darwin-x86_64 :windows]
           (:unsupported-targets policy)))
    (is (= #{:low :medium :high :critical} (:risk-levels policy)))
    (is (= #{:translation-validation :property-test}
           (get-in policy [:minimum-evidence :high])))
    (is (contains? (:diagnostics policy) "C18-RELEASE-GATE"))
    (is (= :non-authority (:authority policy)))
    (is (true? (:clojure-seed-boundary? policy)))
    (is (false? (:self-hosted? policy)))
    (is (false? (:release? policy)))))

(deftest w5-c18-accepts-every-risk-and-evidence-class-but-blocks-release
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [request (request plan 'w5-c18-verification-request
                          (if (= plan accepted-gravity-plan)
                            "/checkout-a/w5-c18/pass-verification.gravity"
                            "/checkout-a/w5-c18/pass-verification.qst"))
          result (verify request)
          trust (:trust-report result)
          gate (:release-gate result)]
      (is (= :accepted (:status result)))
      (is (empty? (:diagnostics result)))
      (is (= 4 (count (:passes request))))
      (is (= 8 (count (:evidence request))))
      (is (= [[:translation-validation :checker/c18-translation]
              [:translation-validation :checker/c18-translation]]
             (mapv (juxt :kind :checker)
                   (:translation-validations request))))
      (is (= [["proof:optimizer-property"]
              ["proof:lowerer-certificate"]]
             (mapv :proofs (:translation-validations request))))
      (is (= [["certificate:optimizer-property"]
              ["certificate:lowerer"]]
             (mapv :certificates (:translation-validations request))))
      (is (= [[:property-proof :property-certificate
               :checker/c18-property]
              [:checked-certificate-proof :checked-certificate
               :checker/c18-certificate]]
             (mapv (juxt :kind :certificate-kind
                         :certificate-checker)
                   (:proofs request))))
      (is (every? #(= :unverified (:hash-verification %))
                  (:passes request)))
      (is (every? #(false? (:cryptographic-hash-verified? %))
                  (:passes request)))
      (is (= 1 (count (get-in request [:backends 0 :emitted-artifacts]))))
      (is (= [(get-in request [:passes 3 :output-ir])]
             (mapv :artifact-id
                   (get-in request [:backends 0 :emitted-artifacts]))))
      (is (= :gravity/w5-c18-trust-report (:artifact trust)))
      (is (= 4 (count (:passes trust))))
      (is (= :blocked (:decision gate)))
      (is (false? (:release-eligible? gate)))
      (is (= :pending (:self-hosting-comparison gate)))
      (is (if (= plan accepted-gravity-plan)
            (str/ends-with?
             (get-in result [:provenance :actual-source-path]) ".gravity")
            (str/ends-with?
             (get-in result [:provenance :actual-source-path]) ".qst")))
      (is (true? (:clojure-seed-boundary? result)))
      (is (false? (:self-hosted? result)))
      (is (false? (:release? result)))
      (is (false? (:independent-review? result)))
      (is (= :non-authority (:authority result)))
      (is (some #{:clojure-seed-boundary}
                (keys (:residual-gaps result))))
      (is (some #{:jvm-execution} (keys (:residual-gaps result))))
      (is (some #{:independent-review} (keys (:residual-gaps result))))
      (is (= :passed
             (:status
              (invoke-engine 'w5-c18-verify-result [request result])))))))

(deftest w5-c18-identity-is-path-neutral-and-provenance-is-real
  (let [left-request
        (request accepted-gravity-plan
                 'w5-c18-verification-request
                 "/checkout-a/w5-c18/pass-verification.gravity")
        right-request
        (request accepted-gravity-plan
                 'w5-c18-verification-alternate-path-request
                 "/checkout-b/w5-c18/pass-verification.qst")
        left (verify left-request)
        right (verify right-request)]
    (is (= :accepted (:status left)))
    (is (= :accepted (:status right)))
    (is (= (:identity-input left) (:identity-input right)))
    (is (not= (:provenance left) (:provenance right)))
    (is (not= (get-in left [:provenance :actual-source-path])
              (get-in right [:provenance :actual-source-path])))
    (is (not-any?
         #(re-find #"/checkout|/secret|/different|/opt" (str %))
         (tree-seq coll? seq (:identity-input left))))
    (is (re-find #"/checkout-a/"
                 (get-in left [:provenance :actual-source-path])))
    (is (re-find #"/checkout-b/"
                 (get-in right [:provenance :actual-source-path])))))

(deftest w5-c18-rejects-every-stable-negative-family
  (doseq [[function [rule reason]] rejected-cases]
    (testing (str function)
      (doseq [[accepted-plan rejected-plan]
              [[accepted-gravity-plan rejected-gravity-plan]
               [accepted-qst-plan rejected-qst-plan]]]
        (let [base (request accepted-plan 'w5-c18-verification-request
                            (if (= accepted-plan accepted-gravity-plan)
                              "/checkout-a/w5-c18/pass-verification.gravity"
                              "/checkout-a/w5-c18/pass-verification.qst"))
              invalid (invoke rejected-plan function [base])
              result (verify invalid)
              diagnostic (first (:diagnostics result))]
          (is (= :rejected (:status result)))
          (is (= rule (:rule diagnostic)))
          (is (= reason (:remediation diagnostic)))
          (is (contains? diagnostic :pass-id))
          (is (contains? diagnostic :pass-version))
          (is (contains? diagnostic :risk))
          (is (contains? diagnostic :affected-profiles))
          (is (contains? diagnostic :affected-targets))
          (is (contains? diagnostic :source-span))
          (is (keyword? (:remediation diagnostic)))
          (is (true? (:clojure-seed-boundary? result)))
          (is (false? (:self-hosted? result)))
          (is (false? (:release? result)))))
      )))

(deftest w5-c18-recomputes-after-mutation-and-rejects-substitution
  (let [request (request accepted-gravity-plan 'w5-c18-verification-request
                        "/checkout-a/w5-c18/pass-verification.gravity")
        result (verify request)
        substituted (assoc-in result [:release-gate :decision] :accepted)
        substitution-check
        (invoke-engine 'w5-c18-verify-result [request substituted])
        semantic-mutation
        (assoc-in request [:passes 2 :output-ir] :substituted-mir)
        mutation-check
        (invoke-engine 'w5-c18-verify-result [semantic-mutation result])
        producer-flags
        (assoc result :clojure-seed-boundary? false
               :self-hosted? true :release? true)
        producer-check
        (invoke-engine 'w5-c18-verify-result [request producer-flags])
        result-substitutions
        [[(assoc result :unexpected-field :forbidden)
          :verification-result-keyset-mismatch]
         [(dissoc result :verification-id)
          :verification-result-keyset-mismatch]
         [(assoc result :artifact :gravity/substituted-result)
          :verification-result-substitution]
         [(assoc result :status :rejected)
          :verification-result-keyset-mismatch]
         [(assoc result :diagnostics [:substituted])
          :verification-result-substitution]
         [(assoc result :completion :complete)
          :verification-result-substitution]
         [(assoc result :authority :release-authority)
          :verification-result-substitution]
         [(assoc result :residual-gaps {})
          :verification-result-substitution]]]
    (is (= :passed
           (:status (invoke-engine 'w5-c18-verify-result [request result]))))
    (is (= :rejected (:status substitution-check)))
    (is (= "C18-RELEASE-GATE"
           (get-in substitution-check [:diagnostics 0 :rule])))
    (is (= :rejected (:status mutation-check)))
    (is (= "C18-RISK"
           (get-in mutation-check [:diagnostics 0 :rule])))
    (is (= :rejected (:status producer-check)))
    (is (= "C18-RELEASE-GATE"
           (get-in producer-check [:diagnostics 0 :rule])))
    (doseq [[candidate expected-reason] result-substitutions]
      (let [check
            (invoke-engine 'w5-c18-verify-result [request candidate])]
        (is (= :rejected (:status check)))
        (is (= "C18-RELEASE-GATE"
               (get-in check [:diagnostics 0 :rule])))
        (is (= expected-reason
               (get-in check [:diagnostics 0 :remediation])))))))
