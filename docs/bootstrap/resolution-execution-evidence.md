# Resolution execution and declared digest materialization

This increment moves the C5 request execution algorithm and its carrier,
declared-digest, analysis, rejection and order-projection helpers into
`gravity.resolution`. D1, D2, D3, D9, L3, C3, C4, C5, C15, BOOT8 and TEST2
govern the boundary. Existing Gravity functions retain resolution semantics;
the temporary Clojure stage owns explicit seed execution and materialization.

The stage consumes an owner-admitted request and plan binding through explicit
operations. It must preserve native carrier rejection, fresh template and
resolved-result verification, complete ordered digest requests, legal digest
indices, deterministic identities, exact rejection shape and diagnostic
provenance. Pending digest references used during identity construction confer
no artifact or publication authority. No project accounting or input quotas may
be reintroduced.

Bootstrap retains source and parent admission, contextual authority, envelope
ownership, proof transactions and final publication. Existing entry points and
interposition remain compatible. Public APIs and their dependencies must be
recorded with actual implementation, not callable stubs. Stage/configuration
and operation roots must continue to invalidate stale receipts.

Acceptance requires focused accepted/rejected execution and digest tests,
real paired library/consumer artifacts and unresolved-reference rejections,
complete product/diagnostic comparisons, proportionate regressions, independent
review and exact clean integration preflight after successful CI. This early
boundary document grants no acceptance. Complete resolution, current Reader
migration, release, performance, self-hosting and seed retirement remain
unclaimed.

The nine public APIs are require-carrier!, digest-reference,
resolve-declared-id!, resolve-record-binding-id!, resolve-analysis!,
resolve-digest-requests!, raise-rejection!, run-request! and order-compatibility.
Their exact arities and operation inputs are recorded in the namespace contract.
The stage cold-loads 55 public APIs without Bootstrap. Direct source/test lint
passes with zero errors or warnings. Existing Bootstrap wrappers retain their
arities and explicit operation interposition. The ten added operation ports are
bound before the existing late verifier snapshot; construction drift roots
include the new stage and operation Vars. Existing diagnostic catalog input and
all source/profile/effect/capability metadata stay unchanged.

Outputs are materialized analysis, declared identities, the existing verified
run record and exact C5 rejection diagnostics. Native carrier validation,
plan execution, hashing and catalog access are explicit seed dependencies.
The pending-artifact-id flag is only an intermediate digest-construction mode;
it does not publish an artifact or supply proof authority. Every new API is
marked admission:none. Focused tests, full regression, real paired artifact and
rejection parity, contextual lint and independent review remain pending.

Focused execution/digest, carrier, phase-order, contract and root-interposition
checks pass 7 tests / 1584 assertions. The accepted and unresolved-rejection
execution test runs the actual current Gravity resolver leaf on normalized
requests. It is semantic execution evidence, not authenticated source or
publication admission. Broader regression and paired full-artifact comparisons
remain in progress.

The full affected resolution regression passes 54 tests / 2293 assertions,
zero failures and zero errors, on source ee4c01f1 with the final 0db5585d tests.
All six complete before/after comparisons pass: actual library and consumer
artifacts plus generated C5-UNRESOLVED rejection payloads in both .gravity and
.qst spellings. Each consumer retains one freshly verified library premise;
all four accepted products pass fresh public verification. Library identity is
sha256:7cee4a4156c5eb1ddd4ed19e94a6ee6938bd19bb594cdc495863eb648f8cf0a4
(267 bindings / 10 resolutions); consumer identity is
sha256:4b7cf3c7880ab84ad20efb41b158d70f43392107a8a107724473d7fdb935903d
(267 / 6). Both listish? rejections retain two generated origin records.
Whole-payload equality includes actual source and header provenance.

These comparisons bind the 3e608639 baseline source context; the intervening
094e5a77 merge changed documentation only. All nine moved algorithm bodies are
conserved after reversing the explicit operation/input seam and normalizing
reader-generated anonymous symbols. Contextual lint passes zero blockers,
language boundary 6 / 11, documentation 240 / 19 and roadmap validation pass.
Independent source review found no blockers on 0db5585d; exact evidence-only
acceptance, current-main composition, successful final CI and clean integration
preflight remain required. No source, admission or release authority is promoted.
