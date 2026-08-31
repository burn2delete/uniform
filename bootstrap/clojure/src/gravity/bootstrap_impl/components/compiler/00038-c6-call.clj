
(defn- c6-call [f & args]
  (if *c6-leaf-call?*
    (apply f args)
    (binding [*c6-leaf-call?* true]
      (c6/with-operations (c6-lowering-ops) #(apply f args)))))
(defn c6-lowering-source-overrides [module]
  (c6-call c6/c6-lowering-source-overrides module))
(defn c6-lowering-message [id] (c6-call c6/c6-lowering-message id))
(defn c6-lowering-fail! [id source-path subject extra]
  (c6-call c6/c6-lowering-fail! id source-path subject extra))
(defn c6-lowering-validate-overrides! [source-path module overrides]
  (c6-call c6/c6-lowering-validate-overrides! source-path module overrides))
(defn c6-node-id [counter] (c6-call c6/c6-node-id counter))
(defn c6-core-node [node-id form syntax module data]
  (c6-call c6/c6-core-node node-id form syntax module data))
(defn c6-lower-children [counter module syntax forms]
  (c6-call c6/c6-lower-children counter module syntax forms))
(defn c6-eval-order [form child-count] (c6-call c6/c6-eval-order form child-count))
(defn c6-form->core-form [form] (c6-call c6/c6-form->core-form form))
(defn c6-lower-form [counter module syntax form]
  (c6-call c6/c6-lower-form counter module syntax form))
(defn c6-core-child-nodes [value] (c6-call c6/c6-core-child-nodes value))
(defn c6-flatten-core [node] (c6-call c6/c6-flatten-core node))
(defn c6-domain-boundary-records [module expanded-stream c5-artifact]
  (c6-call c6/c6-domain-boundary-records module expanded-stream c5-artifact))
(defn c6-surface-to-core-map [roots domain-boundaries]
  (c6-call c6/c6-surface-to-core-map roots domain-boundaries))
(defn c6-desugaring-trace [roots] (c6-call c6/c6-desugaring-trace roots))
(defn c6-evaluation-order-records [flat-nodes]
  (c6-call c6/c6-evaluation-order-records flat-nodes))
(defn c6-core-verifier-report [flat-nodes domain-boundaries c5-artifact]
  (c6-call c6/c6-core-verifier-report flat-nodes domain-boundaries c5-artifact))
(defn c6-rule-invalidation-record [roots]
  (c6-call c6/c6-rule-invalidation-record roots))
(defn c6-lowering-capability-proof [artifact]
  (c6-call c6/c6-lowering-capability-proof artifact))
(defn c6-lowering-validate! [source-path artifact]
  (c6-call c6/c6-lowering-validate! source-path artifact))