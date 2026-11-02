# Explicit C11 selection at downstream construction

Owner: ACC11. Contract basis: C11, C12, C13, C14, B1 and OWN-06 PR545
(`a2011d9e1a8d91116dba933bbd12c4d91d336da0`). Historical candidate basis:
`d2e35467d26fde9bc241e235b4c5916ce9bbb601`; original PR552 head:
`462d0713a4fee5d862cf59485d65dd40ee3d81bc`, tree
`57f42978243b0b1c40a36ad733f1ab5f192396d0`.

## Current supported route

The four-argument `p15-s23-stage2-c13-c14-b1-packet-from-c11!` forwards
`[c11-artifact checked-core context interface-header]` to the existing current
public C11 owner. The supported public header is exactly
`{:interface :gravity/c11-mir :version 2}`. The owner performs fresh C11 v2
construction and report production before downstream work. The downstream
selector derives the version from that owner-produced report, checks exact
header/report parity, and never selects from caller-supplied report labels.

The selected v2 source contract and rule are compared against both the original
C11 artifact and the fresh report. The checked-core ingress is compared across
both records. The current C11 policy validator and policy-binding projection
then bind the Core/context policy; the resulting distinct policy binding must
match the artifact verification report, MIR pass-execution record and B1
preflight binding. The selection is retained at
`[:c11 :source-contract-selection]`, after which packet semantic, artifact and
actual-path identities are recomputed.

The three-argument packet route remains unchanged and retains its historical
unversioned behavior. The verification-report, `verify!`, and `authentic?`
wrappers propagate the explicit header. Public C11 owner failures occur before
downstream construction and retain their C11 diagnostics; they are not routed
through backend containment.

## Version boundaries and unaccepted work

The current main source-interface API accepts public C11 version 2 only. The
separate C11 source-contract DATA selector also contains a version-3
flat-bool-literal-sequence record, but the current public C11 API does not
admit version 3 and the corresponding Gravity producer functions are absent.
This port therefore does not claim or invoke v3, copy a Native v3 producer, or
invent a v3 fallback. C6/C11 source-contract DATA versions 1/2/3 are not the
same thing as the public interface version; only the owner-supported v2 route
is wired here.

The original PR's four authored paths (the two packet components,
`bootstrap/clojure/test/gravity/self_hosting/downstream_selection_test.clj`,
and this report) are fully accounted for:

- the two packet construction/verification components retain the useful
  current-consumer behavior described above;
- `downstream_selection_test.clj` supplies positive forwarding/order and
  negative owner, header, source-rule, ingress, policy, and identity controls;
- this report preserves the historical source and evidence limits.

No C13 Gravity source, C12 domain IR, C14 lowering source, source pin, load
order, transport format, or legacy contract was changed. Existing downstream
C13/C14/B1 source contracts continue to decide lowering legality; binding a
selected C11 contract does not legalize another source tuple.

## Evidence scope

The compiler-free controls evaluate the actual current packet dispatch and
verification forms in isolated namespaces with explicitly injected owner
functions. The pinned C11 producer forms are read from immutable current-main
commit `5ecc0003e5595e73b3fb2d2e79a98233b9dd8950`; their source hashes are
checked before isolated evaluation. These controls prove argument forwarding,
owner-before-downstream ordering, current v2 header parsing, current v2
contract/rule projection, policy-binding joins, identity recomputation, and
fail-closed tamper behavior. Injected owner results and synthetic packets are
NONACCEPTANCE evidence; they do not authenticate a Core, Reader, C11 product,
or downstream C13/C14/B1 product.

The PR453 handoff now supplies genuine current-v2 producer evidence on the
composed main: `v2-c11-public-construction-consumes-real-c6-artifact` ran as
1 test with 9 assertions and zero failures or errors
(`/tmp/uniform-pr552-v2-c11-public-after-560.log`). The live C6/C11 owner
route therefore no longer has the earlier fixed-C6-pin blocker. The earlier
5ecc final-main run that stopped before C11 construction is retained only as
superseded-base history; this consumer does not renew that historical pin.
The bounded four-argument packet-selection control separately exercises the
real C6/C11 producer tuple and rejects a tampered source mode at the C11
owner boundary. No full C13/C14/B1 source composition, native C compiler,
restricted process route, broker admission, release, self-hosting, or v3
product execution is claimed.

## Validation record

The focused selector suite ran in a fresh JVM against the pinned source
fixture with 9 tests, 113 assertions, zero failures and zero errors
(`/tmp/uniform-pr552-final-selector-5ecc.log`). The genuine current-v2
producer positive above ran separately with 1 test and 9 assertions, zero
failures and zero errors. The bounded four-argument current-v2 selector
control also passed with 1 test and 6 assertions; its downstream C13/C14/B1
owner is deliberately stopped at an injected boundary, so it is not packet
or backend acceptance evidence
(`/tmp/uniform-pr552-v2-c11-packet-selector-after-560.log`). The former 5ecc C6 mismatch log remains an
immutable historical nonpass for that older base, not current acceptance
status. The earlier 69d/4df genuine runs are superseded-base evidence only.
The language-boundary gate,
document/roadmap validators, `git diff --check`, and current-main composition
checks remain separate evidence. Any existing broader C13/C14/B1 ownership or
host boundary nonpass remains a nonpass and is not converted into PR552
acceptance.

Integration still requires independent review of the exact candidate commit and
tree, clean worktree, current-main base identity, and the coordinator's normal
preflight. This increment grants no OWN-06 completion, backend, release,
self-hosting, migration, or seed-retirement authority.
