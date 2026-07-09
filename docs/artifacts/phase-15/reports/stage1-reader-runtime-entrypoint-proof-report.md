# Stage1 Reader Runtime Entrypoint Proof Report

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
- `docs/phase-12-build-package-and-artifact-system/171-pkg7-reproducible-build-specification.md`
- `docs/phase-14-testing-verification-and-conformance/202-test13-self-hosting-validation-plan.md`

## Capability Command

```text
clojure -M:gravity stage1-reader-runtime-entrypoint bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

## Artifact Summary

- artifact kind: `:gravity/stage1-reader-runtime-entrypoint-artifact`
- artifact id: `sha256:6608bdaeb1277edf6c6a2ec3adb59baf6c176f14c3107bd1bdf00edfb04af9d0`
- runtime entrypoint artifact id: `sha256:49c3f2252fe77d94f95826125d456eedd62e4eca5cabfd79c034b8e9c323f5b8`
- runtime entrypoint id: `sha256:b47468695d02f9e7408ad965a45120e5eda8121a6bc95496c672051436e62b8d`
- compiler driver id: `sha256:f2ca47953c5bee40d670a804f9f87a9406821458c29092ac2b4bf34d3911333a`
- entrypoint: `stage1-read-source-runtime-entrypoint`
- host primitives: `[]`
- seed builtin fallbacks: `[]`
- seed orchestration fallbacks: `[]`
- runner fallbacks: `[]`
- OS boundaries: `[:os-process-launch :os-filesystem-read :stdout-stream]`
- Gravity runtimes: `[:stage1-reader-self-hosted-runtime :stage1-reader-source-runtime :stage1-reader-core-bootstrap-runtime :stage1-reader-compiler-driver :stage1-reader-runtime-entrypoint]`
- Gravity executors: `[:stage1-reader-token-automaton-executor :stage1-reader-form-builder-executor]`
- accepted fixture character records: `506`
- accepted fixture token records: `82`
- accepted fixture top-level forms: `4`
- diagnostic records: `13`

## Proof Claims

The proof records these trusted-boundary facts for the claimed bridge:

```clojure
{:clojure-runtime-interpreter? false
 :clojure-instruction-executor? false
 :clojure-binary-runner? false
 :clojure-character-stream-implementation? false
 :clojure-seed-builtins? false
 :clojure-seed-orchestration? false
 :clojure-driver-runner? false
 :host-command-invocation? false
 :host-file-read? false
 :os-process-boundary? true
 :os-filesystem-read-boundary? true
 :stdout-boundary? true}
```

The command proves that command decoding, source opening, source-byte delivery,
compiler-driver execution, artifact output routing, process-exit mapping, and
OS boundary recording are described by the Gravity-authored
`stage1-reader-runtime-entrypoint` record. The bridge preserves source spans,
token coverage, form coverage, artifact provenance, compiler-driver routing,
core-bootstrap builtin coverage, and stage0 form parity.

The proof does not claim the Clojure seed is retired. The remaining trusted
boundaries are `:os-process-boundary? true`,
`:os-filesystem-read-boundary? true`, and `:stdout-boundary? true`. The next
required capability is
`:replace-os-process-filesystem-and-stdout-boundaries-with-bootstrapped-runtime-image`.

## Rejected Behavior

The proof artifact records stable diagnostics for malformed reader fixtures
and runtime-entrypoint internal failure modes:

- `STAGE1RTE001` missing runtime entrypoint
- `STAGE1RTE002` unsupported runtime-entrypoint operation
- `STAGE1RTE003` missing runtime-entrypoint record
- `STAGE1RTE004` source routing divergence
- `STAGE1RTE005` artifact output divergence
- `STAGE1RTE006` process exit divergence
- `STAGE1RTE007` illegal runner fallback
- `STAGE1RTE008` invalid runtime-entrypoint record
- `STAGE1READER001` through `STAGE1READER005` malformed reader input

## Validation

```text
$ clojure -M:test
Ran 172 tests containing 9236 assertions.
0 failures, 0 errors.

$ clojure -M:gravity stage1-reader-runtime-entrypoint bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
:gravity/stage1-reader-runtime-entrypoint-artifact
```

## Artifacts

- `docs/artifacts/phase-15/bootstrap/stage1-reader-runtime-entrypoint-proof.edn`
- `docs/artifacts/phase-15/reports/stage1-reader-runtime-entrypoint-proof-report.md`

