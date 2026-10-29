# Safety source-family task interface proposal

Scope: `requirement-reconciliation/phase-02-safety`, SAFE1-SAFE16 only.
Source/task base: `6276c94b0b753c888ef8fecc794bcc81245ea3b3`, tree
`9f464ed0f03866bd4978e4213a488f3b1ae48683`. The immutable shared input is
[PR554](https://github.com/burn2delete/uniform/pull/554) at
`53ae6a403b20d15155bb5b04911fe7ac0092b921`.

The [proposal](proposal.edn) preserves 16 original/current document hashes,
1,908 exact API occurrences, 962 inherited nonlayout dispositions and 1,055
facets. Its 119 inherited semantic records are trace records, not a new
requirement denominator or task population. All 757 acceptance-rule facets
retain their source bytes, occurrences and unit membership. The remaining
context/layout facets and all original negative conditions remain present.

The patch has 329 guarded field operations on existing requirement records:
primary task references, supplementary interfaces and stale task-gap text.
Each operation carries collection, ID, field path, predecessor presence/value
and replacement value. It preserves semantic predicates, aliases, external
contexts, atomicity, nonoverlap, source dispositions and historical receipts.
Later composed records may have different predecessors; report those conflicts
and compose explicitly, never force these replacements over them.

Fourteen existing acceptance records in Legality, Auth, Runtime, Build and
Domains now name concrete safety checks, rejection behavior and artifact
inspection. Their generated roadmap views are updated. Eighteen existing task
interfaces across seven components are referenced; unchanged Expand and
Workflow interfaces retain their actual scope. Task IDs, titles, Open statuses
and frozen historical task suffixes are unchanged. Task-reference order is not
a dependency schedule. The proposal records the actual typed/effected/Auth to
Legality, generated-code, Runtime, package and conformance handoffs.

All 119 assigned-source task mappings are proposals for independent review.
Every implementation acceptance remains unassessed. Other-family refinements
on shared semantic records remain unassessed here; their IDs and original
records are preserved. No missing interface is hidden by deleting a source row.

The exact inherited qualification facets remain:

- `safe5/linear-states/block-3/slice-2712-2760`: lease renewal and terminal cleanup.
- `safe9/requirements/rule-3/slice-970-1068`: mandatory checked/proven narrowing.
- `safe9/casts-and-conversions/block-2/slice-3834-3901`: explicit wrapping/saturating conversions.

The delivered reviewed Safety qualification/atomicity compositions and later
D8/D9 decisions are separate pinned inputs listed in the proposal. This patch
does not redo, reverse or confer acceptance on them. Their central composition
must retain safe public API versus internal-only unsafe boundaries. No latest
all-project composed manifest is asserted.

The historical Build yank-interface artifact remains byte-identical. Its test
checks its original exact task bytes at the frozen base and rejects current
changed task bytes. This successor's task pins are in the proposal. Global SH
crosswalk and migration source pins are not rewritten by a family proposal;
central adoption must reconcile their changed task-source identities.

## Validation and consumption

The [Clojure checker](check.clj) evaluates only the three pure definitions from
the exact `c0dc5790197a1bbd3a8aa285e5458c272d877220` occurrence API. It does not
load the original compiler/evidence entrypoints. Supply the preserved applying
manifest as `MANIFEST`; its required SHA256 is
`aa7ee8dfa7c7b0b7a9cb0e609944d7dcbcd81791f080708364a0faf8cc2e7980`.
The committed PR445 manifest has different bytes and is refused.

```bash
clojure -J-Xmx512m -M specs/requirement-reconciliation/phase-02-safety/check.clj --check MANIFEST
```

The positive construction and ten negative controls pass: missing/renamed task,
wrong marker/task hash/source hash/occurrence, lost facet or unit, alias with a
different context, stale predecessor and duplicate application. Field edits
cannot mutate semantic/alias collections or unrelated requirement fields.
Checks verify raw UTF-8 spans, all original memberships, current task pins and
exact predecessor values. They do not establish semantic acceptance.

Focused projection suites pass: Legality 4 tests/59 assertions, Auth 4/61,
Runtime 4/67, Build 5/80 and Domains 6/103. Language boundary passes 6/11;
document validation passes 240 documents/19 phase indexes; full-language
roadmap, generated views and whitespace checks pass. Initial projection
fixtures omitted acceptance-field link targets; their copy inputs now include
both evidence and acceptance links. No compiler, runtime, broker or historical
heavy suite ran.

Next adoption step: the canonical reconciliation owner routes independent
review of the immutable candidate and resolves exact later predecessor
conflicts before adopting this family patch. Review, canonical adoption,
integration and implementation acceptance remain pending. No overall count,
rate, release, self-hosting or seed-retirement claim is made.
