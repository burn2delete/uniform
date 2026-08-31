

(defn check-typed-node
  [checker ctx node]
  (case (:kind node)
    :literal
    (typed-fact checker node (literal-type (:value node)) #{} #{}
                {:value (:value node)})

    :symbol
    (do
      (when-let [move (get-in @ctx [:moved-values (:name node)])]
        (typed-diagnostic! "L10-USE-AFTER-MOVE"
                           "owned value is used after transfer"
                           node
                           "Do not use a moved value unless ownership is returned explicitly."
                           {:name (:name node)
                            :moved-at (:moved-at move)}))
      (typed-fact checker node
                  (get-in @ctx [:variable-types (:name node)] "Dynamic")
                  #{} #{}
                  {:name (:name node)}))

    :quote
    (typed-fact checker node "QuotedData" #{} #{}
                {:value (:value node)})

    :if
    (let [children (mapv #(check-typed-node checker ctx %) (:children node))]
      (typed-fact checker node (common-type (rest children))
                  (collect-fact-effects children)
                  (collect-fact-capabilities children)
                  {:children children}))

    :do
    (let [children (mapv #(check-typed-node checker ctx %) (:children node))]
      (typed-fact checker node (if (seq children) (:type (last children)) "Nil")
                  (collect-fact-effects children)
                  (collect-fact-capabilities children)
                  {:children children}))

    :let
    (let [child (child-context ctx)
          bound-linear (atom [])
          binding-facts (mapv (fn [{:keys [name initializer]}]
                                (let [fact (check-typed-node checker child initializer)]
                                  (swap! child assoc-in [:variable-types name] (:type fact))
                                  (when (linear-type? (:type fact))
                                    (swap! child assoc-in [:linear-resources name]
                                           {:name name
                                            :resource-type (linear-resource-type (:type fact))
                                            :node-id (:node-id fact)
                                            :consumed? false})
                                    (swap! bound-linear conj name))
                                  fact))
                              (:bindings node))
          body-facts (mapv #(check-typed-node checker child %) (:children node))]
      (doseq [name @bound-linear]
        (let [state (get-in @child [:linear-resources name])]
          (when-not (:consumed? state)
            (typed-diagnostic! "L10-LINEAR-RESOURCE"
                               "linear resource was not consumed exactly once"
                               node
                               "Consume, transfer, or explicitly forget the linear resource under a privileged policy."
                               {:resource name
                                :resource-type (:resource-type state)}))))
      (typed-fact checker node (if (seq body-facts) (:type (last body-facts)) "Nil")
                  (collect-fact-effects (concat binding-facts body-facts))
                  (collect-fact-capabilities (concat binding-facts body-facts))
                  {:children (vec (concat binding-facts body-facts))}))

    :fn
    (let [child (child-context ctx)
          params (parse-typed-params (:params node))]
      (doseq [{:keys [name type]} params]
        (swap! child assoc-in [:variable-types name] type))
      (let [body (mapv #(check-typed-node checker child %) (:children node))
            latent-effects (collect-fact-effects body)
            capabilities (collect-fact-capabilities body)
            return-type (if (seq body) (:type (last body)) "Nil")
            signature {:node-id (:node-id node)
                       :params (mapv #(select-keys % [:name :type :rest? :declared?]) params)
                       :return-type return-type
                       :latent-effects latent-effects
                       :capabilities capabilities}]
        (record-checker! checker :function-signature-table signature)
        (typed-fact checker node (str "Fn[" (count params) "]->" return-type)
                    #{} #{}
                    {:latent-effects latent-effects
                     :children body})))

    :loop
    (let [child (child-context ctx)
          binding-facts (mapv (fn [{:keys [name initializer]}]
                                (let [fact (check-typed-node checker child initializer)]
                                  (swap! child assoc-in [:variable-types name] (:type fact))
                                  fact))
                              (:bindings node))
          body (mapv #(check-typed-node checker child %) (:children node))]
      (typed-fact checker node (if (seq body) (:type (last body)) "Nil")
                  (collect-fact-effects (concat binding-facts body))
                  (collect-fact-capabilities (concat binding-facts body))
                  {:children (vec (concat binding-facts body))}))

    :recur
    (let [args (mapv #(check-typed-node checker ctx %) (:arguments node))]
      (typed-fact checker node "Never"
                  (conj (collect-fact-effects args) :control/recur)
                  (collect-fact-capabilities args)
                  {:children args}))

    :def
    (let [value-fact (check-typed-node checker ctx (:value node))]
      (swap! ctx assoc-in [:variable-types (:name node)] (:type value-fact))
      (when (:compile-time-binding? node)
        (record-compile-time-binding! checker ctx node value-fact))
      (typed-fact checker node (:type value-fact)
                  (:effects value-fact)
                  (:capabilities value-fact)
                  {:name (:name node)
                   :children [value-fact]}))

    :var
    (typed-fact checker node (str "Var[" (:name node) "]") #{} #{}
                {:name (:name node)})

    :set!
    (let [value-fact (check-typed-node checker ctx (:value node))]
      (typed-fact checker node "Nil"
                  (conj (:effects value-fact) :state/write)
                  (:capabilities value-fact)
                  {:target (:target node)
                   :children [value-fact]}))

    :try
    (let [body (check-typed-node checker ctx (:body node))]
      (typed-fact checker node (:type body)
                  (:effects body)
                  (:capabilities body)
                  {:children [body]
                   :handlers (:handlers node)}))

    :throw
    (let [value-fact (check-typed-node checker ctx (:value node))]
      (record-checker! checker :thrown-error-effect-records
                       {:node-id (:node-id node)
                        :error-type (:type value-fact)
                        :effect :error/throw
                        :profile (:profile @ctx)
                        :source-span (:source-span node)})
      (typed-fact checker node "Never"
                  (conj (:effects value-fact) :error/throw)
                  (:capabilities value-fact)
                  {:children [value-fact]}))

    :match
    (check-match-node checker ctx node)

    :call
    (check-call-node checker ctx node)

    (typed-diagnostic! "L5-ANNOTATION-REQUIRED"
                       "typed core checker has no rule for this core form"
                       node
                       "Add a typed core rule before using this form.")))