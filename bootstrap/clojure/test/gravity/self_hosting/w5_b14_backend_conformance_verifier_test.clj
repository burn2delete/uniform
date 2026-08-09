(ns gravity.self-hosting.w5-b14-backend-conformance-verifier-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

; Later JVM clearance command (intentionally not run in this change):
; clojure -M:test --namespace gravity.self-hosting.w5-b14-backend-conformance-verifier-test

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/w5_b14_backend_conformance_verifier_test.clj")]
    (when-not resource
      (throw
       (ex-info "W5 B14 verifier test source is not on the classpath"
                {:id "W5-B14-TEST-SOURCE"})))
    (loop [candidate
           (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "W5-B14-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private engine-source
  "bootstrap/gravity/src/gravity/backend/w5_b14_backend_conformance_verifier.gravity")
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/w5-b14")

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

(def ^:private engine-plan (delay (compile-plan engine-source)))
(def ^:private accepted-gravity-plan
  (delay
   (compile-plan
    (fixture-path "accepted" "backend-conformance" ".gravity"))))
(def ^:private accepted-qst-plan
  (delay
   (compile-plan
    (fixture-path "accepted" "backend-conformance" ".qst"))))
(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-path "rejected" "invalid-backend-conformance" ".gravity"))))
(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (fixture-path "rejected" "invalid-backend-conformance" ".qst"))))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-w5-b14-backend-conformance-verifier
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- request [plan function]
  (invoke plan function []))

(defn- request-at [plan function actual-source-path compiler-source-path logical-source]
  (invoke plan function
          [actual-source-path compiler-source-path logical-source]))

(defn- execute [request-value]
  (invoke engine-plan 'w5-b14-execute [request-value]))

(def ^:private diagnostic-families
  {"w5-b14-invalid-coverage-request"
   ["B14-COVERAGE" :fixture-matrix-required]
  "w5-b14-invalid-target-request"
   ["B14-TARGET" :candidate-target-availability-required]
   "w5-b14-invalid-suite-target-link-request"
   ["B14-COVERAGE" :suite-manifest-required]
  "w5-b14-invalid-positive-request"
   ["B14-POSITIVE" :positive-lowering-results-required]
   "w5-b14-invalid-positive-fixture-link-request"
   ["B14-EVIDENCE" :cross-record-links-required]
  "w5-b14-invalid-negative-request"
   ["B14-NEGATIVE" :negative-diagnostic-fixtures-required]
  "w5-b14-invalid-negative-span-link-request"
   ["B14-EVIDENCE" :cross-record-links-required]
  "w5-b14-invalid-reversed-span-request"
   ["B14-NEGATIVE" :negative-diagnostic-fixtures-required]
  "w5-b14-invalid-differential-request"
   ["B14-DIFFERENTIAL" :semantic-comparison-required]
   "w5-b14-invalid-differential-link-request"
   ["B14-EVIDENCE" :cross-record-links-required]
   "w5-b14-invalid-metadata-request"
   ["B14-METADATA" :metadata-preservation-required]
  "w5-b14-invalid-artifact-request"
   ["B14-ARTIFACT" :artifact-manifest-validation-required]
   "w5-b14-invalid-artifact-link-request"
   ["B14-EVIDENCE" :cross-record-links-required]
   "w5-b14-invalid-nondeterminism-request"
   ["B14-NONDETERMINISM" :replay-record-required]
   "w5-b14-invalid-skip-request"
   ["B14-SKIP" :fail-closed-skip-record-required]
  "w5-b14-invalid-evidence-request"
   ["B14-EVIDENCE" :evidence-pack-required]
   "w5-b14-invalid-identity-link-request"
   ["B14-EVIDENCE" :cross-record-links-required]
   "w5-b14-invalid-provenance-link-request"
   ["B14-EVIDENCE" :cross-record-links-required]})

(deftest w5-b14-engine-and-fixtures-compile-through-stage2-plan
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  ; :jvm is only the current stage2 compiler-plan namespace harness.  The
  ; verifier policy and request/result records below assert exact LLVM target
  ; evidence and reject JVM/Darwin/cross-target inference.
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :jvm (get-in @plan [:module :target]))))
  (doseq [function
          '[w5-b14-policy
            w5-b14-diagnostic-catalog
            w5-b14-suite-manifest
            w5-b14-fixture-matrix
            w5-b14-target-availability
            w5-b14-positive-results
            w5-b14-negative-results
            w5-b14-differential-results
            w5-b14-metadata-report
            w5-b14-artifact-report
            w5-b14-nondeterminism-record
            w5-b14-skip-report
            w5-b14-risk-coverage
            w5-b14-evidence-pack
            w5-b14-diagnostic-stream
            w5-b14-differential-entry-valid?
            w5-b14-differential-entries-valid?
            w5-b14-record-links-valid?
            w5-b14-conformance-execute
            w5-b14-backend-conformance
            w5-b14-conformance
            w5-b14-execute
            w5-b14-run
            w5-b14-verify
            w5-b14-recompute
            w5-b14-verify-result]]
    (is (map? (get-in @engine-plan [:functions function])) function))
  (doseq [[plan functions]
          [[accepted-gravity-plan
           '[w5-b14-conformance-request
              w5-b14-conformance-request-at
              w5-b14-conformance-alternate-path-request]]
           [accepted-qst-plan
            '[w5-b14-conformance-request
              w5-b14-conformance-request-at
              w5-b14-conformance-alternate-path-request]]
           [rejected-gravity-plan (keys diagnostic-families)]
           [rejected-qst-plan (keys diagnostic-families)]]]
    (doseq [function functions]
      (is (map? (get-in @plan [:functions (symbol function)])) function)))
  (doseq [[family basename]
          [["accepted" "backend-conformance"]
           ["rejected" "invalid-backend-conformance"]]]
    (is (= (slurp (path (fixture-path family basename ".gravity")))
           (slurp (path (fixture-path family basename ".qst")))))))

(deftest w5-b14-policy-is-static-nonauthority-and-target-bounded
  (let [policy (invoke engine-plan 'w5-b14-policy [])
        catalog (invoke engine-plan 'w5-b14-diagnostic-catalog [])]
    (is (= :gravity/w5-b14-backend-conformance-policy (:artifact policy)))
    (is (= :stage2-static-only (:scope policy)))
    (is (= :llvm-x86_64-linux (:target policy)))
    (is (= :llvm-x86_64-linux (:candidate-target policy)))
    (is (= {:os :linux :arch :x86_64 :backend :llvm
            :object-format :elf :abi :sysv-amd64}
           (:candidate-platform policy)))
    (is (= :llvm (:supported-backend policy)))
    (is (true? (:static-only? policy)))
    (is (= :incomplete (:coverage-status policy)))
    (is (false? (:release-eligible? policy)))
    (is (= :non-authority (:authority policy)))
    (is (= [:darwin :darwin-arm64 :darwin-x86_64 :windows]
           (:unsupported-platforms policy)))
    (is (= [:darwin :darwin-arm64 :darwin-x86_64 :windows]
           (:unsupported-targets policy)))
    (doseq [entry (:unsupported-target-policies policy)]
      (is (= :unsupported (:support entry)))
      (is (false? (:invokes-clojure? entry)))
      (is (false? (:links-jvm? entry)))
      (is (false? (:fallback? entry))))
    (is (false? (:cross-target-inference? policy)))
    (is (false? (:darwin-fallback? policy)))
    (is (true? (:clojure-seed-boundary? policy)))
    (is (false? (:self-hosted? policy)))
    (is (false? (:release? policy)))
    (is (false? (:public-authority? policy)))
    (is (= :clojure-bootstrap
           (get-in policy [:residual-boundaries :stage2-compiler-plan])))
    (is (= :jvm
           (get-in policy [:residual-boundaries :stage2-runtime])))
    (is (= :non-authority (:authority catalog)))
    (is (= #{"B14-COVERAGE" "B14-TARGET" "B14-POSITIVE"
             "B14-NEGATIVE" "B14-DIFFERENTIAL" "B14-METADATA"
             "B14-ARTIFACT" "B14-NONDETERMINISM" "B14-SKIP"
             "B14-EVIDENCE"}
           (set (:diagnostics catalog))))))

(deftest w5-b14-accepted-record-verifies-all-static-surfaces
  (doseq [[plan function actual-source-path logical-source]
          [[accepted-gravity-plan 'w5-b14-conformance-request
            "/checkout-a/bootstrap/clojure/fixtures/self-hosting/w5-b14/accepted/backend-conformance.gravity"
            "fixtures/w5-b14/accepted/backend-conformance.gravity"]
           [accepted-qst-plan 'w5-b14-conformance-request-at
            "/checkout-a/bootstrap/clojure/fixtures/self-hosting/w5-b14/accepted/backend-conformance.qst"
            "fixtures/w5-b14/accepted/backend-conformance.qst"]]]
    (let [request-value
          (if (= function 'w5-b14-conformance-request)
            (request plan function)
            (request-at plan function actual-source-path
                        "/checkout-a/bootstrap/gravity/src/gravity/backend/w5_b14_backend_conformance_verifier.gravity"
                        logical-source))
          result (execute request-value)
          suite (:suite-manifest request-value)
          availability (:target-availability request-value)
          evidence (:evidence-pack request-value)]
      (is (= :accepted (:status result)))
      (is (= :incomplete (:coverage-status result)))
      (is (= 30 (count (:fixture-matrix result))))
      (is (= 27 (count (:positive-results result))))
      (is (= 10 (count (:negative-results result))))
      (is (= :passed
             (get-in result [:differential-results :status])))
      (is (= :preserved
             (get-in result [:metadata-report :status])))
      (is (= :valid
             (get-in result [:artifact-report :status])))
      (is (= :recorded
             (get-in result [:nondeterminism-record :status])))
      (is (= :incomplete (:status evidence)))
      (is (= :incomplete (:coverage-status suite)))
      (is (= #{:llvm} (:covered-backends suite)))
      (is (= :llvm-x86_64-linux (:candidate-target result)))
      (is (= :incomplete (:status availability)))
      (is (= #{:errors :ai-tool-calls :schemas}
             (set (map :family
                       (filter #(= :pending (:status %))
                               (:fixture-matrix result))))))
      (is (empty? (:unsupported-skips availability)))
      (is (= [:darwin :darwin-arm64 :darwin-x86_64 :windows]
             (:unsupported-platforms availability)))
      (is (= [:darwin :darwin-arm64 :darwin-x86_64 :windows]
             (:unsupported-targets availability)))
      (doseq [entry (:unsupported-target-policies availability)]
        (is (= :unsupported (:support entry)))
        (is (false? (:invokes-clojure? entry)))
        (is (false? (:links-jvm? entry)))
        (is (false? (:fallback? entry))))
      (is (false? (:release-eligible? result)))
      (is (= :non-authority (:authority result)))
      (is (true? (:clojure-seed-boundary? result)))
      (is (false? (:self-hosted? result)))
      (is (false? (:release? result)))
      (is (false? (:public-authority? result)))
      (is (empty? (:diagnostics result))))))

(deftest w5-b14-differential-validator-has-total-execution-shape
  (let [request-value
        (request accepted-gravity-plan 'w5-b14-conformance-request)
        entries (get (:differential-results request-value) :comparisons)
        entry (first entries)]
    (is (true? (invoke engine-plan 'w5-b14-differential-entry-valid?
                       [entry])))
    (is (false? (invoke engine-plan 'w5-b14-differential-entry-valid?
                        [(assoc entry :execution :executed-on-jvm)])))
    (is (true? (invoke engine-plan 'w5-b14-differential-entries-valid?
                       [entries 0])))
    (is (false? (invoke engine-plan 'w5-b14-differential-entries-valid?
                        [(assoc entries 0
                                (assoc entry :status :mismatched)) 0])))))

(deftest w5-b14-provenance-retains-source-kind
  (let [gravity-request
        (request accepted-gravity-plan 'w5-b14-conformance-request)
        qst-request
        (request-at accepted-qst-plan 'w5-b14-conformance-request-at
                    "/checkout-a/bootstrap/clojure/fixtures/self-hosting/w5-b14/accepted/backend-conformance.qst"
                    "/checkout-a/bootstrap/gravity/src/gravity/backend/w5_b14_backend_conformance_verifier.gravity"
                    "fixtures/w5-b14/accepted/backend-conformance.qst")
        gravity-result (execute gravity-request)
        qst-result (execute qst-request)]
    (is (= :accepted (:status gravity-result)))
    (is (= :accepted (:status qst-result)))
    (is (str/ends-with? (get-in gravity-result [:provenance :actual-source-path])
                        ".gravity"))
    (is (str/ends-with? (get-in qst-result [:provenance :actual-source-path])
                        ".qst"))
    (is (str/ends-with? (get-in qst-result [:provenance :logical-source])
                        ".qst"))))

(deftest w5-b14-identity-is-path-neutral-and-provenance-retains-paths
  (let [left-request
        (request accepted-gravity-plan
                 'w5-b14-conformance-request)
        right-request
        (request accepted-gravity-plan
                 'w5-b14-conformance-alternate-path-request)
        left (execute left-request)
        right (execute right-request)]
    (is (= :accepted (:status left)))
    (is (= :accepted (:status right)))
    (is (= (:identity-input left) (:identity-input right)))
    (is (not= (:provenance left) (:provenance right)))
    (is (not (str/includes? (pr-str (:identity-input left)) "/checkout-a/")))
    (is (not (str/includes? (pr-str (:identity-input right)) "/checkout-b/")))
    (is (str/includes? (pr-str (:provenance left)) "/checkout-a/"))
    (is (str/includes? (pr-str (:provenance right)) "/checkout-b/"))
    (is (false? (:cross-target-inference? left)))
    (is (false? (:darwin-fallback? left)))))

(deftest w5-b14-rejected-fixture-covers-every-diagnostic-family
  (doseq [[function-name [expected-rule remediation]] diagnostic-families]
    (testing function-name
      (doseq [[accepted-plan rejected-plan]
              [[accepted-gravity-plan rejected-gravity-plan]
               [accepted-qst-plan rejected-qst-plan]]]
        (let [base
              (if (= accepted-plan accepted-qst-plan)
                (request-at accepted-plan 'w5-b14-conformance-request-at
                            "/checkout-a/bootstrap/clojure/fixtures/self-hosting/w5-b14/accepted/backend-conformance.qst"
                            "/checkout-a/bootstrap/gravity/src/gravity/backend/w5_b14_backend_conformance_verifier.gravity"
                            "fixtures/w5-b14/accepted/backend-conformance.qst")
                (request accepted-plan 'w5-b14-conformance-request))
              mutator (symbol function-name)
              invalid (invoke rejected-plan mutator [base])
              result (execute invalid)
              diagnostic (first (:diagnostics result))]
          (is (= :rejected (:status result)))
          (is (= expected-rule (:rule diagnostic)))
          (is (= expected-rule (:diagnostic-id diagnostic)))
          (is (= remediation (:remediation diagnostic)))
          (is (= :backend-conformance (:stage diagnostic)))
          (is (= :meta (:profile diagnostic)))
          (is (= :llvm-x86_64-linux (:target diagnostic)))
          (is (map? (:source-span diagnostic)))
          (is (map? (:provenance diagnostic)))
          (is (true? (get-in diagnostic [:facts :source-span-preserved?])))
          (is (true?
               (get-in diagnostic [:facts :actual-path-in-provenance-only])))
          (is (true? (:clojure-seed-boundary? result)))
          (is (false? (:self-hosted? result)))
          (is (false? (:release? result)))
          (is (false? (:public-authority? result))))))))

(deftest w5-b14-result-verifier-recomputes-and-rejects-substitution
  (let [request-value
        (request accepted-gravity-plan 'w5-b14-conformance-request)
        result (execute request-value)
        verification
        (invoke engine-plan 'w5-b14-verify-result
                [request-value result])
        altered-result
        (assoc-in result [:positive-results 0 :status] :failed)
        altered-verification
        (invoke engine-plan 'w5-b14-verify-result
                [request-value altered-result])
        altered-request
        (assoc-in request-value [:metadata-report :source-spans] false)
        recomputed
        (execute altered-request)]
    (is (= :passed (:status verification)))
    (is (true? (:recomputed verification)))
    (is (= :rejected (:status altered-verification)))
    (is (= "B14-EVIDENCE"
           (get-in altered-verification [:diagnostics 0 :rule])))
    (is (= :rejected (:status recomputed)))
    (is (= "B14-METADATA"
           (get-in recomputed [:diagnostics 0 :rule])))))

(deftest w5-b14-producer-flags-cannot-promote-authority
  (let [request-value
        (request accepted-gravity-plan 'w5-b14-conformance-request)
        result (execute request-value)
        forged (assoc result :clojure-seed-boundary? false
                      :self-hosted? true
                      :release? true
                      :public-authority? true
                      :authority :public-authority)
        verification
        (invoke engine-plan 'w5-b14-verify-result
                [request-value forged])]
    (is (= :rejected (:status verification)))
    (is (= "B14-EVIDENCE"
           (get-in verification [:diagnostics 0 :rule])))))
