# P06-D080-D097 Compiler Document Coverage Report

Date: 2026-06-25
Tasks: historical scaffold for `P06-D080` through `P06-D097`
Phase: 06 - Compiler Architecture
Status: superseded historical scaffold evidence

## Governing Documents Read

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

## Implemented Surface

- `docs/artifacts/phase-06/fixtures/document-coverage/accepted-compiler-document-coverage.json`
- `docs/artifacts/phase-06/document-coverage/compiler-document-coverage.accepted.json`

## Coverage

The document coverage validator accepts one artifact-backed fixture for each
`C1` through `C18` document and rejects one stable diagnostic fixture for each
document. Required artifact fields are checked per document so coverage remains
grounded in the owning compiler architecture artifact surface. That scaffold is
not current completion evidence for the open Phase 06 document-coverage tasks.

Current Clojure-backed document coverage is recorded in the phase proof report
for `C1` through `C18`. The C11 gate is
`clojure -M:gravity compiler-c11-mir-spec
bootstrap/clojure/fixtures/accepted/compiler-c11-mir-spec.gravity`; it emits
`:gravity/stage0-c11-mir-spec-artifact` with target-independent MIR operations,
operation-family coverage, control/data-flow graphs, type/effect/source-origin
tables, domain-anchor records, runtime-check and safety-outcome tables,
diagnostics, verifier output, conformance results, and capability-based proof.
The C12 gate is `clojure -M:gravity compiler-c12-domain-ir
bootstrap/clojure/fixtures/accepted/compiler-c12-domain-ir.gravity`; it emits
`:gravity/stage0-c12-domain-ir-architecture-artifact` with domain
registrations, domain artifacts, semantic anchors, entry/exit pass records,
verifier output, proof/certificate references, lowering eligibility, fallback
records, plugin policy, domain diagnostics, conformance results, and
capability-based proof.
The C13 gate is `clojure -M:gravity compiler-c13-optimization
bootstrap/clojure/fixtures/accepted/compiler-c13-optimization.gravity`; it
emits `:gravity/stage0-c13-mir-optimization-artifact` with pass contracts,
deterministic pipeline manifest, decision records, invalidation and analysis
cache records, proof/certificate usage, residual cost records, verifier output,
optimized MIR, diagnostics, conformance results, and capability-based proof.
The C14 gate is `clojure -M:gravity compiler-c14-lowering
bootstrap/clojure/fixtures/accepted/compiler-c14-lowering.gravity`; it emits
`:gravity/stage0-c14-target-lowering-artifact` with lowering request
verification, target eligibility, ABI and runtime/provider manifests, provider
selection records, proof-to-target metadata, source/generated-origin mapping,
capability preservation, unsupported-feature handling, target artifacts,
diagnostics, conformance results, and capability-based proof.
The C15 gate is `clojure -M:gravity compiler-c15-diagnostics
bootstrap/clojure/fixtures/accepted/compiler-c15-diagnostics.gravity`; it emits
`:gravity/stage0-c15-compiler-diagnostics-artifact` with diagnostic schema,
deterministic diagnostic stream, catalog rules, related spans, remediation and
quick fixes, redaction, rendering records, golden fixtures, conformance results,
and capability-based proof.
The C16 gate is `clojure -M:gravity compiler-c16-incremental
bootstrap/clojure/fixtures/accepted/compiler-c16-incremental.gravity`; it emits
`:gravity/stage0-c16-incremental-compilation-artifact` with an incremental
dependency graph, cache key schema, cache entries, invalidation trace,
revalidation reports, stale-proof and stale-diagnostic rejection, replay,
speculative reuse boundaries, reproducible rebuild evidence, diagnostics,
conformance results, and capability-based proof.
The C17 gate is `clojure -M:gravity compiler-c17-plugin
bootstrap/clojure/fixtures/accepted/compiler-c17-plugin.gravity`; it emits
`:gravity/stage0-c17-compiler-plugin-artifact` with a plugin manifest, API
compatibility report, sandbox and trusted-package grants, hermetic
build-effect denial, pass registration records, domain and facet registration
records, plugin cache keys, plugin output artifacts, plugin execution traces,
diagnostics, conformance results, and capability-based proof.
The C18 gate is `clojure -M:gravity compiler-c18-verification
bootstrap/clojure/fixtures/accepted/compiler-c18-verification.gravity`; it
emits `:gravity/stage0-c18-compiler-verification-artifact` with a compiler
verification plan, pass risk classification, pass evidence records, stage
verifier reports, translation validation logs, proof and certificate
references, differential and property fixture results, compiler trust report,
release gate report, counterexample regression, plugin evidence, backend
conformance, diagnostics, conformance results, and capability-based proof.

## Residual Risks

The historical document coverage artifact is review context. Current
capability-backed Phase 06 status is recorded in
`docs/artifacts/phase-06/reports/phase-06-proof-report.md`.
