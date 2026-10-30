# Pure Reader current-main composition

## Exact inputs and disposition

Captured authoritative main:
`5b3252074b2b609a0e676f68f1e8995e348c26f6`, confirmed against the GitHub
main ref before work. Retained source:
`29e272ea1b476018754cd08651a42a1080b5e993` /
tree `1635975bff06001efe320fdc9e4dc56aa9a480a6`.
Normal merge reconciles all incoming main history without conflicts. Earlier
source branches, failed F29 review, corrected selection review and receipts
remain immutable. This is a finite composition of
`reader/finite-remaining-work/extension-policy-closure`, not task completion.

The incoming source includes integrated PR652 at `64b42b07` (source
`e6b12e5bb8ff8a7f522ffe8068430dcb2ee5caf5`), PR653 syntax stream/envelope
assembly and later source-descriptor/native-index extraction through `5b325207`.
No namespace extraction is repeated. The incoming stage ownership, thin
Bootstrap call arities, identity domains, pinned source configuration and
existing reverification port remain intact.

## Actual field disposition and minimum reconcile delta

Normal opt-in Reader adaptation still returns `:pure-extension-results` after
raw Reader/source checks and fresh pure comparison. The constructor, host walk,
result verifier, helper selection and leaf dependencies are byte-conserved
from corrected source `29e272ea`. No actual normal run is inferred.

Integrated `gravity.syntax/current-reader-identity-view` returns the complete
artifact in its existing no-projection and already-projected cases, conserving
an attached companion without asserting sealed admission. The artifact-view
function instead builds a whitelist with `select-keys`: a supplied pure
companion would be silently dropped. The standard compiler construction still
selects standard reader options, and precomputed admission does not authorize
the new opt-in policy or its companion as a sealed product.

Two minimal boundary corrections make that distinction explicit:

- `gravity.syntax/reader-artifact-view` rejects the presence of
  `:pure-extension-results` with `C2-HASH` and
  `:missing-fields [:authenticated-pure-extension-results]` before projection.
  The ordinary view still forwards its opaque internal Reader reverification
  operation with the same authority/path/artifact/invocation arguments.
- `sh03-reader-precomputed-products-verify!` requires the absence of that field
  in its existing admission conjunction. All existing source, raw-result,
  resolved-result, plan-binding, envelope and payload checks remain required.

Presence includes nil and empty-vector values. This prevents silent loss or
accidental interpretation of an unsealed companion as an authenticated result.
The companion remains available on normal opt-in Reader products; it is
explicitly unsupported at the standard sealed-product/view boundary. No
unverified result is copied into an envelope, identity or replay authority.
Future authenticated custom-policy/C2 admission remains separate work.

Only these two production guard additions differ from the normal union of
the source candidate and main. The new composition test and this evidence
file are the other reconcile additions. Existing pure code, fixtures, tests,
selection correction and their historical evidence remain conserved.

## Focused source/interface controls

`gravity.reader.composition-test` reads exact named source bodies as DATA and
evaluates only the artifact/identity view bodies with synthetic operation
ports, plus the exact added precomputed-field predicate. It does not load
`gravity.syntax`, Bootstrap or a test runner that constructs real products.
It covers:

- Standard field projection and exact opaque reverification argument forwarding.
- Propagation of an actual sentinel failure from that modeled opaque port.
- Identity-view field conservation without admission credit.
- Nonempty, empty and nil companion refusal before view ports execute.
- The actual precomputed admission conjunct on absent/present native map data.
- A behavioral mutant removing the new view guard, reproducing the silent drop.

These port/predicate controls do not execute the complete precomputed verifier,
genuine Reader, Gravity compiler, fixture program, provider or native index.
An initial extra parenthesis in the new test prevented source loading; it was
corrected before the passing run and supplies no behavioral evidence.

```sh
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -e '(require (quote gravity.reader.composition-test)) (let [r (clojure.test/run-tests (quote gravity.reader.composition-test))] (System/exit (if (zero? (+ (:fail r) (:error r))) 0 1)))'
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
git diff --check
```

Focused controls: 5 tests / 22 assertions, zero failures/errors.
Language boundary: 6 tests / 11 assertions, zero failures/errors.
Whitespace passed. The prior 34 host negatives and 62 source-selection checks
are not repeated or extended to these new guards. The exact real leaf is not
executed again.

## Real-leaf reuse and frozen artifacts

The actual unreplaced `read-text` check ran once at `29e272ea`: 39 checks,
256 MiB JVM, 270 ms script time, natural exit 0. Synthetic native identities
establish no source authenticity or normal integration. Receipt SHA256:
`1140e77f63b6a1e500dbb43b5d6ce0bbee099c2ad05eddad740a7d78e66e4772`.

Read-only evidence files in `/tmp/reader-pure-leaf-real-29e.oOkDwz/` include
`check.clj`, `receipt.edn`, `stdout.txt`, `context.edn`, and `SHA256SUMS`.
Stdout is explicitly marked as transcribed from the original tool response.
The context pins source/dependency/runtime hashes and the exact command; no
new run was used to obtain them. File hashes, rather than filesystem mode,
bind their contents. The context distinguishes the held-out provider counter
from an actual supplied provider binding and records the held transaction as
not invoked.

The following exact blobs remain unchanged and supply the narrow reuse basis:

| Source | SHA256 |
| --- | --- |
| `gravity/reader/dispatch.clj` | `1f21df8be39f353b60c95cdae341956b40bb0b0e8e19cd2940edffb61dcbf93e` |
| `gravity/c2_artifact_identity/canonical.clj` | `0cef0a0d75e250caa512200ef1a1c2f34b0acaade6f9587b81dc3fa5a18a2db2` |
| `gravity/digest.clj` | `084986ba48fc07b0215c64fbbc3474dbfcdcba5df0de6ff9a87d3b1ab52ae948` |

Paths above are under `bootstrap/clojure/src/`. New view guards are outside
that leaf's dependency chain and receive only the new modeled source checks.
Independent selection receipt SHA256
`8bd6d1f2e6e77c74d630cef6382e1667187cddf559d947c69bfcf56a5de9b227`
remains attributable only to the conserved correction.

## Limits and handoff

For OWN's one affected composition review; no author review assignment or
outbound review dispatch is made. No publication, CI handoff, merge to main,
approval-bundle retry, task/lifecycle edit, resource-pipeline change or genuine
held transaction occurs. Full Reader outcome, granted effects and genuine
integration remain open. The original blocked goal remains unchanged.
