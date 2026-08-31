(ns gravity.c6-core-lowering
  "Hosted Stage0 C6 AST/core-lowering engine.

   This facade preserves the bootstrap-compatible API while readable semantic
   leaves own lowering, projections, diagnostics, verification, and assembly."
  (:require [gravity.c6-core-lowering.artifact :as artifact]
            [gravity.c6-core-lowering.config :as config]
            [gravity.c6-core-lowering.context :as context]
            [gravity.c6-core-lowering.diagnostics :as diagnostics]
            [gravity.c6-core-lowering.lowering :as lowering]
            [gravity.c6-core-lowering.projection :as projection]
            [gravity.c6-core-lowering.verification :as verification]))

(def ^:private ^:dynamic *operations* {})
(def ^:private operation-keys context/operation-keys)
(def ^:private function-operation-keys context/function-operation-keys)
(def ^:private namespace-contract config/namespace-contract)
(def ^:private core-forms config/core-forms)
(def ^:private lowering-gap-forms config/lowering-gap-forms)
(def ^:private known-source-profiles config/known-source-profiles)
(def ^:private supported-targets config/supported-targets)

(defn- valid-keyword-set? [value]
  (and (set? value) (seq value) (every? keyword? value)))
(defn- valid-string-vector? [value]
  (and (vector? value) (seq value) (every? string? value)))
(defn- valid-map-of-keywords-to-strings? [value]
  (and (map? value)
       (every? (fn [[k v]] (and (keyword? k) (string? v))) value)))
(defn- valid-symbol-set? [value]
  (and (set? value) (every? symbol? value)))
(defn- valid-symbol-or-keyword-set? [value]
  (and (set? value) (seq value)
       (every? #(or (symbol? %) (keyword? %)) value)))
(defn- valid-operation-map! [operations]
  (context/valid-operation-map! operations))
(defn- default-fail! [id message data]
  (context/default-fail! id message data))
(defn- default-source-span [source-path form-index]
  (context/default-source-span source-path form-index))
(defn- default-c4-artifact-id [artifact]
  (context/default-c4-artifact-id artifact))
(defn- op-fn [key fallback]
  (or (get *operations* key) (context/op-fn key fallback)))
(defn- op-value [key fallback]
  (or (get *operations* key) (context/op-value key fallback)))
(defn- invoke-op [key fallback & args]
  (apply (op-fn key fallback) args))
(defn- fail! [id message data]
  ((op-fn :fail! default-fail!) id message data))
(defn- source-span [path index]
  ((op-fn :source-span default-source-span) path index))
(defn- c4-artifact-id [artifact]
  ((op-fn :c4-artifact-id default-c4-artifact-id) artifact))
(defn- form-effect [form]
  ((op-fn :form-effect (fn [_] #{})) form))
(defn- ns-form? [form]
  ((op-fn :ns-form? #(and (seq? %) (= 'ns (first %)))) form))

(def c6-lowering-diagnostic-ids config/c6-lowering-diagnostic-ids)
(def c6-lowering-governing-document config/c6-lowering-governing-document)
(def c6-lowering-rejected-designs config/c6-lowering-rejected-designs)
(def c6-lowering-override-diagnostics config/c6-lowering-override-diagnostics)
(def c6-domain-boundary-operators config/c6-domain-boundary-operators)
(def c6-core-node-forms config/c6-core-node-forms)

(declare public-api c6-lowering-source-overrides c6-lowering-message
         c6-lowering-fail! c6-lowering-validate-overrides! c6-node-id
         c6-core-node c6-lower-children c6-eval-order c6-form->core-form
         c6-lower-form c6-core-child-nodes c6-flatten-core
         c6-domain-boundary-records c6-surface-to-core-map
         c6-desugaring-trace c6-evaluation-order-records
         c6-core-verifier-report c6-rule-invalidation-record
         c6-lowering-capability-proof c6-lowering-validate!)

(defn c6-engine-contract []
  (assoc namespace-contract :public-api public-api))

(defn- facade-operations []
  {:core-forms core-forms
   :lowering-gap-forms lowering-gap-forms
   :known-source-profiles known-source-profiles :supported-targets supported-targets
   :c6-lowering-diagnostic-ids c6-lowering-diagnostic-ids
   :c6-lowering-governing-document c6-lowering-governing-document
   :c6-lowering-rejected-designs c6-lowering-rejected-designs
   :c6-lowering-override-diagnostics c6-lowering-override-diagnostics
   :c6-domain-boundary-operators c6-domain-boundary-operators
   :c6-core-node-forms c6-core-node-forms
   :c6-lowering-source-overrides c6-lowering-source-overrides
   :c6-lowering-message c6-lowering-message :c6-lowering-fail! c6-lowering-fail!
   :c6-lowering-validate-overrides! c6-lowering-validate-overrides!
   :c6-node-id c6-node-id :c6-core-node c6-core-node
   :c6-lower-children c6-lower-children :c6-eval-order c6-eval-order
   :c6-form->core-form c6-form->core-form :c6-lower-form c6-lower-form
   :c6-core-child-nodes c6-core-child-nodes :c6-flatten-core c6-flatten-core
   :c6-domain-boundary-records c6-domain-boundary-records
   :c6-surface-to-core-map c6-surface-to-core-map
   :c6-desugaring-trace c6-desugaring-trace
   :c6-evaluation-order-records c6-evaluation-order-records
   :c6-core-verifier-report c6-core-verifier-report
   :c6-rule-invalidation-record c6-rule-invalidation-record
   :c6-lowering-capability-proof c6-lowering-capability-proof
   :c6-lowering-validate! c6-lowering-validate!})

(def ^:private ^:dynamic *facade-call?* false)
(defn- leaf-call [f & args]
  (if *facade-call?*
    (apply f args)
    (binding [*facade-call?* true]
      (context/with-operations (merge (facade-operations) *operations*)
        #(apply f args)))))

(defn with-operations [operations thunk]
  (valid-operation-map! operations)
  (binding [*operations* (merge *operations* operations)
            *facade-call?* true]
    (context/with-operations operations thunk)))

(defn c6-lowering-source-overrides [module]
  (leaf-call diagnostics/c6-lowering-source-overrides module))
(defn c6-lowering-message [id]
  (leaf-call diagnostics/c6-lowering-message id))
(defn c6-lowering-fail! [id source-path subject extra]
  (leaf-call diagnostics/c6-lowering-fail! id source-path subject extra))
(defn c6-lowering-validate-overrides! [source-path module overrides]
  (leaf-call diagnostics/c6-lowering-validate-overrides!
             source-path module overrides))
(defn c6-node-id [counter] (leaf-call lowering/c6-node-id counter))
(defn c6-core-node [node-id form syntax module data]
  (leaf-call lowering/c6-core-node node-id form syntax module data))
(defn c6-lower-children [counter module syntax forms]
  (leaf-call lowering/c6-lower-children counter module syntax forms))
(defn c6-eval-order [form child-count]
  (leaf-call lowering/c6-eval-order form child-count))
(defn c6-form->core-form [form]
  (leaf-call lowering/c6-form->core-form form))
(defn c6-lower-form [counter module syntax form]
  (leaf-call lowering/c6-lower-form counter module syntax form))
(defn c6-core-child-nodes [value]
  (leaf-call lowering/c6-core-child-nodes value))
(defn c6-flatten-core [node] (leaf-call lowering/c6-flatten-core node))
(defn c6-domain-boundary-records [module expanded-stream c5-artifact]
  (leaf-call projection/c6-domain-boundary-records
             module expanded-stream c5-artifact))
(defn c6-surface-to-core-map [roots domain-boundaries]
  (leaf-call projection/c6-surface-to-core-map roots domain-boundaries))
(defn c6-desugaring-trace [roots]
  (leaf-call projection/c6-desugaring-trace roots))
(defn c6-evaluation-order-records [flat-nodes]
  (leaf-call projection/c6-evaluation-order-records flat-nodes))
(defn c6-core-verifier-report [flat-nodes domain-boundaries c5-artifact]
  (leaf-call verification/c6-core-verifier-report
             flat-nodes domain-boundaries c5-artifact))
(defn c6-rule-invalidation-record [roots]
  (leaf-call projection/c6-rule-invalidation-record roots))
(defn c6-lowering-capability-proof [artifact]
  (leaf-call verification/c6-lowering-capability-proof artifact))
(defn c6-lowering-validate! [source-path artifact]
  (leaf-call verification/c6-lowering-validate! source-path artifact))
(defn c6-lowering-artifact [source-path module c5-artifact expanded-stream]
  (leaf-call artifact/c6-lowering-artifact
             source-path module c5-artifact expanded-stream))

(def public-api
  {'public-api {:kind :contract}
   'c6-engine-contract {:arglists '([])}
   'with-operations {:arglists '([operations thunk])}
   'c6-lowering-artifact {:arglists '([source-path module c5-artifact expanded-stream])}
   'c6-lowering-diagnostic-ids {} 'c6-lowering-governing-document {}
   'c6-lowering-rejected-designs {} 'c6-lowering-override-diagnostics {}
   'c6-domain-boundary-operators {} 'c6-core-node-forms {}
   'c6-lowering-source-overrides {:arglists '([module])}
   'c6-lowering-message {:arglists '([id])}
   'c6-lowering-fail! {:arglists '([id source-path subject extra])}
   'c6-lowering-validate-overrides! {:arglists '([source-path module overrides])}
   'c6-node-id {:arglists '([counter])}
   'c6-core-node {:arglists '([node-id form syntax module data])}
   'c6-lower-children {:arglists '([counter module syntax forms])}
   'c6-eval-order {:arglists '([form child-count])}
   'c6-form->core-form {:arglists '([form])}
   'c6-lower-form {:arglists '([counter module syntax form])}
   'c6-core-child-nodes {:arglists '([value])}
   'c6-flatten-core {:arglists '([node])}
   'c6-domain-boundary-records {:arglists '([module expanded-stream c5-artifact])}
   'c6-surface-to-core-map {:arglists '([roots domain-boundaries])}
   'c6-desugaring-trace {:arglists '([roots])}
   'c6-evaluation-order-records {:arglists '([flat-nodes])}
   'c6-core-verifier-report {:arglists '([flat-nodes domain-boundaries c5-artifact])}
   'c6-rule-invalidation-record {:arglists '([roots])}
   'c6-lowering-capability-proof {:arglists '([artifact])}
   'c6-lowering-validate! {:arglists '([source-path artifact])}})
