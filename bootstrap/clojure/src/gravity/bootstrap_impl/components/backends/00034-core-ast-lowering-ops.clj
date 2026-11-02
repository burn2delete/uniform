

(defn- core-ast-lowering-ops
  []
  {:fail! fail!
   :macro-source-artifact macro-source-artifact
   :uses-println? uses-println?
   :core-forms core-forms
   :lowering-gap-forms lowering-gap-forms
   :unknown-reserved-core-forms unknown-reserved-core-forms
   :form-effect form-effect
   :combine-effects combine-effects
   :core-node core-node
   :lower-sequential-body lower-sequential-body
   :extract-pattern-guard extract-pattern-guard
   :lower-match-clauses lower-match-clauses
   :next-node-id next-node-id
   :assert-recur-target! assert-recur-target!
   :assert-set-target! assert-set-target!
   :assert-throw-legal! assert-throw-legal!
   :assert-core-operator! assert-core-operator!
   :lower-core-expr lower-core-expr
   :flatten-core flatten-core
   :core-source-artifact core-source-artifact})

(def ^:private ^:dynamic *core-ast-lowering-leaf-call?* false)

(defn- core-ast-lowering-call
  [operation-key operation & args]
  (if *core-ast-lowering-leaf-call?*
    (core-ast-lowering/call-entrypoint-body operation-key operation args)
    (binding [*core-ast-lowering-leaf-call?* true]
      (core-ast-lowering/with-operations
       (core-ast-lowering-ops)
       #(core-ast-lowering/call-entrypoint-body
         operation-key operation args)))))