# Stage1 Reader Verified Boot-Chain Proof Report

Date: 2026-06-30
Agent: Codex

## Governing Documents Read

- `docs/phase-15-bootstrap-and-self-hosting/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-15-bootstrap-and-self-hosting/203-boot1-bootstrap-strategy.md`
- `docs/phase-15-bootstrap-and-self-hosting/205-boot3-self-hosted-compiler-plan.md`
- `docs/phase-15-bootstrap-and-self-hosting/208-boot6-trusting-trust-and-reproducible-bootstrap-plan.md`
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
clojure -M:gravity stage1-reader-verified-boot-chain bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

## Artifact Summary

- artifact kind: `:gravity/stage1-reader-verified-boot-chain-artifact`
- artifact id: `sha256:e67dab6bfac0e81a6171a8ae4704f0f7fe1303f8de6eb1d187744c3c7fedc341`
- verified boot-chain artifact id: `sha256:89a0c8859fdedff3857e7c82a426791cb75a36cf8663c981cf8cdaf4c08bd450`
- verified boot-chain id: `sha256:89513b69a44a03ae95a3f029d22618022d87327ebf812fe71e2570daa95ab9ea`
- runtime image id: `sha256:48f1b93ed00075759a23a16a4874c6b5af94b0ea93a1800fc11ccd686168ade1`
- runtime entrypoint id: `sha256:b47468695d02f9e7408ad965a45120e5eda8121a6bc95496c672051436e62b8d`
- compiler driver id: `sha256:f2ca47953c5bee40d670a804f9f87a9406821458c29092ac2b4bf34d3911333a`
- entrypoint: `stage1-read-source-verified-boot-chain`
- host primitives: `[]`
- seed builtin fallbacks: `[]`
- seed orchestration fallbacks: `[]`
- runner fallbacks: `[]`
- OS boundaries: `[]`
- machine boundaries: `[]`
- replaced machine boundaries: `[:machine-instruction-dispatch :kernel-process-scheduler :artifact-loader]`
- trust-anchor boundaries: `[:hardware-reset-vector :firmware-root-of-trust :external-auditor-key]`
- image fallbacks: `[]`
- boot-chain fallbacks: `[]`
- Gravity runtimes: `[:stage1-reader-self-hosted-runtime :stage1-reader-source-runtime :stage1-reader-core-bootstrap-runtime :stage1-reader-compiler-driver :stage1-reader-runtime-entrypoint :stage1-reader-runtime-image :stage1-reader-verified-boot-chain]`
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
 :machine-boundary? false
 :kernel-process-scheduler-boundary? false
 :artifact-loader-boundary? false
 :hardware-reset-vector-boundary? true
 :firmware-root-of-trust-boundary? true
 :external-auditor-key-boundary? true}
```

The command proves that boot-chain verification, runtime image loading, runtime
image activation, machine instruction dispatch, kernel process scheduling,
artifact loading, and trust-anchor recording are described by the
Gravity-authored `stage1-reader-verified-boot-chain` record. The bridge
preserves source spans, token coverage, form coverage, artifact provenance,
runtime-image routing, runtime-entrypoint routing, compiler-driver routing,
core-bootstrap builtin coverage, and stage0 form parity.

The proof does not claim the Clojure seed is retired. The remaining trusted
boundaries are `:hardware-reset-vector-boundary? true`,
`:firmware-root-of-trust-boundary? true`, and
`:external-auditor-key-boundary? true`. The next required capability is
`:replace-hardware-firmware-and-external-trust-anchors-with-diverse-self-hosted-bootstrap-verification`.

## Rejected Behavior

The proof artifact records stable diagnostics for malformed reader fixtures
and verified boot-chain internal failure modes:

- `STAGE1BOOT001` missing verified boot-chain entrypoint
- `STAGE1BOOT002` unsupported boot operation
- `STAGE1BOOT003` missing verified boot-chain record
- `STAGE1BOOT004` artifact-loader divergence
- `STAGE1BOOT005` scheduler authority divergence
- `STAGE1BOOT006` unreproducible boot provenance
- `STAGE1BOOT007` illegal machine or kernel fallback
- `STAGE1BOOT008` invalid verified boot-chain record
- `STAGE1READER001` through `STAGE1READER005` malformed reader input

## Validation

```text
$ clojure -M:test
Ran 176 tests containing 9394 assertions.
0 failures, 0 errors.

$ clojure -M:gravity stage1-reader-verified-boot-chain bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
:gravity/stage1-reader-verified-boot-chain-artifact
```

## Artifacts

- `docs/artifacts/phase-15/bootstrap/stage1-reader-verified-boot-chain-proof.edn`
- `docs/artifacts/phase-15/reports/stage1-reader-verified-boot-chain-proof-report.md`
