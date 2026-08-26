(ns gravity.self-hosting.sh07-c10-safety-source-coverage-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as string]
            [clojure.test :refer [deftest is testing]]
            [clojure.walk :as walk]
            [gravity.bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_c10_safety_source_coverage_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-07 C10 safety source test is not on the classpath"
        {:id "SH07-C10-COVERAGE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH07-C10-COVERAGE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private c10-relative-path
  "bootstrap/gravity/src/gravity/compiler/c10_safety_analysis_pipeline.gravity")
(def ^:private proof-contract-relative-path
  "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")
(def ^:private expected-source-byte-count 196156)
(def ^:private expected-source-revision-id
  "sha256:c232a59c64affd4c64bd6c679d844a47c9a8dffcb588480ef83ee1372d82554d")
(def ^:private expected-sh06-semantic-projection-id
  "sha256:13312eba92eb8c173719a8ef426c46c712e391b114e348969321973f2f6cd0a1")
(def ^:private expected-coverage
  {:fragment-count 162
   :root-form-count 162
   :form-count 15853
   :binding-count 1045
   :resolution-count 5653})
(def ^:private expected-core-census
  {:core-node-count 13557
   :definition-count 162
   :call-count 2355
   :reference-count 4494
   :keyword-lookup-count 0
   :core-form-frequencies
   {:literal 5035
    :collection-literal 514
    :def 162
    :reference 4494
    :call 2355
    :if 707
    :let 96
    :loop 16
    :recur 20
    :quote 3
    :fn 155}})
(def ^:private expected-census-measurements
  {:fragments 73
   :top-level-forms 73
   :forms 5652
   :bindings 542
   :resolutions 2046
   :maximum-fragment-forms 374
   :maximum-fragment-resolutions 151
   :maximum-fragment-local-bindings 28
   :maximum-fragment-external-bindings 22
   :maximum-form-children 38
   :maximum-form-depth 36
   :carrier-nodes 380720
   :carrier-depth 38
   :carrier-width 5652
   :carrier-exact-utf8-scalar-bytes 6314034
   :carrier-scalar-bytes 25243788
   :predicted-maximum-core-nodes 5652
   :predicted-maximum-digest-requests 5656})
(def ^:private expected-export-names
  '[c10-safety-analysis-contract
    c10-operation-inventory-contract
    c10-outcome-record-contract
    c10-runtime-proof-unsafe-contract
    c10-generated-taint-capability-contract
    c10-ffi-numeric-optimization-contract
    c10-safety-diagnostic-catalog
    build-c10-safety-operation
    build-c10-safety-outcome
    verify-c10-safety-analysis
    sh11-safety-policy
    sh11-classify-operation
    sh11-verify-safety-result
    sh11-authenticated-safety-adapter-policy
    sh11-authenticated-c9-input-valid?
    sh11-build-authenticated-safety-core
    sh11-verify-authenticated-safety-core
    sh11-authenticated-safety-identity-requests
    sh11-authenticated-safety-result-identity-request
    sh11-authenticated-safety-core-identity-request
    sh11-bind-authenticated-safety-identities
    sh11-verify-authenticated-safety-identities
    sh11-authenticated-division-bounds-policy
    sh11-authenticated-division-bounds-check-request
    sh11-classify-authenticated-division-bounds
    sh11-verify-authenticated-division-bounds-result
    sh11-authenticated-overflow-cast-policy
    sh11-authenticated-overflow-cast-check-request
    sh11-classify-authenticated-overflow-cast
    sh11-verify-authenticated-overflow-cast-result])
(def ^:private expected-definition-names
  '#{c10-safety-analysis-contract
     c10-operation-inventory-contract
     c10-outcome-record-contract
     c10-runtime-proof-unsafe-contract
     c10-generated-taint-capability-contract
     c10-ffi-numeric-optimization-contract
     c10-safety-diagnostic-catalog
     build-c10-safety-operation
     build-c10-safety-outcome
     verify-c10-safety-analysis
     sh11-safety-policy sh11-not sh11-and sh11-member? sh11-boolean?
     sh11-exact-keys? sh11-nonempty-string? sh11-lowercase-hex?
     sh11-sha256-id? sh11-positive-integer? sh11-nonnegative-integer?
     sh11-bounded-positive-width? sh11-width-minimum sh11-width-maximum
     sh11-nonempty-keyword-set? sh11-keyword-set?
     sh11-nonempty-keyword-vector? sh11-sha-vector? sh11-subset?
     sh11-valid-source-span? sh11-aggregate? sh11-aggregate-width
     sh11-aggregate-components sh11-max sh11-structural-preflight
     sh11-in-range? sh11-mode-valid-for-kind? sh11-valid-bounds?
     sh11-index-facts-valid? sh11-overflow-facts-valid?
     sh11-division-facts-valid? sh11-cast-mode-coherent?
     sh11-cast-facts-valid? sh11-shift-facts-valid?
     sh11-operation-facts-valid? sh11-valid-request-shape?
     sh11-add-safe? sh11-subtract-safe? sh11-multiply-safe?
     sh11-overflow-operation-safe? sh11-overflow-result sh11-static-safe?
     sh11-runtime-condition sh11-predicate-expression
     sh11-predicate-operands sh11-runtime-mode-valid?
     sh11-failure-and-effects-valid? sh11-runtime-support-valid?
     sh11-specialized-rule sh11-runtime-check-valid? sh11-unsafe-mode?
     sh11-unsafe-audit-valid? sh11-evidence-count sh11-diagnostic
     sh11-outcome-record sh11-result sh11-proven-result
     sh11-runtime-checked-result sh11-rejected-result sh11-unsafe-result
     sh11-bounds-rejected sh11-classify-operation
     sh11-verify-safety-result
     sh11-authenticated-adapter-diagnostic
     sh11-authenticated-adapter-pending
     sh11-authenticated-c9-input-valid?
     sh11-authenticated-division-bounds-check-request
     sh11-authenticated-division-bounds-policy
     sh11-authenticated-identity-components
     sh11-authenticated-identity-preflight
     sh11-authenticated-load-operation-valid?
     sh11-authenticated-load-value-type?
     sh11-authenticated-overflow-cast-check-request
     sh11-authenticated-overflow-cast-policy
     sh11-authenticated-safety-adapter-policy
     sh11-authenticated-safety-core-identity-request
     sh11-authenticated-safety-identity-requests
     sh11-authenticated-safety-result-identity-request
     sh11-bind-authenticated-safety-identities
     sh11-build-authenticated-safety-core
     sh11-c9-binding-checks
     sh11-c9-binding-verification-valid?
     sh11-c9-bound-shape-valid?
     sh11-c9-read-proof-request
     sh11-c9-read-record-exactly-linked?
     sh11-c9-read-record-valid?
     sh11-c9-upstream-value-type
     sh11-cast-contract-valid?
     sh11-classify-authenticated-division-bounds
     sh11-classify-authenticated-load
     sh11-classify-authenticated-overflow-cast
     sh11-division-bounds-basic-descriptor-valid?
     sh11-division-bounds-candidate-shape-valid?
     sh11-division-bounds-descriptor-operands-valid?
     sh11-division-bounds-diagnostic
     sh11-division-bounds-operand-binding
     sh11-division-bounds-operand-bindings
     sh11-division-bounds-operand-reference
     sh11-division-bounds-rejected-result
     sh11-division-bounds-result
     sh11-division-bounds-runtime-descriptor-valid?
     sh11-division-bounds-runtime-record
     sh11-division-bounds-specialized-rule
     sh11-division-bounds-template-rejection
     sh11-division-bounds-verification-diagnostic
     sh11-load-facts-valid?
     sh11-load-fields-valid?
     sh11-load-proof-id-request-from-operation
     sh11-load-proof-request-valid?
     sh11-load-request-mode-valid?
     sh11-overflow-cast-basic-descriptor-valid?
     sh11-overflow-cast-binding-context-valid?
     sh11-overflow-cast-candidate-shape-valid?
     sh11-overflow-cast-descriptor-operands-valid?
     sh11-overflow-cast-diagnostic
     sh11-overflow-cast-diagnostic-context
     sh11-overflow-cast-execute-guard-case
     sh11-overflow-cast-expected-execution-evidence
     sh11-overflow-cast-numeric-contract-valid?
     sh11-overflow-cast-operand-bindings
     sh11-overflow-cast-operand-references
     sh11-overflow-cast-read-records
     sh11-overflow-cast-read-records-valid?
     sh11-overflow-cast-rejected-result
     sh11-overflow-cast-result
     sh11-overflow-cast-runtime-descriptor-valid?
     sh11-overflow-cast-runtime-record
     sh11-overflow-cast-specialized-rule
     sh11-overflow-cast-target-support-valid?
     sh11-overflow-cast-template-rejection
     sh11-overflow-cast-typed-operation-contract
     sh11-overflow-cast-typed-operation-fields-match?
     sh11-overflow-cast-typed-operation-valid?
     sh11-overflow-cast-verification-diagnostic
     sh11-overflow-contract-valid?
     sh11-proven-proof-assumptions
     sh11-proven-proof-id-request
     sh11-proven-proof-method
     sh11-proven-proof-provider
     sh11-request-fact-links-valid?
     sh11-request-runtime-support-valid?
     sh11-resolution-entry-valid?
     sh11-resolved-load-operation
     sh11-result-scope
     sh11-single-c9-read-record
     sh11-two-c9-integer-reads-valid?
     sh11-two-c9-read-records
     sh11-verify-authenticated-division-bounds-result
     sh11-verify-authenticated-load-result
     sh11-verify-authenticated-overflow-cast-result
     sh11-verify-authenticated-safety-core
     sh11-verify-authenticated-safety-identities})
(def ^:private quoted-definition-names
  '#{build-c10-safety-operation build-c10-safety-outcome
     verify-c10-safety-analysis})
(def ^:private data-definition-names
  '#{c10-safety-analysis-contract c10-operation-inventory-contract
     c10-outcome-record-contract c10-runtime-proof-unsafe-contract
     c10-generated-taint-capability-contract
     c10-ffi-numeric-optimization-contract c10-safety-diagnostic-catalog})
(def ^:private expected-executable-sh11-names
  (set/difference expected-definition-names
                  data-definition-names
                  quoted-definition-names))
(def ^:private expected-document-ids
  ["C10" "SAFE1" "SAFE2" "SAFE3" "SAFE4" "SAFE5" "SAFE6"
   "SAFE7" "SAFE8" "SAFE9" "SAFE10" "SAFE11" "SAFE12" "SAFE13"
   "SAFE14" "SAFE15" "SAFE16" "C7" "C8" "C9" "C11" "C15" "D8"
   "D9" "P1" "P6" "P7" "PERF10" "R1" "R11" "PKG6" "PKG7"
   "BOOT1" "BOOT2" "BOOT3" "BOOT4" "BOOT5" "BOOT6" "BOOT7"
   "BOOT8" "TEST2"])
(def ^:private expected-policy
  {:artifact :gravity/sh11-safety-policy
   :version 2
   :request-artifact :gravity/sh11-normalized-safety-operation
   :result-artifact :gravity/sh11-safety-classification-result
   :operation-kinds #{:index :numeric-overflow :division :numeric-cast :shift}
   :outcomes #{:proven-safe :runtime-checked :rejected :unsafe-island}
   :runtime-conditions
   {:index :bounds
    :numeric-overflow :overflow
    :division :valid-divisor-and-quotient
    :numeric-cast :representable-cast
    :shift :shift-range}
   :specialized-rules
   {:index :bounds-safety
    :numeric-overflow :SAFE9-OVERFLOW
    :division :SAFE9-DIV-ZERO
    :numeric-cast :SAFE9-NARROW
    :shift :SAFE9-SHIFT}
   :legal-modes
   {:index #{:proof-required :checked :panic :unsafe-unchecked}
    :numeric-overflow
    #{:proof-required :checked :panic :wrapping :saturating
      :arbitrary-precision :unsafe-unchecked}
    :division #{:proof-required :checked :panic :unsafe-unchecked}
    :numeric-cast
    #{:proof-required :checked :panic :wrapping :saturating :unsafe-unchecked}
    :shift #{:proof-required :checked :panic :wrapping :unsafe-unchecked}}
   :structural-bounds
   {:maximum-nodes 2048
    :maximum-depth 16
    :maximum-width 64
    :maximum-scalars 1600
    :maximum-scalar-units 16384
    :maximum-collections 512}
   :diagnostics ["C10-NO-OUTCOME" "C10-CHECK" "C10-UNSAFE" "C10-NUMERIC"]
   :pending [:memory-safety
             :ownership-and-lifetime-safety
             :region-and-arena-safety
             :linear-resource-safety
             :ffi-safety
             :concurrency-and-race-safety
             :capability-and-taint-safety
             :generated-code-safety
             :elementary-and-floating-safety
             :optimization-invalidation
             :authenticated-non-persistent-read-safety-families
             :mir-preservation]})
(def ^:private expected-diagnostic-catalog
  {:diagnostics ["C10-NO-OUTCOME" "C10-PROOF" "C10-CHECK" "C10-UNSAFE"
                 "C10-GENERATED" "C10-TAINT" "C10-CAPABILITY" "C10-FFI"
                 "C10-NUMERIC" "C10-OPTIMIZATION"]
   :stable-diagnostic-fields
   [:diagnostic-id :severity :operation-id :specialized-safe-rule
    :source-span :generated-origin-chain :profile :target :safety-mode
    :missing-fact :proof-id :runtime-check-id :remediation]
   :stops [:mir-construction :optimizer :target-lowering
           :runtime-selection :package-release :safety-certificate]
   :must-not-depend-on
   [:localized-rendering-text :backend-undefined-behavior
    :host-runtime-accident :optimizer-assumption :implicit-unsafe-policy]})
(def ^:private expected-contract-structures
  {'c10-safety-analysis-contract
   {:artifact :gravity/c10-safety-analysis-contract
    :required-fields
    [:module :operations :outcomes :runtime-checks :proof-obligations
     :certificate-references :unsafe-islands :taint-report
     :capability-report :generated-code-provenance :ffi-boundaries
     :numeric-safety :optimization-invalidation :diagnostics]
    :required-input-facts
    [:type-facts :effect-facts :capability-facts :ownership-facts
     :profile-validation-facts :generated-origin-chains]
    :must-compute
    [:safety-operation-inventory :exactly-one-safe1-outcome-per-operation
     :runtime-check-list :proof-obligation-list :certificate-reference-list
     :unsafe-island-audit-manifest :taint-and-capability-safety-report
     :generated-code-safety-provenance
     :optimization-proof-preservation-report]
    :must-preserve
    [:core-node-identity :source-spans :type-facts :effect-facts
     :capability-facts :ownership-facts :artifact-provenance]
    :forbidden
    [:lint-only-safety-analysis :backend-only-safety-legalization
     :runtime-check-with-undefined-failure :unsafe-operation-without-audit
     :generated-code-dropping-safety-provenance
     :optimization-erasing-check-without-proof]}
   'c10-operation-inventory-contract
   {:artifact :gravity/c10-operation-inventory-contract
    :operation-kinds
    [:load :store :allocation :deallocation :pointer-conversion
     :initialization :moved-field-read :index :slice :numeric-overflow
     :division :shift :numeric-cast :elementary-approximation :borrow :move
     :consume :region :arena :linear-resource :ffi-call :host-interop
     :concurrency :atomic :lock :task-capture :channel :capability-use
     :taint-sink :sanitizer-boundary :macro-generated-unsafe :ai-tool-call
     :supply-chain-import :certificate-trust-point]
    :required-fields
    [:operation-id :kind :core-node-id :source-span :generated-origin-chain
     :profile :target :type-fact-id :effect-fact-id :capability-proof-id
     :ownership-fact-id :safety-mode]
    :downstream-consumers
    [:mir-construction :optimizer :target-lowering :runtime-selection
     :package-release :safety-certificate]
    :forbidden
    [:operation-without-core-node :operation-without-source-span
     :generated-operation-without-origin :profile-or-target-omitted
     :safety-sensitive-operation-uninventoried]}
   'c10-outcome-record-contract
   {:artifact :gravity/c10-outcome-record-contract
    :outcomes [:proven-safe :runtime-checked :rejected :unsafe-island]
    :required-fields
    [:operation-id :outcome :source :profile :target :safety-mode
     :specialized-safe-rule :facts :proof :runtime-check :unsafe-audit
     :diagnostic]
    :outcome-rules
    [:exactly-one-outcome-per-operation
     :proven-safe-requires-proof-or-static-facts
     :runtime-checked-requires-check-record
     :rejected-requires-stable-diagnostic
     :unsafe-island-requires-audit-record
     :generated-code-outcomes-preserve-origin]
    :forbidden
    [:missing-outcome :multiple-outcomes :proven-safe-without-evidence
     :runtime-checked-without-check :unsafe-island-without-policy
     :rejected-without-diagnostic]}
   'c10-runtime-proof-unsafe-contract
   {:artifact :gravity/c10-runtime-proof-unsafe-contract
    :runtime-check-fields
    [:check-id :condition :operation-id :emitted-location :profile :target
     :failure-behavior :effects-introduced :performance-class :guard-proof
     :invalidation-conditions]
    :proof-obligation-fields
    [:proof-id :claim :operation-id :source-span :assumptions :method
     :provider :profile :target :result]
    :unsafe-island-fields
    [:audit-id :operation :owner :reason :source-span
     :generated-origin-chain :profile :target :effects :capabilities
     :preconditions :postconditions :invariants :evidence :safe-wrapper
     :review :re-review]
    :forbidden
    [:check-without-defined-failure :proof-without-checker-or-provider
     :certificate-reference-without-trust-status
     :unsafe-island-missing-owner :unsafe-island-missing-safe-boundary
     :unsafe-island-hidden-from-artifacts]}
   'c10-generated-taint-capability-contract
   {:artifact :gravity/c10-generated-taint-capability-contract
    :generated-origin-fields
    [:generated-form :generator :source-form :macro-or-tool
     :model-or-provider :origin-chain]
    :taint-report-fields
    [:taint-source :taint-category :flow-path :validator :sink
     :residual-constraints :secret-redaction]
    :capability-report-fields
    [:requested-capability :provider :grant :scope :policy-layer :phase
     :runtime-check]
    :rules
    [:generated-unsafe-diagnoses-generated-and-generator-spans
     :taint-facts-survive-to-sinks
     :secret-taint-redacted-from-public-artifacts
     :capability-use-covered-by-proof-or-check
     :ambient-authority-rejected-in-constrained-profiles]
    :forbidden
    [:generated-unsafe-without-origin :taint-sink-without-validator-or-policy
     :secret-in-diagnostic :capability-from-effect-name
     :ambient-authority-by-default]}
   'c10-ffi-numeric-optimization-contract
   {:artifact :gravity/c10-ffi-numeric-optimization-contract
    :ffi-fields
    [:boundary-id :foreign-symbol :abi :ownership-transfer :nullability
     :lifetime :error-model :capabilities :unsafe-audit]
    :numeric-fields
    [:operation-id :numeric-mode :overflow-policy :division-policy
     :shift-policy :cast-policy :elementary-domain-proof
     :runtime-check-or-proof]
    :optimization-fields
    [:transform-id :affected-operation :preserved-facts :invalidated-facts
     :replacement-proof :restored-runtime-check :diagnostic-on-gap]
    :rules
    [:ffi-boundary-has-safety-facts :numeric-operation-has-proof-or-check
     :optimization-invalidates-or-preserves-proof
     :check-elision-requires-certificate :backend-receives-safety-facts]
    :forbidden
    [:ffi-call-without-boundary-record :numeric-mode-implicit-fast-path
     :optimization-using-stale-evidence :check-erasure-without-proof
     :backend-assumption-as-safety-evidence]}})
(def ^:private expected-rejection-tuples
  #{["C10-CHECK" :invalid-runtime-check
     :defined-profile-legal-runtime-check :supply-an-exact-operand-bound-check]
    ["C10-CHECK" :target-support-without-runtime-check
     :runtime-check :remove-unused-support-or-supply-a-check]
    ["C10-NO-OUTCOME" :illegal-outcome
     :one-of-four-safe1-outcomes :use-a-defined-safe1-outcome]
    ["C10-NO-OUTCOME" :malformed-safety-operation
     :complete-normalized-operation
     :provide-all-operation-identity-profile-and-fact-fields]
    ["C10-NO-OUTCOME" :multiple-outcome-evidence
     :exclusive-safety-path :retain-exactly-one-proof-check-or-unsafe-path]
    ["C10-NO-OUTCOME" :outcome-count-mismatch
     :exactly-one-outcome :emit-one-and-only-one-outcome]
    ["C10-NO-OUTCOME" :safety-result-substitution
     :complete-recomputed-result :recompute-from-the-normalized-operation]
    ["C10-NUMERIC" :numeric-operation-lacks-proof-check-or-audit
     '(sh11-specialized-rule (get request :kind))
     :prove-check-or-explicitly-audit-the-operation]
    ["C10-UNSAFE" :unsafe-island-policy-or-metadata-gap
     :complete-policy-approved-unsafe-audit
     :supply-a-complete-audit-in-an-unsafe-capable-mode]
    ["C10-UNSAFE" :unsafe-island-without-unsafe-numeric-mode
     :unsafe-unchecked-numeric-mode :declare-the-unsafe-numeric-mode-explicitly]
    ["C10-UNSAFE" :unsafe-mode-cannot-use-runtime-check
     :complete-policy-approved-unsafe-audit
     :use-an-unsafe-island-not-a-runtime-check]})
(def ^:private expected-structural-reasons
  #{:structure-node-bound :structure-depth-bound :structure-width-bound
    :structure-scalar-count-bound :structure-scalar-unit-bound
    :structure-collection-bound :request-structural-bound
    :candidate-structural-bound})
(def ^:private expected-b16-bounds
  {:maximum-module-forms 65536
   :maximum-module-core-nodes 65536
   :maximum-fragments 1024
   :maximum-top-level-forms 1024
   :maximum-fragment-forms 1024
   :maximum-form-children 1024
   :maximum-form-depth 256
   :maximum-bindings 2048
   :maximum-module-bindings 2440
   :maximum-alias-records 256
   :maximum-fragment-resolutions 2048
   :maximum-module-resolutions 65536
   :maximum-origin-entries 256
   :maximum-metadata-entries 256
   :maximum-effects 128
   :maximum-capabilities 128
   :maximum-exports 1024
   :maximum-module-carrier-nodes 8388608
   :maximum-module-carrier-depth 256
   :maximum-module-carrier-width 65536
   :maximum-module-scalar-bytes 268435456
   :maximum-template-carrier-nodes 33554432
   :maximum-template-carrier-depth 256
   :maximum-template-carrier-width 65536
   :maximum-template-scalar-bytes 1073741824
   :maximum-resolved-core-carrier-nodes 16777216
   :maximum-resolved-core-carrier-depth 256
   :maximum-resolved-core-carrier-width 65536
   :maximum-resolved-core-scalar-bytes 1073741824
   :maximum-generated-digest-carrier-nodes 16777216
   :maximum-generated-digest-carrier-depth 256
   :maximum-generated-digest-carrier-width 65536
   :maximum-generated-digest-scalar-bytes 1073741824
   :maximum-module-digest-requests 65540})
(def ^:private rejected-families
  {"core-shape" "C6-CORE-SHAPE"
   "lowering-gap" "C6-LOWERING-GAP"})
(def ^:private zero-id (str "sha256:" (apply str (repeat 64 "0"))))

(defn- path [relative] (str (.resolve @root relative)))

(defn- required-var
  [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw
       (ex-info
        "Required SH-07-B31 coordinator adapter is absent"
        {:id "SH07-C10-COVERAGE-ADAPTER-ABSENT" :symbol symbol}))))

(defn- source-bytes
  [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))

(defn- source-forms
  []
  (with-open
   [reader (clojure.lang.LineNumberingPushbackReader.
            (io/reader (path c10-relative-path)))]
    (loop [forms []]
      (let [form (read {:eof ::eof} reader)]
        (if (= ::eof form) forms (recur (conj forms form)))))))

(defn- named-top-level-form
  [forms kind name]
  (first (filter #(and (seq? %) (= kind (first %)) (= name (second %))) forms)))

(defn- quoted-body
  [definition-form]
  (let [body (nth definition-form 3 nil)]
    (when (and (seq? body) (= 'quote (first body))) (second body))))

(defn- collect-calls
  [operator value]
  (let [found (volatile! [])]
    (walk/postwalk
     (fn [entry]
       (when (and (seq? entry) (= operator (first entry)))
         (vswap! found conj entry))
       entry)
     value)
    @found))

(defn- invalid-source-if-forms
  [forms]
  (vec
   (filter #(not= 4 (count %))
           (mapcat #(collect-calls 'if %) forms))))

(defn- undefined-exported-symbols
  [forms]
  (let [namespace-form (first forms)
        export-clauses
        (filter #(and (seq? %) (= :exports (first %)))
                (drop 2 namespace-form))
        export-clause (first export-clauses)
        export-values (second export-clause)
        valid-export-clause?
        (and (seq? namespace-form)
             (= 'ns (first namespace-form))
             (symbol? (second namespace-form))
             (= 1 (count export-clauses))
             (= 2 (count export-clause))
             (vector? export-values)
             (every? symbol? export-values)
             (= (count export-values) (count (set export-values))))
        exported (set export-values)
        defined
        (set (keep (fn [form]
                     (when (and (seq? form)
                                (#{'def 'defn} (first form))
                                (symbol? (second form)))
                       (second form)))
                   (rest forms)))]
    (if valid-export-clause?
      (set/difference exported defined)
      #{::invalid-exports-clause})))

(defn- symbols-in
  [value]
  (let [found (volatile! #{})]
    (walk/postwalk
     (fn [entry]
       (when (symbol? entry) (vswap! found conj entry))
       entry)
     value)
    @found))

(defn- sha256-id
  [bytes]
  (let [digest (.digest (java.security.MessageDigest/getInstance "SHA-256") bytes)]
    (str "sha256:"
         (apply str (map #(format "%02x" (bit-and 0xff %)) digest)))))

(defn- delete-tree!
  [root-path]
  (when (java.nio.file.Files/exists root-path
                                    (make-array java.nio.file.LinkOption 0))
    (with-open [stream (java.nio.file.Files/walk
                       root-path (make-array java.nio.file.FileVisitOption 0))]
      (doseq [entry (reverse (iterator-seq (.iterator stream)))]
        (java.nio.file.Files/deleteIfExists entry)))))

(defn- request [artifact]
  (get-in artifact [:gravity-core-boundary :authenticated-core-request]))
(defn- core [artifact]
  (get-in artifact [:gravity-core-boundary :canonical-core-artifact]))
(defn- identity-input [artifact]
  ((required-var 'sh07-core-artifact-identity-input) artifact))

(defn- coverage
  [artifact]
  (let [authenticated-request (request artifact)]
    {:fragment-count (count (:fragment-manifest authenticated-request))
     :root-form-count (count (:top-level-form-ids authenticated-request))
     :form-count (count (:forms authenticated-request))
     :binding-count (count (:binding-table authenticated-request))
     :resolution-count (count (:resolution-table authenticated-request))}))

(defn- core-census
  [artifact]
  (let [core-artifact (core artifact)
        nodes (:nodes core-artifact)]
    {:core-node-count (count nodes)
     :definition-count (count (:definitions core-artifact))
     :call-count (count (:calls core-artifact))
     :reference-count (count (:reference-uses core-artifact))
     :keyword-lookup-count (count (:keyword-lookups core-artifact))
     :core-form-frequencies (frequencies (map :core-form nodes))}))

(defn- exactly-once-index
  [records key-name]
  (let [index (into {} (map (juxt key-name identity)) records)]
    (when-not (= (count records) (count index))
      (throw
       (ex-info
        "SH-07-B31 records are not uniquely identifiable"
        {:id "SH07-C10-COVERAGE-AMBIGUOUS-INDEX"
         :key key-name :record-count (count records)
         :unique-count (count index)})))
    index))

(defn- diagnostic-result
  [operation]
  (try
    {:value (operation)}
    (catch clojure.lang.ExceptionInfo exception
      {:exception-data (ex-data exception)})
    (catch Throwable throwable
      {:raw-host-error {:class (.getName (class throwable))
                        :message (.getMessage throwable)}})))

(defn- diagnostic-data
  [result]
  (let [data (:exception-data result)
        value (:value result)]
    (or (when (= :gravity/sh07-core-diagnostic (:artifact data)) data)
        (when (= :gravity/sh07-core-diagnostic
                 (get-in data [:diagnostic :artifact]))
          (:diagnostic data))
        (when (= :gravity/sh07-core-diagnostic (:artifact value)) value)
        (when (= :gravity/sh07-core-diagnostic
                 (get-in value [:diagnostic :artifact]))
          (:diagnostic value)))))

(def ^:private c10-artifact
  ;; Delayed so namespace loading and static tests never start SH-07.
  (delay ((required-var 'sh07-core-file-artifact) (path c10-relative-path))))

(def ^:private c10-upstream-verification
  (delay
    ((required-var 'sh06-resolution-artifact-verification)
     (:sh06-resolution-artifact @c10-artifact))))

(def ^:private parity-artifacts
  (delay
    (let [temp-root
          (java.nio.file.Files/createTempDirectory
           "gravity-sh07-c10-coverage-"
           (make-array java.nio.file.attribute.FileAttribute 0))
          right-path (.resolve temp-root "right/c10_safety_analysis_pipeline.qst")
          left-path (path c10-relative-path)
          bytes (source-bytes left-path)]
      (try
        (java.nio.file.Files/createDirectories
         (.getParent right-path)
         (make-array java.nio.file.attribute.FileAttribute 0))
        (java.nio.file.Files/write right-path bytes
                                   (make-array java.nio.file.OpenOption 0))
        {:left @c10-artifact
         :right ((required-var 'sh07-core-file-artifact) (str right-path))
         :left-path left-path
         :right-path (str right-path)}
        (finally (delete-tree! temp-root))))))

(deftest sh07-b31-proof-contract-registers-c10-source-exactly
  (let [contract (edn/read-string (slurp (path proof-contract-relative-path)))
        expectation
        (get-in contract
                [:authoritative-coverage-census
                 :module-expectations :c10-safety])
        nonclaims (set (:nonclaims contract))]
    (is (= "SH-07-B47" (:coverage-milestone contract)))
    (is (= c10-relative-path
           (get-in contract [:authoritative-modules :c10-safety])))
    (is (= {:keyword-lookups 0}
           (get-in contract [:required-core-product-counts :c10-safety])))
    (is (= expected-b16-bounds (:bounds contract)))
    (is (= {:request-schema-version 15
            :task "SH-07-B47"
            :input-task "SH-07-B15"
            :scope :sh07-b15-keyword-map-lookup
            :adapter :gravity/sh07-to-c6-core-products-v16
            :fresh-authoritative-process-required true
            :iteration-cache-authoritative false}
           (:boundary contract)))
    (is (= {:module-namespace
            'gravity.compiler.c10-safety-analysis-pipeline
            :source-binding
            {:source-byte-count expected-source-byte-count
             :source-bytes-sha256 expected-source-revision-id}}
           expectation))
    (is (not (contains? expectation :request-counts)))
    (is (not (contains? expectation :core-counts)))
    (doseq [nonclaim
            [:c10-production-safety-analysis-execution
             :c10-contract-and-diagnostic-schema-enforcement
             :sh11-authenticated-sh09-sh10-convergence
             :sh11-memory-lifetime-region-and-linear-safety
             :sh11-ffi-concurrency-taint-and-generated-code-safety
             :sh11-floating-point-and-elementary-function-safety
             :sh11-optimization-invalidation-and-mir-preservation
             :sh11-complete :sh07-complete :seed-retirement
             :self-hosting-complete]]
      (is (contains? nonclaims nonclaim)))))

(deftest sh07-b31-c10-source-control-form-arities-are-bounded
  (let [forms (source-forms)
        form-node-counts
        (mapv #(count (tree-seq coll? seq %)) forms)]
    (is (empty? (invalid-source-if-forms forms)))
    (is (= '[(if true)]
           (invalid-source-if-forms '[(if true)])))
    (is (= '[(if true :then :else :extra)]
           (invalid-source-if-forms
            '[(if true :then :else :extra)])))
    (is (<= (apply max form-node-counts) 1024)
        "Conservative reader-tree admission is not the authoritative C6 form-id census")))

(deftest sh07-b31-c10-source-export-definitions-are-complete
  (let [forms (source-forms)]
    (is (empty? (undefined-exported-symbols forms)))
    (is (= '#{missing-export}
           (undefined-exported-symbols
            '[(ns example (:exports [defined-export missing-export]))
              (defn defined-export [] :present)])))
    (is (= #{::invalid-exports-clause}
           (undefined-exported-symbols '[(ns example)])))
    (is (= #{::invalid-exports-clause}
           (undefined-exported-symbols
            '[(not-ns example (:exports [defined]))
              (def defined :present)])))
    (is (= #{::invalid-exports-clause}
           (undefined-exported-symbols
            '[(ns example (:exports [defined] :trailing))
              (def defined :present)])))
    (is (= #{::invalid-exports-clause}
           (undefined-exported-symbols
            '[(ns example (:exports [duplicate duplicate]))
              (def duplicate :present)])))
    (is (empty?
         (undefined-exported-symbols
          '[(ns example (:exports [constant function]))
            (def constant :present)
            (defn function [] :present)])))))

(deftest sh07-b31-c10-source-contracts-policy-outcomes-and-reasons-are-exact
  (let [forms (source-forms)
        namespace-form (first forms)
        namespace-clauses
        (into {} (map (fn [clause] [(first clause) (second clause)]))
              (drop 2 namespace-form))
        bootstrap-metadata (get-in namespace-clauses [:metadata :bootstrap])
        definitions
        (into {}
              (for [name expected-definition-names]
                [name (or (named-top-level-form forms 'def name)
                          (named-top-level-form forms 'defn name))]))
        executable
        (set (for [[name form] definitions
                   :when (and (= 'defn (first form)) (nil? (quoted-body form)))]
               name))
        policy (nth (get definitions 'sh11-safety-policy) 3)
        rejection-calls
        (concat
         (collect-calls 'sh11-rejected-result
                        (get definitions 'sh11-classify-operation))
         (collect-calls 'sh11-diagnostic
                        (get definitions 'sh11-verify-safety-result)))
        rejection-tuples
        (set (map #(vec (take 4 (drop 2 %))) rejection-calls))
        preflight-reasons
        (set/intersection
         expected-structural-reasons
         (set (filter keyword?
                      (tree-seq coll? seq
                                (get definitions
                                     'sh11-structural-preflight)))))
        verification-reasons
        (set (for [entry (tree-seq coll? seq
                                   (get definitions
                                        'sh11-verify-safety-result))
                   :when (keyword? entry)
                   :when (#{:request-structural-bound
                            :candidate-structural-bound} entry)]
               entry))
        all-if-calls (mapcat #(collect-calls 'if %) (vals definitions))]
    (is (= 163 (count forms)))
    (is (= 'gravity.compiler.c10-safety-analysis-pipeline
           (second namespace-form)))
    (is (= :meta (:profile namespace-clauses)))
    (is (= :jvm (:target namespace-clauses)))
    (is (= expected-export-names (:exports namespace-clauses)))
    (is (= #{} (:effects namespace-clauses)))
    (is (= #{} (:capabilities namespace-clauses)))
    (is (= :safe (:safety namespace-clauses)))
    (is (= expected-document-ids (:documents bootstrap-metadata)))
    (is (= {:stage :stage1
            :component :safety-analysis
            :owner :gravity-source
            :source-language :gravity
            :seed :clojure-stage0
            :retirement-objective :replace-clojure-seed
            :ambient-authority-denied true}
           (select-keys
            bootstrap-metadata
            [:stage :component :owner :source-language :seed
             :retirement-objective :ambient-authority-denied])))
    (is (= #{:safety-operation-inventory
             :safe1-outcome-records
             :runtime-check-records
             :proof-obligation-records
             :certificate-reference-records
             :unsafe-island-audit-manifest
             :taint-safety-report
             :capability-safety-report
             :generated-code-safety-provenance
             :ffi-safety-boundary-records
             :numeric-safety-records
             :optimization-invalidation-records
             :safety-diagnostic-catalog}
           (set (:implements bootstrap-metadata))))
    (is (= #{:source-spans :syntax-identity :diagnostic-codes
             :artifact-provenance :core-node-identity :type-facts
             :effect-facts :capability-facts :ownership-facts
             :profile-target-metadata :safe1-outcomes
             :runtime-check-records :proof-obligations
             :certificate-references :unsafe-island-audits
             :generated-origin-chains :taint-flow-records
             :capability-proof-records :ffi-boundary-facts
             :numeric-mode-facts :optimization-invalidation-records}
           (:preserves bootstrap-metadata)))
    (is (= {:verified-by :clojure-stage0
            :compiled-by :clojure-stage0
            :next-stage :stage1
            :replaces
            [:clojure-c10-safety-analysis-contract-subset
             :clojure-c10-diagnostic-catalog]}
           (:lineage bootstrap-metadata)))
    (is (= expected-definition-names (set (keys definitions))))
    (is (= 7 (count (filter #(= 'def (first %)) (vals definitions)))))
    (is (= 155 (count (filter #(= 'defn (first %)) (vals definitions)))))
    (is (= quoted-definition-names
           (set (for [[name form] definitions :when (quoted-body form)] name))))
    (is (= expected-executable-sh11-names executable))
    (is (= 152 (count executable)))
    (is (= expected-policy policy))
    (is (= expected-diagnostic-catalog
           (nth (get definitions 'c10-safety-diagnostic-catalog) 2)))
    (doseq [[name contract] expected-contract-structures]
      (is (= contract (nth (get definitions name) 2))))
    (is (= expected-rejection-tuples rejection-tuples))
    (is (= expected-structural-reasons
           (set/union preflight-reasons verification-reasons)))
    (is (= 707 (count all-if-calls)))
    (is (every? #(= 4 (count %)) all-if-calls))
    (is (= :gravity/c10-safety-operation
           (:artifact (quoted-body
                       (get definitions 'build-c10-safety-operation)))))
    (is (= :gravity/c10-safety-outcome
           (:artifact (quoted-body
                       (get definitions 'build-c10-safety-outcome)))))
    (is (= :stage1-source-owned
           (:status (quoted-body
                     (get definitions 'verify-c10-safety-analysis)))))
    (is (= expected-source-byte-count
           (alength (source-bytes (path c10-relative-path)))))
    (is (= expected-source-revision-id
           (sha256-id (source-bytes (path c10-relative-path)))))))

(deftest sh07-b31-c10-static-lookup-and-residual-boundaries-are-exact
  (let [forms (source-forms)
        definitions
        (into {} (for [name expected-definition-names]
                   [name (or (named-top-level-form forms 'def name)
                             (named-top-level-form forms 'defn name))]))
        get-calls (mapcat #(collect-calls 'get %) (vals definitions))
        literal-gets (filter #(keyword? (nth % 2 nil)) get-calls)
        dynamic-gets
        (sort-by #(-> % meta :line)
                 (remove #(keyword? (nth % 2 nil)) get-calls))
        policy (nth (get definitions 'sh11-safety-policy) 3)
        catalog (nth (get definitions 'c10-safety-diagnostic-catalog) 2)
        preflight (get definitions 'sh11-structural-preflight)
        verifier (get definitions 'sh11-verify-safety-result)
        sha-shape (get definitions 'sh11-sha256-id?)]
    (is (= 1085 (count get-calls)))
    (is (= 1066 (count literal-gets)))
    (is (= '[(get values value)
             (get value (first remaining))
             (get value (first remaining))
             (get frontier cursor)
             (get (get (sh11-safety-policy) :legal-modes) kind)
             (get (get effected :type-table) (get request :value-id))
             (get (get (sh11-authenticated-division-bounds-policy)
                       :invalidation-conditions) kind)
             (get (get (sh11-authenticated-division-bounds-policy)
                       :specialized-rules) kind)
             (get (get (sh11-authenticated-division-bounds-policy)
                       :operand-roles) kind)
             (get (get (sh11-authenticated-division-bounds-policy)
                       :conditions) (get descriptor :kind))
             (get (get (sh11-authenticated-division-bounds-policy)
                       :predicate-expressions) (get descriptor :kind))
             (get (get (sh11-authenticated-overflow-cast-policy)
                       :specialized-rules) kind)
             (get operation (first fields))
             (get descriptor (first fields))
             (get {:authenticated-checked-add
                   {:kind :numeric-overflow :operator :add
                    :core-node-id
                    "sha256:0000000000000000000000000000000000000000000000000000000000000321"
                    :numeric-contract {:bit-width 8 :signedness :signed}
                    :resolution-id
                    "sha256:000000000000000000000000000000000000000000000000000000000000032b"}
                   :authenticated-checked-multiply
                   {:kind :numeric-overflow :operator :multiply
                    :core-node-id
                    "sha256:0000000000000000000000000000000000000000000000000000000000000322"
                    :numeric-contract {:bit-width 8 :signedness :unsigned}
                    :resolution-id
                    "sha256:000000000000000000000000000000000000000000000000000000000000032c"}
                   :authenticated-checked-cast
                   {:kind :numeric-cast :operator :checked-narrowing
                    :core-node-id
                    "sha256:0000000000000000000000000000000000000000000000000000000000000323"
                    :numeric-contract
                    {:source-width 16 :target-width 8
                     :source-signedness :signed :target-signedness :signed}
                    :resolution-id
                    "sha256:000000000000000000000000000000000000000000000000000000000000032d"}
                   :authenticated-incompatible-cast
                   {:kind :numeric-cast :operator :checked-narrowing
                    :core-node-id
                    "sha256:0000000000000000000000000000000000000000000000000000000000000324"
                    :numeric-contract
                    {:source-width 8 :target-width 16
                     :source-signedness :signed :target-signedness :signed}
                    :resolution-id
                    "sha256:000000000000000000000000000000000000000000000000000000000000032e"}}
                  operation-id)
             (get (get (sh11-authenticated-overflow-cast-policy)
                       :operand-roles) operator)
             (get (get (sh11-authenticated-overflow-cast-policy)
                       :invalidation-conditions) operator)
             (get (get (sh11-authenticated-overflow-cast-policy)
                       :conditions) (get descriptor :kind))
             (get (get (sh11-authenticated-overflow-cast-policy)
                       :predicate-expressions) operator)]
           (vec dynamic-gets)))
    (is (= [469 649 742 751 838 2198 3087 3105 3133
            3213 3218 3622 3713 3714 3720 4053 4135 4294 4299]
           (mapv #(-> % meta :line) dynamic-gets)))
    (is (= #{"C10-PROOF" "C10-GENERATED" "C10-TAINT"
             "C10-CAPABILITY" "C10-FFI" "C10-OPTIMIZATION"}
           (set/difference (set (:diagnostics catalog))
                           (set (:diagnostics policy)))))
    (is (= (:pending expected-policy) (:pending policy)))
    (is (contains? (symbols-in preflight) 'sh11-aggregate-components))
    (is (contains? (symbols-in preflight) 'sh11-structural-preflight))
    (is (not (string/includes? (pr-str preflight) ":identity-cycle")))
    (is (contains? (symbols-in verifier) 'sh11-classify-operation))
    (is (not (contains? (symbols-in verifier)
                        'independent-safety-result-verifier)))
    (is (not (contains? (symbols-in sha-shape)
                        'authenticated-envelope-verify-template)))
    (is (= (:structural-bounds expected-policy)
           (:structural-bounds policy)))))

(deftest sh07-b31-c10-source-has-exact-authentic-coverage
  (let [artifact @c10-artifact
        authenticated-request (request artifact)
        core-artifact (core artifact)
        coverage-record (:fragment-coverage core-artifact)
        fragments (:fragment-manifest authenticated-request)]
    (is (= :accepted (:status artifact)))
    (is (= :accepted (get-in artifact [:sh06-resolution-artifact :status])))
    (is (= "SH-07-B47" (:task artifact)))
    (is (= 15 (:schema-version authenticated-request)))
    (is (= :sh07-b15-keyword-map-lookup (:scope authenticated-request)))
    (is (= 'gravity.compiler.c10-safety-analysis-pipeline
           (get-in authenticated-request [:module :namespace])))
    (is (= expected-source-revision-id
           (get-in authenticated-request [:module :source-revision-id])
           (get-in authenticated-request [:lineage :source-revision-id])))
    (is (= expected-sh06-semantic-projection-id
           (get-in authenticated-request
                   [:lineage :sh06-semantic-projection-id])))
    (is (= expected-coverage (coverage artifact)))
    (is (= expected-core-census (core-census artifact)))
    (is (= (:top-level-form-ids authenticated-request)
           (:covered-root-form-ids coverage-record)
           (vec (mapcat :root-form-ids fragments))))
    (is (= (mapv :form-id (:forms authenticated-request))
           (:covered-form-ids coverage-record)
           (vec (mapcat :form-ids fragments))))
    (is (false? (get-in artifact
                        [:gravity-core-boundary :target-source-reread?])))))

(deftest sh07-b31-c10-calls-lookups-and-quotes-are-exact
  (let [core-artifact (core @c10-artifact)
        nodes (:nodes core-artifact)
        node-by-id (exactly-once-index nodes :node-id)
        reference-by-node-id
        (exactly-once-index (:reference-uses core-artifact) :core-node-id)
        get-calls
        (filterv #(= 'get (get-in reference-by-node-id
                                  [(:operator-node-id %) :symbol]))
                 (:calls core-artifact))
        literal-gets
        (filterv #(keyword? (get-in node-by-id
                                    [(second (:argument-node-ids %))
                                     :attributes :value]))
                 get-calls)
        dynamic-gets
        (filterv #(not (keyword? (get-in node-by-id
                                         [(second (:argument-node-ids %))
                                          :attributes :value])))
                 get-calls)
        quote-nodes (filterv #(= :quote (:core-form %)) nodes)]
    (is (= expected-definition-names
           (set (map :name (:definitions core-artifact)))))
    (is (= 1085 (count get-calls)))
    (is (= 1066 (count literal-gets)))
    (is (= 19 (count dynamic-gets)))
    (is (= {:reference 11 :call 8}
           (frequencies
            (map #(get-in node-by-id
                          [(second (:argument-node-ids %)) :core-form])
                 dynamic-gets))))
    (is (empty? (:keyword-lookups core-artifact)))
    (is (zero? (count (filter #(= :keyword-map-lookup (:core-form %)) nodes))))
    (is (= 3 (count quote-nodes)))
    (doseq [node quote-nodes]
      (is (= [] (:children node)))
      (is (= :no-evaluation (get-in node [:evaluation :kind])))
      (is (= [] (get-in node [:evaluation :order]))))))

(deftest sh07-b31-c10-is-deterministic-path-neutral-and-provenanced
  (let [{:keys [left right left-path right-path]} @parity-artifacts]
    (is (= :accepted (:status left) (:status right)))
    (is (= (:artifact-id left) (:artifact-id right)))
    (is (= (identity-input left) (identity-input right)))
    (is (= (coverage left) (coverage right)))
    (is (= (core-census left) (core-census right)))
    (is (= (:fragment-manifest (request left))
           (:fragment-manifest (request right))))
    (is (= left-path
           (get-in left [:provenance :source-path])
           (get-in (core left) [:provenance :actual-source-path])))
    (is (= right-path
           (get-in right [:provenance :source-path])
           (get-in (core right) [:provenance :actual-source-path])))
    (is (not= left-path right-path))))

(deftest sh07-b31-c10-replay-and-high-value-alterations-are-contained
  (let [artifact @c10-artifact
        core-artifact (core artifact)
        quote-index
        (first (keep-indexed (fn [index node]
                               (when (= :quote (:core-form node)) index))
                             (:nodes core-artifact)))
        report ((required-var 'sh07-core-artifact-verification) artifact)
        proof (:capability-based-proof artifact)
        request-alteration
        (assoc-in (request artifact) [:module :source-revision-id] zero-id)
        request-result
        (diagnostic-result
         #((required-var 'sh07-core-run-request-for-test)
           (:sh06-resolution-artifact artifact) request-alteration))
        request-diagnostic (diagnostic-data request-result)]
    (is (= :passed (:status report)))
    (is (= [] (:failed-checks report)))
    (is (= :complete (:status proof)))
    (is (= [] (:failed-checks proof)))
    (is (nil? (:raw-host-error request-result)))
    (is (= :gravity/sh07-core-diagnostic (:artifact request-diagnostic)))
    (is (= "C6-VERIFY" (:rule request-diagnostic)))
    (doseq [[label altered expected-check]
            [["definition binding"
              (assoc-in artifact
                        [:gravity-core-boundary :canonical-core-artifact
                         :definitions 0 :binding-id] zero-id)
              :canonical-core-replays?]
             ["call binding"
              (assoc-in artifact
                        [:gravity-core-boundary :canonical-core-artifact
                         :calls 0 :operator-binding-id] zero-id)
              :calls-replay?]
             ["quoted body"
              (assoc-in artifact
                        [:gravity-core-boundary :canonical-core-artifact
                         :nodes quote-index :attributes :quoted-value]
                        :altered)
              :canonical-core-replays?]
             ["actual path provenance"
              (assoc-in artifact
                        [:gravity-core-boundary :canonical-core-artifact
                         :provenance :actual-source-path]
                        "/altered/root/c10_safety_analysis_pipeline.gravity")
              :provenance-retained?]
             ["stored proof"
              (assoc-in artifact [:capability-based-proof :status] :failed)
              :stored-capability-proof-current?]]]
      (testing label
        (let [checks
              ((required-var 'sh07-core-verification-checks)
               altered artifact @c10-upstream-verification)
              failed (set (for [[check passed?] checks
                                :when (not (true? passed?))]
                            check))]
          (is (contains? failed expected-check)))))))

(deftest sh07-b31-existing-rejected-families-remain-paired-and-structured
  (doseq [[basename expected-rule] rejected-families
          extension ["gravity" "qst"]]
    (testing (str basename "." extension)
      (let [source-path
            (path
             (str "bootstrap/clojure/fixtures/self-hosting/sh-07/rejected/"
                  basename "." extension))
            peer-extension (if (= "gravity" extension) "qst" "gravity")
            peer-path
            (path
             (str "bootstrap/clojure/fixtures/self-hosting/sh-07/rejected/"
                  basename "." peer-extension))
            result
            (diagnostic-result
             #((required-var 'sh07-core-file-artifact) source-path))
            diagnostic (diagnostic-data result)]
        (is (= (vec (source-bytes source-path))
               (vec (source-bytes peer-path))))
        (is (nil? (:raw-host-error result)))
        (is (= :gravity/sh07-core-diagnostic (:artifact diagnostic)))
        (is (= expected-rule (:rule diagnostic)))
        (is (= source-path (get-in diagnostic [:source-span :source])))
        (is (= true (get-in diagnostic [:facts :fail-closed])))))))

(deftest sh07-b31-c10-measured-carrier-fits-unchanged-b16-bounds
  (let [m expected-census-measurements
        b expected-b16-bounds]
    (is (< (:forms m) (:maximum-module-forms b)))
    (is (< (:predicted-maximum-core-nodes m)
           (:maximum-module-core-nodes b)))
    (is (< (:fragments m) (:maximum-fragments b)))
    (is (< (:bindings m) (:maximum-bindings b)))
    (is (< (:resolutions m) (:maximum-module-resolutions b)))
    (is (< (:carrier-nodes m) (:maximum-module-carrier-nodes b)))
    (is (< (:carrier-depth m) (:maximum-module-carrier-depth b)))
    (is (< (:carrier-width m) (:maximum-module-carrier-width b)))
    (is (< (:carrier-scalar-bytes m) (:maximum-module-scalar-bytes b)))
    (is (< (:predicted-maximum-digest-requests m)
           (:maximum-module-digest-requests b)))))
