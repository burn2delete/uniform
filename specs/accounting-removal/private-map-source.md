# Private Map source-census accounting removal

The later [construction-report supplement](private-map-construction.md) removes
the construction field/debit retained by this historical source-census increment.
Its acceptance tuple and conservation evidence below remain scoped to that
earlier change.

This finite ACC-04 source supplement continues from `cbfd2f6aee78020b3a3468938e2c676e6ca1e6e5`,
tree `f0206b9ee8513ac423462d856575efcd77ccd483`. It changes the three
`sh08-pmk-source`, `body-pmk-source`, `p15-s23-pmk-source` builders and their
three direct `pmk-header` callers. PR534's accepted change removed the separate
form-index/type-view reservations; its source builders were unchanged and still
match this supplement's source parent before the edit. No PR534 work is replayed.

## Removed transaction

The source builders no longer take a budget or enforce the 65536 cap, the
`129 + function-record-count` reservation, the profile/target width reservation
or their cumulative refusal. They return semantic status/descriptor cells;
there is no top-level work report or replacement sentinel. The accounting-only
`:profile-validation-work` descriptor field is removed in all three owners.

The headers stop debiting source-census work. They retain their existing result
charge and the existing one-operation construction field, using that field
explicitly to keep those remaining charges separate. A source failure returns
its real semantic reason with only the header's existing result charge and
does not invoke proof. Proof/header/physical/parameter/map-source/entry and
runtime accounting remains open; this change does not silently remove it.
The retained construction and key-domain descriptor fields are unchanged
residual accounting metadata, not claims of safety or provider CPU bounds.

## Preserved semantics

[STD3](../../docs/phase-16-standard-library/213-std3-collections-library-specification.md)
requires complete private-entry, whole-map and compatibility evidence.
[C7](../../docs/phase-06-compiler-architecture/086-c7-type-checker-design.md),
[C8](../../docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md)
and [C14](../../docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md)
require authentic source, binding, type, profile and effect facts at consumers.
Current AGENTS.md supersedes the historical project-work reservations.

The actual ID, binding, profile/target-vector, type-admission and source equality
calls remain in their existing order. Malformed IDs refuse before native type
admission. Profile/target members validate before set construction; malformed
owner records validate before owner comparison. Canonical call/edge, owner,
operator attributes, evaluation order, effects/capabilities, allocation/storage
policy and no-refinement semantics remain. Ordinal nonnegativity and fixed
unary-call/ID-group cardinalities enforce source/arity legality, not work quotas.

[Conservation evidence](private-map-source-conservation.edn) verifies byte
identity outside the six selected production functions, exact source bodies
after the declared accounting erasure, and an unchanged header proof branch.
Both immutable selector contracts retain their bytes. Their old whole-source
pins are stale for this successor; no fingerprint or packet is refreshed.

## Source-only controls and limits

`gravity.types.private-map-source-test` evaluates the extracted source functions
as Clojure-compatible models without loading Bootstrap or a Gravity engine.
It uses the actual ID/binding/profile/target helpers and explicit seams for
native type admission and complete entry proof. Controls include 65537 owner
records, profile/target widths beyond the former reservation, equally late
invalid records/members, opaque IDs/types with zero callbacks, corrupted
call/edge/binding/profile fields, complete three-owner descriptor parity,
stale profile-work fact rejection and the actual header handoff/refusal path.

These fixtures are normalized source cells, not authentic compiler requests.
Their helper/native/proof boundaries are not promoted to executed PMK acceptance.
The four new controls passed 137 assertions; combined with the existing cycle
and fact controls, 10 tests / 4,853 assertions passed with Bootstrap unloaded.
Source conservation passed for all three owners and six production functions;
boundary controls passed 6/11, changed model/checker lint had zero findings and
whitespace checking passed. Initial helper/test reader errors were corrected
before these passing results; no failed result is promoted.
The genuine SH08 composition control receives only its necessary source-call
arity and report/schema assertions; it remains UNRUN. DSE, ANE, BKA and runtime
source are untouched. Independent affected source review returned source-only
PASS for `37de82881573d9977229b6eb9f6066ec68b41bc6`, tree
`b5f8f6e77ed639f3e53660ce526ff2960d42adb9`. The reviewer reproduced the initial
10/4,850 controls, then identified the missing valid-wide-target positive.
That P2 evidence finding was corrected with one assertion per owner; the reviewer
verified the final test/evidence delta without repeating unchanged controls.
[Review consumption](private-map-source-review-consumption.edn) binds both
completed reviewer turns, final input digests, reproduced/reported results and
remaining nonclaims. This consumes the existing independent review and routes
no duplicate. The original genuine-selector goal remains BLOCKED and its failed
wrapper is not retried.
