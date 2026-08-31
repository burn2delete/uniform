(ns gravity.core-ast-lowering.assertions
  "Stable L2 legality checks and diagnostics."
  (:require [clojure.string :as str]))

(defn assert-recur-target!
  [fail! _module syntax form context]
  (when (= 'recur (first form))
    (let [target-arity (:recur-arity context)
          actual-arity (count (rest form))]
      (when (or (nil? target-arity) (not= target-arity actual-arity))
        (fail! "L2-RECUR-TARGET"
               "recur has no compatible loop or function target"
               {:source-span (:span syntax)
                :form form
                :target-arity target-arity
                :actual-arity actual-arity
                :remediation
                "Use recur only inside a compatible loop or function recur point with matching arity."})))))

(defn assert-set-target!
  [fail! module syntax form]
  (when (= 'set! (first form))
    (let [[_ target] form]
      (when-not (and (symbol? target)
                     (str/starts-with? (name target) "mutable-"))
        (fail! "L2-SET-ILLEGAL"
               "set! targets an immutable or profile-forbidden location"
               {:source-span (:span syntax)
                :target target
                :profile (:profile module)
                :remediation
                "Use an explicit mutable location accepted by the active profile."})))))

(defn assert-throw-legal!
  [fail! module syntax form]
  (when (and (= 'throw (first form))
             (not (contains? (:effects module) :error/throw)))
    (fail! "L2-THROW-ILLEGAL"
           "throw requires an error effect in the namespace"
           {:source-span (:span syntax)
            :declared-effects (:effects module)
            :required-effect :error/throw
            :remediation
            "Declare :error/throw or lower to an explicit result value."})))

(defn assert-core-operator!
  [fail! operation-value lowering-gap-forms unknown-reserved-core-forms
   _module syntax form]
  (when (seq? form)
    (let [op (first form)]
      (cond
        (contains? (operation-value :unknown-reserved-core-forms
                                    unknown-reserved-core-forms)
                   op)
        (fail! "L2-UNKNOWN-CORE-FORM"
               "analyzer found an unrecognized reserved core form"
               {:source-span (:span syntax)
                :operator op
                :remediation
                "Use an L2 core form, a call, or a documented domain IR boundary."})

        (contains? (operation-value :lowering-gap-forms lowering-gap-forms) op)
        (fail! "L2-LOWERING-GAP"
               "surface form failed to lower to core or a declared domain IR boundary"
               {:source-span (:span syntax)
                :operator op
                :remediation
                "Lower the surface form to L2 core before core analysis."})

        (= 'reorder-effects op)
        (fail! "L2-EVAL-ORDER"
               "transformation changed required evaluation order for effectful expressions"
               {:source-span (:span syntax)
                :operator op
                :remediation
                "Preserve left-to-right order for effectful expressions or prove purity before reordering."})

        (= 'host-exception op)
        (fail! "L2-HOST-SEMANTICS"
               "code depends on host behavior not represented in Gravity semantics"
               {:source-span (:span syntax)
                :operator op
                :remediation
                "Normalize host behavior into Gravity error, type, effect, and capability contracts."})

        :else nil))))
