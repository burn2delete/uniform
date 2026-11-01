# Plan: Resolve owner source-to-Core verification

[Specification](spec.md) | [Finite task](tasks.md) | [Evidence](evidence.md)

## Dependencies and handoffs

Use the existing reviewed staged public APIs in the frozen cohort. Syntax and
Macro products feed C5; C5 feeds Core. Complete artifacts are DATA, not authority
from stored passed reports. Current host-owned invocation selects the Reader and
authenticated-envelope source explicitly. No expected-Core object or verification
session is supplied to the generic verifier.

The subject is the full owner source, not a smaller fixture. A closed,
non-evaluating Clojure reader inventories quotation as data; genuine C2 must agree
independently. Ordered root, origin, binding, reference and occurrence coverage
is checked separately from construction.

Each constructor first captures its complete unaccepted product using full
serialization, parsed equality, SHA-256 and atomic completion. Only then does a
separate coverage check and independent review permit the next stage. Immutable
source, host and dependency identities are checked before and after every run.

## Bounded progression

1. Inventory all source forms and exports; genuine Reader/Syntax construction.
2. Expand all definitions using public Macro construction and fresh parent checks.
3. Construct C5; verify all binding/reference/scope/export coverage.
4. Run generic fresh C5, binding mutation and caller-source-selection controls.
5. Construct Core and verify the complete source-to-Core occurrence partition.
6. Run generic fresh Core and genuine semantic mutation; independent acceptance.

This sequence refines one finite task. It does not reproduce the canonical
[Resolve](../../docs/component-roadmaps/resolve.md#current-evidence-and-remaining-work)
or [Core](../../docs/component-roadmaps/core.md#current-evidence-and-remaining-work)
implementation tables, alter ownership of other work or close their wider gates.

## Reproduction and resource boundary

Use only the exact source/host closure in evidence. Public entry points are
`compiler-c3-syntax-source-artifact`, `sh05-macro-artifact-from-c3-artifact`,
`sh06-resolution-artifact-from-sh05-artifact`,
`sh06-resolution-artifact-verification`, `sh07-core-from-resolution-artifact`
and `sh07-core-artifact-verification`, with their original cohort arities.
Host watchdogs are execution guards, not language quotas. Larger Core capture
required a reviewed 3GiB serial JVM profile before its first attempt; full
round-trip and equality checks were retained. Do not infer current-main results
or guaranteed memory fit from this cohort.

## Documentation delivery

Own only this package's four Markdown artifacts. Validate links, ASCII and
existing documentation/roadmap gates; independently review the exact candidate.
Publish as draft until documentation integration is separately recorded.
