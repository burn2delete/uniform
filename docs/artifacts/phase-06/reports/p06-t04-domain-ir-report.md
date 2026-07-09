# P06-T04 Domain IR Architecture Report

Date: 2026-06-25
Phase: 06 - Compiler Architecture
Task: `P06-T04`
Status: complete (stage0 domain-IR compiler capability)

## Governing Documents Read

- `docs/phase-06-compiler-architecture/README.md`
- `docs/phase-06-compiler-architecture/091-c12-domain-ir-architecture.md`
- `docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md`
- `docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md`
- `docs/phase-01-core-language/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-02-safety/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-03-profile-system/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-05-mathematical-and-elementary-function-system/IMPLEMENTATION-ROADMAP.md`

## Implemented Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/compiler-domain-ir.gravity`
- rejected `compiler-domain-ir-*.gravity` fixtures

The `domain-ir` command emits `:gravity/stage0-domain-ir-artifact`. The
artifact consumes verified MIR and produces a domain IR registry, domain IR
artifact schema, semantic anchor map, entry and exit pass records, domain
verifier report, proof and certificate references, lowering eligibility
matrix, fallback records, plugin registration policy, conformance results, and
capability-based proof.

The current stage0 registry covers EFIR, schema IR, workflow IR, AI agent IR,
query IR, HDL/state-machine IR, UI IR, GPU IR, FFI boundary IR, and
package/artifact graph IR.

## Validation

```text
clojure -M:gravity domain-ir bootstrap/clojure/fixtures/accepted/compiler-domain-ir.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-domain-ir-artifact,
 :pass :domain-ir-registry-and-artifacts,
 :output :domain-ir-registry,
 :status :complete,
 :domains 10,
 :registrations 10,
 :anchors 10,
 :proofs 10,
 :lowering 10,
 :fallbacks 10,
 :proof :complete}
```

Artifact hash:

```text
sha256:34df63f8862f793d88f2d910f2b24f5cc13e5d10e12907107297848099f94dd4
```

```text
clojure -M:test
Ran 56 tests containing 2952 assertions.
0 failures, 0 errors.
```

## Rejected Diagnostics

- `C12-REGISTRATION`
- `C12-ANCHOR`
- `C12-SCHEMA`
- `C12-FACTS`
- `C12-VERIFY`
- `C12-PROOF`
- `C12-LOWERING`
- `C12-FALLBACK`
- `C12-PLUGIN`

## Proof Records

- `docs/artifacts/phase-06/compiler/stage0-p06-t04-domain-ir-proof.edn`

## Remaining Limits

This completes `P06-T04` for the Clojure stage0 domain-IR registry and artifact
boundary only. It does not claim Phase 06 document coverage tasks, release
readiness, backend code generation, or self-hosting.
