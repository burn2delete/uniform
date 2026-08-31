; Semantic decomposition of HEAD reader line 12567.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 typed-source-artifact
 [source-path source-text]
 (let
  [core-artifact
   (core-source-artifact source-path source-text)
   module
   (:module core-artifact)
   checker
   (new-typed-checker)
   ctx
   (typed-context module)
   typed-roots
   (mapv
    (fn* [p1__205#] (check-typed-node checker ctx p1__205#))
    (:expanded-core-ast core-artifact))
   checked
   @checker
   type-coverage
   (distinct-records (:type-category-coverage checked))
   type-facts
   (:type-facts checked)
   effect-facts
   (:effect-facts checked)
   build-log
   (distinct-records (:build-effect-log checked))
   handled-table
   (distinct-records (:handled-effect-table checked))
   mir-records
   (mir-type-preservation-records type-facts)
   module-effects
   (set/union
    (collect-fact-effects typed-roots)
    (set (mapcat :effects effect-facts)))
   module-capabilities
   (set/union
    (collect-fact-capabilities typed-roots)
    (set (mapcat :capabilities effect-facts)))
   handled-labels
   (set (map :effect-label handled-table))
   escaping-effects
   (set/difference module-effects handled-labels)
   mir-effect-records
   (vec (mir-effect-annotation-records effect-facts))
   source-forms
   (mapv :form (rest (:expanded-syntax-object-stream core-artifact)))
   protocol-table
   (protocol-table-from-forms source-forms)
   method-signatures
   (method-signature-records protocol-table)
   implementation-table
   (implementation-table-from-forms source-forms protocol-table)
   dispatch-records
   (distinct-records (:dispatch-mode-records checked))
   multimethods
   (multimethod-tables-from-forms source-forms)
   host-dispatch-records
   (distinct-records (:host-interop-dispatch-records checked))
   interface-artifacts
   (interface-lowering-artifacts
    implementation-table
    dispatch-records)]
  (let
   [state
    {:implementation-table implementation-table,
     :interface-artifacts interface-artifacts,
     :core-artifact core-artifact,
     :typed-roots typed-roots,
     :type-facts type-facts,
     :handled-table handled-table,
     :module module,
     :source-path source-path,
     :checker checker,
     :source-forms source-forms,
     :escaping-effects escaping-effects,
     :module-effects module-effects,
     :multimethods multimethods,
     :dispatch-records dispatch-records,
     :effect-facts effect-facts,
     :checked checked,
     :type-coverage type-coverage,
     :module-capabilities module-capabilities,
     :host-dispatch-records host-dispatch-records,
     :build-log build-log,
     :method-signatures method-signatures,
     :ctx ctx,
     :mir-effect-records mir-effect-records,
     :handled-labels handled-labels,
     :protocol-table protocol-table,
     :mir-records mir-records}
    artifact-00
    {}
    artifact-01
    (semantic-early-typed-artifact-phase-01 artifact-00 state)
    artifact-02
    (semantic-early-typed-artifact-phase-02 artifact-01 state)
    artifact-03
    (semantic-early-typed-artifact-phase-03 artifact-02 state)
    artifact-04
    (semantic-early-typed-artifact-phase-04 artifact-03 state)
    artifact-05
    (semantic-early-typed-artifact-phase-05 artifact-04 state)
    artifact-06
    (semantic-early-typed-artifact-phase-06 artifact-05 state)
    artifact-07
    (semantic-early-typed-artifact-phase-07 artifact-06 state)
    artifact-08
    (semantic-early-typed-artifact-phase-08 artifact-07 state)
    artifact-09
    (semantic-early-typed-artifact-phase-09 artifact-08 state)
    artifact-10
    (semantic-early-typed-artifact-phase-10 artifact-09 state)
    artifact-11
    (semantic-early-typed-artifact-phase-11 artifact-10 state)
    artifact-12
    (semantic-early-typed-artifact-phase-12 artifact-11 state)
    artifact-13
    (semantic-early-typed-artifact-phase-13 artifact-12 state)
    artifact-14
    (semantic-early-typed-artifact-phase-14 artifact-13 state)
    artifact-15
    (semantic-early-typed-artifact-phase-15 artifact-14 state)
    artifact-16
    (semantic-early-typed-artifact-phase-16 artifact-15 state)
    artifact-17
    (semantic-early-typed-artifact-phase-17 artifact-16 state)
    artifact-18
    (semantic-early-typed-artifact-phase-18 artifact-17 state)]
   artifact-18)))

(clojure.core/doseq
 [symbol__1382__auto__
  '[semantic-early-typed-pass-emits-01
    semantic-early-typed-pass-emits-02
    semantic-early-typed-pass-emits-03
    semantic-early-typed-pass-emits-04
    semantic-early-typed-pass-emits-05
    semantic-early-typed-pass-emits-06
    semantic-early-typed-pass-rejects-01
    semantic-early-typed-pass-rejects-02
    semantic-early-typed-pass-rejects-03
    semantic-early-typed-pass-rejects-04
    semantic-early-typed-pass-rejects-05
    semantic-early-typed-pass-rejects-06
    semantic-early-typed-artifact-pass
    semantic-early-typed-artifact-phase-01
    semantic-early-typed-artifact-phase-02
    semantic-early-typed-artifact-phase-03
    semantic-early-typed-artifact-phase-04
    semantic-early-typed-artifact-phase-05
    semantic-early-typed-artifact-phase-06
    semantic-early-typed-artifact-phase-07
    semantic-early-typed-artifact-phase-08
    semantic-early-typed-artifact-phase-09
    semantic-early-typed-artifact-phase-10
    semantic-early-typed-artifact-phase-11
    semantic-early-typed-artifact-phase-12
    semantic-early-typed-artifact-phase-13
    semantic-early-typed-artifact-phase-14
    semantic-early-typed-artifact-phase-15
    semantic-early-typed-artifact-phase-16
    semantic-early-typed-artifact-phase-17
    semantic-early-typed-artifact-phase-18]]
 (clojure.core/ns-unmap clojure.core/*ns* symbol__1382__auto__))
