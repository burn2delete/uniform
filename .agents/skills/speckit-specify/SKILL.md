---
name: speckit-specify
description: Specify one bounded Uniform change
compatibility: Requires spec-kit project structure with .specify/ directory
metadata:
  author: github-spec-kit
  source: preset:uniform
---

# Speckit Specify Skill

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

Create or update `spec.md` in the selected feature directory using the resolved
`spec-template`. To create a numbered directory, use the official
`.specify/scripts/bash/create-new-feature.sh --json --short-name NAME DESCRIPTION`
only after checking existing directories. It creates artifacts, not Git branches;
branch/worktree ownership stays with the coordinator. Keep the feature pointer
checkout-local. For an existing PR, record its immutable head and observed scope;
do not recreate implementation or retrospectively assert full conformance.
Name user scenarios, accepted/rejected behavior, canonical links, scope exclusions,
owner and evidence expectations. Do not bulk-convert existing contracts.
