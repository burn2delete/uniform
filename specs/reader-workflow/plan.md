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
- Roadmap navigation: [sole task ledger](tasks.md#implementation-tasks).
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

The Reader roadmap links directly to these records. Historical receipts, release
criteria and anchors remain on the roadmap; the historical scaffold record stays
frozen outside the task region and is not counted as implementation work. No
independent machine-readable status ledger is added.

## Validation and evidence

Run from repository root:

```bash
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -M tools/validate_gravity_docs.clj
clojure -M tools/validate_full_language_roadmap.clj
```

Check `git diff --check`. No compiler behavior tests or full migration acceptance
are inferred from these structural checks.

## Delivery and remaining work

Prepare an immutable candidate for independent review. Record observed checks
separately from review and integration. Future real implementation changes
update the authoritative record; the roadmap continues to link to that ledger.

## Names, callers and compatibility

Preserve existing semantic IDs and roadmap anchors. The dashboard follows the
editable tasks.md source without changing task status or counts. No runtime API,
load order, manifest, hash, schema or protocol identifier changes.
