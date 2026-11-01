# Evidence: CLI workflow migration

## Source and scope

Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.
Owned set: `specs/cli-workflow/spec.md`, `plan.md`, `tasks.md`, `evidence.md`.
Canonical implementation task source: [component roadmap](../../docs/component-roadmaps/cli.md).

## Observed documentation results

Four workflow artifacts prepared. Contract routing is a reviewable proposal based on the canonical inventory and component boundaries; it does not establish semantic implementation or exclusive ownership. Documentation checks executed on 2026-10-26: 28 artifacts, 232 local target/anchor checks and seven migration-only ledgers passed ASCII/private-context scanning. Three negative fixtures rejected a missing file, missing anchor and added implementation task. Existing `clojure -M tools/validate_gravity_docs.clj` passed for 240 documents / 19 phase indexes; `clojure -M tools/validate_full_language_roadmap.clj` passed, including its accepted/rejected self-test. These dependency-free checks used the documented Clojure runtime entrypoint; no external Specify CLI was run. Validation covers structure and references, not full semantic conformance of the linked contracts.

## Acceptance state

- Artifact preparation: complete.
- Documentation validation: passed for the declared structural/link checks.
- Independent review: pending.
- Integration: pending; no merge receipt.
- Component implementation/release acceptance: unassessed by this package.

## Limits and remaining work

No compiler tests, runtime/device/service sessions, external Specify CLI invocation or automatic command discovery were executed for this documentation slice. Existing canonical contracts and historical pilot receipts are unchanged. Review and integration cannot infer implementation completion from file presence or migration checkboxes. The selected compiler recipe must enumerate its actual command/options closure before that release slice can be accepted; wider command families remain independently open.

## Implementation authority transfer

Source: `dcc9849df582ebb177560b41d702071e3481669a`.
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

## Selected-command scope authority

The complete original `cli/compiler-commands` body is also edited only in
[the selected-scope ledger region](tasks.md#selected-compiler-command-scope).
Its original three numbered closure steps, required commands/options, both
source extensions, provenance, public-binary and wider-service exclusions
remain unchanged; they add no task IDs or general-task completion count.
Both anchored links preserve their actual files and heading fragments.
Local fragment handling checks containment before heading reads; other
components and shared helpers remain unchanged. No mapping appendix existed
in this plan, and none is invented. Earlier workflow-only statements above
describe historical preparation, not the current editable authority.

### Observed transfer checks

The first focused invocation stopped before tests because a test-only local
binding was closed early. After correction, one negative fixture changed only
an explanatory identity mention rather than the canonical scope identity; the
fixture was corrected to exercise that actual source. Those attempts are not
passing evidence. Focused tests then passed 5 tests / 88 assertions with zero
failures/errors; projection check passed.

Baseline comparison passed the five original table rows/cells, derived IDs/Open
statuses, five table evidence targets, complete selected `cli/compiler-commands`
body with all three closure steps and both file-plus-heading targets, entire
historical suffix, full original roadmap reconstruction, prior navigation and
five Markdown ASCII/file/heading checks plus missing-file/missing-heading
controls. No contract-interface appendix existed or was invented.
Table SHA256:
`119898bd310990072d5c016b7b584b1bb93034727dfdb53717667e7b542f0023`.
Selected scope SHA256:
`1f3398db28efe1c85f78cf89ef96feb43b38b224357ff4857763d5962dc249aa`.
Language boundary passed 6 tests / 11 assertions; dependency tests passed
10 tests / 88 assertions. Documentation passed 240 documents / 19 phase indexes;
full-language roadmap and whitespace passed. Passing commands exited naturally
with 0. Exact committed-tree preflight and independent disposition belong to
the public PR receipt; no command service or public compiler was executed.
