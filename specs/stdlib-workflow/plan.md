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
