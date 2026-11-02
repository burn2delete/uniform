# Reader source binding repair receipt

## Scope and disposition

This receipt records a bounded current-source repair of PR571, "Reader source-
byte/product joins." It ports the useful standard dispatch joins onto the
current nine-argument Reader path while preserving the exact four-field
semantic dispatch seed. It is implementation evidence, not producer
authentication or migration acceptance.

The repair was developed from current `origin/main` commit
`9b6f1169ad5fe1689c722f3975cde339b02a8087`, tree
`bd72e21bf24b4a208fdb53cf29e727175d72a9e5`. The source implementation commit
is `045ab0087f4019bc8ad636dca15acba32fb9c1f6`, tree
`27a1060b217c7b2e9a2f2e2db6b9b461dd73c158`; this receipt is a documentation-
only successor.

The original PR identity is preserved locally for comparison:

- candidate head: `a9d43c5b3e4b19c0a7e2630ddc242d77c6e33532`
- candidate tree: `d3d64876d4c67f700d6004029c8d2dec7f7e8c4d`
- candidate parent: `1c10242e4c1bc68557f70c1ed932e2a3615ac464`
- declared base: `042f72adca0075ef4163ca832390e0be29b02890`
- declared base tree: `f710471f73b3ceff8e29ca0aafacf623a54fbf8f`
- effective merge base used for the authored comparison:
  `60f59d225b03338eb30cdc22850838ecebd07303`, tree
  `83d1e0a4ee9f4aaccd8e8164594f495f672a5ef5`

## Semantic and provenance boundary

The standard semantic seed remains exactly:

```clojure
{:source-id source-id
 :form-id form-id
 :span span
 :datum datum}
```

The semantic input hash is over the reader-extension domain, policy hash, tag,
and that seed. The returned-product hash is over the result domain, input hash,
and returned seed. Actual source path, origin, source bytes/content hash, and
the path/source joins are separate provenance observations and are never added
to the semantic preimage. A fully populated six-field generic seed is rejected.

The current repair validates the standard source unit and its UTF-8 bytes,
source identity/content joins, form identity and byte span, and fresh returned
products. It preserves the current pure, sealed, and resource branches without
routing them through standard source joins. It does not manufacture a Reader
product or claim that an injected parsed graph is genuine Reader-to-C2
execution.

## Original six-path accounting

The original PR changed six paths. All useful deltas are accounted for here:

1. `bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00815-sh03-reader-normalization-deferred.clj`:
   ported standard source/form validation and fresh returned-product matching
   to the current nine-argument branch; pure/sealed/resource behavior is
   retained.
2. `bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00828-sh03-reader-internal-product-authority.clj`:
   ported current-product reconstruction and standard observation matching,
   while keeping the existing outer product guards.
3. `bootstrap/clojure/src/gravity/reader/dispatch.clj`: retained the exact
   four-field seed and added source-byte, span, form, and product joins with
   provenance kept outside semantic identity.
4. `bootstrap/clojure/test/gravity/reader/dispatch_test.clj`: added the
   six-field rejection and semantic/provenance hash control.
5. `bootstrap/clojure/test/gravity/reader/normal_dispatch_test.clj`: added
   positive and negative source/product joins, tamper and malformed-carrier
   controls, and untouched pure/sealed/resource branch controls.
6. `specs/reader-workflow/dispatch-source-binding-evidence.md`: this receipt
   binds the exact identities, scope, evidence, and nonclaims.

The older PR's direct files were not replayed over current branches. The
current source-owner APIs are used, and pending PR453 v2 source/provenance work
does not supersede these source-byte and returned-product joins.

## Evidence

Focused current-source checks on implementation commit `045ab0087f4019bc8ad636dca15acba32fb9c1f6`:

- Reader dispatch and normal-dispatch namespaces: 18 tests, 167 assertions,
  0 failures, 0 errors.
- language-boundary gate: 6 tests, 11 assertions, 0 failures, 0 errors.
- gravity document validator: 240 documents, 19 phase indexes, validation
  passed.
- full-language roadmap structural validator: passed.
- pinned changed-file lint using
  `/tmp/acc08-controls-01a183dd/kondo-2025.10.23`: 0 blocking warnings or
  errors.
- `git diff --check`: passed.

The fixtures exercise current Clojure source/data paths, source-unit hashing,
real handler callbacks, malformed and foreign carriers, tamper rejection, and
branch isolation. They do not establish producer authentication, a genuine
Reader-to-C2 product, native execution, large-input behavior, or any denied or
restricted route.

## Nonclaims

This receipt does not confer public source admission, canonical migration
authority, release readiness, self-hosting completion, seed retirement, or
roadmap credit beyond whatever an independent integration review explicitly
records. No admission pin was renewed, and no GitHub, push, merge, native, or
restricted operation was performed by this repair.

## Current-main composition refresh

For the final current-main review, the branch was normally merged with main at
`5dfe15c9fad14e5abebf23bf1e645bf9154a2e1e`, tree
`643afe5f3b00918fc3de76f2855294481a09b505`. The composed candidate is merge
commit `95fa0579e00de24ee74851f548a1a43e4077e8fe`, tree
`63e45eed928eacb324bf907d1db2f3777378ae98`, with a clean worktree.

The intervening main changes are disjoint C8/C7 effect-checker quota-removal
and traversal changes plus their test/report paths. They do not alter any of
the five Reader implementation/test paths or this receipt's semantic claims.
The final comparison against current main contains exactly the five Reader
source/test paths and this receipt. Repeated focused validation on the exact
composition remains 18 tests and 167 assertions with zero failures or errors;
the governance, document, roadmap, language-boundary, diff, and pinned
changed-file lint checks also pass, with zero blocking lint warnings/errors.
