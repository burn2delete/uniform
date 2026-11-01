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

The initial broader selection reported two failures in 14 tests / 478
assertions. Both reproduce unchanged on the abf3b0c7 baseline control (2 tests /
14 assertions). One cache fixture selected the compiler file as its emitter
then edited an unobserved emitter file; its two explicit source ports now point
to the distinct fixture files. The other older artifact-construction model
enters current owner admission with empty artifacts before its intended revision
race. Its baseline nonpass remains recorded; no owner gate or historical refusal
was bypassed to force that model through.

The applicable runtime selection now passes 13 tests / 474 assertions, zero
failures and errors: all nine new stage/runtime/receipt/construction tests plus
four existing current-binding and normalized-kernel checks. This is not a full
SH-07 artifact or migration run. CI adds a checked-core-runtime mode that runs
those nine tests and the four existing checks in separate fresh processes.
The older owner-gated artifact model is not counted as runtime evidence.

The actual checked-core-runtime CI command also passes locally in fresh child
processes: stage suite 9 tests / 417 assertions and current-binding/kernel suite
4 / 57. The runtime body conservation check passes for all six moved functions,
after reversing only the explicit operation/input seam and cache-lock alias.
All unselected definitions, including the historical pin-check body and its
configuration, remain conserved. The scope does not include the baseline
owner-admission model or authenticated Core artifact construction.

Final prefix static checks pass: changed-file/context lint has 3503 unchanged
baseline warnings and zero blocking findings; cljfmt 0.13.4 accepts tools/ci;
actual CI harness controls pass 11 tests / 106 assertions. No lint policy or
warning baseline was relaxed. Independent prefix review accepts d2255ffc
conditionally; final evidence-only review, current-main composition, exact-head
CI and final clean integration preflight remain required.

The first exact-head CI run at b9fcb4bd exposed an omitted production-helper
ownership registration for gravity.checked-core. The ownership EDN and stage0
component JSON now join its real source, SH-07 owner, Bootstrap consumer and
standalone stage test. The 55 primary component pairs remain unchanged. The
ownership suite passes 20 tests / 798 assertions, including rejection of forged
owners, namespaces, consumers and test registration. The language boundary
gate passes 6 tests / 11 assertions. This registration carries no migration,
release or seed-retirement authority; new exact-head review and CI are required.
