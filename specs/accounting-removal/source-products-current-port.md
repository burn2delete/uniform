# PR593 current-main port receipt

This receipt binds the finite private source-product reconstruction to the
current main source that was actually reviewed. It does not promote the
historical PR593 review record or a source fixture into C7/B47 authority.

## Current composition and evidence

The clean source checkpoint is `a49c5b8ef6cca27a7e1e71f840404227560b375b`,
tree `c604b7c22155b16833b2bb5a968cfb2bd7212df3`, over main
`e61d452c3ab5ebc2d989588f58b0a1c2651818ef`, tree
`2a66ff230e7befa2ad2266847163d20578424dc0`. It includes the hosted
source-product reuse change from PR844. The current-composition section of
`source-products-census.edn` records the complete current source-file byte
counts and hashes, and explicitly records the Reader/Syntax/Macro handoff
adaptation of `gis3-current-source-Core-products`.

The other census fields are historical source-review inputs. Their old whole
file and test-form hashes, static-only results and pending statuses are not
the current composition's identities or results. All 45 private definitions
and 15 external dependencies were independently checked for form conservation.
The separate metadata commit and integration preflight bind the later final
candidate without rewriting this clean source checkpoint.

After PR844, the real source-product control passed naturally: 1 test / 28
assertions, zero failures/errors, exit 0, recorded in
`/tmp/uniform-pr593-post844-source.log`. The four native DATA/index/comparison
controls passed 4/117 at `2969dc22fe6ea8b286b1a1943fe2168dc464d0eb`; their
owned source files are byte-identical at this checkpoint. Only the minimum and
interval `.gravity` files executed in the source-product check. Their `.qst`
companions were byte-compared, not independently executed.

## Historical port identities and ancestry

- current-main base: `f43e77992fe86b6dfb55df3516b27cfcdea7fa40`
  (tree `a0e433f9f185b651f6f5ebc54340a085a5cd6721`)
- original PR593 head: `c8f16877f822959b7a024aed6efefad511bfb868`
  (tree `4dea01d8d3073b57a1dfd026068ffe71bd9080f2`), parent PR591
  `1c0d699f06372fe3c52fa003f53aa38ffd40d07c`
- current implementation commit: `6edd97beb97cdeae38bd502d58a720c16a9427ff`
- ancestry-preserving current merge: `fcd228ebb6372b1076e5506bdc03840c50c3dd32`
  (tree `a416c01193cc598676a0a8a1e03ef96dced7f49c`)
- current-source handoff repair: `e91d132807c5fa6d1b5cea222c975752107308ca`
  (tree `3b6adbf90cd25bae217dc1acbded11148b38d08f`), composed from the
  reviewed current Syntax owner handoff and the explicit development-current
  Reader/Syntax/Macro invocation in the SH08 fixture.
- the ancestry-preserving merge retains the original PR593 head as its second
  parent. Later compositions retain that ancestry and the current quota-free
  owners rather than replaying the historical parent tree.

## Conserved scope

The original eight changed paths are conserved: four fixture files forming two
byte-identical `compact-source-proof-{minimum,interval}` `.gravity`/`.qst` pairs under a
`preserved/` development-fixture directory, the SH08 focused controls, the C7
engine, and the source-product census/description. The C7 port contains the
39 private `sh08-gis3-*` definitions, five native arena/comparison definitions,
the ordinary parameter-choice helper, and the five native DATA guard helpers
required from the independently reviewed PR591 source. The native helpers use
iterative traversal and identity-only cycle detection; they add no project
work, depth, width, byte, scalar, shape-size, or evidence-authority admission.

The private consumer validates complete original DATA, rebuilds indexes and
source relationships for each invocation, checks parameter/evaluation/
visibility/metadata relationships, reconstructs the guarded integer product,
and independently validates and compares the candidate. It retains
`:numeric-proof-established false` and does not route public C7, B47 or
coordinator verification. Historical `accepted/` fixture paths remain source
references only; the current files are explicitly `preserved/` and
development-only.

## Static and focused evidence

- C7 plan load and direct private controls passed. Selected controls:
  `sh08-gis3-unmetered-data-and-index-development-kernel`,
  `sh08-verifier-unmetered-native-data`,
  `sh08-verifier-unmetered-native-comparison`, and
  `sh08-verifier-unmetered-cycle-and-callback-safety`: 4 tests / 117
  assertions / 0 failures / 0 errors.
- C7 source form inventory: 1,124 forms, 1,123 definitions, 0 duplicate
  definitions, 39 GIS3 definitions and 11 `sh08-vf-*` definitions.
- The repaired current-source control passed for both preserved fixtures:
  `sh08-gis3-unmetered-current-source-product-development-kernel`, 1 test / 28
  assertions / 0 failures / 0 errors. It freshly executed C3, SH05, SH06 and
  SH07 with explicit current Reader/Syntax/Macro selection, then constructed
  and freshly consumed the private GIS3 product. Candidate substitution,
  source mutation, and opaque-callback controls remained rejecting/quiet.
- Language boundary: 6 tests / 11 assertions passed.
- Canonical documentation validator: 240 documents passed. Full-language
  roadmap structural validator passed.
- Fixture hashes are paired byte-for-byte:
  `compact-source-proof-minimum` is
  `cb92409e01b28c9218c631327a104a07dcb14c4c9d867d4b97557b074e832e6b`, and
  `compact-source-proof-interval` is
  `b73cb794e6ef937a8fa786c6a6d5ae91afced2457d34367e07ce50f72306ccec`.

## Explicit nonpasses and nonclaims

The earlier current-source attempt stopped before C7 at the inherited C4
macro-source contract: `:exact-pinned-macro-source` reported expected 56,488
bytes and observed 54,374 bytes for the current macro owner. The exact
historical receipt is `/tmp/uniform-pr593-source-current-v2.log`. The repair
does not repin that historical source; it selects the authenticated
development-current Macro route and the reviewed current Syntax source route.

No native-C, restricted broker/process operation, full SH08 namespace, public
function-type acceptance, B47/coordinator authentication, numeric proof,
runtime/fixture acceptance, release, or PR502 closure is claimed. An
independent peer must review this frozen tuple before any governed
integration.
