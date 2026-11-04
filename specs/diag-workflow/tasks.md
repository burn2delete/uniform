# Tasks: Diagnostics implementation authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)

## Implementation tasks

This marked region is the sole editable implementation task ledger. All four
outcomes remain open. Evidence baselines are references, not acceptance. The
linked component roadmap is navigation to this ledger. Historical migration
checkboxes below are separate from implementation completion.

<!-- diag-implementation-tasks:start -->

- [ ] `diag/remaining-tasks-and-evidence/reconcile-stage-carriers-with-canonical-record-schema` Reconcile stage carriers with canonical record schema

Status: Open

Evidence baseline: [Base carrier](../../bootstrap/clojure/src/gravity/diagnostics.clj), [records](../../bootstrap/clojure/src/gravity/c15_diagnostics/records.clj)

Closing evidence: Record/schema parity fixtures from each producer, including malformed-record rejection

<!-- diag-canonical-records:start -->
Apply the [complete diagnostic source criteria](plan.md#complete-diagnostic-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

Foundation diagnostic criterion gravity/foundation-diagnostic-context (Open): For an actual rejected D0-D4 or D7 contract, preserve the stable owning rule, failed thesis/layer/profile boundary and downstream document that owns the detailed rule. Include applicable source span or manifest entry, active profile/target, type/effect facts, missing effect/capability, unsafe audit identity when relevant, producing/consuming pass and remediation without inventing phase-unavailable values. Doc tooling, compiler manifest checks and governance consumers use the same diagnostic/artifact model; roadmap policy diagnostics block the unsupported release claim and type/effect errors retain stable IDs. Preserve D1-ARTIFACT-GAP for reported missing provenance edges, D4-INTEROP-UNTRACKED for a reported external-boundary record gap, and D7-MACRO-PROVENANCE and D7-PASS-CONTRACT for the reported origin/IR-fact failures. Required outputs: owning-checker rejection plus its structured contextual diagnostic and source/compiler/profile/target/manifest links. Exercise correctly attributed positive records and reject missing/stale rule, lost required context, raw host prose or substituted producer/pass identity. This record producer consumes the actual owning checker's failure and does not implement its legality rule, architecture decision, release admission or other foundation scope.

C15 record/stream refinement (Open; existing compiler/c15/structured-diagnostic): Validate actual produced records and streams against the canonical C15 schema, including artifact/diagnostic/rule/message-key, severity/lifecycle, primary and related location roles, origins, phase-available profile/target/syntax identity, typed facts, remediation and redactions. Support error, warning, info, hint and internal-error severity under their C15 emission rules; error prevents compilation/artifact emission and an internal-error retains compiler-invariant bug-report context. Preserve active, suppressed-by-policy, redacted, resolved-by-fix and stale-after-edit lifecycle states. Preserve the source/generated/macro/manifest/package/MIR/domain/backend/runtime location variants, selecting the most actionable source span as primary and downstream/generated context as related. Typed fact fields carry the applicable type/effect/capability/profile/ownership/safety/proof/target/provider/artifact facts, not prose substitutes. Stream records bind stage, input artifact, output artifact if any, diagnostics, actual summary counts by severity, deterministic ordering key, redaction policy and rendering version; attach them to pipeline and target manifests. Summary counts describe actual stream contents and impose no work allowance. Required outputs: schema/catalog/stream and manifest links, related-span/remediation/redaction records and positive/negative producer fixtures. Refuse malformed or missing required fields, invalid phase availability, lost location/origin/fact/remediation data, inconsistent severity summaries or ordering and missing stream-manifest links with the applicable C15-SCHEMA/SPAN/ORIGIN/FACTS/REMEDIATION/ORDER rules. Diagnostics about diagnostics retain producing stage, offending diagnostic identity, schema field, artifact and remediation. Consume actual stage/source/origin/fact products; a supplied schema or success flag cannot prove producer execution.

Criterion compiler/c15/structured-diagnostic: Emit authoritative machine-readable diagnostic records with stable rule, severity/lifecycle, primary and related locations, generated-origin chain, stage, profile, target, involved artifacts, typed facts and structured remediation; actionable records include remediation. Preserve phase availability with explicit nil profile/target/primary syntax-id in early reader errors and unavailable input metadata in diagnostic-system errors. Generated diagnostics identify source producer unless package privacy redacts it; record redaction while retaining authorized audit data. Text/IDE/CI/localized renderings derive from this schema, never replace it. Reject malformed records, missing/invalid spans or origins, untyped facts, absent remediation and secret/private raw output leakage with C15-SCHEMA/SPAN/ORIGIN/FACTS/REMEDIATION/REDACTION; suppression retains the internal audit record. Emit versioned schema, ordered stream/catalog, related-span/remediation/redaction artifacts and accepted/rejected golden JSON fixtures from actual reader-through-lowering producers and public consumers.
<!-- diag-canonical-records:end -->



<!-- diag-library-source-diagnostics:start -->

Apply the [complete library-principles source criteria](../stdlib-workflow/plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

L13 complete library diagnostic-carrier criterion (Open): Preserve real owning L13-PROFILE, L13-EFFECT, L13-CAPABILITY, L13-ALLOC, L13-RESOURCE, L13-NUMERIC-MODE, L13-UNSAFE-INVARIANT, L13-EXAMPLE and L13-COMPAT failures with namespace, symbol, active profile, target, declared contract, caller effects, capability context and a concrete alternative when available. Source/generated provenance, stable structured/text representations and redaction survive transport. Compiler, library, conformance and Governance owners produce their actual failures; Diag neither guesses an API outcome nor manufactures a requested diagnostic. Required evidence: genuine failures with complete fields, stable round-trip/origin/order and refused missing/stale/tampered/substituted records across actual selected consumers.
<!-- diag-library-source-diagnostics:end -->


<!-- diag-facet-source-diagnostics:start -->

Apply the [complete facet source criteria](../expand-workflow/plan.md#complete-facet-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L14 complete facet diagnostic-carrier criterion (Open): Preserve actual owning L14-FACET-NOT-ACTIVE, L14-FACET-AMBIGUOUS, L14-PROFILE, L14-BUILD-EFFECT, L14-CAPABILITY, L14-LOWERING, L14-DOMAIN-CHECK, L14-GENERATED-CODE, L14-IR-SCHEMA, L14-COMPOSITION and L14-PRIVACY-BOUNDARY meanings. Each record carries facet id/version, active profile, original source span, generated-origin chain when present, requested effects/capabilities and failed domain rule. Privacy failures also carry value/witness id, public-output schema id, reveal reason when present and the erasing facet edge, preserving redaction rather than leaking private contents. Owners produce genuine failures; Diag transports stable structured/text/origin records without fabricating outcomes. Required evidence: actual positive/refused carrier round-trips, source/generated/external mapping and missing/stale/tampered/privacy-field controls.
<!-- diag-facet-source-diagnostics:end -->



<!-- diag-alternative-types-source-diagnostics:start -->

Apply the [complete alternative type system source criteria](../types-workflow/plan.md#complete-alternative-type-system-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L17 complete alternative-type diagnostic-carrier criterion (Open): Preserve genuine owning L17-PROVIDER, L17-LOWERING, L17-SOUNDNESS, L17-EFFECT-ERASURE, L17-CAPABILITY-ERASURE, L17-OWNERSHIP-FACT, L17-GRADUAL-BOUNDARY, L17-UNSAFE-CAST, L17-DOMAIN-FACT and L17-DIAGNOSTIC-MAP failures. Each retains actual provider id/version, active profile, source span, generated-origin chain, type expression, effect set, capability set, relevant proof id and failed core rule. Owners derive failures; Diag transports stable structured/text records with proper privacy/redaction rather than fabricate a requested outcome. Required evidence: authentic ten-family carrier and field/origin round-trip/tamper/missing-provider/proof refusals from actual selected checker, profile, authority, optimization and consumer boundaries.
<!-- diag-alternative-types-source-diagnostics:end -->


- [ ] `diag/remaining-tasks-and-evidence/preserve-primaryrelatedgenerated-origins-and-stable-order` Preserve primary/related/generated origins and stable order

Status: Open

Evidence baseline: [Projection](../../bootstrap/clojure/src/gravity/c15_diagnostics/projection.clj)

Closing evidence: Macro and optimized-source diagnostics with original and generated locations

<!-- diag-canonical-identity:start -->
Apply the [complete diagnostic source criteria](plan.md#complete-diagnostic-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

Criterion compiler/c15/diagnostic-identity: Derive the resolved diagnostic identity from rule, primary artifact, producing stage and semantic facts, independently of display wording. For fixed source/profile/target/compiler/dependencies/grants preserve deterministic identity and stream order across text, JSON, IDE and CI consumers. Demonstrate wording-only stability and changed semantic subject separation; reject unstable or duplicate IDs with C15-ID and nondeterministic order with C15-ORDER. Retain actual span variants and generated origins. Early reader profile/target/primary syntax-id keys remain explicitly nil until produced; invent no module, coordinates or meta/JVM program context. A private construction record or unresolved digest request is not a resolved identity or safe publication. Emit resolved identity/catalog, ordering and consumer parity records plus positive and negative golden fixtures. Consume actual stage/source/origin facts and canonical diagnostic schema; do not infer compiler execution from a manifest.
<!-- diag-canonical-identity:end -->


<!-- macro-source-diagnostics:start -->

Apply the [complete macro source criteria](../expand-workflow/plan.md#complete-macro-source-criteria) within this existing task. Finite reviewed predicates remain scoped history; status and current independent acceptance do not change.

Complete macro diagnostic consumer criterion (Open): Preserve the 34 active L4/L16/C4/SAFE12 diagnostic meanings and deterministic ordering across reference/alternative engines, early typed diagnostics and normal generated-program failures. Carry provider id/version, macro symbol/version, definition/call/generated span/origin, phase/profile/target, hygiene context, requested/declared/granted effects/capabilities, equivalent L4 rule, missing safety fact and remediation where applicable. Redact secrets while retaining actionable context in actual text/JSON/debugger/LSP consumers. Preserve historical L4-EXPANSION-DEPTH, C4-DEPTH and C4-SIZE only as retired accounting source context; no renamed quota or substituted proof gate is introduced.
<!-- macro-source-diagnostics:end -->



<!-- diag-alternative-types-source-origins:start -->

Apply the [complete alternative type system source criteria](../types-workflow/plan.md#complete-alternative-type-system-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L17 actual alternative-type source diagnostic mapping criterion (Open): Map real checker/type/proof failures through base and alternative macro expansion, generated code and facet lowering to original source spans, provider/version/type-expression facts and definition/call/generated-origin chains. Preserve actual expected-type/runtime-check/failure/blame records where applicable and source identity across common typed-Core and downstream consumers. Reject a generated-only location hiding its source, a stale/foreign type-fact link or a preserved-map flag without the actual map. Required evidence: genuine accepted/rejected generated and handwritten cases, source/generated mapping artifacts and L17-DIAGNOSTIC-MAP/owning failures, preserving existing macro/hygiene and compiler diagnostic contracts.
<!-- diag-alternative-types-source-origins:end -->


- [ ] `diag/remaining-tasks-and-evidence/close-textjsonredaction-and-explain-contracts` Close text/JSON/redaction and explain contracts

Status: Open

Evidence baseline: [CLI presentation](../../bootstrap/clojure/src/gravity/cli/diagnostic_presentation.clj)

Closing evidence: Golden text/JSON equivalence, secret redaction and known-ID explanation

<!-- diag-canonical-views:start -->
Apply the [complete diagnostic source criteria](plan.md#complete-diagnostic-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

Criterion compiler/c15/diagnostic-redaction (Open): Apply declared publication policy to secret values, private package internals, proprietary macro expansions and sensitive target details. Public views exclude secret values and raw model/tool outputs classified private while retaining enough structure to fix the issue and explicit redaction markers. Full records are kept only in authorized private artifact stores; suppression preserves the internal audit artifact. Package privacy may redact generated producer internals but must retain authorized origin evidence. A private construction record or unresolved digest request is not a resolved diagnostic or safe public projection. Required outputs: redaction/publication decision and report, authorized private/audit record, public view and accepted/refused boundary fixtures. Positive fixtures show useful redacted public output, authorized private access and retained suppressed audit data. Reject secret/private leakage, missing markers or useful context, unauthorized full-record access, lost audit/origin records and premature publication with C15-REDACTION and applicable identity/origin/schema diagnostics. Consume actual source/record/origin and publication-policy facts; no renderer or supplied authorization flag grants private-store authority.

Criterion compiler/c15/diagnostic-views (Open): Derive CLI/text, JSON, IDE ranges, CI annotations, localized messages, known-ID explain pages and remediation/quick-fix views from the same validated, resolved C15 records. Preserve diagnostic identity, rule, severity, primary and related span roles, stage/profile/target, typed fact keys/values, remediation category/target, redaction markers and ordering across consumers. Required outputs: actual consumer rendering/quick-fix/explain artifacts, shared-record parity results and golden JSON fixtures. Positive fixtures compare the views and preserve identity/semantic golden results when human wording or localization improves. Reject view-only rule changes, lost origins/remediation, a golden mismatch, plain strings substituted for structured input and goldens keyed only to localized prose; retain C15-GOLDEN and applicable schema/identity/span/origin diagnostics. Consume actual diagnostic producer and identity/order outputs; view presence alone does not establish their implementation.
<!-- diag-canonical-views:end -->


- [ ] `diag/remaining-tasks-and-evidence/integrate-runtimebuildeditor-consumers-without-rule-drift` Integrate runtime/build/editor consumers without rule drift

Status: Open

Evidence baseline: [Tooling contracts](../../docs/phase-13-tooling-and-developer-experience/README.md)

Closing evidence: Same underlying diagnostic identity across public consumers and failure paths


<!-- diag-foundation-runtime-checks:start -->
Apply the [foundation source criteria](plan.md#foundation-source-criteria) within this existing task. Status remains Open; implementation acceptance is unassessed.

Foundation requirement gravity/runtime-check-diagnostic-stability: Specify and exercise runtime checks with deterministic owning code, source/manifest span, profile/target, failed predicate and remediation; compare their structured results across optimization and supported runtime providers. Refusals: Reject missing or changed check attribution, raw host failure leakage and a check erased without surviving evidence; fixtures must trigger the actual check, not match only free text. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection - Selected runtime/provider service contracts and actual link/load/service-use evidence; legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries - Dangerous-operation outcomes, actual checks, proof references and audited wrapper boundaries.
<!-- diag-foundation-runtime-checks:end -->

<!-- diag-implementation-tasks:end -->

## Historical migration record (frozen)

# Tasks: Diagnostics workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md).
Migration owner role: documentation migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

- [x] T001 [US1] Prepare the four owned artifacts with retained-contract links and exact canonical task anchors.
- [x] T002 [US1] Validate local targets/anchors, semantic names, ASCII, existing document/roadmap checks and negative controls; record results in evidence.md.
- [ ] T003 [US2] Independent review accepts the exact artifact tree, mapping and nonclaims; record disposition in evidence.md.
- [ ] T004 [US2] Integrate the reviewed change and record its merge receipt without claiming implementation completion.

## Dependencies and outcomes

T002 uses prepared artifacts; T003 uses validated exact content; T004 follows accepted review. These are documentation-migration tasks only. Implementation tasks remain at the links in plan.md, not copied into this ledger. Current implementation acceptance is unassessed by this package. Existing accepted finite increments retain their original scope and evidence.

## New-name review

All new paths use `diag-workflow` and ordinary artifact names. No planning-ID implementation name or compatibility identifier is changed.
