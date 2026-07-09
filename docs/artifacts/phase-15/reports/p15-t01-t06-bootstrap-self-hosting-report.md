# Phase 15 P15-T01-T06 Bootstrap Self-Hosting Report

Date: 2026-06-29
Agent: Codex

## Governing Documents Read

- `AGENTS.md`
- `README.md`
- `docs/README.md`
- `docs/source-concepts.md`
- `docs/document-sequence.md`
- `docs/implementation-roadmap.md`
- `docs/phase-15-bootstrap-and-self-hosting/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-15-bootstrap-and-self-hosting/README.md`
- `docs/phase-15-bootstrap-and-self-hosting/203-boot1-bootstrap-strategy.md`
- `docs/phase-15-bootstrap-and-self-hosting/204-boot2-seed-compiler-design.md`
- `docs/phase-15-bootstrap-and-self-hosting/205-boot3-self-hosted-compiler-plan.md`
- `docs/phase-15-bootstrap-and-self-hosting/206-boot4-compiler-in-gravity-coding-standard.md`
- `docs/phase-15-bootstrap-and-self-hosting/207-boot5-stage-compatibility-matrix.md`
- `docs/phase-15-bootstrap-and-self-hosting/208-boot6-trusting-trust-and-reproducible-bootstrap-plan.md`
- `docs/phase-15-bootstrap-and-self-hosting/209-boot7-self-hosting-validation-and-equivalence-plan.md`
- `docs/phase-15-bootstrap-and-self-hosting/210-boot8-bootstrap-artifact-provenance-specification.md`
- `docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md`
- `docs/phase-12-build-package-and-artifact-system/171-pkg7-reproducible-build-specification.md`
- `docs/phase-14-testing-verification-and-conformance/202-test13-self-hosting-validation-plan.md`

## Implementation Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/bootstrap-self-hosting.gravity`
- `bootstrap/clojure/fixtures/rejected/bootstrap-boot*.gravity`
- `docs/artifacts/phase-15/bootstrap/stage0-p15-bootstrap-self-hosting-proof.edn`

## Accepted Behavior

- `P15-T01`: records `stage0` through `stage3` manifests, trusted inputs, supported profiles, supported backends, conformance reports, equivalence reports, TCB deltas, locked dependencies, compiler lineage, stage gaps, release gates, and unsafe audit records.
- `P15-T02`: records a Clojure seed compiler manifest with implemented documents, exclusions, supported profiles, stable diagnostics, provenance, host dependencies, runtime assumptions, backend artifact identity, conformance report, stage comparison metadata, and the retirement objective.
- `P15-T03`: records self-hosted module migration evidence for reader, syntax, macroexpander, resolver, typed core, effect checker, MIR, and diagnostics under `:meta`.
- `P15-T04`: records compiler-in-Gravity coding standard evidence for effects, capabilities, preserved facts, deterministic output, diagnostics, generated artifacts, ambient access policy, and unsafe audit.
- `P15-T05`: records explicit stage compatibility rows, profile and backend conformance links, gap review, support level, release readiness, and versioned matrix changes.
- `P15-T06`: records controlled rebuild inputs, environment controls, compiler lineage, rebuild comparisons, diverse rebuild identity, accepted deltas, revocation checks, equivalence reports, trusting-trust summary, and bootstrap provenance.

## Rejected Behavior

- `BOOT1001`: missing stage evidence.
- `BOOT2002`: unsupported profile accepted by the seed compiler.
- `BOOT3002`: ambient authority in a compiler module.
- `BOOT4003`: compiler pass loses a preserved fact.
- `BOOT5003`: stage matrix row lacks conformance links.
- `BOOT6001`: reproducibility claim lacks environment record.
- `BOOT7001`: equivalence report lacks compiler identities.
- `BOOT8002`: bootstrap provenance has a compiler lineage gap.

## Validation

```text
$ clojure -M:test
Ran 115 tests containing 7571 assertions.
0 failures, 0 errors.

$ clojure -M:gravity bootstrap-self-hosting bootstrap/clojure/fixtures/accepted/bootstrap-self-hosting.gravity > docs/artifacts/phase-15/bootstrap/stage0-p15-bootstrap-self-hosting-proof.edn
```

Proof parse:

```text
:gravity/stage0-bootstrap-self-hosting-artifact
sha256:8ebcbe0e30752f75bad9e70125e71a09ded3d4c46a8126d5b12d5e10e0a0e6f4
8
55
:complete
```

## Residual Risks

This artifact proves the Phase 15 stage0 Clojure bootstrap contract for bootstrap manifests, stage comparison evidence, trusting-trust records, and provenance. It does not claim a complete executable self-hosted compiler or release-ready bootstrap chain beyond the fixture-backed contract behavior.
