---
name: speckit-clarify
description: Resolve material ambiguity without extra ceremonies
compatibility: Requires spec-kit project structure with .specify/ directory
metadata:
  author: github-spec-kit
  source: preset:uniform
---

# Speckit Clarify Skill

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

Read the selected spec and canonical owners. Resolve routine choices from
existing decisions; ask concise questions only for missing material intent or
unresolved contract conflict. Record answers and explicit unresolved items in
`spec.md`; continue independent work. Do not ask for authorization already given.
