

(defn stage2-runtime-derived-packet-fail!
  [id message source-path requested-target subject extra]
  (fail! id message
         (merge {:severity :error
                 :stage :stage2-runtime-derived-packet
                 :diagnostic-family :c14-target-lowering
                 :backend :target-neutral-stage2
                 :target requested-target
                 :source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :primary {:span (or (:source-span subject)
                                     (source-span source-path 0))}
                 :fallback-status :rejected}
                extra)))

(def stage2-runtime-derived-source-targets
  ;; :llvm, the exact Linux LLVM target, and :wasm are admitted only to the
  ;; internal pure checked-core/C11
  ;; construction path.  Effectful reference authority remains explicitly
  ;; JVM-only below.
  #{:jvm :c :c-hosted :c11 :js :js-ts :llvm :llvm-x86_64-linux :wasm})

(def p15-s23-closed-runtime-operations
  #{:literal :quote :local :builtin-call :println :do :if :let})

(defn p15-s23-closed-runtime-inferred-type
  [instruction local-types _depth]
  ;; Keep inference iterative.  The enclosing plan walk is already explicit,
  ;; and this fact lookup must not reintroduce host-stack growth for nested
  ;; :if/:do/:let expressions or comparison operands.  The task/value stacks
  ;; preserve the original left-to-right binding and branch evaluation order.
  (loop [pending [{:kind :infer
                   :instruction instruction
                   :local-types local-types}]
         values []]
    (if (seq pending)
      (let [frame (peek pending)
            pending (pop pending)]
        (case (:kind frame)
          :infer
          (let [instruction (:instruction frame)
                local-types (:local-types frame)]
            (if (not (map? instruction))
              (recur pending (conj values :unknown))
              (case (:op instruction)
                (:literal :quote)
                (let [value (:value instruction)
                      inferred (cond
                                 (integer? value) :gravity/integer
                                 (boolean? value) :gravity/bool
                                 (string? value) :gravity/string
                                 (nil? value) :gravity/nil
                                 :else :unknown)]
                  (recur pending (conj values inferred)))

                :local
                (recur pending
                       (conj values (get local-types (:name instruction)
                                             :unknown)))

                :builtin-call
                (let [function (:function instruction)
                      arguments (:args instruction)]
                  (cond
                    (= 'str function)
                    (recur pending (conj values :gravity/string))

                    (and (contains? '#{= < <= > >=} function)
                         (vector? arguments)
                         (= 2 (count arguments)))
                    (recur
                     (conj pending
                           {:kind :comparison
                            :arguments arguments
                            :index 0
                            :local-types local-types}
                           {:kind :infer
                            :instruction (first arguments)
                            :local-types local-types})
                     values)

                    :else
                    (recur pending (conj values :unknown))))

                :println
                (recur pending (conj values :gravity/nil))

                :do
                (let [body (:body instruction)]
                  (if (and (vector? body) (seq body))
                    (recur
                     (conj pending
                           {:kind :infer
                            :instruction (peek body)
                            :local-types local-types})
                     values)
                    (recur pending (conj values :gravity/nil))))

                :if
                (recur
                 (conj pending
                       {:kind :if-result}
                       {:kind :infer
                        :instruction (:else instruction)
                        :local-types local-types}
                       {:kind :infer
                        :instruction (:then instruction)
                        :local-types local-types})
                 values)

                :let
                (let [bindings (:bindings instruction)
                      body (:body instruction)]
                  (if (and (vector? bindings)
                           (vector? body)
                           (every? #(and (map? %)
                                         (symbol? (:name %))
                                         (map? (:expr %)))
                                   bindings))
                    (recur
                     (conj pending
                           {:kind :let-binding
                            :bindings bindings
                            :index 0
                            :local-types local-types
                            :body body})
                     values)
                    (recur pending (conj values :unknown))))

                (recur pending (conj values :unknown)))))

          :comparison
          (let [operand-type (peek values)
                values (pop values)]
            (if (not= :gravity/integer operand-type)
              (recur pending (conj values :unknown))
              (if (< (inc (:index frame)) (count (:arguments frame)))
                (recur
                 (conj pending
                       (assoc frame
                              :index (inc (:index frame)))
                       {:kind :infer
                        :instruction (nth (:arguments frame)
                                          (inc (:index frame)))
                        :local-types (:local-types frame)})
                 values)
                (recur pending (conj values :gravity/bool)))))

          :if-result
          (let [else-type (peek values)
                values (pop values)
                then-type (peek values)
                values (pop values)]
            (recur pending
                   (conj values (if (= then-type else-type)
                                  then-type
                                  :unknown))))

          :let-binding
          (if (< (:index frame) (count (:bindings frame)))
            (let [binding (nth (:bindings frame) (:index frame))]
              (recur
               (conj pending
                     {:kind :let-binding-result
                      :bindings (:bindings frame)
                      :index (:index frame)
                      :local-types (:local-types frame)
                      :body (:body frame)}
                     {:kind :infer
                      :instruction (:expr binding)
                      :local-types (:local-types frame)})
               values))
            (if (seq (:body frame))
              (recur
               (conj pending
                     {:kind :infer
                      :instruction (peek (:body frame))
                      :local-types (:local-types frame)})
               values)
              (recur pending (conj values :gravity/nil))))

          :let-binding-result
          (let [inferred (peek values)
                values (pop values)
                binding (nth (:bindings frame) (:index frame))
                next-types (assoc (:local-types frame)
                                  (:name binding)
                                  inferred)]
            (recur
             (conj pending
                   {:kind :let-binding
                    :bindings (:bindings frame)
                    :index (inc (:index frame))
                    :local-types next-types
                    :body (:body frame)})
             values))))
      (peek values))))
