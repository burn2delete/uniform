

(defn validate-stage0-compiled-self-hosting-validation!
  [module self-hosting-report]
  (when-not (:provenance-attestation self-hosting-report)
    (compiled-conformance-fail!
     "TEST13002" module self-hosting-report
     {:missing-fields [:provenance-attestation]})))

(defn validate-stage0-compiled-conformance!
  [module]
  (when (stage0-compiled-conformance-suite-present? module)
    (let [suite (stage0-compiled-conformance-suite module)]
      (doseq [fixture-manifest (:fixture-manifests suite)]
        (validate-stage0-compiled-fixture-manifest!
         module fixture-manifest))
      (doseq [compiler-report (:compiler-test-reports suite)]
        (validate-stage0-compiled-compiler-test-report!
         module compiler-report))
      (doseq [runtime-report (:runtime-conformance-reports suite)]
        (validate-stage0-compiled-runtime-conformance!
         module runtime-report))
      (doseq [profile-report (:profile-compliance-reports suite)]
        (validate-stage0-compiled-profile-compliance!
         module profile-report))
      (doseq [safety-report (:safety-conformance-reports suite)]
        (validate-stage0-compiled-safety-conformance!
         module safety-report))
      (doseq [backend-report (:backend-conformance-reports suite)]
        (validate-stage0-compiled-conformance-backend-report!
         module backend-report))
      (doseq [standard-library-report
              (:standard-library-test-reports suite)]
        (validate-stage0-compiled-standard-library-conformance!
         module standard-library-report))
      (doseq [ai-workflow-report (:ai-workflow-eval-reports suite)]
        (validate-stage0-compiled-ai-workflow-conformance!
         module ai-workflow-report))
      (doseq [fuzz-property-suite (:fuzz-property-suites suite)]
        (validate-stage0-compiled-fuzz-property-suite!
         module fuzz-property-suite))
      (doseq [differential-report (:differential-reports suite)]
        (validate-stage0-compiled-differential-report!
         module differential-report))
      (doseq [formal-proof-report (:formal-proof-reports suite)]
        (validate-stage0-compiled-formal-proof-report!
         module formal-proof-report))
      (doseq [performance-report (:performance-regression-reports suite)]
        (validate-stage0-compiled-performance-regression!
         module performance-report))
      (doseq [self-hosting-report
              (:self-hosting-validation-reports suite)]
        (validate-stage0-compiled-self-hosting-validation!
         module self-hosting-report)))))

(defn eval-do
  [module env forms]
  (reduce (fn [_ form] (eval-expr module env form)) nil forms))

(defn bind-let
  [module env bindings]
  (when-not (and (vector? bindings) (even? (count bindings)))
    (fail! "L2-LET-BINDING"
           "let requires an even binding vector"
           {:source-span {:source (:source-path module)}
            :bindings bindings
            :remediation "Use pairs of local names and expressions in let."}))
  (loop [env env
         pairs (partition 2 bindings)]
    (if-let [[name expr] (first pairs)]
      (do
        (when-not (symbol? name)
          (fail! "L2-LET-BINDING"
                 "let binding name must be a symbol"
                 {:source-span {:source (:source-path module)}
                  :binding name
                  :remediation "Bind symbols in stage0 let forms."}))
        (recur (assoc env name (eval-expr module env expr)) (rest pairs)))
      env)))

(defn function-definition-form
  [source-path form]
  (cond
    (defn-form? form)
    (let [[_ name params & body] form]
      {:name name :params params :body body :definition-form :defn})

    (def-function-form? form)
    (let [[_ params & body] (nth form 2)]
      {:name (second form) :params params :body body :definition-form :def-fn})

    :else nil))

(defn- seed-declared-header? [{:keys [params body]}]
  (or (and (vector? params) (some #(= :- %) params)) (= :- (first body))
      (and (seq? (first body)) (= 'typed/return (ffirst body)))))

(defn- normalize-seed-function-header
  [source-path {:keys [name params body] :as definition}]
  (if (and (symbol? name) (vector? params)
           (seed-declared-header? definition))
    (letfn [(reject! [reason]
              (fail! "L2-FUNCTION-PARAMS" "stage0 function declaration header is malformed"
                     {:source-span {:source source-path} :function name :params params
                      :reason reason :remediation "Use fixed arity names with supported type declarations."}))
            (type-token? [value] (and (symbol? value) (not= '& value)))
            (parameter-type? [value]
              (or (type-token? value)
                  (and (seq? value) (= 2 (count value)) (every? type-token? value))))]
      (loop [remaining (seq params) names []]
        (if (seq remaining)
          (let [parameter (first remaining) annotated? (= :- (second remaining))]
            (when-not (symbol? parameter) (reject! :parameter-name-required))
            (if annotated?
              (do (when-not (parameter-type? (nth remaining 2 nil))
                    (reject! (if (seq? (nth remaining 2 nil)) :parameter-type-shape-required :atomic-parameter-type-required)))
                  (recur (nthnext remaining 3) (conj names parameter)))
              (recur (next remaining) (conj names parameter))))
          (let [return? (= :- (first body)) wrapper (first body)
                wrapped? (and (seq? wrapper) (= 'typed/return (first wrapper)))
                quoted (when wrapped? (second wrapper)) block (when wrapped? (nth wrapper 2 nil))]
            (when (and wrapped?
                       (not (and (= 1 (count body)) (= 3 (count wrapper))
                                 (seq? quoted) (= 2 (count quoted)) (= 'quote (first quoted))
                                 (seq? block) (= 'do (first block)))))
              (reject! :malformed-return-declaration-wrapper))
            (let [type (if return? (second body) (when wrapped? (second quoted)))
                  logical-body (cond return? (nnext body) wrapped? (rest block) :else body)]
            (when (and (or return? wrapped?) (not (parameter-type? type)))
              (reject! :atomic-return-type-required))
            (when (and (or return? wrapped?) (= :- (first logical-body)))
              (reject! :repeated-return-declaration))
            (assoc definition :params names :body logical-body
                   :source-declarations {:parameters params
                                         :return (cond return? (vec (take 2 body))
                                                       wrapped? (vec (take 2 wrapper)) :else nil)}))))))
    definition))

(defn validate-function-definition!
  [source-path {:keys [name params body]}]
  (when-not (symbol? name)
    (fail! "L2-FUNCTION-PARAMS"
           "stage0 function name must be a symbol"
           {:source-span {:source source-path}
            :function name
            :remediation "Define functions with symbolic names."}))
  (when-not (and (vector? params) (every? symbol? params))
    (fail! "L2-FUNCTION-PARAMS"
           "stage0 function parameters must be a vector of symbols"
           {:source-span {:source source-path}
            :function name
            :params params
            :remediation "Use a fixed arity parameter vector such as [x y]."}))
  (when (or (some #{'&} params) (not= (count params) (count (distinct params))))
    (fail! "L2-FUNCTION-PARAMS" "stage0 function parameters require distinct fixed arity names"
           {:source-span {:source source-path} :function name :params params
            :reason (if (some #{'&} params) :variadic-parameters-unsupported :duplicate-parameter-names)
            :remediation "Use distinct logical parameter names without a variadic marker."}))
  (when-not (seq body)
    (fail! "L2-FUNCTION-PARAMS"
           "stage0 function body cannot be empty"
           {:source-span {:source source-path}
            :function name
            :remediation "Add at least one body form."})))

(defn- validate-seed-function-names! [source-path definitions]
  (let [duplicate (first (for [[name n] (frequencies (map :name definitions)) :when (> n 1)] name))]
    (when duplicate
      (fail! "L2-DUPLICATE-FUNCTION"
             "stage0 executable has duplicate function definitions"
             {:source-span {:source source-path}
              :function duplicate
              :remediation "Keep one definition per stage0 function name."}))))

(defn stage0-function-table
  [module]
  (let [source-path (:source-path module)
        definitions (keep #(function-definition-form source-path %) (:forms module))]
    (validate-seed-function-names! source-path definitions)
    (into {}
          (map (fn [raw-definition]
                 (let [definition (normalize-seed-function-header source-path raw-definition)]
                 (validate-function-definition! source-path definition)
                 [(:name definition)
                  (assoc definition
                         :arity (count (:params definition))
                         :body-form-count (count (:body definition)))])))
          definitions)))

(defn- validate-seed-source-headers! [source-path forms]
  (let [definitions (keep #(function-definition-form source-path %) forms)]
    (when (some seed-declared-header? definitions)
      (validate-seed-function-names! source-path definitions)
      (doseq [definition definitions :when (seed-declared-header? definition)]
        (validate-function-definition! source-path
          (normalize-seed-function-header source-path definition))))))

(defn fail-call-arity!
  [id module callee args expected]
  (fail! id
         (case id
           "L2-FUNCTION-ARITY" "stage0 function call has the wrong arity"
           "L2-BUILTIN-ARITY" "stage0 builtin call has the wrong arity"
           "stage0 call has the wrong arity")
         {:source-span {:source (:source-path module)}
          :function callee
          :expected-arity expected
          :actual-arity (count args)
          :remediation "Call the function with the arity supported by the stage0 hosted subset."}))

(defn assert-min-arity!
  [module callee args n]
  (when (< (count args) n)
    (fail-call-arity! "L2-BUILTIN-ARITY" module callee args
                      (str "at least " n))))

(defn assert-exact-arity!
  [module callee args n]
  (when-not (= n (count args))
    (fail-call-arity! "L2-BUILTIN-ARITY" module callee args n)))

(defn assert-between-arity!
  [module callee args min-n max-n]
  (when-not (<= min-n (count args) max-n)
    (fail-call-arity! "L2-BUILTIN-ARITY" module callee args
                      (str min-n " to " max-n))))

(defn assert-even-arity!
  [module callee args]
  (when (odd? (count args))
    (fail-call-arity! "L2-BUILTIN-ARITY" module callee args
                      "an even number of arguments")))
