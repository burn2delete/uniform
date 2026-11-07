# Native zero-cost abstraction claim validation

## Scope and contracts

Move the complete existing PERF2 family in foundation/00140 into
`gravity.optimization`: five policy values and nine functions. Preserve the
original Bootstrap names as policy aliases and function adapters. Keep 00141
artifact assembly, the file facade and CLI wiring in Bootstrap. This draft
records the boundary before changing production code.

D0, D1, D2, D3, D6, D8, D9, C1, C11, C12, C13, C15, SAFE15,
PERF1, PERF2, PERF10, BOOT8 and TEST2 govern this temporary seed extraction.
PERF2 concerns physical allocation, boxing, dispatch, reflection and runtime
checks. It does not reinstate project accounting, admission credits, quotas,
work budgets or cost debits.

## Public boundary and artifacts

Add five native policy values:
`native-zero-cost-required-claim-fields`,
`native-zero-cost-required-nonempty-claim-fields`,
`native-zero-cost-required-artifacts`, `native-zero-cost-abstraction-families`
and `native-zero-cost-cost-diagnostic`, plus these nine function APIs:

```clojure
(native-zero-cost-normalize-claim claim)
(native-zero-cost-missing-claim-fields
  present-var required-fields-var nonempty-fields-var claim)
(native-zero-cost-failure!
  failure-var id source-path manifest performance-claim claim extra)
(native-zero-cost-ir-artifact-id claim key)
(native-zero-cost-costs-in-ir claim ir-key)
(native-zero-cost-hidden-costs costs-in-ir-var claim)
(native-zero-cost-missing-evidence
  present-var artifact-id-var required-artifacts-var claim)
(validate-native-zero-cost-claim!
  missing-fields-var hidden-costs-var missing-evidence-var failure-var
  abstraction-families-var cost-diagnostic-var
  source-path manifest performance-claim claim)
(native-zero-cost-capability-proof
  present-var artifact-id-var manifest performance-claim claims)
```

Inputs are caller-owned native claims, manifests, IR-shaped records and explicit
late compatibility Vars. Outputs retain normalized claims, ordered missing
fields, supplied IR identifiers, categorical cost/evidence sets, owner results
or exceptions, relative `:complete` and native boolean proof envelopes.
The validator ignores returning failures and returns `:complete` if all invoked
operations return; uncaught operation exceptions propagate unchanged. A passed
label or a `:status :complete` proof envelope is not verifier authority.

Optimization owns the nine PERF2 rule decisions, messages, remediation and
diagnostic facts. Generic failure construction/publication/transport remain
with the supplied owner. Preserve extra-fact override precedence, lazy reads,
historical policy/helper Var interposition and every original call boundary.

Dependencies become Bootstrap -> Optimization -> Clojure core and the explicit
unaliased `clojure.set` library. Keep owner aliases empty; use fully qualified
set operations. No Bootstrap, Diagnostics, backend, CLI or release import is
added to Optimization. No new namespace, component JSON or CI command is needed.

## Real source controls and verification

Use the existing accepted `zero-cost-abstractions.gravity` fixture and all
nine rejected zero-cost fixtures through `zero-cost-source-artifact`, paired
with `.qst` source paths. This route emits the existing five-claim PERF2
artifact through PERF1/profile/typed/Core/macro/module composition. It is not
the compiled-plan route: the Stage0 performance plan gate does not invoke PERF2.
Capture whole before/after products and stable diagnostic facts, source paths,
metadata, upstream artifact hashes and output identity under the pinned runtime.

Extend the existing Bootstrap-free C13 leaf for direct owner contracts,
policies, all refusal categories, late Var/getter order, original exceptions,
returning failures and native proof-envelope limitations. Update the existing
Optimization pipeline API inventory without running its private carrier path
locally. Keep genuine source-artifact regressions with the existing Bootstrap
test owner, selecting only inspected exact test Vars. Focused test placement is
being reviewed before any namespace execution.

Run source/integration regressions, cold owner loading, lint, language/ownership
gates, document/roadmap validation and the changed-file comparison against Main.
Freeze a clean reviewed commit, maintain the early draft PR and audit its own
provider CI. Independent source/CI and current-base integration remain separate;
no predecessor acceptance transfers.

## Unsupported boundaries

The historical hidden-cost set traversal has no declared precedence for several
simultaneous category failures. Preserve it without inventing a new priority
algorithm. Identity evidence is scoped to the same pinned runtime/source input;
no cross-runtime or multiple-hidden-cost diagnostic-precedence claim is made.

The diagnostic catalog and audit traversal cover only their existing categories.
An unknown cost present only in post-IR data can evade category refusal unless
it is also declared residual or expected-but-unerased work. Tests and evidence
must state that limitation. The native capability proof reports booleans with
`:status :complete`; it does not independently admit an invalid claim or prove
that callbacks throw. Preserve the original envelope rather than fabricating
stronger evidence.

Supplied MIR ids, proof ids, audits, fingerprints, benchmark/guard labels and
effect/capability records are not authenticated or compared with actual target
execution. This slice performs no optimization, benchmark, check removal,
certificate verification, backend/native execution, source admission, release,
migration, self-hosting or seed retirement. No held Reader/authentication,
private source-factory or pin route is used for local controls.
