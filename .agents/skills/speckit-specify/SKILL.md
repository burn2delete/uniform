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

Before selecting a feature directory, classify the request with
`.specify/semantic-names.md`. For private rename-only changes, return to the
ordinary branch/PR workflow there and stop: record the old/new map, complete
reference search, focused owner checks and required manifest/digest updates;
create no feature directory or Spec Kit artifact. Otherwise continue only with
this command's applicable bounded workflow. Public/serialized identity,
protocol/schema/diagnostic IDs, ownership, semantics and new evidence are
explicitly not private-only; no substantive edit implicitly requires all four
artifacts.

Read `AGENTS.md`, `.specify/README.md`, `.specify/memory/constitution.md`,
and the owning canonical contracts before changing artifacts. User authorization
and current coordination remain authoritative. Use the selected feature directory
from explicit user input, `SPECIFY_FEATURE_DIRECTORY`, or checkout-local
`.specify/feature.json`; never select another worker's feature implicitly. If
selection is missing, ask only for that missing context. Do not change shared
files owned by another worker. Keep partial work visible in an early draft PR.
Do not add project quotas, repeated approval gates, hooks, services or schedules.

Create or update `spec.md` in the explicitly selected feature directory using
`.specify/templates/overrides/spec-template.md`. Create only an absent owned
directory after checking current feature names. Write the checkout-local
`.specify/feature.json` only if useful, using a repository-relative
`feature_directory`. Branch/worktree ownership stays with the coordinator. For an existing PR, record its immutable head and observed scope;
do not recreate implementation or retrospectively assert full conformance.
Name user scenarios, accepted/rejected behavior, canonical links, scope exclusions,
owner and evidence expectations. Do not bulk-convert existing contracts.

Apply `.specify/semantic-names.md` to newly introduced implementation filenames,
namespaces and application/API names. Use short subsystem/behavior words; keep
planning IDs in metadata/cross-references. Review new names in this change only,
record legacy rename dependencies, and preserve compatibility-bearing IDs.
