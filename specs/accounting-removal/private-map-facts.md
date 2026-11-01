# Private Map fact-census accounting removal

This finite ACC-04 source supplement removes the fact-table charge from
`body-pmk-fact-census` and `p15-s23-pmk-fact-census`, and its two direct
`pmk-evidence` caller debits. It continues from PR577 source
`4f972b9537e163118965c8847006db8c72442bd0`, tree
`9664a9494b2516cd9d36f8a2b8cbd6758a00b9e5`.

The deleted allowance was `3 * count(facts)`. The census no longer takes a
budget, returns a work count or emits a work-limit refusal. The callers pass
the remaining allowance from their other premises directly to entry evidence;
they no longer reserve or debit fact-census work. No replacement cap, sentinel
or zero-cost telemetry is introduced. Header, row and entry-proof accounting
remains separate open work.

## Preserved contract

[STD3](../../docs/phase-16-standard-library/213-std3-collections-library-specification.md)
requires complete private-Map entry and compatibility evidence at every
consumer. [C8](../../docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md)
and [C14](../../docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md)
require typed/effect/source facts to survive lowering. Current AGENTS.md removes
project work allowances while retaining these semantic requirements.

Both censuses still validate every fact ID, map key/ID agreement, request
domain and dispatch before selection. They reject duplicate PMK IDs, missing
descriptors, incomplete descriptor coverage and any difference from the full
fact reconstructed by the unchanged `pmk-call-fact` constructor. Unrelated
fact families retain their existing handling. Profile, target, source/origin,
argument/result types, proof, effects, capabilities and mode fields remain in
that full equality. No descriptor schema or alias changes are made.

The only production changes are those four functions in Effects and emitter.
C7, DSE, ANE, BKA and the original fifteen-selector/twenty-edge contract are
unchanged. Existing immutable source packets retain their old bytes and are
stale for this successor; their whole-source pins must not be refreshed or
treated as execution authority.

## Source-only evidence

`gravity.types.private-map-fact-test` reads and evaluates the actual selected
function forms without loading Bootstrap. It reuses the existing explicit
native/source/type seams of `private-map-cycle-test`. Controls cover Vector and
Map fact tables above the old allowance threshold, missing/duplicate/foreign
IDs, equally late invalid facts, domain/dispatch mismatches, complete fact
field tampering and inert opaque values with zero callbacks.

The caller control evaluates the actual `pmk-evidence`, original-source index,
row census, fact census and constructors. Header/type permission and entry
admission remain explicit seams. It verifies the removal of the caller debit
and that malformed facts refuse before entry admission. It does not establish
that a wide table passes the remaining entry-proof accounting.

The initial control run exposed a missing emitter aggregation seam; it had
zero assertion failures and one harness error. After that seam was corrected,
the combined existing/new controls passed 6 tests / 4,716 assertions, including
3 new tests / 78 assertions. The wide Map negative explicitly places opaque
dispatch at the last key and checks zero callbacks. Boundary tests passed 6/11;
changed test lint and whitespace checks passed. Independent affected-source
review completed with source-only PASS for `efc5eed9ab5701329383a110257793f084965c3e`,
tree `b5cda9818462d04cc1f9ddad8de54757afb2806c`, with no actionable source defect.
The existing separate reviewer inspected the production delta and final-key Map
successor; it did not independently rerun the controls. The implementation-owner
control results above remain separately attributed.

[Review consumption](private-map-facts-review-consumption.edn) identifies the
completed reviewer turn, source base, six reviewed input digests, scope and
nonclaims. Completion was read back through both the agent status and reviewer
chat. Recorded reviewer commands contain no implementation mutation; consumed
source bytes match the reviewed candidate. An external plugin configuration
diagnostic has no repository or semantic evidence authority. No duplicate
review, new assessment or genuine operation was routed to obtain this record.

Routine PR CI for the reviewed source head completed run `41011204868` with
seven successful checks and the two configured source-Core/prerelease skips.
This includes the ordinary CI Bootstrap-load job, separate from the local
compiler-free controls. Check status was observed; CI artifacts were not
inspected here. No CI result is promoted to held-selector or PMK acceptance.

[Source conservation](private-map-facts-conservation.edn) checks exact byte
identity outside the four selected production functions. Inside each census,
erasing only the old allowance wrapper and work-count fields reproduces the
new loop exactly. The caller comparison permits only the matching argument,
debit and obsolete work-limit reason removal. C7 and both immutable selector
contracts are byte-identical to the source parent. These are structural checks,
not compiler/native conformance evidence.

The original genuine-selector goal remains BLOCKED. Its failed wrapper consumed
the one admitted execution; no wrapper/selector, held Bootstrap/compiler,
provider/native/runtime, broker/lock or historical unknown-mode operation is
retried. Ordinary public CI is recorded separately above.
This supplement supplies no genuine PMK, native, composed compiler, current-main
integration, migration, self-hosting or release acceptance.
