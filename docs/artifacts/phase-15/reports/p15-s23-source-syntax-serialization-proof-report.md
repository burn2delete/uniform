# P15-S23 Source/Syntax Serialization Proof Report

Date: 2026-06-30

Task: `P15-S23`

Status: in progress; source/syntax serialization proof active

Artifact: `docs/artifacts/phase-15/bootstrap/p15-s23-source-syntax-serialization-proof.edn`

Artifact id: `sha256:4b4ab937b732d19fd4ddc7014a7c178a771e3f1eaf9287c2766d9b04a7de49e9`

Proof id: `sha256:7e34b95b16512e0a4c26ed22570dc045e33c9b1cd255d835112643aac3681138`

Serialization id: `sha256:d98aa915a8719cbb4c4d31baeff1eef0dc7972992b95af693ef213018305a84f`

## Capability Proven

`bootstrap/gravity/p15_s23/compiler.gravity` now contains a Gravity-authored
source-unit and syntax-object serialization proof. The verifier emits
`:gravity/p15-s23-source-syntax-serialization-proof-artifact`, links it to the
P15-S23 compiler source inventory, builds focused C2 source-unit evidence and
C3 syntax-object evidence for the Gravity compiler source, and proves EDN
round-tripping for the source-unit record, syntax-object summary, syntax ids,
source spans, origin chains, and syntax verification report.

The proof is accepted as P15-S23 evidence for
`:source-unit-and-syntax-serialization-proof`. It still records
`:full-language-compiler-self-hosted? false` and `:clojure-seed-retired?
false`.

## Rejected Behavior

The artifact includes internal rejected candidates with stable diagnostics:

- `P15S23S001`: missing source/syntax proof contract.
- `P15S23S002`: source-unit identity does not round-trip.
- `P15S23S003`: syntax ids, spans, origins, or preservation facts are missing.
- `P15S23S004`: serialization or syntax verification does not round-trip.
- `P15S23S005`: unsupported self-hosting or seed-retirement claim.

## Verification

- `clojure -M:test`: 214 tests, 10509 assertions, 0 failures, 0 errors.
- `clojure -M:gravity p15-s23-source-syntax-serialization-proof bootstrap/gravity/p15_s23/compiler.gravity`: emitted artifact id `sha256:4b4ab937b732d19fd4ddc7014a7c178a771e3f1eaf9287c2766d9b04a7de49e9`, proof id `sha256:7e34b95b16512e0a4c26ed22570dc045e33c9b1cd255d835112643aac3681138`, serialization id `sha256:d98aa915a8719cbb4c4d31baeff1eef0dc7972992b95af693ef213018305a84f`, 18 syntax objects, diagnostics `P15S23S001` through `P15S23S005`, `:full-language-compiler-self-hosted? false`, and `:clojure-seed-retired? false`.
- `clojure -M:gravity p15-s23-whole-language-self-hosting-gate bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`: now records source/syntax serialization evidence, core lowering/diagnostic preservation evidence, runtime manifest/capability enforcement evidence, accepted app execution evidence, rejected app diagnostic evidence, reproducible rebuild log evidence, stage comparison report evidence, and reports 1 remaining missing evidence category.

## Remaining Phase Work

This proof is not the self-hosted compiler. `P15-S23` still requires a
governance/package evidence,
and actual Clojure seed retirement.
