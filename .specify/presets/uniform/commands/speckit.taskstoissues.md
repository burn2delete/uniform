---
description: Report optional issue conversion status
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

The upstream core issue-conversion command is deprecated in v1.2.3 and the
optional GitHub extension is not installed here. Report this fact. Do not create
issues, install extensions or contact other people from this command. Tasks and
the draft PR already provide the adoption record; later external integration is
a separate decision for Deus.
