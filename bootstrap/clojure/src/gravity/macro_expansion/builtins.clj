(ns gravity.macro-expansion.builtins
  (:require [gravity.macro-expansion.operations :as operations]))

(defn defn-output
  [args call-span ops]
  (let [[name params & body] args
        [return-type body] (if (= ':- (first body))
                             [(second body) (nnext body)]
                             [nil body])
        fn-form (if return-type
                  (list 'fn params
                        (list 'typed/return (list 'quote return-type)
                              (cons 'do body)))
                  (cons 'fn (cons params body)))]
    (when-not (and (symbol? name) (vector? params))
      (operations/fail!
       ops "L4-MACRO-NOT-SYNTAX"
       "defn expansion requires a symbolic name and vector parameters"
       {:source-span call-span
        :form (cons 'defn args)
        :remediation "Use (defn name [args] body...)."}))
    (list 'def name fn-form)))

(defn when-output
  [args call-span ops]
  (let [[condition & body] args]
    (when (nil? condition)
      (operations/fail!
       ops "L4-MACRO-NOT-SYNTAX" "when requires a condition"
       {:source-span call-span
        :remediation "Use (when condition body...)."}))
    (list 'if condition (cons 'do body) nil)))

(defn thread-first-step
  [value step]
  (if (seq? step)
    (apply list (first step) value (rest step))
    (list step value)))

(defn thread-first-output
  [args call-span ops]
  (let [[initial & steps] args
        thread-step (operations/operation ops :thread-first-step
                                          thread-first-step)]
    (when (nil? initial)
      (operations/fail!
       ops "L4-MACRO-NOT-SYNTAX"
       "thread-first requires an initial expression"
       {:source-span call-span
        :remediation "Use (-> value step...)."}))
    (reduce thread-step initial steps)))

(defn registry
  [builtin-macros ops]
  (let [macros (or (:builtin-macros ops) builtin-macros)]
    (reduce-kv (fn [acc name macro]
                 (assoc acc name macro (:identity macro) macro))
               {}
               macros)))
