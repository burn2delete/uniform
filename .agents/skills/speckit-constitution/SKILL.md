---
name: speckit-constitution
description: Maintain the existing user-decision record
compatibility: Requires spec-kit project structure with .specify/ directory
metadata:
  author: github-spec-kit
  source: preset:uniform
---

# Speckit Constitution Skill

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

Update `.specify/memory/constitution.md` only for actual user decisions supplied
in this conversation. Preserve canonical doc links and current authority.
This command changes only the constitution and affected Spec Kit artifacts;
report unrelated implementation intents as follow-up work. Do not invent new
principles, approval gates or accounting quotas to fill a template.
