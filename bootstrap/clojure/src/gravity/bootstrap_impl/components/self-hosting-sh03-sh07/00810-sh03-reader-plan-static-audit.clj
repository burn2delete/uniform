

(defn sh03-reader-plan-static-audit!
  [source-path plan]
  (let [functions (:functions plan)
        function-arities
        (into {} (map (fn [[name function]] [name (:arity function)]))
              functions)
        initial-tasks
        (reduce
         (fn [tasks [_ function]]
           (let [params (:params function)
                 target {:kind :function :arity (:arity function)}]
             (into tasks
                   (sh03-reader-plan-sequence-tasks
                    (:instructions function) (set params) target true 0))))
         [] functions)]
    (loop [pending initial-tasks
           observed 0
           summary {}
           builtins #{}
           maximum-depth 0]
      (when (> observed sh03-reader-plan-maximum-nodes)
        (sh03-reader-boundary-fail!
         source-path :bounded-static-sh03-reader-plan
         observed {:maximum-nodes sh03-reader-plan-maximum-nodes}))
      (if (empty? pending)
        {:instruction-summary summary
         :builtin-functions builtins
         :observed-nodes observed
         :observed-depth maximum-depth}
        (let [{:keys [instruction locals target tail? depth]} (peek pending)
              pending (pop pending)
              op (:op instruction)
              expected-keys (get sh03-reader-instruction-keysets op)]
          (when-not (and (map? instruction)
                         (contains? sh03-reader-allowed-opcodes op)
                         (= expected-keys (set (keys instruction)))
                         (<= depth sh03-reader-plan-maximum-depth))
            (sh03-reader-boundary-fail!
             source-path :exact-bounded-sh03-reader-instruction
             instruction {:opcode op :depth depth}))
          (let [tasks
                (case op
                  (:literal :quote) []

                  :local
                  (do
                    (when-not (contains? locals (:name instruction))
                      (sh03-reader-boundary-fail!
                       source-path :lexically-bound-sh03-reader-local
                       instruction {:local (:name instruction)}))
                    [])

                  (:vector-literal :set-literal)
                  (mapv #(sh03-reader-plan-task
                          % locals target false (inc depth))
                        (:items instruction))

                  :map-literal
                  (reduce
                   (fn [result entry]
                     (when-not (and (map? entry)
                                    (= #{:key :value} (set (keys entry))))
                       (sh03-reader-boundary-fail!
                        source-path :exact-sh03-reader-map-entry
                        entry {:opcode op}))
                     (conj result
                           (sh03-reader-plan-task
                            (:key entry) locals target false (inc depth))
                           (sh03-reader-plan-task
                            (:value entry) locals target false (inc depth))))
                   [] (:entries instruction))

                  :do
                  (sh03-reader-plan-sequence-tasks
                   (:body instruction) locals target tail? depth)

                  :if
                  [(sh03-reader-plan-task
                    (:test instruction) locals target false (inc depth))
                   (sh03-reader-plan-task
                    (:then instruction) locals target tail? (inc depth))
                   (sh03-reader-plan-task
                    (:else instruction) locals target tail? (inc depth))]

                  (:let :loop)
                  (let [bindings (:bindings instruction)
                        body (:body instruction)]
                    (when-not (and (vector? bindings)
                                   (vector? body)
                                   (seq body)
                                   (every? #(and (map? %)
                                                 (= #{:name :expr}
                                                    (set (keys %)))
                                                 (symbol? (:name %)))
                                           bindings)
                                   (= (count bindings)
                                      (count (distinct (map :name bindings))))
                                   (if (= :loop op)
                                     (= (:binding-count instruction)
                                        (count bindings))
                                     true))
                      (sh03-reader-boundary-fail!
                       source-path :exact-sh03-reader-local-bindings
                       instruction {:opcode op}))
                    (let [[binding-tasks body-locals]
                          (reduce
                           (fn [[result scope] binding]
                             [(conj result
                                    (sh03-reader-plan-task
                                     (:expr binding) scope target false
                                     (inc depth)))
                              (conj scope (:name binding))])
                           [[] locals] bindings)
                          body-target
                          (if (= :loop op)
                            {:kind :loop :arity (count bindings)}
                            target)
                          body-tail? (if (= :loop op) true tail?)]
                      (into binding-tasks
                            (sh03-reader-plan-sequence-tasks
                             body body-locals body-target body-tail? depth))))

                  :recur
                  (do
                    (when-not (and target tail?
                                   (= (:arity target)
                                      (count (:args instruction))))
                      (sh03-reader-boundary-fail!
                       source-path :valid-tail-sh03-reader-recur
                       instruction
                       {:target target :tail-position? tail?
                        :actual-arity (count (:args instruction))}))
                    (mapv #(sh03-reader-plan-task
                            % locals target false (inc depth))
                          (:args instruction)))

                  :builtin-call
                  (do
                    (when-not (contains? sh03-reader-allowed-builtins
                                         (:function instruction))
                      (sh03-reader-boundary-fail!
                       source-path :approved-pure-sh03-reader-builtin
                       instruction {:function (:function instruction)}))
                    (mapv #(sh03-reader-plan-task
                            % locals target false (inc depth))
                          (:args instruction)))

                  :function-call
                  (let [callee (:function instruction)
                        arity (get function-arities callee ::missing)]
                    (when-not (and (not= ::missing arity)
                                   (= arity (count (:args instruction))))
                      (sh03-reader-boundary-fail!
                       source-path :exact-sh03-reader-function-call
                       instruction
                       {:function callee :expected-arity
                        (when-not (= ::missing arity) arity)
                        :actual-arity (count (:args instruction))}))
                    (mapv #(sh03-reader-plan-task
                            % locals target false (inc depth))
                          (:args instruction))))]
            (recur (into pending (reverse tasks))
                   (inc observed)
                   (update summary op (fnil inc 0))
                   (cond-> builtins (= :builtin-call op)
                     (conj (:function instruction)))
                   (max maximum-depth depth))))))))