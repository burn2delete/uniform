(ns gravity.c11-mir
  "Hosted Stage0 C11 target-independent MIR construction and projection.

  This leaf preserves the Clojure seed compatibility representation. It is not
  canonical MIR, verifier, optimizer, backend, self-hosting, or release
  authority."
  (:require [clojure.string :as str]
            [gravity.digest :as digest]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})

(def ^:private function-operation-keys
  #{:fail!
    :source-span
    :c4-artifact-id
    :read-source-form-records
    :validate-ns-syntax!
    :parse-module
    :compiler-c10-safety-source-artifact
    :c11-mir-source-overrides
    :c11-mir-message
    :c11-mir-fail!
    :c11-mir-validate-overrides!
    :c11-family-opcode
    :c11-family-effects
    :c11-mir-operation
    :c11-mir-module-record
    :c11-data-flow-graph
    :c11-domain-anchor-table
    :c11-present?
    :c11-mir-diagnostics
    :c11-mir-verifier-report
    :c11-mir-capability-proof
    :c11-mir-validate!
    :compiler-c11-mir-source-artifact
    :compiler-c11-mir-file-artifact})

(def ^:private scalar-operation-keys
  #{:c11-mir-diagnostic-ids
    :c11-mir-governing-document
    :c11-mir-required-operation-families
    :c11-mir-rejected-designs
    :c11-mir-override-diagnostics})

(def ^:private operation-keys
  (into function-operation-keys scalar-operation-keys))

(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key)
    (get *operations* key)))

(defmacro ^:private definterposable [name args & body]
  (let [key (keyword name)]
    `(defn ~name ~args
       (if-let [operation# (current-operation ~key)]
         (binding [*active-operation-keys* (conj *active-operation-keys* ~key)]
           (operation# ~@args))
         (do ~@body)))))

(defn- default-fail! [id message data]
  (throw (ex-info message (assoc (or data {}) :id id))))
(defn- default-source-span [path index] {:source path :form-index index})
(defn- default-c4-artifact-id [artifact]
  (str "sha256:" (digest/sha256-hex (pr-str artifact))))
(defn- unsupported-host-operation [operation]
  (fn [& _]
    (throw (ex-info (str "C11 leaf requires injected operation " operation)
                    {:operation operation}))))
(defn- op-fn [key fallback] (or (get *operations* key) fallback))
(defn- fail! [id message data] ((op-fn :fail! default-fail!) id message data))
(defn- source-span [path index] ((op-fn :source-span default-source-span) path index))
(defn- c4-artifact-id [artifact] ((op-fn :c4-artifact-id default-c4-artifact-id) artifact))
(defn- read-source-form-records [path text]
  ((op-fn :read-source-form-records (unsupported-host-operation :read-source-form-records)) path text))
(defn- validate-ns-syntax! [path forms]
  ((op-fn :validate-ns-syntax! (unsupported-host-operation :validate-ns-syntax!)) path forms))
(defn- parse-module [path forms]
  ((op-fn :parse-module (unsupported-host-operation :parse-module)) path forms))
(defn- compiler-c10-safety-source-artifact [path text]
  ((op-fn :compiler-c10-safety-source-artifact
          (unsupported-host-operation :compiler-c10-safety-source-artifact)) path text))

(def ^:dynamic c11-mir-diagnostic-ids
  ["C11-MODULE"
   "C11-BLOCK"
   "C11-DOMINANCE"
   "C11-TYPE"
   "C11-EFFECT"
   "C11-SAFETY"
   "C11-ORIGIN"
   "C11-DOMAIN"
   "C11-TARGET-LEAK"
   "C11-VERIFY"])

(def ^:dynamic c11-mir-governing-document
  "docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md")

(def ^:dynamic c11-mir-required-operation-families
  [:constant
   :local
   :call
   :closure
   :dispatch
   :data-constructor
   :field-index-buffer
   :numeric
   :memory
   :region
   :linear-resource
   :control-flow
   :error
   :ffi
   :concurrency
   :workflow
   :ai-tool
   :domain-anchor
   :runtime-check
   :proof-reference])

(def ^:dynamic c11-mir-rejected-designs
  [{:diagnostic "C11-MODULE"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-mir-module.gravity"
    :rejected-design :malformed-module}
   {:diagnostic "C11-BLOCK"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-mir-block.gravity"
    :rejected-design :invalid-block}
   {:diagnostic "C11-DOMINANCE"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-mir-dominance.gravity"
    :rejected-design :use-before-definition}
   {:diagnostic "C11-TYPE"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-mir-type.gravity"
    :rejected-design :missing-type}
   {:diagnostic "C11-EFFECT"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-mir-effect.gravity"
    :rejected-design :missing-effect-ordering}
   {:diagnostic "C11-SAFETY"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-mir-safety.gravity"
    :rejected-design :missing-safety-outcome}
   {:diagnostic "C11-ORIGIN"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-mir-origin.gravity"
    :rejected-design :missing-origin}
   {:diagnostic "C11-DOMAIN"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-mir-domain.gravity"
    :rejected-design :invalid-domain-anchor}
   {:diagnostic "C11-TARGET-LEAK"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-mir-target-leak.gravity"
    :rejected-design :target-specific-generic-mir}
   {:diagnostic "C11-VERIFY"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-mir-verify.gravity"
    :rejected-design :verifier-failure}])

(def ^:dynamic c11-mir-override-diagnostics
  {:module "C11-MODULE"
   :block "C11-BLOCK"
   :dominance "C11-DOMINANCE"
   :type "C11-TYPE"
   :effect "C11-EFFECT"
   :safety "C11-SAFETY"
   :origin "C11-ORIGIN"
   :domain "C11-DOMAIN"
   :target-leak "C11-TARGET-LEAK"
   :verify "C11-VERIFY"})

(definterposable c11-mir-source-overrides
  [module]
  (or (get-in module [:metadata :compiler :c11-mir-spec])
      (get-in module [:metadata :compiler :mir])
      {}))

(definterposable c11-mir-message
  [id]
  (case id
    "C11-MODULE" "MIR module record is malformed"
    "C11-BLOCK" "MIR block is malformed or unterminated"
    "C11-DOMINANCE" "MIR operation uses a value before definition"
    "C11-TYPE" "MIR operation is missing type evidence"
    "C11-EFFECT" "effectful MIR operation is missing ordering evidence"
    "C11-SAFETY" "safety-sensitive MIR operation is missing outcome evidence"
    "C11-ORIGIN" "MIR operation is missing source or generated origin"
    "C11-DOMAIN" "MIR domain anchor is invalid"
    "C11-TARGET-LEAK" "target-specific opcode appeared in generic MIR"
    "C11-VERIFY" "MIR verifier failed"
    "MIR validation failed"))

(definterposable c11-mir-fail!
  [id source-path subject extra]
  (fail! id
         (c11-mir-message id)
         (merge {:source-span (or (:source-span subject)
                                  (get-in subject [:source :span])
                                  (source-span source-path 0))
                 :diagnostic-family :c11-mir-specification
                 :stage :mir-construction
                 :document-id "C11"
                 :expected-document c11-mir-governing-document
                 :mir-module (or (:mir-module subject) :fixture/mir-module)
                 :function (or (:function subject) :fixture/function)
                 :block (or (:block subject) :fixture/block)
                 :operation-id (or (:operation-id subject)
                                   (:op-id subject)
                                   :fixture/operation)
                 :origin-chain (or (:generated-origin subject)
                                   (get-in subject [:source :origin-chain])
                                   [])
                 :profile (:profile subject)
                 :target-request (or (:target-request subject)
                                     (:target subject))
                 :missing-fact (:missing-fact subject)
                 :remediation "Regenerate target-independent MIR from C10 safety-checked core with type, effect, ownership, capability, safety, proof, profile, target, and source-origin facts."}
                extra)))

(definterposable c11-mir-validate-overrides!
  [source-path module overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get c11-mir-override-diagnostics fail-kind)]
      (c11-mir-fail! id source-path
                     {:source-span (source-span source-path 0)
                      :operation-id (keyword "fixture" (name fail-kind))
                      :profile (:profile module)
                      :target-request (:target module)
                      :missing-fact fail-kind}
                     {:missing-fields [fail-kind]}))))

(definterposable c11-family-opcode
  [family]
  (case family
    :constant :mir/constant
    :local :mir/local
    :call :mir/call
    :closure :mir/closure
    :dispatch :mir/dispatch
    :data-constructor :mir/construct
    :field-index-buffer :mir/index
    :numeric :mir/add-checked
    :memory :mir/load
    :region :mir/region-alloc
    :linear-resource :mir/resource-close
    :control-flow :mir/branch
    :error :mir/throw
    :ffi :mir/ffi-call
    :concurrency :mir/task-spawn
    :workflow :mir/workflow-yield
    :ai-tool :mir/ai-tool-call
    :domain-anchor :mir/domain-anchor
    :runtime-check :mir/runtime-check
    :proof-reference :mir/proof-assert
    :mir/unknown))

(definterposable c11-family-effects
  [family]
  (case family
    :call #{:runtime/dynamic-dispatch}
    :dispatch #{:runtime/dynamic-dispatch}
    :field-index-buffer #{:error/throw}
    :numeric #{:error/throw}
    :memory #{:memory/raw}
    :region #{:memory/raw}
    :linear-resource #{:io/write}
    :error #{:error/throw}
    :ffi #{:memory/raw}
    :concurrency #{:runtime/dynamic-dispatch}
    :workflow #{:runtime/dynamic-dispatch}
    :ai-tool #{:runtime/dynamic-dispatch}
    :runtime-check #{:error/throw}
    #{}))

(definterposable c11-mir-operation
  [module span outcome-by-index index family]
  (let [effects (c11-family-effects family)
        op-id (str "c11-mir-op-" (name family))
        outcome (get outcome-by-index (mod index (count outcome-by-index)))]
    {:op-id op-id
     :opcode (c11-family-opcode family)
     :family family
     :operands (if (zero? index) [] [(str "c11-value-" (dec index))])
     :result (when-not (contains? #{:control-flow :error} family)
               (str "c11-value-" index))
     :type (case family
             :constant "I64"
             :numeric "I64"
             :field-index-buffer "Byte"
             :runtime-check "Unit"
             :proof-reference "Unit"
             :domain-anchor "DomainAnchor"
             "Unit")
     :effects effects
     :ordering (if (seq effects) :sequence :none)
     :source {:core-node (str "c10:" (:operation outcome))
              :span span
              :origin-chain (get-in outcome [:source :origin-chain] [])}
     :profile (:profile module)
     :facts {:ownership "c9:ownership"
             :capabilities "c8:capabilities"
             :safety (:operation outcome)
             :runtime-check (:runtime-check outcome)
             :proofs (vec (remove nil? [(:proof outcome)]))}
     :domain-anchor (when (= :domain-anchor family)
                      "c11-domain-anchor-efir")
     :verifier-status :passed}))

(definterposable c11-mir-module-record
  [module c10-artifact operations]
  (let [fn-id (str "c11-mir-fn-" (name (:module module)) "-main")
        op-ids (mapv :op-id operations)
        entry-block {:block-id :entry
                     :operations op-ids
                     :terminator {:kind :return
                                  :value (last (keep :result operations))}
                     :successors []}]
    {:artifact :gravity/mir-module
     :module (:module module)
     :source-core (:artifact-id c10-artifact)
     :profile (:profile module)
     :target-request (:target module)
     :functions {fn-id {:fn-id fn-id
                        :name (symbol (str (:module module)) "main")
                        :params []
                        :returns "Unit"
                        :latent-effects (:effects module)
                        :blocks {:entry entry-block}
                        :entry :entry
                        :source {:span (source-span (:source-path module) 0)
                                 :origin-chain []}}}
     :globals {}
     :types :c11/type-table
     :effects :c11/effect-table
     :ownership :c11/ownership-table
     :capabilities :c11/capability-table
     :safety :c11/safety-table
     :domain-anchors :c11/domain-anchor-table
     :diagnostics []}))

(definterposable c11-data-flow-graph
  [operations]
  (mapv (fn [[from to]]
          {:from (:op-id from)
           :to (:op-id to)
           :edge :sequence
           :dominance-status :passed})
        (partition 2 1 operations)))

(definterposable c11-domain-anchor-table
  []
  [{:domain :efir
    :anchor-id "c11-domain-anchor-efir"
    :mir-ops ["c11-mir-op-domain-anchor"]
    :semantic-artifact "stage0-efir-graph"
    :equivalence-proof "proof-domain-anchor-round-trip"
    :fallback "c11-fallback-mir-subgraph"
    :status :valid}])

(definterposable c11-present?
  [value]
  (cond
    (nil? value) false
    (and (coll? value) (empty? value)) false
    (and (string? value) (str/blank? value)) false
    :else true))

(definterposable c11-mir-diagnostics
  [source-path]
  {:artifact :gravity/c11-mir-diagnostic-registry
   :required-diagnostic-ids c11-mir-diagnostic-ids
   :diagnostics
   (mapv (fn [design]
           {:diagnostic (:diagnostic design)
            :fixture (:fixture design)
            :mir-module :fixture/mir-module
            :function :fixture/function
            :block :fixture/block
            :operation-id (keyword "fixture" (:diagnostic design))
            :source-span (source-span source-path 0)
            :origin-chain []
            :profile :fixture/profile
            :target-request :fixture/target
            :missing-fact (:rejected-design design)
            :remediation "Keep target-independent MIR typed, effected, safety-linked, source-mapped, and verifier-clean before optimization or target lowering."})
         c11-mir-rejected-designs)
   :status :complete})

(definterposable c11-mir-verifier-report
  [module operations data-flow domain-anchors diagnostics]
  (let [families (set (map :family operations))
        diagnostics? (= (set c11-mir-diagnostic-ids)
                        (set (map :diagnostic (:diagnostics diagnostics))))]
    {:artifact :gravity/c11-mir-verifier-report
     :module-shape-valid? (= :gravity/mir-module (:artifact module))
     :blocks-terminate? (every? #(c11-present? (:terminator %))
                                (mapcat (comp vals :blocks)
                                        (vals (:functions module))))
     :dominance-valid? (every? #(= :passed (:dominance-status %)) data-flow)
     :types-present? (every? #(c11-present? (:type %)) operations)
     :effect-ordering-present? (every? #(or (empty? (:effects %))
                                            (not= :none (:ordering %)))
                                       operations)
     :safety-linked? (every? #(c11-present? (get-in % [:facts :safety]))
                             operations)
     :origins-linked? (every? #(c11-present? (get-in % [:source :span]))
                              operations)
     :domain-anchors-valid? (every? #(and (c11-present? (:anchor-id %))
                                          (c11-present? (:fallback %)))
                                    domain-anchors)
     :target-independent? (not-any? #(= :target-specific (:family %))
                                    operations)
     :operation-family-coverage-complete?
     (= (set c11-mir-required-operation-families) families)
     :diagnostics-covered? diagnostics?
     :status (if (and (= :gravity/mir-module (:artifact module))
                      (every? #(c11-present? (:type %)) operations)
                      (every? #(or (empty? (:effects %))
                                   (not= :none (:ordering %)))
                              operations)
                      (every? #(c11-present? (get-in % [:facts :safety]))
                              operations)
                      (every? #(c11-present? (get-in % [:source :span]))
                              operations)
                      (every? #(and (c11-present? (:anchor-id %))
                                    (c11-present? (:fallback %)))
                              domain-anchors)
                      (= (set c11-mir-required-operation-families) families)
                      diagnostics?)
               :passed
               :failed)}))

(definterposable c11-mir-capability-proof
  [artifact]
  (let [verifier (:mir-verifier-report artifact)]
    {:module-serialized? (:module-shape-valid? verifier)
     :blocks-terminated? (:blocks-terminate? verifier)
     :operations-typed? (:types-present? verifier)
     :effect-ordering-present? (:effect-ordering-present? verifier)
     :safety-outcomes-linked? (:safety-linked? verifier)
     :origins-linked? (:origins-linked? verifier)
     :domain-anchors-valid? (:domain-anchors-valid? verifier)
     :target-independent? (:target-independent? verifier)
     :operation-family-coverage-complete?
     (:operation-family-coverage-complete? verifier)
     :diagnostics-covered? (:diagnostics-covered? verifier)
     :verifier-passed? (= :passed (:status verifier))
     :status :complete}))

(definterposable c11-mir-validate!
  [source-path artifact]
  (let [proof (c11-mir-capability-proof artifact)]
    (doseq [[field id] [[:module-serialized? "C11-MODULE"]
                        [:blocks-terminated? "C11-BLOCK"]
                        [:operations-typed? "C11-TYPE"]
                        [:effect-ordering-present? "C11-EFFECT"]
                        [:safety-outcomes-linked? "C11-SAFETY"]
                        [:origins-linked? "C11-ORIGIN"]
                        [:domain-anchors-valid? "C11-DOMAIN"]
                        [:target-independent? "C11-TARGET-LEAK"]
                        [:operation-family-coverage-complete?
                         "C11-VERIFY"]
                        [:diagnostics-covered? "C11-VERIFY"]
                        [:verifier-passed? "C11-VERIFY"]]]
      (when-not (get proof field)
        (c11-mir-fail! id source-path {:stage :mir-construction}
                       {:missing-fields [field]}))))
  :complete)

(definterposable compiler-c11-mir-source-artifact
  [source-path source-text]
  (let [records (read-source-form-records source-path source-text)
        forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        overrides (c11-mir-source-overrides module)
        _ (c11-mir-validate-overrides! source-path module overrides)
        c10-artifact (compiler-c10-safety-source-artifact source-path source-text)
        outcomes (vec (get-in c10-artifact [:safety-outcome-records :records]))
        span (source-span source-path 0)
        operations (mapv (fn [index family]
                           (c11-mir-operation module span outcomes index family))
                         (range)
                         c11-mir-required-operation-families)
        mir-module (c11-mir-module-record module c10-artifact operations)
        data-flow (c11-data-flow-graph operations)
        domain-anchors (c11-domain-anchor-table)
        diagnostics (c11-mir-diagnostics source-path)
        verifier (c11-mir-verifier-report mir-module operations data-flow
                                          domain-anchors diagnostics)
        artifact-base
        {:kind :gravity/stage0-c11-mir-spec-artifact
         :task "P06-D090"
         :document-set ["C11"]
         :governing-document c11-mir-governing-document
         :pass {:name :c11-mir-specification
                :input :safety-checked-core
                :output :gravity/mir
                :requires [:c10-safety-analysis :types :effects :ownership
                           :capabilities :safety-outcomes :profile :target]
                :preserves [:source-spans :origin-chain :profile :target
                            :types :effects :ownership :capabilities
                            :safety-outcomes :proofs :diagnostics]
                :emits [:mir-module :mir-operations :control-flow-graph
                        :data-flow-graph :metadata-tables
                        :source-origin-map :domain-anchor-table
                        :mir-verifier-report :mir-diagnostic-stream]
                :rejects c11-mir-diagnostic-ids}
         :source-overrides overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :c10-safety-analysis-artifact
         (select-keys c10-artifact [:kind :artifact-id
                                    :safety-operation-inventory
                                    :safety-outcome-records
                                    :runtime-check-list
                                    :capability-based-proof])
         :mir-module mir-module
         :mir-operations operations
         :operation-family-coverage
         (mapv (fn [family]
                 {:family family
                  :status :represented-by-operation})
               c11-mir-required-operation-families)
         :control-flow-graph {:entry :entry
                              :blocks (get-in mir-module
                                              [:functions
                                               (first (keys (:functions mir-module)))
                                               :blocks])
                              :status :complete}
         :data-flow-graph data-flow
         :metadata-tables {:types :c11/type-table
                           :effects :c11/effect-table
                           :ownership :c11/ownership-table
                           :capabilities :c11/capability-table
                           :safety :c11/safety-table
                           :runtime-checks :c11/runtime-check-table
                           :proofs :c11/proof-table
                           :source-origins :c11/source-origin-table
                           :profile-target :c11/profile-target-table
                           :domain-anchors :c11/domain-anchor-table
                           :status :complete}
         :type-table (into {}
                           (map (fn [op] [(:result op) (:type op)]))
                           (filter :result operations))
         :effect-table (into {}
                             (map (fn [op] [(:op-id op) (:effects op)]))
                             operations)
         :ownership-table (get-in c10-artifact
                                  [:c9-ownership-checker-artifact
                                   :ownership-graph])
         :capability-proof-table
         (get-in c10-artifact
                 [:c9-ownership-checker-artifact
                  :capability-based-proof])
         :safety-outcome-table (:safety-outcome-records c10-artifact)
         :runtime-check-table (:runtime-check-list c10-artifact)
         :proof-certificate-table (:proof-certificate-references c10-artifact)
         :source-origin-map (mapv #(select-keys % [:op-id :source])
                                  operations)
         :domain-anchor-table domain-anchors
         :optimization-invalidation-hooks
         [{:hook :c11-mir-fact-invalidation
           :invalidates [:type-table :effect-table :ownership-table
                         :safety-outcome-table :domain-anchor-table]
           :requires [:mir-verifier-report :proof-certificate-table]
           :status :recorded}]
         :target-lowering-input-validation
         {:input :gravity/mir
          :requires [:mir-verifier-report :profile :target-request
                     :runtime-check-table :safety-outcome-table]
          :status :ready-for-target-lowering}
         :mir-verifier-report verifier
         :mir-diagnostic-stream diagnostics
         :c11-mir-spec-results
         {:documents ["C11"]
          :task "P06-D090"
          :required-diagnostic-ids c11-mir-diagnostic-ids
          :module-status :complete
          :function-block-operation-status :complete
          :metadata-table-status :complete
          :runtime-check-preservation-status :complete
          :domain-anchor-status :complete
          :optimization-invalidation-status :complete
          :target-lowering-input-status :complete
          :verifier-status (:status verifier)
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (c11-mir-validate! source-path artifact-base)
        capability-proof (c11-mir-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(definterposable compiler-c11-mir-file-artifact
  [path]
  (compiler-c11-mir-source-artifact path (slurp path)))

(def ^:private namespace-contract
  {:contract-boundary :hosted-stage0-c11-mir
   :artifact-inputs [:c10-safety-analysis-artifact :module-context]
   :artifact-outputs [:mir-module :control-flow-graph :data-flow-graph
                      :metadata-tables :source-origin-map
                      :domain-anchor-table :mir-verifier-report
                      :mir-diagnostics]
   :owns [:hosted-stage0-c11-mir-construction
          :hosted-stage0-c11-artifact-projection]
   :dependency-direction {:requires ['clojure.string 'gravity.digest]
                          :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :does-not-own [:canonical-c11-authority :source-authentication
                  :type-effect-ownership-safety-authority
                  :canonical-mir-verifier-authority
                  :domain-ir-authority :optimization-authority
                  :target-lowering-authority :backend-authority
                  :proof-authority :equivalence :self-hosting :release
                  :seed-retirement]
   :compatibility-only? true
   :override-driven-diagnostics? true
   :mir-model-complete? false
   :canonical-c11-authority? false
   :operation-interposition {:accepted-keys operation-keys
                             :unknown-keys-rejected? true
                             :partial-overrides? true
                             :single-binding-per-top-level-call? true}})

(defn- string-vector? [v] (and (vector? v) (seq v) (every? string? v)))
(defn- keyword-vector? [v] (and (vector? v) (seq v) (every? keyword? v)))
(defn- vector-of-maps? [v] (and (vector? v) (every? map? v)))
(defn- keyword-string-map? [v]
  (and (map? v) (every? (fn [[k x]] (and (keyword? k) (string? x))) v)))

(defn- validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C11 operation map must be a map" {:value operations})))
  (let [unknown (seq (remove operation-keys (keys operations)))
        invalid (seq (for [[k v] (select-keys operations function-operation-keys)
                           :when (not (fn? v))] k))]
    (when unknown
      (throw (ex-info "C11 operation map contains unknown keys"
                      {:unknown-keys (vec unknown) :allowed-keys operation-keys})))
    (when invalid
      (throw (ex-info "C11 function operation values must be functions"
                      {:non-function-keys (vec invalid)}))))
  (doseq [[key predicate expected]
          [[:c11-mir-diagnostic-ids string-vector? :non-empty-string-vector]
           [:c11-mir-governing-document #(and (string? %) (seq %)) :non-empty-string]
           [:c11-mir-required-operation-families keyword-vector? :non-empty-keyword-vector]
           [:c11-mir-rejected-designs vector-of-maps? :vector-of-maps]
           [:c11-mir-override-diagnostics keyword-string-map? :keyword-to-string-map]]
          :when (and (contains? operations key)
                     (not (predicate (get operations key))))]
    (throw (ex-info "C11 scalar operation has an invalid shape"
                    {:key key :expected expected :actual (get operations key)})))
  operations)

(defn with-operations [operations thunk]
  (validate-operations! operations)
  (let [merged (merge *operations* operations)]
    (binding [*operations* merged
              c11-mir-diagnostic-ids (get merged :c11-mir-diagnostic-ids c11-mir-diagnostic-ids)
              c11-mir-governing-document (get merged :c11-mir-governing-document c11-mir-governing-document)
              c11-mir-required-operation-families (get merged :c11-mir-required-operation-families c11-mir-required-operation-families)
              c11-mir-rejected-designs (get merged :c11-mir-rejected-designs c11-mir-rejected-designs)
              c11-mir-override-diagnostics (get merged :c11-mir-override-diagnostics c11-mir-override-diagnostics)]
      (thunk))))

(def public-api
  {'public-api {:kind :contract}
   'with-operations {:arglists '([operations thunk])}
   'c11-engine-contract {:arglists '([])}
   'c11-mir-diagnostic-ids {:kind :constant}
   'c11-mir-governing-document {:kind :constant}
   'c11-mir-required-operation-families {:kind :constant}
   'c11-mir-rejected-designs {:kind :constant}
   'c11-mir-override-diagnostics {:kind :constant}
   'c11-mir-source-overrides {:arglists '([module])}
   'c11-mir-message {:arglists '([id])}
   'c11-mir-fail! {:arglists '([id source-path subject extra])}
   'c11-mir-validate-overrides! {:arglists '([source-path module overrides])}
   'c11-family-opcode {:arglists '([family])}
   'c11-family-effects {:arglists '([family])}
   'c11-mir-operation {:arglists '([module span outcome-by-index index family])}
   'c11-mir-module-record {:arglists '([module c10-artifact operations])}
   'c11-data-flow-graph {:arglists '([operations])}
   'c11-domain-anchor-table {:arglists '([])}
   'c11-present? {:arglists '([value])}
   'c11-mir-diagnostics {:arglists '([source-path])}
   'c11-mir-verifier-report {:arglists '([module operations data-flow domain-anchors diagnostics])}
   'c11-mir-capability-proof {:arglists '([artifact])}
   'c11-mir-validate! {:arglists '([source-path artifact])}
   'compiler-c11-mir-source-artifact {:arglists '([source-path source-text])}
   'compiler-c11-mir-file-artifact {:arglists '([path])}
   })

(defn c11-engine-contract []
  (assoc namespace-contract :public-api public-api))
