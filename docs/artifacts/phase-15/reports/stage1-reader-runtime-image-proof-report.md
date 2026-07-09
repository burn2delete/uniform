# Stage1 Reader Runtime Image Proof Report

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
clojure -M:gravity stage1-reader-runtime-image bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

## Artifact Summary

- artifact kind: `:gravity/stage1-reader-runtime-image-artifact`
- artifact id: `sha256:64ac61cc75110479f83338aa68eccae7852f9c904bb9c8d2946362444504f736`
- runtime image artifact id: `sha256:00845bb29fa56663733b750bff275e73bf739ac881c64c299fa39a4a309c7e90`
- runtime image id: `sha256:48f1b93ed00075759a23a16a4874c6b5af94b0ea93a1800fc11ccd686168ade1`
- runtime entrypoint id: `sha256:b47468695d02f9e7408ad965a45120e5eda8121a6bc95496c672051436e62b8d`
- compiler driver id: `sha256:f2ca47953c5bee40d670a804f9f87a9406821458c29092ac2b4bf34d3911333a`
- entrypoint: `stage1-read-source-runtime-image`
- host primitives: `[]`
- seed builtin fallbacks: `[]`
- seed orchestration fallbacks: `[]`
- runner fallbacks: `[]`
- OS boundaries: `[]`
- replaced OS boundaries: `[:os-process-launch :os-filesystem-read :stdout-stream]`
- machine boundaries: `[:machine-instruction-dispatch :kernel-process-scheduler :artifact-loader]`
- image fallbacks: `[]`
- Gravity runtimes: `[:stage1-reader-self-hosted-runtime :stage1-reader-source-runtime :stage1-reader-core-bootstrap-runtime :stage1-reader-compiler-driver :stage1-reader-runtime-entrypoint :stage1-reader-runtime-image]`
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
 :os-process-boundary? false
 :os-filesystem-read-boundary? false
 :stdout-boundary? false
 :machine-boundary? true
 :kernel-process-scheduler-boundary? true
 :artifact-loader-boundary? true}
```

The command proves that runtime image loading, runtime entrypoint installation,
source mounting, runtime-entrypoint execution, stdout routing, runtime artifact
emission, and machine-boundary recording are described by the Gravity-authored
`stage1-reader-runtime-image` record. The bridge preserves source spans, token
coverage, form coverage, artifact provenance, runtime-entrypoint routing,
compiler-driver routing, core-bootstrap builtin coverage, and stage0 form
parity.

The proof does not claim the Clojure seed is retired. The remaining trusted
boundaries are `:machine-boundary? true`,
`:kernel-process-scheduler-boundary? true`, and
`:artifact-loader-boundary? true`. The next required capability is
`:replace-machine-kernel-and-artifact-loader-boundaries-with-verified-boot-chain`.

## Rejected Behavior

The proof artifact records stable diagnostics for malformed reader fixtures
and runtime-image internal failure modes:

- `STAGE1IMG001` missing runtime image entrypoint
- `STAGE1IMG002` unsupported runtime image operation
- `STAGE1IMG003` missing runtime image record
- `STAGE1IMG004` filesystem authority divergence
- `STAGE1IMG005` stdout routing divergence
- `STAGE1IMG006` runtime image provenance gap
- `STAGE1IMG007` illegal OS boundary fallback
- `STAGE1IMG008` invalid runtime image record
- `STAGE1READER001` through `STAGE1READER005` malformed reader input

## Validation

```text
$ clojure -M:test
Ran 174 tests containing 9313 assertions.
0 failures, 0 errors.

$ clojure -M:gravity stage1-reader-runtime-image bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
:gravity/stage1-reader-runtime-image-artifact
```

## Artifacts

- `docs/artifacts/phase-15/bootstrap/stage1-reader-runtime-image-proof.edn`
- `docs/artifacts/phase-15/reports/stage1-reader-runtime-image-proof-report.md`
