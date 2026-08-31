

(def c6-lowering-diagnostic-ids c6/c6-lowering-diagnostic-ids)
(def c6-lowering-governing-document c6/c6-lowering-governing-document)
(def c6-lowering-rejected-designs c6/c6-lowering-rejected-designs)
(def c6-lowering-override-diagnostics c6/c6-lowering-override-diagnostics)
(def c6-domain-boundary-operators c6/c6-domain-boundary-operators)
(def c6-core-node-forms c6/c6-core-node-forms)

(declare c6-lowering-source-overrides
         c6-lowering-message
         c6-lowering-fail!
         c6-lowering-validate-overrides!
         c6-node-id
         c6-core-node
         c6-lower-children
         c6-eval-order
         c6-form->core-form
         c6-lower-form
         c6-core-child-nodes
         c6-flatten-core
         c6-domain-boundary-records
         c6-surface-to-core-map
         c6-desugaring-trace
         c6-evaluation-order-records
         c6-core-verifier-report
         c6-rule-invalidation-record
         c6-lowering-capability-proof
         c6-lowering-validate!)

(defn- c6-lowering-ops []
  {:fail! fail! :source-span source-span :c4-artifact-id c4-artifact-id
   :form-effect form-effect :ns-form? ns-form? :core-forms core-forms
   :lowering-gap-forms lowering-gap-forms :known-source-profiles known-source-profiles
   :supported-targets supported-targets
   :c6-lowering-diagnostic-ids c6-lowering-diagnostic-ids
   :c6-lowering-governing-document c6-lowering-governing-document
   :c6-lowering-rejected-designs c6-lowering-rejected-designs
   :c6-lowering-override-diagnostics c6-lowering-override-diagnostics
   :c6-domain-boundary-operators c6-domain-boundary-operators
   :c6-core-node-forms c6-core-node-forms
   :c6-lowering-source-overrides c6-lowering-source-overrides
   :c6-lowering-message c6-lowering-message
   :c6-lowering-fail! c6-lowering-fail!
   :c6-lowering-validate-overrides! c6-lowering-validate-overrides!
   :c6-node-id c6-node-id
   :c6-core-node c6-core-node
   :c6-lower-children c6-lower-children
   :c6-eval-order c6-eval-order
   :c6-form->core-form c6-form->core-form
   :c6-lower-form c6-lower-form
   :c6-core-child-nodes c6-core-child-nodes
   :c6-flatten-core c6-flatten-core
   :c6-domain-boundary-records c6-domain-boundary-records
   :c6-surface-to-core-map c6-surface-to-core-map
   :c6-desugaring-trace c6-desugaring-trace
   :c6-evaluation-order-records c6-evaluation-order-records
   :c6-core-verifier-report c6-core-verifier-report
   :c6-rule-invalidation-record c6-rule-invalidation-record
   :c6-lowering-capability-proof c6-lowering-capability-proof
   :c6-lowering-validate! c6-lowering-validate!})