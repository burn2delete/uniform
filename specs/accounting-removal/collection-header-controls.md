# Preserved collection header reconstruction controls

Owner: ACC-01. Base main `2654d155322f7863dd087065f19000b43c877cbe`.
Historical source: PR502 `6a4f87f013d0076b81aba9e0667650e70d28e635`.

## Scope

Restore the historical three-owner reconstruction controls in four existing
lowering kernels: vector-empty, List-set, Map-keys and vector-rest. The tests
derive the C7 descriptor, independently call the actual Effects and emitter
header builders, require both to accept, and compare the entire descriptors.
They also require the obsolete `:operand-type-work` field to be absent.

All historical and current assertion forms survive. Original executable
instruction/result checks, complete forged descriptors, source/binding/call
mutations and semantic case tables remain in the same test bodies. Production,
shared helpers and every other test byte remain unchanged.

## Exact historical adaptations

Each original body has only two recorded raw adaptations. Remove its unused
call to the retired `p15-s23-lc-type-work` API. Replace the extra-field forgery
derived from that result's retired work count with explicit 1 and nil forgeries.
The existing zero-valued forgery remains, covering obsolete fields with 0/1/nil
without requiring an accounting API. No assertion or semantic mutation retires.

The existing header/caller budget arguments remain unchanged source facts;
this test restoration neither adds nor endorses their admission policy. The
three full owner files and all twelve current header definitions/argument
lists are pinned. Historical header differences remain explicit. Six current
upstream fixture/derive/invocation definitions are pinned separately. These
pins do not prove transitive helper equivalence or actual execution.

## Verification and acceptance

The companion census records complete test-file hashes, four original/current/
adapted interval hashes and all eight raw substitutions. Reader checks preserve
all other definitions and the multisets of original and current assertions.
Regex literals are compared by reader serialization, not host Pattern identity;
the initial in-memory conservation check rejected that identity comparison
before source writes. Final checks use complete serialized source comparisons.

Observed checks pass: language boundary 6 tests/11 assertions, canonical docs,
roadmap and staged/full-parent whitespace. Pinned clj-kondo 2025.10.23 with
cache disabled reports zero errors and identical complete 76-warning multisets
before/after, with no new findings or configuration changes. New EDN has zero
findings.

Genuine kernels, host bootstrap, compiler, Effects, emitter and runtime are
UNRUN. No held or denied selector is retried. Independent source review,
current-head CI, affected composition and exact clean preflight remain separate
requirements. This is a finite successor for four missing semantic controls,
not whole-path incorporation, accounting completion, PR502 closure, release or
self-hosting evidence.
