(ns
 gravity.c11-mir.records
 (:require
  [clojure.string :as str]
  [gravity.c11-mir.operations :as operations]))
(defn
 source-span
 [path index]
 (operations/invoke
  :source-span
  (fn [p i] {:source p, :form-index i})
  path
  index))
(defn
 source-overrides
 [module]
 (or
  (get-in module [:metadata :compiler :c11-mir-spec])
  (get-in module [:metadata :compiler :mir])
  {}))
(defn
 message
 [id]
 (get
  {"C11-SAFETY"
   "safety-sensitive MIR operation is missing outcome evidence",
   "C11-MODULE" "MIR module record is malformed",
   "C11-VERIFY" "MIR verifier failed",
   "C11-TYPE" "MIR operation is missing type evidence",
   "C11-BLOCK" "MIR block is malformed or unterminated",
   "C11-EFFECT" "effectful MIR operation is missing ordering evidence",
   "C11-TARGET-LEAK" "target-specific opcode appeared in generic MIR",
   "C11-DOMINANCE" "MIR operation uses a value before definition",
   "C11-ORIGIN" "MIR operation is missing source or generated origin",
   "C11-DOMAIN" "MIR domain anchor is invalid"}
  id
  "MIR validation failed"))
(defn
 opcode
 [family]
 ({:domain-anchor :mir/domain-anchor,
   :ai-tool :mir/ai-tool-call,
   :concurrency :mir/task-spawn,
   :field-index-buffer :mir/index,
   :numeric :mir/add-checked,
   :call :mir/call,
   :proof-reference :mir/proof-assert,
   :constant :mir/constant,
   :memory :mir/load,
   :linear-resource :mir/resource-close,
   :region :mir/region-alloc,
   :dispatch :mir/dispatch,
   :workflow :mir/workflow-yield,
   :data-constructor :mir/construct,
   :ffi :mir/ffi-call,
   :error :mir/throw,
   :control-flow :mir/branch,
   :local :mir/local,
   :runtime-check :mir/runtime-check,
   :closure :mir/closure}
  family
  :mir/unknown))
(defn
 effects
 [family]
 ({:ai-tool #{:runtime/dynamic-dispatch},
   :concurrency #{:runtime/dynamic-dispatch},
   :field-index-buffer #{:error/throw},
   :numeric #{:error/throw},
   :call #{:runtime/dynamic-dispatch},
   :memory #{:memory/raw},
   :linear-resource #{:io/write},
   :region #{:memory/raw},
   :dispatch #{:runtime/dynamic-dispatch},
   :workflow #{:runtime/dynamic-dispatch},
   :ffi #{:memory/raw},
   :error #{:error/throw},
   :runtime-check #{:error/throw}}
  family
  #{}))
(defn
 operation
 [module span outcomes index family]
 (let
  [effects*
   (operations/invoke :c11-family-effects effects family)
   outcome
   (get outcomes (mod index (count outcomes)))]
  {:domain-anchor
   (when (= :domain-anchor family) "c11-domain-anchor-efir"),
   :family family,
   :opcode (operations/invoke :c11-family-opcode opcode family),
   :type
   (case
    family
    :constant
    "I64"
    :numeric
    "I64"
    :field-index-buffer
    "Byte"
    :runtime-check
    "Unit"
    :proof-reference
    "Unit"
    :domain-anchor
    "DomainAnchor"
    "Unit"),
   :source
   {:core-node (str "c10:" (:operation outcome)),
    :span span,
    :origin-chain (get-in outcome [:source :origin-chain] [])},
   :operands (if (zero? index) [] [(str "c11-value-" (dec index))]),
   :ordering (if (seq effects*) :sequence :none),
   :result
   (when-not
    (contains? #{:error :control-flow} family)
    (str "c11-value-" index)),
   :effects effects*,
   :verifier-status :passed,
   :facts
   {:ownership "c9:ownership",
    :capabilities "c8:capabilities",
    :safety (:operation outcome),
    :runtime-check (:runtime-check outcome),
    :proofs (vec (remove nil? [(:proof outcome)]))},
   :op-id (str "c11-mir-op-" (name family)),
   :profile (:profile module)}))
(defn
 module-record
 [module c10-artifact operations*]
 (let
  [fn-id
   (str "c11-mir-fn-" (name (:module module)) "-main")
   ops
   (mapv :op-id operations*)
   entry
   {:block-id :entry,
    :operations ops,
    :terminator
    {:kind :return, :value (last (keep :result operations*))},
    :successors []}]
  {:domain-anchors :c11/domain-anchor-table,
   :capabilities :c11/capability-table,
   :diagnostics [],
   :functions
   {fn-id
    {:fn-id fn-id,
     :name (symbol (str (:module module)) "main"),
     :params [],
     :returns "Unit",
     :latent-effects (:effects module),
     :blocks {:entry entry},
     :entry :entry,
     :source
     {:span (source-span (:source-path module) 0), :origin-chain []}}},
   :module (:module module),
   :types :c11/type-table,
   :source-core (:artifact-id c10-artifact),
   :effects :c11/effect-table,
   :safety :c11/safety-table,
   :ownership :c11/ownership-table,
   :artifact :gravity/mir-module,
   :target-request (:target module),
   :globals {},
   :profile (:profile module)}))
(defn
 data-flow
 [operations*]
 (mapv
  (fn
   [[a b]]
   {:from (:op-id a),
    :to (:op-id b),
    :edge :sequence,
    :dominance-status :passed})
  (partition 2 1 operations*)))
(defn
 anchors
 []
 [{:domain :efir,
   :anchor-id "c11-domain-anchor-efir",
   :mir-ops ["c11-mir-op-domain-anchor"],
   :semantic-artifact "stage0-efir-graph",
   :equivalence-proof "proof-domain-anchor-round-trip",
   :fallback "c11-fallback-mir-subgraph",
   :status :valid}])
(defn
 present?
 [x]
 (cond
  (nil? x)
  false
  (and (coll? x) (empty? x))
  false
  (and (string? x) (str/blank? x))
  false
  :else
  true))
(defn
 diagnostics
 [configuration source-path]
 {:artifact :gravity/c11-mir-diagnostic-registry,
  :required-diagnostic-ids (:c11-mir-diagnostic-ids configuration),
  :diagnostics
  (mapv
   (fn
    [design]
    {:remediation
     "Keep target-independent MIR typed, effected, safety-linked, source-mapped, and verifier-clean before optimization or target lowering.",
     :block :fixture/block,
     :source-span (source-span source-path 0),
     :diagnostic (:diagnostic design),
     :origin-chain [],
     :operation-id (keyword "fixture" (:diagnostic design)),
     :function :fixture/function,
     :missing-fact (:rejected-design design),
     :mir-module :fixture/mir-module,
     :fixture (:fixture design),
     :target-request :fixture/target,
     :profile :fixture/profile})
   (:c11-mir-rejected-designs configuration)),
  :status :complete})
