# P15-S23 Stage3 Equivalence Bundle Report

Date: 2026-07-01

Task: `P15-S23`

Status: stage3 equivalence bundle complete; stage3 application execution
complete; final seed retirement incomplete

Artifact:
`docs/artifacts/phase-15/bootstrap/p15-s23-stage3-equivalence-bundle.edn`

Artifact id:
`sha256:421b3e070fff35d83d1e64ec60b990a49865028d8c720e4941fb8c81b9022d2a`

Proof id:
`sha256:339ccbc8b0ef8b68ce0e4e580b0412699b7305a2a1783e1eeb25c5445720630a`

## Capability Proven

`p15-s23-stage3-equivalence-bundle` records a Gravity-authored proof that the
stage3 seedless compiler candidate is equivalent to the current stage for the
claimed implementation subset. It links the accepted application output,
rejected application diagnostics, reproducible rebuild log, stage comparison,
self-hosting conformance report, provenance attestation, trusted-computing-base
delta, and unsafe audit evidence.

The proof records `:stage3-equivalence-bundle-complete? true`,
`:equivalence-proven-against-current-stage? true`, and
`:candidate-compiler-path-uses-clojure-seed? false`. It also records
`:final-self-hosted-application-run? false`, so it keeps
`:full-language-compiler-self-hosted? false` and
`:clojure-seed-retired? false`.

## Evidence

- Accepted application output:
  `core-app\ngravity:19:2\n(:ok 19)\n`.
- Rejected application diagnostics:
  `L2-BUILTIN-ARITY` and `L2-FUNCTION-ARITY`.
- Equivalence diagnostics covered:
  `P15S23AB001` through `P15S23AB008`.
- Linked evidence includes the stage3 seedless compiler candidate,
  reproducible rebuild log, stage comparison report, self-hosting conformance
  report, provenance attestation, TCB delta record, unsafe audit report,
  accepted app execution proof, and rejected app diagnostic proof.
- The refreshed whole-language gate artifact id is
  `sha256:ce1a645e83664091bece7cdb8792d862fb630cf25915ca20bc4795492ef030dd`;
  it records `:stage3-equivalence-bundle-present? true`, records
  `:stage3-self-hosted-application-execution-present? true`, still reports
  missing evidence `[:clojure-seed-retired]`, and points next to
  `:emit_final_seed_retirement_proof`.

## Verification

- `clojure -M:gravity p15-s23-stage3-equivalence-bundle
  bootstrap/gravity/p15_s23/compiler.gravity`: emitted status `:complete`,
  accepted output equivalence, rejected diagnostic equivalence, rebuild and
  conformance evidence, provenance/TCB/unsafe links, and fail-closed
  equivalence diagnostics.
- `clojure -M:test`: 236 tests, 11391 assertions, 0 failures, 0 errors.

## Remaining Work

Do not mark `P15-S23` complete until Gravity emits the final seed-retirement
proof artifact with both
`:full-language-compiler-self-hosted? true` and `:clojure-seed-retired? true`.
