

(defn p15-s23-stage2-seed-validate-recur!
  [module form target-arity tail-position?]
  ;; Source forms are untrusted at this boundary.  Carry tail context on an
  ;; explicit worklist so deeply nested rejected input cannot consume the host
  ;; call stack before producing a structured diagnostic.
  (loop [pending [[form target-arity tail-position?]]
         observed 0]
    (when (> observed 262144)
      (p15-s23-stage2-seed-recur-fail!
       module form target-arity nil :validation-form-limit))
    (if (empty? pending)
      :passed
      (let [[current current-target current-tail?] (peek pending)
            pending (pop pending)
            tasks
            (cond
              (seq? current)
              (let [callee (first current)]
                (cond
                  (= callee 'quote)
                  []

                  (= callee 'recur)
                  (let [arguments (vec (rest current))
                        actual-arity (count arguments)]
                    (when (or (nil? current-target)
                              (not current-tail?)
                              (not= current-target actual-arity))
                      (p15-s23-stage2-seed-recur-fail!
                       module current current-target actual-arity
                       (cond
                         (nil? current-target) :missing-target
                         (not current-tail?) :non-tail-position
                         :else :arity-mismatch)))
                    (mapv #(vector % current-target false) arguments))

                  (= callee 'if)
                  (let [[_ test then else] current]
                    [[test current-target false]
                     [then current-target current-tail?]
                     [else current-target current-tail?]])

                  (= callee 'do)
                  (let [body (vec (rest current))
                        last-index (dec (count body))]
                    (mapv (fn [index body-form]
                            [body-form current-target
                             (and current-tail? (= index last-index))])
                          (range)
                          body))

                  (= callee 'let)
                  (let [[_ bindings & raw-body] current
                        body (vec raw-body)]
                    (when-not (and (vector? bindings)
                                   (even? (count bindings)))
                      (fail! "L2-LET-BINDING"
                             "let requires an even binding vector"
                             {:source-span {:source (:source-path module)}
                              :bindings bindings
                              :remediation
                              "Use pairs of local names and expressions in let."}))
                    (doseq [[name _] (partition 2 bindings)]
                      (when-not (symbol? name)
                        (fail! "L2-LET-BINDING"
                               "let binding name must be a symbol"
                               {:source-span
                                {:source (:source-path module)}
                                :binding name
                                :remediation
                                "Bind symbols in stage2 let forms."})))
                    (let [binding-tasks
                          (mapv (fn [[_ expression]]
                                  [expression current-target false])
                                (partition 2 bindings))
                          last-index (dec (count body))
                          body-tasks
                          (mapv (fn [index body-form]
                                  [body-form current-target
                                   (and current-tail?
                                        (= index last-index))])
                                (range)
                                body)]
                      (into binding-tasks body-tasks)))

                  (= callee 'loop)
                  (let [[_ bindings & raw-body] current
                        body (vec raw-body)]
                    (when-not (and (vector? bindings)
                                   (even? (count bindings)))
                      (p15-s23-stage2-seed-recur-fail!
                       module current nil nil :invalid-loop-bindings))
                    (doseq [[name _] (partition 2 bindings)]
                      (when-not (symbol? name)
                        (p15-s23-stage2-seed-recur-fail!
                         module current nil nil
                         :invalid-loop-binding-name)))
                    (let [nested-target (quot (count bindings) 2)]
                      (when (empty? body)
                        (p15-s23-stage2-seed-recur-fail!
                         module current nested-target nil
                         :missing-loop-body))
                      (let [binding-tasks
                            (mapv (fn [[_ expression]]
                                    [expression current-target false])
                                  (partition 2 bindings))
                            last-index (dec (count body))
                            body-tasks
                            (mapv (fn [index body-form]
                                    [body-form nested-target
                                     (= index last-index)])
                                  (range)
                                  body)]
                        (into binding-tasks body-tasks))))

                  :else
                  (mapv #(vector % current-target false)
                        (rest current))))

              (vector? current)
              (mapv #(vector % current-target false) current)

              (map? current)
              (mapv #(vector % current-target false)
                    (mapcat identity (sort-by pr-str current)))

              (set? current)
              (mapv #(vector % current-target false)
                    (sort-by pr-str current))

              :else
              [])]
        ;; `pending` is a LIFO vector.  Reverse newly discovered tasks so
        ;; diagnostics retain deterministic left-to-right source order.
        (recur (into pending (reverse tasks)) (inc observed))))))

(defn p15-s23-stage2-seed-compile-collection
  [emitter module locals form]
  (let [collection-rules (:collection-rules emitter)]
    (cond
      (vector? form)
      {:op (get-in collection-rules [:vector :op] :vector-literal)
       :items (mapv #(p15-s23-stage2-seed-compile-expr emitter module locals %)
                    form)}

      (map? form)
      {:op (get-in collection-rules [:map :op] :map-literal)
       :entries (mapv (fn [[k v]]
                        {:key (p15-s23-stage2-seed-compile-expr
                               emitter module locals k)
                         :value (p15-s23-stage2-seed-compile-expr
                                 emitter module locals v)})
                      (sort-by (fn [[k v]] (pr-str [k v])) form))}

      (set? form)
      {:op (get-in collection-rules [:set :op] :set-literal)
       :items (mapv #(p15-s23-stage2-seed-compile-expr emitter module locals %)
                    (sort-by pr-str form))}

      :else
      {:op (get-in collection-rules [:literal :op] :literal)
       :value form})))

(defn p15-s23-stage2-seed-compile-let
  [emitter module locals bindings body]
  (when-not (and (vector? bindings) (even? (count bindings)))
    (fail! "L2-LET-BINDING"
           "let requires an even binding vector"
           {:source-span {:source (:source-path module)}
            :bindings bindings
            :remediation "Use pairs of local names and expressions in let."}))
  (loop [scope locals
         compiled []
         pairs (partition 2 bindings)]
    (if-let [[name expr] (first pairs)]
      (do
        (when-not (symbol? name)
          (fail! "L2-LET-BINDING"
                 "let binding name must be a symbol"
                 {:source-span {:source (:source-path module)}
                  :binding name
                  :remediation "Bind symbols in stage2 let forms."}))
        (recur (conj scope name)
               (conj compiled
                     {:name name
                      :expr (p15-s23-stage2-seed-compile-expr
                             emitter module scope expr)})
               (rest pairs)))
      {:op (get-in emitter [:special-form-rules 'let :op] :let)
       :bindings compiled
       :body (mapv #(p15-s23-stage2-seed-compile-expr
                     emitter module scope %)
                   body)})))

(defn p15-s23-stage2-seed-compile-loop
  [emitter module locals bindings body]
  (when-not (and (vector? bindings) (even? (count bindings)))
    (p15-s23-stage2-seed-recur-fail!
     module (list 'loop bindings) nil nil :invalid-loop-bindings))
  (loop [scope locals
         compiled []
         pairs (partition 2 bindings)]
    (if-let [[name expr] (first pairs)]
      (do
        (when-not (symbol? name)
          (p15-s23-stage2-seed-recur-fail!
           module (list 'loop bindings) nil nil :invalid-loop-binding-name))
        (recur (conj scope name)
               (conj compiled
                     {:name name
                      :expr (p15-s23-stage2-seed-compile-expr
                             emitter module scope expr)})
               (rest pairs)))
      {:op :loop
       :bindings compiled
       :binding-count (count compiled)
       :body (mapv #(p15-s23-stage2-seed-compile-expr
                     emitter module scope %)
                   body)})))