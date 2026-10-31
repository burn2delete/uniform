# Resolution source and plan runtime

This increment moves the current SH-06 source authentication, plan binding and
execution algorithms into gravity.resolution. D1, D2, D3, D9, L3, C1, C3, C4,
C5, C15 and TEST2 govern this boundary. Gravity-authored resolution remains the
semantic authority. Clojure remains the explicit temporary seed.
Operation maps are trusted implementation dependencies, not an authority or
sandbox boundary; arbitrary supplied operations cannot establish acceptance.

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
this extraction.

The current-cohort-unmetered fixtures emitted accepted artifacts with 265
bindings and three resolutions in both .gravity and .qst. Their identity is
sha256:49d37a2f0d79fb8902e3be9928cab495e734997f5e3e15bfee186ae5b9ed8bc1.
Complete before/after artifact values compare equal for each spelling.

The receipt guard tracks stage, factory, operation, static configuration and
cache Var roots without executing replaceable stage code. The legitimate
dynamic operation binding keeps its original scope; its raw Var root is
tracked, and its complete value is an epoch input so actual plan changes cannot
hide behind unchanged advertised hashes.

The existing construction drift seam also records explicit stage and factory
Var dependencies, so unchanged compatibility forwarders cannot hide a changed
implementation. It retains its original later owner-verification requirements.
Cheap receipt qualification tests isolate the contained verifier and contextual
admission. They do not establish
semantic or source authority. Separate real source execution and verification
supply that evidence. Interrupted lifetime tests isolate the Core prelude and
exercise the production cleanup path without Core admission.

The older bootstrap-compatibility.c5-test selection stopped at its frozen
Reader source guard: C2-HASH, expected 231576 bytes and observed 231666. That
nonpass remains recorded; no Reader pin or guard was changed. Current-source
SH-06 tests use the existing explicit development invocation. The initial
extraction also exposed an incorrectly auto-resolved legacy context keyword;
its original gravity.bootstrap identity is restored before the passing paired
artifact comparison.

Local validation passed the complete current-source CI selection (24 tests,
578 assertions), ownership/catalog/language selection (51/1037), source-version
conservation (16/212), acceptance dependencies (14/402), and CI harness (11/101).
After naming the existing shared cache lock, its focused policy regression
passed 1/10. Changed-source lint reports zero blockers with 3505 unchanged
baseline warnings. Structural docs (240/19), the full-language roadmap and the
crosswalk projection pass. The new namespace cold-loads ten public APIs without
loading bootstrap. The final candidate still requires independent acceptance,
exact-head CI and integration preflight; local results alone grant none of those.
