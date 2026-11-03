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

## Compile-time evaluation dependency interface

Use the [compile-time evaluation producer and consumer order](../expand-workflow/plan.md#compile-time-evaluation-producer-and-consumer-order) and existing selected-result table. Only actually source-permitted, normally checked fallback or generated Q uses selected runtime services before later startup/execution/cleanup evidence. Early service/failure contracts precede compiler work; no hidden fallback or full-family start gate is introduced.

Task bodies/statuses and finite review subjects stay unchanged. Current source/interface entailment and independent acceptance remain pending; preserve all actual holds.

## Provider and capability dependency interface

Use the [provider and capability producer and consumer order](../auth-workflow/plan.md#provider-and-capability-producer-and-consumer-order) and existing selected-result table. Expose truthful support and actual selected primitives first, validate provider presence/version/scope/contracts before user code, then enforce actual actions and redacted audit. Gate installation precedes actions; their traces do not authorize themselves.

Task authorities and frozen review subjects stay unchanged. Selected interfaces remain proposed; current independent acceptance and implementation are unassessed. Preserve all actual restrictions.

## Provider and capability source interface

Use the [complete provider and capability source criteria](../auth-workflow/plan.md#complete-provider-and-capability-source-criteria). Use Runtime2 for actual pre-user-code provider initialization/fallback, Runtime5 for action/handle/revocation/observation checks, and Runtime4 for real workflow/AI consumers. A manifest or a grant table is not execution.

Historical finite reviews remain scoped; current whole-source/interface review and implementation acceptance remain pending. Preserve all actual holds and existing task authority.

## Concurrency dependency interface

Use the [concurrency producer and consumer order](../legality-workflow/plan.md#concurrency-producer-and-consumer-order) and existing selected-result table. Separate early support/cleanup/event/gate/record APIs, real selected primitive implementations and later task/message/cancel/replay/enforcement results. A no-runtime profile gains no scheduler, and a manifest is not execution.

Task bodies/statuses, source criteria and qualified reviews stay unchanged. Current source/dependency entailment and implementation acceptance remain pending; preserve every actual hold.

## Complete concurrency source interface

Use the [complete concurrency source criteria](../legality-workflow/plan.md#complete-concurrency-source-criteria). The five existing tasks select support, perform cleanup, execute tasks/synchronization/messages, perform durable/AI replay, and enforce actual action policy. Runtime records do not substitute for those behaviors. Historical finite reviews remain scoped; current source composition and implementation acceptance remain pending. Preserve every held operation.

## Source effect dependency interface

Use the [source effect and handler dependency order](../effects-workflow/plan.md#source-effect-and-handler-dependency-order) and the existing selected-result table. Separate early service/cleanup contracts, actual selected continuation/scheduler/record primitives and later complete error/async/generator/workflow/tool examples. Workflow/AI supply early definitions and later consume actual results; separately required review evidence remains external.

Task bodies/statuses and previous qualified evidence stay unchanged. Current source/dependency entailment and implementation acceptance remain pending; preserve all actual operation holds.

## Source effects and typed-handler interface

Use the [complete source effect and handler criteria](../effects-workflow/plan.md#complete-source-effect-and-handler-criteria) with the existing task authority. Use the existing cleanup task for typed resumable-error execution, memory/concurrency for async/generator execution and distributed/AI for workflow/tool interpretation. Consume checked handler/grant/capture/service products and produce actual values/cleanup/replay traces; no second interpreter authority is added.

The frozen finite review is qualified to its exact source predicates; complete L6/current source entailment, independent review and implementation acceptance remain pending. Preserve every actual held operation.

## Source memory-model and provider interface

Use the [complete source memory-model and provider criteria](../legality-workflow/plan.md#complete-source-memory-model-and-provider-criteria) with the existing task authority.

The existing memory/concurrency/typed-FFI task restores the reviewed release/transfer and device-boundary clauses and adds L18's actual service obligations. Preserve matching allocator identity, foreign lifetime/null/init/error/affinity/callback contracts, GC/RC rooting/finalization/pause/pinning behavior, device/unified visibility/synchronization and MMIO map/width/volatile/order/grants. Actual public allocation APIs remain with Stdlib; compiler facts and Auth grants remain separate inputs. Demonstrate real permitted service/release/transfer/check/cleanup behavior where claimed, retaining all actual execution holds and original unresolved source-recovery IDs. A manifest or assigned completion flag cannot close this task.

The new whole-source proposal preserves the eight finite review qualifications; current independent review, full cross-component entailment and actual implementation acceptance remain pending. No compiler/provider/native/device/callback/probe/replay/migration/review/signing/publication action runs in this planning increment.

## Source memory-model dependency order

Use the [source memory-model dependency order](../legality-workflow/plan.md#source-memory-model-dependency-order) and the existing selected-result table. Actual memory services consume legal source facts, provider/grant/enforcement decisions and compiled target inputs. Early device-map and service/panic/cleanup contracts precede execution; final device/API/family results follow. Do not require this service's own final handoff, conformance or public API result before executing it. Preserve actual allocation/release/transfer/failure evidence and held operations.

Current selected source/dependency review and implementation acceptance remain pending/unassessed. No actual provider, device, review or publication operation is performed here; all current restrictions and sole task authorities remain.

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

The FFI task owns the reusable foreign-boundary conversion and callback
adapters. The managed/native execution task consumes those adapters and owns
family startup, failure/cleanup and managed collection/state behavior; it does
not create a second implementation authority for the same boundary converter.

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

## Macro dependency interface

Use the [macro producer and consumer order](../expand-workflow/plan.md#macro-producer-and-consumer-order) and existing selected-result table. Supply actual selected adapters, session/invalidation/replay and authority products where macro-generated runtime or tool sessions are claimed; inspection records do not prove live frames or full family support.

Task bodies/statuses and frozen review subjects stay unchanged. Declared interfaces permit local construction; actual composition, acceptance and release retain their separate required results. Current independent source/interface acceptance is pending and every actual restriction remains.

## Complete Core semantic interface

Use the [complete Core source criteria](../core-workflow/plan.md#complete-core-source-criteria). Execute the actual admitted value/control/call/mutation/error/cleanup behavior under the selected runtime/profile, normalizing host crossings. Narrow mutation or exception slices cannot establish all thirteen primitive semantics or complete Core.

Task identities/statuses, prior source/review subjects and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance remain pending; no held execution or task transfer is authorized.

## Core dependency interface

Use the [Core producer and consumer order](../core-workflow/plan.md#core-producer-and-consumer-order) and existing selected-result table. Earlier error/cleanup/provider/session primitives permit construction; selected execution later consumes actual checked Core or actual emitted input for that path. Preserve source control, order, location, error and cleanup semantics without a second Core evaluator.

Existing task bodies/statuses, source criteria, frozen reviews and actual restrictions remain unchanged. Current selected-interface entailment and independent acceptance remain pending; no held operation is enabled.

## Complete Types source interface

Use the [complete Types source criteria](../types-workflow/plan.md#complete-types-source-criteria). Execute only the selected admitted typed program/boundary, preserving actual Option/Result/error/nullability/dispatch/value semantics. Earlier runtime/provider contracts permit construction; source type success grants no runtime authority.

Current task identities/statuses, historical review subjects and original C7 publication/other actual restrictions remain unchanged. This source proposal grants no implementation or current independent acceptance.

## Types dependency interface

Use the [Types producer and consumer order](../types-workflow/plan.md#types-producer-and-consumer-order) and existing selected-result table. Supply early selected conversion/FFI/resource/failure contracts before static checking, then implement actual admitted directional/nullability/encoding/loss/failure behavior under real grants/profile/ownership/safety. Preserve typed Option/Result/error distinctions and every held execution boundary.

Existing task bodies/statuses, source criteria, frozen review scopes and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance remain pending; no held operation is enabled.

## Complete pattern-matching source interface

Use the [complete pattern-matching source criteria](../core-workflow/plan.md#complete-pattern-matching-source-criteria). Actual selected execution evaluates the scrutinee once and guards once only after structural success/binding, preserves false continuation and non-returning transfer, and performs required validation/resource behavior under real admission. Early service contracts precede callers; no second Core evaluator or held execution is authorized.

Task IDs/statuses, historical review scopes and actual restrictions remain unchanged. Current complete source/interface entailment and independent acceptance remain pending; no held operation is enabled.

## Pattern-matching dependency interface

Use the [pattern-matching producer and consumer order](../core-workflow/plan.md#pattern-matching-producer-and-consumer-order) and existing selected-result table. Earlier actual validator/resource/error/session/recording contracts precede compiling their callers. Later selected Core or emitted-program execution proves once-only/false/non-returning guards, validation, ownership and replay behavior. Keep interpreter/compiled paths distinct and all held actions restricted.

Task bodies/statuses, source criteria, frozen review scopes and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance remain pending; no held operation is enabled.

## Complete protocol and dispatch source interface

Use the [complete protocol and dispatch source criteria](../types-workflow/plan.md#complete-protocol-and-dispatch-source-criteria). Select actual declared checked dispatch/reflection/host services and normalize actual foreign null/exception/resource behavior before safe core entry. Types boundary records do not execute conversion; no held native/provider action is enabled.

Task identities/statuses, historical review scopes and actual restrictions remain. Current full source/interface entailment and independent acceptance are pending; no held operation is enabled.

## Protocol and dispatch dependency interface

Use the [protocol and dispatch producer and consumer order](../types-workflow/plan.md#protocol-and-dispatch-producer-and-consumer-order) and existing selected-result table. Early actual host wrapper/normalization/ABI/service/error/session contracts precede Types and callers; later admitted selected Core or emitted programs produce real call/null/exception/resource/tool traces. All held actions remain restricted.

Task bodies/statuses, source criteria, frozen reviews and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance are pending; no held operation is enabled.

## Complete error-handling source interface

Use the [complete error-handling source criteria](../core-workflow/plan.md#complete-error-handling-source-criteria). Restore the five exact earlier panic/check/foreign/workflow/AI predicates to their same three Open Runtime tasks, totaling 6,269 bytes. Early real service/error/cleanup/wrapper contracts precede callers; later actual selected execution/normalization/durable replay products establish behavior, without renewing earlier reviews or enabling held actions.

Task identities/statuses, historical review scopes and actual restrictions remain. Current full source/interface entailment and independent acceptance are pending; no held operation is enabled.

## Error-handling dependency interface

Use the [error-handling producer and consumer order](../core-workflow/plan.md#error-handling-producer-and-consumer-order) and existing selected-result table. Early real error/panic/check/cleanup/wrapper/normalization/durable-AI recording contracts precede callers. Later selected checked-Core interpreter or emitted compiled programs produce actual typed failure, trap, conversion, cleanup, failure/compensation replay and policy/audit traces, with no universal native artifact or held execution.

Task bodies/statuses, source criteria, frozen reviews and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance are pending; no held operation is enabled.

## Library dependency interface

Use the [library producer and consumer order](../stdlib-workflow/plan.md#library-producer-and-consumer-order) and existing selected-result table. Early source/provider/error/cleanup/family/schema contracts precede callers. Actual selected checked-Core or emitted input precedes real API values, normalization, resource cleanup and required recorded replay; an interpreter needs no universal native artifact.

Task bodies/statuses, source criteria, frozen reviews and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance are pending; no held operation is enabled.

## Complete facet source interface

Use the [complete facet source criteria](../expand-workflow/plan.md#complete-facet-source-criteria). Retain actual selected service, provider, FFI ownership/error/ABI and execution boundaries for facets that require them. A portable Core-only facet needs no invented host runtime or native binary. Agent/model/tool/IO/FFI manifests do not execute services, grant authority or accept a live provider.

Current source/interface entailment and independent acceptance remain pending. Preserve existing task identities/statuses, historical review scopes and every actual restriction. No held execution is enabled.


## Facet dependency interface

Use the [facet producer and consumer order](../expand-workflow/plan.md#facet-producer-and-consumer-order) and existing selected-result table. Early real FFI/ABI/ownership/error/provider contracts precede wrapper checking. Actual selected admitted callers and needed layouts precede later normalization/transfer/cleanup. Generation-only facets and agent artifacts authorize no live calls or universal native runtime.

Task bodies/statuses, source criteria, historical reviews and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance remain pending. No held action is enabled.


## Complete alternative type system source interface

Use the [complete alternative type system source criteria](../types-workflow/plan.md#complete-alternative-type-system-source-criteria). Actually execute selected permitted inserted gradual/cast checks with expected type, declared failure, blame and source/provider identity. An unchecked cast stays unsafe; recorded runtime-checked does not execute a check. Source-only analysis needs no unrelated live runtime/provider or native artifact.

Current source/interface entailment and independent acceptance remain pending. Preserve existing task identities/statuses, historical review scopes and every actual restriction. No held execution is enabled.


## Alternative type system dependency interface

Use the [alternative type system producer and consumer order](../types-workflow/plan.md#alternative-type-system-producer-and-consumer-order) and sole selected-result table. Complete L17 selected runtime checking uses early real check/expected-type/failure/blame/profile/representation/service/ABI contracts, then actual admitted check records and needed layouts before executing against actual values. Preserve accepted Result/User or DecodeError, blame, resource/panic and unsafe boundaries. Later runtime execution is not the first typed check schema; source-only typing needs no universal runtime, external provider or native artifact. Complete L17 foreign/resource consumption uses early actual lifetime/pinning/handle/ABI/layout interfaces, then authentic sufficient exported facts and admitted selected calls before real transfer/cleanup. Later executions cannot supply the first ownership or layout contract. No alternative type fact grants authority or permits a hidden unsafe/nullability boundary; unrelated pure checker work needs no live foreign service.

Task bodies/statuses, all source criteria, historical reviews and forty future source bindings remain unchanged. Current source/interface entailment, independent acceptance and integration remain pending. No held action is enabled.


## Complete interoperability and migration source criteria

Read complete [L19](../../docs/phase-01-core-language/029-l19-language-interoperability-and-migration-specification.md)
and the selected L3/L5/L6/L9/L10/L12/L13/L15/L18, safety/profile/ABI/runtime/schema/
package and compatibility contracts. Existing [task authorities](tasks.md#implementation-tasks)
keep all editable outcomes. The criterion keys below join source requirements
within those tasks, not new tasks or completion credit. Restore two published
Expand predicates exactly; retain the three current directional/error predicates.
Current source/interface entailment, implementation acceptance and independent
review remain unassessed or pending. All execution restrictions remain.

| Criterion | Existing task | Source requirement |
| --- | --- | --- |
| `declarations` | `types/current-evidence-and-remaining-work/types2-check-declared-abstractions` | L19 complete foreign declaration and boundary-schema criterion (Open) |
| `provider` | `auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection` | L19 actual foreign provider selection criterion (Open) |
| `admission` | `auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime` | L19 complete foreign-boundary admission criterion (Open) |
| `bindings` | `resolve/current-evidence-and-remaining-work/resolve2-validate-module-boundaries` | L19 actual foreign import and symbol boundary criterion (Open) |
| `mappings` | `types/current-evidence-and-remaining-work/types3-make-dynamic-boundaries-explicit` | L19 complete directional type-mapping criterion (Open) |
| `facts` | `types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts` | L19 complete interop artifact and fact-export criterion (Open) |
| `verify` | `types/current-evidence-and-remaining-work/types5-integrate-typed-core` | L19 actual common typed-boundary verification criterion (Open) |
| `effects` | `effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering` | L19 actual foreign and generated-binding effect admission criterion (Open) |
| `generate` | `expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs` | L19 complete schema-binding and scoped-shim generation criterion (Open) |
| `compile-time` | `expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects` | L19 actual schema-import and binding-generation action criterion (Open) |
| `wrappers` | `legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries` | L19 complete safe foreign-wrapper criterion (Open) |
| `ownership` | `legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis` | L19 complete foreign ownership and pointer-fact criterion (Open) |
| `profiles` | `legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks` | L19 complete interop profile-legality criterion (Open) |
| `handoff` | `legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution` | L19 actual checked interop obligation handoff criterion (Open) |
| `abi` | `lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout` | L19 actual ABI and wire-representation lowering criterion (Open) |
| `execute` | `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries` | L19 actual foreign conversion, call and cleanup criterion (Open) |
| `hosts` | `runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup` | L19 complete hosted bridge behavior criterion (Open) |
| `replay` | `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution` | L19 actual process/network/service and migration execution criterion (Open) |
| `locks` | `build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution` | L19 actual boundary identity and compatibility-lock criterion (Open) |
| `cache` | `build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation` | L19 actual interop regeneration and drift-cache criterion (Open) |
| `reproducibility` | `build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix` | L19 actual versioned binding and artifact reproducibility criterion (Open) |
| `diagnostics` | `diag/remaining-tasks-and-evidence/reconcile-stage-carriers-with-canonical-record-schema` | L19 complete interop diagnostic carrier criterion (Open) |
| `origins` | `diag/remaining-tasks-and-evidence/preserve-primaryrelatedgenerated-origins-and-stable-order` | L19 actual foreign/schema/generated source mapping criterion (Open) |
| `library` | `stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis` | L19 actual standard foreign resource and safe-wrapper API criterion (Open) |
| `adapters` | `stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters` | L19 actual schema and service adapter API criterion (Open) |
| `parity` | `expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs` | L19 complete incumbent compatibility and migration evidence criterion (Open) |
| `finite-emit-scoped-migration-shim` | `expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs` | Restore exact earlier finite predicate; historical scope unchanged. |
| `finite-generate-schema-binding` | `expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs` | Restore exact earlier finite predicate; historical scope unchanged. |
| `mapping-existing` | `types/current-evidence-and-remaining-work/types3-make-dynamic-boundaries-explicit` | Preserve the complete existing criterion bytes and owner. |
| `failure-existing` | `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries` | Preserve the complete existing criterion bytes and owner. |
| `error-contract-existing` | `types/current-evidence-and-remaining-work/types3-make-dynamic-boundaries-explicit` | Preserve the complete existing criterion bytes and owner. |


The preserved census contains 114 facets, 78 occurrences and six original owner
obligations. Read complete section preambles, lists, examples and following
conditions rather than treat names/links as coverage. Source custody and selected
historical witnesses do not confer aggregate adoption or whole-source acceptance.
The source proposal keeps original Auth foreign admission, Types conversion,
Expand schema/shim generation, Runtime failure normalization and Legality wrapper
roles. Related producers supply their real results; none inherits another's
inference, grant, proof, action or independent acceptance responsibility.

### Ten families and explicit complete declarations

| Boundary family | Required declared interface and actual evidence |
| --- | --- |
| Native ABI | C/C++/Rust/Swift/Zig/platform library ABI, explicit types/ownership/error/thread/layout/safety and selected provider support |
| Managed host | JVM/CLR/BEAM/Python/Ruby/similar runtime version, object/root/lifetime/thread/dynamic/error and profile contracts |
| JavaScript/TypeScript | Browser/Node/edge/UI host object/null/promise/schema and declared runtime/profile contracts |
| WebAssembly | Module/import/export/memory/component types, representation, ownership/error and selected support |
| Schema | GraphQL/OpenAPI/SQL/Protobuf/Avro/JSON Schema/binary source, codecs/validators, nullability, provenance and drift |
| Process | Actual CLI/subprocess protocol, arguments/status/signal/error/ownership/thread/blocking and authority |
| Network | Actual service/RPC/queue/stream/workflow protocol/schema/error/replay/audit and authority |
| Hardware | MMIO/driver/register/interrupt/DMA/device-tree memory/order/ownership/profile contracts without adopting a device |
| AI | Actual model/tool/prompt/embedding/memory/evaluation typed effect/authority/privacy/provider contracts |
| Package | Incumbent package/build identities, versions, compatibility, source provenance and permitted actions |

Every boundary has a typed declaration and actual provider, and is usable only
in supported profiles. Keep all thirteen declaration fields: foreign language/
protocol; link/symbol/method/endpoint/schema/import identity; ABI/wire protocol;
type mapping; effects; capabilities; ownership; null/init; error/exception;
thread/blocking; safety; profiles/targets; version/compatibility. The requirements
also retain memory behavior, authentic source and actual provider identity.
Incomplete declarations in safe namespaces reject. Resolving a symbol/import or
loading a schema is not invoking a provider, issuing a grant or proving safety.
The strlen and db/query examples illustrate contracts rather than install a
library or supply capabilities. Foreign support never changes the repository's
Clojure-only temporary bootstrap/tooling and Gravity/Uniform destination boundary.

### Directional values, wrappers, ownership and real failures

Six mapping families retain their direction and declared representation: numeric;
string; Option; Result; collection; linear resource. String may become an owned
UTF-8 buffer, borrowed slice or distinct JVM/JS/Python host string. Option may
be nullable/tagged/optional; Result may use an error code plus out parameter,
exception, rejected promise, tagged response or schema union. Collections may
be owned arrays, slices, host lists/arrays, repeated fields or typed buffers;
linear handles name their release. Loss needs an explicit conversion; fallible
mapping returns Result or the declared L9 boundary error. Declared Option
absence stays legal and actual errors/grant denials cannot become absence.
Types owns actual mapping/type/dataflow facts; Runtime owns actual conversion,
normalization, calls and cleanup. Default bidirectionality or assigned round-trip
status is not their evidence.

Retain seven ownership modes: Gravity-owned/foreign-borrowed; foreign-owned/
Gravity-borrowed; transfers in both directions; shared immutable; synchronized/
atomic shared mutable; named foreign-release handles. Foreign pointers carry
nullability, initialization, lifetime, allocator identity, aliasing and thread
affinity; they are not ordinary safe references. Unrepresentable facts keep the
boundary unsafe. Actual release/transfer, borrow intervals, failure cleanup,
concurrency/blocking and thread rules require genuine owner results, not host
GC, a function name or process exit. L19 does not waive the wider L10/L18 rules.

Unsafe foreign calls become safe only through actual wrapper invariants proved
or checked. Preserve visible unsafe operation/reason/source/profile/effect/grant,
pre/postconditions, no-retain/ownership/layout assumptions, safe boundary, owner,
audit/evidence and re-review trigger. The safe-strlen example needs actual
non-null/null-termination/within-buffer/no-retention obligations, not copied
INTEROP-STRLEN or passed-audit labels. Target-specific layouts remain explicit
and changed ABI/CString layout invalidates affected evidence. Safe Gravity keeps
its governing safety outcomes; explicit unsafe is no safe proof.

Foreign exceptions, panics, error codes/errno, rejected promises, signals and
process exit statuses translate into declared typed Gravity errors before safe
entry. Unexpected or undeclared null cannot enter a non-null type; declared
absence is separate. Preserve eight L9 FFI error fields and actual failure
resource behavior. An untranslated foreign failure remains unsafe. Host runtime
conveniences cannot silently become source semantics.

### Real schema generation, host bridges and scoped migration

Schema binding generation retains nine fields: source digest/version; generated
types/codecs; field nullability; error/status map; runtime effects/capabilities;
changed-schema compatibility; diagnostic provenance. Schema loading/generation
is a real L12 compile-time effect with authentic source/read/build grants.
Available checked P precedes invocation and generated Q receives ordinary
checks. The example schema/read label creates no grant. Generated bindings retain
foreign/schema provenance and never masquerade as handwritten source. Build
versions/locks artifacts and compares actual repeated generation under its
reproducibility contract; flags establish neither equality nor drift checks.

Hosted bridges retain runtime version, reflection/dynamic use, object lifetime/
rooting, threading, exception translation, performance/allocation and non-hosted
missing-feature behavior. Their measured boundary artifacts permit incremental
replacement; they do not make the host runtime portable Gravity. Actual roots,
null/error translation, supported runtime and deterministic resource behavior
require genuine service results. A fixed JVM/JS/Wasm adapter map implements none
of those operations on its own.

Seven illustrative shims remain distinct: C-header extern/initial wrappers;
selected Java classes; TypeScript schemas/codecs; Python-model provider tools;
SQL query types/migration checks; GraphQL operations/response validators; Rust
ABI-safe native exports. Each names incumbent source, target namespace,
preserved/emulated/narrowed/rejected behavior, effects/grants, safety, generated
files, parity tests, owner and stability. Shims remain temporary unless promoted.
Original Expand generation and normal checking remain the owner; existing
conditional Evolution change/stabilization decisions govern actual stable
changes or promotion without a per-run approval or fresh migration-proof gate.
No host language becomes a new seed/tooling implementation language.

Compatibility requires actual golden comparison, directional type round trips,
error translation, ownership transfer, release, concurrency/blocking, performance
envelopes where replacement is sensitive and unsupported-profile rejection.
At least one real incumbent-language/schema parity report remains necessary;
all eight evidence families bind actual provider/schema versions, target manifests
and locks. Actual owner cases and oracles precede applicable independently
executed Release language/compiler/profile/runtime/safety/library results.
Projected reports, one equal example and a status collector do not prove parity,
complete interoperability or unrestricted safety. Held/unavailable execution
remains explicit work and does not disappear from coverage.

### Profile, artifacts, diagnostics and current implementation scope

Core has no foreign boundaries except schema data lowering to portable typed
Core. Hosted/native retain their declared distinct services; kernel/firmware
require modeled allocation/blocking/interrupt/MMIO. Hardware uses descriptions/
maps/buses/external modules. Distributed/AI retain actual service/tool/model/
schema/network replay/audit. These are eight named profiles; workflow is a
distributed facet/target/artifact, not another profile. Reject host leakage and
unsupported representations before backend/runtime-only failure. Native/host/
Wasm/JS/backend claims keep actual target evidence and current restrictions.

All ten artifact families keep real producing owners and exact source/provider/
schema/runtime/ABI/profile/target/lock identities. Eleven L19 diagnostics retain
ten actual context fields: boundary id, foreign source, profile, span/manifest,
provider, type map, ownership, effects, capabilities and safe-wrapper/migration
remediation. Generated origins, redaction and source maps remain real artifacts.
Changed source/schema/ABI/layout/runtime inputs invalidate affected binding,
wrapper/fact/proof/error/origin/parity products before actual regeneration/reuse.

Static inspection covers only complete selected record-interop-call!,
interop-conformance-fixture and host-interop-adapter-manifest functions. The
first projects arguments/defaults and assigns complete/passed/reproducible/
translation/enforcement flags; the second covers nineteen tokens using record/
status presence; the third emits four fixed adapter maps and complete status.
These functions alone execute no actual foreign conversion/call/wrapper proof,
schema generation/drift comparison or incumbent parity. Other implementations
remain unassessed; no whole absence or whole implementation is inferred and
none of these helpers is executed by this proposal.

Complete L19 selected-result staging is next in the existing dependency table.
All existing routes/joins, task identities/statuses, future declarations/source
bindings and historical reviews remain. Current whole-source/owner/task/interface
entailment and independent acceptance remain pending. Preserve the unresolved
D1/C1/C5/C6 order and L6/L12 hermetic wording, accounting removal, real semantic/
host/platform/external-account restrictions and every held action. No compiler,
provider/plugin/C7/C12/native/callback/device/probe/replay/migration/private-store/
signing or product-publication action, reviewer wake, other-chat message,
aggregate publication or merge is enabled. Reader contract coverage remains the
first actual implementation increment.

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
