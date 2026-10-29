# Governance task authority

Spec Kit authority for the GOV1-GOV10 artifact producers. Implementation remains
Open. This adds governance tooling interfaces, not a compiler pipeline pass.

## Purpose and requirements

The ten tasks in [tasks.md](tasks.md) own change, experiment, library, namespace,
yank, RFC, security response/review, stabilization and target-support artifacts.
Each preserves its required outputs, source criteria, rejection diagnostics and
conditional dependency interfaces. Package policy consumers remain in Build.

The [governing contracts](../../docs/phase-17-governance-and-evolution/README.md)
retain semantic authority. Ordinary implementation and focused validation do
not acquire a per-run governance permission or migration-proof prerequisite.
Governance acceptance applies to the governed contract or release claim.

## Dependencies and artifacts

[plan.md](plan.md) records producer/consumer boundaries. Outputs are actual
subject-bound governance records and decisions, rejection evidence and consumer
results. Existing stage0 record validation is not execution acceptance.

## Conformance

Every declared task has positive and negative closing criteria. Missing,
duplicate or unknown IDs, incomplete fields, changed checkbox/status agreement
and stale generated views reject. Independent composition, granularity and
implementation acceptance are separate. [evidence.md](evidence.md) records
observed source checks without claiming governed services execute.
