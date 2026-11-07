(ns gravity.compiler-pass-manifest
  "C1/P06-T01 compiler pass-manifest contract leaf."
  (:require [gravity.compiler-pass-manifest.artifact :as artifact]
            [gravity.compiler-pass-manifest.contracts :as contracts]
            [gravity.compiler-pass-manifest.diagnostic-validation :as diagnostic-validation]
            [gravity.compiler-pass-manifest.diagnostics :as diagnostic-data]
            [gravity.compiler-pass-manifest.failures :as failures]
            [gravity.compiler-pass-manifest.incremental :as incremental]
            [gravity.compiler-pass-manifest.incremental-validation :as incremental-validation]
            [gravity.compiler-pass-manifest.pipeline-validation :as pipeline-validation]
            [gravity.compiler-pass-manifest.plugin-validation :as plugin-validation]
            [gravity.compiler-pass-manifest.plugins :as plugins]
            [gravity.compiler-pass-manifest.proof :as proof]
            [gravity.compiler-pass-manifest.suite :as suite]
            [gravity.compiler-pass-manifest.verification :as verification]
            [gravity.compiler-pass-manifest.verification-validation :as verification-validation]
            [gravity.diagnostics :as diagnostics]))

(def ^:private c1-diagnostic-ids
  ["C1-PIPELINE" "C1-PASS-CONTRACT" "C1-EVIDENCE-DROP"
   "C1-UNCHECKED-BACKEND" "C1-MANIFEST"])

(def ^:private c15-diagnostic-ids
  ["C15-SCHEMA" "C15-ID" "C15-SPAN" "C15-ORIGIN" "C15-FACTS"
   "C15-REMEDIATION" "C15-REDACTION" "C15-ORDER"])

(def ^:private c16-diagnostic-ids
  ["C16-KEY" "C16-ENTRY" "C16-PROOF" "C16-SPECULATIVE"])

(def ^:private c17-diagnostic-ids
  ["C17-MANIFEST" "C17-API" "C17-CAPABILITY"
   "C17-PASS-CONTRACT" "C17-OUTPUT"])

(def ^:private c18-diagnostic-ids
  ["C18-RISK" "C18-EVIDENCE" "C18-TRUST-REPORT"
   "C18-RELEASE-GATE"])

(def compiler-pass-diagnostic-ids
  (vec (concat c1-diagnostic-ids c15-diagnostic-ids c16-diagnostic-ids
               c17-diagnostic-ids c18-diagnostic-ids)))

(defn- perf-present?
  [value]
  (and (some? value)
       (not (and (coll? value) (empty? value)))))

(defn- fail!
  [id message data]
  (diagnostics/fail! id message data))

(def ^:private namespace-contract
  {:namespace 'gravity.compiler-pass-manifest
   :contract-boundary :stage0-compiler-pass-manifest
   :governing-contracts '[D0 D1 D2 D3 D6 D8 D9 C1 C15 C18 BOOT8 TEST2]
   :authenticates-inputs? false :issues-certificates? false
   :source-test-owner 'gravity.lowering.plan-assembly-test
   :artifact-inputs [:caller-owned-legacy-module-pass-risk-and-late-owner-vars :math-conformance-artifact :compiler-pass-overrides]
   :artifact-outputs [:legacy-native-manifest-suite-presence-or-owner-result :compiler-pipeline-manifest :pass-contract-registry
                      :diagnostic-registry :incremental-cache-key-schema
                      :plugin-pass-api-manifest :verification-plan
                      :compiler-trust-report]
   :owns [:legacy-stage0-manifest-suite-presence-and-c1-c18-rule-facts
          :stage0-pass-contract-construction
          :stage0-pass-contract-validation
          :stage0-pass-contract-capability-proof]
   :dependency-direction
   {:requires ['clojure.set 'clojure.string 'gravity.diagnostics
               'gravity.digest]
    :consumed-by ['gravity.bootstrap]
    :forbids ['gravity.bootstrap]}
   :does-not-own [:native-labels-as-authenticated-proof :complete-c1-c18-verification :canonical-compiler-authority
                  :self-hosting :release :seed-retirement
                  :math-conformance-construction
                  :source-reading :target-lowering]
   :compatibility-only? true
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-authority? false
   :self-hosted? false
   :release? false
   :seed-retirement? false
   :test-owner
   'gravity.compiler-pass-manifest-test/compiler-pass-manifest-is-a-bootstrap-free-stage-boundary})

(def compiler-pass-default-stage-order
  contracts/compiler-pass-default-stage-order)
(def compiler-pass-contract-required-fields
  contracts/compiler-pass-contract-required-fields)
(def compiler-pass-durable-facts
  contracts/compiler-pass-durable-facts)
(def compiler-pass-default-contracts
  contracts/compiler-pass-default-contracts)
(def compiler-pass-default-diagnostic-schema
  diagnostic-data/compiler-pass-default-diagnostic-schema)
(def compiler-pass-default-diagnostic-catalog
  diagnostic-data/compiler-pass-default-diagnostic-catalog)
(def compiler-pass-default-diagnostic-fixtures
  diagnostic-data/compiler-pass-default-diagnostic-fixtures)
(def compiler-pass-default-cache-key-schema
  incremental/compiler-pass-default-cache-key-schema)
(def compiler-pass-default-cache-keys
  incremental/compiler-pass-default-cache-keys)
(def compiler-pass-default-cache-entries
  incremental/compiler-pass-default-cache-entries)
(def compiler-pass-default-proof-reuse-records
  incremental/compiler-pass-default-proof-reuse-records)
(def compiler-pass-default-speculative-reuse-records
  incremental/compiler-pass-default-speculative-reuse-records)
(def compiler-pass-default-plugin-manifest
  plugins/compiler-pass-default-plugin-manifest)
(def compiler-pass-default-plugin-pass-contracts
  plugins/compiler-pass-default-plugin-pass-contracts)
(def compiler-pass-default-plugin-execution-traces
  plugins/compiler-pass-default-plugin-execution-traces)
(def compiler-pass-default-release-gate-report
  verification/compiler-pass-default-release-gate-report)

(defn compiler-pass-contract
  [pass owner-doc input output requires preserves invalidates regenerates emits
   rejects risk evidence-class]
  (contracts/compiler-pass-contract
   pass owner-doc input output requires preserves invalidates regenerates emits
   rejects risk evidence-class))

(defn compiler-pass-default-risk-classification
  [contracts]
  (verification/compiler-pass-default-risk-classification contracts))
(defn compiler-pass-default-trust-report
  [contracts risk-records]
  (verification/compiler-pass-default-trust-report contracts risk-records))
(defn compiler-pass-merge-record-overrides
  [defaults overrides id-key]
  (suite/compiler-pass-merge-record-overrides defaults overrides id-key))
(defn compiler-pass-suite
  [manifest]
  (suite/compiler-pass-suite manifest))
(defn compiler-pass-fail!
  [id source-path manifest record extra]
  (failures/compiler-pass-fail! id source-path manifest record extra))
(defn compiler-pass-missing-fields
  [record required-fields]
  (failures/compiler-pass-missing-fields record required-fields))
(defn compiler-pass-validate-pipeline!
  [source-path manifest suite]
  (pipeline-validation/compiler-pass-validate-pipeline!
   source-path manifest suite))
(defn compiler-pass-validate-diagnostics!
  [source-path manifest suite]
  (diagnostic-validation/compiler-pass-validate-diagnostics!
   source-path manifest suite))
(defn compiler-pass-validate-incremental!
  [source-path manifest suite]
  (incremental-validation/compiler-pass-validate-incremental!
   source-path manifest suite))
(defn compiler-pass-validate-plugins!
  [source-path manifest suite]
  (plugin-validation/compiler-pass-validate-plugins!
   source-path manifest suite))
(defn compiler-pass-validate-verification!
  [source-path manifest suite]
  (verification-validation/compiler-pass-validate-verification!
   source-path manifest suite))
(defn compiler-pass-capability-proof
  [suite]
  (proof/compiler-pass-capability-proof suite))
(defn compiler-pass-source-artifact-from-upstream
  [source-path upstream-artifact]
  (artifact/compiler-pass-source-artifact-from-upstream
   source-path upstream-artifact))

(defn native-stage0-compiler-manifest [module]
  {:profile (:profile module),
   :target (:target module),
   :source-effects (:effects module),
   :source-capabilities (:capabilities module),
   :metadata (:metadata module)})

(defn native-stage0-compiler-suite [module]
  (get-in module [:metadata :compiler :compiled-gate] {}))

(defn native-stage0-compiler-suite-present? [module]
  (contains? (get-in module [:metadata :compiler] {}) :compiled-gate))

(defn validate-native-stage0-pass-contract! [operations
                                             module
                                             manifest
                                             contract]
  (let [source-path (:source-path module)
        missing-fields ((deref (:missing-fields operations))
                         contract
                         (deref (:required-fields operations)))]
    (if (seq missing-fields)
      ((deref (:fail! operations))
        "C1-PASS-CONTRACT"
        source-path
        manifest
        contract
        {:missing-fields missing-fields,
         :remediation
         "Expose the compiled executable pass contract before claiming compiler architecture coverage."})
      (let [durable-drops ((deref (:intersection operations))
                            (deref (:durable-facts operations))
                            (set (:invalidates contract)))
            replacements (set
                           (concat
                             (:regenerates contract)
                             (:replacement-evidence contract)
                             (:emits contract)))
            missing-replacements ((deref (:difference operations))
                                   durable-drops
                                   replacements)]
        (when (seq missing-replacements)
          ((deref (:fail! operations))
            "C1-EVIDENCE-DROP"
            source-path
            manifest
            contract
            {:missing-fields (vec missing-replacements),
             :remediation
             "Regenerate durable facts, emit replacement proof, keep runtime checks, or reject the transformation."}))))))

(defn validate-native-stage0-verification-risk! [operations
                                                 module
                                                 risk-record]
  (let [required (set (:minimum-evidence risk-record))
        available (set (:available-evidence risk-record))]
    (when (and
            (contains? #{:high :critical} (:risk risk-record))
            (not ((deref (:subset? operations)) required available)))
      ((deref (:fail! operations))
        "C18-EVIDENCE"
        (:source-path module)
        {:stage :stage0-compiled-compiler-gate,
         :required-evidence required,
         :artifact-id (:artifact-id risk-record),
         :available-evidence available,
         :risk-class (:risk risk-record),
         :affected-profiles #{(:profile module)},
         :version (or (:version risk-record) "stage0-p06-s1"),
         :affected-targets #{(:target module)},
         :pass-id (:pass risk-record)}
        {:missing-fields
         (vec
           (sort-by
             name
             ((deref (:difference operations))
               required
               available)))}))))

(def public-api
  {'native-stage0-compiler-manifest {:authenticates-inputs? false, :admission :none, :operation-selection :late-owner-var-before-call-arguments, :artifact-inputs [:caller-owned-legacy-module-pass-and-risk-metadata], :arglists (quote ([module])), :artifact-outputs [:legacy-manifest-suite-presence-nil-or-owner-result], :issues-certificates? false, :contract-boundary :stage0-native-legacy-compiler-gate, :native-data-only? true}
   'native-stage0-compiler-suite {:authenticates-inputs? false, :admission :none, :operation-selection :late-owner-var-before-call-arguments, :artifact-inputs [:caller-owned-legacy-module-pass-and-risk-metadata], :arglists (quote ([module])), :artifact-outputs [:legacy-manifest-suite-presence-nil-or-owner-result], :issues-certificates? false, :contract-boundary :stage0-native-legacy-compiler-gate, :native-data-only? true}
   'native-stage0-compiler-suite-present? {:authenticates-inputs? false, :admission :none, :operation-selection :late-owner-var-before-call-arguments, :artifact-inputs [:caller-owned-legacy-module-pass-and-risk-metadata], :arglists (quote ([module])), :artifact-outputs [:legacy-manifest-suite-presence-nil-or-owner-result], :issues-certificates? false, :contract-boundary :stage0-native-legacy-compiler-gate, :native-data-only? true}
   'validate-native-stage0-pass-contract! {:authenticates-inputs? false, :admission :none, :operation-selection :late-owner-var-before-call-arguments, :operation-keys [:missing-fields :required-fields :durable-facts :intersection :difference :fail!], :data-operation-keys [:required-fields :durable-facts], :artifact-inputs [:caller-owned-legacy-module-pass-and-risk-metadata], :arglists (quote ([operations module manifest contract])), :artifact-outputs [:legacy-manifest-suite-presence-nil-or-owner-result], :issues-certificates? false, :contract-boundary :stage0-native-legacy-compiler-gate, :native-data-only? true}
   'validate-native-stage0-verification-risk! {:authenticates-inputs? false, :admission :none, :operation-selection :late-owner-var-before-call-arguments, :operation-keys [:subset? :difference :fail!], :artifact-inputs [:caller-owned-legacy-module-pass-and-risk-metadata], :arglists (quote ([operations module risk-record])), :artifact-outputs [:legacy-manifest-suite-presence-nil-or-owner-result], :issues-certificates? false, :contract-boundary :stage0-native-legacy-compiler-gate, :native-data-only? true}
   'public-api {:kind :contract}
   'compiler-pass-manifest-contract {:arglists '([])}
   'compiler-pass-diagnostic-ids {:kind :constant}
   'compiler-pass-default-stage-order {:kind :constant}
   'compiler-pass-contract-required-fields {:kind :constant}
   'compiler-pass-durable-facts {:kind :constant}
   'compiler-pass-contract
   {:arglists '([pass owner-doc input output requires preserves invalidates
                regenerates emits rejects risk evidence-class])}
   'compiler-pass-default-contracts {:kind :constant}
   'compiler-pass-default-diagnostic-schema {:kind :constant}
   'compiler-pass-default-diagnostic-catalog {:kind :constant}
   'compiler-pass-default-diagnostic-fixtures {:kind :constant}
   'compiler-pass-default-cache-key-schema {:kind :constant}
   'compiler-pass-default-cache-keys {:kind :constant}
   'compiler-pass-default-cache-entries {:kind :constant}
   'compiler-pass-default-proof-reuse-records {:kind :constant}
   'compiler-pass-default-speculative-reuse-records {:kind :constant}
   'compiler-pass-default-plugin-manifest {:kind :constant}
   'compiler-pass-default-plugin-pass-contracts {:kind :constant}
   'compiler-pass-default-plugin-execution-traces {:kind :constant}
   'compiler-pass-default-risk-classification
   {:arglists '([contracts])}
   'compiler-pass-default-trust-report
   {:arglists '([contracts risk-records])}
   'compiler-pass-default-release-gate-report {:kind :constant}
   'compiler-pass-merge-record-overrides
   {:arglists '([defaults overrides id-key])}
   'compiler-pass-suite {:arglists '([manifest])}
   'compiler-pass-fail! {:arglists '([id source-path manifest record extra])}
   'compiler-pass-missing-fields {:arglists '([record required-fields])}
   'compiler-pass-validate-pipeline!
   {:arglists '([source-path manifest suite])}
   'compiler-pass-validate-diagnostics!
   {:arglists '([source-path manifest suite])}
   'compiler-pass-validate-incremental!
   {:arglists '([source-path manifest suite])}
   'compiler-pass-validate-plugins!
   {:arglists '([source-path manifest suite])}
   'compiler-pass-validate-verification!
   {:arglists '([source-path manifest suite])}
   'compiler-pass-capability-proof {:arglists '([suite])}
   'compiler-pass-source-artifact-from-upstream
   {:arglists '([source-path upstream-artifact])}})


(defn compiler-pass-manifest-contract
  []
  (assoc namespace-contract :public-api public-api))
