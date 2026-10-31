# Checked-core request preflight and test execution

This increment moves the existing request preflight and two explicitly test-only
execution runners into `gravity.checked-core`. D1, D2, D3, D9, L1, L2, C1-C6,
C15, BOOT8 and TEST2 govern this boundary. Inputs are native Core request data,
verified C5 parent/context inputs, explicit Reader invocation and current Core
source/plan operations. Outputs are preflight acceptance or C6 rejection and
the existing template/digest/resolved-candidate/replay report bundle.

The structural runner remains test-only and does not authenticate C5 membership.
The executable test runner must preserve C5 owner verification and exact request
membership comparison before structural execution. Neither runner grants Core
owner admission, proof, publication, migration, release or self-hosting status.
The production owner boundary remains with Bootstrap.

Native carrier validation precedes every lookup/hash. Request shapes, declared
projection/alias identities, schema and scope retain their existing checks.
Execution must preserve both source-key bookends and the lifetime of the scoped
Core execution binding, including exception unwinding. The temporary seed owns
the dynamic Var; the stage must receive an explicit scoped-binding operation,
without requiring Bootstrap or copying lifetime authority.

Acceptance requires actual paired current-development Reader/C5 requests and
Gravity execution/replay bundles, real preflight and membership rejection,
complete before/after output/diagnostic comparisons, binding lifetime and
revision-drift adversarial tests, proportionate regressions, independent review,
exact-head CI and clean integration preflight. No quota/accounting, renewed pins
or historical denied Reader/native/migration/capture retry is introduced. This
early draft grants no acceptance or full Core/publication/release completion.
