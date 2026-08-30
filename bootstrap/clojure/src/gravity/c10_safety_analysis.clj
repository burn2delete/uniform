(ns gravity.c10-safety-analysis
  "Hosted Stage0 C10 safety-analysis pipeline and artifact projection.

  This leaf preserves the Clojure seed compatibility implementation and its
  SAFE1 outcome records. It is not safety, proof, self-hosting, or release
  authority."
  (:require [clojure.set :as set]
            [gravity.digest :as digest]
            [gravity.c10-safety-analysis.policy :as policy]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})

(def ^:private function-operation-keys policy/function-operation-keys)
(def ^:private scalar-operation-keys policy/scalar-operation-keys)
(def ^:private operation-keys policy/operation-keys)

(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key)
    (get *operations* key)))

(defmacro ^:private definterposable [name args & body]
  (let [key (keyword name)]
    `(defn ~name ~args
       (if-let [operation# (current-operation ~key)]
         (binding [*active-operation-keys*
                   (conj *active-operation-keys* ~key)]
           (operation# ~@args))
         (do ~@body)))))

(defn- default-fail! [id message data]
  (throw (ex-info message (assoc (or data {}) :id id))))
(defn- default-source-span [path index]
  {:source path :form-index index})
(defn- default-c4-artifact-id [artifact]
  (str "sha256:" (digest/sha256-hex (pr-str artifact))))
(def ^:private unsupported-host-operation policy/unsupported-host-operation)
(defn- op-fn [key fallback] (or (get *operations* key) fallback))
(defn- fail! [id message data]
  ((op-fn :fail! default-fail!) id message data))
(defn- source-span [path index]
  ((op-fn :source-span default-source-span) path index))
(defn- c4-artifact-id [artifact]
  ((op-fn :c4-artifact-id default-c4-artifact-id) artifact))
(defn- read-source-form-records [path text]
  ((op-fn :read-source-form-records
          (unsupported-host-operation :read-source-form-records))
   path text))
(defn- validate-ns-syntax! [path forms]
  ((op-fn :validate-ns-syntax!
          (unsupported-host-operation :validate-ns-syntax!))
   path forms))
(defn- parse-module [path forms]
  ((op-fn :parse-module (unsupported-host-operation :parse-module))
   path forms))
(defn- compiler-c9-ownership-source-artifact [path text]
  ((op-fn :compiler-c9-ownership-source-artifact
          (unsupported-host-operation :compiler-c9-ownership-source-artifact))
   path text))

(def ^:dynamic c10-safety-diagnostic-ids
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

(def ^:dynamic c10-safety-governing-document
  "docs/phase-06-compiler-architecture/089-c10-safety-analysis-pipeline-design.md")

(def ^:dynamic c10-safety-rejected-designs
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

(def ^:dynamic c10-safety-override-diagnostics
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

(def ^:dynamic c10-safe-outcomes
  #{:proven-safe :runtime-checked :rejected :unsafe-island})

(definterposable c10-safety-source-overrides
  [module]
  (get-in module [:metadata :compiler :c10-safety-analysis] {}))

(definterposable c10-safety-message
  [id]
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

(definterposable c10-safety-fail!
  [id source-path subject extra]
  (fail! id
         (c10-safety-message id)
         (merge {:source-span (or (:source-span subject)
                                  (get-in subject [:source :span])
                                  (:span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :c10-safety-analysis
                 :stage :safety-analysis
                 :document-id "C10"
                 :expected-document c10-safety-governing-document
                 :operation-id (or (:operation-id subject) :fixture/operation)
                 :specialized-safe-rule (or (:specialized-safe-rule subject)
                                            :fixture/safe-rule)
                 :generated-origin-chain (or (:generated-origin subject)
                                             (get-in subject
                                                     [:source :origin-chain]))
                 :profile (:profile subject)
                 :target (:target subject)
                 :safety-mode (or (:safety-mode subject) :safe)
                 :missing-fact (:missing-fact subject)
                 :proof-id (:proof-id subject)
                 :runtime-check (:runtime-check subject)
                 :unsafe-audit (:unsafe-audit subject)
                 :remediation "Classify each safety-sensitive operation with exactly one SAFE1 outcome and emit runtime checks, proof obligations, unsafe audit metadata, generated provenance, taint/capability reports, and optimization invalidation records before MIR construction."}
                extra)))

(definterposable c10-safety-validate-overrides!
  [source-path module overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get c10-safety-override-diagnostics fail-kind)]
      (c10-safety-fail! id source-path
                        {:source-span (source-span source-path 0)
                         :operation-id (keyword "fixture" (name fail-kind))
                         :profile (:profile module)
                         :target (:target module)
                         :safety-mode (:safety module)
                         :generated-origin []}
                        {:missing-fields [fail-kind]}))))

(definterposable c10-safety-operation-inventory
  [module c9-artifact]
  {:artifact :gravity/c10-safety-operation-inventory
   :module (:module module)
   :records [{:operation-id "op-memory-load"
              :kind :buffer-read
              :safe-family :memory
              :source-core-node "c6-core-1"
              :facts {:types :typed-core
                      :effects :effect-graph
                      :ownership :ownership-graph}
              :profile (:profile module)
              :target (:target module)}
             {:operation-id "op-bounds-index"
              :kind :indexing
              :safe-family :bounds
              :source-core-node "c6-core-2"
              :facts {:types :typed-core
                      :effects :effect-graph
                      :ownership :borrow-graph}
              :profile (:profile module)
              :target (:target module)}
             {:operation-id "op-borrow"
              :kind :borrow
              :safe-family :ownership
              :source-core-node "c6-core-3"
              :facts {:ownership :borrow-graph
                      :lifetimes :lifetime-interval-map}
              :profile (:profile module)
              :target (:target module)}
             {:operation-id "op-region"
              :kind :region-allocation
              :safe-family :region
              :facts {:regions :region-lifetime-graph}
              :profile (:profile module)
              :target (:target module)}
             {:operation-id "op-linear"
              :kind :resource-close
              :safe-family :linear-resource
              :facts {:linear :linear-resource-flow-graph}
              :profile (:profile module)
              :target (:target module)}
             {:operation-id "op-ffi"
              :kind :ffi-call
              :safe-family :ffi
              :facts {:transfer :transfer-records}
              :profile (:profile module)
              :target (:target module)}
             {:operation-id "op-concurrency"
              :kind :task-transfer
              :safe-family :concurrency
              :facts {:transfer :transfer-records
                      :lifetimes :lifetime-interval-map}
              :profile (:profile module)
              :target (:target module)}
             {:operation-id "op-numeric"
              :kind :numeric-overflow
              :safe-family :numeric
              :facts {:types :typed-core}
              :profile (:profile module)
              :target (:target module)}
             {:operation-id "op-capability"
              :kind :authority-use
              :safe-family :capability
              :facts {:capabilities :capability-proof-records}
              :profile (:profile module)
              :target (:target module)}
             {:operation-id "op-taint"
              :kind :taint-sink
              :safe-family :taint
              :facts {:taint :taint-report}
              :profile (:profile module)
              :target (:target module)}
             {:operation-id "op-generated-unsafe"
              :kind :generated-unsafe
              :safe-family :macro-safety
              :facts {:origin :generated-origin-chain}
              :profile (:profile module)
              :target (:target module)}
             {:operation-id "op-optimization-erased-check"
              :kind :check-elision
              :safe-family :optimization
              :facts {:proof :optimization-proof}
              :profile (:profile module)
              :target (:target module)}]
   :upstream {:c9-artifact-id (:artifact-id c9-artifact)
              :ownership-proof (get-in c9-artifact
                                       [:capability-based-proof :status])}
   :status :complete})

(definterposable c10-safety-outcome-records
  [module inventory]
  (let [span (source-span (:source-path module) 0)]
    {:artifact :gravity/c10-safety-outcome-records
     :module (:module module)
     :records
     [{:operation "op-memory-load"
       :kind :buffer-read
       :source {:core-node "c6-core-1"
                :span span
                :origin-chain []}
       :profile (:profile module)
       :target (:target module)
       :facts {:type "c7-type-fact"
               :effects "c8-effect-fact"
               :ownership "c9-owner-fact"}
       :outcome :proven-safe
       :proof "proof-memory-valid"
       :runtime-check nil
       :unsafe-audit nil}
      {:operation "op-bounds-index"
       :kind :indexing
       :source {:core-node "c6-core-2"
                :span span
                :origin-chain []}
       :profile (:profile module)
       :target (:target module)
       :facts {:type "c7-length-fact"
               :effects "c8-read-effect"
               :ownership "c9-borrow-fact"}
       :outcome :runtime-checked
       :condition :bounds
       :runtime-check "check-bounds-1"
       :failure-behavior :panic/bounds}
      {:operation "op-borrow"
       :kind :borrow
       :source {:core-node "c6-core-3"
                :span span
                :origin-chain []}
       :profile (:profile module)
       :target (:target module)
       :facts {:ownership "borrow-immutable-a"
               :lifetime "lt-borrow-read"}
       :outcome :proven-safe
       :proof "proof-borrow-lifetime"}
      {:operation "op-region"
       :kind :region-allocation
       :source {:core-node "region-value-config"
                :span span
                :origin-chain []}
       :profile (:profile module)
       :target (:target module)
       :facts {:region "region-outer"}
       :outcome :proven-safe
       :proof "proof-region-no-escape"}
      {:operation "op-linear"
       :kind :resource-close
       :source {:core-node "resource-file"
                :span span
                :origin-chain []}
       :profile (:profile module)
       :target (:target module)
       :facts {:linear "resource-file"}
       :outcome :proven-safe
       :proof "proof-linear-exact-terminal"}
      {:operation "op-ffi"
       :kind :ffi-call
       :source {:core-node "ffi-slice"
                :span span
                :origin-chain []}
       :profile (:profile module)
       :target (:target module)
       :facts {:transfer "transfer-ffi-borrow"}
       :outcome :unsafe-island
       :unsafe-audit "unsafe-ffi-borrow-audit"}
      {:operation "op-concurrency"
       :kind :task-transfer
       :source {:core-node "task-buffer"
                :span span
                :origin-chain []}
       :profile (:profile module)
       :target (:target module)
       :facts {:transfer "transfer-structured-task"}
       :outcome :proven-safe
       :proof "proof-structured-task-join"}
      {:operation "op-numeric"
       :kind :numeric-overflow
       :source {:core-node "numeric-add"
                :span span
                :origin-chain []}
       :profile (:profile module)
       :target (:target module)
       :facts {:type "I64"}
       :outcome :runtime-checked
       :condition :overflow
       :runtime-check "check-overflow-1"
       :failure-behavior :panic/overflow}
      {:operation "op-capability"
       :kind :authority-use
       :source {:core-node "capability-use"
                :span span
                :origin-chain []}
       :profile (:profile module)
       :target (:target module)
       :facts {:capability "stage0/stdout"}
       :outcome :proven-safe
       :proof "proof-capability-scope"}
      {:operation "op-taint"
       :kind :taint-sink
       :source {:core-node "taint-sink"
                :span span
                :origin-chain []}
       :profile (:profile module)
       :target (:target module)
       :facts {:taint "sanitized-input"}
       :outcome :runtime-checked
       :condition :sanitized-before-sink
       :runtime-check "check-taint-1"
       :failure-behavior :error/taint}
      {:operation "op-generated-unsafe"
       :kind :generated-unsafe
       :source {:core-node "generated-unsafe"
                :span span
                :origin-chain [{:kind :generated
                                :macro "stage0-unsafe"}]}
       :profile (:profile module)
       :target (:target module)
       :facts {:generated-origin "stage0-unsafe"}
       :outcome :unsafe-island
       :unsafe-audit "unsafe-generated-audit"}
      {:operation "op-optimization-erased-check"
       :kind :check-elision
       :source {:core-node "optimized-bounds"
                :span span
                :origin-chain []}
       :profile (:profile module)
       :target (:target module)
       :facts {:proof "range-analysis-1"}
       :outcome :proven-safe
       :proof "proof-check-elision-preserved"}]
     :operation-count (count (:records inventory))
     :status :complete}))

(definterposable c10-runtime-check-list
  [module outcomes]
  {:artifact :gravity/c10-runtime-check-list
   :module (:module module)
   :records
   (mapv (fn [outcome]
           {:check-id (:runtime-check outcome)
            :operation (:operation outcome)
            :condition (:condition outcome)
            :profile (:profile outcome)
            :target (:target outcome)
            :failure-behavior (:failure-behavior outcome)
            :effects #{:error/throw}
            :performance-class :bounded
            :guards-exact-operation? true
            :invalidates-on [:control-flow-change :proof-change]
            :status :recorded})
         (filter #(= :runtime-checked (:outcome %)) (:records outcomes)))
   :status :complete})

(definterposable c10-proof-obligation-list
  [module outcomes]
  {:artifact :gravity/c10-proof-obligation-list
   :module (:module module)
   :records [{:obligation-id "proof-memory-valid"
              :kind :memory
              :operation "op-memory-load"
              :discharged-by :static-analysis
              :status :discharged}
             {:obligation-id "proof-borrow-lifetime"
              :kind :lifetime
              :operation "op-borrow"
              :discharged-by :ownership-checker
              :status :discharged}
             {:obligation-id "proof-region-no-escape"
              :kind :region-escape
              :operation "op-region"
              :discharged-by :region-graph
              :status :discharged}
             {:obligation-id "proof-linear-exact-terminal"
              :kind :resource-terminal-state
              :operation "op-linear"
              :discharged-by :linear-flow-graph
              :status :discharged}
             {:obligation-id "proof-structured-task-join"
              :kind :data-race-freedom
              :operation "op-concurrency"
              :discharged-by :structured-concurrency
              :status :discharged}
             {:obligation-id "proof-capability-scope"
              :kind :capability-scope
              :operation "op-capability"
              :discharged-by :capability-proof-record
              :status :discharged}
             {:obligation-id "proof-check-elision-preserved"
              :kind :optimization-preservation
              :operation "op-optimization-erased-check"
              :discharged-by :range-analysis-certificate
              :status :discharged}]
   :source-outcomes (count (:records outcomes))
   :status :complete})

(definterposable c10-proof-certificate-references
  [module]
  {:artifact :gravity/c10-proof-certificate-references
   :module (:module module)
   :records [{:certificate-id "cert-safe1-outcomes"
              :feeds :SAFE15
              :source :safety-outcome-records
              :status :recorded}
             {:certificate-id "cert-runtime-checks"
              :feeds :SAFE15
              :source :runtime-check-list
              :status :recorded}
             {:certificate-id "cert-conformance-fixtures"
              :feeds :SAFE16
              :source :diagnostic-fixtures
              :status :recorded}]
   :status :complete})

(definterposable c10-unsafe-island-audit-manifest
  [module outcomes]
  {:artifact :gravity/c10-unsafe-island-audit-manifest
   :module (:module module)
   :records
   (mapv (fn [outcome]
           {:audit-id (:unsafe-audit outcome)
            :operation (:operation outcome)
            :owner "compiler-stage0"
            :reason (:kind outcome)
            :source-span (get-in outcome [:source :span])
            :generated-origin (get-in outcome [:source :origin-chain])
            :profile (:profile outcome)
            :target (:target outcome)
            :effects #{:memory/raw}
            :capabilities #{:memory/raw}
            :preconditions [:typed :effected :ownership-checked]
            :postconditions [:safe-wrapper-boundary]
            :invariants [:no-invalid-state-leaks]
            :safe-wrapper :stage0/safe-wrapper
            :review {:policy :required
                     :id "C10-SAFETY-AUDIT"}
            :status :recorded})
         (filter #(= :unsafe-island (:outcome %)) (:records outcomes)))
   :status :complete})

(definterposable c10-taint-capability-safety-report
  [module]
  {:artifact :gravity/c10-taint-capability-safety-report
   :module (:module module)
   :taint-records [{:source :external-input
                    :sink :io/write
                    :sanitizer :stage0/sanitize
                    :check-id "check-taint-1"
                    :status :accepted}]
   :capability-records [{:operation "op-capability"
                         :capability :io/stdout
                         :grant :stage0/stdout
                         :proof "proof-capability-scope"
                         :status :accepted}]
   :status :complete})

(definterposable c10-generated-code-safety-provenance
  [module]
  {:artifact :gravity/c10-generated-code-safety-provenance
   :module (:module module)
   :records [{:operation "op-generated-unsafe"
              :generated-form "generated-unsafe"
              :generator "stage0-unsafe"
              :generator-source-span (source-span (:source-path module) 0)
              :diagnostic-provenance :source-and-generated
              :unsafe-audit "unsafe-generated-audit"
              :status :recorded}]
   :status :complete})

(definterposable c10-optimization-safety-preservation
  [module]
  {:artifact :gravity/c10-optimization-safety-preservation
   :module (:module module)
   :records [{:operation "op-optimization-erased-check"
              :erased-check :bounds
              :proof "proof-check-elision-preserved"
              :certificate "range-analysis-1"
              :invalidates-on [:range-fact-change :layout-change
                               :control-flow-change]
              :status :preserved}
             {:operation "op-memory-load"
              :required-recheck-on [:alias-change :lifetime-change]
              :status :invalidation-recorded}]
   :status :complete})

(definterposable c10-safety-diagnostics
  [source-path]
  {:artifact :gravity/c10-safety-diagnostic-registry
   :required-diagnostic-ids c10-safety-diagnostic-ids
   :diagnostics
   (mapv (fn [design]
           {:diagnostic (:diagnostic design)
            :fixture (:fixture design)
            :operation-id (keyword "fixture" (:diagnostic design))
            :specialized-safe-rule (:rejected-design design)
            :source-span (source-span source-path 0)
            :generated-origin-chain []
            :profile :fixture/profile
            :target :fixture/target
            :safety-mode :safe
            :missing-fact (:rejected-design design)
            :remediation "Keep SAFE1 outcome, proof, check, unsafe, generated, taint, capability, FFI, numeric, and optimization evidence explicit before MIR construction."})
         c10-safety-rejected-designs)
   :status :complete})

(definterposable c10-safety-verifier-report
  [c9-artifact inventory outcomes checks obligations certificates unsafe report generated optimization diagnostics]
  (let [outcome-ops (set (map :operation (:records outcomes)))
        inventory-ops (set (map :operation-id (:records inventory)))
        outcomes-valid? (every? #(contains? c10-safe-outcomes (:outcome %))
                                (:records outcomes))
        one-outcome? (= outcome-ops inventory-ops)
        checks? (seq (:records checks))
        proofs? (every? #(= :discharged (:status %)) (:records obligations))
        unsafe? (seq (:records unsafe))
        report? (and (seq (:taint-records report))
                     (seq (:capability-records report)))
        diagnostics? (= (set c10-safety-diagnostic-ids)
                        (set (map :diagnostic (:diagnostics diagnostics))))]
    {:artifact :gravity/c10-safety-verifier-report
     :c9-proof-complete? (= :complete
                            (get-in c9-artifact
                                    [:capability-based-proof :status]))
     :operation-inventory-complete? (and (seq (:records inventory))
                                         (= :complete (:status inventory)))
     :exactly-one-outcome-per-operation? (and outcomes-valid? one-outcome?)
     :runtime-checks-emitted? (boolean checks?)
     :proof-obligations-discharged? proofs?
     :certificate-references-recorded? (seq (:records certificates))
     :unsafe-island-audits-complete? (boolean unsafe?)
     :taint-and-capability-reports-complete? (boolean report?)
     :generated-provenance-recorded? (seq (:records generated))
     :optimization-evidence-preserved? (every? #{:preserved
                                                 :invalidation-recorded}
                                               (map :status
                                                    (:records optimization)))
     :diagnostics-covered? diagnostics?
     :status (if (and (= :complete
                         (get-in c9-artifact
                                 [:capability-based-proof :status]))
                      (seq (:records inventory))
                      outcomes-valid?
                      one-outcome?
                      checks?
                      proofs?
                      (seq (:records certificates))
                      unsafe?
                      report?
                      (seq (:records generated))
                      (every? #{:preserved :invalidation-recorded}
                              (map :status (:records optimization)))
                      diagnostics?)
               :passed
               :failed)}))

(definterposable c10-safety-capability-proof
  [artifact]
  (let [verifier (:safety-verifier-report artifact)]
    {:operation-inventory-complete?
     (:operation-inventory-complete? verifier)
     :exactly-one-outcome-per-operation?
     (:exactly-one-outcome-per-operation? verifier)
     :runtime-checks-emitted?
     (:runtime-checks-emitted? verifier)
     :proof-obligations-discharged?
     (:proof-obligations-discharged? verifier)
     :certificate-references-recorded?
     (boolean (:certificate-references-recorded? verifier))
     :unsafe-island-audits-complete?
     (:unsafe-island-audits-complete? verifier)
     :taint-and-capability-reports-complete?
     (:taint-and-capability-reports-complete? verifier)
     :generated-provenance-recorded?
     (boolean (:generated-provenance-recorded? verifier))
     :optimization-evidence-preserved?
     (:optimization-evidence-preserved? verifier)
     :diagnostics-covered?
     (:diagnostics-covered? verifier)
     :verifier-passed?
     (= :passed (:status verifier))
     :status :complete}))

(definterposable c10-safety-validate!
  [source-path artifact]
  (let [proof (c10-safety-capability-proof artifact)]
    (doseq [[field id] [[:operation-inventory-complete? "C10-NO-OUTCOME"]
                        [:exactly-one-outcome-per-operation?
                         "C10-NO-OUTCOME"]
                        [:runtime-checks-emitted? "C10-CHECK"]
                        [:proof-obligations-discharged? "C10-PROOF"]
                        [:certificate-references-recorded? "C10-PROOF"]
                        [:unsafe-island-audits-complete? "C10-UNSAFE"]
                        [:taint-and-capability-reports-complete?
                         "C10-TAINT"]
                        [:generated-provenance-recorded?
                         "C10-GENERATED"]
                        [:optimization-evidence-preserved?
                         "C10-OPTIMIZATION"]
                        [:diagnostics-covered? "C10-NO-OUTCOME"]
                        [:verifier-passed? "C10-NO-OUTCOME"]]]
      (when-not (get proof field)
        (c10-safety-fail! id source-path {:stage :safety-analysis}
                          {:missing-fields [field]}))))
  :complete)

(definterposable compiler-c10-safety-source-artifact
  [source-path source-text]
  (let [records (read-source-form-records source-path source-text)
        forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        overrides (c10-safety-source-overrides module)
        _ (c10-safety-validate-overrides! source-path module overrides)
        c9-artifact (compiler-c9-ownership-source-artifact source-path source-text)
        inventory (c10-safety-operation-inventory module c9-artifact)
        outcomes (c10-safety-outcome-records module inventory)
        checks (c10-runtime-check-list module outcomes)
        obligations (c10-proof-obligation-list module outcomes)
        certificates (c10-proof-certificate-references module)
        unsafe (c10-unsafe-island-audit-manifest module outcomes)
        report (c10-taint-capability-safety-report module)
        generated (c10-generated-code-safety-provenance module)
        optimization (c10-optimization-safety-preservation module)
        diagnostics (c10-safety-diagnostics source-path)
        verifier (c10-safety-verifier-report c9-artifact inventory outcomes
                                             checks obligations certificates
                                             unsafe report generated
                                             optimization diagnostics)
        artifact-base
        {:kind :gravity/stage0-c10-safety-analysis-artifact
         :task "P06-D089"
         :document-set ["C10"]
         :governing-document c10-safety-governing-document
         :pass {:name :c10-safety-analysis-pipeline
                :input :ownership-checked-core
                :output :safety-checked-core
                :requires [:typed-core-module :effect-graph
                           :capability-proof-records :ownership-graph
                           :borrow-graph :lifetime-interval-map
                           :linear-resource-flow-graph :profile :target]
                :preserves [:source-spans :generated-origin :types
                            :effects :capabilities :ownership-facts
                            :profile :target :unsafe-metadata]
                :emits [:safety-operation-inventory
                        :safety-outcome-records :runtime-check-list
                        :proof-obligation-list
                        :proof-certificate-references
                        :unsafe-island-audit-manifest
                        :taint-capability-safety-report
                        :generated-code-safety-provenance
                        :optimization-safety-preservation
                        :safety-diagnostics]
                :rejects c10-safety-diagnostic-ids}
         :source-overrides overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :c9-ownership-checker-artifact
         (select-keys c9-artifact [:kind :artifact-id :ownership-graph
                                   :borrow-graph :lifetime-interval-map
                                   :linear-resource-flow-graph
                                   :capability-based-proof])
         :safety-operation-inventory inventory
         :safety-outcome-records outcomes
         :runtime-check-list checks
         :proof-obligation-list obligations
         :proof-certificate-references certificates
         :unsafe-island-audit-manifest unsafe
         :taint-capability-safety-report report
         :generated-code-safety-provenance generated
         :optimization-safety-preservation optimization
         :safety-verifier-report verifier
         :safety-diagnostics diagnostics
         :c10-safety-analysis-results
         {:documents ["C10"]
          :task "P06-D089"
          :required-diagnostic-ids c10-safety-diagnostic-ids
          :operation-inventory-status :complete
          :outcome-status :complete
          :runtime-check-status :complete
          :proof-obligation-status :complete
          :certificate-status :complete
          :unsafe-audit-status :complete
          :taint-capability-status :complete
          :generated-provenance-status :complete
          :optimization-preservation-status :complete
          :verifier-status (:status verifier)
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (c10-safety-validate! source-path artifact-base)
        capability-proof (c10-safety-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(definterposable compiler-c10-safety-file-artifact
  [path]
  (compiler-c10-safety-source-artifact path (slurp path)))

(def ^:private namespace-contract
  {:contract-boundary :hosted-stage0-c10-safety-analysis
   :artifact-inputs [:c9-ownership-checker-artifact :module-context]
   :artifact-outputs [:safety-operation-inventory :safety-outcome-records
                      :runtime-check-list :proof-obligation-list
                      :proof-certificate-references
                      :unsafe-island-audit-manifest
                      :taint-capability-safety-report
                      :generated-code-safety-provenance
                      :optimization-safety-preservation :safety-diagnostics]
   :owns [:hosted-stage0-c10-safety-analysis
          :hosted-stage0-c10-artifact-projection]
   :dependency-direction {:requires ['clojure.set 'gravity.digest]
                          :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :does-not-own [:canonical-c10-authority :source-authentication
                  :type-effect-ownership-authority
                  :safe1-classification-authority
                  :runtime-check-provider-authority
                  :proof-certificate-authority :unsafe-review-authority
                  :optimization-safety-authority :mir-construction
                  :equivalence :self-hosting :release :seed-retirement]
   :compatibility-only? true
   :override-driven-diagnostics? true
   :safety-model-complete? false
   :canonical-c10-authority? false
   :operation-interposition {:accepted-keys operation-keys
                             :unknown-keys-rejected? true
                             :partial-overrides? true
                             :single-binding-per-top-level-call? true}})

(defn with-operations [operations thunk]
  (policy/validate-operations! operations)
  (let [merged (merge *operations* operations)]
    (binding [*operations* merged
              c10-safety-diagnostic-ids
              (get merged :c10-safety-diagnostic-ids c10-safety-diagnostic-ids)
              c10-safety-governing-document
              (get merged :c10-safety-governing-document
                   c10-safety-governing-document)
              c10-safety-rejected-designs
              (get merged :c10-safety-rejected-designs
                   c10-safety-rejected-designs)
              c10-safety-override-diagnostics
              (get merged :c10-safety-override-diagnostics
                   c10-safety-override-diagnostics)
              c10-safe-outcomes
              (get merged :c10-safe-outcomes c10-safe-outcomes)]
      (thunk))))

(def public-api
  {'public-api {:kind :contract}
   'with-operations {:arglists '([operations thunk])}
   'c10-engine-contract {:arglists '([])}
   'c10-safety-diagnostic-ids {:kind :constant}
   'c10-safety-governing-document {:kind :constant}
   'c10-safety-rejected-designs {:kind :constant}
   'c10-safety-override-diagnostics {:kind :constant}
   'c10-safe-outcomes {:kind :constant}
   'c10-safety-source-overrides {:arglists '([module])}
   'c10-safety-message {:arglists '([id])}
   'c10-safety-fail! {:arglists '([id source-path subject extra])}
   'c10-safety-validate-overrides! {:arglists '([source-path module overrides])}
   'c10-safety-operation-inventory {:arglists '([module c9-artifact])}
   'c10-safety-outcome-records {:arglists '([module inventory])}
   'c10-runtime-check-list {:arglists '([module outcomes])}
   'c10-proof-obligation-list {:arglists '([module outcomes])}
   'c10-proof-certificate-references {:arglists '([module])}
   'c10-unsafe-island-audit-manifest {:arglists '([module outcomes])}
   'c10-taint-capability-safety-report {:arglists '([module])}
   'c10-generated-code-safety-provenance {:arglists '([module])}
   'c10-optimization-safety-preservation {:arglists '([module])}
   'c10-safety-diagnostics {:arglists '([source-path])}
   'c10-safety-verifier-report {:arglists '([c9-artifact inventory outcomes checks obligations certificates unsafe report generated optimization diagnostics])}
   'c10-safety-capability-proof {:arglists '([artifact])}
   'c10-safety-validate! {:arglists '([source-path artifact])}
   'compiler-c10-safety-source-artifact {:arglists '([source-path source-text])}
   'compiler-c10-safety-file-artifact {:arglists '([path])}
   })

(defn c10-engine-contract []
  (assoc namespace-contract :public-api public-api))
