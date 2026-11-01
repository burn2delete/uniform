# Evidence: Auth workflow migration

## Scope and identity

Observed source: `017b541d678c6acb93eb5e843b1eda5a66babc32`.
Source tree: `a9e593299ab0963578da242403572eb3941281ce`.
Date: 2026-10-26. Scope: four Markdown workflow artifacts only.

## Current disposition

- Artifact preparation: complete.
- Documentation validation: passed; structural checks only.
- Independent review: pending.
- Integration: pending; no merge receipt claimed.
- Component implementation: unassessed by this change; canonical outcome remains open.

## Retained evidence and limits

The [component roadmap](../../docs/component-roadmaps/auth.md) is the
source for existing partial implementation observations and remaining work.
Hosted capability projections explicitly lack grant/provider-trust, proof and release authority. Workflow artifacts do not change credentials, tool permissions or trust roots.
No source compilation, runtime action, authoritative migration or release test
was executed for this documentation slice. No new CLI execution or full automatic
workflow discovery is claimed. Contract routing is navigation, not a replacement
for retained specifications or an accepted semantic ownership transfer.

## Validation record

Locally observed 2026-10-26 on the 16-file checked-IR workflow candidate:

- Focused Clojure document checks: 16 artifacts, 141 valid relative links and
  heading fragments, ASCII/private-path checks, four migration-only checklists
  and four explicit unassessed implementation states passed.
- Negative link controls: missing file and missing heading rejected.
- `clojure -M tools/validate_gravity_docs.clj`: passed (240 contracts,
  19 phase indexes, ASCII and placeholder checks).
- `clojure -M tools/validate_full_language_roadmap.clj`: passed.
- `git diff --check`: passed.

These checks validate document structure and navigation only. No compiler test
ran and no implementation task received acceptance from them.

## Review and integration record

Await independent review of the exact candidate and a verified merge receipt.
Do not mark integration complete merely because a branch or draft PR exists.

## Implementation authority transfer candidate

Baseline: `846644db7402c26a421eb7afd52615d52f145590`, inspected on
2026-10-26. Earlier sections retain their original scaffold-only scope. The five
real implementation rows now live in [tasks.md](tasks.md#implementation-tasks)
as the sole editable authority. The roadmap table is generated. All five remain
Open and unchecked. The complete original migration task file remains a
byte-identical suffix, including its checkboxes and historical links. Migration
progress is separate from compiler implementation completion.

### Identity, contracts and relative-link provenance

IDs follow the established component/heading/title-slug convention documented by
[Reader](../reader-workflow/evidence.md#local-review-correction-link-context-and-identity).
They are `auth/remaining-tasks-and-evidence/` plus:

- `complete-provider-declaration-and-deterministic-selection`
- `enforce-grant-intersection-attenuation-revocation-phase-and-lifetime`
- `bind-compiler-decisions-to-runtimebuild-enforcement-hooks`
- `bind-artifact-trust-to-exact-source-compiler-and-policy-identity`
- `publish-conformance-and-remaining-trust-assumptions`

Slash removal in runtime/build yields runtimebuild. No ID was positionally
renumbered. IDs were independently derived from original titles and compared in
display order. All titles, evidence baselines and closing criteria retain their
original meaning and projected bytes. The all-open header carries status;
evidence links are not acceptance. Later status updates render an explicit
Status column and require checkbox/Accepted; consistency, plus actual owner
receipts and semantic review outside the structural tool.

Three bootstrap evidence links retain their spelling in both document contexts.
The runtime-enforcement contract uses
`../../docs/phase-08-runtime-architecture/122-r11-runtime-capability-enforcement-design.md`
in the ledger instead of the roadmap's `../phase-08-runtime-architecture/` path.
The provider contract similarly uses
`../../docs/phase-01-core-language/025-l15-capability-provider-specification.md`
in the ledger instead of `../phase-01-core-language/` in the roadmap. Projection
rebases each target back to the original roadmap spelling. All five files exist;
all 18 contract-navigation lines in the plan retain their original bytes.

### Narrow shared-link extraction

`tools/task_projection_links.clj` extracts the accepted Legality relative-path
syntax, rebasing, file-existence and symlink-containment logic. It has no task
registry, discovery, CLI dispatch or write operation. Target validation inspects
file metadata and canonical paths; it does not read target contents. The fixed
component wrappers still validate before writing only their roadmap. Existing
Legality public arities, paths, record shape, diagnostics and output bytes are
preserved. Its focused test changes only to copy the helper into isolated CLI
fixtures. Legality task data and workflow artifacts are unchanged. The pure
shared parser, six earlier HTTPS-only wrappers and Syntax are unchanged.

### Observed scoped validation

Observed locally on 2026-10-26 from repository root:

- `clojure -J-Xmx512m -M tools/auth_task_projection.clj --check`: passed; original table bytes retained.
- `clojure -J-Xmx512m -M tools/auth_task_projection_test.clj`: 4 tests / 61 assertions, zero failures/errors. Covers deterministic projection, IDs/markers/history/status controls, all five actual evidence targets, unsupported/missing/escaping links, symlink escapes and subprocess fixed-path writes. Loading the helper with write-like arguments performs no CLI action and preserves files.
- `clojure -J-Xmx512m -M tools/legality_task_projection_test.clj`: 4 tests / 59 assertions, zero failures/errors. Existing assertions unchanged; fixture setup includes the extracted helper.
- `clojure -J-Xmx512m -M tools/task_projection_test.clj`: 5 tests / 137 assertions, zero failures/errors.
- An accepted-Legality before/after comparison passed 22 record/projection cases and five instrumented CLI cases, preserving returned records, full table/project bytes, diagnostic messages/data and fixed-path IO effects. It loaded the accepted implementation from Git baseline before the new implementation.
- The retained six-wrapper comparison passed 15 record cases and five CLI cases each, preserving complete output bytes, records, diagnostics, link policies and fixed-path IO effects.
- `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test`: 6 tests / 11 assertions, zero failures/errors.
- `clojure -J-Xmx512m -M tools/validate_gravity_docs.clj`: passed, 240 documents / 19 phase indexes.
- `clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj`: passed.
- A Clojure comparison checked exact original/current table bytes, five derived IDs/titles/Open statuses/closing criteria and actual link targets, complete historical suffix, 18 contract lines, five Markdown documents' ASCII/file/heading checks, and missing-file/missing-heading negative controls. Table SHA256: `72203b91f3afe43c24ce2f75a8e21fea40f74b43aa9dd4c381eac8f4776e3cfd`.

### Review, integration and limits

Independent review of all ten public files and PR body, clean exact-tree
integration preflight and publication identities are recorded in the handoff
when completed. Keep the candidate draft pending maintainer current-head review
and integration disposition. No external GitHub approval event is claimed.

No compiler source/tests, runtime enforcement, authentication policy, provider
credentials, permissions, signing custody or trust roots changed. Hosted
capability projections retain their explicit lack of grant/provider-trust,
proof, canonical and release authority. No compiler semantic suite, conformance,
release, self-hosting, seed retirement, dependency readiness or overall
completion percentage is established. All five outcomes remain open. Effects,
grants and signatures remain distinct; Build/Release retain signing and custody.
