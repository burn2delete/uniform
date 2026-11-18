---
description: Assess the declared increment and remaining evidence
---

## User Input

```text
$ARGUMENTS
```

Before selecting a feature directory, apply the ordinary fix workflow in
`.specify/README.md`. For a routine fix, review the diff and focused regressions
using the PR and its CI results. Record the disposition on the PR and stop
before feature selection unless the user requests separate Spec Kit artifacts
or the owning contract requires them.

Read `AGENTS.md`, `.specify/README.md`, `.specify/memory/constitution.md`,
and the owning canonical contracts before changing artifacts. User authorization
and current coordination remain authoritative. Use the selected feature directory
from explicit user input, `SPECIFY_FEATURE_DIRECTORY`, or checkout-local
`.specify/feature.json`; never select another worker's feature implicitly. If
selection is missing, ask only for that missing context. Do not change shared
files owned by another worker. Keep partial work visible in an early draft PR.
Do not add project quotas, repeated approval gates, hooks, services or schedules.

Compare the selected spec, plan, tasks, implementation revision and current
evidence. Report each requirement as evidenced, externally reported, unrun,
blocked or contradicted. Report the results on the PR. Update separate evidence
only where required by the owning contract or governed admission. Add concrete
remaining tasks where needed. Scope the conclusion to this declared increment;
do not infer SH07, release, self-hosting, integration or full-language completion.
A draft can remain open with remaining review/integration work. Deus decides merge.
