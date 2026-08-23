# Stage1 Reader Compiler Driver Proof Report

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
clojure -M:gravity stage1-reader-compiler-driver bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

## Artifact Summary

- artifact kind: `:gravity/stage1-reader-compiler-driver-artifact`
- artifact id: `sha256:06e49afe6c4d4cc4b2757d381919bc4183d7c124934ff569504ded77eb0bb5ed`
- compiler driver artifact id: `sha256:4032978afcee6a499ed284c886b09a86df7e3ba77b3ef3bab44e3b9c136ca262`
- compiler driver id: `sha256:f2ca47953c5bee40d670a804f9f87a9406821458c29092ac2b4bf34d3911333a`
- core bootstrap runtime id: `sha256:befba5b87d3d82786473ba17bca09d814b67a6c21d2a09be362d0b9193f6ba96`
- core bootstrap builtin id: `sha256:daa5373e451dcfc3f34d85be18130faa8fd0c09ea5aa518eecca9e544cc3738c`
- entrypoint: `stage1-read-source-compiler-driver`
- host primitives: `[]`
- seed builtin fallbacks: `[]`
- seed orchestration fallbacks: `[]`
- host command boundaries: `[:host-command-invocation :host-file-read]`
- Gravity runtimes: `[:stage1-reader-self-hosted-runtime :stage1-reader-source-runtime :stage1-reader-core-bootstrap-runtime :stage1-reader-compiler-driver]`
- Gravity executors: `[:stage1-reader-token-automaton-executor :stage1-reader-form-builder-executor]`
- accepted fixture character records: `506`
- accepted fixture token records: `82`
- accepted fixture top-level forms: `4`
- diagnostic records: `12`

## Proof Claims

The proof records these trusted-boundary facts for the claimed bridge:

```clojure
{:clojure-runtime-interpreter? false
 :clojure-instruction-executor? false
 :clojure-binary-runner? false
 :clojure-character-stream-implementation? false
 :clojure-seed-builtins? false
 :clojure-seed-orchestration? false
 :clojure-driver-runner? true
 :host-command-invocation? true
 :host-file-read? true}
```

The command proves that stage1 source routing, entrypoint resolution, runtime
execution routing, diagnostic stream routing, proof artifact routing, stage0
comparison, and provenance recording are now described by the Gravity-authored
`stage1-reader-compiler-driver` record. The bridge preserves source spans,
token coverage, form coverage, artifact provenance, core-bootstrap builtin
coverage, and stage0 form parity.

The proof does not claim the Clojure seed is retired. The remaining trusted
boundaries are `:clojure-driver-runner? true`, `:host-command-invocation?
true`, and `:host-file-read? true`. The next required capability is
`:replace-clojure-driver-runner-and-host-io-with-gravity-runtime-entrypoint`.

## Rejected Behavior

The proof artifact records stable diagnostics for malformed reader fixtures
and compiler-driver internal failure modes:

- `STAGE1DRV001` missing compiler-driver entrypoint
- `STAGE1DRV002` unsupported compiler-driver operation
- `STAGE1DRV003` missing compiler-driver record
- `STAGE1DRV004` artifact routing divergence
- `STAGE1DRV005` diagnostic stream divergence
- `STAGE1DRV006` illegal seed orchestration fallback
- `STAGE1DRV007` invalid compiler-driver record
- `STAGE1READER001` through `STAGE1READER005` malformed reader input

## Validation

```text
$ clojure -M:test
Ran 170 tests containing 9161 assertions.
0 failures, 0 errors.

$ clojure -M:gravity stage1-reader-compiler-driver bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
:gravity/stage1-reader-compiler-driver-artifact
```

## Artifacts

- `docs/artifacts/phase-15/bootstrap/stage1-reader-compiler-driver-proof.edn`
- `docs/artifacts/phase-15/reports/stage1-reader-compiler-driver-proof-report.md`
