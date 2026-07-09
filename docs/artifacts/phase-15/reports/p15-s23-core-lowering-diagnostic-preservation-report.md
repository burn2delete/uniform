# P15-S23 Core Lowering And Diagnostic Preservation Report

Date: 2026-06-30

Task: `P15-S23`

Status: in progress; core lowering and diagnostic preservation proof active

Artifact: `docs/artifacts/phase-15/bootstrap/p15-s23-core-lowering-diagnostic-preservation.edn`

Artifact id: `sha256:de1265a2f5dcacd231fd404ba20aea4b0736ad0e1b036055ae74d9529d423cba`

Proof id: `sha256:1da4fce97ba0f9412f508fe7f8550d297d776317bae8157c5e75c99407b59f69`

C6 artifact id: `sha256:250ff982a510fb41ed73f11da7bc9bd878181c50214ceda280c894b3ce7d4956`

C15 artifact id: `sha256:965d7140c68fda8fe1b2795a63749dc07bb18972d1327af27a5cff0a547977d4`

## Capability Proven

`bootstrap/gravity/p15_s23/compiler.gravity` now contains a Gravity-authored
core lowering and diagnostic preservation report. The verifier emits
`:gravity/p15-s23-core-lowering-diagnostic-preservation-artifact`, links it to
the P15-S23 source/syntax proof and compiler pipeline manifest, builds focused
C6 core-lowering evidence from the verified syntax stream, and builds focused
C15 diagnostic preservation evidence over the lowered core nodes.

The proof records 18 core nodes, a surface-to-core map, evaluation-order records,
a passed core verifier report, structured diagnostic preservation records,
stable diagnostic ids, source span preservation, syntax identity preservation,
origin-chain preservation, and remediation preservation.

The proof is accepted as P15-S23 evidence for
`:core-lowering-and-diagnostic-preservation-report`. It still records
`:full-language-compiler-self-hosted? false` and `:clojure-seed-retired?
false`.

## Rejected Behavior

The artifact includes internal rejected candidates with stable diagnostics:

- `P15S23D001`: missing core lowering and diagnostic preservation report.
- `P15S23D002`: core lowering evidence does not preserve required facts.
- `P15S23D003`: diagnostic preservation evidence is incomplete or unstable.
- `P15S23D004`: required source/syntax, pipeline, C6, or C15 artifact link is missing.
- `P15S23D005`: unsupported self-hosting or seed-retirement claim.

## Verification

- `clojure -M:test`: 214 tests, 10509 assertions, 0 failures, 0 errors.
- `clojure -M:gravity p15-s23-core-lowering-diagnostic-preservation bootstrap/gravity/p15_s23/compiler.gravity`: emitted artifact id `sha256:de1265a2f5dcacd231fd404ba20aea4b0736ad0e1b036055ae74d9529d423cba`, proof id `sha256:1da4fce97ba0f9412f508fe7f8550d297d776317bae8157c5e75c99407b59f69`, C6 artifact id `sha256:250ff982a510fb41ed73f11da7bc9bd878181c50214ceda280c894b3ce7d4956`, C15 artifact id `sha256:965d7140c68fda8fe1b2795a63749dc07bb18972d1327af27a5cff0a547977d4`, 18 core nodes, diagnostics `P15S23D001` through `P15S23D005`, `:full-language-compiler-self-hosted? false`, and `:clojure-seed-retired? false`.
- `clojure -M:gravity p15-s23-whole-language-self-hosting-gate bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`: now records core lowering and diagnostic preservation evidence, runtime manifest/capability enforcement evidence, accepted app execution evidence, rejected app diagnostic evidence, reproducible rebuild log evidence, stage comparison report evidence, and reports 1 remaining missing evidence category.

## Remaining Phase Work

This proof is not the self-hosted compiler. `P15-S23` still requires a
governance/package evidence,
and actual Clojure seed retirement.
