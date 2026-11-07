(ns gravity.resolution.stage0-functions
  "Native Stage0 declaration and header/table compatibility; no source or C5 admission authority.")

(def ^:private namespace-contract
  '{:public-api {stage0-function-table {:authenticates-inputs? false, :admission :none, :operation-inputs [:function-definition-form :validate-seed-function-names! :normalize-seed-function-header :validate-function-definition!], :input-boundary :caller-owned-native-expanded-form-and-function-data, :grants-authority? false, :callback-selection :late-owner-var-before-call-arguments, :arglists ([operations module]), :visibility :public, :proves-legality? false, :contract-boundary :stage0-native-function-declaration-table}, validate-function-definition! {:authenticates-inputs? false, :admission :none, :input-boundary :caller-owned-native-expanded-form-and-function-data, :operation-vars [fail-var], :grants-authority? false, :callback-selection :late-owner-var-before-call-arguments, :arglists ([fail-var source-path {:keys [name params body]}]), :visibility :public, :proves-legality? false, :contract-boundary :stage0-native-function-declaration-table}, function-definition-form {:authenticates-inputs? false, :admission :none, :input-boundary :caller-owned-native-expanded-form-and-function-data, :operation-vars [defn-form?-var def-function-form?-var], :grants-authority? false, :callback-selection :late-owner-var-before-call-arguments, :arglists ([defn-form?-var def-function-form?-var source-path form]), :visibility :public, :proves-legality? false, :contract-boundary :stage0-native-function-declaration-table}}, :source-reading? false, :macro-execution? false, :proof-authority? false, :tests [gravity.resolution.stage0-functions-test], :dependency-direction {:requires [clojure.core], :consumed-by [gravity.bootstrap], :forbids [gravity.bootstrap gravity.resolution gravity.reader gravity.syntax gravity.diagnostics gravity.checked-core gravity.types gravity.package gravity.build gravity.cli gravity.release]}, :canonical-c5-admission? false, :internal-api {seed-declared-header? {:authenticates-inputs? false, :admission :none, :input-boundary :caller-owned-native-expanded-form-and-function-data, :operation-vars [], :grants-authority? false, :callback-selection :none, :arglists ([{:keys [params body]}]), :visibility :private-native-bridge, :proves-legality? false, :contract-boundary :stage0-native-function-declaration-table}, normalize-seed-function-header {:authenticates-inputs? false, :admission :none, :input-boundary :caller-owned-native-expanded-form-and-function-data, :operation-vars [fail-var declared-header?-var], :grants-authority? false, :callback-selection :late-owner-var-before-call-arguments, :arglists ([fail-var declared-header?-var source-path {:keys [name params body], :as definition}]), :visibility :private-native-bridge, :proves-legality? false, :contract-boundary :stage0-native-function-declaration-table}, validate-seed-function-names! {:authenticates-inputs? false, :admission :none, :input-boundary :caller-owned-native-expanded-form-and-function-data, :operation-vars [fail-var], :grants-authority? false, :callback-selection :late-owner-var-before-call-arguments, :arglists ([fail-var source-path definitions]), :visibility :private-native-bridge, :proves-legality? false, :contract-boundary :stage0-native-function-declaration-table}, validate-seed-source-headers! {:authenticates-inputs? false, :admission :none, :operation-inputs [:function-definition-form :seed-declared-header? :validate-seed-function-names! :validate-function-definition! :normalize-seed-function-header], :input-boundary :caller-owned-native-expanded-form-and-function-data, :grants-authority? false, :callback-selection :late-owner-var-before-call-arguments, :arglists ([operations source-path forms]), :visibility :private-native-bridge, :proves-legality? false, :contract-boundary :stage0-native-function-declaration-table}}, :governing-contracts [D0 D1 D2 D3 D6 D8 D9 L1 L2 L3 C1 C5 C6 C15 BOOT8 TEST2], :unsupported [:source-or-plan-authentication :canonical-binding-identities :core-or-typed-artifacts :complete-source-map-and-origins :profile-effect-capability-ownership-safety-admission :backend-execution :release :migration :self-hosting :seed-retirement], :artifact-inputs [:ordered-native-expanded-forms :source-path :module-data :late-owner-operation-vars], :canonical-core-or-type-authority? false, :namespace gravity.resolution.stage0-functions, :diagnostic-ownership {:stage [L2-FUNCTION-PARAMS L2-DUPLICATE-FUNCTION], :diagnostic-constructor gravity.diagnostics/diagnostic, :throw-transport :bootstrap-fail!}, :artifact-outputs [:raw-function-definition :normalized-function-table :source-declarations :nil-or-owner-return-or-unchanged-failure], :contract-boundary :stage0-native-function-declaration-table})

#_{:clj-kondo/ignore [:unused-binding]}
(defn function-definition-form
  [defn-form?-var def-function-form?-var source-path form]
  (cond
    ((deref defn-form?-var) form)
    (let [[_ name params & body] form]
      {:name name :params params :body body :definition-form :defn})

    ((deref def-function-form?-var) form)
    (let [[_ params & body] (nth form 2)]
      {:name (second form) :params params :body body :definition-form :def-fn})

    :else nil))

#_{:clj-kondo/ignore [:unused-private-var]}
(defn- seed-declared-header? [{:keys [params body]}]
  (or (and (vector? params) (some #(= :- %) params)) (= :- (first body))
      (and (seq? (first body)) (= 'typed/return (ffirst body)))))

#_{:clj-kondo/ignore [:unused-private-var]}
(defn- normalize-seed-function-header
  [fail-var declared-header?-var source-path {:keys [name params body] :as definition}]
  (if (and (symbol? name) (vector? params)
           ((deref declared-header?-var) definition))
    (letfn [(reject! [reason]
              ((deref fail-var) "L2-FUNCTION-PARAMS" "stage0 function declaration header is malformed"
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
  [fail-var source-path {:keys [name params body]}]
  (when-not (symbol? name)
    ((deref fail-var) "L2-FUNCTION-PARAMS"
           "stage0 function name must be a symbol"
           {:source-span {:source source-path}
            :function name
            :remediation "Define functions with symbolic names."}))
  (when-not (and (vector? params) (every? symbol? params))
    ((deref fail-var) "L2-FUNCTION-PARAMS"
           "stage0 function parameters must be a vector of symbols"
           {:source-span {:source source-path}
            :function name
            :params params
            :remediation "Use a fixed arity parameter vector such as [x y]."}))
  (when (or (some #{'&} params) (not= (count params) (count (distinct params))))
    ((deref fail-var) "L2-FUNCTION-PARAMS" "stage0 function parameters require distinct fixed arity names"
           {:source-span {:source source-path} :function name :params params
            :reason (if (some #{'&} params) :variadic-parameters-unsupported :duplicate-parameter-names)
            :remediation "Use distinct logical parameter names without a variadic marker."}))
  (when-not (seq body)
    ((deref fail-var) "L2-FUNCTION-PARAMS"
           "stage0 function body cannot be empty"
           {:source-span {:source source-path}
            :function name
            :remediation "Add at least one body form."})))

#_{:clj-kondo/ignore [:unused-private-var]}
(defn- validate-seed-function-names! [fail-var source-path definitions]
  (let [duplicate (first (for [[name n] (frequencies (map :name definitions)) :when (> n 1)] name))]
    (when duplicate
      ((deref fail-var) "L2-DUPLICATE-FUNCTION"
             "stage0 executable has duplicate function definitions"
             {:source-span {:source source-path}
              :function duplicate
              :remediation "Keep one definition per stage0 function name."}))))

(defn stage0-function-table
  [operations module]
  (let [source-path (:source-path module)
        definitions (keep #((deref (:function-definition-form operations)) source-path %) (:forms module))]
    ((deref (:validate-seed-function-names! operations)) source-path definitions)
    (into {}
          (map (fn [raw-definition]
                 (let [definition ((deref (:normalize-seed-function-header operations)) source-path raw-definition)]
                 ((deref (:validate-function-definition! operations)) source-path definition)
                 [(:name definition)
                  (assoc definition
                         :arity (count (:params definition))
                         :body-form-count (count (:body definition)))])))
          definitions)))

#_{:clj-kondo/ignore [:unused-private-var]}
(defn- validate-seed-source-headers! [operations source-path forms]
  (let [definitions (keep #((deref (:function-definition-form operations)) source-path %) forms)]
    (when (some (deref (:seed-declared-header? operations)) definitions)
      ((deref (:validate-seed-function-names! operations)) source-path definitions)
      (doseq [definition definitions :when ((deref (:seed-declared-header? operations)) definition)]
        ((deref (:validate-function-definition! operations)) source-path
          ((deref (:normalize-seed-function-header operations)) source-path definition))))))

(doseq [name (concat (keys (:public-api namespace-contract)) (keys (:internal-api namespace-contract)))
        :let [operation (get (ns-interns *ns*) name)]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
