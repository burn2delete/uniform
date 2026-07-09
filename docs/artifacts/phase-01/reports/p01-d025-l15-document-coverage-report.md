# P01-D025 L15 Document Coverage Report

Date: 2026-06-24
Agent: Codex
Status: complete

## Governing Document Read

- `docs/phase-01-core-language/025-l15-capability-provider-specification.md`
- `docs/phase-01-core-language/015-l5-type-system-specification.md`
- `docs/phase-01-core-language/016-l6-effect-system-specification.md`
- `docs/phase-01-core-language/019-l9-error-handling-specification.md`
- `docs/phase-01-core-language/022-l12-compile-time-evaluation-specification.md`
- `docs/phase-01-core-language/023-l13-standard-library-design-principles.md`
- `docs/phase-01-core-language/024-l14-language-facet-system-specification.md`

## Implementation Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/capability-provider.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-capability-missing.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-provider-*.gravity`
- `docs/artifacts/phase-01/providers/stage0-l15-document-coverage-proof.edn`

## Accepted Evidence

The accepted `capability-provider.gravity` fixture is checked through the
Clojure stage0 typed/effected core pass. It records the common provider
machinery defined by L15 without claiming full runtime-provider boot,
package-lock persistence, or platform integration.

The emitted artifact records:

- provider declarations with ids, versions, categories, capabilities, profiles,
  targets, effects, contracts, failures, trust levels, artifact schemas, and
  conformance suites;
- runtime and build grant records with principals, capabilities, providers,
  scopes, phases, lifetimes, and audit policies;
- explicit capability value records;
- deterministic provider selections by source annotation, manifest,
  workspace policy, profile default, and compiler default;
- filesystem, network, environment, model, tool, memory, and compiler scope
  audit logs;
- compile-time provider replay records with redacted secret policy;
- runtime provider manifests;
- provider conformance results;
- safe replacement records for a math provider and a memory provider;
- attenuation and revocation records;
- complete L15 capability-provider conformance.

The current artifact summary is 3 provider declarations, 2 grants, 1
capability value, 5 provider selections, 7 scope audits, 1 compile-time replay
record, 1 runtime manifest, 1 conformance result, 2 replacement records, 1
attenuation record, and 1 revocation record.

## Rejected Evidence

The rejected fixtures prove stable diagnostics for:

- `L15-CAPABILITY-MISSING`
- `L15-PROVIDER-MISSING`
- `L15-PROVIDER-AMBIGUOUS`
- `L15-PROFILE`
- `L15-SCOPE`
- `L15-PHASE`
- `L15-TRUST`
- `L15-REPLAY`
- `L15-SECRET`
- `L15-CONTRACT`
- `L15-REVOCATION`

## Validation

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/capability-provider.gravity
```

Expected artifact kind:

```text
:gravity/stage0-typed-core-artifact
```

```bash
clojure -M:test
```

Output:

```text
Testing gravity.bootstrap-test

Ran 19 tests containing 771 assertions.
0 failures, 0 errors.
clojure bootstrap validation passed: hosted hello, L1 reader artifacts, L2 core artifacts, L3 module artifacts, L4 macro artifacts, L5 typed/effected artifacts, L6 effect-system artifacts, L7 pattern-match artifacts, L8 dispatch artifacts, L9 error-handling artifacts, L10 memory-model artifacts, L11 concurrency artifacts, L12 compile-time artifacts, L13 standard-library artifacts, L14 facet artifacts, L15 provider artifacts, L16 alternative macro artifacts, and 142 rejected fixtures
```

```bash
clojure -M -e artifact-summary
```

Output:

```text
complete L15 provider conformance with 3 declarations, 2 grants, 5 deterministic selections, 7 scope audits, replay, runtime manifest, conformance, replacement, attenuation, and revocation records
```

## Residual Risks

This completes the stage0 L15 document task. It does not claim full runtime
provider initialization, package lockfile integration, platform-provider boot,
category-specific provider conformance suites, release readiness, or
self-hosting.
