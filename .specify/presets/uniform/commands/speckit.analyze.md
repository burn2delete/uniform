---
description: Analyze consistency and evidence scope
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

Read the selected spec, plan, tasks and evidence record. Report contradictions,
missing canonical references, unsupported completion/authority claims and owner
conflicts with exact paths. Do not modify implementation or wake reviewers.
Existing obligations determine severity; invent no generic process gates.
