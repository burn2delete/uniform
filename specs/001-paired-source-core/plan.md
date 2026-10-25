# Implementation plan: paired source/Core reference pilot

Spec: [spec.md](spec.md) | Evidence: [evidence.md](evidence.md)
Owner: Spec Kit adoption delegate for this directory only.
Input revision: PR356 `700e2929c6d5aeb394f4fe342108a86dfb54f58e`.

## Contract alignment

Reuse the C5/C6/L2 contract links from the spec and the existing public ingress,
Core constructor/verifier and development runner. Preserve semantic provenance
and fail-closed rejection. The constitution does not grant new authority or
remove language safety obligations; it prevents project-accounting and repeated
approval bureaucracy from entering the tooling workflow.

## Approach and exact files

1. Inspect immutable PR356 metadata and its test file with `git show`. Map the
   three `paired-` tests and ingress controls to R1-R5, retaining their actual
   scope. Do not cherry-pick, edit or recreate its implementation.
2. Use the generated prompt/artifact skill to create `specs/001-paired-source-core/spec.md`, then
   read the supported local plan/tasks template overrides. Fill this plan and dependency-ordered
   `tasks.md` using the generated Uniform Codex skill instructions.
3. Record observed source and externally reported validation separately in
   `evidence.md`. Preserve open review/integration items unchecked.
4. Validate all new scaffold/config/skills/templates in a disposable copy using
   `.specify/validation/verify.clj`; run the existing document validator. Keep
   reproducible observations in `.specify/validation/results.edn`.

Dependencies: the existing PR356 test commit must remain available for historical
inspection. Pilot files depend only on canonical contract links and that snapshot.
Shared AGENTS/development/SH07/README paths remain with their active owners.
No new service, data model, compiler adapter, CI lane or scheduler is planned.

## Validation and evidence

For implementation acceptance, the owner command is preserved in the first
comment of the immutable PR356 test file; it selects the three paired tests and
five ingress/snapshot controls through the existing runner and host broker.
Use current coordinator/evidence rules for any future rerun. The historical
watchdog and heap are host execution safeguards, not project work quotas.
This adoption task runs no JVM and independently validates no compiler behavior.

For adoption acceptance, verify actual feature creation, local override resolution,
plan preservation, tasks prerequisites, negative missing-feature behavior,
config/events state, ASCII, local links, preset copies, generated skills and
read-only CLI inventory. Report unsupported runtime dispatch accurately.

## Delivery and remaining work

Adoption draft [PR371](https://github.com/burn2delete/uniform/pull/371) is separate
from implementation PR356. The source implementation is reported validated by
its owner, and later merged separately; this pilot performed no independent review or integration. No checkbox in
this pilot changes that disposition. Deus reviews this additive integration and
decides merge; the process bridge may add one shared navigation link afterward.
