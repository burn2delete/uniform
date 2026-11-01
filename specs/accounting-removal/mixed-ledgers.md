# List-to-Set and Map-key mixed-ledger successor

Planning: ACC-01/04/05/09/12. Accountable implementation owner: ACC-01,
thread `01a183dd-7208-7d33-a336-7483fd90e358`. This finite PR502 successor is
stacked on PR595 at `99893151b3d0b7c3c435d5c2e6e90c03255b5951`, tree
`67c0a515f30c26b61fc6f0bc06d37f2ed28bd107`. It covers the two preserved
mixed-ledger test files and four corresponding emitter census functions.
It does not port DSE, change runtime providers or close an accounting criterion.

## Governing contracts and source change

[C7](../../docs/phase-06-compiler-architecture/086-c7-type-checker-design.md),
[C14](../../docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md)
and [D9](../../docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md)
require actual source/type facts, complete reconstruction, tamper rejection and
provenance appropriate to the claimed result. The original C7 ordinary-call row
and fact producers omit their optional producer/dispatch tags. Their absence is
not itself a malformed ordinary entry; present non-keyword tags remain invalid.

The emitter's List-to-Set and public Map-key row/fact censuses restore the
preserved missing-tag defaults from PR502 head
`6a4f87f013d0076b81aba9e0667650e70d28e635`. A default alone would skip records
whose owned-family routing tags were stripped. This is the same source pattern
that the separate DSE review found insufficient; it is not reused as acceptance.

Owned rows are also identified from reserved projection, dependency kind or
constraint-request domain. Owned facts are also identified by their applied
payload field, known descriptor ID or fact-request domain. Classification does
not confer acceptance: the existing unique-ID checks, fresh source reconstruction
and complete canonical row/fact equality still apply. Foreign-family entries
remain outside these specialized census owners. Private Map-key routing and DSE
implementation are unchanged.

The new semantic helper `typed-ledger-record?` admits native Map/default-comparator
TreeMap records with legal extents, no host metadata and Keyword keys. It checks
the native header before enumeration or lookup, so opaque/foreign carriers and
supplied opaque keys are not used as lookup authority. It introduces no size,
depth, work or scalar allowance. Exact actual native-header/callback acceptance
remains required; source inspection does not establish it.

Only five emitter definitions change. Undoing the listed default, classification
and native-record guard substitutions gives the complete previous four census
bodies. Thus their existing reconstruction, arithmetic, accounting and diagnostic
expressions are preserved, not replaced by a host model. Remaining lowering
allowances are historical implementation debt, not completion credit for this
semantic increment. No accounting mechanism or larger constant is introduced.

## Preserved tests and explicit adaptations

The two original `sh01_*_mixed_ledger_test.clj` files are restored as semantic
successors. Their existing names/namespaces retain historical compatibility and
path identity; the new helper name describes its behavior without a planning ID.
Both original positive bodies and all helpers match the preserved source. Tests
still construct actual C7 ordinary and collection rows/facts, combine both row
orders and vector/Map fact carriers, and check the exact expected lowering value.

All original malformed-input assertion expressions and all fifteen original
candidate mutations per file remain. Each file adds eight duplicate/orphan
mutations with one or both identifying fields stripped, plus four native-record
metadata/opaque-key mutations. The existing zero-callback assertion remains.
No new test is called a passing semantic result.

The payment-only middle test is replaced by its retained `:complete` status
assertion over the same mixed candidate. Exact debit, exact-budget and one-short
refusal assertions are retired. Existing budget arguments in test adapters are
required by the unchanged current lowering ABI, not new test requirements for
payment or work telemetry.

[The census](mixed-ledgers-census.edn) pins historical/base/current source hashes,
all four census definitions, the complete native guard and both test files.
Source checks preserve all original positive bodies, helpers, negative assertions,
original candidate order and the middle test's semantic assertion. They also
verify the sixteen added stripped-tag and eight native-record cases. Anonymous
reader symbols alone are canonicalized for assertion comparison; constants,
keywords, types, argument lists and schema fields are not normalized away.

## Observed checks and finite remaining acceptance

Pinned local Clojure source validation passed with reader evaluation disabled,
unique definitions, exactly five emitter changes and complete reconstruction/body
conservation outside the listed substitutions. Its initial test-record definition
had a delimiter error before completion; corrected validation passed. Both
observations remain local history. The exact invocation and generator are retained
locally; the generator digest is in the census.

Contextual clj-kondo using `tools/ci/kondo` reports zero errors and warnings for
both restored Clojure files. The language boundary passed 6 tests / 11 assertions;
canonical docs passed 240 documents / 19 phase indexes; roadmap and whitespace
checks passed.
No Bootstrap, Gravity/compiler, authenticated pipeline, Effects/emitter execution,
runtime, broker or held selector was launched locally.

Keep draft pending coordinator-routed independent source/result review. No denied
review or stopped operation is replayed, transferred or routed through this draft.
Genuine acceptance remains UNRUN:

1. Run both restored namespaces through actual current C7 production and emitter
   reconstruction. Check every ordinary/collection row order, both fact carriers,
   exact lowering values and complete positive row/fact selection.
2. Exercise all original malformed/forged controls and the new stripped-tag
   duplicates/orphans. Confirm missing optional ordinary tags succeed while present
   malformed tags and any reserved-family incomplete/duplicate evidence reject.
3. Run actual native-header checks for metadata, foreign carriers, opaque keys,
   malformed extents and zero callbacks; verify the whole record before lookup.
4. Compose the typed consumer with the affected genuine source/Effects/runtime
   paths when authorized, retaining current source and diagnostic identities.
   These normalized kernels alone do not establish source admission or runtime.
5. Consume applicable CI and accepted dependencies, independently review changed
   current-main composition and integrate only exact accepted identities. All
   broader lowering accounting and separate runtime/DSE acceptance remain open.
