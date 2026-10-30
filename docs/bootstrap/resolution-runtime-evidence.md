# Resolution source and plan runtime

This increment moves the current SH-06 source authentication, plan binding and
execution algorithms into gravity.resolution. D1, D2, D3, D9, L3, C1, C3, C4,
C5, C15 and TEST2 govern this boundary. Gravity-authored resolution remains the
semantic authority. Clojure remains the explicit temporary seed.

The stage consumes source paths/bytes, compiler and emitter snapshots, plan
records and explicit seed operations. It emits source identities, development
plan bindings, function identities and actual Gravity execution results. The
current-development and frozen pinned-source routes keep their distinct checks
and authority records. Neither route grants migration or release acceptance.

The intended public APIs are boundary-fail!, resolve-source-path,
read-current-source!, read-pinned-source!, plan-identities, build-plan-binding!,
source-key, binding-current!, current-plan-binding! and execute!. Namespace
metadata will record their exact arguments and required operations. The stage
owns these algorithms; bootstrap retains the source/version configuration,
process-local cache atom, operation binding and compatibility entry points.

C5 owns plan/source and contained host-failure diagnostics here. Actual binding,
visibility, profile, target and capability diagnostics remain with the Gravity
resolver and its existing diagnostic catalog. The stage must have no dependency
on gravity.bootstrap; seed composition supplies host operations explicitly.

Acceptance requires executing the actual current Gravity resolver, emitting an
accepted namespace-analysis product and rejecting an actual invalid resolution
request. Source and plan substitutions, source/toolchain drift, cache lifetime,
thread/invocation binding, host containment, metadata, deterministic identity and
paired extension provenance need proportionate regression/adversarial checks.
New stage dependencies must not permit stale SH-07 receipt reuse. Existing
compatibility arities and diagnostics must remain stable.

The request builder, artifact assembly, authenticated parent transport,
verification, legacy hosted C5 engine and remaining resolution helpers remain
outside this increment. Do not infer complete resolution, self-hosting,
seed retirement, performance, authoritative migration or release support from
this extraction. The draft PR will record actual checks and immutable evidence
as implementation progresses.
