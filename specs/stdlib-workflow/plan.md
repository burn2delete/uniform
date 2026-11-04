# Standard library implementation planning entry

Select one unresolved [existing task](tasks.md#implementation-tasks) and actual
export family, preserving compiler-support scope and the historical plan below.
Read [spec.md](spec.md), complete applicable L13/STD/TEST7 rules and the actual
language/domain/runtime/provider contract. Resolve current semantic source and
temporary adapters using [development verification](../../docs/development-verification.md)
and the [stage ownership table](../../docs/bootstrap/stage-owned-namespaces.md).

Name the actual compiled namespace/export/binding, input/result types and value
laws, evaluation/transfers, effect/allocation/storage, profile/target/grants,
provider and real caller. Plan legal examples plus arity/type/value/profile/
authority/ownership/taint/schema or proof negatives specific to the API. For
collections preserve full physical validation and precise empty/false/nil,
ordering and lifetime rules; do not copy superseded quotas or infer unsupported
shape/presence facts. Compatibility spellings alone are not module-export proof.

Run focused API/property checks and affected compiler/runtime/domain/provider
composition, with runnable documentation and exact diagnostic/artifact identity.
Use the existing task authority only, recording absent exports, failed/unrun
cases, residual host/provider boundaries and actual manifest/stability changes.
Preserve reserved writers and held live operations. Separate reachable compiler
support, wider module/profile completion, independent acceptance and release.

## Compile-time evaluation dependency interface

Use the [compile-time evaluation producer and consumer order](../expand-workflow/plan.md#compile-time-evaluation-producer-and-consumer-order) and existing selected-result table. Actual available callable definitions and their pure value/overflow/allocation contracts precede eligible use; later compiled API/source results precede independent library harness execution. Exports alone are not callable implementation.

Task bodies/statuses and finite review subjects stay unchanged. Current source/interface entailment and independent acceptance remain pending; preserve all actual holds.

## Complete compile-time evaluation interface

Use the [complete compile-time evaluation source criteria](../expand-workflow/plan.md#complete-compile-time-evaluation-source-criteria). Only actually available, eligible compile-time-callable pure functions can supply the claimed constant example; real value/overflow/representation and phase facts remain required. An export declaration or host convenience is not the implementation.

Current source/interface entailment and implementation acceptance remain pending; preserve existing task authority, frozen reviews and every actual restriction.

## Provider and capability dependency interface

Use the [provider and capability producer and consumer order](../auth-workflow/plan.md#provider-and-capability-producer-and-consumer-order) and existing selected-result table. Actual typed capability-value/declared-ambient contracts precede compiler use; usable APIs and safe memory or IO replacement consume real implemented services, grants/gates and selected startup. Owner cases precede independent library suites.

Task authorities and frozen review subjects stay unchanged. Selected interfaces remain proposed; current independent acceptance and implementation are unassessed. Preserve all actual restrictions.

## Provider and capability source interface

Use the [complete provider and capability source criteria](../auth-workflow/plan.md#complete-provider-and-capability-source-criteria). Real capability-valued or fully declared permitted ambient APIs consume checked provider/grant/startup/action products. Pure value transport does not erase later effects or prove live provider support.

Historical finite reviews remain scoped; current whole-source/interface review and implementation acceptance remain pending. Preserve all actual holds and existing task authority.

## Concurrency dependency interface

Use the [concurrency producer and consumer order](../legality-workflow/plan.md#concurrency-producer-and-consumer-order) and existing selected-result table. Early typed task/future/channel/actor/atomic definitions precede Core use; real selected Runtime primitives precede compiled API results. Final Release execution is later and does not construct or accept the API.

Task bodies/statuses, source criteria and qualified reviews stay unchanged. Current source/dependency entailment and implementation acceptance remain pending; preserve every actual hold.

## Complete concurrency source interface

Use the [complete concurrency source criteria](../legality-workflow/plan.md#complete-concurrency-source-criteria). The existing resource/concurrency task implements actual typed APIs over Runtime services, preserving atomic defaults, capture/message/guard rules and physical backpressure semantics. Historical finite reviews remain scoped; current source composition and implementation acceptance remain pending. Preserve every held operation.

## Source effect dependency interface

Use the [source effect and handler dependency order](../effects-workflow/plan.md#source-effect-and-handler-dependency-order) and the existing selected-result table. The real typed fixture API and selected Runtime primitive precede the Release harness that calls them. A local fake needs only its actual selected services, retains the target profile and explicit fixture grants, and cannot prove live behavior.

Task bodies/statuses and previous qualified evidence stay unchanged. Current source/dependency entailment and implementation acceptance remain pending; preserve all actual operation holds.

## Source effects and typed-handler interface

Use the [complete source effect and handler criteria](../effects-workflow/plan.md#complete-source-effect-and-handler-criteria) with the existing task authority. The existing pure-core/testing API task owns actual typed fixture/fake/mock/golden/replay interpretation, setup/body/teardown and expected diagnostics under the active target profile and explicit harness grants. Release consumes actual API results; a fake cannot hide live authority or profile violations.

The frozen finite review is qualified to its exact source predicates; complete L6/current source entailment, independent review and implementation acceptance remain pending. Preserve every actual held operation.

## Source memory-model and provider interface

Use the [complete source memory-model and provider criteria](../legality-workflow/plan.md#complete-source-memory-model-and-provider-criteria) with the existing task authority.

The existing resource/concurrency/IO/network task restores the reviewed public allocation criterion and adds complete L18 API/provider-boundary requirements. Execute a real selected allocation/use path with initialized storage or a usable handle and declared layout/alignment/failure/sharing/profile/authority facts; reject missing or incompatible providers and invalid accesses. Runtime owns its actual service and matching release/transfer, while compiler checkers derive safety. Declared-interface fixtures can start independently; no mock, compatibility spelling or record-only implementation establishes a live provider or all seventeen family implementations.

The new whole-source proposal preserves the eight finite review qualifications; current independent review, full cross-component entailment and actual implementation acceptance remain pending. No compiler/provider/native/device/callback/probe/replay/migration/review/signing/publication action runs in this planning increment.

## Source memory-model dependency order

Use the [source memory-model dependency order](../legality-workflow/plan.md#source-memory-model-dependency-order) and the existing selected-result table. Publish the selected versioned resource source/macro/API contract before compiling its consumer examples. Actual public allocation/API acceptance follows the ordinary compiler and real Runtime provider/grant/layout results. Early source definitions and later usable values/handles are separate products; neither whole IO/network completion nor final family conformance gates ordinary interface work.

Current selected source/dependency review and implementation acceptance remain pending/unassessed. No actual provider, device, review or publication operation is performed here; all current restrictions and sole task authorities remain.

## Complete macro source interface

Use the [complete macro source criteria](../expand-workflow/plan.md#complete-macro-source-criteria). Supply actual available eligible macro helper/library definitions and claimed semantic behavior; macro registration, return types and conformance flags do not prove callable execution.

Existing task identities/statuses, historical reviews, actual restrictions and ordinary construction versus composition/acceptance boundaries remain. These are source-backed current criteria proposals; full source/interface entailment and independent current acceptance are pending.

## Macro dependency interface

Use the [macro producer and consumer order](../expand-workflow/plan.md#macro-producer-and-consumer-order) and existing selected-result table. Supply real available eligible syntax/data/helper definitions before macro use; actual generated APIs and claimed runtime behavior are checked and exercised later by their owners and independent harnesses.

Task bodies/statuses and frozen review subjects stay unchanged. Declared interfaces permit local construction; actual composition, acceptance and release retain their separate required results. Current independent source/interface acceptance is pending and every actual restriction remains.

## Complete Core semantic interface

Use the [complete Core source criteria](../core-workflow/plan.md#complete-core-source-criteria). Supply early actual library/macro definitions for structured lowering and admitted value/dispatch/resource APIs. Runtime execution and Legality flow checking remain later distinct owners; full library release is no initial Core construction gate.

Task identities/statuses, prior source/review subjects and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance remain pending; no held execution or task transfer is authorized.

## Core dependency interface

Use the [Core producer and consumer order](../core-workflow/plan.md#core-producer-and-consumer-order) and existing selected-result table. Provide actual early versioned constructor/protocol/macro/resource declarations, then demonstrate real callable value and cleanup behavior using checked callers and selected Runtime products. Library execution cannot be its own early declaration prerequisite.

Existing task bodies/statuses, source criteria, frozen reviews and actual restrictions remain unchanged. Current selected-interface entailment and independent acceptance remain pending; no held operation is enabled.

## Complete Types source interface

Use the [complete Types source criteria](../types-workflow/plan.md#complete-types-source-criteria). Keep collection operations and current STD3 quota removal distinct from Types inference. Supply actual declared value contracts and consume independently established compatible types; expected results or an export table prove no inference.

Current task identities/statuses, historical review subjects and original C7 publication/other actual restrictions remain unchanged. This source proposal grants no implementation or current independent acceptance.

## Types dependency interface

Use the [Types producer and consumer order](../types-workflow/plan.md#types-producer-and-consumer-order) and existing selected-result table. Supply real early public function/type/protocol/collection/resource declarations, then exercise actual implementations and checked callers. Preserve actual argument/body compatibility, full stored-value validation, independent branch/loop/private-census premises and typed foreign/resource failure/cleanup obligations.

Existing task bodies/statuses, source criteria, frozen review scopes and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance remain pending; no held operation is enabled.

## Complete pattern-matching source interface

Use the [complete pattern-matching source criteria](../core-workflow/plan.md#complete-pattern-matching-source-criteria). Real Result/Option/record/enum/collection/resource declarations and callable implementations retain pattern binding/narrowing, validation and linear cleanup contracts. Source declarations precede analysis; later callable behavior and consumer fixtures require actual implementations, not a type-shaped table.

Task IDs/statuses, historical review scopes and actual restrictions remain unchanged. Current complete source/interface entailment and independent acceptance remain pending; no held operation is enabled.

## Pattern-matching dependency interface

Use the [pattern-matching producer and consumer order](../core-workflow/plan.md#pattern-matching-producer-and-consumer-order) and existing selected-result table. Provide early real Result/Option/constructor/collection/resource declarations, then demonstrate selected callable pattern consumers and consumption/cleanup with checked callers. Unsupported required families remain work; declarations and type labels are not executed behavior.

Task bodies/statuses, source criteria, frozen review scopes and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance remain pending; no held operation is enabled.

## Complete protocol and dispatch source interface

Use the [complete protocol and dispatch source criteria](../types-workflow/plan.md#complete-protocol-and-dispatch-source-criteria). Provide actual protocol/interface definitions and concrete implementations whose receiver/signature/effect/capability/default/stability contracts survive dispatch. Real resource/host implementations preserve boundary cleanup and actual callable behavior.

Task identities/statuses, historical review scopes and actual restrictions remain. Current full source/interface entailment and independent acceptance are pending; no held operation is enabled.

## Protocol and dispatch dependency interface

Use the [protocol and dispatch producer and consumer order](../types-workflow/plan.md#protocol-and-dispatch-producer-and-consumer-order) and existing selected-result table. Early actual method/default/stability/resource/host declarations precede checked implementations and real callable behavior, normalization and cleanup. Declarations and sample protocol records do not demonstrate implementation.

Task bodies/statuses, source criteria, frozen reviews and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance are pending; no held operation is enabled.

## Complete error-handling source interface

Use the [complete error-handling source criteria](../core-workflow/plan.md#complete-error-handling-source-criteria). Implement actual callable Some/None/Ok/Err and recoverable APIs with real Result/Option exhaustiveness, preserving declared reasons for profile-specific throw. Panic cannot become ordinary stable-API control flow; declarations and data shapes are not behavior.

Task identities/statuses, historical review scopes and actual restrictions remain. Current full source/interface entailment and independent acceptance are pending; no held operation is enabled.

## Error-handling dependency interface

Use the [error-handling producer and consumer order](../core-workflow/plan.md#error-handling-producer-and-consumer-order) and existing selected-result table. Actual early Some/None/Ok/Err and recoverable API/resource/failure declarations precede real callable behavior, exhaustiveness and cleanup. Preserve actual profile-specific throw rationale without a universal ban; panic cannot be ordinary stable-API control flow.

Task bodies/statuses, source criteria, frozen reviews and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance are pending; no held operation is enabled.

## Complete library-principles source criteria

The complete [L13](../../docs/phase-01-core-language/023-l13-standard-library-design-principles.md) governs these existing tasks. The earlier ten exact profile74 library predicates are restored (5,758 bytes) under their original Library roles and declared dependencies. Current whole-source criteria add the omitted public-contract, classification, byte/resource/compiler API, profile/authority/safety, diagnostic, preservation and composition conditions. The unchanged existing complete macro generated-program predicate retains Expand's normal revalidation role. Original task identities/statuses, narrow review subjects and actual restrictions survive; none of those reviews is renewed or expanded into whole L13, STD1-STD20 or current implementation acceptance.

| Criterion | Existing task | Required source outcome |
| --- | --- | --- |
| `contracts` | `stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations` | L13 complete public library-contract criterion (Open) |
| `classification` | `stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations` | L13 API-classification criterion (Open) |
| `data` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | L13 complete portable-data API criterion (Open) |
| `numeric` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | L13 complete public numeric-mode API criterion (Open) |
| `resources` | `stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis` | L13 complete resource and concurrency API criterion (Open) |
| `meta` | `stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters` | L13 complete compiler/domain library API criterion (Open) |
| `release` | `stdlib/remaining-tasks-and-evidence/integrate-platform-modules-and-close-release-manifests` | L13 library compatibility and release-assembly criterion (Open) |
| `import` | `resolve/current-evidence-and-remaining-work/resolve2-validate-module-boundaries` | L13 real library import-boundary criterion (Open) |
| `typed` | `types/current-evidence-and-remaining-work/types2-check-declared-abstractions` | L13 actual public library type-contract criterion (Open) |
| `effects` | `effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering` | L13 actual public library effect-admission criterion (Open) |
| `grants` | `auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime` | L13 actual library authority criterion (Open) |
| `wrappers` | `legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries` | L13 real safe-wrapper boundary criterion (Open) |
| `preservation` | `opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors` | L13 library numeric and safe-API preservation criterion (Open) |
| `diagnostics` | `diag/remaining-tasks-and-evidence/reconcile-stage-carriers-with-canonical-record-schema` | L13 complete library diagnostic-carrier criterion (Open) |
| `composition` | `stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations` | L13 complete library artifact and conformance composition criterion (Open) |
| `finite-access-ordered-vector` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | Restore exact finite predicate; retain original review scope. |
| `finite-access-persistent-list` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | Restore exact finite predicate; retain original review scope. |
| `finite-compile-contract-examples` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | Restore exact finite predicate; retain original review scope. |
| `finite-enforce-portability-layers` | `stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations` | Restore exact finite predicate; retain original review scope. |
| `finite-execute-declared-numeric-mode` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | Restore exact finite predicate; retain original review scope. |
| `finite-maintain-set-uniqueness` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | Restore exact finite predicate; retain original review scope. |
| `finite-publish-public-contract` | `stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations` | Restore exact finite predicate; retain original review scope. |
| `finite-record-compatibility-event` | `stdlib/remaining-tasks-and-evidence/integrate-platform-modules-and-close-release-manifests` | Restore exact finite predicate; retain original review scope. |
| `finite-traverse-map-associations` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | Restore exact finite predicate; retain original review scope. |
| `finite-validate-and-slice-string` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | Restore exact finite predicate; retain original review scope. |
| `generated-existing` | `expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs` | Unchanged complete generated-source checking predicate. |


Read each complete L13 preamble, list, example and following condition. The source occurrence/facet census locates the full conjunction; a document link, owner match or finite earlier predicate alone cannot establish it. Supporting STD1, TEST7, GOV3, C17 and selected L6/L15 interfaces retain their own larger source obligations.

The library keeps five portability layers: compiler core (0), portable data (1), memory/resources/concurrency (2), capability-backed host/platform (3) and domain/compiler libraries (4). Bind the complete source namespace inventory and actual dependency edges; reject a lower-to-higher edge and report exactly the shipped subset in each profile manifest. A compiler-support closure may complete earlier than wider application modules. Neither the full library nor final Build/release is the first interface prerequisite for a callable pure function or a declared namespace.

Every public export carries real type, effect/capability, allocation/resource, failure/panic, blocking/scheduling/nondeterminism/replay, profile/target and stability facts. Preserve all seven API classifications in metadata/docs/lints/checks/fixtures; labels grant no authority or behavior. Library owns declarations and actual APIs. Resolve validates real imports/layer graphs, Types derives compatibility, Effects actual call admission, Auth authentic scoped grants, Legality safety/wrappers, and Opt preservation. The inherited Auth references for generated checking and effect admission remain historical context with the actual Expand/Effects witnesses; the inherited Opt numeric owner supplies preservation while Library supplies callable numeric behavior.

IR inspection declares the L6 `:compiler/read-ir` effect and C17's scoped `:compiler/ir-transform` capability. A `:read-mir` scope grants no `:write-mir`; artifact kind, namespace, pass phase and package policy still apply. All emitted forms undergo ordinary handwritten-source checks with complete source span/hygiene/phase/origin/type/effect/profile/target/artifact identity. Named intrinsic hooks are auditable and specified, not privileged.

Retain precise List/Vector/Map/Set/byte/String contracts, physical fixed-capacity/region-backed alternatives, numeric modes and required proofs, ten memory/resource distinctions and source concurrency/scheduler behavior. Physical capacity, queue, memory layout, index/arity and target representability rules survive current project accounting removal; no work/input allowance or failure-attempt quota returns. Actual selected Runtime/provider values and cleanup establish service support; fakes or host builtins do not accept every API or profile.

Safe wrappers isolate unsafe namespaces and own actual invariants, tests/proofs/checks, permitted profiles, cleanup and release/safety-critical audits under D8's four outcomes. Preserve failure/check/diagnostic/audit behavior through selected transformations, with required exact-subject evidence and changed-owner regeneration. Valid raw MIR remains usable; no universal optimizer or future full Formal gate is added.

All six L13 artifact families and nine diagnostics retain actual producing owners and required namespace/symbol/profile/target/contract/caller-effects/capability/remediation fields. Examples compile with real imports under every claimed major profile; missing imports, hidden globals, untracked authority, profile mismatch or false expected diagnostics fail. Library owns actual examples and API fixtures; the existing independent TEST7/T7/Release harness consumes them later. Unavailable or held required cases remain Open. Compatibility events include profile removal and obey existing Governance/STD20/GOV2/GOV8 policy; Library assembles the affected records rather than granting stabilization or release approval.

Early real namespace/export/schema/provider/profile/ABI/checker contracts support construction; actual selected compiler/service/domain results support callable behavior and later conformance. The sole dependency graph still needs the complete L13 selected-result interface staging; this source proposal does not silently amend its 2,199 joins, 141 routes, 101 current parents, forty future records or coarse cycles. Current source/owner/interface entailment, independent acceptance and integration remain pending. Reader contract coverage remains the first actual implementation increment. Preserve all writer reservations, original C7/C12 and paired/provider/plugin/native/callback/device/probe/replay/migration/private-store/signing/publication restrictions and stopped handoffs. No held operation, other-chat message, reviewer wake, aggregate publication or merge occurs.

## Library producer and consumer order

Use the [complete library-principles source criteria](#complete-library-principles-source-criteria) and [sole selected-result table](../../docs/acceptance-dependencies.md#future-task-construction-and-composition). Seventy-three selected consumers preserve all 2,199 earlier joins and 73 unaffected route meanings. Five missing routes use existing Library inventory, domain/compiler adapter, platform/release assembly, Governance library-admission and stabilization tasks. Add 287 inputs for 2,486 joins over 146 routes, 106 current parents and forty unchanged future records. All 26 L13 criteria retain thirteen existing task routes. No task body/status, source criterion, future declaration, source binding or historical review changes.

The coarse graph now has one 126-task feedback set: both earlier 99-task and nine-task cycles are absorbed, all 108 previous cyclic members survive, and eighteen formerly acyclic tasks enter. Five are the newly routed existing tasks; the other thirteen comprise Auth/Legality conformance and eleven Release execution/acceptance producers. This exposes real whole-task feedback rather than proving an executable order. Actual selected products below distinguish early contracts, real source/owner results, separate independent execution, scope-specific decisions and final release evidence. Current complete interface entailment and independent review remain pending.

| Boundary | Concrete selected product and order |
| --- | --- |
| Namespace, layer and public contract | Library publishes authentic versioned namespace/export/type/macro/protocol/constructor/resource, five-layer, seven-class, profile/stability and intrinsic contracts before consumers. Resolve builds and checks actual binding/import/layer graphs; later compiled/built availability confirms shipped claims. Task-group result edges are not namespace imports and cannot waive actual lower-to-higher rejection. A completed inventory is not its own first declaration. |
| Helper P and generated Q | Genuinely available checked callable P helpers precede macro/compiler-library invocation. Actual generated Q then receives ordinary binding/type/effect/grant/profile/safety checking with complete source/hygiene/phase/origins. Q's future inventory, suite or release is not P's first implementation or Q's first syntax. Preserve the unchanged 857-byte Expand predicate. |
| Structure, types and effect admission | Actual public declarations and structural calls precede independent payload/type/generic/schema premises and preliminary typed verification. Real direct/latent/transitive effects follow; later allowance/profile/resource/service admission and separate authentic grants precede checked export. A pure/checked/intrinsic class, declared allowance or future final typed/effect result cannot manufacture its own first premise. |
| Profile, service and wrapper | Early truthful profile/availability/allocation/blocking/panic/provider/ABI/invariant/checker contracts support callers and independent analysis. Actual selected D8 verdicts, surviving proof/check/audit and concrete needed layouts precede affected use. Real service values, normalization, cleanup and boundary examples establish later behavior. Future final Library/Runtime/Platform release is not the first contract, and hosted convenience cannot silently emulate absent constrained facilities. |
| Compiler authority and generated APIs | L6's :compiler/read-ir effect and C17's scoped :compiler/ir-transform read authority precede IR inspection. Read-mir grants no write-mir; real writes need their own scope/effect. Actual selected library/compiler/domain products and normal verifiers establish claims; package identity, API registration and intrinsic names confer no privilege. All held plugin/callback/provider actions remain unrun. |
| Raw MIR and optional preservation | Actual admitted library calls and current owner tables precede verified raw MIR. Selected transforms use actual mode/resource/error/wrapper/check/authority semantics and available checker interfaces, then required exact-subject proof/certificate/validation, affected regeneration and new-subject verification before use. Normal value equality or structural validity does not preserve lost failure/cleanup/audit behavior. Valid unoptimized APIs remain usable without universal Opt or full future Formal completion. |
| Artifact and execution | Early genuine target/manifest/canonical-record contracts precede actual admitted target artifacts, written bytes/readback and matching provenance. Actual selected checked-Core input or emitted compiled input precedes real API results; an interpreter requires no universal native artifact. Wrapper and typed service contracts precede actual conversion/transfer/cleanup. Fakes and host builtins accept no wider library/profile or live provider. |
| Owner cases and independent harness | Real available implementations, owner case contracts and source examples precede the dedicated independent library harness. Selected Auth/Legality boundaries, Runtime families/services, property generators/seeds and documentation examples supply actual required premises and results without requiring their whole future acceptance first. Library inventory consumes actual independent results later. Held/unavailable required cases remain work; no final self-hosting/full-language/release receipt is the first API fixture. |
| Library review and stabilization | Early real policy/criteria/schema and actual candidate API/profile/safety/compatibility review products support the scope-specific decisions required by GOV3/GOV8/STD20. Actual scoped library review precedes promotion; final released-module admission also consumes actual stability and signed-candidate evidence. Relevant experiment closure and applicable actual security/unsafe review remain required. A whole final admission is not its own first review input; an early schema or scoped review is not final release approval or a new ordinary-development permission. |
| Signature producer and installation consumer | Actual canonical artifact content/manifest/provenance/attestation/SBOM/safety and required identity/policy evidence precede a real signature and verification record under PKG12. Early signing schemas/key policy precede that producer. Final released-module admission consumes actual signed-candidate evidence; Build installation/publication then consumes actual applicable Governance decisions. The same package task's final consumer verdict is not its first signature-format interface. No signature is fabricated, key/consumer policy weakened, signing action authorized or held operation performed here. |
| Release assembly and compatibility | Early real stability/availability/assembly interfaces plus actual per-export candidate evidence precede Library's later release-bound inventory/examples/audit/compatibility assembly. Actual independent conformance, scoped decisions, migration/feasible shims and exact signed provenance support claimed release states. Profile removal is breaking even when hosted behavior stays unchanged. Library assembles records, Governance owns decisions, Build real signatures/verification, and Release the claimed component/product result. Compiler-support closure stays distinct from wider modules and final seedless release. |
| Invalidation, tools and remaining cycles | Real source/export/layer/class/mode/provider/schema/profile/policy changes invalidate affected typed/effect/check/audit/example/compatibility artifacts before owner regeneration and reuse. Source-only docs/index services can work earlier; semantic views need current real producers and runnable examples actual APIs. The 126-task cycle is not permission for unmarked source/build cycles: retain PKG2 acyclic graph legality and only its explicitly governed bootstrap fixed-point boundary. Preserve unresolved D1/C1/C5/C6 ordering and L6/L12 hermetic wording decisions. |

The complete L13 source record, all ten restored predicates (5,758 bytes), one unchanged generated predicate (857 bytes), thirteen original owners and their qualified reviews remain exact. Selected STD1/TEST7/GOV2/GOV3/GOV8/GOV9/STD20/PKG12/C17/L6/L15 interfaces confer no whole support-contract acceptance. Six artifact families and nine actual diagnostics retain source/namespace/symbol/profile/target/contract/caller-effect/grant/remediation identity. Physical capacity, queues, indices/arity, layout, Unicode and target representability survive; removed project work/cost/input/attempt quotas do not return. All current task keys and forty future records/source bindings remain unchanged.

Current source/owner/interface entailment, implementation acceptance, independent current review and integration remain pending/unassessed. Reader contract coverage remains the first actual implementation increment. Preserve all actual writer reservations, original C7/C12 and paired/provider/plugin/native/callback/device/probe/replay/migration/private-store/signing/publication restrictions and stopped handoffs. No held execution, other-chat message, reviewer wake, aggregate publication or merge occurs.
## Facet dependency interface

Use the [facet producer and consumer order](../expand-workflow/plan.md#facet-producer-and-consumer-order) and existing selected-result table. Actual available checked compiler/library helper P precedes facet invocation; generated APIs receive ordinary Q checking with declared effects/grants/profiles/privacy. Earlier Library source/dependency populations remain bound to their checkpoints; the latest facet continuation and sole table record the current graph. No whole library release gates first source declarations.

Task bodies/statuses, source criteria, historical reviews and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance remain pending. No held action is enabled.


## Complete interoperability and migration source interface

Use the [complete interoperability and migration source criteria](../runtime-workflow/plan.md#complete-interoperability-and-migration-source-criteria). Existing resource APIs own actual safe foreign wrappers, conversions and named release over selected real Runtime/provider services; schema/service adapters own usable typed codecs/validators and declared normal effects/grants. Metadata, function names, host GC and generation-only bundles cannot establish executed value/error/cleanup behavior.

Current source/interface entailment and independent acceptance remain pending. Preserve existing task identities/statuses, historical review scopes and every actual restriction. No held execution is enabled.


## Interoperability dependency interface

Use the [interoperability producer and consumer order](../runtime-workflow/plan.md#interoperability-producer-and-consumer-order) and sole selected-result table. Complete L19 resource APIs consume early actual safe-wrapper/type/directional mapping/lifetime/error/provider contracts, then real admitted selected Runtime services and grants before usable value/failure/release results. Compiler records, a release name and host GC implement none of the API. Actual required wrapper/audit evidence precedes safe use; unrelated source construction needs no complete library or native service. Complete L19 typed schema/query/workflow/AI/tool adapters use early real source/codec/validator/protocol contracts and later actual checked generated products and claimed permitted service behavior. Pure source generation needs no live database/model/tool/store, while actual calls need real effects/grants/error/ownership enforcement and replay/audit where required. Drift and unvalidated responses remain refused, not hidden behind host convenience.

Task bodies/statuses, all source criteria, historical reviews and forty future source bindings remain unchanged. Current source/interface entailment, independent acceptance and integration remain pending. No held action is enabled.


## Complete current collections source criteria

The complete current [STD3](../../docs/phase-16-standard-library/213-std3-collections-library-specification.md) governs these criteria. The preserved local census's older STD3 identity remains historical: source hash 58d096cc16d922ad72d560c5b3e4d60d093c9527a9472c2414d63deb98630e3d is not refreshed into current af847392b2ff50155d61a9a9c93bb6e2f1cc573220ceb11920de4dd483bd4160. Eighty-eight of 94 original occurrences retain exact bytes; six changed occurrences have five explicit successor groups. All 166 facet identities and 26 original owner obligations remain. This is identity/custody and an author source proposal, not semantic equivalence, independent whole-source acceptance or implementation credit.

| Criterion | Existing task owner | Current behavior and required evidence |
| --- | --- | --- |
| `module` | `stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations` | STD3 current module criterion (Open): Bind the actual gravity.collections namespace, artifact and every supported public constructor/query/update/traversal/builder/protocol/bounded API to callable Gravity exports with type/effect/allocation/error/profile/stability, representation/provider/ownership and target metadata. Include the complete required surface, not just compiler-reachable exports; leave unsupported required operations Open. Refuse declaration-only or host-substituted availability and false manifest/package/example claims. Evidence: exact per-export implementation and accepted/refused profile/API/diagnostic/artifact joins, including required example, representation, benchmark and schema-boundary products. |
| `surface` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | STD3 current surface criterion (Open): Implement the complete STD3 constructor, query, update, traversal, builder, protocol and bounded-system surfaces, with actual supported overloads stated separately from the language laws. Preserve old values, Gravity equality/hash and any promised ordering; carry explicit allocation, provider, checked/proven bounds, unsafe audit and borrow/copy/retain/stream policy. Traversal callbacks retain their declared effects; inert payload laws do not prohibit explicitly requested callbacks. Refuse host-null/identity/order/coercion shortcuts, hidden allocation, invalid views/builders or unsupported profile claims. Evidence: real callable positives and exact STD3001-STD3008 refusals, per-profile examples, proof/check/audit and representation-specific benchmark/schema-boundary results. |
| `families` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | STD3 current families criterion (Open): Implement unary list?, vector?, map? and set? as actual evaluated value-family classification; include empty persistent Lists and distinguish traversal views or other convertible carriers. Preserve once-only L2 operand effects and transfers, inert stored keys/values/payloads and original values. Classification does not construct a collection or establish member types, cardinality, presence, bounds, order or flow refinements. Refuse spelling/carrier/host coercion and invented facts or new profile/target permission. Evidence: actual family/nonfamily, empty, false/nil payload, effect/transfer and owner-diagnostic cases, with compatibility bindings kept distinct from implemented module exports. |
| `set-conversion` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | STD3 current set-conversion criterion (Open): Implement finite Vector/List-to-persistent-Set conversion using supported Gravity equality/hash classes, including empty and duplicate inputs, preserving the old input and inert payloads without promising order, identity or sharing. The initial List subset is exactly a homogeneous Keyword List returning bare Set, with no static member-domain/cardinality refinement. Complete first/rest termination and every physical Keyword validation before comparison, formatting, hashing or insertion; partial or skipped census cannot pass. Declare temporary/output allocation even when empty, shallow persistent value lifetimes and no borrowed List spine. A Never operand does not invoke conversion or directly allocate, while latent allocation and ordinary source/profile/target/binding/effect checks survive. Refuse nil/arbitrary views, unsupported members and malformed tails. Evidence: actual complete-census, duplicate/empty, late-invalid, transfer/Never and allocation/profile results; removed project work/element/depth allowances are not prerequisites. |
| `set-query` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | STD3 current set-query criterion (Open): Implement Set count as the nonnegative number of distinct stored members and binary contains? as applicable Gravity member value equality, including empty/present/absent cases. Operands evaluate once in ordinary order; transfers abort invocation, original Sets and inert payloads survive, and membership does not construct a collection. Refuse identity/enumeration-order substitutes, truthiness and unestablished member/equality/profile support. Evidence: actual distinct-count and present/absent/nested supported-member cases, ordinary effect/transfer preservation and exact owning refusals; no iteration, cardinality-analysis or flow policy is inferred from membership. |
| `set-insert` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | STD3 current set-insert criterion (Open): Implement binary Set conj by retaining all old value-equality classes and adding the new class only when absent. An equal member is accepted and leaves a value-equivalent Set. Preserve L2 order/transfers, inert payloads and the old Set; declare allocation/storage/lifetime even for present members. Refuse host equality/hash substitutions and invented cardinality/member/order/identity facts or variadic/arbitrary-member/profile support. Evidence: actual empty/present/absent, equal-but-distinct supported values, effects/transfers, persistence and allocation/refusal fixtures. |
| `set-equality` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | STD3 current set-equality criterion (Open): Implement binary persistent Set equality by absence of unmatched members under applicable member equality; empty Sets are equal. Preserve once-only ordered operand effects/transfers, originals and inert payloads. Equality does not construct a collection. Refuse enumeration/object-identity/sharing substitutes and unselected numeric conversion, cross-collection equality, hashing, representation or profile claims. Evidence: actual empty/reordered/equal/different-membership and supported nested-value cases plus transfer and unsupported-domain refusals. |
| `vector-append` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | STD3 current vector-append criterion (Open): Implement binary Vector conj as the original ordered prefix followed by one stored value, with heterogeneous values permitted by the language law. Preserve the original, inert payloads and once-only ordered operand effects/transfers; declare possible allocation/storage/lifetime including empty input. Refuse coercion, variadic extension, invented static type/cardinality facts, or unsupported representation/profile behavior. Evidence: actual empty/nonempty/heterogeneous/false/nil append, persistence, transfer, allocation and supported-subset diagnostic cases. |
| `vector-equality` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | STD3 current vector-equality criterion (Open): Implement persistent Vector equality by equal counts and corresponding supported Gravity stored-value equality in order. Preserve originals, inert payloads and ordinary operand effects/transfers. Refuse identity/sharing, reordered comparison and unselected numeric conversion/cross-family/representation/profile behavior. Evidence: actual equal, reordered, changed, unequal-length and nested supported values, including transfers and explicit unsupported-domain refusals. |
| `list-equality` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | STD3 current list-equality criterion (Open): Implement binary persistent List equality by equal counts and corresponding supported stored-value equality in order, including equal empty Lists. Preserve originals, inert payloads and once-only ordered operand effects/transfers without constructing a collection. Refuse object/type identity, sharing or order-independent substitutes and unselected numeric/cross-family/variadic/profile behavior. Evidence: actual empty/equal/reordered/changed/unequal-length/nested supported values and explicit compiler equality-domain/refusal records. |
| `vector-get` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | STD3 current vector-get criterion (Open): Implement binary Vector get only with independently established integer 0 <= index < count, returning that stored value without evaluating it or changing the Vector. Preserve eager ordered operand effects and transfers. Bounds require the owning proof or runtime check; the type family alone is insufficient. Refuse unjustified in-range facts and invented out-of-range/host-null policies or representation/profile support. Evidence: actual valid first/middle/last/false/nil lookup, proof/check products, transfers and exact unestablished-bound/subset diagnostics. |
| `vector-slice` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | STD3 current vector-slice criterion (Open): Implement ternary subvec for independently established integer 0 <= start <= end <= count, returning the ordered persistent half-open slice, including equal-index empty ranges. Preserve the old Vector, inert payloads and eager once-only operand effects/transfers. Declare allocation even for empty/full slices and whether storage retains, borrows or copies under actual lifetime/effect/profile rules. Refuse missing proof/check and invented out-of-range fallback, host exception, two-argument/variadic, literal-only or sharing policy. Evidence: actual empty/full/prefix/middle/suffix, dynamic supported operands, transfer and bound/allocation refusals. |
| `vector-empty` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | STD3 current vector-empty criterion (Open): Implement unary Vector empty? as zero stored elements, independently of stored false/nil or inert payloads. Preserve once-only operand effects/transfers and the original without constructing a collection. Refuse truthiness, payload execution and invented member/extent/nonempty-branch/flow facts or internal allocation-free/complexity/profile claims. Evidence: actual empty/nonempty/false/nil, effect/transfer and supported-representation cases with owning refusals. |
| `vector-first` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | STD3 current vector-first criterion (Open): Implement unary Vector first under independently established permitted nonempty evidence, returning stored index zero including false/nil without evaluating payloads or changing the Vector. Preserve once-only operand evaluation/transfers. Refuse absent nonempty proof/check and invented empty-Vector result, host-sequence/coercion, literal-only, identity/sharing or allocation-free behavior. Evidence: actual singleton/multiple/false/nil/dynamic supported operands, accepted nonempty facts and exact missing-fact/transfer cases; state the compiler's supported operand/member subset. |
| `vector-rest` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | STD3 current vector-rest criterion (Open): Implement unary Vector rest as the ordered persistent List suffix after index zero; empty/singleton Vectors produce empty List. Preserve false/nil, inert payloads, original Vector and once-only operand effects/transfers. Declare possible allocation including empty results, copied List spine, shallow shared values with independent persistent lifetimes and no borrowed Vector backing storage. Validate the actual input before construction without inventing facts. Refuse unsupported operand/member/profile cases and identity/sharing/allocation-free/complexity promises. Evidence: actual suffix/order/lifetime/copy/allocation and late-invalid/transfer cases without reinstating removed construction-work accounting. |
| `map-keys` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | STD3 current map-keys criterion (Open): Implement unary keys as each stored Map key once in a persistent List, including empty Map, preserving stronger declared provider order while default order stays unspecified. Evaluate the operand once in non-tail position; effects/capabilities/transfers survive, Never prevents enumeration/construction/direct allocation, stored values remain inert and the Map unchanged. The checked subset validates a complete finite literal Map's even physical member-types slots, every Keyword key and every value type. A private bare Map needs the separate complete Types entry/use/caller proof; no presence/shape/cardinality/refinement follows. Validate ignored values and malformed tails and enumerate all actual physical keys before fresh copied List construction. Declare allocation including empty, shallow key sharing and persistent lifetimes without borrowed Map storage. Evidence: actual complete census, empty/late-invalid/default-versus-provider order, Never, allocation and caller-proof refusals; removed width/depth/work limits cannot replace validation. |
| `map-default` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | STD3 current map-default criterion (Open): Implement ternary Map get by actual key presence: return the associated value including false/nil, otherwise the default. Evaluate Map, key and default eagerly in order even for a present key; transfers abort invocation. Preserve the Map and STD2 key equality. Refuse truthiness, lazy/default-thunk or host-null substitutes and unsupported representation/profile claims. Evidence: actual present/missing/false/nil and effectful/default-transfer order cases with owning type/domain refusals. |
| `map-present` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | STD3 current map-present criterion (Open): Implement binary Map get only under independent evidence that the evaluated key is present, returning the stored value including false/nil without evaluating it or changing the Map. Preserve eager Map-then-key evaluation and transfers; a nonreturning operand requires no returned value/presence fact. Refuse presence inferred from member families alone and invented absent/host-null policies or representation/profile claims. Evidence: actual independent-presence and missing-fact cases, false/nil, effects/transfers and explicit unsupported-subset diagnostics. |
| `map-remove` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | STD3 current map-remove criterion (Open): Implement binary dissoc by removing the association matching applicable STD2 key equality; an absent key yields a value-equivalent Map. Preserve all other associations, original Map and inert payloads, with ordinary ordered operand effects/transfers. Declare possible allocation/storage/lifetime for both present and absent keys. Refuse truthiness, host equality and identity/sharing/allocation-free/variadic/arbitrary-key claims. Evidence: actual present/absent/empty/false/nil persistence, transfer and allocation/profile cases. |
| `list-queries` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | STD3 current list-queries criterion (Open): Implement persistent List first as its first stored value or nil when empty; rest as its ordered List suffix or empty List; empty? as zero elements; count as its nonnegative stored-element count. Evaluate the operand normally, preserve effects/transfers, original Lists and inert payloads, including false/nil. Declare whether suffix storage retains, borrows or copies under actual memory/profile rules; allocation-free claims need selected representation evidence. Refuse host sequence substitutes, payload execution and ungrounded storage/profile claims. Evidence: actual empty/singleton/multiple/order/count/false/nil, lifetime/storage and transfer cases for each API. |
| `list-prepend` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | STD3 current list-prepend criterion (Open): Implement binary List conj by prepending the new stored value to the old ordered values, preserving the original and inert payloads. Evaluate operands once in ordinary order and preserve transfers. Declare construction allocation/storage/lifetime under actual effect/ownership/profile rules; no identical suffix storage is promised. Refuse variadic/arbitrary-sequence extension, host mutation and undeclared allocation/profile behavior. Evidence: actual empty/nonempty/order/false/nil, persistence/lifetime and effect/transfer/refusal cases. |
| `ordering` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | STD3 current ordering criterion (Open): Require actual comparator totality for ordered maps/sorted sets and preserve each provider's promised iteration order; default Map/Set contracts gain no implicit order. Equality/hash follow STD2 and survive target/host delegation. Refuse non-total comparators, host identity/equality drift and claimed order without its contract. Evidence: actual comparator acceptance/refusal, ordered versus unordered traversal, cross-target equality/hash and exact STD3005/STD3006 diagnostics. |
| `builders` | `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | STD3 current builders criterion (Open): Implement transient/builders as linear or scope-bound APIs unusable after persistent conversion; iterator/sequence views declare retention, borrowing, copying, streaming and invalidation. Bounded systems variants expose actual physical representation/allocation/lifetime policy; this is not a compiler work allowance. Refuse scope escape, use-after-freeze/invalidation, hidden allocation and unsafe optimized internals without checked/proved wrappers and audits. Evidence: actual builder/freeze/iterator/view mutation and lifetime cases, restricted-profile refusals, safe-wrapper records and owning STD3001-STD3004/STD3008 diagnostics. |
| `private-domain` | `types/current-evidence-and-remaining-work/types1-solve-core-constraints` | STD3 current private-domain criterion (Open): For the immutable bare-Map keys exception, reconstruct the complete closed private function/use/call census at each compiler consumer, with actual argument sources/types, bare-Map compatibility and complete normal-entry whole Keyword-key/value validation. Different entry shapes/value types and empty Map witnesses are allowed; the parameter remains bare Map and the result homogeneous Keyword List. At least one normal entry is required; any Never coordinate makes a call non-invoking. Refuse unknown/circular/unaccounted/all-non-invoking witnesses and omitted public entry/escape/mutation/re-entry. Retain function/parameter/use/caller dependencies and invalidations; selected-key presence is insufficient and no shape/presence/cardinality/flow fact is added. Evidence: actual positive and late-invalid caller/type products and exact refusals, with no removed 512-pair/depth/work allowances reintroduced. |
| `builder-flow` | `core/current-evidence-and-remaining-work/core3-desugar-without-semantic-loss` | STD3 current builder-flow criterion (Open): Preserve Core's structured linear resource/scoped-flow responsibility when lowering actual STD3 transient/builders and memory-backed views: retain checked ownership, scope/freeze/transfer and cleanup obligations into downstream products rather than converting them into ordinary reusable values. Library owns callable APIs; Legality owns actual lifetime/alias/linearity verdicts and Runtime owns required execution. Refuse missing/duplicated consumption, escaped scopes and hidden unsafe responsibility. Evidence: real checked Core-to-MIR builder/scope/transfer products and owning positive/negative fixtures; this source interface accepts no whole Core or collection implementation. |

### Current source succession and accounting removal

The List-to-Set provider now validates complete first/rest traversal, termination and every physical Keyword before any comparison/hash/insertion; temporary and output allocation/lifetimes remain real. Vector rest validates its input before construction. Map keys validates every physical key and complete paired value type, including ignored values and malformed tails, plus the complete private normal-entry proof where used. Old element/width/pair/depth/logical-work counters and prepaid/cumulative work checks do not survive as current prerequisites. Source successorship does not erase the old identity, renew an old review or promote finite old runtime evidence.

Every current source section's complete preamble, lists, example and following conditions is conjunctive. Source labels/family classification create no member types, presence, cardinality, nonempty or flow facts; independent owning evidence is required for operations that consume them. Module-surface overloads are real required behavior, while implemented subsets are stated separately and remain Open when missing. Library owns callable exports, Types owns inference/premises, Core owns structured lowering, Legality owns safety analysis and Runtime owns actual representation/execution. No role transfers or second ledger are added.

### Real interfaces and evidence

`backends`: Actual selected backend/provider products preserve current collection semantics, including false/nil, inert payloads, ordinary transfers, representation-specific allocation and complete validation. Supported overload/member/operand subsets are declared separately from language laws. Target conformance uses actual outputs and current metadata, and optional/unavailable execution never becomes a pass or an initial whole-backend gate.

`build`: Build binds actual module exports, representation/provider/profile/target/effect/allocation metadata, compiler/toolchain/lock/source and examples to genuine artifact identities. Changed collection source, caller/use proofs or representation facts invalidate affected products; an old receipt or compatibility binding proves no current callable module. Signing/publication remains separate.

`core`: Core retains ordinary left-to-right, eager, once-only evaluation, effects, transfers and explicit linear builder/scope/cleanup lowering. Inert stored payload laws do not erase explicitly requested traversal callbacks. Never prevents invocation/direct allocation while latent signature requirements and ordinary legality still apply.

`legality`: Legality supplies actual profile, ownership/lifetime, bounds/check/proof, builder linearity, invalidation and safe-wrapper/audit verdicts before admitted collection operations. A family, bare type, schema, digest or actual result is no replacement proof; constraints concern actual representation and semantics, not removed project admission quotas.

`opt`: Optimization preserves actual collection value/evaluation/effect/transfer, equality/hash/order, allocation/lifetime and source/proof facts. Exact dominating evidence valid through transformations is required before removing checks; invalidation requires genuine repair/rechecking, restoring checks or rejection. Classification creates no implicit cardinality, presence, bounds or flow refinements.

`release`: Independent library conformance consumes actual callable module/profile/representation fixtures, expected STD3001-STD3008 diagnostics, complete-census and invalid tail/value cases, type/flow and lifetime/proof products, property/benchmark/schema-boundary results and real selected target comparisons. Source inventory, p16 summary status flags and an injected fixture do not establish module acceptance. Preserve finite receipt scopes and actual holds.

`runtime`: Runtime supplies actual chosen representation/provider storage, retention/borrow/copy/stream, allocation/failure, persistent shared-value lifetimes and explicit cleanup behavior. Vector-rest and Map-keys Lists own copied spines and borrow no input backing storage; List-to-Set temporary/output storage has the declared independent lifetimes. GC, native capacity or old work counters cannot replace these facts.

`types`: Types retains independently established bounds, nonempty and Map-presence premises and the complete private whole-key-domain caller/use proof. Family labels and the result of keys cannot create their own input facts. Current STD3 has no removed pair/depth/work allowance; complete physical key and value validation, invalidation and each consumer's reconstruction remain.

The read-only inspection of the existing p16 source-artifact/document-record helpers found summary-derived accepted/complete statuses; no real library call or conformance ran here. Existing implementation and accepted receipts keep their narrow scopes; other callable implementations remain unassessed. Actual API fixtures and required checker/provider results precede their claims, while future whole-library, backend or release acceptance is not the first definition or type contract. The existing dependency table remains the sole navigation authority and is not changed in this source continuation.

Other STD1/STD2/STD4/STD5/STD6 sources were read as context; their own complete source/task reconciliation and STD7-STD20 remain separate work. Preserve every task status, finite historical review, forty future declaration/binding, actual restriction and writer reservation. Independent current source/owner/interface acceptance and global conjunctions remain pending. No Reader/Gravity/bootstrap/paired/C12/provider/native/callback/device/probe/replay/migration/private-store/signing/publication execution or stopped C7 handoff retry/transfer occurs; no other chat, reviewer or automation is messaged/woken. The first implementation increment stays [Reader contract coverage](../reader-workflow/plan.md#selected-increment-complete-contract-coverage).

## Historical workflow-transfer plan

# Implementation plan: Standard Library workflow migration

Spec: [spec.md](spec.md) | Migration owner: documentation migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Contract alignment

Follow the retained contracts linked in spec.md and [constitution](../../.specify/memory/constitution.md). The current requested migration extends the original pilot scope, without changing language semantics or execution restrictions. Use [development verification](../../docs/development-verification.md) when an actual implementation slice is selected; this documentation change requires structural/link checks, not another compiler proof run.

## Canonical task source

Edit implementation records only in [tasks.md](tasks.md#implementation-tasks). These original outcome sections retain their acceptance boundaries and selected-versus-wider scope:

- [remaining tasks and evidence](../../docs/component-roadmaps/stdlib.md#remaining-tasks-and-evidence)
- [compiler support outcome](../../docs/component-roadmaps/stdlib.md#compiler-support-outcome)

## Approach and exact files

1. `spec.md`: scope, retained contract navigation and accepted/rejected workflow behavior.
2. `plan.md`: canonical task anchors, interfaces and dependency boundaries.
3. `tasks.md`: five implementation records and the entire unchanged historical migration file, clearly separated.
4. `evidence.md`: observed checks and implementation nonclaims.

Central migration inventory/schema is owned separately. This package does not write shared indexes or transfer implementation ownership.

## Interfaces and dependencies

Core, Types, Effects, Legality and Auth govern compiled API acceptance. Runtime providers are needed only for serviced APIs; domain and Platform adapters consume their actual outcomes. Build packages the results later.

## Validation and evidence

Check all local Markdown targets and anchors, four-artifact presence, ASCII and semantic names. Negative controls must reject an absent file/anchor and duplicate implementation task truth. Run existing document and full-language roadmap validators against the candidate. Evidence is documentation-only; no compiler, target execution, external CLI or upstream workflow discovery is run for this slice.

## Delivery and remaining work

Publish the prepared slice as a draft. An independent reviewer checks mapping, anchors, scope and evidence; Deus integrates only the reviewed exact tree. Central inventory links and any remaining per-contract semantic-routing questions stay explicit, not silently marked accepted.

## Names, callers and compatibility

Package key `stdlib-workflow` is semantic. No imports, load order, manifests, source hashes, schemas or public APIs are changed. Historical planning IDs in canonical links remain traceability only.
