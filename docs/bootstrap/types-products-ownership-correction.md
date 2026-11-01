# Types products: exact ownership registration correction

D1, D2, D3, D9, BOOT8 and the existing SH-01 ownership contract govern this
metadata correction. Source candidate
7441bbbb6244c75d9cf9359b2a6c9719ee2f5ddc, tree
77d53a46f415a8a1aa1e20f9ca513cd54031d071, remains the retained implementation.

## Original failed observation

CI run 41200102977 at that head failed Bootstrap job 124534050947. Its tested
merge was 76586bb21ec2bccdef0061bbd0180b073f39a0a1 with the exact candidate
tree above. The ownership child exited 1 after 20 tests / 799 assertions with
two failures: 117 actual stage0 paths versus 116 declared, and unequal complete
path sets. The added gravity/types.clj path was not registered. Reader and
syntax regressions, cold Bootstrap load, language and catalog controls passed;
the printed fail-fast sentinel was an intentional control whose enclosing
catalog result passed. The cause is not the separate frozen Reader C2-HASH
source-control refusal.

The original run is terminal failure, without retry or waiver. Downloaded
receipts and every SHA256SUMS entry verify. The checksum-list digest is
bede084fe9595cdca28e3e00144278010827caa0b27ec87de3740bd748ea13de.
The failed head and source acceptance are preserved as history, not integration
eligibility.

## Concrete correction and validation

Register gravity.types as a production helper owned by sh-types / SH-08, with
its actual inference consumer and existing bootstrap-free C7 catalog test.
The ownership EDN and strict JSON component mirror name the exact source,
consumer, namespace, test and registration. The C7 primary pair and 53-entry
leaf catalog remain unchanged; this adds no standalone test reservation.
Existing count formulas derive their additional helper from this registration.
No count constant, inventory equality, owner check or gate is weakened.

The ownership regression now passes 21 tests / 820 assertions, including new
negative owner, missing registration, namespace, consumer, test and registration
kind cases. The actual existing C7 leaf runner passes 7 / 176 with the unchanged
catalog hash. All seven type bodies/arities, wrappers and original product
evidence remain unchanged. There is no compiler, semantic, pin, CI configuration,
authority, milestone, migration or release change.

Language boundary, exact changed-file lint, independent acceptance of this
metadata delta, current-main composition, final-head CI and integration preflight
remain required. Main's later Reader/compiler/runtime changes require their own
real composition and verification; they do not erase this failure.
