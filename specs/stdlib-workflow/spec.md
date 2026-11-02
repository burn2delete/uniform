# Standard library implementation specification

Implementation scope added under the 2026-11-01 request. Existing library and
specialist domain/runtime/platform owners retain their responsibilities.
Historical migration below establishes no callable export or module acceptance.

## Behavior and interfaces

Implement [the Standard Library outcome](../../docs/component-roadmaps/stdlib.md#release-outcome)
under complete L13, STD1-STD20, TEST7 and GOV3 retained below. Actual compiled
Gravity modules, type/effect/profile/capability/safety and source-schema facts,
explicit runtime/providers and package/compatibility policy produce callable
exports, real API examples/results, module/export manifests, profile matrices,
safe-wrapper/audit evidence, documentation, conformance and release artifacts.

Every public namespace/export has actual type, effects, authority, allocation/
resource/blocking/error/nondeterminism, profile/target and stability behavior.
Hosted implementations preserve Gravity laws rather than importing host
equality, null, collection, Unicode or numeric conventions. Compiler primitive
forms and recognized compatibility bindings do not automatically establish
implemented standard module exports.

The existing [compiler-support outcome](../../docs/component-roadmaps/stdlib.md#compiler-support-outcome)
binds the complete reachable export/helper closure required by the selected
compiler recipe using the same five task records. It can finish before every
application-facing module. Wider STD1-STD20 APIs remain required, separately
owned and open until their actual complete evidence exists.

## Existing implementation routes and acceptance

Use [the sole marked ledger](tasks.md#implementation-tasks) under
`stdlib/remaining-tasks-and-evidence/`. Existing criteria/status, finer source
joins, compiler-support scope and accepted narrow receipts retain their meaning.

| Existing suffix | Required accepted behavior | Required rejection and evidence |
| --- | --- | --- |
| `reconcile-export-inventory-with-actual-callable-implementations` | Bind each required namespace/export/profile to actual source, compiled binding, type/effect/allocation/error/authority/stability metadata and positive/negative fixture or explicit required proof. Include all compiler-reachable helpers and wider module surfaces without conflating their outcomes. | Keep absent/unassessed exports explicit; reject inventory-only implementation, compatibility spelling as module-export proof, host-only portability and unsupported profile claims. Actual module manifests, docs and conformance agree with compiled exports. |
| `complete-pure-core-collections-text-numeric-and-testing-apis` | Execute STD2-5/14 APIs with ordinary L2 evaluation/transfers, inert stored data, persistent original values, actual equality/hash/order, explicit partial checks and allocation/storage. Preserve scalar/byte/grapheme/locale/normalization and numeric mode/EFIR/certificate rules; testing APIs produce structured results, seeds/goldens/diagnostics and declared fakes. | Reject fabricated presence/member/extent/flow facts, host-null fallbacks, invalid bounds/Unicode, lossy conversion, implicit fast math, missing proof and hidden effects. Compile real examples/properties across each claimed profile; a narrow builtin subset cannot close whole modules. |
| `complete-resourceconcurrencyionetwork-apis` | Execute STD6-9 through actual allocators, structured tasks/sync/typed channels, resource scopes, scoped paths/streams and network/TLS/schema providers. Preserve complete lifetime/transfer/cleanup, cancellation/backpressure, declared failure, replay and secret policy. | Reject invalid borrow/region/linear/race/atomic behavior, ambient or root-escaping IO, hidden host services, invalid encoding/TLS, missing grants and repeated replay effects. Exercise actual provider denial and teardown; mocks remain explicit and confer no live-provider support. |
| `complete-schemaqueryworkflowaimetacryptoui-adapters` | Implement STD10-15/18/19 facades over actual domain/runtime APIs: validated canonical/versioned schemas, parameterized typed database/transaction/migration, durable replay/compensation, AI prompt/tool/memory/policy/evaluation, checked syntax/pass/plugin, explicit crypto policy and typed UI/event/accessibility/adapter behavior. | Reject schema weakening, tainted queries/results, incompatible in-flight workflow state, injection/unreviewed payload or generated-code bypass, hidden compiler mutation, invalid secret/nonce/algorithm/lifecycle/constant-time evidence and unsafe markup/lifecycle effects. Retain real boundary results and owner-produced artifacts; a facade cannot replace the corresponding domain implementation. |
| `integrate-platform-modules-and-close-release-manifests` | Execute STD16/17 APIs against declared target/host/device/provider contracts, preserving process/env/time/random distinctions, MMIO/interrupt/DMA/clock/volatile ordering and safe wrappers. Complete STD20/GOV3 ownership, export stability, compatibility/deprecation/migration and signed module/package evidence. | Reject target/host confusion, ambient authority, missing device/register/interrupt/DMA/synchronization facts, unsafe leaks, invalid profile access and evidence-free stabilization. Actual supported platform fixtures, audits and per-profile examples are required; unavailable hardware or unfinished Platform decisions remain explicit. |

STD3's current List-to-Set and Map-keys rules require complete physical member
and type validation, including ignored values/malformed tails, before
construction/comparison. Private bare-Map key-domain use requires the complete
independent caller/use/entry evidence, with no invented presence/cardinality
facts. Preserve the source's precise result/allocation/storage/evaluation laws;
do not restore the removed prepaid/width/depth allowances from its predecessor.

STD18 retains explicit algorithm families/parameters/lifecycles, hybrid
transcript/combiner/downgrade and migration inventory, secret/constant-time and
independent verification requirements. Provider defaults cannot choose those
semantics. STD20/GOV3 policy owners classify actual compatibility/stability
changes; this implementation route grants no blanket stabilization authority.
Other superseded project accounting clauses remain with accounting removal;
actual target format, lifetime, safety, platform and external account policy
requirements remain distinct.

## Starting work and completion

Explicit compiler/provider/domain interfaces and focused fixtures permit local
API work before all runtime/domain/Platform releases. Pure/compiler-support
work does not wait for unrelated live services. Acceptance of an effectful or
domain adapter needs actual relevant implementation, provider boundary and
consumer composition. Complete each claimed module/profile with callable
exports, accepted/rejected examples, properties or required proof, safe-wrapper
evidence and accurate manifests/docs/compatibility. Preserve writer/operation
holds and custody; independent acceptance, integration, wider module completion
and final seedless compiler release remain separate.

## Historical workflow-transfer specification

# Feature specification: Standard Library workflow migration

Status: prepared; independent review and integration pending.
Migration owner role: documentation migration maintainer.
Component owner role: library maintainer; person assignment remains unverified.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Purpose and canonical contracts

The retained standard-library module inventory, profile-aware exports, safe wrappers, metadata, runnable examples and compiler-support subset.

Adopt a four-artifact Spec Kit workflow around the [canonical component roadmap](../../docs/component-roadmaps/stdlib.md). Language rules stay in their retained contracts; implementation records are edited only in [tasks.md](tasks.md#implementation-tasks), with a checked direct roadmap link to the task ledger. This is workflow migration, not a rewrite of normative specifications or a new implementation result.

[Inventory](../../docs/document-inventory.json) entries routed for this component are listed below as reviewable navigation, not proof of semantic conformance or exclusive ownership. The routing proposal must be reconciled centrally; cross-component links are legitimate.

- [L13 - Standard Library Design Principles](../../docs/phase-01-core-language/023-l13-standard-library-design-principles.md)
- [TEST7 - Standard Library Test Strategy](../../docs/phase-14-testing-verification-and-conformance/196-test7-standard-library-test-strategy.md)
- [STD1 - Standard Library Architecture](../../docs/phase-16-standard-library/211-std1-standard-library-architecture.md)
- [STD2 - Core Library Specification](../../docs/phase-16-standard-library/212-std2-core-library-specification.md)
- [STD3 - Collections Library Specification](../../docs/phase-16-standard-library/213-std3-collections-library-specification.md)
- [STD4 - String and Text Library Specification](../../docs/phase-16-standard-library/214-std4-string-and-text-library-specification.md)
- [STD5 - Numeric and Math Library Specification](../../docs/phase-16-standard-library/215-std5-numeric-and-math-library-specification.md)
- [STD6 - Memory and Resource Library Specification](../../docs/phase-16-standard-library/216-std6-memory-and-resource-library-specification.md)
- [STD7 - Concurrency Library Specification](../../docs/phase-16-standard-library/217-std7-concurrency-library-specification.md)
- [STD8 - IO and Filesystem Library Specification](../../docs/phase-16-standard-library/218-std8-io-and-filesystem-library-specification.md)
- [STD9 - Network and HTTP Library Specification](../../docs/phase-16-standard-library/219-std9-network-and-http-library-specification.md)
- [STD10 - Serialization and Schema Library Specification](../../docs/phase-16-standard-library/220-std10-serialization-and-schema-library-specification.md)
- [STD11 - Database and Query Library Specification](../../docs/phase-16-standard-library/221-std11-database-and-query-library-specification.md)
- [STD12 - Workflow Library Specification](../../docs/phase-16-standard-library/222-std12-workflow-library-specification.md)
- [STD13 - AI and Agent Library Specification](../../docs/phase-16-standard-library/223-std13-ai-and-agent-library-specification.md)
- [STD14 - Testing Library Specification](../../docs/phase-16-standard-library/224-std14-testing-library-specification.md)
- [STD15 - Compiler Meta-Programming Library Specification](../../docs/phase-16-standard-library/225-std15-compiler-meta-programming-library-specification.md)
- [STD16 - Platform and OS Library Specification](../../docs/phase-16-standard-library/226-std16-platform-and-os-library-specification.md)
- [STD17 - Hardware and Firmware Library Specification](../../docs/phase-16-standard-library/227-std17-hardware-and-firmware-library-specification.md)
- [STD18 - Cryptography Library Specification](../../docs/phase-16-standard-library/228-std18-cryptography-library-specification.md)
- [STD19 - UI and Application Library Specification](../../docs/phase-16-standard-library/229-std19-ui-and-application-library-specification.md)
- [STD20 - Standard Library Stability Policy](../../docs/phase-16-standard-library/230-std20-standard-library-stability-policy.md)
- [GOV3 - Standard Library Governance](../../docs/phase-17-governance-and-evolution/233-gov3-standard-library-governance.md)

## User scenarios and acceptance

### US1 - Find and execute the next canonical slice

A contributor starts here, follows plan.md to the existing task source, selects a finite owned slice and records actual validation in its implementation evidence. Acceptance: all four artifacts and retained-contract/task links resolve; no second editable implementation checklist exists. Rejection: a missing target, substituted normative authority, fabricated passing result or duplicate editable implementation ledger blocks workflow acceptance.

### US2 - Distinguish workflow adoption from product completion

A reader sees preparation, independent review and integration separately in tasks.md and evidence.md. Component implementation remains unassessed by this package. Acceptance requires this distinction to survive linking and integration; a directory, checkbox or merged documentation PR cannot close a component outcome.

## Scope and compatibility

Own `specs/stdlib-workflow/{spec.md,plan.md,tasks.md,evidence.md}`. No compiler/runtime source, existing contract, shared helper, historical pilot, protocol ID or permission changes. No Specify CLI execution or automatic discovery is claimed.

## Evidence and outcome ownership

Documentation migration maintainer owns these artifacts and link checks. The library maintainer retains implementation semantics. Independent review is pending; Deus coordinates integration. [Evidence](evidence.md) records observed checks and nonclaims.

## Unresolved items

The full module/profile release matrix and selected compiler reachable-export closure remain explicit library decisions. A builtin fixture does not complete a module or the entire library. These implementation decisions do not prevent preparing and reviewing this workflow.

## Implementation naming review

The semantic key `stdlib` names this package. Planning IDs appear only in contract references. No implementation namespaces are added or renamed; meaningful protocol and diagnostic IDs remain unchanged. Follow [semantic naming](../../.specify/semantic-names.md).
