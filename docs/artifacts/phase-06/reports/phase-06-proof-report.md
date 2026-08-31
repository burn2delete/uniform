# Phase 06 Proof Report - Compiler Architecture

Date: 2026-06-25
Phase: 06 - Compiler Architecture
Status: complete (stage0 compiler architecture capability)
Progress: 24/24 tasks complete

## Governing Documents Read

- `docs/phase-06-compiler-architecture/README.md`
- `docs/phase-06-compiler-architecture/080-c1-compiler-architecture-overview.md`
- `docs/phase-06-compiler-architecture/081-c2-reader-implementation-design.md`
- `docs/phase-06-compiler-architecture/082-c3-syntax-object-model.md`
- `docs/phase-06-compiler-architecture/083-c4-macro-expansion-engine-design.md`
- `docs/phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md`
- `docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md`
- `docs/phase-06-compiler-architecture/086-c7-type-checker-design.md`
- `docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md`
- `docs/phase-06-compiler-architecture/088-c9-ownership-lifetime-and-region-checker-design.md`
- `docs/phase-06-compiler-architecture/089-c10-safety-analysis-pipeline-design.md`
- `docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md`
- `docs/phase-06-compiler-architecture/091-c12-domain-ir-architecture.md`
- `docs/phase-06-compiler-architecture/092-c13-mir-optimization-passes-design.md`
- `docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md`
- `docs/phase-06-compiler-architecture/094-c15-compiler-diagnostics-specification.md`
- `docs/phase-06-compiler-architecture/095-c16-incremental-compilation-design.md`
- `docs/phase-06-compiler-architecture/096-c17-compiler-plugin-and-pass-api-specification.md`
- `docs/phase-06-compiler-architecture/097-c18-compiler-verification-and-pass-correctness-strategy.md`
- `docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md`
- `docs/phase-01-core-language/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-02-safety/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-03-profile-system/IMPLEMENTATION-ROADMAP.md`

## Tasks Completed

- `P06-T01`
- `P06-T02`
- `P06-T03`
- `P06-T04`
- `P06-T05`
- `P06-T06`
- `P06-D080`
- `P06-D081`
- `P06-D082`
- `P06-D083`
- `P06-D084`
- `P06-D085`
- `P06-D086`
- `P06-D087`
- `P06-D088`
- `P06-D089`
- `P06-D090`
- `P06-D091`
- `P06-D092`
- `P06-D093`
- `P06-D094`
- `P06-D095`
- `P06-D096`
- `P06-D097`

No Phase 06 tasks remain open at the Clojure stage0 boundary.

## Accepted Fixtures and Artifacts

- `bootstrap/clojure/fixtures/accepted/compiler-passes.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-checked-core.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-mir.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-domain-ir.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-optimization-lowering.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-verification.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c1-architecture.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c2-reader.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c3-syntax-object.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c4-macro-engine.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c5-name-resolution.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c6-core-lowering.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c7-type-checker.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c8-effect-checker.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c9-ownership-checker.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c10-safety-analysis.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c11-mir-spec.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c12-domain-ir.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c13-optimization.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c14-lowering.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c15-diagnostics.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c16-incremental.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c17-plugin.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c18-verification.gravity`
- `docs/artifacts/phase-06/compiler/stage0-p06-t01-pass-contract-proof.edn`
- `docs/artifacts/phase-06/compiler/stage0-p06-t02-checked-core-proof.edn`
- `docs/artifacts/phase-06/compiler/stage0-p06-t03-mir-proof.edn`
- `docs/artifacts/phase-06/compiler/stage0-p06-t04-domain-ir-proof.edn`
- `docs/artifacts/phase-06/compiler/stage0-p06-t05-optimization-lowering-proof.edn`
- `docs/artifacts/phase-06/compiler/stage0-p06-t06-compiler-verification-proof.edn`
- `docs/artifacts/phase-06/compiler/stage0-p06-d080-c1-compiler-architecture-proof.edn`
- `docs/artifacts/phase-06/compiler/stage0-p06-d081-c2-reader-proof.edn`
- `docs/artifacts/phase-06/compiler/stage0-p06-d082-c3-syntax-object-proof.edn`
- `docs/artifacts/phase-06/compiler/stage0-p06-d083-c4-macro-expansion-proof.edn`
- `docs/artifacts/phase-06/compiler/stage0-p06-d084-c5-name-resolution-proof.edn`
- `docs/artifacts/phase-06/compiler/stage0-p06-d085-c6-core-lowering-proof.edn`
- `docs/artifacts/phase-06/compiler/stage0-p06-d086-c7-type-checker-proof.edn`
- `docs/artifacts/phase-06/compiler/stage0-p06-d087-c8-effect-checker-proof.edn`
- `docs/artifacts/phase-06/compiler/stage0-p06-d088-c9-ownership-checker-proof.edn`
- `docs/artifacts/phase-06/compiler/stage0-p06-d089-c10-safety-analysis-proof.edn`
- `docs/artifacts/phase-06/compiler/stage0-p06-d090-c11-mir-spec-proof.edn`
- `docs/artifacts/phase-06/compiler/stage0-p06-d091-c12-domain-ir-proof.edn`
- `docs/artifacts/phase-06/compiler/stage0-p06-d092-c13-optimization-proof.edn`
- `docs/artifacts/phase-06/compiler/stage0-p06-d093-c14-lowering-proof.edn`
- `docs/artifacts/phase-06/compiler/stage0-p06-d094-c15-diagnostics-proof.edn`
- `docs/artifacts/phase-06/compiler/stage0-p06-d095-c16-incremental-proof.edn`
- `docs/artifacts/phase-06/compiler/stage0-p06-d096-c17-plugin-proof.edn`
- `docs/artifacts/phase-06/compiler/stage0-p06-d097-c18-verification-proof.edn`
- `docs/artifacts/phase-06/reports/p06-t01-pass-contract-report.md`
- `docs/artifacts/phase-06/reports/p06-t02-checked-core-report.md`
- `docs/artifacts/phase-06/reports/p06-t03-mir-report.md`
- `docs/artifacts/phase-06/reports/p06-t04-domain-ir-report.md`
- `docs/artifacts/phase-06/reports/p06-t05-optimization-lowering-report.md`
- `docs/artifacts/phase-06/reports/p06-t06-compiler-verification-report.md`
- `docs/artifacts/phase-06/reports/p06-d080-c1-compiler-architecture-report.md`
- `docs/artifacts/phase-06/reports/p06-d081-c2-reader-report.md`
- `docs/artifacts/phase-06/reports/p06-d082-c3-syntax-object-report.md`
- `docs/artifacts/phase-06/reports/p06-d083-c4-macro-expansion-report.md`
- `docs/artifacts/phase-06/reports/p06-d084-c5-name-resolution-report.md`
- `docs/artifacts/phase-06/reports/p06-d085-c6-core-lowering-report.md`
- `docs/artifacts/phase-06/reports/p06-d086-c7-type-checker-report.md`
- `docs/artifacts/phase-06/reports/p06-d087-c8-effect-checker-report.md`
- `docs/artifacts/phase-06/reports/p06-d088-c9-ownership-checker-report.md`
- `docs/artifacts/phase-06/reports/p06-d089-c10-safety-analysis-report.md`
- `docs/artifacts/phase-06/reports/p06-d090-c11-mir-spec-report.md`
- `docs/artifacts/phase-06/reports/p06-d091-c12-domain-ir-report.md`
- `docs/artifacts/phase-06/reports/p06-d092-c13-optimization-report.md`
- `docs/artifacts/phase-06/reports/p06-d093-c14-lowering-report.md`
- `docs/artifacts/phase-06/reports/p06-d094-c15-diagnostics-report.md`
- `docs/artifacts/phase-06/reports/p06-d095-c16-incremental-report.md`
- `docs/artifacts/phase-06/reports/p06-d096-c17-plugin-report.md`
- `docs/artifacts/phase-06/reports/p06-d097-c18-verification-report.md`

The `compiler-passes` fixture emits `:gravity/stage0-pass-contract-manifest-artifact`
with canonical pipeline stage order, 19 pass contracts, pipeline manifest,
diagnostic registry, diagnostic schema, diagnostic fixtures, incremental cache
key records, cache entry manifest, proof reuse records, speculative reuse
records, plugin manifest, plugin pass contracts, plugin execution traces, pass
risk classifications, compiler trust report, release-gate report, conformance
results, and capability-based proof.

The `compiler-checked-core` fixture emits
`:gravity/stage0-checked-core-pipeline-artifact` with 11 pre-MIR stage records,
source-unit identity, syntax object stream, macro expansion trace, namespace
binding table, verified core lowering records, typed/effected facts, capability
proof records, profile validation report, ownership facts, safety outcome
records, stage output identities, conformance results, and capability-based
proof.

The `compiler-mir` fixture emits `:gravity/stage0-mir-artifact` with a
target-independent MIR module, operation records, control-flow graph,
data-flow graph, type/effect/ownership tables, capability proof table, safety
outcome table, runtime check table, source-origin map, domain-anchor table,
target-lowering input readiness, MIR verifier report, conformance results, and
capability-based proof.

The `compiler-domain-ir` fixture emits `:gravity/stage0-domain-ir-artifact`
with a domain IR registry, domain IR artifact schema, semantic anchor map,
entry and exit pass records, domain verifier report, proof and certificate
references, lowering eligibility matrix, fallback records, plugin registration
policy, conformance results, and capability-based proof for EFIR, schema IR,
workflow IR, AI agent IR, query IR, HDL/state-machine IR, UI IR, GPU IR, FFI
boundary IR, and package/artifact graph IR.

The `compiler-optimization-lowering` fixture emits
`:gravity/stage0-optimization-lowering-artifact` with optimization pass
contracts, deterministic pipeline manifest, decision log, invalidation ledger,
analysis cache records, proof/certificate usage, residual cost report,
post-pass verifier reports, lowering request, target eligibility, ABI and
runtime/provider manifests, layout decision record, proof-to-target metadata
map, unsupported feature report, target artifact manifest, conformance results,
and capability-based proof.

The `compiler-verification` fixture emits
`:gravity/stage0-compiler-verification-artifact` with diagnostic schema and
streams, incremental graph/cache/revalidation records, plugin
manifest/API/sandbox/execution records, verification plan, pass risk records,
translation validation logs, trust report, release gate report, counterexample
records, conformance results, and capability-based proof.

The `compiler-c1-architecture` fixture emits
`:gravity/stage0-c1-compiler-architecture-artifact` with the canonical
pipeline manifest, pass contract registry, stage artifact records, evidence
log, IR snapshot bundle, diagnostic stream, artifact provenance graph,
verifier gate reports, self-hosting comparison inputs, conformance results, and
capability-based proof.

The `compiler-c2-reader` fixture emits
`:gravity/stage0-c2-reader-document-artifact` with source-unit identity, token
stream records, form tree records, syntax seed stream, reader source map,
literal decoding records, comment/trivia retention records, reader extension
policy and invocation records, semantic-error deferment record, incremental
reader hashes, conformance results, and capability-based proof.

The `compiler-c3-syntax` fixture emits
`:gravity/stage0-c3-syntax-object-artifact` with a syntax object schema, stable
syntax object stream, exposed hygiene context map, origin-chain graph, metadata
ledger, generated syntax report, fact invalidation ledger, syntax verification
report, serialization fixture, conformance results, and capability-based
proof.

The `compiler-c4-macro` fixture emits
`:gravity/stage0-c4-macro-expansion-artifact` with expansion input, macro
environment, expanded syntax stream, deterministic expansion trace,
hygiene/capture records, build-effect log, macro safety declarations,
generated-origin source map, expansion cache key, trace replay report, macro
safety report, self-hosting comparison inputs, conformance results, and
capability-based proof.

The `compiler-c5-resolution` fixture emits
`:gravity/stage0-c5-name-resolution-artifact` with namespace analysis, binding
table, alias table, import/export table, lexical scope graph, dependency graph,
cross-profile edge report, resolution diagnostics, incremental invalidation
keys, conformance results, and capability-based proof.

The `compiler-c6-lowering` fixture emits
`:gravity/stage0-c6-core-lowering-artifact` with a core AST module, core-node
table, surface-to-core map, desugaring trace, evaluation-order records,
domain-boundary records, core verifier report, versioned lowering-rule
invalidation record, conformance results, and capability-based proof.

The `compiler-c7-type-check` fixture emits
`:gravity/stage0-c7-type-checker-artifact` with a typed-core module, type
environment, type facts, constraint ledger, function type table, generic
instantiation table, protocol dispatch type table, dynamic boundary records,
cast and conversion records, schema type links, layout facts, typed-core
verifier report, conformance results, and capability-based proof.

The `compiler-c8-effect-check` fixture emits
`:gravity/stage0-c8-effect-checker-artifact` with an effect graph, function
latent effect table, namespace effect summary, effect legality report,
capability proof records, build-effect log, replay requirements, ordering
constraints, residual effect report, verifier report, conformance results, and
capability-based proof.

The `compiler-c9-ownership-check` fixture emits
`:gravity/stage0-c9-ownership-checker-artifact` with an ownership graph, borrow
graph, lifetime interval map, move and consume records, escape-analysis report,
region lifetime graph, arena generation graph, linear resource flow graph,
transfer records, runtime check records, unsafe audit references, verifier
report, conformance results, and capability-based proof.

The `compiler-c10-safety-analysis` fixture emits
`:gravity/stage0-c10-safety-analysis-artifact` with a safety operation
inventory, safety outcome records, runtime check list, proof obligation list,
proof certificate references, unsafe island audit manifest, taint and
capability safety report, generated-code safety provenance, optimization safety
preservation records, verifier report, conformance results, and
capability-based proof.

The `compiler-c11-mir-spec` fixture emits
`:gravity/stage0-c11-mir-spec-artifact` with a target-independent MIR module,
20 operation records covering all 20 C11 operation families, control-flow and
data-flow graphs, type and effect tables, source-origin map, domain-anchor
table, runtime-check and safety-outcome tables, MIR diagnostic stream,
verifier report, conformance results, and capability-based proof.

The `compiler-c12-domain-ir` fixture emits
`:gravity/stage0-c12-domain-ir-architecture-artifact` from the C11 MIR
specification artifact. It records the domain IR registry, domain artifact
schema, semantic anchor map, entry and exit pass records, domain verifier
report, proof and certificate references, lowering eligibility matrix,
fallback records, plugin registration policy, domain diagnostic catalog,
conformance results, and capability-based proof.

The `compiler-c13-optimization` fixture emits
`:gravity/stage0-c13-mir-optimization-artifact` from the C12 domain IR
architecture artifact. It records MIR optimization pass contracts,
deterministic pipeline manifest, decision log, invalidated-fact ledger,
analysis cache records, proof and certificate usage, residual cost report,
check-elision and effect-order proof records, safety and domain-anchor refresh
records, replay record, post-pass verifier reports, optimized MIR artifact,
diagnostic catalog, conformance results, and capability-based proof.

The `compiler-c14-lowering` fixture emits
`:gravity/stage0-c14-target-lowering-artifact` from the C13 MIR optimization
artifact. It records lowering request verification, target eligibility, ABI and
runtime/provider manifests, provider selection records, layout decisions,
proof-to-target metadata, source/generated-origin mapping, capability
preservation, unsupported-feature handling, target artifact manifest, diagnostic
catalog, conformance results, and capability-based proof.

The `compiler-c15-diagnostics` fixture emits
`:gravity/stage0-c15-compiler-diagnostics-artifact` from the C14 target lowering
artifact. It records a diagnostic schema, deterministic diagnostic stream,
diagnostic catalog, related-span map, remediation and quick-fix records,
redaction report, CLI/IDE/CI/safety/package rendering records, golden diagnostic
fixtures, conformance results, and capability-based proof.

The `compiler-c16-incremental` fixture emits
`:gravity/stage0-c16-incremental-compilation-artifact` from the C15 diagnostics
artifact. It records an incremental dependency graph, cache key schema, stage
cache keys, cache entry manifest, invalidation trace, artifact reuse and
revalidation reports, stale-proof and stale-diagnostic rejection, build-effect
replay, speculative reuse boundaries, reproducible release rebuild evidence,
diagnostics, conformance results, and capability-based proof.

The `compiler-c17-plugin` fixture emits
`:gravity/stage0-c17-compiler-plugin-artifact` from the C16 incremental
compilation artifact. It records a plugin manifest, API compatibility report,
sandbox and trusted-package grants, hermetic build-effect denial, plugin pass
registration records, domain and facet registration records, plugin cache keys,
plugin output artifacts, plugin execution traces, diagnostics, conformance
results, and capability-based proof.

The `compiler-c18-verification` fixture emits
`:gravity/stage0-c18-compiler-verification-artifact` from the C17 compiler
plugin/pass API artifact. It records a compiler verification plan, pass risk
classification, pass evidence records, stage verifier reports, translation
validation logs, proof and certificate references, differential and property
fixture results, compiler trust report, release gate report, blocked
release-gate failure fixture, counterexample regression artifact, experimental
pass gates, plugin evidence report, backend conformance report, verification
diagnostics, conformance results, and capability-based proof.

## Rejected Fixtures and Diagnostics

The Clojure suite includes 26 Phase 06 pass-contract rejected fixtures:

- `C1-PIPELINE`, `C1-PASS-CONTRACT`, `C1-EVIDENCE-DROP`,
  `C1-UNCHECKED-BACKEND`, and `C1-MANIFEST`
- `C15-SCHEMA`, `C15-ID`, `C15-SPAN`, `C15-ORIGIN`, `C15-FACTS`,
  `C15-REMEDIATION`, `C15-REDACTION`, and `C15-ORDER`
- `C16-KEY`, `C16-ENTRY`, `C16-PROOF`, and `C16-SPECULATIVE`
- `C17-MANIFEST`, `C17-API`, `C17-CAPABILITY`,
  `C17-PASS-CONTRACT`, and `C17-OUTPUT`
- `C18-RISK`, `C18-EVIDENCE`, `C18-TRUST-REPORT`, and
  `C18-RELEASE-GATE`

The Clojure suite also includes 10 checked-core integration rejected fixtures:

- `C1-EVIDENCE-DROP`
- `C2-HASH`
- `C3-ORIGIN`
- `C4-TRACE`
- `C5-UNRESOLVED`
- `C6-VERIFY`
- `C7-VERIFY`
- `C8-CAPABILITY`
- `C9-LINEAR-LEAK`
- `C10-NO-OUTCOME`

The Clojure suite also includes 10 MIR verifier rejected fixtures:

- `C11-MODULE`
- `C11-BLOCK`
- `C11-DOMINANCE`
- `C11-TYPE`
- `C11-EFFECT`
- `C11-SAFETY`
- `C11-ORIGIN`
- `C11-DOMAIN`
- `C11-TARGET-LEAK`
- `C11-VERIFY`

The Clojure suite also includes 9 domain IR rejected fixtures:

- `C12-REGISTRATION`
- `C12-ANCHOR`
- `C12-SCHEMA`
- `C12-FACTS`
- `C12-VERIFY`
- `C12-PROOF`
- `C12-LOWERING`
- `C12-FALLBACK`
- `C12-PLUGIN`

The Clojure suite also includes 20 optimization and lowering rejected fixtures:

- `C13-CONTRACT`, `C13-PRESERVE`, `C13-INVALIDATE`, `C13-PROOF`,
  `C13-CHECK-ELISION`, `C13-EFFECT`, `C13-SAFETY`, `C13-DOMAIN`,
  `C13-NONDETERMINISM`, and `C13-VERIFY`
- `C14-INPUT`, `C14-PROFILE`, `C14-TARGET`, `C14-ABI`, `C14-RUNTIME`,
  `C14-PROVIDER`, `C14-PROOF-METADATA`, `C14-CAPABILITY`,
  `C14-UNSUPPORTED`, and `C14-MANIFEST`

The Clojure suite also includes 37 compiler diagnostics and verification
rejected fixtures covering all C15, C16, C17, and C18 diagnostics.

The Clojure suite also includes 7 C1 document coverage rejected fixtures:

- `C1-PIPELINE`
- `C1-PASS-CONTRACT`
- `C1-EVIDENCE-DROP`
- `C1-UNCHECKED-BACKEND`
- `C1-DOMAIN-ANCHOR`
- `C1-MANIFEST`
- `C1-SELF-HOST`

The Clojure suite also includes 9 C2 reader document coverage rejected
fixtures:

- `C2-ENCODING`
- `C2-DELIMITER`
- `C2-STRING`
- `C2-MAP`
- `C2-SET`
- `C2-METADATA`
- `C2-ABBREV`
- `C2-EXTENSION`
- `C2-HASH`

The Clojure suite also includes 9 C3 syntax object document coverage rejected
fixtures:

- `C3-SHAPE`
- `C3-ID`
- `C3-SPAN`
- `C3-ORIGIN`
- `C3-HYGIENE`
- `C3-CAPTURE`
- `C3-METADATA`
- `C3-FACT-STALE`
- `C3-SERIALIZE`

The Clojure suite also includes 10 C4 macro expansion document coverage
rejected fixtures:

- `C4-NOT-MACRO`
- `C4-RETURN`
- `C4-DEPTH`
- `C4-SIZE`
- `C4-BUILD-EFFECT`
- `C4-HYGIENE`
- `C4-CAPTURE`
- `C4-GENERATED-UNSAFE`
- `C4-PROFILE`
- `C4-TRACE`

The Clojure suite also includes 10 C5 name resolution document coverage
rejected fixtures:

- `C5-UNRESOLVED`
- `C5-AMBIGUOUS`
- `C5-PRIVATE`
- `C5-ALIAS`
- `C5-SHADOW`
- `C5-CYCLE`
- `C5-CROSS-PROFILE`
- `C5-CAPABILITY`
- `C5-TARGET`
- `C5-FOREIGN`

The Clojure suite also includes 8 C6 AST and core lowering document coverage
rejected fixtures:

- `C6-LOWERING-GAP`
- `C6-CORE-SHAPE`
- `C6-EVAL-ORDER`
- `C6-ORIGIN`
- `C6-EFFECT-DROP`
- `C6-UNSAFE-DROP`
- `C6-DOMAIN-BOUNDARY`
- `C6-VERIFY`

The Clojure suite also includes 10 C7 type checker document coverage rejected
fixtures:

- `C7-TYPE-MISMATCH`
- `C7-ANNOTATION`
- `C7-DYNAMIC`
- `C7-CAST`
- `C7-NULLABILITY`
- `C7-GENERIC`
- `C7-PROTOCOL`
- `C7-LAYOUT`
- `C7-SCHEMA`
- `C7-VERIFY`

The Clojure suite also includes 9 C8 effect checker document coverage rejected
fixtures:

- `C8-UNDECLARED`
- `C8-PROFILE`
- `C8-CAPABILITY`
- `C8-BUILD`
- `C8-REPLAY`
- `C8-ORDER`
- `C8-RUNTIME`
- `C8-UNKNOWN`
- `C8-VERIFY`

The Clojure suite also includes 12 C9 ownership checker document coverage
rejected fixtures:

- `C9-USE-AFTER-MOVE`
- `C9-USE-AFTER-CONSUME`
- `C9-BORROW-ESCAPE`
- `C9-MUT-ALIAS`
- `C9-MOVE-WHILE-BORROWED`
- `C9-REGION-ESCAPE`
- `C9-ARENA-GENERATION`
- `C9-LINEAR-LEAK`
- `C9-LINEAR-DOUBLE`
- `C9-TRANSFER`
- `C9-RUNTIME-CHECK`
- `C9-UNSAFE`

The Clojure suite also includes 10 C10 safety analysis document coverage
rejected fixtures:

- `C10-NO-OUTCOME`
- `C10-PROOF`
- `C10-CHECK`
- `C10-UNSAFE`
- `C10-GENERATED`
- `C10-TAINT`
- `C10-CAPABILITY`
- `C10-FFI`
- `C10-NUMERIC`
- `C10-OPTIMIZATION`

The Clojure suite also includes 10 C11 MIR specification document coverage
rejected fixtures:

- `C11-MODULE`
- `C11-BLOCK`
- `C11-DOMINANCE`
- `C11-TYPE`
- `C11-EFFECT`
- `C11-SAFETY`
- `C11-ORIGIN`
- `C11-DOMAIN`
- `C11-TARGET-LEAK`
- `C11-VERIFY`

The Clojure suite also includes 9 C12 domain IR architecture document coverage
rejected fixtures:

- `C12-REGISTRATION`
- `C12-ANCHOR`
- `C12-SCHEMA`
- `C12-FACTS`
- `C12-VERIFY`
- `C12-PROOF`
- `C12-LOWERING`
- `C12-FALLBACK`
- `C12-PLUGIN`

The Clojure suite also includes 10 C13 MIR optimization document coverage
rejected fixtures:

- `C13-CONTRACT`
- `C13-PRESERVE`
- `C13-INVALIDATE`
- `C13-PROOF`
- `C13-CHECK-ELISION`
- `C13-EFFECT`
- `C13-SAFETY`
- `C13-DOMAIN`
- `C13-NONDETERMINISM`
- `C13-VERIFY`

The Clojure suite also includes 10 C14 target lowering document coverage
rejected fixtures:

- `C14-INPUT`
- `C14-PROFILE`
- `C14-TARGET`
- `C14-ABI`
- `C14-RUNTIME`
- `C14-PROVIDER`
- `C14-PROOF-METADATA`
- `C14-CAPABILITY`
- `C14-UNSUPPORTED`
- `C14-MANIFEST`

The Clojure suite also includes 9 C15 compiler diagnostics document coverage
rejected fixtures:

- `C15-SCHEMA`
- `C15-ID`
- `C15-SPAN`
- `C15-ORIGIN`
- `C15-FACTS`
- `C15-REMEDIATION`
- `C15-REDACTION`
- `C15-ORDER`
- `C15-GOLDEN`

The Clojure suite also includes 9 C16 incremental compilation document coverage
rejected fixtures:

- `C16-KEY`
- `C16-ENTRY`
- `C16-STALE`
- `C16-PROOF`
- `C16-SPECULATIVE`
- `C16-REPLAY`
- `C16-POLICY`
- `C16-DIAGNOSTIC`
- `C16-GRAPH`

The Clojure suite also includes 10 C17 compiler plugin/pass API document
coverage rejected fixtures:

- `C17-MANIFEST`
- `C17-API`
- `C17-CAPABILITY`
- `C17-BUILD-EFFECT`
- `C17-SANDBOX`
- `C17-PASS-CONTRACT`
- `C17-OUTPUT`
- `C17-DOMAIN`
- `C17-FACET`
- `C17-TRUST`

The Clojure suite also includes 9 C18 compiler verification/pass-correctness
document coverage rejected fixtures:

- `C18-RISK`
- `C18-EVIDENCE`
- `C18-VALIDATION`
- `C18-PROOF`
- `C18-TRUST-REPORT`
- `C18-RELEASE-GATE`
- `C18-COUNTEREXAMPLE`
- `C18-PLUGIN`
- `C18-BACKEND`

## Validation Commands

```text
clojure -M:gravity compiler-passes bootstrap/clojure/fixtures/accepted/compiler-passes.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-pass-contract-manifest-artifact,
 :pass :compiler-pass-contract-manifest,
 :output :pass-contract-manifest,
 :status :complete,
 :stages 19,
 :contracts 19,
 :cache-keys 1,
 :plugin-passes 1,
 :risk-records 19,
 :diagnostic-families 26,
 :proof :complete}
```

Artifact hash:

```text
sha256:777fa920f45f520006f5a510839998d6bfdaec7863cc5873eb115b555492af25
```

```text
clojure -M:test
Ran 70 tests containing 3829 assertions.
0 failures, 0 errors.
```

```text
clojure -M:gravity checked-core bootstrap/clojure/fixtures/accepted/compiler-checked-core.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-checked-core-pipeline-artifact,
 :pass :reader-through-checked-core-integration,
 :output :checked-core,
 :status :complete,
 :stages 11,
 :stage-outputs 11,
 :syntax 6,
 :macro-steps 2,
 :bindings 5,
 :core-nodes 4,
 :type-facts 23,
 :capability-proofs 4,
 :ownership-facts 1,
 :safety-outcomes 1,
 :proof :complete}
```

Artifact hash:

```text
sha256:19ba8a51e68721a8b50d0389f53ce1fe48790a6d0750b45ee94a387fd2712cb1
```

```text
clojure -M:gravity mir bootstrap/clojure/fixtures/accepted/compiler-mir.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-mir-artifact,
 :pass :mir-construction-and-verifier,
 :output :gravity/mir,
 :status :complete,
 :ops 23,
 :families 20,
 :blocks 1,
 :data-edges 22,
 :types 23,
 :safety 1,
 :runtime-checks 1,
 :proof :complete}
```

Artifact hash:

```text
sha256:6b27b18f6e09472c8714536bcd7d65fed947243f548aa30c99a5d7cc4517ea53
```

```text
clojure -M:gravity domain-ir bootstrap/clojure/fixtures/accepted/compiler-domain-ir.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-domain-ir-artifact,
 :pass :domain-ir-registry-and-artifacts,
 :output :domain-ir-registry,
 :status :complete,
 :domains 10,
 :registrations 10,
 :anchors 10,
 :proofs 10,
 :lowering 10,
 :fallbacks 10,
 :proof :complete}
```

Artifact hash:

```text
sha256:34df63f8862f793d88f2d910f2b24f5cc13e5d10e12907107297848099f94dd4
```

```text
clojure -M:gravity optimize-lower bootstrap/clojure/fixtures/accepted/compiler-optimization-lowering.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-optimization-lowering-artifact,
 :pass :optimization-and-target-lowering-api,
 :output :optimization-lowering-manifest,
 :status :complete,
 :contracts 6,
 :decisions 6,
 :invalidations 6,
 :verifiers 6,
 :providers 3,
 :metadata 3,
 :unsupported 1,
 :proof :complete}
```

Artifact hash:

```text
sha256:07ed66f3a131e02a57abf31989a7d63e72ebaef7a26d28c1432756c62d68d98e
```

```text
clojure -M:gravity compiler-verify bootstrap/clojure/fixtures/accepted/compiler-verification.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-compiler-verification-artifact,
 :pass :compiler-diagnostics-and-verification,
 :output :compiler-verification-report,
 :status :complete,
 :diagnostics 1,
 :cache-nodes 9,
 :plugin :accepted,
 :risk 3,
 :translation 1,
 :trust :complete,
 :release :passed,
 :proof :complete}
```

Artifact hash:

```text
sha256:6ac6868e7993a284c37d2d0527355c974981017354557be5eaa08c79b4694a8f
```

```text
clojure -M:gravity compiler-c1-architecture bootstrap/clojure/fixtures/accepted/compiler-c1-architecture.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-c1-compiler-architecture-artifact,
 :task "P06-D080",
 :status :complete,
 :pipeline-stages 19,
 :pass-contracts 19,
 :stage-artifacts 6,
 :ir-snapshots 5,
 :verifier-gates 19,
 :rejected-designs 7,
 :proof :complete}
```

Artifact hash:

```text
sha256:626fda5148cf8db9ce7ab5dac84d6758cabc8abc74708b69a6cb002f5b0ad30a
```

```text
clojure -M:gravity compiler-c2-reader bootstrap/clojure/fixtures/accepted/compiler-c2-reader.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-c2-reader-document-artifact,
 :task "P06-D081",
 :status :complete,
 :source-units 1,
 :tokens 10,
 :forms 10,
 :syntax-seeds 10,
 :literal-records 76,
 :extension-records 1,
 :rejected-designs 9,
 :proof :complete}
```

Artifact hash:

```text
sha256:10cabc6359fc884e72f45bc2e7918d41391f63be18e5865da6becde0cbaeea9f
```

```text
clojure -M:gravity compiler-c3-syntax bootstrap/clojure/fixtures/accepted/compiler-c3-syntax-object.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-c3-syntax-object-artifact,
 :task "P06-D082",
 :status :complete,
 :syntax-objects 6,
 :generated-syntax-objects 1,
 :hygiene-contexts 6,
 :origin-chain-nodes 6,
 :rejected-designs 9,
 :serialization-roundtrip true,
 :proof :complete}
```

Artifact hash:

```text
sha256:8015efb36d657957b1ea0405fb51b3efd213465c726e46f75830b77e44fbe34b
```

```text
clojure -M:gravity compiler-c4-macro bootstrap/clojure/fixtures/accepted/compiler-c4-macro-engine.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-c4-macro-expansion-artifact,
 :task "P06-D083",
 :status :complete,
 :expanded-syntax-records 6,
 :macro-expansion-steps 3,
 :hygiene-capture-records 1,
 :build-effect-log-status :complete,
 :macro-safety-status :complete,
 :trace-replay-status :passed,
 :rejected-designs 10,
 :proof :complete}
```

Artifact hash:

```text
sha256:2430d0c2eb94c2f43a11dc468ca27f5bb32186efdf6e697824d2fe6ed4f7c82b
```

```text
clojure -M:gravity compiler-c5-resolution bootstrap/clojure/fixtures/accepted/compiler-c5-name-resolution.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-c5-name-resolution-artifact,
 :task "P06-D084",
 :status :complete,
 :binding-records 45,
 :namespace-bindings 16,
 :local-bindings 5,
 :aliases 3,
 :dependency-edges 3,
 :rejected-designs 10,
 :proof :complete}
```

Artifact hash:

```text
sha256:9b94f6b87dabeb53716f20d452d70a8726f94a6861a81e051bf38612a4a8da94
```

```text
clojure -M:gravity compiler-c6-lowering bootstrap/clojure/fixtures/accepted/compiler-c6-core-lowering.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-c6-core-lowering-artifact,
 :task "P06-D085",
 :status :complete,
 :core-nodes 53,
 :core-roots 6,
 :surface-map-entries 7,
 :evaluation-order-records 24,
 :domain-boundaries 1,
 :rejected-designs 8,
 :proof :complete}
```

Artifact hash:

```text
sha256:baee4e97293095597550832e56e1a67db8a2f06428fe5bf4ae17cdfbf05a554f
```

```text
clojure -M:gravity compiler-c7-type-check bootstrap/clojure/fixtures/accepted/compiler-c7-type-checker.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-c7-type-checker-artifact,
 :task "P06-D086",
 :status :complete,
 :type-facts 76,
 :constraints 76,
 :functions 2,
 :dynamic-boundaries 1,
 :casts 1,
 :generics 1,
 :dispatch 1,
 :schema-links 1,
 :layout-facts 76,
 :rejected-designs 10,
 :proof :complete}
```

Artifact hash:

```text
sha256:02a8d8e1f9cb1ec17d39cb3a3cd6183facb6002588d2376025099378239f6b94
```

```text
clojure -M:gravity compiler-c8-effect-check bootstrap/clojure/fixtures/accepted/compiler-c8-effect-checker.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-c8-effect-checker-artifact,
 :task "P06-D087",
 :status :complete,
 :effect-nodes 76,
 :inferred-effects 4,
 :function-effect-summaries 2,
 :legality-records 4,
 :capability-proofs 4,
 :build-effects 1,
 :replay-records 1,
 :ordering-constraints 10,
 :residual-effects 3,
 :rejected-designs 9,
 :proof :complete}
```

Artifact hash:

```text
sha256:7b85db9e074425bc276dc6b7e8e6cd6147902c3c1a2b368dd20710ae13022755
```

```text
clojure -M:gravity compiler-c9-ownership-check bootstrap/clojure/fixtures/accepted/compiler-c9-ownership-checker.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-c9-ownership-checker-artifact,
 :task "P06-D088",
 :status :complete,
 :owners 76,
 :borrow-edges 5,
 :lifetimes 9,
 :moves 1,
 :consumes 1,
 :regions 2,
 :arenas 1,
 :linear-resources 2,
 :transfers 4,
 :runtime-checks 4,
 :unsafe-audits 2,
 :rejected-designs 12,
 :proof :complete}
```

Artifact hash:

```text
sha256:936e57abb689b6b5645a77aee462071c2de9d61ba2c13ae9612a54179fb438d8
```

```text
clojure -M:gravity compiler-c10-safety-analysis bootstrap/clojure/fixtures/accepted/compiler-c10-safety-analysis.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-c10-safety-analysis-artifact,
 :task "P06-D089",
 :status :complete,
 :operations 12,
 :outcomes 12,
 :runtime-checks 3,
 :proof-obligations 7,
 :certificates 3,
 :unsafe-islands 2,
 :taint-records 1,
 :capability-records 1,
 :generated-records 1,
 :optimization-records 2,
 :rejected-designs 10,
 :proof :complete}
```

Artifact hash:

```text
sha256:88fa7c62fbbab1f2cc01e2898a5964f1466c23345ca76de39e35e89d8600d2d4
```

```text
clojure -M:gravity compiler-c11-mir-spec bootstrap/clojure/fixtures/accepted/compiler-c11-mir-spec.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-c11-mir-spec-artifact,
 :task "P06-D090",
 :status :complete,
 :operations 20,
 :families 20,
 :blocks 1,
 :data-edges 19,
 :type-table 18,
 :effect-table 20,
 :source-origins 20,
 :domain-anchors 1,
 :runtime-checks 3,
 :safety-outcomes 12,
 :diagnostics 10,
 :proof :complete}
```

Artifact hash:

```text
sha256:39867f721bbe5b520ec8218af20782eacee2699a5c45874d3ad1a9d89f2d3ee6
```

```text
clojure -M:gravity compiler-c12-domain-ir bootstrap/clojure/fixtures/accepted/compiler-c12-domain-ir.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-c12-domain-ir-architecture-artifact,
 :task "P06-D091",
 :status :complete,
 :domain-registrations 10,
 :domain-artifacts 10,
 :semantic-anchors 10,
 :proof-records 10,
 :lowering-records 10,
 :fallback-records 10,
 :diagnostics 9,
 :proof :complete}
```

Artifact hash:

```text
sha256:49095f63f6003d82ae9e43826e1eaa6cdc482995b39dfd78999b359ceffb85ec
```

```text
clojure -M:gravity compiler-c13-optimization bootstrap/clojure/fixtures/accepted/compiler-c13-optimization.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-c13-mir-optimization-artifact,
 :task "P06-D092",
 :status :complete,
 :pass-contracts 6,
 :decisions 6,
 :invalidations 6,
 :analysis-caches 6,
 :proof-records 6,
 :post-pass-verifiers 6,
 :diagnostics 10,
 :proof :complete}
```

Artifact hash:

```text
sha256:dcd53778692db446e3bf54caf889bf66b47765a54abd73c00e6773552a0c2ce9
```

```text
clojure -M:gravity compiler-c14-lowering bootstrap/clojure/fixtures/accepted/compiler-c14-lowering.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-c14-target-lowering-artifact,
 :task "P06-D093",
 :status :complete,
 :provider-records 3,
 :target-metadata 3,
 :target-artifacts 1,
 :unsupported-feature-records 1,
 :diagnostics 10,
 :proof :complete}
```

Artifact hash:

```text
sha256:c641c11d84d3c059d602c365f16cf8055317a197e943d28d85b74d427a25a7b8
```

```text
clojure -M:gravity compiler-c15-diagnostics bootstrap/clojure/fixtures/accepted/compiler-c15-diagnostics.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-c15-compiler-diagnostics-artifact,
 :task "P06-D094",
 :status :complete,
 :structured-diagnostics 4,
 :catalog-rules 9,
 :quick-fixes 9,
 :renderers 5,
 :golden-fixtures 9,
 :diagnostics 9,
 :proof :complete}
```

Artifact hash:

```text
sha256:a0815c5ae472679476b6c6879fb9e749ec64015b99bc4eedc7edce52becab401
```

```text
clojure -M:gravity compiler-c16-incremental bootstrap/clojure/fixtures/accepted/compiler-c16-incremental.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-c16-incremental-compilation-artifact,
 :task "P06-D095",
 :status :complete,
 :graph-nodes 15,
 :graph-edges 10,
 :cache-keys 8,
 :cache-entries 8,
 :invalidations 19,
 :diagnostics 9,
 :proof :complete}
```

Artifact hash:

```text
sha256:a479b07d535ad8dd46edf96ec5a06ad26d8c1c722ee1805d69eddbaee17c3d99
```

```text
clojure -M:gravity compiler-c17-plugin bootstrap/clojure/fixtures/accepted/compiler-c17-plugin.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-c17-compiler-plugin-artifact,
 :task "P06-D096",
 :status :complete,
 :trust-grants 2,
 :passes 2,
 :cache-keys 2,
 :traces 2,
 :outputs 2,
 :diagnostics 10,
 :proof :complete}
```

Artifact hash:

```text
sha256:db2917d2f7edf975c9d563a5f81a4154906c86a73d90b40a26406f18cdcdb89a
```

```text
clojure -M:gravity compiler-c18-verification bootstrap/clojure/fixtures/accepted/compiler-c18-verification.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-c18-compiler-verification-artifact,
 :task "P06-D097",
 :status :complete,
 :risk-records 8,
 :evidence-records 8,
 :translation-validations 2,
 :proofs 3,
 :counterexamples 1,
 :backend-conformance 1,
 :diagnostics 9,
 :proof :complete}
```

Artifact hash:

```text
sha256:0cd9259ccf67eb4299fb59872730abd61205942e719e740f193485105f65103c
```

## Residual Risks

This proof report completes `P06-T01` through `P06-T06` and `P06-D080` through
`P06-D097` at the current Clojure stage0 boundary. It does not claim
production compiler readiness, backend code
generation, runtime execution, external plugin ecosystem support, release
readiness, or self-hosting.
