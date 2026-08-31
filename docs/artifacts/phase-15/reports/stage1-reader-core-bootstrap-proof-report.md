# Stage1 Reader Core Bootstrap Proof Report

Date: 2026-06-30
Agent: Codex

## Governing Documents Read

- `docs/phase-15-bootstrap-and-self-hosting/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-15-bootstrap-and-self-hosting/203-boot1-bootstrap-strategy.md`
- `docs/phase-15-bootstrap-and-self-hosting/205-boot3-self-hosted-compiler-plan.md`
- `docs/phase-15-bootstrap-and-self-hosting/209-boot7-self-hosting-validation-and-equivalence-plan.md`
- `docs/phase-15-bootstrap-and-self-hosting/210-boot8-bootstrap-artifact-provenance-specification.md`
- `docs/phase-06-compiler-architecture/080-c1-compiler-architecture-overview.md`
- `docs/phase-06-compiler-architecture/081-c2-reader-implementation-design.md`
- `docs/phase-06-compiler-architecture/082-c3-syntax-object-model.md`
- `docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md`
- `docs/phase-06-compiler-architecture/094-c15-compiler-diagnostics-specification.md`
- `docs/phase-08-runtime-architecture/112-r1-runtime-architecture-overview.md`
- `docs/phase-08-runtime-architecture/122-r11-runtime-capability-enforcement-design.md`
- `docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md`
- `docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md`

## Capability Command

```text
clojure -M:gravity stage1-reader-core-bootstrap bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

## Artifact Summary

- artifact kind: `:gravity/stage1-reader-core-bootstrap-artifact`
- artifact id: `sha256:13735aef5b2b86de76d2d74c8c145228b4ea47312ef0f4e59dc44bbf2b062af6`
- core bootstrap artifact id: `sha256:2d4471b874eb9fdff3cfc65891d21f2b70485251240e4aa6acd87cf9bee74acb`
- core bootstrap runtime id: `sha256:befba5b87d3d82786473ba17bca09d814b67a6c21d2a09be362d0b9193f6ba96`
- core bootstrap builtin id: `sha256:daa5373e451dcfc3f34d85be18130faa8fd0c09ea5aa518eecca9e544cc3738c`
- entrypoint: `stage1-read-source-core-bootstrap`
- host primitives: `[]`
- seed builtin fallbacks: `[]`
- Gravity runtimes: `[:stage1-reader-self-hosted-runtime :stage1-reader-source-runtime :stage1-reader-core-bootstrap-runtime]`
- Gravity executors: `[:stage1-reader-token-automaton-executor :stage1-reader-form-builder-executor]`
- accepted fixture character records: `506`
- accepted fixture token records: `82`
- accepted fixture top-level forms: `4`
- diagnostic records: `11`

## Proof Claims

The proof records these trusted-boundary facts for the claimed bridge:

```clojure
{:clojure-runtime-interpreter? false
 :clojure-instruction-executor? false
 :clojure-binary-runner? false
 :clojure-character-stream-implementation? false
 :clojure-seed-builtins? false
 :clojure-seed-orchestration? true}
```

The command proves that the latest reader bridge now uses the
Gravity-authored `stage1-reader-core-bootstrap-builtins` record for source
character stream, token classification, token realization, token automaton
execution, form builder execution, stage0 parity, diagnostic stream, and
artifact provenance operations. The bridge preserves accepted fixture source
spans, token coverage, form coverage, artifact provenance, and stage0 form
parity.

The proof does not claim the Clojure seed is retired. The remaining trusted
boundary is `:clojure-seed-orchestration? true`, and the next required
capability is
`:replace-clojure-seed-orchestration-with-gravity-compiler-driver`.

## Rejected Behavior

The proof artifact records stable diagnostics for malformed reader fixtures
and core-bootstrap internal failure modes:

- `STAGE1CORE001` missing core-bootstrap entrypoint
- `STAGE1CORE002` unsupported core-bootstrap builtin operation
- `STAGE1CORE003` missing core-bootstrap builtin record
- `STAGE1CORE004` builtin/runtime divergence
- `STAGE1CORE005` illegal host fallback
- `STAGE1CORE006` invalid core-bootstrap runtime
- `STAGE1READER001` through `STAGE1READER005` malformed reader input

## Validation

```text
$ clojure -M:test
Ran 168 tests containing 9086 assertions.
0 failures, 0 errors.

$ clojure -M:gravity stage1-reader-core-bootstrap bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
:gravity/stage1-reader-core-bootstrap-artifact
```

## Artifacts

- `docs/artifacts/phase-15/bootstrap/stage1-reader-core-bootstrap-proof.edn`
- `docs/artifacts/phase-15/reports/stage1-reader-core-bootstrap-proof-report.md`
