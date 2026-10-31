# Resolution verification boundary

This increment moves the current C5 artifact verification algorithm into
`gravity.resolution`, with explicit operations for fresh upstream validation,
request replay, identity calculation and authenticated-envelope verification.
D1, D2, D3, D9, L3, C3, C4, C5, C15, BOOT8 and TEST2 govern the boundary.

The stage must preserve native carrier validation before semantic inspection,
exact artifact and boundary shapes, current plan bindings, fresh resolver
replay, source provenance, deterministic identities and all existing checks.
Private construction permits an absent embedded proof only while constructing
that proof; public verification must require the exact complete proof.
The outer containment and request replay retain their explicit reinterrupt and
rethrow handling. The existing SH05, binding and envelope Throwable catches
remain unchanged; this extraction does not establish interrupt propagation at
those three inner boundaries. Contained host failures remain failed verification
reports, never acceptance.

Bootstrap retains actual owner admission, source snapshots, envelope authority,
scoped proof transactions and final publication. Its verifier-root snapshot must
be installed after every explicit operation is bound; capturing an unbound
forward declaration would incorrectly disable legitimate receipt reuse.
Root changes and thread bindings must continue to invalidate reuse.

Public `verify [artifact context operations]` always checks a published artifact
with construction mode disabled. `verification-failure [error]` builds the
existing failed report. The lower-level `verify-current*`, `verify*` and
`verify-contained*` are private implementation functions; Bootstrap's private
construction path alone passes the existing construction flag. Every stage API
has `:admission :none`: its report has no proof or publication authority.
Owner-supplied context callbacks retain owner validation and grant no new stage
admission. Compatibility callbacks retain the existing acyclic dispatch:
contained check -> preflight -> fresh verification. They do not call the public
proof-transaction closure or publish artifacts.

Acceptance requires focused carrier, replay, proof and dependency-drift tests,
real accepted library and consumer artifacts in both source spellings, actual
unresolved-reference rejection, and complete before/after product and diagnostic
comparisons. Fresh public verification, proportionate regressions, language and
document validation, independent review, clean commits and exact main preflight
are required before integration. This document records scope before code and
validation; it grants no acceptance. Complete resolution, current Reader
migration, release, self-hosting and seed retirement remain unclaimed.

The stage cold-loads 46 public APIs without Bootstrap; the three replay,
preflight and contained implementation Vars are private. Focused checks passed
7 tests / 1369 assertions before the final private-root expansion. The initial
focused run exposed a missing public `verify` entry in construction drift roots
(three nonpasses); that production omission was corrected and the focused run
then passed. Direct source/test lint reports zero errors or warnings. The
complete regression and six-product comparison are still running; no pending
check is counted as passing.

All six complete before/after comparisons now pass: library, consumer and
C5-UNRESOLVED generated-origin rejection in both .gravity and .qst spellings.
Each consumer has one premise from a freshly verified library. All four accepted
products pass fresh public verification. Library products retain 267 bindings /
10 resolutions and identity
sha256:7cee4a4156c5eb1ddd4ed19e94a6ee6938bd19bb594cdc495863eb648f8cf0a4;
consumers retain 267 / 6 and identity
sha256:4b7cf3c7880ab84ad20efb41b158d70f43392107a8a107724473d7fdb935903d.
Both rejected listish? fixtures retain C5-UNRESOLVED and two origin records.
These comparisons include whole artifacts and diagnostic payloads, including
actual source and header paths. They cover the c70 source context and the
extracted algorithms before the later installation-only layout amendment.

The initial contextual lint attempt could not construct a baseline for a newly
added install resource. The resource and loader delta were removed; the same
closure now ends the existing late C5 component, after its callbacks are bound.
Contextual lint on that layout passes with zero blocking findings, without a
lint-policy change. A non-evaluating source comparison confirms all four moved
algorithm bodies, the public proof-transaction body and the existing late-file
prefix are conserved. It reverses only the explicit input/operation seam and
normalizes reader-generated anonymous symbols.

The obsolete first full-regression process was stopped after the layout and
nested-owner fixture amendment, and is not counted as passing. The replacement
full regression executes the current 977aee30 source. Test replacement of nested
compatibility owners uses the actual Var ports; passing a subroutine operation
map does not recursively replace the dependencies of another supplied callback.
Language boundary passes 6 / 11, documentation 240 documents / 19 phase indexes,
and roadmap structural validation passes. Current regression, independent
acceptance and integration evidence must still be recorded before merge.
