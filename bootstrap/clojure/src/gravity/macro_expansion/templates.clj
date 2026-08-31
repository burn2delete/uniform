(ns gravity.macro-expansion.templates
  (:require [gravity.macro-expansion.operations :as operations]))

(defn parse-param-list
  [params ops]
  (loop [items (seq params)
         fixed []]
    (cond
      (nil? items) {:fixed fixed :rest nil}
      (= '& (first items))
      (let [rest-name (second items)]
        (when-not (and (symbol? rest-name) (nil? (nnext items)))
          (operations/fail!
           ops "L4-MACRO-NOT-SYNTAX"
           "macro rest parameter must be a single symbol"
           {:params params
            :remediation "Use a parameter vector such as [x & body]."}))
        {:fixed fixed :rest rest-name})
      (symbol? (first items)) (recur (next items) (conj fixed (first items)))
      :else
      (operations/fail!
       ops "L4-MACRO-NOT-SYNTAX" "macro parameters must be symbols"
       {:params params :remediation "Use symbolic macro parameters."}))))

(defn bind-macro-arguments
  [macro args call-span ops]
  (let [{:keys [fixed rest]} (:params macro)]
    (when (or (< (count args) (count fixed))
              (and (nil? rest) (not= (count args) (count fixed))))
      (operations/fail!
       ops "L4-MACRO-NOT-SYNTAX"
       "macro call does not match the accepted syntax shape"
       {:source-span call-span
        :macro (:identity macro)
        :params fixed
        :rest rest
        :argument-count (count args)
        :remediation
        "Call the macro with the syntax shape declared by its parameter vector."}))
    (let [fixed-bindings (zipmap fixed (take (count fixed) args))]
      (if rest
        (assoc fixed-bindings rest (vec (drop (count fixed) args)))
        fixed-bindings))))

(declare expand-template)

(defn expand-template-items
  [env items ops]
  (let [splice-key (or (:splice-key ops) :gravity.macro-expansion/splice)
        expand (or (:expand-template ops)
                   (fn [inner-env template]
                     (expand-template inner-env template ops)))]
    (mapcat (fn [item]
              (let [expanded (expand env item)]
                (if (and (map? expanded) (contains? expanded splice-key))
                  (get expanded splice-key)
                  [expanded])))
            items)))

(defn macro-env-value
  [env sym ops]
  (if (contains? env sym)
    (get env sym)
    (operations/fail!
     ops "L4-MACRO-NOT-SYNTAX"
     "syntax template references an unbound macro parameter"
     {:symbol sym
      :remediation
      "Use only symbols bound by the macro parameter vector inside unquote forms."})))

(defn expand-template
  [env template ops]
  (let [splice-key (or (:splice-key ops) :gravity.macro-expansion/splice)
        expand (or (:expand-template ops)
                   (fn [inner-env value]
                     (expand-template inner-env value ops)))
        env-value (or (:macro-env-value ops)
                      (fn [inner-env sym]
                        (macro-env-value inner-env sym ops)))
        expand-items (or (:expand-template-items ops)
                         (fn [inner-env values]
                           (expand-template-items inner-env values ops)))]
    (cond
      (seq? template)
      (case (first template)
        unquote
        (let [[_ sym] template]
          (when-not (symbol? sym)
            (operations/fail!
             ops "L4-MACRO-NOT-SYNTAX"
             "unquote requires a macro parameter symbol"
             {:form template
              :remediation "Use (unquote name) inside syntax templates."}))
          (env-value env sym))

        splice-unquote
        (let [[_ sym] template
              value (env-value env sym)]
          (when-not (sequential? value)
            (operations/fail!
             ops "L4-MACRO-NOT-SYNTAX"
             "splice-unquote requires a rest parameter value"
             {:form template
              :value value
              :remediation
              "Use splice-unquote with a rest parameter such as body."}))
          {splice-key value})

        (apply list (expand-items env template)))

      (vector? template) (vec (expand-items env template))
      (map? template) (into {} (map (fn [[k v]]
                                      [(expand env k) (expand env v)])
                                    template))
      (set? template) (set (expand-items env template))
      :else template)))

(defn parse-syntax-template
  [macro call-span ops]
  (let [body (:body macro)]
    (when-not (and (= 1 (count body))
                   (seq? (first body))
                   (= 'syntax-quote (ffirst body)))
      (operations/fail!
       ops "L4-MACRO-NOT-SYNTAX"
       "stage0 macros must return syntax through a syntax-quote template"
       {:source-span call-span
        :macro (:identity macro)
        :body body
        :remediation
        "Return (syntax-quote form) from stage0 defmacro bodies."}))
    (second (first body))))
