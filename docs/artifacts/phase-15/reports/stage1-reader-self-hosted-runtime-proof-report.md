# Stage1 Reader Self-Hosted Runtime Proof Report

Date: 2026-06-30
Agent: Codex
Task: `P15-S14`

## Governing Documents Read

- `docs/phase-15-bootstrap-and-self-hosting/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-15-bootstrap-and-self-hosting/README.md`
- `docs/phase-15-bootstrap-and-self-hosting/203-boot1-bootstrap-strategy.md`
- `docs/phase-15-bootstrap-and-self-hosting/205-boot3-self-hosted-compiler-plan.md`
- `docs/phase-15-bootstrap-and-self-hosting/209-boot7-self-hosting-validation-and-equivalence-plan.md`
- `docs/phase-15-bootstrap-and-self-hosting/210-boot8-bootstrap-artifact-provenance-specification.md`
- `docs/phase-06-compiler-architecture/081-c2-reader-and-parser-architecture.md`
- `docs/phase-06-compiler-architecture/082-c3-syntax-object-model.md`
- `docs/phase-06-compiler-architecture/094-c15-compiler-diagnostics-model.md`
- `docs/phase-08-runtime-architecture/112-r1-runtime-architecture-overview.md`
- `docs/phase-08-runtime-architecture/122-r11-runtime-capability-enforcement.md`
- `docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md`
- `docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md`

## Capability

`stage1-reader-self-hosted-runtime` executes the Gravity-authored
`stage1-reader-self-hosted-runtime` direct runtime record for the
`stage1-read-source-self-hosted-runtime` entrypoint. The bridge replaces the
Clojure binary runner and Clojure character-stream implementation in the
latest stage1 reader path while preserving the explicit Clojure seed builtin
boundary.

```bash
clojure -M:gravity stage1-reader-self-hosted-runtime bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

## Proof Artifact

- Artifact:
  `docs/artifacts/phase-15/bootstrap/stage1-reader-self-hosted-runtime-proof.edn`
- Kind: `:gravity/stage1-reader-self-hosted-runtime-artifact`
- Artifact id:
  `sha256:393b938274d10ad43222a31594d45e219c4c62ffbdff295166abf4e838ecd322`
- Self-hosted runtime id:
  `sha256:3085c700ba66420d397ed5c6178d3b23e191dfe25a49ce7e4d9c0d565749953d`
- Reader self-hosted runtime id:
  `sha256:a501623f5a7a384b00186584199b53ba5d1bf7b08a63bb27780c39808025d5a4`
- Entry point: `stage1-read-source-self-hosted-runtime`
- Host primitives: `[]`
- Gravity runtimes:
  `[:stage1-reader-self-hosted-runtime :stage1-reader-source-runtime]`
- Gravity executors:
  `[:stage1-reader-token-automaton-executor :stage1-reader-form-builder-executor]`
- Character records: 506
- Token records: 82
- Top-level forms: 4
- Capability proof status: `:complete`

## Accepted Fixture Proof

- `bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`

The accepted fixture proves:

- the self-hosted runtime record is authored in Gravity source;
- the bridge records no reader host primitives;
- source-character, token-classifier, token-realizer, token-automaton,
  form-builder, executor, token, form, and source-span coverage are preserved;
- stage1 forms match the stage0 reader output;
- `:clojure-runtime-interpreter?`, `:clojure-instruction-executor?`,
  `:clojure-binary-runner?`, and
  `:clojure-character-stream-implementation?` are all false for this bridge;
- `:clojure-seed-builtins?` remains true and is the next retirement target.

## Rejected Fixture Proof

The bridge preserves malformed-reader diagnostics through the new runtime path:

- `stage1-reader-unexpected-close.gravity` -> `STAGE1READER001`
- `stage1-reader-unclosed-list.gravity` -> `STAGE1READER002`
- `stage1-reader-unclosed-string.gravity` -> `STAGE1READER003`
- `stage1-reader-unsupported-dispatch.gravity` -> `STAGE1READER004`
- `stage1-reader-odd-map.gravity` -> `STAGE1READER005`

The artifact also records self-hosted runtime diagnostics for invalid bridge
definitions:

- `STAGE1SELF001` missing entrypoint
- `STAGE1SELF002` unsupported form
- `STAGE1SELF003` unsupported host primitive
- `STAGE1SELF004` invalid runtime
- `STAGE1SELF005` stage0 divergence

## Validation

```text
$ clojure -M:gravity stage1-reader-self-hosted-runtime bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
:gravity/stage1-reader-self-hosted-runtime-artifact
sha256:393b938274d10ad43222a31594d45e219c4c62ffbdff295166abf4e838ecd322
:complete

$ clojure -M:test
Ran 166 tests containing 9016 assertions.
0 failures, 0 errors.
```

## Conformance Argument

This bridge advances Phase 15 self-hosting by replacing the previously trusted
Clojure binary runner and Clojure character-stream implementation in the
stage1 reader path with a Gravity-authored runtime record and direct stage
plan. It keeps the D9 proof boundary explicit by preserving accepted behavior,
stable rejection diagnostics, artifact provenance, source identity, and the
remaining Clojure seed builtin limitation.

## Residual Risks

This proof does not claim complete self-hosting, a production compiler, or a
fully retired trusted seed. The next required capability is
`:replace-clojure-seed-builtins-with-gravity-core-bootstrap`.
