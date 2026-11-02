# Private source-product successor

Planning: ACC-01/04/07/12/13. Accountable implementation owner: ACC-01,
thread `01a183dd-7208-7d33-a336-7483fd90e358`. This draft preserves the finite
GIS3/private source-product family from PR502. It is stacked on quoted-data
PR591 at `1c0d699f06372fe3c52fa003f53aa38ffd40d07c`, tree
`5daf1c0d0f690a4557c362fc7203ebe589570768`; that dependency supplies the five
native-DATA guard helpers. Neither draft is independently accepted or integrated.

## Governing contracts and source scope

[L5](../../docs/phase-01-core-language/015-l5-type-system-specification.md),
[C6](../../docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md),
[C7](../../docs/phase-06-compiler-architecture/086-c7-type-checker-design.md) and
[D9](../../docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md)
require actual source relationships, type facts, preserved evaluation order and
provenance appropriate to each claim. Similar trees or a compact witness do not
establish numeric correctness, source authentication or general compiler support.

The source increment restores 45 complete definitions from preserved PR502 head
`6a4f87f013d0076b81aba9e0667650e70d28e635` without body adaptation:

- 39 GIS3 definitions: original DATA admission and indexing; private entry/use,
  visibility and call census; original parameter/source/evaluation relationships;
  numeric-source selection; compact v3 product construction; candidate shape and
  carrier checks; fresh source reconstruction and complete product comparison.
- Five native comparison definitions: scalar-kind normalization, indexed arena
  construction, native DATA arenas, iterative arena comparison and candidate-first
  comparison. They reuse PR591's exact native header/metadata/cycle guards.
- The ordinary parameter-choice helper required by entry unification. Existing
  ordinary-inference callers are unchanged; their broader migration is not covered.

All existing parent definitions remain unchanged. External `sh08-*` dependencies
are present and their complete definitions match the historical inputs. The
restored internal names retain their historical `sh08-gis3-*` and `sh08-vf-*`
identities for source reconciliation; this is recorded legacy naming debt, not a
new naming scheme. No public routing, schema or diagnostic ID is renamed.

The candidate consumer independently admits candidate DATA, checks its closed
shape, reconstructs expected products from original source rows and compares
complete kinds and values. The implementation explicitly retains
`:numeric-proof-established false`. No public C7 routing, B47/coordinator
verification or certified numeric result is added or claimed.

## Preserved tests and fixtures

[The census](source-products-census.edn) binds the original and dependent base,
45 complete definition hashes, fifteen unchanged external dependencies, twelve
added test/helper definitions and four fixture copies. The nine tests are the six
preserved GIS3 source/index/entry/product tests and the three native DATA,
comparison and callback/cycle tests. Test names and body hashes are recorded.

The original interval/minimum `.gravity` and `.qst` fixtures are copied byte for
byte under `bootstrap/clojure/fixtures/self-hosting/sh-08/preserved/`. Original
accepted paths and bytes at PR502 remain historical inputs. Both pairs have equal
bytes; no reader, typed, emitted or runtime fixture acceptance follows. Test
fixture lookups select `preserved/`; namespace/source contents are unchanged.

The sole other test adaptation removes an unused `forms` destructuring binding
in the cold-entry test. Its `:as original` input still retains those forms, and
all assertions and mutations are unchanged. The source comparison explicitly
allows this one pattern and the fixture-directory strings; it normalizes only
reader-generated anonymous-function symbols. Numbers, keywords, schema values,
assertions and argument lists are not normalized.

Preserved controls include whole private caller/use coverage; source/span,
binding, evaluation and ancestry mutation; independent expanded product
reconstruction; coherent candidate field/kind/value tampering; unsupported
native DATA and metadata; duplicate Map entries; Map/Set ordering and carrier
kind distinctions; cycles, opaque nested DATA and zero callbacks. Large/deep
DATA and late source mutations remain in the test definitions. They are UNRUN.

## Observed validation and limitations

- `clojure -M /tmp/acc01-source-products-validation.clj`: 45 exact definitions,
  fifteen exact external dependencies, twelve test/helper forms, unchanged
  parent definitions, four historical fixture byte copies and pair equality
  passed. Reader evaluation is disabled and duplicate definitions reject.
  The generator is retained locally and its digest is in the census.
- `clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test`:
  6 tests / 11 assertions passed, with no failures or errors.
- Contextual clj-kondo using `tools/ci/kondo`: zero errors / 71 warnings for SH08,
  exactly the parent finding multiset. The initial check found one new unused
  binding; its local removal above resolved that finding without suppressing lint.
- Whitespace, canonical document and full-language roadmap validators passed.
  These do not execute or accept the new Gravity semantics.

An exploratory source inventory initially appended a Git read after shutting
down its Clojure agent pool, producing RejectedExecutionException. The corrected
read-only inventory passed; no compiler or target process was launched by it.
No Bootstrap, Gravity interpreter/compiler, broker, authenticated pipeline,
Effects/emitter, runtime, held selector or unknown historical operation ran locally.
Original passing and failing receipts retain their original scopes.

## Finite acceptance still open

Keep draft pending coordinator-routed independent source review and the applicable
execution assessment. The stopped PR569 successor review is not replayed or
rerouted through this draft. Required genuine acceptance remains UNRUN:

1. Run the nine exact preserved tests with actual current source and the existing
   native-header ABI, including real source/Core transport, original-entry
   reconstruction, coherent positive products and actual negative controls.
2. Check interval and minimum source products and independent expanded references
   under their declared source identities. Confirm both byte pairs and record the
   spelling that actually ran; do not label byte equality paired semantic execution.
3. Exercise candidate-first native validation, complete private fresh reconstruction,
   Map/Set kind/value distinctions, duplicate entries, metadata, malformed carriers,
   cycles and zero callbacks. Native arena helper success alone does not accept the
   public function-type verifier or the separate verifier fixture family.
4. Reconcile accepted parent and candidate with current main, consume applicable CI,
   review changed composition independently, then perform governed integration
   only with exact accepted identities. No main, release or self-hosting claim is
   made by this source draft.

Ordinary-function verifier/qualified-Map fixtures, general carrier quotas,
nil/List joins, mixed ledgers and other unique PR502 source remain separate.
PR502 remains open until every unique delta has its required disposition.
