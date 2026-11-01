# Verification record DATA successor

Planning: ACC-01/07/12/13. Accountable implementation owner: ACC-01,
thread `01a183dd-7208-7d33-a336-7483fd90e358`. Parent: PR614 at
`c506514d4b32baf8269a7c3ff49ca86d2dc18c05`, tree
`ef9a09328663a93e213f65754c8caab882362c2d`. This finite source increment
preserves historical check-record validation and two original test kernels.
Parent and candidate acceptance remain separate and pending.

## Source and contract

[C7](../../docs/phase-06-compiler-architecture/086-c7-type-checker-design.md)
and [D9](../../docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md)
require current facts, complete verification records and evidence appropriate
to their authority. [D8](../../docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md)
and the current [development policy](../../docs/development-verification.md)
retain native class/metadata/callback rules and tamper rejection. A passing
receipt-shaped DATA record does not itself establish B47/source authority.

Restore exactly three complete definitions from historical PR502 head
`6a4f87f013d0076b81aba9e0667650e70d28e635`:

- `sh08-ft-all-checks-true?` validates the complete native DATA first, requires a
  nonempty Map, visits every physical entry, requires Keyword keys, rejects
  duplicate keys and requires each stored value to be exactly true. It does not
  use a caller-supplied key for lookup before native/type validation.
- `sh08-ft-verification-receipt-valid?` performs that physical check before
  constructing the catalog key set.
- `sh08-ft-coordinator-verification-valid?` makes the same ordering correction
  at its existing report-check branch.

[The census](verification-records-census.edn) verifies the complete historical
forms and exactly one swapped guard pair in each of the latter two functions.
All other expressions in those two functions are unchanged, including earlier
coordinator equality and authority checks. Five native-DATA dependencies,
the current full B47 check catalog, every other production definition and the
host Native provider remain unchanged. All argument lists and direct C7 caller
argument lists are conserved. Their caller forms are pinned: receipt/context
validation and authenticated-Core admission remain distinct downstream paths.

This is not a claim of complete coordinator callback safety. Earlier equality,
outer record handling, provider execution and source authority require their
own actual acceptance. No catalog entry, receipt version, provider permission,
opaque witness, digest, source-provenance identity or authentication mechanism
is added or relaxed. No fresh receipt is issued and no old receipt is renewed.

## Complete original tests and new controls

Restore both complete original kernels, preserving their bodies/comments/bytes:

- `sh08-carrier-preflight-unmetered-development-kernel` retains depth 192,
  width 70000, shared acyclic data, default sorted Map, native cycle, opaque
  nested value, metadata, lazy tail and zero-callback obligations. PR613's
  additional carrier controls remain unchanged; they did not substitute for
  these exact original inputs.
- `sh08-verification-receipt-physical-checks-development-kernel` retains the
  actual current catalog, native default Map/tree positives, physically
  duplicated entries, false/missing/extra/non-Keyword keys, opaque and foreign
  carriers, metadata and zero callbacks. Its original comment explicitly
  distinguishes receipt DATA from B47 authority.

The new `verification-record-entry-order-development-kernel` checks one extra
physical duplicate despite a complete catalog, an opaque physical key, odd and
nil native ArrayMap backing, empty metadata, foreign comparator, non-true values,
missing and extra catalog entries. Valid native HashMap/default tree Maps remain
positive. Hash/equality/render/comparator callbacks must remain unobserved.
The outer receipt is ordinary literal DATA; this test constructs no coordinator
witness and grants no source authority.

All existing parent test bytes remain unchanged. No original assertion, fixture,
number, schema, argument list or diagnostic is normalized away. These actual-C7
test definitions are UNRUN, not a host model or a passing Native receipt.

## Observed checks and remaining evidence

The pinned local Clojure checker passed complete historical forms, the two exact
guard-only reorderings, unchanged catalog/native provider/dependencies and caller
argument lists, complete original test forms/byte spans and parent byte
conservation. The first attempt stopped on a checker delimiter error; the
corrected checker passed. Reader evaluation is disabled and Bootstrap remains
unloaded. Source/test/provider pins and generator digest are in the census;
exact scripts, invocations and raw outputs remain local.

The language boundary gate passes 6 tests/11 assertions. Contextual clj-kondo
under `tools/ci/kondo`, cache disabled, reports zero errors and the same 71
warnings as the exact parent. Document/roadmap and whitespace checks pass.
Routine CI for PR614 `c506514d` and prior evidence PR569 `7a28a17c` completed
seven successes and two configured skips each; these jobs do not execute the new
held acceptance. The earlier prohibited cache cleanup remains stopped, and its
existing generated cache remains local/untracked. No clean-worktree or
integration claim is made.

Keep draft pending coordinator-routed independent source/result acceptance.
No denied successor review or held/unknown operation is replayed or rerouted.
Genuine receipt/carrier kernels, Native header/reflection/provider checks,
public/B47/coordinator source authority, compiler, runtime, Effects/emitter,
broker and held selectors remain UNRUN. Finite remaining evidence includes:

1. Execute both complete original kernels and the added entry-order kernel with
   actual current Native behavior, preserving every positive, physical duplicate,
   malformed/metadata/late-entry and callback control.
2. Revalidate affected context and authenticated-Core callers under their genuine
   authority assessment. Source order and receipt DATA cannot establish a valid
   provenance binding, digest, witness or authentic source result.
3. Preserve applicable earlier coordinator equality/provider/native boundaries;
   independently assess any broader behavior without attributing it to this
   guard-order slice. Existing parent carrier and primitive obligations remain.
4. Consume applicable CI, independently review the exact candidate and fresh
   composed main, and integrate only accepted identities through preflight.

Other original source/test deltas and accounting remain open. No new receipt,
B47 authority, whole callback safety, integration, release, self-hosting,
PR502 closure or expanded PR532 acceptance is claimed.
