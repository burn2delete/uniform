---
description: Create evidence-linked Uniform tasks
---

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

Run `.specify/scripts/bash/setup-tasks.sh --json` for the selected feature.
Read `spec.md`, `plan.md` and the resolved tasks template. Write dependency-ordered
`tasks.md` using `- [ ] T001 [US1] Description with exact path` (story labels only
where applicable). Track a reviewable declared increment. Check a task only when
its named deliverable and evidence exist; externally reported validation is
explicitly attributed. Keep implementation, review and integration separate.
Do not mark inherited PR implementation complete merely because this planning
exercise is complete. No mandatory all-feature completion gate for draft PRs.
