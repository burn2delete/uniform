# Runtime implementation planning entry

Select one unresolved [existing task](tasks.md#implementation-tasks), preserving
the historical transfer plan below. Read [spec.md](spec.md), the complete
selected R1-R12 family rules and relevant profile/safety/provider/ABI contracts.
Locate actual Gravity runtime semantics and temporary service adapters through
[development verification](../../docs/development-verification.md) and the
[stage ownership table](../../docs/bootstrap/stage-owned-namespaces.md).

Bind the actual artifact/profile/target, service classifications, linked/loaded
helpers, provider/grants, resource and failure/cleanup behavior and consumers.
Define a real allowed operation and relevant forbidden-service, lifetime/race,
foreign-boundary, denial, redaction, replay or session-invalidation negatives.
For handlers include suspension/resume/cancel/finalization and actual interpreted
behavior; metadata alone cannot establish it. Provider fixtures remain explicit
and cannot waive ordinary legality or held live execution.

Run focused family tests and affected Lower/backend/Effects/Auth/domain/CLI
composition. Inspect actual dependencies and action side effects, preserve
source/diagnostic/artifact identities and record failed/unrun work. Refine only
the existing selected task. Distinguish compiler-service subset acceptance,
wider families, independent review/integration and release; do not restore
removed project accounting as a runtime admission requirement.

## Foundation source criteria

Four preserved clauses from the
[foundation context](https://github.com/burn2delete/uniform/blob/df11890b21f71d3f246cc47e9f63d866239a0d68/specs/requirement-reconciliation/foundation-residual-interfaces.edn)
now refine two existing Open tasks; their IDs and complete predicates stay in
[tasks.md](tasks.md#implementation-tasks).

| Existing requirement | Canonical conditions and existing outcome |
| --- | --- |
| `gravity/explicit-runtime-service-dependencies` | Under [D1 runtime selection](../../docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md#runtime-architecture), [R1 service classification](../../docs/phase-08-runtime-architecture/112-r1-runtime-architecture-overview.md#service-classification) and [R2 generated support](../../docs/phase-08-runtime-architecture/113-r2-no-runtime-execution-model.md#generated-support), family selection records actual linked/generated/delegated/external/forbidden services with provider/version, effects/grants and source dependency. Exercise real use and reject hidden services. |
| `gravity/runtime-manifest-closure` | The same task closes that manifest transitively over real link/load/helper use and compares it with produced artifacts. Reject undeclared allocator/IO/reflection/dispatch/thread dependencies and unavailable no-runtime support. Generated or statically linked target support remains allowed only under its explicit R2 contract; `:runtime :none` is not an empty-service assertion. |
| `gravity/interop-boundary-manifest` | The existing memory/concurrency/FFI task produces and consumes applicable ABI/layout/alignment/calling-convention/symbol, ownership/lifetime/transfer, null/error/callback/thread, schema/provider, effects/grants and provenance obligations under [D5 interop](../../docs/phase-00-foundation-and-thesis/006-d5-language-replacement-strategy.md#interop-rules) and [R10 bindings](../../docs/phase-08-runtime-architecture/121-r10-ffi-runtime-design.md#binding-and-adapter-model). Actual conversion/invocation, release and callback re-entry remain required where claimed. |
| `gravity/interop-is-not-host-semantics` | The same FFI task normalizes admitted values, failures and callbacks through audited typed Gravity boundaries. [R4](../../docs/phase-08-runtime-architecture/115-r4-managed-runtime-design.md#requirements) preserves null/undefined, exception/promise, collection/equality/hash, resource, reflection/loading and source-map contracts. Reject unadapted host semantics, invalid ownership, wrong ABI/provider/schema or missing authority. |

Consume actual Lower operation/provider/preservation outputs, source-schema
validators and Legality's profile/check/wrapper facts as named in the clauses;
declared interfaces allow local implementation before whole-component release.
Record actual link/load inspection and execution, provider/compiler/profile/
target/version, binding/handle/callback identities, source/generated locations,
failure/cleanup and owning R1/R2/R4/R10 diagnostics. A successful manifest check
does not establish an actual call or every delegated service. The separately
preserved unresolved typed-host-delegation and foreign-symbol-resolution joins
remain unassessed; these four foundation clauses cannot discharge their full
source conjunctions by alias. All native/provider/callback holds and exact
class rules remain in force; no operation is executed by this reconciliation.

## Historical workflow-transfer plan

# Implementation plan: Runtime workflow migration

Spec: [spec.md](spec.md). Owner: backend workflow migration owner.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Contract alignment

Follow the [constitution](../../.specify/memory/constitution.md),
[development verification workflow](../../docs/development-verification.md) and
[canonical contracts](spec.md#purpose-and-canonical-contracts).
The workflow records work and evidence; it does not replace normative authority.

## Approach and exact files

Keep the four workflow artifacts and direct roadmap task link. The roadmap links
directly to the ledger; retain the entire original migration file as history.

| Canonical truth | Reference | Package use |
| --- | --- | --- |
| Finite outcome | [Release outcome](../../docs/component-roadmaps/runtime.md#release-outcome) | Declares component scope, not migration completion |
| Implementation tasks | [Runtime canonical remaining work](../../docs/component-roadmaps/runtime.md#remaining-tasks-and-evidence) | the five remaining-task rows and selected compiler-service scope; retain their original status and evidence |
| Component acceptance | [Acceptance criteria](../../docs/component-roadmaps/runtime.md#acceptance-and-integration) | Required semantic evidence stays here |
| Phase tasks | [Phase implementation roadmap](../../docs/phase-08-runtime-architecture/IMPLEMENTATION-ROADMAP.md) | Historical/current phase claims retain original scope |

Component role: runtime maintainer. Interface owners: [lower](../../docs/component-roadmaps/lower.md#release-outcome), [legality](../../docs/component-roadmaps/legality.md#release-outcome), [auth](../../docs/component-roadmaps/auth.md#release-outcome), [build](../../docs/component-roadmaps/build.md#release-outcome).
Integrated execution consumes Lower, Legality and Auth facts; service-table and ABI work can proceed on versioned fixtures. The selected compiler-services outcome does not require all distributed/AI/interactive runtime families. No new grants, effects or service execution are authorized by this documentation package.

## Validation and evidence

Check ASCII, the exact seven-file set, local Markdown target/anchor resolution,
semantic package names and absence of duplicated component checklists.
Negative acceptance cases are broken/missing links, treating artifact presence
as implementation completion, or narrowing retained contracts without disposition.
Run the existing structural documentation and full-language roadmap validators.
Run focused document/link and language-boundary checks.
No compiler/runtime execution suite is claimed. Record commands/results and
exact review identity in evidence.md.

## Delivery and remaining work

Publish partial artifact work in a draft PR. Keep package review and integration
unaccepted until their actual receipts exist. Deus owns final integration;
repository validation cannot confer semantic component or release acceptance.
Do not add a recurring acceptance loop solely because evidence text changes.

## Names, callers and compatibility

No source names, callers, wire/schema identities, load order or historical pins
change. Retain original task identities and phase anchors. Future source work
uses subsystem/behavior names under the semantic naming rule, separately reviewed.

## Additional contract interfaces

The [central contract mapping](../../.specify/contract-mapping.md) records
these scoped dependencies. They add navigation and evidence obligations, not
implementation tasks, accepted outcomes or new semantic authority. Existing
task IDs, statuses, acceptance text and historical receipts retain their scope.

- [L9](../../docs/phase-01-core-language/019-l9-error-handling-specification.md#requirements): Normalize host/FFI failures, cleanup and target error representations without leaking foreign exceptions into safe Gravity.
- [L11](../../docs/phase-01-core-language/021-l11-concurrency-model-specification.md#requirements): Implement the declared scheduler/task/channel/cancellation behavior with profile-specific service manifests and observable concurrency traces.
- [L19](../../docs/phase-01-core-language/029-l19-language-interoperability-and-migration-specification.md#requirements): Implement declared host/FFI ABI, ownership, callback, cleanup and error-translation boundaries; host behavior cannot define portable semantics.
- [TEST3](../../docs/phase-14-testing-verification-and-conformance/192-test3-runtime-test-strategy.md#requirements): Exercise each claimed runtime family with explicit artifact/profile/target/grants and accepted/rejected cleanup, replay, ABI and redaction evidence. Selected compiler services do not close all families.
- [L18](../../docs/phase-01-core-language/028-l18-alternative-memory-model-contract.md#requirements): Execute the declared memory-provider families and test allocation/failure, lifetimes, cleanup/leak behavior, ownership transfer and FFI/device boundaries. Provider registration and compile-time safety facts do not establish actual runtime conformance.
