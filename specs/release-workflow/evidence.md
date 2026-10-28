# Evidence: Release workflow migration

Inspected source: `017b541d678c6acb93eb5e843b1eda5a66babc32` (2026-10-26).

## Status dimensions

- Artifact preparation: complete; four migration documents are present.
- Local validation: passed on 2026-10-26 for the eight-package documentation candidate.
- Independent package review: pending.
- Main integration: pending; no merge receipt claimed.
- Component implementation acceptance: unassessed by this package; canonical
  [release outcome](../../docs/component-roadmaps/release.md#release-outcome) remains open.

## Source evidence and preservation

The [Release canonical remaining work](../../docs/component-roadmaps/release.md#remaining-tasks-and-evidence) and its contracts were inspected as work inputs, not
executed implementation evidence. Existing phase completion retains its exact
historical surface. Existing finite feature receipts are neither discarded
nor promoted to whole-component completion.

The four documents link the original task and acceptance locations instead of
creating another implementation ledger. No compiler source, runtime action,
target selection, contract semantics or historical identity was changed.

## Validation receipts

The same documentation candidate was checked as one eight-package transaction:

| Check | Result |
| --- | --- |
| Focused ASCII, file-set, Markdown link/anchor and migration-ledger checks | Passed: 8 packages, 32 files; all local links resolve; exactly four migration tasks per package |
| `clojure -J-Xmx256m -M tools/validate_gravity_docs.clj` | Exit 0: 240 documents, 19 phase indexes, ASCII and placeholder checks |
| `clojure -J-Xmx256m -M tools/validate_full_language_roadmap.clj` | Exit 0 |
| `git diff --check` | Passed |

The focused check enumerated only these eight package directories, resolved
relative targets from each document, compared anchor slugs with actual headings,
and asserted that tasks are migration-only rather than implementation copies.
The canonical validators establish structural compatibility, not semantic
implementation acceptance. No source or tooling was added to the repository.
Compiler execution, external Specify CLI invocation, command parsing and fresh
skill discovery are unrun. Independent review and integration remain pending.

## Review and integration receipts

Pending independent review and coordinator integration. Their absence is not
a request for another general project approval. Only an exact accepted package
plus integration receipt closes this migration increment.

## Implementation authority transfer

Source: `82f7e8d62c71b4e5bd895f8b62c2c76363e15d3b`.
The five original implementation rows are edited only in
[tasks.md](tasks.md#implementation-tasks), with the roadmap table generated.
IDs derive from the existing component/section/title-slug convention rather
than literal IDs in the original table. All rows remain Open. Actual evidence
targets, the entire historical task file, prior navigation and any integrated
contract-interface appendix remain unchanged. Historical workflow statements
above describe the earlier preparation slice, not current task authority.

Exact checks and independent review belong to the public change receipt.
Structural projection does not execute the product, authenticate semantic
evidence, grant authority or accept implementation/release outcomes.

### Observed transfer checks

Focused projector tests passed 4 tests / 66 assertions, zero failures/errors;
projection check passed. Baseline comparisons passed all five original table
rows/cells, derived IDs/Open statuses, five links/five actual targets, entire
historical suffix, original full roadmap and selected Runtime/Stdlib/CLI
prerequisite scope, prior navigation, exact TEST13/GOV2 appendix, five Markdown
ASCII/file/heading checks and missing-file/missing-heading controls.
Table SHA256:
`c25c231d002ee92f0bfce9a8f2081f649ca31fd4e58234cd625e05d4f2c37474`.
Appendix SHA256:
`5a5dbfafa9eacf62ee173bfe32adab674131a7ccd7f9c9466d241b7abc20fd70`.
Language boundary passed 6 tests / 11 assertions; dependency tests passed
10 tests / 88 assertions. Documentation passed 240 documents / 19 phase indexes;
full-language roadmap and whitespace passed. All commands exited naturally with
0. Exact committed-tree preflight and independent disposition belong to the
public PR receipt; no release command, rebuild or seed-retirement test ran.
# Actual next-stage rebuild task boundary

The task-authority owner and canonical requirement owner explicitly approved
the bounded refinement of the existing third Release record. Its literal ID,
title, Open status, baseline and public check/run/compile/absent-seed obligation
remain. The closing criterion now explicitly requires real prior-stage process
invocation on the complete next-stage closure, actual logs/output lineage,
link/package and accepted/rejected compiler execution without Clojure in that
candidate boundary. SH25 component construction, SH27 equivalence and final
release acceptance remain distinct; supplied rebuild records are insufficient.
This text does not claim that the executor has been implemented.

[Task-authority lineage](../sh-family-crosswalk/task-authority-lineage.edn)
retains predecessor/new file and acceptance-line hashes, equality of the four
other Release records and unchanged historical suffix. The population remains
five records, and the roadmap was regenerated through its owning projector.
Focused Release projection checks passed 5 tests / 78 assertions, zero
failures/errors; new controls preserve the executor boundary and original public
obligation. Independent review/integration remain pending. No compiler/rebuild,
equivalence, signature, deployment or public seedless execution was performed.

## Governance consumer interface correction

Input commit: `abc4ed1c8bdb944c7aed949a0215f14579864311`, tree
`dbf376de8d4655008058846b913147f498062402` (2026-10-28).
The [plan](plan.md#governance-evidence-consumers) now traces GOV1, GOV6, GOV7
and GOV8 to the existing first Open Release task, with concrete required
artifacts and rejected release cases. These are Release consumer interfaces;
the accountable policy owner and full evolution lifecycle remain unresolved.
FL-P17-T01 remains Open. All five Release records, the generated roadmap,
their acceptance text and SH crosswalk source pins were byte-identical to
the input commit in the initial correction. No implementation or independent
acceptance is inferred.

The direct package-link census changes from 236 to 240 original contracts.
This is navigation coverage only. It does not resolve PR445's 577 proposed
missing task interfaces or entail any particular candidate requirement's
acceptance. The historical contract-mapping candidate and its exact-candidate
checker retain their original scope; neither is a current full-package census.
At the input commit, the path filter is 31 complete current `specs/` quartets
plus two historical `.specify/` quartets (naming-pilot and reader-core-invocation),
33 total. The dependency audit's mapping registry contains 31 registered
packages; it is a different filter. No package or task is created here, and
none of these counts is a requirement or completion denominator.

Preserved reconciliation inputs, without adoption:

- PR445 head: `e9fa4d0d8e7c942e9ac1cc535c13f6d7840dae30`.
- Preserved manifest SHA256: `aa7ee8dfa7c7b0b7a9cb0e609944d7dcbcd81791f080708364a0faf8cc2e7980`.
- Accepted isolated D0 composition SHA256: `5c4c7eacf7745b10dbe7c29558255b51c0b1a9a7aad01020cae6aad040c3ad4d`, target `6056b2efa7188e7756cda6100b831d9bc489d4d52dbff5e924b0aa859c303418`.
- Unverified D1/D2 composition SHA256: `1ac5dd7859af44785428710dfe7c9c54631eb5e5098f6274bc3a9511ef9bf82a`, target `d03f730eee4608c359068ee9a7c99ce5845eef74457803cbba8a9e35e0c281a3`.

The accepted source-review backlog remains 586; 470 is proposed and unverified.
Unknown historical operations remain history and were not replayed.

Observed local checks (all exit 0):

| Command | Result |
| --- | --- |
| `clojure -J-Xmx256m -M tools/release_task_projection_test.clj` | 6 tests, 98 assertions; stable task identity/status, current projection and malformed identity/link/write controls |
| `clojure -J-Xmx256m -M tools/acceptance_dependencies_test.clj` | 14 tests, 402 assertions; source-bound crosswalk conservation and acceptance-forgery rejection retained; 240 direct contract links |
| `clojure -J-Xmx256m -M tools/validate_gravity_docs.clj` | 240 documents, 19 phase indexes, ASCII and placeholder checks passed |
| `clojure -J-Xmx256m -M tools/validate_full_language_roadmap.clj` | Passed |
| `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test` | 6 tests, 11 assertions |

Initial local checks caught an unsupported task-link fragment and invalidated
crosswalk spans in an attempted acceptance-text refinement. That task edit
was removed in full; the final increment only adds the plan interfaces and
their structural checks. No compiler, broker, runtime or release execution
was performed. Independent review and integration of this correction remain
pending; the broader requirement reconciliation remains unfinished.

Current-main composition: ordinary merge `f60cf2e90752bb2e5df74697cab95dc5b799c0bd`
incorporated main `2855b6cc8683be1100dc0be45ef4cbd01c7043c3` without textual
conflicts. The same dependency suite (14 tests/402 assertions), Release suite
(6 tests/98 assertions) and projection check passed on that composition.
Comparison with that main confirms no changes to Release task authority,
generated roadmap or SH crosswalk; the four-file correction remains isolated.
The incorporated main's cleanup changes retain their original authority and
are not claimed as this increment's work.
