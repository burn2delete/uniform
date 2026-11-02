# Pure Reader publication composition

Task: `reader/finite-remaining-work/extension-policy-closure` / FL-P01-T01.
Current main was verified locally and through GitHub as
`81dc3cf271707e37afc80806475155d3c2b654d3`. Reviewed source:
`9b28fb38c4c2511eadc7172fd3440c31740c49f2`, tree
`f4ba19f5f747d2383a6541f9875bb719e344314c`.
Normal merge `a8585bbba943953bff10971ec944d72c3552f9e2`, tree
`1c095fcbac8a825947b2cf3d46cbae4f6a4076bf`, preserves both histories.
The successor is on `codex/reader-pure-publication-20261030`.

The qualified independent native-ingress rereview was consumed once by reading
and verifying SHA256
`56a5e3b34fe87339abd864d92c7f6dcc46722dfbe76e729434ca757d1a482cc8`.
Its 190 checks, 28 hostile-artifact cases, seven options cases and five mutants
close the two original findings only in their exact SOURCE/host-model scope.
No independent audit or accepted model population was rerun.

## Current-base compatibility

The merge has no conflicts. All Reader implementation, fixtures, models and
native validation dependencies are byte-conserved at the merge. The only
Bootstrap entrypoint overlap adds main's Macro storage require alongside the
Reader require; no require is lost. The two-argument `macro-source-artifact`
facade used by Reader plan construction is byte-conserved, as are the selected
Reader source/plan interfaces and authenticated-envelope schema. Incoming main
Macro/C11 changes remain intact. The CI workflow, harness and lint policy are
identical to current main. No runtime compatibility claim follows from this
source comparison.

The inherited accepted callable-runner source/check definitions remain exact
9b28 blobs. They are not Reader-authored engineering changes in this successor.
The old E351 request remains stale for the changed Reader; its resource/runtime
hold is not cleared by this composition.

## Publication validation delta

All-state PR queries for both source branches and the proposed publication
branch returned no matches. The remote publication branch was absent. The
original standard-handler PR571 remains a separate open draft with its original
criterion; it was not updated or replaced. No push or new PR has been attempted.

The unchanged changed-source CI lint gate initially reported 25 blocking
findings. Reader-owned corrections remove 24 without changing lint policy:
explicit declarations for source-installed model functions; explicit dynamic
alias registration via real namespace Vars; assertions around observable foreign
callback return values; and flattening a redundant nested C2 `let` while
retaining the same binding order and result expression. The Gravity producer,
raw verifier, policy, native admission detector, C3 consumer and Syntax source
are unchanged by these lint corrections. The C2 change is an authored structural
normalization, not a new independent acceptance claim.

The six corrected source/test paths are:

- `bootstrap/clojure/src/gravity/bootstrap_impl/components/compiler/00829-compiler-c2-reader-source-artifact.clj`
- `bootstrap/clojure/test/gravity/reader/composition_test.clj`
- `bootstrap/clojure/test/gravity/reader/gravity_pure_text_test.clj`
- `bootstrap/clojure/test/gravity/reader/native_admission_test.clj`
- `bootstrap/clojure/test/gravity/reader/normal_dispatch_test.clj`
- `bootstrap/clojure/test/gravity/reader/plan_selection_test.clj`

Only the affected hostile-carrier positive controls, dynamic view alias,
selected-function closure and definition-compilation test vars were run, plus
the language boundary: 10 tests / 106 assertions, zero failures/errors, exit 0.
Bootstrap and Syntax namespace load indicators were false. The initial clean
merge language-boundary check separately passed 6 / 11. No genuine Reader,
Gravity handler, raw verifier, constructor, provider or held operation ran.

The post-correction lint diagnostic reports one remaining blocking error:

```text
specs/accounting-removal/process-lease-custody/check.clj:1
namespace-name-mismatch: process-lease-custody.check
```

That inherited callable-runner check is byte-conserved and outside this
Reader correction's write scope. It was not edited, removed or exempted. The
ordinary CI policy remains unchanged; no failure is relabeled as a pass.
Local validation logs/receipts are retained externally, not added to source.

## Exact next handoff

Return this current-base source freeze and the six-path lint correction to OWN,
with the inherited check's actual blocking diagnostic for its existing owner.
Publication has not been performed while that normal CI blocker remains and
the new normalization delta awaits its handoff. No specific publication denial
or auto-review rejection was encountered; there is no alternate publication
route or CI bypass.

Next original acceptance is the existing paired genuine sealed constructor,
normal fresh consumer and tamper definition, pending the unchanged common
resource disposition for the 6343 hold. Those definitions are already present;
another pure design proposal is unnecessary. The other original criterion is
the effectful trusted-grant/recorded-result consumer that verifies the original
effectful invocation without replaying provider IO. It remains unimplemented
and may advance as scoped source work without claiming runtime clearance.
The original native goal remains blocked. No merge, approval, CI rerun, resource
probe/acquisition, alternate root or publication was performed.
