

(defn p15-s23-stage2-compiler-artifact-binding!
  [emitter source-path target]
  (let [artifact-source (p15-s23-stage2-compiler-artifact-source-path)]
    (when-not (.isFile (java.io.File. artifact-source))
      (p15-s23-stage2-plan-emitter-fail!
       "P15S23Q001" artifact-source nil
       {:requested-source source-path
        :target target
        :missing-fields [:compiler-artifact-source]
        :missing-fact :stage2-expression-lowering-artifact}))
    (let [artifact-text (slurp artifact-source)
          plan
          (try
            (p15-s23-stage2-compiler-artifact-plan
             emitter artifact-source artifact-text)
            (catch clojure.lang.ExceptionInfo ex
              (let [data (ex-data ex)]
                (if (str/starts-with? (str (:id data)) "P15S23Q")
                  (throw ex)
                  (p15-s23-stage2-plan-emitter-fail!
                   "P15S23Q002" artifact-source nil
                   {:requested-source source-path
                    :target target
                    :missing-fact
                    :stage2-expression-lowering-function-shape
                    :cause-diagnostic (:id data)}))))
            (catch Exception ex
              (p15-s23-stage2-plan-emitter-fail!
               "P15S23Q002" artifact-source nil
               {:requested-source source-path
                :target target
                :missing-fact :stage2-expression-lowering-compilation
                :cause-message (.getMessage ex)})))
          functions (:functions plan)
          observed-arities
          (into {}
                (map (fn [[name _]]
                       [name (select-keys (get functions name)
                                          [:arity :params])]))
                p15-s23-stage2-compiler-artifact-required-functions)
          missing-functions
          (set (for [[name shape]
                     p15-s23-stage2-compiler-artifact-required-functions
                     :when (not= shape (get observed-arities name))]
                 name))
          semantic-hash
          (str "sha256:"
               (sha256-hex
                (pr-str
                 (c-backend-canonical-value
                  (p15-s23-stage2-compiler-artifact-semantic-input plan)))))]
      (when (seq missing-functions)
        (p15-s23-stage2-plan-emitter-fail!
         "P15S23Q002" artifact-source observed-arities
         {:requested-source source-path
          :target target
          :missing-fact :stage2-expression-lowering-function-shape
          :missing-functions (vec (sort-by str missing-functions))}))
      (when-not (= p15-s23-stage2-compiler-artifact-expected-semantic-hash
                   semantic-hash)
        (p15-s23-stage2-plan-emitter-fail!
         "P15S23Q002" artifact-source semantic-hash
         {:requested-source source-path
          :target target
          :missing-fact :stage2-expression-lowering-semantic-hash
          :expected-semantic-hash
          p15-s23-stage2-compiler-artifact-expected-semantic-hash
          :actual-semantic-hash semantic-hash}))
      (let [source-content-hash
            (str "sha256:" (sha256-hex artifact-text))]
        (when-not (= p15-s23-stage2-compiler-artifact-expected-source-content-hash
                     source-content-hash)
          (p15-s23-stage2-plan-emitter-fail!
           "P15S23Q002" artifact-source source-content-hash
           {:requested-source source-path
            :target target
            :missing-fact
            :stage2-expression-lowering-source-content-hash
            :expected-source-content-hash
            p15-s23-stage2-compiler-artifact-expected-source-content-hash
            :actual-source-content-hash source-content-hash})))
      {:artifact :gravity/p15-s23-stage2-expression-lowering-binding
       :status :complete
       :source-path artifact-source
       :source-content-hash
       p15-s23-stage2-compiler-artifact-expected-source-content-hash
       :semantic-hash semantic-hash
       :artifact-hash (str "sha256:"
                           (sha256-hex
                            (pr-str
                             (c-backend-canonical-value
                              {:source-content-hash
                               (str "sha256:" (sha256-hex artifact-text))
                               :semantic-hash semantic-hash}))))
       :functions observed-arities
       :plan plan
       :invoked? true
       :generic-bridge-residual? true
       :clojure-seed-boundary? true
       :self-hosted? false})))

(defn p15-s23-stage2-compiler-artifact-invoke
  [function args]
  (let [binding *p15-s23-stage2-compiler-artifact-binding*
        plan (:plan binding)
        definition (get-in plan [:functions function])]
    (when-not (and (= :complete (:status binding))
                   (map? plan)
                   (map? definition)
                   (= (:arity definition) (count args)))
      (p15-s23-stage2-plan-emitter-fail!
       "P15S23Q002" (:source-path binding) definition
       {:missing-fact :stage2-expression-lowering-invocation
        :function function
        :expected-arity (:arity definition)
        :actual-arity (count args)}))
    (p15-s23-stage2-runtime-execute-function
     {:engine :gravity-stage2-compiler-artifact-host-runner
      :compiler-artifact-plan? true}
     plan function args)))

(defn p15-s23-stage2-compiler-result!
  [module result]
  (if (= :complete (:status result))
    (:value result)
    (let [id (:diagnostic result)
          facts (:facts result)]
      (case id
        "L3-UNKNOWN-ALIAS"
        (fail! id "stage2 executable requires a main function"
               {:source-span {:source (:source-path module)}
                :entrypoint (:entrypoint facts)
                :remediation "Add (defn main [] ...)."})
        "L2-MAIN-ARITY"
        (fail! id "stage2 main must take no arguments"
               {:source-span {:source (:source-path module)}
                :params (:params facts)
                :remediation "Use (defn main [] ...)."})
        "L2-UNKNOWN-SYMBOL"
        (fail! id "stage2 plan emitter cannot resolve symbol"
               {:source-span {:source (:source-path module)}
                :symbol (:symbol facts)
                :remediation "Define the symbol or stay within the stage2 hosted-core subset."})
        "L2-LET-BINDING"
        (fail! id
               (if (contains? facts :binding)
                 "let binding name must be a symbol"
                 "let requires an even binding vector")
               (merge {:source-span {:source (:source-path module)}
                       :remediation
                       (if (contains? facts :binding)
                         "Bind symbols in stage2 let forms."
                         "Use pairs of local names and expressions in let.")}
                      facts))
        "P4-HOST-REFLECTION"
        (fail! id "host reflection is not implemented in stage2 plan emission"
               {:source-span {:source (:source-path module)}
                :remediation "Remove host reflection from the stage2 executable subset."})
        (fail! (or id "L2-UNKNOWN-CORE-FORM")
               "stage2 plan emitter cannot compile this form"
               {:source-span {:source (:source-path module)}
                :operator (:operator facts)
                :remediation "Use defn, println, do, if, let, quote, supported core builtins, or local function calls in the stage2 hosted-core subset."})))))

(defn p15-s23-stage2-compile-collection
  [emitter module locals form]
  (p15-s23-stage2-compiler-result!
   module
   (p15-s23-stage2-compiler-artifact-invoke
    p15-s23-stage2-compiler-artifact-collection-function
    [emitter module locals form])))

(defn p15-s23-stage2-compile-let
  [emitter module locals bindings body]
  (p15-s23-stage2-compiler-result!
   module
   (p15-s23-stage2-compiler-artifact-invoke
    p15-s23-stage2-compiler-artifact-let-function
    [emitter module locals bindings body])))

(defn p15-s23-stage2-compile-expr
  [emitter module locals form]
  (p15-s23-stage2-compiler-result!
   module
   (p15-s23-stage2-compiler-artifact-invoke
    p15-s23-stage2-compiler-artifact-expression-function
    [emitter module locals form])))

(defn p15-s23-stage2-compile-function
  [emitter module definition]
  (p15-s23-stage2-compiler-result!
   module
   (p15-s23-stage2-compiler-artifact-invoke
    p15-s23-stage2-compiler-artifact-function
    [emitter module definition])))

(defn p15-s23-stage2-assemble-plan-products
  [emitter module ordered-definitions]
  (p15-s23-stage2-compiler-result!
   module
   (p15-s23-stage2-compiler-artifact-invoke
    p15-s23-stage2-compiler-artifact-plan-assembly-function
    [emitter module ordered-definitions])))