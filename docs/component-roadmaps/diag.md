# Diagnostics

Working name: **Diagnostics** (proposed). Semantic key: `diag`.
Owner role: diagnostic infrastructure maintainer; current owner unassigned by
this proposal. Baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232`.

## Purpose and scope

Carry actionable source/manifest diagnostics consistently across compiler,
runtime, CLI and editor consumers. Own record identity, ordering, redaction,
rendering contracts and source-origin presentation. Stage owners retain rule
semantics. Existing diagnostic and wire identifiers are compatibility data,
not a reason to put planning IDs into new implementation names.

## Release outcome

Every claimed stage emits stable structured diagnostics that preserve source
and generated origins, profile/target and remediation; human and machine
renderers agree, and sensitive values are redacted. Outcome open.

## Interfaces and dependencies

Inputs: rule ID, severity, human message, structured facts, spans/origins and
artifact edges. Outputs: canonical records, ordered diagnostic streams,
redacted text/JSON and explainable links. Source spans/syntax origins are shared
interfaces. Each stage integrates independently; no dependency on a completed
Build or Release system is introduced.

## Remaining tasks and evidence

Implementation task authority: [Spec Kit ledger](../../specs/diag-workflow/tasks.md#implementation-tasks).
The following table is generated; edit the ledger and run
`clojure -M tools/diag_task_projection.clj --write`.

<!-- diag-task-projection:start -->
| Task (all open) | Evidence baseline | Closing evidence |
| --- | --- | --- |
| Reconcile stage carriers with canonical record schema | [Base carrier](../../bootstrap/clojure/src/gravity/diagnostics.clj), [records](../../bootstrap/clojure/src/gravity/c15_diagnostics/records.clj) | Record/schema parity fixtures from each producer, including malformed-record rejection Criterion compiler/c15/structured-diagnostic: Emit authoritative machine-readable diagnostic records with stable rule, severity/lifecycle, primary and related locations, generated-origin chain, stage, profile, target, involved artifacts, typed facts and structured remediation; actionable records include remediation. Preserve phase availability with explicit nil profile/target/primary syntax-id in early reader errors and unavailable input metadata in diagnostic-system errors. Generated diagnostics identify source producer unless package privacy redacts it; record redaction while retaining authorized audit data. Text/IDE/CI/localized renderings derive from this schema, never replace it. Reject malformed records, missing/invalid spans or origins, untyped facts, absent remediation and secret/private raw output leakage with C15-SCHEMA/SPAN/ORIGIN/FACTS/REMEDIATION/REDACTION; suppression retains the internal audit record. Emit versioned schema, ordered stream/catalog, related-span/remediation/redaction artifacts and accepted/rejected golden JSON fixtures from actual reader-through-lowering producers and public consumers. Bootstrap diagnostic criterion bootstrap/compiler-diagnostic-compatibility (Open): Every declared self-hosted compiler module emits the C15 structured diagnostic record and a diagnostic manifest naming its actual stable codes and source spans; seed diagnostic codes remain stable enough for conformance fixtures. Reject a missing or unstable seed code with BOOT2004 and a self-hosted diagnostic code gap with BOOT4006. Positive fixtures exercise actual seed and self-hosted diagnostic production against accepted code/span expectations, retaining the module/compiler/source identities, manifest and golden records. Negative fixtures exercise missing/unstable codes and lost or changed source spans. Produce the module diagnostic products consumed by the existing release stage-equivalence and TEST13 diagnostic-schema checks. Diagnostic drift without compatibility review fails BOOT3003; accepted deltas require the actual policy-linked GOV2 compatibility assessment and the release comparison report, rather than a producer-supplied success flag. Preserve C15 phase availability, generated origins, privacy/redaction and ordinary-development versus authoritative-migration distinctions; this criterion does not itself compare stage executables or admit migration. |
| Preserve primary/related/generated origins and stable order | [Projection](../../bootstrap/clojure/src/gravity/c15_diagnostics/projection.clj) | Macro and optimized-source diagnostics with original and generated locations Criterion compiler/c15/diagnostic-identity: Derive the resolved diagnostic identity from rule, primary artifact, producing stage and semantic facts, independently of display wording. For fixed source/profile/target/compiler/dependencies/grants preserve deterministic identity and stream order across text, JSON, IDE and CI consumers. Demonstrate wording-only stability and changed semantic subject separation; reject unstable or duplicate IDs with C15-ID and nondeterministic order with C15-ORDER. Retain actual span variants and generated origins. Early reader profile/target/primary syntax-id keys remain explicitly nil until produced; invent no module, coordinates or meta/JVM program context. A private construction record or unresolved digest request is not a resolved identity or safe publication. Emit resolved identity/catalog, ordering and consumer parity records plus positive and negative golden fixtures. Consume actual stage/source/origin facts and canonical diagnostic schema; do not infer compiler execution from a manifest. |
| Close text/JSON/redaction and explain contracts | [CLI presentation](../../bootstrap/clojure/src/gravity/cli/diagnostic_presentation.clj) | Golden text/JSON equivalence, secret redaction and known-ID explanation |
| Integrate runtime/build/editor consumers without rule drift | [Tooling contracts](../phase-13-tooling-and-developer-experience/README.md) | Same underlying diagnostic identity across public consumers and failure paths Foundation requirement gravity/runtime-check-diagnostic-stability: Specify and exercise runtime checks with deterministic owning code, source/manifest span, profile/target, failed predicate and remediation; compare their structured results across optimization and supported runtime providers. Refusals: Reject missing or changed check attribution, raw host failure leakage and a check erased without surviving evidence; fixtures must trigger the actual check, not match only free text. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection - Selected runtime/provider service contracts and actual link/load/service-use evidence; legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries - Dangerous-operation outcomes, actual checks, proof references and audited wrapper boundaries. |
<!-- diag-task-projection:end -->

The base carrier owns only construction/throwing of bootstrap ExceptionInfo;
it explicitly excludes rule catalogs, schema, identity, ordering and rendering.
It is not a completed diagnostics stack. Architecture decision owner:
diagnostic maintainer. Recommendation: one structured record schema with
separate renderers, retaining legacy IDs through explicit compatibility maps.

## Acceptance and integration

Yes/no: are identities stable? Are origin chains preserved? Do invalid records
fail validation? Are text and JSON semantically equal? Are secrets absent?
All must be yes. Integration milestones: Reader/Syntax errors, semantic errors,
then Runtime/Build/CLI parity. This page does not own source rule completion.
