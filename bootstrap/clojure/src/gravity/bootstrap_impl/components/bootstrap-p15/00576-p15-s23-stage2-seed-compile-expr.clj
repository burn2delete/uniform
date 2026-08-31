

(defn p15-s23-stage2-seed-compile-expr
  [emitter module locals form]
  (cond
    (symbol? form)
    (if (contains? locals form)
      {:op :local :name form}
      (fail! "L2-UNKNOWN-SYMBOL"
             "stage2 plan emitter cannot resolve symbol"
             {:source-span {:source (:source-path module)}
              :symbol form
              :remediation "Define the symbol or stay within the stage2 hosted-core subset."}))

    (seq? form)
    (let [callee (first form)
          special-rule (get-in emitter [:special-form-rules callee])
          builtin-functions (set (get-in emitter
                                         [:call-rules :builtin-functions]))]
      (cond
        (= callee 'println)
        {:op (:op special-rule)
         :effect (:effect special-rule)
         :capability (:capability special-rule)
         :args (mapv #(p15-s23-stage2-seed-compile-expr
                       emitter module locals %)
                     (rest form))}

        (= callee 'do)
        {:op (:op special-rule)
         :body (mapv #(p15-s23-stage2-seed-compile-expr
                       emitter module locals %)
                     (rest form))}

        (= callee 'if)
        (let [[_ test then else] form]
          {:op (:op special-rule)
           :test (p15-s23-stage2-seed-compile-expr emitter module locals test)
           :then (p15-s23-stage2-seed-compile-expr emitter module locals then)
           :else (p15-s23-stage2-seed-compile-expr emitter module locals else)})

        (= callee 'let)
        (let [[_ bindings & body] form]
          (p15-s23-stage2-seed-compile-let emitter module locals bindings body))

        (= callee 'loop)
        (let [[_ bindings & body] form]
          (p15-s23-stage2-seed-compile-loop emitter module locals bindings body))

        (= callee 'recur)
        {:op :recur
         :args (mapv #(p15-s23-stage2-seed-compile-expr
                       emitter module locals %)
                     (rest form))}

        (= callee 'quote)
        {:op (:op special-rule) :value (second form)}

        (= callee 'host-reflect)
        (fail! (get-in special-rule [:diagnostic] "P4-HOST-REFLECTION")
               "host reflection is not implemented in stage2 plan emission"
               {:source-span {:source (:source-path module)}
                :remediation "Remove host reflection from the stage2 executable subset."})

        (contains? builtin-functions callee)
        {:op (get-in emitter [:call-rules :builtin-op] :builtin-call)
         :function callee
         :args (mapv #(p15-s23-stage2-seed-compile-expr
                       emitter module locals %)
                     (rest form))}

        (contains? (:function-table module) callee)
        {:op (get-in emitter [:call-rules :function-op] :function-call)
         :function callee
         :args (mapv #(p15-s23-stage2-seed-compile-expr
                       emitter module locals %)
                     (rest form))}

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
       module form target-arity (= index last-index)))
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
           :instructions (mapv #(p15-s23-stage2-seed-compile-expr
                                 emitter module (set params) %)
                               body))))

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
  '#{symbol? keyword? char? number? list? seq? vector? map? set? string? contains? even? set sort-by-pr-str
     vec quot subvec integer? boolean? keys})

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
  "sha256:5743c5415a9d35c13fc45baf85323c943f9dc6d156eb194de63b1bbdacc75446")

(def p15-s23-stage2-compiler-artifact-expected-source-content-hash
  "sha256:3543311198f6bfe43fc2c9d33b9ccddf575789e286df4576cf31fb77836130e7")