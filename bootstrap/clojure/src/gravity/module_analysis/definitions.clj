(ns gravity.module-analysis.definitions)

(defn top-level-definition
  [syntax]
  (let [form (:form syntax)]
    (when (seq? form)
      (case (first form)
        def {:name (second form) :kind :var}
        defconst {:name (second form) :kind :compile-time-constant}
        defn {:name (second form) :kind :function}
        defmacro {:name (second form) :kind :macro}
        defschema {:name (second form) :kind :schema}
        defprotocol {:name (second form) :kind :protocol}
        nil))))

(defn definition-table
  [{:keys [top-level-definition]} syntax module]
  (let [exports (set (:exports module))]
    (->> syntax
         (keep (fn [syn]
                 (when-let [definition (top-level-definition syn)]
                   (when (symbol? (:name definition))
                     (merge definition
                            {:visibility (if (contains? exports (:name definition))
                                           :public
                                           :private)
                             :source-span (:span syn)
                             :profile (:profile module)
                             :safety (:safety module)
                             :latent-effects #{}
                             :required-capabilities #{}
                             :artifact-links []})))))
         vec)))

(defn collect-symbols
  [recur-collect form]
  (cond
    (symbol? form) [form]
    (seq? form) (mapcat recur-collect form)
    (coll? form) (mapcat recur-collect form)
    :else []))

(defn collect-code-symbols
  [recur-collect form]
  (cond
    (symbol? form) [form]
    (seq? form) (if (= 'quote (first form))
                  (if (symbol? (first form)) [(first form)] [])
                  (mapcat recur-collect form))
    (coll? form) (mapcat recur-collect form)
    :else []))
