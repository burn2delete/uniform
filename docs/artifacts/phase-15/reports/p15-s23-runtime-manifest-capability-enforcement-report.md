# P15-S23 Runtime Manifest And Capability Enforcement Report

Date: 2026-06-30

Task: `P15-S23`

Status: in progress; runtime manifest and capability enforcement proof active

Artifact: `docs/artifacts/phase-15/bootstrap/p15-s23-runtime-manifest-capability-enforcement.edn`

Artifact id: `sha256:3d5d27620cf4f20d5fc4e419997ffbdfa4bef591b4ed179cb9e5107cc9333700`

Proof id: `sha256:bb52f3896eb06391efae9085f0a01056b717a042d51a553f6625d7390ed07216`

## Capability Proven

`bootstrap/gravity/p15_s23/compiler.gravity` now contains a Gravity-authored
runtime manifest and capability enforcement report. The verifier emits
`:gravity/p15-s23-runtime-manifest-capability-enforcement-artifact`, links it
to the P15-S23 core lowering/diagnostic preservation proof and compiler
pipeline manifest, selects an explicit managed runtime family, classifies
linked/generated/delegated/external/forbidden services, and builds the runtime
capability manifest consumed by the P15-S23 gate.

The proof records 16 authority-family decisions across filesystem, network,
database, environment, process, shell, secrets, FFI, raw memory, model, tool,
memory, human-review, observability, deployment, and package mutation actions.
It demonstrates deny-by-default enforcement, grant/deny/delegate/revoke
coverage, scoped delegated handles, revocation records, principal identity,
decision audit logs, denial diagnostics, and redaction/secret-handling records.

The proof is accepted as P15-S23 evidence for
`:runtime-manifest-and-capability-enforcement-report`. It still records
`:full-language-compiler-self-hosted? false` and `:clojure-seed-retired?
false`.

## Rejected Behavior

The artifact includes internal rejected candidates with stable diagnostics:

- `P15S23R001`: missing runtime manifest/capability enforcement report.
- `P15S23R002`: incomplete runtime manifest or family selection.
- `P15S23R003`: incomplete service classification or hidden runtime service.
- `P15S23R004`: capability enforcement is incomplete or not deny-by-default.
- `P15S23R005`: audit, principal, delegation, revocation, or redaction evidence is incomplete.
- `P15S23R006`: required compiler artifact links are missing.
- `P15S23R007`: unsupported self-hosting or seed-retirement claim.

## Verification

- `clojure -M:test`: 214 tests, 10509 assertions, 0 failures, 0 errors.
- `clojure -M:gravity p15-s23-runtime-manifest-capability-enforcement bootstrap/gravity/p15_s23/compiler.gravity`: emitted artifact id `sha256:3d5d27620cf4f20d5fc4e419997ffbdfa4bef591b4ed179cb9e5107cc9333700`, proof id `sha256:bb52f3896eb06391efae9085f0a01056b717a042d51a553f6625d7390ed07216`, runtime family `:managed`, 16 authority families, 16 runtime decisions, diagnostics `P15S23R001` through `P15S23R007`, `:full-language-compiler-self-hosted? false`, and `:clojure-seed-retired? false`.
- `clojure -M:gravity p15-s23-whole-language-self-hosting-gate bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`: now records runtime manifest/capability enforcement evidence, accepted app execution evidence, rejected app diagnostic evidence, reproducible rebuild log evidence, stage comparison report evidence, and reports 1 remaining missing evidence category.

## Remaining Phase Work

This proof is not the self-hosted compiler. `P15-S23` still requires a
governance/package evidence,
and actual Clojure seed retirement.
