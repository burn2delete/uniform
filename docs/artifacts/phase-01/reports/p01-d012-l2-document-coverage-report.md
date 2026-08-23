# P01-D012 L2 Document Coverage Report

Date: 2026-06-24
Agent: Codex
Status: complete by capability proof

## Governing Document Read

- `docs/phase-01-core-language/012-l2-core-language-semantics.md`

## Implementation Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/core-semantics.gravity`
- `bootstrap/clojure/fixtures/rejected/core-unknown-form.gravity`
- `bootstrap/clojure/fixtures/rejected/eval-order.gravity`
- `bootstrap/clojure/fixtures/rejected/recur-target.gravity`
- `bootstrap/clojure/fixtures/rejected/set-illegal.gravity`
- `bootstrap/clojure/fixtures/rejected/throw-illegal.gravity`
- `bootstrap/clojure/fixtures/rejected/host-semantics.gravity`
- `bootstrap/clojure/fixtures/rejected/lowering-gap.gravity`
- `docs/artifacts/phase-01/core/stage0-core-capability-proof.edn`

## Accepted Evidence

`core-semantics.gravity` proves the current stage0 compiler can lower source
forms into a serializable L2 core artifact. The artifact records source spans,
generated-origin chains, profile facts, effect facts, evaluation order, latent
function effects, and call records for the initial core forms.

## Rejected Evidence

The Clojure bootstrap test suite proves L2 rejection for unknown core forms,
evaluation-order violations, incompatible recur, illegal mutation, throw
without error effect, host semantic leakage, and surface forms without a core
or domain lowering.

## Validation

```bash
clojure -M:test
```

## Residual Risks

L2 is complete only for the current stage0 core artifact surface. Full macro
expansion, type/effect algebra, profile legality, safety classification, MIR
construction, and backend preservation checks remain downstream work unless a
later roadmap task claims them with capability proof.
