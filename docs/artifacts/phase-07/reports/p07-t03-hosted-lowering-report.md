# P07-T03 Hosted Wasm/JVM/JS-TS Lowering Proof Report

Date: 2026-06-25
Task: `P07-T03`
Status: complete (stage0 hosted Wasm/JVM/JS-TS lowering capability)

## Governing Documents Read

- `docs/phase-07-backend-architecture/101-b4-wasm-backend-design.md`
- `docs/phase-07-backend-architecture/102-b5-jvm-backend-design.md`
- `docs/phase-07-backend-architecture/103-b6-javascript-typescript-backend-design.md`
- `docs/phase-07-backend-architecture/110-b13-artifact-emission-specification.md`
- `docs/phase-07-backend-architecture/111-b14-backend-conformance-test-plan.md`
- `docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md`

## Implemented Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity`
- `bootstrap/clojure/fixtures/rejected/backend-b4-*.gravity`
- `bootstrap/clojure/fixtures/rejected/backend-b5-*.gravity`
- `bootstrap/clojure/fixtures/rejected/backend-b6-*.gravity`
- `docs/artifacts/phase-07/backend/stage0-p07-t03-hosted-lowering-proof.edn`

The `hosted-lowering` command emits
`:gravity/stage0-hosted-lowering-artifact` from the current P07-T01 backend
interface artifact. It records target-lowering manifests for Wasm, JVM, and
JS/TS; Wasm component/ABI/import/export/host-schema/async/replay records; JVM
class/JAR/interop/nullability/exception/reflection/runtime/native-image
records; JS module, TypeScript declarations, source map, capability, package,
async, nullish/exception, numeric, and UI metadata records; B13 artifact
manifests; an artifact graph; metadata preservation; backend conformance;
diagnostics; and capability-based proof.

## Validation

```text
clojure -M:gravity hosted-lowering bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-hosted-lowering-artifact,
 :task "P07-T03",
 :status :complete,
 :targets [:gravity.backend/wasm :gravity.backend/jvm :gravity.backend/js-ts],
 :artifact-manifests 3,
 :diagnostics 36,
 :negative-diagnostic-results 36,
 :proof :complete}
```

Artifact hash:

```text
sha256:34c45b4dfdbb2368b351b1117cf9d56b34c8a8728fa151c2b018e1656fa7c239
```

```text
clojure -M:test
Ran 73 tests containing 4064 assertions.
0 failures, 0 errors.
```

```text
clojure -M -e <phase-07 backend EDN parse>
parsed 3 phase-07 backend EDN proof files
```

```text
git diff --check
passed with no output
```

## Rejected Diagnostics

The rejected fixture suite covers all current P07-T03 hosted lowering
diagnostic IDs:

- `B4-TARGET`, `B4-COMPONENT`, `B4-CANONICAL-ABI`, `B4-IMPORT`, `B4-EXPORT`, `B4-MEMORY`, `B4-BOUNDS`, `B4-NONDETERMINISM`, `B4-ASYNC`, `B4-WASI-ASYNC`, `B4-SIMD`, `B4-ATOMIC`, `B4-HOST-SCHEMA`, `B4-MANIFEST`
- `B5-TARGET`, `B5-NULL`, `B5-EXCEPTION`, `B5-REFLECTION`, `B5-CLASSLOADING`, `B5-INTEROP`, `B5-RESOURCE`, `B5-THREAD`, `B5-NATIVE-IMAGE`, `B5-PROFILE`, `B5-MANIFEST`
- `B6-TARGET`, `B6-GLOBAL`, `B6-IMPORT`, `B6-NULLISH`, `B6-EXCEPTION`, `B6-NUMERIC`, `B6-EVAL`, `B6-PROTOTYPE`, `B6-ASYNC`, `B6-UI`, `B6-MANIFEST`

## Proof Records

- `docs/artifacts/phase-07/backend/stage0-p07-t03-hosted-lowering-proof.edn`

## Remaining Limits

This completes `P07-T03` for deterministic Clojure stage0 emission of hosted
Wasm, JVM, and JS/TS target artifacts plus host-boundary manifests. It does not
claim external Wasm, JVM, JavaScript, bundler, or browser execution, production
hosted backend stabilization, release readiness, or full backend conformance
coverage.
