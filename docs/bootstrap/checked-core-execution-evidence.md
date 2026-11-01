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

The stage now owns `request-preflight!`, `run-structural-request-for-test` and
`run-request-for-test`, with 53 public functions and four private helpers in
total. Namespace metadata records their exact arities, operation inputs,
artifact flow and diagnostic ownership. Both runners are explicitly test-only;
only the executable runner authenticates C5 membership, and every API retains
admission:none. The stage requires only clojure.core and forbids Bootstrap.
Bootstrap retains the dynamic Var, its cache and the small explicit
with-execution-binding callback. Callable operations remain Var ports; new stage,
compatibility and scope-helper roots join both construction and receipt guards.

The extraction conserves all three original bodies and every arity after
reversing only operation lookups, scalar inputs and the scoped callback. All
unselected runtime and historical pin definitions are unchanged. Strict EOF
reading covers whole files; cold loading exposes the stage without loading
Bootstrap. Direct stage/test lint has zero errors or warnings. A fresh load
caught three missing forward declarations before any post-extraction execution
evidence was produced. They were added to the seed declarations; the failed
load is not counted as test evidence.

The initial focused selection passes 5 tests / 260 assertions. It covers real
native-first validation and request shapes/hashes, dispatch arities and legacy
context identity, owner rejection before execution, exact membership rejection,
scoped binding selection/restoration, source-key bookends, both replay failures
and invalid result status. Additional checks exercise a different nested binding
unwinding on exception and unchanged rejected-result diagnostic propagation.
The scoped callback owns only dynamic lifetime; it cannot confer C5 or Core
admission. Model operation tests are separate from actual paired source runs.

Actual current-development Reader/C5 requests for source-occurrences.gravity
and source-occurrences.qst execute the current Gravity Core plan through the
public test runner. Each emits 43 nodes and 46 resolved digests with passing
template and resolved replays. The candidate identity remains
`sha256:462f37046c92a22b5f519155432752c7bbc068a015e9389f12ed09ada07777cf`.
The current Core source identity is
`sha256:455f2d496e410ba8c5f4d17b2e4e558c6fb62306b614a6b9643107c8e1c82770`;
its 390-function plan remains development-unadmitted and self-hosted:false.
Changing the actual request projection binding raises C6-VERIFY with
request-projection-binding-mismatch at preflight and
authenticated-sh06-request-membership-mismatch at the executable test boundary.
No C5 parent is admitted by a structural-only runner.

Complete parent, request, current binding identities, preflight result,
execution bundle and rejected diagnostics compare equal before and after for
both spellings: twelve complete-data and serialized-text comparisons. Both
actual source-run processes exit 0. The fresh-process runtime CI mode passes
stage 31 tests / 2070 assertions, current-binding/kernel 4 / 57 and projection
kernels 3 / 286: 38 tests / 2413 assertions, zero failures/errors. The language
gate passes 6 / 11; docs validation passes 240 docs / 19 phase indexes and the
roadmap structural validator passes without granting completion authority.

The source prefix uses the retained PR686 base, not the later Reader change
on main. That Reader change alters construction and verification inputs and
requires explicit dependency composition checks before integration. Independent
source acceptance, contextual lint, exact-head CI and clean integration
preflight remain required. PR684's final aggregate Resolution CI timeout is
still a real NONPASS, with cause, active selector and descendant cleanup
unknown. This slice neither reruns that head nor clears it, and changes no
shared CI path or watchdog.
