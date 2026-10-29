# Standard Library

Working name: **Standard Library** (proposed). Semantic key: `stdlib`.
Owner role: library maintainer; current owner unassigned by this proposal.
Baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232`.

Implementation task authority: [Spec Kit ledger](../../specs/stdlib-workflow/tasks.md#implementation-tasks).
Edit the ledger and run `clojure -M tools/stdlib_task_projection.clj --write`;
the remaining-task table below is generated.

## Purpose and scope

Provide real profile-aware APIs, not host-language escape hatches. The finite
module inventory is the [standard-library contract index](../phase-16-standard-library/README.md).
Own exports, manifests, safe wrappers and examples; compiler language forms,
runtime providers and platform implementation remain separate components.

## Release outcome

The contract inventory's modules have implemented exports, profile/effect/
capability/allocation/stability metadata, executable API examples and rejected
profile/authority fixtures. Unsupported profile/module pairs are explicit.
Full platform modules consume Platform outcomes rather than implying them.
Outcome open; this does not promote metadata coverage to functioning libraries.

## Interfaces and dependencies

Inputs: typed language operations, runtime/provider APIs and module schemas.
Outputs: compiled modules, export manifests, documentation examples, unsafe
island audits and compatibility evidence. Core/Types/Effects/Legality/Auth are
required for compiled API acceptance; Runtime is required only for serviced
APIs. Pure library algorithms and fixtures can develop earlier with the seed.
Build consumes manifests later, so no circular build prerequisite exists.

## Remaining tasks and evidence

<!-- stdlib-task-projection:start -->
| Task (all open) | Evidence baseline | Closing evidence |
| --- | --- | --- |
| Reconcile export inventory with actual callable implementations | [Library source artifact](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/foundation/00499-standard-library-source-artifact.clj) | Per-export source/implementation/fixture map, with absent exports explicitly open; compiled module manifests agree with package metadata, documentation and conformance discovery on namespace/artifact identity, callable exports, profiles, effects, capabilities, allocation, errors, safety and stability. Reject missing or inconsistent metadata, undeclared effects, missing grants, host-dependent core exports and unaudited unsafe internals with STD1001-STD1008 as applicable. Apply the source-specific acceptance and dependency matrix in plan.md. |
| Complete pure core, collections, text, numeric and testing APIs | [Library architecture](../phase-16-standard-library/211-std1-standard-library-architecture.md) | Compiled examples and type/arity/value rejection through real APIs; STD2-STD5 and STD14 fixtures preserve evaluation order, transfers, inert stored values, original persistent values, Unicode boundaries, explicit numeric modes and structured test evidence. Retain each operation's proven-presence/nonempty/in-range premise without invented fallback, coercion, static facts or provider guarantees. Reject invalid UTF-8, implicit locale/lossy conversion, unchecked partial operations, unproved math transformations, free-text-only diagnostic matching and ungranted live-service tests. Compile documentation under every claimed profile; require the exact outputs and negative cases in plan.md. |
| Complete resource/concurrency/IO/network APIs | [Module inventory](../phase-16-standard-library/README.md) | Provider-mediated executions, missing-grant rejection and safe resource teardown; STD6-STD9 fixtures exercise borrow/region escape, exactly-once close/transfer, child cancellation and lifetime, atomic memory order, forbidden blocking, root/symlink traversal, explicit text encoding, atomic-write durability failures, network timeout/retry/redirect/body/cancellation policy, TLS validation and secret redaction. Replay must record or isolate actual external effects and reject unrecorded nondeterminism; host delegation must preserve Gravity diagnostics and provider provenance. Runtime, Legality and Auth dependencies remain explicit in plan.md. |
| Complete schema/query/workflow/AI/meta/crypto/UI adapters | [Module inventory](../phase-16-standard-library/README.md) | APIs consuming corresponding domain/runtime outcomes, with boundary failures exercised; STD10-STD13, STD15, STD18 and STD19 fixtures validate schemas before trusted decoding, canonical bytes, migration compatibility, parameterized queries, transaction commit/rollback/retry/isolation, durable workflow replay and in-flight migration, AI schema/taint/tool/review gates, generated-code rechecking, syntax context and pass invalidations, crypto lifecycle/hybrid binding/redaction, and typed UI events/render trust/accessibility/routes. Require actual provider execution and artifact consumers, not catalogue names or manifest presence. Reject each source-specific boundary failure in plan.md; unresolved provider, binding and equivalence decisions remain unassessed. |
| Integrate platform modules and close release manifests | [Platform outcome](platform.md#release-outcome) | Actual supported platform API fixtures, unsafe audits and compatibility/stability report; STD16-STD17 fixtures distinguish target/build/runtime-host facts, all clock/random kinds, process command/args/environment/stdio/timeout/working-directory policy, MMIO widths/alignment/volatile ordering, interrupt effects/reentrancy, DMA ownership/coherence/completion and clock-domain synchronization. STD20 release evidence compares source, behavior, effects, capabilities, diagnostics, artifact schemas, profiles and fixtures; exercises experimental opt-in/internal-import denial, deprecation/replacement/removal schedules and governed breaking changes; and binds signed provenance to the actual release. Platform target and product-release outcomes retain their separate scope. |
<!-- stdlib-task-projection:end -->

Architecture gate owner: library maintainer. Recommendation: use contract
modules as export ownership boundaries and separate target adapters from pure
APIs. Record the selected module/profile release matrix before claiming module
completion; do not reduce the normative inventory by silently omitting modules.

## Acceptance and integration

Yes/no: does every claimed export execute? Do manifests and compiled exports
agree? Are effects/grants checked? Are unsafe internals isolated? Do all shipped
examples compile for their stated profiles? All must be yes. Integration:
pure compiler support, serviced Runtime APIs, domain adapters, then Build package
and Release compatibility. A builtin used by a bootstrap fixture is not proof
of the entire standard library.

## Compiler support outcome

`stdlib/compiler-support` is the finite release slice comprising the exact
exports reachable from the audited complete compiler source closure. The
library owner records those exports and profiles, implements their semantics,
executes accepted/rejected API fixtures and verifies no unresolved or hidden
host-only dependency remains in the claimed compiler path. It uses the relevant
inventory, pure/API, resource and manifest tasks above without duplicating them.
The selected compiler release may consume this accepted slice without waiting
for UI, AI, platform libraries or every general-purpose module. Full inventory
completion remains the separate wider-language outcome; the compiler slice is
not permission to redefine that full inventory.
