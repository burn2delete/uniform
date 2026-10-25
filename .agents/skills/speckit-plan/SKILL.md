---
name: speckit-plan
description: Plan a bounded change against Uniform contracts
compatibility: Requires spec-kit project structure with .specify/ directory
metadata:
  author: github-spec-kit
  source: preset:uniform
---

# Speckit Plan Skill

## User Input

```text
$ARGUMENTS
```

Read `AGENTS.md`, `.specify/README.md`, `.specify/memory/constitution.md`,
and the owning canonical contracts before changing artifacts. User authorization
and current coordination remain authoritative. Use the selected feature directory
from explicit user input, `SPECIFY_FEATURE_DIRECTORY`, or checkout-local
`.specify/feature.json`; never select another worker's feature implicitly. If
selection is missing, ask only for that missing context. Do not change shared
files owned by another worker. Keep partial work visible in an early draft PR.
Do not add project quotas, repeated approval gates, hooks, services or schedules.

Read the selected `spec.md` and `.specify/templates/overrides/plan-template.md`.
Create `plan.md` only if absent; preserve and reconcile an existing plan. Write it
with existing architecture, exact files/owners, dependencies, relevant positive
and negative checks, evidence ownership, unrun behavior and integration path.
Reuse existing verification tools; do not create new admission or accounting
systems. Omit inapplicable design artifacts. Preserve unresolved questions as
explicit facts; progress on independent work without routine approval stops.
