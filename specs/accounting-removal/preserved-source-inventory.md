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

## Imported producer and main-census follow-up

PR621 at `6c8d70290a52789ba57d5f7c95e5c700cc7ce89c`, tree
`f2b5fbcc0a7fa8b2d4ac70cccad72fe874f62f2d`, parent PR618 `3a1a553d`, restores
the complete original imported-producer validation kernel and comments byte
for byte. Production and all prior tests remain unchanged. Four actual
entrypoint forms match history. Nine immediate source pins explicitly retain
the producer core-invalid wrapper difference. The preserved 4097 requests,
16777217-character DATA carrier and authentication rejection distinguish DATA
from genuine producer authority. Source checks, boundary 6/11, unchanged lint
71 warnings/zero errors, docs/roadmap and whitespace pass. Genuine execution,
independent acceptance and composition remain pending.

PR619 prior head `ab33cc5f` now has independent changes-required-census-only
receipt SHA256
`4e9f49959c5e296ac74d0be826a4a0df9c56f740a24a8838c016c63c60e47993`: 5 controls,
127 passing assertions, 1 failure, no errors. Production-source disposition
passes for that exact main extraction; its census counted 28 instead of 29
newline-terminated deleted lines. Corrected successor
`8330e46870de055f3ad4c864921dc83b999d4a06`, tree
`6606e4cdd541156117589399ff5d9fb861ff8529`, changes only the count to 29 and
the regenerated checker digest. Production, all other census values and the
main documentation remain unchanged. Separate correction/source checks,
docs/roadmap and whitespace pass. Original review history is preserved;
independent successor disposition, exact-head CI and fresh-main integration
remain pending. Immutable inventory counts and snapshots remain unchanged.

## Recursive type control follow-up

Main-based test-only PR622 at `47a0f75617aff8da5136e084b837b4f853190433`,
tree `d9888ed279ddf2c99acbd3272bd6b74aa61fd051`, base main `e047157d`,
restores the complete original List-construction unmetered-types kernel,
raw body/comments included. It also restores the original depth-192
Symbol-carrier positive alongside current depth-127/129 and late-invalid cases.
Width 4097, late invalid type at index 4096, nested Never, Map parity, extra
fields, cyclic maps and opaque zero-callback controls remain. Every other test
byte and all production are unchanged. Nine immediate source pins explicitly
retain five historical differences, without helper-equivalence or whole-callgraph
claims. Source checks, boundary 6/11, unchanged lint 71 warnings/zero errors,
docs/roadmap and whitespace pass. Genuine execution, independent acceptance and
composition remain pending. Immutable inventory counts are unchanged.

PR620 exact head `585c2dab` routine CI completed with seven successes and two
configured skips. Its genuine Record/Union helper execution and independent
acceptance remain open. The original child PR616 census has no removed-line
count field, so the PR619 line-count defect does not require a child-census
mutation; the reviewed child remains unchanged.

## Predicate helper correspondence supplement

The separate `predicate-helper-correspondence.edn` and Markdown supplement
consume the original six nonmatching helper keys without changing the original
record or inventory. At main `e047157d`, four complete source tails match under
explicit parameter/helper renames: Effects/lowering ordered-child helpers and
List-predicate ID helpers. Their three actual ID-validator bodies match in full.
All selected forms remain unchanged from the original `0f7acce5` assessment.
Ten source mutations are detected; the recursive-versus-loop all-true helper
difference remains explicit for both owners. Source facts confer no semantic
equivalence, genuine execution or independent acceptance. Full remaining
predicate derive/join/lowering/runtime/forgery/invalid-input obligations remain
open. Immutable machine inventory and original evidence counts are unchanged.

Corrected PR619 head `8330e468` and PR621 head `6c8d7029` each completed routine
CI with seven successes/two configured skips. PR619 successor disposition and
PR621 genuine execution/independent acceptance remain pending. Earlier live-CI
observations above are historical; these terminal results do not expand their
semantic scope.

## LC admission implementation and accepted census correction

PR623 at `c5e9fccdad293b34c290859cc3e4716870241dd6`, tree
`7b506c824110d5f88fc228bdf9b4c5999b35b3a7`, base main `e047157d`, removes
the three LC-source operand ceilings while preserving newer native/exact
validation. Its two complete historical C7 derive/step definitions remove
allowance inputs and output charges; the single inference caller and two direct
test callers migrate. Six production definitions change, with every other
production byte conserved. Existing semantic controls survive alongside three
new actual-owner width/late-invalid/source/schema kernels. Source checks,
original reader contract 4/167, boundary 6/11, zero lint errors/same 223 warnings
and one info, docs/roadmap and whitespace pass. Genuine execution, independent
acceptance, context/lowering/runtime composition and broader accounting remain
open. This follow-up does not recompute or credit immutable inventory counts.

Corrected PR619 exact `8330e468` received affected-successor PASS from the same
reviewer: receipt SHA256
`457ed321acbc06f14651c7bb872a6fe995b0ba7524be8324aae0a3a52e79807f`, 6 controls,
143 assertions, no failures/errors. `loop-main-review-consumption.edn` verifies
the receipt, candidate/tree, source/census pins and the unchanged original
failed receipt. The census finding closes only for that successor; the exact
production slice is reused with proven byte conservation. Fresh-current-main
applicability, integration preflight and integration remain pending. No parent
stack, PR569, live loop or whole-ACC acceptance is inferred.

PR622 exact head `47a0f756` subsequently completed routine CI with seven
successes/two configured skips. Its restored genuine recursive-type controls
and independent acceptance remain open.

## Actual current-main loop applicability and preflight

The separate current-main applicability record assesses reviewed PR619/8330e468
against observed main `e047157d` without changing the immutable inventory.
Conflict-free proposed merge tree `f26f96d713d8964246ca7809aee8dd573f4d8ca3`
retains complete reviewed C7 bytes, 1087 definitions, 30 loop functions, scoped
reference results and both reviewed evidence files. All other main tree paths
are unchanged; the six intervening main paths are disjoint. No commit/branch
mutation or integration occurred. Read-only preflight inspect completed with
all-pass false: divergent ancestry and the retained untracked lint cache prevent
integration eligibility. The earlier rejected cleanup is not retried, excluded
or worked around. Integration-mode preflight and coordinator disposition remain
pending. Source checks and docs/roadmap pass without compiler/runtime execution.

PR623 exact head `c5e9fccd` completed routine CI with seven successes/two
configured skips. This does not run the new genuine LC kernels or supply
independent source/result acceptance.

## Original Effects ordinary-local-call control follow-up

PR624 at `9f9dce89fc592050c4c44461522485cc49df0037`, tree
`350ddd4c3e76b3c428f881b1ec223dac3729082a`, base main `e047157d`, restores
the complete original ordinary-local-call fixture helper plus both List-to-Set
and public Map-key Effects composition controls. Four payment assertions,
diagnostic counter selection and two newly copied unused bindings retire; all
ordering, malformed/foreign/duplicate/missing-tag, coherent source/binding/caller,
evaluation and opaque zero-callback candidates survive. Production and every
preexisting test byte remain unchanged. Seventeen of twenty-two selected C7 and
Effects dependency forms match history; five differences remain explicit, not
semantic equivalence claims. Source checks, boundary 6/11, zero lint errors/same
76 warnings/one info, docs/roadmap and whitespace pass. Genuine Effects execution,
independent source/result disposition, API accounting and integration remain
open. PR533 PMK preparation, PR598 emitter controls and DSE acceptance retain
their own scope. Immutable source inventory counts remain unchanged.

## Original predicate actual-owner control successor

PR625 at `50c7e58c5fb41a3001bf423b39f6e5157e030566`, tree
`b9cc661dcfea7fe7859adbd4346e35541bf76c53`, base main `e047157d`, restores
the entire original predicate-family test file and actual-owner control. Three
raw edits migrate the two obsolete validation APIs and replace a retired work
counter with explicit obsolete-field forgery values 0, 1 and nil. All eleven
family entries and every other original byte remain identical. Actual C7
derive, complete C7/Effects/lowering descriptor equality, Effects contexts/joins,
typed lowering, runtime results, coherent forgeries, late invalid types and
opaque zero-callback controls retain definitions. All production/preexisting
tests are unchanged. Source integrity, boundary 6/11, zero lint findings,
docs/roadmap and whitespace pass; census pins 114 owner forms and 22 upstream
helpers. Genuine execution, independent source/result disposition and integration
remain pending. Original source-only records and immutable inventory counts
are unchanged; no semantic credit is inferred from this test restoration.

## Restored source-reader contract paths

PR626 at `bcf0bb4ba093a2634bf51b4541aa153040e333aa`, tree
`ef10f7d8d95dfe750955556afd2a85638f5f6a68`, base main `e047157d`, restores
the original private Map-route and List-construction files byte for byte. It
also restores the complete List-query file with eight recorded native-guard
correspondence/routing-parity edits and one additional guard/routing mutation
control. Candidate source-reader results pass 2/223, 4/167 and 8/276. The
unchanged original List-query is separately rerun: 7 tests, 257 passing
assertions, 5 failures, zero errors, NONPASS, with log digest retained. Adapted
results do not replace that failure. All production/preexisting test bytes and
source bookends are conserved. Boundary 6/11, zero lint errors/same one warning,
docs/roadmap and whitespace pass. Independent source/adaptation disposition and
genuine compiler/Effects/lowering/runtime/authentication acceptance remain open.
The immutable original inventory and prior evidence records are unchanged.

PR624 at `9f9dce89` and PR625 at `50c7e58c` each completed routine CI with
seven successes/two configured skips. Those catalogs do not run their new
genuine Effects/predicate controls or supply independent source/result
acceptance.

## Consumed exact cold quote-metadata source review

PR618 exact `3a1a553d`, tree `c8600e2c`, parent PR616 `2f2f8f70`, received
independent source PASS, receipt SHA256
`9892645f1804e7d9d9012dc5f69d90b693ca0aa0e5e8fea6759187b2acd16ec7`.
`cold-quote-review-consumption.edn` verifies the receipt, candidate/tree and all
eight source/test/census/document hashes. Three source/non-authoritative DATA
controls pass 88 assertions; genuine owner-helper kernels remain UNRUN. The
review preserves 2646 other production forms, closed metadata/source/mode/cut
checks and obsolete-field negatives. It confers no parent615/616, PR569, whole
accounting, authenticated source, runtime or current-main acceptance. Any future
main extraction requires its own identity, affected composition review, CI and
preflight. Original inventory counts and prior source assessments are unchanged.

## Consumed exact Record and Union source review

PR620 exact `585c2dab`, tree `09f8eff9`, base main `e047157d`, received
independent scoped-source PASS, receipt SHA256
`4abe17689fec09ca290dba77a1bea2d111c6428275d7808d8e536bf215913c40`.
`record-union-review-consumption.edn` verifies receipt/tree, four immutable file
pins, both raw original spans and their exact appended-file equation, plus all
31 deterministic current/historical C7 definition pins. The independent scope
preserves all old test bytes and 10068 other blobs/modes, leaves production
unchanged and retains six explicit helper differences; six negative source
controls refuse mutations. Two auxiliary reader-serialized complete-test-form
hashes are NOT independently endorsed because generated anonymous symbols are
process-dependent. Neither the review nor this consumption uses those fields
as independent identity evidence; raw spans and deterministic C7 pins carry
the source result. Genuine owner kernels, authenticated/executable Record/Union
behavior, current-main composition and integration remain pending. No transitive
helper equivalence or full ACC/PR502 acceptance is inferred. Immutable inventory
counts and snapshots remain unchanged.

## Main quote extraction, reader CI correction and further source reviews

PR628 exact `46e8f15a`, tree `98369ced`, extracts only PR618's three reviewed
definition intervals and five raw fixture/control forms onto main `e047157d`.
All other main bytes/callers survive. Main's metered quote-products and absent
valid-data helper remain explicit changed-composition boundaries; no parent
stack is imported. Source checks, boundary 6/11, unchanged lint multiplicities
and docs/roadmap pass. Independent main-composition review, genuine acceptance,
CI and preflight remain pending.

PR626 predecessor `bcf0bb4b` static job `123752212872`, run `40960649429`,
failed on the new List-query file's unused testing import (zero main baseline,
one blocking warning). Successor `70eba308`, tree `5adafde0`, removes only that
refer and refreshes the ninth exact adaptation/file hashes. Reader results
remain 14/666; original query NONPASS remains separate. Boundary 6/11 and the
unchanged strict lint checker pass with zero baseline/zero blocking findings.
The CI failure is retained and corrected-head automatic CI is separate.

Three new consumption records verify exact independent source receipts:

- PR621 `6c8d7029`, SHA256
  `45103e9648abf38fc0850f9e7fec0abbc22edee263358b43268318eeab5e9b02`: 3/69,
  nine dependency pins, original kernel/comments and parent test prefix. Large
  4097/16777217 cases were definitions only, not allocated or executed; PR613
  carrier acceptance remains excluded.
- PR622 `47a0f756`, SHA256
  `6acc15a62eea2cbf0837bc8309c5935e2671ca1fa7482df81a2e8da93e82cfbd`: raw
  List kernel/file conservation, nine C7 pins and seven refused source mutations.
  Symbol192 uses the current constructor; historical constructor/body equivalence
  and auxiliary reader-serialized test hashes are not independently endorsed.
- PR624 `9f9dce89`, SHA256
  `17c28f920d5268fc9ddb0e68d05b7e99f05b759d3944e5033accf2f9caf8bea4`: 3/60,
  six inert negative controls, 22 dependency pins/five explicit differences,
  shared helper and semantic candidate conservation.

Genuine kernels, actual Effects/producer/runtime composition, authentication and
main integration remain open. These source results do not clear any old denial
or confer whole ACC/PR502 authority. Immutable inventory counts are unchanged.

## Reconciled loop candidate and LC source review

PR619 now has normal reconciliation candidate `c5fc3c57`, tree `998aee8e`,
with parents reviewed `8330e468` and current main `7a74f6e3`. The fresh
applicability record proves exact precomputed merge-tree identity, main ancestry,
complete reviewed C7/evidence bytes, 1087 definitions/30 loops and conservation
of every other main tree path. Boundary 6/11 and docs/roadmap pass. Read-only
preflight passes ancestry/expected identities but retains clean-worktree/all-pass
false for the same untracked lint cache. No cleanup, exclusion, alternate
checkout or main integration occurred. Old reviewed commits/receipts remain
history; fresh independent composition, current-head CI and coordinator
integration/preflight remain pending.

PR623 exact `c5e9fccd` source-only PASS receipt SHA256
`94f70e5279164d8f10ddcced4d0bc8301bc59c2e9c9a30e04259101890e01a86` is
verified in `list-construction-review-consumption.edn`: tree/seven input hashes,
4 reader tests/167 assertions, nine refused altered-source controls, coherent
header/derive/step/caller changes and retained newer native/exact guards. Genuine
owner kernels, fresh main composition and integration remain pending. Original
inventory counts and source-only authority limits remain unchanged.

## Exact main-based cold metadata source PASS

PR628 exact `46e8f15a`, tree `98369ced`, base `e047157d`, received independent
affected-main SOURCE PASS, receipt SHA256
`a939654e46b3d1b1b16b4294c5b4d67d65606827cf748568899c8a29e790ba3d`.
`cold-quote-main-review-consumption.edn` verifies receipt/tree/eight file pins.
Three inert source/non-authoritative DATA controls pass 121 assertions; eight
metadata negatives and eight altered-source guard/schema cases reject. Exact
three definition intervals/five raw added forms, all other main bytes/callers
and six consumer dependencies survive. Main's quote-products remain metered and
the stack valid-data helper remains absent, never called by copied controls.
This is metadata retirement only; genuine private/Effects/lowering/runtime
controls remain UNRUN. Exact-head CI has seven successes/two skips, but changed-
main applicability, lifecycle and fresh clean integration preflight remain open.
Corrected PR626 `70eba308` also completed seven CI successes/two skips; its old
static failure and unchanged original query NONPASS remain history. Original
inventory counts and global ACC/PR502 authority remain unchanged.
