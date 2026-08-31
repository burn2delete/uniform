(ns gravity.module-analysis.effects)

(defn uses-println?
  [recur-check form]
  (cond
    (seq? form) (or (= 'println (first form)) (some recur-check form))
    (coll? form) (some recur-check form)
    :else false))

(defn infer-effects
  [uses-println? forms]
  (set (mapcat (fn [form]
                 (cond
                   (uses-println? form) [:io/write]
                   (and (seq? form) (= 'network-listen (first form)))
                   [:network/listen]
                   :else []))
               forms)))

(defn required-capabilities-for-effects
  [effect-capability effects]
  (set (keep effect-capability effects)))

(defn validate-module-effects!
  [{:keys [uses-println? fail!]} module]
  (let [forms (:forms module)
        writes? (some uses-println? forms)]
    (when (and writes? (not (contains? (:effects module) :io/write)))
      (fail! "L6-EFFECT-UNDECLARED"
             "println requires the :io/write effect"
             {:source-span {:source (:source-path module)}
              :required-effect :io/write
              :declared-effects (:effects module)
              :remediation "Add :io/write to the namespace effects."}))
    (when (and writes? (not (contains? (:capabilities module) :io/stdout)))
      (fail! "L3-CAPABILITY-MISSING"
             "println requires the :io/stdout capability"
             {:source-span {:source (:source-path module)}
              :required-capability :io/stdout
              :declared-capabilities (:capabilities module)
              :remediation "Add :io/stdout to the namespace capabilities."}))))
