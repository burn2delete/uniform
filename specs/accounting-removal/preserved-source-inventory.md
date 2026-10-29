# Complete preserved-source comparison

Planning: ACC-01/12. Owner: ACC-01,
thread `01a183dd-7208-7d33-a336-7483fd90e358`. This is a source inventory for
the existing 29-path reconciliation, not a second outcome ledger or acceptance
receipt. No implementation or canonical ACC status changes in this increment.

## Exact inputs and coverage

[The machine record](preserved-source-inventory.edn) compares the entire authored
path set from `cfb499405450d479955fa70daa0f14dfa3233ee8` to preserved head
`6a4f87f013d0076b81aba9e0667650e70d28e635`. It asserts exact equality with the
29 original paths in `preserved-paths.md`. Current main is pinned to
`95ef84594f51862df0f5a7400ce5a988bbfdedd1`; the finite ACC-01 stack is pinned to
PR599 head `b786b464a67e33af2b75bfcf90a87619b71774c3`. Nine draft heads and
their exact reported bases are also pinned, including the existing DSE/PMK and
inferred-insertion owners. A reported base is a comparison input, not an
independent assertion of the draft's complete admitted scope.

All paths are represented:

- Three production Gravity files and fourteen Clojure test files have complete
  top-level reader-unit inventories. There are 530 original changed units,
  including 316 in the three production files.
- Ten fixture paths retain exact original-byte comparisons, same-path presence
  and explicitly named `preserved/` copies across the pinned heads. They do not
  receive semantic reader, typed or runtime acceptance from byte matching.
- The two accounting Markdown inputs retain whole-file identities. Their
  historical/canonical semantic disposition remains in the existing path table;
  this generator does not analyze Markdown meaning.

Every file observation includes a byte digest. Reader observations also pin the
ordered unit-key sequence and total unit count. Named units preserve their form
head and name; unnamed forms preserve their complete digest and occurrence count.
Only original added/removed/modified units appear in the per-unit delta table.
No original changed unit is selected by a family-name heuristic.

## Method and observations

Clojure reads forms with reader evaluation disabled in the same inert namespace.
No namespace or definition is evaluated. Authored spellings that could be confused
with reader-generated anonymous symbols are rejected by the analysis method;
only actual reader-generated anonymous symbols are canonicalized. Numbers,
keywords, argument lists, assertions, schema values and function names are not
normalized. Duplicate named units fail the inventory rather than disappearing
through map replacement. Comments/formatting remain covered by file-byte pins,
not by reader-form equivalence.

The production observations are:

| Current observation | Main | ACC-01 stack |
| --- | ---: | ---: |
| Complete historical reader form | 28 | 92 |
| Historical removal also absent | 5 | 9 |
| Complete authored-base form still present | 58 | 37 |
| Same-name form absent | 113 | 57 |
| Other complete form difference | 112 | 121 |
| Total original changed production units | 316 | 316 |

These are observations, not completion totals or percentages. An exact form can
still have different dependencies, namespace meaning, provenance or execution.
A stronger successor can deliberately differ from the historical form. Absence
can mean a renamed successor, retired API, or genuinely missing implementation;
it cannot establish rejection or incorporation by itself.

The record therefore pins the existing premise-test relocation, List-query
adaptation, predicate correspondence and DSE census artifacts. DSE has renamed
`types-set-equality-*`, `effects-set-equality-*` and lowering definitions; many
old same-name entries remain absent while that owner carries the actual source
route. Retired `lc/pl/lr-type-work` spellings similarly require their recorded
semantic API mappings, not recreation. No names are normalized away in this
inventory to manufacture equality.

For each original unit, draft entries report only that the candidate form differs
from its exact reported base and its literal correspondence to the historical
unit. A draft touching a large function does not dispose of all original changes
inside it. This record grants no owner, review or integration authority.

## Concrete next work and open decisions

The 37 stack units still identical to the authored base and the other differing
units provide exact next-inspection identities. Existing owner routes must be
checked before source changes; the census is not permission to duplicate DSE,
PMK, inferred-insertion, Native or held runtime work.

A focused source check confirms one finite unported accounting delta:
`sh08-st-source`, `body-st-source` and `p15-s23-st-source` each still contain one
`:operand-type-work` field in the `:gravity.type/applied-set-conversion`
descriptor. Removing only that field from each current complete definition gives
the corresponding complete historical definition. No implementation is changed
here. The next implementation increment must trace actual descriptor consumers
and preserve positive reconstruction and obsolete/forged-field rejection across
all three owners; it cannot infer acceptance from this textual correspondence.

Other authored-base identities include the shared C7 carrier preflight and its
aggregate helpers, coordinator/receipt/source validation, conditional List-element
checks in Effects/emitter and selected schema/caller cells. Each requires its
actual source/contract/caller disposition. The machine table names every unit;
this prose does not replace that work or assign broad completion credit to a
family label.

## Validation and limits

The pinned local Clojure generator completed the full inventory with Bootstrap
unloaded. Its first attempt had an unmatched delimiter before completion; the
corrected run and subsequent input-pin update completed. The generator and exact
invocations remain local, and its digest is in the machine record.

Integrity checks cover one complete EDN record, ASCII, exact 29-path conservation,
unique unit keys, digest shapes, state totals and all ten fixture byte-copy
identities. The separate three-definition check confirms the exact Set descriptor
field difference above. Document/roadmap and whitespace checks passed.

No Gravity/compiler, authenticated pipeline, Effects/emitter, runtime, broker,
C12 or held selector ran. This inventory does not classify every cost guard,
prove a call graph, renew an old review, confer independent acceptance or close
PR502. Independent source dispositions and genuine finite acceptance remain open.
