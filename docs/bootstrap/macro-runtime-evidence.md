# Macro plan execution boundary

This increment assigns the pinned Gravity macro plan adapter to `gravity.macro`.
L4, L12, L16, SAFE12, C1, C3, C4, C15, D1, D2, D3 and D9 govern the boundary.
The implementation work moves executable algorithms from bootstrap into the
stage; bootstrap retains pinned configuration, process-local lifetime and
explicit compatibility operations.

The stage inputs are pinned Gravity source/plan configuration, syntax macro
requests, explicit seed operations and transaction-free plan state. Outputs are
verified plan bindings, resolved expansions and ordered digest/verification
records. C4 owns plan, return, trace and authority failures; host interruption
and containment behavior must remain observable. Generated source retains
reader identities, call/definition origins and deterministic semantic digests.

Dependencies flow from authenticated syntax through macro execution to expanded
syntax. The stage must not import or discover bootstrap. Seed callbacks remain
explicit host boundaries and confer no proof authority by their presence.

Acceptance requires genuine paired source expansion, real invalid-request
rejection, full artifact and plan identity comparison, direct stage adversarial
tests, language-boundary and affected regression checks, independent review and
exact clean candidate integration. The draft PR records executed checks and
the final source tuple as implementation progresses.

This increment does not claim completion of the macro stage. Artifact assembly,
source snapshots, compatibility routing, older macro helpers and their remaining
accounting removal remain open. No release, migration, seed-retirement,
self-hosting or performance authority is established by namespace extraction.
