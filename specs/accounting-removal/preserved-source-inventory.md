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

The later [dispatch/direct-call source map](dispatch-source-correspondence.md)
pins twelve historical units and two exact inline helper-body proposals at
assessed main `98c391a71c69b8086f15053319de123ee8cbf7fc` and existing DSE PR568
`f4c9fdcd`. It distinguishes complete alias correspondence, two guard changes,
three whole fallback correspondences, an additional selector difference and
the remaining caller decomposition. No guard/schema/argument value is
normalized away. All twelve selected DSE candidate forms remain identical to
the existing `ec085f5f` source-only reviewed input. The new mapping and inline
incorporation proposals remain pending independent disposition; structural
call paths do not prove semantic reachability. The original machine inventory
and its counts remain unchanged rather than silently treating renamed DSE
functions or inlined helpers as absences or completed work.

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

The two List element width checks below now have standalone follow-up draft
PR611 at `edef2c00c103df4b5222cf44124c6e1d707820e4`, tree
`7f12fa541cf26179ac59b6bdb95c666674ecdfb2`, assessed base
`e9ec1c599614a99e3779b5e3a329e8d9a4b8fa80`. Its complete two historical
definitions match after only the width predicate deletions; all other production
forms and direct callers remain unchanged. Actual-owner positive/late-invalid
and schema/extent test definitions are added, with all existing test bytes
preserved. Source checks, boundary, lint comparison and document checks pass;
actual execution and independent acceptance remain pending. Their work
parameters/charges/refusals remain explicit open obligations. This follow-up
does not rewrite the original machine snapshot or grant whole-function credit
to a later caller or composition.

The shared C7 carrier preflight and its four obsolete aggregate/max helpers now
have source successor PR613 at `5ebff5cae276ddd93b002a28806a1c363687f7c9`,
tree `aa6a3d0d21905452338262c48dce94e2b9b8dce3`, parent PR599 `b786b464`.
Its source census pins the historical preflight/public imported-result wrapper,
four historical absences, three orphan policy deletions and five unchanged
native dependencies. All eleven direct callers and exact diagnostic/test/coverage
adaptations are accounted for. Actual native/late-invalid/callback/producer and
source-coverage execution, independent acceptance and composed integration remain
pending. Remaining structural/core/inference limits are explicit open work; the
original machine snapshot and its state totals remain unchanged.

The three physical check/receipt/coordinator guard-order definitions now have
source successor PR615 at `bb3a9acfbb5dd683d6df0403867a179a5fdc51bc`, tree
`4c78e092a77a7dac53732d5bf0439d5fdf964493`, parent PR614 `c506514d`.
Complete historical forms, two exact guard-only reorderings, unchanged current
catalog/native provider/dependencies and caller argument lists are pinned. Both
original carrier/receipt physical-entry kernels are restored, with every prior
test byte conserved. A third kernel adds physical duplicate, hostile key,
malformed native map, metadata/comparator and true-only/catalog controls.
Genuine execution and independent acceptance remain pending. Earlier coordinator
equality/authority checks are unchanged; receipt DATA establishes no fresh
receipt, witness or B47/source authority. Original machine counts are unchanged.

Other authored-base identities include remaining C7 source validation,
conditional List-element work in Effects/emitter and selected schema/caller
cells. Each requires its
actual source/contract/caller disposition. The machine table names every unit;
this prose does not replace that work or assign broad completion credit to a
family label.

Cold quote metadata now has coherent source successor PR618 at
`3a1a553dbb1b3eb0ae472f5bf806171c02a71109`, tree
`c8600e2c52f4ba47f1366fae4035248d23365623`, parent PR616 `2f2f8f70`.
The complete C7 builder matches historical source. Its Effects/emitter consumer
repairs are explicit current-contract edits because their historical forms
still required the retired field; they are not labeled historical equality.
Caller/argument lists, other production definitions, source/cut/mode identities
and all existing test bytes remain unchanged. Three actual-owner helper controls
cover fresh metadata and eight invalid cases. Source checks/boundary/lint/docs
pass; genuine execution and independent acceptance remain pending. Other
inference accounting stays open, and original machine counts remain unchanged.

The unused `sh08-lsi-row-index`/`sh08-lsi-target-rows` pair now has retirement
draft PR616 at `2f2f8f701f19660dad89e98f351693669a97980b`, tree
`e21649ac124613802ad024edce7df203176f32e8`, parent PR615 `bb3a9acf`.
Both historical absences match; namespace/exports, all other production code,
tests and all 30 remaining loop-family definitions remain unchanged. The pinned
source/tooling reference scan finds no external caller or remaining reference.
Exact child source review subsequently passed as recorded below. Fresh composed
reference checks and main acceptance remain pending;
current loop validation/accounting and genuine results are not accepted by this
unused-helper deletion. Original machine snapshot/state counts are unchanged.

Independent PR616 source review now passes only that exact child deletion and
census, receipt SHA256
`c91616d8e3279d871583f174398f00b649173384c1343d199038df8cd38e6bee`, reviewer
`01a18529-dad4-7de1-b68d-3e567c8976a3`, 3 controls/89 source assertions.
Receipt/source/census identities were verified. It accepts no parent dependency,
main integration or genuine loop result and does not revive the denied review.
The fresh-main extraction is PR619 `ab33cc5fbf97bf6d9b6160ba5e54dae0d7c71acc`,
tree `b53719628047069dd3c64aba71b6ebd2160f9f3a`, base main40ecbb97. Its exact
removed byte interval matches the reviewed parent, with all other main bytes,
1087 remaining definitions, namespace/exports and 30 loop-family forms conserved.
Main-candidate independent disposition and integration remain pending. The
original machine inventory is retained unchanged rather than treating this
consumed child review as completed main integration.

Primitive uniqueness/node typing and the product-vector predicate now have
source successor PR614 at `c506514d4b32baf8269a7c3ff49ca86d2dc18c05`, tree
`ef9a09328663a93e213f65754c8caab882362c2d`, parent PR613 `5ebff5ca`. Its three
complete definitions and complete original structural-vector kernel match
historical PR502. External C7 source calls, arities, twelve transitive source
dependencies and all prior test bytes are conserved. An added state kernel
preserves observed-prefix, seeded/partial fact/type and first-failure behavior.
Actual execution and independent acceptance remain pending. The product-vector
predicate has no C7 source caller; its shape control grants no public artifact
admission. The original machine snapshot/state counts remain unchanged.

## Validation and limits

The pinned local Clojure generator completed the full inventory with Bootstrap
unloaded. Its first attempt had an unmatched delimiter before completion; the
corrected run and subsequent input-pin update completed. The generator and exact
invocations remain local, and its digest is in the machine record.

Integrity checks cover one complete EDN record, ASCII, exact 29-path conservation,
unique unit keys, digest shapes, state totals and all ten fixture byte-copy
identities. The separate three-definition check confirms the exact Set descriptor
field difference above. Document/roadmap and whitespace checks passed.

Follow-up draft PR610 implements that finite descriptor-field increment at
`2b70fac2fccdc4963e47d1a3cf0713cc970b155a`, tree
`e2534b8db74530208de9393e4d0de6d168c9b3dd`, standalone base
`8f9af1be55f99a076992086ac95f91364a22924e`. Its `operand-work.md` and
`operand-work-census.edn` pin three field-only production edits, unchanged
complete-equality consumers, two C7 absence/forgery controls and five runtime
metadata assertion replacements. Independent semantic checks are conserved;
payment and width-only refusal assertions in the two edited C7 tests are
retired. Source checks, boundary 6/11, docs and roadmap pass; lint retains the
base's same 147 warnings with zero errors. Genuine execution and independent
acceptance remain pending. The machine inventory above remains the original
main/stack snapshot; its counts are not recomputed or credited by this follow-up.

No Gravity/compiler, authenticated pipeline, Effects/emitter, runtime, broker,
C12 or held selector ran. This inventory does not classify every cost guard,
prove a call graph, renew an old review, confer independent acceptance or close
PR502. Independent source dispositions and genuine finite acceptance remain open.

## Original Record and Union control follow-up

Test-only PR620 at `585c2dab0fcc0a2d8d30ec988a3f3f7d5e151f53`, tree
`09f8eff9ff27422df1a321001b76d7f8f90fa287`, restores two complete original
Record/Union descriptor-row and union-branch-definition kernels on main base
`e047157d711652a5624012f0da7d6911ce086718`. Raw bodies and comments match
PR502 byte for byte. Existing test bytes and all production remain unchanged.
Its census pins 31 syntactic C7 dependencies, six of which differ from history;
those differences remain explicit rather than receiving equivalence credit.
Source checks, boundary 6/11, unchanged lint 71 warnings/zero errors, docs,
roadmap and whitespace pass. Genuine helper execution and independent acceptance
remain pending; CI was running at readback. These normalized descriptor/Core
controls do not establish authenticated source admission or executable
Record/Union support. The immutable inventory snapshot and its counts remain
unchanged.

PR619 at `ab33cc5f` and this evidence draft's prior head `9df7b2d3` subsequently
completed routine CI: seven successes and two configured skips each. This does
not extend the exact-child PR616 source review to main or supply genuine kernel
execution.
