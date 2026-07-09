# P02-D032 SAFE3 Document Coverage Report

Date: 2026-06-24
Agent: Codex
Task: `P02-D032`
Status: complete (stage0 SAFE3 capability)

SAFE3 is covered by `bootstrap/clojure/fixtures/accepted/memory-safety.gravity`
and `docs/artifacts/phase-02/memory-safety/stage0-safe3-document-coverage-proof.edn`.
The stage0 artifact records ownership graph, borrow graph, lifetime interval,
ownership transfer, and runtime borrow-check evidence.

Rejected fixtures cover all SAFE3 diagnostics: `SAFE3-USE-AFTER-MOVE`,
`SAFE3-USE-AFTER-CONSUME`, `SAFE3-BORROW-ESCAPE`, `SAFE3-MUT-ALIAS`,
`SAFE3-MOVE-WHILE-BORROWED`, `SAFE3-CONSUME-WHILE-BORROWED`,
`SAFE3-LIFETIME`, `SAFE3-TASK-CAPTURE`, `SAFE3-FFI-OWNERSHIP`,
`SAFE3-RUNTIME-CHECK`, and `SAFE3-UNSAFE-ALIAS`.

Validation: `clojure -M:test` passed 28 tests, 1493 assertions, and 337
rejected fixtures.
