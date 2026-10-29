# Domain source-family proposal

Owner: phase-09 source-family implementation owner, thread
`01a18529-4177-7882-b394-61e8fa73a1cb`. Date: 2026-10-28.
Scope key: `requirement-reconciliation/phase-09-domain-specific-computing-coverage`.
Independent review and canonical adoption: pending with the canonical owner.

The [bounded proposal](domain-family-proposal.edn) conserves DOM1 through DOM21,
original sequence 124 through 144. It is directly consumable field operations
and source evidence, not another implementation ledger. All 139 inherited
semantic records touching this family remain unassessed. The 32 proposed
task-interface bindings are partial accountable interfaces; 107 other records
retain explicit unresolved qualifications and stable IDs in
`:unresolved-stable-ids`. These are overlapping source-family records, not a
project denominator or an implementation completion count.

## Exact inputs and consumed decisions

Current source/task base: `6276c94b0b753c888ef8fecc794bcc81245ea3b3`, tree
`9f464ed0f03866bd4978e4213a488f3b1ae48683`. Frozen input package: PR554 head
`53ae6a403b20d15155bb5b04911fe7ac0092b921`; snapshot SHA256
`423dd6ec37f6d8f461de4a24d325033902ccce3249187073b796065fa3ca5d94`.
The proposal format is the existing PR445 format, SHA256
`69ec49fb954ba66ce86621d5dfac3a4cb0b514b84915277b86944ae2ec857adc`.

The applying semantic view is the previously independently reviewed Domain
composition, SHA256
`43ac4686b1aee03cdb8b97c634a429f6b538e1726eb280db6a8d939c33eec28d`.
It was reconstructed read-only from the exact `f1fddadc` predecessor and
`ea343b5a` composition receipt, checking every before record and the full target
hash. This consumes the delivered Language/Runtime/Domain/Schema normalization,
DOM19 admission correction and existing domain checker/lowerer distinction.
It does not restore retired compound parents or repeat those decisions.
Exact upstream artifact hashes are in `:preserved-lineage`.

This view is distinct from both the preserved applying manifest SHA256
`aa7ee8dfa7c7b0b7a9cb0e609944d7dcbcd81791f080708364a0faf8cc2e7980`
and committed PR445 manifest SHA256
`f4c171820dfdadaf7e7b5a3f04a701a124adfffd6cbaf6b598c1b82098aae690`.
Neither is adopted or overwritten. The canonical owner must compose later
decisions explicitly; stale or repeated field operations refuse application.

## Concrete task-interface changes

The four existing [Platform records](../platform-workflow/tasks.md) gain DOM1
through DOM4 source-backed positive/negative fixtures, artifacts and dependencies:
FIFO/CDC/HDL; sensor startup/MMIO/image; syscall/kernel audit/emulator; and
UART/DMA/device adapters. Their IDs, statuses, product scope and historical
records remain unchanged. No ISA, OS, hardware or application choice is made.

The existing [Workflow and UI records](../domains-workflow/tasks.md) gain the
missing DOM9 CRDT/coordination/offline convergence cases and separate DOM6
browser versus DOM7 mobile lifecycle/offline/permission cases. Their generated
roadmap views are refreshed with the existing projection tools. Other proposed
bindings reference existing task criteria; they retain partial-interface gaps.
The candidate changes six existing task blocks and creates no task IDs.

The broader storage-engine, native/simulation, crypto/private-computation,
blockchain, formal-proof, scripting and visual-programming interface residuals
remain explicit. A generic Math, Schema, Runtime or Workflow link is not treated
as entailment of those full contracts. Original diagnostic and rejection
facets, including provider noise/depth and selected chain protocol constraints,
are preserved. Removed project accounting is not reinstated.

## Observed controls

The proposal preserves 21 current/original document pins, 1,533 raw source
occurrences, 777 inherited nonlayout dispositions and 1,838 applying-view
facets. The extra applying-view facets relative to PR445 are consumed lineage,
not newly extracted obligations. The occurrence API at exact `c0dc5790` was used
for source occurrences. Raw UTF-8 spans/hashes and API identities are checked;
756 layout occurrences remain in the byte-conservation table.

Run with the exact composed manifest supplied by the canonical owner:

```bash
clojure -J-Xmx1g -Sdeps '{:paths ["bootstrap/clojure/src" "tools"]}' -M -m domain-requirement-patch EXACT-COMPOSED-MANIFEST.edn
clojure -J-Xmx1g -Sdeps '{:paths ["bootstrap/clojure/src" "tools"]}' -M tools/domain_requirement_patch_test.clj EXACT-COMPOSED-MANIFEST.edn
```

The focused controls passed: 3 tests, 22 assertions. They refuse stale/repeated
or duplicate field application, missing/renamed task IDs, wrong task hash or
marker, source pin/ID/facet loss, lost negative obligation, different-context
alias and unsupported acceptance promotion. Original record digests preserve
the applying historical receipt and contextual qualification boundaries.

Other passing checks:

- Domains projection: 6 tests, 103 assertions.
- Platform projection: 5 tests, 80 assertions.
- Documentation validator: 240 documents, 19 phase indexes, ASCII.
- Full-language roadmap validator.
- Language boundary: 6 tests, 11 assertions.
- `git diff --check`.

The initial focused check rejected the layout/nonlayout population mismatch;
the corrected check requires all nonlayout dispositions and separately checks
every raw source byte. No compiler, producer, runtime, broker or historical
heavy proof lane was executed. These controls establish source conservation
and patch mechanics, not semantic independent acceptance or implementation
conformance. Review routing and aggregate adoption remain with the canonical
owner; this candidate confers no release, platform-product, self-hosting or
seed-retirement credit.

## CI successor qualification

Original candidate `0199e14d0c60f46a7c8cf8f43a988eefeeb178e3`, tree
`64e77bccd76d7e952f8d6507097d8a22af5cbe43`, remains preserved. CI run
`40800225903` found an unresolved namespace in the new test loader and rejected
changed task-source spans. The successor uses a declared test namespace with an
ordinary required tool namespace and an explicit `-main` entrypoint; the focused
3 tests/22 assertions pass using the commands above. Local clj-kondo is absent,
so the corrected static check remains for CI.

The proposal, task and generated-roadmap bytes are unchanged by this repair.
The canonical owner confirmed the shared source-version successor is already
active in PR554 and must compose distinct family task amendments before
rebinding their spans. This family draft does not update shared SH mapping,
copy prior acceptance forward or weaken stale-input controls. Its source-span
CI rejection remains an explicit central-composition dependency; independently
reviewed semantic entailment and adoption remain pending.
