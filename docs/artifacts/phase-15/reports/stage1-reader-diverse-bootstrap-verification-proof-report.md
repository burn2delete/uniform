# Stage1 Reader Diverse Bootstrap Verification Proof Report

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
clojure -M:gravity stage1-reader-diverse-bootstrap-verification bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

## Artifact Summary

- artifact kind: `:gravity/stage1-reader-diverse-bootstrap-verification-artifact`
- artifact id: `sha256:beb7e151aecfcbbb46f55ab188842540417bf1313b6ebedd2d0015c5210abcdc`
- diverse bootstrap verification id: `sha256:489b067157bf4368c0681230b6112340ad19980f318de881946375866f2b69f6`
- verified boot-chain id: `sha256:89513b69a44a03ae95a3f029d22618022d87327ebf812fe71e2570daa95ab9ea`
- entrypoint: `stage1-read-source-diverse-bootstrap-verification`
- host primitives: `[]`
- seed builtin fallbacks: `[]`
- seed orchestration fallbacks: `[]`
- runner fallbacks: `[]`
- OS boundaries: `[]`
- machine boundaries: `[]`
- trust-anchor boundaries: `[]`
- image fallbacks: `[]`
- boot-chain fallbacks: `[]`
- diverse verification fallbacks: `[]`
- replaced trust-anchor boundaries: `[:hardware-reset-vector :firmware-root-of-trust :external-auditor-key]`
- residual trust boundaries: `[:physical-device-manufacturing :supply-chain-custody :independent-diversity-review]`
- Gravity runtimes: `[:stage1-reader-self-hosted-runtime :stage1-reader-source-runtime :stage1-reader-core-bootstrap-runtime :stage1-reader-compiler-driver :stage1-reader-runtime-entrypoint :stage1-reader-runtime-image :stage1-reader-verified-boot-chain :stage1-reader-diverse-bootstrap-verification]`
- Gravity executors: `[:stage1-reader-token-automaton-executor :stage1-reader-form-builder-executor]`
- accepted fixture character records: `506`
- accepted fixture token records: `82`
- accepted fixture top-level forms: `4`
- diagnostic records: `14`

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
 :hardware-reset-vector-boundary? false
 :firmware-root-of-trust-boundary? false
 :external-auditor-key-boundary? false
 :physical-device-manufacturing-boundary? true
 :supply-chain-custody-boundary? true
 :independent-diversity-review-boundary? true}
```

The command proves that seed-built rebuild, self-built rebuild, clean-environment
rebuild, diverse-toolchain rebuild, bootstrap trace comparison, provenance
verification, and independent audit recording are described by the
Gravity-authored `stage1-reader-diverse-bootstrap-verification` record. The
bridge preserves source spans, token coverage, form coverage, artifact
provenance, verified-boot-chain routing, runtime-image routing,
runtime-entrypoint routing, compiler-driver routing, core-bootstrap builtin
coverage, and stage0 form parity.

The proof does not claim the Clojure seed is retired. The remaining trusted
boundaries are `:physical-device-manufacturing-boundary? true`,
`:supply-chain-custody-boundary? true`, and
`:independent-diversity-review-boundary? true`. The next required capability is
`:replace-physical-supply-chain-and-independent-diversity-assumptions-with-release-attestation-and-seed-retirement`.

## Rejected Behavior

The proof artifact records stable diagnostics for malformed reader fixtures and
diverse bootstrap verification internal failure modes:

- `STAGE1DIV001` missing diverse bootstrap verification entrypoint
- `STAGE1DIV002` unsupported diverse bootstrap verification operation
- `STAGE1DIV003` missing diverse bootstrap verification record
- `STAGE1DIV004` single implementation self-certification
- `STAGE1DIV005` divergent bootstrap trace
- `STAGE1DIV006` unreproducible diverse build provenance
- `STAGE1DIV007` missing independent audit metadata
- `STAGE1DIV008` illegal hardware, firmware, or external trust-anchor fallback
- `STAGE1DIV009` invalid diverse bootstrap verification record
- `STAGE1READER001` through `STAGE1READER005` malformed reader input

## Validation

```text
$ clojure -M:test
Ran 178 tests containing 9494 assertions.
0 failures, 0 errors.

$ clojure -M:gravity stage1-reader-diverse-bootstrap-verification bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
:gravity/stage1-reader-diverse-bootstrap-verification-artifact
```

## Artifacts

- `docs/artifacts/phase-15/bootstrap/stage1-reader-diverse-bootstrap-verification-proof.edn`
- `docs/artifacts/phase-15/reports/stage1-reader-diverse-bootstrap-verification-proof-report.md`
