# Evidence: Developer Tools workflow migration

## Source and scope

Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.
Owned set: `specs/tools-workflow/spec.md`, `plan.md`, `tasks.md`, `evidence.md`.
Canonical implementation task source: [component roadmap](../../docs/component-roadmaps/tools.md).

## Observed documentation results

Four workflow artifacts prepared. Contract routing is a reviewable proposal based on the canonical inventory and component boundaries; it does not establish semantic implementation or exclusive ownership. Documentation checks executed on 2026-10-26: 28 artifacts, 232 local target/anchor checks and seven migration-only ledgers passed ASCII/private-context scanning. Three negative fixtures rejected a missing file, missing anchor and added implementation task. Existing `clojure -M tools/validate_gravity_docs.clj` passed for 240 documents / 19 phase indexes; `clojure -M tools/validate_full_language_roadmap.clj` passed, including its accepted/rejected self-test. These dependency-free checks used the documented Clojure runtime entrypoint; no external Specify CLI was run. Validation covers structure and references, not full semantic conformance of the linked contracts.

## Acceptance state

- Artifact preparation: complete.
- Documentation validation: passed for the declared structural/link checks.
- Independent review: pending.
- Integration: pending; no merge receipt.
- Component implementation/release acceptance: unassessed by this package.

## Limits and remaining work

No compiler tests, runtime/device/service sessions, external Specify CLI invocation or automatic command discovery were executed for this documentation slice. Existing canonical contracts and historical pilot receipts are unchanged. Review and integration cannot infer implementation completion from file presence or migration checkboxes. Service-specific schema and target support decisions remain with their specialist roles. Existing tooling record assemblers do not establish real sessions or supported services.

## Implementation authority transfer

Source: `dcc9849df582ebb177560b41d702071e3481669a`.
The twelve original named service rows are edited only in
[tasks.md](tasks.md#implementation-tasks), with the roadmap table generated.
Each complete name/role, remaining implementation tasks, closing evidence and
governing-contract cell is retained. IDs derive from the existing
component/section/full-title-slug convention; the source had titles rather
than literal individual IDs. All records remain Open. The original migration
file, every prior link and D6/PERF6 appendix remain unchanged.

Earlier workflow-only statements above describe historical preparation.
Exact checks and independent review belong to the public PR receipt.
No service session, generated patch acceptance, authority grant, supported
target or compiler/release completion is inferred from this transfer.

### Observed transfer checks

Focused projector tests passed 4 tests / 80 assertions, zero failures/errors;
projection check passed. Baseline comparison passed all twelve named rows and
complete role/action/closing-contract cells, derived IDs/Open statuses, twelve
links/twelve actual targets, entire historical suffix, original full roadmap
reconstruction, prior navigation, exact D6/PERF6 appendix, five Markdown
ASCII/file/heading checks and missing-file/missing-heading controls.
Table SHA256:
`83b3a429e69cf9854f5bdea1e0a72aa8abc3ba5f175e8305720c90b791166454`.
Appendix SHA256:
`0d8cca74c2877cb9a8712757cef0d941f07abc6da6ac61de6784b3bf26e7ad97`.
Language boundary passed 6 tests / 11 assertions; dependency tests passed
10 tests / 88 assertions. Documentation passed 240 documents / 19 phase indexes;
full-language roadmap and whitespace passed. All commands exited naturally with
0. Exact committed-tree preflight and independent disposition belong to the
public PR receipt, without implying executed developer services.

## Current-source tooling requirements check

The current-port checker and its negative fixtures preserve the historical
Phase 13 proposal as a noncanonical source view. The view is anchored to base
commit `6218e80e0238cbd071a8837193e7afb5f0b87869` and tree
`51a615a96828ecaff57824625bb062a9dd2c5f20`, with the historical proposal at
`2ab753db9a43a5544379cf72c75c91eeab3d23b7` and tree
`c0610e6ac4dfccc9908273dd3a8aaf705a0fa332`. It pins the historical manifest,
source snapshot, proposal format, occurrence API and both historical task
sources, and independently checks current document, manifest, format, API and
task-source hashes.

`clojure -M tools/tooling_requirements_test.clj` passed 7 tests / 43
assertions, zero failures/errors. `clojure -M tools/tooling_requirements.clj
--check` passed 13 source documents, 631 occurrences, 960 facets, 65
requirements, 200 guarded edits and 16 task references. The checker verifies
source spans and hashes, exact historical task acceptance-boundary hashes,
current Open task identity/title boundaries, all primary and additional task
interfaces, exact ordered requirement identity populations, exact paired
current-document identities, retained normative-facet unit links, contextual
refinement state, stale/duplicate edit refusal and proposal-only nonclaims.

The language-boundary gate passed 6 tests / 11 assertions. The document
validator passed 240 documents / 19 phase indexes; the full-language roadmap
validator and workstream-governance validator also passed. `git diff --check`
reported no whitespace errors.

Historical task criteria are retained only in
`specs/tools-workflow/current-source-view.edn`; current task acceptance is not
replaced, implementation acceptance is unassessed, and independent review and
integration remain pending. This receipt does not claim canonical adoption,
global nonoverlap, release completion or seed retirement.

## Current-main composition refresh

The repaired source-view increment was normally merged with current main
`4df59ab107d5865e3afbf9173041cc9401227542`, tree
`d977cf8ca3de45e73920d6d6a5f5f59aca219527`. The composed merge commit is
`c3ef5daa788f5d3e534347e04b7eaeb5c60bfdaf`, tree
`66e06c479e0c25aa6a6fc06eb0d580e7dbd6cbff`, with the original repaired
candidate `e216a4463a6db730b687da1ff6c190d87becae1a` retained as its other
parent. The frozen source-view baseline remains `6218e80e0238cbd071a8837193e7afb5f0b87869` / tree
`51a615a96828ecaff57824625bb062a9dd2c5f20`; it is not repinned to current
main.

The final comparison against current main contains only the four PR557 paths:
the source view, this evidence receipt, the bounded checker and its focused
tests. The manifest, proposal format, occurrence API, Phase 13 documents and
both current task sources referenced by the source view are byte-identical to
the frozen base. Main's C6/C11/CI/effects work is disjoint and is not counted
as tooling-requirements evidence.

The pinned changed-file lint used
`PATH=/tmp/acc08-controls-01a183dd/kondo-2025.10.23:$PATH clojure -J-Xmx512m -M tools/ci/lint.clj 4df59ab107d5865e3afbf9173041cc9401227542 tools/tooling_requirements.clj tools/tooling_requirements_test.clj`.
It passed with 0 unchanged baseline warnings and 0 blocking warnings/errors.
The generated receipt was moved intact to
`/tmp/uniform-pr557-current-main-ci-lint-c3ef5daa-final`.

The lint-only repair adds an explicit namespace require to the focused test;
it does not alter checker behavior or source authority. Focused current-port
checks remain 7 tests / 43 assertions, with the same 13-document, 631-
occurrence, 960-facet, 65-requirement, 200-edit and 16-task-reference result.
No hosted CI, GitHub write, push, native execution, release, canonical
adoption or implementation acceptance is claimed by this composition.

## Stage-owner main composition

The next normal merge incorporates main
`d4a7ddc3c01b91a6152120b44aaa9498950878ae`, tree
`df7d6b2f8b8117c81f4d396aa4b2210e28709c14`. Its sixteen MIR/Types
implementation, test, and Bootstrap evidence paths are disjoint from the
checker, focused tests, source view, and their referenced proposal, occurrence,
task, manifest and Phase 13 document inputs. The three executable/view blobs
remain byte-identical to the accepted candidate; this receipt adds only the
composition observation. The frozen baseline remains `6218e80e` as above.

Fresh checks on this composition passed: 7 tests / 43 assertions; the complete
13-document, 631-occurrence, 960-facet, 65-requirement, 200-edit and
16-task-reference checker; the 240-document validator; and whitespace. Logs:
`/tmp/uniform-pr557-815-tests.log`, `/tmp/uniform-pr557-815-check.log`, and
`/tmp/uniform-pr557-815-docs.log`. Source-proposal review and implementation
acceptance remain separately unassessed.
