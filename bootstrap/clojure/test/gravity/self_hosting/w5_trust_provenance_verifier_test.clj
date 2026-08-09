(ns gravity.self-hosting.w5-trust-provenance-verifier-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

; Later JVM clearance command (intentionally not run in this change):
; clojure -M:test --namespace gravity.self-hosting.w5-trust-provenance-verifier-test

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/w5_trust_provenance_verifier_test.clj")]
    (when-not resource
      (throw
       (ex-info "W5 trust/provenance test source is not on the classpath"
                {:id "W5-TRUST-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "W5-TRUST-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private engine-source
  "bootstrap/gravity/src/gravity/self_hosting/w5_trust_provenance_verifier.gravity")
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/w5-trust")

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
    (fixture-path "accepted" "incomplete-trust-closure" ".gravity"))))
(def ^:private accepted-qst-plan
  (delay
   (compile-plan
    (fixture-path "accepted" "incomplete-trust-closure" ".qst"))))
(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-path "rejected" "invalid-trust-closure" ".gravity"))))
(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (fixture-path "rejected" "invalid-trust-closure" ".qst"))))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-w5-trust-provenance-verifier
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- request [plan function]
  (invoke plan function []))

(defn- verify [request-value]
  (invoke engine-plan 'w5-trust-provenance-verify [request-value]))

(def ^:private rejected-cases
  {'w5-trust-invalid-upstream-request ["W5-TRUST-UPSTREAM" :upstream-ids]
   'w5-trust-invalid-span-request ["W5-TRUST-SCHEMA" :source-span]
   'w5-trust-invalid-source-span-order-request ["W5-TRUST-SBOM" :sbom]
   'w5-trust-invalid-provenance-request ["W5-TRUST-SCHEMA" :repository]
   'w5-trust-invalid-lineage-cycle-request ["W5-TRUST-LINEAGE" :lineage]
   'w5-trust-invalid-lineage-gap-request ["W5-TRUST-LINEAGE" :lineage]
   'w5-trust-invalid-lineage-unreachable-request
   ["W5-TRUST-LINEAGE" :lineage]
   'w5-trust-invalid-lineage-order-request
   ["W5-TRUST-LINEAGE" :lineage]
   'w5-trust-invalid-lineage-cross-binding-request
   ["W5-TRUST-RECIPE" :lineage]
   'w5-trust-invalid-r3-recipe-request
   ["W5-TRUST-RECIPE" :build-recipes]
   'w5-trust-invalid-dependency-request
   ["W5-TRUST-DEPENDENCY" :dependency-graphs]
   'w5-trust-invalid-lock-request ["W5-TRUST-LOCK" :locks]
   'w5-trust-invalid-environment-request
   ["W5-TRUST-ENVIRONMENT" :environments]
   'w5-trust-invalid-toolchain-request
   ["W5-TRUST-TOOLCHAIN" :toolchains]
   'w5-trust-invalid-recipe-request ["W5-TRUST-RECIPE" :lineage]
   'w5-trust-invalid-diversity-request
   ["W5-TRUST-DIVERSITY" :diverse-rebuilds]
   'w5-trust-invalid-diverse-recipe-switch-request
   ["W5-TRUST-DIVERSITY" :diverse-rebuilds]
   'w5-trust-invalid-diverse-coherent-recipe-swap-request
   ["W5-TRUST-DIVERSITY" :diverse-rebuilds]
   'w5-trust-invalid-anchor-request ["W5-TRUST-ANCHOR" :trust-anchors]
   'w5-trust-invalid-attestation-request
   ["W5-TRUST-ATTESTATION" :attestations]
   'w5-trust-invalid-attestation-subject-request
   ["W5-TRUST-ATTESTATION" :attestations]
   'w5-trust-invalid-attestation-other-builder-request
   ["W5-TRUST-ATTESTATION" :attestations]
   'w5-trust-invalid-sbom-request ["W5-TRUST-SBOM" :sbom]
   'w5-trust-invalid-signature-request ["W5-TRUST-SIGNATURE" :signatures]
   'w5-trust-invalid-signature-payload-request
   ["W5-TRUST-SIGNATURE" :signatures]
   'w5-trust-invalid-signature-other-recipe-request
   ["W5-TRUST-SIGNATURE" :signatures]
   'w5-trust-invalid-signature-other-signer-anchor-request
   ["W5-TRUST-SIGNATURE" :signatures]
   'w5-trust-invalid-revocation-request
   ["W5-TRUST-REVOCATION" :revocations]
   'w5-trust-invalid-revocation-subject-request
   ["W5-TRUST-REVOCATION" :revocations]
   'w5-trust-invalid-revocation-compiler-subject-request
   ["W5-TRUST-REVOCATION" :revocations]
   'w5-trust-invalid-revocation-provenance-subject-request
   ["W5-TRUST-REVOCATION" :revocations]
   'w5-trust-invalid-revocation-dropped-package-request
   ["W5-TRUST-REVOCATION" :revocations]
   'w5-trust-invalid-hash-status-request
   ["W5-TRUST-LINEAGE" :lineage]
   'w5-trust-invalid-unsafe-audit-request
   ["W5-TRUST-UNSAFE-AUDIT" :unsafe-audits]
   'w5-trust-invalid-tcb-request ["W5-TRUST-TCB" :tcb-delta]
   'w5-trust-invalid-tcb-components-request ["W5-TRUST-TCB" :tcb-delta]
   'w5-trust-invalid-equivalence-request
   ["W5-TRUST-EQUIVALENCE" :equivalence]
   'w5-trust-invalid-replay-request ["W5-TRUST-REPLAY" :replay]
   'w5-trust-invalid-replay-cross-binding-request
   ["W5-TRUST-REPLAY" :replay]
   'w5-trust-invalid-target-request ["W5-TRUST-TARGET" :target-scope]
   'w5-trust-invalid-cross-target-request
   ["W5-TRUST-TARGET" :target-scope]
   'w5-trust-invalid-fallback-request ["W5-TRUST-TARGET" :target-scope]
   'w5-trust-invalid-authority-request
   ["W5-TRUST-AUTHORITY" :authority]
   'w5-trust-invalid-review-request ["W5-TRUST-REVIEW" :review]})

(deftest w5-trust-engine-and-co-canonical-fixtures-compile
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (doseq [function
          '[w5-trust-provenance-policy
            w5-trust-provenance-diagnostic-catalog
            w5-trust-span?
            w5-trust-diagnostic-source-span
            w5-trust-target-valid?
            w5-trust-upstream-valid?
            w5-trust-lineage-valid?
            w5-trust-dependency-graphs-valid?
            w5-trust-locks-valid?
            w5-trust-environments-valid?
            w5-trust-toolchains-valid?
            w5-trust-build-recipes-valid?
            w5-trust-r3-recipe-cross-binding-valid?
            w5-trust-diverse-rebuilds-valid?
            w5-trust-anchors-valid?
            w5-trust-attestations-valid?
            w5-trust-sbom-valid?
            w5-trust-signature-subject-map
            w5-trust-signature-subject
            w5-trust-signatures-valid?
            w5-trust-revocations-valid?
            w5-trust-revocation-subjects-valid?
            w5-trust-unsafe-audits-valid?
            w5-trust-tcb-delta-valid?
            w5-trust-equivalence-valid?
            w5-trust-replay-valid?
            w5-trust-authority-valid?
            w5-trust-review-valid?
            w5-trust-claims-valid?
            w5-trust-request-valid?
            w5-trust-identity-input
            w5-trust-provenance
            w5-trust-provenance-verify
            w5-trust-provenance-execute
            w5-trust-provenance-run
            w5-trust-provenance-recompute
            w5-trust-structural-recompute
            w5-trust-verify-result]]
    (is (map? (get (get @engine-plan :functions) function)) function))
  (doseq [[family basename]
          [["accepted" "incomplete-trust-closure"]
           ["rejected" "invalid-trust-closure"]]]
    (is (= (slurp (path (fixture-path family basename ".gravity")))
           (slurp (path (fixture-path family basename ".qst")))))))

(deftest w5-trust-policy-is-target-exact-static-and-nonauthority
  (let [policy (invoke engine-plan 'w5-trust-provenance-policy [])
        target (:candidate-platform policy)]
    (is (= :gravity/w5-trust-provenance-policy (:artifact policy)))
    (is (= :wave3-static-only (:scope policy)))
    (is (= :jvm (:target policy)))
    (is (= :llvm-x86_64-linux (:candidate-target policy)))
    (is (= {:os :linux :arch :x86_64 :backend :llvm
            :artifact-format :elf :abi :sysv-amd64}
           target))
    (is (= [:darwin :darwin-arm64 :darwin-x86_64 :windows]
           (:unsupported-platforms policy)))
    (is (false? (:cross-target-inference? policy)))
    (is (false? (:darwin-fallback? policy)))
    (is (= :non-authority (:authority policy)))
    (is (true? (:static-only? policy)))
    (is (true? (:clojure-seed-boundary? policy)))
    (is (false? (:self-hosted? policy)))
    (is (false? (:release? policy)))
    (is (false? (:public-authority? policy)))
    (doseq [unsupported (:unsupported-platforms policy)]
      (let [record (first (filter #(= unsupported (:target %))
                                  (get (get (get (get @engine-plan :module)
                                                :metadata)
                                            :bootstrap)
                                       :unsupported-target-policies)))]
        (is (map? record) unsupported)
        (is (false? (:invokes-clojure? record)) unsupported)
        (is (false? (:links-jvm? record)) unsupported)
        (is (false? (:fallback? record)) unsupported)))))

(deftest w5-trust-accepted-record-is-incomplete-and-auditable
  (let [gravity-request
        (request accepted-gravity-plan
                 'w5-trust-provenance-request)
        qst-request
        (request accepted-qst-plan
                 'w5-trust-provenance-request)
        result (verify gravity-request)]
    (is (= gravity-request qst-request))
    (is (= :incomplete (:claimed-status gravity-request)))
    (is (= {:producer-asserted? true :narrative-only? true
            :source-ownership-only? false :check-only? false
            :container-evidence? true :closure-authority? false}
           (:claims gravity-request)))
    (is (= :accepted (:status result)))
    (is (= :incomplete (:trust-status result)))
    (is (= :incomplete (:closure-status result)))
    (is (= :structural-only (:recomputation-mode result)))
    (is (= :pending (get-in result [:hash-validation :status])))
    (is (= :pending (get-in result [:hash-validation :raw-byte-hashes])))
    (is (= :pending (get-in result [:hash-validation :canonical-byte-hashes])))
    (is (empty? (:diagnostics result)))
    (is (= :non-authority (:authority result)))
    (is (true? (:clojure-seed-boundary? result)))
    (is (false? (:self-hosted? result)))
    (is (false? (:release? result)))
    (is (false? (:public-authority? result)))
    (is (= [:clojure-stage0-seed :jvm-stage2-runtime :native-linux-replay
            :independent-trust-anchors :independent-sol-review
            :stage-equivalence :external-signing]
           (:residual-trusted-components result)))
    (is (some #{:native-linux-replay-pending} (:gaps result)))
    (is (some #{:independent-trust-anchors-pending} (:gaps result)))
    (is (some #{:actual-stage-equivalence-pending} (:gaps result)))
    (is (some #{:external-signing-pending} (:gaps result)))
    (is (= :passed
           (:status
            (invoke engine-plan 'w5-trust-verify-result
                    [gravity-request result]))))))

(deftest w5-trust-all-independent-static-checks-pass-before-open-gates
  (let [request-value
        (request accepted-gravity-plan 'w5-trust-provenance-request)]
    (is (true? (invoke engine-plan 'w5-trust-target-valid?
                        [(get request-value :target-scope)])))
    (is (true? (invoke engine-plan 'w5-trust-upstream-valid?
                        [(get request-value :upstream-ids)])))
    (is (true? (invoke engine-plan 'w5-trust-lineage-valid?
                        [request-value])))
    (is (true? (invoke engine-plan 'w5-trust-dependency-graphs-valid?
                        [request-value])))
    (is (true? (invoke engine-plan 'w5-trust-locks-valid? [request-value])))
    (is (true? (invoke engine-plan 'w5-trust-environments-valid?
                        [(get request-value :environments)])))
    (is (true? (invoke engine-plan 'w5-trust-toolchains-valid?
                        [(get request-value :toolchains)])))
    (is (true? (invoke engine-plan 'w5-trust-build-recipes-valid?
                        [request-value])))
    (is (true? (invoke engine-plan
                       'w5-trust-r3-recipe-cross-binding-valid?
                       [request-value])))
    (is (true? (invoke engine-plan 'w5-trust-diverse-rebuilds-valid?
                        [request-value])))
    (is (true? (invoke engine-plan 'w5-trust-anchors-valid?
                        [request-value])))
    (is (true? (invoke engine-plan 'w5-trust-attestations-valid?
                        [request-value])))
    (is (true? (invoke engine-plan 'w5-trust-sbom-valid?
                        [request-value])))
    (is (true? (invoke engine-plan 'w5-trust-signatures-valid?
                        [request-value])))
    (is (true? (invoke engine-plan 'w5-trust-revocations-valid?
                        [request-value])))
    (is (true? (invoke engine-plan
                       'w5-trust-revocation-subjects-valid?
                       [request-value])))
    (is (true? (invoke engine-plan 'w5-trust-unsafe-audits-valid?
                        [request-value])))
    (is (true? (invoke engine-plan 'w5-trust-tcb-delta-valid?
                        [request-value])))
    (is (true? (invoke engine-plan 'w5-trust-equivalence-valid?
                        [request-value])))
    (is (true? (invoke engine-plan 'w5-trust-replay-valid?
                        [request-value])))
    (is (true? (invoke engine-plan 'w5-trust-authority-valid?
                        [(get request-value :authority)])))
    (is (true? (invoke engine-plan 'w5-trust-review-valid?
                        [(get request-value :review)])))
    (is (true? (invoke engine-plan 'w5-trust-claims-valid?
                        [(get request-value :claims)])))
    (is (true? (invoke engine-plan 'w5-trust-request-valid?
                        [request-value])))))

(deftest w5-trust-identity-is-path-neutral-and-provenance-retains-paths
  (let [left-request
        (request accepted-gravity-plan
                 'w5-trust-provenance-request)
        right-request
        (request accepted-gravity-plan
                 'w5-trust-provenance-alternate-path-request)
        left (verify left-request)
        right (verify right-request)]
    (is (= (:identity-input left) (:identity-input right)))
    (is (not= (:provenance left) (:provenance right)))
    (is (not (str/includes? (pr-str (:identity-input left)) "/checkout-a")))
    (is (not (str/includes? (pr-str (:identity-input right)) "/checkout-b")))
    (is (str/includes? (pr-str (:provenance left)) "/checkout-a"))
    (is (str/includes? (pr-str (:provenance right)) "/checkout-b"))))

(deftest w5-trust-invalid-span-uses-deterministic-diagnostic-fallback
  (let [base (request accepted-gravity-plan 'w5-trust-provenance-request)
        invalid (invoke rejected-gravity-plan
                        'w5-trust-invalid-span-request
                        [base])
        diagnostic (first (:diagnostics (verify invalid)))]
    (is (= :w5-trust-request
           (get-in diagnostic [:source-span :source-id])))
    (is (= 0 (get-in diagnostic [:source-span :start :byte])))
    (is (= 0 (get-in diagnostic [:source-span :end :byte])))
    (is (true? (invoke engine-plan 'w5-trust-span?
                        [(:source-span diagnostic)])))))

(deftest w5-trust-diverse-rebuilds-and-attestations-are-exactly-cross-bound
  (let [base (request accepted-gravity-plan 'w5-trust-provenance-request)
        recipe-switch
        (invoke rejected-gravity-plan
                'w5-trust-invalid-diverse-recipe-switch-request [base])
        coherent-swap
        (invoke rejected-gravity-plan
                'w5-trust-invalid-diverse-coherent-recipe-swap-request [base])
        other-builder
        (invoke rejected-gravity-plan
                'w5-trust-invalid-attestation-other-builder-request [base])]
    (is (true? (invoke engine-plan 'w5-trust-diverse-rebuilds-valid? [base])))
    (is (true? (invoke engine-plan 'w5-trust-attestations-valid? [base])))
    (is (false? (invoke engine-plan 'w5-trust-diverse-rebuilds-valid?
                        [recipe-switch])))
    (is (false? (invoke engine-plan 'w5-trust-diverse-rebuilds-valid?
                        [coherent-swap])))
    (is (false? (invoke engine-plan 'w5-trust-attestations-valid?
                        [other-builder])))
    (is (= ["W5-TRUST-DIVERSITY"]
           (mapv :rule (:diagnostics (verify recipe-switch)))))
    (is (= ["W5-TRUST-DIVERSITY"]
           (mapv :rule (:diagnostics (verify coherent-swap)))))
    (is (= ["W5-TRUST-ATTESTATION"]
           (mapv :rule (:diagnostics (verify other-builder)))))))

(deftest w5-trust-signatures-have-fixed-ordered-subjects
  (let [base (request accepted-gravity-plan 'w5-trust-provenance-request)
        records (:signatures base)
        expected (invoke engine-plan 'w5-trust-signature-subject-map [])
        other-recipe
        (invoke rejected-gravity-plan
                'w5-trust-invalid-signature-other-recipe-request [base])
        other-signer
        (invoke rejected-gravity-plan
                'w5-trust-invalid-signature-other-signer-anchor-request [base])]
    (is (= expected
           (mapv #(invoke engine-plan 'w5-trust-signature-subject [% base])
                 records)))
    (is (= 2 (count (set (map :signature-id expected)))))
    (is (= 2 (count (set (map :attestation-id expected)))))
    (is (= 2 (count (set (map :rebuild-id expected)))))
    (is (= 2 (count (set (map :build-recipe-id expected)))))
    (is (= 2 (count (set (map :signer-id expected)))))
    (is (= 2 (count (set (map :trust-anchor-id expected)))))
    (is (true? (invoke engine-plan 'w5-trust-signatures-valid? [base])))
    (is (false? (invoke engine-plan 'w5-trust-signatures-valid?
                        [other-recipe])))
    (is (false? (invoke engine-plan 'w5-trust-signatures-valid?
                        [other-signer])))
    (is (= ["W5-TRUST-SIGNATURE"]
           (mapv :rule (:diagnostics (verify other-recipe)))))
    (is (= ["W5-TRUST-SIGNATURE"]
           (mapv :rule (:diagnostics (verify other-signer)))))))

(deftest w5-trust-rejected-fixture-covers-every-stable-family
  (let [gravity-request
        (request accepted-gravity-plan 'w5-trust-provenance-request)
        qst-request
        (request accepted-qst-plan 'w5-trust-provenance-request)]
    (doseq [[function [rule field]] rejected-cases]
      (testing (str function)
        (let [gravity-invalid
              (invoke rejected-gravity-plan function [gravity-request])
              qst-invalid
              (invoke rejected-qst-plan function [qst-request])
              gravity-result (verify gravity-invalid)
              qst-result (verify qst-invalid)
              diagnostic (first (:diagnostics gravity-result))]
          (is (= gravity-invalid qst-invalid))
          (is (= gravity-result qst-result))
          (is (= :rejected (:status gravity-result)))
          (is (= 1 (count (:diagnostics gravity-result))))
          (is (= rule (:rule diagnostic) (:diagnostic-id diagnostic)))
          (is (= field (:field diagnostic)))
          (is (= :error (:severity diagnostic)))
          (is (map? (:source-span diagnostic)))
          (is (keyword? (:remediation diagnostic)))
          (is (= :non-authority (:authority diagnostic)))
          (is (true? (:clojure-seed-boundary? gravity-result)))
          (is (false? (:self-hosted? gravity-result)))
          (is (false? (:release? gravity-result))))))))

(deftest w5-trust-result-substitution-fails-closed
  (let [request-value
        (request accepted-gravity-plan 'w5-trust-provenance-request)
        result (verify request-value)
        hostile (invoke rejected-gravity-plan 'w5-trust-invalid-result [result])
        verification (invoke engine-plan 'w5-trust-verify-result
                              [request-value hostile])]
    (is (= :complete (:closure-status hostile)))
    (is (= :rejected (:status verification)))
    (is (= "W5-TRUST-SUBSTITUTION"
           (get (get (get verification :diagnostics) 0) :rule)))
    (is (= :non-authority (:authority verification)))
    (is (true? (:clojure-seed-boundary? verification)))))
