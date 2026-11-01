# Pure Reader native admission correction

Canonical task: `reader/finite-remaining-work/extension-policy-closure` /
FL-P01-T01. Base: `3ba15aa94209fba9da926961dbda310e436a898e`, tree
`d614d14c000e2d1907a6444d1992158001bd2daa`.
Successor branch: `codex/reader-pure-native-admission`.

This fixes both findings in the independent CHANGES-REQUIRED receipt
`/tmp/reader-sealed-independent-3ba.edn`, SHA256
`a0656106ccd81dc1b667e1632be43065e1ded95739cbb80c040f58cbd4368c6b`.
The receipt was read and hash-verified once; its 71-check audit/reproducer was
not rerun. The original candidate and finding history remain immutable.
This is author SOURCE/MODELED correction evidence, not independent acceptance.

## Corrected ordering

The previous marker detector could invoke foreign lookup/sequence protocols
before the native-carrier guard. `admission-required?` now inspects only its
fixed marker paths through a private class-first helper. Map/vector classes,
native map keys and metadata are checked before lookup or sequence operations;
marker scalar classes are checked before equality. Unsupported carriers
conservatively select the owning native validation without invoking their
lookup, sequence, equality, hash or metadata methods. Fixed keyword key checks
also precede map lookup, avoiding comparisons against foreign colliding keys.
This helper selects validation; it grants no authentication authority.

Both the direct C3 precomputed verifier and Syntax artifact view now perform
the existing complete native DATA validation on that selected route before
the companion guard, source selection, opaque port, reconstruction or envelope
calls. Syntax uses the native-validation operation already provided by its
owning Reader-view operation set; its operation declaration is updated.
Unsealed-companion refusals and every marker-stripping/missing-invocation
condition remain. Standard artifacts take the existing route, including native
Date/UUID registered values outside the marker paths; the pure-text native
validator is not imposed on those standard values.

The eight-argument constructor now validates the complete reader-options
carrier before policy lookup or either fixed-options equality check. The pure
construction input tuple additionally includes reader-options alongside source
path, text and project context. Existing constructor arities and their fixed
standard-options forwarding are unchanged.

The Gravity handler, producer, whole-result fresh verifier, invocation digest,
adapter projections, C2 reconstruction comparison, sealed source schema and
unrun genuine integration definition are unchanged from 3ba. The capability
implementation remains present; this successor changes ingress ordering only.

## Focused checks

`gravity.reader.native-admission-test` reads exact native validator and host
ingress definitions as source. It installs only the named Clojure definitions
and fixed native-field reflection helpers, excluding the surrounding authority
closure and Bootstrap load order. Fresh construction, source binding, opaque
verification and envelope operations are explicit synthetic ports. No Gravity
form, handler, parser, compiler or genuine producer/verifier is executed.

The hostile carrier implements ILookup, Seqable, IMeta, IHashEq, equals and
hashCode with observable counters. Tests include root and nested foreign record
carriers, foreign sequences/scalar markers/options, metadata-bearing maps and
foreign hash keys, with both top policy/reconstruction markers stripped.
Twenty-two artifact-port cases plus six constructor-options cases refuse with
all callback counters zero and only native-validation entry observed. Each
callback has a positive observability control. Invalid options do not reach
policy selection/equality or the constructor continuation.

Native standard models retain their original continuation and error routing,
including Date/UUID values in parsed data and tagged forms. A native pure
positive flows through the actual isolated view and C3 source bodies into the
mocked fresh constructor before envelope admission. Five individual surviving
markers still select validation and refuse a missing invocation. Removing both
top markers while retaining the record marker still refuses. A native pure
artifact with no invocation refuses without source/constructor/envelope calls.
These are port-order and DATA models, not authentic sealed acceptance.

```sh
clojure -J-Xmx256m -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -e '(require (quote gravity.reader.native-admission-test) (quote gravity.self-hosting.sh01-language-boundary-test)) (let [r (clojure.test/run-tests (quote gravity.reader.native-admission-test) (quote gravity.self-hosting.sh01-language-boundary-test))] (println (pr-str {:bootstrap-loaded? (boolean (find-ns (quote gravity.bootstrap))) :syntax-loaded? (boolean (find-ns (quote gravity.syntax))) :evidence :source-modeled-native-ingress})) (System/exit (if (zero? (+ (:fail r) (:error r))) 0 1)))'
```

Result: 9 tests / 132 assertions, zero failures/errors, exit 0. The new affected
models account for 3 / 121; the language boundary accounts for 6 / 11. Both
printed application load indicators were false. An initial focused run before
the additional native-positive/marker controls passed 3 / 104.

Only three existing test vars whose source-guard selectors were affected were
rerun: `removing-view-guard-demonstrates-silent-drop-regression`,
`pure-presence-refuses-before-standard-verification`, and
`standard-authority-and-existing-guards-are-retained`. Result: 3 tests /
25 assertions, zero failures/errors, exit 0. Their selectors now locate the
unchanged companion guard after the new native-validation prefix. The larger
Gravity producer/plan/model population and prior independent audits were not
repeated.

Both compiler-free document validators passed with exit 0:
`clojure -J-Xmx256m -M tools/validate_gravity_docs.clj` (240 documents,
19 phase indexes) and `clojure -J-Xmx256m -M tools/validate_full_language_roadmap.clj`.
`git diff --check` also passed.

New implementation naming is `marker-at?` and
`gravity.reader.native-admission-test`; existing compatibility names remain.
There is no new callback/authentication protocol, generic validator framework,
quota, registry or service. Existing native validation retains its authority.

## Handoff and remaining gate

The owned delta is exactly these eight paths:

- `bootstrap/clojure/src/gravity/reader/pure_text.clj`
- `bootstrap/clojure/src/gravity/syntax.clj`
- `bootstrap/clojure/src/gravity/bootstrap_impl/components/compiler/00829-compiler-c2-reader-source-artifact.clj`
- `bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00850-sh03-c3-precomputed-c2-verify.clj`
- `bootstrap/clojure/test/gravity/reader/native_admission_test.clj`
- `bootstrap/clojure/test/gravity/reader/composition_test.clj`
- `bootstrap/clojure/test/gravity/reader/product_admission_test.clj`
- `specs/reader-workflow/pure-native-admission-evidence.md`

Request one affected independent rereview through OWN for this exact successor,
covering both findings and the retained producer/admission joins. No reviewer
was dispatched by the author. No lifecycle ledger, resource/root helper or
unrelated worktree was edited. No publication or integration was performed.

The original native goal remains blocked. Genuine Reader/Gravity/Bootstrap,
paired integration, provider, lease, probe and shared-6343 execution remain
unrun under the existing hold. This correction does not supply runtime
clearance or confer broad extension-policy completion, release or seed credit.
