# MIR carrier semantic regressions

Semantic task: `mir/carrier-semantic-regressions`.
Owner role: MIR regression implementation; independent reviewer separate.
Status: running; review and integration pending.

## Contract and finite outcome

The [MIR contract](../../docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md)
and [MIR component roadmap](../../docs/component-roadmaps/mir.md) remain canonical.
This bounded work package contributes regression evidence to `mir/3` and `mir/5`;
it does not close either component row.

Reconcile the scalar-carrier regression with PR #378's existing unmetered
persistent-DATA admission. Accepted receipts have no project-work counters and
1025 valid values are not refused merely for width. Preserve semantic scalar
kinds, exact SHA-256 identity shape, MIR operand indices and types. Invalid late
leaves must still be checked; lazy sequences must be rejected without realization.

## Acceptance

- Positive scalar and width-boundary carriers are accepted by the real C11 plan.
- Invalid numeric/native scalar leaves after the former width boundary reject.
- A late lazy sequence rejects without running its body.
- Existing invalid identity, MIR index and type controls still reject.
- Focused checks pass, an independent reviewer accepts the exact change, and
  the reviewed increment is published and integrated through the normal route.

No production behavior or source identity pins change. Supplied DATA evidence is
not fresh authenticated source ingress, C12/C13 integration, native execution,
seedless self-hosting, or completion/readiness of PR #378 or dependent PR #383.
