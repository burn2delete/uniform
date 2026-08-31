

(def p15-s23-stage2-plan-emitter-required-preserves
  #{:source-spans :diagnostic-codes :function-bindings
    :instruction-semantics :effects :capabilities :profile
    :compiler-lineage :artifact-provenance})

(def p15-s23-stage2-plan-emitter-required-emits
  #{:stage2-instruction-plan :stage2-plan-execution-record
    :accepted-output-comparison :rejected-diagnostic-comparison
    :stage2-emitter-boundary-record})

(def p15-s23-stage2-plan-emitter-required-special-forms
  '#{println do if let quote host-reflect})

(def p15-s23-stage2-plan-emitter-diagnostic-messages
  {"P15S23Q001" "P15-S23 stage2 plan emitter contract is missing"
   "P15S23Q002" "P15-S23 stage2 plan emitter rule set is incomplete"
   "P15S23Q003" "P15-S23 stage2 plan emitter accepted plan or output is not equivalent"
   "P15S23Q004" "P15-S23 stage2 plan emitter rejected diagnostics are not preserved"
   "P15S23Q005" "P15-S23 stage2 plan emitter evidence links are incomplete"
   "P15S23Q006" "P15-S23 stage2 plan emitter preservation or emission contract is incomplete"
   "P15S23Q007" "P15-S23 stage2 plan emitter residual boundary record is incomplete"
   "P15S23Q008" "P15-S23 stage2 plan emitter makes an unsupported self-hosting or seed-retirement claim"})

(def p15-s23-stage2-plan-emitter-diagnostic-ids
  ["P15S23Q001" "P15S23Q002" "P15S23Q003" "P15S23Q004"
   "P15S23Q005" "P15S23Q006" "P15S23Q007" "P15S23Q008"])

(defn p15-s23-stage2-plan-emitter-fail!
  [id source-path value data]
  (fail! id
         (get p15-s23-stage2-plan-emitter-diagnostic-messages
              id
              "P15-S23 stage2 plan emitter proof failed")
         (merge {:source-span {:source source-path}
                 :stage :p15-s23-stage2-plan-emitter
                 :diagnostic-family :p15-s23-stage2-plan-emitter
                 :value value
                 :remediation "Keep the stage2 plan emitter rules authored in Gravity source, execute them through the declared Clojure rule-runner boundary, prove accepted output and rejected diagnostics against the current stage, and keep full self-hosting claims false."}
                data)))

(defn p15-s23-stage2-plan-emitter-diagnostic-record
  [source-path id value data]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p15-s23-stage2-plan-emitter
   :source-span {:source source-path}
   :message (get p15-s23-stage2-plan-emitter-diagnostic-messages id)
   :facts data
   :observed value
   :remediation :repair_stage2_plan_emitter})

(defn p15-s23-stage2-plan-emitter-rule-record
  [emitter]
  (let [special-rules (set (keys (:special-form-rules emitter)))
        builtin-functions (set (get-in emitter
                                       [:call-rules :builtin-functions]))
        missing-special
        (set/difference p15-s23-stage2-plan-emitter-required-special-forms
                        special-rules)
        missing-builtins
        (set/difference stage0-builtin-functions builtin-functions)
        plan-kind (get-in emitter [:plan-shape :kind])
        compiler-owner (get-in emitter [:plan-shape :compiler :owner])]
    {:artifact :gravity/p15-s23-stage2-plan-emitter-rule-record
     :engine (:engine emitter)
     :special-forms (p15-s23-stage2-sort-values special-rules)
     :builtin-functions (p15-s23-stage2-sort-values builtin-functions)
     :missing-special-forms (p15-s23-stage2-sort-values missing-special)
     :missing-builtin-functions (p15-s23-stage2-sort-values
                                 missing-builtins)
     :plan-kind plan-kind
     :compiler-owner compiler-owner
     :rule-set-complete?
     (and (= :gravity-stage2-plan-emitter-rules-v1 (:engine emitter))
          (empty? missing-special)
          (empty? missing-builtins)
          (= :gravity/stage2-hosted-core-compiled-plan plan-kind)
          (= :gravity-source compiler-owner))
     :status (if (and (= :gravity-stage2-plan-emitter-rules-v1
                         (:engine emitter))
                      (empty? missing-special)
                      (empty? missing-builtins)
                      (= :gravity/stage2-hosted-core-compiled-plan
                         plan-kind)
                      (= :gravity-source compiler-owner))
               :complete
               :failed)}))

(declare p15-s23-stage2-seed-compile-expr)

(declare p15-s23-stage2-seed-validate-recur!)

(defn p15-s23-stage2-seed-recur-fail!
  [module form target-arity actual-arity reason]
  (fail! "L2-RECUR-TARGET"
         "recur has no compatible loop or function target"
         {:source-span {:source (:source-path module)}
          :form form
          :target-arity target-arity
          :actual-arity actual-arity
          :reason reason
          :remediation
          "Use recur only in tail position inside a compatible loop or function with matching arity."}))

(defn p15-s23-stage2-seed-validate-tail-body!
  [module forms target-arity tail-position?]
  (let [forms (vec forms)
        last-index (dec (count forms))]
    (doseq [[index form] (map-indexed vector forms)]
      (p15-s23-stage2-seed-validate-recur!
       module form target-arity
       (and tail-position? (= index last-index)))))
  :passed)

(defn p15-s23-stage2-seed-validate-binding-expressions!
  [module bindings target-arity diagnostic]
  (when-not (and (vector? bindings) (even? (count bindings)))
    (if (= diagnostic "L2-LET-BINDING")
      (fail! diagnostic
             "let requires an even binding vector"
             {:source-span {:source (:source-path module)}
              :bindings bindings
              :remediation
              "Use pairs of local names and expressions in let."})
      (p15-s23-stage2-seed-recur-fail!
       module (list 'loop bindings) nil nil :invalid-loop-bindings)))
  (doseq [[name expr] (partition 2 bindings)]
    (when-not (symbol? name)
      (if (= diagnostic "L2-LET-BINDING")
        (fail! diagnostic
               "let binding name must be a symbol"
               {:source-span {:source (:source-path module)}
                :binding name
                :remediation "Bind symbols in stage2 let forms."})
        (p15-s23-stage2-seed-recur-fail!
         module (list 'loop bindings) nil nil :invalid-loop-binding-name)))
    (p15-s23-stage2-seed-validate-recur!
     module expr target-arity false))
  :passed)