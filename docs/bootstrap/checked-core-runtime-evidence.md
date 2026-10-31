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
