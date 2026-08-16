(ns gravity.self-hosting.w5-full-language-evidence-verifier-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root []
  (let [resource (io/resource
                  "gravity/self_hosting/w5_full_language_evidence_verifier_test.clj")]
    (when-not resource
      (throw (ex-info "Wave4 evidence test is not on the classpath"
                      {:id "W5-FULL-EVIDENCE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "W5-FULL-EVIDENCE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(defn- path [relative] (str (.resolve @root relative)))
(def ^:private engine-source
  "bootstrap/gravity/src/gravity/self_hosting/w5_full_language_evidence_verifier.gravity")
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/w5-full-language-evidence")
(defn- fixture [family basename extension]
  (str fixture-root "/" family "/" basename extension))

(defn- compile-plan [relative]
  (let [source-path (path relative)
        source-text (slurp source-path)
        emitter (:emitter
                 (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                  source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private engine-plan (delay (compile-plan engine-source)))
(def ^:private accepted-gravity-plan
  (delay (compile-plan (fixture "accepted" "incomplete-full-language-evidence" ".gravity"))))
(def ^:private accepted-qst-plan
  (delay (compile-plan (fixture "accepted" "incomplete-full-language-evidence" ".qst"))))
(def ^:private rejected-gravity-plan
  (delay (compile-plan (fixture "rejected" "invalid-full-language-evidence" ".gravity"))))
(def ^:private rejected-qst-plan
  (delay (compile-plan (fixture "rejected" "invalid-full-language-evidence" ".qst"))))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-w5-full-language-evidence-verifier
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- request [plan function]
  (invoke plan function []))

(defn- request-for-source [plan root-path extension]
  (invoke plan 'w5-full-language-evidence-request-for-source
          [root-path extension]))
(defn- verify [value]
  (invoke engine-plan 'w5-full-language-evidence-verify [value]))

(def ^:private exported-functions
  '[w5-full-language-evidence-policy
    w5-full-language-evidence-diagnostic-catalog
    w5-full-target-valid?
    w5-full-source-fixture-path?
    w5-full-inventory-valid?
    w5-full-coverage-matrix-valid?
    w5-full-gap-report-valid?
    w5-full-task-list-valid?
    w5-full-attestations-valid?
    w5-full-reporting-valid?
    w5-full-evidence-valid?
    w5-full-claims-valid?
    w5-full-authority-valid?
    w5-full-request-valid?
    w5-full-identity-input
    w5-full-provenance
    w5-full-language-evidence-verify
    w5-full-language-evidence-execute
    w5-full-language-evidence-run
    w5-full-language-evidence-recompute
    w5-full-language-evidence-verify-result])

(deftest w5-full-language-evidence-source-and-fixtures-compile
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (doseq [function exported-functions]
    (is (map? (get (get @engine-plan :functions) function)) function))
  (is (= (slurp (path (fixture "accepted" "incomplete-full-language-evidence" ".gravity")))
         (slurp (path (fixture "accepted" "incomplete-full-language-evidence" ".qst")))))
  (is (= (slurp (path (fixture "rejected" "invalid-full-language-evidence" ".gravity")))
         (slurp (path (fixture "rejected" "invalid-full-language-evidence" ".qst"))))))

(deftest w5-full-language-evidence-rejected-fixture-source-paths-are-bound
  (let [gravity-request (request-for-source rejected-gravity-plan
                                             "/checkout-a" ".gravity")
        qst-request (request-for-source rejected-qst-plan
                                        "/checkout-a" ".qst")]
    (is (str/ends-with?
         (get (get gravity-request :source-span) :actual-source-path)
         "rejected/invalid-full-language-evidence.gravity"))
    (is (str/ends-with?
         (get (get qst-request :source-span) :actual-source-path)
         "rejected/invalid-full-language-evidence.qst"))
    (is (= :accepted (:status (verify gravity-request))))
    (is (= :accepted (:status (verify qst-request))))))

(deftest w5-full-language-evidence-policy-is-exact-and-nonauthoritative
  (let [policy (invoke engine-plan 'w5-full-language-evidence-policy [])]
    (is (= :gravity/w5-full-language-evidence-policy (:artifact policy)))
    (is (= 240 (:inventory-count policy)))
    (is (= 38 (:named-gap-task-count policy)))
    (is (= 0 (:attestation-count policy)))
    (is (= 0 (:full-language-complete-count policy)))
    (is (= :llvm-x86_64-linux (:candidate-target policy)))
    (is (= {:os :linux :arch :x86_64 :backend :llvm
            :artifact-format :elf :abi :sysv-amd64}
           (:candidate-platform policy)))
    (is (= [:darwin :darwin-arm64 :darwin-x86_64 :windows]
           (:unsupported-targets policy)))
    (doseq [record (:unsupported-target-policies policy)]
      (is (= :unsupported (:support record)))
      (is (false? (:invokes-clojure? record)))
      (is (false? (:links-jvm? record)))
      (is (false? (:fallback? record))))
    (is (= :blocked (:completion-status policy)))
    (is (= :non-authority (:authority policy)))
    (is (true? (:clojure-seed-boundary? policy)))
    (is (false? (:self-hosted? policy)))
    (is (false? (:release? policy)))
    (is (false? (:public-authority? policy)))
    (is (some #{:narrative} (:forbidden-completion-evidence policy)))
    (is (some #{:producer-boolean} (:forbidden-completion-evidence policy)))
    (is (some #{:global-advancement} (:forbidden-completion-evidence policy)))))

(deftest w5-full-language-evidence-source-path-helper-fails-closed
  (is (true? (invoke engine-plan 'w5-full-source-fixture-path?
                     ["x.gravity"])))
  (is (true? (invoke engine-plan 'w5-full-source-fixture-path?
                     ["x.qst"])))
  (is (false? (invoke engine-plan 'w5-full-source-fixture-path?
                      ["x.edn"])))
  (is (false? (invoke engine-plan 'w5-full-source-fixture-path?
                      ["x"]))))

(deftest w5-full-language-evidence-accepted-is-zero-of-240-blocked
  (let [gravity-request (request-for-source accepted-gravity-plan
                                            "/checkout-a" ".gravity")
        qst-request (request-for-source accepted-qst-plan
                                        "/checkout-a" ".qst")
        result (verify gravity-request)]
    (is (= (dissoc gravity-request :source-span)
           (dissoc qst-request :source-span)))
    (is (str/ends-with?
         (get (get gravity-request :source-span) :actual-source-path)
         ".gravity"))
    (is (str/ends-with?
         (get (get qst-request :source-span) :actual-source-path)
         ".qst"))
    (is (= :accepted (:status (verify qst-request))))
    (is (= :incomplete (:claimed-status gravity-request)))
    (is (= 240 (count (:inventory gravity-request))))
    (is (= 240 (count (:coverage-matrix gravity-request))))
    (is (= 240 (get (:coverage gravity-request) :inventory-count)))
    (is (= 0 (get (:coverage gravity-request) :full-language-complete-count)))
    (is (= 0 (get (:coverage gravity-request) :attestation-count)))
    (is (= [] (get (:coverage gravity-request) :satisfied-task-ids)))
    (is (= 38 (count (:gap-tasks gravity-request))))
    (is (= [] (:attestations gravity-request)))
    (is (= false (get (:claims gravity-request) :producer-boolean?)))
    (is (= false (get (:claims gravity-request) :narrative?)))
    (is (= false (get (:claims gravity-request) :source-ownership-only?)))
    (is (= false (get (:claims gravity-request) :check-only?)))
    (is (= "5b8dd5b6d987c34b36dc71f3be1dfa54b2ce0d88"
           (get (get (get gravity-request :reporting) :observation) :commit)))
    (is (= "53561999651023ba439f94bea508d3fe9e663785"
           (get (get (get gravity-request :reporting) :observation) :tree)))
    (is (= "sha256:912d42f0b789f2a290d81d01450230de834b5529c93df29dfe1c1f648a377142"
           (get (get (get gravity-request :reporting) :contract) :raw-sha256)))
    (is (= :incomplete
           (get (get (get gravity-request :reporting) :matrix) :status)))
    (is (= 240
           (get (get (get gravity-request :reporting) :gap-report) :gap-count)))
    (is (= "disabled-pending-target-coherent-public-native-evidence-v2"
           (get (get (get gravity-request :reporting) :contract) :admission)))
    (is (= :accepted (:status result)))
    (is (= :incomplete (:trust-status result)))
    (is (= :blocked (:closure-status result)))
    (is (= 0 (:full-language-complete-count result)))
    (is (= 0 (:attestation-count result)))
    (is (= "disabled-pending-target-coherent-public-native-evidence-v2"
           (:completion-admission result)))
    (is (= 38 (:named-gap-task-count result)))
    (is (= 0 (:named-task-satisfied-count result)))
    (is (empty? (:diagnostics result)))
    (is (= :non-authority (:authority result)))
    (is (true? (:clojure-seed-boundary? result)))
    (is (false? (:self-hosted? result)))
    (is (false? (:release? result)))
    (is (false? (:public-authority? result)))
    (is (false? (:full-language-completion-authority? result)))
    (is (= :passed
           (:status
            (invoke engine-plan 'w5-full-language-evidence-verify-result
                    [gravity-request result]))))))

(deftest w5-full-language-evidence-static-consumers-pass
  (let [value (request accepted-gravity-plan
                       'w5-full-language-evidence-request)]
    (is (true? (invoke engine-plan 'w5-full-target-valid?
                        [(get value :target-scope)])))
    (is (true? (invoke engine-plan 'w5-full-inventory-valid?
                        [(get value :inventory)])))
    (is (true? (invoke engine-plan 'w5-full-coverage-matrix-valid?
                        [value])))
    (is (true? (invoke engine-plan 'w5-full-gap-report-valid? [value])))
    (is (true? (invoke engine-plan 'w5-full-task-list-valid?
                        [(get value :gap-tasks)])))
    (is (true? (invoke engine-plan 'w5-full-attestations-valid?
                        [(get value :attestations)])))
    (is (true? (invoke engine-plan 'w5-full-reporting-valid?
                        [(get value :reporting)])))
    (is (true? (invoke engine-plan 'w5-full-evidence-valid? [value])))
    (is (true? (invoke engine-plan 'w5-full-claims-valid?
                        [(get value :claims)])))
    (is (true? (invoke engine-plan 'w5-full-authority-valid?
                        [(get value :authority)])))
    (is (true? (invoke engine-plan 'w5-full-request-valid? [value])))))

(deftest w5-full-language-evidence-inventory-and-report-rows-are-exactly-bound
  (let [value (request accepted-gravity-plan
                       'w5-full-language-evidence-request)
        inventory (:inventory value)
        first-document (first inventory)
        last-document (get inventory 239)
        first-matrix-row (first (:coverage-matrix value))
        first-gap-row (first (get (:gap-report value) :entries))]
    (is (= {:sequence 1
            :id "D0"
            :title "Gravity Vision & Design Thesis"
            :path "docs/phase-00-foundation-and-thesis/001-d0-gravity-vision-and-design-thesis.md"
            :phase 0
            :phaseName "Foundation and Thesis"
            :category "foundation"
            :content-id "sha256:0000000000000000000000000000000000000000000000000000000000000001"}
           first-document))
    (is (= {:sequence 240
            :id "GOV10"
            :title "Ecosystem Package Governance Policy"
            :path "docs/phase-17-governance-and-evolution/240-gov10-ecosystem-package-governance-policy.md"
            :phase 17
            :phaseName "Governance and Evolution"
            :category "governance"
            :content-id "sha256:00000000000000000000000000000000000000000000000000000000000000f0"}
           last-document))
    (doseq [row [first-matrix-row first-gap-row]]
      (is (= (:sequence first-document) (:sequence row)))
      (is (= (:id first-document) (:document-id row)))
      (is (= (:title first-document) (:title row)))
      (is (= (:path first-document) (:path row)))
      (is (= (:phase first-document) (:phase row)))
      (is (= (:phaseName first-document) (:phaseName row)))
      (is (= (:category first-document) (:category row)))
      (is (= (:content-id first-document) (:content-id row))))))

(deftest w5-full-language-evidence-identity-is-path-neutral
  (let [left (request accepted-gravity-plan
                      'w5-full-language-evidence-request)
        right (request accepted-gravity-plan
                       'w5-full-language-evidence-alternate-path-request)
        left-result (verify left)
        right-result (verify right)]
    (is (= (:identity-input left-result) (:identity-input right-result)))
    (is (not= (:provenance left-result) (:provenance right-result)))
    (is (not (re-find #"/checkout-a" (pr-str (:identity-input left-result)))))
    (is (not (re-find #"/checkout-b" (pr-str (:identity-input right-result)))))
    (is (re-find #"/checkout-a" (pr-str (:provenance left-result))))
    (is (re-find #"/checkout-b" (pr-str (:provenance right-result))))))

(def ^:private rejected-cases
  {'w5-full-invalid-missing-document-request ["W5-FULL-INVENTORY" :inventory]
   'w5-full-invalid-extra-document-request ["W5-FULL-INVENTORY" :inventory]
   'w5-full-invalid-duplicate-document-request ["W5-FULL-INVENTORY" :inventory]
   'w5-full-invalid-order-request ["W5-FULL-INVENTORY" :inventory]
   'w5-full-invalid-substitution-request ["W5-FULL-INVENTORY" :inventory]
   'w5-full-invalid-sequence-request ["W5-FULL-INVENTORY" :inventory]
   'w5-full-invalid-title-request ["W5-FULL-INVENTORY" :inventory]
   'w5-full-invalid-path-request ["W5-FULL-INVENTORY" :inventory]
   'w5-full-invalid-phase-request ["W5-FULL-INVENTORY" :inventory]
   'w5-full-invalid-phase-name-request ["W5-FULL-INVENTORY" :inventory]
   'w5-full-invalid-category-request ["W5-FULL-INVENTORY" :inventory]
   'w5-full-invalid-content-id-request ["W5-FULL-INVENTORY" :inventory]
   'w5-full-invalid-positive-evidence-request ["W5-FULL-MATRIX" :coverage-matrix]
   'w5-full-invalid-evidence-id-request ["W5-FULL-MATRIX" :coverage-matrix]
   'w5-full-invalid-rejected-evidence-request ["W5-FULL-MATRIX" :coverage-matrix]
   'w5-full-invalid-provenance-request ["W5-FULL-MATRIX" :coverage-matrix]
   'w5-full-invalid-span-request ["W5-FULL-MATRIX" :coverage-matrix]
   'w5-full-invalid-reversed-line-request ["W5-FULL-MATRIX" :coverage-matrix]
   'w5-full-invalid-reversed-column-request ["W5-FULL-MATRIX" :coverage-matrix]
   'w5-full-invalid-coherent-matrix-document-request
   ["W5-FULL-MATRIX" :coverage-matrix]
   'w5-full-invalid-diagnostic-request ["W5-FULL-MATRIX" :coverage-matrix]
   'w5-full-invalid-gap-report-request ["W5-FULL-GAPS" :gap-report]
   'w5-full-invalid-task-request ["W5-FULL-TASKS" :gap-tasks]
   'w5-full-invalid-attestation-request ["W5-FULL-ATTESTATION" :attestations]
   'w5-full-invalid-reporting-identity-request ["W5-FULL-REPORTING" :reporting]
   'w5-full-invalid-reporting-admission-request ["W5-FULL-REPORTING" :reporting]
   'w5-full-invalid-producer-boolean-request ["W5-FULL-CLAIMS" :claims]
   'w5-full-invalid-narrative-request ["W5-FULL-CLAIMS" :claims]
   'w5-full-invalid-source-owner-request ["W5-FULL-CLAIMS" :claims]
   'w5-full-invalid-check-only-request ["W5-FULL-CLAIMS" :claims]
   'w5-full-invalid-replay-only-request ["W5-FULL-CLAIMS" :claims]
   'w5-full-invalid-global-advancement-request ["W5-FULL-MATRIX" :coverage]
   'w5-full-invalid-target-request ["W5-FULL-TARGET" :target-scope]
   'w5-full-invalid-authority-request ["W5-FULL-AUTHORITY" :authority]
   'w5-full-invalid-evidence-provenance-crosslink-request
   ["W5-FULL-MATRIX" :coverage-matrix]
   'w5-full-invalid-diagnostic-crosslink-request
   ["W5-FULL-MATRIX" :coverage-matrix]
   'w5-full-invalid-coherent-diagnostic-request
   ["W5-FULL-MATRIX" :coverage-matrix]
   'w5-full-invalid-gap-provenance-request ["W5-FULL-GAPS" :gap-report]
   'w5-full-invalid-coherent-gap-document-request
   ["W5-FULL-GAPS" :gap-report]
   'w5-full-invalid-coherent-gap-content-request
   ["W5-FULL-GAPS" :gap-report]
   'w5-full-invalid-no-owner-count-request ["W5-FULL-GAPS" :gap-report]
   'w5-full-invalid-top-span-source-id-request
   ["W5-FULL-SCHEMA" :source-span]
   'w5-full-invalid-top-span-bounds-request
   ["W5-FULL-SCHEMA" :source-span]
   'w5-full-invalid-top-span-line-column-request
   ["W5-FULL-SCHEMA" :source-span]
   'w5-full-invalid-top-span-request ["W5-FULL-SCHEMA" :source-span]
   'w5-full-invalid-source-fixture-suffix-request
   ["W5-FULL-PROVENANCE" :source-span]})

(deftest w5-full-language-evidence-rejected-mutators-fail-closed
  (let [value (request accepted-gravity-plan
                       'w5-full-language-evidence-request)]
    (doseq [[function [rule field]] rejected-cases]
      (doseq [plan [rejected-gravity-plan rejected-qst-plan]]
        (let [mutated (invoke plan function [value])
              result (verify mutated)
              diagnostic (first (:diagnostics result))]
          (is (= :rejected (:status result)) function)
          (is (= rule (:rule diagnostic)) function)
          (is (= field (:field diagnostic)) function)
          (is (= :error (:severity diagnostic)) function)
          (is (= :non-authority (:authority diagnostic)) function)
          (is (map? (:source-span diagnostic)) function))))))

(deftest w5-full-language-evidence-malformed-span-uses-deterministic-fallback
  (let [value (request accepted-gravity-plan
                       'w5-full-language-evidence-request)
        mutated (invoke rejected-gravity-plan
                        'w5-full-invalid-top-span-request [value])
        diagnostic (first (:diagnostics (verify mutated)))]
    (is (= "W5-FULL-SCHEMA" (:rule diagnostic)))
    (is (= {:source-id :gravity/w5-full-language-evidence
            :actual-source-path "<unavailable>"
            :start {:byte 0 :line 1 :column 1}
            :end {:byte 0 :line 1 :column 1}}
           (:source-span diagnostic)))))

(deftest w5-full-language-evidence-coherent-top-span-substitutions-fail-closed
  (let [value (request accepted-gravity-plan
                       'w5-full-language-evidence-request)]
    (doseq [function ['w5-full-invalid-top-span-source-id-request
                      'w5-full-invalid-top-span-bounds-request
                      'w5-full-invalid-top-span-line-column-request]]
      (doseq [plan [rejected-gravity-plan rejected-qst-plan]]
        (let [mutated (invoke plan function [value])
              result (verify mutated)
              diagnostic (first (:diagnostics result))]
          (is (false? (invoke engine-plan 'w5-full-request-valid? [mutated]))
              function)
          (is (= :rejected (:status result)) function)
          (is (= "W5-FULL-SCHEMA" (:rule diagnostic)) function)
          (is (= :source-span-origin-or-coordinate-mismatch
                 (:reason diagnostic)) function)
          (is (= {:source-id :gravity/w5-full-language-evidence
                  :actual-source-path "<unavailable>"
                  :start {:byte 0 :line 1 :column 1}
                  :end {:byte 0 :line 1 :column 1}}
                 (:source-span diagnostic)) function)
          (is (= :incomplete (:trust-status result)) function)
          (is (= :blocked (:closure-status result)) function)
          (is (= 0 (:inventory-count result)) function)
          (is (= 0 (:full-language-complete-count result)) function)
          (is (= :non-authority (:authority result)) function)
          (is (true? (:clojure-seed-boundary? result)) function)
          (is (= :passed
                 (:status
                  (invoke engine-plan 'w5-full-language-evidence-verify-result
                          [mutated result]))) function))))))

(deftest w5-full-language-evidence-identity-freezes-row-semantics
  (let [value (request accepted-gravity-plan
                       'w5-full-language-evidence-request)
        identity (invoke engine-plan 'w5-full-identity-input [value])
        diagnostic-hostile
        (invoke rejected-gravity-plan
                'w5-full-invalid-coherent-diagnostic-request [value])
        gap-hostile
        (invoke rejected-gravity-plan
                'w5-full-invalid-coherent-gap-content-request [value])]
    (is (= 240 (count (:matrix-evidence-diagnostic-records identity))))
    (is (= 240 (count (:gap-report-semantic-entries identity))))
    (is (not= identity
              (invoke engine-plan 'w5-full-identity-input
                      [diagnostic-hostile])))
    (is (not= identity
              (invoke engine-plan 'w5-full-identity-input [gap-hostile])))
    (is (= "FULL-D0-PENDING"
           (get-in identity
                   [:matrix-evidence-diagnostic-records 0
                    :diagnostic :diagnostic-id])))
    (is (= [:no-executable-positive :no-rejected-specific
            :no-stable-diagnostic :no-gravity-authored-implementation
            :no-public-gravity-accepted-proof
            :no-public-gravity-rejected-proof]
           (get-in identity [:gap-report-semantic-entries 0 :gaps])))))

(deftest w5-full-language-evidence-result-substitution-fails-closed
  (let [value (request accepted-gravity-plan
                       'w5-full-language-evidence-request)
        result (verify value)
        hostile (invoke rejected-gravity-plan 'w5-full-invalid-result [result])
        verification
        (invoke engine-plan 'w5-full-language-evidence-verify-result
                [value hostile])]
    (is (= :complete (:closure-status hostile)))
    (is (= :rejected (:status verification)))
    (is (= "W5-FULL-SUBSTITUTION"
           (get (get (get verification :diagnostics) 0) :rule)))
    (is (= :non-authority (:authority verification)))
    (is (true? (:clojure-seed-boundary? verification)))))
