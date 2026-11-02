# Implementation plan: Reader task authority

Spec: [spec.md](spec.md) | Owner role: Reader maintainer.
Input revision: `c40d74aded3a019ff2bdeb1f4050cf95dbb11c1e`.

## Contract alignment

Follow the [constitution](../../.specify/memory/constitution.md),
[development verification](../../docs/development-verification.md),
[compiler architecture phase](../../docs/phase-06-compiler-architecture/README.md),
and [owning contract](../../docs/phase-06-compiler-architecture/081-c2-reader-implementation-design.md).
Only the editable implementation task location changes, not language semantics
or authoritative compiler migration.

## Canonical work and handoffs

- Implementation work: [the seven authoritative records](tasks.md#implementation-tasks).
- Compatibility view: [generated table](../../docs/component-roadmaps/reader.md#finite-remaining-work).
- Release criteria: [owning acceptance checklist](../../docs/component-roadmaps/reader.md#acceptance-checklist).
- Overall composition: [integration outcomes](../../docs/component-roadmaps/README.md#integration-outcomes).

Reader owns source bytes and lexical/seed origins; Syntax owns syntax context
and hygiene. Reader can begin with explicit source and policy records; Build
completion and final self-hosting are not start prerequisites. Existing
interface decisions remain with the Reader/Syntax maintainers. Record order is
not dependency evidence. No additional dependency graph is manufactured here.

## Approach and exact files

Edit only the marked implementation region in tasks.md for task progress.
Each record has one stable semantic ID, title, checkbox, exact evidence/status
and exact yes/no acceptance text. The known seven-ID set guards accidental row
loss or substitution; an intentional task-set change requires explicit review
of that set, not positional renumbering. Titles cannot collide.

The Clojure projection tool copies those three text fields without interpretation
into the marked table region in the Reader roadmap. Everything outside that
region is retained, including historical receipts, release criteria and anchors.
The historical scaffold record stays frozen outside the task region and is not
counted as implementation work. No independent machine-readable status ledger
is added.

## Validation and evidence

Run from repository root:

```bash
clojure -M tools/reader_task_projection.clj --write
clojure -M tools/reader_task_projection.clj --check
clojure -M tools/reader_task_projection_test.clj
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -M tools/validate_gravity_docs.clj
clojure -M tools/validate_full_language_roadmap.clj
```

`--check` is read-only. `--write` validates the complete source first and changes
only the generated roadmap region. Missing/duplicate records and region markers,
malformed records, checkbox/status disagreement and stale projections reject.
Compare the initial projected table byte-for-byte with the baseline table;
completion evidence must not change during this migration. Check `git diff
--check`. No compiler behavior tests or full migration acceptance are inferred
from these structural checks.

## Delivery and remaining work

Prepare an immutable candidate for independent review. Record observed checks
separately from review and integration. Future real implementation changes
update the authoritative record and regenerate the compatibility table together.

## Names, callers and compatibility

Preserve existing semantic IDs and roadmap anchors. The dashboard can continue
extracting the same three-column table without changing task status or counts.
It consumes a generated view; the editable source is tasks.md. No runtime API,
load order, manifest, hash, schema or protocol identifier changes.

## Context-independent projected links

Task titles, evidence/status and acceptance fields support plain text and
inline links of the form `[label](https://host/path#anchor)` only. Use the
immutable GitHub commit/blob URL for a receipt, not a source-relative link.
Both Spec Kit and the generated roadmap then resolve to the exact same target.
Relative, root-relative, fragment-only and protocol-relative links, reference
links, images, raw HTML, autolinks, escaped Markdown and link-title syntax are
rejected rather than guessed or silently relocated. This structural check does
not fetch the URL or establish that its claimed evidence is accepted.
