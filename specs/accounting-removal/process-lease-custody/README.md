# Normal process result and broker failure custody

This finite correction addresses the specific `with-lease -> process-result
-> return/throw -> release!` path identified by OWN. It does not adopt or run
OWN's three ALIAS selectors. The original ACC06 native binding goal remains
blocked. The separately rejected publication of `7b6a0f6c` is stopped; that
frozen candidate is not changed or rebased.

## Source basis and disposition

Inputs are `/tmp/own-alias-action-specific-assessment-20261030.edn` (SHA256
`800cecdc388aa77e452fe20ec97fd0142d592cbdf2313bb5cd36a8896fa81752`) and
`/tmp/own-alias-action-command-pins-20261030.edn` (SHA256
`0aa3fd1506da4075c2ca14cbe1228d46396e8c4677e858122dca5f08b7a039a1`).
Their subject is `30a8cda181b704eac9b390aa653f5017f6624e83`, tree
`1da1310c60209b423bf59b6a3813171e67225fe3`. Only their broker/normal-runner
failure-custody question is assessed here, not the whole selected creation
closure or every classpath inventory entry. The subject checkout was read
clean at that exact commit. Its pinned runner SHA256
`3ddb5fb8e68debdd21e15032b318b6eb09e0ae799770957e371625db7a462456` and
broker SHA256 `0126b1d96f11d7e6908ea693f7f29a6d3d139cf30f22471ff6525229ca5ece20`
also match the unmodified files at this candidate's main base
`787637b9ae780a7cfef7ca5763611882a8f2b629`. That newer main contains finite
Resolver preparation; no acceptance from the older `70dcb222` composition is
inherited or claimed.

The documented cooperative broker is not an OS containment mechanism. That
qualification explains loss of file locks on supervisor death. It does not
establish terminal root/capture facts while a live supervisor is about to
release capacity. The old `with-lease` unconditionally releases in `finally`,
while `process-result` can report a live root, nonterminal tracked handles,
capture timeout/error/forced close, or throw. Root-only workload reasoning
does not dispose that specific failure path. A source correction is needed
for strict failure custody on this route.

Governing basis is D8 linear resource safety, D9 accurate evidence and explicit
nonclaims, T1 stable failures, TEST2 focused positive/negative controls, and
the existing host-broker contract in `docs/development-verification.md`.

## Small correction

The existing `with-lease` binds an in-JVM custody collection. Normal
`process-result` registers its actual validated ownership token before the
wait/capture operation, and records either its unchanged result or its
original propagated Throwable. No process-result arity changes. No additional
launch, wait, finalization, termination, observer or retry is introduced.

The lease releases only when every registered process reports an exited root,
false root-alive flag, terminal tracked handles and both complete or
EOF-drained truncated captures, without capture errors or forced close.
Neither exit-code success nor unknown descendant coverage decides release.
A completed nonzero exit releases; a timeout whose root/tracked handles and
captures actually became terminal can release without promoting its failed,
unproven-containment result. Missing or contradictory observations quarantine.

On incomplete return or process throw, a broker-owned map keeps strong
references to the actual lease, ownership tokens and observations/errors.
Slots are not unlocked, and `:quarantined` telemetry carries
`SH01-BROKER-PROCESS-CUSTODY`. Results, stdout/stderr, errors, exit status,
cleanup flags and unknown descendant coverage are not rewritten. No automatic
clearance is added. Recovery/disposition of a real quarantine remains separate
owner work; this task neither creates nor clears one.

Nonprocess thunks preserve their existing return/throw/release behavior.
Unleased process-result calls and manual `acquire!`/`release!` users, including
the separate development-loop `brokered-result`, are unchanged. This correction
does not claim to repair every broker caller, pre-token launch failure,
asynchronous work escaping its thunk, supervisor death or arbitrary resource
closure. Nested acquisition remains unsupported as documented.

## Compiler-free modeled evidence

`check.clj` loads only the broker tooling namespace. It reads the actual
modified `process-result` definition with read-eval disabled and evaluates that
single Clojure definition in a private model namespace. Its process wait,
capture, finalization and interruption dependencies are model functions. The
broker acquisition and release functions are replaced with identity-checked
in-memory models; the fake Process never launches and refuses wait, exit
observation or termination. Neither the runner/planner nor Bootstrap/Gravity
namespaces are required. These controls do not execute the genuine runtime
implementation and do not claim actual OS/lease behavior.

Run with only the Clojure, spec.alpha and core.specs.alpha jars on the classpath:

```text
java -Xmx256m -cp <three-core-jars> clojure.main specs/accounting-removal/process_lease_custody/check.clj
```

The controls cover normal/failed exits, EOF-drained truncation, failed timeout
with terminal observations, 26 missing/contradictory/failing result cases,
original exception and suppressed cleanup-error identity, multiple process
registrations, a later nonprocess throw, and unchanged unleased/nonprocess
behavior. The repository language-boundary gate is separate tooling evidence.

Independent affected source review is required before this candidate receives
acceptance. No actual child, lease acquisition, process probe, ALIAS selector,
Gravity/compiler runtime, publication, CI, merge or held retry is performed.
All original denied/unknown operations and the original blocked goal remain
unchanged. This creates no blanket prohibition or bespoke binding prerequisite
for ordinary permitted development selections.
