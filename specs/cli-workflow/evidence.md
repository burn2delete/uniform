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
