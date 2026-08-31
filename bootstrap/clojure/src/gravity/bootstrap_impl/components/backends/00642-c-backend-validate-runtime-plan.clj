

(defn c-backend-validate-runtime-plan!
  "Fail closed before lowering when the explicitly requested runtime-derived
  mode sees an instruction outside its closed scalar control-flow subset.

  Validation is iterative so malformed or adversarially deep plans cannot
  exhaust the host stack.  The lexical-local set travels with each pending
  frame and value-position failures retain the stable parent `println`
  diagnostic used by the earlier runtime-derived slice."
  [source-path target plan]
  (loop [pending (vec (mapcat (fn [[_ function]]
                                (map (fn [instruction]
                                       {:kind :statement
                                        :instruction instruction
                                        :locals {}})
                                     (:instructions function)))
                              (:functions plan)))]
    (when-let [{:keys [kind instruction locals parent]} (peek pending)]
      (let [pending (pop pending)
            op (when (map? instruction) (:op instruction))]
        (when-not (map? instruction)
          (c-backend-runtime-reject!
           source-path target instruction
           "runtime-derived C lowering received a malformed instruction"
           op :instruction-record))
        (when-not (contains? c-backend-runtime-derived-instructions op)
          (c-backend-runtime-reject!
           source-path target (or parent instruction)
           "runtime-derived C lowering does not implement this instruction"
           op :runtime-c-lowering-rule))
        (case kind
          :expression
          (cond
            (#{:literal :quote} op)
            (do
              (when-not (c-backend-runtime-literal? (:value instruction))
                (c-backend-runtime-reject!
                 source-path target (or parent instruction)
                 "runtime-derived C lowering only accepts scalar literals"
                 op (if parent :runtime-c-value-lowering
                        :scalar-literal-lowering)))
              (recur pending))
            (= :local op)
            (do
              (when-not (c-backend-runtime-local-present?
                         locals (:name instruction))
                (c-backend-runtime-reject!
                 source-path target (or parent instruction)
                 "runtime-derived C lowering cannot resolve this local"
                 :local (if parent :runtime-c-value-lowering
                            :runtime-local-binding)))
              (recur pending))
            (= :builtin-call op)
            (let [str? (= 'str (:function instruction))
                  supported-arity? (contains? #{1 2}
                                               (count (:args instruction)))
                  direct-println? (= :println (:op parent))
                  operands? (and (vector? (:args instruction))
                                 (every? #(c-backend-runtime-string-expression-supported?
                                            % locals)
                                         (:args instruction)))]
              (when-not (and str? direct-println? supported-arity? operands?)
                (c-backend-runtime-reject!
                 source-path target (or parent instruction)
                 (if (and str? direct-println? supported-arity?)
                   "runtime-derived str requires one or two string scalar literals, quotes, or proven string locals"
                   (if (and str? direct-println?)
                     "runtime-derived str supports one or two operands"
                     "runtime-derived str is only supported as a direct println value"))
                 (:function instruction)
                 (if (and str? direct-println? supported-arity?)
                   :runtime-str-operand-lowering
                   (if (and str? direct-println?)
                     :runtime-str-arity-lowering
                     :runtime-c-value-lowering))))
              (recur pending))
            (= :if op)
            (do
              (when-not (c-backend-runtime-test-expression-supported?
                         (:test instruction) locals)
                (c-backend-runtime-reject!
                 source-path target (or parent instruction)
                 "runtime-derived C lowering requires a scalar local or literal test"
                 :if :runtime-c-test-lowering))
              (recur (into pending
                           [{:kind :expression
                             :instruction (:else instruction)
                             :locals locals
                             :parent instruction}
                            {:kind :expression
                             :instruction (:then instruction)
                             :locals locals
                             :parent instruction}
                            {:kind :expression
                             :instruction (:test instruction)
                             :locals locals
                             :parent instruction}])))
            (= :let op)
            (let [bindings (:bindings instruction)]
              (when-not (and (vector? bindings)
                             (vector? (:body instruction)))
                (c-backend-runtime-reject!
                 source-path target (or parent instruction)
                 "runtime-derived let has a malformed binding or body shape"
                 :let :runtime-let-shape))
              (let [next-locals (c-backend-runtime-next-local-kinds
                                 locals bindings)]
                (when-not (= 1 (count (:body instruction)))
                  (c-backend-runtime-reject!
                   source-path target (or parent instruction)
                   "runtime-derived let value requires one scalar body expression"
                   :let :runtime-c-value-lowering))
                (doseq [{:keys [name expr]} bindings]
                  (when-not (and (symbol? name)
                                 (#{:literal :quote} (:op expr))
                                 (c-backend-runtime-literal?
                                  (:value expr)))
                    (c-backend-runtime-reject!
                     source-path target (or parent instruction)
                     "runtime-derived let bindings must be scalar literals"
                     :let :runtime-let-binding-lowering)))
                (recur (conj pending {:kind :expression
                                      :instruction (first (:body instruction))
                                      :locals next-locals
                                      :parent instruction}))))
            :else
            (c-backend-runtime-reject!
             source-path target (or parent instruction)
             "runtime-derived C lowering does not implement this value expression"
             (or (some-> parent :op) op)
             :runtime-c-value-lowering))

          :statement
          (case op
            :literal (recur pending)
            :quote (recur pending)
            :local
            (do
              (when-not (c-backend-runtime-local-present?
                         locals (:name instruction))
                (c-backend-runtime-reject!
                 source-path target instruction
                 "runtime-derived C lowering cannot resolve this local"
                 :local :runtime-local-binding))
              (recur pending))
            :println
            (do
              (when-not (vector? (:args instruction))
                (c-backend-runtime-reject!
                 source-path target instruction
                 "runtime-derived println has a malformed argument shape"
                 :println :runtime-c-value-lowering))
              (recur (into pending
                           (map (fn [arg]
                                  {:kind :expression
                                   :instruction arg
                                   :locals locals
                                   :parent instruction})
                                (:args instruction)))))
            :do
            (do
              (when-not (vector? (:body instruction))
                (c-backend-runtime-reject!
                 source-path target instruction
                 "runtime-derived do has a malformed body shape"
                 :do :runtime-c-lowering-rule))
              (recur (into pending
                           (map (fn [child]
                                  {:kind :statement
                                   :instruction child
                                   :locals locals})
                                (:body instruction)))))
            :if
            (do
              (when-not (c-backend-runtime-test-expression-supported?
                         (:test instruction) locals)
                (c-backend-runtime-reject!
                 source-path target instruction
                 "runtime-derived C lowering requires a scalar local or literal test"
                 :if :runtime-c-test-lowering))
              (recur (into pending
                           [{:kind :statement
                             :instruction (:else instruction)
                             :locals locals}
                            {:kind :statement
                             :instruction (:then instruction)
                             :locals locals}
                            {:kind :expression
                             :instruction (:test instruction)
                             :locals locals}])) )
            :let
            (let [bindings (:bindings instruction)]
              (when-not (and (vector? bindings)
                             (vector? (:body instruction)))
                (c-backend-runtime-reject!
                 source-path target instruction
                 "runtime-derived let has a malformed binding or body shape"
                 :let :runtime-let-shape))
              (let [next-locals (c-backend-runtime-next-local-kinds
                                 locals bindings)]
                (doseq [{:keys [name expr]} bindings]
                  (when-not (and (symbol? name)
                                 (#{:literal :quote} (:op expr))
                                 (c-backend-runtime-literal?
                                  (:value expr)))
                    (c-backend-runtime-reject!
                     source-path target instruction
                     "runtime-derived let bindings must be scalar literals"
                     :let :runtime-let-binding-lowering)))
                (recur (into pending
                             (map (fn [child]
                                    {:kind :statement
                                     :instruction child
                                     :locals next-locals})
                                  (:body instruction))))))
            ;; A statement value is legal only for the closed scalar forms.
            (c-backend-runtime-reject!
             source-path target instruction
             "runtime-derived C lowering does not implement this statement"
             op :runtime-c-lowering-rule))))))
  :passed)

(defn c-backend-resolve-p15-s23-compiler-source-path
  "Resolve the repository-owned P15-S23 source even when a direct caller is
  running from an unrelated working directory."
  []
  (let [relative p15-s23-compiler-source-path
        classpath-roots
        (keep (fn [entry]
                (let [file (java.io.File. entry)]
                  (when (.isDirectory file) file)))
              (str/split (System/getProperty "java.class.path" "")
                         (re-pattern (java.io.File/pathSeparator))))
        roots (distinct (cons (java.io.File.
                               (System/getProperty "user.dir"))
                              classpath-roots))]
    (or (some (fn [root]
                (loop [directory root]
                  (let [candidate (java.io.File. directory relative)]
                    (cond
                      (.isFile candidate) (.getPath candidate)
                      (.getParentFile directory)
                      (recur (.getParentFile directory))
                      :else nil))))
              roots)
        relative)))