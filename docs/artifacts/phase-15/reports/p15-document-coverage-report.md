# Phase 15 Document Coverage Report

Date: 2026-06-29
Agent: Codex

## Scope

This report covers `P15-D203` through `P15-D210`.

## Implementation Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/bootstrap-self-hosting.gravity`
- `bootstrap/clojure/fixtures/rejected/bootstrap-boot*.gravity`
- `docs/artifacts/phase-15/bootstrap/stage0-p15-bootstrap-self-hosting-proof.edn`

## Coverage Summary

| Task | Document | Accepted evidence | Rejected diagnostic |
| --- | --- | --- | --- |
| `P15-D203` | `BOOT1` | stage manifests, conformance reports, TCB deltas, locked dependencies, compiler lineage | `BOOT1001` |
| `P15-D204` | `BOOT2` | Clojure seed compiler subset, exclusions, stable diagnostics, provenance, comparison metadata | `BOOT2002` |
| `P15-D205` | `BOOT3` | module migration manifests, meta profile, equivalence reports, diagnostic compatibility, TCB deltas | `BOOT3002` |
| `P15-D206` | `BOOT4` | compiler effects, capabilities, pass preservation, deterministic output, unsafe audit | `BOOT4003` |
| `P15-D207` | `BOOT5` | versioned stage compatibility matrix, explicit support, profile/backend reports, gap review | `BOOT5003` |
| `P15-D208` | `BOOT6` | controlled environment, rebuild comparison, diverse rebuild identity, accepted deltas, revocation | `BOOT6001` |
| `P15-D209` | `BOOT7` | compiler identities, comparison modes, diagnostic equivalence, accepted deltas, release decision | `BOOT7001` |
| `P15-D210` | `BOOT8` | source/compiler/lockfile/build/environment hashes, evidence links, canonicalization, auditor lineage | `BOOT8002` |

## Validation

```text
$ clojure -M:test
Ran 115 tests containing 7571 assertions.
0 failures, 0 errors.

$ clojure -M:gravity bootstrap-self-hosting bootstrap/clojure/fixtures/accepted/bootstrap-self-hosting.gravity > docs/artifacts/phase-15/bootstrap/stage0-p15-bootstrap-self-hosting-proof.edn
```

The persisted artifact parses as `:gravity/stage0-bootstrap-self-hosting-artifact` with 8 documents, 55 diagnostics, and capability proof status `:complete`.

## Residual Risks

The document coverage artifact proves every BOOT1 through BOOT8 document has accepted evidence, rejected diagnostics, and task coverage in the stage0 Clojure bootstrap. It does not replace the future executable self-hosted compiler or release bootstrap chain.
