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
root. The proposed fix observes native Var roots at both snapshot and check.
It neither removes a guarded Var nor bypasses a check.

## Evidence required

Reproduce the distinction with native dynamic Vars without constructing owner
acceptance. A legitimate local binding must leave the root snapshot unchanged;
a root replacement must remain detectable even inside that local binding.
Preserve the existing phase/thread/dependency and real-owner guard tests at
their permitted boundaries. The historical Reader/H6/native paths remain off.
After correcting the actual root-observation invariant, use the documented
current-reader public source API to emit real complete-source Core artifacts,
recompute source/Core identities and retain real refusals. Do not prewarm or
redefine the guard, synthesize an authenticated packet, change a source pin or
project a failed compilation.

Run proportionate focused tests, language/docs/roadmap/governance, contextual
lint, independent exact-source review, own CI and a clean current-main
integration preflight. This initial scope record provides no implementation
or integration credit.

## Unsupported boundaries

This is a temporary Clojure adapter correction. It grants no source, Core,
profile, type, effect, ownership, safety, native/backend, full C6 completion,
migration, self-hosting, release or seed-retirement authority. Thread-local
execution-plan selection remains separately bound to the captured source plan;
the root snapshot is not a caller-selected execution binding.
