(declare p15-s23-stage2-module-compile-call)
(defn p15-s23-stage2-seed-compile-expr
  [emitter module locals form]
  (cond
    (symbol? form)
    (cond
      (contains? locals form) {:op :local :name form}
      (contains? (:p15-s23-stage2-module-literals module) form)
      {:op :literal :value (get (:p15-s23-stage2-module-literals module) form)}
      :else
      (fail! "L2-UNKNOWN-SYMBOL"
             "stage2 plan emitter cannot resolve symbol"
             {:source-span {:source (:source-path module)}
              :symbol form
              :remediation "Define the symbol or stay within the stage2 hosted-core subset."}))

    (seq? form)
    (let [callee (first form)
          parent-path (:p15-s23-sh07-callsite-path module [])
          child-module (fn [index]
                         (assoc module :p15-s23-sh07-callsite-path
                                (conj parent-path index)))
          special-rule (get-in emitter [:special-form-rules callee])
          builtin-functions (set (get-in emitter [:call-rules :builtin-functions]))
          internal-arities (get-in emitter [:call-rules :compiler-internal-builtin-arities])]
      (cond
        (= callee 'println)
        {:op (:op special-rule)
         :effect (:effect special-rule)
         :capability (:capability special-rule)
         :args (mapv (fn [index argument]
                       (p15-s23-stage2-seed-compile-expr
                        emitter (child-module index) locals argument))
                     (range 1 (count form)) (rest form))}
        (= callee 'do)
        {:op (:op special-rule)
         :body (mapv (fn [index body-form]
                       (p15-s23-stage2-seed-compile-expr
                        emitter (child-module index) locals body-form))
                     (range 1 (count form)) (rest form))}
        (= callee 'if)
        (let [actual-arity (count (rest form))]
          (when-not (= 3 actual-arity)
            (fail! "L2-UNKNOWN-CORE-FORM"
                   "if requires exactly test, then, and else operands"
                   {:source-span {:source (:source-path module)}
                    :operator 'if
                    :expected-arity 3
                    :actual-arity actual-arity
                    :remediation
                    "Use exactly (if test then else) in the stage2 hosted-core subset."}))
          (let [[_ test then else] form]
          {:op (:op special-rule)
           :test (p15-s23-stage2-seed-compile-expr emitter (child-module 1) locals test)
           :then (p15-s23-stage2-seed-compile-expr emitter (child-module 2) locals then)
           :else (p15-s23-stage2-seed-compile-expr emitter (child-module 3) locals else)}))

        (= callee 'let)
        (let [[_ bindings & body] form]
          (p15-s23-stage2-seed-compile-let emitter module locals bindings body))
        (= callee 'loop)
        (let [[_ bindings & body] form]
          (p15-s23-stage2-seed-compile-loop emitter module locals bindings body))

        (= callee 'recur)
        {:op :recur
         :args (mapv (fn [index argument]
                       (p15-s23-stage2-seed-compile-expr
                        emitter (child-module index) locals argument))
                     (range 1 (count form)) (rest form))}

        (= callee 'throw)
        (do (p15-s23-stage2-seed-throw-arity! module form)
            {:op :throw :value (p15-s23-stage2-seed-compile-expr emitter (child-module 1) locals (second form))})

        (= callee 'quote)
        {:op (:op special-rule) :value (second form)}

        (= callee 'host-reflect)
        (fail! (get-in special-rule [:diagnostic] "P4-HOST-REFLECTION")
               "host reflection is not implemented in stage2 plan emission"
               {:source-span {:source (:source-path module)}
                :remediation "Remove host reflection from the stage2 executable subset."})

        (contains? internal-arities callee)
        (let [caller (:p15-s23-sh07-enclosing-function module)
              definition (:p15-s23-sh07-enclosing-definition module)
              expected-arity (get internal-arities callee)]
          (when-not (= expected-arity (count (rest form)))
            (fail! "L2-BUILTIN-ARITY"
                   "compiler-internal builtin call has the wrong arity"
                   {:source-span {:source (:source-path module)}
                    :function callee
                    :expected-arity expected-arity
                    :actual-arity (count (rest form))}))
          (when-not
           (p15-s23-sh07-compiler-internal-caller-authorized?
            module definition callee caller)
            (fail! "L2-UNKNOWN-CORE-FORM"
                   "compiler-internal builtin caller is not authorized"
                   {:source-span {:source (:source-path module)}
                    :function callee
                    :caller caller}))
          {:op (get-in emitter [:call-rules :builtin-op] :builtin-call)
           :function callee
           :compiler-internal-caller caller
           :compiler-internal-provenance
           (p15-s23-sh07-compiler-internal-provenance
            module definition callee caller)
           :args (mapv (fn [index argument]
                         (p15-s23-stage2-seed-compile-expr
                          emitter (child-module index) locals argument))
                       (range 1 (count form)) (rest form))})

        (contains? (:p15-s23-stage2-module-routes module)
                   [(:p15-s23-sh07-enclosing-function module) parent-path])
        (p15-s23-stage2-module-compile-call emitter module locals form child-module)

        (contains? builtin-functions callee)
        {:op (get-in emitter [:call-rules :builtin-op] :builtin-call)
         :function callee
         :args (mapv (fn [index argument]
                       (p15-s23-stage2-seed-compile-expr
                        emitter (child-module index) locals argument))
                     (range 1 (count form)) (rest form))}

        (contains? (:function-table module) callee)
        {:op (get-in emitter [:call-rules :function-op] :function-call)
         :function callee
         :args (mapv (fn [index argument]
                       (p15-s23-stage2-seed-compile-expr
                        emitter (child-module index) locals argument))
                     (range 1 (count form)) (rest form))}

        (contains? (set (map :symbol
                             (get-in module
                                     [:p15-s23-sh07-module-source-descriptor
                                      :external-bindings])))
                   callee)
        (let [external-link
              (p15-s23-sh07-b51-external-instruction
               module (:p15-s23-sh07-enclosing-function module) callee
               (count (rest form)) parent-path)]
          {:op (get-in emitter [:call-rules :function-op] :function-call)
           :function callee
           :args (mapv (fn [index argument]
                         (p15-s23-stage2-seed-compile-expr
                          emitter (child-module index) locals argument))
                       (range 1 (count form)) (rest form))
           :external-link external-link})

        :else
        (fail! (get-in emitter
                       [:call-rules :unknown-callee-diagnostic]
                       "L2-UNKNOWN-CORE-FORM")
               "stage2 plan emitter cannot compile this form"
               {:source-span {:source (:source-path module)}
                :operator callee
                :remediation "Use defn, println, do, if, let, loop, recur, quote, supported core builtins, or local function calls in the stage2 hosted-core subset."})))

    (or (vector? form) (map? form) (set? form))
    (p15-s23-stage2-seed-compile-collection emitter module locals form)

    :else
    {:op (get-in emitter [:collection-rules :literal :op] :literal)
     :value form}))

(defn p15-s23-stage2-seed-compile-function
  [emitter module {:keys [name params body] :as definition}]
  (let [body (vec body)
        last-index (dec (count body))
        target-arity (count params)]
    (doseq [[index form] (map-indexed vector body)]
      (p15-s23-stage2-seed-validate-recur!
       (if (contains? module :p15-s23-stage2-map-order) (assoc module :p15-s23-sh07-enclosing-function name :p15-s23-sh07-callsite-path [index]) module) form target-arity (= index last-index)))
    (assoc definition
           :binding {:name name
                     :kind :function
                     :namespace (:module module)
                     :profile (:profile module)
                     :target (:target module)
                     :visibility (if (seq (:exports module))
                                   (if (contains? (set (:exports module)) name)
                                     :public
                                     :private)
                                   :stage2-local)
                     :effects (:effects module)
                     :capabilities (:capabilities module)}
           :instructions
           (mapv (fn [body-index body-form]
                   (p15-s23-stage2-seed-compile-expr
                    emitter
                    (assoc module
                           :p15-s23-sh07-enclosing-function name
                           :p15-s23-sh07-enclosing-definition definition
                           :p15-s23-sh07-callsite-path [body-index])
                    (set params) body-form))
                 (range) body))))

(def p15-s23-stage2-compiler-artifact-source-relative-path
  "bootstrap/gravity/p15_s23/emitter.gravity")
(def p15-s23-stage2-compiler-artifact-function
  'p15-s23-compile-function)
(def p15-s23-stage2-compiler-artifact-expression-function
  'p15-s23-compile-expr)
(def p15-s23-stage2-compiler-artifact-collection-function
  'p15-s23-compile-collection)
(def p15-s23-stage2-compiler-artifact-let-function
  'p15-s23-compile-let)
(def p15-s23-stage2-compiler-artifact-plan-assembly-function
  'p15-s23-assemble-plan-products)

(def p15-s23-stage2-compiler-artifact-builtins
  '#{symbol? keyword? char? number? list? seq? vector? map? set? string?
     contains? even? set sort-by-pr-str vec quot subvec integer? boolean? keys hosted-native-data-preflight bootstrap-symbol-utf16-unit-count bootstrap-native-carrier-header bootstrap-native-identical?
     sh07-canonical-text-compare sh07-declared-digest-hash
     sh07-observe-utf8-byte-count
     sh07-b51-admitted-executable-carrier-validation?})

(def p15-s23-stage2-compiler-artifact-required-functions
  {'p15-s23-compile-collection
   {:arity 4 :params ['emitter 'module 'locals 'form]}
   'p15-s23-compile-let
   {:arity 5 :params ['emitter 'module 'locals 'bindings 'body]}
   'p15-s23-compile-expr
   {:arity 4 :params ['emitter 'module 'locals 'form]}
   'p15-s23-compile-function
   {:arity 3 :params ['emitter 'module 'definition]}
   'p15-s23-summary-instructions
   {:arity 2 :params ['instructions 'summary]}
   'p15-s23-summary-map-entries
   {:arity 2 :params ['entries 'summary]}
   'p15-s23-summary-let-bindings
   {:arity 2 :params ['bindings 'summary]}
   'p15-s23-summary-instruction
   {:arity 2 :params ['instruction 'summary]}
   'p15-s23-assemble-functions
   {:arity 6 :params ['emitter 'module 'definitions 'functions
                      'bindings 'summary]}
   'p15-s23-assemble-plan-products
   {:arity 3 :params ['emitter 'module 'definitions]}})

;; Filled with the canonical semantic function-plan hash after the source is
;; compiled below.  This is intentionally pinned rather than merely recorded:
;; a changed compiler function cannot silently become production lowering.
(def p15-s23-stage2-compiler-artifact-expected-semantic-hash
  "sha256:2004f388034a182483d7b9c28f4e6d6baa76126d29867c50183b0329fa5e0ddc")

(def p15-s23-stage2-compiler-artifact-expected-source-content-hash
  "sha256:e273535bc06e5f67eb5a54ddcba0ef726814bc9dbe484df73bf7d3ab56d1e949")
