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

## Source-specific acceptance and dependencies

This matrix strengthens the five existing Open implementation records in
[tasks.md](tasks.md#implementation-tasks). It creates no new implementation task
or completion credit. Inventory, Pure, Services, Adapters and Release below mean,
respectively, the existing reconcile-export, complete-pure, complete-resource,
complete-schema and integrate-platform task titles. An export name is catalogue
coverage until its actual binding, supported subset and executable evidence are
known. Selected compiler-support evidence cannot close the wider library.

| Original source | Existing task | Required observable evidence and refusal |
| --- | --- | --- |
| [STD1](../../docs/phase-16-standard-library/211-std1-standard-library-architecture.md) | Inventory | Compiled manifests, per-export fixture discovery and package/docs agreement; reject missing metadata, host-dependent core, missing grants, undeclared effects and unsafe audit gaps (STD1001-STD1008). |
| [STD2](../../docs/phase-16-standard-library/212-std2-core-library-specification.md) | Pure | Cross-profile equality/hash, predicates, option/result/error and syntax metadata fixtures; retain ordinary evaluation/transfers and reject host state, unchecked partial unwrap and lost spans (STD2001-STD2007). |
| [STD3](../../docs/phase-16-standard-library/213-std3-collections-library-specification.md) | Pure | Exercise every specified persistent operation, empty/false/nil cases, evaluation/transfers, complete physical member validation and storage/lifetime policy. Prove required presence/range/nonempty premises; reject invalid bounds, escaped builders, invalidated iterators, non-total comparators and unaudited internals (STD3001-STD3008). Project accounting clauses do not authorize quotas; adjacent validation laws survive. |
| [STD4](../../docs/phase-16-standard-library/214-std4-string-and-text-library-specification.md) | Pure | Scalar/grapheme and supplementary Character cases, UTF-8 validation, exact concatenation/conversion, normalization version/form, explicit locale and provider behavior; reject invalid boundaries, silent loss and taint escape (STD4001-STD4008). Separate actual pattern/provider restrictions from removed compiler work accounting. |
| [STD5](../../docs/phase-16-standard-library/215-std5-numeric-and-math-library-specification.md) | Pure | Explicit checked/wrapping/saturating/trapping and floating modes, EFIR, independently checked approximation certificates and target lowering; reject invalid domains, implicit fast math and stale or missing proofs (STD5001-STD5008). |
| [STD6](../../docs/phase-16-standard-library/216-std6-memory-and-resource-library-specification.md) | Services | Borrow/region/linearity and deterministic cleanup fixtures with actual allocators and host wrappers; reject escape, aliasing, double close, use after transfer and hidden allocators (STD6001-STD6008). |
| [STD7](../../docs/phase-16-standard-library/217-std7-concurrency-library-specification.md) | Services | Structured child lifetime/cancellation, race checks, declared atomic memory order and replay choices; reject detached leaks, forbidden blocking and unrecorded nondeterminism (STD7001-STD7008). External provider billing remains distinct from compiler accounting. |
| [STD8](../../docs/phase-16-standard-library/218-std8-io-and-filesystem-library-specification.md) | Services | Root/operation grants, symlink/cycle/platform policy, encoding, linear handles and atomic-write durability/failure records; reject escape, missing close/policy and replay-unsafe IO (STD8001-STD8008). |
| [STD9](../../docs/phase-16-standard-library/219-std9-network-and-http-library-specification.md) | Services | Client policies, typed routes/bodies, TLS validation, redaction and recorded activities; reject undeclared hosts/ports/listeners, unsafe TLS, unauthorized AI tools and replay-unsafe calls (STD9001-STD9008). |
| [STD10](../../docs/phase-16-standard-library/220-std10-serialization-and-schema-library-specification.md) | Adapters | Validate before trust, canonical bytes across targets, schema paths, reflection-free derivation and checked migration; reject weakening, noncanonical bytes and missing boundary schema identity (STD10001-STD10008). |
| [STD11](../../docs/phase-16-standard-library/221-std11-database-and-query-library-specification.md) | Adapters | Separate structure/parameters, validated rows, provider dialect/errors, commit/rollback/retry/isolation and migration evidence; reject interpolation, absent grants, unsafe migration and AI policy bypass (STD11001-STD11008). Transaction overlap remains a central equivalence decision. |
| [STD12](../../docs/phase-16-standard-library/222-std12-workflow-library-specification.md) | Adapters | Typed graph/events/state, real replay/activity isolation, compensation success/failure and in-flight migrations; reject repeated external effects, unrecorded nondeterminism, missing compensation and incompatible state (STD12001-STD12008). |
| [STD13](../../docs/phase-16-standard-library/223-std13-ai-and-agent-library-specification.md) | Adapters | Provider/model/schema/policy records, authority-partitioned prompts, granted typed tools, memory isolation, eval and review artifacts; reject unvalidated outputs, injection, unauthorized tools and unchecked generated code (STD13001-STD13008). Human review does not itself confer capability. |
| [STD14](../../docs/phase-16-standard-library/224-std14-testing-library-specification.md) | Pure | Structured failures, seeds/generators/shrinking/counterexamples, hashed golden updates, exact diagnostic fields and packageable reports; reject missing provenance, undeclared mocks and ungranted live-service execution (STD14001-STD14008). |
| [STD15](../../docs/phase-16-standard-library/225-std15-compiler-meta-programming-library-specification.md) | Adapters; Pure syntax helpers | Syntax spans/hygiene/phase/context, ordinary generated-code reentry, stable IR views, pass invariants/invalidations and signed compatible plugins; reject unchecked generation, internal mutation and concealed seed trust (STD15001-STD15008). Syntax API/compiler overlap remains unresolved. |
| [STD16](../../docs/phase-16-standard-library/226-std16-platform-and-os-library-specification.md) | Release; Adapters random-provider boundary | Target/build/runtime-host separation, wall/monotonic/CPU/compile/replay clocks, explicit random kind and complete process policies including working directory; reject ambient access, omitted policies and replay reads (STD16001-STD16008). The example's omitted working directory is not a default specification. |
| [STD17](../../docs/phase-16-standard-library/227-std17-hardware-and-firmware-library-specification.md) | Release | Memory-map/interrupt-table outputs, volatile/order-preserving lowering, DMA ownership/coherence and synchronization evidence; reject missing widths/alignment, forbidden interrupt effects and unsynchronized clocks (STD17001-STD17008). Software/device adapters are not blanket HDL aliases. |
| [STD18](../../docs/phase-16-standard-library/228-std18-cryptography-library-specification.md) | Adapters | Declared algorithms/parameters/lifecycles, PQ and hybrid success/failure vectors, transcript/combiner/downgrade checks, migration inventory, secrets and side-channel evidence; reject provider-chosen defaults, unavailable entropy, nonce misuse, leaks and invalid lifecycle policy (STD18001-STD18013). |
| [STD19](../../docs/phase-16-standard-library/229-std19-ui-and-application-library-specification.md) | Adapters | Typed component/state/event/render/route/style/accessibility artifacts consumed by snapshots/interactions; reject hidden effects, unsafe markup, missing labels and absent adapters (STD19001-STD19008). Rendering trust overlap stays context-specific. |
| [STD20](../../docs/phase-16-standard-library/230-std20-standard-library-stability-policy.md) | Release | Per-export stability and eight compatibility dimensions, opt-in/internal import rejection, deprecation guidance/schedules, impact/migration/governance records and signed release provenance (STD20001-STD20008). The illustrative policy form does not invent a callable API. |

Inventory precedes fixture selection for each declared module/profile. Pure
semantics depend on Core, Types, Effects and Legality; Services also depend on
Auth and the actual Runtime provider. Adapters consume Schema, Query, Workflow,
AI, compiler-pass and target outcomes, rather than treating a manifest as their
implementation. Release consumes those verified exports, Platform target
fixtures and Build signing/provenance plus the owning governance decisions.
Each dependency needs its actual artifact and invalidation identity. Missing
provider/signature/fallback detail remains an explicit acceptance gap. The
bounded phase-16 proposal delivered to the canonical reconciliation owner
preserves inherited units/facets, historical receipt pointers, five unresolved
cross-family equivalences and selective accounting lineage for central adoption.
It confers no source-scope, implementation or release acceptance.
