# Native zero-cost abstraction claim validation

## Scope and contracts

Move the complete existing PERF2 family in foundation/00140 into
`gravity.optimization`: five policy values and nine functions. Preserve the
original Bootstrap names as policy aliases and function adapters. Keep 00141
artifact assembly, the file facade and CLI wiring in Bootstrap. This draft
opened at clean scope `e61eb7143666ca8e42605e94fb3e021e92d316f5` before
changing production code.

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

## Extraction evidence

The nine complete function bodies and five policy definitions now live in the
owner, increasing its public inventory from twenty-three to thirty-seven.
Raw-body receipt
`3d7e7d35564ab95526e8dda8e6857ddcfafe45dc022e00ff4e5931ecfdb450c4`
retains original and moved text. Only names, explicit arguments, operation
references and fully qualified `clojure.set` references change; map forms are
not walked or reprinted. The Bootstrap component retains nine function adapters
and five policy aliases. No outer artifact assembler or caller is moved.

All twenty complete paired source observations match: two real five-claim
PERF2 artifacts and eighteen genuine refusals, covering all nine diagnostic
classes. Captures include the actual upstream macro/Core/typed/profile/module
and performance products, output projections, helper arguments/results and
complete failure payloads. Existing performance-artifact hash links are
recomputed; repeat source controls preserve the output value under the same
pinned Clojure 1.12.5 runtime. No artifact-id field is invented for an output
which does not have one.

Baseline receipt
`91f28adccc5102a50fb49c4bcc0784a74660667ff68e302deb9f9cef3a5235e4`
binds the actual clean scope commit. After receipt
`fea48e5b8222ffc3cc3b143fd7c8d4fed1d682703a2ceb2e20bf87172caed66a`
binds that same HEAD with two dirty producer hashes, not a later clean commit.
The `.qst` cases use the existing Gravity fixture text with caller-supplied
paired source paths; file authentication is not claimed.

Bootstrap-free native controls pass twenty-nine tests / 424 assertions.
Existing whole plan/instruction regressions pass 45 / 1004, and language/
ownership gates pass 48 / 1202. Native controls cover every policy and refusal
category, same-runtime hidden-cost selection, live diagnostic policy changes,
lazy field reads, original owner/exception identity, returning failures and
the unsupported unknown post-IR cost case. Native proof envelopes retain
`:status :complete` even with false preservation fields or vacuous empty input;
the tests explicitly do not promote those envelopes to admission authority.

The first focused source test run passed the actual artifact/refusal controls
but incorrectly expected an interposed IR-cost vector to be normalized into
a set. Original `clojure.set/union` can retain that largest borrowed value
as a vector when the other inputs are empty. A second incorrect expectation
assumed object identity; the variadic union reduces `into`, which may copy the
vector. The corrected regression checks original value, vector type and exact
forwarded arguments. Both failed test logs are retained. No production
semantic change was made for these expectations.

Focused source tests, exact namespace-load guard results, final lint/doc checks,
full Main comparison, independent reviews and own hosted CI are supplemented
in the maintained PR as they complete. The private compiler-carrier pipeline
suite is not a local execution route for this slice.

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
