# Cold quoted-data metadata retirement

Planning: ACC-01/04/08/12. Accountable implementation owner: ACC-01,
thread `01a183dd-7208-7d33-a336-7483fd90e358`. Parent: PR616 at
`2f2f8f701f19660dad89e98f351693669a97980b`, tree
`e21649ac124613802ad024edce7df203176f32e8`. This finite three-owner repair
completes an omitted metadata dependency of the quoted-data successor.

## Source mismatch and contract

PR591's unmetered `sh08-qd-products` and `sh08-qd-products-valid-data` no longer
emit a work count. The C7 cold-evidence builder still copied that absent value
into `:quote-work-count`. Both Effects and emitter cold-metadata consumers
required that field to be an integer and charged it in a cost inequality.
Those source contracts disagree on the stacked successor. Source inspection is
not a claimed observed runtime failure; genuine private inference remains held.

The current [accounting removal rule](../../docs/development-verification.md#remove-project-cost-and-budget-mechanisms)
removes this orphan accounting field and its dependent admission requirements.
[C7](../../docs/phase-06-compiler-architecture/086-c7-type-checker-design.md),
[C8](../../docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md)
and [C14](../../docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md)
preserve actual source/type/effect relationships. [D9](../../docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md)
requires coherent producer/consumer evidence and claims bounded to real results.

## Exact three-owner changes

Remove only the `:quote-work-count` entry from `sh08-is-cold-evidence`. The
complete result matches historical PR502
`6a4f87f013d0076b81aba9e0667650e70d28e635` exactly.

In `body-is-cold-metadata-valid?` and
`p15-s23-is-cold-metadata-valid?`, remove that field from the exact schema,
remove it from the integer/nonnegative operand list, delete its quote-only
count bound, and remove its term from the existing lower-bound expression.
All other expressions are unchanged. These consumer changes are current-contract
repairs: their historical definitions still required the retired field, so only
the C7 producer receives complete historical-form correspondence.

Mode, exact cut, actual argument-call IDs, closed metadata shape and other
current dependencies are preserved. Any obsolete `:quote-work-count`, including
zero or nil, remains an unknown-field rejection. No replacement counter,
unbounded sentinel or larger constant is introduced. Remaining preparation,
round, aggregate-work and other inference accounting is unchanged and remains
open; this increment does not endorse those requirements.

[The census](cold-quote-metadata-census.edn) pins all three complete functions,
exact consumer expression changes, unchanged argument/caller lists, unchanged
quote-product functions with no work output, and every other production form.
Complete consumer bodies correspond under explicit helper-name aliases; no
helper semantic equivalence is inferred from those names. The field has no
remaining production reference in tracked Gravity or host implementation.

## Actual-owner control definitions

Three added kernels use the actual C7 cold-evidence producer and the two actual
consumer metadata validators. The shared fixture is normalized helper DATA,
not an authenticated Core or a private function proof. Existing non-quote fields
satisfy the unchanged legacy interface; their fixture values introduce no new
accounting policy or closing evidence for those remaining mechanisms.

The C7 kernel requires the exact preserved metadata without the retired field
and unchanged output when old quote-work values differ. Each consumer kernel
requires the fresh C7-produced metadata to pass, and eight candidates to fail:
obsolete field 0/1/nil, changed mode, changed cut, forged source argument IDs,
missing semantic field and an unknown field. All existing test bytes and
private/indexed-self source/cut forgery controls remain unchanged.

These three kernels are UNRUN. They do not establish actual source-derived
private inference, effect legality, typed lowering, native callbacks, subject
runtime values, provenance or authentication.

## Observed checks and remaining acceptance

The pinned local Clojure checker passed complete historical C7 form equality,
explicit consumer-only substitutions, all other production-form conservation,
caller/argument conservation, unchanged quote-product forms, exact seven-key
metadata parity, complete consumer-body correspondence, eight invalid cases,
existing test-byte conservation and zero remaining production field references.
Reader evaluation is disabled and Bootstrap remains unloaded. Complete source,
form/test pins and generator digest are in the census; exact scripts, invocation
and raw output remain local.

The boundary gate passes 6 tests/11 assertions. Contextual clj-kondo under
`tools/ci/kondo`, cache disabled, has zero errors, 223 warnings and one information
finding, the same per-file finding multisets as the exact parent across the three
changed test files. Canonical docs, full-language roadmap and whitespace pass.
No compiler, Native/provider, authenticated source, Effects/lowering kernel,
runtime, broker or held selector was launched. The earlier denied cache cleanup
remains stopped; existing local cache is untracked. No clean-worktree claim is
made.

Keep draft pending coordinator-routed independent source/result acceptance. A
separate parent-routed review of immutable PR616 is not duplicated or treated
as acceptance of this successor. The denied PR569 successor review remains
stopped and is not replayed or rerouted. Required finite evidence remains:

1. Execute all three actual-owner helper kernels and preserve obsolete-field,
   mode/cut/source/closed-schema positives and negatives with actual providers.
2. Revalidate genuine private/indexed-self source inference through C7, Effects
   and typed emission, including the existing source/cut tamper controls and
   actual runtime results where required by their owners. Normalized metadata
   success cannot establish this composition.
3. Consume applicable CI, independently review the exact repair and fresh main
   composition, and integrate only accepted identities through preflight.

All remaining private-inference accounting and original source/test obligations
stay open. No independent acceptance, public compiler/authentication support,
whole accounting completion, release, self-hosting, PR502 closure or expanded
PR532 acceptance is claimed.
