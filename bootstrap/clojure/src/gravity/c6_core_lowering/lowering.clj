(ns gravity.c6-core-lowering.lowering
  "Core-node construction and recursive C6 surface-form lowering."
  (:require [gravity.c6-core-lowering.config :as config]
            [gravity.c6-core-lowering.context :as context]
            [gravity.c6-core-lowering.diagnostics :as diagnostics]))

(defn c6-node-id [counter]
  (str "c6-core-" (let [id @counter] (swap! counter inc) id)))

(defn c6-core-node [node-id form syntax module data]
  (let [source {:syntax-id (:syntax-id syntax)
                :span (:span syntax)
                :origin-chain
                (vec (concat (when (:origin syntax)
                               [{:kind (:origin syntax)}])
                             (:generated-origin syntax)))}
        surface-form (:surface-form data)
        unsafe-metadata
        (when (= 'unsafe (and (seq? surface-form) (first surface-form)))
          {:unsafe-island :declared
           :safety-outcome :pending-safe6})]
    (merge {:artifact :gravity/core-node
            :node-id node-id
            :form form
            :children {}
            :source source
            :binding-context :namespace-root
            :profile (:profile module)
            :target (:target module)
            :metadata (:metadata syntax)
            :facts {:resolved-bindings :pending-c5-binding-table}
            :effects ((context/op-fn :form-effect context/form-effect)
                      (:form syntax))
            :capabilities (:capabilities module)
            :unsafe-metadata unsafe-metadata
            :generated? (boolean (seq (:generated-origin syntax)))}
           data)))

(declare c6-lower-form)

(defn c6-lower-children [counter module syntax forms]
  (mapv #(context/invoke-op :c6-lower-form c6-lower-form
                            counter module syntax %)
        forms))

(defn c6-eval-order [form child-count]
  (case form
    quote []
    if [:condition :then-or-else]
    do (mapv (fn [idx] [:expr idx]) (range child-count))
    let [:bindings-left-to-right :body-left-to-right]
    fn [:call-arguments-left-to-right]
    loop [:loop-bindings-left-to-right :body-left-to-right]
    recur [:arguments-left-to-right]
    def [:initializer]
    var []
    set! [:value]
    try [:body :matching-handler]
    throw [:value]
    match [:scrutinee :selected-clause]
    :call [:operator :arguments-left-to-right]
    :declared-primitive [:arguments-left-to-right]
    []))

(defn c6-form->core-form [form]
  (cond
    (not (seq? form)) (if (symbol? form) :symbol :literal)
    (contains? (context/op-value :core-forms config/core-forms)
               (first form))
    (first form)
    (= 'unsafe (first form)) :declared-primitive
    :else :call))

(defn- lower-bindings [counter module syntax bindings]
  (mapv (fn [[name expr]]
          {:name name
           :initializer (context/invoke-op :c6-lower-form c6-lower-form
                                           counter module syntax expr)})
        (partition 2 bindings)))

(defn- lower-sequential-body [counter module syntax bindings body]
  {:bindings (lower-bindings counter module syntax bindings)
   :body (context/invoke-op :c6-lower-children c6-lower-children
                            counter module syntax body)})

(defn- lower-compound-children [counter module syntax form core-form]
  (case core-form
    quote []
    if (context/invoke-op :c6-lower-children c6-lower-children
                          counter module syntax (rest form))
    do (context/invoke-op :c6-lower-children c6-lower-children
                          counter module syntax (rest form))
    let (let [[_ bindings & body] form]
          (lower-sequential-body counter module syntax bindings body))
    fn (let [[_ params & body] form]
         {:params params
          :body (context/invoke-op :c6-lower-children c6-lower-children
                                   counter module syntax body)})
    loop (let [[_ bindings & body] form]
           (lower-sequential-body counter module syntax bindings body))
    recur {:arguments (context/invoke-op :c6-lower-children c6-lower-children
                                         counter module syntax (rest form))}
    def (let [[_ name value] form]
          {:name name
           :value (context/invoke-op :c6-lower-form c6-lower-form
                                     counter module syntax value)})
    var {:name (second form)}
    set! (let [[_ target value] form]
           {:target target
            :value (context/invoke-op :c6-lower-form c6-lower-form
                                      counter module syntax value)})
    try (let [[_ body & handlers] form]
          {:body (context/invoke-op :c6-lower-form c6-lower-form
                                    counter module syntax body)
           :handlers handlers})
    throw {:value (context/invoke-op :c6-lower-form c6-lower-form
                                     counter module syntax (second form))}
    match (let [[_ value & clauses] form]
            {:scrutinee (context/invoke-op :c6-lower-form c6-lower-form
                                           counter module syntax value)
             :clauses
             (mapv (fn [[pattern expr]]
                     {:pattern pattern
                      :body (context/invoke-op :c6-lower-form c6-lower-form
                                               counter module syntax expr)})
                   (partition 2 clauses))})
    :declared-primitive
    {:operator (first form)
     :arguments (context/invoke-op :c6-lower-children c6-lower-children
                                   counter module syntax (rest form))}
    :call
    {:operator (first form)
     :arguments (context/invoke-op :c6-lower-children c6-lower-children
                                   counter module syntax (rest form))}))

(defn c6-lower-form [counter module syntax form]
  (let [core-form (context/invoke-op :c6-form->core-form
                                     c6-form->core-form form)
        node-id (context/invoke-op :c6-node-id c6-node-id counter)]
    (cond
      (and (seq? form)
           (contains? (context/op-value :c6-domain-boundary-operators
                                        config/c6-domain-boundary-operators)
                      (first form)))
      nil

      (and (seq? form) (= :call core-form)
           (contains? (context/op-value :lowering-gap-forms
                                        config/lowering-gap-forms)
                      (first form)))
      ((context/op-fn :c6-lowering-fail! diagnostics/c6-lowering-fail!)
       "C6-LOWERING-GAP" (:source-path module) syntax
       {:lowering-rule (first form)})

      (seq? form)
      (let [children (lower-compound-children counter module syntax
                                              form core-form)]
        (context/invoke-op
         :c6-core-node c6-core-node node-id core-form syntax module
         {:surface-form form
          :children children
          :evaluation-order
          (context/invoke-op :c6-eval-order c6-eval-order core-form
                             (if (map? children)
                               (count children)
                               (count children)))
          :lowering-rule (if (= :call core-form)
                           :declared-call
                           core-form)}))

      :else
      (context/invoke-op
       :c6-core-node c6-core-node node-id core-form syntax module
       {:surface-form form
        :children {}
        :evaluation-order []
        :lowering-rule core-form
        :value form}))))

(defn c6-core-child-nodes [value]
  (cond
    (and (map? value) (= :gravity/core-node (:artifact value))) [value]
    (map? value)
    (mapcat #(context/invoke-op :c6-core-child-nodes c6-core-child-nodes %)
            (vals value))
    (coll? value)
    (mapcat #(context/invoke-op :c6-core-child-nodes c6-core-child-nodes %)
            value)
    :else []))

(defn c6-flatten-core [node]
  (vec (cons node
             (mapcat #(context/invoke-op :c6-flatten-core c6-flatten-core %)
                     (context/invoke-op :c6-core-child-nodes
                                        c6-core-child-nodes
                                        (:children node))))))
