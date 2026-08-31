; Semantic decomposition of committed HEAD reader line 150856.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/let
 [semantic-late-sh05-macro-artifact-verification*-carrier
  semantic-late-sh05-macro-artifact-verification*-carrier
  semantic-late-sh05-macro-artifact-verification*-expansion-replay
  semantic-late-sh05-macro-artifact-verification*-expansion-replay
  semantic-late-sh05-macro-artifact-verification*-provenance
  semantic-late-sh05-macro-artifact-verification*-provenance
  semantic-late-sh05-macro-artifact-verification*-proof
  semantic-late-sh05-macro-artifact-verification*-proof]
 (defn
  sh05-macro-artifact-verification*
  [artifact allow-missing-proof?]
  (clojure.core/let
   [state-0
    {:artifact artifact, :allow-missing-proof? allow-missing-proof?}
    state-1
    (semantic-late-sh05-macro-artifact-verification*-carrier state-0)
    state-2
    (semantic-late-sh05-macro-artifact-verification*-expansion-replay state-1)
    state-3
    (semantic-late-sh05-macro-artifact-verification*-provenance state-2)
    state-4
    (semantic-late-sh05-macro-artifact-verification*-proof state-3)]
   (clojure.core/let
    [{:keys
      [artifact
       allow-missing-proof?
       source-path
       boundary
       c3-artifact
       c3-boundary
       gravity-verifiers
       output-envelope-ok?
       binding
       embedded-descriptor
       expected-descriptor
       descriptor-current?
       sh03-current?
       sh04-current?
       template-passed?
       resolved-passed?
       runs
       input-forms
       input-syntax
       input-module
       expected-expanded-stream
       expected-trace
       first-run
       first-run-shortcuts-current?
       run-count-current?
       run-storage-exact?
       plan-binding-current?
       pinned-version?
       grants-current?
       trace-current?
       output-current?
       generated-origins-current?
       input-lineage-current?
       c3-syntax-by-id
       expected-reader-binding
       expected-reader-revision
       expected-definition-span
       run-provenance-current?
       provenance-current?
       graph-valid?
       graph-current?
       exact-shape?
       artifact-id-current?
       base-checks
       base-failed-checks
       expected-embedded-proof
       embedded-proof-current?
       checks
       failed-checks]}
     state-4]
    {:artifact :gravity/sh05-macro-artifact-verification,
     :status (if (empty? failed-checks) :passed :failed),
     :checks checks,
     :failed-checks failed-checks,
     :gravity-verifiers gravity-verifiers}))))
