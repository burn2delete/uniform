

(defn c-backend-runtime-scalar-expression-supported?
  "Recognize the value subset that can be represented by the runtime C
  emitter.  This predicate is deliberately closed over lexical locals: an
  unbound local, collection, builtin, function call, or statement in a value
  position is rejected before any target bytes are emitted."
  [instruction locals]
  (and (map? instruction)
       (let [op (:op instruction)]
         (cond
           (#{:literal :quote} op)
           (c-backend-runtime-literal? (:value instruction))
           (= :local op)
           (c-backend-runtime-local-present? locals (:name instruction))
           (= :builtin-call op)
           (and (= 'str (:function instruction))
                (contains? #{1 2} (count (:args instruction)))
                (every? #(c-backend-runtime-string-expression-supported?
                           % locals)
                        (:args instruction)))
           (= :if op)
           (and (c-backend-runtime-test-expression-supported?
                 (:test instruction) locals)
                (c-backend-runtime-scalar-expression-supported?
                 (:then instruction) locals)
                (c-backend-runtime-scalar-expression-supported?
                 (:else instruction) locals))
           (= :let op)
           (let [bindings (:bindings instruction)
                 next-locals (c-backend-runtime-next-local-kinds
                              locals bindings)]
             (and (= 1 (count (:body instruction)))
                  (every? (fn [{:keys [name expr]}]
                            (and (symbol? name)
                                 (or (= :literal (:op expr))
                                     (= :quote (:op expr)))
                                 (c-backend-runtime-literal?
                                  (:value expr))))
                          bindings)
                  (c-backend-runtime-scalar-expression-supported?
                   (first (:body instruction)) next-locals)))
           :else false))))

(defn c-backend-runtime-test-expression-supported?
  "Tests are intentionally simple scalar values.  Nested `if`/`let` tests
  would require a temporary tagged value ABI; rejecting them keeps this
  bounded slice explicit and fail-closed while still allowing locals and all
  scalar literal spellings as conditions."
  [instruction locals]
  (and (map? instruction)
       (let [op (:op instruction)]
         (cond
           (#{:literal :quote} op)
           (c-backend-runtime-literal? (:value instruction))
           (= :local op)
           (c-backend-runtime-local-present? locals (:name instruction))
           :else false))))

(defn c-backend-runtime-instruction-supported?
  ([instruction] (c-backend-runtime-instruction-supported? instruction #{}))
  ([instruction locals]
   (and (map? instruction)
        (contains? c-backend-runtime-derived-instructions (:op instruction))
        (let [op (:op instruction)]
          (cond
            (#{:literal :quote} op)
            (c-backend-runtime-literal? (:value instruction))
            (= :local op)
            (c-backend-runtime-local-present? locals (:name instruction))
            (= :println op)
            (every? #(c-backend-runtime-scalar-expression-supported?
                       % locals)
                    (:args instruction))
            (= :do op)
            (every? #(c-backend-runtime-instruction-supported? % locals)
                    (:body instruction))
            (= :if op)
            (and (c-backend-runtime-test-expression-supported?
                  (:test instruction) locals)
                 (c-backend-runtime-instruction-supported?
                  (:then instruction) locals)
                 (c-backend-runtime-instruction-supported?
                  (:else instruction) locals))
            (= :let op)
            (let [bindings (:bindings instruction)
                  next-locals (c-backend-runtime-next-local-kinds
                               locals bindings)]
              (and (every? (fn [{:keys [name expr]}]
                             (and (symbol? name)
                                  (or (= :literal (:op expr))
                                      (= :quote (:op expr)))
                                  (c-backend-runtime-literal?
                                   (:value expr))))
                           bindings)
                   (every? #(c-backend-runtime-instruction-supported?
                             % next-locals)
                           (:body instruction))))
            :else false)))))

(defn c-backend-runtime-plan-supported?
  [plan]
  (every? (fn [[_ function]]
            (every? #(c-backend-runtime-instruction-supported? % #{})
                    (:instructions function)))
          (:functions plan)))

(defn c-backend-runtime-reject!
  [source-path target subject message unsupported-op missing-fact]
  (c-backend-fail! "B2-UNSUPPORTED"
                   message
                   source-path target subject
                   {:unsupported-op unsupported-op
                    :lowering-mode :runtime-derived
                    :missing-fact missing-fact
                    :remediation "Use the verified stage0 fallback or restrict the source to scalar literals, locals, println, direct string concatenation, do, if, and let."}))