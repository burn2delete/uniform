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

## Original T1-T13 family candidate, 2026-10-28

Input main: `6276c94b0b753c888ef8fecc794bcc81245ea3b3`, tree
`9f464ed0f03866bd4978e4213a488f3b1ae48683`. PR554 input snapshot at
`53ae6a403b20d15155bb5b04911fe7ac0092b921` has SHA256
`423dd6ec37f6d8f461de4a24d325033902ccce3249187073b796065fa3ca5d94`.
The [family proposal](requirement-proposal.edn) retains 13 original documents,
631 actual occurrences, 960 facets and 65 existing semantic requirement IDs.
All 814 facets outside the 146 nonnormative facets have retained unit owners.
The 85 negative-acceptance facets remain intact. No original source is edited.

The committed PR445 manifest SHA256 `f4c171820dfdadaf7e7b5a3f04a701a124adfffd6cbaf6b598c1b82098aae690`
and preserved applying manifest SHA256 `aa7ee8dfa7c7b0b7a9cb0e609944d7dcbcd81791f080708364a0faf8cc2e7980`
are different complete inputs. Direct local comparison found this family's
65 records, 960 facets and all dispositions identical between them. The
existing ai-build-tools proposal lineage and dependency artifact hashes are
preserved; other families' delivered decisions are not re-applied here.
Prior atomicity states remain 51 reviewed and 14 proposed; all prior
nonoverlap and implementation acceptance qualifications remain in force.

The candidate has 200 guarded field edits. They bind the current task hashes
and closing boundaries, retain concrete implementation/evidence gaps, add
source-qualified dependency/acceptance detail, correct four unsupported
obligation phrases and add the existing CLI command-inventory interface.
Sixteen existing task rows gain closing criteria: twelve Tools and four CLI.
All remain Open. Four context-sensitive redaction refinements remain proposals
for central adjudication; no aliases or semantic IDs are deleted.

Four corrected IDs: `behavior/t3/formatter-write-policy`,
`behavior/t7/runnable-doc-examples`, `behavior/t9/registry-policy-search`,
`behavior/t11/distributed-ai-cost-classes`. The first removes invented general
ownership authority, the second retains mandatory checking of runnable
examples, the third preserves the actual search filters, and the fourth
preserves actual provider/tool reporting without invented memory-cost policy.
Every ID in the proposal's `unresolved-requirement-ids` remains unassessed for
implementation and cross-family nonoverlap. Historical support is empty for
these 65 records; historical phase-13 record proofs are not promoted to actual
service execution or accepted implementation.

Observed candidate checks: tooling requirement controls passed 6 tests / 24
assertions; Tools projection tests passed 4 / 80; CLI projection tests passed
5 / 88; both projection checks passed. Document validation passed 240 documents
and 19 phase indexes; full-language roadmap and whitespace passed. Language
boundary passed 6 / 11. Negative controls reject wrong source/task hashes,
missing or renamed tasks/markers/IDs, lost occurrences/facets/negative
conditions, changed acceptance state, context-erasing aliases and stale or
duplicate field operations. Initial local producer/test reader delimiter errors
were corrected before the passing controls; no compiler evidence was produced.

Independent exact-candidate review and central adoption remain pending.
The coordinator routes review after consuming the immutable candidate. No
global completion count, requirement denominator, service/runtime execution,
release acceptance or self-hosting credit is conferred by this patch.

### Independent review guard correction

The 2026-10-28 independent review returned changes required for the proposal
checker, receipt SHA256
`e69a09869b67eaa4af7efd53ea37a4c1dd02581b628376b813debe91abe20411`.
Its direct source/task review passed the thirteen sources, sixteen amended
rows, four obligation corrections and retained context-sensitive refinements.
The original `2ab753db` and namespace-only `1108a447` candidates remain history.
Independent review did not accept either as the complete consumable candidate.

The author reproduced all nine incorrect acceptances against the original
checker: absent/wrong input metadata, missing dependency/batch lineage,
hidden unresolved IDs, false unit atomicity/history, erased qualification
payload and an accepted/Complete unit gap. The successor pins snapshot and
proposal-format bytes, binds metadata/lineage and the exact guarded edits to
the frozen source-qualified proposal, and checks duplicated unit states and
gaps against the preserved/patched records. Qualification acceptance, owner,
facet IDs and dependencies must match the actual task and source-qualified
unit. This reuses independently inspected source clauses; it confers no
implementation acceptance or acceptance of the old checker.

The repaired focused suite passed 7 tests / 33 assertions, including all nine
review regressions. The proposal, all 65 requirement IDs, 960 facets, task
criteria, states and historical records remain byte-identical to `1108a447`.
No shared SH crosswalk or central source-version composition is changed.
Affected independent re-review remains with the same reviewer, after its
existing governance repair, through the coordinator's existing route.

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
