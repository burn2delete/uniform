(ns gravity.core-ast-lowering.effects
  "Effect discovery and core-node construction for hosted Stage0 L2.")

(defn uses-println?
  [recur? form]
  (cond
    (seq? form) (or (= 'println (first form)) (some recur? form))
    (coll? form) (some recur? form)
    :else false))

(defn form-effect
  [uses-println? form]
  (cond
    (uses-println? form) #{:io/write}
    (and (seq? form) (= 'throw (first form))) #{:error/throw}
    (and (seq? form) (= 'set! (first form))) #{:state/write}
    :else #{}))

(defn combine-effects
  [effect-sets]
  (set (mapcat identity effect-sets)))

(defn core-node
  [form-effect node-id kind syntax form data]
  (merge {:node-id (str "stage0-core-" node-id)
          :kind kind
          :form form
          :source-span (:span syntax)
          :generated-origin (:generated-origin syntax)
          :profile (:profile syntax)
          :namespace (:namespace syntax)
          :effects (form-effect form)
          :capabilities #{}}
         data))
