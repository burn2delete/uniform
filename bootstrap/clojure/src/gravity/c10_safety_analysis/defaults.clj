(ns gravity.c10-safety-analysis.defaults
  "Stable data and diagnostic behavior for the hosted C10 facade.")

(def diagnostic-ids
  ["C10-NO-OUTCOME"
   "C10-PROOF"
   "C10-CHECK"
   "C10-UNSAFE"
   "C10-GENERATED"
   "C10-TAINT"
   "C10-CAPABILITY"
   "C10-FFI"
   "C10-NUMERIC"
   "C10-OPTIMIZATION"])

(def governing-document
  "docs/phase-06-compiler-architecture/089-c10-safety-analysis-pipeline-design.md")

(def rejected-designs
  [{:diagnostic "C10-NO-OUTCOME"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c10-no-outcome.gravity"
    :rejected-design :missing-safety-classification}
   {:diagnostic "C10-PROOF"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c10-proof.gravity"
    :rejected-design :missing-or-invalid-proof}
   {:diagnostic "C10-CHECK"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c10-check.gravity"
    :rejected-design :missing-or-illegal-runtime-check}
   {:diagnostic "C10-UNSAFE"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c10-unsafe.gravity"
    :rejected-design :unsafe-island-policy-or-metadata-gap}
   {:diagnostic "C10-GENERATED"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c10-generated.gravity"
    :rejected-design :generated-unsafe-without-provenance}
   {:diagnostic "C10-TAINT"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c10-taint.gravity"
    :rejected-design :taint-facts-dropped-before-sink}
   {:diagnostic "C10-CAPABILITY"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c10-capability.gravity"
    :rejected-design :capability-use-without-proof}
   {:diagnostic "C10-FFI"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c10-ffi.gravity"
    :rejected-design :foreign-boundary-safety-gap}
   {:diagnostic "C10-NUMERIC"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c10-numeric.gravity"
    :rejected-design :numeric-safety-gap}
   {:diagnostic "C10-OPTIMIZATION"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c10-optimization.gravity"
    :rejected-design :stale-safety-evidence-after-transform}])

(def override-diagnostics
  {:no-outcome "C10-NO-OUTCOME"
   :proof "C10-PROOF"
   :check "C10-CHECK"
   :unsafe "C10-UNSAFE"
   :generated "C10-GENERATED"
   :taint "C10-TAINT"
   :capability "C10-CAPABILITY"
   :ffi "C10-FFI"
   :numeric "C10-NUMERIC"
   :optimization "C10-OPTIMIZATION"})

(def safe-outcomes
  #{:proven-safe :runtime-checked :rejected :unsafe-island})

(defn source-overrides [module]
  (get-in module [:metadata :compiler :c10-safety-analysis] {}))

(defn message [id]
  (case id
    "C10-NO-OUTCOME" "safety-sensitive operation lacks a SAFE1 outcome"
    "C10-PROOF" "safety proof evidence is missing or invalid"
    "C10-CHECK" "runtime check record is missing or illegal"
    "C10-UNSAFE" "unsafe island lacks required metadata or policy approval"
    "C10-GENERATED" "generated unsafe or rejected code lacks provenance"
    "C10-TAINT" "taint facts were dropped before a sink"
    "C10-CAPABILITY" "authority use is not covered by capability proof"
    "C10-FFI" "foreign boundary is missing safety facts"
    "C10-NUMERIC" "numeric operation lacks required proof or check"
    "C10-OPTIMIZATION" "transformed code has stale safety evidence"
    "Safety analysis failed"))

(defn fail! [fail-op message-op source-span-op governing-document
             id source-path subject extra]
  (fail-op id
           (message-op id)
           (merge {:source-span (or (:source-span subject)
                                    (get-in subject [:source :span])
                                    (:span subject)
                                    (source-span-op source-path 0))
                   :diagnostic-family :c10-safety-analysis
                   :stage :safety-analysis
                   :document-id "C10"
                   :expected-document governing-document
                   :operation-id (or (:operation-id subject) :fixture/operation)
                   :specialized-safe-rule (or (:specialized-safe-rule subject)
                                              :fixture/safe-rule)
                   :generated-origin-chain (or (:generated-origin subject)
                                               (get-in subject [:source :origin-chain]))
                   :profile (:profile subject)
                   :target (:target subject)
                   :safety-mode (or (:safety-mode subject) :safe)
                   :missing-fact (:missing-fact subject)
                   :proof-id (:proof-id subject)
                   :runtime-check (:runtime-check subject)
                   :unsafe-audit (:unsafe-audit subject)
                   :remediation "Classify each safety-sensitive operation with exactly one SAFE1 outcome and emit runtime checks, proof obligations, unsafe audit metadata, generated provenance, taint/capability reports, and optimization invalidation records before MIR construction."}
                  extra)))

(defn validate-overrides! [fail-op source-span-op override-diagnostics
                            source-path module overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get override-diagnostics fail-kind)]
      (fail-op id source-path
               {:source-span (source-span-op source-path 0)
                :operation-id (keyword "fixture" (name fail-kind))
                :profile (:profile module)
                :target (:target module)
                :safety-mode (:safety module)
                :generated-origin []}
               {:missing-fields [fail-kind]}))))

(defn diagnostics [source-span-op diagnostic-ids rejected-designs source-path]
  {:artifact :gravity/c10-safety-diagnostic-registry
   :required-diagnostic-ids diagnostic-ids
   :diagnostics
   (mapv (fn [design]
           {:diagnostic (:diagnostic design)
            :fixture (:fixture design)
            :operation-id (keyword "fixture" (:diagnostic design))
            :specialized-safe-rule (:rejected-design design)
            :source-span (source-span-op source-path 0)
            :generated-origin-chain []
            :profile :fixture/profile
            :target :fixture/target
            :safety-mode :safe
            :missing-fact (:rejected-design design)
            :remediation "Keep SAFE1 outcome, proof, check, unsafe, generated, taint, capability, FFI, numeric, and optimization evidence explicit before MIR construction."})
         rejected-designs)
   :status :complete})
