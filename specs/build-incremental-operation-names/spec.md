# Semantic incremental operation names

Owner: semantic-name migration. Input: `16ff1f59471a7fedd7d2a4552c4bde1edf0b3d9e`.
Planning reference: C16. Status: implementation independently accepted at
`ab77968ec97a0d9032ea32b5f049c3ad52f2aab6`; final-base preflight and merge pending.

## Outcome and contracts

Migrate one internal operation-binding namespace to subsystem/behavior naming,
following the [existing sampled audit](../../.specify/naming-pilot/plan.md),
[naming rule](../../.specify/semantic-names.md), and
[Build boundary](../../docs/component-roadmaps/build.md).
The [incremental contract](../../docs/phase-06-compiler-architecture/095-c16-incremental-compilation-design.md)
and [development workflow](../../docs/development-verification.md) remain canonical.

## Acceptance and compatibility

- `gravity.build.incremental.operations` replaces the internal legacy helper;
  its complete facade caller graph loads without the old namespace.
- Partial/nested operation overrides, recursive fallback, exception unwinding
  and subsequent independent calls retain their behavior.
- The public `gravity.c16-incremental` facade, operation keys, diagnostics,
  schemas, artifact fields, test selectors and historical evidence stay unchanged.
- No compiler semantics, real cache, self-hosting or release completion is claimed.

Implementation owns the change and [evidence](evidence.md). Independent review
accepted the finite implementation, retaining the documented baseline failures
and planner limitations. Coordinator verification of the documentation-only
successor against final main and integration remain separate, pending steps.
