(ns gravity.c6-core-lowering.projection
  "C6 domain-boundary, trace, mapping, and evaluation-order projections."
  (:require [gravity.c6-core-lowering.config :as config]
            [gravity.c6-core-lowering.context :as context]
            [gravity.c6-core-lowering.lowering :as lowering]))

(defn c6-domain-boundary-records [module expanded-stream c5-artifact]
  (vec
   (keep
    (fn [syntax]
      (let [form (:form syntax)]
        (when (and (seq? form)
                   (contains?
                    (context/op-value :c6-domain-boundary-operators
                                      config/c6-domain-boundary-operators)
                    (first form)))
          {:artifact :gravity/c6-domain-boundary-record
           :domain (case (first form)
                     defschema :schema-ir
                     defworkflow :workflow-graph-ir
                     defagent :ai-agent-ir
                     ui :ui-ir
                     query :query-ir
                     ai-form :ai-agent-ir)
           :owner-document "C12"
           :required-checker :domain-ir-verifier
           :source {:syntax-id (:syntax-id syntax)
                    :span (:span syntax)
                    :origin-chain (:generated-origin syntax)}
           :semantic-anchor
           {:source-syntax (:syntax-id syntax)
            :namespace (get-in c5-artifact
                               [:namespace-analysis :namespace])
            :future-typed-core :pending-c7}
           :profile (:profile module)
           :target (:target module)
           :effects (:effects module)
           :capabilities (:capabilities module)
           :fallback :lower-after-domain-verifier
           :status :declared})))
    expanded-stream)))

(defn c6-surface-to-core-map [roots domain-boundaries]
  {:artifact :gravity/c6-surface-to-core-map
   :entries
   (vec (concat
         (map (fn [root]
                {:surface-syntax (get-in root [:source :syntax-id])
                 :core-root (:node-id root)
                 :core-form (:form root)
                 :generated? (:generated? root)})
              roots)
         (map (fn [boundary]
                {:surface-syntax (get-in boundary [:source :syntax-id])
                 :domain-boundary (:domain boundary)
                 :status :accepted-domain-boundary})
              domain-boundaries)))
   :status :complete})

(defn c6-desugaring-trace [roots]
  {:artifact :gravity/c6-desugaring-trace
   :records
   (mapv (fn [root]
           {:surface-syntax (get-in root [:source :syntax-id])
            :surface-kind (:form root)
            :core-root (:node-id root)
            :introduced-forms
            (vec (keep #(when (:generated? %) (:form %))
                       (context/invoke-op :c6-flatten-core
                                          lowering/c6-flatten-core root)))
            :preserved #{:source-spans :metadata :profile
                         :capabilities :effects :generated-origin}
            :introduced-origin
            (mapv (fn [node]
                    {:core-node (:node-id node)
                     :reason :surface-or-macro-desugar
                     :from (get-in node [:source :syntax-id])})
                  (filter :generated?
                          (context/invoke-op :c6-flatten-core
                                             lowering/c6-flatten-core root)))
            :evaluation-order (:evaluation-order root)
            :diagnostics []})
         roots)
   :status :complete})

(defn c6-evaluation-order-records [flat-nodes]
  {:artifact :gravity/c6-evaluation-order-records
   :records
   (mapv (fn [node]
           {:core-node (:node-id node)
            :form (:form node)
            :order (:evaluation-order node)
            :effect-sensitive? (boolean (seq (:effects node)))
            :source (get node :source)})
         (filter #(seq (:evaluation-order %)) flat-nodes))
   :status :complete})

(defn c6-rule-invalidation-record [roots]
  {:artifact :gravity/c6-lowering-rule-invalidation
   :rule-version "stage0-c6.1"
   :rules (vec (sort (set (map :lowering-rule roots))))
   :invalidates [:typed-core :effects :ownership :safety :mir :diagnostics]
   :status :stable})
