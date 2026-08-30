(ns gravity.c11-mir
  "Hosted Stage0 C11 target-independent MIR construction and projection."
  (:require [gravity.c11-mir.artifact :as artifact]
            [gravity.c11-mir.operations :as operations]
            [gravity.c11-mir.policy :as policy]
            [gravity.c11-mir.records :as records]
            [gravity.c11-mir.validation :as validation]))

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

(defn- configuration
  []
  {:c11-mir-diagnostic-ids c11-mir-diagnostic-ids
   :c11-mir-governing-document c11-mir-governing-document
   :c11-mir-required-operation-families c11-mir-required-operation-families
   :c11-mir-rejected-designs c11-mir-rejected-designs
   :c11-mir-override-diagnostics c11-mir-override-diagnostics})

(defn c11-mir-source-overrides [module]
  (operations/invoke :c11-mir-source-overrides records/source-overrides module))
(defn c11-mir-message [id]
  (operations/invoke :c11-mir-message records/message id))
(defn c11-mir-fail! [id source-path subject extra]
  (validation/fail! (configuration) id source-path subject extra))
(defn c11-mir-validate-overrides! [source-path module overrides]
  (operations/invoke :c11-mir-validate-overrides!
                     (fn [path parsed overrides]
                       (validation/validate-overrides! (configuration) path parsed overrides))
                     source-path module overrides))
(defn c11-family-opcode [family]
  (operations/invoke :c11-family-opcode records/opcode family))
(defn c11-family-effects [family]
  (operations/invoke :c11-family-effects records/effects family))
(defn c11-mir-operation [module span outcome-by-index index family]
  (operations/invoke :c11-mir-operation records/operation
                     module span outcome-by-index index family))
(defn c11-mir-module-record [module c10-artifact operations]
  (operations/invoke :c11-mir-module-record records/module-record
                     module c10-artifact operations))
(defn c11-data-flow-graph [operations]
  (operations/invoke :c11-data-flow-graph records/data-flow operations))
(defn c11-domain-anchor-table []
  (operations/invoke :c11-domain-anchor-table records/anchors))
(defn c11-present? [value]
  (operations/invoke :c11-present? records/present? value))
(defn c11-mir-diagnostics [source-path]
  (operations/invoke :c11-mir-diagnostics
                     (fn [path] (records/diagnostics (configuration) path))
                     source-path))
(defn c11-mir-verifier-report [module operations data-flow domain-anchors diagnostics]
  (operations/invoke :c11-mir-verifier-report
                     (fn [mir ops flow anchors diagnostic-stream]
                       (validation/verifier (configuration) mir ops flow anchors diagnostic-stream))
                     module operations data-flow domain-anchors diagnostics))
(defn c11-mir-capability-proof [artifact]
  (operations/invoke :c11-mir-capability-proof validation/proof artifact))
(defn c11-mir-validate! [source-path artifact]
  (operations/invoke :c11-mir-validate!
                     (fn [path value] (validation/validate! (configuration) path value))
                     source-path artifact))
(defn compiler-c11-mir-source-artifact [source-path source-text]
  (operations/invoke :compiler-c11-mir-source-artifact
                     (fn [path text] (artifact/source-artifact (configuration) path text))
                     source-path source-text))
(defn compiler-c11-mir-file-artifact [path]
  (operations/invoke :compiler-c11-mir-file-artifact
                     (fn [source-path] (artifact/file-artifact (configuration) source-path))
                     path))

(defn with-operations [operations thunk]
  (policy/validate-operations! operations)
  (let [merged (merge (operations/current-operations) operations)]
    (binding [c11-mir-diagnostic-ids
              (get merged :c11-mir-diagnostic-ids c11-mir-diagnostic-ids)
              c11-mir-governing-document
              (get merged :c11-mir-governing-document c11-mir-governing-document)
              c11-mir-required-operation-families
              (get merged :c11-mir-required-operation-families
                   c11-mir-required-operation-families)
              c11-mir-rejected-designs
              (get merged :c11-mir-rejected-designs c11-mir-rejected-designs)
              c11-mir-override-diagnostics
              (get merged :c11-mir-override-diagnostics c11-mir-override-diagnostics)]
      (operations/with-operations operations thunk))))

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
   'c11-mir-verifier-report
   {:arglists '([module operations data-flow domain-anchors diagnostics])}
   'c11-mir-capability-proof {:arglists '([artifact])}
   'c11-mir-validate! {:arglists '([source-path artifact])}
   'compiler-c11-mir-source-artifact {:arglists '([source-path source-text])}
   'compiler-c11-mir-file-artifact {:arglists '([path])}})

(doseq [[name spec] public-api :when (:arglists spec)]
  (when-let [var (ns-resolve *ns* name)]
    (alter-meta! var assoc :arglists (:arglists spec))))

(defn c11-engine-contract []
  (policy/engine-contract public-api))
