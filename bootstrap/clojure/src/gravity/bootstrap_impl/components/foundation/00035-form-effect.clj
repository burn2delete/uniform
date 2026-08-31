

(defn form-effect
  [form]
  (core-ast-lowering-call
   :form-effect core-ast-lowering/form-effect form))

(defn combine-effects
  [& effect-sets]
  (apply core-ast-lowering-call
         :combine-effects core-ast-lowering/combine-effects effect-sets))

(defn core-node
  [node-id kind syntax form data]
  (core-ast-lowering-call
   :core-node core-ast-lowering/core-node
   node-id kind syntax form data))

(defn lower-sequential-body
  [counter module syntax forms context]
  (core-ast-lowering-call
   :lower-sequential-body core-ast-lowering/lower-sequential-body
   counter module syntax forms context))

(defn extract-pattern-guard
  [pattern]
  (core-ast-lowering-call
   :extract-pattern-guard core-ast-lowering/extract-pattern-guard pattern))

(defn lower-match-clauses
  [counter module syntax clauses context]
  (core-ast-lowering-call
   :lower-match-clauses core-ast-lowering/lower-match-clauses
   counter module syntax clauses context))

(defn next-node-id
  [counter]
  (core-ast-lowering-call
   :next-node-id core-ast-lowering/next-node-id counter))

(defn assert-recur-target!
  [module syntax form context]
  (core-ast-lowering-call
   :assert-recur-target! core-ast-lowering/assert-recur-target!
   module syntax form context))

(defn assert-set-target!
  [module syntax form]
  (core-ast-lowering-call
   :assert-set-target! core-ast-lowering/assert-set-target!
   module syntax form))

(defn assert-throw-legal!
  [module syntax form]
  (core-ast-lowering-call
   :assert-throw-legal! core-ast-lowering/assert-throw-legal!
   module syntax form))

(defn assert-core-operator!
  [module syntax form]
  (core-ast-lowering-call
   :assert-core-operator! core-ast-lowering/assert-core-operator!
   module syntax form))

(defn lower-core-expr
  [counter module syntax form context]
  (core-ast-lowering-call
   :lower-core-expr core-ast-lowering/lower-core-expr
   counter module syntax form context))

(defn flatten-core
  [node]
  (core-ast-lowering-call
   :flatten-core core-ast-lowering/flatten-core node))

(defn core-source-artifact
  [source-path source-text]
  (core-ast-lowering-call
   :core-source-artifact core-ast-lowering/core-source-artifact
   source-path source-text))