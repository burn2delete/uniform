# Core construction operation roots

## Contract and failure

D1/D2/D3/D8/D9, L2/L3, C1/C6/C15, BOOT1/BOOT8 and TEST2 govern
construction dependency checks. A captured compiler operation root must remain
the same operation throughout construction. Source, plan bindings, carrier,
reader, phase, thread, request membership and upstream verification retain
their separate checks.

The documented public development-current-reader route currently rejects a
complete source with C6-VERIFY / construction-dependency-drift. The retained
CLI control is `/tmp/uniform-cli-core-arguments-before-control.log`, report
`/var/folders/3y/hpny2h8j4jn2qwyxxrzm3hb00000gn/T/clojure-6726046598742302627.edn`.
CLI source remains unchanged. The failing guard runs inside the builder's
legitimate binding of `*sh07-core-execution-binding*`; that Var is included in
the roots snapshot. `var-get` observes its thread-local binding instead of its
root. The fix observes native Var roots at both snapshot and check.
It neither removes a guarded Var nor bypasses a check.

## Development evidence

Two new native guard-unit tests reproduce both failures before the fix:
two tests/five assertions, three failures and one error. The old implementation
rejects a legitimate local plan binding and misses a physical root replacement
masked by a local binding. The corrected tests and existing safe unit controls
pass five tests/18 assertions, including phase, thread, dependency, operation
root, carrier, error and interruption behavior. Their explicit guard models
construct no owner acceptance. Existing real-owner tests using historical
Reader paths were not run.

After the two source observation changes, the documented public
development-current-reader API emits accepted Core artifacts from both complete
`compiler-exact-keys.gravity` and `.qst` files. Each contains 25 nodes, reads its
target once, and passes actual template and resolved Gravity verification. The
actual source hash is
`sha256:8e63ea9e37ab40a8e66975b3e58dc8eb5b9b74aa339d2625d5bd67070802758d`;
the recomputed Core id in both spellings is
`sha256:16ec6d50a8514188f3010880f3022e88f7f1054fe4058a5caa6b74367ee98f0e`.
The complete current Core implementation binding contains 390 functions and
source hash
`sha256:455f2d496e410ba8c5f4d17b2e4e558c6fb62306b614a6b9643107c8e1c82770`.
Its development-unadmitted, generic-seed and incomplete/self-hosting-false
boundaries remain intact. This is new successful emission after an invariant
repair, not parity credit from the failed baseline.

Full products, actual provenance, arguments, read observations and identities
are captured under `/tmp/uniform-cli-core-arguments-evidence/root-fixed-*`.
The script is `/tmp/uniform-cli-core-arguments-control.clj root-fixed` and logs
use `/tmp/uniform-core-operation-roots-*`. No guard is redefined, no caller
supplies an execution plan, no authenticated packet is synthesized, no pin is
changed and no failed source is projected. The source-read counter wraps only
the original source reader and forwards it; the actual root mismatch was
confirmed independently by native Var tests and the builder's binding scope.

Language boundary6/11, docs240/19, roadmap and governance185 pass. Contextual
lint, independent exact-source review, own CI and clean current-main preflight
remain required for integration.

## Unsupported boundaries

This is a temporary Clojure adapter correction. It grants no source, Core,
profile, type, effect, ownership, safety, native/backend, full C6 completion,
migration, self-hosting, release or seed-retirement authority. Thread-local
execution-plan selection remains separately bound to the captured source plan;
the root snapshot is not a caller-selected execution binding.
