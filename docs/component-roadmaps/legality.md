# Legality

Working name: **Legality** (proposed). Semantic key: `legality`.
Owner role: profile, ownership and safety maintainer; current owner unassigned
by this proposal. Baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232`.

## Purpose and scope

Make profile, ownership/lifetime and safety decisions before MIR construction.
These are related analysis interfaces, not a replacement for independent type,
effect or grant authority. Governed by [profiles](../phase-03-profile-system/README.md)
and [safety contracts](../phase-02-safety/README.md).
No backend may legalize rejected source; no optimizer may invent a safety proof.

## Release outcome

Typed/effected source receives profile legality, ownership/lifetime facts and
one of proven-safe, runtime-checked, rejected or unsafe-island for each dangerous
operation, with check obligations connected to execution. Outcome remains open.

## Interfaces and dependencies

Inputs: typed/effected core, module/profile policy, grant facts, control/dataflow
and generated-source origins. Outputs: profile-valid core; borrow/move/lifetime
and resource-flow graphs; safety classifications; runtime checks; proof and
unsafe-audit references; diagnostics. Types/Effects are prerequisites for a real
analysis run. Auth exchanges grant facts; MIR consumes the combined facts.
Policy tables, graph schemas and negative fixtures may proceed in parallel.
Build and release are consumers, not prerequisites for local analysis.

## Remaining tasks and evidence

Implementation task authority: [Spec Kit ledger](../../specs/legality-workflow/tasks.md#implementation-tasks).
The following table is generated; edit the ledger and run
`clojure -M tools/legality_task_projection.clj --write`.

<!-- legality-task-projection:start -->
| Task (all open) | Evidence baseline | Closing evidence |
| --- | --- | --- |
| Complete profile restrictions and generated-form checks | [Profile contract](../../bootstrap/clojure/src/gravity/profile_validation/contract.clj) | Allowed profile matrix and hidden-allocation/reflection/effect rejection before lowering. For SAFE1/SAFE2/SAFE4, reject unavailable checks, memory regimes and provider metadata before backend lowering; distinguish safe, safe-optimized, audited-unsafe, systems, trusted-runtime, experimental and unsafe claims, including weaker dependency modes. Prove or check actual lifetime, layout and failure conditions; no project work allowance substitutes for those semantics. P1/P2/P3/P4/P5/P12: validate one active profile (retaining the explicit implementation-seed exception), reusable support sets/exclusions, and post-expansion/pre-optimization admission; emit portable manifests with dependencies/target/providers. Reject hidden runtime services, impure core generation, unsupported facets and generated-profile violations. Core values remain independent of concrete allocation identity; meta raw memory/FFI requires an explicitly allowed audited implementation-provider boundary. Formal admission rejects unmodeled effects/nondeterminism/unsafe code; assumptions remain explicit. Effect allowances and capability grants are separate checks with an explicit provider mapping, never an intersection of differently typed labels. |
| Complete ownership, borrow, initialization and lifetime analysis | [Ownership contract](../../bootstrap/clojure/src/gravity/c9_ownership_checker/contract.clj) | Real use-after-move, escape, invalid borrow and linear-resource fixtures with graphs. Consume the [SAFE2-SAFE5 contracts](../../specs/legality-workflow/plan.md): partial initialization drops only initialized fields; disjoint borrows require range proof; calls expose copy/move/borrow/consume behavior; region exit and arena reset invalidate dependent references; reject foreign retention and detached captures without lifetime proof. Demonstrate provider-specific exactly-once terminal cleanup on normal, exceptional, panic, cancellation, timeout and early-return paths, including generated linear duplication rejection. Lease renewal versus terminal consumption remains a source qualification, not an inferred terminal event. |
| Complete safe-code classification and taint/unsafe boundaries | [Safety contract](../../bootstrap/clojure/src/gravity/c10_safety_analysis/contract.clj) | Every dangerous operation classified and unclassified operations rejected. Under the [SAFE1/SAFE6-SAFE11/SAFE15 contracts](../../specs/legality-workflow/plan.md), inspect real checks before operations with defined profile-legal failure, complete unsafe audits even when permitted, wrapper preconditions and cleanup, source-bound review invalidation and dependency policy. Reject incomplete FFI contracts, unsupported atomics, implicit narrowing, undeclared relaxed numerics, taint loss, incompatible sanitizers and unvalidated deserialization; preserve residual taint and secret redaction. Emit inspectable outcome, proof, trust and audit records; internal-only unsafe boundaries confer no safe public API. Explicit wrapping/saturating conversions retain their unresolved relation to checked/proven narrowing. Runtime/provider execution and domain-specific consumers require their own evidence. P12: proof consumers check theorem/source/certificate/hash-chain/trusted-kernel/assumption/profile/target agreement; unverified imports are ignored or rejected only as policy states. Proof production and symbolic lowering are separate Math producer interfaces; a profile check alone cannot close them. |
| Preserve obligations into MIR and runtime execution | [Safety pipeline](../../bootstrap/clojure/src/gravity/c10_safety_analysis/pipeline.clj) | Surviving check/proof/source edges; missing-check and unjustified-elision rejection. For SAFE1/SAFE2/SAFE8/SAFE9/SAFE15, invalidate or regenerate affected facts after source, expansion, type/effect, profile/target, numeric mode, memory/capability provider, pass/backend, dependency, grant or replay changes. Verify imported certificate schema, exact inputs, compatibility and trust before use. Erase only the exact proved condition with surviving backend assumptions and recorded check/proof identities; restore the check or reject on invalidation. Manual review alone is insufficient unless governing claim policy explicitly permits it; authority, audit, replay and human-review gates retain their separate policy obligations. |
| Close conformance for the declared profile surface | [Architecture](../phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md) | Accepted/rejected source fixtures, emitted facts and diagnostics for each claimed profile. Apply [SAFE16](../phase-02-safety/045-safe16-safety-conformance-test-plan.md) to every SAFE1-SAFE15 family: positive, negative and artifact-inspection fixtures; whole-fixture verdicts distinct from operation outcomes; diagnostic rule IDs and source spans rather than prose; checks-retained versus optimized output and actual backend preservation. Wrong artifacts fail despite correct diagnostics. Publish machine-readable expected/actual outcomes, compiler/profile/target, inspected artifacts, proof and island IDs and pass/fail. Unavailable targets stay open; no execution evidence is inferred from manifest fields. |
<!-- legality-task-projection:end -->

Hosted projection contracts deny canonical analysis authority; safety explicitly
records that its model is not complete. Recommendation for the analysis
maintainer's architecture gate: preserve profile, ownership and safety as
separate pass contracts even if one implementation traverses core once. Adopt
or reject before changing pass interfaces, not as a blanket block on fixtures.

## Acceptance and integration

Yes/no: are all dangerous operations classified? Do missing facts fail closed?
Do handwritten and macro-generated forms obey identical rules? Are ownership
and runtime-check obligations preserved through MIR/lowering? Can unsupported
profiles reach execution? (last must be no; others yes.)

Integration milestones: safe typed core with Auth; verified MIR handoff; native
check preservation with Lower/Runtime. Full hardware and OS conformance remains
separately tracked in [Platform](platform.md#release-outcome).

## Analysis ordering

Produce profile legality from typed/effected core first; Auth then validates
capability grants; ownership/lifetime and safety consume those facts before
MIR. Intermediate profile facts are an interface, not a claim that all Legality
is complete. This ordering avoids a whole-component Auth/Legality cycle while
preserving the canonical stage sequence. Auth's early artifact authentication
is a separate interface, not capability analysis over untyped reader products.
