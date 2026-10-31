# Checked-core source and plan runtime

This increment moves the existing C6 source lookup, plan identities, current
binding construction and execution runtime into `gravity.checked-core`.
D1, D2, D3, D9, L2, C1, C3, C4, C5, C6, C15, BOOT2, BOOT8 and TEST2 govern
the boundary. Gravity source retains core lowering semantics; Clojure remains
the explicit temporary seed runtime.

The stage consumes current source/toolchain snapshots, plan records, explicit
cache and scoped binding inputs, function names and native argument carriers.
It emits development plan bindings, source/function identities and actual
Gravity results or existing C6-VERIFY failures. Cache reuse must remain tied to
input bytes; toolchain drift and source changes must retain their existing
failures. Runtime carrier checks and host-error/interrupt behavior must remain
unchanged. Existing compatibility entry points retain their arities.

Bootstrap owns cache lifetime, dynamic scope, historical migration pin checks,
actual parent/source admission, proof transactions and artifact publication.
The extracted stage must not import Bootstrap or discover its Vars. Explicit
operations and configuration roots must qualify construction and receipt reuse;
changing a stage/operation/configuration root or thread binding cannot preserve
an old receipt. Legitimate scoped execution bindings must be explicit epochs.

Acceptance requires real current Gravity compilation and emitted/rejected Core
kernel results, exact before/after plan/result comparisons, focused source,
cache, drift, carrier, containment and root-interposition tests, proportionate
regressions, language/docs validation and independent adversarial review.
Historical Reader/migration/native-launch refusals are not retried or renewed.
No authenticated Core artifact, full lowering, type/effect/safety completion,
release, performance, migration, self-hosting or seed-retirement authority is
claimed. This early document records scope and grants no acceptance.

The six public APIs are source-path, plan-identities, build-plan-binding!,
source-key, current-plan-binding! and execute!, with exact arities and operation
inputs in namespace metadata. Fifteen callable ports and five scalar inputs
remain explicit. Cache storage and the cached-binding reify/IPending holder
remain separate Bootstrap objects. A lock alias names the same input cache cell;
it introduces no new lock or cache.

The first focused suite passes 8 tests / 383 assertions. All four complete
before/after comparisons pass: current binding/plan, accepted Core var with
resolved reference, foreign-var rejection and lexical-var rejection. The actual
current source compiles 390 functions with source identity
sha256:455f2d496e410ba8c5f4d17b2e4e558c6fb62306b614a6b9643107c8e1c82770
and plan identity
sha256:b614072c6258aa0d198fef8b217855577110a9fdc47242e6dbf3a38a71e099ce.
The rejections retain C6-LOWERING-GAP / qualified-var-reference-deferred and
C6-VERIFY / var-top-level-namespace-binding-required. These are real normalized
Gravity kernel results; they are not authenticated Core source artifacts.

The initial baseline probe assumed :form and a nested diagnostic vector; the
existing kernel uses :core-form and top-level rejection facts. That probe
nonpass and its initial binding remain history; the corrected probe's complete
binding is equal to the initial binding. A scratch generator delimiter error and
test-file delimiter errors were corrected before semantic tests. The intentional
write followed a non-evaluating write-guard check and successful generated-source
parse/load; no dry-run source mutation occurred. Direct source/test lint now
passes with zero errors or warnings.

Receipt qualification captures every stage, compatibility, operation, factory,
input and configuration root after they are bound. The dynamic execution root
is checked separately, while its scoped value is part of the receipt epoch.
Equal-content plan bindings are semantic data and may reuse; changing the actual
plan with unchanged advertised hashes must miss. Construction bookends also
track the scoped execution binding and reject changed runtime inputs before
later owner work. Current checks must not be treated as migration/pin renewal.
